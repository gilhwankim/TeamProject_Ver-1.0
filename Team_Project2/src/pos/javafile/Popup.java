package pos.javafile;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowListener;
import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
//�˾�â Ŭ����
public class Popup {
	public Label textLabel;
	public Button okbtn;
	public Stage stage;
	
	public Popup() {
		AnchorPane ac = null;
		stage = new Stage();
		try {
			ac = FXMLLoader.load(getClass().getResource("../fxml/Popup.fxml"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		okbtn = (Button)ac.lookup("#okbtn");
		okbtn.setText("Ȯ��");
		okbtn.setOnAction((e)->stage.close());
		textLabel = (Label)ac.lookup("#textLabel");
		Scene scene = new Scene(ac);
		stage.setScene(scene);
	}
	//�˾� �޼��� ���� 
	public void popupMsg(String label) {
		textLabel.setAlignment(Pos.CENTER);
		textLabel.setText(label);
		stage.show();
	}
}
