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
			//탭이 한개도 없을 떄
			if(tp.getTabs().size() == 0) {
				 //탭을 새로만든다.(메뉴의 종류 ex)샐러드,파스타..)
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
