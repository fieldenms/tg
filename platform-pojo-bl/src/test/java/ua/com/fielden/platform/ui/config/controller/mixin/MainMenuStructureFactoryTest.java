package ua.com.fielden.platform.ui.config.controller.mixin;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;
import ua.com.fielden.platform.ui.config.MainMenuItem;

import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * Test correct construction of the main menu structure using {@link MainMenuStructureFactory}.
 *
 * @author TG Team
 *
 */
public class MainMenuStructureFactoryTest {

    private final Module module = new CommonTestEntityModuleWithPropertyFactory();
    private final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();

    @Test
    public void test_menu_construction() {
	final String menuItemName = "ua.com.fielden.platform.swing.menu.TreeMenuItem";
	final MainMenuStructureFactory builder = injector.getInstance(MainMenuStructureFactory.class)
	.push(menuItemName)
	     .push(menuItemName).pop()
	.pop()
	.push(menuItemName)
	     .push(menuItemName).pop()
	     .push(menuItemName).pop()
	.pop();

	final List<MainMenuItem> menu = builder.build();
	assertEquals("Incorrect number of first level items", 2, menu.size());
	assertEquals("Incorrect number of sub items of the first item", 1, menu.get(0).getChildren().size());
	assertEquals("Incorrect number of sub items of the second item", 2, menu.get(1).getChildren().size());
    }
}
