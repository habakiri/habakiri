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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

import jp.kirikiri.tjs2.BinaryStream;
import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tjs2.VariantException;
import jp.kirikiri.tvp2.TVP;
import jp.kirikiri.tvp2.base.ScriptsClass;
import jp.kirikiri.tvp2.base.SystemInitializer;
import jp.kirikiri.tvp2.base.TVPSystem;
import jp.kirikiri.tvp2.utils.DebugClass;
import jp.kirikiri.tvp2.visual.LayerNI;
//import dalvik.system.VMRuntime;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

public class BaseActivity extends Activity implements MediaPlayer.OnCompletionListener {
	static final int DIALOG_MAIN_MENU_ID = 0;
	static final int DIALOG_LICENSE_ID = 1;
	static final int DIALOG_MENU_ID = 2;

	// private static final long INITIAL_HEAP_SIZE = 4*1024*1024;

	private static final String PROP_FILE_NAME = "engine.properties";
	private static final String DATA_INIT_FILE_NAME = "datainitialize.txt";

	//private long mOldHeapSize;
	private Handler mHandler;
	private ImagePanel mPanel;
	WindowForm mForm;
	private EventHandleThread mVMThread;

	// for Menu
	boolean mIsShowMenu;
	private int mMenuPage;
	private MenuView mMenuView;
	private MenuItem mCurrentMenu;

	private static Method Func_getExternalFilesDir;

	private int mCurrentOrientation;

	private ArrayList<Object> mDataInitCommand;
	private FileDownloadTask mDownloadTask;
	private MD5CheckTask mMD5CheckTask;
	private FileCopyTask mFileCopyTask;
	private int mRetryCount;
	// for movie
	//private boolean mIsShowLayer;

	public static File getExternalFilesDirRefrect( String type ) {
		try {
			if (Func_getExternalFilesDir == null) {
				try {
					Func_getExternalFilesDir = Context.class.getMethod("getExternalFilesDir", new Class[] {});
				} catch (NoSuchMethodException nsme) {
					return null; // メソッド得られず
				}
			}
			if (Func_getExternalFilesDir != null) {
				return (File)Func_getExternalFilesDir.invoke(type);
			} else {
				return null;
			}
		} catch (InvocationTargetException ite) {
			Throwable cause = ite.getCause();
			if (cause instanceof RuntimeException) {
				throw (RuntimeException) cause;
			} else if (cause instanceof Error) {
				throw (Error) cause;
			} else {
				throw new RuntimeException(ite);
			}
		} catch (IllegalAccessException ie) {
			return null;
		}
	}
	/**
	 * セーブフォルダを得る
	 * @return セーブフォルダパス
	 */
	private String getDefaultSaveFolder() {
		File file = getExternalFilesDirRefrect(null);
		if( file != null ) {
			return file.getAbsolutePath();
		} else {
			return Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/" + getPackageName() + "/files/";
		}
	}
	/**
	 * license.txt が存在しなかったら生成しコピーする
	 */
	private void copyLicenseText() {
		final String dataPath = getDefaultSaveFolder();
		final String path = getDefaultSaveFolder() + "license.txt";
		File licenseFile = new File(path);
		if( licenseFile.exists() == false ) {
			File dataFolder = new File(dataPath);
			dataFolder.mkdirs();
            try {
				FileOutputStream output = new FileOutputStream(licenseFile);
				//InputStream input = getResources().openRawResource(R.raw.license);
				InputStream input = getAssets().open( "license.txt" );
				final int DEFAULT_BUFFER_SIZE = 1024 * 4;
				byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
				int n = 0;
				try {
					while (-1 != (n = input.read(buffer))) {
						output.write(buffer, 0, n);
					}
				} catch (IOException e) {
				} finally {
					input.close();
					output.close();
				}
				input.close();
				output.close();
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
			}
		}
	}
	/**
	 * SD が書き込み可能でマウントされているかどうか調べる
	 * @return true : 書き込み可能 / false : マウントされていないか読み込み専用
	 */
	private boolean checkSD() {
		String state = Environment.getExternalStorageState();
		if( Environment.MEDIA_MOUNTED.equals(state) ) {
			return true; // 読み書き可能
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			return false;	// 読み込みのみ可能なのでセーブできない
		} else {
			return false;	// マウントされていない
		}
	}

	private void loadProperties( final String filename ) {
		if( TVP.Properties == null ) TVP.Properties = new Properties();
		try {
			//@SuppressWarnings("rawtypes")
			//Class c = TVP.class;
			//InputStream is = c.getResourceAsStream(filename);
			InputStream is = getAssets().open( filename );
			if( is == null ) {
				is = new FileInputStream(filename);
			}
			//InputStream br = new BufferedReader(new InputStreamReader(is));
			//TVP.Properties.load(br);
			TVP.Properties.load(is);
			//br.close();
			is.close();
		} catch (IOException e) {
		}
	}
	class LocalTouchEvent implements Runnable{
		int mType;
		int mX;
		int mY;
		int mMeta;
		public LocalTouchEvent( int type, int x, int y, int meta ) {
			mType = type;
			mX = x;
			mY = y;
			mMeta = meta;
		}
		@Override
		public void run() {
			if( mForm == null ) return;

			if( mIsShowMenu ) {
				onTouchMenu( mType, mX, mY );
				return;
			}
			int meta = mMeta;
			switch( mType ) {
			case MotionEvent.ACTION_DOWN:
				mForm.mousePressed( mX, mY, meta, WindowForm.mbLeft, 1 );
				break;
			case MotionEvent.ACTION_UP:
				mForm.mouseReleased( mX, mY, meta, WindowForm.mbLeft, 1 );
				mForm.mouseClicked( 1 );
				break;
			case MotionEvent.ACTION_MOVE:
				mForm.mouseMoved( mX, mY, meta, 1 );
				break;
			}
		}
	}
	class LocalKeyEvent implements Runnable {
		int mType;
		int mKeyCode;
		int mMeta;
		public LocalKeyEvent( int type, int keycode, int meta ) {
			mType = type;
			mKeyCode = keycode;
			mMeta = meta;
		}
		@Override
		public void run() {
			if( mForm == null ) return;

			switch( mType ) {
			case KeyEvent.ACTION_DOWN: {
				int keyCode = mKeyCode;
				if( mIsShowMenu ) {
					onKeyDownMenu( mKeyCode, 0 );
				} else if( keyCode == KeyEvent.KEYCODE_MENU ) {
					MenuItem root = mForm.getMainMenu();
					if( root != null && root.getVisible() && root.countVisibleItem() > 0 ) {
						onKeyDownMenu( mKeyCode, 0 );
					} else {
						mForm.keyPressed( VirtualKey.VK_ESCAPE, 0 );
					}
				} else {
					if( keyCode == KeyEvent.KEYCODE_MENU || keyCode == KeyEvent.KEYCODE_BACK ) {
						keyCode = VirtualKey.VK_ESCAPE;
					}
					mForm.keyPressed( keyCode, 0 );
				}
				break;
			}
			case KeyEvent.ACTION_UP: {
				int keyCode = mKeyCode;
				if( mIsShowMenu ) {
					onKeyUpMenu( mKeyCode, 0 );
				} else if( keyCode == KeyEvent.KEYCODE_MENU ) {
					MenuItem root = mForm.getMainMenu();
					if( root != null && root.getVisible() && root.countVisibleItem() > 0 ) {
						onKeyUpMenu( mKeyCode, 0 );
					} else {
						mForm.keyReleased( VirtualKey.VK_ESCAPE, 0 );
					}
				} else {
					if( keyCode == KeyEvent.KEYCODE_MENU  || keyCode == KeyEvent.KEYCODE_BACK ) {
						keyCode = VirtualKey.VK_ESCAPE;
					}
					mForm.keyReleased( mKeyCode, 0 );
				}
				break;
			}
			}
		}
	}
	class LocalWindowEvent implements Runnable {
		private boolean mIsActive;
		public LocalWindowEvent( boolean isActive ) {
			mIsActive = isActive;
		}
		@Override
		public void run() {
			if( mForm == null ) return;

			if( mIsActive ) mForm.doActiveWindow();
			else mForm.doDeactiveWindow();
		}
	}
	class TerminateVMThreadEvent implements Runnable {
		@Override
		public void run() {
			if( mVMThread != null ) mVMThread.terminateThread();
		}
	}
	private final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;
	//private final int FP = ViewGroup.LayoutParams.FILL_PARENT;
	FrameLayout mFrameLayout;
	FrameLayout mMovieFrame;
	SurfaceView mMovieView;
	MovePlayCallback mMoviePlayer;

	class OpneMovieEvent implements Runnable {
		private BinaryStream mSrc;
		private Rect mRect;
		private boolean mIsLayerHide;
		private boolean mVisible;
		private VideoOverlay mVideoOverlay;
		public OpneMovieEvent( VideoOverlay videoOverlay, BinaryStream src, Rect rect, boolean islayerhide, boolean visible ) {
			mSrc = src;
			mRect = rect;
			mIsLayerHide = islayerhide;
			mVisible = visible;
			mVideoOverlay = videoOverlay;
		}

		@Override
		public void run() {
			openMovie( mVideoOverlay, mSrc, mRect, mIsLayerHide, mVisible );
			mSrc = null;
			mRect = null;
			mVideoOverlay = null;
		}
	}
	void openMovie( VideoOverlay overlay, BinaryStream src, Rect rect, boolean ishidelayer, boolean visible ) {
		try {
			if( mForm == null ) return;
			//Log.v("Movie","do openVideo");

			final int w = mPanel.getWidth();
			final int h = mPanel.getHeight();
			final int iw = mForm.getInnerWidth();
			final int ih = mForm.getInnerHeight();
			mFrameLayout = new FrameLayout(this);
			setContentView(mFrameLayout);
			mFrameLayout.addView( mPanel, new ViewGroup.LayoutParams(WC, WC));

			mMovieFrame = new FrameLayout(this);
			if( visible ) {
				mMovieFrame.setVisibility( View.VISIBLE );
			} else {
				mMovieFrame.setVisibility( View.INVISIBLE );
			}
			//if( ismoviefront ) {
			//	mFrameLayout.addView( mMovieFrame, 0, new ViewGroup.LayoutParams(WC, WC));
			//} else {
				mFrameLayout.addView( mMovieFrame, new ViewGroup.LayoutParams(WC, WC));
			//}
			//MovePlayCallback.adjustImageFrame( mFrameLayout, iw, ih, w, h, rect );

			mMovieView =  new SurfaceView(this);
			SurfaceHolder holder = mMovieView.getHolder();
			// 新しいバージョンでは SurfaceHolder.setType は廃止されたようだが、古いバージョンではこれがないとうまく再生できない
			holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
			mMoviePlayer = new MovePlayCallback( src, mMovieFrame, this, rect, w, h, iw, ih );
			holder.addCallback( mMoviePlayer );
			mMovieFrame.addView( mMovieView, new ViewGroup.LayoutParams(WC, WC));
			overlay.setMoviePlayer(mMoviePlayer);
			mPanel.setHideLayer(ishidelayer);
			try {
				mPanel.show();
			} catch (TJSException e) {
			}
		} finally {
			//Log.v("Movie","interrupt");
			//mVMThread.interrupt();
		}
	}
	public void interruptVMThread() {
		//Log.v("Movie","interrupt");
		mVMThread.interrupt();
	}
	public void openVideo( VideoOverlay videoOverlay, BinaryStream src, Rect rect, boolean ishidelayer, boolean visible ) {
		//Log.v("Movie","post openVideo");
		mHandler.post( new OpneMovieEvent(videoOverlay,src,rect,ishidelayer,visible) );
	}

	private void changeNormalSurface() {
		if( mFrameLayout != null ) {
			mFrameLayout.removeAllViews();
			mFrameLayout = null;
		}
		if( mMovieFrame != null ) {
			mMovieFrame.removeAllViews();
			mMovieFrame = null;
		}
		setContentView( mPanel );
		mPanel.setHideLayer(false);
		mMovieView = null;
		mMoviePlayer = null;
		try {
			show();
		} catch (TJSException e) {
		}
	}
	private void loadProp() {
		loadProperties(PROP_FILE_NAME);
		String ishide = TVP.Properties.getProperty("purge_on_hide", "true" );
		if( "true".equals(ishide) || "yes".equals(ishide) ) {
			LayerNI.PurgeOnHide = true;
		} else {
			LayerNI.PurgeOnHide = false;
		}
		String latealloc = TVP.Properties.getProperty("late_image_allocate", "true" );
		if( "true".equals(latealloc) || "yes".equals(latealloc) ) {
			NativeImageBuffer.LATE_IMAGE_ALLOCATE = true;
		} else {
			NativeImageBuffer.LATE_IMAGE_ALLOCATE = false;
		}
	}

	private void startGameVM() {
		// スクリーン常時ON を解除
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		ApplicationSystem app = (ApplicationSystem)getApplication();
		app.setCurrentContext( this );
		if( TVP.Application == null || TVP.Application.get() == null ) {
			TVP.Application = new WeakReference<ApplicationSystem>(app);
		}
		mVMThread = new EventHandleThread( new Runnable() {
			@Override
			public void run() {
				long startTime = System.currentTimeMillis();
				doScript();
				long time = System.currentTimeMillis() - startTime;
				try {
					showTimeAndFinish(time);
				} catch (TJSException e) {
				}
			}
		});
		//SoundStream.resumeAllPlayer();
		//mVMThread.post( new LocalWindowEvent(true) );
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if( checkSD() == false ) {
			Toast.makeText( this, SystemMessage.SD_CARD_NOT_READY, Toast.LENGTH_LONG ).show();
			finish();
			return;
		}
		copyLicenseText();

		// フルスクリーン化
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		//VMRuntime.getRuntime().setMinimumHeapSize(INITIAL_HEAP_SIZE);
		// VMRuntime.getRuntime().gcSoftReferences();
		mHandler = new Handler();

		//mMotionEventHander = new MotionEventHander();
		mPanel = new ImagePanel(this);
		setContentView( mPanel );
		//NativeImageBuffer.showMemoryInfo();

		MenuView.initialize(this);
		mMenuView = new MenuView();
		mCurrentMenu = null;
		mIsShowMenu = false;
		mForm = null;

		Configuration config = getResources().getConfiguration();
		mCurrentOrientation = config.orientation;
	}
	@Override
	protected void onRestart() {
		super.onRestart();
	}
	@Override
	protected void onResume() {
		super.onResume();

		if( checkSD() == false ) {
			Toast.makeText( this, SystemMessage.SD_CARD_NOT_READY, Toast.LENGTH_LONG ).show();
			finish();
			return;
		}

		loadProp();

		mRetryCount = 2;
		if( dataInitialize() ) {
			// コピー処理やダウンロード処理が非同期で行われるので、それが完了するまでゲームを起動しない
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // スクリーンをOFFにしないようにする
		} else {
			startGameVM();
		}
	}
	@Override
	public void onPause() {
		super.onPause();
		if( mVMThread == null ) { // VM が起動していないのなら、まだデータコピー等の処理中のはず
			// データコピー等の処理をキャンセルする
			if( mDownloadTask != null ) { mDownloadTask.cancel(true); }
			if( mMD5CheckTask != null ) { mMD5CheckTask.cancel(true); }
			if( mFileCopyTask != null ) { mFileCopyTask.cancel(true); }
		}
		if( mVMThread != null ) mVMThread.post( new LocalWindowEvent(false) );
		/*
		mVMThread.post( new TerminateVMThreadEvent() ); // VM を停止するイベントを入れる

		// 一時停止を実現する場合 VM を止めた後、sound を pause しないとダメか。リジューム時は逆にして。
		//SoundStream.pauseAllPlayer(); // 動画も必要、後、VMも止めた方がいいな。deactiveが到達した後、あーでもタイマーが動くのか

		SoundStream.stopAllPlayer(); // サウンドが鳴っていたら止める
		TimerThread.uninit(); // タイマースレッドが動いていたら止める

		ApplicationSystem app = (ApplicationSystem)getApplication();
		app.suspendSync();
		*/
		//exitApplication();
		if( mVMThread != null ) mVMThread.post( new TerminateVMThreadEvent() ); // VM を停止するイベントを入れる
		SoundStream.stopAllPlayer(); // サウンドが鳴っていたら止める
		VideoOverlay.stopAllPlayer(); // 動画があったら止める
		TimerThread.uninit(); // タイマースレッドが動いていたら止める
		Configuration config = getResources().getConfiguration();
		setRequestedOrientation(config.orientation);
		if( mVMThread != null ) mVMThread.waitTerminate();
		finish();
	}
	@Override
	public void onDestroy() {
		//exitApplication();
		ApplicationSystem app = (ApplicationSystem)getApplication();
		app.terminateSync(0);
		app.setCurrentContext( null );
		super.onDestroy();
	}
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if( mCurrentOrientation != newConfig.orientation ) {
			if( mForm != null ) {
				mForm.doOrientationChanged();
			}
		}
	}
	/*
	private void exitApplication() {
		mVMThread.post( new Runnable() {
			@Override
			public void run() {
				doExit();
			}
		});
	}
	void doExit() {
		TimerThread.uninit();
		if( mForm != null ) {
			mForm.setPanel(null);
			mForm.windowClosing();
			mForm.windowClosed();
		}
		mForm = null;

		SoundStream.stopAllPlayer();
		ApplicationSystem app = (ApplicationSystem)getApplication();
		app.terminateSync(0);
	}
	*/
	public void exitFromScript() {
		TimerThread.uninit();
		mForm = null;
		SoundStream.stopAllPlayer();
		ApplicationSystem app = (ApplicationSystem)getApplication();
		app.terminateSync(0);
	}

	public void doScript() {
		try {
			//Debug.startMethodTracing("patom");	// プロファイリング開始
			TVP.initialize();
			ScriptsClass.initScriptEnging();

			// banner
			DebugClass.addImportantLog( "Program started on " + TVPSystem.getOSName() );
			DebugClass.addImportantLog( "JVM : " + TVPSystem.getJVMName() );
			DebugClass.addImportantLog( "JRE : " + TVPSystem.getJREName() );

			SystemInitializer.systemInitialize();
			ScriptsClass.initializeStartupScript();

			//Debug.stopMethodTracing();	//プロファイリング終了
		} catch (VariantException e) {
			e.printStackTrace();
		} catch (TJSException e) {
			e.printStackTrace();
		} catch( Exception e ) {
			e.printStackTrace();
		}
	}
	public void showTimeAndFinish( long time ) throws TJSException {
		Log.v("KIRIKIRI", "finish initialize. time : " + time + " msec");
		//Log.v("KIRIKIRI", "top level script time : " + ScriptBlock.mTopLevelTime + " msec");
		//Log.v("KIRIKIRI", "top level script count : " + ScriptBlock.mRunTime + " times");
		if( mForm != null && mForm.isVisible() ) {
			mForm.onDraw( mPanel.getBackbufferCanvas() );
		}
	}

	/**
	 * オプションメニューが表示される度に呼び出される
	 */
	/*
	@Override
	public boolean onPrepareOptionsMenu( Menu menu ) {
		return super.onPrepareOptionsMenu(menu);
	}
	*/
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if( keyCode != KeyEvent.KEYCODE_HOME && mVMThread != null ) { // home キーはスルーする
			int meta = getModifiersToInt( event.getMetaState(), false );
			LocalKeyEvent levent = new LocalKeyEvent( KeyEvent.ACTION_DOWN, keyCode, meta );
			mVMThread.post( levent );
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	/*
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
		return super.onKeyLongPress( keyCode, event );
	}
	*/
	public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
		return super.onKeyMultiple(keyCode, repeatCount, event);
	}
	/*
	public boolean  onKeyShortcut(int keyCode, KeyEvent event) {
		return super.onKeyShortcut( keyCode, event );
	}
	*/
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if( keyCode != KeyEvent.KEYCODE_HOME && mVMThread != null ) { // home キーはスルーする
			int meta = getModifiersToInt( event.getMetaState(), false );
			LocalKeyEvent levent = new LocalKeyEvent( KeyEvent.ACTION_UP, keyCode, meta );
			mVMThread.post( levent );
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}
	// down -> up -> click
	private int mPrevX;
	private int mPrevY;
	public boolean onTouchEvent(MotionEvent event) {
		if(  mVMThread == null ) return super.onTouchEvent(event);
		//int btn = getMouseButtonNumber( event.getButtonState() ); android 4.0 から
		//int type = getMouseToolType( event.getToolType() ); // android 4.0 から

		LocalTouchEvent levent = null;
		int action = event.getAction();
		int meta;
		int x = (int)event.getX();
		int y = (int)event.getY();
		switch( action ) {
		case MotionEvent.ACTION_DOWN:
			meta = getModifiersToInt( event.getMetaState(), true );
			levent = new LocalTouchEvent( MotionEvent.ACTION_DOWN, x, y, meta );
			mPrevX = x;
			mPrevY = y;
			break;
		case MotionEvent.ACTION_UP:
			meta = getModifiersToInt( event.getMetaState(), false );
			levent = new LocalTouchEvent( MotionEvent.ACTION_UP, x, y, meta );
			mPrevX = x;
			mPrevY = y;
			break;
		case MotionEvent.ACTION_MOVE:
			meta = getModifiersToInt( event.getMetaState(), true );
			if( mPrevX != x || mPrevY != y ) {
				levent = new LocalTouchEvent( MotionEvent.ACTION_MOVE, x, y, meta );
				mPrevX = x;
				mPrevY = y;
			}
			break;
		case 7:// == MotionEvent.ACTION_HOVER_MOVE: // 非押下移動も判定出来るようにする
			meta = getModifiersToInt( event.getMetaState(), false );
			if( mPrevX != x || mPrevY != y ) {
				levent = new LocalTouchEvent( MotionEvent.ACTION_MOVE, x, y, meta );
				mPrevX = x;
				mPrevY = y;
			}
			break;
		case MotionEvent.ACTION_CANCEL:
			meta = getModifiersToInt( event.getMetaState(), false );
			levent = new LocalTouchEvent( MotionEvent.ACTION_UP, x, y, meta );
			mPrevX = x;
			mPrevY = y;
			break;
		case MotionEvent.ACTION_OUTSIDE:
			meta = getModifiersToInt( event.getMetaState(), false );
			levent = new LocalTouchEvent( MotionEvent.ACTION_UP, x, y, meta );
			mPrevX = x;
			mPrevY = y;
			break;
		}
		if( levent != null ) {
			//mMotionEventHander.post(levent);
			mVMThread.post( levent );
		}
		/*
		//int btn = getMouseButtonNumber( event.getButtonState() ); android 4.0 から
		//int type = getMouseToolType( event.getToolType() ); // android 4.0 から
		int meta = getModifiersToInt( event.getMetaState(), event.getDownTime() > 0 );
		switch( event.getAction() ) {
		case MotionEvent.ACTION_DOWN:
			if( mForm != null ) mForm.mousePressed( (int)event.getX(), (int)event.getY(), meta, WindowForm.mbLeft, 1 );
			break;
		case MotionEvent.ACTION_UP:
			if( mForm != null ) {
				mForm.mouseReleased( (int)event.getX(), (int)event.getY(), meta, WindowForm.mbLeft, 1 );
				mForm.mouseClicked( 1 );
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if( mForm != null ) mForm.mouseMoved( (int)event.getX(), (int)event.getY(), meta, 1 );
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_OUTSIDE:
		}
		*/
		// if( mForm != null ) mForm.mouseClicked( 1 ); // 1 or 2
		return super.onTouchEvent(event);
	}
	public static final int SS_SHIFT   = 0x01;
	public static final int SS_ALT     = 0x02;
	public static final int SS_CTRL    = 0x04;
	public static final int SS_LEFT    = 0x08;
	public static final int SS_RIGHT   = 0x10;
	public static final int SS_MIDDLE  = 0x20;
	public static final int SS_DOUBLE  = 0x40;
	public static final int SS_REPEAT  = 0x80;
	public static final int SS_ALTGRAPH= 0x100; // AltGraph キー
	public static final int SS_META    = 0x200; // Meta キー
	private static final int META_ALT_MASK = KeyEvent.META_ALT_ON|KeyEvent.META_ALT_LEFT_ON|KeyEvent.META_ALT_RIGHT_ON; // API 11
	private static final int META_SHIFT_MASK = KeyEvent.META_SHIFT_ON|KeyEvent.META_SHIFT_LEFT_ON|KeyEvent.META_SHIFT_RIGHT_ON; // API 11
	private static final int META_CTRL_MASK = 0x00007000; // API 11
	private static final int META_META_MASK = 0x00070000; // API 11
	private static final int getModifiersToInt( int meta, boolean ispressing ) {
		int f = 0;
		if( (meta & META_ALT_MASK) != 0 ) { f += SS_ALT; }
		if( (meta & META_SHIFT_MASK) != 0 ) { f += SS_SHIFT; }
		if( (meta & META_CTRL_MASK) != 0 ) { f += SS_CTRL; }
		if( (meta & META_META_MASK) != 0 ) { f += SS_META; }
		if( ispressing ) { f += SS_LEFT; }
		return f;
		/*
		KeyEvent.META_SYM_ON
		KeyEvent.META_FUNCTION_ON
		KeyEvent.META_CAPS_LOCK_ON
		KeyEvent.META_NUM_LOCK_ON
		KeyEvent.META_SCROLL_LOCK_ON
		*/
	}

	/* android 4.0 から
	private static final int getMouseButtonNumber( int state ) {
		//int getButtonState ()
		if( (state & MotionEvent.BUTTON_PRIMARY) != 0 ) {
			return WindowForm.mbLeft; // 左クリック
		}
		if( (state & MotionEvent.BUTTON_SECONDARY) != 0 ) {
			return WindowForm.mbRight; // 右クリック
		}
		if( (state & MotionEvent.BUTTON_TERTIARY) != 0 ) {
			return WindowForm.mbMiddle; // 真ん中クリック
		}
		if( (state & MotionEvent.BUTTON_FORWARD) != 0 ) { //  マウスのホイールボタンを前に進める
		}
		if( (state & MotionEvent.BUTTON_BACK) != 0 ) { //マウスのホイールボタンを後ろに進める
		}
		return WindowForm.mbLeft;
	}
	private static final int getMouseToolType( int type ) {
		switch( type ) {
		case MotionEvent.TOOL_TYPE_FINGER: //	指
		case MotionEvent.TOOL_TYPE_MOUSE: //	マウス
		case MotionEvent.TOOL_TYPE_STYLUS: //	スタイラス
		case MotionEvent.TOOL_TYPE_ERASER: //	消しゴム
		case MotionEvent.TOOL_TYPE_UNKNOWN: //	その他
		}
	}
	*/

	public boolean onTrackballEvent(MotionEvent event) {
		return super.onTrackballEvent(event);
	}
	/*
	void onClickTopDialog( int item ) {
		switch( item ) {
		case 0: // menu
			showDialog(DIALOG_MENU_ID);
			break;
		case 1: // opensource
			showDialog(DIALOG_LICENSE_ID);
			break;
		case 2: // exit
			exitApplication();
			break;
		}
	}
	void onClickMenuDialog( DialogInterface dialog, int item ) {
	}
	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		switch(id) {
		case DIALOG_MAIN_MENU_ID: {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle( R.string.menu_top_titile );
			builder.setItems(R.array.main_menu, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					onClickTopDialog(item);
				}
			});
			dialog = builder.create();
			break;
		}
		case DIALOG_MENU_ID: { // 仮
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle( R.string.menu_menu );
			builder.setItems(R.array.main_menu, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					onClickMenuDialog(dialog,item);
				}
			});
			dialog = builder.create();
			break;
		}
		case DIALOG_LICENSE_ID: {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle( R.string.lisence );
			InputStream stream = null;
			Resources res = getResources();
			try {
				stream = res.openRawResource( R.raw.license );
				byte[] buffer = new byte[stream.available()];
				while((stream.read(buffer)) != -1) {}
				String s = new String(buffer);
				builder.setMessage(s);
				stream.close();
			} catch( IOException e ) {
				Toast.makeText(this, R.string.faild_lisence_load, Toast.LENGTH_LONG).show();
			}
			dialog = builder.create();
			break;
		}
		default:
			dialog = null;
		}
		return dialog;
	}
	@Override
	protected void onPrepareDialog( int id, Dialog dialog ) {
		if( id == DIALOG_MENU_ID ) {
			if( mForm != null ) {
				if( mForm.getMenuBarVisible() ) {
					MenuItem menu = mForm.getMainMenu();
					if( menu.isEmpty() == false ) {

					}
				}
			}
 		}
	}
	private boolean hasMenu() {
		if( mForm != null ) {
			if( mForm.getMenuBarVisible() ) {
				MenuItem menu = mForm.getMainMenu();
				return ( menu.isEmpty() == false );
			}
		}
		return false;
	}
	*/

	public Handler getHandler() { return mHandler; }
	public EventHandleThread getVMThread() { return mVMThread; }

	public int getWidth() {
		return mPanel.getWidth();
	}
	public int getHeight() {
		return mPanel.getHeight();
	}
	public void show() throws TJSException {
		mPanel.show();
	}
	public boolean setWindowForm( WindowForm form ) {
		if( mForm == null ) {
			mForm = form;
			if( form != null ) form.setPanel(mPanel);
		} else {
			if( form != null ) {
				form.setPanel(null);
			}
		}
		return mForm == form;
	}
	public Canvas getDrawCanvas() throws TJSException {
		return mPanel.getBackbufferCanvas();
	}
	public void toastMessage( final String caption ) {
		Toast.makeText( this, caption, Toast.LENGTH_LONG ).show();
	}
	public void setErrorMessage( final String caption ) {
		mPanel.setErrorMessage( caption );
	}
	public boolean isMainForm( WindowForm form ) {
		if( mForm == null ) return false;
		return mForm == form;
	}
	public void drawMenu( Canvas canvas ) {
		if( mIsShowMenu == false ) return;
		if( mForm != null ) {
			mMenuView.draw(canvas);
		}
	}
	void showMenu() {
		if( mForm != null ) {
			mMenuPage = 0;
			MenuItem m = mForm.getMainMenu();
			mCurrentMenu = m;
			mMenuView.layout( m, mPanel.getWidth(), mPanel.getHeight(), mMenuPage );
			mIsShowMenu = true;
			try {
				mPanel.show();
			} catch (TJSException e) {
			}
		}
	}
	private void hideMenu() {
		mMenuView.clear();
		mMenuPage = 0;
		mIsShowMenu = false;
		try {
			mPanel.show();
		} catch (TJSException e) {
		}
	}
	void backMenu() {
		if( mCurrentMenu != null && mCurrentMenu.getParent() != null ) {
			mCurrentMenu = mCurrentMenu.getParent();
			mMenuPage = 0;
			mMenuView.layout( mCurrentMenu, mPanel.getWidth(), mPanel.getHeight(), mMenuPage );
			try {
				mPanel.show();
			} catch (TJSException e) {
			}
		} else {
			hideMenu();
		}
	}
	boolean onKeyDownMenu(int keyCode, int meta ) {
		return true;
	}
	boolean onKeyUpMenu(int keyCode, int meta ) {
		if( keyCode == KeyEvent.KEYCODE_MENU ) {
			if( mIsShowMenu ) {
				hideMenu();
			} else {
				showMenu();
			}
		} else if( keyCode == KeyEvent.KEYCODE_BACK ) {
			backMenu();
		}
		return true;
	}
	boolean onTouchMenu( int action, int x, int y ) {
		//int action = event.getAction();
		//int x = (int)event.getX();
		//int y = (int)event.getY();
		int hit = -1;
		switch( action ) {
		case MotionEvent.ACTION_DOWN:
			hit = mMenuView.hitTest(x, y);
			if( hit >= 0 ) mMenuView.setPressed(hit);
			else mMenuView.clearPressed();
			try {
				mPanel.show();
			} catch (TJSException e) {}
			break;
		case MotionEvent.ACTION_UP:
			hit = mMenuView.hitTest(x, y);
			if( hit >= 0 ) {
				int next = mMenuView.handleMenu(hit);
				if( next != 0 ) {
					if( next != 2 ) {
						mMenuPage += next;
						if( mMenuPage < 0 ) mMenuPage = 0;
					} else {
						mCurrentMenu = mMenuView.getMenuItem(hit);
						mMenuPage = 0;
					}
					mMenuView.layout( mCurrentMenu, mPanel.getWidth(), mPanel.getHeight(), mMenuPage );
				} else {
					hideMenu();
				}
			}
			try {
				mPanel.show();
			} catch (TJSException e) {}
			break;
		case MotionEvent.ACTION_MOVE:
			hit = mMenuView.hitTest(x, y);
			if( hit >= 0 ) mMenuView.setPressed(hit);
			else mMenuView.clearPressed();
			try {
				mPanel.show();
			} catch (TJSException e) {}
			break;
		case MotionEvent.ACTION_CANCEL:
			mMenuView.clearPressed();
			try {
				mPanel.show();
			} catch (TJSException e) {}
			break;
		}
		return true;
	}
	@Override
	public void onCompletion(MediaPlayer mp) {
		// Log.v("MediapLayer","onCompletion");
		if( mp.isLooping() == false ) {
			// 再生終了
			changeNormalSurface();
		}
	}
	private boolean handleDataInitCommand() {
		while( mDataInitCommand.size() > 0 ) {
			Object obj = mDataInitCommand.get(0);
			if( obj instanceof DataInitializeScript.Download ) {
				DataInitializeScript.Download dl = (DataInitializeScript.Download) obj;
				dl.mPath = ApplicationSystem.replaceDataPath(dl.mPath);
				if( dl.mPath.startsWith("file://") ) {
					dl.mPath = dl.mPath.substring(7); // file:// を切り取る
				}
				File path = new File(dl.mPath);
				if( path.exists() ){
					//&& path.length() != Long.valueOf(dl.mFileSize) ) {
					final long length = path.length();
					final long size = Long.valueOf(dl.mFileSize);
					if( length != size ) {
						path.delete();
					}
				}
				if( path.exists() == false ) {
					// まだダウンロードされていない、ダウンロードする
					startDownload( dl.mUrl, dl.mPath, Integer.valueOf(dl.mFileSize), true );
					return true;
				}
			} else if( obj instanceof DataInitializeScript.Copy ) {
				DataInitializeScript.Copy cp = (DataInitializeScript.Copy)obj;
				cp.mDest = ApplicationSystem.replaceDataPath(cp.mDest);
				if( cp.mDest.startsWith("file://") ) {
					cp.mDest = cp.mDest.substring(7); // file:// を切り取る
				}
				File path = new File(cp.mDest);
				if( path.exists() ) {
					//&& path.length() != Long.valueOf(cp.mFileSize) ) {
					final long length = path.length();
					final long size = Long.valueOf(cp.mFileSize);
					if( length != size ) {
						path.delete();
					}
				}
				if( path.exists() == false ) {
					// まだコピーされていない、コピーする
					copyFiles( cp.mSource, cp.mDest );
					return true;
				}
			} else { // unknwon command, ignore
			}
			mDataInitCommand.remove(0);
		}
		return false;
	}
	private boolean dataInitialize() {
		if( loadDataInitializeScript(DATA_INIT_FILE_NAME) ) {
			if( mDataInitCommand != null ) {
				return handleDataInitCommand();
			}
		}
		return false;
	}
	// datainitialize.txt
	private boolean loadDataInitializeScript( final String filename ) {
		try {
			InputStream is = getAssets().open( filename );
			if( is != null ) {
				mDataInitCommand = new ArrayList<Object>();
				DataInitializeScript.parse(is, mDataInitCommand );
				is.close();
				return true;
			}
		} catch (IOException e) {
			return false;
		}
		return false;
	}

	private void startDownload( String url, String path, int filesize, boolean retry ) {
		mRetryCount--;
		mDownloadTask = new FileDownloadTask(this);
		final String isclean = retry ? "true" : "false";
		mDownloadTask.execute(url,path,String.valueOf(filesize),isclean);
	}
	public void onFinishDownload( boolean result, String path, boolean usercancel ) {
		mDownloadTask = null;
		if( usercancel ) {
			deleteCurrentDataInitFile();
			finish();
			return;
		}
		if( result ) {
			mMD5CheckTask = new MD5CheckTask(this);
			mMD5CheckTask.execute(path);
		} else if( mRetryCount > 0 ) {
			boolean ret = handleDataInitCommand();
			if( ret == false ) {
				Toast.makeText(this, "内部エラー", Toast.LENGTH_LONG).show();
				deleteCurrentDataInitFile();
				finish();
			}
			// retry
		} else {
			Toast.makeText(this, "ダウンロードに失敗しました。", Toast.LENGTH_LONG).show();
			deleteCurrentDataInitFile();
			finish();
		}
	}
	public void onCalcMD5( byte[] digest ) {
		mMD5CheckTask = null;
		if( digest != null ) {
			//Log.v("MD5",digest.toString());
			Object obj = mDataInitCommand.get(0);
			if( obj instanceof DataInitializeScript.Download ) {
				DataInitializeScript.Download dl = (DataInitializeScript.Download) obj;
				byte[] org = asByteArray(dl.mMD5);
				if( Arrays.equals(org ,digest) ) { // MD5 一致
					mDataInitCommand.remove(0);
					if( mDataInitCommand.size() > 0 ) {
						boolean ret = handleDataInitCommand();
						if( ret == false ) {
							Toast.makeText(this, "内部エラー", Toast.LENGTH_LONG).show();
							deleteCurrentDataInitFile();
							finish();
						}
					} else { // スクリプトでこれ以上指定はない, 実行開始
						startGameVM();
					}
				} else { // MD5不一致
					deleteCurrentDataInitFile();
					if( mRetryCount > 0 ) {
						boolean ret = handleDataInitCommand();
						if( ret == false ) {
							Toast.makeText(this, "内部エラー", Toast.LENGTH_LONG).show();
							finish();
						}
						// retry
					} else {
						Toast.makeText(this, "ダウンロードに失敗しました。", Toast.LENGTH_LONG).show();
						finish();
					}
				}
			} else {
				deleteCurrentDataInitFile();
				Toast.makeText(this, "内部エラー", Toast.LENGTH_LONG).show();
				finish();
			}
		} else {
			deleteCurrentDataInitFile();
			Toast.makeText(this, "内部エラー", Toast.LENGTH_LONG).show();
			finish();
		}
	}
	/**
	 * 正常にコピーやダウンロードが終了しなかった時、その時に処理していたファイルを消しておく
	 */
	private void deleteCurrentDataInitFile() {
		while( mDataInitCommand.size() > 0 ) {
			Object obj = mDataInitCommand.get(0);
			if( obj instanceof DataInitializeScript.Download ) {
				DataInitializeScript.Download dl = (DataInitializeScript.Download) obj;
				dl.mPath = ApplicationSystem.replaceDataPath(dl.mPath);
				File path = new File(dl.mPath);
				if( path.exists() ) {
					path.delete();
				}
			} else if( obj instanceof DataInitializeScript.Copy ) {
				DataInitializeScript.Copy cp = (DataInitializeScript.Copy)obj;
				cp.mDest = ApplicationSystem.replaceDataPath(cp.mDest);
				if( cp.mDest.startsWith("file://") ) {
					cp.mDest = cp.mDest.substring(7); // file:// を切り取る
				}
				File path = new File(cp.mDest);
				if( path.exists() ) {
					path.delete();
				}
			} else { // unknwon command, ignore
			}
			mDataInitCommand.remove(0);
		}
	}
	/**
	 * 16進数の文字列をバイト配列に変換する。
	 * @param hex 16進数の文字列
	 * @return バイト配列
	 */
	public static byte[] asByteArray(String hex) {
		byte[] bytes = new byte[hex.length() / 2];
		for( int i= 0; i< bytes.length; i++ ) {
			bytes[i] = (byte)Integer.parseInt( hex.substring(i*2, (i+1) * 2), 16 );
		}
		return bytes;
	}
	private void copyFiles(String source, String dest) {
		mFileCopyTask = new FileCopyTask( this );
		mFileCopyTask.execute( source, dest );
	}
	public void onFinishFileCopy(boolean result) {
		if( result ) {
			mDataInitCommand.remove(0);
			if( mDataInitCommand.size() > 0 ) {
				boolean ret = handleDataInitCommand();
				if( ret == false ) {
					deleteCurrentDataInitFile();
					Toast.makeText(this, "内部エラー", Toast.LENGTH_LONG).show();
					finish();
				}
			} else { // スクリプトでこれ以上指定はない, 実行開始
				startGameVM();
			}
		} else {
			deleteCurrentDataInitFile();
			Toast.makeText(this, "ファイルコピーに失敗しました。", Toast.LENGTH_LONG).show();
			finish();
		}
	}
}
