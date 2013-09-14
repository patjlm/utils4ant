package googlecode.utils4ant.filters;

import java.util.ArrayList;

import org.apache.tools.ant.filters.TokenFilter.ChainableReaderFilter;

public class ConditionFilter extends ChainableReaderFilter {

	private ArrayList<ConditionContext> contextList = new ArrayList<ConditionFilter.ConditionContext>();

	private String ifPattern = "^[ 	]*##if (.+)[ 	]*$";
	private String elseIfPattern = "^[ 	]*##elseif (.+)[ 	]*$";
	private String elsePattern = "^[ 	]*##else[ 	]*$";
	private String endIfPattern = "^[ 	]*##endif[ 	]*$";

	public void setIfPattern(String ifPattern) {
		this.ifPattern = ifPattern;
	}

	public void setElseIfPattern(String elseIfPattern) {
		this.elseIfPattern = elseIfPattern;
	}

	public void setElsePattern(String elsePattern) {
		this.elsePattern = elsePattern;
	}

	public void setEndIfPattern(String endIfPattern) {
		this.endIfPattern = endIfPattern;
	}

	public String filter(String str) {
		ConditionContext ctx = ctx();
		if (ctx == null) {
			// no current context
			if (str.matches(ifPattern)) {
				//entering first context
				enterNewContext(str, ifPattern);
			} else {
				return str;
			}
		} else if (str.matches(endIfPattern)) {
			// exit current condition context, ie remove context
			contextList.remove(contextList.size() - 1);
		} else if (ctx.match) {
			// in context an in match section
			if (str.matches(ifPattern)) {
				// if: enter embedded context
				enterNewContext(str, ifPattern);
			} else if (str.matches(elseIfPattern) || str.matches(elsePattern)) {
				// elseif or else: exiting match section
				ctx.match = false;
			} else {
				return str;
			}
		} else if (!ctx.matchDone) {
			// !ctx.match && !ctx.matchDone
			if (str.matches(elsePattern) || ctx.conditionMet(str, elseIfPattern)) {
				// else: enter match section
				// matching elseif: enter match section
				ctx.setMatch(true);
			}
		}
		// cases for null return:
		// first if (ctx == null && if)
		// endif (ctx != null && endif)
		// ctx.match && entering new context (if)
		// ctx.match && exiting match section (elseif or else)
		// !ctx.match && ctx.matchDone
		// !ctx.match && !ctx.matchDone && entering else, elseif, or nothing
		return null;
	}

	private void enterNewContext(String str, String pattern) {
		ConditionContext ctx = new ConditionContext();
		ctx.conditionMet(str, ifPattern);
		contextList.add(ctx);
	}

	private ConditionContext ctx() {
		if (contextList.isEmpty()) {
			return null;
		}
		return contextList.get(contextList.size() - 1);
	}

	private class ConditionContext {
		// are we in a if match section in this context
		private boolean match = false;
		// did we already successfully match a condition in this context
		private boolean matchDone = false;
		
		public boolean setMatch(boolean b) {
			match = b;
			matchDone = matchDone || match;
			return match;
		}

		public boolean conditionMet(String str, String pattern) {
			if (str.matches(pattern)) {
				String propValue = getProject().getProperty(str.replaceAll(pattern, "$1"));
				return setMatch(Boolean.valueOf(propValue));
			}
			return false;
		}
	}

}
