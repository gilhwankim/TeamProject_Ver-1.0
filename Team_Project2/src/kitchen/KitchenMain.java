package kitchen;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class KitchenMain extends Application{
	
	public static Stage KitchenStage;
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		Font.loadFont(getClass().getResourceAsStream("/font/NanumGothi.ttf"), 14);
		Font.loadFont(getClass().getResourceAsStream("/font/NanumGothicBold.ttf"), 14);
		Font.loadFont(getClass().getResourceAsStream("/font/NanumGothicExtraBold.ttf"), 14);
		
		KitchenStage = primaryStage;
		Parent parent = FXMLLoader.load(getClass().getResource("fxml/OrderBoard.fxml"));
		Scene scene = new Scene(parent);
		scene.getStylesheets().add(getClass().getResource("/css/style.css").toString());
		primaryStage.setScene(scene);
		primaryStage.setTitle("Kitchen");
		primaryStage.toFront();
		primaryStage.centerOnScreen();
		primaryStage.show();
	}
	public static void main(String[] args) {
		launch(args);
		
	}
}