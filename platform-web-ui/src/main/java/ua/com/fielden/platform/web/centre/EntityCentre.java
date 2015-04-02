package ua.com.fielden.platform.web.centre;

import java.util.LinkedHashSet;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.dom.InnerTextElement;
import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.utils.ResourceLoader;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig;
import ua.com.fielden.platform.web.centre.api.ICentre;
import ua.com.fielden.platform.web.interfaces.IRenderable;

import com.google.inject.Injector;

/**
 * Represents the entity centre.
 *
 * @author TG Team
 *
 */
public class EntityCentre<T extends AbstractEntity<?>> implements ICentre<T> {
    private final Logger logger = Logger.getLogger(getClass());
    private final Class<? extends MiWithConfigurationSupport<?>> menuItemType;
    private final String name;
    private final EntityCentreConfig dslDefaultConfig;
    private final Injector injector;
    private final Class<T> entityType;
    private final Class<? extends MiWithConfigurationSupport<?>> miType;

    /**
     * Creates new {@link EntityCentre} instance for the menu item type and with specified name.
     *
     * @param miType
     *            - the menu item type for which this entity centre is to be created.
     * @param name
     *            - the name for this entity centre.
     * @param dslDefaultConfig
     *            -- default configuration taken from Centre DSL
     */
    public EntityCentre(final Class<? extends MiWithConfigurationSupport<?>> miType, final String name, final EntityCentreConfig dslDefaultConfig, final Injector injector) {
        this.menuItemType = miType;
        this.name = name;
        this.dslDefaultConfig = dslDefaultConfig;
        this.injector = injector;
        this.miType = miType;
        this.entityType = (Class<T>) CentreUtils.getEntityType(miType);
    }

    /**
     * Returns the menu item type for this {@link EntityCentre} instance.
     *
     * @return
     */
    public Class<? extends MiWithConfigurationSupport<?>> getMenuItemType() {
        return this.menuItemType;
    }

    /**
     * Returns the entity centre name.
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Default configuration taken from Centre DSL.
     *
     * @return
     */
    public EntityCentreConfig getDslDefaultConfig() {
        return dslDefaultConfig;
    }

    @Override
    public IRenderable build() {
        return createRenderableRepresentation();
    }

    private IRenderable createRenderableRepresentation() {
        final ICentreDomainTreeManagerAndEnhancer centre = CentreUtils.getFreshCentre(getUserSpecificGdtm(), this.menuItemType);
        logger.error("Building renderable for cdtmae:" + centre);

        final LinkedHashSet<String> importPaths = new LinkedHashSet<>();
        importPaths.add("polymer/polymer/polymer");
        importPaths.add("master/tg-entity-master");

        //        final DomElement editorContainer = layout.render();
        //
        //        importPaths.add(layout.importPath());
        //        widgets.forEach(widget -> {
        //            importPaths.add(widget.widget().importPath());
        //            editorContainer.add(widget.widget().render());
        //            if (widget.widget().action() != null) {
        //                propertyActionsStr.append(widget.widget().action().code().toString());
        //            }
        //        });

        final String entityCentreStr = ResourceLoader.getText("ua/com/fielden/platform/web/centre/tg-entity-centre-template.html").
                // replace("<!--@imports-->", createImports(importPaths)).
                replace("@entity_type", entityType.getSimpleName()).
                replace("@full_entity_type", entityType.getName()).
                replace("@mi_type", miType.getName());
        // replace("<!--@criteria_editors-->", editorContainer.toString());

        final IRenderable representation = new IRenderable() {
            @Override
            public DomElement render() {
                return new InnerTextElement(entityCentreStr);
            }
        };
        return representation;
    }

    /**
     * Returns the global manager for the user on this concrete thread, on which {@link #build()} was invoked.
     *
     * @return
     */
    private IGlobalDomainTreeManager getUserSpecificGdtm() {
        return injector.getInstance(IGlobalDomainTreeManager.class);
    }
}
