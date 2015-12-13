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


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Debug;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tvp2.visual.Size;

/**
 *
 * クライアントサイズ - スクリーンサイズ = 拡大率, 表示位置
 * レイヤー位置、レイヤー拡大率 * 拡大率 + 表示位置
 *
 */
public class ImagePanel extends SurfaceView implements SurfaceHolder.Callback {

	static final private boolean SHOW_PERFORMANCE_BAR = false;

	private PanelDrawListener mDraw;
	private SurfaceHolder mSurfaceHolder;

	private Paint mPaint;
	private NativeImageBuffer mImage;
	private android.graphics.Rect mSrcRect;
	private android.graphics.Rect mDstRect;
	private android.graphics.Rect mESrcRect;
	private android.graphics.Rect mEDstRect;
	private android.graphics.Rect mRateRect;
	private android.graphics.Rect mLayerRect;
	private Size mClientSize;

	private int mSurfaceWidth = 1;
	private int mSurfaceHeight = 1;
	private boolean mIsHideLayer;

	private String mErrorMessage;
	private CPUUsedRate mCPURate;

	private BaseActivity mActivity;
	//コンストラクタ
	public ImagePanel( Context context, AttributeSet attrs) {
		super(context, attrs);
		mActivity = (BaseActivity) context;
		// register our interest in hearing about changes to our surface
		SurfaceHolder holder = getHolder();
		holder.setFormat(PixelFormat.TRANSPARENT);
		holder.addCallback(this);
		//holder.setType(SurfaceHolder.SURFACE_TYPE_GPU);
		//mImage = new NativeImageBuffer(32,32);
		mSrcRect = new android.graphics.Rect();
		mDstRect = new android.graphics.Rect();
		mLayerRect = new android.graphics.Rect();
		mESrcRect = new android.graphics.Rect();
		mEDstRect = new android.graphics.Rect();
		mRateRect = new android.graphics.Rect();
		mClientSize = new Size();
		mPaint = new Paint();
		PorterDuffXfermode mode = new PorterDuffXfermode( PorterDuff.Mode.SRC );
		mPaint.setXfermode(mode);
		if( SHOW_PERFORMANCE_BAR ) {
			mCPURate = new CPUUsedRate();
		}
	}
	public ImagePanel(Context context) {
		super(context);
		mActivity = (BaseActivity) context;
		// register our interest in hearing about changes to our surface
		SurfaceHolder holder = getHolder();
		holder.setFormat(PixelFormat.TRANSPARENT);
		holder.addCallback(this);
		//holder.setType(SurfaceHolder.SURFACE_TYPE_GPU);
		//mImage = new NativeImageBuffer(32,32);
		mSrcRect = new android.graphics.Rect();
		mDstRect = new android.graphics.Rect();
		mLayerRect = new android.graphics.Rect();
		mESrcRect = new android.graphics.Rect();
		mEDstRect = new android.graphics.Rect();
		mRateRect = new android.graphics.Rect();
		mClientSize = new Size();
		mPaint = new Paint();
		PorterDuffXfermode mode = new PorterDuffXfermode( PorterDuff.Mode.SRC );
		mPaint.setXfermode(mode);
		if( SHOW_PERFORMANCE_BAR ) {
			mCPURate = new CPUUsedRate();
		}
	}
	public void setLocation(int l, int t, int w, int h) {
		mLayerRect.set( l, t, l+w, t+h );
		updateDestSize();
	}
	public void setSize(int w, int h ) {
		if( w == 0 || h == 0 ) return;
		try {
			//mSrcRect.set( 0, 0, w, h );
			mClientSize.set( w, h );
			updateDestSize();
			if( mImage != null ) {
				mImage.recreate(w, h, true);
				if( mDraw != null ) {
					Canvas c = mImage.getCanvas();
					if( c != null ) {
						mDraw.onDraw( c );
					}
				}
			}
		} catch (TJSException e) {
		}
	}
	public void translatePosition( int[] pos ) {
		int l = mDstRect.left;
		int t = mDstRect.top;
		pos[0] -= l;
		pos[1] -= t;
		//int sw = mSrcRect.width();
		//int sh = mSrcRect.height();
		int sw = mClientSize.width;
		int sh = mClientSize.height;
		int dw = mDstRect.width();
		int dh = mDstRect.height();
		pos[0] = pos[0] * sw / dw;
		pos[1] = pos[1] * sh / dh;
		//pos[0] = pos[0] * dw / sw;
		//pos[1] = pos[1] * dh / sh;
		//Log.v("Pos","x:"+pos[0]+", y:"+pos[1]);
	}
	private void updateDestSize() {
		int x, y, w, h;
		//int ow = mSrcRect.width();
		//int oh = mSrcRect.height();
		int ow = mClientSize.width;
		int oh = mClientSize.height;
		if( ow == 0 || oh == 0 ) return;
		final int dw = mSurfaceWidth;
		final int dh = mSurfaceHeight;
		int cl = mLayerRect.left;
		int ct = mLayerRect.top;
		int cw = mLayerRect.width();
		int ch = mLayerRect.height();
		if( dw == ow && dh >= oh ) { // 等倍、幅は同じ
			x = 0;
			y = (dh - oh) / 2;
			mPaint.setFilterBitmap(false);
		} else if( dw >= ow && dh == oh ) { // 等倍、高さが同じ
			x = (dw - ow) / 2;
			y = 0;
			mPaint.setFilterBitmap(false);
		} else if( dw >= ow && dh >= oh ) { // 拡大
			mPaint.setFilterBitmap(true);
			int sw = dh * ow / oh;
			//int sh = dw * oh / ow;
			if( sw > dw ) { // 幅がはみ出す、高さ基準ではなく幅基準で拡大する
				//w = dw;
				h = (oh * dw) / ow;
				x = 0;
				y = (dh - h)/2;
				cl = (cl * dw) / ow;
				ct = (ct * dw) / ow;
				cw = (cw * dw) / ow;
				ch = (ch * dw) / ow;
			} else {
				w = (ow * dh) / oh;
				//h = dh;
				x = (dw - w)/2;
				y = 0;
				cl = (cl * dh) / oh;
				ct = (ct * dh) / oh;
				cw = (cw * dh) / oh;
				ch = (ch * dh) / oh;
			}
		} else { // 縮小
			mPaint.setFilterBitmap(true);
			int rw = (dw<<16) / ow;
			int rh = (dh<<16) / oh;
			if( rw < rh ) {
				//w = dw;
				h = (oh * dw) / ow;
				x = 0;
				y = (dh - h)/2;
				cl = (cl * dw) / ow;
				ct = (ct * dw) / ow;
				cw = (cw * dw) / ow;
				ch = (ch * dw) / ow;
			} else {
				w = (ow * dh) / oh;
				//h = dh;
				x = (dw - w)/2;
				y = 0;
				cl = (cl * dh) / oh;
				ct = (ct * dh) / oh;
				cw = (cw * dh) / oh;
				ch = (ch * dh) / oh;
			}
		}
		x += cl;
		y += ct;
		mDstRect.set( x, y, x+cw, y+ch );
		mSrcRect.set( 0, 0, mClientSize.width, mClientSize.height ); // TODO mClientSize に入れる必要なかった？
	}

	// 画面の描画処理
	public void show() throws TJSException {
		/*
		if( TVP.MainWindow != null ) {
			final int w = TVP.MainWindow.getInnerWidth();
			final int h = TVP.MainWindow.getInnerHeight();
			final int dw = mSurfaceWidth;
			final int dh = mSurfaceHeight;
			mSrcRect.set( 0, 0, w, h );
			int x = (mSurfaceWidth - w) / 2;
			int y = (mSurfaceHeight - h) / 2;
			mDstRect.set( x, y, x+w, y+h );
		}
		*/
		Canvas c = null;
		try {
			c = mSurfaceHolder.lockCanvas(null);
			if( c != null )
				if( mIsHideLayer == false ) {
					// 両端の余白を塗る
					mPaint.setColor(0xff000000);
					if( mDstRect.left > 0 ) c.drawRect(0, 0, mDstRect.left, mSurfaceHeight, mPaint );
					if( mDstRect.right < mSurfaceWidth ) c.drawRect( mDstRect.right, 0, mSurfaceWidth, mSurfaceHeight, mPaint );

					if( mImage != null ) {
						c.drawBitmap(mImage.getImage(), mSrcRect, mDstRect, mPaint );
					}
				} else {
					c.drawColor(0, PorterDuff.Mode.SRC );
				}
				if( SHOW_PERFORMANCE_BAR ) {
					drawRate( c );
				}
				mActivity.drawMenu(c);
				if( mErrorMessage != null ) {
					drawErrorMessage(c);
				}
		} catch( NullPointerException e ) {
			if( c != null ) { // c が null の時の例外は無視する
				throw e;
			}
		} finally {
			if (c != null) {
				mSurfaceHolder.unlockCanvasAndPost(c);
			}
		}
	}
	final static private int RATE_W = 8;
	private void drawRate( Canvas c ) {
		// draw CPU rate
		int left = 0;
		int right = RATE_W;
		float rate = mCPURate.getRate();
		int h = (int) (mSurfaceHeight * rate);
		int top = mSurfaceHeight-h;
		mRateRect.set( left, 0, right, top );
		mPaint.setColor(0xff000000);
		c.drawRect( mRateRect, mPaint );

		mRateRect.set( left, top, right, mSurfaceHeight );
		mPaint.setColor(0xff00ff00);
		c.drawRect( mRateRect, mPaint );
		left = right;
		right += RATE_W;

		// draw native memory
		Runtime runtime = Runtime.getRuntime();
		long maxmem = runtime.maxMemory();
		long used = (runtime.totalMemory() - runtime.freeMemory()) + Debug.getNativeHeapSize();
		//long used = Debug.getNativeHeapAllocatedSize();
		//long hl = mSurfaceHeight * used / (24*1024*1024);
		long hl = mSurfaceHeight * used / maxmem;
		top = (int) (mSurfaceHeight-hl);
		if( top < 0 ) {
			top = 0;
		} else {
			mRateRect.set( left, 0, right, top );
			mPaint.setColor(0xff000000);
			c.drawRect( mRateRect, mPaint );
		}
		mRateRect.set( left, top, right, mSurfaceHeight );
		mPaint.setColor(0xffff0000);
		c.drawRect( mRateRect, mPaint );
		left = right;
		right += RATE_W;

		// draw dalvik memory
		used = runtime.totalMemory() - runtime.freeMemory();
		long maxsize = runtime.totalMemory();
		hl = mSurfaceHeight * used / maxsize;
		top = (int) (mSurfaceHeight-hl);
		if( top < 0 ) {
			top = 0;
		} else {
			mRateRect.set( left, 0, right, top );
			mPaint.setColor(0xff000000);
			c.drawRect( mRateRect, mPaint );
		}
		mRateRect.set( left, top, right, mSurfaceHeight );
		mPaint.setColor(0xffffff00);
		c.drawRect( mRateRect, mPaint );

		mCPURate.startLogging();
	}
	private void drawErrorMessage( Canvas c ) {
		mESrcRect.set( 0, 0, mSurfaceWidth, mSurfaceHeight );
		final int limW = mSurfaceWidth-20;
		final int length = mErrorMessage.length();
		mPaint.setTextSize(24);
		mPaint.getTextBounds( mErrorMessage, 0, length, mEDstRect );
		if( mEDstRect.right > limW || mErrorMessage.indexOf('\n') != -1 ) {
			// はみ出してしまう
			mESrcRect.bottom = 10;
			mPaint.setColor(0xff0000ff);
			c.drawRect(mESrcRect, mPaint);
			mESrcRect.top = mESrcRect.bottom;

			int start = 0;
			int end = 1;
			while( end <= length ) {
				char ch = 0;
				do {
					ch = mErrorMessage.charAt(end-1);
					mPaint.getTextBounds( mErrorMessage, start, end, mEDstRect );
					end++;
				} while( mEDstRect.right < limW && end <= length && ch != '\n' );
				mESrcRect.bottom = mESrcRect.top+mEDstRect.height();
				mPaint.setColor(0xff0000ff);
				c.drawRect(mESrcRect, mPaint);
				mPaint.setColor(0xffffffff);
				c.drawText( mErrorMessage, start, ch != '\n' ? end-1 : end-2, 10, mESrcRect.top-mEDstRect.top, mPaint );
				mESrcRect.top = mESrcRect.bottom;
				start = end-1;
			}
		} else {
			mESrcRect.bottom = mEDstRect.height()+10;
			mPaint.setColor(0xff0000ff);
			c.drawRect(mESrcRect, mPaint);
			mPaint.setColor(0xffffffff);
			c.drawText( mErrorMessage, 10, 10-mEDstRect.top, mPaint );
		}
	}
	public void setDrawListener( PanelDrawListener draw ) {
		mDraw = draw;
	}
	public Canvas getBackbufferCanvas() throws TJSException {
		if( mImage == null ) {
			//int w = mSrcRect.width();
			//int h = mSrcRect.height();
			int w = mClientSize.width;
			int h = mClientSize.height;
			if( w == 0 || h == 0 ) {
				w = mSurfaceWidth;
				h = mSurfaceHeight;
				if( w == 0 || h == 0 ) {
					w = 32;
					h = 32;
				}
			}
			mImage = new NativeImageBuffer(w,h);
		}
		return mImage.getCanvas();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		mSurfaceHolder = holder;
		mSurfaceWidth = width;
		mSurfaceHeight = height;
		mDstRect.set( 0,0,width,height);
		updateDestSize();
		//setSize( width, height );
		//mImage.recreate(width, height, true);
		//mSrcRect.set( 0, 0, width, height );
	}
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
	}
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
	}
	public void setErrorMessage(String caption) {
		mErrorMessage = caption;
		try {
			show();
		} catch (TJSException e) {
		}
	}
	public void setHideLayer( boolean b ) {
		mIsHideLayer = b;
	}
}
