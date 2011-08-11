package ua.com.fielden.platform.treemodel.rules.criteria.analyses;

/**
 * This interface defines how domain tree can be represented for simple <b>analyses</b> (single property distribution). <br><br>
 *
 * <b>Important:</b> it is necessary to override {@link #equals(Object)} and {@link #hashCode()} methods in implementors to provide logical comparison of instances. <br><br>
 *
 * @author TG Team
 *
 */
public interface IAnalysisDomainTreeRepresentation extends IAbstractAnalysisDomainTreeRepresentation {
}
