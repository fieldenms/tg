package ua.com.fielden.platform.entity.annotation.factory;

import java.lang.annotation.Annotation;

import ua.com.fielden.platform.report.query.generation.GroupProperty;

/**
 * The {@link GroupProperty} annotation factory.
 *
 * @author TG Team
 *
 */
public class GroupAnnotation {

    private final String groupPropertyName;

    /**
     * Initialises this {@link GroupProperty} annotation factory with real distribution property name.
     *
     * @param groupPropertyName
     */
    public GroupAnnotation(final String groupPropertyName){
	this.groupPropertyName = groupPropertyName;
    }

    public GroupProperty newInstance(){
	return new GroupProperty() {

	    @Override
	    public Class<? extends Annotation> annotationType() {
		return GroupProperty.class;
	    }

	    @Override
	    public String groupProperty() {
		return groupPropertyName;
	    }
	};
    }
}
