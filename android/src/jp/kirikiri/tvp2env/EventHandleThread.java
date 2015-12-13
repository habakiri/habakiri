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

import java.util.concurrent.ConcurrentLinkedQueue;

import android.util.Log;

import jp.kirikiri.tjs2.TJS;
import jp.kirikiri.tvp2.TVP;
import jp.kirikiri.tvp2.sound.WaveSoundBufferNI;

public class EventHandleThread implements Runnable {

	static final private String THREAD_NAME = "Main VM Thread";
	private Thread mThread;
	private ConcurrentLinkedQueue<Runnable> mEvents;
	private boolean mIsEnableSoundEvent;
	private boolean mIsTerminate;
	private long mLastSoundTick;
	static final private int SOUND_CYCLE = WaveSoundBufferNI.SB_BEAT_INTERVAL;
	static final private int TERMINATE_WAIT_MAX = 10000; // 10秒まで待つ

	public EventHandleThread( Runnable first ) {
		mEvents = new ConcurrentLinkedQueue<Runnable>();
		mEvents.add( first );

		mIsTerminate = false;

		// スタックサイズ 64KB で実行する
		mThread = new Thread( Thread.currentThread().getThreadGroup(), this, THREAD_NAME, 64*1024 );
		mThread.start();
	}
	/**
	 * イベントハンドラスレッドで実行するオブジェクトを追加する
	 * @param run 実行対象オブジェクト
	 */
	public void post( Runnable run ) {
		mEvents.add( run );
	}
	public void setSoundEvent( boolean b ) {
		if( b ) {
			if( mIsEnableSoundEvent == false ) {
				mLastSoundTick = System.currentTimeMillis();
				mIsEnableSoundEvent = true;
			}
		} else {
			mIsEnableSoundEvent = false;
		}
	}

	@Override
	public void run() {
		ApplicationSystem app = TVP.Application.get();
		boolean running = app != null && app.CurrentContext != null;
		app = null;
		while( running ) {
			long tick = System.currentTimeMillis();

			TJS.doObjectFinalize();

			if( mIsEnableSoundEvent ) {
				long duration = tick - mLastSoundTick;
				if( duration >= SOUND_CYCLE ) {
					WaveSoundBufferNI.doSoundEvents();
					mLastSoundTick += SOUND_CYCLE;
				}
			}
			boolean hasEvent = false;
			//synchronized( mEvents ) {
				// イベントがあるかチェックして、あったらそれを実行する
				hasEvent = mEvents.isEmpty() == false;
				if( hasEvent ) {
					Runnable r = mEvents.poll();
					while( r != null ) {
						r.run();
						r = mEvents.poll();
					}
				}
			//}
			if( hasEvent == false ) {
				// イベントが空の時、アイドルイベントを実行する
				if( TVP.MainForm != null ) TVP.MainForm.applicationIdle();
			}
			long end = System.currentTimeMillis();
			if( hasEvent == false ) {
				// イベントが空だった時は、少し待つ
				long waitTime = 16 - (end - tick);
				if( waitTime < 1 ) waitTime = 1;
				try {
					Thread.sleep(waitTime);
				} catch (InterruptedException e) {
				}
			}
			app = TVP.Application != null ? TVP.Application.get() : null;
			running = app != null && app.CurrentContext != null && mIsTerminate == false;
			app = null;
		}
		Log.v("EVENT","finish Main VM thread");
		System.gc();
	}
	public void interrupt() {
		if( mThread != null ) mThread.interrupt();
	}
	public void terminateThread() {
		mIsTerminate = true;
	}
	public void waitTerminate() {
		if( mThread != null ) {
			try {
				mThread.join(TERMINATE_WAIT_MAX);
			} catch (InterruptedException e) {
			}
		}
	}
}
