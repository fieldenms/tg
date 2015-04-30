package ua.com.fielden.platform.web.test.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import ua.com.fielden.platform.sample.domain.TgPersistentEntityWithProperties;
import ua.com.fielden.platform.web.centre.api.resultset.IRenderingCustomiser;

public class TestRenderingCustomiser implements IRenderingCustomiser<TgPersistentEntityWithProperties, Map<String, Map<String, String>>> {

    private final List<String> properties = new ArrayList<String>() {
        {
            add("integerProp");
            add("bigDecimalProp");
            add("entityProp");
            add("booleanProp");
            add("dateProp");
            add("compositeProp");
            add("stringProp");
        }
    };

    @Override
    public Optional<Map<String, Map<String, String>>> getCustomRenderingFor(final TgPersistentEntityWithProperties entity) {
        final Map<String, Map<String, String>> res = new HashMap<String, Map<String, String>>();
        for (final String propName : properties) {
            final Map<String, String> propStyle = new HashMap<String, String>();
            propStyle.put("background-color", "green");
            res.put(propName, propStyle);
        }
        return Optional.of(res);
    };
}
