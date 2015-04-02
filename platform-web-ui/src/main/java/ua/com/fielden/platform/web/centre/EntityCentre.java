package ua.com.fielden.platform.web.centre;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

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
import ua.com.fielden.platform.web.centre.api.crit.impl.AbstractCriterionWidget;
import ua.com.fielden.platform.web.centre.api.crit.impl.CriterionWidget;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.interfaces.IRenderable;
import ua.com.fielden.platform.web.layout.FlexLayout;
import ua.com.fielden.platform.web.view.master.api.impl.SimpleMasterBuilder;

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

        // TODO remove this later, when layout will be correctly initialised in dslDefaultConfig!
        final String mr = "['margin-right: 40px', 'flex']";
        final String mrLast = "['flex']";
        this.dslDefaultConfig.getSelectionCriteriaLayout().whenMedia(Device.DESKTOP, null).set(
                ("[['center-justified', mr, mr, mrLast]," +
                        "['center-justified', mr, mr, mrLast]," +
                        "['center-justified', mr, mr, mrLast]]")
                        .replaceAll("mrLast", mrLast).replaceAll("mr", mr)
                );
        this.dslDefaultConfig.getSelectionCriteriaLayout().whenMedia(Device.TABLET, null).set(
                ("[['center-justified', mr, mrLast]," +
                        "['center-justified', mr, mrLast]," +
                        "['center-justified', mr, mrLast]," +
                        "['center-justified', mr, mrLast]," +
                        "['center-justified', mr, mrLast]]")
                        .replaceAll("mrLast", mrLast).replaceAll("mr", mr)
                );
        this.dslDefaultConfig.getSelectionCriteriaLayout().whenMedia(Device.MOBILE, null).set(
                ("[['center-justified', mrLast]," +
                        "['center-justified', mrLast]," +
                        "['center-justified', mrLast]," +
                        "['center-justified', mrLast]," +
                        "['center-justified', mrLast]," +
                        "['center-justified', mrLast]," +
                        "['center-justified', mrLast]," +
                        "['center-justified', mrLast]," +
                        "['center-justified', mrLast]]")
                        .replaceAll("mrLast", mrLast).replaceAll("mr", mr)
                );

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

        final FlexLayout layout = this.dslDefaultConfig.getSelectionCriteriaLayout();

        final DomElement editorContainer = layout.render();

        importPaths.add(layout.importPath());

        final Class<?> root = this.entityType;

        final List<AbstractCriterionWidget> criteriaWidgets = new ArrayList<>();
        for (final String critProp : centre.getFirstTick().checkedProperties(root)) {
            criteriaWidgets.add(new CriterionWidget(root, centre.getEnhancer().getManagedType(root), critProp));
        }
        criteriaWidgets.forEach(widget -> {
            importPaths.add(widget.importPath());
            importPaths.addAll(widget.editorsImportPaths());
            editorContainer.add(widget.render());
        });

        final String entityCentreStr = ResourceLoader.getText("ua/com/fielden/platform/web/centre/tg-entity-centre-template.html").
                replace("<!--@imports-->", SimpleMasterBuilder.createImports(importPaths)).
                replace("@entity_type", entityType.getSimpleName()).
                replace("@full_entity_type", entityType.getName()).
                replace("@mi_type", miType.getName()).
                replace("<!--@criteria_editors-->", editorContainer.toString());

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
