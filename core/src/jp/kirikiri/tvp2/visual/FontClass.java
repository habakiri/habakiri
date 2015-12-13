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
import jp.kirikiri.tjs2.VariantException;
import jp.kirikiri.tvp2.TVP;

public class FontClass extends NativeClass {

	static int mClassID = -1;
	static private final String CLASS_NAME = "Font";

	protected NativeInstance createNativeInstance() {
		return new FontNI();
	}

	/*
	private static FontNI getNativeInstance( Dispatch2 objthis ) {
		Holder<NativeInstance> holder = new Holder<NativeInstance>(null);
		int hr = objthis.nativeInstanceSupport( Interface.NIS_GETINSTANCE, mClassID, holder );
		if( hr < 0 ) return null;
		return (FontNI) holder.mValue;
	}
	*/

	public FontClass() throws VariantException, TJSException {
		super( CLASS_NAME );
		final int NCM_CLASSID = TJS.registerNativeClass(CLASS_NAME);
		setClassID( NCM_CLASSID );
		mClassID = NCM_CLASSID;

		// constructor
		registerNCM( CLASS_NAME, new NativeClassConstructor() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				FontNI _this = (FontNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				int hr = _this.construct(param, objthis);
				if( hr < 0 ) return hr;
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );


		registerNCM( "finalize", TVP.ReturnOKMethod, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "getTextWidth", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				FontNI _this = (FontNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if(param.length < 1) return Error.E_BADPARAMCOUNT;
				if( result != null ) result.set( _this.getLayer().getTextWidth(param[0].asString()) );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "getTextHeight", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				FontNI _this = (FontNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if(param.length < 1) return Error.E_BADPARAMCOUNT;
				if( result != null ) result.set( _this.getLayer().getTextHeight(param[0].asString()) );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "getEscWidthX", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				FontNI _this = (FontNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if(param.length < 1) return Error.E_BADPARAMCOUNT;
				if( result != null ) result.set( _this.getLayer().getEscWidthX(param[0].asString()) );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "getEscWidthY", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				FontNI _this = (FontNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if(param.length < 1) return Error.E_BADPARAMCOUNT;
				if( result != null ) result.set( _this.getLayer().getEscWidthY(param[0].asString()) );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "getEscHeightX", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				FontNI _this = (FontNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if(param.length < 1) return Error.E_BADPARAMCOUNT;
				if( result != null ) result.set( _this.getLayer().getEscHeightX(param[0].asString()) );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "getEscHeightY", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				FontNI _this = (FontNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if(param.length < 1) return Error.E_BADPARAMCOUNT;
				if( result != null ) result.set( _this.getLayer().getEscHeightY(param[0].asString()) );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "doUserSelect", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				FontNI _this = (FontNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if(param.length < 4) return Error.E_BADPARAMCOUNT;
				int flags = param[0].asInteger();
				String caption = param[1].asString();
				String prompt = param[2].asString();
				String samplestring = param[3].asString();
				int ret = _this.getLayer().doUserFontSelect( flags, caption, prompt, samplestring ) ? 1 : 0;
				if( result != null ) result.set( ret );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "getList", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				FontNI _this = (FontNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if(param.length < 1) return Error.E_BADPARAMCOUNT;
				int flags = param[0].asInteger();
				ArrayList<String> list = new ArrayList<String>();
				_this.getLayer().getFontList(flags,list);
				if( result != null ) {
					Dispatch2 dsp = TJS.createArrayObject();
					result.set( dsp, dsp );
					final int count = list.size();
					Variant tmp = new Variant();
					for( int i = 0; i < count; i++) {
						tmp.set( list.get(i) );
						dsp.propSetByNum( Interface.MEMBERENSURE, i, tmp, dsp );
					}
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "mapPrerenderedFont", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				FontNI _this = (FontNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if(param.length < 1) return Error.E_BADPARAMCOUNT;
				_this.getLayer().mapPrerenderedFont(param[0].asString());
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "unmapPrerenderedFont", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				FontNI _this = (FontNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.getLayer().unmapPrerenderedFont();
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "loadFont", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				FontNI _this = (FontNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if(param.length < 1) return Error.E_BADPARAMCOUNT;
				String facename = null;
				if( param.length >= 2 && param[1].isVoid() != true ) facename = param[1].asString();
				_this.getLayer().loadFont(param[0].asString(),facename);
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );



		registerNCM( "face", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				FontNI _this = (FontNI)objthis.getNativeInstance(mClassID);
				result.set( _this.getLayer().getFontFace() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				FontNI _this = (FontNI)objthis.getNativeInstance(mClassID);
				_this.getLayer().setFontFace(param.asString());
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "height", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				FontNI _this = (FontNI)objthis.getNativeInstance(mClassID);
				result.set( _this.getLayer().getFontHeight() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				FontNI _this = (FontNI)objthis.getNativeInstance(mClassID);
				_this.getLayer().setFontHeight(param.asInteger());
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "bold", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				FontNI _this = (FontNI)objthis.getNativeInstance(mClassID);
				result.set( _this.getLayer().getFontBold() ? 1 : 0 );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) {
				FontNI _this = (FontNI)objthis.getNativeInstance(mClassID);
				_this.getLayer().setFontBold(param.asBoolean());
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "italic", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				FontNI _this = (FontNI)objthis.getNativeInstance(mClassID);
				result.set( _this.getLayer().getFontItalic() ? 1 : 0 );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) {
				FontNI _this = (FontNI)objthis.getNativeInstance(mClassID);
				_this.getLayer().setFontItalic(param.asBoolean());
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "strikeout", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				FontNI _this = (FontNI)objthis.getNativeInstance(mClassID);
				result.set( _this.getLayer().getFontStrikeout() ? 1 : 0 );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) {
				FontNI _this = (FontNI)objthis.getNativeInstance(mClassID);
				_this.getLayer().setFontStrikeout(param.asBoolean());
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "underline", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				FontNI _this = (FontNI)objthis.getNativeInstance(mClassID);
				result.set( _this.getLayer().getFontUnderline() ? 1 : 0 );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) {
				FontNI _this = (FontNI)objthis.getNativeInstance(mClassID);
				_this.getLayer().setFontUnderline(param.asBoolean());
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "angle", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				FontNI _this = (FontNI)objthis.getNativeInstance(mClassID);
				result.set( _this.getLayer().getFontAngle() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				FontNI _this = (FontNI)objthis.getNativeInstance(mClassID);
				_this.getLayer().setFontAngle(param.asInteger());
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );
	}

}
