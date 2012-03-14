/**
 *
 */
package ua.com.fielden.platform.test.domain.entities.daos;

import java.util.List;

import ua.com.fielden.platform.dao2.IEntityDao2;
import ua.com.fielden.platform.test.domain.entities.Person;

/**
 * Contract for DAO handling person instances.
 *
 * @author 01es
 *
 */
public interface IPersonDao2 extends IEntityDao2<Person> {

    /**
     * Returns persons with roles
     *
     * @return
     */
    List<Person> retrieveAllPersonsWithRoles();

    /**
     * Returns the persons without their roles
     *
     * @return
     */
    List<Person> retrieveAllPersons();

    /**
     * Returns the person with specified id without roles
     *
     * @param id
     * @return
     */
    Person findPersonById(Long id);

    /**
     * Returns the person with specified id. The returned person is with initialized set of roles
     *
     * @param id
     * @return
     */
    Person findPersonByIdWithRoles(Long id);

    /**
     * Returns person with specified key without user roles
     *
     * @param key
     * @return
     */
    Person findPersonByKey(String key);

    /**
     * Returns the person with specified key. The returned person also have associated user roles
     *
     * @param key
     * @return
     */
    Person findPersonByKeyWithUserRoles(String key);
}
