package ua.com.fielden.platform.eql.stage2.operands;

import static ua.com.fielden.platform.utils.EntityUtils.isEntityType;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;

import org.hibernate.type.BigDecimalType;
import org.hibernate.type.IntegerType;
import org.hibernate.type.LongType;
import org.hibernate.type.StringType;
import org.hibernate.type.YesNoType;

import ua.com.fielden.platform.entity.query.exceptions.EqlException;
import ua.com.fielden.platform.persistence.types.DateTimeType;

public abstract class AbstractSingleOperand2 {
    public final Class<?> type;
    public final Object hibType;
    
    public AbstractSingleOperand2(final Class<?> type, final Object hibType) {
        this.type = type;
        this.hibType = hibType;
    }
    
    public AbstractSingleOperand2(final Set<Class<?>> types) {
        if (types.contains(Date.class)) {
            this.type = Date.class;
            this.hibType = DateTimeType.INSTANCE; 
        } else if (types.contains(BigDecimal.class)) {
            this.type = BigDecimal.class;
            this.hibType = BigDecimalType.INSTANCE; 
        } else if (types.contains(Long.class)) {
            this.type = Long.class;
            this.hibType = LongType.INSTANCE; 
        } else if (types.contains(Integer.class)){
            this.type = Integer.class;
            this.hibType = IntegerType.INSTANCE; 
        } else if (types.contains(int.class)){
            this.type = int.class;
            this.hibType = IntegerType.INSTANCE; 
        } else if (types.contains(String.class)){
            this.type = String.class;
            this.hibType = StringType.INSTANCE; 
        } else if (types.size() == 1) {
            final Class<?> singleType = types.iterator().next(); 
            if (isEntityType(singleType)) {
                this.type = singleType;
                this.hibType = LongType.INSTANCE; 
            } else if (singleType == boolean.class || singleType == Boolean.class){
                this.type = singleType;
                this.hibType = YesNoType.INSTANCE;
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

    public Object hibType() {
        return hibType;
    }
}