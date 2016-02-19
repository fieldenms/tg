package ua.com.fielden.platform.web.view.master.entity_manipulation;

import java.util.Optional;

import ua.com.fielden.platform.basic.IValueMatcherWithContext;
import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.dom.InnerTextElement;
import ua.com.fielden.platform.entity.EntityManipulationAction;
import ua.com.fielden.platform.utils.ResourceLoader;
import ua.com.fielden.platform.web.interfaces.IRenderable;
import ua.com.fielden.platform.web.view.master.api.IMaster;

public class EntityManipulationMaster implements IMaster<EntityManipulationAction> {

    private final IRenderable renderable;

    public EntityManipulationMaster() {

        final String entityMasterStr = ResourceLoader.getText("ua/com/fielden/platform/web/master/tg-entity-master-template.html")
                .replace("<!--@imports-->", "<link rel='import' href='/app/tg-element-loader.html'>\n")
                .replace("@entity_type", EntityManipulationAction.class.getSimpleName())
                .replace("<!--@tg-entity-master-content-->",
                        "<tg-element-loader id='loader' context='[[_createContextHolderForEmbeddedViews]]' context-property='getMasterEntity' "
                                + "    import='[[_currBindingEntity.importUri]]' "
                                + "    element-name='[[_currBindingEntity.elementName]]' "
                                + "    attrs='[[_calcAttrs(_currBindingEntity)]]' "
                                + "    >"
                                + "</tg-element-loader>")
                .replace("//@ready-callback",
                        "this._calcAttrs = (function(bindingEntity){\n" +
                                "   if (bindingEntity !== null) {\n" +
                                "       return {\n" +
                                "           currentState: 'EDIT',\n" +
                                "           centreUuid: this.centreUuid,\n" +
                                "           entityId: bindingEntity.entityId,\n" +
                                "           entityType: bindingEntity.entityType\n" +
                                "       };\n" +
                                "   };\n" +
                                "}).bind(this);\n")
                .replace("//@attached-callback", "this.canLeave = function () {"
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

    @Override
    public Optional<Class<? extends IValueMatcherWithContext<EntityManipulationAction, ?>>> matcherTypeFor(final String propName) {
        return Optional.empty();
    }

    @Override
    public IRenderable render() {
        return renderable;
    }

}
