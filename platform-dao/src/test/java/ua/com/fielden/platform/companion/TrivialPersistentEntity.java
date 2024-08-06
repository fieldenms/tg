package ua.com.fielden.platform.companion;

import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.annotation.*;

/**
 * Entity type for tests in {@link PersistentEntitySaverTest}.
 */
@MapEntityTo
@KeyType(String.class)
@CompanionObject(TrivialPersistentEntityDao.class)
public class TrivialPersistentEntity extends AbstractPersistentEntity<String> {}
