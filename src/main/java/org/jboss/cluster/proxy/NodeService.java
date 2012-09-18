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
import org.jboss.cluster.proxy.container.Node.NodeStatus;
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
public class NodeService extends LifeCycleServiceAdapter {

	private static final Logger logger = Logger.getLogger(NodeService.class);
	private List<Node> nodes;
	private List<Node> failedNodes;
	private Random random;
	private Runnable task;
	private Runnable healthCheckerTask;

	// private MCMConfig config = MCMPAdapter.conf;

	/**
	 * Create a new instance of {@code NodeService}
	 */
	public NodeService() {
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
		this.nodes = new ArrayList<Node>();
		this.failedNodes = new ArrayList<>();

		XmlNodes xmlNodes = XmlConfig.loadNodes();
		logger.info("Adding new nodes : " + xmlNodes);
		for (XmlNode n : xmlNodes.getNodes()) {
			Node node = new Node();
			node.setJvmRoute(UUID.randomUUID().toString());
			node.setHostname(n.getHostname());
			node.setPort(n.getPort());
			this.nodes.add(node);
		}
		setInitialized(true);

		this.healthCheckerTask = new Runnable() {

			@Override
			public void run() {
				List<Node> tmp = new ArrayList<>();
				while (true) {
					while (failedNodes.isEmpty()) {
						synchronized (failedNodes) {
							try {
								// max wait is 5 seconds
								failedNodes.wait(5000);
							} catch (InterruptedException e) {
								// NOPE
							}
						}
					}

					for (Node node : failedNodes) {
						if (checkHealth(node)) {
							node.setNodeUp();
							tmp.add(node);
						}
					}

					if (!tmp.isEmpty()) {
						for (Node node : tmp) {
							synchronized (nodes) {
								nodes.add(node);
							}
						}

						synchronized (failedNodes) {
							failedNodes.removeAll(tmp);
						}
						tmp.clear();
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
			 * @return <tt>true</tt> if the node is reachable else
			 *         <tt>false</tt>
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
		};

		task = new Runnable() {

			@Override
			public void run() {
				List<Node> tmp = new ArrayList<>();
				while (true) {
					try {
						Thread.sleep(5000);
						for (Node n : nodes) {
							if (n.getStatus() == NodeStatus.NODE_DOWN) {
								tmp.add(n);
							}
						}
						if (!tmp.isEmpty()) {
							nodes.removeAll(tmp);
							failedNodes.addAll(tmp);
							tmp.clear();
						}

						for (Node n : failedNodes) {
							if (n.getStatus() == NodeStatus.NODE_UP) {
								tmp.add(n);
							}
						}
						if (!tmp.isEmpty()) {
							failedNodes.removeAll(tmp);
							nodes.addAll(tmp);
							tmp.clear();
						}

						printNodes();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};

		logger.info("Node Service initialized");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.LifeCycleServiceAdapter#start()
	 */
	@Override
	public void start() throws Exception {
		// NOPE
		Thread t = new Thread(this.task);
		t.setDaemon(true);
		t.start();

		Thread healthThread = new Thread(this.healthCheckerTask);
		healthThread.setDaemon(true);
		healthThread.start();
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

	/**
	 * Select a node for the specified {@code Request}
	 * 
	 * @param request
	 * @return a node instance form the list of nodes
	 */
	public Node getNode(Request request) {
		// URI decoding
		// String requestURI = request.decodedURI().toString();

		// TODO complete code here

		return getNode();
	}

	public void printNodes() {
		StringBuilder sb = new StringBuilder("--> Available nodes : [");
		int i = 0;
		for (Node n : this.nodes) {
			sb.append(n.getHostname() + ":" + n.getPort());
			if ((i++) < this.nodes.size() - 1) {
				sb.append(", ");
			}
		}
		sb.append("]");
		logger.info(sb);
	}

	/**
	 * Select a new node for the specified request and mark the failed node as
	 * unreachable
	 * 
	 * @param request
	 * @param failedNode
	 * @return
	 */
	public Node getNode(Request request, Node failedNode) {

		/*
		 * if (failedNode != null) {
		 * logger.info("The node [" + failedNode.getHostname() + ":" +
		 * failedNode.getPort()
		 * + "] is failed --> removed from available nodes");
		 * failedNode.setStatus(NodeStatus.NODE_DOWN);
		 * failedNodes.add(failedNode);
		 * nodes.remove(failedNode);
		 * }
		 */

		return getNode(request);
	}

	/**
	 * 
	 * @param failedNode
	 */
	public void nodeDown(Node failedNode) {
		if (failedNode != null) {
			failedNode.setNodeDown();
		}
	}

}
