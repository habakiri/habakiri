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

import jp.kirikiri.tjs2.Error;
import jp.kirikiri.tjs2.Dispatch2;
import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tjs2.Variant;
import jp.kirikiri.tjs2.VariantException;
import jp.kirikiri.tvp2.TVP;

public class Event implements Runnable {

	private Dispatch2 mTarget;
	private Dispatch2 mSource;
	private String mEventName;
	private int mTag;
	private Variant[] mArgs;
	private int mFlags;
	private long mSequence;

	public Event( Dispatch2 target, Dispatch2 source, String eventname, int tag, Variant[] args, int flags ) {
		// constructor
		// eventname is not a const object but this object only touch to
		// eventname.GetHint()
		//mArgs = null;
		//mTarget = null;
		//mSource = null;

		mSequence = TVP.EventManager.getSequenceNumber();
		mEventName = eventname;
		final int count = args.length;
		mArgs = new Variant[count];
		for( int i = 0; i < count; i++ ) {
			mArgs[i] = new Variant(args[i]);
		}
		mTarget = target;
		mSource = source;
		mTag = tag;
		mFlags = flags;
	}


	public Event( final Event ref ){
		// copy constructor
		//mArgs = null;
		//mTarget = null;
		//mSource = null;

		mEventName = ref.mEventName;
		final int count = ref.mArgs.length;
		mArgs = new Variant[count];
		for( int i = 0; i < count; i++ ) {
			mArgs[i] = new Variant( ref.mArgs[i] );
		}
		mTarget = ref.mTarget;
		mSource = ref.mSource;
		mTag = ref.mTag;
	}

	/*
	protected void finalize() {
		mArgs = null;
		mTarget = null;
		mSource = null;
	}
	*/

	public void deliver() throws VariantException, TJSException {
		int hr = mTarget.isValid(0, null, mTarget);
		if( hr != Error.S_TRUE && hr != Error.E_NOTIMPL ) return; // The target had been invalidated
		mTarget.funcCall( 0, mEventName, null, mArgs, mTarget );
	}

	@Override
	public void run() {
		try {
			deliver();
		} catch (VariantException e) {
		} catch (TJSException e) {
		}
	}
	static public void immediate( Dispatch2 target, Dispatch2 source, String eventname, int tag, Variant[] args, int flags ) throws VariantException, TJSException {
		int hr = target.isValid(0, null, target);
		if( hr != Error.S_TRUE && hr != Error.E_NOTIMPL ) return; // The target had been invalidated
		target.funcCall( 0, eventname, null, args, target );
	}


	public Dispatch2 getTarget() { return mTarget; }
	public Dispatch2 getSource() { return mSource; }
	public final String getEventName() { return mEventName; }
	public int getTag() { return mTag; }
	public int getFlags() { return mFlags; }
	public long getSequence() { return mSequence; }

}
