package ua.com.fielden.platform.types.markers;

import ua.com.fielden.platform.entity.query.IUserTypeInstantiate;

import java.util.Date;

/// A contract for Hibernate types that map [Date] in UTC.
///
public interface IUtcDateTimeType extends IUserTypeInstantiate<Date> {

}
