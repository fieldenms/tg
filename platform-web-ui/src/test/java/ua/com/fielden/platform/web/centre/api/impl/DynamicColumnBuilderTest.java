package ua.com.fielden.platform.web.centre.api.impl;

import org.junit.Test;
import ua.com.fielden.platform.sample.domain.TgCollectionalSerialisationParent;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static ua.com.fielden.platform.web.centre.api.impl.DynamicColumn.DYN_COL_GROUP_PROP_VALUE;
import static ua.com.fielden.platform.web.centre.api.impl.DynamicColumn.DYN_COL_WORD_WRAP;
import static ua.com.fielden.platform.web.centre.api.impl.DynamicColumnBuilder.forProperty;

/// Tests for the dynamic-column builder, focused on `withWordWrap()` introduced for issue #2721.
///
public class DynamicColumnBuilderTest {

    @Test
    public void wordWrap_is_disabled_for_a_dynamic_column_by_default() {
        final List<Map<String, Object>> built = forProperty(TgCollectionalSerialisationParent.class, "collProp")
                .withGroupProp("key2")
                .withDisplayProp("desc")
                .addColumn("col-1").title("Column 1")
                .done()
                .build();

        assertEquals(1, built.size());
        assertEquals(false, built.getFirst().get(DYN_COL_WORD_WRAP));
    }

    @Test
    public void withWordWrap_enables_word_wrap_for_a_dynamic_column() {
        final List<Map<String, Object>> built = forProperty(TgCollectionalSerialisationParent.class, "collProp")
                .withGroupProp("key2")
                .withDisplayProp("desc")
                .addColumn("col-1").title("Column 1").withWordWrap()
                .done()
                .build();

        assertEquals(true, built.getFirst().get(DYN_COL_WORD_WRAP));
    }

    @Test
    public void withWordWrap_is_independent_per_column() {
        final List<Map<String, Object>> built = forProperty(TgCollectionalSerialisationParent.class, "collProp")
                .withGroupProp("key2")
                .withDisplayProp("desc")
                .addColumn("wrapped").title("Wrapped").withWordWrap()
                .addColumn("plain").title("Plain")
                .done()
                .build();

        assertEquals(2, built.size());
        final Map<String, Object> wrapped = byGroupValue(built, "wrapped");
        final Map<String, Object> plain = byGroupValue(built, "plain");
        assertTrue("Wrapped column should have word wrap enabled.", (Boolean) wrapped.get(DYN_COL_WORD_WRAP));
        assertFalse("Plain column should not have word wrap enabled.", (Boolean) plain.get(DYN_COL_WORD_WRAP));
    }

    @Test
    public void withWordWrap_is_chainable_after_width() {
        final List<Map<String, Object>> built = forProperty(TgCollectionalSerialisationParent.class, "collProp")
                .withGroupProp("key2")
                .withDisplayProp("desc")
                .addColumn("col-1").title("Column 1").width(120).withWordWrap()
                .done()
                .build();

        assertEquals(true, built.getFirst().get(DYN_COL_WORD_WRAP));
    }

    @Test
    public void withWordWrap_is_chainable_after_minWidth() {
        final List<Map<String, Object>> built = forProperty(TgCollectionalSerialisationParent.class, "collProp")
                .withGroupProp("key2")
                .withDisplayProp("desc")
                .addColumn("col-1").title("Column 1").minWidth(60).withWordWrap()
                .done()
                .build();

        assertEquals(true, built.getFirst().get(DYN_COL_WORD_WRAP));
    }

    @Test
    public void withWordWrap_is_chainable_after_desc() {
        final List<Map<String, Object>> built = forProperty(TgCollectionalSerialisationParent.class, "collProp")
                .withGroupProp("key2")
                .withDisplayProp("desc")
                .addColumn("col-1").title("Column 1").desc("Some description").withWordWrap()
                .done()
                .build();

        assertEquals(true, built.getFirst().get(DYN_COL_WORD_WRAP));
    }

    private static Map<String, Object> byGroupValue(final List<Map<String, Object>> built, final String groupValue) {
        return built.stream()
                .filter(m -> groupValue.equals(m.get(DYN_COL_GROUP_PROP_VALUE)))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No column with group value: " + groupValue));
    }

}
