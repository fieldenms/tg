package ua.com.fielden.platform.companion;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * This interface defines the generator of entity companion object types.
 */
public interface ICompanionGenerator {

    /**
     * Generates a companion object implementation for the specified entity type.
     * <p>
     * The generated type will implement {@link IEntityDao}.
     */
    Class<?> generateCompanion(Class<? extends AbstractEntity> type);

}
