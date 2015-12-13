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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import jp.kirikiri.tjs2.BinaryStream;
import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tvp2.msg.Message;

public class PNGLoader {
	static private final long PNG_SIGNATURE = 0x89504E470D0A1A0AL;
	static private final int IHDR_CHUNK = 0x49484452; // IHDR
	static private final int IHDR_CHUNK_SIZE = 13;
	static private final int CRC_SIZE = 4;
	static private final int PLTE_CHUNK = 0x504C5445;
	static private final int IDAT_CHUNK = 0x49444154;
	static private final int IEND_CHUNK = 0x49454E44;

	static private final int COLOR_TYPE_GRAYSCALE = 0;
	static private final int COLOR_TYPE_RGB = 2;
	static private final int COLOR_TYPE_PALETTE = 3;
	static private final int COLOR_TYPE_GRAYSCALE_ALPHA = 4;
	static private final int COLOR_TYPE_RGB_ALPHA = 6;

	static private final int INTERLACE_TYPE_NON = 0;
	static private final int INTERLACE_TYPE_ADAM7 = 1;

	static private final int FILTER_TYPE_NON = 0;
	static private final int FILTER_TYPE_SUB = 1;
	static private final int FILTER_TYPE_UP = 2;
	static private final int FILTER_TYPE_AVERAGE = 3;
	static private final int FILTER_TYPE_PAETH = 4;
	private static Inflater Decompresser;
	public static void initialize() {
		Decompresser = null;
	}
	public static void finalizeApplication() {
		Decompresser = null;
	}

	public static BufferedImage loadPNG( BinaryStream src, int mode ) throws TJSException {
		if( Decompresser == null ) Decompresser = new Inflater();

		byte[] mark = new byte[8];
		src.read(mark);
		ByteBuffer buff = ByteBuffer.wrap(mark);
		buff.order(ByteOrder.BIG_ENDIAN);
		buff.position(0);
		long sig = buff.getLong();
		if( sig != PNG_SIGNATURE )
			Message.throwExceptionMessage( "It's not PNG file." );

		src.read(mark);
		buff.position(0);
		int size = buff.getInt();
		if( size != IHDR_CHUNK_SIZE )
			Message.throwExceptionMessage( "Invalidate IHDR chunk size." );

		int chunk_header = buff.getInt();
		if( chunk_header != IHDR_CHUNK )
			Message.throwExceptionMessage( "Invalidate PNG file." );

		byte[] ihdr = new byte[IHDR_CHUNK_SIZE + CRC_SIZE];
		src.read(ihdr);
		ByteBuffer chunkbuff = ByteBuffer.wrap(ihdr);
		chunkbuff.order(ByteOrder.BIG_ENDIAN);
		chunkbuff.position(0);
		int width = chunkbuff.getInt();
		int height = chunkbuff.getInt();
		byte bitdepth = chunkbuff.get();
		byte colortype = chunkbuff.get();
		byte compresstype = chunkbuff.get();
		byte filtertype = chunkbuff.get();
		byte interlacetype = chunkbuff.get();
		// ignore CRC

		if( compresstype != 0 || filtertype != 0 || (interlacetype!=0&&interlacetype!=1) ) {
			Message.throwExceptionMessage( "Invalidate Image format." );
		}
		if( interlacetype == 1 ) {
			Message.throwExceptionMessage( "Not supported interlace Adam7." );
		}
		if( colortype != COLOR_TYPE_PALETTE && colortype != COLOR_TYPE_GRAYSCALE) {
			Message.throwExceptionMessage( "Not supported color type." );
		}
		byte[] image = null;
		int[] palette = null;
		final long filesize = src.getSize();
		L: while( src.getPosition() <= filesize ) {
			// read header
			src.read(mark);
			buff.position(0);
			size = buff.getInt();
			chunk_header = buff.getInt();
			switch( chunk_header ) {
			case PLTE_CHUNK:
				palette = readPalette(src,size);
				break;
			case IDAT_CHUNK:
				image = readImage(src,palette,size,width,height,bitdepth,colortype);
				src.setPosition(src.getPosition()+size+CRC_SIZE); // skip chunk
				break;
			case IEND_CHUNK:
				break L;
			default: {
				char c0 = (char) ((chunk_header >>> 24)&0xff);
				char c1 = (char) ((chunk_header >>> 16)&0xff);
				char c2 = (char) ((chunk_header >>> 8)&0xff);
				char c3 = (char) ((chunk_header)&0xff);
				System.out.print( "Ignore Chunk : "+c0+c1+c2+c3 + "\n");
				src.setPosition(src.getPosition()+size+CRC_SIZE); // skip chunk
				break;
			}
			}
		}
		if( image == null )
			Message.throwExceptionMessage( "Internal error." );

		BufferedImage result = new BufferedImage( width, height, BufferedImage.TYPE_INT_ARGB );
		int[] imagedata = null;
		WritableRaster srcRaster = result.getRaster();
		DataBuffer dataBuff = srcRaster.getDataBuffer();
		final int type = dataBuff.getDataType();
		if( type == DataBuffer.TYPE_INT ) {
			DataBufferInt srcBuff = (DataBufferInt)dataBuff;
			imagedata = srcBuff.getData();
		} else {
			Message.throwExceptionMessage( "Internal error." );
		}
		final int stride = (((width*bitdepth)+7)>>>3);
		int p = 0;
		int o = 0;
		if( colortype == COLOR_TYPE_PALETTE ) {
			if( palette == null ) Message.throwExceptionMessage( "Internal error." );
			if( bitdepth == 8) {
				for( int y = 0; y < height; y++ ) {
					for( int x = 0; x < width; x++ ) {
						int idx = image[p]&0xff; p++;
						imagedata[o] = palette[idx]; o++;
					}
				}
			} else if( bitdepth == 4 ) {
				for( int y = 0; y < height; y++ ) {
					for( int x = 0; x < stride; x++ ) {
						int idx = image[p]&0xff; p++;
						int h = idx >>> 4;
						int l = idx & 0xf;
						imagedata[o] = palette[h]; o++;
						imagedata[o] = palette[l]; o++;
					}
				}
			} else if( bitdepth == 2 ) {
				for( int y = 0; y < height; y++ ) {
					for( int x = 0; x < stride; x++ ) {
						int idx = image[p]&0xff; p++;
						int i0 = idx >>> 6;
						int i1 = (idx >>> 4) & 0x3;
						int i2 = (idx >>> 2) & 0x3;
						int i3 = idx & 0x3;
						imagedata[o] = palette[i0]; o++;
						imagedata[o] = palette[i1]; o++;
						imagedata[o] = palette[i2]; o++;
						imagedata[o] = palette[i3]; o++;
					}
				}
			} else if( bitdepth == 1 ) {
				for( int y = 0; y < height; y++ ) {
					for( int x = 0; x < stride; x++ ) {
						int idx = image[p]&0xff; p++;
						for( int b = 7; b >= 0; b-- ) {
							int b0 = (idx >>> b) & 0x1;
							imagedata[o] = palette[b0]; o++;
						}
					}
				}
			}
		} else if( colortype == COLOR_TYPE_GRAYSCALE ) {
			if( bitdepth == 8) {
				for( int y = 0; y < height; y++ ) {
					for( int x = 0; x < width; x++ ) {
						int col = image[p]&0xff; p++;
						imagedata[o] = 0xff000000 | (col<<16) | (col<<8) | col; o++;
					}
				}
			} else if( bitdepth == 4 ) {
				for( int y = 0; y < height; y++ ) {
					for( int x = 0; x < stride; x++ ) {
						int idx = image[p]&0xff; p++;
						int col = idx >>> 4;
						imagedata[o] = 0xff000000 | (col<<16) | (col<<8) | col; o++;
						col  = idx & 0xf;
						imagedata[o] = 0xff000000 | (col<<16) | (col<<8) | col; o++;
					}
				}
			} else if( bitdepth == 2 ) {
				for( int y = 0; y < height; y++ ) {
					for( int x = 0; x < stride; x++ ) {
						int idx = image[p]&0xff; p++;
						int col = idx >>> 6;
						imagedata[o] = 0xff000000 | (col<<16) | (col<<8) | col; o++;
						col = (idx >>> 4) & 0x3;
						imagedata[o] = 0xff000000 | (col<<16) | (col<<8) | col; o++;
						col = (idx >>> 2) & 0x3;
						imagedata[o] = 0xff000000 | (col<<16) | (col<<8) | col; o++;
						col = idx & 0x3;
						imagedata[o] = 0xff000000 | (col<<16) | (col<<8) | col; o++;
					}
				}
			} else if( bitdepth == 1 ) {
				for( int y = 0; y < height; y++ ) {
					for( int x = 0; x < stride; x++ ) {
						int idx = image[p]&0xff; p++;
						for( int b = 7; b >= 0; b-- ) {
							int col = (idx >>> b) & 0x1;
							imagedata[o] = 0xff000000 | (col<<16) | (col<<8) | col; o++;
						}
					}
				}
			}
		}
		return result;
	}
	private static byte[] readImage(BinaryStream src, int[] palette, int size, int width, int height, byte bitdepth, byte colortype) throws TJSException {
		// bitdepth が 8 以外の時、バイト境界にそろえる
		final int stride = (((width*bitdepth)+7)>>>3);
		int imagebuffsize = stride * height + height;
		byte[] idat = new byte[size];
		src.read(idat);
		src.setPosition(src.getPosition()+CRC_SIZE); // skip CRC

		byte[] buffer = new byte[imagebuffsize];
		int len = decompressData( buffer, idat );
		if( len < stride ) Message.throwExceptionMessage( "Invalidate IDAT size ." );

		int bpp = bitdepth >>> 3;
		if( bpp <= 0 ) bpp = 1;

		byte[] output = new byte[stride*height];
		// これじゃ IDAT が複数に分割されている時に対応出来ないか
		int p = 0;
		int o = 0;
		int filtertype = buffer[p]; p++;
		int prev;
		switch( filtertype ) {
		case FILTER_TYPE_NON:
		case FILTER_TYPE_UP: // 1ライン目が UP の時は、NON と等価。
			for( int x = 0; x < stride; x++ ) {
				output[o] = buffer[p]; p++; o++;
			}
			break;
		case FILTER_TYPE_SUB:
		case FILTER_TYPE_PAETH: // 上のラインが0の時は、SUB と同じ
			prev = 0;
			for( int x = 0; x < stride; x++ ) {
				int idx = buffer[p]&0xff; p++;
				idx = (idx + prev)&0xff;
				prev = idx;
				output[o] = (byte) idx; o++;
			}
			break;
		case FILTER_TYPE_AVERAGE: // 上のラインは 0 なので、加算しない
			prev = 0;
			for( int x = 0; x < stride; x++ ) {
				int idx = buffer[p]&0xff; p++;
				idx = ((idx + prev)>>>1)&0xff;
				prev = idx;
				output[o] = (byte) idx; o++;
			}
			break;
		default:
			Message.throwExceptionMessage( "Unknown scanline filter type." );
		}

		int u = 0; // 上のライン位置
		for( int y = 1; y < height; y++ ) {
			filtertype = buffer[p]; p++;
			switch( filtertype ) {
			case FILTER_TYPE_NON: // そのまま
				for( int x = 0; x < stride; x++ ) {
					output[o] = buffer[p]; p++; o++;
				}
				u += stride;
				break;
			case FILTER_TYPE_SUB: { // 左との差分
				int lett = 0;
				for( int x = 0; x < stride; x++ ) {
					int idx = buffer[p]&0xff; p++;
					idx = (idx + lett)&0xff;
					lett = idx;
					output[o] = (byte) idx; o++;
				}
				u += stride;
				break;
			}
			case FILTER_TYPE_UP:
				for( int x = 0; x < stride; x++ ) {
					int idx = buffer[p]&0xff; p++;
					int up = output[u]&0xff; u++;
					idx = (idx + up)&0xff;
					output[o] = (byte) idx; o++;
				}
				break;
			case FILTER_TYPE_AVERAGE: {
				int left = 0;
				for( int x = 0; x < stride; x++ ) {
					int idx = buffer[p]&0xff; p++;
					int up = output[u]&0xff; u++;
					idx = (idx + ((left+up)>>>1)) & 0xff;
					left = idx;
					output[o] = (byte) idx; o++;
				}
				break;
			}
			case FILTER_TYPE_PAETH: {
				int left = 0;
				int upleft = 0;
				for( int x = 0; x < stride; x++ ) {
					int idx = buffer[p]&0xff; p++;
					int up = output[u]&0xff; u++;
					int pp = left + up - upleft;
					int pa = Math.abs(pp-left);
					int pb = Math.abs(pp-up);
					int pc = Math.abs(pp-upleft);
					int v;
					if( pa <= pb && pa <= pc ) {
						v = left;
					} else if( pb <= pc ) {
						v = up;
					} else {
						v = upleft;
					}
					upleft = up;
					idx = (idx + v) & 0xff;
					left = idx;
					output[o] = (byte) idx; o++;
				}
				break;
			}
			default:
				Message.throwExceptionMessage( "Unknown scanline filter type." );
			}
		}
		return output;
	}
	private static int decompressData( byte[] output, byte[] indata) throws TJSException {
		int destlen = 0;
		try {
			Decompresser.setInput(indata);
			destlen = Decompresser.inflate(output);
			Decompresser.reset();
		} catch (DataFormatException e) {
			Message.throwExceptionMessage(Message.UncompressionFailed);
		} finally {
			indata = null;
		}
		return destlen;
	}
	private static int[] readPalette( BinaryStream src, final int size ) throws TJSException {
		if( (size % 3) != 0 )
			Message.throwExceptionMessage( "Invalidate PLTE chunk size." );
		final byte[] pal = new byte[size+CRC_SIZE];
		src.read(pal);
		final int len = size / 3;
		final int[] result = new int[len];
		int p = 0;
		for( int i = 0; i < len; i++ ) {
			final int color = 0xff000000 | ((pal[p]&0xff) << 16) | ((pal[p+1]&0xff) << 8) | (pal[p+2]&0xff);
			result[i] = color;
			p += 3;
		}
		return result;
	}
}
