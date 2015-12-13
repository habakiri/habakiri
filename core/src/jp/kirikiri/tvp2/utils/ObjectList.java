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
package jp.kirikiri.tvp2.utils;

import java.util.ArrayList;

public class ObjectList<E> {

	private ArrayList<E> mObjects;
	private ArrayList<E> mBackupedObjects;
	private int mSafeLockCount;
	private boolean mBackuped;

	/**
	 * this internal function does not free the array previously allocated.
	 * only "Objects" ( not "BackupedObjects" ) is copied.
	 * @param ref
	 */
	@SuppressWarnings("unchecked")
	private final void internalAssign( final ObjectList<E> ref ) {
		if( ref.mObjects != null && ref.mObjects.size() > 0 ) {
			mObjects = (ArrayList<E>) ref.mObjects.clone();
		} else {
			mObjects = null;
		}
	}
	/**
	 * backup current "Objects" to "BackupedObjects"
	 */
	@SuppressWarnings("unchecked")
	private final void backup() {
		if( mObjects != null && mObjects.size() > 0 ) {
			mBackupedObjects = (ArrayList<E>)mObjects.clone();
		} else {
			mBackupedObjects = null;
		}
		mBackuped = true;
	}
	/**
	 * commit the current array
	 * this simply free BackupedObjects ( and its related things )
	 */
	private final void commit() {
		mBackupedObjects = null;
		mBackuped = false;
	}

	public ObjectList() {
		//mObjects = null;
		//mBackupedObjects = null;
		//mBackuped = false;
		//mSafeLockCount = 0;
	}
	public ObjectList( final ObjectList<E> ref ) {
		//mSafeLockCount = 0;
		//mBackupedObjects = null;
		//mBackuped = false;
		internalAssign(ref);
	}

	/**
	 * note that this function does not change any safe locking
	 * effects ( locking count is not changed )
	 * @param ref
	 */
	public final void assign( final ObjectList<E> ref) {
		mObjects = null;
		internalAssign(ref);
	}
	/**
	 * lock array safely
	 * safe locking is managed by reference counter;
	 */
	public final void safeLock() {
		if(mSafeLockCount == 0)
			compact(); // this is needed
		mSafeLockCount++;
	}
	public final void safeUnlock() {
		mSafeLockCount--;
		if(mSafeLockCount == 0)
			commit();
	}
	/**
	 * this function only valid in safe locking
	 * @return
	 */
	public final int getSafeLockedObjectCount() {
		if(mBackuped) {
			if( mBackupedObjects == null) return 0;
			else return mBackupedObjects.size();
		} else {
			if( mObjects == null ) return 0;
			else return mObjects.size();
		}
	}
	/**
	 * this migight contain null pointer
	 * @param index
	 * @return
	 */
	public final E getSafeLockedObjectAt( int index) {
		if( mBackuped ) {
			if( mBackupedObjects == null) return null;
			else return mBackupedObjects.get(index);
		} else {
			if( mObjects == null ) return null;
			else return mObjects.get(index);
		}
	}
	/**
	 * note that if you want to ensure the result is
	 * actual object count in the array,
	 * call compact() before getCount().
	 * @return
	 */
	public final int getCount() {
		if( mObjects == null ) return 0;
		else return mObjects.size();
	}
	/**
	 * this does compact() before returning current Count,
	 * ensuring returned count is actual object count in the array.
	 * @return
	 */
	public final int getActualCount() {
		compact();
		if( mObjects == null ) return 0;
		else return mObjects.size();
	}
	public final void setCount( int count ) {
		if(mSafeLockCount != 0 && !mBackuped) backup();
		reserve(count);
	}
	public final void reserve( int count ) {
		if(mSafeLockCount !=0 && !mBackuped) backup();
		mObjects.ensureCapacity(count);
		int i = mObjects.size();
		for( ; i < count; i++ ) {
			mObjects.add( null );
		}
	}
	/**
	 * this eliminates null pointer from the array
	 */
	public final void compact() {
		if( mSafeLockCount != 0 && !mBackuped) backup();
		if( mObjects == null ) return;
		final int count = mObjects.size();
		for( int i = count-1; i >= 0; i-- ) {
			E o = mObjects.get(i);
			if( o == null ) {
				mObjects.remove(i);
			}
		}
		if( mObjects.size() == 0 )
			mObjects = null;
	}
	/**
	 * this might return a null pointer
	 * @param index
	 * @return
	 */
	public final E get( int index ) {
		if(mSafeLockCount !=0 && !mBackuped) backup();
		if( mObjects == null) return null;
		return mObjects.get( index );
	}

	/**
	 * this might return a null pointer
	 * @param index
	 * @return
	 */
	public final E getConst( int index) {
		if( mObjects == null) return null;
		return mObjects.get( index );
	}

	/**
	 *
	 * @param index
	 * @param obj
	 * @return
	 */
	public final E set( int index, E obj ) {
		if(mSafeLockCount !=0 && !mBackuped) backup();
		if( mObjects == null) return null;
		return mObjects.set( index, obj );
	}


	/**
	 * find "object" from array
	 * return -1 if "object" does not exist
	 * @param object
	 * @return
	 */
	public final int find( E object ) {
		if( object == null ) return -1; // null cannot be finded
		if( mObjects == null ) return -1;
		final int count = mObjects.size();
		for( int i = 0; i < count; i++ ) {
			E o = mObjects.get(i);
			if( o == object ) return i;
		}
		return -1;
	}
	/**
	 * add "object" to array
	 * this does not allow duplicates
	 * @return
	 */
	public final boolean add( E object) {
		if(mSafeLockCount != 0 && !mBackuped) backup();
		if(object==null) return false; // null cannot be added
		if( find(object) == -1 ) {
			if( mObjects == null ) {
				mObjects = new ArrayList<E>();
			}
			mObjects.add( object );
			return true;
		}
		return false;
	}

	/**
	 * remove object from array
	 * (this only set the pointer to null)
	 * @param object
	 * @return
	 */
	public final boolean remove( E object ){
		if( mSafeLockCount != 0 && !mBackuped ) backup();
		int index = find(object);
		boolean ret;
		if( index != -1 ) {
			mObjects.set( index, null );
			ret = true;
		} else {
			ret = false;
		}
		if( mBackuped && object != null ) {
			// remove also from BackupedObjects
			if( mBackupedObjects != null ) {
				final int count = mBackupedObjects.size();
				for( int i = 0; i < count; i++ ) {
					E o = mBackupedObjects.get(i);
					if( o == object ) {
						mBackupedObjects.set( i, null );
						break;
					}
				}
			}
		}
		return ret;
	}

	public final void remove( int index ) {
		if( mSafeLockCount!=0 && !mBackuped) backup();
		E object = null;
		if( mObjects != null ) {
			object = mObjects.get(index);
			mObjects.set( index, null );
		}
		if( mBackuped && object != null ) {
			// remove also from BackupedObjects
			if( mBackupedObjects != null ) {
				final int count = mBackupedObjects.size();
				for( int i = 0; i < count; i++ ) {
					E o = mBackupedObjects.get(i);
					if( o == object ) {
						mBackupedObjects.set( i, null );
						break;
					}
				}
			}
		}
	}
}
