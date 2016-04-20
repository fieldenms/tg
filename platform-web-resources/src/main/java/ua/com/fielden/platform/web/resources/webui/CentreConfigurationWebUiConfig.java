package ua.com.fielden.platform.web.resources.webui;

import static java.lang.String.format;
import static ua.com.fielden.platform.web.centre.api.actions.impl.EntityActionBuilder.action;
import static ua.com.fielden.platform.web.centre.api.context.impl.EntityCentreContextSelector.context;

import java.util.Optional;

import com.google.inject.Injector;

import ua.com.fielden.platform.security.user.UserRole;
import ua.com.fielden.platform.web.centre.CentreConfigUpdater;
import ua.com.fielden.platform.web.centre.CentreConfigUpdaterProducer;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.view.master.EntityMaster;
import ua.com.fielden.platform.web.view.master.api.IMaster;
import ua.com.fielden.platform.web.view.master.api.actions.MasterActions;
import ua.com.fielden.platform.web.view.master.api.actions.post.IPostAction;
import ua.com.fielden.platform.web.view.master.api.impl.SimpleMasterBuilder;

/**
 * {@link UserRole} Web UI configuration.
 *
 * @author TG Team
 *
 */
public class CentreConfigurationWebUiConfig {
    private static final String actionButton = "'margin: 10px', 'width: 110px'";
    private static final String bottomButtonPanel = "['horizontal', 'margin-top: 20px', 'justify-content: center', 'wrap', [%s], [%s]]";
    
    public final EntityMaster<CentreConfigUpdater> centreConfigUpdater;

    public CentreConfigurationWebUiConfig(final Injector injector) {
        centreConfigUpdater = createCentreConfigUpdater(injector);
    }

    /**
     * Creates entity master for {@link CentreConfigUpdater}.
     *
     * @return
     */
    private static EntityMaster<CentreConfigUpdater> createCentreConfigUpdater(final Injector injector) {
        final IMaster<CentreConfigUpdater> masterConfig = new SimpleMasterBuilder<CentreConfigUpdater>()
                .forEntity(CentreConfigUpdater.class)
                .addProp("sortingProperties").asCollectionalEditor().maxVisibleRows(5).withHeader("title")
                .also()
                .addAction(MasterActions.REFRESH).shortDesc("CANCEL").longDesc("Cancel action")
                .addAction(MasterActions.SAVE).shortDesc("SORT").longDesc("Sorting action")

                .setLayoutFor(Device.DESKTOP, Optional.empty(), (
                        "      ['padding:20px', 'width:500px', "
                        + format("['flex', ['flex']],")
                        + format(bottomButtonPanel, actionButton, actionButton)
                        + "    ]"))
                .done();
        return new EntityMaster<CentreConfigUpdater>(
                CentreConfigUpdater.class,
                CentreConfigUpdaterProducer.class,
                masterConfig,
                injector);
    }
    
    public static enum CentreConfigActions {
        SORT_ACTION {
            @Override
            public EntityActionConfig mkAction() {
                return action(CentreConfigUpdater.class)
                        .withContext(context().withSelectionCrit().build())
                        .postActionSuccess(new IPostAction() {
                            @Override
                            public JsCode build() {
                                return new JsCode("   return self.retrieve().then(function () { self.run(); }); \n");
                            }
                        })
                        .icon("av:playlist-add-check")
                        .shortDesc("Change Sorting")
                        .longDesc("Change sorting properties for this centre.")
                        .withNoParentCentreRefresh()
                        .build();
            }
            
        };

        public abstract EntityActionConfig mkAction();
    }
}
