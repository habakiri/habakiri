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
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import jp.kirikiri.tjs2.Error;
import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tvp2.visual.DivisibleData;
import jp.kirikiri.tvp2.visual.DivisibleTransHandler;
import jp.kirikiri.tvp2.visual.ScanLineProvider;
import jp.kirikiri.tvp2.visual.SimpleOptionProvider;

/**
 * 'モザイク' トランジション
 * 矩形モザイクによるトランジション
 *
 * オリジナルとは挙動が少し異なるが、近い結果にはなると思われる。
 * 縮小コピーと拡大を使った実装になっている。
 * モザイク化時、オリジナルでは平均色ではないので、これもニアレストネイバーで処理している。
 * バイリニアはコメントアウトしているが、そちらの方が少し綺麗に(色が薄く)なる。
 */
public class MosaicTransHandler implements DivisibleTransHandler {

	private long mStartTick; // トランジションを開始した tick count
	private long mTime; // トランジションに要する時間
	private long mHalfTime; // トランジションに要する時間 / 2
	private long mCurTime; // 現在の時間
	//private int mWidth; // 処理する画像の幅
	//private int mHeight; // 処理する画像の高さ
	//private int mCurOfsX; // ブロックオフセット X
	//private int mCurOfsY; // ブロックオフセット Y
	private int mMaxBlockSize; // 最大のブロック幅
	private int mCurBlockSize; // 現在のブロック幅
	private int mBlendRatio; // ブレンド比
	private boolean mFirst; // 一番最初の呼び出しかどうか
	private BufferedImage mWorkImage;

	public MosaicTransHandler(long time, int width, int height, int maxblocksize) {

		//mWidth = width;
		//mHeight = height;
		mTime = time;
		mHalfTime = time / 2;
		mMaxBlockSize = maxblocksize;

		mFirst = true;

		mWorkImage = new BufferedImage( width, height, BufferedImage.TYPE_INT_ARGB );
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
		long t = mCurTime = (tick - mStartTick);
		if(mCurTime > mTime) mCurTime = mTime;
		if(t >= mHalfTime) t = mTime - t;
		if(t < 0) t = 0;
		mCurBlockSize = (int) ((mMaxBlockSize-2) * t / mHalfTime + 2);

		// BlendRatio
		mBlendRatio = (int) (mCurTime * 255 / mTime);
		if(mBlendRatio > 255) mBlendRatio = 255;

		// 中心のブロックを本当に中心に持ってこられるように CurOfsX と CurOfsY を調整
		/*
		int x = mWidth / 2;
		int y = mHeight / 2;
		x /= mCurBlockSize;
		y /= mCurBlockSize;
		x *= mCurBlockSize;
		y *= mCurBlockSize;
		mCurOfsX = (mWidth - mCurBlockSize) / 2 - x;
		mCurOfsY = (mHeight - mCurBlockSize) / 2 - y;
		if(mCurOfsX > 0) mCurOfsX -= mCurBlockSize;
		if(mCurOfsY > 0) mCurOfsY -= mCurBlockSize;
		*/

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
	 * @param data には領域や画像に関する情報が入っている
	 *
	 * data.Left, data.Top, data.Width, data.Height で示される矩形に
	 * のみ転送する必要がある。
	 */
	@Override
	public int process(DivisibleData data) {
		BufferedImage dest = (BufferedImage)data.Dest.getScanLineForWrite().getImage();
		BufferedImage src1 = (BufferedImage)data.Src1.getScanLine().getImage();
		BufferedImage src2 = (BufferedImage)data.Src2.getScanLine().getImage();
		final int bs = mCurBlockSize;
		final int hb = bs/2;
		final int destLeft = data.DestLeft - hb;
		final int src1Left = data.Src1Left;
		final int src2Left = data.Src2Left;
		final int h = data.Height;
		final int w = data.Width;
		final int destTop = data.DestTop - hb;
		final int src1Top = data.Src1Top;
		final int src2Top = data.Src2Top;
		final float opa = ((float)mBlendRatio)/255.0f;
		final int bw = w / bs;
		final int bh = h / bs;

		Graphics2D wg = (Graphics2D)mWorkImage.getGraphics();
		wg.setComposite( AlphaComposite.Src );
		//wg.setRenderingHint(RenderingHints.KEY_INTERPOLATION , RenderingHints.VALUE_INTERPOLATION_BILINEAR ); // バイリニア
		wg.setRenderingHint(RenderingHints.KEY_INTERPOLATION , RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR ); // ニアレストネイバー
		wg.drawImage( src1, 0, 0, bw, bh, src1Left, src1Top, src1Left+w, src1Top+h, null );

		Graphics2D g = (Graphics2D)dest.getGraphics();
		g.setComposite( AlphaComposite.Src );
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION , RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR ); // ニアレストネイバー
		g.drawImage( mWorkImage, destLeft, destTop, destLeft+w, destTop+h, 0, 0, bw, bh, null );
		g.drawImage( mWorkImage, destLeft+w, destTop, destLeft+w+hb, destTop+h, bw-1, 0, bw, bh, null );
		g.drawImage( mWorkImage, destLeft, destTop+h, destLeft+w, destTop+h+hb, 0, bh-1, bw, bh, null );
		g.drawImage( mWorkImage, destLeft+w, destTop+h, destLeft+w+hb, destTop+h+hb, bw-1, bh-1, bw, bh, null );

		wg.drawImage( src2, 0, 0, bw, bh, src2Left, src2Top, src2Left+w, src2Top+h, null );
		wg.dispose();

		Composite composite = AlphaComposite.getInstance( AlphaComposite.SRC_OVER, opa );
		g.setComposite( composite );
		g.drawImage( mWorkImage, destLeft, destTop, destLeft+w, destTop+h, 0, 0, bw, bh, null );
		g.drawImage( mWorkImage, destLeft+w, destTop, destLeft+w+hb, destTop+h, bw-1, 0, bw, bh, null );
		g.drawImage( mWorkImage, destLeft, destTop+h, destLeft+w, destTop+h+hb, 0, bh-1, bw, bh, null );
		g.drawImage( mWorkImage, destLeft+w, destTop+h, destLeft+w+hb, destTop+h+hb, bw-1, bh-1, bw, bh, null );
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION , RenderingHints.VALUE_INTERPOLATION_BILINEAR ); // バイリニア
		g.dispose();

		return Error.S_OK;
	}

	@Override
	public int makeFinalImage(ScanLineProvider dest, ScanLineProvider src1, ScanLineProvider src2) throws TJSException {
		// 常に最終画像は src2
		dest.copyFrom( src2 );
		return Error.S_OK;
	}
}
