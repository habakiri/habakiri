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
