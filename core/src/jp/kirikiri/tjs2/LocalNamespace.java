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
