package pos.tablepayment;

import java.util.Iterator;
import java.util.List;

import data.MenuData;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import pos.javafile.PosController.Tablet;

public class TablePaymentController  {
   
   private Stage stage;            //TablePayment â �������� 
   private List<MenuData> menu_list;   //�������� �Ѿ���� ��ü �޴� ����Ʈ
   
   private ObservableList<MenuData> obb = FXCollections.observableArrayList();
   private TableView<MenuData> tableView;   //TablePayment â�� ���̺� ��
   private Label payTotal;               //�հ� �ݾ�
   private Label tNo; //�ش� ���̺� ��ȣ
   
   private Tablet t;                  //�� ���̺��� �Ѿ���� Ŭ���̾�Ʈ
   
   private TabPane tp;
   
   private  MakeTab mt;
   private Payment p; //����ȭ��
   
   //���� ���� ����� TablePaymentController������ ȣ���ϰ� �ʱ�ȭ�Ѵ�.
   @SuppressWarnings("unchecked")
   public TablePaymentController(List<MenuData> menu) {
     //������ ȣ��� �������� �� �޴� ����Ʈ�� �޾ƿ´�.
      this.menu_list = menu;
      
      //TablePayment.fxml ���� �θ� ��尡 Hbox �̹Ƿ� hbox�� �޾��ش�.
      HBox hbox = null;
      try {
         stage = new Stage();
         hbox = FXMLLoader.load(getClass().getResource("TablePayment.fxml"));
         tableView = (TableView<MenuData>)hbox.lookup("#tableView");
         tp = (TabPane)hbox.lookup("#tp");
         Button plus = (Button)hbox.lookup("#plus");
         Button minus = (Button)hbox.lookup("#minus");
         Button payCash = (Button)hbox.lookup("#payCash");
         Button payCard = (Button)hbox.lookup("#payCard");
         Button order = (Button)hbox.lookup("#payOrder");
         tNo = (Label)hbox.lookup("#tableNo");
         payTotal = (Label)hbox.lookup("#payTotal");

         //���̺�� Į�� ��Ī
         TableColumn<MenuData, ?> a = tableView.getColumns().get(0);
         a.setCellValueFactory(new PropertyValueFactory<>("name"));
         
         TableColumn<MenuData, ?> b = tableView.getColumns().get(1);
         b.setCellValueFactory(new PropertyValueFactory<>("price"));
         
         TableColumn<MenuData, ?> c = tableView.getColumns().get(2);
         c.setCellValueFactory(new PropertyValueFactory<>("cnt"));
         
         //OrderMenuData.java ������ getTotal() �� getPrice * getCnt ���־
         //total �ݾ� �θ� �� ���� �ܰ� * ���� ����Ͽ��� �޴´�.
         TableColumn<MenuData, ?> d = tableView.getColumns().get(3);
         d.setCellValueFactory(new PropertyValueFactory<>("total"));
         
         //��ư���� ����
         plus.setOnAction( e -> plusAction(e));
         minus.setOnAction( e -> minusAction(e));
         
         //��ư ���۽� ����ȭ��
         p = new Payment();
         payCash.setOnAction((event)-> p.cashShow()); //���ݰ��� ȭ�� ��ư
         payCard.setOnAction((event)-> p.cardShow()); //ī����� ȭ�� ��ư
         
         //TabPane ����
         mt = new MakeTab();
         
         //�ֹ��ϱ� ������ �ֹ����� ���� �ֹ��� ���� ����
         order.setOnAction(e->{
            //�������(TablePayment ȭ��)���� �ֹ��� �޴� ����Ʈ
            List<MenuData> list = mt.getOrderBoardList(e);
            if(list.size()!=0) {
               //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@�ֹ濡 OrderMenuData ��ü ����@@@@@@@@@@@@@@@
               mt.listClearplz();
            }
         });
         tp = mt.make(menu_list, tp);
         
         Scene scene = new Scene(hbox);
         stage.setScene(scene);
         
      } catch (Exception e) {
         e.printStackTrace();
         
      }
   }
   //���� Ŭ���̾�Ʈ �ܿ��� show(...)�� �θ��� �º��� �º��� ���̺�並 �޴´�.
   public void show(String tableNo, Tablet tablet) {
      this.t = tablet;
      obb.clear();
      for(MenuData omd : t.om_list) {
         obb.add(omd);
      }
      //TablePayment â�� ���̺� �信 Ŭ�󸮾�Ʈ�� ���̺�並 �Է½�Ų��.
      this.tableView.setItems(obb);
      //TablePayment â�� �� �հ�ݾ� ������Ʈ
      this.priceUpdate();
      
      //TablePayment�� �ش� ���̺� ��ȣ�� �ҷ��´�.
      tNo.textProperty().bind(new SimpleStringProperty(tableNo));
      System.out.println("���̺�" + tableNo);
      
      //stage.show()
      Platform.runLater( () -> stage.show());
      mt.setOrderListAndTable(tableView, obb, t);
   }
   
   private void plusAction(ActionEvent event) {
      //���� ���̺���� �������� ������ ������ nullpointException �߻��ϴϱ� ����
      if(tableView.getItems().size() == 0)
         return;
      //�������� �־ ���þ��ϰ� ������ nullpointException
      if(tableView.getSelectionModel().getSelectedItem() == null)
         return;
      
      //���õ� �޴��� �̸��� �޾ƿ´�.
      String name = tableView.getSelectionModel().getSelectedItem().getName();
      
      //�����޴� ����Ʈ���� ���õ� �޴��� ã�´�.
      for(MenuData om : t.om_list) {
         if(om.getName().equals(name)) {
            //ã�Ƽ� ���� +1
            om.setCnt(om.getCnt() + 1);
            System.out.println(om.getCnt());
            //�� ���̺�並 ������Ʈ�Ѵ�.
            tableView.refresh();
            //pos�� �� ���̺��� �հ�ݾ� ������Ʈ
//            c.priceUpdate();@@@@@@@@@@@@@@@@@@@@@
            break;
         }
      }
   }
   
   private void minusAction(ActionEvent event) {
      if(tableView.getItems().size() == 0)
         return;
      if(tableView.getSelectionModel().getSelectedItem() == null)
         return;
      
      String name = tableView.getSelectionModel().getSelectedItem().getName();
      
      //�޴� ������ 1�϶� '-' ��ư�� ������ 0�̵ǰ� �����ؾ��ϹǷ�
      //���ܹ߻��� ����� Iterator�� ����. 
      Iterator<MenuData> it = t.om_list.iterator();
      while(it.hasNext()) {
         MenuData om = it.next();
         if(om.getName().equals(name)) {
            if(om.getCnt() > 1) {
               //������ 2 �̻��� ���� ���� -1
               om.setCnt(om.getCnt() - 1);
            }else {
               //1 ������ ���� �����Ѵ�.
               t.om_list.remove(om);
            }
            System.out.println(om.getCnt());
            
            //�� ���̺�並 ������Ʈ�Ѵ�.
            tableView.refresh();
            //pos�� �� ���̺��� �հ�ݾ� ������Ʈ
//            c.priceUpdate();@@@@@@@@@@@@@@@@@
            
            break;
         }
      }
   }
   
   //�հ�ݾ��� �ٽ� ����ؼ� ������Ʈ�Ѵ�.
   public void priceUpdate() {
     Platform.runLater( () -> {
       this.tableView.refresh();
        int total = 0;
        if(this.t != null) {
            for(MenuData om : this.t.om_list) {
               total += om.getTotal();
            }
             payTotal.setText("�ѱݾ� : " + total + "��");
        }
      });
   }
}