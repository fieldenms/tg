package ua.com.fielden.platform.entity.fetch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.*;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Test;

import ua.com.fielden.platform.sample.domain.TgPersistentCompositeEntity;
import ua.com.fielden.platform.sample.domain.TgPersistentEntityWithProperties;
import ua.com.fielden.platform.utils.EntityUtils;

public class FetchProviderTest {

    private static Set<String> set(final String... props) {
        final Set<String> set = new LinkedHashSet<String>();
        set.addAll(Arrays.asList(props));
        return set;
    }

    @Test
    public void empty_fetch_provider_generates_empty_fetch_only_model() {
        final IFetchProvider<TgPersistentEntityWithProperties> fp =
                EntityUtils.fetch(TgPersistentEntityWithProperties.class);

        assertFalse("Incorrect shouldFetch for property.", fp.shouldFetch("integerProp"));
        assertFalse("Incorrect shouldFetch for property.", fp.shouldFetch("entityProp"));

        try {
            fp.fetchFor("integerProp");
            fail("Should be not applicable for the properties of non-entity type.");
        } catch (final IllegalArgumentException e) {
        }
        try {
            fp.fetchFor("entityProp");
            fail("Should fail with exception 'should be not be fetched as defined in fetch provider'.");
        } catch (final IllegalStateException e) {
        }

        assertEquals("Incorrect allProperties list.", set(), fp.allProperties());
        assertEquals("Incorrect fetch model has been generated.", fetchOnlyAndInstrument(TgPersistentEntityWithProperties.class), fp.fetchModel());
    }

    @Test
    public void keyAndDesc_fetch_provider_generates_keyAndDescOnly_fetch_model() {
        final IFetchProvider<TgPersistentEntityWithProperties> fp = EntityUtils.fetchWithKeyAndDesc(TgPersistentEntityWithProperties.class);

        assertFalse("Incorrect shouldFetch for property.", fp.shouldFetch("integerProp"));
        assertFalse("Incorrect shouldFetch for property.", fp.shouldFetch("entityProp"));

        try {
            fp.fetchFor("integerProp");
            fail("Should be not applicable for the properties of non-entity type.");
        } catch (final IllegalArgumentException e) {
        }
        try {
            fp.fetchFor("entityProp");
            fail("Should fail with exception 'should be not be fetched as defined in fetch provider'.");
        } catch (final IllegalStateException e) {
        }

        assertEquals("Incorrect allProperties list.", set(), fp.allProperties());
        assertEquals("Incorrect fetch model has been generated.", fetchKeyAndDescOnlyAndInstrument(TgPersistentEntityWithProperties.class), fp.fetchModel());
    }
    
    
    @Test
    public void fetch_provider_generates_instrumented_fetch_model() {
        final IFetchProvider<TgPersistentEntityWithProperties> fp =
                EntityUtils.fetch(TgPersistentEntityWithProperties.class);

        assertTrue("Should be intrumented.", fp.instrumented());
    }

    @Test
    public void fetch_provider_withKeyAndDesc_generates_instrumented_fetch_model() {
        final IFetchProvider<TgPersistentEntityWithProperties> fp =
                EntityUtils.fetchWithKeyAndDesc(TgPersistentEntityWithProperties.class);

        assertTrue("Should be intrumented.", fp.instrumented());
    }
    
    @Test
    public void fetch_provider_with_entity_typed_property_generates_instrumented_fetch_submodel() {
        final IFetchProvider<TgPersistentEntityWithProperties> fp =
                EntityUtils.fetchWithKeyAndDesc(TgPersistentEntityWithProperties.class)
                .with("entityProp");

        assertTrue("Should be intrumented.", fp.fetchFor("entityProp").instrumented());
        assertEquals("Incorrect property provider.", EntityUtils.fetchWithKeyAndDesc(TgPersistentEntityWithProperties.class), fp.fetchFor("entityProp"));
        assertEquals("Incorrect fetch model has been generated.",
                fetchKeyAndDescOnlyAndInstrument(TgPersistentEntityWithProperties.class),
                fp.fetchFor("entityProp").fetchModel());
    }
    
    @Test
    public void fetch_provider_with_entity_typed_property_concrete_provider_generates_concrete_fetch_submodel_with_concrete_instrumentation() {
        final IFetchProvider<TgPersistentEntityWithProperties> fp =
                EntityUtils.fetchWithKeyAndDesc(TgPersistentEntityWithProperties.class)
                .with("entityProp", EntityUtils.fetchNotInstrumentedWithKeyAndDesc(TgPersistentEntityWithProperties.class));

        assertFalse("Should be not intrumented.", fp.fetchFor("entityProp").instrumented());
        assertEquals("Incorrect property provider.", EntityUtils.fetchNotInstrumentedWithKeyAndDesc(TgPersistentEntityWithProperties.class), fp.fetchFor("entityProp"));
        assertEquals("Incorrect fetch model has been generated.",
                fetchKeyAndDescOnly(TgPersistentEntityWithProperties.class),
                fp.fetchFor("entityProp").fetchModel());
    }

    @Test
    public void fetch_provider_with_regular_property_generates_fetch_model_with_regular_property() {
        final IFetchProvider<TgPersistentEntityWithProperties> fp =
                EntityUtils.fetch(TgPersistentEntityWithProperties.class).
                        with("integerProp");

        assertTrue("Incorrect shouldFetch for property.", fp.shouldFetch("integerProp"));

        try {
            fp.fetchFor("integerProp");
            fail("Should be not applicable for the properties of non-entity type.");
        } catch (final IllegalArgumentException e) {
        }

        assertEquals("Incorrect allProperties list.", set("integerProp"), fp.allProperties());
        assertEquals("Incorrect fetch model has been generated.",
                fetchOnlyAndInstrument(TgPersistentEntityWithProperties.class).with("integerProp"),
                fp.fetchModel());
    }

    @Test
    public void fetch_provider_with_entity_typed_property_generates_fetch_model_with_keyAndDesc_submodel() {
        final IFetchProvider<TgPersistentEntityWithProperties> fp =
                EntityUtils.fetch(TgPersistentEntityWithProperties.class).
                        with("entityProp");

        assertTrue("Incorrect shouldFetch for property.", fp.shouldFetch("entityProp"));

        assertEquals("Incorrect property provider.", EntityUtils.fetchWithKeyAndDesc(TgPersistentEntityWithProperties.class), fp.fetchFor("entityProp"));

        assertEquals("Incorrect allProperties list.", set("entityProp"), fp.allProperties());
        assertEquals("Incorrect fetch model has been generated.",
                fetchOnlyAndInstrument(TgPersistentEntityWithProperties.class).
                with("entityProp", fetchKeyAndDescOnlyAndInstrument(TgPersistentEntityWithProperties.class)),
                fp.fetchModel());
    }

    @Test
    public void fetch_provider_with_composite_entity_typed_property_generates_fetch_model_with_keyAndDesc_submodel() {
        final IFetchProvider<TgPersistentEntityWithProperties> fp =
                EntityUtils.fetch(TgPersistentEntityWithProperties.class).
                        with("compositeProp");

        assertTrue("Incorrect shouldFetch for property.", fp.shouldFetch("compositeProp"));

        assertEquals("Incorrect property provider.", EntityUtils.fetchWithKeyAndDesc(TgPersistentCompositeEntity.class), fp.fetchFor("compositeProp"));

        assertEquals("Incorrect allProperties list.", set("compositeProp"), fp.allProperties());
        assertEquals("Incorrect fetch model has been generated.",
                fetchOnlyAndInstrument(TgPersistentEntityWithProperties.class).
                with("compositeProp", fetchKeyAndDescOnlyAndInstrument(TgPersistentCompositeEntity.class)),
                fp.fetchModel());
    }

    @Test
    public void fetch_provider_with_concrete_property_provider_generates_fetch_model_with_concrete_submodel() {
        final IFetchProvider<TgPersistentEntityWithProperties> fp =
                EntityUtils.fetch(TgPersistentEntityWithProperties.class).
                        with("compositeProp",
                                EntityUtils.fetch(TgPersistentCompositeEntity.class).
                                        with("key1", EntityUtils.fetch(TgPersistentEntityWithProperties.class))
                        );

        assertTrue("Incorrect shouldFetch for property.", fp.shouldFetch("compositeProp"));
        assertTrue("Incorrect shouldFetch for property.", fp.shouldFetch("compositeProp.key1"));
        assertFalse("Incorrect shouldFetch for property.", fp.shouldFetch("compositeProp.key2"));

        assertEquals("Incorrect property provider.",
                EntityUtils.fetch(TgPersistentCompositeEntity.class)
                        .with("key1", EntityUtils.fetch(TgPersistentEntityWithProperties.class)),
                fp.fetchFor("compositeProp"));

        assertEquals("Incorrect property provider.",
                EntityUtils.fetch(TgPersistentEntityWithProperties.class),
                fp.fetchFor("compositeProp.key1"));

        assertEquals("Incorrect allProperties list.", set("compositeProp", "compositeProp.key1"), fp.allProperties());
        assertEquals("Incorrect fetch model has been generated.",
                fetchOnlyAndInstrument(TgPersistentEntityWithProperties.class).
                        with("compositeProp",
                                fetchOnlyAndInstrument(TgPersistentCompositeEntity.class).
                                        with("key1", fetchOnlyAndInstrument(TgPersistentEntityWithProperties.class))),
                fp.fetchModel());
    }

    @Test
    public void merged_fetch_providers_generates_appropriate_fetch_model() {
        final IFetchProvider<TgPersistentEntityWithProperties> fp1 =
                EntityUtils.fetch(TgPersistentEntityWithProperties.class).
                        with("compositeProp",
                                EntityUtils.fetch(TgPersistentCompositeEntity.class).
                                        with("key1", EntityUtils.fetch(TgPersistentEntityWithProperties.class))
                        );

        final IFetchProvider<TgPersistentEntityWithProperties> fp2 =
                EntityUtils.fetch(TgPersistentEntityWithProperties.class).
                        with("compositeProp.key2");
        final IFetchProvider<TgPersistentEntityWithProperties> fp = fp1.with(fp2);

        assertTrue("Incorrect shouldFetch for property.", fp.shouldFetch("compositeProp"));
        assertTrue("Incorrect shouldFetch for property.", fp.shouldFetch("compositeProp.key1"));
        assertTrue("Incorrect shouldFetch for property.", fp.shouldFetch("compositeProp.key2"));

        // during the merge 'keyAndDesc' provider should have the priority over 'empty' provider
        assertEquals("Incorrect property provider.",
                EntityUtils.fetchWithKeyAndDesc(TgPersistentCompositeEntity.class)
                        .with("key1", EntityUtils.fetch(TgPersistentEntityWithProperties.class))
                        .with("key2"),
                fp.fetchFor("compositeProp"));

        assertEquals("Incorrect property provider.",
                EntityUtils.fetch(TgPersistentEntityWithProperties.class),
                fp.fetchFor("compositeProp.key1"));

        assertEquals("Incorrect allProperties list.", set("compositeProp", "compositeProp.key1", "compositeProp.key2"), fp.allProperties());
        assertEquals("Incorrect fetch model has been generated.",
                fetchOnlyAndInstrument(TgPersistentEntityWithProperties.class).
                        with("compositeProp",
                                fetchKeyAndDescOnlyAndInstrument(TgPersistentCompositeEntity.class).
                                        with("key1", fetchOnlyAndInstrument(TgPersistentEntityWithProperties.class)).
                                        with("key2"))
                ,
                fp.fetchModel());
    }
}
