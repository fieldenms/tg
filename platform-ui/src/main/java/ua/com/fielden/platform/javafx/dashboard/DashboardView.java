package ua.com.fielden.platform.javafx.dashboard;

import java.util.List;
import java.util.Set;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.analyses.ISentinelDomainTreeManager;
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
		    final IAbstractAnalysisDomainTreeManager analysis = centreManager.getAnalysisManager(analysisName);
		    if (analysis instanceof ISentinelDomainTreeManager) {
			data.add(new Sentinel(this, criteriaGenerator, globalManager, masterManager, mmiType, centreName, analysisName));
		    }
		}
	    }
	}
	return data;
    }

    private Scene createScene() {
//      final VBox vbox = new VBox();
//      vbox.setSpacing(5);
//      vbox.setPadding(new Insets(10, 0, 0, 10));
//      vbox.getChildren().addAll(label, table);
	final BorderPane borderPane = new BorderPane();

	final Scene scene = new Scene(borderPane);

        final Label label = new Label("Dashboard");
        label.setFont(new Font("Arial", 20));

        table.setEditable(true);
        table.setMaxWidth(Double.MAX_VALUE);
        table.setMaxHeight(Double.MAX_VALUE);
//        table.setPrefHeight(Double.MAX_VALUE);
//        table.setPrefWidth(Double.MAX_VALUE);

        final TableColumn firstNameCol = new TableColumn("Sentinel");
        firstNameCol.setMinWidth(100);
        firstNameCol.setCellValueFactory(
                new PropertyValueFactory<Sentinel, String>("sentinelTitle"));

        final TableColumn lastNameCol = new TableColumn("Last Name");
        lastNameCol.setMinWidth(100);
        lastNameCol.setCellValueFactory(
                new PropertyValueFactory<Sentinel, String>("lastName"));

//        firstNameCol.prefWidthProperty().bind(table.widthProperty().divide(4)); // w * 1/4
//        lastNameCol.prefWidthProperty().bind(table.widthProperty().divide(2)); // w * 2/4
//        emailCol.prefWidthProperty().bind(table.widthProperty().divide(4)); // w * 1/4

        table.setItems(data);
        table.getColumns().addAll(firstNameCol, lastNameCol);

        table.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(final MouseEvent event) {
        	if (event.getClickCount() == 2) {
        	    table.getSelectionModel().getSelectedItem().openAnalysis();
        	}
            }
        });


        borderPane.setTop(label);
        borderPane.setCenter(table);
        return (scene);
    }
}
