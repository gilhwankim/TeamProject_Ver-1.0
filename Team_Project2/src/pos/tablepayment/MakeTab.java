package pos.tablepayment;

import java.util.ArrayList;
import java.util.List;

import data.MenuData;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import pos.javafile.PosController.Tablet;

public class MakeTab {

   private Tab tab;
   private FlowPane fp;
   private StackPane sp;
   
   private boolean flag = false;
   
   private List<MenuData> menuList;
   private List<MenuData> toOrderBoard = new ArrayList<MenuData>();
   private Tablet tablet;
   private ObservableList<MenuData> ol;
   private TableView<MenuData> tv;
   
   public MakeTab() {}
    
   public TabPane make(List<MenuData> list, TabPane tabPane) {
      TabPane tp = tabPane;
      menuList = list;
      try {
      for(MenuData m : menuList) {
         //���� �Ѱ��� ���� ��
         if(tp.getTabs().size() == 0) {
             //���� ���θ����.(�޴��� ���� ex)������,�Ľ�Ÿ..)
             tab = new Tab(m.getCategory());
             //�� �ȿ� �� �÷ο�����
             fp = new FlowPane(5D,0);
             //�÷ο����� �ȿ� �� ��ư
             sp = node(m.getName(), m.getPrice());
             fp.getChildren().add(sp);
             tab.setContent(fp);
             tp.getTabs().add(tab);
         }else {
            //���� �ϳ��̻��� �� ���� ����� �޴��� ī�װ��� ��
            for(Tab t : tp.getTabs()) {
               if(t.getText().equals(m.getCategory())) {
                  fp = (FlowPane)t.getContent();
                  sp = node(m.getName(), m.getPrice());
                  fp.getChildren().add(sp);
                  //������ �ְ� flag = true
                  flag = true;
                  break;
               }
            }
            //���� ���� �޴��� ���� flag�� true�̸� ���������ʴ� �κ�
            //�޴��� ī�װ��� ���� ������ �ȸ¾Ƽ� ���� �޴��� ������
            //���ο� �� ����
            if(flag == false) {
               tab = new Tab(m.getCategory());
               fp = new FlowPane(5D, 0);
               sp = node(m.getName(), m.getPrice());
               fp.getChildren().add(sp);
               tab.setContent(fp);
               tp.getTabs().add(tab);
            }
            flag = false;
         }
      }
      return tp;
      }catch (Exception e) {
         e.printStackTrace();
      }
      return tp;
   }
   
   //Flowpane�ȿ� �� ��ư�� �� �޴��� �̸��� ������ ������ �����Ѵ�.
   private StackPane node(String name, int price) {
         StackPane node = null;
         try {
            node = FXMLLoader.load(getClass().getResource("TablePaymentMenuBtn.fxml"));
            Button PaymentMenuBtn = (Button)node.lookup("#PaymentMenuBtn");
            node.setMargin(PaymentMenuBtn, new Insets(5,0,0,0));
            PaymentMenuBtn.setText(name);
            Label PaymentMenuPrice = (Label)node.lookup("#PaymentMenuPrice");
            PaymentMenuPrice.setText(price+"");
            
            //�� ��ư�� �׼��� ������ �� �ִ� ������ �Ѵ�.
            PaymentMenuBtn.setOnAction( e -> {
               nodeAction(e, PaymentMenuBtn.getText());
               
               //@@@@@@@@@@@@priceupdate()@@@@@@@@@@@@@@@@
               
            });
            return node;
         } catch (Exception e) {
            e.printStackTrace();
         }
         return node;
      }
      
      //TablePaymentController�� ���� Ŭ���̾�Ʈ ������ �Ѱܹ޴´�.(serverController�� 
      //Tablet �޼��带 ����ϱ� ����)
      public void setOrderListAndTable(TableView<MenuData> tv, ObservableList<MenuData> obb, Tablet t) {
         this.tv = tv;
         this.ol = obb;
         this.tablet = t;
      }
      
      private void nodeAction(ActionEvent event, String Name) {
         flag = false;
         for(MenuData m : menuList) {
            System.out.println("@@�޴�����Ʈ �޴�: "+m.getName() + ", " + m.getCnt());
         }
         
         //�� �޴� ����Ʈ����
         for(MenuData m : menuList) {
            //���� Ŭ���� ��ư�� �̸��� ���� �޴��� �ִ��� ã�´�.(��ȿ�� �˻�)
            if(m.getName().equals(Name)) {
               //Ŭ���̾�Ʈ�� �����޴�����Ʈ�� ����� ���� ���ο� �����޴��� ���� ����Ʈ�� �߰��Ѵ�.
               if(tablet.om_list.size() == 0) {
                  MenuData orderMenu = m;
                  orderMenu.setCnt(1);
                  System.out.println("@@@@�����޴� �� ī��Ʈ: " + orderMenu.getCnt());
                  tablet.om_list.add(orderMenu);
                  ol.add(orderMenu);
                  addOrderBoardList(orderMenu);
                  System.out.println("�����");
                  flag = true;
                  return;
               }else {
                  System.out.println("���⿩��");
                  //������� ���� ���� ���� Ŭ���� ��ư�� �̸��� ������ �ִ��� ã�´�.
                  for(MenuData om : tablet.om_list) {
                     //������ �� �����޴��� ���� +1
                     if(Name.equals(om.getName())) {
                       addOrderBoardList(om);
                       System.out.println(om.getCnt());
                        om.setCnt(om.getCnt() + 1);
                        for(MenuData omm : ol) {
                           if(omm.getName().equals(om.getName())) {
                              omm = om;
                              tv.refresh();
                              break;
                           }
                        }
                        flag = true;
                        return;
                     }
                  }
                  //�� ã�ƺôµ� ���� ���� ���ο� �����޴��� ����� �ִ´�.
                  if(flag == false) {
                    MenuData orderMenu = m;
                    orderMenu.setCnt(1);
                     tablet.om_list.add(orderMenu);
                     ol.add(orderMenu);
                     addOrderBoardList(orderMenu);
                  }
                  flag = false;
                  return;
               }
            }
         }
      }
      //�ֹ����� ���� ����Ʈ�� �޴� �߰�
      public void addOrderBoardList(MenuData orderMenu) {
         boolean kitchenFlag = false;
         //�ֹ����� ���� ����Ʈ ��� �˻�
         for(MenuData om : toOrderBoard) {
            //�̹� �ֹ��� �޴��� ���
            if(om.getName().equals(orderMenu.getName())) {
               om.setCnt(om.getCnt()+1);
//               for(MenuData dd : toOrderBoard) {
//                  System.out.println(dd.getName()+","+ dd.getCnt());
//               }
               kitchenFlag = true;
               return;
            }
         }
         
         //���� �߰��� �޴��� ���
         if(kitchenFlag==false) {
            for(MenuData m : menuList) {
               if(orderMenu.getName().equals(m.getName())) {
                  MenuData omm = m;
                  omm.setCnt(1);
                  toOrderBoard.add(omm);
               }
            }
//            for(MenuData dd : toOrderBoard) {
//               System.out.println(dd.getName()+","+ dd.getCnt());
//            }
         }
         kitchenFlag=false;
         return;
      }
      
      public List<MenuData> getOrderBoardList(ActionEvent e){
         return toOrderBoard;
      }
      
      public void listClearplz() {
         toOrderBoard.clear(); 
      }
      
   }