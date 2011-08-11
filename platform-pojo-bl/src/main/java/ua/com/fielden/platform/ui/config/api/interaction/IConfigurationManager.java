package ua.com.fielden.platform.ui.config.api.interaction;

import ua.com.fielden.platform.error.Result;

public interface IConfigurationManager extends ILocatorConfigurationManager {

    Result canRemove(String centerKey);

    Result canConfigureAnalysis(String centerKey);

}
