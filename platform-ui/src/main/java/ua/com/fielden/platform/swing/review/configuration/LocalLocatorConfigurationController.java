package ua.com.fielden.platform.swing.review.configuration;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.ui.config.api.interaction.ILocatorConfigurationController;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class LocalLocatorConfigurationController extends LocalConfigurationController implements ILocatorConfigurationController {

    @Inject
    public LocalLocatorConfigurationController(final @Named("reports.path") String reportPath, final EntityFactory entityFactory) {
	super(reportPath, entityFactory);
    }

    @Override
    public String generateKeyForAutocompleterConfiguration(final Class<?> entityType, final String propertyName) {
	return entityType.getName() + "." + propertyName;
    }

    @Override
    public String generateKeyForDefaultAutocompleterConfiguration(final Class<?> forType) {
	return getReportPath() + System.getProperty("file.separator") + "global" + System.getProperty("file.separator") + forType.getName().replace('.', '/') + ".dcf";
    }

    @Override
    public void removeConfiguration(final String key) {
	throw new UnsupportedOperationException("The locator configuration can not be removed.");
    }

    @Override
    public Result canRemove(final String centerKey) {
	return new Result(new UnsupportedOperationException("The locator configuration can not be removed."));
    }

    @Override
    public Result canConfigureAnalysis(final String centerKey) {
	throw new UnsupportedOperationException("The locator's analysis can not be configured");
    }

    @Override
    public Result canSave(final String locatorKey) {
	return Result.successful(this);
    }

    @Override
    public Result canConfigure(final String locatorKey) {
	return Result.successful(this);
    }

}
