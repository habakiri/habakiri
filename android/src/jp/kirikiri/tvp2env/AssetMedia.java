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
package jp.kirikiri.tvp2env;

import java.io.IOException;
import java.util.HashMap;

import android.content.res.AssetManager;
import android.util.Log;
import jp.kirikiri.tjs2.BinaryStream;
import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tvp2.TVP;
import jp.kirikiri.tvp2.base.StorageLister;
import jp.kirikiri.tvp2.base.StorageMedia;
import jp.kirikiri.tvp2.msg.Message;


public class AssetMedia implements StorageMedia {

	private HashMap<String,String[]> mPathCache;
	private static final String SCHEME = "asset";
	private AssetManager mManager;

	public AssetMedia() {
		mManager = TVP.Application.get().getAssets();
		mPathCache = new HashMap<String,String[]>();
	}
	/**
	 * returns media name like "file", "http" etc.
	 */
	public String getName() { return SCHEME; }

	/**
	 * normalize domain name according with the media's rule
	 */
	public String normalizeDomainName( final String name ) {
		if( name == null ) return null;
		// make all characters small
		return name.toLowerCase();
	}

	/**
	 * normalize path name according with the media's rule
	 * "name" below is normalized but does not contain media, eg.
	 * not "media://domain/path" but "domain/path"
	 */
	public String normalizePathName( final String name ) {
		// make all characters small
		return name.toLowerCase();
	}


	/**
	 * check file existence
	 * @throws TJSException
	 */
	public boolean checkExistentStorage( final String name ) throws TJSException {
		if( name.length() == 0 ) return false;
		String _name = getLocalName(name);

		try {
			String filename;
			String[] filelist;
			int pos = _name.lastIndexOf('/');
			String pathname;
			if( pos != -1 ) {
				pathname = _name.substring(0,pos);
				//filelist = mManager.list( _name.substring(0,pos) );
				filename = _name.substring(pos+1);
			} else {
				pathname = "";
				//filelist = mManager.list( "./" );
				//filelist = mManager.list( "" );
				filename = _name;
			}
			filelist = mPathCache.get(pathname);
			if( filelist == null ) {
				filelist = mManager.list( pathname );
				mPathCache.put(pathname, filelist);
			}

			final int count = filelist.length;
			for( int i = 0; i < count; i++ ) {
				if( filename.equalsIgnoreCase(filelist[i]) ) {
					return true;
				}
			}
		} catch (IOException e) {
			Message.throwExceptionMessage( Message.CannotOpenStorage, name );
		}
		return false;
	}

	/**
	 * open a storage and return a tTJSBinaryStream instance.
	 * @param name does not contain in-archive storage name but is normalized.
	 * @throws TJSException
	 */
	public BinaryStream open( final String name, int flags ) throws TJSException {
		// open storage named "name".
		// currently only local/network(by OS) storage systems are supported.
		if( name == null | name.length() == 0 )
			Message.throwExceptionMessage( Message.CannotOpenStorage, "\"\"" );

		String origname = name;
		String _name = getCaseSensitiveName(getLocalName(name));
		return new AssetFileStream( mManager, origname, _name, flags );
	}

	/**
	 * list files at given place
	 * @throws TJSException
	 */
	public void getListAt( final String name, StorageLister lister ) throws TJSException {
		String localName = getLocalName(name);

		try {
			int pos = localName.lastIndexOf('/');
			if( pos != -1 ) {
				String pathname = localName.substring(0,pos);
				String[] filelist;
				filelist = mPathCache.get(pathname);
				if( filelist == null ) {
					filelist = mManager.list( pathname );
					mPathCache.put(pathname, filelist);
				}
				final int count = filelist.length;
				for( int i = 0; i < count; i++ ) {
					lister.add( filelist[i].toLowerCase() );
				}
			}
		} catch (IOException e) {
			Message.throwExceptionMessage( Message.CannotOpenStorage, name );
		}
	}

	/**
	 * basically the same as above,
	 * check wether given name is easily accessible from local OS filesystem.
	 * if true, returns local OS native name. otherwise returns an empty string.
	 */
	public String getLocallyAccessibleName( final String name ) {
		StringBuilder newname = new StringBuilder(256);
		int start = 0;
		final int count = name.length();
		if( count > 0 && name.charAt(0) == '.' ) start++; // 最初に . がある場合はスキップ
		while( count > start && (name.charAt(start) == '/' || name.charAt(start) == '\\') ) start++; // 最初に / \ がある場合はスキップ

		while( start < count ) {
			char c = name.charAt(start);
			if( c != '\\' ) {
				newname.append(c);
			} else {
				newname.append('/'); // \ は / に置換する
			}
			start++;
		}
		return newname.toString();
	}

	public String getLocalName( final String name ) throws TJSException {
		String tmp = getLocallyAccessibleName(name);
		if( tmp == null | tmp.length() == 0 ) Message.throwExceptionMessage( Message.CannotGetLocalName, name );
		return tmp;
	}

	@Override
	public String getDefaultDomain() {
		return null;
	}

	public String getCaseSensitiveName( final String name ) throws TJSException {
		if( name.length() == 0 ) return null;
		StringBuilder builder = new StringBuilder(256);
		try {
			String filename;
			String[] filelist;
			int pos = name.lastIndexOf('/');
			String pathname;
			if( pos != -1 ) {
				pathname = name.substring(0,pos);
				filename = name.substring(pos+1);
			} else {
				pathname = "";
				filename = name;
			}
			filelist = mPathCache.get(pathname);
			if( filelist == null ) {
				filelist = mManager.list( pathname );
				mPathCache.put(pathname, filelist);
			}

			final int count = filelist.length;
			for( int i = 0; i < count; i++ ) {
				if( filename.equalsIgnoreCase(filelist[i]) ) {
					if( pathname.length() > 0 ) {
						builder.append(pathname);
						builder.append('/');
					}
					builder.append( filelist[i] );
					return builder.toString();
				}
			}
		} catch (IOException e) {
			Message.throwExceptionMessage( Message.CannotOpenStorage, name );
		}
		return null;
	}
}
