package auction.command;

import auction.dao.*;

/**
 * A specialized command for data access on the server side.
 * 
 * @author Christian Bauer
 */
public abstract class DataAccessCommand implements Command {

    protected DAOFactory daoFactory;

    public void setDAOFactory(DAOFactory daoFactory) {
	this.daoFactory = daoFactory;
    }
}
