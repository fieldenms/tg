package ua.com.fielden.platform.web.centre;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Period;

import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager.MetaValueType;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.domaintree.centre.impl.CentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTree;
import ua.com.fielden.platform.domaintree.impl.GlobalDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.ui.menu.MiTypeAnnotation;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

/**
 * Represents a set of utility methods for updating / committing of surrogate centres, for e.g. 'fresh', 'previouslyRun' etc.
 * <p>
 * Every surrogate centre has its own diff centre that, saves into the database during {@link #commitCentre(IGlobalDomainTreeManager, Class, String)} process.
 *
 * @author TG Team
 *
 */
public class CentreUpdater {
    private final static Logger logger = Logger.getLogger(CentreUpdater.class);
    private static final String DIFFERENCES_SUFFIX = "__________DIFFERENCES";

    public static final String FRESH_CENTRE_NAME = "__________FRESH";
    public static final String PREVIOUSLY_RUN_CENTRE_NAME = "__________PREVIOUSLY_RUN";
    public static final String SAVED_CENTRE_NAME = "__________SAVED";

    /**
     * Returns user-specific version of surrogate centre name.
     *
     * @param surrogateName -- surrogate name of the centre (fresh, previouslyRun etc.)
     * @param gdtm
     *
     * @return
     */
    private static String userSpecificName(final String surrogateName, final IGlobalDomainTreeManager gdtm) {
        return surrogateName + "_FOR_USER_" + gdtm.getUserProvider().getUser().getId();
    }

    /**
     * Returns the current version of centre manager (it assumes that it should be initialised!).
     *
     * @param gdtm
     * @param miType
     * @param name -- surrogate name of the centre (fresh, previouslyRun etc.)
     *
     * @return
     */
    public static ICentreDomainTreeManagerAndEnhancer centre(final IGlobalDomainTreeManager gdtm, final Class<? extends MiWithConfigurationSupport<?>> miType, final String name) {
        return centre0(gdtm, miType, userSpecificName(name, gdtm));
    }
    private static ICentreDomainTreeManagerAndEnhancer centre0(final IGlobalDomainTreeManager gdtm, final Class<? extends MiWithConfigurationSupport<?>> miType, final String userSpecificName) {
        if (gdtm.getEntityCentreManager(miType, userSpecificName) == null) {
            throw new IllegalStateException(String.format("The '%s' centre should be initialised.", userSpecificName));
        }
        return gdtm.getEntityCentreManager(miType, userSpecificName);
    }

    /**
     * Returns the current version of centre (initialises it in case if it is not created yet, updates it in case where it is stale).
     * <p>
     * Initialisation / updating goes through the following chain: 'default centre' + 'differences centre' := 'centre'.
     * <p>
     * Centre on its own is never saved, but it is used to create 'differences centre' (when committing is performed).
     *
     * @param gdtm
     * @param miType
     * @param name -- surrogate name of the centre (fresh, previouslyRun etc.)
     *
     * @return
     */
    public static ICentreDomainTreeManagerAndEnhancer updateCentre(final IGlobalDomainTreeManager gdtm, final Class<? extends MiWithConfigurationSupport<?>> miType, final String name) {
        final String userSpecificName = userSpecificName(name, gdtm);
        synchronized (gdtm) {
            if (gdtm.getEntityCentreManager(miType, userSpecificName) == null) {
                return updateOrLoadCentre(gdtm, miType, userSpecificName, false);
            } else {
                if (isDiffCentreStale(gdtm, miType, userSpecificName)) {
                    return updateOrLoadCentre(gdtm, miType, userSpecificName, true);
                } else {
                    return centre0(gdtm, miType, userSpecificName);
                }
            }
        }
    }

    /**
     * Commits the centre's diff to the database and removes it from cache (needs to be updated to be able to be used).
     *
     * @param gdtm
     * @param miType
     * @param name -- surrogate name of the centre (fresh, previouslyRun etc.)
     */
    public static ICentreDomainTreeManagerAndEnhancer commitCentre(final IGlobalDomainTreeManager gdtm, final Class<? extends MiWithConfigurationSupport<?>> miType, final String name) {
        return commitCentre0(gdtm, miType, userSpecificName(name, gdtm));
    }
    private static ICentreDomainTreeManagerAndEnhancer commitCentre0(final IGlobalDomainTreeManager gdtm, final Class<? extends MiWithConfigurationSupport<?>> miType, final String userSpecificName) {
        synchronized (gdtm) {
            logger.debug(String.format("%s '%s' centre for miType [%s] for user %s...", "Committing", userSpecificName, miType.getSimpleName(), gdtm.getUserProvider().getUser()));
            final DateTime start = new DateTime();
            // gets the centre (that was created from the chain 'default centre' + 'saved diff centre' + 'current user diff' := 'centre')
            final ICentreDomainTreeManagerAndEnhancer centre = centre0(gdtm, miType, userSpecificName);

            final ICentreDomainTreeManagerAndEnhancer defaultCentre = getDefaultCentre(gdtm, miType);
            // creates differences centre from the differences between 'default centre' and 'centre'
            final ICentreDomainTreeManagerAndEnhancer differencesCentre = createDifferencesCentre(centre, defaultCentre, CentreUtils.getEntityType(miType), gdtm);

            // override old 'diff centre' with recently created one and save it
            overrideAndSaveDifferencesCentre(gdtm, miType, userSpecificName, differencesCentre);

            final DateTime end = new DateTime();
            final Period pd = new Period(start, end);
            logger.debug(String.format("%s the '%s' centre for miType [%s] for user %s... done in [%s].", "Committed", userSpecificName, miType.getSimpleName(), gdtm.getUserProvider().getUser(), pd.getSeconds() + " s " + pd.getMillis() + " ms"));
            return centre;
        }
    }

    /**
     * Initialises and commits centre from the passed <code>centreToBeInitialisedAndCommitted</code> instance for surrogate centre with concrete <code>name</code>.
     *
     * @param gdtm
     * @param miType
     * @param name -- surrogate name of the centre (fresh, previouslyRun etc.)
     * @param centreToBeInitialisedAndCommitted
     */
    public static ICentreDomainTreeManagerAndEnhancer initAndCommit(final IGlobalDomainTreeManager gdtm, final Class<? extends MiWithConfigurationSupport<?>> miType, final String name, final ICentreDomainTreeManagerAndEnhancer centreToBeInitialisedAndCommitted) {
        synchronized (gdtm) {
            final String userSpecificName = userSpecificName(name, gdtm);
            logger.debug(String.format("%s '%s' centre for miType [%s] for user %s...", "Initialising & committing", userSpecificName, miType.getSimpleName(), gdtm.getUserProvider().getUser()));
            final DateTime start = new DateTime();

            // there is a need to copy passed instance not to have shared state between surrogate centres (for e.g.
            //  same 'fresh' centre instance should not be used for 'previouslyRun' centre, it will cause unpredictable results after changing 'fresh' centre's criteria)
            final ICentreDomainTreeManagerAndEnhancer copiedInstance = copyCentre(centreToBeInitialisedAndCommitted, gdtm);
            // initialises centre from copied instance
            initCentre(gdtm, miType, userSpecificName, copiedInstance);
            // and then commit it to the database (save its diff)
            commitCentre0(gdtm, miType, userSpecificName);

            final DateTime end = new DateTime();
            final Period pd = new Period(start, end);
            logger.debug(String.format("%s the '%s' centre for miType [%s] for user %s... done in [%s].", "Initialised & committed", userSpecificName, miType.getSimpleName(), gdtm.getUserProvider().getUser(), pd.getSeconds() + " s " + pd.getMillis() + " ms"));
            return copiedInstance;
        }
    }

    /**
     * Updates / loads the centre from its updated diff.
     *
     * @param gdtm
     * @param miType
     * @param userSpecificName -- surrogate name of the centre (fresh, previouslyRun etc.) with user ID at the end
     * @param update -- <code>true</code> if update process is done, <code>false</code> if init process is done
     * @return
     */
    private static ICentreDomainTreeManagerAndEnhancer updateOrLoadCentre(final IGlobalDomainTreeManager gdtm, final Class<? extends MiWithConfigurationSupport<?>> miType, final String userSpecificName, final boolean update) {
        logger.debug(String.format("%s '%s' centre for miType [%s] %sfor user %s...", update ? "Updating of stale" : "Initialising", userSpecificName, miType.getSimpleName(), update ? "" : "for the first time ", gdtm.getUserProvider().getUser()));
        final DateTime start = new DateTime();

        final ICentreDomainTreeManagerAndEnhancer updatedDiffCentre = updateDifferencesCentre(gdtm, miType, userSpecificName);
        final ICentreDomainTreeManagerAndEnhancer loadedCentre = loadCentreFromDefaultAndDiff(gdtm, miType, userSpecificName, updatedDiffCentre);
        initCentre(gdtm, miType, userSpecificName, loadedCentre);

        final DateTime end = new DateTime();
        final Period pd = new Period(start, end);
        logger.debug(String.format("%s the '%s' centre for miType [%s] %sfor user %s... done in [%s].", update ? "Updated stale" : "Initialised", userSpecificName, miType.getSimpleName(), update ? "" : "for the first time ", gdtm.getUserProvider().getUser(), pd.getSeconds() + " s " + pd.getMillis() + " ms"));
        return centre0(gdtm, miType, userSpecificName);
    }

    /**
     * Returns <code>true</code> in case where the centre stale (by checking staleness of its diff cenre), <code>false</code> otherwise.
     *
     * @param gdtm
     * @param miType
     * @param userSpecificName -- surrogate name of the centre (fresh, previouslyRun etc.) with user ID at the end
     * @return
     */
    private static boolean isDiffCentreStale(final IGlobalDomainTreeManager gdtm, final Class<? extends MiWithConfigurationSupport<?>> miType, final String userSpecificName) {
        // the name consists of 'userSpecificName' and 'DIFFERENCES_SUFFIX'
        final String userSpecificDiffName = userSpecificName + DIFFERENCES_SUFFIX;

        // ensure that diff centre exists (it should)
        final CentreDomainTreeManagerAndEnhancer currentDiffCentre = (CentreDomainTreeManagerAndEnhancer) centre0(gdtm, miType, userSpecificDiffName);
        return ((GlobalDomainTreeManager) gdtm).isStale(currentDiffCentre);
    }

    /**
     * Loads centre through the following chain: 'default centre' + 'differences centre' := 'centre'.
     *
     * @param gdtm
     * @param miType
     * @param userSpecificName -- surrogate name of the centre (fresh, previouslyRun etc.) with user ID at the end
     * @param updatedDiffCentre -- updated differences centre
     *
     * @return
     */
    private static ICentreDomainTreeManagerAndEnhancer loadCentreFromDefaultAndDiff(final IGlobalDomainTreeManager gdtm, final Class<? extends MiWithConfigurationSupport<?>> miType, final String userSpecificName, final ICentreDomainTreeManagerAndEnhancer updatedDiffCentre) {
        logger.debug(String.format("\t%s '%s' centre for miType [%s] for user %s...", "loadCentreFromDefaultAndDiff", userSpecificName, miType.getSimpleName(), gdtm.getUserProvider().getUser()));
        final DateTime start = new DateTime();

        // TODO consider not copying of default centre for performance reasons:
        final ICentreDomainTreeManagerAndEnhancer defaultCentreCopy = copyCentre(getDefaultCentre(gdtm, miType), gdtm);
        // applies diffCentre on top of defaultCentreCopy to produce loadedCentre:
        final ICentreDomainTreeManagerAndEnhancer loadedCentre = applyDifferences(defaultCentreCopy, updatedDiffCentre, CentreUtils.getEntityType(miType));
        // For all generated types on freshCentre (and on its derivatives like 'unchanged freshCentre', 'previouslyRun centre', 'unchanged previouslyRun centre' etc.) there is a need to
        //  provide miType information inside its generated type to be sent to the client application. This is done through the use of
        //  annotation miType and, in future, other custom annotations, for example @SaveAsName.
        // Please note that copyCentre method in GlobalDomainTreeManager performs copying of all defined annotations to provide freshCentre's derivatives
        //  with such additional information too.
        for (final Class<?> root: loadedCentre.getRepresentation().rootTypes()) {
            if (DynamicEntityClassLoader.isGenerated(loadedCentre.getEnhancer().getManagedType(root))) {
                loadedCentre.getEnhancer().adjustManagedTypeAnnotations(root, new MiTypeAnnotation().newInstance(miType));
            }
        }
        final DateTime end = new DateTime();
        final Period pd = new Period(start, end);
        logger.debug(String.format("\t%s the '%s' centre for miType [%s] for user %s... done in [%s].", "loadCentreFromDefaultAndDiff", userSpecificName, miType.getSimpleName(), gdtm.getUserProvider().getUser(), pd.getSeconds() + " s " + pd.getMillis() + " ms"));
        return loadedCentre;
    }

    /**
     * Returns the current version of default centre manager (initialises it in case if it is not created yet).
     * <p>
     * Currently it is created from Entity Centre DSL through the special gdtm, which knows about Centre DSL configuration.
     *
     * IMPORTANT: this 'default centre' is used for constructing 'fresh centre', 'previouslyRun centre' and their 'diff centres', that is why it is very important to make it suitable for Web UI default values.
     * All other centres will reuse such Web UI specific default values.
     *
     * @param gdtm
     * @param miType
     * @return
     */
    private static ICentreDomainTreeManagerAndEnhancer getDefaultCentre(final IGlobalDomainTreeManager gdtm, final Class<? extends MiWithConfigurationSupport<?>> miType) {
        logger.debug(String.format("\t\t%s centre for miType [%s] for user %s...", "getDefaultCentre", miType.getSimpleName(), gdtm.getUserProvider().getUser()));
        final DateTime start = new DateTime();

        if (gdtm.getEntityCentreManager(miType, null) == null) {
            // standard init (from Centre DSL config)
            gdtm.initEntityCentreManager(miType, null);

            // Web UI default values application
            final ICentreDomainTreeManagerAndEnhancer defaultedCentre = applyWebUIDefaultValues(
                    gdtm.getEntityCentreManager(miType, null),
                    CentreUtils.getEntityType(miType) //
            );
            initCentre(gdtm, miType, null, defaultedCentre); // after this action default centre will be changed in most cases!
        }

        final DateTime end = new DateTime();
        final Period pd = new Period(start, end);
        logger.debug(String.format("\t\t%s the centre for miType [%s] for user %s... done in [%s].", "getDefaultCentre", miType.getSimpleName(), gdtm.getUserProvider().getUser(), pd.getSeconds() + " s " + pd.getMillis() + " ms"));

        return gdtm.getEntityCentreManager(miType, null);
    }

    /**
     * Initialises 'differences centre' from the persistent storage only if it is not initialised on server.
     *
     * @param globalManager
     * @param miType
     * @param userSpecificName -- surrogate name of the centre (fresh, previouslyRun etc.) with user ID at the end
     *
     * @return
     */
    private static ICentreDomainTreeManagerAndEnhancer updateDifferencesCentreOnlyIfNotInitialised(final IGlobalDomainTreeManager globalManager, final Class<? extends MiWithConfigurationSupport<?>> miType, final String userSpecificName) {
        // the name consists of 'userSpecificName' and 'DIFFERENCES_SUFFIX'
        final String userSpecificDiffName = userSpecificName + DIFFERENCES_SUFFIX;

        if (globalManager.getEntityCentreManager(miType, userSpecificDiffName) == null) {
            return updateDifferencesCentre(globalManager, miType, userSpecificName);
        }
        return globalManager.getEntityCentreManager(miType, userSpecificDiffName);
    }

    /**
     * Initialises 'differences centre' from the persistent storage, if it exists.
     * <p>
     * If no 'differences centre' exists -- the following steps are performed:
     * <p>
     * 1. make sure that 'default centre' exists in gdtm (with already applied Web UI default values!);<br>
     * 2. make saveAs from 'default centre' which will be 'diff centre' (this promotes the empty diff to the storage!)<br>
     *
     * @param globalManager
     * @param miType
     * @param userSpecificName -- surrogate name of the centre (fresh, previouslyRun etc.) with user ID at the end
     *
     * @return
     */
    private static ICentreDomainTreeManagerAndEnhancer updateDifferencesCentre(final IGlobalDomainTreeManager globalManager, final Class<? extends MiWithConfigurationSupport<?>> miType, final String userSpecificName) {
        logger.debug(String.format("\t%s '%s' centre for miType [%s] for user %s...", "updateDifferencesCentre", userSpecificName, miType.getSimpleName(), globalManager.getUserProvider().getUser()));
        final DateTime start = new DateTime();

        // the name consists of 'userSpecificName' and 'DIFFERENCES_SUFFIX'
        final String userSpecificDiffName = userSpecificName + DIFFERENCES_SUFFIX;

        // WILL BE UPDATED IN EVERY CALL OF updateDifferencesCentre!
        try {
            // init (or update) diff centre from persistent storage if exists
            globalManager.initEntityCentreManager(miType, userSpecificDiffName);
        } catch (final IllegalArgumentException e) {
            if (e.getMessage().startsWith("Unable to initialise a non-existent entity-centre instance for type")) {
                // diff centre does not exist in persistent storage yet -- initialise EMPTY diff (there potentially can be some values from 'default centre',
                //   but diff centre will be empty disregarding that fact -- no properties were marked as changed; but initialisation from 'default centre' is important --
                //   this makes diff centre nicely synchronised with Web UI default values)
                getDefaultCentre(globalManager, miType);

                globalManager.saveAsEntityCentreManager(miType, null, userSpecificDiffName);
            } else {
                throw e;
            }
        }
        final ICentreDomainTreeManagerAndEnhancer differencesCentre = globalManager.getEntityCentreManager(miType, userSpecificDiffName);

        final DateTime end = new DateTime();
        final Period pd = new Period(start, end);
        logger.debug(String.format("\t%s the '%s' centre for miType [%s] for user %s... done in [%s].", "updateDifferencesCentre", userSpecificName, miType.getSimpleName(), globalManager.getUserProvider().getUser(), pd.getSeconds() + " s " + pd.getMillis() + " ms"));
        return differencesCentre;
    }

    /**
     * Initialises 'virtual' (should never be persistent) centre -- caches it on the server (into currentCentres only).
     *
     * @param gdtm
     * @param miType
     * @param userSpecificName -- surrogate name of the centre (fresh, previouslyRun etc.) with user ID at the end
     * @param centre
     *
     * @return
     */
    private static void initCentre(final IGlobalDomainTreeManager gdtm, final Class<? extends MiWithConfigurationSupport<?>> miType, final String userSpecificName, final ICentreDomainTreeManagerAndEnhancer centre) {
        synchronized (gdtm) {
            ((GlobalDomainTreeManager) gdtm).overrideCentre(miType, userSpecificName, centre);
        }
    }

    /**
     * Copies centre manager.
     *
     * @param centre
     * @param gdtm
     *
     * @return
     */
    private static ICentreDomainTreeManagerAndEnhancer copyCentre(final ICentreDomainTreeManagerAndEnhancer centre, final IGlobalDomainTreeManager gdtm) {
        return ((GlobalDomainTreeManager) gdtm).copyCentre(centre);
    }

    /**
     * Applies the differences from 'differences centre' on top of 'target centre'.
     *
     * @param targetCentre
     * @param differencesCentre
     * @param root
     * @return
     */
    private static ICentreDomainTreeManagerAndEnhancer applyDifferences(final ICentreDomainTreeManagerAndEnhancer targetCentre, final ICentreDomainTreeManagerAndEnhancer differencesCentre, final Class<AbstractEntity<?>> root) {
        for (final String property : differencesCentre.getFirstTick().checkedProperties(root)) {
            if (!AbstractDomainTree.isPlaceholder(property)) {
                // Check whether the 'property' has not been disappeared from domain type since last server restart.
                // In such case 'checkedProperties' will contain that property but 'managedType(root, differencesCentre)' will not contain corresponding field.
                // Such properties need to be silently ignored. During next diffCentre creation such properties will disappear from diffCentre fully.
                final boolean isEntityItself = "".equals(property); // empty property means "entity itself"
                if (!isEntityItself) {
                    try {
                        PropertyTypeDeterminator.determinePropertyType(managedType(root, differencesCentre), property);
                    } catch (final Exception ex) {
                        logger.warn(String.format("Property [%s] could not be found in type [%s] in diffCentre. It will be skipped. Most likely this property was deleted from domain type definition.", property, managedType(root, differencesCentre).getSimpleName()), ex);
                        continue;
                    }
                }

                if (AbstractDomainTree.isDoubleCriterion(managedType(root, differencesCentre), property)) {
                    if (differencesCentre.getFirstTick().isMetaValuePresent(MetaValueType.EXCLUSIVE, root, property)) {
                        targetCentre.getFirstTick().setExclusive(root, property, differencesCentre.getFirstTick().getExclusive(root, property));
                    }
                    if (differencesCentre.getFirstTick().isMetaValuePresent(MetaValueType.EXCLUSIVE2, root, property)) {
                        targetCentre.getFirstTick().setExclusive2(root, property, differencesCentre.getFirstTick().getExclusive2(root, property));
                    }
                }
                final Class<?> propertyType = StringUtils.isEmpty(property) ? managedType(root, differencesCentre) : PropertyTypeDeterminator.determinePropertyType(managedType(root, differencesCentre), property);
                if (EntityUtils.isDate(propertyType)) {
                    if (differencesCentre.getFirstTick().isMetaValuePresent(MetaValueType.DATE_PREFIX, root, property)) {
                        targetCentre.getFirstTick().setDatePrefix(root, property, differencesCentre.getFirstTick().getDatePrefix(root, property));
                    }
                    if (differencesCentre.getFirstTick().isMetaValuePresent(MetaValueType.DATE_MNEMONIC, root, property)) {
                        targetCentre.getFirstTick().setDateMnemonic(root, property, differencesCentre.getFirstTick().getDateMnemonic(root, property));
                    }
                    if (differencesCentre.getFirstTick().isMetaValuePresent(MetaValueType.AND_BEFORE, root, property)) {
                        targetCentre.getFirstTick().setAndBefore(root, property, differencesCentre.getFirstTick().getAndBefore(root, property));
                    }
                }

                if (differencesCentre.getFirstTick().isMetaValuePresent(MetaValueType.OR_NULL, root, property)) {
                    targetCentre.getFirstTick().setOrNull(root, property, differencesCentre.getFirstTick().getOrNull(root, property));
                }
                if (differencesCentre.getFirstTick().isMetaValuePresent(MetaValueType.NOT, root, property)) {
                    targetCentre.getFirstTick().setNot(root, property, differencesCentre.getFirstTick().getNot(root, property));
                }

                if (differencesCentre.getFirstTick().isMetaValuePresent(MetaValueType.VALUE, root, property)) {
                    targetCentre.getFirstTick().setValue(root, property, differencesCentre.getFirstTick().getValue(root, property));
                }
                if (AbstractDomainTree.isDoubleCriterionOrBoolean(managedType(root, differencesCentre), property)) {
                    if (differencesCentre.getFirstTick().isMetaValuePresent(MetaValueType.VALUE2, root, property)) {
                        targetCentre.getFirstTick().setValue2(root, property, differencesCentre.getFirstTick().getValue2(root, property));
                    }
                }
            }
        }
        
        // Diff centre contains full information of checkedProperties and usedProperties.
        // Such information should be carefully merged with potentially updated targetCentre.
        final List<String> targetCheckedProperties = targetCentre.getSecondTick().checkedProperties(root);
        final List<String> diffCheckedProperties = differencesCentre.getSecondTick().checkedProperties(root);
        // determine properties that were added into targetCentre (default config) comparing to differencesCentre (currently saved config)
        final List<String> addedIntoTarget = minus(targetCheckedProperties, diffCheckedProperties);
        logger.error("Added into default config: [" + addedIntoTarget + "]");
        // determine properties that were removed from targetCentre (default config) comparing to differencesCentre (currently saved config)
        final List<String> removedFromTarget = minus(diffCheckedProperties, targetCheckedProperties);
        logger.error("Removed from default config: [" + removedFromTarget + "]");
        
        final List<String> diffUsedProperties = differencesCentre.getSecondTick().usedProperties(root);
        // remove the properties that was removed in default configuration
        final List<String> diffUsedPropertiesWithoutDissapearedProps = minus(diffUsedProperties, removedFromTarget);
        // apply resultant properties on top
        // un-'use' all old properties
        final List<String> currUsedProperties = targetCentre.getSecondTick().usedProperties(root);
        logger.error("Curr used (default, target, config): [" + currUsedProperties + "]");
        for (final String currUsedProperty: currUsedProperties) {
            targetCentre.getSecondTick().use(root, currUsedProperty, false);
        }
        
        // 'use' all new properties
        for (final String chosenId : diffUsedPropertiesWithoutDissapearedProps) {
            targetCentre.getSecondTick().use(root, chosenId, true);
        }
        logger.error("New used (default, target, config): [" + targetCentre.getSecondTick().usedProperties(root) + "]");

        // apply widths and grow factor that were marked as changed
        for (final String property : differencesCentre.getSecondTick().checkedProperties(root)) {
            if (differencesCentre.getFirstTick().isMetaValuePresent(MetaValueType.WIDTH, root, property)) {
                targetCentre.getSecondTick().setWidth(root, property, differencesCentre.getSecondTick().getWidth(root, property));
            }
            if (differencesCentre.getFirstTick().isMetaValuePresent(MetaValueType.GROW_FACTOR, root, property)) {
                targetCentre.getSecondTick().setGrowFactor(root, property, differencesCentre.getSecondTick().getGrowFactor(root, property));
            }
        }

        if (differencesCentre.getFirstTick().isMetaValuePresent(MetaValueType.ALL_ORDERING, root, "")) {
            // need to clear all previous orderings:
            final List<Pair<String, Ordering>> orderedProperties = new ArrayList<>(targetCentre.getSecondTick().orderedProperties(root));
            for (final Pair<String, Ordering> orderedProperty: orderedProperties) {
                if (Ordering.ASCENDING == orderedProperty.getValue()) {
                    targetCentre.getSecondTick().toggleOrdering(root, orderedProperty.getKey());
                }
                targetCentre.getSecondTick().toggleOrdering(root, orderedProperty.getKey());
            }
            // and apply new ones from diff centre:
            for (final Pair<String, Ordering> newOrderedProperty: differencesCentre.getSecondTick().orderedProperties(root)) {
                // Check whether the 'property' has not been disappeared from domain type since last server restart.
                // In such case 'orderedProperties' will contain that property but 'managedType(root, differencesCentre)' will not contain corresponding field.
                // Such properties need to be silently ignored. During next diffCentre creation such properties will disappear from diffCentre fully.
                final boolean isEntityItself = "".equals(newOrderedProperty.getKey()); // empty property means "entity itself"
                if (!isEntityItself) {
                    try {
                        PropertyTypeDeterminator.determinePropertyType(managedType(root, differencesCentre), newOrderedProperty.getKey());
                    } catch (final Exception ex) {
                        logger.warn(String.format("Property [%s] could not be found in type [%s] in diffCentre. It will be skipped. Most likely this property was deleted from domain type definition.", newOrderedProperty.getKey(), managedType(root, differencesCentre).getSimpleName()), ex);
                        continue;
                    }
                }

                targetCentre.getSecondTick().toggleOrdering(root, newOrderedProperty.getKey());
                if (Ordering.DESCENDING == newOrderedProperty.getValue()) {
                    targetCentre.getSecondTick().toggleOrdering(root, newOrderedProperty.getKey());
                }
            }
        }

        return targetCentre;
    }

    private static List<String> minus(final List<String> targetCheckedProperties, final List<String> diffCheckedProperties) {
        final List<String> result = new ArrayList<>();
        for (final String targetCheckedProperty: targetCheckedProperties) {
            if (!diffCheckedProperties.contains(targetCheckedProperty)) {
                result.add(targetCheckedProperty);
            }
        }
        return result;
    }

    /**
     * Creates 'diff centre' from 'centre' and 'originalCentre' with marked meta-values (only those that are different).
     *
     * @param centre
     * @param originalCentre
     * @param root
     * @return
     */
    private static ICentreDomainTreeManagerAndEnhancer createDifferencesCentre(final ICentreDomainTreeManagerAndEnhancer centre, final ICentreDomainTreeManagerAndEnhancer originalCentre, final Class<AbstractEntity<?>> root, final IGlobalDomainTreeManager gdtm) {
        logger.debug(String.format("\t%s centre for user %s...", "createDifferencesCentre", gdtm.getUserProvider().getUser()));
        final DateTime start = new DateTime();
        final ICentreDomainTreeManagerAndEnhancer differencesCentre = copyCentre(centre, gdtm);

        for (final String property : differencesCentre.getFirstTick().checkedProperties(root)) {
            if (!AbstractDomainTree.isPlaceholder(property)) {
                if (AbstractDomainTree.isDoubleCriterion(managedType(root, differencesCentre), property)) {
                    if (!EntityUtils.equalsEx(differencesCentre.getFirstTick().getExclusive(root, property), originalCentre.getFirstTick().getExclusive(root, property))) {
                        differencesCentre.getFirstTick().markMetaValuePresent(MetaValueType.EXCLUSIVE, root, property);
                    }
                    if (!EntityUtils.equalsEx(differencesCentre.getFirstTick().getExclusive2(root, property), originalCentre.getFirstTick().getExclusive2(root, property))) {
                        differencesCentre.getFirstTick().markMetaValuePresent(MetaValueType.EXCLUSIVE2, root, property);
                    }
                }
                final Class<?> propertyType = StringUtils.isEmpty(property) ? managedType(root, differencesCentre) : PropertyTypeDeterminator.determinePropertyType(managedType(root, differencesCentre), property);
                if (EntityUtils.isDate(propertyType)) {
                    if (!EntityUtils.equalsEx(differencesCentre.getFirstTick().getDatePrefix(root, property), originalCentre.getFirstTick().getDatePrefix(root, property))) {
                        differencesCentre.getFirstTick().markMetaValuePresent(MetaValueType.DATE_PREFIX, root, property);
                    }
                    if (!EntityUtils.equalsEx(differencesCentre.getFirstTick().getDateMnemonic(root, property), originalCentre.getFirstTick().getDateMnemonic(root, property))) {
                        differencesCentre.getFirstTick().markMetaValuePresent(MetaValueType.DATE_MNEMONIC, root, property);
                    }
                    if (!EntityUtils.equalsEx(differencesCentre.getFirstTick().getAndBefore(root, property), originalCentre.getFirstTick().getAndBefore(root, property))) {
                        differencesCentre.getFirstTick().markMetaValuePresent(MetaValueType.AND_BEFORE, root, property);
                    }
                }

                if (!EntityUtils.equalsEx(differencesCentre.getFirstTick().getOrNull(root, property), originalCentre.getFirstTick().getOrNull(root, property))) {
                    differencesCentre.getFirstTick().markMetaValuePresent(MetaValueType.OR_NULL, root, property);
                }
                if (!EntityUtils.equalsEx(differencesCentre.getFirstTick().getNot(root, property), originalCentre.getFirstTick().getNot(root, property))) {
                    differencesCentre.getFirstTick().markMetaValuePresent(MetaValueType.NOT, root, property);
                }

                if (!EntityUtils.equalsEx(differencesCentre.getFirstTick().getValue(root, property), originalCentre.getFirstTick().getValue(root, property))) {
                    differencesCentre.getFirstTick().markMetaValuePresent(MetaValueType.VALUE, root, property);
                }
                if (AbstractDomainTree.isDoubleCriterionOrBoolean(managedType(root, differencesCentre), property)) {
                    if (!EntityUtils.equalsEx(differencesCentre.getFirstTick().getValue2(root, property), originalCentre.getFirstTick().getValue2(root, property))) {
                        differencesCentre.getFirstTick().markMetaValuePresent(MetaValueType.VALUE2, root, property);
                    }
                }
            }
        }

        // extract widths that are changed and mark them
        for (final String property : differencesCentre.getSecondTick().checkedProperties(root)) {
            if (!EntityUtils.equalsEx(differencesCentre.getSecondTick().getWidth(root, property), originalCentre.getSecondTick().getWidth(root, property))) {
                differencesCentre.getFirstTick().markMetaValuePresent(MetaValueType.WIDTH, root, property);
            }
            if (!EntityUtils.equalsEx(differencesCentre.getSecondTick().getGrowFactor(root, property), originalCentre.getSecondTick().getGrowFactor(root, property))) {
                differencesCentre.getFirstTick().markMetaValuePresent(MetaValueType.GROW_FACTOR, root, property);
            }
        }

        // need to determine whether orderedProperties have been changed (as a whole) and mark diff centre if true:
        if (!EntityUtils.equalsEx(differencesCentre.getSecondTick().orderedProperties(root), originalCentre.getSecondTick().orderedProperties(root))) {
            differencesCentre.getFirstTick().markMetaValuePresent(MetaValueType.ALL_ORDERING, root, "");
        }

        final DateTime end = new DateTime();
        final Period pd = new Period(start, end);
        logger.debug(String.format("\t%s centre for user %s... done in [%s].", "createDifferencesCentre", gdtm.getUserProvider().getUser(), pd.getSeconds() + " s " + pd.getMillis() + " ms"));
        return differencesCentre;
    }

    /**
     * Applies correct default values to be in sync with Web UI ones on top of 'centre'.
     * <p>
     * Currently, only String-typed properties in Web UI have different default values (<code>null</code> instead of ""). This method traverses all String-typed properties and
     * provides correct default values for them.
     * <p>
     * Please, refer to method {@link DynamicQueryBuilder#getEmptyValue(Class, boolean)} for more details on standard default values.
     *
     * @param centre
     * @param root
     */
    private static ICentreDomainTreeManagerAndEnhancer applyWebUIDefaultValues(final ICentreDomainTreeManagerAndEnhancer centre, final Class<AbstractEntity<?>> root) {
        for (final String includedProperty : centre.getRepresentation().includedProperties(root)) {
            if (!AbstractDomainTree.isDummyMarker(includedProperty)) {
                final String property = AbstractDomainTree.reflectionProperty(includedProperty);
                final boolean isEntityItself = "".equals(property); // empty property means "entity itself"
                final Class<?> propertyType = isEntityItself ? managedType(root, centre) : PropertyTypeDeterminator.determinePropertyType(managedType(root, centre), property);

                if (EntityUtils.isString(propertyType)) {
                    centre.getRepresentation().getFirstTick().setValueByDefault(root, includedProperty, null);
                }
            }
        }
        return centre;
    }

    /**
     * Returns the 'managed type' for the 'centre' manager.
     *
     * @param root
     * @param centre
     * @return
     */
    protected static Class<?> managedType(final Class<AbstractEntity<?>> root, final ICentreDomainTreeManagerAndEnhancer centre) {
        return centre.getEnhancer().getManagedType(root);
    }

    /**
     * Overrides old 'differences centre' with new one and saves it.
     *
     * @param gdtm
     * @param miType
     * @param userSpecificName -- surrogate name of the centre (fresh, previouslyRun etc.) with user ID at the end
     * @param newDiffCentre
     *
     * @return
     */
    private static void overrideAndSaveDifferencesCentre(final IGlobalDomainTreeManager gdtm, final Class<? extends MiWithConfigurationSupport<?>> miType, final String userSpecificName, final ICentreDomainTreeManagerAndEnhancer newDiffCentre) {
        logger.debug(String.format("\t%s '%s' centre for miType [%s] for user %s...", "overrideAndSaveDifferencesCentre", userSpecificName, miType.getSimpleName(), gdtm.getUserProvider().getUser()));
        final DateTime start = new DateTime();

        // the name consists of 'userSpecificName' and 'DIFFERENCES_SUFFIX'
        final String userSpecificDiffName = userSpecificName + DIFFERENCES_SUFFIX;

        // In case where diff centre was not ever initialised from persistent storage -- it should be initialised for the first time.
        // It guarantees that at the point of diff centre saving, the empty diff was already saved. See method 'updateDifferencesCentre' for more details.
        final ICentreDomainTreeManagerAndEnhancer staleDiffCentre = updateDifferencesCentreOnlyIfNotInitialised(gdtm, miType, userSpecificName);
        final boolean diffChanged = !EntityUtils.equalsEx(staleDiffCentre, newDiffCentre);

        if (diffChanged) {
            initCentre(gdtm, miType, userSpecificDiffName, newDiffCentre);
            gdtm.saveEntityCentreManager(miType, userSpecificDiffName);
        }

        final DateTime end = new DateTime();
        final Period pd = new Period(start, end);
        logger.debug(String.format("\t%s the '%s' centre for miType [%s] for user %s... done in [%s].", "overrideAndSaveDifferencesCentre" + (diffChanged ? "" : " (nothing has changed)"), userSpecificName, miType.getSimpleName(), gdtm.getUserProvider().getUser(), pd.getSeconds() + " s " + pd.getMillis() + " ms"));
    }

    /**
     * Clears all cached instances of centre managers for concrete user's {@link IGlobalDomainTreeManager}.
     *
     * @param gdtm
     */
    public static void clearAllCentres(final IGlobalDomainTreeManager gdtm) {
        for (final Class<?> miType: gdtm.entityCentreMenuItemTypes()) {
            final GlobalDomainTreeManager globalManager = (GlobalDomainTreeManager) gdtm;
            globalManager.overrideCentre(miType, null, null);

            globalManager.overrideCentre(miType, userSpecificName(FRESH_CENTRE_NAME, gdtm), null);
            globalManager.overrideCentre(miType, userSpecificName(FRESH_CENTRE_NAME, gdtm) + DIFFERENCES_SUFFIX, null);

            globalManager.overrideCentre(miType, userSpecificName(PREVIOUSLY_RUN_CENTRE_NAME, gdtm), null);
            globalManager.overrideCentre(miType, userSpecificName(PREVIOUSLY_RUN_CENTRE_NAME, gdtm) + DIFFERENCES_SUFFIX, null);

            globalManager.overrideCentre(miType, userSpecificName(SAVED_CENTRE_NAME, gdtm), null);
            globalManager.overrideCentre(miType, userSpecificName(SAVED_CENTRE_NAME, gdtm) + DIFFERENCES_SUFFIX, null);
        }
    }
}
