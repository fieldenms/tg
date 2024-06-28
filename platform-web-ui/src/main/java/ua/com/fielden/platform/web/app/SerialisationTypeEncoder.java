package ua.com.fielden.platform.web.app;

import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.reflection.ClassesRetriever.findClass;
import static ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader.getOriginalType;
import static ua.com.fielden.platform.reflection.asm.impl.DynamicTypeNamingService.APPENDIX;
import static ua.com.fielden.platform.reflection.asm.impl.DynamicTypeNamingService.decodeOriginalGeneratedTypeFromCriteriaType;
import static ua.com.fielden.platform.reflection.asm.impl.DynamicTypeNamingService.decodeOriginalTypeFrom;

import jakarta.inject.Singleton;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity_centre.review.criteria.EntityQueryCriteria;
import ua.com.fielden.platform.reflection.exceptions.ReflectionException;
import ua.com.fielden.platform.serialisation.api.ISerialisationTypeEncoder;
import ua.com.fielden.platform.serialisation.api.ISerialiserEngine;
import ua.com.fielden.platform.serialisation.api.impl.TgJackson;
import ua.com.fielden.platform.serialisation.jackson.EntityTypeInfoGetter;

@Singleton
public class SerialisationTypeEncoder implements ISerialisationTypeEncoder {
    private static final Logger LOGGER = getLogger(SerialisationTypeEncoder.class);

    // @formatter:off
    private static final String
        ERR_DECODED_ENTITY_TYPE_MUST_BE_PRESENT =
        "Decoded entity type %s must be present after forced loading.",
        ERR_CLIENT_APP_IS_OUTDATED =
        "The client application is outdated. Please reload and try again.",
        // see EntityTypeInfoGetter.register for more details
        ERR_DECODED_TYPE_WAS_ALREADY_REGISTERED =
        "Somehow decoded generated entity type %s was registered in TgJackson's type table. Only ungenerated types should be there.";
    // @formatter:on

    private TgJackson tgJackson;
    private EntityTypeInfoGetter entityTypeInfoGetter;
    private final IWebUiConfig webUiConfig;

    @Inject
    public SerialisationTypeEncoder(final IWebUiConfig webUiConfig) {
        this.webUiConfig = webUiConfig;
    }

    @Override
    public <T extends AbstractEntity<?>> Class<T> decode(final String entityTypeName) {
        final boolean isGenerated = entityTypeName.contains(APPENDIX);
        Class<T> decodedEntityType = null;
        if (isGenerated) {
            try {
                decodedEntityType = (Class<T>) findClass(entityTypeName);
            } catch (final ReflectionException doesNotExistException) {
                final Class<?> root = findClass(decodeOriginalTypeFrom(entityTypeName));
                if (EntityQueryCriteria.class.isAssignableFrom(root)) {
                    // In the case where fully fledged criteria entity type does not exist on this server node, and thus could not yet be deserialised, we need to generate criteria type and its managedType;
                    // start with managedType first - decode() invocation below
                    final var decodedManagedType = decode(decodeOriginalGeneratedTypeFromCriteriaType(entityTypeName));
                    // continue with loading of all criteria + managed types for this decodedManagedType original type (may be from different centres with distinct miTypes)
                    webUiConfig.loadCentreGeneratedTypesAndCriteriaTypes(getOriginalType(decodedManagedType));
                } else {
                    // In the case where fully fledged managed entity type does not exist on this server node, and thus could not yet be deserialised, we need to generate it managedType;
                    // load all criteria + managed types for this root original type (may be from different centres with distinct miTypes)
                    webUiConfig.loadCentreGeneratedTypesAndCriteriaTypes(root);
                }
                try {
                    decodedEntityType = (Class<T>) findClass(entityTypeName);
                } catch (final ReflectionException doesNotExistExceptionAgain) {
                    LOGGER.error(ERR_DECODED_ENTITY_TYPE_MUST_BE_PRESENT.formatted(entityTypeName));
                    throw new SerialisationTypeEncoderException(ERR_CLIENT_APP_IS_OUTDATED);
                }
                if (entityTypeInfoGetter.get(decodedEntityType.getName()) != null) {
                    LOGGER.warn(ERR_DECODED_TYPE_WAS_ALREADY_REGISTERED.formatted(decodedEntityType.getName()));
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
