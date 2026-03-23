package ua.com.fielden.platform.tiny;

import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import ua.com.fielden.platform.entity.functional.centre.CentreContextHolder;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.sample.domain.TgPersistentEntityWithProperties;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.types.Hyperlink;
import ua.com.fielden.platform.web.annotations.AppUri;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getTitleAndDesc;
import static ua.com.fielden.platform.tiny.TinyHyperlinkDao.*;

/// Tests pertaining to [TinyHyperlink].
///
public class TinyHyperlinkTest extends AbstractDaoTestCase {

    private String appUri;

    @Inject
    protected void init(@AppUri final String appUri) {
        this.appUri = StringUtils.stripEnd(appUri, "/");
    }

    @Test
    public void saving_a_new_tiny_hyperlink_with_same_target_as_already_saved_returns_the_existing_record() {
        final TinyHyperlinkCo coTinyHyperlink = co$(TinyHyperlink.class);
        final var target = new Hyperlink("https://fielden.com.au");
        final var tiny1 = coTinyHyperlink.saveWithTarget(target);
        final var tiny2 = coTinyHyperlink.saveWithTarget(target);
        assertEquals(tiny1.getId(), tiny2.getId());
    }

    @Test
    public void saving_a_new_tiny_hyperlink_with_same_shared_entity_as_already_saved_returns_the_existing_record() {
        final TinyHyperlinkCo coTinyHyperlink = co$(TinyHyperlink.class);
        final var tiny1 = coTinyHyperlink.save(
                User.class,
                Map.of(),
                new CentreContextHolder(),
                IActionIdentifier.of("test"));
        final var tiny2 = coTinyHyperlink.save(
                User.class,
                Map.of(),
                new CentreContextHolder(),
                IActionIdentifier.of("test"));
        assertEquals(tiny1.getId(), tiny2.getId());
    }

    @Test
    public void toURL_converts_a_tiny_hyperlink_into_a_URL_using_a_specific_format() {
        final TinyHyperlinkCo coTinyHyperlink = co$(TinyHyperlink.class);

        final var tiny1 = coTinyHyperlink.saveWithTarget(new Hyperlink("https://fielden.com.au"));
        assertEquals("%s/#/tiny/%s".formatted(appUri, tiny1.getHash()),
                     coTinyHyperlink.toURL(tiny1));

        final var tiny2 = coTinyHyperlink.save(
                User.class,
                Map.of(),
                new CentreContextHolder(),
                IActionIdentifier.of("test"));
        assertEquals("%s/#/tiny/%s".formatted(appUri, tiny2.getHash()),
                     coTinyHyperlink.toURL(tiny2));
    }

    @Test
    public void calling_hash_on_a_persisted_tiny_hyperlink_returns_the_assigned_hash() {
        final TinyHyperlinkCo coTinyHyperlink = co$(TinyHyperlink.class);

        final var tiny1 = coTinyHyperlink.saveWithTarget(new Hyperlink("https://fielden.com.au"));
        assertEquals(tiny1.getHash(), coTinyHyperlink.hash(tiny1));

        final var tiny2 = coTinyHyperlink.save(
                User.class,
                Map.of(),
                new CentreContextHolder(),
                IActionIdentifier.of("test"));
        assertEquals(tiny2.getHash(), coTinyHyperlink.hash(tiny2));
    }

    @Test
    public void calling_hash_on_a_new_tiny_hyperlink_computes_the_hash() {
        final TinyHyperlinkCo coTinyHyperlink = co$(TinyHyperlink.class);

        final var tiny1 = new_(TinyHyperlink.class).setTarget(new Hyperlink("https://fielden.com.au"));
        assertNull(tiny1.getHash());
        assertNotNull(coTinyHyperlink.hash(tiny1));

        final var tiny2 = new_(TinyHyperlink.class)
                .setEntityTypeName(User.class.getCanonicalName())
                .setSavingInfoHolder(new byte[] {0})
                .setActionIdentifier("test");
        assertNull(tiny2.getHash());
        assertNotNull(coTinyHyperlink.hash(tiny2));
    }

    @Test
    public void property_with_binary_type_cannot_be_specified_as_modified() {
        final TinyHyperlinkCo coTinyHyperlink = co$(TinyHyperlink.class);

        assertThatThrownBy(() -> coTinyHyperlink.save(TgPersistentEntityWithProperties.class,
                                                      Map.of("bytes", "hello".getBytes()),
                                                      new CentreContextHolder(),
                                                      IActionIdentifier.of("test")))
                .hasMessage(ERR_UNSUPPORTED_PROP_TYPE_FOR_SHARING.formatted("byte[]"));
    }

    @Test
    public void collectional_property_cannot_be_specified_as_modified() {
        final TinyHyperlinkCo coTinyHyperlink = co$(TinyHyperlink.class);

        assertThatThrownBy(() -> coTinyHyperlink.save(TgVehicle.class,
                                                      Map.of("fuelUsages", Set.of()),
                                                      new CentreContextHolder(),
                                                      IActionIdentifier.of("test")))
                .hasMessage(ERR_NO_SUPPORT_FOR_COLLECTIONAL_PROPS);
    }

    @Test
    public void toURL_requires_a_persisted_tiny_hyperlink() {
        final TinyHyperlinkCo coTinyHyperlink = co$(TinyHyperlink.class);
        final var tiny = new_(TinyHyperlink.class).setTarget(new Hyperlink("https://fielden.com.au"));

        assertThatThrownBy(() -> coTinyHyperlink.toURL(tiny))
                .hasMessage(ERR_URLS_FOR_PERSISTED_ONLY.formatted(TinyHyperlink.class.getSimpleName()));
    }

    @Test
    public void hash_requires_savingInfoHolder_for_entity_based_tiny_hyperlinks() {
        final TinyHyperlinkCo coTinyHyperlink = co$(TinyHyperlink.class);
        final var tiny = new_(TinyHyperlink.class)
                .setEntityTypeName(User.class.getCanonicalName())
                .setActionIdentifier("test");

        assertThatThrownBy(() -> coTinyHyperlink.hash(tiny))
                .hasMessage(ERR_INVALID_STATE_TO_COMPUTE_HASH.formatted(TinyHyperlink.SAVING_INFO_HOLDER));
    }

    @Test
    public void hash_requires_actionIdentifier_for_entity_based_tiny_hyperlinks() {
        final TinyHyperlinkCo coTinyHyperlink = co$(TinyHyperlink.class);
        final var tiny = new_(TinyHyperlink.class)
                .setEntityTypeName(User.class.getCanonicalName())
                .setSavingInfoHolder(new byte[] {0});

        assertThatThrownBy(() -> coTinyHyperlink.hash(tiny))
                .hasMessage(ERR_INVALID_STATE_TO_COMPUTE_HASH.formatted(TinyHyperlink.ACTION_IDENTIFIER));
    }

    @Test
    public void saving_a_new_tiny_hyperlink_requires_target_or_entityTypeName_and_savingInfoHolder_and_actionIdentifier() {
        final TinyHyperlinkCo coTinyHyperlink = co$(TinyHyperlink.class);
        // Only entityTypeName set — missing savingInfoHolder and actionIdentifier.
        final var tiny = new_(TinyHyperlink.class)
                .setEntityTypeName(User.class.getCanonicalName());

        assertThatThrownBy(() -> coTinyHyperlink.save(tiny))
                .isInstanceOf(Result.class)
                .hasMessage(ERR_REQUIRED_PROPS_VALIDATION.formatted(
                        getTitleAndDesc(TinyHyperlink.TARGET, TinyHyperlink.class).getKey(),
                        Stream.of(TinyHyperlink.ENTITY_TYPE_NAME, TinyHyperlink.SAVING_INFO_HOLDER, TinyHyperlink.ACTION_IDENTIFIER)
                                .map(prop -> getTitleAndDesc(prop, TinyHyperlink.class).getKey())
                                .collect(joining(", "))));
    }

}
