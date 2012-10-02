package main;
import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 * ツイートデータhtmlファイルのインデックス作成を行うクラス． FSDirectoryを使っている． 実行構成のVM引数に「-Xmx1g」と書くこと．
 */
public class MakeIndexMicroblog {
	/** インンデックスを作るディレクトリのパス */
	private String pathOfIndex;
	/** アナライザ */
	private Analyzer analyzer;



	public MakeIndexMicroblog(String pathOfIndex) {
		setPathOfIndex(pathOfIndex);
		setAnalyzer(new StandardAnalyzer(Version.LUCENE_36));
	}

	public MakeIndexMicroblog(String pathOfIndex, Analyzer analyzer) {
		setPathOfIndex(pathOfIndex);
		setAnalyzer(analyzer);
	}

	public void setPathOfIndex(String passOfIndex) {
		this.pathOfIndex = passOfIndex;
	}

	public String getPathOfIndex() {
		return this.pathOfIndex;
	}

	public void setAnalyzer(Analyzer analyzer) {
		this.analyzer = analyzer;
	}

	/**
	 * 1つのファイルからインデックスを作成する．． インデックスのpath(oathOfIndex)と，ファイルのpath(filename)と，
	 * アナライザ(analyzer)を受け取って，インデックスを作成する．
	 * 
	 * @param pathOfIndex
	 *            インデックスの場所
	 * @param filename
	 *            インデックス付けするファイル
	 * @param analyzer
	 *            アナライザ
	 * @throws IOException
	 */
	public static boolean makeIndexSimpleFile(String pathOfIndex,
			String filename, Analyzer analyzer) throws IOException {
		// インデックスディレクトリを開く．
		Directory index = FSDirectory.open(new File(pathOfIndex));
		// IndexWriterクラスの設定クラス
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_36,
				analyzer);

		// IndexWriterを作る
		IndexWriter writer = new IndexWriter(index, config);
		// 索引付けする単語の数を増やす．通常10,000らしい．
		// w.setMaxFieldLength(20000);
		// インデックスを初期化
		writer.deleteAll();

		// ファイルをインデックスに追加．
		addFileToIndex(new File(filename), writer);

		writer.close();
		index.close();
		return true;
	}

	/**
	 * ディレクトリ内の全てのファイルからインデックスを作成する．
	 * インデックスのpath(oathOfIndex)と，ファイルあるディレクトリのpath(pathOfFileDir)と，
	 * アナライザ(analyzer)を受け取って，インデックスを作成する．
	 * 
	 * @param pathOfIndex
	 *            インデックスの場所
	 * @param pathOfFileDir
	 *            インデックス付けするファイルのあるディレクトリ
	 * @param analyzer
	 *            アナライザ
	 * @throws IOException
	 */
	public static boolean makeIndexAllFiles(String pathOfIndex,
			String pathOfFileDir, Analyzer analyzer) throws IOException {
		// インデックスディレクトリを開く．
		Directory index = FSDirectory.open(new File(pathOfIndex));

		// IndexWriterクラスの設定クラス
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_36,
				analyzer);

		// IndexWriterを作る
		IndexWriter writer = new IndexWriter(index, config);
		// 索引付けする単語の数を増やす．通常10,000らしい．
		// w.setMaxFieldLength(20000);
		// インデックスを初期化
		writer.deleteAll();

		// ディレクトリ内のファイルを取得
		File[] files = new File(pathOfFileDir).listFiles();
		for (File file : files) { // ファイルを一つずつ取り出して
			addFileToIndex(file, writer);
			System.out.println("ファイルをインデックスに追加しました：" + file.getPath());
		}
		System.out.println("次の" + files.length + "個のファイルをインデックスに追加しました．");
		for (File file : files) {
			System.out.println(file.getPath());
		}
		writer.close();
		index.close();
		return true;
	}

	/**
	 * ファイルをインデックスに追加する． 引数には，IndexWriterを渡す．(インデックスのpathは渡さない)
	 * 
	 * @param file
	 *            　インデックスに加えたいファイル
	 * @param writer
	 *            使用するIndexWriter
	 * @throws IOException
	 */
	public static boolean addFileToIndex(File file, IndexWriter writer)
			throws IOException {
		// ファイルの中身を取り出す
		List<String> list = FileUtils.readLines(file, "utf-8");
		// 中身を1行ずつ取り出してドキュメントに追加．
		for (String line : list) {
			Document document = new Document();
			// ID(最初の17桁)
			document.add(new Field("ID", line.substring(0, 17),
					Field.Store.YES, Field.Index.NO));
			// System.out.println(line.substring(0,17)); //確認用
			// ユーザ名
			int head = line.indexOf("\t") + 1;
			int end = line.indexOf("\t", head);
			document.add(new Field("USER", line.substring(head, end),
					Field.Store.YES, Field.Index.NO));
			// System.out.println(line.substring(head, end)); //確認用
			// 日付
			head = line.indexOf("\t", end + 1) + 1;
			end = line.indexOf("\t", head);
			document.add(new Field("TIME", line.substring(head, end),
					Field.Store.YES, Field.Index.NO));
			// System.out.println(line.substring(head, end)); //確認用
			// ツイート
			head = end + 1;
			document.add(new Field("TWEET", line.substring(head),
					Field.Store.YES, Field.Index.ANALYZED));
			writer.addDocument(document);
		}
		return true;
	}

	/**
	 * フィールドの値と，filenameからインデックスを作成する．
	 * 
	 * @param filename
	 *            インデックス付けしたいファイルのパス
	 * @throws IOException
	 */
	public boolean makeIndexSimpleFile(String filename) throws IOException {
		return MakeIndexMicroblog.makeIndexSimpleFile(this.pathOfIndex,
				filename, this.analyzer);
	}

	/**
	 * フィールドの値と，pathOfFileDir内の複数ファイルからインデックスを作成する．
	 * 
	 * @param pathOfFileDir
	 *            インデックス付けしたいファイル群のあるディレクトリ．
	 * @throws IOException
	 */
	public boolean makeIndexAllFiles(String pathOfFileDir) throws IOException {
		return MakeIndexMicroblog.makeIndexAllFiles(this.pathOfIndex,
				pathOfFileDir, this.analyzer);
	}

}
