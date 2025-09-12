package ua.com.fielden.platform.eql.meta;

import com.google.common.collect.Iterators;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.dao.exceptions.DbException;
import ua.com.fielden.platform.dao.session.TransactionalExecution;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.dbschema.PropertyInlinerImpl;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.utils.EntityUtils;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;
import static java.util.stream.Collectors.toSet;

/**
 * Performs instant persistence of the Domain Metadata Model entities, generated for an application domain.
 *
 * @author TG Team
 *
 */
// TODO make this class injectable and replace static with instance methods (a breaking change)
public class PersistDomainMetadataModel {
    final static String CRITERION = "[selection criterion]";
    final static String DOMAINTYPE_INSERT_STMT = "INSERT INTO DOMAINTYPE_(_ID, KEY_, DESC_, DBTABLE_, ENTITYTYPEDESC_, ENTITY_, PROPSCOUNT_, _VERSION) VALUES(?, ?, ?, ?, ?, ?, ?, ?);";
    final static String DOMAINPROPERTY_INSERT_STMT = "INSERT INTO DOMAINPROPERTY_(_ID, NAME_, TITLE_, DESC_, HOLDER__DOMAINTYPE, HOLDER__DOMAINPROPERTY, DOMAINTYPE_, KEYINDEX_, REQUIRED_, DBCOLUMN_, POSITION_, _VERSION) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
    final static String EXISTING_DATA_DELETE_STMT = "DELETE FROM DOMAINPROPERTY_; DELETE FROM DOMAINTYPE_;";

    private static final Logger LOGGER = LogManager.getLogger(PersistDomainMetadataModel.class);

    /**
     * Synchronises persistent model of Domain Explorer entities with an actual application domain.
     *
     * @param entityTypes - domain entities to be included into metadata model entities data.
     */
    public static void persist(
            final List<Class<? extends AbstractEntity<?>>> entityTypes,
            final IDomainMetadata domainMetadata,
            final TransactionalExecution trEx) {
        LOGGER.info("Starting to save the domain metadata...");
        try {
            LOGGER.info("Removing old domain metadata records...");
            clearExistingMetadata(trEx);

            final Set<Class<? extends AbstractEntity<?>>> domainTypesForIntrospection = entityTypes.stream().filter(EntityUtils::isIntrospectionAllowed).collect(toSet());
            final var generator = new DomainMetadataModelGenerator(domainMetadata, new PropertyInlinerImpl(domainMetadata));
            final Map<Class<?>, DomainTypeData> typesMap = generator.generateDomainTypesData(domainTypesForIntrospection);

            LOGGER.info("Inserting metadata about domain entity types...");
            persistDomainTypesData(typesMap.values(), trEx, 1000);

            LOGGER.info("Inserting metadata about domain entity properties...");
            persistDomainPropsData(generator.generateDomainPropsData(typesMap), trEx, 1000);

            LOGGER.info("Completed saving of the domain metadata.");
        } catch (final Exception ex) {
            LOGGER.fatal("Saving domain metadata failed.", ex);
            throw ex;
        }
    }

    private static void clearExistingMetadata(final TransactionalExecution trEx) {
        trEx.exec(conn -> {
            try (final Statement st = conn.createStatement()) {
                st.execute(EXISTING_DATA_DELETE_STMT);
            } catch (final SQLException ex) {
                throw new DbException("Failed to clear existing metadata.", ex);
            }
        });
    }

    private static void persistDomainTypesData(final Collection<DomainTypeData> dtd, final TransactionalExecution trEx, final int batchSize) {
        LOGGER.info(format("Inserting domain types -- %s records in total...", dtd.size()));
        Iterators.partition(dtd.iterator(), batchSize > 0 ? batchSize : 1)
        .forEachRemaining(batch -> {
            trEx.exec(conn -> {
                try (final PreparedStatement pst = conn.prepareStatement(DOMAINTYPE_INSERT_STMT)) {
                    // batch insert statements
                    batch.forEach(propType -> {
                        try {
                            pst.setLong(1, propType.id());
                            pst.setString(2, propType.key());
                            pst.setString(3, propType.desc());
                            pst.setString(4, propType.dbTable());
                            pst.setString(5, propType.entityTypeDesc());
                            setBooleanParameter(6, propType.isEntity(), pst);
                            pst.setInt(7, propType.propsCount());
                            pst.setInt(8, 0);
                            pst.addBatch();
                        } catch (final SQLException ex) {
                            final String error = format("Could not create insert for [%s].", propType.key());
                            throw new DbException(error, ex);
                        }
                    });
                    LOGGER.info(format("Inserting domain types, batch of %s records...", batch.size()));
                    pst.executeBatch();
                } catch (final SQLException ex) {
                    throw new DbException("Could not batch insert domain types.", ex);
                }
            });
        });
        LOGGER.info("Completed inserting domain types.");
    }

    private static void persistDomainPropsData(final List<DomainPropertyData> dpd, final TransactionalExecution trEx, final int batchSize) {
        LOGGER.info(format("Inserting domain properties -- %s records in total...", dpd.size()));
        // batch insert statements
        Iterators.partition(dpd.iterator(), batchSize > 0 ? batchSize : 1)
        .forEachRemaining(batch -> {
            trEx.exec(conn -> {
                try (final PreparedStatement pst = conn.prepareStatement(DOMAINPROPERTY_INSERT_STMT)) {
                    batch.forEach(propType -> {
                        try {
                            pst.setLong(1, propType.id());
                            pst.setString(2, propType.name());
                            pst.setString(3, propType.title());
                            pst.setString(4, propType.desc());
                            setNullableLongParameter(5, propType.holderAsDomainType() == null ? null : propType.holderAsDomainType().id(),
                                                     pst);
                            setNullableLongParameter(6, propType.holderAsDomainProperty() == null ? null : propType.holderAsDomainProperty().id(),
                                                     pst);
                            pst.setLong(7, propType.domainType().id());
                            setNullableIntegerParameter(8, propType.keyIndex(), pst);
                            setBooleanParameter(9, propType.required(), pst);
                            pst.setString(10, propType.dbColumn());
                            pst.setInt(11, propType.position());
                            pst.setInt(12, 0);
                            pst.addBatch();
                        } catch (final SQLException ex) {
                            final String error = format("Could not create insert for [%s].", propType.name());
                            throw new DbException(error, ex);
                        }
                    });
                    LOGGER.info(format("Inserting domain properties, batch of %s records...", batch.size()));
                    pst.executeBatch();
                } catch (final SQLException ex) {
                    throw new DbException("Could not batch insert domain properties.", ex);
                }
            });
        });
        LOGGER.info("Completed inserting domain properties.");
    }

    private static void setNullableIntegerParameter(final int paramIndex, final Integer paramValue, final PreparedStatement pst) throws SQLException {
        if (paramValue != null) {
            pst.setInt(paramIndex, paramValue);
        } else {
            pst.setNull(paramIndex, 4);
        }
    }

    private static void setNullableLongParameter(final int paramIndex, final Long paramValue, final PreparedStatement pst) throws SQLException {
        if (paramValue != null) {
            pst.setLong(paramIndex, paramValue);
        } else {
            pst.setNull(paramIndex, -5);
        }
    }

    private static void setBooleanParameter(final int paramIndex, final boolean paramValue, final PreparedStatement pst) throws SQLException {
        pst.setString(paramIndex, paramValue ? "Y" : "N");
    }

}
