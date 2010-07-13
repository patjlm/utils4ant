package googlecode.utils4ant.tasks;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Iterator;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.FileResource;
import org.apache.tools.ant.util.ResourceUtils;

public class FastCopy extends Task {
	private ResourceCollection rc = null;
	private boolean failOnEmptyResources = false;
	private boolean failOnNotExist = false;
	private boolean failOnError = false;
	private boolean overwrite = false;
	private boolean includeEmptyDirs = true;
	private boolean preserveLastModified = true;
	private boolean printSummary = true;
	private boolean logSkippedResources = false;
	private boolean useApacheIO = false;
	private File toDir = null;

	private boolean useAntResourceUtils = false;
	private int skipLogLEvel = Project.MSG_VERBOSE;

	/**
	 * Set whether to fail if no resource is contained in the nested resource
	 * collection. Default is false.
	 * 
	 * @param failOnEmptyResources
	 *            if true, will fail if no resource is contained in the nested
	 *            resource collection
	 */
	public void setFailOnEmptyResources(boolean failOnEmptyResources) {
		this.failOnEmptyResources = failOnEmptyResources;
	}

	/**
	 * Set whether copy should fail when any of the specified resource does not
	 * exist. Default is false.
	 * 
	 * @param failOnNotExist
	 *            if true, fails if any of the specified resource does not exist
	 */
	public void setFailOnNotExist(boolean failOnNotExist) {
		this.failOnNotExist = failOnNotExist;
	}

	/**
	 * Set whether copy should fail on any other error. Default is false.
	 * 
	 * @param failOnError
	 */
	public void setFailOnError(boolean failOnError) {
		this.failOnError = failOnError;
	}

	/**
	 * Set whether copy should overwrite any existing file. Default is false.
	 * 
	 * @param overwrite
	 */
	public void setOverwrite(boolean overwrite) {
		this.overwrite = overwrite;
	}

	/**
	 * Set whether to copy empty directories. Default is true.
	 * 
	 * @param includeEmptyDirs
	 */
	public void setIncludeEmptyDirs(boolean includeEmptyDirs) {
		this.includeEmptyDirs = includeEmptyDirs;
	}

	/**
	 * Set whether to give the copied files the same last modified time as the
	 * original files. Default is true.
	 * 
	 * @param preserveLastModified
	 */
	public void setPreserveLastModified(boolean preserveLastModified) {
		this.preserveLastModified = preserveLastModified;
	}

	/**
	 * Set whether to print an activity summary at the end of the copy. Default
	 * is true.
	 * 
	 * @param printSummary
	 */
	public void setPrintSummary(boolean printSummary) {
		this.printSummary = printSummary;
	}

	/**
	 * Set whether skipped resources should be logged. Default is false.
	 * 
	 * @param logSkippedResources
	 */
	public void setLogSkippedResources(boolean logSkippedResources) {
		this.logSkippedResources = logSkippedResources;
	}

	/**
	 * Defines if copy should be done using apache IO. NOte that Apache Commons
	 * IO library should be in the classpath of this task if this is set to
	 * true. Default is false.
	 * 
	 * @param useApacheIO
	 */
	public void setUseApacheIO(boolean useApacheIO) {
		this.useApacheIO = useApacheIO;
	}

	/**
	 * Sets the target directory. Mandatory
	 * 
	 * @param toDir
	 */
	public void setToDir(File toDir) {
		this.toDir = toDir;
	}

	/**
	 * Adds a nested resource collection. There should be only one
	 * 
	 * @param rc
	 */
	public void addConfigured(ResourceCollection rc) {
		if (this.rc != null) {
			processIssue(true, "Only one nested resource collection is allowed");
		}
		if (!rc.isFilesystemOnly()) {
			processIssue(true, "Only filesystem resources are allowed");
		}
		this.rc = rc;
	}

	/**
	 * Does the overall processing
	 */
	public void execute() {
		long startTime = System.currentTimeMillis();
		checkSetup();
		log("Trying to copy " + rc.size() + " listed resources");
		int nbCopiedResources = 0;
		int nbCreatedDirectoryResources = 0;
		int nbSkippedResources = 0;
		Iterator it = rc.iterator();
		while (it.hasNext()) {
			Resource r = (Resource) it.next();
			if (!r.isExists()) {
				processIssue(failOnNotExist, "Resource " + r.getName()
						+ " does not exist");
			}
			File destFile = new File(toDir, r.getName());
			if (r.isDirectory()) {
				if (isDirSkipped(r, destFile)) {
					nbSkippedResources++;
				} else {
					if (destFile.mkdirs()) {
						nbCreatedDirectoryResources++;
					} else {
						processIssue(true, "Could not create directory "
								+ destFile.getAbsolutePath());
					}
				}
			} else {
				if (isFileSkipped(r, destFile)) {
					nbSkippedResources++;
				} else {
					doCopy(r, destFile);
					nbCopiedResources++;
				}
			}
		}
		if (printSummary) {
			long endtime = System.currentTimeMillis();
			log("Copy done in " + (endtime - startTime) + " ms");
			log("Created " + nbCreatedDirectoryResources
					+ " listed directories");
			log("Copied " + nbCopiedResources + " resources");
			log("Skipped " + nbSkippedResources + " resources");
		}
	}

	/**
	 * Validate task settings
	 * 
	 */
	private void checkSetup() {
		if (rc == null) {
			processIssue(failOnEmptyResources, "No resource found");
		}
		if (toDir == null) {
			processIssue(true, "Mandatory attribute missing: todir");
		}
		if (logSkippedResources) {
			skipLogLEvel = Project.MSG_INFO;
		}
	}

	/**
	 * log or throw an exception depending on settings
	 * 
	 * @param fail
	 *            if true, throw an BuildException. Else, log a warning
	 * @param message
	 *            the message to log
	 */
	private void processIssue(boolean fail, String message) {
		if (fail) {
			throw new BuildException(message);
		} else {
			log(message, Project.MSG_WARN);
		}
	}

	/**
	 * log or throw an exception depending on settings
	 * 
	 * @param fail
	 *            if true, throw a BuildException. Else, log a warning
	 * @param message
	 *            the exception to embed or to log
	 */
	private void processIssue(boolean fail, Throwable t) {
		if (fail) {
			throw new BuildException(t);
		} else {
			log(t.getMessage(), Project.MSG_WARN);
		}
	}

	/**
	 * Decide whether the specified resource should be skipped or not
	 * 
	 * @param src
	 *            The resource to skip or not
	 * @param destFile
	 *            The target file for the copy of this resource
	 * @return true if the resource should be skipped. False otherwise.
	 */
	private boolean isDirSkipped(Resource src, File destFile) {
		boolean skip = false;
		if (!includeEmptyDirs) {
			if (logSkippedResources) {
				log("Skipping "
						+ src.getName()
						+ " as includeEmptyDirs=false. Will be created if it contains file(s)",
						skipLogLEvel);
			}
			skip = true;
		} else if (destFile.exists()) {
			if (logSkippedResources) {
				log("Skipping " + src.getName()
						+ " as destination already exists", skipLogLEvel);
			}
			skip = true;
		}
		return skip;
	}

	/**
	 * Decide whether the specified resource should be skipped or not
	 * 
	 * @param src
	 *            The resource to skip or not
	 * @param destFile
	 *            The target file for the copy of this resource
	 * @return true if the resource should be skipped. False otherwise.
	 */
	private boolean isFileSkipped(Resource src, File destFile) {
		boolean skip = false;
		if (!overwrite && destFile.exists()) {
			if (src.getLastModified() < destFile.lastModified()) {
				if (logSkippedResources) {
					log("Skipping " + src.getName()
							+ " as destination is newer", skipLogLEvel);
				}
				skip = true;
			}
		} else if (src.equals(destFile)) {
			if (logSkippedResources) {
				log("Skipping " + src.getName()
						+ " as destination is the same file", skipLogLEvel);
			}
			skip = true;
		}
		return skip;
	}

	private byte[] buffer = new byte[4096];

	/**
	 * Does the actual file copy
	 * 
	 * @param src
	 *            The resource to copy
	 * @param destFile
	 *            The target file
	 */
	private void doCopy(Resource src, File destFile) {
		if (useAntResourceUtils) {
			try {
				ResourceUtils.copyResource(src, new FileResource(destFile),
						null, null, overwrite, preserveLastModified, null,
						null, getProject());
			} catch (IOException e) {
				processIssue(failOnError, e);
			}
		} else if (useApacheIO) {
			createParent(destFile);
			try {
				copyWithApacheIO(src.getInputStream(), new FileOutputStream(
						destFile));
			} catch (IOException e) {
				processIssue(failOnError, e);
			}
			if (preserveLastModified) {
				destFile.setLastModified(src.getLastModified());
			}
		} else {
			createParent(destFile);
			InputStream in = null;
			OutputStream out = null;
			try {
				out = new BufferedOutputStream(new FileOutputStream(destFile));
				in = new BufferedInputStream(src.getInputStream());
				int size = -1;
				while ((size = in.read(buffer)) != -1) {
					out.write(buffer, 0, size);
				}
				if (preserveLastModified) {
					destFile.setLastModified(src.getLastModified());
				}
			} catch (IOException e) {
				processIssue(failOnError, e);
			} finally {
				try {
					if (out != null) {
						out.close();
					}
					if (in != null) {
						in.close();
					}
				} catch (IOException e) {
					processIssue(failOnError, e);
				}
			}
		}
	}

	/**
	 * Creates parent directories
	 * 
	 * @param file
	 *            The file to create parents for
	 */
	private void createParent(File file) {
		File parent = file.getParentFile();
		if (!parent.exists() && !parent.mkdirs()) {
			processIssue(true, "Could not create destination directory "
					+ parent.getAbsolutePath());
		}
	}

	/**
	 * Copy using Apache Commons IO
	 * 
	 * @param in
	 *            input stream
	 * @param out
	 *            output stream
	 */
	private void copyWithApacheIO(InputStream in, OutputStream out) {
		try {
			getApacheIOCopyMethod().invoke(null, new Object[] { in, out });
		} catch (Exception e) {
			processIssue(failOnError, e);
		}
	}

	private Method apacheIOCopyMethod = null;

	/**
	 * Gets the Apache Commons IO copy() method (and cache it) by reflection.
	 * This avoids to have the apache library in the classpath if we do not want
	 * to use it
	 * 
	 * @return The copy Method
	 */
	private Method getApacheIOCopyMethod() {
		if (apacheIOCopyMethod == null) {
			Class ioUtilsClass = null;
			try {
				ioUtilsClass = Class.forName("org.apache.commons.io.IOUtils");
			} catch (ClassNotFoundException e) {
				processIssue(true, e);
			}
			try {
				apacheIOCopyMethod = ioUtilsClass.getMethod("copy",
						new Class[] { InputStream.class, OutputStream.class });
			} catch (Exception e) {
				processIssue(failOnError, e);
			}
		}
		return apacheIOCopyMethod;
	}
}
