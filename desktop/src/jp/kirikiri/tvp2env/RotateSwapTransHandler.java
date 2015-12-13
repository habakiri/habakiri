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
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import jp.kirikiri.tjs2.Error;
import jp.kirikiri.tvp2.visual.DivisibleData;

/**
 * 表画面が回転しながら遠ざかり、裏画面が回転しながら近寄るトランジション
 *
 * オリジナルでは、遠ざかる時水平方向に少し倒れたようになるが、これは手前を向いたままなので、少し見た目が違う。
 */
public class RotateSwapTransHandler extends BaseRotateTransHandler {

	private boolean mSrc1First; // src1 を先に描画するかどうか

	private AffineTransform mSrc1Mat;
	private AffineTransform mSrc2Mat;
	private float mTwist;
	private Color mBGColorObj;

	public RotateSwapTransHandler(long time, int width, int height, int bgcolor, double twist ) {
		super(time, width, height, bgcolor);
		mTwist = (float) (twist * Math.PI * 2.0);
		mBGColorObj = new Color( bgcolor|0xff000000, true );
	}

	@Override
	public int process(DivisibleData data) {
		BufferedImage dest = (BufferedImage)data.Dest.getScanLineForWrite().getImage();
		BufferedImage src1 = (BufferedImage)data.Src1.getScanLine().getImage();
		BufferedImage src2 = (BufferedImage)data.Src2.getScanLine().getImage();
		final int destLeft = data.DestLeft;
		final int src1Left = data.Src1Left;
		final int src2Left = data.Src2Left;
		final int h = data.Height;
		final int w = data.Width;
		final int destTop = data.DestTop;
		final int src1Top = data.Src1Top;
		final int src2Top = data.Src2Top;

		Graphics2D g = (Graphics2D)dest.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION , RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR ); // ニアレストネイバー
		g.setComposite( AlphaComposite.Src );
		g.setColor( mBGColorObj );
		g.fillRect( destLeft, destTop, w, h );

		g.setTransform(mSrc1Mat);
		if( mSrc1First ) {
			g.drawImage( src1, destLeft, destTop, destLeft+w, destTop+h, src1Left, src1Top, src1Left+w, src1Top+h, null );
		} else {
			g.drawImage( src2, destLeft, destTop, destLeft+w, destTop+h, src1Left, src1Top, src1Left+w, src1Top+h, null );
		}

		g.setTransform(mSrc2Mat);
		if( mSrc1First ) {
			g.drawImage( src2, destLeft, destTop, destLeft+w, destTop+h, src2Left, src2Top, src2Left+w, src2Top+h, null );
		} else {
			g.drawImage( src1, destLeft, destTop, destLeft+w, destTop+h, src2Left, src2Top, src2Left+w, src2Top+h, null );
		}
		g.dispose();

		return Error.S_OK;
	}

	@Override
	void calcPosition() {
		float rad;
		float cx, cy;
		final int scx = mWidth/2;
		final int scy = mHeight/2;
		final float zm = (float)(int)mCurTime / (float)(int)mTime;
		float tm;//, s, c;
		mSrc1First = ( mCurTime >= mTime / 2 );

		// src1
		tm = zm * zm;
		cx = (float) (( - scx ) * tm + scx + Math.sin(tm * Math.PI)*scx*1.5f);
		cy = ( - scy ) * tm + scy;
		rad = tm * mTwist;
		tm = 1.0f - tm;
		//s = (float) (Math.sin(rad) * tm);
		//c = (float) (Math.cos(rad) * tm);
		AffineTransform mat;
		AffineTransform rot = AffineTransform.getRotateInstance( -rad, cx, cy );
		AffineTransform scale = AffineTransform.getScaleInstance(tm, tm);
		scale.concatenate(rot);
		if( tm != 0.0f && tm != 1.0f ) {
			cx = cx - cx * tm;
			cy = cy - cy * tm;
			AffineTransform trans = AffineTransform.getTranslateInstance(cx, cy);
			trans.concatenate(scale);
			mat = trans;
		} else {
			mat = scale;
		}
		if( mSrc1First ) {
			mSrc1Mat = mat;
		} else {
			mSrc2Mat = mat;
		}

		// src2
		tm = 1.0f - (1.0f - zm) * (1.0f - zm);
		cx = (float) ((scx - (mWidth  - 1)) * tm + (mWidth  - 1) - Math.sin(tm * Math.PI) * scx*1.5f);
		cy = (scy - (mHeight - 1)) * tm + (mHeight - 1);
		rad = (-1.0f + tm) * mTwist;
		//s = sin(rad) * tm;
		//c = cos(rad) * tm;
		rot = AffineTransform.getRotateInstance( -rad, cx, cy );
		scale = AffineTransform.getScaleInstance(tm, tm);
		scale.concatenate(rot);
		if( tm != 0.0f && tm != 1.0f ) {
			cx = cx - cx * tm;
			cy = cy - cy * tm;
			AffineTransform trans = AffineTransform.getTranslateInstance(cx, cy);
			trans.concatenate(scale);
			mat = trans;
		} else {
			mat = scale;
		}
		if( mSrc1First ) {
			mSrc2Mat = mat;
		} else {
			mSrc1Mat = mat;
		}
	}

}
