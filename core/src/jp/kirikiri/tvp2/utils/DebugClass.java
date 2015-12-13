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
package jp.kirikiri.tvp2.utils;

import jp.kirikiri.tjs2.Error;
import jp.kirikiri.tjs2.Interface;
import jp.kirikiri.tjs2.Dispatch2;
import jp.kirikiri.tjs2.NativeClass;
import jp.kirikiri.tjs2.NativeClassConstructor;
import jp.kirikiri.tjs2.NativeClassMethod;
import jp.kirikiri.tjs2.NativeClassProperty;
import jp.kirikiri.tjs2.NativeInstance;
import jp.kirikiri.tjs2.TJS;
import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tjs2.Variant;
import jp.kirikiri.tjs2.VariantException;
import jp.kirikiri.tvp2.TVP;
import jp.kirikiri.tvp2.msg.Message;

public class DebugClass extends NativeClass {

	static int mClassID = -1;
	static private final String CLASS_NAME = "Debug";

	public static void addLog( String mes ) {
		TVP.DebugLog.addLog( mes );
	}
	public static void addImportantLog( String mes ) {
		TVP.DebugLog.addImportantLog( mes );
	}

	protected NativeInstance createNativeInstance() {
		return null;
	}

	public DebugClass() throws VariantException, TJSException {
		super( CLASS_NAME );
		final int NCM_CLASSID = TJS.registerNativeClass(CLASS_NAME);
		setClassID( NCM_CLASSID );
		mClassID = NCM_CLASSID;

		// constructor
		registerNCM( CLASS_NAME, TVP.ReturnOKConstructor, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "finalize", TVP.ReturnOKMethod, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "message", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				if( param.length < 1 ) return Error.E_BADPARAMCOUNT;
				if( param.length == 1 ) {
					addLog( param[0].asString() );
				} else {
					// display the arguments separated with ", "
					StringBuilder builder = new StringBuilder(512);
					for( int i = 0; i < param.length; i++ ) {
						if( i != 0 ) builder.append(", ");
						builder.append( param[i].asString() );
					}
					addLog( builder.toString() );
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "notice", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				if( param.length < 1 ) return Error.E_BADPARAMCOUNT;
				if( param.length == 1 ) {
					addImportantLog( param[0].asString() );
				} else {
					// display the arguments separated with ", "
					StringBuilder builder = new StringBuilder(512);
					for( int i = 0; i < param.length; i++ ) {
						if( i != 0 ) builder.append(", ");
						builder.append( param[i].asString() );
					}
					addImportantLog( builder.toString() );
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "startLogToFile", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				boolean clear = false;
				if( param.length >= 1) clear = param[0].asBoolean();
				TVP.DebugLog.startLogToFile(clear);
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "logAsError", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				TVP.DebugLog.onError();
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		// property
		registerNCM( "logLocation", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				result.set( TVP.DebugLog.getLogLocation() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException, TJSException {
				TVP.DebugLog.setLogLocation( param.asString() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, Interface.STATICMEMBER );

		registerNCM( "logToFileOnError", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				result.set( TVP.DebugLog.getAutoLogToFileOnError() ? 1 : 0 );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) {
				TVP.DebugLog.setAutoLogToFileOnError( param.asBoolean() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, Interface.STATICMEMBER );

		registerNCM( "clearLogFileOnError", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				result.set( TVP.DebugLog.getAutoClearLogOnError() ? 1 : 0 );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) {
				TVP.DebugLog.setAutoClearLogOnError( param.asBoolean() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, Interface.STATICMEMBER );

		registerNCM( "controller", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				Dispatch2 dsp = TVP.getControllerClass();
				result.set( dsp, dsp );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, Interface.STATICMEMBER );

		registerNCM( "console", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				Dispatch2 dsp = TVP.getConsoleClass();
				result.set( dsp, dsp );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, Interface.STATICMEMBER );

		// put version information to DMS
		addImportantLog( TVP.Application.get().getVersionInformation() );
		addImportantLog( Message.VersionInformation2  );
	}

}
