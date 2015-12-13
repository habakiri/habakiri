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
package jp.kirikiri.tvp2.utils;

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
import jp.kirikiri.tvp2.base.EventManager;
import jp.kirikiri.tvp2env.TimerThread;


public class TimerNI extends NativeInstanceObject {
	private static final int DEFAULT_TIMER_CAPACITY = 6;
	private static final int
		atmNormal = 0,
		atmExclusive = 1,
		atmAtIdle = 2;
	public static boolean LimitTimerCapacity;
	private static final String ON_TIMER = "onTimer";

	/** */
	private WeakReference<Dispatch2> mOwner;
	/** object to send action */
	private VariantClosure mActionOwner;
	/** serial number for event tag */
	private int mCounter;
	/** max queue size for this timer object */
	private int mCapacity;
	private String mActionName;
	/** trigger mode */
	private int mMode;

	private long mInterval;
	private long mNextTick;
	private int mPendingCount;
	private boolean mEnabled;

	public static void initialize() {
		LimitTimerCapacity = false;
	}
	public TimerNI() {
		//mOwner = null;
		//mCounter = 0;
		mCapacity = DEFAULT_TIMER_CAPACITY;
		mActionOwner = new VariantClosure(null);
		//mActionOwner.mObject = mActionOwner.mObjThis = null;
		mActionName = EventManager.ActionName;
		mMode = atmNormal;


		//mNextTick = 0;
		mInterval = 1000;
		//mPendingCount = 0;
		//mEnabled = false;
	}
	public int construct( Variant[] param, Dispatch2 tjs_obj ) throws VariantException, TJSException {
		if( param.length < 1 ) return Error.E_BADPARAMCOUNT;

		int hr = super.construct( param, tjs_obj);
		if( hr < 0 ) return hr;
		if( param.length >= 2 && param[1].isVoid() != true )
			mActionName = param[1].asString(); // action function to be called

		//mActionOwner = param[0].asObjectClosure();
		mActionOwner.set( param[0].asObjectClosure() );
		mOwner = new WeakReference<Dispatch2>(tjs_obj);

		TimerThread.add(this);
		return Error.S_OK;
	}
	public void invalidate() throws VariantException, TJSException {
		TimerThread.remove(this);
		zeroPendingCount();
		cancelEvents();

		TVP.EventManager.cancelSourceEvents(mOwner.get());
		mOwner.clear();
		mActionOwner.mObjThis = mActionOwner.mObject = null;
		super.invalidate();
	}

	private void fire( int n ) {
		Dispatch2 owner = mOwner.get();
		if( owner == null) return;
		if( TVP.EventManager == null ) return;
		int count = TVP.EventManager.countEventsInQueue( owner, owner, ON_TIMER, 0);

		int cap = LimitTimerCapacity ? 1 : (mCapacity == 0 ? 65535 : mCapacity);
			// 65536 should be considered as to be no-limit.

		int more = cap - count;
		if( more > 0 ) {
			if(n > more) n = more;
			owner = mOwner.get();
			if( owner != null ) {
				int tag = 1 + (mCounter << 1);
				int flags = EventManager.EPT_POST|EventManager.EPT_DISCARDABLE;
				switch(mMode)
				{
				case atmNormal:			flags |= EventManager.EPT_NORMAL; break;
				case atmExclusive:		flags |= EventManager.EPT_EXCLUSIVE; break;
				case atmAtIdle:			flags |= EventManager.EPT_IDLE; break;
				}
				while( n-- > 0 ) {
					TVP.EventManager.postEvent( owner, owner, ON_TIMER, tag, flags, TJS.NULL_ARG );
				}
			}
			mCounter++;
		}
	}
	public void cancelEvents() {
		// cancel all events
		Dispatch2 owner = mOwner.get();
		if( owner != null ) {
			TVP.EventManager.cancelEvents( owner, owner, ON_TIMER, 0 );
		}
	}
	private boolean areEventsInQueue() {
		// are events in event queue ?
		Dispatch2 owner = mOwner.get();
		if( owner != null ) {
			return TVP.EventManager.areEventsInQueue( owner, owner, ON_TIMER, 0 );
		}
		return false;
	}

	public VariantClosure getActionOwner() { return mActionOwner; }
	public final String getActionName() { return mActionName; }

	public int getCapacity() { return mCapacity; }
	public void setCapacity( int c ) { mCapacity = c; }

	public int getMode() { return mMode; }
	public void setMode( int mode) { mMode = mode; }

	public void internalSetInterval(long n) { mInterval = n; }
	public void setInterval( long n ) {
		if( TVP.TimerThread != null ) TVP.TimerThread.setInterval(this, n);
	}
	public long getInterval() { return mInterval; }

	public void zeroPendingCount() { mPendingCount = 0; }

	public void setNextTick( long n ) { mNextTick = n; }
	public long getNextTick() { return mNextTick; }

	public void internalSetEnabled(boolean b) { mEnabled = b; }
	public void setEnabled(boolean b) {
		if( TVP.TimerThread != null ) TVP.TimerThread.setEnabled(this, b);
	}
	public boolean getEnabled() { return mEnabled; }

	public void trigger( int n ) {
		if(mPendingCount == 0) TimerThread.registerToPending(this);
		mPendingCount += n;
	}
	public void firePendingEventsAndClear() {
		// fire all pending events and clear the pending event count
		if( mPendingCount != 0) {
			fire(mPendingCount);
			zeroPendingCount();
		}
	}

	// private void cancelTrigger() {}
}
