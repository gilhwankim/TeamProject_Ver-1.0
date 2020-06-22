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
   private Tablet kitchen = null; //�ֹ� Ŭ���̾�Ʈ
   
   private ServerSocket ss;
   private Socket s;
   private ExecutorService threadPool;
   private List<Tablet> tablet_list = new ArrayList<>();   //���̺� ����,Ŭ��������
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
   
   private Pattern p = Pattern.compile("^[0-9]*$");   //���ڸ�
   private Matcher m;
   
   
   
   @Override
   public void initialize(URL location, ResourceBundle resources) {
      this.posStage = PosMain.posStage;

      //������ �� db���� �¼����� �޾ƿ���
      seatData = (SeatData)dao.load("�¼�");
      
      //�¼� ����(�����ͺ��̽��� ������������)
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
      
      //������ ���� �̺�Ʈ
      this.posStage.setOnCloseRequest(e -> {
         save();
         stopPos();
      });
      
      //�Ǹ���Ȳ 
      salesHistory.setOnAction(e->{
    	  
    	  try {
			Parent parent = FXMLLoader.load(getClass().getResource("../fxml/SalesStatus.fxml"));
			bp.setCenter(parent);
		} catch (Exception e2) {
			e2.printStackTrace();
		}
      });
      
      //pos ȭ�� ��ȯ ����
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
      
      //������ ���̺� ���� Ŭ��
      posSetting.setOnAction(e-> {
         bp.setCenter(posSetting());
         posSet = true;
      });
    
      //���� ����
      startPos();
   }
   
   //�����ͺ��̽����� �޾ƿ� ���̺� ������ gridpane(gp)�� ����
   private void setGridPane(int c, int r) {
      colAndRow(c, r);
      int m = 0;
      for(int i=0; i<r; i++) {
         for(int j=0; j<c; j++) {
            if(m >= tables.size()) {
               VBox v = tableNode("�⺻", false, new ArrayList<MenuData>(), "0");
               gp.add(v, j, i);
               int idx = gp.getChildren().indexOf(v);
               v.setId("�⺻" + idx);
            }else {
               TableData td = tables.get(m);
               td.setColor("0xff0000ff");   //������ �ʱ�ȭ
               VBox v = tableNode(td.getTableId(), td.isDisable(), td.getOm_list(), td.getTotal());
               gp.add(v, j, i);
            }
            m++;
         }
      }
   }
   
   //�׸��������� �� ���̺� ���� ����
   //���̺�id, Ȱ��ȭ/��Ȱ��ȭ ����, �ֹ��� �޴�����, �� �ݾ� ����
   @SuppressWarnings("unchecked")
   private VBox tableNode(String id,  Boolean d, List<MenuData> list, String total) {
      VBox v = makeNode.make();
      VBox vb = (VBox)v.lookup("#vb");
      TextField tf = (TextField)v.lookup("#tf");
      Circle c = (Circle)v.lookup("#circle");
      JFXListView<HBox> lv = (JFXListView<HBox>)v.lookup("#lv");
      ObservableList<HBox> lv_ol = lv.getItems();
      Label price = (Label)v.lookup("#price");
      
      //���̵� ���ų� ""�� ���̺��� id�� �׻� "�⺻"�̴�.
      v.setId(id);
      
      if(id.indexOf("�⺻") != -1)
         tf.setText("");
      else
         tf.setText(id);
      
      c.setFill(Color.web("0xff0000ff"));
      
      //Ȱ��ȭ/��Ȱ��ȭ ����
      vb.setDisable(d);
      if(d)
         vb.setOpacity(0.0);
      else
         vb.setOpacity(10.0);
      
      //�ֹ� �޴����� �ҷ��� �ִ´�.
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
               System.out.println("���߿�");
               //��ȣ,���Ÿ���Ʈ ������
                  for(Tablet t : tablet_list) {
                     if(t.TableNo.equals(v.getId())) {
                        tpc.show(t.TableNo, t);      
                     }
                  }
         }   
         
      });
      
      //��Ŭ������ Ȱ��ȭ/��Ȱ��ȭ
      v.setOnMouseClicked( e -> {
         //Ȩȭ���̸� ���� �Ұ���
         if(posSet == false) {
            return;
         }
         if(e.getClickCount() == 1 && e.getButton() == MouseButton.SECONDARY) {
            //�������϶��� ��Ȱ��ȭ ����
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
      
      //����Ŭ�� �� ���̺��ȣ ����(���̺��ȣ�� ���̺��� �������϶��� ������ �� �ִ�.)
      tf.setOnMouseClicked( e -> {
       //Ȩȭ���̸� ���� �Ұ���
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
            v.setId("�⺻" + idx);
            tables.get(idx).setTableId("�⺻" + idx);
            tables.get(idx).setTableNo("�⺻" + idx);
            tablet_list.get(idx).TableNo = "�⺻" + idx;
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
      dao.save("�¼�", seatData);
   }
   
   //������ ���̺� ����
   private BorderPane posSetting() {
      try {
      BorderPane bp = FXMLLoader.load(getClass().getResource("../fxml/posSetting.fxml"));
      TextField row = (TextField)bp.lookup("#row");
      TextField col = (TextField)bp.lookup("#col");
      Button apply = (Button)bp.lookup("#apply");
      Button reset = (Button)bp.lookup("#reset");
      bp.setCenter(gp);
      
       //���� ��ư
      apply.setOnAction( e -> {
         int c =  Integer.parseInt(col.getText());
         int r = Integer.parseInt(row.getText());
         int oC = gp.getColumnConstraints().size();
         int oR = gp.getRowConstraints().size();
         //���� ���μ��ο� �Է� ���μ��ΰ� �������� ����
         if(oC == c && oR == r) {
            return;
         }else {
            //���� ������ ����,���� ���� ��ŭ �ٽ� ���̺� ���� ����
            for(int i=0; i<c; i++) {
                for(int j=0; j<r; j++) {
                   VBox v = tableNode("�⺻", false, new ArrayList<MenuData>(), "0");
                   gp.add(v, j, i);
                   int idx = gp.getChildren().indexOf(v);
                   v.setId("�⺻" + idx);
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
      //��,�� �ؽ�Ʈ ����(10�� �ִ�)
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
      
      //�ʱ�ȭ��ư (�ϼ��ϰ� �����)
      reset.setOnAction( e -> {
         tables.clear();
         colAndRow(4, 4);
         for(int i=0; i<4; i++) {
            for(int j=0; j<4; j++) {
               VBox v = tableNode("�⺻", false, new ArrayList<MenuData>(), "0");
               gp.add(v, j, i);
               int idx = gp.getChildren().indexOf(v);
               v.setId("�⺻" + idx);
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
   
   //���μ��� ���ؼ� �Էµ� ��,���� �°� �����
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
//            ip = InetAddress.getLocalHost();   //���� ��ǻ�� ������ �޾ƿ���
            threadPool = Executors.newFixedThreadPool(10);
            if(ss == null) {
               ss = new ServerSocket();
//               ss.bind(new InetSocketAddress(ip.getHostAddress(), 5555));
               ss.bind(new InetSocketAddress("localhost", 8888));
               System.out.println("���� ����");
            }
         } catch (Exception e) {
            stopPos();
         }
         //���� ���� �� �� Ŭ���̾�Ʈ�� ������ ��ٸ���.
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
      
      //�� �׺��� ������ ����
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
         
         if (data.getStatus().equals("�ֹ�")) {
         kitchen = this;
         send(data);
      }
         if(data.getStatus().equals("�ȳ�") || data.getStatus().equals("�¼����ΰ�ħ")) {
            //���� ������ �¼� ��ȣ���� �Ѱ��ֱ�
            List<String> no_list = new ArrayList<>();
            for(TableData td : tables) {
              if(!td.isDisable() && td.getColor().equals("0xff0000ff")) {
                 String tmp = td.getTableId();
                 if(tmp.indexOf("�⺻") == -1)
                    no_list.add(tmp);
              }
            }
            data.setNo_list(no_list);
            send(data);
            return;
         }else if(data.getStatus().equals("��ȣ���ߴ�")) {
            for(TableData td : tables) {
               if(td.getTableId().equals(data.getTableNo())) {
                  //������, �ʷϺ� üũ
                  if(td.getColor().equals("0xff0000ff")) {
                     this.TableNo = data.getTableNo();
                     this.om_list = td.getOm_list();
                     System.out.println(TableNo + "�� ���ɴϴ�.");
                     data.setStatus("����");
                     //�޴�����Ʈ �����ֱ�
                     data.setM_list(mc.m_list);
                     send(data);
                  
                     //�ʷϺ� �����
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
//                     //�º� ����Ʈ�� �ڽ��߰�
//                     tablet_list.add(this);
                     sinho();
                     break;
                  }else {
                     data.setStatus("��������");
                     send(data);
                  }
               }
            }
         
         }else if(data.getStatus().equals("�ֹ�")) {
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
            
         }else if(data.getStatus().equals("��꼭��û")) {
             data.setOm_list(this.om_list);  
             data.setStatus("��꼭Ȯ��");
              send(data);
          }//�׺����� ����ȣ���� �� ��
         else if(data.getStatus().equals("����ȣ��")) { 
             for(int i=0; i<tables.size(); i++) {
                //�ش��ϴ� ���̺� ã��
                VBox vbox = (VBox) gp.getChildren().get(i);
                if(vbox.getId().equals(data.getTableNo())) {
                   vbox.setStyle("-fx-border-color:red");
                }
             }
             JOptionPane.showMessageDialog(null,"ȣ��.");
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
                     System.out.println("����");
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
            //���� ���̺��� �޴������� �� �ݾ��� ����
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
                  //������, �ʷϺ� üũ
                  if(td.getColor().equals("0x00ff00ff")) {
                     System.out.println(TableNo + "�� �����ϴ�.");
                     td.setColor("0xff0000ff");
                     sinho();
                     save();
                     break;
                  }
               }
            //tableNo�� null�� ��
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

      //���̺� ��ȣ, �ֹ���������
      public void sendOrderInfo(Data data) {
         if(data.getOm_list().size()==0) {
            System.out.println("�ֹ��Ͻ� �޴��� �������ּ���");
            return;
         }
         Date time = new Date();
         SimpleDateFormat format = new SimpleDateFormat("hh��mm��ss��");
         String nowTime = format.format(time);
         //�ֹ����� �޴� ����
         try {
            data.setTime(nowTime);
            //reset�� �ϰ� ������� �ٲ� ������ �Ѱܹ޴´�.
            //�׳� ������ ó���� ����� �����θ� �޴´�.
            kitchen.oos.reset();
            kitchen.oos.writeObject(data);
            kitchen.oos.flush();
         } catch (Exception e) {
            e.printStackTrace();
         }
      }
      //���� ��ư ������ ���� ���� �� ���̺� �ֹ� ��� �ʱ�ȭ
      public void deleteTableinfo() {
         tpc.close();
         //DB�� �ö󰡴� ���̺� �ֹ� ����
         for(TableData td : tables) {
            if(this.TableNo.equals(td.getTableNo())) {
               td.setOm_list(new ArrayList<MenuData>());
               td.setTotal("0");
               break;
            }
         }
         //������ ���̺� ȭ�� ����(�ʱ�ȭ)
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
         //�����Ⱑ ������ �ִ� �ֹ� ���
         this.om_list = new ArrayList<MenuData>();
         System.out.println("@@@@@@@@@@�����Ϸ�"+this.TableNo+"��");
         //DB�� �ʱ�ȭ�� ������ ����
         save();
      }
   }
}