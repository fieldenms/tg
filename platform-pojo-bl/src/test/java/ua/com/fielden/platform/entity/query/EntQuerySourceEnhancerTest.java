package ua.com.fielden.platform.entity.query;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.model.elements.EntQuery.PropTree;
import ua.com.fielden.platform.entity.query.model.elements.EntQuerySourceAsEntity;
import ua.com.fielden.platform.entity.query.model.elements.EntQuerySourcesEnhancer;
import ua.com.fielden.platform.entity.query.model.elements.IEntQuerySource;
import ua.com.fielden.platform.sample.domain.TgOrgUnit3;
import ua.com.fielden.platform.sample.domain.TgOrgUnit4;
import ua.com.fielden.platform.sample.domain.TgOrgUnit5;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.sample.domain.TgVehicleMake;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import static org.junit.Assert.assertEquals;


public class EntQuerySourceEnhancerTest {
    protected final EntQuerySourcesEnhancer qse = new EntQuerySourcesEnhancer();

    protected final Set<String> emptyStringSet = Collections.emptySet();

    protected final Set<PropTree> emptyPropTreeSet = Collections.emptySet();

    protected EntQuerySourceAsEntity source(final String alias, final Class<? extends AbstractEntity> entityType) {
	return new EntQuerySourceAsEntity(entityType, alias, true);
    }

    protected Set<String> set(final String ... strings) {
	return new HashSet<String>(Arrays.asList(strings));
    }

    protected Set<PropTree> set(final PropTree... objects) {
	return new HashSet<PropTree>(Arrays.asList(objects));
    }

    protected IEntQuerySource getProvider(final Class entityType, final String alias) {
	return new EntQuerySourceAsEntity(entityType, alias);
    }

    @Test
    public void test1() {
	final String[] props = new String[]{"model.make.key", "model.make.desc", "station.parent.parent", "desc", "price", "replacedBy.station"};
	final Map<String, Set<String>> exp = new HashMap<String, Set<String>>();
	exp.put("model", set("make.key", "make.desc"));
	exp.put("station", set("parent.parent"));
	exp.put("desc", emptyStringSet);
	exp.put("price", emptyStringSet);
	exp.put("replacedBy", set("station"));

	assertEquals("Incorrect set", exp, qse.determinePropGroups(set(props)));
    }

    @Test
    public void test2() {
	final String[] props = new String[]{"model.make.key", "station.parent.parent.key", "desc", "price", "replacedBy.station.key"};
	final Set<PropTree> exp = new HashSet<PropTree>();
	exp.add(new PropTree(source("replacedBy", TgVehicle.class), true, set(new PropTree(source("replacedBy.station", TgOrgUnit5.class), true, emptyPropTreeSet, null)), null));
	exp.add(new PropTree(source("model", TgVehicleModel.class), false, set(new PropTree(source("model.make", TgVehicleMake.class), true, emptyPropTreeSet, null)), null));
	exp.add(new PropTree(source("station", TgOrgUnit5.class), true, set(new PropTree(source("station.parent", TgOrgUnit4.class), true, set(new PropTree(source("station.parent.parent", TgOrgUnit3.class), true, emptyPropTreeSet, null)), null)), null));

	assertEquals("Incorrect set", exp, qse.produceSourcesTree(getProvider(TgVehicle.class, null), false, set(props), null));
    }

    @Test
    public void test2a() {
	final String[] props = new String[]{"model.make.key", "station.parent.parent.key", "desc", "price", "replacedBy.station.key"};
	final Set<PropTree> exp = new HashSet<PropTree>();
	exp.add(new PropTree(source("v.replacedBy", TgVehicle.class), true, set(new PropTree(source("v.replacedBy.station", TgOrgUnit5.class), true, emptyPropTreeSet, null)), null));
	exp.add(new PropTree(source("v.model", TgVehicleModel.class), true, set(new PropTree(source("v.model.make", TgVehicleMake.class), true, emptyPropTreeSet, null)), null));
	exp.add(new PropTree(source("v.station", TgOrgUnit5.class), true, set(new PropTree(source("v.station.parent", TgOrgUnit4.class), true, set(new PropTree(source("v.station.parent.parent", TgOrgUnit3.class), true, emptyPropTreeSet, null)), null)), null));

	assertEquals("Incorrect set", exp, qse.produceSourcesTree(getProvider(TgVehicle.class, "v"), true, set(props), null));
    }

    @Test
    public void test3() {
	final String[] props = new String[]{"model.make.key", "station.parent.parent", "desc", "price", "replacedBy.station"};
	final Set<PropTree> exp = new HashSet<PropTree>();
	exp.add(new PropTree(source("replacedBy", TgVehicle.class), true, emptyPropTreeSet, null));
	exp.add(new PropTree(source("model", TgVehicleModel.class), false, set(new PropTree(source("model.make", TgVehicleMake.class), true, emptyPropTreeSet, null)), null));
	exp.add(new PropTree(source("station", TgOrgUnit5.class), true, set(new PropTree(source("station.parent", TgOrgUnit4.class), true, emptyPropTreeSet, null)), null));

	assertEquals("Incorrect set", exp, qse.produceSourcesTree(getProvider(TgVehicle.class, null), false, set(props), null));
    }

    @Test
    public void test4() {
	final String[] props = new String[]{"model.make.key", "station.parent.parent", "desc", "price", "replacedBy.model.key"};
	final Set<PropTree> exp = new HashSet<PropTree>();
	exp.add(new PropTree(source("replacedBy", TgVehicle.class), true, set(new PropTree(source("replacedBy.model", TgVehicleModel.class), true, emptyPropTreeSet, null)), null));
	exp.add(new PropTree(source("model", TgVehicleModel.class), false, set(new PropTree(source("model.make", TgVehicleMake.class), true, emptyPropTreeSet, null)), null));
	exp.add(new PropTree(source("station", TgOrgUnit5.class), true, set(new PropTree(source("station.parent", TgOrgUnit4.class), true, emptyPropTreeSet, null)), null));

	assertEquals("Incorrect set", exp, qse.produceSourcesTree(getProvider(TgVehicle.class, null), false, set(props), null));
    }
}