package ua.com.fielden.platform.swing.review.development;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeManager.ILocatorDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.matcher.IValueMatcherFactory;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.utils.Pair;

import com.google.inject.Inject;

public class EnhancedLocatorEntityQueryCriteria<T extends AbstractEntity<?>, DAO extends IEntityDao<T>> extends EntityQueryCriteria<ILocatorDomainTreeManagerAndEnhancer, T, DAO> {

    private static final long serialVersionUID = -9199540944743417928L;

    @Inject
    public EnhancedLocatorEntityQueryCriteria(final IValueMatcherFactory valueMatcherFactory) {
	super(valueMatcherFactory);
    }

    @SuppressWarnings("unused")
    public final List<T> runLocatorQuery(final int resultSize, final Object kerOrDescValue, final fetch<?> fetch, final Pair<String, Object>... otherPropValues){
	return new ArrayList<T>();
    }
}