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

	public static final boolean is_clPalIdx( int n ) {
		return ((n)&0xff000000) == clPalIdx;
	}
	public static final int get_clPalIdx( int n ) {
		return n&0xff;
	}
	public static final boolean is_clAlphaMat( int n ) {
		return ((n)&0xff000000) == clAlphaMat;
	}
	public static final int get_clAlphaMat( int n ) {
		return n&0xffffff;
	}

	public static int colorToRGB( int color ) {
		switch( color ) {
		case clScrollBar:
			return java.awt.SystemColor.scrollbar.getRGB();
		case clBackground:
			return java.awt.SystemColor.desktop.getRGB();
		case clActiveCaption:
			return java.awt.SystemColor.activeCaption.getRGB();
		case clInactiveCaption:
			return java.awt.SystemColor.inactiveCaption.getRGB();
		case clMenu:
			return java.awt.SystemColor.menu .getRGB();
		case clWindow:
			return java.awt.SystemColor.window.getRGB();
		case clWindowFrame:
			return java.awt.SystemColor.windowBorder.getRGB();
		case clMenuText:
			return java.awt.SystemColor.menuText.getRGB();
		case clWindowText:
			return java.awt.SystemColor.windowText.getRGB();
		case clCaptionText:
			return java.awt.SystemColor.activeCaptionText.getRGB();
		case clActiveBorder:
			return java.awt.SystemColor.activeCaptionBorder.getRGB();
		case clInactiveBorder:
			return java.awt.SystemColor.inactiveCaptionBorder.getRGB();
		case clAppWorkSpace:
			// return java.awt.SystemColor.desktop.getRGB(); //
			return 0xffababab; // 該当するものがない模様
		case clHighlight:
			return java.awt.SystemColor.textHighlight.getRGB();
		case clHighlightText:
			return java.awt.SystemColor.textHighlightText.getRGB();
		case clBtnFace:
			return java.awt.SystemColor.control.getRGB();
		case clBtnShadow:
			return java.awt.SystemColor.controlShadow.getRGB();
		case clGrayText:
			return java.awt.SystemColor.textInactiveText.getRGB();
		case clBtnText:
			return java.awt.SystemColor.controlText.getRGB();
		case clInactiveCaptionText:
			return java.awt.SystemColor.inactiveCaptionText.getRGB();
		case clBtnHighlight:
			return java.awt.SystemColor.controlLtHighlight.getRGB();
		case cl3DDkShadow:
			return java.awt.SystemColor.controlDkShadow.getRGB();
		case cl3DLight:
			return java.awt.SystemColor.controlHighlight.getRGB();
		case clInfoText:
			return java.awt.SystemColor.infoText.getRGB();
		case clInfoBk:
			return java.awt.SystemColor.info.getRGB();
		case clNone:
			return 0xffffffff;
		case clAdapt:
			return clAdapt;
		case clPalIdx:
			return clPalIdx;
		case clAlphaMat:
			return clAlphaMat;
		}
		return color; // unknown, passthru
	}
}
