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
package jp.kirikiri.tvp2;

import jp.kirikiri.tjs2.ConsoleOutput;
import jp.kirikiri.tjs2.Dispatch2;
import jp.kirikiri.tjs2.Error;
import jp.kirikiri.tjs2.Holder;
import jp.kirikiri.tjs2.NativeClassConstructor;
import jp.kirikiri.tjs2.NativeClassMethod;
import jp.kirikiri.tjs2.NativeClassProperty;
import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tjs2.Variant;
import jp.kirikiri.tjs2.VariantException;
import jp.kirikiri.tvp2.base.Archive;
import jp.kirikiri.tvp2.base.ArchiveCache;
import jp.kirikiri.tvp2.base.BaseInputEvent;
import jp.kirikiri.tvp2.base.Storage;
import jp.kirikiri.tvp2.base.StorageMediaManager;
import jp.kirikiri.tvp2.base.SystemInitializer;
import jp.kirikiri.tvp2.base.XP3Archive;
import jp.kirikiri.tvp2.base.CompactEvent;
import jp.kirikiri.tvp2.base.AutoPath;
import jp.kirikiri.tvp2.base.XP3ArchiveStream;
import jp.kirikiri.tvp2.msg.Message;
import jp.kirikiri.tvp2.sound.WaveSoundBufferNI;
import jp.kirikiri.tvp2.utils.ConsoleClass;
import jp.kirikiri.tvp2.utils.ControllerClass;
import jp.kirikiri.tvp2.utils.KAGParserNI;
import jp.kirikiri.tvp2.utils.KeyStateHelper;
import jp.kirikiri.tvp2.utils.Random;
import jp.kirikiri.tvp2.utils.TJS2ConsoleOutputGateway;
import jp.kirikiri.tvp2.utils.TJS2DumpOutputGateway;
import jp.kirikiri.tvp2.utils.TimerNI;
import jp.kirikiri.tvp2.visual.BaseBitmap;
import jp.kirikiri.tvp2.visual.GraphicsLoader;
import jp.kirikiri.tvp2.visual.LayerNI;
import jp.kirikiri.tvp2.visual.MenuItemClass;
import jp.kirikiri.tvp2.visual.PassThroughDrawDeviceClass;
import jp.kirikiri.tvp2.visual.Rect;
import jp.kirikiri.tvp2.visual.RegionRect;
import jp.kirikiri.tvp2.visual.SimpleImageProviderObject;
import jp.kirikiri.tvp2.visual.TempBitmapHolder;
import jp.kirikiri.tvp2.visual.TransHandlerProvider;
import jp.kirikiri.tvp2.visual.WindowNI;
import jp.kirikiri.tvp2.visual.FontClass;
import jp.kirikiri.tvp2.visual.WindowList;
import jp.kirikiri.tvp2.base.EventManager;
import jp.kirikiri.tvp2.utils.ScnearioCache;
import jp.kirikiri.tvp2.utils.LogStreamHolder;
import jp.kirikiri.tvp2.utils.DebugLog;
import jp.kirikiri.tvp2env.ApplicationSystem;
import jp.kirikiri.tvp2env.CrossFadeTransHandlerProvider;
import jp.kirikiri.tvp2env.MainForm;
import jp.kirikiri.tvp2env.MosaicTransHandlerProvider;
import jp.kirikiri.tvp2env.MouseCursor;
import jp.kirikiri.tvp2env.NativeImageBuffer;
import jp.kirikiri.tvp2env.RippleTransHandlerProvider;
import jp.kirikiri.tvp2env.RotateSwapTransHandlerProvider;
import jp.kirikiri.tvp2env.RotateVanishTransHandlerProvider;
import jp.kirikiri.tvp2env.RotateZoomTransHandlerProvider;
import jp.kirikiri.tvp2env.ScrollTransHandlerProvider;
import jp.kirikiri.tvp2env.SoundMixer;
import jp.kirikiri.tvp2env.TimerThread;
import jp.kirikiri.tvp2env.TurnTransHandlerProvider;
import jp.kirikiri.tvp2env.UniversalTransHandlerProvider;
import jp.kirikiri.tvp2env.VideoOverlay;
import jp.kirikiri.tvp2env.WaveTransHandlerProvider;

import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Properties;

public class TVP {
	private static final String APP_TITLE = "吉里吉里Java";

	public static StorageMediaManager StorageMediaManager;
	public static ArchiveCache ArchiveCache;
	public static Object CreateStreamCS;
	public static CompactEvent CompactEvent;
	public static AutoPath AutoPath;
	public static WindowNI MainWindow;
	public static EventManager EventManager;
	public static WindowList WindowList;
	public static MainForm MainForm;
	public static TimerThread TimerThread;
	public static Properties Properties;
	public static ScnearioCache ScnearioCache;
	public static MouseCursor MouseCursor;
	public static ConsoleOutput ConsoleOutputGetway;
	public static ConsoleOutput DumpOutputGetway;
	public static LogStreamHolder LogStreamHolder;
	public static DebugLog DebugLog;
	public static SoundMixer SoundMixer;
	public static WeakReference<ApplicationSystem> Application;
	public static SimpleImageProviderObject SimpleImageProvider;

	// global enviroment values
	public static long MaxMemory;
	public static boolean ProjectDirSelected;
	public static String ProjectDir;
	public static String NativeProjectDir;
	public static long GraphicCacheSystemLimit; // この辺りのキャッシュ設定は GraphicsLoader に移動した方がいいかも
	public static long GraphicCacheLimit;
	public static long GraphicCacheTotalBytes;
	public static boolean GraphicCacheEnabled;
	public static int GraphicSplitOperationType;
	public static boolean DefaultHoldAlpha;
	public static boolean TerminateOnWindowClose;
	public static boolean TerminateOnNoWindowStartup;
	public static String NativeDataPath;
	public static String DataPath;
	public static String AppTitle;
	public static boolean AppTitleInit;
	public static OutputStream DumpOutputStream;
	public static boolean IsTarminating;
	public static boolean IsLowMemory; // メモリが少ない環境

	private static boolean mSystemEventDisabledState;
	private static FontClass mFontClass;
	private static MenuItemClass mMenuItemClass;
	private static long mStartTick;
	private static KeyStateHelper mKeyStateHelper;
	private static ControllerClass mControllerClass;
	private static ConsoleClass mConsoleClass;
	private static PassThroughDrawDeviceClass mPassThroughDrawDeviceClass;
	private static boolean mTransHandlerProviderInit;
	private static HashMap<String,TransHandlerProvider> mTransHandlerProviders;

	public static NativeClassConstructor ReturnOKConstructor;
	public static NativeClassProperty NotImplProp;
	public static NativeClassMethod NotImplMethod;
	public static NativeClassMethod ReturnOKMethod;

	public static void initialize() throws TJSException {
		if( Properties == null ) Properties = new Properties();

		ReturnOKConstructor = new NativeClassConstructor() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) {
				return Error.S_OK;
			}
		};
		ReturnOKMethod = new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				return Error.S_OK;
			}
		};
		NotImplMethod = new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				return Error.E_NOTIMPL;
			}
		};
		NotImplProp = new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		};

		mStartTick = java.lang.System.currentTimeMillis();
		Random.initialize();
		ApplicationSystem.initializeSystem();
		SystemInitializer.initialize();
		NativeImageBuffer.initialize(); // グラフィック処理用テーブル初期化
		BaseBitmap.initialize();
		GraphicsLoader.initialize();
		WaveSoundBufferNI.initialize();
		BaseInputEvent.initialize();
		Storage.initialize();
		jp.kirikiri.tvp2.base.StorageMediaManager.initialize();
		XP3Archive.initialize();
		TimerNI.initialize();
		LayerNI.initialize();
		Rect.initialize();
		RegionRect.initialize();
		TempBitmapHolder.initialize();
		XP3ArchiveStream.initialize();
		VideoOverlay.initialize();

		CreateStreamCS = new Object();
		StorageMediaManager = new StorageMediaManager();
		ArchiveCache = new ArchiveCache();
		CompactEvent = new CompactEvent();
		AutoPath = new AutoPath();
		EventManager = new EventManager();
		WindowList = new WindowList();
		MainForm = new MainForm();
		ScnearioCache = new ScnearioCache();
		MouseCursor = new MouseCursor();
		mKeyStateHelper = new KeyStateHelper();
		ConsoleOutputGetway = new TJS2ConsoleOutputGateway();
		DumpOutputGetway = new TJS2DumpOutputGateway();
		LogStreamHolder = new LogStreamHolder();
		DebugLog = new DebugLog();
		SoundMixer = new SoundMixer();
		SimpleImageProvider = new SimpleImageProviderObject();

		IsLowMemory = false;
		IsTarminating = false;
		ProjectDirSelected = false;
		mSystemEventDisabledState = false;
		GraphicCacheSystemLimit = 0;
		GraphicCacheLimit = 0;
		GraphicCacheTotalBytes = 0;
		GraphicCacheEnabled = false;
		GraphicSplitOperationType = 0;
		DefaultHoldAlpha = false;
		TerminateOnWindowClose = true;
		TerminateOnNoWindowStartup = true;
		NativeDataPath = null;
		DataPath = null;
		AppTitle = APP_TITLE;
		AppTitleInit = false;
		DumpOutputStream = null;
		mTransHandlerProviderInit = false;
		mTransHandlerProviders = new HashMap<String,TransHandlerProvider>();

		SoundMixer.initialize(); // サウンド初期化
	}
	public static void initializeScript() {
		KAGParserNI.initialize();
		try {
			mFontClass = new FontClass();
			mMenuItemClass = new MenuItemClass();
			mControllerClass = new ControllerClass();
			mConsoleClass = new ConsoleClass();
			mPassThroughDrawDeviceClass = new PassThroughDrawDeviceClass();
		} catch (VariantException e) {
		} catch (TJSException e) {
		}
	}
	public static void finalizeApplication() {

		ApplicationSystem.finalizeApplication();
		Storage.finalizeApplication();
		jp.kirikiri.tvp2.base.StorageMediaManager.finalizeApplication();
		XP3Archive.finalizeApplication();
		WaveSoundBufferNI.finalizeApplication();
		Random.finalizeApplication();
		BaseBitmap.finalizeApplication();
		GraphicsLoader.finalizeApplication();
		Rect.finalizeApplication();
		RegionRect.finalizeApplication();
		TempBitmapHolder.finalizeApplication();
		XP3ArchiveStream.finalizeApplication();
		VideoOverlay.finalizeApplication();

		StorageMediaManager = null;
		ArchiveCache = null;
		CreateStreamCS = null;
		CompactEvent = null;
		AutoPath = null;
		MainWindow = null;
		EventManager = null;
		WindowList = null;
		MainForm = null;
		TimerThread = null;
		//Properties = null;
		ScnearioCache = null;
		MouseCursor = null;
		ConsoleOutputGetway = null;
		DumpOutputGetway = null;
		LogStreamHolder = null;
		DebugLog = null;
		SoundMixer = null;
		Application = null;
		SimpleImageProvider = null;

		ProjectDir = null;
		NativeProjectDir = null;
		NativeDataPath = null;
		DataPath = null;
		AppTitle = null;
		DumpOutputStream = null;

		mFontClass = null;
		mMenuItemClass = null;
		mKeyStateHelper = null;
		mControllerClass = null;
		mConsoleClass = null;
		mPassThroughDrawDeviceClass = null;
		mTransHandlerProviders = null;

		NotImplProp = null;
		NotImplMethod = null;
		ReturnOKMethod = null;
		ReturnOKConstructor = null;
	}
	public static Archive openArchive( final String name ) throws TJSException {
		/*
		tTVPArchive * archive = TVPOpenSusieArchive(name); // in SusieArchive.h
		if(!archive) return new tTVPXP3Archive(name); else return archive;
		*/
		// Susie プラグインは非サポート
		// java 版は、jar ファイルアーカイブをサポートしてもいいかもね
		return new XP3Archive(name);
	}

	public static void setSystemEventDisabledState(boolean en) {
		mSystemEventDisabledState = en;
		if(!en) EventManager.deliverAllEvents();
	}
	//---------------------------------------------------------------------------
	public static boolean getSystemEventDisabledState() {
		return mSystemEventDisabledState;
	}
	public static void mainWindowClose() {
		if( MainWindow !=null && !MainWindow.getVisible() && TerminateOnWindowClose) {
			ApplicationSystem app = Application.get();
			if( app != null ) app.terminateAsync(0);
		}
		//System.exit(0);
	}
	public static Dispatch2 createFontObject( Dispatch2 layer ) throws VariantException, TJSException {
		Variant param = new Variant(layer);
		Variant[] pparam = new Variant[1];
		pparam[0] = param;
		Holder<Dispatch2> holder = new Holder<Dispatch2>(null);
		int hr = mFontClass.createNew(0, null, holder, pparam, mFontClass );
		if( hr < 0 )
			Message.throwExceptionMessage(Message.InternalError);
		return holder.mValue;
	}
	public static long getTickCount() {
		return java.lang.System.currentTimeMillis() - mStartTick;
	}
	public static void setKeyState( int code ) {
		mKeyStateHelper.setKey( code );
	}
	public static void resetKeyState( int code ) {
		mKeyStateHelper.resetkey( code );
	}
	public static boolean isKeyPressing( int code ) {
		return mKeyStateHelper.isPressing( code );
	}
	public static void clearKeyStates() {
		mKeyStateHelper.clear();
	}
	public static int getGraphicCacheLimit() {
		return (int) GraphicCacheLimit;
	}
	public static void setGraphicCacheLimit(int limit) {
		// set limit of graphic cache by total bytes.
		if(limit == 0 ) {
			GraphicCacheLimit = limit;
			GraphicCacheEnabled = false;
		} else if(limit == -1) {
			GraphicCacheLimit = GraphicCacheSystemLimit;
			GraphicCacheEnabled = GraphicCacheLimit > 0;
		} else {
			if(limit > GraphicCacheSystemLimit)
				limit = (int) GraphicCacheSystemLimit;
			GraphicCacheLimit = limit;
			GraphicCacheEnabled = limit > 0;
		}
		GraphicsLoader.checkGraphicCacheLimit();
	}
	public static void setAppTitle( final String title ) {
		AppTitleInit = true;
		AppTitle = title;
		if( MainWindow != null ) {
			MainWindow.setCaption(title);
		}
	}
	public static String getAppTitle() {
		return AppTitle;
	}
	public static Dispatch2 createMenuItemObject( Dispatch2 window ) throws TJSException {
		if( mMenuItemClass == null ) return null;

		Holder<Dispatch2> out = new Holder<Dispatch2>(null);
		Variant param = new Variant(window);
		Variant[] pparam = {param, param};
		int hr = mMenuItemClass.createNew( 0, null, out, pparam, mMenuItemClass );
		if( hr < 0 )
			Message.throwExceptionMessage( Message.InternalError, "TVP.createMenuItemObject" );
		return out.mValue;
	}
	public static Dispatch2 getControllerClass() { return mControllerClass; }
	public static Dispatch2 getConsoleClass() { return mConsoleClass; }
	public static Dispatch2 getPassThroughDrawDeviceClass() { return mPassThroughDrawDeviceClass; }

	public static TransHandlerProvider findTransHandlerProvider( final String name ) throws TJSException {
		if( mTransHandlerProviderInit == false ) {
			// we assume that transition that has the same name as the other does not exist
			registerDefaultTransHandlerProvider();
			mTransHandlerProviderInit = true;
		}
		TransHandlerProvider pro = mTransHandlerProviders.get(name);
		if( pro == null )
			Message.throwExceptionMessage(Message.CannotFindTransHander, name);
		return pro;
	}
	private static void registerDefaultTransHandlerProvider() throws TJSException {
		TransHandlerProvider prov;
		String name;
		prov = new CrossFadeTransHandlerProvider();
		name = prov.getName();
		if( name == null ) Message.throwExceptionMessage( Message.TransHandlerError, "TransHandlerProvider.getName failed" );
		if( mTransHandlerProviders.get(name) != null ) Message.throwExceptionMessage( Message.TransAlreadyRegistered, name );
		mTransHandlerProviders.put(name, prov);

		prov = new UniversalTransHandlerProvider();
		name = prov.getName();
		if( name == null ) Message.throwExceptionMessage( Message.TransHandlerError, "UniversalTransHandlerProvider.getName failed" );
		if( mTransHandlerProviders.get(name) != null ) Message.throwExceptionMessage( Message.TransAlreadyRegistered, name );
		mTransHandlerProviders.put(name, prov);

		prov = new ScrollTransHandlerProvider();
		name = prov.getName();
		if( name == null ) Message.throwExceptionMessage( Message.TransHandlerError, "UniversalTransHandlerProvider.getName failed" );
		if( mTransHandlerProviders.get(name) != null ) Message.throwExceptionMessage( Message.TransAlreadyRegistered, name );
		mTransHandlerProviders.put(name, prov);

		prov = new WaveTransHandlerProvider();
		name = prov.getName();
		if( name == null ) Message.throwExceptionMessage( Message.TransHandlerError, "WaveTransHandlerProvider.getName failed" );
		if( mTransHandlerProviders.get(name) != null ) Message.throwExceptionMessage( Message.TransAlreadyRegistered, name );
		mTransHandlerProviders.put(name, prov);

		prov = new MosaicTransHandlerProvider();
		name = prov.getName();
		if( name == null ) Message.throwExceptionMessage( Message.TransHandlerError, "MosaicTransHandlerProvider.getName failed" );
		if( mTransHandlerProviders.get(name) != null ) Message.throwExceptionMessage( Message.TransAlreadyRegistered, name );
		mTransHandlerProviders.put(name, prov);

		prov = new TurnTransHandlerProvider();
		name = prov.getName();
		if( name == null ) Message.throwExceptionMessage( Message.TransHandlerError, "TurnTransHandlerProvider.getName failed" );
		if( mTransHandlerProviders.get(name) != null ) Message.throwExceptionMessage( Message.TransAlreadyRegistered, name );
		mTransHandlerProviders.put(name, prov);

		prov = new RotateZoomTransHandlerProvider();
		name = prov.getName();
		if( name == null ) Message.throwExceptionMessage( Message.TransHandlerError, "RotateZoomTransHandlerProvider.getName failed" );
		if( mTransHandlerProviders.get(name) != null ) Message.throwExceptionMessage( Message.TransAlreadyRegistered, name );
		mTransHandlerProviders.put(name, prov);

		prov = new RotateVanishTransHandlerProvider();
		name = prov.getName();
		if( name == null ) Message.throwExceptionMessage( Message.TransHandlerError, "RotateVanishTransHandlerProvider.getName failed" );
		if( mTransHandlerProviders.get(name) != null ) Message.throwExceptionMessage( Message.TransAlreadyRegistered, name );
		mTransHandlerProviders.put(name, prov);

		prov = new RotateSwapTransHandlerProvider();
		name = prov.getName();
		if( name == null ) Message.throwExceptionMessage( Message.TransHandlerError, "RotateSwapTransHandlerProvider.getName failed" );
		if( mTransHandlerProviders.get(name) != null ) Message.throwExceptionMessage( Message.TransAlreadyRegistered, name );
		mTransHandlerProviders.put(name, prov);

		prov = new RippleTransHandlerProvider();
		name = prov.getName();
		if( name == null ) Message.throwExceptionMessage( Message.TransHandlerError, "RippleTransHandlerProvider.getName failed" );
		if( mTransHandlerProviders.get(name) != null ) Message.throwExceptionMessage( Message.TransAlreadyRegistered, name );
		mTransHandlerProviders.put(name, prov);
	}
}

