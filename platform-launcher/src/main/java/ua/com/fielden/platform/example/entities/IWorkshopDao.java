package ua.com.fielden.platform.example.entities;

import java.util.List;

import ua.com.fielden.platform.dao.IEntityDao;

public interface IWorkshopDao extends IEntityDao<Workshop> {
    /**
     * Retrieves bogies, which are contained in the given workshop
     * @param workshop
     * @return
     */
    List<Bogie> findWorkshopBogies(final Workshop workshop);

    /**
     * Retrieves wheelsets, which are contained in the given workshop
     * @param workshop
     * @return
     */
    List<Wheelset> findWorkshopWheelsets(final Workshop workshop);

    /**
     * Retrieves all rotables, which are contained in the given workshop
     * @param <T>
     * @param workshop
     * @return
     */
    List<Rotable> findWorkshopRotables(final Workshop workshop);

    /**
     * Retrieves workorders, which exist for the given workshop
     * @param workshop
     * @return
     */
    List<WorkOrder> findWorkshopWorkorders(final Workshop workshop);

    /**
     * Retrieves bogies, which are contained in the workshop with the given key
     * @param workshop
     * @return
     */
    List<Bogie> findWorkshopBogies(final String workshop);

    /**
     * Retrieves wheelsets, which are contained in the workshop with the given key
     * @param workshop
     * @return
     */
    List<Wheelset> findWorkshopWheelsets(final String workshop);

    /**
     * Retrieves all rotables, which are contained in the workshop with the given key
     * @param <T>
     * @param workshop
     * @return
     */
    List<Rotable> findWorkshopRotables(final String workshop);

    /**
     * Retrieves work orders, which exist for the workshop with the given key
     * @param workshop
     * @return
     */
    List<WorkOrder> findWorkshopWorkorders(final String workshop);

}
