//
// JODConverter - Java OpenDocument Converter
// Copyright 2004-2012 Mirko Nasato and contributors
//
// JODConverter is Open Source software, you can redistribute it and/or
// modify it under either (at your option) of the following licenses
//
// 1. The GNU Lesser General Public License v3 (or later)
//    -> http://www.gnu.org/licenses/lgpl-3.0.txt
// 2. The Apache License, Version 2.0
//    -> http://www.apache.org/licenses/LICENSE-2.0.txt
//
package org.artofsolving.jodconverter.office;

import java.net.ConnectException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.sun.star.frame.XDesktop;
import com.sun.star.lang.DisposedException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ManagedOfficeProcess {

	private static final Integer EXIT_CODE_NEW_INSTALLATION = Integer.valueOf(81);

	private final ManagedOfficeProcessSettings settings;

	private final OfficeProcess process;
	private final OfficeConnection connection;

	private final ExecutorService executor = Executors.newSingleThreadExecutor(new NamedThreadFactory("OfficeProcessThread"));

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public ManagedOfficeProcess(final ManagedOfficeProcessSettings settings) throws OfficeException {
		this.settings = settings;
		this.process = new OfficeProcess(settings.getOfficeHome(), settings.getUnoUrl(), settings.getRunAsArgs(), settings.getTemplateProfileDir(), settings.getWorkDir(), settings
				.getProcessManager());
		this.connection = new OfficeConnection(settings.getUnoUrl());
	}

	public OfficeConnection getConnection() {
		return this.connection;
	}

	public void startAndWait() throws OfficeException {
		final Future<?> future = this.executor.submit(new Runnable() {
			public void run() {
				ManagedOfficeProcess.this.doStartProcessAndConnect();
			}
		});
		try {
			future.get();
		} catch (final Exception exception) {
			throw new OfficeException("failed to start and connect", exception);
		}
	}

	public void stopAndWait() throws OfficeException {
		final Future<?> future = this.executor.submit(new Runnable() {
			public void run() {
				ManagedOfficeProcess.this.doStopProcess();
			}
		});
		try {
			future.get();
		} catch (final Exception exception) {
			throw new OfficeException("failed to start and connect", exception);
		}
	}

	public void restartAndWait() {
		final Future<?> future = this.executor.submit(new Runnable() {
			public void run() {
				ManagedOfficeProcess.this.doStopProcess();
				ManagedOfficeProcess.this.doStartProcessAndConnect();
			}
		});
		try {
			future.get();
		} catch (final Exception exception) {
			throw new OfficeException("failed to restart", exception);
		}
	}

	public void restartDueToTaskTimeout() {
		this.executor.execute(new Runnable() {
			public void run() {
				ManagedOfficeProcess.this.doTerminateProcess();
				// will cause unexpected disconnection and subsequent restart
			}
		});
	}

	public void restartDueToLostConnection() {
		this.executor.execute(new Runnable() {
			public void run() {
				try {
					ManagedOfficeProcess.this.doEnsureProcessExited();
					ManagedOfficeProcess.this.doStartProcessAndConnect();
				} catch (final OfficeException officeException) {
					ManagedOfficeProcess.this.logger.error("could not restart process", officeException);
				}
			}
		});
	}

	private void doStartProcessAndConnect() throws OfficeException {
		try {
			this.process.start();
			new Retryable() {
				@Override
        protected void attempt() throws TemporaryException, Exception {
					try {
						ManagedOfficeProcess.this.connection.connect();
					} catch (final ConnectException connectException) {
						final Integer exitCode = ManagedOfficeProcess.this.process.getExitCode();
						if (exitCode == null) {
							// process is running; retry later
							throw new TemporaryException(connectException);
						} else if (exitCode.equals(ManagedOfficeProcess.EXIT_CODE_NEW_INSTALLATION)) {
							// restart and retry later
							// see http://code.google.com/p/jodconverter/issues/detail?id=84
							ManagedOfficeProcess.this.logger.warn("office process died with exit code 81; restarting it");
							ManagedOfficeProcess.this.process.start(true);
							throw new TemporaryException(connectException);
						} else {
							throw new OfficeException("office process died with exit code " + exitCode);
						}
					}
				}
			}.execute(this.settings.getRetryInterval(), this.settings.getRetryTimeout());
		} catch (final Exception exception) {
			throw new OfficeException("could not establish connection", exception);
		}
	}

	private void doStopProcess() {
		try {
			final XDesktop desktop = OfficeUtils.cast(XDesktop.class, this.connection.getService(OfficeUtils.SERVICE_DESKTOP));
			desktop.terminate();
		} catch (final DisposedException disposedException) {
			// expected
		} catch (final Exception exception) {
			// in case we can't get hold of the desktop
			this.doTerminateProcess();
		}
		this.doEnsureProcessExited();
	}

	private void doEnsureProcessExited() throws OfficeException {
		try {
			final int exitCode = this.process.getExitCode(this.settings.getRetryInterval(), this.settings.getRetryTimeout());
			this.logger.info("process exited with code " + exitCode);
		} catch (final RetryTimeoutException retryTimeoutException) {
			this.doTerminateProcess();
		}
		this.process.deleteProfileDir();
	}

	private void doTerminateProcess() throws OfficeException {
		try {
			final int exitCode = this.process.forciblyTerminate(this.settings.getRetryInterval(), this.settings.getRetryTimeout());
			this.logger.info("process forcibly terminated with code " + exitCode);
		} catch (final Exception exception) {
			throw new OfficeException("could not terminate process", exception);
		}
	}

	boolean isConnected() {
		return this.connection.isConnected();
	}

}
