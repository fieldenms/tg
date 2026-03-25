package ua.com.fielden.platform.types.tuples;

import java.util.Map;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static ua.com.fielden.platform.utils.EntityUtils.equalsEx;

/// A data structure to represent a tuple of 2 values.
///
/// @param <T_1>  the type for the first member
/// @param <T_2>  the type for the second member
///
public class T2<T_1, T_2> {
    
    public final T_1 _1;
    public final T_2 _2;
    
    public T2(final T_1 _1, final T_2 _2) {
        this._1 = _1;
        this._2 = _2;
    }
    
    /// A convenient factory method for shorthand instantiation of this class.
    ///
    /// @param _1
    /// @param _2
    /// @return
    public static <T_1, T_2> T2<T_1, T_2> t2(final T_1 _1, final T_2 _2) {
        return new T2<>(_1, _2);
    }

    public T_1 _1() {
        return _1;
    }

    public T_2 _2() {
        return _2;
    }

    //::::::::::::::::::::::::::::::::::
    //:::::::: Mapping functions :::::::
    //::::::::::::::::::::::::::::::::::
    public <R> T2<R, T_2> map1(final Function<? super T_1, R> mapper) {
        return new T2<>(mapper.apply(_1), _2);
    }

    public <R> T2<T_1, R> map2(final Function<? super T_2, R> mapper) {
        return new T2<>(_1, mapper.apply(_2));
    }

    public <R> R map(final BiFunction<? super T_1, ? super T_2, R> mapper) {
        return mapper.apply(this._1, this._2);
    }


    //::::::::::::::::::::::::::::::::::
    //::::::: Running functions ::::::::
    //::::::::::::::::::::::::::::::::::

    /// Applies the specified action to the first value.
    ///
    public void run1(final Consumer<? super T_1> action) {
        action.accept(_1);
    }

    /// Applies the specified action to the second value.
    ///
    public void run2(final Consumer<? super T_2> action) {
        action.accept(_2);
    }

    /// Applies the specified action to the contents of this tuple.
    ///
    public void run(final BiConsumer<? super T_1, ? super T_2> action) {
        action.accept(_1, _2);
    }

    //::::::::::::::::::::::::::::::::::
    //:::::::::: Utilities :::::::::::::
    //::::::::::::::::::::::::::::::::::

    public static <A, B> Collector<T2<A, B>, ?, Map<A, B>> toMap() {
        return Collectors.toMap(T2::_1, T2::_2);
    }

    public static <A, B> Collector<T2<A, B>, ?, Map<A, B>> toMap(BinaryOperator<B> mergeFunction) {
        return Collectors.toMap(T2::_1, T2::_2, mergeFunction);
    }

    public static <A, B, M extends Map<A, B>> Collector<T2<A, B>, ?, M> toMap(BinaryOperator<B> mergeFunction, Supplier<M> mapFactory) {
        return Collectors.toMap(T2::_1, T2::_2, mergeFunction, mapFactory);
    }

    //::::::::::::::::::::::::::::::::::

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
