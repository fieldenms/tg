package ua.com.fielden.platform.swing.booking;

import java.util.Comparator;
import java.util.TreeSet;

import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;

import ua.com.fielden.platform.entity.AbstractEntity;

public class BookingTaskSeries<T extends AbstractEntity<?>, ST extends AbstractEntity<?>> extends TaskSeries {

   private static final long serialVersionUID = -4380791797451456534L;

   private TreeSet<BookingTask<T, ST>> tasks;

    public BookingTaskSeries(final String name) {
	super(name);
	tasks = new TreeSet<>(createComparator());
    }

    private Comparator<BookingTask<T, ST>> createComparator() {
	return new Comparator<BookingTask<T, ST>>() {

	    @Override
	    public int compare(final BookingTask<T, ST> o1, final BookingTask<T, ST> o2) {
		return o1.getFrom().compareTo(o2.getFrom());
	    }
	};
    }

    /**
     * Adds a task to the series and sends a
     * {@link org.jfree.data.general.SeriesChangeEvent} to all registered
     * listeners.
     *
     * @param task  the task (<code>null</code> not permitted).
     */
    @SuppressWarnings("unchecked")
    @Override
    public void add(final Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Null 'task' argument.");
        }
        if(!(task instanceof BookingTask)) {
            throw new IllegalArgumentException("The task must be of BookingTask type");
        }
        super.add(task);
        tasks.add((BookingTask<T, ST>)task);
    }

    /**
     * Removes a task from the series and sends
     * a {@link org.jfree.data.general.SeriesChangeEvent}
     * to all registered listeners.
     *
     * @param task  the task.
     */
    @Override
    public void remove(final Task task) {
	super.remove(task);
        tasks.remove(task);
    }

    /**
     * Removes all tasks from the series and sends
     * a {@link org.jfree.data.general.SeriesChangeEvent}
     * to all registered listeners.
     */
    @Override
    public void removeAll() {
	super.removeAll();
        tasks.clear();
    }

    /**
     * Returns the number of items in the series.
     *
     * @return The item count.
     */
    @Override
    public int getItemCount() {
        return tasks.size();
    }

    /**
     * Returns a task from the series.
     *
     * @param index  the task index (zero-based).
     *
     * @return The task.
     */
    @SuppressWarnings("unchecked")
    @Override
    public BookingTask<T, ST> get(final int index) {
        return (BookingTask<T, ST>)super.get(index);
    }

    /**
     * Returns the task in the series that has the specified description.
     *
     * @param description  the name (<code>null</code> not permitted).
     *
     * @return The task (possibly <code>null</code>).
     */
    @Override
    public BookingTask<T, ST> get(final String description) {
	for(final BookingTask<T, ST> t : tasks) {
	    if (t.getDescription().equals(description)) {
		return t;
	    }
	}
        return null;
    }


    /**
     * Tests this object for equality with an arbitrary object.
     *
     * @param obj  the object to test against (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof BookingTaskSeries)) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        final BookingTaskSeries<T, ST> that = (BookingTaskSeries<T, ST>) obj;
        if (!this.tasks.equals(that.tasks)) {
            return false;
        }
        return true;
    }

    /**
     * Returns an independent copy of this series.
     *
     * @return A clone of the series.
     *
     * @throws CloneNotSupportedException if there is some problem cloning
     *     the dataset.
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object clone() throws CloneNotSupportedException {
        final BookingTaskSeries<T, ST> clone = (BookingTaskSeries<T, ST>) super.clone();
        clone.tasks = (TreeSet<BookingTask<T, ST>>) tasks.clone();
        return clone;
    }

    public BookingTask<T, ST> lower(final BookingTask<T, ST> toKey) {
	return tasks.lower(toKey);
    }

    public BookingTask<T, ST> higher(final BookingTask<T, ST> fromKey) {
	return tasks.higher(fromKey);
    }
}
