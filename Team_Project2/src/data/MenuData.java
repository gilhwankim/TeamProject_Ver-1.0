package data;

import java.io.Serializable;

//�޴�����
public class MenuData implements Serializable{

	int No;
	String category;
	String name;
	int price;
	byte[] image;
	
	public int getNo() {
		return No;
	}
	public void setNo(int no) {
		No = no;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getPrice() {
		return price;
	}
	public void setPrice(int price) {
		this.price = price;
	}
	public byte[] getImage() {
		return image;
	}
	public void setImage(byte[] image) {
		this.image = image;
	}
	
	
	
}
