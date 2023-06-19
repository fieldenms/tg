package ua.com.fielden.platform.eql.stage2.operands;

import static ua.com.fielden.platform.utils.EntityUtils.isEntityType;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;

import ua.com.fielden.platform.entity.query.exceptions.EqlException;

public abstract class AbstractSingleOperand2 {
    public final Class<?> type;
    
    public AbstractSingleOperand2(final Class<?> type) {
        this.type = type;
    }
    
    public AbstractSingleOperand2(final Set<Class<?>> types) {
        this.type = getTypeHighestPrecedence(types);
    }
    
    public static Class<?> getTypeHighestPrecedence(final Set<Class<?>> types) {
        if (types.contains(Date.class)) {
            return Date.class;
        } else if (types.contains(BigDecimal.class)) {
            return BigDecimal.class;
        } else if (types.contains(Long.class)) {
            return Long.class;
        } else if (types.contains(Integer.class)){
            return Integer.class;
        } else if (types.contains(int.class)){
            return int.class;
        } else if (types.contains(String.class)){
            return String.class;
        } else if (types.size() == 1) {
            final Class<?> singleType = types.iterator().next(); 
            if (isEntityType(singleType)) {
                return singleType;
            } else if (singleType == boolean.class || singleType == Boolean.class){
                return singleType;
            } else {
                throw new EqlException("Can't determine type with highest precedence for such set :" + types);    
            }
        } else {
            throw new EqlException("Can't determine type with highest precedence for such set :" + types);
        }
    }

    public Class<?> type() {
        return type;
    }
}