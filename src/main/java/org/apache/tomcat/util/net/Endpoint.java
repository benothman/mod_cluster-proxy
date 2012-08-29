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
package org.apache.tomcat.util.net;

import java.io.Closeable;
import java.net.InetAddress;

/**
 * {@code Endpoint}
 * 
 * Created on Jun 13, 2012 at 4:12:16 PM
 * 
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 * @param <T> 
 */
public interface Endpoint<T extends Closeable> {

	/**
	 * Initialize the endpoint
	 * 
	 * @throws Exception
	 */
	public void init() throws Exception;

	/**
	 * Start the endpoint, creating acceptor, poller and sendfile threads, etc.
	 * 
	 * @throws Exception
	 */
	public void start() throws Exception;

	/**
	 * Pause the endpoint, which will make it stop accepting new sockets.
	 */
	public void pause();

	/**
	 * Resume the endpoint, which will make it start accepting new connections
	 * again.
	 */
	public void resume();

	/**
	 * Stop the endpoint. This will cause all processing threads to stop.
	 */
	public void stop();

	/**
	 * Deallocate the memory pools, and close server socket.
	 * 
	 * @throws Exception
	 */
	public void destroy() throws Exception;

	/**
	 * Process the specified object.
	 * @param obj
	 */
	public boolean process(T obj);
	
	/**
	 * @return the name of the endpoint
	 */
	public String getName();

	/**
	 * @param obj
	 */
	public void close(T obj);
	
	/**
	 * @return true if the endpoint is running
	 */
	public boolean isRunning();

	/**
	 * @return true if the endpoint is paused
	 */
	public boolean isPaused();

	/**
	 * @return true if the endpoint was initialized
	 */
	public boolean isInitialized();

	/**
	 * @return the endpoint Inet Address
	 */
	public InetAddress getAddress();

	/**
	 * @return the port number of the endpoint
	 */
	public int getPort();
	
}
