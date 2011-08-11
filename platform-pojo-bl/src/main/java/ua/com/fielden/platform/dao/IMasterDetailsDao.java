package ua.com.fielden.platform.dao;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.equery.fetch;
import ua.com.fielden.platform.pagination.IPage;

/**
 * An contract for managing master/details relationship.
 * <p>
 * Let's define what we mean by master/details relationship: <i>The kind of parent/children(child) association where the life cycle of children entities entirely depends on the
 * parent entity is called master/details.</i> Such type of association is also known as <i>aggregation</i>.
 * 
 * There are two type of master/details relationships:
 * <ul>
 * <li>one-to-many -- one master and many children entities (e.g. work order and indirect charges)
 * <li>one-to-one -- one master and one child (e.g. vehicle and vehicle technical details)
 * </ul>
 * 
 * The details entity can be a composite entity. For example, association between work orders and tasks, where both entities are independent, can be represented by a third special
 * entity <b>work order task</b> with a composite key consisting of work order and task.
 * 
 * @author TG Team
 * 
 * @param <M>
 * @param <D>
 */
public interface IMasterDetailsDao<M extends AbstractEntity, D extends AbstractEntity> {
    /**
     * Should return a page containing a page of instances representing details entities associated with a master entity.
     * 
     * @param masterEntity
     *            -- master entity
     * @param model
     *            -- identifies how exactly retrieved entities should be initialised; in most cases this is specific to UI models and thus should exist as a parameter.
     * @param pageCapacity
     * @return
     */
    IPage<D> findDetails(final M masterEntity, fetch<D> model, final int pageCapacity);

    /**
     * Persists an instance of the details entity.
     * 
     * @param masterEntity
     *            -- serves as a context in which detail entity is being saved
     * @param detailEntity
     *            -- entity to be saved
     */
    D saveDetails(final M masterEntity, final D detailEntity);

    /**
     * Deletes entity instance. Currently, in most cases it is not support since deletion is not a simple operation.
     * 
     * @param masterEntity
     *            -- serves as a context in which detail entity is being deleted
     * @param detailEntity
     *            -- entity to be deleted
     */
    void deleteDetails(final M masterEntity, final D detailEntity);
}
