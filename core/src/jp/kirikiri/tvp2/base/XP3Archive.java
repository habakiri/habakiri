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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import jp.kirikiri.tjs2.BinaryStream;
import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tvp2.msg.Message;
import jp.kirikiri.tvp2.utils.DebugClass;

public class XP3Archive extends Archive {
	static public boolean AllowExtractProtectedStorage;
	/** 復号化用インターフェイス */
	static public XP3ArchiveExtractionFilter ArchiveExtractionFilter;

	static public void initialize() {
		ArchiveExtractionFilter = null;
		AllowExtractProtectedStorage = true;
	}
	public static void finalizeApplication() {
		ArchiveExtractionFilter = null;
	}

	static public class XP3ArchiveSegment {
		/** アーカイブ内での開始位置 */
		public long Start;
		/** アーカイブ内でのオフセット(非圧縮) */
		public long Offset;
		/** オリジナル(非圧縮)セグメントサイズ */
		public long OrgSize;
		/** アーカイブ内(圧縮)セグメントサイズ */
		public long ArcSize;
		/** 圧縮有無 */
		public boolean IsCompressed;
	}

	static class ArchiveItem {
		/** ファイル名 */
		public String Name;
		/** ファイルのハッシュ値 */
		public int FileHash;
		/** オリジナルファイルサイズ */
		public long OrgSize;
		/** アーカイブ内サイズ */
		public long ArcSize;
		/** セグメント */
		public ArrayList<XP3ArchiveSegment> Segments;

		public ArchiveItem() {
			Segments = new ArrayList<XP3ArchiveSegment>();
		}
	}
	/** ファイル名 */
	private String mName;
	/** ファイル数 */
	private int mCount;
	/** ファイル情報リスト */
	private ArrayList<ArchiveItem> mItems;

	private static final byte XP3_INDEX_ENCODE_METHOD_MASK = 0x07;
	private static final byte XP3_INDEX_ENCODE_ZLIB = 1;
	private static final byte XP3_INDEX_ENCODE_RAW = 0;

	private static final int XP3_INDEX_CONTINUE = 0x80;
	private static final int XP3_FILE_PROTECTED =(1<<31);

	private static final int XP3_SEGM_ENCODE_METHOD_MASK = 0x07;
	private static final int XP3_SEGM_ENCODE_RAW = 0;
	private static final int XP3_SEGM_ENCODE_ZLIB = 1;

	//private static final byte[] CN_FILE = { 0x46/*'F'*/, 0x69/*'i'*/, 0x6c/*'l'*/, 0x65/*'e'*/ };
	//private static final byte[] CN_INFO = { 0x69/*'i'*/, 0x6e/*'n'*/, 0x66/*'f'*/, 0x6f/*'o'*/ };
	//private static final byte[] CN_SEGM = { 0x73/*'s'*/, 0x65/*'e'*/, 0x67/*'g'*/, 0x6d/*'m'*/ };
	//private static final byte[] CH_ADLR = { 0x61/*'a'*/, 0x64/*'d'*/, 0x6c/*'l'*/, 0x72/*'r'*/ };

	private static final int FILE_TAG = ('F') | ('i' << 8) | ('l' << 16) | ('e' << 24);
	private static final int INFO_TAG = ('i') | ('n' << 8) | ('f' << 16) | ('o' << 24);
	private static final int SEGM_TAG = ('s') | ('e' << 8) | ('g' << 16) | ('m' << 24);
	private static final int ADLR_TAG = ('a') | ('d' << 8) | ('l' << 16) | ('r' << 24);

	public XP3Archive(String name) throws TJSException {
		super(name);
		mName = name;
		mItems = new ArrayList<ArchiveItem>();
		// mCount = 0;
		long offset;
		BinaryStream st = Storage.createStream( name, BinaryStream.READ );
		byte[] indexdata = null;

		DebugClass.addLog( "(info) Trying to read XP3 virtual file system information from : " + name);
		int segmentcount = 0;
		try {
			// アーカイブ開始位置を取得する
			offset = getXP3ArchiveOffset( st, name, true );

			// ヘッダー分を飛ばす
			st.setPosition(11 + offset);

			// ZLIB のデコンプレッサー準備
			Inflater decompresser = new Inflater();

			// XP3 のすべてのインデックスを読み込む
			//byte[] long2Read = new byte[16];
			byte[] longRead = new byte[8];
			byte[] byteRead = new byte[1];
			int[] start = new int[1];
			int[] size = new int[1];
			ByteBuffer longBuff = ByteBuffer.wrap(longRead);
			longBuff.order(ByteOrder.LITTLE_ENDIAN);
			while(true) {
				st.read( longRead );
				longBuff.position(0);
				long index_ofs = longBuff.getLong();
				//long index_ofs = ((long)longRead[7] << 56) | ((long)longRead[6] << 48) | ((long)longRead[5] << 40) | ((long)longRead[4] << 32) |
				//			((long)longRead[3] << 24) | ((long)longRead[2] << 16) | ((long)longRead[1] << 8) | ((long)longRead[0]);
				st.setPosition(index_ofs + offset);


				// index をメモリに読み込む
				byte index_flag;
				st.read(byteRead);
				index_flag = byteRead[0];
				int index_size = 0;
				if( (index_flag&XP3_INDEX_ENCODE_METHOD_MASK) == XP3_INDEX_ENCODE_ZLIB ) {
					st.read( longRead );
					longBuff.position(0);
					long compressed_size = longBuff.getLong();
					//long compressed_size = ((long)long2Read[7] << 56) | ((long)long2Read[6] << 48) | ((long)long2Read[5] << 40) | ((long)long2Read[4] << 32) |
					//	((long)long2Read[3] << 24) | ((long)long2Read[2] << 16) | ((long)long2Read[1] << 8) | ((long)long2Read[0]);
					st.read( longRead );
					longBuff.position(0);
					long r_index_size = longBuff.getLong();
					//long r_index_size = ((long)long2Read[15] << 56) | ((long)long2Read[14] << 48) | ((long)long2Read[13] << 40) | ((long)long2Read[12] << 32) |
					//	((long)long2Read[11] << 24) | ((long)long2Read[10] << 16) | ((long)long2Read[9] << 8) | ((long)long2Read[8]);

					if( compressed_size > Integer.MAX_VALUE || r_index_size > Integer.MAX_VALUE ) {
						Message.throwExceptionMessage(Message.ReadError); // 大きすぎる
					}
					index_size = (int) r_index_size;
					indexdata = new byte[index_size];
					byte[] compressed = new byte[(int)compressed_size];
					try {
						st.read(compressed);
						// Decompress the bytes
						decompresser.setInput(compressed);
						int destlen = decompresser.inflate(indexdata);
						//decompresser.end();
						decompresser.reset();
						if( destlen != index_size )
							Message.throwExceptionMessage(Message.UncompressionFailed);
					} catch (DataFormatException e) {
						Message.throwExceptionMessage(Message.UncompressionFailed);
					} finally {
						compressed = null;
					}
				} else if( (index_flag&XP3_INDEX_ENCODE_METHOD_MASK) == XP3_INDEX_ENCODE_RAW ) {
					st.read( longRead );
					longBuff.position(0);
					long r_index_size = longBuff.getLong();
					//long r_index_size = ((long)longRead[7] << 56) | ((long)longRead[6] << 48) | ((long)longRead[5] << 40) | ((long)longRead[4] << 32) |
					//	((long)longRead[3] << 24) | ((long)longRead[2] << 16) | ((long)longRead[1] << 8) | ((long)longRead[0]);
					if( r_index_size > Integer.MAX_VALUE ) {
						Message.throwExceptionMessage(Message.ReadError); // 大きすぎる
					}
					index_size = (int) r_index_size;
					indexdata = new byte[index_size];
					st.read(indexdata);
				} else {
					// 不明な形式
					Message.throwExceptionMessage(Message.ReadError);
				}
				// index情報をメモリから読み込む
				ByteBuffer indexdatabuf = ByteBuffer.wrap(indexdata);
				indexdatabuf.order(ByteOrder.LITTLE_ENDIAN);
				int ch_file_start = 0;
				int ch_file_size = index_size;
				while(true){
					start[0] = ch_file_start;
					size[0] = ch_file_size;
					// 'File' チャンクを探す
					if(!findChunk(indexdatabuf, FILE_TAG, start, size))
						break; // not found
					ch_file_start = start[0];
					ch_file_size = size[0];

					// 'info' サブチャンクを探す
					if(!findChunk(indexdatabuf, INFO_TAG, start, size))
						Message.throwExceptionMessage(Message.ReadError);
					int ch_info_start = start[0];
					//int ch_info_size = size[0];

					indexdatabuf.position(ch_info_start);
					// info サブチャンク読み込み
					ArchiveItem item = new ArchiveItem();
					//int flags = ((int)indexdata[ch_info_start+3] << 24) | ((int)indexdata[ch_info_start+2] << 16) |
					//	((int)indexdata[ch_info_start+1] << 8) | ((int)indexdata[ch_info_start+0] << 8);
					int flags = indexdatabuf.getInt();

					if( !AllowExtractProtectedStorage && (flags & XP3_FILE_PROTECTED) != 0 )
						Message.throwExceptionMessage( Message.XP3Protected );

					/*
					item.OrgSize = ((long)indexdata[ch_info_start+4+7] << 56) | ((long)indexdata[ch_info_start+4+6] << 48) |
							((long)indexdata[ch_info_start+4+5] << 40) | ((long)indexdata[ch_info_start+4+4] << 32) |
							((long)indexdata[ch_info_start+4+3] << 24) | ((long)indexdata[ch_info_start+4+2] << 16) |
							((long)indexdata[ch_info_start+4+1] << 8) | ((long)indexdata[ch_info_start+4]);
					item.ArcSize = ((long)indexdata[ch_info_start+12+7] << 56) | ((long)indexdata[ch_info_start+12+6] << 48) |
							((long)indexdata[ch_info_start+12+5] << 40) | ((long)indexdata[ch_info_start+12+4] << 32) |
							((long)indexdata[ch_info_start+12+3] << 24) | ((long)indexdata[ch_info_start+12+2] << 16) |
							((long)indexdata[ch_info_start+12+1] << 8) | ((long)indexdata[ch_info_start+12]);
					 */
					item.OrgSize = indexdatabuf.getLong();
					item.ArcSize = indexdatabuf.getLong();
					//int len = ((int)indexdata[ch_info_start+20+1] << 8) | ((int)indexdata[ch_info_start+20]);
					int len = indexdatabuf.getShort();
					CharBuffer cbuff = indexdatabuf.asCharBuffer();
					if( len == -1 ) {
						len = cbuff.capacity();
					}
					for( int i = 0; i < len; i++ ) { // 長さチェック
						if( cbuff.get(i) == 0 ) {
							len = i;
							break;
						}
					}
					item.Name = cbuff.subSequence(0, len).toString();
					item.Name = normalizeInArchiveStorageName(item.Name);

					// 'segm' チャンクを探す
					// Each of in-archive storages can be splitted into some segments.
					// Each segment can be compressed or uncompressed independently.
					// segments can share partial area of archive storage. ( this is used for
					// OggVorbis' VQ code book sharing )
					start[0] = ch_file_start;
					size[0] = ch_file_size;
					if(!findChunk(indexdatabuf, SEGM_TAG, start, size))
						Message.throwExceptionMessage(Message.ReadError);
					int ch_segm_start = start[0];
					int ch_segm_size = size[0];

					// read segm sub-chunk
					int segment_count = ch_segm_size / 28;
					long offset_in_archive = 0;
					indexdatabuf.order(ByteOrder.LITTLE_ENDIAN);
					for( int i = 0; i < segment_count; i++ ) {
						int pos_base = i * 28 + ch_segm_start;
						indexdatabuf.position(pos_base);
						XP3ArchiveSegment seg = new XP3ArchiveSegment();
						int segflags = indexdatabuf.getInt();

						if( (segflags & XP3_SEGM_ENCODE_METHOD_MASK) == XP3_SEGM_ENCODE_RAW )
							seg.IsCompressed = false;
						else if( (segflags & XP3_SEGM_ENCODE_METHOD_MASK) == XP3_SEGM_ENCODE_ZLIB )
							seg.IsCompressed = true;
						else
							Message.throwExceptionMessage(Message.ReadError); // unknown encode method

						seg.Start = indexdatabuf.getLong() + offset; // ReadI64FromMem(indexdata + pos_base + 4) + offset;
							// data offset in archive
						seg.Offset = offset_in_archive; // offset in in-archive storage
						seg.OrgSize = indexdatabuf.getLong(); // ReadI64FromMem(indexdata + pos_base + 12); // original size
						seg.ArcSize = indexdatabuf.getLong(); // ReadI64FromMem(indexdata + pos_base + 20); // archived size
						item.Segments.add(seg);
						offset_in_archive += seg.OrgSize;
						segmentcount ++;
					}

					// 'adlr' サブチャンクを探す
					start[0] = ch_file_start;
					size[0] = ch_file_size;
					if(!findChunk(indexdatabuf, ADLR_TAG, start, size))
						Message.throwExceptionMessage(Message.ReadError);
					int ch_adlr_start = start[0];
					//int ch_adlr_size = size[0];

					// read 'aldr' sub-chunk
					indexdatabuf.position(ch_adlr_start);
					item.FileHash = indexdatabuf.getInt(); // ReadI32FromMem(indexdata + ch_adlr_start);

					// push information
					mItems.add(item);

					// to next file
					ch_file_start += ch_file_size;
					ch_file_size = index_size - ch_file_start;
					mCount++;
				}

				if( (index_flag & XP3_INDEX_CONTINUE) == 0 )
					break; // continue reading index when the bit sets

				// sort item vector by its name (required for Archive specification)
				Collections.sort( mItems, new Comparator<ArchiveItem>() {
					@Override
					public int compare(ArchiveItem o1, ArchiveItem o2) {
						return o1.Name.compareTo(o2.Name);
					}
				});
			}
		} catch( TJSException e ) {
			DebugClass.addLog("(info) Faild.");
			throw e;
		} finally {
			indexdata = null;
			st.close();
			st = null;
		}
		DebugClass.addLog( "(info) Done. (contains " + mCount + " file(s), " + segmentcount + " segment(s))" );
	}

	@Override
	public int getCount() { return mCount; }

	@Override
	public String getName(int idx) { return mItems.get(idx).Name; }

	public String getName() {
		return mName;
	}
	@Override
	public BinaryStream createStreamByIndex(int idx) throws TJSException {
		if( idx < 0 || idx >= mItems.size() )
			Message.throwExceptionMessage(Message.ReadError);

		ArchiveItem item = mItems.get(idx);
		BinaryStream stream = Storage.createStream( mName, BinaryStream.READ ); // TODO オリジナルではこのストリームはキャッシュしている
		BinaryStream out = null;
		out = new XP3ArchiveStream( this, idx, item.Segments, stream, item.OrgSize, item.Name );
		return out;
	}

	static final private byte[] XP3Mark = {
		0x58/*'X'*/, 0x50/*'P'*/, 0x33/*'3'*/, 0x0d/*'\r'*/, 0x0a/*'\n'*/, 0x20/*' '*/, 0x0a/*'\n'*/, 0x1a/*EOF*/,
		(byte) 0x8b, 0x67, 0x01
	};
	/**
	 *
	 * @param st ストリーム
	 * @param name ファイル名
	 * @param raise XP3ファイルでない時例外を投げるかどうか
	 * @return 開始位置オフセット、実行ファイルのことはないので、常に0のはず
	 * @throws TJSException
	 */
	static private long getXP3ArchiveOffset( BinaryStream st, final String name, boolean raise ) throws TJSException {
		long offset;
		st.setPosition(0);
		byte[] mark = new byte[11+1];

		//static tjs_uint8 XP3Mark1[] =
		//{ 0x58/*'X'*/, 0x50/*'P'*/, 0x33/*'3'*/, 0x0d/*'\r'*/, 0x0a/*'\n'*/, 0x20/*' '*/, 0x0a/*'\n'*/, 0x1a/*EOF*/, 0xff /* sentinel */ };
		//static tjs_uint8 XP3Mark2[] =
		//{ 0x8b, 0x67, 0x01, 0xff/* sentinel */ };

		// XP3 header mark contains:
		// 1. line feed and carriage return to detect corruption by unnecessary
		//    line-feeds convertion
		// 2. 1A EOF mark which indicates file's text readable header ending.
		// 3. 8B67 KANJI-CODE to detect curruption by unnecessary code convertion
		// 4. 01 file structure version and character coding
		//    higher 4 bits are file structure version, currently 0.
		//    lower 4 bits are character coding, currently 1, is BMP 16bit Unicode.

		//static tjs_uint8 XP3Mark[11+1];
		// +1: I was warned by CodeGuard that the code will do
		// access overrun... because a number of 11 is not aligned by DWORD,
		// and the processor may read the value of DWORD at last of this array
		// from offset 8. Then the last 1 byte would cause a fail.
		//static boolean DoInit = true;
		/*
		if(DoInit)
		{
			// the XP3 header above is splitted into two part; to avoid
			// mis-finding of the header in the program's initialized data area.
			DoInit = false;
			memcpy(XP3Mark, XP3Mark1, 8);
			memcpy(XP3Mark + 8, XP3Mark2, 3);
			// here joins it.
		}
		*/

		mark[0] = 0; // sentinel
		st.read(mark, 0, 11 );
		// たぶん、実行ファイルに結合することはないので、これは不要だと思われる
		if( mark[0] == 0x4d/*'M'*/ && mark[1] == 0x5a/*'Z'*/ ) {
			// "MZ" is a mark of Win32/DOS executables,
			// TVP searches the first mark of XP3 archive in the executeble file.
			boolean found = false;

			offset = 16;
			st.setPosition(16);

			// XP3 mark must be aligned by a paragraph ( 16 bytes )
			final int one_read_size = 256*1024;
			int read;
			byte[] buffer = new byte[one_read_size]; // read 256kbytes at once

			while( 0 != (read = st.read(buffer, 0, one_read_size)) ) {
				int p = 0;
				while( p < read ) {
					if( XP3Mark[0] == buffer[p+0] &&
						XP3Mark[1] == buffer[p+1] &&
						XP3Mark[2] == buffer[p+2] &&
						XP3Mark[3] == buffer[p+3] &&
						XP3Mark[4] == buffer[p+4] &&
						XP3Mark[5] == buffer[p+5] &&
						XP3Mark[6] == buffer[p+6] &&
						XP3Mark[7] == buffer[p+7] &&
						XP3Mark[8] == buffer[p+8] &&
						XP3Mark[9] == buffer[p+9] &&
						XP3Mark[10] == buffer[p+10] ) {
						// found the mark
						offset += p;
						found = true;
						break;
					}
					p+=16;
				}
				if(found) break;
				offset += one_read_size;
			}

			if( !found ) {
				if( raise )
					Message.throwExceptionMessage(Message.CannotUnbindXP3EXE, name);
				else
					return -1;
			}
		} else if( XP3Mark[0] == mark[0] &&
				XP3Mark[1] == mark[1] &&
				XP3Mark[2] == mark[2] &&
				XP3Mark[3] == mark[3] &&
				XP3Mark[4] == mark[4] &&
				XP3Mark[5] == mark[5] &&
				XP3Mark[6] == mark[6] &&
				XP3Mark[7] == mark[7] &&
				XP3Mark[8] == mark[8] &&
				XP3Mark[9] == mark[9] &&
				XP3Mark[10] == mark[10] ) {
			// XP3 mark found
			offset = 0;
		} else {
			if( raise )
				Message.throwExceptionMessage(Message.CannotFindXP3Mark, name);
			return -1;
		}

		return offset;
	}
	private static boolean findChunk( final ByteBuffer data, final int name, int[] start, int[] size ) throws TJSException {
		int start_save = start[0];
		int size_save = size[0];

		int pos = 0;
		int tmp_start = start[0];
		int tmp_size = size[0];
		while( pos < tmp_size ) {
			data.position(tmp_start);
			int fourcc = data.getInt();
			boolean found = fourcc == name;
			tmp_start += 4;
			data.position(tmp_start);
			long r_size = data.getLong();

			tmp_start += 8;
			if( r_size > Integer.MAX_VALUE )
				Message.throwExceptionMessage(Message.ReadError);

			int size_chunk = (int)r_size;
			if( found ) {
				// found
				start[0] = tmp_start;
				size[0] = size_chunk;
				return true;
			}
			tmp_start += size_chunk;
			pos += size_chunk + 4 + 8;
		}

		start[0] = start_save;
		size[0] = size_save;
		return false;
	}

	public int getFileHash(int idx) {
		return mItems.get(idx).FileHash;
	}
}
