package googlecode.utils4ant.tasks;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class Trim extends Task {
	private String property = null;
	private String value = null;
	private boolean override = false;
	
	public void setProperty(String property) {
		this.property = property;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public void setOverride(boolean override) {
		this.override = override;
	}

	public void execute() {
		validate();
		if (override) {
			getProject().setProperty(property, value.trim());
		} else {
			getProject().setNewProperty(property, value.trim());
		}
	}
	
	private void validate() {
		if (property == null) {
			throw new BuildException("property attribute is missing");
		}
		if (value == null) {
			value = getProject().getProperty(property);
			if (value == null) {
				throw new BuildException("value attribute is not set and property " + property + " does not exist in this project");
			}
		}
	}
}
