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
import java.nio.channels.ClosedChannelException;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.TimeUnit;

import org.apache.tomcat.util.net.NioChannel;
import org.apache.tomcat.util.net.NioEndpoint;
import org.jboss.logging.Logger;

/**
 * {@code InternalNioOutputBuffer}
 * 
 * Created on Jul 3, 2012 at 10:28:33 AM
 * 
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 */
public class InternalNioOutputBuffer implements OutputBuffer {

	
	private static final Logger logger = Logger.getLogger(InternalNioOutputBuffer.class);
	
	private NioChannel channel;
	private Response response;
	private ByteBuffer buffer;
	private NioEndpoint endpoint;
	private CompletionHandler<Integer, NioChannel> completionHandler;
	private long timeout;

	/**
	 * Create a new instance of {@code InternalNioOutputBuffer}
	 */
	public InternalNioOutputBuffer() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jboss.cluster.proxy.http11.OutputBuffer#init()
	 */
	public void init() {
		this.buffer = ByteBuffer.allocateDirect(Constants.MAX_POST_SIZE);
		this.timeout = endpoint.getSoTimeout();
		this.completionHandler = new CompletionHandler<Integer, NioChannel>() {

			@Override
			public void completed(Integer nBytes, NioChannel attachment) {
				if (nBytes < 0) {
					failed(new ClosedChannelException(), attachment);
					return;
				}

				if (buffer.hasRemaining()) {
					attachment.write(buffer, timeout, TimeUnit.MILLISECONDS, attachment, this);
				}
			}

			@Override
			public void failed(Throwable exc, NioChannel attachment) {
				// TODO Auto-generated method stub

			}
		};
	}

	/**
	 * 
	 */
	public void recycle() {
		this.channel = null;
		this.buffer.clear();
	}

	/**
	 * 
	 */
	public void flushBuffer() {
		try {
			this.channel.write(buffer, timeout, TimeUnit.MILLISECONDS, this.channel,
					this.completionHandler);
		} catch (Throwable t) {
			
		}
	}

	/**
	 * 
	 * @param ch
	 */
	public void closeChannel(NioChannel ch) {

	}

	public void writeHeader(String name, String value) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jboss.cluster.proxy.http11.OutputBuffer#doWrite(org.jboss.cluster
	 * .proxy.http11.Response)
	 */
	@Override
	public int doWrite(Response response) {

		return 0;
	}

	/**
	 * Getter for channel
	 * 
	 * @return the channel
	 */
	public NioChannel getChannel() {
		return this.channel;
	}

	/**
	 * Setter for the channel
	 * 
	 * @param channel
	 *            the channel to set
	 */
	public void setChannel(NioChannel channel) {
		this.channel = channel;
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
	 * Getter for buffer
	 * 
	 * @return the buffer
	 */
	public ByteBuffer getBuffer() {
		return this.buffer;
	}

	/**
	 * Setter for the buffer
	 * 
	 * @param buffer
	 *            the buffer to set
	 */
	public void setBuffer(ByteBuffer buffer) {
		this.buffer = buffer;
	}

	/**
	 * Getter for endpoint
	 * 
	 * @return the endpoint
	 */
	public NioEndpoint getEndpoint() {
		return this.endpoint;
	}

	/**
	 * Setter for the endpoint
	 * 
	 * @param endpoint
	 *            the endpoint to set
	 */
	public void setEndpoint(NioEndpoint endpoint) {
		this.endpoint = endpoint;
	}

}
