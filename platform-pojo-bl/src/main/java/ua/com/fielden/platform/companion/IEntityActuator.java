package ua.com.fielden.platform.companion;

import ua.com.fielden.platform.entity.AbstractEntity;

/// A contract to actuate (save/execute) entities.
///
public interface IEntityActuator<T extends AbstractEntity<?>> {

    /// This method is an actuator, which triggers the entity *execution*.
    /// The nature of the *execution* depends on whether entity is persistent or represent an action:
    ///
    ///   -  For a persistent entity, executing an entity means persisting it (inserting or updating).
    ///   -  For an action entity, executing an entity means running the action it represents.
    ///      It may or may not have a side effect, such as updating some persistent entities.
    ///
    /// **Important:** The passed in and the returned entity instances are **NOT** reference equivalent.
    /// The returned entity should be thought of as a newer version of the passed in instance and used everywhere in the downstream logic of the callee,
    /// while the passed in entity should be discarded.
    ///
    /// @param entity an instance to be saved (if a persistent entity) or executed (if an action entity).
    /// @return a saved instanced, which should always be used instead the passed one as it may be outdated
    ///         (e.g. saving a persistent entities, returns a re-fetched entity after successful saving).
    ///
    T save(final T entity);
    
}
