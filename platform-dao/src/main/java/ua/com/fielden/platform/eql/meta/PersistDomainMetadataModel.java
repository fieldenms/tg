package ua.com.fielden.platform.eql.meta;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.AbstractEntity.VERSION;
import static ua.com.fielden.platform.entity.query.metadata.EntityCategory.QUERY_BASED;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getTitleAndDesc;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistedEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isUnionEntityType;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ua.com.fielden.platform.attachment.Attachment;
import ua.com.fielden.platform.dao.exceptions.DbException;
import ua.com.fielden.platform.dao.session.TransactionalExecution;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityForCollectionModification;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.security.user.SecurityRoleAssociation;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;
import ua.com.fielden.platform.security.user.UserRole;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.Pair;

/**
 * Performs instant (re-)persistance of the Domain Metadata Model entities from an actual TG-Java domain.   
 * 
 * @author TG Team 
 *
 */
public class PersistDomainMetadataModel {
    final static String CRITERION = "[selection criterion]";
    final static String DOMAINTYPE_INSERT_STMT = "INSERT INTO DOMAINTYPE_ VALUES(?,?,?,?,?,?,?,?);";
    final static String DOMAINPROPERTY_INSERT_STMT = "INSERT INTO DOMAINPROPERTY_ VALUES(?,?,?,?,?,?,?,?,?,?,?);";
    final static String EXISTING_DATA_DELETE_STMT = "DELETE FROM DOMAINPROPERTY_; DELETE FROM DOMAINTYPE_;";
    final static List<Class<? extends AbstractEntity<?>>> platformTypesOfInterest = asList(Attachment.class, User.class, UserRole.class, UserAndRoleAssociation.class, SecurityRoleAssociation.class);

    /**
     * Synchronises persistent model of Domain Explorer entities with actual Domain java classes. 
     * 
     * @param entityTypes - domain entities to be included into metadata model entities data.
     * @param domainMetadata
     * @param trEx
     */
    public static void persist( //
            final List<Class<? extends AbstractEntity<?>>> entityTypes, //
            final EqlDomainMetadata domainMetadata,
            final TransactionalExecution trEx) {
        
        emptyExistingMetadata(trEx);
        
        final Set<Class<? extends AbstractEntity<?>>> adjustedDomainTypes = adjustDomainTypes(entityTypes);

        final Map<Class<?>, DomainTypeData> typesMap = generateDomainTypeData(adjustedDomainTypes, domainMetadata);
        
        persistDomainTypesData(typesMap.values(), trEx);
        
        persistDomainPropsData(generateDomainPropsData(typesMap), trEx);
    }

    private static final Set<Class<? extends AbstractEntity<?>>> adjustDomainTypes(final List<Class<? extends AbstractEntity<?>>> entityTypes) {
        final Set<Class<? extends AbstractEntity<?>>> domainTypes = entityTypes.stream().filter(type -> shouldInclude(type)).collect(toSet());
        domainTypes.addAll(platformTypesOfInterest);
        return domainTypes;
    }
    
    private static boolean shouldInclude(final Class<? extends AbstractEntity<?>> type) {
        return !(isUnionEntityType(type)
                || AbstractFunctionalEntityWithCentreContext.class.isAssignableFrom(type) // need to exclude them as some are persistent
                || AbstractFunctionalEntityForCollectionModification.class.isAssignableFrom(type) // need to exclude them as some are persistent
                );
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

    private static Map<Class<?>, DomainTypeData> generateDomainTypeData(final Set<Class<? extends AbstractEntity<?>>> entityTypes, final EqlDomainMetadata domainMetadata) {
        final Map<Class<?>, DomainTypeData> result = new HashMap<>();
        long id = 0;
        final Map<Class<? extends AbstractEntity<?>>, EqlEntityMetadata> entitiesMetadataMap = domainMetadata.entityPropsMetadata();
        for (final Class<? extends AbstractEntity<?>> entityType : entityTypes) {
            final EqlEntityMetadata entityMd = entitiesMetadataMap.get(entityType);
            if (entityMd != null) {
                id = id + 1;
                final Pair<String, String> typeTitleAndDesc = getEntityTitleAndDesc(entityType);
                final List<EqlPropertyMetadata> propsMd = entityMd.props().stream().filter(pm -> !pm.name.equals(VERSION) && !pm.isVirtualKey()).collect(toList());
                final Class<?> superType = entityMd.typeInfo.category == QUERY_BASED && isPersistedEntityType(entityType.getSuperclass()) ? entityType.getSuperclass() : null;
                final String tableName = superType == null ? entityMd.typeInfo.tableName : entitiesMetadataMap.get(superType).typeInfo.tableName;
                result.put(entityType, new DomainTypeData(entityType, superType, id, entityType.getName(), typeTitleAndDesc.getKey(), true, tableName, typeTitleAndDesc.getValue(), propsMd.size(), entityMd.typeInfo.compositeKeyMembers, propsMd));
                
                // collecting primitive, union,custom user types and pure types (like XXXGroupingProperty) from props
                for (final EqlPropertyMetadata pmd : propsMd) {
                    if ((!entitiesMetadataMap.containsKey(pmd.javaType) || !entityTypes.contains(pmd.javaType)) && !result.containsKey(pmd.javaType)) {
                        id = id + 1;
                        final List<EqlPropertyMetadata> subItems = pmd.subitems().stream().filter(si -> si.column != null).collect(toList());
                        final int propsCount = !subItems.isEmpty() && !(Money.class.equals(pmd.javaType) && subItems.size() == 1) ? subItems.size() : 0;
                        final Pair<String, String> subTypeTitleAndDesc = isUnionEntityType(pmd.javaType) ? getEntityTitleAndDesc((Class<? extends AbstractUnionEntity>) pmd.javaType) : null;
                        final String title = subTypeTitleAndDesc != null ? subTypeTitleAndDesc.getKey() : pmd.javaType.getSimpleName();
                        final String titleDesc = subTypeTitleAndDesc != null ? subTypeTitleAndDesc.getValue() : pmd.javaType.getSimpleName();
                        result.put(pmd.javaType, new DomainTypeData(pmd.javaType, null, id, pmd.javaType.getName(), title, false, null, titleDesc, propsCount, emptyList(), emptyList()));
                    }
                }
            }
        }

        return result;
    }
    
    private static List<DomainPropertyData> generateDomainPropsData(final Map<Class<?>, DomainTypeData> typesMap) {
        final List<DomainPropertyData> result = new ArrayList<>();

        long id = typesMap.size();
        for (final DomainTypeData entityType : typesMap.values().stream().filter(t -> t.isEntity).collect(toSet())) {
            int position = 0;
            for (final EqlPropertyMetadata propMd : entityType.getProps().values()) {
                id = id + 1;
                position = position + 1;
                final String prelTitle = getTitleAndDesc(propMd.name, entityType.type).getKey();

                final DomainTypeData superTypeDtd = typesMap.get(entityType.superType);
                result.add(new DomainPropertyData(id, //
                        propMd.name, //
                        entityType.id, //
                        null, //
                        typesMap.get(propMd.javaType).id, //
                        (isEmpty(prelTitle) && ID.equals(propMd.getName()) ? "ID" : prelTitle), //
                        entityType.getKeyMemberIndex(propMd.name), //
                        propMd.required, //
                        determinePropColumn(entityType.superType == null ? propMd
                                : superTypeDtd.getProps().get(propMd.name) != null ? superTypeDtd.getProps().get(propMd.name) : propMd), //
                        position));

                // adding subproperties of union type properties 
                if (propMd.subitems().size() > 1) { //skipping cases of SimpleMoney with single subproperty
                    final long holderId = id;
                    int subItemPosition = 0;
                    for (final EqlPropertyMetadata subProp : propMd.subitems().stream().filter(el -> el.column != null).collect(toList())) {
                        id = id + 1;
                        subItemPosition = subItemPosition + 1;
                        result.add(new DomainPropertyData(id, //
                                subProp.getName(), //
                                null, //
                                holderId, //
                                typesMap.get(subProp.javaType).id, //
                                getTitleAndDesc(subProp.getName(), propMd.javaType).getKey(), //
                                null, //
                                false, //
                                subProp.column.name, //
                                subItemPosition));
                    }
                }
            }
        }

        return result;
    }
    
    private static String determinePropColumn(final EqlPropertyMetadata propMd) {
        if (propMd.critOnly) {
            return CRITERION;
        }
        return (propMd.column != null ? propMd.column.name
                : (propMd.subitems().size() == 1 ? (propMd.subitems().get(0).column != null ? propMd.subitems().get(0).column.name : null) : null));
    }
    
    private static void persistDomainTypesData(final Collection<DomainTypeData> dtd, final TransactionalExecution trEx) {
        trEx.exec(conn -> {
            try (final PreparedStatement pst = conn.prepareStatement(DOMAINTYPE_INSERT_STMT)) {
                // batch insert statements
                dtd.stream().forEach(propType -> {
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

                // execute the batch
                pst.executeBatch();
            } catch (final SQLException ex) {
                final String error = format("Could not batch insert for [%s].", dtd.size());
                throw new DbException(error, ex);
            }
        });   
    }
    
    private static void persistDomainPropsData(final List<DomainPropertyData> dpd, final TransactionalExecution trEx) {
        trEx.exec(conn -> {
            try (final PreparedStatement pst = conn.prepareStatement(DOMAINPROPERTY_INSERT_STMT)) {
                // batch insert statements

                dpd.stream().forEach(propType -> {
                    try {
                        pst.setLong(1, propType.id);
                        pst.setString(2, propType.name);
                        pst.setString(3, propType.title);
                        setNullableLongParameter(4, propType.holderAsDomainType, pst);
                        setNullableLongParameter(5, propType.holderAsDomainProperty, pst);
                        pst.setLong(6, propType.domainType);
                        setNullableIntegerParameter(7, propType.keyIndex, pst);
                        setBooleanParameter(8, propType.required, pst);
                        pst.setString(9, propType.dbColumn);
                        pst.setInt(10, propType.position);
                        pst.setInt(11, 0);
                        pst.addBatch();
                    } catch (final SQLException ex) {
                        final String error = format("Could not create insert for [%s].", propType.name);
                        throw new DbException(error, ex);
                    }
                });

                // execute the batch
                pst.executeBatch();
            } catch (final SQLException ex) {
                final String error = format("Could not batch insert [%s].", dpd.size());
                throw new DbException(error, ex);
            }
        });
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
        private final Map<String, EqlPropertyMetadata> props = new LinkedHashMap<>();
        
        public DomainTypeData(final Class<?> type, final Class<?> superType, final long id, final String key, final String desc, final boolean isEntity, final String dbTable, final String entityTypeDesc, final int propsCount, final List<T2<String, Class<?>>> keyMembers, final List<EqlPropertyMetadata> props) {
            this.type = type;
            this.superType = superType;
            this.id = id;
            this.key = key;
            this.desc = desc;
            this.isEntity = isEntity;
            this.dbTable = dbTable;
            this.entityTypeDesc = entityTypeDesc;
            this.propsCount = propsCount;
            for (final EqlPropertyMetadata prop : props) {
                this.props.put(prop.name, prop);
            }
            
            int i = 0;
            for (final T2<String, Class<?>> element : keyMembers) {
                i = i + 1;
                keyMembersIndices.put(element._1, i);
            }

            if (keyMembersIndices.isEmpty()) {
                keyMembersIndices.put("key", 0);
            }
        }
        
        public Integer getKeyMemberIndex(final String keyMember) {
            return keyMembersIndices.get(keyMember);
        }
        
        public Map<String, EqlPropertyMetadata> getProps() {
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
        private final Integer keyIndex;
        private final boolean required;
        private final String dbColumn;
        private final int position;

        public DomainPropertyData(final long id, final String name, final Long holderAsDomainType, final Long holderAsDomainProperty, final long domainType, final String title, final Integer keyIndex, final boolean required, final String dbColumn, final int position) {
            this.id = id;
            this.name = name;
            this.holderAsDomainType = holderAsDomainType;
            this.holderAsDomainProperty = holderAsDomainProperty;
            this.domainType = domainType;
            this.title = title;
            this.keyIndex = keyIndex;
            this.required = required;
            this.dbColumn = dbColumn;
            this.position = position;
        }
    }
}