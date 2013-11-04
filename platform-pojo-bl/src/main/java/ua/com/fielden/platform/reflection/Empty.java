package ua.com.fielden.platform.reflection;

import java.lang.annotation.Annotation;

public class Empty implements Annotation {

    @Override
    public Class<? extends Annotation> annotationType() {
	return Empty.class;
    }

}
