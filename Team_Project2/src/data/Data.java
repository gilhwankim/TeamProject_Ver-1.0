package data;

import java.io.Serializable;
import java.util.List;

//소켓끼리의 데이터전송용
public class Data implements Serializable{

	String status;
	String tableNo;
	List<String> no_list;
	MenuData md;
	List<MenuData> m_list;
	List<OrderMenuData> om_list;
	
	
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getTableNo() {
		return tableNo;
	}
	public void setTableNo(String tableNo) {
		this.tableNo = tableNo;
	}
	public List<String> getNo_list() {
		return no_list;
	}
	public void setNo_list(List<String> no_list) {
		this.no_list = no_list;
	}
	public MenuData getMd() {
		return md;
	}
	public void setMd(MenuData md) {
		this.md = md;
	}
	public List<MenuData> getM_list() {
		return m_list;
	}
	public void setM_list(List<MenuData> m_list) {
		this.m_list = m_list;
	}
	public List<OrderMenuData> getOm_list() {
		return om_list;
	}
	public void setOm_list(List<OrderMenuData> om_list) {
		this.om_list = om_list;
	}
	
	
	
}
