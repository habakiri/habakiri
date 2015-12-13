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
import jp.kirikiri.tjs2.Holder;
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
import jp.kirikiri.tvp2.msg.Message;

public class KAGParserClass extends NativeClass {

	static int mClassID = -1;
	static private final String CLASS_NAME = "KAGParser";

	protected NativeInstance createNativeInstance() throws VariantException, TJSException {
		return new KAGParserNI();
	}
	/*
	private static KAGParserNI getNativeInstance( Dispatch2 objthis ) {
		return (KAGParserNI) objthis.getNativeInstance( mClassID );
	}
	*/

	public KAGParserClass() throws VariantException, TJSException {
		super( CLASS_NAME );
		final int NCM_CLASSID = TJS.registerNativeClass(CLASS_NAME);
		setClassID( NCM_CLASSID );
		mClassID = NCM_CLASSID;

		// constructor
		registerNCM( CLASS_NAME, new NativeClassConstructor() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException, TJSException {
				KAGParserNI _this = (KAGParserNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				int hr = _this.construct(param, objthis);
				if( hr < 0 ) return hr;
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );


		registerNCM( "finalize", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) {
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "loadScenario", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				KAGParserNI _this = (KAGParserNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if( param.length < 1 ) return Error.E_BADPARAMCOUNT;
				_this.loadScenario(param[0].asString());
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "goToLabel", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				KAGParserNI _this = (KAGParserNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if( param.length < 1 ) return Error.E_BADPARAMCOUNT;
				_this.goToLabel(param[0].asString());
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "callLabel", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				KAGParserNI _this = (KAGParserNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if( param.length < 1 ) return Error.E_BADPARAMCOUNT;
				_this.callLabel(param[0].asString());
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "getNextTag", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				KAGParserNI _this = (KAGParserNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				Dispatch2 dsp = _this.getNextTag();
				if(dsp == null) {
					if(result!=null) result.clear(); // return void ( not null )
				} else {
					if(result!=null) result.set( dsp, dsp );
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "assign", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				KAGParserNI _this = (KAGParserNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if( param.length < 1 ) return Error.E_BADPARAMCOUNT;
				VariantClosure clo = param[0].asObjectClosure();
				if( clo.mObject!=null ) {
					KAGParserNI src = (KAGParserNI)clo.mObject.getNativeInstance( KAGParserClass.mClassID );
					if( src == null )
						Message.throwExceptionMessage(Message.KAGSpecifyKAGParser);
					_this.assign( src );
				} else {
					Message.throwExceptionMessage(Message.KAGSpecifyKAGParser);
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "clear", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				KAGParserNI _this = (KAGParserNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.clear();
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "store", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				KAGParserNI _this = (KAGParserNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				Dispatch2 dsp = _this.store();
				if(result!=null) result.set(dsp, dsp);
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "restore", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				KAGParserNI _this = (KAGParserNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if( param.length < 1 ) return Error.E_BADPARAMCOUNT;
				Dispatch2 dsp = param[0].asObject();
				_this.restore(dsp);
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "clearCallStack", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				KAGParserNI _this = (KAGParserNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.clearCallStack();
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "popMacroArgs", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				KAGParserNI _this = (KAGParserNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.popMacroArgs();
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "interrupt", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				KAGParserNI _this = (KAGParserNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.interrupt();
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "resetInterrupt", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				KAGParserNI _this = (KAGParserNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.resetInterrupt();
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );


		registerNCM( "curLine", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				KAGParserNI _this = (KAGParserNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getCurLine() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "curPos", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				KAGParserNI _this = (KAGParserNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getCurPos() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "curLineStr", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				KAGParserNI _this = (KAGParserNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getCurLineStr() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "processSpecialTags", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				KAGParserNI _this = (KAGParserNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getProcessSpecialTags() ? 1 : 0 );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) {
				KAGParserNI _this = (KAGParserNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setProcessSpecialTags(param.asBoolean());
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "ignoreCR", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				KAGParserNI _this = (KAGParserNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getIgnoreCR() ? 1 : 0 );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) {
				KAGParserNI _this = (KAGParserNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setIgnoreCR(param.asBoolean());
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "debugLevel", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				KAGParserNI _this = (KAGParserNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getDebugLevel() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				KAGParserNI _this = (KAGParserNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setDebugLevel(param.asInteger());
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "macros", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				KAGParserNI _this = (KAGParserNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				Dispatch2 macros = _this.getMacros();
				result.set(macros, macros);
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "macroParams", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				KAGParserNI _this = (KAGParserNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				Dispatch2 macros = _this.getMacroTop();
				result.set(macros, macros);
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "mp", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				KAGParserNI _this = (KAGParserNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				Dispatch2 macros = _this.getMacroTop();
				result.set(macros, macros);
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "callStackDepth", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				KAGParserNI _this = (KAGParserNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getCallStackDepth() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "curStorage", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				KAGParserNI _this = (KAGParserNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getStorageName() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws TJSException {
				KAGParserNI _this = (KAGParserNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.loadScenario(param.asString());
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "curLabel", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				KAGParserNI _this = (KAGParserNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getCurLabel() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, 0 );
	}

}
