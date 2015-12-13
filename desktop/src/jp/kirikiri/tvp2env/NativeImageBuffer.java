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

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.LongBuffer;
import java.util.HashMap;

import javax.imageio.ImageIO;

import jp.kirikiri.tjs2.BinaryStream;
import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tvp2.TVP;
import jp.kirikiri.tvp2.base.Storage;
import jp.kirikiri.tvp2.msg.Message;
import jp.kirikiri.tvp2.utils.DebugClass;
import jp.kirikiri.tvp2.visual.AffineMatrix2D;
import jp.kirikiri.tvp2.visual.BaseBitmap;
import jp.kirikiri.tvp2.visual.CharacterData;
import jp.kirikiri.tvp2.visual.ComplexRect;
import jp.kirikiri.tvp2.visual.GammaAdjustTempData;
import jp.kirikiri.tvp2.visual.LayerNI;
import jp.kirikiri.tvp2.visual.PointD;
import jp.kirikiri.tvp2.visual.Rect;


public class NativeImageBuffer {
	private static String[] WRITABLE_FORMANT_NAMES;

	private static byte[][] Color252DitherPalette;
	private static byte[][][][] DitherTable_676;
	private static final byte Dither4x4[][] = {
		 {   0, 12,  2, 14   },
		 {   8,  4, 10,  6   },
		 {   3, 15,  1, 13   },
		 {  11,  7,  9,  5   }};

	public static void initialize() {
		CustomOperationComposite.initialize();
		Color252DitherPalette = null;
		DitherTable_676 = null;
	}

	private BufferedImage mImage;
	private int mWidth;
	private int mHeight;
	private HashMap<Character,GlyphVector> mGlyphHash;
	private java.awt.Font mChecheFont;
	private int mRefCount;

	public NativeImageBuffer( int w, int h ) {
		mWidth = w;
		mHeight = h;
		//mImage = new BufferedImage( w, h, BufferedImage.TYPE_4BYTE_ABGR );
		mImage = new BufferedImage( w, h, BufferedImage.TYPE_INT_ARGB );
		mRefCount = 1;
	}
	public NativeImageBuffer(int w, int h, int bpp) {
		mWidth = w;
		mHeight = h;
		//int type = BufferedImage.TYPE_4BYTE_ABGR;
		int type = BufferedImage.TYPE_INT_ARGB;
		if( bpp == 8 ) {
			type = BufferedImage.TYPE_BYTE_INDEXED;
		}
		mImage = new BufferedImage( w, h, type );
		mRefCount = 1;
	}
	public NativeImageBuffer(NativeImageBuffer src) {
		mWidth = src.mWidth;
		mHeight = src.mHeight;
		int type = src.mImage.getType();
		mImage = new BufferedImage( mWidth, mHeight, type );
		Graphics2D g = (Graphics2D)mImage.getGraphics();
		g.setComposite( AlphaComposite.Src );
		g.drawImage( src.mImage, 0, 0, null );
		g.dispose();
		mRefCount = 1;
	}
	public NativeImageBuffer( int w, int h, NativeImageBuffer src ) {
		mWidth = w;
		mHeight = h;
		int type = src.mImage.getType();
		mImage = new BufferedImage( mWidth, mHeight, type );
		Graphics2D g = (Graphics2D)mImage.getGraphics();
		g.setComposite( AlphaComposite.Src );
		g.drawImage( src.mImage, 0, 0, null );
		g.dispose();
		mRefCount = 1;
	}
	public NativeImageBuffer(BufferedImage img) {
		mWidth = img.getWidth();
		mHeight = img.getHeight();
		mImage = img;
		mRefCount = 1;
	}
	public final void addRef() { mRefCount++; }
	public void finalizeRelease() { if( mRefCount != 1 ) mRefCount--; } // 解放までは行わない(行えない)
	public final void release() { if( mRefCount != 1 ) mRefCount--; } // 解放までは行わない(行えない)
	public final boolean isIndependent() { return mRefCount == 1; }
	public final Image getImage() {
		/*
		if( mComponent != null ) {
			if( mImage == null ) {
				mImage = mComponent.createVolatileImage(mWidth, mHeight);
			} else {
				GraphicsConfiguration gc = mComponent.getGraphicsConfiguration();
				// VRAMバッファ用領域に変更があった場合は再度生成処理を実行する
				if( ((VolatileImage)mImage).validate(gc) == VolatileImage.IMAGE_INCOMPATIBLE ) {
					mImage = mComponent.createVolatileImage(mWidth, mHeight);
				}
			}
		}
		*/
		return mImage;
	}
	public final void setImage( BufferedImage img ) {
		mImage = img;
		mWidth = img.getWidth();
		mHeight = img.getHeight();
	}
	public final int getWidth() { return mImage.getWidth(); }
	public final int getHeight() { return mImage.getHeight(); }

	public final void recreate( int w, int h, boolean keepimage ) throws TJSException {
		//if( mComponent != null ) throw new TJSException("オフスクリーンバッファはリサイズできない");

		int imageType = mImage.getType();
		if( w != 0 && h != 0 ) {
			BufferedImage img = new BufferedImage( w, h, imageType );
			if( keepimage && mImage != null ) {
				Graphics dest = img.getGraphics();
				dest.drawImage(mImage , 0, 0, null );
			}
			mImage = null;
			mImage = img;
		} else {
			mImage = null;
		}
		mWidth = w;
		mHeight = h;
	}
	public final int getBPP() { return mImage.getColorModel().getPixelSize(); }
	public final void getScanLine( int l, int[] buff) {
		if( mImage instanceof BufferedImage ) {
			BufferedImage bi = (BufferedImage)mImage;
			final int w = bi.getWidth();
			bi.getRGB( 0, l, w, 1, buff, 0, w );
		}
		// 他の時は何も返さない
	}
	public final int[] getScanLine( int l ) {
		if( mImage instanceof BufferedImage ) {
			BufferedImage bi = (BufferedImage)mImage;
			final int w = bi.getWidth();
			return bi.getRGB( 0, l, w, 1, null, 0, w );
		}
		return null;
	}
	public final int[] getScanLineForWrite( int l ) {
		if( mImage instanceof BufferedImage ) {
			BufferedImage bi = (BufferedImage)mImage;
			WritableRaster wr = bi.getRaster();
			DataBufferInt bdi = (DataBufferInt) wr.getDataBuffer();
			return bdi.getData(); // TODO スキャンライン関係ない……
		}
		return null;
	}
	public final void fill(Rect rect, int value) {
		if( mImage.getType() == BufferedImage.TYPE_BYTE_INDEXED ) {
			final int w = mImage.getWidth();
			WritableRaster src = mImage.getRaster();
			DataBuffer buff = src.getDataBuffer();
			final int type = buff.getDataType();
			if( type == DataBuffer.TYPE_BYTE ) {
				DataBufferByte srcBuff = (DataBufferByte)buff;
				byte[] s = srcBuff.getData();
				int ystart = rect.top*w + rect.left;
				final int rw = rect.right-rect.left;
				final int rh = (rect.bottom-rect.top);
				byte color = (byte) (value&0xff);
				for( int y = 0; y < rh; y++ ) {
					int sp = ystart;
					for( int x = 0; x < rw; x++ ) {
						s[sp] = color;
						sp++;
					}
					ystart += w;
				}
			}
		} else {
			//if( (value & 0xff000000) == 0xff000000 ) {
				Graphics2D g = (Graphics2D)mImage.getGraphics();
				g.setComposite( AlphaComposite.Src );
				g.setColor( new Color( value, true) );
				g.fillRect( rect.left, rect.top, rect.width(), rect.height() );
				g.dispose();
			/*} else {
				final int w = mImage.getWidth();
				WritableRaster src = mImage.getRaster();
				DataBuffer buff = src.getDataBuffer();
				final int type = buff.getDataType();
				if( type == DataBuffer.TYPE_INT ) {
					DataBufferInt srcBuff = (DataBufferInt)buff;
					int[] s = srcBuff.getData();
					int ystart = rect.top*w + rect.left;
					final int rw = rect.right-rect.left;
					final int rh = (rect.bottom-rect.top);
					for( int y = 0; y < rh; y++ ) {
						int sp = ystart;
						for( int x = 0; x < rw; x++ ) {
							s[sp] = value;
							sp++;
						}
						ystart += w;
					}
				}
			}*/
		}
	}
	public final void fillColor(Rect rect, int color, int opa ) {
		Graphics2D g = (Graphics2D)mImage.getGraphics();
		//g.setComposite(createAlphaComposite( AlphaComposite.SRC, opa ));
		g.setComposite(createAlphaComposite( AlphaComposite.SRC_OVER, opa ));
		//g.setColor( new Color( (color&0xFFFFFF)|(opa<<24), true) );
		g.setColor( new Color( color, false) );
		g.fillRect( rect.left, rect.top, rect.width(), rect.height() );
		g.dispose();
	}

	public final boolean copyRect(int x, int y, NativeImageBuffer src, Rect refrect ) {
		Graphics2D g = (Graphics2D)mImage.getGraphics();
		g.setComposite( AlphaComposite.Src );
		boolean ret = g.drawImage( src.mImage, x, y, x+refrect.width(), y+refrect.height(),
				refrect.left, refrect.top, refrect.right, refrect.bottom, null );
		g.dispose();
		return ret;
	}
	public final boolean copyRect(int x, int y, NativeImageBuffer src, Rect refrect, int plane) {
		Graphics2D g = (Graphics2D)mImage.getGraphics();
		if( (BaseBitmap.BB_COPY_MAIN|BaseBitmap.BB_COPY_MASK) == plane ) {
			g.setComposite( AlphaComposite.Src );
		} else if( BaseBitmap.BB_COPY_MAIN == plane ) {
			g.setComposite(new CustomOperationComposite( CustomOperationComposite.copyMain, 255, false ));
		} else if( BaseBitmap.BB_COPY_MASK == plane ) {
			g.setComposite(new CustomOperationComposite( CustomOperationComposite.copyMask, 255, false ));
		}

		int h = refrect.height();
		int w = refrect.width();
		BufferedImage tmp;
		if( this == src && y >= refrect.top && (y - refrect.top) < h && x >= refrect.left && (x-refrect.left) < w ) {
			//int height = y - refrect.top;
			// コピー元とコピー先が同じ場合は、一端テンポラリへコピーする
			// テンポラリへ入れなくても、下から順にコピーしていけば大丈夫なようだが速度が気になる
			int type = src.mImage.getType();
			tmp = new BufferedImage( w, h, type );
			Graphics2D tg = (Graphics2D)tmp.getGraphics();
			tg.setComposite( AlphaComposite.Src );
			tg.drawImage( src.mImage, 0, 0, w, h, refrect.left, refrect.top, refrect.right, refrect.bottom, null );
			tg.dispose();
			refrect = new Rect(0,0,w,h);
		} else {
			tmp = src.mImage;
		}
		boolean ret = g.drawImage( tmp, x, y, x+refrect.width(), y+refrect.height(),
				refrect.left, refrect.top, refrect.right, refrect.bottom, null );
		g.dispose();
		return ret;
	}
	public final boolean copyRect(Rect dstrect, NativeImageBuffer src, Rect srcrect, int method, int opa, boolean hda ) {
		/*
		createAlphaComposite( AlphaComposite.CLEAR, opa );
		createAlphaComposite( AlphaComposite.DST, opa );
		createAlphaComposite( AlphaComposite.DST_ATOP, opa );
		createAlphaComposite( AlphaComposite.DST_IN, opa );
		createAlphaComposite( AlphaComposite.DST_OUT, opa );
		createAlphaComposite( AlphaComposite.DST_OVER, opa );
		createAlphaComposite( AlphaComposite.SRC_ATOP, opa );
		createAlphaComposite( AlphaComposite.SRC_IN, opa );
		createAlphaComposite( AlphaComposite.SRC_OUT, opa );
		createAlphaComposite( AlphaComposite.XOR, opa );
		*/

		Graphics2D g = (Graphics2D)mImage.getGraphics();
		g.setComposite(new CustomOperationComposite( method, opa, hda ));
		/*
		if( !hda ) {
			if( method == LayerNI.bmCopy ) {
				if( opa == 255 ) {
					g.setComposite( AlphaComposite.Src );
				} else {
					g.setComposite(createAlphaComposite( AlphaComposite.SRC, opa ));
				}
			} else if( method == LayerNI.bmCopyOnAlpha || method == LayerNI.bmCopyOnAddAlpha ) {
				if( opa == 255 ) {
					g.setComposite( AlphaComposite.Src );
				} else {
					g.setComposite(createAlphaComposite( AlphaComposite.SRC, opa ));
				}
			} else if( method == LayerNI.bmAlpha || method == LayerNI.bmAlphaOnAlpha
					|| method == LayerNI.bmAlphaOnAddAlpha || method == LayerNI.bmAddAlpha || method == LayerNI.bmAddAlphaOnAlpha || method == LayerNI.bmAddAlphaOnAddAlpha ){
				if( opa == 255 ) {
					g.setComposite( AlphaComposite.SrcOver );
				} else {
					g.setComposite(createAlphaComposite( AlphaComposite.SRC_OVER, opa ));
				}
			} else {
				g.setComposite(new CustomOperationComposite( method, opa, hda ));
			}
		} else {
			//g.setComposite(createAlphaComposite( AlphaComposite.SRC_OVER, opa ));
			g.setComposite(new CustomOperationComposite( method, opa, hda ));
		}
		*/
		boolean ret = g.drawImage( src.mImage, dstrect.left, dstrect.top, dstrect.right, dstrect.bottom,
				srcrect.left, srcrect.top, srcrect.right, srcrect.bottom, null );
		g.dispose();
		return ret;
	}

	private static BufferedImage mFontTempImage;
	private static final int FontTempImageWidth = 64;
	private static final int FontTempImageHeight= 64;
	public static CharacterData getCharacterData( Font fontV, char ch, boolean aa ) {
		if( mFontTempImage == null ) {
			mFontTempImage = new BufferedImage( FontTempImageWidth, FontTempImageHeight, BufferedImage.TYPE_BYTE_GRAY );
		}
		Graphics2D g = (Graphics2D)mFontTempImage.getGraphics();
		java.awt.Font font = fontV.getFont();
		if( aa ) {
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
		} else {
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF );
		}
		FontRenderContext frc = g.getFontRenderContext();
		char[] c = {ch};
		GlyphVector glyph = font.layoutGlyphVector(frc, c, 0, 1, 0 );
		Rectangle r = glyph.getPixelBounds(frc,0,0);
		//FontMetrics metrics = g.getFontMetrics(font);
	    //float ox = 0;
	    //float oy = metrics.getMaxAscent();
		int rw = r.width;
		rw =  (((rw -1)>>>2)+1)<<2;
		int rh = r.height;
		if( rw > mFontTempImage.getWidth() || rh > mFontTempImage.getHeight() ) {
			// 小さすぎるので再生成
			int size = rw;
			if( size < rh ) size = rh;
			int s2 = mFontTempImage.getWidth();
			while( s2 < size ) s2 <<= 1; // 2 の累乗値で指定サイズより大きいサイズを探す
			mFontTempImage = new BufferedImage( s2, s2, BufferedImage.TYPE_BYTE_GRAY );
			g = (Graphics2D)mFontTempImage.getGraphics();
			if( aa ) {
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
			} else {
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF );
			}
			frc = g.getFontRenderContext();
			glyph = font.layoutGlyphVector(frc, c, 0, 1, 0 );
			r = glyph.getPixelBounds(frc,0,0);
		}
		g.setComposite(AlphaComposite.Src);
		g.setPaint( Color.BLACK );
		g.fillRect( 0, 0, rw, rh );
		g.setPaint( Color.WHITE );
		g.drawGlyphVector(glyph,-r.x,-r.y);

		// 描画したbitmap を 65 階調のグレーススケールに変換する
		CharacterData ret = new CharacterData();
		ret.alloc(rw*rh);
		byte[] bmp = ret.getData();
		final int w = mFontTempImage.getWidth();
		WritableRaster src = mFontTempImage.getRaster();
		DataBuffer buff = src.getDataBuffer();
		final int type = buff.getDataType();
		if( type == DataBuffer.TYPE_BYTE ) {
			DataBufferByte srcBuff = (DataBufferByte)buff;
			byte[] s = srcBuff.getData();
			int ystart = 0;//(int) ((r.y+oy)*w + r.x);
			int idx = 0;
			for( int y = 0; y < rh; y++ ) {
				int sp = ystart;
				for( int x = 0; x < rw; x++ ) {
					int color = s[sp]&0xff;
					byte out = (byte) (color >>> 2); // 0 - 63
					if( color != 0 ) out++;
					bmp[idx] = out;
					idx++;
					sp++;
				}
				ystart += w;
			}
		}
		ret.mAntialiased = aa;
		ret.mPitch = rw;
		ret.mOriginX = r.x;
	    //float base = metrics..getMaxAscent();
		//ret.mOriginY = (int) (r.y+oy);
		ret.mOriginY = r.y;
		ret.mBlackBoxX = rw;
		ret.mBlackBoxY = rh;
		Rectangle2D r2 = font.getStringBounds( c, 0, 1,frc );
		ret.mCellIncX = (int) r2.getWidth();
		ret.mCellIncY = 0;//(int) r2.getHeight();

		return ret;
	}
	/**
	 * 影のぼけと透明度を取り除く処理は実装していない。
	 * ネイティブではこの実装とし、エンジン指定の時ピクセル単位での処理による描画となる。
	 * @param font
	 * @param fontChange
	 * @param destrect
	 * @param x
	 * @param y
	 * @param text
	 * @param color
	 * @param bltmode
	 * @param opa
	 * @param holdalpha
	 * @param aa
	 * @param shlevel
	 * @param shadowcolor
	 * @param shwidth
	 * @param shofsx
	 * @param shofsy
	 * @param updaterects
	 */
	public final void drawText( Font fontV, final Rect destrect, int x, int y, final String text,
			int color, int bltmode, int opa, boolean holdalpha, boolean aa,
			int shlevel, int shadowcolor, int shwidth, int shofsx, int shofsy, ComplexRect updaterects ) {
		final int count = text.length();
		if( count == 0 ) return;

		java.awt.Font font = fontV.getFont();
		if( bltmode == LayerNI.bmAlphaOnAlpha ) {
			//if(opa < -255) opa = -255;
			if(opa < 0) opa = 0; // 透明度を取り除く処理していない
			if(opa > 255) opa = 255;
		} else {
			if(opa < 0) opa = 0;
			if(opa > 255 ) opa = 255;
		}
		if(opa == 0) return; // nothing to do

		if( mGlyphHash == null ) {
			mGlyphHash = new HashMap<Character,GlyphVector>();
		}
		boolean fontChange = mChecheFont != font;
		if( fontChange ) {
			mGlyphHash.clear();
			mChecheFont = font;
		}

		Image img = getImage();
		if( img == null ) return;
		Graphics2D g = (Graphics2D)img.getGraphics();
		FontMetrics metrics = g.getFontMetrics(font);
		Composite composite;
		Composite shadowcomposite = null;
		if( shlevel > 255 ) shlevel = 255; // 256より大きい場合無視
		if( bltmode == LayerNI.bmCopy || bltmode == LayerNI.bmCopyOnAlpha ) {
			composite = createAlphaComposite( AlphaComposite.SRC, opa );
			if( shlevel != 0 )
				composite = createAlphaComposite( AlphaComposite.SRC, shlevel );
		} else if( bltmode == LayerNI.bmAlpha || bltmode == LayerNI.bmAlphaOnAlpha ) {
			composite = createAlphaComposite( AlphaComposite.SRC_OVER, opa );
			if( shlevel != 0 )
				shadowcomposite = createAlphaComposite( AlphaComposite.SRC_OVER, shlevel );
		} else {
			composite = createAlphaComposite( AlphaComposite.SRC_OVER, opa );
			if( shlevel != 0 )
				shadowcomposite = createAlphaComposite( AlphaComposite.SRC_OVER, shlevel );
			// g.setComposite(new CustomOperationComposite( bltmode, opa, hda )); TODO
		}
		if( shlevel == 0 ) {
			g.setComposite(composite);
		}
		if( aa ) {
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
		} else {
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF );
		}
		Color fontColor = new Color(color);
		Color shadowColor = new Color(shadowcolor);
		char[] c = text.toCharArray();
		FontRenderContext frc = g.getFontRenderContext();
	    float ox = x;
	    float oy = y + metrics.getMaxAscent();
	    Rect shbound = new Rect();
		Rect srect = new Rect();
		Rect union = new Rect();
		BasicStroke shStoroke = null;
		if( shlevel != 0 ) { // 影を書く
			shStoroke = new BasicStroke(shwidth); // オリジナルと違いぼやけない
		}
		for( int i = 0; i < count; i++ ) {
			boolean shadowdraw = false;
			boolean glyphdraw = false;
			GlyphVector glyph = mGlyphHash.get(c[i]);
			if( glyph == null ) {
				glyph = font.layoutGlyphVector(frc, c, i, i+1, 0 );
				mGlyphHash.put(c[i], glyph );
			}
			if( shlevel != 0 ) { // 影を書く
				g.setStroke(shStoroke);
				Shape shape = glyph.getOutline(ox+shofsx-shwidth/2, oy+shofsy-shwidth/2);
				Rectangle r = shape.getBounds();
			    if( checkBounds(destrect, r ) ) {
					if( shlevel != 0 ) {
						g.setComposite(shadowcomposite);
					}
			    	g.setPaint( shadowColor );
			    	g.draw(shape);
			    	g.fill(shape);
			    	shbound.set( r.x, r.y, r.x+r.width, r.y+r.height );
			    	shadowdraw = true;
			    	updaterects.or(shbound);
			    }
			}
		    Rectangle r = glyph.getPixelBounds(frc,ox,oy);
		    if( checkBounds(destrect, r ) ) {
				if( shlevel != 0 ) {
					g.setComposite(composite);
				}
				g.setPaint( fontColor );
			    g.drawGlyphVector(glyph,ox,oy);
			    if( updaterects != null ) {
			    	srect.set( r.x, r.y, r.x+r.width, r.y+r.height );
			    	glyphdraw = true;
			    }
		    }
		    if( shadowdraw || glyphdraw ) {
		    	if( shadowdraw && glyphdraw ) {
		    		Rect.unionRect(union, srect, shbound);
			    	updaterects.or(union);
		    	} else if( shadowdraw ) {
			    	updaterects.or(shbound);
		    	} else if( glyphdraw ) {
			    	updaterects.or(srect);
		    	}
		    }
			ox += glyph.getGlyphMetrics(0).getAdvanceX();
		}
	}
	private static final boolean checkBounds( final Rect drect, final Rectangle srect ) {
		// check boundary
		if( srect.x >= drect.right ) return false;
		if( (srect.x+srect.width) < drect.left ) return false;

		if( srect.y >= drect.bottom ) return false;
		if( (srect.y+srect.height) < drect.top ) return false;

		return true;
	}
	private AlphaComposite createAlphaComposite( int type, int opa ) {
		return AlphaComposite.getInstance( type, ((float)opa)/255.0f );
	}

	private static final int BITMAPFILEHEADER_SIZE = 14;
	private static final int BITMAPINFOHEADER_SIZE = 40;
	private static final int BMP_HEADER_SIZE = BITMAPFILEHEADER_SIZE + BITMAPINFOHEADER_SIZE;
	private static final int BMP_PALETTE_SIZE = 1024;
	private static final int BI_RGB = 0;
	private final static void create256ColorPaletteTable() {
		DitherTable_676 = new byte[3][][][];
		for( int i = 0; i < 3; i++ ) {
			DitherTable_676[i] = new byte[4][][];
			for( int j = 0; j < 4; j++ ) {
				DitherTable_676[i][j] = new byte[4][];
				for( int k = 0; k < 4; k++ ) {
					DitherTable_676[i][j][k] = new byte[256];
				}
			}
		}

		/* create an ordered dither table for conversion of 8bit->6bit and 8bit->5bit and */
		/* RGB ( 256*256*256 ) -> palettized 252 colors ( 6*7*6 ) */
		for( int j = 0; j < 4; j ++ ) {
			for( int i = 0; i < 4; i ++ ) {
				double v1 = Dither4x4[j][i] / 16.0;
				double v2 = Dither4x4[((j+1)%2)][((i+1)%2)] / 16.0;
				double v3 = Dither4x4[j][((i+1)%2)] / 16.0;

				int n;

				for( n = 0; n < 256; n++ ) {
					double nt = n / 255.0;
					double frac;
					int main;

					/*
					// for 5bit
					main = (int)(nt * 31.0);
					frac = nt * 31.0 - (int)(nt * 31.0);
					TVPDitherTable_5_6[j][i][0][n] = main + ((v1 < frac)?1:0);
					TVPDitherTable_5_6[j+4][i][0][n] = TVPDitherTable_5_6[j][i][0][n];

					// for 6bit
					main = (int)(nt * 63.0);
					frac = nt * 63.0 - (int)(nt * 63.0);
					TVPDitherTable_5_6[j][i][1][n] = main + ((v2 < frac)?1:0);
					TVPDitherTable_5_6[j+4][i][1][n] = TVPDitherTable_5_6[j][i][1][n];
					*/

					// 256 level -> 6 level R, B
					main = (int)(nt * 5);
					frac = nt * 5 - (int)(nt * 5);
					DitherTable_676[2][i][j][n] = (byte) ((main + ((v1 < frac)?1:0)) * (6 * 7));
					DitherTable_676[0][i][j][n] = (byte) (main + ((v2 < frac)?1:0));

					// 256 level -> 7 level G
					main = (int)(nt * 6);
					frac = nt * 6 - (int)(nt * 6);
					DitherTable_676[1][i][j][n] = (byte) ((main + ((v3 < frac)?1:0)) * (6));
				}
			}
		}

		/* create 256 colors dither palette table */
		/* ( 252 colors are used ) */
		Color252DitherPalette = new byte[3][];
		for( int i = 0; i < 3; i++ ) {
			Color252DitherPalette[i] = new byte[256];
		}
		/* create 256 colors dither palette table */
		/* ( 252 colors are used ) */
		int c = 0;
		for( int r = 0; r < 6; r++ ) {
			for( int g = 0; g < 7; g++ ) {
				for( int b = 0; b < 6; b++ ) {
					Color252DitherPalette[0][c] = (byte)( (r * 255 / 5)&0xff );
					Color252DitherPalette[1][c] = (byte)( (g * 255 / 6)&0xff );
					Color252DitherPalette[2][c] = (byte)( (b * 255 / 5)&0xff );
					c++;
				}
			}
		}
		for( ; c < 256; c++ ) {
			Color252DitherPalette[0][c] = Color252DitherPalette[1][c] = Color252DitherPalette[2][c] = 0;
		}
	}
	public final void saveAs( OutputStream output, final String name, final String type ) throws TJSException {
		if( WRITABLE_FORMANT_NAMES == null ) {
	    	WRITABLE_FORMANT_NAMES = ImageIO.getWriterFormatNames();
		}
		BufferedImage img = null;
		if( mImage instanceof BufferedImage ) {
			img = (BufferedImage)mImage;
		} else if( mImage != null ) {
			img = new BufferedImage( mImage.getWidth(null), mImage.getHeight(null), BufferedImage.TYPE_INT_ARGB );
			Graphics2D g = (Graphics2D)img.getGraphics();
			g.setComposite( AlphaComposite.Src );
			g.drawImage( mImage, 0, 0, null );
			g.dispose();
		}
		if( img == null ) {
			Message.throwExceptionMessage(Message.InternalError);
			return;
		}

		boolean foundtype = false;
		boolean saveBmp = false;
		if( type.startsWith("bmp") ) {
			saveBmp = true;
			/*
			final int count = WRITABLE_FORMANT_NAMES.length;
			for( int i = 0; i < count; i++ ) {
				if( WRITABLE_FORMANT_NAMES[i].equals("bmp") ) {
					foundtype = true;
					break;
				}
			}
			*/
		} else {
			final int count = WRITABLE_FORMANT_NAMES.length;
			for( int i = 0; i < count; i++ ) {
				if( WRITABLE_FORMANT_NAMES[i].equals(type) ) {
					foundtype = true;
					break;
				}
			}
		}
		if( foundtype == false && saveBmp == false ) {
			Message.throwExceptionMessage(Message.InvalidImageSaveType, type);
			return;
		}
		String format = type;
		if( saveBmp ) {
			// open stream TODO ストリーム開必要ないんじゃない？ output に吐けばいいような
			BinaryStream stream = Storage.createStream(TVP.StorageMediaManager.normalizeStorageName(name,null), BinaryStream.WRITE );

			try {
				int pixelbytes = 4;
				if( "bmp24".equalsIgnoreCase(type) ) {
					pixelbytes = 3;
				} else if( "bmp8".equalsIgnoreCase(type) ) {
					pixelbytes = 1;
				}
				final int height = img.getHeight();
				final int width = img.getWidth();
				final int fileSize = calcBmpSize( width, height, pixelbytes*8 );
				ByteBuffer buffer = ByteBuffer.allocate(fileSize);
				buffer.order(ByteOrder.LITTLE_ENDIAN);

				// prepare header
				int bmppitch = width * pixelbytes;
				bmppitch = (((bmppitch - 1) >> 2) + 1) << 2;

				buffer.putShort((short) 0x4d42);  // bfType
				buffer.putInt( BMP_HEADER_SIZE + bmppitch*height + (pixelbytes==1 ? BMP_PALETTE_SIZE : 0) ); // bfSize
				buffer.putShort( (short) 0 );  // bfReserved1
				buffer.putShort( (short) 0 );  // bfReserved2
				buffer.putInt( BMP_HEADER_SIZE + (pixelbytes==1 ? BMP_PALETTE_SIZE : 0) ); // bfOffBits
				buffer.putInt( BITMAPINFOHEADER_SIZE ); // biSize
				buffer.putInt( width ); // biWidth
				buffer.putInt( height ); // biHeight
				buffer.putShort( (short) 1 );  // biPlanes
				buffer.putShort( (short)(pixelbytes*8) );  // biBitCount
				buffer.putInt( BI_RGB ); // biCompression
				buffer.putInt( 0 ); // biSizeImage
				buffer.putInt( 0 ); // biXPelsPerMeter
				buffer.putInt( 0 ); // biYPelsPerMeter
				buffer.putInt( 0 ); // biClrUsed
				buffer.putInt( 0 ); // biClrImportant

				// write palette
				if( pixelbytes == 1 ) {
					if( DitherTable_676 == null ) create256ColorPaletteTable();
					for( int i = 0; i < 256; i++ ) {
						buffer.put(Color252DitherPalette[0][i]);
						buffer.put(Color252DitherPalette[1][i]);
						buffer.put(Color252DitherPalette[2][i]);
						buffer.put((byte)0);
					}
				}

				WritableRaster src = mImage.getRaster();
				DataBuffer buff = src.getDataBuffer();
				final int buftype = buff.getDataType();
				if( buftype != DataBuffer.TYPE_INT ) Message.throwExceptionMessage(Message.InternalError);
				DataBufferInt srcBuff = (DataBufferInt)buff;
				int[] s = srcBuff.getData();

				int remain24 = bmppitch%3;
				// write bitmap body
				for( int y = height - 1; y >= 0; y -- ) {
					//if(!buf) buf = new tjs_uint8[bmppitch];
					int starty = y * width;
					if( pixelbytes == 4 ) {
						for( int x = 0; x < width; x++ ) {
							buffer.putInt( s[starty+x] );
						}
					} else if( pixelbytes == 1 ) {
						int xofs = 0;
						int yofs = y;
						/*
						byte[] line = DitherTable_676[0][yofs & 0x03][0];
						int vx = (xofs & 0x03) << 8;
						for( int x = 0; x < width; x++ ) {
							int v = s[starty+x];
							// TODO 一次元配列とみなしてアクセスしているので、Java では期待通りに動かない、方法を変える必要有り
							int dest = (line[vx + ((v >>> 16) & 0xff)])+ (line[(256 * 16 * 2) + vx + (v & 0xff)]) +
								(line[(16 * 256) + vx + ((v >>> 8) & 0xff)]);
							vx += 0x100;
							vx &= 0x300;
							*/
						byte[][][][] line = DitherTable_676;
						yofs = yofs & 0x03;
						int vx = xofs & 0x03;
						for( int x = 0; x < width; x++ ) {
							int v = s[starty+x];
							int dest = (line[0][yofs][vx][((v >>> 16) & 0xff)])
									 + (line[2][yofs][vx][(v & 0xff)])
									 + (line[1][yofs][vx][((v >>> 8) & 0xff)]);
							vx += 0x1;
							vx &= 0x3;

							buffer.put( (byte) (dest&0xff) );
						}
						for( int i = width; i < bmppitch; i++ ) buffer.put( (byte) 0 );
					} else {
						for( int x = 0; x < width; x++ ) {
							int color = s[starty+x];
							buffer.put( (byte) (color&0xff) );
							buffer.put( (byte) ((color>>>8)&0xff) );
							buffer.put( (byte) ((color>>>16)&0xff) );
						}
						for( int i = 0; i < remain24; i++ ) buffer.put( (byte) 0 );
					}
				}
				stream.write( buffer.array() );
			} finally {
				stream.close();
				stream = null;
				img = null;
			}
		} else {
			try {
				ImageIO.write( img, format, output );
				output.close();
			} catch (IOException e) {
				img = null;
				Message.throwExceptionMessage(Message.WriteError );
			}
		}
		img = null;
	}

	/**
	 * BMP 形式で出力した時のファイルサイズを計算する
	 * @param w 幅
	 * @param h 高さ
	 * @param bpp ピクセル当たりのビット数
	 * @return ファイルサイズ
	 */
	private int calcBmpSize( int w, int h, int bpp ) {
		int size = 0;
		if( bpp == 8 )
			size = ((((w - 1) >> 2) + 1) << 2) * h + BMP_PALETTE_SIZE + BMP_HEADER_SIZE;
		else if( bpp == 32 )
			size = w * 4 * h + BMP_HEADER_SIZE;
		else // 24
			size = (((w * 3 + 3) >> 2) << 2) * h + BMP_HEADER_SIZE;
		return size;
	}
	public final int getPoint(int x, int y) throws TJSException {
		if( x < 0 || y < 0 || x >= getWidth() || y >= getHeight() )
			Message.throwExceptionMessage(Message.OutOfRectangle);

		if( mImage.getType() == BufferedImage.TYPE_BYTE_INDEXED ) {
			final int w = mImage.getWidth();
			WritableRaster src = mImage.getRaster();
			DataBuffer buff = src.getDataBuffer();
			final int type = buff.getDataType();
			if( type == DataBuffer.TYPE_BYTE ) {
				DataBufferByte srcBuff = (DataBufferByte)buff;
				byte[] s = srcBuff.getData();
				final int pos = x+y*w;
				if( pos >= 0 && pos < s.length ) {
					final byte ret = s[pos];
					//System.out.println( "x:"+x+", y:"+y+", color:"+ret );
					return ret;
				} else {
					Message.throwExceptionMessage(Message.OutOfRectangle);
				}
			}
			return 0;
		} else {
			return mImage.getRGB(x, y);
		}
	}
	public final void setPoint(int x, int y, int n) throws TJSException {
		if( x < 0 || y < 0 || x >= getWidth() || y >= getHeight() )
			Message.throwExceptionMessage(Message.OutOfRectangle);

		if( mImage.getType() == BufferedImage.TYPE_BYTE_INDEXED ) {
			final int w = mImage.getWidth();
			WritableRaster src = mImage.getRaster();
			DataBuffer buff = src.getDataBuffer();
			final int type = buff.getDataType();
			if( type == DataBuffer.TYPE_BYTE ) {
				DataBufferByte srcBuff = (DataBufferByte)buff;
				byte[] s = srcBuff.getData();
				final int pos = x+y*w;
				if( pos >= 0 && pos < s.length ) {
					s[pos] = (byte) (n&0xff);
				} else {
					Message.throwExceptionMessage(Message.OutOfRectangle);
				}
			}
		} else {
			mImage.setRGB(x, y, n);
		}
	}
	public void coerceData(boolean b) {
		mImage.coerceData(b);
	}
	public boolean isAlphaPremultiplied() {
		return mImage.isAlphaPremultiplied();
	}
	public void flipLR(Rect rect) throws TJSException {
		final int w = mImage.getWidth();

		WritableRaster src = mImage.getRaster();
		DataBuffer buff = src.getDataBuffer();
		final int type = buff.getDataType();
		if( type == DataBuffer.TYPE_INT ) {
			DataBufferInt srcBuff = (DataBufferInt)buff;
			int[] s = srcBuff.getData();
			int ystart = rect.top*w + rect.left;
			final int rh = rect.bottom-rect.top;
			final int rw = rect.right-rect.left;
			final int w2 = rw / 2;
			for( int y = 0; y < rh; y++ ) {
				for( int x = 0; x < w2; x++ ) {
					int pos = ystart+x;
					int dst = ystart+rw-x-1;
					int c = s[pos];
					s[pos] = s[dst];
					s[dst] = c;
				}
				ystart += w;
			}
		} else if( type == DataBuffer.TYPE_BYTE && mImage.getType() == BufferedImage.TYPE_BYTE_GRAY ) {
			byte[] s = ((DataBufferByte) buff).getData();
			int ystart = rect.top*w + rect.left;
			final int rh = rect.bottom-rect.top;
			final int rw = rect.right-rect.left;
			final int w2 = rw / 2;
			for( int y = 0; y < rh; y++ ) {
				for( int x = 0; x < w2; x++ ) {
					int pos = ystart+x;
					int dst = ystart+rw-x-1;
					byte c = s[pos];
					s[pos] = s[dst];
					s[dst] = c;
				}
				ystart += w;
			}
		} else {
			throw new TJSException("Not support image type.");
		}
	}
	public void flipUD(Rect rect) throws TJSException {
		final int w = mImage.getWidth();

		WritableRaster src = mImage.getRaster();
		DataBuffer buff = src.getDataBuffer();
		final int type = buff.getDataType();
		if( type == DataBuffer.TYPE_INT ) {
			DataBufferInt srcBuff = (DataBufferInt)buff;
			int[] s = srcBuff.getData();
			int ystart = rect.top*w + rect.left;
			int yend = (rect.bottom-1)*w + rect.left;
			final int rw = rect.right-rect.left;
			final int h2 = (rect.bottom-rect.top) / 2;
			for( int y = 0; y < h2; y++ ) {
				int sp = ystart;
				int dp = yend;
				for( int x = 0; x < rw; x++ ) {
					int c = s[sp];
					s[sp] = s[dp];
					s[dp] = c;
					sp++;
					dp++;
				}
				ystart += w;
				yend -= w;
			}
		} else if( type == DataBuffer.TYPE_BYTE && mImage.getType() == BufferedImage.TYPE_BYTE_GRAY ) {
			byte[] s = ((DataBufferByte) buff).getData();
			int ystart = rect.top*w + rect.left;
			int yend = (rect.bottom-1)*w + rect.left;
			final int rw = rect.right-rect.left;
			final int h2 = (rect.bottom-rect.top) / 2;
			for( int y = 0; y < h2; y++ ) {
				int sp = ystart;
				int dp = yend;
				for( int x = 0; x < rw; x++ ) {
					byte c = s[sp];
					s[sp] = s[dp];
					s[dp] = c;
					sp++;
					dp++;
				}
				ystart += w;
				yend -= w;
			}
		} else {
			throw new TJSException("Not support image type.");
		}
	}
	public void fillMask(Rect rect, int opa) throws TJSException {
		final int w = mImage.getWidth();
		final int mask = opa << 24;

		WritableRaster src = mImage.getRaster();
		DataBuffer buff = src.getDataBuffer();
		final int type = buff.getDataType();
		if( type == DataBuffer.TYPE_INT ) {
			DataBufferInt srcBuff = (DataBufferInt)buff;
			int[] s = srcBuff.getData();
			int ystart = rect.top*w + rect.left;
			final int rw = rect.right-rect.left;
			final int rh = (rect.bottom-rect.top);
			for( int y = 0; y < rh; y++ ) {
				int sp = ystart;
				for( int x = 0; x < rw; x++ ) {
					int c = s[sp] & 0x00ffffff;
					s[sp] = c | mask;
					sp++;
				}
				ystart += w;
			}
		} else {
			throw new TJSException("Not support image type.");
		}
	}
	public void setPointMain(int x, int y, int color) {
		int c = mImage.getRGB(x, y);
		c &= 0xff000000;
		c |= color;
		mImage.setRGB(x, y, c);
	}
	public void setPointMask(int x, int y, int mask) {
		int c = mImage.getRGB(x, y);
		c &= 0x00ffffff;
		c |= (mask&0xff) << 24;
		mImage.setRGB(x, y, c);
	}
	public void blendColor(Rect rect, int color, int opa, boolean additive) throws TJSException {
		Graphics2D g = (Graphics2D)mImage.getGraphics();
		if( opa == 255 ) {
			g.setComposite( AlphaComposite.Src );
		} else {
			g.setComposite(createAlphaComposite( AlphaComposite.SRC_OVER, opa ));
		}
		g.setColor( new Color(color, false) );
		g.fillRect( rect.left, rect.top, rect.width(), rect.height() );
		g.dispose();

		/*
		final int w = mImage.getWidth();
		WritableRaster src = mImage.getRaster();
		DataBuffer buff = src.getDataBuffer();
		final int type = buff.getDataType();
		if( type == DataBuffer.TYPE_INT ) {
			DataBufferInt srcBuff = (DataBufferInt)buff;
			int[] s = srcBuff.getData();
			int ystart = rect.top*w + rect.left;
			final int rw = rect.right-rect.left;
			final int rh = (rect.bottom-rect.top);
			if( opa == 255 ) {
				color |= 0xff000000;
				for( int y = 0; y < rh; y++ ) {
					int sp = ystart;
					for( int x = 0; x < rw; x++ ) {
						s[sp] = color;
						sp++;
					}
					ystart += w;
				}
			} else {
				// alpha fill
				if( !additive ) {
					int s1 = color & 0xff00ff;
					color = color & 0xff00;
					for( int y = 0; y < rh; y++ ) {
						int sp = ystart;
						for( int x = 0; x < rw; x++ ) {
							int d = s[sp];
							int dopa = d >>> 24;
							int alpha = ((int)CustomOperationComposite.OpacityOnOpacityTable[dopa+(opa<<8)])&0xff;
							int d1 = d & 0xff00ff;
							d1 = ((d1 + ((s1 - d1) * alpha >>> 8)) & 0xff00ff) | ((255-((255-dopa)*(255-opa)>>>8)) << 24);
							d &= 0xff00;
							s[sp] = d1 | ((d + ((color - d) * alpha >>> 8)) & 0xff00);
							sp++;
						}
						ystart += w;
					}
				} else {
					int src1 = (((((color & 0x00ff00) * opa) & 0x00ff0000) + (((color & 0xff00ff) * opa) & 0xff00ff00) ) >>> 8);
					int opa_inv = opa ^ 0xff;
					for( int y = 0; y < rh; y++ ) {
						int sp = ystart;
						for( int x = 0; x < rw; x++ ) {
							int dest = s[sp];
							// Di = sat(Si, (1-Sa)*Di)
							// Da = Sa + Da - SaDa
							int dopa = dest >>> 24;
							dopa = dopa + opa - (dopa*opa >>> 8);
							dopa -= (dopa >>> 8); // adjust alpha
							int a = (((dest & 0xff00ff)*opa_inv >>> 8) & 0xff00ff) + (((dest & 0xff00)*opa_inv >>> 8) & 0xff00);
							int b = src1;
							// Add each byte of packed 8bit values in two 32bit uint32, with saturation.
							int tmp = (  ( a & b ) + ( ((a ^ b)>>>1) & 0x7f7f7f7f)  ) & 0x80808080;
							tmp = (tmp<<1) - (tmp>>>7);
							tmp = (a + b - tmp) | tmp;
							s[sp] = (dopa << 24) +  tmp;
							sp++;
						}
						ystart += w;
					}
				}
			}
		} else {
			throw new TJSException("Not support image type.");
		}
		*/
	}
	public void removeConstOpacity(Rect rect, int level) {
		final int w = mImage.getWidth();
		WritableRaster src = mImage.getRaster();
		DataBuffer buff = src.getDataBuffer();
		final int type = buff.getDataType();
		if( type == DataBuffer.TYPE_INT ) {
			DataBufferInt srcBuff = (DataBufferInt)buff;
			int[] s = srcBuff.getData();
			int ystart = rect.top*w + rect.left;
			final int rw = rect.right-rect.left;
			final int rh = (rect.bottom-rect.top);
			if( level == 255 ) {
				for( int y = 0; y < rh; y++ ) {
					int sp = ystart;
					for( int x = 0; x < rw; x++ ) {
						s[sp] = s[sp] & 0x00ffffff;
						sp++;
					}
					ystart += w;
				}
			} else {
				int strength = 255 - level;
				for( int y = 0; y < rh; y++ ) {
					int sp = ystart;
					for( int x = 0; x < rw; x++ ) {
						int d = s[sp];
						s[sp] = (d & 0xffffff) + ( (((d>>>24)*strength) << 16) & 0xff000000);
						sp++;
					}
					ystart += w;
				}
			}
		}
	}
	/**
	 *
	 * @param destrect 転送先矩形
	 * @param ref ソース画像
	 * @param refrect 転送元矩形
	 * @param type 補間タイプ
	 * @param hda アルファ保持有無
	 * @param opa 透明度
	 * @param method コピー方法
	 */
	public boolean stretchBlt(Rect cliprect, Rect destrect, NativeImageBuffer ref, Rect refrect, int type, boolean hda, int opa, int method) {
		Graphics2D g = (Graphics2D)mImage.getGraphics();
		g.setClip( cliprect.left, cliprect.top, cliprect.width(), cliprect.height() );
		if( type == BaseBitmap.stFastLinear || type == BaseBitmap.stLinear ) {
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION , RenderingHints.VALUE_INTERPOLATION_BILINEAR ); // バイリニア
		} else if( type == BaseBitmap.stCubic ) {
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION , RenderingHints.VALUE_INTERPOLATION_BICUBIC ); // バイキュービック
		} else { // BaseBitmap.stNearest
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION , RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR ); // ニアレストネイバー
		}
		if( !hda ) {
			if( method == LayerNI.bmCopy ) {
				if( opa == 255 ) {
					g.setComposite( AlphaComposite.Src );
				} else {
					g.setComposite(createAlphaComposite( AlphaComposite.SRC, opa ));
				}
			} else if( method == LayerNI.bmCopyOnAlpha || method == LayerNI.bmCopyOnAddAlpha ) {
				if( opa == 255 ) {
					g.setComposite( AlphaComposite.Src );
				} else {
					g.setComposite(createAlphaComposite( AlphaComposite.SRC, opa ));
				}
			} else if( method == LayerNI.bmAlpha || method == LayerNI.bmAlphaOnAlpha || method == LayerNI.bmAlphaOnAddAlpha ||
						method == LayerNI.bmAddAlpha || method == LayerNI.bmAddAlphaOnAlpha || method == LayerNI.bmAddAlphaOnAddAlpha ) {
				if( opa == 255 ) {
					g.setComposite( AlphaComposite.SrcOver );
				} else {
					g.setComposite(createAlphaComposite( AlphaComposite.SRC_OVER, opa ));
				}
			} else {
				g.setComposite(new CustomOperationComposite( method, opa, hda ));
			}
		} else {
			g.setComposite(new CustomOperationComposite( method, opa, hda ));
		}
		boolean ret = g.drawImage( ref.mImage, destrect.left, destrect.top, destrect.right, destrect.bottom,
				refrect.left, refrect.top, refrect.right, refrect.bottom, null );
		g.dispose();
		return ret;
	}
	public boolean affineBlt(Rect destrect, NativeImageBuffer ref, Rect refrect,
			AffineMatrix2D matrix, int method, int opa, Rect updaterect,
			boolean hda, int type, boolean clear, int clearcolor) {

		Graphics2D g = (Graphics2D)mImage.getGraphics();
		g.setClip( destrect.left, destrect.top, destrect.width(), destrect.height() );

		if( clear ) {
			g.setComposite( AlphaComposite.Src );
			g.setColor( new Color( clearcolor, true) );
			g.fillRect( destrect.left, destrect.top, destrect.width(), destrect.height() );
		}

		if( type == BaseBitmap.stFastLinear || type == BaseBitmap.stLinear ) {
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION , RenderingHints.VALUE_INTERPOLATION_BILINEAR ); // バイリニア
		} else if( type == BaseBitmap.stCubic ) {
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION , RenderingHints.VALUE_INTERPOLATION_BICUBIC ); // バイキュービック
		} else { // BaseBitmap.stNearest
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION , RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR ); // ニアレストネイバー
		}
		AffineTransform trans = new AffineTransform( matrix.a, matrix.b, matrix.c, matrix.d, matrix.tx, matrix.ty );

		// 更新される矩形を求める
		int width = refrect.width();
		int height = refrect.height();
		if( clear ) {
			updaterect.left = destrect.left;
			updaterect.right= destrect.right;
			updaterect.top   = destrect.top;
			updaterect.bottom= destrect.bottom;
		} else {
			double[] pts = new double[8];
			pts[0] = 0; // x1
		    pts[1] = 0; // y1
			pts[2] = width; // x2
		    pts[3] = 0; // y2
			pts[4] = width; // x3
		    pts[5] = height; // y3
			pts[6] = 0; // x4
		    pts[7] = height; // y4
			double[] dstPts = new double[8];
			trans.transform( pts, 0, dstPts, 0, 4 );
			// ソート
			for( int i = 0; i < 3; i++) {
				for( int j = 3; j > i; j--) {
					int p = (j-1)*2;
					int c = j*2;
					double prev = dstPts[p];
					double cur = dstPts[c];
					if( prev > cur ) {	// 前の要素の方が大きかったら
						// 交換する
						dstPts[c] = prev;
						dstPts[p] = cur;
					}
					p = (j-1)*2+1;
					c = j*2+1;
					prev = dstPts[p];
					cur = dstPts[c];
					if( prev > cur ) {	// 前の要素の方が大きかったら
						// 交換する
						dstPts[c] = prev;
						dstPts[p] = cur;
					}
				}
			}
			updaterect.left = (int) dstPts[0];
			updaterect.right= (int) dstPts[6]+1;
			updaterect.top   = (int) dstPts[1];
			updaterect.bottom= (int) dstPts[7]+1;
		}

		g.setTransform(trans);
		if( !hda ) {
			if( method == LayerNI.bmCopy ) {
				if( opa == 255 ) {
					g.setComposite( AlphaComposite.Src );
					//g.setComposite(new CustomOperationComposite( LayerNI.bmCopy, 255, false ));
				} else {
					g.setComposite(createAlphaComposite( AlphaComposite.SRC, opa ));
					//g.setComposite(new CustomOperationComposite( LayerNI.bmCopy, opa, false ));
				}
			} else if( method == LayerNI.bmCopyOnAlpha || method == LayerNI.bmCopyOnAddAlpha ) {
				if( opa == 255 ) {
					g.setComposite( AlphaComposite.Src );
				} else {
					g.setComposite(createAlphaComposite( AlphaComposite.SRC, opa ));
				}
			} else if( method == LayerNI.bmAlpha || method == LayerNI.bmAlphaOnAlpha || method == LayerNI.bmAlphaOnAddAlpha ||
						method == LayerNI.bmAddAlpha || method == LayerNI.bmAddAlphaOnAlpha || method == LayerNI.bmAddAlphaOnAddAlpha ) {
				if( opa == 255 ) {
					g.setComposite( AlphaComposite.SrcOver );
				} else {
					g.setComposite(createAlphaComposite( AlphaComposite.SRC_OVER, opa ));
				}
			} else {
				g.setComposite(new CustomOperationComposite( method, opa, hda ));
			}
		} else {
			g.setComposite(new CustomOperationComposite( method, opa, hda ));
		}
		g.drawImage( ref.mImage, 0, 0, width, height, refrect.left, refrect.top, refrect.right, refrect.bottom, null );
		//boolean ret = g.drawImage( ref.mImage, 0, 0, width, height, refrect.left, refrect.top, refrect.right, refrect.bottom, null );
		//g.drawImage( ref.mImage, trans, null );
		g.dispose();
		return true;
	}
	/**
	 * ボックスブラー
	 * ここでは単純に縮小してから拡大してぼかすだけ
	 * @param cliprect
	 * @param rect
	 * @param isAlpha
	 */
	/* イマイチ
	public boolean doBoxBlur(Rect rect, Rect area, boolean isAlpha ) {
		int w = area.width() / 2 + 1;
		int h = area.height() / 2 + 1;
		if( w <= 1 && h <= 1 ) return false;

		if( w <= 0 ) w = 1;
		if( h <= 0 ) h = 1;
		int sw = rect.width();
		int sh = rect.height();
		w = sw / w;
		h = sh / h;
		if( w <= 0 || h <= 0 ) return false;

		final int type = mImage.getType();
		BufferedImage tmp = new BufferedImage( w, h, type );
		Graphics2D g = (Graphics2D)tmp.getGraphics();
		g.setComposite( AlphaComposite.Src );
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION , RenderingHints.VALUE_INTERPOLATION_BILINEAR );
		g.drawImage( mImage, 0, 0, w, h, rect.left, rect.top, rect.right, rect.bottom, null );
		g.dispose();

		g = (Graphics2D)mImage.getGraphics();
		g.setComposite( AlphaComposite.Src );
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION , RenderingHints.VALUE_INTERPOLATION_BILINEAR );
		g.drawImage( tmp, rect.left, rect.top, rect.right, rect.bottom, 0, 0, w, h, null );
		g.dispose();
		return true;
	}
	*/
	public void doBoxBlurLoop(Rect rect, Rect area) throws TJSException {
		final int w = mImage.getWidth();
		WritableRaster src = mImage.getRaster();
		DataBuffer buff = src.getDataBuffer();
		final int type = buff.getDataType();
		if( type != DataBuffer.TYPE_INT )
			throw new TJSException("Not support image type.");

		DataBufferInt srcBuff = (DataBufferInt)buff;
		int[] s = srcBuff.getData();


		int width = getWidth();
		int height = getHeight();

		int dest_buf_size = area.top <= 0 ? (1-area.top) : 0;

		int vert_sum_left_limit = rect.left + area.left;
		if(vert_sum_left_limit < 0) vert_sum_left_limit = 0;
		int vert_sum_right_limit = (rect.right-1) + area.right;
		if(vert_sum_right_limit >= width) vert_sum_right_limit = width - 1;


		int[] vert_sum = null; // vertical sum of the pixel
		int[][] dest_buf = null; // destination pixel temporary buffer

		int vert_sum_count;

		try {
			// allocate buffers
			vert_sum = new int[(vert_sum_right_limit - vert_sum_left_limit + 1 + 1)*4]; // use 128bit aligned allocation

			if( dest_buf_size != 0 ) {
				dest_buf = new int[dest_buf_size][];
				for( int i = 0; i < dest_buf_size; i++)
					dest_buf[i] = new int[rect.right - rect.left];
			}

			// initialize vert_sum
			{
				/*
				for( int i = vert_sum_right_limit - vert_sum_left_limit + 1 -1; i>=0; i-- )
					vert_sum[i] = 0;
				*/

				int v_init_start = rect.top + area.top;
				if(v_init_start < 0) v_init_start = 0;
				int v_init_end = rect.top + area.bottom;
				if(v_init_end >= height) v_init_end = height - 1;
				vert_sum_count = v_init_end - v_init_start + 1;
				for( int y = v_init_start; y <= v_init_end; y++) {
					int add_line = y * w;
					int vs = 0;
					for(int x = vert_sum_left_limit; x <= vert_sum_right_limit; x++) {
						int sv = s[add_line+x];
						vert_sum[vs+0] +=  sv >>> 24;
						vert_sum[vs+1] += (sv >>> 16) & 0xff;
						vert_sum[vs+2] += (sv >>>  8) & 0xff;
						vert_sum[vs+3] +=  sv         & 0xff;
						vs+=4;
					}
				}
			}

			// prepare variables to be used in following loop
			int h_init_start = rect.left + area.left; // this always be the same value as vert_sum_left_limit
			if(h_init_start < 0) h_init_start = 0;
			int h_init_end = rect.left + area.right;
			if(h_init_end >= width) h_init_end = width - 1;

			int left_frac_len = rect.left + area.left < 0 ? -(rect.left + area.left) : 0;
			int right_frac_len = rect.right + area.right >= width ? rect.right + area.right - width + 1: 0;
			int center_len = rect.right - rect.left - left_frac_len - right_frac_len;

			if( center_len < 0 ) {
				left_frac_len = rect.right - rect.left;
				right_frac_len = 0;
				center_len = 0;
			}
			int left_frac_lim = rect.left + left_frac_len;
			int center_lim = rect.left + left_frac_len + center_len;

			// for each line
			int dest_buf_free = dest_buf_size;
			int dest_buf_wp = 0;

			int[] sum = new int[4];
			for( int y = rect.top; y < rect.bottom; y++ ) {
				// rotate dest_buf
				if( dest_buf_free == 0 ) {
					// dest_buf is full;
					// write last dest_buf back to the bitmap
					System.arraycopy(dest_buf[dest_buf_wp], 0, s, (y-dest_buf_size)*w+rect.left, rect.right - rect.left );
				} else {
					dest_buf_free--;
				}

				// build initial sum
				sum[0] = sum[1] = sum[2] = sum[3] = 0;
				int horz_sum_count = h_init_end - h_init_start + 1;

				for( int x = h_init_start; x <= h_init_end; x++) {
					int off = (x - vert_sum_left_limit) << 2;
					sum[0] += vert_sum[off+0];
					sum[1] += vert_sum[off+1];
					sum[2] += vert_sum[off+2];
					sum[3] += vert_sum[off+3];
				}

				// process a line
				int[] dp = dest_buf[dest_buf_wp];
				int di = 0;
				int x = rect.left;

				//- do left fraction part
				for( ; x < left_frac_lim; x++ ) {
					int div = horz_sum_count * vert_sum_count;
					int a = ((sum[0] / div) << 24) & 0xff000000;
					int r = ((sum[1] / div) << 16) & 0x00ff0000;
					int g = ((sum[2] / div) << 8) & 0x0000ff00;
					int b = (sum[3] / div) & 0xff;
					dp[di] = a | r | g | b;
					di++;
					// avg

					// update sum
					if(x + area.left >= 0) {
						int off = (x + area.left - vert_sum_left_limit) << 2;
						sum[0] -= vert_sum[off+0];
						sum[1] -= vert_sum[off+1];
						sum[2] -= vert_sum[off+2];
						sum[3] -= vert_sum[off+3];
						horz_sum_count--;
					}
					if(x + area.right + 1 < width) {
						int off = (x + area.right + 1 - vert_sum_left_limit) << 2;
						sum[0] += vert_sum[off+0];
						sum[1] += vert_sum[off+1];
						sum[2] += vert_sum[off+2];
						sum[3] += vert_sum[off+3];
						horz_sum_count++;
					}
				}

				//- do center part
				if( center_len > 0 ) {
					// uses function in tvpgl
					doBoxBlurAvg16(dp, di, sum,
						vert_sum, x + area.right + 1 - vert_sum_left_limit,
						x + area.left - vert_sum_left_limit,
						horz_sum_count * vert_sum_count,
						center_len);
					di += center_len;
				}
				x = center_lim;

				//- do right fraction part
				for(; x < rect.right; x++) {
					//tmp.average(horz_sum_count * vert_sum_count);
					int div = horz_sum_count * vert_sum_count;
					int a = ((sum[0] / div) << 24) & 0xff000000;
					int r = ((sum[1] / div) << 16) & 0x00ff0000;
					int g = ((sum[2] / div) << 8) & 0x0000ff00;
					int b = (sum[3] / div) & 0xff;
					dp[di] = a | r | g | b;
					di++;

					// update sum
					if( x + area.left >= 0 ) {
						int off = (x + area.left - vert_sum_left_limit) << 2;
						sum[0] -= vert_sum[off+0];
						sum[1] -= vert_sum[off+1];
						sum[2] -= vert_sum[off+2];
						sum[3] -= vert_sum[off+3];
						horz_sum_count--;
					}
					if(x + area.right + 1 < width) {
						int off = (x + area.right + 1 - vert_sum_left_limit) << 2;
						sum[0] += vert_sum[off+0];
						sum[1] += vert_sum[off+1];
						sum[2] += vert_sum[off+2];
						sum[3] += vert_sum[off+3];
						horz_sum_count++;
					}
				}

				// update vert_sum
				if(y != rect.bottom - 1) {
					int topline = (y + area.top);
					int bottomline = (y + area.bottom + 1);
					if( topline >= 0 && bottomline < height ) {
						// both sub_line and add_line are available uses function in tvpgl
						int sub_line = topline * w;
						int add_line = bottomline * w;
						addSubVertSum16( vert_sum, s,
							add_line + vert_sum_left_limit,
							sub_line + vert_sum_left_limit,
							vert_sum_right_limit - vert_sum_left_limit + 1);
					} else if( topline >= 0 ) {
						// only sub_line is available
						int vs = 0;
						int sub_line = topline * w;
						final int limit = sub_line + vert_sum_right_limit;
						sub_line += vert_sum_left_limit;
						while( sub_line <= limit ) {
							int sv = s[sub_line];
							vert_sum[vs+0] -=  sv >>> 24;
							vert_sum[vs+1] -= (sv >>> 16 ) & 0xff;
							vert_sum[vs+2] -= (sv >>>  8 ) & 0xff;
							vert_sum[vs+3] -=  sv          & 0xff;
							sub_line++;
							vs+=4;
						}
						vert_sum_count--;
					} else if( bottomline < height ) {
						// only add_line is available
						int vs = 0;
						int add_line = bottomline * w;
						final int limit = add_line + vert_sum_right_limit;
						add_line += vert_sum_left_limit;
						while( add_line <= limit ) {
							int sv = s[add_line];
							vert_sum[vs+0] +=  sv >>> 24;
							vert_sum[vs+1] += (sv >>> 16 ) & 0xff;
							vert_sum[vs+2] += (sv >>>  8 ) & 0xff;
							vert_sum[vs+3] +=  sv          & 0xff;
							add_line++;
							vs+=4;
						}
						vert_sum_count++;
					}
				}

				// step dest_buf_wp
				dest_buf_wp++;
				if(dest_buf_wp >= dest_buf_size) dest_buf_wp = 0;
			}

			// write remaining dest_buf back to the bitmap
			while( dest_buf_free < dest_buf_size ) {
				System.arraycopy(dest_buf[dest_buf_wp], 0, s, (rect.bottom - (dest_buf_size - dest_buf_free))*w+rect.left, rect.right - rect.left );
				dest_buf_wp++;
				if(dest_buf_wp >= dest_buf_size) dest_buf_wp = 0;
				dest_buf_free++;
			}
		} finally {
			// exception caught
			vert_sum = null;
			dest_buf = null;
		}
	}
	/**
	 * doBoxBlurLoop と呼び出しているメソッドが違うだけで他は同じ
	 * @param rect
	 * @param area
	 * @throws TJSException
	 */
	public void doBoxBlurLoopAlpha(Rect rect, Rect area) throws TJSException {
		final int w = mImage.getWidth();
		WritableRaster src = mImage.getRaster();
		DataBuffer buff = src.getDataBuffer();
		final int type = buff.getDataType();
		if( type != DataBuffer.TYPE_INT )
			throw new TJSException("Not support image type.");

		DataBufferInt srcBuff = (DataBufferInt)buff;
		int[] s = srcBuff.getData();


		int width = getWidth();
		int height = getHeight();

		int dest_buf_size = area.top <= 0 ? (1-area.top) : 0;

		int vert_sum_left_limit = rect.left + area.left;
		if(vert_sum_left_limit < 0) vert_sum_left_limit = 0;
		int vert_sum_right_limit = (rect.right-1) + area.right;
		if(vert_sum_right_limit >= width) vert_sum_right_limit = width - 1;


		int[] vert_sum = null; // vertical sum of the pixel
		int[][] dest_buf = null; // destination pixel temporary buffer

		int vert_sum_count;

		try {
			// allocate buffers
			vert_sum = new int[(vert_sum_right_limit - vert_sum_left_limit + 1 + 1)*4]; // use 128bit aligned allocation

			if( dest_buf_size != 0 ) {
				dest_buf = new int[dest_buf_size][];
				for( int i = 0; i < dest_buf_size; i++)
					dest_buf[i] = new int[rect.right - rect.left];
			}

			// initialize vert_sum
			{
				/*
				for( int i = vert_sum_right_limit - vert_sum_left_limit + 1 -1; i>=0; i-- )
					vert_sum[i] = 0;
				*/

				int v_init_start = rect.top + area.top;
				if(v_init_start < 0) v_init_start = 0;
				int v_init_end = rect.top + area.bottom;
				if(v_init_end >= height) v_init_end = height - 1;
				vert_sum_count = v_init_end - v_init_start + 1;
				for( int y = v_init_start; y <= v_init_end; y++) {
					int add_line = y * w;
					int vs = 0;
					for(int x = vert_sum_left_limit; x <= vert_sum_right_limit; x++) {
						int add = s[add_line+x];
						int add_a = add >>> 24;
						vert_sum[vs+0] += add_a;
						add_a += add_a >>> 7;

						vert_sum[vs+1] += ((((add >>> 16) & 0xff) * add_a) >>> 8);
						vert_sum[vs+2] += ((((add >>>  8) & 0xff) * add_a) >>> 8);
						vert_sum[vs+3] += ((( add & 0xff)         * add_a) >>> 8);
						vs+=4;
					}
				}
			}

			// prepare variables to be used in following loop
			int h_init_start = rect.left + area.left; // this always be the same value as vert_sum_left_limit
			if(h_init_start < 0) h_init_start = 0;
			int h_init_end = rect.left + area.right;
			if(h_init_end >= width) h_init_end = width - 1;

			int left_frac_len = rect.left + area.left < 0 ? -(rect.left + area.left) : 0;
			int right_frac_len = rect.right + area.right >= width ? rect.right + area.right - width + 1: 0;
			int center_len = rect.right - rect.left - left_frac_len - right_frac_len;

			if( center_len < 0 ) {
				left_frac_len = rect.right - rect.left;
				right_frac_len = 0;
				center_len = 0;
			}
			int left_frac_lim = rect.left + left_frac_len;
			int center_lim = rect.left + left_frac_len + center_len;

			// for each line
			int dest_buf_free = dest_buf_size;
			int dest_buf_wp = 0;

			int[] sum = new int[4];
			for( int y = rect.top; y < rect.bottom; y++ ) {
				// rotate dest_buf
				if( dest_buf_free == 0 ) {
					// dest_buf is full;
					// write last dest_buf back to the bitmap
					System.arraycopy(dest_buf[dest_buf_wp], 0, s, (y-dest_buf_size)*w+rect.left, rect.right - rect.left );
				} else {
					dest_buf_free--;
				}

				// build initial sum
				sum[0] = sum[1] = sum[2] = sum[3] = 0;
				int horz_sum_count = h_init_end - h_init_start + 1;

				for( int x = h_init_start; x <= h_init_end; x++) {
					int off = (x - vert_sum_left_limit) << 2;
					sum[0] += vert_sum[off+0];
					sum[1] += vert_sum[off+1];
					sum[2] += vert_sum[off+2];
					sum[3] += vert_sum[off+3];
				}

				// process a line
				int[] dp = dest_buf[dest_buf_wp];
				int di = 0;
				int x = rect.left;

				//- do left fraction part
				for( ; x < left_frac_lim; x++ ) {
					int div = horz_sum_count * vert_sum_count;
					int a = ((sum[0] / div) << 24) & 0xff000000;
					int r = ((sum[1] / div) << 16) & 0x00ff0000;
					int g = ((sum[2] / div) << 8) & 0x0000ff00;
					int b = (sum[3] / div) & 0xff;
					dp[di] = a | r | g | b;
					di++;
					// avg

					// update sum
					if( (x + area.left) >= 0) {
						int off = (x + area.left - vert_sum_left_limit) << 2;
						sum[0] -= vert_sum[off+0];
						sum[1] -= vert_sum[off+1];
						sum[2] -= vert_sum[off+2];
						sum[3] -= vert_sum[off+3];
						horz_sum_count--;
					}
					if( (x + area.right + 1) < width) {
						int off = (x + area.right + 1 - vert_sum_left_limit) << 2;
						sum[0] += vert_sum[off+0];
						sum[1] += vert_sum[off+1];
						sum[2] += vert_sum[off+2];
						sum[3] += vert_sum[off+3];
						horz_sum_count++;
					}
				}

				//- do center part
				if( center_len > 0 ) {
					// uses function in tvpgl
					doBoxBlurAvg16_d(dp, di, sum,
						vert_sum, x + area.right + 1 - vert_sum_left_limit,
						x + area.left - vert_sum_left_limit,
						horz_sum_count * vert_sum_count,
						center_len);
					di += center_len;
				}
				x = center_lim;

				//- do right fraction part
				for(; x < rect.right; x++) {
					//tmp.average(horz_sum_count * vert_sum_count);
					int div = horz_sum_count * vert_sum_count;
					int a = ((sum[0] / div) << 24) & 0xff000000;
					int r = ((sum[1] / div) << 16) & 0x00ff0000;
					int g = ((sum[2] / div) << 8) & 0x0000ff00;
					int b = (sum[3] / div) & 0xff;
					dp[di] = a | r | g | b;
					di++;

					// update sum
					if( (x + area.left) >= 0 ) {
						int off = (x + area.left - vert_sum_left_limit) << 2;
						sum[0] -= vert_sum[off+0];
						sum[1] -= vert_sum[off+1];
						sum[2] -= vert_sum[off+2];
						sum[3] -= vert_sum[off+3];
						horz_sum_count--;
					}
					if( (x + area.right + 1) < width) {
						int off = (x + area.right + 1 - vert_sum_left_limit) << 2;
						sum[0] += vert_sum[off+0];
						sum[1] += vert_sum[off+1];
						sum[2] += vert_sum[off+2];
						sum[3] += vert_sum[off+3];
						horz_sum_count++;
					}
				}

				// update vert_sum
				if(y != rect.bottom - 1) {
					int topline = (y + area.top);
					int bottomline = (y + area.bottom + 1);
					if( topline >= 0 && bottomline < height ) {
						int sub_line = topline * w;
						int add_line = bottomline * w;
						// both sub_line and add_line are available
						// uses function in tvpgl
						addSubVertSum16_d( vert_sum, s,
							add_line + vert_sum_left_limit,
							sub_line + vert_sum_left_limit,
							vert_sum_right_limit - vert_sum_left_limit + 1);
					} else if( topline >= 0 ) {
						int sub_line = topline * w + vert_sum_left_limit;
						// only sub_line is available
						int vs = 0;
						for(int i = vert_sum_left_limit; i <= vert_sum_right_limit; i++) {
							int sub = s[sub_line];
							int sub_a = sub >>> 24;
							vert_sum[vs+0] -= sub_a;
							sub_a += sub_a >>> 7;
							vert_sum[vs+1] -= ((((sub >>> 16) & 0xff) * sub_a) >>> 8);
							vert_sum[vs+2] -= ((((sub >>>  8) & 0xff) * sub_a) >>> 8);
							vert_sum[vs+3] -= ((( sub & 0xff)         * sub_a) >>> 8);
							vs+=4;
							sub_line++;
						}
						vert_sum_count--;
					} else if( bottomline < height ) {
						int add_line = bottomline * w + vert_sum_left_limit;
						// only add_line is available
						int vs = 0;
						for(int i = vert_sum_left_limit; i <= vert_sum_right_limit; i++) {
							int add = s[add_line];
							int add_a = add >>> 24;
							vert_sum[vs+0] += add_a;
							add_a += add_a >>> 7;

							vert_sum[vs+1] += ((((add >>> 16) & 0xff) * add_a) >>> 8);
							vert_sum[vs+2] += ((((add >>>  8) & 0xff) * add_a) >>> 8);
							vert_sum[vs+3] += ((( add & 0xff)         * add_a) >>> 8);
							vs+=4;
							add_line++;
						}
						vert_sum_count++;
					}
				}

				// step dest_buf_wp
				dest_buf_wp++;
				if(dest_buf_wp >= dest_buf_size) dest_buf_wp = 0;
			}

			// write remaining dest_buf back to the bitmap
			while( dest_buf_free < dest_buf_size ) {
				System.arraycopy(dest_buf[dest_buf_wp], 0, s, (rect.bottom - (dest_buf_size - dest_buf_free))*w+rect.left, rect.right - rect.left );
				dest_buf_wp++;
				if(dest_buf_wp >= dest_buf_size) dest_buf_wp = 0;
				dest_buf_free++;
			}
		} finally {
			// exception caught
			vert_sum = null;
			dest_buf = null;
		}
	}
	private static final void addSubVertSum16(int[] dest, int[] s, int addline, int subline, int len) {
		int d = 0;
		len += addline;
		while( addline < len ) {
			int add = s[addline];
			int sub = s[subline];

			dest[d+0] += ( add >>> 24         ) - ( sub >>> 24         );
			dest[d+1] += ((add >>> 16 ) & 0xff) - ((sub >>> 16 ) & 0xff);
			dest[d+2] += ((add >>>  8 ) & 0xff) - ((sub >>>  8 ) & 0xff);
			dest[d+3] += ( add          & 0xff) - ( sub          & 0xff);
			d += 4;
			addline++;
			subline++;
		}
	}
	private static final void addSubVertSum16_d(int[] dest, int[] s, int addline, int subline, int len) {
		int d = 0;
		len += addline;
		while( addline < len ) {
			int add = s[addline];
			int sub = s[subline];
			int add_a = add >>> 24;
			int sub_a = sub >>> 24;
			dest[d+0] += add_a - sub_a;
			add_a += add_a >>> 7;
			sub_a += sub_a >>> 7;

			dest[d+1] += ((((add >>> 16) & 0xff) * add_a) >>> 8) - ((((sub >>> 16) & 0xff) * sub_a) >>> 8);
			dest[d+2] += ((((add >>>  8) & 0xff) * add_a) >>> 8) - ((((sub >>>  8) & 0xff) * sub_a) >>> 8);
			dest[d+3] += ((( add & 0xff)         * add_a) >>> 8) - ((( sub & 0xff)         * sub_a) >>> 8);
			d += 4;
			addline++;
			subline++;
		}
	}
	private static final void doBoxBlurAvg16(int[] dest, int desti, int[] sum, int[] vert_sum,
			int addi, int subi, int n, int len) {

		int rcp = (1<<16)/n;
		int half_n = n >>> 1;
		addi <<= 2;
		subi <<= 2;
		len += desti;
		while( desti < len ) {
			dest[desti] =
				(((sum[0] + half_n) * rcp >>> 16) << 24 )+
				(((sum[1] + half_n) * rcp >>> 16) << 16 )+
				(((sum[2] + half_n) * rcp >>> 16) <<  8 )+
				(((sum[3] + half_n) * rcp >>> 16));

			sum[0] += vert_sum[addi+0] - vert_sum[subi+0];
			sum[1] += vert_sum[addi+1] - vert_sum[subi+1];
			sum[2] += vert_sum[addi+2] - vert_sum[subi+2];
			sum[3] += vert_sum[addi+3] - vert_sum[subi+3];
			desti++;
			addi+=4;
			subi+=4;
		}
	}
	private static final void doBoxBlurAvg16_d(int[] dest, int desti, int[] sum, int[] vert_sum,
			int addi, int subi, int n, int len) {

		byte[] table = CustomOperationComposite.DivTable;
		int rcp = (1<<16)/n;
		int half_n = n >>> 1;
		addi <<= 2;
		subi <<= 2;
		len += desti;
		while( desti < len ) {
			int a = (sum[0] + half_n) * rcp >>> 16;
			int t = a << 8;
			int r = sum[1];
			int g = sum[2];
			int b = sum[3];

			r = ((int)table[t+((r + half_n) * rcp >>> 16)]) & 0xff;
			g = ((int)table[t+((g + half_n) * rcp >>> 16)]) & 0xff;
			b = ((int)table[t+((b + half_n) * rcp >>> 16)]) & 0xff;

			dest[desti] = a << 24 | r << 16 | g << 8 | b;

			sum[0] += vert_sum[addi+0] - vert_sum[subi+0];
			sum[1] += vert_sum[addi+1] - vert_sum[subi+1];
			sum[2] += vert_sum[addi+2] - vert_sum[subi+2];
			sum[3] += vert_sum[addi+3] - vert_sum[subi+3];
			desti++;
			addi+=4;
			subi+=4;
		}
	}
	public void adjustGamma(Rect rect,GammaAdjustTempData tmp) throws TJSException {
		final int w = mImage.getWidth();
		WritableRaster src = mImage.getRaster();
		DataBuffer buff = src.getDataBuffer();
		final int type = buff.getDataType();
		if( type == DataBuffer.TYPE_INT ) {
			DataBufferInt srcBuff = (DataBufferInt)buff;
			byte[] R = tmp.R;
			byte[] G = tmp.G;
			byte[] B = tmp.B;
			int[] s = srcBuff.getData();
			int ystart = rect.top*w + rect.left;
			final int rw = rect.right-rect.left;
			final int rh = (rect.bottom-rect.top);
			for( int y = 0; y < rh; y++ ) {
				int sp = ystart;
				for( int x = 0; x < rw; x++ ) {
					int d1 = s[sp];
					int a = d1 & 0xff000000;
					if( a != 0 ) {
						// process only non-fully-transparent pixel
						int t1 = ((int)B[d1 & 0xff]) & 0xff;
						d1 >>>= 8;
						t1 |= (((int)G[d1 & 0xff])&0xff) << 8;
						d1 >>>= 8;
						t1 |= (((int)R[d1 & 0xff])&0xff) << 16;
						t1 |= a;
						s[sp] = t1;
					}
					sp++;
				}
				ystart += w;
			}
		} else {
			throw new TJSException("Not support image type.");
		}
	}
	public void adjustGammaForAdditiveAlpha(Rect rect, GammaAdjustTempData tmp) throws TJSException {
		final int w = mImage.getWidth();
		WritableRaster src = mImage.getRaster();
		DataBuffer buff = src.getDataBuffer();
		final int type = buff.getDataType();
		if( type == DataBuffer.TYPE_INT ) {
			DataBufferInt srcBuff = (DataBufferInt)buff;
			byte[] R = tmp.R;
			byte[] G = tmp.G;
			byte[] B = tmp.B;
			short[] table = CustomOperationComposite.RecipTable256_16;
			int[] s = srcBuff.getData();
			int ystart = rect.top*w + rect.left;
			final int rw = rect.right-rect.left;
			final int rh = (rect.bottom-rect.top);
			for( int y = 0; y < rh; y++ ) {
				int sp = ystart;
				for( int x = 0; x < rw; x++ ) {
					int d = s[sp];
					int a = d & 0xff000000;
					if( a == 0xff000000 ) {
						// completely opaque
						int t1 = ((int)B[d & 0xff]) & 0xff;
						d >>>= 8;
						t1 |= (((int)G[d & 0xff])&0xff) << 8;
						d >>>= 8;
						t1 |= (((int)R[d & 0xff])&0xff) << 16;
						t1 |= a;
						s[sp] = t1;
					} else if( a != 0 ) {
						// not completely transparent
						int alpha = a >>> 24;
						int alpha_adj = alpha + (alpha >>> 7);
						int recip = table[alpha];
						int d_tmp;

						/* B */
						int t = d & 0xff;
						if(t > alpha)
							d_tmp = (B[255] * alpha_adj >>> 8) + t - alpha;
						else
							d_tmp = B[recip * t >>> 8] * alpha_adj >>> 8;
						// G
						t = (d>>>8) & 0xff;
						if(t > alpha)
							d_tmp |= ((G[255] * alpha_adj >>> 8) + t - alpha) << 8;
						else
							d_tmp |= (G[recip * t >>> 8] * alpha_adj >>> 8) << 8;
						// R
						t = (d>>>16) & 0xff;
						if(t > alpha)
							d_tmp |= ((R[255] * alpha_adj >>> 8) + t - alpha) << 16;
						else
							d_tmp |= (R[recip * t >>> 8] * alpha_adj >>> 8) << 16;
						// A
						d_tmp |= a;
						s[sp] = d_tmp;
					}
					sp++;
				}
				ystart += w;
			}
		} else {
			throw new TJSException("Not support image type.");
		}
	}
	public void doGrayScale(Rect rect) throws TJSException {
		final int w = mImage.getWidth();
		WritableRaster src = mImage.getRaster();
		DataBuffer buff = src.getDataBuffer();
		final int type = buff.getDataType();
		if( type == DataBuffer.TYPE_INT ) {
			DataBufferInt srcBuff = (DataBufferInt)buff;
			int[] s = srcBuff.getData();
			int ystart = rect.top*w + rect.left;
			final int rw = rect.right-rect.left;
			final int rh = (rect.bottom-rect.top);
			for( int y = 0; y < rh; y++ ) {
				int sp = ystart;
				for( int x = 0; x < rw; x++ ) {
					int s1 = s[sp];
					int d1 = (s1&0xff)*19;
					d1 += ((s1 >>> 8)&0xff)*183;
					d1 += ((s1 >>> 16)&0xff)*54;
					d1 = (d1 >>> 8) * 0x10101 + (s1 & 0xff000000);
					s[sp] = d1;
					sp++;
				}
				ystart += w;
			}
		} else {
			throw new TJSException("Not support image type.");
		}
	}
	public void drawFontImage(byte[] bp, int pitch, Rect srect, Rect drect,
			int color, int bltmode, int opa, boolean holdalpha) throws TJSException {

		final int sh = drect.bottom - drect.top;
		final int sw = drect.right - drect.left;
		int so = pitch * srect.top + srect.left;

		final int w = mImage.getWidth();
		WritableRaster src = mImage.getRaster();
		DataBuffer buff = src.getDataBuffer();
		final int type = buff.getDataType();
		if( type == DataBuffer.TYPE_INT ) {
			DataBufferInt srcBuff = (DataBufferInt)buff;
			int[] s = srcBuff.getData();
			int ystart = drect.top*w + drect.left;
			final int rw = drect.right-drect.left;
			final int rh = (drect.bottom-drect.top);
			final int c1 = color & 0xff00ff;
			color = color & 0x00ff00;
			if( bltmode == LayerNI.bmAlphaOnAlpha ) {
				if( opa > 0 ) {
					byte[] otable = CustomOperationComposite.OpacityOnOpacityTable65;
					byte[] ntable = CustomOperationComposite.NegativeMulTable65;
					if( opa == 255 ) {
						// TVPApplyColorMap65_d_c
						for( int y = 0; y < rh; y++ ) {
							int dp = ystart;
							int sp = so;
							for( int x = 0; x < rw; x++ ) {
								int d = s[dp];
								int s1 = bp[sp];
								int addr = (s1<<8) + (d>>>24);
								int destalpha = (((int)ntable[addr])&0xff)<<24;
								int sopa = ((int)otable[addr])&0xff;
								int d1 = d & 0xff00ff;
								d1 = (d1 + ((c1 - d1) * sopa >>> 8)) & 0xff00ff;
								d &= 0x00ff00;
								s[dp] = d1 | ((d + ((color - d) * sopa >>> 8)) & 0x00ff00) | destalpha;
								sp++;
								dp++;
							}
							ystart += w;
							so += pitch;
						}
					} else {
						// TVPApplyColorMap65_do
						for( int y = 0; y < rh; y++ ) {
							int dp = ystart;
							int sp = so;
							for( int x = 0; x < rw; x++ ) {
								int d = s[dp];
								int s1 = bp[sp];
								int addr = ((s1 * opa) & 0xff00) + (d>>>24);
								int destalpha = (((int)ntable[addr])&0xff)<<24;
								int sopa = ((int)otable[addr])&0xff;
								int d1 = d & 0xff00ff;
								d1 = (d1 + ((c1 - d1) * sopa >>> 8)) & 0xff00ff;
								d &= 0x00ff00;
								s[dp] = d1 + ((d + ((color - d) * sopa >>> 8)) & 0x00ff00) + destalpha;
								sp++;
								dp++;
							}
							ystart += w;
							so += pitch;
						}
					}
				} else {
					if( opa == -255 ) {
						// TVPRemoveOpacity65
						for( int y = 0; y < rh; y++ ) {
							int dp = ystart;
							int sp = so;
							for( int x = 0; x < rw; x++ ) {
								int d = s[dp];
								int s1 = bp[sp];
								s[dp] = (d & 0xffffff) + ( (((d>>>24) * (64-s1)) << 18) & 0xff000000);
								sp++;
								dp++;
							}
							ystart += w;
							so += pitch;
						}
					} else {
						// TVPRemoveOpacity65_o
						int strength = -opa;
						if(strength > 127) strength++; // adjust for error
						for( int y = 0; y < rh; y++ ) {
							int dp = ystart;
							int sp = so;
							for( int x = 0; x < rw; x++ ) {
								int d = s[dp];
								int s1 = bp[sp];
								s[dp] = (d & 0xffffff) + ( (((d>>>24) * (16384-s1*strength )) << 10) & 0xff000000);
								sp++;
								dp++;
							}
							ystart += w;
							so += pitch;
						}
					}
				}
			} else if( bltmode == LayerNI.bmAlphaOnAddAlpha ) {
				if( opa == 255 ) {
					// TVPApplyColorMap65_a
					for( int y = 0; y < rh; y++ ) {
						int dp = ystart;
						int sp = so;
						for( int x = 0; x < rw; x++ ) {
							int d = s[dp];
							int s1 = bp[sp];
							int s_tmp = s1;
							int tmp =
								((s_tmp * (c1    & 0xff00ff) >>> 6) & 0xff00ff) +
								((s_tmp * (color & 0x00ff00) >>> 6) & 0x00ff00);
							s_tmp <<= (8 - 6);
							s_tmp -= (s_tmp >>> 8); // adjust alpha

							// TVPAddAlphaBlend_a_ca
							int sopa_inv = s_tmp ^ 0xff;
							int dopa = d >>> 24;
							dopa = dopa + s_tmp - (dopa*s_tmp >>> 8);
							dopa -= (dopa >>> 8); // adjust alpha

							// TVPSaturatedAdd
							int a = (((d & 0xff00ff)*sopa_inv >>> 8) & 0xff00ff) + (((d & 0xff00)*sopa_inv >>> 8) & 0xff00);
							int b = tmp;
							int tmp1 = (  ( a & b ) + ( ((a ^ b)>>>1) & 0x7f7f7f7f)  ) & 0x80808080;
							tmp1 = (tmp1<<1) - (tmp1>>>7);
							tmp1 = (a + b - tmp1) | tmp1;

							s[dp] = (dopa << 24) + tmp1;
							sp++;
							dp++;
						}
						ystart += w;
						so += pitch;
					}
				} else {
					// TVPApplyColorMap65_ao
					for( int y = 0; y < rh; y++ ) {
						int dp = ystart;
						int sp = so;
						for( int x = 0; x < rw; x++ ) {
							int d = s[dp];
							int s1 = bp[sp];
							int s_tmp = (s1 * opa) >>> 8;
							int tmp =
								((s_tmp * (c1    & 0xff00ff) >>> 6) & 0xff00ff) +
								((s_tmp * (color & 0x00ff00) >>> 6) & 0x00ff00);
							s_tmp <<= (8 - 6);
							s_tmp -= (s_tmp >>> 8); // adjust alpha

							// TVPAddAlphaBlend_a_ca
							int sopa_inv = s_tmp ^ 0xff;
							int dopa = d >>> 24;
							dopa = dopa + s_tmp - (dopa*s_tmp >>> 8);
							dopa -= (dopa >>> 8); // adjust alpha

							// TVPSaturatedAdd
							int a = (((d & 0xff00ff)*sopa_inv >>> 8) & 0xff00ff) + (((d & 0xff00)*sopa_inv >>> 8) & 0xff00);
							int b = tmp;
							int tmp1 = (  ( a & b ) + ( ((a ^ b)>>>1) & 0x7f7f7f7f)  ) & 0x80808080;
							tmp1 = (tmp1<<1) - (tmp1>>>7);
							tmp1 = (a + b - tmp1) | tmp1;

							s[dp] = (dopa << 24) + tmp1;
							sp++;
							dp++;
						}
						ystart += w;
						so += pitch;
					}
				}
			} else {
				if( opa == 255 ) {
					if( holdalpha ) {
						// TVPApplyColorMap65_HDA
						for( int y = 0; y < rh; y++ ) {
							int dp = ystart;
							int sp = so;
							for( int x = 0; x < rw; x++ ) {
								int d = s[dp];
								int s1 = bp[sp];
								int sopa = s1;
								int d1 = d & 0xff00ff;
								d1 = ((d1 + ((c1 - d1) * sopa >>> 6)) & 0xff00ff) + (d & 0xff000000);
								d &= 0xff00;
								s[dp] = d1 | ((d + ((color - d) * sopa >>> 6)) & 0x00ff00);
								sp++;
								dp++;
							}
							ystart += w;
							so += pitch;
						}
					} else {
						// TVPApplyColorMap65
						for( int y = 0; y < rh; y++ ) {
							int dp = ystart;
							int sp = so;
							for( int x = 0; x < rw; x++ ) {
								int d = s[dp];
								int s1 = bp[sp];
								int sopa = s1;
								int d1 = d & 0xff00ff;
								d1 = ((d1 + ((c1 - d1) * sopa >>> 6)) & 0xff00ff);
								d &= 0xff00;
								s[dp] = d1 | ((d + ((color - d) * sopa >>> 6)) & 0x00ff00) | 0xff000000;
								sp++;
								dp++;
							}
							ystart += w;
							so += pitch;
						}
					}
				} else {
					if( holdalpha ) {
						// TVPApplyColorMap65_HDA_o
						for( int y = 0; y < rh; y++ ) {
							int dp = ystart;
							int sp = so;
							for( int x = 0; x < rw; x++ ) {
								int d = s[dp];
								int s1 = bp[sp];
								int sopa = (s1 * opa) >>> 8;
								int d1 = d & 0xff00ff;
								d1 = ((d1 + ((c1 - d1) * sopa >>> 6)) & 0xff00ff) + (d & 0xff000000);
								d &= 0x00ff00;
								s[dp] = d1 | ((d + ((color - d) * sopa >>> 6)) & 0x00ff00);
								sp++;
								dp++;
							}
							ystart += w;
							so += pitch;
						}
					} else {
						// TVPApplyColorMap65_o
						for( int y = 0; y < rh; y++ ) {
							int dp = ystart;
							int sp = so;
							for( int x = 0; x < rw; x++ ) {
								int d = s[dp];
								int s1 = bp[sp];
								int sopa = (s1 * opa) >>> 8;
								int d1 = d & 0xff00ff;
								d1 = ((d1 + ((c1 - d1) * sopa >>> 6)) & 0xff00ff);
								d &= 0x00ff00;
								s[dp] = d1 | ((d + ((color - d) * sopa >>> 6)) & 0x00ff00);
								sp++;
								dp++;
							}
							ystart += w;
							so += pitch;
						}
					}
				}
			}
		} else {
			throw new TJSException("Not support image type.");
		}
	}
	private static final void bubbleSort( int[] a, int n ) {
		for( int i = 0; i < n - 1; i++) {
			for( int j = n - 1; j > i; j--) {
				int prev = a[j-1];
				int cur = a[j];
				if( prev > cur ) {	// 前の要素の方が大きかったら
					// 交換する
					a[j] = prev;
					a[j - 1] = cur;
				}
			}
		}
	}
	public void makeAlphaFromAdaptiveColor() throws TJSException {
		final int w = mImage.getWidth();
		WritableRaster src = mImage.getRaster();
		DataBuffer buff = src.getDataBuffer();
		final int type = buff.getDataType();
		if( type == DataBuffer.TYPE_INT ) {
			DataBufferInt srcBuff = (DataBufferInt)buff;
			int[] s = srcBuff.getData();
			int[] buffer = new int[w];
			for( int i = 0; i < w; i++ ) {
				buffer[i] = s[i] & 0x00ffffff;
			}
			bubbleSort( buffer, w ); // ソート

			// もっとも使われている色を検索
			int maxlen = 0;
			int maxlencolor = -1;
			int pcolor = -1;
			int l = 0;
			for( int i = 0; i < w+1; i++ ) {
				if( buffer[i] != pcolor ) {
					if( maxlen < l ) {
						maxlen = l;
						maxlencolor = pcolor;
						l = 0;
					}
				} else {
					l++;
				}
				pcolor = buffer[i];
			}
			if( maxlencolor == -1 ) {
				// may color be not found...
				maxlencolor = 0; // black is a default colorkey
			}
			makeAlphaFromKey( s, maxlencolor );
		} else {
			throw new TJSException("Not support image type.");
		}
	}
	private void makeAlphaFromKey( int[] s, int key ) {
		final int count = s.length;
		for( int i = 0; i < count; i++ ) {
			int a = s[i] & 0x00ffffff;
			if( a != key ) a |= 0xff000000;
			s[i] = a;
		}
	}
	public void makeAlphaFromKeyColor( int color ) throws TJSException {
		WritableRaster src = mImage.getRaster();
		DataBuffer buff = src.getDataBuffer();
		final int type = buff.getDataType();
		if( type == DataBuffer.TYPE_INT ) {
			DataBufferInt srcBuff = (DataBufferInt)buff;
			int[] s = srcBuff.getData();
			color &= 0x00ffffff;
			makeAlphaFromKey( s, color );
		} else {
			throw new TJSException("Not support image type.");
		}
	}
	/**
	 * Do alpha matting.
	 * 'mat' means underlying color of the image. This function piles
	 * specified color under the image, then blend. The output image
	 * will be totally opaque. This function always assumes the image
	 * has pixel value for alpha blend mode, not additive alpha blend mode.
	 * @param color
	 * @throws TJSException
	 */
	public void doAlphaColorMat( int color ) throws TJSException {

		WritableRaster src = mImage.getRaster();
		DataBuffer buff = src.getDataBuffer();
		final int type = buff.getDataType();
		if( type == DataBuffer.TYPE_INT ) {
			DataBufferInt srcBuff = (DataBufferInt)buff;
			int[] dest = srcBuff.getData();
			final int length = dest.length;
			for( int i = 0; i < length; i++ ) {
				int s = dest[i];
				int d = color;
				int sopa = s >>> 24;
				int d1 = d & 0xff00ff;
				d1 = (d1 + (((s & 0xff00ff) - d1) * sopa >>> 8)) & 0xff00ff;
				d &= 0xff00;
				s &= 0xff00;
				dest[i] = d1 + ((d + ((s - d) * sopa >>> 8)) & 0xff00) + 0xff000000;
			}
		} else {
			throw new TJSException("Not support image type.");
		}
	}
	// PC 版は何もしない
	public void toMutable() {}
	// PC 版は常に確保済み
	public boolean isImageAllocated() { return true; }
	// PC 版は何もしない
	public void purgeImage() {}
}
