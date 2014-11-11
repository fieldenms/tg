package ua.com.fielden.platform.entity.annotation.factory;

import java.lang.annotation.Annotation;

import ua.com.fielden.platform.entity.annotation.mutator.AfterChange;
import ua.com.fielden.platform.entity.annotation.mutator.ClassParam;
import ua.com.fielden.platform.entity.annotation.mutator.DateParam;
import ua.com.fielden.platform.entity.annotation.mutator.DateTimeParam;
import ua.com.fielden.platform.entity.annotation.mutator.DblParam;
import ua.com.fielden.platform.entity.annotation.mutator.EnumParam;
import ua.com.fielden.platform.entity.annotation.mutator.IntParam;
import ua.com.fielden.platform.entity.annotation.mutator.MoneyParam;
import ua.com.fielden.platform.entity.annotation.mutator.StrParam;
import ua.com.fielden.platform.entity.meta.IAfterChangeEventHandler;

public class AfterChangeAnnotation<T> {

    private final Class<? extends IAfterChangeEventHandler<T>> value;

    public AfterChangeAnnotation(final Class<? extends IAfterChangeEventHandler<T>> value) {
        this.value = value;
    }

    public AfterChange newInstance() {
        return new AfterChange() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return AfterChange.class;
            }

            @Override
            public Class<? extends IAfterChangeEventHandler<?>> value() {
                return value;
            }

            @Override
            public StrParam[] str() {
                return new StrParam[0];
            }

            @Override
            public ClassParam[] non_ordinary() {
                return new ClassParam[0];
            }

            @Override
            public MoneyParam[] money() {
                return new MoneyParam[0];
            }

            @Override
            public IntParam[] integer() {
                return new IntParam[0];
            }

            @Override
            public DblParam[] dbl() {
                return new DblParam[0];
            }

            @Override
            public DateTimeParam[] date_time() {
                return new DateTimeParam[0];
            }

            @Override
            public DateParam[] date() {
                return new DateParam[0];
            }

            @Override
            public ClassParam[] clazz() {
                return new ClassParam[0];
            }

            @Override
            public EnumParam[] enumeration() {
                return new EnumParam[0];
            }
        };
    }

}
