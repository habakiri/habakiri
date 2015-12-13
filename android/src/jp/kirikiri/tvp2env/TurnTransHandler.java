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

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.Style;
import android.util.FloatMath;

import jp.kirikiri.tjs2.Error;
import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tvp2.visual.DivisibleData;
import jp.kirikiri.tvp2.visual.DivisibleTransHandler;
import jp.kirikiri.tvp2.visual.ScanLineProvider;
import jp.kirikiri.tvp2.visual.SimpleOptionProvider;

/**
 * 'turn' トランジション
 * 正方形の小さなタイルをひっくり返すようにして切り替わるトランジション
 *
 * オリジナルとは異なり、シャーリング変換とスケーリング変換、移動によるアフェイン変換によって実装。
 * オリジナルも近い処理によってテーブルを生成し、それを元に描画しているようだが、テーブルが巨大なので
 * Java だと「static イニシャライザーのコードが 65535バイトの制限を超えています」と出て動かない。
 * そのためアフェイン変換を用いた実装になっている。
 * 分割描画等があるとたぶん期待通りには動かないと思われる。
 */
public class TurnTransHandler implements DivisibleTransHandler {

	/** 1 を設定すると 2 を設定したときよりも一度に回転するブロックの数が多くなる */
	private static final int TURN_WIDTH_FACTOR = 2;
	private static final int BLOCK_SIZE = 64;
	//private static final int HALF_BLOCK_SIZE = BLOCK_SIZE / 2;

	// テカり
	static private final int[] GLOSS = {
		   0,   0,   0,   0,  16,  48,  80, 128,
		 192, 128,  80,  48,  16,   0,   0,   0,
		   0,   0,   0,   0,   0,   0,   0,   0,
		   0,   0,   0,   0,   0,   0,   0,   0,
		   0,   0,   0,   0,   0,   0,   0,   0,
		   0,   0,   0,   0,   0,   0,   0,   0,
		   0,   0,   0,   0,   0,   0,   0,   0,
		   0,   0,   0,   0,   0,   0,   0,   0,
	};

	private long mStartTick; // トランジションを開始した tick count
	private long mTime; // トランジションに要する時間
	private long mCurTime; // 現在の時間
	private int mWidth; // 処理する画像の幅
	private int mHeight; // 処理する画像の高さ
	private int mBGColor; // 背景色
	private int mPhase; // アニメーションのフェーズ
	private boolean mFirst; // 一番最初の呼び出しかどうか

	private Matrix[] mTransMatrix;

	protected Paint mPaint;
	protected Rect mSrcRect;
	protected Rect mDstRect;
	private Matrix mTrans;
	private float[] mScales;
	private Canvas mCanvas;

	static private final float PI = 3.141592653589793f;
	public TurnTransHandler(long time, int width, int height, int bgcolor) {

		mWidth = width;
		mHeight = height;
		mTime = time;
		mBGColor = bgcolor | 0xFF000000;

		// 変換用マトリックス準備
		mScales = new float[32];
		mTransMatrix = new Matrix[32];
		mTransMatrix[0] = new Matrix(); // identity matrix
		mScales[0] = 1;
		Matrix shearm = new Matrix();
		for( int i = 1; i < 32; i++ ) {
			float p = (i*i / 31.0f);
			float d = (FloatMath.sin( p * PI / 32 ) * 4);
			float y = p - d;
			float shear = y / 32.0f;
			float scale = 1.0f/(1.0f+shear);

			shearm.reset();
			shearm.setScale( scale, scale );
			shearm.preSkew( shear, shear );

			mTransMatrix[i] = new Matrix(shearm);
			mScales[i] = scale;
		}
		shearm = null;

		mPaint = new Paint();
		mSrcRect = new Rect();
		mDstRect = new Rect();
		mTrans = new Matrix();
		mCanvas = new Canvas();
		mFirst = true;
	}

	@Override
	public int setOption(SimpleOptionProvider options) {
		return Error.S_OK;
	}

	/**
	 * トランジションの画面更新一回ごとに呼ばれる
	 *
	 * トランジションの画面更新一回につき、まず最初に StartProcess が呼ばれる
	 * そのあと Process が複数回呼ばれる ( 領域を分割処理している場合 )
	 * 最後に EndProcess が呼ばれる
	 */
	@Override
	public int startProcess(long tick) {
		if( mFirst ) {
			// 最初の実行
			mFirst = false;
			mStartTick = tick;
		}

		// 画像演算に必要なパラメータを計算
		// 左下から回転し始め、最後に右上が回転を終えるまで処理をする

		mCurTime = (tick - mStartTick);
		if(mCurTime > mTime) mCurTime = mTime;
		int xcount = (mWidth-1) / BLOCK_SIZE + 1;
		int ycount = (mHeight-1) / BLOCK_SIZE + 1;
		mPhase = (int) (mCurTime * (BLOCK_SIZE + (xcount +  ycount) *TURN_WIDTH_FACTOR) / mTime - ycount *TURN_WIDTH_FACTOR);

		return Error.S_TRUE;
	}

	@Override
	public int endProcess() {
		// トランジションの画面更新一回分が終わるごとに呼ばれる
		if(mCurTime == mTime) return Error.S_FALSE; // トランジション終了
		return Error.S_TRUE;
	}

	/**
	 * トランジションの各領域ごとに呼ばれる
	 * 吉里吉里は画面を更新するときにいくつかの領域に分割しながら処理を行うので
	 * このメソッドは通常、画面更新一回につき複数回呼ばれる
	 *
	 * @param data には領域や画像に関する情報が入っている
	 *
	 * data.Left, data.Top, data.Width, data.Height で示される矩形に
	 * のみ転送する必要がある。ここで行う処理は 'モザイク' のトランジション
	 * の処理に似ていて、以下の通り。
	 * 1: その転送矩形に含まれるブロックの範囲を判定する
	 * 2: 画面一番下のブロックはアクセスオーバーランに気をつけて転送する
	 * 3: その範囲の左端と右端のブロックは、上下のクリッピングに加え、
	 * 		左右のクリッピングを行いながら転送する
	 * 4: それ以外のブロックは上下のクリッピングのみを行いながら転送する
	 * ちなみに吉里吉里は通常 8 ラインごとの横に細長い領域を上から順に
	 * 指定してくる。
	 * ブロックサイズは 64x64 固定。
	 * @throws TJSException
	 */
	@Override
	public int process(DivisibleData data) throws TJSException {
		Bitmap dest = (Bitmap)data.Dest.getScanLineForWrite().getImage();
		Bitmap src1 = (Bitmap)data.Src1.getScanLine().getImage();
		Bitmap src2 = (Bitmap)data.Src2.getScanLine().getImage();
		final int destLeft = data.DestLeft;
		final int h = data.Height;
		final int w = data.Width;
		final int destTop = data.DestTop;
		final int left = data.Left;
		final int top = data.Top;
		final int right = left + w;
		final int bottom = top + h;

		final int destxofs = destLeft - left;
		final int destyofs = destTop - top;

		final Canvas c = mCanvas;
		c.setBitmap(dest);
		final Paint paint = mPaint;
		paint.setFilterBitmap(false);
		paint.setStyle(Style.FILL);
		final Matrix trans = mTrans;
		final Rect srcRect = mSrcRect;
		final Rect dstRect = mDstRect;
		final Matrix[] transMatix = mTransMatrix;
		final int basePhase = mPhase;
		final float[] scales = mScales;
		final int bgColor = mBGColor;

		// 1: その転送矩形に含まれるブロックの範囲を判定する
		final int startx = data.Left / BLOCK_SIZE;
		final int starty = data.Top / BLOCK_SIZE;
		final int endx = (data.Left + data.Width - 1) / BLOCK_SIZE;
		final int endy = (data.Top + data.Height - 1) / BLOCK_SIZE;
		for( int y = starty; y <= endy; y++ ) {
			for( int x = startx; x <= endx; x++ ) {
				int phase = basePhase - (x - y) * TURN_WIDTH_FACTOR;
				if(phase < 0) phase = 0;
				if(phase > 63) phase = 63;
				final int gl = GLOSS[phase];
				//if( y * BLOCK_SIZE + BLOCK_SIZE >= mHeight || x == startx || x == endx ) {
				{
					// 下側がアクセスオーバーランの可能性がある
					// あるいは 左端 右端のブロック
					int l = (x) * BLOCK_SIZE;
					int t = (y) * BLOCK_SIZE;
					int r = l + BLOCK_SIZE;
					int b = t + BLOCK_SIZE;
					if(l < left) l = left;
					if(r > right) r = right;
					if(t < top) t = top;
					if(b > bottom) b = bottom;
					if( (l < r) && (t < b) ) {
						// l, t, r, b は既にクリップされた領域を表している
						// phase を決定
						if( phase == 0 ) {
							// 完全に src1
							c.setMatrix(transMatix[0]);
							srcRect.set( l, t, r, b );
							dstRect.set( l+destxofs, t+destyofs, r+destxofs, b+destyofs );
							c.drawBitmap( src1, srcRect, dstRect, paint );
						} else if(phase == 63) {
							// 完全に src2
							c.setMatrix(transMatix[0]);
							srcRect.set( l, t, r, b );
							dstRect.set( l+destxofs, t+destyofs, r+destxofs, b+destyofs );
							c.drawBitmap( src2, srcRect, dstRect, paint );
						} else {
							Bitmap src;
							Matrix mat;
							float scale;
							if( phase < 32 ) {
								src = src1;
								mat = transMatix[phase];
								scale = scales[phase];
							} else {
								src = src2;
								mat = transMatix[63-phase];
								scale = scales[63-phase];
							}
							if( x != y ) {
								// 中心からずれている, どの程度ずれているかによって移動量を考える
								//mat.getValues(values);
								//float scale = values[Matrix.MSCALE_X];
								float ox = (l - l*scale);
								float oy = (t - t*scale);
								trans.reset();
								trans.setTranslate(ox-oy, -ox+oy);
								trans.preConcat(mat);
								mat = trans;
							}
							// まず背景を塗る
							c.setMatrix(transMatix[0]);
							paint.setColor(bgColor);
							c.drawRect( l+destxofs, t+destyofs, r+destxofs, b+destyofs, paint );
							if( gl != 0 ) {
								c.setMatrix(mat);
								srcRect.set( l, t, r, b );
								dstRect.set( l+destxofs, t+destyofs, r+destxofs, b+destyofs );
								c.drawBitmap( src, srcRect, dstRect, paint);
								paint.setColor( Color.WHITE );
								paint.setAlpha(gl);
								c.drawRect( l+destxofs, t+destyofs, r+destxofs, b+destyofs, paint );
								paint.setAlpha(255);
							} else {
								c.setMatrix(mat);
								srcRect.set( l, t, r, b );
								dstRect.set( l+destxofs, t+destyofs, r+destxofs, b+destyofs );
								c.drawBitmap( src, srcRect, dstRect, paint );
							}
						}
					}
				}
			}
		}
		return Error.S_OK;
	}

	@Override
	public int makeFinalImage(ScanLineProvider dest, ScanLineProvider src1, ScanLineProvider src2) throws TJSException {
		// 常に最終画像は src2
		dest.copyFrom( src2 );
		return Error.S_OK;
	}

}
