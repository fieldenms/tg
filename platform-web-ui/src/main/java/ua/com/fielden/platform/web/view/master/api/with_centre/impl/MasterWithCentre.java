package ua.com.fielden.platform.web.view.master.api.with_centre.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.minijs.JsCode;

import java.util.Optional;

import static java.lang.String.format;

/**
 * An entity master that represents a single Entity Centre.
 *
 * @author TG Team
 *
 * @param <T>
 */
public class MasterWithCentre<T extends AbstractEntity<?>> extends AbstractMasterWithCentre<T> {
    public final EntityCentre<?> embeddedCentre;

    MasterWithCentre(final Class<T> entityType, final boolean saveOnActivate, final EntityCentre<?> entityCentre, final Optional<JsCode> customCode, final Optional<JsCode> customCodeOnAttach, final Optional<JsCode> customImports) {
        super(entityType, saveOnActivate, customCode, customCodeOnAttach,customImports);
        embeddedCentre = entityCentre;
    }

    @Override
    protected String getAttributes() {
        final StringBuilder attrs = new StringBuilder();

        //////////////////////////////////////////////////////////////////////////////////////
        //// attributes should always be added with comma and space at the end, i.e. ", " ////
        //// this suffix is used to remove the last comma, which prevents JSON conversion ////
        //////////////////////////////////////////////////////////////////////////////////////
        attrs.append("{");
        attrs.append("\"embedded\": true, ");
        if (embeddedCentre.shouldEnforcePostSaveRefresh()) {
            attrs.append("\"enforcePostSaveRefresh\": true, ");
        }
        attrs.append(format("eventSourceClass: \"%s\",", embeddedCentre.eventSourceClass().map(clazz -> clazz.getName()).orElse("")));

        // let's make sure that uuid is defined from the embedded centre, which is required
        // for proper communication of the centre with related actions
        attrs.append("\"uuid\": this.uuid, ");
        attrs.append("}");

        return attrs.toString().replace(", }", " }");
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
