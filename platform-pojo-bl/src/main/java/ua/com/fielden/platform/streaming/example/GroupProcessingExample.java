package ua.com.fielden.platform.streaming.example;

import java.util.stream.Stream;

import ua.com.fielden.platform.streaming.GroupProcessor;
import ua.com.fielden.platform.streaming.GroupSplitterator;

public class GroupProcessingExample {
    public static void main(String[] args) {
        // the base stream should really be ordered to make the grouping actually work!
        // the base stream below is not ordered on purpose to demonstrate the implication
        final Stream<String> baseStream = Stream.of("1", "1", "2", "2", "2", "3", "4", "4", "1");
        
        final GroupProcessor<String> processor = new GroupProcessor<>(
                (seed, valueToCheck) -> seed.equals(valueToCheck), 
                group -> System.out.printf("Processing: group '%s', size %s.\n", group.get(0), group.size()));

        final Stream<String> stream = GroupSplitterator.stream(baseStream, processor);
        
        stream.peek(System.out::println).forEach(processor);
    }
}
