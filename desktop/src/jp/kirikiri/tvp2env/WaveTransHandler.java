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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import jp.kirikiri.tjs2.Error;
import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tvp2.visual.DivisibleData;
import jp.kirikiri.tvp2.visual.DivisibleTransHandler;
import jp.kirikiri.tvp2.visual.ScanLineProvider;
import jp.kirikiri.tvp2.visual.SimpleOptionProvider;

/**
 * 波トランジション
 * ラスタスクロールによるトランジション
 */
public class WaveTransHandler implements DivisibleTransHandler {

	/** トランジションを開始した tick count */
	protected long mStartTick;

	/** トランジションに要する時間 / 2 */
	protected long mHalfTime;

	/** トランジションに要する時間 */
	protected long mTime;

	/** レイヤタイプ */
	protected int mLayerType;

	/** 処理する画像の幅 */
	protected int mWidth;

	/** 処理する画像の高さ */
	protected int mHeight;

	/** 最大振幅 */
	protected int mMaxH;

	/** 最大角速度 */
	protected double mMaxOmega;

	/** 現在の振幅 */
	protected int mCurH;

	/** 現在の角速度 */
	protected double mCurOmega;

	/** 角開始位置 */
	protected double mCurRadStart;

	/** 現在の tick count */
	protected long mCurTime;

	/** ブレンド比 */
	protected int mBlendRatio;

	/** 背景色その１ */
	protected int mBGColor1;

	/** 背景色その２ */
	protected int mBGColor2;

	/** 現在の背景色 */
	protected int mCurBGColor;

	/** 0 = 最初と最後 1 = 最初 2 = 最後 が波が細かい */
	protected int mWaveType;

	/** 一番最初の呼び出しかどうか */
	protected boolean mFirst;

	private Color mCurrentBGColor;
	private Composite mBlendComposite;

	public WaveTransHandler(long time, int layertype, int width, int height,
			int maxh, double maxomega, int bgcolor1, int bgcolor2, int wavetype) {

		mLayerType = layertype;
		mWidth = width;
		mHeight = height;
		mTime = time;
		mHalfTime = time / 2;
		mMaxH = maxh;
		mMaxOmega = maxomega;
		mBGColor1 = bgcolor1 | 0xff000000;
		mBGColor2 = bgcolor2 | 0xff000000;
		mWaveType = wavetype;

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

		// 画像演算に必要な各パラメータを計算
		long t = mCurTime = (tick - mStartTick);
		if(mCurTime > mTime) mCurTime = mTime;
		if(t >= mHalfTime) t = mTime - t;
		if(t < 0) t = 0;

		double tt = Math.sin((3.14159265358979/2.0) * t / mHalfTime);

		// CurH, CurOmega, CurRadStart
		mCurH = (int) (tt * mMaxH);
		switch(mWaveType) {
		case 0: // 最初と最後が波が細かい
			mCurOmega = mMaxOmega * tt;
			break;
		case 1: // 最初が波が細かい
			mCurOmega = mMaxOmega * ((long)mTime - mCurTime) / (long)mTime;
			break;
		case 2: // 最後が波が細かい
			mCurOmega = mMaxOmega * mCurTime / (long)mTime;
			break;
		}
		mCurRadStart = -mCurOmega * (mHeight / 2);

		// BlendRatio
		mBlendRatio = (int) (mCurTime * 255 / mTime);
		if(mBlendRatio > 255) mBlendRatio = 255;

		// 背景色のブレンド
		int oldColor = mCurBGColor;
		mCurBGColor = blend(mBGColor1, mBGColor2, mBlendRatio);

		if( oldColor != mCurBGColor || mCurrentBGColor == null ) {
			mCurrentBGColor = new Color(mCurBGColor,true);
		}
		mBlendComposite = AlphaComposite.getInstance( AlphaComposite.SRC_OVER, (float)mBlendRatio/255.0f );

		return Error.S_TRUE;
	}

	/**
	 * トランジションの画面更新一回分が終わるごとに呼ばれる
	 */
	@Override
	public int endProcess() {
		if(mBlendRatio == 255) return Error.S_FALSE; // トランジション終了
		return Error.S_TRUE;
	}

	/**
	 * トランジションの各領域ごとに呼ばれる
	 * 吉里吉里は画面を更新するときにいくつかの領域に分割しながら処理を行うので
	 * このメソッドは通常、画面更新一回につき複数回呼ばれる
	 *
	 * @param data 領域や画像に関する情報
	 */
	@Override
	public int process(DivisibleData data) {
		// 初期パラメータを計算
		double rad = data.Top * mCurOmega + mCurRadStart; // 角度

		// スキャンライン
		BufferedImage dest = (BufferedImage)data.Dest.getScanLineForWrite().getImage();
		BufferedImage src1 = (BufferedImage)data.Src1.getScanLine().getImage();
		BufferedImage src2 = (BufferedImage)data.Src2.getScanLine().getImage();

		final int destLeft = data.DestLeft;
		//final int src1Left = data.Src1Left;
		//final int src2Left = data.Src2Left;
		final int left = data.Left;
		int top = data.Top;
		//final int h = data.Height;
		final int w = data.Width;
		final int right = left + w;
		int destTop = data.DestTop;
		//final int src1Top = data.Src1Top;
		//final int src2Top = data.Src2Top;
		Graphics2D g = (Graphics2D)dest.getGraphics();

		// ラインごとに処理
		for( int n = 0; n < data.Height; n++, rad += mCurOmega ) {
			// ズレ位置
			int d = (int)(Math.sin(rad) * mCurH);

			// 転送
			int l, r;

			// ここでやるべきことは、data.Src1 と data.Src2 の (0, data.Top + n) から
			// 始まる１ラインを BlendRatio によってブレンドし、(d, data.Top + n) に
			// 転送する。はみ出て描画されない部分は CurBGColor で塗りつぶす。
			// ただし、左右は data.Left と data.Width によってクリッピングされる。
			// また、data.Dest に転送するときは、そのオフセットは (data.Left, data.Top)
			// ではなくて(data.DestLeft, data.DestTop) になるので補正する。


			// 左側のずれる部分に背景色を転送
			if( d > 0 ) {
				l = 0;
				r = d;

				if( l < left ) l = left;
				if( r > right ) r = right;
				if( l < r ) {
					g.setComposite( AlphaComposite.Src );
					g.setColor( mCurrentBGColor );
					g.fillRect( l + destLeft - left, destTop, r - l, 1 );
				}
			}

			// 左端のずれる部分に背景色を転送
			if( d < 0 ) {
				l = d + mWidth;
				r = mWidth;
				if( l < left ) l = left;
				if( r > right ) r = right;
				if( l < r ) {
					g.setComposite( AlphaComposite.Src );
					g.setColor( mCurrentBGColor );
					g.fillRect( l + destLeft - left, destTop, r - l, 1 );
				}
			}

			// ブレンドしながら転送
			// TVPConstAlphaBlend_SD(dest, src1, src2, len, opa)
			// は dest に src1 と src2 を opa で指定した混合比で混合して転送する
			l = d;
			r = mWidth + d;
			if( l < left ) l = left;
			if( r > right ) r = right;
			if( l < r ) {
				final int width = r - l;
				final int dl = l+destLeft-left;
				final int dr = dl + width;
				final int db = destTop+1;
				final int sl = l-d;
				final int sr = sl + width;
				final int sb = top + 1;
				g.setComposite( AlphaComposite.Src );
				g.drawImage( src1, dl, destTop, dr, db, sl, top, sr, sb, null );

				g.setComposite( mBlendComposite );
				g.drawImage( src2, dl, destTop, dr, db, sl, top, sr, sb, null );

				/*
				if(mLayerType == LayerType.ltAlpha )
					TVPConstAlphaBlend_SD_d(dest + l + data.DestLeft - data.Left, src1 + l - d, src2 + l - d, r - l, mBlendRatio);
				else if(mLayerType == LayerType.ltAddAlpha )
					TVPConstAlphaBlend_SD_a(dest + l + data.DestLeft - data.Left, src1 + l - d, src2 + l - d, r - l, mBlendRatio);
				else
					TVPConstAlphaBlend_SD(dest + l + data.DestLeft - data.Left, src1 + l - d, src2 + l - d, r - l, mBlendRatio);
				*/
					/*
						転送先がαを持っている場合はブレンドアルゴリズムが違うので
						注意する必要がある。
						_d のサフィックスを持つブレンド関数はすべて通常のαブレンドで、
						α値を考慮したブレンドを行う。同様に _a のサフィックスを持つ
						ブレンド関数は加算αブレンドである。_a や _d サフィックスを持
						たないブレンド関数に比べて低速。_d や _a サフィックスを持たな
						いブレンド関数はα値は扱わない ( 常に完全に不透明であると扱われる )。
					*/
			}
			destTop++;
			top++;
		}
		g.dispose();

		return Error.S_OK;
	}

	@Override
	public int makeFinalImage(ScanLineProvider dest, ScanLineProvider src1, ScanLineProvider src2) throws TJSException {
		// 常に最終画像は src2
		dest.copyFrom( src2 );
		return Error.S_OK;
	}

	static public int blend( int a, int b, int opa ) {
		// a と b を混合比 opa で混合して返す ( opa = 0 ～ 255, 0 = a, 255 = b )
		int ret;
		int tmp;

		tmp = a & 0x000000ff;  ret   = 0x000000ff & (tmp + (( (b & 0x000000ff) - tmp ) * opa >>> 8));
		tmp = a & 0x0000ff00;  ret  |= 0x0000ff00 & (tmp + (( (b & 0x0000ff00) - tmp ) * opa >>> 8));
		tmp = a & 0x00ff0000;  ret  |= 0x00ff0000 & (tmp + (( (b & 0x00ff0000) - tmp ) * opa >>> 8));
		tmp = a >>> 24;
		ret  |= (0x000000ff & (tmp + (( (b >>> 24) - tmp ) * opa >>> 8))) << 24;

		return ret;
	}
}
