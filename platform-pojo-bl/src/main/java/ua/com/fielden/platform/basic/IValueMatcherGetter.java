/**
 *
 */
package ua.com.fielden.platform.basic;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * Interface which should return different value matchers, depending on some parameters
 * 
 * @author Yura, Oleh
 * 
 */
public interface IValueMatcherGetter<T extends AbstractEntity<?>> {

    IValueMatcher<T> getValueMatcher(Object... params);

}
