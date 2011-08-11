/**
 *
 */
package ua.com.fielden.platform.test.domain.entities.daos;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.equery.interfaces.IFilter;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.test.domain.entities.RotableClass;

import com.google.inject.Inject;

/**
 *
 * Dao implementation for Rotable Classes (bogies and wheelsets)
 *
 * @author TG Team
 *
 */
@EntityType(RotableClass.class)
public class RotableClassDao extends CommonEntityDao<RotableClass> implements IRotableClassDao{

    @Inject
    protected RotableClassDao(final IFilter filter) {
	super(filter);
    }


}
