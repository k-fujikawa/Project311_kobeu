package cs24.kitaguchi.prorin.hellomongodb;

import java.net.UnknownHostException;
import java.util.List;
import java.util.Set;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

public class HelloMongodb {

  /**
   * @param args
   * @throws MongoException
   * @throws UnknownHostException
   */
  public static void main(String[] args) {
    String dbName = "helloDB";
    String colName = "hello";

    try {
      // **********データベース接続とコレクション取得 ***************
      // 接続
      //Mongo m = new Mongo("localhost", 27017);
      Mongo m = new Mongo();
      // データベース"hello"を削除というか，初期化
      System.out.println("データベース" + dbName + "を削除します．");
      m.dropDatabase(dbName);

      // データベース取得
      System.out.println("データベース" + dbName + "を取得します．");
      DB db = m.getDB(dbName);
      // データベースは0個以上のコレクションを持ってる．コレクションのリストを取得する．
      Set<String> colls = db.getCollectionNames();
      for (String s : colls) {
        System.out.println(s);
      }
      // コレクションを取得
      System.out.println("コレクション" + colName + "を取得します．");
      DBCollection coll = db.getCollection(colName);

      // ************ ドキュメントをコレクションに加える *************
      /* ドキュメントの挿入（以下のように構造の中に構造を入れることが可能）
      {
        "name" : "MongoDB",
        "type" : "database",
        "count" : 1,
        "info" : {
                    x : 203,
                    y : 102
                  }
      }
      */
      // docっていうオブジェクト作るよ
      BasicDBObject doc = new BasicDBObject();
      // doc.put(キー,値)
      doc.put("name", "MongoDB");
      doc.put("type", "database");
      doc.put("count", 1);
      // infoっていうオブジェクトも作るよ
      BasicDBObject info = new BasicDBObject();
      info.put("x", 203);
      info.put("y", 102);
      // doc内にinfoをいれちゃうよ
      doc.put("info", info);
      // コレクションに加える
      coll.insert(doc);

      // ************ ドキュメントの検索 *************
      // 単一ドキュメントを検索する
      DBObject myDoc = coll.findOne();
      System.out.println(myDoc);

      //*******　注意 *********
      //*"_"とか"$"とかで始まる　*
      //*オブジェクト名は予約されて*
      //*いるから使わないで      *
      //**********************

      // ****** 要素が1つだけのドキュメントみたいなのをいっぱい加えよう ******
      /* つまりこういうの
       *  {
       *     "i" : value
       *  }
       */
      for (int i=0; i < 100; i++) {
        coll.insert(new BasicDBObject().append("i", i));
      }
      // ほら違う構造の・・・スキーマレスだからね！ってくだりはPythonでやったよね．

      /* ここでfindOne()したらどうなるかしら．
       * 　結果：さっきと同じ出力でした．
      myDoc = coll.findOne();
      System.out.println(myDoc);
      */

      //コレクション内のドキュメントを数える
      System.out.println(coll.getCount());

      // 全てのドキュメントを取得する
      DBCursor cur = coll.find();

      while(cur.hasNext()) {
        System.out.println(cur.next());
      }

      /* $ではじまるのはやはり範囲を示すっぽい|英語の壁
       * おそらく，
       *    $ne ------> !=    (not equal)
       *    $in ------>
       *    $nin ----->
       *    $nor
       *    $or
       *    $and ----->
       *    $gt ------> >     (greater than : i > value)
       *    $gte -----> >=    (greater than or equal : i >= value)
       *    $lt ------> <     (less than : i < value)
       *    $lte -----> <=    (less than or equal : i <= value)
       *    $all ----->
       *    $exists --> 指定のフィールドを持つオブジェクトが存在するか(true/false)
       *    $mod ----->
       *    $size
       *    $tyep
       */
      BasicDBObject query = new BasicDBObject();
      // i>10 i!=30 の順でputすると30が結果に出力されない．
      // i!=30 i>10 の順だと30が出力される．なんで？
      query.put("i", new BasicDBObject("$gt", 10)); // i > 10
      query.put("i", new BasicDBObject("$ne", 30)); // i != 30
      //query.put("i", new BasicDBObject("$gt", 10)); // i > 10
      cur = coll.find(query);
      System.out.println("検索結果を表示します．");
      while(cur.hasNext()) {
        System.out.println(cur.next());
      }

      //　検索結果のセットを受け取る？
      query = new BasicDBObject();
      query.put("i", new BasicDBObject("$gt", 50)); // i > 50
      cur = coll.find(query);
      System.out.println("\n\ni > 50を出力します．");
      while(cur.hasNext()) {
        System.out.println(cur.next());
      }

      // 因縁の範囲クエリ
      // appendはaddするもの．
      System.out.println("\n\n20 < i <= 30を出力します．");
      query = new BasicDBObject();
      query.put("i", new BasicDBObject("$gt", 20).append("$lte", 30));
      cur = coll.find(query);
      while(cur.hasNext()) {
        System.out.println(cur.next());
      }

      // インデックスを作る
      coll.createIndex(new BasicDBObject("i", 1)); // iに上昇的なindexをつける
      // コレクションにおけるインデックスのリストを取得
      List<DBObject> list = coll.getIndexInfo();
      System.out.println("\n\n　indexのリストを表示");
      for (DBObject o : list) {
        System.out.println(o);
      }

      // データベースのリストを取得する
      System.out.println("\n\nデータベースのリストを取得");
      for (String str : m.getDatabaseNames()) {
        System.out.println(str);
      }


    } catch (UnknownHostException e) {
      // TODO 自動生成された catch ブロック
      e.printStackTrace();
    } catch (MongoException e) {
      // TODO 自動生成された catch ブロック
      e.printStackTrace();
    }
  }

}
