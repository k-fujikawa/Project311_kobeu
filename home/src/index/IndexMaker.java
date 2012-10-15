package index;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import search.AnalyzerFactory;

/**
 * Project311向け．索引作成プログラム
 */
public class IndexMaker {
  /** 索引を作るディレクトリ */
  private String pathOfIndex;
  /** アナライザ */
  private Analyzer analyzer;

  /**
   * コンストラクタ
   * @param pathOfIndex 索引を作るディレクトリ
   * @param analyzer アナライザ
   */
  public IndexMaker(String pathOfIndex, Analyzer analyzer) {
    setPathOfIndex(pathOfIndex);
    setAnalyzer(analyzer);
  }

  /**
   * ファイルをインデックスに追加する．
   * @param filename インデックスに加えたいファイル
   * @throws IOException
   */
  public void updateAddIndex(String filename) throws IOException {
    // インデックスディレクトリを開く
    Directory index = FSDirectory.open(new File(pathOfIndex));
    // IndexWriterクラスの設定クラス
    IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_36, analyzer);
    // IndexWriterを作る
    IndexWriter writer = new IndexWriter(index, config);
    // ファイルをインデックスに追加
    //addFileToIndex(new File(filename), writer);
    LineIterator li = null;
    try {
      li = FileUtils.lineIterator(new File(filename), "utf-8");
      while (li.hasNext()) { // ファイルの中身を1行ずつ取り出してドキュメントに追加
        String line = li.nextLine();
        String[] lineAry = line.split("\t");
        Document document = new Document();
        // tweet_id (値取り出し可，検索に含めない)
        //document.add(new Field("tweet_id", lineAry[0], Field.Store.YES, Field.Index.NO));
        // user_id（DB使うなら不要？）
        //document.add(new Field("user_id", lineAry[1], Field.Store.YES, Field.Index.NO));
        // datetime
        //document.add(new Field("datetime", lineAry[2], Field.Store.YES, Field.Index.NO));
        // text
        //document.add(new Field("text", lineAry[3], Field.Store.YES, Field.Index.ANALYZED));
        document.add(new Field("text", lineAry[3], Field.Store.NO, Field.Index.ANALYZED));
        writer.addDocument(document);
      }
    } catch (IOException e) {
      System.err.println("error: faield to read input files :" + filename);
      e.printStackTrace();
    }
    writer.commit();
    writer.close();
    index.close();
  }

  /**
   * 索引を作るディレクトリを取得します。
   * @return pathOfIndex 索引を作るディレクトリ
   */
  public String getPathOfIndex() {
      return pathOfIndex;
  }

  /**
   * 索引を作るディレクトリを設定します。
   * @param pathOfIndex 索引を作るディレクトリ
   */
  public void setPathOfIndex(String pathOfIndex) {
      this.pathOfIndex = pathOfIndex;
  }

  /**
   * アナライザを取得します。
   * @return analyzer アナライザ
   */
  public Analyzer getAnalyzer() {
      return analyzer;
  }

  /**
   * アナライザを設定します。
   * @param analyzer アナライザ
   */
  public void setAnalyzer(Analyzer analyzer) {
      this.analyzer = analyzer;
  }

  /**
   * インデックスを作成
   * @param args[0] 索引を作るディレクトリ
   * @param args[1] 索引付けするファイル
   */
  public static void main(String[] args) {
    if (args.length != 2) {
      System.err.println("error: 引数は2つだけ指定してください．");
      System.exit(0);
    }
    AnalyzerFactory analyzerFactory = new AnalyzerFactory(Version.LUCENE_36);
    Analyzer analyzer = analyzerFactory.getJapaneseEnglishAnalyzer();
    IndexMaker indexMaker = new IndexMaker(args[0], analyzer);
    try {
      indexMaker.updateAddIndex(args[1]);
    } catch (IOException e) {
      System.err.println("error: faild to make index");
      e.printStackTrace();
    }

  }

}
