import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
//import java.sql.ResultSetMetaData;
//import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.*;

public class ConcurrentAlignment {
	
    private static String dbURL;
    private static String tableName = "dnaseqs.realtable1";
    
    // jdbc Connection
    public static Connection conn = null;
    
    public static Statement stmt = null;
    
    public static ResultSet results;
    
    // 1000 LONG
//    public static String target = "ATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACAT";
    
    // 750 LONG
//    public static String target = "ATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATC";

    // 500 LONG
//    public static String target = "ATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACAT";

    // 100 LONG
    public static String target = "ATCGATCAAGGATCTCGATACGATCGACTAGCTACGACTACATTTACGATCTAGGAGGCCCTAGATCGATCAGCTACGACTACATCGATCAAGGATCTCG";
    
    public static CleanLazyList finalResults= new CleanLazyList();
//    public static AlignResult testResult;
    
    public int me;
    public static Lock lock = new ReentrantLock();


	public ConcurrentAlignment(int newMe) {
		me = newMe;
	}
	
	public static void main(String[] args){
		
        
        long startTime = System.nanoTime();
        System.out.println(tableName);



        String os = System.getProperty("os.name");
        if (os.startsWith("Linux")){
            dbURL = "jdbc:derby:../MyDB;";
        }
        else{
            dbURL = "jdbc:derby:MyDB;";
        }
        createConnection();
        
        try {
			stmt = conn.createStatement();
		} catch (SQLException e2) {
			e2.printStackTrace();
		}
        
		try {
			ResultSet results = stmt.executeQuery("select * from " + tableName);
			BlockingQueue<DbResult> queue1 = new LinkedBlockingQueue<DbResult>(20);
	        Producer producer = new Producer(queue1, results);
	        //Splitter splitter = new Splitter(queue1, target);
	        
	        Thread p1 = new Thread(producer);
//	        Thread p2 = new Thread(producer);
	        Thread s1 = new Thread(new Splitter(queue1, target));
	        Thread s2 = new Thread(new Splitter(queue1, target));
	        Thread s3 = new Thread(new Splitter(queue1, target));
	        Thread s4 = new Thread(new Splitter(queue1, target));
	        
	        p1.start();
//	        p2.start();
	        s1.start();
	        s2.start();
	        s3.start();
	        s4.start();
	        
	        try{
	        	p1.join();
//	        	p2.join();
	        	s1.join();
	        	s2.join();
	        	s3.join();
	        	s4.join();
	        } catch(InterruptedException e) {}
	        
            

            for (int i = 1; i <= finalResults.size(); i++)
            {
                System.out.println( i +" "+ finalResults.get(i).geneName + " = " + finalResults.get(i).alignmentScore);
            }
                    
                    
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

        try{
            //results.close();
            stmt.close();
        }
        catch (SQLException sqlExcept){
            sqlExcept.printStackTrace();
        }
        
		shutdown();
        
        
        long endTime = System.nanoTime();
        System.out.println("That took " + (endTime - startTime)/1000000 + " milliseconds");
        
        
        
	}
	private static void createConnection()
    {
        try{
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
            //Get a connection
            conn = DriverManager.getConnection(dbURL); 
        }
        catch (Exception except){
            except.printStackTrace();
        }
   }
   
    private static void shutdown()
    {
        try{
            if (stmt != null){
                stmt.close();
            }
            if (conn != null){
                DriverManager.getConnection(dbURL + ";shutdown=true");
                conn.close();
            }           
        }
        catch (SQLException sqlExcept){
        	// This will always happen, so we don't need to print anything, really 
            //sqlExcept.printStackTrace();
        }
    }
	
}
