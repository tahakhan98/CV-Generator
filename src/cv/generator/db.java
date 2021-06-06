
package cv.generator;


import java.sql.*;
import javax.swing.*;
public class db {
    
    
    
    Connection getConnection=null;
    public static Connection java_db(){
        
        try{
            Class.forName("org.sqlite.JDBC");
            Connection connection =DriverManager.getConnection("jdbc:sqlite:C:\\Users\\Hyrex\\Documents\\NetBeansProjects\\CV Generator\\cvdatabase.sqlite");
            //JOptionPane.showMessageDialog(null, "Connection to database is successful");
      
            return connection;
           
            
        }catch (Exception e){
            JOptionPane.showMessageDialog(null, e);
            return null;
        }
        
    }
}
