package ua.com.fielden.platform.domaintree.impl;

import static java.lang.String.format;
import static ua.com.fielden.platform.utils.Pair.pair;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.domaintree.centre.impl.CentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.impl.LocatorDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.exceptions.DomainTreeException;
import ua.com.fielden.platform.domaintree.master.impl.MasterDomainTreeManager;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
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
@Deprecated
public class DomainTreeVersionMaintainer extends AbstractDomainTree {
    private final static Logger logger = Logger.getLogger(DomainTreeVersionMaintainer.class);

    // private final ISerialiser serialiser = getSerialiser();
    private final IEntityLocatorConfig elcController;
    private final IEntityCentreConfig eccController;
    private final IEntityMasterConfig emcController;

    public DomainTreeVersionMaintainer(final ISerialiser serialiser, final IEntityLocatorConfig entityLocatorConfigController, final IEntityCentreConfig entityCentreConfigController, final IEntityMasterConfig entityMasterConfigController) {
        super(serialiser);

        //	System.err.println("\tserialiser0 == " + serialiser0);
        //	System.err.println("\tserialiser == " + serialiser);

        this.elcController = entityLocatorConfigController;
        this.eccController = entityCentreConfigController;
        this.emcController = entityMasterConfigController;
    }

    public static Pair<LocatorDomainTreeManagerAndEnhancer, Boolean> retrieveLocator(final String elcKey, final byte[] array, final ISerialiser serialiser) {
        logger.debug("Started maintaining the version of default locator instance for [" + elcKey + "].");
        try {
            // try to deserialise with a current version of serialiser
            final LocatorDomainTreeManagerAndEnhancer ldtmae = serialiser.deserialise(array, LocatorDomainTreeManagerAndEnhancer.class);
            logger.debug("\tA default locator instance for [" + elcKey + "] is of CURRENT (1) version and has been succesfully deserialised.");
            // all is okay -- the version is current
            logger.debug("Ended maintaining the version of default locator instance for [" + elcKey + "].");
            return pair(ldtmae, false);
        } catch (final Exception e) {
            final String msg = format("Could not deserialise a default locator instance for [%s]", elcKey);
            logger.error(msg);
            throw new DomainTreeException(msg, e);
        }
    }

    public LocatorDomainTreeManagerAndEnhancer maintainLocatorVersion(final EntityLocatorConfig downloadedElc) throws Exception {
        final Pair<LocatorDomainTreeManagerAndEnhancer, Boolean> ldtmaeAndShouldBePromoted = retrieveLocator(downloadedElc.toString(), downloadedElc.getConfigBody(), getSerialiser());
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

    public static Pair<CentreDomainTreeManagerAndEnhancer, Boolean> retrieveCentre(final String eccKey, final byte[] array, final ISerialiser serialiser)
            throws Exception {
        logger.debug("Started maintaining the version of centre instance for [" + eccKey + "].");
        try {
            // try to deserialise with a current version of serialiser
            final CentreDomainTreeManagerAndEnhancer cdtmae = serialiser.deserialise(array, CentreDomainTreeManagerAndEnhancer.class);
            logger.debug("\tA centre instance for [" + eccKey + "] is of CURRENT (1) version and has been succesfully deserialised.");
            // all is okay -- the version is current
            logger.debug("Ended maintaining the version of centre instance for [" + eccKey + "].");
            return new Pair<>(cdtmae, false);
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
        final Pair<CentreDomainTreeManagerAndEnhancer, Boolean> cdtmaeAndShouldBePromoted = retrieveCentre(downloadedEcc.toString(), downloadedEcc.getConfigBody(), getSerialiser());
        if (cdtmaeAndShouldBePromoted.getValue()) {
            // the converted version should be promoted to the cloud
            downloadedEcc.setConfigBody(getSerialiser().serialise(cdtmaeAndShouldBePromoted.getKey())); // serialise with CURRENT version of serialiser
            // TODO DomainTreeVersionMaintainer is not used anywhere; delete it once inspiration for issue #1145 will be gained.
            final EntityCentreConfig updatedEcc = eccController.save(downloadedEcc);
            logger.warn("\tA centre instance, converted to CURRENT (1) version, for [" + downloadedEcc + "] has been succesfully saved (promoted to the cloud).");

            logger.debug("Ended maintaining the version of centre instance for [" + downloadedEcc + "].");
            return maintainCentreVersion(updatedEcc);
        } else {
            logger.debug("Ended maintaining the version of centre instance for [" + downloadedEcc + "].");
            return cdtmaeAndShouldBePromoted.getKey();
        }
    }

    public static Pair<MasterDomainTreeManager, Boolean> retrieveMaster(final String emcKey, final byte[] array, final ISerialiser serialiser) {
        logger.debug("Started maintaining the version of master instance for [" + emcKey + "].");
        try {
            // try to deserialise with a current version of serialiser
            final MasterDomainTreeManager mdtm = serialiser.deserialise(array, MasterDomainTreeManager.class);
            logger.debug("\tA master instance for [" + emcKey + "] is of CURRENT (1) version and has been succesfully deserialised.");
            // all is okay -- the version is current
            logger.debug("Ended maintaining the version of master instance for [" + emcKey + "].");
            return pair(mdtm, false);
        } catch (final Exception e) {
            final String msg = format("Could not deserialise a master instance for [%s]", emcKey);
            logger.error(msg);
            throw new DomainTreeException(msg, e);
        }
    }

    public MasterDomainTreeManager maintainMasterVersion(final EntityMasterConfig downloadedEmc) throws Exception {
        final Pair<MasterDomainTreeManager, Boolean> mdtmAndShouldBePromoted = retrieveMaster(downloadedEmc.toString(), downloadedEmc.getConfigBody(), getSerialiser());
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
}
