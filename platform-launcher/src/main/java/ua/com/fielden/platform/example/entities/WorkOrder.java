package ua.com.fielden.platform.example.entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.KeyType;

/**
 * Represents a simplified work order entity.
 *
 * TODO Need to enhance this entity to support all equipment types and reflect the concept of open and closed work orders.
 *
 * @author 01es
 *
 */
@KeyType(String.class)
public class WorkOrder extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    private Wagon equipment;
    private WorkOrderStatus status;
    private Workshop workshop;


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

    public void setEquipment(final Wagon equipment) {
        this.equipment = equipment;
    }

    public WorkOrderStatus getStatus() {
        return status;
    }

    public void setStatus(final WorkOrderStatus status) {
        this.status = status;
    }

    public Workshop getWorkshop() {
        return workshop;
    }

    protected void setWorkshop(final Workshop workshop) {
        this.workshop = workshop;
    }

}
