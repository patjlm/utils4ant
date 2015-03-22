package googlecode.utils4ant.mappers;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.apache.tools.ant.util.FileNameMapper;

public class LoopReplaceMapper implements FileNameMapper {
	private String from;
	private ArrayList list;

	public void setFrom(String from) {
		this.from = from;
	}

	public void setToList(String _list) {
		StringTokenizer st = new StringTokenizer(_list, "       , ");
		this.list = new ArrayList(st.countTokens());
		while (st.hasMoreTokens()) {
			list.add(st.nextToken());
		}
	}

	public String[] mapFileName(String src) {
		String[] res = null;
		if (src.contains(from)) {
			res = new String[list.size()];
			for (int index = 0; index < list.size(); index++) {
				res[index] = src.replace(from, (String) list.get(index));
			}
		} else {
			res = new String[1];
			res[0] = src;
		}
		return res;
	}

	public void setTo(String to) {
	}
}
