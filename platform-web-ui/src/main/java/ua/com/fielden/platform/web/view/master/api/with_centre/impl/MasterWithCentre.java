package ua.com.fielden.platform.web.view.master.api.with_centre.impl;

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
public class MasterWithCentre<T extends AbstractEntity<?>> implements IMaster<T> {

    private final IRenderable renderable;

    public MasterWithCentre(final Class<T> entityType, final boolean saveOnActivate, final EntityCentre<?> entityCentre) {
        final StringBuilder attrs = new StringBuilder();

        //////////////////////////////////////////////////////////////////////////////////////
        //// attributes should always be added with comma and space at the end, i.e. ", " ////
        //// this suffix is used to remove the last comma, which prevents JSON conversion ////
        //////////////////////////////////////////////////////////////////////////////////////
        attrs.append("{");
        if (entityCentre.isRunAutomatically()) {
            attrs.append("\"autoRun\": true, ");
        }
        if (entityCentre.shouldEnforcePostSaveRefresh()) {
            attrs.append("\"enforcePostSaveRefresh\": true, ");
        }
        if (entityCentre.eventSourceUri().isPresent()) {
            attrs.append(format("\"uri\": \"%s\", ", entityCentre.eventSourceUri().get()));
        }
        
        // let's make sure that uuid is defined from the embedded centre, which is required
        // for proper communication of the centre with related actions
        attrs.append("\"uuid\": this.uuid, ");
        attrs.append("}");

        final String attributes = attrs.toString().replace(", }", " }");

        final String entityMasterStr = ResourceLoader.getText("ua/com/fielden/platform/web/master/tg-entity-master-template.html")
                .replace("<!--@imports-->", "<link rel='import' href='/app/tg-element-loader.html'>\n")
                .replace("@entity_type", entityType.getSimpleName())
                .replace("<!--@tg-entity-master-content-->",
                        format(""
                        + "<tg-element-loader id='loader' context='[[_createContextHolderForEmbeddedViews]]' context-property='getMasterEntity' "
                        + "    import='/centre_ui/%s' "
                        + "    element-name='tg-%s-centre'>"
                        + "</tg-element-loader>",
                        entityCentre.getMenuItemType().getName(), entityCentre.getMenuItemType().getSimpleName(), attributes))
                .replace("//@ready-callback", "self.classList.remove('canLeave');")
                .replace("//@attached-callback", format("this.$.loader.attrs = %s;\n", attributes))
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
