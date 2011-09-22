package ua.com.fielden.platform.entity.validation.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.Mutator;
import ua.com.fielden.platform.entity.annotation.mutator.BeforeChange;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;

/**
 * An annotation for indicating mutators that require domain specific validation logic. In {@link ValidationAnnotation} enumeration it make the last entry, which means that it will
 * be last in the order of validation execution logic.
 * <p>
 * <b>IMPORTANT:</b><i>In case of a collectional property (refer {@link AbstractEntity} for more details) there can be up to three mutators requiring domain validation. However,
 * there can be only one domain validator associated with any particular property. Therefore, specific validator implementation should take into account the fact that values for
 * parameters <code>newValue</code> and <code>oldValue</code> passed into method
 * {@link IBeforeChangeEventHandler#validate(ua.com.fielden.platform.entity.meta.MetaProperty, Object, Object, Object, Mutator)} depend on what mutator it is invoked. </i>
 * <p>
 * The following rules should be taken into account when implementing validators for collectional properties:
 * <ul>
 * <li>If mutator is <code>setter</code> (starts with <i>set</i>) then both parameters a collections.
 * <li>If mutator is <code>incremetor</code> (starts with <i>addTo</i>) then <code>newValue</code> matches mutator's parameter and <code>oldValue</code> is null.
 * <li>If mutator is <code>decremetor</code> (starts with <i>removeFrom</i>) then <code>newValue</code> is null and <code>oldValue</code> matches mutator's parameter.
 * </ul>
 *
 * <b>IMPORTANT:</b><i>This annotation is deprecated. {@link BeforeChange} should be used instead.</i>
 *
 * @author TG Team
 *
 */
@Deprecated
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface DomainValidation {

}
