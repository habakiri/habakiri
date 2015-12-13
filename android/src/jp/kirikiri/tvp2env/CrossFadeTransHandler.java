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

import java.nio.IntBuffer;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;

import jp.kirikiri.tjs2.Error;
import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tvp2.visual.DivisibleData;
import jp.kirikiri.tvp2.visual.DivisibleTransHandler;
import jp.kirikiri.tvp2.visual.LayerNI;
import jp.kirikiri.tvp2.visual.LayerType;
import jp.kirikiri.tvp2.visual.ScanLineProvider;
import jp.kirikiri.tvp2.visual.SimpleOptionProvider;

public class CrossFadeTransHandler implements DivisibleTransHandler {

	protected SimpleOptionProvider mOptions;
	protected int mDestLayerType;
	protected long mStartTick;
	protected long mTime; // time during transition
	protected boolean mFirst;

	protected int mPhaseMax;
	protected int mPhase; // current phase (0 thru PhaseMax)

	protected Paint mPaint;
	protected Rect mSrcRect;
	protected Rect mDstRect;

	public CrossFadeTransHandler( SimpleOptionProvider options, int layertype, long time ) {
		this( options, layertype, time, 255 );
	}

	public CrossFadeTransHandler(SimpleOptionProvider options, int layertype, long time, int phasemax ) {
		mDestLayerType = layertype;
		mOptions = options;
		mTime = time;
		mPhaseMax = phasemax;
		mFirst = true;

		mPaint = new Paint();
		mSrcRect = new Rect();
		mDstRect = new Rect();
	}

	@Override
	public int setOption(SimpleOptionProvider options) {
		mOptions = null;
		mOptions = options;
		return Error.S_OK;
	}

	@Override
	public int startProcess(long tick) {
		// notifies starting of the update
		if( mFirst ) {
			mFirst = false;
			mStartTick = tick;
		}

		// compute phase ( 0 thru 255 )
		mPhase = (int) ((tick - mStartTick ) * mPhaseMax / mTime);
		if( mPhase >= mPhaseMax ) mPhase = mPhaseMax;
		return Error.S_TRUE;
	}

	@Override
	public int endProcess() {
		if( mPhase >= mPhaseMax ) {
			finishProcess();
			return Error.S_FALSE;
		}
		return Error.S_TRUE;
	}
	protected void finishProcess() {
	}

	@Override
	public int process( DivisibleData data ) {
		if( mPhase == 0 ) {
			// completely source 1
			data.Dest = data.Src1;
			data.DestLeft = data.Src1Left;
			data.DestTop = data.Src1Top;
		} else if( mPhase == mPhaseMax ) {
			// completety source 2
			data.Dest = data.Src2;
			data.DestLeft = data.Src2Left;
			data.DestTop = data.Src2Top;
		} else {
			try {
				blend(data);
			} catch( Exception e ) {
				e.printStackTrace();
			}
		}
		return Error.S_OK;
	}

	@Override
	public int makeFinalImage( ScanLineProvider dest, ScanLineProvider src1, ScanLineProvider src2 ) throws TJSException {
		// final image is the source2 bitmap
		dest.copyFrom( src2 );
		return Error.S_OK;
	}

	public void blend( DivisibleData data ) throws TJSException {
		Bitmap dest = (Bitmap)data.Dest.getScanLineForWrite().getImage();
		Bitmap src1 = (Bitmap)data.Src1.getScanLine().getImage();
		Bitmap src2 = (Bitmap)data.Src2.getScanLine().getImage();
		int destLeft = data.DestLeft;
		int src1Left = data.Src1Left;
		int src2Left = data.Src2Left;
		int h = data.Height;
		int w = data.Width;
		int destTop = data.DestTop;
		int src1Top = data.Src1Top;
		int src2Top = data.Src2Top;

		// android でもし、アルファ付きのクロスフェードを期待通り処理してくれるのなら、この最初のパターンはいらない
		if( LayerType.isTypeUsingAlpha(mDestLayerType) ) {
			/*
			final int dw = dest.getRowBytes()/4;
			final int dh = dest.getHeight();
			final int s1w = src1.getRowBytes()/4;
			final int s1h = src1.getHeight();
			final int s2w = src2.getRowBytes()/4;
			final int s2h = src2.getHeight();
			{
				byte[] table = CustomOperationComposite.OpacityOnOpacityTable;
				int opa = mPhase;
				if(opa > 127) opa++; // adjust for error
				int iopa = 256 - opa;
				int[] s1 = new int[s1w*s1h];
				int[] s2 = new int[s2w*s2h];
				int[] d = new int[dw*dh];
				IntBuffer s1b = IntBuffer.wrap(s1);
				IntBuffer s2b = IntBuffer.wrap(s2);
				src1.copyPixelsToBuffer(s1b);
				src2.copyPixelsToBuffer(s2b);

				final int destW = dest.getWidth();
				final int src1W = src1.getWidth();
				final int src2W = src2.getWidth();
				destTop = destTop * destW + destLeft;
				src1Top = src1Top * src1W + src1Left;
				src2Top = src2Top * src2W + src2Left;
				for( int y = 0; y < h; y++ ) {
					for( int x = 0; x < w; x++ ) {
						int s1v = s1[src1Top+x];
						int s2v = s2[src1Top+x];
						int a1 = s1v >>> 24;
						int a2 = s2v >>> 24;
						int addr = (a2*opa & 0xff00) + (a1*iopa >>> 8);
						int alpha = table[addr] & 0xff;
						int s1t = s1v & 0xff00ff;
						s1t = ((s1t + (((s2v & 0xff00ff) - s1t) * alpha >>> 8)) & 0xff00ff);
						s1v &= 0xff00;
						s2v &= 0xff00;
						s1t |= (a1 + ((a2 - a1)*opa >>> 8)) << 24;
						int color = s1t | ((s1v + ((s2v - s1v) * alpha >>> 8)) & 0xff00);
						d[destTop+x] = color;
					}
					destTop += destW;
					src1Top += src1W;
					src2Top += src2W;
				}
				// この一気にコピーする方法と、下のdrawBitmapではどっちが早いだろう？
				IntBuffer db = IntBuffer.wrap(d);
				dest.copyPixelsFromBuffer(db);

				// こっちの方が早いかも知れないので、後で計測する事
				// Canvas c = new Canvas(dest);
				// c.drawBitmap( d, 0, dw, 0, 0, dest.getWidth(), dh, true, null );
			}
			*/
			int opa = mPhase;
			if( dest == src1 && destLeft == src1Left && destTop == src1Top) {
				Canvas c = new Canvas(dest);
				mPaint.setAlpha(opa);
				mSrcRect.set(src2Left, src2Top, src2Left+w, src2Top+h);
				mDstRect.set(destLeft, destTop, destLeft+w, destTop+h);
				c.drawBitmap( src2, mSrcRect, mDstRect, mPaint);
				c = null;
			} else {
				Canvas c = new Canvas(dest);
				mPaint.setAlpha(255);
				mSrcRect.set(src1Left, src1Top, src1Left+w, src1Top+h);
				mDstRect.set(destLeft, destTop, destLeft+w, destTop+h);
				c.drawBitmap( src1, mSrcRect, mDstRect, mPaint);

				mPaint.setAlpha(opa);
				mSrcRect.set(src2Left, src2Top, src2Left+w, src2Top+h);
				c.drawBitmap( src2, mSrcRect, mDstRect, mPaint);
				c = null;
			}
		} else if( LayerType.isTypeUsingAddAlpha(mDestLayerType) ) {
			final int dw = dest.getRowBytes()/4;
			final int dh = dest.getHeight();
			final int s1w = src1.getRowBytes()/4;
			final int s1h = src1.getHeight();
			final int s2w = src2.getRowBytes()/4;
			final int s2h = src2.getHeight();
			{
				int opa = mPhase;
				int[] s1 = new int[s1w*s1h];
				int[] s2 = new int[s2w*s2h];
				int[] d = new int[dw*dh];
				IntBuffer s1b = IntBuffer.wrap(s1);
				IntBuffer s2b = IntBuffer.wrap(s2);
				src1.copyPixelsToBuffer(s1b);
				src2.copyPixelsToBuffer(s2b);

				final int destW = dest.getWidth();
				final int src1W = src1.getWidth();
				final int src2W = src2.getWidth();
				destTop = destTop * destW + destLeft;
				src1Top = src1Top * src1W + src1Left;
				src2Top = src2Top * src2W + src2Left;
				for( int y = 0; y < h; y++ ) {
					for( int x = 0; x < w; x++ ) {
						int b = s1[src1Top+x];
						int a = s2[src1Top+x];
						/* dest = a * ratio + b * (1 - ratio) */
						int b2 = b & 0x00ff00ff;
						int t = (b2 + (((a & 0x00ff00ff) - b2) * opa >>> 8)) & 0x00ff00ff;
						b2 = (b & 0xff00ff00) >>> 8;
						d[destTop+x] = t +  (((b2 + (( ((a & 0xff00ff00) >>> 8) - b2) * opa >>> 8)) << 8)& 0xff00ff00);
					}
					destTop += destW;
					src1Top += src1W;
					src2Top += src2W;
				}
				// この一気にコピーする方法と、下のdrawBitmapではどっちが早いだろう？
				IntBuffer db = IntBuffer.wrap(d);
				dest.copyPixelsFromBuffer(db);

				// こっちの方が早いかも知れないので、後で計測する事
				// Canvas c = new Canvas(dest);
				// c.drawBitmap( d, 0, dw, 0, 0, dest.getWidth(), dh, true, null );
			}
		} else {
			int opa = mPhase;
			if( dest == src1 && destLeft == src1Left && destTop == src1Top) {
				Canvas c = new Canvas(dest);
				mPaint.setAlpha(opa);
				mSrcRect.set(src2Left, src2Top, src2Left+w, src2Top+h);
				mDstRect.set(destLeft, destTop, destLeft+w, destTop+h);
				c.drawBitmap( src2, mSrcRect, mDstRect, mPaint);
				c = null;
			} else {
				Canvas c = new Canvas(dest);
				mPaint.setAlpha(255);
				mSrcRect.set(src1Left, src1Top, src1Left+w, src1Top+h);
				mDstRect.set(destLeft, destTop, destLeft+w, destTop+h);
				c.drawBitmap( src1, mSrcRect, mDstRect, mPaint);

				mPaint.setAlpha(opa);
				mSrcRect.set(src2Left, src2Top, src2Left+w, src2Top+h);
				c.drawBitmap( src2, mSrcRect, mDstRect, mPaint);
				c = null;
			}
		}
	}
}
