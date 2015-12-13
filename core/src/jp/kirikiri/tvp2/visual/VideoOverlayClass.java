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

public class VideoOverlayClass extends NativeClass {

	static public final int vomOverlay=0, vomLayer=1, vomMixer=2;

	static public int ClassID = -1;
	static private final String CLASS_NAME = "VideoOverlay";

	protected NativeInstance createNativeInstance() {
		return new VideoOverlayNI();
	}

	public VideoOverlayClass() throws VariantException, TJSException {
		super( CLASS_NAME );
		final int NCM_CLASSID = TJS.registerNativeClass(CLASS_NAME);
		setClassID( NCM_CLASSID );
		ClassID = NCM_CLASSID;

		// constructor
		registerNCM( CLASS_NAME, new NativeClassConstructor() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				VideoOverlayNI _this = (VideoOverlayNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				int hr = _this.construct(param, objthis);
				if( hr < 0 ) return hr;
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );


		registerNCM( "finalize", TVP.ReturnOKMethod, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "open", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				VideoOverlayNI _this = (VideoOverlayNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if( param.length < 1) return Error.E_BADPARAMCOUNT;
				_this.open( param[0].asString() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "play", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				VideoOverlayNI _this = (VideoOverlayNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.play();
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "stop", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				VideoOverlayNI _this = (VideoOverlayNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.stop();
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "close", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				VideoOverlayNI _this = (VideoOverlayNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.close();
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "setPos", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				VideoOverlayNI _this = (VideoOverlayNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if( param.length < 2) return Error.E_BADPARAMCOUNT;
				_this.setPosition( param[0].asInteger(), param[1].asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "setSize", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				VideoOverlayNI _this = (VideoOverlayNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if( param.length < 2) return Error.E_BADPARAMCOUNT;
				_this.setSize( param[0].asInteger(), param[1].asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "setBounds", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				VideoOverlayNI _this = (VideoOverlayNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if( param.length < 4) return Error.E_BADPARAMCOUNT;
				int l = param[0].asInteger();
				int t = param[1].asInteger();
				int r = l + param[2].asInteger();
				int b = t + param[3].asInteger();
				_this.setBounds( new Rect(l,t,r,b) );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "pause", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				VideoOverlayNI _this = (VideoOverlayNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.pause();
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "rewind", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				VideoOverlayNI _this = (VideoOverlayNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.rewind();
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "prepare", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				VideoOverlayNI _this = (VideoOverlayNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.prepare();
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "setSegmentLoop", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				VideoOverlayNI _this = (VideoOverlayNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if( param.length < 2) return Error.E_BADPARAMCOUNT;
				_this.setSegmentLoop( param[0].asInteger(), param[1].asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "cancelSegmentLoop", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				VideoOverlayNI _this = (VideoOverlayNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.cancelSegmentLoop();
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "setPeriodEvent", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				VideoOverlayNI _this = (VideoOverlayNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if( param.length < 1)
					_this.setPeriodEvent( -1 );
				else if( param.length < 2 )
					_this.setPeriodEvent( param[0].asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "cancelPeriodEvent", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				VideoOverlayNI _this = (VideoOverlayNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setPeriodEvent( -1 );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "selectAudioStream", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				VideoOverlayNI _this = (VideoOverlayNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if( param.length < 1) return Error.E_BADPARAMCOUNT;
				_this.selectAudioStream( param[0].asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "setMixingLayer", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				return Error.E_NOTIMPL;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "resetMixingLayer", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				return Error.E_NOTIMPL;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );


		// event
		registerNCM( "onStatusChanged", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				VideoOverlayNI _this = (VideoOverlayNI)objthis.getNativeInstance(ClassID);
				VariantClosure obj = _this.getActionOwner();
				if( obj.mObject != null ) {
					if( param.length < 1 ) return Error.E_BADPARAMCOUNT;

					Dispatch2 evobj = EventManager.createEventObject( "onStatusChanged", objthis, objthis );
					Variant evval = new Variant(evobj, evobj);
					// set member
					evobj.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "status", param[0], evobj );

					Variant[] pevval = {evval};
					obj.funcCall( 0, EventManager.ActionName, result, pevval, null );
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "onCallbackCommand", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				VideoOverlayNI _this = (VideoOverlayNI)objthis.getNativeInstance(ClassID);
				VariantClosure obj = _this.getActionOwner();
				if( obj.mObject != null ) {
					if( param.length < 1 ) return Error.E_BADPARAMCOUNT;

					Dispatch2 evobj = EventManager.createEventObject( "onCallbackCommand", objthis, objthis );
					Variant evval = new Variant(evobj, evobj);
					// set member
					evobj.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "command", param[0], evobj );
					evobj.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "arg", param[1], evobj );

					Variant[] pevval = {evval};
					obj.funcCall( 0, EventManager.ActionName, result, pevval, null );
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "onPeriod", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				VideoOverlayNI _this = (VideoOverlayNI)objthis.getNativeInstance(ClassID);
				VariantClosure obj = _this.getActionOwner();
				if( obj.mObject != null ) {
					if( param.length < 1 ) return Error.E_BADPARAMCOUNT;

					Dispatch2 evobj = EventManager.createEventObject( "onPeriod", objthis, objthis );
					Variant evval = new Variant(evobj, evobj);
					// set member
					evobj.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "reason", param[0], evobj );

					Variant[] pevval = {evval};
					obj.funcCall( 0, EventManager.ActionName, result, pevval, null );
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "onFrameUpdate", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				VideoOverlayNI _this = (VideoOverlayNI)objthis.getNativeInstance(ClassID);
				VariantClosure obj = _this.getActionOwner();
				if( obj.mObject != null ) {
					if( param.length < 1 ) return Error.E_BADPARAMCOUNT;

					Dispatch2 evobj = EventManager.createEventObject( "onFrameUpdate", objthis, objthis );
					Variant evval = new Variant(evobj, evobj);
					// set member
					evobj.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "frame", param[0], evobj );

					Variant[] pevval = {evval};
					obj.funcCall( 0, EventManager.ActionName, result, pevval, null );
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );


		registerNCM( "position", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				VideoOverlayNI _this = (VideoOverlayNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( (int)_this.getTimePosition() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				VideoOverlayNI _this = (VideoOverlayNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setTimePosition( param.asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "left", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				VideoOverlayNI _this = (VideoOverlayNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getLeft() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				VideoOverlayNI _this = (VideoOverlayNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setLeft(param.asInteger());
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "top", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				VideoOverlayNI _this = (VideoOverlayNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getTop() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				VideoOverlayNI _this = (VideoOverlayNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setTop(param.asInteger());
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "width", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				VideoOverlayNI _this = (VideoOverlayNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getWidth() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				VideoOverlayNI _this = (VideoOverlayNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setWidth(param.asInteger());
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "height", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				VideoOverlayNI _this = (VideoOverlayNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getHeight() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				VideoOverlayNI _this = (VideoOverlayNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setHeight(param.asInteger());
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "originalWidth", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				VideoOverlayNI _this = (VideoOverlayNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getOriginalWidth() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "originalHeight", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				VideoOverlayNI _this = (VideoOverlayNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getOriginalHeight() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "visible", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				VideoOverlayNI _this = (VideoOverlayNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getVisible() ? 1 : 0 );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				VideoOverlayNI _this = (VideoOverlayNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setVisible(param.asBoolean());
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "loop", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				VideoOverlayNI _this = (VideoOverlayNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getLoop() ? 1 : 0 );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				VideoOverlayNI _this = (VideoOverlayNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setLoop(param.asBoolean());
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "frame", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				VideoOverlayNI _this = (VideoOverlayNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getFrame() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				VideoOverlayNI _this = (VideoOverlayNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setFrame(param.asInteger());
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "fps", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				VideoOverlayNI _this = (VideoOverlayNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getFPS() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "numberOfFrame", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				VideoOverlayNI _this = (VideoOverlayNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getNumberOfFrame() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "totalTime", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				VideoOverlayNI _this = (VideoOverlayNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( (int)_this.getTotalTime() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "layer1", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) { result.set(0); return Error.S_OK; }
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.S_OK; }
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "layer2", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) { result.set(0); return Error.S_OK; }
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.S_OK; }
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "mode", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				VideoOverlayNI _this = (VideoOverlayNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getMode() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				VideoOverlayNI _this = (VideoOverlayNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setMode(param.asInteger());
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "playRate", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				VideoOverlayNI _this = (VideoOverlayNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getPlayRate() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				VideoOverlayNI _this = (VideoOverlayNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setPlayRate(param.asDouble());
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "segmentLoopStartFrame", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				VideoOverlayNI _this = (VideoOverlayNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getSegmentLoopEndFrame() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "segmentLoopEndFrame", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				VideoOverlayNI _this = (VideoOverlayNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getSegmentLoopStartFrame() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "periodEventFrame", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				VideoOverlayNI _this = (VideoOverlayNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getPeriodEventFrame() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				VideoOverlayNI _this = (VideoOverlayNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setPeriodEvent(param.asInteger());
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "audioBalance", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				VideoOverlayNI _this = (VideoOverlayNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getAudioBalance() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				VideoOverlayNI _this = (VideoOverlayNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setAudioBalance(param.asInteger());
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "audioVolume", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				VideoOverlayNI _this = (VideoOverlayNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getAudioVolume() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				VideoOverlayNI _this = (VideoOverlayNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setAudioVolume(param.asInteger());
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "numberOfAudioStream", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				VideoOverlayNI _this = (VideoOverlayNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getNumberOfAudioStream() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "enabledAudioStream", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				VideoOverlayNI _this = (VideoOverlayNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getEnabledAudioStream() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				VideoOverlayNI _this = (VideoOverlayNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.selectAudioStream(param.asInteger());
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "numberOfVideoStream", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				VideoOverlayNI _this = (VideoOverlayNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getNumberOfVideoStream() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "enabledVideoStream", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				VideoOverlayNI _this = (VideoOverlayNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getEnabledVideoStream() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				VideoOverlayNI _this = (VideoOverlayNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.selectVideoStream(param.asInteger());
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "mixingMovieAlpha", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				VideoOverlayNI _this = (VideoOverlayNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getMixingMovieAlpha() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				VideoOverlayNI _this = (VideoOverlayNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setMixingMovieAlpha(param.asDouble());
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "mixingMovieBGColor", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				VideoOverlayNI _this = (VideoOverlayNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getMixingMovieBGColor() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				VideoOverlayNI _this = (VideoOverlayNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setMixingMovieBGColor(param.asInteger());
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "contrastRangeMin", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "contrastRangeMax", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "contrastDefaultValue", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "contrastStepSize", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "contrast", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "brightnessRangeMin", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "brightnessRangeMax", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "brightnessDefaultValue", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "brightnessStepSize", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "brightness", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "hueRangeMin", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "hueRangeMax", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "hueDefaultValue", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "hueStepSize", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "hue", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "saturationRangeMin", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "saturationRangeMax", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "saturationDefaultValue", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "saturationStepSize", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "saturation", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, 0 );
	}

}
