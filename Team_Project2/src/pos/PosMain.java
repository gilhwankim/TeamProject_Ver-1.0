package pos;

import java.text.DecimalFormat;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
	
public class PosMain extends Application{
	
	public static Stage posStage;
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		posStage = primaryStage;
		Parent parent = FXMLLoader.load(getClass().getResource("fxml/pos.fxml"));
		Scene scene = new Scene(parent);
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}