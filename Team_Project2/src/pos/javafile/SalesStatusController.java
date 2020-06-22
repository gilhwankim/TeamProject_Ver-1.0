package pos.javafile;

import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import pos.management.PaymentInfo;

public class SalesStatusController implements Initializable{
	private @FXML Label  monthlyPay; // 월 결제 금액
	private @FXML Label  monthlyPayCnt; // 월 결제 건수
	private @FXML Label  lastThirtydayPay; // 최근 30일 결제 금액
	private @FXML Label  dailyPay; // 일일 매출
	private @FXML Label  dailyPayCnt; //일 결제 건 수
	private @FXML Label  cardPay;
	private @FXML Label  cashPay;
	private @FXML PieChart bestMenu;
	private @FXML Button tmp;
	private @FXML DatePicker dateSel;
	private DAO dao = DAO.getinstance();
	List<PaymentInfo> monthlyList;
	List<PaymentInfo> dailyList;
	List<PaymentInfo> thirtyDaysList;
	private SimpleDateFormat sdfMonth = new SimpleDateFormat("yyyy년MM월"); //월 단위 
	private SimpleDateFormat sdfdaily = new SimpleDateFormat("yyyy년MM월dd일"); //일 단위
	private DecimalFormat df = new DecimalFormat("###,###");
	private int dayPrice;
	private int totalPrice;// 총 결제 금액
	private int cnt = 0; // 결제 건 수 count
	
	public SalesStatusController() {
		
	}
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		//월 결제 내역 기본 값
		monthlyList = dao.selectDate(sdfMonth.format(new Date()));
		for(PaymentInfo mpi : monthlyList) {
			//환불 건 수를 제외한 카드/현금 결제 금액 합계
			if(!mpi.getPayMethod().equals("환불")) {
			totalPrice += Integer.parseInt(mpi.getTotalPrice().replaceAll(",", ""));
			cnt++;
			}
		}
		monthlyPay.setText(df.format(totalPrice)+"원");
		monthlyPayCnt.setText(df.format(cnt)+"건");
		cnt = 0;
		totalPrice = 0;
		
		//일 결제 내역 기본 값
		dailyList = dao.selectDate(sdfdaily.format(new Date()));
		for(PaymentInfo dpi : dailyList) {
			//환불 건 수를 제외한 카드/현금 결제 금액 합계
			if(!dpi.getPayMethod().equals("환불")) {
				totalPrice += Integer.parseInt(dpi.getTotalPrice().replaceAll(",", ""));
				cnt++;
			}
		}
		dailyPay.setText(df.format(totalPrice)+"원");
		dailyPayCnt.setText(df.format(cnt)+"건");
		cnt = 0;
		totalPrice = 0;
		
		dateSel.setOnAction(e->{
			//LocalDate 타입 날짜 형식 지정. (LocalDate -> String)
			System.out.println(dateSel.getValue().format(DateTimeFormatter.ofPattern("yyyy년MM월dd일")));
		});
		
		//최근 30일 평균 결제 금액(오늘~29일전)
		for(int i=0; i<30; i++) {
			thirtyDaysList =  dao.selectDate(LocalDate.now().minusDays(i).format(DateTimeFormatter.ofPattern("yyyy년MM월dd일")));
			for(PaymentInfo payAvg : thirtyDaysList) {
				dayPrice += Integer.parseInt(payAvg.getTotalPrice().replaceAll(",", ""));
				cnt++;
			}
			totalPrice += dayPrice;
			System.out.println(i+"일차 가격: " + dayPrice);
			dayPrice = 0;
			
		}
		int totalAvg = totalPrice/cnt;
		lastThirtydayPay.setText(df.format(totalAvg)+"원");		
	}
	
}
