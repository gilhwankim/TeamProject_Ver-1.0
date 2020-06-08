package pos.javafile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;

import data.CategoryData;
import data.MenuData;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.ImageInput;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class MenuController  {

	private DAO dao = DAO.getinstance();
	private Stage serverStage;
	//menu.fxml-----------
	private VBox vbox;
	private ComboBox<String> comboBox;
	private TableView<MenuInMC> table;
	private TextField tfNum;
	private TextField tfName;
	private TextField tfPrice;
	private ImageView imageView;
	private Button btnCAdd;
	private Button btnCDel;
	private Button btnMAdd;
	private Button btnMDel;
	
	private ObservableList<String> comboBox_ol = FXCollections.observableArrayList();
	private ObservableList<MenuInMC> m_ol = FXCollections.observableArrayList();
	public List<MenuData> m_list; 	//메뉴데이터가 들어갈 리스트
	
	//categoryDialog.fxml-----------
	private Stage CD_stage;
	private TextField tf;
	
	private File file;
	private Pattern p = Pattern.compile("^[0-9]*$");	//숫자만
	private Matcher m;
	private FileChooser fc = new FileChooser();
	
	
	@SuppressWarnings("unchecked")
	public MenuController(Stage serverStage) {
		this.serverStage = serverStage;
		try {
			vbox = FXMLLoader.load(getClass().getResource("../fxml/menu.fxml"));
			comboBox = (ComboBox<String>)vbox.lookup("#comboBox");
			table = (TableView<MenuInMC>)vbox.lookup("#table");
			tfNum = (TextField)vbox.lookup("#tfNum");
			tfName = (TextField)vbox.lookup("#tfName");
			tfPrice = (TextField)vbox.lookup("#tfPrice");
			btnCAdd = (Button)vbox.lookup("#btnCAdd");
			btnCDel = (Button)vbox.lookup("#btnCDel");
			btnMAdd = (Button)vbox.lookup("#btnMAdd");
			btnMDel = (Button)vbox.lookup("#btnMDel");
			imageView = (ImageView)vbox.lookup("#imageView");
		} catch (Exception e) {
			e.printStackTrace();
		}
		CategoryData cd = (CategoryData)dao.load("메뉴카테고리");
		if(cd != null) {
			for(String s : cd.getCategorys()) {
				comboBox_ol.add(s);
			}
		}
		table.setItems(m_ol);
		comboBox.setItems(comboBox_ol);
		//처음 기본 선택되는 index = 0
		comboBox.getSelectionModel().clearAndSelect(0);
		//최대 보여주는 index 수 (넘어가면 스크롤생김)
		comboBox.setVisibleRowCount(5);
		
		//번호 숫자만
		tfNum.textProperty().addListener( (ob, oldS, newS) -> {
			m = p.matcher(newS);
			if(!m.find()) {
				tfNum.setText(oldS);
			}
		});
		//가격 숫자만
		tfPrice.textProperty().addListener( (ob, oldS, newS) -> {
			m = p.matcher(newS);
			if(!m.find()) {
				tfPrice.setText(oldS);
			}
		});
		//메뉴 선택했을때
		table.getSelectionModel().selectedItemProperty().addListener((ob, oldB, newB) -> {
			if(newB != null) {
				tfNum.setText(newB.getNo() + "");
				for(String str : comboBox_ol) {
					if(newB.getCategory().equals(str)) {
						comboBox.getSelectionModel().select(str);
						break;
					}
				}
				tfName.setText(newB.getName());
				tfPrice.setText(newB.getPrice() + "");
				imageView.setImage(new Image(new ByteArrayInputStream(newB.getImage())));
			}
		});
		table.setOnKeyReleased( e -> {
			//ESC 눌렀을 때(선택초기화)
			if(e.getCode() == KeyCode.ESCAPE) {
				System.out.println("누름");
				table.getSelectionModel().clearSelection();
				clear();
			//DEL 눌렀을 때(메뉴삭제)
			}else if(e.getCode() == KeyCode.DELETE) {
				btnMDelAction(new ActionEvent());
			}
		});
		
		
		
		TableColumn<MenuInMC, ?> toNum = table.getColumns().get(0);
		toNum.setCellValueFactory(new PropertyValueFactory<>("no"));
		toNum.setStyle("-fx-aliment : CENTER");
		
		TableColumn<MenuInMC, ?> toCategory = table.getColumns().get(1);
		toCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
		toCategory.setStyle("-fx-aliment : CENTER");
		
		TableColumn<MenuInMC, ?> toName = table.getColumns().get(2);
		toName.setCellValueFactory(new PropertyValueFactory<>("name"));
		toName.setStyle("-fx-aliment : CENTER");
		
		TableColumn<MenuInMC, ?> toPrice = table.getColumns().get(3);
		toPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
		toPrice.setStyle("-fx-aliment : CENTER");
		
		btnCAdd.setOnAction( e -> btnCAddAction(e));
		btnCDel.setOnAction( e -> btnCDelAction(e));
		imageView.setOnMouseClicked( e -> ivClickAction(e));
		btnMAdd.setOnAction( e -> btnMAddAction(e));
		btnMDel.setOnAction( e -> btnMDelAction(e));
		menuLoad();
	}
	
	public VBox get() {
		return this.vbox;
	}
	
	public List<MenuData> getM_list(){
		return this.m_list;
	}
	
	//카테고리 추가 버튼 동작
	private void btnCAddAction(ActionEvent event) {
		try {
			//다이얼로그를 만든다.
			CD_stage = new Stage(StageStyle.TRANSPARENT);
			VBox vbox = FXMLLoader.load(getClass().getResource("../fxml/categoryDialog.fxml"));
			Scene scene = new Scene(vbox);
			tf = (TextField)vbox.lookup("#tf");
			Button ok = (Button)vbox.lookup("#ok");
			CD_stage.setScene(scene);
			CD_stage.initModality(Modality.APPLICATION_MODAL);
			CD_stage.show();
			ok.setOnAction( e -> CD_okAction(e));
			scene.setOnKeyReleased(e -> {
				if(CD_stage.isShowing()) {
					if(e.getCode() == KeyCode.ESCAPE)
						CD_stage.close();
					else if(e.getCode() == KeyCode.ENTER) {
						CD_okAction(new ActionEvent());
					}
				}
			});
			//테두리 css
			vbox.setStyle("-fx-border-color: black; -fx-border-radius: 5px");
//			CD_stage.setY(serverStage.getY() + serverStage.getHeight() - 185);
//			CD_stage.setX(serverStage.getX() + serverStage.getWidth() - 320);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//카테고리추가 다이얼로그의 확인버튼
	private void CD_okAction(ActionEvent event) {
		if(tf.getText().length() != 0 && !tf.getText().equals("")) {
			comboBox_ol.add(tf.getText());
			CD_stage.close();
			categorySave();
		}else {
			CD_stage.close();
		}
	}
	
	//카테고리 삭제
	private void btnCDelAction(ActionEvent event) {
		String str = comboBox.getSelectionModel().getSelectedItem();
		comboBox_ol.removeIf( e -> e.contains(str));
		categorySave();
	}
	
	//이미지뷰 클릭시 선택차 열기
	private void ivClickAction(MouseEvent event) {
		if(event.getButton() == MouseButton.PRIMARY) {
			fc.getExtensionFilters().add(new ExtensionFilter("이미지(*.jpg,*.png,*.gif)", "*.jpg","*.png","*.gif"));
			try {
				file = fc.showOpenDialog(serverStage);
				imageView.setImage(new Image(new FileInputStream(file)));
				System.out.println(file.toString());
			} catch (Exception e) {
				return;
			}
		}
	}
	
	//메뉴 추가버튼
	private void btnMAddAction(ActionEvent event) {
		if(tfNum.getText().equals("") || tfName.getText().equals("") || 
				tfPrice.getText().equals("") || comboBox.getSelectionModel().getSelectedItem().equals(null))
			return;
		
		int num = Integer.parseInt(tfNum.getText());
		String category = comboBox.getSelectionModel().getSelectedItem();
		String name = tfName.getText();
		int price = Integer.parseInt(tfPrice.getText());
		
		byte[] b = null;
		FileInputStream fis = null;
		try {
			//이미지파일 선택 안할시 noImage.jpg 설정
			if(file == null) {
				//현재 프로젝트의 경로를 가진다.
				file = new File(".");
				String projectPath = file.getAbsolutePath().substring(0, file.getAbsolutePath().length()-1);
				file = new File(projectPath + "src\\pos\\images\\noImage.jpg");
			}
			fis = new FileInputStream(file);
			b = new byte[fis.available()]; 
			fis.read(b);
			fis.close();
			
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		menuSave(num, category, name, price, b);
		clear();
	}
	
	//메뉴 삭제버튼
	private void btnMDelAction(ActionEvent event) {
		MenuInMC mc = table.getSelectionModel().getSelectedItem();
		if(mc != null) {
			dao.menuDelete(mc.getNo());
		}
		menuLoad();
		clear();
	}
	
	private void clear() {
		tfNum.clear();
		tfName.clear();
		tfPrice.clear();
		imageView.setImage(null);
		file = null;
		comboBox.getSelectionModel().selectFirst();
	}
	
	//DB에 카테고리 정보 저장
	private void categorySave() {
		List<String> list = new ArrayList<>();
		for(String s : comboBox_ol) {
			list.add(s);
		}
		CategoryData cd = new CategoryData();
		cd.setStatus("메뉴카테고리");
		cd.setCategorys(list);
		dao.save(cd.getStatus(), cd);
	}
	
	//DB에 메뉴 저장
	private void menuSave(int num, String category, String name, int price, byte[] image) {
		MenuData md = new MenuData();
		md.setNo(num);
		md.setCategory(category);
		md.setName(name);
		md.setPrice(price);
		md.setImage(image);
		dao.menuSave(md);
		menuLoad();
	}
	
	//DB에서 메뉴리스트 로드
	private void menuLoad() {
		m_ol.clear();
		m_list = dao.menuListLoad();
		for(MenuData md : m_list) {
			MenuInMC mc = new MenuInMC(md.getNo(), md.getCategory(), md.getName(), md.getPrice(), md.getImage());
			m_ol.add(mc);
		}
	}
	
	
}
