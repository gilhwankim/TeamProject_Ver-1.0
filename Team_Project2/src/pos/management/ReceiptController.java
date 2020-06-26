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
   //���� �ݾ� ��
   @FXML private Label totalPrice; 
   @FXML private DatePicker dateChoice;
   @FXML private Button refund;
   DecimalFormat df = new DecimalFormat("###,###"); //�������� ��ǥ
   private DAO dao = DAO.getinstance(); //�ŷ����� DB                        
   @FXML private TableView<PaymentInfo> receiptTable; //�ŷ�����, �����ݾ�, ������� ���̺� 
   @FXML private TableView<MenuData> receiptDetailTable; //�޴���, �ܰ�, ����, �ݾ� ���̺�   
   List<PaymentInfo> payList; //�������� ����Ʈ   
   ObservableList<PaymentInfo> obPayList; //�������� ���̺� ����Ʈ
   List<MenuData> omList = new ArrayList<MenuData>(); //�� ���������� ���θ޴� ����Ʈ
   ObservableList<MenuData> obOmList = FXCollections.observableArrayList(); //���θ޴� ���̺� ����Ʈ   
  
   @Override
   public void initialize(URL location, ResourceBundle resources) {   
      //���� ��¥ ��� �� DB���� ���ó�¥ �ŷ����� ������
      showDb(currentDateSetting());
      //������ ��¥�� �´� �ŷ����� ������
      dateChoice.valueProperty().addListener((ov, oldDate, newDate)->{
         //��¥�� �������� null���� �Ǹ� ��ü ���� ���
         if(newDate == null) {
            showDb(null);
            obOmList.clear();
         }else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy��MM��dd��");  
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
        	  //DB,POS�� ������ �ִ� �����͸� ȯ�ҷ� �ٲ۴�.
        	  AllPayment.refundsList(pi);
        	  //���̺� ���ΰ�ħ
        	  receiptTable.refresh();
          }
       });
      
      //ū ���̺��� �����ϸ� �������̺� ������ ��µǰ� ��
      receiptTable.getSelectionModel().selectedItemProperty().addListener((p, old, news) ->{
         omList.clear(); //���� �ִ� ������ ����
         try {
         totalPrice.setText(df.format(showDetailDB(news)) + "��"); //���γ��� �����ִ� ���ÿ� �Ѱ��� ������ ���Ϲ޾� �󺧿� ������
         }catch (Exception e) {
      }
      });
   }
   //���� ��¥ ��Ÿ���� �޼���
   public String currentDateSetting() {
     SimpleDateFormat sdf = new SimpleDateFormat("yyyy��MM��dd��");
      String today = sdf.format(new Date());
      dateChoice.setValue(LocalDate.now());
      return today;
   } 
   //�ŷ����� �������� �޼���
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
      //���̺� ���� ����
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
             if(item.equals("ȯ��")) {
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
      //�ش� ��¥�� ������ ������ ������ ���ٰ� ���
      if(obPayList.size() == 0)
      try {
        obOmList.clear();
      }catch (Exception e) {
   }
      receiptDetailTable.setItems(obOmList);
      receiptTable.setPlaceholder(new Label("������ �����ϴ�."));
      receiptDetailTable.setPlaceholder(new Label("������ �����ϴ�."));
      totalPrice.setText(""); //������ ������ ���� �ݾ� �Ⱥ���
      
   }
   //���̺� Ŭ���� ���������� �޾ƿͼ� �������̺� �����ִ� �޼���
   public int showDetailDB(PaymentInfo paymentInfo) {
      //�� ������ ���� ����
      int totalTmp = 0;      
      //�������̺� ����
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
      return totalTmp; //�Ѱ��� �ݾ� ����      
   }
}