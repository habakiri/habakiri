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

import java.lang.ref.WeakReference;

import jp.kirikiri.tjs2.BinaryStream;
import jp.kirikiri.tjs2.Dispatch2;
import jp.kirikiri.tjs2.Error;
import jp.kirikiri.tjs2.NativeInstanceObject;
import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tjs2.Variant;
import jp.kirikiri.tjs2.VariantClosure;
import jp.kirikiri.tvp2.TVP;
import jp.kirikiri.tvp2.base.EventManager;
import jp.kirikiri.tvp2.base.Storage;
import jp.kirikiri.tvp2.msg.Message;
import jp.kirikiri.tvp2.sound.WaveSoundBufferNI;
import jp.kirikiri.tvp2.utils.DebugClass;
import jp.kirikiri.tvp2env.VideoOverlay;
import jp.kirikiri.tvp2env.WindowForm;

public class VideoOverlayNI extends NativeInstanceObject {
	public final int
		perLoop = 0, // the event is by loop rewind
		perPeriod = 1, // the event is by period point specified by the user
		perPrepare = 2, // the event is by prepare() method
		perSegLoop = 3; // the event is by segment loop rewind

	/** data is not specified */
	static public final int ssUnload = WaveSoundBufferNI.ssUnload;
	/** stop */
	static public final int ssStop = WaveSoundBufferNI.ssStop;
	/** play */
	static public final int ssPlay = WaveSoundBufferNI.ssPlay;
	/** pause */
	static public final int ssPause = WaveSoundBufferNI.ssPause;

	static private final String ON_STATUS_CHANGED = "onStatusChanged";
	static private final String ON_CALLBACK_COMMAND = "onCallbackCommand";
	static private final String ON_PERIOD = "onPeriod";
	static private final String ON_FRAME_UPDATE = "onFrameUpdate";

	private VideoOverlay mVideoOverlay;
	private StringBuilder mMessageBuilder;

	private WeakReference<Dispatch2> mOwner;
	private boolean mCanDeliverEvents;
	private WindowNI mWindow;
	private VariantClosure mActionOwner;
	private int mStatus; // status

	private Rect mRect;
	private boolean mVisible;
	private LayerNI mLayer1;
	private LayerNI mLayer2;
	private int mMode;
	private boolean mLoop;
	private boolean mIsPrepare;
	private int mSegLoopStartFrame;
	private int mSegLoopEndFrame;
	private boolean mIsEventPast;
	private int mEventFrame;

	public VideoOverlayNI() {
		mActionOwner = new VariantClosure(null);
		mStatus = ssUnload;
		mOwner = null;
		mCanDeliverEvents = true;

		mVideoOverlay = new VideoOverlay();
		mMessageBuilder = new StringBuilder();
		mRect = new Rect();
	}
	public int construct( Variant[] param, Dispatch2 tjs_obj ) throws TJSException {
		int hr = super.construct( param, tjs_obj );
		if( hr < 0 ) return hr;

		if( param.length < 1 ) return Error.E_BADPARAMCOUNT;

		VariantClosure clo = param[0].asObjectClosure();
		if( clo.mObject == null ) Message.throwExceptionMessage( Message.SpecifyWindow );
		WindowNI win = (WindowNI) clo.mObject.getNativeInstance(WindowClass.ClassID);
		if( win == null ) Message.throwExceptionMessage( Message.SpecifyWindow );
		mWindow = win;
		mWindow.registerVideoOverlayObject(this);
		mActionOwner.mObject = clo.mObject;
		mActionOwner.mObjThis= clo.mObjThis;
		mOwner = new WeakReference<Dispatch2>(tjs_obj);
		return Error.S_OK;
	}
	public void invalidate() throws TJSException {
		mOwner.clear();
		if( mWindow != null ) mWindow.unregisterVideoOverlayObject(this);
		mActionOwner.mObject = mActionOwner.mObjThis = null;
		super.invalidate();
		close();
	}

	private String getStatusString() {
		switch( mStatus ) {
		case ssUnload:	return "unload";
		case ssPlay:	return "play";
		case ssStop:	return "stop";
		case ssPause:	return "pause";
		default:		return "unknown";
		}
	}
	private void setStatus( int s ) {
		// this function may call the onStatusChanged event immediately
		if( mStatus != s ) {
			mStatus = s;
			Dispatch2 owner = mOwner.get();
			if( owner != null ) {
				// Cancel Previous un-delivered Events
				TVP.EventManager.cancelSourceEvents(owner);
				// fire
				if( mCanDeliverEvents ) {
					// fire onStatusChanged event
					Variant[] param = {new Variant(getStatusString())};
					TVP.EventManager.postEvent( owner, owner, ON_STATUS_CHANGED, 0, EventManager.EPT_IMMEDIATE, param );
				}
			}
		}
	}
	private void setStatusAsync( int s ) {
		// this function posts the onStatusChanged event
		if( mStatus != s ) {
			mStatus = s;
			Dispatch2 owner = mOwner.get();
			if( owner != null ) {
				// Cancel Previous un-delivered Events
				TVP.EventManager.cancelSourceEvents(owner);
				// fire
				if( mCanDeliverEvents ) {
					// fire onStatusChanged event
					Variant[] param = {new Variant(getStatusString())};
					TVP.EventManager.postEvent( owner, owner, ON_STATUS_CHANGED, 0, EventManager.EPT_POST, param);
				}
			}
		}

	}
	private void fireCallbackCommand( final String command, final String argument ) {
		// fire call back command event.
		// this is always synchronized event.
		Dispatch2 owner = mOwner.get();
		if( owner != null ) {
			// fire
			if( mCanDeliverEvents ) {
				// fire onStatusChanged event
				Variant[] param = {new Variant(command), new Variant(argument)};
				TVP.EventManager.postEvent( owner, owner, ON_CALLBACK_COMMAND, 0, EventManager.EPT_IMMEDIATE, param );
			}
		}
	}
	private void firePeriodEvent( int reason ) {
		// fire onPeriod event
		// this is always synchronized event.
		Dispatch2 owner = mOwner.get();
		if( owner != null ) {
			// fire
			if( mCanDeliverEvents ) {
				// fire onPeriod event
				Variant[] param  = {new Variant(reason)};
				TVP.EventManager.postEvent(owner, owner, ON_PERIOD, 0, EventManager.EPT_IMMEDIATE, param );
			}
		}
	}
	private void fireFrameUpdateEvent( int frame ) {
		// fire onFrameUpdate event
		// this is always synchronized event.
		Dispatch2 owner = mOwner.get();
		if( owner != null ) {
			// fire
			if( mCanDeliverEvents ) {
				// fire onPeriod event
				Variant[] param = {new Variant(frame)};
				TVP.EventManager.postEvent( owner, owner, ON_FRAME_UPDATE, 0, EventManager.EPT_IMMEDIATE, param );
			}
		}
	}
	public VariantClosure getActionOwner() {
		return mActionOwner;
	}

	public void open( final String name ) throws TJSException {
		close();
		BinaryStream stream = Storage.createStream(name,BinaryStream.READ);
		mVideoOverlay.open( name, stream, mMode, mVisible );
	}
	public void close() {
		mVideoOverlay.close();
		setStatus( ssUnload );
	}
	public void shutdown() {
		// shutdown the system
		// this functions closes the overlay object, but must not fire any events.
		boolean c = mCanDeliverEvents;
		//ClearWndProcMessages();
		setStatus(ssUnload);
		try {
			mVideoOverlay.close();
		} finally {
			mCanDeliverEvents = c;
		}
	}
	public void disconnect() {
		// disconnect the object
		shutdown();
		mWindow = null;
	}
	public void play() {
		mVideoOverlay.setLoop(mLoop);
		mVideoOverlay.play();
		//clearWndProcMessages();
		setStatus(ssPlay);
	}
	public void stop() {
		mVideoOverlay.stop();
		//clearWndProcMessages();
		setStatus(ssStop);
	}
	public void pause() {
		mVideoOverlay.pause();
		setStatus(ssPause);
	}
	public void rewind() {
		mVideoOverlay.rewind();
		if( mEventFrame >= 0 && mIsEventPast )
			mIsEventPast = false;
	}
	public void prepare() {
		//pause();
		//rewind();
		mIsPrepare = true;
		//play();
	}

	public void setSegmentLoop( int comeFrame, int goFrame ) {
		mSegLoopStartFrame = comeFrame;
		mSegLoopEndFrame = goFrame;
	}
	public void cancelSegmentLoop() {
		mSegLoopStartFrame = -1;
		mSegLoopEndFrame = -1;
	}
	public void setPeriodEvent( int eventFrame ) {
		mEventFrame = eventFrame;
		if( eventFrame <= getFrame() )
			mIsEventPast = true;
		else
			mIsEventPast = false;
	}
	public void setStopFrame( int f ) {
		mVideoOverlay.setStopFrame( f );
	}
	public void setDefaultStopFrame() {
		mVideoOverlay.setDefaultStopFrame();
	}
	public int getStopFrame() {
		return mVideoOverlay.getStopFrame();
	}

	public void setRectangleToVideoOverlay(){
		Rect rect = new Rect( mRect );
		mMessageBuilder.delete(0, mMessageBuilder.length());
		mMessageBuilder.append( "Video zoom: (" );
		mMessageBuilder.append( rect.left );
		mMessageBuilder.append( "," );
		mMessageBuilder.append( rect.top );
		mMessageBuilder.append( ")-(" );
		mMessageBuilder.append( rect.right);
		mMessageBuilder.append( "," );
		mMessageBuilder.append( rect.bottom );
		mMessageBuilder.append( ") ->" );
		DebugClass.addLog( mMessageBuilder.toString() );

		mWindow.zoomRectangle( rect );

		mMessageBuilder.delete(0, mMessageBuilder.length());
		mMessageBuilder.append( "(" );
		mMessageBuilder.append( rect.left );
		mMessageBuilder.append( "," );
		mMessageBuilder.append( rect.top );
		mMessageBuilder.append( ")-(" );
		mMessageBuilder.append( rect.right);
		mMessageBuilder.append( "," );
		mMessageBuilder.append( rect.bottom );
		mMessageBuilder.append( ")" );
		DebugClass.addLog( mMessageBuilder.toString() );

		mVideoOverlay.setRect(rect);
	}

	public void setPosition( int left, int top) {
		mRect.setOffsets(left, top);
		setRectangleToVideoOverlay();
	}
	public void setSize( int width, int height) {
		mRect.setSize(width, height);
		setRectangleToVideoOverlay();
	}
	public void setBounds( Rect rect ) {
		mRect.set(rect);
		setRectangleToVideoOverlay();
	}

	public void setLeft(int l) {
		mRect.setOffsets(l, mRect.top);
		setRectangleToVideoOverlay();
	}
	public int getLeft() { return mRect.left; }
	public void setTop(int t) {
		mRect.setOffsets(mRect.left, t);
		setRectangleToVideoOverlay();
	}
	public int getTop() { return mRect.top; }
	public void setWidth(int w) {
		mRect.right = mRect.left + w;
		setRectangleToVideoOverlay();
	}
	public int getWidth() { return mRect.width(); }
	public void setHeight(int h) {
		mRect.bottom = mRect.top + h;
		setRectangleToVideoOverlay();
	}
	public int getHeight() { return mRect.height(); }

	public void setVisible(boolean b) {
		mVisible = b;
		mVideoOverlay.setVisible(mVisible);
	}
	public boolean getVisible() { return mVisible; }

	public void setTimePosition( long p ) {
		mVideoOverlay.setPosition( p );
	}
	public long getTimePosition() {
		return mVideoOverlay.getPosition();
	}

	public void setFrame( int f ) {
		mVideoOverlay.setFrame( f );
		if( mEventFrame >= f && mIsEventPast )
			mIsEventPast = false;
	}
	public int getFrame() {
		return mVideoOverlay.getFrame();
	}

	public double getFPS() {
		return mVideoOverlay.getFPS();
	}
	public int getNumberOfFrame() {
		return mVideoOverlay.getNumberOfFrame();
	}
	public long getTotalTime() {
		return mVideoOverlay.getTotalTime();
	}

	public void setLoop( boolean b ) {
		mLoop = b;
		mVideoOverlay.setLoop(mLoop);
	}
	public boolean getLoop() { return mLoop; }

	public void setLayer1( LayerNI l ) {
		// mLayer1 = l;
	}
	public LayerNI getLayer1() { return mLayer1; }
	public void setLayer2( LayerNI l ) {
		// mLayer2 = l;
	}
	public LayerNI getLayer2() { return mLayer2; }

	public void setMode( int m ) {
		if( mVideoOverlay.isOpen() == false ) {
			mMode = m;
		}
	}
	public int getMode() { return mMode; }

	public double getPlayRate() {
		return mVideoOverlay.getPlayRate();
	}
	public void setPlayRate(double r) {
		mVideoOverlay.setPlayRate( r );
	}

	public int getSegmentLoopStartFrame() { return mSegLoopStartFrame; }
	public int getSegmentLoopEndFrame() { return mSegLoopEndFrame; }
	public int getPeriodEventFrame() { return mEventFrame; }

	public int getAudioBalance() {
		return mVideoOverlay.getAudioBalance();
	}
	public void setAudioBalance(int b) {
		mVideoOverlay.setAudioBalance( b );
	}
	public int getAudioVolume() {
		return mVideoOverlay.getAudioVolume();
	}
	public void setAudioVolume(int v) {
		mVideoOverlay.setAudioVolume( v );
	}

	public int getNumberOfAudioStream() {
		return mVideoOverlay.getNumberOfAudioStream();
	}
	public void selectAudioStream(int n) {
		mVideoOverlay.selectAudioStream( n );
	}
	public int getEnabledAudioStream() {
		return mVideoOverlay.getEnableAudioStreamNum();
	}
	public void disableAudioStream() {
		mVideoOverlay.disableAudioStream();
	}

	public int getNumberOfVideoStream() {
		return mVideoOverlay.getNumberOfVideoStream();
	}
	public void selectVideoStream(int n) {
		mVideoOverlay.selectVideoStream( n );
	}
	public int getEnabledVideoStream() {
		return mVideoOverlay.getEnableVideoStreamNum();
	}
	public void setMixingLayer( LayerNI l ) {
		// 何もしない
	}
	public void resetMixingBitmap() {
		// 何もしない
	}

	public void setMixingMovieAlpha( double a ) {
		mVideoOverlay.setMixingMovieAlpha( a );
	}
	public double getMixingMovieAlpha() {
		return mVideoOverlay.getMixingMovieAlpha();
	}
	public void setMixingMovieBGColor( int col ) {
		mVideoOverlay.setMixingMovieBGColor( col );
	}
	public int getMixingMovieBGColor() {
		return mVideoOverlay.getMixingMovieBGColor();
	}

	public int getOriginalWidth() {
		return mVideoOverlay.getVideoWidth();
	}
	public int getOriginalHeight() {
		return mVideoOverlay.getVideoHeight();
	}

	public void resetOverlayParams() {
		// set Rectangle
		setRectangleToVideoOverlay();

		// set Visible
		mVideoOverlay.setVisible(mVisible);
	}
	public void setRectOffset(int ofsx, int ofsy) {
		Rect r = new Rect( mRect.left + ofsx, mRect.top + ofsy, mRect.right + ofsx, mRect.bottom + ofsy );
		mVideoOverlay.setRect(r);
	}
	public void detachVideoOverlay() {}

	public void setTargetWindow(WindowForm form ) {
		mVideoOverlay.setWindow( form );
	}
}
