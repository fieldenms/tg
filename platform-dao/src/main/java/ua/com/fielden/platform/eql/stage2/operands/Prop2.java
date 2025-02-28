package ua.com.fielden.platform.eql.stage2.operands;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.exceptions.EqlStage2ProcessingException;
import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.eql.meta.query.AbstractQuerySourceItem;
import ua.com.fielden.platform.eql.meta.query.QuerySourceItemForEntityType;
import ua.com.fielden.platform.eql.stage2.TransformationContextFromStage2To3;
import ua.com.fielden.platform.eql.stage2.TransformationResultFromStage2To3;
import ua.com.fielden.platform.eql.stage2.sources.ISource2;
import ua.com.fielden.platform.eql.stage3.operands.Expression3;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.eql.stage3.operands.Prop3;
import ua.com.fielden.platform.eql.stage3.sources.ISource3;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.ToString;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.eql.meta.PropType.LONG_PROP_TYPE;
import static ua.com.fielden.platform.eql.meta.PropType.propType;
import static ua.com.fielden.platform.utils.CollectionUtil.setOf;

/**
 * A structure to represent a dot-notated property, resolved to its respective source. This information is used at Stage 3 to build up table joins to retrieve the information expressed by the property.
 * <p>
 * Dot-notated path may contain calculated properties. Such properties are represented as their names, but their expressions are still at the model level (i.e., not resolved to any stage yet).
 * <p>
 * Dot-notated path may also contain "headers" such as union-typed property or component-typed property (e.g., {@link Money}). The parts of the path that represent such properties exist mainly to preserve the structure of dot-notated properties.
 * For example, property {@code vehicle.model.make.avgPrice.amount} will be resolved to 5 parts, where the part corresponding to {@code avgPrice} will be a "header", without any retrievable value.
 * At a later processing stage such "header" parts get combined into "chunks" (represented by {@link ua.com.fielden.platform.eql.stage2.sources.enhance.PropChunk PropChunk}) that have retrievable values.
 * In the current example such chunk would correspond to {@code avgPrice.amount}.
 *
 * @author TG Team
 *
 */
public class Prop2 extends AbstractSingleOperand2 implements ISingleOperand2<ISingleOperand3> {
    public final ISource2<? extends ISource3> source; // An explicit qry source to which a given property gets resolved (e.g., Vehicle.class in case of select(Vehicle) ...).
    private final List<AbstractQuerySourceItem<?>> path; // A sequence of individual properties in a dot-notated property (path), resolved to their source.
    public final String propPath; // An explicit property name used in '.prop(...)' (e.g., "model.make.key" in case of select(Vehicle.class).where().prop("model.make.key")... ).

    public Prop2(final ISource2<? extends ISource3> source, final List<AbstractQuerySourceItem<?>> path) {
        this(source, path, false);
    }

    public Prop2(final ISource2<? extends ISource3> source, final List<AbstractQuerySourceItem<?>> path, final boolean shouldBeTreatedAsId) {
        super(shouldBeTreatedAsId ? LONG_PROP_TYPE : obtainPropType(path));

        if (path.isEmpty()) {
            throw new EqlStage2ProcessingException("Property path must not be empty.");
        }

        this.source = source;
        this.path = path;
        this.propPath = path.stream().map(k -> k.name).collect(Collectors.joining("."));
    }

    private static PropType obtainPropType(final List<AbstractQuerySourceItem<?>> path) {
        final AbstractQuerySourceItem<?> lastElement = path.getLast();
        return propType(lastElement.javaType(), lastElement.hibType);
    }

    @Override
    public TransformationResultFromStage2To3<ISingleOperand3> transform(final TransformationContextFromStage2To3 context) {
        if (lastPart().hasExpression()) {
            final Expression2 expr2 = context.resolveExpression(source.id(), propPath);
            final TransformationResultFromStage2To3<Expression3> exprTr = expr2.transform(context);
            return new TransformationResultFromStage2To3<>(exprTr.item.isSingleOperandExpression() ? exprTr.item.firstOperand : exprTr.item, exprTr.updatedContext);
        } else {
            final T2<String, ISource3> resolution = context.resolve(source.id(), propPath);
            return new TransformationResultFromStage2To3<>(new Prop3(resolution._1, resolution._2, type), context);
        }
    }

    @Override
    public Set<Prop2> collectProps() {
        return setOf(this);
    }

    @Override
    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        return emptySet(); //TODO explore within calc-prop expressions on the prop path (also add prop result type in case it's SE itself)
    }

    public List<AbstractQuerySourceItem<?>> getPath() {
        return unmodifiableList(path);
    }

    public AbstractQuerySourceItem<?> lastPart() {
        return path.getLast();
    }

    /**
     * Returns a part of the {@link #path}, which precedes the last part.
     * This requires for the path to contain at least 2 parts. And so, for shorter paths, an empty result is returned.
     *
     * @return
     */
    public Optional<AbstractQuerySourceItem<?>> penultPart() {
        return path.size() == 1 ? empty() : of(path.get(path.size() - 2));
    }

    @Override
    public boolean ignore() {
        return false;
    }

    @Override
    public boolean isNonnullableEntity() {
        if (ID.equals(propPath)) {
            return true; // TODO this is a temporary fix to be able to treat such primitive prop correctly until distinction between usual 1ong and PK long is introduced.
        }

        for (final AbstractQuerySourceItem<?> querySourceInfoItem : path) {
            if (!isNonnullableEntity(querySourceInfoItem)) {
                return false;
            }
        }

        return true;
    }

    private static boolean isNonnullableEntity(final AbstractQuerySourceItem<?> querySourceInfoItem) {
        return querySourceInfoItem instanceof QuerySourceItemForEntityType ? ((QuerySourceItemForEntityType<?>) querySourceInfoItem).nonnullable : false;
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
        return this == obj ||
               obj instanceof Prop2 that
               && Objects.equals(path, that.path)
               && Objects.equals(source.id(), that.source.id());
    }

    @Override
    public ToString addToString(final ToString toString) {
        return super.addToString(toString)
                .add("source", source)
                .add("propPath", propPath)
                .add("path", path);
    }

}
