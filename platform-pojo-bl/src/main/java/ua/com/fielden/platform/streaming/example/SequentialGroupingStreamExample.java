package ua.com.fielden.platform.streaming.example;

import java.util.List;
import java.util.stream.Stream;

import ua.com.fielden.platform.streaming.SequentialGroupingStream;

public class SequentialGroupingStreamExample {
    private SequentialGroupingStreamExample() {}
    
    public static void main(String[] args) {
        
        System.out.println("Group into groups of equal sequentially:");
        
        final Stream<List<String>> stream = SequentialGroupingStream.stream(
                Stream.of("1", "1", "2", "2", "2", "3", "4", "4", "1"), 
                (el, group) -> group.isEmpty() || group.get(0).equals(el));
        
        stream.forEach(lst -> System.out.println("\t" + lst));
        
        System.out.println("\nGroup into groups of 2");
        
        final Stream<List<String>> groupBy2 = SequentialGroupingStream.stream(
                Stream.of("1", "1", "2", "2", "2", "3", "4", "4", "1"), 
                (el, group) -> group.size() < 2);
        
        groupBy2.forEach(lst -> System.out.println("\t" + lst));

        System.out.println("Group into groups of 2 for an empty stream");
        
        final Stream<List<String>> groupBy3 = SequentialGroupingStream.stream(Stream.of(), (valueToCheck, list) -> list.size() < 2);
        
        groupBy3.forEach(lst -> System.out.println("\t" + lst));

    }
}
