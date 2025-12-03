package ua.com.fielden.platform.web.resources.test;

import org.apache.commons.lang3.StringUtils;
import ua.com.fielden.platform.basic.config.Workflows;
import ua.com.fielden.platform.web.app.config.IWebUiBuilder;
import ua.com.fielden.platform.web.resources.webui.AbstractWebUiConfig;
import ua.com.fielden.platform.web.resources.webui.test_entities.Action1;
import ua.com.fielden.platform.web.resources.webui.test_entities.Action2;

import java.util.Optional;
import java.util.Properties;

import static ua.com.fielden.platform.web.view.master.EntityMaster.noUiFunctionalMaster;

/// Web UI configuration for web resource tests.
///
/// @see WebResourcesTestRunner
///
class WebUiConfig extends AbstractWebUiConfig {

    private final String domainName;
    private final String path;
    private final int port;

    public WebUiConfig(final Properties props) {
        super("TG Test Application",
              Workflows.valueOf(props.getProperty("workflow")),
              new String[0],
              Boolean.valueOf(props.getProperty("independent.time.zone")),
              Optional.empty(),
              Optional.of("https://www.google.com"));
        if (StringUtils.isEmpty(props.getProperty("web.domain")) || StringUtils.isEmpty(props.getProperty("web.path"))) {
            throw new IllegalArgumentException("Both the domain name and application binding path should be specified.");
        }
        this.domainName = props.getProperty("web.domain");
        this.path = props.getProperty("web.path");
        this.port = Integer.valueOf(props.getProperty("port"));
    }

    @Override
    public String getDomainName() {
        return domainName;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public void initConfiguration() {
        super.initConfiguration();

        final IWebUiBuilder builder = configApp();

        builder.register(noUiFunctionalMaster(Action1.class, injector()));
        builder.register(noUiFunctionalMaster(Action2.class, injector()));
    }

    @Override
    public int getPort() {
        return port;
    }

}
