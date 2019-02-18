package ua.com.fielden.platform.eql.stage1.elements.operands;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ua.com.fielden.platform.entity.query.exceptions.EqlStage1ProcessingException;
import ua.com.fielden.platform.eql.meta.AbstractPropInfo;
import ua.com.fielden.platform.eql.meta.PropResolution;
import ua.com.fielden.platform.eql.meta.PropsResolutionContext;
import ua.com.fielden.platform.eql.meta.TransformationResult;
import ua.com.fielden.platform.eql.stage1.elements.AbstractElement1;
import ua.com.fielden.platform.eql.stage2.elements.EntProp2;
import ua.com.fielden.platform.eql.stage2.elements.IQrySource2;

public class EntProp1 extends AbstractElement1 implements ISingleOperand1<EntProp2> {
    private String name;
    private boolean external;

    public EntProp1(final String name, final boolean external, final int contextId) {
        super(contextId);
        this.name = name;
        this.external = external;
    }

    public EntProp1(final String name, final int contextId) {
        this(name, false, contextId);
    }

    @Override
    public String toString() {
        return name + " " + hashCode();
    }

    @Override
    public TransformationResult<EntProp2> transform(final PropsResolutionContext resolutionContext) {
        
        final Iterator<List<IQrySource2>> it = resolutionContext.getSources().iterator();
        if (isExternal()) {
            it.next();
        }

        for (; it.hasNext();) {
            final List<IQrySource2> item = it.next();
            final PropResolution resolution = resolveProp(item, this);
            if (resolution != null) {
                final EntProp2 transformedProp = new EntProp2(resolution.getAliaslessName(), resolution.getSource(), resolution.getType());
                
                return new TransformationResult<EntProp2>(transformedProp, resolutionContext.cloneWithAdded(transformedProp));
            }
        }

        throw new EqlStage1ProcessingException(format("Can't resolve property [%s].", getName()));
        
    }
    
    private PropResolution resolvePropAgainstSource(final IQrySource2 source, final EntProp1 entProp) {
        final AbstractPropInfo<?, ?> asIsResolution = source.entityInfo().resolve(entProp.getName());
        if (source.alias() != null && entProp.getName().startsWith(source.alias() + ".")) {
            final String aliasLessPropName = entProp.getName().substring(source.alias().length() + 1);
            final AbstractPropInfo<?, ?> aliasLessResolution = source.entityInfo().resolve(aliasLessPropName);
            if (aliasLessResolution != null) {
                if (asIsResolution == null) {
                    return new PropResolution(aliasLessPropName, source, aliasLessResolution.javaType());
                } else {
                    throw new EqlStage1ProcessingException(format("Ambiguity while resolving prop [%s]. Both [%s] and [%s] are resolvable against the given source.", entProp.getName(), entProp.getName(), aliasLessPropName));
                }
            }
        }
        return asIsResolution != null ? new PropResolution(entProp.getName(), source, asIsResolution.javaType()) : null;
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
            throw new EqlStage1ProcessingException(format("Ambiguity while resolving prop [%s]", entProp.getName()));
        }

        return result.size() == 1 ? result.get(0) : null;
    }


    public String getName() {
        return name;
    }

    public boolean isExternal() {
        return external;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof EntProp1)) {
            return false;
        }
        final EntProp1 other = (EntProp1) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }
}