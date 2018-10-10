package ua.com.fielden.platform.entity.annotation.factory;

import java.lang.annotation.Annotation;

import ua.com.fielden.platform.entity.annotation.Observable;

public class ObservableAnnotation {

    public static Observable newInstance() {
        return new Observable() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return Observable.class;
            }

        };
    }
}
