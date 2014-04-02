package ua.com.fielden.platform.test.domain.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.validation.annotation.DomainValidation;
import ua.com.fielden.platform.entity.validation.annotation.EntityExists;
import ua.com.fielden.platform.entity.validation.annotation.Final;
import ua.com.fielden.platform.entity.validation.annotation.NotNull;
import ua.com.fielden.platform.error.Result;

/**
 * RMA advice business entity.
 * 
 * @author 01es
 * 
 */
@KeyType(Long.class)
public class Advice extends AbstractEntity<Long> {
    private static final long serialVersionUID = 1L;

    private final List<AdvicePosition> positions = new ArrayList<AdvicePosition>();
    @IsProperty
    private Date dateRaised;
    @IsProperty
    private Date dateDispatched;
    /**
     * For advices initiated in PNL workshops, presence of a contractor workshop in this filed indicates the fact of the final dispatch (i.e. from PNL to Contractor).
     * 
     */
    @IsProperty
    private Workshop dispatchedToWorkshop;
    @IsProperty
    private boolean road = false;
    @IsProperty
    private Wagon carrier;
    /**
     * Indicates a workshop where this advice was created. If this workshop is Contractor's then this advice delivers fixed rotables to PNL. Otherwise, this an advice delivering
     * bogies to a Contractor's workshop for repair.
     */
    @IsProperty
    private Workshop initiatedAtWorkshop;

    @IsProperty
    private boolean received = false;

    /**
     * string representation of the property name for "carrier" property
     */
    public final static String PROPERTY_CARRIER = "carrier";

    /**
     * Constructor for (@link EntityFactory}.
     */
    protected Advice() {
        setDateRaised(new Date());
    }

    /**
     * TODO: should be removed.
     * 
     * The main constructor.
     * 
     * @param number
     *            -- unique advice number
     * @param desc
     * @param numberOfPositions
     */
    public Advice(final Long number, final String desc) {
        super(null, number, desc);
        setDateRaised(new Date());
    }

    public void updateNumberOfPosition(final int numberOfPositions) {
        if (getKey() == -1L) {
            positions.clear();
            for (int index = 0; index < numberOfPositions; index++) {
                positions.add(getEntityFactory().newByKey(AdvicePosition.class, this, index + 1)); // plus 1 to make position non-zero based
            }
        }
    }

    /**
     * Should be used construction of brand new advices. TODO: should replace current implementation with commented one.
     * 
     * @param desc
     */
    public Advice(final String desc) {
        this(-1L, desc);
        this.dateRaised = new Date();
    }

    /**
     * Ensures that key and id are the same.
     * 
     * TODO: This approach is somewhat questionable.
     */
    @Override
    protected void setId(final Long id) {
        super.setId(id);
        setKey(id);
    }

    public List<AdvicePosition> getPositions() {
        return Collections.unmodifiableList(positions);
    }

    public Date getDateRaised() {
        return dateRaised;
    }

    @NotNull
    @Final
    @Observable
    protected Advice setDateRaised(final Date dateRaised) {
        this.dateRaised = dateRaised;
        return this;
    }

    public Date getDateDispatched() {
        return dateDispatched;
    }

    @NotNull
    @Observable
    public Advice setDateDispatched(final Date dateDispatched) {
        this.dateDispatched = dateDispatched;
        return this;
    }

    public boolean isRoad() {
        return road;
    }

    @Observable
    @DomainValidation
    public Advice setRoad(final boolean road) {
        this.road = road;
        return this;
    }

    public Wagon getCarrier() {
        return carrier;
    }

    @Observable
    @EntityExists(Wagon.class)
    @DomainValidation
    public Advice setCarrier(final Wagon carrier) {
        this.carrier = carrier;
        return this;
    }

    public int getNumberOfVacantPositions() {
        return getVacantPositions().size();
    }

    public boolean isDispatched() {
        return getDispatchedToWorkshop() != null;
    }

    public boolean isReceived() {
        return received;
    }

    @Observable
    public Advice setReceived(final boolean received) {
        this.received = received;
        return this;
    }

    /**
     * Iterates through all positions and tests them for pending. If there is at least one pending position this method returns <code>true</code>, otherwise false.
     * 
     * @return
     */
    public boolean hasPendingPositions() {
        for (final AdvicePosition position : getPositions()) {
            if (position.isPending()) {
                return true;
            }
        }
        return false;
    }

    public Workshop getInitiatedAtWorkshop() {
        return initiatedAtWorkshop;
    }

    /**
     * Sets initiatedAtWorkshop value. This assignment can be done only once, and hopefully before this advice instance is persisted.
     * 
     * This setter is not intended to be used for setting values specified by a user via UI -- only programmatically.
     * 
     * @param initiatedAtWorkshop
     */
    @NotNull
    @Final
    @EntityExists(Workshop.class)
    @Observable
    public void setInitiatedAtWorkshop(final Workshop initiatedAtWorkshop) {
        this.initiatedAtWorkshop = initiatedAtWorkshop;
    }

    public Workshop getDispatchedToWorkshop() {
        return dispatchedToWorkshop;
    }

    /**
     * Sets a workshop as part of the dispatch process.
     * 
     * @param dispatchedToWorkshop
     */
    @NotNull
    @EntityExists(Workshop.class)
    @Observable
    public void setDispatchedToWorkshop(final Workshop dispatchedToWorkshop) {
        this.dispatchedToWorkshop = dispatchedToWorkshop;
    }

    /**
     * Return a list of vacant positions that can be used for placement of rotables.
     * 
     * @return
     */
    public List<AdvicePosition> getVacantPositions() {
        final List<AdvicePosition> result = new ArrayList<AdvicePosition>();
        for (final AdvicePosition position : getPositions()) {
            if (position.isVacant()) {
                result.add(position);
            }
        }
        return result;
    }

    /**
     * Checks the Advice validation logic. Includes : 1) all properties validation 2) all aggregated positions validation 3) checks unicity of the rotables of the aggregated
     * positions. 4) checks if the carrier is specified when advise is not road.
     */
    @Override
    public Result validate() {
        // inherited validation
        final Result superResult = super.validate();
        if (!superResult.isSuccessful()) {
            return superResult;
        }

        // checks if the carrier is specified if not IsRoad
        if (!isRoad() && getCarrier() == null) {
            return new Result(this, new IllegalArgumentException("Either advice should be marked 'road' or carrier specified."));
        } else if (isRoad() && getCarrier() != null) {
            return new Result(this, new IllegalArgumentException("Road and carrier cannot be specified simultaneously."));
        }

        // checks if the aggregated positions are valid. All the positions have to be valid
        for (final AdvicePosition position : getPositions()) {
            final Result result = position.isValid();
            if (!result.isSuccessful()) {
                return result;
            }
        }

        // checks if the rotables of the aggregated positions are unique.
        final List<Rotable> rotables = new ArrayList<Rotable>();
        for (final AdvicePosition position : getPositions()) {
            final Rotable rotable = position.getRotable();
            if (rotable != null) {
                if (rotables.contains(rotable)) {
                    return new Result(this, new IllegalArgumentException("Rotable " + rotable.getKey() + " is a duplicate."));
                }
                rotables.add(rotable);
            }
        }

        return new Result(this, "Advice " + this + " is valid.");
    }

    /**
     * Advice is active from PNL perspective if: 1. Advice is not received. AND 2. Advice is initiated at PNL, not dispatched or dispatched to another PNL workshop OR Advice is
     * initiated at Contractor workshop, dispatched to a PNL workshop.
     * 
     * @return
     */
    public boolean isActiveForPnl() {
        if (isReceived()) {
            return false;
        }

        if ((!getInitiatedAtWorkshop().isContractorWorkshop() && (getDispatchedToWorkshop() == null || !getDispatchedToWorkshop().isContractorWorkshop()))
                || (getInitiatedAtWorkshop().isContractorWorkshop() && getDispatchedToWorkshop() != null && !getDispatchedToWorkshop().isContractorWorkshop())) {
            return true;
        }
        return false;
    }

    /**
     * Advice is active from Contractor perspective if: 1. Advice is not received. AND 2. Advice is initiated at Contractor, not dispatched or dispatched to another Contractor
     * workshop OR Advice is initiated at PNL workshop, dispatched to a Contractor workshop.
     * 
     * @return
     */
    public boolean isActiveForContractor() {
        if (isReceived()) {
            return false;
        }

        if ((getInitiatedAtWorkshop().isContractorWorkshop() && (getDispatchedToWorkshop() == null || getDispatchedToWorkshop().isContractorWorkshop()))
                || (!getInitiatedAtWorkshop().isContractorWorkshop() && getDispatchedToWorkshop() != null && getDispatchedToWorkshop().isContractorWorkshop())) {
            return true;
        }
        return false;
    }

    /**
     * A convenience method for obtaining rotables currently associated with advice positions
     * 
     * @return
     */
    public List<Rotable> rotables() {
        final List<Rotable> rotables = new ArrayList<Rotable>();
        for (final AdvicePosition pos : getPositions()) {
            if (pos.getRotable() != null) {
                rotables.add(pos.getRotable());
            }
        }
        return rotables;
    }

}
