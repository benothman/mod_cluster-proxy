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
package org.jboss.cluster.proxy.http11;

/**
 * {@code Constants}
 * 
 * Created on Jul 3, 2012 at 9:58:20 AM
 * 
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 */
public final class Constants {

	/**
	 * 
	 */
	public static final int MAX_POST_SIZE = 7 * 1500;

	public static final int DEFAULT_CONNECTION_LINGER = -1;
	public static final int DEFAULT_CONNECTION_TIMEOUT = 60000;
	public static final int DEFAULT_CONNECTION_UPLOAD_TIMEOUT = 300000;
	public static final int DEFAULT_SERVER_SOCKET_TIMEOUT = 0;
	public static final boolean DEFAULT_TCP_NO_DELAY = true;

	/**
	 * CRLF.
	 */
	public static final String CRLF = "\r\n";

	/**
	 * Server string.
	 */
	public static final byte[] SERVER_BYTES = ("Server: Apache-Coyote/1.1" + CRLF).getBytes();

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
	 * SEMI_COLON.
	 */
	public static final byte SEMI_COLON = (byte) ';';

	/**
	 * 'A'.
	 */
	public static final byte A = (byte) 'A';

	/**
	 * 'a'.
	 */
	public static final byte a = (byte) 'a';

	/**
	 * 'Z'.
	 */
	public static final byte Z = (byte) 'Z';

	/**
	 * '?'.
	 */
	public static final byte QUESTION = (byte) '?';

	/**
	 * Lower case offset.
	 */
	public static final byte LC_OFFSET = A - a;

	/**
	 * Default HTTP header buffer size.
	 */
	public static final int DEFAULT_HTTP_HEADER_BUFFER_SIZE = 48 * 1024;

	/**
	 * Minimum buffer size
	 */
	public static final int MIN_BUFFER_SIZE = 8 * 1024;

	/* Various constant "strings" */
	public static final byte[] CRLF_BYTES = CRLF.getBytes();
	public static final byte[] COLON_BYTES = ": ".getBytes();
	public static final String CONNECTION = "Connection";
	public static final String CLOSE = "close";
	public static final byte[] CLOSE_BYTES = CLOSE.getBytes();
	public static final String KEEPALIVE = "keep-alive";
	public static final byte[] KEEPALIVE_BYTES = KEEPALIVE.getBytes();
	public static final String CHUNKED = "chunked";
	public static final byte[] ACK_BYTES = ("HTTP/1.1 100 Continue" + CRLF + CRLF).getBytes();
	public static final String TRANSFERENCODING = "Transfer-Encoding";
	public static final byte[] _200_BYTES = "200".getBytes();
	public static final byte[] _400_BYTES = "400".getBytes();
	public static final byte[] _404_BYTES = "404".getBytes();

	/**
	 * Identity filters (input and output).
	 */
	public static final int IDENTITY_FILTER = 0;

	/**
	 * Chunked filters (input and output).
	 */
	public static final int CHUNKED_FILTER = 1;

	/**
	 * Void filters (input and output).
	 */
	public static final int VOID_FILTER = 2;

	/**
	 * GZIP filter (output).
	 */
	public static final int GZIP_FILTER = 3;

	/**
	 * Buffered filter (input)
	 */
	public static final int BUFFERED_FILTER = 3;

	/**
	 * HTTP/1.0.
	 */
	public static final String HTTP_10 = "HTTP/1.0";

	/**
	 * HTTP/1.1.
	 */
	public static final String HTTP_11 = "HTTP/1.1";
	/**
	 * 
	 */
	public static final byte[] HTTP_11_BYTES = HTTP_11.getBytes();

	/**
	 * GET.
	 */
	public static final String GET = "GET";

	/**
	 * HEAD.
	 */
	public static final String HEAD = "HEAD";

	/**
	 * POST.
	 */
	public static final String POST = "POST";

	/**
	 * Create a new instance of {@code Constants}
	 */
	private Constants() {
		super();
	}

}
