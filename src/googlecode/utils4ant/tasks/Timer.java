package googlecode.utils4ant.tasks;

import org.apache.tools.ant.taskdefs.Sequential;

public class Timer extends Sequential {
	private String property = null;
	private boolean override = false;

	public void setProperty(String property) {
		this.property = property;
	}

	public void setOverride(boolean override) {
		this.override = override;
	}

	public void execute() {
		long startTime = System.currentTimeMillis();
		super.execute();
		long duration = startTime - System.currentTimeMillis();
		if (property != null) {
			if (override || getProject().getProperty(property) == null) {
				getProject().setProperty(property, String.valueOf(duration));
			}
		}
	}

}
