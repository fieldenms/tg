package ua.com.fielden.platform.example.entities;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * This class implements basic logic for deferred rotables fitments/defitments (by "deferred" here it is meant that operations are not persisted immediately).
 * 
 * @author nc
 * 
 */
public class RotableMovementLogic {

    /**
     * Fits bogie onto the wagon slot: the wagon slot is assigned as a new location of the bogie; the bogie is added to the wagon rotables list.
     * 
     * @param bogieWithRotables
     * @param wagonWithRotables
     * @param slotPosition
     *            - position of slot; numeration starts from 1.
     * @throws Exception
     */
    public static void fitBogie(final BogieWithRotables bogieWithRotables, final WagonWithRotables wagonWithRotables, final Integer slotPosition) throws Exception {
        if (canSlotAcceptBogie(bogieWithRotables.getBogie(), wagonWithRotables, slotPosition)) {
            bogieWithRotables.getBogie().setLocation(wagonWithRotables.getWagon().getSlot(slotPosition));
            wagonWithRotables.getBogiesRotables().add(bogieWithRotables);
        } else {
            throw new Exception("The slot can not accept this bogie");
        }
    }

    /**
     * Defits bogie from the wagon slot to workshop: the workshop is assigned as a new location of the bogie; the bogie is removed from the wagon rotables list.
     * 
     * @param bogieWithRotables
     * @param wagonWithRotables
     * @param workshop
     */
    public static void defitBogie(final BogieWithRotables bogieWithRotables, final WagonWithRotables wagonWithRotables, final Workshop workshop) {
        bogieWithRotables.getBogie().setLocation(workshop);
        wagonWithRotables.getBogiesRotables().remove(bogieWithRotables);
    }

    /**
     * Checks whether given wagon slot can accept given bogie. Ensures that slot is empty and bogie is class compatible with the given wagon or slot just holds this very bogie.
     * 
     * @param bogie
     * @param wagonWithRotables
     * @param slotPosition
     *            - position of slot; numeration starts from 1.
     * @return
     * @throws Exception
     */
    public static Boolean canSlotAcceptBogie(final Bogie bogie, final WagonWithRotables wagonWithRotables, final Integer slotPosition) throws Exception {
        if ((wagonWithRotables.getBogieInSlotPosition(slotPosition) == null && wagonWithRotables.getWagon().isClassCompatible(bogie))
                || (wagonWithRotables.getBogieInSlotPosition(slotPosition) != null && wagonWithRotables.getBogieInSlotPosition(slotPosition).equals(bogie))) {
            System.out.println("slot " + slotPosition + " of wagon " + wagonWithRotables.getWagon().getKey() + " can accept bogie " + bogie.getKey());
            return true;
        } else {
            System.out.println("slot " + slotPosition + " of wagon " + wagonWithRotables.getWagon().getKey() + " can NOT accept bogie " + bogie.getKey());
            return false;
        }
    }

    /**
     * Fits wheelset onto the bogie slot: the bogie slot is assigned as a new location of the wheelset; the wheelset is added to the bogie rotables list.
     * 
     * @param wheelset
     * @param bogieWithRotables
     * @param slotPosition
     *            - position of slot; numeration starts from 1.
     * @throws Exception
     */
    public static void fitWheelset(final Wheelset wheelset, final BogieWithRotables bogieWithRotables, final Integer slotPosition) throws Exception {
        if (canSlotAcceptWheelset(wheelset, bogieWithRotables, slotPosition)) {
            wheelset.setLocation(bogieWithRotables.getBogie().getSlot(slotPosition));
            bogieWithRotables.getWheelsets().add(wheelset);
        } else {
            throw new Exception("The slot can not accept this wheelset");
        }
    }

    /**
     * Defits wheelset from the bogie slot to workshop: the workshop is assigned as a new location of the wheelset; the wheelset is removed from the bogie rotables list.
     * 
     * @param wheelset
     * @param bogieWithRotables
     * @param workshop
     */
    static public void defitWheelset(final Wheelset wheelset, final BogieWithRotables bogieWithRotables, final Workshop workshop) {
        wheelset.setLocation(workshop);
        bogieWithRotables.getWheelsets().remove(wheelset);
    }

    /**
     * Checks whether given bogie slot can accept given wheelset. Ensures that slot is empty and wheelset is class compatible with the given bogie or slot just holds this very
     * wheelset.
     * 
     * @param wheelset
     * @param bogieWithRotables
     * @param slotPosition
     *            - position of slot; numeration starts from 1.
     * @return
     * @throws Exception
     */
    static public Boolean canSlotAcceptWheelset(final Wheelset wheelset, final BogieWithRotables bogieWithRotables, final Integer slotPosition) throws Exception {
        if ((bogieWithRotables.getWheelsetInSlotPosition(slotPosition) == null && bogieWithRotables.getBogie().isClassCompatible(wheelset))
                || (bogieWithRotables.getWheelsetInSlotPosition(slotPosition) != null && bogieWithRotables.getWheelsetInSlotPosition(slotPosition).equals(wheelset))) {
            System.out.println("slot " + slotPosition + " of bogie " + bogieWithRotables.getBogie().getKey() + " can accept wheelset " + wheelset.getKey());
            return true;
        } else {
            System.out.println("slot " + slotPosition + " of bogie " + bogieWithRotables.getBogie().getKey() + " can NOT accept wheelset " + wheelset.getKey());
            return false;
        }
    }

    /**
     * returns the list of rotables that didn't changed it's location and current location is workshop, also that list doesn't contain bogies those have wheelsets attached to them.
     * 
     * @param rotableWorkspace
     * @return
     */
    @SuppressWarnings("unchecked")
    static public List<Rotable> getWorkshopRotablesOnWorkspace(final RotableWorkspace rotableWorkspace) {
        final List<Rotable> inWorkshop = new ArrayList<Rotable>();
        final Hashtable<Bogie, Boolean> forbidenBogies = new Hashtable<Bogie, Boolean>();

        final Iterator<Bogie> bogieIterator = rotableWorkspace.getBogies().iterator();
        final Iterator<Wheelset> wheelsetIterator = rotableWorkspace.getWheelsets().iterator();

        while (wheelsetIterator.hasNext()) {
            final Wheelset wheelset = wheelsetIterator.next();
            if ((!wheelset.getProperty("location").isChangedFromOriginal()) && (rotableWorkspace.getWorkshop().equals(wheelset.getLocation()))) {
                inWorkshop.add(wheelset);
            } else {
                if (wheelset.getLocation() instanceof BogieSlot) {
                    final Bogie bogie = ((BogieSlot) (wheelset.getLocation())).getBogie();
                    forbidenBogies.put(bogie, Boolean.TRUE);
                }
            }
        }

        while (bogieIterator.hasNext()) {
            final Bogie bogie = bogieIterator.next();
            if ((!bogie.getProperty("location").isChangedFromOriginal()) && (rotableWorkspace.getWorkshop().equals(bogie.getLocation())) && (forbidenBogies.get(bogie) == null)) {
                inWorkshop.add(bogie);
            }
        }

        return inWorkshop;
    }

    @SuppressWarnings("unchecked")
    /**
     * @param rotableWorkspace
     * @return
     */
    static public Set<Rotable> getUnchangedStatusRotables(final RotableWorkspace rotableWorkspace) {
        final Hashtable<Rotable, Boolean> includedRotables = new Hashtable<Rotable, Boolean>();

        final Iterator<Bogie> bogieIterator = rotableWorkspace.getBogies().iterator();
        final Iterator<Wheelset> wheelsetIterator = rotableWorkspace.getWheelsets().iterator();

        while (bogieIterator.hasNext()) {
            final Bogie bogie = bogieIterator.next();

            if ((rotableWorkspace.getWorkshop().equals(bogie.getLocation())) && (bogie.getProperty("location").isChangedFromOriginal())
                    && (!bogie.getProperty("status").isChangedFromOriginal())) {
                includedRotables.put(bogie, Boolean.TRUE);
            }
        }

        while (wheelsetIterator.hasNext()) {
            final Wheelset wheelset = wheelsetIterator.next();
            if (rotableWorkspace.getWorkshop().equals(wheelset.getLocation())) {
                if ((wheelset.getProperty("location").isChangedFromOriginal()) && (!wheelset.getProperty("status").isChangedFromOriginal())) {
                    includedRotables.put(wheelset, Boolean.TRUE);
                }
            } else {
                final Bogie bogie = ((BogieSlot) wheelset.getLocation()).getBogie();
                if ((bogie.getProperty("location").isChangedFromOriginal()) && (!wheelset.getProperty("status").isChangedFromOriginal())) {
                    includedRotables.put(wheelset, Boolean.TRUE);
                }
            }
        }

        return includedRotables.keySet();
    }
}
