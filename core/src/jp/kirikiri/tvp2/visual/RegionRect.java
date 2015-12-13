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

public class RegionRect extends Rect {

	/*
	class RegionRectInitialHolder {
		// Initial rectangle holder to prevent unnecessary freeing of the vector.
		// This holds a rectangle from program start to end.
		private RegionRect InitialRect;
		public RegionRectInitialHolder() { InitialRect = null; }
		void hold() {
			if( InitialRect == null ) {
				//InitialRect = (tTVPRegionRect *)-1;
				InitialRect = allocate();
			}
		}
		protected void finalize() {
			if(InitialRect != null ) release(InitialRect);
		}
	}
	static RegionRectInitialHolder RegionRectInitialHolder;
	*/

	static private ArrayList<RegionRect> RegionFreeRects;
	static private final int REGIONRECT_ALLOC_UNITS = 256;
	static private int RegionFreeMaxCount = 0;

	static private void prepareRegionRectangle() {
		// Prepare rectangles and insert them into free rectangle list
		for( int i = 0; i < REGIONRECT_ALLOC_UNITS; i++ ) {
			RegionFreeRects.add( new RegionRect() );
			RegionFreeMaxCount++;
		}
	}
	static public void initialize() {
		RegionFreeRects = new ArrayList<RegionRect>(REGIONRECT_ALLOC_UNITS*2);
		prepareRegionRectangle();
	}
	public static void finalizeApplication() {
		RegionFreeRects = null;
	}

	// TODO どうもプールするとfinalizeで呼ばれるケースでバグるっぽい
	static public RegionRect allocate() {
		// Allocate a region rectangle. Note that this function does not clear
		// nor initialize the rectangle object.

		// Create a vector of free rectangle list if it does not exists
		//if( RegionFreeRects == null )
		//	RegionFreeRects = new ArrayList<RegionRect>(REGIONRECT_ALLOC_UNITS*2);

		// Prepare free rectangles if the free list is empty
		//if( RegionFreeRects.size() == 0 )
		//	prepareRegionRectangle();

		// Take a free rectangle from last of the free rectangle list
		final int last = RegionFreeRects.size()-1;
		if( last < 0 ) return new RegionRect();

		RegionRect r = RegionFreeRects.get(last);
		RegionFreeRects.remove(last);

		// Hold initial rectangle
		// RegionRectInitialHolder.Hold();

		// Return the rectangle
		return r;
	}
	static public void release( RegionRect rect) {
		// Deallocate a region rectangle allocated in TVPAllocateRegionRect().

		// 多くなりすぎた時はそのまま放置
		if( RegionFreeRects.size() > (REGIONRECT_ALLOC_UNITS*2) )
			return;

		// Append to TVPRegionFreeRects
		RegionFreeRects.add(rect);

		// Full-Free check
		/*
		if( RegionFreeRects.size() == RegionFreeMaxCount ) {
			// Free all rectangles
			// Free the vector
			RegionFreeRects.clear();
			RegionFreeRects = null;
			RegionFreeMaxCount = 0;
		}
		*/
	}

	public RegionRect Prev; // previous link
	public RegionRect Next; // next link

	public RegionRect() {}
	public RegionRect( final Rect r) { super(r); }

	// link operations
	public void linkAfter( RegionRect r ) {
		// Insert this after r.
		RegionRect n = r.Next;
		r.Next = this;
		n.Prev = this;
		Prev = r;
		Next = n;
	}

	public void linkBefore( RegionRect r ) {
		// Insert this before r.
		RegionRect p = r.Prev;
		r.Prev = this;
		p.Next = this;
		Prev = p;
		Next = r;
	}

	public void unlink() {
		// unchain from the link list
		RegionRect prev = Prev;
		RegionRect next = Next;
		prev.Next = next;
		next.Prev = prev;
	}
}
