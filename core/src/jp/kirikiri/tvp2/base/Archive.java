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
package jp.kirikiri.tvp2.base;

import java.util.HashMap;

import jp.kirikiri.tjs2.BinaryStream;
import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tvp2.msg.Message;

public abstract class Archive {

	private HashMap<String, Integer> mHash;
	private boolean mInit;
	private String mArchiveName;

	//-- constructor
	public Archive( final String name ) {
		mArchiveName = name;
		//mInit = false;
		mHash = new HashMap<String, Integer>();
	}

	public abstract int getCount();

	/**
	 * @return name must be already normalized using NormalizeInArchiveStorageName
	 * and the index must be sorted by its name.
	 * this is needed by fast directory search.
	 */
	public abstract String getName( int idx);

	public abstract BinaryStream createStreamByIndex( int idx ) throws TJSException;


	public static String normalizeInArchiveStorageName( final String name ) {
		// normalization of in-archive storage name does :
		if( name == null || name.length() == 0 ) return null;

		// make all characters small
		// change '\\' to '/'
		String tmp = name.toLowerCase();
		tmp = tmp.replace('\\','/');

		// eliminate duplicated slashes
		char[] ptr = tmp.toCharArray();
		final int len = ptr.length;
		int dest = 0;
		for( int i = 0; i < len; ) {
			if( ptr[i] != '/' ) {
				ptr[dest] = ptr[i];
				i++;
				dest++;
			} else {
				if( i != 0 ) {
					ptr[dest] = ptr[i];
					i++;
					dest++;
				}
				while( i < len && ptr[i] == '/' ) i++;
			}
		}
		return new String( ptr, 0, dest );
	}

	private void addToHash() {
		// enter all names to the hash table
		final int count = getCount();
		for( int i = 0; i < count; i++ ) {
			String name = getName(i);
			name = normalizeInArchiveStorageName(name);
			mHash.put( name, i );
		}
	}
	public BinaryStream createStream( final String name ) throws TJSException {
		if( name == null || name.length() == 0 ) return null;

		if( !mInit ) {
			mInit = true;
			addToHash();
		}

		Integer p = mHash.get(name);
		if( p == null ) Message.throwExceptionMessage( Message.StorageInArchiveNotFound, name, mArchiveName );
		return createStreamByIndex(p);

	}
	public boolean isExistent( final String name ) {
		if( name == null || name.length() == 0 ) return false;
		if( !mInit ) {
			mInit = true;
			addToHash();
		}
		return mHash.get(name) != null;
	}

	/**
	 * the item must be sorted by operator < , otherwise this function
	 * will not work propertly.
	 * @return first index which have 'prefix' at start of the name.
	 * @return -1 if the target is not found.
	 */
	public int getFirstIndexStartsWith( final String prefix ) {
		int total_count = getCount();
		int s = 0, e = total_count;
		while( e - s > 1 ) {
			int m = (e + s) / 2;
			if( !(getName(m).compareTo(prefix) < 0) ) {
				// m is after or at the target
				e = m;
			} else {
				// m is before the target
				s = m;
			}
		}

		// at this point, s or s+1 should point the target.
		// be certain.
		if( s >= total_count) return -1; // out of the index
		if( getName(s).startsWith(prefix) ) return s;
		s++;
		if( s >= total_count ) return -1; // out of the index
		if( getName(s).startsWith(prefix) ) return s;
		return -1;
	}
}
