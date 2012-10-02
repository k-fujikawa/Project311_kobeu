package cs24.kitaguchi.Microblog.twitter.searcher;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Explanation.IDFExplanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class Test {

  /**
   * @param args
   */
  public static void main(String[] args) {
    String pathOfIndex = "/Users/KitaguchiSayaka/Desktop/IndexTest2";
    Directory index;
    try {
      index = FSDirectory.open(new File(pathOfIndex));
      IndexReader reader = IndexReader.open(index);
      IndexSearcher searcher = new IndexSearcher(reader);
      Similarity similarity = new NonTfSimilarity();
      searcher.setSimilarity(similarity);
      AnalyzerFactory af = new AnalyzerFactory(Version.LUCENE_36);
      Analyzer analyzer = af.getJapaneseEnglishAnalyzer();

      TwitterSearcher twitterSearcher = new TwitterSearcher(searcher, analyzer);
      TwitterQueryMaker tQM = new TwitterQueryMaker(analyzer);
      Query q = tQM.makeQuery("地震 千葉");
      Result result = twitterSearcher.search(q, 10); // 検索
      ScoreDoc[] hits = result.getHits();
      for(int n=0; n<hits.length; n++) {
        int docId = hits[n].doc;
        Document d = searcher.doc(docId);
        System.out.println(docId + ":" + hits[n].score + " : " + d.get("created_at")+ " : " + d.get("screen_name") + " : " + d.get("text"));

        TimeParser tP = new TimeParser();
        Calendar cal = tP.parseCreatedAt(d.get("created_at"));
        System.out.println(cal.get(Calendar.MONTH) + ":" + cal.get(Calendar.DAY_OF_MONTH) + "/" + cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE));

        TermFreqVector tfv = reader.getTermFreqVector(docId, "text");
        String[] terms = tfv.getTerms();
        List terms1 = Arrays.asList(terms);
        if(terms1.contains("千葉")) {
          System.out.println("@````````");
        }
        Iterator<String> it = terms1.iterator();
        while (it.hasNext()) {
          System.out.println(it.next());
        }
        /*for(int p=0; p<terms.length; p++) {
          System.out.println(terms[p]);
        }*/
        //System.out.println(tfv.size());
      }
      System.out.println("\n****************************\n");

      int numDocs = reader.numDocs();
      System.out.println("登録ドキュメント数：" + numDocs);
      // そのうち"千葉"を含むドキュメント数
      Term term = new Term("text", "千葉");
      int docFreq = reader.docFreq(term);
      System.out.println("千葉を含むドキュメント数：" + docFreq);
      // 千葉のidf
      //Similarity similarity = Similarity.getDefault();
      float f = similarity.idf(docFreq, numDocs);
      System.out.println("千葉のidf：" + f);
      // 違う求め方
      IDFExplanation idfExplanation = similarity.idfExplain(term, searcher);
      float f2 = idfExplanation.getIdf();
      System.out.println("千葉のidf：" + f2);
      System.out.println("\n****************************\n");


    } catch (IOException e) {
      // TODO 自動生成された catch ブロック
      e.printStackTrace();
    } catch (ParseException e) {
      // TODO 自動生成された catch ブロック
      e.printStackTrace();
    }
  }




  public void test1() {
    // ファイルの中身を取り出す
    String filepath = "/Users/KitaguchiSayaka/ProRin/data/kita_fields/6-19/kita_field0.txt";
    List<String> list;
    int i=0;
    try {
      list = FileUtils.readLines(new File(filepath), "utf-8");
      // 中身を1行ずつ取り出してドキュメントに追加
      for(String line : list) {
        //Document document = new Document();
        String[] lineAry = line.split("\t");
        // created_at
        //document.add(new Field("created_at", lineAry[0], Field.Store.YES, Field.Index.NO));
        System.out.println(lineAry[0]);
        // user's screen_name
        //document.add(new Field("screen_name", lineAry[1], Field.Store.YES, Field.Index.NO));
        System.out.println("\t" + lineAry[1]);
        // profile_image_url
        //document.add(new Field("image", lineAry[2], Field.Store.YES, Field.Index.NO));
        System.out.println("\t" + lineAry[2]);
        // in_reply_to_screen_name
        //document.add(new Field("reply_name", lineAry[3], Field.Store.YES, Field.Index.NO));
        System.out.println("\t" + lineAry[3]);
        // text
        //document.add(new Field("text", lineAry[4], Field.Store.YES, Field.Index.ANALYZED));
        System.out.println("\t" + lineAry[4]);
        //writer.addDocument(document);
        if (++i == 10) break;
      }
    } catch (IOException e) {
      // TODO 自動生成された catch ブロック
      e.printStackTrace();
    }
  }

}
