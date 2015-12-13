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

import jp.kirikiri.tjs2.TJSException;

public interface TransHandlerProvider {

	/** transition using only one(self) layer ( eg. simple fading ) */
	public static final int ttSimple = 0;
	/** transition using two layer ( eg. cross fading ) */
	public static final int ttExchange = 1;


	/**
	 * used when the transition processing is region-divisible and
	 * the transition updates entire area of the layer.
	 * update area is always given by TransHandler.process caller.
	 * handler must use only given area of the source bitmap on each
	 * callbacking.
	 */
	public static final int tutDivisibleFade = 0;
	/**
	 * same as tutDivisibleFade, except for its usage of source area.
	 * handler can use any area of the source bitmap.
	 * this will somewhat slower than tutDivisibleFade.
	 */
	public static final int tutDivisible = 1;
	/**
	 * used when the transition processing is not region-divisible or
	 * the transition updates only some small regions rather than entire
	 * area.
	 * update area is given by callee of TransHandler.process,
	 * via LayerUpdater interface.
	 */
	public static final int tutGiveUpdate = 2;

	/**
	 * return this transition name
	 * @return
	 */
	public String getName();

	/**
	 * start transition and return a handler.
	 * "handler" is an object of iTVPDivisibleTransHandler when
	 * updatetype is tutDivisibleFade or tutDivisible.
	 * Otherwise is an object of iTVPGiveUpdateTransHandler ( cast to
	 * each class to use it )
	 * layertype is the destination layer type.
	 * @param options : option provider
	 * @param imagepro : image provider
	 * @param layertype : destination layer type
	 * @param src1w : source 1 width
	 * @param src1h : source 1 height
	 * @param src2w : source 2 width
	 * @param src2h : source 2 height
	 * @param type : [0]transition type, [1]update typwe
	 * @return  transition handler
	 * @throws TJSException
	 */
	public BaseTransHandler startTransition( SimpleOptionProvider options, SimpleImageProvider imagepro,
			int layertype, int src1w, int src1h, int src2w, int src2h, int[] type ) throws TJSException;
}
