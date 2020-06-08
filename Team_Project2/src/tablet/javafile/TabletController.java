package tablet.javafile;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import data.Data;
import data.MenuData;
import data.OrderMenuData;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tablet.TabletMain;

public class TabletController implements Initializable{

   private Stage tabletStage = TabletMain.tabletStage;
   
   private Socket s;
   private ObjectInputStream ois;
   private ObjectOutputStream oos;
   private Data data;
   
   private @FXML Button orderBtn;
   private @FXML Label total; 
   private @FXML Label tableNo; //���ø��� ���̺��ȣ ǥ�� ��
   private @FXML Button billBtn; //��꼭 ȣ�� ��ư
   private @FXML Button subtractBtn; // - ��ư
   private @FXML Button plusBtn; // + ��ư
   private @FXML TabPane tp;
   
   private String no;
   private List<MenuData> m_list;	//�޴� ����Ʈ
   
   private @FXML TableView<OrderMenuData> orderTable;	//���� ���̺�
   private ObservableList<OrderMenuData> orderTableOl = FXCollections.observableArrayList();
   @Override
   public void initialize(URL location, ResourceBundle resources) {
	   startTablet();
	   orderTable.setItems(orderTableOl);
	   
	   plusBtn.setOnAction( e -> plusBtnAction(e));
	   subtractBtn.setOnAction( e -> subtractBtnAction(e));
	   orderBtn.setOnAction(e -> orderBtnAction(e));
   }
   
   //ó�� �ߴ� â
   private void tableSet(List<String> list) {
	  AnchorPane settableNo = null;
      try {
         settableNo = FXMLLoader.load(getClass().getResource("../fxml/selectTablenum.fxml")); 
      } catch (Exception e) {
    	  e.printStackTrace();
      }
      Button btn = (Button)settableNo.lookup("#btn");
      
      @SuppressWarnings("unchecked")
      ComboBox<String> cb = (ComboBox<String>)settableNo.lookup("#cb");
      ObservableList<String> cb_ol = cb.getItems();
      for(String str : list) {
    	  cb_ol.add(str);
      }
      //������������ ���̺� ��ȣ ����
      cb_ol.sort((a, b) -> Integer.compare(Integer.parseInt(a), Integer.parseInt(b)));
      cb.getSelectionModel().selectFirst();
      
      Scene scene = new Scene(settableNo);
      Stage stage = new Stage();
      stage.setScene(scene);
      stage.setTitle("�¼� ��ȣ ����");
      stage.show();
      
      btn.setOnAction( e -> {
         String str = cb.getSelectionModel().getSelectedItem();
         if(str != null)
            this.no = str;
         
         if(Integer.parseInt(this.no) < 10)
        	 this.tableNo.setText("0" + no);
         else
        	 this.tableNo.setText(no);
         
         data = new Data();
         data.setStatus("��ȣ���ߴ�");
         data.setTableNo(no);
         send(data);
         
         try {
        	data = (Data)ois.readObject();
			if(data.getStatus().equals("����")) {
				//�����鼭 �޴�����Ʈ�� �޾ƿ´�.
				this.m_list = data.getM_list();
				stage.close();
				showTablet();
			}
			
		} catch (Exception e2) {
			e2.printStackTrace();
		}
         
      });
   }
   
   
   private void startTablet() {
	   s = new Socket();
	   try {
		   s.connect(new InetSocketAddress("localhost", 8888));
		   oos = new ObjectOutputStream(s.getOutputStream());
		   data = new Data();
		   data.setStatus("�ȳ�");
		   send(data);
		   
		   ois = new ObjectInputStream(s.getInputStream());
		   data = (Data)ois.readObject();
		   if(data.getStatus().equals("�ȳ�")) {
			   tableSet(data.getNo_list());
		   }
		   
		   
		} catch (Exception e) {
			System.out.println("������ �ȿ��ȴ�~~");
			e.printStackTrace();
		}
	   
	   
   }
   
   private void showTablet() {
	   for(MenuData md : m_list) {
		   System.out.println(md.getName());
	   }
	   this.tabletStage.show();
	   
		MakeTab mt = new MakeTab();
	 	tp = mt.make(m_list, tp);
	 	addMenu();
	 	orderTableSetting();
	   
   }
   
   private void addMenu(){
 	  for(MenuData md : m_list) {
	         try {
	            //�� �޴� ������
	            VBox node = FXMLLoader.load(getClass().getResource("../fxml/menuItem.fxml"));
	            Label labelName = (Label)node.lookup("#labelName");
	            Label labelPrice = (Label)node.lookup("#labelPrice");
	            //menuItem.fxml���� imageView ã�ƿ�
	            ImageView imageMenu = (ImageView)node.lookup("#menuImg");
	            
	            imageMenu.setImage(new Image(new ByteArrayInputStream(md.getImage())));
	            labelName.setText(md.getName());
	            labelPrice.setText(md.getPrice() + "��");            
	            
	            node.setOnMouseClicked(e -> {
	               if(e.getClickCount() == 2) {
	                  System.out.println("�޴��̸� : " + labelName.getText() + "�޴����� : " + labelPrice.getText());
	                  addOrdertable(labelName.getText());
	               }
	            });
	            
	            for(Tab t : tp.getTabs()) {
	            	if(t.getText().equals(md.getCategory())) {
		            	HBox h = (HBox)t.getContent();
		            	VBox v = (VBox)h.getChildren().get(0);
		            	ListView<HBox> lv = (ListView<HBox>)v.getChildren().get(0);
		            	if(lv.getItems().size() == 0) {
		            		HBox hbox = new HBox();
		            		hbox.setSpacing(10);
		            		hbox.getChildren().add(node);
		            		lv.getItems().add(hbox);
		            		break;
		            	}else if(lv.getItems().get(lv.getItems().size() - 1).getChildren().size() % 3 == 0 ) {
		            		HBox hbox = new HBox();
		            		hbox.setSpacing(10);
		            		hbox.getChildren().add(node);
		            		lv.getItems().add(hbox);
		            		break;
		            	}else {
		            		lv.getItems().get(lv.getItems().size()-1).getChildren().add(node);
		            		break;
		            	}
	            	}
	            }
	         }catch (Exception e) {
	        	 e.printStackTrace();
	         }
 	  }
   }
   
   //���� ���̺� �޴� �ֱ�.
   private void addOrdertable(String name) {
	  MenuData md = null;
	  for(MenuData m : m_list) {
	     if(m.getName().equals(name)) {
	        md = m;
	        break;
	     }
	  }
	  for(OrderMenuData om : orderTableOl) {
	     if(om.getM().getName().equals(md.getName())) {
	        om.setCnt(om.getCnt() + 1);
	        orderTable.refresh();
	        priceUpdate();
	        return;
	     }
	  }
	  OrderMenuData om = new OrderMenuData();
	  om.setName(md.getName());
	  om.setM(md);
	  om.setCnt(1);
	  orderTableOl.add(om);
	  orderTable.refresh();
	  priceUpdate();
   }

   //'�ֹ��ϱ�'��ư�� �׼�
   private void orderBtnAction(ActionEvent event) {
	   if(orderTableOl.size() == 0) {
		   return;
	   }
	   List<OrderMenuData> om_list = new ArrayList<>();
      for(OrderMenuData m : orderTableOl) {
//         addTableBill(m);
    	  om_list.add(m);
      }
      Data data = new Data();
      data.setStatus("�ֹ�");
      data.setTableNo(this.no);
      data.setOm_list(om_list);
      send(data);
      
      
      Platform.runLater(() -> orderTableOl.clear());
      priceUpdate();
      total.setText("0��");
      
   }
   
 //���̺�� �ʱ�ȭ
   private void orderTableSetting() {
      TableColumn<OrderMenuData, ?> a = orderTable.getColumns().get(0);
       a.setCellValueFactory(new PropertyValueFactory<>("name"));
       a.setText("�޴� ��");
       
       TableColumn<OrderMenuData, ?> b = orderTable.getColumns().get(1);
       b.setCellValueFactory(new PropertyValueFactory<>("cnt"));
       b.setText("����");
       
       TableColumn<OrderMenuData, ?> c = orderTable.getColumns().get(2);
       c.setCellValueFactory(new PropertyValueFactory<>("total"));
       c.setText("����");
       
   }
   
 //'-' ��ư ����
   private void subtractBtnAction(ActionEvent event) {
      //���Ÿ�Ͽ� �ϳ������� �������ϱ� ����
      if(orderTableOl.size() == 0)
         return;
      //���Ÿ�Ͽ� �־ ���þ��ϰ� ������ �������ϱ� ����
      if(orderTable.getSelectionModel().getSelectedItem() == null)
             return;
      
      String name = orderTable.getSelectionModel().getSelectedItem().getName();
      //���Ÿ�Ͽ��� ������ �޴��� �̸����� ã�´�.
      for(OrderMenuData om : orderTableOl) {
         if(om.getName().equals(name)) {
            if(om.getCnt() > 1)
               om.setCnt(om.getCnt() - 1);
            else 
               orderTableOl.remove(om);
            
            orderTable.refresh();
            priceUpdate();
            break;
         }
      }
      
   }
   //'+' ��ư ����
   private void plusBtnAction(ActionEvent event) {
      if(orderTableOl.size() == 0)
         return;
      if(orderTable.getSelectionModel().getSelectedItem() == null)
             return;
      
      String name = orderTable.getSelectionModel().getSelectedItem().getName();
      
      for(OrderMenuData om : orderTableOl) {
         if(om.getName().equals(name)) {
            om.setCnt(om.getCnt() + 1);
            orderTable.refresh();
            priceUpdate();
         }
      }
   }
   private void priceUpdate() {
	   int i = 0;
	   for(OrderMenuData om : orderTableOl) {
		   i += om.getTotal();
	   }
	   total.setText(i + "��");
   }
   
   private void send(Data data) {
	   try {
		   oos.writeObject(data);
		   oos.flush();
		
		} catch (Exception e) {
			e.printStackTrace();
		}
   }
   
}