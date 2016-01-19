package ua.com.fielden.platform.web.test.server;

import java.util.List;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.sample.domain.MiDetailsCentre;
import ua.com.fielden.platform.sample.domain.MiTgEntityWithPropertyDependency;
import ua.com.fielden.platform.sample.domain.MiTgFetchProviderTestEntity;
import ua.com.fielden.platform.sample.domain.MiTgPersistentEntityWithProperties;
import ua.com.fielden.platform.sample.domain.MiTgPersistentEntityWithProperties1;
import ua.com.fielden.platform.sample.domain.MiTgPersistentEntityWithProperties2;
import ua.com.fielden.platform.sample.domain.MiTgPersistentEntityWithProperties3;
import ua.com.fielden.platform.sample.domain.MiTgPersistentEntityWithProperties4;
import ua.com.fielden.platform.sample.domain.MiUser;
import ua.com.fielden.platform.sample.domain.MiUserRole;
import ua.com.fielden.platform.ui.config.MainMenuItem;
import ua.com.fielden.platform.ui.config.api.IMainMenuStructureBuilder;
import ua.com.fielden.platform.ui.config.controller.mixin.MainMenuStructureFactory;

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
                .pop();
    }

    @Override
    public List<MainMenuItem> build() {
        return builder.build();
    }

}
