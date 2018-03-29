package com.ynr.core;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class MysqlUtils {

	public static final String MYSQL_URL = "jdbc:mysql://117.48.211.42:3306/autofillform?useUnicode=true&characterEncoding=UTF-8";
	public static final String MYSQL_PASSWD = "Cckzcbm110";
	public static final String MYSQL_USER = "root";
	
	static {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			System.out.println("MysqlUtils : " + e.toString());
		}
	}
	
	public static boolean updateTaskRecordStatus(long id, String status){
		boolean updateSuccess = false;
		try {
			Connection conn = DriverManager.getConnection(MYSQL_URL, MYSQL_USER, MYSQL_PASSWD);
			Statement stmt = conn.createStatement();
			String sql = "update task set status='"+status+"' where id=" + id;
            int ret = stmt.executeUpdate(sql);
            if(ret > 0) updateSuccess = true;
		} catch (SQLException e) {
			System.out.println("updateTaskRecordStatus" + e.toString());
		}
		return updateSuccess;
	}
}
