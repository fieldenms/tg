package ua.com.fielden.platform.eql.stage1.operands;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.lang.String.format;
import static java.util.Collections.emptySet;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.utils.CollectionUtil.first;
import static ua.com.fielden.platform.utils.EntityUtils.isEntityType;

import java.util.*;

import com.google.common.collect.ImmutableList;
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
import ua.com.fielden.platform.utils.CollectionUtil;

import javax.annotation.Nullable;

public class Prop1 implements ISingleOperand1<Prop2> {

    public static final String ERR_CANNOT_RESOLVE_PROPERTY = "Cannot resolve property [%s].";

    public final String propPath;
    public final boolean external;

    public Prop1(final String propPath, final boolean external) {
        this.propPath = propPath;
        this.external = external;
    }

    @Override
    public Prop2 transform(final TransformationContextFromStage1To2 context) {
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
        if (originalPath.getLast() instanceof QuerySourceItemForComponentType<?> lastResolutionItemAsComponent && lastResolutionItemAsComponent.getSubitems().size() == 1) {
            final var autoResolvedItem = first(lastResolutionItemAsComponent.getSubitems().values()).orElseThrow();
            return ImmutableList. <AbstractQuerySourceItem<?>> builder()
                    .addAll(originalPath)
                    .add(autoResolvedItem)
                    .build();
        }
        else {
            return originalPath;
        }
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
                    throw new EqlStage1ProcessingException(format(
                            "Ambiguity while resolving property [%s] against [%s]. Both [%s] and [%s] are resolvable.",
                            prop.propPath, source, prop.propPath, aliaslessPropName));
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
            throw new EqlStage1ProcessingException(format("Ambiguity while resolving property [%s]", prop.propPath));
        }

        return first(result);
    }

    @Override
    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        return emptySet();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + propPath.hashCode();
        result = prime * result + (external ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Prop1)) {
            return false;
        }

        final Prop1 other = (Prop1) obj;

        return Objects.equals(propPath, other.propPath) && (external == other.external);
    }
}
