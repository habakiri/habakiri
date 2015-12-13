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

public class CharacterData {
	// character data holder for caching
	private byte[] mData;

	public int mOriginX;
	public int mOriginY;
	public int mCellIncX;
	public int mCellIncY;
	public int mPitch;
	public int mBlackBoxX;
	public int mBlackBoxY;
	public int mBlurLevel;
	public int mBlurWidth;

	public boolean mAntialiased;
	public boolean mBlured;
	public boolean mFullColored;

	public void alloc(int size) {
		mData = new byte[size];
	}
	public byte[] getData() { return mData; }
	public void blur() {
		// 	blur the bitmap
		blur(mBlurLevel, mBlurWidth);
	}
	private void blur(int blurlevel, int blurwidth) {
		// blur the bitmap with given parameters
		// blur the bitmap
		if( mData == null ) return;
		if( blurlevel == 255 && blurwidth == 0 ) return; // no need to blur
		if( blurwidth == 0 ) {
			// no need to blur but must be transparent
			chBlurMulCopy65( mData, mData, mPitch*mBlackBoxY, mBlurLevel<<10);
			return;
		}

		// simple blur ( need to optimize )
		int bw = Math.abs(blurwidth);
		int newwidth = mBlackBoxX + bw*2;
		int newheight = mBlackBoxY + bw*2;
		int newpitch =  (((newwidth -1)>>2)+1)<<2;

		byte[] newdata = new byte[newpitch * newheight];

		chBlurCopy65(newdata, newpitch, newwidth, newheight, mData, mPitch, mBlackBoxX, mBlackBoxY, bw, blurlevel);

		mData = null;
		mData = newdata;
		mBlackBoxX = newwidth;
		mBlackBoxY = newheight;
		mPitch = newpitch;
		mOriginX -= blurwidth;
		mOriginY -= blurwidth;
	}
	private void chBlurCopy65(byte[] dest, int destpitch, int destwidth,
			int destheight, byte[] src, int srcpitch, int srcwidth,
			int srcheight, int blurwidth, int blurlevel ) {

		// clear destination
		//memset(dest, 0, destpitch*destheight);

		// compute filter level
		int lvsum = 0;
		for( int y = -blurwidth; y <= blurwidth; y++ ) {
			for( int x = -blurwidth; x <= blurwidth; x++ ) {
				int len = fastIntHypot(x, y);
				if(len <= blurwidth)
					lvsum += (blurwidth - len +1);
			}
		}

		if( lvsum != 0 ) lvsum = (1<<18)/lvsum;
		else lvsum=(1<<18);

		/* apply */
		for( int y = -blurwidth; y <= blurwidth; y++ ) {
			for( int x = -blurwidth; x <= blurwidth; x++ ) {
				int len = fastIntHypot( x, y );
				if( len <= blurwidth ) {
					len = blurwidth - len +1;
					len *= lvsum;
					len *= blurlevel;
					len >>>= 8;
					for( int sy = 0; sy < srcheight; sy++ ) {
						chBlurAddMulCopy65( dest, (y + sy + blurwidth)*destpitch + x + blurwidth,
							src, sy * srcpitch, srcwidth, len);
					}
				}
			}
		}
	}
	private static final void chBlurMulCopy65( byte[] dest, byte[] src, int len, int level ) {
		for( int i = 0; i < len; i++ ) {
			int a = src[i] * level >>> 18;
			if( a >= 64 ) a = 64;
			dest[i] = (byte) a;
		}

	}
	private static final void chBlurAddMulCopy65( byte[] dest, int desti, byte[] src, int srci, int len, int level ) {
		final int limit = desti + len;
		while( desti < limit ) {
			int a = dest[desti] +(src[srci] * level >>> 18);
			if( a >= 64 ) a = 64;
			dest[desti] = (byte)a;
			desti++;
			srci++;
		}
	}
	/* fast_int_hypot from http://demo.and.or.jp/makedemo/effect/math/hypot/fast_hypot.c */
	private static final int fastIntHypot( int lx, int ly ) {
		int len1, len2, t, length;
		if( lx < 0 ) lx = -lx;
		if( ly < 0 ) ly = -ly;
		if( lx >= ly ) {
			len1 = lx;
			len2 = ly;
		} else {
			len1 = ly;
			len2 = lx;
		}
		t = len2 + (len2 >>> 1);
		length = len1 - (len1 >>> 5) - (len1 >>> 7) + (t >>> 2) + (t >>> 6);
		return length;
	}
}
