package cs24.kitaguchi.Microblog.twitter.searcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.search.Explanation.IDFExplanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Similarity;


public class TwitterMMR {
  private IndexSearcher searcher;
  private IndexReader reader;
  private Similarity similarity;
  private double scoreSum;
  private double lambda;
  private int clusterNum;

  public TwitterMMR(IndexSearcher searcher, IndexReader reader, Analyzer analyzer,
      Similarity similarity, double lambda, int clusterNum) {
    this.searcher = searcher;
    this.reader = reader;
    this.similarity = similarity;
    this.scoreSum = 0;
    this.clusterNum = clusterNum;
    setLambda(lambda);
  }

  /**
   * 検索結果のスコアの合計を求める
   * @param hits 検索結果の集合
   * @return スコアの合計
   */
  public double calcSumScore(ScoreDoc[] hits) {
    double sum = 0;
    for (int i=0; i < hits.length; i++) {
      sum += hits[i].score;
    }
    System.out.println("スコアの合計は" + sum + "でした．");
    return sum;
  }

  /**
   * MMRを行う．<br>
   * clusterNumで指定した数のクラスタを作る．<br>
   * 各クラスタに1つづつ代表Tweetを入れる
   * @param hits
   * @return
   * @throws IOException
   * @throws CorruptIndexException
   */
  public ResultMMR MMR(ScoreDoc[] hits, TimeParser timeParser) throws CorruptIndexException, IOException {
    //ResultMMR resultMMR = new ResultMMR(clusterNum, searcher, clusterNum);
    ResultMMR resultMMR = new ResultMMR(clusterNum, searcher, timeParser, clusterNum);
    int cluNum = 0; // 現在作成されているクラスタの数

    // ******************** スコアの合計値を求める ******************** ．
    scoreSum = calcSumScore(hits);

    // ********************** 代表Tweetを選出 **********************
    // 1番目の検索結果をクラスタ0に挿入
    resultMMR.addAtClusterN(hits[0], cluNum++);
    // 残りの検索結果をリスト化
    ArrayList<ScoreDoc> Rlist = new ArrayList<ScoreDoc>(0);
    for(int i=1; i<hits.length; i++) {
      Rlist.add(hits[i]);
    }
    // 残りの検索結果から，既に選ばれたTweetとの類似度が最大のものを選択
    while(cluNum < clusterNum) {
      ScoreDoc maxDoc = null; // 次にクラスタに入れるTweet
      Iterator<ScoreDoc> it = Rlist.iterator();
      double maxSim = 0;
      int flag = 0;
      while(it.hasNext()) { // 残りの検索結果1つひとつに対してのループ
        double maxSim2 = 0;
        ScoreDoc di = it.next();
        for (int i=0; i<cluNum; i++) { // sim2の最大値を求める
          // 既に選択されたTweetを取り出す．
          ScoreDoc dj = resultMMR.getScoreDocAtFrom(i, 0);
          double nSim2 = sim2(di, dj);
          //System.out.print("nsin2: "+ nSim2);
          if (nSim2 > maxSim2) {
            maxSim2 = nSim2;
          }
        }
        //System.out.println("maxSim2" + maxSim2);
        //System.out.println("score: " + di.score);
        double nSim = sim(di, maxSim2);
        if (flag == 0) { // 初回のみmaxSimとmaxDocのセット
          maxSim = nSim;
          maxDoc = di;
          flag = 1;
        }
        //System.out.println("nSim: " + nSim);
        //if (nSim == maxSim)
          //System.out.println("スコアが同じ");
        if (nSim > maxSim) {
          maxSim = nSim;
          maxDoc = di;
          //System.out.println("**maxchange**");
        }
      }
      if (maxDoc == null) {
        System.err.println("error:MicroblogMMRのmaxDocがnullです．");
      }
      //System.out.println("maxSim: " + maxSim);
      resultMMR.addAtClusterN(maxDoc, cluNum++);
      Rlist.remove(maxDoc);
    }

    // ****************** 残りのTweetを分類 ******************
    Iterator<ScoreDoc> itR = Rlist.iterator();
    while( itR.hasNext() ) { // 残りのTweet1つひとつに対して
      ScoreDoc docR = itR.next();
      // 代表Tweetとの類似度が最大のリストにadd
      double rSim2Max = sim2(docR, resultMMR.getScoreDocAtFrom(0, 0));
      int rClass = 0;
      for(int i = 1; i < cluNum; i++) {
        double rSim2 = sim2(docR, resultMMR.getScoreDocAtFrom(i, 0));
        if (rSim2 > rSim2Max) {
          rSim2Max = rSim2;
          rClass = i;
        }
      }
      resultMMR.addAtClusterN(docR, rClass);
    }
    // 時刻の平均値を求める
    resultMMR.setTimeList();
    // クラスタを時間順ソート
    resultMMR.sortTime();
    return resultMMR;
  }

  /**
   * MMRの目的関数を計算し，その結果を返す．
   * @param d1 現在注目中のTweet
   * @param maxSim2 d1と既に選択されたTweetとの類似度の最大値
   * @return MMRの目的関数の計算結果
   */
  public double sim(ScoreDoc d1, double maxSim2) {
    double sim=0;
    sim = lambda*sim1(d1) - (1-lambda)*maxSim2;
    return sim;
  }

  /**
   * Tweetとクエリの類似度sim1を返す．
   * @param d1
   * @return Tweetとクエリの類似度
   */
  public double sim1(ScoreDoc d1) {
    return d1.score / scoreSum;
  }


  /**
   * Tweet同士の類似度を，コサイン類似度で求めて返す．<br>
   * @param d1 Tweet1
   * @param d2 Tweet2
   * @return Tweet同士の類似度
   */
  public double sim2(ScoreDoc d1, ScoreDoc d2) {
    double sim = 0, normD1 = 0, normD2 = 0;
    try {
      TermFreqVector tfv1 = reader.getTermFreqVector(d1.doc, "text");
      TermFreqVector tfv2 = reader.getTermFreqVector(d2.doc, "text");
      List<String> terms1 = Arrays.asList(tfv1.getTerms()); // d1の単語リスト
      List<String> terms2 = Arrays.asList(tfv2.getTerms()); // d2の単語リスト
      Iterator<String> it1 = terms1.iterator();
      while(it1.hasNext()) {
        String str = it1.next();
        float idf = idf(str);
        if(terms2.contains(str)) {
          sim += idf*idf;
        }
        normD1 += idf*idf;
      }
      normD2 = norm(terms2);
      sim = sim/((Math.sqrt(normD1))*(Math.sqrt(normD2)));
    } catch (IOException e) {
      System.out.println("error: sim2");
      e.printStackTrace();
    }
    return sim;
  }

  /**
   * 単語のリストを受け取って，idfからノルムを返す
   * @param terms 単語リスト
   * @return 2-ノルム
   * @throws IOException
   */
  public double norm(List<String> terms) throws IOException {
    Iterator<String> it = terms.iterator();
    double norm = 0;
    while(it.hasNext()) {
      float idf = idf(it.next());
      norm += idf*idf;
    }
    return norm;
  }

  /**
   * 引数として受け取った単語のidfを返す．
   * @param termStr idfが知りたい単語
   * @return idf
   * @throws IOException
   */
  public float idf(String termStr) throws IOException {
    Term term = new Term("text", termStr);
    IDFExplanation idfExplanation = similarity.idfExplain(term, searcher);
    return idfExplanation.getIdf();
  }

  public double getLambda() {
    return lambda;
  }

  public void setLambda(double lambda) {
    this.lambda = lambda;
  }

}
