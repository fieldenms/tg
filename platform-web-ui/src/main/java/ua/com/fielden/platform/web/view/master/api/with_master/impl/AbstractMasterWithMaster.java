package ua.com.fielden.platform.web.view.master.api.with_master.impl;

import java.util.Optional;

import ua.com.fielden.platform.basic.IValueMatcherWithContext;
import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.dom.InnerTextElement;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.utils.ResourceLoader;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionKind;
import ua.com.fielden.platform.web.interfaces.IRenderable;
import ua.com.fielden.platform.web.view.master.api.IMaster;

public abstract class AbstractMasterWithMaster<T extends AbstractEntity<?>> implements IMaster<T> {

    private final IRenderable renderable;

    public AbstractMasterWithMaster(final Class<T> entityType, final Class<? extends AbstractEntity<?>> embededMasterType, final boolean shouldRefreshParentCentreAfterSave) {
        final String entityMasterStr = ResourceLoader.getText("ua/com/fielden/platform/web/master/tg-entity-master-template.html")
                .replace("<!--@imports-->", "<link rel='import' href='/app/tg-element-loader.html'>\n")
                .replace("@entity_type", entityType.getSimpleName())
                .replace("<!--@tg-entity-master-content-->",
                          "<tg-element-loader id='loader' context='[[_createContextHolderForEmbeddedViews]]' context-property='getMasterEntity' "
                        + "    import=" + getImportUri(embededMasterType)
                        + "    element-name=" + getElementName(embededMasterType)
                        + "    attrs='[[_calcAttrs(_currBindingEntity)]]'"
                        + "    >"
                        + "</tg-element-loader>")
                .replace("//@ready-callback",
                        "this.masterWithMaster = true;\n" +
                        "this._focusEmbededView = function () {\n" +
                        "    if (this.$.loader.loadedElement && this.$.loader.loadedElement.focusView) {\n" +
                        "        this.$.loader.loadedElement.focusView();\n" +
                        "    }\n" +
                        "}.bind(this);\n" +
                        "this._hasEmbededView = function () {\n" +
                        "    return true;\n" +
                        "}.bind(this);\n" +
                        "self.wasLoaded = function () {\n" +
                        "    if (this.$.loader.loadedElement && this.$.loader.loadedElement.wasLoaded) {\n" +
                        "        return this.$.loader.loadedElement.wasLoaded();\n" +
                        "    }\n" +
                        "    return false;\n" +
                        "}.bind(self);\n" +
                        "this._focusNextEmbededView = function (e) {\n" +
                        "    if (this.$.loader.loadedElement && this.$.loader.loadedElement.focusNextView) {\n" +
                        "        this.$.loader.loadedElement.focusNextView(e);\n" +
                        "    }\n" +
                        "}.bind(this);\n" +
                        "this._focusPreviousEmbededView = function (e) {\n" +
                        "    if (this.$.loader.loadedElement && this.$.loader.loadedElement.focusPreviousView) {\n" +
                        "        this.$.loader.loadedElement.focusPreviousView(e);\n" +
                        "    }\n" +
                        "}.bind(this);\n" +
                        "this._calcAttrs = (function(_currBindingEntity){\n" +
                        "   if (_currBindingEntity !== null) {\n" +
                        "       return " + getAttributes(embededMasterType, "_currBindingEntity", shouldRefreshParentCentreAfterSave) +
                        "   };\n" +
                        "}).bind(this);\n")
                .replace("//@attached-callback",
                          "this.canLeave = function () {"
                        + "    const embeddedMaster = this.$.loader.loadedElement;\n"
                        + "    if (embeddedMaster && embeddedMaster.classList.contains('canLeave')) {\n"
                        + "        return embeddedMaster.canLeave();\n"
                        + "    }\n"
                        + "    return undefined;\n"
                        + "}.bind(this);\n"
                        + "this.addEventListener('after-load', " + getAfterLoadListener() + ");\n")
                .replace("@prefDim", "null")
                .replace("@noUiValue", "false")
                .replace("@saveOnActivationValue", "true");

        renderable = new IRenderable() {
            @Override
            public DomElement render() {
                return new InnerTextElement(entityMasterStr);
            }
        };
    }

    /**
     * Returns the implementation for the after load listener of embedded master.
     *
     * @return
     */
    protected String getAfterLoadListener() {
        return "this._assignPostSavedHandlersForEmbeddedMaster.bind(this)";
    }

    protected abstract String getAttributes(final Class<? extends AbstractEntity<?>> entityType, String bindingEntityName, final boolean shouldRefreshParentCentreAfterSave);

    protected abstract String getElementName(final Class<? extends AbstractEntity<?>> entityType);

    protected abstract String getImportUri(final Class<? extends AbstractEntity<?>> entityType);

    @Override
    public Optional<Class<? extends IValueMatcherWithContext<T, ?>>> matcherTypeFor(final String propName) {
        return Optional.empty();
    }

    @Override
    public IRenderable render() {
        return renderable;
    }

    @Override
    public EntityActionConfig actionConfig(final FunctionalActionKind actionKind, final int actionNumber) {
        throw new UnsupportedOperationException("Getting of action configuration is not supported.");
    }
}
