package googlecode.utils4ant.types;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.filters.util.ChainReaderHelper;
import org.apache.tools.ant.types.DataType;
import org.apache.tools.ant.types.FileList;
import org.apache.tools.ant.types.FilterChain;

public class EnhancedFileList extends FileList {
	/**
	 * Inner class corresponding to the &lt;file&gt; nested element.
	 */
	public static class ListFile extends DataType {
		private File srcFile = null;
		private boolean failOnError = true;
		private final Vector filterChains = new Vector();
		/**
		 * Encoding to use for filenames, defaults to the platform's default
		 * encoding.
		 */
		private String encoding = null;

		/**
		 * The file to read the list from
		 * 
		 * @param file
		 *            the file to add to the file list.
		 */
		public void setSrcFile(File file) {
			this.srcFile = file;
		}

		/**
		 * @return the file for this element.
		 */
		public File getSrcFile() {
			return srcFile;
		}

		public boolean isFailOnError() {
			return failOnError;
		}

		public void setFailOnError(boolean failOnError) {
			this.failOnError = failOnError;
		}

		public BufferedReader getSrcFileReader() {
			BufferedReader reader = null;
			FileInputStream fis = null;
			BufferedInputStream bis = null;
			Reader instream = null;
			try {
				final long len = srcFile.length();
				// discard most of really big files
				final int size = (int) len;
				fis = new FileInputStream(srcFile);
				bis = new BufferedInputStream(fis);
				if (encoding == null) {
					instream = new InputStreamReader(bis);
				} else {
					instream = new InputStreamReader(bis, encoding);
				}

				if (size != 0) {
					ChainReaderHelper crh = new ChainReaderHelper();
					crh.setBufferSize(size);
					crh.setPrimaryReader(instream);
					crh.setFilterChains(filterChains);
					crh.setProject(getProject());
					reader = new BufferedReader(crh.getAssembledReader());
				}
			} catch (final IOException ioe) {
				final String message = "Unable to load file: " + ioe.toString();
				if (failOnError) {
					throw new BuildException(message, ioe, getLocation());
				} else {
					log(message, Project.MSG_ERR);
				}
			} catch (final BuildException be) {
				if (failOnError) {
					throw be;
				} else {
					log(be.getMessage(), Project.MSG_ERR);
				}
			} finally {
				try {
					if (fis != null) {
						fis.close();
					}
				} catch (IOException ioex) {
					// ignore
				}
			}
			return reader;
		}

		/**
		 * Add the FilterChain element.
		 * 
		 * @param filter
		 *            the filter to add
		 */
		public final void addFilterChain(FilterChain filter) {
			filterChains.addElement(filter);
		}

		public String getEncoding() {
			return encoding;
		}

		public void setEncoding(String encoding) {
			this.encoding = encoding;
		}
	}

	/**
	 * Add a nested &lt;list&gt; nested element.
	 * 
	 * @param name
	 *            a configured file element with a name.
	 * @since Ant 1.6.2
	 */
	public void addConfiguredList(ListFile list) {
		if (list.getSrcFile() == null) {
			throw new BuildException("No file specified in nested list element");
		} else if (!list.getSrcFile().exists()) {
			String message = "The file specified in nested list element does not exist";
			if (list.isFailOnError()) {
				throw new BuildException(message);
			} else {
				getProject().log(message, Project.MSG_WARN);
				return;
			}
		} else if (!list.getSrcFile().canRead()) {
			String message = "The file specified in nested list element is not readable";
			if (list.isFailOnError()) {
				throw new BuildException(message);
			} else {
				getProject().log(message, Project.MSG_WARN);
				return;
			}
		}
		try {
			BufferedReader br = list.getSrcFileReader();
			String fileName = null;
			while ((fileName = br.readLine()) != null) {
				FileName name = new FileName();
				name.setName(fileName);
				addConfiguredFile(name);
			}
		} catch (IOException e) {
			throw new BuildException(e);
		}
	}

}
