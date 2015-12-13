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

