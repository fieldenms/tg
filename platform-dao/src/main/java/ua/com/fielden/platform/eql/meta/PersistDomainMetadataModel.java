package ua.com.fielden.platform.eql.meta;

import com.google.common.collect.Iterators;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.dao.exceptions.DbException;
import ua.com.fielden.platform.dao.session.TransactionalExecution;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;
import ua.com.fielden.platform.meta.*;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

import javax.annotation.Nullable;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.function.Function;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toSet;
import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeRepresentation.isExcluded;
import static ua.com.fielden.platform.entity.AbstractEntity.VERSION;
import static ua.com.fielden.platform.meta.PropertyMetadataKeys.REQUIRED;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getTitleAndDesc;
import static ua.com.fielden.platform.utils.EntityUtils.isUnionEntityType;

/**
 * Performs instant (re-)persistence of the Domain Metadata Model entities, generated for an application domain.
 *
 * @author TG Team
 *
 */
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
            final EqlDomainMetadata domainMetadata,
            final TransactionalExecution trEx) {
        throw new UnsupportedOperationException("Migration to typeful metadata is in progress.");
    }

    public static void persist(
            final List<Class<? extends AbstractEntity<?>>> entityTypes,
            final IDomainMetadata domainMetadata,
            final TransactionalExecution trEx) {
        LOGGER.info("Starting to save the domain metadata...");
        try {
            LOGGER.info("Removing old domain metadata records...");
            emptyExistingMetadata(trEx);

            final Set<Class<? extends AbstractEntity<?>>> domainTypesForIntrospection = entityTypes.stream().filter(EntityUtils::isIntrospectionAllowed).collect(toSet());
            final Map<Class<?>, DomainTypeData> typesMap = generateDomainTypeData(domainTypesForIntrospection, domainMetadata);

            LOGGER.info("Inserting metadata about domain entity types...");
            persistDomainTypesData(typesMap.values(), trEx, 1000);

            LOGGER.info("Inserting metadata about domain entity properties...");
            persistDomainPropsData(generateDomainPropsData(domainMetadata, typesMap), trEx, 1000);

            LOGGER.info("Completed saving of the domain metadata.");
        } catch (final Exception ex) {
            LOGGER.fatal("Presisting of the domain metadata did not succeed.", ex);
            throw ex;
        }
    }

    private static void emptyExistingMetadata(final TransactionalExecution trEx) {
        trEx.exec(conn -> {
            try (final Statement st = conn.createStatement()) {
                st.execute(EXISTING_DATA_DELETE_STMT);
            } catch (final SQLException ex) {
                throw new DbException(format("Failed to empty existing metadata."), ex);
            }
        });
    }

    private static Map<Class<?>, DomainTypeData> generateDomainTypeData(final Set<Class<? extends AbstractEntity<?>>> entityTypes, final IDomainMetadata domainMetadata) {
        final Map<Class<?>, DomainTypeData> result = new HashMap<>();
        long id = 0;
        for (final Class<? extends AbstractEntity<?>> entityType : entityTypes) {
            final var em = domainMetadata.forEntity(entityType);
            id = id + 1;
            final Pair<String, String> typeTitleAndDesc = getEntityTitleAndDesc(entityType);
            final List<? extends PropertyMetadata> props = em.properties().stream()
                    .filter(pm -> !pm.name().equals(VERSION) && !pm.type().isCollectional() && !pm.type().isCompositeKey())
                    .toList();

            final String tableName = (switch (em) {
                case EntityMetadata.Persistent pem -> Optional.of(pem.data().tableName());
                case EntityMetadata.Synthetic sem -> ifSyntheticBasedOnPersistent(domainMetadata, sem, pem -> pem.data().tableName());
                default -> Optional.<String> empty();
            }).orElseThrow(() -> new InvalidArgumentException("Couldn't determine table name for entity [%s].".formatted(em)));

            final Class<?> superType = ifSyntheticBasedOnPersistent(domainMetadata, em, EntityMetadata::javaType).orElse(null);

            result.put(entityType,
                       new DomainTypeData(entityType, superType, id, entityType.getName(), typeTitleAndDesc.getKey(),
                                          true, tableName, typeTitleAndDesc.getValue(), props.size(),
                                          domainMetadata.entityMetadataUtils().compositeKeyMembers(em),
                                          props));

            // collecting primitive, union,custom user types and pure types (like XXXGroupingProperty) from props
            for (final PropertyMetadata pm : props) {
                final Optional<Class<?>> optPropJavaType = switch (pm.type()) {
                    case PropertyTypeMetadata.Composite    it -> Optional.of(it.javaType());
                    case PropertyTypeMetadata.Primitive    it -> Optional.of(it.javaType());
                    case PropertyTypeMetadata.CompositeKey it -> Optional.of(it.javaType());
                    case PropertyTypeMetadata.Entity       it when domainMetadata.forType(it.javaType()).isEmpty()
                                                                   || !entityTypes.contains(it.javaType())
                                                                      && !result.containsKey(it.javaType())
                            -> Optional.of(it.javaType());
                    default -> Optional.empty();
                };

                if (optPropJavaType.isPresent()) {
                    // can't use ifPresent due to local variable "id"
                    final Class<?> propJavaType = optPropJavaType.get();
                    id = id + 1;

                    final List<PropertyMetadata.Persistent> subItems = domainMetadata.propertyMetadataUtils().subProperties(pm).stream()
                            .map(PropertyMetadata::asPersistent).flatMap(Optional::stream)
                            .toList();

                    final int propsCount = !subItems.isEmpty() && !(Money.class.equals(propJavaType) && subItems.size() == 1)
                            ? subItems.size() : 0;
                    final Pair<String, String> subTypeTitleAndDesc = isUnionEntityType(propJavaType)
                            ? getEntityTitleAndDesc((Class<? extends AbstractUnionEntity>) propJavaType)
                            : null;
                    final String title = subTypeTitleAndDesc != null ? subTypeTitleAndDesc.getKey() : propJavaType.getSimpleName();
                    final String titleDesc = subTypeTitleAndDesc != null ? subTypeTitleAndDesc.getValue() : propJavaType.getSimpleName();
                    result.put(propJavaType,
                               new DomainTypeData(propJavaType, null, id, propJavaType.getName(), title, false,
                                                  null, titleDesc, propsCount, emptyList(), emptyList()));
                }
            }
        }

        return result;
    }

    /**
     * If given a synthetic entity that is based on a persistent one, apply the function to the persistent one.
     *
     * @param em  potential synthetic entity
     * @param fn  function to apply
     */
    private static <R> Optional<R> ifSyntheticBasedOnPersistent(final IDomainMetadata domainMetadata, final EntityMetadata em,
                                                                final Function<EntityMetadata.Persistent, R> fn) {
        return em.asSynthetic()
                .map(EntityMetadata::javaType)
                .map(Class::getSuperclass)
                .flatMap(domainMetadata::forType)
                .flatMap(TypeMetadata::asEntity)
                .flatMap(EntityMetadata::asPersistent)
                .map(fn);
    }

    private static List<DomainPropertyData> generateDomainPropsData
    (final IDomainMetadata domainMetadata, final Map<Class<?>, DomainTypeData> typesMap)
    {
        final List<DomainPropertyData> result = new ArrayList<>();

        long id = typesMap.size();
        for (final DomainTypeData entityType : typesMap.values()) {
            if (!entityType.isEntity) {
                continue;
            }
            int position = 0;
            for (final PropertyMetadata pm : entityType.getProps().values()) {
                if (isExcluded(entityType.type, pm.name())) {
                    continue;
                }

                id = id + 1;
                position = position + 1;
                final Pair<String, String> prelTitleAndDesc = getTitleAndDesc(pm.name(), entityType.type);
                final String prelTitle = prelTitleAndDesc.getKey();
                final String prelDesc = prelTitleAndDesc.getValue();

                final var propJavaType = (Class<?>) pm.type().javaType();
                final DomainTypeData superTypeDtd = typesMap.get(entityType.superType);
                result.add(new DomainPropertyData(id, //
                                                  pm.name(), //
                                                  entityType.id, //
                                                  null, //
                                                  typesMap.get(propJavaType).id, //
                                                  prelTitle, //
                                                  prelDesc, //
                                                  entityType.getKeyMemberIndex(pm.name()), //
                                                  pm.is(REQUIRED), //
                                                  determinePropColumn(domainMetadata.propertyMetadataUtils(),
                                                                      entityType.superType == null
                                                                              ? pm
                                                                              : superTypeDtd.getProps().get(pm.name()) != null
                                                                                      ? superTypeDtd.getProps().get(pm.name())
                                                                                      : pm), //
                                                  position));

                // adding subproperties of union type properties
                final List<PropertyMetadata> subProps = domainMetadata.propertyMetadataUtils().subProperties(pm);
                if (subProps.size() > 1) { //skipping cases of SimpleMoney with single subproperty
                    final long holderId = id;
                    int subItemPosition = 0;
                    for (final var spm : subProps.stream().flatMap(spm -> spm.asPersistent().stream()).toList()) {
                        id = id + 1;
                        subItemPosition = subItemPosition + 1;
                        final Pair<String, String> titleAndDesc = getTitleAndDesc(spm.name(), propJavaType);
                        result.add(new DomainPropertyData(id, //
                                                          pm.name(), //
                                                          null, //
                                                          holderId, //
                                                          typesMap.get((Class<?>) spm.type().javaType()).id, //
                                                          titleAndDesc.getKey(), //
                                                          titleAndDesc.getValue(), //
                                                          null, //
                                                          false, //
                                                          spm.data().column().name, //
                                                          subItemPosition));
                    }
                }
            }
        }

        return result;
    }

    private static @Nullable String determinePropColumn(final PropertyMetadataUtils pmUtils, final PropertyMetadata pm) {
        return switch (pm) {
            case PropertyMetadata.CritOnly $ -> CRITERION;
            case PropertyMetadata.Persistent ppm -> ppm.data().column().name;
            default -> {
                final var subProps = pmUtils.subProperties(pm);
                yield subProps.size() == 1
                        ? subProps.getFirst().asPersistent().map(pspm -> pspm.data().column().name).orElse(null)
                        : null;
            }
        };
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
                            pst.setLong(1, propType.id);
                            pst.setString(2, propType.key);
                            pst.setString(3, propType.desc);
                            pst.setString(4, propType.dbTable);
                            pst.setString(5, propType.entityTypeDesc);
                            setBooleanParameter(6, propType.isEntity, pst);
                            pst.setInt(7, propType.propsCount);
                            pst.setInt(8, 0);
                            pst.addBatch();
                        } catch (final SQLException ex) {
                            final String error = format("Could not create insert for [%s].", propType.key);
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
                            pst.setLong(1, propType.id);
                            pst.setString(2, propType.name);
                            pst.setString(3, propType.title);
                            pst.setString(4, propType.desc);
                            setNullableLongParameter(5, propType.holderAsDomainType, pst);
                            setNullableLongParameter(6, propType.holderAsDomainProperty, pst);
                            pst.setLong(7, propType.domainType);
                            setNullableIntegerParameter(8, propType.keyIndex, pst);
                            setBooleanParameter(9, propType.required, pst);
                            pst.setString(10, propType.dbColumn);
                            pst.setInt(11, propType.position);
                            pst.setInt(12, 0);
                            pst.addBatch();
                        } catch (final SQLException ex) {
                            final String error = format("Could not create insert for [%s].", propType.name);
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

    private static class DomainTypeData {
        private final Class<?> type;
        private final Class<?> superType;
        private final long id;
        private final String key;
        private final String desc;
        private final boolean isEntity;
        private final String dbTable;
        private final String entityTypeDesc;
        private final int propsCount;
        private final Map<String, Integer> keyMembersIndices = new HashMap<>();
        private final Map<String, PropertyMetadata> props = new LinkedHashMap<>();

        public DomainTypeData(final Class<?> type, final Class<?> superType, final long id, final String key, final String desc, final boolean isEntity, final String dbTable, final String entityTypeDesc, final int propsCount, final List<? extends PropertyMetadata> keyMembers, final List<? extends PropertyMetadata> props) {
            this.type = type;
            this.superType = superType;
            this.id = id;
            this.key = key;
            this.desc = desc;
            this.isEntity = isEntity;
            this.dbTable = dbTable;
            this.entityTypeDesc = entityTypeDesc;
            this.propsCount = propsCount;
            for (final PropertyMetadata prop : props) {
                this.props.put(prop.name(), prop);
            }

            int i = 0;
            for (final PropertyMetadata km : keyMembers) {
                i = i + 1;
                keyMembersIndices.put(km.name(), i);
            }

            if (keyMembersIndices.isEmpty()) {
                keyMembersIndices.put("key", 0);
            }
        }

        public Integer getKeyMemberIndex(final String keyMember) {
            return keyMembersIndices.get(keyMember);
        }

        public Map<String, PropertyMetadata> getProps() {
            return unmodifiableMap(props);
        }
    }

    private static class DomainPropertyData {
        private final long id;
        private final String name;
        private final Long holderAsDomainType;
        private final Long holderAsDomainProperty;
        private final long domainType;
        private final String title;
        private final String desc;
        private final Integer keyIndex;
        private final boolean required;
        private final String dbColumn;
        private final int position;

        public DomainPropertyData(final long id, final String name, final Long holderAsDomainType, final Long holderAsDomainProperty, final long domainType, final String title, final String desc, final Integer keyIndex, final boolean required, final String dbColumn, final int position) {
            this.id = id;
            this.name = name;
            this.holderAsDomainType = holderAsDomainType;
            this.holderAsDomainProperty = holderAsDomainProperty;
            this.domainType = domainType;
            this.title = title;
            this.desc = desc;
            this.keyIndex = keyIndex;
            this.required = required;
            this.dbColumn = dbColumn;
            this.position = position;
        }
    }
}
