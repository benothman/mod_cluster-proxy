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
package org.apache.coyote;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * {@code BufferPool}
 * 
 * Created on Jul 4, 2012 at 4:07:45 PM
 * 
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 */
public final class BufferPool {

	public static final int DEFAULT_BUFFER_SIZE = Integer.valueOf(System.getProperty(
			"org.apache.coyote.BUFFER_SIZE", "" + (8 * 1024)));

	private int capacity;
	private ConcurrentLinkedQueue<ByteBuffer> pool;

	/**
	 * Create a new instance of {@code BufferPool}
	 */
	private BufferPool() {
		this(DEFAULT_BUFFER_SIZE);
	}

	/**
	 * Create a new instance of {@code BufferPool}
	 * 
	 * @param capacity
	 */
	private BufferPool(int capacity) {
		if (capacity <= 0) {
			throw new IllegalArgumentException("Negative or zero capacity value : " + capacity);
		}

		this.capacity = capacity;
		this.pool = new ConcurrentLinkedQueue<ByteBuffer>();
	}

	/**
	 * @return a new instance of {@code BufferPool} with default capacity
	 */
	public static BufferPool newInstance() {
		return new BufferPool();
	}

	/**
	 * @param capacity
	 * @return a new instance of {@code BufferPool} with the specified capacity
	 */
	public static BufferPool newInstance(int capacity) {
		return new BufferPool(capacity);
	}

	/**
	 * Destroy the buffer pool
	 */
	public void destroy() {
		this.pool.clear();
		this.pool = null;
	}

	/**
	 * Returns {@code true} if this queue contains no elements.
	 * 
	 * @return {@code true} if this queue contains no elements
	 */
	public boolean isEmpty() {
		return this.pool.isEmpty();
	}

	/**
	 * Retrieves and removes the head of the pool
	 * 
	 * @return an instance of {@code ByteBuffer}
	 */
	public ByteBuffer poll() {
		ByteBuffer buffer = this.pool.poll();
		if (buffer == null) {
			buffer = ByteBuffer.allocateDirect(this.capacity);
		}

		return buffer;
	}

	/**
	 * Put the buffer in the pool for next usage
	 * 
	 * @param buffer
	 */
	public void offer(ByteBuffer buffer) {
		buffer.clear();
		this.pool.offer(buffer);
	}

	/**
	 * 
	 * @param buffer
	 */
	public void offer(Collection<ByteBuffer> buffer) {
		this.pool.addAll(buffer);
	}

	/**
	 * 
	 * @param buffer
	 */
	public void offer(BufferPool bufferPool) {
		this.pool.addAll(bufferPool.pool);
	}

	/**
	 * Remove all existing byte buffer in the pool
	 */
	public void clear() {
		this.pool.clear();
	}
}
