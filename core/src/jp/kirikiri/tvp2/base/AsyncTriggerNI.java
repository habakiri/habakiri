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
