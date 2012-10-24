package sample;

import java.io.IOException;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;

public class SampleExcec {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	  // 参考：http://gihyo.jp/dev/serial/01/engineer_toolbox/0019
		CommandLine commandLine = new CommandLine("ping"); // pingコマンドの実行を作成
	    commandLine.addArgument("localhost"); // 引数
	    //commandLine.addArgument("-n");
	    //commandLine.addArgument("5");
	    //commandLine.addArguments("-w 1000");
	    //commandLine.addArgument("127.0.0.1"); // adress of localhost

	    // Executorを作成
	    DefaultExecutor executor = new DefaultExecutor();
	    try {
	      executor.setExitValue(0);    // 正常終了の場合に返される値
	      // 実行
	      int exitValue = executor.execute(commandLine);
	    } catch (ExecuteException ex) {
	      ex.printStackTrace();
	    } catch (IOException ex) {
	      ex.printStackTrace();
	    }
	}
}
