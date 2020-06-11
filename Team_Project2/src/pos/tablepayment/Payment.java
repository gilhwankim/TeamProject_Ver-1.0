package pos.tablepayment;

import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;

import data.MenuData;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Payment implements Initializable{
   
   private Stage tablePaymentStage;
   public ObservableList<MenuData> orderMenu_list = FXCollections.observableArrayList();
   private @FXML TextField amountReceived; //받은 금액
   private @FXML TextField change; //거스름돈
   private @FXML TextField amountOfPayment; //현금, 카드결제 화면 청구금액
   private DecimalFormat df = new DecimalFormat("###,###"); //단위마다 쉼표
   //생성자
   public Payment() {
	   System.out.println("생성자@@@@");
	   
   }
   
   @Override
	public void initialize(URL location, ResourceBundle resources) {
	   System.out.println("이니셜@@");
	   String str = "제발요";
	   amountReceived.setText(str);
   }  
   
   //현금결제
   public void cashShow(int total) {
       System.out.println("현금결제");
       Stage dialog = new Stage(StageStyle.UNDECORATED);
       dialog.initModality(Modality.APPLICATION_MODAL); //dialog를 모달(소유자 윈도우 사용불가)로 설정
       dialog.initOwner(tablePaymentStage);
       
       try {
          Parent cashPayment = FXMLLoader.load(getClass().getResource("CashPayment.fxml"));
          Scene scene = new Scene(cashPayment);
          dialog.setScene(scene);
          dialog.setResizable(false); //사용자가 크기를 조절하지 못하게 함
          dialog.show();
          System.out.println("111111");
          //청구금액
          
          //현금결제 화면 닫기
          Button cashExitBtn = (Button)cashPayment.lookup("#exit");
          cashExitBtn.setOnMouseClicked(e-> dialog.close());
           
       } catch (IOException e) { e.printStackTrace(); }
   }
   
   //카드결제
//   public void cardShow(int total) {
//      System.out.println("카드결제");
//       Stage dialog = new Stage(StageStyle.UNDECORATED);
//       dialog.initModality(Modality.WINDOW_MODAL); //dialog를 모달(소유자 윈도우 사용불가)로 설정
//       dialog.initOwner(tablePaymentStage);
//         
//       try {
//          Parent cardPayment = FXMLLoader.load(getClass().getResource("PayingCreditCard.fxml"));
//          Scene scene = new Scene(cardPayment);
//          dialog.setScene(scene);
//          dialog.setResizable(false); //사용자가 크기를 조절하지 못하게 함
//          dialog.show();
//          //카드결제 화면 닫기
//          Button cardExitBtn = (Button)cardPayment.lookup("#exit");
//          cardExitBtn.setOnMouseClicked(e-> dialog.close());
//          
//          //결제금액                                                                                                
////          amountOfPayment.setText(df.format(total));
//            
//       } catch (IOException e) { e.printStackTrace(); }
//   }
   
   //숫자입력 버튼
   public void numberProcess(ActionEvent event) {
	   //눌러진 버튼 값
	   String number = ((Button)event.getSource()).getText();
	   
	   amountReceived.textProperty().addListener((ob,olds,news)->{
		   news = news.replaceAll(",", "");
		   if(Long.valueOf(news)>=2147483647) {
			   JOptionPane.showMessageDialog(null, "더 이상 금액을 추가할 수 없습니다.");
			   amountReceived.setText(olds);
		   }
	   });
	   
	   amountReceived.appendText(number);
	   String tmp = amountReceived.getText();
	   tmp = tmp.replaceAll(",", "");
	   amountReceived.setText(df.format(Long.valueOf(tmp)));
	   calChange();
   }
   
   //1000,5000,10000단위 숫자 입력 버튼
   public void addNumberProcess(ActionEvent event) {
	   String addNumber = ((Button)event.getSource()).getText();
	   addNumber = addNumber.replaceAll(",", "");
	   if(amountReceived.getText().equals("")) {
		   amountReceived.setText("0");
	   }
	   int Num = Integer.parseInt(amountReceived.getText());
	   int addNum = Integer.parseInt(addNumber);
	   amountReceived.setText(Integer.toString(Num+addNum));
	   calChange();
   }
   //받은금액 텍스트 초기화
   public void clearNumberProcess(ActionEvent event) {
	   amountReceived.clear();
   }
   
   public void calChange() {
	   //받은금액
	   System.out.println("@@@@받은금액" + amountReceived.getText());
	   String str = amountReceived.getText();
	   //결제금액
	   System.out.println("@@@@결제금액"+amountOfPayment.getText());
	   String str2 = amountOfPayment.getText();
	   str = str.replaceAll(",", "");
	   str2 = str2.replaceAll(",", "");
	   if((Integer.parseInt(str2)-Integer.parseInt(str))<=0) {
		   change.setText("0");
		   return;
	   }
	   
	   change.setText(Integer.toString(Integer.parseInt(str2)-Integer.parseInt(str)));
	   
   }
}