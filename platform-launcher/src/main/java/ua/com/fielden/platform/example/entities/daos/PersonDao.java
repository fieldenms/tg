/**
 *
 */
package ua.com.fielden.platform.example.entities.daos;

import static ua.com.fielden.platform.equery.equery.select;

import java.util.List;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.equery.fetch;
import ua.com.fielden.platform.equery.interfaces.IFilter;
import ua.com.fielden.platform.equery.interfaces.IQueryModel;
import ua.com.fielden.platform.example.entities.IPersonDao;
import ua.com.fielden.platform.example.entities.Person;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

/**
 * Class for retrieval of {@link Person} instances
 *
 * @author TG Team
 */
@EntityType(Person.class)
public class PersonDao extends CommonEntityDao<Person> implements IPersonDao {

    @Inject
    protected PersonDao(final IFilter filter) {
	super(filter);
    }

    private final IQueryModel<Person> model = select(Person.class).model();
    private final fetch<Person> fetchModel = new fetch<Person>(Person.class).with("roles", new fetch(UserAndRoleAssociation.class).with("userRole"));

    @Override
    @SessionRequired
    public List<Person> retrieveAllPersonsWithRoles() {
	return getEntities(model, fetchModel);
    }

    @Override
    @SessionRequired
    public Person findPersonById(final Long id) {
	return findById(id);
    }

    @Override
    @SessionRequired
    public Person findPersonByIdWithRoles(final Long id) {
	return findById(id, fetchModel);
    }

    @Override
    @SessionRequired
    public List<Person> retrieveAllPersons() {
	return firstPage(Integer.MAX_VALUE).data();
    }

    @Override
    @SessionRequired
    public Person findPersonByKey(final String key) {
	return findByKey(key);
    }

    @Override
    @SessionRequired
    public Person findPersonByKeyWithUserRoles(final String key) {
	final IQueryModel<Person> model = select(Person.class).where().prop("key").eq().val(key).model();
	return getEntity(model, fetchModel);
    }
}
