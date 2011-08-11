package ua.com.fielden.platform.example.entities.daos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.transform.ResultTransformer;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.equery.interfaces.IFilter;
import ua.com.fielden.platform.example.entities.Bogie;
import ua.com.fielden.platform.example.entities.BogieSlot;
import ua.com.fielden.platform.example.entities.BogieWithRotables;
import ua.com.fielden.platform.example.entities.IWagonDao;
import ua.com.fielden.platform.example.entities.Rotable;
import ua.com.fielden.platform.example.entities.RotableLocation;
import ua.com.fielden.platform.example.entities.Wagon;
import ua.com.fielden.platform.example.entities.WagonSlot;
import ua.com.fielden.platform.example.entities.WagonWithRotables;
import ua.com.fielden.platform.example.entities.Wheelset;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

/**
 * DAO for retrieving wagon related data: wagon itself, wagon with its rotables.
 *
 * @author TG Team
 *
 */
@EntityType(Wagon.class)
public class WagonDao extends CommonEntityDao<Wagon> implements IWagonDao {

    @Inject
    protected WagonDao(final IFilter filter) {
	super(filter);
    }

    /**
     * Retrieves wagonWithRotables object by the given wagon
     *
     * @param wagon
     * @return
     */
    @SessionRequired
    public WagonWithRotables getWagonWithRotables(final Wagon wagon) {
	final Map<Bogie, List<Wheelset>> bogieWheelsets = new HashMap<Bogie, List<Wheelset>>();

	for (final Bogie bogie : getWagonBogies(wagon)) {
	    bogieWheelsets.put(bogie, new ArrayList<Wheelset>());
	}

	for (final Wheelset wheelset : getWagonWheelsets(wagon)) {
	    if (bogieWheelsets.containsKey(((BogieSlot) wheelset.getLocation()).getBogie())) {
		bogieWheelsets.get(((BogieSlot) wheelset.getLocation()).getBogie()).add(wheelset);
	    }
	}

	final List<BogieWithRotables> bogieWithRotables = new ArrayList<BogieWithRotables>();

	for (final Map.Entry<Bogie, List<Wheelset>> bogieWhs : bogieWheelsets.entrySet()) {
	    bogieWithRotables.add(new BogieWithRotables(bogieWhs.getKey(), bogieWhs.getValue()));
	}
	return new WagonWithRotables(wagon, bogieWithRotables);
    }

    /**
     * Retrieves bogies, currently contained in the given wagon slots
     *
     * @param wagon
     * @return
     */
    @SuppressWarnings("unchecked")
    @SessionRequired
    private List<Bogie> getWagonBogies(final Wagon wagon) {
	return getSession().createQuery("select b, ws from Bogie b, WagonSlot ws where b.location = ws and ws.wagon = :wagon").setEntity("wagon", wagon).setResultTransformer(
		new RotableInstantiator<Bogie, WagonSlot>(Bogie.class, WagonSlot.class)).list();
    }

    /**
     * Retrieves wheelsets, contained in the bogies of the given wagon
     *
     * @param wagon
     * @return
     */
    @SuppressWarnings("unchecked")
    @SessionRequired
    private List<Wheelset> getWagonWheelsets(final Wagon wagon) {
	return getSession().createQuery(
		"select whs, bs from Wheelset whs, BogieSlot bs where whs.location = bs and bs.bogie in "
			+ "(select b from Bogie b, WagonSlot ws where b.location = ws and ws.wagon = :wagon)").setEntity("wagon", wagon).setResultTransformer(
		new RotableInstantiator<Wheelset, BogieSlot>(Wheelset.class, BogieSlot.class)).list();
    }

    /**
     * Used only for data migration. Retrieves WagonSlot by wagon serialNo and slot position.
     */
    @SuppressWarnings("unchecked")
    @SessionRequired
    public WagonSlot getWagonSlotByWagonSerialNoAndPosition(final String serialNo, final Integer position) {
	final List<WagonSlot> result = getSession().createQuery("select ws from Wagon w, WagonSlot ws where w.id = ws.wagon and w.serialNo = :serialno and ws.position = :position").
	setString("serialno",serialNo).
	setInteger("position", position).
	list();
	return result.size() == 1 ? result.get(0) : null;
    }

    /**
     * Used only for data migration. Retrieves BogieSlot by wagon serialNo, bogie and wheelset slot positions.
     */
    @SuppressWarnings("unchecked")
    @SessionRequired
    public BogieSlot getBogieSlotByWagonSerialNoAndBogieAndWheelsetSlotPositions(final String serialNo, final Integer bogiePosition, final Integer wheelsetPosition) {
	final List<BogieSlot> result = getSession().createQuery("select bs from Wagon w, WagonSlot ws, Bogie b, BogieSlot bs " +
		"where w.id = ws.wagon and w.serialNo = :serialno and ws.position = :bogie_position and b.location = ws and b.id = bs.bogie and bs.position = :wset_position").
	setString("serialno",serialNo).
	setInteger("bogie_position", bogiePosition).
	setInteger("wset_position", wheelsetPosition).
	list();
	return result.size() == 1 ? result.get(0) : null;
    }

    /**
     * This class just reassigns rotable location to the retrieved rotable. Introduced to improve db performance by simplifying and reducing number of SQL queries.
     *
     * @author nc
     *
     * @param <R>
     * @param <L>
     */
    private static class RotableInstantiator<R extends Rotable, L extends RotableLocation<?>> implements ResultTransformer {
	private static final long serialVersionUID = 1L;
	private final Class<R> rotableKlass;
	private final Class<L> rotableLocationKlass;

	public RotableInstantiator(final Class<R> rotableKlass, final Class<L> rotableLocationKlass) {
	    this.rotableKlass = rotableKlass;
	    this.rotableLocationKlass = rotableLocationKlass;
	}

	@SuppressWarnings("unchecked")
	public List transformList(final List result) {
	    return result;
	}

	public Object transformTuple(final Object[] args, final String[] arg1) {
	    final R rotable = rotableKlass.cast(args[0]);
	    final L rotableLocation = rotableLocationKlass.cast(args[1]);
	    rotable.setLocation(rotableLocation);
	    return rotable;
	}
    }
}
