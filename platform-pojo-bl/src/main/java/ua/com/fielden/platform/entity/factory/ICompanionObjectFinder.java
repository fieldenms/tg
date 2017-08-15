package ua.com.fielden.platform.entity.factory;

import ua.com.fielden.platform.companion.IEntityReader;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A contract for providing implementation for default entity controller.
 * 
 * @author TG Team
 * 
 */
public interface ICompanionObjectFinder {
    
    /**
     * Returns a new instance of a fully fledged companion object as defined for the provided entity type.
     * <p>
     * Should not, buy may return <code>null</code>.
     * 
     * @param type
     * @return
     */
    <T extends IEntityDao<E>, E extends AbstractEntity<?>> T find(final Class<E> type);
    
    /**
     * Returns a new instance of a companion object as defined for the provided entity type, but narrowed down to a reader contract, which is defined by {@link IEntityReader}. 
     * 
     * @param type
     * @return
     */
    <T extends IEntityReader<E>, E extends AbstractEntity<?>> T findAsReader(final Class<E> type, final boolean uninstrumented);
}
