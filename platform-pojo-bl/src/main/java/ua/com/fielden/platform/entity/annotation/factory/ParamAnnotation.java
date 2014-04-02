package ua.com.fielden.platform.entity.annotation.factory;

import java.lang.annotation.Annotation;

import ua.com.fielden.platform.entity.annotation.mutator.ClassParam;
import ua.com.fielden.platform.entity.annotation.mutator.DateParam;
import ua.com.fielden.platform.entity.annotation.mutator.DateTimeParam;
import ua.com.fielden.platform.entity.annotation.mutator.DblParam;
import ua.com.fielden.platform.entity.annotation.mutator.IntParam;
import ua.com.fielden.platform.entity.annotation.mutator.MoneyParam;
import ua.com.fielden.platform.entity.annotation.mutator.StrParam;

/**
 * A factory for convenient instantiation of annotations {@link ClassParam}, {@link DateParam}, {@link DateTimeParam}, {@link DblParam}, {@link IntParam}, {@link MoneyParam} and
 * {@link StrParam};
 * 
 * @author TG Team
 * 
 */
public class ParamAnnotation {

    public static ClassParam classParam(final String name, final Class<?> value) {
        return new ClassParam() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return ClassParam.class;
            }

            @Override
            public String name() {
                return name;
            }

            @Override
            public Class<?> value() {
                return value;
            }
        };
    }

    public static DateParam dateParam(final String name, final String value) {
        return new DateParam() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return DateParam.class;
            }

            @Override
            public String name() {
                return name;
            }

            @Override
            public String value() {
                return value;
            }
        };
    }

    public static DateTimeParam dateTimeParam(final String name, final String value) {
        return new DateTimeParam() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return DateTimeParam.class;
            }

            @Override
            public String name() {
                return name;
            }

            @Override
            public String value() {
                return value;
            }
        };
    }

    public static DblParam dateParam(final String name, final double value) {
        return new DblParam() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return DblParam.class;
            }

            @Override
            public String name() {
                return name;
            }

            @Override
            public double value() {
                return value;
            }
        };
    }

    public static IntParam dateParam(final String name, final int value) {
        return new IntParam() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return IntParam.class;
            }

            @Override
            public String name() {
                return name;
            }

            @Override
            public int value() {
                return value;
            }
        };
    }

    public static MoneyParam moneyParam(final String name, final String value) {
        return new MoneyParam() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return MoneyParam.class;
            }

            @Override
            public String name() {
                return name;
            }

            @Override
            public String value() {
                return value;
            }
        };
    }

    public static StrParam strParam(final String name, final String value) {
        return new StrParam() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return StrParam.class;
            }

            @Override
            public String name() {
                return name;
            }

            @Override
            public String value() {
                return value;
            }
        };
    }
}
