package ua.com.fielden.platform.eql.stage2.elements.operands;

import static java.util.Collections.unmodifiableList;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import ua.com.fielden.platform.eql.meta.AbstractPropInfo;
import ua.com.fielden.platform.eql.stage2.elements.AbstractElement2;
import ua.com.fielden.platform.eql.stage2.elements.TransformationContext;
import ua.com.fielden.platform.eql.stage2.elements.TransformationResult;
import ua.com.fielden.platform.eql.stage2.elements.sources.IQrySource2;
import ua.com.fielden.platform.eql.stage3.elements.operands.EntProp3;
import ua.com.fielden.platform.eql.stage3.elements.sources.IQrySource3;
import ua.com.fielden.platform.types.tuples.T2;

public class EntProp2 extends AbstractElement2 implements ISingleOperand2<EntProp3> {
    public final IQrySource2<? extends IQrySource3> source;
    private final List<AbstractPropInfo<?>> path;
    public final String name;
    public final Class<?> type;

    public EntProp2(final IQrySource2<? extends IQrySource3> source, final int contextId, final List<AbstractPropInfo<?>> path) {
        super(contextId);
        this.source = source;
        this.path = path;
        this.name = path.stream().map(k -> k.getName()).collect(Collectors.joining("."));
        this.type = path.stream().reduce((first, second) -> second).orElse(null).javaType();
    }

    @Override
    public TransformationResult<EntProp3> transform(final TransformationContext context) {
        final T2<IQrySource3, String> resolution = context.resolve(source, name);
        final EntProp3 transformedProp = new EntProp3(resolution._2, resolution._1);
        return new TransformationResult<EntProp3>(transformedProp, context);
    }

    public List<AbstractPropInfo<?>> getPath() {
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

        return Objects.equals(path, other.path) && Objects.equals(source, other.source);
    }
}