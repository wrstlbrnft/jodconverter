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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.artofsolving.jodconverter.process.ProcessManager;
import org.artofsolving.jodconverter.process.ProcessQuery;
import org.artofsolving.jodconverter.util.PlatformUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class OfficeProcess {

    private final File officeHome;
    private final UnoUrl unoUrl;
    private final String[] runAsArgs;
    private final File templateProfileDir;
    private final File instanceProfileDir;
    private final ProcessManager processManager;

    private Process process;
    private long pid = ProcessManager.PID_UNKNOWN;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public OfficeProcess(final File officeHome, final UnoUrl unoUrl, final String[] runAsArgs, final File templateProfileDir, final File workDir, final ProcessManager processManager) {
        this.officeHome = officeHome;
        this.unoUrl = unoUrl;
        this.runAsArgs = runAsArgs;
        this.templateProfileDir = templateProfileDir;
        this.instanceProfileDir = this.getInstanceProfileDir(workDir, unoUrl);
        this.processManager = processManager;
    }

    public void start() throws IOException {
        this.start(false);
    }

    public void start(final boolean restart) throws IOException {
        final ProcessQuery processQuery = new ProcessQuery("soffice.bin", this.unoUrl.getAcceptString());
        final long existingPid = this.processManager.findPid(processQuery);
    	if (!(existingPid == ProcessManager.PID_NOT_FOUND || existingPid == ProcessManager.PID_UNKNOWN)) {
			throw new IllegalStateException(String.format("a process with acceptString '%s' is already running; pid %d",
			        this.unoUrl.getAcceptString(), existingPid));
        }
    	if (!restart) {
    	    this.prepareInstanceProfileDir();
    	}
        final List<String> command = new ArrayList<String>();
        final File executable = OfficeUtils.getOfficeExecutable(this.officeHome);
        if (this.runAsArgs != null) {
        	command.addAll(Arrays.asList(this.runAsArgs));
        }
        command.add(executable.getAbsolutePath());
        command.add("-accept=" + this.unoUrl.getAcceptString() + ";urp;");
        command.add("-env:UserInstallation=" + OfficeUtils.toUrl(this.instanceProfileDir));
        command.add("-headless");
        //command.add("-nocrashreport");
        command.add("-nodefault");
        command.add("-nofirststartwizard");
        command.add("-nolockcheck");
        command.add("-nologo");
        command.add("-norestore");
        final ProcessBuilder processBuilder = new ProcessBuilder(command);
        if (PlatformUtils.isWindows()) {
            this.addBasisAndUrePaths(processBuilder);
        }
        this.logger.info(String.format("starting process with acceptString '%s' and profileDir '%s'", this.unoUrl, this.instanceProfileDir));
        this.process = processBuilder.start();

		// Fix for 24165, on linux it seems to take long the first time to start up. So adding a 5 second delay.
		if (PlatformUtils.isLinux()) {
			try {
				Thread.sleep(5000);
			} catch (final InterruptedException e) {
				this.logger.info(e.getMessage());
			}
		}

        this.pid = this.processManager.findPid(processQuery);
        if (this.pid == ProcessManager.PID_NOT_FOUND) {
            throw new IllegalStateException(String.format("process with acceptString '%s' started but its pid could not be found",
                    this.unoUrl.getAcceptString()));
        }
        this.logger.info("started process" + (this.pid != ProcessManager.PID_UNKNOWN ? "; pid = " + this.pid : ""));
    }

    private File getInstanceProfileDir(final File workDir, final UnoUrl unoUrl) {
        final String dirName = ".jodconverter_" + unoUrl.getAcceptString().replace(',', '_').replace('=', '-');
        return new File(workDir, dirName);
    }

    private void prepareInstanceProfileDir() throws OfficeException {
        if (this.instanceProfileDir.exists()) {
            this.logger.warn("profile dir '{}' already exists; deleting", this.instanceProfileDir);
            this.deleteProfileDir();
        }
        if (this.templateProfileDir != null) {
            try {
                FileUtils.copyDirectory(this.templateProfileDir, this.instanceProfileDir);
            } catch (final IOException ioException) {
                throw new OfficeException("failed to create profileDir", ioException);
            }
        }
    }

    public void deleteProfileDir() {
        if (this.instanceProfileDir != null) {
            try {
                FileUtils.deleteDirectory(this.instanceProfileDir);
            } catch (final IOException ioException) {
                final File oldProfileDir = new File(this.instanceProfileDir.getParentFile(), this.instanceProfileDir.getName() + ".old." + System.currentTimeMillis());
                if (this.instanceProfileDir.renameTo(oldProfileDir)) {
                    this.logger.warn("could not delete profileDir: " + ioException.getMessage() + "; renamed it to " + oldProfileDir);
                } else {
                    this.logger.error("could not delete profileDir: " + ioException.getMessage());
                }
            }
        }
    }

    private void addBasisAndUrePaths(final ProcessBuilder processBuilder) throws IOException {
        // see http://wiki.services.openoffice.org/wiki/ODF_Toolkit/Efforts/Three-Layer_OOo
        final File basisLink = new File(this.officeHome, "basis-link");
        if (!basisLink.isFile()) {
            this.logger.debug("no %OFFICE_HOME%/basis-link found; assuming it's OOo 2.x and we don't need to append URE and Basic paths");
            return;
        }
        final String basisLinkText = FileUtils.readFileToString(basisLink).trim();
        final File basisHome = new File(this.officeHome, basisLinkText);
        final File basisProgram = new File(basisHome, "program");
        final File ureLink = new File(basisHome, "ure-link");
        final String ureLinkText = FileUtils.readFileToString(ureLink).trim();
        final File ureHome = new File(basisHome, ureLinkText);
        final File ureBin = new File(ureHome, "bin");
        final Map<String,String> environment = processBuilder.environment();
        // Windows environment variables are case insensitive but Java maps are not :-/
        // so let's make sure we modify the existing key
        String pathKey = "PATH";
        for (final String key : environment.keySet()) {
            if ("PATH".equalsIgnoreCase(key)) {
                pathKey = key;
            }
        }
        final String path = environment.get(pathKey) + ";" + ureBin.getAbsolutePath() + ";" + basisProgram.getAbsolutePath();
        this.logger.debug("setting {} to \"{}\"", pathKey, path);
        environment.put(pathKey, path);
    }

    public boolean isRunning() {
        if (this.process == null) {
            return false;
        }
        return this.getExitCode() == null;
    }

    private class ExitCodeRetryable extends Retryable {

        private int exitCode;

        @Override
        protected void attempt() throws TemporaryException, Exception {
            try {
                this.exitCode = OfficeProcess.this.process.exitValue();
            } catch (final IllegalThreadStateException illegalThreadStateException) {
                throw new TemporaryException(illegalThreadStateException);
            }
        }

        public int getExitCode() {
            return this.exitCode;
        }

    }

    public Integer getExitCode() {
        try {
            return this.process.exitValue();
        } catch (final IllegalThreadStateException exception) {
            return null;
        }
    }

    public int getExitCode(final long retryInterval, final long retryTimeout) throws RetryTimeoutException {
        try {
            final ExitCodeRetryable retryable = new ExitCodeRetryable();
            retryable.execute(retryInterval, retryTimeout);
            return retryable.getExitCode();
        } catch (final RetryTimeoutException retryTimeoutException) {
            throw retryTimeoutException;
        } catch (final Exception exception) {
            throw new OfficeException("could not get process exit code", exception);
        }
    }

    public int forciblyTerminate(final long retryInterval, final long retryTimeout) throws IOException, RetryTimeoutException {
        this.logger.info(String.format("trying to forcibly terminate process: '" + this.unoUrl + "'" + (this.pid != ProcessManager.PID_UNKNOWN ? " (pid " + this.pid  + ")" : "")));
        this.processManager.kill(this.process, this.pid);
        return this.getExitCode(retryInterval, retryTimeout);
    }

}
