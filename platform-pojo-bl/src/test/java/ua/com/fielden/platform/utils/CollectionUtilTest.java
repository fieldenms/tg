package ua.com.fielden.platform.utils;

import org.junit.Test;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.Assert.*;
import static ua.com.fielden.platform.test_utils.CollectionTestUtils.assertEqualByContents;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.CollectionUtil.*;

public class CollectionUtilTest {

    @Test
    public void listOf_with_no_arguments_returns_empty_list() {
        assertEquals(0, listOf().size());
    }

    @Test
    public void listOf_returns_mutable_list() {
        final List<String> list = listOf("one");
        list.add("two");
        assertEquals(2, list.size());
    }

    @Test
    public void listOf_null_is_the_same_as_listOf_with_no_arguments() {
        assertEquals(0, listOf(null).size());
    }

    @Test
    public void listOf_with_more_than_one_null_arugment_produces_a_list_of_null_values() {
        final List<Object> list = listOf(null, null);
        assertEquals(2, list.size());
        assertNull(list.get(0));
        assertNull(list.get(1));
    }

    @Test
    public void mapOf_with_no_arguments_produces_empty_map() {
        assertNotNull(mapOf());
        assertTrue(mapOf().isEmpty());
    }

    @Test
    public void mapOf_produces_map_with_all_key_value_pairs_matching_arguments() {
        final Map<String, Integer> map = mapOf(t2("key1", 42), t2("key2", 12));

        assertEquals(2, map.size());
        assertEquals(Integer.valueOf(42), map.get("key1"));
        assertEquals(Integer.valueOf(12), map.get("key2"));
    }

    @Test
    public void linkedMapOf_produces_linked_map_with_all_key_value_pairs_matching_arguments() {
        final Map<String, Integer> map = linkedMapOf(t2("key1", 42), t2("key2", 12));

        assertTrue(map instanceof LinkedHashMap);
        assertEquals(2, map.size());
        assertEquals(Integer.valueOf(42), map.get("key1"));
        assertEquals(Integer.valueOf(12), map.get("key2"));
    }

    @Test
    public void tail_for_empty_array_is_empty_optional() {
        assertFalse(tail(new String[] {}).isPresent());
    }

    @Test
    public void tail_for_not_empty_array_contains_all_except_the_first_elements() {
        assertArrayEquals(new Integer[] {}, tail(new Integer[] {1}).get());
        assertArrayEquals(new Integer[] {2}, tail(new Integer[] {1, 2}).get());
        assertArrayEquals(new Integer[] {2, 3}, tail(new Integer[] {1, 2, 3}).get());
    }

     @Test
    public void collections_of_different_types_but_the_same_elements_are_equal_by_contents() {
        final List<Integer> list = List.of(1, 4, 2, 5);
        final Set<Integer> set = Set.of(4, 1, 5, 2);
        assertTrue(CollectionUtil.areEqualByContents(list, set));
    }

    @Test
    public void null_collections_are_not_equal_by_contents() {
        assertFalse(CollectionUtil.areEqualByContents(null, null));
    }

    @Test
    public void null_and_non_null_collections_are_not_equal_by_contents() {
        assertFalse(CollectionUtil.areEqualByContents(null, List.of(1, 4, 2, 5)));
        assertFalse(CollectionUtil.areEqualByContents(List.of(1, 4, 2, 5), null));
    }

    @Test
    public void collections_with_null_elements_cannot_be_checked_for_equality_by_contents() {
        final List<Integer> xs = CollectionUtil.listOf(1, 4, null, 5);
        final List<Integer> ys = CollectionUtil.listOf(4, 5, 1, null);
        assertThrows(NullPointerException.class, () -> CollectionUtil.areEqualByContents(xs, ys));
    }

    @Test
    public void lists_with_different_elements_are_not_equal_by_contents() {
        final List<Integer> xs = List.of(1, 4, 2, 5);
        final List<Integer> ys = List.of(1, 4, 2, 1);
        assertFalse(CollectionUtil.areEqualByContents(xs, ys));
    }

    @Test
    public void areEqualByContents_is_reflexive() {
        final List<Integer> xs = List.of(1, 4, 2, 5);
        assertTrue(CollectionUtil.areEqualByContents(xs, xs));
        assertTrue(CollectionUtil.areEqualByContents(List.of(1, 4, 2, 5), List.of(1, 4, 2, 5)));
    }

    @Test
    public void areEqualByContents_is_symmetric() {
        final List<Integer> xs = List.of(1, 4, 2, 5);
        final Set<Integer> ys = Set.of(2, 4, 5, 1);
        assertTrue(CollectionUtil.areEqualByContents(xs, ys));
        assertTrue(CollectionUtil.areEqualByContents(ys, xs));
    }

    @Test
    public void areEqualByContents_is_transitive() {
        final List<Integer> xs = List.of(1, 4, 2, 5);
        final Set<Integer> ys = Set.of(2, 4, 5, 1);
        final List<Integer> zs = List.of(4, 2, 5, 1);
        assertTrue(CollectionUtil.areEqualByContents(xs, ys));
        assertTrue(CollectionUtil.areEqualByContents(ys, zs));
        assertTrue(CollectionUtil.areEqualByContents(xs, zs));
    }

    @Test
    public void removeFirst_removes_only_the_first_element_matching_the_predicate() {
        final List<Integer> xs = listOf(1, 2, 3);
        assertEquals(Integer.valueOf(2), removeFirst(xs, x -> x >=2).get());
        assertEquals(listOf(1, 3), xs);
    }

    @Test
    public void removeFirst_removes_nothing_and_returns_empty_Optional_if_no_elements_match_the_predicate() {
        final List<Integer> xs = listOf(1, 2, 3);
        assertFalse(removeFirst(xs, x -> x < 0).isPresent());
        assertEquals(listOf(1, 2, 3), xs);
    }

    @Test
    public void removeFirst_passing_null_collection_throws_InvalidArgumentException() {
        final List<Integer> xs = null;
        assertThrows(InvalidArgumentException.class, () -> removeFirst(xs, x -> x >=2));
    }

    @Test
    public void removeFirst_passing_null_predicate_throws_InvalidArgumentException() {
        final List<Integer> xs = listOf(1, 2, 3);
        assertThrows(InvalidArgumentException.class, () -> removeFirst(xs, null));
    }

    @Test
    public void removeFirst_does_not_permit_null_elements_throwing_InvalidArgumentException() {
        final List<Integer> xs = listOf(1, null, 3);
        assertThrows(InvalidArgumentException.class, () -> removeFirst(xs, x -> x >=2));
    }

    @Test
    public void merge_for_a_single_map_produces_a_new_map_of_the_same_type_with_elements_from_that_map() {
        final var map1 = new HashMap<Integer, String>();
        map1.put(1, "a");
        map1.put(2, "b");

        final var result = merge(map1);
        assertNotNull(result);
        assertNotSame(map1, result);
        assertEquals(map1.getClass(), result.getClass());
        assertEquals(map1, result);
    }

    @Test
    public void merge_for_several_maps_of_the_same_type_produces_a_new_map_of_that_type_with_elements_from_all_maps() {
        final var map1 = new HashMap<Integer, String>();
        map1.put(1, "a");
        map1.put(2, "b");
        final var map2 = new HashMap<Integer, String>();
        map2.put(1, "a");
        map2.put(3, "c");
        final var map3 = new HashMap<Integer, String>();
        map3.put(4, "d");
        map3.put(5, "e");

        final var expectedResult = new HashMap<Integer, String>();
        expectedResult.putAll(map1);
        expectedResult.putAll(map2);
        expectedResult.putAll(map3);

        final var result = merge(map1, map2, map3);
        assertNotNull(result);
        assertNotSame(map1, result);
        assertEquals(expectedResult.getClass(), result.getClass());
        assertEquals(expectedResult, result);
    }

    @Test
    public void merge_for_several_maps_of_different_types_produces_a_new_map_of_the_type_as_first_argument_with_elements_from_all_maps() {
        final var map1 = new TreeMap<Integer, String>();
        map1.put(1, "a");
        map1.put(2, "b");
        final var map2 = new HashMap<Integer, String>();
        map2.put(1, "a");
        map2.put(3, "c");
        final var map3 = new LinkedHashMap<Integer, String>();
        map3.put(4, "d");
        map3.put(5, "e");

        final var expectedResult = new TreeMap<Integer, String>();
        expectedResult.putAll(map1);
        expectedResult.putAll(map2);
        expectedResult.putAll(map3);

        final var result = merge(map1, map2, map3);
        assertNotNull(result);
        assertNotSame(map1, result);
        assertEquals(expectedResult.getClass(), result.getClass());
        assertEquals(expectedResult, result);
    }

    @Test
    public void merge_skips_null_arguments_while_mering_all_other() {
        final var map1 = new TreeMap<Integer, String>();
        map1.put(1, "a");
        map1.put(2, "b");
        final var map2 = new HashMap<Integer, String>();
        map2.put(1, "c");
        map2.put(3, "c");
        final var map3 = new LinkedHashMap<Integer, String>();
        map3.put(4, "d");
        map3.put(5, "e");

        final var expectedResult = new TreeMap<Integer, String>();
        expectedResult.putAll(map1);
        expectedResult.putAll(map2);
        expectedResult.putAll(map3);

        final var result = merge(map1, null, map2, null, map3, null);
        assertNotNull(result);
        assertNotSame(map1, result);
        assertEquals(expectedResult.getClass(), result.getClass());
        assertEquals(expectedResult, result);
    }

    @Test
    public void merge_supports_maps_with_keys_and_values_of_compatible_types() {
        final var map1 = new HashMap<Number, CharSequence>();
        map1.put(1, "a");
        map1.put(2, "b");
        final var map2 = new HashMap<Integer, String>();
        map2.put(1, "c");
        map2.put(3, "d");
        final var map3 = new HashMap<BigDecimal, StringBuffer>();
        map3.put(BigDecimal.valueOf(2), new StringBuffer("e"));
        map3.put(BigDecimal.valueOf(3), new StringBuffer("f"));

        final var expectedResult = new HashMap<Number, CharSequence>();
        expectedResult.putAll(map1);
        expectedResult.putAll(map2);
        expectedResult.putAll(map3);

        final var result = merge(map1, map2, map3);
        assertNotNull(result);
        assertNotSame(map1, result);
        assertEquals(expectedResult.getClass(), result.getClass());
        assertEquals(expectedResult, result);
        assertEquals(5, result.size());
        assertEqualByContents(List.of("b","c","d","e","f"), result.values().stream().map(Object::toString).toList());
    }

}
