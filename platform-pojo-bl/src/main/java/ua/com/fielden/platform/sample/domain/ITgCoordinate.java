package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.IEntityDao;

import ua.com.fielden.platform.dao.IMasterDetailsDao;

/** 
 * Companion object for entity {@link TgCoordinate}.
 * 
 * @author Developers
 *
 */
public interface ITgCoordinate extends IEntityDao<TgCoordinate>, IMasterDetailsDao<TgPolygon, TgCoordinate> {

}