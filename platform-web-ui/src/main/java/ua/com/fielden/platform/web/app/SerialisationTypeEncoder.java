package ua.com.fielden.platform.web.app;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static ua.com.fielden.platform.reflection.ClassesRetriever.findClass;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.stripIfNeeded;
import static ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader.isGenerated;
import static ua.com.fielden.platform.web.centre.CentreUpdater.PREVIOUSLY_RUN_CENTRE_NAME;
import static ua.com.fielden.platform.web.centre.CentreUpdater.updateCentre;
import static ua.com.fielden.platform.web.utils.EntityResourceUtils.getEntityType;

import java.lang.annotation.Annotation;
import java.util.Optional;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancerCache;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity_centre.review.criteria.EntityQueryCriteria;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.reflection.asm.impl.DynamicTypeNamingService;
import ua.com.fielden.platform.reflection.exceptions.ReflectionException;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.serialisation.api.ISerialisationTypeEncoder;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.api.ISerialiserEngine;
import ua.com.fielden.platform.serialisation.api.impl.TgJackson;
import ua.com.fielden.platform.serialisation.jackson.EntityTypeInfoGetter;
import ua.com.fielden.platform.ui.config.api.IEntityCentreConfig;
import ua.com.fielden.platform.ui.config.api.IMainMenuItem;
import ua.com.fielden.platform.ui.menu.MiType;
import ua.com.fielden.platform.ui.menu.MiTypeAnnotation;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.ui.menu.SaveAsName;
import ua.com.fielden.platform.ui.menu.SaveAsNameAnnotation;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;

public class SerialisationTypeEncoder implements ISerialisationTypeEncoder {
    private final Logger logger = Logger.getLogger(getClass());
    private TgJackson tgJackson;
    private EntityTypeInfoGetter entityTypeInfoGetter;
    private final IDeviceProvider deviceProvider;
    private final ICriteriaGenerator criteriaGenerator;
    private final IUserProvider userProvider;
    private final ISerialiser serialiser;
    private final IDomainTreeEnhancerCache domainTreeEnhancerCache;
    private final IWebUiConfig webUiConfig;
    private final IEntityCentreConfig eccCompanion;
    private final IMainMenuItem mmiCompanion;
    private final IUser userCompanion;
    
    @Inject
    public SerialisationTypeEncoder(
            final IDeviceProvider deviceProvider,
            final ICriteriaGenerator criteriaGenerator,
            final IUserProvider userProvider,
            final ISerialiser serialiser,
            final IDomainTreeEnhancerCache domainTreeEnhancerCache,
            final IWebUiConfig webUiConfig,
            final IEntityCentreConfig eccCompanion,
            final IMainMenuItem mmiCompanion,
            final IUser userCompanion) {
        this.deviceProvider = deviceProvider;
        this.criteriaGenerator = criteriaGenerator;
        this.userProvider = userProvider;
        this.serialiser = serialiser;
        this.domainTreeEnhancerCache = domainTreeEnhancerCache;
        this.webUiConfig = webUiConfig;
        this.eccCompanion = eccCompanion;
        this.mmiCompanion = mmiCompanion;
        this.userCompanion = userCompanion;
    }
    
    @Override
    public <T extends AbstractEntity<?>> String encode(final Class<T> entityType) {
        final String entityTypeName = entityType.getName();
        if (isGenerated(entityType)) { // here we have both simple entity types AND criteria entity types
            final MiType miTypeAnnotation = entityType.getAnnotation(MiType.class);
            final Class<? extends MiWithConfigurationSupport<?>> miType = miTypeAnnotation.value();
            final SaveAsName saveAsNameAnnotation = entityType.getAnnotation(SaveAsName.class);
            final String saveAsName = saveAsNameAnnotation == null ? "" : saveAsNameAnnotation.value();
            logger.debug(format("============encode============== miType = [%s], saveAsName = [%s]", miType, saveAsName));
            return entityTypeName + ":" + miType.getName() + ":" + saveAsName;
        } else {
            return entityTypeName;
        }
    }
    
    @Override
    public <T extends AbstractEntity<?>> Class<T> decode(final String entityTypeId) {
        final boolean isGenerated = entityTypeId.contains(":");
        final String entityTypeName;
        Class<T> decodedEntityType = null;
        
        if (isGenerated) {
            logger.debug(format("-------------decode------------- entityTypeId = [%s]", entityTypeId));
            entityTypeName = entityTypeId.substring(0, entityTypeId.indexOf(":"));
            
            try {
                decodedEntityType = (Class<T>) findClass(entityTypeName);
            } catch (final ReflectionException doesNotExistException) {
                final String[] parts = entityTypeId.split(":");
                if (parts.length < 2 || parts.length > 3) {
                    throw new SerialisationTypeEncoderException(format("Generated type has unknown format for its identifier %s.", entityTypeId));
                }
                final String miTypeName = parts[1];
                final Optional<String> saveAsName = parts.length > 2 ? of(parts[2]) : empty();
                final Class<? extends MiWithConfigurationSupport<?>> miType = (Class<? extends MiWithConfigurationSupport<?>>) findClass(miTypeName);
                
                final User user = userProvider.getUser();
                if (user == null) { // the user is unknown at this stage!
                    throw new SerialisationTypeEncoderException(format("User is somehow unknown during decoding of entity type inside deserialisation process."));
                }
                final String[] originalAndSuffix = entityTypeName.split(Pattern.quote(DynamicTypeNamingService.APPENDIX + "_"));
                
                final ICentreDomainTreeManagerAndEnhancer previouslyRunCentre = updateCentre(user, userProvider, miType, PREVIOUSLY_RUN_CENTRE_NAME, saveAsName, deviceProvider.getDeviceProfile(), serialiser, domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion);
                final Class<?> root = findClass(originalAndSuffix[0]);
                if (EntityQueryCriteria.class.isAssignableFrom(root)) {
                    // In the case where fully fledged criteria entity type does not exist on this server node, and thus could not yet be deserialised, we need to generate criteria type with exact correspondence to 'previouslyRunCentre'.
                    final MiType miTypeAnnotation = new MiTypeAnnotation().newInstance(miType);
                    final Annotation [] annotations = saveAsName.isPresent() ? new Annotation[] {miTypeAnnotation, new SaveAsNameAnnotation().newInstance(saveAsName.get())} : new Annotation[] {miTypeAnnotation};
                    final Class<?> managedType = stripIfNeeded(criteriaGenerator.generateCentreQueryCriteria(getEntityType(miType), previouslyRunCentre, miType, annotations).getClass());
                    final DynamicEntityClassLoader classLoader = DynamicEntityClassLoader.getInstance(ClassLoader.getSystemClassLoader());
                    try {
                        // The type name from client-side needs to be promoted to this server node very similarly as it is done below for centre managed types ('adjustManagedTypeName' call).
                        final String predefinedTypeName = root.getName() + DynamicTypeNamingService.APPENDIX + "_" + originalAndSuffix[1];
                        decodedEntityType = (Class<T>) classLoader.startModification(managedType.getName()).modifyTypeName(predefinedTypeName).endModification();
                    } catch (final ClassNotFoundException e) {
                        throw new SerialisationTypeEncoderException(e.getMessage());
                    }
                } else {
                    decodedEntityType = (Class<T>) previouslyRunCentre.getEnhancer().adjustManagedTypeName(root, originalAndSuffix[1]);
                }
                if (entityTypeInfoGetter.get(decodedEntityType.getName()) != null) {
                    throw new SerialisationTypeEncoderException(String.format("Somehow decoded entity type %s was already registered in TgJackson.", decodedEntityType.getName()));
                }
                tgJackson.registerNewEntityType((Class<AbstractEntity<?>>) decodedEntityType);
            }
        } else {
            entityTypeName = entityTypeId;
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