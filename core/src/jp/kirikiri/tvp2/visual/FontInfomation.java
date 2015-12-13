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

import jp.kirikiri.tvp2env.Font;

public class FontInfomation {
	private String mFaceName;
	private int mHeight;
	private int mFlags;
	private int mAngle;

	public FontInfomation() {}
	public FontInfomation( final String face, int height, int flags, int angle ) {
		mFaceName = face;
		mHeight = height;
		mFlags = flags;
		mAngle = angle;
	}
	public FontInfomation( Font f ) {
		mFaceName = f.getFaceName();
		mHeight = f.getHeight();
		mAngle = f.getAngle();
		mFlags = 0;
		mFlags |= f.getBold() ? Font.TF_BOLD : 0;
		mFlags |= f.getItalic() ? Font.TF_ITALIC : 0;
		mFlags |= f.getStrikeout() ? Font.TF_STRIKEOUT : 0;
		mFlags |= f.getUnderline() ? Font.TF_UNDERLINE : 0;
	}
	public FontInfomation(FontInfomation font) {
		mFaceName = font.mFaceName;
		mHeight = font.mHeight;
		mFlags = font.mFlags;
		mAngle = font.mAngle;
	}
	public void setFont( Font f ) {
		mFaceName = f.getFaceName();
		mHeight = f.getHeight();
		mAngle = f.getAngle();
		mFlags = 0;
		mFlags |= f.getBold() ? Font.TF_BOLD : 0;
		mFlags |= f.getItalic() ? Font.TF_ITALIC : 0;
		mFlags |= f.getStrikeout() ? Font.TF_STRIKEOUT : 0;
		mFlags |= f.getUnderline() ? Font.TF_UNDERLINE : 0;
	}
	public boolean equals(Object o){
		if( o instanceof FontInfomation ) {
			FontInfomation f = (FontInfomation)o;
			if( mFaceName == null ) {
				if( mFaceName == f.mFaceName && mHeight == f.mHeight && mFlags == f.mFlags && mAngle == f.mAngle ) {
					return true;
				} else {
					return false;
				}
			} else if( mFaceName.equals( f.mFaceName) && mHeight == f.mHeight && mFlags == f.mFlags && mAngle == f.mAngle ) {
				return true;
			} else {
				return false;
			}
		} else {
			return false ;
		}
	}
	public int hashCode() {
		int hash = 0;
		if( mFaceName != null ) {
			hash = mFaceName.hashCode();
		}
		hash ^= mFlags;
		hash ^= mAngle;
		hash ^= mHeight;
		return hash;
	}
}
