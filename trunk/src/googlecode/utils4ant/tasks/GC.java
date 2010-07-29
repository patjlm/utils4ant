package googlecode.utils4ant.tasks;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.util.Iterator;

import org.apache.tools.ant.Task;

public class GC extends Task {
	public void execute() {
		printMemoryStatus("Before GC:");
		System.gc();
		printMemoryStatus("After GC:");
	}

	private void printMemoryStatus(String header) {
		log(header);
		Iterator iter = ManagementFactory.getMemoryPoolMXBeans().iterator();
		while (iter.hasNext()) {
			MemoryPoolMXBean item = (MemoryPoolMXBean) iter.next();
			log("  " + item.getName() + " (" + item.getType()+ "): " + item.getUsage());
		}
	}
}
