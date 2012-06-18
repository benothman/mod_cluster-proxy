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
package org.jboss.cluster.proxy.load;

import org.jboss.cluster.proxy.container.Balancer;
import org.jboss.cluster.proxy.container.Context;
import org.jboss.cluster.proxy.container.Node;

/**
 * {@code LoadBalancer}
 * 
 * Created on Jun 13, 2012 at 4:06:39 PM
 * 
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 */
public interface LoadBalancer {

	/**
	 * @param context
	 * @return the node for the specified context
	 */
	public Node getNode(Context context);

	/**
	 * @return The {@link Balancer} used
	 */
	public Balancer getBalancer();

	/**
	 * @param name
	 *            of the {@link Balancer}
	 * @return the {@link Balancer} having the specified name
	 */
	public Balancer getBalancer(String name);

}
