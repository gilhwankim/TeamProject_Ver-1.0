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
import java.util.StringTokenizer;

import data.MenuData;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import pos.javafile.DAO;

public class ReceiptController implements Initializable{
   //���� �ݾ� ��
   @FXML private Label totalPrice; 
   @FXML private DatePicker dateChoice;
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
      payList = dao.selectDate(date); //DB���� ������           
      //���̺� ���� ����
      TableColumn<PaymentInfo, ?> dateTc = receiptTable.getColumns().get(0);
      dateTc.setCellValueFactory(new PropertyValueFactory<>("date"));      
      TableColumn<PaymentInfo, ?> totalpayTc = receiptTable.getColumns().get(1);
      totalpayTc.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));      
      TableColumn<PaymentInfo, ?> paymethodTc = receiptTable.getColumns().get(2);
      paymethodTc.setCellValueFactory(new PropertyValueFactory<>("payMethod"));     
      obPayList = FXCollections.observableArrayList(payList);
      receiptTable.setItems(obPayList);
      //�ش� ��¥�� ������ ������ ������ ���ٰ� ���
      if(payList.size() == 0)
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
      //PaymentInfo�� ������ �����ؼ� �ֱ�
      //�� ������ ���� ����
      int totalTmp = 0;      
      //�޴����� ����
//      StringTokenizer allSt = new StringTokenizer(paymentInfo.getAllMenu(), "@");
//      int stSize = allSt.countTokens(); //�޴���    
      
//      for(int i=0; i<stSize; i++) {
//         String allMenu = allSt.nextToken();
         //�޴� ���γ��� ����
//         StringTokenizer menuSt = new StringTokenizer(allMenu, "$");
//         String name = menuSt.nextToken();
//         int count = Integer.parseInt(menuSt.nextToken());
//         int price = Integer.parseInt(menuSt.nextToken());
//         omTmp =  new MenuData();
//         omTmp.setName(name);
//         omTmp.setPrice(price);
//         omTmp.setCnt(count);
//         omList.add(omTmp);
//      }      
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