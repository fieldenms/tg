package ua.com.fielden.platform.web.centre;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Period;

import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager.MetaValueType;
import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.impl.CentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTree;
import ua.com.fielden.platform.domaintree.impl.GlobalDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.swing.menu.MiTypeAnnotation;
import ua.com.fielden.platform.swing.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.swing.review.DynamicQueryBuilder;
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
     * Returns the current version of centre manager (it assumes that it should be initialised!).
     *
     * @param gdtm
     * @param miType
     * @param name -- surrogate name of the centre (fresh, previouslyRun etc.)
     * 
     * @return
     */
    public static ICentreDomainTreeManagerAndEnhancer centre(final IGlobalDomainTreeManager gdtm, final Class<? extends MiWithConfigurationSupport<?>> miType, final String name) {
        if (gdtm.getEntityCentreManager(miType, name) == null) {
            throw new IllegalStateException(String.format("The '%s' centre should be initialised.", name));
        }
        return gdtm.getEntityCentreManager(miType, name);
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
        synchronized (gdtm) {
            if (gdtm.getEntityCentreManager(miType, name) == null) {
                return updateOrLoadCentre(gdtm, miType, name, false);
            } else {
                if (isDiffCentreStale(gdtm, miType, name)) {
                    return updateOrLoadCentre(gdtm, miType, name, true);
                } else {
                    return centre(gdtm, miType, name);
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
        synchronized (gdtm) {
            logger.debug(String.format("%s '%s' centre for miType [%s] for user %s...", "Committing", name, miType.getSimpleName(), gdtm.getUserProvider().getUser()));
            final DateTime start = new DateTime();
            // gets the centre (that was created from the chain 'default centre' + 'saved diff centre' + 'current user diff' := 'centre')
            final ICentreDomainTreeManagerAndEnhancer centre = centre(gdtm, miType, name);
            
            final ICentreDomainTreeManagerAndEnhancer defaultCentre = getDefaultCentre(gdtm, miType);
            // creates differences centre from the differences between 'default centre' and 'centre'
            final ICentreDomainTreeManagerAndEnhancer differencesCentre = createDifferencesCentre(centre, defaultCentre, CentreUtils.getEntityType(miType), gdtm);
            
            // override old 'diff centre' with recently created one and save it
            overrideAndSaveDifferencesCentre(gdtm, miType, name, differencesCentre);

            final DateTime end = new DateTime();
            final Period pd = new Period(start, end);
            logger.debug(String.format("%s the '%s' centre for miType [%s] for user %s... done in [%s].", "Committed", name, miType.getSimpleName(), gdtm.getUserProvider().getUser(), pd.getSeconds() + " s " + pd.getMillis() + " ms"));
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
            logger.debug(String.format("%s '%s' centre for miType [%s] for user %s...", "Initialising & committing", name, miType.getSimpleName(), gdtm.getUserProvider().getUser()));
            final DateTime start = new DateTime();
            
            // there is a need to copy passed instance not to have shared state between surrogate centres (for e.g. 
            //  same 'fresh' centre instance should not be used for 'previouslyRun' centre, it will cause unpredictable results after changing 'fresh' centre's criteria)
            final ICentreDomainTreeManagerAndEnhancer copiedInstance = copyCentre(centreToBeInitialisedAndCommitted, gdtm);
            // initialises centre from copied instance
            initCentre(gdtm, miType, name, copiedInstance);
            // and then commit it to the database (save its diff)
            commitCentre(gdtm, miType, name);

            final DateTime end = new DateTime();
            final Period pd = new Period(start, end);
            logger.debug(String.format("%s the '%s' centre for miType [%s] for user %s... done in [%s].", "Initialised & committed", name, miType.getSimpleName(), gdtm.getUserProvider().getUser(), pd.getSeconds() + " s " + pd.getMillis() + " ms"));
            return copiedInstance;
        }
    }
    
    /**
     * Updates / loads the centre from its updated diff.
     * 
     * @param gdtm
     * @param miType
     * @param name -- surrogate name of the centre (fresh, previouslyRun etc.)
     * @param update -- <code>true</code> if update process is done, <code>false</code> if init process is done
     * @return
     */
    private static ICentreDomainTreeManagerAndEnhancer updateOrLoadCentre(final IGlobalDomainTreeManager gdtm, final Class<? extends MiWithConfigurationSupport<?>> miType, final String name, final boolean update) {
        logger.debug(String.format("%s '%s' centre for miType [%s] %sfor user %s...", update ? "Updating of stale" : "Initialising", name, miType.getSimpleName(), update ? "" : "for the first time ", gdtm.getUserProvider().getUser()));
        final DateTime start = new DateTime();

        final ICentreDomainTreeManagerAndEnhancer updatedDiffCentre = updateDifferencesCentre(gdtm, miType, name);
        final ICentreDomainTreeManagerAndEnhancer loadedCentre = loadCentreFromDefaultAndDiff(gdtm, miType, name, updatedDiffCentre);
        initCentre(gdtm, miType, name, loadedCentre);
        
        final DateTime end = new DateTime();
        final Period pd = new Period(start, end);
        logger.debug(String.format("%s the '%s' centre for miType [%s] %sfor user %s... done in [%s].", update ? "Updated stale" : "Initialised", name, miType.getSimpleName(), update ? "" : "for the first time ", gdtm.getUserProvider().getUser(), pd.getSeconds() + " s " + pd.getMillis() + " ms"));
        return centre(gdtm, miType, name);
    }
    
    /**
     * Returns <code>true</code> in case where the centre stale (by checking staleness of its diff cenre), <code>false</code> otherwise.
     * 
     * @param gdtm
     * @param miType
     * @param name -- surrogate name of the centre (fresh, previouslyRun etc.)
     * @return
     */
    private static boolean isDiffCentreStale(final IGlobalDomainTreeManager gdtm, final Class<? extends MiWithConfigurationSupport<?>> miType, final String name) {
        // the name consists of 'name' and 'DIFFERENCES_SUFFIX'
        final String diffSurrogateName = name + DIFFERENCES_SUFFIX;
        
        // ensure that diff centre exists (it should)
        final CentreDomainTreeManagerAndEnhancer currentDiffCentre = (CentreDomainTreeManagerAndEnhancer) centre(gdtm, miType, diffSurrogateName);
        return ((GlobalDomainTreeManager) gdtm).isStale(currentDiffCentre);
    }

    /**
     * Loads centre through the following chain: 'default centre' + 'differences centre' := 'centre'.
     *
     * @param gdtm
     * @param miType
     * @param name -- surrogate name of the centre (fresh, previouslyRun etc.)
     * @param updatedDiffCentre -- updated differences centre 
     *  
     * @return
     */
    private static ICentreDomainTreeManagerAndEnhancer loadCentreFromDefaultAndDiff(final IGlobalDomainTreeManager gdtm, final Class<? extends MiWithConfigurationSupport<?>> miType, final String name, final ICentreDomainTreeManagerAndEnhancer updatedDiffCentre) {
        logger.debug(String.format("\t%s '%s' centre for miType [%s] for user %s...", "loadCentreFromDefaultAndDiff", name, miType.getSimpleName(), gdtm.getUserProvider().getUser()));
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
        logger.debug(String.format("\t%s the '%s' centre for miType [%s] for user %s... done in [%s].", "loadCentreFromDefaultAndDiff", name, miType.getSimpleName(), gdtm.getUserProvider().getUser(), pd.getSeconds() + " s " + pd.getMillis() + " ms"));
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
     * @param name -- surrogate name of the centre (fresh, previouslyRun etc.)
     *  
     * @return
     */
    private static ICentreDomainTreeManagerAndEnhancer updateDifferencesCentreOnlyIfNotInitialised(final IGlobalDomainTreeManager globalManager, final Class<? extends MiWithConfigurationSupport<?>> miType, final String name) {
        // the name consists of 'name' and 'DIFFERENCES_SUFFIX'
        final String diffSurrogateName = name + DIFFERENCES_SUFFIX;
        
        if (globalManager.getEntityCentreManager(miType, diffSurrogateName) == null) {
            return updateDifferencesCentre(globalManager, miType, name);
        }
        return globalManager.getEntityCentreManager(miType, diffSurrogateName);
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
     * @param name -- surrogate name of the centre (fresh, previouslyRun etc.)
     *  
     * @return
     */
    private static ICentreDomainTreeManagerAndEnhancer updateDifferencesCentre(final IGlobalDomainTreeManager globalManager, final Class<? extends MiWithConfigurationSupport<?>> miType, final String name) {
        logger.debug(String.format("\t%s '%s' centre for miType [%s] for user %s...", "updateDifferencesCentre", name, miType.getSimpleName(), globalManager.getUserProvider().getUser()));
        final DateTime start = new DateTime();
        
        // the name consists of 'name' and 'DIFFERENCES_SUFFIX'
        final String diffSurrogateName = name + DIFFERENCES_SUFFIX;
        
        // WILL BE UPDATED IN EVERY CALL OF updateDifferencesCentre!
        try {
            // init (or update) diff centre from persistent storage if exists
            globalManager.initEntityCentreManager(miType, diffSurrogateName);
        } catch (final IllegalArgumentException e) {
            if (e.getMessage().startsWith("Unable to initialise a non-existent entity-centre instance for type")) {
                // diff centre does not exist in persistent storage yet -- initialise EMPTY diff (there potentially can be some values from 'default centre',
                //   but diff centre will be empty disregarding that fact -- no properties were marked as changed; but initialisation from 'default centre' is important --
                //   this makes diff centre nicely synchronised with Web UI default values)
                getDefaultCentre(globalManager, miType);

                globalManager.saveAsEntityCentreManager(miType, null, diffSurrogateName);
            } else {
                throw e;
            }
        }
        final ICentreDomainTreeManagerAndEnhancer differencesCentre = globalManager.getEntityCentreManager(miType, diffSurrogateName);

        final DateTime end = new DateTime();
        final Period pd = new Period(start, end);
        logger.debug(String.format("\t%s the '%s' centre for miType [%s] for user %s... done in [%s].", "updateDifferencesCentre", name, miType.getSimpleName(), globalManager.getUserProvider().getUser(), pd.getSeconds() + " s " + pd.getMillis() + " ms"));
        return differencesCentre;
    }
    
    /**
     * Initialises 'virtual' (should never be persistent) centre -- caches it on the server (into currentCentres only).
     *
     * @param gdtm
     * @param miType
     * @param name -- surrogate name of the centre (fresh, previouslyRun etc.)
     * @param centre
     *  
     * @return
     */
    private static void initCentre(final IGlobalDomainTreeManager gdtm, final Class<? extends MiWithConfigurationSupport<?>> miType, final String name, final ICentreDomainTreeManagerAndEnhancer centre) {
        synchronized (gdtm) {
            ((GlobalDomainTreeManager) gdtm).overrideCentre(miType, name, centre);
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
                targetCentre.getSecondTick().toggleOrdering(root, newOrderedProperty.getKey());
                if (Ordering.DESCENDING == newOrderedProperty.getValue()) {
                    targetCentre.getSecondTick().toggleOrdering(root, newOrderedProperty.getKey());
                }
            }
        }
        
        return targetCentre;
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
     * @param name -- surrogate name of the centre (fresh, previouslyRun etc.)
     * @param newDiffCentre
     * 
     * @return
     */
    private static void overrideAndSaveDifferencesCentre(final IGlobalDomainTreeManager gdtm, final Class<? extends MiWithConfigurationSupport<?>> miType, final String name, final ICentreDomainTreeManagerAndEnhancer newDiffCentre) {
        logger.debug(String.format("\t%s '%s' centre for miType [%s] for user %s...", "overrideAndSaveDifferencesCentre", name, miType.getSimpleName(), gdtm.getUserProvider().getUser()));
        final DateTime start = new DateTime();
        
        // the name consists of 'name' and 'DIFFERENCES_SUFFIX'
        final String diffSurrogateName = name + DIFFERENCES_SUFFIX;
        
        // In case where diff centre was not ever initialised from persistent storage -- it should be initialised for the first time.
        // It guarantees that at the point of diff centre saving, the empty diff was already saved. See method 'updateDifferencesCentre' for more details.
        final ICentreDomainTreeManagerAndEnhancer staleDiffCentre = updateDifferencesCentreOnlyIfNotInitialised(gdtm, miType, name);
        final boolean diffChanged = !EntityUtils.equalsEx(staleDiffCentre, newDiffCentre);
        
        if (diffChanged) {
            initCentre(gdtm, miType, diffSurrogateName, newDiffCentre);
            gdtm.saveEntityCentreManager(miType, diffSurrogateName);
        }

        final DateTime end = new DateTime();
        final Period pd = new Period(start, end);
        logger.debug(String.format("\t%s the '%s' centre for miType [%s] for user %s... done in [%s].", "overrideAndSaveDifferencesCentre" + (diffChanged ? "" : " (nothing has changed)"), name, miType.getSimpleName(), gdtm.getUserProvider().getUser(), pd.getSeconds() + " s " + pd.getMillis() + " ms"));
    }
}
