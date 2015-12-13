/**
 ******************************************************************************
 * Copyright (c), Takenori Imoto
 * 楓 software http://www.kaede-software.com/
 * All rights reserved.
 ******************************************************************************
 * ソースコード形式かバイナリ形式か、変更するかしないかを問わず、以下の条件を満
 * たす場合に限り、再頒布および使用が許可されます。
 *
 * ・ソースコードを再頒布する場合、上記の著作権表示、本条件一覧、および下記免責
 *   条項を含めること。
 * ・バイナリ形式で再頒布する場合、頒布物に付属のドキュメント等の資料に、上記の
 *   著作権表示、本条件一覧、および下記免責条項を含めること。
 * ・書面による特別の許可なしに、本ソフトウェアから派生した製品の宣伝または販売
 *   促進に、組織の名前またはコントリビューターの名前を使用してはならない。
 *
 * 本ソフトウェアは、著作権者およびコントリビューターによって「現状のまま」提供
 * されており、明示黙示を問わず、商業的な使用可能性、および特定の目的に対する適
 * 合性に関する暗黙の保証も含め、またそれに限定されない、いかなる保証もありませ
 * ん。著作権者もコントリビューターも、事由のいかんを問わず、損害発生の原因いか
 * んを問わず、かつ責任の根拠が契約であるか厳格責任であるか（過失その他の）不法
 * 行為であるかを問わず、仮にそのような損害が発生する可能性を知らされていたとし
 * ても、本ソフトウェアの使用によって発生した（代替品または代用サービスの調達、
 * 使用の喪失、データの喪失、利益の喪失、業務の中断も含め、またそれに限定されな
 * い）直接損害、間接損害、偶発的な損害、特別損害、懲罰的損害、または結果損害に
 * ついて、一切責任を負わないものとします。
 ******************************************************************************
 * 本ソフトウェアは、吉里吉里2 ( http://kikyou.info/tvp/ ) のソースコードをJava
 * に書き換えたものを一部使用しています。
 * 吉里吉里2 Copyright (C) W.Dee <dee@kikyou.info> and contributors
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
