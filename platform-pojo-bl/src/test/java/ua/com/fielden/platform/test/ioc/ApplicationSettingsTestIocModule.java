package ua.com.fielden.platform.test.ioc;

import com.google.inject.Provides;
import com.google.inject.name.Names;
import jakarta.inject.Named;
import ua.com.fielden.platform.basic.config.ApplicationSettings;
import ua.com.fielden.platform.basic.config.IApplicationSettings;
import ua.com.fielden.platform.ioc.AbstractPlatformIocModule;

import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.types.tuples.T2.toMap;

/// This IoC module:
/// * creates named bindings ([Named]) for a set of required application properties ([#APP_PROPERTIES]).
/// * binds a map for [IApplicationSettings#currencySymbolMap()].
/// * binds [IApplicationSettings].
///
/// All required application properties must be bound, and not necessarily by this module.
/// For example, the required property `app.name` may be bound directly by this module or by any other module used in conjuction.
///
public class ApplicationSettingsTestIocModule extends AbstractPlatformIocModule {

    public static final Set<String> APP_PROPERTIES = Set.of(
            "app.name",
            "reports.path",
            "domain.path",
            "domain.package",
            "tokens.path",
            "tokens.package",
            "workflow",
            "auth.mode",
            "email.smtp",
            "email.fromAddress",
            "currency.symbol"
    );

    private final Properties properties;

    public ApplicationSettingsTestIocModule(final Properties properties) {
        this.properties = properties;
    }

    @Override
    protected void configure() {
        super.configure();

        bind(IApplicationSettings.class).to(ApplicationSettings.class);

        APP_PROPERTIES.forEach(prop -> {
            if (properties.containsKey(prop)) {
                bindConstant().annotatedWith(Names.named(prop)).to(properties.getProperty(prop));
            }
        });
    }

    @Provides
    @Named("currencySymbolMap")
    Map<String, String> provideCurrencySymbolMap() {
        return properties.stringPropertyNames()
                .stream()
                .map(key -> {
                    final var matcher = CURRENCY_CODE_SYMBOL_PATTERN.matcher(key);
                    if (!matcher.matches()) {
                        return null;
                    }
                    final var code = matcher.group(1);
                    if (code == null) {
                        return null;
                    }
                    return t2(code, properties.getProperty(key));
                })
                .filter(Objects::nonNull)
                .collect(toMap());
    }

    private static final Pattern CURRENCY_CODE_SYMBOL_PATTERN = Pattern.compile("currency\\.(\\w+)\\.symbol");

}
