package ua.com.fielden.platform.example.dynamiccriteria;

import java.util.List;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.swing.menu.LocalMainMenuStructureBuilder;
import ua.com.fielden.platform.ui.config.MainMenuItem;
import ua.com.fielden.platform.ui.config.api.IMainMenuStructureBuilder;

import com.google.inject.Inject;

public class TemplateMainMenu implements IMainMenuStructureBuilder {

    private final LocalMainMenuStructureBuilder builder;

    @Inject
    public TemplateMainMenu(final EntityFactory factory) {
	builder = new LocalMainMenuStructureBuilder(factory);
	builder
	.push(MiSimpleECEEntity.class).pop()
	.push(MiSimpleCompositeEntity.class).pop();
    }

    @Override
    public List<MainMenuItem> build(final String username) {
	return builder.build();
    }
}
