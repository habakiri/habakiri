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

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import jp.kirikiri.tjs2.BinaryStream;
import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tvp2.TVP;
import jp.kirikiri.tvp2.base.CompactEventCallbackInterface;
import jp.kirikiri.tvp2.msg.Message;
import jp.kirikiri.tvp2.utils.DebugClass;
import jp.kirikiri.tvp2.visual.GraphicsLoader;


public class NativeImageLoader {
	static private BitmapFactory.Options mLoadOption;

	static public void initialize() {
		mLoadOption = new BitmapFactory.Options();
	}
	public static void finalizeApplication() {
		mLoadOption = null;
	}
	private static void forceGC() {
		DebugClass.addLog("Faild image loading. Force GC and try agian.");
		TVP.EventManager.deliverCompactEvent(CompactEventCallbackInterface.COMPACT_LEVEL_MAX);
		//VMRuntime.getRuntime().gcSoftReferences();
		System.gc();
		NativeImageBuffer.showMemoryInfo();
	}

	public static NativeImageBuffer loadImage(BinaryStream stream, String ext, HashMap<String, String> metainfo, int keyidx, int mode) throws TJSException {
		if( mLoadOption == null ) {
			mLoadOption = new BitmapFactory.Options();
			mLoadOption.inPreferredConfig = Bitmap.Config.ARGB_8888;
		}
		Bitmap img = null;
		if( ".tlg".equalsIgnoreCase(ext) || ".tlg5".equalsIgnoreCase(ext ) || ".tlg6".equalsIgnoreCase(ext ) ) {
			try {
				img = TLGLoader.loadTLG(stream, mode, metainfo );
			} catch( OutOfMemoryError e ) { // メモリ不足の場合コンパクト化とgcを実行して出来るだけメモリを空ける
				forceGC();
				// 2回目は try-catch せず、2回目も失敗した時は諦める
				img = TLGLoader.loadTLG(stream, mode, metainfo );
			}
		}
		if( mode == GraphicsLoader.glmPalettized && ".png".equalsIgnoreCase(ext) ) {
			IndexedBitmap idx = null;
			try {
				idx = PNGLoader.loadPNG( stream, mode );
			} catch( OutOfMemoryError e ) { // メモリ不足の場合コンパクト化とgcを実行して出来るだけメモリを空ける
				forceGC();
				// 2回目は try-catch せず、2回目も失敗した時は諦める
				idx = PNGLoader.loadPNG( stream, mode );
			}
			if( idx != null ) {
				stream.close();
				return new NativeImageBuffer(idx);
			}
		}
		if( img == null ) {
			// BitmapFactory.Options opts = new BitmapFactory.Options();
			try {
				img = BitmapFactory.decodeStream(stream.getInputStream(),null,mLoadOption);
			} catch( OutOfMemoryError e ) { // メモリ不足の場合コンパクト化とgcを実行して出来るだけメモリを空ける
				forceGC();
				// 2回目は try-catch せず、2回目も失敗した時は諦める
				img = BitmapFactory.decodeStream(stream.getInputStream(),null,mLoadOption);
			}
		}
		if( img == null ) return null;
		if( mode == GraphicsLoader.glmPalettized ) {
			// これだと領域画像がうまく扱えない, Java 実装の PNG ローダーがいるかな
			Message.throwExceptionMessage( Message.ImageLoadError, "Unsupported color mode for palettized image.");
		} else if( mode == GraphicsLoader.glmGrayscale ) {
			try {
				img = convertAlpha8( img );
			} catch( OutOfMemoryError e ) { // メモリ不足の場合コンパクト化とgcを実行して出来るだけメモリを空ける
				forceGC();
				// 2回目は try-catch せず、2回目も失敗した時は諦める
				img = convertAlpha8( img );
			}
		}
		/* Mutable かどうかはここでは判定せず、使用直前に差し替える
		if( img.isMutable() == false ) {
			Bitmap src = img;
			Bitmap.Config type = Bitmap.Config.ARGB_8888;
			if( mode == GraphicsLoader.glmPalettized || mode == GraphicsLoader.glmGrayscale ) {
				type = Bitmap.Config.ALPHA_8;
			}
			try {
				img = src.copy( type, true);
			} catch( OutOfMemoryError e ) { // メモリ不足の場合コンパクト化とgcを実行して出来るだけメモリを空ける
				forceGC();
				// 2回目は try-catch せず、2回目も失敗した時は諦める
				img = src.copy( type, true);
			}
			src.recycle();
			src = null;
		}
		*/
		stream.close();
		return new NativeImageBuffer(img);
	}
	private static Bitmap convertAlpha8( Bitmap src ) throws TJSException {
		final Bitmap.Config type = src.getConfig();
		if( Bitmap.Config.ALPHA_8.equals( type ) != true ) {
			if( Bitmap.Config.ARGB_8888.equals(type) != true ) {
				Message.throwExceptionMessage( Message.ImageLoadError, "Unsupported color mode for gray image.");
			}
			final int w = src.getWidth();
			final int h = src.getHeight();
			final int size = w*h;
			int[] work = new int[size];
			IntBuffer workBuf = IntBuffer.wrap(work);
			src.copyPixelsToBuffer(workBuf);
			src.recycle();
			src = null;
			byte[] gray = new byte[size];
			for( int i = 0; i < size; i++ ) {
				gray[i] = (byte) (work[i]&0xff);
			}
			work = null;
			workBuf = null;
			ByteBuffer bb = ByteBuffer.wrap(gray);
			src = Bitmap.createBitmap(w, h, Bitmap.Config.ALPHA_8 );
			src.copyPixelsFromBuffer(bb);
			gray = null;
			bb = null;
			return src;
		} else {
			return src;
		}
	}
}
