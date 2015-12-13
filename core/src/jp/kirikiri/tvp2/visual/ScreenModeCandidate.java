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

/**
 * Structure for monitor screen mode candidate
 */
public class ScreenModeCandidate extends ScreenMode {
	public int mZoomNumer; // zoom ratio numer
	public int mZoomDenom; // zoom ratio denom
	public int mRankZoomIn;
	public int mRankBPP;
	public int mRankZoomBeauty;
	public int mRankSize; // candidate preference priority (lower value is higher preference)

	public String dump() {
		StringBuilder builder = new StringBuilder(128);
		builder.append(super.dump());
		builder.append(", ZoomNumer=");
		builder.append(mZoomNumer);
		builder.append(", ZoomDenom=");
		builder.append(mZoomDenom);
		builder.append(", RankZoomIn=");
		builder.append(mRankZoomIn);
		builder.append(", RankBPP=");
		builder.append(mRankBPP);
		builder.append(", RankZoomBeauty=");
		builder.append(mRankZoomBeauty);
		builder.append(", RankSize=");
		builder.append(mRankSize);
		return builder.toString();
	}

	// operator <
	public boolean littlerThan( final ScreenModeCandidate rhs ) {
		if(mRankZoomIn < rhs.mRankZoomIn) return true;
		if(mRankZoomIn > rhs.mRankZoomIn) return false;
		if(mRankBPP < rhs.mRankBPP) return true;
		if(mRankBPP > rhs.mRankBPP) return false;
		if(mRankZoomBeauty < rhs.mRankZoomBeauty) return true;
		if(mRankZoomBeauty > rhs.mRankZoomBeauty) return false;
		if(mRankSize < rhs.mRankSize) return true;
		if(mRankSize > rhs.mRankSize) return false;
		return false;
	}
}
