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
package org.apache.catalina.connector;

import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.CompletionHandler;
import java.util.HashMap;
import java.util.Map;

import org.apache.catalina.http.DataBuffer;
import org.apache.catalina.http.HttpResponseParser;
import org.apache.catalina.util.URLEncoder;
import org.apache.coyote.Adapter;
import org.apache.coyote.http11.AbstractInternalInputBuffer;
import org.apache.coyote.http11.AbstractInternalOutputBuffer;
import org.apache.coyote.http11.Http11AbstractProcessor;
import org.apache.coyote.http11.InternalNioOutputBuffer;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.buf.CharChunk;
import org.apache.tomcat.util.buf.MessageBytes;
import org.apache.tomcat.util.net.NioChannel;
import org.apache.tomcat.util.net.SocketStatus;
import org.jboss.cluster.proxy.container.Node;
import org.jboss.cluster.proxy.logging.Logger;

/**
 * {@code CoyoteAdapter}
 * <p>
 * Implementation of a request processor which delegates the processing to a
 * Coyote processor.
 * </p>
 * Created on Jun 20, 2012 at 2:21:46 PM
 * 
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 */
public class CoyoteAdapter implements Adapter {
	/**
	 * 
	 */
	private static Logger logger = Logger.getLogger(CoyoteAdapter.class);

	protected static final boolean ALLOW_BACKSLASH = Boolean.valueOf(
			System.getProperty("org.apache.catalina.connector.CoyoteAdapter.ALLOW_BACKSLASH",
					"false")).booleanValue();

	protected static final String X_POWERED_BY = System.getProperty(
			"org.apache.catalina.connector.CoyoteAdapter.X_POWERED_BY", "Servlet/3.0; JBossWeb-3");

	/**
	 * 
	 */
	public static final String NODE_DATA_BUFFER = "NODE_DATA_BUFFER";
	/**
	 * 
	 */
	public static final String HTTP_RESPONSE_PARSER = "HTTP_RESPONSE_PARSER";

	/**
	 * The CoyoteConnector with which this processor is associated.
	 */
	private Connector connector = null;

	/**
	 * The string manager for this package.
	 */
	protected org.apache.tomcat.util.res.StringManager sm = org.apache.tomcat.util.res.StringManager
			.getManager(Constants.Package);

	/**
	 * Encoder for the Location URL in HTTP redirects.
	 */
	protected static URLEncoder urlEncoder;

	/**
	 * The safe character set.
	 */
	static {
		urlEncoder = new URLEncoder();
		urlEncoder.addSafeCharacter('-');
		urlEncoder.addSafeCharacter('_');
		urlEncoder.addSafeCharacter('.');
		urlEncoder.addSafeCharacter('*');
		urlEncoder.addSafeCharacter('/');
	}

	/**
	 * Create a new instance of {@code CoyoteAdapter}
	 * <p>
	 * Construct a new CoyoteProcessor associated with the specified connector.
	 * </p>
	 * 
	 * @param connector
	 *            CoyoteConnector that owns this processor
	 */
	public CoyoteAdapter(Connector connector) {
		this.connector = connector;
	}

	/**
	 * Event method.
	 * 
	 * @return false to indicate an error, expected or not
	 */
	public boolean event(org.apache.coyote.Request req, org.apache.coyote.Response res,
			SocketStatus status) {

		// TODO

		return true;
	}

	/**
	 * Service method.
	 */
	public void service(final org.apache.coyote.Request request,
			final org.apache.coyote.Response response) throws Exception {

		Node node = this.connector.getNodeService().getNode(request.requestURI().getString());
		NioChannel channel = this.connector.getConnectionManager().getChannel(node);

		// Client request
		AbstractInternalInputBuffer inputBuffer = (AbstractInternalInputBuffer) request
				.getInputBuffer();

		final ByteBuffer inBuffer = (ByteBuffer) inputBuffer.getByteBuffer().clear();

		// Client response
		AbstractInternalOutputBuffer outputBuffer = (AbstractInternalOutputBuffer) response
				.getOutputBuffer();
		final ByteBuffer outBuffer = (ByteBuffer) outputBuffer.getByteBuffer().clear();

		// Put data to forward to the node in the byte buffer
		inBuffer.put(inputBuffer.getBuffer(), 0, inputBuffer.getLastValid()).flip();

		response.getResponseParser().recycle();

		// Put relevant elements in the map attachment
		final Map<String, Object> params = new HashMap<>();
		params.put(Constants.REQUEST_NAME, request);
		params.put(Constants.RESPONSE_NAME, response);
		params.put(Constants.IN_BUFFER_NAME, inBuffer);
		params.put(Constants.OUT_BUFFER_NAME, outBuffer);
		params.put(Constants.CHANNEL_NAME, channel);
		params.put(Constants.NODE_NAME, node);
		params.put(HTTP_RESPONSE_PARSER, response.getResponseParser());
		// Send the request to the selected node
		sendToNode(params);
	}

	/**
	 * Forwards the client request to the selected node.
	 * 
	 * @param params
	 *            a map containing all required parameters
	 */
	private void sendToNode(Map<String, Object> params) throws Exception {
		NioChannel channel = (NioChannel) params.get(Constants.CHANNEL_NAME);
		final ByteBuffer inBuffer = (ByteBuffer) params.get(Constants.IN_BUFFER_NAME);
		// Write the request to the server
		channel.write(inBuffer, params, new CompletionHandler<Integer, Map<String, Object>>() {

			@Override
			public void completed(Integer nBytes, Map<String, Object> attachment) {
				if (nBytes < 0) {
					failed(new ClosedChannelException(), attachment);
				} else {
					ByteBuffer buff = (ByteBuffer) attachment.get(Constants.IN_BUFFER_NAME);
					if (buff.hasRemaining()) {
						NioChannel ch = (NioChannel) attachment.get(Constants.CHANNEL_NAME);
						ch.write(buff, attachment, this);
					} else {
						// Read response from the node and forward it back to
						// client
						try {
							readFromNode(attachment);
						} catch (Exception exp) {
							failed(exp, attachment);
						}
					}
				}
			}

			@Override
			public void failed(Throwable exc, Map<String, Object> attachment) {
				logger.error(exc.getMessage(), exc);

				// TODO
			}
		});

	}

	/**
	 * 
	 * @param params
	 */
	private void readFromNode(Map<String, Object> params) throws Exception {

		final NioChannel nodeChannel = (NioChannel) params.get(Constants.CHANNEL_NAME);
		final ByteBuffer buffer = (ByteBuffer) params.get(Constants.IN_BUFFER_NAME);
		buffer.clear();
		params.put(NODE_DATA_BUFFER, new DataBuffer(new byte[buffer.capacity()]));

		// Read bytes from node.
		nodeChannel.read(buffer, params, new CompletionHandler<Integer, Map<String, Object>>() {

			private long contentLength = 0;

			@Override
			public void completed(Integer nBytes, Map<String, Object> attachment) {
				if (nBytes < 0) {
					failed(new ClosedChannelException(), attachment);
				} else if (nBytes > 0) {
					buffer.flip();

					org.apache.coyote.Response response = (org.apache.coyote.Response) attachment
							.get(Constants.RESPONSE_NAME);

					AbstractInternalOutputBuffer outputBuffer = (AbstractInternalOutputBuffer) response
							.getOutputBuffer();

					// Parse the HTTP Header
					HttpResponseParser httpResponseParser = (HttpResponseParser) attachment
							.get(HTTP_RESPONSE_PARSER);

					byte bytes[] = outputBuffer.getBytes();
					buffer.get(bytes, 0, nBytes);

					if (httpResponseParser.parsingHeader()) {
						httpResponseParser.parse(response, bytes, nBytes);
					}

					contentLength += nBytes;
					buffer.clear();
					outputBuffer.writeToClient(outputBuffer.getBytes(), 0, nBytes);

					if (this.contentLength < response.getContentLength()
							+ httpResponseParser.getHeaderLength()) {
						nodeChannel.read(buffer, attachment, this);
					} else {
						Http11AbstractProcessor<?> processor = (Http11AbstractProcessor<?>) response.hook;
						boolean chunked = response.isChunked();
						processor.endRequest();
						processor.nextRequest();
						if (!processor.isKeepAlive()) {
							processor.closeSocket();
							processor.recycle();
						} else if (chunked) {
							((InternalNioOutputBuffer) outputBuffer).configChunked(nodeChannel);
							processor.recycle();
						} else {
							processor.awaitForNext();
							processor.recycle();
						}
					}
				}
			}

			@Override
			public void failed(Throwable exc, Map<String, Object> attachment) {
				logger.error(exc.getMessage(), exc);
			}
		});
	}

	/**
	 * Character conversion of the a US-ASCII MessageBytes.
	 */
	protected void convertMB(MessageBytes mb) {

		// This is of course only meaningful for bytes
		if (mb.getType() != MessageBytes.T_BYTES)
			return;

		ByteChunk bc = mb.getByteChunk();
		CharChunk cc = mb.getCharChunk();
		int length = bc.getLength();
		cc.allocate(length, -1);

		// Default encoding: fast conversion
		byte[] bbuf = bc.getBuffer();
		char[] cbuf = cc.getBuffer();
		int start = bc.getStart();
		for (int i = 0; i < length; i++) {
			cbuf[i] = (char) (bbuf[i + start] & 0xff);
		}
		mb.setChars(cbuf, 0, length);

	}

	/**
	 * Normalize URI.
	 * <p>
	 * This method normalizes "\", "//", "/./" and "/../". This method will
	 * return false when trying to go above the root, or if the URI contains a
	 * null byte.
	 * 
	 * @param uriMB
	 *            URI to be normalized
	 * @return true
	 */
	public static boolean normalize(MessageBytes uriMB) {

		ByteChunk uriBC = uriMB.getByteChunk();
		byte[] b = uriBC.getBytes();
		int start = uriBC.getStart();
		int end = uriBC.getEnd();

		// An empty URL is not acceptable
		if (start == end)
			return false;

		// URL * is acceptable
		if ((end - start == 1) && b[start] == (byte) '*')
			return true;

		int pos = 0;
		int index = 0;

		// Replace '\' with '/'
		// Check for null byte
		for (pos = start; pos < end; pos++) {
			if (b[pos] == (byte) '\\') {
				if (ALLOW_BACKSLASH) {
					b[pos] = (byte) '/';
				} else {
					return false;
				}
			}
			if (b[pos] == (byte) 0) {
				return false;
			}
		}

		// The URL must start with '/'
		if (b[start] != (byte) '/') {
			return false;
		}

		// Replace "//" with "/"
		for (pos = start; pos < (end - 1); pos++) {
			if (b[pos] == (byte) '/') {
				while ((pos + 1 < end) && (b[pos + 1] == (byte) '/')) {
					copyBytes(b, pos, pos + 1, end - pos - 1);
					end--;
				}
			}
		}

		// If the URI ends with "/." or "/..", then we append an extra "/"
		// Note: It is possible to extend the URI by 1 without any side effect
		// as the next character is a non-significant WS.
		if (((end - start) >= 2) && (b[end - 1] == (byte) '.')) {
			if ((b[end - 2] == (byte) '/')
					|| ((b[end - 2] == (byte) '.') && (b[end - 3] == (byte) '/'))) {
				b[end] = (byte) '/';
				end++;
			}
		}

		uriBC.setEnd(end);

		index = 0;

		// Resolve occurrences of "/./" in the normalized path
		while (true) {
			index = uriBC.indexOf("/./", 0, 3, index);
			if (index < 0)
				break;
			copyBytes(b, start + index, start + index + 2, end - start - index - 2);
			end = end - 2;
			uriBC.setEnd(end);
		}

		index = 0;

		// Resolve occurrences of "/../" in the normalized path
		while (true) {
			index = uriBC.indexOf("/../", 0, 4, index);
			if (index < 0)
				break;
			// Prevent from going outside our context
			if (index == 0)
				return false;
			int index2 = -1;
			for (pos = start + index - 1; (pos >= 0) && (index2 < 0); pos--) {
				if (b[pos] == (byte) '/') {
					index2 = pos;
				}
			}
			copyBytes(b, start + index2, start + index + 3, end - start - index - 3);
			end = end + index2 - index - 3;
			uriBC.setEnd(end);
			index = index2;
		}

		uriBC.setBytes(b, start, end);

		return true;

	}

	/**
	 * Check that the URI is normalized following character decoding.
	 * <p>
	 * This method checks for "\", 0, "//", "/./" and "/../". This method will
	 * return false if sequences that are supposed to be normalized are still
	 * present in the URI.
	 * 
	 * @param uriMB
	 *            URI to be checked (should be chars)
	 * @return true if the URI is normalized
	 */
	public static boolean checkNormalize(MessageBytes uriMB) {

		CharChunk uriCC = uriMB.getCharChunk();
		char[] c = uriCC.getChars();
		int start = uriCC.getStart();
		int end = uriCC.getEnd();

		int pos = 0;

		// Check for '\' and 0
		for (pos = start; pos < end; pos++) {
			if (c[pos] == '\\') {
				return false;
			}
			if (c[pos] == 0) {
				return false;
			}
		}

		// Check for "//"
		for (pos = start; pos < (end - 1); pos++) {
			if (c[pos] == '/') {
				if (c[pos + 1] == '/') {
					return false;
				}
			}
		}

		// Check for ending with "/." or "/.."
		if (((end - start) >= 2) && (c[end - 1] == '.')) {
			if ((c[end - 2] == '/') || ((c[end - 2] == '.') && (c[end - 3] == '/'))) {
				return false;
			}
		}

		// Check for "/./"
		if (uriCC.indexOf("/./", 0, 3, 0) >= 0) {
			return false;
		}

		// Check for "/../"
		if (uriCC.indexOf("/../", 0, 4, 0) >= 0) {
			return false;
		}

		return true;

	}

	// ------------------------------------------------------ Protected Methods

	/**
	 * Copy an array of bytes to a different position. Used during
	 * normalization.
	 */
	protected static void copyBytes(byte[] b, int dest, int src, int len) {
		for (int pos = 0; pos < len; pos++) {
			b[pos + dest] = b[pos + src];
		}
	}

}
