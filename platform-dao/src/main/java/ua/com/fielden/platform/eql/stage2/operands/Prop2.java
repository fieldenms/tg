package ua.com.fielden.platform.eql.stage2.operands;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableList;
import static ua.com.fielden.platform.utils.CollectionUtil.setOf;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import ua.com.fielden.platform.eql.exceptions.EqlStage2ProcessingException;
import ua.com.fielden.platform.eql.meta.AbstractPropInfo;
import ua.com.fielden.platform.eql.stage2.PathsToTreeTransformer;
import ua.com.fielden.platform.eql.stage2.TransformationContext2;
import ua.com.fielden.platform.eql.stage2.TransformationResult2;
import ua.com.fielden.platform.eql.stage2.sources.ISource2;
import ua.com.fielden.platform.eql.stage3.operands.Expression3;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.eql.stage3.operands.Prop3;
import ua.com.fielden.platform.eql.stage3.sources.ISource3;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.types.tuples.T3;

/**
 * A structure to represent a dot-notated property, resolved to its respective source. This information is used at Stage 3 to build up table joins to retrieve the information expressed by the property.
 * <p>
 * Dot-notated path may contain calculated properties. Such properties are represented as their names, but their expressions are still at the model level (i.e., not resolved to any stage yet).
 * <p>
 * Dot-notated path may also contain "headers" such as union-typed property or component-typed property (e.g., {@link Money}). The parts of the path that represent such properties exist mainly to preserve the structure of dot-notated properties.
 * For example, property {@code vehicle.model.make.avgPrice.amount} will be resolved to 5 parts, where the part corresponding to {@code avgPrice} will be a "header", without any retrievable value.
 * At a later processing stage such "header" parts get combined into "chunks" (represented by {@code PropChunk} in {@link PathsToTreeTransformer} that have retrievable values.
 * In the correct example such chunk would correspond to {@code avgPrice.amount}.   
 * 
 * @author TG Team
 *
 */
public class Prop2 extends AbstractSingleOperand2 implements ISingleOperand2<ISingleOperand3> {
    public final ISource2<? extends ISource3> source; // An explicit qry source to which a given property gets resolved (e.g., Vehicle.class in case of select(Vehicle) ...). 
    private final List<AbstractPropInfo<?>> path; // A sequence of individual properties in a dot-notated property (path), resolved to their source. 
    public final String name; // An explicit property name used in '.prop(...)' (e.g., "model.make.key" in case of select(Vehicle.class).where().prop("model.make.key")... ). 

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
    public TransformationResult2<ISingleOperand3> transform(final TransformationContext2 context) {
        if (isHeader()) { //resolution to column level is not applicable here
            return new TransformationResult2<>(new Prop3(lastPart().name, null, type, hibType), context);
        }
        
        final T3<String, ISource3, Expression2> resolution = context.resolve(source.id(), name);

        if (resolution._2 != null) {
            return new TransformationResult2<>(new Prop3(resolution._1, resolution._2, type, hibType), context);
        } else if (resolution._3 != null) {
            final TransformationResult2<Expression3> exprTr = resolution._3.transform(context);
            return new TransformationResult2<>(exprTr.item.isSingle() ? exprTr.item.first : exprTr.item, exprTr.updatedContext);
        } else {
        	throw new EqlStage2ProcessingException("Unexpected state while resolving property  [%s].".formatted(resolution._1));
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