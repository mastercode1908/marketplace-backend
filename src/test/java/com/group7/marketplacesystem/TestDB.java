package com.group7.marketplacesystem;

import java.sql.Connection;
import java.sql.DriverManager;

public class TestDB {
    public static void main(String[] args) throws Exception {
        try {
            // Load driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Kết nối tới DB
            String url = "jdbc:mysql://localhost:3306/OnlineMarketPlaceSystem3?allowPublicKeyRetrieval=true&useSSL=false";
            String username = "root";      // thay bằng user DB của bạn
            String password = "root";    // thay bằng password DB của bạn

            Connection conn = DriverManager.getConnection(url, username, password);
            System.out.println("Kết nối thành công!");
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
