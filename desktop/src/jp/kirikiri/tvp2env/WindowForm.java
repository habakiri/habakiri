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

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.InputEvent;
import java.awt.event.InputMethodEvent;
import java.awt.event.InputMethodListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.font.TextHitInfo;
import java.awt.im.InputMethodRequests;
import java.awt.image.BufferStrategy;
import java.io.File;
import java.io.IOException;
import java.text.AttributedCharacterIterator;
import java.text.AttributedCharacterIterator.Attribute;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.MouseInputListener;

import jp.kirikiri.tjs2.Error;
import jp.kirikiri.tjs2.Interface;
import jp.kirikiri.tjs2.Dispatch2;
import jp.kirikiri.tjs2.TJS;
import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tjs2.Variant;
import jp.kirikiri.tjs2.VariantException;
import jp.kirikiri.tvp2.TVP;
import jp.kirikiri.tvp2.base.EventManager;
import jp.kirikiri.tvp2.base.WindowEvents;
import jp.kirikiri.tvp2.utils.DebugClass;
import jp.kirikiri.tvp2.utils.Random;
import jp.kirikiri.tvp2.visual.Rect;
import jp.kirikiri.tvp2.visual.ScreenMode;
import jp.kirikiri.tvp2.visual.ScreenModeCandidate;
import jp.kirikiri.tvp2.visual.WindowNI;

public class WindowForm  extends JFrame implements WindowListener, KeyListener, InputMethodListener, InputMethodRequests, MouseInputListener, MouseWheelListener, PanelDrawListener, ComponentListener, DropTargetListener {

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

	private static final int mbLeft=0;
	private static final int mbRight=1;
	private static final int mbMiddle =2;

	/** the mouse cursor is visible */
	private static final int mcsVisible = 0;
	/** the mouse cursor is temporarily hidden */
	private static final int mcsTempHidden = 1;
	/** the mouse cursor is invisible */
	private static final int mcsHidden = 2;

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
	private Point mAttentionPoint;
	private java.awt.Font mAttentionFont;

	//-- mouse cursor
	private int mMouseCursorState;
	private boolean mForceMouseCursorVisible; // true in menu select
	private Cursor mCurrentMouseCursor;
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

	private ImagePanel mPanel;
	private JScrollPane mScrollBox;
	private DrawTarget mDrawTarget;
	private DropTarget mDropTarget;
	private JMenuBar mMenuBar;
	private MenuItem mRootMenuItem;

	private long mLastRecheckInputStateSent;
	//private BufferStrategy mBufferStrategy;

	public WindowForm( WindowNI ni ) {
		super();
		// initialize members
		mNativeInstance = ni;

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
		mCurrentMouseCursor = Cursor.getDefaultCursor();


		//mLastRecheckInputStateSent = 0;

		mDrawTarget = new DrawTarget();

		enableInputMethods(true);
		addInputMethodListener(this);
		addKeyListener(this);
		requestFocus();

		Color transcolor = new Color(0,true);
		mPanel = new ImagePanel();
		mPanel.setOwner(this);
		mPanel.setDrawListener( this );
		mPanel.setSize(CLIENT_WIDTH, CLIENT_HEIGHT);
		mPanel.setPreferredSize( new Dimension(CLIENT_WIDTH, CLIENT_HEIGHT) );
		//mPanel.addKeyListener(this); // リスナー系はウィンドウにつけた方がいいか？
		mPanel.addMouseListener(this);
		mPanel.addMouseMotionListener(this);
		mPanel.addMouseWheelListener(this);
		mPanel.setOpaque(false);
		mPanel.setBackground(transcolor);
		mPanel.setBorder(new EmptyBorder(0,0,0,0));

		//mPanel.addInputMethodListener(this);
		//OverlayLayout layout = new OverlayLayout(this);
		//setLayout(layout);

		mScrollBox = new JScrollPane(mPanel);
		mScrollBox.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED );
		mScrollBox.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );
		mScrollBox.setPreferredSize(new Dimension(CLIENT_WIDTH, CLIENT_HEIGHT));
		mScrollBox.setOpaque(false);
		mScrollBox.setBackground(transcolor);
		JViewport viewport = mScrollBox.getViewport();
		viewport.setOpaque(true);
		viewport.setBackground(new Color(0,false));
		getContentPane().add(mScrollBox, BorderLayout.CENTER);

		/*
		try {
			createBufferStrategy(3);
			mBufferStrategy = getBufferStrategy();
		} catch (Exception e) {
		}
		*/

		if( TVP.getAppTitle() != null ) {
			setTitle(TVP.getAppTitle());
		}
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);	// windowClosing で処理
		//setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		//setBackground(transcolor);
		addWindowListener( this );
		addComponentListener( this );
		mDropTarget = new DropTarget( this, DnDConstants.ACTION_COPY_OR_MOVE, this, true );
		setSize( CLIENT_WIDTH, CLIENT_HEIGHT );
	}

	/*
	@Override
	public void paintComponents( Graphics graphic ) {
	}
	@Override
	public void paint( Graphics graphic ) {
		mPanel.paintComponent(graphic);
	}
	*/
	public boolean getWindowActive() { return isActive(); }
	/*
	private void unacquireImeControl();
	private void acquireImeControl();
	*/
	// private TTVPWindowForm * GetKeyTrapperWindow();
	//static boolean FindKeyTrapper(LRESULT &result, UINT msg, WPARAM wparam, LPARAM lparam);
	//bool ProcessTrappedKeyMessage(LRESULT &result, UINT msg, WPARAM wparam, LPARAM lparam);
	/**
	 * ウィンドウが最初に可視になったときに呼び出されます。
	 */
	@Override
	public void windowOpened(WindowEvent e) {
		// ウィンドウが可視になるまえはタイトルバーなどのサイズが得られず、意図したサイズで表示できないので
		// 内部サイズが設定しれていた場合、ここで再度値を設定して意図したサイズにする。
		if( mInnerWidthSave != 0 && mInnerHeightSave != 0 ) {
			setInnerSize( mInnerWidthSave, mInnerHeightSave );
		}
	}
	/**
	 * ユーザーが、ウィンドウのシステムメニューでウィンドウを閉じようとしたときに呼び出されます。
	 */
	@Override
	public void windowClosing(WindowEvent e) {
		// closing actions are 3 patterns;
		// 1. closing action by the user
		// 2. "close" method
		// 3. object invalidation
		if( TVP.EventManager.getBreathing() ) {
			return;
		}

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
						dispose();
					}
				}
			} else {
				dispose();
			}
		} else {
			dispose();
		}
	}
	/**
	 *  ウィンドウに対する dispose の呼び出しの結果として、ウィンドウがクローズされたときに呼び出されます。
	 */
	@Override
	public void windowClosed(WindowEvent e) {
		//if( mModalResult == 0) Action = caNone; else Action = caHide;

		//if( mProgramClosing )
		{
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
					if( obj != null ) obj.invalidate(0, null, obj);
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
	@Override
	public void windowIconified(WindowEvent e) {
	}
	/**
	 * ウィンドウが最小化された状態から通常の状態に変更されたときに呼び出されます。
	 */
	@Override
	public void windowDeiconified(WindowEvent e) {
	}
	/**
	 * Window がアクティブ Window に設定されると呼び出されます。
	 */
	@Override
	public void windowActivated(WindowEvent e) {
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
	@Override
	public void windowDeactivated(WindowEvent e) {
		TVP.clearKeyStates(); // キーボードの状態をクリアしてしまう

		if( mNativeInstance != null ) {
			TVP.EventManager.postInputEvent( new WindowEvents.OnReleaseCaptureInputEvent(mNativeInstance), 0 );
		}

		// hide also popups
		deliverPopupHide();
	}
	/**
	 *  コンポーネント上でマウスボタンをクリック (押してから離す) したときに呼び出されます。
	 */
	@Override
	public void mouseClicked(MouseEvent e) {
		/*
		int x = e.getX();
		int y = e.getY();
		int mod = e.getModifiersEx();
		int button = e.getButton();
		*/
		int ct = e.getClickCount();
		// fire click event
		if( mNativeInstance != null ) {
			if( ct == 2 ) {
				//System.out.print("Mouse 2 click\n");
				//TVP.EventManager.postInputEvent(
				//		new WindowEvents.OnDoubleClickInputEvent( mNativeInstance, mLastMouseDownX, mLastMouseDownY), 0);
			} else {
				/*
				System.out.print("Mouse click\n");
				TVP.EventManager.postInputEvent(
						new WindowEvents.OnClickInputEvent( mNativeInstance, mLastMouseDownX, mLastMouseDownY), 0);
				*/
			}
			e.consume();
		}
	}
	/**
	 * コンポーネント上でマウスボタンが押されると呼び出されます。
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		int mod = e.getModifiersEx();
		int button = getMouseButtonNumber(e);
		int ct = e.getClickCount();

		if(!canSendPopupHide()) deliverPopupHide();

		mLastMouseDownX = x;
		mLastMouseDownY = y;

		if( mNativeInstance != null ) {
			if( button == mbLeft && ct == 2 ) {
				// Windows とイベント発生順をそろえるためにここにDblClickを挿入する
				//System.out.print("Mouse 2 click\n");
				TVP.EventManager.postInputEvent(
						new WindowEvents.OnDoubleClickInputEvent( mNativeInstance, mLastMouseDownX, mLastMouseDownY), 0);
			}
			//System.out.print("Mouse down\n");
			int shift = modifiersToInt(mod,ct,e) & (~SS_DOUBLE);
			TVP.EventManager.postInputEvent(
				new WindowEvents.OnMouseDownInputEvent( mNativeInstance, x, y, button, shift), 0 );
			e.consume();
		}
	}
	/**
	 * コンポーネント上でマウスボタンが離されると呼び出されます。
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		int mod = e.getModifiersEx();
		int button = getMouseButtonNumber(e);
		int ct = e.getClickCount();

		//::SetCaptureControl(NULL);

		if( mNativeInstance != null ) {
			if( button == mbLeft && ct != 2 ) {
				// Windows とイベント発生順をそろえるためにここにClickを挿入する
				//System.out.print("Mouse click\n");
				TVP.EventManager.postInputEvent(
						new WindowEvents.OnClickInputEvent( mNativeInstance, mLastMouseDownX, mLastMouseDownY), 0);
			}
			//System.out.print("Mouse up\n");
			int shift = modifiersToInt(mod,ct,e) & (~SS_DOUBLE);
			TVP.EventManager.postInputEvent(
				new WindowEvents.OnMouseUpInputEvent( mNativeInstance, x, y, button, shift), 0);
			e.consume();
		}
	}
	/**
	 *  コンポーネントにマウスが入ると呼び出されます。
	 */
	@Override
	public void mouseEntered(MouseEvent e) {
		Random.updateEnvironNoiseForTick();

		if( mNativeInstance != null ) {
			TVP.EventManager.postInputEvent( new WindowEvents.OnMouseEnterInputEvent(mNativeInstance), 0 );
		}
	}
	/**
	 * コンポーネントからマウスが出ると呼び出されます。
	 */
	@Override
	public void mouseExited(MouseEvent e) {
		Random.updateEnvironNoiseForTick();

		if( mNativeInstance != null ) {
			TVP.EventManager.postInputEvent( new WindowEvents.OnMouseOutOfWindowInputEvent(mNativeInstance), 0 );
			TVP.EventManager.postInputEvent( new WindowEvents.OnMouseLeaveInputEvent(mNativeInstance), 0 );
		}
	}
	/**
	 * コンポーネント上でマウスのボタンを押してドラッグすると呼び出されます。
	 */
	@Override
	public void mouseDragged(MouseEvent e) {
		restoreMouseCursor();

		int x = e.getX();
		int y = e.getY();
		int mod = e.getModifiersEx();
		int ct = e.getClickCount();

		if( mNativeInstance != null ) {
			int shift = modifiersToInt(mod,ct,e) & (~SS_DOUBLE);
			TVP.EventManager.postInputEvent( new WindowEvents.OnMouseMoveInputEvent( mNativeInstance, x, y, shift), EventManager.EPT_DISCARDABLE);
			e.consume();
		}

		//checkMenuBarDrop();
		//restoreMouseCursor();

		int pos = (y << 16) + x;
		Random.pushEnvironNoise(pos);

		mLastMouseMovedPos.x = x;
		mLastMouseMovedPos.y = y;
	}
	/**
	 *  ボタンを押さずに、マウスカーソルをコンポーネント上に移動すると呼び出されます。
	 */
	@Override
	public void mouseMoved(MouseEvent e) {
		restoreMouseCursor();

		int x = e.getX();
		int y = e.getY();
		int mod = e.getModifiersEx();
		int ct = e.getClickCount();

		if( mNativeInstance != null ) {
			int shift = modifiersToInt(mod,ct,e) & (~SS_DOUBLE);
			TVP.EventManager.postInputEvent( new WindowEvents.OnMouseMoveInputEvent( mNativeInstance, x, y, shift), EventManager.EPT_DISCARDABLE);
			e.consume();
		}

		//checkMenuBarDrop();
		//restoreMouseCursor();

		int pos = (y << 16) + x;
		Random.pushEnvironNoise(pos);

		mLastMouseMovedPos.x = x;
		mLastMouseMovedPos.y = y;
	}
	/**
	 * マウスホイールが回転すると呼び出されます。
	 */
	@Override
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
	/**
	 * キーを入力しているときに呼び出されます。
	 */
	@Override
	public void keyTyped(KeyEvent e) {
		this.requestFocus();
		char keyChar = e.getKeyChar();
		if( mNativeInstance != null && keyChar != 0 ) {
			//if( mUseMouseKey && (keyChar == 0x1b || keyChar == 13 || keyChar == 32)) return;
			TVP.EventManager.postInputEvent(new WindowEvents.OnKeyPressInputEvent(mNativeInstance, keyChar), 0);
		}
	}
	/**
	 * キーを押しているときに呼び出されます。
	 */
	@Override
	public void keyPressed(KeyEvent e) {
		TVP.setKeyState(e.getKeyCode()); // キー登録

		if( mNativeInstance != null ) {
			int mod = e.getModifiersEx();
			int shift = modifiersToInt(mod,0,null);
			internalKeyDown( e.getKeyCode(), mIsRepeatMessage ? (shift|SS_REPEAT): shift );
		}
	}
	/**
	 * キーを離したときに呼び出されます。
	 */
	@Override
	public void keyReleased(KeyEvent e) {
		TVP.resetKeyState(e.getKeyCode()); // キー登録解除

		int mod = e.getModifiersEx();
		int shift = modifiersToInt(mod,0,null);
		internalKeyUp( e.getKeyCode(), shift );
	}

	/**
	 * コンポーネントのサイズが変わると呼び出されます。
	 */
	@Override
	public void componentResized(ComponentEvent e) {
		// on resize
		if( mNativeInstance != null ) {
			// here specifies EPT_REMOVE_POST, to remove redundant onResize
			// events.
			TVP.EventManager.postInputEvent( new WindowEvents.OnResizeInputEvent(mNativeInstance), EventManager.EPT_REMOVE_POST );
		}
	}

	/**
	 * コンポーネントの位置が変わると呼び出されます。
	 */
	@Override
	public void componentMoved(ComponentEvent e) {
	}

	/**
	 * コンポーネントが可視になると呼び出されます。
	 */
	@Override
	public void componentShown(ComponentEvent e) {
	}

	/**
	 * コンポーネントが不可視になると呼び出されます。
	 */
	@Override
	public void componentHidden(ComponentEvent e) {
	}

	/**
	 *  ドラッグ操作中に、リスナーに登録された DropTarget のドロップサイトの操作可能な部分にマウスポインタが入ったときに呼び出されます。
	 */
	@Override
	public void dragEnter(DropTargetDragEvent dtde) {
	}

	/**
	 *  ドラッグ操作中に、リスナーに登録された DropTarget のドロップサイトの操作可能な部分にマウスポインタがまだあるときに呼び出されます。
	 */
	@Override
	public void dragOver(DropTargetDragEvent dtde) {
		if(dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
			dtde.acceptDrag(DnDConstants.ACTION_COPY);
			return;
		}
		dtde.rejectDrag();
	}

	/**
	 * ユーザーが現在のドロップジェスチャーを変更した場合に呼び出されます。
	 */
	@Override
	public void dropActionChanged(DropTargetDragEvent dtde) {
	}

	/**
	 *ドラッグ操作中に、リスナーに登録された DropTarget のドロップサイトの操作可能な部分からマウスポインタが出たときに呼び出されます。
	 */
	@Override
	public void dragExit(DropTargetEvent dte) {
	}

	/**
	 * リスナーに登録された DropTarget のドロップサイトの操作可能な部分へのドロップでドラッグ操作が終了したときに呼び出されます。
	 */
	@Override
	public void drop(DropTargetDropEvent dtde) {
		if(mNativeInstance==null) {
			dtde.rejectDrop();
			return;
		}

		Dispatch2 array = null;
		try {
			array = TJS.createArrayObject();
			if( dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor) ) {
				dtde.acceptDrop(DnDConstants.ACTION_COPY);
				Transferable t = dtde.getTransferable();
				@SuppressWarnings("rawtypes")
				java.util.List list = (java.util.List)t.getTransferData( DataFlavor.javaFileListFlavor);
				Variant val = new Variant();
				int count = 0;
				for( Object o: list ) {
					if(o instanceof File) {
						File f = (File) o;
						val.set( TVP.StorageMediaManager.normalizeStorageName(f.getAbsolutePath(),null) );
						// push into array
						array.propSetByNum(Interface.MEMBERENSURE|Interface.IGNOREPROP, count, val, array);
						count++;
					}
				}
				Variant arg = new Variant(array, array);
				TVP.EventManager.postInputEvent( new WindowEvents.OnFileDropInputEvent(mNativeInstance, arg), 0);
				dtde.dropComplete(true);
				array = null;
				return;
			}
		}catch(UnsupportedFlavorException ufe) {
			array = null;
		}catch(IOException ioe) {
			array = null;
		} catch (VariantException e) {
			array = null;
		} catch (TJSException e) {
			array = null;
		}
		dtde.rejectDrop();
	}
	private static final int getMouseButtonNumber( MouseEvent e ) {
		int num = e.getButton();
		if( num == MouseEvent.BUTTON1 ) return mbLeft;
		if( num == MouseEvent.BUTTON2 ) return mbMiddle;
		if( num == MouseEvent.BUTTON3 ) return mbRight;
		/*
		if( SwingUtilities.isLeftMouseButton(e) ) return mbLeft;
		if( SwingUtilities.isRightMouseButton(e) ) return mbRight;
		if( SwingUtilities.isMiddleMouseButton(e) ) return mbMiddle;
		*/
		return mbLeft; // どれも押されていない時は、左を返す
	}

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
	public static int modifiersToInt( int state, int clickcount, MouseEvent e ) {
		int f = 0;
		if( (state&InputEvent.SHIFT_DOWN_MASK) != 0 ) f += SS_SHIFT;
		if( (state&InputEvent.ALT_DOWN_MASK ) != 0 ) f += SS_ALT;
		if( (state&InputEvent.CTRL_DOWN_MASK ) != 0 ) f += SS_CTRL;
		/*
		if( e != null ) {
			if( SwingUtilities.isLeftMouseButton(e) ) f += SS_LEFT;
			if( SwingUtilities.isRightMouseButton(e) ) f += SS_RIGHT;
			if( SwingUtilities.isMiddleMouseButton(e) ) f += SS_MIDDLE;
		}
		*/
		if( (state&InputEvent.BUTTON1_DOWN_MASK ) != 0 ) f += SS_LEFT;
		if( (state&InputEvent.BUTTON2_DOWN_MASK ) != 0 ) f += SS_MIDDLE;
		if( (state&InputEvent.BUTTON3_DOWN_MASK ) != 0 ) f += SS_RIGHT;
		if( (state&InputEvent.ALT_GRAPH_DOWN_MASK  ) != 0 ) f += SS_ALTGRAPH;
		if( (state&InputEvent.META_DOWN_MASK ) != 0 ) f += SS_META;
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
		//getToolkit().getSystemEventQueue().postEvent( new WindowEvent( this, WindowEvent.WINDOW_CLOSING) );
		// sync call
		windowClosing(null);
		mProgramClosing = false;
	}
	public void invalidateClose() {
		// closing action by object invalidation;
		// this will not cause any user confirmation of closing the window.
		mNativeInstance = null;
		super.setVisible( false );
		dispose();
	}
	public void getCursorPos( jp.kirikiri.tvp2.visual.Point pt ) {
		Point pos = mPanel.getMousePosition();
		if( pos != null ) {
			pt.x = pos.x;
			pt.y = pos.y;
		} else {
			PointerInfo pi = MouseInfo.getPointerInfo();
		  	Point p = pi.getLocation();
		  	SwingUtilities.convertPointFromScreen(p, mPanel);
			pt.x = p.x;
			pt.y = p.y;
		}
	}
	private Point getCursorPos() {
		Point pos = mPanel.getMousePosition();
		if( pos != null ) {
			return pos;
		} else {
			PointerInfo pi = MouseInfo.getPointerInfo();
		  	Point p = pi.getLocation();
		  	SwingUtilities.convertPointFromScreen(p, mPanel);
		  	return p;
		}
	}
	public void setFullScreenMode( boolean b ) {
		GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice graphicsDevice = graphicsEnvironment.getDefaultScreenDevice();
		if( b ) {
			if( graphicsDevice.getFullScreenWindow() != this ) {

				// save position and size
				Rectangle rect = super.getBounds();
				mOrgLeft = rect.x;
				mOrgTop = rect.y;
				mOrgWidth = rect.width;
				mOrgHeight = rect.height;

				dispose(); //destroy the native resources
				setUndecorated(true);
				setVisible(true); //rebuilding the native resources
				graphicsDevice.setFullScreenWindow( this );
			}
		} else {
			if( graphicsDevice.getFullScreenWindow() == this ) {
				graphicsDevice.setFullScreenWindow( null );
				dispose();
				setUndecorated(false);
				setVisible(true);
				repaint();
			}
		}
		requestFocusInWindow();
	}
	public boolean getFullScreenMode() {
		GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice graphicsDevice = graphicsEnvironment.getDefaultScreenDevice();
		return graphicsDevice.getFullScreenWindow() == this;
	}

	@Override
	public void onDraw(Graphics graphic) {
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
			Rectangle tr = mPanel.getBounds();
			r.left   = tr.x;
			r.top    = tr.y;
			r.right  = tr.x + tr.width;
			r.bottom = tr.y + tr.height;
			mNativeInstance.notifyWindowExposureToLayer(r);
		}
	}
	private void setDrawDeviceDestRect() {
		int x_ofs = 0;
		int y_ofs = 0;

		Rectangle tr = mPanel.getBounds();
		Rect destrect = new Rect(tr.x + x_ofs, tr.y + y_ofs,
				tr.width + tr.x + x_ofs, tr.height + tr.y + y_ofs);

		if( mLastSentDrawDeviceDestRect.notEquals( destrect ) ) {
			if( mNativeInstance != null )
				mNativeInstance.setDestRectangle(destrect);
			mLastSentDrawDeviceDestRect.set( destrect );
		}
	}
	public boolean getFormEnabled() {
		return isEnabled();
	}
	public void sendCloseMessage() {
		getToolkit().getSystemEventQueue().postEvent( new WindowEvent( this, WindowEvent.WINDOW_CLOSING) );
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
	public void resetDrawDevice() {
		mNextSetWindowHandleToDrawDevice = true;
		mLastSentDrawDeviceDestRect.clear();
		if(mPanel!=null) mPanel.repaint();
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
		JScrollBar bar = mScrollBox.getVerticalScrollBar();
		if( bar != null ) bar.setValue(0);
		bar = mScrollBox.getHorizontalScrollBar();
		if( bar != null ) bar.setValue(0);
		mLayerWidth  = w;
		mLayerHeight = h;
		internalSetPaintBoxSize();
	}
	public void setCursorPos(int x, int y) {
		// TODO 指定不可
		restoreMouseCursor();
	}
	public void setHintText(String text) {
		mPanel.setToolTipText(text);
	}
	public void disableAttentionPoint() {
		mAttentionPointEnabled = false;
	}
	public void setImeMode(int mode) {
		// TODO 指定不可
	}
	public int getDefaultImeMode() {
		// TODO 指定不可
		return 0;
	}
	public void resetImeMode() {
		// TODO 指定不可
	}

	private void acquireImeControl() {
		this.requestFocus();
		this.enableInputMethods(true);
	}
	public void zoomRectangle(Rect rect) {
		rect.left =   (rect.left   * mActualZoomNumer) / mActualZoomDenom;
		rect.top =    (rect.top    * mActualZoomNumer) / mActualZoomDenom;
		rect.right =  (rect.right  * mActualZoomNumer) / mActualZoomDenom;
		rect.bottom = (rect.bottom * mActualZoomNumer) / mActualZoomDenom;
	}
	public void registerWindowMessageReceiver(int mode, Object proc, Object userdata) {
		// TODO 仕様未決、未実装
	}
	public void onCloseQueryCalled(boolean b) throws VariantException, TJSException {
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
					dispose();
				}
			} else {
				mClosing = false;
			}
		} else {
			// closing action by the program
			mCanCloseWork = b;
		}
	}
	public void beginMove() {
		// TODO 未実装
	}
	public void bringToFront() {
		toFront();
	}
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
		Cursor cur = TVP.MouseCursor.getCursor(handle);
		if( cur != null ) {
			if( mMouseCursorState == mcsVisible && !mForceMouseCursorVisible ) {
				setCursor(cur);
			}
			mCurrentMouseCursor = cur;
		} else {
			// cur が null (見付からない) ときは、デフォルトのカーソルを設定する
			mCurrentMouseCursor = Cursor.getDefaultCursor();
			setCursor( mCurrentMouseCursor );
		}
	}
	/*
	@Override
	public void setCursor(Cursor cursor) {
		super.setCursor(cursor);
		if( mPanel != null ) {
			mPanel.setCursor(cursor);
		}
	}
	*/
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
		if(b)
			setCursor(mCurrentMouseCursor);
		else
			setCursor(TVP.MouseCursor.getCursor(MouseCursor.crNone));
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
		if( mCurrentMouseCursor != Cursor.getDefaultCursor() ) {
			mCurrentMouseCursor = Cursor.getDefaultCursor();
			if( getCursor() != mCurrentMouseCursor ) {
				setCursor( mCurrentMouseCursor );
			}
		}
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
	/* オーバーライドせず、元のものを使う
	@Override
	public void setSize( int w, int h ) {
		Insets insets = getInsets();
		w+=insets.left+insets.right;
		h+=insets.top+insets.bottom;
		if( mPanel != null ) {
			insets = mPanel.getInsets();
			w+=insets.left+insets.right;
			h+=insets.top+insets.bottom;
			mPanel.setSize(w,h);
		}
		super.setSize(w,h);
	}
	*/
	public void setWidth(int w) {
		setSize( w, getHeight() );
	}
	public void setHeight(int h) {
		setSize( getWidth(), h );
	}
	public void setLeft(int l) {
		setLocation( l, getY() );
	}
	public int getLeft() {
		return getX();
	}
	public void setTop(int t) {
		setLocation( getX(), t );
	}
	public int getTop() {
		return getY();
	}
	public void setMinWidth(int v) {
		 Dimension dim = getMinimumSize();
		 dim.width = v;
		 setMinimumSize(dim);
	}
	public int getMinWidth() {
		return getMinimumSize().width;
	}
	public void setMinHeight(int v) {
		 Dimension dim = getMinimumSize();
		 dim.height = v;
		 setMinimumSize(dim);
	}
	public int getMinHeight() {
		return getMinimumSize().height;
	}
	public void setMinSize(int w, int h) {
		 Dimension dim = getMinimumSize();
		 dim.width = w;
		 dim.height = h;
		 setMinimumSize(dim);
	}
	public void setMaxWidth(int v) {
		 Dimension dim = getMaximumSize();
		 dim.width = v;
		 setMaximumSize(dim);
	}
	public void setMaxHeight(int v) {
		 Dimension dim = getMaximumSize();
		 dim.height = v;
		 setMaximumSize(dim);
	}
	public int getMaxWidth() {
		return getMaximumSize().width;
	}
	public int getMaxHeight() {
		return getMaximumSize().height;
	}
	public void setMaxSize(int w, int h) {
		 Dimension dim = getMaximumSize();
		 dim.width = w;
		 dim.height = h;
		 setMaximumSize(dim);
	}
	public void setPosition(int l, int t) {
		setLocation( l, t );
	}
	private void internalSetPaintBoxSize() {
		int l = (mLayerLeft   * mActualZoomNumer) / mActualZoomDenom;
		int t = (mLayerTop    * mActualZoomNumer) / mActualZoomDenom;
		int w = (mLayerWidth  * mActualZoomNumer) / mActualZoomDenom;
		int h = (mLayerHeight * mActualZoomNumer) / mActualZoomDenom;
		mPanel.setSize(w,h);
		mPanel.setLocation( l ,t );
		mPanel.setPreferredSize( new Dimension(w, h) );
		//mPanel.setBounds(l, t, w, h);
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
		if( b ) {
			mScrollBox.setViewportBorder( new BevelBorder(BevelBorder.LOWERED) );
		} else {
			mScrollBox.setViewportBorder( new EmptyBorder(0,0,0,0) );
		}
	}
	public boolean getInnerSunken() {
		Border b = mScrollBox.getViewportBorder();
		return b instanceof LineBorder;
	}
	public void setInnerWidth(int w) {
		mInnerWidthSave = w;
		int h = mPanel.getHeight();
		Insets insets = mPanel.getInsets();
		w+=insets.left+insets.right;
		h+=insets.top+insets.bottom;
		mPanel.setSize(w,h);

		insets = getInsets();
		w+=insets.left+insets.right;
		h+=insets.top+insets.bottom;

		insets = mScrollBox.getInsets();
		w+=insets.left+insets.right;
		h+=insets.top+insets.bottom;

		if( getJMenuBar() != null ) {
			JMenuBar bar = getJMenuBar();
			h += bar.getHeight();
		}
		super.setSize(w,h);
	}
	public int getInnerWidth() {
		/*
		int w = mPanel.getWidth();
		Insets insets = mPanel.getInsets();
		w-=insets.left+insets.right;
		return w;
		*/

		int w = mScrollBox.getViewport().getWidth();
		Insets insets = mPanel.getInsets();
		w-=insets.left+insets.right;
		return w;
	}
	public void setInnerHeight(int h) {
		mInnerHeightSave = h;
		int w = mPanel.getWidth();
		Insets insets = mPanel.getInsets();
		w+=insets.left+insets.right;
		h+=insets.top+insets.bottom;
		mPanel.setSize(w,h);

		insets = getInsets();
		w+=insets.left+insets.right;
		h+=insets.top+insets.bottom;

		insets = mScrollBox.getInsets();
		w+=insets.left+insets.right;
		h+=insets.top+insets.bottom;

		if( getJMenuBar() != null ) {
			JMenuBar bar = getJMenuBar();
			h += bar.getHeight();
		}
		super.setSize(w,h);
	}
	public int getInnerHeight() {
		/*
		int h = mPanel.getHeight();
		Insets insets = mPanel.getInsets();
		h-=insets.top+insets.bottom;
		return h;
		*/

		int h = mScrollBox.getViewport().getHeight();
		Insets insets = mPanel.getInsets();
		h-=insets.top+insets.bottom;
		return h;
	}
	public void setInnerSize(int w, int h) {
		mInnerWidthSave = w;
		mInnerHeightSave = h;
		Insets insets = mPanel.getInsets();
		w+=insets.left+insets.right;
		h+=insets.top+insets.bottom;
		mPanel.setSize(w,h);
		mPanel.setPreferredSize(new Dimension(w,h) );

		insets = mScrollBox.getInsets();
		w+=insets.left+insets.right;
		h+=insets.top+insets.bottom;
		Border border = mScrollBox.getViewportBorder();
		if( border != null ) {
			insets = border.getBorderInsets(mScrollBox);
			w+=insets.left+insets.right;
			h+=insets.top+insets.bottom;
		}

		insets = getInsets();
		w+=insets.left+insets.right;
		h+=insets.top+insets.bottom;

		if( getJMenuBar() != null ) {
			JMenuBar bar = getJMenuBar();
			h += bar.getHeight();
		}
		super.setSize(w,h);
	}
	public void setMenuBarVisible(boolean b) {
		mMenuBarVisible = b;
		JMenuBar bar = getJMenuBar();
		if( bar != null ) {
			bar.setVisible(b);
		}
	}
	public boolean getMenuBarVisible() {
		JMenuBar bar = getJMenuBar();
		if( bar != null ) {
			return bar.isVisible();
		}
		return false;
	}
	public void setAttentionPoint(int left, int top, jp.kirikiri.tvp2env.Font font) {
		Insets insets = mPanel.getInsets();
		left+=insets.left;
		top+=insets.top;

		insets = mScrollBox.getInsets();
		left+=insets.left;
		top+=insets.top;

		insets = getInsets();
		left+=insets.left;
		top+=insets.top;

		// set attention point information
		mAttentionPoint.x = left;
		mAttentionPoint.y = top;
		mAttentionPointEnabled = true;
		if( font != null ) {
			mAttentionFont = font.getFont();
		} else {
			mAttentionFont = jp.kirikiri.tvp2env.Font.getDefaultFont().getFont();
		}
		acquireImeControl();
	}

	public void showWindowAsModal() {
		// TODO モーダル表示不可
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
	public void removeMaskRegion() {
		// TODO リージョン設定不可
	}

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
		//return getFocusableWindowState();
	}
	@Override
	public void setFocusable( boolean b ) {
		mFocusable = b;
		//super.setFocusable(b);
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

		switch( st ) {
		case bsNone:
			setResizable(false);
			break;
		case bsSingle:
			setResizable(false);
			break;
		case bsSizeable:
			setResizable(true);
			break;
		case bsDialog:
			setResizable(false);
			break;
		case bsToolWindow:
			setResizable(false);
			break;
		case bsSizeToolWin:
			setResizable(true);
			break;
		}
	}
	public int getBorderStyle() { return mBorderStyle; }

	public void setStayOnTop(boolean b) {
		setAlwaysOnTop( b );
	}
	public boolean getStayOnTop() {
		return isAlwaysOnTop();
	}
	public void setShowScrollBars(boolean b) {
		if( b ) {
			mScrollBox.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			mScrollBox.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		} else {
			mScrollBox.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER );
			mScrollBox.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );
		}
	}
	public boolean getShowScrollBars() {
		JScrollBar vbar = mScrollBox.getVerticalScrollBar();
		JScrollBar hbar = mScrollBox.getHorizontalScrollBar();
		return (vbar != null && vbar.isVisible()) || (hbar!=null && hbar.isVisible() );
	}
	public void setUseMouseKey(boolean b) {
		mUseMouseKey = b;
		/*
		if( b ) {
			mMouseLeftButtonEmulatedPushed = false;
			mMouseRightButtonEmulatedPushed = false;
			mLastMouseKeyTick = (int) TVP.getTickCount();
		} else {
			if( mMouseLeftButtonEmulatedPushed ) {
				mMouseLeftButtonEmulatedPushed = false;
				paintBoxMouseUp( mbLeft, 0, mLastMouseMovedPos.x, mLastMouseMovedPos.y);
			}
			if( mMouseRightButtonEmulatedPushed ) {
				mMouseRightButtonEmulatedPushed = false;
				paintBoxMouseUp( mbRight, 0, mLastMouseMovedPos.x, mLastMouseMovedPos.y);
			}
		}
		*/
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
		GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice graphicsDevice = graphicsEnvironment.getDefaultScreenDevice();
		DisplayMode mode = graphicsDevice.getDisplayMode();

		DefaultScreenMode.mWidth = mode.getWidth();
		DefaultScreenMode.mHeight = mode.getHeight();
		DefaultScreenMode.mBitsPerPixel = mode.getBitDepth();
	}
	/**
	 * enumerate all display modes
	 */
	static private final void enumerateAllDisplayModes( ArrayList<ScreenMode> modes ) {
		modes.clear();

		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice device = ge.getDefaultScreenDevice();
		DisplayMode[] dmodes = device.getDisplayModes();

		final int count = dmodes.length;
		for( int i = 0; i < count; i++ ) {
			DisplayMode m = dmodes[i];
			modes.add( new ScreenMode(m.getWidth(),m.getHeight(),m.getBitDepth()) );
		}
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
		if( mMenuBar == null ) {
			mMenuBar = new JMenuBar();
			setJMenuBar(mMenuBar);
		}
		if( mRootMenuItem == null ) {
			mRootMenuItem = new MenuItem( mMenuBar );
		}
		return mRootMenuItem;
	}
	public int getInnerWidthSave() {
		return mInnerWidthSave;
	}
	public int getInnerHeightSave() {
		return mInnerHeightSave;
	}
	/*
	public void drawImage() {
		try {
			if( mBufferStrategy == null ) {
				createBufferStrategy(3);
				mBufferStrategy = getBufferStrategy();
			}
			if( mBufferStrategy.contentsLost() ) {
				createBufferStrategy(3);
				mBufferStrategy = getBufferStrategy();
			}
			Graphics g = mBufferStrategy.getDrawGraphics();
			if(!mBufferStrategy.contentsLost()){
				Insets insets = mPanel.getInsets();
				int x = insets.left;
				int y = insets.top;

				insets = getInsets();
				x+=insets.left;
				y+=insets.top;

				insets = mScrollBox.getInsets();
				x+=insets.left;
				y+=insets.top;
				if( getJMenuBar() != null ) {
					JMenuBar bar = getJMenuBar();
					y += bar.getHeight();
				}

				mPanel.drawImage(g,x,y);
				mBufferStrategy.show();
				g.dispose();
			}
		} catch( Exception e ) {
		}
	}
	*/
	/*
	public int getInnerWidth() {
		Insets insets = mPanel.getInsets();
		int w = mPanel.getWidth();
		w-=insets.left+insets.right;
		return w;
	}
	public int getInnerHeight() {
		Insets insets = mPanel.getInsets();
		int h = mPanel.getHeight();
		h-=insets.top+insets.bottom;
		return h;
	}
	*/

	public void repaintPanel() {
		mPanel.repaint();
	}

	public void showMenu() {
		// PC 版では何もしない
	}

	@Override
	public Rectangle getTextLocation(TextHitInfo offset) {
		return new Rectangle(mAttentionPoint.x,mAttentionPoint.y,10,10);
	}

	@Override
	public TextHitInfo getLocationOffset(int x, int y) {
		return null;
	}

	@Override
	public int getInsertPositionOffset() {
		return 0;
	}

	@Override
	public AttributedCharacterIterator getCommittedText(int beginIndex, int endIndex, Attribute[] attributes) {
		return null;
	}

	@Override
	public int getCommittedTextLength() {
		return 0;
	}

	@Override
	public AttributedCharacterIterator cancelLatestCommittedText( Attribute[] attributes) {
		return null;
	}

	@Override
	public AttributedCharacterIterator getSelectedText(Attribute[] attributes) {
		return null;
	}

	@Override
	public void inputMethodTextChanged(InputMethodEvent event) {
		if( mNativeInstance != null ) {
			AttributedCharacterIterator text = event.getText();
			char key = text.current();
			TVP.EventManager.postInputEvent(new WindowEvents.OnKeyPressInputEvent(mNativeInstance, key), 0 );
			event.consume();
		}
	}

	@Override
	public void caretPositionChanged(InputMethodEvent event) {
	}

}
