package ua.com.fielden.platform.eql.stage2.operands;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableList;
import static ua.com.fielden.platform.utils.CollectionUtil.setOf;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import ua.com.fielden.platform.eql.meta.AbstractPropInfo;
import ua.com.fielden.platform.eql.stage2.TransformationContext;
import ua.com.fielden.platform.eql.stage2.TransformationResult;
import ua.com.fielden.platform.eql.stage2.sources.ISource2;
import ua.com.fielden.platform.eql.stage3.operands.Expression3;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.eql.stage3.operands.Prop3;
import ua.com.fielden.platform.eql.stage3.sources.ISource3;
import ua.com.fielden.platform.types.tuples.T2;

public class Prop2 extends AbstractSingleOperand2 implements ISingleOperand2<ISingleOperand3> {
    public final ISource2<? extends ISource3> source;
    private final List<AbstractPropInfo<?>> path;
    public final String name;

    public Prop2(final ISource2<? extends ISource3> source, final List<AbstractPropInfo<?>> path) {
        this(source, path, false);
    }

    public Prop2(final ISource2<? extends ISource3> source, final List<AbstractPropInfo<?>> path, final boolean shouldBeTreatedAsId) {
        super(shouldBeTreatedAsId ? Long.class : path.stream().reduce((first, second) -> second).orElse(null).javaType(), 
                path.stream().reduce((first, second) -> second).orElse(null).hibType);
        this.source = source;
        this.path = path;
        this.name = path.stream().map(k -> k.name).collect(Collectors.joining("."));
    }

    @Override
    public TransformationResult<ISingleOperand3> transform(final TransformationContext context) {
        if (isHeader()) { //resolution to column level is not applicable here
            return new TransformationResult<>(new Prop3(lastPart().name, null, type, hibType), context);
        }
        
        final T2<ISource3, Object> resolution = context.resolve(source.id(), name);

        if (resolution._2 instanceof String) {
            return new TransformationResult<>(new Prop3((String) resolution._2, resolution._1, type, hibType), context);
        } else {
            final TransformationResult<Expression3> exprTr = ((Expression2) resolution._2).transform(context);
            return new TransformationResult<>(exprTr.item.isSingle() ? exprTr.item.first : exprTr.item, exprTr.updatedContext);
        }
    }

    @Override
    public Set<Prop2> collectProps() {
        // header props may happen here as they carry useful type info for yielding purposes, but since they are not going to be resolved to any columns -- will not be included during props collection 
        return isHeader() ? emptySet() : setOf(this);
    }
    
    public List<AbstractPropInfo<?>> getPath() {
        return unmodifiableList(path);
    }
    
    public boolean isCalculated() {
        return lastPart().hasExpression();
    }
    
    public AbstractPropInfo<?> lastPart() {
        return path.get(path.size() - 1);
    }
    
    @Override
    public boolean ignore() {
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + source.id().hashCode();
        result = prime * result + path.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Prop2)) {
            return false;
        }

        final Prop2 other = (Prop2) obj;

        return Objects.equals(path, other.path) && Objects.equals(source.id(), other.source.id());
    }
}