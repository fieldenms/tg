package ua.com.fielden.platform.example.dynamiccriteria;

import java.util.List;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.ui.config.MainMenuItem;
import ua.com.fielden.platform.ui.config.api.IMainMenuStructureBuilder;
import ua.com.fielden.platform.ui.config.controller.mixin.MainMenuStructureFactory;

import com.google.inject.Inject;

public class TemplateMainMenu implements IMainMenuStructureBuilder {

    private final MainMenuStructureFactory builder;

    @Inject
    public TemplateMainMenu(final EntityFactory factory) {
        builder = new MainMenuStructureFactory(factory);
        builder.push(MiSimpleECEEntity.class.getName()).pop().push(MiSimpleCompositeEntity.class.getName()).pop();
    }

    @Override
    public List<MainMenuItem> build() {
        return builder.build();
    }
}
