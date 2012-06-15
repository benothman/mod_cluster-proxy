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


/**
 * {@code ResponseType}
 * 
 * Created on Jun 14, 2012 at 12:35:23 PM
 * 
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 */
public enum ResponseType {

	/**
	 * Response to CONFIG request
	 */
	CONFIG_RSP("CONFIG-RSP"),
	/**
	 * Response to ENABLE-APP request
	 */
	ENABLE_APP_RSP("ENABLE-APP-RSP"),
	/**
	 * Response to DISABLE-APP request
	 */
	DISABLE_APP_RSP("DISABLE-APP-RSP"),
	/**
	 * Response to STOP-APP (since version 0.2.0)
	 */
	STOP_APP_RSP("STOP-APP-RSP"),
	/**
	 * Response to a REMOVE_APP request
	 */
	REMOVE_APP_RSP("REMOVE-APP-RSP"),
	/**
	 * mod_cluster_manager response to a STATUS.
	 */
	STATUS_RSP("STATUS-RSP"),
	/**
	 * Response to INFO virtual host and listen address/port.
	 */
	INFO_RSP("INFO-RSP"),
	/**
	 * Response to DUMP.
	 */
	DUMP_RSP("DUMP-RSP"),
	/**
	 * Response to PING.
	 */
	PING_RSP("PING-RSP");

	/**
	 * The request type command
	 */
	private String command;

	/**
	 * Create a new instance of {@code RequestType}
	 * 
	 * @param command
	 */
	private ResponseType(String command) {
		this.command = command;
	}

	/**
	 * @return The command
	 */
	public String getCommand() {
		return this.command;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString() {
		return this.command;
	}
}
