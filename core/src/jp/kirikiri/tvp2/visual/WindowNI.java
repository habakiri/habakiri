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
import java.util.ArrayList;

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
import jp.kirikiri.tvp2.base.WindowEvents;
import jp.kirikiri.tvp2.msg.Message;
import jp.kirikiri.tvp2.utils.DebugClass;
import jp.kirikiri.tvp2.utils.ObjectList;
import jp.kirikiri.tvp2env.DrawTarget;
import jp.kirikiri.tvp2env.Font;
import jp.kirikiri.tvp2env.MenuItem;
import jp.kirikiri.tvp2env.NativeImageBuffer;
import jp.kirikiri.tvp2env.WindowForm;

public class WindowNI extends NativeInstanceObject {

	private ArrayList<VariantClosure> mObjectVector;
	private boolean mObjectVectorLocked;
	//protected Dispatch2 mOwner;
	protected WeakReference<Dispatch2> mOwner;

	protected Rect mWindowExposedRegion;
	//protected BaseBitmap mDrawBuffer;
	protected boolean mWindowUpdating; // window is in updating

	private DrawTarget mTargetWindow;
	protected NativeImageBuffer mOffscreenBuffer;
	private boolean mIsMainWindow;
	private boolean mDrawUpdateRectangle = false; // TODO debug

	//----- interface to video overlay object
	//protected ArrayList<BaseVideoOverlayNI> mVideoOverlay;
	//public void registerVideoOverlayObject(BaseVideoOverlayNI ovl);
	//public void unregisterVideoOverlayObject(BaseVideoOverlayNI ovl);

	// draw device 関係 start, オリジナルは分離しているけど、ここではくっつけてしまう
	protected int mPrimaryLayerManagerIndex; //!< プライマリレイヤマネージャ
	private ArrayList<LayerManager> mManagers; //!< レイヤマネージャの配列
	private Rect mDestRect; //!< 描画先位置

	private boolean mShouldShow;

	private Dispatch2 mMenuItemObject;

	private WindowForm mForm;

	public static final int
		mcsVisible = 0, // the mouse cursor is visible
		mcsTempHidden = 1, // the mouse cursor is temporarily hidden
		mcsHidden = 2; // the mouse cursor is invisible

	static private final int imDisable = 0,
	imClose=1, imOpen=2, imDontCare=3, imSAlpha=4, imAlpha=5, imHira=6, imSKata=7, imKata=8, imChinese=9, imSHanguel=10, imHanguel=11;


	ObjectList<VideoOverlayNI> mVideoOverlay;

	public Dispatch2 getOwner() { return mOwner.get(); }
	public Dispatch2 getWindowDispatch() {
		if( mOwner != null ) return mOwner.get();
		return null;
	}
	public WindowNI() {
		mObjectVector = new ArrayList<VariantClosure>();
		//mObjectVectorLocked = false;

		//mMenuItemObject = null;
		mWindowExposedRegion = new Rect();
		//mWindowExposedRegion.clear();
		//mWindowUpdating = false;

		mManagers = new ArrayList<LayerManager>();
		mDestRect = new Rect();
		mVideoOverlay = new ObjectList<VideoOverlayNI>();
	}

	public int construct( Variant[] param, Dispatch2 tjs_obj ) throws TJSException {
		mOwner = new WeakReference<Dispatch2>(tjs_obj); // no addref
		TVP.WindowList.registerWindowToList( (WindowNI) this );

		mForm = new WindowForm( this );
		return Error.S_OK;
	}

	public void invalidate() throws VariantException, TJSException {
		// remove from list
		TVP.WindowList.unregisterWindowToList( (WindowNI) this );

		// remove all events
		TVP.EventManager.cancelSourceEvents( mOwner.get() );
		TVP.EventManager.cancelInputEvents( this );

		// clear all window update events
		TVP.EventManager.removeWindowUpdate( this );

		// free DrawBuffer
		//if( mDrawBuffer != null ) mDrawBuffer = null;

		// disconnect all VideoOverlay objects
		try {
			mVideoOverlay.safeLock();
			int count = mVideoOverlay.getSafeLockedObjectCount();
			for( int i = 0; i < count; i++ ) {
				VideoOverlayNI item = mVideoOverlay.getSafeLockedObjectAt(i);
				if( item == null ) continue;
				item.disconnect();
			}
		} finally {
			mVideoOverlay.safeUnlock();
		}

		// invalidate all registered objects
		mObjectVectorLocked = true;
		final int count = mObjectVector.size();
		for( int i = 0; i < count; i++ ) {
			// invalidate each --
			// objects may throw an exception while invalidating,
			// but here we cannot care for them.
			VariantClosure clo = mObjectVector.get(i);
			try {
				clo.invalidate(0, null, null);
				clo = null;
			} catch( TJSException e ) {
				DebugClass.addLog(e.getMessage()); // just in case, log the error
			}
		}

		// invalidate menu object
		if( mMenuItemObject != null ) {
			mMenuItemObject.invalidate( 0, null, mMenuItemObject );
			mMenuItemObject = null;
		}

		// remove all events (again)
		//TVPCancelSourceEvents( mOwner );
		//TVPCancelInputEvents( this );

		// clear all window update events (again)
		//TVPRemoveWindowUpdate( this );

		// release draw device
		// setDrawDeviceObject( new Variant() );


		super.invalidate();
		/* NOTE: at this point, Owner is still non-null.
		   Caller must ensure that the Owner being null at the end of the
		   invalidate chain. */

		if( mForm != null ) {
			mForm.invalidateClose();
			mForm = null;
		}

		// remove all events
		TVP.EventManager.cancelSourceEvents(mOwner.get());
		TVP.EventManager.cancelInputEvents(this);

		// Set Owner null
		mOwner.clear();
	}

	public boolean isMainWindow() { return TVP.MainWindow == this; }

	public void fireOnActivate(boolean activate_or_deactivate) {
		// fire Window.onActivate or Window.onDeactivate event
		TVP.EventManager.postInputEvent( new WindowEvents.OnWindowActivateEvent(this, activate_or_deactivate), EventManager.EPT_REMOVE_POST );
		// to discard redundant events
	}
	/**
	 * 画面の向きが変更された時、イベントを発行する
	 */
	public void fireOnOrientationChanged() {
		// fire Window.onOrientationChanged
		TVP.EventManager.postInputEvent( new WindowEvents.OnOrientationChangedEvent(this), EventManager.EPT_REMOVE_POST );
	}

	public void onClose() {
		if(!canDeliverEvents()) return;
		Dispatch2 owner = mOwner.get();
		if( owner != null ) {
			Variant[] arg = new Variant[1];
			arg[0] = new Variant(1); // true
			TVP.EventManager.postEvent( owner, owner, "onCloseQuery", 0, EventManager.EPT_IMMEDIATE, arg);
		}
	}
	public void onResize() {
		if(!canDeliverEvents()) return;
		Dispatch2 owner = mOwner.get();
		if( owner != null ) {
			TVP.EventManager.postEvent( owner, owner, "onResize", 0, EventManager.EPT_IMMEDIATE, TJS.NULL_ARG );
		}
	}
	public void onClick( int x, int y ) throws TJSException {
		if(!canDeliverEvents()) return;
		Dispatch2 owner = mOwner.get();
		if( owner != null ) {
			Variant[] arg = new Variant[2];
			arg[0] = new Variant(x);
			arg[1] = new Variant(y);
			TVP.EventManager.postEvent( owner, owner, "onClick", 0, EventManager.EPT_IMMEDIATE, arg);
		}

		Point pos = new Point(x,y);
		if(!transformToPrimaryLayerManager(pos)) return;
		LayerManager manager = getLayerManagerAt(mPrimaryLayerManagerIndex);
		if(manager==null) return;
		manager.notifyClick(pos.x, pos.y);
	}
	public void onDoubleClick( int x, int y ) throws TJSException {
		if(!canDeliverEvents()) return;
		Dispatch2 owner = mOwner.get();
		if( owner != null ) {
			Variant[] arg = new Variant[2];
			arg[0] = new Variant(x);
			arg[1] = new Variant(y);
			TVP.EventManager.postEvent( owner, owner, "onDoubleClick", 0, EventManager.EPT_IMMEDIATE, arg);
		}

		Point pos = new Point(x,y);
		if(!transformToPrimaryLayerManager(pos)) return;
		LayerManager manager = getLayerManagerAt(mPrimaryLayerManagerIndex);
		if(manager==null) return;
		manager.notifyDoubleClick(pos.x, pos.y);
	}
	public void onMouseDown( int x, int y, int mb, int flags ) throws TJSException {
		if(!canDeliverEvents()) return;
		Dispatch2 owner = mOwner.get();
		if( owner != null ) {
			Variant[] arg = new Variant[4];
			arg[0] = new Variant(x);
			arg[1] = new Variant(y);
			arg[2] = new Variant(mb);
			arg[3] = new Variant(flags);
			TVP.EventManager.postEvent( owner, owner, "onMouseDown", 0, EventManager.EPT_IMMEDIATE, arg);
		}

		Point pos = new Point(x,y);
		if(!transformToPrimaryLayerManager(pos)) return;
		LayerManager manager = getLayerManagerAt(mPrimaryLayerManagerIndex);
		if(manager==null) return;
		manager.notifyMouseDown(pos.x, pos.y, mb, flags );
	}
	public void onMouseUp( int x, int y, int mb, int flags) throws TJSException {
		if(!canDeliverEvents()) return;
		Dispatch2 owner = mOwner.get();
		if( owner != null ) {
			Variant[] arg = new Variant[4];
			arg[0] = new Variant(x);
			arg[1] = new Variant(y);
			arg[2] = new Variant(mb);
			arg[3] = new Variant(flags);
			TVP.EventManager.postEvent( owner, owner, "onMouseUp", 0, EventManager.EPT_IMMEDIATE, arg);
		}

		Point pos = new Point(x,y);
		if(!transformToPrimaryLayerManager(pos)) return;
		LayerManager manager = getLayerManagerAt(mPrimaryLayerManagerIndex);
		if(manager==null) return;
		manager.notifyMouseUp(pos.x, pos.y, mb, flags );
	}
	public void onMouseMove( int x, int y, int flags ) throws TJSException {
		if(!canDeliverEvents()) return;
		Dispatch2 owner = mOwner.get();
		if( owner != null ) {
			Variant[] arg = new Variant[3];
			arg[0] = new Variant(x);
			arg[1] = new Variant(y);
			arg[2] = new Variant(flags);
			TVP.EventManager.postEvent( owner, owner, "onMouseMove", 0, EventManager.EPT_IMMEDIATE, arg);
		}

		Point pos = new Point(x,y);
		if(!transformToPrimaryLayerManager(pos)) return;
		LayerManager manager = getLayerManagerAt(mPrimaryLayerManagerIndex);
		if(manager==null) return;
		manager.notifyMouseMove(pos.x, pos.y, flags );
	}
	public void onReleaseCapture() {
		if(!canDeliverEvents()) return;

		LayerManager manager = getLayerManagerAt(mPrimaryLayerManagerIndex);
		if(manager==null) return;
		manager.releaseCapture();
	}
	public void onMouseOutOfWindow() throws TJSException {
		if(!canDeliverEvents()) return;

		LayerManager manager = getLayerManagerAt(mPrimaryLayerManagerIndex);
		if(manager==null) return;
		manager.notifyMouseOutOfWindow();
	}
	public void onMouseEnter() {
		if(!canDeliverEvents()) return;
		Dispatch2 owner = mOwner.get();
		if( owner != null ) {
			TVP.EventManager.postEvent( owner, owner, "onMouseEnter", 0, EventManager.EPT_IMMEDIATE, TJS.NULL_ARG );
		}
	}
	public void onMouseLeave() {
		if(!canDeliverEvents()) return;
		Dispatch2 owner = mOwner.get();
		if( owner != null ) {
			TVP.EventManager.postEvent( owner, owner, "onMouseLeave", 0, EventManager.EPT_IMMEDIATE, TJS.NULL_ARG );
		}
	}
	public void onKeyDown( int key, int shift ) throws TJSException {
		if(!canDeliverEvents()) return;
		Dispatch2 owner = mOwner.get();
		if( owner != null ) {
			Variant[] arg = new Variant[2];
			arg[0] = new Variant(key);
			arg[1] = new Variant(shift);
			TVP.EventManager.postEvent( owner, owner, "onKeyDown", 0, EventManager.EPT_IMMEDIATE, arg);
		}

		LayerManager manager = getLayerManagerAt(mPrimaryLayerManagerIndex);
		if(manager==null) return;
		manager.notifyKeyDown(key,shift);
	}
	public void onKeyUp( int key, int shift ) {
		if(!canDeliverEvents()) return;
		Dispatch2 owner = mOwner.get();
		if( owner != null ) {
			Variant[] arg = new Variant[2];
			arg[0] = new Variant(key);
			arg[1] = new Variant(shift);
			TVP.EventManager.postEvent( owner, owner, "onKeyUp", 0, EventManager.EPT_IMMEDIATE, arg);
		}

		LayerManager manager = getLayerManagerAt(mPrimaryLayerManagerIndex);
		if(manager==null) return;
		manager.notifyKeyUp(key,shift);
	}
	public void onKeyPress( char key ) {
		if(!canDeliverEvents()) return;
		Dispatch2 owner = mOwner.get();
		if( owner != null ) {
			Variant[] arg = new Variant[1];
			arg[0] = new Variant( String.valueOf(key) );
			TVP.EventManager.postEvent( owner, owner, "onKeyPress", 0, EventManager.EPT_IMMEDIATE, arg);
		}

		LayerManager manager = getLayerManagerAt(mPrimaryLayerManagerIndex);
		if(manager==null) return;
		manager.notifyKeyPress(key);
	}
	public void onFileDrop( final Variant array ) {
		if(!canDeliverEvents()) return;
		Dispatch2 owner = mOwner.get();
		if( owner != null ) {
			Variant[] arg = new Variant[1];
			arg[0] = array;
			TVP.EventManager.postEvent( owner, owner, "onFileDrop", 0, EventManager.EPT_IMMEDIATE, arg);
		}
	}
	public void onMouseWheel( int shift, int delta, int x, int y ) {
		if(!canDeliverEvents()) return;
		Dispatch2 owner = mOwner.get();
		if( owner != null ) {
			Variant[] arg = new Variant[4];
			arg[0] = new Variant(shift);
			arg[1] = new Variant(delta);
			arg[2] = new Variant(x);
			arg[3] = new Variant(y);
			TVP.EventManager.postEvent( owner, owner, "onMouseWheel", 0, EventManager.EPT_IMMEDIATE, arg);
		}

		Point pos = new Point(x,y);
		if(!transformToPrimaryLayerManager(pos)) return;
		LayerManager manager = getLayerManagerAt(mPrimaryLayerManagerIndex);
		if(manager==null) return;
		manager.notifyMouseWheel(shift,delta, pos.x, pos.y );
	}
	public void onPopupHide() {
		if(!canDeliverEvents()) return;
		Dispatch2 owner = mOwner.get();
		if( owner != null ) {
			TVP.EventManager.postEvent( owner, owner, "onPopupHide", 0, EventManager.EPT_IMMEDIATE, TJS.NULL_ARG );
		}
	}
	public void onActivate(boolean activate_or_deactivate) {
		if(!canDeliverEvents()) return;
		Dispatch2 owner = mOwner.get();
		if( owner != null ) {
			// re-check the window activate state
			if( getWindowActive() == activate_or_deactivate ) {
				if( activate_or_deactivate )
					TVP.EventManager.postEvent( owner, owner, "onActivate", 0, EventManager.EPT_IMMEDIATE, TJS.NULL_ARG );
				else
					TVP.EventManager.postEvent( owner, owner, "onDeactivate", 0, EventManager.EPT_IMMEDIATE, TJS.NULL_ARG );
			}
		}
	}
	public void onOrientationChanged() {
		if(!canDeliverEvents()) return;
		Dispatch2 owner = mOwner.get();
		if( owner != null ) {
			TVP.EventManager.postEvent( owner, owner, "onOrientationChanged", 0, EventManager.EPT_IMMEDIATE, TJS.NULL_ARG );
		}
	}

	public void clearInputEvents() {
		TVP.EventManager.cancelInputEvents(this);
	}

	public void postReleaseCaptureEvent() {
		TVP.EventManager.postInputEvent( new WindowEvents.OnReleaseCaptureInputEvent(this), 0 );
	}

	//----- layer managermant
	public void registerLayerManager( LayerManager manager ) {
		//addLayerManager(manager);
		mManagers.add( manager );
	}
	public void unregisterLayerManager( LayerManager manager ) throws TJSException {
		//removeLayerManager(manager);
		final int count = mManagers.size();
		for( int i = 0; i < count; i++ ) {
			LayerManager man = mManagers.get(i);
			if( manager == man ) {
				mManagers.remove(i);
				return;
			}
		}
		Message.throwExceptionMessage( Message.InternalError );
	}

	public void notifyWindowExposureToLayer( final Rect cliprect ) {
		requestInvalidation( cliprect);
	}
	/*
	public void notifyWindowExposureToLayer(const tTVPRect &cliprect)
	{
		DrawDevice->RequestInvalidation(cliprect);
	}
	*/

	public void notifyUpdateRegionFixed( final ComplexRect updaterects ) { // is called by layer manager
		// is called by layer manager
		beginUpdate(updaterects);
	}

	public void updateContent() throws VariantException, TJSException { // is called from event dispatcher
		// is called from event dispatcher
		update();
		show();

	 	endUpdate();
	}
	public void deliverDrawDeviceShow() throws TJSException {
		show();
	}
	public void beginUpdate( final ComplexRect rects ) {
		mWindowUpdating = true;
	}
	public void endUpdate() {
		mWindowUpdating = false;
	}
	public void requestUpdate() {
		// is called from primary layer

		// post update event to self
		TVP.EventManager.postWindowUpdate((WindowNI)this);
	}

	public void dumpPrimaryLayerStructure() {
		dumpLayerStructure();
	}

	private void dumpLayerStructure() {
		// すべての layer manager の DumpLayerStructure を呼ぶ
		final int count = mManagers.size();
		for( int i  = 0; i < count; i++ ) {
			LayerManager m = mManagers.get(i);
			m.dumpLayerStructure();
		}
	}
	public void recheckInputState() throws TJSException { // slow timer tick (about 1 sec interval, inaccurate)
		LayerManager manager = getLayerManagerAt(mPrimaryLayerManagerIndex);
		if(manager==null) return;

		manager.recheckInputState();
	}

	public void setShowUpdateRect( boolean b ) {
		mDrawUpdateRectangle = b;
	}


	//----- methods
	public void add( VariantClosure clo ) {
		if(mObjectVectorLocked) return;
		int idx = mObjectVector.indexOf(clo);
		if( idx < 0 ) {
			mObjectVector.add( clo );
		}
	}
	public void remove( VariantClosure clo ) {
		if(mObjectVectorLocked) return;
		int idx = mObjectVector.indexOf(clo);
		if( idx >= 0 ) {
			mObjectVector.remove( idx );
		}
	}

	//----- interface to menu object
	public Dispatch2 getMenuItemObject() throws TJSException {
		if( mMenuItemObject != null ) return mMenuItemObject;

		// create MenuItemObect
		Dispatch2 owner = mOwner.get();
		if(owner==null) Message.throwExceptionMessage( Message.InternalError, "BaseWindowNI.getMenuItemObject" );
		mMenuItemObject = TVP.createMenuItemObject(owner);
		return mMenuItemObject;
	}

	LayerNI getPrimaryLayer() {
		LayerManager manager = getLayerManagerAt(mPrimaryLayerManagerIndex);
		if( manager == null ) return null;
		return manager.getPrimaryLayer();
	}
	public LayerManager getLayerManagerAt( int index ) {
		if( mManagers.size() <= index ) return null;
		return mManagers.get(index);
	}
	public LayerNI getFocusedLayer() {
		LayerManager manager = getLayerManagerAt(mPrimaryLayerManagerIndex);
		if( manager == null ) return null;
		return manager.getFocusedLayer();
	}
	public void setFocusedLayer( LayerNI layer ) throws TJSException {
		LayerManager manager = getLayerManagerAt(mPrimaryLayerManagerIndex);
		if( manager == null ) return;
		manager.setFocusedLayer(layer);
	}
	private void requestInvalidation( final Rect rect ) {
		Point lt = new Point( rect.left, rect.top);
		Point rb = new Point( rect.right, rect.bottom);
		if(!transformToPrimaryLayerManager(lt)) return;
		if(!transformToPrimaryLayerManager(rb)) return;
		rb.x++; // 誤差の吸収(本当はもうちょっと厳密にやらないとならないがそれが問題になることはない)
		rb.y++;

		LayerManager manager = getLayerManagerAt(mPrimaryLayerManagerIndex);
		if( manager == null ) return;
		manager.requestInvalidation( new Rect(lt.x, lt.y, rb.x, rb.y ) );
	}

	private void show() throws TJSException {
		mTargetWindow.show();
		/*
		if( mOffscreenBuffer != null && mShouldShow ) {
			mTargetWindow.drawImage( mOffscreenBuffer );
			mShouldShow = false;
		}
		*/
	}
	private void update() throws VariantException, TJSException {
		// すべての layer manager の UpdateToDrawDevice を呼ぶ
		final int count = mManagers.size();
		for( int i = 0; i < count; i++ ) {
			LayerManager m = mManagers.get(i);
			m.updateToDrawDevice();
		}
	}
	public void setImeMode(LayerManager manager, int mode ) {
		LayerManager primary_manager = getLayerManagerAt(mPrimaryLayerManagerIndex);
		if( primary_manager == null ) return;
		if( primary_manager == manager ) {
			setImeMode(mode);
		}
	}
	public void resetImeMode( LayerManager manager ) {
		LayerManager primary_manager = getLayerManagerAt(mPrimaryLayerManagerIndex);
		if( primary_manager == null ) return;
		if( primary_manager == manager ) {
			resetImeMode();
		}
	}
	public void setAttentionPoint( LayerManager manager, LayerNI layer, Point pt ) {
		LayerManager primary_manager = getLayerManagerAt(mPrimaryLayerManagerIndex);
		if( primary_manager == null ) return;
		if( primary_manager == manager ) {
			if( transformFromPrimaryLayerManager(pt) )
				setAttentionPoint(layer, pt );
		}
	}
	private boolean transformToPrimaryLayerManager( Point pt ) {
		LayerManager manager = getLayerManagerAt(mPrimaryLayerManagerIndex);
		if( manager == null ) return false;

		// プライマリレイヤマネージャのプライマリレイヤのサイズを得る
		Size pl = new Size();
		if(!manager.getPrimaryLayerSize(pl)) return false;

		// x , y は DestRect の 0, 0 を原点とした座標として渡されてきている
		int w = mDestRect.width();
		int h = mDestRect.height();
		pt.x = w != 0 ? (pt.x * pl.width / w) : 0;
		pt.y = h != 0 ? (pt.y * pl.height / h) : 0;
		return true;
	}
	private boolean transformFromPrimaryLayerManager( Point pt ) {
		LayerManager manager = getLayerManagerAt(mPrimaryLayerManagerIndex);
		if( manager == null ) return false;

		// プライマリレイヤマネージャのプライマリレイヤのサイズを得る
		Size pl = new Size();
		if(!manager.getPrimaryLayerSize(pl)) return false;

		// x , y は DestRect の 0, 0 を原点とした座標として渡されてきている
		pt.x = pl.width != 0 ? (pt.x * mDestRect.width()  / pl.width) : 0;
		pt.y = pl.height != 0 ? (pt.y * mDestRect.height() / pl.height) : 0;

		return true;
	}
	public void disableAttentionPoint( LayerManager manager ) {
		LayerManager primary_manager = getLayerManagerAt(mPrimaryLayerManagerIndex);
		if( primary_manager == null ) return;
		if( primary_manager == manager ) {
			disableAttentionPoint();
		}
	}
	public void notifyLayerImageChange( LayerManager manager ) {
		LayerManager primary_manager = getLayerManagerAt(mPrimaryLayerManagerIndex);
		if( primary_manager == manager )
			requestUpdate();
	}
	public void setTargetWindow(DrawTarget target, boolean ismain) {
		// TVPInitPassThroughOptions();
		// destroyDrawer();
		mTargetWindow = target;
		mIsMainWindow = ismain;
		//createOffscreenImage();
	}
	public void notifyBitmapCompleted(LayerManager layerManager, int x, int y,
			NativeImageBuffer nativeImageBuffer, Rect cliprect, int type, int opacity) throws TJSException {
		// TODO 自動生成されたメソッド・スタブ
		// nativeImageBuffer をオフスクリーンに描画する
		// show で実際のdrawdeviceへ転送する
		/*
		if( mOffscreenBuffer != null ) {
			mShouldShow = true;
			mOffscreenBuffer.copyRect(x, y, nativeImageBuffer, cliprect );
		}
		*/
		mTargetWindow.drawImage(x, y, nativeImageBuffer, cliprect, type, opacity );

		if( mDrawUpdateRectangle ) {
			int rleft   = x;
			int rtop    = y;
			int rright  = rleft + cliprect.width();
			int rbottom = rtop  + cliprect.height();

			Point[] points = new Point[4];
			points[0] = new Point( rleft, rtop );
			points[1] = new Point( rright -1, rtop );
			points[2] = new Point( rright -1, rbottom -1 );
			points[3] = new Point( rleft, rbottom -1 );
			mTargetWindow.drawLines( points, 0xffff00 );
			points = null;
		}
	}
	public void windowReleaseCapture(LayerManager manager) {
		LayerManager primary_manager = getLayerManagerAt(mPrimaryLayerManagerIndex);
		if( primary_manager == null ) return;
		if( primary_manager == manager ) {
			windowReleaseCapture();
		}
	}
	public void notifyLayerResize(LayerManager manager) throws TJSException {
		LayerManager primary_manager = getLayerManagerAt(mPrimaryLayerManagerIndex);
		if(primary_manager == manager)
			notifySrcResize();
	}
	public void startBitmapCompletion(LayerManager manager) {
		/*
		EnsureDrawer();

		// この中で DestroyDrawer が呼ばれる可能性に注意すること
		if(Drawer) Drawer->StartBitmapCompletion();

		if(!Drawer)
		{
			// リトライする
			EnsureDrawer();
			if(Drawer) Drawer->StartBitmapCompletion();
		}
		*/
	}
	public void endBitmapCompletion(LayerManager manager) {
		//if(Drawer) Drawer->EndBitmapCompletion();
	}
	public void setDestRectangle( final Rect rect ) {
		mDestRect.set( rect );
	}
	public void setCursorPos(LayerManager manager, int x, int y) {
		LayerManager primary_manager = getLayerManagerAt(mPrimaryLayerManagerIndex);
		if(primary_manager==null) return;
		if(primary_manager == manager) {
			Point pos = new Point(x,y);
			if(transformFromPrimaryLayerManager(pos) )
				setCursorPos(pos.x, pos.y);
		}
	}
	public void setDefaultMouseCursor(LayerManager manager) {
		LayerManager primary_manager = getLayerManagerAt(mPrimaryLayerManagerIndex);
		if(primary_manager==null) return;
		if(primary_manager == manager) {
			setDefaultMouseCursor();
		}
	}
	public void setMouseCursor(LayerManager manager, int cursor) {
		LayerManager primary_manager = getLayerManagerAt(mPrimaryLayerManagerIndex);
		if(primary_manager==null) return;
		if(primary_manager == manager) {
			setMouseCursor(cursor);
		}
	}
	public void getCursorPos(LayerManager manager, Point pos) {
		LayerManager primary_manager = getLayerManagerAt(mPrimaryLayerManagerIndex);
		if(primary_manager==null) return;
		getCursorPos(pos);
		if( primary_manager != manager || !transformToPrimaryLayerManager(pos) ) {
			// プライマリレイヤマネージャ以外には座標 0,0 で渡しておく
			pos.x = 0;
			pos.y = 0;
		}
	}
	public void setHintText(LayerManager manager, String text) {
		LayerManager primary_manager = getLayerManagerAt(mPrimaryLayerManagerIndex);
		if(primary_manager==null) return;
		if(primary_manager == manager) {
			setHintText(text);
		}
	}

	// -----------------------------------------------------------------------------------------------------------------

	public boolean canDeliverEvents() {
		if( mForm == null ) return false;
		return getVisible() && mForm.getFormEnabled();
	}
	public WindowForm getForm() { return mForm; }
	public void notifyWindowClose() { mForm = null; }
	public void sendCloseMessage() {
		if( mForm != null ) mForm.sendCloseMessage();
	}
	public void tickBeat() throws TJSException {
		if( mForm != null ) mForm.tickBeat();
	}

	protected boolean getWindowActive() {
		if( mForm != null ) return mForm.getWindowActive();
		return false;
	}
	public void resetDrawDevice() throws TJSException {
		if( mForm != null ) mForm.resetDrawDevice();
	}
	private static final int
		etUnknown = 0,
		etOnKeyDown = 1,
		etOnKeyUp = 2,
		etOnKeyPress = 3;
	public void postInputEvent( final String name, Dispatch2 params ) throws TJSException {
		// posts input event
		if( mForm == null ) return;

		final String key_name = "key";
		final String shift_name = "shift";

		// check input event name
		int type;

		if( "onKeyDown".equals(name) )
			type = etOnKeyDown;
		else if( "onKeyUp".equals(name) )
			type = etOnKeyUp;
		else if( "onKeyPress".equals(name) )
			type = etOnKeyPress;
		else
			type = etUnknown;

		if( type == etUnknown )
			Message.throwExceptionMessage(Message.SpecifiedEventNameIsUnknown, name);

		if( type == etOnKeyDown || type == etOnKeyUp ) {
			// this needs params, "key" and "shift"
			if(params == null)
				Message.throwExceptionMessage( Message.SpecifiedEventNeedsParameter, name );

			int key = 0;
			int shift = 0;

			Variant val = new Variant();
			int hr = params.propGet(0, key_name, val, params );
			if( hr >= 0 )
				key = val.asInteger();
			else
				Message.throwExceptionMessage( Message.SpecifiedEventNeedsParameter2, name, "key" );

			hr = params.propGet( 0, shift_name, val, params );
			if( hr >= 0  )
				shift = val.asInteger();
			else
				Message.throwExceptionMessage( Message.SpecifiedEventNeedsParameter2, name, "shift" );

			char vcl_key = (char) key;
			if( type == etOnKeyDown )
				mForm.internalKeyDown(key, shift);
			else if( type == etOnKeyUp )
				mForm.onKeyUp( vcl_key, shift );
		} else if( type == etOnKeyPress ) {
			// this needs param, "key"
			if( params == null )
				Message.throwExceptionMessage( Message.SpecifiedEventNeedsParameter, name );
			int key = 0;

			Variant val = new Variant();
			int hr = params.propGet(0, key_name, val, params );
			if( hr >= 0 )
				key = val.asInteger();
			else
				Message.throwExceptionMessage( Message.SpecifiedEventNeedsParameter2, name, "key" );

			char vcl_key = (char) key;
			mForm.onKeyPress( vcl_key);
		}
	}

	public void notifySrcResize() throws TJSException { // is called from primary layer
		// is called from primary layer
		if(mWindowUpdating)
			Message.throwExceptionMessage(Message.InvalidMethodInUpdating);

		// is called from primary layer
		// ( or from WindowForm to reset paint box's size )
		Size s = new Size();
		getSrcSize( s );
		if( mForm != null )
			mForm.setPaintBoxSize( s.width, s.height );
	}
	void getSrcSize( Size s ) {
		s.width = 0;
		s.height = 0;
		LayerManager manager = getLayerManagerAt(mPrimaryLayerManagerIndex);
		if( manager == null ) return;
		if(!manager.getPrimaryLayerSize(s)) {
			s.width = 0;
			s.height = 0;
		}
	}
	public void setDefaultMouseCursor() {
		// set window mouse cursor to default
		if( mForm != null ) mForm.setDefaultMouseCursor();
	}
	public void setMouseCursor( int handle ) {
		// set window mouse cursor
		if( mForm != null ) mForm.setMouseCursor(handle);
	}

	public void getCursorPos( Point pt ) {
		// get cursor pos in primary layer's coordinates
		if( mForm != null ) mForm.getCursorPos( pt );
	}

	public void setCursorPos(int x, int y) {
		// set cursor pos in primar layer's coordinates
		if( mForm != null ) mForm.setCursorPos(x, y);
	}

	public void windowReleaseCapture() {
		// ::ReleaseCapture(); // Windows API
	}

	public void setHintText(String text) {
		// set hint text to window
		if( mForm != null ) mForm.setHintText(text);
	}
	public void setAttentionPoint( LayerNI layer, Point pt ) {
		// set attention point to window
		if( mForm != null ) {
			Font font = null;
			if( layer != null ) {
				/*
				BaseBitmap bmp = layer.getMainImage();
				if( bmp != null )
					font = bmp.getFontCanvas().getFont();
				*/
			}
			mForm.setAttentionPoint(pt.x, pt.y, font);
		}
	}

	public void disableAttentionPoint() {
		// disable attention point
		if( mForm != null ) mForm.disableAttentionPoint();
	}

	public void setImeMode(int mode) {
		// set ime mode
		if( mForm != null ) mForm.setImeMode(mode);
	}

	public void setDefaultImeMode( int mode ) {
		// set default ime mode
		if( mForm != null ) {
//			mForm.setDefaultImeMode( mode, LayerManager.getFocusedLayer() == null );
		}
	}

	public int getDefaultImeMode() {
		if( mForm != null ) return mForm.getDefaultImeMode();
		return imDisable;
	}
	public void resetImeMode() {
		// set default ime mode ( default mode is imDisable; IME is disabled )
		if( mForm != null ) mForm.resetImeMode();
	}

	public MenuItem getRootMenuItem() {
		if( mForm == null ) return null;
		return mForm.getMainMenu();
	}

	public void setMenuBarVisible( boolean b ) throws TJSException {
		if( mForm == null ) return;
		if( mForm.getFullScreenMode() )
			Message.throwExceptionMessage( Message.InvalidPropertyInFullScreen );
		mForm.setMenuBarVisible(b);
	}

	public boolean getMenuBarVisible() {
		if( mForm == null ) return false;
		return mForm.getMenuBarVisible();
	}

	/*
	int getMenuOwnerWindowHandle() {
		if( mForm == null ) return null;
		return mForm.getMenuOwnerWindowHandle();
	}

	int getSurfaceWindowHandle() {
		if( mForm == null ) return null;
		return mForm.getSurfaceWindowHandle();
	}
	*/

	public void zoomRectangle( Rect rect ) {
		if( mForm == null ) return;
		mForm.zoomRectangle( rect );
	}

	/*
	int getWindowHandle( int ofsx, int ofsy) {
		if( mForm == null ) return null;
		return mForm.getWindowHandle(ofsx, ofsy);
	}
	*/

	public void readjustVideoRect() {
		if( mForm == null ) return;

		/* TODO
		// re-adjust video rectangle.
		// this reconnects owner window and video offsets.

		tObjectListSafeLockHolder<BaseVideoOverlayNI> holder(VideoOverlay);
		int count = VideoOverlay.GetSafeLockedObjectCount();

		for( int i = 0; i < count; i++ ) {
			VideoOverlayNI item = (VideoOverlayNI)VideoOverlay.GetSafeLockedObjectAt(i);
			if( item != null ) item.resetOverlayParams();
		}
		*/
	}

	public void windowMoved() {
		// inform video overlays that the window has moved.
		// video overlays typically owns DirectDraw surface which is not a part of
		// normal window systems and does not matter where the owner window is.
		// so we must inform window moving to overlay window.

		/* TODO
		tObjectListSafeLockHolder<BaseVideoOverlayNI> holder(VideoOverlay);
		int count = VideoOverlay.GetSafeLockedObjectCount();
		for( int i = 0; i < count; i++ ) {
			VideoOverlayNI item = (VideoOverlayNI)VideoOverlay.GetSafeLockedObjectAt(i);
			if( item != null ) item.setRectangleToVideoOverlay();
		}
		*/
	}

	public void detachVideoOverlay() {
		// detach video overlay window
		// this is done before the window is being fullscreened or un-fullscreened.
		/* TODO
		tObjectListSafeLockHolder<BaseVideoOverlayNI> holder(VideoOverlay);
		int count = VideoOverlay.GetSafeLockedObjectCount();
		for( int i = 0; i < count; i++) {
			VideoOverlayNI item = (VideoOverlayNI)VideoOverlay.GetSafeLockedObjectAt(i);
			if( item != null ) item.detachVideoOverlay();
		}
		*/
	}

	/*
	public int getWindowHandleForPlugin() {
		if( mForm == null ) return null;
		return mForm.getWindowHandleForPlugin();
	}
	*/

	/*
	public void registerWindowMessageReceiver( int mode, Object proc, final Object userdata ) {
		if( mForm == null ) return;
		mForm.registerWindowMessageReceiver(mode, proc, userdata);
	}
	*/

	public void close() {
		if( mForm != null ) mForm.close();
	}
	/*
	public void onCloseQueryCalled( boolean b ) {
		if( mForm != null ) mForm.onCloseQueryCalled(b);
	}

*/
	public void beginMove() throws TJSException {
		if( mForm != null ) {
			if( mForm.getFullScreenMode() )
				Message.throwExceptionMessage( Message.InvalidMethodInFullScreen );
			mForm.beginMove();
		}
	}

	public void bringToFront() {
		if( mForm != null ) mForm.bringToFront();
	}

	public void update( int type ) throws VariantException, TJSException {
		if( mForm != null ) mForm.updateWindow(type);
	}

	public void showModal() throws TJSException {
		if( mForm != null ) {
			if( mForm.getFullScreenMode() )
				Message.throwExceptionMessage( Message.InvalidMethodInFullScreen );

			TVP.WindowList.clearAllWindowInputEvents();
			// cancel all input events that can cause delayed operation

			mForm.showWindowAsModal();
		}
	}

	public void hideMouseCursor() {
		if( mForm != null ) mForm.hideMouseCursor();
	}

	public boolean getVisible() {
		if( mForm == null ) return false;
		return mForm.getVisible();
	}

	public void setVisible(boolean s) throws TJSException {
		if( mForm != null ) {
			if( mForm.getFullScreenMode())
				Message.throwExceptionMessage( Message.InvalidPropertyInFullScreen );
			mForm.setVisible(s);
		}
	}


	public String getCaption() {
		if( mForm != null ) return mForm.getCaption();
		else return null;
	}

	public void setCaption( final String v ) {
		if( mForm != null ) mForm.setCaption( v );
	}

	public void setWidth( int w ) throws TJSException {
		if( mForm != null ) {
			if( mForm.getFullScreenMode() )
				Message.throwExceptionMessage( Message.InvalidPropertyInFullScreen );
			mForm.setWidth( w );
		}
	}

	public int getWidth() {
		if( mForm == null ) return 0;
		return mForm.getWidth();
	}

	public void setHeight( int h ) throws TJSException {
		if( mForm != null ) {
			if( mForm.getFullScreenMode() )
				Message.throwExceptionMessage( Message.InvalidPropertyInFullScreen );
			mForm.setHeight( h );
		}
	}

	public int getHeight() {
		if( mForm == null ) return 0;
		return mForm.getHeight();
	}

	public void setLeft( int l ) throws TJSException {
		if( mForm != null ) {
			if( mForm.getFullScreenMode() )
				Message.throwExceptionMessage( Message.InvalidPropertyInFullScreen );
			mForm.setLeft( l );
		}
	}

	public int getLeft() {
		if( mForm == null ) return 0;
		return mForm.getLeft();
	}

	public void setTop( int t ) throws TJSException {
		if( mForm != null ) {
			if( mForm.getFullScreenMode() )
				Message.throwExceptionMessage( Message.InvalidPropertyInFullScreen );
			mForm.setTop( t );
		}
	}

	public int getTop() {
		if( mForm == null ) return 0;
		return mForm.getTop();
	}

	public void setSize( int w, int h ) throws TJSException {
		if( mForm != null ) {
			if( mForm.getFullScreenMode() )
				Message.throwExceptionMessage( Message.InvalidPropertyInFullScreen );
			mForm.setSize( w, h );
		}
	}

	public void setMinWidth( int v ) throws TJSException {
		if( mForm != null ) {
			if( mForm.getFullScreenMode() )
				Message.throwExceptionMessage( Message.InvalidPropertyInFullScreen );
			mForm.setMinWidth( v );
		}
	}

	public int getMinWidth() {
		if( mForm != null ) return mForm.getMinWidth();
		else return 0;
	}

	public void setMinHeight(int v) throws TJSException {
		if( mForm != null ) {
			if( mForm.getFullScreenMode() )
				Message.throwExceptionMessage( Message.InvalidPropertyInFullScreen );
			mForm.setMinHeight( v );
		}
	}

	public int getMinHeight() {
		if( mForm != null ) return mForm.getMinHeight();
		else return 0;
	}

	public void setMinSize(int w, int h) throws TJSException {
		if( mForm != null ) {
			if( mForm.getFullScreenMode() )
				Message.throwExceptionMessage( Message.InvalidPropertyInFullScreen );
			mForm.setMinSize( w, h );
		}
	}

	public void setMaxWidth(int v) throws TJSException {
		if( mForm != null ) {
			if( mForm.getFullScreenMode() )
				Message.throwExceptionMessage( Message.InvalidPropertyInFullScreen );
			mForm.setMaxWidth( v );
		}
	}

	public int getMaxWidth() {
		if( mForm != null ) return mForm.getMaxWidth();
		else return 0;
	}

	public void setMaxHeight(int v) throws TJSException {
		if( mForm != null ) {
			if( mForm.getFullScreenMode() )
				Message.throwExceptionMessage( Message.InvalidPropertyInFullScreen );
			mForm.setMaxHeight( v );
		}
	}

	public int getMaxHeight() {
		if( mForm != null ) return mForm.getMaxHeight();
		else return 0;
	}

	public void setMaxSize(int w, int h) throws TJSException {
		if( mForm != null ) {
			if( mForm.getFullScreenMode() )
				Message.throwExceptionMessage( Message.InvalidPropertyInFullScreen );
			mForm.setMaxSize( w, h );
		}
	}

	public void setPosition( int l, int t) throws TJSException {
		if( mForm != null ) {
			if( mForm.getFullScreenMode() )
				Message.throwExceptionMessage( Message.InvalidPropertyInFullScreen );
			mForm.setPosition( l, t );
		}
	}

	public void setLayerLeft( int l ) {
		if( mForm != null ) mForm.setLayerLeft(l);
	}

	public int getLayerLeft() {
		if( mForm == null ) return 0;
		return mForm.getLayerLeft();
	}

	public void setLayerTop( int t ) {
		if( mForm != null ) mForm.setLayerTop(t);
	}

	public int getLayerTop() {
		if( mForm == null ) return 0;
		return mForm.getLayerTop();
	}

	public void setLayerPosition( int l, int t ) {
		if( mForm != null ) mForm.setLayerPosition(l, t);
	}

	public void setInnerSunken( boolean b ) throws TJSException {
		if( mForm != null ) {
			if( mForm.getFullScreenMode() )
				Message.throwExceptionMessage( Message.InvalidPropertyInFullScreen );
			mForm.setInnerSunken( b );
		}
	}

	public boolean getInnerSunken() {
		if( mForm == null ) return true;
		return mForm.getInnerSunken();
	}

	public void setInnerWidth( int w ) throws TJSException {
		if( mForm != null ) {
			if( mForm.getFullScreenMode() )
				Message.throwExceptionMessage( Message.InvalidPropertyInFullScreen );
			mForm.setInnerWidth( w );
		}
	}

	public int getInnerWidth() {
		if( mForm == null ) return 0;
		return mForm.getInnerWidth();
	}

	public void setInnerHeight( int h ) throws TJSException {
		if( mForm != null ) {
			if( mForm.getFullScreenMode() )
				Message.throwExceptionMessage( Message.InvalidPropertyInFullScreen );
			mForm.setInnerHeight( h );
		}
	}

	public int getInnerHeight() {
		if( mForm == null ) return 0;
		return mForm.getInnerHeight();
	}

	public void setInnerSize( int w, int h ) throws TJSException {
		if( mForm != null ) {
			if( mForm.getFullScreenMode() )
				Message.throwExceptionMessage( Message.InvalidPropertyInFullScreen );
			mForm.setInnerSize(w, h);
		}
	}
	public void setBorderStyle( int st ) throws TJSException {
		if( mForm != null ) {
			if( mForm.getFullScreenMode() )
				Message.throwExceptionMessage( Message.InvalidPropertyInFullScreen );
			mForm.setBorderStyle(st);
		}
	}

	public int getBorderStyle() {
		if( mForm == null ) return 0;
		return mForm.getBorderStyle();
	}
	public void setStayOnTop( boolean b ) {
		if( mForm == null ) return;
		mForm.setStayOnTop(b);
	}

	public boolean getStayOnTop() {
		if( mForm == null ) return false;
		return mForm.getStayOnTop();
	}

	public void setShowScrollBars(boolean b) {
		if( mForm != null ) mForm.setShowScrollBars(b);
	}

	public boolean getShowScrollBars() {
		if( mForm == null ) return true;
		return mForm.getShowScrollBars();
	}

	public void setFullScreen(boolean b) {
		if( mForm == null ) return;
		mForm.setFullScreenMode(b);
	}

	public boolean getFullScreen() {
		if( mForm == null ) return false;
		return mForm.getFullScreenMode();
	}

	public void setUseMouseKey(boolean b) {
		if(mForm == null) return;
		mForm.setUseMouseKey(b);
	}

	public boolean getUseMouseKey() {
		if( mForm == null ) return false;
		return mForm.getUseMouseKey();
	}

	public void setTrapKey(boolean b) {
		if( mForm == null ) return;
		mForm.setTrapKey(b);
	}

	public boolean getTrapKey() {
		if( mForm == null ) return false;
		return mForm.getTrapKey();
	}

	public void setMaskRegion( int threshold ) {
		if( mForm == null ) return;

		/* TODO
		if( mDrawDevice == null )
			Message.throwExceptionMessage(Message.WindowHasNoLayer);
		BaseLayerNI lay = mDrawDevice.getPrimaryLayer();
		if( lay == null )
			Message.throwExceptionMessage( Message.WindowHasNoLayer );
		mForm.setMaskRegion(((LayerNI)lay).createMaskRgn( threshold ) );
		*/
	}


	public void removeMaskRegion() {
		if( mForm == null ) return;
		mForm.removeMaskRegion();
	}

	public void setMouseCursorState( int mcs ) {
		if( mForm == null ) return;
		mForm.setMouseCursorState(mcs);
	}

	public int getMouseCursorState() {
		if( mForm == null ) return mcsVisible;
		return mForm.getMouseCursorState();
	}

	public void setFocusable(boolean b) {
		if( mForm == null ) return;
		mForm.setFocusable(b);
	}

	public boolean getFocusable() {
		if( mForm != null ) return true;
		return mForm.getFocusable();
	}

	public void setZoom( int numer, int denom ) {
		if( mForm == null ) return;
		mForm.setZoom(numer, denom);
	}
	public void setZoomNumer( int n ){
		if( mForm == null ) return;
		mForm.setZoomNumer(n);
	}

	public int getZoomNumer() {
		if( mForm == null ) return 1;
		return mForm.getZoomNumer();
	}

	public void setZoomDenom(int n) {
		if( mForm == null ) return;
		mForm.setZoomDenom(n);
	}
	public int getZoomDenom() {
		if( mForm == null ) return 1;
		return mForm.getZoomDenom();
	}
	public static void makeFullScreenModeCandidates(ScreenMode preferred,
			int mode, int zoom_mode, ArrayList<ScreenModeCandidate> candidates) {
		WindowForm.makeFullScreenModeCandidates(preferred, mode, zoom_mode, candidates);
	}

	public void registerWindowMessageReceiver(int asInteger, Dispatch2 asObject, Dispatch2 asObject2) {
		// TODO 自動生成されたメソッド・スタブ
	}

	void createOffscreenImage() {
		/* TODO 要らないかな？
		if( mForm != null ) {
			mOffscreenBuffer = mForm.createOffscreenImage();
		}
		*/
	}

	public void onCloseQueryCalled(boolean b) throws VariantException, TJSException {
		if(mForm!=null) mForm.onCloseQueryCalled(b);
	}
	public void showMenu() {
		if( mForm != null ) mForm.showMenu();
	}
	public void registerVideoOverlayObject( VideoOverlayNI overlay ) {
		mVideoOverlay.add( overlay );
		overlay.setTargetWindow( mForm );
	}
	public void unregisterVideoOverlayObject( VideoOverlayNI overlay ) {
		mVideoOverlay.remove( overlay );
		overlay.setTargetWindow( null );
	}
}
