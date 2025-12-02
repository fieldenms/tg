package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.security.tokens.security_matrix.SecurityRoleAssociation_CanRead_Token;
import ua.com.fielden.platform.security.tokens.security_matrix.SecurityRoleAssociation_CanSave_Token;

/// Companion object for entity [SecurityMatrixSaveAction].
///
public interface SecurityMatrixSaveActionCo extends IEntityDao<SecurityMatrixSaveAction> {

    String ERR_SECURITY_TOKEN_NOT_FOUND = "Security token [%s] could not be found.",
           ERR_CAN_NOT_DELETE_ASSOCIATIONS_FOR_READING = "Removing the [%s] security token from all your user roles will block access to the Security Matrix.".formatted(SecurityRoleAssociation_CanRead_Token.TITLE),
           ERR_CAN_NOT_DELETE_ASSOCIATIONS_FOR_SAVING = "Removing the [%s] security token from all your user roles will prevent you from editing the Security Matrix.".formatted(SecurityRoleAssociation_CanSave_Token.TITLE);

}
