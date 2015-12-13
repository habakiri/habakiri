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
import jp.kirikiri.tjs2.Dispatch2;
import jp.kirikiri.tjs2.Holder;
import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tjs2.Variant;
import jp.kirikiri.tjs2.VariantClosure;

public class SimpleOptionProviderObject implements SimpleOptionProvider {

	private VariantClosure mObject;
	//private String mString;
	private Variant mRetVariant;

	public SimpleOptionProviderObject( VariantClosure object ) {
		mObject = object;
		mRetVariant = new Variant();
	}
	@Override
	public int getAsNumber(String name, long[] value) {
		try {
			mRetVariant.clear();
			int er = mObject.propGet( 0, name, mRetVariant, null );
			if( er < 0 ) return er;

			if( mRetVariant.isVoid() ) return Error.E_MEMBERNOTFOUND;

			if( value != null ) value[0] = mRetVariant.asInteger();
			mRetVariant.clear();

			return Error.S_OK;
		} catch( TJSException e ) {
			return Error.E_FAIL;
		}
	}

	@Override
	public int getAsString(String name, String[] out) {
		try {
			mRetVariant.clear();
			int er = mObject.propGet( 0, name, mRetVariant, null );
			if( er < 0 ) return er;

			if( mRetVariant.isVoid() ) return Error.E_MEMBERNOTFOUND;

			if( out != null ) out[0] = mRetVariant.asString();
			mRetVariant.clear();

			return Error.S_OK;
		} catch( TJSException e ) {
			return Error.E_FAIL;
		}
	}

	@Override
	public int getValue(String name, Variant dest) {
		try {
			if( dest == null ) return Error.E_FAIL;
			int er = mObject.propGet( 0, name, dest, null );
			if( er < 0 ) return er;
			return Error.S_OK;
		} catch( TJSException e ) {
			return Error.E_FAIL;
		}
	}

	@Override
	public int getDispatchObject(Holder<Dispatch2> dsp) {
		if( dsp != null ) dsp.mValue = mObject.mObjThis;
		return Error.S_OK;
	}

}
