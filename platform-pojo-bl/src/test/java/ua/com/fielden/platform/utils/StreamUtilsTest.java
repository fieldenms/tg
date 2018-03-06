package ua.com.fielden.platform.utils;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.utils.CollectionUtil.listOf;
import static ua.com.fielden.platform.utils.StreamUtils.ERR_FIRST_STREAM_ELEM_CANNOT_BE_NULL;
import static ua.com.fielden.platform.utils.StreamUtils.head_and_tail;
import static ua.com.fielden.platform.utils.StreamUtils.takeWhile;
import static ua.com.fielden.platform.utils.StreamUtils.zip;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import ua.com.fielden.platform.types.tuples.T2;

public class StreamUtilsTest {

    @Test
    public void of_with_first_value_null_throws_NPE() {
        try {
            StreamUtils.of(null, "second", "thirds");
            fail();
        } catch (final NullPointerException ex) {
            assertEquals(ERR_FIRST_STREAM_ELEM_CANNOT_BE_NULL, ex.getMessage());
        }
    }

    @Test
    public void of_can_make_stream_with_one_elem() {
        final Stream<String> xs = StreamUtils.of("one");
        assertEquals("one", xs.findFirst().orElse("Stream is empty"));
    }

    @Test
    public void of_can_make_stream_with_more_than_one_elem() {
        final Stream<String> xs = StreamUtils.of("one", "two");
        
        final List<String> ys = xs.collect(Collectors.toList());
        assertEquals(2, ys.size());
        assertEquals("one", ys.get(0));
        assertEquals("two", ys.get(1));
    }
    
    @Test
    public void of_can_make_stream_with_that_incudes_nulls_if_first_value_is_not_null() {
        final Stream<Integer> xs = StreamUtils.of(1, 2, null, 4);
        
        final List<Integer> ys = xs.collect(Collectors.toList());
        assertEquals(4, ys.size());
        assertEquals(Integer.valueOf(1), ys.get(0));
        assertEquals(Integer.valueOf(2), ys.get(1));
        assertNull(ys.get(2));
        assertEquals(Integer.valueOf(4), ys.get(3));
    }
    

    @Test
    public void prepending_null_throws_NPE() {
        try {
            StreamUtils.prepend(null, Stream.of("second", "thirds"));
            fail();
        } catch (final NullPointerException ex) {
            assertEquals(ERR_FIRST_STREAM_ELEM_CANNOT_BE_NULL, ex.getMessage());
        }
    }

    @Test
    public void prepending_to_empty_stream_results_in_single_element_stream() {
        final Stream<String> xs = StreamUtils.prepend("one", Stream.empty());
        assertEquals("one", xs.findFirst().orElse("Stream is empty"));
    }
    
    @Test
    public void prepending_to_non_empty_stream_add_element_as_head() {
        final Stream<Integer> xs = StreamUtils.prepend(0, Stream.of(1, 2, 3, 4));
        
        final List<Integer> ys = xs.collect(Collectors.toList());
        assertEquals(5, ys.size());
        assertEquals(Integer.valueOf(0), ys.get(0));
        assertEquals(Integer.valueOf(1), ys.get(1));
        assertEquals(Integer.valueOf(2), ys.get(2));
        assertEquals(Integer.valueOf(3), ys.get(3));
        assertEquals(Integer.valueOf(4), ys.get(4));
    }

    @Test
    public void head_and_tail_of_a_stream_with_more_than_2_elements_are_not_empty_and_contain_the_expected_elements() {
        final T2<Optional<Integer>, Stream<Integer>> head_and_tail = head_and_tail(Stream.of(1, 2, 3, 4));
        final Optional<Integer> head = head_and_tail._1;
        final Stream<Integer> tail = head_and_tail._2;
        
        assertTrue(head.isPresent());
        assertEquals(Integer.valueOf(1), head.get());
        
        final AtomicInteger expectedCurrValue = new AtomicInteger(head.get());
        assertTrue(tail.allMatch(v -> v == expectedCurrValue.incrementAndGet()));
    }

    @Test
    public void head_and_tail_of_a_stream_with_1_element_are_split_into_non_empty_head_and_empty_tail() {
        final T2<Optional<Integer>, Stream<Integer>> head_and_tail = head_and_tail(Stream.of(1));
        final Optional<Integer> head = head_and_tail._1;
        final Stream<Integer> tail = head_and_tail._2;
        
        assertTrue(head.isPresent());
        assertEquals(Integer.valueOf(1), head.get());
        
        assertEquals(0L, tail.count());
    }

    @Test
    public void head_and_tail_of_a_stream_with_0_element_are_split_into_empty_head_and_empty_tail() {
        final T2<Optional<Integer>, Stream<Integer>> head_and_tail = head_and_tail(Stream.empty());
        final Optional<Integer> head = head_and_tail._1;
        final Stream<Integer> tail = head_and_tail._2;
        
        assertFalse(head.isPresent());
        assertEquals(0L, tail.count());
    }

    @Test
    public void takeWhile_for_empty_stream_returns_empty_stream() {
        assertEquals(0L, takeWhile(Stream.empty(), e -> true).count());
    }

    @Test
    public void takeWhile_returns_the_longest_predicate_of_the_stream_whose_elements_satisfy_predicare() {
        final Stream<Integer> prefix = takeWhile(Stream.of(0, 1, 2, 3, 4, 5, 6, 1, 2, 3), e -> e < 5);

        final AtomicInteger expectedCurrValue = new AtomicInteger(-1);
        assertTrue(prefix.allMatch(v -> v == expectedCurrValue.incrementAndGet()));
        assertEquals(4, expectedCurrValue.get());
    }
    
    @Test
    public void can_zip_steams_of_different_size() {
        assertEquals(listOf(0, 2, 4), zip(Stream.of(0, 1, 2), Stream.of(0, 1, 2, 3), (x, y) -> x+y).collect(toList()));
        assertEquals(listOf(0, 2, 4), zip(Stream.of(0, 1, 2, 3), Stream.of(0, 1, 2), (x, y) -> x+y).collect(toList()));
        assertEquals(listOf(), zip(Stream.<Integer>empty(), Stream.of(0, 1, 2), (x, y) -> x+y).collect(toList()));
        assertEquals(listOf(), zip(Stream.of(0, 1, 2), Stream.<Integer>empty(), (x, y) -> x+y).collect(toList()));
        
    }
}
