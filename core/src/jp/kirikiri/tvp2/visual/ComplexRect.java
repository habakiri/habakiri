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

import jp.kirikiri.tjs2.Logger;

public class ComplexRect {
	static class Iterator {
		private RegionRect Head;
		private RegionRect Current;

		public Iterator() {
			Head = null;
			Current = null;
		}
		Iterator( final RegionRect head) {
			Head = head;
			//Current = null;
		}

		public RegionRect get() { return Current; }

		// stepping forward; this object supports only forward step.
		// method step returns true if stepping successful,
		// otherwise returns false (when already at last of the list)
		// sample:
		//   tTVPComplexRect::tIterator it = rects.GetIterator();
		//   while(it.Step()) { .. do something with it .. }
		public boolean step() {
			// Step forward
			if(Head == null ) return false;
			if(Current == null ) { Current = Head; return true; }
			if(Current.Next == Head) { return false; }
			Current = Current.Next;
			return true;
		}
	}

	private RegionRect Head; // head of the link list
	private RegionRect Current; // a rectangle which is touched last time
	private int Count; // rectangle Count
	private Rect Bound; // bounding rectangle
	private boolean BoundValid; // whether the bounding rectangle is ready to use

	public ComplexRect() {
		Bound = new Rect();
		init();
	}
	public ComplexRect( final ComplexRect ref ) {
		Bound = new Rect( ref.Bound );
		init();

		// allocate storage
		setCount(ref.Count);

		// copy rectangles
		if(Count!=0) {
			RegionRect this_cur = Head;
			RegionRect ref_cur = ref.Head;
			do {
				this_cur.set( ref_cur ); // copy as tTVPRect
				this_cur = this_cur.Next;
				ref_cur = ref_cur.Next;
			} while(ref_cur != ref.Head);
		}
	}
	/*
	protected void finalize() {
		Head = null;
	}
	*/

	public void clear() {
		freeAllRectangles();
		init();
	}

	private void freeAllRectangles() {
		// free all rectangles
		if(Count!=0) {
			RegionRect cur = Head;
			do {
				RegionRect n  = cur.Next;
				RegionRect.release(cur);
				cur = n;
			} while(cur != Head);
		}
	}
	private void init() {
		Head = null;
		Current = null;
		Count = 0;
		Bound.left = Bound.top = Bound.right = Bound.bottom = 0;
		BoundValid = false;
	}
	private void setCount( int count ) { // grow or shrink rectangle storage area
		// grow or shrink rectangle storage area
		if( count > Count ) {
			// grow
			int add_count = count - Count;
			if( Count == 0 ) {
				// no existent rectangle
				Head = RegionRect.allocate();
				Head.Prev = Head;
				Head.Next = Head;
				add_count--;
			}

			RegionRect cur = Head.Prev;
			Current = cur;
			while( add_count > 0 ) {
				RegionRect newrect = RegionRect.allocate();
				newrect.linkBefore(Head);
				add_count--;
			}
			Count = count;
		} else if(count < Count) {
			// shrink
			if(Count != 0) {
				int remove_count = Count - count;
				RegionRect cur = Head.Prev;
				while( remove_count > 0 ) {
					RegionRect prev = cur.Prev;
					RegionRect.release(cur);
					cur = prev;
					remove_count--;
				}
				Count = count;
				if(Count!=0) {
					cur.Next = Head;
					Current = Head;
					Head.Prev = cur;
				} else {
					Head = null;
					Current = null;
				}
			}
		}
	}
	private boolean insert( final Rect rect ) { // insert inplace
		// Insert a region rectangle inplace.
		// Note that this function does not update the bounding rectangle.
		// This does empty check.

		if(rect.isEmpty()) return false;

		if( Count == 0 ){
			// first insertion
			Head = RegionRect.allocate();
			Head.set(rect);
			Head.Prev = Head.Next = Head;
			Current = Head;
			Count = 1;
			return true;
		}

		// Search insertion point and insert there.
		// Link list is sorted by upper-left point of the rectangle,
		// Search starts from "Current", which is the most recently touched rectangle.
		//if( Current > rect ) {
		if( Current.greaterThan(rect) ) {
			// insertion point is before Current
			RegionRect prev;
			while(true) {
				if(Current == Head) {
					// insert before head
					if(Head.top == rect.top && Head.bottom == rect.bottom && Head.left == rect.right) {
						Head.left = rect.left; // unite Head
						break;
					} else {
						RegionRect new_rect = RegionRect.allocate();
						new_rect.set(rect);
						new_rect.linkBefore(Head);
						Count ++;
						Head = new_rect;
						break;
					}
				}
				prev = Current.Prev;
				//if( prev < rect)
				if( prev.littlerThan(rect) ) {
					// insert between prev and Current
					if(Current.top == rect.top && Current.bottom == rect.bottom && Current.left == rect.right) {
						Current.left = rect.left; // unite right
						break;
					} else {
						RegionRect new_rect = RegionRect.allocate();
						new_rect.set(rect);
						new_rect.linkBefore(Current);
						Count ++;
						break;
					}

				}
				Current = prev;
			}
		} else {
			// insertion point is after Current
			RegionRect next;
			while(true) {
				next = Current.Next;
				if(next == Head) {
					// insert after last of the link list
					// (that is, before the Head)
					if(Current.top == rect.top && Current.bottom == rect.bottom && Current.right == rect.left) {
						Current.right = rect.right; // unite right
						break;
					} else {
						RegionRect new_rect = RegionRect.allocate();
						new_rect.set(rect);
						new_rect.linkBefore(Head);
						Count++;
						break;
					}
				}
				//if(*next > rect) {
				if( next.greaterThan(rect) ) {
					// insert between next and Current
					if( next.top == rect.top && next.bottom == rect.bottom && next.left == rect.right) {
						next.left = rect.left; // unite right
						if( Current.top == rect.top && Current.bottom == rect.bottom && Current.right == rect.left) {
							next.left = Current.left; // unite left
							remove(Current);
						}
						break;
					} else {
						RegionRect new_rect = RegionRect.allocate();
						new_rect.set(rect);
						new_rect.linkBefore(next);
						Count++;
						break;
					}
				}
				Current = next;
			}
		}
		return true;
	}
	private void remove( RegionRect rect ) { // remove a rectangle
		// Remove a rectangle.
		// Note that this function does not update the bounding rectangle.
		if( rect == Head ) Head = rect.Next;

		Count--;
		if( Count == 0 ) {
			// no more rectangles
			Current = Head = null;
		} else {
			if(rect == Current) Current = rect.Prev;
		}
		rect.unlink();
		RegionRect.release(rect);
	}
	private void merge( final ComplexRect rects ) { // merge non-overlaped complex rectangle
		// Merge non-overlaped complex rectangle
		// Calculate bounding rectangle
		rects.ensureBound();
		ensureBound();
		if( Count != 0 ) {
			if( rects.Count != 0 )
				Bound.doUnion(rects.Bound);
			else
				; // do nothing
		} else {
			if(rects.Count != 0 )
				Bound.set( rects.Bound );
			else
				return; // both empty; do nothing
		}

		// merge per a rectangle
		RegionRect ref_cur = rects.Head;
		do {
			insert(ref_cur);
			ref_cur = ref_cur.Next;
		} while( ref_cur != rects.Head );
	}
	public int getCount() { return Count; }

	 // logical operations
	public void or( final Rect r ) {
		// OR operation

		// empty check
		if(r.isEmpty()) return;

		// simply insert when no rectangle exists
		if(Count == 0)
		{
			if(insert(r)) {
				Bound.set( r );
				BoundValid = true;
			}
			return;
		}

		// Check for bounding rectangle
		ensureBound();
		if(!Bound.intersectsWithNoEmptyCheck(r)) {
			// Out of the Bouding rectangle; Simply insert
			if(insert(r))
				Bound.doUnion(r);
			return;
		}

		// Check for null rectangle
		if(r.left >= r.right || r.top >= r.bottom) return; // null rect

		// Update bounding rectangle
		Bound.doUnion(r);

		// Walk through rectangles
		RegionRect c = Head;
		while( c.top < r.bottom ) {
			// Check inclusion
			if(r.includedInNoEmptyCheck(c)) {
				// r is completely included in c
				return; //---                         returns here
			}

			// Check intersection
			boolean next_is_head;
			if(c.intersectsWithNoEmptyCheck(r)) {
				// Do OR operation. This may increase the rectangle count.
				RegionRect next = c.Next;
				next_is_head = next == Head;
				Current = c;
				rectangleSub(c, r);
				c = next_is_head ? Head : next;
					// Re-assign head since the "Head" may change in "RectangleSub"
			} else {
				// Step next
				c = c.Next;
				next_is_head = c == Head;
			}

			if( Head == null || c == null || next_is_head) break;
		}

		// finally insert r
		insert(r);
	}
	public void or( final ComplexRect ref ) {
		// OR operation
		if(ref.Count == 0) return; // nothing to do

		ensureBound();
		ref.ensureBound();
		if(!Bound.intersectsWithNoEmptyCheck(ref.Bound))
		{
			// Out of the Bouding rectangle; Simply marge
			merge(ref);
			return;
		}

		RegionRect c = ref.Head;
		while(true) {
			or(c);
			c = c.Next;
			if(c == ref.Head) break;
		}
	}
	public void sub( final Rect r ) {
		// Subtraction operation

		// Check for null rectangle
		if(r.left >= r.right || r.top >= r.bottom) return; // null rect

		// check bounding rectangle
		ensureBound();
		if(!Bound.intersectsWithNoEmptyCheck(r)) {
			// Out of the Bouding rectangle; nothing to do
			return;
		}

		switch(getRectangleIntersectionCode(Bound, r)) {
		case 8 + 4 + 2 + 1: // r overlaps Bound
			clear(); // nothing remains
			return;
		case 8 + 4 + 2: // r overlaps upper of Bound
			Bound.top = r.bottom;
			break;
		case 4 + 2 + 1: // r overlaps right of Bound
			Bound.right = r.left;
			break;
		case 8 + 4 + 1: // r overlaps bottom of Bound
			Bound.bottom = r.top;
			break;
		case 8 + 2 + 1: // r overlaps left of Bound
			Bound.left = r.right;
			break;
		}

		// Walk through rectangles
		RegionRect c = Head;
		while(c.top < r.bottom) {
			// Check intersection
			boolean next_is_head;
			if(c.intersectsWithNoEmptyCheck(r)) {
				// check bounding rectangle
				if(c.left == Bound.left || c.top == Bound.top || c.right == Bound.top || c.bottom == Bound.bottom) {
					// one of the rectangle edge touches bounding rectangle
					BoundValid = false; // invalidate bounding rectangle
				}

				// Do Subtract operation. This may increase the rectangle count.
				RegionRect next = c.Next;
				next_is_head = next == Head;
				Current = c;
				rectangleSub(c, r);
				c = next_is_head ? Head : next;
					// Re-assign head since the "Head" may change in "RectangleSub"
			} else {
				// Step next
				c = c.Next;
				next_is_head = c == Head;
			}

			if( Head == null || c == null || next_is_head) break;
		}
	}
	public void sub( final ComplexRect ref ) {
		// Subtract operation
		if(ref.Count == 0) return; // nothing to do

		// check bounding rectangle
		ensureBound();
		ref.ensureBound();
		if(!Bound.intersectsWithNoEmptyCheck(ref.Bound)) {
			// Out of the Bouding rectangle; nothing to do
			return;
		}

		RegionRect c = ref.Head;
		boolean boundvalid = true;
		while(true) {
			// subtract a rectangle
			sub(c);

			// check bounding rectangle validity
			boundvalid = BoundValid && boundvalid;
			BoundValid = true; // force validate bounding rectangle not to recalculate it.

			// step next
			c = c.Next;
			if(c == ref.Head) break;
		}

		BoundValid = boundvalid;
	}
	public void and( final Rect r ) {
		// Do "logical and" operation
		if(Count == 0) return; // nothing to do

		// Check for null rectangle
		if(r.left >= r.right || r.top >= r.bottom) {
			clear(); // null rectangle; nothing remains
			return;
		}

		// check bounding rectangle
		ensureBound();
		if(!Bound.intersectsWithNoEmptyCheck(r)) {
			// Out of the Bouding rectangle; nothing remains
			clear();
			return;
		}

		switch(getRectangleIntersectionCode(Bound, r)) {
		case 8 + 4 + 2 + 1: // r overlaps Bound
			// nothing to do
			return;
		}

		boolean is_first = true;
		boolean next_is_head;
		RegionRect c = Head, cc;
		while(true) {
			c = (cc = c).Next;
			next_is_head = c == Head;
			if(cc.clip(r)) {// clip with r

				if(is_first) {
					Bound = cc;
					is_first = false;
				} else
					Bound.doUnion(cc);
			} else {
				remove(cc);
			}

			if(next_is_head) break;
		}

		if(is_first)
			BoundValid = false; // Nothing remains
		else
			BoundValid = true;
	}

	// operation utilities
	public void copyWithOffsets( final ComplexRect ref, final Rect clip, int ofsx, int ofsy ) {
		// Copy "ref" to this.
		// "ref" is to be added offsets, and is to be done logical-and with the "clip",
		// Note that this function must be called immediately after the construction or
		// the "Clear" function (This function never clears the rectangles).

		// copy rectangles
		if(ref.Count != 0) {
			RegionRect ref_cur = ref.Head;
			boolean is_first = true;
			do {
				Rect rect = new Rect(ref_cur);
				rect.addOffsets(ofsx, ofsy);
				if( Rect.intersectRect(rect, rect, clip)) {
					// has intersection
					insert(rect);
					if(is_first) {
						Bound.set( rect );
						is_first = false;
					}
					else
						Bound.doUnion(rect);
				}
				ref_cur = ref_cur.Next;
			} while(ref_cur != ref.Head);

			if(is_first)
				BoundValid = false; // Nothing remains
			else
				BoundValid = true;
		}
	}

	// bounding rectangle
	public final Rect getBound() {
		ensureBound();
		return Bound;
	}

	public void unite() {
		// make union (bounding) one rectangle
		Rect r = new Rect(getBound());
		clear();
		or(r);
	}

	private void ensureBound() { if(!BoundValid) calcBound(); }
	private void calcBound() {
		// Calculate bounding rectangle
		if(Count != 0) {
			RegionRect c = Head;
			Bound.set( Head );
			c = c.Next;
			while(c != Head) {
				Bound.doUnion(c);
				c = c.Next;
			}
		} else {
			// no rectangles; bounding rectangle is not valid
			Bound.left = Bound.top = Bound.right = Bound.bottom = 0;
		}
	}

	// geometric rectangle operations
	private int getRectangleIntersectionCode( final Rect r, final Rect rr ) {
		// Retrieve condition code which represents
		// how two rectangles have the intersection.
		int cond;
		if(rr.left   <= r.left   && rr.right   >= r.left   )
			cond =  8; else cond = 0;
		if(rr.left   <= r.right  && rr.right   >= r.right  )
			cond |= 4;
		if(rr.top    <= r.top    && rr.bottom  >= r.top    )
			cond |= 2;
		if(rr.top    <= r.bottom && rr.bottom  >= r.bottom )
			cond |= 1;
			/*
					   +8             +4

					+------+ +---+ +------+
					|rr    | |rr | |    rr|     +2
					|    +-----------+    |
					+----|-+ +---+ +-|----+
					+----|-+       +-|----+
					|rr  | |   r   | |  rr|
					+----|-+       +-|----+
					+----|-+ +---+ +-|----+
					|    +-----------+    |     +1
					|rr    | | rr| |    rr|
					+------+ +---+ +------+
			*/
		return cond;
	}

	private void rectangleSub( RegionRect r, final Rect rr ) {
		// Subtract rr from r
		int cond = getRectangleIntersectionCode(r, rr);

		Rect nr = new Rect();
		switch(cond)
		{
	//--------------------------------------------------
		case 0:
	/*
	     +-----------+
		 |     r     |
		 +---+---+---+
	     |nr2|rr |nr3|
	     +---+---+---+
	     |    nr4    |
		 +-----------+
	*/
			// nr2
			nr.left    = r.left;
			nr.top     = rr.top;
			nr.right   = rr.left;
			nr.bottom  = rr.bottom;
			insert(nr);
			// nr3
			nr.left    = rr.right;
			nr.top     = rr.top;
			nr.right   = r.right;
			nr.bottom  = rr.bottom;
			insert(nr);
			// nr4
			nr.left    = r.left;
			nr.top     = rr.bottom;
			nr.right   = r.right;
			nr.bottom  = r.bottom;
			insert(nr);

			// r
			r.bottom  = rr.top;

			break;
	//--------------------------------------------------
		case 8 + 4 + 2 + 1:
	/*
	+---------------------+
	|                     |
	|                     |
	|                     |
	|                     |
	|         rr          |
	|                     |
	|                     |
	|                     |
	|                     |
	+---------------------+
	*/
			remove(r);
			break;
	//--------------------------------------------------
		case 8 + 4:
	/*
	     +-----------+
		 |     r     |
	+----+-----------+----+
	|         rr          |
	+----+-----------+----+
	     |    nr2    |
	     +-----------+
	*/
			// nr2
			nr.left    = r.left;
			nr.top     = rr.bottom;
			nr.right   = r.right;
			nr.bottom  = r.bottom;
			insert(nr);

			// r
			r.bottom  = rr.top;

			break;
	//--------------------------------------------------
		case 1 + 2:
	/*
	         +---+
	         |   |
	     +---+   +---+
	     |   |   |   |
	     |   |   |   |
		 | r |rr |nr2|
	     |   |   |   |
	     |   |   |   |
	     +---+   +---+
	         |   |
	         +---+
	*/
			// nr2
			nr.left    = rr.right;
			nr.top     = r.top;
			nr.right   = r.right;
			nr.bottom  = r.bottom;
			insert(nr);

			// r
			r.right   = rr.left;

			break;
	//--------------------------------------------------
		case 8 + 2:
	/*
	+------+
	|      |
	|      +---------+
	|  rr  |         |
	|      |   nr1   |
	|      |         |
	+----+-+---------+
	     |     nr2   |
	     +-----------+
	*/
			// nr1
			nr.left    = rr.right;
			nr.top     = r.top;
			nr.right   = r.right;
			nr.bottom  = rr.bottom;
			insert(nr);
			// nr2
			nr.left    = r.left;
			nr.top     = rr.bottom;
			nr.right   = r.right;
			nr.bottom  = r.bottom;
			insert(nr);

			remove(r);
			break;
	//--------------------------------------------------
		case 4 + 2:
	/*
	               +------+
	               |      |
	     +---------+      |
	     |         |  rr  |
		 |    r    |      |
	     |         |      |
	     +---------+-+----+
	     |   nr2     |
	     +-----------+
	*/
			// nr2
			nr.left    = r.left;
			nr.top     = rr.bottom;
			nr.right   = r.right;
			nr.bottom  = r.bottom;
			insert(nr);

			// r
			r.bottom  = rr.bottom;
			r.right   = rr.left;

			break;
	//--------------------------------------------------
		case 4 + 1:
	/*
		 +-----------+
		 |     r     |
		 +---------+-+----+
		 |         |      |
		 |    nr2  |      |
		 |         |  rr  |
		 +---------+      |
				   |      |
				   +------+
	*/
			// nr2
			nr.left    = r.left;
			nr.top     = rr.top;
			nr.right   = rr.left;
			nr.bottom  = r.bottom;
			insert(nr);

			// r
			r.bottom  = rr.top;

			break;
	//--------------------------------------------------
		case 8 + 1:
	/*
	     +-----------+
		 |      r    |
	+----+-+---------+
	|      |         |
	|      |   nr2   |
	|  rr  |         |
	|      +---------+
	|      |
	+------+
	*/
			// nr2
			nr.left    = rr.right;
			nr.top     = rr.top;
			nr.right   = r.right;
			nr.bottom  = r.bottom;
			insert(nr);

			// r
			r.bottom  = rr.top;

			break;
	//--------------------------------------------------
		case 8 + 4 + 2:
	/*
	+---------------------+
	|                     |
	|                     |
	|          rr         |
	|                     |
	|                     |
	+----+-----------+----+
		 |     nr    |
		 +-----------+
	*/
			// nr
			nr.left    = r.left;
			nr.top     = rr.bottom;
			nr.right   = r.right;
			nr.bottom  = r.bottom;
			insert(nr);

			remove(r);
			break;
	//--------------------------------------------------
		case 4 + 2 + 1:
	/*
			 +------------+
			 |            |
		 +---+            |
		 |   |            |
		 |   |            |
		 | r |     rr     |
		 |   |            |
		 |   |            |
		 +---+            |
			 |            |
			 +------------+
	*/
			// r
			r.right   = rr.left;

			break;
	//--------------------------------------------------
		case 8 + 4 + 1:
	/*
		 +-----------+
		 |     r     |
	+----+-----------+----+
	|                     |
	|                     |
	|          rr         |
	|                     |
	|                     |
	+---------------------+
	*/
			// r
			r.bottom  = rr.top;

			break;
	//--------------------------------------------------
		case 8 + 2 + 1:
	/*
	+------------+
	|            |
	|            +---+
	|            |   |
	|            |   |
	|    rr      |nr |
	|            |   |
	|            |   |
	|            +---+
	|            |
	+------------+
	*/
			// nr
			nr.left    = rr.right;
			nr.top     = r.top;
			nr.right   = r.right;
			nr.bottom  = r.bottom;
			insert(nr);

			remove(r);
			break;
	//--------------------------------------------------
		case 8:
	/*
	     +-----------+
		 |      r    |
	+----+-+---------+
	|  rr  |   nr2   |
	+----+-+---------+
	     |     nr3   |
	     +-----------+
	*/
			// nr2
			nr.left    = rr.right;
			nr.top     = rr.top;
			nr.right   = r.right;
			nr.bottom  = rr.bottom;
			insert(nr);
			// nr3
			nr.left    = r.left;
			nr.top     = rr.bottom;
			nr.right   = r.right;
			nr.bottom  = r.bottom;
			insert(nr);
			// r
			r.bottom  = rr.top;

			break;
	//--------------------------------------------------
		case 4:
	/*
	     +-----------+
		 |    r      |
		 +---------+-+----+
	     |   nr2   |  rr  |
	     +---------+-+----+
	     |   nr3     |
	     +-----------+
	*/
			// nr2
			nr.left    = r.left;
			nr.top     = rr.top;
			nr.right   = rr.left;
			nr.bottom  = rr.bottom;
			insert(nr);
			// nr3
			nr.left    = r.left;
			nr.top     = rr.bottom;
			nr.right   = r.right;
			nr.bottom  = r.bottom;
			insert(nr);

			// r
			r.bottom  = rr.top;

			break;
	//--------------------------------------------------
		case 2:
	/*
	         +---+
	         |   |
	     +---+ rr+---+
		 | r |   |nr2|
	     +---+---+---+
	     |           |
	     |    nr3    |
	     |           |
	     +-----------+
	*/
			// nr2
			nr.left    = rr.right;
			nr.top     = r.top;
			nr.right   = r.right;
			nr.bottom  = rr.bottom;
			insert(nr);
			// nr3
			nr.left    = r.left;
			nr.top     = rr.bottom;
			nr.right   = r.right;
			nr.bottom  = r.bottom;
			insert(nr);

			// r
			r.right   = rr.left;
			r.bottom  = rr.bottom;

			break;
	//--------------------------------------------------
		case 1:
	/*
	     +-----------+
	     |           |
		 |     r     |
	     |           |
	     +---+---+---+
	     |nr2|   |nr3|
	     +---+rr +---+
	         |   |
	         +---+
	*/
			// nr2
			nr.left    = r.left;
			nr.top     = rr.top;
			nr.right   = rr.left;
			nr.bottom  = r.bottom;
			insert(nr);
			// nr3
			nr.left    = rr.right;
			nr.top     = rr.top;
			nr.right   = r.right;
			nr.bottom  = r.bottom;
			insert(nr);

			// r
			r.bottom  = rr.top;

			break;

		default:
			return  ;
		}
	}

	public void addOffsets( int x, int y) {
		// Add offsets to rectangles
		if(Count == 0) return; // nothing to do

		// for bounding rectangle
		Bound.addOffsets(x, y);

		// process per a rectangle
		RegionRect cur = Head;
		do {
			cur.addOffsets(x, y);
			cur = cur.Next;
		} while(cur != Head);
	}

	// iterator
	public Iterator getIterator() {
		if(Count!=0) return new Iterator(Head);
		else return new Iterator(null);
	}

	// debug
	public void dumpChain() {
		StringBuilder builder = new StringBuilder(256);
		Iterator it = getIterator();
		while(it.step()) {
			builder.append( String.format( "%x (%x) %x : ", it.get().Prev.hashCode(), it.get().hashCode(), it.get().Next.hashCode() ) );
		}
		Logger.log(builder.toString());
	}
}
