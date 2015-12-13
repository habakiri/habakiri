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
package jp.kirikiri.tvp2.visual;

public class LayerType {
	static public final int
		ltBinder = 0,
		ltCoverRect = 1,
		ltOpaque = 1, // the same as ltCoverRect
		ltTransparent = 2, // alpha blend
		ltAlpha = 2, // the same as ltTransparent
		ltAdditive = 3,
		ltSubtractive = 4,
		ltMultiplicative = 5,
		ltEffect = 6,
		ltFilter = 7,
		ltDodge = 8,
		ltDarken = 9,
		ltLighten = 10,
		ltScreen = 11,
		ltAddAlpha = 12, // additive alpha blend
		ltPsNormal = 13,
		ltPsAdditive = 14,
		ltPsSubtractive = 15,
		ltPsMultiplicative = 16,
		ltPsScreen = 17,
		ltPsOverlay = 18,
		ltPsHardLight = 19,
		ltPsSoftLight = 20,
		ltPsColorDodge = 21,
		ltPsColorDodge5 = 22,
		ltPsColorBurn = 23,
		ltPsLighten = 24,
		ltPsDarken = 25,
		ltPsDifference = 26,
		ltPsDifference5 = 27,
		ltPsExclusion = 28;

	static public final int
		omPsNormal = ltPsNormal,
		omPsAdditive = ltPsAdditive,
		omPsSubtractive = ltPsSubtractive,
		omPsMultiplicative = ltPsMultiplicative,
		omPsScreen = ltPsScreen,
		omPsOverlay = ltPsOverlay,
		omPsHardLight = ltPsHardLight,
		omPsSoftLight = ltPsSoftLight,
		omPsColorDodge = ltPsColorDodge,
		omPsColorDodge5 = ltPsColorDodge5,
		omPsColorBurn = ltPsColorBurn,
		omPsLighten = ltPsLighten,
		omPsDarken = ltPsDarken,
		omPsDifference = ltPsDifference,
		omPsDifference5 = ltPsDifference5,
		omPsExclusion = ltPsExclusion,
		omAdditive = ltAdditive,
		omSubtractive = ltSubtractive,
		omMultiplicative = ltMultiplicative,
		omDodge = ltDodge,
		omDarken = ltDarken,
		omLighten = ltLighten,
		omScreen = ltScreen,
		omAlpha = ltTransparent,
		omAddAlpha = ltAddAlpha,
		omOpaque = ltCoverRect,

		omAuto = 128;	// operation mode is guessed from the source layer type

	static public final int
		/** primal method; nearest neighbor method */
		stNearest = 0,
		/** fast linear interpolation (does not have so much precision) */
		stFastLinear = 1,
		/** (strict) linear interpolation */
		stLinear = 2,
		/** cubic interpolation */
		stCubic = 3,

		/** stretch type mask */
		stTypeMask = 0xf,
		/** flag mask */
		stFlagMask = 0xf0,

		/** referencing source is not limited by the given rectangle
		 * (may allow to see the border pixel to interpolate) */
		stRefNoClip = 0x10;

	static final public boolean isTypeUsingAlpha( int type ) {
		return
			type == ltAlpha				||
			type == ltPsNormal			||
			type == ltPsAdditive		||
			type == ltPsSubtractive		||
			type == ltPsMultiplicative	||
			type == ltPsScreen			||
			type == ltPsOverlay			||
			type == ltPsHardLight		||
			type == ltPsSoftLight		||
			type == ltPsColorDodge		||
			type == ltPsColorDodge5		||
			type == ltPsColorBurn		||
			type == ltPsLighten			||
			type == ltPsDarken			||
			type == ltPsDifference		||
			type == ltPsDifference5		||
			type == ltPsExclusion		;
	}

	static final public boolean isTypeUsingAddAlpha( int  type ) {
		return type == ltAddAlpha;
	}

	static final public boolean isTypeUsingAlphaChannel( int type ) {
		return isTypeUsingAddAlpha(type) || isTypeUsingAlpha(type);
	}
}
