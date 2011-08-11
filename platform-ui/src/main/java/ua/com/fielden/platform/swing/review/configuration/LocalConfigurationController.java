package ua.com.fielden.platform.swing.review.configuration;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.serialisation.ClientSerialiser;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.ui.config.api.interaction.IConfigurationController;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public abstract class LocalConfigurationController implements IConfigurationController {

    private final ISerialiser serialiser;
    private final String reportPath;

    private final Logger logger;

    @Inject
    public LocalConfigurationController(final @Named("reports.path") String reportPath, final EntityFactory entityFactory) {

	this.logger = Logger.getLogger(this.getClass());

	this.serialiser = new ClientSerialiser(entityFactory);
	this.reportPath = reportPath.startsWith("~") ? System.getProperty("user.home") + reportPath.substring(1) : reportPath;
    }

    @Override
    public void save(final String key, final byte[] objectToSave) {
	BufferedOutputStream bos = null;

	try {
	    //creating directories for the file if they doesn't exist.
	    createDirsIfNeeded(key);

	    //create an object of FileOutputStream
	    final FileOutputStream fos = new FileOutputStream(new File(key));

	    //create an object of BufferedOutputStream
	    bos = new BufferedOutputStream(fos);

	    bos.write(objectToSave);

	} catch (final FileNotFoundException fnfe) {
	    logger.debug(fnfe.getMessage());
	    System.out.println("Specified " + key + " wasn't found");
	} catch (final IOException ioe) {
	    logger.debug(ioe.getMessage());
	    System.out.println("Error while writing file " + ioe);
	} finally {
	    if (bos != null) {
		try {

		    //flush the BufferedOutputStream
		    bos.flush();

		    //close the BufferedOutputStream
		    bos.close();

		} catch (final Exception e) {
		}
	    }
	}

    }

    private void createDirsIfNeeded(final String key) {
	final File file = new File(key.substring(0, key.lastIndexOf(System.getProperty("file.separator"))));
	if (!file.exists()) {
	    file.mkdirs();
	}
    }

    @Override
    public byte[] load(final String key) {
	final File file = new File(key);

	try {
	    //create FileInputStream object
	    final FileInputStream fin = new FileInputStream(file);

	    /*
	     * Create byte array large enough to hold the content of the file.
	     * Use File.length to determine size of the file in bytes.
	     */

	    final byte fileContent[] = new byte[(int) file.length()];

	    fin.read(fileContent);

	    return fileContent;
	} catch (final FileNotFoundException e) {
	    logger.debug(e.getMessage());
	    System.out.println("File not found " + e);
	} catch (final IOException ioe) {
	    logger.debug(ioe.getMessage());
	    System.out.println("Exception while reading the file " + ioe);
	}
	return null;
    }

    @Override
    public ISerialiser getSerialiser() {
	return serialiser;
    }

    @Override
    public boolean exists(final String key) {
	final File configurationFile = new File(key);
	return configurationFile.exists();
    }

    public String getReportPath() {
	return reportPath;
    }

}
