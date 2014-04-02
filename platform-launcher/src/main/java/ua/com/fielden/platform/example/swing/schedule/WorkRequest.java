package ua.com.fielden.platform.example.swing.schedule;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.Dependent;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.validation.annotation.GeProperty;
import ua.com.fielden.platform.entity.validation.annotation.LeProperty;
import ua.com.fielden.platform.entity.validation.annotation.NotNull;
import ua.com.fielden.platform.error.Result;

@KeyTitle(value = "Work request", desc = "Work request number")
@KeyType(String.class)
@DescTitle(value = "Description", desc = "Work request description")
@EntityTitle(value = "Work Request", desc = "Domain entity representing an work request.")
public class WorkRequest extends AbstractEntity<String> {

    private static final long serialVersionUID = 616658360097771693L;

    @IsProperty(WorkOrderEntity.class)
    @Title(value = "Work orders", desc = "Work orders")
    private Set<WorkOrderEntity> workOrders = new HashSet<WorkOrderEntity>();

    @Observable
    protected WorkRequest setWorkOrders(final Set<WorkOrderEntity> workOrders) {
        this.workOrders.clear();
        this.workOrders.addAll(workOrders);
        return this;
    }

    public Set<WorkOrderEntity> getWorkOrders() {
        return Collections.unmodifiableSet(workOrders);
    }

    @IsProperty
    @Title(value = "Request Start", desc = "Request Start")
    @Dependent("requestFinish")
    private Date requestStart;

    @IsProperty
    @Title(value = "Request Finish", desc = "Request Finish")
    @Dependent("requestStart")
    private Date requestFinish;

    /////////////////////////////////////////////
    //////////////// GETTERS ////////////////////
    /////////////////////////////////////////////

    public Date getRequestStart() {
        return requestStart;
    }

    public Date getRequestFinish() {
        return requestFinish;
    }

    /////////////////////////////////////////////
    //////////////// SETTERS ////////////////////
    /////////////////////////////////////////////

    @Observable
    @LeProperty("requestFinish")
    public WorkRequest setRequestStart(final Date requestStart) throws Result {
        this.requestStart = requestStart;
        return this;
    }

    @Observable
    @GeProperty("requestStart")
    public WorkRequest setRequestFinish(final Date requestFinish) throws Result {
        this.requestFinish = requestFinish;
        return this;
    }

    @Override
    @NotNull
    @Observable
    public WorkRequest setKey(final String key) {
        super.setKey(key);
        return this;
    }

    @Override
    @Observable
    public WorkRequest setDesc(final String desc) {
        super.setDesc(desc);
        return this;
    }

}
