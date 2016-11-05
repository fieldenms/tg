package ua.com.fielden.platform.types.tuples;

import static ua.com.fielden.platform.utils.EntityUtils.equalsEx;

/**
 * A data structure to represent a tuple of 3 values.
 * 
 * @author TG Team
 *
 * @param <T_1>
 * @param <T_2>
 * @param <T_3>
 */
public class T3<T_1, T_2, T_3> {
    
    public final T_1 _1;
    public final T_2 _2;
    public final T_3 _3;
    
    public T3(final T_1 _1, final T_2 _2, final T_3 _3) {
        this._1 = _1;
        this._2 = _2;
        this._3 = _3;
    }
    
    /**
     * A convenient factory method for shorthand instantiation of this class.
     *  
     * @param _1
     * @param _2
     * @param _3
     * @return
     */
    public static <T_1, T_2, T_3> T3<T_1, T_2, T_3> t3(final T_1 _1, final T_2 _2, final T_3 _3) {
        return new T3<>(_1, _2, _3);
    }
    
    @Override
    public int hashCode() {
        int result = 29;
        if (_1 != null) {
            result += _1.hashCode() * 13;
        }
        if (_2 != null) {
            result += _2.hashCode() * 17;
        }
        
        if (_3 != null) {
            result += _3.hashCode() * 23;
        }
        return result;
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        
        if (!(obj instanceof T3)) {
            return false;
        }
        
        @SuppressWarnings("rawtypes")
        final T3 that = (T3) obj;
        
        return equalsEx(this._1, that._1) && equalsEx(this._2, that._2) && equalsEx(this._3, that._3);
    }

}
