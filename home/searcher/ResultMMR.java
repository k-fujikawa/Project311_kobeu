package cs24.kitaguchi.Microblog.twitter.searcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;

public class ResultMMR {
  //private ArrayList<ScoreDoc>[] clusterList;
  private ArrayList<ArrayList<ScoreDoc>> clusterList;
  private Calendar[] timeList;
  private IndexSearcher searcher;
  private TimeParser timeParser;
  private int clusterNum;

  public ResultMMR(int clusterNum, IndexSearcher searcher, TimeParser timeParser, int cNum) {
    //clusterList = new ArrayList<ScoreDoc>[clusterNum];
    //コメントアウトした書き方だと，総称配列が作成できないそうだ．
    clusterList = new ArrayList<ArrayList<ScoreDoc>>();
    this.timeList = new Calendar[cNum];
    this.searcher = searcher;
    this.timeParser = timeParser;
    this.clusterNum = cNum;
  }

  /**
   * インデックスnのclusterに要素を挿入
   * @param scoreDoc 要素
   * @param n 挿入するクラスタのインデックス
   */
  public void addAtClusterN(ScoreDoc scoreDoc, int n) {
    if (n >= clusterNum) {
      System.err.println("error:存在しないクラスタに要素を挿入しようとしました．");
    }
    if(clusterList.size() <= n) {
      ArrayList<ScoreDoc> list = new ArrayList<ScoreDoc>(1);
      clusterList.add(list);
    }
      clusterList.get(n).add(scoreDoc);
  }

  /**
   * 指定したインデックスのclusterの，インデックスnの要素を削除
   * @param clusterIndex 削除するクラスタのインデクス
   * @param n 削除する要素のインデクス
   */
  public void removeAtClusterN(int clusterIndex, int n) {
    if (clusterIndex >= clusterNum) {
      System.err.println("error:そのインデクスのクラスタは存在しません．");
    }
    if (n < clusterList.get(clusterIndex).size()) {
      System.err.println("そのインデクスの要素は存在しません．");
    }
    clusterList.get(clusterIndex).remove(n);
  }

  /**
   * 指定されたインデックスのcluseter内の，指定されたインデックスのScoreDocを返す．
   * @param clusterIndex クラスタのindex
   * @param scoreDocIndex ScoreDocのインデックス
   * @return ScoreDoc
   */
  public ScoreDoc getScoreDocAtFrom(int clusterIndex, int scoreDocIndex) {
    return clusterList.get(clusterIndex).get(scoreDocIndex);
  }

  /**
   * 指定されたインデックスのclusterの要素数を返す．
   * @param clusterIndex クラスタのindex
   * @return 指定されたクラスタの要素数
   */
  public int sizeAt(int clusterIndex) {
    return clusterList.get(clusterIndex).size();
  }

  /**
   * clusterListを外部に取得させる
   * @return
   */
  public ArrayList<ArrayList<ScoreDoc>> getClusterList() {
    return clusterList;
  }

  /**
   * clusterListの要素についてiteratorを返す
   * @return
   */
  public Iterator<ArrayList<ScoreDoc>> iteratorClusterList() {
    return clusterList.iterator();
  }

  /**
   * clusterList内のcluseterの要素についてiteratorを返す
   * @param n
   * @return
   */
  public Iterator<ScoreDoc> iteratorClusterAt(int n) {
    return clusterList.get(n).iterator();
  }

  /**
   * 時刻の平均値を求めて配列にセットする．
   */
  public void setTimeList() {
    for(int i=0; i < clusterNum; i++) {
      timeList[i] = averageTime(i);
    }
  }

  /**
   * 指定されたインデックスのクラスタの，時刻の平均値をCalendarオブジェクトで返す．<br>
   * これが，そのクラスタの平均時刻となる．
   * @param clusterIndex クラスタのインデックス
   * @return 平均時刻Calendar
   */
  public Calendar averageTime(int clusterIndex) {
    ScoreDoc scoreDoc1 = getScoreDocAtFrom(clusterIndex, 0);
    Calendar calendar1 = Calendar.getInstance();
    long[] diffAry = new long[4];
    for (int i = 0; i < 4; i++) {
      diffAry[i] = 0;
    }
    try {
      // 1つめの時刻(Calendar)を取得．これを基準とする
      calendar1 = timeParser.parseCreatedAt(searcher.doc(scoreDoc1.doc).get("created_at"));
      // 2つめ以降の時刻の基準との差を足してゆく．
      for (int i = 1; i < clusterList.get(clusterIndex).size(); i++) {
        ScoreDoc scoreDoc2 = getScoreDocAtFrom(clusterIndex, i);
        Calendar calendar2 = Calendar.getInstance();
        // 2つめの時刻(Calendar)を取得
        calendar2 = timeParser.parseCreatedAt(searcher.doc(scoreDoc2.doc).get("created_at"));
        // 時刻の差を取得
        long[] diffAry2 = TimeParser.diffTime(calendar1, calendar2);
        // 加算
        for(int j=0; j < 4; j++) {
          diffAry[j] += diffAry2[j];
        }
      }
      for(int i = 0; i < 4; i++) {
        diffAry[i] = diffAry[i] / clusterList.get(clusterIndex).size();
      }
      calendar1.add(Calendar.DAY_OF_MONTH, (int) diffAry[0]);
      calendar1.add(Calendar.HOUR, (int) diffAry[1]);
      calendar1.add(Calendar.MINUTE, (int) diffAry[2]);
      calendar1.add(Calendar.SECOND, (int) diffAry[3]);
    } catch (CorruptIndexException e) {
      // TODO 自動生成された catch ブロック
      e.printStackTrace();
    } catch (IOException e) {
      // TODO 自動生成された catch ブロック
      e.printStackTrace();
    }
    return calendar1;
  }

  /**
   * クラスタのArrayListおよび，平均時刻の配列をソートする
   */
  public void sortTime() {
    for(int i=0; i < clusterNum; i++) {
      for(int j= i+1; j < clusterNum; j++) {
        if (timeList[i].compareTo(timeList[j]) < 0) { // timeList[i]がtimeList[j]の前
          // timeList入れ替え
          Calendar tempTime = timeList[i];
          timeList[i] = timeList[j];
          timeList[j] = tempTime;
          // ArrayList入れ替え
          ArrayList<ScoreDoc> tempAi = clusterList.get(i);
          ArrayList<ScoreDoc> tempAj = clusterList.get(j);
          clusterList.remove(i);
          clusterList.add(i, tempAj);
          clusterList.remove(j);
          clusterList.add(j, tempAi);
        }
      }
    }
  }

  /**
   * インデックスで指定されたクラスタの代表時刻をCalendarオブジェクトで返す．
   * @param clusterIndex
   * @return
   */
  public Calendar getClusterTime(int clusterIndex) {
    return timeList[clusterIndex];
  }

  public Calendar[] getTimeList() {
    return timeList;
  }

  public void filteringByTime() {
    for(int i=0; i < clusterNum; i++) {
      Calendar startCal = Calendar.getInstance();
      Calendar endCal = Calendar.getInstance();
      startCal.setTimeInMillis(timeList[i].getTimeInMillis());
      startCal.add(Calendar.DAY_OF_MONTH, -2);
      endCal.setTimeInMillis(timeList[i].getTimeInMillis());
      endCal.add(Calendar.DAY_OF_MONTH, 2);
      int size = clusterList.get(i).size();
      for(int j=0; j<size; j++) {
        try {
          Document doc = searcher.doc((clusterList.get(i).get(j).doc));
          Calendar docCal = Calendar.getInstance();
          docCal = timeParser.parseCreatedAt(doc.get("created_at"));
          TimeParser.show(docCal);
          TimeParser.show(endCal);
          System.out.print("\n");
          if(docCal.compareTo(startCal) < 0) {
            System.out.println("11111");
            removeAtClusterN(i, j);
            j--;
            size--;
          } else if(docCal.compareTo(endCal) > 0) {
            System.out.println("22222");
            removeAtClusterN(i, j);
            j--;
            size--;
          }
        } catch (CorruptIndexException e) {
          // TODO 自動生成された catch ブロック
          e.printStackTrace();
        } catch (IOException e) {
          // TODO 自動生成された catch ブロック
          e.printStackTrace();
        }
      }
    }
  }

}
