package ua.com.fielden.platform.ui.menu.sample;

import static org.apache.logging.log4j.LogManager.getLogger;

import org.apache.logging.log4j.Logger;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.impl.CentreManagerConfigurator;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.sample.domain.TgPersistentEntityWithProperties;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;

@EntityType(TgPersistentEntityWithProperties.class)
public class MiTgPersistentEntityWithProperties extends MiWithConfigurationSupport<TgPersistentEntityWithProperties> {
    private static final Logger logger = getLogger(MiTgPersistentEntityWithProperties.class);

    private static final String caption = "Tg Persistent Entity With Properties";
    private static final String description = "<html>" + "<h3>Tg Persistent Entity With Properties Centre</h3>"
            + //
            "A facility to query Tg Persistent Entity With Properties information.</html>";

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
                // cdtme.getFirstTick().setOrNull(root(), "integerProp", true);
                // cdtme.getFirstTick().setValue2(root(), "integerProp", 7);
                // cdtme.getFirstTick().setExclusive2(root(), "integerProp", true);
                addCriteria(cdtme, "entityProp");
                addCriteria(cdtme, "bigDecimalProp");
                addCriteria(cdtme, "booleanProp");
                addCriteria(cdtme, "dateProp");
                addCriteria(cdtme, "compositeProp");
                addCriteria(cdtme, "critOnlyDateProp");

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
                addColumn(cdtme, "", 80); // empty means entity itself, which gets represented by key
                addColumn(cdtme, "desc", 200);

                addColumn(cdtme, "integerProp", 60);
                addColumn(cdtme, "bigDecimalProp", 60);
                addColumn(cdtme, "entityProp", 100);
                addColumn(cdtme, "booleanProp", 50);
                addColumn(cdtme, "dateProp", 100);
                addColumn(cdtme, "compositeProp", 100);
                addColumn(cdtme, "stringProp", 50);

                logger.debug("\tAdded result columns.");

                // addTotal(cdtme, "Person Count", "Person Count.", "COUNT(SELF)", "SELF");

                logger.debug("Ended creation of default TgPersistentEntityWithProperties config.");
                return cdtme;
            }
        };
    }
}