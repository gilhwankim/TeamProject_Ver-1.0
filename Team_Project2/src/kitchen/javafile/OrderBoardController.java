package kitchen.javafile;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import data.Data;
import data.MenuData;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import kitchen.KitchenMain;
import makeSound.MakeSound;

public class OrderBoardController implements Initializable {
   
      //�������ῡ �ʿ��� ���
      private Socket s;
      private ObjectInputStream ois;
      private ObjectOutputStream oos;
      
      private Data data;
      private @FXML Label tableNum; //���̺� ��ȣ �� 
      private TableView<OrderBoardMenu> kitchenTableview; //���̺� ��
      private @FXML ListView<HBox> orderBoardlv; //����Ʈ��
      private ObservableList<HBox> tableViewOl = FXCollections.observableArrayList();
      private List<AnchorPane> nodeList = new ArrayList<>();
      private int cnt = 0;
      
      private @FXML Label dateLabel; // �������� ��� ��¥
      private @FXML Label timeLabel; // �������� ��� �ð�
      
   @Override
   public void initialize(URL location, ResourceBundle resources) {
      startClient();
      //�������� ��� ��¥
      new Clock(dateLabel,timeLabel);
       //�����ư
      KitchenMain.KitchenStage.setOnCloseRequest( e -> stopClient());
   }
   public OrderBoardController() {
   }
     private void startClient() {
         try {
           //���� ����
            s = new Socket();
            s.connect(new InetSocketAddress("localhost", 8888));
            //��Ʈ��ũ �ڿ� �Ҵ�
            oos = new ObjectOutputStream(s.getOutputStream());
            data = new Data();
            //�������� �ֹ��� �˸���.
            data.setStatus("�ֹ�");
            oos.writeObject(data);
            oos.flush();
            ois = new ObjectInputStream(s.getInputStream());
            
            System.out.println(data.getStatus());
            System.out.println("���Ἲ��!");
            //����
            kitchenConnect();
            
      }catch (Exception e) {
         e.printStackTrace();
         if(!s.isClosed()) {
               try {
                   ois.close();
                   oos.close();
                   s.close();
                }catch (Exception e2) {
                   e2.printStackTrace();
                   System.exit(0);
                }
             }else {
                System.exit(0);
             }
            }
        }
     
     private void stopClient() {
           try {
              oos.writeObject(data);
              oos.flush();
              if(!s.isClosed()) {
                   s.close();
               }
               ois.close();
               oos.close();
               
               System.out.println("�ֹ� ����.");
               System.exit(0);
                }catch (Exception e) {
                   System.exit(0);
                }
        }
     
     //������ �����ϸ�, �����κ��� ���̺���� �ֹ��� ���޹޴´�.
     public void kitchenConnect() {
        Thread thread = new Thread(new Runnable() {
             @Override
             public void run() {
                while(true) {
                   try {
                      data = (Data)ois.readObject();
                      if (data.getStatus().equals("�ֹ�")) {
                         //�������忡 �޴����� �߰�
                         ordertoBoard(data);
                         //�������� ����Ʈ�� tableViewOl����
                         orderBoardlv.setItems(tableViewOl);
                         orderBoardlv.refresh();
                         //���̺� �� �ʱ�ȭ
                         orderTableSettingg(data.getTableNo());
                     }
                      
                   } catch (Exception e) {
                      if(!s.isClosed()) {
                            try {
                                ois.close();
                                oos.close();
                                s.close();
                             }catch (Exception e2) {
                                e2.printStackTrace();
                                System.exit(0);
                             }
                          }else {
                             System.exit(0);
                          }
                   }   
                }
                
             }
          });
        thread.start();
     }
     
      @SuppressWarnings("unchecked")
      private void ordertoBoard(Data data) {
            try {
               //���� �� ���̺� �� fxml 
               AnchorPane node = FXMLLoader.load(getClass().getResource("../fxml/OrderMenu.fxml"));
               Label fxtableNum = (Label)node.lookup("#time");
               Button orderCom = (Button)node.lookup("#orderCom");
               kitchenTableview = (TableView<OrderBoardMenu>)node.lookup("#kitchenTableview");
               //�ֹ� ������ �˸���
               MakeSound.kitchenOrderSound();
               
               //�ֹ����� �����ִ� ���̺� �ֹ��ð�
               fxtableNum.setText(data.getTime());
               //Ȯ�ι�ư �׼�
               
               ObservableList<OrderBoardMenu> menuToTable = FXCollections.observableArrayList(); 
               
               for(MenuData m : data.getOm_list()) {
                  OrderBoardMenu obm = new OrderBoardMenu(m.getName(), m.getCnt()+"");
                  menuToTable.add(obm);
               }
               //��ư���� ī��Ʈ�� �ش�.
               orderCom.setId(orderCom.getId() + cnt++);
               
               kitchenTableview.setItems(menuToTable);
               nodeList.add(node);
               
               Platform.runLater(()->addNode(node));
               
               orderCom.setOnAction(e -> orderComAction(e));
               
                  }catch (Exception e) {
                     e.printStackTrace();
                  }
      }
      //���̺� �� ������ ���� ����
         private void orderTableSettingg(String num) {
             TableColumn<OrderBoardMenu, ?> a = kitchenTableview.getColumns().get(0);
              a.setCellValueFactory(new PropertyValueFactory<>("menuName"));
              a.setText("���̺� ��ȣ : ");
              
              TableColumn<OrderBoardMenu, ?> b = kitchenTableview.getColumns().get(1);
              b.setCellValueFactory(new PropertyValueFactory<>("menuCnt"));
              b.setText(num);
         }
         
         //�Ϸ��ư ������ �� ���̺� ����
         private void orderComAction(ActionEvent event) {
            //����Ʈ���� hbox�� �θ���
            for(HBox hbox : tableViewOl) {
               //hbox�� node ���� �ϳ��� ��� ��ư�� �̸��� Ȯ��
               for(int i=0; i<hbox.getChildren().size(); i++) {
                  AnchorPane ap = (AnchorPane)hbox.getChildren().get(i);
                  Button button = (Button)ap.getChildren().get(2);
                  //������ �ش� node ����
                  if(event.getTarget().toString().indexOf(button.getId()) != -1) {
                     //���õ� ���(ap)�� ����Ʈ���� �����.
                     nodeList.remove(ap);
                     tableViewOl.removeAll(tableViewOl);
                     for(AnchorPane tmpAp : nodeList) {
                        Platform.runLater(()->addNode(tmpAp)); 
                     }
                     return;
                  }
                  System.out.println();
               }
            }
         }
         //fxml�� ���� ��带 ����Ʈ�信 ������ HBox�ȿ� �ִ´�. 
        private void addNode(AnchorPane node) {
           if(tableViewOl.size() == 0) {
                  HBox hbox = new HBox();
                  hbox.setSpacing(10);
                  hbox.getChildren().add(node);
                  tableViewOl.add(hbox);
               }else if(tableViewOl.get(tableViewOl.size()-1).getChildren().size() % 4 == 0 ) {
                  HBox hbox = new HBox();
                  hbox.setSpacing(10);
                  hbox.getChildren().add(node);
                  tableViewOl.add(hbox);
               }else {
                  tableViewOl.get(tableViewOl.size()-1).getChildren().add(node);
               }
        }
}