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
   DecimalFormat df = new DecimalFormat("###,###"); //단위마다 쉼표

   
   public Bill() {}
   
   //테이블별 계산서 부르는 메서드
   public void show(List<MenuData> orderTableTotal) {          
          Stage dialog = new Stage(StageStyle.UNDECORATED);           
             dialog.initModality(Modality.WINDOW_MODAL); //dialog를 모달(소유자 윈도우 사용불가)로 설정
             dialog.initOwner(tabletStage);
             
             Parent tableBill;
           try {
              tableBill = FXMLLoader.load(getClass().getResource("../fxml/tableBill.fxml"));
              Button billExitBtn = (Button)tableBill.lookup("#exit");
              Label totalPrice = (Label)tableBill.lookup("#totalPrice");
              
              @SuppressWarnings("unchecked")
              TableView<MenuData> billTable = (TableView<MenuData>) tableBill.lookup("#billTable");
              //첫번째 열
              TableColumn<MenuData, ?> nameColumn = billTable.getColumns().get(0);
              nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
              nameColumn.setText("메뉴");   
              nameColumn.setStyle("-fx-alignment: CENTER;");
              //두번째 열
              TableColumn<MenuData, ?> countColumn = billTable.getColumns().get(1);
              countColumn.setCellValueFactory(new PropertyValueFactory<>("cnt"));
              countColumn.setText("수량");            
              countColumn.setStyle("-fx-alignment: CENTER;");
              //세번째 열
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
              priceColumn.setText("가격");
              priceColumn.setStyle("-fx-alignment: CENTER;");
              
              
              if(orderTableTotal.size()==0){ //하나도 주문 안했으면 아무것도 안적힘
                 billTable.setPlaceholder(new Label(""));
                 totalPrice.setText("");
              }else { //주문을 했다면 계산서 나옴
                ObservableList<MenuData> OmList = FXCollections.observableArrayList(orderTableTotal);   
                 billTable.setItems(OmList); //테이블뷰에 세팅   
                 
                 int totalResult = 0;
                 for(MenuData om : orderTableTotal) {
                   totalResult += om.getTotal(); //시킨 메뉴 가격을 더함
                 }
                 totalPrice.setText((df.format(totalResult)) + "원"); //현재까지 주문한 가격 출력                   
              }
              //tableBill의 X표시 누르면 창닫힘
              billExitBtn.setOnMouseClicked(e -> dialog.close());             
            
              Scene scene = new Scene(tableBill);            
                dialog.setScene(scene);
                dialog.setResizable(false);  //사용자가 크기를 조절하지 못하게 함
                dialog.show();       
           } catch (IOException e) {
              e.printStackTrace(); 
           }      
      }         
}