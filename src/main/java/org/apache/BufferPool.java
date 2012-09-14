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
package org.apache;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * {@code BufferPool}
 * 
 * Created on Sep 10, 2012 at 10:59:08 AM
 * 
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 */
public class BufferPool<T> {

	private ConcurrentLinkedQueue<T> pool;
	private Class<T> clazz;

	/**
	 * Create a new instance of {@code BufferPool}
	 * 
	 * @param clazz
	 *            the class type of objects in the pool
	 */
	public BufferPool(Class<T> clazz) {
		this.clazz = clazz;
		this.pool = new ConcurrentLinkedQueue<T>();
	}

	/**
	 * @return the first element, if any, in the pool
	 * @throws Exception
	 */
	public T poll() throws Exception {
		T obj = this.pool.poll();

		if (obj == null) {
			obj = (T) clazz.newInstance();
		}

		return obj;
	}

	/**
	 * Insert the specified object, if it is not null, into the pool.
	 * 
	 * @param obj
	 */
	public void offer(T obj) {
		if (obj != null) {
			this.pool.offer(obj);
		}
	}

	/**
	 * @return true if the pool is empty
	 */
	public boolean isEmpty() {
		return this.pool.isEmpty();
	}

	/**
	 * Add all elements in the specified collection to the pool
	 * 
	 * @param c
	 */
	public void offer(Collection<T> c) {
		this.pool.addAll(c);
	}

	/**
	 * Remove all existing byte buffer in the pool
	 */
	public void clear() {
		this.pool.clear();
	}
}
