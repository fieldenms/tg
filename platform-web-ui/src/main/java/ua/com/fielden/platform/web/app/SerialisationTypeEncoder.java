package ua.com.fielden.platform.web.app;

import static java.util.regex.Pattern.quote;
import static ua.com.fielden.platform.reflection.ClassesRetriever.findClass;
import static ua.com.fielden.platform.reflection.asm.impl.DynamicTypeNamingService.APPENDIX;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity_centre.review.criteria.EntityQueryCriteria;
import ua.com.fielden.platform.reflection.exceptions.ReflectionException;
import ua.com.fielden.platform.serialisation.api.ISerialisationTypeEncoder;
import ua.com.fielden.platform.serialisation.api.ISerialiserEngine;
import ua.com.fielden.platform.serialisation.api.impl.TgJackson;
import ua.com.fielden.platform.serialisation.jackson.EntityTypeInfoGetter;

public class SerialisationTypeEncoder implements ISerialisationTypeEncoder {
    private TgJackson tgJackson;
    private EntityTypeInfoGetter entityTypeInfoGetter;
    private final IWebUiConfig webUiConfig;
    
    @Inject
    public SerialisationTypeEncoder(final IWebUiConfig webUiConfig) {
        this.webUiConfig = webUiConfig;
    }
    
    @Override
    public <T extends AbstractEntity<?>> String encode(final Class<T> entityType) {
        return entityType.getName();
    }
    
    @Override
    public <T extends AbstractEntity<?>> Class<T> decode(final String entityTypeId) {
        final boolean isGenerated = entityTypeId.contains(APPENDIX); // entityTypeId.contains(":");
        Class<T> decodedEntityType = null;
        
        final String entityTypeName = entityTypeId;
        if (isGenerated) {
            try {
                decodedEntityType = (Class<T>) findClass(entityTypeName);
            } catch (final ReflectionException doesNotExistException) {
                final String[] originalAndSuffix = entityTypeName.split(quote(APPENDIX + "_"));
                final Class<?> root = findClass(originalAndSuffix[0]);
                if (EntityQueryCriteria.class.isAssignableFrom(root)) {
                    final var sha256AndManagedType = originalAndSuffix.length > 2 ? (originalAndSuffix[1] + APPENDIX + "_" + originalAndSuffix[2]) : originalAndSuffix[1]; // -- this SHA256 may be used later to load user-defined type transformations from database
                    
                    // In the case where fully fledged criteria entity type does not exist on this server node, and thus could not yet be deserialised, we need to generate criteria type and its managedType; start with managedType first
                    decode(sha256AndManagedType.substring(64).replace("$$$", "."));
                    
                    try {
                        decodedEntityType = (Class<T>) findClass(entityTypeName);
                    } catch (final ReflectionException doesNotExistExceptionAgain) {
                        throw new SerialisationTypeEncoderException(String.format("Decoded entity type %s must be present after forced loading.", entityTypeName));
                    }
                } else {
                    // final var sha256 = originalAndSuffix[1]; -- this SHA256 may be used later to load user-defined type transformations from database
                    webUiConfig.loadCentreGeneratedTypesAndCriteriaTypes(root);
                    try {
                        decodedEntityType = (Class<T>) findClass(entityTypeName); // (Class<T>) previouslyRunCentre.getEnhancer().adjustManagedTypeName(root, originalAndSuffix[1]);
                    } catch (final ReflectionException doesNotExistExceptionAgain) {
                        throw new SerialisationTypeEncoderException(String.format("Decoded entity type %s must be present after forced loading.", entityTypeName));
                    }
                }
                if (entityTypeInfoGetter.get(decodedEntityType.getName()) != null) {
                    throw new SerialisationTypeEncoderException(String.format("Somehow decoded entity type %s was already registered in TgJackson.", decodedEntityType.getName()));
                }
                tgJackson.registerNewEntityType((Class<AbstractEntity<?>>) decodedEntityType);
            }
        } else {
            decodedEntityType = (Class<T>) findClass(entityTypeName);
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