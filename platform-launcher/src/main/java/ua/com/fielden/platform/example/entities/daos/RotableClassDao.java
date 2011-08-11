/**
 *
 */
package ua.com.fielden.platform.example.entities.daos;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.equery.interfaces.IFilter;
import ua.com.fielden.platform.example.entities.IRotableClassDao;
import ua.com.fielden.platform.example.entities.RotableClass;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

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
