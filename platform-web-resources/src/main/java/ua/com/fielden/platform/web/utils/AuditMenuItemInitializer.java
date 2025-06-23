package ua.com.fielden.platform.web.utils;

import jakarta.inject.Inject;
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
    public AuditCompoundMenuItem init(final AuditCompoundMenuItem menuItem) {
        return null;
    }
}
