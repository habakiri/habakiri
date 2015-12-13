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
package jp.kirikiri.tvp2.base;

import java.util.Enumeration;
import java.util.UUID;

import jp.kirikiri.tjs2.TJS;
import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tvp2.TVP;
import jp.kirikiri.tvp2.utils.DebugClass;
import jp.kirikiri.tvp2.utils.Random;
import jp.kirikiri.tvp2.visual.BaseBitmap;

public class SystemInitializer {

	static private boolean SystemUninitCalled;
	static private boolean ProgramArgumentsInit;
	static private boolean DataPathDirectoryEnsured;

	static public void initialize() {
		SystemUninitCalled = false;
		ProgramArgumentsInit = false;
		DataPathDirectoryEnsured = false;
	}
	static public void systemInitialize() throws TJSException {
		beforeSystemInitialize();

		//ScriptsClass.initScriptEnging();

		afterSystemInit();
	}
	static private void initializeRandomGenerator() {
		// initialize random generator
		Random.updateEnvironNoiseForTick();

		UUID uuid = UUID.randomUUID();
		long l = uuid.getLeastSignificantBits();
        Random.pushEnvironNoise(l);
        long m = uuid.getMostSignificantBits();
		Random.pushEnvironNoise(m);

		// プロセスIDの取得は飛ばす

		long id = Thread.currentThread().getId();
		Random.pushEnvironNoise(id);

		Random.updateEnvironNoiseForTick();

		// カーソル位置からの取得は飛ばす

		// ウィンドウ情報からの取得は飛ばす
	}
	static private void beforeSystemInitialize() throws TJSException {

		//RegisterDllLoadHook(); // register DLL delayed import hook to support _inmm.dll

		initProgramArgumentsAndDataPath(false); // ensure command line

		/*
		Application->HintHidePause = 24*60*60*1000; // not to hide tool tip hint immediately
		Application->ShowHint = false;
		Application->ShowHint = true; // to ensure assigning new HintWindow Class defined in HintWindow.cpp
		*/


		// randomize
		initializeRandomGenerator();

		// memory usage
		{
			Runtime runtime = Runtime.getRuntime();
			long totalMem = runtime.totalMemory();
			long maxMem = runtime.maxMemory();
			long freeMem = runtime.freeMemory();
			Random.pushEnvironNoise(totalMem);
			Random.pushEnvironNoise(maxMem);
			Random.pushEnvironNoise(freeMem);

			TVP.MaxMemory = maxMem;

			DebugClass.addImportantLog( "(info) Max memory : " + maxMem );

			/*
			Variant opt = new Variant();
			if(TVPGetCommandLine( "-memusage", opt) ) {
				String str = opt.asString();
				if( str == "low") TVP.MaxMemory = 0; // assumes zero
			}
			*/

			if( TVP.MaxMemory <= 36*1024*1024 ) {
				// very very low memory, forcing to assume zero memory
				TVP.MaxMemory = 0;
			}

			/*
			if( TVP.MaxMemory < 48*1024*1024 ) {
				// extra low memory
				if( TJSObjectHashBitsLimit > 0) TJSObjectHashBitsLimit = 0;
				TVPSegmentCacheLimit = 0;
				TVPFreeUnusedLayerCache = true; // in LayerIntf.cpp
			} else if( TVP.MaxMemory < 64*1024*1024) {
				// low memory
				if(TJSObjectHashBitsLimit > 4)
					TJSObjectHashBitsLimit = 4;
			}
			*/
		}

		TVP.Application.get().initializeDataPath();

		if( TVP.ProjectDirSelected ) {
			DebugClass.addImportantLog( "(info) Selected project directory : " + TVP.ProjectDir );
		}
	}
	private static final int
		gsotNone = 0,
		gsotSimple = 1,
		gsotInterlace = 2,
		gsotBiDirection = 3;

	static private void dumpOptions() {
		StringBuilder builder = new StringBuilder(256);
		builder.append( "(info) Specified option(s) (earlier item has more priority) :" );
		boolean appending = false;
		for( Enumeration<?> en = TVP.Properties.propertyNames(); en.hasMoreElements();) {
			String key = (String) en.nextElement();
			String value = TVP.Properties.getProperty(key);
			builder.append( " -" );
			builder.append( key );
			builder.append( '=' );
			builder.append( value );
			appending = true;
		}
		if( appending == false ) {
			builder.append( " (none)" );
		}
		DebugClass.addImportantLog(builder.toString());
		builder = null;
	}
	private static void afterSystemInit() {
		// ensure datapath directory
		// ensureDataPathDirectory();

		// determine maximum graphic cache limit

		// オプション見てタイマースレッドの優先度を変える
		int limitmb = -1;
		String prop = TVP.Properties.getProperty("gclim","auto");
		if( !"auto".equals(prop) ) {
			limitmb = Integer.valueOf(prop);
		}

		// TVP.MaxMemory は VM のヒープなので、画像が読み込まれるヒープとは違う
		// Android だと常に 0 になってしまうはず
		if(limitmb == -1) {
			if( TVP.MaxMemory <= 32*1024*1024)
				TVP.GraphicCacheSystemLimit = 0;
			else if(TVP.MaxMemory <= 48*1024*1024)
				TVP.GraphicCacheSystemLimit = 0;
			else if(TVP.MaxMemory <= 64*1024*1024)
				TVP.GraphicCacheSystemLimit = 0;
			else if(TVP.MaxMemory <= 96*1024*1024)
				TVP.GraphicCacheSystemLimit = 4;
			else if(TVP.MaxMemory <= 128*1024*1024)
				TVP.GraphicCacheSystemLimit = 8;
			else if(TVP.MaxMemory <= 192*1024*1024)
				TVP.GraphicCacheSystemLimit = 12;
			else if(TVP.MaxMemory <= 256*1024*1024)
				TVP.GraphicCacheSystemLimit = 20;
			else if(TVP.MaxMemory <= 512*1024*1024)
				TVP.GraphicCacheSystemLimit = 40;
			else
				TVP.GraphicCacheSystemLimit = (TVP.MaxMemory / (1024*1024*10));	// cachemem = physmem / 10
			TVP.GraphicCacheSystemLimit *= 1024*1024;
		} else {
			TVP.GraphicCacheSystemLimit = limitmb * 1024*1024;
		}
		if( TVP.MaxMemory <= (64*1024*1024) ) {
			TVP.IsLowMemory = true;
		}

		/*
		if( TVP.MaxMemory <= 64*1024*1024)
			TVP.setFontCacheForLowMem();
		*/

		// check TVPGraphicSplitOperation option
		prop = TVP.Properties.getProperty("gsplit","yes");
		if( "no".equals(prop) )
			TVP.GraphicSplitOperationType = gsotNone;
		else if( "int".equals(prop) )
			TVP.GraphicSplitOperationType = gsotInterlace;
		else if( "yes".equals(prop) || "simple".equals(prop) )
			TVP.GraphicSplitOperationType = gsotSimple;
		else if( "bidi".equals(prop) )
			TVP.GraphicSplitOperationType = gsotBiDirection;


		// check TVPDefaultHoldAlpha option
		prop = TVP.Properties.getProperty("holdalpha","false");
		if( "yes".equals(prop) || "true".equals(prop) )
			TVP.DefaultHoldAlpha = true;
		else
			TVP.DefaultHoldAlpha = false;

		// JPEG デコーダー精度は無視

		// dump option
		dumpOptions();

		// timer 精度は無視

		// draw thread 数は無視(常に自動)
	}

	static void initProgramArgumentsAndDataPath( boolean stop_after_datapath_got ) throws TJSException {
		if(!ProgramArgumentsInit) {
			ProgramArgumentsInit = true;

			TVP.Application.get().initializeSaveDataPath( stop_after_datapath_got );
		}
	}

	/**
	 * システム終了処理
	 */
	public static void systemUninitialize() {
		if(SystemUninitCalled) return;
		SystemUninitCalled = true;

		try {
			ScriptsClass.uninitScriptEngine();
		} catch( Exception e )  {
			// ignore errors
		}
		BaseBitmap.unmapPrerenderedAllFont();
		TVP.finalizeApplication();
		TJS.finalizeApplication();
		System.gc();
	}

	public static void ensureDataPathDirectory() {
		if(!DataPathDirectoryEnsured) {
			DataPathDirectoryEnsured = true;
			// ensure data path existence
			if( Storage.checkExistentLocalFolder(TVP.NativeDataPath) == false ) {
				if( Storage.createFolders( TVP.NativeDataPath ) )
					DebugClass.addImportantLog("(info) Data path does not exist, trying to make it ... ok.");
				else
					DebugClass.addImportantLog("(info) Data path does not exist, trying to make it ... failed.");
			}
		}
	}
}
