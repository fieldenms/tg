package ua.com.fielden.platform.test.mapping;

import org.hibernate.type.YesNoType;
import org.junit.Test;
import ua.com.fielden.platform.dashboard.DashboardRefreshFrequency;
import ua.com.fielden.platform.dashboard.DashboardRefreshFrequencyUnit;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.dbschema.HibernateMappingsGenerator;
import ua.com.fielden.platform.eql.meta.EqlTables;
import ua.com.fielden.platform.meta.DomainMetadataBuilder;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.persistence.types.HibernateTypeMappings;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.ui.config.EntityCentreConfig;
import ua.com.fielden.platform.ui.config.MainMenuItem;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class MappingGenerationTest {

    // TODO use dependency injection
    @Test
    public void dump_mapping_for_type_wity_byte_array_property() {
        final List<Class<? extends AbstractEntity<?>>> domainTypes = new ArrayList<>();
        domainTypes.add(User.class);
        domainTypes.add(MainMenuItem.class);
        domainTypes.add(DashboardRefreshFrequency.class);
        domainTypes.add(DashboardRefreshFrequencyUnit.class);
        domainTypes.add(EntityCentreConfig.class);

        final IDomainMetadata domainMetadata = new DomainMetadataBuilder(
                HibernateTypeMappings.builder()
                        .put(boolean.class, YesNoType.INSTANCE)
                        .put(Boolean.class, YesNoType.INSTANCE)
                        .build(),
                domainTypes, DbVersion.H2)
                .build();

        final String tgModelMapping = new HibernateMappingsGenerator(domainMetadata, new EqlTables(domainMetadata)).generateMappings();
        final String expectedMapping = String.format("""
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE hibernate-mapping PUBLIC
"-//Hibernate/Hibernate Mapping DTD 3.0//EN"
"http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd">
<hibernate-mapping default-access="field">
<class name="ua.com.fielden.platform.dashboard.DashboardRefreshFrequency" table="DASHBOARDREFRESHFREQUENCY_">
\t<id name="id" column="_ID" type="org.hibernate.type.LongType" access="property">
\t</id>
\t<version name="version" type="org.hibernate.type.LongType" access="field" insert="false">
\t\t<column name="_VERSION" default="0" />
\t</version>
\t<many-to-one name="refreshFrequencyUnit" class="ua.com.fielden.platform.dashboard.DashboardRefreshFrequencyUnit" column="REFRESHFREQUENCYUNIT_"/>
\t<property name="value" column="VALUE_" type="org.hibernate.type.IntegerType"/>
</class>

<class name="ua.com.fielden.platform.dashboard.DashboardRefreshFrequencyUnit" table="DASHBOARDREFRESHFREQUENCYUNIT_">
\t<id name="id" column="_ID" type="org.hibernate.type.LongType" access="property">
\t</id>
\t<version name="version" type="org.hibernate.type.LongType" access="field" insert="false">
\t\t<column name="_VERSION" default="0" />
\t</version>
\t<property name="millis" column="MILLIS_" type="org.hibernate.type.IntegerType"/>
\t<property name="unit" column="UNIT_" type="org.hibernate.type.StringType" length="16"/>
</class>

<class name="ua.com.fielden.platform.ui.config.EntityCentreConfig" table="ENTITY_CENTRE_CONFIG">
\t<id name="id" column="_ID" type="org.hibernate.type.LongType" access="property">
\t</id>
\t<version name="version" type="org.hibernate.type.LongType" access="field" insert="false">
\t\t<column name="_VERSION" default="0" />
\t</version>
\t<property name="configBody" column="BODY" type="org.hibernate.type.BinaryType" length="2147483647"/>
\t<property name="configUuid" column="CONFIGUUID_" type="org.hibernate.type.StringType"/>
\t<many-to-one name="dashboardRefreshFrequency" class="ua.com.fielden.platform.dashboard.DashboardRefreshFrequency" column="DASHBOARDREFRESHFREQUENCY_"/>
\t<property name="dashboardable" column="DASHBOARDABLE_" type="org.hibernate.type.YesNoType"/>
\t<property name="dashboardableDate" column="DASHBOARDABLEDATE_" type="org.hibernate.type.TimestampType"/>
\t<property name="desc" column="DESC_" type="org.hibernate.type.StringType"/>
\t<many-to-one name="menuItem" class="ua.com.fielden.platform.ui.config.MainMenuItem" column="ID_MAIN_MENU"/>
\t<many-to-one name="owner" class="ua.com.fielden.platform.security.user.User" column="ID_CRAFT"/>
\t<property name="preferred" column="PREFERRED_" type="org.hibernate.type.YesNoType"/>
\t<property name="principal" column="IS_PRINCIPAL" type="org.hibernate.type.YesNoType"/>
\t<property name="runAutomatically" column="RUNAUTOMATICALLY_" type="org.hibernate.type.YesNoType"/>
\t<property name="title" column="TITLE" type="org.hibernate.type.StringType"/>
</class>

<class name="ua.com.fielden.platform.ui.config.MainMenuItem" table="MAIN_MENU">
\t<id name="id" column="_ID" type="org.hibernate.type.LongType" access="property">
\t</id>
\t<version name="version" type="org.hibernate.type.LongType" access="field" insert="false">
\t\t<column name="_VERSION" default="0" />
\t</version>
\t<property name="key" column="KEY_" type="org.hibernate.type.StringType"/>
\t<property name="desc" column="DESC_" type="org.hibernate.type.StringType"/>
\t<property name="order" column="ITEM_ORDER" type="org.hibernate.type.IntegerType"/>
\t<many-to-one name="parent" class="ua.com.fielden.platform.ui.config.MainMenuItem" column="ID_PARENT"/>
\t<property name="title" column="TITLE" type="org.hibernate.type.StringType"/>
</class>

<class name="ua.com.fielden.platform.security.user.User" table="USER_">
\t<id name="id" column="_ID" type="org.hibernate.type.LongType" access="property">
\t</id>
\t<version name="version" type="org.hibernate.type.LongType" access="field" insert="false">
\t\t<column name="_VERSION" default="0" />
\t</version>
\t<property name="key" column="KEY_" type="org.hibernate.type.StringType"/>
\t<property name="active" column="ACTIVE_" type="org.hibernate.type.YesNoType"/>
\t<property name="base" column="BASE_" type="org.hibernate.type.YesNoType"/>
\t<many-to-one name="basedOnUser" class="ua.com.fielden.platform.security.user.User" column="BASEDONUSER_"/>
\t<many-to-one name="createdBy" class="ua.com.fielden.platform.security.user.User" column="CREATEDBY_"/>
\t<property name="createdDate" column="CREATEDDATE_" type="org.hibernate.type.TimestampType"/>
\t<property name="createdTransactionGuid" column="CREATEDTRANSACTIONGUID_" type="org.hibernate.type.StringType"/>
\t<property name="email" column="EMAIL_" type="org.hibernate.type.StringType"/>
\t<many-to-one name="lastUpdatedBy" class="ua.com.fielden.platform.security.user.User" column="LASTUPDATEDBY_"/>
\t<property name="lastUpdatedDate" column="LASTUPDATEDDATE_" type="org.hibernate.type.TimestampType"/>
\t<property name="lastUpdatedTransactionGuid" column="LASTUPDATEDTRANSACTIONGUID_" type="org.hibernate.type.StringType"/>
\t<property name="refCount" column="REFCOUNT_" type="org.hibernate.type.IntegerType"/>
\t<property name="ssoOnly" column="SSOONLY_" type="org.hibernate.type.YesNoType"/>
</class>

</hibernate-mapping>""", Integer.MAX_VALUE);
        assertEquals("Incorrect mapping.", expectedMapping, tgModelMapping);
    }

}
