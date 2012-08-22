package ua.com.fielden.platform.gis;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Reflection;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
* Changing Text Fonts
* @author cdea
*/
public class ChangingTextFonts extends Application {
    /**
     * @param args
     *            the command line arguments
     */
    public static void main(final String[] args) {
	Application.launch(args);
    }

    @Override
    public void start(final Stage primaryStage) {
	primaryStage.setTitle("Chapter 1-4 Changing Text Fonts");
	final Group root = new Group();
	final Scene scene = new Scene(root, 550, 250, Color.WHITE);
	// Serif with drop shadow
	final Text text2 = new Text(50, 50, "JavaFX 2.0: Intro. by Example");
	final Font serif = Font.font("Serif", 30);
	text2.setFont(serif);
	text2.setFill(Color.RED);
	final DropShadow dropShadow = new DropShadow();
	dropShadow.setOffsetX(2.0f);
	dropShadow.setOffsetY(2.0f);
	dropShadow.setColor(Color.rgb(50, 50, 50, .588));
	text2.setEffect(dropShadow);
	root.getChildren().add(text2);
	// SanSerif
	final Text text3 = new Text(50, 100, "JavaFX 2.0: Intro. by Example");
	final Font sanSerif = Font.font("SanSerif", 30);
	text3.setFont(sanSerif);
	text3.setFill(Color.BLUE);
	root.getChildren().add(text3);
	// Dialog
	final Text text4 = new Text(50, 150, "JavaFX 2.0: Intro. by Example");
	final Font dialogFont = Font.font("Dialog", 30);
	text4.setFont(dialogFont);
	text4.setFill(Color.rgb(0, 255, 0));
	root.getChildren().add(text4);
	// Monospaced
	final Text text5 = new Text(50, 200, "JavaFX 2.0: Intro. by Example");
	final Font monoFont = Font.font("Monospaced", 30);
	text5.setFont(monoFont);
	text5.setFill(Color.BLACK);
	root.getChildren().add(text5);
	final Reflection refl = new Reflection();
	refl.setFraction(0.8f);
	text5.setEffect(refl);
	primaryStage.setScene(scene);
	primaryStage.show();
    }
}
