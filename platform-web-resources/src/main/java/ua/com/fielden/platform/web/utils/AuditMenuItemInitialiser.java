package ua.com.fielden.platform.web.utils;

import jakarta.inject.Inject;
import ua.com.fielden.platform.audit.AuditUtils;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AuditCompoundMenuItem;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.audit.IAuditWebUiConfigFactory;
import ua.com.fielden.platform.web.interfaces.IAuditMenuItemInitialiser;
import ua.com.fielden.platform.web.ioc.exceptions.MissingCentreConfigurationException;

public class AuditMenuItemInitialiser implements IAuditMenuItemInitialiser {

    public static final String ERR_ENTITY_TYPE_IS_NOT_AUDITED = "Entity type [%s] is not audited.";
    public static final String ERR_AUDITED_TYPE_HAS_NO_REGISTERED_CENTRE = "Audited entity type [%s] has no registered entity centre with menu type [%s].";


    private final IWebUiConfig webUiConfig;
    private final IAuditWebUiConfigFactory auditConfigFactory;

    @Inject
    public AuditMenuItemInitialiser(final IWebUiConfig webUiConfig, final IAuditWebUiConfigFactory auditConfigFactory) {
        this.webUiConfig = webUiConfig;
        this.auditConfigFactory = auditConfigFactory;
    }

    @Override
    public AuditCompoundMenuItem init(final Class<? extends AbstractEntity<?>> auditedType, final AuditCompoundMenuItem menuItem) {
        if (AuditUtils.isAudited(auditedType)) {
            var miType =  auditConfigFactory.miTypeForEmbeddedCentre(auditedType);
            var centre = webUiConfig.getCentres().get(miType);
            if (centre != null) {
                menuItem.setMenuItemTypeForCentre(miType)
                        .setShouldEnforcePostSaveRefresh(centre.shouldEnforcePostSaveRefresh())
                        .setEventSourceClass(centre.eventSourceClass().map(Class::getName).orElse(""));
            }
            else {
                throw new MissingCentreConfigurationException(ERR_AUDITED_TYPE_HAS_NO_REGISTERED_CENTRE.formatted(auditedType.getSimpleName(), miType.getSimpleName()));
            }
        }
        else {
            throw new InvalidArgumentException(ERR_ENTITY_TYPE_IS_NOT_AUDITED.formatted(auditedType.getSimpleName()));
        }
        return menuItem;
    }

}
