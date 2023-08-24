package ua.com.fielden.platform.eql.stage2.operands;

import static ua.com.fielden.platform.eql.meta.PropType.BIGDECIMAL_PROP_TYPE;
import static ua.com.fielden.platform.eql.meta.PropType.DATETIME_PROP_TYPE;
import static ua.com.fielden.platform.eql.meta.PropType.DATE_PROP_TYPE;
import static ua.com.fielden.platform.eql.meta.PropType.INTEGER_PROP_TYPE;
import static ua.com.fielden.platform.eql.meta.PropType.INT_PROP_TYPE;
import static ua.com.fielden.platform.eql.meta.PropType.LONG_PROP_TYPE;
import static ua.com.fielden.platform.eql.meta.PropType.STRING_PROP_TYPE;
import static ua.com.fielden.platform.eql.meta.PropType.UTCDATETIME_PROP_TYPE;

import java.util.Set;

import ua.com.fielden.platform.entity.query.exceptions.EqlException;
import ua.com.fielden.platform.eql.meta.PropType;

public abstract class AbstractSingleOperand2 {
    public final PropType type;
    
    public AbstractSingleOperand2(final PropType type) {
        this.type = type;
    }
    
    public AbstractSingleOperand2(final Set<PropType> types) {
        this.type = getTypeHighestPrecedence(types);
    }
    
    public static PropType getTypeHighestPrecedence(final Set<PropType> types) {
        if (types.size() == 1) {
            return types.iterator().next();
        } else if (types.contains(DATE_PROP_TYPE)) {
            return DATE_PROP_TYPE;
        } else if (types.contains(DATETIME_PROP_TYPE)) {
            return DATETIME_PROP_TYPE;
        } else if (types.contains(UTCDATETIME_PROP_TYPE)) {
            return UTCDATETIME_PROP_TYPE;
        } else if (types.contains(BIGDECIMAL_PROP_TYPE)) {
            return BIGDECIMAL_PROP_TYPE;
        } else if (types.contains(LONG_PROP_TYPE)) {
            return LONG_PROP_TYPE;
        } else if (types.contains(INTEGER_PROP_TYPE)){
            return INTEGER_PROP_TYPE;
        } else if (types.contains(INT_PROP_TYPE)){
            return INT_PROP_TYPE;
        } else if (types.contains(STRING_PROP_TYPE)){
            return STRING_PROP_TYPE;
        } else {
            throw new EqlException("Can't determine type with highest precedence for such set :" + types);
        }
    }
    
    public PropType type() {
        return type;
    }
}