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
package org.apache.coyote.http11;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.TimeUnit;

import org.apache.coyote.InputBuffer;
import org.apache.coyote.Request;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.net.NioChannel;
import org.apache.tomcat.util.net.NioEndpoint;
import org.apache.tomcat.util.net.SocketStatus;

/**
 * {@code InternalNioInputBuffer}
 * <p>
 * Implementation of InputBuffer which provides HTTP request header parsing as
 * well as transfer decoding.
 * </p>
 * 
 * Created on Dec 14, 2011 at 9:06:18 AM
 * 
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 */
public class InternalNioInputBuffer extends AbstractInternalInputBuffer {

	/**
	 * Underlying channel.
	 */
	protected NioChannel channel;

	/**
	 * Non blocking mode.
	 */
	protected boolean nonBlocking = false;

	/**
	 * Non blocking mode.
	 */
	protected boolean available = false;

	/**
	 * NIO end point.
	 */
	protected NioEndpoint endpoint = null;

	/**
	 * The completion handler used for asynchronous read operations
	 */
	private CompletionHandler<Integer, NioChannel> completionHandler;

	/**
	 * Create a new instance of {@code InternalNioInputBuffer}
	 * 
	 * @param request
	 * @param headerBufferSize
	 * @param endpoint
	 */
	public InternalNioInputBuffer(Request request, int headerBufferSize, NioEndpoint endpoint) {
		super(request, headerBufferSize);
		this.endpoint = endpoint;
		this.init();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.coyote.http11.AbstractInternalInputBuffer#init()
	 */
	public void init() {
		this.inputBuffer = new InputBufferImpl();
		this.readTimeout = (endpoint.getSoTimeout() > 0 ? endpoint.getSoTimeout()
				: Integer.MAX_VALUE);

		// Initialize the completion handler
		this.completionHandler = new CompletionHandler<Integer, NioChannel>() {

			@Override
			public void completed(Integer nBytes, NioChannel attachment) {
				if (nBytes < 0) {
					failed(new ClosedChannelException(), attachment);
					return;
				}

				if (nBytes > 0) {
					bbuf.flip();
					bbuf.get(buf, pos, nBytes);
					lastValid = pos + nBytes;
					endpoint.processChannel(attachment, SocketStatus.OPEN_READ);
				}
			}

			@Override
			public void failed(Throwable exc, NioChannel attachment) {
				endpoint.processChannel(attachment, SocketStatus.ERROR);
			}
		};
	}

	/**
	 * Available bytes (note that due to encoding, this may not correspond )
	 */
	public void useAvailable() {
		available = true;
	}

	/**
	 * Set the underlying channel.
	 * 
	 * @param channel
	 */
	public void setChannel(NioChannel channel) {
		this.channel = channel;
	}

	/**
	 * Get the underlying socket input stream.
	 * 
	 * @return the channel
	 */
	public NioChannel getChannel() {
		return channel;
	}

	/**
	 * Set the non blocking flag.
	 * 
	 * @param nonBlocking
	 */
	public void setNonBlocking(boolean nonBlocking) {
		this.nonBlocking = nonBlocking;
	}

	/**
	 * Get the non blocking flag value.
	 * 
	 * @return true if the buffer is non-blocking else false
	 */
	public boolean getNonBlocking() {
		return nonBlocking;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.coyote.http11.AbstractInternalInputBuffer#recycle()
	 */
	public void recycle() {
		super.recycle();
		bbuf.clear();
		channel = null;
		available = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.coyote.http11.AbstractInternalInputBuffer#nextRequest()
	 */
	public boolean nextRequest() {
		boolean result = super.nextRequest();
		nonBlocking = false;
		available = false;

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.coyote.InputBuffer#doRead(org.apache.tomcat.util.buf.ByteChunk
	 * , org.apache.coyote.Request)
	 */
	public int doRead(ByteChunk chunk, Request req) throws IOException {
		return (lastActiveFilter == -1) ? inputBuffer.doRead(chunk, req)
				: activeFilters[lastActiveFilter].doRead(chunk, req);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.coyote.http11.AbstractInternalInputBuffer#fill()
	 */
	public boolean fill() throws IOException {
		// Prepare the internal input buffer for reading
		this.prepare();
		// Read from client
		int nRead = this.blockingRead();

		if (nRead > 0) {
			bbuf.flip();
			bbuf.get(buf, pos, nRead);
			System.arraycopy(buf, pos, buf2, pos, nRead);
			lastValid = pos + nRead;
		} else if (nRead == NioChannel.OP_STATUS_CLOSED) {
			throw new IOException(sm.getString("iib.failedread"));
		} else if (nRead == NioChannel.OP_STATUS_READ_TIMEOUT) {
			throw new SocketTimeoutException(sm.getString("iib.failedread"));
		}

		return (nRead >= 0);
	}

	/**
	 * Prepare the input buffer for reading
	 */
	private void prepare() {
		bbuf.clear();

		if (parsingHeader) {
			if (lastValid == buf.length) {
				throw new IllegalArgumentException(sm.getString("iib.requestheadertoolarge.error"));
			}
		} else {
			if (buf.length - end < 4500) {
				// In this case, the request header was really large, so we
				// allocate a
				// brand new one; the old one will get GCed when subsequent
				// requests
				// clear all references
				buf = new byte[buf.length];
				end = 0;
			}
			pos = end;
			lastValid = pos;
		}
	}

	/**
	 * Close the channel
	 */
	private void close(NioChannel channel) {
		endpoint.close(channel);
	}

	/**
	 * Read a sequence of bytes in non-blocking mode from he current channel
	 * 
	 * @param bb
	 *            the byte buffer which will contain the bytes read from the
	 *            current channel
	 */
	private void nonBlockingRead(final ByteBuffer bb, long timeout, TimeUnit unit) {
		final NioChannel ch = this.channel;
		try {
			ch.read(bb, ch, this.completionHandler);
		} catch (Throwable t) {
			if (log.isDebugEnabled()) {
				log.debug("An error occurs when trying a non-blocking read ", t);
			}
		}
	}

	/**
	 * 
	 * @throws IOException
	 */
	protected void readAsync() throws IOException {
		this.prepare();
		this.nonBlockingRead(bbuf, readTimeout, TIME_UNIT);
	}

	/**
	 * Read a sequence of bytes in blocking mode from he current channel
	 * 
	 * @param bb
	 * @return the number of bytes read or -1 if the end of the stream was
	 *         reached
	 */
	protected int blockingRead() {
		int nr = 0;
		try {
			nr = this.channel.readBytes(this.bbuf, readTimeout, TIME_UNIT);
			if (nr < 0) {
				close(channel);
			}
		} catch (Throwable e) {
			if (log.isDebugEnabled()) {
				log.debug("An error occurs when trying a blocking read " + e.getMessage(), e);
			}
		}

		return nr;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.coyote.http11.AbstractInternalInputBuffer#readBytes(java.nio
	 * .ByteBuffer, long, java.util.concurrent.TimeUnit)
	 */
	public int readBytes(ByteBuffer dst, long timeout, TimeUnit unit) throws Exception {
		int n = this.channel.readBytes(dst, timeout, unit);
		if (n > 0) {
			lastValid += n;
		}

		return n;
	}

	/**
	 * This class is an input buffer which will read its data from an input
	 * stream.
	 */
	protected class InputBufferImpl implements InputBuffer {

		/**
		 * Read bytes into the specified chunk.
		 */
		public int doRead(ByteChunk chunk, Request req) throws IOException {

			if (pos >= lastValid) {
				if (!fill()) {
					return -1;
				}
			}

			int length = lastValid - pos;
			chunk.setBytes(buf, pos, length);
			pos = lastValid;

			return (length);
		}
	}
}
