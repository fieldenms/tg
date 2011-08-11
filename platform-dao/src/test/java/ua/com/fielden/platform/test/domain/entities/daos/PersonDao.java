/**
 *
 */
package ua.com.fielden.platform.test.domain.entities.daos;

import java.util.List;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.equery.fetch;
import ua.com.fielden.platform.equery.interfaces.IFilter;
import ua.com.fielden.platform.equery.interfaces.IQueryModel;
import ua.com.fielden.platform.equery.interfaces.IQueryOrderedModel;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.test.domain.entities.Person;

import com.google.inject.Inject;

import static ua.com.fielden.platform.equery.equery.select;

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

    @SuppressWarnings("unchecked")
    private final IQueryOrderedModel<Person> model = select(Person.class).orderBy("key").model();

    @Override
    @SessionRequired
    public List<Person> retrieveAllPersonsWithRoles() {
	return getEntities(model, new fetch(Person.class).with("roles", new fetch(UserAndRoleAssociation.class).with("userRole").with("user", new fetch(Person.class))));
    }

    @Override
    public Person findPersonById(final Long id) {
	return findById(id);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Person findPersonByIdWithRoles(final Long id) {
	return findById(id, new fetch(Person.class).with("roles", new fetch(UserAndRoleAssociation.class).with("userRole").with("user", new fetch(Person.class))));
    }

    @Override
    public List<Person> retrieveAllPersons() {
	return firstPage(Integer.MAX_VALUE).data();
    }

    @Override
    public Person findPersonByKey(final String key) {
	return findByKey(key);
    }

    @Override
    public Person findPersonByKeyWithUserRoles(final String key) {
	@SuppressWarnings("unchecked")
	final IQueryModel<Person> model = select(Person.class).where().prop("key").eq().val(key).model();
	return getEntity(model, new fetch(Person.class).with("roles", new fetch(UserAndRoleAssociation.class).with("userRole").with("user", new fetch(Person.class))));
    }

}
