package ua.com.fielden.platform.eql.stage2.elements.operands;

import java.util.Objects;

import ua.com.fielden.platform.eql.stage2.elements.AbstractElement2;
import ua.com.fielden.platform.eql.stage2.elements.TransformationContext;
import ua.com.fielden.platform.eql.stage2.elements.TransformationResult;
import ua.com.fielden.platform.eql.stage2.elements.sources.IQrySource2;
import ua.com.fielden.platform.eql.stage3.elements.operands.EntProp3;

public class EntProp2 extends AbstractElement2 implements ISingleOperand2<EntProp3> {
    public final String name;
    public final IQrySource2 source;
    public final Class<?> type;

    public EntProp2(final String name, final IQrySource2 source, final Class<?> type, final int contextId) {
        super(contextId);
        this.name = name;
        this.source = source;
        this.type = type;
    }

    @Override
    public TransformationResult<EntProp3> transform(final TransformationContext transformationContext) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean ignore() {
        return false;
    }

    @Override
    public Class<?> type() {
        return type;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((source == null) ? 0 : source.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        
        if (!super.equals(obj)) {
            return false;
        }
        
        if (!(obj instanceof EntProp2)) {
            return false;
        }
        
        final EntProp2 other = (EntProp2) obj;
        
        return Objects.equals(name, other.name) &&
                Objects.equals(type, other.type) &&
                Objects.equals(source, other.source);
    }
}