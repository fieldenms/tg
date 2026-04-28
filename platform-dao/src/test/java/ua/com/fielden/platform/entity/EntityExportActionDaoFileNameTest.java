package ua.com.fielden.platform.entity;

import org.junit.Test;

import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.entity.EntityExportActionDao.MAX_CONFIG_TITLE_IN_FILE_NAME;
import static ua.com.fielden.platform.entity.EntityExportActionDao.buildFileName;
import static ua.com.fielden.platform.entity.EntityExportActionDao.sanitiseForFileName;
import static ua.com.fielden.platform.web.centre.WebApiUtils.LINK_CONFIG_TITLE;

/// Unit tests for the export file-name construction in {@link EntityExportActionDao}.
public class EntityExportActionDaoFileNameTest {

    @Test
    public void default_configuration_yields_plain_file_name() {
        assertEquals("export-of-Equipment.xlsx", buildFileName("Equipment", empty()));
    }

    @Test
    public void link_configuration_yields_plain_file_name() {
        assertEquals("export-of-Equipment.xlsx", buildFileName("Equipment", of(LINK_CONFIG_TITLE)));
    }

    @Test
    public void saved_configuration_title_is_appended_to_file_name() {
        assertEquals("export-of-Equipment-Critical equipment.xlsx", buildFileName("Equipment", of("Critical equipment")));
    }

    @Test
    public void disallowed_file_name_characters_are_replaced_with_dash() {
        assertEquals("export-of-Equipment-A-B-C-D.xlsx", buildFileName("Equipment", of("A/B:C\\D")));
    }

    @Test
    public void all_disallowed_characters_are_replaced() {
        assertEquals("a-b-c-d-e-f-g-h-i", sanitiseForFileName("a\\b/c:d*e?f\"g<h>i|"));
    }

    @Test
    public void runs_of_dashes_introduced_by_replacement_are_collapsed() {
        assertEquals("a-b", sanitiseForFileName("a///b"));
    }

    @Test
    public void leading_and_trailing_dashes_are_trimmed() {
        assertEquals("name", sanitiseForFileName("/name/"));
    }

    @Test
    public void title_longer_than_limit_is_truncated() {
        final String longTitle = "x".repeat(MAX_CONFIG_TITLE_IN_FILE_NAME + 50);
        final String result = sanitiseForFileName(longTitle);
        assertEquals(MAX_CONFIG_TITLE_IN_FILE_NAME, result.length());
    }

    @Test
    public void title_at_limit_is_not_truncated() {
        final String exactTitle = "y".repeat(MAX_CONFIG_TITLE_IN_FILE_NAME);
        assertEquals(exactTitle, sanitiseForFileName(exactTitle));
    }

    @Test
    public void spaces_in_title_are_preserved() {
        assertEquals("My filter v2", sanitiseForFileName("My filter v2"));
    }

    @Test
    public void truncation_applies_via_buildFileName_for_long_titles() {
        final String longTitle = "z".repeat(MAX_CONFIG_TITLE_IN_FILE_NAME + 10);
        final String expectedTitlePart = "z".repeat(MAX_CONFIG_TITLE_IN_FILE_NAME);
        assertEquals("export-of-Equipment-%s.xlsx".formatted(expectedTitlePart), buildFileName("Equipment", of(longTitle)));
    }

    @Test
    public void empty_optional_is_treated_as_default() {
        final Optional<String> noConfig = empty();
        assertEquals("export-of-WorkActivity.xlsx", buildFileName("WorkActivity", noConfig));
    }

}
