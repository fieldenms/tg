package ua.com.fielden.platform.test.domain.entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.entity.validation.annotation.Final;
import ua.com.fielden.platform.entity.validation.annotation.NotNull;

/**
 * Represents a work order status required field.
 * 
 * @author TG Team
 * 
 */
@KeyType(DynamicEntityKey.class)
public class WoStatusRequiredField extends AbstractEntity<DynamicEntityKey> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @CompositeKeyMember(1)
    private WorkOrderStatus woStatus;
    @IsProperty(WorkOrder.class)
    @CompositeKeyMember(2)
    private PropertyDescriptor<WorkOrder> requiredProperty;

    /**
     * Default constructor for instantiation by Hibernate.
     */
    protected WoStatusRequiredField() {
        super(null, null, "");
        setKey(new DynamicEntityKey(this));
    }

    /**
     * The main constructor.
     * 
     * @param
     */
    public WoStatusRequiredField(final WorkOrderStatus woStatus, final PropertyDescriptor<WorkOrder> requiredField) {
        this();
        setKey(new DynamicEntityKey(this));
        setWoStatus(woStatus);
        setRequiredProperty(requiredField);
    }

    public WorkOrderStatus getWoStatus() {
        return woStatus;
    }

    @NotNull
    @Final
    @Observable
    public void setWoStatus(final WorkOrderStatus woStatus) {
        this.woStatus = woStatus;
    }

    public PropertyDescriptor<WorkOrder> getRequiredProperty() {
        return requiredProperty;
    }

    @NotNull
    @Final
    @Observable
    public void setRequiredProperty(final PropertyDescriptor<WorkOrder> requiredField) {
        this.requiredProperty = requiredField;
    }
}