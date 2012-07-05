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

/**
 * {@code DataBuffer}
 * 
 * Created on Jun 25, 2012 at 3:28:20 PM
 * 
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 */
public class DataBuffer {

	byte buf[];
	private int pos = 0;
	private int length;

	/**
	 * Create a new instance of {@code DataBuffer}
	 */
	public DataBuffer() {
		super();
	}

	/**
	 * Create a new instance of {@code DataBuffer}
	 * 
	 * @param buf
	 */
	public DataBuffer(byte buf[]) {
		this(buf, 0, buf.length);
	}

	/**
	 * Create a new instance of {@code DataBuffer}
	 * 
	 * @param buf
	 * @param pos
	 * @param length
	 */
	public DataBuffer(byte buf[], int pos, int length) {
		this.buf = buf;
		this.pos = pos;
		this.length = length;
	}

	/**
	 * 
	 */
	public void flip() {
		this.pos = 0;
	}

	/**
	 * 
	 */
	public void clear() {
		setBytes(null, 0, 0);
	}
	
	/**
	 * 
	 * @param buf
	 * @param pos
	 * @param length
	 */
	public void setBytes(byte[] buf, int pos, int length) {
		this.buf = buf;
		this.pos = pos;
		this.length = length;
	}

	/**
	 * 
	 * @param buf
	 */
	public void setBytes(byte[] buf) {
		setBytes(buf, 0, buf.length);
	}

	/**
	 * @return the current position
	 */
	public int position() {
		return this.pos;
	}

	/**
	 * Set the specified value at current position
	 * 
	 * @param value
	 */
	public void setByte(byte value) {
		setByte(this.pos, value);
	}

	/**
	 * @param position
	 * @param value
	 */
	public void setByte(int position, byte value) {
		this.buf[position] = value;
	}

	/**
	 * @return <tt>true</tt> if the buffer has more elements
	 */
	public boolean hasNext() {
		return this.pos < this.length;
	}

	/**
	 * @return the length of the buffer
	 */
	public int length() {
		return this.length;
	}

	/**
	 * @return the byte at the current position
	 */
	public byte getByte() {
		return getByte(this.pos);
	}

	/**
	 * @param position
	 * @return the byte at the specified position
	 */
	public byte getByte(int position) {
		return this.buf[position];
	}

	/**
	 * update the position
	 */
	public void update() {
		pos++;
	}

	/**
	 * @return the byte at the current position and increment the position
	 */
	public byte getAndUpdate() {
		return this.buf[this.pos++];
	}
}
