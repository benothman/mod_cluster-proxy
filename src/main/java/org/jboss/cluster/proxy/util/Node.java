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
import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@code Node}
 * 
 * Created on Jun 11, 2012 at 11:10:06 AM
 * 
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 */
public class Node implements Serializable {

	private static final AtomicInteger counter = new AtomicInteger(0);
	private long id;
	private String name;
	private int port;
	private InetAddress address;
	private int status = 0;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Create a new instance of {@code Node}
	 */
	public Node() {
		this("unknown", -1, null);
	}

	/**
	 * Create a new instance of {@code Node}
	 * 
	 * @param name
	 *            the node name
	 * @param port
	 *            the node port
	 * @param address
	 *            the node IP address
	 */
	public Node(String name, int port, InetAddress address) {
		this.name = name;
		this.port = port;
		this.address = address;
		this.id = counter.getAndIncrement();
	}

	/**
	 * Getter for id
	 * 
	 * @return the id
	 */
	public long getId() {
		return this.id;
	}

	/**
	 * Setter for the id
	 * 
	 * @param id
	 *            the id to set
	 */
	public void setId(long id) {
		this.id = id;
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
	 * Getter for port
	 * 
	 * @return the port
	 */
	public int getPort() {
		return this.port;
	}

	/**
	 * Setter for the port
	 * 
	 * @param port
	 *            the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * Getter for address
	 * 
	 * @return the address
	 */
	public InetAddress getAddress() {
		return this.address;
	}

	/**
	 * Setter for the address
	 * 
	 * @param address
	 *            the address to set
	 */
	public void setAddress(InetAddress address) {
		this.address = address;
	}

	/**
	 * Getter for status
	 * 
	 * @return the status
	 */
	public int getStatus() {
		return this.status;
	}

	/**
	 * Setter for the status
	 * 
	 * @param status
	 *            the status to set
	 */
	public void setStatus(int status) {
		this.status = status;
	}

}
