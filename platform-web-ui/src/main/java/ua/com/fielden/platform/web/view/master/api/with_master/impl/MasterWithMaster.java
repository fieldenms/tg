package ua.com.fielden.platform.web.view.master.api.with_master.impl;

import static java.lang.String.format;

import java.util.Optional;

import ua.com.fielden.platform.basic.IValueMatcherWithContext;
import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.dom.InnerTextElement;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.utils.ResourceLoader;
import ua.com.fielden.platform.web.interfaces.IRenderable;
import ua.com.fielden.platform.web.view.master.EntityMaster;
import ua.com.fielden.platform.web.view.master.api.IMaster;

/**
 * An entity master that embeds a single Entity Master.
 *
 * @author TG Team
 *
 * @param <T>
 */
public class MasterWithMaster<T extends AbstractEntity<?>> implements IMaster<T> {

    private final IRenderable renderable;

    public MasterWithMaster(final Class<T> entityType, final EntityMaster<?> entityMaster) {
        final StringBuilder attrs = new StringBuilder();

        //////////////////////////////////////////////////////////////////////////////////////
        //// attributes should always be added with comma and space at the end, i.e. ", " ////
        //// this suffix is used to remove the last comma, which prevents JSON conversion ////
        //////////////////////////////////////////////////////////////////////////////////////
        attrs.append("{");
        attrs.append("\"entityType\":\"" + entityMaster.getEntityType().getName() + "\","
                   + "\"currentState\":\"EDIT\","
                   + "\"centreUuid\": this.centreUuid");
        attrs.append("}");

        final String attributes = attrs.toString();

        final String entityMasterStr = ResourceLoader.getText("ua/com/fielden/platform/web/master/tg-entity-master-template.html")
                .replace("<!--@imports-->", "<link rel='import' href='/app/tg-element-loader.html'>\n")
                .replace("@entity_type", entityType.getSimpleName())
                .replace("<!--@tg-entity-master-content-->",
                        format(""
                        + "<tg-element-loader id='loader' context='[[_createContextHolderForEmbeddedViews]]' context-property='getMasterEntity' "
                        + "    import='/master_ui/%s' "
                        + "    element-name='tg-%s-master' "
                        + "    >"
                        + "</tg-element-loader>",
                        entityMaster.getEntityType().getName(), entityMaster.getEntityType().getSimpleName()))
                .replace("//@ready-callback", "")
                .replace("//@attached-callback", format(""
                        + "this.$.loader.attrs = %s;\n"
                        + "this.canLeave = function () {"
                        + "    var embeddedMaster = this.$.loader.loadedElement;\n"
                        + "    if (embeddedMaster && embeddedMaster.classList.contains('canLeave')) {\n"
                        + "        return embeddedMaster.canLeave();\n"
                        + "    } else {\n"
                        + "        return undefined;\n"
                        + "    }\n"
                        + "}.bind(this);\n"
                        + "this.addEventListener('after-load', this._assignPostSavedHandlersForEmbeddedMaster.bind(this));\n",
                        attributes))
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
    public IRenderable render() {
        return renderable;
    }

    @Override
    public Optional<Class<? extends IValueMatcherWithContext<T, ?>>> matcherTypeFor(final String propName) {
        return Optional.empty();
    }

}
