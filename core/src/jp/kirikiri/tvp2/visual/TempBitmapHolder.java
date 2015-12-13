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

import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tjs2.TJSScriptError;
import jp.kirikiri.tjs2.TJSScriptException;
import jp.kirikiri.tvp2.TVP;
import jp.kirikiri.tvp2.base.CompactEventCallbackInterface;

public class TempBitmapHolder implements CompactEventCallbackInterface {
	static final private int INIT_SIZE = 32;
	static final private int INIT_BPP = 32;
	static final private int INIT_COLOR = 0x00FFFFFF; //TVP_RGBA2COLOR(255, 255, 255, 0));

	static private TempBitmapHolder TempBitmapHolder;
	static public void initialize() {
		TempBitmapHolder = null;
	}
	public static void finalizeApplication() {
		TempBitmapHolder = null;
	}

	private BaseBitmap mBitmap;
	private ArrayList<BaseBitmap> mTemporaries;
	private int mTempLevel;
	private boolean mTempCompactInit;

	private TempBitmapHolder() throws TJSException {
		mBitmap = new BaseBitmap(INIT_SIZE,INIT_SIZE,INIT_BPP);
		mTemporaries = new ArrayList<BaseBitmap>();
		//mTempLevel = 0;
		//mTempCompactInit = false;
		// the default image must be a transparent, white colored rectangle
		mBitmap.fill( new Rect(0, 0, INIT_SIZE, INIT_SIZE), INIT_COLOR );
	}

	protected void finalize() {
		final int count = mTemporaries.size();
		for( int i = 0; i < count; i++ ) {
			mTemporaries.set(i,null);
		}
		if(mTempCompactInit && TVP.EventManager != null ) TVP.EventManager.removeCompactEventHook(this);
	}

	private BaseBitmap internalGetTemp( int w, int h, boolean fit ) throws TJSException {
		// compact initialization
		if(!mTempCompactInit) {
			TVP.EventManager.addCompactEventHook(this);
			mTempCompactInit = true;
		}

		// align width to even
		if(!fit) w += (w & 1);

		// get temporary bitmap (nested)
		mTempLevel++;
		if(mTempLevel > mTemporaries.size()) {
			// increase buffer size
			BaseBitmap bmp = new BaseBitmap(w, h, INIT_BPP);
			mTemporaries.add(bmp);
			return bmp;
		} else {
			BaseBitmap bmp = mTemporaries.get(mTempLevel -1);
			if(!fit) {
				int bw = bmp.getWidth();
				int bh = bmp.getHeight();
				if(bw < w || bh < h) {
					// increase image size
					bmp.setSize(bw > w ? bw:w, bh > h ? bh:h, false);
				}
			} else {
				// the size must be fitted
				int bw = bmp.getWidth();
				int bh = bmp.getHeight();
				if(bw != w || bh != h)
					bmp.setSize(w, h, false);
			}
			return bmp;
		}
	}

	private void internalFreeTemp() {
		if(mTempLevel == 0) return ; // this must be a logical failure
		mTempLevel--;
		//compactTempBitmap(); // always compact
	}

	private void compactTempBitmap() {
		// compact tmporary bitmap cache
		final int count = mTemporaries.size();
		for( int i = count-1; i >= mTempLevel; i-- ) {
			BaseBitmap bmp = mTemporaries.get(i);
			bmp.purgeImage();
			mTemporaries.remove(i);
		}
	}
	@Override
	public void onCompact(int level) throws TJSScriptException, TJSScriptError, TJSException {
		// OnCompact method from tTVPCompactEventCallbackIntf
		// called when the application is idle, deactivated, minimized, or etc...
		if(level >= CompactEventCallbackInterface.COMPACT_LEVEL_DEACTIVATE) compactTempBitmap();
	}

	static BaseBitmap get() throws TJSException {
		if( TempBitmapHolder == null ) TempBitmapHolder = new TempBitmapHolder();
		return TempBitmapHolder.mBitmap;
	}

	static BaseBitmap getTemp( int w, int h ) throws TJSException {
		return getTemp( w, h, false );
	}
	static BaseBitmap getTemp( int w, int h, boolean fit ) throws TJSException {
		if( TempBitmapHolder == null ) TempBitmapHolder = new TempBitmapHolder();
		return TempBitmapHolder.internalGetTemp(w, h, fit);
	}

	static void freeTemp() {
		if( TempBitmapHolder != null ) {
			TempBitmapHolder.internalFreeTemp();
		}
	}
}
