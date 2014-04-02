package ua.com.fielden.platform.dao.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import ua.com.fielden.platform.dao.handlers.IAfterSave;

/**
 * Should be used to annotate DAO implementations of companion objects in order to express the need for processing of the after save event.
 * <p/>
 * The actual event handler is specified using property <code>value</code> and must implement contract {@link IAfterSave}.
 * <p/>
 * This annotation is soft-bounded. If the specified event handler could not be instantiated for some reason then no exception is raised. Instead only a warning message is logged.
 * 
 * @author TG Team
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface AfterSave {
    Class<? extends IAfterSave<?>> value();
}
