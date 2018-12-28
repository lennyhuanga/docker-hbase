package com.jxy.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * phoenix工具类
 * Author lennyhuang
 */


public class PhoenixUtil {

	    public static void main(String arg[]) throws SQLException {
	    	  System.setProperty("hadoop.home.dir", "D:\\\\hadoop-2.7.2");
	    	  Connection conn = null;
	          Statement state = null;
	          ResultSet rs = null;
	          try {
	              Class.forName("org.apache.phoenix.jdbc.PhoenixDriver");
	              conn = DriverManager.getConnection("jdbc:phoenix:hadoop-master,hadoop-slave1,hadoop-slave2:2181");
	              state = conn.createStatement();
	              rs=  state.executeQuery("select * from itinfo");

	              while(rs.next()){
	                  System.out.println("no:"+rs.getString("id"));
	                  System.out.println("name:"+rs.getString("name"));
	                  System.out.println("age:"+rs.getInt("age"));
	                  System.out.println("-------------------------");
	              }

	          }catch (Exception e) {
	              e.printStackTrace();
	          } finally {
	              if (rs != null) rs.close();
	              if (state != null) state.close();
	              if (conn != null) conn.close();
	          }

	    }


}