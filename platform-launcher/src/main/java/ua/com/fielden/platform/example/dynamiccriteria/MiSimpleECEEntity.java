package ua.com.fielden.platform.example.dynamiccriteria;

import ua.com.fielden.platform.example.dynamiccriteria.entities.SimpleECEEntity;
import ua.com.fielden.platform.swing.menu.ITreeMenuItemVisibilityProvider;
import ua.com.fielden.platform.swing.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.swing.menu.TreeMenuWithTabs;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.swing.review.report.centre.configuration.DefaultCentreConfigurationFactory;

import com.google.inject.Injector;

@EntityType(SimpleECEEntity.class)
public class MiSimpleECEEntity extends MiWithConfigurationSupport<SimpleECEEntity> {

    private static final long serialVersionUID = 8348286315949258950L;

    public MiSimpleECEEntity(final TreeMenuWithTabs<?> treeMenu, final Injector injector, final ITreeMenuItemVisibilityProvider visibilityProvider) {
	super("Simple entity", "Simple entity description", injector, treeMenu, new DefaultCentreConfigurationFactory<SimpleECEEntity>(), visibilityProvider, MiSimpleECEEntity.class);
    }

}
