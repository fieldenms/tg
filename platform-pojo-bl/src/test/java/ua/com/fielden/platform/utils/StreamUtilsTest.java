package ua.com.fielden.platform.utils;

import org.junit.Test;
import ua.com.fielden.platform.types.tuples.T2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.*;
import static ua.com.fielden.platform.test_utils.TestUtils.assertEmpty;
import static ua.com.fielden.platform.test_utils.TestUtils.assertOptEquals;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.CollectionUtil.listOf;
import static ua.com.fielden.platform.utils.Pair.pair;
import static ua.com.fielden.platform.utils.StreamUtils.*;

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
    public void takeWhile_returns_the_longest_prefix_of_the_stream_whose_elements_satisfy_predicate() {
        final Stream<Integer> prefix = takeWhile(Stream.of(0, 1, 2, 3, 4, 5, 6, 1, 2, 3), e -> e < 5);

        final AtomicInteger expectedCurrValue = new AtomicInteger(-1);
        assertTrue(prefix.allMatch(v -> v == expectedCurrValue.incrementAndGet()));
        assertEquals(4, expectedCurrValue.get());
    }

    @Test
    public void stopAfter_for_empty_stream_returns_empty_stream() {
        assertEquals(0L, stopAfter(Stream.empty(), e -> true).count());
    }

    @Test
    public void stopAfter_returns_the_longest_prefix_of_the_stream_stopping_after_element_satisfying_predicate() {
        final Stream<Integer> prefix = stopAfter(Stream.of(0, 1, 2, 3, 4, 5, 6, 1, 2, 3), e -> e >= 5);

        final AtomicInteger expectedCurrValue = new AtomicInteger(-1);
        assertTrue(prefix.allMatch(v -> v == expectedCurrValue.incrementAndGet()));
        assertEquals(5, expectedCurrValue.get());
    }

    @Test
    public void stopAfter_returns_the_whole_stream_if_no_element_satisfies_predicate() {
        final List<Integer> numbers = List.of(0, 1, 2, 3, 4, 5, 6, 1, 2, 3);
        final List<Integer> prefix = stopAfter(numbers.stream(), e -> e >= 7).toList();

        assertTrue(numbers.containsAll(prefix) && numbers.size() == prefix.size());
    }
    
    @Test
    public void distinct_returns_a_stream_whose_elements_are_distinct_according_to_mapper() {
        final List<Pair<String, Integer>> elements = List.of(pair("one", 1), pair("two", 2), pair("one", 3), pair("three", 1));

        // distinct by key
        assertEquals(List.of(pair("one", 1), pair("two", 2), pair("three", 1)), 
                StreamUtils.distinct(elements.stream(), Pair::getKey).toList());
        // distinct by value
        assertEquals(List.of(pair("one", 1), pair("two", 2), pair("one", 3)), 
                StreamUtils.distinct(elements.stream(), Pair::getValue).toList());
    }

    @Test
    public void distinct_returns_a_stream_with_order_preserved() {
        final List<Pair<String, Integer>> elements = List.of(pair("one", 1), pair("two", 2), pair("two", 22), pair("one", 11));

        assertEquals(List.of(pair("one", 1), pair("two", 2)), 
                StreamUtils.distinct(elements.stream(), Pair::getKey).toList());
    }

    @Test
    public void distinct_returns_a_stream_whose_spliterator_provides_distinct_elements() {
        final var splitr = StreamUtils.distinct(Stream.of("fun", "proc", "function", "routine"), s -> s.charAt(0)).spliterator();
        final var distinct = new ArrayList<String>();
        splitr.forEachRemaining(distinct::add);
        assertEquals(List.of("fun", "proc", "routine"), distinct);
    }

    @Test
    public void distinct_returns_a_stream_whose_iterator_provides_distinct_elements() {
        final var iter = StreamUtils.distinct(Stream.of("fun", "proc", "function", "routine"), s -> s.charAt(0)).iterator();
        final var distinct = new ArrayList<String>();
        iter.forEachRemaining(distinct::add);
        assertEquals(List.of("fun", "proc", "routine"), distinct);
    }

    @Test
    public void can_zip_streams_of_different_size() {
        assertEquals(listOf(0, 2, 4), zip(Stream.of(0, 1, 2), Stream.of(0, 1, 2, 3), (x, y) -> x+y).toList());
        assertEquals(listOf(0, 2, 4), zip(Stream.of(0, 1, 2, 3), Stream.of(0, 1, 2), (x, y) -> x+y).toList());
        assertEquals(listOf(), zip(Stream.<Integer>empty(), Stream.of(0, 1, 2), (x, y) -> x+y).toList());
        assertEquals(listOf(), zip(Stream.of(0, 1, 2), Stream.<Integer>empty(), (x, y) -> x+y).toList());
    }

    @Test
    public void can_zip_finite_with_infinite() {
        final AtomicInteger ai = new AtomicInteger(0);
        assertEquals(listOf(0, 2, 4), zip(Stream.of(0, 1, 2), Stream.generate(ai::getAndIncrement), (x, y) -> x+y).toList());
        assertEquals(listOf(1, 2, 3), zip(Stream.generate(() -> 1), Stream.of(0, 1, 2), (x, y) -> x+y).toList());
    }

    @Test
    public void stream_with_even_number_of_elements_can_be_windowed_into_two_equal_parts() {
        final Stream<Integer> source = Stream.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        final Stream<List<Integer>> windowed = StreamUtils.windowed(source, 5);
        final List<List<Integer>> windowedAsList = windowed.collect(toList());
        assertEquals(2, windowedAsList.size());
        assertEquals(listOf(0, 1, 2, 3, 4), windowedAsList.get(0));
        assertEquals(listOf(5, 6, 7, 8, 9), windowedAsList.get(1));
    }

    @Test
    public void stream_can_be_windowed_into_equal_parts_with_a_shorter_remainder() {
        final Stream<Integer> source = Stream.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        final Stream<List<Integer>> windowed = StreamUtils.windowed(source, 3);
        final List<List<Integer>> windowedAsList = windowed.collect(toList());
        assertEquals(4, windowedAsList.size());
        assertEquals(listOf(0, 1, 2), windowedAsList.get(0));
        assertEquals(listOf(3, 4, 5), windowedAsList.get(1));
        assertEquals(listOf(6, 7, 8), windowedAsList.get(2));
        assertEquals(listOf(9), windowedAsList.get(3));
    }

    @Test
    public void stream_can_be_windowed_into_parts_with_a_single_element_each() {
        final Stream<Integer> source = Stream.of(0, 1, 2);
        final Stream<List<Integer>> windowed = StreamUtils.windowed(source, 1);
        final List<List<Integer>> windowedAsList = windowed.collect(toList());
        assertEquals(3, windowedAsList.size());
        assertEquals(listOf(0), windowedAsList.get(0));
        assertEquals(listOf(1), windowedAsList.get(1));
        assertEquals(listOf(2), windowedAsList.get(2));
    }

    @Test
    public void windowing_a_stream_with_a_non_positive_window_size_is_an_error() {
        assertThrows(IllegalArgumentException.class, () -> StreamUtils.windowed(Stream.of(0, 1, 2), -1));
        assertThrows(IllegalArgumentException.class, () -> StreamUtils.windowed(Stream.of(0, 1, 2), 0));
    }

    @Test
    public void empty_stream_is_windowed_into_empty_stream() {
        final Stream<Integer> source = Stream.empty();
        final Stream<List<Integer>> windowed = StreamUtils.windowed(source, 1);
        final List<List<Integer>> windowedAsList = windowed.collect(toList());
        assertEquals(0, windowedAsList.size());
    }

    @Test
    public void typeFilter_preserves_only_instances_of_the_given_type_in_a_stream() {
        assertEquals(List.of(1), Stream.of("one", 1).mapMulti(typeFilter(Integer.class)).toList());
        assertEquals(List.of(1), Stream.of("one", 1).mapMulti(typeFilter(Number.class)).toList());
        assertEquals(List.of("one", 1), Stream.of("one", 1).mapMulti(typeFilter(Object.class)).toList());
        assertEquals(List.of(), Stream.of("one", 1).mapMulti(typeFilter(List.class)).toList());
    }

    @Test
    public void reduceLeft_returns_empty_optional_for_empty_stream() {
        assertEmpty(reduceLeft(IntStream.of(), Integer::sum));
    }

    @Test
    public void reduceLeft_returns_optional_with_first_element_for_stream_with_one_element() {
        assertOptEquals("one", reduceLeft(Stream.of("one"), String::concat));
    }

    @Test
    public void reduceLeft_processes_stream_elements_sequentially_from_left_to_right() {
        assertOptEquals(1, reduceLeft(IntStream.of(1), Integer::sum));
        assertOptEquals("one-two", reduceLeft(Stream.of("one-", "two"), String::concat));
    }

    @Test
    public void foldLeft_returns_optional_with_initial_element_for_empty_stream() {
        assertEquals("zero", foldLeft(Stream.of(), "zero", String::concat));
    }

    @Test
    public void foldLeft_processes_stream_elements_sequentially_from_left_to_right() {
        assertEquals("one-two-three", foldLeft(Stream.of("two-", "three"), "one-", String::concat));
    }

    @Test
    public void supplyIfEmpty_returns_equivalent_stream_if_original_is_not_empty() {
        final var xs = CollectionUtil.listOf(1, 2);
        final var xsEquivalent = StreamUtils.supplyIfEmpty(xs.stream(), () -> 0).toList();
        assertEquals(xs, xsEquivalent);
    }

    @Test
    public void supplyIfEmpty_returns_alternative_stream_if_original_is_empty() {
        final var xsAlternative = StreamUtils.supplyIfEmpty(Stream.empty(), () -> 0).limit(3).toList();
        assertEquals(CollectionUtil.listOf(0, 0, 0), xsAlternative);
    }

    @Test
    public void removeAll_returns_a_stream_with_specified_elements_removed() {
        assertEquals(IntStream.rangeClosed(6, 10).boxed().toList(),
                     StreamUtils.removeAll(IntStream.rangeClosed(1, 10).boxed(),
                                           IntStream.rangeClosed(1, 5).boxed().toList())
                             .toList());

        assertEquals(List.of("b"),
                     StreamUtils.removeAll(Stream.of("a", "b", "c"), List.of("A", ".", "C"), String::equalsIgnoreCase)
                             .toList());
    }

    @Test
    public void removeAll_doesnt_remove_anything_if_items_to_remove_are_empty() {
        assertEquals(List.of("a", "b"),
                     StreamUtils.removeAll(Stream.of("a", "b"), List.of()).toList());

        assertEquals(List.of("a", "b"),
                     StreamUtils.removeAll(Stream.of("a", "b"), List.of(), String::equalsIgnoreCase)
                             .toList());
    }

    @Test
    public void removeAll_returns_an_empty_stream_given_an_empty_stream() {
        assertEquals(List.of(),
                     StreamUtils.removeAll(Stream.of(), List.of("a")).toList());

        assertEquals(List.of(),
                     StreamUtils.removeAll(Stream.of(), List.of("a"), String::equalsIgnoreCase).toList());
    }

    @Test
    public void removeAll_with_default_predicate_allows_nulls_and_treats_them_like_other_objects() {
        assertEquals(List.of("a"),
                     StreamUtils.removeAll(Stream.of("a", null, null, "b"), listOf("b", null)).toList());
    }

    @Test
    public void collectToImmutableMap_terminates_upon_reaching_the_shorter_stream() {
        assertEquals(Map.of("a", 1), collectToImmutableMap(Stream.of("a", "b"), Stream.of(1)));
        assertEquals(Map.of(), collectToImmutableMap(Stream.of(), Stream.of(1)));
    }

    @Test
    public void collectToImmutableMap_applies_given_functions_to_produce_keys_and_values() {
        assertEquals(Map.of("", 0, "cdecde", 6),
                     collectToImmutableMap(Stream.of("ab", "cde"), Stream.of(0, 2),
                                           (s, i) -> s.repeat(i), (s, i) -> i * s.length()));
    }

    @Test
    public void enumerate_pairs_each_stream_element_with_its_sequential_number_starting_from_0_by_default() {
        assertEquals(List.of("0:a", "1:b"),
                     enumerate(Stream.of("a", "b"), (x, i) -> "%s:%s".formatted(i, x)).toList());
    }

    @Test
    public void enumerate_pairs_each_stream_element_with_its_sequential_number_starting_from_the_given_one() {
        assertEquals(List.of("4:a", "5:b"),
                     enumerate(Stream.of("a", "b"), 4, (x, i) -> "%s:%s".formatted(i, x)).toList());
    }

    @Test
    public void enumerate_constructs_an_empty_stream_given_an_empty_stream() {
        assertTrue(enumerate(Stream.of(), (v, i) -> "").toList().isEmpty());
    }

    public void transpose_returns_MxN_matrix_given_NxM_matrix() {
        final var matrix = List.of(List.of(1, 2), List.of(3, 4), List.of(5, 6));
        assertEquals(
                List.of(List.of(1, 3, 5), List.of(2, 4, 6)),
                transpose(matrix).toList());
    }

    @Test
    public void tranpose_returns_as_many_lists_as_the_length_of_shortest_input_collection() {
        final var matrix = List.of(List.of(1, 2, 3), List.of(4, 5), List.of(6, 7, 8));
        assertEquals(
                List.of(List.of(1, 4, 6), List.of(2, 5, 7)),
                transpose(matrix).toList());

        assertEquals(List.of(), transpose(List.of()).toList());
    }

    @Test
    public void isSingleElementStream_returns_true_for_streams_with_one_element() {
        assertTrue(isSingleElementStream(Stream.of("x")));
        assertTrue(isSingleElementStream(IntStream.of(5)));
    }

    @Test
    public void isSingleElementStream_returns_false_for_an_empty_stream() {
        assertFalse(isSingleElementStream(Stream.of()));
    }

    @Test
    public void isSingleElementStream_returns_false_for_a_stream_with_multiple_elements() {
        assertFalse(isSingleElementStream(Stream.of("a", "b")));
        assertFalse(isSingleElementStream(Stream.of("a", "b").parallel()));
    }

    @Test
    public void isMultiElementStream_returns_false_for_streams_with_one_element() {
        assertFalse(isMultiElementStream(Stream.of("x")));
        assertFalse(isMultiElementStream(IntStream.of(5)));
    }

    @Test
    public void isMultiElementStream_returns_false_for_an_empty_stream() {
        assertFalse(isMultiElementStream(Stream.of()));
    }

    @Test
    public void isMultiElementStream_returns_true_for_a_stream_with_multiple_elements() {
        assertTrue(isMultiElementStream(Stream.of("a", "b")));
        assertTrue(isMultiElementStream(Stream.of("a", "b").parallel()));
    }

    @Test
    public void areAllEqual_returns_an_empty_optional_for_an_empty_stream() {
        assertTrue(areAllEqual(IntStream.of()).isEmpty());
    }

    @Test
    public void areAllEqual_returns_true_for_a_stream_of_the_same_integer() {
        final int n = 764932;
        assertOptEquals(true, areAllEqual(IntStream.of(n)));
        assertOptEquals(true, areAllEqual(IntStream.of(n, n)));
        assertOptEquals(true, areAllEqual(IntStream.of(n, n, n)));
    }

    @Test
    public void areAllEqual_returns_false_for_a_stream_of_different_integers() {
        final int n = 764932;
        final int m = 43279;
        assertOptEquals(false, areAllEqual(IntStream.of(n, m)));
        assertOptEquals(false, areAllEqual(IntStream.of(m, n, m + 1)));
        assertOptEquals(false, areAllEqual(IntStream.of(m, m, n)));
    }

    @Test
    public void partitioning_collects_a_tupple_of_empty_lists_for_an_empty_stream() {
        assertEquals(t2(List.of(), List.of()), Stream.of().collect(partitioning(x -> true)));
        assertEquals(t2(List.of(), List.of()), Stream.of().collect(partitioning(x -> false)));
    }

    @Test
    public void partitioning_collects_a_tupple_whose_first_element_contains_all_stream_elements_satisfying_predicate_and_second_element_contains_the_rest() {
        assertEquals(t2(List.of(1), List.of()), Stream.of(1).collect(partitioning(x -> x > 0)));
        assertEquals(t2(List.of(1, 2, 3), List.of()), Stream.of(1, 2, 3).collect(partitioning(x -> x > 0)));
        assertEquals(t2(List.of(), List.of(0)), Stream.of(0).collect(partitioning(x -> x > 0)));
        assertEquals(t2(List.of(1, 2), List.of(-1, -2)), Stream.of(-1, 1, -2, 2).collect(partitioning(x -> x > 0)));
    }

}
