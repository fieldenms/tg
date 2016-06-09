package ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.construction.range;


public interface IRangeCritOtherValueMnemonic1ToValue<V> extends IRangeCritOtherValueMnemonic2MissingValue<V> {
    IRangeCritOtherValueMnemonic2MissingValue<V> setToValue(final V to);
    IRangeCritOtherValueMnemonic2MissingValue<V> setToValueExclusive(final V to);
}
