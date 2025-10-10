package ua.com.fielden.platform.web.test.server;

import ua.com.fielden.platform.security.provider.ISecurityTokenNodeTransformation;
import ua.com.fielden.platform.security.provider.SecurityTokenNode;
import ua.com.fielden.platform.security.provider.SecurityTokenNodeTransformations;
import ua.com.fielden.platform.security.tokens.open_simple_master.UserMaster_CanOpen_Token;
import ua.com.fielden.platform.security.tokens.user.*;

import java.util.List;
import java.util.SortedSet;

/// A demo security token node transformer to illustrate the use of [SecurityTokenNodeTransformations] and
/// the ability to build token hierarchies with more than one level of nesting.
///
public class TgTestApplicationSecurityTokenNodeTransformation implements ISecurityTokenNodeTransformation {

    @Override
    public SortedSet<SecurityTokenNode> transform(SortedSet<SecurityTokenNode> tree) {
        final var trans1 = SecurityTokenNodeTransformations.setParentOf(
                List.of(
                        UserMaster_CanOpen_Token.class,
                        User_CanDelete_Token.class,
                        User_CanRead_Token.class),
                User_CanSave_Token.class);

        final var trans2 = SecurityTokenNodeTransformations.setParentOf(
                List.of(
                        User_CanReadModel_Token.class,
                        ReUser_CanRead_Token.class,
                        ReUser_CanReadModel_Token.class),
                User_CanRead_Token.class);

//        final var trans3 = SecurityTokenNodeTransformations.setParentOf(
//                List.of(
//                        OpenUserMasterAction_CanOpen_Token.class,
//                        UserMaster_OpenMain_MenuItem_CanAccess_Token.class,
//                        UserMaster_OpenUserAndRoleAssociation_MenuItem_CanAccess_Token.class),
//                UserMaster_CanOpen_Token.class);

        return SecurityTokenNodeTransformations
                .compose(trans1, trans2/*, trans3*/)
                .transform(tree);
    }

}
