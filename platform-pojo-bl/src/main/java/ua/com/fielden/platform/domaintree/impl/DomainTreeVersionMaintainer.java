package ua.com.fielden.platform.domaintree.impl;

import java.util.Map.Entry;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.domaintree.centre.impl.CentreDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.impl.CentreDomainTreeManager.AddToCriteriaTickManager;
import ua.com.fielden.platform.domaintree.centre.impl.CentreDomainTreeManager.AddToResultTickManager;
import ua.com.fielden.platform.domaintree.centre.impl.CentreDomainTreeManager0.AddToCriteriaTickManager0;
import ua.com.fielden.platform.domaintree.centre.impl.CentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.impl.CentreDomainTreeManagerAndEnhancer0;
import ua.com.fielden.platform.domaintree.centre.impl.CentreDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.centre.impl.LocatorDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.impl.LocatorDomainTreeManager.AddToCriteriaTickManagerForLocator;
import ua.com.fielden.platform.domaintree.centre.impl.LocatorDomainTreeManager0.AddToCriteriaTickManagerForLocator0;
import ua.com.fielden.platform.domaintree.centre.impl.LocatorDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.impl.LocatorDomainTreeManagerAndEnhancer0;
import ua.com.fielden.platform.domaintree.centre.impl.LocatorDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.master.impl.MasterDomainTreeManager;
import ua.com.fielden.platform.domaintree.master.impl.MasterDomainTreeManager0;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.api.ISerialiser0;
import ua.com.fielden.platform.ui.config.EntityCentreConfig;
import ua.com.fielden.platform.ui.config.EntityLocatorConfig;
import ua.com.fielden.platform.ui.config.EntityMasterConfig;
import ua.com.fielden.platform.ui.config.api.IEntityCentreConfig;
import ua.com.fielden.platform.ui.config.api.IEntityLocatorConfig;
import ua.com.fielden.platform.ui.config.api.IEntityMasterConfig;
import ua.com.fielden.platform.utils.Pair;

/**
 * This utility class is responsible in maintaining configuration versions.
 * <p>
 * 1. Version of Domain Tree serialisation classes.<br>
 * a) 0 Version -- DomainTreeEnhanser with byte arrays serialised<br>
 * b) CURRENT Version -- DomainTreeEnhanser with {@link CalculatedPropertyInfo} serialised<br>
 * <br>
 * 2. Version of Original types<br>
 * a) TODO property adding<br>
 * b) TODO property removal<br>
 *
 * @author TG Team
 *
 */
public class DomainTreeVersionMaintainer extends AbstractDomainTree {
    private final static Logger logger = Logger.getLogger(DomainTreeVersionMaintainer.class);

    // private final ISerialiser serialiser = getSerialiser();
    private final ISerialiser0 serialiser0;
    private final IEntityLocatorConfig elcController;
    private final IEntityCentreConfig eccController;
    private final IEntityMasterConfig emcController;

    public DomainTreeVersionMaintainer(final ISerialiser serialiser, final ISerialiser0 serialiser0, final IEntityLocatorConfig entityLocatorConfigController, final IEntityCentreConfig entityCentreConfigController, final IEntityMasterConfig entityMasterConfigController) {
        super(serialiser);
        this.serialiser0 = serialiser0;

        //	System.err.println("\tserialiser0 == " + serialiser0);
        //	System.err.println("\tserialiser == " + serialiser);

        this.elcController = entityLocatorConfigController;
        this.eccController = entityCentreConfigController;
        this.emcController = entityMasterConfigController;
    }

    public static Pair<LocatorDomainTreeManagerAndEnhancer, Boolean> retrieveLocator(final String elcKey, final byte[] array, final ISerialiser serialiser, final ISerialiser serialiser0)
            throws Exception {
        logger.debug("Started maintaining the version of default locator instance for [" + elcKey + "].");
        try {
            // try to deserialise with a current version of serialiser
            final LocatorDomainTreeManagerAndEnhancer ldtmae = serialiser.deserialise(array, LocatorDomainTreeManagerAndEnhancer.class);
            logger.debug("\tA default locator instance for [" + elcKey + "] is of CURRENT (1) version and has been succesfully deserialised.");
            // all is okay -- the version is current
            logger.debug("Ended maintaining the version of default locator instance for [" + elcKey + "].");
            return new Pair<LocatorDomainTreeManagerAndEnhancer, Boolean>(ldtmae, false);
        } catch (final Exception e) {
            System.out.println("EXCEPTION:" + e.getMessage());
            //e.printStackTrace();
            // smth is wrong -- try previous versions
            final LocatorDomainTreeManagerAndEnhancer0 ldtmae0 = serialiser0.deserialise(array, LocatorDomainTreeManagerAndEnhancer0.class);
            logger.warn("\tA default locator instance for [" + elcKey + "] is of OLD (0) version -- trying to convert it to CURRENT (1) version.");

            // all is okay -- the version is 0 -- convert it to current and return
            final LocatorDomainTreeManagerAndEnhancer ldtmae = convert(ldtmae0, serialiser);
            logger.warn("\tA default locator instance for [" + elcKey + "] has been converted succesfully to CURRENT (1) version from OLD (0) version.");

            return new Pair<LocatorDomainTreeManagerAndEnhancer, Boolean>(ldtmae, true);
        }
    }

    public LocatorDomainTreeManagerAndEnhancer maintainLocatorVersion(final EntityLocatorConfig downloadedElc) throws Exception {
        final Pair<LocatorDomainTreeManagerAndEnhancer, Boolean> ldtmaeAndShouldBePromoted = retrieveLocator(downloadedElc.toString(), downloadedElc.getConfigBody(), getSerialiser(), serialiser0);
        if (ldtmaeAndShouldBePromoted.getValue()) {
            // the converted version should be promoted to the cloud
            downloadedElc.setConfigBody(getSerialiser().serialise(ldtmaeAndShouldBePromoted.getKey())); // serialise with CURRENT version of serialiser
            final EntityLocatorConfig updatedElc = elcController.save(downloadedElc);
            logger.warn("\tA default locator instance, converted to CURRENT (1) version, for [" + downloadedElc + "] has been succesfully saved (promoted to the cloud).");

            logger.debug("Ended maintaining the version of default locator instance for [" + downloadedElc + "].");
            return maintainLocatorVersion(updatedElc);
        } else {
            logger.debug("Ended maintaining the version of default locator instance for [" + downloadedElc + "].");
            return ldtmaeAndShouldBePromoted.getKey();
        }
    }

    private static LocatorDomainTreeManagerAndEnhancer convert(final LocatorDomainTreeManagerAndEnhancer0 ldtmae0, final ISerialiser serialiser) {
        final AddToCriteriaTickManagerForLocator0 atctmfl0 = (AddToCriteriaTickManagerForLocator0) ldtmae0.base().getFirstTick();
        return //
        new LocatorDomainTreeManagerAndEnhancer(//
        serialiser, //
        new LocatorDomainTreeManager(serialiser, (LocatorDomainTreeRepresentation) ldtmae0.base().getRepresentation(), new AddToCriteriaTickManagerForLocator(atctmfl0.checkedProperties(), serialiser, atctmfl0.propertiesValues1(), atctmfl0.propertiesValues2(), atctmfl0.propertiesExclusive1(), atctmfl0.propertiesExclusive2(), atctmfl0.propertiesDatePrefixes(), atctmfl0.propertiesDateMnemonics(), atctmfl0.propertiesAndBefore(), atctmfl0.propertiesOrNulls(), atctmfl0.propertiesNots(), atctmfl0.columnsNumber(), new LocatorManager(serialiser, ldtmae0.base().getRepresentation().rootTypes(), convert(atctmfl0.locatorManager().persistentLocators(), serialiser)), atctmfl0.propertiesMetaValuePresences()), (AddToResultTickManager) ldtmae0.base().getSecondTick(), ldtmae0.base().isRunAutomatically(), ldtmae0.base().isUseForAutocompletion(), ldtmae0.base().getSearchBy()), new DomainTreeEnhancer(serialiser, ldtmae0.getEnhancer().originalAndEnhancedRootTypesAndArrays(), ldtmae0.getEnhancer().calculatedProperties(), ldtmae0.getEnhancer().customProperties()), ldtmae0.persistentAnalyses(), ldtmae0.currentAnalyses(), ldtmae0.freezedAnalyses());
    }

    public static Pair<CentreDomainTreeManagerAndEnhancer, Boolean> retrieveCentre(final String eccKey, final byte[] array, final ISerialiser serialiser, final ISerialiser0 serialiser0)
            throws Exception {
        logger.debug("Started maintaining the version of centre instance for [" + eccKey + "].");
        try {
            // try to deserialise with a current version of serialiser
            final CentreDomainTreeManagerAndEnhancer cdtmae = serialiser.deserialise(array, CentreDomainTreeManagerAndEnhancer.class);
            logger.debug("\tA centre instance for [" + eccKey + "] is of CURRENT (1) version and has been succesfully deserialised.");
            // all is okay -- the version is current
            logger.debug("Ended maintaining the version of centre instance for [" + eccKey + "].");
            return new Pair<CentreDomainTreeManagerAndEnhancer, Boolean>(cdtmae, false);
        } catch (final Exception e) {
            logger.debug("Ended maintaining the version of centre instance for [" + eccKey + "] -- the exception has occured.");
            throw e;
//            // smth is wrong -- try previous versions
//            final CentreDomainTreeManagerAndEnhancer0 cdtmae0 = serialiser0.deserialise(array, CentreDomainTreeManagerAndEnhancer0.class);
//            logger.warn("\tA centre instance for [" + eccKey + "] is of OLD (0) version -- trying to convert it to CURRENT (1) version.");
//
//            // all is okay -- the version is 0 -- convert it to current and return
//            final CentreDomainTreeManagerAndEnhancer cdtmae = convert(cdtmae0, serialiser);
//            logger.warn("\tA centre instance for [" + eccKey + "] has been converted succesfully to CURRENT (1) version from OLD (0) version.");
//
//            return new Pair<CentreDomainTreeManagerAndEnhancer, Boolean>(cdtmae, true);
        }
    }

    public CentreDomainTreeManagerAndEnhancer maintainCentreVersion(final EntityCentreConfig downloadedEcc) throws Exception {
        final Pair<CentreDomainTreeManagerAndEnhancer, Boolean> cdtmaeAndShouldBePromoted = retrieveCentre(downloadedEcc.toString(), downloadedEcc.getConfigBody(), getSerialiser(), serialiser0);
        // populate Id and Version to be able to determine staleness of the centre
        cdtmaeAndShouldBePromoted.getKey().setSavedEntityId(downloadedEcc.getId());
        cdtmaeAndShouldBePromoted.getKey().setSavedEntityVersion(downloadedEcc.getVersion());
        if (cdtmaeAndShouldBePromoted.getValue()) {
            // the converted version should be promoted to the cloud
            downloadedEcc.setConfigBody(getSerialiser().serialise(cdtmaeAndShouldBePromoted.getKey())); // serialise with CURRENT version of serialiser
            final EntityCentreConfig updatedEcc = eccController.save(downloadedEcc);
            logger.warn("\tA centre instance, converted to CURRENT (1) version, for [" + downloadedEcc + "] has been succesfully saved (promoted to the cloud).");

            logger.debug("Ended maintaining the version of centre instance for [" + downloadedEcc + "].");
            return maintainCentreVersion(updatedEcc);
        } else {
            logger.debug("Ended maintaining the version of centre instance for [" + downloadedEcc + "].");
            return cdtmaeAndShouldBePromoted.getKey();
        }
    }

    private static CentreDomainTreeManagerAndEnhancer convert(final CentreDomainTreeManagerAndEnhancer0 cdtmae0, final ISerialiser serialiser) {
        final AddToCriteriaTickManager0 atctm0 = (AddToCriteriaTickManager0) cdtmae0.base().getFirstTick();
        return //
        new CentreDomainTreeManagerAndEnhancer(//
        serialiser, //
        new CentreDomainTreeManager(serialiser, (CentreDomainTreeRepresentation) cdtmae0.base().getRepresentation(), new AddToCriteriaTickManager(atctm0.checkedProperties(), serialiser, atctm0.propertiesValues1(), atctm0.propertiesValues2(), atctm0.propertiesExclusive1(), atctm0.propertiesExclusive2(), atctm0.propertiesDatePrefixes(), atctm0.propertiesDateMnemonics(), atctm0.propertiesAndBefore(), atctm0.propertiesOrNulls(), atctm0.propertiesNots(), atctm0.columnsNumber(), new LocatorManager(serialiser, cdtmae0.base().getRepresentation().rootTypes(), convert(atctm0.locatorManager().persistentLocators(), serialiser)), atctm0.propertiesMetaValuePresences()), (AddToResultTickManager) cdtmae0.base().getSecondTick(), cdtmae0.base().isRunAutomatically()), new DomainTreeEnhancer(serialiser, cdtmae0.getEnhancer().originalAndEnhancedRootTypesAndArrays(), cdtmae0.getEnhancer().calculatedProperties(), cdtmae0.getEnhancer().customProperties()), cdtmae0.persistentAnalyses(), cdtmae0.currentAnalyses(), cdtmae0.freezedAnalyses());
    }

    public static Pair<MasterDomainTreeManager, Boolean> retrieveMaster(final String emcKey, final byte[] array, final ISerialiser serialiser, final ISerialiser0 serialiser0)
            throws Exception {
        logger.debug("Started maintaining the version of master instance for [" + emcKey + "].");
        try {
            // try to deserialise with a current version of serialiser
            final MasterDomainTreeManager mdtm = serialiser.deserialise(array, MasterDomainTreeManager.class);
            logger.debug("\tA master instance for [" + emcKey + "] is of CURRENT (1) version and has been succesfully deserialised.");
            // all is okay -- the version is current
            logger.debug("Ended maintaining the version of master instance for [" + emcKey + "].");
            return new Pair<MasterDomainTreeManager, Boolean>(mdtm, false);
        } catch (final Exception e) {
            System.out.println("EXCEPTION:" + e.getMessage());
            //e.printStackTrace();
            // smth is wrong -- try previous versions
            final MasterDomainTreeManager0 mdtm0 = serialiser0.deserialise(array, MasterDomainTreeManager0.class);
            logger.warn("\tA master instance for [" + emcKey + "] is of OLD (0) version -- trying to convert it to CURRENT (1) version.");

            // all is okay -- the version is 0 -- convert it to current and return
            final MasterDomainTreeManager mdtm = convert(mdtm0, serialiser);
            logger.warn("\tA master instance for [" + emcKey + "] has been converted succesfully to CURRENT (1) version from OLD (0) version.");

            return new Pair<MasterDomainTreeManager, Boolean>(mdtm, true);
        }
    }

    public MasterDomainTreeManager maintainMasterVersion(final EntityMasterConfig downloadedEmc) throws Exception {
        final Pair<MasterDomainTreeManager, Boolean> mdtmAndShouldBePromoted = retrieveMaster(downloadedEmc.toString(), downloadedEmc.getConfigBody(), getSerialiser(), serialiser0);
        if (mdtmAndShouldBePromoted.getValue()) {
            // the converted version should be promoted to the cloud
            downloadedEmc.setConfigBody(getSerialiser().serialise(mdtmAndShouldBePromoted.getKey())); // serialise with CURRENT version of serialiser
            final EntityMasterConfig updatedEmc = emcController.save(downloadedEmc);
            logger.warn("\tA master instance, converted to CURRENT (1) version, for [" + downloadedEmc + "] has been succesfully saved (promoted to the cloud).");

            logger.debug("Ended maintaining the version of master instance for [" + downloadedEmc + "].");
            return maintainMasterVersion(updatedEmc);
        } else {
            logger.debug("Ended maintaining the version of master instance for [" + downloadedEmc + "].");
            return mdtmAndShouldBePromoted.getKey();
        }
    }

    private static MasterDomainTreeManager convert(final MasterDomainTreeManager0 mdtm0, final ISerialiser serialiser) {
        return new MasterDomainTreeManager(serialiser, new LocatorManager(serialiser, mdtm0.rootTypes(), convert(mdtm0.locatorManager().persistentLocators(), serialiser)));
    }

    private static EnhancementPropertiesMap<LocatorDomainTreeManagerAndEnhancer> convert(final EnhancementPropertiesMap<LocatorDomainTreeManagerAndEnhancer0> persistentLocators, final ISerialiser serialiser) {
        final EnhancementPropertiesMap<LocatorDomainTreeManagerAndEnhancer> map = AbstractDomainTree.<LocatorDomainTreeManagerAndEnhancer> createPropertiesMap();
        for (final Entry<Pair<Class<?>, String>, LocatorDomainTreeManagerAndEnhancer0> entry : persistentLocators.entrySet()) {
            map.put(entry.getKey(), convert(entry.getValue(), serialiser));
        }
        if (!map.isEmpty()) {
            logger.warn("\t\tConverted " + map.size() + " locators to CURRENT (1) version.");
        }
        return map;
    }
}
