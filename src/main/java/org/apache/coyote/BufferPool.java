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
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * {@code BufferPool}
 * 
 * Created on Jul 4, 2012 at 4:07:45 PM
 * 
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 */
public class BufferPool {

	private static final ConcurrentLinkedQueue<ByteBuffer> POOL = new ConcurrentLinkedQueue<ByteBuffer>();

	private static final int BUFFER_SIZE = Integer.valueOf(System.getProperty(
			"org.apache.coyote.BUFFER_SIZE", "" + (8 * 1024)));

	/**
	 * Create a new instance of {@code BufferPool}
	 */
	private BufferPool() {
		super();
	}

	/**
	 * Retrieves and removes the head of the pool
	 * 
	 * @return an instance of {@code ByteBuffer}
	 */
	public static ByteBuffer poll() {
		ByteBuffer buffer = POOL.poll();
		if (buffer == null) {
			buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
		}

		return buffer;
	}

	/**
	 * Put the buffer in the pool for next usage
	 * 
	 * @param buffer
	 */
	public static void offer(ByteBuffer buffer) {
		buffer.clear();
		POOL.offer(buffer);
	}

}
