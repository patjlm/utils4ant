package googlecode.utils4ant.filters;

import org.apache.tools.ant.filters.TokenFilter.ChainableReaderFilter;
import org.apache.tools.ant.types.PatternSet;
import org.apache.tools.ant.types.selectors.SelectorUtils;

public class PatternFilter extends ChainableReaderFilter {
	private PatternSet patternSet;
	private boolean caseSensitive = false;
	private String basedir = null;

	public String getBasedir() {
		return basedir;
	}

	public void setBasedir(String basedir) {
		this.basedir = basedir;
	}

	public void addConfiguredPatternSet(PatternSet patternSet) {
		this.patternSet = patternSet;
		if (this.patternSet.getIncludePatterns(getProject()).length == 0) {
			patternSet.setIncludes("**");
		}
	}

	public String filter(String string) {
		String[] includes = patternSet.getIncludePatterns(getProject());
		String[] excludes = patternSet.getExcludePatterns(getProject());
		for (int i = 0; i < includes.length; i++) {
			if (matchPath(includes[i], string)) {
				boolean match = true;
				if (excludes != null) {
					for (int j = 0; j < excludes.length; j++) {
						if (matchPath(excludes[j], string)) {
							match = false;
							break;
						}
					}
				}
				if (match) {
					return string;
				}
			}
		}
		return null;
	}

	private String modify(String str) {
		return str.replace('\\', '/');
	}

	private boolean matchPath(String pattern, String string) {
		String path = (basedir == null) ? string : basedir + "/" + string;
		return SelectorUtils.matchPath(modify(pattern), modify(path),
				caseSensitive);
	}
}
