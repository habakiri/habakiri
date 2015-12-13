/**
 ******************************************************************************
 * Copyright (c), Takenori Imoto
 * �� software http://www.kaede-software.com/
 * All rights reserved.
 ******************************************************************************
 * �\�[�X�R�[�h�`�����o�C�i���`�����A�ύX���邩���Ȃ������킸�A�ȉ��̏�����
 * �����ꍇ�Ɍ���A�ĔЕz����юg�p��������܂��B
 *
 * �E�\�[�X�R�[�h���ĔЕz����ꍇ�A��L�̒��쌠�\���A�{�����ꗗ�A����щ��L�Ɛ�
 *   �������܂߂邱�ƁB
 * �E�o�C�i���`���ōĔЕz����ꍇ�A�Еz���ɕt���̃h�L�������g���̎����ɁA��L��
 *   ���쌠�\���A�{�����ꗗ�A����щ��L�Ɛӏ������܂߂邱�ƁB
 * �E���ʂɂ����ʂ̋��Ȃ��ɁA�{�\�t�g�E�F�A����h���������i�̐�`�܂��͔̔�
 *   ���i�ɁA�g�D�̖��O�܂��̓R���g���r���[�^�[�̖��O���g�p���Ă͂Ȃ�Ȃ��B
 *
 * �{�\�t�g�E�F�A�́A���쌠�҂���уR���g���r���[�^�[�ɂ���āu����̂܂܁v��
 * ����Ă���A�����َ����킸�A���ƓI�Ȏg�p�\���A����ѓ���̖ړI�ɑ΂���K
 * �����Ɋւ���Öق̕ۏ؂��܂߁A�܂�����Ɍ��肳��Ȃ��A�����Ȃ�ۏ؂�����܂�
 * ��B���쌠�҂��R���g���r���[�^�[���A���R�̂�������킸�A���Q�����̌�������
 * ����킸�A���ӔC�̍������_��ł��邩���i�ӔC�ł��邩�i�ߎ����̑��́j�s�@
 * �s�ׂł��邩���킸�A���ɂ��̂悤�ȑ��Q����������\����m�炳��Ă����Ƃ�
 * �Ă��A�{�\�t�g�E�F�A�̎g�p�ɂ���Ĕ��������i��֕i�܂��͑�p�T�[�r�X�̒��B�A
 * �g�p�̑r���A�f�[�^�̑r���A���v�̑r���A�Ɩ��̒��f���܂߁A�܂�����Ɍ��肳���
 * ���j���ڑ��Q�A�Ԑڑ��Q�A�����I�ȑ��Q�A���ʑ��Q�A�����I���Q�A�܂��͌��ʑ��Q��
 * ���āA��ؐӔC�𕉂�Ȃ����̂Ƃ��܂��B
 ******************************************************************************
 * �{�\�t�g�E�F�A�́A�g���g��2 ( http://kikyou.info/tvp/ ) �̃\�[�X�R�[�h��Java
 * �ɏ������������̂��ꕔ�g�p���Ă��܂��B
 * �g���g��2 Copyright (C) W.Dee <dee@kikyou.info> and contributors
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
