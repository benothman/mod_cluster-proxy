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
package org.jboss.cluster.proxy;

/**
 * {@code StartException}
 * 
 * Created on Jun 20, 2012 at 10:32:09 AM
 * 
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 */
public class StartException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 182974928374293L;

	/**
	 * Create a new instance of {@code StartException}
	 */
	public StartException() {
		super();
	}

	/**
	 * Create a new instance of {@code StartException}
	 * 
	 * @param message
	 */
	public StartException(String message) {
		super(message);
	}

	/**
	 * Create a new instance of {@code StartException}
	 * 
	 * @param cause
	 */
	public StartException(Throwable cause) {
		super(cause);
	}

	/**
	 * Create a new instance of {@code StartException}
	 * 
	 * @param message
	 * @param cause
	 */
	public StartException(String message, Throwable cause) {
		super(message, cause);
	}
}
