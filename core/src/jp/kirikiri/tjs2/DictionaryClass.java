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
package jp.kirikiri.tjs2;

import java.util.ArrayList;

public class DictionaryClass extends NativeClass {

	static public int ClassID = -1;
	static private final String CLASS_NAME = "Dictionary";

	protected NativeInstance createNativeInstance() {
		return new DictionaryNI();
	}
	protected Dispatch2 createBaseTJSObject() {
		return new DictionaryObject();
	}

	public DictionaryClass() throws VariantException, TJSException {
		super(CLASS_NAME);
		final int NCM_CLASSID = TJS.registerNativeClass(CLASS_NAME);
		setClassID( NCM_CLASSID );
		ClassID = NCM_CLASSID;

		// constructor
		registerNCM( CLASS_NAME, new NativeClassConstructor() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) {
				DictionaryNI _this = (DictionaryNI)objthis.getNativeInstance( ClassID );
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				int hr = _this.construct( param, objthis );
				if( hr < 0 ) return hr;

				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "load", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) {
				DictionaryNI ni = (DictionaryNI)objthis.getNativeInstance(ClassID);
				if( ni == null ) return Error.E_NATIVECLASSCRASH;
				if( !ni.isValid() ) return Error.E_INVALIDOBJECT;
				// OribinalTODO: implement Dictionary.load()
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "save", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) {
				DictionaryNI ni = (DictionaryNI)objthis.getNativeInstance(ClassID);
				if( ni == null ) return Error.E_NATIVECLASSCRASH;
				if( !ni.isValid() ) return Error.E_INVALIDOBJECT;
				// OribinalTODO: implement Dictionary.save()
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "saveStruct", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				// Structured output for flie;
				// the content can be interpret as an expression to re-construct the object.
				DictionaryNI ni = (DictionaryNI)objthis.getNativeInstance(ClassID);
				if( ni == null ) return Error.E_NATIVECLASSCRASH;
				if( !ni.isValid() ) return Error.E_INVALIDOBJECT;
				if( param.length < 1) return Error.E_BADPARAMCOUNT;

				String name = param[0].asString();
				String mode = null;
				if( param.length >= 2 && param[1].isVoid() != true ) mode = param[1].asString();

				TextWriteStreamInterface stream = TJS.mStorage.createTextWriteStream(name, mode);
				try {
					ArrayList<Dispatch2> stack = new ArrayList<Dispatch2>();
					stack.add(objthis);
					ni.saveStructuredData( stack, stream, "" );
				} finally {
					stream.destruct();
				}
				if( result != null ) result.set( new Variant(objthis, objthis) );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "assign", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				DictionaryNI ni = (DictionaryNI)objthis.getNativeInstance(ClassID);
				if( ni == null ) return Error.E_NATIVECLASSCRASH;
				if( !ni.isValid() ) return Error.E_INVALIDOBJECT;
				if( param.length < 1) return Error.E_BADPARAMCOUNT;

				boolean clear = true;
				if( param.length >= 2 && param[1].isVoid() != true ) clear = param[1].asBoolean();

				VariantClosure clo = param[0].asObjectClosure();
				if( clo.mObjThis != null )
					ni.assign(clo.mObjThis, clear);
				else if( clo.mObject != null )
					ni.assign(clo.mObject, clear);
				else throw new TJSException(Error.NullAccess);
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "assignStruct", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				DictionaryNI ni = (DictionaryNI)objthis.getNativeInstance(ClassID);
				if( ni == null ) return Error.E_NATIVECLASSCRASH;
				if( !ni.isValid() ) return Error.E_INVALIDOBJECT;
				if( param.length < 1) return Error.E_BADPARAMCOUNT;

				ArrayList<Dispatch2> stack = new ArrayList<Dispatch2>();
				VariantClosure clo = param[0].asObjectClosure();
				if( clo.mObjThis != null )
					ni.assignStructure( clo.mObjThis, stack );
				else if( clo.mObject != null )
					ni.assignStructure(clo.mObject, stack);
				else throw new TJSException(Error.NullAccess);
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "clear", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) {
				DictionaryNI ni = (DictionaryNI)objthis.getNativeInstance(ClassID);
				if( ni == null ) return Error.E_NATIVECLASSCRASH;
				if( !ni.isValid() ) return Error.E_INVALIDOBJECT;
				ni.clear();
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );
	}

}
