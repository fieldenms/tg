package ua.com.fielden.platform.example.swing.schedule;

import java.util.Date;

public class WorkOrderEntity {

    private String key;
    private Date earlyStart;
    private Date earlyFinish;
    private Date actualStart;
    private Date actualFinish;

    public WorkOrderEntity(final String key) {
	this.key = key;
    }

    public String getKey() {
        return key;
    }

    public WorkOrderEntity setKey(final String key) {
        this.key = key;
        return this;
    }

    public Date getEarlyStart() {
        return earlyStart;
    }

    public WorkOrderEntity setEarlyStart(final Date earlyStart) {
        this.earlyStart = earlyStart;
        return this;
    }

    public Date getEarlyFinish() {
        return earlyFinish;
    }

    public WorkOrderEntity setEarlyFinish(final Date earlyFinish) {
        this.earlyFinish = earlyFinish;
        return this;
    }

    public Date getActualStart() {
        return actualStart;
    }

    public WorkOrderEntity setActualStart(final Date actualStart) {
        this.actualStart = actualStart;
        return this;
    }

    public Date getActualFinish() {
        return actualFinish;
    }

    public WorkOrderEntity setActualFinish(final Date actualFinish) {
        this.actualFinish = actualFinish;
        return this;
    }
}
