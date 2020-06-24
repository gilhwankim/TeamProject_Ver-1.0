package tablet;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class TabletMain extends Application{
	
	public static Stage tabletStage;
	@Override
	public void start(Stage primaryStage) throws Exception {
		tabletStage = primaryStage;
		Parent parent = FXMLLoader.load(getClass().getResource("fxml/tablet.fxml"));
		Scene scene = new Scene(parent);
		primaryStage.setScene(scene);
		primaryStage.setTitle("Tablet");
		primaryStage.toFront();
	}
	public static void main(String[] args) {
		launch(args);
	}

}