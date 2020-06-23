package pos.javafile;

import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import data.MenuData;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.PieChart.Data;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
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
	private @FXML ProgressBar cardBar;
	private @FXML ProgressBar cashBar;
	private DAO dao = DAO.getinstance();
	List<PaymentInfo> monthlyList;
	List<PaymentInfo> dailyList;
	List<PaymentInfo> thirtyDaysList;
	Map<MenuData, Integer> map = new HashMap<MenuData, Integer>();
	private SimpleDateFormat sdfMonth = new SimpleDateFormat("yyyy년MM월"); //월 단위 
	private SimpleDateFormat sdfdaily = new SimpleDateFormat("yyyy년MM월dd일"); //일 단위
	private DecimalFormat df = new DecimalFormat("###,###");
	private int dayPrice;
	private int totalPrice;// 총 결제 금액
	private int cardPrice;
	private int cashPrice;
	private int cnt = 0; // 결제 건 수 count
	private int cardCnt;
	private int cashCnt;
	public SalesStatusController() {
		
	}
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		pieChart(sdfMonth.format(new Date()));
		//월 결제 내역 기본 값
		pickerMonthlyPay(sdfMonth.format(new Date()));
		//일 결제 내역 기본 값
		pickerDailyPay(sdfdaily.format(new Date()));
		//결제 수단 별 월 매출 기본 값
		MonthlyPayMethod(sdfMonth.format(new Date()));
		
		//날짜 선택시 매출 내역 변경
		dateSel.setOnAction(e->{
			//LocalDate 타입 날짜 형식 지정. (LocalDate -> String)
//			dateSel.getValue().format(DateTimeFormatter.ofPattern("yyyy년MM월dd일"));
			pickerMonthlyPay(dateSel.getValue().format(DateTimeFormatter.ofPattern("yyyy년MM월")));
			pickerDailyPay(dateSel.getValue().format(DateTimeFormatter.ofPattern("yyyy년MM월dd일")));
			MonthlyPayMethod(dateSel.getValue().format(DateTimeFormatter.ofPattern("yyyy년MM월")));
			pieChart(dateSel.getValue().format(DateTimeFormatter.ofPattern("yyyy년MM월")));
		});
		
		//최근 30일 평균 결제 금액(오늘~29일전)
		for(int i=0; i<30; i++) {
			thirtyDaysList =  dao.selectDate(LocalDate.now().minusDays(i).format(DateTimeFormatter.ofPattern("yyyy년MM월dd일")));
			for(PaymentInfo payAvg : thirtyDaysList) {
				if(!payAvg.getPayMethod().equals("환불")) {
					dayPrice += Integer.parseInt(payAvg.getTotalPrice().replaceAll(",", ""));
					cnt++;
				}
			}
			totalPrice += dayPrice;
			dayPrice = 0;
		}
		int totalAvg = totalPrice/cnt;
		lastThirtydayPay.setText(df.format(totalAvg)+"원");	
		
 
		
	}
	
	//날짜 선택 시 해당 날짜가 속하는 달을 기준으로 변경.
	public void pickerMonthlyPay(String date) {
		cnt = 0;
		totalPrice = 0;
		monthlyList = dao.selectDate(date);
		for(PaymentInfo mpi : monthlyList) {
			//환불 건 수를 제외한 카드/현금 결제 금액 합계
			if(!mpi.getPayMethod().equals("환불")) {
			totalPrice += Integer.parseInt(mpi.getTotalPrice().replaceAll(",", ""));
			cnt++;
			}
		}
		monthlyPay.setText(df.format(totalPrice)+"원");
		monthlyPayCnt.setText(df.format(cnt)+"건");
	}
	
	//날짜 선택 시 해당 날짜 기준으로 변경.
	public void pickerDailyPay(String date) {
		cnt = 0;
		totalPrice = 0;
		monthlyList = dao.selectDate(date);
		for(PaymentInfo mpi : monthlyList) {
			//환불 건 수를 제외한 카드/현금 결제 금액 합계
			if(!mpi.getPayMethod().equals("환불")) {
			totalPrice += Integer.parseInt(mpi.getTotalPrice().replaceAll(",", ""));
			cnt++;
			}
		}
		dailyPay.setText(df.format(totalPrice)+"원");
		dailyPayCnt.setText(df.format(cnt)+"건");
	}
	
	//결제 수단 별 월 매출
	public void MonthlyPayMethod(String date) {
		cardPrice = 0;
		cashPrice = 0;
		cardCnt = 0;
		cashCnt = 0;
		monthlyList = dao.selectDate(date);
		for(PaymentInfo mpi : monthlyList) {
			if(!mpi.getPayMethod().equals("환불")) {
				if(mpi.getPayMethod().equals("카드")) {
					cardPrice += Integer.parseInt(mpi.getTotalPrice().replaceAll(",", ""));
					cardCnt++;
				}else if(mpi.getPayMethod().equals("현금")){
					cashPrice += Integer.parseInt(mpi.getTotalPrice().replaceAll(",", ""));
					cashCnt++;
				}
			}
		}
		cardPay.setText(df.format(cardPrice));
		cashPay.setText(df.format(cashPrice));
		double cardRatio = (double)cardCnt/(cardCnt+cashCnt);
		double cashRatio = (double)cashCnt/(cardCnt+cashCnt);
		cardBar.setProgress(cardRatio);
		cashBar.setProgress(cashRatio);
	}
	
	//Best5 메뉴 파이 차트
	public void pieChart(String date) {
			//이전에 가지고 있던 데이터 제거
			map.clear();
			
			monthlyList = dao.selectDate(date);
			for(PaymentInfo mpi : monthlyList) {
				if(!mpi.getPayMethod().equals("환불")) {
					boolean flag = false;
					//불러온 결제 목록의 메뉴를 담는다.
					for(MenuData md : mpi.getAllMenu()) {
						//제일 처음 비었을 때 해당 메뉴의 개수 만큼 입력.
						if(map.size()==0) {
							map.put(md, md.getCnt());
							continue;
						}else {
							//비어있지 않을 때
							for(MenuData md2 : map.keySet()) {
								//같은 메뉴가 이미 들어가 있다면(md.getName()) 지금 비교하는 메뉴(md2.getName())의 주문한 수량 값을 기존 값에다 더한다.
								if(md.getName().equals(md2.getName())) {
									map.put(md2, map.get(md2)+md.getCnt());
									flag = true;
									break;
								}
								//비어있지 않지만 새로운 메뉴일 때 해당 메뉴의 개수 만큼 입력.
						}if(flag==false) {
							map.put(md, md.getCnt());
						}
						flag = false;
					}
				}
			}
		}
			   ObservableList<Data> list = FXCollections.observableArrayList(); 
			   //map 데이터를 ObservableList에 담는다
			   for(MenuData md : map.keySet()) {
				   list.add(new Data(md.getName(), map.get(md)));
			   }
			   //기본 오름차순 정렬
			   list.sort((a,b) -> Double.compare(a.getPieValue(), b.getPieValue()));
			   //내림차순 정렬(제일 많이 팔린 순)
			   Collections.reverse(list);
			   Platform.runLater(()->{
				   //제일 많이 팔린 앞의 5개만 남기고 나머지 삭제
				   list.remove(5, list.size());				   
			   });
			   //파이 차트에 데이터 입력
		        bestMenu.setData(list); 
	}
}

