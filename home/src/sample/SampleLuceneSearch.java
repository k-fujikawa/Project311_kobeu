package sample;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import search.AnalyzerFactory;

public class SampleLuceneSearch {
  /**
   * @param args
   */
  public static void main(String[] args) {
    SampleLuceneSearch SLS = new SampleLuceneSearch();
    SLS.search1(args);
  }

  public void search(String[] args) {
    //String pathOfIndex = "/Users/kitaguchisayaka/Project311/index";
    for (int i=0; i<args.length; i++) {
      System.out.println(args[i]);
    }
    if (args.length < 1) {
      System.err.println("args[0] is pathOfIndex, args[n] is queryStr (n>=1)");
      System.exit(0);
    }
    String pathOfIndex = args[0];
    StringBuilder sb = new StringBuilder();
    int len = args.length;
    for (int i=1; i<len; i++) {
      sb.append(args[i]);
      sb.append(" ");
    }
    String queryStr = sb.toString();
    try {
      Directory index = FSDirectory.open(new File(pathOfIndex));
      IndexReader reader = DirectoryReader.open(index);
      IndexSearcher seracher = new IndexSearcher(reader);
      Similarity similarity;
      similarity = new BM25Similarity();
      seracher.setSimilarity(similarity);
      AnalyzerFactory af = new AnalyzerFactory(Version.LUCENE_40);
      Analyzer analyzer = af.getJapaneseEnglishAnalyzer();

      if (args.length < 2) {
        queryStr = "地震";
      }
      System.out.println("Search!!   queryStr : " + queryStr);
      QueryParser qp = new QueryParser(Version.LUCENE_40, "text", analyzer);
      //qp.setDefaultOperator(Operator.AND); // AND検索
      Query query = qp.parse(queryStr);

      TopDocs topDocs = seracher.search(query, 10);
      ScoreDoc[] hits = topDocs.scoreDocs;
      for (int i=0; i<hits.length; i++) {
        int docId = hits[i].doc;
        Document doc = seracher.doc(docId);
        String docno = doc.get("tweet_id");
        System.out.println(i + " : " + docno);
      }
    } catch (IOException e) {
      // TODO 自動生成された catch ブロック
      e.printStackTrace();
    } catch (ParseException e) {
      // TODO 自動生成された catch ブロック
      e.printStackTrace();
    }
  }

  public void search1(String[] args) {
    //String pathOfIndex = "/Users/kitaguchisayaka/Project311/indexAllField";
    for (int i=0; i<args.length; i++) {
      System.out.println(args[i]);
    }
    if (args.length < 1) {
      System.err.println("args[0] is pathOfIndex, args[n] is queryStr (n>=1)");
      System.exit(0);
    }
    String pathOfIndex = args[0];
    StringBuilder sb = new StringBuilder();
    int len = args.length;
    for (int i=1; i<len; i++) {
      sb.append(args[i]);
      sb.append(" ");
    }
    String queryStr = sb.toString();
    try {
      Directory index = FSDirectory.open(new File(pathOfIndex));
      IndexReader reader = DirectoryReader.open(index);
      IndexSearcher seracher = new IndexSearcher(reader);
      Similarity similarity;
      //similarity = new DefaultSimilarity();
      //similarity = new BM25Similarity();
      //similarity = new LMDirichletSimilarity(); // スコアが負の時は0になる
      //similarity = new NonTfSimilarity();
      similarity = new LMJelinekMercerSimilarity(0.7F);
      seracher.setSimilarity(similarity);
      AnalyzerFactory af = new AnalyzerFactory(Version.LUCENE_40);
      Analyzer analyzer = af.getJapaneseEnglishAnalyzer();

      if (args.length < 2) {
        queryStr = "地震";
      }
      System.out.println("Search!!   queryStr : " + queryStr);
      QueryParser qp = new QueryParser(Version.LUCENE_40, "text", analyzer);
      //qp.setDefaultOperator(Operator.AND);
      Query query = qp.parse(queryStr);

      TopDocs topDocs = seracher.search(query, 10);
      ScoreDoc[] hits = topDocs.scoreDocs;
      for (int i=0; i<hits.length; i++) {
        int docId = hits[i].doc;
        Document doc = seracher.doc(docId);
        float score = hits[i].score;
        String tweetid = doc.get("tweet_id");
        String userid = doc.get("user_id");
        String datetime = doc.get("datetime");
        String text = doc.get("text");
        System.out.println("{\"score\":\"" + score + "\", \"tweet_id\":\"" + tweetid + "\", \"user_id\":\"" + userid + "\", \"datetime\":\"" + datetime + "\", \"text\":\"" + text + "\"}");
      }
      index.close();
      reader.close();
    } catch (IOException e) {
      // TODO 自動生成された catch ブロック
      e.printStackTrace();
    } catch (ParseException e) {
      // TODO 自動生成された catch ブロック
      e.printStackTrace();
    }
  }

}
