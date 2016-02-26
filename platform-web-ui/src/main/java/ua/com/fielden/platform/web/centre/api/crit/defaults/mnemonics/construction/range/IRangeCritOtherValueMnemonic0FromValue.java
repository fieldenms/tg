package ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.construction.range;


public interface IRangeCritOtherValueMnemonic0FromValue<V> {
    IRangeCritOtherValueMnemonic1ToValue<V> setFromValue(final V from);
    IRangeCritOtherValueMnemonic1ToValue<V> setFromValueExclusive(final V from);
    IRangeCritOtherValueMnemonic2MissingValue<V> setToValue(final V to);
    IRangeCritOtherValueMnemonic2MissingValue<V> setToValueExclusive(final V to);
    IRangeCritOtherValueMnemonic3Value<V> canHaveNoValue();
}
