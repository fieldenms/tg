package ua.com.fielden.platform.web.test.server;

import java.io.FileInputStream;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Properties;
import java.util.SortedSet;

import org.joda.time.DateTime;

import ua.com.fielden.platform.algorithm.search.ISearchAlgorithm;
import ua.com.fielden.platform.algorithm.search.bfs.BreadthFirstSearch;
import ua.com.fielden.platform.basic.config.IApplicationSettings;
import ua.com.fielden.platform.devdb_support.DomainDrivenDataPopulation;
import ua.com.fielden.platform.devdb_support.SecurityTokenAssociator;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.sample.domain.ITgPerson;
import ua.com.fielden.platform.sample.domain.TgCollectionalSerialisationChild;
import ua.com.fielden.platform.sample.domain.TgCollectionalSerialisationParent;
import ua.com.fielden.platform.sample.domain.TgEntityForColourMaster;
import ua.com.fielden.platform.sample.domain.TgEntityWithPropertyDependency;
import ua.com.fielden.platform.sample.domain.TgFetchProviderTestEntity;
import ua.com.fielden.platform.sample.domain.TgPersistentCompositeEntity;
import ua.com.fielden.platform.sample.domain.TgPersistentEntityWithProperties;
import ua.com.fielden.platform.sample.domain.TgPersistentStatus;
import ua.com.fielden.platform.sample.domain.TgPerson;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.provider.SecurityTokenNode;
import ua.com.fielden.platform.security.provider.SecurityTokenProvider;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.SecurityRoleAssociation;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;
import ua.com.fielden.platform.security.user.UserRole;
import ua.com.fielden.platform.test.IDomainDrivenTestCaseConfiguration;
import ua.com.fielden.platform.types.Colour;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.ui.config.MainMenu;
import ua.com.fielden.platform.ui.config.controller.mixin.MainMenuStructureFactory;

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

    private final TgTestApplicationDomain applicationDomainProvider = new TgTestApplicationDomain();

    private PopulateDb(final IDomainDrivenTestCaseConfiguration config) {
        super(config);
    }

    public static void main(final String[] args) throws Exception {
        final String configFileName = args.length == 1 ? args[0] : "src/main/resources/application.properties";
        final FileInputStream in = new FileInputStream(configFileName);
        final Properties props = IDomainDrivenTestCaseConfiguration.hbc;
        props.load(in);
        in.close();

        // override/set some of the Hibernate properties in order to ensure (re-)creation of the target database
        props.put("hibernate.show_sql", "false");
        props.put("hibernate.format_sql", "true");
        props.put("hibernate.hbm2ddl.auto", "create");

        final IDomainDrivenTestCaseConfiguration config = new DataPopulationConfig();

        final PopulateDb popDb = new PopulateDb(config);
        popDb.createAndPopulate();
    }

    @Override
    protected void populateDomain() {
        System.out.println("Creating and populating the development database...");
        final IUser coUser = ao(User.class);
        final User _su = coUser.save(new_(User.class, User.system_users.SU.name()).setBase(true));
        final User su = coUser.resetPasswd(_su, _su.getKey());
        final User _demo = ao(User.class).save(new_(User.class, "DEMO").setBasedOnUser(su));
        final User demo = coUser.resetPasswd(_demo, _demo.getKey());
        
        final ITgPerson aoPerson = (ITgPerson) ao(TgPerson.class);
        aoPerson.populateNew("Super", "User", "Super User", User.system_users.SU.name());
        aoPerson.populateNew("Demo", "User", "Demo User", "DEMO");

        final UserRole admin = save(new_(UserRole.class, "ADMINISTRATION", "A role, which has a full access to the the system and should be used only for users who need administrative previligies."));
        System.out.println("admin.getId() == " + admin.getId());

        save(new_composite(UserAndRoleAssociation.class, su, admin));

        // populate testing entities
        final TgPersistentEntityWithProperties ent1 = save(new_(TgPersistentEntityWithProperties.class, "KEY1").setIntegerProp(43).setRequiredValidatedProp(30)
                .setDesc("Description for entity with key 1. This is a relatively long description to demonstrate how well does is behave during value autocompletion."));
        System.out.println("ent1.getId() == " + ent1.getId());
        final TgPersistentEntityWithProperties ent2 = save(new_(TgPersistentEntityWithProperties.class, "KEY2").setIntegerProp(14).setDesc("Description for entity with key 2.").setRequiredValidatedProp(30));
        System.out.println("ent2.getId() == " + ent2.getId());
        final TgPersistentEntityWithProperties ent3 = save(new_(TgPersistentEntityWithProperties.class, "KEY3").setIntegerProp(15).setDesc("Description for entity with key 3.").setRequiredValidatedProp(30));
        System.out.println("ent3.getId() == " + ent3.getId());
        save(ent2.setEntityProp(ent3));

        final TgPersistentEntityWithProperties moneyEnt1 = save(new_(TgPersistentEntityWithProperties.class, "KEY4").setIntegerProp(63).setMoneyProp(new Money("23.0", Currency.getInstance("USD"))).setDesc("Description for entity with key 4.").setRequiredValidatedProp(30));
        System.out.println("moneyEnt1.getId() == " + moneyEnt1.getId());

        final TgPersistentEntityWithProperties bigDecimalEnt1 = save(new_(TgPersistentEntityWithProperties.class, "KEY5").setBigDecimalProp(new BigDecimal(23.0)).setDesc("Description for entity with key 5.").setRequiredValidatedProp(30));
        System.out.println("bigDecimalEnt1.getId() == " + bigDecimalEnt1.getId());

        final TgPersistentEntityWithProperties stringEnt1 = save(new_(TgPersistentEntityWithProperties.class, "KEY6").setIntegerProp(61).setStringProp("ok").setDesc("Description for entity with key 6.").setRequiredValidatedProp(30));
        System.out.println("stringEnt1.getId() == " + stringEnt1.getId());

        final TgPersistentEntityWithProperties booleanEnt1 = save(new_(TgPersistentEntityWithProperties.class, "KEY7").setBooleanProp(true).setDesc("Description for entity with key 7.").setRequiredValidatedProp(30));
        System.out.println("booleanEnt1.getId() == " + booleanEnt1.getId());

        final TgPersistentEntityWithProperties dateEnt1 = save(new_(TgPersistentEntityWithProperties.class, "KEY8").setDateProp(new DateTime(9999L).toDate()).setDesc("Description for entity with key 8.").setRequiredValidatedProp(30));
        System.out.println("dateEnt1.getId() == " + dateEnt1.getId());

        final TgPersistentEntityWithProperties de = new_(TgPersistentEntityWithProperties.class, "DEFAULT_KEY")
                // please note that proxies are not created for 'null' entity properties and regular (date, string..) properties!
                // .setProducerInitProp(ent1)
                .setIntegerProp(7).setMoneyProp(new Money("7.0", Currency.getInstance("USD"))).setBigDecimalProp(new BigDecimal(7.7))
                .setStringProp("ok_def").setBooleanProp(true).setDateProp(new DateTime(7777L).toDate()).setRequiredValidatedProp(30);
        de.setDesc("Default entity description");
        final TgPersistentEntityWithProperties defaultEnt = save(de);
        System.out.println("defaultEnt.getId() == " + defaultEnt.getId());

        final TgPersistentEntityWithProperties staleEnt1 = save(new_(TgPersistentEntityWithProperties.class, "KEY10").setConflictingProp("initial").setNonConflictingProp("initial").setDesc("Description for entity with key 10.").setRequiredValidatedProp(30));
        System.out.println("staleEnt1.getId() == " + staleEnt1.getId());
        System.out.println("staleEnt1.getVersion() == " + staleEnt1.getVersion());

        final TgPersistentEntityWithProperties staleEnt1New = save(staleEnt1.setConflictingProp("persistently modified").setRequiredValidatedProp(30));
        System.out.println("staleEnt1New.getVersion() == " + staleEnt1New.getVersion());

        final TgPersistentCompositeEntity ce = new_composite(TgPersistentCompositeEntity.class, defaultEnt, 10);
        ce.setDesc("Default composite entity description as a long text to demonstrate proper word wrapping as part of displaying the autocompleted values.");
        final TgPersistentCompositeEntity compositeEnt1 = save(ce);
        System.out.println("compositeEnt1.getId() == " + compositeEnt1.getId());

        final TgPersistentEntityWithProperties exampleEntToBeSaved = new_(TgPersistentEntityWithProperties.class, "KEY11").setStringProp("ok").setIntegerProp(43).setEntityProp(defaultEnt).setBigDecimalProp(new BigDecimal(23).setScale(5)).setDateProp(new DateTime(960000L).toDate()).setBooleanProp(true).setCompositeProp(compositeEnt1).setDesc("Description for entity with key 11.").setRequiredValidatedProp(30);
        System.out.println("exampleEntToBeSaved.getBigDecimalProp().scale() == " + exampleEntToBeSaved.getBigDecimalProp().scale());
        final TgPersistentEntityWithProperties exampleEnt1 = save(exampleEntToBeSaved);
        System.out.println("exampleEnt1.getBigDecimalProp().scale() == " + exampleEnt1.getBigDecimalProp().scale());
        System.out.println("exampleEnt1.getId() == " + exampleEnt1.getId());

        save(new_(TgFetchProviderTestEntity.class, "FETCH1").setProperty(exampleEnt1).setAdditionalProperty(su));

        createDemoDomain(ent1, ent3, compositeEnt1);
        //
        //        final TgPersistentEntityWithProperties ent1WithCompositeProp = save(new_(TgPersistentEntityWithProperties.class, "KEY12").setCompositeProp(compositeEnt1));
        //        System.out.println("ent1WithCompositeProp.getId() == " + ent1WithCompositeProp.getId());

        final TgEntityForColourMaster colourEntity = new_(TgEntityForColourMaster.class, "KEY12").setStringProp("ok").setBooleanProp(true).setColourProp(new Colour("aaacdc"));
        final TgEntityForColourMaster defaultColourEnt = save(colourEntity);
        System.out.println("defaultColourEnt.getId() == " + defaultColourEnt.getId());

        final TgEntityWithPropertyDependency entWithPropDependency = save(new_(TgEntityWithPropertyDependency.class, "KEY1").setProperty("IS"));
        System.out.println("entWithPropDependency.getId() == " + entWithPropDependency.getId());

        save(new_composite(UserAndRoleAssociation.class, demo, admin));

        final UserRole stationMgr = save(new_(UserRole.class, "STATION_MGR", "A role, which has access to the the station managing functionality."));
        System.out.println("stationMgr.getId() == " + stationMgr.getId());
        final UserAndRoleAssociation su2StationMgr = save(new_composite(UserAndRoleAssociation.class, su, stationMgr));
        System.out.println("su2StationMgr.getId() == " + su2StationMgr.getId());

        final UserRole testRole1 = save(new_(UserRole.class, "TEST_ROLE1", "A role, which has access to the the station managing functionality."));
        System.out.println("testRole1.getId() == " + testRole1.getId());

        final UserRole testRole2 = save(new_(UserRole.class, "TEST_ROLE2", "A role, which has access to the the station managing functionality."));
        System.out.println("testRole2.getId() == " + testRole2.getId());

        final UserRole testRole3 = save(new_(UserRole.class, "TEST_ROLE3", "A role, which has access to the the station managing functionality."));
        System.out.println("testRole3.getId() == " + testRole3.getId());

        final UserRole testRole4 = save(new_(UserRole.class, "TEST_ROLE4", "A role, which has access to the the station managing functionality."));
        System.out.println("testRole4.getId() == " + testRole4.getId());

        final UserRole testRole5 = save(new_(UserRole.class, "TEST_ROLE5", "A role, which has access to the the station managing functionality."));
        System.out.println("testRole5.getId() == " + testRole5.getId());

        final UserRole testRole6 = save(new_(UserRole.class, "TEST_ROLE6", "A role, which has access to the the station managing functionality."));
        System.out.println("testRole6.getId() == " + testRole6.getId());

        final UserRole testRole7 = save(new_(UserRole.class, "TEST_ROLE7", "A role, which has access to the the station managing functionality."));
        System.out.println("testRole7.getId() == " + testRole7.getId());

        final UserRole testRole8 = save(new_(UserRole.class, "TEST_ROLE8", "A role, which has access to the the station managing functionality."));
        System.out.println("testRole8.getId() == " + testRole8.getId());

        final UserRole testRole9 = save(new_(UserRole.class, "TEST_ROLE9", "A role, which has access to the the station managing functionality."));
        System.out.println("testRole9.getId() == " + testRole9.getId());

        final TgCollectionalSerialisationParent csp1 = (TgCollectionalSerialisationParent) save(new_(TgCollectionalSerialisationParent.class, "CSP1").setDesc("desc1"));
        save(new_composite(TgCollectionalSerialisationChild.class, csp1, "1").setDesc("desc1"));
        
        final MainMenu mainMenu = new_(MainMenu.class, "IRRELEVANT");
        mainMenu.setMenuItems(MainMenuStructureFactory.toStrings(config.getInstance(TemplateMainMenu.class).build()));
        save(mainMenu);

        try {
            final IApplicationSettings settings = config.getInstance(IApplicationSettings.class);
            final SecurityTokenProvider provider = new SecurityTokenProvider(settings.pathToSecurityTokens(), settings.securityTokensPackageName()); //  IDomainDrivenTestCaseConfiguration.hbc.getProperty("tokens.path"), IDomainDrivenTestCaseConfiguration.hbc.getProperty("tokens.package")
            final SortedSet<SecurityTokenNode> topNodes = provider.getTopLevelSecurityTokenNodes();
            final SecurityTokenAssociator predicate = new SecurityTokenAssociator(admin, ao(SecurityRoleAssociation.class));
            final ISearchAlgorithm<Class<? extends ISecurityToken>, SecurityTokenNode> alg = new BreadthFirstSearch<Class<? extends ISecurityToken>, SecurityTokenNode>();
            for (final SecurityTokenNode securityNode : topNodes) {
                alg.search(securityNode, predicate);
            }

            System.out.println("Completed database creation and population.");
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
