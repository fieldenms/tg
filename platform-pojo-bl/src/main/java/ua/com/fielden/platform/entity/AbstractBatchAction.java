package ua.com.fielden.platform.entity;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

/**
 * The entity that collects data for batch save.
 *
 * @author TG Team
 *
 * @param <T>
 */
public abstract class AbstractBatchAction<T extends AbstractEntity<?>> extends AbstractEntity<T> {

    private static final long serialVersionUID = 909995682564099704L;

    @IsProperty(AbstractEntity.class)
    @Title(value = "Save entities", desc = "Entities to save")
    private Set<T> saveEntities = new HashSet<T>();

    @IsProperty(AbstractEntity.class)
    @Title(value = "Remove entities", desc = "Entities to remove")
    private Set<T> removeEntities = new HashSet<T>();

    @IsProperty(AbstractEntity.class)
    @Title(value = "Update entities", desc = "Entities to update")
    private Set<T> updateEntities = new HashSet<T>();

    /**
     * Set the dirty persistent entities to update.
     *
     * @param updateEntities
     * @return
     */
    @Observable
    public AbstractBatchAction<T> setUpdateEntities(final Set<T> updateEntities) {
	this.updateEntities.clear();
	this.updateEntities.addAll(updateEntities);
	return this;
    }

    /**
     * Returns the set of dirty persistent entities. These entities must be saved.
     *
     * @return
     */
    public Set<T> getUpdateEntities() {
	return Collections.unmodifiableSet(updateEntities);
    }

    /**
     * Set the entities those must be removed.
     *
     * @param removeEntities
     * @return
     */
    @Observable
    public AbstractBatchAction<T> setRemoveEntities(final Set<T> removeEntities) {
	this.removeEntities.clear();
	this.removeEntities.addAll(removeEntities);
	return this;
    }

    /**
     * Adds new entity that must be removed.
     *
     * @param entity
     * @return
     */
    @Observable
    public AbstractBatchAction<T> addToRemoveEntities(final T entity) {
	this.removeEntities.add(entity);
	return this;
    }

    /**
     * Removes the entity from the set of remove entities.
     *
     * @param entity
     * @return
     */
    @Observable
    public AbstractBatchAction<T> removeFromRemoveEntities(final T entity) {
	this.removeEntities.remove(entity);
	return this;
    }

    /**
     * Returns the set of entities those must be removed.
     *
     * @return
     */
    public Set<T> getRemoveEntities() {
	return Collections.unmodifiableSet(removeEntities);
    }

    /**
     * Set entities that must be created and saved.
     *
     * @param saveEntities
     * @return
     */
    @Observable
    public AbstractBatchAction<T> setSaveEntities(final Set<T> saveEntities) {
	this.saveEntities.clear();
	this.saveEntities.addAll(saveEntities);
	return this;
    }

    /**
     * Adds new entity that must be saved.
     *
     * @param entity
     * @return
     */
    @Observable
    public AbstractBatchAction<T> addToSaveEntities(final T entity) {
	this.saveEntities.add(entity);
	return this;
    }

    /**
     * Removes the specified entity from remove set.
     *
     * @param entity
     * @return
     */
    @Observable
    public AbstractBatchAction<T> removeFromSaveEntities(final T entity) {
	this.saveEntities.remove(entity);
	return this;
    }

    /**
     * Returns the save entities list.
     *
     * @return
     */
    public Set<T> getSaveEntities() {
	return Collections.unmodifiableSet(saveEntities);
    }
}
