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
package org.jboss.cluster.proxy.net;

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
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;

import org.jboss.cluster.proxy.container.Node;
import org.jboss.cluster.proxy.net.jsse.NioJSSESocketChannelFactory;
import org.jboss.logging.Logger;

/**
 * {@code NioEndpoint}
 * 
 * Created on Jun 11, 2012 at 9:45:14 AM
 * 
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 */
public class NioEndpoint extends AbstractEndpoint<NioChannel> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected static Logger logger = Logger.getLogger(NioEndpoint.class);

	private AsynchronousServerSocketChannel listener;
	private ConcurrentHashMap<Long, NioChannel> connections;
	private ConcurrentLinkedQueue<ChannelProcessor> recycledChannelProcessors;
	private ConcurrentLinkedQueue<HandshakeHandler> recycledHandshakeHandlers;
	private ConcurrentHashMap<Long, Node> nodes;

	/**
	 * 
	 */
	protected NioServerSocketChannelFactory serverSocketChannelFactory = null;

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jboss.cluster.proxy.net.AbstractEndpoint#init()
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
			this.threadFactory = new DefaultThreadFactory(getName() + "-", threadPriority);
		}

		if (this.connections == null) {
			this.connections = new ConcurrentHashMap<>();
		}

		// If the executor is not set, create it with a fixed thread pool
		if (this.executor == null) {
			this.executor = Executors.newFixedThreadPool(this.maxThreads, this.threadFactory);
		}

		if (this.recycledChannelProcessors == null) {
			this.recycledChannelProcessors = new ConcurrentLinkedQueue<>();
		}
		if (this.recycledHandshakeHandlers == null) {
			this.recycledHandshakeHandlers = new ConcurrentLinkedQueue<>();
		}

		if (this.nodes == null) {
			this.nodes = new ConcurrentHashMap<>();
		}

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
				listener = this.serverSocketChannelFactory.createServerChannel(port, backlog,
						address, reuseAddress);
			} catch (BindException be) {
				logger.fatal(be.getMessage(), be);
				throw new BindException(be.getMessage() + " "
						+ (address == null ? "<null>" : address.toString()) + ":" + port);
			}
		}

		initialized = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jboss.cluster.proxy.net.AbstractEndpoint#start()
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
				Thread acceptorThread = newThread(new Acceptor(), "Acceptor", daemon);
				acceptorThread.start();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jboss.cluster.proxy.net.AbstractEndpoint#stop()
	 */
	@Override
	public void stop() {
		if (running) {
			running = false;
			unlockAccept();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jboss.cluster.proxy.net.AbstractEndpoint#destroy()
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
				logger.error(e.getMessage(), e);
			} finally {
				listener = null;
			}
		}

		// Closing all alive connections
		for (NioChannel ch : this.connections.values()) {
			try {
				ch.close();
			} catch (Throwable t) {
				// Nothing to do
			}
		}
		// Remove all connections
		this.connections.clear();
		// Destroy the server socket channel factory
		this.serverSocketChannelFactory.destroy();
		this.serverSocketChannelFactory = null;
		// Destroy all recycled handshake processors
		this.recycledHandshakeHandlers.clear();
		this.recycledHandshakeHandlers = null;
		// Shut down the executor
		((ExecutorService) this.executor).shutdown();

		initialized = false;
	}

	/**
	 * Process given channel for an event.
	 * 
	 * @param channel
	 * @return <tt>true</tt> if the processing of the channel finish
	 *         successfully else <tt>false</tt>
	 */
	public boolean processChannel(NioChannel channel) {
		if (channel.isClosed()) {
			return false;
		}
		try {
			ChannelProcessor processor = getChannelProcessor(channel);
			this.executor.execute(processor);
			return true;
		} catch (Throwable t) {
			// This means we got an OOM or similar creating a thread, or that
			// the pool and its queue are full
			logger.error("Channel process fail", t);
			return false;
		}
	}

	/**
	 * @param channel
	 * @return
	 */
	private boolean handshake(NioChannel channel) {
		try {
			HandshakeHandler handler = getHandshakeHandler(channel);
			this.executor.execute(handler);
			return true;
		} catch (Throwable t) {
			// This means we got an OOM or similar creating a thread, or that
			// the pool and its queue are full
			logger.error("Process Handshake fail", t);
			return false;
		}
	}

	/**
	 * Peek a processor from the recycled processors list. If the list is empty,
	 * create a new one and return it.
	 * 
	 * @param channel
	 *            the channel to be processed by the processor
	 * @return a {@link ChannelProcessor}
	 */
	private ChannelProcessor getChannelProcessor(NioChannel channel) {
		ChannelProcessor processor = this.recycledChannelProcessors.poll();
		if (processor == null) {
			processor = new ChannelProcessor(channel);
		} else {
			processor.channel = channel;
		}
		return processor;
	}

	/**
	 * @return peek a handshake handler from the recycled handlers list
	 */
	private HandshakeHandler getHandshakeHandler(NioChannel channel) {
		HandshakeHandler processor = this.recycledHandshakeHandlers.poll();
		if (processor == null) {
			processor = new HandshakeHandler(channel);
		} else {
			processor.channel = channel;
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
	 * Configure the channel options before being processed
	 */
	protected boolean setChannelOptions(NioChannel channel) {
		// Process the connection
		try {
			// Set channel options: timeout, linger, etc
			if (keepAliveTimeout > 0) {
				channel.setOption(StandardSocketOptions.SO_KEEPALIVE, Boolean.TRUE);
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
					logger.debug("Handshake ERROR", t);
				} else {
					logger.debug("Unexpected ERROR", t);
				}
			}

			return false;
		}
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
					final NioChannel channel = serverSocketChannelFactory.acceptChannel(listener);
					boolean ok = false;
					if (addChannel(channel) && setChannelOptions(channel) && channel.isOpen()) {
						if (channel.isSecure()) {
							ok = handshake(channel);
						} else {
							// ok = processChannel(channel, null);
							// TODO
						}
					}
					// If a problem occurs, close the channel right away
					if (!ok) {
						logger.info("Fail processing the channel");
						close(channel);
					}
				} catch (Exception exp) {
					if (running) {
						logger.error("Channel accept fails", exp);
					}
				} catch (Throwable t) {
					logger.error("Channel accept fails", t);
				}
			}
		}
	}

	/**
	 * {@code ChannelProcessor}
	 * <p>
	 * This class is the equivalent of the Worker, but will simply use in an
	 * external Executor thread pool.
	 * </p>
	 * 
	 * Created on Jun 12, 2012 at 2:59:34 PM
	 * 
	 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
	 */
	private class ChannelProcessor implements Runnable {

		private NioChannel channel;

		/**
		 * Create a new instance of {@code ChannelProcessor}
		 * 
		 * @param channel
		 */
		public ChannelProcessor(NioChannel channel) {
			this.channel = channel;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			// TODO
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

				if (!processChannel(channel)) {
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
			if (recycledHandshakeHandlers != null) {
				recycledHandshakeHandlers.offer(this);
			}
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
			group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
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
			this("pool-" + poolNumber.getAndIncrement() + "-thread-", threadPriority);
		}

		/**
		 * Create and return a new thread
		 */
		public Thread newThread(Runnable r) {
			Thread thread = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
			if (thread.isDaemon())
				thread.setDaemon(false);

			if (thread.getPriority() != this.threadPriority)
				thread.setPriority(this.threadPriority);
			return thread;
		}
	}
}
