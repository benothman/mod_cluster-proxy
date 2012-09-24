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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

import org.jboss.logging.Logger;

/**
 * {@code CLI}
 * 
 * Created on Sep 24, 2012 at 11:55:11 AM
 * 
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 */
public class CLI implements Runnable {

	private static final Logger logger = Logger.getLogger(CLI.class);

	protected static final String STOP_CMD = "stop";
	protected static final String QUIT_CMD = "quit";
	protected static final String EXIT_CMD = "exit";
	protected static final String PAUSE_CMD = "pause";
	protected static final String START_CMD = "start";
	protected static final String RESUME_CMD = "resume";
	protected static final String HELP_CMD = "help";
	private boolean running = true;
	private boolean paused = false;
	private PrintStream out;

	/**
	 * Create a new instance of {@code CLI}
	 * 
	 * @param out
	 */
	public CLI(PrintStream out) {
		if (out == null) {
			throw new NullPointerException("Out Stream is null");
		}
		this.out = out;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {

			final String HELP_CONTENT = new StringBuilder(
					"\nThe supported commands are the following: \n\n")
					.append("\t[stop|quit|exit] -- To stop the program\n")
					.append("\t[start|resume] \t -- To resume the program after a pause\n")
					.append("\t[pause] \t -- To pause the program\n").toString();

			String line = null;

			while (running && (line = br.readLine()) != null) {
				line = line.trim().toLowerCase();
				if (line.isEmpty()) {
					continue;
				}

				switch (line) {
					case STOP_CMD:
					case QUIT_CMD:
					case EXIT_CMD:
						out.println("Processing command '" + line + "'");
						running = false;
						break;
					case PAUSE_CMD:
						logger.info("Processing command '" + line + "'");
						if (paused) {
							logger.error("The system is already paused");
						} else {

							try {
								ProxyMain.pause();
								paused = true;
							} catch (Exception e) {
								logger.error(e.getMessage(), e);
								e.printStackTrace();
							}
						}
						break;
					case RESUME_CMD:
					case START_CMD:
						logger.info("Processing command '" + line + "'");
						if (paused) {
							try {
								ProxyMain.start();
								paused = false;
							} catch (StartException e) {
								logger.error(e.getMessage(), e);
								e.printStackTrace();
							}
						} else {
							logger.error("The system is already running");
						}

						break;
					case HELP_CMD:
						logger.info("Processing command '" + line + "'");
						out.println(HELP_CONTENT);

						break;
					default:
						logger.error("Unknow command : " + line);
						break;
				}
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		System.exit(0);
	}

}
