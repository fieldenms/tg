package ua.com.fielden.platform.example.dynamiccriteria;

import java.io.FileInputStream;
import java.util.List;
import java.util.Properties;

import ua.com.fielden.platform.devdb_support.DomainDrivenDataPopulation;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.example.dynamiccriteria.entities.SimpleCompositeEntity;
import ua.com.fielden.platform.example.dynamiccriteria.entities.SimpleECEEntity;
import ua.com.fielden.platform.example.dynamiccriteria.entities.SimpleNestedEntity;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.test.IDomainDrivenTestCaseConfiguration;
import ua.com.fielden.platform.ui.config.MainMenuItem;

/**
 * This is a convenience class for (re-)creation of the development database and its population.
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

    private final EntityCentreExampleDomain applicationDomainProvider = new EntityCentreExampleDomain();

    private PopulateDb(final IDomainDrivenTestCaseConfiguration config) {
	super(config);
    }

    public static void main(final String[] args) throws Exception {
	final String configFileName = args.length == 1 ? args[1] : "src/main/resources/entity_centre_example.properties";
	final FileInputStream in = new FileInputStream(configFileName);
	final Properties props = IDomainDrivenTestCaseConfiguration.hbc;
	props.load(in);
	in.close();

	// override/set some of the Hibernate properties in order to ensure (re-)creation of the target database
	props.put("hibernate.show_sql", "false");
	props.put("hibernate.format_sql", "true");
	props.put("hibernate.hbm2ddl.auto", "create");


	final IDomainDrivenTestCaseConfiguration config = new EntityCentreDataPopulationConfiguration();

	final PopulateDb popDb = new PopulateDb(config);
	popDb.createAndPopulate();
    }

    @Override
    protected void populateDomain() {
	//Configure base and non base users and save them into the database.
	final User baseUser = save(new_(User.class, User.system_users.SU.name(), "Super user").setBase(true));
	save(new_(User.class, "DEMO", "Non base user").setBase(false).setBasedOnUser(baseUser));

	//Configure main menu.
	save(new_(MainMenuItem.class, MiSimpleECEEntity.class.getName()));
	save(new_(MainMenuItem.class, MiSimpleCompositeEntity.class.getName()));

	//Configuring domain
	//Nested entities.
	final SimpleNestedEntity nentity1 = save(new_(SimpleNestedEntity.class, "NESTED1").setStringProperty("nestedString1").setInitDate(date("2008-02-09 00:00:00")).setActive(true).setNumValue(Integer.valueOf(30)));
	final SimpleNestedEntity nentity2 = save(new_(SimpleNestedEntity.class, "NESTED2").setStringProperty("nestedString2").setInitDate(date("2008-03-10 00:00:00")).setActive(false).setNumValue(Integer.valueOf(40)));
	final SimpleNestedEntity nentity3 = save(new_(SimpleNestedEntity.class, "NESTED3").setStringProperty("nestedString3").setInitDate(date("2008-04-11 00:00:00")).setActive(true).setNumValue(Integer.valueOf(50)));
	final SimpleNestedEntity nentity4 = save(new_(SimpleNestedEntity.class, "NESTED4").setStringProperty("nestedString4").setInitDate(date("2008-05-12 00:00:00")).setActive(false).setNumValue(Integer.valueOf(60)));
	final SimpleNestedEntity nentity5 = save(new_(SimpleNestedEntity.class, "NESTED5").setStringProperty("nestedString5").setInitDate(date("2008-06-13 00:00:00")).setActive(true).setNumValue(Integer.valueOf(70)));
	final SimpleNestedEntity nentity6 = save(new_(SimpleNestedEntity.class, "NESTED6").setStringProperty("nestedString6").setInitDate(date("2008-07-14 00:00:00")).setActive(false).setNumValue(Integer.valueOf(80)));
	final SimpleNestedEntity nentity7 = save(new_(SimpleNestedEntity.class, "NESTED7").setStringProperty("nestedString7").setInitDate(date("2008-08-15 00:00:00")).setActive(true).setNumValue(Integer.valueOf(90)));
	final SimpleNestedEntity nentity8 = save(new_(SimpleNestedEntity.class, "NESTED8").setStringProperty("nestedString8").setInitDate(date("2008-09-16 00:00:00")).setActive(false).setNumValue(Integer.valueOf(100)));
	final SimpleNestedEntity nentity9 = save(new_(SimpleNestedEntity.class, "NESTED9").setStringProperty("nestedString9").setInitDate(date("2008-10-17 00:00:00")).setActive(true).setNumValue(Integer.valueOf(301)));
	final SimpleNestedEntity nentity10 = save(new_(SimpleNestedEntity.class, "NESTED10").setStringProperty("nestedString10").setInitDate(date("2008-11-18 00:00:00")).setActive(false).setNumValue(Integer.valueOf(303)));

	final SimpleECEEntity sentity1 = save(new_(SimpleECEEntity.class, "SIMPLE1").setDesc("LDKFGKLJDFG").setStringProperty("simpleString1").setInitDate(date("2009-01-01 00:00:00")).setActive(true).setNumValue(Integer.valueOf(10)).setNestedEntity(nentity1));
	final SimpleECEEntity sentity2 = save(new_(SimpleECEEntity.class, "SIMPLE2").setDesc("DLGKGJERJ").setStringProperty("simpleString1").setInitDate(date("2009-01-02 00:00:00")).setActive(false).setNumValue(Integer.valueOf(20)).setNestedEntity(nentity1));
	final SimpleECEEntity sentity3 = save(new_(SimpleECEEntity.class, "SIMPLE3").setDesc("LWIDKFHKLF").setStringProperty("simpleString1").setInitDate(date("2009-01-03 00:00:00")).setActive(true).setNumValue(Integer.valueOf(30)).setNestedEntity(nentity1));
	final SimpleECEEntity sentity4 = save(new_(SimpleECEEntity.class, "SIMPLE4").setDesc(";HKJLDJFG").setStringProperty("simpleString1").setInitDate(date("2009-01-04 00:00:00")).setActive(false).setNumValue(Integer.valueOf(40)).setNestedEntity(nentity1));
	final SimpleECEEntity sentity5 = save(new_(SimpleECEEntity.class, "SIMPLE5").setDesc("D;FGEL;RLERJ").setStringProperty("simpleString1").setInitDate(date("2009-01-05 00:00:00")).setActive(true).setNumValue(Integer.valueOf(50)).setNestedEntity(nentity1));
	final SimpleECEEntity sentity6 = save(new_(SimpleECEEntity.class, "SIMPLE6").setDesc("DFGKEJKG").setStringProperty("simpleString1").setInitDate(date("2009-01-06 00:00:00")).setActive(false).setNumValue(Integer.valueOf(10)).setNestedEntity(nentity2));
	final SimpleECEEntity sentity7 = save(new_(SimpleECEEntity.class, "SIMPLE7").setDesc("DFLGJEKLJR").setStringProperty("simpleString1").setInitDate(date("2009-01-07 00:00:00")).setActive(true).setNumValue(Integer.valueOf(20)).setNestedEntity(nentity2));
	final SimpleECEEntity sentity8 = save(new_(SimpleECEEntity.class, "SIMPLE8").setDesc("LD;FGKSDFG").setStringProperty("simpleString1").setInitDate(date("2009-01-08 00:00:00")).setActive(false).setNumValue(Integer.valueOf(30)).setNestedEntity(nentity2));
	final SimpleECEEntity sentity9 = save(new_(SimpleECEEntity.class, "SIMPLE9").setDesc("D/LFGMLKEG").setStringProperty("simpleString1").setInitDate(date("2009-01-09 00:00:00")).setActive(true).setNumValue(Integer.valueOf(40)).setNestedEntity(nentity2));
	final SimpleECEEntity sentity10 = save(new_(SimpleECEEntity.class, "SIMPLE10").setDesc("L;FMGKSLDNG;G").setStringProperty("simpleString1").setInitDate(date("2009-01-10 00:00:00")).setActive(false).setNumValue(Integer.valueOf(50)).setNestedEntity(nentity2));
	final SimpleECEEntity sentity11 = save(new_(SimpleECEEntity.class, "SIMPLE11").setDesc("DL;RJLKJDGF").setStringProperty("simpleString2").setInitDate(date("2009-02-01 00:00:00")).setActive(true).setNumValue(Integer.valueOf(60)).setNestedEntity(nentity3));
	final SimpleECEEntity sentity12 = save(new_(SimpleECEEntity.class, "SIMPLE12").setDesc("F,HFJKNJKDFGH").setStringProperty("simpleString2").setInitDate(date("2009-02-02 00:00:00")).setActive(false).setNumValue(Integer.valueOf(70)).setNestedEntity(nentity3));
	final SimpleECEEntity sentity13 = save(new_(SimpleECEEntity.class, "SIMPLE13").setDesc("HSGSHF").setStringProperty("simpleString2").setInitDate(date("2009-02-03 00:00:00")).setActive(true).setNumValue(Integer.valueOf(80)).setNestedEntity(nentity3));
	final SimpleECEEntity sentity14 = save(new_(SimpleECEEntity.class, "SIMPLE14").setDesc("DFKLHMGD").setStringProperty("simpleString2").setInitDate(date("2009-02-04 00:00:00")).setActive(false).setNumValue(Integer.valueOf(10)).setNestedEntity(nentity3));
	final SimpleECEEntity sentity15 = save(new_(SimpleECEEntity.class, "SIMPLE15").setDesc("FKJHKSKDFJ").setStringProperty("simpleString2").setInitDate(date("2009-02-05 00:00:00")).setActive(true).setNumValue(Integer.valueOf(20)).setNestedEntity(nentity3));
	final SimpleECEEntity sentity16 = save(new_(SimpleECEEntity.class, "SIMPLE16").setDesc("ERJJGLDJG").setStringProperty("simpleString2").setInitDate(date("2009-02-06 00:00:00")).setActive(false).setNumValue(Integer.valueOf(11)).setNestedEntity(nentity4));
	final SimpleECEEntity sentity17 = save(new_(SimpleECEEntity.class, "SIMPLE17").setDesc("DJKFGJGKFLG").setStringProperty("simpleString2").setInitDate(date("2009-02-07 00:00:00")).setActive(true).setNumValue(Integer.valueOf(100)).setNestedEntity(nentity4));
	final SimpleECEEntity sentity18 = save(new_(SimpleECEEntity.class, "SIMPLE18").setDesc("LH,;LKDDSF").setStringProperty("simpleString2").setInitDate(date("2009-02-08 00:00:00")).setActive(false).setNumValue(Integer.valueOf(140)).setNestedEntity(nentity4));
	final SimpleECEEntity sentity19 = save(new_(SimpleECEEntity.class, "SIMPLE19").setDesc("F.GHKLFNHGH").setStringProperty("simpleString2").setInitDate(date("2009-02-09 00:00:00")).setActive(true).setNumValue(Integer.valueOf(15)).setNestedEntity(nentity4));
	final SimpleECEEntity sentity20 = save(new_(SimpleECEEntity.class, "SIMPLE20").setDesc("DLFKKELJGDFG").setStringProperty("simpleString2").setInitDate(date("2009-02-10 00:00:00")).setActive(false).setNumValue(Integer.valueOf(17)).setNestedEntity(nentity4));
	final SimpleECEEntity sentity21 = save(new_(SimpleECEEntity.class, "SIMPLE21").setDesc("D.KFJGJDF").setStringProperty("simpleString3").setInitDate(date("2009-03-01 00:00:00")).setActive(true).setNumValue(Integer.valueOf(18)).setNestedEntity(nentity5));
	final SimpleECEEntity sentity22 = save(new_(SimpleECEEntity.class, "SIMPLE22").setDesc("D/,.FGMKDJFGD").setStringProperty("simpleString3").setInitDate(date("2009-03-02 00:00:00")).setActive(false).setNumValue(Integer.valueOf(13)).setNestedEntity(nentity5));
	final SimpleECEEntity sentity23 = save(new_(SimpleECEEntity.class, "SIMPLE23").setDesc("D/LFGDFMG").setStringProperty("simpleString3").setInitDate(date("2009-03-03 00:00:00")).setActive(true).setNumValue(Integer.valueOf(155)).setNestedEntity(nentity5));
	final SimpleECEEntity sentity24 = save(new_(SimpleECEEntity.class, "SIMPLE24").setDesc("DLFMGDKFJGDGF").setStringProperty("simpleString3").setInitDate(date("2009-03-04 00:00:00")).setActive(false).setNumValue(Integer.valueOf(150)).setNestedEntity(nentity5));
	final SimpleECEEntity sentity25 = save(new_(SimpleECEEntity.class, "SIMPLE25").setDesc("D.FMGKLDMFGDG").setStringProperty("simpleString3").setInitDate(date("2009-03-05 00:00:00")).setActive(true).setNumValue(Integer.valueOf(106)).setNestedEntity(nentity5));
	final SimpleECEEntity sentity26 = save(new_(SimpleECEEntity.class, "SIMPLE26").setDesc("D;LFGKLKDFGD").setStringProperty("simpleString3").setInitDate(date("2010-03-06 00:00:00")).setActive(false).setNumValue(Integer.valueOf(107)).setNestedEntity(nentity6));
	final SimpleECEEntity sentity27 = save(new_(SimpleECEEntity.class, "SIMPLE27").setDesc("GSVDFHGSDVF").setStringProperty("simpleString3").setInitDate(date("2010-03-07 00:00:00")).setActive(true).setNumValue(Integer.valueOf(108)).setNestedEntity(nentity6));
	final SimpleECEEntity sentity28 = save(new_(SimpleECEEntity.class, "SIMPLE28").setDesc("SHGDHGF").setStringProperty("simpleString3").setInitDate(date("2010-03-08 00:00:00")).setActive(false).setNumValue(Integer.valueOf(109)).setNestedEntity(nentity6));
	final SimpleECEEntity sentity29 = save(new_(SimpleECEEntity.class, "SIMPLE29").setDesc("POTYUPOU").setStringProperty("simpleString3").setInitDate(date("2010-03-09 00:00:00")).setActive(true).setNumValue(Integer.valueOf(102)).setNestedEntity(nentity6));
	final SimpleECEEntity sentity30 = save(new_(SimpleECEEntity.class, "SIMPLE30").setDesc("NBNSVDFSFHY").setStringProperty("simpleString3").setInitDate(date("2010-03-10 00:00:00")).setActive(false).setNumValue(Integer.valueOf(104)).setNestedEntity(nentity6));
	final SimpleECEEntity sentity31 = save(new_(SimpleECEEntity.class, "SIMPLE31").setDesc("SIHFKDGD").setStringProperty("simpleString4").setInitDate(date("2010-04-01 00:00:00")).setActive(true).setNumValue(Integer.valueOf(105)).setNestedEntity(nentity7));
	final SimpleECEEntity sentity32 = save(new_(SimpleECEEntity.class, "SIMPLE32").setDesc("DFGJBDGDFGF").setStringProperty("simpleString4").setInitDate(date("2010-04-02 00:00:00")).setActive(false).setNumValue(Integer.valueOf(150)).setNestedEntity(nentity7));
	final SimpleECEEntity sentity33 = save(new_(SimpleECEEntity.class, "SIMPLE33").setDesc("NBCNMXDFG").setStringProperty("simpleString4").setInitDate(date("2010-04-03 00:00:00")).setActive(true).setNumValue(Integer.valueOf(140)).setNestedEntity(nentity7));
	final SimpleECEEntity sentity34 = save(new_(SimpleECEEntity.class, "SIMPLE34").setDesc("JGBDFGB").setStringProperty("simpleString4").setInitDate(date("2010-04-04 00:00:00")).setActive(false).setNumValue(Integer.valueOf(130)).setNestedEntity(nentity7));
	final SimpleECEEntity sentity35 = save(new_(SimpleECEEntity.class, "SIMPLE35").setDesc("CXCNDFG").setStringProperty("simpleString4").setInitDate(date("2010-04-05 00:00:00")).setActive(true).setNumValue(Integer.valueOf(120)).setNestedEntity(nentity7));
	final SimpleECEEntity sentity36 = save(new_(SimpleECEEntity.class, "SIMPLE36").setDesc("EKRJGEKJTE").setStringProperty("simpleString4").setInitDate(date("2010-04-06 00:00:00")).setActive(false).setNumValue(Integer.valueOf(60)).setNestedEntity(nentity8));
	final SimpleECEEntity sentity37 = save(new_(SimpleECEEntity.class, "SIMPLE37").setDesc("DFGHDFGDFG").setStringProperty("simpleString4").setInitDate(date("2010-04-07 00:00:00")).setActive(true).setNumValue(Integer.valueOf(70)).setNestedEntity(nentity8));
	final SimpleECEEntity sentity38 = save(new_(SimpleECEEntity.class, "SIMPLE38").setDesc("NMGSJGFD").setStringProperty("simpleString4").setInitDate(date("2010-04-08 00:00:00")).setActive(false).setNumValue(Integer.valueOf(80)).setNestedEntity(nentity8));
	final SimpleECEEntity sentity39 = save(new_(SimpleECEEntity.class, "SIMPLE39").setDesc("RTYREBSERYV").setStringProperty("simpleString4").setInitDate(date("2010-04-09 00:00:00")).setActive(true).setNumValue(Integer.valueOf(90)).setNestedEntity(nentity8));
	final SimpleECEEntity sentity40 = save(new_(SimpleECEEntity.class, "SIMPLE40").setDesc("SDGSDFSNPWOE").setStringProperty("simpleString4").setInitDate(date("2010-04-10 00:00:00")).setActive(false).setNumValue(Integer.valueOf(610)).setNestedEntity(nentity8));
	final SimpleECEEntity sentity41 = save(new_(SimpleECEEntity.class, "SIMPLE41").setDesc("SDFSFPWER").setStringProperty("simpleString5").setInitDate(date("2010-05-01 00:00:00")).setActive(true).setNumValue(Integer.valueOf(610)).setNestedEntity(nentity9));
	final SimpleECEEntity sentity42 = save(new_(SimpleECEEntity.class, "SIMPLE42").setDesc("DSFSDFWERY").setStringProperty("simpleString5").setInitDate(date("2010-05-02 00:00:00")).setActive(false).setNumValue(Integer.valueOf(710)).setNestedEntity(nentity9));
	final SimpleECEEntity sentity43 = save(new_(SimpleECEEntity.class, "SIMPLE43").setDesc("SDUWERVWER").setStringProperty("simpleString5").setInitDate(date("2010-05-03 00:00:00")).setActive(true).setNumValue(Integer.valueOf(810)).setNestedEntity(nentity9));
	final SimpleECEEntity sentity44 = save(new_(SimpleECEEntity.class, "SIMPLE44").setDesc("DSHSLKDJR").setStringProperty("simpleString5").setInitDate(date("2010-05-04 00:00:00")).setActive(false).setNumValue(Integer.valueOf(310)).setNestedEntity(nentity9));
	final SimpleECEEntity sentity45 = save(new_(SimpleECEEntity.class, "SIMPLE45").setDesc("SDKFJLDKF").setStringProperty("simpleString5").setInitDate(date("2010-05-05 00:00:00")).setActive(true).setNumValue(Integer.valueOf(610)).setNestedEntity(nentity9));
	final SimpleECEEntity sentity46 = save(new_(SimpleECEEntity.class, "SIMPLE46").setDesc("SPERSSDF").setStringProperty("simpleString5").setInitDate(date("2010-05-06 00:00:00")).setActive(false).setNumValue(Integer.valueOf(310)).setNestedEntity(nentity10));
	final SimpleECEEntity sentity47 = save(new_(SimpleECEEntity.class, "SIMPLE47").setDesc("FLKGHJGFHW").setStringProperty("simpleString5").setInitDate(date("2010-05-07 00:00:00")).setActive(true).setNumValue(Integer.valueOf(710)).setNestedEntity(nentity10));
	final SimpleECEEntity sentity48 = save(new_(SimpleECEEntity.class, "SIMPLE48").setDesc("DJSHFWER").setStringProperty("simpleString5").setInitDate(date("2010-05-08 00:00:00")).setActive(false).setNumValue(Integer.valueOf(810)).setNestedEntity(nentity10));
	final SimpleECEEntity sentity49 = save(new_(SimpleECEEntity.class, "SIMPLE49").setDesc("M,FDNSDKJFNS").setStringProperty("simpleString5").setInitDate(date("2010-05-09 00:00:00")).setActive(true).setNumValue(Integer.valueOf(510)).setNestedEntity(nentity10));
	final SimpleECEEntity sentity50 = save(new_(SimpleECEEntity.class, "SIMPLE50").setDesc("SJDKFJDFS").setStringProperty("simpleString5").setInitDate(date("2010-05-10 00:00:00")).setActive(false).setNumValue(Integer.valueOf(210)).setNestedEntity(nentity10));

	save(new_composite(SimpleCompositeEntity.class, sentity1, "COMPLEX1").setInitDate(date("2008-01-01 00:00:00")).setActive(true).setNumValue(Integer.valueOf(10)));
	save(new_composite(SimpleCompositeEntity.class, sentity1, "COMPLEX2").setInitDate(date("2008-01-02 00:00:00")).setActive(false).setNumValue(Integer.valueOf(130)));
	save(new_composite(SimpleCompositeEntity.class, sentity2, "COMPLEX1").setInitDate(date("2008-01-03 00:00:00")).setActive(true).setNumValue(Integer.valueOf(104)));
	save(new_composite(SimpleCompositeEntity.class, sentity2, "COMPLEX2").setInitDate(date("2008-01-04 00:00:00")).setActive(true).setNumValue(Integer.valueOf(105)));
	save(new_composite(SimpleCompositeEntity.class, sentity3, "COMPLEX1").setInitDate(date("2008-01-05 00:00:00")).setActive(false).setNumValue(Integer.valueOf(150)));
	save(new_composite(SimpleCompositeEntity.class, sentity3, "COMPLEX2").setInitDate(date("2008-01-06 00:00:00")).setActive(false).setNumValue(Integer.valueOf(140)));
	save(new_composite(SimpleCompositeEntity.class, sentity4, "COMPLEX1").setInitDate(date("2008-01-07 00:00:00")).setActive(true).setNumValue(Integer.valueOf(101)));
	save(new_composite(SimpleCompositeEntity.class, sentity4, "COMPLEX2").setInitDate(date("2008-01-08 00:00:00")).setActive(true).setNumValue(Integer.valueOf(103)));
	save(new_composite(SimpleCompositeEntity.class, sentity5, "COMPLEX1").setInitDate(date("2008-01-09 00:00:00")).setActive(false).setNumValue(Integer.valueOf(120)));
	save(new_composite(SimpleCompositeEntity.class, sentity5, "COMPLEX2").setInitDate(date("2008-01-10 00:00:00")).setActive(true).setNumValue(Integer.valueOf(103)));
	save(new_composite(SimpleCompositeEntity.class, sentity6, "COMPLEX1").setInitDate(date("2008-01-11 00:00:00")).setActive(false).setNumValue(Integer.valueOf(130)));
	save(new_composite(SimpleCompositeEntity.class, sentity6, "COMPLEX2").setInitDate(date("2008-01-12 00:00:00")).setActive(true).setNumValue(Integer.valueOf(50)));
	save(new_composite(SimpleCompositeEntity.class, sentity7, "COMPLEX1").setInitDate(date("2008-01-13 00:00:00")).setActive(true).setNumValue(Integer.valueOf(60)));
	save(new_composite(SimpleCompositeEntity.class, sentity7, "COMPLEX2").setInitDate(date("2008-01-14 00:00:00")).setActive(false).setNumValue(Integer.valueOf(410)));
	save(new_composite(SimpleCompositeEntity.class, sentity8, "COMPLEX1").setInitDate(date("2008-01-15 00:00:00")).setActive(false).setNumValue(Integer.valueOf(150)));
	save(new_composite(SimpleCompositeEntity.class, sentity8, "COMPLEX2").setInitDate(date("2008-01-16 00:00:00")).setActive(true).setNumValue(Integer.valueOf(106)));
	save(new_composite(SimpleCompositeEntity.class, sentity9, "COMPLEX1").setInitDate(date("2008-01-17 00:00:00")).setActive(true).setNumValue(Integer.valueOf(108)));
	save(new_composite(SimpleCompositeEntity.class, sentity9, "COMPLEX2").setInitDate(date("2008-01-18 00:00:00")).setActive(false).setNumValue(Integer.valueOf(150)));
	save(new_composite(SimpleCompositeEntity.class, sentity10, "COMPLEX1").setInitDate(date("2008-01-19 00:00:00")).setActive(false).setNumValue(Integer.valueOf(610)));
	save(new_composite(SimpleCompositeEntity.class, sentity10, "COMPLEX2").setInitDate(date("2008-01-20 00:00:00")).setActive(true).setNumValue(Integer.valueOf(180)));
	save(new_composite(SimpleCompositeEntity.class, sentity11, "COMPLEX1").setInitDate(date("2008-02-01 00:00:00")).setActive(false).setNumValue(Integer.valueOf(1)));
	save(new_composite(SimpleCompositeEntity.class, sentity11, "COMPLEX2").setInitDate(date("2008-02-02 00:00:00")).setActive(true).setNumValue(Integer.valueOf(1)));
	save(new_composite(SimpleCompositeEntity.class, sentity12, "COMPLEX1").setInitDate(date("2008-02-03 00:00:00")).setActive(false).setNumValue(Integer.valueOf(140)));
	save(new_composite(SimpleCompositeEntity.class, sentity12, "COMPLEX2").setInitDate(date("2008-02-04 00:00:00")).setActive(false).setNumValue(Integer.valueOf(140)));
	save(new_composite(SimpleCompositeEntity.class, sentity13, "COMPLEX1").setInitDate(date("2008-02-05 00:00:00")).setActive(true).setNumValue(Integer.valueOf(105)));
	save(new_composite(SimpleCompositeEntity.class, sentity13, "COMPLEX2").setInitDate(date("2008-02-06 00:00:00")).setActive(false).setNumValue(Integer.valueOf(105)));
	save(new_composite(SimpleCompositeEntity.class, sentity14, "COMPLEX1").setInitDate(date("2008-02-07 00:00:00")).setActive(true).setNumValue(Integer.valueOf(60)));
	save(new_composite(SimpleCompositeEntity.class, sentity14, "COMPLEX2").setInitDate(date("2008-02-08 00:00:00")).setActive(true).setNumValue(Integer.valueOf(70)));
	save(new_composite(SimpleCompositeEntity.class, sentity15, "COMPLEX1").setInitDate(date("2008-02-09 00:00:00")).setActive(false).setNumValue(Integer.valueOf(40)));
	save(new_composite(SimpleCompositeEntity.class, sentity15, "COMPLEX2").setInitDate(date("2008-02-10 00:00:00")).setActive(true).setNumValue(Integer.valueOf(106)));
	save(new_composite(SimpleCompositeEntity.class, sentity16, "COMPLEX1").setInitDate(date("2008-02-11 00:00:00")).setActive(false).setNumValue(Integer.valueOf(10)));
	save(new_composite(SimpleCompositeEntity.class, sentity16, "COMPLEX2").setInitDate(date("2008-02-12 00:00:00")).setActive(true).setNumValue(Integer.valueOf(210)));
	save(new_composite(SimpleCompositeEntity.class, sentity17, "COMPLEX1").setInitDate(date("2008-02-13 00:00:00")).setActive(false).setNumValue(Integer.valueOf(210)));
	save(new_composite(SimpleCompositeEntity.class, sentity17, "COMPLEX2").setInitDate(date("2008-02-14 00:00:00")).setActive(true).setNumValue(Integer.valueOf(104)));
	save(new_composite(SimpleCompositeEntity.class, sentity18, "COMPLEX1").setInitDate(date("2008-02-15 00:00:00")).setActive(false).setNumValue(Integer.valueOf(104)));
	save(new_composite(SimpleCompositeEntity.class, sentity18, "COMPLEX2").setInitDate(date("2008-02-16 00:00:00")).setActive(false).setNumValue(Integer.valueOf(106)));
	save(new_composite(SimpleCompositeEntity.class, sentity19, "COMPLEX1").setInitDate(date("2008-02-17 00:00:00")).setActive(true).setNumValue(Integer.valueOf(170)));
	save(new_composite(SimpleCompositeEntity.class, sentity19, "COMPLEX2").setInitDate(date("2008-02-18 00:00:00")).setActive(true).setNumValue(Integer.valueOf(107)));
	save(new_composite(SimpleCompositeEntity.class, sentity20, "COMPLEX1").setInitDate(date("2008-02-19 00:00:00")).setActive(true).setNumValue(Integer.valueOf(106)));
	save(new_composite(SimpleCompositeEntity.class, sentity20, "COMPLEX2").setInitDate(date("2008-02-20 00:00:00")).setActive(false).setNumValue(Integer.valueOf(105)));
	save(new_composite(SimpleCompositeEntity.class, sentity21, "COMPLEX1").setInitDate(date("2008-03-01 00:00:00")).setActive(true).setNumValue(Integer.valueOf(10)));
	save(new_composite(SimpleCompositeEntity.class, sentity21, "COMPLEX2").setInitDate(date("2008-03-02 00:00:00")).setActive(true).setNumValue(Integer.valueOf(10)));
	save(new_composite(SimpleCompositeEntity.class, sentity22, "COMPLEX1").setInitDate(date("2008-03-03 00:00:00")).setActive(false).setNumValue(Integer.valueOf(104)));
	save(new_composite(SimpleCompositeEntity.class, sentity22, "COMPLEX2").setInitDate(date("2008-03-04 00:00:00")).setActive(true).setNumValue(Integer.valueOf(10)));
	save(new_composite(SimpleCompositeEntity.class, sentity23, "COMPLEX1").setInitDate(date("2008-03-05 00:00:00")).setActive(false).setNumValue(Integer.valueOf(10)));
	save(new_composite(SimpleCompositeEntity.class, sentity23, "COMPLEX2").setInitDate(date("2008-03-06 00:00:00")).setActive(true).setNumValue(Integer.valueOf(106)));
	save(new_composite(SimpleCompositeEntity.class, sentity24, "COMPLEX1").setInitDate(date("2008-03-07 00:00:00")).setActive(false).setNumValue(Integer.valueOf(107)));
	save(new_composite(SimpleCompositeEntity.class, sentity24, "COMPLEX2").setInitDate(date("2008-03-08 00:00:00")).setActive(true).setNumValue(Integer.valueOf(10)));
	save(new_composite(SimpleCompositeEntity.class, sentity25, "COMPLEX1").setInitDate(date("2008-03-09 00:00:00")).setActive(true).setNumValue(Integer.valueOf(120)));
	save(new_composite(SimpleCompositeEntity.class, sentity25, "COMPLEX2").setInitDate(date("2008-03-10 00:00:00")).setActive(false).setNumValue(Integer.valueOf(1650)));
	save(new_composite(SimpleCompositeEntity.class, sentity26, "COMPLEX1").setInitDate(date("2010-01-01 00:00:00")).setActive(true).setNumValue(Integer.valueOf(10)));
	save(new_composite(SimpleCompositeEntity.class, sentity26, "COMPLEX2").setInitDate(date("2010-01-02 00:00:00")).setActive(false).setNumValue(Integer.valueOf(410)));
	save(new_composite(SimpleCompositeEntity.class, sentity27, "COMPLEX1").setInitDate(date("2010-01-03 00:00:00")).setActive(true).setNumValue(Integer.valueOf(140)));
	save(new_composite(SimpleCompositeEntity.class, sentity27, "COMPLEX2").setInitDate(date("2010-01-04 00:00:00")).setActive(false).setNumValue(Integer.valueOf(310)));
	save(new_composite(SimpleCompositeEntity.class, sentity28, "COMPLEX1").setInitDate(date("2010-01-05 00:00:00")).setActive(true).setNumValue(Integer.valueOf(103)));
	save(new_composite(SimpleCompositeEntity.class, sentity28, "COMPLEX2").setInitDate(date("2010-01-06 00:00:00")).setActive(false).setNumValue(Integer.valueOf(103)));
	save(new_composite(SimpleCompositeEntity.class, sentity29, "COMPLEX1").setInitDate(date("2010-01-07 00:00:00")).setActive(true).setNumValue(Integer.valueOf(104)));
	save(new_composite(SimpleCompositeEntity.class, sentity29, "COMPLEX2").setInitDate(date("2010-01-08 00:00:00")).setActive(true).setNumValue(Integer.valueOf(17)));
	save(new_composite(SimpleCompositeEntity.class, sentity30, "COMPLEX1").setInitDate(date("2010-01-09 00:00:00")).setActive(true).setNumValue(Integer.valueOf(108)));
	save(new_composite(SimpleCompositeEntity.class, sentity30, "COMPLEX2").setInitDate(date("2010-01-10 00:00:00")).setActive(false).setNumValue(Integer.valueOf(103)));
	save(new_composite(SimpleCompositeEntity.class, sentity31, "COMPLEX1").setInitDate(date("2010-01-11 00:00:00")).setActive(false).setNumValue(Integer.valueOf(13)));
	save(new_composite(SimpleCompositeEntity.class, sentity31, "COMPLEX2").setInitDate(date("2010-01-12 00:00:00")).setActive(true).setNumValue(Integer.valueOf(143)));
	save(new_composite(SimpleCompositeEntity.class, sentity32, "COMPLEX1").setInitDate(date("2010-01-13 00:00:00")).setActive(false).setNumValue(Integer.valueOf(145)));
	save(new_composite(SimpleCompositeEntity.class, sentity32, "COMPLEX2").setInitDate(date("2010-01-14 00:00:00")).setActive(false).setNumValue(Integer.valueOf(107)));
	save(new_composite(SimpleCompositeEntity.class, sentity33, "COMPLEX1").setInitDate(date("2010-01-15 00:00:00")).setActive(false).setNumValue(Integer.valueOf(167)));
	save(new_composite(SimpleCompositeEntity.class, sentity33, "COMPLEX2").setInitDate(date("2010-01-16 00:00:00")).setActive(true).setNumValue(Integer.valueOf(189)));
	save(new_composite(SimpleCompositeEntity.class, sentity34, "COMPLEX1").setInitDate(date("2010-01-17 00:00:00")).setActive(false).setNumValue(Integer.valueOf(112)));
	save(new_composite(SimpleCompositeEntity.class, sentity34, "COMPLEX2").setInitDate(date("2010-01-18 00:00:00")).setActive(true).setNumValue(Integer.valueOf(143)));
	save(new_composite(SimpleCompositeEntity.class, sentity35, "COMPLEX1").setInitDate(date("2010-01-19 00:00:00")).setActive(false).setNumValue(Integer.valueOf(153)));
	save(new_composite(SimpleCompositeEntity.class, sentity35, "COMPLEX2").setInitDate(date("2010-01-20 00:00:00")).setActive(true).setNumValue(Integer.valueOf(107)));
	save(new_composite(SimpleCompositeEntity.class, sentity36, "COMPLEX1").setInitDate(date("2010-02-01 00:00:00")).setActive(true).setNumValue(Integer.valueOf(10)));
	save(new_composite(SimpleCompositeEntity.class, sentity36, "COMPLEX2").setInitDate(date("2010-02-02 00:00:00")).setActive(true).setNumValue(Integer.valueOf(17)));
	save(new_composite(SimpleCompositeEntity.class, sentity37, "COMPLEX1").setInitDate(date("2010-02-03 00:00:00")).setActive(false).setNumValue(Integer.valueOf(16)));
	save(new_composite(SimpleCompositeEntity.class, sentity37, "COMPLEX2").setInitDate(date("2010-02-04 00:00:00")).setActive(true).setNumValue(Integer.valueOf(18)));
	save(new_composite(SimpleCompositeEntity.class, sentity38, "COMPLEX1").setInitDate(date("2010-02-05 00:00:00")).setActive(false).setNumValue(Integer.valueOf(145)));
	save(new_composite(SimpleCompositeEntity.class, sentity38, "COMPLEX2").setInitDate(date("2010-02-06 00:00:00")).setActive(true).setNumValue(Integer.valueOf(234)));
	save(new_composite(SimpleCompositeEntity.class, sentity39, "COMPLEX1").setInitDate(date("2010-02-07 00:00:00")).setActive(true).setNumValue(Integer.valueOf(482)));
	save(new_composite(SimpleCompositeEntity.class, sentity39, "COMPLEX2").setInitDate(date("2010-02-08 00:00:00")).setActive(false).setNumValue(Integer.valueOf(256)));
	save(new_composite(SimpleCompositeEntity.class, sentity40, "COMPLEX1").setInitDate(date("2010-02-09 00:00:00")).setActive(true).setNumValue(Integer.valueOf(53)));
	save(new_composite(SimpleCompositeEntity.class, sentity40, "COMPLEX2").setInitDate(date("2010-02-10 00:00:00")).setActive(true).setNumValue(Integer.valueOf(76)));
	save(new_composite(SimpleCompositeEntity.class, sentity41, "COMPLEX1").setInitDate(date("2010-02-11 00:00:00")).setActive(true).setNumValue(Integer.valueOf(56)));
	save(new_composite(SimpleCompositeEntity.class, sentity41, "COMPLEX2").setInitDate(date("2010-02-12 00:00:00")).setActive(false).setNumValue(Integer.valueOf(23)));
	save(new_composite(SimpleCompositeEntity.class, sentity42, "COMPLEX1").setInitDate(date("2010-02-13 00:00:00")).setActive(true).setNumValue(Integer.valueOf(67)));
	save(new_composite(SimpleCompositeEntity.class, sentity42, "COMPLEX2").setInitDate(date("2010-02-14 00:00:00")).setActive(false).setNumValue(Integer.valueOf(933)));
	save(new_composite(SimpleCompositeEntity.class, sentity43, "COMPLEX1").setInitDate(date("2010-02-15 00:00:00")).setActive(true).setNumValue(Integer.valueOf(836)));
	save(new_composite(SimpleCompositeEntity.class, sentity43, "COMPLEX2").setInitDate(date("2010-02-16 00:00:00")).setActive(true).setNumValue(Integer.valueOf(833)));
	save(new_composite(SimpleCompositeEntity.class, sentity44, "COMPLEX1").setInitDate(date("2010-02-17 00:00:00")).setActive(false).setNumValue(Integer.valueOf(8337)));
	save(new_composite(SimpleCompositeEntity.class, sentity44, "COMPLEX2").setInitDate(date("2010-02-18 00:00:00")).setActive(false).setNumValue(Integer.valueOf(722)));
	save(new_composite(SimpleCompositeEntity.class, sentity45, "COMPLEX1").setInitDate(date("2010-02-19 00:00:00")).setActive(true).setNumValue(Integer.valueOf(468)));
	save(new_composite(SimpleCompositeEntity.class, sentity45, "COMPLEX2").setInitDate(date("2010-02-20 00:00:00")).setActive(true).setNumValue(Integer.valueOf(783)));
	save(new_composite(SimpleCompositeEntity.class, sentity46, "COMPLEX1").setInitDate(date("2010-03-01 00:00:00")).setActive(true).setNumValue(Integer.valueOf(13)));
	save(new_composite(SimpleCompositeEntity.class, sentity46, "COMPLEX2").setInitDate(date("2010-03-02 00:00:00")).setActive(false).setNumValue(Integer.valueOf(67)));
	save(new_composite(SimpleCompositeEntity.class, sentity47, "COMPLEX1").setInitDate(date("2010-03-03 00:00:00")).setActive(false).setNumValue(Integer.valueOf(93)));
	save(new_composite(SimpleCompositeEntity.class, sentity47, "COMPLEX2").setInitDate(date("2010-03-04 00:00:00")).setActive(true).setNumValue(Integer.valueOf(111)));
	save(new_composite(SimpleCompositeEntity.class, sentity48, "COMPLEX1").setInitDate(date("2010-03-05 00:00:00")).setActive(false).setNumValue(Integer.valueOf(126)));
	save(new_composite(SimpleCompositeEntity.class, sentity48, "COMPLEX2").setInitDate(date("2010-03-06 00:00:00")).setActive(false).setNumValue(Integer.valueOf(167)));
	save(new_composite(SimpleCompositeEntity.class, sentity49, "COMPLEX1").setInitDate(date("2010-03-07 00:00:00")).setActive(true).setNumValue(Integer.valueOf(783)));
	save(new_composite(SimpleCompositeEntity.class, sentity49, "COMPLEX2").setInitDate(date("2010-03-08 00:00:00")).setActive(false).setNumValue(Integer.valueOf(672)));
	save(new_composite(SimpleCompositeEntity.class, sentity50, "COMPLEX1").setInitDate(date("2010-03-09 00:00:00")).setActive(false).setNumValue(Integer.valueOf(788)));
	save(new_composite(SimpleCompositeEntity.class, sentity50, "COMPLEX2").setInitDate(date("2010-03-10 00:00:00")).setActive(true).setNumValue(Integer.valueOf(75)));
    }

    @Override
    protected List<Class<? extends AbstractEntity<?>>> domainEntityTypes() {
	return applicationDomainProvider.domainTypes();
    }

}
