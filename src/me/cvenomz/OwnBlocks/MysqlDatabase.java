package me.cvenomz.OwnBlocks;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MysqlDatabase {
    
    private OwnBlocks pluginRef;
    private String username;
    private String password;
    private String host;
    private String databaseName;
    private String tableName;
    private String url;
    private Connection conn;
    
    public MysqlDatabase() {}
    
    public MysqlDatabase(OwnBlocks ob, String host, String databaseName, String username, String password)
    {
        pluginRef = ob;
        this.host = host;
        this.databaseName = databaseName;
        this.username = username;
        this.password = password;
        
        this.tableName = "OwnBlocks";
    }
    
    public void establishConnection() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException
    {
        url = "jdbc:mysql://"+host+"/"+databaseName;
        Class.forName("com.mysql.jdbc.Driver").newInstance();
        conn = DriverManager.getConnection(url, username, password);
        //pluginRef.debugMessage("Connection attempt to database -- done");
    }
    
    public void closeConnection() throws SQLException
    {
        conn.close();
        //pluginRef.debugMessage("Connection close attempt -- done");
    }
    
    private boolean tableExists()throws Exception
    {
        Statement s = conn.createStatement();
        s.executeQuery("SHOW TABLES");
        ResultSet rs = s.getResultSet();
        boolean ret = false;
        while (rs.next())
        {
            if (rs.getString(1).equalsIgnoreCase(tableName))
                ret = true;
        }
        return ret;
    }
    
    public void CheckOBTable()throws Exception
    {
        Statement s = conn.createStatement();
        if (!tableExists())
            s.executeUpdate("CREATE TABLE OwnBlocks (id INT UNSIGNED NOT NULL UNIQUE AUTO_INCREMENT, PRIMARY KEY (id), world varchar(50), x INT NOT NULL, y INT NOT NULL, z INT NOT NULL, owner varchar(50) NOT NULL, allowed TEXT, tag varchar(50), time TIMESTAMP )");
    }
    
    public int addBlock(MysqlBlock mb)
    {
        int ret = -1;
        try {
            if (getBlock(mb) != null)   //check to see if block is already in database for some reason
                deleteBlock(mb);        //delete the block to prevent duplicates
        Statement s = conn.createStatement();
        String value = mb.toSQLString();
        ret = s.executeUpdate("INSERT INTO "+tableName+" (world, x, y, z, owner, allowed, tag) VALUES " + value);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            pluginRef.log.severe("[OwnBlocks] Failed to add block.  Probably SQL error");
            e.printStackTrace();
        }        
        return ret;
    }
    
    public MysqlBlock getBlock(MysqlBlock mb)
    {
        MysqlBlock ret = null;
        try {
            Statement s = conn.createStatement();
            s.executeQuery("SELECT * FROM "+tableName+" WHERE world="+mb.getWorld()+" AND x="+mb.getX()+" AND y="+mb.getY()+" AND z="+mb.getZ());
            ResultSet rs = s.getResultSet();
            while (rs.next())
            {
                ret = new MysqlBlock(rs.getString("world"), rs.getInt("x"), rs.getInt("y"), rs.getInt("z"), rs.getString("owner"), rs.getString("allowed"), rs.getString("tag"), rs.getTime("time"));
            }
        }catch (Exception e) {
            pluginRef.log.severe("[OwnBlocks] Failed to get block.  Probably SQL error");
            e.printStackTrace();
        }
        return ret;
    }
    
    public int deleteBlock(MysqlBlock mb)
    {
        int ret = -1;
        try {
            Statement s = conn.createStatement();
            ret = s.executeUpdate("DELETE FROM "+tableName+" WHERE world="+mb.getWorld()+" AND x="+mb.getX()+" AND y="+mb.getY()+" AND z="+mb.getZ());
        }catch (Exception e) {
            pluginRef.log.severe("[OwnBlocks] Failed to delete block.  Probably SQL error");
            e.printStackTrace();
        }
        return ret;
    }
    
    
}
