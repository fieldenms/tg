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
                logger.debug("Started creation of default TgPersistentEntityWithProperties config.");
                // Selection criteria
                cdtme.getFirstTick().setColumnsNumber(3);

                addCriteria(cdtme, ""); // empty means entity itself, which gets represented by key
                addCriteria(cdtme, "desc");

                addCriteria(cdtme, "integerProp");
                cdtme.getFirstTick().setOrNull(root(), "integerProp", true);
                cdtme.getFirstTick().setValue2(root(), "integerProp", 7);
                cdtme.getFirstTick().setExclusive2(root(), "integerProp", true);
                addCriteria(cdtme, "bigDecimalProp");
                addCriteria(cdtme, "entityProp");
                addCriteria(cdtme, "booleanProp");
                addCriteria(cdtme, "dateProp");
                addCriteria(cdtme, "compositeProp");

                addCriteria(cdtme, "critOnlyEntityProp");
                logger.debug("\tAdded criteria.");

                //                cdtme.getFirstTick().setValue(root(), "", entityVal("KE*", "AB*"));
                //                cdtme.getFirstTick().setValue(root(), "desc", "de*");
                //
                //                // cdtme.getFirstTick().setValue(root(), "integerProp", 3);
                //                cdtme.getFirstTick().setValue2(root(), "integerProp", 9);
                //                cdtme.getFirstTick().setValue(root(), "bigDecimalProp", BigDecimal.TEN);
                //                // cdtme.getFirstTick().setValue2(root(), "bigDecimalProp", BigDecimal.ONE);
                //                cdtme.getFirstTick().setValue(root(), "entityProp", entityVal("KE*", "AB*"));
                //                cdtme.getFirstTick().setValue(root(), "booleanProp", false);
                //                // cdtme.getFirstTick().setValue2(root(), "booleanProp", false);
                //                cdtme.getFirstTick().setValue(root(), "dateProp", dateVal("2014-12-12 00:00:00"));
                //                // cdtme.getFirstTick().setValue2(root(), "dateProp", dateVal("2014-12-12 00:00:00"));
                //                cdtme.getFirstTick().setValue(root(), "compositeProp", entityVal("DEFAULT_KE*"));
                //
                //                cdtme.getFirstTick().setValue(root(), "critOnlyEntityProp", null);

                // Main result
                addColumn(cdtme, ""); // empty means entity itself, which gets represented by key
                addColumn(cdtme, "desc");

                addColumn(cdtme, "integerProp");
                addColumn(cdtme, "bigDecimalProp");
                addColumn(cdtme, "entityProp");
                addColumn(cdtme, "booleanProp");
                addColumn(cdtme, "dateProp");
                addColumn(cdtme, "compositeProp");

                logger.debug("\tAdded result columns.");

                // addTotal(cdtme, "Person Count", "Person Count.", "COUNT(SELF)", "SELF");

                logger.debug("Ended creation of default TgPersistentEntityWithProperties config.");
                return cdtme;
            }
        };
    }
}