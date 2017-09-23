package sysu.coreclass.database.tool;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

/**
 * 连接工厂类，获得commit集合和class集合的对象
 * @author zhiyong
 *
 */
public class ConnectionFactory {
	
	private static MongoDatabase db = new MongoClient("192.168.2.168",27017).getDatabase("commitbase");
	private static MongoCollection<Document> classCollection = db.getCollection("class");
	private static MongoCollection<Document> commitCollection = db.getCollection("commit");
	
	public static MongoCollection<Document> getClassCollection(){
		if(classCollection!=null){
			return classCollection;
		}else{
			return db.getCollection("class");
		}
	}
	
	public static MongoCollection<Document> getCommitCollection(){
		if(commitCollection!=null){
			return commitCollection;
		}else{
			return db.getCollection("commit");
		}
	}
	
	public static MongoDatabase getDB(){
		if(db!=null){
			return db;
		}else{
			return new MongoClient("192.168.2.168",27017).getDatabase("commitbase");
		}
	}

}
