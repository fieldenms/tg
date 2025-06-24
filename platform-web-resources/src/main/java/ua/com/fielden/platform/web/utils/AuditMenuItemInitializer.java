package ua.com.fielden.platform.web.utils;

import jakarta.inject.Inject;
import ua.com.fielden.platform.audit.AuditUtils;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AuditCompoundMenuItem;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.audit.IAuditWebUiConfigFactory;
import ua.com.fielden.platform.web.interfaces.IAuditMenuItemInitializer;

public class AuditMenuItemInitializer implements IAuditMenuItemInitializer {

    private final IWebUiConfig webUiConfig;
    private final IAuditWebUiConfigFactory auditConfigFactory;

    @Inject
    public AuditMenuItemInitializer(final IWebUiConfig webUiConfig, final IAuditWebUiConfigFactory auditConfigFactory) {
        this.webUiConfig = webUiConfig;
        this.auditConfigFactory = auditConfigFactory;
    }

    @Override
    public AuditCompoundMenuItem init(final Class<? extends AbstractEntity<?>> auditedType, final AuditCompoundMenuItem menuItem) {
        if (AuditUtils.isAudited(auditedType)) {
            var miType =  auditConfigFactory.miTypeForEmbeddedCentre(auditedType);
            var embeddedCentre = webUiConfig.getEmbeddedCentres().get(miType);
            if (embeddedCentre != null) {
                menuItem.setMenuItemTypeForCentre(miType)
                        .setShouldEnforcePostSaveRefresh(embeddedCentre._1.shouldEnforcePostSaveRefresh())
                        .setEventSourceClass(embeddedCentre._1.eventSourceClass().map(Class::getName).orElse(""));
            }
        }
        return menuItem;
    }
}
