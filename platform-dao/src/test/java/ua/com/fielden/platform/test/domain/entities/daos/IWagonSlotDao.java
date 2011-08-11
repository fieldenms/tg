/**
 *
 */
package ua.com.fielden.platform.test.domain.entities.daos;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.test.domain.entities.WagonSlot;

@EntityType(WagonSlot.class)
public interface IWagonSlotDao extends IEntityDao<WagonSlot> {
}
