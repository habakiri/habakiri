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
import jp.kirikiri.tvp2.msg.Message;
import jp.kirikiri.tvp2.visual.BaseTransHandler;
import jp.kirikiri.tvp2.visual.ScanLineProvider;
import jp.kirikiri.tvp2.visual.SimpleImageProvider;
import jp.kirikiri.tvp2.visual.SimpleOptionProvider;

public class UniversalTransHandlerProvider extends CrossFadeTransHandlerProvider {

	private static final String VAGUE = "vague";
	private static final String RULE = "rule";

	private String[] mStringValueRet;

	public UniversalTransHandlerProvider() {
		super();
		mStringValueRet = new String[1];
	}
	@Override
	public String getName() { return "universal"; }

	protected BaseTransHandler getTransitionObject( SimpleOptionProvider options,
			SimpleImageProvider imagepro, int layertype, int src1w, int src1h,
			int src2w, int src2h ) throws TJSException {

		// retrieve "time" option
		int er = options.getAsNumber( TIME, mLogValueRet );
		if( er < 0 ) Message.throwExceptionMessage( Message.SpecifyOption, TIME );
		long time = mLogValueRet[0];
		if( time < 2) time = 2; // too small time may cause problem

		// retrieve "vague" option
		er = options.getAsNumber( VAGUE, mLogValueRet );
		int vague = (int) mLogValueRet[0];
		if( er < 0 ) vague = 64;

		// retrieve "rule" option and load it as an image
		er = options.getAsString( RULE, mStringValueRet );
		if( er < 0 ) Message.throwExceptionMessage( Message.SpecifyOption, RULE );
		String rulename = mStringValueRet[0];
		mStringValueRet[0] = null;
		ScanLineProvider scpro = imagepro.loadImage( rulename, 8, 0x02ffffff, src1w, src1h );
		if( scpro == null ) Message.throwExceptionMessage( Message.CannotLoadRuleGraphic, rulename );

		return new UniversalTransHander( options, layertype, time, vague, scpro );
	}
}
