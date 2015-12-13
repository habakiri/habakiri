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
/*
 * オブジェクトを使わないように個別定義
 */
package jp.kirikiri.tjs2;


public class IntVector {
	private static final int DEFAULT_SIZE = 16;

	private int[] mItems;
	private int mIndex;

	public IntVector() {
		this(DEFAULT_SIZE);
	}
	public IntVector(int initialCapacity) {
		mItems = new int[initialCapacity];
	}
	public IntVector( int[] datasrc, int index ) {
		mItems = new int[index];
		mIndex = index;
		for( int i = 0; i < index; i++ ) {
			mItems[i] = datasrc[i];
		}
	}
	private IntVector( int[] src ) {
		mItems = src;
		mIndex = src.length;
	}
	public static IntVector wrap( int[] src ) {
		return new IntVector(src);
	}

	private final void resize() {
		final int count = mItems.length * 2;
		int[] newArray = new int[count];
		System.arraycopy( mItems, 0, newArray, 0, mItems.length );
		mItems = null;
		mItems = newArray;
	}

	public final void clear() {
		mIndex = 0;
	}
	public final void push_back( int val ) {
		if( mIndex < mItems.length ) {
			mItems[mIndex] = val;
			mIndex++;
		} else {
			resize();
			if( mIndex < mItems.length ) {
				mItems[mIndex] = val;
				mIndex++;
			} else {
				throw new OutOfMemoryError( Error.InternalError );
			}
		}
	}
	public final void add( int val ) {
		if( mIndex < mItems.length ) {
			mItems[mIndex] = val;
			mIndex++;
		} else {
			resize();
			if( mIndex < mItems.length ) {
				mItems[mIndex] = val;
				mIndex++;
			} else {
				throw new OutOfMemoryError( Error.InternalError );
			}
		}
	}
	public final int size() { return mIndex; }

	public final int lastElement() throws IndexOutOfBoundsException {
		if( mIndex == 0 ) throw new IndexOutOfBoundsException();
		return mItems[mIndex-1];
	}
	public final int back() throws IndexOutOfBoundsException {
		if( mIndex == 0 ) throw new IndexOutOfBoundsException();
		return mItems[mIndex-1];
	}
	public final int top() throws IndexOutOfBoundsException {
		if( mIndex == 0 ) throw new IndexOutOfBoundsException();
		return mItems[0];
	}
	public final void pop_back() {
		if( mIndex > 0 ) {
			mIndex--;
		}
	}
	public final void set( int index, int element ) {
		if( index >= 0 && index < mIndex ) {
			mItems[index] = element;
		}
	}
	public final int get( int index ) throws IndexOutOfBoundsException {
		if( index < 0 && index >= mIndex ) throw new IndexOutOfBoundsException();
		return mItems[index];
	}
	public final void remove( int index ) throws IndexOutOfBoundsException {
		if( index < 0 && index >= mIndex ) throw new IndexOutOfBoundsException();

		final int count = mIndex - 1;
		for( int i = index; i < count; i++ ) {
			mItems[i] = mItems[i+1];
		}
		mIndex--;
	}

	public IntVector clone() {
		return new IntVector( mItems, mIndex );
	}
	public boolean isEmpty() { return mIndex == 0; }

	public int[] toArray() {
		int[] ret = new int[mIndex];
		/*
		for( int i = 0; i < mIndex; i++ ) {
			ret[i] = mItems[i];
		}
		*/
		System.arraycopy( mItems, 0, ret, 0, mIndex );
		return ret;
	}
	public int[] getRowArray() {
		return mItems;
	}
}

