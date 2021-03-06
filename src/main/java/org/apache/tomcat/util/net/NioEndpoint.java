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

import java.io.IOException;
import java.net.BindException;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;

import org.apache.tomcat.util.net.NioEndpoint.Handler.SocketState;
import org.apache.tomcat.util.net.jsse.NioJSSESocketChannelFactory;
import org.jboss.logging.Logger;

/**
 * {@code NioEndpoint} NIO2 endpoint, providing the following services:
 * <ul>
 * <li>Socket channel acceptor thread</li>
 * <li>Simple Worker thread pool, with possible use of executors</li>
 * </ul>
 * 
 * Created on Dec 13, 2011 at 9:41:53 AM
 * 
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 */
public class NioEndpoint extends AbstractEndpoint<NioChannel> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9093834511892234234L;

	protected static Logger logger = Logger.getLogger(NioEndpoint.class);

	private AsynchronousServerSocketChannel listener;
	private ConcurrentHashMap<Long, NioChannel> connections;
	private ConcurrentLinkedQueue<ChannelProcessor> recycledChannelProcessors;
	private ConcurrentLinkedQueue<HandshakeHandler> recycledHandshakeProcessors;

	/**
	 * Handling of accepted sockets.
	 */
	protected Handler handler = null;

	/**
	 * 
	 */
	protected NioServerSocketChannelFactory serverSocketChannelFactory = null;

	/**
	 * Maximum size of a POST which will be automatically parsed by the
	 * container. 2MB by default.
	 */
	private int maxPostSize = 2 * 1024 * 1024;

	/**
	 * SSL context.
	 */
	protected SSLContext sslContext;

	/**
	 * Create a new instance of {@code NioEndpoint}
	 */
	public NioEndpoint() {
		super();
	}

	/**
	 * @param handler
	 */
	public void setHandler(Handler handler) {
		this.handler = handler;
	}

	/**
	 * @return the handler
	 */
	public Handler getHandler() {
		return handler;
	}

	/**
	 * Return the amount of threads that are managed by the pool.
	 * 
	 * @return the amount of threads that are managed by the pool
	 */
	public int getCurrentThreadCount() {
		return curThreads;
	}

	/**
	 * Return the amount of threads currently busy.
	 * 
	 * @return the amount of threads currently busy
	 */
	public int getCurrentThreadsBusy() {
		return curThreadsBusy;
	}

	/**
	 * Getter for sslContext
	 * 
	 * @return the sslContext
	 */
	public SSLContext getSslContext() {
		return this.sslContext;
	}

	/**
	 * Setter for the sslContext
	 * 
	 * @param sslContext
	 *            the sslContext to set
	 */
	public void setSslContext(SSLContext sslContext) {
		this.sslContext = sslContext;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.tomcat.util.net.AbstractEndpoint#init()
	 */
	@Override
	public void init() throws Exception {
		if (initialized) {
			return;
		}

		if (this.soTimeout < 0) {
			this.soTimeout = DEFAULT_SO_TIMEOUT;
		}

		if (this.keepAliveTimeout < 0) {
			this.keepAliveTimeout = this.soTimeout;
		}

		// Initialize thread count defaults for acceptor
		if (acceptorThreadCount <= 0) {
			acceptorThreadCount = 1;
		}

		// Create the thread factory
		if (this.threadFactory == null) {
			this.threadFactory = new DefaultThreadFactory(getName() + "-",
					threadPriority);
		}

		if (this.connections == null) {
			this.connections = new ConcurrentHashMap<>();
		}

		if (this.recycledChannelProcessors == null) {
			this.recycledChannelProcessors = new ConcurrentLinkedQueue<>();
		}

		if (this.recycledHandshakeProcessors == null) {
			this.recycledHandshakeProcessors = new ConcurrentLinkedQueue<>();
		}

		// If the executor is not set, create it with a fixed thread pool
		if (this.executor == null) {
			this.executor = Executors.newFixedThreadPool(this.maxThreads,
					this.threadFactory);
		}

		// this.forkJoinPool = new ForkJoinPool(Runtime.getRuntime()
		// .availableProcessors(),
		// ForkJoinPool.defaultForkJoinWorkerThreadFactory, null, true);

		ExecutorService executorService = (ExecutorService) this.executor;
		AsynchronousChannelGroup threadGroup = AsynchronousChannelGroup
				.withThreadPool(executorService);

		if (this.serverSocketChannelFactory == null) {
			this.serverSocketChannelFactory = NioServerSocketChannelFactory
					.createServerSocketChannelFactory(threadGroup, SSLEnabled);
		} else {
			this.serverSocketChannelFactory.threadGroup = threadGroup;
		}

		// Initialize the SSL context if the SSL mode is enabled
		if (SSLEnabled) {
			NioJSSESocketChannelFactory factory = (NioJSSESocketChannelFactory) this.serverSocketChannelFactory;
			sslContext = factory.getSslContext();
		}

		// Initialize the channel factory
		this.serverSocketChannelFactory.init();

		if (listener == null) {
			try {
				listener = this.serverSocketChannelFactory.createServerChannel(
						port, backlog, address, reuseAddress);
			} catch (BindException be) {
				logger.fatal(be.getMessage(), be);
				throw new BindException(be.getMessage() + " "
						+ (address == null ? "<null>" : address.toString())
						+ ":" + port);
			}
		}

		initialized = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.tomcat.util.net.AbstractEndpoint#start()
	 */
	@Override
	public void start() throws Exception {
		// Initialize channel if not done before
		if (!initialized) {
			init();
		}
		if (!running) {
			running = true;
			paused = false;

			// Start acceptor threads
			for (int i = 0; i < acceptorThreadCount; i++) {
				Thread acceptorThread = newThread(new Acceptor(), "Acceptor",
						daemon);
				acceptorThread.start();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.tomcat.util.net.AbstractEndpoint#stop()
	 */
	@Override
	public void stop() {
		if (running) {
			running = false;
			unlockAccept();
			try {
				this.listener.setOption(StandardSocketOptions.SO_LINGER, 0);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.tomcat.util.net.AbstractEndpoint#destroy()
	 */
	@Override
	public void destroy() throws Exception {
		
		if (running) {
			stop();
		}
		if (listener != null) {
			try {
				listener.close();
			} catch (IOException e) {
				logger.error(sm.getString("endpoint.err.close"), e);
			} finally {
				listener = null;
			}
		}

		// Shut down the executor
		((ExecutorService) this.executor).shutdown();

		// Closing all alive connections
		for (NioChannel ch : this.connections.values()) {
			close(ch);
		}
		// Remove all connections
		this.connections.clear();
		// Destroy the server socket channel factory
		this.serverSocketChannelFactory.destroy();
		this.serverSocketChannelFactory = null;
		// Destroy all recycled channel processors
		this.recycledChannelProcessors.clear();
		this.recycledChannelProcessors = null;
		// Destroy all recycled handshake processors
		this.recycledHandshakeProcessors.clear();
		this.recycledHandshakeProcessors = null;

		initialized = false;
	}

	/**
	 * Configure the channel options before being processed
	 */
	protected boolean setChannelOptions(NioChannel channel) {
		// Process the connection
		try {
			// Set channel options: timeout, linger, etc
			if (keepAliveTimeout > 0) {
				channel.setOption(StandardSocketOptions.SO_KEEPALIVE,
						Boolean.TRUE);
			}
			if (soLinger >= 0) {
				channel.setOption(StandardSocketOptions.SO_LINGER, soLinger);
			}
			if (tcpNoDelay) {
				channel.setOption(StandardSocketOptions.TCP_NODELAY, tcpNoDelay);
			}

			// Initialize the channel
			serverSocketChannelFactory.initChannel(channel);
			return true;
		} catch (Throwable t) {
			// logger.error(t.getMessage(), t);
			if (logger.isDebugEnabled()) {
				if (t instanceof SSLHandshakeException) {
					logger.debug(sm.getString("endpoint.err.handshake"), t);
				} else {
					logger.debug(sm.getString("endpoint.err.unexpected"), t);
				}
			}

			return false;
		}
	}

	/**
	 * Process given channel.
	 */
	protected boolean processChannelWithOptions(NioChannel channel) {
		try {
			executor.execute(new ChannelWithOptionsProcessor(channel));
		} catch (Throwable t) {
			// This means we got an OOM or similar creating a thread, or that
			// the pool and its queue are full
			logger.error(sm.getString("endpoint.process.fail"), t);
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.tomcat.util.net.Endpoint#process(java.io.Closeable)
	 */
	public boolean process(NioChannel channel) {
		return processChannel(channel, null);
	}

	/**
	 * Process given channel for an event.
	 * 
	 * @param channel
	 * @param status
	 * @return <tt>true</tt> if the processing of the channel finish
	 *         successfully else <tt>false</tt>
	 */
	public boolean processChannel(NioChannel channel, SocketStatus status) {
		if (channel.isClosed()) {
			return false;
		}
		try {
			ChannelProcessor processor = getChannelProcessor(channel, status);
			this.executor.execute(processor);
			return true;
		} catch (Throwable t) {
			// This means we got an OOM or similar creating a thread, or that
			// the pool and its queue are full
			logger.error(sm.getString("endpoint.process.fail"), t);
			return false;
		}
	}

	/**
	 * @param channel
	 * @return
	 */
	private boolean handshake(NioChannel channel) {
		try {
			HandshakeHandler processor = getHandshakeProcessor(channel);
			this.executor.execute(processor);
			return true;
		} catch (Throwable t) {
			// This means we got an OOM or similar creating a thread, or that
			// the pool and its queue are full
			logger.error(sm.getString("endpoint.process.fail"), t);
			return false;
		}
	}

	/**
	 * @return peek a processor from the recycled processors list
	 */
	private ChannelProcessor getChannelProcessor(NioChannel channel,
			SocketStatus status) {
		ChannelProcessor processor = this.recycledChannelProcessors.poll();
		if (processor == null) {
			processor = new ChannelProcessor(channel, status);
		} else {
			processor.setChannel(channel);
			processor.setStatus(status);
		}
		return processor;
	}

	/**
	 * @return peek a handshake processor from the recycled processors list
	 */
	private HandshakeHandler getHandshakeProcessor(NioChannel channel) {
		HandshakeHandler processor = this.recycledHandshakeProcessors.poll();
		if (processor == null) {
			processor = new HandshakeHandler(channel);
		} else {
			processor.setChannel(channel);
		}
		return processor;
	}

	/**
	 * Try to add the specified channel to the list of connections.
	 * 
	 * @param channel
	 *            the channel to be added
	 * @return <tt>true</tt> if the channel is added successfully, else
	 *         <tt>false</tt>
	 */
	private boolean addChannel(NioChannel channel) {
		if (this.counter.get() < this.maxConnections && channel.isOpen()) {
			if (this.connections.get(channel.getId()) == null
					|| this.connections.get(channel.getId()).isClosed()) {
				this.connections.put(channel.getId(), channel);
				this.counter.incrementAndGet();
				return true;
			}
		}

		return false;
	}

	/**
	 * Getter for serverSocketChannelFactory
	 * 
	 * @return the serverSocketChannelFactory
	 */
	public NioServerSocketChannelFactory getServerSocketChannelFactory() {
		return this.serverSocketChannelFactory;
	}

	/**
	 * Setter for the serverSocketChannelFactory
	 * 
	 * @param serverSocketChannelFactory
	 *            the serverSocketChannelFactory to set
	 */
	public void setServerSocketChannelFactory(
			NioServerSocketChannelFactory serverSocketChannelFactory) {
		this.serverSocketChannelFactory = serverSocketChannelFactory;
	}

	/**
	 * Close the specified channel and remove it from the list of open
	 * connections
	 * 
	 * @param channel
	 *            the channel to be closed
	 */
	public void close(NioChannel channel) {
		if (channel != null) {
			try {
				channel.close();
			} catch (IOException e) {
				if (logger.isDebugEnabled()) {
					logger.debug(e.getMessage(), e);
				}
			} finally {
				if (this.connections.remove(channel.getId()) != null) {
					this.counter.decrementAndGet();
				}
			}
		}
	}

	/**
	 * {@code Acceptor}
	 * 
	 * <p>
	 * Server socket acceptor thread.
	 * </p>
	 * Created on Mar 6, 2012 at 9:13:34 AM
	 * 
	 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
	 */
	protected class Acceptor implements Runnable {

		/**
		 * The background thread that listens for incoming TCP/IP connections
		 * and hands them off to an appropriate processor.
		 */
		public void run() {

			// Loop until we receive a shutdown command
			while (running) {
				// Loop if end point is paused
				while (paused) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// Ignore
					}
				}

				try {
					// Accept the next incoming connection from the server
					// channel
					final NioChannel channel = serverSocketChannelFactory
							.accept(listener);

					boolean ok = false;
					if (addChannel(channel) && setChannelOptions(channel)
							&& channel.isOpen()) {
						if (channel.isSecure()) {
							handshake(channel);
							ok = true;
						} else {
							ok = processChannel(channel, null);
						}
					}
					// If a problem occurs, close the channel right away
					if (!ok) {
						logger.info("Fail processing the channel");
						close(channel);
					}
				} catch (Exception exp) {
					if (running) {
						logger.error(sm.getString("endpoint.accept.fail"), exp);
					}
				} catch (Throwable t) {
					logger.error(sm.getString("endpoint.accept.fail"), t);
				}
			}
		}
	}

	/**
	 * {@code HandshakeHandler}
	 * <p>
	 * Asynchronous handler for the secure channel handshake. Since the
	 * handshake for the secure channels may take awhile, if several new
	 * connections are received at the same time, the non-blocking handshake
	 * aims to avoid connections to be timed out. Note that this does not
	 * guarantee that no connection will be timed out, this depends to the
	 * socket SO_TIMEOUT in the client side.
	 * </p>
	 * 
	 * Created on May 23, 2012 at 11:48:45 AM
	 * 
	 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
	 */
	protected class HandshakeHandler implements Runnable {

		private NioChannel channel;

		/**
		 * Create a new instance of {@code HandshakeProcessor}
		 * 
		 * @param channel
		 */
		public HandshakeHandler(NioChannel channel) {
			this.channel = channel;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			try {
				serverSocketChannelFactory.handshake(channel);

				if (!processChannel(channel, null)) {
					logger.info("Fail processing the channel");
					close(channel);
				}
			} catch (Exception exp) {
				if (logger.isDebugEnabled()) {
					logger.debug(exp.getMessage(), exp);
				}

				close(channel);
			} finally {
				this.recycle();
			}
		}

		/**
		 * Recycle the handshake processor
		 */
		private void recycle() {
			this.channel = null;
			if (recycledHandshakeProcessors != null) {
				recycledHandshakeProcessors.offer(this);
			}
		}

		/**
		 * Setter for the channel
		 * 
		 * @param channel
		 */
		public void setChannel(NioChannel channel) {
			this.channel = channel;
		}
	}

	/**
	 * {@code ChannelInfo}
	 * <p>
	 * Channel list class, used to avoid using a possibly large amount of
	 * objects with very little actual use.
	 * </p>
	 * Created on Apr 13, 2012 at 11:13:13 AM
	 * 
	 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
	 */
	public static class ChannelInfo {
		/**
		 * 
		 */
		public static final int READ = 1;
		/**
		 * 
		 */
		public static final int WRITE = 2;
		/**
		 * 
		 */
		public static final int RESUME = 4;
		/**
		 * 
		 */
		public static final int WAKEUP = 8;

		protected NioChannel channel;
		protected long timeout;
		protected int flags;

		/**
		 * Create a new instance of {@code ChannelInfo}
		 */
		public ChannelInfo() {
			this(null, 0, 0);
		}

		/**
		 * Create a new instance of {@code ChannelInfo}
		 * 
		 * @param channel
		 *            the channel
		 * @param timeout
		 *            the channel timeout. The default time unit is
		 *            {@code java.util.concurrent.TimeUnit.MILLISECONDS}
		 * @param flags
		 */
		public ChannelInfo(NioChannel channel, long timeout, int flags) {
			this.channel = channel;
			this.timeout = timeout;
			this.flags = flags;
		}

		/**
		 * Create a new instance of {@code ChannelInfo}
		 * 
		 * @param channel
		 * @param timeout
		 * @param unit
		 * @param flags
		 */
		public ChannelInfo(NioChannel channel, long timeout, TimeUnit unit,
				int flags) {
			this(channel, TimeUnit.MILLISECONDS.convert(timeout, unit), flags);
		}

		/**
		 * Recycle this channel info for next use
		 */
		public void recycle() {
			this.channel = null;
			this.timeout = 0;
			this.flags = 0;
		}

		/**
		 * @return the read flag
		 */
		public boolean read() {
			return (flags & READ) == READ;
		}

		/**
		 * Set the <code>read</code> flag. If the parameter is true, the read
		 * flag will have the value 1 else 0.
		 * 
		 * @param read
		 */
		public void read(boolean read) {
			this.flags = (read ? (this.flags | READ) : (this.flags & 0xE));
		}

		/**
		 * @return the write flag
		 */
		public boolean write() {
			return (flags & WRITE) == WRITE;
		}

		/**
		 * Set the <code>write</code> flag. If the parameter is true, the write
		 * flag will have the value 1 else 0.
		 * 
		 * @param write
		 */
		public void write(boolean write) {
			this.flags = (write ? (this.flags | WRITE) : (this.flags & 0xD));
		}

		/**
		 * @return the resume flag
		 */
		public boolean resume() {
			return (flags & RESUME) == RESUME;
		}

		/**
		 * Set the <code>resume</code> flag. If the parameter is true, the
		 * resume flag will have the value 1 else 0.
		 * 
		 * @param resume
		 */
		public void resume(boolean resume) {
			this.flags = (resume ? (this.flags | RESUME) : (this.flags & 0xB));
		}

		/**
		 * @return the wake up flag
		 */
		public boolean wakeup() {
			return (flags & WAKEUP) == WAKEUP;
		}

		/**
		 * Set the <code>wakeup</code> flag. If the parameter is true, the
		 * wakeup flag will have the value 1 else 0.
		 * 
		 * @param wakeup
		 */
		public void wakeup(boolean wakeup) {
			this.flags = (wakeup ? (this.flags | WAKEUP) : (this.flags & 0x7));
		}

		/**
		 * Merge the tow flags
		 * 
		 * @param flag1
		 * @param flag2
		 * @return the result of merging the tow flags
		 */
		public static int merge(int flag1, int flag2) {
			return ((flag1 & READ) | (flag2 & READ))
					| ((flag1 & WRITE) | (flag2 & WRITE))
					| ((flag1 & RESUME) | (flag2 & RESUME))
					| ((flag1 & WAKEUP) & (flag2 & WAKEUP));
		}
	}

	/**
	 * {@code Handler}
	 * 
	 * <p>
	 * Bare bones interface used for socket processing. Per thread data is to be
	 * stored in the ThreadWithAttributes extra folders, or alternately in
	 * thread local fields.
	 * </p>
	 * 
	 * Created on Mar 6, 2012 at 9:13:07 AM
	 * 
	 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
	 */
	public interface Handler {
		/**
		 * {@code ChannelState}
		 * 
		 * Created on Dec 12, 2011 at 9:41:06 AM
		 * 
		 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
		 */
		public enum SocketState {
			/**
			 * 
			 */
			OPEN,
			/**
			 * 
			 */
			CLOSED,
			/**
			 * 
			 */
			LONG
		}

		/**
		 * Process the specified {@code org.apache.tomcat.util.net.NioChannel}
		 * 
		 * @param channel
		 *            the {@code org.apache.tomcat.util.net.NioChannel}
		 * @return a channel state
		 */
		public SocketState process(NioChannel channel);

		/**
		 * Process the specified {@code org.apache.tomcat.util.net.NioChannel}
		 * 
		 * @param channel
		 * @param status
		 * @return a channel state
		 */
		public SocketState event(NioChannel channel, SocketStatus status);

	}

	/**
	 * {@code ChannelWithOptionsProcessor}
	 * <p>
	 * This class is the equivalent of the Worker, but will simply use in an
	 * external Executor thread pool. This will also set the channel options and
	 * do the handshake.
	 * </p>
	 * Created on Mar 6, 2012 at 9:09:43 AM
	 * 
	 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
	 */
	protected class ChannelWithOptionsProcessor extends ChannelProcessor {

		/**
		 * Create a new instance of {@code ChannelWithOptionsProcessor}
		 * 
		 * @param channel
		 */
		public ChannelWithOptionsProcessor(NioChannel channel) {
			super(channel);
		}

		@Override
		public void run() {
			boolean ok = true;

			if (!deferAccept) {
				ok = setChannelOptions(channel);
			} else {
				// Process the request from this channel
				ok = setChannelOptions(channel)
						&& handler.process(channel) != Handler.SocketState.CLOSED;
			}

			if (!ok) {
				// Close the channel
				close(channel);
			}

			channel = null;
		}
	}

	/**
	 * {@code ChannelProcessor}
	 * <p>
	 * This class is the equivalent of the Worker, but will simply use in an
	 * external Executor thread pool.
	 * </p>
	 * Created on Mar 6, 2012 at 9:10:06 AM
	 * 
	 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
	 */
	protected class ChannelProcessor implements Runnable {

		protected NioChannel channel;
		protected SocketStatus status = null;

		/**
		 * Create a new instance of {@code ChannelProcessor}
		 * 
		 * @param channel
		 */
		public ChannelProcessor(NioChannel channel) {
			this.channel = channel;
		}

		/**
		 * Create a new instance of {@code ChannelProcessor}
		 * 
		 * @param channel
		 * @param status
		 */
		public ChannelProcessor(NioChannel channel, SocketStatus status) {
			this(channel);
			this.status = status;
		}

		@Override
		public void run() {
			try {
				Handler.SocketState state = ((status == null) ? handler
						.process(channel) : handler.event(channel, status));

				if (state == SocketState.CLOSED) {
					close(channel);
				}
			} catch (Throwable th) {
				logger.error(th.getMessage(), th);
				if (logger.isDebugEnabled()) {
					logger.debug(th.getMessage(), th);
				}
			} finally {
				this.recycle();
			}
		}

		/**
		 * Reset this channel processor
		 */
		protected void recycle() {
			this.channel = null;
			this.status = null;
			if (recycledChannelProcessors != null) {
				recycledChannelProcessors.offer(this);
			}
		}

		/**
		 * 
		 * @param channel
		 * @param status
		 */
		protected void setup(NioChannel channel, SocketStatus status) {
			this.channel = channel;
			this.status = status;
		}

		/**
		 * @param status
		 */
		public void setStatus(SocketStatus status) {
			this.status = status;
		}

		/**
		 * @param channel
		 */
		public void setChannel(NioChannel channel) {
			this.channel = channel;
		}
	}

	/**
	 * {@code DefaultThreadFactory}
	 * 
	 * The default thread factory
	 * 
	 * Created on Mar 6, 2012 at 9:11:20 AM
	 * 
	 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
	 */
	protected static class DefaultThreadFactory implements ThreadFactory {
		private static final AtomicInteger poolNumber = new AtomicInteger(1);
		private final ThreadGroup group;
		private final AtomicInteger threadNumber = new AtomicInteger(1);
		private final String namePrefix;
		private final int threadPriority;

		/**
		 * Create a new instance of {@code DefaultThreadFactory}
		 * 
		 * @param namePrefix
		 * @param threadPriority
		 */
		public DefaultThreadFactory(String namePrefix, int threadPriority) {
			SecurityManager s = System.getSecurityManager();
			group = (s != null) ? s.getThreadGroup() : Thread.currentThread()
					.getThreadGroup();
			this.namePrefix = namePrefix;
			this.threadPriority = threadPriority;
		}

		/**
		 * 
		 * Create a new instance of {@code DefaultThreadFactory}
		 * 
		 * @param threadPriority
		 */
		public DefaultThreadFactory(int threadPriority) {
			this("pool-" + poolNumber.getAndIncrement() + "-thread-",
					threadPriority);
		}

		/**
		 * Create and return a new thread
		 */
		public Thread newThread(Runnable r) {
			Thread thread = new Thread(group, r, namePrefix
					+ threadNumber.getAndIncrement(), 0);
			if (thread.isDaemon())
				thread.setDaemon(false);

			if (thread.getPriority() != this.threadPriority)
				thread.setPriority(this.threadPriority);
			return thread;
		}
	}

	/**
	 * @return the maxPostSize
	 */
	public int getMaxPostSize() {
		return maxPostSize;
	}

	/**
	 * @param maxPostSize
	 *            the maxPostSize to set
	 */
	public void setMaxPostSize(int maxPostSize) {
		this.maxPostSize = maxPostSize;
	}

}
