package com.jxy.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;



public class MysqlUtil {


	public static void main(String args[]) {
//		initMysql();
		
		getPareByTableName("tbl_user_finance_log_user");
	}
	
	public static StringBuffer getPareByTableName(String tableName) {
		
		StringBuffer sb = new StringBuffer();
		try {
			Connection conn = openLink();
			PreparedStatement statement = conn.prepareStatement("desc "+tableName);
			ResultSet rs = statement.executeQuery();
			while(rs.next()){
				String name = rs.getString(1);
				sb.append(name+",");
			}
			System.out.println(sb);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sb;
	}
    
	private static String username= "zzt";
	private static String password= "123456";
	private static String url= "jdbc:mysql://192.168.11.29:3306/diggle_logger?serverTimezone=GMT%2B8";
	private static String sqlDriver= "com.mysql.cj.jdbc.Driver";  
	private static Connection conn;
	private static Statement stm;
	private static ResultSet rs;
	

	
	/*
	 * 数据库连接
	 */
	public static Connection openLink()
	{
		try
		{
			Class.forName(sqlDriver);
			conn = DriverManager.getConnection(url,username,password);
		}
		catch(Exception e)
		{
			System.out.println("数据库连接异常");
		}
		return conn;
	}
	/*
	 * 执行sql查询语句
	 */
	public void query(String sql)
	{
		//this.sql = sql;
		try
		{
			this.stm = this.conn.createStatement();
			this.rs = this.stm.executeQuery(sql);
			//System.out.println(this.rs);
		}
		catch(Exception e)
		{
			System.out.println("查询操作异常");
		}
	}
	/*
	 * 执行查询操作并返回一个ResultSet结果集
	 */
	public ResultSet select(String sql)
	{
		openLink();
		query(sql);
		return this.rs;
		
	}
	/*
	 * 数据库更新操作
	 */
	 public void Update(String sql)
	 {
		 openLink();
		 try
		 {
		 this.stm = this.conn.createStatement();
		 this.stm.executeUpdate(sql);
		 }
		 catch(Exception e)
		 {
			 System.out.println("更新操作异常");
		 }
	 }
	 /*
	  * 关闭数据库查询操作
	  */
	 public void closeSelect()
	 {
		 try
		 {
			 stm.close();
			 rs.close();
			 conn.close();
		 }
		 catch(Exception e)
		 {
			 System.out.println("数据库关闭异常");
		 }
	 }
	 /*
	  * 关闭数据库更新操作
	  */
	 public void closeUpdate()
	 {
		 try
		 {
			 stm.close();
			 conn.close();
		 }
		 catch(Exception e)
		 {
			 System.out.println("数据库关闭异常");
		 }
	 }

	  /*  // JDBC 驱动名及数据库 URL
	    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";  
	    static final String DB_URL = "jdbc:mysql://192.168.11.29:3306/diggle_logger?serverTimezone=GMT%2B8";
	 
	    // 数据库的用户名与密码，需要根据自己的设置
	    static final String USER = "zzt";
	    static final String PASS = "123456";
		public static Connection initMysql() {
	   	 Connection conn = null;
	        Statement stmt = null;
	        try{
	            // 注册 JDBC 驱动
	            Class.forName("com.mysql.cj.jdbc.Driver");
	        
	            // 打开链接
	            System.out.println("连接数据库...");
	            conn =  DriverManager.getConnection(DB_URL,USER,PASS);
	        
	            // 执行查询
	            System.out.println(" 实例化Statement对象...");
	            stmt = conn.createStatement();
	            String sql;
	            sql = "SELECT id, userid, amount FROM tbl_user_finance_log_user where id = 905";
	            ResultSet rs = stmt.executeQuery(sql);
	        
	            // 展开结果集数据库
	            while(rs.next()){
	                // 通过字段检索
	                int id  = rs.getInt("id");
	                String userid = rs.getString("userid");
	                String amount = rs.getString("amount");
	    
	                // 输出数据
	                System.out.print("ID: " + id);
	                System.out.print(", 用户编号: " + userid);
	                System.out.print(", 账户余额: " + amount);
	                System.out.print("\n");
	            }
	            // 完成后关闭
	            rs.close();
	            stmt.close();
	            conn.close();
	        }catch(SQLException se){
	            // 处理 JDBC 错误
	            se.printStackTrace();
	        }catch(Exception e){
	            // 处理 Class.forName 错误
	            e.printStackTrace();
	        }finally{
	            // 关闭资源
	            try{
	                if(stmt!=null) stmt.close();
	            }catch(SQLException se2){
	            }// 什么都不做
	            try{
	                if(conn!=null) conn.close();
	            }catch(SQLException se){
	                se.printStackTrace();
	            }
	        }
	        System.out.println("Goodbye!");
	        return conn;
	    }
	    */
}

