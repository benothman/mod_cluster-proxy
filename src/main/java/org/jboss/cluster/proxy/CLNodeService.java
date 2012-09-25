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

import java.net.StandardSocketOptions;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.LifeCycleServiceAdapter;
import org.apache.coyote.Request;
import org.apache.tomcat.util.net.NioChannel;
import org.jboss.cluster.proxy.container.Node;
import org.jboss.cluster.proxy.container.NodeService;
import org.jboss.cluster.proxy.xml.XmlConfig;
import org.jboss.cluster.proxy.xml.XmlNode;
import org.jboss.cluster.proxy.xml.XmlNodes;
import org.jboss.logging.Logger;

/**
 * {@code NodeService}
 * 
 * Created on Jun 20, 2012 at 3:16:46 PM
 * 
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 */
public class CLNodeService extends LifeCycleServiceAdapter implements NodeService {

	private static final Logger logger = Logger.getLogger(CLNodeService.class);
	private List<Node> nodes;
	private Random random;
	private boolean running = false;
	private Object mutex;
	private AtomicInteger activeNodes = new AtomicInteger(0);
	private ConnectionManager connectionManager;

	/**
	 * Create a new instance of {@code NodeService}
	 */
	public CLNodeService() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.LifeCycleServiceAdapter#init()
	 */
	public void init() throws Exception {
		if (isInitialized()) {
			return;
		}

		logger.info("Initializing Node Service");
		this.random = new Random();
		this.nodes = new ArrayList<>();

		XmlNodes xmlNodes = XmlConfig.loadNodes();
		logger.info("Adding new nodes : " + xmlNodes);
		for (XmlNode n : xmlNodes.getNodes()) {
			Node node = new Node();
			node.setHostname(n.getHostname());
			node.setPort(n.getPort());
			this.addNode(node);
		}
		this.mutex = new Object();
		setInitialized(true);
		this.activeNodes.set(nodes.size());
		logger.info("Node Service initialized");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.LifeCycleServiceAdapter#start()
	 */
	@Override
	public void start() throws Exception {
		if (!isInitialized()) {
			init();
		}
		// Start new thread for failed node health check
		Thread t = new Thread(new HealthChecker());
		t.setDaemon(true);
		t.setPriority(Thread.MIN_PRIORITY);
		t.start();
		setStarted(true);
		this.running = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.LifeCycleServiceAdapter#stop()
	 */
	@Override
	public void stop() throws Exception {
		this.running = false;
		setStarted(false);
		setPaused(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.LifeCycleServiceAdapter#destroy()
	 */
	@Override
	public void destroy() throws Exception {
		stop();
		this.nodes.clear();
		this.nodes = null;
		this.random = null;
		synchronized (this.mutex) {
			this.mutex.notifyAll();
		}

		setInitialized(false);
	}

	/**
	 * Add new node to the list of nodes
	 * 
	 * @param node
	 *            the node to be added
	 */
	public void addNode(Node node) {
		if (node == null) {
			return;
		}

		if (node.getJvmRoute() == null) {
			node.setJvmRoute(UUID.randomUUID().toString());
			synchronized (this.nodes) {
				nodes.add(node);
			}
			return;
		}

		for (Node n : this.nodes) {
			if (node.getJvmRoute().equals(n.getJvmRoute())) {
				return;
			}
		}
		// The node does not exist in the list, add it to the list
		synchronized (this.nodes) {
			nodes.add(node);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jboss.cluster.proxy.container.NodeService#getActiveNodes()
	 */
	@Override
	public int getActiveNodes() {
		return this.activeNodes.get();
	}

	/**
	 * @return the number of failed node
	 */
	public int getFailedNodes() {
		return this.nodes.size() - getActiveNodes();
	}

	/**
	 * @return <tt>true</tt> if there exist at least one failed node, else
	 *         <tt>false</tt>
	 */
	public boolean failedExist() {
		return getActiveNodes() != this.nodes.size();
	}

	/**
	 * @return a node
	 */
	private Node getNode() {
		return ((this.nodes.isEmpty() || getActiveNodes() <= 0) ? null : getNode(0));
	}

	/**
	 * Select a node randomly
	 * 
	 * @param n
	 *            the number of tries
	 * @return a {@link Node}
	 * @see #getNode()
	 */
	private Node getNode(int n) {
		// This means that we made enough random tests, so check all nodes and
		// return the first node available (if any)
		if (n >= this.nodes.size()) {
			int pos = 0;
			Node array[] = new Node[getActiveNodes()];
			for (Node nod : this.nodes) {
				if (nod.isNodeUp()) {
					array[pos++] = nod;
				}
			}

			return (pos > 0 ? array[random.nextInt(pos)] : null);
		} else {
			int index = random.nextInt(this.nodes.size());
			Node node = this.nodes.get(index);
			return (node.isNodeUp() ? node : getNode(n + 1));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jboss.cluster.proxy.container.NodeService#getNode(org.apache.coyote
	 * .Request)
	 */
	@Override
	public Node getNode(Request request) {
		// URI decoding
		// String requestURI = request.decodedURI().toString();

		// TODO complete code here

		return getNode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jboss.cluster.proxy.container.NodeService#getNode(org.apache.coyote
	 * .Request, org.jboss.cluster.proxy.container.Node)
	 */
	@Override
	public Node getNode(Request request, Node failedNode) {
		// Mark the node as failed
		failedNode(failedNode);
		// Check for another node
		return getNode(request);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jboss.cluster.proxy.container.NodeService#failedNode(org.jboss.cluster
	 * .proxy.container.Node)
	 */
	public void failedNode(Node node) {
		if (node != null) {
			synchronized (node) {
				if (!node.isNodeDown()) {
					// Update the node status to DOWN
					node.setNodeDown();
					// Update the number of active nodes
					this.activeNodes.decrementAndGet();
					logger.info("New failed node <" + node.getHostname() + ":" + node.getPort()
							+ ">");
					// Notify health checker thread
					synchronized (this.mutex) {
						this.mutex.notifyAll();
					}
				}
			}
		}
	}

	/**
	 * Print out the list nodes with their status
	 */
	public void printNodes() {
		StringBuilder sb = new StringBuilder("--> Registered nodes : {");
		int i = 0;
		for (Node n : this.nodes) {
			sb.append("[").append(n.getHostname()).append(":").append(n.getPort()).append(":")
					.append(n.getStatus()).append("]");
			if ((i++) < this.nodes.size() - 1) {
				sb.append(", ");
			}
		}
		sb.append("}");
		logger.info(sb);
	}

	/**
	 * @param connectionManager
	 *            the connectionManager to set
	 */
	public void setConnectionManager(ConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
	}

	/**
	 * @return the connectionManager
	 */
	public ConnectionManager getConnectionManager() {
		return connectionManager;
	}

	/**
	 * {@code HealthChecker}
	 * 
	 * Created on Sep 18, 2012 at 3:46:36 PM
	 * 
	 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
	 */
	private class HealthChecker implements Runnable {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			while (running) {
				// Wait until there is at least one failed node or the system is
				// paused
				while (!failedExist() || isPaused()) {
					synchronized (mutex) {
						try {
							// Waits at most 10 seconds
							mutex.wait(10000);
						} catch (InterruptedException e) {
							// NOPE
						}
					}
				}

				if (!running) {
					break;
				}

				for (Node node : nodes) {
					if (node.isNodeDown()) {
						if (checkHealth(node)) {
							// Update the node status to UP
							node.setNodeUp();
							// Update the number of active nodes
							activeNodes.incrementAndGet();
							logger.info("New available node <" + node.getHostname() + ":"
									+ node.getPort() + ">");
						}
					}
				}

				try {
					// Try after 5 seconds
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					// NOPE
				}
			}
		}

		/**
		 * Check the health of the failed node
		 * 
		 * @param node
		 * @return <tt>true</tt> if the node is reachable else <tt>false</tt>
		 */
		public boolean checkHealth(Node node) {
			return (node == null) ? false : ((connectionManager != null) ? checkHealth0(node)
					: checkHealth1(node));
		}

		/**
		 * Check node health using the connection manager
		 * 
		 * @param node
		 *            the node to check
		 * @return <tt>true</tt> if the node is reachable else <tt>false</tt>
		 */
		private boolean checkHealth0(Node node) {
			try {
				NioChannel channel = connectionManager.getChannel(node.getHostname(),
						node.getPort());

				if (channel.isOpen()) {
					// Put the channel in the recycled channel's list
					connectionManager.recycle(node, channel);
				} else {
					channel.setOption(StandardSocketOptions.SO_LINGER, 0);
					channel.close();
				}
				return true;
			} catch (Throwable th) {
				// NOPE
				return false;
			}
		}

		/**
		 * Check node health using standard Java Socket API
		 * 
		 * @param node
		 *            the node to check
		 * @return <tt>true</tt> if the node is reachable else <tt>false</tt>
		 */
		private boolean checkHealth1(Node node) {
			boolean ok = false;
			java.net.Socket s = null;
			try {
				s = new java.net.Socket(node.getHostname(), node.getPort());
				s.setSoLinger(true, 0);
				ok = true;
			} catch (Exception e) {
				// Ignore
			} finally {
				if (s != null) {
					try {
						s.close();
					} catch (Exception e) {
						// Ignore
					}
				}
			}

			return ok;
		}
	}
}
