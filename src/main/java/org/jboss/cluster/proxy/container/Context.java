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

import java.io.Serializable;

/**
 * {@code Context}
 * 
 * Created on Jun 12, 2012 at 4:24:58 PM
 * 
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 */
public class Context implements Serializable {

	/**
	 * {@code Status}
	 * <p>
	 * This class represents the status of the context.
	 * </p>
	 * Created on Jun 13, 2012 at 4:22:11 PM
	 * 
	 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
	 */
	public enum Status {
		/**
		 * 
		 */
		ENABLED,
		/**
		 * 
		 */
		DISABLED,
		/**
		 * 
		 */
		STOPPED;
	}

	/**
     *
     */
	private static final long serialVersionUID = -3107364662635260034L;
	/**
	 * Context: (String) URL to be mapped.
	 */
	private String name;
	/**
	 * Status of the application: ENABLED, DISABLED or STOPPED.
	 */
	private Status status;
	/**
	 * The context path
	 */
	private String path;
	
	/*
	 * The corresponding node identification.
	 */
	private String JVMRoute;
	
	/*
	 * The virtualhost id.
	 */
	private long hostid;

	/**
	 * Create a new instance of {@code Context}
	 */
	public Context() {
		super();
	}

	/**
	 * @return true if this context is enabled
	 */
	public boolean isEnabled() {
		return this.status == Status.ENABLED;
	}

	/**
	 * @return true if this context is disabled
	 */
	public boolean isDisabled() {
		return this.status == Status.DISABLED;
	}

	/**
	 * @return true if this context is stopped
	 */
	public boolean isStopped() {
		return this.status == Status.STOPPED;
	}

	/**
	 * Getter for name
	 * 
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Setter for the name
	 * 
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Getter for status
	 * 
	 * @return the status of the context
	 */
	public Status getStatus() {
		return this.status;
	}

	/**
	 * Setter for the status
	 * 
	 * @param status
	 *            the status to set
	 */
	public void setStatus(Status status) {
		this.status = status;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Context[Path: " + this.name + ", Status: " + this.status + "]";
	}

	/**
	 * Getter for path
	 *
	 * @return the path
	 */
	public String getPath() {
		return this.path;
	}

	/**
	 * Setter for the path
	 *
	 * @param path the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}

	public String getJVMRoute() {
		return JVMRoute;
	}

	public void setJVMRoute(String jVMRoute) {
		JVMRoute = jVMRoute;
	}

	public long getHostId() {
		return getHostid();
	}

	public long getHostid() {
		return hostid;
	}

	public void setHostid(long hostid) {
		this.hostid = hostid;
	}
}
