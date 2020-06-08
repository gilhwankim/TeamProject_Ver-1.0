package data;

import java.io.Serializable;

//메뉴정보를 포함한 주문한 메뉴의 정보
public class OrderMenuData implements Serializable{

	MenuData m;
	String name;
	int cnt;
	int total;
	
	public OrderMenuData() {
	}
	
	public OrderMenuData(MenuData m, int cnt) {
		this.m = m;
		this.cnt = cnt;
	}
	
	public MenuData getM() {
		return m;
	}
	public void setM(MenuData m) {
		this.m = m;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getCnt() {
		return cnt;
	}
	public void setCnt(int cnt) {
		this.cnt = cnt;
	}
	public int getTotal() {
		this.total = m.getPrice() * cnt;
		return total;
	}
	
}
