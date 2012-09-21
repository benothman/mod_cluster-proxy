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

import org.apache.LifeCycleService;
import org.apache.coyote.Request;

/**
 * {@code NodeService}
 * <p>
 * This interface is an abstract facade to node service. Depending to the nature
 * of the node service to provide, the user should implement this interface and
 * write his/her own service.
 * </p>
 * Created on Sep 21, 2012 at 10:37:53 AM
 * 
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 */
public interface NodeService extends LifeCycleService {

	/**
	 * Select a node for the specified {@code Request}
	 * 
	 * @param request
	 * @return a node instance form the list of nodes
	 */
	public Node getNode(Request request);

	/**
	 * Select a new node for the specified request and mark the failed node as
	 * unreachable
	 * 
	 * @param request
	 * @param failedNode
	 * @return
	 */
	public Node getNode(Request request, Node failedNode);

	/**
	 * @return the number of active nodes, i.e., the number of nodes having
	 *         status to <tt>UP</tt>
	 */
	public int getActiveNodes();

}
