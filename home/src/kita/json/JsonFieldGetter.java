package kita.json;

import java.io.File;
import java.io.IOException;

import net.sf.json.JSONObject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

public class JsonFieldGetter {

  /**
   * フィールドを抽出したいjsonファイルのパス<br>
   * 出力ファイルの命名の都合上，ファイル名は"JA番号.json"（番号は整数）として下さい．
   */
  private String pathOfJson;
  /**
   * 出力ファイルを作成するディレクトリのパス<br>
   * 出力ファイル名は，"kita_field番号.txt"（番号は読み込んだjsonファイルのそれに一致）
   */
  private String pathOfOutputDir;

/**
 *
 * @param args[0] 出力ファイルを作成するディレクトリのパス
 * @param args[1] 入力ファイルのパス
 */
  public static void main(String[] args) {
    JsonFieldGetter jsonFieldGetter = new JsonFieldGetter(args[0], args[1]);
    jsonFieldGetter.makeFieldFile();
  }

  /**
   * コンストラクタ
   * @param pathOfJson フィールドを抽出したいjsonファイル
   */
  public JsonFieldGetter(String pathOfOutputDir, String pathOfJson) {
    this.pathOfOutputDir = pathOfOutputDir;
    this.pathOfJson = pathOfJson;
  }

  /**
   *
   */
  public void makeFieldFile() {
    String num = pathOfJson.substring(pathOfJson.indexOf("JA")+2, pathOfJson.indexOf(".json"));
    String filepath = pathOfOutputDir + "kita_field" + num + ".txt";
    File out = new File(filepath);
    try { // ファイルを1行ずつ読み込んで処理（tweet1件ずつ）
      LineIterator it = FileUtils.lineIterator(new File(pathOfJson), "utf-8");
      while(it.hasNext()) {
        String tweet = it.next();
        if (tweet.isEmpty()) continue; // 空行は除く
        // create JSONObject
        //tweet = "{" + tweet + "}"; // 先輩はこのように記述されたそうですが，私のだとこれ書くと駄目．なぜ？
        JSONObject jsonObject = JSONObject.fromObject(tweet);
        JSONObject user = jsonObject.getJSONObject("user");
        if(!(user.getString("lang").equals("ja"))) {
          continue; // 日本語以外のTweetは無視
        }
        StringBuilder stringBuilder = new StringBuilder();
        // created_at
        stringBuilder.append(jsonObject.getString("created_at"));
        stringBuilder.append("\t");
        // Userのscreen_name
        stringBuilder.append(user.getString("screen_name"));
        stringBuilder.append("\t");
        // Userのプロフィール画像のURL
        stringBuilder.append(user.getString("profile_image_url"));
        stringBuilder.append("\t");
        // 返信先のscreen_name
        stringBuilder.append(jsonObject.getString("in_reply_to_screen_name"));
        stringBuilder.append("\t");
        // text
        String text = jsonObject.getString("text");
        //String lineSeparatorStr = System.getProperty("line.separator"); //改行コード
        //text = text.replace(lineSeparatorStr, " "); // 改行コードを空白に置換
        text = text.replace("\n", " "); // 改行コードを空白に置換
        text = text.replace("\r", " "); // キャリッジリターンも空白に置換
        stringBuilder.append(text);
        stringBuilder.append("\n");

        // ファイル書き込み
        FileUtils.writeStringToFile(out, stringBuilder.toString(), "utf-8", true);
      }
      it.close();
    } catch (IOException e) {
      System.err.println("ファイル" + pathOfJson + "の読み込みでエラーがおきました．");
      e.printStackTrace();
    }
  }

}
