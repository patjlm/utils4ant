package googlecode.utils4ant.filters;

import org.apache.tools.ant.filters.TokenFilter.ChainableReaderFilter;

public class EchoFilter extends ChainableReaderFilter {

	public String filter(String string) {
		getProject().log(string);
		return string;
	}

}
