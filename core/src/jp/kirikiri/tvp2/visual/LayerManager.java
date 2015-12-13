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

import java.util.ArrayList;

import jp.kirikiri.tjs2.Holder;
import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tjs2.VariantException;
import jp.kirikiri.tvp2.msg.Message;
import jp.kirikiri.tvp2.visual.Drawable;
import jp.kirikiri.tvp2env.WindowForm;

public class LayerManager implements Drawable {
	private static final int UPDATE_UNITE_LIMIT = 300;

	private WindowNI mWindow;

	//void * DrawDeviceData; //!< draw device specific information

	private BaseBitmap mDrawBuffer;
	private int mDesiredLayerType; //!< desired layer type by the draw device for this layer manager

	private LayerNI mCaptureOwner;
	private LayerNI mLastMouseMoveSent;

	private ArrayList<LayerNI> mModalLayerVector; 		// pointer to modal layer vector
	private LayerNI mFocusedLayer; // pointer to current focused layer
	private LayerNI mPrimary; // primary layer
	private boolean mOverallOrderIndexValid;
	private ArrayList<LayerNI> mAllNodes;
		// hold overall nodes;
		// use GetAllNodes to retrieve the newest information of this
	private ComplexRect mUpdateRegion; // window update region

	private boolean mFocusChangeLock;

	private boolean mVisualStateChanged;
		// flag for visual
		// state changing ( see tTJSNI_BaseLaye::NotifyChildrenVisualStateChanged)

	private int mLastMouseMoveX;
	private int mLastMouseMoveY;

	private boolean mReleaseCaptureCalled;

	private boolean mInNotifyingHintOrCursorChange;
	private int mEnabledWorkRefCount;

	public LayerManager( WindowNI window ) {

		mModalLayerVector = new ArrayList<LayerNI>();
		mAllNodes = new ArrayList<LayerNI>();
		mUpdateRegion = new ComplexRect();

		mWindow = window;
		//mDrawBuffer = null;
		mDesiredLayerType = LayerType.ltOpaque;

		//mCaptureOwner = null;
		//mLastMouseMoveSent = null;
		//mPrimary = null;
		//mFocusedLayer = null;
		//mOverallOrderIndexValid = false;
		//mEnabledWorkRefCount = 0;
		//mFocusChangeLock = false;
		mVisualStateChanged = true;
		mLastMouseMoveX = -1;
		mLastMouseMoveY = -1;
		//mInNotifyingHintOrCursorChange = false;
	}
	public void registerSelfToWindow() {
		mWindow.registerLayerManager(this);
	}

	public void unregisterSelfFromWindow() throws TJSException {
		mWindow.unregisterLayerManager(this);
	}
	public void notifyVisualStateChanged() { mVisualStateChanged = true; }
	@Override
	public BaseBitmap getDrawTargetBitmap(Rect rect, Rect cliprect) throws TJSException {
		// retrieve draw target bitmap
		int w = rect.width();
		int h = rect.height();

		if( mDrawBuffer == null ) {
			// create draw buffer
			mDrawBuffer = new BaseBitmap(w, h, 32);
		} else {
			int bw = mDrawBuffer.getWidth();
			int bh = mDrawBuffer.getHeight();
			if( bw < w || bh  < h ) {
				// insufficient size; resize the draw buffer
				int neww = bw > w ? bw:w;
				neww += (neww & 1); // align to even
				mDrawBuffer.setSize(neww, bh > h ? bh:h);
			}
		}
		cliprect.left = 0;
		cliprect.top = 0;
		cliprect.right = w;
		cliprect.bottom = h;
		return mDrawBuffer;
	}

	@Override
	public int getTargetLayerType() {
		return mDesiredLayerType;
	}
	public WindowNI getWindow() { return mWindow; }

	public ComplexRect getUpdateRegionForCompletion() { return mUpdateRegion; }

	@Override
	public void drawCompleted(Rect destrect, BaseBitmap bmp, Rect cliprect, int type, int opacity) throws TJSException {
		mWindow.notifyBitmapCompleted(this, destrect.left, destrect.top, bmp.getNativeImageBuffer(), cliprect, type, opacity);
	}
	public void attachPrimary( LayerNI pri ) throws TJSException {
		// attach primary layer to the manager
		detachPrimary();
		if( mPrimary == null ) {
			mPrimary = pri;
			mEnabledWorkRefCount = 0;
			mOverallOrderIndexValid = false;
			mUpdateRegion.clear();
			pri.setVisible(true);
			pri.setOpacity(255);
		}
	}
	public void detachPrimary() throws TJSException {
		// detach primary layer from the manager
		if( mPrimary != null ) {
			setFocusTo(null);
			releaseCapture();
			forceMouseLeave();
			notifyPart(mPrimary);
			mPrimary = null;
		}
	}
	public LayerNI getPrimaryLayer() { return mPrimary; }
	public boolean isPrimaryLayerAttached() { return mPrimary != null; }
	public boolean getPrimaryLayerSize( Size c ) {
		if( isPrimaryLayerAttached() ) {
			c.width = mPrimary.getWidth();
			c.height = mPrimary.getHeight();
			return true;
		} else {
			return false;
		}
	}

	public void notifyPart( LayerNI lay ) throws TJSException {
		// notifies layer parting from its parent
		invalidateOverallIndex();
		blurTree(lay);
		releaseCaptureFromTree(lay);
	}

	public void invalidateOverallIndex() {
		mOverallOrderIndexValid = false;
	}

	public void recreateOverallOrderIndex() {
		// recreate overall order index
		if( !mOverallOrderIndexValid ) {
			int[] index = {0};
			mAllNodes.clear();
			if(mPrimary != null) mPrimary.recreateOverallOrderIndex(index, mAllNodes);
			mOverallOrderIndexValid = true;
		}
	}
	public ArrayList<LayerNI> getAllNodes() {
		if(!mOverallOrderIndexValid) recreateOverallOrderIndex();
		return mAllNodes;
	}

	public void queryUpdateExcludeRect() {
		if(!mVisualStateChanged) return;
		Rect r = new Rect();
		r.clear();
		if( mPrimary != null ) mPrimary.queryUpdateExcludeRect(r, true);
		mVisualStateChanged = false;
	}
	public void notifyMouseCursorChange( LayerNI layer, int cursor ) throws TJSException {
		if(mInNotifyingHintOrCursorChange) return;

		mInNotifyingHintOrCursorChange = true;
		try {
			LayerNI l;
			if(mCaptureOwner != null)
				l = mCaptureOwner;
			else
				l = getMostFrontChildAt(mLastMouseMoveX, mLastMouseMoveY);

			if(l == layer) setMouseCursor(cursor);
		} finally {
			mInNotifyingHintOrCursorChange = false;
		}
	}

	public void setMouseCursor( int cursor ) {
		if(mWindow == null ) return;
		if(cursor == 0)
			mWindow.setDefaultMouseCursor(this);
		else
			mWindow.setMouseCursor(this, cursor);
	}

	public void getCursorPos( Point pos ) {
		if( mWindow == null ) return;
		mWindow.getCursorPos(this, pos );
	}
	public void setCursorPos( int x, int y) {
		if( mWindow == null ) return;
		mWindow.setCursorPos(this, x, y);
	}

	public void notifyHintChange( LayerNI layer, final String hint ) throws TJSException {
		if(mInNotifyingHintOrCursorChange) return;
		mInNotifyingHintOrCursorChange = true;
		try {
			LayerNI l;

			if( mCaptureOwner != null )
				l = mCaptureOwner;
			else
				l = getMostFrontChildAt( mLastMouseMoveX, mLastMouseMoveY );
			if(l == layer) setHint(hint);
		} finally {
			mInNotifyingHintOrCursorChange = false;
		}
	}

	public void setHint( final String hint ) {
		if( mWindow == null ) return;
		mWindow.setHintText( this, hint );
	}

	public void notifyLayerResize() throws TJSException {
		// notifies layer resizing to the window
		if( mWindow == null ) return;
		mWindow.notifyLayerResize(this);
	}

	public void notifyWindowInvalidation() {
		// notifies layer surface is invalidated and should be transfered to window.
		if( mWindow == null ) return;
		mWindow.notifyLayerImageChange(this);
	}
	public void setWindow( WindowNI window ) {
		// sets window
		mWindow = window;
	}

	public void notifyResizeFromWindow( int w, int h ) throws TJSException {
		// is called by the owner window, notifies windows's client area size
		// has been changed.
		// does not be called if owner window's "autoResize" property is false.

		// currently this function is not used.

		if( mPrimary != null ) mPrimary.internalSetSize(w, h);
	}
	public LayerNI getMostFrontChildAt( int x, int y ) throws TJSException {
		return getMostFrontChildAt(x,y,null,false);
	}
	public LayerNI getMostFrontChildAt( int x, int y, LayerNI except, boolean get_disabled ) throws TJSException {
		// return most front layer at given point.
		// this does checking of layer's visibility.
		// x and y are given in primary layer's coordinates.
		if( mPrimary == null ) return null;

		Holder<LayerNI> lay = new Holder<LayerNI>(null);
		mPrimary.getMostFrontChildAt( x, y, lay, except, get_disabled);
		return lay.mValue;
	}
	public void primaryClick( int x, int y ) throws TJSException {
		LayerNI l = getMostFrontChildAt(x, y);
		if( l != null && mCaptureOwner == l)
		{
			// TODO
			int[] ax = {x};
			int[] ay = {y};
			l.fromPrimaryCoordinates(ax, ay);
			l.fireClick(ax[0], ay[0]);
		}
	}

	public void primaryDoubleClick( int x, int y ) throws TJSException {
		LayerNI l = getMostFrontChildAt(x, y);
		if( l != null ) {
			// TODO
			int[] ax = {x};
			int[] ay = {y};
			l.fromPrimaryCoordinates(ax, ay);
			l.fireDoubleClick(ax[0], ay[0]);
		}
	}

	public void primaryMouseDown( int x, int y, int mb, int flags ) throws TJSException {
		primaryMouseMove(x, y, flags);
		LayerNI l = mCaptureOwner != null ? mCaptureOwner : getMostFrontChildAt(x, y);
		if( l != null ) {
			// TODO
			int[] ax = {x};
			int[] ay = {y};
			l.fromPrimaryCoordinates(ax, ay);
			mReleaseCaptureCalled = false;
			l.fireMouseDown(ax[0], ay[0], mb, flags);
			boolean no_capture = mReleaseCaptureCalled;
			if( mCaptureOwner != l ){
				releaseCapture();
				if( !no_capture ) {
					mCaptureOwner = l;
					//if(mCaptureOwner.mOwner) mCaptureOwner.mOwner.addRef(); // addref TJS object
				}
			}
			setHint("");
		} else {
			releaseCapture();
		}
	}

	void primaryMouseUp( int x, int y, int mb, int flags) throws TJSException {
		LayerNI l;
		if( mCaptureOwner != null)
			l = mCaptureOwner;
		else
			l = getMostFrontChildAt(x, y);

		if(l!=null) {
			int orig_x = x, orig_y = y;

			// TODO
			int[] ax = {x};
			int[] ay = {y};
			l.fromPrimaryCoordinates(ax, ay);
			l.fireMouseUp(ax[0], ay[0], mb, flags);

			if( !WindowForm.isAnyMouseButtonPressedInShiftStateFlags(flags) ) {
				releaseCapture();
				primaryMouseMove(orig_x, orig_y, flags); // force recheck current under-cursor layer
			}
		}
	}

	void primaryMouseMove(int x, int y, int flags) throws TJSException {
		boolean poschanged = (mLastMouseMoveX != x || mLastMouseMoveY != y);
		mLastMouseMoveX = x;
		mLastMouseMoveY = y;

		LayerNI l;

		if( mCaptureOwner != null ) {
			l = mCaptureOwner;
		} else {
			l = getMostFrontChildAt(x, y);
		}

		// enter/leave event
		if(mLastMouseMoveSent != l){
			if(mLastMouseMoveSent!=null) mLastMouseMoveSent.fireMouseLeave();

			// recheck l because the layer may become invalid during
			// FireMouseLeave call.
			if(mCaptureOwner!=null)
				l = mCaptureOwner;
			else
				l = getMostFrontChildAt(x, y);

			if(l!=null){
				mInNotifyingHintOrCursorChange = true;
				try {
					LayerNI ll;

					l.fireMouseEnter();

					// recheck l because the layer may become invalid during
					// FireMouseEnter call.
					if(mCaptureOwner!=null)
						ll = mCaptureOwner;
					else
						ll = getMostFrontChildAt(x, y);

					if(l != ll) {
						l.fireMouseLeave();
						l = ll;
						if(l!=null) l.fireMouseEnter();
					}

					// note: rechecking is done only once to avoid infinite loop

					if(l!=null) l.setCurrentCursorToWindow();
					if(l!=null) l.setCurrentHintToWindow();
				} finally {
					mInNotifyingHintOrCursorChange = false;
				}
			}

			if(l==null) {
				setMouseCursor(0);
				setHint("");
			}
		}

		if(mLastMouseMoveSent != l) {
			if(mLastMouseMoveSent!=null){
				//LayerNI lay = mLastMouseMoveSent;
				mLastMouseMoveSent = null;
				//if(lay.mOwner!=null) lay->Owner->Release();
			}

			mLastMouseMoveSent = l;

			/*
			if(mLastMouseMoveSent!=null) {
				//if( mLastMouseMoveSent.mOwner != null ) mLastMouseMoveSent.mOwner->AddRef();
			}
			*/
		}

		if(l!=null) {
			if(poschanged) {
				// TODO もうちょっと軽減した方が良いような
				int[] ax = {x};
				int[] ay = {y};
				l.fromPrimaryCoordinates(ax, ay);
				l.fireMouseMove(ax[0], ay[0], flags);
			}
		} else {
			// no layer to send the event
		}
	}
	private void forceMouseLeave() {
		if( mLastMouseMoveSent != null ) {
			LayerNI lay = mLastMouseMoveSent;
			mLastMouseMoveSent = null;
			lay.fireMouseLeave();
			//if(lay->Owner) lay->Owner->Release();
		}
	}
	void forceMouseRecheck() throws TJSException {
		primaryMouseMove(mLastMouseMoveX, mLastMouseMoveY, 0);
	}
	void mouseOutOfWindow() throws TJSException {
		// notifys that the mouse cursor goes outside of the window.
		if(mCaptureOwner==null)
			primaryMouseMove(-1, -1, 0); // force mouse cursor out of the all
	}
	void leaveMouseFromTree( LayerNI root ) {
		// force to leave mouse
		if( mLastMouseMoveSent != null ) {
			if( mLastMouseMoveSent.isAncestorOrSelf(root) ) {
				LayerNI lay = mLastMouseMoveSent;
				mLastMouseMoveSent = null;
				lay.fireMouseLeave();
				//if(lay->Owner) lay->Owner->Release(); TODO
			}
		}
	}
	public void releaseCapture() {
		// release capture state
		mReleaseCaptureCalled = true;
		if( mCaptureOwner != null ) {
			//LayerNI lay = mCaptureOwner;
			mCaptureOwner = null;
			//if( lay.getOwner() != null ) mOwner.release();
				// release TJS object
			mWindow.windowReleaseCapture(this);
		}
	}
	private void releaseCaptureFromTree( LayerNI layer ) {
		// Release capture state, if the capture object is descendant of
		// 'layer' or 'layer' itself.
		if( mCaptureOwner != null )
		{
			if( mCaptureOwner.isAncestorOrSelf(layer) ) {
				releaseCapture();
			}
		}
	}
	public final boolean blurTree( LayerNI root ) throws TJSException {
		// (primary only) remove focus from "root"
		removeTreeModalState(root);
		leaveMouseFromTree(root);

		if( mFocusedLayer == null ) return false;

		if( mFocusedLayer.isAncestorOrSelf(root) ) return false;
			// root is not ancestor of current focused layer

		LayerNI next = root.getNextFocusable();

		if(next != mFocusedLayer)
			setFocusTo(next, true); // focus to root's next focusable layer
		else
			setFocusTo(null, true);

		return true;
	}
	public LayerNI getFocusedLayer() { return mFocusedLayer; }
	public void setFocusedLayer( LayerNI layer) throws TJSException { setFocusTo(layer, false); }

	public final void checkTreeFocusableState( LayerNI root ) {
		// (primary only) check newly added tree's focusable state
	}

	LayerNI focusPrev() throws TJSException {
		// focus to previous layer
		LayerNI l;
		if( mFocusedLayer == null)
			l = searchFirstFocusable(false);// search first focusable layer
		else
			l = mFocusedLayer.getPrevFocusable();

		if(l!=null) setFocusTo(l, false);
		return l;
	}

	LayerNI focusNext() throws TJSException {
		// focus to next layer
		LayerNI l;
		if( mFocusedLayer == null )
			l = searchFirstFocusable(false);// search first focusable layer
		else
			l = mFocusedLayer.getNextFocusable();

		if(l!=null) setFocusTo(l, true);
		return l;
	}

	LayerNI searchFirstFocusable(boolean ignore_chain_focusable)
	{
		// (primary only) search first focusable layer
		if(mPrimary==null) return null;
		LayerNI lay = mPrimary.searchFirstFocusable(ignore_chain_focusable);

		return lay;
	}

	public boolean setFocusTo( LayerNI layer ) throws TJSException { return setFocusTo( layer, true ); }
	public boolean setFocusTo( LayerNI layer, boolean direction ) throws TJSException {
		// set focus to layer
		// direction = true : forward focus
		// direction = false: backward focus

		if( layer != null && !layer.getNodeFocusable() ) return false;

		if(layer != null && !layer.getShutdown() )
			layer = layer.fireBeforeFocus( mFocusedLayer, direction);

		if(layer != null && !layer.getNodeFocusable()) return false;

		if( mFocusedLayer == layer ) return false;


		if( mFocusChangeLock )
			Message.throwExceptionMessage( Message.CannotChangeFocusInProcessingFocus );
		mFocusChangeLock = true;

		LayerNI org = mFocusedLayer;
		mFocusedLayer = layer;

		try {
			if(org != null&& !org.getShutdown())
				org.fireBlur(layer);

			if( mFocusedLayer != null && !mFocusedLayer.getShutdown())
				mFocusedLayer.fireFocus(org, direction);
		} finally {
			//if(mFocusedLayer!=null) if(mFocusedLayer.mOwner != null) mFocusedLayer.mOwner.addRef();
			//if(org!=null) if(org.getOwner()!=null) org.mOwner.ReleaseOwner();
			mFocusChangeLock = false;
		}

		if(mFocusedLayer!=null) setImeModeOf(mFocusedLayer);
		else resetImeMode();
		if(mFocusedLayer!=null) setAttentionPointOf(mFocusedLayer);
		else disableAttentionPoint();

		mFocusChangeLock = false;
		return true;
	}
	void releaseAllModalLayer() {
		// (primary only) release all modal layer on invalidation
		mModalLayerVector.clear();
	}
	void setModeTo(LayerNI layer) throws TJSException {
		// (primary only) set mode to layer
		if(layer==null) return;

		saveEnabledWork();

		try {
			LayerNI current = getCurrentModalLayer();
			if(current !=null && layer.isAncestorOrSelf(current))
				Message.throwExceptionMessage(Message.CannotSetModeToDisabledOrModal);
					// cannot set mode to parent layer
			if(!layer.mVisible) layer.mVisible = true;
			if(!layer.getParentVisible() || !layer.mEnabled)
				Message.throwExceptionMessage(Message.CannotSetModeToDisabledOrModal);
					// cannot set mode to parent layer
			if(layer == current)
				Message.throwExceptionMessage(Message.CannotSetModeToDisabledOrModal);
					// cannot set mode to already modal layer

			setFocusTo(layer.searchFirstFocusable(), true);

			//if(layer.mOwner!=null) layer.mOwner->AddRef();
			mModalLayerVector.add(layer);

		} finally {
			notifyNodeEnabledState();
		}
	}
	void removeModeFrom(LayerNI layer) throws TJSException {
		// remove modal state from given layer
		boolean do_notify = false;

		try {
			int count = mModalLayerVector.size();
			for( int i = 0; i < count; ) {
				LayerNI ni = mModalLayerVector.get(i);
				if( layer == ni ) {
					if(!do_notify) { do_notify = true; saveEnabledWork(); }
					//if(layer.mOwner) layer.mOwner->Release();
					setFocusTo(layer.getNextFocusable(), true);
					mModalLayerVector.remove(i);
					count = mModalLayerVector.size();
				} else {
					i++;
				}
			}
		} finally {
			if(do_notify) notifyNodeEnabledState();
		}
	}

	private void removeTreeModalState( LayerNI root ) throws TJSException {
		// remove modal state from given tree
		boolean do_notify = false;

		try {
			int count = mModalLayerVector.size();
			for( int i = 0; i < count; ) {
				LayerNI ni = mModalLayerVector.get(i);
				if( ni.isAncestorOrSelf(root) ) {
					if(!do_notify) {
						do_notify = true;
						saveEnabledWork();
					}
					//if((*i)->Owner) (*i)->Owner->Release(); TODO
					setFocusTo(root.getNextFocusable(), true);
					mModalLayerVector.remove(i);
					count = mModalLayerVector.size();
				} else {
					i++;
				}
			}
		} finally {
			if(do_notify) notifyNodeEnabledState();
		}
	}

	public LayerNI getCurrentModalLayer() {
		// (primary only) get current modal layer
		int size = mModalLayerVector.size();
		if(size == 0) return null;
		return mModalLayerVector.get( size - 1 );
	}

	private boolean searchAttentionPoint( LayerNI target, Point pt ) {
		// search specified layer 's attention point
		while( target != null ) {
			if( target.getUseAttention() ) {
				pt.x = target.getAttentionLeft();
				pt.y = target.getAttentionTop();
				target.toPrimaryCoordinates(pt);
				return true;
			}
			target = target.getParent();
		}
		return false;
	}

	private void setAttentionPointOf(LayerNI layer ) {
		if( mWindow == null ) return;
		Point pt = new Point();
		if( searchAttentionPoint( layer, pt) )
			mWindow.setAttentionPoint( this, layer, pt );
		else
			mWindow.disableAttentionPoint(this);
	}

	private void disableAttentionPoint() {
		if( mWindow != null ) mWindow.disableAttentionPoint(this);
	}
	void notifyAttentionStateChanged(LayerNI from) {
		if(mFocusedLayer == from)
		{
			setAttentionPointOf(from);
		}
	}
	private void setImeModeOf(LayerNI layer) {
		if( mWindow == null ) return;
		mWindow.setImeMode( this, layer.getImeMode() );
	}
	private void resetImeMode() {
		mWindow.resetImeMode(this);
	}
	void notifyImeModeChanged(LayerNI from) {
		if( mFocusedLayer == from) {
			setImeModeOf(from);
		}
	}

	void saveEnabledWork() {
		// save current node enabled state to EnabledWork
		// this does recursive call
		if( mEnabledWorkRefCount == 0) if(mPrimary!=null) mPrimary.saveEnabledWork();

		mEnabledWorkRefCount++;
	}
	void notifyNodeEnabledState() {
		// notify node enabled state change to self and its children
		// this refers EnabledWork which is created by SaveEnabledWork
		mEnabledWorkRefCount--;

		if( mEnabledWorkRefCount == 0) if(mPrimary!=null) mPrimary.notifyNodeEnabledState();
	}

	void primaryKeyDown(int key, int shift ) throws TJSException {
		if(mFocusedLayer!=null)
			mFocusedLayer.fireKeyDown(key, shift);
		else
			if(mPrimary!=null) mPrimary.defaultKeyDown(key, shift);
	}
	void primaryKeyUp(int key, int shift) {
		if(mFocusedLayer!=null)
			mFocusedLayer.fireKeyUp(key, shift);
		else
			if(mPrimary!=null) mPrimary.defaultKeyUp(key, shift);
	}
	void primaryKeyPress(char key) {
		if(mFocusedLayer!=null)
			mFocusedLayer.fireKeyPress(key);
		else
			if(mPrimary!=null) mPrimary.defaultKeyPress(key);
	}
	void primaryMouseWheel(int shift, int delta, int x, int y) {
		if(mFocusedLayer!=null)
			mFocusedLayer.fireMouseWheel(shift, delta, x, y);
	}
	public void addUpdateRegion( final ComplexRect rects ) {
		mUpdateRegion.or(rects);
		if(mUpdateRegion.getCount() > UPDATE_UNITE_LIMIT)
			mUpdateRegion.unite();
		notifyWindowInvalidation();
	}
	public void addUpdateRegion( final Rect rect ) {
		// the window is invalidated;
		mUpdateRegion.or(rect);
		notifyWindowInvalidation();
	}
	public void updateToDrawDevice() throws VariantException, TJSException {
		// drawdevice -> layer
		if( mPrimary == null ) return;
		mPrimary.completeForWindow(this);
	}

	public void notifyUpdateRegionFixed() {
		// called by primary layer, notifying final update region is fixed
//			Window->NotifyUpdateRegionFixed(UpdateRegion);
	}

	public void requestInvalidation( final Rect r ) {
		// called by the owner window to notify window surface is invalidated by
		// the system or user.
		if( mPrimary == null ) return;

		Rect ur = new Rect();
		Rect cr = new Rect(0, 0, mPrimary.getRect().width(), mPrimary.getRect().height());

		if( Rect.intersectRect( ur, r, cr) ) {
			addUpdateRegion(ur);
		}
	}
	void recheckInputState() throws TJSException {
		// To re-check current layer under current mouse position
		// and update hint, cursor type and process layer enter/leave.
		// This can be reasonably slow, about 1 sec interval.
		forceMouseRecheck();
	}
	void dumpLayerStructure() {
		if(mPrimary!=null) mPrimary.dumpStructure();
	}
	public void notifyClick(int x, int y) throws TJSException {
		primaryClick(x, y);
	}
	public void notifyDoubleClick(int x, int y) throws TJSException {
		primaryDoubleClick(x, y);
	}
	public void notifyMouseDown(int x, int y, int mb, int flags) throws TJSException {
		primaryMouseDown(x, y, mb, flags);
	}
	public void notifyMouseUp(int x, int y, int mb, int flags) throws TJSException {
		primaryMouseUp(x, y, mb, flags);
	}
	public void notifyMouseMove(int x, int y, int flags) throws TJSException {
		primaryMouseMove(x, y, flags);
	}
	public void notifyMouseOutOfWindow() throws TJSException {
		mouseOutOfWindow();
	}
	public void notifyKeyDown(int key, int shift) throws TJSException {
		primaryKeyDown(key, shift);
	}
	public void notifyKeyUp(int key, int shift) {
		primaryKeyUp(key, shift);
	}
	public void notifyKeyPress(char key) {
		primaryKeyPress(key);
	}
	public void notifyMouseWheel(int shift, int delta, int x, int y) {
		primaryMouseWheel(shift, delta, x, y);
	}

}
