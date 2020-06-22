package pos.tablepayment;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JOptionPane;

import data.MenuData;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import pos.javafile.DAO;
import pos.javafile.Popup;
import pos.javafile.PosController.Tablet;

public class Payment {
   
   private Stage tablePaymentStage;
   public ObservableList<MenuData> orderMenu_list = FXCollections.observableArrayList();
   public Popup pop = new Popup();
   
   private TextField amountOfPayment; //현금, 카드결제 화면 청구금액
   private TextField amountReceived; //받은 금액
   private TextField price; //현금영수증 결제 금액
   private TextField change; //거스름돈
   private TextField customerInfo; //현금영수증 번호
   private TextField cardNum; //현금영수증 번호
   private Button cashPaymentbtn; //현금 결제 버튼
   private Button cardPaymentbtn; //카드 결제 버튼
   private Button approval; //승인요청 버튼
   private ChoiceBox<String> installment; //할부개월 초이스 박스
   private DecimalFormat df = new DecimalFormat("###,###"); //단위마다 쉼표
   private Tablet t;
   
   
   DAO dao = DAO.getinstance();
   
   //생성자
   public Payment() {   }
   
   //현금결제
   public void cashShow(int total,Tablet t) {
      System.out.println("현금결제");
       Stage dialog = new Stage(StageStyle.UNDECORATED);
       dialog.initModality(Modality.WINDOW_MODAL); //dialog를 모달(소유자 윈도우 사용불가)로 설정
       dialog.initOwner(tablePaymentStage);
        this.t = t;
       try {
          Parent cashPayment = FXMLLoader.load(getClass().getResource("CashPayment.fxml"));
          Scene scene = new Scene(cashPayment);
          dialog.setScene(scene);
          dialog.setResizable(false); //사용자가 크기를 조절하지 못하게 함
          dialog.show();
          //결제금액
          amountOfPayment = (TextField)cashPayment.lookup("#cashAmountOfPayment");
          amountOfPayment.setText(df.format(total));
          //받은금액
          amountReceived = (TextField)cashPayment.lookup("#amountReceived");
          amountReceived.setText("0");
          cashFieldListener(amountReceived);
          //거스름돈
          change = (TextField)cashPayment.lookup("#change");
          change.setText("0");
          //현금결제 화면 닫기
          Button cashExitBtn = (Button)cashPayment.lookup("#exit");
          //결제 버튼
          cashPaymentbtn = (Button)cashPayment.lookup("#Payment");
          cashPaymentbtn.setOnAction(e->{
        	  String received = amountReceived.getText().replaceAll(",", "");
        	  String price =  amountOfPayment.getText().replaceAll(",", "");
        	  if(received.equals("0")) {
        		  System.out.println("받은 금액이 없습니다.");
        		  return;
        	  }else if(Integer.parseInt(received)<Integer.parseInt(price)) {
        		  System.out.println("금액이 부족합니다!");
        		  return;
        	  }
        	  
             SimpleDateFormat sdf = new SimpleDateFormat("yyyy년MM월dd일 hh:mm:ss a");
             String date = sdf.format(new Date());
             dao.PaymentInfo(date,t.om_list,amountOfPayment.getText(), false, customerInfo.getText());
             //결제완료 후 Stage닫음
             pop.popupMsg("받은 금액: " + amountReceived.getText()+"원\n\n결제 금액: "+ amountOfPayment.getText() + "원\n\n결제가 완료되었습니다.");
             dialog.close();
             t.deleteTableinfo();
             
          });
          //현금영수증 금액
          price = (TextField)cashPayment.lookup("#price");
          price.setText(df.format(total));
          //현금영수증 번호
          customerInfo = (TextField)cashPayment.lookup("#customerInfo");
          keyLocked(customerInfo, 10);
          //현금영수증 승인요청 버튼
          approval = (Button)cashPayment.lookup("#approval");
          
          //승인완료 알림 팝업창
          approval.setOnAction(e->{
        	  if(amountOfPayment.getText().equals("0")) {
        		  pop.popupMsg("결제 금액이 없습니다.");
        		  return;
        	  }else if(customerInfo.getText().equals("")) {
        		  pop.popupMsg("번호를 입력해주세요.");
        		  return;
        	  }
        	  pop.popupMsg("결제 금액: " + amountOfPayment.getText() +"\n\n요청 번호: " + customerInfo.getText() + "\n\n승인이 완료되었습니다.");
          });
          
          cashExitBtn.setOnMouseClicked(e-> dialog.close());
          Button clear = (Button)cashPayment.lookup("#clear");
          clear.setOnAction(e->clearNumberProcess(e));
          Button[] btns = new Button[15];
          //계산기 버튼 이름 설정
          for(int i=0; i<15; i++) {
             btns[i] = (Button)cashPayment.lookup("#Num"+i);
             if(i<11) {
                 btns[i].setOnAction((e)->numberProcess(e));  
             } else {
                btns[i].setOnAction((e)->addNumberProcess(e));  
             }
          }
//          
       } catch (IOException e) {e.printStackTrace(); }
   }
   
   
   //받은 금액 텍스트 필드 검증
   public void cashFieldListener(TextField tf) {
	   //텍스트 필드 검증
	   tf.textProperty().addListener((ob,olds,news)->{
		   try {
          news = news.replaceAll(",", "");
          if(news.equals("")) {
        	  tf.setText("0");
          }
          //int 한계 값을 초과하는 값을 입력하면(news) 금액을 추가할 수 없다는 알림창을 띄우고 이전 값(olds)로 되돌린다.
          else if(Long.valueOf(news)>=2147483647) {
             JOptionPane.showMessageDialog(null, "더 이상 금액을 추가할 수 없습니다.");
             tf.setText(olds);
             
          } else {
          //입력받은 값을 더한 현재 금액을 천단위 구분 다시 해서 TextField에 입력.
          String received = df.format(Long.valueOf(news));
          //백스페이스키를 사용할 경우 ","찍어주는 작업 떄문에 1000단위 지점에서 지우게되면 the start must be <= the end 라는 오류가 나서
          //Platform.runLater로 setText를 해줘야한다. (","찍어주는 작업이 없다면 그냥 setText로 써도 상관없다.)
          Platform.runLater(()->{
        	  tf.setText(received);
        	  //Platform.runLater()로 setText하게되면 텍스트필드 값 변경 후 
        	  //기본적으로 커서가 제일 앞(0 번째)에 위치하게 되기때문에 텍스트필드의 끝에다 위치를 다시 잡아준다.
        	  tf.positionCaret(tf.getLength());
          }); 
          //거스름돈 계산
          calChange();
          }
		   } catch (Exception e) {
			   //잘못된 값을 받은금액칸에 입력하면 이전 값으로 되돌린다.(숫자가 아닌 값)
			   System.out.println("잘못된 값을 입력하셨습니다.");
			   tf.setText(olds);
		}
       	});
   }

   //숫자입력 버튼
   public void numberProcess(ActionEvent event) {
   //눌러진 버튼 값
   String number = ((Button)event.getSource()).getText();
   //TextField에 append
   amountReceived.appendText(number);
   }
   
   //1000,5000,10000,50000단위 숫자 입력 버튼
   public void addNumberProcess(ActionEvent event) {
      String addNumber = ((Button)event.getSource()).getText();
      addNumber = addNumber.replaceAll(",", "");
      int Num = Integer.parseInt(amountReceived.getText().replaceAll(",", ""));
      int addNum = Integer.parseInt(addNumber);
      amountReceived.setText(df.format(Num+addNum));
   }
   
   //거스름돈 
   public void calChange() {
      //받은금액
      String receivedStr = amountReceived.getText();
      //결제금액
      String paymentStr = amountOfPayment.getText();
      //결제금액
      receivedStr = receivedStr.replaceAll(",", "");
      paymentStr = paymentStr.replaceAll(",", "");
      if((Integer.parseInt(receivedStr)-Integer.parseInt(paymentStr))<=0) {
         change.setText("0");
         return;
      }
      change.setText(df.format(Integer.parseInt(receivedStr)-Integer.parseInt(paymentStr)));
   }
   
   //받은금액 텍스트 초기화
   public void clearNumberProcess(ActionEvent event) {
      amountReceived.setText("0");
      change.setText("0");
   }
   
   //키 입력 제한
   public void keyLocked(TextField textField,int length) {
      textField.textProperty().addListener((observable, oldValue, newValue) -> {
         
         //텍스트 필드 길이 제한
         if(newValue.length()>length) {
            textField.setText(newValue.substring(0,length));
         }
//         텍스트필드 숫자만 입력가능하도록 제한.
         if (!newValue.matches("\\d*")) {
        	 textField.setText(newValue.replaceAll("[^\\d]", ""));
         }
       });
   }
   
   //카드결제
   public void cardShow(int total, Tablet t) {
      System.out.println("카드결제");
       Stage dialog = new Stage(StageStyle.UNDECORATED);
       dialog.initModality(Modality.WINDOW_MODAL); //dialog를 모달(소유자 윈도우 사용불가)로 설정
       dialog.initOwner(tablePaymentStage);
         this.t = t;
       try {
          Parent cardPayment = FXMLLoader.load(getClass().getResource("PayingCreditCard.fxml"));
          Scene scene = new Scene(cardPayment);
          dialog.setScene(scene);
          dialog.setResizable(false); //사용자가 크기를 조절하지 못하게 함
          dialog.show();
          //카드결제 화면 닫기
          Button cardExitBtn = (Button)cardPayment.lookup("#exit");
          cardExitBtn.setOnMouseClicked(e-> dialog.close());
          
          cardNum = (TextField)cardPayment.lookup("#cardNum");
          keyLocked(cardNum, 16);
          //초이스박스
          installment = (ChoiceBox<String>)cardPayment.lookup("#installment");
          //결제금액                                                                                                
          amountOfPayment = (TextField)cardPayment.lookup("#cardAmountOfPayment");
          amountOfPayment.setText(df.format(total));
          //결제버튼
          cardPaymentbtn = (Button)cardPayment.lookup("#payment");
          cardPaymentbtn.setOnAction(e->{
             if(cardNum.getText().equals("")) {
                System.out.println("카드 번호를 입력해주세요");
                pop.popupMsg("카드 번호를 입력해주세요.");
                return;
             }else if(installment.getSelectionModel().getSelectedItem()==null) {
                System.out.println("할부개월을 선택해주세요");
                pop.popupMsg("할부개월을 선택해주세요.");
                return;
             }
             SimpleDateFormat sdf = new SimpleDateFormat("yyyy년MM월dd일 hh:mm:ss a");
             String date = sdf.format(new Date());
             dao.PaymentInfo(date,t.om_list,amountOfPayment.getText(), true, cardNum.getText());
             
             //결제완료 후 Stage닫음
             pop.popupMsg("결제 금액: "+ amountOfPayment.getText() + "원\n\n할부 개월: "+installment.getSelectionModel().getSelectedItem()+"\n\n결제가 완료되었습니다.");
             dialog.close();
             t.deleteTableinfo();
          });
          //카드번호

          
       } catch (IOException e) { e.printStackTrace(); }
   }
}