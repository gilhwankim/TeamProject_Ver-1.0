package pos.management;

import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import data.MenuData;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import pos.javafile.AllPayment;
import pos.javafile.DAO;

public class ReceiptController implements Initializable{
   //결제 금액 라벨
   @FXML private Label totalPrice; 
   @FXML private DatePicker dateChoice;
   @FXML private Button refund;
   DecimalFormat df = new DecimalFormat("###,###"); //단위마다 쉼표
   private DAO dao = DAO.getinstance(); //거래내역 DB                        
   @FXML private TableView<PaymentInfo> receiptTable; //거래일자, 결제금액, 결제방법 테이블 
   @FXML private TableView<MenuData> receiptDetailTable; //메뉴명, 단가, 수량, 금액 테이블   
   List<PaymentInfo> payList; //결제내역 리스트   
   ObservableList<PaymentInfo> obPayList; //결제내역 테이블 리스트
   List<MenuData> omList = new ArrayList<MenuData>(); //각 결제내역의 세부메뉴 리스트
   ObservableList<MenuData> obOmList = FXCollections.observableArrayList(); //세부메뉴 테이블 리스트   
  
   @Override
   public void initialize(URL location, ResourceBundle resources) {   
      //현재 날짜 출력 및 DB에서 오늘날짜 거래내역 가져옴
      showDb(currentDateSetting());
      //선택한 날짜에 맞는 거래내역 가져옴
      dateChoice.valueProperty().addListener((ov, oldDate, newDate)->{
         //날짜를 다지워서 null값이 되면 전체 내역 출력
         if(newDate == null) {
            showDb(null);
            obOmList.clear();
         }else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년MM월dd일");  
            showDb(newDate.format(formatter));
            obOmList.clear();
         }
      });    
      dateChoice.setOnKeyReleased( e -> {
         if(dateChoice.isFocused()) {
            if(e.getCode() == KeyCode.ESCAPE) {
               dateChoice.setValue(null);
            }
         }
      });
      
      refund.setOnAction( e -> {
          PaymentInfo pi = receiptTable.getSelectionModel().getSelectedItem();
          if(pi!=null) {
        	  //DB,POS가 가지고 있는 데이터를 환불로 바꾼다.
        	  AllPayment.refundsList(pi);
        	  //테이블 새로고침
        	  receiptTable.refresh();
          }
       });
      
      //큰 테이블에서 선택하면 세부테이블에 내용이 출력되게 함
      receiptTable.getSelectionModel().selectedItemProperty().addListener((p, old, news) ->{
         omList.clear(); //전에 있던 내용은 없앰
         try {
         totalPrice.setText(df.format(showDetailDB(news)) + "원"); //세부내용 보여주는 동시에 총결제 가격을 리턴받아 라벨에 보여줌
         }catch (Exception e) {
      }
      });
   }
   //오늘 날짜 나타내는 메서드
   public String currentDateSetting() {
     SimpleDateFormat sdf = new SimpleDateFormat("yyyy년MM월dd일");
      String today = sdf.format(new Date());
      dateChoice.setValue(LocalDate.now());
      return today;
   } 
   //거래내역 가져오는 메서드
   public void showDb(String date) { 
	   obPayList = FXCollections.observableArrayList();
	   if(date==null) {
		   obPayList = FXCollections.observableArrayList(AllPayment.payInfoList);
	   }else {
	   
	   for(PaymentInfo pi : AllPayment.payInfoList) {
		   if(pi.getDate().substring(0, 11).equals(date)) {
			   obPayList.add(pi);
		   }
	   	 }
	   }
      //테이블에 내용 세팅
      TableColumn<PaymentInfo, ?> dateTc = receiptTable.getColumns().get(0);
      dateTc.setCellValueFactory(new PropertyValueFactory<>("date"));      
      TableColumn<PaymentInfo, ?> totalpayTc = receiptTable.getColumns().get(1);
      totalpayTc.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));      
      TableColumn<PaymentInfo, String> paymethodTc = (TableColumn<PaymentInfo, String>)receiptTable.getColumns().get(2);
      paymethodTc.setCellValueFactory(new PropertyValueFactory<>("payMethod"));
      paymethodTc.setCellFactory(tc -> new TableCell<PaymentInfo, String>(){
         @Override
       protected void updateItem(String item, boolean empty) {
          super.updateItem(item, empty);
          if(!empty) {
             if(item.equals("환불")) {
                setText(item);
                setTextFill(Color.RED);
             }else {
                setText(item);
                setTextFill(Color.BLACK);
             }
          }
       }
      });
      
      receiptTable.setItems(obPayList);
      //해당 날짜에 가져올 내역이 없으면 없다고 출력
      if(obPayList.size() == 0)
      try {
        obOmList.clear();
      }catch (Exception e) {
   }
      receiptDetailTable.setItems(obOmList);
      receiptTable.setPlaceholder(new Label("내역이 없습니다."));
      receiptDetailTable.setPlaceholder(new Label("내역이 없습니다."));
      totalPrice.setText(""); //받은게 없으니 결제 금액 안보임
      
   }
   //테이블에 클릭된 결제내역을 받아와서 세부테이블에 보여주는 메서드
   public int showDetailDB(PaymentInfo paymentInfo) {
      //총 결제액 담을 변수
      int totalTmp = 0;      
      //세부테이블에 세팅
      TableColumn<MenuData, ?> menuNameTc = receiptDetailTable.getColumns().get(0);
      menuNameTc.setCellValueFactory(new PropertyValueFactory<>("name"));      
      TableColumn<MenuData, ?> menuPriceTc = receiptDetailTable.getColumns().get(1);
      menuPriceTc.setCellValueFactory(new PropertyValueFactory<>("price"));      
      TableColumn<MenuData, ?> menuCountTc = receiptDetailTable.getColumns().get(2);
      menuCountTc.setCellValueFactory(new PropertyValueFactory<>("cnt"));
      TableColumn<MenuData, ?> totalPriceTc = receiptDetailTable.getColumns().get(3);
      totalPriceTc.setCellValueFactory(new PropertyValueFactory<>("total"));
      ObservableList<MenuData> obOmList2 = FXCollections.observableArrayList(paymentInfo.getAllMenu());
      receiptDetailTable.setItems(obOmList2);  
      
      for(MenuData om : obOmList2) {
         totalTmp += om.getTotal();
      }
      return totalTmp; //총결제 금액 리턴      
   }
}