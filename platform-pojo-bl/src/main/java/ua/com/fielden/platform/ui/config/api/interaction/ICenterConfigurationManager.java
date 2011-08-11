package ua.com.fielden.platform.ui.config.api.interaction;

import ua.com.fielden.platform.error.Result;

public interface ICenterConfigurationManager extends IConfigurationManager {

    Result canAddAnalysis(String key);

    Result canRemoveAnalysis(String key);
}
