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
 * {@code Response}
 * 
 * Created on Jul 3, 2012 at 9:49:46 AM
 * 
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 */
public class Response {

	private Request request;
	private Map<String, String> headers;
	private int status;
	private String message;
	private ByteBuffer body;

	/**
	 * Create a new instance of {@code Response}
	 */
	public Response() {
		this.headers = new HashMap<>();
		this.body = ByteBuffer.allocateDirect(Constants.MAX_POST_SIZE);
	}

	/**
	 * Recycle the {@code Response} for next usage
	 */
	public void recycle() {
		this.body.clear();
		this.headers.clear();
		this.status = -1;
		this.message = "";
	}
	
	/**
	 * Add a new header to the list of headers.
	 * 
	 * @param name
	 * @param value
	 */
	public void addHeader(String name, String value) {
		this.headers.put(name, value);
	}

	/**
	 * Update the value of the header with the specified name. If no header with
	 * the specified name is present, so it just ignore the operation
	 * 
	 * @param name
	 * @param value
	 */
	public void setHeader(String name, String value) {
		if (this.headers.get(name) != null) {
			this.headers.put(name, value);
		}
	}

	/**
	 * Getter for request
	 * 
	 * @return the request
	 */
	public Request getRequest() {
		return this.request;
	}

	/**
	 * Setter for the request
	 * 
	 * @param request
	 *            the request to set
	 */
	public void setRequest(Request request) {
		this.request = request;
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

	/**
	 * Getter for message
	 * 
	 * @return the message
	 */
	public String getMessage() {
		return this.message;
	}

	/**
	 * Setter for the message
	 * 
	 * @param message
	 *            the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

}
