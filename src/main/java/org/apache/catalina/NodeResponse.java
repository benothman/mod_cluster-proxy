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
package org.apache.catalina;

import org.apache.catalina.http.HttpResponseParser;

/**
 * {@code Response}
 * 
 * Created on Jun 26, 2012 at 12:19:45 PM
 * 
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 */
public class NodeResponse {

	private org.apache.coyote.Response coyoteResponse;
	private HttpResponseParser httpParser;

	/**
	 * Create a new instance of {@code NodeResponse}
	 * 
	 * @param coyoteResponse
	 */
	public NodeResponse(org.apache.coyote.Response coyoteResponse) {
		this.coyoteResponse = coyoteResponse;
		this.httpParser = new HttpResponseParser(this);
	}

	/**
	 * 
	 */
	public void recycle() {
		this.httpParser.recycle();
	}

	/**
	 * Getter for coyoteResponse
	 * 
	 * @return the coyoteResponse
	 */
	public org.apache.coyote.Response getCoyoteResponse() {
		return this.coyoteResponse;
	}

	/**
	 * Setter for the coyoteResponse
	 * 
	 * @param coyoteResponse
	 *            the coyoteResponse to set
	 */
	public void setCoyoteResponse(org.apache.coyote.Response coyoteResponse) {
		this.coyoteResponse = coyoteResponse;
	}

	/**
	 * Getter for httpParser
	 * 
	 * @return the httpParser
	 */
	public HttpResponseParser getHttpParser() {
		return this.httpParser;
	}

	/**
	 * Setter for the httpParser
	 * 
	 * @param httpParser
	 *            the httpParser to set
	 */
	public void setHttpParser(HttpResponseParser httpParser) {
		this.httpParser = httpParser;
	}

}
