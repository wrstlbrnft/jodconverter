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

import java.io.File;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.artofsolving.jodconverter.process.ProcessManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ProcessPoolOfficeManager implements OfficeManager {

    private final BlockingQueue<PooledOfficeManager> pool;
    private final PooledOfficeManager[] pooledManagers;
    private final long taskQueueTimeout;

    private volatile boolean running = false;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public ProcessPoolOfficeManager(final File officeHome, final UnoUrl[] unoUrls, final String[] runAsArgs, final File templateProfileDir, final File workDir,
            final long retryTimeout, final long taskQueueTimeout, final long taskExecutionTimeout, final int maxTasksPerProcess,
            final ProcessManager processManager) {
		this.taskQueueTimeout = taskQueueTimeout;
        this.pool = new ArrayBlockingQueue<PooledOfficeManager>(unoUrls.length);
        this.pooledManagers = new PooledOfficeManager[unoUrls.length];
        for (int i = 0; i < unoUrls.length; i++) {
            final PooledOfficeManagerSettings settings = new PooledOfficeManagerSettings(unoUrls[i]);
            settings.setRunAsArgs(runAsArgs);
            settings.setTemplateProfileDir(templateProfileDir);
            settings.setWorkDir(workDir);
            settings.setOfficeHome(officeHome);
            settings.setRetryTimeout(retryTimeout);
            settings.setTaskExecutionTimeout(taskExecutionTimeout);
            settings.setMaxTasksPerProcess(maxTasksPerProcess);
            settings.setProcessManager(processManager);
            this.pooledManagers[i] = new PooledOfficeManager(settings);
        }
        this.logger.info("ProcessManager implementation is " + processManager.getClass().getSimpleName());
    }

    public synchronized void start() throws OfficeException {
        for (final PooledOfficeManager pooledManager : this.pooledManagers) {
            pooledManager.start();
            this.releaseManager(pooledManager);
        }
        this.running = true;
    }

    public void execute(final OfficeTask task) throws IllegalStateException, OfficeException {
        if (!this.running) {
            throw new IllegalStateException("this OfficeManager is currently stopped");
        }
        PooledOfficeManager manager = null;
        try {
            manager = this.acquireManager();
            if (manager == null) {
                throw new OfficeException("no office manager available");
            }
            manager.execute(task);
        } finally {
            if (manager != null) {
                this.releaseManager(manager);
            }
        }
    }

    public synchronized void stop() throws OfficeException {
        this.running = false;
        this.logger.info("stopping");
        this.pool.clear();
        for (final PooledOfficeManager pooledManager : this.pooledManagers) {
            pooledManager.stop();
        }
        this.logger.info("stopped");
    }

    private PooledOfficeManager acquireManager() {
        try {
            return this.pool.poll(this.taskQueueTimeout, TimeUnit.MILLISECONDS);
        } catch (final InterruptedException interruptedException) {
            throw new OfficeException("interrupted", interruptedException);
        }
    }

    private void releaseManager(final PooledOfficeManager manager) {
        try {
            this.pool.put(manager);
        } catch (final InterruptedException interruptedException) {
            throw new OfficeException("interrupted", interruptedException);
        }
    }

	public boolean isRunning() {
		return this.running;
	}

}
