package ua.com.fielden.platform.example.dynamiccriteria;

import java.io.FileInputStream;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.devdb_support.DomainDrivenDataPopulation;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.example.dynamiccriteria.entities.SimpleECEEntity;
import ua.com.fielden.platform.security.user.IUserDao;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.test.IDomainDrivenTestCaseConfiguration;
import ua.com.fielden.platform.ui.config.MainMenuItem;
import ua.com.fielden.platform.ui.config.api.IMainMenuItemController;

public class PopulateDbForEntityCentreExample extends DomainDrivenDataPopulation {

    public PopulateDbForEntityCentreExample(final IDomainDrivenTestCaseConfiguration config) {
	super(config);
	// TODO Auto-generated constructor stub
    }

    public static void initDb(final String arg) throws Exception{
	//TODO please review entity_centre_example.properties and correct appropriate properties.
	final String configFileName = !StringUtils.isEmpty(arg) ? arg : "src/main/resources/entity_centre_example.properties";
	final FileInputStream in = new FileInputStream(configFileName);
	final Properties props = IDomainDrivenTestCaseConfiguration.hbc;
	props.load(in);
	in.close();

	// override/set some of the Hibernate properties in order to ensure (re-)creation of the target database
	props.put("hibernate.show_sql", "false");
	props.put("hibernate.format_sql", "true");
	props.put("hibernate.hbm2ddl.auto", "create");


	final IDomainDrivenTestCaseConfiguration config = new EntityCentreDataPopulationConfiguration();


	final PopulateDbForEntityCentreExample popDb = new PopulateDbForEntityCentreExample(config);
	popDb.createAndPopulate();
    }

    @Override
    protected void populateDomain() {

	//Configure base and non base users and save them into the database.
	final IUserDao userDao = ao(User.class);
	final User baseUser = new_(User.class, User.system_users.SU.name(), "Super user");
	baseUser.setBase(true);
	userDao.save(baseUser);
	final User nonBaseUser = new_(User.class, "DEMO", "Non base user");
	nonBaseUser.setBase(false);
	nonBaseUser.setBasedOnUser(baseUser);
	userDao.save(nonBaseUser);

	//Configure main menu.
	final MainMenuItem menuItem = new_(MainMenuItem.class, SimpleECEEntity.class.getName());
	final IMainMenuItemController menuItemDao = ao(MainMenuItem.class);
	menuItemDao.save(menuItem);

    }

    @Override
    protected List<Class<? extends AbstractEntity>> domainEntityTypes() {
	return EntityCentreExampleDomain.entityTypes;
    }

}
