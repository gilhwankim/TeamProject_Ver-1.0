package pos.javafile;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class MakeNode {
	VBox vbox;
	HBox h;
	
	public MakeNode() {
	}
	
	public VBox make() {
		try {
			 vbox = FXMLLoader.load(getClass().getResource("../fxml/table.fxml"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return vbox;
	}
	
	public HBox menuMake(String n, int c) {
		try {
			 h = FXMLLoader.load(getClass().getResource("../fxml/tableNode.fxml"));
			 Label name = (Label)h.lookup("#name");
			 Label cnt = (Label)h.lookup("#cnt");
			 name.setText(n);
			 if(c == 0)
	             cnt.setText("");
	          else
	             cnt.setText(c + "");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return h;
	}
}
