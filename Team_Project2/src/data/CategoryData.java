package data;

import java.io.Serializable;
import java.util.List;

//�޴����� �ǿ��� ����ϴ� ī�װ�
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
