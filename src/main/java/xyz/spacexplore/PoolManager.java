package xyz.spacexplore;
/**
 * 单例池管理器获得池对象
 * @author ander
 *
 */
public enum PoolManager {
		INSTANCE;
		private  Pool pool ;
		PoolManager() {
			pool = new PoolImpl();
	    }
	    public Pool getInstance() {
	        return pool;
	    }
	}
