package ua.com.fielden.platform.example.entities;

import java.util.ArrayList;
import java.util.List;

/**
 * Wagon with its bogies. Not mapped to db.
 * 
 * @author nc
 * 
 */
public class WagonWithRotables {

    private static final long serialVersionUID = 1L;

    private final List<BogieWithRotables> bogiesRotables;

    private final Wagon wagon;

    public WagonWithRotables(final Wagon wagon, final List<BogieWithRotables> bogiesRotables) {
        this.bogiesRotables = bogiesRotables;
        this.wagon = wagon;
    }

    public WagonWithRotables(final Wagon wagon) {
        this.bogiesRotables = new ArrayList<BogieWithRotables>();
        this.wagon = wagon;
    }

    public BogieWithRotables getBogieInSlot(final WagonSlot slot) {
        for (final BogieWithRotables bogie : bogiesRotables) {
            if (bogie.getBogie().getLocation().equals(slot)) {
                return bogie;
            }
        }
        return null;
    }

    public BogieWithRotables getBogieInSlotPosition(final Integer slotPosition) throws Exception {
        return getBogieInSlot(wagon.getSlot(slotPosition));
    }

    public Wagon getWagon() {
        return wagon;
    }

    public List<BogieWithRotables> getBogiesRotables() {
        return bogiesRotables;
    }
}
