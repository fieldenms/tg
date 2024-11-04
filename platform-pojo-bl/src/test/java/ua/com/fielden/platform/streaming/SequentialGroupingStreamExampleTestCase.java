package ua.com.fielden.platform.streaming;

import org.junit.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;

public class SequentialGroupingStreamExampleTestCase {

    @Test
    public void grouping_by_sequentally_equal_elements_works_correctly_without_missing_first_and_last_stream_elements() {
        final Stream<List<String>> stream = SequentialGroupingStream.stream(
                Stream.of("1", "1", "2", "2", "2", "3", "4", "4", "1"), 
                (el, group) -> group.isEmpty() || group.get(0).equals(el));
        
        final List<List<String>> groups = stream.collect(Collectors.toList());

        assertEquals(5, groups.size());
        
        assertEquals(2, groups.get(0).size());
        assertEquals("1", groups.get(0).get(0));
        assertEquals("1", groups.get(0).get(1));
        
        assertEquals(3, groups.get(1).size());
        assertEquals("2", groups.get(1).get(0));
        assertEquals("2", groups.get(1).get(1));
        assertEquals("2", groups.get(1).get(2));
        
        assertEquals(1, groups.get(2).size());
        assertEquals("3", groups.get(2).get(0));
        
        assertEquals(2, groups.get(3).size());
        assertEquals("4", groups.get(3).get(0));
        assertEquals("4", groups.get(3).get(1));

        assertEquals(1, groups.get(4).size());
        assertEquals("1", groups.get(4).get(0));
    }
    
    @Test
    public void grouping_by_the_specified_number_of_elements_works_correctly_for_streams_with_odd_number_of_elements() {
        final Stream<List<String>> stream = SequentialGroupingStream.stream(
                Stream.of("1", "1", "2", "2", "2", "3", "4", "4", "1"), 
                (el, group) -> group.size() < 2);
        
        final List<List<String>> groups = stream.collect(Collectors.toList());

        assertEquals(5, groups.size());
        
        assertEquals(2, groups.get(0).size());
        assertEquals("1", groups.get(0).get(0));
        assertEquals("1", groups.get(0).get(1));
        
        assertEquals(2, groups.get(1).size());
        assertEquals("2", groups.get(1).get(0));
        assertEquals("2", groups.get(1).get(1));
        
        assertEquals(2, groups.get(2).size());
        assertEquals("2", groups.get(2).get(0));
        assertEquals("3", groups.get(2).get(1));
        
        assertEquals(2, groups.get(3).size());
        assertEquals("4", groups.get(3).get(0));
        assertEquals("4", groups.get(3).get(1));

        assertEquals(1, groups.get(4).size());
        assertEquals("1", groups.get(4).get(0));
    }

    @Test
    public void grouping_by_the_specified_number_of_elements_works_correctly_for_streams_with_even_number_of_elements() {
        final Stream<List<String>> stream = SequentialGroupingStream.stream(
                Stream.of("1", "1", "2", "2", "2", "3", "4", "4", "1", "5"), 
                (el, group) -> group.size() < 2);
        
        final List<List<String>> groups = stream.collect(Collectors.toList());

        assertEquals(5, groups.size());
        
        assertEquals(2, groups.get(0).size());
        assertEquals("1", groups.get(0).get(0));
        assertEquals("1", groups.get(0).get(1));
        
        assertEquals(2, groups.get(1).size());
        assertEquals("2", groups.get(1).get(0));
        assertEquals("2", groups.get(1).get(1));
        
        assertEquals(2, groups.get(2).size());
        assertEquals("2", groups.get(2).get(0));
        assertEquals("3", groups.get(2).get(1));
        
        assertEquals(2, groups.get(3).size());
        assertEquals("4", groups.get(3).get(0));
        assertEquals("4", groups.get(3).get(1));

        assertEquals(2, groups.get(4).size());
        assertEquals("1", groups.get(4).get(0));
        assertEquals("5", groups.get(4).get(1));
    }


    @Test
    public void grouping_by_the_specified_number_of_elements_works_correctly_for_streams_with_a_single_element() {
        final Stream<List<String>> stream = SequentialGroupingStream.stream(
                Stream.of("1"), 
                (el, group) -> group.size() < 2);
        
        final List<List<String>> groups = stream.collect(Collectors.toList());

        assertEquals(1, groups.size());
        
        assertEquals(1, groups.get(0).size());
        assertEquals("1", groups.get(0).get(0));
    }

    @Test
    public void grouping_an_empty_stream_produces_an_empty_stream() {
        final Stream<List<String>> stream = SequentialGroupingStream.stream(Stream.of(), (valueToCheck, list) -> list.size() < 2);
        
        final List<List<String>> groups = stream.collect(Collectors.toList());
        
        assertEquals(0, groups.size());
    }

    @Test
    public void grouping_of_elements_that_do_not_satisfy_the_group_condition_produces_a_stream_of_single_element_groups() {
        final Stream<List<String>> stream = SequentialGroupingStream.stream(
                Stream.of("1", "1", "2"), 
                (el, group) -> "non existing".equals(el));
        
        final List<List<String>> groups = stream.collect(Collectors.toList());

        assertEquals(3, groups.size());

        assertEquals(1, groups.get(0).size());
        assertEquals("1", groups.get(0).get(0));

        assertEquals(1, groups.get(1).size());
        assertEquals("1", groups.get(1).get(0));

        assertEquals(1, groups.get(2).size());
        assertEquals("2", groups.get(2).get(0));
    }

    @Test
    public void base_stream_is_closed_upon_closing_the_grouping_stream_but_not_upon_terminal_op() {
        final AtomicBoolean baseStreamIsClosed = new AtomicBoolean(false);
        final Stream<String> baseStream = Stream.of("1", "1", "2").onClose(() -> baseStreamIsClosed.set(true));
        try(final Stream<List<String>> stream = SequentialGroupingStream.stream(
                baseStream,
                (el, group) -> group.isEmpty() || group.getFirst().equals(el))) {
            // run a terminal operation
            final List<List<String>> groups = stream.collect(Collectors.toList());
            assertEquals(2, groups.size());
            assertFalse("Base stream was closed prematurely.", baseStreamIsClosed.get());
        }
        assertTrue(baseStreamIsClosed.get());
    }

}
