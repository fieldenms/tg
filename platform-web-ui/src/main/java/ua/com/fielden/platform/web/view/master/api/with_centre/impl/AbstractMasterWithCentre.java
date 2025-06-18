package ua.com.fielden.platform.web.view.master.api.with_centre.impl;

import ua.com.fielden.platform.basic.IValueMatcherWithContext;
import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.dom.InnerTextElement;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.utils.ResourceLoader;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionKind;
import ua.com.fielden.platform.web.interfaces.IRenderable;
import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.view.master.api.IMaster;

import java.util.Optional;

import static java.lang.String.format;
import static ua.com.fielden.platform.web.centre.EntityCentre.IMPORTS;
import static ua.com.fielden.platform.web.view.master.EntityMaster.ENTITY_TYPE;
import static ua.com.fielden.platform.web.view.master.EntityMaster.flattenedNameOf;

public abstract class AbstractMasterWithCentre<T extends AbstractEntity<?>> implements IMaster<T> {

    private final Class<T> entityType;
    private final boolean saveOnActivate;
    private final Optional<JsCode> customCode;
    private final Optional<JsCode> customCodeOnAttach;
    private final Optional<JsCode> customImports;

    private IRenderable renderable;

    AbstractMasterWithCentre(final Class<T> entityType, final boolean saveOnActivate, final Optional<JsCode> customCode, final Optional<JsCode> customCodeOnAttach, final Optional<JsCode> customImports) {
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
                    .replace(IMPORTS, "import '/resources/element_loader/tg-element-loader.js';\n" + customImports.map(ci -> ci.toString()).orElse(""))
                    .replace(ENTITY_TYPE, flattenedNameOf(entityType))
                    .replace("<!--@tg-entity-master-content-->",
                            format(""
                                            + "<tg-element-loader id='loader' context='[[_createContextHolderForEmbeddedViews]]' context-property='getMasterEntity' "
                                            + "    import='%s' "
                                            + "    element-name='%s'>"
                                            + "</tg-element-loader>",
                                    getImportUri(), getElementName()))
                    .replace("//@ready-callback",
                            "self.masterWithCentre = true;\n" +
                                    "self.classList.remove('canLeave');\n" +
                                    "self._focusEmbededView = function () {\n" +
                                    "    if (this.wasLoaded() && this.$.loader.loadedElement.focusView) {\n" +
                                    "        this.$.loader.loadedElement.focusView();\n" +
                                    "    }\n" +
                                    "}.bind(self);\n" +
                                    "self._hasEmbededView = function () {\n" +
                                    "    return true;\n" +
                                    "}.bind(self);\n"+
                                    "self.wasLoaded = function () {\n" +
                                    "    if (this.$.loader.loadedElement) {\n" +
                                    "        return this.$.loader.loadedElement.wasLoaded();\n" +
                                    "    }\n" +
                                    "    return false;\n" +
                                    "}.bind(self);\n" +
                                    "self._focusNextEmbededView = function (e) {\n" +
                                    "    if (this.wasLoaded() && this.$.loader.loadedElement.focusNextView) {\n" +
                                    "        this.$.loader.loadedElement.focusNextView(e);\n" +
                                    "    }\n" +
                                    "}.bind(self);\n" +
                                    "self._focusPreviousEmbededView = function (e) {\n" +
                                    "    if (this.wasLoaded() && this.$.loader.loadedElement.focusPreviousView) {\n" +
                                    "        this.$.loader.loadedElement.focusPreviousView(e);\n" +
                                    "    }\n" +
                                    "}.bind(self);\n")
                    .replace("//@attached-callback",
                            format(""
                                            + "self.$.loader.attrs = %s;\n"
                                            + "self.registerCentreRefreshRedirector();\n",
                                    getAttributes()))
                    .replace("//@master-is-ready-custom-code", customCode.map(code -> code.toString()).orElse(""))
                    .replace("//@master-has-been-attached-custom-code", customCodeOnAttach.map(code -> code.toString()).orElse(""))
                    .replace("@prefDim", "null")
                    .replace("@noUiValue", "false")
                    .replace("@saveOnActivationValue", saveOnActivate + "");
            renderable = new IRenderable() {
                @Override
                public DomElement render() {
                    return new InnerTextElement(entityMasterStr);
                }
            };
        }
        return renderable;
    }

    @Override
    public Optional<Class<? extends IValueMatcherWithContext<T, ?>>> matcherTypeFor(final String propName) {
        return Optional.empty();
    }

    @Override
    public EntityActionConfig actionConfig(final FunctionalActionKind actionKind, final int actionNumber) {
        throw new UnsupportedOperationException("Getting of action configuration is not supported.");
    }
}
