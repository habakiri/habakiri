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

public class DictionaryObject extends CustomObject {
	private static Variant VoidVal;
	public static void initialize() {
		VoidVal = new Variant();
	}
	public static void finalizeApplication() {
		VoidVal = null;
	}
	public DictionaryObject() {
		super();
		mCallFinalize = false;
	}
	/*
	public int funcCall( int flag, final String membername, Variant result, Variant[] param, Dispatch2 objthis ) throws VariantException, TJSException {
		return super.funcCall(flag, membername, result, param, objthis);
	}
	*/

	public int propGet( int flag, final String membername, Variant result, Dispatch2 objthis ) throws VariantException, TJSException {
		int hr = super.propGet(flag, membername, result, objthis);
		if( hr == Error.E_MEMBERNOTFOUND && (flag & Interface.MEMBERMUSTEXIST) == 0) {
			if(result != null) result.clear(); // returns void
			return Error.S_OK;
		}
		return hr;
	}
	public int createNew( int flag, final String membername, Holder<Dispatch2> result, Variant[] param, Dispatch2 objthis ) throws VariantException, TJSException {
		int hr = super.createNew(flag, membername, result, param, objthis);
		if( hr == Error.E_MEMBERNOTFOUND && (flag & Interface.MEMBERMUSTEXIST) == 0 )
			return Error.E_INVALIDTYPE; // call operation for void
		return hr;
	}
	public int operation( int flag, final String membername, Variant result, final Variant param, Dispatch2 objthis ) throws VariantException, TJSException {
		int hr = super.operation(flag, membername, result, param, objthis);
		if( hr == Error.E_MEMBERNOTFOUND && (flag & Interface.MEMBERMUSTEXIST) == 0 ) {
			// value not found -> create a value, do the operation once more
			hr = super.propSet( Interface.MEMBERENSURE, membername, VoidVal, objthis);
			if( hr < 0 ) return hr;
			hr = super.operation(flag, membername, result, param, objthis);
		}
		return hr;
	}
}
