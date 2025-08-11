package ua.com.fielden.platform.eql.stage1.operands;

import jakarta.annotation.Nullable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.exceptions.EqlStage1ProcessingException;
import ua.com.fielden.platform.eql.meta.query.AbstractQuerySourceItem;
import ua.com.fielden.platform.eql.meta.query.QuerySourceItemForComponentType;
import ua.com.fielden.platform.eql.stage1.PropResolution;
import ua.com.fielden.platform.eql.stage1.PropResolutionProgress;
import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage2.sources.ISource2;
import ua.com.fielden.platform.eql.stage3.sources.ISource3;
import ua.com.fielden.platform.types.RichText;
import ua.com.fielden.platform.utils.ToString;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.Collections.emptySet;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.utils.CollectionUtil.append;
import static ua.com.fielden.platform.utils.CollectionUtil.first;
import static ua.com.fielden.platform.utils.EntityUtils.isEntityType;

public record Prop1(String propPath, boolean external) implements ISingleOperand1<Prop2>, ToString.IFormattable {

    public static final String ERR_CANNOT_RESOLVE_PROPERTY = "Cannot resolve property [%s].";
    public static final String ERR_AMBIGUITY_WHILE_RESOLVING_PROPERTY_1 = "Ambiguity while resolving property [%s]. Both [%s] and [%s] are resolvable.";
    public static final String ERR_AMBIGUITY_WHILE_RESOLVING_PROPERTY_2 = "Ambiguity while resolving property [%s].";

    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public Prop2 transform(final TransformationContextFromStage1To2 context) {
        final var prop2 = transformBase(context);
        return AppendIdToUnionTypedProp1.INSTANCE.apply(this, prop2, context).orElse(prop2);
    }

    /// An alternative to [#transform(TransformationContextFromStage1To2)] that does not apply [AppendIdToUnionTypedProp1].
    ///
    public Prop2 transformBase(final TransformationContextFromStage1To2 context) {
        return context.sourcesForNestedQueries.stream()
                .skip(external ? 1 : 0)
                .map(item -> resolveProp(item, this))
                .flatMap(Optional::stream)
                .map(resolution -> {
                    final var shouldBeTreatedAsId = propPath.endsWith("." + ID) && isEntityType(resolution.lastPart().javaType());
                    return new Prop2(resolution.source, enhancePath(resolution.getPath()), shouldBeTreatedAsId);
                })
                .findFirst()
                .orElseThrow(() -> new EqlStage1ProcessingException(ERR_CANNOT_RESOLVE_PROPERTY.formatted(propPath)));
    }

    /**
     * If the given path ends with a component type that has a single property, appends that property onto the path.
     * Otherwise, returns the given path.
     *
     * @return a new enhanced path or the same path
     */
    public static List<AbstractQuerySourceItem<?>> enhancePath(final List<AbstractQuerySourceItem<?>> originalPath) {
        if (originalPath.getLast() instanceof QuerySourceItemForComponentType<?> lastComponent) {
            if (lastComponent.getSubitems().size() == 1) {
                return append(originalPath, first(lastComponent.getSubitems().values()).orElseThrow());
            }
            else if (lastComponent.javaType() == RichText.class) {
                return append(originalPath, lastComponent.getSubitems().get(RichText.SEARCH_TEXT));
            }
        }
        return originalPath;
    }

    public static @Nullable PropResolution resolvePropAgainstSource(final ISource2<? extends ISource3> source, final Prop1 prop) {
        final PropResolutionProgress asIsResolution = source.querySourceInfo().resolve(new PropResolutionProgress(prop.propPath));
        if (source.alias() != null && (prop.propPath.startsWith(source.alias() + ".") || prop.propPath.equals(source.alias()))) {
            final String aliaslessPropName = prop.propPath.equals(source.alias()) ? ID : prop.propPath.substring(source.alias().length() + 1);
            final PropResolutionProgress aliaslessResolution = source.querySourceInfo().resolve(new PropResolutionProgress(aliaslessPropName));
            if (aliaslessResolution.isSuccessful()) {
                if (!asIsResolution.isSuccessful()) {
                    return new PropResolution(source, aliaslessResolution.getResolved());
                } else {
                    final var exception = new EqlStage1ProcessingException(
                            ERR_AMBIGUITY_WHILE_RESOLVING_PROPERTY_1.formatted(prop.propPath, prop.propPath, aliaslessPropName));
                    LOGGER.error(() -> "%s\nSource: %s".formatted(exception.getMessage(), source));
                    throw exception;
                }
            }
        }
        return asIsResolution.isSuccessful() ? new PropResolution(source, asIsResolution.getResolved()) : null;
    }

    private static Optional<PropResolution> resolveProp(final List<ISource2<? extends ISource3>> sources, final Prop1 prop) {
        final List<PropResolution> result = sources.stream()
                .map(source -> resolvePropAgainstSource(source, prop))
                .filter(Objects::nonNull)
                .limit(2)
                .collect(toImmutableList());

        if (result.size() > 1) {
            throw new EqlStage1ProcessingException(ERR_AMBIGUITY_WHILE_RESOLVING_PROPERTY_2.formatted(prop.propPath));
        }

        return first(result);
    }

    @Override
    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        return emptySet();
    }

    @Override
    public String toString() {
        return toString(ToString.standard);
    }

    @Override
    public String toString(final ToString.IFormat format) {
        return format.toString(this)
                .add("path", propPath)
                .add("external", external)
                .$();
    }

}
