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
     * Returns <code>true</code> if the centre is changed (and thus can be saved / discarded) from client application perspective, <code>false</code> otherwise.
     *
     * @param updatedFreshCentre -- updated instance of 'fresh' centre
     * @param updatedSavedCentre -- updated instance of 'saved' centre
     * @return
     */
    public static boolean isFreshCentreChanged(final ICentreDomainTreeManagerAndEnhancer updatedFreshCentre, final ICentreDomainTreeManagerAndEnhancer updatedSavedCentre) {
        final boolean isCentreChanged = !EntityUtils.equalsEx(updatedFreshCentre, updatedSavedCentre);
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
