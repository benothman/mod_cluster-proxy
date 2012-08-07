/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.apache.coyote;

import java.io.IOException;
import java.util.Locale;

import org.apache.catalina.http.HttpResponseParser;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.http.MimeHeaders;

/**
 * Response object.
 * 
 * @author James Duncan Davidson [duncan@eng.sun.com]
 * @author Jason Hunter [jch@eng.sun.com]
 * @author James Todd [gonzo@eng.sun.com]
 * @author Harish Prabandham
 * @author Hans Bergsten <hans@gefionsoftware.com>
 * @author Remy Maucherat
 */
public final class Response {

	// ----------------------------------------------------------- Constructors

	/**
	 * Create a new instance of {@code Response}
	 */
	public Response() {
	}

	// ----------------------------------------------------- Class Variables

	/**
	 * Default locale as mandated by the spec.
	 */
	private static Locale DEFAULT_LOCALE = Locale.getDefault();

	// ----------------------------------------------------- Instance Variables

	private HttpResponseParser responseParser;

	/**
	 * Status code.
	 */
	protected int status = 200;

	/**
	 * Status message.
	 */
	protected String message = null;

	/**
	 * Response headers.
	 */
	protected MimeHeaders headers = new MimeHeaders();

	/**
	 * Associated output buffer.
	 */
	protected OutputBuffer outputBuffer;

	/**
	 * Notes.
	 */
	protected Object notes[] = new Object[Constants.MAX_NOTES];

	/**
	 * Committed flag.
	 */
	protected boolean commited = false;

	/**
	 * Action hook.
	 */
	public ActionHook hook;

	/**
	 * HTTP specific fields.
	 */
	protected String contentType = null;
	protected String contentLanguage = null;
	protected String transferEncoding = null;
	protected String characterEncoding = Constants.DEFAULT_CHARACTER_ENCODING;
	protected long contentLength = -1;
	private Locale locale = DEFAULT_LOCALE;

	// General informations
	private long bytesWritten = 0;

	/**
	 * Holds request error exception.
	 */
	protected Exception errorException = null;

	/**
	 * Has the charset been explicitly set.
	 */
	protected boolean charsetSet = false;

	/**
	 * Request error URI.
	 */
	protected String errorURI = null;

	protected Request req;

	protected int lastWrite = 1;
	protected boolean flushLeftovers = true;

	protected String sendfilePath = null;
	protected long sendfileStart = 0;
	protected long sendfileEnd = 0;

	// ------------------------------------------------------------- Properties

	/**
	 * @return The request tied to this response
	 */
	public Request getRequest() {
		return req;
	}

	/**
	 * Set the request tied to this response
	 * 
	 * @param req
	 */
	public void setRequest(Request req) {
		this.req = req;
	}

	/**
	 * @return the output buffer
	 */
	public OutputBuffer getOutputBuffer() {
		return outputBuffer;
	}

	/**
	 * Set the output buffer
	 * 
	 * @param outputBuffer
	 */
	public void setOutputBuffer(OutputBuffer outputBuffer) {
		this.outputBuffer = outputBuffer;
	}

	/**
	 * @return the mime headers
	 */
	public MimeHeaders getMimeHeaders() {
		return headers;
	}

	/**
	 * @return the action hook
	 */
	public ActionHook getHook() {
		return hook;
	}

	/**
	 * @param hook
	 */
	public void setHook(ActionHook hook) {
		this.hook = hook;
	}

	// -------------------- Per-Response "notes" --------------------

	/**
	 * @param pos
	 * @param value
	 */
	public final void setNote(int pos, Object value) {
		notes[pos] = value;
	}

	/**
	 * @param pos
	 * @return the object at the specified position
	 */
	public final Object getNote(int pos) {
		return notes[pos];
	}

	// -------------------- Actions --------------------

	/**
	 * @param actionCode
	 * @param param
	 */
	public void action(ActionCode actionCode, Object param) {
		if (hook != null) {
			if (param == null) {
				hook.action(actionCode, this);
			} else {
				hook.action(actionCode, param);
			}
		}
	}

	// -------------------- State --------------------

	/**
	 * @return <tt>true</tt>
	 */
	public boolean getFlushLeftovers() {
		return flushLeftovers;
	}

	/**
	 * @param flushLeftovers
	 */
	public void setFlushLeftovers(boolean flushLeftovers) {
		this.flushLeftovers = flushLeftovers;
	}

	/**
	 * @return the number of bytes written at the last operation
	 */
	public int getLastWrite() {
		flushLeftovers = false;
		return lastWrite;
	}

	/**
	 * @param lastWrite
	 */
	public void setLastWrite(int lastWrite) {
		this.lastWrite = lastWrite;
	}

	/**
	 * @return the response status code
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * Set the response status
	 * 
	 * @param status
	 */
	public void setStatus(int status) {
		this.status = status;
	}

	/**
	 * Get the status message.
	 * 
	 * @return the response reason phrase
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Set the status message.
	 * 
	 * @param message
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @return <tt>true</tt> if the response has been committed, else
	 *         <tt>false</tt>
	 */
	public boolean isCommitted() {
		return commited;
	}

	/**
	 * @param commited
	 */
	public void setCommitted(boolean commited) {
		this.commited = commited;
	}

	// -----------------Error State --------------------

	/**
	 * Set the error Exception that occurred during request processing.
	 * 
	 * @param ex
	 */
	public void setErrorException(Exception ex) {
		errorException = ex;
	}

	/**
	 * Get the Exception that occurred during request processing.
	 * 
	 * @return the error exception
	 */
	public Exception getErrorException() {
		return errorException;
	}

	/**
	 * @return <tt>true</tt> if there is an exception occurs during the request
	 *         processing
	 */
	public boolean isExceptionPresent() {
		return (errorException != null);
	}

	/**
	 * Set request URI that caused an error during request processing.
	 * 
	 * @param uri
	 */
	public void setErrorURI(String uri) {
		errorURI = uri;
	}

	/**
	 * Get the request URI that caused the original error.
	 * 
	 * @return the error URI
	 */
	public String getErrorURI() {
		return errorURI;
	}

	// -------------------- Methods --------------------

	/**
	 * @throws IllegalStateException
	 */
	public void reset() throws IllegalStateException {

		if (commited) {
			throw new IllegalStateException();
		}

		recycle();
	}

	/**
	 * @throws IOException
	 */
	public void finish() throws IOException {
		action(ActionCode.ACTION_CLOSE, this);
	}

	/**
	 * @throws IOException
	 */
	public void acknowledge() throws IOException {
		action(ActionCode.ACTION_ACK, this);
	}

	// -------------------- Headers --------------------
	/**
	 * Warning: This method always returns <code>false<code> for Content-Type
	 * and Content-Length.
	 * 
	 * @param name
	 * @return <tt>true</tt> if there is a header with the specified name
	 */
	public boolean containsHeader(String name) {
		return headers.getHeader(name) != null;
	}

	/**
	 * @param name
	 * @param value
	 */
	public void setHeader(String name, String value) {
		char cc = name.charAt(0);
		if (cc == 'C' || cc == 'c') {
			if (checkSpecialHeader(name, value))
				return;
		}
		headers.setValue(name).setString(value);
	}

	/**
	 * Add a new header to the list of headers with the specified name and value
	 * 
	 * @param name
	 *            the header name
	 * @param value
	 *            the header value
	 */
	public void addHeader(String name, String value) {
		char cc = name.charAt(0);
		if (cc == 'C' || cc == 'c') {
			if (checkSpecialHeader(name, value))
				return;
		}
		if (name.equalsIgnoreCase("transfer-encoding")) {
			setTransferEncoding(value);
		}
		headers.addValue(name).setString(value);
	}

	/**
	 * Set internal fields for special header names. Called from set/addHeader.
	 * Return true if the header is special, no need to set the header.
	 */
	private boolean checkSpecialHeader(String name, String value) {
		// XXX Eliminate redundant fields !!!
		// ( both header and in special fields )
		if (name.equalsIgnoreCase("Content-Type")) {
			setContentType(value);
			return true;
		}
		if (name.equalsIgnoreCase("Content-Length")) {
			try {
				long cL = Long.parseLong(value);
				setContentLength(cL);
				return true;
			} catch (NumberFormatException ex) {
				// Do nothing - the spec doesn't have any "throws"
				// and the user might know what he's doing
				return false;
			}
		}

		if (name.equalsIgnoreCase("Content-Language")) {
			// XXX XXX Need to construct Locale or something else
		}
		return false;
	}

	/**
	 * Signal that we're done with the headers, and body will follow. Any
	 * implementation needs to notify ContextManager, to allow interceptors to
	 * fix headers.
	 * 
	 * @throws IOException
	 */
	public void sendHeaders() throws IOException {
		action(ActionCode.ACTION_COMMIT, this);
		commited = true;
	}

	// -------------------- I18N --------------------

	/**
	 * @return the response Locale
	 */
	public Locale getLocale() {
		return locale;
	}

	/**
	 * Called explicitely by user to set the Content-Language and the default
	 * encoding
	 * 
	 * @param locale
	 */
	public void setLocale(Locale locale) {

		if (locale == null) {
			return; // throw an exception?
		}

		// Save the locale for use by getLocale()
		this.locale = locale;

		// Set the contentLanguage for header output
		contentLanguage = locale.getLanguage();
		if ((contentLanguage != null) && (contentLanguage.length() > 0)) {
			String country = locale.getCountry();
			StringBuilder value = new StringBuilder(contentLanguage);
			if ((country != null) && (country.length() > 0)) {
				value.append('-');
				value.append(country);
			}
			contentLanguage = value.toString();
		}

	}

	/**
	 * @return the content language.
	 */
	public String getContentLanguage() {
		return contentLanguage;
	}

	/**
	 * Overrides the name of the character encoding used in the body of the
	 * response. This method must be called prior to writing output using
	 * getWriter().
	 * 
	 * @param charset
	 *            String containing the name of the chararacter encoding.
	 */
	public void setCharacterEncoding(String charset) {

		if (isCommitted())
			return;
		if (charset == null)
			return;

		characterEncoding = charset;
		charsetSet = true;
	}

	/**
	 * @return the char encoding
	 */
	public String getCharacterEncoding() {
		return characterEncoding;
	}

	/**
	 * Getter for transferEncoding
	 * 
	 * @return the transferEncoding
	 */
	public String getTransferEncoding() {
		return this.transferEncoding;
	}

	/**
	 * Setter for the transferEncoding
	 * 
	 * @param value
	 *            the transferEncoding to set
	 */
	public void setTransferEncoding(String value) {
		this.transferEncoding = value;
	}

	/**
	 * @return <tt>true</tt> if the transfer encoding is set to chunked
	 */
	public boolean isChunked() {
		return "chunked".equalsIgnoreCase(this.transferEncoding);
	}
	
	/**
	 * Getter for responseParser
	 * 
	 * @return the responseParser
	 */
	public HttpResponseParser getResponseParser() {
		return this.responseParser;
	}

	/**
	 * Setter for the responseParser
	 * 
	 * @param responseParser
	 *            the responseParser to set
	 */
	public void setResponseParser(HttpResponseParser responseParser) {
		this.responseParser = responseParser;
	}

	/**
	 * Sets the content type.
	 * 
	 * This method must preserve any response charset that may already have been
	 * set via a call to response.setContentType(), response.setLocale(), or
	 * response.setCharacterEncoding().
	 * 
	 * @param type
	 *            the content type
	 */
	@SuppressWarnings("deprecation")
	public void setContentType(String type) {

		int semicolonIndex = -1;

		if (type == null) {
			this.contentType = null;
			return;
		}

		/*
		 * Remove the charset param (if any) from the Content-Type, and use it
		 * to set the response encoding. The most recent response encoding
		 * setting will be appended to the response's Content-Type (as its
		 * charset param) by getContentType();
		 */
		boolean hasCharset = false;
		int len = type.length();
		int index = type.indexOf(';');
		while (index != -1) {
			semicolonIndex = index;
			index++;
			while (index < len && Character.isSpace(type.charAt(index))) {
				index++;
			}
			if (index + 8 < len && type.charAt(index) == 'c' && type.charAt(index + 1) == 'h'
					&& type.charAt(index + 2) == 'a' && type.charAt(index + 3) == 'r'
					&& type.charAt(index + 4) == 's' && type.charAt(index + 5) == 'e'
					&& type.charAt(index + 6) == 't' && type.charAt(index + 7) == '=') {
				hasCharset = true;
				break;
			}
			index = type.indexOf(';', index);
		}

		if (!hasCharset) {
			this.contentType = type;
			return;
		}

		this.contentType = type.substring(0, semicolonIndex);
		String tail = type.substring(index + 8);
		int nextParam = tail.indexOf(';');
		String charsetValue = null;
		if (nextParam != -1) {
			this.contentType += tail.substring(nextParam);
			charsetValue = tail.substring(0, nextParam);
		} else {
			charsetValue = tail;
		}

		// The charset value may be quoted, but must not contain any quotes.
		if (charsetValue != null && charsetValue.length() > 0) {
			charsetSet = true;
			charsetValue = charsetValue.replace('"', ' ');
			this.characterEncoding = charsetValue.trim();
		}
	}

	/**
	 * @return the response content type
	 */
	public String getContentType() {

		String ret = contentType;

		if (ret != null && characterEncoding != null && charsetSet) {
			ret = ret + ";charset=" + characterEncoding;
		}

		return ret;
	}

	/**
	 * Set the response content length
	 * 
	 * @param contentLength
	 */
	public void setContentLength(int contentLength) {
		this.contentLength = contentLength;
	}

	/**
	 * 
	 * @param contentLength
	 */
	public void setContentLength(long contentLength) {
		this.contentLength = contentLength;
	}

	/**
	 * @return the content length
	 */
	public int getContentLength() {
		long length = getContentLengthLong();

		if (length < Integer.MAX_VALUE) {
			return (int) length;
		}
		return -1;
	}

	/**
	 * @return the content length
	 */
	public long getContentLengthLong() {
		return contentLength;
	}

	/**
	 * @return the send file path
	 */
	public String getSendfilePath() {
		return sendfilePath;
	}

	/**
	 * @param sendfilePath
	 */
	public void setSendfilePath(String sendfilePath) {
		this.sendfilePath = sendfilePath;
	}

	/**
	 * @return the send file start position
	 */
	public long getSendfileStart() {
		return sendfileStart;
	}

	/**
	 * @param sendfileStart
	 */
	public void setSendfileStart(long sendfileStart) {
		this.sendfileStart = sendfileStart;
	}

	/**
	 * @return the send file end position
	 */
	public long getSendfileEnd() {
		return sendfileEnd;
	}

	/**
	 * @param sendfileEnd
	 */
	public void setSendfileEnd(long sendfileEnd) {
		this.sendfileEnd = sendfileEnd;
	}

	/**
	 * Write a chunk of bytes.
	 * 
	 * @param chunk
	 * @throws IOException
	 */
	public void doWrite(ByteChunk chunk/* byte buffer[], int pos, int count */) throws IOException {
		outputBuffer.doWrite(chunk, this);
		bytesWritten += chunk.getLength();
	}

	// --------------------

	/**
	 * 
	 */
	public void recycle() {
		
		contentType = null;
		contentLanguage = null;
		this.transferEncoding = null;
		locale = DEFAULT_LOCALE;
		characterEncoding = Constants.DEFAULT_CHARACTER_ENCODING;
		charsetSet = false;
		contentLength = -1;
		status = 200;
		message = null;
		commited = false;
		errorException = null;
		errorURI = null;
		headers.clear();

		sendfilePath = null;

		// update counters
		lastWrite = 1;
		bytesWritten = 0;
		this.responseParser.recycle();
	}

	/**
	 * @return the total number of bytes written
	 */
	public long getBytesWritten() {
		return bytesWritten;
	}

	/**
	 * @param bytesWritten
	 */
	public void setBytesWritten(long bytesWritten) {
		this.bytesWritten = bytesWritten;
	}

}
