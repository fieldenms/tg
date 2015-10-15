package ua.com.fielden.platform.web.view.master.api.compound.impl;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.basic.IValueMatcherWithContext;
import ua.com.fielden.platform.dom.DomContainer;
import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.dom.InnerTextElement;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.utils.ResourceLoader;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionElement;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionKind;
import ua.com.fielden.platform.web.interfaces.IRenderable;
import ua.com.fielden.platform.web.view.master.api.IMaster;
import ua.com.fielden.platform.web.view.master.api.impl.SimpleMasterBuilder;

/**
 * A compound entity master that has a menu.
 *
 * @author TG Team
 *
 * @param <T> -- a type of the main entity, which drives the logic of the master, such as Vehicle, WorkOrder.
 * @param <F> -- a type of the functional entity, which opens the compound master; its key has to be of the same type as the main entity.
 */
public class MasterWithMenu<T extends AbstractEntity<?>, F extends AbstractFunctionalEntityWithCentreContext<T>> implements IMaster<F> {

    /**
     * Actions that represent menu items in a compound view.
     */
    private final List<EntityActionConfig> menuItemActions = new ArrayList<>();
    private final IRenderable renderable;
    private final Logger logger = Logger.getLogger(getClass());

    public MasterWithMenu(final Class<T> entityType, final Class<F> functionalEntityType, final List<EntityActionConfig> menuItemActions) {
        logger.debug("Initiating insertion point actions...");
        final LinkedHashSet<String> importPaths = new LinkedHashSet<>();
        importPaths.add("master/menu/tg-master-menu");
        importPaths.add("master/menu/tg-master-menu-item-section");
        
        this.menuItemActions.addAll(menuItemActions);

        final List<FunctionalActionElement> menuItemActionsElements = new ArrayList<>();
        for (int index = 0; index < menuItemActions.size(); index++) {
            final EntityActionConfig eac = menuItemActions.get(index);
            final FunctionalActionElement el = new FunctionalActionElement(eac, index, FunctionalActionKind.MENU_ITEM);
            menuItemActionsElements.add(el);
        }

        final StringBuilder jsMenuItemActionObjects = new StringBuilder();
        final DomContainer menuItemActionsDom = new DomContainer();
        final DomContainer menuItemViewsDom = new DomContainer();
        
        for (final FunctionalActionElement el : menuItemActionsElements) {
            importPaths.add(el.importPath());
            menuItemActionsDom.add(el.render());
            jsMenuItemActionObjects.append(el.createActionObject() + ",\n");
            menuItemViewsDom.add(new DomElement("tg-master-menu-item-section").attr("id", "mi" + el.numberOfAction).attr("data-route", el.conf().functionalEntity.get().getSimpleName()));
        }

        // generate the final master with menu
        final String entityMasterStr = ResourceLoader.getText("ua/com/fielden/platform/web/master/tg-entity-master-template.html")
                .replace("<!--@imports-->", SimpleMasterBuilder.createImports(importPaths))
                .replace("@entity_type", functionalEntityType.getSimpleName())
                .replace("<!--@tg-entity-master-content-->",
                        format(""
                        + "<tg-master-menu id='menu' default-route='%s' menu-actions='[[menuItemActions]]' get-master-entity='[[_createContextHolderForEmbeddedViews]]'>"
                        + menuItemActionsDom
                        + menuItemViewsDom
                        + "</tg-master-menu>",
                        this.menuItemActions.get(0).functionalEntity.get().getSimpleName()))
                .replace("//@ready-callback", format("this.menuItemActions = [%s];\n", jsMenuItemActionObjects)) // 
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
    public Optional<Class<? extends IValueMatcherWithContext<F, ?>>> matcherTypeFor(final String propName) {
        return Optional.empty();
    }

}
