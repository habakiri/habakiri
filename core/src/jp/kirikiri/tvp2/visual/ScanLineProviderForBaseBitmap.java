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

import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tvp2.msg.Message;
import jp.kirikiri.tvp2env.NativeImageBuffer;

public class ScanLineProviderForBaseBitmap implements ScanLineProvider {
	private boolean mOwn;
	BaseBitmap mBitmap;

	public ScanLineProviderForBaseBitmap( BaseBitmap bmp ) {
		this(bmp,false);
	}
	public ScanLineProviderForBaseBitmap( BaseBitmap bmp, boolean own ) {
		mOwn = own;
		mBitmap = bmp;
	}

	/**
	 * attach bitmap
	 * @param bmp
	 */
	public void attach( BaseBitmap bmp ) {
		mBitmap = bmp;
	}
	@Override
	public int getWidth() {
		return mBitmap.getWidth();
	}

	@Override
	public int getHeight() {
		return mBitmap.getHeight();
	}

	@Override
	public int getPixelFormat() {
		return mBitmap.getBPP();
	}

	@Override
	public int getPitchBytes() {
		return mBitmap.getPitchBytes();
	}

	@Override
	public NativeImageBuffer getScanLine() {
		return mBitmap.getBitmap();
	}

	@Override
	public NativeImageBuffer getScanLineForWrite() {
		mBitmap.independ();
		NativeImageBuffer ret = mBitmap.getBitmap();
		ret.toMutable();
		return ret;
	}
	@Override
	public void copyFrom(ScanLineProvider src) throws TJSException {
		if( src instanceof ScanLineProviderForBaseBitmap ) {
			ScanLineProviderForBaseBitmap s = (ScanLineProviderForBaseBitmap)src;
			mBitmap = s.mBitmap;
			mOwn = s.mOwn;
		} else {
			Message.throwExceptionMessage(Message.InternalError);
		}
	}
}
