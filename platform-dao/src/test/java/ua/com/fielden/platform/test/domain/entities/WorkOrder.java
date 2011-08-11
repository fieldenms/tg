package ua.com.fielden.platform.test.domain.entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;

/**
 * Represents a simplified work order entity.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Wo No")
@DescTitle(value = "Description")
public class WorkOrder extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @Title(value = "Wagon", desc = "Wagon being worked on")
    private Wagon equipment;
    @IsProperty
    @Title(value = "Status", desc = "Current work order status")
    private WorkOrderStatus status;
    @IsProperty
    @Title(value = "Workshop", desc = "Workshop where work is done")
    private Workshop workshop;
    @IsProperty(WorkOrder.class)
    @Title(value = "Important Property", desc = "Property that has a special meaning")
    private PropertyDescriptor<WorkOrder> importantProperty;


    /**
     * Constructor for Hibernate.
     */
    protected WorkOrder() {

    }

    /**
     * The main constructor.
     *
     * @param number
     * @param desc
     * @param equipment
     */
    public WorkOrder(final String number, final String desc, final Wagon equipment) {
	super(null, number, desc);
	setEquipment(equipment);
	setStatus(WorkOrderStatus.E);
    }

    public Wagon getEquipment() {
        return equipment;
    }

    @Observable
    public void setEquipment(final Wagon equipment) {
        this.equipment = equipment;
    }

    public WorkOrderStatus getStatus() {
        return status;
    }

    @Observable
    public void setStatus(final WorkOrderStatus status) {
        this.status = status;
    }

    public Workshop getWorkshop() {
        return workshop;
    }

    @Observable
    public void setWorkshop(final Workshop workshop) {
        this.workshop = workshop;
    }

    public PropertyDescriptor<WorkOrder> getImportantProperty() {
        return importantProperty;
    }

    @Observable
    public void setImportantProperty(final PropertyDescriptor<WorkOrder> importantProperty) {
        this.importantProperty = importantProperty;
    }

}
