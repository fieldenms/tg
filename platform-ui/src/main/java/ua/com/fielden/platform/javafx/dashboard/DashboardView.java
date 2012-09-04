package ua.com.fielden.platform.javafx.dashboard;

import java.util.List;
import java.util.Set;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.analyses.ISentinelDomainTreeManager;
import ua.com.fielden.platform.selectioncheckbox.SelectionCheckBoxPanel.IAction;
import ua.com.fielden.platform.swing.menu.TreeMenuItem;
import ua.com.fielden.platform.swing.menu.TreeMenuWithTabs;
import ua.com.fielden.platform.swing.review.IEntityMasterManager;

/**
 * A swing container to contain javaFx dashboard.
 *
 * @author TG Team
 *
 */
public class DashboardView extends JFXPanel {
    private static final long serialVersionUID = 9202827128855362320L;

    private final IGlobalDomainTreeManager globalManager;
    private final ICriteriaGenerator criteriaGenerator;
    private final IEntityMasterManager masterManager;
    private TableView<Sentinel> table = new TableView<Sentinel>();
    private TreeMenuItem miMyProfile = null;
    private ObservableList<Sentinel> data;
    private TreeMenuWithTabs<?> treeMenu;

    private final String webLightGrey = "d6d9df";
    private final Color lightGrey = Color.web(webLightGrey);

    public void setTreeMenu(final TreeMenuWithTabs<?> treeMenu) {
	this.treeMenu = treeMenu;
    }

    public TreeMenuWithTabs<?> getTreeMenu() {
	return treeMenu;
    }

    public void setMiMyProfile(final TreeMenuItem miMyProfile) {
	this.miMyProfile = miMyProfile;
    }

    public TreeMenuItem getMiMyProfile() {
	return miMyProfile;
    }

    public TreeMenuItem getRootNode() {
	return (TreeMenuItem) miMyProfile.getRoot();
    }

    /**
     * Creates a swing container with javaFx dashboard.
     *
     * @return
     */
    public DashboardView(final IGlobalDomainTreeManager globalManager, final ICriteriaGenerator criteriaGenerator, final IEntityMasterManager masterManager) {
	this.globalManager = globalManager;
	this.criteriaGenerator = criteriaGenerator;
	this.masterManager = masterManager;
	this.data = createData();
	Platform.runLater(new Runnable() {
	    @Override
	    public void run() {
	        // This method is invoked on the JavaFX thread
	        final Scene scene = createScene();
	        setScene(scene);
	    }
	});
    }

    private ObservableList<Sentinel> createData() {
	final ObservableList<Sentinel> data = FXCollections.observableArrayList();
	final List<Class<?>> mmiTypes = globalManager.entityCentreMenuItemTypes();
	for (final Class<?> mmiType : mmiTypes) {
	    final Set<String> centreNames = globalManager.entityCentreNames(mmiType);
	    for (final String centreName : centreNames) {
		globalManager.initEntityCentreManager(mmiType, centreName); // TODO this operation consumes a lot of time / memory during load
		final ICentreDomainTreeManagerAndEnhancer centreManager = globalManager.getEntityCentreManager(mmiType, centreName);
		final List<String> analysisKeys = centreManager.analysisKeys();
		for (final String analysisName : analysisKeys) {
		    System.out.println("====================+++++++++++++ analysisName == " +  analysisName);
		    final IAbstractAnalysisDomainTreeManager analysis = centreManager.getAnalysisManager(analysisName);
		    if (analysis instanceof ISentinelDomainTreeManager) {
			data.add(new Sentinel(this, criteriaGenerator, globalManager, masterManager, mmiType, centreName, analysisName));
		    }
		}
	    }
	}
	return data;
    }

    public class TrafficLightsCell extends TableCell<Sentinel, Integer> {
	@Override
	protected void updateItem(final Integer arg0, final boolean arg1) {
	    super.updateItem(arg0, arg1);

	    if (getIndex() <= table.getItems().size() - 1) {
		final Sentinel sentinel = table.getItems().get(getIndex());
		//if (arg0 != null) {
		setGraphic(new TrafficLights(sentinel.getModel(),
			new IAction() { @Override public void action() { sentinel.invokeDetails("RED"); }}, //
			new IAction() { @Override public void action() { sentinel.invokeDetails("YELLOW"); }}, //
			new IAction() { @Override public void action() { sentinel.invokeDetails("GREEN"); }} //
			));
		//}
	    }
	}
    }

    public class RefreshCell extends TableCell<Sentinel, Void> {
	@Override
	protected void updateItem(final Void arg0, final boolean arg1) {
	    super.updateItem(arg0, arg1);

	    if (getIndex() <= table.getItems().size() - 1) {
		final Sentinel sentinel = table.getItems().get(getIndex());
		final Button button = new Button("Refresh");
		button.setOnAction(new EventHandler<ActionEvent>() {
		    public void handle(final ActionEvent arg0) {
			sentinel.runQuery();
		    };
		});
		setGraphic(button);
	    }
	}
    }

    private Scene createScene() {
//      final VBox vbox = new VBox();
//      vbox.setSpacing(5);
//      vbox.setPadding(new Insets(10, 0, 0, 10));
//      vbox.getChildren().addAll(label, table);
	final BorderPane borderPane = new BorderPane();

	borderPane.setStyle("-fx-base: #" + webLightGrey + "; -fx-background: #" + webLightGrey + ";");


	final Scene scene = new Scene(borderPane, lightGrey);

        // final Label label = new Label("Dashboard");
        // label.setFont(new Font("Arial", 20));

        table.setEditable(true);
        table.setMaxWidth(Double.MAX_VALUE);
        table.setMaxHeight(Double.MAX_VALUE);

        //        table.setPrefHeight(Double.MAX_VALUE);
//        table.setPrefWidth(Double.MAX_VALUE);

        final TableColumn<Sentinel, String> sentinelCol = new TableColumn<>("Sentinel rule");
        sentinelCol.setMinWidth(100);
        sentinelCol.setCellValueFactory(
                new PropertyValueFactory<Sentinel, String>("sentinelTitle"));

        final TableColumn<Sentinel, Integer> resultCol = new TableColumn<>("Status");
        resultCol.setCellFactory(new Callback<TableColumn<Sentinel, Integer>, TableCell<Sentinel, Integer>>()  {
            @Override
            public TableCell<Sentinel, Integer> call(final TableColumn<Sentinel, Integer> arg0) {
        	return new TrafficLightsCell();
            }
        });
        resultCol.setCellValueFactory(
                new PropertyValueFactory<Sentinel, Integer>("countOfBad"));
        final double resultColWidth = 97;
        resultCol.setMinWidth(resultColWidth);
        resultCol.setPrefWidth(resultColWidth);
        resultCol.setMaxWidth(resultColWidth);

        final TableColumn<Sentinel, Void> refreshCol = new TableColumn<>("Action");
        refreshCol.setCellFactory(new Callback<TableColumn<Sentinel, Void>, TableCell<Sentinel, Void>>()  {
            @Override
            public TableCell<Sentinel, Void> call(final TableColumn<Sentinel, Void> arg0) {
        	return new RefreshCell();
            }
        });
        final double refreshColWidth = 79;
        refreshCol.setMinWidth(refreshColWidth);
        refreshCol.setPrefWidth(refreshColWidth);
        refreshCol.setMaxWidth(refreshColWidth);

        // table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        sentinelCol.prefWidthProperty().bind(table.widthProperty().subtract(resultColWidth + refreshColWidth + 2));

//        firstNameCol.prefWidthProperty().bind(table.widthProperty().divide(4)); // w * 1/4
//        lastNameCol.prefWidthProperty().bind(table.widthProperty().divide(2)); // w * 2/4
//        emailCol.prefWidthProperty().bind(table.widthProperty().divide(4)); // w * 1/4

        table.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(final MouseEvent event) {
        	if (event.getClickCount() == 2 && table.getSelectionModel().getSelectedItem() != null) {
        	    table.getSelectionModel().getSelectedItem().openAnalysis();
        	}
            }
        });
        table.getColumns().addAll(resultCol, sentinelCol, refreshCol);
        table.setItems(data);

        for (final Sentinel sentinel : data) {
            sentinel.runQuery();
        }
        sort();

        borderPane.setCenter(table);

        final VBox vbButtons = new VBox();
        // vbButtons.setSpacing(10);
        vbButtons.setPadding(new Insets(4, 0, 4, 0));

        final Button refreshAll = new Button("Refresh");
        refreshAll.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent arg0) {
        	refreshAll();
            }
        });
        vbButtons.getChildren().add(refreshAll);
        borderPane.setBottom(vbButtons);
        refreshAll.translateXProperty().bind(table.widthProperty().subtract(refreshAll.widthProperty().add(5.0)));
        return (scene);
    }

    public void refreshAll() {
	table.getSortOrder().clear();
	table.getItems().clear();
	table.getItems().addAll(createData());
        for (final Sentinel sentinel : table.getItems()) {
            sentinel.runQuery();
        }
        sort();
    }

    private void sort() {
	final int index = 0;
	table.getSortOrder().add(table.getColumns().get(index));
        table.getColumns().get(index).setSortType(TableColumn.SortType.DESCENDING);
        table.getColumns().get(index).setSortable(true); // This performs a sort
    }
}
