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

    public MasterWithMenu(final Class<T> entityType, final Class<F> functionalEntityType, final List<EntityActionConfig> menuItemActions, final int defaultMenuItemIndex) {
        logger.debug(format("Generating master with menu for entity %s, invoked by functional entity %s.", entityType.getSimpleName(), functionalEntityType.getSimpleName()));
        if (defaultMenuItemIndex < 0 || defaultMenuItemIndex >= menuItemActions.size()) {
            throw new IllegalArgumentException(format("The default menu item index %s is outside of the range for the provided menu items.", defaultMenuItemIndex));
        }
        
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
        final DomContainer menuItemsDom = new DomContainer();
        
        for (final FunctionalActionElement el : menuItemActionsElements) {
            importPaths.add(el.importPath());
            menuItemActionsDom.add(el.render());
            jsMenuItemActionObjects.append(el.createActionObject() + ",\n");
            menuItemViewsDom.add(
                    new DomElement("tg-master-menu-item-section")
                    .attr("id", "mi" + el.numberOfAction)
                    .attr("class", "menu-item-section")
                    .attr("data-route", el.getDataRoute())
                    .attr("title", el.getShortDesc()));
            menuItemsDom.add(
                    new DomElement("paper-item").attr("class", "menu-item").attr("data-route", el.getDataRoute())
                    .add(new DomElement("iron-icon").attr("icon", el.getIcon()).attr("style", "margin-right: 10px"))
                    .add(new DomElement("span").add(new InnerTextElement(el.getShortDesc())))
                    );
        }

        // generate the final master with menu
        final String entityMasterStr = ResourceLoader.getText("ua/com/fielden/platform/web/master/tg-entity-master-template.html")
                .replace("<!--@imports-->", SimpleMasterBuilder.createImports(importPaths))
                .replace("@entity_type", functionalEntityType.getSimpleName())
                .replace("<!--@tg-entity-master-content-->",
                        format(""
                        + "<tg-master-menu\n"
                        + "    id='menu'\n"
                        + "    default-route='%s'\n"
                        + "    menu-actions='[[menuItemActions]]'\n"
                        + "    uuid='[[uuid]]'\n"
                        + "    centre-uuid='[[centreUuid]]'\n"
                        + "    get-master-entity='[[_createContextHolderForEmbeddedViews]]'\n"
                        + "    refresh-compound-master='[[save]]'\n"
                        + "    augment-context-with-saved-entity='[[augmentContextWithSavedEntity]]'>\n"
                        + menuItemActionsDom + "\n"
                        + menuItemsDom + "\n"
                        + menuItemViewsDom + "\n"
                        + "</tg-master-menu>",
                        this.menuItemActions.get(defaultMenuItemIndex).functionalEntity.get().getSimpleName()))
                .replace("//@ready-callback", 
                        format("self.menuItemActions = [%s];\n"
                             + "self.$.menu.parent = self;\n"
                             + "self.canLeave = self.$.menu.canClose.bind(self.$.menu);\n", 
                             jsMenuItemActionObjects)) // 
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
