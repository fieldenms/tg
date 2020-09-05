package ua.com.fielden.platform.eql.stage1.elements.operands;

import static java.lang.String.format;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.utils.EntityUtils.isEntityType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import ua.com.fielden.platform.entity.query.exceptions.EqlStage1ProcessingException;
import ua.com.fielden.platform.eql.meta.AbstractPropInfo;
import ua.com.fielden.platform.eql.meta.ComponentTypePropInfo;
import ua.com.fielden.platform.eql.stage1.elements.PropResolution;
import ua.com.fielden.platform.eql.stage1.elements.PropsResolutionContext;
import ua.com.fielden.platform.eql.stage1.elements.PropResolutionProgress;
import ua.com.fielden.platform.eql.stage2.elements.operands.EntProp2;
import ua.com.fielden.platform.eql.stage2.elements.sources.IQrySource2;
import ua.com.fielden.platform.eql.stage3.elements.sources.IQrySource3;

public class EntProp1 implements ISingleOperand1<EntProp2> {
    public final String name;
    public final boolean external;

    public EntProp1(final String name, final boolean external) {
        this.name = name;
        this.external = external;
    }

    @Override
    public EntProp2 transform(final PropsResolutionContext context) {
        
        final Iterator<List<IQrySource2<? extends IQrySource3>>> it = context.getSources().iterator();
        if (external) {
            it.next();
        }

        for (; it.hasNext();) {
            final List<IQrySource2<? extends IQrySource3>> item = it.next();
            final PropResolution resolution = resolveProp(item, this);
            if (resolution != null) {
                final boolean shouldBeTreatedAsId = name.endsWith("." + ID) && isEntityType(resolution.lastPart().javaType());
                return new EntProp2(resolution.source, enhancePath(resolution.getPath()), shouldBeTreatedAsId);
            }
        }

        throw new EqlStage1ProcessingException(format("Can't resolve property [%s].", name));
    }
    
    public static final List<AbstractPropInfo<?>> enhancePath(final List<AbstractPropInfo<?>> originalPath) {
        final AbstractPropInfo<?> lastResolutionItem = originalPath.get(originalPath.size() - 1);
        if (lastResolutionItem instanceof ComponentTypePropInfo && ((ComponentTypePropInfo) lastResolutionItem).getProps().size() == 1) {
            final List<AbstractPropInfo<?>> enhancedPath = new ArrayList<>(originalPath);
            final AbstractPropInfo<?> autoResolvedItem = (AbstractPropInfo<?>) ((ComponentTypePropInfo) lastResolutionItem).getProps().values().iterator().next();
            enhancedPath.add(autoResolvedItem);
            return enhancedPath;
        }
        return originalPath;
    }
    
    public static PropResolution resolvePropAgainstSource(final IQrySource2<? extends IQrySource3> source, final EntProp1 entProp) {
        final PropResolutionProgress asIsResolution = source.entityInfo().resolve(new PropResolutionProgress(entProp.name));
        if (source.alias() != null && (entProp.name.startsWith(source.alias() + ".") || entProp.name.equals(source.alias()))) {
            final String aliaslessPropName = entProp.name.equals(source.alias()) ? ID : entProp.name.substring(source.alias().length() + 1);
            final PropResolutionProgress aliaslessResolution = source.entityInfo().resolve(new PropResolutionProgress(aliaslessPropName));
            if (aliaslessResolution.isSuccessful()) {
                if (!asIsResolution.isSuccessful()) {
                    return new PropResolution(source, aliaslessResolution.getResolved());
                } else {
                    throw new EqlStage1ProcessingException(format("Ambiguity while resolving prop [%s]. Both [%s] and [%s] are resolvable against the given source.", entProp.name, entProp.name, aliaslessPropName));
                }
            }
        }
        return asIsResolution.isSuccessful() ? new PropResolution(source, asIsResolution.getResolved()) : null;
    }
    
    private PropResolution resolveProp(final List<IQrySource2<? extends IQrySource3>> sources, final EntProp1 entProp) {
        final List<PropResolution> result = new ArrayList<>();
        for (final IQrySource2<? extends IQrySource3> source : sources) {
            final PropResolution resolution = resolvePropAgainstSource(source, entProp);
            if (resolution != null) {
                result.add(resolution);
            }
        }

        if (result.size() > 1) {
            throw new EqlStage1ProcessingException(format("Ambiguity while resolving prop [%s]", entProp.name));
        }

        return result.size() == 1 ? result.get(0) : null;
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

        if (!(obj instanceof EntProp1)) {
            return false;
        }
        
        final EntProp1 other = (EntProp1) obj;
        
        return Objects.equals(name, other.name) && (external == other.external);
    }
}