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
package jp.kirikiri.tjs2;

public class LocalNamespace {
	private VectorWrap<LocalSymbolList> mLevels;
	private int mMaxCount;
	private int mCurrentCount;
	private MaxCountWriter mMaxCountWriter;

	public LocalNamespace() {
		//mMaxCount = 0;
		//mCurrentCount = 0;
		mLevels = new VectorWrap<LocalSymbolList>();
	}
	public void setMaxCountWriter( MaxCountWriter writer ) {
		mMaxCountWriter = writer;
	}
	public int getCount() {
		int count = 0;
		final int size = mLevels.size();
		for( int i = 0; i < size; i++ ) {
			LocalSymbolList list = mLevels.get(i);
			count += list.getCount();
		}
		return count;
	}
	public void push() {
		mCurrentCount = getCount();
		mLevels.add( new LocalSymbolList(mCurrentCount) );
	}
	public void pop() {
		LocalSymbolList list = mLevels.lastElement();
		commit();
		mCurrentCount = list.getLocalCountStart();
		mLevels.remove(mLevels.size()-1);
		list = null;
	}
	public int find( final String name ) {
		final int count = mLevels.size();
		for( int i = count-1; i >= 0; i-- ) {
			LocalSymbolList list = mLevels.get(i);
			int lindex = list.find( name );
			if( lindex != -1 ) {
				return lindex + list.getLocalCountStart();
			}
		}
		return -1;
	}
	public int getLevel() { return mLevels.size(); }
	public void add( final String name ) {
		LocalSymbolList top = getTopSymbolList();
		if( top == null ) return;
		top.add( name );
	}
	public void remove( final String name ) {
		final int count = mLevels.size();
		for( int i = count-1; i >= 0; i-- ) {
			LocalSymbolList list = mLevels.get(i);
			int lindex = list.find( name );
			if( lindex != -1 ) {
				list.remove( lindex );
				return;
			}
		}
	}
	public void commit() {
		int count = 0;
		for( int i = mLevels.size()-1; i >= 0; i-- ) {
			LocalSymbolList list = mLevels.get(i);
			count += list.getCount();
		}
		if( mMaxCount < count ) {
			mMaxCount = count;
			if( mMaxCountWriter != null ) mMaxCountWriter.setMaxCount(count);
		}
	}
	public LocalSymbolList getTopSymbolList() {
		if( mLevels.size() == 0 ) return null;
		return mLevels.lastElement();
	}
	public void clear() {
		while( mLevels.size() > 0 ) pop();
	}
	public int getMaxCount() { return mMaxCount; }
}
