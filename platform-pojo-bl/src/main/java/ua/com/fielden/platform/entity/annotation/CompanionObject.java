package ua.com.fielden.platform.entity.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;

/// Annotates an entity type to specify its companion object.
///
/// @see CompanionIsGenerated
///
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface CompanionObject {

    Class<? extends IEntityDao<? extends AbstractEntity<?>>> value();

}
