package ua.com.fielden.platform.eql.stage1.operands;

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

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static java.lang.String.format;
import static java.util.Collections.emptySet;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.utils.CollectionUtil.append;
import static ua.com.fielden.platform.utils.EntityUtils.isEntityType;

public record Prop1(String propPath, boolean external) implements ISingleOperand1<Prop2>, ToString.IFormattable {

    public static final String ERR_CANNOT_RESOLVE_PROPERTY = "Cannot resolve property [%s].";

    @Override
    public Prop2 transform(final TransformationContextFromStage1To2 context) {
        final Iterator<List<ISource2<? extends ISource3>>> it = context.sourcesForNestedQueries.iterator();
        if (external) {
            it.next();
        }

        while (it.hasNext()) {
            final List<ISource2<? extends ISource3>> item = it.next();
            final PropResolution resolution = resolveProp(item, this);
            if (resolution != null) {
                final boolean shouldBeTreatedAsId = propPath.endsWith("." + ID) && isEntityType(
                        resolution.lastPart().javaType());
                return new Prop2(resolution.source, enhancePath(resolution.getPath()), shouldBeTreatedAsId);
            }
        }

        throw new EqlStage1ProcessingException(ERR_CANNOT_RESOLVE_PROPERTY.formatted(propPath));
    }

    public static List<AbstractQuerySourceItem<?>> enhancePath(final List<AbstractQuerySourceItem<?>> originalPath) {
        final AbstractQuerySourceItem<?> last = originalPath.get(originalPath.size() - 1);
        if (last instanceof QuerySourceItemForComponentType<?> lastComponent) {
            if (lastComponent.getSubitems().size() == 1) {
                return append(originalPath, lastComponent.getSubitems().values().iterator().next());
            }
            else if (lastComponent.javaType() == RichText.class) {
                return append(originalPath, lastComponent.getSubitems().get(RichText.CORE_TEXT));
            }
        }
        return originalPath;
    }

    public static PropResolution resolvePropAgainstSource(final ISource2<? extends ISource3> source, final Prop1 prop) {
        final PropResolutionProgress asIsResolution = source.querySourceInfo()
                .resolve(new PropResolutionProgress(prop.propPath));
        if (source.alias() != null && (prop.propPath.startsWith(source.alias() + ".") || prop.propPath.equals(
                source.alias()))) {
            final String aliaslessPropName = prop.propPath.equals(source.alias())
                    ? ID
                    : prop.propPath.substring(source.alias().length() + 1);
            final PropResolutionProgress aliaslessResolution = source.querySourceInfo()
                    .resolve(new PropResolutionProgress(aliaslessPropName));
            if (aliaslessResolution.isSuccessful()) {
                if (!asIsResolution.isSuccessful()) {
                    return new PropResolution(source, aliaslessResolution.getResolved());
                } else {
                    throw new EqlStage1ProcessingException(
                            String.format("Ambiguity while resolving prop [%s]. Both [%s] and [%s] are resolvable against the given source.",
                                          prop.propPath, prop.propPath, aliaslessPropName));
                }
            }
        }
        return asIsResolution.isSuccessful() ? new PropResolution(source, asIsResolution.getResolved()) : null;
    }

    private static PropResolution resolveProp(final List<ISource2<? extends ISource3>> sources, final Prop1 prop) {
        final List<PropResolution> result = sources.stream()
                .map(source -> resolvePropAgainstSource(source, prop))
                .filter(Objects::nonNull)
                .toList();

        if (result.size() > 1) {
            throw new EqlStage1ProcessingException("Ambiguity while resolving prop [%s]".formatted(prop.propPath));
        }

        return result.size() == 1 ? result.getFirst() : null;
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
