package ua.com.fielden.platform.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

/**
 * A class which provides startup and update logic for TG-based web client applications. The main method accepts two parameters:
 * <ul>
 * <li><b>First parameter</b> -- the path to a Log4J XML configuration file.
 * <li><b>Second parameter</b> -- the path to an ANT execution script.
 * </ul>
 * 
 * @author TG Team
 * 
 */
public class Bootstrap {

    private final Logger logger = Logger.getLogger(getClass());
    private final String REMOVE_FILE_NAME = "remove.txt";

    private final File dependencyLocation;
    private final File updateLocation;

    public Bootstrap(final String applicationLocation) throws Exception {
        logger.info("Checking required locations that should exist in " + applicationLocation);
        try {
            this.dependencyLocation = new File(applicationLocation, "dependencies/");
            if (!dependencyLocation.exists()) {
                throw new IOException("The location " + dependencyLocation.getAbsolutePath() + " should exist.");
            }
            if (!dependencyLocation.isDirectory()) {
                throw new IOException("The location " + dependencyLocation.getAbsolutePath() + " should be a directory.");
            }

            updateLocation = new File(applicationLocation, "update/");
            if (!updateLocation.exists()) {
                if (!updateLocation.mkdirs()) {
                    throw new IOException("Could not create an update directory " + updateLocation.getAbsolutePath() + ".");
                }
            }
            if (!updateLocation.isDirectory()) {
                throw new IOException("The location " + updateLocation.getAbsolutePath() + " should be a directory.");
            }
        } catch (final Exception ex) {
            logger.error(ex);
            throw ex;
        }
    }

    public void update() throws Exception {
        try {
            // copy all files from update location to dependency location except "remove.txt"
            logger.info("Copying all files from " + updateLocation.getAbsolutePath() + " to " + dependencyLocation.getAbsolutePath() + "...");
            FileUtils.copyDirectory(updateLocation, dependencyLocation, new FileFilter() {
                @Override
                public boolean accept(final File file) {
                    return !REMOVE_FILE_NAME.equalsIgnoreCase(file.getName());
                }

            });
            // read the info what files should be removed from the dependency location and remove them
            final File removeFile = new File(updateLocation, REMOVE_FILE_NAME);
            if (removeFile.exists()) {
                logger.info("Reading information from file " + removeFile.getAbsolutePath() + " to remove unnecessary files");
                final BufferedReader reader = new BufferedReader(new FileReader(removeFile));
                try {
                    String fileNameToDelete = null;
                    while (!StringUtils.isEmpty(fileNameToDelete = reader.readLine())) {
                        final File fileToRemove = new File(dependencyLocation, fileNameToDelete);
                        logger.info("Checking " + fileToRemove);
                        if (fileToRemove.exists() && fileToRemove.isFile()) {
                            logger.info("Deleting " + fileToRemove.getAbsolutePath());
                            fileToRemove.delete();
                        }
                    }
                } finally {
                    if (reader != null) {
                        reader.close();
                    }
                }
            }
            // at last clean the update location
            logger.info("Cleaning directory " + updateLocation.getAbsolutePath());
            FileUtils.cleanDirectory(updateLocation);
        } catch (final Exception ex) {
            ex.printStackTrace();
            logger.error(ex);
        }
    }

    public void run(final String antScriptFileName) {
        logger.info("Preparing to launch the client application");
        final File buildFile = new File(antScriptFileName);
        final Project p = new Project();
        p.setUserProperty("ant.file", buildFile.getAbsolutePath());

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
            logger.info("Launching the client application");
            p.executeTarget(p.getDefaultTarget());
            p.fireBuildFinished(null);
        } catch (final BuildException e) {
            p.fireBuildFinished(e);
            logger.error(e);
        }
    }

    public static void main(final String[] args) throws Exception {
        /////////////////// validate the input parameters /////////////////////
        if (args.length != 2) {
            throw new IllegalArgumentException("Two parameters are expected: the path to log4j.xml and path to app-launcher.xml");
        }

        final File log4j = new File(args[0]);
        if (!log4j.exists()) {
            throw new IOException("Log4j configuration file " + log4j.getAbsolutePath() + " does not exist.");
        }
        final File antFile = new File(args[1]);
        if (!antFile.exists()) {
            throw new IOException("Ant configuration file " + antFile.getAbsolutePath() + " does not exist.");
        }

        ////////////////// start the bootstrap process ///////////////////////
        DOMConfigurator.configure(log4j.getAbsolutePath());
        final Bootstrap bootstarp = new Bootstrap(System.getProperty("user.dir"));
        bootstarp.update();
        bootstarp.run(antFile.getAbsolutePath());
    }
}
