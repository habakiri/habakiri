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

import jp.kirikiri.tvp2.visual.DivisibleData;
import jp.kirikiri.tvp2.visual.Rect;
import jp.kirikiri.tvp2.visual.ScanLineProvider;
import jp.kirikiri.tvp2.visual.SimpleOptionProvider;

public class ScrollTransHandler extends CrossFadeTransHandler {

	public static final int sttLeft = 0, sttTop = 1, sttRight = 2, sttBottom = 3;
	public static final int ststNoStay = 0, ststStayDest = 1, ststStaySrc = 2;

	private int mFrom;
	private int mStay;

	public ScrollTransHandler(SimpleOptionProvider options, int layertype,
			long time, int from, int stay, int maxphase ) {
		super(options, layertype, time, maxphase );
		mFrom = from;
		mStay = stay;
	}

	@Override
	public void blend( DivisibleData data ) {
		final int from = mFrom;
		final int stay = mStay;
		final int phase = mPhase;
		final int imagewidth = data.Src1.getWidth();
		final int imageheight = data.Src1.getHeight();
		int src1left = 0;
		int src1top = 0;
		int src2left = 0;
		int src2top = 0;

		switch( from ) {
		case sttLeft:
			if( stay == ststNoStay ) {
				src1left = phase;
				src2left = phase - imagewidth;
			} else if( stay == ststStayDest ) {
				src2left = phase - imagewidth;
			} else if( stay == ststStaySrc ) {
				src1left = phase;
			}
			break;
		case sttTop:
			if( stay == ststNoStay ) {
				src1top = phase;
				src2top = phase - imageheight;
			} else if( stay == ststStayDest ) {
				src2top = phase - imageheight;
			} else if( stay == ststStaySrc ) {
				src1top = phase;
			}
			break;
		case sttRight:
			if( stay == ststNoStay ) {
				src1left = -phase;
				src2left = imagewidth - phase;
			} else if( stay == ststStayDest ) {
				src2left = imagewidth - phase;
			} else if( stay == ststStaySrc ) {
				src1left = -phase;
			}
			break;
		case sttBottom:
			if( stay == ststNoStay ) {
				src1top = -phase;
				src2top = imageheight - phase;
			} else if( stay == ststStayDest ) {
				src2top = imageheight - phase;
			} else if( stay == ststStaySrc ) {
				src1top = -phase;
			}
			break;
		}

		Rect rdest = new Rect(data.Left, data.Top, data.Width+data.Left, data.Height+data.Top);
		Rect rs1 = new Rect(src1left, src1top, imagewidth + src1left, imageheight + src1top);
		Rect rs2 = new Rect(src2left, src2top, imagewidth + src2left, imageheight + src2top);
		if( stay == ststNoStay ) {
			// both layers have no priority than another.
			// nothing to do.
		} else if( stay == ststStayDest ) {
			// Src2 has priority.
			if( from == sttLeft || from == sttRight ) {
				if(rs2.right >= rs1.left && rs2.right < rs1.right)
					rs1.left = rs2.right;
				if(rs2.left >= rs1.left && rs2.left < rs1.right)
					rs1.right = rs2.left;
			} else {
				if(rs2.bottom >= rs1.top && rs2.bottom < rs1.bottom)
					rs1.top = rs2.bottom;
				if(rs2.top >= rs1.top && rs2.top < rs1.bottom)
					rs1.bottom = rs2.top;
			}
		} else if( stay == ststStaySrc ) {
			// Src1 has priority.
			if( from == sttLeft || from == sttRight ) {
				if(rs1.right >= rs2.left && rs1.right < rs2.right)
					rs2.left = rs1.right;
				if(rs1.left >= rs2.left && rs1.left < rs2.right)
					rs2.right = rs1.left;
			} else {
				if(rs1.bottom >= rs2.top && rs1.bottom < rs2.bottom)
					rs2.top = rs1.bottom;
				if(rs1.top >= rs2.top && rs1.top < rs2.bottom)
					rs2.bottom = rs1.top;
			}
		}

		// copy to destination image
		Rect d = new Rect();
		if( Rect.intersectRect( d, rdest, rs1) ) {
			int dl = d.left - data.Left + data.DestLeft;
			int dt = d.top - data.Top + data.DestTop;
			d.addOffsets( -src1left, -src1top );
			copyRect( data.Dest, dl, dt, data.Src1, d );
		}
		if( Rect.intersectRect( d, rdest, rs2) ) {
			int dl = d.left - data.Left + data.DestLeft;
			int dt = d.top - data.Top + data.DestTop;
			d.addOffsets( -src2left, -src2top );
			copyRect( data.Dest, dl, dt, data.Src2, d );
		}
	}

	private void copyRect( ScanLineProvider destimg, int x, int y, ScanLineProvider srcimg, final Rect srcrect ) {
		// this function does not matter if the src==dest and copying area is overlapped.
		// destimg and srcimg must be 32bpp bitmap.

		NativeImageBuffer dest = destimg.getScanLineForWrite();
		NativeImageBuffer src = srcimg.getScanLine();
		dest.copyRect( x, y, src, srcrect );
	}
}
