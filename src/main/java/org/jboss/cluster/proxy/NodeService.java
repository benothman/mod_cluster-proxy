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

import org.jboss.cluster.proxy.container.Node;
import org.jboss.cluster.proxy.xml.XmlConfig;
import org.jboss.cluster.proxy.xml.XmlNode;
import org.jboss.cluster.proxy.xml.XmlNodes;

/**
 * {@code NodeService}
 * 
 * Created on Jun 20, 2012 at 3:16:46 PM
 * 
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 */
public class NodeService {

	private List<Node> nodes;
	private Random random;

	/**
	 * Create a new instance of {@code NodeService}
	 */
	public NodeService() {
		super();
	}

	/**
	 * @return a node
	 */
	public Node getNode() {
		int index = random.nextInt(this.nodes.size());
		return this.nodes.get(index);
	}

	/**
	 * @param contextPath
	 * @return a node hosting the specified context path
	 */
	public Node getNode(String contextPath) {
		// TODO
		return getNode();
	}

	/**
	 * @throws Exception
	 */
	public void init() throws Exception {
		this.random = new Random();
		this.nodes = new ArrayList<Node>();
		XmlNodes xmlNodes = XmlConfig.loadNodes();
		for (XmlNode n : xmlNodes.getNodes()) {
			Node node = new Node();
			node.setJvmRoute(UUID.randomUUID().toString());
			node.setHostname(n.getHostname());
			node.setPort(n.getPort());
			this.nodes.add(node);
		}
	}
}
