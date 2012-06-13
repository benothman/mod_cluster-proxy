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
package org.jboss.modcluster.proxy.util;

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
     *
     */
	private static final long serialVersionUID = 1L;
	/**
	 * Context: (String) URL to be mapped.
	 */
	private String name;
	/**
	 * Status of the application: ENABLED, DISABLED or STOPPED.
	 */
	private ContextStatus status;

	/**
	 * Create a new instance of {@code Context}
	 */
	public Context() {
		super();
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
	public ContextStatus getStatus() {
		return this.status;
	}

	/**
	 * Setter for the status
	 * 
	 * @param status
	 *            the status to set
	 */
	public void setStatus(ContextStatus status) {
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
}
