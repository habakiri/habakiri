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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import jp.kirikiri.tjs2.BinaryStream;
import jp.kirikiri.tjs2.Error;
import jp.kirikiri.tjs2.LexBase;
import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tvp2.TVP;
import jp.kirikiri.tvp2.msg.Message;
import jp.kirikiri.tvp2.utils.DebugClass;


public class Storage {

	public static final char ArchiveDelimiter = '>';
	private static Inflater Decompresser;

	public static final void initialize() {
		Decompresser = null;
	}
	public static void finalizeApplication() {
		Decompresser = null;
	}
	public static boolean checkExistentLocalFile( final String name ) {
		//File file = new File(name);
		File file = getCaseInsensitiveFile(name);
		if( file.exists() ) {
			if( file.isFile() ) {
				return true;
			}
		}
		/*
		if( File.separatorChar == '/' ) {
			// may be case sensitive
			File parent = file.getParentFile();
			if( parent != null ) {
				if( parent.exists() ) {

				}
			}
		}
		*/
		file = null;
		return false;
	}
	/**
	 * case insensitive でファイルを探す
	 * TODO 既に見付かっているものはキャッシングして高速化した方がいい
	 * @param name フルパスファイル名
	 * @return 見付かったらその File 、なかったら null
	 */
	public static File getCaseInsensitiveFile( final String name ) {
		if( File.separatorChar == '/' ) {
			File fistcheck = new File(name); // まずはそのままの名前でチェック
			if( fistcheck.exists() ) {
				return fistcheck;
			}

			String[] path = name.split("/");
			int preLength = 0;
			final int count = path.length;
			StringBuilder fullPath = new StringBuilder(name.length());
			for( int i = 0; i < count; i++ ) {
				if( path[i] != null && path[i].length() > 0 ) {
					boolean isFile = i == (count-1);

					fullPath.append('/');
					fullPath.append(path[i]);
					File tmp = new File( fullPath.toString() );
					if( tmp.exists() != true ) { // 見付からないので、ファイルをリストアップしてcase insensitiveで比較
						boolean isfound = false;
						fullPath.delete(preLength, fullPath.length() );
						if( fullPath.length() == 0 ) {
							fullPath.append('/');
						}
						String fullpathstr = fullPath.toString();
						tmp = new File( fullpathstr );
						File[] files = tmp.listFiles();
						final int filecount = files.length;
						if( isFile != true ) {
							for( int j = 0; j < filecount; j++ ) {
								if( files[j].isDirectory() ) {
									String filename = files[j].getName();
									if( filename.equalsIgnoreCase(path[i]) ) {
										if( fullPath.length() != 1 ) {
											fullPath.append('/');
											fullPath.append( filename );
										} else {
											fullPath.append( filename );
										}
										isfound = true;
										break;
									}
								}
							}
						} else {
							for( int j = 0; j < filecount; j++ ) {
								if( files[j].isFile() ) {
									String filename = files[j].getName();
									if( filename.equalsIgnoreCase(path[i]) ) {
										if( fullPath.length() != 1 ) {
											fullPath.append('/');
											fullPath.append( filename );
										} else {
											fullPath.append( filename );
										}
										isfound = true;
										break;
									}
								}
							}
						}
						if( isfound == false ) {
							// 見付からなかった。途中まで比較した物はそのまま使い、残りはただ単に追加する
							for( ; i < count; i++ ) {
								fullPath.append('/');
								fullPath.append(path[i]);
							}
							return new File(fullPath.toString());
						}
					}

				}
				preLength = fullPath.length();
			}

			return new File(fullPath.toString());
		} else {
			return new File(name);
		}
	}

	public static String extractStorageExt( final String name ) {
		// extract an extension from name.
		// returned string will contain extension delimiter ( '.' ), except for
		// missing extension of the input string.
		// ( returns null string when input string does not have an extension )

		final int slen = name.length();
		int p = slen - 1;
		while( p >= 0 ){
			char c = name.charAt(p);
			if(c == '\\') break;
			if(c == '/') break;
			if(c == ArchiveDelimiter) break;
			if(c == '.') {
				// found extension delimiter
				//int extlen = slen - p;
				return name.substring( p );
			}
			p--;
		}
		// not found
		return new String();
	}
	public static String extractStorageName( final String name ) {
		// extract "name"'s storage name ( excluding path ) and return it.
		final int slen = name.length();
		int p = slen - 1;
		while( p >= 0 ){
			char c = name.charAt(p);
			if(c == '\\') break;
			if(c == '/') break;
			if(c == ArchiveDelimiter) break;
			p--;
		}
		p++;
		if( p <= 0 ) {
			return name;
		} else {
			return name.substring(p);
		}
	}
	public static String extractStoragePath( final String name ) {
		// extract "name"'s path ( including last delimiter ) and return it.
		final int slen = name.length();
		int p = slen;
		p--;
		while( p >= 0 ) {
			char c = name.charAt(p);
			if( c == '\\' ) break;
			if( c == '/' ) break;
			if( c == ArchiveDelimiter ) break;
			p--;
		}
		p++;
		return name.substring(0,p);
	}
	public static String chopStorageExt(String name) {
		// chop storage's extension and return it.
		int slen = name.length();
		int p = slen;
		p--;
		while( p >= 0 ) {
			char c = name.charAt(p);
			if( c == '\\' ) break;
			if( c == '/' ) break;
			if( c == ArchiveDelimiter) break;
			if( c == '.' ) {
				// found extension delimiter
				return name.substring(0,p);
			}
			p--;
		}
		// not found
		return name;
	}

	public static String searchPlacedPath( final String name ) throws TJSException {
		String place = getPlacedPath(name);
		if( place == null || place.length() == 0) Message.throwExceptionMessage( Message.CannotFindStorage, name);
		return place;
	}

	/**
	 * search path and return the path which the "name" is placed.
	 * returned name is normalized. returns empty string if the storage is not
	 * found.
	 * @throws TJSException
	 */
	public static String getPlacedPath( final String name ) throws TJSException {
		return TVP.AutoPath.getPlacedPath(name);
	}
	public static boolean isExistentStorage( final String name ) throws TJSException {
		String path = TVP.AutoPath.getPlacedPath(name);
		return( path != null && path.length() > 0 );
	}

	public static boolean isExistentStorageNoSearch(String name) throws TJSException {
		return isExistentStorageNoSearchNoNormalize( TVP.StorageMediaManager.normalizeStorageName(name,null) );
	}

	public static boolean isExistentStorageNoSearchNoNormalize( String name  ) throws TJSException {
		// does name contain > ?
		synchronized (TVP.CreateStreamCS) {
			final int sharp_pos = name.indexOf( ArchiveDelimiter );
			if( sharp_pos != -1 ) {
				// this storagename indicates a file in an archive
				String arcname = name.substring(0, sharp_pos);
				Archive arc = TVP.ArchiveCache.get(arcname);
				boolean ret;
				try {
					String in_arc_name = name.substring(sharp_pos + 1);
					in_arc_name = Archive.normalizeInArchiveStorageName(in_arc_name);
					ret = arc.isExistent(in_arc_name);
				} finally {
					arc = null;
				}
				return ret;
			}

			return TVP.StorageMediaManager.checkExistentStorage(name);
		}
	}
	public static BinaryStream createStream( final String _name, int flags ) throws TJSException {
		try {
			synchronized (TVP.CreateStreamCS) {
				String name;
				int access = flags & BinaryStream.ACCESS_MASK;
				if( access == BinaryStream.WRITE )
					name = TVP.StorageMediaManager.normalizeStorageName(_name,null);
				else
					name = TVP.AutoPath.getPlacedPath(_name); // file must exist

				if( name == null || name.length() == 0 ) Message.throwExceptionMessage(Message.CannotOpenStorage, _name);

				// does name contain > ?
				int sharp_pos = name.indexOf(ArchiveDelimiter);
				if( sharp_pos != -1 ) {
					// this storagename indicates a file in an archive
					if( access != BinaryStream.READ )
						Message.throwExceptionMessage(Message.CannotWriteToArchive);

					String arcname = name.substring( 0, sharp_pos );

					Archive arc;
					BinaryStream stream;
					arc = TVP.ArchiveCache.get(arcname);
					try {
						String in_arc_name = name.substring(sharp_pos + 1);
						in_arc_name = Archive.normalizeInArchiveStorageName(in_arc_name);
						stream = arc.createStream(in_arc_name);
					} finally {
						arc = null;
						if(access >= 1) clearStorageCaches();
					}
					return stream;
				}

				BinaryStream stream;
				try {
					stream = TVP.StorageMediaManager.open(name, flags);
				} finally {
					if(access >= 1) clearStorageCaches();
				}
				return stream;
			}
		} catch( TJSException e ) {
			if( _name.indexOf('#') != -1 ) {
				String tmp = Message.FilenameContainsSharpWarn.replace( "%1", _name );
				StringBuilder builder = new StringBuilder(128);
				builder.append(e.getMessage());
				builder.append('[');
				builder.append(tmp);
				builder.append(']');
				throw new TJSException(builder.toString());
			}
			throw e;
		}
	}
	public static void clearStorageCaches() {
		// clear all storage related caches
		// TVPClearXP3SegmentCache(); TODO XP3 書いたら追加すること, キャッシングしていないので今のところ不要
		TVP.AutoPath.clearAutoPathCache();
	}
	public static void setCurrentDirectory( final String _name ) throws TJSException {
		TVP.StorageMediaManager.setCurrentDirectory(_name);
		clearStorageCaches();
	}
	public static String readText( String name, String modestr ) throws TJSException {
		BinaryStream stream = null;
		stream = createStream(name,BinaryStream.READ);
		return readText( stream, name, modestr );
	}
	public static String readText( BinaryStream stream, String name, String modestr ) throws TJSException {
		try {
			String encodeType = null;
			int CryptMode = -1;
			int o_ofs = -1;
			if( modestr != null ) o_ofs = modestr.indexOf('o');
			int ofs = 0;
			if( o_ofs != -1 ) {
				// seek to offset
				StringBuilder builder = new StringBuilder(256);
				int i = o_ofs + 1;
				final int count = modestr.length();
				for( ; i < count; i++ ) {
					char ch = modestr.charAt(i);
					if( ch >= '0' && ch <= '9' ) {
						builder.append(ch);
					} else {
						break;
					}
				}
				LexBase lex = new LexBase( builder.toString() );
				Number num = lex.parseNumber();
				if( num != null ) {
					ofs = num.intValue();
					stream.setPosition(ofs);
				}
			}
			// check first of the file - whether the file is unicode
			byte[] mark = new byte[2];
			stream.read( mark );
			if( mark[0] == (byte)0xff && mark[1] == (byte)0xfe ) {
				// unicode
				encodeType = "UTF-16LE";
			} else if( mark[0] == (byte)0xfe && mark[1] == (byte)0xfe ) {
				// ciphered text or compressed
				byte[] mode = new byte[1];
				stream.read( mode );
				byte m0 = mode[0];
				if( m0 != 0 && m0 != 1 && m0 != 2 ) {
					Message.throwExceptionMessage( Message.UnsupportedCipherMode, name );
				}
				CryptMode = m0;
				stream.read( mark ); // original bom code comes here (is not compressed)
				if(mark[0] != (byte)0xff || mark[1] != (byte)0xfe)
					Message.throwExceptionMessage( Message.UnsupportedCipherMode, name );

				if( CryptMode == 2 ) {
					// compressed text stream
					byte[] compress = new byte[16];
					stream.read( compress );
					ByteBuffer bytebuf = ByteBuffer.wrap(compress);
					bytebuf.order(ByteOrder.LITTLE_ENDIAN);
					long compressed = bytebuf.getLong();
					long uncopressed = bytebuf.getLong();
					if( compressed > Integer.MAX_VALUE || uncopressed > Integer.MAX_VALUE ) {
						Message.throwExceptionMessage( Message.UnsupportedCipherMode, name );
					}
					byte[] nbuf = new byte[(int) (compressed)];
					stream.read(nbuf);
					byte[] output = new byte[(int) (uncopressed)];
					if( Decompresser == null ) Decompresser = new Inflater();
					// 非圧縮データ
					try {
						Decompresser.setInput(nbuf);
						int destlen = Decompresser.inflate(output);
						Decompresser.reset();
						if( destlen != uncopressed )
							Message.throwExceptionMessage(Message.UnsupportedCipherMode);
						ByteBuffer strbuf = ByteBuffer.wrap(output);
						strbuf.order(ByteOrder.LITTLE_ENDIAN);
						CharBuffer cbuff = strbuf.asCharBuffer();
						String result = cbuff.subSequence(0, (int) (uncopressed/2)).toString();
						return result;
					} catch (DataFormatException e) {
						Decompresser.reset();
						Message.throwExceptionMessage(Message.UnsupportedCipherMode);
					}
				} else if( CryptMode == 1 ) {
					long size = stream.getSize() - stream.getPosition();
					byte[] output = new byte[(int) size];
					stream.read(output);
					ByteBuffer strbuf = ByteBuffer.wrap(output);
					strbuf.order(ByteOrder.LITTLE_ENDIAN);
					CharBuffer cbuff = strbuf.asCharBuffer();
					int len = (int) (size / 2);
					for( int i = 0; i < len; i++ ) {
						char ch = cbuff.get(i);
						ch = (char) (((ch & 0xaaaaaaaa)>>>1) | ((ch & 0x55555555)<<1));
						cbuff.put( i, ch );
					}
					String result = cbuff.subSequence(0, len).toString();
					return result;
				} else { // CryptMode == 0
					long size = stream.getSize() - stream.getPosition();
					byte[] output = new byte[(int) size];
					stream.read(output);
					ByteBuffer strbuf = ByteBuffer.wrap(output);
					strbuf.order(ByteOrder.LITTLE_ENDIAN);
					CharBuffer cbuff = strbuf.asCharBuffer();
					int len = (int) (size / 2);
					for( int i = 0; i < len; i++ ) {
						char ch = cbuff.get(i);
						if( ch >= 0x20 ) {
							ch = (char) ( (ch ^ (((ch&0xfe) << 8)^1)) & 0xffff );
							cbuff.put( i, ch );
						}
					}
					String result = cbuff.subSequence(0, len).toString();
					return result;
				}
			} else {
				// 開き直してシークする
				stream.setPosition(ofs);
				encodeType = "MS932";
			}
			if( encodeType != null ) {
				long size = stream.getSize();
				byte[] buf = new byte[(int)size];
				int readSize = stream.read(buf);
				if( readSize > 0 ) {
					if( "UTF-16LE".equals(encodeType) ) {
						ByteBuffer strbuf = ByteBuffer.wrap(buf);
						strbuf.order(ByteOrder.LITTLE_ENDIAN);
						CharBuffer cbuff = strbuf.asCharBuffer();
						int len = (int) (readSize / 2);
						String result = cbuff.subSequence(0, len).toString();
						return result;
					} else {
						String result = new String( buf, 0, readSize, encodeType );
						buf = null;
						return result;
					}
				}
			}
		} catch (IOException e) {
			throw new TJSException( Error.ReadError + e.toString() );
		} finally {
			if( stream != null ) {
				stream.close();
			}
		}
		return null;
	}
	static boolean checkExistentLocalFolder( final String name ) {
		File file = new File(name);
		if( file.exists() && file.isDirectory() )
			return true;
		else
			return false;
	}

	public static boolean createFolders(String name) {
		File newdir = new File(name);
		return newdir.mkdir();
	}

}
