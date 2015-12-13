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

import java.util.ArrayList;

public class Rect {
	/** Rect pool 不用意なnew を避け GCが動く回数を減らす */
	private static ArrayList<Rect> mRectPool = new ArrayList<Rect>(128);
	public static Rect allocate() {
		final int count = mRectPool.size();
		if( count > 0 ) {
			int i = count - 1;
			Rect ret = mRectPool.get(i);
			mRectPool.remove(i);
			return ret;
		} else {
			return new Rect();
		}
	}
	public static void release(Rect r) { mRectPool.add(r); }

	public static void initialize() {
		mRectPool = new ArrayList<Rect>(128);
	}
	public static void finalizeApplication() {
		mRectPool = null;
	}



	public int left;
	public int top;
	public int right;
	public int bottom;

	public Rect( int l, int t, int r, int b ) {
		left = l;
		top = t;
		right = r;
		bottom =b;
	}

	public Rect() {}

	public Rect(Rect rt ) {
		left = rt.left;
		top = rt.top;
		right = rt.right;
		bottom = rt.bottom;
	}

	public final int width() { return right - left; }
	public final int height() { return bottom - top; }

	public final void setWidth( int w) { right = left + w; }
	public final void setHeight( int h) { bottom = top + h; }

	public final void addOffsets( int x, int y) {
		left += x; right += x;
		top += y; bottom += y;
	}

	public final void setOffsets( int x, int y ) {
		int w = width();
		int h = height();
		left = x;
		top = y;
		right = x + w;
		bottom = y + h;
	}

	public final void setSize( int w, int h ) {
		right = left + w;
		bottom = top + h;
	}
	public final void set( Rect rt ) {
		left = rt.left;
		top = rt.top;
		right = rt.right;
		bottom = rt.bottom;
	}
	public void set(int l, int t, int r, int b ) {
		left = l;
		top = t;
		right = r;
		bottom = b;
	}

	public final void clear() {
		left = top = right = bottom = 0;
	}

	public final boolean isEmpty() {
		return left >= right || top >= bottom;
	}

	public final boolean doUnion( final Rect ref ) {
		if(ref.isEmpty()) return false;
		if(left > ref.left) left = ref.left;
		if(top > ref.top) top = ref.top;
		if(right < ref.right) right = ref.right;
		if(bottom < ref.bottom) bottom = ref.bottom;
		return true;
	}

	boolean clip( final Rect ref ) {
		// Clip (take the intersection of) the rectangle with rectangle.
		// returns whether the rectangle remains.
		return intersectRect(this, this, ref);
	}

	public final boolean intersectsWithNoEmptyCheck( final Rect ref ) {
		// returns wether this has intersection with "ref"
		return !(
			left >= ref.right ||
			top >= ref.bottom ||
			right <= ref.left ||
			bottom <= ref.top );
	}

	public final boolean intersectsWith( final Rect ref ) {
		// returns wether this has intersection with "ref"
		if(ref.isEmpty() || isEmpty()) return false;
		return intersectsWithNoEmptyCheck(ref);
	}

	public final boolean includedInNoEmptyCheck( final Rect ref ) {
		// returns wether this is included in "ref"
		return
			ref.left <= left &&
			ref.top <= top &&
			ref.right >= right &&
			ref.bottom >= bottom;
	}

	public final boolean includedIn( final Rect ref ) {
		// returns wether this is included in "ref"
		if(ref.isEmpty() || isEmpty()) return false;
		return includedInNoEmptyCheck(ref);
	}

// comparison operators for sorting
	// <
	public final boolean littlerThan( final Rect rhs ) {
		return top < rhs.top || (top == rhs.top && left < rhs.left);
	}

	// >
	public final boolean greaterThan( final Rect rhs ) {
		return top > rhs.top || (top == rhs.top && left > rhs.left);
	}

	// comparison methods
	// ==
	public final boolean equals( final Rect rhs ) {
		return top == rhs.top && left == rhs.left && right == rhs.right && bottom == rhs.bottom;
	}
	// !=
	public final boolean notEquals( final Rect rhs ) { return !equals(rhs); }

	static public final boolean intersectRect( Rect dest, final Rect src1, final Rect src2 ) {
		int left =		src1.left > src2.left ? src1.left : src2.left;
		int top =		src1.top > src2.top ? src1.top : src2.top;
		int right =		src1.right < src2.right ? src1.right : src2.right;
		int bottom =	src1.bottom < src2.bottom ? src1.bottom : src2.bottom;

		if( right > left && bottom > top ) {
			if( dest != null ){
				dest.left =		left;
				dest.top =		top;
				dest.right =	right;
				dest.bottom =	bottom;
			}
			return true;
		}
		return false;
	}
	static public final boolean unionRect( Rect dest, final Rect src1, final Rect src2 ) {
		int left =		src1.left < src2.left ? src1.left : src2.left;
		int top =		src1.top < src2.top ? src1.top : src2.top;
		int right =		src1.right > src2.right ? src1.right : src2.right;
		int bottom =	src1.bottom > src2.bottom ? src1.bottom : src2.bottom;

		if( right > left && bottom > top ) {

			if( dest != null ) {
				dest.left =	left;
				dest.top =		top;
				dest.right =	right;
				dest.bottom =	bottom;
			}
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(64);
		builder.append("left:");
		builder.append(left);
		builder.append(", top:");
		builder.append(top);
		builder.append(", right:");
		builder.append(right);
		builder.append(", bottom:");
		builder.append(bottom);
		return builder.toString();
	}
}
