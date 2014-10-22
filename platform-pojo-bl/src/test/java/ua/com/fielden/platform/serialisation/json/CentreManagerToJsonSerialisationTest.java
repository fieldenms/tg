package ua.com.fielden.platform.serialisation.json;

import static junit.framework.Assert.fail;

import java.util.HashSet;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;

import ua.com.fielden.platform.criteria.generator.impl.CriteriaGeneratorTestModule;
import ua.com.fielden.platform.criteria.generator.impl.LastLevelEntity;
import ua.com.fielden.platform.criteria.generator.impl.SecondLevelEntity;
import ua.com.fielden.platform.criteria.generator.impl.ThirdLevelEntity;
import ua.com.fielden.platform.criteria.generator.impl.TopLevelEntity;
import ua.com.fielden.platform.domaintree.centre.impl.CentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.testing.ClassProviderForTestingPurposes;
import ua.com.fielden.platform.domaintree.testing.TgKryoForDomainTreesTestingPurposes;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Injector;

public class CentreManagerToJsonSerialisationTest {

    private final CriteriaGeneratorTestModule module = new CriteriaGeneratorTestModule();
    private final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
    {
        module.setInjector(injector);
    }
    private final EntityFactory entityFactory = injector.getInstance(EntityFactory.class);
    TgKryoForDomainTreesTestingPurposes serialiser = new TgKryoForDomainTreesTestingPurposes(entityFactory, new ClassProviderForTestingPurposes(TopLevelEntity.class, LastLevelEntity.class, SecondLevelEntity.class, ThirdLevelEntity.class));

    @SuppressWarnings("serial")
    private final CentreDomainTreeManagerAndEnhancer cdtm = new CentreDomainTreeManagerAndEnhancer(serialiser, new HashSet<Class<?>>() {
        {
            add(TopLevelEntity.class);
        }
    });
    {
        //Configuring first tick check properties.
        cdtm.getFirstTick().check(TopLevelEntity.class, "critSingleEntity", true);
        cdtm.getFirstTick().check(TopLevelEntity.class, "critRangeEntity", true);
        cdtm.getFirstTick().check(TopLevelEntity.class, "critISingleProperty", true);
        cdtm.getFirstTick().check(TopLevelEntity.class, "critIRangeProperty", true);
        cdtm.getFirstTick().check(TopLevelEntity.class, "critSSingleProperty", true);
        cdtm.getFirstTick().check(TopLevelEntity.class, "critSRangeProperty", true);
        cdtm.getFirstTick().check(TopLevelEntity.class, "integerProp", true);
        cdtm.getFirstTick().check(TopLevelEntity.class, "moneyProp", true);
        cdtm.getFirstTick().check(TopLevelEntity.class, "booleanProp", true);
        cdtm.getFirstTick().check(TopLevelEntity.class, "stringProp", true);
        cdtm.getFirstTick().check(TopLevelEntity.class, "", true);
        cdtm.getFirstTick().check(TopLevelEntity.class, "entityProp", true);
        cdtm.getFirstTick().check(TopLevelEntity.class, "entityProp.entityProp.dateProp", true);
        cdtm.getFirstTick().check(TopLevelEntity.class, "entityProp.entityProp.simpleEntityProp", true);

        //Configure second tick check properties.
        cdtm.getSecondTick().check(TopLevelEntity.class, "integerProp", true);
        cdtm.getSecondTick().check(TopLevelEntity.class, "moneyProp", true);
        cdtm.getSecondTick().check(TopLevelEntity.class, "booleanProp", true);
        cdtm.getSecondTick().check(TopLevelEntity.class, "stringProp", true);
        cdtm.getSecondTick().check(TopLevelEntity.class, "", true);
        cdtm.getSecondTick().check(TopLevelEntity.class, "entityProp", true);
        cdtm.getSecondTick().check(TopLevelEntity.class, "entityProp.entityProp.dateProp", true);
        cdtm.getSecondTick().check(TopLevelEntity.class, "entityProp.entityProp.simpleEntityProp", true);
    }

    private final TgObjectMapper tgToJson = injector.getInstance(TgObjectMapper.class);

    @Test
    @Ignore
    public void test_whether_centre_to_JSON_serialisation_works() {
        try {
            final String expectedJson = "{" + "\"criteriaProperties\":[" + "\"critSingleEntity\"," + "\"critRangeEntity\"," + "\"critISingleProperty\","
                    + "\"critIRangeProperty\"," + "\"critSSingleProperty\"," + "\"critSRangeProperty\"," + "\"integerProp\"," + "\"moneyProp\"," + "\"booleanProp\","
                    + "\"stringProp\"," + "\"\"," + "\"entityProp\"," + "\"entityProp.entityProp.dateProp\"," + "\"entityProp.entityProp.simpleEntityProp\"" + "],"
                    + "\"resultProperties\":[" + "\"integerProp\"," + "\"moneyProp\"," + "\"booleanProp\"," + "\"stringProp\"," + "\"\"," + "\"entityProp\","
                    + "\"entityProp.entityProp.dateProp\"," + "\"entityProp.entityProp.simpleEntityProp\"" + "]" + "}";
            Assert.assertEquals("The serialised object isn't correct", expectedJson, tgToJson.writeValueAsString(cdtm));
        } catch (final JsonProcessingException e) {
            e.printStackTrace();
            fail("The json serialisation doesn't work!");
        }
    }
}
