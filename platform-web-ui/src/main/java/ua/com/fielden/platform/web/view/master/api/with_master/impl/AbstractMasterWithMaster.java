package ua.com.fielden.platform.web.view.master.api.with_master.impl;

import java.util.Optional;

import ua.com.fielden.platform.basic.IValueMatcherWithContext;
import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.dom.InnerTextElement;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.utils.ResourceLoader;
import ua.com.fielden.platform.web.interfaces.IRenderable;
import ua.com.fielden.platform.web.view.master.api.IMaster;

public abstract class AbstractMasterWithMaster<T extends AbstractEntity<?>> implements IMaster<T> {

    private final IRenderable renderable;

    public AbstractMasterWithMaster(final Class<T> entityType, final Class<? extends AbstractEntity<?>> embededMasterType) {
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
                .replace("//@ready-callback", "this._calcAttrs = (function(_currBindingEntity){\n" +
                        "   if (_currBindingEntity !== null) {\n" +
                        "       return " + getAttributes(embededMasterType, "_currBindingEntity") +
                        "   };\n" +
                        "}).bind(this);\n")
                .replace("//@attached-callback",
                          "this.canLeave = function () {"
                        + "    var embeddedMaster = this.$.loader.loadedElement;\n"
                        + "    if (embeddedMaster && embeddedMaster.classList.contains('canLeave')) {\n"
                        + "        return embeddedMaster.canLeave();\n"
                        + "    } else {\n"
                        + "        return undefined;\n"
                        + "    }\n"
                        + "}.bind(this);\n"
                        + "this.addEventListener('after-load', this._assignPostSavedHandlersForEmbeddedMaster.bind(this));\n")
                .replace("@noUiValue", "false")
                .replace("@saveOnActivationValue", "true");

        renderable = new IRenderable() {
            @Override
            public DomElement render() {
                return new InnerTextElement(entityMasterStr);
            }
        };
    }

    protected abstract String getAttributes(final Class<? extends AbstractEntity<?>> entityType, String bindingEntityName);

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

}
