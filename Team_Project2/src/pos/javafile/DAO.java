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
            System.out.println("����̹� �ε� ����!!");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("����̹� �ε� ����!!");
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
	            System.out.println("������ ���� ����!");
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
	            System.out.println("������ �ε� ����!");
	            return d;
	        } catch (Exception e) {            
	           System.out.println("������ �ε� ����!");
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
	            System.out.println("������ ���� ����");
	        } catch (Exception e) {            
	        	System.out.println("������ ���� ����");
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
	            System.out.println("������ ���� ����!");
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
	            System.out.println("������ �ε� ����!");
	            return list;
	        } catch (Exception e) {            
	           System.out.println("������ �ε� ����!");
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
			System.out.println("������ ���� ����");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("������ ���� ����");
		}
	}
	
	private void menuUpdate(MenuData md) {
		String sql = "update menutbl set category = ?, mname = ?, mprice = ?, mimage = ? where menuNum = ?;";
		PreparedStatement pstmt = null;
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, md.getCategory());
			pstmt.setString(2, md.getName());
			pstmt.setInt(3, md.getPrice());
			pstmt.setBytes(4, md.getImage());
			pstmt.setInt(5, md.getNo());
			pstmt.executeUpdate();
			System.out.println("������ ���� ����");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("������ ���� ����");
		}
	}
	
}
