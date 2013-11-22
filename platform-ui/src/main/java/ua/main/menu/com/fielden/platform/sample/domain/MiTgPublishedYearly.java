package ua.main.menu.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.sample.domain.TgPublishedYearly;
import ua.com.fielden.platform.swing.menu.ITreeMenuItemVisibilityProvider;
import ua.com.fielden.platform.swing.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.swing.menu.TreeMenuWithTabs;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.swing.review.report.centre.factory.EntityCentreFactoryBinder;

import com.google.inject.Injector;

/**
 * Main menu item representing an entity centre for {@link TgPublishedYearly}.
 *
 * @author Developers
 *
 */
@EntityType(TgPublishedYearly.class)
public class MiTgPublishedYearly extends MiWithConfigurationSupport<TgPublishedYearly> {

    private static final String caption = TitlesDescsGetter.getEntityTitleAndDesc(TgPublishedYearly.class).getKey();
    private static final String description = "<html><h3>" + TitlesDescsGetter.getEntityTitleAndDesc(TgPublishedYearly.class).getKey() + " Centre</h3>" + //
    	                                     "Put your description here." + //
    	                                     "</html>";

    @SuppressWarnings("unchecked")
    public MiTgPublishedYearly(final TreeMenuWithTabs<?> treeMenu, final Injector injector, final ITreeMenuItemVisibilityProvider visibilityProvider) {
        super(caption, description, treeMenu,  injector.getInstance(EntityCentreFactoryBinder.class), visibilityProvider, MiTgPublishedYearly.class, injector.getInstance(IGlobalDomainTreeManager.class));
    }

}