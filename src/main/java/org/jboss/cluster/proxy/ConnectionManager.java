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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.tomcat.util.net.NioChannel;
import org.jboss.cluster.proxy.container.Node;

/**
 * {@code ConnectionManager}
 * 
 * Created on Jun 20, 2012 at 3:25:09 PM
 * 
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 */
public class ConnectionManager {

	private ConcurrentHashMap<String, ConcurrentLinkedQueue<NioChannel>> nodes;
	private boolean initialized = false;

	/**
	 * Create a new instance of {@code ConnectionManager}
	 */
	public ConnectionManager() {
		super();
	}

	/**
	 * Initialize the connection manager
	 */
	public void init() {
		if (initialized) {
			return;
		}

		this.nodes = new ConcurrentHashMap<>();
		initialized = true;
	}

	/**
	 * Destroy the connection manager
	 */
	public void destroy() {
		this.nodes.clear();
		this.nodes = null;
		this.initialized = false;
	}

	/**
	 * @param node
	 * @return a channel
	 */
	public NioChannel getChannel(Node node) {
		checkJvmRoute(node.getJvmRoute());
		NioChannel channel = this.nodes.get(node.getJvmRoute()).poll();
		if (channel == null) {
			channel = open(node);
		}

		return channel;
	}

	/**
	 * 
	 * @param node
	 * @return
	 */
	private NioChannel open(Node node) {

		try {
			InetSocketAddress address = new InetSocketAddress(node.getHostname(), node.getPort());
			NioChannel channel = NioChannel.open();
			channel.connect(address).get();
			return channel;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 
	 * @param jvmRoute
	 * @param channel
	 */
	public void recycle(String jvmRoute, NioChannel channel) {
		if (channel != null && channel.isOpen()) {
			checkJvmRoute(jvmRoute);
			this.nodes.get(jvmRoute).offer(channel);
		}
	}

	/**
	 * 
	 * @param jvmRoute
	 */
	private void checkJvmRoute(String jvmRoute) {
		if (this.nodes.get(jvmRoute) == null) {
			this.nodes.put(jvmRoute, new ConcurrentLinkedQueue<NioChannel>());
		}
	}
}
