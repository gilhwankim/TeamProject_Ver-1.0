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
	private DAO dao = DAO.getinstance();
	List<PaymentInfo> monthlyList;
	List<PaymentInfo> dailyList;
	List<PaymentInfo> thirtyDaysList;
	private SimpleDateFormat sdfMonth = new SimpleDateFormat("yyyy��MM��"); //�� ���� 
	private SimpleDateFormat sdfdaily = new SimpleDateFormat("yyyy��MM��dd��"); //�� ����
	private DecimalFormat df = new DecimalFormat("###,###");
	private int dayPrice;
	private int totalPrice;// �� ���� �ݾ�
	private int cnt = 0; // ���� �� �� count
	
	public SalesStatusController() {
		
	}
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		//�� ���� ���� �⺻ ��
		monthlyList = dao.selectDate(sdfMonth.format(new Date()));
		for(PaymentInfo mpi : monthlyList) {
			//ȯ�� �� ���� ������ ī��/���� ���� �ݾ� �հ�
			if(!mpi.getPayMethod().equals("ȯ��")) {
			totalPrice += Integer.parseInt(mpi.getTotalPrice().replaceAll(",", ""));
			cnt++;
			}
		}
		monthlyPay.setText(df.format(totalPrice)+"��");
		monthlyPayCnt.setText(df.format(cnt)+"��");
		cnt = 0;
		totalPrice = 0;
		
		//�� ���� ���� �⺻ ��
		dailyList = dao.selectDate(sdfdaily.format(new Date()));
		for(PaymentInfo dpi : dailyList) {
			//ȯ�� �� ���� ������ ī��/���� ���� �ݾ� �հ�
			if(!dpi.getPayMethod().equals("ȯ��")) {
				totalPrice += Integer.parseInt(dpi.getTotalPrice().replaceAll(",", ""));
				cnt++;
			}
		}
		dailyPay.setText(df.format(totalPrice)+"��");
		dailyPayCnt.setText(df.format(cnt)+"��");
		cnt = 0;
		totalPrice = 0;
		
		dateSel.setOnAction(e->{
			//LocalDate Ÿ�� ��¥ ���� ����. (LocalDate -> String)
			System.out.println(dateSel.getValue().format(DateTimeFormatter.ofPattern("yyyy��MM��dd��")));
		});
		
		//�ֱ� 30�� ��� ���� �ݾ�(����~29����)
		for(int i=0; i<30; i++) {
			thirtyDaysList =  dao.selectDate(LocalDate.now().minusDays(i).format(DateTimeFormatter.ofPattern("yyyy��MM��dd��")));
			for(PaymentInfo payAvg : thirtyDaysList) {
				dayPrice += Integer.parseInt(payAvg.getTotalPrice().replaceAll(",", ""));
				cnt++;
			}
			totalPrice += dayPrice;
			System.out.println(i+"���� ����: " + dayPrice);
			dayPrice = 0;
			
		}
		int totalAvg = totalPrice/cnt;
		lastThirtydayPay.setText(df.format(totalAvg)+"��");		
	}
	
}
