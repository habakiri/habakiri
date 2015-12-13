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

import jp.kirikiri.tjs2.Error;
import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tvp2.visual.DivisibleTransHandler;
import jp.kirikiri.tvp2.visual.ScanLineProvider;
import jp.kirikiri.tvp2.visual.SimpleOptionProvider;

/**
 * 回転を行うトランジションハンドラ基底クラスの実装
 */
public abstract class BaseRotateTransHandler implements DivisibleTransHandler {

	protected long mStartTick; // トランジションを開始した tick count
	protected long mTime; // トランジションに要する時間
	protected long mCurTime; // 現在の時間
	protected int mWidth; // 処理する画像の幅
	protected int mHeight; // 処理する画像の高さ
	protected int mBGColor; // 背景色
	protected int mPhase; // アニメーションのフェーズ
	protected boolean mFirst; // 一番最初の呼び出しかどうか

	public BaseRotateTransHandler( long time, int width, int height, int bgcolor ) {
		mWidth = width;
		mHeight = height;
		mTime = time;
		mBGColor = bgcolor | 0xff000000;

		//mDrawData = new RotateDrawData[height];

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
		mCurTime = (tick - mStartTick);
		if(mCurTime > mTime) mCurTime = mTime;

		/*
		// データをクリア
		for( int i = 0; i < mHeight; i++) {
			// 背景でクリア
			DrawData[i].count = 1;
			DrawData[i].region[0].left = 0;
			DrawData[i].region[0].right = mWidth;
			DrawData[i].region[0].type = 0; // 0 = 背景
		}
		*/

		calcPosition(); // 下位クラスの CalcPosition メソッドを呼ぶ
		return Error.S_TRUE;
	}

	/**
	 * トランジションの画面更新一回分が終わるごとに呼ばれる
	 */
	@Override
	public int endProcess() {
		if(mCurTime == mTime) return Error.S_FALSE; // トランジション終了
		return Error.S_TRUE;
	}

	@Override
	public int makeFinalImage(ScanLineProvider dest, ScanLineProvider src1, ScanLineProvider src2) throws TJSException {
		// final image is the source2 bitmap
		dest.copyFrom( src2 );
		return Error.S_OK;
	}

	abstract void calcPosition();
}
