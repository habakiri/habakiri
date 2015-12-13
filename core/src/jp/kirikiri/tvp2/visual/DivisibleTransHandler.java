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

public interface DivisibleTransHandler extends BaseTransHandler {

	/**
	 * called before one processing time unit.
	 * expected return values are:
	 * S_TRUE: continue processing
	 * S_FALSE: break processing
	 * @param tick
	 * @return
	 */
	public int startProcess( /*in*/long tick );

	/**
	 * called after one processing time unit.
	 * expected return values are:
	 * S_TRUE: continue processing
	 * S_FALSE: break processing
	 */
	public int endProcess();

	/**
	 * called during StartProcess and EndProcess per an update rectangle.
	 * the handler processes given rectangle and put result image to
	 * "Dest"( in tTVPDivisibleData ).
	 * given "Dest" is a internal image buffer, but callee can change
	 * the "Dest" pointer to Src1 or Src2. Also DestLeft and DestTop can
	 * be changed to point destination image part.
	 * @throws TJSException
	 */
	public int process( /*in,out*/DivisibleData data ) throws TJSException;

	/**
	 * will be called after StartProcess/EndProcess returns S_FALSE.
	 * this function does not called in some occasions.
	 * fill "dest" to make a final image.
	 * dest can be set to either src1 or src2.
	 * @throws TJSException
	 */
	public int makeFinalImage( /*in,out*/ScanLineProvider dest, /*in*/ScanLineProvider src1, /*in*/ScanLineProvider src2 ) throws TJSException;
}
