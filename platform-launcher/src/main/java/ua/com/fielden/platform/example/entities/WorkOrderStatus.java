package ua.com.fielden.platform.example.entities;

/**
 * Represents the concept of work order statuses.
 *
 * @author 01es
 *
 */
public enum WorkOrderStatus {
    E("Entered"),
    A("Active"),
    F("Finished"),
    C("Closed"),
    X("Cancelled");

    private final String desc;

    WorkOrderStatus(final String desc) {
	this.desc = desc;
    }

    @Override
    public String toString() {
        return desc;
    }

}
