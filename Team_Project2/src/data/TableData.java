package data;

import java.io.Serializable;
import java.util.List;

import javafx.scene.paint.Paint;

public class TableData implements Serializable{

	String status;
	String tableId;
	String tableNo;
	String color;
	boolean disable;
	List<MenuData> om_list;
	String total;
	
	//Color.web(레드 or 그린)
	//0xff0000ff - 레드
	//0x00ff00ff - 그린
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getTableId() {
		return tableId;
	}
	public void setTableId(String tableId) {
		this.tableId = tableId;
	}
	public String getTableNo() {
		return tableNo;
	}
	public String getColor() {
		return color;
	}
	public void setColor(String color) {
		this.color = color;
	}
	public void setTableNo(String tableNo) {
		this.tableNo = tableNo;
	}
	public boolean isDisable() {
		return disable;
	}
	public void setDisable(boolean disable) {
		this.disable = disable;
	}
	public List<MenuData> getOm_list() {
		return om_list;
	}
	public void setOm_list(List<MenuData> om_list) {
		this.om_list = om_list;
	}
	public String getTotal() {
		return total;
	}
	public void setTotal(String total) {
		this.total = total;
	}
	
}
