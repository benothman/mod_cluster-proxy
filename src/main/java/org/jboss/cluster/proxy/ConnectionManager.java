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
import java.nio.channels.AsynchronousChannelGroup;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;

import org.apache.LifeCycleServiceAdapter;
import org.apache.tomcat.util.net.NioChannel;
import org.apache.tomcat.util.net.NioChannelFactory;
import org.jboss.cluster.proxy.container.Node;
import org.jboss.cluster.proxy.container.NodeService;
import org.jboss.logging.Logger;

/**
 * {@code ConnectionManager}
 * 
 * Created on Jun 20, 2012 at 3:25:09 PM
 * 
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 */
public class ConnectionManager extends LifeCycleServiceAdapter {

	private static final Logger logger = Logger.getLogger(ConnectionManager.class);
	private ConcurrentHashMap<String, ConcurrentLinkedQueue<NioChannel>> connections;
	private NioChannelFactory factory;

	private NodeService nodeService;

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

		int nThreads = Constants.DEFAULT_MAX_THREADS;
		String str = System.getProperty(Constants.MAX_THREAD_NAME);
		if (str != null) {
			try {
				nThreads = Integer.valueOf(str);
			} catch (NumberFormatException e) {
				logger.warn(e.getMessage());
			}
		}

		logger.info("Configure max thread number for nodes : " + nThreads);

		AsynchronousChannelGroup channelGroup = AsynchronousChannelGroup.withFixedThreadPool(
				nThreads, Executors.defaultThreadFactory());
		this.factory = NioChannelFactory.createNioChannelFactory(channelGroup, secure);
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
		this.stop();
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
	 * Try to retrieve a channel connected to the specified node
	 * 
	 * @param node the targeted node
	 * @return a channel
	 * @throws Exception
	 */
	public NioChannel getChannel(Node node) throws Exception {
		if (node == null) {
			return null;
		}
		checkJvmRoute(node.getJvmRoute());
		NioChannel channel = null;
		do {
			channel = this.connections.get(node.getJvmRoute()).poll();
		} while (channel != null && channel.isClosed());

		if (channel == null) {
			// This means that there is no connection available to the node, so
			// we should create a new connection.
			channel = connect(node);
		}

		return channel;
	}

	/**
	 * Try to connect to the remote host
	 * 
	 * @param hostname
	 *            the host name/IP
	 * @param port
	 *            the port number
	 * @return
	 * @throws Exception
	 */
	public NioChannel getChannel(String hostname, int port) throws Exception {
		return connect(hostname, port);
	}

	/**
	 * Try to connect to the specified node
	 * 
	 * @param node
	 *            the node to which the channel will be connected
	 * @return a new connection to the node
	 * @throws Exception
	 */
	private NioChannel connect(Node node) throws Exception {
		return connect(node.getHostname(), node.getPort());
	}

	/**
	 * Try to connect to the remote host specified by the host name and the port
	 * number
	 * 
	 * @param hostname
	 * @param port
	 * @return a new {@link NioChannel} representing the connection to the
	 *         remote host
	 * @throws Exception
	 */
	private NioChannel connect(String hostname, int port) throws Exception {
		return this.factory.connect(hostname, port);
	}

	/**
	 * Recycle the node connection for next usage
	 * 
	 * @param node
	 *            the node to which the channel is connected
	 * @param channel
	 *            the channel to be recycled
	 * @see {@link #recycle(String, NioChannel)}
	 */
	public void recycle(Node node, NioChannel channel) {
		if (node != null) {
			recycle(node.getJvmRoute(), channel);
		}
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
		if (jvmRoute == null || channel == null) {
			// NOTHING TO DO
			return;
		}

		if (channel.isOpen()) {
			// checkJvmRoute(jvmRoute);
			this.connections.get(jvmRoute).offer(channel);
		} else {
			close(channel);
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
	 * Check whether the specified JVMRoute is present in the table
	 * 
	 * @param jvmRoute
	 *            the JVMRoute to check
	 * @return
	 */
	public boolean jvmRouteExist(String jvmRoute) {
		return this.connections.containsKey(jvmRoute);
	}

	/**
	 * Remove the JVMRoute from the list of registered JVMRoutes
	 * 
	 * @param jvmRoute
	 */
	public void removeJvmRoute(String jvmRoute) {
		this.connections.remove(jvmRoute);
	}

	/**
	 * Check if there is already a connection list tied to the specified
	 * {@code jvmRoute}. If there is no list, then a new list is created and
	 * attached with it.
	 * 
	 * @param jvmRoute
	 *            the jvmRoute (it represents the ID of a node)
	 */
	protected void checkJvmRoute(String jvmRoute) {
		if (this.connections.get(jvmRoute) == null) {
			this.connections.put(jvmRoute, new ConcurrentLinkedQueue<NioChannel>());
		}
	}

	/**
	 * @return the nodeService
	 */
	public NodeService getNodeService() {
		return this.nodeService;
	}

	/**
	 * @param nodeService
	 *            the nodeService to set
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
}
