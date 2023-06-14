package ua.com.fielden.platform.web.test.server;

import static java.lang.String.format;
import static ua.com.fielden.platform.web.action.StandardMastersWebUiConfig.MASTER_ACTION_SPECIFICATION;

import java.util.Optional;

import com.google.inject.Injector;

import ua.com.fielden.platform.sample.domain.MoreDataForDeleteEntity;
import ua.com.fielden.platform.web.app.config.IWebUiBuilder;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.view.master.EntityMaster;
import ua.com.fielden.platform.web.view.master.api.IMaster;
import ua.com.fielden.platform.web.view.master.api.actions.MasterActions;
import ua.com.fielden.platform.web.view.master.api.impl.SimpleMasterBuilder;

public class MoreDataForDeleteEntityWebUiConfig {


    public static MoreDataForDeleteEntityWebUiConfig register(final Injector injector, final IWebUiBuilder builder) {
        return new MoreDataForDeleteEntityWebUiConfig(injector, builder);
    }

    private MoreDataForDeleteEntityWebUiConfig(final Injector injector, final IWebUiBuilder builder) {
        builder.register(createMoreDataMaster(injector));
    }


    public static EntityMaster<MoreDataForDeleteEntity> createMoreDataMaster(final Injector injector) {

        final String bottomButtonPanel = "['horizontal', 'padding: 20px', 'justify-content: center', 'wrap', [%s], [%s]]";
        final String actionButton = MASTER_ACTION_SPECIFICATION;
        final IMaster<MoreDataForDeleteEntity> masterConfig = new SimpleMasterBuilder<MoreDataForDeleteEntity>()
                .forEntity(MoreDataForDeleteEntity.class)
                .addProp("number").asInteger()
                .also()
                .addAction(MasterActions.REFRESH)
                /*      */.shortDesc("CANCEL")
                /*      */.longDesc("Cancel action")
                .addAction(MasterActions.SAVE)
                /*      */.shortDesc("Accept")
                /*      */.longDesc("Accept data")
                .setActionBarLayoutFor(Device.DESKTOP, Optional.empty(), format(bottomButtonPanel, actionButton, actionButton))
                .setLayoutFor(Device.DESKTOP, Optional.empty(), (
                        " ['padding:20px', "
                        + " [['flex']]]"))
                .done();
       return new EntityMaster<>(
                MoreDataForDeleteEntity.class,
                masterConfig,
                injector);
    }
}
