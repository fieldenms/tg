package ua.com.fielden.platform.example.entities.daos;

import java.util.List;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.equery.interfaces.IFilter;
import ua.com.fielden.platform.example.entities.Bogie;
import ua.com.fielden.platform.example.entities.IWorkshopDao;
import ua.com.fielden.platform.example.entities.Rotable;
import ua.com.fielden.platform.example.entities.Wheelset;
import ua.com.fielden.platform.example.entities.WorkOrder;
import ua.com.fielden.platform.example.entities.Workshop;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

/**
 * DAO for retrieving workshop related data: workshop itself, contained rotables, existing active workorders.
 *
 * @author TG Team
 *
 */
@EntityType(Workshop.class)
public class WorkshopDao extends CommonEntityDao<Workshop> implements IWorkshopDao {

    @Inject
    protected WorkshopDao(final IFilter filter) {
	super(filter);
    }

    /**
     * Retrieves bogies, which are contained in the given workshop
     * @param workshop
     * @return
     */
    @SuppressWarnings("unchecked")
    @SessionRequired
    public List<Bogie> findWorkshopBogies(final Workshop workshop) {
	return getSession().createQuery("select b from Bogie b, Workshop ws where b.location = ws and ws = :workshop").setEntity("workshop", workshop).list();
    }

    /**
     * Retrieves wheelsets, which are contained in the given workshop
     * @param workshop
     * @return
     */
    @SuppressWarnings("unchecked")
    @SessionRequired
    public List<Wheelset> findWorkshopWheelsets(final Workshop workshop) {
	return getSession().createQuery("select whs from Wheelset whs, Workshop ws where whs.location = ws and ws = :workshop").setEntity("workshop", workshop).list();
    }

    /**
     * Retrieves all rotables, which are contained in the given workshop
     * @param <T>
     * @param workshop
     * @return
     */
    @SuppressWarnings("unchecked")
    @SessionRequired
    public List<Rotable> findWorkshopRotables(final Workshop workshop) {
	return getSession().createQuery("select r from Rotable r, Workshop ws where r.location = ws and ws = :workshop").setEntity("workshop", workshop).list();
    }

    /**
     * Retrieves workorders, which exist for the given workshop
     * @param workshop
     * @return
     */
    @SuppressWarnings("unchecked")
    @SessionRequired
    public List<WorkOrder> findWorkshopWorkorders(final Workshop workshop) {
	return getSession().createQuery("select wo from Wagon w, WorkOrder wo where wo.equipment = w and wo.workshop = :workshop order by wo.equipment").setEntity("workshop", workshop).list();
    }

    /**
     * Retrieves bogies, which are contained in the workshop with the given key
     * @param workshop
     * @return
     */
    public List<Bogie> findWorkshopBogies(final String workshop) {
	return findWorkshopBogies(findByKey(workshop));
    }

    /**
     * Retrieves wheelsets, which are contained in the workshop with the given key
     * @param workshop
     * @return
     */
    public List<Wheelset> findWorkshopWheelsets(final String workshop) {
	return findWorkshopWheelsets(findByKey(workshop));
    }

    /**
     * Retrieves all rotables, which are contained in the workshop with the given key
     * @param <T>
     * @param workshop
     * @return
     */
    public List<Rotable> findWorkshopRotables(final String workshop) {
	return findWorkshopRotables(findByKey(workshop));
    }

    /**
     * Retrieves workorders, which exist for the workshop with the given key
     * @param workshop
     * @return
     */
    public List<WorkOrder> findWorkshopWorkorders(final String workshop) {
	return findWorkshopWorkorders(findByKey(workshop));
    }
}
