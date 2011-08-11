package ua.com.fielden.platform.example.swing.ei;

import org.hibernate.SessionFactory;

import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.basic.autocompleter.HibernateValueMatcher;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.matcher.IValueMatcherFactory;

/**
 * Demonstrates a custom implementation of {@link IValueMatcherFactory}, which includes a case of two properties of the same type. but different rules for value matching.
 *
 * @author 01es
 *
 */
public class ExampleValueMatcherFactory implements IValueMatcherFactory {
    final HibernateValueMatcher<InspectedEntity> matcher1;
    final HibernateValueMatcher<InspectedEntity> matcher2;

    public ExampleValueMatcherFactory(final SessionFactory sessionFactory) {
	matcher1 = new HibernateValueMatcher<InspectedEntity>(InspectedEntity.class, "key", sessionFactory);
	matcher2 = new HibernateValueMatcher<InspectedEntity>(InspectedEntity.class, "key", sessionFactory);
    }

    @Override
    public IValueMatcher<?> getValueMatcher(final Class<? extends AbstractEntity> propertyOwnerEntityType, final String propertyName, final Object... additionalParameters) {
	if (InspectedEntity.class.isAssignableFrom(propertyOwnerEntityType)) { // handles inheritance
	    if ("entityPropertyOne".equals(propertyName)) {
		return matcher1;
	    }
	    if ("entityPropertyTwo".equals(propertyName)) {
		return matcher2;
	    }
	}
	throw new RuntimeException("There is no value matcher for type " + propertyOwnerEntityType + " and property " + propertyName + ".");
    }
}
