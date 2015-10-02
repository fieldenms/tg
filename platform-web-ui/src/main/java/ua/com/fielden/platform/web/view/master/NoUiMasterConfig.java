package ua.com.fielden.platform.web.view.master;

import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.dom.InnerTextElement;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.utils.ResourceLoader;
import ua.com.fielden.platform.web.interfaces.IRenderable;
import ua.com.fielden.platform.web.view.master.api.IMaster;

/**
 * An entity master that has no UI. Its main purpose is to be used for functional entities that have no visual representation.
 *
 * @author TG Team
 *
 * @param <T>
 */
public class NoUiMasterConfig<T extends AbstractEntity<?>> implements IMaster<T> {

    private final IRenderable renderable;

    public NoUiMasterConfig(final Class<T> entityType) {
        final String entityMasterStr = ResourceLoader.getText("ua/com/fielden/platform/web/master/tg-entity-master-template.html")
                .replace("<!--@imports-->", "")
                .replace("@entity_type", entityType.getSimpleName())
                .replace("<!--@tg-entity-master-content-->", "")
                .replace("//@ready-callback", "")
                .replace("@noUiValue", "true")
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

}
