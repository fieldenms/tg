/**
 *
 */
package ua.com.fielden.platform.test.domain.entities.daos;

import ua.com.fielden.platform.dao2.IEntityDao2;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.test.domain.entities.WagonSlot;

@EntityType(WagonSlot.class)
public interface IWagonSlotDao2 extends IEntityDao2<WagonSlot> {
}
