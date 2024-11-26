package ua.com.fielden.platform.basic.autocompleter;

import com.google.inject.Injector;
import org.junit.Test;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.serialisation.jackson.entities.EntityWithRichText;
import ua.com.fielden.platform.test.CommonEntityTestIocModuleWithPropertyFactory;
import ua.com.fielden.platform.test_entities.Entity;
import ua.com.fielden.platform.types.RichText;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.basic.autocompleter.PojoValueMatcher.matchByAnyPropPredicate;

public class PojoValueMatcherTest {

    private static final Injector injector = new ApplicationInjectorFactory()
            .add(new CommonEntityTestIocModuleWithPropertyFactory())
            .getInjector();
    private static final EntityFactory factory = injector.getInstance(EntityFactory.class);

    private static final List<Entity> entities = List.of(
            factory.newEntity(Entity.class).setKey("ORDINARY VALUE 1").setDesc("Description 1"),
            factory.newEntity(Entity.class).setKey("ORDINARY VALUE 2").setDesc("Description 2"),
            factory.newEntity(Entity.class).setKey("SPECIAL SYMBOL (hrs)"),
            factory.newEntity(Entity.class).setKey("SPECIAL SYMBOL 2 *").setDesc("Description *"),
            factory.newEntity(Entity.class).setKey("Non special symbol --"),
            factory.newEntity(Entity.class).setKey("MIxed CaSe"),
            factory.newEntity(Entity.class).setKey("some more [symbols]"),
            factory.newEntity(Entity.class).setKey("some more {symbols}"),
            factory.newEntity(Entity.class).setKey("some more /symbols/"),
            factory.newEntity(Entity.class).setKey("some more \\symbols\\"));

    @Test
    public void exact_search_for_a_single_value_is_supported() {
        final PojoValueMatcher<Entity> pvm = new PojoValueMatcher<>(entities, "key", entities.size());
        final List<Entity> result = pvm.findMatches("ORDINARY VALUE 1");
        assertEquals(1, result.size());
        assertEquals(entities.getFirst(), result.getFirst());
    }

    @Test
    public void wildcard_search_for_UPPERCASED_values_is_supported() {
        final PojoValueMatcher<Entity> pvm = new PojoValueMatcher<>(entities, "key", entities.size());
        final List<Entity> result = pvm.findMatches("ORDINARY %");
        assertEquals(2, result.size());
        assertEquals(entities.get(0), result.get(0));
        assertEquals(entities.get(1), result.get(1));
    }

    @Test
    public void wildcard_search_for_mixed_case_values_is_supported() {
        final PojoValueMatcher<Entity> pvm = new PojoValueMatcher<>(entities, "key", entities.size());
        final List<Entity> result = pvm.findMatches("%ed CASE%");
        assertEquals(1, result.size());
        assertEquals(entities.get(5), result.get(0));
    }

    @Test
    public void exact_search_for_a_value_with_special_char_is_supported() {
        final PojoValueMatcher<Entity> pvm = new PojoValueMatcher<>(entities, "key", entities.size());
        final List<Entity> result = pvm.findMatches("SPECIAL SYMBOL (hrs)");
        assertEquals(1, result.size());
        assertEquals(entities.get(2), result.get(0));
    }

    @Test
    public void exact_search_for_a_value_with_another_special_char_is_supported() {
        final PojoValueMatcher<Entity> pvm = new PojoValueMatcher<>(entities, "key", entities.size());
        final List<Entity> result = pvm.findMatches("SPECIAL SYMBOL 2 *");
        assertEquals(1, result.size());
        assertEquals(entities.get(3), result.get(0));
    }

    @Test
    public void wild_search_for_a_value_with_special_chars_is_supported() {
        final PojoValueMatcher<Entity> pvm = new PojoValueMatcher<>(entities, "key", entities.size());
        final List<Entity> result = pvm.findMatches("SPECIAL SYMBOL %");
        assertEquals(2, result.size());
        assertEquals(entities.get(2), result.get(0));
        assertEquals(entities.get(3), result.get(1));
    }

    @Test
    public void wild_search_for_a_value_with_dashes_is_supported() {
        final PojoValueMatcher<Entity> pvm = new PojoValueMatcher<>(entities, "key", entities.size());
        final List<Entity> result = pvm.findMatches("%-%");
        assertEquals(1, result.size());
        assertEquals(entities.get(4), result.get(0));
    }

    @Test
    public void exact_search_for_a_value_with_bracket_chars_is_supported() {
        final PojoValueMatcher<Entity> pvm = new PojoValueMatcher<>(entities, "key", entities.size());
        final List<Entity> result = pvm.findMatches("some more [symbols]");
        assertEquals(1, result.size());
        assertEquals(entities.get(6), result.get(0));
    }

    @Test
    public void exact_search_for_a_value_with_curly_bracket_chars_is_supported() {
        final PojoValueMatcher<Entity> pvm = new PojoValueMatcher<>(entities, "key", entities.size());
        final List<Entity> result = pvm.findMatches("some more {symbols}");
        assertEquals(1, result.size());
        assertEquals(entities.get(7), result.get(0));
    }

    @Test
    public void exact_search_for_a_value_with_forward_slash_is_supported() {
        final PojoValueMatcher<Entity> pvm = new PojoValueMatcher<>(entities, "key", entities.size());
        final List<Entity> result = pvm.findMatches("some more /symbols/");
        assertEquals(1, result.size());
        assertEquals(entities.get(8), result.get(0));
    }

    @Test
    public void exact_search_for_a_value_with_back_slash_is_supported() {
        final PojoValueMatcher<Entity> pvm = new PojoValueMatcher<>(entities, "key", entities.size());
        final List<Entity> result = pvm.findMatches("some more \\symbols\\");
        assertEquals(1, result.size());
        assertEquals(entities.get(9), result.get(0));
    }

    @Test
    public void exact_search_by_key_and_desc_is_supported() {
        final PojoValueMatcher<Entity> pvm = new PojoValueMatcher<>(entities, matchByAnyPropPredicate(Set.of("key", "desc")), entities.size());
        final List<Entity> result1 = pvm.findMatches("description 1");
        assertEquals(1, result1.size());
        assertEquals(entities.get(0), result1.get(0));

        final List<Entity> result2 = pvm.findMatches("SPECIAL SYMBOL (hrs)");
        assertEquals(1, result2.size());
        assertEquals(entities.get(2), result2.get(0));
    }

    @Test
    public void wild_search_by_key_and_desc_is_supported() {
        final PojoValueMatcher<Entity> pvm = new PojoValueMatcher<>(entities, matchByAnyPropPredicate(Set.of("key", "desc")), entities.size());
        final List<Entity> result1 = pvm.findMatches("descri%");
        assertEquals(3, result1.size());
        assertEquals(entities.get(0), result1.get(0));
        assertEquals(entities.get(1), result1.get(1));
        assertEquals(entities.get(3), result1.get(2));

        final List<Entity> result = pvm.findMatches("%SPECIAL%");
        assertEquals(3, result.size());
        assertEquals(entities.get(2), result.get(0));
        assertEquals(entities.get(3), result.get(1));
        assertEquals(entities.get(4), result.get(2));
    }

    @Test
    public void search_by_RichText_property_matches_against_core_text() {
        final var entities = List.of(
                factory.newByKey(EntityWithRichText.class, "1").setText(RichText.fromHtml("The <b>blue</b> sky")),
                factory.newByKey(EntityWithRichText.class, "2").setText(RichText.fromHtml("The<br>big<br><p>bang</p>")));
        final var matcher = new PojoValueMatcher<>(entities, "text", entities.size());

        assertEquals("Incorrect matches.", List.of(entities.get(0)), matcher.findMatches("%blue sky%"));
        assertEquals("Incorrect matches.", List.of(entities.get(1)), matcher.findMatches("%big bang%"));
    }

}
