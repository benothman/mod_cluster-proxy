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
package org.jboss.cluster.proxy.container;

import org.apache.LifeCycleServiceAdapter;
import org.apache.coyote.Request;
import org.apache.tomcat.util.http.Cookies;
import org.apache.tomcat.util.http.ServerCookie;
import org.jboss.cluster.proxy.container.Node.NodeStatus;

/**
 * {@code MCMNodeService}
 * 
 * Created on Sep 1, 2012 at 10:45:11 AM
 * 
 * @author <a href="mailto:jclere@redhat.com">Jean-Frederic Clere</a>
 */
public class MCMNodeService extends LifeCycleServiceAdapter implements NodeService {
	static MCMConfig conf = MCMPAdapter.conf;

	@Override
	public void init() throws Exception {
		// Nothing to do :D
	}

	@Override
	public Node getNode(Request request) {
		System.out.println("MCMNodeService: getNode");
		Cookies cookies = request.getCookies();
		Balancer ba = null;
		String value = null;
		for (int i = 0; i < cookies.getCookieCount(); i++) {
			ServerCookie co = cookies.getCookie(i);
			String name = co.getName().getString();
			for (Balancer bal : conf.getBalancers()) {
				if (name.equals(bal.getStickySessionCookie())) {
					ba = bal;
					value = co.getValue().getString();
					break; // Found the balancer.
				}
			}
			if (ba != null)
				break;
		}

		Node node = null;
		if (ba != null) {
			// we have a balancer and a cookie
			int index = value.lastIndexOf('.');
			if (index != -1)
				node = conf.getNode(value.substring(index + 1));
		} else {
			// TODO complete code here
			node = conf.getNode();
		}
		System.out.println("getNode returns: " + node);
		return node;
	}

	@Override
	public Node getNode(Request request, Node failed) {
		System.out.println("MCMNodeService: getNode (failed:" + failed + ")");
		if (failed != null) {
			failed.setStatus(NodeStatus.NODE_DOWN);
			conf.insertupdate(failed);
		}
		return this.getNode(request);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jboss.cluster.proxy.container.NodeService#getActiveNodes()
	 */
	@Override
	public int getActiveNodes() {
		int count = 0;
		for (Node nod : conf.getNodes()) {
			if (nod.isNodeUp()) {
				count++;
			}
		}

		return count;
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
			conf.insertupdate(node);
		}
	}
}
