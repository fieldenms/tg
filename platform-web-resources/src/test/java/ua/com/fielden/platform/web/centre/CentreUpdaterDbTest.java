package ua.com.fielden.platform.web.centre;

import org.junit.Test;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.reflection.exceptions.ReflectionException;
import ua.com.fielden.platform.sample.domain.*;
import ua.com.fielden.platform.test.AbstractDomainDrivenTestCase;
import ua.com.fielden.platform.web.resources.test.AbstractWebResourceWithDaoTestCase;

import java.lang.reflect.Field;

import static ua.com.fielden.platform.web.centre.CentreUpdater.ID_PREFIX;
import static ua.com.fielden.platform.web.centre.CentreUpdater.MetaValueType.VALUE;
import static ua.com.fielden.platform.web.centre.CentreUpdaterTestMixin.*;
import static ua.com.fielden.platform.web.utils.EntityResourceUtils.*;

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
public class CentreUpdaterDbTest extends AbstractWebResourceWithDaoTestCase {
    
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
    
    @Test
    public void critOnlySingle_entity_value() {
        final TgCentreDiffSerialisationPersistentChild propertyVal = save(new_(TgCentreDiffSerialisationPersistentChild.class, "Ent1"));
        testDiffCreationAndApplication(CentreUpdaterTestMixin::create, centre -> centre.getFirstTick().setValue(ROOT, "entityPropCritSingle", propertyVal), expectedDiffWithValue("entityPropCritSingle", VALUE.name(), ID_PREFIX + Long.toString(propertyVal.getId())), companionFinder());
    }
    
    @Test
    public void critOnlySingle_entity_value_notFound() {
        final TgCentreDiffSerialisationPersistentChild propertyVal = (TgCentreDiffSerialisationPersistentChild) createMockNotFoundEntity(TgCentreDiffSerialisationPersistentChild.class, "UNKNOWN");
        testDiffCreationAndApplication(CentreUpdaterTestMixin::create, centre -> centre.getFirstTick().setValue(ROOT, "entityPropCritSingle", propertyVal), expectedDiffWithValue("entityPropCritSingle", VALUE.name(), createNotFoundMockString("UNKNOWN")), companionFinder());
    }
    
    @Test
    public void critOnlySingle_union_entity_value() {
        final TgUnion propertyVal = new_(TgUnion.class).setUnion1(save(new_(TgUnionType1.class, "Union1")));
        testDiffCreationAndApplication(CentreUpdaterTestMixin::create, centre -> centre.getFirstTick().setValue(ROOT, "unionEntityPropCritSingle", propertyVal), expectedDiffWithValue("unionEntityPropCritSingle", VALUE.name(), ID_PREFIX + Long.toString(propertyVal.getId())), companionFinder());
    }
    
    @Test
    public void critOnlySingle_union_entity_value_notFound() {
        final TgUnion propertyVal = (TgUnion) createMockNotFoundEntity(TgUnion.class, "UNKNOWN");
        testDiffCreationAndApplication(CentreUpdaterTestMixin::create, centre -> centre.getFirstTick().setValue(ROOT, "unionEntityPropCritSingle", propertyVal), expectedDiffWithValue("unionEntityPropCritSingle", VALUE.name(), createNotFoundMockString("UNKNOWN")), companionFinder());
    }
    
    @Test
    public void critOnlySingle_union_entity_value_moreThanOne() {
        final TgUnion propertyVal = (TgUnion) createMockFoundMoreThanOneEntity(TgUnion.class, "MORETHANONE");
        testDiffCreationAndApplication(CentreUpdaterTestMixin::create, centre -> centre.getFirstTick().setValue(ROOT, "unionEntityPropCritSingle", propertyVal), expectedDiffWithValue("unionEntityPropCritSingle", VALUE.name(), createMoreThanOneMockString("MORETHANONE")), companionFinder());
    }
    
    @Test
    public void critOnlySingle_non_persistent_entity_value() {
        final TgCentreDiffSerialisationNonPersistentChild propertyVal = new_(TgCentreDiffSerialisationNonPersistentChild.class, "Team");
        testDiffCreationAndApplication(CentreUpdaterTestMixin::create, centre -> centre.getFirstTick().setValue(ROOT, "nonPersistentEntityPropCritSingle", propertyVal), expectedDiffWithValue("nonPersistentEntityPropCritSingle", VALUE.name(), propertyVal.getKey().toString()), companionFinder());
    }
    
    @Test
    public void critOnlySingle_non_persistent_entity_value_notFound() {
        final TgCentreDiffSerialisationNonPersistentChild propertyVal = (TgCentreDiffSerialisationNonPersistentChild) createMockNotFoundEntity(TgCentreDiffSerialisationNonPersistentChild.class, "UNKNOWN");
        testDiffCreationAndApplication(CentreUpdaterTestMixin::create, centre -> centre.getFirstTick().setValue(ROOT, "nonPersistentEntityPropCritSingle", propertyVal), expectedDiffWithValue("nonPersistentEntityPropCritSingle", VALUE.name(), createNotFoundMockString("UNKNOWN")), companionFinder());
    }
    
    @Test
    public void critOnlySingle_non_persistent_composite_entity_value() {
        final TgCentreDiffSerialisationNonPersistentCompositeChild propertyVal = new_composite(TgCentreDiffSerialisationNonPersistentCompositeChild.class, "Team", "West");
        testDiffCreationAndApplication(CentreUpdaterTestMixin::create, centre -> centre.getFirstTick().setValue(ROOT, "nonPersistentCompositeEntityPropCritSingle", propertyVal), expectedDiffWithValue("nonPersistentCompositeEntityPropCritSingle", VALUE.name(), propertyVal.getKey().toString()), companionFinder());
    }
    
    @Test
    public void critOnlySingle_non_persistent_composite_entity_value_notFound() {
        final TgCentreDiffSerialisationNonPersistentCompositeChild propertyVal = (TgCentreDiffSerialisationNonPersistentCompositeChild) createMockNotFoundEntity(TgCentreDiffSerialisationNonPersistentCompositeChild.class, "REALLY UNKNOWN");
        testDiffCreationAndApplication(CentreUpdaterTestMixin::create, centre -> centre.getFirstTick().setValue(ROOT, "nonPersistentCompositeEntityPropCritSingle", propertyVal), expectedDiffWithValue("nonPersistentCompositeEntityPropCritSingle", VALUE.name(), createNotFoundMockString("REALLY UNKNOWN")), companionFinder());
    }
    
}
