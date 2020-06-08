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
	public List<MenuData> m_list; 	//�޴������Ͱ� �� ����Ʈ
	
	//categoryDialog.fxml-----------
	private Stage CD_stage;
	private TextField tf;
	
	private File file;
	private Pattern p = Pattern.compile("^[0-9]*$");	//���ڸ�
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
		CategoryData cd = (CategoryData)dao.load("�޴�ī�װ�");
		if(cd != null) {
			for(String s : cd.getCategorys()) {
				comboBox_ol.add(s);
			}
		}
		table.setItems(m_ol);
		comboBox.setItems(comboBox_ol);
		//ó�� �⺻ ���õǴ� index = 0
		comboBox.getSelectionModel().clearAndSelect(0);
		//�ִ� �����ִ� index �� (�Ѿ�� ��ũ�ѻ���)
		comboBox.setVisibleRowCount(5);
		
		//��ȣ ���ڸ�
		tfNum.textProperty().addListener( (ob, oldS, newS) -> {
			m = p.matcher(newS);
			if(!m.find()) {
				tfNum.setText(oldS);
			}
		});
		//���� ���ڸ�
		tfPrice.textProperty().addListener( (ob, oldS, newS) -> {
			m = p.matcher(newS);
			if(!m.find()) {
				tfPrice.setText(oldS);
			}
		});
		//�޴� ����������
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
			//ESC ������ ��(�����ʱ�ȭ)
			if(e.getCode() == KeyCode.ESCAPE) {
				System.out.println("����");
				table.getSelectionModel().clearSelection();
				clear();
			//DEL ������ ��(�޴�����)
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
	
	//ī�װ� �߰� ��ư ����
	private void btnCAddAction(ActionEvent event) {
		try {
			//���̾�α׸� �����.
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
			//�׵θ� css
			vbox.setStyle("-fx-border-color: black; -fx-border-radius: 5px");
//			CD_stage.setY(serverStage.getY() + serverStage.getHeight() - 185);
//			CD_stage.setX(serverStage.getX() + serverStage.getWidth() - 320);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//ī�װ��߰� ���̾�α��� Ȯ�ι�ư
	private void CD_okAction(ActionEvent event) {
		if(tf.getText().length() != 0 && !tf.getText().equals("")) {
			comboBox_ol.add(tf.getText());
			CD_stage.close();
			categorySave();
		}else {
			CD_stage.close();
		}
	}
	
	//ī�װ� ����
	private void btnCDelAction(ActionEvent event) {
		String str = comboBox.getSelectionModel().getSelectedItem();
		comboBox_ol.removeIf( e -> e.contains(str));
		categorySave();
	}
	
	//�̹����� Ŭ���� ������ ����
	private void ivClickAction(MouseEvent event) {
		if(event.getButton() == MouseButton.PRIMARY) {
			fc.getExtensionFilters().add(new ExtensionFilter("�̹���(*.jpg,*.png,*.gif)", "*.jpg","*.png","*.gif"));
			try {
				file = fc.showOpenDialog(serverStage);
				imageView.setImage(new Image(new FileInputStream(file)));
				System.out.println(file.toString());
			} catch (Exception e) {
				return;
			}
		}
	}
	
	//�޴� �߰���ư
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
			//�̹������� ���� ���ҽ� noImage.jpg ����
			if(file == null) {
				//���� ������Ʈ�� ��θ� ������.
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
	
	//�޴� ������ư
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
	
	//DB�� ī�װ� ���� ����
	private void categorySave() {
		List<String> list = new ArrayList<>();
		for(String s : comboBox_ol) {
			list.add(s);
		}
		CategoryData cd = new CategoryData();
		cd.setStatus("�޴�ī�װ�");
		cd.setCategorys(list);
		dao.save(cd.getStatus(), cd);
	}
	
	//DB�� �޴� ����
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
	
	//DB���� �޴�����Ʈ �ε�
	private void menuLoad() {
		m_ol.clear();
		m_list = dao.menuListLoad();
		for(MenuData md : m_list) {
			MenuInMC mc = new MenuInMC(md.getNo(), md.getCategory(), md.getName(), md.getPrice(), md.getImage());
			m_ol.add(mc);
		}
	}
	
	
}
