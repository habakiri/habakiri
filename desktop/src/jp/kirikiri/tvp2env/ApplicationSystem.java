/**
 ******************************************************************************
 * Copyright (c), Takenori Imoto
 * 楓 software http://www.kaede-software.com/
 * All rights reserved.
 ******************************************************************************
 * ソースコード形式かバイナリ形式か、変更するかしないかを問わず、以下の条件を満
 * たす場合に限り、再頒布および使用が許可されます。
 *
 * ・ソースコードを再頒布する場合、上記の著作権表示、本条件一覧、および下記免責
 *   条項を含めること。
 * ・バイナリ形式で再頒布する場合、頒布物に付属のドキュメント等の資料に、上記の
 *   著作権表示、本条件一覧、および下記免責条項を含めること。
 * ・書面による特別の許可なしに、本ソフトウェアから派生した製品の宣伝または販売
 *   促進に、組織の名前またはコントリビューターの名前を使用してはならない。
 *
 * 本ソフトウェアは、著作権者およびコントリビューターによって「現状のまま」提供
 * されており、明示黙示を問わず、商業的な使用可能性、および特定の目的に対する適
 * 合性に関する暗黙の保証も含め、またそれに限定されない、いかなる保証もありませ
 * ん。著作権者もコントリビューターも、事由のいかんを問わず、損害発生の原因いか
 * んを問わず、かつ責任の根拠が契約であるか厳格責任であるか（過失その他の）不法
 * 行為であるかを問わず、仮にそのような損害が発生する可能性を知らされていたとし
 * ても、本ソフトウェアの使用によって発生した（代替品または代用サービスの調達、
 * 使用の喪失、データの喪失、利益の喪失、業務の中断も含め、またそれに限定されな
 * い）直接損害、間接損害、偶発的な損害、特別損害、懲罰的損害、または結果損害に
 * ついて、一切責任を負わないものとします。
 ******************************************************************************
 * 本ソフトウェアは、吉里吉里2 ( http://kikyou.info/tvp/ ) のソースコードをJava
 * に書き換えたものを一部使用しています。
 * 吉里吉里2 Copyright (C) W.Dee <dee@kikyou.info> and contributors
 ******************************************************************************
 */
package jp.kirikiri.tvp2env;

import java.awt.Component;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.prefs.Preferences;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import jp.kirikiri.tjs2.TJS;
import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tvp2.TVP;
import jp.kirikiri.tvp2.base.FileMedia;
import jp.kirikiri.tvp2.base.Storage;
import jp.kirikiri.tvp2.base.StorageMedia;
import jp.kirikiri.tvp2.base.StorageMediaManager;
import jp.kirikiri.tvp2.base.SystemInitializer;
import jp.kirikiri.tvp2.msg.Message;
import jp.kirikiri.tvp2.utils.DebugClass;

public class ApplicationSystem {

	public static int
		MB_OK = 1,
		MB_ICONSTOP = 2;

	private boolean Terminated;
	private boolean TerminateOnWindowClose;
	private boolean TerminateOnNoWindowStartup;
	private int TerminateCode;

	private static final int VersionMajor = 0;
	private static final int VersionMinor = 0;
	private static final int VersionRelease = 1;
	private static final int VersionBuild = 0;

	private static boolean IsSetLookAndFeel = false;
	private static ApplicationSystem mSelf;
	public static void initializeSystem() {
		if( IsSetLookAndFeel == false ) {
			try {
				// システム標準の見た目に近づける
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				IsSetLookAndFeel = true;
			} catch (ClassNotFoundException e) {
			} catch (InstantiationException e) {
			} catch (IllegalAccessException e) {
			} catch (UnsupportedLookAndFeelException e) {
			}
		}
	}
	public static void finalizeApplication() {

	}
	public static void messageBox( final String caption, final String title, final int flags ) {
		//ERROR_MESSAGE、INFORMATION_MESSAGE、WARNING_MESSAGE、QUESTION_MESSAGE、または PLAIN_MESSAGE
		int messageType = JOptionPane.ERROR_MESSAGE;
		if( (flags & MB_OK) != 0 ) { }
		if( (flags & MB_ICONSTOP) != 0 ) { }
		JOptionPane.showMessageDialog( null, caption, title, messageType );
	}
	public void showSimpleMessageBox( final String text, final String caption ) {
		JOptionPane.showMessageDialog( null, text, caption, JOptionPane.INFORMATION_MESSAGE );
	}
	public String InputQuery(String caption, String prompt, String value ) {
	    String ret = JOptionPane.showInputDialog(prompt,value);
		return ret;
	}
	public boolean showYesNoDialog(String title, String message ) {
		Component parent = null;
		if( TVP.MainWindow != null ) {
			parent = TVP.MainWindow.getForm();
		}
		int ret = JOptionPane.showConfirmDialog( parent, message, title, JOptionPane.YES_NO_OPTION);
		return ret == JOptionPane.YES_OPTION;
	}

	public ApplicationSystem() {
		Terminated = false;
		TerminateOnWindowClose = true;
		TerminateOnNoWindowStartup = true;
		TerminateCode = 0;
		TVP.Application = new WeakReference<ApplicationSystem>(this);
		mSelf = this; // PC 版は強参照を保持する
	}

	public void terminateAsync(int code) {
		// do "A"synchronous temination of application
		Terminated = true;
		TerminateCode = code;

		System.exit(code);
	}

	public void terminateSync(int code) {
		TJS.IsTarminating = true;
		TVP.IsTarminating = true;
		// do synchronous temination of application (never return)
		SystemInitializer.systemUninitialize();
		System.exit(code);
	}
	public static boolean shellExecute( String target, String param ) {
		Runtime runtime = Runtime.getRuntime();
		try {
			if( param != null ) {
				String[] params = param.split(" ");
				final int count = params.length;
				String[] cmdarray = new String[1+count];
				cmdarray[0] = target;
				for( int i = 0; i < count; i++ ) {
					cmdarray[1+i] = params[i];
				}
				runtime.exec( cmdarray );
			} else {
				runtime.exec( target );
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	/**
	 * 型を判別できないので、常に文字列で返す
	 * @param key
	 * @return
	 */
	public static String readRegValue( final String key ) {
		try {
			Preferences root = null;
			String nodePath = key.replace('\\', '/');
			if( key.startsWith("HKEY_CURRENT_USER") ) {
				root = Preferences.userRoot();
				int first = nodePath.indexOf('/');
				if( first != -1 )
					nodePath = nodePath.substring(first);
			} else {
				root = Preferences.systemRoot();
			}
			int last = nodePath.lastIndexOf('/');
			String keyName;
			if( last != -1 )
				keyName = nodePath.substring(last+1);
			else
				keyName = nodePath;

			nodePath = nodePath.substring(0,last);
			Preferences node = root.node(nodePath);
			if( node != null ) {
			 	return node.get(keyName, null);
			} else {
				return null;
			}
		} catch( Exception e ) {
			return null;
		}
	}
	public static String getProperty( final String key ) {
		String name;
		if( key.charAt(0) == '-' ) {
			name = key.substring(1);
		} else {
			name = key;
		}
		return TVP.Properties.getProperty( name, null );
	}
	public static void setProperty( final String key, final String value ) {
		String name;
		if( key.charAt(0) == '-' ) {
			name = key.substring(1);
		} else {
			name = key;
		}
		TVP.Properties.setProperty( name, value );
	}
	/**
	 * アプリの時のみ有効 (ファイルのロックで対処する)
	 * TODO ファイルを出力するフォルダは再考の余地あり
	 * アプレットやAndroidの時は、常に true を返すようにする
	 * @param lockname
	 * @return
	 */
	public static boolean createAppLock( final String lockname ) {
		/* とりあえず無効としておく
		try {
			final FileOutputStream fos = new FileOutputStream(new File(lockname));
			final FileChannel fc = fos.getChannel();
			final FileLock lock = fc.tryLock();
			if (lock == null) {
				//既に起動されているので終了する
				return false;
			}
			//ロック開放処理を登録
			Runtime.getRuntime().addShutdownHook(
					new Thread() {
						public void run() {
							try {
								if (lock != null && lock.isValid()) {
									lock.release();
								}
								fc.close();
								fos.close();
							} catch (IOException e) {
							}
						}
					}
			);
		} catch (FileNotFoundException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
		*/
		return true;
	}
	/*
	private void checkManifest() {
		InputStream is = this.getClass().getResourceAsStream("/META-INF/MANIFEST.MF");
		Manifest mf;
		try {
			mf = new Manifest(is);
			is.close();
			Attributes a = mf.getMainAttributes();
			String val = a.getValue("Manifest-Version");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	*/
	/**
	 * Android なら apk から文字列引っ張ってくる
	 * @return バージョン文字列
	 */
	public final String getVersionString() {
		return String.format("%d.%d.%d.%d", VersionMajor, VersionMinor, VersionRelease, VersionBuild);
	}
	public final String getVersionInformation() {
		String verstr = getVersionString();
		String tjsverstr = String.format( "%d.%d.%d", TJS.VERSION_MAJOR, TJS.VERSION_MINOR, TJS.VERSION_RELEASE);

		return Message.formatMessage( Message.VersionInformation, verstr, tjsverstr );
	}

	public void registerFileMedia(StorageMediaManager storage ) throws TJSException {
		// ファイル読み込み用のメディアを登録する
		StorageMedia filemedia = new FileMedia();
		storage.register( filemedia );
		if( File.separatorChar == '/' ) {
			// TODO Linux Mac の時、もう少しいい方法を考えること
			storage.setCurrentMediaName( "file" );
		}
	}

	public void initializeDataPath() throws TJSException {
		String buf = null;
		boolean bufset = false;
		boolean nosel = false;
		boolean forcesel = false;
		boolean forcedataxp3 = false;
		boolean acceptfilenameargument = false;

		String currentDir = System.getProperty("user.dir"); // TODO Android で大丈夫かチェックというか、カレントディレクトリ意味ないか

		if( currentDir.charAt(currentDir.length()-1) != File.separatorChar ) {
			currentDir += File.separatorChar;
		}
		/*

		char buf[MAX_PATH];
		bool forcedataxp3 = GetSystemSecurityOption("forcedataxp3") != 0;
		bool acceptfilenameargument = GetSystemSecurityOption("acceptfilenameargument") != 0;

		if(!forcedataxp3 && !acceptfilenameargument)
		{
			if(TVPGetCommandLine(TJS_W("-nosel")) || TVPGetCommandLine(TJS_W("-about")))
			{
				nosel = true;
			}
			else
			{
				for(tjs_int i = 1; i<_argc; i++)
				{
					if(_argv[i][0] == '-' &&
						_argv[i][1] == '-' && _argv[i][2] == 0)
						break;

					if(_argv[i][0] != '-')
					{
						// TODO: set the current directory
						strncpy(buf, _argv[i], MAX_PATH-1);
						buf[MAX_PATH-1] = '\0';
						if(DirectoryExists(buf)) // is directory?
							strcat(buf, "\\");

						TVPProjectDirSelected = true;
						bufset = true;
						nosel = true;
					}
				}
			}
		}

		// check "-sel" option, to force show folder selection window
		if(!forcedataxp3 && TVPGetCommandLine(TJS_W("-sel")))
		{
			// sel option was set
			if(bufset)
			{
				char path[MAX_PATH];
				char *dum = 0;
				GetFullPathName(buf, MAX_PATH-1, path, &dum);
				strcpy(buf, path);
				TVPProjectDirSelected = false;
				bufset = true;
			}
			nosel = true;
			forcesel = true;
		}

		// check "content-data" directory
		if(!forcedataxp3 && !nosel)
		{
			char tmp[MAX_PATH];
			strcpy(tmp, IncludeTrailingBackslash(ExtractFileDir(ParamStr(0))).c_str());
			strcat(tmp, "content-data");
			if(DirectoryExists(tmp))
			{
				strcat(tmp, "\\");
				strcpy(buf, tmp);
				TVPProjectDirSelected = true;
				bufset = true;
				nosel = true;
			}
		}
		*/

		// check "data.xp3" archive
	 	if( !nosel ) {
			String dataXP3 = currentDir + "data.xp3";
			File file = new File(dataXP3);
			if( file.exists() && file.isFile() ) {
				buf = dataXP3;
				TVP.ProjectDirSelected = true;
				bufset = true;
				nosel = true;
			}
		}

	 	/*
		// check "data.exe" archive
	 	if(!nosel)
		{
			char tmp[MAX_PATH];
			strcpy(tmp, IncludeTrailingBackslash(ExtractFileDir(ParamStr(0))).c_str());
			strcat(tmp, "data.exe");
			if(FileExists(tmp))
			{
				strcpy(buf, tmp);
				TVPProjectDirSelected = true;
				bufset = true;
				nosel = true;
			}
		}

		// check self combined xpk archive
		if(!nosel)
		{
			if(TVPIsXP3Archive(TVPNormalizeStorageName(ParamStr(0))))
			{
				strcpy(buf, ParamStr(0).c_str());
				TVPProjectDirSelected = true;
				bufset = true;
				nosel = true;
			}
		}
		*/


		// check "data" directory
		if( !forcedataxp3 && !nosel ) {
			String dataDir = currentDir + "data";
			File dir = new File(dataDir);
			if( dir.exists() && dir.isDirectory( ) ) {
				buf = dataDir + File.separatorChar;
				TVP.ProjectDirSelected = true;
				bufset = true;
				nosel = true;
			}
		}

		// decide a directory to execute or to show folder selection
		/*
		if( !bufset ) {
			if(forcedataxp3) throw EAbort("Aborted");
			strcpy(buf, ExtractFileDir(ParamStr(0)).c_str());
			int curdirlen = strlen(buf);
			if(buf[curdirlen-1] != '\\') buf[curdirlen] = '\\', buf[curdirlen+1] = 0;
		}
		*/

		/*
		 * 選択ダイアログを出す
		if(!forcedataxp3 && (!nosel || forcesel)) {
			// load krdevui.dll ( TVP[KiRikiri] Development User Interface )
			HMODULE krdevui = LoadLibrary("krdevui.dll");
			if(!krdevui)
			{
				AnsiString toolspath = (IncludeTrailingBackslash(
						ExtractFilePath(ParamStr(0))) + "tools\\krdevui.dll");
				krdevui = LoadLibrary(toolspath.c_str());
			}

			if(!krdevui)
			{
				// cannot locate the dll
				throw Exception(
					ttstr(TVPCannnotLocateUIDLLForFolderSelection).AsAnsiString());
			}

			typedef int PASCAL (*UIShowFolderSelectorForm_t)(void *reserved, char *buf);
			typedef void PASCAL (*UIGetVersion_t)(DWORD *hi, DWORD *low);

			UIShowFolderSelectorForm_t	UIShowFolderSelectorForm;
			UIGetVersion_t				UIGetVersion;

			UIShowFolderSelectorForm =
				(UIShowFolderSelectorForm_t)GetProcAddress(krdevui, "UIShowFolderSelectorForm");
			UIGetVersion =
				(UIGetVersion_t)GetProcAddress(krdevui, "UIGetVersion");

			if(!UIShowFolderSelectorForm || !UIGetVersion)
			{
				FreeLibrary(krdevui);
				throw Exception(ttstr(TVPInvalidUIDLL).AsAnsiString());
			}

			DWORD h, l;
			UIGetVersion(&h, &l);
			if(h != TVP_NEED_UI_VERSION)
			{
				FreeLibrary(krdevui);
				throw Exception(ttstr(TVPInvalidUIDLL).AsAnsiString());
			}


			int result = UIShowFolderSelectorForm(Application->Handle, buf);

//			FreeLibrary(krdevui);
			// FIXME: the library should be freed as soon as finishing to use it.

			if(result == mrAbort)
			{
				// display the main window
			}
			else
			if(result == mrCancel)
			{
				// cancel
				throw EAbort("Canceled");
			}
			else
			if(result == mrOk)
			{
				// ok, prepare to execute the script
				TVPProjectDirSelected = true;
			}
		}
		*/

		// check project dir and store some environmental variables
		if( TVP.ProjectDirSelected ) {
			//Application->ShowMainForm = false;
		}

		if( buf != null  ) {
			if( buf.charAt(buf.length()-1) != File.separatorChar ) {
				buf += Storage.ArchiveDelimiter;
			}
		} else {
			throw new TJSException(Message.CannnotLocateUIDLLForFolderSelection);
		}
		if( File.separatorChar == '/' ) {
			// TODO Linux Mac の時、もう少しいい方法を考えること
			TVP.StorageMediaManager.setCurrentMediaName("file");
		}

		TVP.ProjectDir = TVP.StorageMediaManager.normalizeStorageName(buf,null);
		Storage.setCurrentDirectory(TVP.ProjectDir);
		TVP.NativeProjectDir = buf;
	}
	/**
	 *
	 * @return 本体のあるパス(カレントディレクトリを返す)
	 * @throws TJSException
	 */
	public static String getAppPath() throws TJSException {
		String path = System.getProperty("user.dir");
		char last = path.charAt(path.length()-1);
		if( last != '/' && last != '\\' ) {
			path += '/';
		}
		path = TVP.StorageMediaManager.normalizeStorageName(path, null);
		return path;
	}
	public static String getPersonalPath() throws TJSException {
		String path = System.getProperty("user.home");
		char last = path.charAt(path.length()-1);
		if( last != '/' && last != '\\' ) {
			path += '/';
		}
		path = TVP.StorageMediaManager.normalizeStorageName(path, null);
		return path;
	}
	public static String getNativeAppPath() throws TJSException {
		return System.getProperty("user.dir");
	}
	public static String getNativePersonalPath() {
		return System.getProperty("user.home");
	}
	public void initializeSaveDataPath(boolean stop_after_datapath_got) throws TJSException {
		// read datapath
		String prop = TVP.Properties.getProperty("datapath","$(exepath)\\savedata");
		if( File.separatorChar == '/' ) {
			if( prop.indexOf('\\') != -1 ) {
				prop = prop.replace("\\","/");
			}
		} else if( File.separatorChar == '\\' ) {
			if( prop.indexOf('/') != -1 ) {
				prop = prop.replace("/","\\");
			}
		}
		if( prop.indexOf("$(exepath)") >= 0 ) {
			prop = prop.replace("$(exepath)", getNativeAppPath());
		}
		if( prop.indexOf("$(appdatapath)") >= 0 ) {
			prop = prop.replace("$(appdatapath)", getNativePersonalPath());
		}
		if( prop.indexOf("$(personalpath)") >= 0 ) {
			prop = prop.replace("$(personalpath)", getNativePersonalPath());
		}
		if( prop.indexOf("$(vistapath)") >= 0 ) {
			prop = prop.replace("$(vistapath)", getNativeAppPath());
		}
		TVP.NativeDataPath = prop;

		if(stop_after_datapath_got) return;

		// set data path
		TVP.DataPath = TVP.StorageMediaManager.normalizeStorageName(TVP.NativeDataPath,null);
		DebugClass.addImportantLog( "(info) Data path : " + TVP.DataPath );

		// set log output directory
		TVP.DebugLog.setLogLocation(TVP.NativeDataPath);
	}
}
