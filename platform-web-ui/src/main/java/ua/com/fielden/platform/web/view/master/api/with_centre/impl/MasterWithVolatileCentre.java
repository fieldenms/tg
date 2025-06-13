package ua.com.fielden.platform.web.view.master.api.with_centre.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.minijs.JsCode;

import java.util.Optional;

public class MasterWithVolatileCentre<T extends AbstractEntity<?>> extends AbstractMasterWithCentre<T>{

    MasterWithVolatileCentre(final Class<T> entityType, final boolean saveOnActivate, final Optional<JsCode> customCode, final Optional<JsCode> customCodeOnAttach, final Optional<JsCode> customImports) {
        super(entityType, saveOnActivate, customCode, customCodeOnAttach, customImports);
    }

    @Override
    protected String getAttributes() {
        return "{" +
                "   embedded: true," +
                "   uuid: this.uuid," +
                "   enforcePostSaveRefresh: this._currBindingEntity.shouldEnforcePostSaveRefresh," +
                "   eventSourceClass: this._currBindingEntity.eventSourceClass" +
                "};";
    }

    @Override
    protected String getElementName() {
        return "'[[_currBindingEntity.elementName]]'";
    }

    @Override
    protected String getImportUri() {
        return "'[[_currBindingEntity.importUri]]'";
    }

}
