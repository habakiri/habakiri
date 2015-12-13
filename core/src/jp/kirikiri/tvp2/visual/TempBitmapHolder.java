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

import java.util.ArrayList;

import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tjs2.TJSScriptError;
import jp.kirikiri.tjs2.TJSScriptException;
import jp.kirikiri.tvp2.TVP;
import jp.kirikiri.tvp2.base.CompactEventCallbackInterface;

public class TempBitmapHolder implements CompactEventCallbackInterface {
	static final private int INIT_SIZE = 32;
	static final private int INIT_BPP = 32;
	static final private int INIT_COLOR = 0x00FFFFFF; //TVP_RGBA2COLOR(255, 255, 255, 0));

	static private TempBitmapHolder TempBitmapHolder;
	static public void initialize() {
		TempBitmapHolder = null;
	}
	public static void finalizeApplication() {
		TempBitmapHolder = null;
	}

	private BaseBitmap mBitmap;
	private ArrayList<BaseBitmap> mTemporaries;
	private int mTempLevel;
	private boolean mTempCompactInit;

	private TempBitmapHolder() throws TJSException {
		mBitmap = new BaseBitmap(INIT_SIZE,INIT_SIZE,INIT_BPP);
		mTemporaries = new ArrayList<BaseBitmap>();
		//mTempLevel = 0;
		//mTempCompactInit = false;
		// the default image must be a transparent, white colored rectangle
		mBitmap.fill( new Rect(0, 0, INIT_SIZE, INIT_SIZE), INIT_COLOR );
	}

	protected void finalize() {
		final int count = mTemporaries.size();
		for( int i = 0; i < count; i++ ) {
			mTemporaries.set(i,null);
		}
		if(mTempCompactInit && TVP.EventManager != null ) TVP.EventManager.removeCompactEventHook(this);
	}

	private BaseBitmap internalGetTemp( int w, int h, boolean fit ) throws TJSException {
		// compact initialization
		if(!mTempCompactInit) {
			TVP.EventManager.addCompactEventHook(this);
			mTempCompactInit = true;
		}

		// align width to even
		if(!fit) w += (w & 1);

		// get temporary bitmap (nested)
		mTempLevel++;
		if(mTempLevel > mTemporaries.size()) {
			// increase buffer size
			BaseBitmap bmp = new BaseBitmap(w, h, INIT_BPP);
			mTemporaries.add(bmp);
			return bmp;
		} else {
			BaseBitmap bmp = mTemporaries.get(mTempLevel -1);
			if(!fit) {
				int bw = bmp.getWidth();
				int bh = bmp.getHeight();
				if(bw < w || bh < h) {
					// increase image size
					bmp.setSize(bw > w ? bw:w, bh > h ? bh:h, false);
				}
			} else {
				// the size must be fitted
				int bw = bmp.getWidth();
				int bh = bmp.getHeight();
				if(bw != w || bh != h)
					bmp.setSize(w, h, false);
			}
			return bmp;
		}
	}

	private void internalFreeTemp() {
		if(mTempLevel == 0) return ; // this must be a logical failure
		mTempLevel--;
		//compactTempBitmap(); // always compact
	}

	private void compactTempBitmap() {
		// compact tmporary bitmap cache
		final int count = mTemporaries.size();
		for( int i = count-1; i >= mTempLevel; i-- ) {
			BaseBitmap bmp = mTemporaries.get(i);
			bmp.purgeImage();
			mTemporaries.remove(i);
		}
	}
	@Override
	public void onCompact(int level) throws TJSScriptException, TJSScriptError, TJSException {
		// OnCompact method from tTVPCompactEventCallbackIntf
		// called when the application is idle, deactivated, minimized, or etc...
		if(level >= CompactEventCallbackInterface.COMPACT_LEVEL_DEACTIVATE) compactTempBitmap();
	}

	static BaseBitmap get() throws TJSException {
		if( TempBitmapHolder == null ) TempBitmapHolder = new TempBitmapHolder();
		return TempBitmapHolder.mBitmap;
	}

	static BaseBitmap getTemp( int w, int h ) throws TJSException {
		return getTemp( w, h, false );
	}
	static BaseBitmap getTemp( int w, int h, boolean fit ) throws TJSException {
		if( TempBitmapHolder == null ) TempBitmapHolder = new TempBitmapHolder();
		return TempBitmapHolder.internalGetTemp(w, h, fit);
	}

	static void freeTemp() {
		if( TempBitmapHolder != null ) {
			TempBitmapHolder.internalFreeTemp();
		}
	}
}
