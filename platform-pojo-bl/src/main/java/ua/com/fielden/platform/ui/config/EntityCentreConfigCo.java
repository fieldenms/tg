package ua.com.fielden.platform.ui.config;

import ua.com.fielden.platform.dao.IEntityDao;

/**
 * Companion object for entity {@link EntityCentreConfig}.
 * <p>
 * Please, do not use standard {@link #save(EntityCentreConfig)} / {@link #quickSave(EntityCentreConfig)} methods in client code.
 * Use {@link #saveWithRetry(EntityCentreConfig)} method instead (for graceful conflict resolution).
 * 
 * @author TG Team
 * 
 */
public interface EntityCentreConfigCo extends IEntityDao<EntityCentreConfig> {
    
    /**
     * Saves Entity Centre {@code config} with retry in case of failure.
     * <p>
     * The retry mechanism is invoked only if the method call is not within a scope of another active session.
     * 
     * @param config
     * @return
     */
    Long saveWithRetry(final EntityCentreConfig config);

}