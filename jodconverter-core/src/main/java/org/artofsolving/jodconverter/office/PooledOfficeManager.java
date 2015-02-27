//
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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PooledOfficeManager implements OfficeManager {

    private final PooledOfficeManagerSettings settings;
    private final ManagedOfficeProcess managedOfficeProcess;
    private final SuspendableThreadPoolExecutor taskExecutor;

    private volatile boolean stopping = false;
    private int taskCount;
    private Future<?> currentTask;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    private final OfficeConnectionEventListener connectionEventListener = new OfficeConnectionEventListener() {
        public void connected(final OfficeConnectionEvent event) {
            PooledOfficeManager.this.taskCount = 0;
            PooledOfficeManager.this.taskExecutor.setAvailable(true);
        }
        public void disconnected(final OfficeConnectionEvent event) {
            PooledOfficeManager.this.taskExecutor.setAvailable(false);
            if (PooledOfficeManager.this.stopping) {
                // expected
                PooledOfficeManager.this.stopping = false;
            } else {
                PooledOfficeManager.this.logger.warn("connection lost unexpectedly; attempting restart");
                if (PooledOfficeManager.this.currentTask != null) {
                    PooledOfficeManager.this.currentTask.cancel(true);
                }
                PooledOfficeManager.this.managedOfficeProcess.restartDueToLostConnection();
            }
        }
    };

    public PooledOfficeManager(final UnoUrl unoUrl) {
        this(new PooledOfficeManagerSettings(unoUrl));
    }

    public PooledOfficeManager(final PooledOfficeManagerSettings settings) {
        this.settings = settings;
        this.managedOfficeProcess = new ManagedOfficeProcess(settings);
        this.managedOfficeProcess.getConnection().addConnectionEventListener(this.connectionEventListener);
        this.taskExecutor = new SuspendableThreadPoolExecutor(new NamedThreadFactory("OfficeTaskThread"));
    }

    public void execute(final OfficeTask task) throws OfficeException {
        final Future<?> futureTask = this.taskExecutor.submit(new Runnable() {
            public void run() {
                if (PooledOfficeManager.this.settings.getMaxTasksPerProcess() > 0 && ++PooledOfficeManager.this.taskCount == PooledOfficeManager.this.settings.getMaxTasksPerProcess() + 1) {
                    PooledOfficeManager.this.logger.info(String.format("reached limit of %d maxTasksPerProcess: restarting", PooledOfficeManager.this.settings.getMaxTasksPerProcess()));
                    PooledOfficeManager.this.taskExecutor.setAvailable(false);
                    PooledOfficeManager.this.stopping = true;
                    PooledOfficeManager.this.managedOfficeProcess.restartAndWait();
                    //FIXME taskCount will be 0 rather than 1 at this point
                }
                task.execute(PooledOfficeManager.this.managedOfficeProcess.getConnection());
             }
         });
         this.currentTask = futureTask;
         try {
             this.logger.info("Waiting for task completion for {} ms", this.settings.getTaskExecutionTimeout());
             futureTask.get(this.settings.getTaskExecutionTimeout(), TimeUnit.MILLISECONDS);
         } catch (final TimeoutException timeoutException) {
             this.managedOfficeProcess.restartDueToTaskTimeout();
             throw new OfficeException("task did not complete within timeout", timeoutException);
         } catch (final ExecutionException executionException) {
             if (executionException.getCause() instanceof OfficeException) {
                 throw (OfficeException) executionException.getCause();
             } else {
                 throw new OfficeException("task failed", executionException.getCause());
             }
         } catch (final Exception exception) {
             throw new OfficeException("task failed", exception);
         }
    }

    public void start() throws OfficeException {
        this.managedOfficeProcess.startAndWait();
    }

    public void stop() throws OfficeException {
        this.taskExecutor.setAvailable(false);
        this.stopping = true;
        this.taskExecutor.shutdownNow();
        this.managedOfficeProcess.stopAndWait();
    }

	public boolean isRunning() {
		return this.managedOfficeProcess.isConnected();
	}

}
