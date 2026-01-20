package ua.com.fielden.platform.types.tuples;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.lang.String.format;
import static ua.com.fielden.platform.utils.EntityUtils.equalsEx;

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

    ////////////////////////////////////
    //////// Mapping functions /////////
    ////////////////////////////////////
    public <R> T2<R, T_2> map1(final Function<? super T_1, R> mapper) {
        return new T2<>(mapper.apply(_1), _2);
    }

    public <R> T2<T_1, R> map2(final Function<? super T_2, R> mapper) {
        return new T2<>(_1, mapper.apply(_2));
    }

    public <R> R map(final BiFunction<? super T_1, ? super T_2, R> mapper) {
        return mapper.apply(this._1, this._2);
    }
    ////////////////////////////////////

    /// Runs the action with the first value.
    ///
    public void run1(final Consumer<? super T_1> action) {
        action.accept(_1);
    }

    /// Runs the action with the second value.
    ///
    public void run2(final Consumer<? super T_2> action) {
        action.accept(_2);
    }

    /// Runs the action with both values.
    ///
    public void run(final BiConsumer<? super T_1, ? super T_2> action) {
        action.accept(_1, _2);
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
