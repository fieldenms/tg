package ua.com.fielden.platform.meta;

import com.google.common.collect.ImmutableList;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.query.ICompositeUserTypeInstantiate;
import ua.com.fielden.platform.meta.PropertyMetadataImpl.Calculated;
import ua.com.fielden.platform.meta.exceptions.DomainMetadataGenerationException;

import java.util.List;
import java.util.StringJoiner;
import java.util.function.Predicate;

import static java.util.Arrays.stream;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static ua.com.fielden.platform.meta.PropertyMetadataImpl.Builder.calculatedProp;
import static ua.com.fielden.platform.meta.PropertyMetadataImpl.Builder.persistentProp;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getPropertyAnnotationOptionally;
import static ua.com.fielden.platform.utils.StreamUtils.zip;

final class PropertyMetadataUtilsImpl implements PropertyMetadataUtils {

    public static final String ERR_UNKNOWN_COMPONENT_TYPE = "Unknown component type: %s.";
    public static final String ERR_MISSING_PROPERTY = "Missing property [%s] in [%s]. Expected by [%s].";
    public static final String ERR_MISSING_HIBERNATE_TYPE = "Expected Hibernate type to be present for [%s].";
    public static final String ERR_INVALID_PROPERTY_TYPE = "Invalid type [%s] of property [%s.%s].";

    private final IDomainMetadata domainMetadata;
    private final DomainMetadataGenerator generator;

    PropertyMetadataUtilsImpl(final IDomainMetadata domainMetadata, final DomainMetadataGenerator generator) {
        this.domainMetadata = domainMetadata;
        this.generator = generator;
    }

    @Override
    public boolean isPropEntityType(final PropertyTypeMetadata propType, final Predicate<EntityMetadata> predicate) {
        return propType.asEntity()
                .flatMap(et -> domainMetadata.forEntityOpt(et.javaType()))
                .filter(predicate)
                .isPresent();
    }

    @Override
    public List<PropertyMetadata> subProperties(final PropertyMetadata pm) {
        return switch (pm.type()) {
            case PropertyTypeMetadata.Component ct -> subPropertiesForComponent(pm, ct);
            case PropertyTypeMetadata.Entity et -> subPropertiesForEntity(pm, et);
            default -> ImmutableList.of();
        };
    }

    private List<PropertyMetadata> subPropertiesForEntity(final PropertyMetadata pm, final PropertyTypeMetadata.Entity et) {
        return domainMetadata.forEntityOpt(et.javaType())
                .flatMap(EntityMetadata::asUnion)
                .map(em -> subPropertiesForUnionEntity(pm, em))
                .orElseGet(ImmutableList::of);
    }

    private List<PropertyMetadata> subPropertiesForUnionEntity(final PropertyMetadata pm, final EntityMetadata.Union em) {
        return ImmutableList.<PropertyMetadata>builder()
                .addAll(generator.generateUnionImplicitCalcSubprops(em.javaType(), pm.name(), EntityMetadataBuilder.toBuilder(em)))
                .addAll(domainMetadata.entityMetadataUtils().unionMembers(em).stream()
                                .map(member -> combineUnionMember(pm, member))
                                .iterator())
                .build();
    }
    // where
    private PropertyMetadata combineUnionMember(final PropertyMetadata parent, final PropertyMetadata member) {
        // union members must be persistent
        return member.asPersistent().map(persistentMember -> {
            final var columnName = parent.asPersistent().map(p -> p.data().column() + "_").orElse("") + persistentMember.data().column();
            return persistentProp(member.name(), member.type(), member.hibType(),
                                  PropertyNature.Persistent.data(generator.propColumn(columnName)))
                    .build();
        }).orElse(member);
    }

    private List<PropertyMetadata> subPropertiesForComponent(final PropertyMetadata prop, final PropertyTypeMetadata.Component ct) {
        final var componentTypeMetadata = domainMetadata.forComponent(ct.javaType())
                .orElseThrow(() -> new DomainMetadataGenerationException(ERR_UNKNOWN_COMPONENT_TYPE.formatted(ct.javaType().getTypeName())));
        return switch (prop) {
            case PropertyMetadata.Persistent it -> subPropertiesForComponentPersistent(it, componentTypeMetadata);
            case Calculated it -> subPropertiesForComponentCalculated(it, componentTypeMetadata);
            // Hibernate type is required to make sense of a component type's representation
            default -> prop.hibType() != null
                    ? subPropertiesForComponentAny(prop, prop.hibType(), componentTypeMetadata)
                    : ImmutableList.of();
        };
    }

    private List<PropertyMetadata> subPropertiesForComponentAny(
            final PropertyMetadata prop,
            final Object hibType,
            final TypeMetadata.Component componentTypeMetadata)
    {
        final var componentHibType = (ICompositeUserTypeInstantiate) hibType;

        return zip(stream(componentHibType.getPropertyNames()), stream(componentHibType.getPropertyTypes()), (subPropName, subHibType) -> {
            final var subProp = componentTypeMetadata.propertyOpt(subPropName)
                    .orElseThrow(() -> new DomainMetadataGenerationException(ERR_MISSING_PROPERTY.formatted(subPropName, componentTypeMetadata, componentHibType)));
            return PropertyMetadataImpl.Builder.toBuilder(prop)
                    .name(subPropName).type(subProp.type()).hibType(subHibType)
                    .build();
        }).toList();
    }

    private List<PropertyMetadata> subPropertiesForComponentPersistent(
            final PropertyMetadata.Persistent prop,
            final TypeMetadata.Component componentTypeMetadata)
    {
        if (prop.hibType() == null) {
            throw new DomainMetadataGenerationException(ERR_MISSING_HIBERNATE_TYPE.formatted(prop));
        }
        final var componentHibType = (ICompositeUserTypeInstantiate) prop.hibType();

        final String[] subPropNames = componentHibType.getPropertyNames();
        final Class<?> componentJavaType = componentTypeMetadata.javaType();

        return zip(stream(subPropNames), stream(componentHibType.getPropertyTypes()), (subPropName, subHibType) -> {
            final var subProp = componentTypeMetadata.propertyOpt(subPropName)
                    .orElseThrow(() -> new DomainMetadataGenerationException(ERR_MISSING_PROPERTY.formatted(subPropName, componentTypeMetadata, componentHibType)));

            final var mapToColumn = getPropertyAnnotationOptionally(MapTo.class, componentJavaType, subPropName)
                    .map(MapTo::value).orElse("");
            final var headerColName = prop.data().column().name;
            final var columnName = subPropNames.length == 1
                    ? headerColName
                    : (headerColName
                       + (headerColName.endsWith("_") ? "" : "_")
                       + (isEmpty(mapToColumn) ? subPropName.toUpperCase() : mapToColumn));
            final var propColumn = generator.propColumn(
                    columnName, getPropertyAnnotationOptionally(IsProperty.class, componentJavaType, subPropName));
            return persistentProp(subPropName, subProp.type(), subHibType, PropertyNature.Persistent.data(propColumn)).build();
        }).toList();
    }

    private List<PropertyMetadata> subPropertiesForComponentCalculated(
            final Calculated prop,
            final TypeMetadata.Component componentTypeMetadata)
    {
        if (prop.hibType() == null) {
            throw new DomainMetadataGenerationException(ERR_MISSING_HIBERNATE_TYPE.formatted(prop));
        }
        final var componentHibType = (ICompositeUserTypeInstantiate) prop.hibType();

        return zip(stream(componentHibType.getPropertyNames()), stream(componentHibType.getPropertyTypes()), (subPropName, subHibType) -> {
            final var subProp = componentTypeMetadata.propertyOpt(subPropName)
                    .orElseThrow(() -> new DomainMetadataGenerationException(ERR_MISSING_PROPERTY.formatted(subPropName, componentTypeMetadata, componentHibType)));
            return calculatedProp(subPropName, subProp.type(), subHibType, prop.data()).build();
        }).toList();
    }

    private static String invalidPropType(final Object type, final Object owner, final Object prop) {
        return ERR_INVALID_PROPERTY_TYPE.formatted(type, owner, prop);
    }

    private static String failedProp(final Object owner, final Object prop, final Object... rest) {
        final var sj = new StringJoiner("\n", "\n", "").setEmptyValue("");
        stream(rest).map(Object::toString).forEach(sj::add);
        return "Failed to generate metadata for property [%s.%s]" + sj;
    }

}
