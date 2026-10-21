package ua.com.fielden.platform.web_api;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLInputObjectType;
import jakarta.annotation.Nullable;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils;
import ua.com.fielden.platform.entity.query.model.ConditionModel;
import ua.com.fielden.platform.web_api.exceptions.WebApiException;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.cond;
import static ua.com.fielden.platform.utils.StreamUtils.reduceLeft;
import static ua.com.fielden.platform.web_api.FluentConditions.*;

/// This visitor compiles GraphQL entity conditions into EQL conditions.
///
/// The resulting [ConditionModel] is wrapped in an optional so that empty conditions can be easily distinguished.
///
class EntityCondToEqlCompiler extends AbstractEntityCondVisitor<Optional<ConditionModel>> {

    public static final String
            ERR_IS_NULL_CANNOT_BE_USED_IN_A_ROOT_ENTITY_CONDITION = "`isNull` cannot be used in a root entity condition.",
            ERR_IN_DOES_NOT_PERMIT_EMPTY_LISTS = "Condition `in` does not permit empty lists.",
            ERR_NOT_IN_DOES_NOT_PERMIT_EMPTY_LISTS = "Condition `notIn` does not permit empty lists.";

    /// Compiles an EQL condition.
    ///
    /// @param env  environment associated with a root entity field that has argument [FluentConditions#WHERE]
    ///             whose value is the condition object to be compiled
    ///
    public static ConditionModel compile(final DataFetchingEnvironment env) {
        return ofNullable(env.<Map<String, Object>>getArgument(WHERE))
                .flatMap(object -> new EntityCondToEqlCompiler((GraphQLInputObjectType) env.getFieldDefinition().getArgument(WHERE).getType())
                                    .rootEntityCond(object))
                .orElseGet(EntityQueryUtils::emptyCondition);
    }

    protected EntityCondToEqlCompiler(final GraphQLInputObjectType rootCondType) {
        super(rootCondType);
    }

    @Override
    public Optional<ConditionModel> combine(final Stream<Optional<ConditionModel>> rs) {
        return combineWithAnd(rs.map(opt -> opt.orElse(null)).filter(Objects::nonNull));
    }

    private static Optional<ConditionModel> combineWithAnd(final Stream<ConditionModel> rs) {
        return reduceLeft(rs, (acc, c) -> cond().condition(acc).and().condition(c).model());
    }

    @Override
    public Optional<ConditionModel> or(final List<Map<String, Object>> list, final CharSequence path) {
        return reduceLeft(list.stream().flatMap(obj -> entityCond(obj, path).stream()),
                          (acc, c) -> cond().condition(acc).or().condition(c).model());
    }

    @Override
    public Optional<ConditionModel> and(final List<Map<String, Object>> list, final CharSequence path) {
        return reduceLeft(list.stream().flatMap(obj -> entityCond(obj, path).stream()),
                          (acc, c) -> cond().condition(acc).and().condition(c).model());
    }

    @Override
    public Optional<ConditionModel> not(final Map<String, Object> object, final CharSequence path) {
        return entityCond(object, path).map(c -> cond().negatedCondition(c).model());
    }

    @Override
    public Optional<ConditionModel> isNull(final boolean value, final CharSequence path) {
        final var propPath = toPropertyPath(rootCondType(), path);
        if (propPath.isEmpty()) {
            throw new WebApiException(ERR_IS_NULL_CANNOT_BE_USED_IN_A_ROOT_ENTITY_CONDITION);
        }
        final var part = cond().prop(propPath);
        return Optional.of(value ? part.isNull().model() : part.isNotNull().model());
    }

    @Override
    public Optional<ConditionModel> stringCond(final Map<String, Object> object, final CharSequence path) {
        final var propertyPath = toPropertyPath(rootCondType(), path);
        return combineWithAnd(Stream.of(genericCondEq(object, propertyPath),
                                        genericCondNe(object, propertyPath),
                                        genericCondIn(object, propertyPath),
                                        genericCondNotIn(object, propertyPath),
                                        genericCondIsNull(object, propertyPath),
                                        stringCondLike(object, propertyPath),
                                        stringCondNotLike(object, propertyPath),
                                        stringCondILike(object, propertyPath),
                                        stringCondNotILike(object, propertyPath))
                                      .filter(Objects::nonNull));
    }

    private @Nullable ConditionModel stringCondLike(final Map<String, Object> object, final String propertyPath) {
        final var like = (String) object.get(LIKE);
        return like == null ? null : cond().prop(propertyPath).like().val(toEqlPattern(like)).model();
    }

    private @Nullable ConditionModel stringCondNotLike(final Map<String, Object> object, final String propertyPath) {
        final var notLike = (String) object.get(NOT_LIKE);
        return notLike == null ? null : cond().prop(propertyPath).notLike().val(toEqlPattern(notLike)).model();
    }

    private @Nullable ConditionModel stringCondILike(final Map<String, Object> object, final String propertyPath) {
        final var iLike = (String) object.get(I_LIKE);
        return iLike == null ? null : cond().prop(propertyPath).iLike().val(toEqlPattern(iLike)).model();
    }

    private @Nullable ConditionModel stringCondNotILike(final Map<String, Object> object, final String propertyPath) {
        final var notILike = (String) object.get(NOT_I_LIKE);
        return notILike == null ? null : cond().prop(propertyPath).notILike().val(toEqlPattern(notILike)).model();
    }

    @Override
    public Optional<ConditionModel> booleanCond(final Map<String, Object> object, final CharSequence path) {
        final var propertyPath = toPropertyPath(rootCondType(), path);
        return ofNullable(genericCondEq(object, propertyPath));
    }

    @Override
    public Optional<ConditionModel> intCond(final Map<String, Object> object, final CharSequence path) {
        return genericNumericCond(object, path);
    }

    @Override
    public Optional<ConditionModel> longCond(final Map<String, Object> object, final CharSequence path) {
        return genericNumericCond(object, path);
    }

    @Override
    public Optional<ConditionModel> bigDecimalCond(final Map<String, Object> object, final CharSequence path) {
        return genericNumericCond(object, path);
    }

    @Override
    public Optional<ConditionModel> moneyCond(final Map<String, Object> object, final CharSequence path) {
        return genericNumericCond(object, path);
    }

    @Override
    public Optional<ConditionModel> dateCond(final Map<String, Object> object, final CharSequence path) {
        final var propertyPath = toPropertyPath(rootCondType(), path);
        return combineWithAnd(Stream.of(genericCondEq(object, propertyPath),
                                        genericCondNe(object, propertyPath),
                                        genericCondLt(object, propertyPath),
                                        genericCondLe(object, propertyPath),
                                        genericCondGt(object, propertyPath),
                                        genericCondGe(object, propertyPath),
                                        genericCondIsNull(object, propertyPath))
                                      .filter(Objects::nonNull));
    }

    @Override
    public Optional<ConditionModel> hyperlinkCond(final Map<String, Object> object, final CharSequence path) {
        final var propertyPath = toPropertyPath(rootCondType(), path);
        return ofNullable(genericCondIsNull(object, propertyPath));
    }

    @Override
    public Optional<ConditionModel> colourCond(final Map<String, Object> object, final CharSequence path) {
        final var propertyPath = toPropertyPath(rootCondType(), path);
        return ofNullable(genericCondIsNull(object, propertyPath));
    }

    private Optional<ConditionModel> genericNumericCond(final Map<String, Object> object, final CharSequence path) {
        final var propertyPath = toPropertyPath(rootCondType(), path);
        return combineWithAnd(Stream.of(genericCondEq(object, propertyPath),
                                        genericCondNe(object, propertyPath),
                                        genericCondLt(object, propertyPath),
                                        genericCondLe(object, propertyPath),
                                        genericCondGt(object, propertyPath),
                                        genericCondGe(object, propertyPath),
                                        genericCondIn(object, propertyPath),
                                        genericCondNotIn(object, propertyPath),
                                        genericCondIsNull(object, propertyPath))
                                      .filter(Objects::nonNull));
    }

    private static @Nullable ConditionModel genericCondEq(final Map<String, Object> object, final String propertyPath) {
        final var eq = object.get(EQ);
        return eq == null ? null : cond().prop(propertyPath).eq().val(eq).model();
    }

    private static @Nullable ConditionModel genericCondNe(final Map<String, Object> object, final String propertyPath) {
        final var ne = object.get(NE);
        return ne == null ? null : cond().prop(propertyPath).ne().val(ne).model();
    }

    private static @Nullable ConditionModel genericCondLt(final Map<String, Object> object, final String propertyPath) {
        final var lt = object.get(LT);
        return lt == null ? null : cond().prop(propertyPath).lt().val(lt).model();
    }

    private static @Nullable ConditionModel genericCondLe(
            final Map<String, Object> object,
            final String propertyPath)
    {
        final var le = object.get(LE);
        return le == null ? null : cond().prop(propertyPath).le().val(le).model();
    }

    private static @Nullable ConditionModel genericCondGt(final Map<String, Object> object, final String propertyPath) {
        final var gt = object.get(GT);
        return gt == null ? null : cond().prop(propertyPath).gt().val(gt).model();
    }

    private static @Nullable ConditionModel genericCondGe(
            final Map<String, Object> object,
            final String propertyPath)
    {
        final var ge = object.get(GE);
        return ge == null ? null : cond().prop(propertyPath).ge().val(ge).model();
    }

    private static @Nullable ConditionModel genericCondIn(final Map<String, Object> object, final String propertyPath) {
        final var in = (List<Object>) object.get(IN);
        if (in == null) {
            return null;
        }
        if (in.isEmpty()) {
            throw new WebApiException(ERR_IN_DOES_NOT_PERMIT_EMPTY_LISTS);
        }
        return cond().prop(propertyPath).in().values(in).model();
    }

    private static @Nullable ConditionModel genericCondNotIn(
            final Map<String, Object> object,
            final String propertyPath)
    {
        final var notIn = (List<Object>) object.get(NOT_IN);
        if (notIn == null) {
            return null;
        }
        if (notIn.isEmpty()) {
            throw new WebApiException(ERR_NOT_IN_DOES_NOT_PERMIT_EMPTY_LISTS);
        }
        return cond().prop(propertyPath).notIn().values(notIn).model();
    }

    private static @Nullable ConditionModel genericCondIsNull(
            final Map<String, Object> object,
            final String propertyPath)
    {
        final var isNull = (Boolean) object.get(IS_NULL);
        if (isNull == null) {
            return null;
        }
        else if (isNull) {
            return cond().prop(propertyPath).isNull().model();
        }
        else {
            return cond().prop(propertyPath).isNotNull().model();
        }
    }

    private static String toEqlPattern(final String pattern) {
        return pattern.replace('*', '%');
    }

}
