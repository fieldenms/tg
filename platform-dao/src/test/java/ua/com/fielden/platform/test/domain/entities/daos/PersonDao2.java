/**
 *
 */
package ua.com.fielden.platform.test.domain.entities.daos;

import java.util.List;

import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.dao2.CommonEntityDao2;
import ua.com.fielden.platform.dao2.QueryExecutionModel;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.test.domain.entities.Person;

import com.google.inject.Inject;

import static ua.com.fielden.platform.entity.query.fluent.query.orderBy;
import static ua.com.fielden.platform.entity.query.fluent.query.select;

/**
 * Class for retrieval of {@link Person} instances
 *
 * @author TG Team
 */
@EntityType(Person.class)
public class PersonDao2 extends CommonEntityDao2<Person> implements IPersonDao2 {

    @Inject
    protected PersonDao2(final IFilter filter) {
	super(filter);
    }

    private final EntityResultQueryModel<Person> model = select(Person.class).model();
    private final OrderingModel orderBy = orderBy().prop("key").asc().model();

    @Override
    @SessionRequired
    public List<Person> retrieveAllPersonsWithRoles() {
	return getEntities(new QueryExecutionModel.Builder<Person>(model). //
		orderModel(orderBy). //
		fetchModel(new fetch<Person>(Person.class).with("roles", new fetch<UserAndRoleAssociation>(UserAndRoleAssociation.class).with("userRole").with("user", new fetch<Person>(Person.class)))). //
		build());
    }

    @Override
    public Person findPersonById(final Long id) {
	return findById(id);
    }

    @Override
    public Person findPersonByIdWithRoles(final Long id) {
	return findById(id, new fetch<Person>(Person.class).with("roles", new fetch<UserAndRoleAssociation>(UserAndRoleAssociation.class).with("userRole").with("user", new fetch<Person>(Person.class))));
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
	final EntityResultQueryModel<Person> model = select(Person.class).where().prop("key").eq().val(key).model();
	return getEntity(new QueryExecutionModel.Builder<Person>(model).fetchModel(new fetch<Person>(Person.class).with("roles", new fetch<UserAndRoleAssociation>(UserAndRoleAssociation.class).with("userRole").with("user", new fetch<Person>(Person.class)))).build());
    }
}