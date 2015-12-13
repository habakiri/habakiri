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
/**
 *
 */
package jp.kirikiri.tvp2env;

import java.util.ArrayList;

import javax.swing.SwingUtilities;

import jp.kirikiri.tvp2.TVP;
import jp.kirikiri.tvp2.utils.TimerNI;

/**
 * thread for triggering punctual event.
 * normal Windows timer cannot call the timer callback routine at
 * too short interval ( roughly less than 50ms ).
 */
public class TimerThread implements Runnable {
	public static final int SUBMILLI_FRAC_BITS = 4;
	private static final int LEAST_TIMER_INTERVAL = 3;

	private ArrayList<TimerNI> mList;
	/** ペンディング中のタイマーインスタンス */
	ArrayList<TimerNI> mPending;
	boolean mPendingEventsAvailable;
	private boolean mIsTerminated;
	private Thread mThread;

	public TimerThread() {
		mList = new ArrayList<TimerNI>();
		mPending = new ArrayList<TimerNI>();
		mPendingEventsAvailable = false;
		mThread = new Thread(this);
		mThread.setPriority( TimerNI.LimitTimerCapacity ? Thread.NORM_PRIORITY  : Thread.MAX_PRIORITY );
		mThread.setName("TVP Timer Thread");
		mThread.start();
	}
	// protected void finalize() { }

	@Override
	public void run() {
		while( !getTerminated() ) {
			long step_next = -1L; // invalid value
			long curtick = TVP.getTickCount();
			curtick = curtick << SUBMILLI_FRAC_BITS;
			long sleeptime;

			synchronized( this ) {	// thread-protected
				boolean any_triggered = false;

				final int count = mList.size();
				for( int i = 0; i < count; i++ ) {
					TimerNI item = mList.get(i);

					if( !item.getEnabled() || item.getInterval() == 0) continue;

					if( item.getNextTick() < curtick ) {
						long n = (curtick - item.getNextTick()) / item.getInterval();
						n++;
						if( n > 40 ) {
							// too large amount of event at once; discard rest
							item.trigger(1);
							any_triggered = true;
							item.setNextTick(curtick + item.getInterval());
						} else {
							item.trigger((int) n);
							any_triggered = true;
							item.setNextTick(item.getNextTick() + n * item.getInterval() );
						}
					}
					long to_next = item.getNextTick() - curtick;

					if( step_next == -1L ) {
						step_next = to_next;
					} else {
						if(step_next > to_next) step_next = to_next;
					}
				}

				if(step_next != -1L ) {
					// step_next が大きすぎる時、小さくする
					if(step_next > Integer.MAX_VALUE)
						sleeptime = Integer.MAX_VALUE; // step_next より小さい値を入れる
					else
						sleeptime = step_next;
				} else {
					sleeptime = Long.MAX_VALUE; // 永遠に待つ代わりに大きい値を入れる
				}

				if( mList.size() == 0 ) sleeptime = Long.MAX_VALUE;

				if( any_triggered ) {
					// ペンディングイベントを swing スレッドで呼ぶ
					if( !mPendingEventsAvailable ) {
						mPendingEventsAvailable = true;
						SwingUtilities.invokeLater(new Runnable() {
							// swing スレッドで実行する
							public void run() {
								if(!getTerminated() ) {
									// pending イベント発行
									synchronized( this ) {	// thread-protected
										final int count = mPending.size();
										for( int i = 0; i < count; i++ ) {
											TimerNI item = mPending.get(i);
											item.firePendingEventsAndClear();
										}
										mPending.clear();
										mPendingEventsAvailable = false;
									}
								}
							}
						});
					}
				}
			}	// end-of-thread-protected

			// now, sleeptime has sub-milliseconds precision but we need millisecond
			// precision time.
			if( sleeptime != Long.MAX_VALUE )
				sleeptime = (sleeptime >> SUBMILLI_FRAC_BITS) + (((sleeptime & ((1<<SUBMILLI_FRAC_BITS)-1)) != 0) ? 1: 0); // round up

			// 小さすぎる時は LEAST_TIMER_INTERVAL は最低待つようにする
			if(sleeptime != Long.MAX_VALUE && sleeptime < LEAST_TIMER_INTERVAL)
				sleeptime = LEAST_TIMER_INTERVAL;

			if( Thread.interrupted() != true ) {
				try {
					Thread.sleep(sleeptime);	// sleeptime 経過するか、割り込みによって起こされるまで待つ
				} catch( InterruptedException e ) {
					// 割り込みで、Sleep解除
				}
			}
		}
	}

	private void addItem(TimerNI item) {
		synchronized( this ) {	// thread-protected
			int index = mList.indexOf(item);
			if( index == -1 ) mList.add(item);
		}
	}
	private boolean removeItem(TimerNI item) {
		synchronized( this ) {	// thread-protected
			final int count = mList.size();
			for( int i = count-1; i >= 0; i-- ) {
				TimerNI t = mList.get(i);
				if( t == item ) {
					mList.remove(i);
				}
			}
			// also remove from the Pending list
			removeFromPendingItem(item);
			return mList.size() != 0;
		}
	}
	private void removeFromPendingItem(TimerNI item) {
		// remove item from pending list
		final int count = mPending.size();
		for( int i = count-1; i >= 0; i-- ) {
			TimerNI t = mPending.get(i);
			if( t == item ) {
				mPending.remove( i );
			}
		}
		item.zeroPendingCount();
	}
	private void registerToPendingItem(TimerNI item) {
		// register item to the pending list
		mPending.add(item);
	}

	public void setEnabled(TimerNI item, boolean enabled) { // managed by this class
		synchronized( this ) {	// thread-protected
			item.internalSetEnabled(enabled);
			if( enabled ) {
				item.setNextTick((TVP.getTickCount() << SUBMILLI_FRAC_BITS) + item.getInterval());
			} else {
				item.cancelEvents();
				item.zeroPendingCount();
			}
		} // end-of-thread-protected

		if(enabled) mThread.interrupt();
	}
	public void setInterval(TimerNI item, long interval) { // managed by this class
		synchronized( this ) {	// thread-protected
			item.internalSetInterval(interval);
			if( item.getEnabled() ) {
				item.cancelEvents();
				item.zeroPendingCount();
				item.setNextTick((TVP.getTickCount() << SUBMILLI_FRAC_BITS) + item.getInterval());
			}
		}
		if(item.getEnabled()) mThread.interrupt();
	}
	private boolean getTerminated() { return mIsTerminated; }

	public static void init() {
		if( TVP.TimerThread == null ) {
			// TVPStartTickCount(); // in TickCount.cpp
			TVP.TimerThread = new TimerThread();
		}
	}
	public static void uninit() {
		if( TVP.TimerThread != null ) {
			// スレッド停止した方がいい
			TVP.TimerThread.mIsTerminated = true;
			try {
				TVP.TimerThread.mThread.interrupt();
				TVP.TimerThread.mThread.join();
			} catch (InterruptedException e) {
			}
			TVP.TimerThread = null;
		}
	}

	public static void add(TimerNI item) {
		// at this point, item->GetEnebled() must be false.
		init();
		TVP.TimerThread.addItem(item);
	}
	public static void remove(TimerNI item) {
		if( TVP.TimerThread != null ) {
			if(!TVP.TimerThread.removeItem(item) ) uninit();
		}
	}

	public static void removeFromPending(TimerNI item) {
		if( TVP.TimerThread != null ) {
			TVP.TimerThread.removeFromPendingItem(item);
		}
	}
	public static void registerToPending(TimerNI item) {
		if( TVP.TimerThread != null ) {
			TVP.TimerThread.registerToPendingItem(item);
		}
	}
}
