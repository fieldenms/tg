package ua.com.fielden.platform.swing.timeline;

import java.awt.Color;
import java.util.Date;

import org.jfree.data.gantt.Task;
import org.jfree.data.time.TimePeriod;

/**
 * A task with a color.
 * 
 * @author Jhou
 * 
 */
public class ColoredTask extends Task {
    private static final long serialVersionUID = -7757946757233027416L;

    private final Color color;
    private final String info;

    public ColoredTask(final String description, final Date start, final Date end, final Color color, final String info) {
        super(description, start, end);

        this.color = color;
        this.info = info;
    }

    /**
     * Creates a new task with a specified color.
     * 
     * @param description
     *            the task description (<code>null</code> not permitted).
     * @param duration
     *            the task duration (<code>null</code> permitted).
     */
    public ColoredTask(final String description, final TimePeriod duration, final Color color, final String info) {
        super(description, duration);

        this.color = color;
        this.info = info;
    }

    public Color getColor() {
        return color;
    }

    public String getInfo() {
        return info;
    }

}
