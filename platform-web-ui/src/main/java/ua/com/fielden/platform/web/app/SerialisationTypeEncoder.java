package ua.com.fielden.platform.web.app;

import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.IServerGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.ClassesRetriever;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.reflection.asm.impl.DynamicTypeNamingService;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.serialisation.api.ISerialisationTypeEncoder;
import ua.com.fielden.platform.serialisation.api.ISerialiserEngine;
import ua.com.fielden.platform.serialisation.api.impl.TgJackson;
import ua.com.fielden.platform.serialisation.jackson.EntityTypeInfoGetter;
import ua.com.fielden.platform.swing.menu.MiType;
import ua.com.fielden.platform.swing.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.web.centre.CentreUpdater;

public class SerialisationTypeEncoder implements ISerialisationTypeEncoder {
    private final Logger logger = Logger.getLogger(getClass());
    private TgJackson tgJackson;
    private EntityTypeInfoGetter entityTypeInfoGetter;
    private final IUserProvider userProvider;
    private final IServerGlobalDomainTreeManager serverGdtm;
    
    @Inject
    public SerialisationTypeEncoder(final IUserProvider userProvider, final IServerGlobalDomainTreeManager serverGdtm) {
        this.userProvider = userProvider;
        this.serverGdtm = serverGdtm;
    }
    
    @Override
    public <T extends AbstractEntity<?>> String encode(final Class<T> entityType) {
        // need to register the type into EntityTypeInfoGetter in caser where it wasn't registered
        final Class<? extends MiWithConfigurationSupport<?>> miType;
        final boolean isGenerated = DynamicEntityClassLoader.isGenerated(entityType);
        if (isGenerated) {
            miType = entityType.getAnnotation(MiType.class).value();
            logger.debug("============encode============== " + miType);
        } else {
            miType = null;
        }
        
        final String entityTypeName = entityType.getName();
        if (entityTypeInfoGetter.get(entityTypeName) == null) {
            throw new IllegalStateException("The type [" + entityTypeName + "] should be already registered at this stage.");
        }
        
        return isGenerated ? entityType.getName() + ":" + miType.getName() + ":%main%": entityType.getName();
    }

    @Override
    public <T extends AbstractEntity<?>> Class<T> decode(final String entityTypeId) {
        final boolean isGenerated = entityTypeId.contains(":");
        final String entityTypeName;
        Class<T> decodedEntityType = null;
        
        if (isGenerated) {
            logger.debug("-------------decode------------- " + entityTypeId);
            entityTypeName = entityTypeId.substring(0, entityTypeId.indexOf(":"));
            
            try {
                decodedEntityType = (Class<T>) ClassesRetriever.findClass(entityTypeName);
            } catch (final IllegalArgumentException doesNotExistException) {
                final String[] parts = entityTypeId.split(":");
                if (parts.length != 3) {
                    throw new SerialisationTypeEncoderException(String.format("Generated type has unknown format for its identifier %s.", entityTypeId));
                }
                final String miTypeName = parts[1];
                final String saveAsName = parts[2];
                final Class<? extends MiWithConfigurationSupport<?>> miType = (Class<? extends MiWithConfigurationSupport<?>>) ClassesRetriever.findClass(miTypeName);
                
                final User user = userProvider.getUser();
                if (user == null) { // the user is unknown at this stage!
                    throw new SerialisationTypeEncoderException(String.format("User is somehow unknown during decoding of entity type inside deserialisation process."));
                }
                final String userName = user.getKey();
                final IGlobalDomainTreeManager userSpecificGdtm = serverGdtm.get(userName);
                
                final String[] originalAndSuffix = entityTypeName.split(Pattern.quote(DynamicTypeNamingService.APPENDIX + "_"));
                
                final ICentreDomainTreeManagerAndEnhancer previouslyRunCentre = CentreUpdater.updateCentre(userSpecificGdtm, miType, CentreUpdater.PREVIOUSLY_RUN_CENTRE_NAME);
                decodedEntityType = (Class<T>) previouslyRunCentre.getEnhancer().adjustManagedTypeName(ClassesRetriever.findClass(originalAndSuffix[0]), originalAndSuffix[1]);
                
                if (entityTypeInfoGetter.get(decodedEntityType.getName()) != null) {
                    throw new SerialisationTypeEncoderException(String.format("Somehow decoded entity type %s was already registered in TgJackson.", decodedEntityType.getName()));
                }
                tgJackson.registerNewEntityType((Class<AbstractEntity<?>>) decodedEntityType);
            }
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
