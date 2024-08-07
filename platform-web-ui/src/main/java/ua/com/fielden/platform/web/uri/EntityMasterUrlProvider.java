package ua.com.fielden.platform.web.uri;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.master.MasterInfo;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.web.annotations.AppUri;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.interfaces.IEntityMasterUrlProvider;
import ua.com.fielden.platform.web.view.master.MasterInfoProvider;

import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.apache.commons.validator.routines.UrlValidator.ALLOW_LOCAL_URLS;
import static org.apache.logging.log4j.LogManager.getLogger;
import static org.apache.tika.utils.StringUtils.isEmpty;
import static ua.com.fielden.platform.types.Hyperlink.SupportedProtocols.HTTP;
import static ua.com.fielden.platform.types.Hyperlink.SupportedProtocols.HTTPS;

/**
 * Implementation of {@link IEntityMasterUrlProvider}
 *
 * @author TG Team
 */
@Singleton
public class EntityMasterUrlProvider implements IEntityMasterUrlProvider {

    private static final UrlValidator URL_VALIDATOR = new UrlValidator(new String[] { HTTP.name(), HTTPS.name(), HTTP.name().toLowerCase(), HTTPS.name().toLowerCase() }, ALLOW_LOCAL_URLS);
    private static final Logger LOGGER = getLogger(EntityMasterUrlProvider.class);
    private static final String WARN_INVALID_URL = "Invalid URI [%s].";

    private final String entityMasterUrlPattern;
    private final MasterInfoProvider masterInfoProvider;

    /**
     * Creates new {@link IEntityMasterUrlProvider} instance based on application Uri, and it's web-UI configuration.
     *
     * @param webUiConfig
     * @param appUri
     */
    @Inject
    public EntityMasterUrlProvider(final IWebUiConfig webUiConfig, final @AppUri String appUri) {
        this.entityMasterUrlPattern = (appUri.endsWith("/") ? appUri : appUri + "/") + PARTIAL_URL_PATTERN;
        this.masterInfoProvider = new MasterInfoProvider(webUiConfig);
    }

    public Optional<String> masterUrlFor(final AbstractEntity<?> entity) {
        if (entity == null) {
            return empty();
        }
        final MasterInfo masterInfo = masterInfoProvider.getMasterInfo(DynamicEntityClassLoader.getOriginalType(entity.getType()));
        if (masterInfo != null) {
            final AbstractEntity<?> relativeEntityValue = isEmpty(masterInfo.getRelativePropertyName()) ? entity : entity.get(masterInfo.getRelativePropertyName());
            final var url = entityMasterUrlPattern.formatted(masterInfo.getRootEntityType().getName(), relativeEntityValue.getId());
            if (URL_VALIDATOR.isValid(url)) {
                return of(url);
            } else {
                LOGGER.warn(WARN_INVALID_URL.formatted(url));
            }
        }
        return empty();
    }

}
