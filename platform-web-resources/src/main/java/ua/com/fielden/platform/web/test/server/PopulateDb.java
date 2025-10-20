package ua.com.fielden.platform.web.test.server;

import fielden.test_app.close_leave.TgCloseLeaveExample;
import org.apache.logging.log4j.Logger;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.dialect.PostgreSQL82Dialect;
import org.joda.time.DateTime;
import ua.com.fielden.platform.algorithm.search.ISearchAlgorithm;
import ua.com.fielden.platform.algorithm.search.bfs.BreadthFirstSearch;
import ua.com.fielden.platform.basic.config.exceptions.ApplicationConfigurationException;
import ua.com.fielden.platform.ddl.IDdlGenerator;
import ua.com.fielden.platform.devdb_support.DomainDrivenDataPopulation;
import ua.com.fielden.platform.devdb_support.SecurityTokenAssociator;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.persistence.HibernateUtil;
import ua.com.fielden.platform.sample.domain.*;
import ua.com.fielden.platform.sample.domain.composite.TgMinorComponent;
import ua.com.fielden.platform.sample.domain.composite.TgRollingStockMajorComponent;
import ua.com.fielden.platform.sample.domain.composite.TgRollingStockMinorComponent;
import ua.com.fielden.platform.sample.domain.compound.TgCompoundEntity;
import ua.com.fielden.platform.sample.domain.compound.TgCompoundEntityChild;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.provider.ISecurityTokenProvider;
import ua.com.fielden.platform.security.provider.SecurityTokenNode;
import ua.com.fielden.platform.security.user.*;
import ua.com.fielden.platform.test.IDomainDrivenTestCaseConfiguration;
import ua.com.fielden.platform.types.Colour;
import ua.com.fielden.platform.types.Hyperlink;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.utils.DbUtils;
import ua.com.fielden.platform.web.test.config.ApplicationDomain;

import java.io.FileInputStream;
import java.math.BigDecimal;
import java.util.*;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.types.RichText.fromHtml;

/**
 * This is a convenience class for (re-)creation of the development database and its population for Web UI Testing Server.
 *
 * It contains the <code>main</code> method and can be executed whenever the target database needs to be (re-)set.
 * <p>
 *
 * <b>IMPORTANT: </b><i>One should be careful not to run this code against the deployment or production databases, which would lead to the loss of all data.</i>
 *
 * <p>
 *
 * @author TG Team
 *
 */
public class PopulateDb extends DomainDrivenDataPopulation {
    private static final Logger LOGGER = getLogger(PopulateDb.class);

    private final ApplicationDomain applicationDomainProvider = new ApplicationDomain();

    private PopulateDb(final IDomainDrivenTestCaseConfiguration config, final Properties props) {
        super(config, props);
    }

    public static void main(final String[] args) throws Exception {
        LOGGER.info("Initialising...");
        final var props = new Properties();
        final String propsFileSuffix; // is used to load either application-PostreSql.properties or application-SqlServer.properties
        // Three system properties are required: databaseUri, databaseUser and databasePasswd.
        final var databseUri = System.getProperty("databaseUri");
        if (isEmpty(databseUri)) {
            throw new ApplicationConfigurationException("Property 'databaseUri' is required.");
        } else {
            final String jdbcUri;
            if (databseUri.contains("5432")) {
                propsFileSuffix = "PostgreSql";
                jdbcUri = "jdbc:postgresql:" + databseUri; 
            } else {
                propsFileSuffix = "SqlServer";
                jdbcUri = "jdbc:sqlserver:" + databseUri; 
 
            }
            props.put("hibernate.connection.url", jdbcUri);
        }
        final var dbUser = System.getProperty("databaseUser");
        if (isEmpty(dbUser)) {
            throw new ApplicationConfigurationException("Property 'databaseUser' is required.");
        } else {
            props.put("hibernate.connection.username", dbUser);
        }
        final var dbPasswd = System.getProperty("databasePasswd");
        if (isEmpty(dbPasswd)) {
            throw new ApplicationConfigurationException("Property 'databasePasswd' is required.");
        } else {
            props.put("hibernate.connection.password", dbPasswd);
        }
        // Default application-PostreSql.properties and application-SqlServer.properties do not have any of the properties already assigned from system properties databaseUri, databaseUser and databasePasswd.
        // However, if some alternative application.properties is provided, which contains those properties, the values from the file will get used.
        final String configFileName = args.length == 1 ? args[0] : "src/main/resources/application-%s.properties".formatted(propsFileSuffix);
        try (final FileInputStream in = new FileInputStream(configFileName)) {
            props.load(in);
        }
        
        
        LOGGER.info("Obtaining Hibernate dialect...");
        final Class<?> dialectType = Class.forName(props.getProperty("hibernate.dialect"));
        final Dialect dialect = (Dialect) dialectType.getDeclaredConstructor().newInstance();
        LOGGER.info(format("Running with dialect %s...", dialect));
        final DataPopulationConfig config = new DataPopulationConfig(props);
        LOGGER.info("Generating DDL and running it against the target DB...");

        // use TG DDL generation or
        // Hibernate DDL generation final List<String> createDdl = DbUtils.generateSchemaByHibernate()
        final List<String> createDdl = config.getInstance(IDdlGenerator.class).generateDatabaseDdl(dialect);
        final List<String> ddl = dialect instanceof H2Dialect ?           DbUtils.prependDropDdlForH2(createDdl) :
                                 dialect instanceof PostgreSQL82Dialect ? DbUtils.prependDropDdlForPostgresql(createDdl) :
                                                                          DbUtils.prependDropDdlForSqlServer(createDdl);
        DbUtils.execSql(ddl, config.getInstance(HibernateUtil.class).getSessionFactory().getCurrentSession());

        final PopulateDb popDb = new PopulateDb(config, props);
        popDb.populateDomain();
    }

    @Override
    protected void populateDomain() {
        // NOTE: If new test entities need to be populated, it must be done at the very end of this method.
        //       This is because Web UI tests rely on a specific order of entity IDs.

        LOGGER.info("Creating and populating the development database...");

        // VIRTUAL_USER is a virtual user (cannot be persisted) and has full access to all security tokens
        // It should always be used as the current user for data population activities
        final IUser coUser = co$(User.class);
        final User u = new_(User.class, User.system_users.VIRTUAL_USER.name()).setBase(true);
        final IUserProvider up = getInstance(IUserProvider.class);
        up.setUser(u);

        final User _su = coUser.save(new_(User.class, User.system_users.SU.name()).setBase(true).setEmail("SU@demoapp.com").setActive(true));
        final User su = coUser.resetPasswd(_su, _su.getKey()).getKey();
        final User _demo = co$(User.class).save(new_(User.class, "DEMO").setBasedOnUser(su).setEmail("DEMO@demoapp.com").setActive(true));
        final User demo = coUser.resetPasswd(_demo, _demo.getKey()).getKey();

        final ITgPerson aoPerson = (ITgPerson) co$(TgPerson.class);
        aoPerson.populateNew("Super", "User", "Super User", User.system_users.SU.name());

        final UserRole admin = save(new_(UserRole.class, "ADMINISTRATION", "A role, which has a full access to the the system and should be used only for users who need administrative previligies.").setActive(true));
        save(new_composite(UserAndRoleAssociation.class, su, admin));

        LOGGER.info("\tPopulate testing entities...");
        final var ent1 = save(new_(TgPersistentEntityWithProperties.class, "KEY1").setIntegerProp(43).setRequiredValidatedProp(30)
                .setDesc("Description for entity with key 1. This is a relatively long description to demonstrate how well does is behave during value autocompletion."));
        final var ent2 = save(new_(TgPersistentEntityWithProperties.class, "KEY2").setIntegerProp(14).setDesc("Description for entity with key 2.").setRequiredValidatedProp(30));
        final var ent3 = save(new_(TgPersistentEntityWithProperties.class, "KEY3").setIntegerProp(15).setDesc("Description for entity with key 3.").setRequiredValidatedProp(30));
        save(ent2.setEntityProp(ent3));
        save(new_(TgPersistentEntityWithProperties.class, "KEY4").setIntegerProp(63).setMoneyProp(new Money("23.0", Currency.getInstance("USD"))).setDesc("Description for entity with key 4.").setRequiredValidatedProp(30));
        save(new_(TgPersistentEntityWithProperties.class, "KEY5").setBigDecimalProp(new BigDecimal(23.0)).setDesc("Description for entity with key 5.").setRequiredValidatedProp(30));
        save(new_(TgPersistentEntityWithProperties.class, "KEY6").setIntegerProp(61).setStringProp("ok").setDesc("Description for entity with key 6.").setRequiredValidatedProp(30).setBigDecimalProp(new BigDecimal("12"))); // id = 10L
        save(new_(TgPersistentEntityWithProperties.class, "KEY7").setBooleanProp(true).setDesc("Description for entity with key 7.").setRequiredValidatedProp(30));
        save(new_(TgPersistentEntityWithProperties.class, "KEY8").setDateProp(new DateTime(3609999L).toDate()).setDesc("Description for entity with key 8.").setRequiredValidatedProp(30));
        final var defaultEnt = save(
                new_(TgPersistentEntityWithProperties.class, "DEFAULT_KEY")
                        // please note that proxies are not created for 'null' entity properties and regular (date, string..) properties!
                        // .setProducerInitProp(ent1)
                        .setIntegerProp(7)
                        .setMoneyProp(new Money("7.0", Currency.getInstance("USD")))
                        .setBigDecimalProp(new BigDecimal("7.7"))
                        .setStringProp("ok_def")
                        .setBooleanProp(true)
                        .setDateProp(new DateTime(7777L).toDate())
                        .setRequiredValidatedProp(30)
                        .setColourProp(Colour.RED)
                        .setHyperlinkProp(new Hyperlink("https://www.fielden.com.au"))
                        .setDesc("Default entity description"));
        final var staleEnt1 = save(new_(TgPersistentEntityWithProperties.class, "KEY10").setConflictingProp("initial")
                                           .setNonConflictingProp("initial")
                                           .setDesc("Description for entity with key 10.")
                                           .setRequiredValidatedProp(30));
        save(staleEnt1.setConflictingProp("persistently modified").setRequiredValidatedProp(30));

        final var compositeEnt1 = save(
                new_composite(TgPersistentCompositeEntity.class, defaultEnt, 10)
                        .setDesc("Default composite entity description as a long text to demonstrate proper word wrapping as part of displaying the autocompleted values."));

        final var ent11 = save(
                new_(TgPersistentEntityWithProperties.class, "KEY11")
                        .setStringProp("ok")
                        .setIntegerProp(43)
                        .setEntityProp(defaultEnt)
                        .setBigDecimalProp(new BigDecimal(23).setScale(5))
                        .setDateProp(new DateTime(960000L).toDate())
                        .setBooleanProp(true)
                        .setCompositeProp(compositeEnt1)
                        .setDesc("Description for entity with key 11.")
                        .setRequiredValidatedProp(30));

        save(new_(TgFetchProviderTestEntity.class, "FETCH1").setProperty(ent11).setAdditionalProperty(su));

        LOGGER.info("\tPopulate demo entities...");
        createDemoDomain(ent1, ent3, compositeEnt1);

        save(new_(TgEntityForColourMaster.class, "KEY12").setStringProp("ok").setBooleanProp(true).setColourProp(new Colour("aaacdc")));
        save(new_(TgEntityWithPropertyDependency.class, "KEY1").setProperty("IS").setCritOnlySingleProp(new Date()));
        save(new_composite(UserAndRoleAssociation.class, demo, admin));

        final var stationMgr = save(new_(UserRole.class, "STATION_MGR", "A role, which has access to the the station managing functionality."));
        save(new_composite(UserAndRoleAssociation.class, su, stationMgr));
        save(new_(UserRole.class, "TEST_ROLE1", "A role, which has access to the the station managing functionality."));
        save(new_(UserRole.class, "TEST_ROLE2", "A role, which has access to the the station managing functionality."));
        save(new_(UserRole.class, "TEST_ROLE3", "A role, which has access to the the station managing functionality."));
        save(new_(UserRole.class, "TEST_ROLE4", "A role, which has access to the the station managing functionality."));
        save(new_(UserRole.class, "TEST_ROLE5", "A role, which has access to the the station managing functionality."));
        save(new_(UserRole.class, "TEST_ROLE6", "A role, which has access to the the station managing functionality."));
        save(new_(UserRole.class, "TEST_ROLE7", "A role, which has access to the the station managing functionality."));
        save(new_(UserRole.class, "TEST_ROLE8", "A role, which has access to the the station managing functionality."));
        save(new_(UserRole.class, "TEST_ROLE9", "A role, which has access to the the station managing functionality."));

        final var csp1 = save(new_(TgCollectionalSerialisationParent.class, "CSP1").setDesc("desc1"));
        save(new_composite(TgCollectionalSerialisationChild.class, csp1, "1").setDesc("desc1"));

        // has children -- to be used for collectional crit-only
        final var ewpd1 = save(new_(TgEntityWithPropertyDescriptor.class, "KEY1")
                                       .setPropertyDescriptor(new PropertyDescriptor<>(TgPersistentEntityWithProperties.class, "integerProp")));

        // multi criteria

        // has children -- to be used for collectional crit-only multi criteria
        final var ewpd2 = save(new_(TgEntityWithPropertyDescriptor.class, "KEY2").setParent(ewpd1));
        save(new_(TgEntityWithPropertyDescriptor.class, "KEY3").setPropertyDescriptor(new PropertyDescriptor<>(TgPersistentEntityWithProperties.class, "integerProp")).setParent(ewpd1));
        save(new_(TgEntityWithPropertyDescriptor.class, "KEY4").setPropertyDescriptor(new PropertyDescriptor<>(TgPersistentEntityWithProperties.class, "bigDecimalProp")));
        save(new_(TgEntityWithPropertyDescriptor.class, "KEY5").setPropertyDescriptor(new PropertyDescriptor<>(TgPersistentEntityWithProperties.class, "entityProp")).setParent(ewpd2));
        save(new_(TgEntityWithPropertyDescriptor.class, "KEY6").setPropertyDescriptor(new PropertyDescriptor<>(TgPersistentEntityWithProperties.class, "stringProp")).setParent(ewpd2));
        save(new_(TgEntityWithPropertyDescriptor.class, "KEY7").setPropertyDescriptor(new PropertyDescriptor<>(TgPersistentEntityWithProperties.class, "booleanProp")).setParent(ewpd2));
        save(new_(TgEntityWithPropertyDescriptor.class, "KEY8").setPropertyDescriptor(new PropertyDescriptor<>(TgPersistentEntityWithProperties.class, "dateProp")));

        save(new_(TgEntityWithTimeZoneDates.class, "KEY1").setDatePropUtc(new Date(3609999)));
        save(new_(TgEntityWithTimeZoneDates.class, "KEY2").setDatePropUtc(new Date(1473057180015L)));
        save(new_(TgEntityWithTimeZoneDates.class, "KEY3").setDatePropUtc(new Date(1473057204015L)));
        save(new_(TgEntityWithTimeZoneDates.class, "KEY4").setDatePropUtc(new Date(1473057204000L)));
        save(new_(TgEntityWithTimeZoneDates.class, "KEY5").setDatePropUtc(new Date(1473057180000L)));

        save(new_(TgGeneratedEntity.class).setEntityKey("KEY1").setCreatedBy(su));

        final var filteredEntity = save(new_(TgPersistentEntityWithProperties.class, "FILTERED")
                                                .setIntegerProp(43)
                                                .setRequiredValidatedProp(30)
                                                .setDesc("Description for filtered entity.")
                                                .setStatus(co$(TgPersistentStatus.class).findByKey("DR")));
        final var savedDefaultEntity = save(defaultEnt.setEntityProp(filteredEntity));

        save(new_(TgCloseLeaveExample.class, "KEY1").setDesc("desc 1"));
        save(new_(TgCloseLeaveExample.class, "KEY2").setDesc("desc 2"));
        save(new_(TgCloseLeaveExample.class, "KEY3").setDesc("desc 3"));
        save(new_(TgCloseLeaveExample.class, "KEY4").setDesc("desc 4"));
        save(new_(TgCloseLeaveExample.class, "KEY5").setDesc("desc 5"));

        save(new_(TgCompoundEntity.class, "KEY1").setActive(true).setDesc("desc 1"));
        save(new_(TgCompoundEntity.class, "KEY2").setActive(true).setDesc("desc 2"));
        save(new_(TgCompoundEntity.class, "KEY3").setActive(true).setDesc("desc 3"));
        save(new_(TgCompoundEntity.class, "KEY4").setActive(true).setDesc("desc 4"));
        save(new_(TgCompoundEntity.class, "KEY5").setActive(true).setDesc("desc 5"));

        save(new_(TgCompoundEntity.class, "FILTERED1").setActive(true).setDesc("Description for filtered TgCompoundEntity entity."));
        final var filteredEntity2 = save(new_(TgCompoundEntity.class, "FILTERED2").setActive(true).setDesc("Description for TgCompoundEntity entity, for which TgCompoundEntityDetail is filtered."));
        save(new_composite(TgCompoundEntityChild.class, filteredEntity2, new Date()).setDesc("Description for filtered TgCompoundEntityChild entity."));

        final var test1 = save(new_(TgCompoundEntity.class, "1TEST").setActive(true).setDesc("1TEST (1TEST detail)"));
        save(new_composite(TgCompoundEntityChild.class, test1, new Date()).setDesc("Description for TgCompoundEntityChild entity of 1TEST."));

        final var compWith0 = save(new_composite(TgPersistentCompositeEntity.class, savedDefaultEntity, 0));

        save(new_(TgPersistentEntityWithProperties.class, "KEY12")
                     .setStringProp("ok")
                     .setIntegerProp(43)
                     .setEntityProp(savedDefaultEntity)
                     .setBigDecimalProp(new BigDecimal(23).setScale(5))
                     .setDateProp(new DateTime(960000L).toDate())
                     .setBooleanProp(true)
                     .setCompositeProp(compWith0)
                     .setDesc("Description for entity with key 12.")
                     .setRequiredValidatedProp(30));

        final var compWithEmptySecondKey = save(
                new_composite(TgPersistentCompositeEntity.class)
                        .setKey1(savedDefaultEntity)
                        .setDesc("Default composite entity description as a long text to demonstrate proper word wrapping as part of displaying the autocompleted values."));

        save(new_(TgPersistentEntityWithProperties.class, "KEY13")
                     .setStringProp("ok")
                     .setIntegerProp(43)
                     .setEntityProp(savedDefaultEntity)
                     .setBigDecimalProp(new BigDecimal(23).setScale(5))
                     .setDateProp(new DateTime(960000L).toDate())
                     .setBooleanProp(true)
                     .setCompositeProp(compWithEmptySecondKey)
                     .setDesc("Description for entity with key 12.")
                     .setRequiredValidatedProp(30));

        // The case when all components are present.
        {
            final var rsMajorComp = save(new_composite(TgRollingStockMajorComponent.class, "Locomotive", "Electrical Equipment"));
            final var minorComp = save(new_composite(TgMinorComponent.class, "Batteries", "Lithium-Ion"));
            final var rsMinorComp = save(new_composite(TgRollingStockMinorComponent.class, rsMajorComp, minorComp));
            save(ent11.setCompProp(rsMinorComp));
        }

        // The case when TgRollingStockMinorComponent is missing minorComponent.
        {
            final var rsMajorComp = save(new_composite(TgRollingStockMajorComponent.class, "Wagon", "Electrical Equipment"));
            final var rsMinorComp = save(new_composite(TgRollingStockMinorComponent.class, rsMajorComp, null));
            save(ent3.setCompProp(rsMinorComp));
        }

        // The case when TgRollingStockMajorComponent is missing majorComponent.
        {
            final var rsMajorComp = save(new_composite(TgRollingStockMajorComponent.class, "Wagon", null));
            final var minorComp = save(new_composite(TgMinorComponent.class, "Batteries", "Lead-Acid"));
            final var rsMinorComp = save(new_composite(TgRollingStockMinorComponent.class, rsMajorComp, minorComp));
            save(ent2.setCompProp(rsMinorComp));
        }

        save(new_(TgNote.class, "01").setText("hello"));
        save(new_(TgNote.class, "02").setText("hello world"));
        save(new_(TgNote.class, "03").setText("hello\nworld"));
        save(new_(TgNote.class, "04").setText(" \tone  \n\u00a0\n  two  \n\n\n three\n"));
        save(new_(TgNote.class, "05").setText("one & two\n1 < 2 && 4 > 0"));
        save(new_(TgNote.class, "06").setText(">>> <<< &&& &> &< >< <div> <&>"));

        final User _demo2 = co$(User.class).save(new_(User.class, "DEMO2").setBasedOnUser(su).setEmail("DEMO2@demoapp.com").setActive(true));
        final User demo2 = coUser.resetPasswd(_demo2, _demo2.getKey()).getKey();
        save(new_composite(UserAndRoleAssociation.class, demo2, admin));

        LOGGER.info("\tPopulating GraphQL data...");
        populateGraphQlData();

        save(new_(TgEntityWithRichTextProp.class, "RICH_TEXT_KEY1").setRichTextProp(fromHtml("<p>Rich text for entity with RICH TEXT KEY1</p>")).setDesc("rich text desc 1"));
        save(new_(TgEntityWithRichTextProp.class, "RICH_TEXT_KEY2").setRichTextProp(fromHtml("<p>Rich text for entity with RICH TEXT KEY2</p>")).setDesc("rich text desc 2"));
        save(new_(TgEntityWithRichTextProp.class, "RICH_TEXT_KEY3").setRichTextProp(fromHtml("<p>Rich text for entity with RICH TEXT KEY3</p>")).setDesc("rich text desc 3"));
        save(new_(TgEntityWithRichTextProp.class, "RICH_TEXT_KEY4").setRichTextProp(fromHtml("<p>Rich text for entity with RICH TEXT KEY4</p>")).setDesc("rich text desc 4"));
        save(new_(TgEntityWithRichTextProp.class, "RICH_TEXT_KEY5").setRichTextProp(fromHtml("<p>Rich text for entity with RICH TEXT KEY5</p>")).setDesc("rich text desc 5"));
        save(new_(TgEntityWithRichTextProp.class, "RICH_TEXT_KEY6").setRichTextProp(fromHtml("hello world")).setDesc("rich text desc 6")); // deliberate value without paragraph tags to induce Toast UI transformation; used to test SAVE disablement

        try {
            final ISecurityTokenProvider provider = config.getInstance(ISecurityTokenProvider.class); //  IDomainDrivenTestCaseConfiguration.hbc.getProperty("tokens.path"), IDomainDrivenTestCaseConfiguration.hbc.getProperty("tokens.package")
            final SortedSet<SecurityTokenNode> topNodes = provider.getTopLevelSecurityTokenNodes();
            final SecurityTokenAssociator predicate = new SecurityTokenAssociator(admin, co$(SecurityRoleAssociation.class));
            final ISearchAlgorithm<Class<? extends ISecurityToken>, SecurityTokenNode> alg = new BreadthFirstSearch<>();
            for (final SecurityTokenNode securityNode : topNodes) {
                alg.search(securityNode, predicate);
            }

            LOGGER.info("Completed database creation and population.");
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private void populateGraphQlData() {
        final TgFuelType unleadedFuelType = save(new_(TgFuelType.class, "U", "Unleaded"));
        final TgFuelType petrolFuelType = save(new_(TgFuelType.class, "P", "Petrol"));

        final TgOrgUnit1 orgUnit1 = save(new_(TgOrgUnit1.class, "orgunit1", "desc orgunit1"));
        final TgOrgUnit2 orgUnit2 = save(new_composite(TgOrgUnit2.class, orgUnit1, "orgunit2"));
        final TgOrgUnit3 orgUnit3 = save(new_composite(TgOrgUnit3.class, orgUnit2, "orgunit3"));
        final TgOrgUnit4 orgUnit4 = save(new_composite(TgOrgUnit4.class, orgUnit3, "orgunit4"));
        final TgOrgUnit5 orgUnit5 = save(new_composite(TgOrgUnit5.class, orgUnit4, "orgunit5").setFuelType(petrolFuelType));

        final TgVehicleMake merc = save(new_(TgVehicleMake.class, "MERC", "Mercedes"));
        final TgVehicleMake audi = save(new_(TgVehicleMake.class, "AUDI", "Audi"));
        final TgVehicleMake bmw = save(new_(TgVehicleMake.class, "BMW", "BMW"));
        save(new_(TgVehicleMake.class, "SUBARO", "Subaro"));

        final TgVehicleModel m316 = save(new_(TgVehicleModel.class, "316", "316").setMake(merc));
        save(new_(TgVehicleModel.class, "317", "317").setMake(audi));
        final TgVehicleModel m318 = save(new_(TgVehicleModel.class, "318", "318").setMake(audi));
        save(new_(TgVehicleModel.class, "319", "319").setMake(bmw));
        save(new_(TgVehicleModel.class, "320", "320").setMake(bmw));
        save(new_(TgVehicleModel.class, "321", "321").setMake(bmw));
        save(new_(TgVehicleModel.class, "322", "322").setMake(bmw));

        final TgVehicle car2 = save(new_(TgVehicle.class, "CAR2", "CAR2 DESC").
                setInitDate(date("2007-01-01 00:00:00")).
                setModel(m316).
                setPrice(new Money("200")).
                setPurchasePrice(new Money("100")).
                setActive(false).
                setLeased(true).
                setLastMeterReading(new BigDecimal("105")).
                setStation(orgUnit5));
        save(new_(TgVehicle.class, "CAR1", "CAR1 DESC").
                setInitDate(date("2001-01-01 00:00:00")).
                setModel(m318).setPrice(new Money("20")).
                setPurchasePrice(new Money("10")).
                setActive(true).
                setLeased(false).
                setReplacedBy(car2));

        save(new_composite(TgFuelUsage.class, car2, date("2006-02-09 00:00:00")).setQty(new BigDecimal("100")).setFuelType(unleadedFuelType));
        save(new_composite(TgFuelUsage.class, car2, date("2008-02-10 00:00:00")).setQty(new BigDecimal("120")).setFuelType(petrolFuelType));
    }

    /**
     * Creates 30-ty entities to be able to demonstrate (with populated statuses).
     */
    private void createDemoDomain(final TgPersistentEntityWithProperties ent1, final TgPersistentEntityWithProperties ent2, final TgPersistentCompositeEntity compEnt1) {
        final TgPersistentStatus dr = save(new_(TgPersistentStatus.class, "DR").setDesc("Defect Radio"));
        final TgPersistentStatus is = save(new_(TgPersistentStatus.class, "IS").setDesc("In Service"));
        final TgPersistentStatus ir = save(new_(TgPersistentStatus.class, "IR").setDesc("In Repair"));
        final TgPersistentStatus on = save(new_(TgPersistentStatus.class, "ON").setDesc("On Road Defect Station"));
        final TgPersistentStatus sr = save(new_(TgPersistentStatus.class, "SR").setDesc("Defect Smash Repair"));

        for (int i = 0; i < 30; i++) {
            save(new_(TgPersistentEntityWithProperties.class, "DEMO" + convert(i))
                    .setStringProp(random("poor", "average", "good", "great", "superb", "excelent", "classic").toUpperCase())
                    .setIntegerProp(random(43, 67, 24, 35, 18, 99, 23))
                    .setEntityProp(random(ent1, null, ent2))
                    .setBigDecimalProp(random(new BigDecimal(23).setScale(5), new BigDecimal(4).setScale(5), new BigDecimal(99).setScale(5)))
                    .setDateProp(new DateTime(random(1000000000000L, 1100000000000L)).toDate())
                    .setBooleanProp(random(true, false))
                    .setCompositeProp(random(compEnt1, null))
                    .setDesc("Description for demo entity with key " + ("DEMO" + convert(i)) + ".")
                    .setRequiredValidatedProp(random(30, 56, 82))
                    .setStatus(random(dr, is, ir, on, sr)));
        }
    }

    private <T> T random(final T... values) {
        return values[(int) Math.round(Math.random() * (values.length - 1))];
    }

    private String convert(final int i) {
        return i < 10 ? ("0" + i) : "" + i;
    }

    @Override
    protected List<Class<? extends AbstractEntity<?>>> domainEntityTypes() {
        return applicationDomainProvider.entityTypes();
    }

}
