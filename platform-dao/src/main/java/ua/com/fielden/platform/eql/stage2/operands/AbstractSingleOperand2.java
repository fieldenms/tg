package ua.com.fielden.platform.eql.stage2.operands;

import ua.com.fielden.platform.entity.query.exceptions.EqlException;
import ua.com.fielden.platform.eql.exceptions.EqlStage2ProcessingException;
import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.utils.CollectionUtil;
import ua.com.fielden.platform.utils.ToString;

import java.util.Set;

import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toSet;
import static ua.com.fielden.platform.eql.meta.PropType.*;

public abstract class AbstractSingleOperand2 implements ToString.IFormattable {

    public final PropType type;

    public AbstractSingleOperand2(final PropType type) {
        this.type = type;
    }

    public AbstractSingleOperand2(final Set<PropType> types) {
        this.type = getTypeHighestPrecedence(types);
    }

    public static PropType getTypeHighestPrecedence(final Set<PropType> types) {
        if (types.isEmpty()) {
            throw new EqlStage2ProcessingException("Argument [types] must not be empty.");
        }

        final var nonNullTypes = types.contains(NULL_TYPE)
                ? types.stream().filter(not(PropType::isNull)).collect(toSet())
                : types;

        if (nonNullTypes.isEmpty()) {
            return NULL_TYPE;
        } else if (nonNullTypes.size() == 1) {
            return nonNullTypes.iterator().next();
        } else if (nonNullTypes.contains(UTCDATETIME_PROP_TYPE)) {
            return UTCDATETIME_PROP_TYPE;
        } else if (nonNullTypes.contains(DATETIME_PROP_TYPE)) {
            return DATETIME_PROP_TYPE;
        } else if (nonNullTypes.contains(DATE_PROP_TYPE)) {
            return DATE_PROP_TYPE;
        } else if (nonNullTypes.contains(BIGDECIMAL_PROP_TYPE)) {
            return BIGDECIMAL_PROP_TYPE;
        } else if (nonNullTypes.contains(LONG_PROP_TYPE)) {
            return LONG_PROP_TYPE;
        } else if (nonNullTypes.contains(INTEGER_PROP_TYPE)){
            return INTEGER_PROP_TYPE;
        } else if (nonNullTypes.contains(INT_PROP_TYPE)){
            return INT_PROP_TYPE;
        } else if (nonNullTypes.contains(NSTRING_PROP_TYPE)) {
            return NSTRING_PROP_TYPE;
        } else if (nonNullTypes.contains(STRING_PROP_TYPE)) {
            return STRING_PROP_TYPE;
        } else {
            throw new EqlException("Can't determine type with highest precedence among {%s}".formatted(CollectionUtil.toString(nonNullTypes, ", ")));
        }
    }

    public PropType type() {
        return type;
    }

    @Override
    public String toString() {
        return toString(ToString.separateLines());
    }

    @Override
    public String toString(final ToString.IFormat format) {
        return format.toString(this)
                .add("type", type)
                .pipe(this::addToString)
                .$();
    }

    protected ToString addToString(final ToString toString) {
        return toString;
    }

}
