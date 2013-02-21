package ua.com.fielden.platform.migration;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.junit.Test;

import ua.com.fielden.platform.migration.AbstractRetriever.Container;
import ua.com.fielden.platform.sample.domain.TgAuthor;
import ua.com.fielden.platform.sample.domain.TgAuthorship;
import ua.com.fielden.platform.sample.domain.TgOrgUnit1;
import ua.com.fielden.platform.sample.domain.TgOrgUnit2;
import ua.com.fielden.platform.sample.domain.TgOrgUnit3;
import ua.com.fielden.platform.sample.domain.TgOrgUnit4;
import ua.com.fielden.platform.sample.domain.TgPersonName;
import ua.com.fielden.platform.sample.domain.TgVehicleMake;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import ua.com.fielden.platform.utils.Pair;
import static org.junit.Assert.assertEquals;

public class AbstractRetrieverTest {

    @Test
    public void testCreateContainers1() {
	final SortedMap<String, Pair<Class, Integer>> props = new TreeMap<String, Pair<Class, Integer>>();
	props.put("id", new Pair<Class, Integer>(Long.class, 0));
	props.put("version", new Pair<Class, Integer>(Long.class, 1));
	props.put("key", new Pair<Class, Integer>(String.class, 2));
	props.put("desc", new Pair<Class, Integer>(String.class, 3));

	final Map<String, Container> result = AbstractRetriever.createContainers(props, TgVehicleMake.class);

	final Map<String, Container> exp = new HashMap<String, Container>();
	exp.put("id", new Container("id", Long.class, 0));
	exp.put("version", new Container("version", Long.class, 1));
	exp.put("key", new Container("key", String.class, 2));
	exp.put("desc", new Container("desc", String.class, 3));

	assertEquals(exp, result);
    }

    @Test
    public void testCreateContainers2() {
	final SortedMap<String, Pair<Class, Integer>> props = new TreeMap<String, Pair<Class, Integer>>();
	props.put("id", new Pair<Class, Integer>(Long.class, 0));
	props.put("version", new Pair<Class, Integer>(Long.class, 1));
	props.put("key", new Pair<Class, Integer>(String.class, 2));
	props.put("desc", new Pair<Class, Integer>(String.class, 3));
	props.put("make", new Pair<Class, Integer>(TgVehicleMake.class, 4));

	final Map<String, Container> result = AbstractRetriever.createContainers(props, TgVehicleModel.class);

	final Map<String, Container> exp = new HashMap<String, Container>();
	exp.put("id", new Container("id", Long.class, 0));
	exp.put("version", new Container("version", Long.class, 1));
	exp.put("key", new Container("key", String.class, 2));
	exp.put("desc", new Container("desc", String.class, 3));
	exp.put("make", new Container("make", TgVehicleMake.class, 4));

	assertEquals(exp, result);
    }

    @Test
    public void testCreateContainers3() {
	final SortedMap<String, Pair<Class, Integer>> props = new TreeMap<String, Pair<Class, Integer>>();
	props.put("id", new Pair<Class, Integer>(Long.class, 0));
	props.put("version", new Pair<Class, Integer>(Long.class, 1));
	props.put("name", new Pair<Class, Integer>(TgPersonName.class, 2));
	props.put("surname", new Pair<Class, Integer>(String.class, 3));

	final Map<String, Container> result = AbstractRetriever.createContainers(props, TgAuthor.class);

	final Map<String, Container> exp = new HashMap<String, Container>();
	exp.put("id", new Container("id", Long.class, 0));
	exp.put("version", new Container("version", Long.class, 1));
	exp.put("name", new Container("name", TgPersonName.class, 2));
	exp.put("surname", new Container("surname", String.class, 3));

	assertEquals(exp, result);
    }

    @Test
    public void testCreateContainers4() {
	final SortedMap<String, Pair<Class, Integer>> props = new TreeMap<String, Pair<Class, Integer>>();
	props.put("id", new Pair<Class, Integer>(Long.class, 0));
	props.put("version", new Pair<Class, Integer>(Long.class, 1));
	props.put("author.name", new Pair<Class, Integer>(TgPersonName.class, 2));
	props.put("author.surname", new Pair<Class, Integer>(String.class, 3));
	props.put("author.born", new Pair<Class, Integer>(Date.class, 5));
	props.put("title", new Pair<Class, Integer>(String.class, 4));

	final Map<String, Container> result = AbstractRetriever.createContainers(props, TgAuthorship.class);

	final Map<String, Container> expAuthor = new HashMap<String, Container>();
	expAuthor.put("name", new Container("name", TgPersonName.class, 2));
	expAuthor.put("surname", new Container("surname", String.class, 3));
	expAuthor.put("born", new Container("born", Date.class, 5));

	final Map<String, Container> exp = new HashMap<String, Container>();
	exp.put("id", new Container("id", Long.class, 0));
	exp.put("version", new Container("version", Long.class, 1));
	exp.put("author", new Container("author", TgAuthor.class, expAuthor));
	exp.put("title", new Container("title", String.class, 4));

	assertEquals(exp, result);
    }

    @Test
    public void testCreateContainers5() {
	final SortedMap<String, Pair<Class, Integer>> props = new TreeMap<String, Pair<Class, Integer>>();
	props.put("id", new Pair<Class, Integer>(Long.class, 0));
	props.put("version", new Pair<Class, Integer>(Long.class, 1));
	props.put("parent.parent", new Pair<Class, Integer>(TgOrgUnit1.class, 2));
	props.put("parent.name", new Pair<Class, Integer>(String.class, 3));
	props.put("name", new Pair<Class, Integer>(String.class, 4));

	final Map<String, Container> result = AbstractRetriever.createContainers(props, TgOrgUnit3.class);

	final Map<String, Container> expParent2 = new HashMap<String, Container>();
	expParent2.put("parent", new Container("parent", TgOrgUnit1.class, 2));
	expParent2.put("name", new Container("name", String.class, 3));

	final Map<String, Container> exp = new HashMap<String, Container>();
	exp.put("id", new Container("id", Long.class, 0));
	exp.put("version", new Container("version", Long.class, 1));
	exp.put("parent", new Container("parent", TgOrgUnit2.class, expParent2));
	exp.put("name", new Container("name", String.class, 4));

	assertEquals(exp, result);
    }

    @Test
    public void testCreateContainers6() {
	final SortedMap<String, Pair<Class, Integer>> props = new TreeMap<String, Pair<Class, Integer>>();
	props.put("id", new Pair<Class, Integer>(Long.class, 0));
	props.put("version", new Pair<Class, Integer>(Long.class, 1));
	props.put("parent.parent.parent", new Pair<Class, Integer>(TgOrgUnit1.class, 2));
	props.put("parent.parent.name", new Pair<Class, Integer>(String.class, 3));
	props.put("parent.name", new Pair<Class, Integer>(String.class, 4));
	props.put("name", new Pair<Class, Integer>(String.class, 5));

	final Map<String, Container> result = AbstractRetriever.createContainers(props, TgOrgUnit4.class);

	final Map<String, Container> expParent2 = new HashMap<String, Container>();
	expParent2.put("parent", new Container("parent", TgOrgUnit1.class, 2));
	expParent2.put("name", new Container("name", String.class, 3));

	final Map<String, Container> expParent3 = new HashMap<String, Container>();
	expParent3.put("parent", new Container("parent", TgOrgUnit2.class, expParent2));
	expParent3.put("name", new Container("name", String.class, 4));

	final Map<String, Container> exp = new HashMap<String, Container>();
	exp.put("id", new Container("id", Long.class, 0));
	exp.put("version", new Container("version", Long.class, 1));
	exp.put("parent", new Container("parent", TgOrgUnit3.class, expParent3));
	exp.put("name", new Container("name", String.class, 5));

	assertEquals(exp, result);
    }
}