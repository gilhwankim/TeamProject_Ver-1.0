package pos.javafile;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jfoenix.controls.JFXListView;

import data.Data;
import data.OrderMenuData;
import data.TableData;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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
	
	private ServerSocket ss;
	private Socket s;
	private ExecutorService threadPool;
	private List<Tablet> tablet_list = new ArrayList<>();	//테이블 소켓,클래스관리
	private List<TableData> tables = new ArrayList<TableData>();
	private @FXML BorderPane bp;
	private @FXML GridPane gp;
	private @FXML TextField cal;
	private @FXML TextField row;
	private @FXML Button apply;
	private @FXML Button home;
	private @FXML Button menuSetting;
	private @FXML Button reset;
	
	private DAO dao = DAO.getinstance();
	private SeatData seatData;
	
	private MakeNode makeNode = new MakeNode();
	private MenuController mc = new MenuController(PosMain.posStage);
	TablePaymentController tpc = new TablePaymentController(mc.m_list);
	
	private Pattern p = Pattern.compile("^[0-9]*$");	//숫자만
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
		}else {
			seatData = new SeatData();
		}
		
		//포스기 끌때 이벤트
		this.posStage.setOnCloseRequest(e -> {
			save();
			stopPos();
		});
		
		//적용 버튼
		apply.setOnAction( e -> {
			int c =  Integer.parseInt(cal.getText());
			int r = Integer.parseInt(row.getText());
			int oC = gp.getColumnConstraints().size();
			int oR = gp.getRowConstraints().size();
			//현재 가로세로와 입력 가로세로가 같을때는 리턴
			if(oC == c && oR == r) {
				return;
			}else {
				setGridPane(c, r);
				save();
			}
		});
		//행,열 텍스트 감시(10이 최대)
		cal.textProperty().addListener( (ob, oldS, newS) -> {
			if(Integer.parseInt(newS) > 10) {				
				cal.setText(oldS);
			}
		});
		row.textProperty().addListener( (ob, oldS, newS) -> {
			if(Integer.parseInt(newS) > 10) {				
				row.setText(oldS);
			}
		});
		
		//pos 화면 전환 여기
		menuSetting.setOnAction( e -> bp.setCenter(mc.get()));
		home.setOnAction( e -> bp.setCenter(gp));
		
		//초기화버튼 (완성하고 지울것)
		reset.setOnAction( e -> {
			tables.clear();
			colAndRow(4, 4);
			for(int i=0; i<4; i++) {
				for(int j=0; j<4; j++) {
					VBox v = tableNode("기본", false, new ArrayList<OrderMenuData>(), "0");
					gp.add(v, j, i);
					TableData td = new TableData();
					td.setTableId("기본");
					td.setTableNo(null);
					td.setOm_list(new ArrayList<OrderMenuData>());
					td.setDisable(false);
					td.setColor("0xff0000ff");
					td.setTotal("0");
					tables.add(td);
				}
			}
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
					VBox v = tableNode("기본", false, new ArrayList<OrderMenuData>(), "0");
					gp.add(v, j, i);
				}else {
					TableData td = tables.get(m);
					VBox v = tableNode(td.getTableId(), td.isDisable(), td.getOm_list(), td.getTotal());
					gp.add(v, j, i);
				}
				m++;
			}
		}
		//빨간불 초기화
		for(TableData td : tables) {
	         td.setColor("0xff0000ff");
	      }
	}
	
	//그리드페인의 각 테이블에 관한 셋팅
	//테이블id, 활성화/비활성화 여부, 주문한 메뉴정보, 총 금액 정보
	@SuppressWarnings("unchecked")
	private VBox tableNode(String id,  Boolean d, List<OrderMenuData> list, String total) {
		VBox v = makeNode.make();
		VBox vb = (VBox)v.lookup("#vb");
		TextField tf = (TextField)v.lookup("#tf");
		Circle c = (Circle)v.lookup("#circle");
		JFXListView<HBox> lv = (JFXListView<HBox>)v.lookup("#lv");
		ObservableList<HBox> lv_ol = lv.getItems();
		Label price = (Label)v.lookup("#price");
		
		//아이디가 없거나 ""인 테이블은 id가 항상 "기본"이다.
		v.setId(id);
		if(id.equals("기본"))
			tf.setText("");
		else
			tf.setText(id);
		//처음 포스기 켤 땐 모든 테이블을 빨간 불로 바꾼다.
		c.setFill(Color.web("0xff0000ff"));
		
		//활성화/비활성화 여부
		vb.setDisable(d);
		if(d)
			vb.setOpacity(0.0);
		else
			vb.setOpacity(10.0);
		
		//주문 메뉴들을 불러와 넣는다.
		if(list != null) {
			for(OrderMenuData om : list) {
				if(om.getM() != null) {
					HBox h = makeNode.menuMake(om.getM().getName(), om.getCnt());
					Platform.runLater( () -> lv_ol.add(h));
				}
			}
		}
		price.setText(total);
		
		lv.setOnMouseClicked(e->{
			if(e.getButton()==MouseButton.PRIMARY&&e.getClickCount()==1) {
					System.out.println("제발요");
						for(Tablet t : tablet_list) {
							System.out.println("for 문은 된다");
							if(t.TableNo.equals(v.getId())) {
								System.out.println("이름 찾았는데 안나온다");
								System.out.println("찾은 이름: "+v.getId());
								tpc.show(Integer.parseInt(t.TableNo), t);			
						}
					}
				}
			
		});
		
		//우클릭으로 활성화/비활성화
		v.setOnMouseClicked( e -> {
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
		
		//테이블번호는 테이블이 빨간불일때만 변경할 수 있다.
		tf.setOnMouseClicked( e -> {
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
				v.setId("기본");
				tables.get(idx).setTableId("기본");
				tables.get(idx).setTableNo("기본");
			}else {
				v.setId(tf.getText());
				tables.get(idx).setTableId(tf.getText());
				tables.get(idx).setTableNo(tf.getText());
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
//	         ip = InetAddress.getLocalHost();   //현재 컴퓨터 아이피 받아오기
	         threadPool = Executors.newFixedThreadPool(10);
	         if(ss == null) {
	            ss = new ServerSocket();
//	            ss.bind(new InetSocketAddress(ip.getHostAddress(), 5555));
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
//	         for(Client c : client_list) {
//	            c.socket.close();
//	         }
//	         if(kitchen != null && !kitchen.socket.isClosed()) {
//	            kitchen.socket.close();
//	         }
	         Platform.exit();
	         System.exit(0);
	      }catch (Exception e) {
	         e.printStackTrace();
	         System.exit(0);
	      }
	}
	
	public class Tablet{
		private String TableNo;
		private Socket s;
		private ObjectInputStream ois;
		private ObjectOutputStream oos;
		
		public List<OrderMenuData> om_list = new ArrayList<>();
		
		private Data data;
		
		public Tablet(Socket s) {
			this.s = s;
		}
		
		private void welcome() {
			try {
				ois = new ObjectInputStream(s.getInputStream());
				data = (Data)ois.readObject();	//안녕
				System.out.println(data.getStatus());
				if(data.getStatus().equals("안녕")) {
					//현재 설정한 좌석 번호들을 넘겨주기
					List<String> no_list = new ArrayList<>();
					for(TableData td : tables) {
						String tmp = td.getTableId();
						if(!tmp.equals("기본"))
							no_list.add(tmp);
					}
					data.setNo_list(no_list);
					oos = new ObjectOutputStream(s.getOutputStream());
					send(data);
					tablet_list.add(this);
					listen();
				}else {
					return;
				}
			} catch (Exception e) {
				e.printStackTrace();
				
			}
		}
		
		//이 테블릿만 들을수 있음
		private void send(Data data) {
			if(data == null)
				return;
			try {
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
			
			if(data.getStatus().equals("번호정했다")) {
				for(TableData td : tables) {
					if(td.getTableId().equals(data.getTableNo())) {
						//빨간불, 초록불 체크
						if(td.getColor().equals("0xff0000ff")) {
							this.TableNo = data.getTableNo();
							System.out.println(TableNo + "번 들어옵니다.");
							data.setStatus("들어와");
							//메뉴리스트 보내주기
							data.setM_list(mc.m_list);
							send(data);
							System.out.println("보냄");
							//초록불 만들기
							td.setColor("0x00ff00ff");
							this.om_list = td.getOm_list();
							//태블릿 리스트에 자신추가
							System.out.println(om_list);
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
					System.out.println("요기??");
				}else {
					boolean flag = false;
					for(OrderMenuData om1 : data.getOm_list()) {
						for(OrderMenuData om2 : this.om_list) {
							if(om1.getM().getName().equals(om2.getM().getName())) {
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
			}
			save();
		}
		
		private void makeNode(String no) {
			System.out.println("메이크노드");
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
						
						for(OrderMenuData om : om_list) {
							HBox hbox = makeNode.menuMake(om.getM().getName(), om.getCnt());
							Platform.runLater( () -> lv_ol.add(hbox));
							total += om.getTotal();
						}
						lv.refresh();
						int t = total;
						Platform.runLater( () -> price.setText(t + ""));
						break;
					}
				}
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
					tablet_list.remove(t);
					break;
				}
			}
		}
	}
	
			
}
