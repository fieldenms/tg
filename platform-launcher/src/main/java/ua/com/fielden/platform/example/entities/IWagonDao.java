/**
 *
 */
package ua.com.fielden.platform.example.entities;

import ua.com.fielden.platform.dao.IEntityDao;

/**
 * @author nc
 *
 */
public interface IWagonDao extends IEntityDao<Wagon> {

    public WagonWithRotables getWagonWithRotables(final Wagon wagon);

    public WagonSlot getWagonSlotByWagonSerialNoAndPosition(String serialNo, Integer position);

    public BogieSlot getBogieSlotByWagonSerialNoAndBogieAndWheelsetSlotPositions(final String serialNo, final Integer bogiePosition, final Integer wheelsetPosition);

}
