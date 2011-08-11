package auction.command;

import java.math.BigDecimal;
import java.util.Currency;

import auction.dao.ItemDAO;
import auction.dao.UserDAO;
import auction.exceptions.BusinessException;
import auction.model.Bid;
import auction.model.Item;
import auction.model.MonetaryAmount;

/**
 * An example of the EJB command pattern.
 * <p>
 * Some parameters are passed in, the control logic is executed, the result comes back.
 *
 * @author Christian Bauer
 */
public class BidForItemCommand extends DataAccessCommand {

    private Long userId;
    private Long itemId;
    private BigDecimal bidAmount;

    private Bid newBid;

    public BidForItemCommand(final Long userId, final Long itemId, final BigDecimal bidAmount) {
	this.userId = userId;
	this.itemId = itemId;
	this.bidAmount = bidAmount;
    }

    public Bid getNewBid() {
	return newBid;
    }

    public void execute() throws CommandException {

	final ItemDAO itemDAO = daoFactory.getItemDAO();
	final UserDAO userDAO = daoFactory.getUserDAO();

	try {

	    final MonetaryAmount newAmount = new MonetaryAmount(bidAmount, Currency.getInstance("USD"));
	    final Bid currentMaxBid = itemDAO.getMaxBid(itemId);
	    final Bid currentMinBid = itemDAO.getMinBid(itemId);

	    final Item item = itemDAO.findById(itemId, true);
	    newBid = item.placeBid(userDAO.findById(userId, false), newAmount, currentMaxBid, currentMinBid);

	} catch (final BusinessException ex) {
	    // Rethrow as a checked exception that can trigger rollback
	    throw new CommandException(ex);
	}
    }

}
