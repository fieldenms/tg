package ua.com.fielden.platform.swing.menu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.ui.config.MainMenuItem;

import com.google.inject.Inject;

/**
 * An factory for constructing structure of the application main menu based on concrete classes implementing corresponding menu items.
 * <p>
 * In order to construct menu like this:
 * <pre>
 *    |--- Item 1
 *    |      |--- Item 1.1
 *    |--- Item 2
 *    |      | --- Item 2.1
 *    |      | --- Item 2.2
 * </pre>
 *
 * One needs to invoke the following code:
 * <pre>
 *  MainMenuStructureBuilder builder = injector.getInstance(MainMenuStructureBuilder.class);
 *
 *  builder.push(Item1.class);
 *  builder.push(Item1_1.class);
 *  builder.pop(); // get to the level of Item 1
 *  builder.pop(); // get to the level of the root
 *  builder.push(Item2.class);
 *  builder.push(Item2_1.class);
 *  builder.pop(); // get to the level of Item 2
 *  builder.push(Item2_2.class);
 *  builder.pop(); // get to the level of Item 2
 *  builder.pop(); // get to the level of the root
 * </pre>
 *
 * The code above could be constructed by reusing the fluent interface capability of the API in order to better represent the hierarchical menu structure.
 * <pre>
 *  MainMenuStructureBuilder builder = injector.getInstance(MainMenuStructureBuilder.class)
 *  .push(Item1.class)
 *       .push(Item1_1.class).pop()
 *  .pop()
 *  .push(Item2.class)
 *       .push(Item2_1.class).pop()
 *       .push(Item2_2.class).pop()
 *  .pop();
 * </pre>
 *
 * @author TG Team
 *
 */
public final class LocalMainMenuStructureBuilder {

    private long id = 0;
    private int order = 0;
    private final EntityFactory factory;
    private List<MainMenuItem> menuItems = new ArrayList<MainMenuItem>();
    private MainMenuItem currentItem = null;

    @Inject
    public LocalMainMenuStructureBuilder(final EntityFactory factory) {
	this.factory = factory;
    }

    /**
     * Creates new {@link MainMenuItem} instance and adds it one level deeper into the hierarchy.
     * Makes the created item the current one.
     *
     * @param miType
     * @return
     */
    public LocalMainMenuStructureBuilder push(final Class<? extends TreeMenuItem> miType) {
	final MainMenuItem item = factory.newEntity(MainMenuItem.class, ++id, miType.getName());
	if (currentItem == null) { // first level item is being added (i.e. under the root)
	    // instantiate item
	    item.setParent(null);
	    item.setOrder(++order);
	    item.setTitle(""); // bit problematic to set
	    menuItems.add(item);
	} else {
	    final int subOrder = currentItem.getChildren().size();
	    currentItem.addChild(item);
	    item.setOrder(subOrder);
	    item.setTitle(""); // bit problematic to set
	}
	currentItem = item;

	return this;
    }

    /**
     * Moves one level up in the menu hierarchy in relation to the current item.
     *
     * @return
     */
    public LocalMainMenuStructureBuilder pop() {
	if (currentItem == null) {
	    throw new IllegalStateException("The current menu level is the highest. No where to pop.");
	}

	currentItem = currentItem.getParent();

	return this;
    }

    /**
     * Returns the constructed menu.
     *
     * @return
     */
    public List<MainMenuItem> build() {
	return Collections.unmodifiableList(menuItems);
    }
}
