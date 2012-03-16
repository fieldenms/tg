package ua.com.fielden.platform.test.domain.entities;

import java.util.Date;

import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.validation.annotation.DomainValidation;
import ua.com.fielden.platform.entity.validation.annotation.EntityExists;
import ua.com.fielden.platform.entity.validation.annotation.Final;
import ua.com.fielden.platform.entity.validation.annotation.GreaterOrEqual;
import ua.com.fielden.platform.entity.validation.annotation.NotNull;
import ua.com.fielden.platform.error.Result;

/**
 * Position within an advice that may or may no contain a rotable.
 *
 * @author 01es
 *
 */
@KeyType(DynamicEntityKey.class)
public class AdvicePosition extends RotableLocation<DynamicEntityKey> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @CompositeKeyMember(1)
    private Advice advice;
    @IsProperty
    @CompositeKeyMember(2)
    private Integer position;
    @IsProperty
    private Rotable rotable;
    @IsProperty
    private Workshop sendingWorkshop;
    @IsProperty
    private Workshop receivingWorkshop;
    @IsProperty
    private Date placementDate;
    @IsProperty
    private boolean received = false;
    @IsProperty
    private Date receivedDate;
    @IsProperty
    private boolean removeBearing = false;

    /**
     * Default constructor for instantiation by Hibernate.
     */
    protected AdvicePosition() {
	super(null, null, "");
	setKey(new DynamicEntityKey(this));
    }

    /**
     * The main constructor.
     *
     * @param advice
     */
    public AdvicePosition(final Advice advice, final Integer position) {
	this();
	setKey(new DynamicEntityKey(this));
	setAdvice(advice);
	setPosition(position);
    }

    public Advice getAdvice() {
	return advice;
    }

    @NotNull
    @Final
    @Observable
    protected void setAdvice(final Advice advice) {
	this.advice = advice;
    }

    public Integer getPosition() {
	return position;
    }

    @NotNull
    @Final
    @GreaterOrEqual(1)
    @Observable
    protected void setPosition(final Integer position) {
	this.position = position;
    }

    public Rotable getRotable() {
	return rotable;
    }

    @NotNull
    @Final
    @EntityExists(Rotable.class)
    @DomainValidation
    @Observable
    public AdvicePosition setRotable(final Rotable rotable) {
	this.rotable = rotable;
	return this;
    }

    public Workshop getSendingWorkshop() {
	return sendingWorkshop;
    }

    @NotNull
    @Final
    @EntityExists(Workshop.class)
    @Observable
    public AdvicePosition setSendingWorkshop(final Workshop sendingWorkshop) {
	this.sendingWorkshop = sendingWorkshop;
	return this;
    }

    public Workshop getReceivingWorkshop() {
	return receivingWorkshop;
    }

    @NotNull
    @EntityExists(Workshop.class)
    @Observable
    public AdvicePosition setReceivingWorkshop(final Workshop receivingWorkshop) throws Result {
	// TODO the isReceived() should be moved to a domain validator
	if (isReceived()) {
	    throw new Result(this, "Receiving workshop cannot be changed once position is received.", new IllegalStateException(
		    "Receiving workshop cannot be changed once position is received."));
	}
	this.receivingWorkshop = receivingWorkshop;
	return this;
    }

    public Date getPlacementDate() {
	return placementDate;
    }

    @NotNull
    @Final
    @Observable
    public AdvicePosition setPlacementDate(final Date placementDate) {
	this.placementDate = placementDate;
	return this;
    }

    public boolean hasRotable() {
	return getRotable() != null;
    }

    public boolean isReceived() {
	return received;
    }

    @Observable
    public AdvicePosition setReceived(final boolean received) {
	this.received = received;
	return this;
    }

    /**
     * Returns true if the rotable is assigned, but not received -- this means receiving is pending.
     *
     * @return
     */
    public boolean isPending() {
	return hasRotable() && !isReceived();
    }

    /**
     * Switches position to the very original state.
     */
    public void clear() {
	rotable = null;
	sendingWorkshop = null;
	receivingWorkshop = null;
	placementDate = null;
	received = false;
	receivedDate = null;
	removeBearing = false;
    }

    /**
     * Determines if this position can have anything placed on it.
     *
     * @return
     */
    public boolean isVacant() {
	return getRotable() == null;
    }

    @Override
    public Result validate() {
	final Result superResult = super.validate();
	if (!superResult.isSuccessful()) {
	    return superResult;
	} else if (rotable == null) {
	    String errorMessage = null;
	    if (sendingWorkshop != null) {
		errorMessage = "Position " + getPosition() + " contains no rotable but 'Sending workshop' is set to " + sendingWorkshop.getKey();
	    } else if (receivingWorkshop != null) {
		errorMessage = "Position " + getPosition() + " contains no rotable but 'Receiving workshop' is set to " + receivingWorkshop.getKey();
	    } else if (placementDate != null) {
		errorMessage = "Position " + getPosition() + " contains no rotable but 'Placement date' is set";
	    } else if (received) {
		errorMessage = "Position " + getPosition() + " contains no rotable but it is marked as 'received'";
	    } else if (receivedDate != null) {
		errorMessage = "Position " + getPosition() + " contains no rotable but 'Received date' is set";
	    } else if (removeBearing) {
		errorMessage = "Position " + getPosition() + " contains no rotable but 'Remove bearings' flag is set";
	    }
	    if(errorMessage != null) {
		return new Result(this, errorMessage, new Exception(errorMessage));
	    }
	}
	return superResult;
    }

    public Date getReceivedDate() {
	return receivedDate;
    }

    @Observable
    public AdvicePosition setReceivedDate(final Date receivedDate) {
	this.receivedDate = receivedDate;
	return this;
    }

    public boolean isRemoveBearing() {
	return removeBearing;
    }

    @Observable
    public void setRemoveBearing(final boolean removeBearing) {
	System.out.println("setRemoveBearing : " + removeBearing);
	this.removeBearing = removeBearing;
    }
}
