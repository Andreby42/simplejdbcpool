package xyz.spacexplore;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class PoolImpl  implements Pool{

	
	private String driver;
	private String url;
	private String user;
	private String password;
	
	private static final String JDBC_URL="jdbc.url";
	private static final String JDBC_DRIVER="jdbc.driver";
	private static final String JDBC_USER="jdbc.user";
	private static final String JDBC_PASSWORD="jdbc.password";
	private static final String JDBC_INITSIZE="jdbc.initsize";

	private volatile int connectionTimeOut=2000;
    private volatile long maxLifetime=1000;
    private volatile int maxPoolSize=2000;
    private volatile int minIdle=100;
    private volatile int initSize=100;
    private volatile int increSize=5;

	
	public int getIncreSize() {
		return increSize;
	}

	public void setIncreSize(int increSize) {
		this.increSize = increSize;
	}

	public int getConnectionTimeOut() {
		return connectionTimeOut;
	}

	public void setConnectionTimeOut(int connectionTimeOut) {
		this.connectionTimeOut = connectionTimeOut;
	}

	public long getMaxLifetime() {
		return maxLifetime;
	}

	public void setMaxLifetime(long maxLifetime) {
		this.maxLifetime = maxLifetime;
	}

	public int getMaxPoolSize() {
		return maxPoolSize;
	}

	public void setMaxPoolSize(int maxPoolSize) {
		this.maxPoolSize = maxPoolSize;
	}

	public int getMinIdle() {
		return minIdle;
	}

	public void setMinIdle(int minIdle) {
		this.minIdle = minIdle;
	}

	public int getInitSize() {
		return initSize;
	}

	public void setInitSize(int initSize) {
		this.initSize = initSize;
	}

	private static CopyOnWriteArrayList<PoolConnection> POOL = new CopyOnWriteArrayList<>();
	
	public String getDriver() {
		return driver;
	}

	public void setDriver(String driver) {
		this.driver = driver;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public PoolImpl() {
		super();
		initPool();
		
	}

	private void initPool() {
		//读取静态配置文件 复制到成员属性
		InputStream in = this.getClass().getClassLoader().getResourceAsStream("jdbc.properties");
		Properties pro = new Properties();
		try {
			pro.load(in);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("初始化连接池... 读取配置文件失败....");
		}
		try {
			driver = pro.getProperty(JDBC_URL);
			url=pro.getProperty(JDBC_DRIVER);
			user=pro.getProperty(JDBC_USER);
			password=pro.getProperty(JDBC_PASSWORD);
			initSize=Integer.valueOf(pro.getProperty(JDBC_INITSIZE));
		} catch (NumberFormatException e) {
			e.printStackTrace();
			System.out.println("初始化连接池... 读取配置文件属性失败....");
		}
		//create连接 将连接加入到并发容器中
		createConnection(initSize);
	}

	@Override
	public  Connection getConnection() {
		if(POOL.size()==0){
			//如果连接池中没有可用连接 再次實例化池
			createConnection(initSize);
		}
		PoolConnection poolconnection = getRealConnection();
		while (poolconnection==null) {
			try {
	            createConnection(increSize);
	            poolconnection=getRealConnection(); 
			} catch (Exception e) {
			}
		}
		Connection  connection= poolconnection.getConnection();
		
		 poolconnection.getBusy().set(true);
		return connection;
	}
	

	private synchronized PoolConnection  getRealConnection() {
		for (PoolConnection poolConnection : POOL) {
			if (!poolConnection.getBusy().get()) {
				Connection connection = poolConnection.getConnection();
				try {
					if (!connection.isValid(3000)) {
						connection=DriverManager.getConnection(url, user, password);
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
				return poolConnection;
			}
		}
		//连接被占用完毕重新创建
		return null;
	}

	@Override
	public void createConnection( int initSize) {
		for (int i=0; i<initSize;i++) {
			//当池子的大小大于最大连接数 break 不再创建连接
			if (this.maxPoolSize > 0    
					&& this.POOL.size()>= this.maxPoolSize) {     
				
				break;     
			}     
		 newConnection();
	}
}

	private Connection newConnection() {
		Connection	connection=null;
		try {
				Class.forName(driver) ;    
					connection=DriverManager.getConnection(url, user, password);
				if (POOL.size()==0) {
					DatabaseMetaData metaData = connection.getMetaData();
					//获得底层数据库所支持的最大连接数
					int maxConnections = metaData.getMaxConnections();
					 if (maxConnections> 0    
			                    && this.maxPoolSize > maxConnections) {     
			    
			                this.maxPoolSize = maxConnections;     
			            }     
				}
				PoolConnection poolConnection = new PoolConnection(connection);
				POOL.add(poolConnection);
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("初始化創建連接池失敗。。。。。数据库配置错误。。。");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.out.println("初始化創建連接池失敗。。。。。未找到数据库驱动包。。。。");
	
		}
			return connection;
		}

}
