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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.apache.LifeCycleServiceAdapter;
import org.apache.coyote.Request;
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
	private boolean failedExist;
	private boolean running = false;
	private Object mutex;

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
		int count = 0;
		for (Node n : this.nodes) {
			if (n.isNodeUp()) {
				count++;
			}
		}

		return count;
	}

	/**
	 * @return the number of failed node
	 */
	public int getFailedNodes() {
		int count = 0;
		for (Node n : this.nodes) {
			if (n.isNodeDown()) {
				count++;
			}
		}

		return count;
	}

	/**
	 * @return a node
	 */
	private Node getNode() {
		return (this.nodes.isEmpty() ? null : getNode(0));
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
		if (n >= this.nodes.size()) {
			return null;
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
		if (failedNode != null) {
			// Set the node status to down
			// logger.warn("The node [" + failedNode.getHostname() + ":" +
			// failedNode.getPort() + "] is down");
			// failedNode.setNodeDown();
			// this.failedExist = true;
			// synchronized (this.mutex) {
			// mutex.notifyAll();
			// }
		}

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
			node.setNodeDown();
			failedExist = true;
			synchronized (this.mutex) {
				this.mutex.notifyAll();
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
			int count = 0;
			int seq = 0;
			while (running) {
				// Wait until there is at least one failed node or the system is
				// paused
				while (!failedExist || isPaused()) {
					System.out.println("[SEQ=" + (seq++) + "] Waiting for condition (active nodes = "+getActiveNodes()+")");
					synchronized (mutex) {
						try {
							// Waits at most 5 seconds
							mutex.wait(5000);
						} catch (InterruptedException e) {
							// NOPE
						}
					}
				}

				System.out.println("failedExist = " + failedExist + ", paused = " + isPaused());

				if (!running) {
					break;
				}

				logger.info("Starting health check for failed nodes");
				count = 0;
				for (Node node : nodes) {
					if (node.isNodeDown()) {
						if (checkHealth(node)) {
							node.setNodeUp();
							count++;
						}
					} else {
						count++;
					}
				}
				// Is there any failed node?
				failedExist = (count != nodes.size());

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
			if (node == null) {
				return false;
			}
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
