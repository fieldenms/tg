package ua.com.fielden.platform.eql.stage1.operands;

import static java.lang.String.format;
import static java.util.Collections.emptySet;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.utils.EntityUtils.isEntityType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.exceptions.EqlStage1ProcessingException;
import ua.com.fielden.platform.eql.meta.query.AbstractQuerySourceItem;
import ua.com.fielden.platform.eql.meta.query.QuerySourceItemForComponentType;
import ua.com.fielden.platform.eql.stage1.PropResolution;
import ua.com.fielden.platform.eql.stage1.PropResolutionProgress;
import ua.com.fielden.platform.eql.stage1.TransformationContext1;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage2.sources.ISource2;
import ua.com.fielden.platform.eql.stage3.sources.ISource3;

public class Prop1 implements ISingleOperand1<Prop2> {
    public final String name;
    public final boolean external;

    public Prop1(final String name, final boolean external) {
        this.name = name;
        this.external = external;
    }

    @Override
    public Prop2 transform(final TransformationContext1 context) {
        
        final Iterator<List<ISource2<? extends ISource3>>> it = context.sourcesForNestedQueries.iterator();
        if (external) {
            it.next();
        }

        while (it.hasNext()) {
            final List<ISource2<? extends ISource3>> item = it.next();
            final PropResolution resolution = resolveProp(item, this);
            if (resolution != null) {
                final boolean shouldBeTreatedAsId = name.endsWith("." + ID) && isEntityType(resolution.lastPart().javaType());
                return new Prop2(resolution.source, enhancePath(resolution.getPath()), shouldBeTreatedAsId);
            }
        }

        throw new EqlStage1ProcessingException(format("Can't resolve property [%s].", name));
    }
    
    public static final List<AbstractQuerySourceItem<?>> enhancePath(final List<AbstractQuerySourceItem<?>> originalPath) {
        final AbstractQuerySourceItem<?> lastResolutionItem = originalPath.get(originalPath.size() - 1);
        if (lastResolutionItem instanceof QuerySourceItemForComponentType && ((QuerySourceItemForComponentType<?>) lastResolutionItem).getSubitems().size() == 1) {
            final List<AbstractQuerySourceItem<?>> enhancedPath = new ArrayList<>(originalPath);
            final AbstractQuerySourceItem<?> autoResolvedItem = ((QuerySourceItemForComponentType<?>) lastResolutionItem).getSubitems().values().iterator().next();
            enhancedPath.add(autoResolvedItem);
            return enhancedPath;
        }
        return originalPath;
    }
    
    public static PropResolution resolvePropAgainstSource(final ISource2<? extends ISource3> source, final Prop1 prop) {
        final PropResolutionProgress asIsResolution = source.querySourceInfo().resolve(new PropResolutionProgress(prop.name));
        if (source.alias() != null && (prop.name.startsWith(source.alias() + ".") || prop.name.equals(source.alias()))) {
            final String aliaslessPropName = prop.name.equals(source.alias()) ? ID : prop.name.substring(source.alias().length() + 1);
            final PropResolutionProgress aliaslessResolution = source.querySourceInfo().resolve(new PropResolutionProgress(aliaslessPropName));
            if (aliaslessResolution.isSuccessful()) {
                if (!asIsResolution.isSuccessful()) {
                    return new PropResolution(source, aliaslessResolution.getResolved());
                } else {
                    throw new EqlStage1ProcessingException(format("Ambiguity while resolving prop [%s]. Both [%s] and [%s] are resolvable against the given source.", prop.name, prop.name, aliaslessPropName));
                }
            }
        }
        return asIsResolution.isSuccessful() ? new PropResolution(source, asIsResolution.getResolved()) : null;
    }
    
    private static PropResolution resolveProp(final List<ISource2<? extends ISource3>> sources, final Prop1 prop) {
        final List<PropResolution> result = new ArrayList<>();
        for (final ISource2<? extends ISource3> source : sources) {
            final PropResolution resolution = resolvePropAgainstSource(source, prop);
            if (resolution != null) {
                result.add(resolution);
            }
        }

        if (result.size() > 1) {
            throw new EqlStage1ProcessingException(format("Ambiguity while resolving prop [%s]", prop.name));
        }

        return result.size() == 1 ? result.get(0) : null;
    }

    @Override
    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        return emptySet();
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + name.hashCode();
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

        return Objects.equals(name, other.name) && (external == other.external);
    }
}