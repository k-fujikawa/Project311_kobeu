package kita.index;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 * インデックスを作成する．
 * 同時に，BM25用にtextフィールドの平均長を求め，ファイルに書き出す．
 * @author KitaguchiSayaka
 *
 */
public class TwitterIndexMaker {

  private String    pathOfIndex;  /** インデックスを作るディレクトリ */
  /** BM25用．textフィールドの平均長を記録したファイルのパス */
  private String    pathOfAverageTweetLength;
  private Analyzer  analyzer;     /** アナライザ */

  public TwitterIndexMaker(String pathOfIndex, String pathOfAverageTweetLength, Analyzer analyzer) {
    setPathOfIndex(pathOfIndex);
    setPathOfAverageTweetLength(pathOfAverageTweetLength);
    setAnalyzer(analyzer);
  }

  public String getPathOfIndex() {
    return pathOfIndex;
  }

  public void setPathOfIndex(String pathOfIndex) {
    this.pathOfIndex = pathOfIndex;
  }

  public Analyzer getAnalyzer() {
    return analyzer;
  }

  public void setAnalyzer(Analyzer analyzer) {
    this.analyzer = analyzer;
  }

  /**
   * インデックスに新たなファイルを追加して，インデックスを更新する．<br>
   * 同時に，textフィールドの平均長を記録したファイルを更新する．
   * @param filename
   * @return
   * @throws IOException
   */
  public boolean updateAddIndex(String filename) throws IOException {
    // textの平均長のファイルから平均長を取り出す
    File averageTweetLengthFile = new File(pathOfAverageTweetLength);
    List<String> list = FileUtils.readLines(averageTweetLengthFile, "utf-8");
    String fieldName = list.get(0);
    String floatValueStr = list.get(1);
    float floatValue = Float.parseFloat(floatValueStr);

    // インデックスディレクトリを開く
    Directory index = FSDirectory.open(new File(pathOfIndex));
    // IndexWriterクラスの設定クラス
    IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_36, analyzer);
    // IndexWriterを作る
    IndexWriter writer = new IndexWriter(index, config);
    // ファイルをインデックスに追加
    float averageTextLength = addFileToIndex(new File(filename), writer);
    floatValue = (floatValue + averageTextLength) / 2;
    floatValueStr = Float.toString(floatValue);
    FileUtils.writeStringToFile(averageTweetLengthFile, fieldName + "\n", "utf-8", false);
    FileUtils.writeStringToFile(averageTweetLengthFile, floatValueStr + "f", "UTF-8", true);

    writer.commit();
    writer.close();
    index.close();
    return true;
  }

  /**
   * ファイルをインデックスに追加する<br>
   * 同時に，"text"フィールドの平均長を返す．
   * @param file インデックスに加えたいファイル
   * @param writer 使用するIndexWriter
   * @return 読み込んだファイルの"text"フィールドの平均長
   * @throws IOException
   */
  private float addFileToIndex(File file, IndexWriter writer) throws IOException {
    float sumOfTextLength = 0;
    // ファイルの中身を取り出す
    List<String> list = FileUtils.readLines(file, "utf-8");
    int listSize = list.size(); // 読み込んだ総ドキュメント数
    // 中身を1行ずつ取り出してドキュメントに追加
    for(String line : list) {
      Document document = new Document();
      String[] lineAry = line.split("\t");
      // created_at
      document.add(new Field("created_at", lineAry[0], Field.Store.YES, Field.Index.NO));
      // user's screen_name
      document.add(new Field("screen_name", lineAry[1], Field.Store.YES, Field.Index.NO));
      // profile_image_url
      document.add(new Field("image", lineAry[2], Field.Store.YES, Field.Index.NO));
      // in_reply_to_screen_name
      document.add(new Field("reply_name", lineAry[3], Field.Store.YES, Field.Index.NO));
      // text
      document.add(new Field("text", lineAry[4], Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.YES));
      sumOfTextLength += lineAry[4].length();
      writer.addDocument(document);
    }
    float averageTextLength = sumOfTextLength / listSize;
    return averageTextLength;
  }

  /**
   * 全てのDocumentを破棄して，インデックスを初期化する.
   * close処理がされないことに注意.
   * @param writer
   */
  public void initIndex(IndexWriter writer) {
    try {
      writer.deleteAll();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public String getPathOfAverageTweetLength() {
    return pathOfAverageTweetLength;
  }

  public void setPathOfAverageTweetLength(String pathOfAverageTweetLength) {
    this.pathOfAverageTweetLength = pathOfAverageTweetLength;
  }

  /* 未完成．一致するDocumentを削除したい
  public boolean updateremoveIndex(String filename) throws IOException {
    // インデックスディレクトリを開く
    Directory index = FSDirectory.open(new File(pathOfIndex));
    // IndexWriterクラスの設定クラス
    IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_36, analyzer);
    // IndexWriterを作る
    IndexWriter writer = new IndexWriter(index, config);
    // ファイルをインデックスから削除
    removeFileToIndex(new File(filename), writer);

    writer.close();
    index.close();
    return true;
  }

  private boolean removeFileToIndex(File file, IndexWriter writer) throws IOException {
    // ファイルの中身を取り出す
    List<String> list = FileUtils.readLines(file, "utf-8");
    // 中身を一行ずつ取り出す
    for(String line : list) {
      Document document = new Document();
      String[] lineAry = line.split("\t");
      // created_at

    }
    return true;
  }
  */

}
