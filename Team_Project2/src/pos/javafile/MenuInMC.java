package pos.javafile;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class MenuInMC {
	
	private SimpleIntegerProperty no;
	private SimpleStringProperty category;
	private SimpleStringProperty name;
	private SimpleIntegerProperty price;
	private byte[] image;
	
	public MenuInMC(int menuNum, String category, String name, int price, byte[] image) {
		this.no = new SimpleIntegerProperty(menuNum);
		this.category = new SimpleStringProperty(category);
		this.name = new SimpleStringProperty(name);
		this.price = new SimpleIntegerProperty(price);
		this.image = image;
	}
	
	public int getNo() {
		return no.get();
	}
	public void setNo(int no) {
		this.no.set(no);
	}
	public String getCategory() {
		return category.get();
	}
	public void setCategory(String category) {
		this.category.set(category);
	}
	public String getName() {
		return name.get();
	}
	public void setName(String name) {
		this.name.set(name);
	}
	public int getPrice() {
		return price.get();
	}
	public void setPrice(int price) {
		this.price.set(price);
	}
	public byte[] getImage() {
		return image;
	}
	public void setImage(byte[] image) {
		this.image = image;
	}
	
}
