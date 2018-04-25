package ua.com.fielden.platform.web.resources.webui;

import static ua.com.fielden.platform.web.PrefDim.mkDim;
import static ua.com.fielden.platform.web.centre.api.actions.impl.EntityActionBuilder.action;
import static ua.com.fielden.platform.web.centre.api.context.impl.EntityCentreContextSelector.context;

import com.google.inject.Injector;

import ua.com.fielden.platform.entity.SecurityMatrixInsertionPoint;
import ua.com.fielden.platform.entity.SecurityMatrixSaveAction;
import ua.com.fielden.platform.security.user.UserRole;
import ua.com.fielden.platform.ui.menu.security.MiSecurityMatrix;
import ua.com.fielden.platform.web.app.config.IWebUiBuilder;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig;
import ua.com.fielden.platform.web.centre.api.impl.EntityCentreBuilder;
import ua.com.fielden.platform.web.centre.api.insertion_points.InsertionPoints;
import ua.com.fielden.platform.web.security.SecurityMatrixInsertionPointMaster;
import ua.com.fielden.platform.web.view.master.EntityMaster;
import ua.com.fielden.platform.web.view.master.api.IMaster;

public class SecurityMatrixWebUiConfig {

    public final EntityCentre<UserRole> centre;

    public static SecurityMatrixWebUiConfig register(final Injector injector, final IWebUiBuilder builder) {
        return new SecurityMatrixWebUiConfig(injector, builder);
    }

    private SecurityMatrixWebUiConfig(final Injector injector, final IWebUiBuilder builder) {

        centre = createCentre(injector);
        builder.register(centre);

        builder.register(createSecurityMatrixInsertionPoint(injector));
        builder.register(createSecurityMatrixSaveActionMaster(injector));
    }

    private EntityMaster<SecurityMatrixSaveAction> createSecurityMatrixSaveActionMaster(final Injector injector) {
        return new EntityMaster<>(SecurityMatrixSaveAction.class, null, null, injector);
    }

    private EntityMaster<SecurityMatrixInsertionPoint> createSecurityMatrixInsertionPoint(final Injector injector) {
        final IMaster<SecurityMatrixInsertionPoint> config = new SecurityMatrixInsertionPointMaster();
        return new EntityMaster<>(SecurityMatrixInsertionPoint.class, config, injector);
    }

    private EntityCentre<UserRole> createCentre(final Injector injector) {

        final EntityCentreConfig<UserRole> ecc = EntityCentreBuilder.centreFor(UserRole.class)
                .runAutomatically()
                .setPageCapacity(10)
                .setVisibleRowsCount(10)
                .addProp("this").width(60).also()
                .addProp("desc").width(160)
                .addInsertionPoint(
                        action(SecurityMatrixInsertionPoint.class)
                             .withContext(context().withSelectionCrit().build())
                             .icon("stub")
                             .prefDimForView(mkDim("'auto'", "'770px'"))
                             .withNoParentCentreRefresh()
                             .build(),
                         InsertionPoints.TOP)
                .build();

        final EntityCentre<UserRole> centreConfig = new EntityCentre<>(MiSecurityMatrix.class, MiSecurityMatrix.class.getSimpleName(), ecc, injector, null);
        return centreConfig;
    }
}
