package pos.javafile;

import java.util.List;

import pos.management.PaymentInfo;

public class AllPayment {
	public static DAO dao = DAO.getinstance();
	public static List<PaymentInfo> payInfoList; //�Ǹų��� ������ �ִ� ����Ʈ
	//���� �Ǹų����� �� �����´�.
    
	public AllPayment() {
	}
	
	public static void makeList() {
		//���� ��ü �Ǹų����� ��´�. 
		payInfoList = dao.selectDate(null);
	}
	
	//DB�� POS���� �Ǹų��� ����Ʈ�� ���� �ֹ��� ���� �߰�
	public static void insertList(PaymentInfo data) {
		dao.PaymentInfo(data);
		payInfoList.add(data);
	}
	
	//ȯ���ϱ�
	public static void refundsList(PaymentInfo pi) {
		//DB������ ����(ȯ��)
		dao.refund(pi.getDate());
		//POS�� ������ �ִ� ������ �����ͼ� ȯ�ҷ� ����
		for(PaymentInfo pif : payInfoList) {
			if(pif.getDate().equals(pi.getDate())) {
				pif.setPayMethod("ȯ��");
				break;
			}
		}
	}
}
