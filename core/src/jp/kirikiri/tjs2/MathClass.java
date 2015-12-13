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

import java.util.Random;

public class MathClass extends NativeClass {

	static public int mClassID;
	static private Random mRandomGenerator;
	static final private String CLASS_NAME = "Math";

	static public void initialize() {
		mRandomGenerator = null;
	}
	public static void finalizeApplication() {
		mRandomGenerator = null;
	}

	public MathClass() throws VariantException, TJSException {
		super(CLASS_NAME);

		if( mRandomGenerator == null ) {
			mRandomGenerator = new Random( System.currentTimeMillis() );
		}

		final int NCM_CLASSID = TJS.registerNativeClass(CLASS_NAME);
		setClassID( NCM_CLASSID );
		mClassID = NCM_CLASSID;

		// constructor
		registerNCM( CLASS_NAME, new NativeClassConstructor() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) {
				return Error.S_OK;
			}
			}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "abs", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				if( param.length < 1 ) return Error.E_BADPARAMCOUNT;
				if( result != null ) result.set( Math.abs( param[0].asDouble() ) );
				return Error.S_OK;
			}
			}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "acos", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				if( param.length < 1 ) return Error.E_BADPARAMCOUNT;
				if( result != null ) result.set( Math.acos( param[0].asDouble() ) );
				return Error.S_OK;
			}
			}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "asin", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				if( param.length < 1 ) return Error.E_BADPARAMCOUNT;
				if( result != null ) result.set( Math.asin( param[0].asDouble() ) );
				return Error.S_OK;
			}
			}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "atan", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				if( param.length < 1 ) return Error.E_BADPARAMCOUNT;
				if( result != null ) result.set( Math.atan( param[0].asDouble() ) );
				return Error.S_OK;
			}
			}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "atan2", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				if( param.length < 2 ) return Error.E_BADPARAMCOUNT;
				if( result != null ) result.set( Math.atan2( param[0].asDouble(), param[1].asDouble() ) );
				return Error.S_OK;
			}
			}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "ceil", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				if( param.length < 1 ) return Error.E_BADPARAMCOUNT;
				if( result != null ) result.set( Math.ceil( param[0].asDouble() ) );
				return Error.S_OK;
			}
			}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "exp", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
			if( param.length < 1 ) return Error.E_BADPARAMCOUNT;
			if( result != null ) result.set( Math.exp( param[0].asDouble() ) );
			return Error.S_OK;
		}
		}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "floor", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				if( param.length < 1 ) return Error.E_BADPARAMCOUNT;
				if( result != null ) result.set( Math.floor( param[0].asDouble() ) );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "log", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				if( param.length < 1 ) return Error.E_BADPARAMCOUNT;
				if( result != null ) result.set( Math.log( param[0].asDouble() ) );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "pow", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				if( param.length < 2 ) return Error.E_BADPARAMCOUNT;
				if( result != null ) result.set( Math.pow( param[0].asDouble(), param[1].asDouble() ) );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "max", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				if( result != null ) {
					double r = Double.NEGATIVE_INFINITY;
					final int count = param.length;
					for( int i = 0; i < count; i++ ) {
						double v = param[i].asDouble();
						if( Double.isNaN(v) ) {
							result.set( Double.NaN );
							return Error.S_OK;
						} else if( Double.compare( v, r ) > 0 ) {
							r = v;
						}
					}
					result.set( r );
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "min", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				if( result != null ) {
					double r = Double.POSITIVE_INFINITY;
					final int count = param.length;
					for( int i = 0; i < count; i++ ) {
						double v = param[i].asDouble();
						if( Double.isNaN(v) ) {
							result.set( Double.NaN );
							return Error.S_OK;
						} else if( Double.compare( v, r ) < 0 ) {
							r = v;
						}
					}
					result.set( r );
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "random", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				if( result != null ) result.set( mRandomGenerator.nextDouble() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "round", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				if( param.length < 1 ) return Error.E_BADPARAMCOUNT;
				if( result != null ) result.set( Math.round( param[0].asDouble() ) );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "sin", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				if( param.length < 1 ) return Error.E_BADPARAMCOUNT;
				if( result != null ) result.set( Math.sin( param[0].asDouble() ) );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "cos", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				if( param.length < 1 ) return Error.E_BADPARAMCOUNT;
				if( result != null ) result.set( Math.cos( param[0].asDouble() ) );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "sqrt", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				if( param.length < 1 ) return Error.E_BADPARAMCOUNT;
				if( result != null ) result.set( Math.sqrt( param[0].asDouble() ) );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "tan", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				if( param.length < 1 ) return Error.E_BADPARAMCOUNT;
				if( result != null ) result.set( Math.tan( param[0].asDouble() ) );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "E", new NativeClassProperty() {
			@Override
			public int get(Variant result, Dispatch2 objthis) {
				result.set( 2.7182818284590452354 );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, Interface.STATICMEMBER );

		registerNCM( "LOG2E", new NativeClassProperty() {
			@Override
			public int get(Variant result, Dispatch2 objthis) {
				result.set( 1.4426950408889634074 );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, Interface.STATICMEMBER );

		registerNCM( "LOG10E", new NativeClassProperty() {
			@Override
			public int get(Variant result, Dispatch2 objthis) {
				result.set( 0.4342944819032518276 );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, Interface.STATICMEMBER );

		registerNCM( "LN10", new NativeClassProperty() {
			@Override
			public int get(Variant result, Dispatch2 objthis) {
				result.set( 2.30258509299404568402 );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, Interface.STATICMEMBER );

		registerNCM( "LN2", new NativeClassProperty() {
			@Override
			public int get(Variant result, Dispatch2 objthis) {
				result.set( 0.69314718055994530942 );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, Interface.STATICMEMBER );

		registerNCM( "PI", new NativeClassProperty() {
			@Override
			public int get(Variant result, Dispatch2 objthis) {
				result.set( 3.14159265358979323846 );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, Interface.STATICMEMBER );

		registerNCM( "SQRT1_2", new NativeClassProperty() {
			@Override
			public int get(Variant result, Dispatch2 objthis) {
				result.set( 0.70710678118654752440 );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, Interface.STATICMEMBER );

		registerNCM( "SQRT2", new NativeClassProperty() {
			@Override
			public int get(Variant result, Dispatch2 objthis) {
				result.set( 1.41421356237309504880 );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, Interface.STATICMEMBER );
	}
}
