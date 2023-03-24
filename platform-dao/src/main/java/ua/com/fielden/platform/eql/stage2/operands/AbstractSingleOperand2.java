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
        if (types.contains(Date.class)) {
            this.type = Date.class;
        } else if (types.contains(BigDecimal.class)) {
            this.type = BigDecimal.class;
        } else if (types.contains(Long.class)) {
            this.type = Long.class;
        } else if (types.contains(Integer.class)){
            this.type = Integer.class;
        } else if (types.contains(int.class)){
            this.type = int.class;
        } else if (types.contains(String.class)){
            this.type = String.class;
        } else if (types.size() == 1) {
            final Class<?> singleType = types.iterator().next(); 
            if (isEntityType(singleType)) {
                this.type = singleType;
            } else if (singleType == boolean.class || singleType == Boolean.class){
                this.type = singleType;
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