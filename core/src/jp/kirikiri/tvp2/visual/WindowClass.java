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
package jp.kirikiri.tvp2.visual;

import java.util.ArrayList;

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
import jp.kirikiri.tjs2.VariantClosure;
import jp.kirikiri.tjs2.VariantException;
import jp.kirikiri.tvp2.TVP;
import jp.kirikiri.tvp2.base.EventManager;
import jp.kirikiri.tvp2.msg.Message;

public class WindowClass extends NativeClass {

	static public int ClassID = -1;
	static private final String CLASS_NAME = "Window";

	static final public int
		utNormal = 0, // only needed region
		utEntire = 1; // entire of window


	protected NativeInstance createNativeInstance() {
		return new WindowNI();
	}

	/*
	private static WindowNI getNativeInstance( Dispatch2 objthis ) {
		Holder<NativeInstance> holder = new Holder<NativeInstance>(null);
		int hr = objthis.nativeInstanceSupport( Interface.NIS_GETINSTANCE, ClassID, holder );
		if( hr < 0 ) return null;
		return (WindowNI) holder.mValue;
	}
	*/


	public WindowClass() throws VariantException, TJSException {
		super( CLASS_NAME );
		final int NCM_CLASSID = TJS.registerNativeClass(CLASS_NAME);
		setClassID( NCM_CLASSID );
		ClassID = NCM_CLASSID;

		// constructor
		registerNCM( CLASS_NAME, new NativeClassConstructor() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				int hr = _this.construct(param, objthis);
				if( hr < 0 ) return hr;
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );


		registerNCM( "finalize", TVP.ReturnOKMethod, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "close", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				_this.close();
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "beginMove", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				_this.beginMove();
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "bringToFront", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				_this.bringToFront();
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "update", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				int type = utNormal;
				if( param.length >= 1 && param[0].isVoid() != true )
					type = param[0].asInteger();
				_this.update(type);
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "showModal", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				_this.showModal();
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "setMaskRegion", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				int threshold = 1;
				if( param.length >= 1 && param[0].isVoid() != true )
					threshold = param[0].asInteger();
				_this.setMaskRegion(threshold);
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "removeMaskRegion", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				_this.removeMaskRegion();
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "add", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				if( param.length < 1 ) return Error.E_BADPARAMCOUNT;
				VariantClosure clo = param[0].asObjectClosure();
				_this.add(clo);
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "remove", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				if( param.length < 1 ) return Error.E_BADPARAMCOUNT;
				VariantClosure clo = param[0].asObjectClosure();
				_this.remove(clo);
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "setSize", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				if( param.length < 2 ) return Error.E_BADPARAMCOUNT;
				_this.setSize(param[0].asInteger(),param[1].asInteger());
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "setMinSize", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException, TJSException {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				if( param.length < 2 ) return Error.E_BADPARAMCOUNT;
				_this.setMinSize(param[0].asInteger(),param[1].asInteger());
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "setMaxSize", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException, TJSException {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				if( param.length < 2 ) return Error.E_BADPARAMCOUNT;
				_this.setMaxSize(param[0].asInteger(),param[1].asInteger());
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "setPos", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException, TJSException {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				if( param.length < 2 ) return Error.E_BADPARAMCOUNT;
				_this.setPosition(param[0].asInteger(),param[1].asInteger());
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "setLayerPos", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				if( param.length < 2 ) return Error.E_BADPARAMCOUNT;
				_this.setLayerPosition(param[0].asInteger(),param[1].asInteger());
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "setInnerSize", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException, TJSException {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				if( param.length < 2 ) return Error.E_BADPARAMCOUNT;
				_this.setInnerSize(param[0].asInteger(),param[1].asInteger());
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "setZoom", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				if( param.length < 2 ) return Error.E_BADPARAMCOUNT;
				_this.setZoom(param[0].asInteger(),param[1].asInteger());
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "hideMouseCursor", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				_this.hideMouseCursor();
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "postInputEvent", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException, TJSException {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				if( param.length < 1 ) return Error.E_BADPARAMCOUNT;
				String eventname = param[0].asString();
				Dispatch2 eventparams = null;
				if( param.length >= 2 )
					eventparams = param[1].asObject();
				_this.postInputEvent( eventname, eventparams );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "findFullScreenCandidates", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				if( param.length < 5) return Error.E_BADPARAMCOUNT;

				ArrayList<ScreenModeCandidate> candidates = new ArrayList<ScreenModeCandidate>();
				ScreenMode preferred = new ScreenMode();
				preferred.mWidth = param[0].asInteger();
				preferred.mHeight = param[1].asInteger();
				preferred.mBitsPerPixel = param[2].asInteger();
				int mode = param[3].asInteger();
				int zoom_mode = param[4].asInteger();

				WindowNI.makeFullScreenModeCandidates(preferred, mode, zoom_mode, candidates);

				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "registerMessageReceiver", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				if( param.length < 3 ) return Error.E_BADPARAMCOUNT;
				// TODO パラメータについては一考の余地あり
				_this.registerWindowMessageReceiver( param[0].asInteger(), param[1].asObject(), param[2].asObject() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "showMenu", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				_this.showMenu();
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		// event
		registerNCM( "onResize", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				return callEventNoParam( "onResize", result, objthis );
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "onMouseEnter", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				return callEventNoParam( "onMouseEnter", result, objthis );
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "onMouseLeave", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				return callEventNoParam( "onMouseLeave", result, objthis );
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "onClick", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				if( param.length < 2 ) return Error.E_BADPARAMCOUNT;
				int arg_count = 0;

				Dispatch2 evobj = EventManager.createEventObject( "onClick", objthis, objthis );
				Variant evval = new Variant(evobj, evobj);
				// set member
				evobj.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "x", param[arg_count++], evobj );
				evobj.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "y", param[arg_count++], evobj );

				Variant[] pevval = new Variant[1];
				pevval[0] = evval;
				VariantClosure clo = new VariantClosure(objthis, objthis);
				clo.funcCall( 0, EventManager.ActionName, result, pevval, null );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "onDoubleClick", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException, TJSException {
				if( param.length < 2 ) return Error.E_BADPARAMCOUNT;
				int arg_count = 0;

				Dispatch2 evobj = EventManager.createEventObject( "onDoubleClick", objthis, objthis );
				Variant evval = new Variant(evobj, evobj);
				// set member
				evobj.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "x", param[arg_count++], evobj );
				evobj.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "y", param[arg_count++], evobj );

				Variant[] pevval = new Variant[1];
				pevval[0] = evval;
				VariantClosure clo = new VariantClosure(objthis, objthis);
				clo.funcCall( 0, EventManager.ActionName, result, pevval, null );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "onMouseDown", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException, TJSException {
				if( param.length < 4 ) return Error.E_BADPARAMCOUNT;
				int arg_count = 0;

				Dispatch2 evobj = EventManager.createEventObject( "onMouseDown", objthis, objthis );
				Variant evval = new Variant(evobj, evobj);
				// set member
				evobj.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "x", param[arg_count++], evobj );
				evobj.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "y", param[arg_count++], evobj );
				evobj.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "button", param[arg_count++], evobj );
				evobj.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "shift", param[arg_count++], evobj );

				Variant[] pevval = new Variant[1];
				pevval[0] = evval;
				VariantClosure clo = new VariantClosure(objthis, objthis);
				clo.funcCall( 0, EventManager.ActionName, result, pevval, null );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "onMouseUp", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException, TJSException {
				if( param.length < 4 ) return Error.E_BADPARAMCOUNT;
				int arg_count = 0;

				Dispatch2 evobj = EventManager.createEventObject( "onMouseUp", objthis, objthis );
				Variant evval = new Variant(evobj, evobj);
				// set member
				evobj.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "x", param[arg_count++], evobj );
				evobj.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "y", param[arg_count++], evobj );
				evobj.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "button", param[arg_count++], evobj );
				evobj.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "shift", param[arg_count++], evobj );

				Variant[] pevval = new Variant[1];
				pevval[0] = evval;
				VariantClosure clo = new VariantClosure(objthis, objthis);
				clo.funcCall( 0, EventManager.ActionName, result, pevval, null );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "onMouseMove", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException, TJSException {
				if( param.length < 3 ) return Error.E_BADPARAMCOUNT;
				int arg_count = 0;

				Dispatch2 evobj = EventManager.createEventObject( "onMouseMove", objthis, objthis );
				Variant evval = new Variant(evobj, evobj);
				// set member
				evobj.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "x", param[arg_count++], evobj );
				evobj.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "y", param[arg_count++], evobj );
				evobj.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "button", param[arg_count++], evobj );

				Variant[] pevval = new Variant[1];
				pevval[0] = evval;
				VariantClosure clo = new VariantClosure(objthis, objthis);
				clo.funcCall( 0, EventManager.ActionName, result, pevval, null );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "onMouseWheel", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException, TJSException {
				if( param.length < 4 ) return Error.E_BADPARAMCOUNT;
				int arg_count = 0;

				Dispatch2 evobj = EventManager.createEventObject( "onMouseWheel", objthis, objthis );
				Variant evval = new Variant(evobj, evobj);
				// set member
				evobj.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "shift", param[arg_count++], evobj );
				evobj.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "delta", param[arg_count++], evobj );
				evobj.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "x", param[arg_count++], evobj );
				evobj.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "y", param[arg_count++], evobj );

				Variant[] pevval = new Variant[1];
				pevval[0] = evval;
				VariantClosure clo = new VariantClosure(objthis, objthis);
				clo.funcCall( 0, EventManager.ActionName, result, pevval, null );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "onKeyDown", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException, TJSException {
				if( param.length < 2 ) return Error.E_BADPARAMCOUNT;
				int arg_count = 0;

				Dispatch2 evobj = EventManager.createEventObject( "onKeyDown", objthis, objthis );
				Variant evval = new Variant(evobj, evobj);
				// set member
				evobj.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "key", param[arg_count++], evobj );
				evobj.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "shift", param[arg_count++], evobj );

				Variant[] pevval = new Variant[1];
				pevval[0] = evval;
				VariantClosure clo = new VariantClosure(objthis, objthis);
				clo.funcCall( 0, EventManager.ActionName, result, pevval, null );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "onKeyUp", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException, TJSException {
				if( param.length < 2 ) return Error.E_BADPARAMCOUNT;
				int arg_count = 0;

				Dispatch2 evobj = EventManager.createEventObject( "onKeyUp", objthis, objthis );
				Variant evval = new Variant(evobj, evobj);
				// set member
				evobj.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "key", param[arg_count++], evobj );
				evobj.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "shift", param[arg_count++], evobj );

				Variant[] pevval = new Variant[1];
				pevval[0] = evval;
				VariantClosure clo = new VariantClosure(objthis, objthis);
				clo.funcCall( 0, EventManager.ActionName, result, pevval, null );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "onKeyPress", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException, TJSException {
				if( param.length < 1 ) return Error.E_BADPARAMCOUNT;
				int arg_count = 0;

				Dispatch2 evobj = EventManager.createEventObject( "onKeyPress", objthis, objthis );
				Variant evval = new Variant(evobj, evobj);
				// set member
				evobj.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "key", param[arg_count++], evobj );

				Variant[] pevval = new Variant[1];
				pevval[0] = evval;
				VariantClosure clo = new VariantClosure(objthis, objthis);
				clo.funcCall( 0, EventManager.ActionName, result, pevval, null );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "onFileDrop", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException, TJSException {
				if( param.length < 1 ) return Error.E_BADPARAMCOUNT;

				Dispatch2 evobj = EventManager.createEventObject( "onFileDrop", objthis, objthis );
				Variant evval = new Variant(evobj, evobj);

				// set member
				evobj.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "files", param[0], evobj );

				Variant[] pevval = { evval };
				VariantClosure clo = new VariantClosure(objthis, objthis);
				clo.funcCall( 0, EventManager.ActionName, result, pevval, null );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "onCloseQuery", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException, TJSException {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				_this.onCloseQueryCalled( param[0].asBoolean() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "onPopupHide", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				return callEventNoParam( "onPopupHide", result, objthis );
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "onActivate", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				return callEventNoParam( "onActivate", result, objthis );
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "onDeactivate", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				return callEventNoParam( "onDeactivate", result, objthis );
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "onOrientationChanged", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				return callEventNoParam( "onOrientationChanged", result, objthis );
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );



		registerNCM( "visible", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				result.set( _this.getVisible() ? 1 : 0 );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws TJSException {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				_this.setVisible( param.asBoolean() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "caption", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				result.set( _this.getCaption() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				_this.setCaption( param.asString() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "width", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				result.set( _this.getWidth() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException, TJSException {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				_this.setWidth( param.asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "height", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				result.set( _this.getHeight() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException, TJSException {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				_this.setHeight( param.asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "minWidth", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				result.set( _this.getMinWidth() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException, TJSException {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				_this.setMinWidth( param.asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "minHeight", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				result.set( _this.getMinHeight() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException, TJSException {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				_this.setMinHeight( param.asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "maxWidth", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				result.set( _this.getMaxWidth() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException, TJSException {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				_this.setMaxWidth( param.asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "maxHeight", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				result.set( _this.getMaxHeight() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException, TJSException {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				_this.setMaxHeight( param.asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "left", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				result.set( _this.getLeft() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException, TJSException {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				_this.setLeft( param.asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "top", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				result.set( _this.getTop() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException, TJSException {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				_this.setTop( param.asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "focusable", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				result.set( _this.getFocusable() ? 1 : 0 );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException, TJSException {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				_this.setFocusable( param.asBoolean() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "layerLeft", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				result.set( _this.getLayerLeft() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException, TJSException {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				_this.setLayerLeft( param.asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "layerTop", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				result.set( _this.getLayerTop() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException, TJSException {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				_this.setLayerTop( param.asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "innerSunken", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				result.set( _this.getInnerSunken() ? 1 : 0 );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException, TJSException {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				_this.setInnerSunken( param.asBoolean() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "innerWidth", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				result.set( _this.getInnerWidth() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException, TJSException {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				_this.setInnerWidth( param.asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "innerHeight", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				result.set( _this.getInnerHeight() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException, TJSException {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				_this.setInnerHeight( param.asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "zoomNumer", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				result.set( _this.getZoomNumer() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException, TJSException {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				_this.setZoomNumer( param.asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "zoomDenom", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				result.set( _this.getZoomDenom() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException, TJSException {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				_this.setZoomDenom( param.asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "borderStyle", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				result.set( _this.getBorderStyle() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException, TJSException {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				_this.setBorderStyle( param.asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "stayOnTop", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				result.set( _this.getStayOnTop() ? 1 : 0 );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException, TJSException {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				_this.setStayOnTop( param.asBoolean() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "showScrollBars", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				result.set( _this.getShowScrollBars() ? 1 : 0 );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException, TJSException {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				_this.setShowScrollBars( param.asBoolean() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "useMouseKey", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				result.set( _this.getUseMouseKey() ? 1 : 0 );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException, TJSException {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				_this.setUseMouseKey( param.asBoolean() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "trapKey", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				result.set( _this.getTrapKey() ? 1 : 0 );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException, TJSException {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				_this.setTrapKey( param.asBoolean() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "imeMode", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				result.set( _this.getDefaultImeMode() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException, TJSException {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				_this.setDefaultImeMode( param.asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "mouseCursorState", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				result.set( _this.getMouseCursorState() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException, TJSException {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				_this.setMouseCursorState( param.asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "menu", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) throws TJSException {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				Dispatch2 dsp = _this.getMenuItemObject();
				result.set( dsp, dsp );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "fullScreen", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				result.set( _this.getFullScreen() ? 1 : 0 );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException, TJSException {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				_this.setFullScreen( param.asBoolean() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "mainWindow", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				if( TVP.MainWindow != null ) {
					Dispatch2 dsp = TVP.MainWindow.getOwner();
					result.set( dsp, dsp );
				} else {
					result.set( null, null );
				}
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "focusedLayer", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				LayerNI lay = _this.getFocusedLayer();
				if( lay != null && lay.getOwner() != null )
					result.set( lay.getOwner(), lay.getOwner() );
				else
					result.set( null, null );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException, TJSException {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				LayerNI to = null;
				if( param.isVoid() != true ) {
					VariantClosure clo = param.asObjectClosure();
					if( clo.mObject != null ) {
						to = (LayerNI)clo.mObject.getNativeInstance( LayerClass.ClassID );
						if( to == null )
							Message.throwExceptionMessage(Message.SpecifyLayer);
					}
				}
				_this.setFocusedLayer( to );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "primaryLayer", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) throws TJSException {
				WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);

				LayerNI pri = _this.getPrimaryLayer();
				if( pri == null )
					Message.throwExceptionMessage(Message.WindowHasNoLayer);

				if( pri.getOwner() != null )
					result.set( pri.getOwner(), pri.getOwner() );
				else
					result.set( null, null );

				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "HWND", new NativeClassProperty() {
			/* TODO HWND は無効 */
			@Override public int get(Variant result, Dispatch2 objthis) {
				result.set( -1 ); // Always Invalid handle value
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "drawDevice", new NativeClassProperty() {
			/* TODO drawDevice は使わない */
			@Override public int get(Variant result, Dispatch2 objthis) {
				// WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				// result.set( _this.getDrawDeviceObject() );
				result.set( TVP.getPassThroughDrawDeviceClass() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException, TJSException {
				// WindowNI _this = (WindowNI)objthis.getNativeInstance(ClassID);
				// _this.setDrawDeviceObject( param );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );
	}

	static int callEventNoParam( final String name, Variant result, Dispatch2 objthis ) throws VariantException, TJSException {
		Dispatch2 evobj = EventManager.createEventObject( name, objthis, objthis );
		Variant[] pevval = new Variant[1];
		pevval[0] = new Variant(evobj, evobj);
		VariantClosure clo = new VariantClosure(objthis, objthis);
		clo.funcCall( 0, EventManager.ActionName, result, pevval, null );
		return Error.S_OK;
	}
}
