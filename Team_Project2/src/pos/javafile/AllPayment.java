package pos.javafile;

import java.util.List;

import pos.management.PaymentInfo;

public class AllPayment {
	public static DAO dao = DAO.getinstance();
	public static List<PaymentInfo> payInfoList; //판매내역 가지고 있는 리스트
	//현재 판매내역을 다 가져온다.
    
	public AllPayment() {
	}
	
	public static void makeList() {
		//현재 전체 판매내역을 담는다. 
		payInfoList = dao.selectDate(null);
	}
	
	//DB와 POS기의 판매내역 리스트에 새로 주문한 내역 추가
	public static void insertList(PaymentInfo data) {
		dao.PaymentInfo(data);
		payInfoList.add(data);
	}
	
	//환불하기
	public static void refundsList(PaymentInfo pi) {
		//DB데이터 수정(환불)
		dao.refund(pi.getDate());
		//POS가 가지고 있는 데이터 가져와서 환불로 수정
		for(PaymentInfo pif : payInfoList) {
			if(pif.getDate().equals(pi.getDate())) {
				pif.setPayMethod("환불");
				break;
			}
		}
	}
}
