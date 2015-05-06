package ua.com.fielden.platform.web.test.server;

import java.util.List;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.sample.domain.MiTgFetchProviderTestEntity;
import ua.com.fielden.platform.sample.domain.MiTgPersistentEntityWithProperties;
import ua.com.fielden.platform.ui.config.MainMenuItem;
import ua.com.fielden.platform.ui.config.api.IMainMenuStructureBuilder;
import ua.com.fielden.platform.ui.config.controller.mixin.MainMenuStructureFactory;

import com.google.inject.Inject;

/**
 * Application specific main menu structure.
 *
 * @author TG Team
 *
 */
public class TemplateMainMenu implements IMainMenuStructureBuilder {

    private final MainMenuStructureFactory builder;

    @Inject
    public TemplateMainMenu(final EntityFactory factory) {
        builder = new MainMenuStructureFactory(factory);
        builder
                .push(MiTgPersistentEntityWithProperties.class.getName())
                .push(MiTgFetchProviderTestEntity.class.getName())
                .pop();
    }

    @Override
    public List<MainMenuItem> build() {
        return builder.build();
    }

}
