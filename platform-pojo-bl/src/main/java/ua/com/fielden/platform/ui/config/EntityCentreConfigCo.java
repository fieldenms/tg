package ua.com.fielden.platform.ui.config;

import java.util.function.Function;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.query.DbVersion;

/**
 * Companion object for entity {@link EntityCentreConfig}.
 * <p>
 * Please, do not use standard {@link #save(EntityCentreConfig)} / {@link #quickSave(EntityCentreConfig)} methods in client code.
 * Use {@link #saveWithoutConflicts} method instead (for graceful conflict resolution).
 * 
 * @author TG Team
 * 
 */
public interface EntityCentreConfigCo extends IEntityDao<EntityCentreConfig> {
    public static final String SAVE_WITHOUT_CONFLICTS = "saveWithoutConflicts";
    
    /**
     * Saves the entity (quickly) in repeating manner until the process is successfully concluded.<br>
     * Only conflicting errors will trigger saving again.
     * <p>
     * VERY IMPORTANT: this must be used outside of another transaction scopes.
     * 
     * @param entity
     * @return
     */
    Long saveWithoutConflicts(final EntityCentreConfig entity);
    
    /**
     * Runs function {@code fun} with the {@link DbVersion} as the argument.
     * @param fun
     * @return
     */
    <T> T withDbVersion(final Function<DbVersion, T> fun);

}