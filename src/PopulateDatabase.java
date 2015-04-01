import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSetMetaData;
import java.util.*;


public class PopulateDatabase
{
  
    private static String dbURL;
    private static String tableName = "dnaseqs.realtable1";
    // jdbc Connection
    private static Connection conn = null;
    private static Statement stmt = null;

    static final String AB = "ACGT";
    static Random rnd = new Random();

    public static String randomString(int len) 
    {
       StringBuilder sb = new StringBuilder(len);
       for(int i=0; i<len; i++) 
          sb.append(AB.charAt(rnd.nextInt(AB.length())));
       return sb.toString();
    }

    public static void main(String[] args)
    {
        String os = System.getProperty("os.name");
        String name;
        if (os.startsWith("Linux")){
            dbURL = "jdbc:derby:../MyDB;";
        }
        else{
            dbURL = "jdbc:derby:MyDB;";
        }
        createConnection();
        for(int i=1; i<=100; i++)
        {
            name = "Gene" + i;
            insertGenes(i, name, randomString(20000));
        }
        //selectGenes();
        shutdown();
    }
    
    private static void createConnection()
    {
        try
        {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
            //Get a connection
        	//EmbeddedDriver driver = new EmbeddedDriver();
            conn = DriverManager.getConnection(dbURL); 
        }
        catch (Exception except)
        {
            except.printStackTrace();
        }
   }
    
    private static void insertGenes(int id, String geneName, String sequence)
    {
        try
        {
            stmt = conn.createStatement();
            stmt.execute("insert into " + tableName + " values (" +
                    id + ",'" + geneName + "','" + sequence +"')");
            stmt.close();
        }
        catch (SQLException sqlExcept)
        {
            sqlExcept.printStackTrace();
        }
    }
    
    private static void selectGenes()
    {
        try
        {
            stmt = conn.createStatement();
            ResultSet results = stmt.executeQuery("select * from " + tableName);
            ResultSetMetaData rsmd = results.getMetaData();
            int numberCols = rsmd.getColumnCount();
            for (int i=1; i<=numberCols; i++)
            {
                //print Column Names
                System.out.print(rsmd.getColumnLabel(i)+"\t\t");  
            }

            System.out.println("\n-------------------------------------------------");

            while(results.next())
            {
                int id = results.getInt(1);
                String geneName = results.getString(2);
                String sequence = results.getString(3);
                System.out.println(id + "\t\t" + geneName + "\t\t" + sequence);
            }
            results.close();
            stmt.close();
        }
        catch (SQLException sqlExcept)
        {
            sqlExcept.printStackTrace();
        }
    }
    
    private static void shutdown()
    {
        try
        {
            if (stmt != null)
            {
                stmt.close();
            }
            if (conn != null)
            {
                DriverManager.getConnection(dbURL + ";shutdown=true");
                conn.close();
            }           
        }
        catch (SQLException sqlExcept)
        {
            
        }

    }
}
