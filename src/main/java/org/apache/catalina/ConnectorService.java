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

import org.apache.coyote.Adapter;
import org.apache.coyote.ProtocolHandler;

/**
 * {@code ConnectorService}
 * 
 * Created on Aug 29, 2012 at 4:00:06 PM
 * 
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 */
public interface ConnectorService {

	public void init() throws Exception;

	public void start() throws Exception;

	public void pause() throws Exception;

	public void resume() throws Exception;

	public void stop() throws Exception;

	public ProtocolHandler getProtocolHandler();

	public void setProtocolHandler(ProtocolHandler protocolHandler);

	/**
	 * 
	 * @return
	 */
	public Adapter getAdapter();

	/**
	 * 
	 * @param adapter
	 */
	public void setAdapter(Adapter adapter);

	/**
	 * @param port
	 */
	public void setPort(int port);

	/**
	 * @param scheme
	 */
	public void setScheme(String scheme);

	/**
	 * @param enableLookups
	 */
	public void setEnableLookups(boolean enableLookups);

	/**
	 * @param maxPostSize
	 */
	public void setMaxPostSize(int maxPostSize);

	/**
	 * @param maxSavePostSize
	 */
	public void setMaxSavePostSize(int maxSavePostSize);

	/**
	 * @param proxyName
	 */
	public void setProxyName(String proxyName);

	/**
	 * @param proxyPort
	 */
	public void setProxyPort(int proxyPort);

	/**
	 * @param redirectPort
	 */
	public void setRedirectPort(int redirectPort);

	/**
	 * @param secure
	 */
	public void setSecure(boolean secure);
}
