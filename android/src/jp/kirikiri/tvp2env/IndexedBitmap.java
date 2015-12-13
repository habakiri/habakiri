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
package jp.kirikiri.tvp2env;

import java.nio.ByteBuffer;
import java.util.Arrays;

import jp.kirikiri.tvp2.TVP;
import jp.kirikiri.tvp2.base.CompactEventCallbackInterface;
import jp.kirikiri.tvp2.visual.Rect;

// TODO 遅延確保を実装する事。実際に使われるまでメモリ確保を遅らせる
public class IndexedBitmap {
	private int[] mPalette;
	private ByteBuffer mBuffer; // byte[] にしてしまった方がいいかな
	private int mWidth;
	private int mHeight;
	private byte mSingleColor;

	public IndexedBitmap( int w, int h ) {
		// 遅延確保
		//final int size = w*h;
		//mBuffer = ByteBuffer.allocateDirect(size);
		//mBuffer.limit(size);
		mWidth = w;
		mHeight = h;
	}
	public IndexedBitmap( int w, int h, byte[] src ) {
		// 遅延確保
		final int size = w*h;
		mBuffer = ByteBuffer.wrap(src);
		mBuffer.limit(size);
		mWidth = w;
		mHeight = h;
	}
	public IndexedBitmap copy() {
		IndexedBitmap ret = new IndexedBitmap(mWidth,mHeight);
		if( mBuffer != null ) {
			// 遅延確保
			final int size = mWidth*mHeight;
			//ret.mBuffer = ByteBuffer.allocateDirect(size);
			ret.mBuffer = ByteBuffer.allocate(size);
			ret.mBuffer.limit(size);
			ret.mBuffer.put( mBuffer );
		}
		ret.mSingleColor = mSingleColor;
		if( mPalette != null ) {
			final int len = mPalette.length;
			ret.mPalette = new int[len];
			System.arraycopy(mPalette, 0, ret.mPalette, 0, len );
		}
		return ret;
	}
	public void recreate( int w, int h, boolean keepimage ) {
		final int ow = mWidth;
		final int oh = mHeight;
		if( w != mWidth || h != mHeight ) {
			mWidth = w;
			mHeight = h;
			if( mBuffer == null ) return;	// 遅延確保
			final int size = w*h;
			if( keepimage == false ) {
				mBuffer = null;
				mBuffer = ByteBuffer.allocate(size);
				mBuffer.limit(size);
			} else {
				ByteBuffer old = mBuffer;
				mBuffer = null;
				mBuffer = ByteBuffer.allocate(size);
				mBuffer.limit(size);
				if( old != null ) {
					byte[] s;
					byte[] d;
					try {
						d = new byte[w*h];
					} catch( OutOfMemoryError e ) {
						System.gc();
						d = new byte[w*h];
					}
					try {
						s = new byte[ow*oh];
					} catch( OutOfMemoryError e ) {
						System.gc();
						s = new byte[ow*oh];
					}
					old.position(0);
					old.get(s);
					final int rw = ow < w ? ow : w;
					final int rh = oh < h ? oh : h;
					int sstart = 0;
					int dstart = 0;
					for( int y = 0; y < rh; y++ ) {
						int sp = sstart;
						int dp = dstart;
						for( int x = 0; x < rw; x++ ) {
							d[dp] = s[sp];
							sp++;
							dp++;
						}
						sstart += ow;
						dstart += w;
					}
					mBuffer.position(0);
					mBuffer.put(d);
				}
			}
		}
	}
	// 遅延確保
	private void createLateAllocate() {
		final int w = mWidth;
		final int h = mHeight;
		final int size = mWidth*mHeight;
		mBuffer = ByteBuffer.allocate(size);
		mBuffer.limit(size);
		if( mSingleColor != 0 ) {
			byte[] s;
			try {
				s = new byte[w*h];
			} catch( OutOfMemoryError e ) {
				System.gc();
				s = new byte[w*h];
			}
			Arrays.fill(s, (byte)(mSingleColor&0xff) );
			mBuffer.position(0);
			mBuffer.put(s);
		}
	}

	public void fill( Rect rect, int value ) {
		final int w = mWidth;
		final int h = mHeight;
		if( mBuffer == null && rect.left == 0 && rect.top == 0 && rect.right == w && rect.bottom == h) {
			// 全体塗りつぶし
			mSingleColor = (byte) (value&0xff);
			return;
		}
		if( mBuffer == null ) {
			createLateAllocate();
		}
		byte[] s;
		try {
			s = new byte[w*h];
		} catch( OutOfMemoryError e ) {
			System.gc();
			s = new byte[w*h];
		}
		mBuffer.position(0);
		mBuffer.get(s);
		int ystart = rect.top*w + rect.left;
		final int rw = rect.right-rect.left;
		final int rh = (rect.bottom-rect.top);
		byte color = (byte) (value&0xff);
		for( int y = 0; y < rh; y++ ) {
			int sp = ystart;
			for( int x = 0; x < rw; x++ ) {
				s[sp] = color;
				sp++;
			}
			ystart += w;
		}
		mBuffer.position(0);
		mBuffer.put(s);
	}
	public void flipLR(Rect rect) {
		if( mBuffer == null ) {
			return; // 空の時は何もしない
		}
		final int w = mWidth;
		final int h = mHeight;
		byte[] s;
		try {
			s = new byte[w*h];
		} catch( OutOfMemoryError e ) {
			System.gc();
			s = new byte[w*h];
		}
		mBuffer.position(0);
		mBuffer.get(s);

		int ystart = rect.top*w + rect.left;
		final int rh = rect.bottom-rect.top;
		final int rw = rect.right-rect.left;
		final int w2 = rw / 2;
		for( int y = 0; y < rh; y++ ) {
			for( int x = 0; x < w2; x++ ) {
				int pos = ystart+x;
				int dst = ystart+rw-x-1;
				byte c = s[pos];
				s[pos] = s[dst];
				s[dst] = c;
			}
			ystart += w;
		}
		mBuffer.position(0);
		mBuffer.put(s);
	}
	public void flipUD(Rect rect) {
		if( mBuffer == null ) {
			return; // 空の時は何もしない
		}
		final int w = mWidth;
		final int h = mHeight;
		byte[] s;
		try {
			s = new byte[w*h];
		} catch( OutOfMemoryError e ) {
			TVP.EventManager.deliverCompactEvent(CompactEventCallbackInterface.COMPACT_LEVEL_MAX);
			System.gc();
			s = new byte[w*h];
		}
		mBuffer.position(0);
		mBuffer.get(s);
		int ystart = rect.top*w + rect.left;
		int yend = (rect.bottom-1)*w + rect.left;
		final int rw = rect.right-rect.left;
		final int h2 = (rect.bottom-rect.top) / 2;
		for( int y = 0; y < h2; y++ ) {
			int sp = ystart;
			int dp = yend;
			for( int x = 0; x < rw; x++ ) {
				byte c = s[sp];
				s[sp] = s[dp];
				s[dp] = c;
				sp++;
				dp++;
			}
			ystart += w;
			yend -= w;
		}
		mBuffer.position(0);
		mBuffer.put(s);
	}
	public int getPixel( int x, int y ) {
		if( mBuffer == null ) {
			return mSingleColor&0xff;
		}
		final int w = mWidth;
		final int pos = y*w  + x;
		if( pos < mBuffer.capacity() ) {
			return mBuffer.get(pos)&0xff;
		}
		return 0;
	}
	public void setPixel( int x, int y, int color ) {
		if( mBuffer == null ) {
			createLateAllocate();
		}
		byte val = (byte) (color & 0xff);
		final int w = mWidth;
		final int pos = y*w  + x;
		if( pos < mBuffer.capacity() ) {
			mBuffer.put( pos, val );
		}
	}
	public void copyRect(int x, int y, IndexedBitmap src, Rect refrect) {
		if( this == src ) {
			if( mBuffer == null ) return;

			int w = refrect.width();
			int h = refrect.height();
			if( ( (x+w) < refrect.left || x >= refrect.right ) &&
				( (y+h) < refrect.top || y >= refrect.bottom ) ) {
				// コピー領域が重なっていない
			} else {
				// 自分自身へのコピーの時で、コピー領域が重複している
				IndexedBitmap srccopy = src.copy();
				copyRect( x, y, srccopy, refrect );
				srccopy = null;
				return;
			}
		} else {
			if( mBuffer == null ) {
				createLateAllocate();
			}
			if( src.mBuffer == null ) {
				src.createLateAllocate();
			}
		}
		final int dw = mWidth;
		final int dh = mHeight;
		final int size = dw*dh;
		if( src.mBuffer != null ) {
			final int sw = src.mWidth;
			final int sh = src.mHeight;
			byte[] s;
			byte[] d;
			try {
				d = new byte[size];
			} catch( OutOfMemoryError e ) {
				System.gc();
				d = new byte[size];
			}
			try {
				s = new byte[sw*sh];
			} catch( OutOfMemoryError e ) {
				System.gc();
				s = new byte[sw*sh];
			}
			src.mBuffer.position(0);
			src.mBuffer.get(s);
			final int rw = refrect.width();
			final int rh = refrect.height();
			int sstart = refrect.top*sw+refrect.left;
			int dstart = y*dw+x;
			for( int cy = 0; cy < rh; cy++ ) {
				int sp = sstart;
				int dp = dstart;
				for( int cx = 0; cx < rw; cx++ ) {
					d[dp] = s[sp];
					sp++;
					dp++;
				}
				sstart += sw;
				dstart += dw;
			}
			mBuffer.position(0);
			mBuffer.put(d);
		}
	}
	public int width() { return mWidth; }
	public int height() { return mHeight; }
}
