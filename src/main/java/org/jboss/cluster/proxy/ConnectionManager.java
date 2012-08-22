/**
 * JBoss, Home of Professional Open Source. Copyright 2012, Red Hat, Inc., and
 * individual contributors as indicated by the @author tags. See the
 * copyright.txt file in the distribution for a full listing of individual
 * contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.jboss.cluster.proxy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;

import org.apache.LifeCycleServiceAdapter;
import org.apache.tomcat.util.net.NioChannel;
import org.apache.tomcat.util.net.NioChannelFactory;
import org.jboss.cluster.proxy.container.Node;
import org.jboss.logging.Logger;

/**
 * {@code ConnectionManager}
 * 
 * Created on Jun 20, 2012 at 3:25:09 PM
 * 
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 */
public class ConnectionManager extends LifeCycleServiceAdapter {

	private static final Logger logger = Logger
			.getLogger(ConnectionManager.class);
	private ConcurrentHashMap<String, ConcurrentLinkedQueue<NioChannel>> connections;
	private NioChannelFactory factory;

	/**
	 * Create a new instance of {@code ConnectionManager}
	 */
	public ConnectionManager() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.LifeCycleServiceAdapter#init()
	 */
    @Override
	public void init() throws Exception {
		if (isInitialized()) {
			return;
		}
		logger.info("Initializing Connection Manager");

		String secureStr = System.getProperty(Constants.SECURE_PROP_NAME, "false");
		boolean secure = Boolean.valueOf(secureStr).booleanValue();
		int nThreads = Runtime.getRuntime().availableProcessors() * 32;
		AsynchronousChannelGroup channelGroup = AsynchronousChannelGroup
				.withFixedThreadPool(nThreads, Executors.defaultThreadFactory());
		this.factory = NioChannelFactory.createNioChannelFactory(channelGroup,
				secure);
		this.factory.init();
		this.connections = new ConcurrentHashMap<>();
		setInitialized(true);
		logger.info("Connection Manager Initialized");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.LifeCycleServiceAdapter#destroy()
	 */
	public void destroy() throws Exception {
		logger.info("Destroying Connection Manager");
		this.factory.destroy();
		for (Collection<NioChannel> cl : this.connections.values()) {
			for (NioChannel nch : cl) {
				try {
					nch.close();
				} catch (IOException e) {
					// NOPE
				}
			}
			cl.clear();
		}

		this.connections.clear();
		this.connections = null;
		setInitialized(false);
		logger.info("Connection Manager Destroyed");
	}

	/**
	 * @param node
	 * @return a channel
	 */
	public NioChannel getChannel(Node node) {
		checkJvmRoute(node.getJvmRoute());
		NioChannel channel = null;
		do {
			channel = this.connections.get(node.getJvmRoute()).poll();
		} while (channel != null && channel.isClosed());

		if (channel == null) {
			channel = connect(node);
		}

		return channel;
	}

	/**
	 * Try to connect to the specified node
	 * 
	 * @param node
	 *            the node to which the channel will be connected
	 * @return a new connection to the node
	 */
	private NioChannel connect(Node node) {
		try {
			InetSocketAddress socketAddress = new InetSocketAddress(
					node.getHostname(), node.getPort());
			NioChannel channel = this.factory.connect(socketAddress);
			return channel;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	/**
	 * Recycle the node connection for next usage
	 * 
	 * @param jvmRoute
	 *            The node ID
	 * @param channel
	 *            the channel to be recycled
	 */
	public void recycle(String jvmRoute, NioChannel channel) {
		if (channel == null) {
			return;
		}
		if (channel.isOpen()) {
			checkJvmRoute(jvmRoute);
			this.connections.get(jvmRoute).offer(channel);
		}
	}

	/**
	 * Close the channel and update counters
	 * 
	 * @param channel
	 */
	public void close(NioChannel channel) {
		if (channel == null) {
			return;
		}

		try {
			channel.close();
		} catch (Exception exp) {
			// NOPE
			exp.printStackTrace();
		}
	}

	/**
	 * Check if there is already a connection list tied to the specified
	 * {@code jvmRoute}. If there is no list, then a new list is created and
	 * attached with it.
	 * 
	 * @param jvmRoute
	 *            the jvmRoute (it represents the ID of a node)
	 */
	private void checkJvmRoute(String jvmRoute) {
		if (this.connections.get(jvmRoute) == null) {
			this.connections.put(jvmRoute,
					new ConcurrentLinkedQueue<NioChannel>());
		}
	}
}
