package ua.com.fielden.platform.example.entities;

import ua.com.fielden.platform.error.Result;

/**
 * Defines a contract for CompletionCertificate related business logic. Most of the defined here methods are relevant for COntractor. The only exception is method {@link #accept(CompletionCertificate)}, which should be invoked by PNL user.
 *
 * @author 01es
 *
 */
public interface ICompletionCertificateController {

    /**
     * A convenience method for obtaining CC by key.
     * @param key
     * @return
     */
    CompletionCertificate get(final Long key);
    /**
     * Places rotable onto a completion certificate.
     * @param cc
     * @param rotable
     * @return
     */
    Result add(final CompletionCertificate cc, final Rotable rotable);

    /**
     * Makes completion certificate ready for PNL review. Only the person originating the completion certificate should be able to make it ready for review by PNL.
     *
     * @param cc
     * @return
     */
    Result makeReady(final CompletionCertificate cc, final Person person);

    /**
     * Completion certificate acceptance action by contractor.
     * @param cc
     * @return
     */
    Result accept(final CompletionCertificate cc, Person person);

    /**
     * Looks for a completion certificate associated with the specified rotable and performs one of the following actions:
     * <ul>
     * <li>If parameter addedToAdvice is true indicating placement of the rotable onto an advice and there is an active CC then update the corresponding CC entry to be completed and check if CC should also be completed.</li>
     * <li>If parameter addedToAdvice is true indicating placement of the rotable onto an advice and there is an inactive CC (uses the latest one) or there is no CC at all then the returned result should indicate an error and placement of the rotable onto advice should be aborted.</li>
     * <li>If parameter addedToAdvice is false indicating advice position correction and there is an active CC then update the corresponding CC entry to be incomplete.</li>
     * <li>If parameter addedToAdvice is false indicating advice position correction and there is an inactive CC (uses the latest one) then make both corresponding CC entry and CC incomplete.</li>
     * <li>If parameter addedToAdvice is false indicating advice position correction and there is no CC at all then the returned result should indicate an error and placement of the rotable onto advice should be aborted. However, this should never happen due to the fact that placement of rotables onto advices is validated against a completion certificate.</li>
     * </ul>
     * <p>
     * This method should never be a direct user action. And the only intention is for it to be used as part of {@link IAdviceController}.
     *
     * @param rotable -- rotable to be placed/removed onto/from an advice
     * @param addedToAdvice -- a flag indicating whether rotable is being placed/removed onto/from an advice
     * @param workshop -- operating workshop, i.e. workshop that is used as the current one
     * @return
     */
    Result check(final Rotable rotable, final boolean addedToAdvice, final Workshop workshop);

    /**
     * Should save completion certificate together with its entries.
     * This method is more of a convenience to avoid direct referencing to {@link ICompletionCertificateDao}.
     *
     * @param cc
     */
    void save(final CompletionCertificate cc);
}
