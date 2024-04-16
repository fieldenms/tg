package ua.com.fielden.platform.web.uri;

import jakarta.inject.Inject;
import org.apache.tika.utils.StringUtils;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.master.MasterInfo;
import ua.com.fielden.platform.web.annotations.AppUri;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.interfaces.IUriGenerator;
import ua.com.fielden.platform.web.view.master.MasterInfoProvider;

import java.util.Optional;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.apache.tika.utils.StringUtils.isEmpty;

/**
 * Implementation of {@link IUriGenerator}
 */
public class UriGenerator implements IUriGenerator {

    private final IWebUiConfig webUiConfig;
    private final String appUri;
    private final MasterInfoProvider masterInfoProvider;

    /**
     * Creates new {@link IUriGenerator} instance based on application Uri, and it's web-UI configuration.
     *
     * @param webUiConfig
     * @param appUri
     */
    @Inject
    public UriGenerator(final IWebUiConfig webUiConfig, @AppUri String appUri) {
        this.webUiConfig = webUiConfig;
        this.appUri = appUri;
        this.masterInfoProvider = new MasterInfoProvider(webUiConfig);
    }
    public <T extends AbstractEntity<?>> Optional<String> generateUri(final T entity) {
        final MasterInfo masterInfo = masterInfoProvider.getMasterInfo(entity.getType());
        if (masterInfo != null) {
            final AbstractEntity<?> relativeEntityValue = isEmpty(masterInfo.getRelativePropertyName()) ? entity : entity.get(masterInfo.getRelativePropertyName());
            return of(format("%s/#/master/%s/%s", appUri, masterInfo.getRootEntityType().getName(), relativeEntityValue.getId()));
        }
        return empty();
    }
}
