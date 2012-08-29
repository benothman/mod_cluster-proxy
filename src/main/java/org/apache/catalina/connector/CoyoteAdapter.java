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

import org.apache.catalina.http.HttpResponseParser;
import org.apache.catalina.util.URLEncoder;
import org.apache.coyote.Adapter;
import org.apache.coyote.Request;
import org.apache.coyote.Response;
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
	protected static final boolean ALLOW_BACKSLASH = Boolean
			.valueOf(
					System.getProperty(
							"org.apache.catalina.connector.CoyoteAdapter.ALLOW_BACKSLASH",
							"false")).booleanValue();

	protected static final String X_POWERED_BY = System.getProperty(
			"org.apache.catalina.connector.CoyoteAdapter.X_POWERED_BY",
			"Servlet/3.0; JBossWeb-3");

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
	public boolean event(Request req, Response res, SocketStatus status) {

		// TODO

		return false;
	}

	/**
	 * Service method.
	 */
	public void service(final Request request, Response response)
			throws Exception {
		prepare(request, response);
		// Send the request to the selected node
		sendToNode(request, response);
	}

	/**
	 * Forwards the client request to the selected node.
	 * 
	 * @param params
	 *            a map containing all required parameters
	 */
	private void sendToNode(final Request request, final Response response)
			throws Exception {
		final NioChannel nodeChannel = (NioChannel) response
				.getNote(Constants.NODE_CHANNEL_NOTE);
		final ByteBuffer inBuffer = (ByteBuffer) response
				.getNote(Constants.IN_BUFFER_NOTE);

		if (inBuffer.position() > 0) {
			inBuffer.flip();
		}
		// Write the request to the node
		nodeChannel.write(inBuffer, response,
				new CompletionHandler<Integer, Response>() {

					@Override
					public void completed(Integer nBytes, Response attachment) {
						if (nBytes < 0) {
							failed(new ClosedChannelException(), attachment);
						} else {
							ByteBuffer buff = (ByteBuffer) attachment
									.getNote(Constants.IN_BUFFER_NOTE);
							if (buff.hasRemaining()) {
								NioChannel ch = (NioChannel) attachment
										.getNote(Constants.NODE_CHANNEL_NOTE);
								ch.write(buff, attachment, this);
							} else {
								// Read response from the node and forward it
								// back
								// to
								// client
								try {
									readFromNode(attachment.getRequest(),
											attachment);
								} catch (Exception exp) {
									failed(exp, attachment);
								}
							}
						}
					}

					@Override
					public void failed(Throwable exc, Response attachment) {
						try {
							// try again with node
							tryWithNode(attachment);
							sendToNode(attachment.getRequest(), attachment);
						} catch (Throwable e) {
							e.printStackTrace();
						}
					}
				});
	}

	/**
	 * 
	 * @param params
	 */
	private void readFromNode(final org.apache.coyote.Request request,
			final org.apache.coyote.Response response) throws Exception {

		final NioChannel nodeChannel = (NioChannel) response
				.getNote(Constants.NODE_CHANNEL_NOTE);
		final ByteBuffer buffer = (ByteBuffer) response
				.getNote(Constants.OUT_BUFFER_NOTE);
		buffer.clear();

		// Read bytes from node.
		nodeChannel.read(buffer, response,
				new CompletionHandler<Integer, org.apache.coyote.Response>() {

					private long contentLength = 0;

					@Override
					public void completed(Integer nBytes, Response attachment) {
						if (nBytes < 0) {
							failed(new ClosedChannelException(), attachment);
						} else if (nBytes > 0) {
							ByteBuffer buff = (ByteBuffer) response
									.getNote(Constants.OUT_BUFFER_NOTE);
							buff.flip();

							AbstractInternalOutputBuffer outputBuffer = (AbstractInternalOutputBuffer) attachment
									.getOutputBuffer();

							// Parse the HTTP Header
							HttpResponseParser httpResponseParser = attachment
									.getResponseParser();

							byte data[] = outputBuffer.getBytes();
							buff.get(data, 0, nBytes);

							if (httpResponseParser.parsingHeader()) {
								httpResponseParser.parse(attachment, data,
										nBytes);
							}

							contentLength += nBytes;
							outputBuffer.setContentLength(attachment
									.getContentLengthLong()
									+ httpResponseParser.getHeaderLength());
							buff.clear();
							outputBuffer.writeToClient(outputBuffer.getBytes(),
									0, nBytes);

							if (this.contentLength < attachment
									.getContentLength()
									+ httpResponseParser.getHeaderLength()) {
								final NioChannel ch = (NioChannel) response
										.getNote(Constants.NODE_CHANNEL_NOTE);
								ch.read(buff, attachment, this);
							} else {
								Http11AbstractProcessor<?> processor = (Http11AbstractProcessor<?>) attachment.hook;
								boolean chunked = attachment.isChunked();
								processor.endRequest();
								processor.nextRequest();
								if (chunked) {
									((InternalNioOutputBuffer) outputBuffer)
											.configChunked(nodeChannel);
								} else {
									if (processor.isKeepAlive()) {
										processor.awaitForNext();
									} else {
										processor.closeSocket();
									}
									Node node = (Node) attachment
											.getNote(Constants.NODE_NOTE);
									NioChannel channel = (NioChannel) attachment
											.getNote(Constants.NODE_CHANNEL_NOTE);
									connector.getConnectionManager().recycle(
											node.getJvmRoute(), channel);
								}
							}
						}
					}

					@Override
					public void failed(Throwable exc,
							org.apache.coyote.Response attachment) {

						try {
							// try again with node
							tryWithNode(attachment);
							sendToNode(attachment.getRequest(), attachment);
						} catch (Throwable e) {
							e.printStackTrace();
						}
					}
				});
	}

	/**
	 * Prepare the request for processing
	 * 
	 * @param request
	 * @param response
	 */
	private void prepare(final org.apache.coyote.Request request,
			final org.apache.coyote.Response response) {

		Node node = this.connector.getNodeService().getNode(request);
		NioChannel nodeChannel = this.connector.getConnectionManager()
				.getChannel(node);

		// Client request
		AbstractInternalInputBuffer inputBuffer = (AbstractInternalInputBuffer) request
				.getInputBuffer();

		final ByteBuffer inBuffer = (ByteBuffer) inputBuffer.getByteBuffer();
		inBuffer.clear();

		// Client response
		AbstractInternalOutputBuffer outputBuffer = (AbstractInternalOutputBuffer) response
				.getOutputBuffer();
		final ByteBuffer outBuffer = (ByteBuffer) outputBuffer.getByteBuffer()
				.clear();

		// Put data to forward to the node in the byte buffer
		inBuffer.put(inputBuffer.getBuffer(), 0, inputBuffer.getLastValid())
				.flip();

		// Put relevant elements in the map attachment
		response.setNote(Constants.NODE_CHANNEL_NOTE, nodeChannel);
		response.setNote(Constants.NODE_NOTE, node);
		response.setNote(Constants.IN_BUFFER_NOTE, inBuffer);
		response.setNote(Constants.OUT_BUFFER_NOTE, outBuffer);
	}

	/**
	 * 
	 * @param response
	 */
	private void tryWithNode(org.apache.coyote.Response response) {
		// Closing the current channel
		NioChannel channel = (NioChannel) response
				.getNote(Constants.NODE_CHANNEL_NOTE);
		connector.getConnectionManager().close(channel);

		// Try with another node
		Node node = this.connector.getNodeService().getNode(
				response.getRequest().requestURI().getString());
		response.setNote(Constants.NODE_NOTE, node);
		channel = this.connector.getConnectionManager().getChannel(node);
		response.setNote(Constants.NODE_CHANNEL_NOTE, channel);
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
			copyBytes(b, start + index, start + index + 2, end - start - index
					- 2);
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
			copyBytes(b, start + index2, start + index + 3, end - start - index
					- 3);
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
			if ((c[end - 2] == '/')
					|| ((c[end - 2] == '.') && (c[end - 3] == '/'))) {
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
