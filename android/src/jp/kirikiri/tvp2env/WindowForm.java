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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.content.Context;
import android.graphics.Canvas;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.WindowManager;

import jp.kirikiri.tjs2.Dispatch2;
import jp.kirikiri.tjs2.Error;
import jp.kirikiri.tjs2.Interface;
import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tjs2.Variant;
import jp.kirikiri.tjs2.VariantException;
import jp.kirikiri.tvp2.TVP;
import jp.kirikiri.tvp2.base.EventManager;
import jp.kirikiri.tvp2.base.WindowEvents;
import jp.kirikiri.tvp2.utils.DebugClass;
import jp.kirikiri.tvp2.utils.Random;
import jp.kirikiri.tvp2.visual.Point;
import jp.kirikiri.tvp2.visual.Rect;
import jp.kirikiri.tvp2.visual.ScreenMode;
import jp.kirikiri.tvp2.visual.ScreenModeCandidate;
import jp.kirikiri.tvp2.visual.WindowNI;

public class WindowForm {


	/** 非可視境界 サイズ変更不可 */
	public static final int bsNone=0;
	/** 一重境界線 サイズ変更不可 */
	public static final int bsSingle=1;
	/** 標準のサイズ変更可能境界 */
	public static final int bsSizeable=2;
	/** 標準のダイアログボックス境界 サイズ変更不可 */
	public static final int bsDialog=3;
	/** bsSingle と同じ。ただしキャプションは小さい */
	public static final int bsToolWindow=4;
	/** bsSizeable と同じ。ただしキャプションは小さい  */
	public static final int bsSizeToolWin =5;

	public static final int mbLeft=0;
	public static final int mbRight=1;
	public static final int mbMiddle =2;

	/** the mouse cursor is visible */
	private static final int mcsVisible = 0;
	/** the mouse cursor is temporarily hidden */
	private static final int mcsTempHidden = 1;
	/** the mouse cursor is invisible */
	private static final int mcsHidden = 2;


	private static final int
		IGNOREPROP = 0x00000800, // ignore property invoking
		MEMBERENSURE = 0x00000200;

	private boolean mInMode;
	private boolean mFocusable;

	//-- drawdevice related

	private boolean mNextSetWindowHandleToDrawDevice;
	private Rect mLastSentDrawDeviceDestRect;


	//-- interface to plugin
	//private ArrayList<MessageReceiverRecord> mWindowMessageReceivers;

	//-- DirectInput related
	/*
	WheelDirectInputDevice mDIWheelDevice;
	PadDirectInputDevice mDIPadDevice;
	boolean mReloadDevice;
	int mReloadDeviceTick;
	*/

	//-- TJS object related
	private WindowNI mNativeInstance;
	private int mLastMouseDownX; // in Layer coodinates
	private int mLastMouseDownY;

	//-- full screen managemant related
	//TPaintBox * PaintBox;
	private int mInnerWidthSave;
	private int mInnerHeightSave;
	private int mOrgStyle;
	private int mOrgExStyle;
	//TBorderStyle mOrgScrollBoxBorderStyle;
	private int mOrgLeft;
	private int mOrgTop;
	private int mOrgWidth;
	private int mOrgHeight;
	private boolean mOrgInnerSunken;
	//MenuContainerForm mMenuContainer;
	private int mResetStayOnTopStateTick;

	//-- keyboard input
	//String mPendingKeyCodes;

	//TImeMode mLastSetImeMode;
	private boolean mTrapKeys;
	private boolean mCanReceiveTrappedKeys;
	private boolean mInReceivingTrappedKeys;
	private boolean mInMenuLoop;
	private boolean mIsRepeatMessage;
	private boolean mUseMouseKey; // whether using mouse key emulation
	private int mMouseKeyXAccel;
	private int mMouseKeyYAccel;
	private int mLastMouseKeyTick;
	private boolean mLastMouseMoved;
	//private int mDefaultImeMode;
	private boolean mMouseLeftButtonEmulatedPushed;
	private boolean mMouseRightButtonEmulatedPushed;
	private Point mLastMouseMovedPos;  // in Layer coodinates

	private boolean mAttentionPointEnabled;

	//-- mouse cursor
	private int mMouseCursorState;
	private boolean mForceMouseCursorVisible; // true in menu select
	//private Cursor mCurrentMouseCursor;
	private int mLastMouseScreenX; // managed by RestoreMouseCursor
	private int mLastMouseScreenY;

	//-- layer position / size
	private int mLayerLeft;
	private int mLayerTop;
	private int mLayerWidth;
	private int mLayerHeight;
	private int mZoomDenom; // Zooming factor denominator (setting)
	private int mZoomNumer; // Zooming factor numerator (setting)
	private int mActualZoomDenom; // Zooming factor denominator (actual)
	private int mActualZoomNumer; // Zooming factor numerator (actual)

	//-- menu related
	private boolean mMenuBarVisible;

	private int[] mPosTrans;

	// VCL に備わっているもの？
	private boolean mProgramClosing;
	private int mModalResult;
	private boolean mCanCloseWork;
	private boolean mClosing;
	private int mBorderStyle;

	// 仮定数定義
	private static final int
		mrOk = 1,
		mrCancel = 2,
		mrAbort = 3,
		mrRetry = 4,
		mrIgnore = 5,
		mrYes = 6,
		mrNo = 7,
		mrAll = 8,
		mrNoToAll = 9,
		mrYesToAll = 10;

	private static final int CLIENT_WIDTH = 320;
	private static final int CLIENT_HEIGHT = 240;

	private WeakReference<ImagePanel> mPanel;
	private DrawTarget mDrawTarget;
	private MenuItem mRootMenuItem;

	private long mLastRecheckInputStateSent;
	private Point mAttentionPoint;

	private WeakReference<BaseActivity> mActivity;
	private boolean mIsRunning; // 最初の初期化時は非GUIスレッドなので、これがfalse
	private boolean mVisible;

	public WindowForm( WindowNI ni ) throws TJSException {
		super();
		// initialize members
		mNativeInstance = ni;

		mActivity = new WeakReference<BaseActivity>(TVP.Application.get().CurrentContext);
		BaseActivity activity = mActivity.get();
		if( activity.setWindowForm(this) == false ) {
			mActivity = new WeakReference<BaseActivity>(null); // このウィンドウはメインではない
		}

		mNextSetWindowHandleToDrawDevice = true;
		mLastSentDrawDeviceDestRect = new Rect();
		//mLastSentDrawDeviceDestRect.clear();

		//mInMode = false;
		//mResetStayOnTopStateTick = 0;
		mFocusable = true;
		//mProgramClosing = false;
		//mModalResult = 0;
		mInnerWidthSave = CLIENT_WIDTH;
		mInnerHeightSave = CLIENT_HEIGHT;
		mMenuBarVisible = true;


		mZoomDenom = mActualZoomDenom = 1;
		mZoomNumer = mActualZoomNumer = 1;

		//mUseMouseKey = false;
		//mInMenuLoop = false;
		//mTrapKeys = false;
		//mCanReceiveTrappedKeys = false;
		//mInReceivingTrappedKeys = false;
		//mMouseKeyXAccel = 0;
		//mMouseKeyYAccel = 0;
		//mLastMouseMoved = false;
		//mMouseLeftButtonEmulatedPushed = false;
		//mMouseRightButtonEmulatedPushed = false;
		mLastMouseMovedPos = new Point();
		//mLastMouseMovedPos.x = 0;
		//mLastMouseMovedPos.y = 0;

		mAttentionPoint = new Point();

		//mForceMouseCursorVisible = false;
		//mCurrentMouseCursor = Cursor.getDefaultCursor();

		mPosTrans = new int[2];

		//mLastRecheckInputStateSent = 0;

		mDrawTarget = new DrawTarget();

		boolean ismain = false;
		if(mNativeInstance!=null) ismain = mNativeInstance.isMainWindow();
		if(mNativeInstance!=null) mNativeInstance.setTargetWindow( mDrawTarget, ismain );
		mDrawTarget.setGraphics(activity.getDrawCanvas(),this);

		/*
		mPanel = new ImagePanel();
		mPanel.setDrawListener( this );
		mPanel.setSize(CLIENT_WIDTH, CLIENT_HEIGHT);
		mPanel.setPreferredSize( new Dimension(CLIENT_WIDTH, CLIENT_HEIGHT) );
		mPanel.addKeyListener(this); // リスナー系はウィンドウにつけた方がいいか？
		mPanel.addMouseListener(this);
		mPanel.addMouseMotionListener(this);
		mPanel.addMouseWheelListener(this);
		mPanel.setBackground( new Color(0,true) );
		mPanel.setOpaque(false);
		mPanel.setBorder(new EmptyBorder(0,0,0,0));
		//OverlayLayout layout = new OverlayLayout(this);
		//setLayout(layout);

		mScrollBox = new JScrollPane(mPanel);
		mScrollBox.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED );
		mScrollBox.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );
		mScrollBox.setPreferredSize(new Dimension(CLIENT_WIDTH, CLIENT_HEIGHT));
		getContentPane().add(mScrollBox, BorderLayout.CENTER);
		*/

		if( TVP.getAppTitle() != null ) {
			setTitle(TVP.getAppTitle());
		}
		/*
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);	// windowClosing で処理
		//setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBackground( new Color(0,true) );
		addWindowListener( this );
		addComponentListener( this );
		mDropTarget = new DropTarget( this, DnDConstants.ACTION_COPY_OR_MOVE, this, true );
		*/
		Display disp = ((WindowManager)activity.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		int width = disp.getWidth();
		int height = disp.getHeight();
		setInnerSize( width, height );
	}

	private void setTitle(String title) {
		BaseActivity activity = mActivity.get();
		if( activity != null ) activity.setTitle(title);
	}

	public boolean getWindowActive() { return true; }

	/**
	 * ウィンドウが最初に可視になったときに呼び出されます。
	 */
	public void windowOpened() {
		// ウィンドウが可視になるまえはタイトルバーなどのサイズが得られず、意図したサイズで表示できないので
		// 内部サイズが設定しれていた場合、ここで再度値を設定して意図したサイズにする。
		if( mInnerWidthSave != 0 && mInnerHeightSave != 0 ) {
			setInnerSize( mInnerWidthSave, mInnerHeightSave );
		}
	}
	/**
	 * ユーザーが、ウィンドウのシステムメニューでウィンドウを閉じようとしたときに呼び出されます。
	 */
	public void windowClosing() {
		// closing actions are 3 patterns;
		// 1. closing action by the user
		// 2. "close" method
		// 3. object invalidation
		if( TVP.EventManager.getBreathing() ) {
			return;
		}
		BaseActivity activity = mActivity.get();

		// the default event handler will invalidate this object when an onCloseQuery
		// event reaches the handler.
		if( mNativeInstance != null &&
			(mModalResult == 0 || mModalResult == mrCancel/* mrCancel=when close button is pushed in modal window */  ))
		{
			Dispatch2 obj = mNativeInstance.getOwner();
			if( obj != null ) {
				Variant[] arg = new Variant[1];
				arg[0] = new Variant(1); // true
				final String eventname = "onCloseQuery";
				if( mProgramClosing == false ) {
					// close action does not happen immediately
					if( mNativeInstance != null ) {
						TVP.EventManager.postInputEvent( new WindowEvents.OnCloseInputEvent(mNativeInstance), 0 );
					}
					mClosing = true; // waiting closing...
					//TVPMainForm->NotifyCloseClicked();
				} else {
					mCanCloseWork = true;
					TVP.EventManager.postEvent(obj, obj, eventname, 0, EventManager.EPT_IMMEDIATE, arg );
						// this event happens immediately
						// and does not return until done
					// CanCloseWork is set by the event handler
					if( mCanCloseWork ) {
						if( activity != null ) activity.finish();
					}
				}
			} else {
				if( activity != null ) activity.finish();
			}
		} else {
			if( activity != null ) activity.finish();
		}
	}
	/**
	 *  ウィンドウに対する dispose の呼び出しの結果として、ウィンドウがクローズされたときに呼び出されます。
	 */
	public void windowClosed() {
		//if( mModalResult == 0) Action = caNone; else Action = caHide;

		if( mProgramClosing ) {
			if( mNativeInstance != null ) {
				if( mNativeInstance.isMainWindow() ) {
					// this is the main window
				} else 				{
					// not the main window
					// Action = caFree;
				}

				/*
				if(TVPFullScreenedWindow != this) {
					// if this is not a fullscreened window
					Visible = false;
				}
				*/
				Dispatch2 obj = mNativeInstance.getOwner();
				mNativeInstance.notifyWindowClose();
				try {
					obj.invalidate(0, null, obj);
				} catch (VariantException e1) {
				} catch (TJSException e1) {
				}
				mNativeInstance = null;
			}
		}
	}
	/**
	 * ウィンドウが通常の状態から最小化された状態に変更されたときに呼び出されます。
	 */
	public void windowIconified() {
	}
	/**
	 * ウィンドウが最小化された状態から通常の状態に変更されたときに呼び出されます。
	 */
	public void windowDeiconified() {
	}
	/**
	 * Window がアクティブ Window に設定されると呼び出されます。
	 */
	public void windowActivated() {
		/* モーダルの対処はしない
		GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice graphicsDevice = graphicsEnvironment.getDefaultScreenDevice();
		if( graphicsDevice.getFullScreenWindow() == this ) {
			showModalAtAppActivate();
		}
		*/
	}
	/**
	 * Window がアクティブ Window でなくなったときに呼び出されます。
	 */
	public void windowDeactivated() {
		TVP.clearKeyStates(); // キーボードの状態をクリアしてしまう

		if( mNativeInstance != null ) {
			TVP.EventManager.postInputEvent( new WindowEvents.OnReleaseCaptureInputEvent(mNativeInstance), 0 );
		}

		// hide also popups
		deliverPopupHide();
	}

	/**
	 * タップ判定
	 * @param ct : クリック数
	 * @return イベント消費したかどうか
	 */
	public boolean mouseClicked( int ct ) {
		// fire click event
		if( mNativeInstance != null ) {
			if( ct == 2 ) {
				TVP.EventManager.postInputEvent(
						new WindowEvents.OnDoubleClickInputEvent( mNativeInstance, mLastMouseDownX, mLastMouseDownY), 0);
			} else {
				TVP.EventManager.postInputEvent(
						new WindowEvents.OnClickInputEvent( mNativeInstance, mLastMouseDownX, mLastMouseDownY), 0);
			}
			return true;
		}
		return false;
	}
	/**
	 * コンポーネント上でマウスボタンが押されると呼び出されます。
	 */
	public boolean mousePressed( int x, int y, int mod, int button ,int ct ) {
		if(!canSendPopupHide()) deliverPopupHide();

		// 座標変換
		if( mPanel != null ) {
			ImagePanel panel = mPanel.get();
			if( panel != null ) {
				mPosTrans[0] = x;
				mPosTrans[1] = y;
				panel.translatePosition( mPosTrans );
				x = mPosTrans[0];
				y = mPosTrans[1];
			}
		}

		mLastMouseDownX = x;
		mLastMouseDownY = y;
		if( mNativeInstance != null ) {
			TVP.EventManager.postInputEvent(
				new WindowEvents.OnMouseDownInputEvent( mNativeInstance, x, y, button, mod), 0 );
			return true;
		}
		return false;
	}
	/**
	 * コンポーネント上でマウスボタンが離されると呼び出されます。
	 */
	public boolean mouseReleased( int x, int y, int mod, int button ,int ct ) {
		//::SetCaptureControl(NULL);

		// 座標変換
		if( mPanel != null ) {
			ImagePanel panel = mPanel.get();
			if( panel != null ) {
				mPosTrans[0] = x;
				mPosTrans[1] = y;
				panel.translatePosition( mPosTrans );
				x = mPosTrans[0];
				y = mPosTrans[1];
			}
		}

		if( mNativeInstance != null ) {
			TVP.EventManager.postInputEvent(
				new WindowEvents.OnMouseUpInputEvent( mNativeInstance, x, y, button, mod), 0);
			return true;
		}
		return false;
	}
	/**
	 *  コンポーネントにマウスが入ると呼び出されます。
	 */
	public void mouseEntered() {
		Random.updateEnvironNoiseForTick();
		if( mNativeInstance != null ) {
			TVP.EventManager.postInputEvent( new WindowEvents.OnMouseEnterInputEvent(mNativeInstance), 0 );
		}
	}
	/**
	 * コンポーネントからマウスが出ると呼び出されます。
	 */
	public void mouseExited() {
		Random.updateEnvironNoiseForTick();
		if( mNativeInstance != null ) {
			TVP.EventManager.postInputEvent( new WindowEvents.OnMouseOutOfWindowInputEvent(mNativeInstance), 0 );
			TVP.EventManager.postInputEvent( new WindowEvents.OnMouseLeaveInputEvent(mNativeInstance), 0 );
		}
	}
	/**
	 * コンポーネント上でマウスのボタンを押してドラッグすると呼び出されます。
	 */
	public void mouseDragged() {}
	/**
	 *  ボタンを押さずに、マウスカーソルをコンポーネント上に移動すると呼び出されます。
	 */
	public boolean mouseMoved( int x, int y, int mod, int ct ) {
		boolean ret = false;

		// 座標変換
		if( mPanel != null ) {
			ImagePanel panel = mPanel.get();
			if( panel != null ) {
				mPosTrans[0] = x;
				mPosTrans[1] = y;
				panel.translatePosition( mPosTrans );
				x = mPosTrans[0];
				y = mPosTrans[1];
			}
		}
		restoreMouseCursor();
		if( mNativeInstance != null ) {
			TVP.EventManager.postInputEvent( new WindowEvents.OnMouseMoveInputEvent( mNativeInstance, x, y, mod), EventManager.EPT_DISCARDABLE);
			ret = true;
		}

		//checkMenuBarDrop();
		//restoreMouseCursor();

		int pos = (y << 16) + x;
		Random.pushEnvironNoise(pos);
		mLastMouseMovedPos.x = x;
		mLastMouseMovedPos.y = y;
		return ret;
	}
	/**
	 * マウスホイールが回転すると呼び出されます。
	 */
	/*
	public void mouseWheelMoved(MouseWheelEvent e) {
		int delta = -e.getWheelRotation() * 120; // Windowsと逆、また Windows では 120の倍数として扱われるのでそれに合わせる
		int x = e.getX();
		int y = e.getY();
		int mod = e.getModifiersEx();
		int ct = e.getClickCount();

		// wheel
		if( mNativeInstance != null ) {
			int shift = modifiersToInt(mod,ct,e);
			TVP.EventManager.postInputEvent( new WindowEvents.OnMouseWheelInputEvent( mNativeInstance, shift, delta, x, y), 0 );
			e.consume();
		}
	}
	*/

	/**
	 * キーを入力しているときに呼び出されます。
	 */
	public void keyTyped( char keyChar ) {
		if( mNativeInstance != null && keyChar != 0 ) {
			TVP.EventManager.postInputEvent(new WindowEvents.OnKeyPressInputEvent(mNativeInstance, keyChar), 0);
		}
	}
	/**
	 * キーを押しているときに呼び出されます。
	 */
	public void keyPressed( int keyCode, int mod ) {
		TVP.setKeyState(keyCode); // キー登録

		if( mNativeInstance != null ) {
			int shift = modifiersToInt(mod,0);
			internalKeyDown( keyCode, mIsRepeatMessage ? (shift|SS_REPEAT): shift );
		}
	}
	/**
	 * キーを離したときに呼び出されます。
	 */
	public void keyReleased( int keyCode, int mod ) {
		TVP.resetKeyState( keyCode ); // キー登録解除
		int shift = modifiersToInt(mod,0);
		internalKeyUp( keyCode, shift );
	}

	/**
	 * コンポーネントのサイズが変わると呼び出されます。
	 */
	public void componentResized() {
		// on resize
		if( mNativeInstance != null ) {
			// here specifies EPT_REMOVE_POST, to remove redundant onResize events.
			TVP.EventManager.postInputEvent( new WindowEvents.OnResizeInputEvent(mNativeInstance), EventManager.EPT_REMOVE_POST );
		}
	}

	/**
	 * コンポーネントの位置が変わると呼び出されます。
	 */
	public void componentMoved() { }

	/**
	 * コンポーネントが可視になると呼び出されます。
	 */
	public void componentShown() { }

	/**
	 * コンポーネントが不可視になると呼び出されます。
	 */
	public void componentHidden() { }

	public static final int SS_SHIFT   = 0x01;
	public static final int SS_ALT     = 0x02;
	public static final int SS_CTRL    = 0x04;
	public static final int SS_LEFT    = 0x08;
	public static final int SS_RIGHT   = 0x10;
	public static final int SS_MIDDLE  = 0x20;
	public static final int SS_DOUBLE  = 0x40;
	public static final int SS_REPEAT  = 0x80;
	public static final int SS_ALTGRAPH= 0x100; // AltGraph キー
	public static final int SS_META    = 0x200; // Meta キー
	private static final int META_SHIFT_MASK = 0x000000c1; // KeyEvent.META_SHIFT_MASK API 11
	private static final int META_ALT_MASK = 0x00000032; // META_ALT_MASK API 11
	private static final int META_CTRL_MASK = 0x00007000; // META_CTRL_MASK API 11
	private static final int META_META_MASK = 0x00070000; // META_META_MASK API 11
	public static int modifiersToInt( int state, int clickcount ) {
		int f = 0;
		if( (state&META_SHIFT_MASK) != 0 ) f += SS_SHIFT;
		if( (state&META_ALT_MASK ) != 0 ) f += SS_ALT;
		if( (state&META_CTRL_MASK ) != 0 ) f += SS_CTRL;
//		if( (state&InputEvent.BUTTON1_DOWN_MASK ) != 0 ) f += SS_LEFT;
//		if( (state&InputEvent.BUTTON2_DOWN_MASK ) != 0 ) f += SS_RIGHT;
//		if( (state&InputEvent.BUTTON3_DOWN_MASK ) != 0 ) f += SS_MIDDLE;
		if( (state&META_META_MASK ) != 0 ) f += SS_META;
		if( clickcount == 2 ) f += SS_DOUBLE;
		return f;
	}
	public static final boolean isAnyMouseButtonPressedInShiftStateFlags( int state) {
		return (state & (SS_LEFT | SS_RIGHT | SS_MIDDLE | SS_DOUBLE)) != 0;
	}
	//---


	public void close() {
		// closing action by "close" method
		if(mClosing) return; // already waiting closing...
		mProgramClosing = true;
		windowClosing();
		if( mCanCloseWork ) {
			windowClosed();
			BaseActivity activity = mActivity.get();
			if( activity != null ) {
				activity.exitFromScript();
				activity = null;
			}
			System.gc();
		}
		mProgramClosing = false;
	}
	public void invalidateClose() {
		// closing action by object invalidation;
		// this will not cause any user confirmation of closing the window.
		mNativeInstance = null;
		BaseActivity activity = mActivity.get();
		if( activity != null ) activity.finish();
	}
	public void getCursorPos( jp.kirikiri.tvp2.visual.Point pt ) {
		pt.x = 0;
		pt.y = 0;
	}
	private Point getCursorPos() {
		return new Point(0,0);
	}
	public void setFullScreenMode( boolean b ) { }
	public boolean getFullScreenMode() { return false; }

	public void onDraw( Canvas graphic) {
		// a painting event
		if( mNextSetWindowHandleToDrawDevice ) {
			boolean ismain = false;
			if(mNativeInstance!=null) ismain = mNativeInstance.isMainWindow();
			if(mNativeInstance!=null) mNativeInstance.setTargetWindow( mDrawTarget, ismain );
			mNextSetWindowHandleToDrawDevice = false;
		}
		setDrawDeviceDestRect();
		mDrawTarget.setGraphics(graphic,this);

		if( mNativeInstance != null ) {
			Rect r = new Rect();
			r.left   = 0;
			r.top    = 0;
			if( mInnerWidthSave == 0 || mInnerHeightSave == 0 ) {
				BaseActivity activity = mActivity.get();
				if( activity != null ) {
					r.right  = activity.getWidth();
					r.bottom = activity.getHeight();
				}
			} else {
				r.right  = mInnerWidthSave;
				r.bottom = mInnerHeightSave;
			}
			mNativeInstance.notifyWindowExposureToLayer(r);
		}
	}
	private void setDrawDeviceDestRect() {
		BaseActivity activity = mActivity.get();
		int w = 0;
		int h = 0;
		if( mInnerWidthSave == 0 || mInnerHeightSave == 0 ) {
			if( activity != null ) {
				w  = activity.getWidth();
				h = activity.getHeight();
			}
		} else {
			w  = mInnerWidthSave;
			h = mInnerHeightSave;
		}
		Rect destrect = new Rect( 0, 0, w, h );

		if( mLastSentDrawDeviceDestRect.notEquals( destrect ) ) {
			if( mNativeInstance != null )
				mNativeInstance.setDestRectangle(destrect);
			mLastSentDrawDeviceDestRect.set( destrect );
		}
	}
	public boolean getFormEnabled() { return true; }

	public void sendCloseMessage() {
		BaseActivity activity = mActivity.get();
		if( activity != null ) activity.finish();
	}
	public void tickBeat() throws TJSException {
		// called every 50ms intervally
		long tickcount = TVP.getTickCount();
		//boolean focused = isFocused();
		boolean showingmenu = mInMenuLoop;

		// set mouse cursor state
		setForceMouseCursorVisible(showingmenu);

		// mouse key は無視
		/*
		if( UseMouseKey && PaintBox && !showingmenu && focused) {
			GenerateMouseEvent(false, false, false, false);
		}
		*/

		// check RecheckInputState
		if(tickcount - mLastRecheckInputStateSent > 1000) {
			mLastRecheckInputStateSent = tickcount;
			if(mNativeInstance!=null) mNativeInstance.recheckInputState();
		}
	}
	public void resetDrawDevice() throws TJSException {
		mNextSetWindowHandleToDrawDevice = true;
		mLastSentDrawDeviceDestRect.clear();
		repaint();
	}
	public void internalKeyDown(int key, int shift) {
		long tick = TVP.getTickCount();
		Random.pushEnvironNoise(tick);
		Random.pushEnvironNoise(key);
		Random.pushEnvironNoise(shift);

		if( mNativeInstance != null ) {
			/*
			if( mUseMouseKey && mPanel != null ) {
				if(key == VirtualKey.VK_RETURN || key == VirtualKey.VK_SPACE || key == VirtualKey.VK_ESCAPE ) {
					Point pos = getCursorPos();
					//if(tp.x >= 0 && tp.y >= 0 && tp.x < ScrollBox->Width && tp.y < ScrollBox->Height)
					if( pos != null && pos.x >= 0 && pos.y >= 0 && pos.x < mPanel.getWidth() && pos.y < mPanel.getHeight() ) {
						if( key == VirtualKey.VK_RETURN || key == VirtualKey.VK_SPACE  ) {
							mMouseLeftButtonEmulatedPushed = true;
							//paintBoxMouseDown( PaintBox, Controls::mbLeft, TShiftState(), pos.x, pos.y );
						}

						if( key == VirtualKey.VK_ESCAPE  ) {
							mMouseRightButtonEmulatedPushed = true;
							//paintBoxMouseDown(PaintBox, Controls::mbRight, TShiftState(), pos.x, pos.y);
						}
					}
					return;
				}

				switch(key) {
				case VirtualKey.VK_LEFT:
				//case VK_PADLEFT:
					if( mMouseKeyXAccel == 0 && mMouseKeyYAccel == 0) {
						generateMouseEvent(true, false, false, false);
						mLastMouseKeyTick = (int) (TVP.getTickCount() + 100);
					}
					return;
				case VirtualKey.VK_RIGHT:
				//case VK_PADRIGHT:
					if( mMouseKeyXAccel == 0 && mMouseKeyYAccel == 0) {
						generateMouseEvent(false, true, false, false);
						mLastMouseKeyTick = (int) (TVP.getTickCount() + 100);
					}
					return;
				case VirtualKey.VK_UP:
				//case VK_PADUP:
					if( mMouseKeyXAccel == 0 && mMouseKeyYAccel == 0 ) {
						generateMouseEvent(false, false, true, false);
						mLastMouseKeyTick = (int) (TVP.getTickCount() + 100);
					}
					return;
				case VirtualKey.VK_DOWN:
				//case VK_PADDOWN:
					if( mMouseKeyXAccel == 0 && mMouseKeyYAccel == 0 ) {
						generateMouseEvent(false, false, false, true);
						mLastMouseKeyTick = (int) (TVP.getTickCount() + 100);
					}
					return;
				}
			}
			*/
			TVP.EventManager.postInputEvent(new WindowEvents.OnKeyDownInputEvent( mNativeInstance, key, shift), 0 );
		}
	}
	private void internalKeyUp(int key, int shift) {
		long tick = TVP.getTickCount();
		Random.pushEnvironNoise(tick);
		Random.pushEnvironNoise(key);
		Random.pushEnvironNoise(shift);
		if( mNativeInstance != null ) {
			// マウスキー機能は未実装
			TVP.EventManager.postInputEvent(new WindowEvents.OnKeyUpInputEvent( mNativeInstance, key, shift), 0);
		}
	}

	public void onKeyUp( int key, int shift ) {
		internalKeyUp(key, shift);
	}
	public void onKeyPress(char key) {
		if( mNativeInstance != null && key != 0 ) {
			//if( mUseMouseKey && (key == 0x1b || key == 13 || key == 32)) return;
			TVP.EventManager.postInputEvent(new WindowEvents.OnKeyPressInputEvent(mNativeInstance, key), 0 );
		}
	}
	public void setPaintBoxSize(int w, int h) {
		mLayerWidth  = w;
		mLayerHeight = h;
		internalSetPaintBoxSize();
	}
	public void setCursorPos(int x, int y) {
		// TODO 指定不可
		restoreMouseCursor();
	}
	public void setHintText(String text) {
		//mPanel.setToolTipText(text);
		// hint text は実装した方がいいかも
	}
	public void disableAttentionPoint() {
		mAttentionPointEnabled = false;
	}
	public void setImeMode(int mode) {
	}
	public int getDefaultImeMode() {
		return 0;
	}
	public void resetImeMode() {
	}

	private void acquireImeControl() {
	}
	public void zoomRectangle(Rect rect) {
		rect.left =   (rect.left   * mActualZoomNumer) / mActualZoomDenom;
		rect.top =    (rect.top    * mActualZoomNumer) / mActualZoomDenom;
		rect.right =  (rect.right  * mActualZoomNumer) / mActualZoomDenom;
		rect.bottom = (rect.bottom * mActualZoomNumer) / mActualZoomDenom;
	}
	public void registerWindowMessageReceiver(int mode, Object proc, Object userdata) {
	}
	public void onCloseQueryCalled(boolean b) throws VariantException, TJSException {
		BaseActivity activity = mActivity.get();
		// closing is allowed by onCloseQuery event handler
		if(!mProgramClosing) {
			// closing action by the user
			if(b) {
				if(mInMode)
					mModalResult = 1; // when modal
				else
					setVisible( false );  // just hide

				mClosing = false;
				if(mNativeInstance!=null) {
					if(mNativeInstance.isMainWindow()) {
						// this is the main window
						Dispatch2 obj = mNativeInstance.getOwner();
						obj.invalidate(0, null, obj);
						mNativeInstance = null;
					}
				} else {
					if( activity != null ) activity.finish();
				}
			} else {
				mClosing = false;
			}
		} else {
			// closing action by the program
			mCanCloseWork = b;
		}
	}
	public void beginMove() { }
	public void bringToFront() { }

	public void updateWindow(int type) throws VariantException, TJSException {
		if( mNativeInstance != null ) {
			Rect r = new Rect();
			r.left = 0;
			r.top = 0;
			r.right = mLayerWidth;
			r.bottom = mLayerHeight;
			mNativeInstance.notifyWindowExposureToLayer(r);

			TVP.EventManager.deliverWindowUpdateEvents();
		}
	}
	public void setMouseCursor(int handle) {
	}
	public void hideMouseCursor() {
		// hide mouse cursor temporarily
	    setMouseCursorState(mcsTempHidden);
	}
	public void setMouseCursorState(int mcs) {
		if( mMouseCursorState == mcsVisible && mcs != mcsVisible) {
			// formerly visible and newly invisible
			if(!mForceMouseCursorVisible) setMouseCursorVisibleState(false);
		} else if(mMouseCursorState != mcsVisible && mcs == mcsVisible) {
			// formerly invisible and newly visible
			if(!mForceMouseCursorVisible) setMouseCursorVisibleState(true);
		}
		if( mMouseCursorState != mcs && mcs == mcsTempHidden) {
			Point pos = getCursorPos();
			mLastMouseScreenX = pos.x;
			mLastMouseScreenY = pos.y;
		}
		mMouseCursorState = mcs;
	}
	private void restoreMouseCursor() {
		// restore mouse cursor hidden by HideMouseCursor
		if( mMouseCursorState == mcsTempHidden ) {
			Point pt = getCursorPos();
			if( mLastMouseScreenX != pt.x || mLastMouseScreenY != pt.y ) {
				setMouseCursorState(mcsVisible);
			}
		}
	}
	/**
	 * set mouse cursor visible state
	 * this does not look mMouseCursorState
	 * @param b true 表示 / false 非表示
	 */
	private void setMouseCursorVisibleState(boolean b) {
	}
	private void setForceMouseCursorVisible(boolean s) {
		if(mForceMouseCursorVisible != s) {
			if(s) {
				// force visible mode
				// the cursor is to be fixed in Cursor.getDefaultCursor()
				setDefaultMouseCursor();
			} else {
				// normal mode
				// restore normal cursor
				setMouseCursorVisibleState(mMouseCursorState == mcsVisible);
			}
			mForceMouseCursorVisible = s;
		}
	}

	public void setDefaultMouseCursor() {
	}
	public boolean getVisible() {
		return isVisible();
	}
	public String getCaption() {
		return getTitle();
	}
	public void setCaption(String v) {
		setTitle( v );
	}

	public void setWidth(int w) {
		setSize( w, getHeight() );
	}
	public void setHeight(int h) {
		setSize( getWidth(), h );
	}
	public void setLeft(int l) {
	}
	public int getLeft() {
		return 0;
	}
	public void setTop(int t) {
	}
	public int getTop() {
		return 0;
	}
	public void setMinWidth(int v) {
	}
	public int getMinWidth() {
		return 0;
	}
	public void setMinHeight(int v) {
	}
	public int getMinHeight() {
		return 0;
	}
	public void setMinSize(int w, int h) {
	}
	public void setMaxWidth(int v) {
	}
	public void setMaxHeight(int v) {
	}
	public int getMaxWidth() {
		BaseActivity activity = mActivity.get();
		if( activity != null ) return activity.getWidth();
		return 0;
	}
	public int getMaxHeight() {
		BaseActivity activity = mActivity.get();
		if( activity != null ) return activity.getHeight();
		return 0;
	}
	public void setMaxSize(int w, int h) {
	}
	public void setPosition(int l, int t) {
	}
	private void internalSetPaintBoxSize() {
		int l = (mLayerLeft   * mActualZoomNumer) / mActualZoomDenom;
		int t = (mLayerTop    * mActualZoomNumer) / mActualZoomDenom;
		int w = (mLayerWidth  * mActualZoomNumer) / mActualZoomDenom;
		int h = (mLayerHeight * mActualZoomNumer) / mActualZoomDenom;
		if( mPanel != null ) {
			ImagePanel panel = mPanel.get();
			if( panel != null ) {
				panel.setLocation( l, t, w, h );
			}
		}
		setDrawDeviceDestRect();
	}
	public void setLayerLeft(int left) {
		if( mLayerLeft != left ) {
			mLayerLeft = left;
			if(mPanel!=null) internalSetPaintBoxSize();
		}
	}
	public int getLayerLeft() { return mLayerLeft; }
	public void setLayerTop(int top) {
		if( mLayerTop != top ) {
			mLayerTop = top;
			if(mPanel!=null) internalSetPaintBoxSize();
		}
	}
	public int getLayerTop() { return mLayerTop; }
	public void setLayerPosition( int left, int top ) {
		if( mLayerLeft != left || mLayerTop != top ) {
			mLayerLeft = left;
			mLayerTop = top;
			if(mPanel!=null) internalSetPaintBoxSize();
		}
	}
	public void setInnerSunken(boolean b) {
	}
	public boolean getInnerSunken() {
		return false;
	}
	public void setInnerWidth(int w) {
		mInnerWidthSave = w;
		ImagePanel panel = mPanel.get();
		if( panel != null ) panel.setSize(mInnerWidthSave,mInnerHeightSave);
	}
	public int getInnerWidth() {
		return mInnerWidthSave;
	}
	public void setInnerHeight(int h) {
		mInnerHeightSave = h;
		ImagePanel panel = mPanel.get();
		if( panel != null ) panel.setSize(mInnerWidthSave,mInnerHeightSave);
	}
	public int getInnerHeight() {
		return mInnerHeightSave;
	}
	public void setInnerSize(int w, int h) {
		mInnerWidthSave = w;
		mInnerHeightSave = h;

		ImagePanel panel = mPanel.get();
		if( panel != null ) panel.setSize(mInnerWidthSave,mInnerHeightSave);
	}
	public void setMenuBarVisible(boolean b) {
		mMenuBarVisible = b;
	}
	public boolean getMenuBarVisible() {
		return mMenuBarVisible;
	}
	public void setAttentionPoint(int left, int top, jp.kirikiri.tvp2env.Font font) {
		// set attention point information
		mAttentionPoint.x = left;
		mAttentionPoint.y = top;
		mAttentionPointEnabled = true;
		acquireImeControl();
	}

	public void showWindowAsModal() throws TJSException {
		mModalResult = 0;
		mInMode = true;
		//setVisible( true );
		if( mNativeInstance != null ) {
			Dispatch2 obj = mNativeInstance.getOwner();
			Variant tmp = new Variant(1);
			try {
				String prop = TVP.Properties.getProperty("modal_force_result_true","true");
				if( "yes".equals(prop) || "true".equals(prop) ) {
					String name = TVP.Properties.getProperty("modal_force_result_member_name","result");
					if( obj.isValid( 0, name, obj ) == Error.S_TRUE ) {
						obj.propSet( Interface.MEMBERENSURE, name, tmp, obj );
					}
				}
			} catch (VariantException e) {
			} catch (TJSException e) {
			}
		}
	}
	public void removeMaskRegion() {}

	public void setZoom(int numer, int denom ) {
		setZoom( numer, denom, true );
	}
	public void setZoom(int numer, int denom, boolean set_logical) {
		// set layer zooming factor;
		// the zooming factor is passed in numerator/denoiminator style.
		// we must find GCM to optimize numer/denium via Euclidean algorithm.
		Point ad = adjustNumerAndDenom(numer, denom);
		numer = ad.x;
		denom = ad.y;
		if(set_logical) {
			mZoomNumer = numer;
			mZoomDenom = denom;
		}
		if(!getFullScreenMode()) {
			// in fullscreen mode, zooming factor is controlled by the system
			mActualZoomDenom = denom;
			mActualZoomNumer = numer;
		}
		internalSetPaintBoxSize();
	}
	private Point adjustNumerAndDenom(int n, int d) {
		int a = n;
		int b = d;
		while(b!=0) {
			int t = b;
			b = a % b;
			a = t;
		}
		n = n / a;
		d = d / a;
		return new Point(n,d);
	}

	public boolean getFocusable() {
		return mFocusable;
	}
	public void setFocusable( boolean b ) {
		mFocusable = b;
	}
	public void setZoomNumer(int n) {
		setZoom(n, mZoomDenom);
	}
	public int getZoomNumer() { return mZoomNumer; }

	public void setZoomDenom(int d) {
		setZoom(mZoomNumer, d);
	}
	public int getZoomDenom() { return mZoomDenom; }
	public void setBorderStyle(int st) {
		mBorderStyle = st;
	}
	public int getBorderStyle() { return mBorderStyle; }

	public void setStayOnTop(boolean b) {
	}
	public boolean getStayOnTop() {
		return true;
	}
	public void setShowScrollBars(boolean b) {
	}
	public boolean getShowScrollBars() {
		return false;
	}
	public void setUseMouseKey(boolean b) {
		mUseMouseKey = b;
	}
	public boolean getUseMouseKey() { return mUseMouseKey; }

	public void setTrapKey(boolean b) {
		mTrapKeys = b;
		if(mTrapKeys) {
			// reset CanReceiveTrappedKeys and InReceivingTrappedKeys
			mCanReceiveTrappedKeys = false;
			mInReceivingTrappedKeys = false;
			// note that SetTrapKey can be called while the key trapping is
			// processing.
		}
	}
	public boolean getTrapKey() { return mTrapKeys; }

	public int getMouseCursorState() { return mMouseCursorState; }
	/*
	public NativeImageBuffer createOffscreenImage() {
		Rectangle rt = getBounds();
		return new NativeImageBuffer( this, rt.width, rt.height );
	}
	*/
	private void deliverPopupHide() {
		// deliver onPopupHide event to unfocusable windows
		int count = TVP.WindowList.getWindowCount();
		for( int i = count - 1; i >= 0; i-- ) {
			WindowNI win = TVP.WindowList.getWindowListAt(i);
			if( win != null ) {
				WindowForm form = win.getForm();
				if(form != null ) {
					form.firePopupHide();
				}
			}
		}
	}

	private void firePopupHide() {
		// fire "onPopupHide" event
		if(!canSendPopupHide()) return;
		if(!getVisible()) return;

		TVP.EventManager.postInputEvent( new WindowEvents.OnPopupHideInputEvent(mNativeInstance), 0 );
	}

	private boolean canSendPopupHide() {
		return !mFocusable && getVisible() && getStayOnTop();
	}

	// Display resolution mode for full screen
	/** auto negotiation */
	static private final int fsrAuto = 0;
	/**
	 * let screen resolution fitting neaest to the preferred resolution,
	 * preserving the original aspect ratio
	 */
	static private final int fsrProportional = 1;
	/**
	 * let screen resolution fitting neaest to the preferred resolution.
	 * There is no guarantee that the aspect ratio is preserved
	 */
	static private final int fsrNearest = 2;
	/** no change resolution */
	static private final int fsrNoChange = 3;

	/** no zoom by the engine */
	static private final int fszmNone = 0;
	/** inner fit on the monitor (uncovered areas may be filled with black) */
	static private final int fszmInner = 1;
	/** outer fit on the monitor (primary layer may jut out of the monitor) */
	static private final int fszmOuter = 2;

	static private final String getGetFullScreenResolutionModeString( int mode ) {
		switch(mode) {
		case fsrAuto:				return "fsrAuto";
		case fsrProportional:		return "fsrProportional";
		case fsrNearest:			return "fsrNearest";
		case fsrNoChange:			return "fsrNoChange";
		}
		return "";
	}
	static private ScreenMode DefaultScreenMode;
	static private final void getOriginalScreenMetrics() {
		// retrieve original (un-fullscreened) information
		if( DefaultScreenMode == null ) {
			DefaultScreenMode = new ScreenMode();
		}
		WindowManager wm = (WindowManager)TVP.Application.get().getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		DefaultScreenMode.mWidth = display.getWidth();
		DefaultScreenMode.mHeight = display.getHeight();
		DefaultScreenMode.mBitsPerPixel = 32;
	}
	/**
	 * enumerate all display modes
	 */
	static private final void enumerateAllDisplayModes( ArrayList<ScreenMode> modes ) {
		modes.clear();
	}
	/**
	 * do reduction for numer over denom
	 */
	static private final void doReductionNumerAndDenom( Point p ) {
		int a = p.x;
		int b = p.y;
		while(b!=0) {
			int t = b;
			b = a % b;
			a = t;
		}
		p.x = p.x / a;
		p.y = p.y / a;
	}
	/**
	 * make full screen mode candidates
	 *
	 * Always call this function *before* entering full screen mode
	 * @param preferred
	 * @param mode
	 * @param zoom_mode
	 * @param candidates
	 */
	public static void makeFullScreenModeCandidates(ScreenMode preferred, int mode, int zoom_mode, ArrayList<ScreenModeCandidate> candidates) {
		// adjust give parameter
		if(mode == fsrAuto && zoom_mode == fszmNone) zoom_mode = fszmInner;
			// fszmInner is ignored (as always be fszmInner) if mode == fsrAuto && zoom_mode == fszmNone

		// デバッグ情報を出力
		DebugClass.addLog("(info) Searching best fullscreen resolution ...");
		DebugClass.addLog("(info) condition: preferred screen mode: " + preferred.dump() );
		DebugClass.addLog("(info) condition: mode: " + getGetFullScreenResolutionModeString(mode));
		DebugClass.addLog("(info) condition: zoom mode: " + (
			zoom_mode == fszmInner ? "inner" :
			zoom_mode == fszmOuter ? "outer" :
				"none") );

		// 現在のスクリーン情報を取得する
		getOriginalScreenMetrics();

		// decide preferred bpp
		int preferred_bpp = preferred.mBitsPerPixel == 0 ? DefaultScreenMode.mBitsPerPixel : preferred.mBitsPerPixel;

		// get original screen aspect ratio
		int screen_aspect_numer = DefaultScreenMode.mWidth;
		int screen_aspect_denom = DefaultScreenMode.mHeight;
		Point pos = new Point(screen_aspect_numer, screen_aspect_denom);
		doReductionNumerAndDenom( pos ); // do reduction
		screen_aspect_numer = pos.x;
		screen_aspect_denom = pos.y;
		DebugClass.addLog("(info) environment: default screen mode: " + DefaultScreenMode.dump());
		DebugClass.addLog("(info) environment: default screen aspect ratio: " +
			screen_aspect_numer + ":" + screen_aspect_denom );

		// clear destination array
		candidates.clear();

		// enumerate all display modes
		ArrayList<ScreenMode> modes = new ArrayList<ScreenMode>();
		enumerateAllDisplayModes(modes);
		Collections.sort(modes, new Comparator<ScreenMode> () {
			@Override
			public int compare(ScreenMode o1, ScreenMode o2) {
				int area_this = o1.mWidth * o1.mHeight;
				int area_rhs  = o2.mWidth * o2.mHeight;
				if(area_this < area_rhs) return -1;
				if(area_this > area_rhs) return 1;
				if(o1.mBitsPerPixel < o2.mBitsPerPixel) return -1;
				if(o1.mBitsPerPixel > o2.mBitsPerPixel) return 1;
				return 0;
			}
		});

		{
			int last_width = -1, last_height = -1;
			String last_line = null;
			DebugClass.addLog("(info) environment: available display modes:");
			final int count = modes.size();
			for( int i = 0; i < count; i++ ) {
				ScreenMode m = modes.get(i);
				if( last_width != m.mWidth || last_height != m.mHeight ) {
					if(last_line != null) DebugClass.addLog(last_line);

					pos.x = m.mWidth;
					pos.y = m.mHeight;
					doReductionNumerAndDenom(pos);
					int w = pos.x;
					int h = pos.y;
					last_line = "(info)  " + m.dumpHeightAndWidth() +
						", AspectRatio=" + w + ":" + h +
						", BitsPerPixel=" + m.mBitsPerPixel;
				} else {
					last_line += "/" + m.mBitsPerPixel;
				}
				last_width = m.mWidth;
				last_height = m.mHeight;
			}
			if( last_line != null ) DebugClass.addLog(last_line);
		}

		if( mode != fsrNoChange ) {
			if( mode != fsrNearest ) {
				// for fstAuto and fsrProportional, we need to see screen aspect ratio

				// reject screen mode which does not match the original screen aspect ratio
				final int count = modes.size();
				for( int i = count-1; i >= 0; i-- ) {
					ScreenMode m = modes.get(i);
					pos.x = m.mWidth;
					pos.y = m.mHeight;
					doReductionNumerAndDenom(pos);
					int aspect_numer = pos.x;
					int aspect_denom = pos.y;
					if(aspect_numer != screen_aspect_numer || aspect_denom != screen_aspect_denom)
						modes.remove(i);
				}
			}

			if(zoom_mode == fszmNone) {
				// we cannot use resolution less than preferred resotution when
				// we do not use zooming, so reject them.
				final int count = modes.size();
				for( int i = count-1; i >= 0; i-- ) {
					ScreenMode m = modes.get(i);
					if( m.mWidth < preferred.mWidth || m.mHeight < preferred.mHeight)
						modes.remove(i);
				}
			}
		} else {
			// reject resolutions other than the original size
			final int count = modes.size();
			for( int i = count-1; i >= 0; i-- ) {
				ScreenMode m = modes.get(i);
				if( m.mWidth  != DefaultScreenMode.mWidth || m.mHeight != DefaultScreenMode.mHeight )
					modes.remove(i);
			}
		}

		// reject resolutions larger than the default screen mode
		int count = modes.size();
		for( int i = count-1; i >= 0; i-- ) {
			ScreenMode m = modes.get(i);
			if( m.mWidth > DefaultScreenMode.mWidth || m.mHeight > DefaultScreenMode.mHeight )
				modes.remove(i);
		}

		// reject resolutions less than 16
		count = modes.size();
		for( int i = count-1; i >= 0; i-- ) {
			ScreenMode m = modes.get(i);
			if( m.mBitsPerPixel < 16 )
				modes.remove(i);
		}

		// check there is at least one candidate mode
		if( modes.size() == 0 ) {
			// panic! no candidates
			// this could be if the driver does not provide the screen
			// mode which is the same size as the default screen...
			// push the default screen mode
			DebugClass.addImportantLog("(info) Panic! There is no reasonable candidate screen mode provided from the driver ... trying to use the default screen size and color depth ...");
			modes.add(new ScreenMode(DefaultScreenMode.mWidth,DefaultScreenMode.mHeight,DefaultScreenMode.mBitsPerPixel));
		}

		// copy modes to candidation, with making zoom ratio and resolution rank
		count = modes.size();
		for( int i = count-1; i >= 0; i-- ) {
			ScreenMode m = modes.get(i);
			ScreenModeCandidate candidate = new ScreenModeCandidate();
			candidate.mWidth = m.mWidth;
			candidate.mHeight = m.mHeight;
			candidate.mBitsPerPixel = m.mBitsPerPixel;
			if( zoom_mode != fszmNone ) {
				double width_r  = (double)candidate.mWidth /  (double)preferred.mWidth;
				double height_r = (double)candidate.mHeight / (double)preferred.mHeight;

				// select width or height, to fit to target screen from preferred size
				if( zoom_mode == fszmInner ? (width_r < height_r) : (width_r > height_r) ) {
					candidate.mZoomNumer = candidate.mWidth;
					candidate.mZoomDenom = preferred.mWidth;
				} else {
					candidate.mZoomNumer = candidate.mHeight;
					candidate.mZoomDenom = preferred.mHeight;
				}

				// if the zooming range is between 1.00 and 1.034 we treat this as 1.00
				double zoom_r = (double)candidate.mZoomNumer / (double)candidate.mZoomDenom;
				if( zoom_r > 1.000 && zoom_r < 1.034 )
					candidate.mZoomDenom = candidate.mZoomNumer = 1;
			} else {
				// zooming disabled
				candidate.mZoomDenom = candidate.mZoomNumer = 1;
			}
			pos.x = candidate.mZoomNumer;
			pos.y = candidate.mZoomDenom;
			doReductionNumerAndDenom(pos);
			candidate.mZoomNumer = pos.x;
			candidate.mZoomDenom = pos.y;

			// make rank on each candidate

			// BPP
			// take absolute difference of preferred and candidate.
			// lesser bpp has less priority, so add 1000 to lesser bpp.
			candidate.mRankBPP = Math.abs(preferred_bpp - candidate.mBitsPerPixel);
			if(candidate.mBitsPerPixel < preferred_bpp) candidate.mRankBPP += 1000;

			// Zoom-in
			// we usually use zoom-in, zooming out (this situation will occur if
			// the screen resolution is lesser than expected) has lesser priority.
			if(candidate.mZoomNumer < candidate.mZoomDenom)
				candidate.mRankZoomIn = 1;
			else
				candidate.mRankZoomIn = 0;

			// Zoom-Beauty
			if( mode == fsrAuto ) {
				// 0: no zooming is the best.
				// 1: zooming using monitor's function is fastest and most preferable.
				// 2: zooming using kirikiri's zooming functions is somewhat slower but not so bad.
				// 3: zooming using monitor's function and kirikiri's function tends to be dirty
				//   because the zooming is applied twice. this is not preferable.
				int zoom_rank = 0;
				if(candidate.mWidth != DefaultScreenMode.mWidth || candidate.mHeight != DefaultScreenMode.mHeight)
					zoom_rank += 1; // zoom by monitor

				if(candidate.mZoomNumer != 1 || candidate.mZoomDenom != 1)
					zoom_rank += 2; // zoom by the engine

				candidate.mRankZoomBeauty = zoom_rank;
			} else {
				// Zoom-Beauty is not considered
				candidate.mRankZoomBeauty = 0;
			}

			// Size
			// size rank is a absolute difference between area size of candidate and preferred.
			candidate.mRankSize = Math.abs( candidate.mWidth * candidate.mHeight - preferred.mWidth * preferred.mHeight);

			// push candidate into candidates array
			candidates.add(candidate);
		}

		// sort candidate by its rank
		Collections.sort(candidates, new Comparator<ScreenModeCandidate> () {
			@Override
			public int compare(ScreenModeCandidate o1, ScreenModeCandidate o2) {
				if(o1.mRankZoomIn < o2.mRankZoomIn) return -1;
				if(o1.mRankZoomIn > o2.mRankZoomIn) return 1;
				if(o1.mRankBPP < o2.mRankBPP) return -1;
				if(o1.mRankBPP > o2.mRankBPP) return 1;
				if(o1.mRankZoomBeauty < o2.mRankZoomBeauty) return -1;
				if(o1.mRankZoomBeauty > o2.mRankZoomBeauty) return 1;
				if(o1.mRankSize < o2.mRankSize) return -1;
				if(o1.mRankSize > o2.mRankSize) return 1;
				return 0;
			}
		});

		// dump all candidates to log
		DebugClass.addLog("(info) result: candidates:");
		count = candidates.size();
		for( int i = 0; i < count; i++ ) {
			ScreenModeCandidate c = candidates.get(i);
			DebugClass.addLog("(info)  " + c.dump() );
		}
	}

	public MenuItem getMainMenu() {
		if( mRootMenuItem == null ) {
			mRootMenuItem = new MenuItem();
		}
		return mRootMenuItem;
	}

	private String getTitle() {
		BaseActivity activity = mActivity.get();
		if( activity != null ) return activity.getTitle().toString();
		return null;
	}

	public void setVisible(boolean s) throws TJSException {
		mVisible = s;
		if( s ) {
			BaseActivity activity = mActivity.get();
			if( activity != null ) onDraw( activity.getDrawCanvas() );
		}
	}
	public boolean isVisible() {
		return mVisible;
	}

	public int getWidth() {
		BaseActivity activity = mActivity.get();
		if( activity != null ) return activity.getWidth();
		return 0;
	}

	public int getHeight() {
		BaseActivity activity = mActivity.get();
		if( activity != null ) return activity.getHeight();
		return 0;
	}

	public void setSize(int w, int h) {
	}
	public void repaint() throws TJSException {
		BaseActivity activity = mActivity.get();
		if( activity != null ) {
			activity.show();
		}
	}
	public void setPanel( ImagePanel panel ) {
		if( panel == null ) {
			if( mPanel != null ) {
				mPanel.clear();
			} else {
				mPanel = new WeakReference<ImagePanel>(null);
			}
		} else {
			mPanel = new WeakReference<ImagePanel>(panel);
			panel.setSize( mInnerWidthSave, mInnerHeightSave );
		}
	}

	public void showMenu() {
		BaseActivity activity = mActivity.get();
		if( activity != null ) activity.onKeyUpMenu( KeyEvent.KEYCODE_MENU, 0 );
	}
	public BaseActivity getActivity() {
		return mActivity.get();
	}
	public void doActiveWindow() {
		if( mNativeInstance != null ) {
			mNativeInstance.fireOnActivate(true);
		}
	}
	public void doDeactiveWindow() {
		if( mNativeInstance != null ) {
			mNativeInstance.fireOnActivate(true);
		}
	}
	public void doOrientationChanged() {
		if( mNativeInstance != null ) {
			mNativeInstance.fireOnOrientationChanged();
		}
	}

}
