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
 * {@code InternalNioInputBuffer}
 * 
 * Created on Jul 3, 2012 at 10:27:38 AM
 * 
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 */
public class InternalNioInputBuffer implements InputBuffer {

	private static final Logger logger = Logger.getLogger(InternalNioInputBuffer.class);
	private NioChannel channel;
	private ByteBuffer buffer;
	private byte[] buff;
	private NioEndpoint endpoint;
	private long timeout;
	private org.apache.coyote.Request request;
	private CompletionHandler<Integer, NioChannel> completionHandler;
	private int pos;
	private int lastValid;

	/**
	 * Create a new instance of {@code InternalNioInputBuffer}
	 * 
	 * @param request
	 */
	public InternalNioInputBuffer(org.apache.coyote.Request request) {
		this.request = request;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jboss.cluster.proxy.http11.InputBuffer#init()
	 */
	public void init() {
		this.buffer = ByteBuffer.allocateDirect(Constants.MAX_POST_SIZE);
		this.buff = new byte[Constants.MAX_POST_SIZE];
		this.timeout = this.endpoint.getSoTimeout();
		this.completionHandler = new CompletionHandler<Integer, NioChannel>() {

			@Override
			public void completed(Integer nBytes, NioChannel attachment) {
				if (nBytes < 0) {
					failed(new ClosedChannelException(), attachment);
					return;
				}
			}

			@Override
			public void failed(Throwable exc, NioChannel attachment) {
				// TODO Auto-generated method stub

			}
		};
	}

	/**
	 * @return true if the number of bytes is greater than zero else false
	 * @throws Exception
	 */
	public boolean fill() throws Exception {
		int nRead = 0;
		this.buffer.clear();
		try {
			nRead = this.channel.readBytes(buffer, this.endpoint.getSoTimeout(),
					TimeUnit.MILLISECONDS);
			if (nRead > 0) {
				this.buffer.flip();
				this.buffer.get(buff, pos, nRead);
				lastValid += nRead;
			}
		} catch (Throwable t) {
			logger.error(t.getMessage(), t);
			throw t;
		}

		return nRead >= 0;
	}

	/**
	 * 
	 * @throws Exception
	 */
	public void getRequestLine() throws Exception {
		
		
	}

	/**
	 * 
	 */
	public void recycle() {
		this.channel = null;
		this.buffer.clear();
		this.request.recycle();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jboss.cluster.proxy.http11.InputBuffer#doRead(org.jboss.cluster.proxy
	 * .http11.Request)
	 */
	@Override
	public int doRead(Request request) {
		// TODO Auto-generated method stub
		return 0;
	}

}
