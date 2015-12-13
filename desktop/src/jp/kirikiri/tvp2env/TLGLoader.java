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

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.WritableRaster;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.HashMap;

import jp.kirikiri.tjs2.BinaryStream;
import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tvp2.msg.Message;
import jp.kirikiri.tvp2.visual.GraphicsLoader;

/**
 * 途中のバッファーで固定サイズのものは使い回しを考えた方が良さそう
 *
 */
public class TLGLoader {
	/** "TLG0.0\x00sds\x1a" */
	static private final byte[] HEADER = { 'T','L','G','0','.','0',0,'s','d','s',0x1a };
	/** "TLG5.0\x00raw\x1a" */
	static private final byte[] HEADER_TLG5 = { 'T','L','G','5','.','0',0,'r','a','w',0x1a };
	/** "TLG6.0\x00raw\x1a" */
	static private final byte[] HEADER_TLG6 = { 'T','L','G','6','.','0',0,'r','a','w',0x1a };

	static private final int TAGS = 't' | ('a'<<8) | ('g'<<16) | ('s'<<24);

	static public final BufferedImage loadTLG( BinaryStream src, int mode, HashMap<String,String> metainfo ) throws TJSException {
		BufferedImage result = null;

		// read header
		byte[] mark = new byte[11];
		src.setPosition(0);
		src.read(mark);
		if( Arrays.equals( HEADER, mark ) ) {
			// read TLG0.0 Structured Data Stream

			// TLG0.0 SDS tagged data is simple "NAME=VALUE," string;
			// Each NAME and VALUE have length:content expression.
			// eg: 4:LEFT=2:20,3:TOP=3:120,4:TYPE=1:3,
			// The last ',' cannot be ommited.
			// Each string (name and value) must be encoded in utf-8.
			byte[] intbuf = new byte[4];
			src.read(intbuf);
			ByteBuffer ibuff = ByteBuffer.wrap(intbuf);
			ibuff.order(ByteOrder.LITTLE_ENDIAN);
			ibuff.position(0);

			// read raw data size
			int rawlen = ibuff.getInt();
			long imgPos = src.getPosition();

			// seek to meta info data point
			src.seek( rawlen + 11 + 4, BinaryStream.SEEK_SET );

			// read tag data
			while( true ) {
				if(4 != src.read(intbuf)) break; // cannot read more
				ibuff.position(0);
				int chunkname = ibuff.getInt();

				src.read(intbuf);
				ibuff.position(0);
				int chunksize = ibuff.getInt();

				if( TAGS == chunkname ) {
					// tag information
					byte[] tag = null;
					String name = null;
					String value = null;
					try {
						tag = new byte[chunksize + 1];
						src.read( tag);
						tag[chunksize] = 0;
						if( metainfo != null ) {
							for( int i = 0; i < chunksize; ) {
								int namelen = 0;
								while( tag[i] >= '0' && tag[i] <= '9') {
									namelen = namelen * 10 + tag[i] - '0';
									i++;
								}
								if( tag[i] != ':')
									Message.throwExceptionMessage( Message.TLGLoadError, "Malformed TLG SDS tag structure, missing colon after name length" );
								i++;

								name = new String(tag,i,namelen,"MS932");
								i += namelen;

								if( tag[i] != '=')
									Message.throwExceptionMessage( Message.TLGLoadError, "Malformed TLG SDS tag structure, missing equals after name" );
								i++;
								int valuelen = 0;
								while( tag[i] >= '0' && tag[i] <= '9') {
									valuelen = valuelen * 10 + tag[i] - '0';
									i++;
								}
								if( tag[i] != ':')
									Message.throwExceptionMessage( Message.TLGLoadError, "Malformed TLG SDS tag structure, missing colon after value length" );
								i++;
								value = new String(tag,i,valuelen,"MS932");
								i += valuelen;

								if( tag[i] != ',')
									Message.throwExceptionMessage( Message.TLGLoadError, "Malformed TLG SDS tag structure, missing comma after a tag" );
								i++;

								// insert into name-value pairs ...
								metainfo.put(name, value);

								name = null;
								value = null;
							}
						}
					} catch (UnsupportedEncodingException e) {
						Message.throwExceptionMessage( Message.TLGLoadError, "Malformed TLG SDS tag structure, character cannot decoeded" );
					} finally {
						tag = null;
						name = null;
						value = null;
					}
				} else {
					// skip the chunk
					src.setPosition( src.getPosition() + chunksize );
				}
			}
			src.setPosition(imgPos);
			result = internalLoadTLG( src, mode, metainfo );
		} else {
			src.seek(0, BinaryStream.SEEK_SET); // rewind
			result = internalLoadTLG( src, mode, metainfo );
		}
		return result;
	}

	private static final BufferedImage internalLoadTLG(BinaryStream src, int mode, HashMap<String,String> metainfo) throws TJSException {
		// read header
		byte[] mark = new byte[11];
		src.read(mark);
		if( Arrays.equals( HEADER_TLG5, mark ) ) {
			return LoadTLG5( src, mode, metainfo );
		} else if( Arrays.equals( HEADER_TLG6, mark ) ) {
			return LoadTLG6( src, mode, metainfo );
		} else {
			Message.throwExceptionMessage( Message.TLGLoadError, "Invalid TLG header or unsupported TLG version." );
		}
		return null;
	}
	public static final BufferedImage LoadTLG5(BinaryStream src, int mode, HashMap<String,String> metainfo) throws TJSException {
		// load TLG v5.0 lossless compressed graphic
		if(mode != GraphicsLoader.glmNormal)
			Message.throwExceptionMessage( Message.TLGLoadError, "TLG cannot be used as universal transition rule, province(_p) or mask(_m) images.");

		BufferedImage result = null;

		byte[] mark = new byte[13];
		int width, height, colors, blockheight;
		src.read(mark);
		ByteBuffer buff = ByteBuffer.wrap(mark);
		buff.order(ByteOrder.LITTLE_ENDIAN);
		buff.position(0);
		colors = buff.get();
		width = buff.getInt();
		height = buff.getInt();
		blockheight = buff.getInt();

		if(colors != 3 && colors != 4)
			Message.throwExceptionMessage( Message.TLGLoadError, "Unsupported color type." );

		int blockcount = (int)((height - 1) / blockheight) + 1;

		// skip block size section
		src.setPosition( src.getPosition() + blockcount * 4 );

		int imgType = BufferedImage.TYPE_INT_ARGB;
		if( metainfo != null ) {
			String imgMode = metainfo.get("mode");
			if( imgMode != null ) {
				if( imgMode.equals("addalpha") ) {
					imgType = BufferedImage.TYPE_INT_ARGB_PRE;
				}
			}
		}

		// decomperss
		result = new BufferedImage( width, height, imgType );
		int[] imagedata = null;

		//result.setRGB(0, 0, width, height, imagedata, 0, width );
		WritableRaster srcRaster = result.getRaster();
		DataBuffer dataBuff = srcRaster.getDataBuffer();
		final int type = dataBuff.getDataType();
		if( type == DataBuffer.TYPE_INT ) {
			DataBufferInt srcBuff = (DataBufferInt)dataBuff;
			imagedata = srcBuff.getData();
		} else {
			Message.throwExceptionMessage( Message.TLGLoadError, "Internal error." );
		}


		byte[] inbuf = null;
		byte[][] outbuf = new byte[4][];
		byte[] text = new byte[4096];
		int rz = 0;

		try {

			inbuf = new byte[blockheight * width + 10];
			for( int i = 0; i < colors; i++)
				outbuf[i] = new byte[blockheight * width + 10];

			int prevline = -1;
			for( int y_blk = 0; y_blk < height; y_blk += blockheight ){
				// read file and decompress
				for( int c = 0; c < colors; c++) {
					src.read(mark, 0, 5);
					buff.position(0);
					byte m = buff.get();
					int size = buff.getInt();
					if( m == 0 ) {
						// modified LZSS compressed data
						src.read( inbuf, 0, size );
						rz = decompressSlideLZSS(outbuf[c], inbuf, size, text, rz );
					} else {
						// raw data
						src.read( outbuf[c], 0, size );
					}
				}

				// compose colors and store
				int y_lim = y_blk + blockheight;
				if( y_lim > height ) y_lim = height;
				int outbufoff = 0;
				for( int y = y_blk; y < y_lim; y++ ) {
					int current = y * width;
					int current_org = current;
					if( prevline >= 0 ) {
						// not first line
						final int end = outbufoff + width;
						switch( colors ) {
						case 3: {
							int tr = 0, tg = 0, tb = 0;
							for( ; outbufoff < end; outbufoff++ ) {
								int b = outbuf[0][outbufoff] & 0xff;
								int g = outbuf[1][outbufoff] & 0xff;
								int r = outbuf[2][outbufoff] & 0xff;
								b += g; r += g;
								int pcolor = imagedata[prevline];
								int pb = pcolor & 0xff;
								int pg = (pcolor >>> 8) & 0xff;
								int pr = (pcolor >>>16) & 0xff;
								imagedata[current] =
														((((tb += b) + pb) & 0xff)      ) +
														((((tg += g) + pg) & 0xff) <<  8) +
														((((tr += r) + pr) & 0xff) << 16) +
														0xff000000;
								current++;
								prevline++;
							}
							break;
						}
						case 4: {
							int tr = 0, tg = 0, tb = 0, ta = 0;
							for( ; outbufoff < end; outbufoff++ ) {
								int b = outbuf[0][outbufoff] & 0xff;
								int g = outbuf[1][outbufoff] & 0xff;
								int r = outbuf[2][outbufoff] & 0xff;
								int a = outbuf[3][outbufoff] & 0xff;
								b += g; r += g;
								int pcolor = imagedata[prevline];
								int pb = pcolor & 0xff;
								int pg = (pcolor >>> 8) & 0xff;
								int pr = (pcolor >>>16) & 0xff;
								int pa = (pcolor >>>24) & 0xff;
								imagedata[current] =
														((((tb += b) + pb) & 0xff)      ) +
														((((tg += g) + pg) & 0xff) <<  8) +
														((((tr += r) + pr) & 0xff) << 16) +
														((((ta += a) + pa) & 0xff) << 24);
								current++;
								prevline++;
							}
							break;
						}
						}
					} else {
						// first line
						final int end = outbufoff + width;
						switch( colors ) {
						case 3:
							for( int pr = 0, pg = 0, pb = 0; outbufoff < end; outbufoff++ ) {
								int b = outbuf[0][outbufoff] & 0xff;
								int g = outbuf[1][outbufoff] & 0xff;
								int r = outbuf[2][outbufoff] & 0xff;
								b += g; r += g;
								pb += b;
								pg += g;
								pr += r;
								imagedata[current] = (0xff << 24) | ((pr&0xff)<<16) | ((pg&0xff)<<8) | (pb&0xff);
								current++;
							}
							break;
						case 4:
							for( int pr = 0, pg = 0, pb = 0, pa = 0; outbufoff < end; outbufoff++ ) {
								int b = outbuf[0][outbufoff] & 0xff;
								int g = outbuf[1][outbufoff] & 0xff;
								int r = outbuf[2][outbufoff] & 0xff;
								int a = outbuf[3][outbufoff] & 0xff;
								b += g; r += g;
								pb += b;
								pg += g;
								pr += r;
								pa += a;
								imagedata[current] = ((pa&0xff) << 24) | ((pr&0xff)<<16) | ((pg&0xff)<<8) | (pb&0xff);
								current++;
							}
							break;
						}
					}
					prevline = current_org;
				}
			}
		} finally {
			inbuf = null;
			text = null;
			for( int i = 0; i < colors; i++ )
				outbuf[i] = null;
			outbuf = null;
		}
		return result;
	}

	private static final int decompressSlideLZSS(byte[] out, byte[] in, int insize, byte[] text, int initialr ) {
		int r = initialr;
		int flags = 0;
		int o = 0;
		for( int i = 0; i < insize; ) {
			if( ((flags >>>= 1) & 256) == 0 ) {
				flags = (in[i] & 0xff) | 0xff00;
				i++;
			}
			if( (flags & 1) == 1 ) {
				int mpos = (in[i]&0xff) | ((in[i+1] & 0xf) << 8);
				int mlen = (in[i+1] & 0xf0) >>> 4;
				i += 2;
				mlen += 3;
				if(mlen == 18) {
					mlen += (in[i]&0xff);
					i++;
				}

				while( mlen > 0 ) {
					out[o++] = text[r++] = text[mpos++];
					mpos &= (4096 - 1);
					r &= (4096 - 1);
					mlen--;
				}
			} else {
				byte c = in[i];
				i++;
				out[o] = c;
				o++;
				text[r] = c;
				r++;
				r &= (4096 - 1);
			}
		}
		return r;
	}

	static final int TLG6_H_BLOCK_SIZE = 8;
	static final int TLG6_W_BLOCK_SIZE = 8;
	public static final BufferedImage LoadTLG6(BinaryStream src, int mode, HashMap<String,String> metainfo) throws TJSException {
		// load TLG v6.0 lossless/near-lossless compressed graphic
		if(mode != GraphicsLoader.glmNormal)
			Message.throwExceptionMessage( Message.TLGLoadError, "TLG cannot be used as universal transition rule, province(_p) or mask(_m) images.");

		byte[] buf = new byte[12];

		src.read(buf, 0, 4);

		int colors = buf[0]; // color component count

		if(colors != 1 && colors != 4 && colors != 3)
			Message.throwExceptionMessage( Message.TLGLoadError, "Unsupported color count : " + colors );

		if(buf[1] != 0) // data flag
			Message.throwExceptionMessage( Message.TLGLoadError, "Data flag must be 0 (any flags are not yet supported)" );

		if(buf[2] != 0) // color type  (currently always zero)
			Message.throwExceptionMessage( Message.TLGLoadError, "Unsupported color type : " + (int)buf[1] );

		if(buf[3] != 0) // external golomb table (currently always zero)
			Message.throwExceptionMessage( Message.TLGLoadError, "External golomb bit length table is not yet supported." );

		src.read(buf, 0, 12);
		ByteBuffer buff = ByteBuffer.wrap(buf);
		buff.order(ByteOrder.LITTLE_ENDIAN);
		buff.position(0);

		int width, height;

		width = buff.getInt();
		height = buff.getInt();

		int max_bit_length;
		max_bit_length = buff.getInt();

		// set destination size
		int imgType = BufferedImage.TYPE_INT_ARGB;
		if( metainfo != null ) {
			String imgMode = metainfo.get("mode");
			if( imgMode != null ) {
				if( imgMode.equals("addalpha") ) {
					imgType = BufferedImage.TYPE_INT_ARGB_PRE;
				}
			}
		}

		// decomperss
		BufferedImage result = new BufferedImage( width, height, imgType );
		int[] imagedata = null;

		WritableRaster srcRaster = result.getRaster();
		DataBuffer dataBuff = srcRaster.getDataBuffer();
		final int type = dataBuff.getDataType();
		if( type == DataBuffer.TYPE_INT ) {
			DataBufferInt srcBuff = (DataBufferInt)dataBuff;
			imagedata = srcBuff.getData();
		} else {
			Message.throwExceptionMessage( Message.TLGLoadError, "Internal error." );
		}

		// compute some values
		int x_block_count = ((width - 1)/ TLG6_W_BLOCK_SIZE) + 1;
		int y_block_count = ((height - 1)/ TLG6_H_BLOCK_SIZE) + 1;
		int main_count = width / TLG6_W_BLOCK_SIZE;
		int fraction = width -  main_count * TLG6_W_BLOCK_SIZE;

		// prepare memory pointers
		byte[] bit_pool = null;
		int[] pixelbuf = null; // pixel buffer
		byte[] filter_types = null;
		byte[] LZSS_text = null;
		int[] zeroline = null;

		try {
			// allocate memories
			bit_pool = new byte[max_bit_length / 8 + 5];
			pixelbuf = new int[width * TLG6_H_BLOCK_SIZE + 1];
			filter_types = new byte[x_block_count * y_block_count];
			zeroline = new int[width];
			LZSS_text = new byte[4096];

			// initialize zero line (virtual y=-1 line)
			if( colors==3 ) {
				Arrays.fill(zeroline, 0xff000000 );
			}
			// TVPFillARGB(zeroline, width, colors==3?0xff000000:0x00000000);
				// 0xff000000 for colors=3 makes alpha value opaque

			// initialize LZSS text (used by chroma filter type codes)
			{
				ByteBuffer p = ByteBuffer.wrap(LZSS_text);
				p.order(ByteOrder.LITTLE_ENDIAN);
				p.position(0);
				for( int i = 0; i < 32*0x01010101; i+=0x01010101 ) {
					for( int j = 0; j < 16*0x01010101; j+=0x01010101 ) {
						p.putInt(i);
						p.putInt(j);
					}
				}
			}

			// read chroma filter types.
			// chroma filter types are compressed via LZSS as used by TLG5.
			{
				src.read(buf, 0, 4);
				buff.position(0);

				int inbuf_size = buff.getInt();
				byte[] inbuf = new byte[inbuf_size];
				try {
					src.read( inbuf );
					decompressSlideLZSS( filter_types, inbuf, inbuf_size, LZSS_text, 0);
				} finally {
					inbuf = null;
				}
			}

			// for each horizontal block group ...
			int prevline = -1;
			for( int y = 0; y < height; y += TLG6_H_BLOCK_SIZE ) {
				int ylim = y + TLG6_H_BLOCK_SIZE;
				if(ylim >= height) ylim = height;

				int pixel_count = (ylim - y) * width;

				// decode values
				for( int c = 0; c < colors; c++ ) {
					// read bit length
					src.read(buf, 0, 4);
					buff.position(0);
					int bit_length = buff.getInt();

					// get compress method
					int method = (bit_length >>> 30)&3;
					bit_length &= 0x3fffffff;

					// compute byte length
					int byte_length = bit_length / 8;
					if( (bit_length % 8) != 0 ) byte_length++;

					// read source from input
					src.read(bit_pool, 0, byte_length);

					// decode values
					// two most significant bits of bitlength are
					// entropy coding method;
					// 00 means Golomb method,
					// 01 means Gamma method (not yet suppoted),
					// 10 means modified LZSS method (not yet supported),
					// 11 means raw (uncompressed) data (not yet supported).

					switch(method) {
					case 0:
						if(c == 0 && colors != 1)
							decodeGolombValuesForFirst( pixelbuf, pixel_count, bit_pool);
						else
							decodeGolombValues( pixelbuf, c, pixel_count, bit_pool );
						break;
					default:
						Message.throwExceptionMessage( Message.TLGLoadError, "Unsupported entropy coding method" );
					}
				}

				// for each line
				//byte[] ft = filter_types + (y / TLG6_H_BLOCK_SIZE)*x_block_count;
				int ft_pos = (y / TLG6_H_BLOCK_SIZE)*x_block_count;
				int skipbytes = (ylim-y)*TLG6_W_BLOCK_SIZE;

				for(int yy = y; yy < ylim; yy++) {
					int curline = yy * width;

					int dir = (yy&1)^1;
					int oddskip = ((ylim - yy -1) - (yy-y));
					if( main_count != 0 ) {
						int start = ((width < TLG6_W_BLOCK_SIZE) ? width : TLG6_W_BLOCK_SIZE) * (yy - y);
						TLG6DecodeLine(
							imagedata,
							prevline,
							zeroline,
							curline,
							width,
							main_count,
							filter_types,
							ft_pos,
							skipbytes,
							pixelbuf, start, colors==3?0xff000000:0, oddskip, dir);
					}

					if(main_count != x_block_count)
					{
						int ww = fraction;
						if(ww > TLG6_W_BLOCK_SIZE) ww = TLG6_W_BLOCK_SIZE;
						int start = ww * (yy - y);
						TLG6DecodeLineGeneric(
							imagedata,
							prevline,
							zeroline,
							curline,
							width,
							main_count,
							x_block_count,
							filter_types,
							ft_pos,
							skipbytes,
							pixelbuf, start, colors==3?0xff000000:0, oddskip, dir);
					}
					prevline = curline;
				}

			}
		} finally {
			bit_pool = null;
			pixelbuf = null;
			filter_types = null;
			zeroline = null;
			LZSS_text = null;
		}
		return result;
	}


	private static final int TLG6_GOLOMB_N_COUNT = 4;
	private static final int TLG6_LeadingZeroTable_BITS = 12;
	private static final int TLG6_LeadingZeroTable_SIZE = (1<<TLG6_LeadingZeroTable_BITS);
	private static byte[] TLG6LeadingZeroTable;
	private static final int TLG6GolombBitLengthTable_SIZE = TLG6_GOLOMB_N_COUNT*2*128;
	private static byte[][] TLG6GolombBitLengthTable;
	private static final short[][] TLG6GolombCompressed = {
			{3,7,15,27,63,108,223,448,130,},
			{3,5,13,24,51,95,192,384,257,},
			{2,5,12,21,39,86,155,320,384,},
			{2,3,9,18,33,61,129,258,511,},
	};

	//[TVP_TLG6_GOLOMB_N_COUNT*2*128][TVP_TLG6_GOLOMB_N_COUNT] =

	private static final void TLG6InitLeadingZeroTable() {
		/* table which indicates first set bit position + 1. */
		/* this may be replaced by BSF (IA32 instrcution). */
		TLG6LeadingZeroTable = new byte[TLG6_LeadingZeroTable_SIZE];
		for( int i = 0; i < TLG6_LeadingZeroTable_SIZE; i++) {
			int cnt = 0;
			int j;
			for(j = 1; j != TLG6_LeadingZeroTable_SIZE && (i & j) == 0; j <<= 1, cnt++);
			cnt ++;
			if(j == TLG6_LeadingZeroTable_SIZE) cnt = 0;
			TLG6LeadingZeroTable[i] = (byte) (cnt&0xff);
		}
	}
	private static final void TLG6InitGolombTable() throws TJSException {
		TLG6GolombBitLengthTable = new byte[TLG6GolombBitLengthTable_SIZE][];
		for( int i = 0; i < TLG6GolombBitLengthTable_SIZE; i++ ) {
			TLG6GolombBitLengthTable[i] = new byte[TLG6_GOLOMB_N_COUNT];
		}
		int n, i, j;
		for(n = 0; n < TLG6_GOLOMB_N_COUNT; n++) {
			int a = 0;
			for(i = 0; i < 9; i++) {
				for(j = 0; j < TLG6GolombCompressed[n][i]; j++)
					TLG6GolombBitLengthTable[a++][n] = (byte)i;
			}
			if(a != TLG6_GOLOMB_N_COUNT*2*128)
				Message.throwExceptionMessage(Message.InternalError);
				//*(char*)0 = 0;   /* THIS MUST NOT BE EXECUETED! */
					/* (this is for compressed table data check) */
		}
	}
	private static final void decodeGolombValuesForFirst(int[] pixelbuf, int pixel_count, byte[] bit_pool) throws TJSException {
		if( TLG6LeadingZeroTable == null ) {
			TLG6InitLeadingZeroTable();
			TLG6InitGolombTable();
		}
		/*
		decode values packed in "bit_pool".
		values are coded using golomb code.

		"ForFirst" function do dword access to pixelbuf,
		clearing with zero except for blue (least siginificant byte).
		*/

		int n = TLG6_GOLOMB_N_COUNT - 1; /* output counter */
		int a = 0; /* summary of absolute values of errors */

		int bit_pos = 1;
		byte zero = (byte) ((bit_pool[0]&1) != 0 ? 0 : 1);

		//tjs_int8 * limit = pixelbuf + pixel_count*4;

		//while(pixelbuf < limit)
		int bit_pool_pos = 0;
		int pixelbuf_pos = 0;
		while( pixelbuf_pos < pixel_count ) {
			/* get running count */
			int count;

			{
				int t =(((bit_pool[bit_pool_pos+0]&0xff)       ) +
						((bit_pool[bit_pool_pos+1]&0xff) <<  8 ) +
						((bit_pool[bit_pool_pos+2]&0xff) << 16 ) +
						((bit_pool[bit_pool_pos+3]&0xff) << 24 ))
						>>> bit_pos;

				int b = TLG6LeadingZeroTable[t&(TLG6_LeadingZeroTable_SIZE-1)]&0xff;
				int bit_count = b;
				while( b == 0 ) {
					bit_count += TLG6_LeadingZeroTable_BITS;
					bit_pos += TLG6_LeadingZeroTable_BITS;
					bit_pool_pos += bit_pos >>> 3;
					bit_pos &= 7;
					t = (((bit_pool[bit_pool_pos+0]&0xff)       ) +
						((bit_pool[bit_pool_pos+1]&0xff) <<  8 ) +
						((bit_pool[bit_pool_pos+2]&0xff) << 16 ) +
						((bit_pool[bit_pool_pos+3]&0xff) << 24 ))
						>>> bit_pos;
					b = TLG6LeadingZeroTable[t&(TLG6_LeadingZeroTable_SIZE-1)]&0xff;
					bit_count += b;
				}


				bit_pos += b;
				bit_pool_pos += bit_pos >>> 3;
				bit_pos &= 7;

				bit_count --;
				count = 1 << bit_count;
				//count += ((TVP_TLG6_FETCH_32BITS(bit_pool) >> (bit_pos)) & (count-1));
				count +=((((bit_pool[bit_pool_pos+0]&0xff)       ) +
						((bit_pool[bit_pool_pos+1]&0xff) <<  8 ) +
						((bit_pool[bit_pool_pos+2]&0xff) << 16 ) +
						((bit_pool[bit_pool_pos+3]&0xff) << 24 ))
						>>> bit_pos) & (count-1);

				bit_pos += bit_count;
				bit_pool_pos += bit_pos >>> 3;
				bit_pos &= 7;

			}

			if (zero != 0 ) {
				/* zero values */
				/* fill distination with zero */
				do {
					pixelbuf[pixelbuf_pos] = 0;
					pixelbuf_pos++;
					--count;
				} while(count!=0);
				zero ^= 1;
			} else {
				/* non-zero values */
				/* fill distination with glomb code */
				do {
					int k = TLG6GolombBitLengthTable[a][n], v, sign;
					int t = (((bit_pool[bit_pool_pos+0]&0xff)       ) +
							((bit_pool[bit_pool_pos+1]&0xff) <<  8 ) +
							((bit_pool[bit_pool_pos+2]&0xff) << 16 ) +
							((bit_pool[bit_pool_pos+3]&0xff) << 24 ))
							>>> bit_pos;
					int bit_count;
					int b;
					if( t != 0 ) {
						b = TLG6LeadingZeroTable[t&(TLG6_LeadingZeroTable_SIZE-1)]&0xff;
						bit_count = b;
						while( b == 0 ) {
							bit_count += TLG6_LeadingZeroTable_BITS;
							bit_pos += TLG6_LeadingZeroTable_BITS;
							bit_pool_pos += bit_pos >>> 3;
							bit_pos &= 7;
							t = (((bit_pool[bit_pool_pos+0]&0xff)       ) +
								((bit_pool[bit_pool_pos+1]&0xff) <<  8 ) +
								((bit_pool[bit_pool_pos+2]&0xff) << 16 ) +
								((bit_pool[bit_pool_pos+3]&0xff) << 24 ))
								>>> bit_pos;
							b = TLG6LeadingZeroTable[t&(TLG6_LeadingZeroTable_SIZE-1)]&0xff;
							bit_count += b;
						}
						bit_count --;
					} else {
						bit_pool_pos += 5;
						bit_count = bit_pool[bit_pool_pos-1]&0xff;
						bit_pos = 0;
						t = (((bit_pool[bit_pool_pos+0]&0xff)       ) +
							((bit_pool[bit_pool_pos+1]&0xff) <<  8 ) +
							((bit_pool[bit_pool_pos+2]&0xff) << 16 ) +
							((bit_pool[bit_pool_pos+3]&0xff) << 24 ));
						b = 0;
					}


					v = (bit_count << k) + ((t >>> b) & ((1<<k)-1));
					sign = (v & 1) - 1;
					v >>>= 1;
					a += v;
					pixelbuf[pixelbuf_pos] = ((v ^ sign) + sign + 1)&0xff;
					pixelbuf_pos++;

					bit_pos += b;
					bit_pos += k;
					bit_pool_pos += bit_pos >>> 3;
					bit_pos &= 7;

					if (--n < 0) {
						a >>>= 1;  n = TLG6_GOLOMB_N_COUNT - 1;
					}
					--count;
				} while(count!=0);
				zero ^= 1;
			}
		}
	}

	/**
	 * decodeGolombValuesForFirstとほぼ同じだけど、c でピクセルカラー位置指定するところが違う
	 * @param pixelbuf
	 * @param c
	 * @param pixel_count
	 * @param bit_pool
	 * @throws TJSException
	 */
	private static void decodeGolombValues(int[] pixelbuf, int c, int pixel_count, byte[] bit_pool) throws TJSException {
		if( TLG6LeadingZeroTable == null ) {
			TLG6InitLeadingZeroTable();
			TLG6InitGolombTable();
		}
		c *= 8;
		//final int shift = 24 - c;
		final int shift = c;
		final int mask = ~(0xff << shift);
		/*
		decode values packed in "bit_pool".
		values are coded using golomb code.

		"ForFirst" function do dword access to pixelbuf,
		clearing with zero except for blue (least siginificant byte).
		*/

		int n = TLG6_GOLOMB_N_COUNT - 1; /* output counter */
		int a = 0; /* summary of absolute values of errors */

		int bit_pos = 1;
		byte zero = (byte) ((bit_pool[0]&1) != 0 ? 0 : 1);

		//tjs_int8 * limit = pixelbuf + pixel_count*4;

		//while(pixelbuf < limit)
		int bit_pool_pos = 0;
		int pixelbuf_pos = 0;
		while( pixelbuf_pos < pixel_count ) {
			/* get running count */
			int count;

			{
				int t = (((bit_pool[bit_pool_pos+0]&0xff)       ) +
						((bit_pool[bit_pool_pos+1]&0xff) <<  8 ) +
						((bit_pool[bit_pool_pos+2]&0xff) << 16 ) +
						((bit_pool[bit_pool_pos+3]&0xff) << 24 ))
						>>> bit_pos;

				int b = TLG6LeadingZeroTable[t&(TLG6_LeadingZeroTable_SIZE-1)]&0xff;
				int bit_count = b;
				while( b == 0 ) {
					bit_count += TLG6_LeadingZeroTable_BITS;
					bit_pos += TLG6_LeadingZeroTable_BITS;
					bit_pool_pos += bit_pos >>> 3;
					bit_pos &= 7;
					t = (((bit_pool[bit_pool_pos+0]&0xff)       ) +
						((bit_pool[bit_pool_pos+1]&0xff) <<  8 ) +
						((bit_pool[bit_pool_pos+2]&0xff) << 16 ) +
						((bit_pool[bit_pool_pos+3]&0xff) << 24 ))
						>>> bit_pos;
					b = TLG6LeadingZeroTable[t&(TLG6_LeadingZeroTable_SIZE-1)]&0xff;
					bit_count += b;
				}


				bit_pos += b;
				bit_pool_pos += bit_pos >>> 3;
				bit_pos &= 7;

				bit_count --;
				count = 1 << bit_count;
				//count += ((TVP_TLG6_FETCH_32BITS(bit_pool) >> (bit_pos)) & (count-1));
				count +=((((bit_pool[bit_pool_pos+0]&0xff)       ) +
						((bit_pool[bit_pool_pos+1]&0xff) <<  8 ) +
						((bit_pool[bit_pool_pos+2]&0xff) << 16 ) +
						((bit_pool[bit_pool_pos+3]&0xff) << 24 ))
						>>> bit_pos) & (count-1);

				bit_pos += bit_count;
				bit_pool_pos += bit_pos >>> 3;
				bit_pos &= 7;

			}

			if (zero != 0 ) {
				/* zero values */
				/* fill distination with zero */
				do {
					pixelbuf[pixelbuf_pos] = (pixelbuf[pixelbuf_pos] & mask);
					pixelbuf_pos++;
					--count;
				} while(count!=0);
				zero ^= 1;
			} else {
				/* non-zero values */
				/* fill distination with glomb code */
				do {
					int k = TLG6GolombBitLengthTable[a][n], v, sign;
					int t = (((bit_pool[bit_pool_pos+0]&0xff)       ) +
							((bit_pool[bit_pool_pos+1]&0xff) <<  8 ) +
							((bit_pool[bit_pool_pos+2]&0xff) << 16 ) +
							((bit_pool[bit_pool_pos+3]&0xff) << 24 ))
							>>> bit_pos;
					int bit_count;
					int b;
					if( t != 0 ) {
						b = TLG6LeadingZeroTable[t&(TLG6_LeadingZeroTable_SIZE-1)]&0xff;
						bit_count = b;
						while( b == 0 ) {
							bit_count += TLG6_LeadingZeroTable_BITS;
							bit_pos += TLG6_LeadingZeroTable_BITS;
							bit_pool_pos += bit_pos >>> 3;
							bit_pos &= 7;
							t = (((bit_pool[bit_pool_pos+0]&0xff)       ) +
								((bit_pool[bit_pool_pos+1]&0xff) <<  8 ) +
								((bit_pool[bit_pool_pos+2]&0xff) << 16 ) +
								((bit_pool[bit_pool_pos+3]&0xff) << 24 ))
								>>> bit_pos;
							b = TLG6LeadingZeroTable[t&(TLG6_LeadingZeroTable_SIZE-1)]&0xff;
							bit_count += b;
						}
						bit_count --;
					} else {
						bit_pool_pos += 5;
						bit_count = bit_pool[bit_pool_pos-1]&0xff;
						bit_pos = 0;
						t = (((bit_pool[bit_pool_pos+0]&0xff)       ) +
							((bit_pool[bit_pool_pos+1]&0xff) <<  8 ) +
							((bit_pool[bit_pool_pos+2]&0xff) << 16 ) +
							((bit_pool[bit_pool_pos+3]&0xff) << 24 ));
						b = 0;
					}


					v = (bit_count << k) + ((t >>> b) & ((1<<k)-1));
					sign = (v & 1) - 1;
					v >>>= 1;
					a += v;
					pixelbuf[pixelbuf_pos] = (pixelbuf[pixelbuf_pos] & mask) | ((((v ^ sign) + sign + 1)&0xff) << shift);
					pixelbuf_pos++;

					bit_pos += b;
					bit_pos += k;
					bit_pool_pos += bit_pos >>> 3;
					bit_pos &= 7;

					if (--n < 0) {
						a >>>= 1;  n = TLG6_GOLOMB_N_COUNT - 1;
					}
					--count;
				} while(count!=0);
				zero ^= 1;
			}
		}
	}

	private static final int make_gt_mask( int a, int b ) {
		int tmp2 = ~b;
		int tmp = ((a & tmp2) + (((a ^ tmp2) >>> 1) & 0x7f7f7f7f) ) & 0x80808080;
		tmp = ((tmp >>> 7) + 0x7f7f7f7f) ^ 0x7f7f7f7f;
		return tmp;
	}
	private static final int packed_bytes_add( int a, int b ) {
		int tmp = (((a & b)<<1) + ((a ^ b) & 0xfefefefe) ) & 0x01010100;
		return a+b-tmp;
	}
	private static final int med2( int a, int b, int c ) {
		/* do Median Edge Detector   thx, Mr. sugi  at    kirikiri.info */
		int aa_gt_bb = make_gt_mask(a, b);
		int a_xor_b_and_aa_gt_bb = ((a ^ b) & aa_gt_bb);
		int aa = a_xor_b_and_aa_gt_bb ^ a;
		int bb = a_xor_b_and_aa_gt_bb ^ b;
		int n = make_gt_mask(c, bb);
		int nn = make_gt_mask(aa, c);
		int m = ~(n | nn);
		return (n & aa) | (nn & bb) | ((bb & m) - (c & m) + (aa & m));
	}
	private static final int med( int a, int b, int c, int v ) {
		return packed_bytes_add(med2(a, b, c), v);
	}
	private static final int avg( int a, int b, int c, int v ) {
		int pack = ((((a) & (b)) + ((((a) ^ (b)) & 0xfefefefe) >>> 1)) + (((a)^(b))&0x01010101));
		return packed_bytes_add( pack, v );
	}
	private static final void TLG6DecodeLineGeneric( int[] imagedata, int prevline, int[] zeroline, int curline, int width, int start_block, int block_limit, byte[] filtertypes, int ft_pos, int skipblockbytes, int[] in, int in_pos, int initialp, int oddskip, int dir) {
		/*
		chroma/luminosity decoding
		(this does reordering, color correlation filter, MED/AVG  at a time)
		*/
		int p, up;
		int step, i;

		int[] prevdata;
		if( prevline < 0 ) {
			prevdata = zeroline;
			prevline = 0;
		} else {
			prevdata = imagedata;
		}
		if(start_block != 0 ) {
			prevline += start_block * TLG6_W_BLOCK_SIZE;
			curline  += start_block * TLG6_W_BLOCK_SIZE;
			p  = imagedata[curline-1];
			up = prevdata[prevline-1];
		} else {
			p = up = initialp;
		}

		in_pos += skipblockbytes * start_block;
		step = (dir&1) != 0 ? 1 : -1;

		for( i = start_block; i < block_limit; i ++ ) {
			int w = width - i*TLG6_W_BLOCK_SIZE, ww;
			if(w > TLG6_W_BLOCK_SIZE) w = TLG6_W_BLOCK_SIZE;
			ww = w;
			if(step==-1) in_pos += ww-1;
			if( (i&1) != 0 ) in_pos += oddskip * ww;
			/*
			int A  = ((in[in_pos]>>>24)&0xff);
			int IR = ((in[in_pos]>>>16)&0xff);
			int IG = ((in[in_pos]>>>8 )&0xff);
			int IB = ((in[in_pos]     )&0xff);
			*/
			int R, G, B;
			switch( filtertypes[ft_pos+i] ) {
			//TVP_TLG6_DO_CHROMA_DECODE( 0, IB, IG, IR);
			case 0 << 1:
				do {
					int A  = ((in[in_pos]>>>24)&0xff);
					int IR = ((in[in_pos]>>>16)&0xff);
					int IG = ((in[in_pos]>>>8 )&0xff);
					int IB = ((in[in_pos]     )&0xff);
					B = IB; G = IG; R = IR;
					int u = prevdata[prevline];
					p = med(p, u, up, (0xff0000 & ((R)<<16)) + (0xff00 & ((G)<<8)) + (0xff & (B)) + ((A) << 24) );
					up = u;
					imagedata[curline] = p;
					curline ++;
					prevline ++;
					in_pos += step;
					--w;
				} while(w>0);
				break;
			case (0<<1)+1:
				do {
					int A  = ((in[in_pos]>>>24)&0xff);
					int IR = ((in[in_pos]>>>16)&0xff);
					int IG = ((in[in_pos]>>>8 )&0xff);
					int IB = ((in[in_pos]     )&0xff);
					B = IB; G = IG; R = IR;
					int u = prevdata[prevline];
					p = avg(p, u, up,
						(0xff0000 & ((R)<<16)) + (0xff00 & ((G)<<8)) + (0xff & (B)) + ((A) << 24) );
					up = u;
					imagedata[curline] = p;
					curline ++;
					prevline ++;
					in_pos += step;
					w--;
				} while(w>0);
				break;

			//TVP_TLG6_DO_CHROMA_DECODE( 1, IB+IG, IG, IR+IG);
			case 1 << 1:
				do {
					int A  = ((in[in_pos]>>>24)&0xff);
					int IR = ((in[in_pos]>>>16)&0xff);
					int IG = ((in[in_pos]>>>8 )&0xff);
					int IB = ((in[in_pos]     )&0xff);
					B = IB+IG; G = IG; R = IR+IG;
					int u = prevdata[prevline];
					p = med(p, u, up, (0xff0000 & ((R)<<16)) + (0xff00 & ((G)<<8)) + (0xff & (B)) + ((A) << 24) );
					up = u;
					imagedata[curline] = p;
					curline ++;
					prevline ++;
					in_pos += step;
					--w;
				} while(w>0);
				break;
			case (1<<1)+1:
				do {
					int A  = ((in[in_pos]>>>24)&0xff);
					int IR = ((in[in_pos]>>>16)&0xff);
					int IG = ((in[in_pos]>>>8 )&0xff);
					int IB = ((in[in_pos]     )&0xff);
					B = IB+IG; G = IG; R = IR+IG;
					int u = prevdata[prevline];
					p = avg(p, u, up,
						(0xff0000 & ((R)<<16)) + (0xff00 & ((G)<<8)) + (0xff & (B)) + ((A) << 24) );
					up = u;
					imagedata[curline] = p;
					curline ++;
					prevline ++;
					in_pos += step;
					w--;
				} while(w>0);
				break;

			// TVP_TLG6_DO_CHROMA_DECODE( 2, IB, IG+IB, IR+IB+IG);
			case 2 << 1:
				do {
					int A  = ((in[in_pos]>>>24)&0xff);
					int IR = ((in[in_pos]>>>16)&0xff);
					int IG = ((in[in_pos]>>>8 )&0xff);
					int IB = ((in[in_pos]     )&0xff);
					B = IB; G = IG+IB; R = IR+IB+IG;
					int u = prevdata[prevline];
					p = med(p, u, up, (0xff0000 & ((R)<<16)) + (0xff00 & ((G)<<8)) + (0xff & (B)) + ((A) << 24) );
					up = u;
					imagedata[curline] = p;
					curline ++;
					prevline ++;
					in_pos += step;
					--w;
				} while(w>0);
				break;
			case (2<<1)+1:
				do {
					int A  = ((in[in_pos]>>>24)&0xff);
					int IR = ((in[in_pos]>>>16)&0xff);
					int IG = ((in[in_pos]>>>8 )&0xff);
					int IB = ((in[in_pos]     )&0xff);
					B = IB; G = IG+IB; R = IR+IB+IG;
					int u = prevdata[prevline];
					p = avg(p, u, up,
						(0xff0000 & ((R)<<16)) + (0xff00 & ((G)<<8)) + (0xff & (B)) + ((A) << 24) );
					up = u;
					imagedata[curline] = p;
					curline ++;
					prevline ++;
					in_pos += step;
					w--;
				} while(w>0);
				break;

			//TVP_TLG6_DO_CHROMA_DECODE( 3, IB+IR+IG, IG+IR, IR);
			case 3 << 1:
				do {
					int A  = ((in[in_pos]>>>24)&0xff);
					int IR = ((in[in_pos]>>>16)&0xff);
					int IG = ((in[in_pos]>>>8 )&0xff);
					int IB = ((in[in_pos]     )&0xff);
					B = IB+IR+IG; G = IG+IR; R = IR;
					int u = prevdata[prevline];
					p = med(p, u, up, (0xff0000 & ((R)<<16)) + (0xff00 & ((G)<<8)) + (0xff & (B)) + ((A) << 24) );
					up = u;
					imagedata[curline] = p;
					curline ++;
					prevline ++;
					in_pos += step;
					--w;
				} while(w>0);
				break;
			case (3<<1)+1:
				do {
					int A  = ((in[in_pos]>>>24)&0xff);
					int IR = ((in[in_pos]>>>16)&0xff);
					int IG = ((in[in_pos]>>>8 )&0xff);
					int IB = ((in[in_pos]     )&0xff);
					B = IB+IR+IG; G = IG+IR; R = IR;
					int u = prevdata[prevline];
					p = avg(p, u, up,
						(0xff0000 & ((R)<<16)) + (0xff00 & ((G)<<8)) + (0xff & (B)) + ((A) << 24) );
					up = u;
					imagedata[curline] = p;
					curline ++;
					prevline ++;
					in_pos += step;
					w--;
				} while(w>0);
				break;

			//TVP_TLG6_DO_CHROMA_DECODE( 4, IB+IR, IG+IB+IR, IR+IB+IR+IG);
			case 4 << 1:
				do {
					int A  = ((in[in_pos]>>>24)&0xff);
					int IR = ((in[in_pos]>>>16)&0xff);
					int IG = ((in[in_pos]>>>8 )&0xff);
					int IB = ((in[in_pos]     )&0xff);
					B = IB+IR; G = IG+IB+IR; R = IR+IB+IR+IG;
					int u = prevdata[prevline];
					p = med(p, u, up, (0xff0000 & ((R)<<16)) + (0xff00 & ((G)<<8)) + (0xff & (B)) + ((A) << 24) );
					up = u;
					imagedata[curline] = p;
					curline ++;
					prevline ++;
					in_pos += step;
					--w;
				} while(w>0);
				break;
			case (4<<1)+1:
				do {
					int A  = ((in[in_pos]>>>24)&0xff);
					int IR = ((in[in_pos]>>>16)&0xff);
					int IG = ((in[in_pos]>>>8 )&0xff);
					int IB = ((in[in_pos]     )&0xff);
					B = IB+IR; G = IG+IB+IR; R = IR+IB+IR+IG;
					int u = prevdata[prevline];
					p = avg(p, u, up,
						(0xff0000 & ((R)<<16)) + (0xff00 & ((G)<<8)) + (0xff & (B)) + ((A) << 24) );
					up = u;
					imagedata[curline] = p;
					curline ++;
					prevline ++;
					in_pos += step;
					w--;
				} while(w>0);
				break;

			//TVP_TLG6_DO_CHROMA_DECODE( 5, IB+IR, IG+IB+IR, IR);
			case 5 << 1:
				do {
					int A  = ((in[in_pos]>>>24)&0xff);
					int IR = ((in[in_pos]>>>16)&0xff);
					int IG = ((in[in_pos]>>>8 )&0xff);
					int IB = ((in[in_pos]     )&0xff);
					B = IB+IR; G = IG+IB+IR; R = IR;
					int u = prevdata[prevline];
					p = med(p, u, up, (0xff0000 & ((R)<<16)) + (0xff00 & ((G)<<8)) + (0xff & (B)) + ((A) << 24) );
					up = u;
					imagedata[curline] = p;
					curline ++;
					prevline ++;
					in_pos += step;
					--w;
				} while(w>0);
				break;
			case (5<<1)+1:
				do {
					int A  = ((in[in_pos]>>>24)&0xff);
					int IR = ((in[in_pos]>>>16)&0xff);
					int IG = ((in[in_pos]>>>8 )&0xff);
					int IB = ((in[in_pos]     )&0xff);
					B = IB+IR; G = IG+IB+IR; R = IR;
					int u = prevdata[prevline];
					p = avg(p, u, up,
						(0xff0000 & ((R)<<16)) + (0xff00 & ((G)<<8)) + (0xff & (B)) + ((A) << 24) );
					up = u;
					imagedata[curline] = p;
					curline ++;
					prevline ++;
					in_pos += step;
					w--;
				} while(w>0);
				break;

			//TVP_TLG6_DO_CHROMA_DECODE( 6, IB+IG, IG, IR);
			case 6 << 1:
				do {
					int A  = ((in[in_pos]>>>24)&0xff);
					int IR = ((in[in_pos]>>>16)&0xff);
					int IG = ((in[in_pos]>>>8 )&0xff);
					int IB = ((in[in_pos]     )&0xff);
					B = IB+IG; G = IG; R = IR;
					int u = prevdata[prevline];
					p = med(p, u, up, (0xff0000 & ((R)<<16)) + (0xff00 & ((G)<<8)) + (0xff & (B)) + ((A) << 24) );
					up = u;
					imagedata[curline] = p;
					curline ++;
					prevline ++;
					in_pos += step;
					--w;
				} while(w>0);
				break;
			case (6<<1)+1:
				do {
					int A  = ((in[in_pos]>>>24)&0xff);
					int IR = ((in[in_pos]>>>16)&0xff);
					int IG = ((in[in_pos]>>>8 )&0xff);
					int IB = ((in[in_pos]     )&0xff);
					B = IB+IG; G = IG; R = IR;
					int u = prevdata[prevline];
					p = avg(p, u, up,
						(0xff0000 & ((R)<<16)) + (0xff00 & ((G)<<8)) + (0xff & (B)) + ((A) << 24) );
					up = u;
					imagedata[curline] = p;
					curline ++;
					prevline ++;
					in_pos += step;
					w--;
				} while(w>0);
				break;

			//TVP_TLG6_DO_CHROMA_DECODE( 7, IB, IG+IB, IR);
			case 7 << 1:
				do {
					int A  = ((in[in_pos]>>>24)&0xff);
					int IR = ((in[in_pos]>>>16)&0xff);
					int IG = ((in[in_pos]>>>8 )&0xff);
					int IB = ((in[in_pos]     )&0xff);
					B = IB; G = IG+IB; R = IR;
					int u = prevdata[prevline];
					p = med(p, u, up, (0xff0000 & ((R)<<16)) + (0xff00 & ((G)<<8)) + (0xff & (B)) + ((A) << 24) );
					up = u;
					imagedata[curline] = p;
					curline ++;
					prevline ++;
					in_pos += step;
					--w;
				} while(w>0);
				break;
			case (7<<1)+1:
				do {
					int A  = ((in[in_pos]>>>24)&0xff);
					int IR = ((in[in_pos]>>>16)&0xff);
					int IG = ((in[in_pos]>>>8 )&0xff);
					int IB = ((in[in_pos]     )&0xff);
					B = IB; G = IG+IB; R = IR;
					int u = prevdata[prevline];
					p = avg(p, u, up,
						(0xff0000 & ((R)<<16)) + (0xff00 & ((G)<<8)) + (0xff & (B)) + ((A) << 24) );
					up = u;
					imagedata[curline] = p;
					curline ++;
					prevline ++;
					in_pos += step;
					w--;
				} while(w>0);
				break;

			//TVP_TLG6_DO_CHROMA_DECODE( 8, IB, IG, IR+IG);
			case 8 << 1:
				do {
					int A  = ((in[in_pos]>>>24)&0xff);
					int IR = ((in[in_pos]>>>16)&0xff);
					int IG = ((in[in_pos]>>>8 )&0xff);
					int IB = ((in[in_pos]     )&0xff);
					B = IB; G = IG; R = IR+IG;
					int u = prevdata[prevline];
					p = med(p, u, up, (0xff0000 & ((R)<<16)) + (0xff00 & ((G)<<8)) + (0xff & (B)) + ((A) << 24) );
					up = u;
					imagedata[curline] = p;
					curline ++;
					prevline ++;
					in_pos += step;
					--w;
				} while(w>0);
				break;
			case (8<<1)+1:
				do {
					int A  = ((in[in_pos]>>>24)&0xff);
					int IR = ((in[in_pos]>>>16)&0xff);
					int IG = ((in[in_pos]>>>8 )&0xff);
					int IB = ((in[in_pos]     )&0xff);
					B = IB; G = IG; R = IR+IG;
					int u = prevdata[prevline];
					p = avg(p, u, up,
						(0xff0000 & ((R)<<16)) + (0xff00 & ((G)<<8)) + (0xff & (B)) + ((A) << 24) );
					up = u;
					imagedata[curline] = p;
					curline ++;
					prevline ++;
					in_pos += step;
					w--;
				} while(w>0);
				break;

			//TVP_TLG6_DO_CHROMA_DECODE( 9, IB+IG+IR+IB, IG+IR+IB, IR+IB);
			case 9 << 1:
				do {
					int A  = ((in[in_pos]>>>24)&0xff);
					int IR = ((in[in_pos]>>>16)&0xff);
					int IG = ((in[in_pos]>>>8 )&0xff);
					int IB = ((in[in_pos]     )&0xff);
					B = IB+IG+IR+IB; G = IG+IR+IB; R = IR+IB;
					int u = prevdata[prevline];
					p = med(p, u, up, (0xff0000 & ((R)<<16)) + (0xff00 & ((G)<<8)) + (0xff & (B)) + ((A) << 24) );
					up = u;
					imagedata[curline] = p;
					curline ++;
					prevline ++;
					in_pos += step;
					--w;
				} while(w>0);
				break;
			case (9<<1)+1:
				do {
					int A  = ((in[in_pos]>>>24)&0xff);
					int IR = ((in[in_pos]>>>16)&0xff);
					int IG = ((in[in_pos]>>>8 )&0xff);
					int IB = ((in[in_pos]     )&0xff);
					B = IB+IG+IR+IB; G = IG+IR+IB; R = IR+IB;
					int u = prevdata[prevline];
					p = avg(p, u, up,
						(0xff0000 & ((R)<<16)) + (0xff00 & ((G)<<8)) + (0xff & (B)) + ((A) << 24) );
					up = u;
					imagedata[curline] = p;
					curline ++;
					prevline ++;
					in_pos += step;
					w--;
				} while(w>0);
				break;

			//TVP_TLG6_DO_CHROMA_DECODE(10, IB+IR, IG+IR, IR);
			case 10 << 1:
				do {
					int A  = ((in[in_pos]>>>24)&0xff);
					int IR = ((in[in_pos]>>>16)&0xff);
					int IG = ((in[in_pos]>>>8 )&0xff);
					int IB = ((in[in_pos]     )&0xff);
					B = IB+IR; G = IG+IR; R = IR;
					int u = prevdata[prevline];
					p = med(p, u, up, (0xff0000 & ((R)<<16)) + (0xff00 & ((G)<<8)) + (0xff & (B)) + ((A) << 24) );
					up = u;
					imagedata[curline] = p;
					curline ++;
					prevline ++;
					in_pos += step;
					--w;
				} while(w>0);
				break;
			case (10<<1)+1:
				do {
					int A  = ((in[in_pos]>>>24)&0xff);
					int IR = ((in[in_pos]>>>16)&0xff);
					int IG = ((in[in_pos]>>>8 )&0xff);
					int IB = ((in[in_pos]     )&0xff);
					B = IB+IR; G = IG+IR; R = IR;
					int u = prevdata[prevline];
					p = avg(p, u, up,
						(0xff0000 & ((R)<<16)) + (0xff00 & ((G)<<8)) + (0xff & (B)) + ((A) << 24) );
					up = u;
					imagedata[curline] = p;
					curline ++;
					prevline ++;
					in_pos += step;
					w--;
				} while(w>0);
				break;

			//TVP_TLG6_DO_CHROMA_DECODE(11, IB, IG+IB, IR+IB);
			case 11 << 1:
				do {
					int A  = ((in[in_pos]>>>24)&0xff);
					int IR = ((in[in_pos]>>>16)&0xff);
					int IG = ((in[in_pos]>>>8 )&0xff);
					int IB = ((in[in_pos]     )&0xff);
					B = IB; G = IG+IB; R = IR+IB;
					int u = prevdata[prevline];
					p = med(p, u, up, (0xff0000 & ((R)<<16)) + (0xff00 & ((G)<<8)) + (0xff & (B)) + ((A) << 24) );
					up = u;
					imagedata[curline] = p;
					curline ++;
					prevline ++;
					in_pos += step;
					--w;
				} while(w>0);
				break;
			case (11<<1)+1:
				do {
					int A  = ((in[in_pos]>>>24)&0xff);
					int IR = ((in[in_pos]>>>16)&0xff);
					int IG = ((in[in_pos]>>>8 )&0xff);
					int IB = ((in[in_pos]     )&0xff);
					B = IB; G = IG+IB; R = IR+IB;
					int u = prevdata[prevline];
					p = avg(p, u, up,
						(0xff0000 & ((R)<<16)) + (0xff00 & ((G)<<8)) + (0xff & (B)) + ((A) << 24) );
					up = u;
					imagedata[curline] = p;
					curline ++;
					prevline ++;
					in_pos += step;
					w--;
				} while(w>0);
				break;

			//TVP_TLG6_DO_CHROMA_DECODE(12, IB, IG+IR+IB, IR+IB);
			case 12 << 1:
				do {
					int A  = ((in[in_pos]>>>24)&0xff);
					int IR = ((in[in_pos]>>>16)&0xff);
					int IG = ((in[in_pos]>>>8 )&0xff);
					int IB = ((in[in_pos]     )&0xff);
					B = IB; G = IG+IR+IB; R = IR+IB;
					int u = prevdata[prevline];
					p = med(p, u, up, (0xff0000 & ((R)<<16)) + (0xff00 & ((G)<<8)) + (0xff & (B)) + ((A) << 24) );
					up = u;
					imagedata[curline] = p;
					curline ++;
					prevline ++;
					in_pos += step;
					--w;
				} while(w>0);
				break;
			case (12<<1)+1:
				do {
					int A  = ((in[in_pos]>>>24)&0xff);
					int IR = ((in[in_pos]>>>16)&0xff);
					int IG = ((in[in_pos]>>>8 )&0xff);
					int IB = ((in[in_pos]     )&0xff);
					B = IB; G = IG+IR+IB; R = IR+IB;
					int u = prevdata[prevline];
					p = avg(p, u, up,
						(0xff0000 & ((R)<<16)) + (0xff00 & ((G)<<8)) + (0xff & (B)) + ((A) << 24) );
					up = u;
					imagedata[curline] = p;
					curline ++;
					prevline ++;
					in_pos += step;
					w--;
				} while(w>0);
				break;

			//TVP_TLG6_DO_CHROMA_DECODE(13, IB+IG, IG+IR+IB+IG, IR+IB+IG);
			case 13 << 1:
				do {
					int A  = ((in[in_pos]>>>24)&0xff);
					int IR = ((in[in_pos]>>>16)&0xff);
					int IG = ((in[in_pos]>>>8 )&0xff);
					int IB = ((in[in_pos]     )&0xff);
					B = IB+IG; G = IG+IR+IB+IG; R = IR+IB+IG;
					int u = prevdata[prevline];
					p = med(p, u, up, (0xff0000 & ((R)<<16)) + (0xff00 & ((G)<<8)) + (0xff & (B)) + ((A) << 24) );
					up = u;
					imagedata[curline] = p;
					curline ++;
					prevline ++;
					in_pos += step;
					--w;
				} while(w>0);
				break;
			case (13<<1)+1:
				do {
					int A  = ((in[in_pos]>>>24)&0xff);
					int IR = ((in[in_pos]>>>16)&0xff);
					int IG = ((in[in_pos]>>>8 )&0xff);
					int IB = ((in[in_pos]     )&0xff);
					B = IB+IG; G = IG+IR+IB+IG; R = IR+IB+IG;
					int u = prevdata[prevline];
					p = avg(p, u, up,
						(0xff0000 & ((R)<<16)) + (0xff00 & ((G)<<8)) + (0xff & (B)) + ((A) << 24) );
					up = u;
					imagedata[curline] = p;
					curline ++;
					prevline ++;
					in_pos += step;
					w--;
				} while(w>0);
				break;

			//TVP_TLG6_DO_CHROMA_DECODE(14, IB+IG+IR, IG+IR, IR+IB+IG+IR);
			case 14 << 1:
				do {
					int A  = ((in[in_pos]>>>24)&0xff);
					int IR = ((in[in_pos]>>>16)&0xff);
					int IG = ((in[in_pos]>>>8 )&0xff);
					int IB = ((in[in_pos]     )&0xff);
					B = IB+IG+IR; G = IG+IR; R = IR+IB+IG+IR;
					int u = prevdata[prevline];
					p = med(p, u, up, (0xff0000 & ((R)<<16)) + (0xff00 & ((G)<<8)) + (0xff & (B)) + ((A) << 24) );
					up = u;
					imagedata[curline] = p;
					curline ++;
					prevline ++;
					in_pos += step;
					--w;
				} while(w>0);
				break;
			case (14<<1)+1:
				do {
					int A  = ((in[in_pos]>>>24)&0xff);
					int IR = ((in[in_pos]>>>16)&0xff);
					int IG = ((in[in_pos]>>>8 )&0xff);
					int IB = ((in[in_pos]     )&0xff);
					B = IB+IG+IR; G = IG+IR; R = IR+IB+IG+IR;
					int u = prevdata[prevline];
					p = avg(p, u, up,
						(0xff0000 & ((R)<<16)) + (0xff00 & ((G)<<8)) + (0xff & (B)) + ((A) << 24) );
					up = u;
					imagedata[curline] = p;
					curline ++;
					prevline ++;
					in_pos += step;
					w--;
				} while(w>0);
				break;

			//TVP_TLG6_DO_CHROMA_DECODE(15, IB, IG+(IB<<1), IR+(IB<<1));
			case 15 << 1:
				do {
					int A  = ((in[in_pos]>>>24)&0xff);
					int IR = ((in[in_pos]>>>16)&0xff);
					int IG = ((in[in_pos]>>>8 )&0xff);
					int IB = ((in[in_pos]     )&0xff);
					B = IB; G = IG+(IB<<1); R = IR+(IB<<1);
					int u = prevdata[prevline];
					p = med(p, u, up, (0xff0000 & ((R)<<16)) + (0xff00 & ((G)<<8)) + (0xff & (B)) + ((A) << 24) );
					up = u;
					imagedata[curline] = p;
					curline ++;
					prevline ++;
					in_pos += step;
					--w;
				} while(w>0);
				break;
			case (15<<1)+1:
				do {
					int A  = ((in[in_pos]>>>24)&0xff);
					int IR = ((in[in_pos]>>>16)&0xff);
					int IG = ((in[in_pos]>>>8 )&0xff);
					int IB = ((in[in_pos]     )&0xff);
					B = IB; G = IG+(IB<<1); R = IR+(IB<<1);
					int u = prevdata[prevline];
					p = avg(p, u, up,
						(0xff0000 & ((R)<<16)) + (0xff00 & ((G)<<8)) + (0xff & (B)) + ((A) << 24) );
					up = u;
					imagedata[curline] = p;
					curline ++;
					prevline ++;
					in_pos += step;
					w--;
				} while(w>0);
				break;

			default: return;
			}
			if(step == 1)
				in_pos += skipblockbytes - ww;
			else
				in_pos += skipblockbytes + 1;
			if( (i&1) != 0 ) in_pos -= oddskip * ww;
		}
	}
	private static final void TLG6DecodeLine( int[] imagedata, int prevline, int[] zeroline, int curline, int width, int block_count, byte[] filtertypes, int ft_pos, int skipblockbytes, int[] in, int in_pos, int initialp, int oddskip, int dir ) {
		TLG6DecodeLineGeneric( imagedata, prevline, zeroline, curline, width, 0, block_count,
			filtertypes, ft_pos, skipblockbytes, in, in_pos, initialp, oddskip, dir);
	}
}
