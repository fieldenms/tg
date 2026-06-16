package ua.com.fielden.platform.web.layout;

/// The [ILayoutConfiguration] for a flex layout.
///
/// It adapts the established string-based flex layout to the kind-agnostic `setLayoutFor` API by pairing a flex layout string with a [FlexLayout] manager.
/// This keeps the existing flex layout API unchanged: a flex string passed to `setLayoutFor` is simply wrapped into this configuration.
///
public record FlexLayoutConfiguration(String layout) implements ILayoutConfiguration {

    @Override
    public AbstractLayout<? extends AbstractLayoutSetter<?>> mkLayoutManager(final String name) {
        return new FlexLayout(name);
    }

}