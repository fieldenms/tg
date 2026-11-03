package ua.com.fielden.platform.mcp;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.mcp.McpAuthorisationCode;
import ua.com.fielden.platform.mcp.McpAuthorisationCodeCo;

@EntityType(McpAuthorisationCode.class)
public class McpAuthorisationCodeDao extends CommonEntityDao<McpAuthorisationCode> implements McpAuthorisationCodeCo {

}
