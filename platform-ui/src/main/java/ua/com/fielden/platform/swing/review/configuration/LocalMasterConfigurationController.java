package ua.com.fielden.platform.swing.review.configuration;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.ui.config.api.interaction.IMasterConfigurationController;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class LocalMasterConfigurationController extends LocalConfigurationController implements IMasterConfigurationController {

    @Inject
    public LocalMasterConfigurationController(final @Named("reports.path") String reportPath, final EntityFactory entityFactory) {
	super(reportPath, entityFactory);
    }

    @Override
    public String generateKeyForMasterConfiguration(final Class<?> forType) {
	return getReportPath() + System.getProperty("file.separator") + forType.getName().replace('.', '/') + ".dcf";
    }

    @Override
    public void removeConfiguration(final String key) {
	throw new UnsupportedOperationException("The master configuration can not be removed.");
    }

    @Override
    public Result canRemove(final String centerKey) {
	return new Result(new UnsupportedOperationException("The master configuration can not be removed."));
    }

    @Override
    public Result canConfigureAnalysis(final String centerKey) {
	throw new UnsupportedOperationException("The masters's analysis can not be configured");
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
