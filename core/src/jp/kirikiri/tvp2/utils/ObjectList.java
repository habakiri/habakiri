/**
 ******************************************************************************
 * Copyright (c), Takenori Imoto
 * �� software http://www.kaede-software.com/
 * All rights reserved.
 ******************************************************************************
 * �\�[�X�R�[�h�`�����o�C�i���`�����A�ύX���邩���Ȃ������킸�A�ȉ��̏�����
 * �����ꍇ�Ɍ���A�ĔЕz����юg�p��������܂��B
 *
 * �E�\�[�X�R�[�h���ĔЕz����ꍇ�A��L�̒��쌠�\���A�{�����ꗗ�A����щ��L�Ɛ�
 *   �������܂߂邱�ƁB
 * �E�o�C�i���`���ōĔЕz����ꍇ�A�Еz���ɕt���̃h�L�������g���̎����ɁA��L��
 *   ���쌠�\���A�{�����ꗗ�A����щ��L�Ɛӏ������܂߂邱�ƁB
 * �E���ʂɂ����ʂ̋��Ȃ��ɁA�{�\�t�g�E�F�A����h���������i�̐�`�܂��͔̔�
 *   ���i�ɁA�g�D�̖��O�܂��̓R���g���r���[�^�[�̖��O���g�p���Ă͂Ȃ�Ȃ��B
 *
 * �{�\�t�g�E�F�A�́A���쌠�҂���уR���g���r���[�^�[�ɂ���āu����̂܂܁v��
 * ����Ă���A�����َ����킸�A���ƓI�Ȏg�p�\���A����ѓ���̖ړI�ɑ΂���K
 * �����Ɋւ���Öق̕ۏ؂��܂߁A�܂�����Ɍ��肳��Ȃ��A�����Ȃ�ۏ؂�����܂�
 * ��B���쌠�҂��R���g���r���[�^�[���A���R�̂�������킸�A���Q�����̌�������
 * ����킸�A���ӔC�̍������_��ł��邩���i�ӔC�ł��邩�i�ߎ����̑��́j�s�@
 * �s�ׂł��邩���킸�A���ɂ��̂悤�ȑ��Q����������\����m�炳��Ă����Ƃ�
 * �Ă��A�{�\�t�g�E�F�A�̎g�p�ɂ���Ĕ��������i��֕i�܂��͑�p�T�[�r�X�̒��B�A
 * �g�p�̑r���A�f�[�^�̑r���A���v�̑r���A�Ɩ��̒��f���܂߁A�܂�����Ɍ��肳���
 * ���j���ڑ��Q�A�Ԑڑ��Q�A�����I�ȑ��Q�A���ʑ��Q�A�����I���Q�A�܂��͌��ʑ��Q��
 * ���āA��ؐӔC�𕉂�Ȃ����̂Ƃ��܂��B
 ******************************************************************************
 * �{�\�t�g�E�F�A�́A�g���g��2 ( http://kikyou.info/tvp/ ) �̃\�[�X�R�[�h��Java
 * �ɏ������������̂��ꕔ�g�p���Ă��܂��B
 * �g���g��2 Copyright (C) W.Dee <dee@kikyou.info> and contributors
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
