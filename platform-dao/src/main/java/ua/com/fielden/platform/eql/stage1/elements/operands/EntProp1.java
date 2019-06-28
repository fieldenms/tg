package ua.com.fielden.platform.eql.stage1.elements.operands;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import ua.com.fielden.platform.entity.query.exceptions.EqlStage1ProcessingException;
import ua.com.fielden.platform.eql.meta.AbstractPropInfo;
import ua.com.fielden.platform.eql.stage1.elements.AbstractElement1;
import ua.com.fielden.platform.eql.stage1.elements.PropResolution;
import ua.com.fielden.platform.eql.stage1.elements.PropsResolutionContext;
import ua.com.fielden.platform.eql.stage1.elements.TransformationResult;
import ua.com.fielden.platform.eql.stage2.elements.operands.EntProp2;
import ua.com.fielden.platform.eql.stage2.elements.sources.IQrySource2;

public class EntProp1 extends AbstractElement1 implements ISingleOperand1<EntProp2> {
    public final String name;
    public final boolean external;

    public EntProp1(final String name, final boolean external, final int contextId) {
        super(contextId);  // contextId is not taken into consideration in hashCode() and equals(..) methods on purpose -- Stage1 elements have no need to reference uniquely one another.
        this.name = name;
        this.external = external;
    }

    public EntProp1(final String name, final int contextId) {
        this(name, false, contextId);
    }

    @Override
    public TransformationResult<EntProp2> transform(final PropsResolutionContext context) {
        
        final Iterator<List<IQrySource2>> it = context.getSources().iterator();
        if (external) {
            it.next();
        }

        for (; it.hasNext();) {
            final List<IQrySource2> item = it.next();
            final PropResolution resolution = resolveProp(item, this);
            if (resolution != null) {
                final EntProp2 transformedProp = new EntProp2(resolution.getAliaslessName(), resolution.getSource(), resolution.getType(), contextId);
                
                return new TransformationResult<EntProp2>(transformedProp, context.cloneWithAdded(transformedProp));
            }
        }

        throw new EqlStage1ProcessingException(format("Can't resolve property [%s].", name));
        
    }
    
    private PropResolution resolvePropAgainstSource(final IQrySource2 source, final EntProp1 entProp) {
        final AbstractPropInfo<?, ?> asIsResolution = source.entityInfo().resolve(entProp.name);
        if (source.alias() != null && entProp.name.startsWith(source.alias() + ".")) {
            final String aliasLessPropName = entProp.name.substring(source.alias().length() + 1);
            final AbstractPropInfo<?, ?> aliasLessResolution = source.entityInfo().resolve(aliasLessPropName);
            if (aliasLessResolution != null) {
                if (asIsResolution == null) {
                    return new PropResolution(aliasLessPropName, source, aliasLessResolution.javaType());
                } else {
                    throw new EqlStage1ProcessingException(format("Ambiguity while resolving prop [%s]. Both [%s] and [%s] are resolvable against the given source.", entProp.name, entProp.name, aliasLessPropName));
                }
            }
        }
        return asIsResolution != null ? new PropResolution(entProp.name, source, asIsResolution.javaType()) : null;
    }

    private PropResolution resolveProp(final List<IQrySource2> sources, final EntProp1 entProp) {
        final List<PropResolution> result = new ArrayList<>();
        for (final IQrySource2 pair : sources) {
            final PropResolution resolution = resolvePropAgainstSource(pair, entProp);
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
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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