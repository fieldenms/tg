package ua.com.fielden.platform.example.swing.egi.performance;

import java.util.HashMap;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.utils.Pair;

@EntityTitle("Example entity type")
@KeyType(String.class)
@KeyTitle(value = "Example entity", desc = "Example entity description")
@DescTitle(value = "Description", desc = "Example entity description")
public class ExampleEntitySimplified extends AbstractEntity<String> {

    private static final long serialVersionUID = -9210142074284483589L;

    public static final Map<String, Pair<String, String>> metaData = new HashMap<String, Pair<String,String>>();

    public static final Map<String, Class<?>> types = new HashMap<String, Class<?>>();

    static {
	metaData.put("", new Pair<String, String>("Example entity", "Example entity description"));
	metaData.put("desc", new Pair<String, String>("Description", "Example entity description"));
	metaData.put("initDate", new Pair<String, String>("Init. date", "Date of initiation"));
	metaData.put("stringProperty", new Pair<String, String>("String property", "String property description"));
	metaData.put("active", new Pair<String, String>("active", "determines the activity of simple entity."));
	metaData.put("numValue", new Pair<String, String>("Num. value", "Number value"));
	metaData.put("nestedEntity", new Pair<String, String>("Example entity", "Example entity description"));
	metaData.put("nestedEntity.desc", new Pair<String, String>("Description", "Example entity description"));
	metaData.put("nestedEntity.initDate", new Pair<String, String>("Init. date", "Date of initiation"));
	metaData.put("nestedEntity.stringProperty", new Pair<String, String>("String property", "String property description"));
	metaData.put("nestedEntity.active", new Pair<String, String>("active", "determines the activity of simple entity."));
	metaData.put("nestedEntity.numValue", new Pair<String, String>("Num. value", "Number value"));
	metaData.put("nestedEntity.nestedEntity.nestedEntity", new Pair<String, String>("Example entity", "Example entity description"));
	metaData.put("nestedEntity.nestedEntity.nestedEntity.desc", new Pair<String, String>("Description", "Example entity description"));
	metaData.put("nestedEntity.nestedEntity.nestedEntity.initDate", new Pair<String, String>("Init. date", "Date of initiation"));
	metaData.put("nestedEntity.nestedEntity.nestedEntity.stringProperty", new Pair<String, String>("String property", "String property description"));
	metaData.put("nestedEntity.nestedEntity.nestedEntity.active", new Pair<String, String>("active", "determines the activity of simple entity."));
	metaData.put("nestedEntity.nestedEntity.nestedEntity.numValue", new Pair<String, String>("Num. value", "Number value"));

	types.put("", String.class);
	types.put("desc", String.class);
	types.put("initDate", String.class);
	types.put("stringProperty", String.class);
	types.put("active", Boolean.class);
	types.put("numValue", String.class);
	types.put("nestedEntity", String.class);
	types.put("nestedEntity.desc", String.class);
	types.put("nestedEntity.initDate", String.class);
	types.put("nestedEntity.stringProperty", String.class);
	types.put("nestedEntity.active", Boolean.class);
	types.put("nestedEntity.numValue", String.class);
	types.put("nestedEntity.nestedEntity.nestedEntity", String.class);
	types.put("nestedEntity.nestedEntity.nestedEntity.desc", String.class);
	types.put("nestedEntity.nestedEntity.nestedEntity.initDate", String.class);
	types.put("nestedEntity.nestedEntity.nestedEntity.stringProperty", String.class);
	types.put("nestedEntity.nestedEntity.nestedEntity.active", Boolean.class);
	types.put("nestedEntity.nestedEntity.nestedEntity.numValue", String.class);
    }

    private final Map<String, Object> data = new HashMap<String, Object>();

    public ExampleEntitySimplified() {
	data.put("", "test");
	data.put("desc", "test description");
	data.put("initDate", "23/01/2012 12:00 AM");
	data.put("stringProperty", "string value");
	data.put("active", true);
	data.put("numValue", "35");
	data.put("nestedEntity", "test");
	data.put("nestedEntity.desc", "test description");
	data.put("nestedEntity.initDate", "23/01/2012 12:00 AM");
	data.put("nestedEntity.stringProperty", "string value");
	data.put("nestedEntity.active", true);
	data.put("nestedEntity.numValue", "35");
	data.put("nestedEntity.nestedEntity.nestedEntity", "test");
	data.put("nestedEntity.nestedEntity.nestedEntity.desc", "test description");
	data.put("nestedEntity.nestedEntity.nestedEntity.initDate", "23/01/2012 12:00 AM");
	data.put("nestedEntity.nestedEntity.nestedEntity.stringProperty", "string value");
	data.put("nestedEntity.nestedEntity.nestedEntity.active", true);
	data.put("nestedEntity.nestedEntity.nestedEntity.numValue", "35");
    }

    @Override
    public Object get(final String propertyName) {
        return data.get(propertyName);
    }

    @Override
    public void set(final String propertyName, final Object value) {
	data.put(propertyName, value);
    }
}
