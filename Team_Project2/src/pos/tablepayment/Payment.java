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
   
   private TextField amountOfPayment; //����, ī����� ȭ�� û���ݾ�
   private TextField amountReceived; //���� �ݾ�
   private TextField price; //���ݿ����� ���� �ݾ�
   private TextField change; //�Ž�����
   private TextField customerInfo; //���ݿ����� ��ȣ
   private TextField cardNum; //���ݿ����� ��ȣ
   private Button cashPaymentbtn; //���� ���� ��ư
   private Button cardPaymentbtn; //ī�� ���� ��ư
   private Button approval; //���ο�û ��ư
   private ChoiceBox<String> installment; //�Һΰ��� ���̽� �ڽ�
   private DecimalFormat df = new DecimalFormat("###,###"); //�������� ��ǥ
   private Tablet t;
   
   
   DAO dao = DAO.getinstance();
   
   //������
   public Payment() {   }
   
   //���ݰ���
   public void cashShow(int total,Tablet t) {
      System.out.println("���ݰ���");
       Stage dialog = new Stage(StageStyle.UNDECORATED);
       dialog.initModality(Modality.WINDOW_MODAL); //dialog�� ���(������ ������ ���Ұ�)�� ����
       dialog.initOwner(tablePaymentStage);
        this.t = t;
       try {
          Parent cashPayment = FXMLLoader.load(getClass().getResource("CashPayment.fxml"));
          Scene scene = new Scene(cashPayment);
          dialog.setScene(scene);
          dialog.setResizable(false); //����ڰ� ũ�⸦ �������� ���ϰ� ��
          dialog.show();
          //�����ݾ�
          amountOfPayment = (TextField)cashPayment.lookup("#cashAmountOfPayment");
          amountOfPayment.setText(df.format(total));
          //�����ݾ�
          amountReceived = (TextField)cashPayment.lookup("#amountReceived");
          amountReceived.setText("0");
          cashFieldListener(amountReceived);
          //�Ž�����
          change = (TextField)cashPayment.lookup("#change");
          change.setText("0");
          //���ݰ��� ȭ�� �ݱ�
          Button cashExitBtn = (Button)cashPayment.lookup("#exit");
          //���� ��ư
          cashPaymentbtn = (Button)cashPayment.lookup("#Payment");
          cashPaymentbtn.setOnAction(e->{
        	  String received = amountReceived.getText().replaceAll(",", "");
        	  String price =  amountOfPayment.getText().replaceAll(",", "");
        	  if(received.equals("0")) {
        		  System.out.println("���� �ݾ��� �����ϴ�.");
        		  return;
        	  }else if(Integer.parseInt(received)<Integer.parseInt(price)) {
        		  System.out.println("�ݾ��� �����մϴ�!");
        		  return;
        	  }
        	  
             SimpleDateFormat sdf = new SimpleDateFormat("yyyy��MM��dd�� hh:mm:ss a");
             String date = sdf.format(new Date());
             dao.PaymentInfo(date,t.om_list,amountOfPayment.getText(), false, customerInfo.getText());
             //�����Ϸ� �� Stage����
             pop.popupMsg("���� �ݾ�: " + amountReceived.getText()+"��\n\n���� �ݾ�: "+ amountOfPayment.getText() + "��\n\n������ �Ϸ�Ǿ����ϴ�.");
             dialog.close();
             t.deleteTableinfo();
             
          });
          //���ݿ����� �ݾ�
          price = (TextField)cashPayment.lookup("#price");
          price.setText(df.format(total));
          //���ݿ����� ��ȣ
          customerInfo = (TextField)cashPayment.lookup("#customerInfo");
          keyLocked(customerInfo, 10);
          //���ݿ����� ���ο�û ��ư
          approval = (Button)cashPayment.lookup("#approval");
          
          //���οϷ� �˸� �˾�â
          approval.setOnAction(e->{
        	  if(amountOfPayment.getText().equals("0")) {
        		  pop.popupMsg("���� �ݾ��� �����ϴ�.");
        		  return;
        	  }else if(customerInfo.getText().equals("")) {
        		  pop.popupMsg("��ȣ�� �Է����ּ���.");
        		  return;
        	  }
        	  pop.popupMsg("���� �ݾ�: " + amountOfPayment.getText() +"\n\n��û ��ȣ: " + customerInfo.getText() + "\n\n������ �Ϸ�Ǿ����ϴ�.");
          });
          
          cashExitBtn.setOnMouseClicked(e-> dialog.close());
          Button clear = (Button)cashPayment.lookup("#clear");
          clear.setOnAction(e->clearNumberProcess(e));
          Button[] btns = new Button[15];
          //���� ��ư �̸� ����
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
   
   
   //���� �ݾ� �ؽ�Ʈ �ʵ� ����
   public void cashFieldListener(TextField tf) {
	   //�ؽ�Ʈ �ʵ� ����
	   tf.textProperty().addListener((ob,olds,news)->{
		   try {
          news = news.replaceAll(",", "");
          if(news.equals("")) {
        	  tf.setText("0");
          }
          //int �Ѱ� ���� �ʰ��ϴ� ���� �Է��ϸ�(news) �ݾ��� �߰��� �� ���ٴ� �˸�â�� ���� ���� ��(olds)�� �ǵ�����.
          else if(Long.valueOf(news)>=2147483647) {
             JOptionPane.showMessageDialog(null, "�� �̻� �ݾ��� �߰��� �� �����ϴ�.");
             tf.setText(olds);
             
          } else {
          //�Է¹��� ���� ���� ���� �ݾ��� õ���� ���� �ٽ� �ؼ� TextField�� �Է�.
          String received = df.format(Long.valueOf(news));
          //�齺���̽�Ű�� ����� ��� ","����ִ� �۾� ������ 1000���� �������� ����ԵǸ� the start must be <= the end ��� ������ ����
          //Platform.runLater�� setText�� ������Ѵ�. (","����ִ� �۾��� ���ٸ� �׳� setText�� �ᵵ �������.)
          Platform.runLater(()->{
        	  tf.setText(received);
        	  //Platform.runLater()�� setText�ϰԵǸ� �ؽ�Ʈ�ʵ� �� ���� �� 
        	  //�⺻������ Ŀ���� ���� ��(0 ��°)�� ��ġ�ϰ� �Ǳ⶧���� �ؽ�Ʈ�ʵ��� ������ ��ġ�� �ٽ� ����ش�.
        	  tf.positionCaret(tf.getLength());
          }); 
          //�Ž����� ���
          calChange();
          }
		   } catch (Exception e) {
			   //�߸��� ���� �����ݾ�ĭ�� �Է��ϸ� ���� ������ �ǵ�����.(���ڰ� �ƴ� ��)
			   System.out.println("�߸��� ���� �Է��ϼ̽��ϴ�.");
			   tf.setText(olds);
		}
       	});
   }

   //�����Է� ��ư
   public void numberProcess(ActionEvent event) {
   //������ ��ư ��
   String number = ((Button)event.getSource()).getText();
   //TextField�� append
   amountReceived.appendText(number);
   }
   
   //1000,5000,10000,50000���� ���� �Է� ��ư
   public void addNumberProcess(ActionEvent event) {
      String addNumber = ((Button)event.getSource()).getText();
      addNumber = addNumber.replaceAll(",", "");
      int Num = Integer.parseInt(amountReceived.getText().replaceAll(",", ""));
      int addNum = Integer.parseInt(addNumber);
      amountReceived.setText(df.format(Num+addNum));
   }
   
   //�Ž����� 
   public void calChange() {
      //�����ݾ�
      String receivedStr = amountReceived.getText();
      //�����ݾ�
      String paymentStr = amountOfPayment.getText();
      //�����ݾ�
      receivedStr = receivedStr.replaceAll(",", "");
      paymentStr = paymentStr.replaceAll(",", "");
      if((Integer.parseInt(receivedStr)-Integer.parseInt(paymentStr))<=0) {
         change.setText("0");
         return;
      }
      change.setText(df.format(Integer.parseInt(receivedStr)-Integer.parseInt(paymentStr)));
   }
   
   //�����ݾ� �ؽ�Ʈ �ʱ�ȭ
   public void clearNumberProcess(ActionEvent event) {
      amountReceived.setText("0");
      change.setText("0");
   }
   
   //Ű �Է� ����
   public void keyLocked(TextField textField,int length) {
      textField.textProperty().addListener((observable, oldValue, newValue) -> {
         
         //�ؽ�Ʈ �ʵ� ���� ����
         if(newValue.length()>length) {
            textField.setText(newValue.substring(0,length));
         }
//         �ؽ�Ʈ�ʵ� ���ڸ� �Է°����ϵ��� ����.
         if (!newValue.matches("\\d*")) {
        	 textField.setText(newValue.replaceAll("[^\\d]", ""));
         }
       });
   }
   
   //ī�����
   public void cardShow(int total, Tablet t) {
      System.out.println("ī�����");
       Stage dialog = new Stage(StageStyle.UNDECORATED);
       dialog.initModality(Modality.WINDOW_MODAL); //dialog�� ���(������ ������ ���Ұ�)�� ����
       dialog.initOwner(tablePaymentStage);
         this.t = t;
       try {
          Parent cardPayment = FXMLLoader.load(getClass().getResource("PayingCreditCard.fxml"));
          Scene scene = new Scene(cardPayment);
          dialog.setScene(scene);
          dialog.setResizable(false); //����ڰ� ũ�⸦ �������� ���ϰ� ��
          dialog.show();
          //ī����� ȭ�� �ݱ�
          Button cardExitBtn = (Button)cardPayment.lookup("#exit");
          cardExitBtn.setOnMouseClicked(e-> dialog.close());
          
          cardNum = (TextField)cardPayment.lookup("#cardNum");
          keyLocked(cardNum, 16);
          //���̽��ڽ�
          installment = (ChoiceBox<String>)cardPayment.lookup("#installment");
          //�����ݾ�                                                                                                
          amountOfPayment = (TextField)cardPayment.lookup("#cardAmountOfPayment");
          amountOfPayment.setText(df.format(total));
          //������ư
          cardPaymentbtn = (Button)cardPayment.lookup("#payment");
          cardPaymentbtn.setOnAction(e->{
             if(cardNum.getText().equals("")) {
                System.out.println("ī�� ��ȣ�� �Է����ּ���");
                pop.popupMsg("ī�� ��ȣ�� �Է����ּ���.");
                return;
             }else if(installment.getSelectionModel().getSelectedItem()==null) {
                System.out.println("�Һΰ����� �������ּ���");
                pop.popupMsg("�Һΰ����� �������ּ���.");
                return;
             }
             SimpleDateFormat sdf = new SimpleDateFormat("yyyy��MM��dd�� hh:mm:ss a");
             String date = sdf.format(new Date());
             dao.PaymentInfo(date,t.om_list,amountOfPayment.getText(), true, cardNum.getText());
             
             //�����Ϸ� �� Stage����
             pop.popupMsg("���� �ݾ�: "+ amountOfPayment.getText() + "��\n\n�Һ� ����: "+installment.getSelectionModel().getSelectedItem()+"\n\n������ �Ϸ�Ǿ����ϴ�.");
             dialog.close();
             t.deleteTableinfo();
          });
          //ī���ȣ

          
       } catch (IOException e) { e.printStackTrace(); }
   }
}