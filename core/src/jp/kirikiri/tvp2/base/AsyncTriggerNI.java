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

import java.lang.ref.WeakReference;

import jp.kirikiri.tjs2.Error;
import jp.kirikiri.tjs2.Dispatch2;
import jp.kirikiri.tjs2.NativeInstanceObject;
import jp.kirikiri.tjs2.TJS;
import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tjs2.Variant;
import jp.kirikiri.tjs2.VariantClosure;
import jp.kirikiri.tjs2.VariantException;
import jp.kirikiri.tvp2.TVP;

/**
 * AsyncTrigger is a class for invoking events at asynchronous.
 * Script can trigger event but the event is not delivered immediately,
 * is delivered at next event flush phase.
 *
 * TJS AsyncTrigger native instance
 */
public class AsyncTriggerNI extends NativeInstanceObject {
	private static final int atmNormal = 0, atmExclusive = 1, atmAtIdle = 2;

	static private final String ON_FIRE = "onFire";

	private WeakReference<Dispatch2> mOwner;
	private VariantClosure mActionOwner; // object to send action
	private String mActionName;

	private boolean mCached; // cached operation
	private int mMode; // event mode
	private int mIdlePendingCount;

	public AsyncTriggerNI() {
		//mOwner = null;
		mCached = true;
		//mIdlePendingCount = 0;
		mMode = atmNormal;
		mActionOwner = new VariantClosure(null);
		//mActionOwner.mObject = mActionOwner.mObjThis = null;
		mActionName = EventManager.ActionName;
	}
	public int construct( Variant[] param, Dispatch2 tjs_obj) throws TJSException {
		if( param.length < 1) return Error.E_BADPARAMCOUNT;

		int hr = super.construct( param, tjs_obj);
		if( hr < 0 ) return hr;

		if( param.length >= 2 && param[1].isVoid() != true )
			mActionName = param[1].asString(); // action function to be called

		mActionOwner.set( param[0].asObjectClosure() );
		mOwner = new WeakReference<Dispatch2>(tjs_obj);

		return Error.S_OK;
	}
	public void invalidate() throws VariantException, TJSException {
		TVP.EventManager.cancelSourceEvents(mOwner.get());
		mOwner.clear();

		mActionOwner.mObjThis = mActionOwner.mObject = null;
		//mActionOwner = null;

		super.invalidate();
	}

	public VariantClosure getActionOwner() { return mActionOwner; }
	public String getActionName() { return mActionName; }

	public void trigger() {
		// trigger event
		Dispatch2 owner = mOwner.get();
		if(owner!=null) {
			if(mCached) {
				// remove undelivered events from queue when "Cached" flag is set
				TVP.EventManager.cancelSourceEvents(owner);
			}

			int flags = EventManager.EPT_POST;
			if(mMode == atmExclusive) flags |= EventManager.EPT_EXCLUSIVE;  // fire exclusive event
			if(mMode == atmAtIdle)    flags |= EventManager.EPT_IDLE;       // fire idle event

			TVP.EventManager.postEvent( owner, owner, ON_FIRE, 0, flags, TJS.NULL_ARG );
		}
	}
	public 	void cancel() {
		// cancel event
		Dispatch2 owner = mOwner.get();
		if(owner!=null) TVP.EventManager.cancelSourceEvents(owner);
		mIdlePendingCount = 0;
	}

	public boolean getCached() { return mCached; }
	public void setCached(boolean b) {
		// set cached operation flag.
		// when this flag is set, only one event is delivered at once.
		if(mCached != b) {
			mCached = b;
			cancel(); // all events are canceled
		}
	}

	public int getMode() { return mMode; }
	public void setMode( int m) {
		if( mMode != m) {
			mMode = m;
			cancel();
		}
	}
}
