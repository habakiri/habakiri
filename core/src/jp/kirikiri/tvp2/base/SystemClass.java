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

import java.util.ArrayList;

import jp.kirikiri.tjs2.Error;
import jp.kirikiri.tjs2.Interface;
import jp.kirikiri.tjs2.NativeClass;
import jp.kirikiri.tjs2.NativeClassConstructor;
import jp.kirikiri.tjs2.NativeClassMethod;
import jp.kirikiri.tjs2.NativeClassProperty;
import jp.kirikiri.tjs2.NativeInstance;
import jp.kirikiri.tjs2.TJS;
import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tjs2.Variant;
import jp.kirikiri.tjs2.VariantClosure;
import jp.kirikiri.tjs2.VariantException;
import jp.kirikiri.tjs2.Dispatch2;
import jp.kirikiri.tvp2.TVP;
import jp.kirikiri.tvp2.msg.Message;
import jp.kirikiri.tvp2.utils.Random;
import jp.kirikiri.tvp2.visual.GraphicsLoader;
import jp.kirikiri.tvp2.visual.LayerNI;
import jp.kirikiri.tvp2env.ApplicationSystem;

public class SystemClass extends NativeClass {
	/** Class ID */
	static public int mClassID = -1;
	/** クラス名 */
	static private final String CLASS_NAME = "System";

	protected NativeInstance createNativeInstance() throws TJSException {
		// this class cannot create an instance
		Message.throwExceptionMessage(Message.CannotCreateInstance);
		return null;
	}

	public SystemClass() throws VariantException, TJSException {
		super(CLASS_NAME);
		final int NCM_CLASSID = TJS.registerNativeClass(CLASS_NAME);
		setClassID( NCM_CLASSID );
		mClassID = NCM_CLASSID;


		// constructor
		registerNCM( CLASS_NAME, TVP.ReturnOKConstructor, CLASS_NAME, Interface.nitMethod, 0 );


		registerNCM( "finalize", TVP.ReturnOKMethod, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "terminate", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				int code = param.length > 0 ? param[0].asInteger() : 0;
				TVP.Application.get().terminateAsync(code);
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "exit", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				// this method does not return
				int code = param.length > 0 ? param[0].asInteger() : 0;
				TVP.Application.get().terminateSync(code);
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "inputString", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				if( param.length < 3) return Error.E_BADPARAMCOUNT;
				String value = param[2].asString();
				String b = TVP.Application.get().InputQuery( param[0].asString(), param[1].asString(), value);
				if( result!=null ) {
					if( b != null )
						result.set( b );
					else
						result.clear();
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "addContinuousHandler", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				// add function to continus handler list
				if( param.length < 1) return Error.E_BADPARAMCOUNT;
				VariantClosure clo = param[0].asObjectClosure();
				TVP.EventManager.addContinuousHandler(clo);
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "removeContinuousHandler", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				// remove function from continuous handler list
				if( param.length < 1) return Error.E_BADPARAMCOUNT;
				VariantClosure clo = param[0].asObjectClosure();
				TVP.EventManager.removeContinuousHandler(clo);
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "toActualColor", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				// convert color codes to 0xRRGGBB format.
				if( param.length < 1) return Error.E_BADPARAMCOUNT;
				if( result != null ) {
					int color = param[0].asInteger();
					color = LayerNI.toActualColor(color);
					result.set( color );
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "clearGraphicCache", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) {
				// clear graphic cache
				GraphicsLoader.clearGraphicCache();
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "touchImages", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException, TJSException {
				// try to cache graphics
				if( param.length < 1) return Error.E_BADPARAMCOUNT;

				ArrayList<String> storages = new ArrayList<String>();
				VariantClosure array = param[0].asObjectClosure();
				int count = 0;
				Variant val = new Variant();
				while(true) {
					int hr = array.mObject.propGetByNum(0, count, val, array.mObjThis);
					if( hr < 0 )
						break;
					if(val.isVoid() ) break;
					storages.add( val.asString() );
					count++;
				}
				int limit = 0;
				long timeout = 0;
				if( param.length >= 2 && param[1].isVoid() != true ) limit = param[1].asInteger();
				if( param.length >= 3 && param[2].isVoid() != true ) timeout = param[1].asInteger();
				GraphicsLoader.touchImages(storages, limit, timeout);
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "createUUID", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) {
				// create UUID
				// return UUID string in form of "43abda37-c597-4646-a279-c27a1373af90"

				byte[] uuid = new byte[16];

				Random.getRandomBits128(uuid);

				uuid[8] &= 0x3f;
				uuid[8] |= 0x80; // override clock_seq

				uuid[6] &= 0x0f;
				uuid[6] |= 0x40; // override version

				String buf = String.format("%02x%02x%02x%02x-%02x%02x-%02x%02x-%02x%02x-%02x%02x%02x%02x%02x%02x",
					uuid[ 0], uuid[ 1], uuid[ 2], uuid[ 3],
					uuid[ 4], uuid[ 5], uuid[ 6], uuid[ 7],
					uuid[ 8], uuid[ 9], uuid[10], uuid[11],
					uuid[12], uuid[13], uuid[14], uuid[15]);

				if( result != null ) result.set(buf);
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "assignMessage", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				// assign system message
				if( param.length < 2) return Error.E_BADPARAMCOUNT;

				String id = param[0].asString();
				String msg = param[1].asString();
				boolean res = TJS.assignMessage( id, msg );
				if(result!=null) result.set( res ? 1 : 0);

				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "doCompact", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				// compact memory usage
				int level = CompactEventCallbackInterface.COMPACT_LEVEL_MAX;
				if( param.length >= 1 && param[0].isVoid() != true )
					level = param[0].asInteger();
				TVP.EventManager.deliverCompactEvent(level);
				System.gc();
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "inform", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				// show simple message box
				if( param.length < 1) return Error.E_BADPARAMCOUNT;
				String text = param[0].asString();
				String caption;
				if( param.length >= 2 && param[1].isVoid() != true )
					caption = param[1].asString();
				else
					caption = "Information";

				TVP.Application.get().showSimpleMessageBox(text, caption);

				if(result!=null) result.clear();
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "getTickCount", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) {
				if( result != null ) {
					result.set( TVP.getTickCount() );
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "getKeyState", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				if( param.length < 1) return Error.E_BADPARAMCOUNT;
				int code = param[0].asInteger();
				// boolean getcurrent = true;
				// if( param.length >= 2) getcurrent = param[1].asBoolean();

				boolean res = TVP.isKeyPressing(code);

				if(result!=null) result.set( res ? 1 : 0 );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "shellExecute", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				if( param.length < 1) return Error.E_BADPARAMCOUNT;

				String target = param[0].asString();
				String execparam = null;
				if( param.length >= 2) execparam = param[1].asString();
				boolean res = ApplicationSystem.shellExecute(target, execparam);
				if(result!=null) result.set( res ? 1  : 0 );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "system", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				if( param.length < 1) return Error.E_BADPARAMCOUNT;

				String target = param[0].asString();
				boolean res = ApplicationSystem.shellExecute(target, null);
				TVP.EventManager.deliverCompactEvent(CompactEventCallbackInterface.COMPACT_LEVEL_MAX); // this should clear all caches
				if(result!=null) result.set( res ? 1 : 0 );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "readRegValue", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				if( param.length < 1) return Error.E_BADPARAMCOUNT;
				if( result == null ) return Error.S_OK;
				String key = param[0].asString();
				result.set( ApplicationSystem.readRegValue(key) );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "getArgument", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				if( param.length < 1) return Error.E_BADPARAMCOUNT;
				if(result==null) return Error.S_OK;

				String name = param[0].asString();
				String res = ApplicationSystem.getProperty( name );
				result.set( res );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "setArgument", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				if( param.length < 2) return Error.E_BADPARAMCOUNT;
				String name = param[0].asString();
				String value = param[1].asString();
				ApplicationSystem.setProperty( name, value );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "createAppLock", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				if( param.length < 1) return Error.E_BADPARAMCOUNT;
				if( result == null ) return Error.S_OK;

				String lockname = param[0].asString();
				boolean res = ApplicationSystem.createAppLock(lockname);
				if(result!=null) result.set( res ? 1 : 0 );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "nullpo", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) {
				// force make a null-po
				throw new NullPointerException();
			}
		}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		// TODO 追加
		registerNCM( "confirmYesNo", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				// show simple message box
				if( param.length < 1) return Error.E_BADPARAMCOUNT;
				String text = param[0].asString();
				String caption;
				if( param.length >= 2 && param[1].isVoid() != true )
					caption = param[1].asString();
				else
					caption = "Confirm";

				boolean ret = TVP.Application.get().showYesNoDialog(caption, text);

				if(result!=null) result.set(ret?1:0);
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );


		registerNCM( "versionString", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				result.set( TVP.Application.get().getVersionString() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, Interface.STATICMEMBER );

		registerNCM( "versionInformation", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				result.set( TVP.Application.get().getVersionInformation() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, Interface.STATICMEMBER );

		registerNCM( "eventDisabled", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				result.set( TVP.getSystemEventDisabledState() ? 1 : 0 );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) {
				TVP.setSystemEventDisabledState( param.asBoolean() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, Interface.STATICMEMBER );

		registerNCM( "graphicCacheLimit", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				result.set( TVP.getGraphicCacheLimit() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				TVP.setGraphicCacheLimit( param.asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, Interface.STATICMEMBER );

		registerNCM( "platformName", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				result.set( TVPSystem.getPlatformName() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, Interface.STATICMEMBER );

		registerNCM( "osName", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				result.set( TVPSystem.getOSName() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, Interface.STATICMEMBER );

		registerNCM( "exitOnWindowClose", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				result.set( TVP.TerminateOnWindowClose ? 1 : 0 );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) {
				TVP.TerminateOnWindowClose = param.asBoolean();
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, Interface.STATICMEMBER );

		registerNCM( "drawThreadNum", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				result.set( 0 ); // 常にauto扱い
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) {
				// 何が来ても無視
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, Interface.STATICMEMBER );

		registerNCM( "processorNum", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				result.set( TVPSystem.getProcessorNum() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, Interface.STATICMEMBER );

		registerNCM( "exitOnNoWindowStartup", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				result.set( TVP.TerminateOnNoWindowStartup ? 1 : 0);
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) {
				TVP.TerminateOnNoWindowStartup = param.asBoolean();
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, Interface.STATICMEMBER );

		registerNCM( "exePath", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) throws TJSException {
				result.set( ApplicationSystem.getAppPath() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, Interface.STATICMEMBER );

		registerNCM( "personalPath", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) throws TJSException {
				result.set( ApplicationSystem.getPersonalPath() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, Interface.STATICMEMBER );

		registerNCM( "appDataPath", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) throws TJSException {
				result.set( ApplicationSystem.getPersonalPath() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, Interface.STATICMEMBER );

		registerNCM( "dataPath", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				result.set( TVP.DataPath );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, Interface.STATICMEMBER );

		registerNCM( "exeName", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				result.set( TVP.MainForm.getExeName() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, Interface.STATICMEMBER );

		registerNCM( "title", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				result.set( TVP.getAppTitle() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				TVP.setAppTitle(param.asString());
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, Interface.STATICMEMBER );

		registerNCM( "screenWidth", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				result.set( TVP.MainForm.getScreenWidth() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, Interface.STATICMEMBER );

		registerNCM( "screenHeight", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				result.set( TVP.MainForm.getScreenHeight() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, Interface.STATICMEMBER );

		registerNCM( "desktopLeft", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				result.set( TVP.MainForm.getDesktopLeft() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, Interface.STATICMEMBER );

		registerNCM( "desktopTop", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				result.set( TVP.MainForm.getDesktopTop() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, Interface.STATICMEMBER );

		registerNCM( "desktopWidth", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				result.set( TVP.MainForm.getDesktopWidth() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, Interface.STATICMEMBER );

		registerNCM( "desktopHeight", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				result.set( TVP.MainForm.getDesktopHeight() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, Interface.STATICMEMBER );

		registerNCM( "stayOnTop", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				if( TVP.MainWindow != null ) {
					result.set( TVP.MainWindow.getStayOnTop() ? 1 : 0 );
				}
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) {
				if( TVP.MainWindow != null ) {
					TVP.MainWindow.setStayOnTop( param.asBoolean() );
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, Interface.STATICMEMBER );


		// register default "exceptionHandler" member
		Variant val = new Variant( null, null );
		propSet( Interface.MEMBERENSURE, "exceptionHandler", val, this);

		// and onActivate, onDeactivate
		propSet( Interface.MEMBERENSURE, "onActivate", val, this);
		propSet( Interface.MEMBERENSURE, "onDeactivate", val, this);
	}
}
