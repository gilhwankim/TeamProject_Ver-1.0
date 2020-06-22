package pos.javafile;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import com.jfoenix.controls.JFXListView;

import data.Data;
import data.MenuData;
import data.TableData;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import pos.PosMain;
import pos.tablepayment.TablePaymentController;

public class PosController implements Initializable{
   
   private Stage posStage;
   private Tablet kitchen = null; //주방 클라이언트
   
   private ServerSocket ss;
   private Socket s;
   private ExecutorService threadPool;
   private List<Tablet> tablet_list = new ArrayList<>();   //테이블 소켓,클래스관리
   private List<TableData> tables = new ArrayList<TableData>();
   private @FXML BorderPane bp;
   private @FXML GridPane gp;
   private @FXML Button home;
   private @FXML Button menuSetting;
   private @FXML Button receipt;
   private @FXML Button posSetting;
   private @FXML Button salesHistory;
   
   private boolean posSet;
   
   private DAO dao = DAO.getinstance();
   private SeatData seatData;
   
   private MakeNode makeNode = new MakeNode();
   private MenuController mc = new MenuController(PosMain.posStage);
   TablePaymentController tpc = new TablePaymentController(mc.m_list);
   
   private Pattern p = Pattern.compile("^[0-9]*$");   //숫자만
   private Matcher m;
   
   
   
   @Override
   public void initialize(URL location, ResourceBundle resources) {
      this.posStage = PosMain.posStage;

      //시작할 때 db에서 좌석정보 받아오기
      seatData = (SeatData)dao.load("좌석");
      
      //좌석 적용(데이터베이스에 정보가있으면)
      if(seatData != null) {
         tables = seatData.getTables();
         setGridPane(seatData.getCol(), seatData.getRow());
         tablet_list.clear();
         for(int i=0; i<tables.size(); i++) {
            Tablet t = new Tablet(); 
            t.TableNo = tables.get(i).getTableId();
            t.om_list = tables.get(i).getOm_list();
            tablet_list.add(t);
         }
      }else {
         seatData = new SeatData();
      }
      
      //포스기 끌때 이벤트
      this.posStage.setOnCloseRequest(e -> {
         save();
         stopPos();
      });
      
      //판매현황 
      salesHistory.setOnAction(e->{
    	  
    	  try {
			Parent parent = FXMLLoader.load(getClass().getResource("../fxml/SalesStatus.fxml"));
			bp.setCenter(parent);
		} catch (Exception e2) {
			e2.printStackTrace();
		}
      });
      
      //pos 화면 전환 여기
      menuSetting.setOnAction( e -> bp.setCenter(mc.get()));
      home.setOnAction( e -> {
         bp.setCenter(gp);
         posSet = false;
      });
      receipt.setOnAction(e ->{
        try {
         Parent p =  FXMLLoader.load(getClass().getResource("../management/Receipt.fxml"));
         bp.setCenter(p);
      } catch (IOException e1) {}
      });
      
      //포스기 테이블 관리 클릭
      posSetting.setOnAction(e-> {
         bp.setCenter(posSetting());
         posSet = true;
      });
    
      //서버 열기
      startPos();
   }
   
   //데이터베이스에서 받아온 테이블 정보를 gridpane(gp)에 적용
   private void setGridPane(int c, int r) {
      colAndRow(c, r);
      int m = 0;
      for(int i=0; i<r; i++) {
         for(int j=0; j<c; j++) {
            if(m >= tables.size()) {
               VBox v = tableNode("기본", false, new ArrayList<MenuData>(), "0");
               gp.add(v, j, i);
               int idx = gp.getChildren().indexOf(v);
               v.setId("기본" + idx);
            }else {
               TableData td = tables.get(m);
               td.setColor("0xff0000ff");   //빨간불 초기화
               VBox v = tableNode(td.getTableId(), td.isDisable(), td.getOm_list(), td.getTotal());
               gp.add(v, j, i);
            }
            m++;
         }
      }
   }
   
   //그리드페인의 각 테이블에 관한 셋팅
   //테이블id, 활성화/비활성화 여부, 주문한 메뉴정보, 총 금액 정보
   @SuppressWarnings("unchecked")
   private VBox tableNode(String id,  Boolean d, List<MenuData> list, String total) {
      VBox v = makeNode.make();
      VBox vb = (VBox)v.lookup("#vb");
      TextField tf = (TextField)v.lookup("#tf");
      Circle c = (Circle)v.lookup("#circle");
      JFXListView<HBox> lv = (JFXListView<HBox>)v.lookup("#lv");
      ObservableList<HBox> lv_ol = lv.getItems();
      Label price = (Label)v.lookup("#price");
      
      //아이디가 없거나 ""인 테이블은 id가 항상 "기본"이다.
      v.setId(id);
      
      if(id.indexOf("기본") != -1)
         tf.setText("");
      else
         tf.setText(id);
      
      c.setFill(Color.web("0xff0000ff"));
      
      //활성화/비활성화 여부
      vb.setDisable(d);
      if(d)
         vb.setOpacity(0.0);
      else
         vb.setOpacity(10.0);
      
      //주문 메뉴들을 불러와 넣는다.
      if(list != null) {
         for(MenuData om : list) {
            if(om.getName() != null) {
               HBox h = makeNode.menuMake(om.getName(), om.getCnt());
               Platform.runLater( () -> lv_ol.add(h));
            }
         }
      }
      price.setText(total);
      
      lv.setOnMouseClicked(e->{
         if(e.getButton()==MouseButton.PRIMARY&&e.getClickCount()==1) {
               System.out.println("제발요");
               //번호,구매리스트 보내기
                  for(Tablet t : tablet_list) {
                     if(t.TableNo.equals(v.getId())) {
                        tpc.show(t.TableNo, t);      
                     }
                  }
         }   
         
      });
      
      //우클릭으로 활성화/비활성화
      v.setOnMouseClicked( e -> {
         //홈화면이면 변경 불가능
         if(posSet == false) {
            return;
         }
         if(e.getClickCount() == 1 && e.getButton() == MouseButton.SECONDARY) {
            //빨간불일때만 비활성화 가능
            if(c.getFill().equals(Color.web("0xff0000ff"))) {
               int idx = gp.getChildren().indexOf(v);
               if(!vb.isDisable()) {
                  vb.setDisable(true);
                  tables.get(idx).setDisable(true);
                  vb.setOpacity(0.0);
                  tf.clear();
               }
               else {
                  vb.setDisable(false);
                  tables.get(idx).setDisable(false);
                  vb.setOpacity(10.0);
               }
               save();
            }//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
         }
       });
      
      //더블클릭 시 테이블번호 설정(테이블번호는 테이블이 빨간불일때만 변경할 수 있다.)
      tf.setOnMouseClicked( e -> {
       //홈화면이면 변경 불가능
         if(posSet == false) {
            return;
         }
         if(e.getClickCount() == 2) {
            if(c.getFill().equals(Color.web("0xff0000ff"))) {
               tf.setEditable(true);
            }
         }
      });
      
      
      tf.setOnKeyReleased( e -> {
         if(tf.isFocused()) {
            if(e.getCode() == KeyCode.ENTER) {
               tf.setEditable(false);
            }
         }
      });
      tf.focusedProperty().addListener( (ob, oldB, newB) -> {
         if(!newB) {
            tf.setEditable(false);
         }
      });
      tf.textProperty().addListener( (ob, oldS, newS) -> {
         m = p.matcher(newS);
         if(!m.find())
            tf.setText(oldS);
         
         int idx = gp.getChildren().indexOf(v);
         if(tf.getText().equals("")) {
            v.setId("기본" + idx);
            tables.get(idx).setTableId("기본" + idx);
            tables.get(idx).setTableNo("기본" + idx);
            tablet_list.get(idx).TableNo = "기본" + idx;
         }else {
            v.setId(tf.getText());
            tables.get(idx).setTableId(tf.getText());
            tables.get(idx).setTableNo(tf.getText());
            tablet_list.get(idx).TableNo = tf.getText();
         }
         save();
      });
      return v;
   }
   
   private void save() {
      int oC = gp.getColumnConstraints().size();
      int oR = gp.getRowConstraints().size();
      seatData.setRow(oC);
      seatData.setCol(oR);
      seatData.setTables(tables);
      dao.save("좌석", seatData);
   }
   
   //포스기 테이블 설정
   private BorderPane posSetting() {
      try {
      BorderPane bp = FXMLLoader.load(getClass().getResource("../fxml/posSetting.fxml"));
      TextField row = (TextField)bp.lookup("#row");
      TextField col = (TextField)bp.lookup("#col");
      Button apply = (Button)bp.lookup("#apply");
      Button reset = (Button)bp.lookup("#reset");
      bp.setCenter(gp);
      
       //적용 버튼
      apply.setOnAction( e -> {
         int c =  Integer.parseInt(col.getText());
         int r = Integer.parseInt(row.getText());
         int oC = gp.getColumnConstraints().size();
         int oR = gp.getRowConstraints().size();
         //현재 가로세로와 입력 가로세로가 같을때는 리턴
         if(oC == c && oR == r) {
            return;
         }else {
            //새로 설정한 가로,세로 길이 만큼 다시 테이블 정보 설정
            for(int i=0; i<c; i++) {
                for(int j=0; j<r; j++) {
                   VBox v = tableNode("기본", false, new ArrayList<MenuData>(), "0");
                   gp.add(v, j, i);
                   int idx = gp.getChildren().indexOf(v);
                   v.setId("기본" + idx);
                   TableData td = new TableData();
                   td.setTableId(v.getId());
                   td.setTableNo(null);
                   td.setOm_list(new ArrayList<MenuData>());
                   td.setDisable(false);
                   td.setColor("0xff0000ff");
                   td.setTotal("0");
                   tables.add(td);
                }
             }
            setGridPane(c, r);
            save();
            tablet_list.clear();
            for(int i=0; i<tables.size(); i++) {
               Tablet t = new Tablet(); 
               t.TableNo = tables.get(i).getTableId();
               t.om_list = tables.get(i).getOm_list();
               tablet_list.add(t);
            }
            for(MenuData m : mc.m_list) {
               m.setCnt(0);
            }
         }
      });
      //행,열 텍스트 감시(10이 최대)
      col.textProperty().addListener( (ob, oldS, newS) -> {
         if(Integer.parseInt(newS) > 10) {            
            col.setText(oldS);
         }
      });
      row.textProperty().addListener( (ob, oldS, newS) -> {
         if(Integer.parseInt(newS) > 10) {            
            row.setText(oldS);
         }
      });
      
      //초기화버튼 (완성하고 지울것)
      reset.setOnAction( e -> {
         tables.clear();
         colAndRow(4, 4);
         for(int i=0; i<4; i++) {
            for(int j=0; j<4; j++) {
               VBox v = tableNode("기본", false, new ArrayList<MenuData>(), "0");
               gp.add(v, j, i);
               int idx = gp.getChildren().indexOf(v);
               v.setId("기본" + idx);
               TableData td = new TableData();
               td.setTableId(v.getId());
               td.setTableNo(null);
               td.setOm_list(new ArrayList<MenuData>());
               td.setDisable(false);
               td.setColor("0xff0000ff");
               td.setTotal("0");
               tables.add(td);
            }
         }
         save();
         tablet_list.clear();
         for(int i=0; i<tables.size(); i++) {
            Tablet t = new Tablet(); 
            t.TableNo = tables.get(i).getTableId();
            t.om_list = tables.get(i).getOm_list();
            tablet_list.add(t);
         }
         for(MenuData m : mc.m_list) {
            m.setCnt(0);
         }
      });
      
      return bp;
   } catch (Exception e) { e.printStackTrace(); }
   return bp;
   }
   
   //가로세로 비교해서 입력된 행,열에 맞게 만들기
   private void colAndRow(int c, int r) {
      int oC = gp.getColumnConstraints().size();
      int oR = gp.getRowConstraints().size();
      
      gp.getChildren().clear();
      if(oC < c) {
         for(int i=0; i<c-oC; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setHgrow(Priority.ALWAYS);
            cc.setFillWidth(true);
            gp.getColumnConstraints().add(cc);
         }
      }else {
         for(int i=oC-1; i>=c; i--) {
            gp.getColumnConstraints().remove(i);
         }
      }
      if(oR < r) {
         for(int i=0; i<r-oR; i++) {
            RowConstraints rc = new RowConstraints();
            rc.setVgrow(Priority.ALWAYS);
            rc.setFillHeight(true);
            gp.getRowConstraints().add(rc);
         }
      }else {
         for(int i=oR-1; i>=r; i--) {
            gp.getRowConstraints().remove(i);
         }
      }
   }
   
   private void startPos() {
        try {
//            ip = InetAddress.getLocalHost();   //현재 컴퓨터 아이피 받아오기
            threadPool = Executors.newFixedThreadPool(10);
            if(ss == null) {
               ss = new ServerSocket();
//               ss.bind(new InetSocketAddress(ip.getHostAddress(), 5555));
               ss.bind(new InetSocketAddress("localhost", 8888));
               System.out.println("서버 시작");
            }
         } catch (Exception e) {
            stopPos();
         }
         //서버 시작 된 후 클라이언트의 연결을 기다린다.
         if(ss != null) {
            Runnable runnable = new Runnable() {
               @Override
               public void run() {
                  while(true) {
                     try {
                        s = ss.accept();
                        Tablet tablet = new Tablet(s);
                        tablet.welcome();
                     } catch (Exception e) {
                        e.printStackTrace();
                     }
                  }
               }
            };
            threadPool.execute(runnable);
         }
   }
   
   private void stopPos() {
         try {
            if(s != null && !s.isClosed()) {
               s.close();
            }
            if(threadPool != null && !threadPool.isShutdown()) {
               threadPool.shutdown();
            }
            Platform.exit();
            System.exit(0);
         }catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
         }
   }
   
   public class Tablet{
      public String TableNo;
      private Socket s;
      private ObjectInputStream ois;
      private ObjectOutputStream oos;
      
      public List<MenuData> om_list = new ArrayList<>();
      
      private Data data;
      
      public Tablet() {
      }
      
      public Tablet(Socket s) {
         this.s = s;
      }
      
      private void welcome() {
         try {
            ois = new ObjectInputStream(s.getInputStream());
            oos = new ObjectOutputStream(s.getOutputStream());
            listen();
         } catch (Exception e) {
            e.printStackTrace();
            
         }
      }
      
      //이 테블릿만 들을수 있음
      private void send(Data data) {
         if(data == null)
            return;
         try {
           oos.reset();
            oos.writeObject(data);
            oos.flush();
         } catch (Exception e) {
            e.printStackTrace();
         }
         
      }
      private void listen() {
         Runnable runnable = new Runnable() {
            @Override
            public void run() {
               while(true) {
                  try {
                     data = (Data)ois.readObject();
                     msgProcess(data);
                  } catch (Exception e) {
                     tabletOut();
                     break;
                  }
               }
            }
         };
         threadPool.execute(runnable);
      }
      
      private void msgProcess(Data data) {
         System.out.println(data.getStatus());
         
         if (data.getStatus().equals("주방")) {
         kitchen = this;
         send(data);
      }
         if(data.getStatus().equals("안녕") || data.getStatus().equals("좌석새로고침")) {
            //현재 설정한 좌석 번호들을 넘겨주기
            List<String> no_list = new ArrayList<>();
            for(TableData td : tables) {
              if(!td.isDisable() && td.getColor().equals("0xff0000ff")) {
                 String tmp = td.getTableId();
                 if(tmp.indexOf("기본") == -1)
                    no_list.add(tmp);
              }
            }
            data.setNo_list(no_list);
            send(data);
            return;
         }else if(data.getStatus().equals("번호정했다")) {
            for(TableData td : tables) {
               if(td.getTableId().equals(data.getTableNo())) {
                  //빨간불, 초록불 체크
                  if(td.getColor().equals("0xff0000ff")) {
                     this.TableNo = data.getTableNo();
                     this.om_list = td.getOm_list();
                     System.out.println(TableNo + "번 들어옵니다.");
                     data.setStatus("들어와");
                     //메뉴리스트 보내주기
                     data.setM_list(mc.m_list);
                     send(data);
                  
                     //초록불 만들기
                     td.setColor("0x00ff00ff");
                     boolean f = false;
                     for(Tablet t : tablet_list) {
                        if(t.TableNo.equals(this.TableNo)){
                           int idx = tablet_list.indexOf(t);
                           tablet_list.remove(t);
                           tablet_list.add(idx, this);
                           f = true;
                           break;
                        }
                     }
                     if(!f) {
                        Tablet t = tablet_list.get(Integer.parseInt(this.TableNo));
                        tablet_list.remove(t);
                        tablet_list.add(Integer.parseInt(this.TableNo), this);
                     }
//                     //태블릿 리스트에 자신추가
//                     tablet_list.add(this);
                     sinho();
                     break;
                  }else {
                     data.setStatus("들어오지마");
                     send(data);
                  }
               }
            }
         
         }else if(data.getStatus().equals("주문")) {
            if(this.om_list.size() == 0) {
               this.om_list = data.getOm_list();
               
               if(kitchen != null) {
                   sendOrderInfo(data);
                }
            }else {
               if(kitchen != null) {
                  sendOrderInfo(data);
               }
               boolean flag = false;
               for(MenuData om1 : data.getOm_list()) {
                  for(MenuData om2 : this.om_list) {
                     if(om1.getName().equals(om2.getName())) {
                        om2.setCnt(om2.getCnt() + om1.getCnt());
                        flag = true;
                        break;
                     }
                  }
                  if(!flag) {
                     this.om_list.add(om1);
                  }
                  else {
                     flag = false;
                  }
               }
            }
            makeNode(data.getTableNo());
            
         }else if(data.getStatus().equals("계산서요청")) {
             data.setOm_list(this.om_list);  
             data.setStatus("계산서확인");
              send(data);
          }//테블릿에서 직원호출을 할 때
         else if(data.getStatus().equals("직원호출")) { 
             for(int i=0; i<tables.size(); i++) {
                //해당하는 테이블 찾음
                VBox vbox = (VBox) gp.getChildren().get(i);
                if(vbox.getId().equals(data.getTableNo())) {
                   vbox.setStyle("-fx-border-color:red");
                }
             }
             JOptionPane.showMessageDialog(null,"호출.");
         }
         save();
      }
      
      public void makeNode(String no) {
        tpc.priceUpdate();
         try {
            int total = 0;
            for(int i=0; i<gp.getChildren().size(); i++) {
               VBox v = (VBox)gp.getChildren().get(i);
               if(v.getId().equals(no)) {
                  @SuppressWarnings("unchecked")
                  JFXListView<HBox> lv = (JFXListView<HBox>)v.lookup("#lv");
                  Label price = (Label)v.lookup("#price");
                  
                  ObservableList<HBox> lv_ol = lv.getItems();
                  Platform.runLater( () -> lv_ol.clear());
                  
                  for(MenuData om : om_list) {
                     System.out.println("여기");
                     System.out.println(om.getName());
                     HBox hbox = makeNode.menuMake(om.getName(), om.getCnt());
                     Platform.runLater( () -> lv_ol.add(hbox));
                     total += om.getTotal();
                  }
                  lv.refresh();
                  int t = total;
                  Platform.runLater( () -> price.setText(t + ""));
                  break;
               }
            }
            //현재 테이블의 메뉴정보와 총 금액을 저장
            for(TableData td : tables) {
               if(td.getTableId().equals(no)) {
                  td.setOm_list(this.om_list);
                  td.setTotal(total + "");
                  break;
               }
            }
            
         }catch (Exception e) {
            e.printStackTrace();
         }
      }
      
      private void sinho() {
         for(int i=0; i<gp.getChildren().size(); i++) {
            VBox v = (VBox)gp.getChildren().get(i);
            Circle c = (Circle)v.lookup("#circle");
            TableData td = tables.get(i);
            c.setFill(Color.web(td.getColor()));
         }
      }
      
      private void tabletOut() {
         for(TableData td : tables) {
            try {
               if(td.getTableId().equals(TableNo)) {
                  //빨간불, 초록불 체크
                  if(td.getColor().equals("0x00ff00ff")) {
                     System.out.println(TableNo + "번 나갑니다.");
                     td.setColor("0xff0000ff");
                     sinho();
                     save();
                     break;
                  }
               }
            //tableNo가 null일 때
            }catch (Exception e) {
               e.printStackTrace();
               break;
            }
         }
         for(Tablet t : tablet_list) {
            if(t.TableNo.equals(TableNo)) {
               int idx = tablet_list.indexOf(t);
               Tablet t1 = new Tablet();
               t1 = t;
               tablet_list.remove(t);
               tablet_list.add(idx, t1);
               break;
            }
         }
      }

      //테이블 번호, 주문내역전송
      public void sendOrderInfo(Data data) {
         if(data.getOm_list().size()==0) {
            System.out.println("주문하실 메뉴를 선택해주세요");
            return;
         }
         Date time = new Date();
         SimpleDateFormat format = new SimpleDateFormat("hh시mm분ss초");
         String nowTime = format.format(time);
         //주방으로 메뉴 보냄
         try {
            data.setTime(nowTime);
            //reset을 하고 보내줘야 바뀐 값으로 넘겨받는다.
            //그냥 보내면 처음에 저장된 값으로만 받는다.
            kitchen.oos.reset();
            kitchen.oos.writeObject(data);
            kitchen.oos.flush();
         } catch (Exception e) {
            e.printStackTrace();
         }
      }
      //결제 버튼 누르면 결제 승인 후 테이블 주문 목록 초기화
      public void deleteTableinfo() {
         tpc.close();
         //DB에 올라가는 테이블 주문 정보
         for(TableData td : tables) {
            if(this.TableNo.equals(td.getTableNo())) {
               td.setOm_list(new ArrayList<MenuData>());
               td.setTotal("0");
               break;
            }
         }
         //포스기 테이블 화면 설정(초기화)
         for(int i=0; i<gp.getChildren().size(); i++) {
            VBox v = (VBox) gp.getChildren().get(i);
            if(v.getId().equals(this.TableNo)) {
               JFXListView<HBox> lv = (JFXListView<HBox>)v.lookup("#lv");
                 ObservableList<HBox> lv_ol = lv.getItems();
                 Label price = (Label)v.lookup("#price");
                 lv_ol.clear();
                 price.setText("0");
                 break;
            }
            
         }
         //포스기가 가지고 있는 주문 목록
         this.om_list = new ArrayList<MenuData>();
         System.out.println("@@@@@@@@@@결제완료"+this.TableNo+"번");
         //DB에 초기화된 데이터 연동
         save();
      }
   }
}