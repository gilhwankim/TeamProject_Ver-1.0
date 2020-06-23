package pos.javafile;

import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
	private @FXML Label  monthlyPay; // �� ���� �ݾ�
	private @FXML Label  monthlyPayCnt; // �� ���� �Ǽ�
	private @FXML Label  lastThirtydayPay; // �ֱ� 30�� ���� �ݾ�
	private @FXML Label  dailyPay; // ���� ����
	private @FXML Label  dailyPayCnt; //�� ���� �� ��
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
	private SimpleDateFormat sdfMonth = new SimpleDateFormat("yyyy��MM��"); //�� ���� 
	private SimpleDateFormat sdfdaily = new SimpleDateFormat("yyyy��MM��dd��"); //�� ����
	private DecimalFormat df = new DecimalFormat("###,###");
	private int dayPrice;
	private int totalPrice;// �� ���� �ݾ�
	private int cardPrice;
	private int cashPrice;
	private int cnt = 0; // ���� �� �� count
	private int cardCnt;
	private int cashCnt;
	
	public SalesStatusController() {
		
	}
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		pieChart(sdfMonth.format(new Date()));
		//�� ���� ���� �⺻ ��
		pickerMonthlyPay(sdfMonth.format(new Date()));
		//�� ���� ���� �⺻ ��
		pickerDailyPay(sdfdaily.format(new Date()));
		//���� ���� �� �� ���� �⺻ ��
		MonthlyPayMethod(sdfMonth.format(new Date()));
		
		//��¥ ���ý� ���� ���� ����
		dateSel.setOnAction(e->{
			//LocalDate Ÿ�� ��¥ ���� ����. (LocalDate -> String)
//			dateSel.getValue().format(DateTimeFormatter.ofPattern("yyyy��MM��dd��"));
			pickerMonthlyPay(dateSel.getValue().format(DateTimeFormatter.ofPattern("yyyy��MM��")));
			pickerDailyPay(dateSel.getValue().format(DateTimeFormatter.ofPattern("yyyy��MM��dd��")));
			MonthlyPayMethod(dateSel.getValue().format(DateTimeFormatter.ofPattern("yyyy��MM��")));
		});
		
		//�ֱ� 30�� ��� ���� �ݾ�(����~29����)
		for(int i=0; i<30; i++) {
			thirtyDaysList =  dao.selectDate(LocalDate.now().minusDays(i).format(DateTimeFormatter.ofPattern("yyyy��MM��dd��")));
			for(PaymentInfo payAvg : thirtyDaysList) {
				if(!payAvg.getPayMethod().equals("ȯ��")) {
					dayPrice += Integer.parseInt(payAvg.getTotalPrice().replaceAll(",", ""));
					cnt++;
				}
			}
			totalPrice += dayPrice;
			dayPrice = 0;
		}
		int totalAvg = totalPrice/cnt;
		lastThirtydayPay.setText(df.format(totalAvg)+"��");	
		
 
		
	}
	
	//��¥ ���� �� �ش� ��¥�� ���ϴ� ���� �������� ����.
	public void pickerMonthlyPay(String date) {
		cnt = 0;
		totalPrice = 0;
		monthlyList = dao.selectDate(date);
		for(PaymentInfo mpi : monthlyList) {
			//ȯ�� �� ���� ������ ī��/���� ���� �ݾ� �հ�
			if(!mpi.getPayMethod().equals("ȯ��")) {
			totalPrice += Integer.parseInt(mpi.getTotalPrice().replaceAll(",", ""));
			cnt++;
			}
		}
		monthlyPay.setText(df.format(totalPrice)+"��");
		monthlyPayCnt.setText(df.format(cnt)+"��");
	}
	
	//��¥ ���� �� �ش� ��¥ �������� ����.
	public void pickerDailyPay(String date) {
		cnt = 0;
		totalPrice = 0;
		monthlyList = dao.selectDate(date);
		for(PaymentInfo mpi : monthlyList) {
			//ȯ�� �� ���� ������ ī��/���� ���� �ݾ� �հ�
			if(!mpi.getPayMethod().equals("ȯ��")) {
			totalPrice += Integer.parseInt(mpi.getTotalPrice().replaceAll(",", ""));
			cnt++;
			}
		}
		dailyPay.setText(df.format(totalPrice)+"��");
		dailyPayCnt.setText(df.format(cnt)+"��");
	}
	
	//���� ���� �� �� ����
	public void MonthlyPayMethod(String date) {
		cardPrice = 0;
		cashPrice = 0;
		cardCnt = 0;
		cashCnt = 0;
		monthlyList = dao.selectDate(date);
		for(PaymentInfo mpi : monthlyList) {
			if(!mpi.getPayMethod().equals("ȯ��")) {
				if(mpi.getPayMethod().equals("ī��")) {
					cardPrice += Integer.parseInt(mpi.getTotalPrice().replaceAll(",", ""));
					cardCnt++;
				}else if(mpi.getPayMethod().equals("����")){
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
	
	public void pieChart(String date) {

			monthlyList = dao.selectDate(date);
			for(PaymentInfo mpi : monthlyList) {
				if(!mpi.getPayMethod().equals("ȯ��")) {
					boolean flag = false;
					for(MenuData md : mpi.getAllMenu()) {
						//�޴� ī��Ʈ
						if(map.size()==0) {
							map.put(md, md.getCnt());
							continue;
						}else {
							
							for(MenuData md2 : map.keySet()) {
								if(md.getName().equals(md2.getName())) {
									map.put(md2, map.get(md2)+md.getCnt());
									flag = true;
									break;
								}
						}if(flag==false) {
							map.put(md, md.getCnt());
						}
						flag = false;
					}
				}
			}
		}
//				
//				List<Entry<MenuData, Integer>> list2 = new ArrayList<>(map.entrySet());
//		        list2.sort(Entry.comparingByValue());
//	
//		        Map<MenuData, Integer> result = new LinkedHashMap<>();
//		        for (Entry<MenuData, Integer> entry : list2) {
//		            result.put(entry.getKey(), entry.getValue());
//		        }
//		        
			   ObservableList<Data> list = FXCollections.observableArrayList(); 
			   for(MenuData md : map.keySet()) {
				   list.add(new Data(md.getName(), map.get(md)));
				   
			   }
			   list.sort((a,b) -> Double.compare(a.getPieValue(), b.getPieValue()));
			   Platform.runLater(()->{ for(int i=0; i<list.size(); i++) {
				   if(i>4) {
					   
						   int tmp = i;
					   list.remove(i);
				   
				   }
			   }
			   });
		        bestMenu.setData(list); 
	}
}
