package ua.com.fielden.platform.entity.annotation;

import ua.com.fielden.platform.utils.IDates;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Date;

/// An annotation for properties of type [java.util.Date] to enforce dependent time-zone mode in Web UI logic.
///
/// Please note, that appropriate [IDates#now()] and [IDates#zoned(Date)] method variations are required in server-side logic.
/// I.e. it may not be sufficient to annotate the properties with this annotation.
///
/// ```java
///static DateTime now(final IDates dates) {
///    return new DateTime(getTimeZone(dates));
///}
///static DateTime zoned(final Date date, fina IDates dates) {
///    return new DateTime(date, getTimeZone(dates));
///}
///private static DateTimeZone getTimeZone(final IDates dates) {
///    if (dates.requestTimeZone().isEmpty()) {
///        throw failure(ERR_TIME_ZONE_IS_MISSING);
///    }
///    return dates.requestTimeZone().get();
///}
/// ```
///
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface DependentTimeZoneMode {

}
