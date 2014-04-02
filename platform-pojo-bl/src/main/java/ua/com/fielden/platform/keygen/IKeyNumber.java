package ua.com.fielden.platform.keygen;

import ua.com.fielden.platform.dao.IEntityDao;

/**
 * Contract to support manual way of generating unique numbers such as WONO.
 * 
 * @author 01es
 * 
 */
public interface IKeyNumber extends IEntityDao<KeyNumber> {
    Integer nextNumber(final String key);

    Integer currNumber(final String key);
}
