package ua.com.fielden.platform.security;

/// Generic top-level security token for an auditing module that may be defined in an application.
public class AuditModuleToken implements ISecurityToken {

    public static final String TITLE = "Auditing";
    public static final String DESC = "Auditing module. Includes information about audited entities.";

}
