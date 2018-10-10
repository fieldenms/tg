package ua.com.fielden.platform.web.resources.webui;

import com.google.inject.Injector;

import ua.com.fielden.platform.entity.SecurityMatrixInsertionPoint;
import ua.com.fielden.platform.entity.SecurityMatrixSaveAction;
import ua.com.fielden.platform.web.app.config.IWebUiBuilder;
import ua.com.fielden.platform.web.security.SecurityMatrixInsertionPointMaster;
import ua.com.fielden.platform.web.view.master.EntityMaster;
import ua.com.fielden.platform.web.view.master.api.IMaster;

public class SecurityMatrixWebUiConfig {

    public final EntityMaster<SecurityMatrixInsertionPoint> master;

    public static SecurityMatrixWebUiConfig register(final Injector injector, final IWebUiBuilder builder) {
        return new SecurityMatrixWebUiConfig(injector, builder);
    }

    private SecurityMatrixWebUiConfig(final Injector injector, final IWebUiBuilder builder) {

        this.master = createSecurityMatrixInsertionPoint(injector);

        builder.register(master);
        builder.register(createSecurityMatrixSaveActionMaster(injector));
    }

    private EntityMaster<SecurityMatrixSaveAction> createSecurityMatrixSaveActionMaster(final Injector injector) {
        return new EntityMaster<>(SecurityMatrixSaveAction.class, null, null, injector);
    }

    private EntityMaster<SecurityMatrixInsertionPoint> createSecurityMatrixInsertionPoint(final Injector injector) {
        final IMaster<SecurityMatrixInsertionPoint> config = new SecurityMatrixInsertionPointMaster();
        return new EntityMaster<>(SecurityMatrixInsertionPoint.class, config, injector);
    }
}
