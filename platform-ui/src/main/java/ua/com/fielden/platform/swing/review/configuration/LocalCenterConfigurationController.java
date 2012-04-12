package ua.com.fielden.platform.swing.review.configuration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.ui.config.api.interaction.ICenterConfigurationController;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class LocalCenterConfigurationController extends LocalConfigurationController implements ICenterConfigurationController {

    @Inject
    public LocalCenterConfigurationController(final @Named("reports.path") String reportPath, final EntityFactory entityFactory) {
	super(reportPath, entityFactory);
    }

    @Override
    public String generateKeyForPrincipleCenter(final Class<?> forType) {
	return getReportPath() + System.getProperty("file.separator") + forType.getSimpleName() + ".dcf";
    }

    @Override
    public String generateKeyForNonPrincipleCenter(final String principleCenterKey, final String nonPrincipleCenterName) {
	return principleCenterKey.substring(0, principleCenterKey.lastIndexOf(".dcf")) + System.getProperty("file.separator") + nonPrincipleCenterName + ".dcf";
    }

    @Override
    public Result canAddAnalysis(final String key) {
	return Result.successful(this);
    }

    @Override
    public Result canRemoveAnalysis(final String key) {
	return Result.successful(this);
    }

    @Override
    public void removeConfiguration(final String key) {
	final File configurationFile = new File(key);
	configurationFile.delete();
    }

    @Override
    public Result canRemove(final String centerKey) {
	return Result.successful(this);
    }

    @Override
    public Result canConfigureAnalysis(final String centerKey) {
	return Result.successful(this);
    }

    @Override
    public Result canSave(final String locatorKey) {
	return Result.successful(this);
    }

    @Override
    public Result canConfigure(final String locatorKey) {
	return Result.successful(this);
    }

    @Override
    public List<String> getNonPrincipleCenters(final String principleCenterKey) {
	final File reportDirectory = new File(principleCenterKey.replace(".dcf", System.getProperty("file.separator")));
	final List<String> centerList = new ArrayList<String>();
	if (!reportDirectory.exists()) {
	    return centerList;
	}
	final File files[] = reportDirectory.listFiles();
	for (final File file : files) {
	    if (!file.isDirectory() && file.getName().endsWith(".dcf")) {
		centerList.add(file.getName().substring(0, file.getName().lastIndexOf(".dcf")));
	    }
	}
	return centerList;
    }

    @Override
    public boolean isNonPrincipleCenterNameValid(final String principleCenterKey, final String nonPrincipleCenterName) {
	try {
	    final File file = new File(nonPrincipleCenterName);
	    if (file.createNewFile()) {
		file.delete();
	    } else {
		return false;
	    }
	} catch (final IOException e) {
	    return false;
	}
	return true;
    }
}
