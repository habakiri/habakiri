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

import java.io.FileDescriptor;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import jp.kirikiri.tjs2.BinaryStream;
import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tvp2.msg.Message;

public class XP3ArchiveStream extends BinaryStream {

	public static void initialize() {
		SegmentData.initialize();
	}
	public static void finalizeApplication() {
		SegmentData.finalizeApplication();
	}
	static class SegmentData {
		private static Inflater Decompresser;
		private int Size;
		private byte[] Data;

		public static void initialize() {
			Decompresser = null;
		}
		public static void finalizeApplication() {
			Decompresser = null;
		}
		public SegmentData() {
			if( Decompresser == null ) Decompresser = new Inflater();
		}
		public void setData(int outsize, BinaryStream instream, int insize ) throws TJSException {
			// 非圧縮データ
			byte[] indata = new byte[insize];
			try {
				instream.read(indata);
				Data = new byte[outsize];
				Decompresser.setInput(indata);
				int destlen = Decompresser.inflate(Data);
				//Decompresser.end();
				Decompresser.reset();
				if( destlen != outsize )
					Message.throwExceptionMessage(Message.UncompressionFailed);
				Size = outsize;
			} catch (DataFormatException e) {
				Message.throwExceptionMessage(Message.UncompressionFailed);
			} finally {
				indata = null;
			}
		}
		public byte[] getData() { return Data; }
		public int getSize() { return Size; }
	}

	private XP3Archive mOwner;
	private int mStorageIndex; // index in archive

	private ArrayList<XP3Archive.XP3ArchiveSegment> mSegments;
	private BinaryStream mStream;
	private long mOrgSize; // original storage size
	private String mOrgName;

	private int mCurSegmentNum;
	private XP3Archive.XP3ArchiveSegment mCurSegment;
		// currently opened segment ( NULL for not opened )

	private int mLastOpenedSegmentNum;

	private long mCurPos; // current position in absolute file position

	private long mSegmentRemain; // remain bytes in current segment
	private long mSegmentPos; // offset from current segment's start

	private SegmentData mSegmentData; // uncompressed segment data

	private boolean mSegmentOpened;

	public XP3ArchiveStream(XP3Archive owener, int storageindex,
			ArrayList<XP3Archive.XP3ArchiveSegment> segments, BinaryStream stream,
			long orgsize, String orgname ) {
		mStorageIndex = storageindex;
		mSegments = segments;
		//mSegmentData = null;
		// mCurSegmentNum = 0;
		mCurSegment = segments.get(0);
		//mSegmentPos = 0;
		mSegmentRemain = mCurSegment.OrgSize;
		//mSegmentOpened = false;
		mLastOpenedSegmentNum = -1;
		mOwner = owener;
		mStream = stream;
		mOrgSize = orgsize;
		mOrgName = orgname;
	}
	private void ensureSegment() throws TJSException {
		// ensure accessing to current segment
		if(mSegmentOpened) return;

		if(mLastOpenedSegmentNum == mCurSegmentNum) {
			if(!mCurSegment.IsCompressed)
				mStream.setPosition(mCurSegment.Start + mSegmentPos);
			return;
		}

		// erase buffer
		if( mSegmentData != null ) mSegmentData = null;

		// is compressed segment ?
		if( mCurSegment.IsCompressed ) {
			// a compressed segment
			//if( mCurSegment.OrgSize >= SEGCACHE_ONE_LIMIT ) {
				// too large to cache
				mStream.setPosition( mCurSegment.Start);
				mSegmentData = new SegmentData();
				mSegmentData.setData( (int)mCurSegment.OrgSize, mStream, (int)mCurSegment.ArcSize );
				// キャッシュ機能は未実装
			/*} else {
				// search thru segment cache
				SegmentCacheSearchData sdata;
				sdata.Name = mOwner.getName();
				sdata.StorageIndex = mStorageIndex;
				sdata.SegmentIndex = mCurSegmentNum;

				int hash;
				hash = tTVPSegmentCacheSearchHashFunc::Make(sdata);

				mSegmentData = TVPSearchFromSegmentCache(sdata, hash);
				if( mSegmentData == null ) {
					// not found in cache
					mStream.setPosition(CurSegment->Start);
					mSegmentData = new SegmentData();
					mSegmentData.setData( (int)mCurSegment.OrgSize, mStream, (int)mCurSegment.ArcSize );

					// add to cache
					TVPPushToSegmentCache(sdata, hash, SegmentData);
				}
			}*/
		} else {
			// not a compressed segment
			mStream.setPosition( mCurSegment.Start + mSegmentPos );
		}
		mSegmentOpened = true;
		mLastOpenedSegmentNum = mCurSegmentNum;
	}

	private void seekToPosition(long pos) {
		// open segment at 'pos' and seek
		// pos must between zero thru OrgSize
		if(mCurPos == pos) return;

		// do binary search to determine current segment number
		int st = 0;
		int et = mSegments.size();
		int seg_num;

		while( true ) {
			if(et-st <= 1) { seg_num = st; break; }
			int m = st + (et-st)/2;
			if( mSegments.get(m).Offset > pos)
				et = m;
			else
				st = m;
		}

		mCurSegmentNum = seg_num;
		mCurSegment =mSegments.get(mCurSegmentNum);
		mSegmentOpened = false;

		mSegmentPos = pos - mCurSegment.Offset;
		mSegmentRemain = mCurSegment.OrgSize - mSegmentPos;
		mCurPos = pos;
	}
	private boolean openNextSegment() throws TJSException {
		// open next segment
		if(mCurSegmentNum == (mSegments.size() -1) )
			return false; // no more segments
		mCurSegmentNum ++;
		mCurSegment = mSegments.get(mCurSegmentNum);
		mSegmentOpened = false;
		mSegmentPos = 0;
		mSegmentRemain = mCurSegment.OrgSize;
		mCurPos = mCurSegment.Offset;
		ensureSegment();
		return true;
	}
	@Override
	public long seek(long offset, int whence) throws TJSException {
		long newpos;
		switch(whence) {
		case SEEK_SET:
			newpos = offset;
			if(offset >= 0 && offset <= mOrgSize) {
				seekToPosition(newpos);
			}
			return mCurPos;

		case SEEK_CUR:
			newpos = offset + mCurPos;
			if(offset >= 0 && offset <= mOrgSize) {
				seekToPosition(newpos);
			}
			return mCurPos;

		case SEEK_END:
			newpos = offset + mOrgSize;
			if(offset >= 0 && offset <= mOrgSize) {
				seekToPosition(newpos);
			}
			return mCurPos;
		}
		return mCurPos;
	}


	@Override
	public int read(ByteBuffer buffer) throws TJSException {
		ensureSegment();
		int read_size = buffer.limit() - buffer.position();
		int write_size = 0;
		byte[] buf = new byte[read_size];
		while( read_size > 0 ) {
			while( mSegmentRemain == 0 ) {
				// must go next segment
				if(!openNextSegment()) {// open next segment
					if( write_size == 0 ) return -1;
					else return write_size; // could not read more
				}
			}

			int one_size = (int) (read_size > mSegmentRemain ? mSegmentRemain : read_size);

			if( mCurSegment.IsCompressed ) {
				// compressed segment; read from uncompressed data in memory
				System.arraycopy( mSegmentData.getData(), (int) mSegmentPos, buf, write_size, one_size );
			} else {
				// read directly from stream
				mStream.read( buf, write_size, one_size );
			}

			// execute filter (for encryption method)
			if( XP3Archive.ArchiveExtractionFilter != null ) {
				XP3Archive.ArchiveExtractionFilter.encrypt( mCurPos, buf, write_size, one_size, mOwner.getFileHash(mStorageIndex) );
			}

			// adjust members
			mSegmentPos += one_size;
			mCurPos += one_size;
			mSegmentRemain -= one_size;
			read_size -= one_size;
			write_size += one_size;
		}
		buffer.put(buf);
		return write_size;
	}


	@Override
	public int read(byte[] buffer) throws TJSException {
		ensureSegment();
		int read_size = buffer.length;
		int write_size = 0;
		while( read_size > 0 ) {
			while( mSegmentRemain == 0 ) {
				// must go next segment
				if(!openNextSegment()) { // open next segment
					if( write_size == 0 ) return -1;
					else return write_size; // could not read more
				}
			}

			int one_size = (int) (read_size > mSegmentRemain ? mSegmentRemain : read_size);

			if( mCurSegment.IsCompressed ) {
				// compressed segment; read from uncompressed data in memory
				System.arraycopy( mSegmentData.getData(), (int) mSegmentPos, buffer, write_size, one_size );
			} else {
				// read directly from stream
				mStream.read( buffer, write_size, one_size );
			}

			// execute filter (for encryption method)
			if( XP3Archive.ArchiveExtractionFilter != null ) {
				XP3Archive.ArchiveExtractionFilter.encrypt( mCurPos, buffer, write_size, one_size, mOwner.getFileHash(mStorageIndex) );
			}

			// adjust members
			mSegmentPos += one_size;
			mCurPos += one_size;
			mSegmentRemain -= one_size;
			read_size -= one_size;
			write_size += one_size;
		}
		return write_size;
	}

	@Override
	public int read(byte[] b, int off, int len) throws TJSException {
		ensureSegment();
		int read_size = len;
		int write_size = 0;
		while( read_size > 0 ) {
			while( mSegmentRemain == 0 ) {
				// must go next segment
				if(!openNextSegment()) { // open next segment
					if( write_size == 0 ) return -1; // end of file
					else return write_size; // could not read more
				}
			}

			int one_size = (int) (read_size > mSegmentRemain ? mSegmentRemain : read_size);

			if( mCurSegment.IsCompressed ) {
				// compressed segment; read from uncompressed data in memory
				System.arraycopy( mSegmentData.getData(), (int) mSegmentPos, b, off+write_size, one_size );
			} else {
				// read directly from stream
				mStream.read( b, off+write_size, one_size );
			}

			// execute filter (for encryption method)
			if( XP3Archive.ArchiveExtractionFilter != null ) {
				XP3Archive.ArchiveExtractionFilter.encrypt( mCurPos, b, write_size, one_size, mOwner.getFileHash(mStorageIndex) );
			}

			// adjust members
			mSegmentPos += one_size;
			mCurPos += one_size;
			mSegmentRemain -= one_size;
			read_size -= one_size;
			write_size += one_size;
		}
		return write_size;
	}

	@Override
	public int write(ByteBuffer buffer) { return 0; }

	@Override
	public int write(byte[] buffer) { return 0; }

	@Override
	public void write(byte[] b, int off, int len) { }

	@Override
	public void write(int b) { }

	@Override
	public void close() {
		mStream.close(); // 毎回作る前提、キャッシュするのなら閉じないようにする TODO
	}

	@Override
	public InputStream getInputStream() {
		return new BinaryInputStream(this);
	}

	@Override
	public OutputStream getOutputStream() {
		return null;
	}

	@Override
	public long getSize() throws TJSException {
		return mOrgSize;
	}

	/**
	 * アーカイブ内のファイルかどうか判定する
	 * @return
	 */
	public boolean isArchive() {
		return true;
	}
	/**
	 */
	@Override
	public String getFilePath() {
		StringBuilder builder = new StringBuilder(256);
		builder.append(mStream.getFilePath());
		builder.append('>');
		builder.append(mOrgName);
		return builder.toString();
	}

	public FileDescriptor getFileDescriptor() {
		return mStream.getFileDescriptor();
	}
	public long getFileOffset() {
		return mStream.getFileOffset() + mSegments.get(0).Start;
	}
}
