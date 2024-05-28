package ua.com.fielden.platform.meta;

import com.google.common.collect.ImmutableList;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.query.ICompositeUserTypeInstantiate;
import ua.com.fielden.platform.eql.exceptions.EqlMetadataGenerationException;

import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Predicate;

import static java.lang.Boolean.FALSE;
import static java.lang.String.format;
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

    private List<PropertyMetadata> subPropertiesForComposite(final PropertyMetadata pm, final PropertyTypeMetadata.Composite ct) {
        final var compositeTypeMetadata = domainMetadata.forComposite(ct.javaType())
                .orElseThrow(() -> new EqlMetadataGenerationException("Unknown composite type: %s".formatted(ct.javaType().getTypeName())));
        final var compositeHibType = (ICompositeUserTypeInstantiate) pm.hibType();

        final String[] propNames = compositeHibType.getPropertyNames();
        final Class<?> compositeJavaType = ct.javaType();

        return zip(Arrays.stream(propNames), Arrays.stream(compositeHibType.getPropertyTypes()), (propName, hibType) -> {
            final var subPm = compositeTypeMetadata.property(propName)
                    .orElseThrow(() -> new EqlMetadataGenerationException(
                            format("Missing property [%s] in [%s]. Expected by [%s].", propName, compositeTypeMetadata, compositeHibType)));

            // determine the nature of the sub-property
            final var builder = switch (pm) {
                case PropertyMetadata.Persistent it -> {
                    final var mapToColumn = getPropertyAnnotationOptionally(MapTo.class, compositeJavaType, propName)
                            .map(MapTo::value).orElse("");
                    final var headerColName = it.data().column().name;
                    final var columnName = propNames.length == 1
                            ? headerColName
                            : (headerColName
                               + (headerColName.endsWith("_") ? "" : "_")
                               + (isEmpty(mapToColumn) ? propName.toUpperCase() : mapToColumn));
                    final var propColumn = generator.propColumn(
                            columnName, getPropertyAnnotationOptionally(IsProperty.class, compositeJavaType, propName));
                    yield persistentProp(propName, subPm.type(), hibType, PropertyNature.Persistent.data(propColumn));
                }
                case PropertyMetadataImpl.Calculated it -> calculatedProp(propName, subPm.type(), hibType, it.data());
                default -> throw new EqlMetadataGenerationException(
                        format("Unexpected nature [%s] for property [%s] of composite type [%s]",
                               pm.nature(), pm.name(), ct));
            };

            return builder.build();
        }).toList();
    }

    private static String invalidPropType(final Object type, final Object owner, final Object prop) {
        return "Invalid type [%s] of property [%s.%s]".formatted(type, owner, prop);
    }

    private static String failedProp(final Object owner, final Object prop, final Object... rest) {
        final var sj = new StringJoiner("\n", "\n", "").setEmptyValue("");
        Arrays.stream(rest).map(Object::toString).forEach(sj::add);
        return "Failed to generate metadata for property [%s.%s]" + sj;
    }

}
