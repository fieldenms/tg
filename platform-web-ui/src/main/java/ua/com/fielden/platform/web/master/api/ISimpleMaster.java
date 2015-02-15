package ua.com.fielden.platform.web.master.api;

import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.sample.domain.TgWorkOrder;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.interfaces.ILayout.Orientation;
import ua.com.fielden.platform.web.master.api.actions.EnabledState;
import ua.com.fielden.platform.web.master.api.actions.post.IPostAction;
import ua.com.fielden.platform.web.master.api.actions.pre.IPreAction;
import ua.com.fielden.platform.web.master.api.helpers.IPropertySelector;
import ua.com.fielden.platform.web.minijs.JsCode;

/**
 * This contract is an entry point for Simple Master API -- an embedded domain specific language for constructing simple entity masters.
 *
 * @see <a href="https://github.com/fieldenms/tg/wiki/Web-UI-Design:-Entity-Masters">Entity Masters Wiki</a>
 *
 * @author TG Team
 *
 */
public interface ISimpleMaster {
    public static class ShowMessageDlg implements IPostAction {
        public ShowMessageDlg(final String msg) {
        }

        @Override
        public JsCode build() {
            return null;
        }
    }

    public static class ToastUserWithMessage implements IPostAction {

        public ToastUserWithMessage(final String toastMsg) {
        }

        @Override
        public JsCode build() {
            return null;
        }
    }

    public static class StartInfiniteBlockingPane implements IPreAction {
        public StartInfiniteBlockingPane(final String msg, final String blockingPanelName) {
        }

        @Override
        public JsCode build() {
            return null;
        }
    }

    <T extends AbstractEntity<?>> IPropertySelector<T> forEntity(Class<T> type);

    // TODO Needs to be removed in a fullness of time. This method exists here purely to demonstrate API fluency as part of the development.
    public static void apiExample(final ISimpleMaster sm) {
        sm.forEntity(TgWorkOrder.class)
                    .addProp("vehicle").asAutocompleter().byDesc()
                    .withAction(TgWorkOrder.class) // should really except only functional entities
                    .preAction(new StartInfiniteBlockingPane("Executing message...", "pane-name"))
                    .postActionSuccess(new ToastUserWithMessage("Completed successfully")) // there is no need to imperatively state "stop infinite blocking pane", as this should be automatically understood from the context
                    .postActionError(new ShowMessageDlg("The action has completed with error: {{error}}"))
                    .enabledWhen(EnabledState.EDIT)
                    .icon("icon name").shortDesc("could be used as title").longDesc("this description appeares in as a ")
                .also()
                    .addHtmlLabel("<p>This is some long text, which might spanned into several lines if necessary and may reference master entity properties. "
                        + "The main idea is to provder application developers with the abilit to add arbitrary, not boundn to any property text, to a view.</p>")
                .also()
                    .addProp("status").asAutocompleter().skipValidation().withMatcher(IValueMatcher.class).byDescOnly()
                .also()
                    .addDivider().withTitle("Section Header").atLevel1()
                .also()
                    .addDivider().atLevel2() // a subsection with no title
                .also()
                    .addProp("singleLineComment").asSinglelineText().skipValidation()
                .also()
                    .addProp("hidden").asHiddenText().skipValidation()
                .also()
                    .addProp("attachedFile").asFile().skipValidation()
                .also()
                    .addProp("dateTime").asDateTimePicker().skipValidation()
                .also()
                    .addProp("date").asDatePicker().skipValidation()
                .also()
                    .addProp("time").asTimePicker().skipValidation()
                .also()
                    .addProp("decimalValue").asDecimal().skipValidation()
                .also()
                    .addProp("integerValue").asSpinner().skipValidation()
                .also()
                    .addProp("money").asMoney().skipValidation()
                .also()
                    .addProp("booleanProp").asCheckbox().skipValidation()
                .also()
                    .addProp("desc").asMultilineText().skipValidation().resizable()
                    .withAction(TgWorkOrder.class).enabledWhen(EnabledState.ANY).icon("my cool icon")
                .also()
                    .addAction(TgWorkOrder.class).enabledWhen(EnabledState.VIEW).shortDesc("&New") // & defines keyboard binding
                    .addAction(TgWorkOrder.class).enabledWhen(EnabledState.VIEW).shortDesc("&Edit")
                    .addAction(TgWorkOrder.class).enabledWhen(EnabledState.EDIT).shortDesc("&Save")
                    .addAction(TgWorkOrder.class).enabledWhen(EnabledState.EDIT).shortDesc("&Cancel")
                    .addAction(TgWorkOrder.class).enabledWhen(EnabledState.VIEW).shortDesc("&Refresh")
                .setLayoutFor(Device.DESKTOP, Orientation.LANDSCAPE, "[][flex]")
                .setLayoutFor(Device.TABLET, Orientation.LANDSCAPE, "[][flex]")
                .setLayoutFor(Device.TABLET, Orientation.PORTRAIT, "[][flex]")
                .done();
    }

}
