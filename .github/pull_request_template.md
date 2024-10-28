<!-- The pull request title should consist of the word "Issue", a space, #issue number, space, dash (-), space, then the issue title. -->

Resolve #nnn <!-- replace with the issue number, in case of several issues addressed by a PR, list them all -->

# To be completed by the pull request creator

This section should be completed with reference to section [Preparing PR](https://github.com/fieldenms/devops/wiki/Code-and-PR-reviews#preparing-pr) of the [Code and PR reviews](https://github.com/fieldenms/devops/wiki/Code-and-PR-reviews) wiki page.

<!-- Delete any items that are not applicable. -->

- [ ] Converted PR to draft while it is being prepared by tapping the "Convert to draft" link beneath the "Reviewers" section.

- [ ] A self-review of all changes has been completed, and the changes are in sync with the issue requirements.

- [ ] Changes to the requirements have been reflected in the issue description.

- [ ] Any "leftovers" such as sysouts, printing of stack traces, and any other "temporary" code, have been removed.

- [ ] Minor refactorings, such as renamings, extraction of constants, etc., have been addressed.

- [ ] Developer documentation (e.g., comments, Javadoc), have been provided where required.

- [ ] New tests have been written, if required, to cover the new functionality.

- [ ] All tests (all existing tests and all new tests) pass successfully.

- [ ] Established security practices have been followed, including the existence and attribution of security tokens.

- [ ] If there are database schema changes (e.g., domain metadata entities changed their structure), an SQL script has been written and tested to update the database schema in a non-destructive way (to allow for blue/green deployments).

- [ ] If there are destructive schema changes (e.g., removal of an entity or property), a second SQL script has been written to be applied to the database in the future once the release has been fully accepted.

- [ ] Changes have been inspected for possible NPE situations, and the changes are sufficiently defensive.

- [ ] Correct transaction demarcation is in place, which is especially important in cases with nested transactional code (e.g., iterative data process), data streams, code shared between interactive actions and calls as part of some other execution workflows, such as background jobs.

- [ ] The correct base branch has been selected for these changes to be merged into.

- [ ] The latest changes from the base branch have already been merged into this feature branch (and tested).

- [ ] Added a change overview to the issue description or as a wiki page, referenced in the issue description.
      Some issue might be very descriptive and server in place of a wiki page.
      In such cases consider adding label `Wiki like` to the issue.

- [ ] Changes subject to performance considerations have been evaluated, and tested against production-size data if applicable.

- [ ] This pull request does not contain significant changes, and at least one appropriate reviewer has been selected.

- [ ] This pull request does contain significant changes, the section "Significant changes" below is completed and at least one Senior Software Engineer with the relevant area of expertise has been selected as reviewer.

- [ ] The `In progress` label has been removed from the issue.

- [ ] The `Pull request` label has been added to the issue.

- [ ] Made PR ready for review by tapping the "Ready for review" button below the list of commits on the PR page.

## Additional details

<!-- Provide any additional details that may be significant or helpful to the pull request reviewer. -->
<!-- Delete this section if it is not applicable. -->

## Significant changes

<!-- If the pull request contains significant changes (as defined in the wiki page, link below), they must be listed here, and at least one Senior Software Engineer with the relevant area of expertise must have been selected as reviewer (see above). -->
<!-- Delete this section if it is not applicable. -->

This pull request contains significant changes as defined in [the wiki page](https://github.com/fieldenms/devops/wiki/Code-and-PR-reviews#significant-changes).

Details are as follows:

<!-- Insert details of the significant changes here as task list to allow the reviewers to tick them as completed during their review. -->

# To be completed by the pull request reviewer

This section should be completed with reference to section [Performing PR review](https://github.com/fieldenms/devops/wiki/Code-and-PR-reviews#performing-pr-review) of the [Code and PR reviews](https://github.com/fieldenms/devops/wiki/Code-and-PR-reviews) wiki page.

<!-- Delete any items that are not applicable. -->

- [ ] The `In progress` label has been added to the pull request in GitHub.

- [ ] The issue requirements have been read and understood (along with any relevant emails and/or Slack messages).

- [ ] The correct base branch is specified, and that base branch is up-to-date in the local source.

- [ ] The issue branch has been checked out locally, and had the base branch merged into it.

- [ ] All automated tests pass successfully.

- [ ] Ensure the implementation satisfies the functional requirements.

- [ ] Ensure that code changes are secure and align with the established coding practices, including code formatting and naming conventions.

- [ ] Ensure that code changes are documented and covered with automated tests as applicable.

- [ ] Ensure that code changes are well-suited for informal reasoning.

- [ ] Ensure that changes are documented for the end-user (a software engineer in the case of TG, or an application user in the case of TG-based applications).

- [ ] If there are significant changes (described above), special attention has been paid to them.
      Marked the task items in section "Significant changes" as completed to indicated that corresponding changes have been reviewed, improved if necessary, and approved.

- [ ] The issue or issues addressed by the pull request are associated with the relevant release milestone.

# To be completed by the pull request reviewer once the changes have been reviewed and accepted

- [ ] The changes have been merged into the base branch (unless there is a specific request not to do so, e.g., they are to be released to SIT).

- [ ] The issue branch has been deleted (unless the changes have not been merged - see above, or there is a specific request not to do so).

- [ ] The `In progress` label has been removed from the pull request.

- [ ] The `Pull request` label has been removed from the pull request.

