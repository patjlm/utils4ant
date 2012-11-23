package googlecode.utils4ant.tasks;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.PatternSet;
import org.apache.tools.ant.types.selectors.SelectorUtils;
import org.apache.tools.ant.util.FileNameMapper;
import org.apache.tools.ant.util.IdentityMapper;

public class Unzip2Zip extends Task {
	private File src = null;
	private File dest = null;

	private PatternSet patternSet = null;
	private boolean caseSensitive = false;

	private FileNameMapper mapper = null;

	public void execute() {
		try {
			ZipFile srcZipFile = new ZipFile(src);
			log("Reading zip " + src.getAbsolutePath(), Project.MSG_VERBOSE);
			ZipArchiveOutputStream zos = new ZipArchiveOutputStream(dest);
			log("Writing zip " + dest.getAbsolutePath(), Project.MSG_VERBOSE);
			Enumeration<ZipArchiveEntry> srcEntries = srcZipFile.getEntries();
			while (srcEntries.hasMoreElements()) {
				ZipArchiveEntry srcEntry = srcEntries.nextElement();
				if (isIncluded(srcEntry.getName())) {
					String[] destNames = getMapper().mapFileName(srcEntry.getName());
					if (destNames != null) {
						for (int i = 0; i < destNames.length; i++) {
							if (destNames[i] != null) {
								getProject();
								log("Zipping entry " + srcEntry.getName() + " as " + destNames[i], Project.MSG_VERBOSE);
								zos.putArchiveEntry(new ZipArchiveEntry(destNames[i]));
								InputStream is = srcZipFile.getInputStream(srcEntry);
								byte[] b = new byte[2048];
								int size;
								while ((size = is.read(b)) != -1) {
									zos.write(b, 0, size);
								}
								zos.closeArchiveEntry();
							}
						}
					}
				}
			}
			srcZipFile.close();
			zos.close();
		} catch (IOException e) {
			throw new BuildException(e);
		}
	}

	public File getSrc() {
		return src;
	}

	public void setSrc(File src) {
		this.src = src;
	}

	public File getDest() {
		return dest;
	}

	public void setDest(File dest) {
		this.dest = dest;
	}

	public boolean isCaseSensitive() {
		return caseSensitive;
	}

	public void setCaseSensitive(boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
	}

	public FileNameMapper getMapper() {
		return mapper == null ? new IdentityMapper() : mapper;
	}

	public void addConfigured(FileNameMapper mapper) {
		if (this.mapper != null) {
			throw new BuildException("Only one nested mapper is allowed");
		}
		this.mapper = mapper;
	}

	public void add(PatternSet patternSet) {
		this.patternSet = patternSet;
	}

	private boolean isIncluded(String entryName) {
		boolean isIncluded = true;
		if (patternSet != null && patternSet.hasPatterns(getProject())) {
			isIncluded = false;
			String[] includes = patternSet.getIncludePatterns(getProject());
			if (includes.length == 0) {
				isIncluded = true; // default include = **
			} else {
				for (int i = 0; i < includes.length; i++) {
					if (SelectorUtils.match(includes[i], entryName, caseSensitive)) {
						isIncluded = true;
						break;
					}
				}
			}
			if (isIncluded) {
				String[] excludes = patternSet.getExcludePatterns(getProject());
				for (int i = 0; i < excludes.length; i++) {
					if (SelectorUtils.match(excludes[i], entryName, caseSensitive)) {
						isIncluded = false;
						break;
					}
				}
			}
		}
		return isIncluded;
	}
}
