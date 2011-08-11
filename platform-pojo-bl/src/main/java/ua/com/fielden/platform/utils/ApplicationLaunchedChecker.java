package ua.com.fielden.platform.utils;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.basic.config.IApplicationSettings;

import com.google.inject.Inject;

/**
 * A utility to lock the specified file unit JVM shutdown.
 *
 * @author TG Team
 *
 */

public final class ApplicationLaunchedChecker {

    private static final Logger logger = Logger.getLogger(ApplicationLaunchedChecker.class);

    private final File lockFile;
    private FileChannel channelForLockFile;
    private FileLock lock;

    private final static String APP_SESSION_FILE_NAME = ".session.tmp";

    @Inject
    public ApplicationLaunchedChecker(final IApplicationSettings settings) {
	lockFile = new File(settings.appHome() + File.separator + APP_SESSION_FILE_NAME);
    }

    /**
     * Tries to acquire a lock on a designated file. If successful then there is no other instances of the application running -- returns false.
     * The acquired lock remains until a corresponding JVM is running to prevent launching of new application instances.
     * <p>
     * If the lock could not be acquired then there is another running application instance -- returns true.
     * <p>
     * The implemented method based on file locking is safe as the lock is removed even if the JVM crushes.
     *
     * @return
     */
    public boolean isAnotherRunning() {
	try {
	    logger.debug("Creating a channel for lockFile " + lockFile.getAbsolutePath());
	    channelForLockFile = new RandomAccessFile(lockFile, "rw").getChannel();

	    try {
		logger.debug("Trying to lock " + lockFile.getAbsolutePath());
		lock = channelForLockFile.tryLock();
		logger.debug("Locked " + lockFile.getAbsolutePath());
	    } catch (final OverlappingFileLockException e) {
		// already locked
		unlock();
		return true;
	    }

	    if (lock == null) {
		unlock();
		return true;
	    }

	    // destroy the lock when the JVM is closing
	    Runtime.getRuntime().addShutdownHook(new Thread() {
		public void run() {
		    logger.debug("Running the shutdown hook to unlock and delete " + lockFile.getAbsolutePath());
		    unlock();
		    deleteLockFile();
		}
	    });
	    return false;
	} catch (final Exception e) {
	    unlock();
	    return true;
	}
    }

    /**
     * Unlocks the lock file and closes the associated channel.
     */
    private void unlock() {
	try {
	    logger.debug("Unlocking " + lockFile.getAbsolutePath());
	    if (lock != null) {
		lock.release();
	    }
	    logger.debug("Unlocked " + lockFile.getAbsolutePath());
	} catch (final Exception e) {
	    logger.warn("Could not unlock " + lockFile.getAbsolutePath(), e);
	}
	try {
	    logger.debug("Closing channel for " + lockFile.getAbsolutePath());
	    if (channelForLockFile != null) {
		channelForLockFile.close();
	    }
	    logger.debug("Closed channel for " + lockFile.getAbsolutePath());
	} catch (final Exception e) {
	    logger.warn("Could not close channel for " + lockFile.getAbsolutePath(), e);
	}
    }

    /**
     * Deletes the lock file.
     */
    private void deleteLockFile() {
	try {
	    logger.debug("Deleting " + lockFile.getAbsolutePath());
	    lockFile.delete();
	    logger.debug("Delete " + lockFile.getAbsolutePath());
	} catch (final Exception e) {
	    logger.warn("Could not delete " + lockFile.getAbsolutePath(), e);
	}
    }
}
