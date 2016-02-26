package ua.com.fielden.platform.web.test.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.sample.domain.TgPersistentStatus;
import ua.com.fielden.platform.web.centre.api.resultset.IRenderingCustomiser;

public class TestRenderingCustomiser implements IRenderingCustomiser<AbstractEntity<?>, Map<String, Map<String, String>>> {
    private final Map<String, String> statusColouringScheme = new HashMap<String, String>() {
        {
            put("dR", "yellow");
            put("iS", "green");
            put("iR", "red");
            put("oN", "yellow");
            put("sR", "yellow");
        }
    };
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
    public Optional<Map<String, Map<String, String>>> getCustomRenderingFor(final AbstractEntity<?> entity) {
        final Map<String, Map<String, String>> res = new HashMap<String, Map<String, String>>();
        for (final String propName : properties) {
            final Map<String, String> propStyle = new HashMap<String, String>();
            propStyle.put("background-color", "palegreen");
            res.put(propName, propStyle);
        }

        final TgPersistentStatus currentStatus = entity.get("status");
        for (final Map.Entry<String, String> entry : statusColouringScheme.entrySet()) {
            if (currentStatus != null && entry.getKey().equalsIgnoreCase(currentStatus.getKey())) {
                res.put(entry.getKey(), new HashMap<String, String>() {
                    {
                        put("background-color", entry.getValue());
                    }
                });
            }
        }
        return Optional.of(res);
    };
}
