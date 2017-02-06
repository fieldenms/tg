package ua.com.fielden.platform.basic.autocompleter;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import ua.com.fielden.platform.entity.Entity;

public class PojoValueMatcherTest {

    private final List<Entity> entities = new ArrayList<>();
    {
        entities.add((Entity) new Entity().setKey("ORDINARY VALUE 1"));
        entities.add((Entity) new Entity().setKey("ORDINARY VALUE 2"));
        entities.add((Entity) new Entity().setKey("SPECIAL SYMBOL (hrs)"));
        entities.add((Entity) new Entity().setKey("SPECIAL SYMBOL 2 *"));
        entities.add((Entity) new Entity().setKey("Non special symbol --"));
        entities.add((Entity) new Entity().setKey("MIxed CaSe"));
        entities.add((Entity) new Entity().setKey("some more [symbols]"));
        entities.add((Entity) new Entity().setKey("some more {symbols}"));
        entities.add((Entity) new Entity().setKey("some more /symbols/"));
        entities.add((Entity) new Entity().setKey("some more \\symbols\\"));
    }
    
    @Test
    public void exact_search_for_a_single_value_is_supported() {
        final PojoValueMatcher<Entity> pvm = new PojoValueMatcher<>(entities, "key", entities.size());
        final List<Entity> result = pvm.findMatches("ORDINARY VALUE 1");
        assertEquals(1, result.size());
        assertEquals(entities.get(0), result.get(0));
        //Pattern.quote(
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
    public void whild_search_for_a_value_with_special_chars_is_supported() {
        final PojoValueMatcher<Entity> pvm = new PojoValueMatcher<>(entities, "key", entities.size());
        final List<Entity> result = pvm.findMatches("SPECIAL SYMBOL %");
        assertEquals(2, result.size());
        assertEquals(entities.get(2), result.get(0));
        assertEquals(entities.get(3), result.get(1));
    }
    
    @Test
    public void whild_search_for_a_value_with_dashes_is_supported() {
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


}
