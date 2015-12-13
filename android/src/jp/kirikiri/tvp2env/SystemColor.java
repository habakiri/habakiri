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

public class SystemColor {
	private static final int
		clScrollBar = 0x80000000,
		clBackground = 0x80000001,
		clActiveCaption = 0x80000002,
		clInactiveCaption = 0x80000003,
		clMenu = 0x80000004;
	public static final int clWindow = 0x80000005;
	public static final int clWindowFrame = 0x80000006, clMenuText = 0x80000007, clWindowText = 0x80000008, clCaptionText = 0x80000009, clActiveBorder = 0x8000000a, clInactiveBorder = 0x8000000b, clAppWorkSpace = 0x8000000c,
			clHighlight = 0x8000000d, clHighlightText = 0x8000000e, clBtnFace = 0x8000000f, clBtnShadow = 0x80000010, clGrayText = 0x80000011, clBtnText = 0x80000012, clInactiveCaptionText = 0x80000013,
			clBtnHighlight = 0x80000014, cl3DDkShadow = 0x80000015, cl3DLight = 0x80000016, clInfoText = 0x80000017, clInfoBk = 0x80000018, clNone = 0x1fffffff, clAdapt= 0x01ffffff,
			clPalIdx = 0x3000000, clAlphaMat = 0x4000000;


	// Windows と合わせるか、Android のテーマ等から引っ張ってくるか
	public static int colorToRGB( int color ) {
		switch( color ) {
		case clScrollBar:
			return 0xffc8c8c8;
		case clBackground:
			return 0xff000000;
		case clActiveCaption:
			return 0xff99b4d1;
		case clInactiveCaption:
			return 0xffbfcddb;
		case clMenu:
			return 0xfff0f0f0;
		case clWindow:
			return 0xffffffff;
		case clWindowFrame:
			return 0xff646464;
		case clMenuText:
			return 0xff000000;
		case clWindowText:
			return 0xff000000;
		case clCaptionText:
			return 0xff000000;
		case clActiveBorder:
			return 0xffb4b4b4;
		case clInactiveBorder:
			return 0xfff4f7fc;
		case clAppWorkSpace:
			return 0xffababab;
		case clHighlight:
			return 0xff3399ff;
		case clHighlightText:
			return 0xffffffff;
		case clBtnFace:
			return 0xfff0f0f0;
		case clBtnShadow:
			return 0xffa0a0a0;
		case clGrayText:
			return 0xff6d6d6d;
		case clBtnText:
			return 0xff000000;
		case clInactiveCaptionText:
			return 0xff434e54;
		case clBtnHighlight:
			return 0xffffffff;
		case cl3DDkShadow:
			return 0xff696969;
		case cl3DLight:
			return 0xffe3e3e3;
		case clInfoText:
			return 0xff000000;
		case clInfoBk:
			return 0xffffffe1;
		case clNone: // black for WinNT
			return 0xff000000;
		case clAdapt:
			return clAdapt;
		case clPalIdx:
			return clPalIdx;
		case clAlphaMat:
			return clAlphaMat;
		}
		return color; // unknown, passthru
	}

	public static final boolean is_clAlphaMat(int n) {
		return ((n)&0xff000000) == clAlphaMat;
	}

	public static final int get_clAlphaMat(int n) {
		return n&0xffffff;
	}

	public static final boolean is_clPalIdx(int n) {
		return ((n)&0xff000000) == clPalIdx;
	}

	public static final int get_clPalIdx(int n ) {
		return n&0xff;
	}
}
