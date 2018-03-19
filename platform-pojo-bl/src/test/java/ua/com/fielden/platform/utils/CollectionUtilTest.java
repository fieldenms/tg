package ua.com.fielden.platform.utils;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.CollectionUtil.listOf;
import static ua.com.fielden.platform.utils.CollectionUtil.mapOf;
import static ua.com.fielden.platform.utils.CollectionUtil.tail;

import java.util.Map;

import org.junit.Test;

public class CollectionUtilTest {

    @Test
    public void listOf_with_no_arguments_returns_empty_list() {
        assertEquals(0, listOf().size());
    }

    @Test
    public void mapOf_with_no_arguments_produces_empty_map() {
        assertNotNull(mapOf());
        assertTrue(mapOf().isEmpty());
    }
    
    @Test
    public void mapOf_produces_map_with_all_key_value_pairs_matching_arguments() {
        Map<String, Integer> map = mapOf(t2("key1", 42), t2("key2", 12));
        
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
