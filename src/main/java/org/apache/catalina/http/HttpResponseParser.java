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
package org.apache.catalina.http;

import org.apache.catalina.NodeResponse;
import org.apache.coyote.Response;
import org.apache.coyote.http11.Constants;
import org.jboss.logging.Logger;

/**
 * {@code HttpResponseParser}
 * 
 * Created on Jun 25, 2012 at 11:54:14 AM
 * 
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 */
public class HttpResponseParser {

	private static final Logger logger = Logger.getLogger(HttpResponseParser.class);

	private boolean parsingHeader = true;
	private boolean responseLine = false;
	private NodeResponse nodeResponse;
	private DataBuffer dataBuffer;
	private long headerLength = 0;
	private static final int INT_BASE = (int) '0';
	private StringBuilder headerNameBuffer = new StringBuilder();
	private StringBuilder headerValueBuffer = new StringBuilder();

	/**
	 * Create a new instance of {@code HttpResponseParser}
	 */
	public HttpResponseParser() {
		this.dataBuffer = new DataBuffer();
	}

	/**
	 * Create a new instance of {@code HttpResponseParser}
	 * 
	 * @param dataBuffer
	 */
	public HttpResponseParser(DataBuffer dataBuffer) {
		this.dataBuffer = dataBuffer;
	}

	/**
	 * Create a new instance of {@code HttpResponseParser}
	 * 
	 * @param nodeResponse
	 */
	public HttpResponseParser(NodeResponse nodeResponse) {
		this.nodeResponse = nodeResponse;
	}

	/**
	 * Parse the HTTP response headers and store them into the {@code response}
	 * object
	 * 
	 * @param response
	 * @param buf
	 *            the byte array containing the node response
	 * @param length
	 */
	public void parse(Response response, byte buf[], int length) {

		if (!this.parsingHeader) {
			return;
		}

		this.dataBuffer.setBytes(buf, 0, length);

		if (!this.responseLine) {
			// Retrieve the status line
			parseResponseLine(response, this.dataBuffer);
			// Skip CRLFs at the begin of the line
			while (this.dataBuffer.getByte() == Constants.CR
					|| this.dataBuffer.getByte() == Constants.LF) {
				this.dataBuffer.update();
			}
			this.responseLine = true;
		}

		// Parsing header
		parseHeaders(response, this.dataBuffer);
		this.headerLength = this.dataBuffer.position();
	}

	/**
	 * Retrieve the HTTP response line
	 * 
	 * @param response
	 * @param respBuff
	 */
	private void parseResponseLine(Response response, DataBuffer respBuff) {

		byte b = 0;
		// Skip blank lines
		while ((b = respBuff.getByte()) == Constants.CR || b == Constants.LF) {
			respBuff.update();
		}

		// Retrieving the protocol
		boolean space = false;

		while (!space) {
			b = respBuff.getAndUpdate();
			// Spec says single SP but it also says be tolerant of HT
			if (b == Constants.SP || b == Constants.HT) {
				space = true;
			}
		}

		// Skipping spaces, Spec says single SP but also says be tolerant of
		// multiple and/or HT
		while (space) {
			b = respBuff.getByte();
			if (b == Constants.SP || b == Constants.HT) {
				respBuff.update();
			} else {
				space = false;
			}
		}

		// Retrieving the status code
		int start = respBuff.position();
		while (!space) {
			b = respBuff.getByte();
			// Spec says single SP but it also says be tolerant of HT
			if (b == Constants.SP || b == Constants.HT) {
				space = true;
				int status = bytesToInt(respBuff.buf, start, respBuff.position() - start);
				response.setStatus(status);
			}
			respBuff.update();
		}

		// Retrieve the line
		StringBuilder sb = new StringBuilder();
		while ((b = respBuff.getByte()) != Constants.CR && b != Constants.LF) {
			sb.append((char) respBuff.getAndUpdate());
		}
		response.setMessage(sb.toString());
		this.headerLength += respBuff.position();
	}

	/**
	 * Parse the HTTP headers.
	 * 
	 * @param response
	 * @param respBuff
	 */
	private void parseHeaders(Response response, DataBuffer respBuff) {

		// Loop while there is more headers to parse
		while (parseHeader(response, respBuff)) {
			// NOPE
		}

		this.parsingHeader = false;
	}

	/**
	 * Parse an HTTP header.
	 * 
	 * @param response
	 * @param respBuff
	 * 
	 * @return false after reading a blank line (which indicates that the HTTP
	 *         header parsing is done
	 */
	private boolean parseHeader(Response response, DataBuffer respBuff) {
		// Check for blank line
		byte b = 0;
		while (true) {
			b = respBuff.getByte();
			if ((b == Constants.CR) || (b == Constants.LF)) {
				if (b == Constants.LF) {
					respBuff.update();
					return false;
				}
			} else {
				break;
			}

			respBuff.update();
		}

		// Prepare StringBuilders for the new header
		headerNameBuffer.delete(0, headerNameBuffer.length());
		headerValueBuffer.delete(0, headerValueBuffer.length());

		// Reading the header name
		// Header name is always US-ASCII
		boolean colon = false;
		// MessageBytes headerValue = null;
		String headerName = null;

		while (!colon) {
			b = respBuff.getByte();
			if (b == Constants.COLON) {
				colon = true;
				headerName = headerNameBuffer.toString();
			}

			if ((b >= Constants.A) && (b <= Constants.Z)) {
				respBuff.setByte((byte) (b - Constants.LC_OFFSET));
			}

			if (!colon) {
				headerNameBuffer.append((char) respBuff.getByte());
			}
			respBuff.update();
		}

		b = respBuff.getByte();
		boolean space = true;

		// Skipping spaces
		while (space) {
			b = respBuff.getByte();
			if (b == Constants.SP || b == Constants.HT) {
				respBuff.update();
			} else {
				space = false;
			}
		}

		while (b != Constants.CR && b != Constants.LF) {
			b = respBuff.getByte();
			headerValueBuffer.append((char) b);
			respBuff.update();
		}

		// Skip the LF at the end of the line
		while ((b = respBuff.getAndUpdate()) != Constants.LF) {
			respBuff.update();
		}

		// Set the header value
		String headerValueStr = headerValueBuffer.toString().trim();
		response.addHeader(headerName, headerValueStr);
		
		
		
		return true;
	}

	/**
	 * 
	 * @param bytes
	 * @param off
	 * @param length
	 * @return
	 */
	private static int bytesToInt(byte[] bytes, int off, int length) {
		int value = 0;
		int len = off + length;

		for (int i = off; i < len; i++) {
			value += (int) Math.pow(10, len - i - 1) * (bytes[i] - INT_BASE);
		}

		return value;
	}

	/**
	 * 
	 */
	public void recycle() {
		this.parsingHeader = true;
		this.responseLine = false;
		this.headerLength = 0;
		this.dataBuffer.clear();
	}

	/**
	 * Getter for headerLength
	 * 
	 * @return the headerLength
	 */
	public long getHeaderLength() {
		return this.headerLength;
	}

	/**
	 * @return <tt>true</tt> if the parse is parsing response header
	 */
	public boolean parsingHeader() {
		return this.parsingHeader;
	}

	/**
	 * Getter for nodeResponse
	 * 
	 * @return the nodeResponse
	 */
	public NodeResponse getNodeResponse() {
		return this.nodeResponse;
	}

	/**
	 * Setter for the nodeResponse
	 * 
	 * @param nodeResponse
	 *            the nodeResponse to set
	 */
	public void setNodeResponse(NodeResponse nodeResponse) {
		this.nodeResponse = nodeResponse;
	}
}
