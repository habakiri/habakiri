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

import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Arrays;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuffXfermode;
import android.graphics.PorterDuff;
import android.graphics.Xfermode;
import android.graphics.Paint.FontMetrics;
import android.os.Debug;
import android.util.Log;

import jp.kirikiri.tjs2.BinaryStream;
import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tvp2.TVP;
import jp.kirikiri.tvp2.base.CompactEventCallbackInterface;
import jp.kirikiri.tvp2.base.Storage;
import jp.kirikiri.tvp2.msg.Message;
import jp.kirikiri.tvp2.utils.DebugClass;
import jp.kirikiri.tvp2.visual.AffineMatrix2D;
import jp.kirikiri.tvp2.visual.BaseBitmap;
import jp.kirikiri.tvp2.visual.CharacterData;
import jp.kirikiri.tvp2.visual.ComplexRect;
import jp.kirikiri.tvp2.visual.GammaAdjustTempData;
import jp.kirikiri.tvp2.visual.LayerNI;
import jp.kirikiri.tvp2.visual.Rect;


public class NativeImageBuffer {
	private static final boolean USE_FIX_POINT_TABLE_CALC = true;
	public static boolean LATE_IMAGE_ALLOCATE = true; // 遅延確保するかどうか

	public static byte[] OpacityOnOpacityTable;
	public static byte[] NegativeMulTable;

	public static byte[] OpacityOnOpacityTable65;
	public static byte[] NegativeMulTable65;

	public static int[] RecipTable256;
	public static short[] RecipTable256_16;

	public static byte[] DivTable;
	public static byte[][] PsTableSoftLight;
	public static byte[][] PsTableColorDodge;
	public static byte[][] PsTableColorBurn;


	private static byte[][] Color252DitherPalette;
	private static byte[][][][] DitherTable_676;
	private static final byte Dither4x4[][] = {
		 {   0, 12,  2, 14   },
		 {   8,  4, 10,  6   },
		 {   3, 15,  1, 13   },
		 {  11,  7,  9,  5   }};

	private static int[] TextBuf;
	private static Bitmap FontTempImage;
	private static final int FontTempImageWidth = 64;
	private static final int FontTempImageHeight= 64;
	private static Paint CachePaint;
	private static Canvas FontTempCanvas;
	private static char[] FontChBuff;
	private static android.graphics.Rect FontTextBounds;
	private static PorterDuffXfermode PorterSrcMode;
	private static PorterDuffXfermode PorterSrcOverMode;
	private static PorterDuffXfermode PorterDarkenMode;
	private static PorterDuffXfermode PorterLightenMode;
	private static PorterDuffXfermode PorterMultiplyMode;
	private static PorterDuffXfermode PorterScreenMode;
	private static ArrayList<WeakReference<NativeImageBuffer>> mAllocImages;
	public static void initialize() {
		TextBuf = null;
		FontTempImage = null;
		FontTempCanvas = null;
		CachePaint = null;
		FontChBuff = null;
		FontTextBounds = null;
		PsTableSoftLight = null;
		PsTableColorDodge = null;
		PsTableColorBurn = null;
		Color252DitherPalette = null;
		DitherTable_676 = null;

		OpacityOnOpacityTable = new byte[256*256];
		NegativeMulTable = new byte[256*256];
		RecipTable256 = new int[256];
		RecipTable256_16 = new short[256];
		DivTable = new byte[256*256];
		OpacityOnOpacityTable65 = new byte[65*256];
		NegativeMulTable65 = new byte[65*256];

		PorterSrcMode = new PorterDuffXfermode( PorterDuff.Mode.SRC );
		PorterSrcOverMode = new PorterDuffXfermode( PorterDuff.Mode.SRC_OVER );
		PorterDarkenMode = new PorterDuffXfermode( PorterDuff.Mode.DARKEN );
		PorterLightenMode = new PorterDuffXfermode( PorterDuff.Mode.LIGHTEN );
		PorterMultiplyMode = new PorterDuffXfermode( PorterDuff.Mode.MULTIPLY );
		PorterScreenMode = new PorterDuffXfermode( PorterDuff.Mode.SCREEN );

		mAllocImages = new ArrayList<WeakReference<NativeImageBuffer>>();
		int a,b;
		//long start = System.currentTimeMillis();
		if( USE_FIX_POINT_TABLE_CALC == false ) {
			for( a = 0; a < 256; a++ ) {
				for( b = 0; b < 256; b++ ) {
					float c;
					int ci;
					int addr = b*256+ a;
					if( a != 0 ) {
						float at = a/255.0f, bt = b/255.0f;
						c = bt / at;
						c /= (1.0 - bt + c);
						ci = (int)(c*255);
						if(ci>=256) ci = 255; /* will not overflow... */
					} else {
						ci=255;
					}
					OpacityOnOpacityTable[addr]=(byte)(ci&0xff);
						/* higher byte of the index is source opacity */
						/* lower byte of the index is destination opacity */
					NegativeMulTable[addr] = (byte)(( 255 - (255-a)*(255-b)/ 255 )&0xff);
				}
			}

			for( a = 0; a < 256; a++ ) {
				for( b = 0; b < 65; b++ ) {
					float c;
					int ci;
					int addr = b*256+ a;
					int bb;
					if( a != 0 ) {
						float at = a / 255.0f;
						float bt = b / 64.0f;
						c = bt / at;
						c /= (1.0 - bt + c);
						ci = (int)(c*255);
						if(ci>=256) ci = 255; /* will not overflow... */
					} else {
						ci = 255;
					}
					// higher byte of the index is source opacity
					// lower byte of the index is destination opacity
					OpacityOnOpacityTable65[addr]= (byte)(ci & 0xff);

					bb = b * 4;
					if(bb > 255) bb = 255;
					NegativeMulTable65[addr] = (byte)( ( 255 - (255-a)*(255-bb)/ 255 ) & 0xff );
				}
			}
		} else {
			//long start2 = System.currentTimeMillis();
			for( a = 0; a < 256; a++ ) {
				for( b = 0; b < 256; b++ ) {
					int ci;
					int addr = (b<<8)+ a;
					if( a != 0 ) {
						int bt = b * 32896; // 23bit(+15bit)
						//int bt = b * 8421504; // 31bit(+24bit)
						int c = bt / a;
						ci = ( c*255 / (((1<<15)-1) - (bt>>>8) + c));
						//0x7FFF
						// if(ci>=256) ci = 255; // will not overflow...
					} else {
						ci=255;
					}
					OpacityOnOpacityTable[addr]=(byte)(ci&0xff);
						// higher byte of the index is source opacity
						// lower byte of the index is destination opacity
					NegativeMulTable[addr] = (byte)(( 255 - (255-a)*(255-b)/ 255 )&0xff);
				}
			}
			for( a = 0; a < 256; a++ ) {
				for( b = 0; b < 65; b++ ) {
					int ci;
					int addr = (b<<8) + a;
					if( a != 0 ) {
						int bi = b * 131071; // 23bit
						ci = bi / a;
						ci = ( ci*255 / (((1<<15)-1) - (bi>>>8) + ci));
						// if(cv>=256) cv = 255; // will not overflow...
					} else {
						ci = 255;
					}
					// higher byte of the index is source opacity
					// lower byte of the index is destination opacity
					OpacityOnOpacityTable65[addr]= (byte)(ci & 0xff);

					int bb = b * 4;
					if(bb > 255) bb = 255;
					NegativeMulTable65[addr] = (byte)( ( 255 - (255-a)*(255-bb)/ 255 ) & 0xff );
				}
			}
			/*
			long end = System.currentTimeMillis();
			long duration = start2 - start;
			long durationint = end - start2;
			Log.v("Time","float:"+duration+", int:"+durationint);
			*/
		}

		RecipTable256[0] = 65536;
		RecipTable256_16[0] = 0x7fff;
		for( int i = 1; i < 256; i++ ) {
			int v = 65536/i;
			RecipTable256[i] = v;
			RecipTable256_16[i] = v > 0x7fff ? 0x7fff : (short)v;
		}

		// create ps tables
		/*
		PsTableSoftLight = new byte[256][];
		PsTableColorDodge = new byte[256][];
		PsTableColorBurn = new byte[256][];
		for( int s = 0; s < 256; s++ ) {
			PsTableSoftLight[s] = new byte[256];
			PsTableColorDodge[s] = new byte[256];
			PsTableColorBurn[s] = new byte[256];
			for( int d = 0; d < 256; d++ ) {
				PsTableSoftLight[s][d]  = (s>=128) ?
						(byte)( ((int)(Math.pow(d/255.0, 128.0/s)*255.0))&0xff ) :
						(byte)( ((int)(Math.pow(d/255.0, (1.0-s/255.0)/0.5)*255.0))&0xff );
				PsTableColorDodge[s][d] = ((255-s)<=d) ? (byte)(0xff) : (byte)(((int)((d*255)/(255-s)))&0xff);
				PsTableColorBurn[s][d]  = (s<=(255-d)) ? (byte)(0x00) : (byte)((int)(255-((255-d)*255)/s)&0xff);
			}
		}
		*/
		CustomOperationComposite.initialize();
	}

	public static void finalizeApplication() {
		OpacityOnOpacityTable = null;
		NegativeMulTable = null;

		OpacityOnOpacityTable65 = null;
		NegativeMulTable65 = null;

		RecipTable256 = null;
		RecipTable256_16 = null;

		DivTable = null;
		PsTableSoftLight = null;
		PsTableColorDodge = null;
		PsTableColorBurn = null;

		Color252DitherPalette = null;
		DitherTable_676 = null;

		TextBuf = null;
		FontTempImage = null;
		CachePaint = null;
		FontTempCanvas = null;
		FontChBuff = null;
		FontTextBounds = null;
		PorterSrcMode = null;
		PorterSrcOverMode = null;
		PorterDarkenMode = null;
		PorterLightenMode = null;
		PorterMultiplyMode = null;
		PorterScreenMode = null;
		CustomOperationComposite.finalizeApplication();
	}

	//private static void delAllocatedImage( NativeImageBuffer img ) {
		/*
		System.gc();
		compactAlloatedImages();
		final int count = mAllocImages.size();
		for( int i = count-1; i >= 0; i-- ) {
			NativeImageBuffer cur = mAllocImages.get(i).get();
			if( cur != null && cur == img ) {
				mAllocImages.remove(i);
			}
		}
		showAllocatedImages();
		*/
	//}
	//private static void addAllocatedImage( NativeImageBuffer img ) {
		/*
		System.gc();
		compactAlloatedImages();
		mAllocImages.add( new WeakReference<NativeImageBuffer>(img) );
		showAllocatedImages();
		*/
	//}
	/*
	private static void compactAlloatedImages() {
		final int count = mAllocImages.size();
		for( int i = count-1; i >= 0; i-- ) {
			NativeImageBuffer img = mAllocImages.get(i).get();
			if( img == null ) {
				mAllocImages.remove(i);
			}
		}
	}
	*/
	//private static void showAllocatedImages() {
	//	final int count = mAllocImages.size();
	//	Log.v("Allocated Image", "count : " + count );
		/*
		for( int i = count-1; i >= 0; i-- ) {
			NativeImageBuffer img = mAllocImages.get(i).get();
			if( img != null ) {
				Log.v("Allocated Image", "no. " + i + ", w : " + img.mWidth + ", h : " + img.mHeight );
			}
		}
		*/
	//}

	private Bitmap mImage;
	private IndexedBitmap mImage8;
	private byte mBPP;
	private int mColor;
	//private boolean mValidColor;
	private Paint mPaint;
	private Canvas mCanvas;
	private int mWidth;
	private int mHeight;
	private int mRefCount;
	private android.graphics.Rect mTextBounds;
	private android.graphics.Rect mSrcRect = new android.graphics.Rect();
	private android.graphics.Rect mDstRect = new android.graphics.Rect();
	private Matrix mAffineMatrix;
	private float[] mAffinePoints;

	private static void forceGC() {
		DebugClass.addLog("Faild to allocate memory in Image Buffer. Force GC and try agian.");
		TVP.EventManager.deliverCompactEvent(CompactEventCallbackInterface.COMPACT_LEVEL_MAX); // 内部でキャッシング等しているデータ開放
		//VMRuntime.getRuntime().gcSoftReferences(); // SoftReference で保持しているキャッシュ開放
		System.gc(); // GC 実行
		System.gc(); // GC 実行
		showMemoryInfo(); // 空きメモリ表示
	}
	public static void showMemoryInfo() {

		// アプリのメモリ情報を取得
		Runtime runtime = Runtime.getRuntime();
		// トータルメモリ
		Log.v("Dalvik Heap", "totalMemory[KB] = " + (int)(runtime.totalMemory()/1024));
		// 空きメモリ
		Log.v("Dalvik Heap", "freeMemory[KB] = " + (int)(runtime.freeMemory()/1024));
		// 現在使用しているメモリ
		Log.v("Dalvik Heap", "usedMemory[KB] = " + (int)( (runtime.totalMemory() - runtime.freeMemory())/1024) );
		// Dalvikで使用できる最大メモリ
		Log.v("Dalvik Heap", "maxMemory[KB] = " + (int)(runtime.maxMemory()/1024));

		// トータルメモリ
		Log.v("Native Heap", "totalMemory[KB] = " + Long.toString(Debug.getNativeHeapSize() / 1024));
		// 空きメモリ
		Log.v("Native Heap", "freeMemory[KB] = " + Long.toString(Debug.getNativeHeapFreeSize() / 1024) );
		// 現在使用しているメモリ
		Log.v("Native Heap", "usedMemory[KB] = " + Long.toString(Debug.getNativeHeapAllocatedSize() / 1024) );
	}
	private static Bitmap createBitmap( int w, int h, Bitmap.Config type ) {
		Bitmap result = null;
		try {
			result = Bitmap.createBitmap( w, h, type );
		} catch( OutOfMemoryError e ) { // メモリ不足の場合コンパクト化とgcを実行して出来るだけメモリを空ける
			forceGC();
			// 2回目は try-catch せず、2回目も失敗した時は諦める
			result = Bitmap.createBitmap( w, h, type );
		}
		return result;
	}
	private static Bitmap copyBitmap( Bitmap bmp ) {
		Bitmap result = null;
		try {
			result = bmp.copy( bmp.getConfig(), true );
		} catch( OutOfMemoryError e ) {
			forceGC();
			result = bmp.copy( bmp.getConfig(), true );
		}
		bmp.recycle();
		return result;
	}
	public NativeImageBuffer( int w, int h ) {
		mWidth = w;
		mHeight = h;
		mBPP = 32;
		// 確保遅延
		if( LATE_IMAGE_ALLOCATE == false ) {
			mImage = createBitmap( w, h, Bitmap.Config.ARGB_8888 );
			mCanvas = new Canvas(mImage);
		}
		mPaint = new Paint();
		mRefCount = 1;
	}
	public NativeImageBuffer(int w, int h, int bpp) {
		mWidth = w;
		mHeight = h;
		mBPP = (byte) bpp;
		if( bpp == 8 ) {
			mImage8 = new IndexedBitmap( w, h );
		} else {
			// 確保遅延
			if( LATE_IMAGE_ALLOCATE == false ) {
				mImage = createBitmap( w, h, Bitmap.Config.ARGB_8888 );
				mCanvas = new Canvas(mImage);
			}
			mPaint = new Paint();
		}
		mRefCount = 1;
	}
	public NativeImageBuffer(NativeImageBuffer src) {
		mWidth = src.mWidth;
		mHeight = src.mHeight;
		mBPP = src.mBPP;
		mColor = src.mColor;
		//mValidColor = src.mValidColor;
		if( src.isImageAllocated() ) {
			if( src.mImage != null ) {
				try {
					mImage = src.mImage.copy(src.mImage.getConfig(), true );
				} catch( OutOfMemoryError e ) { // メモリ不足の場合コンパクト化とgcを実行して出来るだけメモリを空ける
					forceGC();
					// 2回目は try-catch せず、2回目も失敗した時は諦める
					mImage = src.mImage.copy(src.mImage.getConfig(), true );
				}
				mCanvas = new Canvas(mImage);
				mPaint = new Paint();
			} else {
				mImage8 = src.mImage8.copy();
			}
			//addAllocatedImage( this );
		}
		mRefCount = 1;
	}
	public NativeImageBuffer(Bitmap img) {
		mWidth = img.getWidth();
		mHeight = img.getHeight();
		/*
		if( img.isMutable() == false ) {
			Bitmap org = img;
			try {
				img = org.copy(Bitmap.Config.ARGB_8888, true );
			} catch( OutOfMemoryError e ) { // メモリ不足の場合コンパクト化とgcを実行して出来るだけメモリを空ける
				forceGC();
				// 2回目は try-catch せず、2回目も失敗した時は諦める
				img = org.copy(Bitmap.Config.ARGB_8888, true );
			}
			org = null;
		}
		*/
		if( Bitmap.Config.ARGB_8888.equals(img.getConfig()) ) {
			mBPP = 32;
		} else if( Bitmap.Config.ALPHA_8.equals(img.getConfig()) ) {
			mBPP = 8;
		} else {
			mBPP = 16;
		}
		mImage = img;
		if( img.isMutable() ) {
			mCanvas = new Canvas(mImage);
		}
		mPaint = new Paint();
		//addAllocatedImage( this );
		mRefCount = 1;
	}
	public NativeImageBuffer(int w, int h, NativeImageBuffer src) {
		mWidth = w;
		mHeight = h;
		mBPP = src.mBPP;
		mColor = src.mColor;
		//mValidColor = src.mValidColor;
		if( src.isImageAllocated() ) {
			if( src.mImage != null ) {
				mImage = createBitmap( w, h, src.mImage.getConfig() );
				mCanvas = new Canvas(mImage);
				if( mImage != null ) {
					mCanvas.drawBitmap(src.mImage, 0, 0, null );
				}
				mPaint = new Paint();
			} else {
				mImage8 = src.mImage8.copy();
			}
			//addAllocatedImage( this );
		}
		mRefCount = 1;
	}

	public NativeImageBuffer(IndexedBitmap idx) {
		mWidth = idx.width();
		mHeight = idx.height();
		mBPP = 8;
		mImage8 = idx;
		mRefCount = 1;
	}

	public final void addRef() { mRefCount++; }
	public final void finalizeRelease() {
		if( mRefCount != 1 )
			mRefCount--;
	}
	public final void release() {
		if( mRefCount != 1 )
			mRefCount--;
		else {
			// 解放までは行わない(行えない)
			if( mImage != null ) {
				mImage.recycle();
				mImage = null;
				//Log.v( "Thread", "release() : " + Thread.currentThread().getName() );
				//DebugClass.addLog("Release Bitmap.");
			}
		}
	}
	public final boolean isIndependent() { return mRefCount == 1; }
	public final Bitmap getImage() throws TJSException {
		allocateIfEmpty();
		return mImage;
	}
	public boolean isImageAllocated() {
		return ( mImage != null || mImage8 != null );
	}
	/**
	 * 保持している画像を開放する
	 * @return 実際に開放が行われたかどうか
	 */
	public boolean purgeImage() {
		if( mRefCount == 1 && mImage != null) {
			mImage.recycle();
			mImage = null;
			//delAllocatedImage( this );
			return true;
		}
		return false;
	}
	/**
	 * mutable でない時、mutable にする
	 */
	public void toMutable() {
		if( mImage == null ) return;
		if( mImage.isMutable() == false ) {
			mImage = copyBitmap( mImage );
			mCanvas.setBitmap(mImage);
		}
	}
	/**
	 * まだ画像データが確保されていない場合、確保する
	 * @throws TJSException
	 */
	private void allocateIfEmpty() throws TJSException {
		if( mImage != null || mImage8 != null ) return;

		if( mBPP == 32 ) {
			mImage = createBitmap( mWidth, mHeight, Bitmap.Config.ARGB_8888 );
		} else if( mBPP == 8 ) {
			mImage = createBitmap( mWidth, mHeight, Bitmap.Config.ALPHA_8 );
		} else {
			throw new TJSException("Not support image type.");
		}
		//addAllocatedImage( this );
		mCanvas = new Canvas(mImage);
		if( mPaint == null ) mPaint = new Paint();

		if( mColor != 0 ) {
			if( mBPP == 32 ) {
				mCanvas.drawColor( mColor, PorterDuff.Mode.SRC  );
			} else if( mBPP == 8 ) {
				final int w = mWidth;
				final int h = mHeight;
				final int length = w*h;
				byte[] s;
				try {
					s = new byte[length];
				} catch( OutOfMemoryError e ) {
					TVP.EventManager.deliverCompactEvent(CompactEventCallbackInterface.COMPACT_LEVEL_MAX);
					System.gc();
					showMemoryInfo();
					s = new byte[length];
				}
				ByteBuffer buff = ByteBuffer.wrap(s);
				mImage.copyPixelsToBuffer(buff);
				byte color = (byte) (mColor&0xff);
				Arrays.fill( s, color );
				buff.flip();
				mImage.copyPixelsFromBuffer(buff);
			}
		}
	}

	public final int getWidth() { return mWidth; }
	public final int getHeight() { return mHeight; }

	public final void recreate( int w, int h, boolean keepimage ) throws TJSException {
		if( isImageAllocated() ) {
			if( mImage != null ) {
				Bitmap.Config imageType = mImage.getConfig();
				int ow = mImage.getWidth();
				int oh = mImage.getHeight();
				if( w != 0 && h != 0 && ( ow != w || oh != h) ) {
					if( keepimage && mImage != null ) {
						Bitmap.Config type = mImage.getConfig();
						Bitmap tmp;
						try {
							tmp = Bitmap.createBitmap( w, h, type );
						} catch( OutOfMemoryError e ) { // メモリ不足の場合コンパクト化とgcを実行して出来るだけメモリを空ける
							forceGC();
							// 2回目は try-catch せず、2回目も失敗した時は諦める
							tmp = Bitmap.createBitmap( w, h, type );
						}
						Canvas c = new Canvas(tmp);
						c.drawBitmap(mImage, 0, 0, mPaint);
						mImage.recycle();
						mImage = null;
						mImage = tmp;
						mCanvas = c;
					} else {
						mImage.recycle();
						mImage = null;
						mImage = Bitmap.createBitmap( w, h, imageType );
						mCanvas = new Canvas(mImage);
					}
				} else if( w == 0 || h == 0 ) {
					mImage = null;
					mCanvas = null;
				}
			} else {
				mImage8.recreate(w, h, keepimage);
			}
		}
		mWidth = w;
		mHeight = h;
	}
	public final int getBPP() { return mBPP; }

	public final void fill(Rect rect, int value ) throws TJSException {
		if( mImage8 != null ) {
			mImage8.fill( rect, value );
			return;
		}
		if( mImage == null ) {
			//if( mValidColor == false ) {
			//	mColor = value;
			//	mValidColor = true;
			//	return;
			//}
			if( mColor == value ) {
				return;
			}
			if( rect.left == 0 && rect.top == 0 && rect.right == mWidth && rect.bottom == mHeight ) {
				mColor = value;
				return;
			}
			allocateIfEmpty();
		} else {
			// 画像を保持している時、全体を単一色で塗りつぶされた場合は保持している画像の開放を行う
			/* やはり、ここで開放してしまうのは好ましくない。一度塗りつぶしてから、何かを描画する時開放、確保が頻発してしまう
			if( rect.left == 0 && rect.top == 0 && rect.right == mWidth && rect.bottom == mHeight ) {
				if( purgeImage() ) {
					mColor = value;
					return;
				}
			}
			*/
		}

		//mCanvas.save();
		//mCanvas.clipRect( rect.left, rect.top, rect.right, rect.bottom );
		//mCanvas.drawColor(value, PorterDuff.Mode.SRC );
		//mCanvas.restore();
		Bitmap bmp = mImage;
		final Bitmap.Config type = bmp.getConfig();
		if( bmp.isMutable() == false ) {
			mImage = copyBitmap( bmp );
			mCanvas = new Canvas(mImage);
			bmp = mImage;
		}
		if( Bitmap.Config.ALPHA_8.equals( type ) ) {
			final int w = bmp.getWidth();
			final int h = bmp.getHeight();
			byte[] s;
			try {
				s = new byte[w*h];
			} catch( OutOfMemoryError e ) {
				forceGC();
				s = new byte[w*h];
			}
			ByteBuffer buff = ByteBuffer.wrap(s);
			bmp.copyPixelsToBuffer(buff);
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
			buff.flip();
			bmp.copyPixelsFromBuffer(buff);

			/*
			mDstRect.set( rect.left, rect.top, rect.right, rect.bottom );
			mPaint.setXfermode(PorterSrcMode);
			mPaint.setColor( 0 );
			mPaint.setAlpha( value&0xff );
			mPaint.setStyle(Style.FILL);
			mCanvas.drawRect(mDstRect, mPaint);
			*/
		} else {
			mDstRect.set( rect.left, rect.top, rect.right, rect.bottom );
			mPaint.setXfermode(PorterSrcMode);
			mPaint.setColor( value );
			mPaint.setAlpha( value>>>24 );
			mPaint.setStyle(Style.FILL);
			mCanvas.drawRect(mDstRect, mPaint);
		}
	}
	public final void fillColor(Rect rect, int color, int opa ) throws TJSException {
		if( mImage == null ) {
			//if( mValidColor == false ) {
			//	mColor = value;
			//	mValidColor = true;
			//	return;
			//}
			if( opa == 255 && mColor == (color|0xff000000) ) {
				return;
			}
			if( rect.left == 0 && rect.top == 0 && rect.right == mWidth && rect.bottom == mHeight ) {
				if( opa == 255 ) {
					mColor = color | (opa << 24);
				} else {
					int s1 = (color & 0xff00ff)*opa ;
					color = (color & 0xff00)*opa ;
					opa = 255 - opa;
					int d = mColor;
					mColor = (d & 0xff000000) + ((((d & 0xff00ff) * opa + s1) >>> 8) & 0xff00ff) +
						((((d&0xff00) * opa + color) >>> 8) & 0xff00);
				}
				return;
			}
			allocateIfEmpty();
		} // 乗算される形になるので、全面塗りつぶしでも開放は出来ない
		//mCanvas.save();
		//mCanvas.clipRect( rect.left, rect.top, rect.right, rect.bottom );
		//mCanvas.drawColor( (color&0xFFFFFF)|(opa<<24), PorterDuff.Mode.SRC );
		//mCanvas.restore();
		if( mImage.isMutable() == false ) {
			mImage = copyBitmap( mImage );
			mCanvas = new Canvas(mImage);
		}
		mDstRect.set( rect.left, rect.top, rect.right, rect.bottom );
		//mPaint.setXfermode(PorterSrcMode);
		mPaint.setXfermode(PorterSrcOverMode);
		mPaint.setColor( color );
		mPaint.setAlpha( opa );
		mPaint.setStyle(Style.FILL);
		mCanvas.drawRect(mDstRect, mPaint);
	}
	private final boolean fill( Rect rect, int color, int method, int opa, boolean hda ) throws TJSException {
		if( mImage == null ) {
			/* 状況的に少ないかな？ パターンを作るのが大変なので、普通に確保して描画する。
			if( rect.left == 0 && rect.top == 0 && rect.right == mWidth && rect.bottom == mHeight ) {
				// ブレンド処理が必要
				mColor = color;
				return true;
			}
			*/
			allocateIfEmpty();
		}

		if( mImage.isMutable() == false ) {
			mImage = copyBitmap( mImage );
			mCanvas = new Canvas(mImage);
		}

		PorterDuffXfermode mode;
		if( method == LayerNI.bmCopy && !hda ) {
			mode = PorterSrcMode;
		} else if( method == LayerNI.bmCopyOnAlpha ) {
			mode = PorterSrcMode;
		} else if( method == LayerNI.bmAlpha || method == LayerNI.bmAlphaOnAlpha ) {
			mode = PorterSrcOverMode;
//		} else if( method == LayerNI.bmAdd ) {
//			mode = new PorterDuffXfermode( PorterDuff.Mode.ADD );
		} else if( method == LayerNI.bmDarken ) {
			mode = PorterDarkenMode;
		} else if( method == LayerNI.bmLighten ) {
			mode = PorterLightenMode;
		} else if( method == LayerNI.bmMul ) {
			mode = PorterMultiplyMode;
		} else if( method == LayerNI.bmScreen ) {
			mode = PorterScreenMode;
//		} else if( method == LayerNI.bmPsOverlay ) {
//			mode = new PorterDuffXfermode( PorterDuff.Mode.OVERLAY );
		} else {
			mode = PorterSrcOverMode;
		}

		mDstRect.set( rect.left, rect.top, rect.right, rect.bottom );
		mPaint.setXfermode(PorterSrcMode);
		mPaint.setColor( color );
		if( (color & 0xff000000) != 0 ) {
			opa = ((color >>> 24) * opa) / 255;
		}
		mPaint.setAlpha(opa);
		mPaint.setXfermode(mode);
		mPaint.setStyle(Style.FILL);
		mCanvas.drawRect(mDstRect, mPaint);
		return true;
	}

	public final boolean copyRect(int x, int y, NativeImageBuffer src, Rect refrect) throws TJSException {
		final int w = refrect.width();
		final int h = refrect.height();
		if( src.isImageAllocated() == false ) {
			// 元データが未確保の時は、フィルに置き換えて動作する
			fill( new Rect(x,y,x+w,y+h), src.mColor );
			return true;
		}
		allocateIfEmpty();
		if( mImage.isMutable() == false ) {
			mImage = copyBitmap( mImage );
			mCanvas = new Canvas(mImage);
		}
		mDstRect.set(x,y,x+w,y+h);
		mSrcRect.set(refrect.left,refrect.top,refrect.right,refrect.bottom);
		mPaint.setAlpha(255);
		mPaint.setXfermode(PorterSrcMode);
		mCanvas.drawBitmap(src.mImage, mSrcRect, mDstRect, mPaint );
		return true;
	}
	public final boolean copyRect(Rect dstrect, NativeImageBuffer src, Rect srcrect, int method, int opa, boolean hda ) throws TJSException {
		if( src.isImageAllocated() == false ) {
			// 元データが未確保の時は、フィルに置き換えて動作する
			return fill( dstrect, src.mColor, method, opa, hda );
		}
		allocateIfEmpty();

		mDstRect.set(dstrect.left,dstrect.top,dstrect.right,dstrect.bottom);
		mSrcRect.set(srcrect.left,srcrect.top,srcrect.right,srcrect.bottom);

		PorterDuffXfermode mode;
		if( method == LayerNI.bmCopy && !hda ) {
			mode = PorterSrcMode;
		} else if( method == LayerNI.bmCopyOnAlpha ) {
			mode = PorterSrcMode;
		} else if( method == LayerNI.bmAlpha || method == LayerNI.bmAlphaOnAlpha ) {
			mode = PorterSrcOverMode;
//		} else if( method == LayerNI.bmAdd ) {
//			mode = new PorterDuffXfermode( PorterDuff.Mode.ADD );
		} else if( method == LayerNI.bmDarken ) {
			mode = PorterDarkenMode;
		} else if( method == LayerNI.bmLighten ) {
			mode = PorterLightenMode;
		} else if( method == LayerNI.bmMul ) {
			mode = PorterMultiplyMode;
		} else if( method == LayerNI.bmScreen ) {
			mode = PorterScreenMode;
//		} else if( method == LayerNI.bmPsOverlay ) {
//			mode = new PorterDuffXfermode( PorterDuff.Mode.OVERLAY );
		} else {
			// 組み込みで対応出来ない時は、自前実装でコピーする
			if( mImage.isMutable() == false ) {
				mImage = copyBitmap( mImage );
				mCanvas = new Canvas(mImage);
			}
			CustomOperationComposite.blend( mImage, dstrect, src.mImage, srcrect, method, opa, hda );
			return true;
		}
		if( mImage.isMutable() == false ) {
			mImage = copyBitmap( mImage );
			mCanvas = new Canvas(mImage);
		}
		mPaint.setAlpha(opa);
		mPaint.setXfermode(mode);
		mCanvas.drawBitmap(src.mImage, mSrcRect, mDstRect, mPaint );

		return true;
	}

	/**
	 * 影のぼけと透明度を取り除く処理は実装できていない。
	 * これを実装しようとしたら、ピクセル単位での処理が必要になる。
	 * TODO どうするべきか？
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
	 * @throws TJSException
	 */
	public final void drawText( Font fontV, final Rect destrect, int x, int y, final String text,
			int color, int bltmode, int opa, boolean holdalpha, boolean aa,
			int shlevel, int shadowcolor, int shwidth, int shofsx, int shofsy, ComplexRect updaterects ) throws TJSException {
		final int count = text.length();
		if( count == 0 ) return;
		allocateIfEmpty();

		mPaint.setAntiAlias(aa);
		mPaint.setColor(color);
		mPaint.setAlpha(opa);
		mPaint.setTextSize(fontV.getHeight());
		mPaint.setTextAlign( Paint.Align.LEFT );
		mPaint.setStyle( Paint.Style.FILL );
		mPaint.setTypeface(fontV.getFont());

		if( shlevel != 0 ) { // 影を書く
			mPaint.setShadowLayer( shwidth, shofsx, shofsy, shadowcolor );
		}
		PorterDuffXfermode mode;
		if( bltmode == LayerNI.bmCopy && !holdalpha ) {
			mode = PorterSrcMode;
		} else if( bltmode == LayerNI.bmCopyOnAlpha ) {
			mode = PorterSrcMode;
		} else if( bltmode == LayerNI.bmAlpha || bltmode == LayerNI.bmAlphaOnAlpha ) {
			mode = PorterSrcOverMode;
//		} else if( bltmode == LayerNI.bmAdd ) {
//			mode = new PorterDuffXfermode( PorterDuff.Mode.ADD );
		} else if( bltmode == LayerNI.bmDarken ) {
			mode = PorterDarkenMode;
		} else if( bltmode == LayerNI.bmLighten ) {
			mode = PorterLightenMode;
		} else if( bltmode == LayerNI.bmMul ) {
			mode = PorterMultiplyMode;
		} else if( bltmode == LayerNI.bmScreen ) {
			mode = PorterScreenMode;
//		} else if( bltmode == LayerNI.bmPsOverlay ) { // ps 互換じゃないかも
//			mode = new PorterDuffXfermode( PorterDuff.Mode.OVERLAY );
		} else {
			mode = PorterSrcOverMode;
		}
		if( mImage.isMutable() == false ) {
			mImage = copyBitmap( mImage );
			mCanvas = new Canvas(mImage);
		}
	    Rect bound = new Rect();
		mPaint.setXfermode(mode);
		FontMetrics fontMetrics = mPaint.getFontMetrics();
		int top = (int) (y - fontMetrics.top);
		mCanvas.drawText( text, x, top, mPaint );
		mPaint.setXfermode(null);
		//mPaint.clearShadowLayer();
		if( mTextBounds == null ) mTextBounds = new android.graphics.Rect();
		mPaint.getTextBounds(text, 0, text.length(), mTextBounds );
		bound.set( mTextBounds.left+x, mTextBounds.top+top, mTextBounds.right+x, mTextBounds.bottom+top );
    	updaterects.or(bound);
		if( shlevel != 0 ) {
			mPaint.clearShadowLayer();
		}
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
		allocateIfEmpty(); // TODO 未確保の時最適化できるけど、その時はまず使われないかな

		if( type.startsWith("bmp") ) {
			// open stream
			Bitmap img = mImage;
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

				final Bitmap.Config imgtype = img.getConfig();
				if( Bitmap.Config.ARGB_8888.equals( imgtype ) != true ) Message.throwExceptionMessage(Message.InternalError);

				final int w = img.getRowBytes()/4;
				final int h = img.getHeight();
				final int length = w*h;
				int[] s = new int[length];;
				img.getPixels(s, 0, w, 0, 0, w, h );

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
				s = null;
				output.write( buffer.array() );
			} catch (IOException e) {
				Message.throwExceptionMessage(Message.InternalError);
			} finally {
				img = null;
				try {
					output.close();
				} catch (IOException e) {
					Message.throwExceptionMessage(Message.WriteError);
				}
			}
		} else if( type.equalsIgnoreCase("png") ) {
			mImage.compress(Bitmap.CompressFormat.PNG, 100, output );
			try {
				output.close();
			} catch (IOException e) {
				Message.throwExceptionMessage(Message.WriteError);
			}
		} else if( type.startsWith("jpg") ) {
			int rate = 90; // default
			if( type.length() > 3 ) {
				rate = Integer.valueOf(type.substring(3)).intValue();
			}
			mImage.compress(Bitmap.CompressFormat.JPEG, rate, output );
			try {
				output.close();
			} catch (IOException e) {
				Message.throwExceptionMessage(Message.WriteError);
			}
		} else {
			Message.throwExceptionMessage(Message.WriteError );
		}
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
	public final int getPoint(int x, int y) {
		if( mImage8 != null ) {
			int ret = mImage8.getPixel(x, y);
			return ret;
		} else if( mImage == null ) {
			return mColor;
		}
		return mImage.getPixel(x, y);
	}
	public final void setPoint(int x, int y, int n) throws TJSException {
		if( mImage8 != null ) {
			mImage8.setPixel(x, y, n);
			return;
		} else if( mImage == null ) {
			if( mColor == n ) return;
			allocateIfEmpty();
		}
		mImage.setPixel(x, y, n);
	}
	public void coerceData(boolean b) {
		//mImage.coerceData(b);
	}
	public boolean isAlphaPremultiplied() {
		return false;//mImage.isAlphaPremultiplied();
	}
	public void flipLR(Rect rect) {
		if( mImage8 != null ) {
			mImage8.flipLR(rect);
			return;
		}
		if( mImage == null ) return;
		final int w = mImage.getWidth();
		final int h = mImage.getHeight();

		if( mImage.isMutable() == false ) {
			mImage = copyBitmap( mImage );
			mCanvas = new Canvas(mImage);
		}
		Bitmap bmp = mImage;
		final Bitmap.Config type = bmp.getConfig();
		if( Bitmap.Config.ARGB_8888.equals( type ) ) {
			int[] s = new int[w*h];
			IntBuffer buff = IntBuffer.wrap(s);
			bmp.copyPixelsToBuffer(buff);
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
			buff.flip();
			bmp.copyPixelsFromBuffer(buff);
		} else if( Bitmap.Config.ALPHA_8.equals(type) ) {
			byte[] s = new byte[w*h];
			ByteBuffer buff = ByteBuffer.wrap(s);
			bmp.copyPixelsToBuffer(buff);
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
			buff.flip();
			bmp.copyPixelsFromBuffer(buff);
		} else {
			short[] s = new short[w*h];
			ShortBuffer buff = ShortBuffer.wrap(s);
			bmp.copyPixelsToBuffer(buff);
			int ystart = rect.top*w + rect.left;
			final int rh = rect.bottom-rect.top;
			final int rw = rect.right-rect.left;
			final int w2 = rw / 2;
			for( int y = 0; y < rh; y++ ) {
				for( int x = 0; x < w2; x++ ) {
					int pos = ystart+x;
					int dst = ystart+rw-x-1;
					short c = s[pos];
					s[pos] = s[dst];
					s[dst] = c;
				}
				ystart += w;
			}
			buff.flip();
			bmp.copyPixelsFromBuffer(buff);
		}
	}
	public void flipUD(Rect rect) {
		if( mImage8 != null ) {
			mImage8.flipUD(rect);
			return;
		}
		if( mImage == null ) return;
		final int w = mImage.getWidth();
		final int h = mImage.getHeight();

		if( mImage.isMutable() == false ) {
			mImage = copyBitmap( mImage );
			mCanvas = new Canvas(mImage);
		}
		Bitmap bmp = mImage;
		final Bitmap.Config type = bmp.getConfig();
		if( Bitmap.Config.ARGB_8888.equals( type ) ) {
			int[] s = new int[w*h];
			IntBuffer buff = IntBuffer.wrap(s);
			bmp.copyPixelsToBuffer(buff);
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
			buff.flip();
			bmp.copyPixelsFromBuffer(buff);
		} else if( Bitmap.Config.ALPHA_8.equals(type) ) {
			byte[] s = new byte[w*h];
			ByteBuffer buff = ByteBuffer.wrap(s);
			bmp.copyPixelsToBuffer(buff);
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
			buff.flip();
			bmp.copyPixelsFromBuffer(buff);
		} else {
			short[] s = new short[w*h];
			ShortBuffer buff = ShortBuffer.wrap(s);
			bmp.copyPixelsToBuffer(buff);
			int ystart = rect.top*w + rect.left;
			int yend = (rect.bottom-1)*w + rect.left;
			final int rw = rect.right-rect.left;
			final int h2 = (rect.bottom-rect.top) / 2;
			for( int y = 0; y < h2; y++ ) {
				int sp = ystart;
				int dp = yend;
				for( int x = 0; x < rw; x++ ) {
					short c = s[sp];
					s[sp] = s[dp];
					s[dp] = c;
					sp++;
					dp++;
				}
				ystart += w;
				yend -= w;
			}
			buff.flip();
			bmp.copyPixelsFromBuffer(buff);
		}
	}
	public Canvas getCanvas() throws TJSException {
		allocateIfEmpty();
		if( mImage.isMutable() == false ) {
			mImage = copyBitmap( mImage );
			mCanvas = new Canvas(mImage);
		}
		return mCanvas;
	}
	public void drawFontImage(byte[] bp, int pitch, Rect srect, Rect drect,
			int color, int bltmode, int opa, boolean holdalpha) throws TJSException {

		allocateIfEmpty();
		int so = pitch * srect.top + srect.left;

		if( mImage.isMutable() == false ) {
			mImage = copyBitmap( mImage );
			mCanvas = new Canvas(mImage);
		}
		Bitmap bmp = mImage;
		final Bitmap.Config type = bmp.getConfig();
		if( Bitmap.Config.ARGB_8888.equals( type ) ) {
			int ystart = 0;
			final int rw = drect.right-drect.left;
			final int rh = (drect.bottom-drect.top);
			final int w = rw;
			final int size = rw*rh;
			if( TextBuf == null || TextBuf.length < size ) {
				TextBuf = null;
				TextBuf = new int[size];
			}
			int[] s = TextBuf;
			// 描画面が少ないであろうと思われるので、getPixels/setPixels で処理する
			bmp.getPixels( s, 0, rw, drect.left, drect.top, rw, rh );
			final int c1 = color & 0xff00ff;
			color = color & 0x00ff00;
			if( bltmode == LayerNI.bmAlphaOnAlpha ) {
				if( opa > 0 ) {
					byte[] otable = OpacityOnOpacityTable65;
					byte[] ntable = NegativeMulTable65;
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
			bmp.setPixels( s, 0, rw, drect.left, drect.top, rw, rh );
		} else {
			throw new TJSException("Not support image type.");
		}
	}
	public static CharacterData getCharacterData( Font fontV, char ch, boolean aa ) {
		if( CachePaint == null ) {
			CachePaint = new Paint();
		}
		CachePaint.setAntiAlias(aa);
		CachePaint.setAlpha(255);
		CachePaint.setTextSize(fontV.getHeight());
		CachePaint.setTextAlign( Paint.Align.LEFT );
		CachePaint.setStyle( Paint.Style.FILL );
		CachePaint.setTypeface(fontV.getFont());
		//FontMetrics fontMetrics = CachePaint.getFontMetrics();
		if( FontTextBounds == null ) {
			FontTextBounds = new android.graphics.Rect();
		}
		android.graphics.Rect textBounds = FontTextBounds;
		//int top = (int) (-fontMetrics.top);
		if( FontChBuff == null ) {
			FontChBuff = new char[1];
		}
		char[] text = FontChBuff;
		text[0] = ch;
		CachePaint.getTextBounds( text, 0, 1, textBounds );
		int tw = textBounds.width();
		tw = (((tw -1)>>>2)+1)<<2;
		final int th = textBounds.height();
		if( (FontTempImage == null ) || ( tw > FontTempImage.getWidth() || th > FontTempImage.getHeight()) ) {
			// 65階調なので、565 で、G6を使えば十分
			FontTempImage = Bitmap.createBitmap(FontTempImageWidth, FontTempImageHeight, Bitmap.Config.RGB_565 );
			FontTempCanvas = new Canvas(FontTempImage);
		}
		Canvas c = FontTempCanvas;
		CachePaint.setColor(0xff000000);
		//c.drawRect( textBounds.left, textBounds.top+top, textBounds.right, textBounds.bottom+top, CachePaint );
		c.drawRect( 0, 0, tw, th, CachePaint );
		CachePaint.setColor(0xffffffff);
		c.drawText(text, 0, 1, -textBounds.left, -textBounds.top, CachePaint );

		// 描画したbitmap を 65 階調のグレーススケールに変換する
		final int size = tw*th;
		CharacterData ret = new CharacterData();
		ret.alloc(size);
		byte[] bmp = ret.getData();
		if( TextBuf == null || TextBuf.length < size ) {
			TextBuf = new int [size];
		}
		int[] s = TextBuf;
		FontTempImage.getPixels( s, 0, tw, 0, 0, tw, th );
		int ystart = 0;
		int idx = 0;
		for( int y = 0; y < th; y++ ) {
			int sp = ystart;
			for( int x = 0; x < tw; x++ ) {
				//int color = s[sp] >>> 24;
				int color = (s[sp] >>> 8) & 0xff;
				byte out = (byte) (color >>> 2); // 0 - 63
				if( color != 0 ) out++;
				bmp[idx] = out;
				idx++;
				sp++;
			}
			ystart += tw;
		}

		ret.mAntialiased = aa;
		ret.mPitch = tw;
		ret.mOriginX = textBounds.left;
		ret.mOriginY = textBounds.top;
		ret.mBlackBoxX = tw;
		ret.mBlackBoxY = th;
		ret.mCellIncX = (int)CachePaint.measureText( text, 0, 1 );
		ret.mCellIncY = 0;
		//Log.v( "####", "l:"+textBounds.left+", t:" +textBounds.top +", r:"+ textBounds.right +", b:"+ textBounds.bottom );

		return ret;
	}
	public void fillMask(Rect rect, int opa) throws TJSException {
		if( mImage == null ) {
			if( (mColor >>> 24) == opa ) return;
			allocateIfEmpty();
		}
		if( mImage.isMutable() == false ) {
			mImage = copyBitmap( mImage );
			mCanvas = new Canvas(mImage);
		}
		Bitmap bmp = mImage;
		final Bitmap.Config type = bmp.getConfig();
		if( Bitmap.Config.ARGB_8888.equals( type ) ) {
			final int mask = opa << 24;
			final int w = bmp.getRowBytes()/4;
			final int h = bmp.getHeight();
			final int length = w*h;
			int[] s = new int[length];
			IntBuffer buff = IntBuffer.wrap(s);
			bmp.copyPixelsToBuffer(buff);
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
			buff.flip();
			bmp.copyPixelsFromBuffer(buff);
		} else {
			throw new TJSException("Not support image type.");
		}
	}
	public boolean copyRect(int x, int y, NativeImageBuffer src, Rect refrect, int plane) throws TJSException {
		if( (BaseBitmap.BB_COPY_MAIN|BaseBitmap.BB_COPY_MASK) == plane ) {
			if( src.isImageAllocated() == false ) {
				// 元データは未確保
				final int w = refrect.width();
				final int h = refrect.height();
				fill( new Rect(x,y,x+w,y+h), src.mColor );
				return true;
			}
			if( mImage8 != null ) {
				if( src.mImage8 == null ) throw new TJSException("Not support image type.");
				mImage8.copyRect( x, y, src.mImage8, refrect );
				return true;
			}
			src.allocateIfEmpty();
			allocateIfEmpty();
			if( mImage.isMutable() == false ) {
				mImage = copyBitmap( mImage );
				mCanvas = new Canvas(mImage);
			}
			int w = refrect.width();
			int h = refrect.height();
			if( this == src && y >= refrect.top && (y - refrect.top) < h && x >= refrect.left && (x-refrect.left) < w ) {
				Bitmap tmp = Bitmap.createBitmap( src.mImage, refrect.left, refrect.top, w, h );
				mSrcRect.set( 0, 0, w, h );
				mDstRect.set( x, y, x+w, y+h );
				mPaint.setFilterBitmap(false);
				mPaint.setAlpha(255);
				mPaint.setXfermode(PorterSrcMode);
				mCanvas.drawBitmap( tmp, mSrcRect, mDstRect, mPaint );
				tmp.recycle();
			} else {
				mSrcRect.set( refrect.left, refrect.top, refrect.right, refrect.bottom );
				mDstRect.set( x, y, x+refrect.width(), y+refrect.height() );
				mPaint.setFilterBitmap(false);
				mPaint.setAlpha(255);
				mPaint.setXfermode(PorterSrcMode);
				mCanvas.drawBitmap( src.mImage, mSrcRect, mDstRect, mPaint );
			}
			return true;
		} else {
			src.allocateIfEmpty();
			allocateIfEmpty();

			if( mImage.isMutable() == false ) {
				mImage = copyBitmap( mImage );
				mCanvas = new Canvas(mImage);
			}
			Bitmap bmp = mImage;
			Bitmap.Config type = bmp.getConfig();
			if( Bitmap.Config.ARGB_8888.equals( type ) != true ) throw new TJSException("Not support image type.");
			Bitmap srcbmp = src.mImage;
			type = srcbmp.getConfig();
			if( Bitmap.Config.ARGB_8888.equals( type ) != true ) throw new TJSException("Not support image type.");

			final int w = refrect.width();
			final int h = refrect.height();
			final int length = w*h;
			if( length == 0 ) return false;

			int[] s = new int[length];
			int[] d = new int[length];
			srcbmp.getPixels( s, 0, w, refrect.left, refrect.top, w, h );
			bmp.getPixels( d, 0, w, x, y, w, h );
			if( BaseBitmap.BB_COPY_MAIN == plane ) {
				for( int i = 0; i < length; i++ ) {
					d[i] = (d[i] & 0xff000000) | (s[i] & 0x00ffffff);
				}
				bmp.setPixels( d, 0, w, x, y, w, h );
			} else if( BaseBitmap.BB_COPY_MASK == plane ) {
				for( int i = 0; i < length; i++ ) {
					d[i] = (d[i] & 0x00ffffff) | (s[i] & 0xff000000);
				}
				bmp.setPixels( d, 0, w, x, y, w, h );
			}
			return true;
		}
	}
	public void setPointMain(int x, int y, int color) throws TJSException {
		if( mImage == null && (mColor&0xffffff) == (color&0xffffff) ) return;
		allocateIfEmpty();
		if( mImage.isMutable() == false ) {
			mImage = copyBitmap( mImage );
			mCanvas = new Canvas(mImage);
		}
		int c = mImage.getPixel(x, y);
		c &= 0xff000000;
		c |= color;
		mImage.setPixel(x, y, c);
	}
	public void setPointMask(int x, int y, int mask) throws TJSException {
		if( mImage == null && (mColor>>>24) == (mask&0xff) ) return;
		allocateIfEmpty();
		if( mImage.isMutable() == false ) {
			mImage = copyBitmap( mImage );
			mCanvas = new Canvas(mImage);
		}
		int c = mImage.getPixel(x, y);
		c &= 0x00ffffff;
		c |= (mask&0xff) << 24;
		mImage.setPixel(x, y, c);
	}
	public void removeConstOpacity(Rect rect, int level) throws TJSException {
		allocateIfEmpty();
		if( mImage.isMutable() == false ) {
			mImage = copyBitmap( mImage );
			mCanvas = new Canvas(mImage);
		}
		Bitmap bmp = mImage;
		final Bitmap.Config type = bmp.getConfig();
		if( Bitmap.Config.ARGB_8888.equals( type ) ) {
			final int w = bmp.getRowBytes()/4;
			final int h = bmp.getHeight();
			final int length = w*h;
			int[] s = new int[length];;
			IntBuffer buff = IntBuffer.wrap(s);
			bmp.copyPixelsToBuffer(buff);
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
			buff.flip();
			bmp.copyPixelsFromBuffer(buff);
		}
	}
	public void blendColor(Rect rect, int color, int opa, boolean additive) throws TJSException {
		// TODO 未確保の時、最適化できる
		allocateIfEmpty();
		// additive は無視している
		if( mImage.isMutable() == false ) {
			mImage = copyBitmap( mImage );
			mCanvas = new Canvas(mImage);
		}
		mPaint.setColor( color );
		mPaint.setAlpha(opa);
		mPaint.setStyle(Style.FILL);
		if( opa == 255 ) {
			mPaint.setXfermode(PorterSrcMode);
		} else {
			mPaint.setXfermode(PorterSrcOverMode);
		}
		mCanvas.drawRect(rect.left, rect.top, rect.right, rect.bottom, mPaint );
	}
	public boolean stretchBlt(Rect cliprect, Rect destrect, NativeImageBuffer ref, Rect refrect, int type, boolean hda, int opa, int method) throws TJSException {

		ref.allocateIfEmpty();
		allocateIfEmpty();
		PorterDuffXfermode mode;
		if( method == LayerNI.bmCopy && !hda ) {
			mode = PorterSrcMode;
		} else if( method == LayerNI.bmCopyOnAlpha ) {
			mode = PorterSrcMode;
		} else if( method == LayerNI.bmAlpha || method == LayerNI.bmAlphaOnAlpha ) {
			mode = PorterSrcOverMode;
//		} else if( bltmode == LayerNI.bmAdd ) {
//			mode = new PorterDuffXfermode( PorterDuff.Mode.ADD );
		} else if( method == LayerNI.bmDarken ) {
			mode = PorterDarkenMode;
		} else if( method == LayerNI.bmLighten ) {
			mode = PorterLightenMode;
		} else if( method == LayerNI.bmMul ) {
			mode = PorterMultiplyMode;
		} else if( method == LayerNI.bmScreen ) {
			mode = PorterScreenMode;
//		} else if( bltmode == LayerNI.bmPsOverlay ) {
//			mode = PorterOverlayMode;
		} else {
			// mode = PorterSrcOverMode;
			throw new TJSException("Not supported yet.");
		}
		if( mImage.isMutable() == false ) {
			mImage = copyBitmap( mImage );
			mCanvas = new Canvas(mImage);
		}
		mCanvas.save();
		mCanvas.clipRect(cliprect.left, cliprect.top, cliprect.right, cliprect.bottom);
		if( type == BaseBitmap.stFastLinear || type == BaseBitmap.stLinear || type == BaseBitmap.stCubic ) {
			mPaint.setFilterBitmap(true);
		} else { // BaseBitmap.stNearest
			mPaint.setFilterBitmap(false);
		}

		mPaint.setAlpha(opa);
		mPaint.setXfermode(mode);

		mSrcRect.set( refrect.left, refrect.top, refrect.right, refrect.bottom );
		mDstRect.set( destrect.left, destrect.top, destrect.right, destrect.bottom );
		mCanvas.drawBitmap( ref.mImage, mSrcRect, mDstRect, mPaint );

		mPaint.setXfermode(null);
		mCanvas.restore();
		return true;
	}
	public boolean affineBlt(Rect destrect, NativeImageBuffer ref,
			Rect refrect, AffineMatrix2D matrix, int method, int opa,
			Rect updaterect, boolean hda, int type, boolean clear,
			int clearcolor) throws TJSException {

		ref.allocateIfEmpty();
		allocateIfEmpty();
		if( mImage.isMutable() == false ) {
			mImage = copyBitmap( mImage );
			mCanvas = new Canvas(mImage);
		}
		mCanvas.save();
		mCanvas.clipRect(destrect.left, destrect.top, destrect.right, destrect.bottom);
		if( clear ) {
			mPaint.setColor(clearcolor);
			mPaint.setAlpha(255);
			mPaint.setStyle(Style.FILL);
			mCanvas.drawRect( destrect.left, destrect.top, destrect.right, destrect.bottom, mPaint );
		}

		PorterDuffXfermode mode;
		if( method == LayerNI.bmCopy && !hda ) {
			mode = PorterSrcMode;
		} else if( method == LayerNI.bmCopyOnAlpha ) {
			mode = PorterSrcMode;
		} else if( method == LayerNI.bmAlpha || method == LayerNI.bmAlphaOnAlpha ) {
			mode = PorterSrcOverMode;
//		} else if( bltmode == LayerNI.bmAdd ) {
//			mode = new PorterDuffXfermode( PorterDuff.Mode.ADD );
		} else if( method == LayerNI.bmDarken ) {
			mode = PorterDarkenMode;
		} else if( method == LayerNI.bmLighten ) {
			mode = PorterLightenMode;
		} else if( method == LayerNI.bmMul ) {
			mode = PorterMultiplyMode;
		} else if( method == LayerNI.bmScreen ) {
			mode = PorterScreenMode;
//		} else if( bltmode == LayerNI.bmPsOverlay ) {
//			mode = PorterOverlayMode;
		} else {
			// mode = PorterSrcOverMode;
			mCanvas.restore();
			throw new TJSException("Not supported yet.");
		}

		if( type == BaseBitmap.stFastLinear || type == BaseBitmap.stLinear || type == BaseBitmap.stCubic ) {
			mPaint.setFilterBitmap(true);
		} else { // BaseBitmap.stNearest
			mPaint.setFilterBitmap(false);
		}

		if( mAffineMatrix == null ) {
			mAffineMatrix = new Matrix();
		}
		Matrix trans = mAffineMatrix;
		float[] values = {
			(float)matrix.a, (float)matrix.c, (float)matrix.tx,
			(float)matrix.b, (float)matrix.d, (float)matrix.ty,
			0.0f, 0.0f, 1.0f
		};
		trans.setValues(values);

		// 更新される矩形を求める
		int width = refrect.width();
		int height = refrect.height();
		if( mAffinePoints == null ) {
			mAffinePoints = new float[8];
		}
		float[] pts = mAffinePoints;
		pts[0] = 0; // x1
	    pts[1] = 0; // y1
		pts[2] = width; // x2
	    pts[3] = 0; // y2
		pts[4] = width; // x3
	    pts[5] = height; // y3
		pts[6] = 0; // x4
	    pts[7] = height; // y4
		trans.mapPoints( pts );
		// ソート
		for( int i = 0; i < 3; i++) {
			for( int j = 3; j > i; j--) {
				int p = (j-1)*2;
				int c = j*2;
				float prev = pts[p];
				float cur = pts[c];
				if( prev > cur ) {	// 前の要素の方が大きかったら
					// 交換する
					pts[c] = prev;
					pts[p] = cur;
				}
				p = (j-1)*2+1;
				c = j*2+1;
				prev = pts[p];
				cur = pts[c];
				if( prev > cur ) {	// 前の要素の方が大きかったら
					// 交換する
					pts[c] = prev;
					pts[p] = cur;
				}
			}
		}
		updaterect.left = (int) pts[0];
		updaterect.right= (int) pts[6];
		updaterect.top   = (int) pts[1];
		updaterect.bottom= (int) pts[7];

		mPaint.setAlpha(opa);
		mPaint.setXfermode(mode);
		mCanvas.setMatrix(trans);
		mSrcRect.set( refrect.left, refrect.top, refrect.right, refrect.bottom );
		mDstRect.set( 0, 0, width, height );
		mCanvas.drawBitmap( ref.mImage, mSrcRect, mDstRect, mPaint );
		mPaint.setXfermode(null);

		mCanvas.restore();
		return true;
	}
	public void doBoxBlurLoop(Rect rect, Rect area) throws TJSException {
		if( mImage == null ) return;
		if( mImage.isMutable() == false ) {
			mImage = copyBitmap( mImage );
			mCanvas = new Canvas(mImage);
		}
		Bitmap bmp = mImage;
		final Bitmap.Config type = bmp.getConfig();
		if( Bitmap.Config.ARGB_8888.equals( type ) == false ) throw new TJSException("Not support image type.");

		final int w = mImage.getRowBytes()/4;
		final int h = bmp.getHeight();
		final int length = w*h;
		int[] s = new int[length];;
		IntBuffer buff = IntBuffer.wrap(s);
		bmp.copyPixelsToBuffer(buff);

		int width = w;
		int height = h;

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

			buff.flip();
			bmp.copyPixelsFromBuffer(buff);
		} finally {
			// exception caught
			vert_sum = null;
			dest_buf = null;
		}
	}
	public void doBoxBlurLoopAlpha(Rect rect, Rect area) throws TJSException {
		if( mImage == null ) return;
		if( mImage.isMutable() == false ) {
			mImage = copyBitmap( mImage );
			mCanvas = new Canvas(mImage);
		}
		Bitmap bmp = mImage;
		final Bitmap.Config type = bmp.getConfig();
		if( Bitmap.Config.ARGB_8888.equals( type ) == false ) throw new TJSException("Not support image type.");

		final int w = bmp.getRowBytes()/4;
		final int h = bmp.getHeight();
		final int length = w*h;
		int[] s = new int[length];;
		IntBuffer buff = IntBuffer.wrap(s);
		bmp.copyPixelsToBuffer(buff);

		int width = w;
		int height = h;

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

			buff.flip();
			bmp.copyPixelsFromBuffer(buff);
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

		byte[] table = DivTable;
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
	public void adjustGamma(Rect rect, GammaAdjustTempData tmp) throws TJSException {
		if( mImage == null ) {
			byte[] R = tmp.R;
			byte[] G = tmp.G;
			byte[] B = tmp.B;
			int d1 = mColor;
			int a = d1 & 0xff000000;
			if( a != 0 ) {
				int t1 = ((int)B[d1 & 0xff]) & 0xff;
				d1 >>>= 8;
				t1 |= (((int)G[d1 & 0xff])&0xff) << 8;
				d1 >>>= 8;
				t1 |= (((int)R[d1 & 0xff])&0xff) << 16;
				t1 |= a;
				mColor = t1;
			}
			return;
		}
		if( mImage.isMutable() == false ) {
			mImage = copyBitmap( mImage );
			mCanvas = new Canvas(mImage);
		}
		Bitmap bmp = mImage;
		final Bitmap.Config type = bmp.getConfig();
		if( Bitmap.Config.ARGB_8888.equals( type ) ) {
			final int w = bmp.getRowBytes()/4;
			final int h = bmp.getHeight();
			final int length = w*h;
			int[] s = new int[length];;
			IntBuffer buff = IntBuffer.wrap(s);
			bmp.copyPixelsToBuffer(buff); // ABGR オーダー

			byte[] R = tmp.R;
			byte[] G = tmp.G;
			byte[] B = tmp.B;
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
						int t1 = ((int)R[d1 & 0xff]) & 0xff;
						d1 >>>= 8;
						t1 |= (((int)G[d1 & 0xff])&0xff) << 8;
						d1 >>>= 8;
						t1 |= (((int)B[d1 & 0xff])&0xff) << 16;
						t1 |= a;
						s[sp] = t1;
					}
					sp++;
				}
				ystart += w;
			}

			buff.flip();
			bmp.copyPixelsFromBuffer(buff);
		} else {
			throw new TJSException("Not support image type.");
		}
	}
	public void adjustGammaForAdditiveAlpha(Rect rect, GammaAdjustTempData tmp) throws TJSException {
		if( mImage == null ) {
			byte[] R = tmp.R;
			byte[] G = tmp.G;
			byte[] B = tmp.B;
			int d1 = mColor;
			int a = d1 & 0xff000000;
			if( a == 0xff000000 ) {
				int t1 = ((int)B[d1 & 0xff]) & 0xff;
				d1 >>>= 8;
				t1 |= (((int)G[d1 & 0xff])&0xff) << 8;
				d1 >>>= 8;
				t1 |= (((int)R[d1 & 0xff])&0xff) << 16;
				t1 |= a;
				mColor = t1;
			} else if( a != 0 ) {
				short[] table = RecipTable256_16;
				// not completely transparent
				int alpha = a >>> 24;
				int alpha_adj = alpha + (alpha >>> 7);
				int recip = table[alpha];
				int d_tmp;
				int d = mColor;

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
				mColor = d_tmp;
			}
			return;
		}
		if( mImage.isMutable() == false ) {
			mImage = copyBitmap( mImage );
			mCanvas = new Canvas(mImage);
		}
		Bitmap bmp = mImage;
		final Bitmap.Config type = bmp.getConfig();
		if( Bitmap.Config.ARGB_8888.equals( type ) ) {
			final int w = bmp.getRowBytes()/4;
			final int h = bmp.getHeight();
			final int length = w*h;
			int[] s = new int[length];;
			IntBuffer buff = IntBuffer.wrap(s);
			bmp.copyPixelsToBuffer(buff); // ABGR オーダー

			byte[] R = tmp.R;
			byte[] G = tmp.G;
			byte[] B = tmp.B;
			short[] table = RecipTable256_16;
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
						int t1 = ((int)R[d & 0xff]) & 0xff;
						d >>>= 8;
						t1 |= (((int)G[d & 0xff])&0xff) << 8;
						d >>>= 8;
						t1 |= (((int)B[d & 0xff])&0xff) << 16;
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
							d_tmp = (R[255] * alpha_adj >>> 8) + t - alpha;
						else
							d_tmp = R[recip * t >>> 8] * alpha_adj >>> 8;
						// G
						t = (d>>>8) & 0xff;
						if(t > alpha)
							d_tmp |= ((G[255] * alpha_adj >>> 8) + t - alpha) << 8;
						else
							d_tmp |= (G[recip * t >>> 8] * alpha_adj >>> 8) << 8;
						// R
						t = (d>>>16) & 0xff;
						if(t > alpha)
							d_tmp |= ((B[255] * alpha_adj >>> 8) + t - alpha) << 16;
						else
							d_tmp |= (B[recip * t >>> 8] * alpha_adj >>> 8) << 16;
						// A
						d_tmp |= a;
						s[sp] = d_tmp;
					}
					sp++;
				}
				ystart += w;
			}

			buff.flip();
			bmp.copyPixelsFromBuffer(buff);
		} else {
			throw new TJSException("Not support image type.");
		}
	}
	public void doGrayScale(Rect rect) throws TJSException {
		if( mImage == null ) {
			int s1 = mColor;
			// ARGB order
			int d1 = (s1&0xff)*19;
			d1 += ((s1 >>> 8)&0xff)*183;
			d1 += ((s1 >>> 16)&0xff)*54;
			mColor = d1;
			return;
		}
		if( mImage.isMutable() == false ) {
			mImage = copyBitmap( mImage );
			mCanvas = new Canvas(mImage);
		}
		Bitmap bmp = mImage;
		final Bitmap.Config type = bmp.getConfig();
		if( Bitmap.Config.ARGB_8888.equals( type ) ) {
			final int w = bmp.getRowBytes()/4;
			final int h = bmp.getHeight();
			final int length = w*h;
			int[] s = new int[length];;
			IntBuffer buff = IntBuffer.wrap(s);
			bmp.copyPixelsToBuffer(buff);
			int ystart = rect.top*w + rect.left;
			final int rw = rect.right-rect.left;
			final int rh = (rect.bottom-rect.top);
			for( int y = 0; y < rh; y++ ) {
				int sp = ystart;
				for( int x = 0; x < rw; x++ ) {
					int s1 = s[sp];
					// ABGR order
					//int d1 = (s1&0xff)*19;
					//d1 += ((s1 >>> 8)&0xff)*183;
					//d1 += ((s1 >>> 16)&0xff)*54;
					int d1 = (s1&0xff)*54;
					d1 += ((s1 >>> 8)&0xff)*183;
					d1 += ((s1 >>> 16)&0xff)*19;
					d1 = (d1 >>> 8) * 0x10101 + (s1 & 0xff000000);
					s[sp] = d1;
					sp++;
				}
				ystart += w;
			}
			buff.flip();
			bmp.copyPixelsFromBuffer(buff);
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
		if( mImage == null ) {
			mColor &= 0x00ffffff;
			return;
		}
		if( mImage.isMutable() == false ) {
			mImage = copyBitmap( mImage );
			mCanvas = new Canvas(mImage);
		}
		Bitmap bmp = mImage;
		final Bitmap.Config type = bmp.getConfig();
		if( Bitmap.Config.ARGB_8888.equals( type ) ) {
			final int w = bmp.getRowBytes()/4;
			final int h = bmp.getHeight();
			final int length = w*h;
			int[] s = new int[length];
			int[] buffer = new int[w];
			IntBuffer buff = IntBuffer.wrap(s);
			bmp.copyPixelsToBuffer(buff);
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

			buff.flip();
			bmp.copyPixelsFromBuffer(buff);
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
	public void doAlphaColorMat(int color) throws TJSException {
		if( mImage == null ) {
			int s = mColor;
			int d = color;
			int sopa = s >>> 24;
			int d1 = d & 0xff00ff;
			d1 = (d1 + (((s & 0xff00ff) - d1) * sopa >>> 8)) & 0xff00ff;
			d &= 0xff00;
			s &= 0xff00;
			mColor = d1 + ((d + ((s - d) * sopa >>> 8)) & 0xff00) + 0xff000000;
			return;
		}
		if( mImage.isMutable() == false ) {
			mImage = copyBitmap( mImage );
			mCanvas = new Canvas(mImage);
		}
		Bitmap bmp = mImage;
		final Bitmap.Config type = bmp.getConfig();
		if( Bitmap.Config.ARGB_8888.equals( type ) ) {
			final int w = bmp.getRowBytes()/4;
			final int h = bmp.getHeight();
			final int length = w*h;
			int[] dest = new int[length];
			IntBuffer buff = IntBuffer.wrap(dest);
			bmp.copyPixelsToBuffer(buff);
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
			buff.flip();
			bmp.copyPixelsFromBuffer(buff);
		} else {
			throw new TJSException("Not support image type.");
		}
		//buff.order(ByteOrder.nativeOrder());
	}

}
