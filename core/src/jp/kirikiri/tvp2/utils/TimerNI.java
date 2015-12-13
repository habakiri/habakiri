/**
 ******************************************************************************
 * Copyright (c), Takenori Imoto
 * �� software http://www.kaede-software.com/
 * All rights reserved.
 ******************************************************************************
 * �\�[�X�R�[�h�`�����o�C�i���`�����A�ύX���邩���Ȃ������킸�A�ȉ��̏�����
 * �����ꍇ�Ɍ���A�ĔЕz����юg�p��������܂��B
 *
 * �E�\�[�X�R�[�h���ĔЕz����ꍇ�A��L�̒��쌠�\���A�{�����ꗗ�A����щ��L�Ɛ�
 *   �������܂߂邱�ƁB
 * �E�o�C�i���`���ōĔЕz����ꍇ�A�Еz���ɕt���̃h�L�������g���̎����ɁA��L��
 *   ���쌠�\���A�{�����ꗗ�A����щ��L�Ɛӏ������܂߂邱�ƁB
 * �E���ʂɂ����ʂ̋��Ȃ��ɁA�{�\�t�g�E�F�A����h���������i�̐�`�܂��͔̔�
 *   ���i�ɁA�g�D�̖��O�܂��̓R���g���r���[�^�[�̖��O���g�p���Ă͂Ȃ�Ȃ��B
 *
 * �{�\�t�g�E�F�A�́A���쌠�҂���уR���g���r���[�^�[�ɂ���āu����̂܂܁v��
 * ����Ă���A�����َ����킸�A���ƓI�Ȏg�p�\���A����ѓ���̖ړI�ɑ΂���K
 * �����Ɋւ���Öق̕ۏ؂��܂߁A�܂�����Ɍ��肳��Ȃ��A�����Ȃ�ۏ؂�����܂�
 * ��B���쌠�҂��R���g���r���[�^�[���A���R�̂�������킸�A���Q�����̌�������
 * ����킸�A���ӔC�̍������_��ł��邩���i�ӔC�ł��邩�i�ߎ����̑��́j�s�@
 * �s�ׂł��邩���킸�A���ɂ��̂悤�ȑ��Q����������\����m�炳��Ă����Ƃ�
 * �Ă��A�{�\�t�g�E�F�A�̎g�p�ɂ���Ĕ��������i��֕i�܂��͑�p�T�[�r�X�̒��B�A
 * �g�p�̑r���A�f�[�^�̑r���A���v�̑r���A�Ɩ��̒��f���܂߁A�܂�����Ɍ��肳���
 * ���j���ڑ��Q�A�Ԑڑ��Q�A�����I�ȑ��Q�A���ʑ��Q�A�����I���Q�A�܂��͌��ʑ��Q��
 * ���āA��ؐӔC�𕉂�Ȃ����̂Ƃ��܂��B
 ******************************************************************************
 * �{�\�t�g�E�F�A�́A�g���g��2 ( http://kikyou.info/tvp/ ) �̃\�[�X�R�[�h��Java
 * �ɏ������������̂��ꕔ�g�p���Ă��܂��B
 * �g���g��2 Copyright (C) W.Dee <dee@kikyou.info> and contributors
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
