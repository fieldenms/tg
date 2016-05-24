package ua.com.fielden.platform.web.app;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.ClassesRetriever;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.serialisation.api.ISerialisationTypeEncoder;
import ua.com.fielden.platform.serialisation.api.ISerialiserEngine;
import ua.com.fielden.platform.serialisation.api.impl.TgJackson;
import ua.com.fielden.platform.serialisation.jackson.EntityTypeInfoGetter;
import ua.com.fielden.platform.swing.menu.MiType;
import ua.com.fielden.platform.swing.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.utils.RefreshApplicationException;

public class SerialisationTypeEncoder implements ISerialisationTypeEncoder {
    private TgJackson tgJackson;
    private EntityTypeInfoGetter entityTypeInfoGetter;
    private final IUserProvider userProvider;
    
    @Inject
    public SerialisationTypeEncoder(final IUserProvider userProvider) {
        this.userProvider = userProvider;
    }
    
    @Override
    public <T extends AbstractEntity<?>> String encode(final Class<T> entityType) {
        // need to register the type into EntityTypeInfoGetter in caser where it wasn't registered
        final Class<? extends MiWithConfigurationSupport<?>> miType;
        final boolean isGenerated = DynamicEntityClassLoader.isGenerated(entityType);
        if (isGenerated) {
            miType = entityType.getAnnotation(MiType.class).value();
            System.out.println("========================== " + miType);
        } else {
            miType = null;
        }
        
        final String entityTypeName = entityType.getName();
        if (entityTypeInfoGetter.get(entityTypeName) == null) {
            throw new IllegalStateException("The type [" + entityTypeName + "] should be already registered at this stage.");
            // TODO tgJackson.registerNewEntityType((Class<AbstractEntity<?>>) entityType); ?
        }
        
        return isGenerated ? entityType.getName() + ":" + miType.getName() + ":%main%": entityType.getName();
    }

    @Override
    public <T extends AbstractEntity<?>> Class<T> decode(final String entityTypeId) {
        final boolean isGenerated = entityTypeId.contains(":");
        final String entityTypeName;
        final Class<T> decodedEntityType;
        
        if (isGenerated) {
            System.out.println("-------------------------- " + entityTypeId);
            entityTypeName = entityTypeId.substring(0, entityTypeId.indexOf(":"));
            
            // TODO the type, that has been arrived from client, needs to be not only "registered" in classLoader, but also registered in form of EntityType inside TgJackson's EntityTypeInfoGetter!!!
            // TODO there is a need to check whether the type with 'entityTypeName' exists on server, and if not, corresponding centre should be loaded
            if (entityTypeInfoGetter.get(entityTypeName) == null) {
                throw new RefreshApplicationException();
                
                // TODO tgJackson.registerNewEntityType((Class<AbstractEntity<?>>) decodedEntityType);
            }
            
            decodedEntityType = (Class<T>) ClassesRetriever.findClass(entityTypeName);
        } else {
            entityTypeName = entityTypeId;
            decodedEntityType = (Class<T>) ClassesRetriever.findClass(entityTypeName);
        }
        
        return decodedEntityType;
    }

    @Override
    public ISerialisationTypeEncoder setTgJackson(final ISerialiserEngine tgJackson) {
        this.tgJackson = (TgJackson) tgJackson;
        this.entityTypeInfoGetter = this.tgJackson.getEntityTypeInfoGetter();
        return this;
    }
}
