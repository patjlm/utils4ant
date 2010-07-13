package googlecode.utils4ant.launcher;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.Properties;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.listener.Log4jListener;

public class AntLauncher {
	private String previousLog4jConfiguration = null;
	
	public void launch(File baseDir, File buildFile, String target,
			Properties inheritedProperties, File log4JPropertiesFile) throws AntLauncherException {
		Project p = new Project();
		p.initProperties();
		p.setBaseDir(getBaseDir(baseDir, buildFile));
		p.setUserProperty("ant.file", getBuildFile(buildFile));
		// any specified classpaths are appended to the system classpath
		p.setProperty("build.sysclasspath", "first");

		// Copy all inherited properties into the project
		if (inheritedProperties != null) {
			Iterator iterator = inheritedProperties.keySet().iterator();
			while (iterator.hasNext()) {
				String key = (String) iterator.next();
				p.setUserProperty(key, inheritedProperties.getProperty(key));
			}
		}

		if (log4JPropertiesFile != null) {
			setupLog4J(p, log4JPropertiesFile);
		} else {
			setupConsoleLogger(p);
		}
		
		try {
			p.fireBuildStarted();
			p.init();
			ProjectHelper helper = ProjectHelper.getProjectHelper();
			p.addReference("ant.projectHelper", helper);
			helper.parse(p, buildFile);
			p.executeTarget(null == target ? p.getDefaultTarget() : target);
			p.fireBuildFinished(null);
		} catch (BuildException e) {
			e.printStackTrace();
			p.fireBuildFinished(e);
			throw new AntLauncherException(e);
		} finally {
			if (previousLog4jConfiguration != null) {
				System.setProperty("log4j.configuration", previousLog4jConfiguration);
			}
		}
	}

	private String getBuildFile(File buildFile) {
		return buildFile == null ? "build.xml" : buildFile.getAbsolutePath();
	}
	
	private File getBaseDir(File baseDir, File buildFile) {
		File res = baseDir;
		if (res == null) {
			if (buildFile == null) {
				res = new File(".");
			} else {
				res = buildFile.getParentFile();
			}
		}
		return res;
	}
	
	private void setupLog4J(Project p, File log4JPropertiesFile) {
		previousLog4jConfiguration = System.getProperty("log4j.configuration");
		try {
			if (previousLog4jConfiguration != null) {
				System.out.println("Warning: log4j.configuration system property existed before launching ant: log4j.configuration = " + previousLog4jConfiguration);
				System.out.println("Warning: changing this value to " + log4JPropertiesFile.toURI().toURL().toString());
			}
			System.setProperty("log4j.configuration", log4JPropertiesFile.toURI().toURL().toString());
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}
		Log4jListener log4jListener = new Log4jListener();
		p.addBuildListener(log4jListener);
	}
	
	private void setupConsoleLogger(Project p) {
		DefaultLogger consoleLogger = new DefaultLogger();
		consoleLogger.setErrorPrintStream(System.err);
		consoleLogger.setOutputPrintStream(System.out);
		consoleLogger.setMessageOutputLevel(Project.MSG_INFO);
		p.addBuildListener(consoleLogger);
	}
}
