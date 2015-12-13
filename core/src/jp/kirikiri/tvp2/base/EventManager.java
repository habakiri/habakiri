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

import java.util.ArrayList;

import jp.kirikiri.tjs2.Interface;
import jp.kirikiri.tjs2.Dispatch2;
import jp.kirikiri.tjs2.TJS;
import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tjs2.TJSScriptError;
import jp.kirikiri.tjs2.TJSScriptException;
import jp.kirikiri.tjs2.Variant;
import jp.kirikiri.tjs2.VariantClosure;
import jp.kirikiri.tjs2.VariantException;
import jp.kirikiri.tvp2.TVP;
import jp.kirikiri.tvp2.msg.Message;
import jp.kirikiri.tvp2.visual.WindowNI;

public class EventManager {

	public static final int EPT_POST			= 0x00;  // normal post, simply add to queue
	public static final int EPT_REMOVE_POST		= 0x01;
			// remove event in pending queue that has same target, source, tag and
			// name before post
			// (for input events, only the source and the tag are to be checked)
	public static final int EPT_IMMEDIATE		= 0x02;
			// the event will be delivered immediately

	public static final int EPT_DISCARDABLE		= 0x10;
			// the event can be discarded when event system is disabled

	public static final int EPT_NORMAL			= 0x00;
			// (with TVP_EPT_POST only)
			// the event will have normal priority.

	public static final int EPT_EXCLUSIVE		= 0x20;
			// (with TVP_EPT_POST only)
			// the event is given priority and other posted events are not processed
			// until the exclusive event is processed.

	public static final int EPT_IDLE			= 0x40;
			// (with TVP_EPT_POST only)
			// the event is only delivered after the system processes all other events.
			// this will have a priority roughly identical to "continuous" events.

	public static final int EPT_PRIO_MASK		= 0xe0;

	public static final int EPT_METHOD_MASK		= 0x0f;

	// event queue must be a globally sequential queue
	private ArrayList<BaseInputEvent> mInputEventQueue;
	private ArrayList<Event> mEventQueue;
	private ArrayList<WinUpdateEvent> mWinUpdateEventQueue;
	private boolean mExclusiveEventPosted; // true if exclusive event is posted
	private long mEventSequenceNumber; // event sequence number
	private long mEventSequenceNumberToProcess; // current event sequence which must be processed
	private boolean mEventDisabled;
	private boolean mEventInterrupting;
	private boolean mWindowUpdateEventsDelivering;
	private int mInputEventTagMax;

	private boolean mProcessContinuousHandlerEventFlag;
	private ArrayList<ContinuousEventCallbackInterface> mContinuousEventVector;
	private ArrayList<VariantClosure> mContinuousHandlerVector;
	private boolean mContinuousEventProcessing;

	private ArrayList<CompactEventCallbackInterface> mCompactEventVector;

	private boolean mEventInvoked;

	private boolean mBreathing;


	private int mContinousHandlerLimitFrequency;

	public EventManager() {
		//mExclusiveEventPosted = false; // true if exclusive event is posted
		//mEventSequenceNumber = 0; // event sequence number
		//mEventSequenceNumberToProcess = 0;
		//mEventDisabled = false;
		//mEventInterrupting = false;
		//mWindowUpdateEventsDelivering = false;
		//mInputEventTagMax = 0;

		mInputEventQueue = new ArrayList<BaseInputEvent>();
		mEventQueue = new ArrayList<Event>();
		mWinUpdateEventQueue = new ArrayList<WinUpdateEvent>();

		//mProcessContinuousHandlerEventFlag = false;
		//mContinuousEventProcessing = false;
		mContinuousEventVector = new ArrayList<ContinuousEventCallbackInterface>();
		mContinuousHandlerVector = new ArrayList<VariantClosure>();

		mCompactEventVector = new ArrayList<CompactEventCallbackInterface>();

		//mEventInvoked = false;

		//mBreathing = false;

		//mContinousHandlerLimitFrequency = 0;
	}
	public long getSequenceNumber() { return mEventSequenceNumber; }
	public long getSequenceNumberToProcess() { return mEventSequenceNumberToProcess; }

	/**
	 * posts TVP event. this function itself is thread-safe.
	 * @param source
	 * @param target
	 * @param eventname
	 * @param tag
	 * @param flag
	 * @param args
	 */
	public void postEvent( Dispatch2 source, Dispatch2 target, String eventname, int tag, int flag, Variant[] args ) {

		boolean evdisabled = mEventDisabled || TVP.getSystemEventDisabledState();
		if( (flag & EPT_DISCARDABLE) != 0 && (mEventInterrupting || evdisabled)) return;

		int method = flag & EPT_METHOD_MASK;

		if( method == EPT_IMMEDIATE ) {
			// the event is delivered immediately
			if( evdisabled ) return;

			try {
				try {
					Event.immediate(target, source, eventname, tag, args, flag);
				} catch( TJSException e ) {
					throw e;
				} catch( Exception e ) {
					throw new TJSException(e.getMessage());
				}
			} catch( TJSException e ) {
				if( !ScriptsClass.processUnhandledException(e) )
					ScriptsClass.showScriptException(e);
			}
			return;
		}


		if( method == EPT_REMOVE_POST ) {
			// events in queue that have same target/source/name/tag are to be removed
			final int count = mEventQueue.size();
			for( int i = count-1; i >= 0; i-- ) {
				Event ev = mEventQueue.get(i);
				if( source == ev.getSource() && target == ev.getTarget() &&
					eventname.equals( ev.getEventName() ) && ((tag==0)?true:(tag==ev.getTag())) ) {
					mEventQueue.remove(i);
				}
			}
		}

		// put into queue
		mEventQueue.add( new Event(target, source, eventname, tag, args, flag) );

		// is exclusive?
		if( (flag & EPT_PRIO_MASK) == EPT_EXCLUSIVE) mExclusiveEventPosted = true;

		// make sure that the event is to be delivered.
		invokeEvents();
	}

	/**
	 * removes events that has specified source/target/name/tag.
	 * @param source
	 * @param target
	 * @param eventname
	 * @param tag tag == 0 removes all tag from queue.
	 * @return count of removed events.
	 */
	public int cancelEvents( Dispatch2 source, Dispatch2 target, final String eventname, int tag ) {
		int delCount = 0;
		final int count = mEventQueue.size();
		for( int i = count-1; i >= 0; i-- ) {
			Event ev = mEventQueue.get(i);
			if( source == ev.getSource() && target == ev.getTarget() &&
				eventname.equals( ev.getEventName() ) && ((tag==0)?true:(tag==ev.getTag())) ) {
				mEventQueue.remove(i);
				delCount++;
			}
		}
		return delCount;
	}
	public int cancelEvents( Dispatch2 source, Dispatch2 target, final String eventname ) {
		return cancelEvents(source,target,eventname,0);
	}

	/**
	 * whether the events are in queue that have specified
	 * source/target/name/tag.
	 * @param source
	 * @param target
	 * @param eventname
	 * @param tag tag == 0 matches all tag in queue.
	 * @return
	 */
	public boolean areEventsInQueue( Dispatch2 source, Dispatch2 target, final String eventname, int tag ) {
		final int count = mEventQueue.size();
		for( int i = count-1; i >= 0; i-- ) {
			Event ev = mEventQueue.get(i);
			if( source == ev.getSource() && target == ev.getTarget() &&
				eventname.equals( ev.getEventName() ) && ((tag==0)?true:(tag==ev.getTag())) ) {
				return true;
			}
		}
		return false;
	}

	/**
	 * returns count of the events in queue that have specified
	 * source/target/name/tag.
	 * tag == 0 matches all tag in queue.
	 * @param source
	 * @param target
	 * @param eventname
	 * @param tag
	 * @return
	 */
	public int countEventsInQueue( Dispatch2 source, Dispatch2 target, final String eventname, int tag ) {
		int evCount = 0;
		final int count = mEventQueue.size();
		for( int i = count-1; i >= 0; i-- ) {
			Event ev = mEventQueue.get(i);
			if( source == ev.getSource() && target == ev.getTarget() &&
				eventname.equals( ev.getEventName() ) && ((tag==0)?true:(tag==ev.getTag())) ) {
				evCount++;
			}
		}
		return evCount;
	}

	/**
	 * removes all events which have the same source/target/tag.
	 * tag == 0 removes all tag from queue.
	 * @param source
	 * @param target
	 * @param tag
	 */
	public void cancelEventsByTag( Dispatch2 source, Dispatch2 target, int tag ) {
		final int count = mEventQueue.size();
		for( int i = count-1; i >= 0; i-- ) {
			Event ev = mEventQueue.get(i);
			if( source == ev.getSource() && target == ev.getTarget() &&
				 ((tag==0)?true:(tag==ev.getTag())) ) {
				mEventQueue.remove(i);
			}
		}
	}
	public void cancelEventsByTag( Dispatch2 source, Dispatch2 target ) {
		cancelEventsByTag( source, target, 0 );
	}

	public void cancelSourceEvents( Dispatch2 source ) {
		final int count = mEventQueue.size();
		for( int i = count-1; i >= 0; i-- ) {
			Event ev = mEventQueue.get(i);
			if( source == ev.getSource() ) {
				mEventQueue.remove(i);
			}
		}
	}
	public void discardAllDiscardableEvents() {
		final int count = mEventQueue.size();
		for( int i = count-1; i >= 0; i-- ) {
			Event ev = mEventQueue.get(i);
			if( (ev.getFlags() & EPT_DISCARDABLE) != 0 ) {
				mEventQueue.remove(i);
			}
		}
	}
	private void deliverEventByPrio( int prio ) throws VariantException, TJSException {
		while( true ) {
			Event ev = null;

			// retrieve item to deliver
			final int count = mEventQueue.size();
			if( count == 0 ) break;
			int i;
			for( i = 0; i < count; i++ ) {
				ev = mEventQueue.get(i);
				if( ev.getSequence() <= mEventSequenceNumberToProcess &&
					((ev.getFlags() & EPT_PRIO_MASK) == prio) ) break;
			}
			if( i == count ) break;
			mEventQueue.remove(i);

			// event delivering
			if( ev != null ) {
				ev.deliver();
				ev = null;
			}
		}
	}
	private boolean deliverAllEvents2() throws VariantException, TJSException {
		mExclusiveEventPosted = false;

		// process exclusive events
		deliverEventByPrio(EPT_EXCLUSIVE);

		// check exclusive events
		if(mExclusiveEventPosted) return true;

		// process input event queue
		while( true ) {
			// retrieve item to deliver
			final int count = mInputEventQueue.size();
			if( count == 0) break;
			BaseInputEvent ev = mInputEventQueue.get(0);
			mInputEventQueue.remove(0);

			// event delivering
			ev.deliver();
			ev = null;

			// check exclusive events
			if(mExclusiveEventPosted) return true;
		}

		// process normal event queue
		deliverEventByPrio(EPT_NORMAL);

		// check exclusive events
		if(mExclusiveEventPosted) return true;

		return true;
	}
	//---------------------------------------------------------------------------
	private boolean deliverAllEventsInternal() throws VariantException, TJSException {
		// deliver all pending events to targets.
		if( mEventDisabled ) return true;

		// event invokation was received...
		eventReceived();

		// for script event objects
		boolean ret_value = deliverAllEvents2();
		return ret_value;
	}

	public void deliverAllEvents() {
		boolean r = true;

		if( !mEventInterrupting ) {
			mEventSequenceNumberToProcess = mEventSequenceNumber;
			mEventSequenceNumber++; // increment sequence number
		}
		mEventInterrupting = false;

	   try {
			r = deliverAllEventsInternal();
		} catch( TJSException e ) {
			if(!ScriptsClass.processUnhandledException(e))
				ScriptsClass.showScriptException(e);
		}
		//TVP_CATCH_AND_SHOW_SCRIPT_EXCEPTION(TJS_W("event"));

		if( !r ) {
			// event processing is to be interrupted
			// XXX: currently this is not functional
			mEventInterrupting = true;
			callDeliverAllEventsOnIdle();
		}

		if(!mExclusiveEventPosted && !mEventInterrupting) {
			try {
				// process idle event queue
				deliverEventByPrio(EPT_IDLE);
			} catch( TJSException e ) {
				if(!ScriptsClass.processUnhandledException(e))
					ScriptsClass.showScriptException(e);
			}
			//TVP_CATCH_AND_SHOW_SCRIPT_EXCEPTION(TJS_W("idle event"));

			// process continuous events
			if( mProcessContinuousHandlerEventFlag ) {
				mProcessContinuousHandlerEventFlag = false; // processed
				// XXX: strictly saying, we need something like InterlockedExchange
				// to look/set this flag, because TVPProcessContinuousHandlerEventFlag
				// may be accessed by another thread. But I have no dought about
				// that no one does care of missing one event in rare race condition.

				deliverContinuousEvent();
			}

			try {
				// for window content updating
				deliverWindowUpdateEvents();
			} catch( TJSException e ) {
				if(!ScriptsClass.processUnhandledException(e))
					ScriptsClass.showScriptException(e);
			}
			//TVP_CATCH_AND_SHOW_SCRIPT_EXCEPTION(TJS_W("window update"));
		}

		if( mEventQueue.size() == 0 ) {
			mEventSequenceNumber = 0; // reset the number
		}
	}
	public void postWindowUpdate( WindowNI window ) {
		if( !mWindowUpdateEventsDelivering ) {
			if( mWinUpdateEventQueue.size() > 0 ) {
				// since duplication is not allowed ...
				final int count = mWinUpdateEventQueue.size();
				for( int i = 0; i < count; i++ ) {
					WinUpdateEvent ev = mWinUpdateEventQueue.get(i);
					if(!ev.isEmpty() && window == ev.getWindow()) return;
				}
			}
		} else {
			if( mWinUpdateEventQueue.size() > 0 ) {
				// duplication is allowed up to two
				int hitCount = 0;
				final int count = mWinUpdateEventQueue.size();
				for( int i = 0; i < count; i++ ) {
					WinUpdateEvent ev = mWinUpdateEventQueue.get(i);
					if(!ev.isEmpty() && window == ev.getWindow() ) {
						hitCount++;
						if(hitCount == 2) return;
					}
				}
			}
		}

		// put into queue.
		mWinUpdateEventQueue.add( new WinUpdateEvent(window) );

		// make sure that the event is to be delivered.
		invokeEvents();
	}
	public void removeWindowUpdate( WindowNI window ) {
		// removes all window update events from queue.
		if( mWinUpdateEventQueue.size() > 0 ) {
			final int count = mWinUpdateEventQueue.size();
			for( int i = 0; i < count; i++ ) {
				WinUpdateEvent ev = mWinUpdateEventQueue.get(i);
				if(!ev.isEmpty() && window == ev.getWindow() )
					ev.markEmpty();
			}
		}
	}
	public void deliverWindowUpdateEvents() throws VariantException, TJSException {
		if( mWindowUpdateEventsDelivering ) return; // does not allow re-entering
		mWindowUpdateEventsDelivering = true;

		try {
			final int count = mWinUpdateEventQueue.size();
			for( int i = 0; i < count; i++) {
				WinUpdateEvent ev = mWinUpdateEventQueue.get(i);
				if( !ev.isEmpty() )
					ev.deliver();
			}
		} finally {
			mWinUpdateEventQueue.clear();
			mWindowUpdateEventsDelivering = false;
		}
	}
	public void postInputEvent( BaseInputEvent ev, int flags ) {
		// flag check
		if( (flags & EPT_DISCARDABLE) != 0 &&
			(mEventDisabled || TVP.getSystemEventDisabledState()) ) {
			ev = null;
			return;
		}

		if( (flags & EPT_REMOVE_POST) != 0 ) {
			// cancel previously posted events
			cancelInputEvents( ev.getSource(), ev.getTag() );
		}

		// push into the event queue
		mInputEventQueue.add(ev);

		// make sure that the event is to be delivered.
		invokeEvents();
	}
	public void cancelInputEvents( Object source ) {
		// removes all evens which have the same source
		final int count = mInputEventQueue.size();
		for( int i = count-1; i >= 0; i-- ) {
			BaseInputEvent ev = mInputEventQueue.get(i);
			if( source == ev.getSource() ) {
				mInputEventQueue.remove(i);
			}
		}
	}
	void cancelInputEvents( Object source, int tag ) {
		// removes all evens which have the same source
		final int count = mInputEventQueue.size();
		for( int i = count-1; i >= 0; i-- ) {
			BaseInputEvent ev = mInputEventQueue.get(i);
			if( source == ev.getSource() && tag == ev.getTag() ) {
				mInputEventQueue.remove(i);
			}
		}
	}
	public int getInputEventCount() { return mInputEventQueue.size(); }

	final private static String type_name = "type";
	final private static String target_name = "target";
	public final static String ActionName = "action";
	public static Dispatch2 createEventObject( final String type, Dispatch2 targthis, Dispatch2 targ ) throws VariantException, TJSException {
		// create a dictionary object for event dispatching ( to "action" method )
		Dispatch2 object = TJS.createDictionaryObject();

		Variant val = new Variant(type);
		int hr = object.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, type_name, val, object );
		if( hr < 0 ) Message.throwExceptionMessage( Message.InternalError );

		val.set(targthis, targ);
		hr = object.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, target_name, val, object );
		if( hr < 0 ) Message.throwExceptionMessage( Message.InternalError );

		return object;
	}

	public void destroyContinuousHandlerVector() {
		final int count = mContinuousHandlerVector.size();
		for( int i = 0; i < count; i++ ) {
			VariantClosure clo = mContinuousHandlerVector.get(i);
			clo.set(null,null);
		}
		mContinuousHandlerVector.clear();
	}
	public void addContinuousEventHook( ContinuousEventCallbackInterface cb ) {
		beginContinuousEvent();
		mContinuousEventVector.add(cb);
	}
	public void removeContinuousEventHook( ContinuousEventCallbackInterface cb ) {
		final int count = mContinuousEventVector.size();
		for( int i = 0; i < count; i++ ) {
			ContinuousEventCallbackInterface c = mContinuousEventVector.get(i);
			if( c == cb ) {
				mContinuousEventVector.set(i, null);
			}
		}
	}
	private void deliverContinuousEventInternal() throws TJSException {
		long tick = TVP.getTickCount();

		int count = mContinuousEventVector.size();
		if( count > 0 ) {
			boolean emptyflag = false;
			for( int i = 0; i < count; i++) {
				// note that the handler can remove itself while the event
				if( mContinuousEventVector.get(i) != null )
					mContinuousEventVector.get(i).onContinuousCallback(tick);
				else
					emptyflag = true;

				if( mExclusiveEventPosted ) return;  // check exclusive events
			}

			if( emptyflag ) {
				// the array has empty cell
				// eliminate empty
				count = mContinuousEventVector.size();
				for( int i = count-1; i >= 0; i-- ) {
					if( mContinuousEventVector.get(i) == null ) {
						mContinuousEventVector.remove(i);
					}
				}
			}
		}

		if( !mEventDisabled && mContinuousHandlerVector.size() > 0 ) {
			boolean emptyflag = false;
			Variant vtick = new Variant(tick);
			Variant[] pvtick = new Variant[1];
			pvtick[0] = vtick;
			count = mContinuousHandlerVector.size();
			for( int i = 0; i < count; i++ ) {
				VariantClosure clo = mContinuousHandlerVector.get(i);
				if( clo != null && clo.mObject != null ) {
					int er;
					try {
						er = clo.funcCall( 0, null, null, pvtick, null );
					} catch (VariantException e) {
						clo.mObject = null;
						clo.mObjThis = null;
						throw e;
					} catch (TJSException e) {
						clo.mObject = null;
						clo.mObjThis = null;
						throw e;
					}

					if( er < 0 ) {
						// failed
						clo.mObject = null;
						clo.mObjThis = null;
						emptyflag = true;
					}
					if(mExclusiveEventPosted) return;  // check exclusive events
				} else {
					emptyflag = true;
				}

			}

			if( emptyflag ) {
				// the array has empty cell
				// eliminate empty
				count = mContinuousHandlerVector.size();
				for( int i = count-1; i >= 0; i-- ) {
					VariantClosure clo = mContinuousHandlerVector.get(i);
					if( clo == null || clo.mObject == null) {
						mContinuousHandlerVector.remove(i);
					}
				}
			}
		}

		if( mContinuousEventVector.size() == 0 && mContinuousHandlerVector.size() == 0 )
			endContinuousEvent();
	}

	public void deliverContinuousEvent() {
		if( mContinuousEventProcessing ) return;
		mContinuousEventProcessing = true;
		try {
			try {
				deliverContinuousEventInternal();
			} finally {
				mContinuousEventProcessing = false;
			}
		} catch( TJSException e ) {
			if(!ScriptsClass.processUnhandledException(e) )
				ScriptsClass.showScriptException(e);
		}
		//TVP_CATCH_AND_SHOW_SCRIPT_EXCEPTION(TJS_W("continuous event"));
		mContinuousEventProcessing = false;
	}
	public void addContinuousHandler( VariantClosure clo ) {
		boolean found = false;
		final int count = mContinuousHandlerVector.size();
		for( int i = 0; i < count; i++ ) {
			VariantClosure c = mContinuousHandlerVector.get(i);
			if( c != null && c.mObject == clo.mObject && c.mObjThis == clo.mObjThis ) {
				found = true;
				break;
			}
		}
		if( found == false ) {
			beginContinuousEvent();
			mContinuousHandlerVector.add(clo);
		}
	}
	public void removeContinuousHandler( VariantClosure clo ) {
		final int count = mContinuousHandlerVector.size();
		for( int i = 0; i < count; i++ ) {
			VariantClosure c = mContinuousHandlerVector.get(i);
			if( c != null && c.mObject == clo.mObject && c.mObjThis == clo.mObjThis ) {
				c.set(null,null);
				mContinuousHandlerVector.set(i,null);
				break;
			}
		}
	}
	public void addCompactEventHook( CompactEventCallbackInterface cb ) {
		mCompactEventVector.add(cb);
	}
	public void removeCompactEventHook( CompactEventCallbackInterface cb ) {
		final int count = mCompactEventVector.size();
		for( int i = 0; i < count; i++ ) {
			CompactEventCallbackInterface c = mCompactEventVector.get(i);
			if( c == cb ) mCompactEventVector.set(i,null);
		}
	}
	public void deliverCompactEvent( int level ) {
		// must be called by each platforms's implementation
		final int count = mCompactEventVector.size();
		if( count > 0 ) {
			boolean emptyflag = false;
			for( int i = 0; i < count; i++ ) {
				// note that the handler can remove itself while the event
				try {
					try {
						if( mCompactEventVector.get(i) != null )
							mCompactEventVector.get(i).onCompact(level);
						else
							emptyflag = true;
					} catch( TJSScriptException e ) {
						throw e;
					} catch( TJSScriptError e ) {
						throw e;
					} catch( TJSException e ) {
						throw e;
					} catch( Exception e ) {
						throw new TJSException( e.getMessage() );
					}
				} catch( TJSScriptException e ) {
					e.addTrace("Compact Event");
					if(!ScriptsClass.processUnhandledException(e))
						ScriptsClass.showScriptException(e);
				} catch( TJSScriptError e ) {
					e.addTrace("Compact Event");
					if(!ScriptsClass.processUnhandledException(e))
						ScriptsClass.showScriptException(e);
				} catch( TJSException e ) {
					if(!ScriptsClass.processUnhandledException(e))
						ScriptsClass.showScriptException(e);
				}
				//TVP_CATCH_AND_SHOW_SCRIPT_EXCEPTION_FORCE_SHOW_EXCEPTION(TJS_W("Compact Event"));
			}

			if( emptyflag ) {
				// the array has empty cell
				// eliminate empty
				for( int i = count-1; i >= 0; i-- ) {
					if( mCompactEventVector.get(i) == null ) {
						mCompactEventVector.remove(i);
					}
				}
			}
		}
	}

	private void invokeEvents() {
		if( mEventInvoked ) return;
		mEventInvoked = true;
		if( TVP.MainForm != null ) {
			TVP.MainForm.invokeEvents();
		}
	}
	public void eventReceived() {
		mEventInvoked = false;
		TVP.MainForm.notifyEventDelivered();
	}
	public void callDeliverAllEventsOnIdle() {
		if( TVP.MainForm != null ) {
			TVP.MainForm.callDeliverAllEventsOnIdle();
		}
	}
	public void breathe() {
		mEventDisabled = true; // not to call TVP events...
		mBreathing = true;
		try {
			// Application->ProcessMessages(); // do Windows message pumping
		} finally {
			mBreathing = false;
			mEventDisabled = false;
		}
	}
	public boolean getBreathing() {
		// return whether now is in event breathing
		return mBreathing;
	}
	private int ArgumentGeneration = 0;
	void beginContinuousEvent() {
		// read commandline options
		/* TODO
		if( ArgumentGeneration != getCommandLineArgumentGeneration() ) {
			ArgumentGeneration = getCommandLineArgumentGeneration();
			Variant val;
			if( GetCommandLine( "-contfreq", val) ) {
				mContinousHandlerLimitFrequency = val.asInteger();
			}
		}
		*/

		/*if( !getWaitVSync() ) */{
			if( mContinousHandlerLimitFrequency == 0 ) {
				// no limit
				// this notifies continuous calling of TVPDeliverAllEvents.
				if(TVP.MainForm!= null ) TVP.MainForm.beginContinuousEvent();
			} else {
				// has limit
				/*
				if(!TVPContinuousHandlerCallLimitThread)
					TVPContinuousHandlerCallLimitThread = new tTVPContinuousHandlerCallLimitThread();
				TVPContinuousHandlerCallLimitThread->SetInterval( (1<<TVP_SUBMILLI_FRAC_BITS)*1000/TVPContinousHandlerLimitFrequency);
				TVPContinuousHandlerCallLimitThread->SetEnabled(true);
				*/
			}
		}


		//ensureVSyncTimingThread();
		// if we wait vsync, the continuous handler will be executed at the every timing of
		// vsync.
	}
	//---------------------------------------------------------------------------
	void endContinuousEvent() {
		// anyway
		//if(TVPContinuousHandlerCallLimitThread) TVPContinuousHandlerCallLimitThread->SetEnabled(false);

		// anyway
		if(TVP.MainForm != null ) TVP.MainForm.endContinuousEvent();
	}

	public boolean isEventDisabled() { return mEventDisabled; }
	public void setProcessContinuousHandlerEventFlag( boolean b ) {
		mProcessContinuousHandlerEventFlag = b;
	}
}
