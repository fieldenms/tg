package ua.com.fielden.platform.web.centre;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.criteria.generator.impl.CriteriaReflector;
import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.AnnotationReflector;
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
public class CentreUtils<T extends AbstractEntity<?>> extends CentreUpdater {
    private final static Logger logger = Logger.getLogger(CentreUtils.class);

    public CentreUtils() {
    }
    
    /**
     * Initialises and commits previouslyRun centre from the passed <code>preparedPreviouslyRunCentre</code> instance.
     * 
     * @param gdtm
     * @param miType
     * @param preparedPreviouslyRunCentre
     */
    public static void initAndCommitPreviouslyRunCentre(final IGlobalDomainTreeManager gdtm, final Class<? extends MiWithConfigurationSupport<?>> miType, final ICentreDomainTreeManagerAndEnhancer preparedPreviouslyRunCentre) {
        // in case where diff centre is not in the database -- empty diff should be populated into database
        updateDifferencesCentreOnlyIfNotInitialised(gdtm, miType, PREVIOUSLY_RUN_CENTRE_NAME);
        // init 'previously Run centre'
        initCentre(gdtm, miType, PREVIOUSLY_RUN_CENTRE_NAME, preparedPreviouslyRunCentre);
        // and then commit it to the database (save its diff)
        commitCentre(gdtm, miType, PREVIOUSLY_RUN_CENTRE_NAME);
    }
    
    /**
     * Initialises and commits 'saved' centre from the passed <code>preparedSavedCentre</code> instance.
     * 
     * @param gdtm
     * @param miType
     * @param preparedSavedCentre
     */
    public static void initAndCommitSavedCentre(final IGlobalDomainTreeManager gdtm, final Class<? extends MiWithConfigurationSupport<?>> miType, final ICentreDomainTreeManagerAndEnhancer preparedSavedCentre) {
        // TODO in case where 'saved' centre is not in the database -- empty one should be populated into database!!
        // TODO updateDifferencesCentreOnlyIfNotInitialised(gdtm, miType, SAVED_CENTRE_NAME);
        
        // init 'saved centre'
        initCentre(gdtm, miType, SAVED_CENTRE_NAME, preparedSavedCentre);
        // and then commit it to the database (save its diff)
        commitCentre(gdtm, miType, SAVED_CENTRE_NAME);
    }
    
    /**
     * Initialises and commits 'fresh' centre from the passed <code>preparedFreshCentre</code> instance.
     * 
     * @param gdtm
     * @param miType
     * @param preparedFreshCentre
     */
    public static void initAndCommitFreshCentre(final IGlobalDomainTreeManager gdtm, final Class<? extends MiWithConfigurationSupport<?>> miType, final ICentreDomainTreeManagerAndEnhancer preparedFreshCentre) {
        // TODO in case where 'fresh' centre is not in the database -- empty one should be populated into database!!
        // TODO updateDifferencesCentreOnlyIfNotInitialised(gdtm, miType, FRESH_CENTRE_NAME);
        
        // init 'fresh centre'
        initCentre(gdtm, miType, FRESH_CENTRE_NAME, preparedFreshCentre);
        // and then commit it to the database (save its diff)
        commitCentre(gdtm, miType, FRESH_CENTRE_NAME);
    }
    
    /**
     * Commits and updates fresh centre. Usually this should be done after the fresh centre has been changed from client-side modifPropsHolder.
     * 
     * @param gdtm
     * @param miType
     * @return
     */
    public static ICentreDomainTreeManagerAndEnhancer commitAndUpdateFreshCentre(final IGlobalDomainTreeManager gdtm, final Class<? extends MiWithConfigurationSupport<?>> miType) {
        // TODO can we do that more efficiently??!!
        // TODO can we do that more efficiently??!!
        // TODO can we do that more efficiently??!!
        // TODO can we do that more efficiently??!!
        // TODO can we do that more efficiently??!!
        // TODO can we do that more efficiently??!!
        // TODO can we do that more efficiently??!!
        // TODO can we do that more efficiently??!!
        // TODO can we do that more efficiently??!!
        // TODO can we do that more efficiently??!!
        // TODO can we do that more efficiently??!!
        // TODO can we do that more efficiently??!!
        commitCentre(gdtm, miType, FRESH_CENTRE_NAME);
        return updateCentre(gdtm, miType, FRESH_CENTRE_NAME);
    }

    /**
     * Returns <code>true</code> if the centre is changed (and thus can be saved / discarded), <code>false</code> otherwise.
     *
     * @param miType
     * @param gdtm
     * @return
     */
    public static boolean isFreshCentreChanged(final Class<? extends MiWithConfigurationSupport<?>> miType, final IGlobalDomainTreeManager gdtm) {
        final boolean isCentreChanged = !EntityUtils.equalsEx(CentreUpdater.updateCentre(gdtm, miType, CentreUpdater.FRESH_CENTRE_NAME), CentreUpdater.updateCentre(gdtm, miType, CentreUpdater.SAVED_CENTRE_NAME));
        logger.debug("isCentreChanged == " + isCentreChanged);
        return isCentreChanged;
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
