package ua.com.fielden.platform.entity.fetch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.AbstractEntity.VERSION;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchKeyAndDescOnly;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchNone;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchNoneAndInstrument;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchOnly;
import static ua.com.fielden.platform.utils.EntityUtils.fetchWithKeyAndDesc;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Test;

import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.sample.domain.TgAuthor;
import ua.com.fielden.platform.sample.domain.TgAuthorRoyalty;
import ua.com.fielden.platform.sample.domain.TgAuthorship;
import ua.com.fielden.platform.sample.domain.TgAverageFuelUsage;
import ua.com.fielden.platform.sample.domain.TgPersistentCompositeEntity;
import ua.com.fielden.platform.sample.domain.TgPersistentEntityWithProperties;
import ua.com.fielden.platform.sample.domain.TgPersonName;
import ua.com.fielden.platform.sample.domain.TgReVehicleModel;
import ua.com.fielden.platform.utils.EntityUtils;

public class FetchProviderTest {

    private static Set<String> set(final String... props) {
        final Set<String> set = new LinkedHashSet<>();
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
        assertEquals("Incorrect fetch model has been generated.", fetchOnly(TgPersistentEntityWithProperties.class), fp.fetchModel());
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
        assertEquals("Incorrect fetch model has been generated.", fetchKeyAndDescOnly(TgPersistentEntityWithProperties.class), fp.fetchModel());
    }
    
    
    @Test
    public void fetch_provider_generates_instrumented_fetch_model() {
        final IFetchProvider<TgPersistentEntityWithProperties> fp =
                EntityUtils.fetch(TgPersistentEntityWithProperties.class);

        assertFalse("Should not be intrumented.", fp.instrumented());
    }

    @Test
    public void fetch_provider_withKeyAndDesc_generates_instrumented_fetch_model() {
        final IFetchProvider<TgPersistentEntityWithProperties> fp =
                EntityUtils.fetchWithKeyAndDesc(TgPersistentEntityWithProperties.class);

        assertFalse("Should not be instrumented.", fp.instrumented());
    }
    
    @Test
    public void fetch_provider_with_entity_typed_property_generates_uninstrumented_fetch_submodel() {
        final IFetchProvider<TgPersistentEntityWithProperties> fp =
                EntityUtils.fetchWithKeyAndDesc(TgPersistentEntityWithProperties.class)
                .with("entityProp");

        assertFalse("Only the root entity should be intrumented.", fp.fetchFor("entityProp").instrumented());
        assertEquals("Incorrect property provider.", EntityUtils.fetchWithKeyAndDesc(TgPersistentEntityWithProperties.class, false), fp.fetchFor("entityProp"));
        assertEquals("Incorrect fetch model has been generated.",
                fetchKeyAndDescOnly(TgPersistentEntityWithProperties.class),
                fp.fetchFor("entityProp").fetchModel());
    }
    
    @Test
    public void fetch_provider_with_propertyDescriptor_typed_property_generates_fetch_model_with_regular_property() {
        final IFetchProvider<TgPersistentEntityWithProperties> fp =
            fetchWithKeyAndDesc(TgPersistentEntityWithProperties.class)
            .with("propertyDescriptorProp");
        
        assertFalse("Only the root entity should be intrumented.", fp.fetchFor("propertyDescriptorProp").instrumented());
        assertEquals("Incorrect property provider.", EntityUtils.fetchWithKeyAndDesc(PropertyDescriptor.class, false), fp.fetchFor("propertyDescriptorProp"));
        assertEquals("Incorrect fetch model has been generated.",
            fetchKeyAndDescOnly(TgPersistentEntityWithProperties.class).with("propertyDescriptorProp"),
            fp.fetchModel());
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
                fetchOnly(TgPersistentEntityWithProperties.class).with("integerProp"),
                fp.fetchModel());
    }

    @Test
    public void fetch_provider_with_entity_typed_property_generates_fetch_model_with_keyAndDesc_submodel() {
        final IFetchProvider<TgPersistentEntityWithProperties> fp =
                EntityUtils.fetch(TgPersistentEntityWithProperties.class).with("entityProp");

        assertTrue("Incorrect shouldFetch for property.", fp.shouldFetch("entityProp"));

        assertEquals("Incorrect property provider.", EntityUtils.fetchWithKeyAndDesc(TgPersistentEntityWithProperties.class, false), fp.fetchFor("entityProp"));

        assertEquals("Incorrect allProperties list.", set("entityProp"), fp.allProperties());
        assertEquals("Incorrect fetch model has been generated.",
                fetchOnly(TgPersistentEntityWithProperties.class).
                with("entityProp", fetchKeyAndDescOnly(TgPersistentEntityWithProperties.class)),
                fp.fetchModel());
    }

    @Test
    public void fetch_provider_with_composite_entity_typed_property_generates_fetch_model_with_keyAndDesc_submodel() {
        final IFetchProvider<TgPersistentEntityWithProperties> fp =
                EntityUtils.fetch(TgPersistentEntityWithProperties.class).with("compositeProp");

        assertTrue("Incorrect shouldFetch for property.", fp.shouldFetch("compositeProp"));

        assertEquals("Incorrect property provider.", EntityUtils.fetchWithKeyAndDesc(TgPersistentCompositeEntity.class, false), fp.fetchFor("compositeProp"));

        assertEquals("Incorrect allProperties list.", set("compositeProp"), fp.allProperties());
        assertEquals("Incorrect fetch model has been generated.",
                fetchOnly(TgPersistentEntityWithProperties.class).
                with("compositeProp", fetchKeyAndDescOnly(TgPersistentCompositeEntity.class)),
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
                fetchOnly(TgPersistentEntityWithProperties.class).
                        with("compositeProp",
                                fetchOnly(TgPersistentCompositeEntity.class).
                                        with("key1", fetchOnly(TgPersistentEntityWithProperties.class))),
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
                fetchOnly(TgPersistentEntityWithProperties.class).
                        with("compositeProp",
                                fetchKeyAndDescOnly(TgPersistentCompositeEntity.class).
                                        with("key1", fetchOnly(TgPersistentEntityWithProperties.class)).
                                        with("key2"))
                ,
                fp.fetchModel());
    }
    
    @Test
    public void fetchNone_provider_generates_uninstrumented_fetchNone_EQL_model() {
        final IFetchProvider<TgPersistentEntityWithProperties> fp = EntityUtils.fetchNone(TgPersistentEntityWithProperties.class);
        
        assertEquals(set(), fp.allProperties());
        assertEquals(fetchNone(TgPersistentEntityWithProperties.class), fp.fetchModel());
    }
    
    @Test
    public void fetchNone_provider_with_instrumentation_generates_instrumented_fetchNone_EQL_model() {
        final IFetchProvider<TgPersistentEntityWithProperties> fp = EntityUtils.fetchNone(TgPersistentEntityWithProperties.class, true);
        
        assertEquals(set(), fp.allProperties());
        assertEquals(fetchNoneAndInstrument(TgPersistentEntityWithProperties.class), fp.fetchModel());
    }
    
    @Test
    public void fetchNone_provider_generates_fetchNone_EQL_model_with_regular_property() {
        final IFetchProvider<TgPersistentEntityWithProperties> fp = EntityUtils.fetchNone(TgPersistentEntityWithProperties.class).with("integerProp");
        
        assertEquals(set("integerProp"), fp.allProperties());
        assertEquals(fetchNone(TgPersistentEntityWithProperties.class).with("integerProp"), fp.fetchModel());
    }
    
    @Test
    public void fetchNone_provider_generates_fetchNone_EQL_model_with_concrete_subprovider() {
        final IFetchProvider<TgPersistentEntityWithProperties> fp = 
            EntityUtils.fetchNone(TgPersistentEntityWithProperties.class)
                .with("entityProp", EntityUtils.fetchWithKeyAndDesc(TgPersistentEntityWithProperties.class));
        
        assertEquals(set("entityProp"), fp.allProperties());
        assertEquals(fetchNone(TgPersistentEntityWithProperties.class).with("entityProp", fetchKeyAndDescOnly(TgPersistentEntityWithProperties.class)), fp.fetchModel());
    }
    
    @Test
    public void fetchNone_provider_generates_fetchNone_EQL_model_with_default_NONE_subprovider() {
        final IFetchProvider<TgPersistentEntityWithProperties> fp = EntityUtils.fetchNone(TgPersistentEntityWithProperties.class).with("entityProp");
        
        assertEquals(set("entityProp"), fp.allProperties());
        assertEquals(fetchNone(TgPersistentEntityWithProperties.class).with("entityProp", fetchNone(TgPersistentEntityWithProperties.class)), fp.fetchModel());
    }
    
    @Test
    public void fetchNone_provider_generates_fetchNone_EQL_model_with_default_NONE_subproviders_for_dotNotated_property_and_concrete_subprovider() {
        final IFetchProvider<TgPersistentEntityWithProperties> fp = EntityUtils.fetchNone(TgPersistentEntityWithProperties.class).with("entityProp.entityProp.entityProp", EntityUtils.fetch(TgPersistentEntityWithProperties.class));
        
        assertEquals(set("entityProp", "entityProp.entityProp", "entityProp.entityProp.entityProp"), fp.allProperties());
        assertEquals(
            fetchNone(TgPersistentEntityWithProperties.class)
                .with("entityProp", fetchNone(TgPersistentEntityWithProperties.class)
                        .with("entityProp", fetchNone(TgPersistentEntityWithProperties.class)
                                .with("entityProp", fetchOnly(TgPersistentEntityWithProperties.class)))),
            fp.fetchModel()
        );
    }
    
    @Test
    public void fetchNone_provider_generates_fetchNone_EQL_model_with_default_NONE_subproviders_for_dotNotated_property() {
        final IFetchProvider<TgPersistentEntityWithProperties> fp = EntityUtils.fetchNone(TgPersistentEntityWithProperties.class).with("entityProp.entityProp.entityProp");
        
        assertEquals(set("entityProp", "entityProp.entityProp", "entityProp.entityProp.entityProp"), fp.allProperties());
        assertEquals(
            fetchNone(TgPersistentEntityWithProperties.class)
                .with("entityProp", fetchNone(TgPersistentEntityWithProperties.class)
                        .with("entityProp", fetchNone(TgPersistentEntityWithProperties.class)
                                .with("entityProp", fetchNone(TgPersistentEntityWithProperties.class)))),
            fp.fetchModel()
        );
    }
    
    /////////////////////////////////////////////////////////// addKeysTo method: ///////////////////////////////////////////////////////////
    
    // Adding keys to root of the tree:
    @Test(expected = FetchProviderException.class)
    public void keys_cannot_be_added_into_provider_with_category_other_than_NONE() {
        final IFetchProvider<TgPersistentEntityWithProperties> fp = EntityUtils.fetch(TgPersistentEntityWithProperties.class);
        fp.addKeysTo("");
    }
    
    @Test
    public void single_regular_key_is_added_into_provider_for_entity_with_such_key() {
        final IFetchProvider<TgPersistentEntityWithProperties> fp = EntityUtils.fetchNone(TgPersistentEntityWithProperties.class);
        fp.addKeysTo("");
        
        assertEquals(set(KEY, ID, VERSION), fp.allProperties()); // persistent entity type also requires ID and VERSION
    }
    
    @Test
    public void single_regular_key_is_added_into_provider_for_synthetic_based_on_persistent_entity_with_such_key() {
        final IFetchProvider<TgReVehicleModel> fp = EntityUtils.fetchNone(TgReVehicleModel.class);
        fp.addKeysTo("");
        
        assertEquals(set(KEY, ID), fp.allProperties()); // synthetic entity type based on persistent type requires ID
    }
    
    @Test
    public void entity_typed_key_with_own_keys_are_added_into_provider_for_synthetic_entity_with_such_key() {
        final IFetchProvider<TgAverageFuelUsage> fp = EntityUtils.fetchNone(TgAverageFuelUsage.class);
        fp.addKeysTo("");
        
        assertEquals(set(KEY, // synthetic entity type does not require neither ID nor VERSION
            KEY + "." + KEY, KEY + "." + ID, KEY + "." + VERSION // subkey and ID / VERSION for key of persistent TgVehicle type 
        ), fp.allProperties());
    }
    
    @Test
    public void composite_keys_with_own_keys_are_added_into_provider_for_composite_entity_with_such_keys() {
        final IFetchProvider<TgAuthor> fp = EntityUtils.fetchNone(TgAuthor.class);
        fp.addKeysTo("");
        
        assertEquals(set("name", "name." + KEY, "name." + ID, "name." + VERSION, // subkey and ID / VERSION for 'name' of persistent TgPersonName type
                "surname", "patronymic",
                ID, VERSION
        ), fp.allProperties());
    }
    
    // Adding keys to first-level property:
    @Test(expected = FetchProviderException.class)
    public void key_cannot_be_added_into_property_subprovider_if_does_not_exist() {
        final IFetchProvider<TgAuthor> fp = EntityUtils.fetchNone(TgAuthor.class);
        fp.addKeysTo("name");
    }
    
    @Test(expected = FetchProviderException.class)
    public void key_cannot_be_added_into_property_subprovider_if_it_has_category_other_than_NONE() {
        final IFetchProvider<TgAuthor> fp = EntityUtils.fetchNone(TgAuthor.class).with("name", EntityUtils.fetch(TgPersonName.class));
        fp.addKeysTo("name");
    }
    
    @Test
    public void key_is_added_into_property_subprovider() {
        final IFetchProvider<TgAuthor> fp = EntityUtils.fetchNone(TgAuthor.class).with("name");
        fp.addKeysTo("name");
        
        assertEquals(set("name", "name." + KEY, "name." + ID, "name." + VERSION // subkey and ID / VERSION for 'name' property of persistent TgPersonName type
        ), fp.allProperties());
    }
    
    // Adding keys to deep-level property:
    @Test(expected = FetchProviderException.class)
    public void key_cannot_be_added_into_deep_property_subprovider_if_does_not_exist() {
        final IFetchProvider<TgAuthorRoyalty> fp = EntityUtils.fetchNone(TgAuthorRoyalty.class);
        fp.addKeysTo("name");
    }
    
    @Test(expected = FetchProviderException.class)
    public void key_cannot_be_added_into_deep_property_subprovider_if_it_has_category_other_than_NONE() {
        final IFetchProvider<TgAuthorRoyalty> fp = EntityUtils.fetchNone(TgAuthorRoyalty.class).with("authorship.author.name", EntityUtils.fetch(TgPersonName.class));
        fp.addKeysTo("authorship.author.name");
    }
    
    @Test
    public void key_is_added_into_deep_property_subprovider() {
        final IFetchProvider<TgAuthorRoyalty> fp = EntityUtils.fetchNone(TgAuthorRoyalty.class).with("authorship.author.name");
        fp.addKeysTo("authorship.author.name");
        
        assertEquals(set("authorship", "authorship.author", "authorship.author.name", "authorship.author.name." + KEY, "authorship.author.name." + ID, "authorship.author.name." + VERSION // subkey and ID / VERSION for 'name' property of persistent TgPersonName type
        ), fp.allProperties());
    }
    
    @Test
    public void neighbour_branches_do_not_interfere_when_adding_keys() {
        final IFetchProvider<TgAuthorRoyalty> fp = EntityUtils.fetchNone(TgAuthorRoyalty.class).with("authorship.author.name");
        fp.addKeysTo("authorship.author");
        fp.addKeysTo("authorship.author.name");
        
        assertEquals(set("authorship", "authorship.author",
                "authorship.author", /*name is a key of author */ "authorship.author.surname", "authorship.author.patronymic",  "authorship.author." + ID, "authorship.author." + VERSION,
                "authorship.author.name", "authorship.author.name." + KEY, "authorship.author.name." + ID, "authorship.author.name." + VERSION // subkey and ID / VERSION for 'name' property of persistent TgPersonName type
        ), fp.allProperties());
        assertEquals(
            fetchNone(TgAuthorRoyalty.class)
                .with("authorship", fetchNone(TgAuthorship.class)
                        .with("author", fetchNone(TgAuthor.class)
                                .with("surname")
                                .with("patronymic")
                                .with(ID)
                                .with(VERSION)
                                .with("name", fetchNone(TgPersonName.class)
                                        .with(KEY)
                                        .with(ID)
                                        .with(VERSION)))),
            fp.fetchModel()
        );
    }
    
}