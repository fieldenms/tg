package ua.com.fielden.platform.update;

import static java.lang.String.format;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.SwingUtilities;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import ua.com.fielden.platform.cypher.Checksum;
import ua.com.fielden.platform.utils.Pair;

/**
 * A class providing high level abstraction for an application update mechanism.
 * 
 * @author TG Team
 * 
 */
public final class Updater implements Runnable {

    private final Logger logger = Logger.getLogger(getClass());

    private final String REMOVE_FILE_NAME = "remove.txt";

    private final File dependencyLocation;
    private final File updateLocation;
    private final File backupLocation;
    private final IUpdateActionFeedback feedback;
    private final IReferenceDependancyController controller;

    public Updater(final String applicationLocation, final IReferenceDependancyController controller, final IUpdateActionFeedback feedback) throws IOException {
	this.dependencyLocation = new File(applicationLocation, "dependencies/");
	if (!dependencyLocation.exists()) {
	    throw new IOException("The location " + dependencyLocation.getAbsolutePath() + " should exist.");
	}
	if (!dependencyLocation.isDirectory()) {
	    throw new IOException("The location " + dependencyLocation.getAbsolutePath() + " should be a directory.");
	}

	backupLocation = new File(applicationLocation, "backup/");
	if (!backupLocation.exists()) {
	    if (!backupLocation.mkdirs()) {
		throw new IOException("Could not create a backup directory " + backupLocation.getAbsolutePath() + ".");
	    }
	}

	if (!backupLocation.isDirectory()) {
	    throw new IOException("The location " + backupLocation.getAbsolutePath() + " should be a directory.");
	}

	updateLocation = new File(applicationLocation, "update/");
	if (!updateLocation.exists()) {
	    if (!updateLocation.mkdirs()) {
		throw new IOException("Could not create an update directory " + updateLocation.getAbsolutePath() + ".");
	    }
	} else {
	    FileUtils.cleanDirectory(updateLocation);
	}

	if (!updateLocation.isDirectory()) {
	    throw new IOException("The location " + updateLocation.getAbsolutePath() + " should be a directory.");
	}

	this.controller = controller;
	this.feedback = feedback;
    }

    /**
     * Backups the current local dependencies before performing an update. The backup can be used to restore the state of the application in case update does not succeed.
     * 
     * @throws IOException
     */
    public void backup() throws IOException {
	logger.debug("start backup");
	FileUtils.cleanDirectory(backupLocation);
	FileUtils.copyDirectory(dependencyLocation, backupLocation);
	logger.debug("finish backup");
    }

    /**
     * Restores application dependencies from the backup.
     * 
     * @throws IOException
     */
    public void restore() throws IOException {
	logger.debug("start restore");
	FileUtils.cleanDirectory(dependencyLocation);
	FileUtils.copyDirectory(backupLocation, dependencyLocation);
	logger.debug("start restore");
    }

    /**
     * Checks and performs an update.
     */
    public void run() {
	if (SwingUtilities.isEventDispatchThread()) {
	    throw new IllegalStateException("The update process should not be executed on EDT.");
	}

	try {
	    feedback.checkDependency();
	    logger.debug("obtaning dependency info");
	    final Map<String, Pair<String, Long>> referenceDependencies = controller.dependencyInfo();
	    logger.debug("obtaining the state of local dependencies");
	    final Map<String, Pair<String, Long>> localDependencies = Checksum.sha1(dependencyLocation);
	    logger.debug("determining requried actions");
	    final Map<String, DependencyAction> actions = determineActions(localDependencies, referenceDependencies);

	    feedback.dependencyActions(actions);

	    if (actions.size() > 0) { // there is an update
		backup();

		final File removeFile = new File(updateLocation, REMOVE_FILE_NAME);
		if (!removeFile.exists()) {
		    removeFile.createNewFile();
		}
		final FileWriter removeFileWriter = new FileWriter(removeFile);

		logger.debug("processing dependency actions");
		for (final Map.Entry<String, DependencyAction> entry : actions.entrySet()) {
		    final DependencyAction action = entry.getValue();
		    final String dependencyFileName = entry.getKey();

		    logger.debug(format("processing file %s with action %s", dependencyFileName, action));

		    switch (action) {
		    case UPDATE:
			final File dependency = new File(updateLocation, dependencyFileName);
			final String dependencyChecksum = referenceDependencies.get(dependencyFileName).getKey();

			feedback.start(dependencyFileName, referenceDependencies.get(dependencyFileName).getValue(), action);
			try {
			    final byte[] updatedDependecyContent = controller.download(dependencyFileName, dependencyChecksum, feedback);

			    saveDependency(dependency, updatedDependecyContent);

			    feedback.finish(dependencyFileName, action, DependencyActionResult.SUCCESS);
			} catch (final Exception e) {
			    feedback.finish(dependencyFileName, action, DependencyActionResult.FAILURE);
			    throw e;
			}

			break;
		    case DELETE:
			feedback.start(dependencyFileName, localDependencies.get(dependencyFileName).getValue(), action);
			feedback.update(1);
			try {
			    removeFileWriter.write(dependencyFileName + "\n");
			    feedback.finish(dependencyFileName, action, DependencyActionResult.SUCCESS);
			} catch (final Exception e) {
			    feedback.finish(dependencyFileName, action, DependencyActionResult.FAILURE);
			    throw e;
			}
			break;

		    default:
			throw new UnsupportedOperationException("Action " + action + " is not supported.");
		    }
		} // for actions
		removeFileWriter.flush();
		removeFileWriter.close();

		feedback.updateCompleted("Update completed.");
	    } // there is an update

	} catch (final Exception ex) {
	    logger.error(ex);
	    try {
		restore();
	    } catch (final IOException e) {
		logger.error("Restoration did not succeed.", e);
	    }
	    feedback.updateFailed(ex);
	}
    }

    public void saveDependency(final File dependency, final byte[] updatedDependecyContent) throws IOException {
	if (dependency.exists()) {
	    dependency.delete();
	}

	if (!dependency.createNewFile()) {
	    throw new IOException("Could not create " + dependency.getAbsolutePath() + " for unknown reason.");
	}

	final FileOutputStream out = new FileOutputStream(dependency);
	out.write(updatedDependecyContent);
	out.flush();
	out.close();
    }

    /**
     * Determines what dependencies what action require in order to update the client application.
     * 
     * @param localDependencies
     * @param referenceDependencies
     * @return
     */
    public Map<String, DependencyAction> determineActions(final Map<String, Pair<String, Long>> localDependencies, final Map<String, Pair<String, Long>> referenceDependencies) {
	final Map<String, DependencyAction> result = new HashMap<String, DependencyAction>();

	// identify what local dependencies need to be updated
	for (final Map.Entry<String, Pair<String, Long>> entry : referenceDependencies.entrySet()) {
	    // check if the reference dependency exists amongst local dependencies
	    if (!localDependencies.containsKey(entry.getKey())) {
		// the local dependencies do not contain the reference dependency
		// then need to get the reference dependency
		result.put(entry.getKey(), DependencyAction.UPDATE);
	    } else {
		// the local dependencies contain the reference dependency
		// check whether SHA1 keys match
		final String localSha1 = localDependencies.get(entry.getKey()).getKey();
		if (!localSha1.equals(entry.getValue().getKey())) {
		    // if the keys don't match then need to mark the local dependency for an update with the reference one
		    result.put(entry.getKey(), DependencyAction.UPDATE);
		}
		// remove the processed item so that only at the end only non-existing in the reference dependencies local files remain
		localDependencies.remove(entry.getKey());
	    }
	}

	// one the dependencies have been processed for an update, whatever is left in the local dependencies map should be marked for deletion
	for (final String fileName : localDependencies.keySet()) {
	    result.put(fileName, DependencyAction.DELETE);
	}

	return result;
    }

    public static void main(final String[] args) {
	System.out.println(new File("src").getAbsolutePath());
    }

}
