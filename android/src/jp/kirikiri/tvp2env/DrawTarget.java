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
/**
 * プラットフォーム依存(Android/Java 2D)描画処理を隠蔽する
 */
package jp.kirikiri.tvp2env;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Region;
import android.util.Log;
import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tvp2.visual.Point;
import jp.kirikiri.tvp2.visual.Rect;

/**
 *
 *
 */
public class DrawTarget {
	private Canvas mGraphcs;
	private WindowForm mWindow;
	private Paint mPaint;
	private android.graphics.Rect mSrcRect;
	private android.graphics.Rect mDstRect;
	private Region[] mUpdateRegion;
	private PorterDuffXfermode mModeSrcOver;
	private PorterDuffXfermode mModeSrc;
	//private PorterDuffXfermode mModeSrcAtop;

	public DrawTarget() {
		mPaint = new Paint();
		mModeSrcOver = new PorterDuffXfermode( PorterDuff.Mode.SRC_OVER );
		mModeSrc = new PorterDuffXfermode( PorterDuff.Mode.SRC );
		//mModeSrcAtop = new PorterDuffXfermode( PorterDuff.Mode.SRC_ATOP );
		mPaint.setXfermode(mModeSrcOver);
		mSrcRect = new android.graphics.Rect();
		mDstRect = new android.graphics.Rect();
		mUpdateRegion = new Region[2];
		mUpdateRegion[0] = new Region();
		mUpdateRegion[1] = new Region();
	}
	public void setGraphics( Canvas g, WindowForm w ) {
		mGraphcs = g;
		mWindow = w;
	}

	public void drawImage(int x, int y, NativeImageBuffer src, Rect refrect, int type, int opacity) throws TJSException {
		Canvas g = mGraphcs;
		//mPaint.setAlpha(opacity);
		if ((g != null) && (src.getImage() != null)) {
			// type はとりあえず無視
			final int w = refrect.width();
			final int h = refrect.height();
			mDstRect.set(x,y,x+w,y+h);
			mSrcRect.set(refrect.left,refrect.top,refrect.right,refrect.bottom);
			if( opacity == 255 || opacity == 0 ) {
				//mPaint.setColor(0xFF000000);
				//g.drawRect(mDstRect, mPaint);
				mPaint.setXfermode(mModeSrc);
				//mPaint.setXfermode(mModeSrcAtop);
			} else {
				mPaint.setXfermode(mModeSrcOver);
			}
			mPaint.setAlpha(opacity);
			//g.save();
			//g.clipRect( x,y,x+w,y+h );
			//g.drawColor(0,PorterDuff.Mode.CLEAR);
			g.drawBitmap(src.getImage(), mSrcRect, mDstRect, mPaint );
			//g.restore();
			//int sw = src.getWidth();
			//int sh = src.getHeight();
			//Log.v("Draw", "x: "+x+", y :"+y+", w: "+w+", h: "+h +", sw:"+sw+", sh:"+sh);
		}
	}
	public void show() throws TJSException {
		if( mWindow != null && mWindow.isVisible() ) {
			mWindow.repaint();
		}
	}
	public void drawLines( Point[] points, int color ) {
		Canvas g = mGraphcs;
		final int count = points.length;
		float[] pts = new float[count*4];
		pts[0] = points[0].x;
		pts[1] = points[0].y;
		int p = 2;
		for( int i = 1; i < count; i++ ) {
			pts[p] = points[i].x; p++;
			pts[p] = points[i].y; p++;
			pts[p] = points[i].x; p++;
			pts[p] = points[i].y; p++;
		}
		pts[p] = points[0].x; p++;
		pts[p] = points[0].y; p++;
		mPaint.setColor(color);
		mPaint.setAlpha(255);
		g.drawLines( pts, mPaint );
	}
}
