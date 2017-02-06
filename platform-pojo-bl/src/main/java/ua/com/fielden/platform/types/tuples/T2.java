package ua.com.fielden.platform.types.tuples;

import static java.lang.String.format;
import static ua.com.fielden.platform.utils.EntityUtils.equalsEx;
import ua.com.fielden.platform.utils.EntityUtils;

/**
 * A data structure to represent a tuple of 2 values.
 * 
 * @author TG Team
 *
 * @param <T_1>
 * @param <T_2>
 */
public class T2<T_1, T_2> {
    
    public final T_1 _1;
    public final T_2 _2;
    
    public T2(final T_1 _1, final T_2 _2) {
        this._1 = _1;
        this._2 = _2;
    }
    
    /**
     * A convenient factory method for shorthand instantiation of this class.
     *  
     * @param _1
     * @param _2
     * @return
     */
    public static <T_1, T_2> T2<T_1, T_2> t2(final T_1 _1, final T_2 _2) {
        return new T2<>(_1, _2);
    }

    @Override
    public int hashCode() {
        int result = 29;
        if (_1 != null) {
            result += _1.hashCode() * 13;
        }
        if (_2 != null) {
            result += _2.hashCode() * 31;
        }
        return result;
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        
        if (!(obj instanceof T2)) {
            return false;
        }

        @SuppressWarnings("rawtypes")
        final T2 that = (T2) obj;
        
        return equalsEx(this._1, that._1) && equalsEx(this._2, that._2);
    }

    @Override
    public String toString() {
        return format("(%s, %s)", _1, _2);
    }
}
