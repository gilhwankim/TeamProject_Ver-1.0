package pos.javafile;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import data.MenuData;
import pos.management.PaymentInfo;

public class ExportExcel {
   private DAO dao = DAO.getinstance();
   private String directory = "C:\\posLog\\";  
   
   private XSSFWorkbook wb;
   private Sheet sheet;
   private Cell c;
   private Row r;
   
   private File file;
   
   public ExportExcel() {
   }
   
   public void export(List<PaymentInfo> list) {
      
      wb = new XSSFWorkbook();
      //전체 메뉴 받아오기
      List<MenuData> m_list = dao.menuListLoad();
         
      //처음 할 때
      //시트 이름 ex)6월
      sheet = wb.createSheet(list.get(0).getDate().substring(6, 8));
      XSSFCellStyle cs = wb.createCellStyle();
      XSSFDataFormat df = wb.createDataFormat();
      cs.setDataFormat(df.getFormat("#,###"));
      
      sheet.setColumnWidth(0, 9000);
      r = sheet.createRow(0);
      
      c = r.createCell(1);
      c.setCellValue("금액");
      
      c = r.createCell(2);
      c.setCellValue("결제 방법");
      
      int i = 3;
      for(MenuData m : m_list) {
         sheet.setColumnWidth(i, 2000);
         c = r.createCell(i++);
         c.setCellValue(m.getName());
      }
      int j = 1;
      for(PaymentInfo p : list) {
         int k = 0;
         r = sheet.createRow(j++);
         c = r.createCell(k++);
         c.setCellValue(p.getDate());
         
         c = r.createCell(k++);
         c.setCellStyle(cs);
         c.setCellValue(Integer.parseInt(p.getTotalPrice().replaceAll(",", "")));
         
         c = r.createCell(k++);
         c.setCellValue(p.getPayMethod());
         
         for(MenuData m : p.getAllMenu()) {
            Row r2 = sheet.getRow(0);
            for(int i1 = 2; i1 <= r2.getLastCellNum(); i1++) {
               c = r2.getCell(i1);
               if(c.getStringCellValue().equals(m.getName())) {
                  Cell c3 = r.createCell(c.getColumnIndex());
                  c3.setCellValue(m.getCnt());
                  break;
               }
            }
         }
      }
      
      r = sheet.createRow(j + 2);
      c = r.createCell(1);
      c.setCellStyle(cs);
      c.setCellFormula("SUMIF(C2:C" + j + ",\"현금\"," + "B2:B" + j + ")");
      c = r.createCell(2);
      c.setCellValue("현금");
      
      r = sheet.createRow(j + 3);
      c = r.createCell(1);
      c.setCellStyle(cs);
      c.setCellFormula("SUMIF(C2:C" + j + ",\"카드\"," + "B2:B" + j + ")");
      c = r.createCell(2);
      c.setCellValue("카드");
      
      r = sheet.createRow(j + 4);
      c = r.createCell(1);
      c.setCellStyle(cs);
      c.setCellFormula("SUMIF(C2:C" + j + ",\"환불\"," + "B2:B" + j + ")");
      c = r.createCell(2);
      c.setCellValue("환불");
      
      r = sheet.createRow(j + 1);
      c = r.createCell(1);
      c.setCellStyle(cs);
      c.setCellFormula("SUM(B" + (j+3) + ":B" + (j+4) + ")");
      c = r.createCell(2);
      c.setCellValue("전체");
      
      //폴더 생성 경로에 폴더 자동 생성
      File dir = new File(directory);
      if(!dir.exists()) {
         dir.mkdirs();
      }
      
      try {
         file = new File(directory + list.get(0).getDate().substring(0, 8) + ".xlsx");
         if(!file.exists()) {
            file.createNewFile();
         }
         FileOutputStream fos = new FileOutputStream(file);
         wb.write(fos);
         fos.flush();
         fos.close();
      } catch (Exception e) {
         e.printStackTrace();
      }
         
      
      System.out.println("내보내기 성공!");
   }
   
}