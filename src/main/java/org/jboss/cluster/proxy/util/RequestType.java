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
 * {@code RequestType}
 * 
 * Created on Jun 14, 2012 at 12:35:01 PM
 * 
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 */
public enum RequestType {

	/**
	 * Send configuration information for a node or set of nodes.
	 */
	CONFIG("CONFIG"),
	/**
	 * Send requests and assign new sessions to the specified app. Use of to
	 * identify the app means enable all apps on the given node.
	 */
	ENABLE_APP("ENABLE-APP"),
	/**
	 * apache should not create new session for this webapp, but still continue
	 * serving existing session on this node. Use of to identify the app means
	 * disable all apps on the given node.
	 */
	DISABLE_APP("DISABLE-APP"),
	/**
	 * New requests for this webapp should not be sent to this node. Use of to
	 * identify the app means stop all apps on the given node.
	 */
	STOP_APP("STOP-APP"),
	/**
	 * Remove the information about this webapp from mod_cluster tables.
	 */
	REMOVE_APP("REMOVE-APP"),
	/**
	 * Send the current load balance factor for this node (or a set of nodes).
	 * Periodically sent.
	 */
	STATUS("STATUS"),
	/**
	 * Request configuration info from mod_cluster_manager.
	 */
	INFO("INFO"),
	/**
	 * Request a text dump of the current configuration seen by
	 * mod_cluster_manager.
	 */
	DUMP("DUMP"),
	/**
	 * Request a ping to cluster proxy or node. The node could defined by
	 * JVMRoute or Scheme,Host and Port. (since version 0.1.0).
	 */
	PING("PING");

	/**
	 * The request type command
	 */
	private String command;

	/**
	 * Create a new instance of {@code RequestType}
	 * 
	 * @param command
	 */
	private RequestType(String command) {
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
