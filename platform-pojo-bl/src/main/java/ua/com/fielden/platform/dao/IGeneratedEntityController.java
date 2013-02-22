package ua.com.fielden.platform.dao;

import java.io.IOException;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.pagination.IPage;

/**
 * Defines a contract that declares support for running queries for dynamically generated enitity types (i.e. the ones with calcualted properties added by users from UI).
 *
 * Each generated type should have its own instance of this contract.
 *
 * @author TG Team
 *
 */
public interface IGeneratedEntityController<T extends AbstractEntity<?>> extends IComputationMonitor {

    /**
     * Should return an entity type the DAO instance is managing.
     *
     * @return
     */
    Class<T> getEntityType();

    /**
     * Provides a way to set the generated entity type for which an instance of this controller should be used.
     *
     * @param type
     */
    void setEntityType(final Class<T> type);

    /**
     * Should return entity's key type.
     *
     * @return
     */
    Class<? extends Comparable> getKeyType();

    /**
     * Finds entity by its surrogate id.
     *
     * @param id
     *            -- ID of the entity to be loaded.
     * @param models
     *            -- one or more fetching models specifying the initialisation strategy (i.e. what properties should be retrieved).
     * @return
     */
    T findById(final Long id, fetch<T> fetchModel, final List<byte[]> binaryTypes);

    /**
     * Finds entity by its surrogate id.
     *
     * @param id
     *            -- ID of the entity to be loaded.
     * @param models
     *            -- one or more fetching models specifying the initialisation strategy (i.e. what properties should be retrieved).
     * @return
     */
    T findById(final Long id, final List<byte[]> binaryTypes);

    /**
     * A convenient method for retrieving exactly one entity instance determined by the model. If more than one instance was found an exception is thrown. If there is no entity
     * found then a null value is returned.
     *
     * @param model
     * @return
     */
    T getEntity(final QueryExecutionModel<T, ?> model, final List<byte[]> binaryTypes);

    /**
     * Should return a reference to the first page of the specified size containing entity instances retrieved using the provided query execution model.
     *
     * @param qem
     * @param pageCapacity
     * @param binaryTypes -- a list of binary representation for generated types.
     * @return
     */
    IPage<T> firstPage(//
	    final QueryExecutionModel<T, ?> qem,//
	    final int pageCapacity, //
	    final List<byte[]> binaryTypes);

    /**
     * Should return a reference to the first page of the specified size containing entity instances retrieved using the provided query execution model and the summary
     * information based on <code>summaryModel</code>.
     *
     * @param qem
     * @param summaryModel
     * @param pageCapacity
     * @param binaryTypes -- a list of binary representation for generated types.
     * @return
     */
    IPage<T> firstPage(//
	    final QueryExecutionModel<T, ?> qem,//
	    final QueryExecutionModel<T, ?> summaryModel,//
	    final int pageCapacity, //
	    final List<byte[]> binaryTypes);

    /**
     * Returns a reference to a page with requested number and capacity holding entity instances matching the provided query execution model.
     *
     * @param qem
     * @param pageCapacity
     * @param pageNo
     * @param binaryTypes -- a list of binary representation of generated types.
     * @return
     */
    IPage<T> getPage(//
	    final QueryExecutionModel<T, ?> qem,//
	    final int pageNo, //
	    final int pageCapacity,//
	    final List<byte[]> binaryTypes);

    /**
     * Returns a reference to a page with requested number and capacity holding entity instances matching the provided query execution model.
     *
     * @param model
     * @param pageNo
     * @param pageCount
     * @param pageCapacity
     * @param binaryTypes
     * @return
     */
    IPage<T> getPage(//
	    final QueryExecutionModel<T, ?> model, //
	    final int pageNo, //
	    final int pageCount, //
	    final int pageCapacity,//
	    final List<byte[]> binaryTypes);

    /**
     * Returns all entities produced by the provided query.
     *
     * @param qem
     * @param binaryTypes -- a list of binary representation of generated types.
     * @return
     */
    List<T> getAllEntities(//
	    final QueryExecutionModel<T, ?> qem,//
	    final List<byte[]> binaryTypes);

    /**
     * Returns first entities produced by the provided query.
     *
     * @param qem
     * @param binaryTypes -- a list of binary representation of generated types.
     * @return
     */
    List<T> getFirstEntities(//
	    final QueryExecutionModel<T, ?> qem,//
	    int numberOfEntities, //
	    final List<byte[]> binaryTypes);

    /**
     * Should return a byte array representation the exported data in a format envisaged by the specific implementation.
     * <p>
     * For example it could be a byte array of GZipped Excel data.
     *
     * @param query
     *            -- query result of which should be exported.
     * @param propertyNames
     *            -- names of properties, including dot notated properties, which should be used in the export.
     * @param propertyTitles
     *            -- titles corresponding to the properties being exported, which are used as headers of columns.
     * @param binaryTypes -- a list of binary representation of generated types.
     * @return
     */
    byte[] export(//
	    final QueryExecutionModel<T, ?> query, //
	    final String[] propertyNames, //
	    final String[] propertyTitles, //
	    final List<byte[]> binaryTypes) throws IOException;

}