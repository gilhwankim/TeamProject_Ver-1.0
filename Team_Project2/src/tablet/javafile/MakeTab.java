package tablet.javafile;

import java.util.List;

import data.MenuData;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;

public class MakeTab {
	
	private boolean flag = false;

	private Tab tab;
	private HBox hbox;

	public MakeTab() {}
	
	public TabPane make(List<MenuData> list, TabPane tabPane) {
		TabPane tp = tabPane;
		for(MenuData m : list) {
			//���� �Ѱ��� ���� ��
			if(tp.getTabs().size() == 0) {
				 //���� ���θ����.(�޴��� ���� ex)������,�Ľ�Ÿ..)
				 tab = new Tab(m.getCategory());
				 hbox = makeHbox();
				 tab.setContent(hbox);
				 tp.getTabs().add(tab);
			}else {
				for(Tab t : tp.getTabs()) {
					if(t.getText().equals(m.getCategory())) {
						flag = true;
						break;
					}
				}
				if(flag == false) {
					 tab = new Tab(m.getCategory());
					 hbox = makeHbox();
					 tab.setContent(hbox);
					 tp.getTabs().add(tab);
				}
				flag = false;
			}
		}
		return tp;
	}
	
	public HBox makeHbox() {
		HBox hbox = null;
		try {
			hbox = FXMLLoader.load(getClass().getResource("../fxml/HBoxInTab.fxml"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return hbox;
	}
	
	
}
