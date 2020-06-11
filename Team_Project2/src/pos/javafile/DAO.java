package pos.javafile;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import data.MenuData;

public class DAO {
	
	private static final String ID = "root";
	private static final String PW = "1234";
	private static final String URL = "jdbc:mysql://localhost:3306/testdb";
	public Connection conn;
	
	public static DAO instance;
	public DAO() {
		try {
            Class.forName("com.mysql.jdbc.Driver"); 
            conn = DriverManager.getConnection(URL, ID, PW);
            System.out.println("드라이버 로딩 성공!!");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("드라이버 로드 실패!!");
        }
	}
	
	 public static DAO getinstance() {
	    	if(instance == null) {
	    		instance = new DAO();
	    	}
	    	return instance;
	 }
	
	public void save(String status, Object d) {
		 String sql = "insert into testtbl values(?, ?);";
	       PreparedStatement pstmt = null;
	        try {
	            pstmt = conn.prepareStatement(sql);
	            pstmt.setString(1, status);  
	            pstmt.setObject(2, d);
	            pstmt.executeUpdate();
	            System.out.println("데이터 삽입 성공!");
	        } catch (Exception e) {            
		        update(status, d);
	        }
	}
	
	public Object load(String status) {
		 String sql = "select * from testtbl where status = ?;";
	       PreparedStatement pstmt = null;
	        try {
	            pstmt = conn.prepareStatement(sql);
	            pstmt.setString(1, status);
	            ResultSet rs = pstmt.executeQuery();
	            Object d = null;
	            ByteArrayInputStream bais = null;
	            ObjectInputStream ins = null;
	            while(rs.next()) {
	            	bais = new ByteArrayInputStream(rs.getBytes(2));
	            	ins = new ObjectInputStream(bais);
	            	d = ins.readObject();
	            }
	            bais.close();
	            ins.close();
	            System.out.println("데이터 로드 성공!");
	            return d;
	        } catch (Exception e) {            
	           System.out.println("데이터 로드 실패!");
	           return null;
	        } 
	}
	public void update(String status, Object d) {
		 String sql = "update testtbl set obj = ? where status = ?";
	       PreparedStatement pstmt = null;
	        try {
	            pstmt = conn.prepareStatement(sql);
	            pstmt.setObject(1, d);
	            pstmt.setString(2, status);  
	            pstmt.executeUpdate();
	            System.out.println("데이터 갱신 성공");
	        } catch (Exception e) {            
	        	System.out.println("데이터 갱신 실패");
	        	e.printStackTrace();
	        }
	}
	
	public boolean menuSave(MenuData md) {
		 String sql = "insert into menutbl values(?, ?, ?, ?, ?);";
	       PreparedStatement pstmt = null;
	        try {
	            pstmt = conn.prepareStatement(sql);
	            pstmt.setInt(1, md.getNo());  
	            pstmt.setString(2, md.getCategory());
	            pstmt.setString(3, md.getName());
	            pstmt.setInt(4, md.getPrice());
	            pstmt.setObject(5, md.getImage());
	            pstmt.executeUpdate();
	            System.out.println("데이터 삽입 성공!");
	            return true;
	        } catch (Exception e) {            
	        	menuUpdate(md);
	        }
	        return false;
	}
	
	public List<MenuData> menuListLoad() {
		String sql = "select * from menutbl;";
	    PreparedStatement pstmt = null;
	    List<MenuData> list = new ArrayList<>();
	    	try {
	    		pstmt = conn.prepareStatement(sql);
	            ResultSet rs = pstmt.executeQuery();
	            while(rs.next()) {
	            	MenuData md = new MenuData();
	            	md.setNo(rs.getInt(1));
	            	md.setCategory(rs.getString(2));
	            	md.setName(rs.getString(3));
	            	md.setPrice(rs.getInt(4));
	            	md.setImage(rs.getBytes(5));
	            	list.add(md);
	            }
	            System.out.println("데이터 로드 성공!");
	            return list;
	        } catch (Exception e) {            
	           System.out.println("데이터 로드 실패!");
	           return null;
	        } 
	}
	
	public void menuDelete(int no) {
		String sql = "delete from menutbl where menuNum = ?;";
		PreparedStatement pstmt = null;
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, no);
			pstmt.executeUpdate();
			System.out.println("데이터 삭제 성공");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("데이터 삭제 실패");
		}
	}
	
	private void menuUpdate(MenuData md) {
	      System.out.println(md.getImage());
	      String sql = "";
	      if(md.getImage() != null) {
	         sql = "update menutbl set category = ?, mname = ?, mprice = ?, mimage = ? where menuNum = ?;";
	      }else {
	         sql = "update menutbl set category = ?, mname = ?, mprice = ? where menuNum = ?;";
	      }
	      PreparedStatement pstmt = null;
	      try {
	         pstmt = conn.prepareStatement(sql);
	         pstmt.setString(1, md.getCategory());
	         pstmt.setString(2, md.getName());
	         pstmt.setInt(3, md.getPrice());
	         if(md.getImage() != null) {
	            pstmt.setBytes(4, md.getImage());
	            pstmt.setInt(5, md.getNo());
	         }else {
	            pstmt.setInt(4, md.getNo());
	         }
	         pstmt.executeUpdate();
	         System.out.println("데이터 수정 성공");
	      } catch (Exception e) {
	         e.printStackTrace();
	         System.out.println("데이터 수정 실패");
	      }
	   }
	
	public void inTmpSave(int total) {
		
		  String sql = "insert into tmpSave values(?);";
	      PreparedStatement pstmt = null;
	      try {
	         pstmt = conn.prepareStatement(sql);
	         pstmt.setInt(1, total);
	         pstmt.executeUpdate();
	         System.out.println("데이터 수정 성공");
	      } catch (Exception e) {
	         e.printStackTrace();
	         System.out.println("데이터 수정 실패");
	      }
	}
	
	public void delTmpSave() {
		
		  String sql = "delete from tmpSave;";
	      PreparedStatement pstmt = null;
	      try {
	         pstmt = conn.prepareStatement(sql);
	         pstmt.executeUpdate();
	         System.out.println("데이터 수정 성공");
	      } catch (Exception e) {
	         e.printStackTrace();
	         System.out.println("데이터 수정 실패");
	      }
	}
	
	public int selTmpSave() {
		String sql = "select * from tmpSave;";
		PreparedStatement pstmt = null;
		int tmp = 0;
		try {
    		pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
            	tmp=(rs.getInt(1));
            }
            System.out.println("데이터 로드 성공!");
        } catch (Exception e) {            
           System.out.println("데이터 로드 실패!");
        }
		return tmp; 
	}
	
}
