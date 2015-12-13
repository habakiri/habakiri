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

import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tjs2.Variant;
import jp.kirikiri.tvp2.msg.Message;
import jp.kirikiri.tvp2.visual.BaseTransHandler;
import jp.kirikiri.tvp2.visual.SimpleImageProvider;
import jp.kirikiri.tvp2.visual.SimpleOptionProvider;
import jp.kirikiri.tvp2.visual.TransHandlerProvider;

public class RotateSwapTransHandlerProvider implements TransHandlerProvider {

	private static final String TIME = "time";
	private static final String BGCOLOR = "bgcolor";
	private static final String TWIST = "twist";

	private Variant mVariantRet;
	protected long[] mLogValueRet;

	public RotateSwapTransHandlerProvider() {
		mVariantRet = new Variant();
		mLogValueRet = new long[1];
	}

	@Override
	public String getName() { return "rotateswap"; }

	@Override
	public BaseTransHandler startTransition(SimpleOptionProvider options,
			SimpleImageProvider imagepro, int layertype, int src1w, int src1h,
			int src2w, int src2h, int[] type) throws TJSException {

		if( type != null && type.length >= 1 ) type[0] = ttExchange; // transition type : exchange
		if( type != null && type.length >= 2 ) type[1] = tutDivisible; // update type : divisible
			// update type : divisible fade
		if( options == null ) return null;

		if(src1w != src2w || src1h != src2h)
			Message.throwExceptionMessage( Message.TransitionLayerSizeMismatch,
				 String.valueOf(src2w) + "x" + src2h,
				 String.valueOf(src1w) + "x" + src1h );

		// オプションを得る
		// retrieve "time" option
		int er = options.getAsNumber( TIME, mLogValueRet );
		if( er < 0 ) Message.throwExceptionMessage( Message.SpecifyOption, TIME );
		long time = mLogValueRet[0];
		if( time < 2) time = 2; // あまり小さな数値を指定すると問題が起きるので

		er = options.getAsNumber( BGCOLOR, mLogValueRet );
		int bgcolor = 0;
		if( er >= 0 ) {
			bgcolor = (int) mLogValueRet[0];
		}

		er = options.getValue( TWIST, mVariantRet );
		double twist = 1.0;
		if( er >= 0 && mVariantRet.isVoid() != true ) {
			twist = mVariantRet.asDouble();
		}
		mVariantRet.clear();

		// オブジェクトを作成
		return new RotateSwapTransHandler(time, src1w, src1h, bgcolor, twist );
	}

}
