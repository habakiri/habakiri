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
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import jp.kirikiri.tjs2.Error;
import jp.kirikiri.tvp2.visual.DivisibleData;

/**
 * 画面の中心で回転しながらズームイン、あるいはズームアウトするトランジション
 * (KAGでは)裏画面が常に回転してズームイン、あるいはズームアウトする
 */
public class RotateZoomTransHandler extends BaseRotateTransHandler {

	private double mFactor; // 初期ズーム拡大率
	private double mTargetFactor; // 最終ズーム拡大率
	private double mAccel; // 加速度的な動きを行わせるかどうか ( 0 = 行わない )
	private double mTwist; // 初期回転位置
	private double mTwistAccel; // 回転に加速度的な動きを行わせるかどうか
	private int mCenterX; // 回転中心 X 位置
	private int mCenterY; // 回転中心 Y 位置
	private boolean mFixSrc1; // src1 を固定するか

	private AffineTransform mSrc1Mat;
	private AffineTransform mSrc2Mat;

	public RotateZoomTransHandler(long time, int width, int height, double factor, double targetfactor, double accel, double twist, double twistaccel, int centerx, int centery, boolean fixsrc1 ) {
		super(time, width, height, 0);
		mFactor = factor;
		mTargetFactor = targetfactor;
		mAccel = accel;
		mTwist = twist;
		mTwistAccel = twistaccel;
		mCenterX = centerx;
		mCenterY = centery;
		mFixSrc1 = fixsrc1;
		mSrc1Mat = new AffineTransform();
	}

	@Override
	void calcPosition() {
		// src1, src2 の画面位置を設定する

		// src1
		// src1 は常に画面全体固定

		// src2
		float zm = (float)(int)mCurTime / (float)(int)mTime;
		float tm = zm;
		if(mAccel < 0) {
			// 上弦 ( 最初が動きが早く、徐々に遅くなる )
			zm = 1.0f - zm;
			zm = (float) Math.pow(zm, -mAccel);
			zm = 1.0f - zm;
		} else if(mAccel > 0) {
			// 下弦 ( 最初は動きが遅く、徐々に早くなる )
			zm = (float) Math.pow(zm, mAccel);
		}

		float scx = mWidth/2.0f;
		float scy = mHeight/2.0f;
		float cx = ((scx - mCenterX) * zm + mCenterX);
		float cy = ((scy - mCenterY) * zm + mCenterY);

		if( mTwistAccel < 0 ) {
			// 上弦 ( 最初が動きが早く、徐々に遅くなる )
			tm = 1.0f - tm;
			tm = (float) Math.pow(tm, -mTwistAccel);
			tm = 1.0f - tm;
		} else if( mTwistAccel > 0 ) {
			// 下弦 ( 最初は動きが遅く、徐々に早くなる )
			tm = (float) Math.pow(tm, mTwistAccel);
		}

		float rad = (float) (mCurTime == mTime ? 0 : 2 * Math.PI * mTwist * tm);
		zm = (float) ((mTargetFactor - mFactor) * zm + mFactor);

		AffineTransform rot = AffineTransform.getRotateInstance( -rad, cx, cy );
		AffineTransform scale = AffineTransform.getScaleInstance(zm, zm);
		scale.concatenate(rot);
		if( zm != 0.0f && zm != 1.0f ) {
			cx = cx - cx * zm;
			cy = cy - cy * zm;
			AffineTransform trans = AffineTransform.getTranslateInstance(cx, cy);
			trans.concatenate(scale);
			mSrc2Mat = trans;
		} else {
			mSrc2Mat = scale;
		}
	}

	@Override
	public int process(DivisibleData data) {
		BufferedImage dest = (BufferedImage)data.Dest.getScanLineForWrite().getImage();
		BufferedImage src1 = (BufferedImage)data.Src1.getScanLine().getImage();
		BufferedImage src2 = (BufferedImage)data.Src2.getScanLine().getImage();
		int destLeft = data.DestLeft;
		int src1Left = data.Src1Left;
		int src2Left = data.Src2Left;
		int h = data.Height;
		int w = data.Width;
		int destTop = data.DestTop;
		int src1Top = data.Src1Top;
		int src2Top = data.Src2Top;

		Graphics2D g = (Graphics2D)dest.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION , RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR ); // ニアレストネイバー
		g.setComposite( AlphaComposite.Src );
		g.setTransform(mSrc1Mat);
		if( mFixSrc1 ) {
			g.drawImage( src1, destLeft, destTop, destLeft+w, destTop+h, src1Left, src1Top, src1Left+w, src1Top+h, null );
		} else {
			g.drawImage( src2, destLeft, destTop, destLeft+w, destTop+h, src1Left, src1Top, src1Left+w, src1Top+h, null );
		}

		g.setTransform(mSrc2Mat);
		if( mFixSrc1 ) {
			g.drawImage( src2, destLeft, destTop, destLeft+w, destTop+h, src2Left, src2Top, src2Left+w, src2Top+h, null );
		} else {
			g.drawImage( src1, destLeft, destTop, destLeft+w, destTop+h, src2Left, src2Top, src2Left+w, src2Top+h, null );
		}
		g.dispose();

		return Error.S_OK;
	}
}
