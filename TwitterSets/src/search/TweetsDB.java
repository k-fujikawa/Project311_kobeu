package search;

import java.net.UnknownHostException;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

public class TweetsDB {
	Mongo m;
	DB db;

	public TweetsDB() {
		try {
			m = new Mongo();
			db = m.getDB("tweet_database");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	public DBObject getTweet(String docno) {
		DBCollection coll = db.getCollection("htmls");
		BasicDBObject query = new BasicDBObject();
		query.put("tweetid", docno);
		DBCursor cursor = coll.find(query);

		try {
			while (cursor.hasNext()) {
				// System.out.println(cursor.next());
				return cursor.next();
			}
		} finally {
			cursor.close();
		}
		return null;
	}

	public Double getLang(String docno) {
		DBCollection coll = db.getCollection("language");
		BasicDBObject query = new BasicDBObject();
		query.put("tweetid", docno);
		DBCursor cursor = coll.find(query);
		try {
			while (cursor.hasNext()) {
				DBObject obj = cursor.next();
				return (Double) obj.get("en");
			}
		} finally {
			cursor.close();
		}
		return null;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TweetsDB db = new TweetsDB();
		String docno = "34488644131762176";
		DBObject tweet = db.getTweet(docno);
		System.out.println(tweet);
		System.out.println(tweet.get("date"));
		System.out.println(db.getLang(docno));
	}
}
