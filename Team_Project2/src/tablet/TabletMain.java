package tablet;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class TabletMain extends Application{
	
	public static Stage tabletStage;
	@Override
	public void start(Stage primaryStage) throws Exception {
		Font.loadFont(getClass().getResourceAsStream("/font/NanumGothi.ttf"), 14);
		Font.loadFont(getClass().getResourceAsStream("/font/NanumGothicBold.ttf"), 14);
		Font.loadFont(getClass().getResourceAsStream("/font/NanumGothicExtraBold.ttf"), 14);
		
		tabletStage = primaryStage;
		Parent parent = FXMLLoader.load(getClass().getResource("fxml/tablet.fxml"));
		Scene scene = new Scene(parent);
		primaryStage.setScene(scene);
		scene.getStylesheets().add(getClass().getResource("/css/style.css").toString());
		primaryStage.setTitle("Tablet");
		primaryStage.toFront();
	}
	public static void main(String[] args) {
		launch(args);
	}
}