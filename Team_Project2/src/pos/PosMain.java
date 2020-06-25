package pos;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;
	
public class PosMain extends Application{
	
	public static Stage posStage;
	@Override
	public void start(Stage primaryStage) throws Exception {
		Font.loadFont(getClass().getResourceAsStream("/font/NanumGothi.ttf"), 12);
		Font.loadFont(getClass().getResourceAsStream("/font/NanumGothicExtraBold.ttf"), 12);
		
		posStage = primaryStage;
		Parent parent = FXMLLoader.load(getClass().getResource("fxml/pos.fxml"));
		Scene scene = new Scene(parent);
		scene.getStylesheets().add(getClass().getResource("/css/style.css").toString());
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}