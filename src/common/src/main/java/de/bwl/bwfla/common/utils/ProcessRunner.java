/*
 * This file is part of the Emulation-as-a-Service framework.
 *
 * The Emulation-as-a-Service framework is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * The Emulation-as-a-Service framework is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Emulation-as-a-Software framework.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package de.bwl.bwfla.common.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.bwl.bwfla.conf.CommonSingleton;


/**
 * Native command executor, based on {@link ProcessBuilder}.
 * <p/>
 * 
 * <b>NOTE:</b> This implementation currently only works with Linux-based systems!
 */
public class ProcessRunner
{
	/** Logger instance. */
	private static final Logger log = LoggerFactory.getLogger(ProcessRunner.class);
	
	// Member fields
	private final StringBuilder sbuilder;
	private final Map<String, String> environment;
	private final List<String> command;
	private Process process;
	private ProcessMonitor monitor;
	private ProcessOutput stdout;
	private ProcessOutput stderr;
	private int pid;
	private Path workdir;
	private Path outdir;
	private Path runfile;
	private Path pidfile;
	private long timeout = 2 * 1000;  // 2 sec
	private int numWaitingCallers;
	private State state;
	
	/** Internal states */
	private static enum State
	{
		INVALID,
		READY,
		STARTED,
		STOPPED
	}
	
	/** PID, representing an invalid process. */
	private static final int INVALID_PID = -1;

	/** Default capacity of the command-builder. */
	private static final int DEFAULT_CMDBUILDER_CAPACITY = 512;
	
	/** Exception's message */
	private static final String MESSAGE_IOSTREAM_NOT_AVAILABLE = "IO-stream is not available. Process was not started properly!";

	// Initialize the constants from property file
	private static final String PROPERTY_WRAPPER_SCRIPT = CommonSingleton.CONF.processRunner.wrapperScript;
	private static final String PROPERTY_TMPDIR_PREFIX = CommonSingleton.CONF.processRunner.tmpdirPrefix;
	private static final String PROPERTY_RUNFLAG_FILENAME = CommonSingleton.CONF.processRunner.runflagFilename;
	private static final String PROPERTY_PID_FILENAME = CommonSingleton.CONF.processRunner.pidFilename;
	private static final String PROPERTY_STDOUT_FILENAME = CommonSingleton.CONF.processRunner.stdoutFilename;
	private static final String PROPERTY_STDERR_FILENAME = CommonSingleton.CONF.processRunner.stderrFilename;

	/** Create a new ProcessRunner. */
	public ProcessRunner()
	{
		this(DEFAULT_CMDBUILDER_CAPACITY);
	}
	
	/** Create a new ProcessRunner. */
	public ProcessRunner(int cmdCapacity)
	{
		this.sbuilder = new StringBuilder(1024);
		this.environment = new HashMap<String, String>();
		this.command = new ArrayList<String>(cmdCapacity);
		
		this.reset(true);
	}

	/** Create a new ProcessRunner with the specified command. */
	public ProcessRunner(String cmd)
	{
		this(DEFAULT_CMDBUILDER_CAPACITY);
		this.setCommand(cmd, true);
	}
	
	/** Define a new command, resetting this runner. */
	public void setCommand(String cmd)
	{
		this.setCommand(cmd, false);
	}
	
	/**
	 * Define a new command to run in a subprocess.
	 * @param cmd The new command to execute.
	 * @param keepenv If true, then the current environment variables will be reused, else cleared.
	 */
	public synchronized void setCommand(String cmd, boolean keepenv)
	{
		if (state != State.INVALID)
			throw new IllegalStateException("ProcessRunner was not stopped/cleaned correctly!");
		
		this.reset(keepenv);
		command.add(cmd);
		
		state = State.READY;
	}
	
	/**
	 * Add a new argument to current command, separated by a space.
	 * @param arg The argument to add.
	 */
	public synchronized void addArgument(String arg)
	{
		this.ensureStateReady();
		command.add(arg);
	}
	
	/**
	 * Compose a new argument from multiple values and add it to current command.
	 * @param values The values to build the argument from.
	 */
	public synchronized void addArgument(String... values)
	{
		this.ensureStateReady();
		
		sbuilder.setLength(0);
		for (String value : values)
			sbuilder.append(value);

		command.add(sbuilder.toString());
	}

	/**
	 * Append a new argument's value to last argument.
	 * @param value The argument's value to add.
	 */
	public synchronized void addArgValue(String value)
	{
		this.ensureStateReady();
		
		final int index = command.size() - 1;
		String argument = command.get(index);
		command.set(index, argument + value);
	}
	
	/**
	 * Append new argument's values to last argument.
	 * @param values The argument's values to add.
	 */
	public synchronized void addArgValues(String... values)
	{
		this.ensureStateReady();
		
		sbuilder.setLength(0);
		for (String value : values)
			sbuilder.append(value);

		final int index = command.size() - 1;
		String argument = command.get(index);
		argument += sbuilder.toString();
		command.set(index, argument);
	}
	
	/**
	 * Add all arguments from the specified list to current command.
	 * @param args The arguments to add.
	 */
	public synchronized void addArguments(String... args)
	{
		this.ensureStateReady();

		for (String arg : args)
			command.add(arg);
	}
	
	/**
	 * Add all arguments from the specified list to current command.
	 * @param args The arguments to add.
	 */
	public synchronized void addArguments(List<String> args)
	{
		this.ensureStateReady();
		
		for (String arg : args)
			command.add(arg);
	}
	
	/**
	 * Add a new environment-variable to current command.
	 * @param var The variable's name.
	 * @param value The variable's value.
	 */
	public synchronized void addEnvVariable(String var, String value)
	{
		environment.put(var, value);
	}
	
	
	/**
	 * Add all environment-variables to current command.
	 * @param vars The variables to add.
	 */
	public synchronized void addEnvVariables(Map<String, String> vars)
	{
		environment.putAll(vars);
	}

	/** Returns the environment variables of the current command. */
	public Map<String, String> getEnvVariables()
	{
		return environment;
	}
	
	/** Returns the current command as string. */
	public synchronized String getCommandString()
	{
		if (command.isEmpty())
			return "";
		
		sbuilder.setLength(0);
		for (String arg : command) {
			sbuilder.append(arg);
			sbuilder.append(' ');
		}
		
		int last = sbuilder.length() - 1;
		return sbuilder.substring(0, last);
	}
	
	/** Returns the monitor for the running subprocess. */
	public synchronized ProcessMonitor getProcessMonitor()
	{
		if (state != State.STARTED)
			throw new IllegalStateException("Monitor is not available. Process was not started properly!");

		return monitor;
	}
	
	/**
	 * The directory to be used as working directory.
	 * If not set, uses the dir of the current process.
	 * @param dir The new working directory.
	 */
	public synchronized void setWorkingDirectory(Path dir)
	{
		this.ensureStateReady();
		workdir = dir;
	}

	/**
	 * Set the timeout for starting the subprocess.
	 * When the subprocess was not started during
	 * this time, it will be considered as error.
	 * 
	 * @param timeout The new timeout in milliseconds
	 */
	public synchronized void setStartTimeout(long timeout)
	{
		this.timeout = timeout;
	}

	/** Returns the stdin of the process, as byte-stream. */
	public synchronized OutputStream getStdInStream() throws IOException
	{
		if (!(state == State.STARTED || state == State.STOPPED))
			throw new IllegalStateException();
		
		ProcessRunner.ensureNotNull(process, MESSAGE_IOSTREAM_NOT_AVAILABLE);
		return process.getOutputStream();
	}
	
	/** Returns the stdin of the process, as char-stream. */
	public Writer getStdInWriter() throws IOException
	{
		OutputStream stream = this.getStdInStream();
		return new OutputStreamWriter(stream);
	}
	
	/**
	 * Write a string to stdin of the subprocess. <p/>
	 * <b>NOTE:</b>
	 *     For writing multiple messages it is more efficient to get the writer
	 *     returned by {@link #getStdInWriter()} once and write to it directly!
	 * 
	 * @param message The data to write.
	 */
	public void writeToStdIn(String message) throws IOException
	{
		Writer writer = this.getStdInWriter();
		writer.write(message);
		writer.flush();
	}
	
	/** Returns the stdout of the process, as byte-stream. */
	public synchronized InputStream getStdOutStream() throws IOException
	{
		ProcessRunner.ensureNotNull(stdout, MESSAGE_IOSTREAM_NOT_AVAILABLE);
		return stdout.stream();
	}

	/** Returns the stdout of the process, as char-stream. */
	public synchronized Reader getStdOutReader() throws IOException
	{
		ProcessRunner.ensureNotNull(stdout, MESSAGE_IOSTREAM_NOT_AVAILABLE);
		return stdout.reader();
	}
	
	/** Returns the stdout of the process, as string. */
	public synchronized String getStdOutString() throws IOException
	{
		ProcessRunner.ensureNotNull(stdout, MESSAGE_IOSTREAM_NOT_AVAILABLE);
		return stdout.string();
	}
	
	/** Returns the stderr of the process, as byte-stream. */
	public synchronized InputStream getStdErrStream() throws IOException
	{
		ProcessRunner.ensureNotNull(stderr, MESSAGE_IOSTREAM_NOT_AVAILABLE);
		return stderr.stream();
	}
	
	/** Returns the stderr of the process, as char-stream. */
	public synchronized Reader getStdErrReader() throws IOException
	{
		ProcessRunner.ensureNotNull(stderr, MESSAGE_IOSTREAM_NOT_AVAILABLE);
		return stderr.reader();
	}
	
	/** Returns the stderr of the process, as string. */
	public synchronized String getStdErrString() throws IOException
	{
		ProcessRunner.ensureNotNull(stderr, MESSAGE_IOSTREAM_NOT_AVAILABLE);
		return stderr.string();
	}

	/** Print the stdout of the process to the log. */
	public void printStdOut() 
	{
		final int pid = this.getProcessId();

		
		// Print stdout, if available
		try
		{
			String output = this.getStdOutString();
			if (!output.isEmpty())
				log.info("Subprocess {} STDOUT:\n{}", pid, output);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/** Print the stderr of the process to the log. */
	public void printStdErr()
	{
		final int pid = this.getProcessId();

		try
		{
			// Print stderr, if available
			String output = this.getStdErrString();
			if (!output.isEmpty())
				log.info("Subprocess {} STDERR:\n{}", pid, output);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Get the return-code of the process. Can be
	 * called only after the process terminates!
	 */
	public synchronized int getReturnCode()
	{
		if (state != State.STOPPED)
			throw new IllegalStateException("Return code is available only after process termination!");
		
		return process.exitValue();
	}

	/** Get the ID of the process. */
	public synchronized int getProcessId()
	{
		return pid;
	}
	
	/** Returns true when this runner represents a valid process, else false. */
	public synchronized boolean isProcessValid()
	{
		return (state == State.STARTED || state == State.STOPPED);
	}
	
	/** Returns true when the process is in a running state, else false. */
	public synchronized boolean isProcessRunning()
	{
		if (state != State.STARTED)
			return false;
		
		return Files.exists(runfile);
	}
	
	/** Returns true when the process has finished execution, else false. */
	public synchronized boolean isProcessFinished()
	{
		if (state == State.STOPPED)
			return true;
		
		if (state == State.STARTED)
			return (Files.notExists(runfile) && Files.exists(pidfile));
		
		else throw new IllegalStateException("Process was not started/stopped properly!");
	}
	
	/**
	 * Start the process, that is represented by this runner.
	 * @return true when the start was successful, else false.
	 */
	public synchronized boolean start()
	{
		if (state != State.READY)
			throw new IllegalStateException("Process not ready to start!");
		
		// Create the temp-directory for process' output
		try {
			outdir = Files.createTempDirectory(PROPERTY_TMPDIR_PREFIX).toAbsolutePath();
			runfile = outdir.resolve(PROPERTY_RUNFLAG_FILENAME);
			pidfile = outdir.resolve(PROPERTY_PID_FILENAME);
			stdout = new ProcessOutput(outdir.resolve(PROPERTY_STDOUT_FILENAME));
			stderr = new ProcessOutput(outdir.resolve(PROPERTY_STDERR_FILENAME));
		}
		catch (IOException exception) {
			String message = "Could not create a temporary directory for a new subprocess.";
			throw new RuntimeException(message, exception);
		}
		
		// Prepare the wrapped command
		List<String> cmd = new ArrayList<String>(3 + command.size());
		cmd.add(PROPERTY_WRAPPER_SCRIPT);
		cmd.add(pidfile.toString());
		cmd.add(runfile.toString());
		cmd.addAll(command);
		
		// Prepare the process to run
		ProcessBuilder builder = new ProcessBuilder(cmd);
		builder.environment().putAll(environment);

		// Set working directory
		if (workdir != null)
			builder.directory(workdir.toFile());

		// Setup stdout + stderr redirection
		builder.redirectOutput(stdout.file());
		builder.redirectError(stderr.file());
		
		// Finally start the process
		try {
			String cmdstr = this.getCommandString();
			log.info("Starting subprocess:  {}", cmdstr);
			process = builder.start();
		}
		catch (IOException exception) {
			log.error("Error occured while starting a new subprocess.");
			exception.printStackTrace();
			this.cleanup();
			return false;
		}
		
		state = State.STARTED;

		pid = this.waitUntilProcessReady(pidfile, timeout);
		if (pid == INVALID_PID) {
			// Starting failed!
			this.stop();
			this.cleanup();
			return false;
		}
		
		try {
			// Try to create the monitor
			monitor = new ProcessMonitor(pid);
		}
		catch (FileNotFoundException e) {
			// Likely the process was already terminated! 
		}
		
		return true;
	}
	
	/**
	 * Block and wait for a running process to finish.
	 * @return The return-code of the terminated process.
	 */
	public int waitUntilFinished()
	{
		Process procref = null;
		int pidref = INVALID_PID;
		
		synchronized (this) 
		{
			if (state != State.STARTED && state != State.STOPPED) {
				String message = "Waiting is not possible. Process was not started/stopped properly!";
				throw new IllegalStateException(message);
			}
			
			procref = process;
			pidref = this.getProcessId();

			// First waiting caller?
			if (++numWaitingCallers == 1)
				log.info("Waiting for subprocess {} to finish...", pidref);

			// Release the lock of this
		}

		// Wait for the process termination
		int retcode = -1;
		try {
			retcode = procref.waitFor();
		}
		catch (InterruptedException e) {
			// Ignore it!
		}
		
		// Acquire the lock of this again
		synchronized (this) {
			
			state = State.STOPPED;
			
			// Last waiting caller?
			if (--numWaitingCallers == 0)
				log.info("Subprocess {} terminated with code {}", pidref, retcode);
		}
		
		return retcode;
	}
	
	/** Stop the running process and wait for termination. */
	public synchronized void stop()
	{
		if (state != State.STARTED)
			return;
		
		log.info("Stopping subprocess {}...", pid);
		process.destroy();
		
		this.waitUntilFinished();
	}
	
	/** Perform the cleanup of process' temp-directory. */
	public synchronized void cleanup()
	{
		if (this.isProcessRunning())
			throw new IllegalStateException("Attempt to cleanup a running process!");
		
		try {
			// Close all io-streams
			
			if (stdout != null) {
				stdout.close();
				stdout.cleanup();
			}
			
			if (stderr != null) {
				stderr.close();
				stderr.cleanup();
			}
			
			// Delete created files
			
			if (pidfile != null)
				Files.deleteIfExists(pidfile);
			
			if (runfile != null)
				Files.deleteIfExists(runfile);
			
			if (outdir != null)
				Files.deleteIfExists(outdir);
		}
		catch (IOException exception) {
			log.error("Cleanup of process-output directory failed!");
			exception.printStackTrace();
		}
		
		state = State.INVALID;
	}

	
	/* ==================== Helper Methods ==================== */
	
	/**
	 * Start this process and wait for it to finish.
	 * When process terminates, print stdout/stderr and perform cleanup-operations.
	 * @return true when the return-code of the terminated process is 0, else false.
	 */
	public boolean execute()
	{
		return this.execute(true, true);
	}
	
	/**
	 * Start this process and wait for it to finish.
	 * @param verbose If set to true, then print stdout and stderr when process terminates.
	 * @param cleanup If set to true, then perform cleanup-operations when process terminates.
	 * @return @return true when the return-code of the terminated process is 0, else false.
	 */
	public boolean execute(boolean verbose, boolean cleanup)
	{
		if (!this.start())
			return false;
		
		final int retcode = this.waitUntilFinished();

		if (verbose) {
			try {
				this.printStdOut();
				this.printStdErr();
			}
			catch (Exception exception) {
				exception.printStackTrace();
			}
		}
		
		if (cleanup)
			this.cleanup();
		
		return (retcode == 0);
	}
	
	
	/* ==================== Internal Methods ==================== */
	
	private int waitUntilProcessReady(Path pidfile, long timeout)
	{
		// Wait for the subprocess to fully start.
		final long endtime = System.currentTimeMillis() + timeout;
		while (!Files.exists(pidfile)) {
			// Maximal timeout reached?
			long curtime = System.currentTimeMillis();
			if (curtime > endtime) {
				log.error("Could not start a new subprocess, timeout reached!");
				return INVALID_PID;
			}

			try {
				// Wait and retry
				Thread.sleep(100);
			}
			catch (InterruptedException exception) {
				// Restore the interrupted state
				Thread.currentThread().interrupt();
				return INVALID_PID;
			}
		}
		
		try {
			// Read the ID of the started subprocess
			List<String> content = Files.readAllLines(pidfile, StandardCharsets.UTF_8);
			if (content.size() == 1) {
				String pidstr = content.get(0);
				return Integer.parseInt(pidstr);
			}
			else log.error("Invalid content in the PID file found!");
		}
		catch (IOException exception) {
			log.error("Could not read the PID.");
			exception.printStackTrace();
		}
		
		return INVALID_PID;
	}
	
	private void reset(boolean keepenv)
	{
		if (!keepenv)
			environment.clear();
		
		command.clear();
		
		workdir = null;
		process = null;
		monitor = null;
		outdir = null;
		runfile = null;
		pidfile = null;
		stdout = null;
		stderr = null;
		
		pid = INVALID_PID;
		numWaitingCallers = 0;
		state = State.INVALID;
	}

	private void ensureStateReady()
	{
		if (state != State.READY)
			throw new IllegalStateException("No command specified!");
	}
	
	private static void ensureNotNull(Object object, String message)
	{
		if (object == null)
			throw new IllegalStateException(message);
	}
}


final class ProcessOutput
{
	private Path outpath;
	private InputStream outstream;
	
	ProcessOutput(Path path)
	{
		this.outpath = path;
		this.outstream = null;
	}
	
	public Path path()
	{
		return outpath;
	}
	
	public File file()
	{
		return outpath.toFile();
	}
	
	public InputStream stream() throws IOException
	{
		if (outstream == null)
			outstream = Files.newInputStream(outpath);

		return outstream;
	}
	
	public Reader reader() throws IOException
	{
		InputStream stream = this.stream();
		return new InputStreamReader(stream);
	}
	
	public String string() throws IOException
	{
		StringBuilder builder = new StringBuilder(1024);
		char[] buffer = new char[512];
		Reader reader = this.reader();
		while (reader.ready()) {
			int length = reader.read(buffer);
			if (length < 0)
				break;  // End-of-stream
			
			builder.append(buffer, 0, length);
		}
		
		return builder.toString();
	}
	
	public void close() throws IOException
	{
		if (outstream != null)
			outstream.close();
	}
	
	public void cleanup() throws IOException
	{
		Files.deleteIfExists(outpath);
	}
	
	public boolean exists()
	{
		return Files.exists(outpath);
	}
}
