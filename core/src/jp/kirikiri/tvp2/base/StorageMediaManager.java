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


import java.util.HashMap;

import jp.kirikiri.tjs2.BinaryStream;
import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tvp2.TVP;
import jp.kirikiri.tvp2.msg.Message;


public class StorageMediaManager {
	private static String CurrentMedia;
	private static StringBuilder mBuilder;
	public static void initialize() {
		CurrentMedia = null;
		mBuilder = new StringBuilder(256);
	}
	public static void finalizeApplication() {
		CurrentMedia = null;
		mBuilder = null;
	}

	static class MediaRecord {
		public String CurrentDomain;
		public String CurrentPath;
		public StorageMedia MediaIntf;
		public int MediaNameLen;

		public MediaRecord( StorageMedia media ) {
			MediaIntf = media;
			//CurrentDomain = ".";
			CurrentDomain = media.getDefaultDomain();
			CurrentPath = "/";
			String name = media.getName();
			MediaNameLen = name.length();
		}

		String getDomainAndPath( final String name ) {
			return name.substring( MediaNameLen + 3 ); // 3 = strlen("://")
		}
	}
	static class MediaData {
		public String media;
		public String domain;
		public String path;
	}

	private HashMap<String,MediaRecord> mHashTable;

	public StorageMediaManager() throws TJSException {
		mHashTable = new HashMap<String,MediaRecord>();
		TVP.Application.get().registerFileMedia( this );
	}

	private static void throwUnsupportedMediaType( final String name ) throws TJSException {
		Message.throwExceptionMessage( Message.UnsupportedMediaName, extractMediaName(name) );
	}

	private MediaRecord getMediaRecord( final String name ) throws TJSException {
		String media = extractMediaName(name);
		MediaRecord rec = mHashTable.get( media );
		if( rec == null ) throwUnsupportedMediaType(name);
		return rec;
	}

	public void register( StorageMedia media ) throws TJSException {
		String medianame = media.getName();

		MediaRecord rec = mHashTable.get( medianame );
		if( rec != null ) {
			Message.throwExceptionMessage( "Media name \"" + medianame + "\" had already been registered" );
		}
		MediaRecord new_rec = new MediaRecord(media);
		mHashTable.put( medianame, new_rec );
	}
	public void Unregister( StorageMedia media ) throws TJSException {
		String medianame = media.getName();
		MediaRecord rec = mHashTable.get( medianame );
		if( rec == null ) {
			Message.throwExceptionMessage( "Media name \"" + medianame + "\" is not registered" );
		}
		mHashTable.remove( medianame );
	}

	public static String preNormalizeStorageName( final String name ) {
		// if the name is an OS's native expression, change it according with the
		// TVP storage system naming rule.
		final int namelen = name.length();
		if( namelen == 0 ) return name;

		if( namelen >= 2 ) {
			char c0 = name.charAt(0);
			if( ( c0 >= 'a' && c0 <= 'z' || c0 >= 'A' && c0 <= 'Z' ) && name.charAt(1) == ':' ) {
				// Windows drive:path expression
				mBuilder.delete(0, mBuilder.length() );
				StringBuilder builder = mBuilder;
				builder.append( "file://./" );
				builder.append( c0 );
				builder.append( name.substring(2) );
				return builder.toString();
			}
		}

		if( namelen >= 3 ) {
			char c0 = name.charAt(0);
			char c1 = name.charAt(1);
			if( c0 == '\\' && c1 == '\\' || c0 == '/' && c1 == '/' ) {
				// unc expression
				mBuilder.delete(0, mBuilder.length() );
				StringBuilder builder = mBuilder;
				builder.append( "file:" );
				builder.append( name );
				return builder.toString();
			}
		}
		return name;
	}
	/**
	 * TODO このJavaのコードだと効率が悪いので、書き換えること
	 * scheme://domain/path の形式にする
	 * Normalize storage name.

	 * storage name is basically in following form:
	 * media://domain/path

	 * media is sort of access method, like "file", "http" ...etc.
	 * domain represents in which computer the data is.
	 * path is where the data is in the computer.
	 * @throws TJSException
	 */
	public String normalizeStorageName( final String name, MediaData ret_media ) throws TJSException {
		// empty check
		if( name == null || name.length() == 0 ) return name; // empty name is empty name

		// pre-normalize
		String tmp = preNormalizeStorageName(name);

		// unify path delimiter
		tmp = tmp.replace( '\\', '/' );

		mBuilder.delete(0, mBuilder.length() );
		StringBuilder builder = mBuilder;
		// save in-archive storage name and normalize it
		String inarchive_name = null;
		boolean inarc_name_found = false;
		int pa = tmp.indexOf(Storage.ArchiveDelimiter);
		if( pa != -1 ) {
			inarchive_name = tmp.substring( pa + 1 );
			inarchive_name = Archive.normalizeInArchiveStorageName( inarchive_name );
			inarc_name_found = true;
			tmp = tmp.substring(0,pa);
		}
		if( tmp.length() == 0 ) Message.throwExceptionMessage( Message.InvalidPathName, name );


		// split the name into media, domain, path
		// (and guess what component is omitted)
		String media = null;
		String domain = null;
		String path = null;

		// - find media name
		//   media name is: /^[A-Za-z]+:/
		int length = tmp.length();
		int i = 0;
		for( ; i < length; i++ ) {
			char c = tmp.charAt(i);
			if( !( c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z' ) ) break;
		}

		if( i < length && tmp.charAt(i) == ':' ) {
			// media name found
			media = tmp.substring( 0, i );
			i++;
		} else {
			i = 0;
		}

		// - find domain name
		// at this place, pa may point one of following:
		//  ///path        (domain is omitted)
		//  //domain/path  (none is omitted)
		//  /path          (domain is omitted)
		//  relative-path  (domain and current path are omitted)
		if( tmp.charAt(i) == '/' ) {
			if( tmp.charAt(i+1) == '/' ) {
				if( tmp.charAt(i+2) == '/' ) {
					// slash count 3: domain is ommited
					i += 2;
				} else {
					// slash count 2: none is omitted
					i += 2;
					// find '/' as a domain delimiter
					int pc = tmp.indexOf( '/', i );
					if( pc == -1 ) {
						Message.throwExceptionMessage( Message.InvalidPathName, name );
					}
					domain = tmp.substring( i, pc );
					i = pc;
				}
			} else {
				// slash count 1: domain is omitted
			}
		}

		// - get path name
		path = tmp.substring(i);

		// supply omitted and normalize
		if( media == null || media.length() == 0 ) {
			media = CurrentMedia;
		} else {
			// normalize media name ( make them all small )
			media = media.toLowerCase();
		}
		MediaRecord mediarec = getMediaRecord(media);

		if( domain == null || domain.length() == 0 ) {
			if( mediarec.CurrentDomain != null ) {
				domain = new String(mediarec.CurrentDomain);
			} else {
				domain = "";
			}
		}
		domain = mediarec.MediaIntf.normalizeDomainName(domain);

		if(path == null || path.length() == 0 ) {
			path = "/";
		} else if( path.charAt(0) != '/' ) {
			builder.append( mediarec.CurrentPath );
			builder.append( path );
			path = builder.toString();
			builder.delete( 0, builder.length() );
		}
		path = mediarec.MediaIntf.normalizePathName(path);

		// compress redudant path accesses
		if( inarc_name_found ) {
			builder.append( path );
			builder.append( Storage.ArchiveDelimiter );
			//builder.append( (char)0 );
			if( inarchive_name != null ) {
				builder.append( inarchive_name );
			}
			path = builder.toString();
			builder.delete( 0, builder.length() );
		}

		pa = 0;
		int pb = 0;
		int pc = 0; // pa = read pointer, pb = write pointer, pc = start
		int dot_count = -1;
		char[] pathArray = path.toCharArray();
		final int pathLen = pathArray.length;
		while( pa < pathLen ) {
			char ca = pathArray[pa];
			if( ca == Storage.ArchiveDelimiter || ca == '/' || ca == 0 ) {
				char delim = 0;
				if( ca != 0 && dot_count == 0 ) {
					// duplicated slashes
					pb--;
				} else if( dot_count > 0 ) {
					pb--;
					while( pb >= pc ) {
						char cb = pathArray[pb];
						if( cb == '/' || cb == Storage.ArchiveDelimiter ) {
							dot_count--;
							if( dot_count == 0 ) {
								delim = cb;
								break;
							}
							if( cb == Storage.ArchiveDelimiter ) {
								Message.throwExceptionMessage( Message.InvalidPathName, name );
							}
						}
						pb--;
					}
					if( pb < pc ) {
						Message.throwExceptionMessage( Message.InvalidPathName, name );
					}
				}

				if( delim == 0)
					pathArray[pb] = ca;
				else
					pathArray[pb] = delim;
				if( ca == 0 ) break;

				pb++;
				pa++;
				dot_count = 0;
			} else if( ca == '.' ) {
				pathArray[pb] = ca;
				pb++;
				pa++;
				if(dot_count != -1) dot_count ++;
			} else {
				pathArray[pb] = ca;
				pb++;
				pa++;
				dot_count = -1;
			}
		}
		int l = 0;
		for( ; l < pathLen; l++ ) {
			if( pathArray[l] == 0 ) break;
		}
		path = new String( pathArray, 0, l );

		// merge and return normalize storage name
		if( ret_media != null ) {
			ret_media.media = media;
			ret_media.domain = domain;
			ret_media.path = path;
		}
		builder.append( media );
		builder.append( "://" );
		builder.append( domain );
		builder.append( path );
		return builder.toString();
	}

	public void setCurrentDirectory( final String name ) throws TJSException {
		char ch = name.charAt( name.length() - 1 );
		if( ch != '/' && ch != '\\' && ch != Storage.ArchiveDelimiter )
			Message.throwExceptionMessage( Message.MissingPathDelimiterAtLast );

		MediaData data = new MediaData();
		normalizeStorageName( name, data );

		MediaRecord rec = getMediaRecord(data.media);
		rec.CurrentDomain = data.domain;
		rec.CurrentPath = data.path;
		CurrentMedia = data.media;
	}
	public void setCurrentMediaName( final String name ) {
		CurrentMedia = name;
	}

	/**
	 * extract media name from normalized storage named "name".
	 * @return media name does not contain colon.
	 */
	public static String extractMediaName( final String name ) {
		if( name == null ) return null;
		int idx = name.indexOf(':');
		if( idx != -1 ) {
			return name.substring( 0, idx );
		} else {
			return new String(name);
		}
	}

	/**
	 * gateway for checkExistentStorage
	 * @param name must not be an in-archive storage name
	 * @throws TJSException
	 */
	public boolean checkExistentStorage( final String name ) throws TJSException {
		MediaRecord rec = getMediaRecord(name);
		return rec.MediaIntf.checkExistentStorage( rec.getDomainAndPath(name) );
	}

	/**
	 * gateway for Open
	 * @param name must not be an in-archive storage name
	 * @throws TJSException
	 */
	public BinaryStream open( final String name, int flags ) throws TJSException {
		MediaRecord rec = getMediaRecord(name);
		return rec.MediaIntf.open(rec.getDomainAndPath(name), flags);
	}
	/**
	 * gateway for GetListAt
	 * @param name must not be an in-archive storage name
	 * @throws TJSException
	 */
	public void getListAt( final String name, StorageLister lister ) throws TJSException  {
		MediaRecord rec = getMediaRecord(name);
		rec.MediaIntf.getListAt( rec.getDomainAndPath(name), lister );
	}
	/**
	 * gateway for GetLocallyAccessibleName
	 * @param name must not be an in-archive storage name
	 * @throws TJSException
	 */
	public String getLocallyAccessibleName( final String name ) throws TJSException {
		MediaRecord rec = getMediaRecord(name);
		String dname = rec.getDomainAndPath(name);
		return rec.MediaIntf.getLocallyAccessibleName(dname);
	}
	public String getLocalName( final String name ) throws TJSException {
		String tmp = getLocallyAccessibleName(name);
		if(tmp == null || tmp.length() == 0 ) Message.throwExceptionMessage(Message.CannotGetLocalName, name);
		return tmp;
	}
}

