package ua.com.fielden.platform.swing.menu;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;
import ua.com.fielden.platform.ui.config.MainMenuItem;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Test correct construction of the main menu structure using {@link LocalMainMenuStructureBuilder}.
 *
 * @author TG Team
 *
 */
public class MainMenuStructureBuilderTest {

    private Injector injector = Guice.createInjector(new CommonTestEntityModuleWithPropertyFactory());

    @Test
    public void test_menu_construction() {
	final LocalMainMenuStructureBuilder builder = injector.getInstance(LocalMainMenuStructureBuilder.class)
	.push(TreeMenuItem.class)
	     .push(TreeMenuItem.class).pop()
	.pop()
	.push(TreeMenuItem.class)
	     .push(TreeMenuItem.class).pop()
	     .push(TreeMenuItem.class).pop()
	.pop();

	final List<MainMenuItem> menu = builder.build();
	assertEquals("Incorrect number of first level items", 2, menu.size());
	assertEquals("Incorrect number of sub items of the first item", 1, menu.get(0).getChildren().size());
	assertEquals("Incorrect number of sub items of the second item", 2, menu.get(1).getChildren().size());
    }
}
