package ua.com.fielden.platform.entity.annotation.factory;

import java.lang.annotation.Annotation;

import ua.com.fielden.platform.entity.annotation.mutator.ClassParam;
import ua.com.fielden.platform.entity.annotation.mutator.DateParam;
import ua.com.fielden.platform.entity.annotation.mutator.DateTimeParam;
import ua.com.fielden.platform.entity.annotation.mutator.DblParam;
import ua.com.fielden.platform.entity.annotation.mutator.EnumParam;
import ua.com.fielden.platform.entity.annotation.mutator.Handler;
import ua.com.fielden.platform.entity.annotation.mutator.IntParam;
import ua.com.fielden.platform.entity.annotation.mutator.MoneyParam;
import ua.com.fielden.platform.entity.annotation.mutator.StrParam;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;

/**
 * A factory for convenient instantiation of {@link Handler} annotations, which mainly should be used for dynamic property creation.
 *
 * @author TG Team
 *
 */
public class HandlerAnnotation {

    private final Class<? extends IBeforeChangeEventHandler<?>> value;
    private ClassParam[] non_ordinary = new ClassParam[] {};
    private ClassParam[] clazz = new ClassParam[] {};
    private IntParam[] integer = new IntParam[] {};
    private StrParam[] str = new StrParam[] {};
    private DblParam[] dbl = new DblParam[] {};
    private DateParam[] date = new DateParam[] {};
    private DateTimeParam[] date_time = new DateTimeParam[] {};
    private MoneyParam[] money = new MoneyParam[] {};
    private EnumParam[] enumeration = new EnumParam[] {};

    public HandlerAnnotation(final Class<? extends IBeforeChangeEventHandler<?>> value) {
        this.value = value;
    }

    public HandlerAnnotation non_ordinary(final ClassParam[] values) {
        this.non_ordinary = values;
        return this;
    };

    public HandlerAnnotation clazz(final ClassParam[] values) {
        this.clazz = values;
        return this;
    };

    public HandlerAnnotation integer(final IntParam[] values) {
        this.integer = values;
        return this;
    };

    public HandlerAnnotation str(final StrParam[] values) {
        this.str = values;
        return this;
    };

    public HandlerAnnotation dbl(final DblParam[] values) {
        this.dbl = values;
        return this;
    };

    public HandlerAnnotation date(final DateParam[] values) {
        this.date = values;
        return this;
    };

    public HandlerAnnotation date_time(final DateTimeParam[] values) {
        this.date_time = values;
        return this;
    };

    public HandlerAnnotation money(final MoneyParam[] values) {
        this.money = values;
        return this;
    };

    public HandlerAnnotation enumeration(final EnumParam[] values) {
        this.enumeration = values;
        return this;
    };


    public Handler newInstance() {
        return new Handler() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return Handler.class;
            }

            @Override
            public Class<? extends IBeforeChangeEventHandler<?>> value() {
                return value;
            }

            @Override
            public ClassParam[] non_ordinary() {
                return non_ordinary;
            }

            @Override
            public ClassParam[] clazz() {
                return clazz;
            }

            @Override
            public IntParam[] integer() {
                return integer;
            }

            @Override
            public StrParam[] str() {
                return str;
            }

            @Override
            public DblParam[] dbl() {
                return dbl;
            }

            @Override
            public DateParam[] date() {
                return date;
            }

            @Override
            public DateTimeParam[] date_time() {
                return date_time;
            }

            @Override
            public MoneyParam[] money() {
                return money;
            }

            @Override
            public EnumParam[] enumeration() {
                return enumeration;
            }
        };
    }

    public Handler copyFrom(final Handler handler) {
        return new Handler() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return Handler.class;
            }

            @Override
            public Class<? extends IBeforeChangeEventHandler<?>> value() {
                return handler.value();
            }

            @Override
            public ClassParam[] non_ordinary() {
                return handler.non_ordinary();
            }

            @Override
            public ClassParam[] clazz() {
                return handler.clazz();
            }

            @Override
            public IntParam[] integer() {
                return handler.integer();
            }

            @Override
            public StrParam[] str() {
                return handler.str();
            }

            @Override
            public DblParam[] dbl() {
                return handler.dbl();
            }

            @Override
            public DateParam[] date() {
                return handler.date();
            }

            @Override
            public DateTimeParam[] date_time() {
                return handler.date_time();
            }

            @Override
            public MoneyParam[] money() {
                return handler.money();
            }

            @Override
            public EnumParam[] enumeration() {
                return handler.enumeration();
            }

        };
    }
}
