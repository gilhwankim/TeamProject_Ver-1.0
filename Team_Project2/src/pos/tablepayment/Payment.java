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
   private @FXML TextField amountReceived; //���� �ݾ�
   private @FXML TextField change; //�Ž�����
   private @FXML TextField amountOfPayment; //����, ī����� ȭ�� û���ݾ�
   private DecimalFormat df = new DecimalFormat("###,###"); //�������� ��ǥ
   //������
   public Payment() {
	   System.out.println("������@@@@");
	   
   }
   
   @Override
	public void initialize(URL location, ResourceBundle resources) {
	   System.out.println("�̴ϼ�@@");
	   String str = "���߿�";
	   amountReceived.setText(str);
   }  
   
   //���ݰ���
   public void cashShow(int total) {
       System.out.println("���ݰ���");
       Stage dialog = new Stage(StageStyle.UNDECORATED);
       dialog.initModality(Modality.APPLICATION_MODAL); //dialog�� ���(������ ������ ���Ұ�)�� ����
       dialog.initOwner(tablePaymentStage);
       
       try {
          Parent cashPayment = FXMLLoader.load(getClass().getResource("CashPayment.fxml"));
          Scene scene = new Scene(cashPayment);
          dialog.setScene(scene);
          dialog.setResizable(false); //����ڰ� ũ�⸦ �������� ���ϰ� ��
          dialog.show();
          System.out.println("111111");
          //û���ݾ�
          
          //���ݰ��� ȭ�� �ݱ�
          Button cashExitBtn = (Button)cashPayment.lookup("#exit");
          cashExitBtn.setOnMouseClicked(e-> dialog.close());
           
       } catch (IOException e) { e.printStackTrace(); }
   }
   
   //ī�����
//   public void cardShow(int total) {
//      System.out.println("ī�����");
//       Stage dialog = new Stage(StageStyle.UNDECORATED);
//       dialog.initModality(Modality.WINDOW_MODAL); //dialog�� ���(������ ������ ���Ұ�)�� ����
//       dialog.initOwner(tablePaymentStage);
//         
//       try {
//          Parent cardPayment = FXMLLoader.load(getClass().getResource("PayingCreditCard.fxml"));
//          Scene scene = new Scene(cardPayment);
//          dialog.setScene(scene);
//          dialog.setResizable(false); //����ڰ� ũ�⸦ �������� ���ϰ� ��
//          dialog.show();
//          //ī����� ȭ�� �ݱ�
//          Button cardExitBtn = (Button)cardPayment.lookup("#exit");
//          cardExitBtn.setOnMouseClicked(e-> dialog.close());
//          
//          //�����ݾ�                                                                                                
////          amountOfPayment.setText(df.format(total));
//            
//       } catch (IOException e) { e.printStackTrace(); }
//   }
   
   //�����Է� ��ư
   public void numberProcess(ActionEvent event) {
	   //������ ��ư ��
	   String number = ((Button)event.getSource()).getText();
	   
	   amountReceived.textProperty().addListener((ob,olds,news)->{
		   news = news.replaceAll(",", "");
		   if(Long.valueOf(news)>=2147483647) {
			   JOptionPane.showMessageDialog(null, "�� �̻� �ݾ��� �߰��� �� �����ϴ�.");
			   amountReceived.setText(olds);
		   }
	   });
	   
	   amountReceived.appendText(number);
	   String tmp = amountReceived.getText();
	   tmp = tmp.replaceAll(",", "");
	   amountReceived.setText(df.format(Long.valueOf(tmp)));
	   calChange();
   }
   
   //1000,5000,10000���� ���� �Է� ��ư
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
   //�����ݾ� �ؽ�Ʈ �ʱ�ȭ
   public void clearNumberProcess(ActionEvent event) {
	   amountReceived.clear();
   }
   
   public void calChange() {
	   //�����ݾ�
	   System.out.println("@@@@�����ݾ�" + amountReceived.getText());
	   String str = amountReceived.getText();
	   //�����ݾ�
	   System.out.println("@@@@�����ݾ�"+amountOfPayment.getText());
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