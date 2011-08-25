package ua.com.fielden.platform.entity.query;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import junit.framework.TestCase;

import org.hibernate.Hibernate;
import org.hibernate.type.Type;

import ua.com.fielden.platform.dao.MappingsGenerator;
import ua.com.fielden.platform.domain.TgVehicle;
import ua.com.fielden.platform.domain.TgWorkOrder;
import ua.com.fielden.platform.entity.query.model.structure.QueryModelResult.ResultPropertyInfo;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.ioc.HibernateUserTypesModule;
import ua.com.fielden.platform.persistence.types.SimpleMoneyType;
import ua.com.fielden.platform.utils.Pair;

import com.google.inject.Injector;

public class EntityFetcherTestCase extends TestCase {
    final Injector injector = new ApplicationInjectorFactory().add(new HibernateUserTypesModule()).getInjector();
    private EntityResultTreeBuilder ef = new EntityResultTreeBuilder(new MappingsGenerator(new HashMap<Class, Class>(), injector));
    private EntityFetcher entFetcher = new EntityFetcher() {};

    public void test_entity_tree_for_plain_entity() throws Exception {
	final List<ResultPropertyInfo> propInfos = new ArrayList<ResultPropertyInfo>();
	propInfos.add(new ResultPropertyInfo("id", "C1", Long.class));
	propInfos.add(new ResultPropertyInfo("version", "C2", Long.class));
	propInfos.add(new ResultPropertyInfo("key", "C3", String.class));
	propInfos.add(new ResultPropertyInfo("desc", "C4", String.class));
	propInfos.add(new ResultPropertyInfo("initDate", "C5", Date.class));

	final EntityTree expEntTree = new EntityTree(TgVehicle.class);
	expEntTree.getSingles().put(new PropColumn("id", "C1", Hibernate.LONG, null), 0);
	expEntTree.getSingles().put(new PropColumn("version", "C2", Hibernate.LONG, null), 1);
	expEntTree.getSingles().put(new PropColumn("key", "C3", Hibernate.STRING, null), 2);
	expEntTree.getSingles().put(new PropColumn("desc", "C4", Hibernate.STRING, null), 3);
	expEntTree.getSingles().put(new PropColumn("initDate", "C5", Hibernate.TIMESTAMP, null), 4);

	final EntityTree actTree = ef.buildTree(TgVehicle.class, propInfos);
	assertEquals("Act entity tree differs from expected", expEntTree, actTree);

	final List<Pair<String, Type>> expScalarInfo = new ArrayList<Pair<String, Type>>();
	expScalarInfo.add(new Pair<String, Type>("C4", Hibernate.STRING));
	expScalarInfo.add(new Pair<String, Type>("C1", Hibernate.LONG));
	expScalarInfo.add(new Pair<String, Type>("C5", Hibernate.TIMESTAMP));
	expScalarInfo.add(new Pair<String, Type>("C3", Hibernate.STRING));
	expScalarInfo.add(new Pair<String, Type>("C2", Hibernate.LONG));

	assertEquals("Act entity scalar info differs from expected", expScalarInfo, entFetcher.getScalarInfo(actTree));
    }

    public void test_entity_tree_for_plain_entity_with_custom_user_type_prop() throws Exception {
	final List<ResultPropertyInfo> propInfos = new ArrayList<ResultPropertyInfo>();
	propInfos.add(new ResultPropertyInfo("id", "C1", Long.class));
	propInfos.add(new ResultPropertyInfo("version", "C2", Long.class));
	propInfos.add(new ResultPropertyInfo("key", "C3", String.class));
	propInfos.add(new ResultPropertyInfo("desc", "C4", String.class));
	propInfos.add(new ResultPropertyInfo("purchasePrice.amount", "C5", BigDecimal.class));

	final EntityTree expMoneyTree = new EntityTree(SimpleMoneyType.class);
	expMoneyTree.getSingles().put(new PropColumn("amount", "C5", Hibernate.BIG_DECIMAL, null), 4);
	final EntityTree expEntTree = new EntityTree(TgVehicle.class);
	expEntTree.getSingles().put(new PropColumn("id", "C1", Hibernate.LONG, null), 0);
	expEntTree.getSingles().put(new PropColumn("version", "C2", Hibernate.LONG, null), 1);
	expEntTree.getSingles().put(new PropColumn("key", "C3", Hibernate.STRING, null), 2);
	expEntTree.getSingles().put(new PropColumn("desc", "C4", Hibernate.STRING, null), 3);
	expEntTree.getComposites().put("purchasePrice", expMoneyTree);

	final EntityTree actTree = ef.buildTree(TgVehicle.class, propInfos);
	assertEquals("Act entity tree differs from expected", expEntTree, actTree);

	final List<Pair<String, Type>> expScalarInfo = new ArrayList<Pair<String, Type>>();
	expScalarInfo.add(new Pair<String, Type>("C4", Hibernate.STRING));
	expScalarInfo.add(new Pair<String, Type>("C1", Hibernate.LONG));
	expScalarInfo.add(new Pair<String, Type>("C3", Hibernate.STRING));
	expScalarInfo.add(new Pair<String, Type>("C2", Hibernate.LONG));
	expScalarInfo.add(new Pair<String, Type>("C5", Hibernate.BIG_DECIMAL));

	assertEquals("Act entity scalar info differs from expected", expScalarInfo, entFetcher.getScalarInfo(actTree));
    }

    public void test_entity_tree_for_entity_with_entity_property() throws Exception {
	final List<ResultPropertyInfo> propInfos = new ArrayList<ResultPropertyInfo>();
	propInfos.add(new ResultPropertyInfo("vehicle.id", "C1", Long.class));
	propInfos.add(new ResultPropertyInfo("vehicle.version", "C2", Long.class));
	propInfos.add(new ResultPropertyInfo("vehicle.key", "C3", String.class));
	propInfos.add(new ResultPropertyInfo("vehicle.desc", "C4", String.class));
	propInfos.add(new ResultPropertyInfo("vehicle.initDate", "C5", Date.class));
	propInfos.add(new ResultPropertyInfo("id", "C6", Long.class));
	propInfos.add(new ResultPropertyInfo("version", "C7", Long.class));
	propInfos.add(new ResultPropertyInfo("key", "C8", String.class));
	propInfos.add(new ResultPropertyInfo("desc", "C9", String.class));

	final EntityTree expVehTree = new EntityTree(TgVehicle.class);
	expVehTree.getSingles().put(new PropColumn("id", "C1", Hibernate.LONG, null), 4);
	expVehTree.getSingles().put(new PropColumn("version", "C2", Hibernate.LONG, null), 5);
	expVehTree.getSingles().put(new PropColumn("key", "C3", Hibernate.STRING, null), 6);
	expVehTree.getSingles().put(new PropColumn("desc", "C4", Hibernate.STRING, null), 7);
	expVehTree.getSingles().put(new PropColumn("initDate", "C5", Hibernate.TIMESTAMP, null), 8);

	final EntityTree expEntTree = new EntityTree(TgWorkOrder.class);
	expEntTree.getSingles().put(new PropColumn("id", "C6", Hibernate.LONG, null), 0);
	expEntTree.getSingles().put(new PropColumn("version", "C7", Hibernate.LONG, null), 1);
	expEntTree.getSingles().put(new PropColumn("key", "C8", Hibernate.STRING, null), 2);
	expEntTree.getSingles().put(new PropColumn("desc", "C9", Hibernate.STRING, null), 3);
	expEntTree.getComposites().put("vehicle", expVehTree);
	final EntityTree actTree = ef.buildTree(TgWorkOrder.class, propInfos);
	assertEquals("Act entity tree differs from expected", expEntTree, actTree);
    }

    public void test_entity_tree_for_entity_with_nested_entity_properties() throws Exception {
	final List<ResultPropertyInfo> propInfos = new ArrayList<ResultPropertyInfo>();
	propInfos.add(new ResultPropertyInfo("vehicle.id", "C1", Long.class));
	propInfos.add(new ResultPropertyInfo("vehicle.version", "C2", Long.class));
	propInfos.add(new ResultPropertyInfo("vehicle.key", "C3", String.class));
	propInfos.add(new ResultPropertyInfo("vehicle.desc", "C4", String.class));
	propInfos.add(new ResultPropertyInfo("vehicle.initDate", "C5", Date.class));
	propInfos.add(new ResultPropertyInfo("vehicle.replacedBy.id", "C6", Long.class));
	propInfos.add(new ResultPropertyInfo("vehicle.replacedBy.version", "C7", Long.class));
	propInfos.add(new ResultPropertyInfo("vehicle.replacedBy.key", "C8", String.class));
	propInfos.add(new ResultPropertyInfo("vehicle.replacedBy.desc", "C9", String.class));
	propInfos.add(new ResultPropertyInfo("vehicle.replacedBy.initDate", "C10", Date.class));

	propInfos.add(new ResultPropertyInfo("id", "C11", Long.class));
	propInfos.add(new ResultPropertyInfo("version", "C12", Long.class));
	propInfos.add(new ResultPropertyInfo("key", "C13", String.class));
	propInfos.add(new ResultPropertyInfo("desc", "C14", String.class));

	final EntityTree expReplacedByVehTree = new EntityTree(TgVehicle.class);
	expReplacedByVehTree.getSingles().put(new PropColumn("id", "C6", Hibernate.LONG, null), 9);
	expReplacedByVehTree.getSingles().put(new PropColumn("version", "C7", Hibernate.LONG, null), 10);
	expReplacedByVehTree.getSingles().put(new PropColumn("key", "C8", Hibernate.STRING, null), 11);
	expReplacedByVehTree.getSingles().put(new PropColumn("desc", "C9", Hibernate.STRING, null), 12);
	expReplacedByVehTree.getSingles().put(new PropColumn("initDate", "C10", Hibernate.TIMESTAMP, null), 13);

	final EntityTree expVehTree = new EntityTree(TgVehicle.class);
	expVehTree.getSingles().put(new PropColumn("id", "C1", Hibernate.LONG, null), 4);
	expVehTree.getSingles().put(new PropColumn("version", "C2", Hibernate.LONG, null), 5);
	expVehTree.getSingles().put(new PropColumn("key", "C3", Hibernate.STRING, null), 6);
	expVehTree.getSingles().put(new PropColumn("desc", "C4", Hibernate.STRING, null), 7);
	expVehTree.getSingles().put(new PropColumn("initDate", "C5", Hibernate.TIMESTAMP, null), 8);
	expVehTree.getComposites().put("replacedBy", expReplacedByVehTree);

	final EntityTree expEntTree = new EntityTree(TgWorkOrder.class);
	expEntTree.getSingles().put(new PropColumn("id", "C11", Hibernate.LONG, null), 0);
	expEntTree.getSingles().put(new PropColumn("version", "C12", Hibernate.LONG, null), 1);
	expEntTree.getSingles().put(new PropColumn("key", "C13", Hibernate.STRING, null), 2);
	expEntTree.getSingles().put(new PropColumn("desc", "C14", Hibernate.STRING, null), 3);
	expEntTree.getComposites().put("vehicle", expVehTree);
	final EntityTree actTree = ef.buildTree(TgWorkOrder.class, propInfos);
	assertEquals("Act entity tree differs from expected", expEntTree, actTree);
    }
}
