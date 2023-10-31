package com.imss.sivimss.pagoanticipado.util;

import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;

@Service
public class Database {

	@Value("${spring.datasource.url}") 
	private String url;
	
	@Value("${spring.datasource.username}")
	private String envUser;
	
	@Value("${spring.datasource.password}")
	private String envPass;
	
	private static final Logger log = LoggerFactory.getLogger(Database.class);
	
    public Connection getConnection(){
        try{
        	DriverManagerDataSource dataSource = new DriverManagerDataSource();
        	dataSource.setDriverClassName("org.mariadb.jdbc.Driver");
            dataSource.setUrl(url);
            dataSource.setUsername(envUser);
            dataSource.setPassword(envPass);
         return dataSource.getConnection();
      }catch(SQLException e){
         log.info(e.getMessage());
      }
      return null;
   }
	
}
