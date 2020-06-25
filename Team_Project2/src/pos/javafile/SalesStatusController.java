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
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import data.MenuData;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
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
	private LocalDate today;
	public SalesStatusController() {
		
	}
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		//�� ���� ���� �⺻ ��
		pickerMonthlyPay(sdfMonth.format(new Date()));
		//�� ���� ���� �⺻ ��
		pickerDailyPay(sdfdaily.format(new Date()));
		//���� ���� �� �� ���� �⺻ ��
		MonthlyPayMethod();
		//������Ʈ
		pieChart();
		//��¥ ���� �⺻ ��
		
		today = LocalDate.now();
		dateSel.setValue(today);
		//��¥ ���ý� ���� ���� ����
		dateSel.setOnAction(e->{
			//LocalDate Ÿ�� ��¥ ���� ����. (LocalDate -> String)
//			dateSel.getValue().format(DateTimeFormatter.ofPattern("yyyy��MM��dd��"));
			if(today.format(DateTimeFormatter.ofPattern("yyyy��MM��")).equals(dateSel.getValue().format(DateTimeFormatter.ofPattern("yyyy��MM��")))) {
				pickerDailyPay(dateSel.getValue().format(DateTimeFormatter.ofPattern("yyyy��MM��dd��")));
			}else {
				pickerMonthlyPay(dateSel.getValue().format(DateTimeFormatter.ofPattern("yyyy��MM��")));
				pickerDailyPay(dateSel.getValue().format(DateTimeFormatter.ofPattern("yyyy��MM��dd��")));
				MonthlyPayMethod();
				pieChart();
			}
			today = dateSel.getValue();
		});
		
		//�ֱ� 30�� ��� ���� �ݾ�(����~29����)
		int size = AllPayment.payInfoList.size();
		int size2 = 0;
		if(size>30) {
			size2= size-30;
		}
		thirtyDaysList = new ArrayList<PaymentInfo>();
		for(int i=size-1; i>=size2; i--) {
			thirtyDaysList.add(AllPayment.payInfoList.get(i));
//			thirtyDaysList =  dao.selectDate(LocalDate.now().minusDays(i).format(DateTimeFormatter.ofPattern("yyyy��MM��dd��")));
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
		monthlyList = new ArrayList<PaymentInfo>();
		for(PaymentInfo pi : AllPayment.payInfoList) {
			if(pi.getDate().substring(0, 8).equals(date)) {
				monthlyList.add(pi);
			}
		}
//		monthlyList = dao.selectDate(date);
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
	
//	//��¥ ���� �� �ش� ��¥ �������� ����.
	public void pickerDailyPay(String date) {
		cnt = 0;
		totalPrice = 0;
		for(PaymentInfo mpi : monthlyList) {
			if(mpi.getDate().substring(0, 11).equals(date)) {
				//ȯ�� �� ���� ������ ī��/���� ���� �ݾ� �հ�
				if(!mpi.getPayMethod().equals("ȯ��")) {
				totalPrice += Integer.parseInt(mpi.getTotalPrice().replaceAll(",", ""));
				cnt++;
				}
			}
		}
		dailyPay.setText(df.format(totalPrice)+"��");
		dailyPayCnt.setText(df.format(cnt)+"��");
	}
	
//	//���� ���� �� �� ����
	public void MonthlyPayMethod() {
		cardPrice = 0;
		cashPrice = 0;
		cardCnt = 0;
		cashCnt = 0;
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
	
//	//Best5 �޴� ���� ��Ʈ
	public void pieChart() {
			//������ ������ �ִ� ������ ����
			map.clear();
			
			for(PaymentInfo mpi : monthlyList) {
				if(!mpi.getPayMethod().equals("ȯ��")) {
					boolean flag = false;
					//�ҷ��� ���� ����� �޴��� ��´�.
					for(MenuData md : mpi.getAllMenu()) {
						//���� ó�� ����� �� �ش� �޴��� ���� ��ŭ �Է�.
						if(map.size()==0) {
							map.put(md, md.getCnt());
							continue;
						}else {
							//������� ���� ��
							for(MenuData md2 : map.keySet()) {
								//���� �޴��� �̹� �� �ִٸ�(md.getName()) ���� ���ϴ� �޴�(md2.getName())�� �ֹ��� ���� ���� ���� ������ ���Ѵ�.
								if(md.getName().equals(md2.getName())) {
									map.put(md2, map.get(md2)+md.getCnt());
									flag = true;
									break;
								}
								//������� ������ ���ο� �޴��� �� �ش� �޴��� ���� ��ŭ �Է�.
						}if(flag==false) {
							map.put(md, md.getCnt());
						}
						flag = false;
					}
				}
			}
		}
			   ObservableList<Data> list = FXCollections.observableArrayList(); 
			   //map �����͸� ObservableList�� ��´�
			   for(MenuData md : map.keySet()) {
				   list.add(new Data(md.getName(), map.get(md)));
			   }
			   //�⺻ �������� ����
			   list.sort((a,b) -> Double.compare(a.getPieValue(), b.getPieValue()));
			   //�������� ����(���� ���� �ȸ� ��)
			   Collections.reverse(list);
			   Platform.runLater(()->{
				   //���� ���� �ȸ� ���� 5���� ����� ������ ����
				   list.remove(5, list.size());				   
			   });

			   //���� ��Ʈ�� ������ �Է�
		        bestMenu.setData(list);
				   for(Data d : bestMenu.getData()) {
		                 d.nameProperty().bind(Bindings.concat(d.getName() + "\n(" + (int)d.getPieValue() + "��)"));
		              }
					}
				}
				
