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

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static java.lang.String.format;
import static java.util.Collections.emptySet;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.utils.CollectionUtil.append;
import static ua.com.fielden.platform.utils.EntityUtils.isEntityType;

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
        final Iterator<List<ISource2<? extends ISource3>>> it = context.sourcesForNestedQueries.iterator();
        if (external) {
            it.next();
        }

        while (it.hasNext()) {
            final List<ISource2<? extends ISource3>> item = it.next();
            final PropResolution resolution = resolveProp(item, this);
            if (resolution != null) {
                final boolean shouldBeTreatedAsId = propPath.endsWith("." + ID) && isEntityType(resolution.lastPart().javaType());
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
        final PropResolutionProgress asIsResolution = source.querySourceInfo().resolve(new PropResolutionProgress(prop.propPath));
        if (source.alias() != null && (prop.propPath.startsWith(source.alias() + ".") || prop.propPath.equals(source.alias()))) {
            final String aliaslessPropName = prop.propPath.equals(source.alias()) ? ID : prop.propPath.substring(source.alias().length() + 1);
            final PropResolutionProgress aliaslessResolution = source.querySourceInfo().resolve(new PropResolutionProgress(aliaslessPropName));
            if (aliaslessResolution.isSuccessful()) {
                if (!asIsResolution.isSuccessful()) {
                    return new PropResolution(source, aliaslessResolution.getResolved());
                } else {
                    throw new EqlStage1ProcessingException(format("Ambiguity while resolving prop [%s]. Both [%s] and [%s] are resolvable against the given source.", prop.propPath, prop.propPath, aliaslessPropName));
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
            throw new EqlStage1ProcessingException(format("Ambiguity while resolving prop [%s]", prop.propPath));
        }

        return result.size() == 1 ? result.getFirst() : null;
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
