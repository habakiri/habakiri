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
