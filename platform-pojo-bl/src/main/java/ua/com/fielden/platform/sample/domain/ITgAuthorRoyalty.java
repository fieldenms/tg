package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.sample.domain.TgAuthorship;
import ua.com.fielden.platform.dao.IEntityDao;

import ua.com.fielden.platform.dao.IMasterDetailsDao;

/** 
 * Companion object for entity {@link TgAuthorRoyalty}.
 * 
 * @author Developers
 *
 */
public interface ITgAuthorRoyalty extends IEntityDao<TgAuthorRoyalty>, IMasterDetailsDao<TgAuthorship, TgAuthorRoyalty> {

}