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

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.net.URL;

import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tvp2.TVP;
import jp.kirikiri.tvp2.utils.DebugClass;

/**
 * オリジナルとは異なり、Window等の実態を持たないクラス
 *
 */
public class MainForm {

	private long mLastCompactedTick;
	private long mLastContinuousTick;
	private long mLastCloseClickedTick;
	private long mLastShowModalWindowSentTick;
	private long mLastRehashedTick;
	private long mMixedIdleTick;
	private boolean mContinuousEventCalling;
	private boolean mAutoShowConsoleOnError;
	private boolean mApplicationStayOnTop;
	private boolean mApplicationActivating;
	private boolean mApplicationNotMinimizing;
	private String mClassPath;
	private String mExeName;

	public MainForm() {
		mContinuousEventCalling = false;
		mAutoShowConsoleOnError = false;
		mApplicationStayOnTop = false;
		mApplicationActivating = true;
		mApplicationNotMinimizing = true;

		long tick = TVP.getTickCount();
		mLastCompactedTick = tick;
		mLastContinuousTick = tick;
		mLastCloseClickedTick = tick;
		mLastShowModalWindowSentTick = tick;
		mLastRehashedTick = tick;
		mMixedIdleTick = tick;


	}
	public void invokeEvents() {
		callDeliverAllEventsOnIdle();
	}

	public void notifyEventDelivered() {
		// called from event system, notifying the event is delivered.
		mLastCloseClickedTick = 0;
		//if(TVPHaltWarnForm) delete TVPHaltWarnForm, TVPHaltWarnForm = NULL;
	}

	public void callDeliverAllEventsOnIdle() {
		//::PostMessage(TVPMainForm->Handle, WM_USER+0x31/*dummy msg*/, 0, 0);
		// indirectly called by TVPInvokeEvents  *** currently not used ***
		//if(EventButton->Down)
		/*
		{		// TODO 仮で呼んでおく
			TVP.EventManager.deliverAllEvents();
		}
		*/
	}

	/**
	 * クラスパスとjarのパスを取得する
	 * TODO Androidだとどうするか……
	 */
	private void retrieveClassPath() {
		try {
			Class<? extends MainForm> c = this.getClass();
			URL u = c.getResource(c.getSimpleName() + ".class");
			mClassPath = u.toString();
			int delimiPos = mClassPath.indexOf('!');
			String name;
			if( delimiPos >= 0 ) {
				name = mClassPath.substring(0,delimiPos);
			} else {
				name = mClassPath;
			}
			if( name.indexOf("jar:file:/") >= 0 ) {
				name = name.substring("jar:file:/".length());
			}
			mExeName = TVP.StorageMediaManager.normalizeStorageName(name, null);
		} catch( NullPointerException e ) {
			mClassPath = System.getProperty("user.dir");
			mExeName = mClassPath;
		} catch (TJSException e) {
			mClassPath = System.getProperty("user.dir");
			mExeName = mClassPath;
		}
	}
	public String getClassPath() {
		if( mClassPath == null ) {
			retrieveClassPath();
		}
		return mClassPath;
	}
	public String getExeName() {
		if( mExeName == null ) {
			retrieveClassPath();
		}
		return mExeName;
	}

	public void beginContinuousEvent() {
		if(!mContinuousEventCalling) {
			mContinuousEventCalling = true;
			invokeEvents();
		}
	}

	public void endContinuousEvent() {
		if(mContinuousEventCalling) {
			mContinuousEventCalling = false;
		}
	}
	public boolean applicationIdle() {
		deliverEvents();
		boolean cont = mContinuousEventCalling;
		mMixedIdleTick += TVP.getTickCount();
		return cont;
	}
	private void deliverEvents() {
		if( mContinuousEventCalling )
			TVP.EventManager.setProcessContinuousHandlerEventFlag(true); // set flag

		//if(EventButton->Down) TVPDeliverAllEvents();
		TVP.EventManager.deliverAllEvents();
	}

	public int getScreenWidth() {
		GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice device = graphicsEnvironment.getDefaultScreenDevice();
		return device.getDisplayMode().getWidth();
	}
	public int getScreenHeight() {
		GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice device = graphicsEnvironment.getDefaultScreenDevice();
		return device.getDisplayMode().getHeight();
	}
	public int getDesktopHeight() {
		GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		Rectangle rect = graphicsEnvironment.getMaximumWindowBounds();
		return rect.height;
	}
	public int getDesktopLeft() {
		GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		Rectangle rect = graphicsEnvironment.getMaximumWindowBounds();
		return rect.x;
	}
	public int getDesktopTop() {
		GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		Rectangle rect = graphicsEnvironment.getMaximumWindowBounds();
		return rect.y;
	}
	public int getDesktopWidth() {
		GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		Rectangle rect = graphicsEnvironment.getMaximumWindowBounds();
		return rect.width;
	}
}
