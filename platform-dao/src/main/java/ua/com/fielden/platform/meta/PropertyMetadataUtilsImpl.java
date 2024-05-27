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
                .map(et -> domainMetadata.forEntity(et.javaType()))
                .map(predicate::test)
                .orElse(FALSE);
    }

    @Override
    public List<PropertyMetadata> subProperties(final PropertyMetadata pm) {
        return switch (pm.type()) {
            case PropertyTypeMetadata.Composite ct -> subPropertiesForComposite(pm, ct);
            // TODO
            case PropertyTypeMetadata.Entity et -> throw new UnsupportedOperationException();
            default -> ImmutableList.of();
        };
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

}
