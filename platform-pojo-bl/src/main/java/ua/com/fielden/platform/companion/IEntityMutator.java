package ua.com.fielden.platform.companion;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A contract that combines {@link IEntityDeleter}, {@link IEntitySaver} and {@link IWithQuickSave} as a convenience in cases where both sets of mutating methods (save and delete) are required.
 * 
 * @author TG Team
 *
 * @param <T>
 */
public interface IEntityMutator<T extends AbstractEntity<?>> extends IEntityDeleter<T>, IEntitySaver<T>, IWithQuickSave<T> {

}
