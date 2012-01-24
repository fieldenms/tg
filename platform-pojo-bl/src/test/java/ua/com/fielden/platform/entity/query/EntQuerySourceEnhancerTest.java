package ua.com.fielden.platform.entity.query;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import ua.com.fielden.platform.entity.query.model.elements.EntQuery.PropTree;
import ua.com.fielden.platform.entity.query.model.elements.EntQuerySourcesEnhancer;
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

    protected Set<String> set(final String ... strings) {
	return new HashSet<String>(Arrays.asList(strings));
    }

    protected Set<PropTree> set(final PropTree... objects) {
	return new HashSet<PropTree>(Arrays.asList(objects));
    }

    @Test
    public void test1() {
	final String[] props = new String[]{"model.make.key", "station.parent.parent", "desc", "price", "replacedBy.station"};
	final Map<String, Set<String>> exp = new HashMap<String, Set<String>>();
	exp.put("model", set("make.key"));
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
	exp.add(new PropTree("replacedBy", "replacedBy", TgVehicle.class, true, set(new PropTree("station", "replacedBy.station", TgOrgUnit5.class, true, emptyPropTreeSet))));
	exp.add(new PropTree("model", "model", TgVehicleModel.class, false, set(new PropTree("make", "model.make", TgVehicleMake.class, true, emptyPropTreeSet))));
	exp.add(new PropTree("station", "station", TgOrgUnit5.class, true, set(new PropTree("parent", "station.parent", TgOrgUnit4.class, true, set(new PropTree("parent", "station.parent.parent", TgOrgUnit3.class, true, emptyPropTreeSet))))));

	assertEquals("Incorrect set", exp, qse.doS(TgVehicle.class, null, false, set(props)));
    }

    @Test
    public void test2a() {
	final String[] props = new String[]{"model.make.key", "station.parent.parent.key", "desc", "price", "replacedBy.station.key"};
	final Set<PropTree> exp = new HashSet<PropTree>();
	exp.add(new PropTree("replacedBy", "v.replacedBy", TgVehicle.class, true, set(new PropTree("station", "v.replacedBy.station", TgOrgUnit5.class, true, emptyPropTreeSet))));
	exp.add(new PropTree("model", "v.model", TgVehicleModel.class, true, set(new PropTree("make", "v.model.make", TgVehicleMake.class, true, emptyPropTreeSet))));
	exp.add(new PropTree("station", "v.station", TgOrgUnit5.class, true, set(new PropTree("parent", "v.station.parent", TgOrgUnit4.class, true, set(new PropTree("parent", "v.station.parent.parent", TgOrgUnit3.class, true, emptyPropTreeSet))))));

	assertEquals("Incorrect set", exp, qse.doS(TgVehicle.class, "v", true, set(props)));
    }

    @Test
    public void test3() {
	final String[] props = new String[]{"model.make.key", "station.parent.parent", "desc", "price", "replacedBy.station"};
	final Set<PropTree> exp = new HashSet<PropTree>();
	exp.add(new PropTree("replacedBy", "replacedBy", TgVehicle.class, true, emptyPropTreeSet));
	exp.add(new PropTree("model", "model", TgVehicleModel.class, false, set(new PropTree("make", "model.make", TgVehicleMake.class, true, emptyPropTreeSet))));
	exp.add(new PropTree("station", "station", TgOrgUnit5.class, true, set(new PropTree("parent", "station.parent", TgOrgUnit4.class, true, emptyPropTreeSet))));

	assertEquals("Incorrect set", exp, qse.doS(TgVehicle.class, null, false, set(props)));
    }

    @Test
    public void test4() {
	final String[] props = new String[]{"model.make.key", "station.parent.parent", "desc", "price", "replacedBy.model.key"};
	final Set<PropTree> exp = new HashSet<PropTree>();
	exp.add(new PropTree("replacedBy", "replacedBy", TgVehicle.class, true, set(new PropTree("model", "replacedBy.model", TgVehicleModel.class, true, emptyPropTreeSet))));
	exp.add(new PropTree("model", "model", TgVehicleModel.class, false, set(new PropTree("make", "model.make", TgVehicleMake.class, true, emptyPropTreeSet))));
	exp.add(new PropTree("station", "station", TgOrgUnit5.class, true, set(new PropTree("parent", "station.parent", TgOrgUnit4.class, true, emptyPropTreeSet))));

	assertEquals("Incorrect set", exp, qse.doS(TgVehicle.class, null, false, set(props)));
    }
}