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
package org.jboss.cluster.proxy.util;

/**
 * {@code Constants}
 * 
 * Created on Jun 20, 2012 at 10:12:23 AM
 * 
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 */
public final class Constants {

	/**
	 * The delimiter between parameters
	 */
	public static final String PARAMETER_DELIMITER = "&";
	/**
	 * The delimiter between the parameter name and its value
	 */
	public static final String NAME_VALUE_DELIMITER = "=";

	/**
	 * CR.
	 */
	public static final byte CR = (byte) '\r';

	/**
	 * LF.
	 */
	public static final byte LF = (byte) '\n';

	/**
	 * SP.
	 */
	public static final byte SP = (byte) ' ';

	/**
	 * HT.
	 */
	public static final byte HT = (byte) '\t';

	/**
	 * COLON.
	 */
	public static final byte COLON = (byte) ':';

	/**
	 * Create a new instance of {@code Constants}
	 */
	private Constants() {
		super();
	}
}
