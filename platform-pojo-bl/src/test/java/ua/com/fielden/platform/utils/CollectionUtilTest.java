package ua.com.fielden.platform.utils;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.CollectionUtil.linkedMapOf;
import static ua.com.fielden.platform.utils.CollectionUtil.listOf;
import static ua.com.fielden.platform.utils.CollectionUtil.mapOf;
import static ua.com.fielden.platform.utils.CollectionUtil.tail;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

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

}
