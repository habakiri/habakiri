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
package jp.kirikiri.tvp2.visual;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

import jp.kirikiri.tjs2.BinaryStream;
import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tvp2.msg.Message;

/**
 * プリレンダリング済みフォントクラス
 */
public class PrerenderedFont {

	private static final byte[] HEADER = {
		'T', 'V', 'P', ' ', 'p', 'r', 'e', '-', 'r', 'e', 'n', 'd', 'e', 'r', 'e', 'd', ' ', 'f', 'o', 'n', 't', 0x1a
	};
	private static final int SKIP_SIZE = 24;

	private int mVersion;
	private int mIndexCount;

	private char[] mChIndex;
	//private byte[] mData;
	private int mDataOffset;
	private ByteBuffer mDataBuffer;

	static private final int CHARACTER_ITEM_SIZE = 20;
	static public class CharacterItem {
		public int Offset;
		public short Width;
		public short Height;
		public short OriginX;
		public short OriginY;
		public short IncX;
		public short IncY;
		public short Inc;
		public short Reserved;
	}
	private CharacterItem mWork;
	public PrerenderedFont( BinaryStream input ) throws TJSException {
		mWork = new CharacterItem();
		load( input );
	}
	private void load( BinaryStream src ) throws TJSException {
		byte[] buff = new byte[22];
		src.setPosition(0);
		src.read(buff);
		if( Arrays.equals( HEADER, buff ) == false ) {
			Message.throwExceptionMessage(Message.PrerenderedFontMappingFailed, "Signature not found or invalid pre-rendered font file.");
		}
		src.read(buff,0,2);

		if(buff[1] != 2) {
			Message.throwExceptionMessage(Message.PrerenderedFontMappingFailed,"Not a 16-bit UNICODE font file.");
		}
		mVersion = buff[0];
		if( mVersion != 0 && mVersion != 1 ) {
			Message.throwExceptionMessage(Message.PrerenderedFontMappingFailed, "Invalid header version.");
		}
		int length = (int) (src.getSize() - SKIP_SIZE);
		mDataBuffer = ByteBuffer.allocateDirect(length);
		src.read(mDataBuffer);
		ByteBuffer bb = mDataBuffer;
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.clear();
		mDataBuffer = bb;
		mIndexCount = bb.getInt();
		int chOffset = bb.getInt();
		mDataOffset = bb.getInt();

		bb.position(chOffset - SKIP_SIZE);
		CharBuffer cb = bb.asCharBuffer();
		mChIndex = new char[mIndexCount];
		cb.get(mChIndex);
	}
	public CharacterItem getCharData( char ch ) {
		int idx = findCh( ch );
		if( idx < 0 ) return null;

		int offset = mDataOffset + idx * CHARACTER_ITEM_SIZE - SKIP_SIZE;
		ByteBuffer bb = mDataBuffer;
		bb.position(offset);
		CharacterItem ret = mWork;
		ret.Offset = bb.getInt();
		ret.Width = bb.getShort();
		ret.Height = bb.getShort();
		ret.OriginX = bb.getShort();
		ret.OriginY = bb.getShort();
		ret.IncX = bb.getShort();
		ret.IncY = bb.getShort();
		ret.Inc = bb.getShort();
		ret.Reserved = bb.getShort();

		return ret;
	}
	private int findCh( char ch ) {
		// search through ChIndex
		int s = 0;
		int e = mIndexCount;
		final char[] chindex = mChIndex;
		while( true ) {
			int d = e-s;
			if( d <= 1 ) {
				if(chindex[s] == ch)
					return s;
				else
					return -1;
			}
			int m = s + (d>>1);
			int c = ((int)chindex[m])&0xffff;
			if( c > ch) e = m; else s = m;
		}
	}
	public void retrieve( final CharacterItem item, byte[] buffer, int bufferpitch ) {
		// retrieve font data and store to buffer
		// bufferpitch must be larger then or equal to item.Width
		if(item.Width == 0 || item.Height == 0) return;

		final int w = item.Width;
		int ptr = item.Offset - SKIP_SIZE;
		int dest = 0;
		int destlim = w * item.Height;

		ByteBuffer bb = mDataBuffer;
		// expand compressed character bitmap data
		if( mVersion == 0 ) {
			// version 0 decompressor
			while( dest < destlim ) {
				if( bb.get(ptr) == 0x41) { // running
					ptr++;
					byte last = buffer[dest-1];
					int len = bb.get(ptr);
					ptr++;
					while(len > 0) {
						len--;
						buffer[dest] = last;
						dest++;
					}
				} else {
					buffer[dest] = bb.get(ptr);
					dest++;
					ptr++;
				}
			}
		} else if( mVersion >= 1 ) {
			// version 1+ decompressor
			while( dest < destlim ) {
				int p = ((int)bb.get(ptr))&0xff;
				if( p >= 0x41 ) { // running
					int len = p - 0x40;
					ptr++;
					byte last = buffer[dest-1];
					while(len > 0) {
						len--;
						buffer[dest] = last;
						dest++;
					}
				} else {
					buffer[dest] = bb.get(ptr);
					dest++;
					ptr++;
				}
			}
		}

		// expand to each pitch
		ptr = destlim - w;
		dest = bufferpitch * item.Height - bufferpitch;
		byte[] work = new byte[w];
		while( dest >= 0 ) {
			if( dest != ptr ) {
				System.arraycopy( buffer, ptr, work, 0, w );
				System.arraycopy( work, 0, buffer, dest, w );
			}
			dest -= bufferpitch;
			ptr -= w;
		}
	}
}
