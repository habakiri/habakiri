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
package jp.kirikiri.tvp2.sound;


import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import jp.kirikiri.tjs2.Error;
import jp.kirikiri.tjs2.BinaryStream;
import jp.kirikiri.tjs2.Dispatch2;
import jp.kirikiri.tjs2.NativeInstanceObject;
import jp.kirikiri.tjs2.TJS;
import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tjs2.Variant;
import jp.kirikiri.tjs2.VariantClosure;
import jp.kirikiri.tjs2.VariantException;
import jp.kirikiri.tvp2.TVP;
import jp.kirikiri.tvp2.base.EventManager;
import jp.kirikiri.tvp2.base.Storage;
import jp.kirikiri.tvp2.msg.Message;
import jp.kirikiri.tvp2env.SoundEventTimer;
import jp.kirikiri.tvp2env.SoundStream;


public class WaveSoundBufferNI extends NativeInstanceObject {

	static public final int SB_BEAT_INTERVAL = 60;

	/** data is not specified */
	static public final int ssUnload = 0;
	/** stop */
	static public final int ssStop = 1;
	/** play */
	static public final int ssPlay = 2;
	/** pause */
	static public final int ssPause = 3;


	static private final String UNLOAD = "unload";
	static private final String PLAY = "play";
	static private final String STOP = "stop";
	static private final String UNKNOWN = "unknown";
	static private final String ON_STATUS_CHANGED = "onStatusChanged";

	private WeakReference<Dispatch2> mOwner; // owner object
	private VariantClosure mActionOwner; // object to send action
	private int mStatus; // status
	private boolean mCanDeliverEvents;
	private boolean mInFading;
	private int mTargetVolume; // distination volume
	private int mDeltaVolume; // delta volume for each interval
	private int mFadeCount; // beat count over fading
	private int mBlankLeft; // blank time until fading

	private int mVolume;
	private int mVolume2;

	private SoundStream mSoundStream;

	private static SoundEventTimer mSoundBufferTimer;
	private static ArrayList<WaveSoundBufferNI> mSoundBufferVector;
	public static void initialize() {
		mSoundBufferVector = new ArrayList<WaveSoundBufferNI>();
		mSoundBufferTimer = new SoundEventTimer();
	}
	public static void finalizeApplication() {
		mSoundBufferVector = null;
		mSoundBufferTimer = null;
	}
	public static void doSoundEvents() {
		final int count = mSoundBufferVector.size();
		for( int i = 0; i < count; i++ ) {
			WaveSoundBufferNI w = mSoundBufferVector.get(i);
			if( w != null ) {
				w.timerBeatHandler();
			}
		}
	}
	public static void addSoundBuffer( WaveSoundBufferNI buf ) {
		if( mSoundBufferVector.size() == 0 ) {
			mSoundBufferTimer.startIfStop();
			//if( mSoundBufferTimer.isRunning() == false ) {
			//	mSoundBufferTimer.start();
			//}
		}
		mSoundBufferVector.add(buf);
	}
	public static void removeSoundBuffer( WaveSoundBufferNI buf ) {
		if( mSoundBufferVector.size() != 0 ) {
			mSoundBufferVector.remove(buf);
		}
		if( mSoundBufferVector.size() == 0 ) {
			mSoundBufferTimer.stop();
		}
	}

	public WaveSoundBufferNI() {
		//mInFading = false;
		mCanDeliverEvents = true;
		//mOwner = null;
		mActionOwner = new VariantClosure(null);
		//mActionOwner = new WeakReference<VariantClosure>();
		//mActionOwner.mObject = mActionOwner.mObjThis = NULL;
		//mStatus = ssUnload;

		mVolume =  100000;
		mVolume2 = 100000;
	}
	/*
	protected void finalize() {
		mOwner = null;
		mActionOwner = null;
		try {
			super.finalize();
		} catch (Throwable e) {
		}
	}
	*/
	public final int construct( Variant[] param, Dispatch2 tjs_obj ) throws VariantException, TJSException {
		if( param.length < 1) return Error.E_BADPARAMCOUNT;

		int hr = super.construct(param, tjs_obj);
		if( hr < 0 ) return hr;

		mActionOwner.set( param[0].asObjectClosure() );
		mOwner = new WeakReference<Dispatch2>(tjs_obj);

		mSoundStream = new SoundStream(this);
		addSoundBuffer(this);
		return Error.S_OK;
	}

	public final void invalidate() throws VariantException, TJSException {

		removeSoundBuffer(this);

		mCanDeliverEvents = false;
		TVP.EventManager.cancelSourceEvents(mOwner.get());
		mOwner.clear();

		mActionOwner.mObjThis = mActionOwner.mObject = null;
		mSoundStream = null;

		super.invalidate();
		/*
		clear();
		destroySoundBuffer();
		//if(Thread) delete Thread, Thread = NULL;
		removeWaveSoundBuffer(this);
		*/
	}

	static private final String ON_LABEL = "onLabel";
	public void invokeLabelEvent( final String name ) {
		// the invoked event is to be delivered asynchronously.
		// the event is to be erased when the SetStatus is called, but it's ok.
		// ( SetStatus calls TVPCancelSourceEvents(Owner); )
		Dispatch2 owner = mOwner.get();
		if( owner != null && mCanDeliverEvents ) {
			Variant[] param = {new Variant(name)};
			TVP.EventManager.postEvent( owner, owner, ON_LABEL, 0, EventManager.EPT_POST, param );
		}
	}

	public void open(String storagename) throws TJSException {
		mSoundStream.stop();
		BinaryStream stream = Storage.createStream(storagename,BinaryStream.READ);
		try {
			//mSoundStream.openFromStream( stream.getInputStream(), storagename );
			mSoundStream.openFromStream( stream, storagename );
			//setStatus(ssStop);
		} catch (IOException e) {
			Message.throwExceptionMessage( Message.CannotLoadSound, storagename );
		}
	}

	public void play() {
		setVolumeToSoundBuffer();
		mSoundStream.play();
		setStatus(ssPlay);
	}

	public void stop() {
		mSoundStream.stop();
		if(mStatus != ssUnload) setStatus(ssStop);
	}
	public void timerBeatHandler() {
		if( mOwner.get() == null ) return;
		if( !mInFading ) return;

		if( mBlankLeft != 0 ) {
			mBlankLeft -= SB_BEAT_INTERVAL;
			if( mBlankLeft < 0 )  mBlankLeft = 0;
		} else if( mFadeCount != 0 ) {
			if( mFadeCount == 1 ) {
				stopFade( true, true );
			} else {
				mFadeCount--;
				int v = getVolume();
				v += mDeltaVolume;
				if( v < 0 ) v = 0;
				if( v > 100000 ) v = 100000;
				setVolume(v);
			}
		}
	}

	public void fade(int to, int time, int blanktime ) throws TJSException {
		// start fading
		if(mOwner.get()==null) return;

		if( time <= 0 || blanktime < 0 ) {
			Message.throwExceptionMessage(Message.InvalidParam);
		}

		// stop current fade
		if(mInFading) stopFade(false, false);

		// set some parameters
		mDeltaVolume = (to - getVolume()) * SB_BEAT_INTERVAL / time;
		if( mDeltaVolume == 0 ) mDeltaVolume = 1;
		mTargetVolume = to;
		mFadeCount = time / SB_BEAT_INTERVAL;
		mBlankLeft = blanktime;
		mInFading = true;
		if(mFadeCount == 0 && mBlankLeft == 0) stopFade(false, true);
	}

	static private final String onFadeCompleted = "onFadeCompleted";
	public void stopFade( boolean async, boolean settargetvol ) {
		// stop fading
		Dispatch2 owner = mOwner.get();
		if( owner == null ) return;
		if( mInFading ) {
			mInFading = false;

			if( settargetvol ) setVolume(mTargetVolume);

			// post "onFadeCompleted" event to the owner
			if( mCanDeliverEvents ) {
				TVP.EventManager.postEvent( owner, owner, onFadeCompleted, 0,
					async ? EventManager.EPT_POST : EventManager.EPT_IMMEDIATE, TJS.NULL_ARG );
			}
		}
	}

	public VariantClosure getActionOwner() {
		return mActionOwner;
	}

	public boolean getLooping() {
		return mSoundStream.getLooping();
	}

	public void setLooping(boolean b) {
		mSoundStream.setLooping(b);
	}

	public boolean getPaused() {
		return mSoundStream.getPaused();
	}

	public void setPaused(boolean b) {
		mSoundStream.setPaused(b);
	}

	public int getPan() {
		return mSoundStream.getPan();
	}

	public void setPan(int v) {
		mSoundStream.setPan(v);
	}

	public int getPosition() {
		return mSoundStream.getPosition();
	}

	public void setPosition(int pos) {
		mSoundStream.setPosition(pos);
	}

	public int getSamplePosition() {
		return mSoundStream.getSamplePosition();
	}

	public void setSamplePosition(int sample) {
		mSoundStream.setSamplePosition(sample);
	}

	public int getTotalTime() {
		return mSoundStream.getTotalTime();
	}

	public int getVolume() {
		return mVolume;
	}

	public void setVolume(int v) {
		if(v < 0) v = 0;
		if(v > 100000) v = 100000;

		if(mVolume != v) {
			mVolume = v;
			setVolumeToSoundBuffer();
		}
	}

	public int getVolume2() {
		return mVolume2;
	}

	public void setVolume2(int v) {
		if(v < 0) v = 0;
		if(v > 100000) v = 100000;

		if( mVolume2 != v ) {
			mVolume2 = v;
			setVolumeToSoundBuffer();
		}
	}

	private void setVolumeToSoundBuffer() {
		int v = (mVolume / 10) * (mVolume2 / 10) / 1000;
		//v = (v / 10) * (GlobalVolume / 10) / 1000;
		mSoundStream.setVolume(v);
	}

	public void setPos(double x, double y, double z) { }
	public int getPosX() { return 0; }
	public void setPosX(double x) { }
	public double getPosY() { return 0; }
	public void setPosY(double y) { }
	public double getPosZ() { return 0; }
	public void setPosZ(double z) { }

	public String getStatusString() {
		switch( mStatus ) {
		case ssUnload:	return UNLOAD;
		case ssPlay:	return PLAY;
		case ssStop:	return STOP;
		default:		return UNKNOWN;
		}
	}
	public void setStatus( int s ) {
		if( mStatus != s) {
			mStatus = s;
			Dispatch2 owner = mOwner.get();
			if(owner!=null) {
				// Cancel Previous un-delivered Events
				TVP.EventManager.cancelSourceEvents(owner);
				// fire
				if(mCanDeliverEvents) {
					// fire onStatusChanged event
					Variant param = new Variant(getStatusString());
					Variant[] params = {param};
					TVP.EventManager.postEvent( owner, owner, ON_STATUS_CHANGED, 0, EventManager.EPT_IMMEDIATE, params );
				}
			}
		}
	}
	public void setStatusAsync( int s ) {
		// asynchronous version of SetStatus
		// the event may not be delivered immediately.
		if( mStatus != s ) {
			mStatus = s;
			if( mCanDeliverEvents ) {
				// fire onStatusChanged event
				Dispatch2 owner = mOwner.get();
				if( owner != null ) {
					Variant param = new Variant(getStatusString());
					Variant[] params = {param};
					TVP.EventManager.postEvent( owner, owner, ON_STATUS_CHANGED, 0, EventManager.EPT_POST, params );
				}
			}
		}
	}

	public int getFrequency() {
		return mSoundStream.getFrequency();
	}

	public void setFrequency(int freq) {
		mSoundStream.setFrequency(freq);
	}

	public int getBitsPerSample() {
		return mSoundStream.getBitsPerSample();
	}

	public int getChannels() {
		return mSoundStream.getChannels();
	}

	public Dispatch2 getWaveFlagsObject() { return null; }
	public Dispatch2 getWaveLabelsObject() { return null; }
	public Dispatch2 getWaveFiltersObject() { return null; }
}
