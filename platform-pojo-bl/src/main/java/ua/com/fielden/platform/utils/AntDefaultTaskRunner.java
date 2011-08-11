package ua.com.fielden.platform.utils;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

/**
 * A utility class to execute Ant default task in the provided script.
 * <p>
 * The original purpose for this utility was the need to launch java applications by invoking a java execution task configured in an Ant script.
 * 
 * @author TG Team
 * 
 */
public class AntDefaultTaskRunner {

    private AntDefaultTaskRunner() {
    }

    public static void run(final String antScriptFileName) {
	final File buildFile = new File(antScriptFileName);
	final Project p = new Project();
	p.setUserProperty("ant.file", buildFile.getAbsolutePath());
	p.setProperty("name", "TG Fleet");

	final DefaultLogger consoleLogger = new DefaultLogger();
	consoleLogger.setErrorPrintStream(System.err);
	consoleLogger.setOutputPrintStream(System.out);
	consoleLogger.setMessageOutputLevel(Project.MSG_INFO);
	p.addBuildListener(consoleLogger);

	try {
	    p.fireBuildStarted();
	    p.init();
	    final ProjectHelper helper = ProjectHelper.getProjectHelper();
	    p.addReference("ant.projectHelper", helper);
	    helper.parse(p, buildFile);
	    p.executeTarget(p.getDefaultTarget());
	    p.fireBuildFinished(null);
	} catch (final BuildException e) {
	    p.fireBuildFinished(e);
	}
    }
}
