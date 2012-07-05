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

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * {@code Request}
 * 
 * Created on Jul 3, 2012 at 9:49:39 AM
 * 
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 */
public class Request {

	private Response response;
	private Map<String, String> headers;
	private String method;
	private String uriPath;
	private String protocol;
	private ByteBuffer body;
	private long contentLength = -1;
	private String contentType;

	/**
	 * Create a new instance of {@code Request}
	 */
	public Request() {
		this.headers = new HashMap<>();
		this.body = ByteBuffer.allocateDirect(Constants.MAX_POST_SIZE);
	}

	/**
	 * Recycle the {@code Request} object for next usage
	 */
	public void recycle() {
		this.headers.clear();
		this.method = "";
		this.protocol = "";
		this.uriPath = "";
		this.contentLength = -1;
		this.contentType = "";
		this.body.clear();
	}

	/**
	 * 
	 * @param name
	 * @param value
	 */
	public void setHeader(String name, String value) {
		if (this.headers.containsKey(name)) {
			this.headers.put(name, value);
		}
	}

	/**
	 * 
	 * @param name
	 * @param value
	 */
	public void addHeader(String name, String value) {
		this.headers.put(name, value);
	}

	/**
	 * Getter for response
	 * 
	 * @return the response
	 */
	public Response getResponse() {
		return this.response;
	}

	/**
	 * Setter for the response
	 * 
	 * @param response
	 *            the response to set
	 */
	public void setResponse(Response response) {
		this.response = response;
	}

	/**
	 * Getter for headers
	 * 
	 * @return the headers
	 */
	public Map<String, String> getHeaders() {
		return this.headers;
	}

	/**
	 * Setter for the headers
	 * 
	 * @param headers
	 *            the headers to set
	 */
	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.method).append(' ').append(this.uriPath).append(' ').append(this.protocol)
				.append(Constants.CRLF);

		for (String name : this.headers.keySet()) {
			sb.append(name).append(": ").append(this.headers.get(name)).append(Constants.CRLF);
		}
		sb.append(Constants.CRLF);
		this.body.flip();
		byte[] bytes = new byte[this.body.remaining()];
		this.body.get(bytes);
		sb.append(new String(bytes)).append(Constants.CRLF);

		return sb.toString();
	}

	/**
	 * Getter for method
	 * 
	 * @return the method
	 */
	public String getMethod() {
		return this.method;
	}

	/**
	 * Setter for the method
	 * 
	 * @param method
	 *            the method to set
	 */
	public void setMethod(String method) {
		this.method = method;
	}

	/**
	 * Getter for uriPath
	 * 
	 * @return the uriPath
	 */
	public String getUriPath() {
		return this.uriPath;
	}

	/**
	 * Setter for the uriPath
	 * 
	 * @param uriPath
	 *            the uriPath to set
	 */
	public void setUriPath(String uriPath) {
		this.uriPath = uriPath;
	}

	/**
	 * Getter for protocol
	 * 
	 * @return the protocol
	 */
	public String getProtocol() {
		return this.protocol;
	}

	/**
	 * Setter for the protocol
	 * 
	 * @param protocol
	 *            the protocol to set
	 */
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	/**
	 * Getter for contentLength
	 * 
	 * @return the contentLength
	 */
	public long getContentLength() {
		return this.contentLength;
	}

	/**
	 * Setter for the contentLength
	 * 
	 * @param contentLength
	 *            the contentLength to set
	 */
	public void setContentLength(long contentLength) {
		this.contentLength = contentLength;
	}

	/**
	 * Getter for contentType
	 * 
	 * @return the contentType
	 */
	public String getContentType() {
		return this.contentType;
	}

	/**
	 * Setter for the contentType
	 * 
	 * @param contentType
	 *            the contentType to set
	 */
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

}
