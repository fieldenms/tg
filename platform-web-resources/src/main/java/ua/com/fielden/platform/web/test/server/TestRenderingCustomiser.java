package ua.com.fielden.platform.web.test.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.sample.domain.TgPersistentStatus;
import ua.com.fielden.platform.web.centre.api.resultset.CssRenderingCustomiser;

public class TestRenderingCustomiser extends CssRenderingCustomiser {
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
    public Map<String, Object> getCustomRenderingFor(final AbstractEntity<?> entity) {
        final Map<String, Object> res = new HashMap<>();
        for (final String propName : properties) {
            final Map<String, Map<String, String>> propStyle = new HashMap<>();
            final Map<String, String> backgroundStyles = new HashMap<>();
            final Map<String, String> valueStyles = new HashMap<>();
            propStyle.put("backgroundStyles", backgroundStyles);
            propStyle.put("valueStyles", valueStyles);
            backgroundStyles.put("background-color", "palegreen");
            if (propName.equals("integerProp")) {
                valueStyles.put("color", "blue");
                final int value = entity.get("integerProp") == null ? 999 : ((Integer) entity.get("integerProp")).intValue();
                if (value > 60 && value < 100) {
                    valueStyles.put("visibility", "hidden");
                }
            }
            res.put(propName, propStyle);
        }

        final TgPersistentStatus currentStatus = entity.get("status");
        for (final Map.Entry<String, String> entry : statusColouringScheme.entrySet()) {

            final Map<String, Map<String, String>> propStyle = new HashMap<>();
            res.put(entry.getKey(), propStyle);

            final Map<String, String> backgroundStyles = new HashMap<>();
            final Map<String, String> valueStyles = new HashMap<>();
            propStyle.put("backgroundStyles", backgroundStyles);
            propStyle.put("valueStyles", valueStyles);

            // TODO uncomment to hide the "X" value in calculated properties, which is really needed only for data export
            //valueStyles.put("visibility", "hidden");
            if (currentStatus != null && entry.getKey().equalsIgnoreCase(currentStatus.getKey())) {
               backgroundStyles.put("background-color", entry.getValue());
            }
        }

        return res;
    }

}
