package ua.com.fielden.platform.entity.annotation.factory;

import java.lang.annotation.Annotation;

import ua.com.fielden.platform.entity.annotation.Required;

public class RequiredAnnotation {

    public Required newInstance(){
	return new Required() {

	    @Override
	    public Class<? extends Annotation> annotationType() {
		return Required.class;
	    }

	    @Override
	    public String value() {
		return "";
	    }
	};
    }
}
