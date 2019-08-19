package ua.com.fielden.platform.eql.stage2.elements.operands;

import static java.util.Collections.unmodifiableList;

import java.util.List;
import java.util.Objects;

import ua.com.fielden.platform.eql.meta.AbstractPropInfo;
import ua.com.fielden.platform.eql.stage2.elements.AbstractElement2;
import ua.com.fielden.platform.eql.stage2.elements.TransformationContext;
import ua.com.fielden.platform.eql.stage2.elements.TransformationResult;
import ua.com.fielden.platform.eql.stage2.elements.sources.IQrySource2;
import ua.com.fielden.platform.eql.stage3.elements.operands.EntProp3;
import ua.com.fielden.platform.eql.stage3.elements.sources.IQrySource3;

public class EntProp2 extends AbstractElement2 implements ISingleOperand2<EntProp3> {
    public final String name;
    public final IQrySource2<? extends IQrySource3> source;
    public final Class<?> type;
    private final List<AbstractPropInfo<?, ?>> path;

    public EntProp2(final String name, final IQrySource2<? extends IQrySource3> source, final Class<?> type, final int contextId, final List<AbstractPropInfo<?, ?>> path) {
        super(contextId);
        this.name = name;
        this.source = source;
        this.type = type;
        this.path = path;
    }

    @Override
    public TransformationResult<EntProp3> transform(final TransformationContext context) {
        final EntProp3 transformedProp = new EntProp3(name, context.getSource(source));
        return new TransformationResult<EntProp3>(transformedProp, context);
    }

    public List<AbstractPropInfo<?, ?>> getPath() {
        return unmodifiableList(path);
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
        result = prime * result + name.hashCode();
        result = prime * result + type.hashCode();
        result = prime * result + source.hashCode();
        result = prime * result + path.hashCode();
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
                Objects.equals(path, other.path) &&
                Objects.equals(source, other.source);
    }
}