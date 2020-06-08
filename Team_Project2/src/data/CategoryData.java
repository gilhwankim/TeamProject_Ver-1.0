package data;

import java.io.Serializable;
import java.util.List;

//메뉴관리 탭에서 사용하는 카테고리
public class CategoryData implements Serializable{

	private String status;
	private List<String> categorys;
	
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public List<String> getCategorys() {
		return categorys;
	}
	public void setCategorys(List<String> categorys) {
		this.categorys = categorys;
	}
}
