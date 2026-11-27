package ua.com.fielden.platform.companion;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;

/// This interface defines the generator of entity companion objects.
///
public interface ICompanionGenerator {

    /// Returns a companion object for the specified entity type.
    ///
    <E extends AbstractEntity<?>> IEntityDao<E> generateCompanion(Class<E> type);

}
