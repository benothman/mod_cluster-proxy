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
package org.jboss.cluster.proxy.util;

import java.io.Serializable;
import java.util.Map;

/**
 * {@code Request}
 * 
 * Created on Jun 14, 2012 at 12:33:05 PM
 * 
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 */
public interface Request extends Serializable {

	/**
	 * @return The type of the request
	 */
	public RequestType getType();

	/**
	 * @return <tt>true</tt> if the request is a wildcard
	 */
	public boolean isWildcard();

	/**
	 * @return the request parameters
	 */
	public Map<String, Object> getParameters();

	/**
	 * @return the JVM Route
	 */
	public String getJvmRoute();
	
	/**
	 * Returns the parameter, if any, associated with the specified name
	 * @param name the parameter name
	 * @return the parameter associated with the specified name
	 */
	public Object getParameter(String name);
}
