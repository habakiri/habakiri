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
import jp.kirikiri.tvp2.TVP;
import jp.kirikiri.tvp2.base.EventManager;
import jp.kirikiri.tvp2env.TimerThread;

public class TimerClass extends NativeClass {

	static int mClassID = -1;
	static private final String CLASS_NAME = "Timer";

	protected NativeInstance createNativeInstance() {
		return new TimerNI();
	}

	/*
	private static TimerNI getNativeInstance( Dispatch2 objthis ) {
		return (TimerNI) objthis.getNativeInstance( mClassID );
	}
	*/

	public TimerClass() throws VariantException, TJSException {
		super( CLASS_NAME );
		final int NCM_CLASSID = TJS.registerNativeClass(CLASS_NAME);
		setClassID( NCM_CLASSID );
		mClassID = NCM_CLASSID;

		// constructor
		registerNCM( CLASS_NAME, new NativeClassConstructor() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException, TJSException {
				TimerNI _this = (TimerNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				int hr = _this.construct(param, objthis);
				if( hr < 0 ) return hr;
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "finalize", TVP.ReturnOKMethod, CLASS_NAME, Interface.nitMethod, 0 );

		// event
		registerNCM( "onTimer", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				TimerNI _this = (TimerNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				VariantClosure obj = _this.getActionOwner();
				if( obj.mObject != null ) {
					String actionname = _this.getActionName();
					if( actionname.length() == 0 ) actionname = null;
					Dispatch2 evobj = EventManager.createEventObject( "onTimer", objthis, objthis );
					Variant[] pevval = new Variant[1];
					pevval[0] = new Variant(evobj, evobj);
					obj.funcCall( 0, actionname, result, pevval, null );
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );


		registerNCM( "interval", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				TimerNI _this = (TimerNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				double interval = _this.getInterval() * (1.0 / (1<<TimerThread.SUBMILLI_FRAC_BITS));
				result.set( interval );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				TimerNI _this = (TimerNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				double interval = param.asDouble() * (1<<TimerThread.SUBMILLI_FRAC_BITS);
				_this.setInterval((long)(interval + 0.5));
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "enabled", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				TimerNI _this = (TimerNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getEnabled() ? 1 : 0 );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) {
				TimerNI _this = (TimerNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setEnabled( param.asBoolean() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "capacity", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				TimerNI _this = (TimerNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getCapacity() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				TimerNI _this = (TimerNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setCapacity( param.asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "mode", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				TimerNI _this = (TimerNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getCapacity() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				TimerNI _this = (TimerNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setCapacity( param.asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		// オプション見てタイマースレッドの優先度を変える
		String prop = TVP.Properties.getProperty("laxtimer","no");
		if( "yes".equals(prop) ) {
			TimerNI.LimitTimerCapacity = true;
		}
	}
}
