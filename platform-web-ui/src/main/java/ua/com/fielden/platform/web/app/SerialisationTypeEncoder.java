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
        if (DynamicEntityClassLoader.isGenerated(entityType)) {
            miType = entityType.getAnnotation(MiType.class).value();
            System.out.println("========================== " + miType);
        } else {
            miType = null;
        }
        
        // final String entityTypeName = entityType.getName();
//        if (entityTypeInfoGetter.get(entityTypeName) == null) {
//            tgJackson.registerNewEntityType((Class<AbstractEntity<?>>) entityType);
//        }
        
        // TODO continue implementation
//        if (entityType.getKey() == null) {
//            throw new IllegalStateException("The name of the type [" + entityType + "] should be populated to be ready for serialisation.");
//        }

        
        
        return miType == null ? entityType.getName() : entityType.getName() + ":" + miType.getName();
    }

    @Override
    public <T extends AbstractEntity<?>> Class<T> decode(final String entityTypeId) {
        
        // TODO the type, that has been arrived from client, needs to be not only "registered" in classLoader, but also registered in form of EntityType inside TgJackson's EntityTypeInfoGetter!!!
        // TODO the type, that has been arrived from client, needs to be not only "registered" in classLoader, but also registered in form of EntityType inside TgJackson's EntityTypeInfoGetter!!!
        // TODO the type, that has been arrived from client, needs to be not only "registered" in classLoader, but also registered in form of EntityType inside TgJackson's EntityTypeInfoGetter!!!
        // TODO the type, that has been arrived from client, needs to be not only "registered" in classLoader, but also registered in form of EntityType inside TgJackson's EntityTypeInfoGetter!!!
        // TODO the type, that has been arrived from client, needs to be not only "registered" in classLoader, but also registered in form of EntityType inside TgJackson's EntityTypeInfoGetter!!!
//        final EntityType instanceTypeInfo = serialisationTypeEncoder.get(entityTypeId);
//        if (instanceTypeInfo == null) {
//            throw new RefreshApplicationException();
//        }
//        final Class<?> instanceType = ClassesRetriever.findClass(instanceTypeInfo.getKey());
        
        final String entityTypeName = entityTypeId.contains(":") ? entityTypeId.substring(0, entityTypeId.indexOf(":")) : entityTypeId;
        if (entityTypeId.contains(":")) {
            System.out.println("-------------------------- " + entityTypeId);
        }
        final Class<T> decodedEntityType = (Class<T>) ClassesRetriever.findClass(entityTypeName);
        if (entityTypeInfoGetter.get(entityTypeName) == null) {
            tgJackson.registerNewEntityType((Class<AbstractEntity<?>>) decodedEntityType);
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
