package ua.com.fielden.platform.web.centre;

import static ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager.MetaValueType.VALUE;
import static ua.com.fielden.platform.entity.factory.EntityFactory.newPlainEntity;
import static ua.com.fielden.platform.web.centre.CentreUpdaterTest.ROOT;
import static ua.com.fielden.platform.web.centre.CentreUpdaterTest.expectedDiffWithValue;
import static ua.com.fielden.platform.web.centre.CentreUpdaterTest.testDiffCreationAndApplication;

import java.lang.reflect.Field;

import org.junit.Test;

import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.reflection.exceptions.ReflectionException;
import ua.com.fielden.platform.sample.domain.TgCentreDiffSerialisationPersistentChild;
import ua.com.fielden.platform.test.AbstractDomainDrivenTestCase;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

/**
 * Unit tests for {@link CentreUpdater} API methods, particularly for
 * <p>
 * 1. {@link CentreUpdater#createDifferences(ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer, ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer, Class)}<br>
 * 2. {@link CentreUpdater#applyDifferences(ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer, java.util.Map, Class)}
 * <p>
 * This is a db-driven continuation of {@link CentreUpdaterTest} covering entity-typed crit-only single properties.
 * 
 * @author TG Team
 *
 */
public class CentreUpdaterDbTest extends AbstractDaoTestCase {
    
    private static ICompanionObjectFinder companionFinder() {
        Field field;
        try {
            field = AbstractDomainDrivenTestCase.class.getDeclaredField("coFinder");
            field.setAccessible(true);
            return (ICompanionObjectFinder) field.get(null);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            throw new ReflectionException("Companion finder can not be taken.", ex);
        }
    }
    
    private static TgCentreDiffSerialisationPersistentChild createEntityValue(final Long id) {
        return newPlainEntity(TgCentreDiffSerialisationPersistentChild.class, id);
    }
    
    @Test
    public void critOnlySingle_entity_value() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        testDiffCreationAndApplication(CentreUpdaterTest::create, centre -> centre.getFirstTick().setValue(ROOT, "entityPropCritSingle", createEntityValue(123L)), expectedDiffWithValue("entityPropCritSingle", VALUE.name(), 123L), companionFinder());
    }
    
}
