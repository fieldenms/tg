package ua.com.fielden.platform.web.test.server;

import java.util.List;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.ui.config.MainMenuItem;
import ua.com.fielden.platform.ui.config.api.IMainMenuStructureBuilder;
import ua.com.fielden.platform.ui.config.controller.mixin.MainMenuStructureFactory;
import ua.com.fielden.platform.ui.menu.sample.MiDeletionTestEntity;
import ua.com.fielden.platform.ui.menu.sample.MiDetailsCentre;
import ua.com.fielden.platform.ui.menu.sample.MiTgEntityWithPropertyDependency;
import ua.com.fielden.platform.ui.menu.sample.MiTgFetchProviderTestEntity;
import ua.com.fielden.platform.ui.menu.sample.MiTgPersistentEntityWithProperties;
import ua.com.fielden.platform.ui.menu.sample.MiTgPersistentEntityWithProperties1;
import ua.com.fielden.platform.ui.menu.sample.MiTgPersistentEntityWithProperties2;
import ua.com.fielden.platform.ui.menu.sample.MiTgPersistentEntityWithProperties3;
import ua.com.fielden.platform.ui.menu.sample.MiTgPersistentEntityWithProperties4;
import ua.com.fielden.platform.ui.menu.sample.MiUser;
import ua.com.fielden.platform.ui.menu.sample.MiUserRole;

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
                .push(MiTgPersistentEntityWithProperties1.class.getName())
                .push(MiTgPersistentEntityWithProperties2.class.getName())
                .push(MiTgPersistentEntityWithProperties3.class.getName())
                .push(MiTgPersistentEntityWithProperties4.class.getName())
                .push(MiDetailsCentre.class.getName())
                .push(MiTgEntityWithPropertyDependency.class.getName())
                .push(MiUser.class.getName())
                .push(MiTgFetchProviderTestEntity.class.getName())
                .push(MiUserRole.class.getName())
                .push(MiDeletionTestEntity.class.getName())
                .pop();
    }

    @Override
    public List<MainMenuItem> build() {
        return builder.build();
    }

}
