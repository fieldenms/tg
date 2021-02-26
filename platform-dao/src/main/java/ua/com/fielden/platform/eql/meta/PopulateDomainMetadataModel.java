package ua.com.fielden.platform.eql.meta;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.AbstractEntity.VERSION;
import static ua.com.fielden.platform.entity.query.metadata.EntityCategory.PERSISTED;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getTitleAndDesc;
import static ua.com.fielden.platform.utils.EntityUtils.isEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistedEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isSyntheticEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isUnionEntityType;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.inject.Guice;

import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.dao.exceptions.DbException;
import ua.com.fielden.platform.dao.session.TransactionalExecution;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.ioc.HibernateUserTypesModule;
import ua.com.fielden.platform.types.tuples.T2;

public class PopulateDomainMetadataModel {
    final static String DOMAINTYPE_INSERT_STMT = "INSERT INTO DOMAINTYPE_ VALUES(?,?,?,?,?,?,?);";
    final static String DOMAINPROPERTY_INSERT_STMT = "INSERT INTO DOMAINPROPERTY_ VALUES(?,?,?,?,?,?,?,?,?,?,?);";

    public static void populate( //
            final IApplicationDomainProvider applicationDomainProvider, //
            final List<Class<? extends AbstractEntity<?>>> entityTypes, //
            final TransactionalExecution trEx, //
            final Map<Class, Class> hibTypesDefaults, //
            final DbVersion dbVersion) {

        final Map<Class<?>, Class<?>> htd = new HashMap<>();
        for (final Entry<Class, Class> el : hibTypesDefaults.entrySet()) {
            htd.put(el.getKey(), el.getValue());
        }

        final EqlDomainMetadata dm = new EqlDomainMetadata(//
                htd, //
                Guice.createInjector(new HibernateUserTypesModule()), //
                applicationDomainProvider.entityTypes(), dbVersion);

        final Set<Class<?>> propTypes = new HashSet<>();

        final Map<Class<?>, String> tableNamesMap = new HashMap<>();

        for (final Class<? extends AbstractEntity<?>> et : entityTypes) {
            final EqlEntityMetadata el = dm.entityPropsMetadata().get(et);
            if (el != null) {
                propTypes.add(el.typeInfo.entityType);
                if (el.typeInfo.category == PERSISTED) {
                    tableNamesMap.put(el.typeInfo.entityType, el.typeInfo.tableName);
                }

                for (final EqlPropertyMetadata pmd : el.props()) {
                    propTypes.add(pmd.javaType);
                    for (final EqlPropertyMetadata subPmd : pmd.subitems()) {
                        propTypes.add(subPmd.javaType);
                    }
                }
            }
        }

        trEx.exec(conn -> {
            try (final PreparedStatement pst = conn.prepareStatement("DELETE FROM DOMAINPROPERTY_; DELETE FROM DOMAINTYPE_;")) {
                pst.execute();
            } catch (final SQLException ex) {
                throw new DbException(format("Could not delete"), ex);
            }
        });

        final Map<Class<?>, Integer> domainTypesIdsByClass = new HashMap<>();
        int id = 0;
        for (final Class<?> propType : propTypes) {
            id = id + 1;
            domainTypesIdsByClass.put(propType, id);
        }

        trEx.exec(conn -> {
            try (final PreparedStatement pst = conn.prepareStatement(DOMAINTYPE_INSERT_STMT)) {
                // batch insert statements
                propTypes.stream().forEach(propType -> {
                    final boolean isEntity = (isPersistedEntityType(propType) //
                            || isUnionEntityType(propType) //
                            || (isEntityType(propType) && isSyntheticEntityType((Class<? extends AbstractEntity<?>>) propType)));
                    try {
                        pst.setLong(1, domainTypesIdsByClass.get(propType));
                        pst.setString(2, propType.getName());
                        pst.setString(3, isEntity ? getEntityTitleAndDesc((Class<? extends AbstractEntity<?>>) propType).getKey() : propType.getSimpleName());
                        pst.setString(4, tableNamesMap.get(propType));
                        pst.setString(5, (isEntity ? getEntityTitleAndDesc((Class<? extends AbstractEntity<?>>) propType).getValue() : propType.getSimpleName()));
                        pst.setString(6, isEntity && !isUnionEntityType(propType) ? "Y" : "N");
                        pst.setInt(7, 0);
                        pst.addBatch();
                    } catch (final SQLException ex) {
                        final String error = format("Could not create insert for [%s].", propType.getName());
                        throw new DbException(error, ex);
                    }
                });

                // execute the batch
                pst.executeBatch();
            } catch (final SQLException ex) {
                final String error = format("Could not batch insert for [%s].", propTypes.size());
                throw new DbException(error, ex);
            }
        });

        List<DomainPropertyData> dpd = new ArrayList<>();

        for (final Class<?> et : entityTypes) {
            final EqlEntityMetadata em = dm.entityPropsMetadata().get(et);

            if (em != null) {
                int position = 0;
                for (final EqlPropertyMetadata pm : em.props().stream().filter(pm -> !pm.name.equals(VERSION) && !pm.isVirtualKey()).collect(toList())) {

                    final Map<String, Integer> keyMembersIndices = new HashMap<>();
                    int i = 0;
                    for (final T2<String, Class<?>> element : em.typeInfo.compositeKeyMembers) {
                        i = i + 1;
                        keyMembersIndices.put(element._1, i);
                    }

                    if (keyMembersIndices.isEmpty()) {
                        keyMembersIndices.put("key", 0);
                    }
                    id = id + 1;
                    position = position + 1;
                    final String prelTitle = getTitleAndDesc(pm.getName(), em.typeInfo.entityType).getKey();
                    dpd.add(new DomainPropertyData(id, //
                            pm.getName(), //
                            domainTypesIdsByClass.get(em.typeInfo.entityType), //
                            null, //
                            domainTypesIdsByClass.get(pm.javaType), //
                            (isEmpty(prelTitle) && ID.equals(pm.getName()) ? "ID" : prelTitle), //
                            keyMembersIndices.get(pm.getName()), //
                            pm.required, //
                            (pm.column != null ? pm.column.name
                                    : (pm.subitems().size() == 1 ? (pm.subitems().get(0).column != null ? pm.subitems().get(0).column.name : null) : null)), //
                            position));

                    if (pm.subitems().size() > 1) {
                        int holderId = id;
                        int subItemPosition = 0;
                        for (final EqlPropertyMetadata subProp : pm.subitems().stream().filter(el -> el.column != null).collect(toList())) {
                            id = id + 1;
                            subItemPosition = subItemPosition + 1;
                            dpd.add(new DomainPropertyData(id, //
                                    subProp.getName(), //
                                    null, //
                                    holderId, //
                                    domainTypesIdsByClass.get(subProp.javaType), //
                                    getTitleAndDesc(subProp.getName(), pm.javaType).getKey(), //
                                    null, //
                                    false, //
                                    subProp.column.name, //
                                    subItemPosition));
                        }
                    }
                }
            }
        }

        trEx.exec(conn -> {

            try (final PreparedStatement pst = conn.prepareStatement(DOMAINPROPERTY_INSERT_STMT)) {
                // batch insert statements

                dpd.stream().forEach(propType -> {
                    try {
                        pst.setLong(1, propType.id);
                        pst.setString(2, propType.name);
                        pst.setString(3, propType.title);
                        if (propType.holderAsDomainType != null) {
                            pst.setLong(4, propType.holderAsDomainType);
                            pst.setNull(5, 4);
                        } else {
                            pst.setNull(4, 4);
                            pst.setLong(5, propType.holderAsDomainProperty);
                        }
                        pst.setLong(6, propType.domainType);
                        if (propType.keyIndex != null) {
                            pst.setInt(7, propType.keyIndex);
                        } else {
                            pst.setNull(7, 4);
                        }

                        pst.setString(8, propType.required ? "Y" : "N");
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
                final String error = format("Could not batch insert [%s].", propTypes.size());
                throw new DbException(error, ex);
            }
        });
    }

    private static class DomainPropertyData {
        private final int id;
        private final String name;
        private final Integer holderAsDomainType;
        private final Integer holderAsDomainProperty;
        private final int domainType;
        private final String title;
        private final Integer keyIndex;
        private final boolean required;
        private final String dbColumn;
        private final int position;

        public DomainPropertyData(final int id, final String name, final Integer holderAsDomainType, final Integer holderAsDomainProperty, final int domainType, final String title, final Integer keyIndex, final boolean required, final String dbColumn, final int position) {
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