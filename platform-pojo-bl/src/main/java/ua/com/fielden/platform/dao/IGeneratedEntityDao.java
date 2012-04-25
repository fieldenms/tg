package ua.com.fielden.platform.dao;

import java.io.IOException;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.pagination.IPage;

/**
 * Defines a contract that declares support for running queries for dynamically generated enitity types (i.e. the ones with calcualted properties added by users from UI).
 *
 * Each generated type should have its own instance of this contract.
 *
 * @author TG Team
 *
 */
public interface IGeneratedEntityDao {

    /**
     * Should return an entity type the DAO instance is managing.
     *
     * @return
     */
    Class<? extends AbstractEntity> getEntityType();

    /**
     * Should return entity's key type.
     *
     * @return
     */
    Class<? extends Comparable> getKeyType();

    /**
     * Should return a reference to the first page of the specified size containing entity instances retrieved using the provided query execution model.
     *
     * @param qem
     * @param pageCapacity
     * @param binaryTypes -- a list of binary representation for generated types.
     * @return
     */
    IPage<?> firstPage(//
	    final QueryExecutionModel<?, ?> qem,//
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
    IPage<?> firstPage(//
	    final QueryExecutionModel<?, ?> qem,//
	    final QueryExecutionModel<?, ?> summaryModel,//
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
    IPage<?> getPage(//
	    final QueryExecutionModel<?, ?> qem,//
	    final int pageNo, //
	    final int pageCapacity,//
	    final List<byte[]> binaryTypes);

    /**
     * Returns all entities produced by the provided query.
     *
     * @param qem
     * @param binaryTypes -- a list of binary representation of generated types.
     * @return
     */
    List<? extends AbstractEntity> getAllEntities(//
	    final QueryExecutionModel<?, ?> qem,//
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
	    final QueryExecutionModel<?, ?> query, //
	    final String[] propertyNames, //
	    final String[] propertyTitles, //
	    final List<byte[]> binaryTypes) throws IOException;
}