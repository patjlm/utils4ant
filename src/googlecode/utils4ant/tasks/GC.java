package googlecode.utils4ant.tasks;

import org.apache.tools.ant.Task;

public class GC extends Task {
	public void execute() {
//		MemoryMXBean mem = ManagementFactory.getMemoryMXBean();
//		log("before gc - heap: " + mem.getHeapMemoryUsage().getUsed()
//				+ " bytes / permgen: " + mem.getNonHeapMemoryUsage().getUsed()
//				+ " bytes");
		System.gc();
//		log("after  gc - heap: " + mem.getHeapMemoryUsage().getUsed()
//				+ " bytes / permgen: " + mem.getNonHeapMemoryUsage().getUsed()
//				+ " bytes");
	}
}
