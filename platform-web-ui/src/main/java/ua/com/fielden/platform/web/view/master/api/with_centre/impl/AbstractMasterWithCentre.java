package ua.com.fielden.platform.web.view.master.api.with_centre.impl;

import ua.com.fielden.platform.basic.IValueMatcherWithContext;
import ua.com.fielden.platform.dom.InnerTextElement;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.utils.ResourceLoader;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionKind;
import ua.com.fielden.platform.web.interfaces.IRenderable;
import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.view.master.api.IMaster;

import java.util.Optional;

import static ua.com.fielden.platform.web.centre.EntityCentre.IMPORTS;
import static ua.com.fielden.platform.web.view.master.EntityMaster.ENTITY_TYPE;
import static ua.com.fielden.platform.web.view.master.EntityMaster.flattenedNameOf;

/// Abstract implementation of a master with centre view.
///
public abstract class AbstractMasterWithCentre<T extends AbstractEntity<?>> implements IMaster<T> {

    private final Class<T> entityType;
    private final boolean saveOnActivate;
    private final Optional<JsCode> customCode;
    private final Optional<JsCode> customCodeOnAttach;
    private final Optional<JsCode> customImports;

    private IRenderable renderable;

    AbstractMasterWithCentre(
            final Class<T> entityType,
            final boolean saveOnActivate,
            final Optional<JsCode> customCode,
            final Optional<JsCode> customCodeOnAttach,
            final Optional<JsCode> customImports)
    {
        this.entityType = entityType;
        this.saveOnActivate = saveOnActivate;
        this.customCode = customCode;
        this.customCodeOnAttach = customCodeOnAttach;
        this.customImports = customImports;
    }

    protected abstract String getAttributes();

    protected abstract String getElementName();

    protected abstract String getImportUri();

    @Override
    public IRenderable render() {
        if (renderable == null) {
            final String entityMasterStr = ResourceLoader.getText("ua/com/fielden/platform/web/master/tg-entity-master-template.js")
                    .replace(IMPORTS, "import '/resources/element_loader/tg-element-loader.js';\n" + customImports.map(JsCode::toString).orElse(""))
                    .replace(ENTITY_TYPE, flattenedNameOf(entityType))
                    .replace("<!--@tg-entity-master-content-->",
                            """
                            <tg-element-loader id='loader' context='[[_createContextHolderForEmbeddedViews]]' context-property='getMasterEntity'
                                import='%s'
                                element-name='%s'
                                attrs='[[_calcAttrs(_currBindingEntity)]]'>
                            </tg-element-loader>
                            """.formatted(getImportUri(), getElementName()))
                    .replace("//@ready-callback",
                            """
                            self.masterWithCentre = true;
                            self.classList.remove('canLeave');
                            self._focusEmbededView = function () {
                                if (this.wasLoaded() && this.$.loader.loadedElement.focusView) {
                                    this.$.loader.loadedElement.focusView();
                                }
                            }.bind(self);
                            self._hasEmbededView = function () {
                                return true;
                            }.bind(self);
                            self.wasLoaded = function () {
                                if (this.$.loader.loadedElement) {
                                    return this.$.loader.loadedElement.wasLoaded();
                                }
                                return false;
                            }.bind(self);
                            self._focusNextEmbededView = function (e) {
                                if (this.wasLoaded() && this.$.loader.loadedElement.focusNextView) {
                                    this.$.loader.loadedElement.focusNextView(e);
                                }
                            }.bind(self);
                            self._focusPreviousEmbededView = function (e) {
                                if (this.wasLoaded() && this.$.loader.loadedElement.focusPreviousView) {
                                    this.$.loader.loadedElement.focusPreviousView(e);
                                }
                            }.bind(self);
                            self._calcAttrs = function (_currBindingEntity) {
                                if (_currBindingEntity !== null) {
                                    return %s;
                                }
                            }.bind(self);
                            """.formatted(getAttributes()))
                    .replace("//@attached-callback", "self.registerCentreRefreshRedirector();\n")
                    .replace("//@master-is-ready-custom-code", customCode.map(JsCode::toString).orElse(""))
                    .replace("//@master-has-been-attached-custom-code", customCodeOnAttach.map(JsCode::toString).orElse(""))
                    .replace("@prefDim", "null")
                    .replace("@noUiValue", "false")
                    .replace("@saveOnActivationValue", saveOnActivate + "");
            renderable = () -> new InnerTextElement(entityMasterStr);
        }
        return renderable;
    }

    @Override
    public Optional<Class<? extends IValueMatcherWithContext<T, ?>>> matcherTypeFor(final String propName) {
        return Optional.empty();
    }

    @Override
    public EntityActionConfig actionConfig(final FunctionalActionKind actionKind, final int actionNumber) {
        throw new UnsupportedOperationException("Action configuration is not supported.");
    }

}
