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
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import tablet.TabletMain;

public class Bill {

   private Stage tabletStage = TabletMain.tabletStage;   
   DecimalFormat df = new DecimalFormat("###,###"); //�������� ��ǥ

   
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
              //ù��° ��
              TableColumn<MenuData, ?> nameColumn = billTable.getColumns().get(0);
              nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
              nameColumn.setText("�޴�");   
              nameColumn.setStyle("-fx-alignment: CENTER;");
              //�ι�° ��
              TableColumn<MenuData, ?> countColumn = billTable.getColumns().get(1);
              countColumn.setCellValueFactory(new PropertyValueFactory<>("cnt"));
              countColumn.setText("����");            
              countColumn.setStyle("-fx-alignment: CENTER;");
              //����° ��
              @SuppressWarnings("unchecked")
            TableColumn<MenuData, Integer> priceColumn = (TableColumn<MenuData, Integer>) billTable.getColumns().get(2);
              priceColumn.setCellValueFactory(new PropertyValueFactory<>("total"));
              priceColumn.setCellFactory(tc -> new TableCell<MenuData, Integer>(){                 
                @Override
                 protected void updateItem(Integer item, boolean empty) {
                    super.updateItem(item, empty);
                    if(empty) {
                       setText(null);
                    }else {
                       setText(df.format(item));
                    }
                 };
              });
              priceColumn.setText("����");
              priceColumn.setStyle("-fx-alignment: CENTER;");
              
              
              if(orderTableTotal.size()==0){ //�ϳ��� �ֹ� �������� �ƹ��͵� ������
                 billTable.setPlaceholder(new Label(""));
                 totalPrice.setText("");
              }else { //�ֹ��� �ߴٸ� ��꼭 ����
                ObservableList<MenuData> OmList = FXCollections.observableArrayList(orderTableTotal);   
                 billTable.setItems(OmList); //���̺�信 ����   
                 
                 int totalResult = 0;
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