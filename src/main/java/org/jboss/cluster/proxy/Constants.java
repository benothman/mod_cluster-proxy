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

/**
 * {@code Constants}
 * 
 * Created on Jun 13, 2012 at 11:12:54 AM
 * 
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 */
public final class Constants {

	/**
	 * 
	 */
	public static final int DEFAULT_CONNECTION_LINGER = -1;
	/**
	 * 
	 */
	public static final int DEFAULT_CONNECTION_TIMEOUT = 60000;
	/**
	 * 
	 */
	public static final int DEFAULT_CONNECTION_UPLOAD_TIMEOUT = 300000;
	/**
	 * 
	 */
	public static final int DEFAULT_SERVER_SOCKET_TIMEOUT = 0;
	/**
	 * 
	 */
	public static final boolean DEFAULT_TCP_NO_DELAY = true;

	/**
	 * Default buffer size
	 */
	public static final int DEFAULT_BUFFER_SIZE = 8 * 1024;

	/**
	 * Send configuration information for a node or set of nodes
	 */
	public static final String CONFIG_MSG = "CONFIG";
	/**
	 * 
	 */
	public static final byte[] CONFIG_MSG_BYTES = CONFIG_MSG.getBytes();

	/**
	 * Send requests and assign new sessions to the specified app. Use of to
	 * identify the app means enable all apps on the given node
	 */
	public static final String ENABLE_APP_MSG = "ENABLE-APP";
	/**
	 * 
	 */
	public static final byte[] ENABLE_APP_MSG_BYTES = ENABLE_APP_MSG.getBytes();

	/**
	 * apache should not create new session for this webapp, but still continue
	 * serving existing session on this node. Use of to identify the app means
	 * disable all apps on the given node
	 */
	public static final String DISABLE_APP_MSG = "DISABLE-APP";
	/**
	 * 
	 */
	public static final byte[] DISABLE_APP_MSG_BYTES = DISABLE_APP_MSG.getBytes();

	/**
	 * New requests for this webapp should not be sent to this node. Use of to
	 * identify the app means stop all apps on the given node
	 */
	public static final String STOP_APP_MSG = "STOP-APP";
	/**
	 * 
	 */
	public static final byte[] STOP_APP_MSG_BYTES = STOP_APP_MSG.getBytes();

	/**
	 * Response to STOP-APP (since version 0.2.0)
	 */
	public static final String STOP_APP_RSP_MSG = "STOP-APP-RSP";
	/**
	 * 
	 */
	public static final byte[] STOP_APP_RSP_MSG_BYTES = STOP_APP_RSP_MSG.getBytes();

	/**
	 * Remove the information about this webapp from mod_cluster tables
	 */
	public static final String REMOVE_APP_MSG = "REMOVE-APP";
	/**
	 * 
	 */
	public static final byte[] REMOVE_APP_MSG_BYTES = REMOVE_APP_MSG.getBytes();

	/**
	 * Send the current load balance factor for this node (or a set of nodes).
	 * Periodically sent
	 */
	public static final String STATUS_MSG = "STATUS";
	/**
	 * 
	 */
	public static final byte[] STATUS_MSG_BYTES = STATUS_MSG.getBytes();

	/**
	 * mod_cluster_manager response to a STATUS
	 */
	public static final String STATUS_RSP_MSG = "STATUS-RSP";
	/**
	 * 
	 */
	public static final byte[] STATUS_RSP_MSG_BYTES = STATUS_RSP_MSG.getBytes();

	/**
	 * Request configuration info from mod_cluster_manager
	 */
	public static final String INFO_MSG = "INFO";
	/**
	 * 
	 */
	public static final byte[] INFO_MSG_BYTES = INFO_MSG.getBytes();

	/**
	 * Response to INFO virtual host and listen address/port.
	 */
	public static final String INFO_RSP_MSG = "INFO-RSP";
	/**
	 * 
	 */
	public static final byte[] INFO_RSP_MSG_BYTES = INFO_RSP_MSG.getBytes();

	/**
	 * Request a text dump of the current configuration seen by
	 * mod_cluster_manager.
	 */
	public static final String DUMP_MSG = "DUMP";
	/**
	 * 
	 */
	public static final byte[] DUMP_MSG_BYTES = DUMP_MSG.getBytes();

	/**
	 * Response to DUMP.
	 */
	public static final String DUMP_RSP_MSG = "DUMP-RSP";
	/**
	 * 
	 */
	public static final byte[] DUMP_RSP_MSG_BYTES = DUMP_RSP_MSG.getBytes();

	/**
	 * Request a ping to proxy or node. The node could defined by JVMRoute or
	 * Scheme,Host and Port. (since version 0.1.0).
	 */
	public static final String PING_MSG = "PING";
	/**
	 * 
	 */
	public static final byte[] PING_MSG_BYTES = PING_MSG.getBytes();

	/**
	 * Response to PING.
	 */
	public static final String PING_RSP_MSG = "PING-RSP";
	/**
	 * 
	 */
	public static final byte[] PING_RSP_MSG_BYTES = PING_RSP_MSG.getBytes();

	/**
	 * HTTP/1.1.
	 */
	public static final String HTTP_11 = "HTTP/1.1";
	/**
	 * 
	 */
	public static final byte[] HTTP_11_BYTES = HTTP_11.getBytes();

	/**
	 * HTTP/1.0.
	 */
	public static final String HTTP_10 = "HTTP/1.0";
	/**
	 * 
	 */
	public static final byte[] HTTP_10_BYTES = HTTP_10.getBytes();

	/**
	 * 
	 */
	public static final String HTTP_11_200_OK = "HTTP/1.1 200 OK";

	/**
	 * 
	 */
	public static final byte[] HTTP_11_200_OK_BYTES = HTTP_11_200_OK.getBytes();

	/**
	 * The CRLF
	 */
	public static final String CRLF = "\r\n";
	/**
	 * 
	 */
	public static final byte[] CRLF_BYTES = CRLF.getBytes();

	/**
	 * CR.
	 */
	public static final byte CR = (byte) '\r';

	/**
	 * LF.
	 */
	public static final byte LF = (byte) '\n';

	/**
	 * SP.
	 */
	public static final byte SP = (byte) ' ';

	/**
	 * HT.
	 */
	public static final byte HT = (byte) '\t';

	/**
	 * COLON.
	 */
	public static final byte COLON = (byte) ':';

	/**
	 * Create a new instance of {@code Constants}
	 */
	private Constants() {
		// NOPE
	}

	/**
	 * Convert specified String to a byte array. This ONLY WORKS for ascii, UTF
	 * chars will be truncated.
	 * 
	 * @param value
	 *            to convert to byte array
	 * @return the byte array value
	 */
	public static final byte[] convertToBytes(String value) {
		byte[] result = new byte[value.length()];
		for (int i = 0; i < value.length(); i++) {
			result[i] = (byte) value.charAt(i);
		}
		return result;
	}
}
