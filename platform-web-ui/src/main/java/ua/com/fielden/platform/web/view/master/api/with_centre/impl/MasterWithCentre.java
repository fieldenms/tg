package ua.com.fielden.platform.web.view.master.api.with_centre.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.minijs.JsCode;

import java.util.Optional;

import static java.lang.String.format;

/// An entity master that represents a single Entity Centre.
///
public class MasterWithCentre<T extends AbstractEntity<?>> extends AbstractMasterWithCentre<T> {

    public final EntityCentre<?> embeddedCentre;

    MasterWithCentre(
            final Class<T> entityType,
            final boolean saveOnActivate,
            final EntityCentre<?> entityCentre,
            final Optional<JsCode> customCode,
            final Optional<JsCode> customCodeOnAttach,
            final Optional<JsCode> customImports)
    {
        super(entityType, saveOnActivate, customCode, customCodeOnAttach,customImports);
        embeddedCentre = entityCentre;
    }

    @Override
    protected String getAttributes() {
        return """
               {
                    embedded: true,
                    enforcePostSaveRefresh: %s,
                    eventSourceClass: '%s',
                    uuid: this.uuid
               }
               """.formatted(embeddedCentre.shouldEnforcePostSaveRefresh(), embeddedCentre.eventSourceClass().map(Class::getName).orElse(""));
    }

    @Override
    protected String getElementName() {
        return format("tg-%s-centre", embeddedCentre.getMenuItemType().getSimpleName());
    }

    @Override
    protected String getImportUri() {
        return format("/centre_ui/%s", embeddedCentre.getMenuItemType().getName());
    }
}
