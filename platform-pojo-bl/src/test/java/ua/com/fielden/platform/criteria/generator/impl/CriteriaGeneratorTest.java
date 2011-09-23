package ua.com.fielden.platform.criteria.generator.impl;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.swing.review.EntityQueryCriteria;
import ua.com.fielden.platform.treemodel.rules.criteria.impl.CriteriaDomainTreeManager;

import com.google.inject.Injector;

public class CriteriaGeneratorTest {
    private final CriteriaGeneratorTestModule module = new CriteriaGeneratorTestModule();
    private final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
    private final ICriteriaGenerator cg = injector.getInstance(ICriteriaGenerator.class);
    private final CriteriaDomainTreeManager cdtm = new CriteriaDomainTreeManager(null, new HashSet<Class<?>>(){{ add(TopLevelEntity.class); }});
    {
	cdtm.getFirstTick().check(TopLevelEntity.class, "integerProp", true);
	cdtm.getFirstTick().check(TopLevelEntity.class, "moneyProp", true);
	cdtm.getFirstTick().check(TopLevelEntity.class, "booleanProp", true);
	cdtm.getFirstTick().check(TopLevelEntity.class, "stringProp", true);
	cdtm.getFirstTick().check(TopLevelEntity.class, "", true);
	cdtm.getFirstTick().check(TopLevelEntity.class, "entityProp", true);
	cdtm.getFirstTick().check(TopLevelEntity.class, "entityProp.entityProp.dateProp", true);
	cdtm.getFirstTick().check(TopLevelEntity.class, "entityProp.entityProp.simpleEntityProp", true);
    }
    @Test
    public void test_that_criteria_generation_works_correctly(){
	final EntityQueryCriteria<TopLevelEntity, IEntityDao<TopLevelEntity>> criteriaEntity = cg.generateQueryCriteria(TopLevelEntity.class, cdtm);
	final List<Field> criteriaProperties = CriteriaReflector.getCriteriaProperties(criteriaEntity.getClass());
	assertEquals("The number of criteria properties is incorrect", 12, criteriaProperties.size());
    }
}
