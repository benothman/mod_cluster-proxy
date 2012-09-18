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
		logger.info("Node Service initialized");

		Thread t = new Thread() {
			public void run() {
				while(true) {
					try {
						printNodes();
						sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		t.setDaemon(true);
		t.start();
	}

	/**
	 * @return a node
	 */
	private Node getNode() {
		if (!this.nodes.isEmpty()) {
			int index = random.nextInt(this.nodes.size());
			return this.nodes.get(index);
		}

		return null;
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
		StringBuilder sb = new StringBuilder(" Available nodes : [");
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
			failedNode.setStatus(NodeStatus.NODE_DOWN);
			failedNodes.add(failedNode);
			nodes.remove(failedNode);
		}
		printNodes();
	}

}
