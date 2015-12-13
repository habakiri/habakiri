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
//
package jp.kirikiri.tvp2.base;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.Deflater;

import jp.kirikiri.tjs2.BinaryStream;
import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tjs2.TextWriteStreamInterface;
import jp.kirikiri.tvp2.TVP;
import jp.kirikiri.tvp2.msg.Message;
import jp.kirikiri.tvp2.utils.DebugClass;

public class TextWriteStream implements TextWriteStreamInterface {
    //private static final int COMPRESSION_BUFFER_SIZE = 1024 * 1024; // 1MB
	private static final int COMPRESSION_BUFFER_SIZE = 32 * 1024; // 32KB
	private static final int BUILDER_BUFFER_SIZE = 4 * 1024; // 4KB

	// now output text stream will write unicode texts
	private static final byte[] BOM_MARK = { (byte)0xff, (byte)0xfe };
	private static final byte[] DUMMY_POS = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

	private BinaryStream mStream;
	private StringBuilder mBuilder;

	private int mCryptMode;
		// -1 for no-crypt
		// 0: (unused)	(old buggy crypt mode)
		// 1: simple crypt
		// 2: complessed
	private int mCompressionLevel; // compression level of zlib
	private int mCompressionSizePosition;
	private boolean mCompressionFailed;
	private byte[] mCompressionBuffer;
	private Deflater mCompresser;
	private boolean mIsUTF8;

	// mode supports following modes:
	// dN: deflate(compress) at mode N ( currently not implemented )
	// cN: write in cipher at mode N ( currently n is ignored )
	// zN: write with compress at mode N ( N is compression level )
	// oN: write from binary offset N (in bytes)
	public TextWriteStream(String name, String mode) throws TJSException {
		mBuilder = new StringBuilder(BUILDER_BUFFER_SIZE);
		mCryptMode = -1;
		mCompressionLevel = Deflater.DEFAULT_COMPRESSION ;
		//mCompressionSizePosition = 0;
		//mCompressionFailed = false;
		//mCompressionBuffer = null;
		if( mode == null ) mode = "";
		if( "utf-8".equalsIgnoreCase(mode) ) {
			mIsUTF8 = true;
		}
		int p;
		p = mode.indexOf('c');
		if( p != -1 ) {
			mCryptMode = 1; // simple crypt
			if( (p+1) < mode.length() ) {
				char c = mode.charAt(p+1);
				if( c >= '0' && c <= '9' )
					mCryptMode = c - '0';
			}
		}
		p = mode.indexOf('z');
		if( p != -1 ) {
			mCryptMode = 2; // compressed (cannot be with 'c')
			if( (p+1) < mode.length() ) {
				char c = mode.charAt(p+1);
				if( c >= '0' && c <= '9' )
					mCompressionLevel = c - '0';
			}
		}
		if( mCryptMode != -1 && mCryptMode != 1 && mCryptMode != 2 )
			Message.throwExceptionMessage(Message.UnsupportedModeString, "unsupported cipher mode" );

		// check o mode
		int o_ofs = mode.indexOf('o');
		if( o_ofs != -1 ) {
			// seek to offset
			o_ofs++;
			StringBuilder buf = new StringBuilder(256);
			final int modelen = mode.length() - o_ofs;
			for( int i = 0; i < modelen; i++ ) {
				char c = mode.charAt(o_ofs+i);
				if( c >= '0' && c <= '9' )
					buf.append(c);
				else break;
			}
			long ofs = Integer.valueOf( buf.toString() );
			mStream = Storage.createStream( name, BinaryStream.UPDATE );
			mStream.setPosition(ofs);
		} else {
			mStream = Storage.createStream( name, BinaryStream.WRITE );
		}

		if( mCryptMode == 1 || mCryptMode == 2 ) {
			// simple crypt or compressed
			byte[] crypt_mode_sig = new byte[3];
			crypt_mode_sig[0] = crypt_mode_sig[1] = (byte)0xfe;
			crypt_mode_sig[2] = (byte)mCryptMode;
			mStream.write(crypt_mode_sig);
		}

		// now output text stream will write unicode texts
		if( mIsUTF8 == false )
			mStream.write(BOM_MARK);

		if( mCryptMode == 2 ) {
			// allocate and initialize zlib straem
			mCompresser = new Deflater();
			mCompresser.setLevel(mCompressionLevel);
			try {
				mCompressionBuffer = new byte[COMPRESSION_BUFFER_SIZE];
			} catch( OutOfMemoryError e ) { // メモリ不足の場合コンパクト化とgcを実行して出来るだけメモリを空ける
				DebugClass.addLog("Faild to allocate memory in Text write buffer. Force GC and try agian.");
				TVP.EventManager.deliverCompactEvent(CompactEventCallbackInterface.COMPACT_LEVEL_MAX);
				System.gc();

				mCompressionBuffer = new byte[COMPRESSION_BUFFER_SIZE>>>2]; // 一度失敗したら半分にしてみる
			}
			// mCompressionFailed = true;
			// TVPThrowExceptionMessage(TVPCompressionFailed);

			// Compression Size (write dummy)
			mCompressionSizePosition = (int) mStream.getPosition();
			mStream.write( DUMMY_POS );
		}
	}
	public void write( final String val ) throws TJSException {
		if( mStream != null ) {
			final int vallen = val.length();
			final int length = mBuilder.length();
			if( vallen > BUILDER_BUFFER_SIZE ) {
				if( length > 0 ) {
					writeInternal();
					mBuilder.delete( 0, length );
				}
				writeInternal( val );
			} else if( (length + vallen) > BUILDER_BUFFER_SIZE ) {
				writeInternal();
				mBuilder.delete( 0, length );
				mBuilder.append( val );
			} else {
				mBuilder.append( val );
			}
		}
	}
	private void writeInternal() throws TJSException {
		if( mIsUTF8 == false ) {
			StringBuilder val = mBuilder;
			int len = val.length();
			byte[] buf = new byte[len*2];
			try {
				if( mCryptMode == 1 ) {
					// simple crypt
					for( int i = 0; i < len; i++ ) {
						char ch = val.charAt(i);
						ch = (char)( (((ch & 0xaaaaaaaa)>>>1) | ((ch & 0x55555555)<<1))&0xffff );
						buf[(i<<1)]   = (byte) (ch&0xff);
						buf[(i<<1)+1] = (byte) (ch>>>8);
					}
					writeRawData( buf );
				} else {
					for( int i = 0; i < len; i++ ) {
						char ch = val.charAt(i);
						buf[(i<<1)]   = (byte) (ch&0xff);
						buf[(i<<1)+1] = (byte) (ch>>>8);
					}
					writeRawData( buf );
				}
			} finally {
				buf = null;
			}
		} else {
			try {
				String val = mBuilder.toString();
				byte[] buf = val.getBytes("UTF-8");
				writeRawData( buf );
			} catch (UnsupportedEncodingException e) {
				Message.throwExceptionMessage(Message.WriteError);
			}
		}
	}
	private void writeInternal( String val ) throws TJSException {
		if( mIsUTF8 == false ) {
			int len = val.length();
			byte[] buf = new byte[len*2];
			try {
				if( mCryptMode == 1 ) {
					// simple crypt
					for( int i = 0; i < len; i++ ) {
						char ch = val.charAt(i);
						ch = (char)( (((ch & 0xaaaaaaaa)>>>1) | ((ch & 0x55555555)<<1))&0xffff );
						buf[(i<<1)]   = (byte) (ch&0xff);
						buf[(i<<1)+1] = (byte) (ch>>>8);
					}
					writeRawData( buf );
				} else {
					for( int i = 0; i < len; i++ ) {
						char ch = val.charAt(i);
						buf[(i<<1)]   = (byte) (ch&0xff);
						buf[(i<<1)+1] = (byte) (ch>>>8);
					}
					writeRawData( buf );
				}
			} finally {
				buf = null;
			}
		} else {
			try {
				byte[] buf = val.getBytes("UTF-8");
				writeRawData( buf );
			} catch (UnsupportedEncodingException e) {
				Message.throwExceptionMessage(Message.WriteError);
			}
		}
	}
	private void writeRawData(byte[] buf) {
		if( mCryptMode == 2 ) {
			// compressed with zlib stream.
			mCompresser.setInput(buf);
			while( !mCompresser.needsInput() ) {
				int count = mCompresser.deflate(mCompressionBuffer);
				if( count > 0 )  mStream.write( mCompressionBuffer, 0, count );
			}
		} else {
			mStream.write( buf ); // write directly
		}
	}
	public void destruct() throws TJSException {
		writetail();
		if( mStream != null ) {
			mStream.close();
			mStream = null;
		}
	}
	private void writetail() throws TJSException {
		if( mStream != null ) {
			final int length = mBuilder.length();
			if( length > 0 ) {
				writeInternal();
				mBuilder.delete( 0, length );
			}
			if( mCryptMode == 2 ) {
				if( !mCompressionFailed ) {
					try {
						mCompresser.finish();
						while( !mCompresser.finished() ) {
							int count = mCompresser.deflate(mCompressionBuffer);
							mStream.write( mCompressionBuffer, 0, count );
						}
						long totalin = mCompresser.getTotalIn();
						long totalout = mCompresser.getTotalOut();
						mCompresser.reset();

						byte[] size = new byte[16];
						ByteBuffer buf = ByteBuffer.wrap(size);
						buf.order(ByteOrder.LITTLE_ENDIAN);
						buf.putLong(totalout);
						buf.putLong(totalin);
						mStream.setPosition(mCompressionSizePosition);
						mStream.write(size);
					} finally {
						mCompressionBuffer = null;
						mCompresser = null;
						mStream.close();
						mStream = null;
					}
				}
			}
		}
	}
	protected void finalize() {
		if( mStream != null ) {
			try {
				writetail();
				mStream.close();
				mStream = null;
			} catch (TJSException e) {
			}
		}
	}
}
