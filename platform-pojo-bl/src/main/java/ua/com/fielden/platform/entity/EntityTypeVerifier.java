package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.entity.annotation.DeactivatableDependencies;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.exceptions.EntityDefinitionException;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.EntityMetadata;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.types.RichText;

import java.util.Collection;

import static java.lang.String.format;
import static ua.com.fielden.platform.entity.AbstractEntity.fieldsForProperties;
import static ua.com.fielden.platform.entity.annotation.IsProperty.*;
import static ua.com.fielden.platform.entity.exceptions.EntityDefinitionException.*;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.isNumeric;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.stripIfNeeded;
import static ua.com.fielden.platform.utils.EntityUtils.*;

public class EntityTypeVerifier implements IEntityTypeVerifier {

    @Override
    public void verify(final Class<? extends AbstractEntity<?>> entityType)
            throws EntityDefinitionException
    {
        try {
            verify_(entityType);
        }
        catch (final EntityDefinitionException e) {
            throw e;
        }
        catch (final Exception e) {
            throw new EntityDefinitionException(format("Verification of entity type [%s] failed.", stripIfNeeded(entityType).getSimpleName()), e);
        }
    }

    private void verify_(final Class<? extends AbstractEntity<?>> entityType) {
        // For enhanced types that do not contain structural enhancements, "strip" them by using the original type.
        // This should not be done for structurally enhanced types, which may be enhanced with additional properties to be verified.
        final var origEntityType = (Class<? extends AbstractEntity<?>>) stripIfNeeded(entityType);
        if (!ActivatableAbstractEntity.class.isAssignableFrom(origEntityType)
            && origEntityType.isAnnotationPresent(DeactivatableDependencies.class))
        {
            throw new EntityDefinitionException(format("Non-activatable entity [%s] cannot have deactivatable dependencies.", origEntityType.getSimpleName()));
        }

        for (final var prop : fieldsForProperties(origEntityType)) {
            Reflector.obtainPropertyAccessor(origEntityType, prop.getName());
            final Class<?> propType = EntityMetadata.determinePropType(origEntityType, prop);
            final boolean isNumeric = isNumeric(propType);
            final boolean isCollectional = Collection.class.isAssignableFrom(propType);
            final IsProperty isPropertyAnnotation = AnnotationReflector.getAnnotation(prop, IsProperty.class);
            final Class<?> propertyAnnotationType = isPropertyAnnotation.value();

            if (!isNumeric &&
                (isPropertyAnnotation.precision() != DEFAULT_PRECISION ||
                 isPropertyAnnotation.scale() != DEFAULT_SCALE ||
                 isPropertyAnnotation.trailingZeros() != DEFAULT_TRAILING_ZEROS)) {
                final String error = format(INVALID_USE_OF_NUMERIC_PARAMS_MSG, prop.getName(), origEntityType.getName());
                throw new EntityDefinitionException(error);
            }

            if (isNumeric &&
                (isPropertyAnnotation.precision() != DEFAULT_PRECISION || isPropertyAnnotation.scale() != DEFAULT_SCALE) &&
                (isPropertyAnnotation.precision() <= 0 || isPropertyAnnotation.scale() < 0)) {
                final String error = format(INVALID_USE_FOR_PRECISION_AND_SCALE_MSG, prop.getName(), origEntityType.getName());
                throw new EntityDefinitionException(error);
            }

            if (isNumeric && isPropertyAnnotation.precision() != DEFAULT_PRECISION && isPropertyAnnotation.precision() <= isPropertyAnnotation.scale()) {
                final String error = format(INVALID_VALUES_FOR_PRECISION_AND_SCALE_MSG, prop.getName(), origEntityType.getName());
                throw new EntityDefinitionException(error);

            }

            if (!isString(propType) && !isHyperlink(propType) && !RichText.class.isAssignableFrom(propType) && !propType.isArray() && isPropertyAnnotation.length() != DEFAULT_LENGTH) {
                final String error = format(INVALID_USE_OF_PARAM_LENGTH_MSG, prop.getName(), origEntityType.getName());
                throw new EntityDefinitionException(error);
            }

            if ((isCollectional || PropertyDescriptor.class.isAssignableFrom(propType)) && (propertyAnnotationType == Void.class || propertyAnnotationType == null)) {
                final String error = format(COLLECTIONAL_PROP_MISSING_TYPE_MSG, prop.getName(), origEntityType.getName());
                throw new EntityDefinitionException(error);
            }

            if (isCollectional && isLinkPropertyRequiredButMissing(origEntityType, prop.getName())) {
                final String error = format(COLLECTIONAL_PROP_MISSING_LINK_MSG, prop.getName(), origEntityType.getName());
                throw new EntityDefinitionException(error);
            }

            // FIXME there are cases where entities inherit from an entity with implicitly-calculated one-2-one associations, which fail the association check
            // Finder.isOne2One_association uses "equals" to validate the key of the one-2-one- entity matching the holding entity type.
            // This needs to be considered and resolved.
            // if (EntityUtils.isEntityType(type) && EntityUtils.isEntityType(PropertyTypeDeterminator.determinePropertyType(type, KEY))
            //        && !Finder.isOne2One_association(origEntityType, prop.getName())) {
            //    final String error = format(INVALID_ONE2ONE_ASSOCIATION_MSG, prop.getName(), strippedEntityType.getName());
            //    logger.error(error);
            //    throw new EntityDefinitionException(error);
            //}
        }
    }

    private boolean isLinkPropertyRequiredButMissing(final Class<? extends AbstractEntity<?>> entityType, final String propName) {
        if (AbstractFunctionalEntityForCollectionModification.class.isAssignableFrom(entityType)
            && AbstractFunctionalEntityForCollectionModification.isCollectionOfIds(propName))
        {
            return false;
        }
        else {
            return isPersistentEntityType(entityType) && !Finder.hasLinkProperty(entityType, propName);
        }
    }

}
