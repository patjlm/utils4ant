package googlecode.utils4ant.launcher;

import java.io.File;

public class AntLauncherMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		AntLauncher launcher = new AntLauncher();
		String baseDirString = "D:/ETV_F/releasing/dev/eclipse_workspaces/temp/ant-log4j-test";
		System.setProperty("base.dir", baseDirString);
		try {
			launcher.launch(new File(baseDirString),
					new File(baseDirString + "/build.xml"),
					"test",
					null,
					new File(baseDirString + "/lib/log4j.properties"));
		} catch (AntLauncherException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
