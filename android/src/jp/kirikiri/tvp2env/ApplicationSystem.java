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


import java.io.File;
import java.lang.ref.WeakReference;
import java.util.prefs.Preferences;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.os.Handler;
import android.widget.EditText;

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

public class ApplicationSystem extends Application {

	public static int
		MB_OK = 1,
		MB_ICONSTOP = 2;

	private boolean Terminated;
	private boolean TerminateOnWindowClose;
	private boolean TerminateOnNoWindowStartup;
	private int TerminateCode;
	public BaseActivity CurrentContext;
	private boolean mYesNoResult;
	private EditText mEditInput;
	private String mInputText;

	private static final int VersionMajor = 0;
	private static final int VersionMinor = 0;
	private static final int VersionRelease = 1;
	private static final int VersionBuild = 0;

	public static void initializeSystem() {
		Font.initialize();
		NativeImageLoader.initialize();
		RippleTransHandler.initialize();
		SoundStream.initialize();
		PNGLoader.initialize();
	}
	public static void finalizeApplication() {
		Font.finalizeApplication();
		NativeImageBuffer.finalizeApplication();
		NativeImageLoader.finalizeApplication();
		RippleTransHandler.finalizeApplication();
		SoundStream.finalizeApplication();
		PNGLoader.finalizeApplication();
	}

	public ApplicationSystem() {
		//Terminated = false;
		TerminateOnWindowClose = true;
		TerminateOnNoWindowStartup = true;
		//TerminateCode = 0;
		TVP.Application = new WeakReference<ApplicationSystem>(this);
	}
	public void setCurrentContext( BaseActivity ctx ) {
		CurrentContext = ctx;
	}

	public void messageBox( final String caption, final String title, final int flags ) {
		if( CurrentContext != null ) {
			CurrentContext.setErrorMessage(caption);
			/*
			Handler hander = CurrentContext.getHandler();
			hander.post( new Runnable() {
				public void run() {
					CurrentContext.setErrorMessage(caption);
					//CurrentContext.toastMessage(caption);
				}
			} );
			*/
		}
	}
	public void showSimpleMessageBox( final String text, final String caption ) {
		new AlertDialog.Builder(this)
		.setTitle(caption)
		.setMessage(text)
		.setPositiveButton("OK", null)
		.show();
	}
	public String InputQuery(String caption, String prompt, String value ) {
		mInputText = null;
		mEditInput = new EditText(this);
		new AlertDialog.Builder(this)
		.setTitle(prompt)
		.setView(mEditInput)
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				mInputText = mEditInput.getText().toString();
			}
		})
		.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				mInputText = null;
			}
		})
		.show();
		return mInputText;
	}
	public String getInputText() { return mInputText; }
	public boolean showYesNoDialog(String title, String message ) {
		mYesNoResult = false;
		new AlertDialog.Builder(this)
		.setTitle(title)
		.setMessage(message)
		.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				mYesNoResult = true;
			}
		})
		.setNegativeButton("No", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				mYesNoResult = false;
			}
		})
		.show();
		return mYesNoResult;
	}
	public boolean getYesNoResult() { return mYesNoResult; }


	public void terminateAsync(int code) {
		// do "A"synchronous temination of application
		Terminated = true;
		TerminateCode = code;
		TJS.IsTarminating = true;
		TVP.IsTarminating = true;

		SystemInitializer.systemUninitialize();
		/*
		if( CurrentContext != null ) {
			Handler hander = CurrentContext.getHandler();
			hander.post( new Runnable() {
				public void run() {
					if( CurrentContext != null ) CurrentContext.finish();
				}
			} );
		}
		*/
		android.os.Process.killProcess(android.os.Process.myPid());
		//System.exit(code);
	}

	public void terminateSync(int code) {
		TJS.IsTarminating = true;
		TVP.IsTarminating = true;
		// do synchronous temination of application (never return)
		SystemInitializer.systemUninitialize();
		/*
		if( CurrentContext != null ) {
			Handler hander = CurrentContext.getHandler();
			hander.post( new Runnable() {
				public void run() {
					if( CurrentContext != null ) CurrentContext.finish();
				}
			} );
		}
		*/
		android.os.Process.killProcess(android.os.Process.myPid());
		//System.exit(code);
	}
	public void suspendSync() {
		TJS.IsTarminating = true;
		TVP.IsTarminating = true;
		SystemInitializer.systemUninitialize();
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

	public Handler getHandler() {
		return CurrentContext.getHandler();
	}
	public EventHandleThread getVMThread() {
		return CurrentContext.getVMThread();
	}

	public void registerFileMedia(StorageMediaManager storage ) throws TJSException {
		// asset 読み込み用のメディアを登録する
		StorageMedia filemedia = new AssetMedia();
		storage.register( filemedia );

		// ファイル読み込み用のメディアを登録する
		filemedia = new FileMedia();
		storage.register( filemedia );
		/*
		if( File.separatorChar == '/' ) {
			// TODO Linux Mac の時、もう少しいい方法を考えること
			storage.setCurrentMediaName( "file" );
		}
		*/
		String path = TVP.Properties.getProperty("target", "asset:///");
		if( path.startsWith("asset") ) {
			storage.setCurrentMediaName( "asset" );
		} else {
			storage.setCurrentMediaName( "file" );
		}
	}

	public void initializeDataPath() throws TJSException {
		TVP.ProjectDirSelected = true;
		TVP.ProjectDir = TVP.Properties.getProperty("target", "asset:///");
		TVP.ProjectDir = replaceDataPath(TVP.ProjectDir);
		Storage.setCurrentDirectory(TVP.ProjectDir);
		TVP.NativeProjectDir = "";
	}
	//Environment.getDataDirectory().getPath(); // /dataなど
	//Environment.getDownloadCacheDirectory().getPath(); // cacheなど
	/**
	 *
	 * @return 本体のあるパス(カレントディレクトリを返す)
	 * @throws TJSException
	 */
	public static String getAppPath() throws TJSException {
		Context context = TVP.Application.get();
		String files = context.getFilesDir().getAbsolutePath();
		StringBuilder path = new StringBuilder();
		path.append("file://");
		path.append(files);
		path.append('/');
		return path.toString();
	}
	public static String getPersonalPath() throws TJSException {
		String sdpath = Environment.getExternalStorageDirectory().getAbsolutePath();
		Context context = TVP.Application.get();
		StringBuilder path = new StringBuilder();
		path.append("file://");
		path.append(sdpath);
		path.append('/');
		path.append(context.getPackageName());
		path.append('/');
		return path.toString();
	}
	public static String getNativeAppPath() throws TJSException {
		Context context = TVP.Application.get();
		String files = context.getFilesDir().getAbsolutePath();
		StringBuilder path = new StringBuilder();
		path.append(files);
		path.append('/');
		return path.toString();
	}
	public static String getNativePersonalPath() {
		String sdpath = Environment.getExternalStorageDirectory().getAbsolutePath();
		Context context = TVP.Application.get();
		StringBuilder path = new StringBuilder();
		path.append(sdpath);
		path.append('/');
		path.append(context.getPackageName());
		path.append('/');
		return path.toString();
	}
	public static String getAppDataPath() {
		Context context = TVP.Application.get();
		return context.getFilesDir().getAbsolutePath();
	}
	public static String getExternalPath() {
		return Environment.getExternalStorageDirectory().getAbsolutePath();
	}
	public static String getExternalAppDataPath() {
		String sdpath = Environment.getExternalStorageDirectory().getAbsolutePath();
		Context context = TVP.Application.get();
		StringBuilder path = new StringBuilder();
		path.append(sdpath);
		path.append("/Android/data/");
		path.append(context.getPackageName());
		path.append("/files/");
		return path.toString();
	}
	public static String getPackageNameStr() {
		Context context = TVP.Application.get();
		return context.getPackageName();
	}
	public static String replaceDataPath( String src ) {
		Context context = TVP.Application.get();
		if( File.separatorChar == '/' ) {
			if( src.indexOf('\\') != -1 ) {
				src = src.replace("\\","/");
			}
		} else if( File.separatorChar == '\\' ) {
			if( src.indexOf('/') != -1 ) {
				src = src.replace("/","\\");
			}
		}
		if( src.indexOf("$(appdatapath)") >= 0 ) {
			src = src.replace("$(appdatapath)", context.getFilesDir().getAbsolutePath() );
		}
		if( src.indexOf("$(externalpath)") >= 0 ) {
			src = src.replace("$(externalpath)", Environment.getExternalStorageDirectory().getAbsolutePath() );
		}
		if( src.indexOf("$(externalappdatapath)") >= 0 ) {
			String sdpath = Environment.getExternalStorageDirectory().getAbsolutePath();
			StringBuilder path = new StringBuilder();
			path.append(sdpath);
			path.append("/Android/data/");
			path.append(context.getPackageName());
			path.append("/files");
			src = src.replace("$(externalappdatapath)", path.toString());
		}
		if( src.indexOf("$(package)") >= 0 ) {
			src = src.replace("$(package)", context.getPackageName() );
		}
		/*
		if( src.indexOf(':') < 0 ) { // : がない時は、file://とみなす
			if( src.charAt(0) == '/' ) {
				src = "file://" + src;
			} else {
				src = "file:///" + src;
			}
		}*/
		return src;
	}

	/**
	 * セーブデータを保存するパスを設定する
	 * @param stop_after_datapath_got
	 * @throws TJSException
	 */
	public void initializeSaveDataPath(boolean stop_after_datapath_got) throws TJSException {
		// read datapath
		Context context = TVP.Application.get();
		String prop = TVP.Properties.getProperty("datapath","$(externalappdatapath)\\savedata");
		if( File.separatorChar == '/' ) {
			if( prop.indexOf('\\') != -1 ) {
				prop = prop.replace("\\","/");
			}
		} else if( File.separatorChar == '\\' ) {
			if( prop.indexOf('/') != -1 ) {
				prop = prop.replace("/","\\");
			}
		}
		if( prop.indexOf("$(appdatapath)") >= 0 ) {
			prop = prop.replace("$(appdatapath)", context.getFilesDir().getAbsolutePath() );
		}
		if( prop.indexOf("$(externalpath)") >= 0 ) {
			prop = prop.replace("$(externalpath)", Environment.getExternalStorageDirectory().getAbsolutePath() );
		}
		if( prop.indexOf("$(externalappdatapath)") >= 0 ) {
			String sdpath = Environment.getExternalStorageDirectory().getAbsolutePath();
			StringBuilder path = new StringBuilder();
			path.append(sdpath);
			path.append("/Android/data/");
			path.append(context.getPackageName());
			path.append("/files");
			prop = prop.replace("$(externalappdatapath)", path.toString());
		}
		if( prop.indexOf("$(package)") >= 0 ) {
			prop = prop.replace("$(package)", context.getPackageName() );
		}
		if( prop.indexOf(':') < 0 ) { // : がない時は、file://とみなす
			if( prop.charAt(0) == '/' ) {
				prop = "file://" + prop;
			} else {
				prop = "file:///" + prop;
			}
		}
		TVP.NativeDataPath = prop;

		if(stop_after_datapath_got) return;

		// set data path
		TVP.DataPath = TVP.StorageMediaManager.normalizeStorageName(TVP.NativeDataPath,null);
		DebugClass.addImportantLog( "(info) Data path : " + TVP.DataPath );

		// set log output directory
		TVP.DebugLog.setLogLocation(TVP.NativeDataPath);
		/*
		TVP.NativeDataPath = ApplicationSystem.getNativePersonalPath() + "savedata";
		// set data path
		TVP.DataPath = "file://" + TVP.NativeDataPath;
		DebugClass.addImportantLog( "(info) Data path : " + TVP.DataPath );
		// set log output directory
		TVP.DebugLog.setLogLocation(TVP.DataPath);
		*/
	}
}
