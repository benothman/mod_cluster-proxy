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
 * {@code Constants}
 * 
 * Created on Jun 13, 2012 at 11:12:54 AM
 * 
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 */
public interface Constants {

	public static final int DEFAULT_MAX_THREADS = Runtime.getRuntime()
			.availableProcessors() * 32;

	/**
	 * 
	 */
	public static final String PACKAGE = Constants.class.getPackage().getName();

	/**
	 * 
	 */
	public static final String SECURE_PROP_NAME = "org.apache.tomcat.util.net.factory.SECURE";

	/**
	 * 
	 */
	public static final String MAX_THREAD_NAME = "org.jboss.cluster.proxy.MAX_THREADS";

	/**
	 * 
	 */
	public static final int DEFAULT_CONNECTION_LINGER = -1;
	/**
	 * 
	 */
	public static final int DEFAULT_CONNECTION_TIMEOUT = 60000;
	/**
	 * 
	 */
	public static final int DEFAULT_CONNECTION_UPLOAD_TIMEOUT = 300000;
	/**
	 * 
	 */
	public static final int DEFAULT_SERVER_SOCKET_TIMEOUT = 0;
	/**
	 * 
	 */
	public static final boolean DEFAULT_TCP_NO_DELAY = true;

	/**
	 * Default buffer size
	 */
	public static final int DEFAULT_BUFFER_SIZE = 8 * 1024;

	/**
	 * HTTP/1.1.
	 */
	public static final String HTTP_11 = "HTTP/1.1";
	/**
	 * 
	 */
	public static final byte[] HTTP_11_BYTES = HTTP_11.getBytes();

	/**
	 * HTTP/1.0.
	 */
	public static final String HTTP_10 = "HTTP/1.0";
	/**
	 * 
	 */
	public static final byte[] HTTP_10_BYTES = HTTP_10.getBytes();

	/**
	 * 
	 */
	public static final String HTTP_11_200_OK = "HTTP/1.1 200 OK";

	/**
	 * 
	 */
	public static final byte[] HTTP_11_200_OK_BYTES = HTTP_11_200_OK.getBytes();

	/**
	 * 
	 */
	public static final String HTTP_11_500_ERROR = "HTTP/1.1 500 ERROR";
	/**
	 * 
	 */
	public static final byte[] HTTP_11_500_ERROR_BYTES = HTTP_11_500_ERROR
			.getBytes();

	/**
	 * The CRLF
	 */
	public static final String CRLF = "\r\n";
	/**
	 * 
	 */
	public static final byte[] CRLF_BYTES = CRLF.getBytes();

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

	// ---------------

	/**
	 * 
	 */
	public static final String ACCESS_LOG = "access-log";
	/**
	 * 
	 */
	public static final String ALIAS = "alias";
	/**
 * 
 */
	public static final String CA_CERTIFICATE_FILE = "ca-certificate-file";
	/**
	 * 
	 */
	public static final String CA_CERTIFICATE_PASSWORD = "ca-certificate-password";
	/**
	 * 
	 */
	public static final String CA_REVOCATION_URL = "ca-revocation-url";
	/**
	 * 
	 */
	public static final String CACHE_CONTAINER = "cache-container";
	/**
	 * 
	 */
	public static final String CACHE_NAME = "cache-name";
	/**
	 * 
	 */
	public static final String CERTIFICATE_FILE = "certificate-file";
	/**
	 * 
	 */
	public static final String CERTIFICATE_KEY_FILE = "certificate-key-file";
	/**
	 * 
	 */
	public static final String CHECK_INTERVAL = "check-interval";
	/**
	 * 
	 */
	public static final String CIPHER_SUITE = "cipher-suite";
	/**
	 * 
	 */
	public static final String CONDITION = "condition";
	/**
	 * 
	 */
	public static final String CONFIGURATION = "configuration";
	/**
	 * 
	 */
	public static final String CONNECTOR = "connector";
	/**
	 * 
	 */
	public static final String CONTAINER = "container";
	/**
	 * 
	 */
	public static final String DEFAULT_VIRTUAL_SERVER = "default-virtual-server";
	/**
	 * 
	 */
	public static final String DEFAULT_WEB_MODULE = "default-web-module";
	/**
	 * 
	 */
	public static final String DEVELOPMENT = "development";
	/**
	 * 
	 */
	public static final String DIRECTORY = "directory";
	/**
	 * 
	 */
	public static final String DISABLED = "disabled";
	/**
	 * 
	 */
	public static final String DISPLAY_SOURCE_FRAGMENT = "display-source-fragment";
	/**
	 * 
	 */
	public static final String DOMAIN = "domain";
	/**
	 * 
	 */
	public static final String DUMP_SMAP = "dump-smap";
	/**
	 * 
	 */
	public static final String ENABLED = "enabled";
	/**
	 * 
	 */
	public static final String ENABLE_LOOKUPS = "enable-lookups";
	/**
	 * 
	 */
	public static final String ENABLE_WELCOME_ROOT = "enable-welcome-root";
	/**
	 * 
	 */
	public static final String ERROR_ON_USE_BEAN_INVALID_CLASS_ATTRIBUTE = "error-on-use-bean-invalid-class-attribute";
	/**
	 * 
	 */
	public static final String EXECUTOR = "executor";
	/**
	 * 
	 */
	public static final String EXTENDED = "extended";
	/**
	 * 
	 */
	public static final String FILE_ENCODING = "file-encoding";
	/**
	 * 
	 */
	public static final String FLAGS = "flags";
	/**
	 * 
	 */
	public static final String GENERATE_STRINGS_AS_CHAR_ARRAYS = "generate-strings-as-char-arrays";
	/**
	 * 
	 */
	public static final String INSTANCE_ID = "instance-id";
	/**
	 * 
	 */
	public static final String JAVA_ENCODING = "java-encoding";
	/**
	 * 
	 */
	public static final String JSP_CONFIGURATION = "jsp-configuration";
	/**
	 * 
	 */
	public static final String KEEP_GENERATED = "keep-generated";
	/**
	 * 
	 */
	public static final String KEY_ALIAS = "key-alias";
	/**
	 * 
	 */
	public static final String KEYSTORE_TYPE = "keystore-type";
	/**
	 * 
	 */
	public static final String LISTINGS = "listings";
	/**
	 * 
	 */
	public static final String MAPPED_FILE = "mapped-file";
	/**
	 * 
	 */
	public static final String MAX_CONNECTIONS = "max-connections";
	/**
	 * 
	 */
	public static final String MAX_DEPTH = "max-depth";
	/**
	 * 
	 */
	public static final String MAX_POST_SIZE = "max-post-size";
	/**
	 * 
	 */
	public static final String MAX_SAVE_POST_SIZE = "max-save-post-size";
	/**
	 * 
	 */
	public static final String MIME_MAPPING = "mime-mapping";
	/**
	 * 
	 */
	public static final String MODIFICATION_TEST_INTERVAL = "modification-test-interval";
	/**
	 * 
	 */
	public static final String NAME = "name";
	/**
	 * 
	 */
	public static final String NATIVE = "native";
	/**
	 * 
	 */
	public static final String PASSWORD = "password";
	/**
	 * 
	 */
	public static final String PATH = "path";
	/**
	 * 
	 */
	public static final String PATTERN = "pattern";
	/**
	 * 
	 */
	public static final String PREFIX = "prefix";
	/**
	 * 
	 */
	public static final String PROTOCOL = "protocol";
	/**
	 * 
	 */
	public static final String PROXY_NAME = "proxy-name";
	/**
	 * 
	 */
	public static final String PROXY_PORT = "proxy-port";
	/**
	 * 
	 */
	public static final String REAUTHENTICATE = "reauthenticate";
	/**
	 * 
	 */
	public static final String READ_ONLY = "read-only";
	/**
	 * 
	 */
	public static final String RECOMPILE_ON_FAIL = "recompile-on-fail";
	/**
	 * 
	 */
	public static final String REDIRECT_PORT = "redirect-port";
	/**
	 * 
	 */
	public static final String RELATIVE_TO = "relative-to";
	/**
	 * 
	 */
	public static final String RESOLVE_HOSTS = "resolve-hosts";
	/**
	 * 
	 */
	public static final String REWRITE = "rewrite";
	/**
	 * 
	 */
	public static final String ROTATE = "rotate";
	/**
	 * 
	 */
	public static final String SCHEME = "scheme";
	/**
	 * 
	 */
	public static final String SCRATCH_DIR = "scratch-dir";
	/**
	 * 
	 */
	public static final String SECRET = "secret";
	/**
	 * 
	 */
	public static final String SECURE = "secure";
	/**
	 * 
	 */
	public static final String SENDFILE = "sendfile";
	/**
	 * 
	 */
	public static final String SESSION_CACHE_SIZE = "session-cache-size";
	/**
	 * 
	 */
	public static final String SESSION_TIMEOUT = "session-timeout";
	/**
	 * 
	 */
	public static final String SMAP = "smap";
	/**
	 * 
	 */
	public static final String SOCKET_BINDING = "socket-binding";
	/**
	 * 
	 */
	public static final String SOURCE_VM = "source-vm";
	/**
	 * 
	 */
	public static final String SSL = "ssl";
	/**
	 * 
	 */
	public static final String SSO = "sso";
	/**
	 * 
	 */
	public static final String STATIC_RESOURCES = "static-resources";
	/**
	 * 
	 */
	public static final String SUBSTITUTION = "substitution";
	/**
	 * 
	 */
	public static final String SUBSYSTEM = "subsystem";
	/**
	 * 
	 */
	public static final String SETTING = "setting";
	/**
	 * 
	 */
	public static final String TAG_POOLING = "tag-pooling";
	/**
	 * 
	 */
	public static final String TARGET_VM = "target-vm";
	/**
	 * 
	 */
	public static final String TEST = "test";
	/**
	 * 
	 */
	public static final String TRIM_SPACES = "trim-spaces";
	/**
	 * 
	 */
	public static final String TRUSTSTORE_TYPE = "truststore-type";
	/**
	 * 
	 */
	public static final String VALUE = "value";
	/**
	 * 
	 */
	public static final String VERIFY_CLIENT = "verify-client";
	/**
	 * 
	 */
	public static final String VERIFY_DEPTH = "verify-depth";
	/**
	 * 
	 */
	public static final String VIRTUAL_SERVER = "virtual-server";
	/**
	 * 
	 */
	public static final String WEBDAV = "webdav";
	/**
	 * 
	 */
	public static final String WELCOME_FILE = "welcome-file";
	/**
	 * 
	 */
	public static final String X_POWERED_BY = "x-powered-by";

	// Connect stats attributes

	/**
	 * 
	 */
	public static final String BYTES_SENT = "bytesSent";
	/**
	 * 
	 */
	public static final String BYTES_RECEIVED = "bytesReceived";
	/**
	 * 
	 */
	public static final String PROCESSING_TIME = "processingTime";
	/**
	 * 
	 */
	public static final String ERROR_COUNT = "errorCount";
	/**
	 * 
	 */
	public static final String MAX_TIME = "maxTime";
	/**
	 * 
	 */
	public static final String REQUEST_COUNT = "requestCount";
	/**
	 * 
	 */
	public static final String LOAD_TIME = "load-time";
	/**
	 * 
	 */
	public static final String MIN_TIME = "min-time";

}
