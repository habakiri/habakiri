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

import java.util.Arrays;

import jp.kirikiri.tjs2.ByteCodeLoader;
import jp.kirikiri.tjs2.Error;
import jp.kirikiri.tjs2.Interface;
import jp.kirikiri.tjs2.BinaryStream;
import jp.kirikiri.tjs2.CompileException;
import jp.kirikiri.tjs2.ConsoleOutput;
import jp.kirikiri.tjs2.CustomObject;
import jp.kirikiri.tjs2.Dispatch2;
import jp.kirikiri.tjs2.Holder;
import jp.kirikiri.tjs2.NativeClass;
import jp.kirikiri.tjs2.NativeClassConstructor;
import jp.kirikiri.tjs2.NativeClassMethod;
import jp.kirikiri.tjs2.NativeInstance;
import jp.kirikiri.tjs2.SourceCodeAccessor;
import jp.kirikiri.tjs2.TJS;
import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tjs2.TJSScriptError;
import jp.kirikiri.tjs2.TJSScriptException;
import jp.kirikiri.tjs2.Variant;
import jp.kirikiri.tjs2.VariantException;
import jp.kirikiri.tjs2.VariantClosure;
import jp.kirikiri.tvp2.TVP;
import jp.kirikiri.tvp2.msg.Message;
import jp.kirikiri.tvp2.sound.CDDASoundBufferClass;
import jp.kirikiri.tvp2.sound.MIDISoundBufferClass;
import jp.kirikiri.tvp2.sound.PhaseVocoderClass;
import jp.kirikiri.tvp2.sound.WaveSoundBufferClass;
import jp.kirikiri.tvp2.utils.ClipboardClass;
import jp.kirikiri.tvp2.utils.DebugClass;
import jp.kirikiri.tvp2.utils.KAGParserClass;
import jp.kirikiri.tvp2.utils.PadClass;
import jp.kirikiri.tvp2.utils.TimerClass;
import jp.kirikiri.tvp2.visual.LayerClass;
import jp.kirikiri.tvp2.visual.LayerType;
import jp.kirikiri.tvp2.visual.MenuItemClass;
import jp.kirikiri.tvp2.visual.PassThroughDrawDeviceClass;
import jp.kirikiri.tvp2.visual.VideoOverlayClass;
import jp.kirikiri.tvp2.visual.WindowClass;
import jp.kirikiri.tvp2env.PadForm;
import jp.kirikiri.tvp2env.VirtualKey;

public class ScriptsClass extends NativeClass {
	static private final boolean IS_USE_BYTE_CODE = true;
	static private final String CLASS_NAME = "Scripts";

	static int mClassID = -1;
	static private TJS mScriptEngine;
	static private boolean mScriptEngineUninit;
	static private boolean mTJBByteCodeLoading;;
	static private byte[] mHeaderTemp;

	public static void initScriptEnging() throws VariantException, TJSException {
		mScriptEngineUninit = false;

		String prop = TVP.Properties.getProperty("tjb_bytecode_loading","true");
		if( "yes".equals(prop) || "true".equals(prop) ) {
			mTJBByteCodeLoading = true;
		} else {
			mTJBByteCodeLoading = false;
		}
		mHeaderTemp = new byte[8];

		TJS.mStorage = new StorageIO();
		TJS.initialize();
		TVP.initializeScript();
		mScriptEngine = new TJS();

		// script system initialization
		initializeScriptSource();
		//mScriptEngine.execScript( InitTJSScript, null, null, null, 0 );
		//mScriptEngine.execScript( InitTJSScript2, null, null, null, 0 );

		// set console output gateway handler
		TJS.setConsoleOutput( TVP.ConsoleOutputGetway );

		Variant val;
		Dispatch2 dsp;
		Dispatch2 global = mScriptEngine.getGlobal();

		/* classes */
		dsp = new DebugClass();
		val = new Variant(dsp/*, dsp*/);
		global.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "Debug", val, global );

		dsp = new LayerClass();
		val = new Variant(dsp/*, dsp*/);
		global.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "Layer", val, global );

		dsp = new CDDASoundBufferClass();
		val = new Variant(dsp/*, dsp*/);
		global.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "CDDASoundBuffer", val, global );

		dsp = new MIDISoundBufferClass();
		val = new Variant(dsp/*, dsp*/);
		global.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "MIDISoundBuffer", val, global );

		dsp = new TimerClass();
		val = new Variant(dsp/*, dsp*/);
		global.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "Timer", val, global );

		dsp = new AsyncTriggerClass();
		val = new Variant(dsp/*, dsp*/);
		global.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "AsyncTrigger", val, global );

		dsp = new SystemClass();
		val = new Variant(dsp/*, dsp*/);
		global.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "System", val, global );

		dsp = new StorageClass();
		val = new Variant(dsp/*, dsp*/);
		global.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "Storages", val, global );

		dsp = new PluginsClass();
		val = new Variant(dsp/*, dsp*/);
		global.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "Plugins", val, global );

		dsp = new MenuItemClass();
		val = new Variant(dsp/*, dsp*/);
		global.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "MenuItem", val, global );

		dsp = new VideoOverlayClass();
		val = new Variant(dsp/*, dsp*/);
		global.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "VideoOverlay", val, global );

		dsp = new PadClass();
		val = new Variant(dsp/*, dsp*/);
		global.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "Pad", val, global );

		dsp = new ClipboardClass();
		val = new Variant(dsp/*, dsp*/);
		global.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "Clipboard", val, global );

		dsp = new ScriptsClass();
		val = new Variant(dsp/*, dsp*/);
		global.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "Scripts", val, global );

		/* KAG special support */
		dsp = new KAGParserClass();
		val = new Variant(dsp/*, dsp*/);
		global.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "KAGParser", val, global );

		// WaveSoundBuffer
		Dispatch2 waveclass = new WaveSoundBufferClass();
		val = new Variant(waveclass/*, waveclass*/);
		global.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "WaveSoundBuffer", val, global );
		dsp = new PhaseVocoderClass();
		val = new Variant(dsp);
		waveclass.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP|Interface.STATICMEMBER, "PhaseVocoder", val, waveclass);

		/* Window and its drawdevices */
		Dispatch2 windowclass = null;
		windowclass = new WindowClass();
		val = new Variant(windowclass/*, dsp*/);
		global.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "Window", val, global );

		//dsp = new PassThroughDrawDeviceClass();
		dsp = TVP.getPassThroughDrawDeviceClass();
		val = new Variant(dsp);
		windowclass.propSet(Interface.MEMBERENSURE|Interface.IGNOREPROP|Interface.STATICMEMBER, "PassThroughDrawDevice", val, windowclass);


		//CustomObject.doRehash();
	}
	static public void uninitScriptEngine() {
		if(mScriptEngineUninit) return;
		mScriptEngineUninit = true;

		mScriptEngine.shutdown();
		mScriptEngine = null;

		mTJBByteCodeLoading = false;
		mHeaderTemp = null;
	}
	public static Dispatch2 getGlobal() {
		return mScriptEngine.getGlobal();
	}
	public static TJS getEngine() {
		return mScriptEngine;
	}
	private static void executeStartupScript() {
		// TODO 他は後で実装する
		// execute "startup.tjs"
		try {
			DebugClass.addLog("(info) Loading startup script : startup.tjs");
			executeStorage("startup.tjs",null,null,false,null);
			DebugClass.addLog("(info) Startup script ended.");
			// 他の例外はここで受けて変換する
		} catch( TJSScriptException e ) {
			//beforeProcessUnhandledException();
			e.addTrace( "startup" );
			if( !processUnhandledException(e) )
				showScriptException(e);
		} catch( TJSScriptError e) {
			//beforeProcessUnhandledException();
			e.addTrace( "startup" );
			if( !processUnhandledException(e) )
				showScriptException(e);
		} catch (TJSException e) {
			//beforeProcessUnhandledException();
			if( !processUnhandledException(e) ) {
				showScriptException(e);
			}
		}
	}
	static public void initializeStartupScript() {
		//TVPStartObjectHashMap();

		executeStartupScript();
		// 読み込み用バッファの開放を毎回行うようにする
		// startupスクリプト実行時は、読み込み用バッファの開放を出来るだけ行わず、GC を抑止する
		ByteCodeLoader.allwaysFreeReadBuffer();
		if(TVP.TerminateOnNoWindowStartup && TVP.WindowList.getWindowCount() == 0 /*&& (TVP.MainWindow!=null && !TVP.MainWindow.getVisible() )*/ ) {
			// no window is created and main window is invisible
			TVP.Application.get().terminateAsync(0);
		}
	}
	static private final boolean getSystem_exceptionHandler_Object( VariantClosure dest ) throws VariantException, TJSException {
		// get System.exceptionHandler
		Dispatch2 global = mScriptEngine.getGlobal();
		if( global == null ) return false;

		Variant val = new Variant();
		int er = global.propGet( Interface.MEMBERMUSTEXIST, "System", val, global);
		if( er < 0 ) return false;
		if( val.isObject() != true ) return false;

		VariantClosure clo = val.asObjectClosure();
		if( clo.mObject == null ) return false;

		Variant val2 = new Variant();
		clo.propGet( Interface.MEMBERMUSTEXIST, "exceptionHandler", val2, null );
		if( val2.isObject() != true ) return false;

		clo = val2.asObjectClosure();
		dest.set( clo.mObject, clo.mObjThis );
		if( dest.mObject == null ) {
			dest.set( null, null );
			return false;
		}

		return true;
	}
	static final boolean processUnhandledException( TJSException e ) {
		boolean result = false;
		VariantClosure clo = new VariantClosure(null,null);
		try {
			// get the script engine
			TJS engine = mScriptEngine;
			if( engine == null )
				return false; // the script engine had been shutdown

			// get System.exceptionHandler
			if(!getSystem_exceptionHandler_Object(clo))
				return false; // System.exceptionHandler cannot be retrieved

			// execute clo
			Variant obj = new Variant();
			Variant msg = new Variant( e.getMessage() );
			Error.getExceptionObject( engine, obj, msg, null );

			Variant[] pval = new Variant[1];
			pval[0] = obj;

			Variant res = new Variant();
			clo.funcCall(0, null, res, pval, null);

			result = res.asBoolean();
		} catch( TJSScriptError es ) {
			showScriptException(es);
		} catch( TJSException et ) {
			showScriptException(et);
		}
		return result;
	}
	/**
	 * These functions display the error location, reason, etc.
	 * And disable the script event dispatching to avoid massive occurrence of
	 * errors.
	*/
	static public void showScriptException( TJSException e ) {
		//TVPSetSystemEventDisabledState(true);
		//TVPOnError();

		//if(!TVPSystemUninitCalled)
		{
			//if(TVPMainForm) TVPMainForm->Visible = true;
			String errstr = Message.ScriptExceptionRaised + "\n" + e.getMessage();
			DebugClass.addLog( Message.ScriptExceptionRaised + "\n" + e.getMessage() );
			TVP.Application.get().messageBox( errstr, "Error", 0 );
		}
	}
	//---------------------------------------------------------------------------
	static public void showScriptException( TJSScriptError e ) {
		//TVPSetSystemEventDisabledState(true);
		//TVPOnError();

		//if(!TVPSystemUninitCalled)
		{

			//if(TVPMainForm) TVPMainForm->Visible = true;
			/* TODO 例外発生箇所を表示する */

			SourceCodeAccessor block = e.getAccessor();
			if( block != null ) {
				PadForm pad = new PadForm();
				pad.setFreeOnTerminate( true );
				pad.setExecButtonEnabled( false );
				pad.setLines( block.getScript() );
				pad.setReadOnly( true );
				pad.setStatusText( e.getMessage() );
				pad.setCaption( Message.ExceptionCDPName );
				pad.setVisible( true );
				pad.goToLine( block.srcPosToLine(e.getPosition() ) - block.getLineOffset() );
			}

			String errstr = Message.ScriptExceptionRaised + "\n" + e.getMessage();
			DebugClass.addLog( Message.ScriptExceptionRaised + "\n" + e.getMessage() );
			if( e.getTrace().length() != 0) {
				DebugClass.addLog( "trace : " + e.getTrace() );
			}
			TVP.Application.get().messageBox( errstr, "Error", 0 );
		}
	}
	public static void executeExpression( final String content, Dispatch2 context, Variant result ) throws VariantException, TJSException, CompileException {
		if( mScriptEngine == null ) throw new TJSException( Error.InternalError );

		ConsoleOutput output = TJS.getConsoleOutput();
		TJS.setConsoleOutput(null); // once set TJS console to null
		try {
			mScriptEngine.evalExpression(content, result, context, null, 0 );
		} finally {
			TJS.setConsoleOutput(output);
		}
	}

	protected NativeInstance createNativeInstance() throws TJSException {
		// this class cannot create an instance
		Message.throwExceptionMessage(Message.CannotCreateInstance);
		return null;
	}
	public static void executeStorage( final String name, Dispatch2 context, Variant result, boolean isexpression, final String modestr ) throws TJSException, VariantException, CompileException {
		if( mScriptEngine == null ) throw new TJSException( Error.InternalError );

		if( IS_USE_BYTE_CODE ) {
			if( isexpression == false ) {
				if( mTJBByteCodeLoading && name.endsWith(".tjs") ) {
					// tjs ファイルの時、同名の tjb ファイルがあるか探す
					String tjbname = name.substring(0,name.length()-4) + ".tjb";
					String normalname = TVP.StorageMediaManager.normalizeStorageName(tjbname, null);
					if( Storage.isExistentStorage(normalname) ) {
						String binshortname = Storage.extractStorageName(name);
						String binplace = Storage.searchPlacedPath(tjbname);
						if( binplace != null && binplace.length() > 0 ) {
							BinaryStream stream = Storage.createStream( binplace, BinaryStream.READ );
							mScriptEngine.loadByteCode(result, context, binshortname, stream );
							return;
						}
					}
				}
				// ヘッダーチェック
				String place = Storage.searchPlacedPath(name);
				String shortname = Storage.extractStorageName(place);
				BinaryStream stream = Storage.createStream( place, BinaryStream.READ );
				int len = stream.read(mHeaderTemp);
				if( len == 8 && Arrays.equals(mHeaderTemp, jp.kirikiri.tjs2.Compiler.FILE_TAG) ) {
					stream.setPosition(0);
					mScriptEngine.loadByteCode(result, context, shortname, stream );
					return;
				} else {
					// ヘッダーがバイナリのバイトコードでなくて読み替えもしないのならスクリプトとして読む
					stream.setPosition(0);
					String buffer = Storage.readText(stream,place,modestr);
					if( buffer == null ) buffer = "";
					mScriptEngine.execScript(buffer, result, context, shortname, 0 );
					return;
				}
			}
		}

		String place = Storage.searchPlacedPath(name);
		String shortname = Storage.extractStorageName(place);
		String buffer = Storage.readText(place,modestr);
		if( buffer == null ) buffer = "";

		if( isexpression == false ) {
			mScriptEngine.execScript(buffer, result, context, shortname, 0 );
		} else {
			mScriptEngine.evalExpression( buffer, result, context, shortname, 0 );
		}
	}
	private static void compileStorage( final String name, boolean isrequestresult, final String output ) throws TJSException, VariantException, CompileException {
		if( mScriptEngine == null ) throw new TJSException( Error.InternalError );

		String place = Storage.searchPlacedPath(name);
		String buffer = Storage.readText(place,null);

		String filepath;
		filepath = TVP.DataPath + "/" + Storage.extractStorageName(output);
		BinaryStream stream = Storage.createStream( filepath, BinaryStream.WRITE );

		mScriptEngine.compileScript( buffer, name, 0, isrequestresult, stream );
	}
	private static void toJavaCode( final String name, boolean isrequestresult ) throws TJSException, VariantException, CompileException {
		String place = Storage.searchPlacedPath(name);
		String buffer = Storage.readText(place,null);
		mScriptEngine.toJavaCode( buffer, name, 0, isrequestresult );
	}
	private static void dumpScriptEngine() throws TJSException {
		if( mScriptEngine != null ) {
			try {
				TVP.DebugLog.startDump();
				TJS.setConsoleOutput( TVP.DumpOutputGetway );
				mScriptEngine.dump();
			} finally {
				TJS.setConsoleOutput( TVP.ConsoleOutputGetway );
				TVP.DebugLog.endDump();
			}
		}
	}

	public ScriptsClass() throws VariantException, TJSException {
		super(CLASS_NAME);
		final int NCM_CLASSID = TJS.registerNativeClass(CLASS_NAME);
		setClassID( NCM_CLASSID );
		mClassID = NCM_CLASSID;

		// constructor
		registerNCM( CLASS_NAME, TVP.ReturnOKConstructor, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "finalize", TVP.ReturnOKMethod, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "compileStorage", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException, TJSException {
				if( param.length < 1 ) return Error.E_BADPARAMCOUNT;
				String name = param[0].asString();
				String output;
				if( param.length >= 2 && param[1].isVoid() != true ) {
					output = param[1].asString();
				} else {
					output = name.substring(0,name.length()-4) + ".tjb";
				}
				boolean isresult = false;
				if( param.length >= 3 && param[2].isVoid() != true ) {
					isresult = param[2].asBoolean();
				}
				compileStorage( name, isresult, output );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "toJavaCode", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException, TJSException {
				if( param.length < 1 ) return Error.E_BADPARAMCOUNT;
				String name = param[0].asString();
				boolean isresult = false;
				if( param.length >= 2 && param[1].isVoid() != true ) {
					isresult = param[1].asBoolean();
				}
				toJavaCode( name, isresult );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "execStorage", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException, TJSException {
				if( param.length < 1 ) return Error.E_BADPARAMCOUNT;
				String name = param[0].asString();
				String modestr = null;
				if( param.length >=2 && param[1].isVoid() != true ) {
					modestr = param[1].asString();
				}
				Dispatch2 context = param.length >= 3 && param[2].isVoid() != true ? param[2].asObject() : null;
				executeStorage( name, context, result, false, modestr );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "evalStorage", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException, TJSException {
				if( param.length < 1 ) return Error.E_BADPARAMCOUNT;
				String name = param[0].asString();
				String modestr = null;
				if( param.length >=2 && param[1].isVoid() != true ) {
					modestr = param[1].asString();
				}
				Dispatch2 context = param.length >= 3 && param[2].isVoid() != true ? param[2].asObject() : null;
				executeStorage( name, context, result, true, modestr );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "exec", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				// execute given string as a script
				if( param.length < 1) return Error.E_BADPARAMCOUNT;

				String content = param[0].asString();

				String name = null;
				int lineofs = 0;
				if( param.length >= 2 && param[1].isVoid() != true ) name = param[1].asString();
				if( param.length >= 3 && param[2].isVoid() != true ) lineofs = param[2].asInteger();

				Dispatch2 context = param.length >= 4 && param[3].isVoid() != true ? param[3].asObject() : null;

				if(mScriptEngine!=null)
					mScriptEngine.execScript(content, result, context, name, lineofs);
				else
					Message.throwExceptionMessage(Message.InternalError);

				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "eval", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				// execute given string as a script
				if( param.length < 1) return Error.E_BADPARAMCOUNT;

				String content = param[0].asString();

				String name = null;
				int lineofs = 0;
				if( param.length >= 2 && param[1].isVoid() != true ) name = param[1].asString();
				if( param.length >= 3 && param[2].isVoid() != true ) lineofs = param[2].asInteger();

				Dispatch2 context = param.length >= 4 && param[3].isVoid() != true ? param[3].asObject() : null;

				if(mScriptEngine!=null)
					mScriptEngine.evalExpression(content, result, context, name, lineofs);
				else
					Message.throwExceptionMessage(Message.InternalError);

				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "dump", new NativeClassMethod() {
				@Override
				protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				// execute given string as a script
				dumpScriptEngine();
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "getTraceString", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				// get current stack trace as string
				int limit = 0;
				if( param.length >= 1 && param[0].isVoid() != true )
					limit = param[0].asInteger();

				if(result!=null) {
					//result.set( TJS.getStackTraceString(limit) );
					result.set( "" );
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "dumpStringHeap", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) {
				// dump all strings held by TJS2 framework
				//TJS.dumpStringHeap();
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "setCallMissing", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				// set to call "missing" method
				if( param.length < 1) return Error.E_BADPARAMCOUNT;
				Dispatch2 dsp = param[0].asObject();
				if( dsp!=null ) {
					Variant missing = new Variant("missing");
					dsp.classInstanceInfo( Interface.CII_SET_MISSING, 0, missing);
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "getClassNames", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException, TJSException {
				// get class name as an array, last (most end) class first.
				if( param.length < 1) return Error.E_BADPARAMCOUNT;
				Dispatch2 dsp = param[0].asObject();
				if(dsp!=null) {
					Dispatch2 array =  TJS.createArrayObject();
					try {
						int num = 0;
						Variant val = new Variant();
						while(true) {
							int err = dsp.classInstanceInfo( Interface.CII_GET, num, val );
							if( err < 0 ) break;
							array.propSetByNum( Interface.MEMBERENSURE, num, val, array );
							num ++;
						}
						if(result!=null) result.set( array, array );
					} finally {
						array = null;
					}
				} else {
					return Error.E_FAIL;
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

	}

	/*
	// BorderStyle
	private static final int[] const_BorderStyle_val	= { 0,		  1,		  2,			3,			4,				5 };
	private static final String[] const_BorderStyle_name= { "bsNone", "bsSingle", "bsSizeable", "bsDialog", "bsToolWindow", "bsSizeToolWin"};

	// UpdateType
	private static final int[] const_UpdateType_val		= { 0,			1 };
	private static final String[] const_UpdateType_name = { "utNormal", "utEntire"};

	// MouseButtont
	private static final int[] const_MouseButton_val	= { 0,		  1,		 2 };
	private static final String[] const_MouseButton_name= { "mbLeft", "mbRight", "mbMiddle"};

	// MouseCursorState
	private static final int[] const_MouseCursorState_val		= { 0,			  1,			   2 };
	private static final String[] const_MouseCursorState_name	= { "mcsVisible", "mcsTempHidden", "mcsHidden"};

	// ImeMode
	private static final int[] const_ImeMode_val	= { 0,			 1,			2,		  3,			4,			5,		   6,		 7,			8,		  9,		   10,			 11 };
	private static final String[] const_ImeMode_name= { "imDisable", "imClose", "imOpen", "imDontCare", "imSAlpha", "imAlpha", "imHira", "imSKata", "imKata", "imChinese", "imSHanguel", "imHanguel" };

	// Setofshiftstate
	private static final int[] const_Setofshiftstate_val	= { (1<<0),		(1<<1),	(1<<2),	 (1<<3),	(1<<4),		(1<<5),		(1<<6),		(1<<7) };
	private static final String[] const_Setofshiftstate_name= { "ssShift", "ssAlt", "ssCtrl", "ssLeft", "ssRight", "ssMiddle", "ssDouble", "ssRepeat" };

	// TVP_FSF_????
	private static final int[] const_FSF_val	= { 1,				 2,				   4,				8,					0x100 };
	private static final String[] const_FSF_name= { "fsfFixedPitch", "fsfSameCharSet", "fsfNoVertical", "fsfTrueTypeOnly", "fsfUseFontFace" };

	// LayerType
	private static final int[] const_LayerType_val		= { LayerType.ltBinder, LayerType.ltCoverRect, LayerType.ltOpaque, LayerType.ltTransparent, LayerType.ltAlpha, LayerType.ltAdditive, LayerType.ltSubtractive, LayerType.ltMultiplicative, LayerType.ltEffect, LayerType.ltFilter, LayerType.ltDodge, LayerType.ltDarken, LayerType.ltLighten, LayerType.ltScreen, LayerType.ltAddAlpha, LayerType.ltPsNormal, LayerType.ltPsAdditive, LayerType.ltPsSubtractive, LayerType.ltPsMultiplicative, LayerType.ltPsScreen, LayerType.ltPsOverlay, LayerType.ltPsHardLight, LayerType.ltPsSoftLight, LayerType.ltPsColorDodge, LayerType.ltPsColorDodge5, LayerType.ltPsColorBurn, LayerType.ltPsLighten, LayerType.ltPsDarken, LayerType.ltPsDifference, LayerType.ltPsDifference5, LayerType.ltPsExclusion };
	private static final String[] const_LayerType_name	= { "ltBinder",			"ltCoverRect",			"ltOpaque",			"ltTransparent",		"ltAlpha",			"ltAdditive",			"ltSubtractive",		"ltMultiplicative",			"ltEffect",			"ltFilter",			"ltDodge",		"ltDarken",				"ltLighten",		"ltScreen",			"ltAddAlpha",		"ltPsNormal",			"ltPsAdditive",			"ltPsSubtractive",			"ltPsMultiplicative",		"ltPsScreen",			"ltPsOverlay",		"ltPsHardLight",			"ltPsSoftLight",		"ltPsColorDodge",		"ltPsColorDodge5",			"ltPsColorBurn",		"ltPsLighten",			"ltPsDarken",			"ltPsDifference",		"ltPsDifference5",			"ltPsExclusion" };

	// BlendOperationMode
	private static final int[] const_BlendOperationMode_val		= { LayerType.ltPsNormal, LayerType.ltPsAdditive, LayerType.ltPsSubtractive, LayerType.ltPsMultiplicative, LayerType.ltPsScreen, LayerType.ltPsOverlay, LayerType.ltPsHardLight, LayerType.ltPsSoftLight, LayerType.ltPsColorDodge, LayerType.ltPsColorDodge5, LayerType.ltPsColorBurn, LayerType.ltPsLighten, LayerType.ltPsDarken, LayerType.ltPsDifference, LayerType.ltPsDifference5, LayerType.ltPsExclusion, LayerType.ltAdditive, LayerType.ltSubtractive, LayerType.ltMultiplicative, LayerType.ltDodge, LayerType.ltDarken, LayerType.ltLighten, LayerType.ltScreen, LayerType.ltAddAlpha, LayerType.ltOpaque, LayerType.ltAlpha, 128 };
	private static final String[] const_BlendOperationMode_name = { "omPsNormal",			"omPsAdditive",			"omPsSubtractive",			"omPsMultiplicative",		"omPsScreen",			"omPsOverlay",		"omPsHardLight",			"omPsSoftLight",		"omPsColorDodge",		"omPsColorDodge5",			"omPsColorBurn",		"omPsLighten",			"omPsDarken",		"omPsDifference",			"omPsDifference5",			"omPsExclusion",		"omAdditive", 			"omSubtractive", 		"omMultiplicative", 		"omDodge", 			"omDarken", 		"omLighten", 		"omScreen", 		"omAddAlpha", 		"omOpaque", 		"omAlpha", 			"omAuto" };

	// DrawFace
	private static final int[] const_DrawFace_val		= { 0,			0,		4,				1,		1,			2,			3,			128 };
	private static final String[] const_DrawFace_name	= { "dfBoth", "dfAlpha","dfAddAlpha", "dfMain", "dfOpaque", "dfMask", "dfProvince", "dfAuto" };

	// HitType
	private static final int[] const_HitType_val	= { 0,			1 };
	private static final String[] const_HitType_name= { "htMask", "htProvince"};

	// ScrollTransFrom
	private static final int[] const_ScrollTransFrom_val		= { 0,			1,		2,			 3 };
	private static final String[] const_ScrollTransFrom_name	= { "sttLeft", "sttTop", "sttRight", "sttBottom" };

	// ScrollTransStay
	private static final int[] const_ScrollTransStay_val		= { 0,			  1,			   2 };
	private static final String[] const_ScrollTransStay_name	= { "ststNoStay", "ststStayDest", "ststStaySrc"};

	// KAGDebugLevel
	private static final int[] const_KAGDebugLevel_val		= { 0,			  1,			   2 };
	private static final String[] const_KAGDebugLevel_name	= { "tkdlNone", "tkdlSimple", "tkdlVerbose"};

	// AsyncTriggerMode
	private static final int[] const_AsyncTriggerMode_val		= { 0,			  1,			   2 };
	private static final String[] const_AsyncTriggerMode_name	= { "atmNormal", "atmExclusive", "atmAtIdle"};

	// BBStretchType
	private static final int[] const_BBStretchType_val		= { 0,			1,				2,			 3,			0x10 };
	private static final String[] const_BBStretchType_name	= { "stNearest", "stFastLinear", "stLinear", "stCubic", "stRefNoClip" };

	// ClipboardFormat
	private static final int[] const_ClipboardFormat_val	= { 0 };
	private static final String[] const_ClipboardFormat_name= { "cbfText" };

	// COMPACT_LEVEL_????
	private static final int[] const_COMPACT_LEVEL_val		= { 5,			10,			 15,			100 };
	private static final String[] const_COMPACT_LEVEL_name	= { "clIdle", "clDeactivate", "clMinimize", "clAll" };

	// VideoOverlayModeAdd
	private static final int[] const_VideoOverlayModeAdd_val		= { 0,			  1,			2 };
	private static final String[] const_VideoOverlayModeAdd_name	= { "vomOverlay", "vomLayer", "vomMixer"};

	// PeriodEventReason
	private static final int[] const_PeriodEventReason_val		= { 0,			1,			2,			 3 };
	private static final String[] const_PeriodEventReason_name	= { "perLoop", "perPeriod", "perPrepare", "perSegLoop" };

	// SoundGlobalFocusMode
	private static final int[] const_SoundGlobalFocusMode_val		= { 0,			 		1,					2 };
	private static final String[] const_SoundGlobalFocusMode_name	= { "sgfmNeverMute", "sgfmMuteOnMinimize", "sgfmMuteOnDeactivate"};

	// fileattributes
	private static final int[] const_fileattributes_val		= { 0x01,		  0x02,			0x04,		0x08,		0x10,			0x20,			0x3f };
	private static final String[] const_fileattributes_name = { "faReadOnly", "faHidden", "faSysFile", "faVolumeID", "faDirectory", "faArchive", "faAnyFile"};

	// mousecursorconstants
	private static final int[] const_mousecursorconstants_val		= { 0x0,		-1,		-2,			-3,		-4,			-5,		-6,			-7,			-8,			-9,			-10,		-11,			-12,	-13,		-14,	-15,		-16,			-17,		-18,	-19,		-20,	-21,		-22,			1 };
	private static final String[] const_mousecursorconstants_name	= { "crDefault","crNone","crArrow","crCross","crIBeam","crSize","crSizeNESW","crSizeNS","crSizeNWSE","crSizeWE","crUpArrow","crHourGlass","crDrag","crNoDrop","crHSplit","crVSplit","crMultiDrag","crSQLWait","crNo","crAppStart","crHelp","crHandPoint","crSizeAll","crHBeam"};

	// colorconstants
	private static final int[] const_colorconstants_val		= { 0x80000000,		0x80000001,		0x80000002,			0x80000003,			0x80000004, 0x80000005, 0x80000006,		0x80000007, 0x80000008,		0x80000009,		0x8000000a,			0x8000000b,			0x8000000c,		0x8000000d,		0x8000000e,			0x8000000f,	0x80000010,		0x80000011,	0x80000012,		0x80000013,				0x80000014,		0x80000015,		0x80000016,		0x80000017, 0x80000018, 0x1fffffff, 0x01ffffff, 0x3000000, 0x4000000 };
	private static final String[] const_colorconstants_name = { "clScrollBar", "clBackground", "clActiveCaption", "clInactiveCaption", "clMenu", "clWindow", "clWindowFrame", "clMenuText", "clWindowText", "clCaptionText", "clActiveBorder", "clInactiveBorder", "clAppWorkSpace", "clHighlight", "clHighlightText", "clBtnFace", "clBtnShadow", "clGrayText", "clBtnText", "clInactiveCaptionText", "clBtnHighlight", "cl3DDkShadow", "cl3DLight", "clInfoText", "clInfoBk", "clNone", "clAdapt", "clPalIdx", "clAlphaMat" };

	// forMenu.trackPopup(seewinuser.h)
	private static final int[] const_MenuTrackPopup_val		= { 0x0000,			0x0002,			0x0000,			0x0004,			0x0008,			0x0000,			0x0010,			0x0020,				0x0000,			0x0040,		0x0080,			0x0100,		0x0001,			0x0400,				0x0800,				0x1000,				0x2000,				0x4000 };
	private static final String[] const_MenuTrackPopup_name = { "tpmLeftButton","tpmRightButton","tpmLeftAlign","tpmCenterAlign","tpmRightAlign","tpmTopAlign","tpmVCenterAlign","tpmBottomAlign","tpmHorizontal","tpmVertical","tpmNoNotify","tpmReturnCmd","tpmRecurse","tpmHorPosAnimation","tpmHorNegAnimation","tpmVerPosAnimation","tpmVerNegAnimation","tpmNoAnimation" };

	// forPad.showScrollBars(seeVcl/stdctrls.hpp::enumTScrollStyle)
	private static final int[] const_ShowScrollBars_val		= { 0,			1,			2,				3 };
	private static final String[] const_ShowScrollBars_name	= { "ssNone", "ssHorizontal", "ssVertical", "ssBoth" };

	// virtualkeycodes
	private static final int[] const_virtualkeycodes_val	= { VirtualKey.VK_LBUTTON, VirtualKey.VK_RBUTTON, VirtualKey.VK_CANCEL, VirtualKey.VK_MBUTTON, VirtualKey.VK_BACK, VirtualKey.VK_TAB, VirtualKey.VK_CLEAR, VirtualKey.VK_RETURN, VirtualKey.VK_SHIFT, VirtualKey.VK_CONTROL, VirtualKey.VK_MENU, VirtualKey.VK_PAUSE, VirtualKey.VK_CAPITAL, VirtualKey.VK_KANA, VirtualKey.VK_HANGEUL, VirtualKey.VK_HANGUL, VirtualKey.VK_JUNJA, VirtualKey.VK_FINAL, VirtualKey.VK_HANJA, VirtualKey.VK_KANJI, VirtualKey.VK_ESCAPE, VirtualKey.VK_CONVERT, VirtualKey.VK_NONCONVERT, VirtualKey.VK_ACCEPT, VirtualKey.VK_MODECHANGE, VirtualKey.VK_SPACE, VirtualKey.VK_PRIOR, VirtualKey.VK_NEXT, VirtualKey.VK_END, VirtualKey.VK_HOME, VirtualKey.VK_LEFT, VirtualKey.VK_UP, VirtualKey.VK_RIGHT, VirtualKey.VK_DOWN, VirtualKey.VK_SELECT, VirtualKey.VK_PRINT, VirtualKey.VK_EXECUTE, VirtualKey.VK_SNAPSHOT, VirtualKey.VK_INSERT, VirtualKey.VK_DELETE, VirtualKey.VK_HELP, VirtualKey.VK_0, VirtualKey.VK_1, VirtualKey.VK_2, VirtualKey.VK_3, VirtualKey.VK_4, VirtualKey.VK_5, VirtualKey.VK_6, VirtualKey.VK_7, VirtualKey.VK_8, VirtualKey.VK_9, VirtualKey.VK_A, VirtualKey.VK_B, VirtualKey.VK_C, VirtualKey.VK_D, VirtualKey.VK_E, VirtualKey.VK_F, VirtualKey.VK_G, VirtualKey.VK_H, VirtualKey.VK_I, VirtualKey.VK_J, VirtualKey.VK_K, VirtualKey.VK_L, VirtualKey.VK_M, VirtualKey.VK_N, VirtualKey.VK_O, VirtualKey.VK_P, VirtualKey.VK_Q, VirtualKey.VK_R, VirtualKey.VK_S, VirtualKey.VK_T, VirtualKey.VK_U, VirtualKey.VK_V, VirtualKey.VK_W, VirtualKey.VK_X, VirtualKey.VK_Y, VirtualKey.VK_Z, VirtualKey.VK_LWIN, VirtualKey.VK_RWIN, VirtualKey.VK_APPS, VirtualKey.VK_NUMPAD0, VirtualKey.VK_NUMPAD1, VirtualKey.VK_NUMPAD2, VirtualKey.VK_NUMPAD3, VirtualKey.VK_NUMPAD4, VirtualKey.VK_NUMPAD5, VirtualKey.VK_NUMPAD6, VirtualKey.VK_NUMPAD7, VirtualKey.VK_NUMPAD8, VirtualKey.VK_NUMPAD9, VirtualKey.VK_MULTIPLY, VirtualKey.VK_ADD, VirtualKey.VK_SEPARATOR, VirtualKey.VK_SUBTRACT, VirtualKey.VK_DECIMAL, VirtualKey.VK_DIVIDE, VirtualKey.VK_F1, VirtualKey.VK_F2, VirtualKey.VK_F3, VirtualKey.VK_F4, VirtualKey.VK_F5, VirtualKey.VK_F6, VirtualKey.VK_F7, VirtualKey.VK_F8, VirtualKey.VK_F9, VirtualKey.VK_F10, VirtualKey.VK_F11, VirtualKey.VK_F12, VirtualKey.VK_F13, VirtualKey.VK_F14, VirtualKey.VK_F15, VirtualKey.VK_F16, VirtualKey.VK_F17, VirtualKey.VK_F18, VirtualKey.VK_F19, VirtualKey.VK_F20, VirtualKey.VK_F21, VirtualKey.VK_F22, VirtualKey.VK_F23, VirtualKey.VK_F24, VirtualKey.VK_NUMLOCK, VirtualKey.VK_SCROLL, VirtualKey.VK_LSHIFT, VirtualKey.VK_RSHIFT, VirtualKey.VK_LCONTROL, VirtualKey.VK_RCONTROL, VirtualKey.VK_LMENU, VirtualKey.VK_RMENUZ };
	private static final String[] const_virtualkeycodes_name= { "VK_LBUTTON", "VK_RBUTTON", "VK_CANCEL", "VK_MBUTTON", "VK_BACK", "VK_TAB", "VK_CLEAR", "VK_RETURN", "VK_SHIFT", "VK_CONTROL", "VK_MENU", "VK_PAUSE", "VK_CAPITAL", "VK_KANA", "VK_HANGEUL", "VK_HANGUL", "VK_JUNJA", "VK_FINAL", "VK_HANJA", "VK_KANJI", "VK_ESCAPE", "VK_CONVERT", "VK_NONCONVERT", "VK_ACCEPT", "VK_MODECHANGE", "VK_SPACE", "VK_PRIOR", "VK_NEXT", "VK_END", "VK_HOME", "VK_LEFT", "VK_UP", "VK_RIGHT", "VK_DOWN", "VK_SELECT", "VK_PRINT", "VK_EXECUTE", "VK_SNAPSHOT", "VK_INSERT", "VK_DELETE", "VK_HELP", "VK_0", "VK_1", "VK_2", "VK_3", "VK_4", "VK_5", "VK_6", "VK_7", "VK_8", "VK_9", "VK_A", "VK_B", "VK_C", "VK_D", "VK_E", "VK_F", "VK_G", "VK_H", "VK_I", "VK_J", "VK_K", "VK_L", "VK_M", "VK_N", "VK_O", "VK_P", "VK_Q", "VK_R", "VK_S", "VK_T", "VK_U", "VK_V", "VK_W", "VK_X", "VK_Y", "VK_Z", "VK_LWIN", "VK_RWIN", "VK_APPS", "VK_NUMPAD0", "VK_NUMPAD1", "VK_NUMPAD2", "VK_NUMPAD3", "VK_NUMPAD4", "VK_NUMPAD5", "VK_NUMPAD6", "VK_NUMPAD7", "VK_NUMPAD8", "VK_NUMPAD9", "VK_MULTIPLY", "VK_ADD", "VK_SEPARATOR", "VK_SUBTRACT", "VK_DECIMAL", "VK_DIVIDE", "VK_F1", "VK_F2", "VK_F3", "VK_F4", "VK_F5", "VK_F6", "VK_F7", "VK_F8", "VK_F9", "VK_F10", "VK_F11", "VK_F12", "VK_F13", "VK_F14", "VK_F15", "VK_F16", "VK_F17", "VK_F18", "VK_F19", "VK_F20", "VK_F21", "VK_F22", "VK_F23", "VK_F24", "VK_NUMLOCK", "VK_SCROLL", "VK_LSHIFT", "VK_RSHIFT", "VK_LCONTROL", "VK_RCONTROL", "VK_LMENU", "VK_RMENUZ" };

	// VK_PADXXXXareKIRIKIRIspecific
	private static final int[] const_VK_PAD_val	= { VirtualKey.VK_PADLEFT, VirtualKey.VK_PADUP, VirtualKey.VK_PADRIGHT, VirtualKey.VK_PADDOWN, VirtualKey.VK_PAD1, VirtualKey.VK_PAD2, VirtualKey.VK_PAD3, VirtualKey.VK_PAD4, VirtualKey.VK_PAD5, VirtualKey.VK_PAD6, VirtualKey.VK_PAD7, VirtualKey.VK_PAD8, VirtualKey.VK_PAD9, VirtualKey.VK_PAD10, VirtualKey.VK_PADANY, VirtualKey.VK_PROCESSKEY, VirtualKey.VK_ATTN, VirtualKey.VK_CRSEL, VirtualKey.VK_EXSEL, VirtualKey.VK_EREOF, VirtualKey.VK_PLAY, VirtualKey.VK_ZOOM, VirtualKey.VK_NONAME, VirtualKey.VK_PA1, VirtualKey.VK_OEM_CLEAR };
	private static final String[] const_VK_PAD_name= { "VK_PADLEFT", "VK_PADUP", "VK_PADRIGHT", "VK_PADDOWN", "VK_PAD1", "VK_PAD2", "VK_PAD3", "VK_PAD4", "VK_PAD5", "VK_PAD6", "VK_PAD7", "VK_PAD8", "VK_PAD9", "VK_PAD10", "VK_PADANY", "VK_PROCESSKEY", "VK_ATTN", "VK_CRSEL", "VK_EXSEL", "VK_EREOF", "VK_PLAY", "VK_ZOOM", "VK_NONAME", "VK_PA1", "VK_OEM_CLEAR" };

	// graphiccachesystem
	private static final int[] const_graphiccachesystem_val	= { -1 };
	private static final String[] const_graphiccachesystem_name= { "gcsAuto" };

	// drawthreadnum
	private static final int[] const_drawthreadnum_val	= { 0 };
	private static final String[] const_drawthreadnum_name= { "dtnAuto" };
	*/

	// BorderStyle

	private static final int[] const_val	= {
		// BorderStyle
		0, 1, 2, 3, 4, 5,
		// UpdateType
		0, 1,
		// MouseButtont
		0, 1, 2,
		// MouseCursorState
		0, 1, 2,
		// ImeMode
		0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11,
		// Setofshiftstate
		(1<<0), (1<<1), (1<<2), (1<<3), (1<<4), (1<<5), (1<<6), (1<<7),
		// TVP_FSF_????
		1, 2, 4, 8, 0x100,
		// LayerType
		LayerType.ltBinder, LayerType.ltCoverRect, LayerType.ltOpaque, LayerType.ltTransparent, LayerType.ltAlpha, LayerType.ltAdditive, LayerType.ltSubtractive, LayerType.ltMultiplicative, LayerType.ltEffect, LayerType.ltFilter, LayerType.ltDodge, LayerType.ltDarken, LayerType.ltLighten, LayerType.ltScreen, LayerType.ltAddAlpha, LayerType.ltPsNormal, LayerType.ltPsAdditive, LayerType.ltPsSubtractive, LayerType.ltPsMultiplicative, LayerType.ltPsScreen, LayerType.ltPsOverlay, LayerType.ltPsHardLight, LayerType.ltPsSoftLight, LayerType.ltPsColorDodge, LayerType.ltPsColorDodge5, LayerType.ltPsColorBurn, LayerType.ltPsLighten, LayerType.ltPsDarken, LayerType.ltPsDifference, LayerType.ltPsDifference5, LayerType.ltPsExclusion,
		// BlendOperationMode
		LayerType.ltPsNormal, LayerType.ltPsAdditive, LayerType.ltPsSubtractive, LayerType.ltPsMultiplicative, LayerType.ltPsScreen, LayerType.ltPsOverlay, LayerType.ltPsHardLight, LayerType.ltPsSoftLight, LayerType.ltPsColorDodge, LayerType.ltPsColorDodge5, LayerType.ltPsColorBurn, LayerType.ltPsLighten, LayerType.ltPsDarken, LayerType.ltPsDifference, LayerType.ltPsDifference5, LayerType.ltPsExclusion, LayerType.ltAdditive, LayerType.ltSubtractive, LayerType.ltMultiplicative, LayerType.ltDodge, LayerType.ltDarken, LayerType.ltLighten, LayerType.ltScreen, LayerType.ltAddAlpha, LayerType.ltOpaque, LayerType.ltAlpha, 128,
		// DrawFace
		0, 0, 4, 1, 1, 2, 3, 128,
		// HitType
		0, 1,
		// ScrollTransFrom
		0, 1, 2, 3,
		// ScrollTransStay
		0, 1, 2,
		// KAGDebugLevel
		0, 1, 2,
		// AsyncTriggerMode
		0, 1, 2,
		// BBStretchType
		0, 1, 2,  3, 0x10,
		// ClipboardFormat
		0,
		// COMPACT_LEVEL_????
		5, 10, 15, 100,
		// VideoOverlayModeAdd
		0, 1, 2,
		// PeriodEventReason
		0, 1, 2, 3,
		// SoundGlobalFocusMode
		0, 1, 2,
		// fileattributes
		0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x3f,
		// mousecursorconstants
		0x0, -1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -12, -13, -14, -15, -16, -17, -18, -19, -20, -21, -22, 1,
		// colorconstants
		0x80000000,		0x80000001, 0x80000002, 0x80000003, 0x80000004, 0x80000005, 0x80000006, 0x80000007, 0x80000008, 0x80000009, 0x8000000a, 0x8000000b, 0x8000000c, 0x8000000d, 0x8000000e, 0x8000000f, 0x80000010, 0x80000011, 0x80000012, 0x80000013, 0x80000014, 0x80000015, 0x80000016, 0x80000017, 0x80000018, 0x1fffffff, 0x01ffffff, 0x3000000, 0x4000000,
		// forMenu.trackPopup(seewinuser.h)
		0x0000, 0x0002, 0x0000, 0x0004, 0x0008, 0x0000, 0x0010, 0x0020, 0x0000, 0x0040, 0x0080, 0x0100, 0x0001, 0x0400, 0x0800, 0x1000, 0x2000, 0x4000,
		// forPad.showScrollBars(seeVcl/stdctrls.hpp::enumTScrollStyle)
		0, 1, 2, 3,
		// virtualkeycodes
		VirtualKey.VK_LBUTTON, VirtualKey.VK_RBUTTON, VirtualKey.VK_CANCEL, VirtualKey.VK_MBUTTON, VirtualKey.VK_BACK, VirtualKey.VK_TAB, VirtualKey.VK_CLEAR, VirtualKey.VK_RETURN, VirtualKey.VK_SHIFT, VirtualKey.VK_CONTROL, VirtualKey.VK_MENU, VirtualKey.VK_PAUSE, VirtualKey.VK_CAPITAL, VirtualKey.VK_KANA, VirtualKey.VK_HANGEUL, VirtualKey.VK_HANGUL, VirtualKey.VK_JUNJA, VirtualKey.VK_FINAL, VirtualKey.VK_HANJA, VirtualKey.VK_KANJI, VirtualKey.VK_ESCAPE, VirtualKey.VK_CONVERT, VirtualKey.VK_NONCONVERT, VirtualKey.VK_ACCEPT, VirtualKey.VK_MODECHANGE, VirtualKey.VK_SPACE, VirtualKey.VK_PRIOR, VirtualKey.VK_NEXT, VirtualKey.VK_END, VirtualKey.VK_HOME, VirtualKey.VK_LEFT, VirtualKey.VK_UP, VirtualKey.VK_RIGHT, VirtualKey.VK_DOWN, VirtualKey.VK_SELECT, VirtualKey.VK_PRINT, VirtualKey.VK_EXECUTE, VirtualKey.VK_SNAPSHOT, VirtualKey.VK_INSERT, VirtualKey.VK_DELETE, VirtualKey.VK_HELP, VirtualKey.VK_0, VirtualKey.VK_1, VirtualKey.VK_2, VirtualKey.VK_3, VirtualKey.VK_4, VirtualKey.VK_5, VirtualKey.VK_6, VirtualKey.VK_7, VirtualKey.VK_8, VirtualKey.VK_9, VirtualKey.VK_A, VirtualKey.VK_B, VirtualKey.VK_C, VirtualKey.VK_D, VirtualKey.VK_E, VirtualKey.VK_F, VirtualKey.VK_G, VirtualKey.VK_H, VirtualKey.VK_I, VirtualKey.VK_J, VirtualKey.VK_K, VirtualKey.VK_L, VirtualKey.VK_M, VirtualKey.VK_N, VirtualKey.VK_O, VirtualKey.VK_P, VirtualKey.VK_Q, VirtualKey.VK_R, VirtualKey.VK_S, VirtualKey.VK_T, VirtualKey.VK_U, VirtualKey.VK_V, VirtualKey.VK_W, VirtualKey.VK_X, VirtualKey.VK_Y, VirtualKey.VK_Z, VirtualKey.VK_LWIN, VirtualKey.VK_RWIN, VirtualKey.VK_APPS, VirtualKey.VK_NUMPAD0, VirtualKey.VK_NUMPAD1, VirtualKey.VK_NUMPAD2, VirtualKey.VK_NUMPAD3, VirtualKey.VK_NUMPAD4, VirtualKey.VK_NUMPAD5, VirtualKey.VK_NUMPAD6, VirtualKey.VK_NUMPAD7, VirtualKey.VK_NUMPAD8, VirtualKey.VK_NUMPAD9, VirtualKey.VK_MULTIPLY, VirtualKey.VK_ADD, VirtualKey.VK_SEPARATOR, VirtualKey.VK_SUBTRACT, VirtualKey.VK_DECIMAL, VirtualKey.VK_DIVIDE, VirtualKey.VK_F1, VirtualKey.VK_F2, VirtualKey.VK_F3, VirtualKey.VK_F4, VirtualKey.VK_F5, VirtualKey.VK_F6, VirtualKey.VK_F7, VirtualKey.VK_F8, VirtualKey.VK_F9, VirtualKey.VK_F10, VirtualKey.VK_F11, VirtualKey.VK_F12, VirtualKey.VK_F13, VirtualKey.VK_F14, VirtualKey.VK_F15, VirtualKey.VK_F16, VirtualKey.VK_F17, VirtualKey.VK_F18, VirtualKey.VK_F19, VirtualKey.VK_F20, VirtualKey.VK_F21, VirtualKey.VK_F22, VirtualKey.VK_F23, VirtualKey.VK_F24, VirtualKey.VK_NUMLOCK, VirtualKey.VK_SCROLL, VirtualKey.VK_LSHIFT, VirtualKey.VK_RSHIFT, VirtualKey.VK_LCONTROL, VirtualKey.VK_RCONTROL, VirtualKey.VK_LMENU, VirtualKey.VK_RMENUZ,
		// VK_PADXXXXareKIRIKIRIspecific
		VirtualKey.VK_PADLEFT, VirtualKey.VK_PADUP, VirtualKey.VK_PADRIGHT, VirtualKey.VK_PADDOWN, VirtualKey.VK_PAD1, VirtualKey.VK_PAD2, VirtualKey.VK_PAD3, VirtualKey.VK_PAD4, VirtualKey.VK_PAD5, VirtualKey.VK_PAD6, VirtualKey.VK_PAD7, VirtualKey.VK_PAD8, VirtualKey.VK_PAD9, VirtualKey.VK_PAD10, VirtualKey.VK_PADANY, VirtualKey.VK_PROCESSKEY, VirtualKey.VK_ATTN, VirtualKey.VK_CRSEL, VirtualKey.VK_EXSEL, VirtualKey.VK_EREOF, VirtualKey.VK_PLAY, VirtualKey.VK_ZOOM, VirtualKey.VK_NONAME, VirtualKey.VK_PA1, VirtualKey.VK_OEM_CLEAR,
		// graphiccachesystem
		-1,
		// drawthreadnum
		0 };

	private static final String[] const_name = {
		// BorderStyle
		"bsNone", "bsSingle", "bsSizeable", "bsDialog", "bsToolWindow", "bsSizeToolWin",
		// UpdateType
		"utNormal", "utEntire",
		// MouseButtont
		"mbLeft", "mbRight", "mbMiddle",
		// MouseCursorState
		"mcsVisible", "mcsTempHidden", "mcsHidden",
		// ImeMode
		"imDisable", "imClose", "imOpen", "imDontCare", "imSAlpha", "imAlpha", "imHira", "imSKata", "imKata", "imChinese", "imSHanguel", "imHanguel",
		// Setofshiftstate
		"ssShift", "ssAlt", "ssCtrl", "ssLeft", "ssRight", "ssMiddle", "ssDouble", "ssRepeat",
		// TVP_FSF_????
		"fsfFixedPitch", "fsfSameCharSet", "fsfNoVertical", "fsfTrueTypeOnly", "fsfUseFontFace",
		// LayerType
		"ltBinder", "ltCoverRect", "ltOpaque", "ltTransparent", "ltAlpha", "ltAdditive", "ltSubtractive", "ltMultiplicative", "ltEffect", "ltFilter", "ltDodge", "ltDarken", "ltLighten", "ltScreen", "ltAddAlpha", "ltPsNormal", "ltPsAdditive", "ltPsSubtractive", "ltPsMultiplicative", "ltPsScreen", "ltPsOverlay", "ltPsHardLight", "ltPsSoftLight", "ltPsColorDodge", "ltPsColorDodge5", "ltPsColorBurn", "ltPsLighten", "ltPsDarken", "ltPsDifference", "ltPsDifference5", "ltPsExclusion",
		// BlendOperationMode
		"omPsNormal", "omPsAdditive", "omPsSubtractive", "omPsMultiplicative", "omPsScreen",			"omPsOverlay", "omPsHardLight", "omPsSoftLight", "omPsColorDodge", "omPsColorDodge5", "omPsColorBurn", "omPsLighten", "omPsDarken", "omPsDifference",			"omPsDifference5", "omPsExclusion", "omAdditive", 			"omSubtractive", 		"omMultiplicative", 		"omDodge", 			"omDarken", 		"omLighten", 		"omScreen", 		"omAddAlpha", 		"omOpaque", 		"omAlpha", 			"omAuto",
		// DrawFace
		"dfBoth", "dfAlpha","dfAddAlpha", "dfMain", "dfOpaque", "dfMask", "dfProvince", "dfAuto",
		// HitType
		"htMask", "htProvince",
		// ScrollTransFrom
		"sttLeft", "sttTop", "sttRight", "sttBottom",
		// ScrollTransStay
		"ststNoStay", "ststStayDest", "ststStaySrc",
		// KAGDebugLevel
		"tkdlNone", "tkdlSimple", "tkdlVerbose",
		// AsyncTriggerMode
		"atmNormal", "atmExclusive", "atmAtIdle",
		// BBStretchType
		"stNearest", "stFastLinear", "stLinear", "stCubic", "stRefNoClip",
		// ClipboardFormat
		"cbfText",
		// COMPACT_LEVEL_????
		"clIdle", "clDeactivate", "clMinimize", "clAll",
		// VideoOverlayModeAdd
		"vomOverlay", "vomLayer", "vomMixer",
		// PeriodEventReason
		"perLoop", "perPeriod", "perPrepare", "perSegLoop",
		// SoundGlobalFocusMode
		"sgfmNeverMute", "sgfmMuteOnMinimize", "sgfmMuteOnDeactivate",
		// fileattributes
		"faReadOnly", "faHidden", "faSysFile", "faVolumeID", "faDirectory", "faArchive", "faAnyFile",
		// mousecursorconstants
		"crDefault","crNone","crArrow","crCross","crIBeam","crSize","crSizeNESW","crSizeNS","crSizeNWSE","crSizeWE","crUpArrow","crHourGlass","crDrag","crNoDrop","crHSplit","crVSplit","crMultiDrag","crSQLWait","crNo","crAppStart","crHelp","crHandPoint","crSizeAll","crHBeam",
		// colorconstants
		"clScrollBar", "clBackground", "clActiveCaption", "clInactiveCaption", "clMenu", "clWindow", "clWindowFrame", "clMenuText", "clWindowText", "clCaptionText", "clActiveBorder", "clInactiveBorder", "clAppWorkSpace", "clHighlight", "clHighlightText", "clBtnFace", "clBtnShadow", "clGrayText", "clBtnText", "clInactiveCaptionText", "clBtnHighlight", "cl3DDkShadow", "cl3DLight", "clInfoText", "clInfoBk", "clNone", "clAdapt", "clPalIdx", "clAlphaMat",
		// forMenu.trackPopup(seewinuser.h)
		"tpmLeftButton","tpmRightButton","tpmLeftAlign","tpmCenterAlign","tpmRightAlign","tpmTopAlign","tpmVCenterAlign","tpmBottomAlign","tpmHorizontal","tpmVertical","tpmNoNotify","tpmReturnCmd","tpmRecurse","tpmHorPosAnimation","tpmHorNegAnimation","tpmVerPosAnimation","tpmVerNegAnimation","tpmNoAnimation",
		// forPad.showScrollBars(seeVcl/stdctrls.hpp::enumTScrollStyle)
		"ssNone", "ssHorizontal", "ssVertical", "ssBoth",
		// virtualkeycodes
		"VK_LBUTTON", "VK_RBUTTON", "VK_CANCEL", "VK_MBUTTON", "VK_BACK", "VK_TAB", "VK_CLEAR", "VK_RETURN", "VK_SHIFT", "VK_CONTROL", "VK_MENU", "VK_PAUSE", "VK_CAPITAL", "VK_KANA", "VK_HANGEUL", "VK_HANGUL", "VK_JUNJA", "VK_FINAL", "VK_HANJA", "VK_KANJI", "VK_ESCAPE", "VK_CONVERT", "VK_NONCONVERT", "VK_ACCEPT", "VK_MODECHANGE", "VK_SPACE", "VK_PRIOR", "VK_NEXT", "VK_END", "VK_HOME", "VK_LEFT", "VK_UP", "VK_RIGHT", "VK_DOWN", "VK_SELECT", "VK_PRINT", "VK_EXECUTE", "VK_SNAPSHOT", "VK_INSERT", "VK_DELETE", "VK_HELP", "VK_0", "VK_1", "VK_2", "VK_3", "VK_4", "VK_5", "VK_6", "VK_7", "VK_8", "VK_9", "VK_A", "VK_B", "VK_C", "VK_D", "VK_E", "VK_F", "VK_G", "VK_H", "VK_I", "VK_J", "VK_K", "VK_L", "VK_M", "VK_N", "VK_O", "VK_P", "VK_Q", "VK_R", "VK_S", "VK_T", "VK_U", "VK_V", "VK_W", "VK_X", "VK_Y", "VK_Z", "VK_LWIN", "VK_RWIN", "VK_APPS", "VK_NUMPAD0", "VK_NUMPAD1", "VK_NUMPAD2", "VK_NUMPAD3", "VK_NUMPAD4", "VK_NUMPAD5", "VK_NUMPAD6", "VK_NUMPAD7", "VK_NUMPAD8", "VK_NUMPAD9", "VK_MULTIPLY", "VK_ADD", "VK_SEPARATOR", "VK_SUBTRACT", "VK_DECIMAL", "VK_DIVIDE", "VK_F1", "VK_F2", "VK_F3", "VK_F4", "VK_F5", "VK_F6", "VK_F7", "VK_F8", "VK_F9", "VK_F10", "VK_F11", "VK_F12", "VK_F13", "VK_F14", "VK_F15", "VK_F16", "VK_F17", "VK_F18", "VK_F19", "VK_F20", "VK_F21", "VK_F22", "VK_F23", "VK_F24", "VK_NUMLOCK", "VK_SCROLL", "VK_LSHIFT", "VK_RSHIFT", "VK_LCONTROL", "VK_RCONTROL", "VK_LMENU", "VK_RMENUZ",
		// VK_PADXXXXareKIRIKIRIspecific
		"VK_PADLEFT", "VK_PADUP", "VK_PADRIGHT", "VK_PADDOWN", "VK_PAD1", "VK_PAD2", "VK_PAD3", "VK_PAD4", "VK_PAD5", "VK_PAD6", "VK_PAD7", "VK_PAD8", "VK_PAD9", "VK_PAD10", "VK_PADANY", "VK_PROCESSKEY", "VK_ATTN", "VK_CRSEL", "VK_EXSEL", "VK_EREOF", "VK_PLAY", "VK_ZOOM", "VK_NONAME", "VK_PA1", "VK_OEM_CLEAR",
		// graphiccachesystem
		"gcsAuto",
		// drawthreadnum
		"dtnAuto" };

	private static final String TYPE = "type";
	/**
	 * 定数を登録する
	 * オリジナルではスクリプトになっているが、ここでは直接呼び出して登録している
	 **/
	private static void initializeScriptSource() throws VariantException, TJSException {
		Dispatch2 global = mScriptEngine.getGlobal();
		if( global instanceof CustomObject ) {
			CustomObject g = (CustomObject) global;
			g.propSetConstArray( const_name, const_val, g );
			/*
			g.propSetConstArray( const_UpdateType_name, const_UpdateType_val, g );
			g.propSetConstArray( const_MouseButton_name, const_MouseButton_val, g );
			g.propSetConstArray( const_MouseCursorState_name, const_MouseCursorState_val, g );
			g.propSetConstArray( const_ImeMode_name, const_ImeMode_val, g );
			g.propSetConstArray( const_Setofshiftstate_name, const_Setofshiftstate_val, g );
			g.propSetConstArray( const_FSF_name, const_FSF_val, g );
			g.propSetConstArray( const_LayerType_name, const_LayerType_val, g );
			g.propSetConstArray( const_BlendOperationMode_name, const_BlendOperationMode_val, g );
			g.propSetConstArray( const_DrawFace_name, const_DrawFace_val, g );
			g.propSetConstArray( const_HitType_name, const_HitType_val, g );
			g.propSetConstArray( const_ScrollTransFrom_name, const_ScrollTransFrom_val, g );
			g.propSetConstArray( const_ScrollTransStay_name, const_ScrollTransStay_val, g );
			g.propSetConstArray( const_KAGDebugLevel_name, const_KAGDebugLevel_val, g );
			g.propSetConstArray( const_AsyncTriggerMode_name, const_AsyncTriggerMode_val, g );
			g.propSetConstArray( const_BBStretchType_name, const_BBStretchType_val, g );
			g.propSetConstArray( const_ClipboardFormat_name, const_ClipboardFormat_val, g );
			g.propSetConstArray( const_COMPACT_LEVEL_name, const_COMPACT_LEVEL_val, g );
			g.propSetConstArray( const_VideoOverlayModeAdd_name, const_VideoOverlayModeAdd_val, g );
			g.propSetConstArray( const_PeriodEventReason_name, const_PeriodEventReason_val, g );
			g.propSetConstArray( const_SoundGlobalFocusMode_name, const_SoundGlobalFocusMode_val, g );
			g.propSetConstArray( const_fileattributes_name, const_fileattributes_val, g );
			g.propSetConstArray( const_mousecursorconstants_name, const_mousecursorconstants_val, g );
			g.propSetConstArray( const_colorconstants_name, const_colorconstants_val, g );
			g.propSetConstArray( const_MenuTrackPopup_name, const_MenuTrackPopup_val, g );
			g.propSetConstArray( const_ShowScrollBars_name, const_ShowScrollBars_val, g );
			g.propSetConstArray( const_virtualkeycodes_name, const_virtualkeycodes_val, g );
			g.propSetConstArray( const_VK_PAD_name, const_VK_PAD_val, g );
			g.propSetConstArray( const_graphiccachesystem_name, const_graphiccachesystem_val, g );
			g.propSetConstArray( const_drawthreadnum_name, const_drawthreadnum_val, g );
			*/


			// image'mode'tag(mainlyisgeneratedbyimageformatconverter)constants
			// imageTagLayerType=%[
			// opaque:%[type:ltOpaque],
			// rect:%[type:ltOpaque],
			// alpha:%[type:ltAlpha],
			// transparent:%[type:ltAlpha],
			// addalpha:%[type:ltAddAlpha],
			// add:%[type:ltAdditive],
			// sub:%[type:ltSubtractive],
			// mul:%[type:ltMultiplicative],
			// dodge:%[type:ltDodge],
			// darken:%[type:ltDarken],
			// lighten:%[type:ltLighten],
			// screen:%[type:ltScreen],
			// psnormal:%[type:ltPsNormal],
			// psadd:%[type:ltPsAdditive],
			// pssub:%[type:ltPsSubtractive],
			// psmul:%[type:ltPsMultiplicative],
			// psscreen:%[type:ltPsScreen],
			// psoverlay:%[type:ltPsOverlay],
			// pshlight:%[type:ltPsHardLight],
			// psslight:%[type:ltPsSoftLight],
			// psdodge:%[type:ltPsColorDodge],
			// psdodge5:%[type:ltPsColorDodge5],
			// psburn:%[type:ltPsColorBurn],
			// pslighten:%[type:ltPsLighten],
			// psdarken:%[type:ltPsDarken],
			// psdiff:%[type:ltPsDifference],
			// psdiff5:%[type:ltPsDifference5],
			// psexcl:%[type:ltPsExclusion],];
			Holder<Dispatch2> holder = new Holder<Dispatch2>(null);
			Dispatch2 dic = TJS.createDictionaryObject(holder);
			Dispatch2 creater = holder.mValue;

			creater.createNew(0, null, holder, null, creater );
			Dispatch2 tmp = holder.mValue;
			Variant val = new Variant(LayerType.ltOpaque);
			tmp.propSet(Interface.MEMBERENSURE, TYPE, val, null);
			val.set(tmp,tmp);
			dic.propSet(Interface.MEMBERENSURE, "opaque", val, null);

			creater.createNew(0, null, holder, null, creater );
			tmp = holder.mValue;
			val.set(LayerType.ltOpaque);
			tmp.propSet(Interface.MEMBERENSURE, TYPE, val, null);
			val.set(tmp,tmp);
			dic.propSet(Interface.MEMBERENSURE, "rect", val, null);

			creater.createNew(0, null, holder, null, creater );
			tmp = holder.mValue;
			val.set(LayerType.ltAlpha);
			tmp.propSet(Interface.MEMBERENSURE, TYPE, val, null);
			val.set(tmp,tmp);
			dic.propSet(Interface.MEMBERENSURE, "alpha", val, null);

			creater.createNew(0, null, holder, null, creater );
			tmp = holder.mValue;
			val.set(LayerType.ltAlpha);
			tmp.propSet(Interface.MEMBERENSURE, TYPE, val, null);
			val.set(tmp,tmp);
			dic.propSet(Interface.MEMBERENSURE, "transparent", val, null);

			creater.createNew(0, null, holder, null, creater );
			tmp = holder.mValue;
			val.set(LayerType.ltAddAlpha);
			tmp.propSet(Interface.MEMBERENSURE, TYPE, val, null);
			val.set(tmp,tmp);
			dic.propSet(Interface.MEMBERENSURE, "addalpha", val, null);

			creater.createNew(0, null, holder, null, creater );
			tmp = holder.mValue;
			val.set(LayerType.ltAdditive);
			tmp.propSet(Interface.MEMBERENSURE, TYPE, val, null);
			val.set(tmp,tmp);
			dic.propSet(Interface.MEMBERENSURE, "add", val, null);

			creater.createNew(0, null, holder, null, creater );
			tmp = holder.mValue;
			val.set(LayerType.ltSubtractive);
			tmp.propSet(Interface.MEMBERENSURE, TYPE, val, null);
			val.set(tmp,tmp);
			dic.propSet(Interface.MEMBERENSURE, "sub", val, null);

			creater.createNew(0, null, holder, null, creater );
			tmp = holder.mValue;
			val.set(LayerType.ltMultiplicative);
			tmp.propSet(Interface.MEMBERENSURE, TYPE, val, null);
			val.set(tmp,tmp);
			dic.propSet(Interface.MEMBERENSURE, "mul", val, null);

			creater.createNew(0, null, holder, null, creater );
			tmp = holder.mValue;
			val.set(LayerType.ltDodge);
			tmp.propSet(Interface.MEMBERENSURE, TYPE, val, null);
			val.set(tmp,tmp);
			dic.propSet(Interface.MEMBERENSURE, "dodge", val, null);

			creater.createNew(0, null, holder, null, creater );
			tmp = holder.mValue;
			val.set(LayerType.ltDarken);
			tmp.propSet(Interface.MEMBERENSURE, TYPE, val, null);
			val.set(tmp,tmp);
			dic.propSet(Interface.MEMBERENSURE, "darken", val, null);

			creater.createNew(0, null, holder, null, creater );
			tmp = holder.mValue;
			val.set(LayerType.ltLighten);
			tmp.propSet(Interface.MEMBERENSURE, TYPE, val, null);
			val.set(tmp,tmp);
			dic.propSet(Interface.MEMBERENSURE, "lighten", val, null);

			creater.createNew(0, null, holder, null, creater );
			tmp = holder.mValue;
			val.set(LayerType.ltScreen);
			tmp.propSet(Interface.MEMBERENSURE, TYPE, val, null);
			val.set(tmp,tmp);
			dic.propSet(Interface.MEMBERENSURE, "screen", val, null);

			creater.createNew(0, null, holder, null, creater );
			tmp = holder.mValue;
			val.set(LayerType.ltPsNormal);
			tmp.propSet(Interface.MEMBERENSURE, TYPE, val, null);
			val.set(tmp,tmp);
			dic.propSet(Interface.MEMBERENSURE, "psnormal", val, null);

			creater.createNew(0, null, holder, null, creater );
			tmp = holder.mValue;
			val.set(LayerType.ltPsAdditive);
			tmp.propSet(Interface.MEMBERENSURE, TYPE, val, null);
			val.set(tmp,tmp);
			dic.propSet(Interface.MEMBERENSURE, "psadd", val, null);

			creater.createNew(0, null, holder, null, creater );
			tmp = holder.mValue;
			val.set(LayerType.ltPsSubtractive);
			tmp.propSet(Interface.MEMBERENSURE, TYPE, val, null);
			val.set(tmp,tmp);
			dic.propSet(Interface.MEMBERENSURE, "pssub", val, null);

			creater.createNew(0, null, holder, null, creater );
			tmp = holder.mValue;
			val.set(LayerType.ltPsMultiplicative);
			tmp.propSet(Interface.MEMBERENSURE, TYPE, val, null);
			val.set(tmp,tmp);
			dic.propSet(Interface.MEMBERENSURE, "psmul", val, null);

			creater.createNew(0, null, holder, null, creater );
			tmp = holder.mValue;
			val.set(LayerType.ltPsScreen);
			tmp.propSet(Interface.MEMBERENSURE, TYPE, val, null);
			val.set(tmp,tmp);
			dic.propSet(Interface.MEMBERENSURE, "psscreen", val, null);

			creater.createNew(0, null, holder, null, creater );
			tmp = holder.mValue;
			val.set(LayerType.ltPsOverlay);
			tmp.propSet(Interface.MEMBERENSURE, TYPE, val, null);
			val.set(tmp,tmp);
			dic.propSet(Interface.MEMBERENSURE, "psoverlay", val, null);

			creater.createNew(0, null, holder, null, creater );
			tmp = holder.mValue;
			val.set(LayerType.ltPsHardLight);
			tmp.propSet(Interface.MEMBERENSURE, TYPE, val, null);
			val.set(tmp,tmp);
			dic.propSet(Interface.MEMBERENSURE, "pshlight", val, null);

			creater.createNew(0, null, holder, null, creater );
			tmp = holder.mValue;
			val.set(LayerType.ltPsSoftLight);
			tmp.propSet(Interface.MEMBERENSURE, TYPE, val, null);
			val.set(tmp,tmp);
			dic.propSet(Interface.MEMBERENSURE, "psslight", val, null);

			creater.createNew(0, null, holder, null, creater );
			tmp = holder.mValue;
			val.set(LayerType.ltPsColorDodge);
			tmp.propSet(Interface.MEMBERENSURE, TYPE, val, null);
			val.set(tmp,tmp);
			dic.propSet(Interface.MEMBERENSURE, "psdodge", val, null);

			creater.createNew(0, null, holder, null, creater );
			tmp = holder.mValue;
			val.set(LayerType.ltPsColorDodge5);
			tmp.propSet(Interface.MEMBERENSURE, TYPE, val, null);
			val.set(tmp,tmp);
			dic.propSet(Interface.MEMBERENSURE, "psdodge5", val, null);

			creater.createNew(0, null, holder, null, creater );
			tmp = holder.mValue;
			val.set(LayerType.ltPsColorBurn);
			tmp.propSet(Interface.MEMBERENSURE, TYPE, val, null);
			val.set(tmp,tmp);
			dic.propSet(Interface.MEMBERENSURE, "psburn", val, null);

			creater.createNew(0, null, holder, null, creater );
			tmp = holder.mValue;
			val.set(LayerType.ltPsLighten);
			tmp.propSet(Interface.MEMBERENSURE, TYPE, val, null);
			val.set(tmp,tmp);
			dic.propSet(Interface.MEMBERENSURE, "pslighten", val, null);

			creater.createNew(0, null, holder, null, creater );
			tmp = holder.mValue;
			val.set(LayerType.ltPsDarken);
			tmp.propSet(Interface.MEMBERENSURE, TYPE, val, null);
			val.set(tmp,tmp);
			dic.propSet(Interface.MEMBERENSURE, "psdarken", val, null);

			creater.createNew(0, null, holder, null, creater );
			tmp = holder.mValue;
			val.set(LayerType.ltPsDifference);
			tmp.propSet(Interface.MEMBERENSURE, TYPE, val, null);
			val.set(tmp,tmp);
			dic.propSet(Interface.MEMBERENSURE, "psdiff", val, null);

			creater.createNew(0, null, holder, null, creater );
			tmp = holder.mValue;
			val.set(LayerType.ltPsDifference5);
			tmp.propSet(Interface.MEMBERENSURE, TYPE, val, null);
			val.set(tmp,tmp);
			dic.propSet(Interface.MEMBERENSURE, "psdiff5", val, null);

			creater.createNew(0, null, holder, null, creater );
			tmp = holder.mValue;
			val.set(LayerType.ltPsExclusion);
			tmp.propSet(Interface.MEMBERENSURE, TYPE, val, null);
			val.set(tmp,tmp);
			dic.propSet(Interface.MEMBERENSURE, "psexcl", val, null);

			val.set(dic,dic);
			global.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "imageTagLayerType", val, global );
		}
	}
}
