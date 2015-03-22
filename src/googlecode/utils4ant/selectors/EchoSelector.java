package googlecode.utils4ant.selectors;

import org.apache.tools.ant.types.DataType;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.resources.selectors.ResourceSelector;

/**
 * Will just log the name of the resource trying to be selected and select it.
 * 
 * @since Ant 1.7
 */
public class EchoSelector extends DataType implements ResourceSelector {

	private String prefix = null;

	public EchoSelector() {
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public boolean isSelected(Resource r) {
		getProject().log(((prefix == null) ? "" : prefix + " ") + r.getName());
		return true;
	}
}
