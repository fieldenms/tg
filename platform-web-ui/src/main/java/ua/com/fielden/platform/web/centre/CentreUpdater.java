package ua.com.fielden.platform.web.centre;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Period;

import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager.MetaValueType;
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

public class CentreUpdater {
    private final static Logger logger = Logger.getLogger(CentreUpdater.class);
    private static final String DIFFERENCES_SUFFIX = "__________DIFFERENCES";
    
    public static final String FRESH_CENTRE_NAME = "__________FRESH";
    public static final String PREVIOUSLY_RUN_CENTRE_NAME = "__________PREVIOUSLY_RUN";

    /**
     * Returns the current version of edited by the user centre manager (initialises it in case if it is not created yet).
     * <p>
     * Initialisation goes through the following chain: 'default centre' + 'differences centre' := 'initial fresh centre'. Later the user make its own diffs on top of the 'initial
     * fresh centre'.
     * <p>
     * Fresh centre is never saved, but it is used to create 'differences centre' (when saving is performed) and then it is removed.
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
                logger.info(String.format("Initialising '%s' centre for miType [%s] for the first time for user %s...", name, miType.getSimpleName(), gdtm.getUserProvider().getUser()));
                final DateTime start = new DateTime();

                final ICentreDomainTreeManagerAndEnhancer loadedDiffCentre = updateDifferencesCentre(gdtm, miType, name);
                
                final ICentreDomainTreeManagerAndEnhancer loadedCentre = loadCentreFromDefaultAndDiff(gdtm, miType, name, loadedDiffCentre);
                initCentre(gdtm, miType, name, loadedCentre);
                
                final DateTime end = new DateTime();
                final Period pd = new Period(start, end);
                logger.info(String.format("Initialised the '%s' centre for miType [%s] for the first time for user %s... done in [%s].", name, miType.getSimpleName(), gdtm.getUserProvider().getUser(), pd.getSeconds() + " s " + pd.getMillis() + " ms"));
                return centre(gdtm, miType, name);
            } else {
                if (isDiffCentreStale(gdtm, miType, name)) {
                    logger.info(String.format("Updating of stale '%s' centre for miType [%s] for user %s...", name, miType.getSimpleName(), gdtm.getUserProvider().getUser()));
                    final DateTime start = new DateTime();
                    
                    final ICentreDomainTreeManagerAndEnhancer updatedDiffCentre = updateDifferencesCentre(gdtm, miType, name);
                    
                    removeCentre(gdtm, miType, name);
                    final ICentreDomainTreeManagerAndEnhancer loadedCentre = loadCentreFromDefaultAndDiff(gdtm, miType, name, updatedDiffCentre);
                    initCentre(gdtm, miType, name, loadedCentre);
                    
                    final DateTime end = new DateTime();
                    final Period pd = new Period(start, end);
                    logger.info(String.format("Updated stale '%s' centre for miType [%s] for user %s... done in [%s].", name, miType.getSimpleName(), gdtm.getUserProvider().getUser(), pd.getSeconds() + " s " + pd.getMillis() + " ms"));
                    return centre(gdtm, miType, name);
                } else {
                    return centre(gdtm, miType, name);
                }
            }
        }
    }
    
    public static void commitCentre(final IGlobalDomainTreeManager gdtm, final Class<? extends MiWithConfigurationSupport<?>> miType, final String name) {
        // gets the centre (that was created from the chain 'default centre' + 'saved diff centre' + 'current user diff' := 'centre')
        final ICentreDomainTreeManagerAndEnhancer centre = centre(gdtm, miType, name);
        // removes the centre -- to be later re-populated
        removeCentre(gdtm, miType, name);

        final ICentreDomainTreeManagerAndEnhancer defaultCentre = getDefaultCentre(gdtm, miType);
        // creates differences centre from the differences between 'default centre' and 'centre'
        final ICentreDomainTreeManagerAndEnhancer differencesCentre = createDifferencesCentre(centre, defaultCentre, CentreUtils.getEntityType(miType), gdtm);

        // override old 'diff centre' with recently created one and save it
        overrideAndSaveDifferencesCentre(gdtm, miType, name, differencesCentre);
    }
    
    private static boolean isDiffCentreStale(final IGlobalDomainTreeManager gdtm, final Class<? extends MiWithConfigurationSupport<?>> miType, final String name) {
        // the name consists of 'name' and 'DIFFERENCES_SUFFIX'
        final String diffSurrogateName = name + DIFFERENCES_SUFFIX;
        
        // ensure that diff centre exists (it should)
        final CentreDomainTreeManagerAndEnhancer currentDiffCentre = (CentreDomainTreeManagerAndEnhancer) centre(gdtm, miType, diffSurrogateName);
        return ((GlobalDomainTreeManager) gdtm).isStale(currentDiffCentre.getSavedEntityId(), currentDiffCentre.getSavedEntityVersion());
    }

    /**
     * Loads centre through the following chain: 'default centre' + 'differences centre' := 'initial centre'. Later the user make its own diffs on top of the 'initial
     * centre'.
     *
     * @param gdtm
     * @param miType
     * @param name -- surrogate name of the centre (fresh, previouslyRun etc.)
     * @param centre
     *  
     * @return
     */
    private static ICentreDomainTreeManagerAndEnhancer loadCentreFromDefaultAndDiff(final IGlobalDomainTreeManager gdtm, final Class<? extends MiWithConfigurationSupport<?>> miType, final String name, final ICentreDomainTreeManagerAndEnhancer diffCentre) {
        // TODO consider not copying of default centre for performance reasons:
        final ICentreDomainTreeManagerAndEnhancer defaultCentreCopy = ((GlobalDomainTreeManager) gdtm).copyCentre(getDefaultCentre(gdtm, miType));
        // applies diffCentre on top of defaultCentreCopy to producer loadedCentre:
        final ICentreDomainTreeManagerAndEnhancer loadedCentre = applyDifferences(defaultCentreCopy, diffCentre, CentreUtils.getEntityType(miType));
        // For all generated types on freshCentre (and on its derivatives like 'unchanged freshCentre', 'run centre', 'unchanged run centre' etc.) there is a need to
        //  provide miType information inside its generated type to be sent to the client application. This is done through the use of 
        //  annotation miType and, in future, other custom annotations, for example @SaveAsName.
        // Please note that copyCentre method in GlobalDomainTreeManager performs copying of all defined annotations to provide freshCentre's derivatives
        //  with such additional information too.
        for (final Class<?> root: loadedCentre.getRepresentation().rootTypes()) {
            if (DynamicEntityClassLoader.isGenerated(loadedCentre.getEnhancer().getManagedType(root))) {
                loadedCentre.getEnhancer().adjustManagedTypeAnnotations(root, new MiTypeAnnotation().newInstance(miType));
            }
        }
        return loadedCentre;
    }
    
    /**
     * Returns the current version of default centre manager (initialises it in case if it is not created yet).
     * <p>
     * Currently it is created from Entity Centre DSL through the special gdtm, which knows about Centre DSL configuration.
     *
     * IMPORTANT: this 'default centre' is used for constructing 'fresh centre', 'previouslyRun centre' and 'diff centre', that is why it is very important to make it suitable for Web UI default values.
     * All other centre will reuse such Web UI specific default values.
     *
     * @param globalManager
     * @param miType
     * @return
     */
    private static ICentreDomainTreeManagerAndEnhancer getDefaultCentre(final IGlobalDomainTreeManager globalManager, final Class<? extends MiWithConfigurationSupport<?>> miType) {
        if (globalManager.getEntityCentreManager(miType, null) == null) {
            // standard init (from Centre DSL config)
            globalManager.initEntityCentreManager(miType, null);
            
            // TODO is there any need to have default centre cached in 'persistentCentres', not only in 'currentCentres'? Can we avoid unnecessary copying and / or checking on isChanged?

            // check if it is ok (not changed)
            if (globalManager.isChangedEntityCentreManager(miType, null)) {
                throw new IllegalStateException("Should be not changed (after init).");
            }

            // Web UI default values application
            final ICentreDomainTreeManagerAndEnhancer defaultedCentre = applyWebUIDefaultValues(
                    globalManager.getEntityCentreManager(miType, null),
                    CentreUtils.getEntityType(miType) //
            );
            ((GlobalDomainTreeManager) globalManager).init(miType, null, defaultedCentre, true);

            // check if it is ok (not changed)
            if (globalManager.isChangedEntityCentreManager(miType, null)) {
                throw new IllegalStateException("Should be not changed (after init of defaulted centre instance).");
            }
        }

        // check if it is ok (not changed)
        if (globalManager.isChangedEntityCentreManager(miType, null)) {
            throw new IllegalStateException("Should be not changed.");
        }

        return globalManager.getEntityCentreManager(miType, null);
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
    public static ICentreDomainTreeManagerAndEnhancer updateDifferencesCentreOnlyIfNotInitialised(final IGlobalDomainTreeManager globalManager, final Class<? extends MiWithConfigurationSupport<?>> miType, final String name) {
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
        // the name consists of 'name' and 'DIFFERENCES_SUFFIX'
        final String diffSurrogateName = name + DIFFERENCES_SUFFIX;
        
        // WILL BE UPDATED IN EVERY CALL OF updateDifferencesCentre!
        try {
            // init diff centre from persistent storage if exists
            globalManager.initEntityCentreManager(miType, diffSurrogateName);
        } catch (final IllegalArgumentException e) {
            if (e.getMessage().startsWith("Unable to initialise a non-existent entity-centre instance for type")) {
                // diff centre does not exist in persistent storage yet -- initialise EMPTY diff (there potentially can be some values from 'default centre',
                //   but diff centre will be empty disregarding that fact -- no properties were marked as changed; but initialisation from 'default centre' is important --
                //   this makes diff centre nicely synchronised with Web UI default values)
                getDefaultCentre(globalManager, miType);

                globalManager.saveAsEntityCentreManager(miType, null, diffSurrogateName);

                if (globalManager.isChangedEntityCentreManager(miType, diffSurrogateName)) {
                    throw new IllegalStateException("Should be not changed.");
                }
            } else {
                throw e;
            }
        }

        if (globalManager.isChangedEntityCentreManager(miType, diffSurrogateName)) {
            throw new IllegalStateException("Should be not changed.");
        }
        final ICentreDomainTreeManagerAndEnhancer differencesCentre = globalManager.getEntityCentreManager(miType, diffSurrogateName);
        return differencesCentre;
    }
    
    /**
     * Initialises 'virtual' (should never be persistent) centre -- caches it on the server.
     *
     * @param gdtm
     * @param miType
     * @param name -- surrogate name of the centre (fresh, previouslyRun etc.)
     * @param centre
     *  
     * @return
     */
    private static void initCentre(final IGlobalDomainTreeManager gdtm, final Class<? extends MiWithConfigurationSupport<?>> miType, final String name, final ICentreDomainTreeManagerAndEnhancer centre) {
        initUnchangedCentreManager(gdtm, miType, name, centre);

        if (gdtm.isChangedEntityCentreManager(miType, name)) {
            throw new IllegalStateException("Should be not changed.");
        }
    }
    
    /**
     * Removes centre (to be able later to re-populate it automatically).
     *
     * @param gdtm
     * @param miType
     * @param name -- surrogate name of the centre (fresh, previouslyRun etc.)
     *  
     * @return
     */
    private static void removeCentre(final IGlobalDomainTreeManager gdtm, final Class<? extends MiWithConfigurationSupport<?>> miType, final String name) {
        ((GlobalDomainTreeManager) gdtm).removeCentre(miType, name);
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
        if (gdtm.getEntityCentreManager(miType, name) == null) {
            throw new IllegalStateException(String.format("The '%s' centre should be initialised.", name));
        }
        return gdtm.getEntityCentreManager(miType, name);
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
        final ICentreDomainTreeManagerAndEnhancer differencesCentre = ((GlobalDomainTreeManager) gdtm).copyCentre(centre);

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
    private static Class<?> managedType(final Class<AbstractEntity<?>> root, final ICentreDomainTreeManagerAndEnhancer centre) {
        return centre.getEnhancer().getManagedType(root);
    }
    
    static void initUnchangedCentreManager(final IGlobalDomainTreeManager gdtm, final Class<?> menuItemType, final String name, final ICentreDomainTreeManagerAndEnhancer mgr) {
        ((GlobalDomainTreeManager) gdtm).init(menuItemType, name, mgr, true);
    }
    
    /**
     * Overrides old 'differences centre' with new one and saves it.
     *
     * @param gdtm
     * @param miType
     * @param name -- surrogate name of the centre (fresh, previouslyRun etc.)
     * @param newDifferencesCentre
     * 
     * @return
     */
    private static void overrideAndSaveDifferencesCentre(final IGlobalDomainTreeManager gdtm, final Class<? extends MiWithConfigurationSupport<?>> miType, final String name, final ICentreDomainTreeManagerAndEnhancer newDifferencesCentre) {
        // the name consists of 'name' and 'DIFFERENCES_SUFFIX'
        final String diffSurrogateName = name + DIFFERENCES_SUFFIX;
        
        ((GlobalDomainTreeManager) gdtm).overrideCentre(miType, diffSurrogateName, newDifferencesCentre);
        gdtm.saveEntityCentreManager(miType, diffSurrogateName);
    }
}
