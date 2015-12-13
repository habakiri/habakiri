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

import java.util.ArrayList;
import java.util.HashMap;

import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tvp2.TVP;
import jp.kirikiri.tvp2.msg.Message;
import jp.kirikiri.tvp2.utils.DebugClass;


public class AutoPath {

	private boolean mAutoPathTableInit;
	private boolean mClearAutoPathCacheCallbackInit;
	private ArrayList<String> mAutoPathList;
	private HashMap<String,String> mAutoPathCache;
	private HashMap<String,String> mAutoPathTable;

	public AutoPath() {
		mAutoPathList = new ArrayList<String>();
		mAutoPathCache = new HashMap<String,String>();
		mAutoPathTable = new HashMap<String,String>();
		//mAutoPathTableInit = false;
		//mClearAutoPathCacheCallbackInit = false;
	}
	public void clearAutoPathCache() {
		mAutoPathCache.clear();
		mAutoPathTable.clear();
		mAutoPathTableInit = false;
	}

	public void addAutoPath( final String name ) throws TJSException {
		synchronized (TVP.CreateStreamCS) {
			char lastchar = name.charAt(name.length()-1);

			if( lastchar != Storage.ArchiveDelimiter && lastchar != '/' && lastchar != '\\' ) {
				Message.throwExceptionMessage( Message.MissingPathDelimiterAtLast );
			}

			String normalized = TVP.StorageMediaManager.normalizeStorageName( name, null );

			int idx = mAutoPathList.indexOf(normalized);
			if( idx == -1 ) {
				mAutoPathList.add(normalized);
			}

			clearAutoPathCache();
		}
	}
	public void removeAutoPath( final String name ) throws TJSException {
		synchronized (TVP.CreateStreamCS) {
			char lastchar = name.charAt(name.length()-1);
			if( lastchar != Storage.ArchiveDelimiter && lastchar != '/' && lastchar != '\\' )
				Message.throwExceptionMessage( Message.MissingPathDelimiterAtLast );

			String normalized = TVP.StorageMediaManager.normalizeStorageName(name,null);

			int idx = mAutoPathList.indexOf(normalized);
			if( idx != -1 ) {
				mAutoPathList.remove(idx);
			}
			clearAutoPathCache();
		}
	}
	static class Lister implements StorageLister {
		public ArrayList<String> list;
		public Lister() {
			list = new ArrayList<String>();
		}
		public void add( final String file ) {
			list.add(file);
		}
	}
	public int rebuildAutoPathTable() throws TJSException {
		// rebuild auto path table
		if( mAutoPathTableInit ) return 0;
		synchronized (TVP.CreateStreamCS) {

			mAutoPathTable.clear();

			long tick = System.currentTimeMillis();
			DebugClass.addLog( "(info) Rebuilding Auto Path Table ..." );

			int totalcount = 0;

			final int pathcount = mAutoPathList.size();
			for( int i = 0; i < pathcount; i++ ) {
				final String path = mAutoPathList.get(i);
				int count = 0;

				int sharp_pos = path.indexOf( Storage.ArchiveDelimiter );
				if( sharp_pos != -1) {
					// this storagename indicates a file in an archive

					String arcname = path.substring( 0, sharp_pos );
					String in_arc_name = path.substring( sharp_pos + 1 );
					in_arc_name = Archive.normalizeInArchiveStorageName(in_arc_name);
					if( in_arc_name == null ) in_arc_name = "";
					int in_arc_name_len = in_arc_name.length();

					Archive arc = TVP.ArchiveCache.get(arcname);

					try {
						int storagecount = arc.getCount();

						// get first index which the item has 'in_arc_name' as its start
						// of the string.
						int idx = arc.getFirstIndexStartsWith(in_arc_name);
						if( idx != -1 ) {
							for(; idx < storagecount; idx++) {
								String name = arc.getName(idx);
								if( name.startsWith(in_arc_name) ) {
									if( name.indexOf( '/', in_arc_name_len ) == -1 ) {
										String sname = Storage.extractStorageName(name);
										mAutoPathTable.put(sname, path);
										count ++;
									}
								} else {
									// no need to check more;
									// because the list is sorted by the name.
									break;
								}
							}
						}
					} finally {
						arc = null;
					}
				} else {
					// normal folder
					Lister lister = new Lister();
					TVP.StorageMediaManager.getListAt( path, lister );
					ArrayList<String> list = lister.list;
					final int listcount = list.size();
					for( int j = 0; j < listcount; j++ ) {
						String pname = list.get(j);
						mAutoPathTable.put( pname, path );
						count++;
					}
				}
				totalcount += count;
			}

			long endtick = System.currentTimeMillis();
			StringBuilder builder = new StringBuilder(256);
			builder.append( "(info) Total " );
			builder.append( totalcount );
			builder.append( " file(s) found, " );
			builder.append( mAutoPathTable.size() );
			builder.append( " file(s) activated. (" );
			builder.append( endtick - tick );
			builder.append( "ms)" );
			DebugClass.addLog( builder.toString() );
			mAutoPathTableInit = true;
			return totalcount;
		}
	}
	public String getPlacedPath( final String name ) throws TJSException {
		// search path and return the path which the "name" is placed.
		// returned name is normalized. returns empty string if the storage is not
		// found.
		if( !mClearAutoPathCacheCallbackInit ) {
			TVP.CompactEvent.addCompactEventHook( new ClearAutoPathCacheCallback() );
			mClearAutoPathCacheCallbackInit = true;
		}

		String incache = mAutoPathCache.get(name);
		if( incache != null ) return incache; // found in cache

		synchronized(TVP.CreateStreamCS) {

			String normalized = TVP.StorageMediaManager.normalizeStorageName(name,null);

			boolean found = Storage.isExistentStorageNoSearchNoNormalize(normalized);
			if( found ) {
				// found in current folder
				mAutoPathCache.put( name, normalized );
				return normalized;
			}

			// not found in current folder
			// search through auto path table

			String storagename = Storage.extractStorageName(normalized);

			rebuildAutoPathTable(); // ensure auto path table
			String result = mAutoPathTable.get(storagename);
			if( result != null ) {
				// found in table
				String found1 = result + storagename;
				mAutoPathCache.put(name, found1);
				return found1;
			}

			// not found
			mAutoPathCache.put(name, "");
			return "";
		}
	}
	class  ClearAutoPathCacheCallback implements CompactEventCallbackInterface {
		@Override
		public void onCompact(int level) {
			if( level >= COMPACT_LEVEL_DEACTIVATE ) {
				synchronized(TVP.CreateStreamCS) {
					// clear the auto search path cache on application deactivate
					clearAutoPathCache();
				}
			}
		}
	}
}

