package ua.com.fielden.platform.entity.factory;

import ua.com.fielden.platform.companion.IEntityReader;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.exceptions.EntityCompanionException;
import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A contract for providing implementation for default entity controller.
 * 
 * @author TG Team
 * 
 */
public interface ICompanionObjectFinder {
    
    /**
     * Returns a new instance of a companion object as defined for the provided entity type.
     * <p>
     * Should not, buy may return <code>null</code>.
     * <p>
     * If uninstrumentation is request, but a corresponding companion object does not support it then a runtime exception {@link EntityCompanionException} is thrown.
     * 
     * @param type An entity type whose companion object is requested.
     * @param uninstrumented If <code>true</code> then there will be an attempt to produce a companion object that read entities as uninstrumeted instances.
     * @return
     */
    <T extends IEntityDao<E>, E extends AbstractEntity<?>> T find(final Class<E> type, final boolean uninstrumented);
    
    /**
     * Returns a new instance of a fully fledged companion object as defined for the provided entity type.
     * <p>
     * Should not, buy may return <code>null</code>.
     * 
     * @param type An entity type whose companion object is requested.
     * @return
     */
    <T extends IEntityDao<E>, E extends AbstractEntity<?>> T find(final Class<E> type);
    
    /**
     * Returns a new instance of a companion object as defined for the provided entity type, but narrowed down to a reader contract, which is defined by {@link IEntityReader}. 
     * 
     * @param type An entity type whose companion object is requested.
     * @param uninstrumented Indicates whether the returned reader should not instrument (<code>true</code>) or instrument (<code>false</code>) read entity instances.
     * @return
     */
    <T extends IEntityReader<E>, E extends AbstractEntity<?>> T findAsReader(final Class<E> type, final boolean uninstrumented);
}
