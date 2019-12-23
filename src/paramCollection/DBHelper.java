package paramCollection;

import java.sql.*;
import java.util.concurrent.CountDownLatch;

public class DBHelper {
	// MySQL 8.0 以下版本 - JDBC 驱动名及数据库 URL
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
    static final String DB_URL = "jdbc:mysql://localhost:3306/parameter_semantic";
 
    // 数据库的用户名与密码，需要根据自己的设置
    static final String USER = "root";
    static final String PASS = "13962789176qwe";
    
    private Connection conn = null;

	public Connection getConn() {
		return conn;
	}

	public Statement getStmt() {
		return stmt;
	}

	private Statement stmt = null;



	public void getConnection() {
    	//1.加载驱动
        try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			System.out.println("未能成功加载驱动程序，请检查是否导入驱动程序！");
                        //添加一个语句，如果加载驱动异常，检查是否添加驱动，或者添加驱动字符串是否错误
			e.printStackTrace();
		}
		
		//2.连接数据库
		try {
			conn = DriverManager.getConnection(DB_URL,USER,PASS);
//		    System.out.println("获取数据库连接成功！");
		} catch (SQLException e) {
			System.out.println("获取数据库连接失败！");
                        //添加一个语句，如果连接失败，检查连接字符串或者登录名以及密码是否错误
			e.printStackTrace();
		}
    }
    
    /*
     * 执行查询
     */
    public int getResultNum(String sqlString) {
    	getConnection();
    	ResultSet rs = null;
    	int count = 0;
    	try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sqlString);
			while(rs.next()) {
				count+=1;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	//关闭连接
    	try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
			conn=null;
		}
    	return count;
    }
    
    /*
     * 执行增删改
     */
    public void execete(String sqlString) {
    	getConnection();
    	try {
			stmt = conn.createStatement();
			stmt.executeUpdate(sqlString);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	//关闭连接
    	try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
			conn=null;
		}
    }
    
    /*
     * 测试方法，没有用处
     */
	public void QueryTest()
	{
		getConnection();
		//3.进行查询
		try {
            stmt = conn.createStatement();
            String sql;
            sql = "SELECT tag,value,code from parameter_semantic";
            ResultSet rs = stmt.executeQuery(sql);
            // 展开结果集数据库
            while(rs.next()){
                // 通过字段检索
                String tag  = rs.getString("tag");
                String value = rs.getString("value");
                String code = rs.getString("code");
    
                // 输出数据
                System.out.print("tag: " + tag);
                System.out.print(", value: " + value);
                System.out.print(", code: " + code);
                System.out.print("\n");
            }
		} catch(SQLException e) {
			System.out.println("查询失败！请检查语句！");
            //添加一个语句，如果查询失败，检查SQL语句是否错误
			e.printStackTrace();
		}
            //查询完后就要关闭
		if(conn!=null)
		{
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
				conn=null;
			}
		}
	}
}
