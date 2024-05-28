package ua.com.fielden.platform.meta;

import com.google.common.collect.ImmutableList;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.query.ICompositeUserTypeInstantiate;
import ua.com.fielden.platform.eql.exceptions.EqlMetadataGenerationException;
import ua.com.fielden.platform.meta.PropertyMetadataImpl.Calculated;

import java.util.List;
import java.util.StringJoiner;
import java.util.function.Predicate;

import static java.lang.Boolean.FALSE;
import static java.lang.String.format;
import static java.util.Arrays.stream;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static ua.com.fielden.platform.meta.PropertyMetadataImpl.Builder.calculatedProp;
import static ua.com.fielden.platform.meta.PropertyMetadataImpl.Builder.persistentProp;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getPropertyAnnotationOptionally;
import static ua.com.fielden.platform.utils.StreamUtils.zip;

final class PropertyMetadataUtilsImpl implements PropertyMetadataUtils {

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
                .map(predicate::test)
                .orElse(FALSE);
    }

    @Override
    public List<PropertyMetadata> subProperties(final PropertyMetadata pm) {
        return switch (pm.type()) {
            case PropertyTypeMetadata.Composite ct -> subPropertiesForComposite(pm, ct);
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

    private List<PropertyMetadata> subPropertiesForComposite(final PropertyMetadata prop, final PropertyTypeMetadata.Composite ct) {
        final var compositeTypeMetadata = domainMetadata.forComposite(ct.javaType())
                .orElseThrow(() -> new EqlMetadataGenerationException("Unknown composite type: %s".formatted(ct.javaType().getTypeName())));
        return switch (prop) {
            case PropertyMetadata.Persistent it -> subPropertiesForCompositePersistent(it, compositeTypeMetadata);
            case Calculated it -> subPropertiesForCompositeCalculated(it, compositeTypeMetadata);
            default -> ImmutableList.of();
        };
    }

    private List<PropertyMetadata> subPropertiesForCompositePersistent(final PropertyMetadata.Persistent prop,
                                                                       final TypeMetadata.Composite compositeTypeMetadata) {
        if (prop.hibType() == null) {
            throw new EqlMetadataGenerationException("Expected Hibernate type to be present for [%s]".formatted(prop));
        }
        final var compositeHibType = (ICompositeUserTypeInstantiate) prop.hibType();

        final String[] subPropNames = compositeHibType.getPropertyNames();
        final Class<?> compositeJavaType = compositeTypeMetadata.javaType();

        return zip(stream(subPropNames), stream(compositeHibType.getPropertyTypes()), (subPropName, subHibType) -> {
            final var subProp = compositeTypeMetadata.property(subPropName)
                    .orElseThrow(() -> new EqlMetadataGenerationException(
                            format("Missing property [%s] in [%s]. Expected by [%s].", subPropName, compositeTypeMetadata, compositeHibType)));

            final var mapToColumn = getPropertyAnnotationOptionally(MapTo.class, compositeJavaType, subPropName)
                    .map(MapTo::value).orElse("");
            final var headerColName = prop.data().column().name;
            final var columnName = subPropNames.length == 1
                    ? headerColName
                    : (headerColName
                       + (headerColName.endsWith("_") ? "" : "_")
                       + (isEmpty(mapToColumn) ? subPropName.toUpperCase() : mapToColumn));
            final var propColumn = generator.propColumn(
                    columnName, getPropertyAnnotationOptionally(IsProperty.class, compositeJavaType, subPropName));
            return persistentProp(subPropName, subProp.type(), subHibType, PropertyNature.Persistent.data(propColumn)).build();
        }).toList();
    }

    private List<PropertyMetadata> subPropertiesForCompositeCalculated(final Calculated prop,
                                                                       final TypeMetadata.Composite compositeTypeMetadata) {
        if (prop.hibType() == null) {
            throw new EqlMetadataGenerationException("Expected Hibernate type to be present for [%s]".formatted(prop));
        }
        final var compositeHibType = (ICompositeUserTypeInstantiate) prop.hibType();

        return zip(stream(compositeHibType.getPropertyNames()), stream(compositeHibType.getPropertyTypes()), (subPropName, subHibType) -> {
            final var subProp = compositeTypeMetadata.property(subPropName)
                    .orElseThrow(() -> new EqlMetadataGenerationException(
                            format("Missing property [%s] in [%s]. Expected by [%s].", subPropName, compositeTypeMetadata, compositeHibType)));
            return calculatedProp(subPropName, subProp.type(), subHibType, prop.data()).build();
        }).toList();
    }

    private static String invalidPropType(final Object type, final Object owner, final Object prop) {
        return "Invalid type [%s] of property [%s.%s]".formatted(type, owner, prop);
    }

    private static String failedProp(final Object owner, final Object prop, final Object... rest) {
        final var sj = new StringJoiner("\n", "\n", "").setEmptyValue("");
        stream(rest).map(Object::toString).forEach(sj::add);
        return "Failed to generate metadata for property [%s.%s]" + sj;
    }

}
