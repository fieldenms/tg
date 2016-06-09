package ua.com.fielden.platform.streaming;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

/**
 * An abstraction that continuously groups the data consumed from an associated stream with an actual processing logic that gets executed on each identified group.
 * The group processing is progressive. That is, as soon as the first group is identified (by using bi-predicate) it gets passed into the group processor (Consumer of List).
 * Then the just processed group gets discarded and the consumption of the data from the stream continues in search of the next group.  
 * 
 * @author TG Team
 *
 * @param <T>
 */
public class GroupProcessor<T> implements Consumer<T> {

    private final List<T> groupToProcess = new ArrayList<>();
    private final BiPredicate<T, T> belongsToGroupPredicate;
    private final Consumer<List<T>> processor;
   
    /**
     * Constructs group processor.
     * 
     * @param belongsToGroupPredicate -- A bi-predicate where the first argument is the seed element provided by the internal logic and the second argument is the one just consumed from the stream.
     *                                   So, basically the provided bi-predicate should either be commutative and establish an equivalence relation (in most cases this should hold true) or written by using the specified order of arguments. 
     * @param processor -- an implementation to process the provided group as a list of elements with preserved order in which they where consumed from the stream.
     */
    public GroupProcessor(final BiPredicate<T, T> belongsToGroupPredicate, final Consumer<List<T>> processor) {
        this.belongsToGroupPredicate = belongsToGroupPredicate;
        this.processor = processor;
    }
    
    @Override
    public void accept(final T value) {
        // if this is the first value for the group then simply add it
        // to be processed when the group is formed or there would be no more data to process
        // effectively this value becomes a seed element for forming a group
        if (groupToProcess.isEmpty()) {
            groupToProcess.add(value);
        } else {
            // there are two possibilities here
            // 1. the current value fits into the current group then simply add it
            // 2. the current value does not fit into the current group
            //    this means that the current group needs to be processed and emptied
            //    and the current value needs to be added as the first element of the new current group to be processed
            if (belongsToGroupPredicate.test(groupToProcess.get(0), value)) {
                groupToProcess.add(value);
            } else {
                processor.accept(groupToProcess);
                groupToProcess.clear();
                groupToProcess.add(value);
            }
        }
    }

    /**
     * Unfortunately, there is no way to determine the fact of receiving the last element of the stream in method <code>accept</code>.
     * This method is intended to be invoked from outside of this consumer in the event where there is no additional data in the stream that would otherwise kick in the <code>accept</code> call.
     * Such situation occurs for every stream after consuming its last element that may belong to the current group or be a subject for a separate group.
     */
    public void completeProcessing() {
        // do processing of the current and last group 
        processor.accept(groupToProcess);
        groupToProcess.clear();
    }

}
