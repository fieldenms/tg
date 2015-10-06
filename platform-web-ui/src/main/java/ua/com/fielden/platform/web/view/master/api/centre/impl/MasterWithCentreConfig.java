package ua.com.fielden.platform.web.view.master.api.centre.impl;

import static java.lang.String.format;

import java.util.Optional;

import ua.com.fielden.platform.basic.IValueMatcherWithContext;
import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.dom.InnerTextElement;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.utils.ResourceLoader;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.interfaces.IRenderable;
import ua.com.fielden.platform.web.view.master.api.IMaster;

/**
 * An entity master that represents a single Entity Centre.
 *
 * @author TG Team
 *
 * @param <T>
 */
public class MasterWithCentreConfig<T extends AbstractEntity<?>> implements IMaster<T> {

    private final IRenderable renderable;

    public MasterWithCentreConfig(final Class<T> entityType, final boolean saveOnActivate, final EntityCentre<?> entityCentre) {
        final StringBuilder attrs = new StringBuilder();

        //////////////////////////////////////////////////////////////////////////////////////
        //// attributes should always be added with comma and space at the end, i.e. ", " ////
        //// this suffix is used to remove the last comma, which prevents JSON conversion ////
        //////////////////////////////////////////////////////////////////////////////////////
        attrs.append("{");
        if (entityCentre.isRunAutomatically()) {
            attrs.append("\"autoRun\": true, ");
        }
        if (entityCentre.eventSourceUri().isPresent()) {
            attrs.append(format("\"uri\": \"%s\", ", entityCentre.eventSourceUri().get()));
        }

        attrs.append("}");

        final String attributes = attrs.toString().replace(", }", " }");

        final String entityMasterStr = ResourceLoader.getText("ua/com/fielden/platform/web/master/tg-entity-master-template.html")
                .replace("<!--@imports-->", "<link rel='import' href='/app/tg-element-loader.html'>\n")
                .replace("@entity_type", entityType.getSimpleName())
                .replace("<!--@tg-entity-master-content-->",
                        format(""
                        + "<tg-element-loader id='loader' context='[[_createContextHolderForEmbeddedViews]]' context-property='getMasterEntity' "
                        + "    import='/centre_ui/%s' "
                        + "    element-name='tg-%s-centre' "
                        + "    attrs='%s'>"
                        + "</tg-element-loader>",
                        entityCentre.getMenuItemType().getName(), entityCentre.getMenuItemType().getSimpleName(), attributes))
                .replace("//@ready-callback", "")
                .replace("@noUiValue", "false")
                .replace("@saveOnActivationValue", saveOnActivate + "");

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
