package googlecode.utils4ant.mappers;

import org.apache.tools.ant.ProjectComponent;
import org.apache.tools.ant.util.FileNameMapper;

public class EchoMapper extends ProjectComponent implements FileNameMapper {
	public String[] mapFileName(String src) {
		getProject().log(src);
		return new String[] { src };
	}

	public void setTo(String to) {
	}

	public void setFrom(String from) {
	}
}
