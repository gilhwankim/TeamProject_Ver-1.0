package tablet.javafile;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

import data.MenuData;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import tablet.TabletMain;

public class Bill {

   private Stage tabletStage = TabletMain.tabletStage;   
   
   public Bill() {}
   
   //���̺� ��꼭 �θ��� �޼���
   public void show(List<MenuData> orderTableTotal) {          
          Stage dialog = new Stage(StageStyle.UNDECORATED);           
             dialog.initModality(Modality.WINDOW_MODAL); //dialog�� ���(������ ������ ���Ұ�)�� ����
             dialog.initOwner(tabletStage);        
             
             Parent tableBill;
           try {
              tableBill = FXMLLoader.load(getClass().getResource("../fxml/tableBill.fxml"));
              Button billExitBtn = (Button)tableBill.lookup("#exit");
              Label totalPrice = (Label)tableBill.lookup("#totalPrice");
              
              @SuppressWarnings("unchecked")
              TableView<MenuData> billTable = (TableView<MenuData>) tableBill.lookup("#billTable");
              
              TableColumn<MenuData, ?> att1 = billTable.getColumns().get(0);
              att1.setCellValueFactory(new PropertyValueFactory<>("name"));
              att1.setText("�޴�");                
              TableColumn<MenuData, ?> att2 = billTable.getColumns().get(1);
              att2.setCellValueFactory(new PropertyValueFactory<>("cnt"));
              att2.setText("����");                
              TableColumn<MenuData, ?> att3 = billTable.getColumns().get(2);
              att3.setCellValueFactory(new PropertyValueFactory<>("total"));
              att3.setText("����");
              
              if(orderTableTotal.size()==0){ //�ϳ��� �ֹ� �������� �ƹ��͵� ������
                 billTable.setPlaceholder(new Label(""));
                 totalPrice.setText("");
              }else { //�ֹ��� �ߴٸ� ��꼭 ����
                ObservableList<MenuData> OmList = FXCollections.observableArrayList(orderTableTotal);   
                 billTable.setItems(OmList); //���̺�信 ����   
                 
                 int totalResult = 0;
                 DecimalFormat df = new DecimalFormat("###,###"); //�������� ��ǥ
                 for(MenuData om : orderTableTotal) {
                   totalResult += om.getTotal(); //��Ų �޴� ������ ����
                 }
                 totalPrice.setText((df.format(totalResult)) + "��"); //������� �ֹ��� ���� ���                   
              }
              //tableBill�� Xǥ�� ������ â����
              billExitBtn.setOnMouseClicked(e -> dialog.close());
              
              Scene scene = new Scene(tableBill);            
                dialog.setScene(scene);
                dialog.setResizable(false);  //����ڰ� ũ�⸦ �������� ���ϰ� ��
                dialog.show();       
           } catch (IOException e) {
              e.printStackTrace(); 
           }      
      }         
}