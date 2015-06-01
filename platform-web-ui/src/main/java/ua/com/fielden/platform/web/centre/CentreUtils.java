package ua.com.fielden.platform.web.centre;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ua.com.fielden.platform.criteria.generator.impl.CriteriaReflector;
import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager.MetaValueType;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTree;
import ua.com.fielden.platform.domaintree.impl.GlobalDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.CritOnly.Type;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.swing.menu.MiType;
import ua.com.fielden.platform.swing.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.utils.EntityUtils;

/**
 * This utility class contains the methods that are shared across {@link CentreResource} and {@link CriteriaResource}.
 *
 * @author TG Team
 *
 */
public class CentreUtils<T extends AbstractEntity<?>> {
    private final static Logger logger = Logger.getLogger(CentreUtils.class);
    private static final String DIFFERENCES_CENTRE_NAME = "__________DIFFERENCES_CENTRE_NAME";
    private static final String FRESH_CENTRE_NAME = "__________FRESH_CENTRE_NAME";

    public CentreUtils() {
    }

    /**
     * Returns <code>true</code> if the centre is changed (and thus can be saved / discarded), <code>false</code> otherwise.
     *
     * @param miType
     * @param gdtm
     * @return
     */
    public static boolean isFreshCentreChanged(final Class<? extends MiWithConfigurationSupport<?>> miType, final IGlobalDomainTreeManager gdtm) {
        final boolean isCentreChanged = gdtm.isChangedEntityCentreManager(miType, FRESH_CENTRE_NAME);
        logger.debug("isCentreChanged == " + isCentreChanged);
        return isCentreChanged;
    }

    /**
     * Returns the current version of default centre manager (initialises it in case if it is not created yet).
     * <p>
     * Currently it is created from default configuration in Mi types (CDTMAE is used), but later it should be initialised from Entity Centre DSL.
     *
     * @param globalManager
     * @param miType
     * @return
     */
    public static ICentreDomainTreeManagerAndEnhancer getDefaultCentre(final IGlobalDomainTreeManager globalManager, final Class<? extends MiWithConfigurationSupport<?>> miType) {
        if (globalManager.getEntityCentreManager(miType, null) == null) {
            globalManager.initEntityCentreManager(miType, null);
        }

        if (globalManager.isChangedEntityCentreManager(miType, null)) {
            throw new IllegalStateException("Should be not changed (after init).");
        }

        return globalManager.getEntityCentreManager(miType, null);
    }

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
     * @return
     */
    public static ICentreDomainTreeManagerAndEnhancer getFreshCentre(final IGlobalDomainTreeManager gdtm, final Class<? extends MiWithConfigurationSupport<?>> miType) {
        if (gdtm.getEntityCentreManager(miType, FRESH_CENTRE_NAME) == null) {
            final ICentreDomainTreeManagerAndEnhancer freshCentre =
                    applyDifferences(
                            ((GlobalDomainTreeManager) gdtm).copyCentre(getDefaultCentre(gdtm, miType)),
                            getDifferencesCentre(gdtm, miType),
                            getEntityType(miType)
                    );

            ((GlobalDomainTreeManager) gdtm).init(miType, FRESH_CENTRE_NAME, freshCentre, true);
        }
        return freshCentre(gdtm, miType);
    }

    /**
     * Returns the current version of principle centre manager (it assumes that it should be initialised!).
     *
     * @param gdtm
     * @param miType
     * @return
     */
    public static ICentreDomainTreeManagerAndEnhancer freshCentre(final IGlobalDomainTreeManager gdtm, final Class<? extends MiWithConfigurationSupport<?>> miType) {
        if (gdtm.getEntityCentreManager(miType, FRESH_CENTRE_NAME) == null) {
            throw new IllegalStateException("The 'fresh centre' should be initialised.");
        }
        return gdtm.getEntityCentreManager(miType, FRESH_CENTRE_NAME);
    }

    /**
     * Removes fresh centre (to be able later to re-populate it automatically).
     *
     * @param gdtm
     * @param miType
     * @return
     */
    public static void removeFreshCentre(final IGlobalDomainTreeManager gdtm, final Class<? extends MiWithConfigurationSupport<?>> miType) {
        ((GlobalDomainTreeManager) gdtm).removeCentre(miType, FRESH_CENTRE_NAME);
    }

    /**
     * Overrides old 'differences centre' with new one and saves it.
     *
     * @param gdtm
     * @param miType
     * @return
     */
    public static void overrideAndSaveDifferencesCentre(final IGlobalDomainTreeManager gdtm, final Class<? extends MiWithConfigurationSupport<?>> miType, final ICentreDomainTreeManagerAndEnhancer newDifferencesCentre) {
        ((GlobalDomainTreeManager) gdtm).overrideCentre(miType, DIFFERENCES_CENTRE_NAME, newDifferencesCentre);
        gdtm.saveEntityCentreManager(miType, DIFFERENCES_CENTRE_NAME);
    }

    /**
     * Discards fresh centre (throws an exception if it was not changed).
     *
     * @param gdtm
     * @param miType
     * @return
     */
    public static void discardFreshCentre(final IGlobalDomainTreeManager gdtm, final Class<? extends MiWithConfigurationSupport<?>> miType) {
        if (gdtm.isChangedEntityCentreManager(miType, FRESH_CENTRE_NAME)) {
            gdtm.discardEntityCentreManager(miType, FRESH_CENTRE_NAME);
        } else {
            final String message = "Can not discard the centre that was not changed.";
            logger.error(message);
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Initialises 'differences centre' from the persistent storage, if it exists.
     * <p>
     * If no 'differences centre' exists -- the following steps are performed:
     * <p>
     * 1. make sure that 'defalt centre' exists in gdtm;<br>
     * 2. make saveAs from 'default centre' which will be 'diff centre' (this promotes the empty diff to the storage!)<br>
     * 3. applies correct default values to be in sync with Web UI ones (on top of 'diff centre');<br>
     * 4. saves the 'diff centre's changes;<br>
     *
     * @param globalManager
     * @param miType
     * @return
     */
    private static ICentreDomainTreeManagerAndEnhancer getDifferencesCentre(final IGlobalDomainTreeManager globalManager, final Class<? extends MiWithConfigurationSupport<?>> miType) {
        if (globalManager.getEntityCentreManager(miType, DIFFERENCES_CENTRE_NAME) == null) {
            try {
                globalManager.initEntityCentreManager(miType, DIFFERENCES_CENTRE_NAME);
            } catch (final IllegalArgumentException e) {
                if (e.getMessage().startsWith("Unable to initialise a non-existent entity-centre instance for type")) {
                    getDefaultCentre(globalManager, miType);

                    globalManager.saveAsEntityCentreManager(miType, null, DIFFERENCES_CENTRE_NAME);

                    if (globalManager.isChangedEntityCentreManager(miType, DIFFERENCES_CENTRE_NAME)) {
                        throw new IllegalStateException("Should be not changed.");
                    }

                    applyDefaultValues(globalManager.getEntityCentreManager(miType, DIFFERENCES_CENTRE_NAME), getEntityType(miType));

                    globalManager.saveEntityCentreManager(miType, DIFFERENCES_CENTRE_NAME);

                    if (globalManager.isChangedEntityCentreManager(miType, DIFFERENCES_CENTRE_NAME)) {
                        throw new IllegalStateException("Should be not changed.");
                    }
                } else {
                    throw e;
                }
            }

            if (globalManager.isChangedEntityCentreManager(miType, DIFFERENCES_CENTRE_NAME)) {
                throw new IllegalStateException("Should be not changed.");
            }
        }
        final ICentreDomainTreeManagerAndEnhancer differencesCentre = globalManager.getEntityCentreManager(miType, DIFFERENCES_CENTRE_NAME);
        return differencesCentre;
    }

    /**
     * Applies correct default values to be in sync with Web UI ones on top of 'centre'.
     *
     * @param centre
     * @param root
     */
    private static void applyDefaultValues(final ICentreDomainTreeManagerAndEnhancer centre, final Class<AbstractEntity<?>> root) {
        for (final String property : centre.getFirstTick().checkedProperties(root)) {
            if (!AbstractDomainTree.isPlaceholder(property)) {
                final boolean isEntityItself = "".equals(property); // empty property means "entity itself"
                final Class<?> propertyType = isEntityItself ? managedType(root, centre) : PropertyTypeDeterminator.determinePropertyType(managedType(root, centre), property);
                final CritOnly critAnnotation = isEntityItself ? null : AnnotationReflector.getPropertyAnnotation(CritOnly.class, managedType(root, centre), property);
                final boolean single = critAnnotation != null && Type.SINGLE.equals(critAnnotation.value());

                if (!EntityUtils.isBoolean(propertyType) && !(EntityUtils.isEntityType(propertyType) && !single)) {
                    centre.getFirstTick().setValue(root, property, null);
                    if (AbstractDomainTree.isDoubleCriterion(managedType(root, centre), property)) {
                        centre.getFirstTick().setValue2(root, property, null);
                    }
                }
            }
        }
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
     * Determines the entity type for which criteria entity will be generated.
     *
     * @param miType
     * @return
     */
    public static <T extends AbstractEntity<?>> Class<T> getEntityType(final Class<? extends MiWithConfigurationSupport<?>> miType) {
        final EntityType entityTypeAnnotation = miType.getAnnotation(EntityType.class);
        if (entityTypeAnnotation == null) {
            throw new IllegalStateException(String.format("The menu item type [%s] must be annotated with EntityType annotation", miType.getName()));
        }
        return (Class<T>) entityTypeAnnotation.value();
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
     * Determines the miType for which criteria entity was generated.
     *
     * @param miType
     * @return
     */
    public static Class<? extends MiWithConfigurationSupport<?>> getMiType(final Class<? extends AbstractEntity<?>> criteriaType) {
        final MiType annotation = AnnotationReflector.getAnnotation(criteriaType, MiType.class);
        if (annotation == null) {
            throw new IllegalStateException(String.format("The criteria type [%s] should be annotated with MiType annotation.", criteriaType.getName()));
        }
        return annotation.value();
    }

    /**
     * Determines the master type for which criteria entity was generated.
     *
     * @param criteriaType
     * @return
     */
    public static Class<? extends AbstractEntity<?>> getOriginalType(final Class<? extends AbstractEntity<?>> criteriaType) {
        return getEntityType(getMiType(criteriaType));
    }

    /**
     * Determines the property name of the property from which the criteria property was generated. This is only applicable for entity typed properties.
     *
     * @param propertyName
     * @return
     */
    public static String getOriginalPropertyName(final Class<?> criteriaClass, final String propertyName) {
        return CriteriaReflector.getCriteriaProperty(criteriaClass, propertyName);
    }

    /**
     * Determines the managed (in cdtmae) counter-part for master type for which criteria entity was generated.
     *
     * @param criteriaType
     * @param cdtmae
     * @return
     */
    public static Class<?> getOriginalManagedType(final Class<? extends AbstractEntity<?>> criteriaType, final ICentreDomainTreeManagerAndEnhancer cdtmae) {
        return cdtmae.getEnhancer().getManagedType(CentreUtils.getOriginalType(criteriaType));
    }
}
