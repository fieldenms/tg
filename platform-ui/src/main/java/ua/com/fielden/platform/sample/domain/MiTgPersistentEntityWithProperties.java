package ua.com.fielden.platform.sample.domain;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.impl.CentreManagerConfigurator;
import ua.com.fielden.platform.swing.menu.ITreeMenuItemVisibilityProvider;
import ua.com.fielden.platform.swing.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.swing.menu.TreeMenuWithTabs;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.swing.review.report.centre.factory.EntityCentreFactoryBinder;

import com.google.inject.Injector;

@EntityType(TgPersistentEntityWithProperties.class)
public class MiTgPersistentEntityWithProperties extends MiWithConfigurationSupport<TgPersistentEntityWithProperties> {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(MiTgPersistentEntityWithProperties.class);

    private static final String caption = "Tg Persistent Entity With Properties";
    private static final String description = "<html>" + "<h3>Tg Persistent Entity With Properties Centre</h3>"
            + //
            "A facility to query Tg Persistent Entity With Properties information.</html>";

    @SuppressWarnings("unchecked")
    public MiTgPersistentEntityWithProperties(final TreeMenuWithTabs<?> treeMenu, final Injector injector, final ITreeMenuItemVisibilityProvider visibilityProvider) {
        super(caption, description, treeMenu, injector.getInstance(EntityCentreFactoryBinder.class), visibilityProvider, MiTgPersistentEntityWithProperties.class, injector.getInstance(IGlobalDomainTreeManager.class));
    }

    private static CentreManagerConfigurator createCentreConfigurator() {
        return new CentreManagerConfigurator(TgPersistentEntityWithProperties.class) {
            @Override
            public ICentreDomainTreeManagerAndEnhancer configCentre(final ICentreDomainTreeManagerAndEnhancer cdtme) {
                logger.debug("Started creation of default Person config.");
                //Selection criteria
                // addCriteria(cdtme, "").// empty means entity itself, which gets represented by key
                addCriteria(cdtme, "desc").
                        addCriteria(cdtme, "critOnlyEntityProp").
                        addCriteria(cdtme, "integerProp");
                logger.debug("\tAdded criteria.");

                cdtme.getFirstTick().setValue(root(), "critOnlyEntityProp", null);
                cdtme.getFirstTick().setValue(root(), "integerProp", 3);
                cdtme.getFirstTick().setValue2(root(), "integerProp", 9);

                //Main result
                addColumn(cdtme, ""); // .addColumn(cdtme, "desc").addColumn(cdtme, "username").addColumn(cdtme, "contractor").addColumn(cdtme, "manager");
                logger.debug("\tAdded result columns.");

                // addTotal(cdtme, "Person Count", "Person Count.", "COUNT(SELF)", "SELF");

                logger.debug("Ended creation of default TgPersistentEntityWithProperties config.");
                return cdtme;
            }
        };
    }
}