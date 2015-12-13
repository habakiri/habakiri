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

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import javax.imageio.ImageIO;

import jp.kirikiri.tjs2.BinaryStream;
import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tvp2.msg.Message;
import jp.kirikiri.tvp2.visual.GraphicsLoader;


public class NativeImageLoader {

	static public BufferedImage loadImage( InputStream stream ) {
		try {
			BufferedImage img = null;
			img = ImageIO.read(stream);
			return img;
		} catch (IOException e) {
			return null;
		}
	}

	static public NativeImageBuffer loadImage( BinaryStream stream, final String ext, HashMap<String,String> metainfo, int keyidx, int mode ) throws TJSException {
		BufferedImage img = null;
		if( ".tlg".equalsIgnoreCase(ext) || ".tlg5".equalsIgnoreCase(ext ) || ".tlg6".equalsIgnoreCase(ext ) ) {
			img = TLGLoader.loadTLG(stream, mode, metainfo );
		}
		/** test */
		/*
		if( ".png".equalsIgnoreCase(ext) ) {
			img = PNGLoader.loadPNG(stream, mode);
			return new NativeImageBuffer(img);
			// stream.setPosition(0);
		}
		*/
		/** test */
		if( img == null ) {
			try {
				img = ImageIO.read(stream.getInputStream());
			} catch (IOException e) {
			}
		}
		if( img == null ) return null;

		if( GraphicsLoader.glmNormal == mode ) {
			/*
			if( img.getType() != BufferedImage.TYPE_INT_ARGB_PRE ) {
				if( img.getType() == BufferedImage.TYPE_INT_ARGB ) {
					img.coerceData(true); // alpha 乗算
				} else {
					// 常に変換するようにしてみる、DataBuffer は int になるので、自前で処理するのは速くなる
					BufferedImage img2 = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB_PRE );
					Graphics2D g = img2.createGraphics();
					g.drawImage(img, 0, 0, null );
					g.dispose();
					img = img2;
				}
			}
			*/
			final int imgType = img.getType();
			if( imgType != BufferedImage.TYPE_INT_ARGB && imgType != BufferedImage.TYPE_INT_ARGB_PRE ) {
				int transparentColor = -1;
				if( imgType == BufferedImage.TYPE_BYTE_INDEXED && keyidx != -1 ) {
					ColorModel model = img.getColorModel();
					if( model instanceof IndexColorModel ) {
						IndexColorModel indexColor = (IndexColorModel)model;
						int indexSize = indexColor.getMapSize();
						int[] palette = new int[indexSize];
						indexColor.getRGBs(palette);
						if( keyidx < palette.length ) {
							transparentColor = palette[keyidx];
						}
					}
				}
				// 常に変換するようにしてみる、DataBuffer は int になるので、自前で処理するのは速くなる
				BufferedImage img2 = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB );
				Graphics2D g = img2.createGraphics();
				g.drawImage(img, 0, 0, null );
				g.dispose();
				img = img2;
				if( transparentColor != -1 ) {
					WritableRaster src = img.getRaster();
					DataBuffer buff = src.getDataBuffer();
					final int type = buff.getDataType();
					if( type == DataBuffer.TYPE_INT ) {
						DataBufferInt srcBuff = (DataBufferInt)buff;
						int[] s = srcBuff.getData();
						final int color = transparentColor & 0x00ffffff;
						final int count = s.length;
						for( int i = 0; i < count; i++ ) {
							int a = s[i] & 0x00ffffff;
							if( a != color ) a |= 0xff000000;
							s[i] = a;
						}
						s = null;
						srcBuff = null;
					}
					buff = null;
					src = null;
				}
			}
		} else if( mode == GraphicsLoader.glmPalettized ) {
			if( img.getType() != BufferedImage.TYPE_BYTE_INDEXED ) {
				Message.throwExceptionMessage( Message.ImageLoadError, "Unsupported color mode for palettized image.");
			}
		}
		return new NativeImageBuffer(img);
	}
}
