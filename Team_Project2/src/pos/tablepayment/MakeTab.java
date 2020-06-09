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
         //탭이 한개도 없을 떄
         if(tp.getTabs().size() == 0) {
             //탭을 새로만든다.(메뉴의 종류 ex)샐러드,파스타..)
             tab = new Tab(m.getCategory());
             //탭 안에 들어갈 플로우페인
             fp = new FlowPane(5D,0);
             //플로우페인 안에 들어갈 버튼
             sp = node(m.getName(), m.getPrice());
             fp.getChildren().add(sp);
             tab.setContent(fp);
             tp.getTabs().add(tab);
         }else {
            //탭이 하나이상일 때 탭의 제목과 메뉴의 카테고리를 비교
            for(Tab t : tp.getTabs()) {
               if(t.getText().equals(m.getCategory())) {
                  fp = (FlowPane)t.getContent();
                  sp = node(m.getName(), m.getPrice());
                  fp.getChildren().add(sp);
                  //있으면 넣고 flag = true
                  flag = true;
                  break;
               }
            }
            //만약 지금 메뉴가 들어가서 flag가 true이면 실행하지않는 부분
            //메뉴의 카테고리와 탭의 제목이 안맞아서 못들어간 메뉴가 있으면
            //새로운 탭 생성
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
   
   //Flowpane안에 들어갈 버튼들 각 메뉴의 이름과 가격을 가지고 생성한다.
   private StackPane node(String name, int price) {
         StackPane node = null;
         try {
            node = FXMLLoader.load(getClass().getResource("TablePaymentMenuBtn.fxml"));
            Button PaymentMenuBtn = (Button)node.lookup("#PaymentMenuBtn");
            node.setMargin(PaymentMenuBtn, new Insets(5,0,0,0));
            PaymentMenuBtn.setText(name);
            Label PaymentMenuPrice = (Label)node.lookup("#PaymentMenuPrice");
            PaymentMenuPrice.setText(price+"");
            
            //각 버튼의 액션을 정해준 뒤 넣는 순서로 한다.
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
      
      //TablePaymentController로 부터 클라이언트 정보를 넘겨받는다.(serverController의 
      //Tablet 메서드를 사용하기 위해)
      public void setOrderListAndTable(TableView<MenuData> tv, ObservableList<MenuData> obb, Tablet t) {
         this.tv = tv;
         this.ol = obb;
         this.tablet = t;
      }
      
      private void nodeAction(ActionEvent event, String Name) {
         flag = false;
         for(MenuData m : menuList) {
            System.out.println("@@메뉴리스트 메뉴: "+m.getName() + ", " + m.getCnt());
         }
         
         //총 메뉴 리스트에서
         for(MenuData m : menuList) {
            //내가 클릭한 버튼의 이름과 같은 메뉴가 있는지 찾는다.(유효성 검사)
            if(m.getName().equals(Name)) {
               //클라이언트의 오더메뉴리스트가 비었을 때는 새로운 오더메뉴를 만들어서 리스트에 추가한다.
               if(tablet.om_list.size() == 0) {
                  MenuData orderMenu = m;
                  orderMenu.setCnt(1);
                  System.out.println("@@@@오더메뉴 겟 카운트: " + orderMenu.getCnt());
                  tablet.om_list.add(orderMenu);
                  ol.add(orderMenu);
                  addOrderBoardList(orderMenu);
                  System.out.println("요기요기");
                  flag = true;
                  return;
               }else {
                  System.out.println("여기여기");
                  //비어있지 않을 때는 내가 클릭한 버튼의 이름과 같은게 있는지 찾는다.
                  for(MenuData om : tablet.om_list) {
                     //있으면 그 오더메뉴의 개수 +1
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
                  //다 찾아봤는데 없을 때는 새로운 오더메뉴를 만들어 넣는다.
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
      //주방으로 보낼 리스트에 메뉴 추가
      public void addOrderBoardList(MenuData orderMenu) {
         boolean kitchenFlag = false;
         //주방으로 보낼 리스트 목록 검사
         for(MenuData om : toOrderBoard) {
            //이미 주문한 메뉴인 경우
            if(om.getName().equals(orderMenu.getName())) {
               om.setCnt(om.getCnt()+1);
//               for(MenuData dd : toOrderBoard) {
//                  System.out.println(dd.getName()+","+ dd.getCnt());
//               }
               kitchenFlag = true;
               return;
            }
         }
         
         //새로 추가한 메뉴인 경우
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