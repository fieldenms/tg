package ua.com.fielden.platform.example.swing.schedule;

import java.util.Date;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.Dependent;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.annotation.UpperCase;
import ua.com.fielden.platform.entity.validation.annotation.EntityExists;
import ua.com.fielden.platform.entity.validation.annotation.GeProperty;
import ua.com.fielden.platform.entity.validation.annotation.LeProperty;
import ua.com.fielden.platform.entity.validation.annotation.NotNull;
import ua.com.fielden.platform.error.Result;

@KeyTitle(value = "Wo No", desc = "Work order number")
@KeyType(String.class)
@DescTitle(value = "Description", desc = "Work order description")
@EntityTitle(value = "Work Order", desc = "Domain entity representing an order for a maintenance of equipment.")
public class WorkOrderEntity extends AbstractEntity<String> {

    private static final long serialVersionUID = -4000034917653651392L;

    @IsProperty
    @Title(value = "Early Start", desc = "Early Start")
    @Dependent("earlyFinish")
    private Date earlyStart;

    @IsProperty
    @Title(value = "Early Finish", desc = "Early Finish")
    @Dependent("earlyStart")
    private Date earlyFinish;

    @IsProperty
    @Title(value = "Actual Start", desc = "Actual Start")
    @Dependent({ "actualFinish", "odometerReading" })
    private Date actualStart;

    @IsProperty
    @Title(value = "Actual Finish", desc = "Actual Finish")
    @Dependent("actualStart")
    private Date actualFinish;

    @IsProperty
    @UpperCase
    @Title(value = "Job#", desc = "Job number, used mainly as part of PM work orders.")
    private String jobNo;

    @IsProperty
    @MapTo
    @Title(value = "Work request", desc = "Work request")
    private WorkRequest workRequest;

    @Observable
    @EntityExists(WorkRequest.class)
    public WorkOrderEntity setWorkRequest(final WorkRequest workRequest) {
        this.workRequest = workRequest;
        return this;
    }

    public WorkRequest getWorkRequest() {
        return workRequest;
    }

    /////////////////////////////////////////////
    //////////////// GETTERS ////////////////////
    /////////////////////////////////////////////

    public Date getEarlyStart() {
        return earlyStart;
    }

    public Date getEarlyFinish() {
        return earlyFinish;
    }

    public Date getActualStart() {
        return actualStart;
    }

    public Date getActualFinish() {
        return actualFinish;
    }

    public String getJobNo() {
        return jobNo;
    }

    /////////////////////////////////////////////
    //////////////// SETTERS ////////////////////
    /////////////////////////////////////////////

    @Observable
    @LeProperty("earlyFinish")
    public WorkOrderEntity setEarlyStart(final Date earlyStart) throws Result {
        this.earlyStart = earlyStart;
        return this;
    }

    @Observable
    @GeProperty("earlyStart")
    public WorkOrderEntity setEarlyFinish(final Date earlyFinish) throws Result {
        this.earlyFinish = earlyFinish;
        return this;
    }

    @Observable
    @LeProperty("actualFinish")
    public WorkOrderEntity setActualStart(final Date actualStart) throws Result {
        this.actualStart = actualStart;
        return this;
    }

    @Observable
    @GeProperty("actualStart")
    public WorkOrderEntity setActualFinish(final Date actualFinish) throws Result {
        this.actualFinish = actualFinish;
        return this;
    }

    @Observable
    public WorkOrderEntity setJobNo(final String jobNo) {
        this.jobNo = jobNo;
        return this;
    }

    @Override
    @NotNull
    @Observable
    public WorkOrderEntity setKey(final String key) {
        super.setKey(key);
        return this;
    }

    @Override
    @Observable
    public WorkOrderEntity setDesc(final String desc) {
        super.setDesc(desc);
        return this;
    }
}
