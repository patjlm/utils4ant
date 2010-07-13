package googlecode.utils4ant.launcher;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.helpers.NullEnumeration;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;

/**
 * Listener which sends events to Log4j logging system
 * 
 */
public class AntListener implements BuildListener {

	/** Indicates if the listener was initialized. */
	private boolean initialized = false;

	/**
	 * Construct the listener and make sure there is a valid appender.
	 */
	public AntListener(String configFilename) {
		initialized = false;
		PropertyConfigurator.configureAndWatch(configFilename);
		Logger logger = Logger.getLogger("pk.ant.launcher");
		Logger rootLogger = Logger.getRootLogger();
		if (!(rootLogger.getAllAppenders() instanceof NullEnumeration)) {
			initialized = true;
		} else {
			logger.error("No log4j.properties in build area");
		}
	}

	/**
	 * @see BuildListener#buildStarted
	 */
	public void buildStarted(BuildEvent event) {
		if (initialized) {
			getLogger(Project.class).info("Build started.");
		}
	}

	/**
	 * @see BuildListener#buildFinished
	 */
	public void buildFinished(BuildEvent event) {
		if (initialized) {
			Logger logger = getLogger(Project.class);
			if (event.getException() == null) {
				logger.info("Build finished.");
			} else {
				logger.error("Build finished with error.", event.getException());
			}
		}
	}

	/**
	 * @see BuildListener#targetStarted
	 */
	public void targetStarted(BuildEvent event) {
		if (initialized) {
			Logger logger = getLogger(Target.class);
			logger.info("Target \"" + event.getTarget().getName() + "\" started.");
		}
	}

	/**
	 * @see BuildListener#targetFinished
	 */
	public void targetFinished(BuildEvent event) {
		if (initialized) {
			String targetName = event.getTarget().getName();
			Logger logger = getLogger(Target.class);
			if (event.getException() == null) {
				logger.info("Target \"" + targetName + "\" finished.");
			} else {
				logger.error("Target \"" + targetName + "\" finished with error.",
						event.getException());
			}
		}
	}

	/**
	 * @see BuildListener#taskStarted
	 */
	public void taskStarted(BuildEvent event) {
		if (initialized) {
			Task task = event.getTask();
			Logger logger = getLogger(task.getClass());
			logger.info("Task \"" + task.getTaskName() + "\" started.");
		}
	}

	/**
	 * @see BuildListener#taskFinished
	 */
	public void taskFinished(BuildEvent event) {
		if (initialized) {
			Task task = event.getTask();
			Logger logger = getLogger(task.getClass());
			if (event.getException() == null) {
				logger.info("Task \"" + task.getTaskName() + "\" finished.");
			} else {
				logger.error("Task \"" + task.getTaskName()
						+ "\" finished with error.", event.getException());
			}
		}
	}

	/**
	 * @see BuildListener#messageLogged
	 */
	public void messageLogged(BuildEvent event) {
		if (initialized) {
			Object categoryObject = event.getTask();
			if (categoryObject == null) {
				categoryObject = event.getTarget();
				if (categoryObject == null) {
					categoryObject = event.getProject();
				}
			}

			Logger logger = getLogger(categoryObject.getClass());
			switch (event.getPriority()) {
			case Project.MSG_ERR:
				logger.error(event.getMessage());
				break;
			case Project.MSG_WARN:
				logger.warn(event.getMessage());
				break;
			case Project.MSG_INFO:
				logger.info(event.getMessage());
				break;
			case Project.MSG_VERBOSE:
				logger.debug(event.getMessage());
				break;
			case Project.MSG_DEBUG:
				logger.debug(event.getMessage());
				break;
			default:
				logger.error(event.getMessage());
				break;
			}
		}
	}
	
	private Logger getLogger(Class clazz) {
		return Logger.getLogger(clazz.getName());
	}
}
