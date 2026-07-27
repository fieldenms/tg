package ua.com.fielden.platform.web.resources.test;

import org.apache.commons.lang3.StringUtils;
import ua.com.fielden.platform.basic.config.Workflows;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import ua.com.fielden.platform.web.app.config.IWebUiBuilder;
import ua.com.fielden.platform.web.resources.webui.AbstractWebUiConfig;
import ua.com.fielden.platform.web.resources.webui.test_entities.Action1;
import ua.com.fielden.platform.web.resources.webui.test_entities.Action2;
import ua.com.fielden.platform.web.resources.webui.test_entities.Action3;
import ua.com.fielden.platform.web.resources.webui.test_entities.Action3Producer;
import ua.com.fielden.platform.web.view.master.EntityMaster;
import ua.com.fielden.platform.web.view.master.api.actions.MasterActions;
import ua.com.fielden.platform.web.view.master.api.impl.SimpleMasterBuilder;

import java.util.Optional;
import java.util.Properties;

import static ua.com.fielden.platform.web.centre.api.actions.impl.EntityActionBuilder.action;
import static ua.com.fielden.platform.web.centre.api.context.impl.EntityCentreContextSelector.context;
import static ua.com.fielden.platform.web.interfaces.ILayout.Device.DESKTOP;
import static ua.com.fielden.platform.web.layout.api.impl.LayoutBuilder.cell;
import static ua.com.fielden.platform.web.layout.api.impl.LayoutComposer.mkActionLayoutForMaster;
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
        builder.register(noUiFunctionalMaster(Action3.class, Action3Producer.class, injector()));
        builder.registerExtraAction(action(Action3.class)
                                    .withTinyHyperlink(Action3.ACTION_ID_ACTION3)
                                    .withContext(context()
                                                 .withSelectedEntities()
                                                 .withComputation((_, _) -> Action3.COMPUTED_STRING_VALUE)
                                                 .build())
                                    .build());

        builder.register(createEmptyMaster(TgVehicleModel.class));
    }

    @Override
    public int getPort() {
        return port;
    }

    private <E extends AbstractEntity<?>> EntityMaster<E> createEmptyMaster(final Class<E> entityType) {
        return new EntityMaster<>(
                entityType,
                new SimpleMasterBuilder<E>().forEntity(entityType)
                        .addAction(MasterActions.SAVE)
                        .setActionBarLayoutFor(DESKTOP, Optional.empty(), mkActionLayoutForMaster())
                        .setLayoutFor(DESKTOP, Optional.empty(), cell().toString())
                        .done(),
                injector());
    }

}
