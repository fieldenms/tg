package ua.com.fielden.platform.eql.stage2.elements.operands;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static ua.com.fielden.platform.utils.CollectionUtil.setOf;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import ua.com.fielden.platform.eql.meta.AbstractPropInfo;
import ua.com.fielden.platform.eql.stage2.elements.TransformationContext;
import ua.com.fielden.platform.eql.stage2.elements.TransformationResult;
import ua.com.fielden.platform.eql.stage2.elements.sources.IQrySource2;
import ua.com.fielden.platform.eql.stage3.elements.operands.EntProp3;
import ua.com.fielden.platform.eql.stage3.elements.operands.Expression3;
import ua.com.fielden.platform.eql.stage3.elements.sources.IQrySource3;
import ua.com.fielden.platform.types.tuples.T2;

public class EntProp2 implements ISingleOperand2<Expression3> {
    public final IQrySource2<? extends IQrySource3> source;
    private final List<AbstractPropInfo<?>> path;
    public final boolean isId;
    public final String name;
    public final Class<?> type;
    public final Object hibType;

    public EntProp2(final IQrySource2<? extends IQrySource3> source, final List<AbstractPropInfo<?>> path) {
        this(source, path, false);
    }

    public EntProp2(final IQrySource2<? extends IQrySource3> source, final List<AbstractPropInfo<?>> path, final boolean isId) {
        this.source = source;
        this.path = path;
        this.isId = isId;
        this.name = path.stream().map(k -> k.name).collect(Collectors.joining("."));
        this.type = isId ? Long.class : path.stream().reduce((first, second) -> second).orElse(null).javaType();
        this.hibType = path.stream().reduce((first, second) -> second).orElse(null).hibType;
    }

    @Override
    public TransformationResult<Expression3> transform(final TransformationContext context) {
        final T2<IQrySource3, Object> resolution = context.resolve(source, name);
        Expression3 transformedProp;  
        TransformationContext currentContext = context;
        if (resolution._2 instanceof String) {
            transformedProp = new Expression3(new EntProp3((String) resolution._2, resolution._1, type, hibType), emptyList());
        } else {
            final TransformationResult<Expression3> tr = ((Expression2) resolution._2).transform(context);
            currentContext = tr.updatedContext;
            transformedProp = tr.item;
        }
        return new TransformationResult<Expression3>(transformedProp, currentContext);
    }

    @Override
    public Set<EntProp2> collectProps() {
        return setOf(this);
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
    public Object hibType() {
        return hibType;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + source.contextId().hashCode();
        result = prime * result + path.hashCode();
        result = prime * result + (isId ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof EntProp2)) {
            return false;
        }

        final EntProp2 other = (EntProp2) obj;

        return Objects.equals(path, other.path) && Objects.equals(source.contextId(), other.source.contextId()) && (isId == other.isId);
    }
}