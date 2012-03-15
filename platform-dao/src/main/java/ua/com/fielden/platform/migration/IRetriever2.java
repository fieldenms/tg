package ua.com.fielden.platform.migration;

import java.sql.Connection;

import org.hibernate.SessionFactory;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.migration.dao.MigrationErrorDao2;
import ua.com.fielden.platform.migration.dao.MigrationHistoryDao2;

/**
 * Specifies a contract for implementing SQL statement retrieving an entity items of certain type.
 *
 * @author TG Team
 *
 */
public interface IRetriever2<T extends AbstractEntity<?>> {
    /**
     * Should provide an SQL for selection of data from a legacy database. The convention:
     * <ul>
     *   <li> Each column in select should correspond to an entity property, which is defined as a column alias.
     *   <li> Column alias should be named as property_name_; e.g. for property vehicleTechDetails alias must be vehicle_tech_details_.
     * </ul>
     * @return
     */
    String selectSql();

    /**
     * Determines whether data (re-)population is required.
     * In case of data (re-)population retrieves data from a legacy database, creates entity instances based on this data and persists them.
     * @param subset TODO
     * @param session
     *
     * @throws Exception
     */
    Result populateData(final SessionFactory sessionFactory, final Connection conn, final EntityFactory factory, final MigrationErrorDao2 errorDao, MigrationHistoryDao2 histDao, MigrationRun migrationRun, String subset) throws Exception;

    /**
     * Returns entity type information.
     *
     * @return
     */
    Class<T> type();


    /**
     * Returns name of the entity property, by which retrieved entities can be grouped in certain number of batches. This property is usually part of the entity key and cannot be null.
     * @return
     */
    String splitProperty();
}