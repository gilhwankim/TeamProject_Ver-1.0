package tablet.javafile;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import data.Data;
import data.MenuData;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tablet.TabletMain;

public class TabletController implements Initializable{

   private Stage tabletStage = TabletMain.tabletStage;
   
   private Socket s;
   private ObjectInputStream ois;
   private ObjectOutputStream oos;
   private Data data;

   private Stage stage;
   private FlowPane fp;
   private @FXML Button orderBtn;
   private @FXML Label total; 
   private @FXML Label tableNo; //태플릿에 테이블번호 표시 라벨
   private @FXML Button billBtn; //계산서 호출 버튼
   private @FXML Button subtractBtn; // - 버튼
   private @FXML Button plusBtn; // + 버튼
   private @FXML TabPane tp;
   private @FXML Button callBtn;
   
   private String no;
   private List<MenuData> m_list;   //메뉴 리스트
   
   private @FXML TableView<MenuData> orderTable;   //구매 테이블
   private ObservableList<MenuData> orderTableOl = FXCollections.observableArrayList();
   @Override
   public void initialize(URL location, ResourceBundle resources) {
      startTablet();
      orderTable.setItems(orderTableOl);
      plusBtn.setOnAction( e -> plusBtnAction(e));
      subtractBtn.setOnAction( e -> subtractBtnAction(e));
      orderBtn.setOnAction(e -> orderBtnAction(e));
      billBtn.setOnAction(e -> callBill(e));
      tabletStage.setOnCloseRequest( e -> stopTablet());
      callBtn.setOnAction(e -> callEmployee(e));
   }
   
   //처음 뜨는 창
   private void tableSet(List<String> list) {
    stage = new Stage();
     VBox settableNo = null;
      try {
         settableNo = FXMLLoader.load(getClass().getResource("../fxml/selectTablenum.fxml")); 
      } catch (Exception e) {
         e.printStackTrace();
      }
      Button btn = (Button)settableNo.lookup("#btn");
      fp = (FlowPane)settableNo.lookup("#fp");
      
      Scene scene = new Scene(settableNo); 
      stage.setScene(scene);
      stage.setTitle("좌석 번호 선택");
      stage.show();
      
      makeBtn(list);
      
      btn.setOnAction( e -> {
          data.setStatus("좌석새로고침");
          send(data);
      });
      
      Thread t = new Thread(new Runnable() {
      @Override
      public void run() {
         while(stage.isShowing()) {
            Data data = new Data();
            data.setStatus("좌석새로고침");
            send(data);
            try {
               Thread.sleep(1000);
               
            } catch (Exception e) {
               e.printStackTrace();
               break;
            }
         }
      }
      });
      t.start();
   }
   
   private void makeBtn(List<String> list) {
    //오름차순으로 정렬
      list.sort((a, b) -> Integer.compare(Integer.parseInt(a), Integer.parseInt(b)));

      ObservableList<Node> ol = fp.getChildren();
      ol.clear();
      
      for(String str : list) {
         Button b = new Button(str);
         b.setPrefSize(100D, 50D);
         fp.getChildren().add(b);
         
         b.setOnAction( e -> {
            if(b.getText() != null) {
               this.no = b.getText();
            }
            if(Integer.parseInt(this.no) < 10)
                this.tableNo.setText("0" + no);
             else
                this.tableNo.setText(no);
             
             data = new Data();
             data.setStatus("번호정했다");
             data.setTableNo(no);
             send(data);
             
         });
      }
   }
   
   
   private void startTablet() {
      s = new Socket();
      try {
         s.connect(new InetSocketAddress("localhost", 8888));
         oos = new ObjectOutputStream(s.getOutputStream());
         data = new Data();
         data.setStatus("안녕");
         send(data);
         ois = new ObjectInputStream(s.getInputStream());
         
         Thread t = new Thread(new Runnable() {
         @Override
         public void run() {
            while(true) {
               try {
                  data = (Data)ois.readObject();
                  msgProcess(data);
               } catch (Exception e) {
            	   System.out.println("오류 뜬다");
                  stopTablet();
               }
            }
         }
      });
        t.start();
         
      } catch (Exception e) {
         System.out.println("서버가 안열렸다~~");
         e.printStackTrace();
      }
   }
   
   private void stopTablet() {
      try {
         oos.close();
         ois.close();
         if(!this.s.isClosed()) {
            s.close();
         }
      } catch (Exception e) {
         e.printStackTrace();
      }finally {
      System.exit(0);
   }
   }
   
   private void msgProcess(Data data) {
      System.out.println(data.getStatus());
      if(data.getStatus().equals("안녕")) {
           Platform.runLater( () -> tableSet(data.getNo_list()));
       }else if(data.getStatus().equals("좌석새로고침")) {
          Platform.runLater( () -> makeBtn(data.getNo_list()));
       }else if(data.getStatus().equals("들어와")) {
           //들어오면서 메뉴리스트를 받아온다.
           this.m_list = data.getM_list();
           Platform.runLater( () -> {
              stage.close();
              showTablet();
           });
       }else if(data.getStatus().equals("계산서확인")) {
          Bill bill = new Bill();          
           for(MenuData m : data.getOm_list()) {
              System.out.println(m.getName());
           }
           Platform.runLater(()->{
              bill.show(data.getOm_list());         
           });       
       } 
   }
   
   private void showTablet() {
      for(MenuData md : m_list) {
         System.out.println(md.getName());
      }
      this.tabletStage.show();
      
      MakeTab mt = new MakeTab();
       tp = mt.make(m_list, tp);
       addMenu();
       orderTableSetting();
      
   }
   
   private void addMenu(){
      for(MenuData md : m_list) {
            try {
               //각 메뉴 아이템
               VBox node = FXMLLoader.load(getClass().getResource("../fxml/menuItem.fxml"));
               Label labelName = (Label)node.lookup("#labelName");
               Label labelPrice = (Label)node.lookup("#labelPrice");
               //menuItem.fxml에서 imageView 찾아옴
               ImageView imageMenu = (ImageView)node.lookup("#menuImg");
               
               imageMenu.setImage(new Image(new ByteArrayInputStream(md.getImage())));
               labelName.setText(md.getName());
               labelPrice.setText(md.getPrice() + "원");            
               
               node.setOnMouseClicked(e -> {
                  if(e.getClickCount() == 2) {
                     System.out.println("메뉴이름 : " + labelName.getText() + "메뉴가격 : " + labelPrice.getText());
                     addOrdertable(labelName.getText());
                  }
               });
               
               for(Tab t : tp.getTabs()) {
                  if(t.getText().equals(md.getCategory())) {
                     HBox h = (HBox)t.getContent();
                     VBox v = (VBox)h.getChildren().get(0);
                     ListView<HBox> lv = (ListView<HBox>)v.getChildren().get(0);
                     if(lv.getItems().size() == 0) {
                        HBox hbox = new HBox();
                        hbox.setSpacing(10);
                        hbox.getChildren().add(node);
                        lv.getItems().add(hbox);
                        break;
                     }else if(lv.getItems().get(lv.getItems().size() - 1).getChildren().size() % 3 == 0 ) {
                        HBox hbox = new HBox();
                        hbox.setSpacing(10);
                        hbox.getChildren().add(node);
                        lv.getItems().add(hbox);
                        break;
                     }else {
                        lv.getItems().get(lv.getItems().size()-1).getChildren().add(node);
                        break;
                     }
                  }
               }
            }catch (Exception e) {
               e.printStackTrace();
            }
      }
   }
   
   //구매 테이블에 메뉴 넣기.
   private void addOrdertable(String name) {
     MenuData md = null;
     for(MenuData m : m_list) {
        if(m.getName().equals(name)) {
           md = m;
           break;
        }
     }
     for(MenuData om : orderTableOl) {
        if(om.getName().equals(md.getName())) {
           om.setCnt(om.getCnt() + 1);
           orderTable.refresh();
           priceUpdate();
           return;
        }
     }
     MenuData om = new MenuData();
     om.setName(md.getName());
     om = md;
     om.setCnt(1);
     orderTableOl.add(om);
     orderTable.refresh();
     priceUpdate();
   }

   //'주문하기'버튼의 액션
   private void orderBtnAction(ActionEvent event) {
      if(orderTableOl.size() == 0) {
         return;
      }
      List<MenuData> om_list = new ArrayList<>();
      for(MenuData m : orderTableOl) {
//         addTableBill(m);
         om_list.add(m);
      }
      Data data = new Data();
      data.setStatus("주문");
      data.setTableNo(this.no);
      data.setOm_list(om_list);
      send(data);
      
      
      Platform.runLater(() -> orderTableOl.clear());
      priceUpdate();
      total.setText("0원");
      
   }
   
 //테이블뷰 초기화
   private void orderTableSetting() {
      TableColumn<MenuData, ?> a = orderTable.getColumns().get(0);
       a.setCellValueFactory(new PropertyValueFactory<>("name"));
       a.setText("메뉴 명");
       
       TableColumn<MenuData, ?> b = orderTable.getColumns().get(1);
       b.setCellValueFactory(new PropertyValueFactory<>("cnt"));
       b.setText("수량");
       
       TableColumn<MenuData, ?> c = orderTable.getColumns().get(2);
       c.setCellValueFactory(new PropertyValueFactory<>("total"));
       c.setText("가격");
       
   }
   
 //'-' 버튼 동작
   private void subtractBtnAction(ActionEvent event) {
      //구매목록에 하나없으면 에러나니까 리턴
      if(orderTableOl.size() == 0)
         return;
      //구매목록에 있어도 선택안하고 누르면 에러나니까 리턴
      if(orderTable.getSelectionModel().getSelectedItem() == null)
             return;
      
      String name = orderTable.getSelectionModel().getSelectedItem().getName();
      //구매목록에서 선택한 메뉴를 이름으로 찾는다.
      for(MenuData om : orderTableOl) {
         if(om.getName().equals(name)) {
            if(om.getCnt() > 1)
               om.setCnt(om.getCnt() - 1);
            else 
               orderTableOl.remove(om);
            
            orderTable.refresh();
            priceUpdate();
            break;
         }
      }
      
   }
   //'+' 버튼 동작
   private void plusBtnAction(ActionEvent event) {
      if(orderTableOl.size() == 0)
         return;
      if(orderTable.getSelectionModel().getSelectedItem() == null)
             return;
      
      String name = orderTable.getSelectionModel().getSelectedItem().getName();
      
      for(MenuData om : orderTableOl) {
         if(om.getName().equals(name)) {
            om.setCnt(om.getCnt() + 1);
            orderTable.refresh();
            priceUpdate();
         }
      }
   }
   private void priceUpdate() {
      int i = 0;
      for(MenuData om : orderTableOl) {
         i += om.getTotal();
      }
      total.setText(i + "원");
   }
   
   //계산서 서버에 요청
   private void callBill(ActionEvent event) {      
	      Data tmp = new Data();
	      tmp.setStatus("계산서요청");
	      tmp.setTableNo(no);
	      send(tmp);     
	   }
   
   
   private void callEmployee(ActionEvent event) {      
       Data tmp = new Data();
       tmp.setStatus("직원호출");
       tmp.setTableNo(no);
       send(tmp);     
    }
   private void send(Data data) {
      try {
         oos.writeObject(data);
         oos.flush();
      
      } catch (Exception e) {
         e.printStackTrace();
      }
   }
   
}