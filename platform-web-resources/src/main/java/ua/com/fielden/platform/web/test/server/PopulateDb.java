package ua.com.fielden.platform.web.test.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SortedSet;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import fielden.config.ApplicationDomain;
import ua.com.fielden.platform.algorithm.search.ISearchAlgorithm;
import ua.com.fielden.platform.algorithm.search.bfs.BreadthFirstSearch;
import ua.com.fielden.platform.basic.config.IApplicationSettings;
import ua.com.fielden.platform.devdb_support.DomainDrivenDataPopulation;
import ua.com.fielden.platform.devdb_support.SecurityTokenAssociator;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.sample.domain.ITgPerson;
import ua.com.fielden.platform.sample.domain.TgCollectionalSerialisationChild;
import ua.com.fielden.platform.sample.domain.TgCollectionalSerialisationParent;
import ua.com.fielden.platform.sample.domain.TgCoordinate;
import ua.com.fielden.platform.sample.domain.TgEntityForColourMaster;
import ua.com.fielden.platform.sample.domain.TgEntityWithPropertyDependency;
import ua.com.fielden.platform.sample.domain.TgEntityWithPropertyDescriptor;
import ua.com.fielden.platform.sample.domain.TgEntityWithTimeZoneDates;
import ua.com.fielden.platform.sample.domain.TgFetchProviderTestEntity;
import ua.com.fielden.platform.sample.domain.TgGeneratedEntity;
import ua.com.fielden.platform.sample.domain.TgMachine;
import ua.com.fielden.platform.sample.domain.TgMessage;
import ua.com.fielden.platform.sample.domain.TgOrgUnit;
import ua.com.fielden.platform.sample.domain.TgPersistentCompositeEntity;
import ua.com.fielden.platform.sample.domain.TgPersistentEntityWithProperties;
import ua.com.fielden.platform.sample.domain.TgPersistentStatus;
import ua.com.fielden.platform.sample.domain.TgPerson;
import ua.com.fielden.platform.sample.domain.TgPolygon;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.provider.SecurityTokenNode;
import ua.com.fielden.platform.security.provider.SecurityTokenProvider;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.SecurityRoleAssociation;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;
import ua.com.fielden.platform.security.user.UserRole;
import ua.com.fielden.platform.test.IDomainDrivenTestCaseConfiguration;
import ua.com.fielden.platform.types.Colour;
import ua.com.fielden.platform.types.Hyperlink;
import ua.com.fielden.platform.types.Money;

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
    private final Logger logger = Logger.getLogger(getClass());
    private final ApplicationDomain applicationDomainProvider = new ApplicationDomain();

    private PopulateDb(final IDomainDrivenTestCaseConfiguration config, final Properties props) {
        super(config, props);
    }

    public static void main(final String[] args) throws Exception {
        final String configFileName = args.length == 1 ? args[0] : "src/main/resources/application.properties";
        final FileInputStream in = new FileInputStream(configFileName);
        final Properties props = new Properties();
        props.load(in);
        in.close();

        // override/set some of the Hibernate properties in order to ensure (re-)creation of the target database
        props.put("hibernate.show_sql", "false");
        props.put("hibernate.format_sql", "true");
        props.put("hibernate.hbm2ddl.auto", "create");

        final IDomainDrivenTestCaseConfiguration config = new DataPopulationConfig(props);

        final PopulateDb popDb = new PopulateDb(config, props);
        popDb.createAndPopulate();
    }

    @Override
    protected void populateDomain() {
        logger.info("Creating and populating the development database...");

        // VIRTUAL_USER is a virtual user (cannot be persisted) and has full access to all security tokens
        // It should always be used as the current user for data population activities
        final IUser coUser = co(User.class);
        final User u = new_(User.class, User.system_users.VIRTUAL_USER.name()).setBase(true);
        final IUserProvider up = getInstance(IUserProvider.class);
        up.setUser(u);

        final User _su = coUser.save(new_(User.class, User.system_users.SU.name()).setBase(true).setEmail("SU@demoapp.com").setActive(true));
        final User su = coUser.resetPasswd(_su, _su.getKey());
        final User _demo = co(User.class).save(new_(User.class, "DEMO").setBasedOnUser(su).setEmail("DEMO@demoapp.com").setActive(true));
        final User demo = coUser.resetPasswd(_demo, _demo.getKey());

        final ITgPerson aoPerson = (ITgPerson) co(TgPerson.class);
        aoPerson.populateNew("Super", "User", "Super User", User.system_users.SU.name());
        aoPerson.populateNew("Demo", "User", "Demo User", "DEMO");

        final UserRole admin = save(new_(UserRole.class, "ADMINISTRATION", "A role, which has a full access to the the system and should be used only for users who need administrative previligies.").setActive(true));
        save(new_composite(UserAndRoleAssociation.class, su, admin));

        logger.info("\tPopulate testing entities...");
        final TgPersistentEntityWithProperties ent1 = save(new_(TgPersistentEntityWithProperties.class, "KEY1").setIntegerProp(43).setRequiredValidatedProp(30)
                .setDesc("Description for entity with key 1. This is a relatively long description to demonstrate how well does is behave during value autocompletion."));
        final TgPersistentEntityWithProperties ent2 = save(new_(TgPersistentEntityWithProperties.class, "KEY2").setIntegerProp(14).setDesc("Description for entity with key 2.").setRequiredValidatedProp(30));
        final TgPersistentEntityWithProperties ent3 = save(new_(TgPersistentEntityWithProperties.class, "KEY3").setIntegerProp(15).setDesc("Description for entity with key 3.").setRequiredValidatedProp(30));
        save(ent2.setEntityProp(ent3));
        save(new_(TgPersistentEntityWithProperties.class, "KEY4").setIntegerProp(63).setMoneyProp(new Money("23.0", Currency.getInstance("USD"))).setDesc("Description for entity with key 4.").setRequiredValidatedProp(30));
        save(new_(TgPersistentEntityWithProperties.class, "KEY5").setBigDecimalProp(new BigDecimal(23.0)).setDesc("Description for entity with key 5.").setRequiredValidatedProp(30));
        save(new_(TgPersistentEntityWithProperties.class, "KEY6").setIntegerProp(61).setStringProp("ok").setDesc("Description for entity with key 6.").setRequiredValidatedProp(30));
        save(new_(TgPersistentEntityWithProperties.class, "KEY7").setBooleanProp(true).setDesc("Description for entity with key 7.").setRequiredValidatedProp(30));
        save(new_(TgPersistentEntityWithProperties.class, "KEY8").setDateProp(new DateTime(3609999L).toDate()).setDesc("Description for entity with key 8.").setRequiredValidatedProp(30));
        final TgPersistentEntityWithProperties de = new_(TgPersistentEntityWithProperties.class, "DEFAULT_KEY")
                // please note that proxies are not created for 'null' entity properties and regular (date, string..) properties!
                // .setProducerInitProp(ent1)
                .setIntegerProp(7).setMoneyProp(new Money("7.0", Currency.getInstance("USD"))).setBigDecimalProp(new BigDecimal("7.7"))
                .setStringProp("ok_def").setBooleanProp(true).setDateProp(new DateTime(7777L).toDate()).setRequiredValidatedProp(30)
                .setColourProp(Colour.RED).setHyperlinkProp(new Hyperlink("https://www.fielden.com.au"));
        de.setDesc("Default entity description");
        final TgPersistentEntityWithProperties defaultEnt = save(de);
        final TgPersistentEntityWithProperties staleEnt1 = save(new_(TgPersistentEntityWithProperties.class, "KEY10").setConflictingProp("initial").setNonConflictingProp("initial").setDesc("Description for entity with key 10.").setRequiredValidatedProp(30));
        save(staleEnt1.setConflictingProp("persistently modified").setRequiredValidatedProp(30));

        final TgPersistentCompositeEntity ce = new_composite(TgPersistentCompositeEntity.class, defaultEnt, 10);
        ce.setDesc("Default composite entity description as a long text to demonstrate proper word wrapping as part of displaying the autocompleted values.");
        final TgPersistentCompositeEntity compositeEnt1 = save(ce);

        final TgPersistentEntityWithProperties exampleEntToBeSaved = new_(TgPersistentEntityWithProperties.class, "KEY11").setStringProp("ok").setIntegerProp(43).setEntityProp(defaultEnt).setBigDecimalProp(new BigDecimal(23).setScale(5)).setDateProp(new DateTime(960000L).toDate()).setBooleanProp(true).setCompositeProp(compositeEnt1).setDesc("Description for entity with key 11.").setRequiredValidatedProp(30);
        final TgPersistentEntityWithProperties exampleEnt1 = save(exampleEntToBeSaved);

        save(new_(TgFetchProviderTestEntity.class, "FETCH1").setProperty(exampleEnt1).setAdditionalProperty(su));

        logger.info("\tPopulate demo entities...");
        createDemoDomain(ent1, ent3, compositeEnt1);

        final TgEntityForColourMaster colourEntity = new_(TgEntityForColourMaster.class, "KEY12").setStringProp("ok").setBooleanProp(true).setColourProp(new Colour("aaacdc"));
        save(colourEntity);
        save(new_(TgEntityWithPropertyDependency.class, "KEY1").setProperty("IS"));
        save(new_composite(UserAndRoleAssociation.class, demo, admin));

        final UserRole stationMgr = save(new_(UserRole.class, "STATION_MGR", "A role, which has access to the the station managing functionality."));
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

        final TgCollectionalSerialisationParent csp1 = save(new_(TgCollectionalSerialisationParent.class, "CSP1").setDesc("desc1"));
        save(new_composite(TgCollectionalSerialisationChild.class, csp1, "1").setDesc("desc1"));

        save(new_(TgEntityWithPropertyDescriptor.class, "KEY1").setPropertyDescriptor(new PropertyDescriptor<>(TgPersistentEntityWithProperties.class, "integerProp")));
        save(new_(TgEntityWithPropertyDescriptor.class, "KEY2"));
        save(new_(TgEntityWithPropertyDescriptor.class, "KEY3").setPropertyDescriptor(new PropertyDescriptor<>(TgPersistentEntityWithProperties.class, "integerProp")));
        save(new_(TgEntityWithPropertyDescriptor.class, "KEY4").setPropertyDescriptor(new PropertyDescriptor<>(TgPersistentEntityWithProperties.class, "bigDecimalProp")));
        save(new_(TgEntityWithPropertyDescriptor.class, "KEY5").setPropertyDescriptor(new PropertyDescriptor<>(TgPersistentEntityWithProperties.class, "entityProp")));
        save(new_(TgEntityWithPropertyDescriptor.class, "KEY6").setPropertyDescriptor(new PropertyDescriptor<>(TgPersistentEntityWithProperties.class, "stringProp")));
        save(new_(TgEntityWithPropertyDescriptor.class, "KEY7").setPropertyDescriptor(new PropertyDescriptor<>(TgPersistentEntityWithProperties.class, "booleanProp")));
        save(new_(TgEntityWithPropertyDescriptor.class, "KEY8").setPropertyDescriptor(new PropertyDescriptor<>(TgPersistentEntityWithProperties.class, "dateProp")));

        save(new_(TgEntityWithTimeZoneDates.class, "KEY1").setDatePropUtc(new Date(3609999)));
        save(new_(TgEntityWithTimeZoneDates.class, "KEY2").setDatePropUtc(new Date(1473057180015L)));
        save(new_(TgEntityWithTimeZoneDates.class, "KEY3").setDatePropUtc(new Date(1473057204015L)));
        save(new_(TgEntityWithTimeZoneDates.class, "KEY4").setDatePropUtc(new Date(1473057204000L)));
        save(new_(TgEntityWithTimeZoneDates.class, "KEY5").setDatePropUtc(new Date(1473057180000L)));
        
        save(new_(TgGeneratedEntity.class).setEntityKey("KEY1").setCreatedBy(su));
        
        save(new_(TgPersistentEntityWithProperties.class, "FILTERED").setIntegerProp(43).setRequiredValidatedProp(30).setDesc("Description for filtered entity.").setStatus(co(TgPersistentStatus.class).findByKey("DR")));
        
        logger.info("\tPopulating messages...");
        final Map<String, TgMachine> machines = new HashMap<>();
        try {
            final ClassLoader classLoader = getClass().getClassLoader();
            final File file = new File(classLoader.getResource("gis/messageEntities.js").getFile());
            final InputStream stream = new FileInputStream(file);
            final ObjectMapper objectMapper = new ObjectMapper();
            final ArrayList oldMessageEntities = objectMapper.readValue(stream, ArrayList.class);

            for (final Object oldMessageEntity: oldMessageEntities) {
                final Map<String, Object> map = (Map<String, Object>) oldMessageEntity;
                final Map<String, Object> messageProps = ((Map<String, Object>) map.get("properties"));
                final String machineKey = (String) ((Map<String, Object>) ((Map<String, Object>) messageProps.get("machine")).get("properties")).get("key"); 
                TgMachine found = machines.get(machineKey);
                if (found == null) {
                    final TgMachine newMachine = new_(TgMachine.class, machineKey);
                    newMachine.setDesc(machineKey + " desc");
                    found = save(newMachine);
                    machines.put(machineKey, found);
                }
                save(new_composite(TgMessage.class, found, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS").parseDateTime(((String) messageProps.get("gpsTime"))).toDate())
                        .setX(BigDecimal.valueOf((double) messageProps.get("x")))
                        .setY(BigDecimal.valueOf((double) messageProps.get("y")))
                        .setVectorAngle((int) messageProps.get("vectorAngle"))
                        .setVectorSpeed((int) messageProps.get("vectorSpeed"))
                        // .setAltitude(223)
                        // .setVisibleSattelites(2)
                        .setDin1((boolean) messageProps.get("din1"))
                        .setGpsPower((boolean) messageProps.get("gpsPower"))
                        .setTravelledDistance(BigDecimal.valueOf((double) messageProps.get("travelledDistance")))
                );
            }
        } catch (final IOException ex) {
            throw new IllegalStateException(ex);
        }
        
        logger.info("\tPopulating machines...");
        try {
            final ClassLoader classLoader = getClass().getClassLoader();
            final File file = new File(classLoader.getResource("gis/realtimeMonitorEntities.js").getFile());
            final InputStream stream = new FileInputStream(file);
            final ObjectMapper objectMapper = new ObjectMapper();
            final ArrayList oldMachineEntities = objectMapper.readValue(stream, ArrayList.class);

            final Map<String, TgOrgUnit> orgUnits = new HashMap<>();
            for (final Object oldMachineEntity: oldMachineEntities) {
                final Map<String, Object> map = (Map<String, Object>) oldMachineEntity;
                final Map<String, Object> machineProps = ((Map<String, Object>) map.get("properties"));
                final String machineKey = (String) machineProps.get("key"); 
                TgMachine found = machines.get(machineKey);
                if (found == null) {
                    final TgMachine newMachine = new_(TgMachine.class, machineKey);
                    newMachine.setDesc((String) machineProps.get("desc"));
                    final Object orgUnitObject = machineProps.get("orgUnit");
                    if (orgUnitObject != null) {
                        final String orgUnitKey = (String) ((Map<String, Object>) ((Map<String, Object>) orgUnitObject).get("properties")).get("key");
                        TgOrgUnit foundOrgUnit = orgUnits.get(orgUnitKey);
                        if (foundOrgUnit == null) {
                            final TgOrgUnit newOrgUnit = new_(TgOrgUnit.class, orgUnitKey);
                            newOrgUnit.setDesc((String) ((Map<String, Object>) ((Map<String, Object>) machineProps.get("orgUnit")).get("properties")).get("desc"));
                            foundOrgUnit = save(newOrgUnit);
                            orgUnits.put(orgUnitKey, foundOrgUnit);
                        }
                        newMachine.setOrgUnit(foundOrgUnit);
                    }
                    found = save(newMachine);
                    machines.put(machineKey, found);
                }
                final Object lastMessageObject = machineProps.get("lastMessage");
                if (lastMessageObject != null) {
                    final Map<String, Object> lastMessageProps = (Map<String, Object>) ((Map<String, Object>) lastMessageObject).get("properties");
                    
                    save(new_composite(TgMessage.class, found, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS").parseDateTime(((String) lastMessageProps.get("gpsTime"))).toDate())
                            .setX(BigDecimal.valueOf((double) lastMessageProps.get("x")))
                            .setY(BigDecimal.valueOf((double) lastMessageProps.get("y")))
                            .setVectorAngle((int) lastMessageProps.get("vectorAngle"))
                            .setVectorSpeed((int) lastMessageProps.get("vectorSpeed"))
                            // .setAltitude(223)
                            // .setVisibleSattelites(2)
                            .setDin1((boolean) lastMessageProps.get("din1"))
                            .setGpsPower((boolean) lastMessageProps.get("gpsPower"))
                            .setTravelledDistance(BigDecimal.valueOf(15.5)) // lastMessageProps.get("travelledDistance")
                    );
                }
            }
        } catch (final IOException ex) {
            throw new IllegalStateException(ex);
        }
        
        logger.info("\tPopulating geozones...");
        try {
            final ClassLoader classLoader = getClass().getClassLoader();
            final File file = new File(classLoader.getResource("gis/polygonEntities.js").getFile());
            final InputStream stream = new FileInputStream(file);
            final ObjectMapper objectMapper = new ObjectMapper();
            final ArrayList oldPolygonEntities = objectMapper.readValue(stream, ArrayList.class);
            
            final Map<String, TgPolygon> polygons = new HashMap<>();
            for (final Object oldPolygonEntity: oldPolygonEntities) {
                final Map<String, Object> map = (Map<String, Object>) oldPolygonEntity;
                final Map<String, Object> polygonProps = ((Map<String, Object>) map.get("properties"));
                final String polygonKey = (String) polygonProps.get("key"); 
                TgPolygon found = polygons.get(polygonKey);
                if (found == null) {
                    final TgPolygon newPolygon = new_(TgPolygon.class, polygonKey);
                    newPolygon.setDesc((String) polygonProps.get("desc"));
                    found = save(newPolygon);
                    polygons.put(polygonKey, found);
                }
                
                final ArrayList<Object> coordinates = (ArrayList<Object>) polygonProps.get("coordinates");
                for (final Object coord: coordinates) {
                    final Map<String, Object> coordProps = ((Map<String, Object>) ((Map<String, Object>) coord).get("properties"));
                    save(new_composite(TgCoordinate.class, found, (Integer) coordProps.get("order"))
                            .setLongitude(BigDecimal.valueOf((double) coordProps.get("longitude")))
                            .setLatitude(BigDecimal.valueOf((double) coordProps.get("latitude")))
                    );
                }
            }
        } catch (final IOException ex) {
            throw new IllegalStateException(ex);
        }

        try {
            final IApplicationSettings settings = config.getInstance(IApplicationSettings.class);
            final SecurityTokenProvider provider = new SecurityTokenProvider(settings.pathToSecurityTokens(), settings.securityTokensPackageName()); //  IDomainDrivenTestCaseConfiguration.hbc.getProperty("tokens.path"), IDomainDrivenTestCaseConfiguration.hbc.getProperty("tokens.package")
            final SortedSet<SecurityTokenNode> topNodes = provider.getTopLevelSecurityTokenNodes();
            final SecurityTokenAssociator predicate = new SecurityTokenAssociator(admin, co(SecurityRoleAssociation.class));
            final ISearchAlgorithm<Class<? extends ISecurityToken>, SecurityTokenNode> alg = new BreadthFirstSearch<Class<? extends ISecurityToken>, SecurityTokenNode>();
            for (final SecurityTokenNode securityNode : topNodes) {
                alg.search(securityNode, predicate);
            }

            logger.info("Completed database creation and population.");
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Creates 30-ty entities to be able to demonstrate (with populated statuses).
     */
    private void createDemoDomain(final TgPersistentEntityWithProperties ent1, final TgPersistentEntityWithProperties ent2, final TgPersistentCompositeEntity compEnt1) {
        final TgPersistentStatus dr = (TgPersistentStatus) save(new_(TgPersistentStatus.class, "DR").setDesc("Defect Radio"));
        final TgPersistentStatus is = (TgPersistentStatus) save(new_(TgPersistentStatus.class, "IS").setDesc("In Service"));
        final TgPersistentStatus ir = (TgPersistentStatus) save(new_(TgPersistentStatus.class, "IR").setDesc("In Repair"));
        final TgPersistentStatus on = (TgPersistentStatus) save(new_(TgPersistentStatus.class, "ON").setDesc("On Road Defect Station"));
        final TgPersistentStatus sr = (TgPersistentStatus) save(new_(TgPersistentStatus.class, "SR").setDesc("Defect Smash Repair"));

        for (int i = 0; i < 30; i++) {
            save(new_(TgPersistentEntityWithProperties.class, "DEMO" + convert(i))
                    .setStringProp(random("poor", "average", "good", "great", "superb", "excelent", "classic"))
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
