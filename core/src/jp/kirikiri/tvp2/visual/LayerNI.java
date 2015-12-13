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
import jp.kirikiri.tjs2.Interface;
import jp.kirikiri.tjs2.Dispatch2;
import jp.kirikiri.tjs2.Holder;
import jp.kirikiri.tjs2.IntVector;
import jp.kirikiri.tjs2.IntWrapper;
import jp.kirikiri.tjs2.NativeInstance;
import jp.kirikiri.tjs2.NativeInstanceObject;
import jp.kirikiri.tjs2.TJS;
import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tjs2.TJSScriptError;
import jp.kirikiri.tjs2.TJSScriptException;
import jp.kirikiri.tjs2.Variant;
import jp.kirikiri.tjs2.VariantClosure;
import jp.kirikiri.tjs2.VariantException;
import jp.kirikiri.tvp2.TVP;
import jp.kirikiri.tvp2.base.CompactEventCallbackInterface;
import jp.kirikiri.tvp2.base.ContinuousEventCallbackInterface;
import jp.kirikiri.tvp2.base.EventManager;
import jp.kirikiri.tvp2.msg.Message;
import jp.kirikiri.tvp2.utils.DebugClass;
import jp.kirikiri.tvp2.utils.ObjectList;
import jp.kirikiri.tvp2env.Font;
import jp.kirikiri.tvp2env.SystemColor;
import jp.kirikiri.tvp2env.VirtualKey;
import jp.kirikiri.tvp2env.WindowForm;

/**
 * オリジナルとは異なり、BaseLayer クラスはすっ飛ばす
 */
public class LayerNI extends NativeInstanceObject implements CompactEventCallbackInterface, Drawable {

	private static final int TRANS_WHITE = 0x00FFFFFF;
	private static final int TRANS_BLACK = 0x00000000;
	private static final int TRANS_GLAY = 0x00808080;
	private static final int WHITE = 0xFFFFFFFF;

	static public boolean PurgeOnHide = true;
	static public boolean DefaultHoldAlpha;
	static public void initialize() {
		DefaultHoldAlpha = false;
	}

	static private final int htMask = 0, htProvince = 1;

	static private final int
		tutDivisibleFade = 0,
		tutDivisible = 1,
		tutGiveUpdate = 2;

	static private final int
		gsotNone = 0,
		gsotSimple = 1,
		gsotInterlace = 2,
		gsotBiDirection = 3;

	static public final int
		bmCopy = 0,
		bmCopyOnAlpha = 1,
		bmAlpha = 2,
		bmAlphaOnAlpha = 3,
		bmAdd = 4,
		bmSub = 5,
		bmMul = 6,
		bmDodge = 7,
		bmDarken = 8,
		bmLighten = 9,
		bmScreen = 10,
		bmAddAlpha = 11,
		bmAddAlphaOnAddAlpha = 12,
		bmAddAlphaOnAlpha = 13,
		bmAlphaOnAddAlpha = 14,
		bmCopyOnAddAlpha = 15,
		bmPsNormal = 16,
		bmPsAdditive = 17,
		bmPsSubtractive = 18,
		bmPsMultiplicative = 19,
		bmPsScreen = 20,
		bmPsOverlay = 21,
		bmPsHardLight = 22,
		bmPsSoftLight = 23,
		bmPsColorDodge = 24,
		bmPsColorDodge5 = 25,
		bmPsColorBurn = 26,
		bmPsLighten = 27,
		bmPsDarken = 28,
		bmPsDifference = 29,
		bmPsDifference5 = 30,
		bmPsExclusion = 31;


	//static private int GraphicSplitOperationType = gsotSimple;
	static private int GraphicSplitOperationType = gsotNone;
	static private final int CACHE_UNITE_LIMIT = 60;
	static private final int EXPOSED_UNITE_LIMIT = 30;
	static private final int DIRECT_UNITE_LIMIT = 10;
	static private final boolean FreeUnusedLayerCache = true;


	private WeakReference<Dispatch2> mOwner;
	public final Dispatch2 getOwner() { return mOwner.get(); }
	public final void setOwner( Dispatch2 owner ) { mOwner = new WeakReference<Dispatch2>(owner); }
	private VariantClosure mActionOwner;

	private boolean mShutdown;
	public final boolean getShutdown() { return mShutdown; }
	private boolean mCompactEventHookInit;

	LayerManager mManager;
	public final LayerManager getManager() { return mManager; }

	private LayerNI mParent;
	public final LayerNI getParent() { return mParent; }
	private ObjectList<LayerNI> mChildren;
	private String mName;
	boolean mVisible;
	private int mOpacity;
	private int mOrderIndex;
		// index in parent's 'Children' array.
		// do not access this variable directly; use GetOrderIndex() instead.
	private int mOverallOrderIndex;
		// index in overall tree.
		// use GetOverallOrderIndex instead
	private boolean mAbsoluteOrderMode; // manage Z order in absolute Z position
	private int mAbsoluteOrderIndex;
		// do not access this variable directly; use GetAbsoluteOrderIndex() instead.

	private boolean mChildrenOrderIndexValid;

	private int mVisibleChildrenCount;
	private Dispatch2 mChildrenArray; // Array object which holds children array ...
	private Dispatch2 mArrayClearMethod; // holds Array.clear method
	private boolean mChildrenArrayValid;

	private int mType; // user set Type
	private int mDisplayType; // actual Type

	private Rect mRect;
	public final Rect getRect() { return mRect; }
	private boolean mExposedRegionValid;
	private ComplexRect mExposedRegion; // exposed region (no-children-overlapped-region)
	private ComplexRect mOverlappedRegion; // overlapped region (overlapped by children)
		// above two shuld not be accessed directly

	private BaseBitmap mMainImage;
	private boolean mCanHaveImage; // whether the layer can have image
	private BaseBitmap mProvinceImage;
	private int mNeutralColor; // Neutral Color (which can be set by the user)
	private int mTransparentColor; // transparent color (which cannot be set by the user, decided by layer type)

	private int mImageLeft; // image offset left
	private int mImageTop; // image offset top

	private int mHitType;
	private int mHitThreshold;
	private boolean mOnHitTest_Work;
	boolean mEnabled; // is layer enabled for input?
	private boolean mEnabledWork; // work are for delivering onNodeEnabled or onNodeDisabled
	private boolean mFocusable; // is layer focusable ?
	private boolean mJoinFocusChain; // does layer join the focus chain ?
	private LayerNI mFocusWork;

	private int mAttentionLeft; // attention position
	public final int getAttentionLeft() { return mAttentionLeft; }
	private int mAttentionTop;
	public final int getAttentionTop() { return mAttentionTop; }
	private int mUseAttention;
	public final boolean getUseAttention() { return mUseAttention != 0; }

	private int mImeMode; // ime mode
	public final int getImeMode() {return mImeMode; }

	private int mCursor; // mouse cursor
	private int mCursorX_Work; // holds x-coordinate in SetCursorX and SetCursorY

	private String mHint; // layer hint text
	private boolean mShowParentHint; // show parent's hint ?

	private BaseBitmap mCacheBitmap;
	//private ComplexRect mCachedRegion;
	private int mCacheEnabledCount;
	private boolean mCached;  // script-controlled cached state

	private int mDrawFace; // (actual) current drawing layer face
	private int mFace; // (outward) current drawing layer face

	private boolean mHoldAlpha; // whether the layer alpha channel is to be kept
		// when the layer type is ltOpaque

	private boolean mImageModified; // flag to know modification of layer image
	private Rect mClipRect; // clipping rectangle

	private Font mFont;
	private boolean mFontChanged;
	private Dispatch2 mFontObject;

	private int mUpdateOfsX, mUpdateOfsY;
	private Rect mUpdateRectForChild; // to be used in tTVPDrawable::GetDrawTargetBitmap
	private int mUpdateRectForChildOfsX;
	private int mUpdateRectForChildOfsY;
	private Drawable mCurrentDrawTarget; // set by Draw
	private BaseBitmap mUpdateBitmapForChild; // to be used in tTVPDrawable::GetDrawTargetBitmap
	private Rect mUpdateExcludeRect; // rectangle whose update is not be needed

	private ComplexRect mCacheRecalcRegion; // region that must be reconstructed for cache
	private ComplexRect mDrawnRegion; // region that is already marked as "blitted"
	private boolean mDirectTransferToParent; // child image should be directly transfered into parent

	private boolean mCallOnPaint; // call onPaint event when flaged


	private boolean mInCompletion; // update/completion pipe line is processing

	private DivisibleTransHandler mDivisibleTransHandler;
	private GiveUpdateTransHandler mGiveUpdateTransHandler;

	private LayerNI mTransDest; // transition destination
	private Dispatch2 mTransDestObj;
	private LayerNI mTransSrc; // transition source
	private Dispatch2 mTransSrcObj;

	private boolean mInTransition; // is transition processing?
	private boolean mTransWithChildren; // is transition with children?
	private boolean mTransSelfUpdate; // is transition update performed by user code ?

	private long mTransTick; // current tick count
	private boolean mUseTransTickCallback; // whether to use tick source callback function
	private VariantClosure mTransTickCallback; // tick callback function

	private int mTransType; // current transition type
	private int mTransUpdateType; // current transition update type

	private ScanLineProviderForBaseBitmap mDestSLP; // destination scan line provider
	private ScanLineProviderForBaseBitmap mSrcSLP; // source scan line provider

	private boolean mTransCompEventPrevented; // whether "onTransitionCompleted" event is prevented

	static class TransDrawable implements Drawable {
		// tTVPDrawable class for Transition pipe line rearrangement
		LayerNI Owner;
		BaseBitmap Target;
		Rect TargetRect;
		Drawable OrgDrawable;

		BaseBitmap Src1Bmp; // tutDivisible
		BaseBitmap Src2Bmp; // tutDivisible

		public TransDrawable() {
			TargetRect = new Rect();
		}
		public void init( LayerNI owner, Drawable org ) {
			Owner = owner;
			OrgDrawable = org;
			Target = null;
		}

		public BaseBitmap getDrawTargetBitmap( final Rect rect, Rect cliprect ) throws TJSException {
			// save target bitmap pointer
			Target = OrgDrawable.getDrawTargetBitmap(rect, cliprect);
			TargetRect.set( cliprect );
			return Target;
		}
		public int getTargetLayerType() {
			return OrgDrawable.getTargetLayerType();
		}
		public void drawCompleted( final Rect destrect, BaseBitmap bmp, final Rect cliprect, int type, int opacity ) throws VariantException, TJSException {
			// do divisible transition
			if(!Owner.mInTransition || Owner.mDivisibleTransHandler == null ) return;

			DivisibleData data = new DivisibleData();
			data.Left = destrect.left - Owner.mRect.left;
			data.Top = destrect.top - Owner.mRect.top;
			data.Width = cliprect.width();
			data.Height = cliprect.height();

			BaseBitmap src1bmp;
			if(Owner.mTransUpdateType == tutDivisible)
				src1bmp = Src1Bmp;
			else
				src1bmp = bmp;
			Owner.mImageModified = true;

			if( Owner.mSrcSLP == null )
				Owner.mSrcSLP = new ScanLineProviderForBaseBitmap(src1bmp);
			else
				Owner.mSrcSLP.attach(src1bmp);

			data.Src1 = Owner.mSrcSLP;
			data.Src1Left = cliprect.left;
			data.Src1Top = cliprect.top;

			BaseBitmap src = null;
			if( Owner.mTransSrc != null ) {
				// source available
				// prepare source 2 from CacheBitmap
				if(Owner.mTransUpdateType == tutDivisible)
					src = Src2Bmp;
				else
					src = Owner.mTransSrc.complete(destrect);
				if( Owner.mTransSrc.mSrcSLP == null )
					Owner.mTransSrc.mSrcSLP = new ScanLineProviderForBaseBitmap(src);
				else
					Owner.mTransSrc.mSrcSLP.attach(src);

				data.Src2 = Owner.mTransSrc.mSrcSLP;
				data.Src2Left = data.Left; // destrect.left;
				data.Src2Top = data.Top; //destrect.top;
			} else {
				data.Src2 = null;
			}

			BaseBitmap dest;
			boolean tempalloc = false;
			if( bmp == Target || Target == null ) {
				// source bitmap is the same as the Original Target;
				// allocatte temporary bitmap
				dest = TempBitmapHolder.getTemp( cliprect.width(), cliprect.height(), true);  // fit = true
							// OriginalTODO: check whether "fit" can affect the performance

				tempalloc = true;
				if( Owner.mDestSLP == null )
					Owner.mDestSLP = new ScanLineProviderForBaseBitmap(dest);
				else
					Owner.mDestSLP.attach(dest);
				data.Dest = Owner.mDestSLP;
				data.DestLeft = 0;
				data.DestTop = 0;
			} else {
				if( Owner.mDestSLP == null )
					Owner.mDestSLP = new ScanLineProviderForBaseBitmap(Target);
				else
					Owner.mDestSLP.attach(Target);
				dest = Target;
				data.Dest = Owner.mDestSLP;
				data.DestLeft = TargetRect.left;
				data.DestTop = TargetRect.top;
			}

			try {
				Owner.mDivisibleTransHandler.process(data);
				Rect cr = new Rect(cliprect);

				if( data.Dest == Owner.mDestSLP ) {
					cr.setOffsets(data.DestLeft, data.DestTop);
					OrgDrawable.drawCompleted(destrect, dest, cr, type, opacity);
				} else if(data.Dest == data.Src1) {
					cr.setOffsets(data.DestLeft, data.DestTop);
					OrgDrawable.drawCompleted(destrect, bmp, cr, type, opacity);
				} else if(data.Dest == data.Src2 && Owner.mTransSrc != null ) {
					cr.setOffsets(data.DestLeft, data.DestTop);
					OrgDrawable.drawCompleted(destrect, src, cr, type, opacity);
				}
			} finally {
				if(tempalloc) TempBitmapHolder.freeTemp();
			}
		}
	}
	TransDrawable mTransDrawable;

	static class TransIdleCallback implements ContinuousEventCallbackInterface {
		public LayerNI Owner;
		public void onContinuousCallback( long tick ) throws VariantException, TJSException {
			Owner.invokeTransition(tick);
		}
		// from tTVPIdleEventCallbackIntf
	}
	TransIdleCallback mTransIdleCallback;

	public LayerNI() throws TJSException {

		// object lifetime stuff
		//mOwner = null;
		mActionOwner = new VariantClosure(null);
		//mShutdown = false;
		//mCompactEventHookInit = false;

		// interface to layer manager
		//mManager = null;

		// tree management
		//mParent = null;
		//mVisible = false;
		mOpacity = 255;
		mVisibleChildrenCount = -1;
		//mChildrenArray = null;
		//mChildrenArrayValid = false;
		//mArrayClearMethod = null;
		//mOrderIndex = 0;
		//mOverallOrderIndex = 0;
		//mChildrenOrderIndexValid = false;
		//mAbsoluteOrderMode = false; // initially relative mode
		//mAbsoluteOrderIndex = 0;

		// layer type management
		mDisplayType = mType = LayerType.ltAlpha;
			// later reset this if the layer becomes a primary layer
		mNeutralColor = mTransparentColor = TRANS_WHITE; //rgba2Color(255, 255, 255, 0);

		// geographical management
		//mExposedRegionValid = false;
		mRect = new Rect();
		//mRect.left = 0;
		//mRect.top = 0;
		mRect.right = 32;
		mRect.bottom = 32;

		// input event / hit test management
		mHitType = htMask;
		mHitThreshold = 16;
		//mCursor = 0; // 0 = crDefault
		//mCursorX_Work = 0;
		mShowParentHint = true;
		//mUseAttention = 0; // false;
		//mImeMode = 0;//imDisable;
		//mAttentionLeft = mAttentionTop = 0;

		mEnabled = true;
		//mFocusable = false;
		mJoinFocusChain = true;

		// image buffer management
		//mMainImage = null;
		mCanHaveImage = true;
		//mProvinceImage = null;
		//mImageLeft = 0;
		//mImageTop = 0;

		// cache management
		//mCacheEnabledCount = 0;
		//mCacheBitmap = null;
		//mCached = false;

		// drawing function stuff
		mFace = DrawFace.dfAuto;
		updateDrawFace();
		//mImageModified = false;
		mHoldAlpha = DefaultHoldAlpha;
		mClipRect = new Rect();
		//mClipRect.left = 0;
		//mClipRect.right = 0;
		//mClipRect.top = 0;
		//mClipRect.bottom = 0;

		// Updating management
		//mCallOnPaint = false;
		//mInCompletion = false;

		// transition management
		//mDivisibleTransHandler = null;
		//mGiveUpdateTransHandler = null;
		//mTransDest = null;
		//mTransDestObj = null;
		//mTransSrc = null;
		//mTransSrcObj = null;
		//mInTransition = false;
		//mTransWithChildren = false;
		//mDestSLP = null;
		//mSrcSLP = null;
		//mTransCompEventPrevented = false;
		//mUseTransTickCallback = false;
		mTransTickCallback = new VariantClosure(null, null);

		mChildren = new ObjectList<LayerNI>();
		mCacheRecalcRegion = new ComplexRect();
		//mCachedRegion = new ComplexRect();
		mDrawnRegion = new ComplexRect();
		mUpdateRectForChild = new Rect();
		mUpdateExcludeRect = new Rect();
		mExposedRegion = new ComplexRect();
		mOverlappedRegion = new ComplexRect();

		// allocate the default image
		allocateDefaultImage();

		// interface to font object
		mFont = mMainImage.getFont(); // retrieve default font
		//mFontObject = null;

		mTransIdleCallback = new TransIdleCallback();
		mTransDrawable = new TransDrawable();
	}

	public int construct( Variant[] param, Dispatch2 tjs_obj ) throws TJSException {
		if( param.length < 2) return Error.E_BADPARAMCOUNT;

		mOwner = new WeakReference<Dispatch2>(tjs_obj); // no addref

		// get the window native instance
		VariantClosure clo = param[0].asObjectClosure();
		if( clo.mObject == null )
			Message.throwExceptionMessage(Message.SpecifyWindow);

		WindowNI win = (WindowNI)clo.mObject.getNativeInstance( WindowClass.ClassID );
		if( win == null )
			Message.throwExceptionMessage(Message.SpecifyWindow);

		// get the layer native instance
		clo = param[1].asObjectClosure();
		LayerNI lay = null;
		if( clo.mObject != null ) {
			lay = (LayerNI)clo.mObject.getNativeInstance( LayerClass.ClassID );
			if( lay == null )
				Message.throwExceptionMessage(Message.SpecifyLayer);
		}

		// retrieve manager
		// layer manager is the same as the parent, if the parent is given
		if( lay != null ) {
			mManager = lay.getManager();
		}

		// register to parent layer
		if( lay != null ) join(lay);

		// is primarylayer ?
		// ask window to create layer manager
		if( lay == null ) {
			mManager = new LayerManager(win);
			mManager.attachPrimary(this);
			mManager.registerSelfToWindow();

			mType = mDisplayType = LayerType.ltOpaque; // initially ltOpaque
			mNeutralColor = mTransparentColor = WHITE; // rgba2Color(255, 255, 255, 255);
			updateDrawFace();
			mHitThreshold = 0;
		}
//		IncCacheEnabledCount(); ///// -------------------- test

		mActionOwner.set( param[0].asObjectClosure() );

		return Error.S_OK;
	}

	public void invalidate() throws TJSException {
		mShutdown = true;

		// stop transition
		stopTransition();
		if(mTransDest != null ) mTransDest.stopTransition();
		if(mDestSLP != null ) mDestSLP = null;
		if(mSrcSLP != null ) mSrcSLP = null;

		// cancel events
		Dispatch2 owner = mOwner.get();
		TVP.EventManager.cancelSourceEvents(owner);

		// release all objects
		if( isPrimary() ) {
			if(mManager != null) mManager.detachPrimary();
			// also detach from draw device
			mManager.unregisterSelfFromWindow();
		}

		if( mManager != null ) {
			mManager = null; // no longer used in this context
		}

		// part from the parent
		part();

		// sever all children
		try {
			mChildren.safeLock();
			final int count = mChildren.getSafeLockedObjectCount();
			for( int i = 0; i < count; i++ ) {
				LayerNI child = mChildren.get(i);
				if( child != null) {
					child.part();
				}
			}
			mChildrenArrayValid = false;
			mChildrenOrderIndexValid = false;
			if( mManager != null ) mManager.invalidateOverallIndex();
		} finally {
			mChildren.safeUnlock();
		}

		// invalidate font object
		if( mFontObject != null ) {
			mFontObject.invalidate( 0, null, mFontObject );
			mFontObject = null;
		}

		// deallocate image
		deallocateImage();

		// free cache image
		deallocateCache();

		// release the owner
		mActionOwner.mObjThis = mActionOwner.mObject = null;
		//mActionOwner = null;

		// release Children array
		if( mChildrenArray != null ) mChildrenArray = null;
		if( mArrayClearMethod != null ) mArrayClearMethod = null;

		// unregister from compact event hook
		if(mCompactEventHookInit) TVP.EventManager.removeCompactEventHook(this);

		// cancel events once more
		TVP.EventManager.cancelSourceEvents(owner);
	}
	/*
	protected void finalize() {
		try {
			super.finalize();
		} catch (Throwable e) {
		}
	}
	*/

	private void join( LayerNI parent ) throws TJSException {
		if( parent == this )
			Message.throwExceptionMessage(Message.CannotSetParentSelf);
		if( parent != null && parent.mManager != mManager)
			Message.throwExceptionMessage(Message.CannotMoveToUnderOtherPrimaryLayer);
		if( mParent != null ) part();
		mParent = parent;
		if(mParent != null ) parent.addChild(this);
	}
	private void part() throws TJSException {
		update();

		if( mManager != null ) mManager.notifyPart(this);

		if( mParent != null ) {
			mParent.severChild(this);
			mParent = null;
		}
	}
	private void addChild( LayerNI child ) {
		notifyChildrenVisualStateChanged();
		mChildren.add(child);
		if( mAbsoluteOrderMode ) {
			// first insertion
			mChildren.compact();
			int count = mChildren.getCount();
			if( count >= 2 ) {
				LayerNI last = mChildren.get(count-2);
				child.mAbsoluteOrderIndex = last.getAbsoluteOrderIndex() +1;
			}
		}
		mChildrenArrayValid = false;
		mChildrenOrderIndexValid = false;
		if(mManager!=null) mManager.checkTreeFocusableState(child); // check focusable state of child
		if(mManager!=null) mManager.invalidateOverallIndex();
	}
	private void severChild( LayerNI child ) throws TJSException {
		if( mManager != null ) mManager.blurTree(child); // remove focus from "child"
		notifyChildrenVisualStateChanged();
		mChildren.remove(child);
		mChildrenArrayValid = false;
		mChildrenOrderIndexValid = false;
		if(mManager != null) mManager.invalidateOverallIndex();
	}

	private void updateDrawFace() {
		// set DrawFace from Face and Type
		if( mFace == DrawFace.dfAuto ) {
			// DrawFace is chosen automatically from the layer type
			switch(mDisplayType)
			{
		//	case ltBinder:
			case LayerType.ltOpaque:			mDrawFace = DrawFace.dfOpaque;			break;
			case LayerType.ltAlpha:				mDrawFace = DrawFace.dfAlpha;			break;
			case LayerType.ltAdditive:			mDrawFace = DrawFace.dfOpaque;			break;
			case LayerType.ltSubtractive:		mDrawFace = DrawFace.dfOpaque;			break;
			case LayerType.ltMultiplicative:	mDrawFace = DrawFace.dfOpaque;			break;
		//	case ltEffect:
		//	case ltFilter:
			case LayerType.ltDodge:				mDrawFace = DrawFace.dfOpaque;			break;
			case LayerType.ltDarken:			mDrawFace = DrawFace.dfOpaque;			break;
			case LayerType.ltLighten:			mDrawFace = DrawFace.dfOpaque;			break;
			case LayerType.ltScreen:			mDrawFace = DrawFace.dfOpaque;			break;
			case LayerType.ltAddAlpha:			mDrawFace = DrawFace.dfAddAlpha;		break;
			case LayerType.ltPsNormal:			mDrawFace = DrawFace.dfAlpha;			break;
			case LayerType.ltPsAdditive:		mDrawFace = DrawFace.dfAlpha;			break;
			case LayerType.ltPsSubtractive:		mDrawFace = DrawFace.dfAlpha;			break;
			case LayerType.ltPsMultiplicative:	mDrawFace = DrawFace.dfAlpha;			break;
			case LayerType.ltPsScreen:			mDrawFace = DrawFace.dfAlpha;			break;
			case LayerType.ltPsOverlay:			mDrawFace = DrawFace.dfAlpha;			break;
			case LayerType.ltPsHardLight:		mDrawFace = DrawFace.dfAlpha;			break;
			case LayerType.ltPsSoftLight:		mDrawFace = DrawFace.dfAlpha;			break;
			case LayerType.ltPsColorDodge:		mDrawFace = DrawFace.dfAlpha;			break;
			case LayerType.ltPsColorDodge5:		mDrawFace = DrawFace.dfAlpha;			break;
			case LayerType.ltPsColorBurn:		mDrawFace = DrawFace.dfAlpha;			break;
			case LayerType.ltPsLighten:			mDrawFace = DrawFace.dfAlpha;			break;
			case LayerType.ltPsDarken:			mDrawFace = DrawFace.dfAlpha;			break;
			case LayerType.ltPsDifference:	 	mDrawFace = DrawFace.dfAlpha;			break;
			case LayerType.ltPsDifference5:	 	mDrawFace = DrawFace.dfAlpha;			break;
			case LayerType.ltPsExclusion:		mDrawFace = DrawFace.dfAlpha;			break;
			default:
								mDrawFace = DrawFace.dfOpaque;			break;
			}
		} else {
			mDrawFace = mFace;
		}
	}

	static private final String ON_TRANSITION_SOMPLITE = "onTransitionCompleted";
	private void internalStopTransition() throws TJSException {
		// stop transition
		if( mInTransition ) {
			mInTransition = false;
			mTransCompEventPrevented = false;

			// unregister idle event handler
			if(!mTransSelfUpdate) TVP.EventManager.removeContinuousEventHook(mTransIdleCallback);

			// disable cache
			if( mTransWithChildren ) {
				decCacheEnabledCount();
				if(mTransSrc != null) mTransSrc.decCacheEnabledCount();
			}

			//
			// exchange the layer
			if( mTransType == TransHandlerProvider.ttExchange ) {
				int tl = mRect.left;
				int tt = mRect.top;
				int sl = mTransSrc.mRect.left;
				int st = mTransSrc.mRect.top;
				boolean tv = getVisible();
				boolean sv = mTransSrc.getVisible();
				if( mTransWithChildren ){
					// exchange with tree structure
					exchange(mTransSrc,false);
				} else {
					// exchange the layer and the target only
					swap(mTransSrc);
				}
				setPosition(sl, st);
				mTransSrc.setPosition(tl, tt);
				setVisible(sv);
				mTransSrc.setVisible(tv);
			}

			boolean transsrcalive = false;
			if( mTransSrc != null && !mTransSrc.mShutdown ) transsrcalive = true;

			if( mTransSrc != null) mTransSrc.mTransDest = null;
			mTransSrc = null;

			// release transition handler object
			if( mDivisibleTransHandler != null ) mDivisibleTransHandler = null;
			if( mGiveUpdateTransHandler != null ) mGiveUpdateTransHandler = null;

			// fire event
			Dispatch2 owner = mOwner.get();
			if( owner != null && !mShutdown && transsrcalive ) {
				// fire SYNCHRONOUS event of "onTransitionCompleted"
				Variant[] param = {
					new Variant(mTransDestObj, mTransDestObj),
					new Variant( mTransSrcObj, mTransSrcObj) };
				mTransDestObj = null;
				mTransSrcObj = null;

				TVP.EventManager.postEvent( owner, owner, ON_TRANSITION_SOMPLITE, 0, EventManager.EPT_IMMEDIATE, param);
			}

			// release destination and source objects
			if( mTransDestObj != null ) mTransDestObj = null;
			if( mTransSrcObj != null ) mTransSrcObj = null;

			// release TransTickCallback
			mTransTickCallback.set(null, null);
		}
	}
	private void swap(LayerNI target) throws TJSException {
		exchange(target, true);
	}
	private void exchange( LayerNI target, boolean keepchildren ) throws TJSException {
		// exchange this for the other layer
		if(this == target) return;

		LayerNI this_ancestor_child = getAncestorChild(target);
		LayerNI target_ancestor_child = target.getAncestorChild(this);

		LayerNI this_parent = mParent;
		LayerNI target_parent = target.mParent;

		boolean this_primary = isPrimary();
		boolean target_primary = target.isPrimary();

		boolean this_parent_absolute = true;
		if( mParent != null ) this_parent_absolute = mParent.mAbsoluteOrderMode;
		int this_index = getAbsoluteOrderIndex();

		boolean target_parent_absolute = true;
		if( target.mParent != null ) target_parent_absolute = target.mParent.mAbsoluteOrderMode;
		int target_index = target.getAbsoluteOrderIndex();

		// remove primary
		if( mManager != null ) {
			if(this_primary) mManager.detachPrimary();
			if(target_primary) mManager.detachPrimary();
		}

		// part from each parent
		part();
		target.part();

		LayerNI this_joined_parent;
		LayerNI target_joined_parent;
		if( this_ancestor_child != null ) {
			// "this" is a descendant of the "target"
			if( this_ancestor_child != this ) this_ancestor_child.part();

			if( !keepchildren ) {
				// join to each target's parent
				join(this_joined_parent = target_parent);
				if(target == this_parent)
					target.join(target_joined_parent = this);
				else
					target.join(target_joined_parent = this_parent);
			} else {
				// sever children
				IntVector this_orders = new IntVector();
				IntVector target_orders = new IntVector();
				ObjectList<LayerNI> this_children = new ObjectList<LayerNI>(mChildren);
				ObjectList<LayerNI> target_children = new ObjectList<LayerNI>(target.mChildren);
				int this_children_count = this_children.getActualCount();
				int target_children_count = target_children.getActualCount();

				for( int i = 0; i < this_children_count; i++ ) {
					this_orders.push_back( this_children.get(i).getAbsoluteOrderIndex() );
					this_children.get(i).part();
				}

				for(int i = 0; i < target_children_count; i++) {
					target_orders.push_back( target_children.get(i).getAbsoluteOrderIndex());
					target_children.get(i).part();
				}

				// join to each target's parent
				join( this_joined_parent = target_parent );
				if( target == this_parent )
					target.join(target_joined_parent = this);
				else
					target.join(target_joined_parent = this_parent);

				// let children join
				for(int i = 0; i < this_children_count; i++)
					this_children.get(i).join(target);
				for(int i = 0; i < target_children_count; i++)
					target_children.get(i).join(this);

				if( mAbsoluteOrderMode && target.mAbsoluteOrderMode ) {
					// reset order index
					for(int i = 0; i < this_children_count; i++)
						this_children.get(i).setAbsoluteOrderIndex(this_orders.get(i));
					for(int i = 0; i < target_children_count; i++)
						target_children.get(i).setAbsoluteOrderIndex(target_orders.get(i));
				}
			}


			if(this_ancestor_child != this) this_ancestor_child.join(this);
		} else if( target_ancestor_child != null ) {
			// "target" is a descendant of "this"
			if(target_ancestor_child != target) target_ancestor_child.part();

			if( !keepchildren ) {
				// join to each target's parent
				if(this == target_parent)
					this.join(this_joined_parent = target);
				else
					this.join(this_joined_parent = target_parent);
				target.join(target_joined_parent = this_parent);
			} else {
				// sever children
				IntVector this_orders = new IntVector();
				IntVector target_orders = new IntVector();
				ObjectList<LayerNI> this_children = new ObjectList<LayerNI>(mChildren);
				ObjectList<LayerNI> target_children = new ObjectList<LayerNI>(target.mChildren);
				int this_children_count = this_children.getActualCount();
				int target_children_count = target_children.getActualCount();

				for( int i = 0; i < this_children_count; i++ ) {
					this_orders.push_back( this_children.get(i).getAbsoluteOrderIndex());
					this_children.get(i).part();
				}
				for(int i = 0; i < target_children_count; i++) {
					target_orders.push_back( target_children.get(i).getAbsoluteOrderIndex());
					target_children.get(i).part();
				}

				// join to each target's parent
				if(this == target_parent)
					join(this_joined_parent = target);
				else
					join(this_joined_parent = target_parent);
				target.join(target_joined_parent = this_parent);

				// let children join
				for(int i = 0; i < this_children_count; i++)
					this_children.get(i).join(target);
				for(int i = 0; i < target_children_count; i++)
					target_children.get(i).join(this);

				if( mAbsoluteOrderMode && target.mAbsoluteOrderMode ) {
					// reset order index
					for(int i = 0; i < this_children_count; i++)
						this_children.get(i).setAbsoluteOrderIndex(this_orders.get(i));
					for(int i = 0; i < target_children_count; i++)
						target_children.get(i).setAbsoluteOrderIndex(target_orders.get(i));
				}
			}

			if(target_ancestor_child != target) target_ancestor_child.join(target);
		} else {
			// two layers have no parent-child relationship
			if( !keepchildren ) {
				// join to each target's parent
				join(this_joined_parent = target_parent);
				target.join(target_joined_parent = this_parent);
			} else {
				// sever children
				IntVector this_orders = new IntVector();
				IntVector target_orders = new IntVector();
				ObjectList<LayerNI> this_children = new ObjectList<LayerNI>(mChildren);
				ObjectList<LayerNI> target_children = new ObjectList<LayerNI>(target.mChildren);
				int this_children_count = this_children.getActualCount();
				int target_children_count = target_children.getActualCount();

				for( int i = 0; i < this_children_count; i++ ) {
					this_orders.push_back(this_children.get(i).getAbsoluteOrderIndex());
					this_children.get(i).part();
				}
				for( int i = 0; i < target_children_count; i++ ) {
					target_orders.push_back( target_children.get(i).getAbsoluteOrderIndex() );
					target_children.get(i).part();
				}

				// join to each target's parent
				join(this_joined_parent = target_parent);
				target.join(target_joined_parent = this_parent);

				// let children join
				for(int i = 0; i < this_children_count; i++)
					this_children.get(i).join(target);
				for(int i = 0; i < target_children_count; i++)
					target_children.get(i).join(this);

				if( mAbsoluteOrderMode && target.mAbsoluteOrderMode ) {
					// reset order index
					for(int i = 0; i < this_children_count; i++)
						this_children.get(i).setAbsoluteOrderIndex(this_orders.get(i));
					for(int i = 0; i < target_children_count; i++)
						target_children.get(i).setAbsoluteOrderIndex(target_orders.get(i));
				}
			}
		}

		// attach primary
		if( mManager != null ) {
			if(this_primary) mManager.attachPrimary(target);
			if(target_primary) mManager.attachPrimary(this);
		}

		// reset order index
		if(target_joined_parent == this_joined_parent &&
			target_joined_parent != null &&
			target_joined_parent.mAbsoluteOrderMode == target_parent_absolute &&
			this_joined_parent.mAbsoluteOrderMode == this_parent_absolute &&
			target_parent_absolute == this_parent_absolute &&
			target_parent_absolute == false) {
			// two layers have the same parent and the same order mode
			if( this_index < target_index ) {
				target.setOrderIndex(this_index);
				this.setOrderIndex(target_index);
			} else {
				this.setOrderIndex(target_index);
				target.setOrderIndex(this_index);
			}
		} else {
			if( target_joined_parent != null && target_joined_parent.mAbsoluteOrderMode == target_parent_absolute ) {
				if(target_parent_absolute)
					target.setAbsoluteOrderIndex(target_index);
				else
					target.setOrderIndex(target_index);
			}

			if( this_joined_parent != null && this_joined_parent.mAbsoluteOrderMode == this_parent_absolute ) {
				if( this_parent_absolute )
					this.setAbsoluteOrderIndex(this_index);
				else
					this.setOrderIndex(this_index);
			}
		}
	}
	/**
	 * retrieve "ancestor"'s child that is ancestor of this ( can be thisself )
	 * @param ancestor
	 * @return
	 */
	private LayerNI getAncestorChild(LayerNI ancestor) {

		LayerNI c = this;
		LayerNI p = mParent;
		while( p != null ) {
			if(p == ancestor) return c;
			c = p;
			p = p.mParent;
		}
		return null;
	}
	//---------------------------------------------------------------------------
	public void stopTransition() throws TJSException {
		// stop the transition by manual
		internalStopTransition();
	}
	public boolean isPrimary() {
		if( mManager == null ) return false;
		return mManager.getPrimaryLayer() == this;
	}
	private void deallocateImage() {
		if( mMainImage != null) mMainImage = null;
		if( mProvinceImage != null ) mProvinceImage = null;
		mImageModified = true;
	}
	private void allocateCache() throws TJSException {
		if( mCacheBitmap == null ) {
			mCacheBitmap = new BaseBitmap( mRect.width(), mRect.height(), 32 );
		} else {
			mCacheBitmap.setSize( mRect.width(), mRect.height() );
		}
		mCacheRecalcRegion.or( new Rect(0, 0, mRect.width(), mRect.height()) );
	}
	private void deallocateCache() {
		if( mCacheBitmap != null ) {
			mCacheBitmap = null;
		}
	}
	private int incCacheEnabledCount() throws TJSException {
		mCacheEnabledCount++;
		if(mCacheEnabledCount != 0 ) {
			registerCompactEventHook();
				// register to compact event hook to call CompactCache when idle
			allocateCache();
		}
		return mCacheEnabledCount;
	}
	private int decCacheEnabledCount() {
		mCacheEnabledCount--;
		if( FreeUnusedLayerCache && mCacheEnabledCount == 0)
			deallocateCache();
			// object is not freed until compact event, unless TVPFreeUnusedLayerCache flags
		return mCacheEnabledCount;
	}
	void setCached(boolean b) throws TJSException {
		if(b != mCached) {
			mCached = b;
			if(b)
				incCacheEnabledCount();
			else
				decCacheEnabledCount();
		}
	}
	private void registerCompactEventHook() {
		if(!mCompactEventHookInit) {
			TVP.EventManager.addCompactEventHook(this);
			mCompactEventHookInit = true;
		}
	}
	private void updateTransDestinationOnSelfUpdate( final ComplexRect region ) {
		if( mTransDest != null && mTransDest.mInTransition && mTransDest.mTransSelfUpdate ) {
			// transition, its update is performed by user code, is processing on
			// transition destination.
			// update the transition destination as transition source does.
			switch( mTransDest.mTransUpdateType ) {
			case tutDivisibleFade: {
				ComplexRect cp = new ComplexRect(region);
				try {
					mTransDest.update(cp, true);
				} finally {
					cp.clear();
					cp = null; // プール化する時回収する
				}
				break;
			}
			default:
				mTransDest.update(true);
					// update entire area of the transition destination
					// because we cannot determine where the update affects.
				break;
			}
		}
	}
	private void updateTransDestinationOnSelfUpdate(final Rect rect) {
		// essentially the same as UpdateTransDestinationOnSelfUpdate(const tTVPComplexRect &region)
		if( mTransDest != null && mTransDest.mInTransition && mTransDest.mTransSelfUpdate ) {
			switch( mTransDest.mTransUpdateType)
			{
			case tutDivisibleFade:
				mTransDest.update(rect, true);
				break;
			default:
				mTransDest.update(true);
				break;
			}
		}
	}
	private void updateChildRegion( LayerNI child, final ComplexRect region, boolean tempupdate, boolean targvisible, boolean addtoprimary) {
		// called by child.  add update rect subscribed in "rect"
		Rect cr = new Rect();
		cr.left = cr.top = 0;
		cr.right = mRect.width();
		cr.bottom = mRect.height();

		ComplexRect converted = new ComplexRect();
		try {
			converted.copyWithOffsets(region, cr, child.mRect.left, child.mRect.top);

			if(!tempupdate) {
				if(getCacheEnabled()){
					// caching is enabled
					if(targvisible) {
						mCacheRecalcRegion.or(converted);
						if(mCacheRecalcRegion.getCount() > CACHE_UNITE_LIMIT)
							mCacheRecalcRegion.unite();
					}
				}
			}

			if(mParent!=null){
				mParent.updateChildRegion(this, converted, tempupdate, targvisible, addtoprimary);
			} else {
				if(addtoprimary) if(mManager!=null) mManager.addUpdateRegion(converted);
			}

			updateTransDestinationOnSelfUpdate(converted);
		} finally {
			converted.clear();
			converted = null; // プール化する時回収する
		}
	}
	private void internalUpdate( final Rect rect, boolean tempupdate ) {
		Rect cr = new Rect();
		cr.left = cr.top = 0;
		cr.right = mRect.width(); cr.bottom = mRect.height();
		if(!Rect.intersectRect(cr, cr, rect)) return;

		if(!tempupdate) {
			if( getCacheEnabled() ) {
				// caching is enabled
				mCacheRecalcRegion.or(cr);
				if(mCacheRecalcRegion.getCount() > CACHE_UNITE_LIMIT)
					mCacheRecalcRegion.unite();
			}
		}

		if(mParent!=null) {
			ComplexRect c = new ComplexRect();
			try {
				c.or(cr);
				mParent.updateChildRegion(this, c, tempupdate, getVisible(), getNodeVisible());
			} finally {
				c.clear();
				c = null; // プール化する時回収する
			}
		} else {
			if(mManager!=null) mManager.addUpdateRegion(cr);
		}

		updateTransDestinationOnSelfUpdate(cr);
	}
	public void update() { update(false); }
	public void update(boolean tempupdate) {
		// update entire of the layer
		Rect rect = new Rect();
		rect.left = rect.top = 0;
		rect.right = mRect.width();
		rect.bottom = mRect.height();
		internalUpdate(rect, tempupdate);
	}
	private void update( ComplexRect rects ) {
		update( rects, false );
	}
	private void update( ComplexRect rects, boolean tempupdate ) {
		Rect cr = new Rect();
		cr.left = cr.top = 0;
		cr.right = mRect.width();
		cr.bottom = mRect.height();
		rects.and(cr);

		if( rects.getCount() == 0 ) return;

		if(!tempupdate) {
			// in case of tempupdate == false
			/*
				tempupdate == true indicates that the layer content is not changed,
				but the layer need to be updated to the window.
				Mainly used by transition update.

				There is no need to update CacheRecalcRegion because the
				layer content is not changed when tempupdate == true.
			*/
			if( getCacheEnabled() ) {
				// caching is enabled
				mCacheRecalcRegion.or(rects);
				if( mCacheRecalcRegion.getCount() > CACHE_UNITE_LIMIT)
					mCacheRecalcRegion.unite();
			}
		}

		if( mParent != null ) {
			mParent.updateChildRegion(this, rects, tempupdate, getVisible(), getNodeVisible());
		} else {
			if(mManager!=null) mManager.addUpdateRegion(rects);
		}

		updateTransDestinationOnSelfUpdate(rects);
	}

	private void update( final Rect rect, boolean tempupdate ) {
		// update part of the layer
		Rect cr = new Rect();
		cr.left = cr.top = 0;
		cr.right = mRect.width();
		cr.bottom = mRect.height();
		if(!Rect.intersectRect(cr, cr, rect)) return;
		internalUpdate(cr, tempupdate);
	}
	private void notifyChildrenVisualStateChanged() {
		mVisibleChildrenCount = -1;
		setToCreateExposedRegion(); // in geographical management
		if(mManager != null ) mManager.notifyVisualStateChanged();
	}
	private void childChangeOrder( int from, int to ) {
		// called from children; change child's order from "from" to "to"
		// given orders are treated as orders before re-ordering.
		if(from == to) return; // no action
		LayerNI fromlay;
		ComplexRect rects = new ComplexRect();
		try {
			mChildren.compact();
			Rect r = new Rect();
			if(from < to) {
				// forward

				// rotate
				fromlay = mChildren.get(from);
				for( int i = from; i < to; i++ ) {
					mChildren.set(i,mChildren.get(i + 1) );
					r.set(fromlay.mRect);
					if( Rect.intersectRect( r, r, mChildren.get(i).mRect) )
						rects.or(r); // add rectangle to update
				}
				mChildren.set( to, fromlay );
			} else {
				// backward
				// rotate
				fromlay = mChildren.get(from);
				for( int i = from; i > to; i-- ) {
					mChildren.set( i, mChildren.get(i - 1) );
					r.set(fromlay.mRect);
					if( Rect.intersectRect( r, r, mChildren.get(i).mRect) )
						rects.or(r);
				}
				mChildren.set( to, fromlay );
			}

			// update
			update(rects);
		} finally {
			rects.clear();
			rects = null;
		}

		// clear caches
		mChildrenArrayValid = false;
		mChildrenOrderIndexValid = false;
		if(mManager != null ) mManager.invalidateOverallIndex();
	}
	private void childChangeAbsoluteOrder( int from, int abs_to ) {
		// find index order
		int to = 0;
		final int count = mChildren.getCount();
		for( int i = 0; i < count; i++ ) {
			LayerNI child = mChildren.get(i);
			if( child != null ) {
				if(child.mAbsoluteOrderIndex >= abs_to) break;
				to++;
			}
		}

		if(from<to) to--;
		childChangeOrder(from, to);
	}
	public int getAbsoluteOrderIndex() {
		// retrieve order index in absolute position
		if( mParent == null ) return 0;
		if( mParent.mAbsoluteOrderMode )
			return mAbsoluteOrderIndex;
		return getOrderIndex();
	}
	public void setAbsoluteOrderIndex(int index) throws TJSException {
		if( mParent == null ) Message.throwExceptionMessage(Message.CannotMovePrimaryOrSiblingless);
		mParent.setAbsoluteOrderMode(true);
		mParent.childChangeAbsoluteOrder( getOrderIndex(), index);
		mAbsoluteOrderIndex = index;
	}
	public void setAbsoluteOrderMode(boolean b) {
		// set absolute order index mode
		if( mAbsoluteOrderMode != b ) {
			mAbsoluteOrderMode = b;
			if(b) {
				// to absolute order mode
				final int count = mChildren.getCount();
				for( int i = 0; i < count; i++ ) {
					LayerNI child = mChildren.get(i);
					if( child != null ) {
						child.mAbsoluteOrderIndex = child.getOrderIndex();
					}
				}
			} else {
				// to relative order mode
				// nothing to do
			}
		}
	}
	public int getOrderIndex() {
		if( mParent == null ) return 0;
		if( mParent.mChildrenOrderIndexValid ) return mOrderIndex;
		mParent.recreateOrderIndex();
		return mOrderIndex;
	}
	private void setToCreateExposedRegion() { mExposedRegionValid = false; }
	private void recreateOrderIndex() {
		// recreate order index information
		int index = 0;
		final int count = mChildren.getCount();
		for( int i = 0; i < count; i++ ) {
			LayerNI child = mChildren.get(i);
			if( child != null ) {
				child.mOrderIndex = index;
				index++;
			}
		}
		mChildrenOrderIndexValid = true;
	}

	public boolean isAncestor( LayerNI ancestor ) {
		// is "ancestor" is ancestor of this layer ? (cannot be itself)
		LayerNI p = mParent;
		while( p != null ) {
			if( p == ancestor ) return true;
			p = p.mParent;
		}
		return false;
	}
	// is "ancestor" is ancestor of this layer ? (cannot be itself)
	public boolean isAncestorOrSelf( LayerNI ancestor ) {
		return ancestor == this || isAncestor(ancestor);
	}
	// same as IsAncestor (but can be itself)
	private int getOverallOrderIndex() {
		if( mManager == null ) return 0;
		mManager.recreateOverallOrderIndex();
		return mOverallOrderIndex;
	}
	private LayerNI getNeighborBelow(boolean loop) {
		if( mManager == null ) return null;

		int index = getOverallOrderIndex();
		ArrayList<LayerNI> allnodes = mManager.getAllNodes();

		final int count = allnodes.size();
		if( count == 0) return null; // must be an error !!

		if( index == (count -1) ) {
			// last
			if(loop)
				return allnodes.get(0);
			else
				return null;
		}

		return allnodes.get(index +1);
	}
	private LayerNI getNextFocusableInternal() {
		// search next focusable layer forward
		LayerNI p = this;
		LayerNI current = this;

		p = p.getNeighborBelow(true);
		if(current == p) return null;
		if( p == null ) return null;
		current = p;
		do {
			if(p.getNodeFocusable() && p.mJoinFocusChain ) return p; // next focusable layer found
			p = p.getNeighborBelow(true);
		} while( p != null && p != current);

		return null; // no layer found
	}

	public LayerNI getNextFocusable() {
		// search next focusable layer forward
		mFocusWork = getNextFocusableInternal();
		Dispatch2 owner = mOwner.get();
		if( owner != null && !mShutdown && (mFocusWork == null || mFocusWork.mOwner != null )) {
			final String eventname = "onSearchNextFocusable";
			Variant[] param = new Variant[1];
			if( mFocusWork != null )
				param[0] = new Variant( mFocusWork.mOwner.get(), mFocusWork.mOwner.get() );
			else
				param[0] = new Variant(null,null);
			TVP.EventManager.postEvent(owner, owner, eventname, 0, EventManager.EPT_IMMEDIATE, param);

		}
		return mFocusWork;
	}
	public void saveEnabledWork() {
		mEnabledWork = getNodeEnabled();

		final int count = mChildren.getCount();
		for( int i = 0; i < count; i++ ) {
			LayerNI child = mChildren.get(i);
			if( child != null ) {
				child.saveEnabledWork();
			}
		}
	}
	public boolean isDisabledByMode() {
		// is "this" layer disable by modal layer?
		if( mManager == null ) return false;
		LayerNI current = mManager.getCurrentModalLayer();
		if( current == null ) return false;
		return !isAncestorOrSelf(current);
	}
	public boolean parentEnabled() {
		LayerNI par = mParent;
		while( par != null ) {
			if(!par.mEnabled) { return false; }
			par = par.mParent;
		}
		return true;
	}
	public boolean getEnabled() { return mEnabled; }
	public boolean getNodeEnabled() {
		return getEnabled() && parentEnabled() && !isDisabledByMode();
	}
	public void notifyNodeEnabledState() {
		boolean en = getNodeEnabled();
		Dispatch2 owner = mOwner.get();
		if( owner != null && !mShutdown && mEnabledWork != en ) {
			if( en ) {
				final String eventname = "onNodeEnabled";
				TVP.EventManager.postEvent(owner, owner, eventname, 0, EventManager.EPT_IMMEDIATE, TJS.NULL_ARG);
			} else {
				final String eventname = "onNodeDisabled";
				TVP.EventManager.postEvent(owner, owner, eventname, 0, EventManager.EPT_IMMEDIATE, TJS.NULL_ARG);
			}
		}
		try {
			mChildren.safeLock();
			final int count = mChildren.getSafeLockedObjectCount();
			for( int i = 0; i < count; i++ ) {
				LayerNI child = mChildren.get(i);
				if( child != null ) {
					child.notifyNodeEnabledState();
				}
			}
			mChildrenArrayValid = false;
			mChildrenOrderIndexValid = false;
			if(mManager!=null) mManager.invalidateOverallIndex();
		} finally {
			mChildren.safeUnlock();
		}
	}
	private void allocateDefaultImage() throws TJSException {
		if( mMainImage == null )
			mMainImage = new BaseBitmap( TempBitmapHolder.get() );
		else
			mMainImage.assign( TempBitmapHolder.get() );

		mFontChanged = true; // invalidate font assignment cache
		resetClip();  // cliprect is reset

		mImageModified = true;
	}
	public BaseBitmap getMainImage() {
		applyFont();
		return mMainImage;
	}
	void resetClip() throws TJSException {
		if( mMainImage == null ) Message.throwExceptionMessage( Message.NotDrawableLayerType );

		mClipRect.left = mClipRect.top = 0;
		mClipRect.right = mMainImage.getWidth();
		mClipRect.bottom = mMainImage.getHeight();
	}
	private void applyFont() {
		if( mFontChanged && mMainImage != null ) {
			mFontChanged = false;
			mMainImage.setFont(mFont);
		}
	}
	public void setFontFace( final String face ) {
		if( !mFont.getFaceName().equals(face)  ) {
			mFont.setFaceName( face );
			mFontChanged = true;
		}
	}
	public String getFontFace() {
		return mFont.getFaceName();
	}
	public void setFontHeight( int height ) {
		if(height < 0) height = -height; // TVP2 does not support negative value of height

		if( mFont.getHeight() != height) {
			mFont.setHeight(height);
			mFontChanged = true;
		}
	}

	public int getFontHeight() {
		return mFont.getHeight();
	}

	public void setFontAngle( int angle ) {
		if( mFont.getAngle() != angle ) {
			angle = angle % 3600;
			if(angle < 0) angle += 3600;
			mFont.setAngle( angle );
			mFontChanged = true;
		}
	}

	public int getFontAngle() {
		return mFont.getAngle();
	}

	public void setFontBold( boolean b ) {
		if( mFont.getBold() != b ) {
			mFont.setBold( b );
			mFontChanged = true;
		}
	}

	public boolean getFontBold() 	{
		return mFont.getBold();
	}

	public void setFontItalic( boolean  b ) {
		if( mFont.getItalic() != b ) {
			mFont.setItalic( b );
			mFontChanged = true;
		}
	}

	public boolean getFontItalic() {
		return mFont.getItalic();
	}

	public void setFontStrikeout(boolean b) {
		if( mFont.getStrikeout() != b ) {
			mFont.setStrikeout( b );
			mFontChanged = true;
		}
	}

	public boolean getFontStrikeout() {
		return mFont.getStrikeout();
	}

	public void setFontUnderline(boolean b) {
		if( mFont.getUnderline()  != b ) {
			mFont.setUnderline( b );
			mFontChanged = true;
		}
	}

	public boolean getFontUnderline() {
		return mFont.getUnderline();
	}

	public void setVisible(boolean st) throws TJSException {
		if( mVisible != st ) {
			if( isPrimary() && !st)
				Message.throwExceptionMessage(Message.CannotSetPrimaryInvisible);
			if(!st) update();
			mVisible = st;
			if(st) update();
			if(mParent!=null) mParent.notifyChildrenVisualStateChanged();
			if( mVisible ) {
				if(mManager != null ) mManager.checkTreeFocusableState(this);
			} else {
				if(mManager != null ) mManager.blurTree(this); // in input/keyboard focus management

				if( PurgeOnHide ) purgeImage();
			}
		}
	}

	public void setOpacity(int opa) throws TJSException {
		if( mOpacity != opa ) {
			if( isPrimary() && opa!=255 )
				Message.throwExceptionMessage(Message.CannotSetPrimaryInvisible);
			mOpacity = opa;
			if(mParent!=null) mParent.notifyChildrenVisualStateChanged();
			update();
		}
	}

	public final int getLeft() { return mRect.left; }
	public final int getTop() { return mRect.top; }
	public final int getWidth() { return mRect.width(); }
	public final int getHeight() { return mRect.height(); }

	/**
	 *
	 * @param index 参照渡しの代わりに配列渡し
	 * @param nodes
	 */
	public void recreateOverallOrderIndex(int[] index, ArrayList<LayerNI> nodes) {
		mOverallOrderIndex = index[0];
		index[0]++;
		nodes.add(this);

		final int count = mChildren.getCount();
		for( int i = 0; i < count; i++ ) {
			LayerNI child = mChildren.get(i);
			if( child != null ) {
				child.recreateOverallOrderIndex(index, nodes);
			}
		}
	}

	public void queryUpdateExcludeRect(Rect rect, boolean parentvisible) {
		// query completely opaque area

		// convert to local coordinates
		rect.left -= mRect.left;
		rect.right -= mRect.left;
		rect.top -= mRect.top;
		rect.bottom -= mRect.top;

		// recur to children
		parentvisible = parentvisible && mVisible &&
			(mDisplayType == LayerType.ltOpaque || mDisplayType == LayerType.ltAlpha ||
					mDisplayType == LayerType.ltAddAlpha || mDisplayType == LayerType.ltPsNormal) &&
			mOpacity == 255; // fixed 2004/01/09 W.Dee

		final int count = mChildren.getCount();
		for( int i = count-1; i >= 0; i-- ) {
			LayerNI child = mChildren.get(i);
			if( child != null ) {
				child.queryUpdateExcludeRect(rect, parentvisible);
			}
		}

		// copy current update exclude rect
		if(parentvisible) mUpdateExcludeRect.set(rect);
		else mUpdateExcludeRect.clear();

		// convert to parent's coordinates
		rect.left += mRect.left;
		rect.right += mRect.left;
		rect.top += mRect.top;
		rect.bottom += mRect.top;

		// check visibility & opacity
		if(parentvisible && mDisplayType == LayerType.ltOpaque && mOpacity == 255) {
			if( rect.isEmpty() ) {
				rect.set( mRect );
			} else {
				if(!Rect.intersectRect( rect, rect, mRect) )
					rect.clear();
			}
		}
	}

	public boolean parentFocusable() {
		LayerNI par = mParent;
		while( par != null ) {
			if(!par.mVisible || !par.mEnabled) { return false; }
				// note that here we do not check parent's focusable state.
			par = par.mParent;
		}
		return true;
	}
	public boolean getNodeFocusable() {
		return checkFocusable() && parentFocusable() && !isDisabledByMode();
	}
	public boolean checkFocusable() { return mFocusable && mVisible && mEnabled; }

	public LayerNI fireBeforeFocus(LayerNI prevfocused, boolean direction ) {
		mFocusWork = this;
		Dispatch2 owner = mOwner.get();
		if( owner != null && !mShutdown ){
			final String eventname = "onBeforeFocus";
			Variant[] param = new Variant[3];
			param[0] = new Variant(owner, owner);
			if( prevfocused != null )
				param[1] = new Variant(prevfocused.mOwner.get(), prevfocused.mOwner.get());
			else
				param[1] = new Variant( null, null );
			param[2] = new Variant( direction ? 1 : 0 );
			TVP.EventManager.postEvent( owner, owner, eventname, 0, EventManager.EPT_IMMEDIATE, param);

		}
		return mFocusWork;
	}

	public void fireBlur(LayerNI prevfocused) {
		Dispatch2 owner = mOwner.get();
		if( owner != null && !mShutdown ) {
			final String eventname = "onBlur";
			Variant[] param = new Variant[1];
			if(prevfocused != null )
				param[0] = new Variant(prevfocused.mOwner.get(), prevfocused.mOwner.get());
			else
				param[0] = new Variant( null, null );
			TVP.EventManager.postEvent(owner, owner, eventname, 0, EventManager.EPT_IMMEDIATE, param);
		}
	}

	public void fireFocus(LayerNI prevfocused, boolean direction) {
		Dispatch2 owner = mOwner.get();
		if( owner != null && !mShutdown ) {
			final String eventname = "onFocus";
			Variant[] param = new Variant[2];
			if( prevfocused != null )
				param[0] = new Variant(prevfocused.mOwner.get(), prevfocused.mOwner.get());
			else
				param[0] = new Variant( null, null );
			param[1] = new Variant( direction ? 1 : 0 );
			TVP.EventManager.postEvent( owner, owner, eventname, 0, EventManager.EPT_IMMEDIATE, param );
		}
	}

	public void toPrimaryCoordinates(Point pt) {
		LayerNI l = this;
		while( l != null && !l.isPrimary() ) {
			pt.x += l.mRect.left;
			pt.y += l.mRect.top;
			l = l.mParent;
		}
	}

	public void completeForWindow( Drawable drawable ) throws VariantException, TJSException {
		beforeCompletion();

		if( mManager != null ) mManager.notifyUpdateRegionFixed();

		mInCompletion = true;

		if( mManager != null ) mManager.getWindow().startBitmapCompletion(mManager);
		try {
			internalComplete2( mManager.getUpdateRegionForCompletion(), drawable);
		} finally {
			if( mManager != null ) mManager.getWindow().endBitmapCompletion(mManager);
		}
		mInCompletion = false;
		afterCompletion();
	}
	private void beforeCompletion() throws VariantException, TJSException {
		// called before the drawing is processed
		if(mInCompletion) return;
			// calling during completion more than once is not allowed


		// fire onPaint
		if(mCallOnPaint) {
			mCallOnPaint = false;
			final String eventname = "onPaint";
			Dispatch2 owner = mOwner.get();
			TVP.EventManager.postEvent( owner, owner, eventname, 0, EventManager.EPT_IMMEDIATE, TJS.NULL_ARG );
		}

		// for transition
		if(mInTransition) {
			// transition is processing

			if(mDivisibleTransHandler!=null) {
				// notify start of processing unit
				int er;
				if( mTransSelfUpdate ) {
					// set TransTick here if the transition is performed by user code;
					// otherwise the TransTick is to be set at
					// InvokeTransition
					mTransTick = getTransTick();
				}
				er = mDivisibleTransHandler.startProcess(mTransTick);
				if(er != Error.S_TRUE) stopTransitionByHandler();
			} else if( mGiveUpdateTransHandler != null ) {
				// not yet implemented
			}
		}

		if( mInTransition && mTransWithChildren && mTransUpdateType == tutDivisible ) {
			// complete all area of the layer
			mInTransition = false; // cheat!!!
			mTransDrawable.Src1Bmp = complete();
			mInTransition = true;

			if( mTransSrc != null ) mTransDrawable.Src2Bmp = mTransSrc.complete();
		}

		try {
			mChildren.safeLock();
			final int count = mChildren.getSafeLockedObjectCount();
			for( int i = 0; i < count; i++ ) {
				LayerNI child = mChildren.get(i);
				if( child != null ) child.beforeCompletion();
			}
			mChildrenArrayValid = false;
			mChildrenOrderIndexValid = false;
			if(mManager!=null) mManager.invalidateOverallIndex();
		} finally {
			mChildren.safeUnlock();
		}
	}
	private long getTransTick() throws VariantException, TJSException {
		if( !mUseTransTickCallback) {
			// just use TVPGetTickCount() as a source
			return TVP.getTickCount();
		} else {
			// call TransTickCallback to receive result
			Variant res = new Variant();
			mTransTickCallback.funcCall(0, null, res, null, null );
			return res.asInteger();
		}
	}

	private void afterCompletion() throws TJSException {
		// called after the drawing is processed
		if(mInCompletion) return;
			// calling during completion more than once is not allowed

		if(mInTransition) {
			// transition is processing
			if( mDivisibleTransHandler != null ){
				// notify start of processing unit
				int er = mDivisibleTransHandler.endProcess();
				if(er != Error.S_TRUE) stopTransitionByHandler();
			} else if( mGiveUpdateTransHandler != null ){
				// not yet implemented
			}
		}
		try {
			mChildren.safeLock();
			final int count = mChildren.getSafeLockedObjectCount();
			for( int i = 0; i < count; i++ ) {
				LayerNI child = mChildren.get(i);
				if( child != null ) child.afterCompletion();
			}
			mChildrenArrayValid = false;
			mChildrenOrderIndexValid = false;
			if(mManager!=null) mManager.invalidateOverallIndex();
		} finally {
			mChildren.safeUnlock();
		}
	}
	private void stopTransitionByHandler() throws TJSException {
		// stopping of the transition caused by the handler
		if( !TVP.EventManager.isEventDisabled() ) {
			// event dispatching is enabled
			internalStopTransition();
		} else {
			// event dispatching is not enabled
			mTransCompEventPrevented = true;
		}
	}
	@Override
	public void onCompact(int level) throws TJSScriptException, TJSScriptError, TJSException {
		if(level >= CompactEventCallbackInterface.COMPACT_LEVEL_DEACTIVATE) compactCache();
	}
	private void compactCache() {
		// free cache image if the cache is not needed
		if(mCacheEnabledCount == 0) deallocateCache();
	}
	void setWidth( int width ) throws TJSException {
		if( mRect.width() != width ) {
			update(false);
			mRect.setWidth(width);
			if( mParent != null ) mParent.notifyChildrenVisualStateChanged();
			setToCreateExposedRegion();
			imageLayerSizeChanged();
			update(false);
			if( isPrimary() && mManager != null ) mManager.notifyLayerResize();
		}
	}

	void setHeight( int height ) throws TJSException {
		if( mRect.height() != height ) {
			update(false);
			mRect.setHeight(height);
			if( mParent != null ) mParent.notifyChildrenVisualStateChanged();
			setToCreateExposedRegion();
			imageLayerSizeChanged();
			update(false);
			if(isPrimary() && mManager != null ) mManager.notifyLayerResize();
		}
	}
	public void setSize( int width, int height ) throws TJSException {
		internalSetSize(width, height);
	}
	void internalSetSize( int width, int height ) throws TJSException {
		if( mRect.width() != width || mRect.height() != height ) {
			update(false);
			mRect.setWidth(width);
			mRect.setHeight(height);
			if( mParent != null ) mParent.notifyChildrenVisualStateChanged();
			setToCreateExposedRegion();
			imageLayerSizeChanged();
			update(false);
			if(isPrimary() && mManager != null ) mManager.notifyLayerResize();
		}
	}
	private void imageLayerSizeChanged() throws TJSException {
		// called from geographical management
		if( mMainImage == null ) return;

		if( mMainImage.getWidth() < mRect.width() ) {
			changeImageSize( mRect.width(), mMainImage.getHeight() );
		}
		if( (mMainImage.getWidth() + mImageLeft) < mRect.width() ) {
			mImageLeft = mRect.width() - mMainImage.getWidth();
			update();
		}

		if( mMainImage.getHeight() < mRect.height() ) {
			changeImageSize( mMainImage.getWidth(), mRect.height() );
		}
		if( (mMainImage.getHeight() + mImageTop) < mRect.height() ) {
			mImageTop = mRect.height() - mMainImage.getHeight();
			update();
		}
	}
	private void changeImageSize( int width, int height ) throws TJSException {
		// be called from geographical management
		if( width == 0 || height == 0 )
			Message.throwExceptionMessage( Message.CannotCreateEmptyLayerImage );

		if( mMainImage != null ) mMainImage.setSizeWithFill( width, height, mNeutralColor );
		if( mProvinceImage != null ) mProvinceImage.setSizeWithFill(width, height, 0);

		if( mMainImage != null ) resetClip();  // cliprect is reset

		mImageModified = true;

		resizeCache(); // in cache management
	}
	private void resizeCache() throws TJSException {
		// resize to Rect's size
		if( mCacheBitmap != null && mMainImage != null ) {
			mCacheBitmap.setSize( mMainImage.getWidth(), mMainImage.getHeight() );
		}
		mCacheRecalcRegion.or( new Rect(0, 0, mRect.width(), mRect.height()) );
	}
	private void internalSetImageSize( int width, int height ) throws TJSException {
		// adjust position
		if( width < mRect.width() ) {
			mImageLeft = 0;
			setWidth(width); // change layer size
		} if( (width + mImageLeft) < mRect.width() ) {
			mImageLeft = mRect.width() - width;
		}

		if( height < mRect.height() ) {
			mImageTop = 0;
			setHeight(height); // change layer size
		}
		if( (height + mImageTop) < mRect.height() ) {
			mImageTop = mRect.height() - height;
		}

		changeImageSize(width, height);
	}
	private void allocateProvinceImage() throws TJSException {
		int neww = mMainImage != null ? mMainImage.getWidth() : mRect.width();
		int newh = mMainImage != null ? mMainImage.getHeight() : mRect.height();

		if(mProvinceImage == null ) {
			mProvinceImage = new BaseBitmap(neww, newh, 8);
			mProvinceImage.fill( new Rect(0, 0, neww, newh), 0);
		} else {
			mProvinceImage.setSizeWithFill(neww, newh, 0);
		}
		mImageModified = true;
	}
	private void deallocateProvinceImage() {
		if( mProvinceImage != null ) mProvinceImage = null;
		mImageModified = true;
	}
	public Dispatch2 loadImages( String name, int colorkey ) throws TJSException {
		if( mMainImage == null ) Message.throwExceptionMessage(Message.NotDrawableLayerType);

		// 読み込み前に以前の画像は破棄してしまう
		purgeImage();

		String[] provincename = new String[1];
		Holder<Dispatch2> holder = new Holder<Dispatch2>(null);
		GraphicsLoader.loadGraphic( mMainImage, name, colorkey, 0, 0, GraphicsLoader.glmNormal, provincename, holder ); //, mDisplayType==LayerType.ltAddAlpha );
		Dispatch2 metainfo = holder.mValue;

		internalSetImageSize( mMainImage.getWidth(), mMainImage.getHeight() );
		if( provincename[0] != null && provincename[0].length() != 0 ) {
			// province image exists
			allocateProvinceImage();
			try {
				GraphicsLoader.loadGraphic( mProvinceImage, provincename[0], 0,
					mMainImage.getWidth(), mMainImage.getHeight(), GraphicsLoader.glmPalettized, null, null );

				if( mProvinceImage.getWidth() != mMainImage.getWidth() ||
					mProvinceImage.getHeight() != mMainImage.getHeight() )
					Message.throwExceptionMessage(Message.ProvinceSizeMismatch, provincename[0]);
			} catch(Exception e) {
				deallocateProvinceImage();
				throw new TJSException(e.getMessage());
			}
		} else {
			// province image does not exist
			deallocateProvinceImage();
		}

		mImageModified = true;
		resetClip();  // cliprect is reset
		update(false);

		return metainfo;
	}
	public boolean getCacheEnabled() { return mCacheEnabledCount!=0; }
	private int getVisibleChildrenCount() {
		if(mVisibleChildrenCount == -1) checkChildrenVisibleState();
		return mVisibleChildrenCount;
	}
	public int getCount() { return mChildren.getActualCount(); }
	public boolean getVisible() { return mVisible; }
	public int getOpacity() { return mOpacity; }

	private void checkChildrenVisibleState() {
		if( getCount() == 0 ) {
			mVisibleChildrenCount = 0;
			return;
		}
		mVisibleChildrenCount = 0;

		final int count = mChildren.getCount();
		for( int i = 0; i < count; i++ ) {
			LayerNI child = mChildren.get(i);
			if( child != null ) {
				if(child.getVisible() && child.getOpacity() != 0)
					mVisibleChildrenCount++;
			}
		}
	}
	private static int internalComplete2direction = 0;
	private void internalComplete2( ComplexRect updateregion, Drawable drawable ) throws VariantException, TJSException {
		//--- querying phase

		// search ltOpaque, not to draw region behind them.
		if(mManager!=null) mManager.queryUpdateExcludeRect();

		//--- drawing phase

		// split the region to some stripes to utilize the CPU's memory
		// caching.

		//int i;
		ComplexRect.Iterator it = updateregion.getIterator();
		while( it.step() ) {
			Rect r = new Rect(it.get());

			// Add layer offset because Draw() accepts the position in
			// *parent* 's coordinates.
			r.addOffsets(mRect.left, mRect.top);

			if( GraphicSplitOperationType != gsotNone ) {
				// compute optimum height of the stripe
				int oh;

				if( getVisibleChildrenCount() != 0 || mInTransition ) {
					// split
					int rw = r.width();
					if(rw < 40) oh = 128;
					else if(rw < 80) oh = 64;
					else if(rw < 160) oh = 32;
					else if(rw < 320) oh = 16;
					else oh = 8;
				} else {
					// no need to split
					oh = r.height();
				}

				// split to some stripes
				int y;
				Rect or = new Rect();
				or.left = r.left;
				or.right = r.right;
				if( GraphicSplitOperationType == gsotInterlace ) {
					// interlaced split
					for(y = r.top; y < r.bottom; y+= oh*2) {
						or.top = y;
						or.bottom = (y+oh < r.bottom) ? y+oh: r.bottom;

						// call "Draw" to draw to the window
						draw(drawable, or, false);
					}
					for(y = r.top + oh; y < r.bottom; y+= oh*2) {
						or.top = y;
						or.bottom = (y+oh < r.bottom) ? y+oh: r.bottom;

						// call "Draw" to draw to the window
						draw(drawable, or, false);
					}
				} else if( GraphicSplitOperationType == gsotSimple ) {
					// non-interlaced
					for(y = r.top; y < r.bottom; y+=oh) {
						or.top = y;
						or.bottom = (y+oh < r.bottom) ? y+oh: r.bottom;

						// call "Draw" to draw to the window
						draw(drawable, or, false);
					}
				} else if( GraphicSplitOperationType == gsotBiDirection ) {
					// bidirection
					//final int direction = 0;
					if( (internalComplete2direction & 1) != 0 ) {
						for(y = r.top; y < r.bottom; y+=oh) {
							or.top = y;
							or.bottom = (y+oh < r.bottom) ? y+oh: r.bottom;

							// call "Draw" to draw to the window
							draw(drawable, or, false);
						}
					} else {
						y = r.bottom - oh;
						if(y < r.top) y = r.top;
						while( true ) {
							or.top = (y < r.top ? r.top : y);
							or.bottom = (y+oh < r.bottom) ? y+oh: r.bottom;

							if(or.bottom <= r.top) break;

							// call "Draw" to draw to the window
							draw(drawable, or, false);

							y-=oh;
						}
					}
					internalComplete2direction++;
				}
			} else {
				// don't split scanlines
				draw(drawable, r, false);
			}
		}

		updateregion.clear();
	}
	private void internalComplete( ComplexRect updateregion, Drawable drawable ) throws VariantException, TJSException {
		beforeCompletion();

		// at this point, final update region (in this completion) is determined
		mInCompletion = true;

		internalComplete2(updateregion, drawable);

		mInCompletion = false;
		afterCompletion();
	}
	static class CompleteDrawable implements Drawable {
		BaseBitmap Bitmap;
		//Rect BitmapRect;
		int LayerType;
		public CompleteDrawable( BaseBitmap bmp, int layertype ) {
			Bitmap = bmp;
			LayerType = layertype;
			//BitmapRect = new Rect();
		}

		public BaseBitmap getDrawTargetBitmap( final Rect rect, Rect cliprect) {
			cliprect.set( rect );
			return Bitmap;
		}
		public int getTargetLayerType() { return LayerType; }
		public void drawCompleted( final Rect destrect, BaseBitmap bmp, final Rect cliprect, int type, int opacity ) throws TJSException {
			if( bmp != Bitmap ) {
				Bitmap.copyRect(destrect.left, destrect.top, bmp, cliprect);
			}
		}
	}
	private BaseBitmap complete( final Rect rect ) throws VariantException, TJSException {

		// complete given rectangle of cache.
		if(!getCacheEnabled()) return null;
			// caller must ensure that the caching is enabled

		if( getVisibleChildrenCount() == 0 && mImageLeft == 0 && mImageTop == 0 &&
			mMainImage.getWidth() == getWidth() && mMainImage.getHeight() == getHeight() ) {
			// the layer has no visible children
			// and entire of the bitmap is the visible area.
			// simply returns main image
			return mMainImage;
		}

		if( mCacheRecalcRegion.getCount() == 0 ) {
			// the layer has not region to reconstruct
			return mCacheBitmap;
		}

		// create drawable object
		CompleteDrawable drawable = new CompleteDrawable(mCacheBitmap, mDisplayType);

		// complete
		ComplexRect ur = new ComplexRect();
		try {
			ur.or(rect);
			internalComplete( ur, drawable ); // complete cache
		} finally {
			drawable = null;
			ur.clear();
			ur = null;
		}

		return mCacheBitmap;
	}

	BaseBitmap complete() throws VariantException, TJSException {
		// complete entire area of the layer
		Rect r = new Rect();
		r.left = 0;
		r.top = 0;
		r.right = mRect.width();
		r.bottom = mRect.height();
		return complete(r);
	}
	public boolean isSeen() { return mVisible && mOpacity != 0; }
	private void parentRectToChildRect( Rect rect ) {
		// note that this function does not convert transformed layer coordinates.
		rect.left -= mRect.left;
		rect.right -= mRect.left;
		rect.top -= mRect.top;
		rect.bottom -= mRect.top;
	}
	private void drawSelf( Drawable target, Rect pr, Rect cr ) throws VariantException, TJSException {
		if( mMainImage == null ) {
			if( mDisplayType == LayerType.ltOpaque ) {
				// fill destination with specified color
				BaseBitmap temp = TempBitmapHolder.getTemp( cr.width(), cr.height() );
				try {
					// do transition
					Rect bitmaprect = new Rect(cr);
					bitmaprect.setOffsets(0, 0);
					copySelf(temp, 0, 0, bitmaprect); // this fills temp with neutral color

					// send completion message
					target.drawCompleted(pr, temp, bitmaprect, mDisplayType, mOpacity);
				} finally {
					TempBitmapHolder.freeTemp();
				}
			}
			return;
		}

		// draw self MainImage(only) to target
		cr.addOffsets(-mImageLeft, -mImageTop);

		if( mInTransition && !mTransWithChildren && mDivisibleTransHandler != null ) {
			// transition without children

			// allocate temporary bitmap
			BaseBitmap temp = TempBitmapHolder.getTemp( cr.width(), cr.height() );
			try {
				// do transition
				Rect bitmaprect = new Rect(cr);
				bitmaprect.setOffsets(0, 0);
				doDivisibleTransition(temp, 0, 0, cr);
				// send completion message
				target.drawCompleted(pr, temp, bitmaprect, mDisplayType, mOpacity );
			} finally {
				TempBitmapHolder.freeTemp();
			}
		} else {
			target.drawCompleted(pr, mMainImage, cr, mDisplayType, mOpacity);
		}
	}
	private void copySelfForRect( BaseBitmap dest, int destx, int desty, final Rect srcrect ) throws TJSException {
		// copy self image to the target
		Rect cr = new Rect(srcrect);
		cr.addOffsets(-mImageLeft, -mImageTop);

		if( mInTransition && !mTransWithChildren && mDivisibleTransHandler != null ) {
			// transition without children
			doDivisibleTransition(dest, destx, desty, cr);
		} else {
			if( mMainImage != null ) {
				dest.copyRect(destx, desty, mMainImage, cr);
			} else {
				// main image does not exist
				// fill destination with TransparentColor
				// (this need to be transparent, so we do not use NeutralColor which can be
				//  set by the user unless the DisplayType is ltOpaque)
				dest.fill( new Rect(destx, desty, destx + cr.width(), desty + cr.height()),
						mDisplayType == LayerType.ltOpaque ? mNeutralColor : mTransparentColor);
			}
		}
	}
	private void copySelf( BaseBitmap dest, int destx, int desty, final Rect r ) throws TJSException {
		final Rect uer = mUpdateExcludeRect;
		if(uer.isEmpty()) {
			copySelfForRect(dest, destx, desty, r);
		} else {
			if(uer.top <= r.top && uer.bottom >= r.bottom) {
				if(uer.left > r.left && uer.right < r.right) {
					// split into two
					Rect r2 = new Rect(r);
					r2.right = uer.left;
					copySelfForRect(dest, destx, desty, r2);
					r2.right = r.right;
					r2.left = uer.right;
					copySelfForRect(dest, destx + (r2.left - r.left), desty, r2);
				} else if(r.left >= uer.left && r.right <= uer.right) {
					;// nothing to do
				} else if(r.right <= uer.left || r.left >= uer.right) {
					copySelfForRect(dest, destx, desty, r);
				} else if(r.right > uer.left && r.right <= uer.right) {
					Rect r2 = new Rect(r);
					r2.right = uer.left;
					copySelfForRect(dest, destx, desty, r2);
				} else if(r.left >= uer.left && r.left < uer.left) {
					Rect r2 = new Rect(r);
					r2.left = uer.right;
					copySelfForRect(dest, destx + (r2.left - r.left), desty, r2);
				} else {
					copySelfForRect(dest, destx, desty, r);
				}
			} else {
				copySelfForRect(dest, destx, desty, r);
			}
		}
	}
	private void doDivisibleTransition( BaseBitmap dest, int dx, int dy, final Rect srcrect ) throws TJSException {
		// apply transition ( with no children ) over given target bitmap
		if(!mInTransition || mDivisibleTransHandler == null ) return;

		DivisibleData data = new DivisibleData();
		data.Left = srcrect.left;
		data.Top = srcrect.top;
		data.Width = srcrect.width();
		data.Height = srcrect.height();

		// src1
		if(mSrcSLP == null)
			mSrcSLP = new ScanLineProviderForBaseBitmap(mMainImage);
		else
			mSrcSLP.attach(mMainImage);
		data.Src1 = mSrcSLP;
		data.Src1Left = srcrect.left;
		data.Src1Top = srcrect.top;
		mImageModified = true;

		// src2
		if( mTransSrc != null ) {
			// source available
			if( mTransSrc.mSrcSLP == null )
				mTransSrc.mSrcSLP = new ScanLineProviderForBaseBitmap(mTransSrc.mMainImage);
			else
				mTransSrc.mSrcSLP.attach(mTransSrc.mMainImage);

			data.Src2 = mTransSrc.mSrcSLP;
			data.Src2Left = srcrect.left;
			data.Src2Top = srcrect.top;
		}

		// dest
		if( mDestSLP == null )
			mDestSLP = new ScanLineProviderForBaseBitmap(dest);
		else
			mDestSLP.attach(dest);
		data.Dest = mDestSLP;
		data.DestLeft = dx;
		data.DestTop = dy;

		// process
		mDivisibleTransHandler.process(data);

		if( data.Dest == data.Src1 ) {
			// returned destination differs from given destination
			// (returned destination is data.Src1)
			dest.copyRect(dx, dy, mMainImage, srcrect);
		} else if(data.Dest == data.Src2) {
			// (returned destination is data.Src2)
			dest.copyRect(dx, dy, mTransSrc.mMainImage, srcrect);
		}
	}

	private void createExposedRegion() {
		// create exposed/overlapped region information

		// find region which is not piled by any children
		mExposedRegion.clear();
		mOverlappedRegion.clear();

		Rect rect = new Rect();
		rect.left = rect.top = 0;
		rect.right = mRect.width();
		rect.bottom = mRect.height();

		if( mMainImage != null ) {
			// the layer has image

			if( getVisibleChildrenCount() > EXPOSED_UNITE_LIMIT ) {
				mExposedRegion.or(rect);

				boolean first = true;

				Rect r2 = new Rect();

				final int count = mChildren.getCount();
				for( int i = 0; i < count; i++ ) {
					LayerNI child = mChildren.get(i);
					if( child != null ) {
						if(child.isSeen())
						{
							Rect r = new Rect(child.getRect());
							if(Rect.intersectRect(r, r, rect)) {
								if(first) {
									r2.set( child.getRect() );
									first = false;
								} else {
									Rect.unionRect( r2, r2, r);
								}
							}
						}
					}
				}

				mOverlappedRegion.or(r2);
				mExposedRegion.sub(mOverlappedRegion);
			} else {
				Rect rect1 = new Rect();
				rect1.left = rect1.top = 0;
				rect1.right = mRect.width();
				rect1.bottom = mRect.height();
				mExposedRegion.or(rect1);

				final int count = mChildren.getCount();
				for( int i = 0; i < count; i++ ) {
					LayerNI child = mChildren.get(i);
					if( child != null ) {
						if(child.isSeen()) {
							Rect r = new Rect(child.getRect());
							if(Rect.intersectRect(r, r, rect1))
								mOverlappedRegion.or(r);
						}
					}
				}
				mExposedRegion.sub(mOverlappedRegion);
			}
		} else {
			// the layer has no image
			// ExposedRegion : child layer can directly transfer the image to the parent's target
			// OverlappedRegion : Inverse of ExposedRegion

			mExposedRegion.clear();
			mOverlappedRegion.clear();
			mOverlappedRegion.or(rect);

			// ExposedRegion is a region with is only one child layer piled
			// under the parent layer.
			// Recalculating this is pretty high-cost operation,
			if( getVisibleChildrenCount() < DIRECT_UNITE_LIMIT ) {
				ComplexRect one = mExposedRegion; // alias of ExposedRegion
				ComplexRect two = new ComplexRect(); // region which is more than two layers piled

				try {
					final int count = mChildren.getCount();
					for( int i = 0; i < count; i++ ) {
						LayerNI child = mChildren.get(i);
						if( child != null ) {
							if( child.isSeen() ) {
								Rect r = new Rect(child.getRect());
								if( child.mDisplayType == this.mDisplayType && child.mOpacity == 255 ) {
									ComplexRect one_and_r = new ComplexRect(one);
									one_and_r.and(r);
									ComplexRect two_and_r = new ComplexRect(two);
									two_and_r.and(r);
									one.sub(one_and_r);
									two.or(one_and_r);
									two.or(two_and_r);
									ComplexRect tmp = new ComplexRect();
									tmp.or(r);
									tmp.sub(one_and_r);
									tmp.sub(two_and_r);
									one.or(tmp);
									one_and_r.clear();
									two_and_r.clear();
									tmp.clear();
								} else {
									two.or(r);
									one.sub(r);
								}
							}
						}
					}
				} finally {
					two.clear();
					two = null;
				}
			}

			mOverlappedRegion.sub(mExposedRegion);
		}
		mExposedRegionValid = true;
	}
	private final ComplexRect getExposedRegion() {
		if(!mExposedRegionValid) createExposedRegion();
		return mExposedRegion;
	}
	private final ComplexRect getOverlappedRegion() {
		if(!mExposedRegionValid) createExposedRegion();
		return mOverlappedRegion;
	}
	private void draw( Drawable target, final Rect r ) throws VariantException, TJSException {
		draw( target,r,true);
	}
	private void draw( Drawable target, final Rect r, boolean visiblecheck) throws VariantException, TJSException {
		// process updating pipe line.
		// draw the layer content to "target".
		// "r" is a rectangle to be drawn in the parent's coordinates.
		// parent has responsibility for piling the image returned from children.


		if(visiblecheck && !isSeen()) return;

		Rect rect = new Rect(r);
		if(!Rect.intersectRect( rect, rect, mRect)) return; // no intersection


		mCurrentDrawTarget = target;

		parentRectToChildRect(rect);

		if( mInTransition && mTransWithChildren ) {
			// rearrange pipe line for transition
			mTransDrawable.init(this, target);
			target = mTransDrawable;
		}

		// process drawing
		mDirectTransferToParent = false;
		boolean totalopaque = (mDisplayType == LayerType.ltOpaque && mOpacity == 255);

		if(getCacheEnabled() && !(mInTransition && !mTransWithChildren && mDivisibleTransHandler != null )) {
			// process must-recalc region

			ComplexRect.Iterator it = mCacheRecalcRegion.getIterator();
			while( it.step() ) {
				Rect cr = new Rect(it.get());

				// intersection check
				if(!Rect.intersectRect( cr, cr, rect)) continue;

				// clear DrawnRegion
				mDrawnRegion.clear();

				// setup UpdateBitmapForChild
				mUpdateBitmapForChild = mCacheBitmap;

				// copy self image to UpdateBitmapForChild
				if(mMainImage != null) {
					copySelf(mUpdateBitmapForChild, cr.left, cr.top, cr); // transfer self image
				}


				try {
					mChildren.safeLock();
					final int count = mChildren.getSafeLockedObjectCount();
					for( int i = 0; i < count; i++ ) {
						LayerNI child = mChildren.get(i);
						if( child != null ) {
							// for each child...

							// intersection check
							if( Rect.intersectRect(mUpdateRectForChild, cr, child.mRect) ) {
								// setup UpdateOfsX/Y UpdateRectForChildOfsX/Y
								mUpdateOfsX = 0;
								mUpdateOfsY = 0;
								mUpdateRectForChildOfsX = mUpdateRectForChild.left - child.mRect.left;
								mUpdateRectForChildOfsY = mUpdateRectForChild.top - child.mRect.top;

								// call children's "Draw" method
								child.draw( this, mUpdateRectForChild, true);
							}
						}
					}
					mChildrenArrayValid = false;
					mChildrenOrderIndexValid = false;
					if(mManager!=null) mManager.invalidateOverallIndex();
				} finally {
					mChildren.safeUnlock();
				}

				// special optimazation for MainImage == NULL

				if( mMainImage == null ) {
					ComplexRect nr = new ComplexRect();
					nr.or(cr);
					nr.sub(mDrawnRegion);
					ComplexRect.Iterator it1 = nr.getIterator();
					while( it1.step() ) {
						Rect r1 = new Rect(it1.get());
						copySelf( mUpdateBitmapForChild, r1.left, r1.top, r1 );
								// CopySelf of MainImage == NULL actually
								// fills target rectangle with full transparency
					}
					nr.clear();
				}

			}

			mCacheRecalcRegion.sub(rect);

			if(mCacheRecalcRegion.getCount() > CACHE_UNITE_LIMIT)
				mCacheRecalcRegion.unite();

			// at this point, the cache bitmap should be completed

			// send completion message to the target
			Rect pr = new Rect(rect);
			pr.addOffsets(mRect.left, mRect.top);
			target.drawCompleted( pr, mCacheBitmap, rect, mDisplayType, mOpacity );
		} else {
			// caching is not enabled

			if( getVisibleChildrenCount() == 0 ) {
				// no visible children; no action needed
				Rect pr = new Rect(rect);
				pr.addOffsets(mRect.left, mRect.top);
				Rect cr = new Rect(rect);
				drawSelf(target, pr, cr);
			} else {
				// has at least one visible child
				final ComplexRect overlapped = getOverlappedRegion();
				final ComplexRect exposed = getExposedRegion();

				// process overlapped region
				// clear DrawnRegion
				ComplexRect.Iterator it;

				mDrawnRegion.clear();

				it = overlapped.getIterator();
				while(it.step()) {
					Rect cr = new Rect(it.get());

					// intersection check
					if(!Rect.intersectRect( cr, cr, rect)) continue;


					Rect updaterectforchild = new Rect();
					boolean tempalloc = false;

					// setup UpdateBitmapForChild and "updaterectforchild"
					if( totalopaque ) {
						// this layer is totally opaque
						mUpdateBitmapForChild = target.getDrawTargetBitmap( cr, updaterectforchild);
					} else {
						// this layer is transparent

						// retrieve temporary bitmap
						mUpdateBitmapForChild = TempBitmapHolder.getTemp( cr.width(), cr.height() );
						tempalloc = true;
						updaterectforchild.left = 0;
						updaterectforchild.top = 0;
						updaterectforchild.right = cr.width();
						updaterectforchild.bottom = cr.height();
					}

					try {
						// copy self image to the target
						copySelf( mUpdateBitmapForChild, updaterectforchild.left, updaterectforchild.top, cr);

						try {
							mChildren.safeLock();
							final int lcount = mChildren.getSafeLockedObjectCount();
							for( int i = 0; i < lcount; i++ ) {
								LayerNI child = mChildren.get(i);
								if( child != null ) {
									// for each child...

									// visible check
									if(!child.mVisible) continue;

									// intersection check
									Rect chrect = new Rect();
									if(!Rect.intersectRect( chrect, cr, child.mRect))
										continue;

									// setup UpdateRectForChild
									int ox = chrect.left - cr.left;
									int oy = chrect.top - cr.top;

									mUpdateRectForChild.set( updaterectforchild );
									mUpdateRectForChild.addOffsets(ox, oy);
									mUpdateRectForChildOfsX = chrect.left - child.mRect.left;
									mUpdateRectForChildOfsY = chrect.top - child.mRect.top;

									// setup UpdateOfsX, UpdateOfsY
									mUpdateOfsX = cr.left - updaterectforchild.left;
									mUpdateOfsY = cr.top - updaterectforchild.top;

									// call children's "Draw" method
									child.draw( this, chrect, true);
								}
							}
							mChildrenArrayValid = false;
							mChildrenOrderIndexValid = false;
							if(mManager!=null) mManager.invalidateOverallIndex();
						} finally {
							mChildren.safeUnlock();
						}
					} catch( TJSException e ) {
						if(tempalloc) TempBitmapHolder.freeTemp();
						throw e;
					}

					// send completion message to the target
					if( mDisplayType != LayerType.ltBinder ) {
						Rect pr = new Rect(cr);
						pr.addOffsets( mRect.left, mRect.top );
						target.drawCompleted( pr, mUpdateBitmapForChild, updaterectforchild, mDisplayType, mOpacity);
					}

					// release temporary bitmap
					if(tempalloc) TempBitmapHolder.freeTemp();
				} // overlapped region


				// process exposed region
				mDirectTransferToParent = true; // this flag is used only when MainImage == NULL

				it = exposed.getIterator();
				while( it.step() ) {
					Rect cr = new Rect(it.get());

					// intersection check
					if(!Rect.intersectRect( cr, cr, rect)) continue;

					if( mMainImage != null ) {
						// send completion message to the target
						Rect pr = new Rect(cr);
						pr.addOffsets(mRect.left, mRect.top);
						drawSelf(target, pr, cr);
					} else {
						// call children's "Draw" method
						Rect cr1 = new Rect(it.get());

						// intersection check
						if(!Rect.intersectRect(cr1, cr1, rect)) continue;


						//Rect updaterectforchild = new Rect();

						try {
							mChildren.safeLock();
							final int llcount = mChildren.getSafeLockedObjectCount();
							for( int i = 0; i < llcount; i++ ) {
								LayerNI child = mChildren.get(i);
								if( child != null ) {
									//TVP_LAYER_FOR_EACH_CHILD_BEGIN(child)
									// for each child...

									// visible check
									if(!child.mVisible) continue;

									// intersection check
									Rect chrect = new Rect();
									if(!Rect.intersectRect( chrect, cr1, child.mRect))
										continue;

									// call children's "Draw" method
									child.draw( this, chrect, true);
								}
							}
							mChildrenArrayValid = false;
							mChildrenOrderIndexValid = false;
							if(mManager!=null) mManager.invalidateOverallIndex();
						} finally {
							mChildren.safeUnlock();
						}
					}
				}
				mDirectTransferToParent = false;
			} // has visible children/no visible children
		} // cache enabled/disabled
		mCurrentDrawTarget = null;
	}
	private void updateAllChildren(boolean tempupdate) {
		try {
			mChildren.safeLock();
			final int count = mChildren.getSafeLockedObjectCount();
			for( int i = 0; i < count; i++ ) {
				LayerNI child = mChildren.get(i);
				if( child != null ) {
					child.update(tempupdate);
				}
			}
			mChildrenArrayValid = false;
			mChildrenOrderIndexValid = false;
			if(mManager!=null) mManager.invalidateOverallIndex();
		} finally {
			mChildren.safeUnlock();
		}
	}
	public boolean getParentVisible() {
		// is parent visible? this does not check opacity
		LayerNI par = mParent;
		while( par != null ) {
			if(!par.mVisible) { return false; }
			par = par.mParent;
		}
		return true;
	}
	public boolean getNodeVisible() {
		return getParentVisible() && mVisible;
	} // this does not check opacity
	private void invokeTransition( long tick ) throws VariantException, TJSException {
		if(!mTransCompEventPrevented) {
			if(mUseTransTickCallback)
				mTransTick = getTransTick();
			else
				mTransTick = tick;
			if(!getNodeVisible()) {
				stopTransitionByHandler();
			} else {
				if( mMainImage == null && mTransWithChildren &&
					mTransUpdateType == tutDivisibleFade && mDisplayType != LayerType.ltOpaque) {
					// update only for child region
					updateAllChildren(true);
					if(mTransSrc != null) mTransSrc.updateAllChildren(true);
				} else {
					update(true); // update without re-computing piled images
				}
			}
		} else {
			// transition complete event is prevented
			if( !TVP.EventManager.isEventDisabled() ) // if event dispatching is enabled
				internalStopTransition(); // stop the transition
		}
	}
	@Override
	public BaseBitmap getDrawTargetBitmap( final Rect rect, Rect cliprect) throws TJSException {
		// called from children to get the image buffer drawn to.
		if( mDisplayType == LayerType.ltBinder || mMainImage == null && mDirectTransferToParent ) {
			Rect _rect = new Rect(rect);
			_rect.addOffsets(mRect.left, mRect.top);
			BaseBitmap bmp = mCurrentDrawTarget.getDrawTargetBitmap(_rect, cliprect);
			return bmp;
		}
		int w = rect.width();
		int h = rect.height();
		if(mUpdateRectForChild.width() < w || mUpdateRectForChild.height() < h)
			Message.throwExceptionMessage(Message.InternalError);
		cliprect.set( mUpdateRectForChild );
		cliprect.addOffsets(rect.left - mUpdateRectForChildOfsX, rect.top - mUpdateRectForChildOfsY);
		return mUpdateBitmapForChild;
	}
	@Override
	public int getTargetLayerType() {
		if( mDisplayType == LayerType.ltBinder ) // return parent's display layer type when DisplayType == ltBinder
			return mParent != null ? mParent.mDisplayType : LayerType.ltOpaque;
		return mDisplayType;
	}
	@Override
	public void drawCompleted( final Rect destrect, BaseBitmap bmp, final Rect cliprect, int type, int opacity) throws VariantException, TJSException {
		// called from children to notify that the image drawing is completed.
		// blend the image to the target unless bmp is the same as UpdateBitmapForChild.
		if( mDisplayType == LayerType.ltBinder || mMainImage == null && mDirectTransferToParent ) {
			Rect _destrect = new Rect(destrect);
			Rect _cliprect = new Rect(cliprect);
			_destrect.addOffsets(mRect.left, mRect.top);
			mCurrentDrawTarget.drawCompleted(_destrect, bmp, _cliprect, type, opacity);
			return;
		}

		if( bmp != mUpdateBitmapForChild ) {
			if( mMainImage == null ) {
				Rect r = new Rect();
				Rect sr = new Rect();

				// special optimization for MainImage == NULL
				// (all the layer face is treated as transparent)
				ComplexRect nr = new ComplexRect(); // new region
				nr.or(destrect);
				nr.sub(mDrawnRegion);
				ComplexRect or = new ComplexRect(); // operation region
				// now nr is a client region which is not overlapped by children
				// at this time
				if( mDisplayType == type && opacity == 255 ) {
					// DisplayType == type and full opacity
					// just copy the target bitmap
					ComplexRect.Iterator it = nr.getIterator();
					while(it.step()) {
						r.set( it.get() );
						sr.left = cliprect.left + (r.left - destrect.left);
						sr.top  = cliprect.top  + (r.top  - destrect.top );
						sr.right = sr.left + r.width();
						sr.bottom = sr.top + r.height();

						mUpdateBitmapForChild.copyRect( r.left - mUpdateOfsX, r.top - mUpdateOfsY, bmp, sr);
					}
					// calculate operation region
					or.or(destrect);
					or.sub(nr);
				} else {
					// set operation region
					ComplexRect.Iterator it = nr.getIterator();
					while(it.step()) {
						r.set( it.get() );
						r.addOffsets(-mUpdateOfsX, -mUpdateOfsY);
						// fill r with transparent color
						copySelf(mUpdateBitmapForChild, r.left, r.top, r);
								// CopySelf of MainImage == NULL actually
								// fills target rectangle with full transparency
					}
					or.or(destrect);
				}

				// operate r
				ComplexRect.Iterator it = or.getIterator();
				while( it.step() ) {
					r.set(it.get());
					sr.left = cliprect.left + (r.left - destrect.left);
					sr.top  = cliprect.top  + (r.top  - destrect.top );
					sr.right = sr.left + r.width();
					sr.bottom = sr.top + r.height();
					bltImage( mUpdateBitmapForChild, mDisplayType, r.left - mUpdateOfsX, r.top - mUpdateOfsY, bmp, sr, type, opacity);
				}

				// update DrawnRegion
				mDrawnRegion.or(destrect);
				nr.clear();
				or.clear();
			} else {
				bltImage( mUpdateBitmapForChild, mDisplayType, destrect.left - mUpdateOfsX, destrect.top - mUpdateOfsY, bmp, cliprect, type, opacity);
			}
		}
	}
	private static void bltImage( BaseBitmap dest, int destlayertype, int destx, int desty, BaseBitmap src, final Rect srcrect, int drawtype, int opacity ) throws TJSException {
		// draw src to dest according with layer type
	/*
		// do the effect
		tTVPRect destrect;
		destrect.left = destx;
		destrect.top = desty;
		destrect.right = destx + srcrect.get_width();
		destrect.bottom = desty + srcrect.get_height();
		EffectImage(dest, destrect);
	*/

		// blt to the target
		boolean hda = false;
		int met;
		switch( drawtype ) {
		case LayerType.ltBinder:
			// no action
			return;

		case LayerType.ltOpaque: // formerly ltCoverRect
			// copy
			if(LayerType.isTypeUsingAlpha(destlayertype))
				met = bmCopyOnAlpha;
			else if(LayerType.isTypeUsingAddAlpha(destlayertype))
				met = bmCopyOnAddAlpha;
			else
				met = bmCopy;
			break;

		case LayerType.ltAlpha: // formerly ltTransparent
			// alpha blend
			if(LayerType.isTypeUsingAlpha(destlayertype))
				met = bmAlphaOnAlpha;
			else if(LayerType.isTypeUsingAddAlpha(destlayertype))
				met = bmAlphaOnAddAlpha;
			else
				met = bmAlpha;
			break;

		case LayerType.ltAdditive:
			// additive blend
			hda = LayerType.isTypeUsingAlphaChannel(destlayertype);
				// hda = true if destination has alpha
				// ( preserving mask )
			met = bmAdd;
			break;

		case LayerType.ltSubtractive:
			// subtractive blend
			hda = LayerType.isTypeUsingAlphaChannel(destlayertype);
			met = bmSub;
			break;

		case LayerType.ltMultiplicative:
			// multiplicative blend
			hda = LayerType.isTypeUsingAlphaChannel(destlayertype);
			met = bmMul;
			break;

		case LayerType.ltDodge:
			// color dodge ( "Ooi yaki" in Japanese )
			hda = LayerType.isTypeUsingAlphaChannel(destlayertype);
			met = bmDodge;
			break;

		case LayerType.ltDarken:
			// darken blend (select lower luminosity)
			hda = LayerType.isTypeUsingAlphaChannel(destlayertype);
			met = bmDarken;
			break;

		case LayerType.ltLighten:
			// lighten blend (select higher luminosity)
			hda = LayerType.isTypeUsingAlphaChannel(destlayertype);
			met = bmLighten;
			break;

		case LayerType.ltScreen:
			// screen multiplicative blend
			hda = LayerType.isTypeUsingAlphaChannel(destlayertype);
			met = bmScreen;
			break;

		case LayerType.ltAddAlpha:
			// alpha blend
			if(LayerType.isTypeUsingAlpha(destlayertype))
				met = bmAddAlphaOnAlpha;
			else if(LayerType.isTypeUsingAddAlpha(destlayertype))
				met = bmAddAlphaOnAddAlpha;
			else
				met = bmAddAlpha;
			break;

		case LayerType.ltPsNormal:
			// Photoshop compatible normal blend
			hda = LayerType.isTypeUsingAlphaChannel(destlayertype);
			met = bmPsNormal;
			break;

		case LayerType.ltPsAdditive:
			// Photoshop compatible additive blend
			hda = LayerType.isTypeUsingAlphaChannel(destlayertype);
			met = bmPsAdditive;
			break;

		case LayerType.ltPsSubtractive:
			// Photoshop compatible subtractive blend
			hda = LayerType.isTypeUsingAlphaChannel(destlayertype);
			met = bmPsSubtractive;
			break;

		case LayerType.ltPsMultiplicative:
			// Photoshop compatible multiplicative blend
			hda = LayerType.isTypeUsingAlphaChannel(destlayertype);
			met = bmPsMultiplicative;
			break;

		case LayerType.ltPsScreen:
			// Photoshop compatible screen blend
			hda = LayerType.isTypeUsingAlphaChannel(destlayertype);
			met = bmPsScreen;
			break;

		case LayerType.ltPsOverlay:
			// Photoshop compatible overlay blend
			hda = LayerType.isTypeUsingAlphaChannel(destlayertype);
			met = bmPsOverlay;
			break;

		case LayerType.ltPsHardLight:
			// Photoshop compatible hard light blend
			hda = LayerType.isTypeUsingAlphaChannel(destlayertype);
			met = bmPsHardLight;
			break;

		case LayerType.ltPsSoftLight:
			// Photoshop compatible soft light blend
			hda = LayerType.isTypeUsingAlphaChannel(destlayertype);
			met = bmPsSoftLight;
			break;

		case LayerType.ltPsColorDodge:
			// Photoshop compatible color dodge blend
			hda = LayerType.isTypeUsingAlphaChannel(destlayertype);
			met = bmPsColorDodge;
			break;

		case LayerType.ltPsColorDodge5:
			// Photoshop 5.x compatible color dodge blend
			hda = LayerType.isTypeUsingAlphaChannel(destlayertype);
			met = bmPsColorDodge5;
			break;

		case LayerType.ltPsColorBurn:
			// Photoshop compatible color burn blend
			hda = LayerType.isTypeUsingAlphaChannel(destlayertype);
			met = bmPsColorBurn;
			break;

		case LayerType.ltPsLighten:
			// Photoshop compatible compare (lighten) blend
			hda = LayerType.isTypeUsingAlphaChannel(destlayertype);
			met = bmPsLighten;
			break;

		case LayerType.ltPsDarken:
			// Photoshop compatible compare (darken) blend
			hda = LayerType.isTypeUsingAlphaChannel(destlayertype);
			met = bmPsDarken;
			break;

		case LayerType.ltPsDifference:
			// Photoshop compatible difference blend
			hda = LayerType.isTypeUsingAlphaChannel(destlayertype);
			met = bmPsDifference;
			break;

		case LayerType.ltPsDifference5:
			// Photoshop 5.x compatible difference blend
			hda = LayerType.isTypeUsingAlphaChannel(destlayertype);
			met = bmPsDifference5;
			break;

		case LayerType.ltPsExclusion:
			// Photoshop compatible exclusion blend
			hda = LayerType.isTypeUsingAlphaChannel(destlayertype);
			met = bmPsExclusion;
			break;

		default:
			return;
		}

		dest.blt(destx, desty, src, srcrect, met, opacity, hda);
	}
	public void setImageWidth(int width) throws TJSException
	{
		if(mMainImage == null ) Message.throwExceptionMessage(Message.NotDrawableLayerType);

		if(width == mMainImage.getWidth()) return;

		// adjust position
		if( width < mRect.width()) {
			mImageLeft = 0;
			setWidth(width); // change layer size
		}

		if( width + mImageLeft < mRect.width() ) {
			mImageLeft = mRect.width() - width;
		}

		// change image size...
		changeImageSize(width, mMainImage.getHeight());
	}

	public int getImageWidth() throws TJSException {
		if( mMainImage == null ) Message.throwExceptionMessage(Message.NotDrawableLayerType);
		return mMainImage.getWidth();
	}

	public void setImageHeight( int height ) throws TJSException {
		if(mMainImage == null ) Message.throwExceptionMessage(Message.NotDrawableLayerType);

		if(height == mMainImage.getHeight()) return;

		// adjust position
		if( height < mRect.height() ) {
			mImageTop = 0;
			setHeight(height); // change layer size
		}

		if( (height + mImageTop) < mRect.height() ) {
			mImageTop = mRect.height() - height;
		}

		// change image size...
		changeImageSize( mMainImage.getWidth(), height );
	}

	public int getImageHeight() throws TJSException {
		if( mMainImage == null ) Message.throwExceptionMessage(Message.NotDrawableLayerType);
		return mMainImage.getHeight();
	}
	public void update( final Rect rect ) {
		update( rect, false );
	}
	public void updateByScript( final Rect rect) {
		mCallOnPaint = true;
		update(rect);
	}
	public void updateByScript() {
		mCallOnPaint = true;
		update();
	}
	private void parentUpdate() {
		// called when layer moves
		if( mParent != null ) {
			Rect rect = new Rect();
			rect.left = rect.top = 0;
			rect.right = mRect.width();
			rect.bottom = mRect.height();
			ComplexRect c = new ComplexRect();
			c.or(rect);
			mParent.updateChildRegion(this, c, false, getVisible(), getNodeVisible());
			c.clear();
		}
	}
	private void internalSetBounds( final Rect rect ) throws TJSException {
		int width = rect.right - rect.left;
		int height = rect.bottom - rect.top;
		if( width < 0 || height < 0) Message.throwExceptionMessage(Message.InvalidParam);

		if( mRect.left != rect.left || mRect.top != rect.top ) {
			boolean visible = getVisible() || getNodeVisible();
			if( isPrimary() && (rect.left != 0 || rect.top != 0))
				Message.throwExceptionMessage(Message.CannotMovePrimary);

			if(visible) parentUpdate();
			mRect.setOffsets(rect.left, rect.top);
			if(mParent != null ) mParent.notifyChildrenVisualStateChanged();
			setToCreateExposedRegion();
			if(visible) parentUpdate();
		}
		internalSetSize(width, height);
	}
	public void setBounds(Rect rect) throws TJSException {
		internalSetBounds(rect);
	}
	public void setPosition( int left, int top ) throws TJSException {
		if( mRect.left != left || mRect.top != top ) {
			boolean visible = getVisible() || getNodeVisible();
			if( isPrimary() && (left != 0 || top != 0))
				Message.throwExceptionMessage(Message.CannotMovePrimary);
			if(visible) parentUpdate();
			mRect.setOffsets(left, top);
			if(mParent!=null) mParent.notifyChildrenVisualStateChanged();
			// OriginalTODO: SetPosition
			if(visible) parentUpdate();
		}

	}
	public void drawText(int x, int y, final String text,
			int color, int opa, boolean aa, int shadowlevel, int shadowcolor, int shadowwidth, int shadowofsx, int shadowofsy) throws TJSException {
		// draw text
		if(mMainImage==null) Message.throwExceptionMessage(Message.NotDrawableLayerType);

		int met = 0;

		switch(mDrawFace)
		{
		case DrawFace.dfAlpha:
			met = bmAlphaOnAlpha;
			break;
		case DrawFace.dfAddAlpha:
			if(opa<0) Message.throwExceptionMessage(Message.NegativeOpacityNotSupportedOnThisFace);
			met = bmAlphaOnAddAlpha;
			break;
		case DrawFace.dfOpaque:
			met = bmAlpha;
			break;
		default:
			Message.throwExceptionMessage(Message.NotDrawableFaceType, "drawText" );
		}

		applyFont();

		ComplexRect r = new ComplexRect();

		color = toActualColor(color);

		mMainImage.drawText(mClipRect, x, y, text, color, met,
			opa, mHoldAlpha, aa, shadowlevel, shadowcolor, shadowwidth,
			shadowofsx, shadowofsy, r);

		if(r.getCount()!=0) mImageModified = true;

		if(mImageLeft != 0 || mImageTop != 0) {
			r.addOffsets(mImageLeft, mImageTop);
		}
		update(r);
		r.clear();
	}
	/**
	 * convert color identifier or TVP system color to/from actual color
	 * @param color
	 * @return
	 */
	static public final int toActualColor( int color ) {
		if( (color & 0xff000000) != 0 ) {
			// color = ColorToRGB((TColor)color); // VCL color to RGB
			// TODO システムカラーを取得するものだけど、ここではそのままバイパス, どうするか検討
			return SystemColor.colorToRGB(color);
			// convert byte order to 0xRRGGBB since ColorToRGB's return value is in
			// a format of 0xBBGGRR.
			//return ((color&0xff)<<16) + (color&0xff00) + ((color&0xff0000)>>16);
		} else {
			return color;
		}
	}

	static public final int fromActualColor( int color ) {
		color &= 0xffffff;
		return color;
	}
	public Dispatch2 getFontObject() throws TJSException {
		if( mFontObject != null ) return mFontObject;

		// create font object if the object is not yet created.
		Dispatch2 owner = mOwner.get();
		if( owner == null ) Message.throwExceptionMessage( "Panic! Layer object is not properly constructed. The constructor was not called??" );
		mFontObject = TVP.createFontObject(owner);

		return mFontObject;
	}
	public void setImageSize(int width, int height) throws TJSException {
		if(mMainImage==null) Message.throwExceptionMessage(Message.NotDrawableLayerType);

		if(width == mMainImage.getWidth() && height == mMainImage.getHeight()) return;

		internalSetImageSize(width, height);
	}
	public void fillRect( final Rect rect, int color) throws TJSException {
		// fill given rectangle with given "color"
		// this method does not do transparent coloring.
		Rect destrect = new Rect();
		if(!Rect.intersectRect( destrect, rect, mClipRect) ) return; // out of the clipping rectangle

		if(mDrawFace == DrawFace.dfAlpha || mDrawFace == DrawFace.dfAddAlpha || (mDrawFace == DrawFace.dfOpaque && !mHoldAlpha)) {
			// main and mask
			if(mMainImage==null) Message.throwExceptionMessage(Message.NotDrawableLayerType);
			color = (color & 0xff000000) + (toActualColor(color&0xffffff)&0xffffff);
			mImageModified = mMainImage.fill(destrect, color) || mImageModified;
		} else if(mDrawFace == DrawFace.dfOpaque) {
			// main only
			if(mMainImage==null) Message.throwExceptionMessage(Message.NotDrawableLayerType);
			color = toActualColor(color);
			mImageModified = mMainImage.fillColor(destrect, color, 255) || mImageModified;
		} else if(mDrawFace == DrawFace.dfMask) {
			// mask only
			if(mMainImage==null) Message.throwExceptionMessage(Message.NotDrawableLayerType);
			mImageModified = mMainImage.fillMask(destrect, color&0xff) || mImageModified;
		} else if(mDrawFace == DrawFace.dfProvince) {
			// province
			color = color & 0xff;
			if(color!=0){
				if( mProvinceImage == null ) allocateProvinceImage();
				if( mProvinceImage != null )
					mImageModified = mProvinceImage.fill(destrect, color&0xff) || mImageModified;
			} else {
				if(mProvinceImage!=null) {
					if(destrect.left == 0 && destrect.top == 0 &&
						destrect.right == mProvinceImage.getWidth() &&
						destrect.bottom == mProvinceImage.getHeight()) {
						// entire area of the province image will be filled with 0
						deallocateProvinceImage();
						mImageModified = true;
					} else {
						mImageModified = mProvinceImage.fill(destrect, color&0xff) || mImageModified;
					}
				}
			}
		}

		if(mImageLeft != 0 || mImageTop != 0) {
			Rect ur = new Rect(destrect);
			ur.addOffsets(mImageLeft, mImageTop);
			update(ur);
		} else {
			update(destrect);
		}
	}
	public VariantClosure getActionOwner() { return mActionOwner; }
	public int getTextWidth(String text) throws TJSException {
		if(mMainImage==null) Message.throwExceptionMessage(Message.UnsupportedLayerType, "getTextWidth" );
		applyFont();
		return mMainImage.getTextWidth(text);
	}
	public int getTextHeight(String text) throws TJSException {
		if(mMainImage==null) Message.throwExceptionMessage(Message.UnsupportedLayerType, "getTextHeight" );
		applyFont();
		return mMainImage.getTextHeight(text);
	}
	public double getEscWidthX(String text) throws TJSException {
		if(mMainImage==null) Message.throwExceptionMessage(Message.UnsupportedLayerType, "getEscWidthX" );
		applyFont();
		return mMainImage.getEscWidthX(text);
	}
	public double getEscWidthY(String text) throws TJSException {
		if(mMainImage==null) Message.throwExceptionMessage(Message.UnsupportedLayerType, "getEscWidthY" );
		applyFont();
		return mMainImage.getEscWidthY(text);
	}
	public double getEscHeightX(String text) throws TJSException {
		if(mMainImage==null) Message.throwExceptionMessage(Message.UnsupportedLayerType, "getEscHeightX" );
		applyFont();
		return mMainImage.getEscHeightX(text);
	}
	public double getEscHeightY(String text) throws TJSException {
		if(mMainImage==null) Message.throwExceptionMessage(Message.UnsupportedLayerType, "getEscHeightY" );
		applyFont();
		return mMainImage.getEscHeightY(text);
	}
	public boolean doUserFontSelect(int flags, String caption, String prompt, String samplestring) {
		applyFont();
		boolean b = mMainImage.selectFont(flags, caption, prompt, samplestring, mFont.getFaceName() );
		if(b) mFontChanged = true;
		return b;
	}
	public void getFontList(int flags, ArrayList<String> list) {
		applyFont();
		mMainImage.getFontList(flags, list);
	}
	public void mapPrerenderedFont(String storage) throws TJSException {
		applyFont();
		mMainImage.mapPrerenderedFont(storage);
	}
	public void unmapPrerenderedFont() {
		applyFont();
		mMainImage.unmapPrerenderedFont();
	}
	public void loadFont( String storage, String facename ) throws TJSException {
		applyFont();
		mMainImage.loadFont(storage,facename);
	}
	private void checkZOrderMoveRule( LayerNI lay ) throws TJSException {
		if(mParent==null) Message.throwExceptionMessage(Message.CannotMovePrimaryOrSiblingless);
		if( mParent.mChildren.getActualCount() <= 1)
			Message.throwExceptionMessage(Message.CannotMovePrimaryOrSiblingless);
		if(lay == this)
			Message.throwExceptionMessage(Message.CannotMoveNextToSelfOrNotSiblings);
		if(lay.mParent != mParent)
			Message.throwExceptionMessage(Message.CannotMoveNextToSelfOrNotSiblings);
	}
	/**
	 * 指定レイヤの手前に移動
	 * @param lay ここで指定したレイヤの手前に移動します。兄弟レイヤ ( 同じ親を持つレイヤ ) のみを指定できます。
	 * @throws TJSException
	 */
	public void moveBefore(LayerNI lay) throws TJSException {
		// move before sibling : lay
		// lay must not be a null
		if( mParent != null ) mParent.setAbsoluteOrderMode(false);

		checkZOrderMoveRule(lay);

		int this_order = getOrderIndex();
		int lay_order = lay.getOrderIndex();

		if( this_order < lay_order )
			mParent.childChangeOrder(this_order, lay_order); // move forward
		else
			mParent.childChangeOrder(this_order, lay_order + 1); // move backward
	}
	/**
	 * 指定レイヤの奥に移動
	 * @param lay ここで指定したレイヤの奥に移動します。兄弟レイヤ ( 同じ親を持つレイヤ ) のみを指定できます。
	 * @throws TJSException
	 */
	public void moveBehind(LayerNI lay) throws TJSException {
		// move behind sibling : lay
		// lay must not be a null
		if(mParent!=null) mParent.setAbsoluteOrderMode(false);

		checkZOrderMoveRule(lay);

		int this_order = getOrderIndex();
		int lay_order = lay.getOrderIndex();

		if(this_order < lay_order)
			mParent.childChangeOrder(this_order, lay_order - 1); // move forward
		else
			mParent.childChangeOrder(this_order, lay_order); // move backward
	}
	void setOrderIndex( int index ) throws TJSException {
		// change order index
		if(mParent==null) Message.throwExceptionMessage(Message.CannotMovePrimaryOrSiblingless);

		mParent.setAbsoluteOrderMode(false);

		if(index < 0) index = 0;
		if(index >= mParent.mChildren.getActualCount())
			index = mParent.mChildren.getActualCount()-1;

		mParent.childChangeOrder(getOrderIndex(), index);
	}
	public void bringToBack() throws TJSException {
		// to most back position
		if(mParent==null) Message.throwExceptionMessage(Message.CannotMovePrimaryOrSiblingless);
		mParent.setAbsoluteOrderMode(false);
		setOrderIndex(0);
	}
	public void bringToFront() throws TJSException {
		// to most front position
		if(mParent==null) Message.throwExceptionMessage(Message.CannotMovePrimaryOrSiblingless);
		mParent.setAbsoluteOrderMode(false);
		setOrderIndex(mParent.mChildren.getActualCount() - 1);
	}
	public void saveLayerImage(String name, String type) throws TJSException {
		if(mMainImage==null) Message.throwExceptionMessage(Message.NotDrawableLayerType);
		mMainImage.saveAsBMP( name, type ); // TVPSaveAsBMP
	}
	public void loadProvinceImage(String name) throws TJSException {
		// load an image as a province image
		if(mMainImage==null) Message.throwExceptionMessage(Message.NotDrawableLayerType);
		allocateProvinceImage();
		try {
			GraphicsLoader.loadGraphic( mProvinceImage, name, 0,
				mMainImage.getWidth(), mMainImage.getHeight(), GraphicsLoader.glmPalettized, null, null );

			if( mProvinceImage.getWidth() != mMainImage.getWidth() ||
				mProvinceImage.getHeight() != mMainImage.getHeight())
				Message.throwExceptionMessage(Message.ProvinceSizeMismatch, name);
		} catch( TJSException e ) {
			deallocateProvinceImage();
			throw e;
		}
		mImageModified = true;
	}
	public int getMainPixel(int x, int y) throws TJSException {
		if(mMainImage==null) Message.throwExceptionMessage(Message.NotDrawableLayerType);
		return fromActualColor(mMainImage.getPoint(x, y) & 0xffffff);
	}
	public void setMainPixel(int x, int y, int color) throws TJSException {
		if(mMainImage==null) Message.throwExceptionMessage(Message.NotDrawableLayerType);
		if(x < mClipRect.left || y < mClipRect.top ||
			x >= mClipRect.right || y >= mClipRect.bottom) return; // out of clipping rectangle

		mMainImage.setPointMain(x, y, toActualColor(color));

		mImageModified = true;
		Rect r = new Rect();
		r.left = mImageLeft + x;
		r.top = mImageTop + y;
		r.right = r.left + 1;
		r.bottom = r.top + 1;
		update(r);
	}
	public int getMaskPixel(int x, int y) throws TJSException {
		if(mMainImage==null) Message.throwExceptionMessage(Message.NotDrawableLayerType);
		return (mMainImage.getPoint(x, y) & 0xff000000) >>> 24;
	}
	public void setMaskPixel(int x, int y, int mask) throws TJSException {
		if(mMainImage==null) Message.throwExceptionMessage(Message.NotDrawableLayerType);

		if(x < mClipRect.left || y < mClipRect.top ||
			x >= mClipRect.right || y >= mClipRect.bottom) return; // out of clipping rectangle

		mMainImage.setPointMask(x, y, mask);

		mImageModified = true;
		Rect r = new Rect();
		r.left = mImageLeft + x;
		r.top = mImageTop + y;
		r.right = r.left + 1;
		r.bottom = r.top + 1;
		update(r);
	}
	public int getProvincePixel(int x, int y) throws TJSException {
		if( mProvinceImage == null ) return 0;
		if(x < 0 || y < 0 || x >= mProvinceImage.getWidth() || y >= mProvinceImage.getHeight()) return 0;
		int ret = mProvinceImage.getPoint(x, y);
		//System.out.print(ret);
		return ret;
	}
	public void setProvincePixel(int x, int y, int n) throws TJSException {
		if(mProvinceImage==null) allocateProvinceImage();
		if(x < mClipRect.left || y < mClipRect.top || x >= mClipRect.right || y >= mClipRect.bottom) return; // out of clipping rectangle

		mProvinceImage.setPoint(x, y, n);

		mImageModified = true;
		Rect r = new Rect();
		r.left = mImageLeft + x;
		r.top = mImageTop + y;
		r.right = r.left + 1;
		r.bottom = r.top + 1;
		update(r);
	}
	public LayerNI getMostFrontChildAt(int x, int y, boolean exclude_self, boolean get_disabled) throws TJSException {
		// get most front layer at (x, y),
		// excluding self layer if "exclude_self" is true.
		if(mManager==null) return null;

		// convert to primary layer's coods
		LayerNI p = this;
		while( p != null ) {
			if(p.mParent == null ) break;
			x += p.mRect.left;
			y += p.mRect.top;
			p = p.mParent;
		}

		// call Manager->GetMostFrontChildAt
		return mManager.getMostFrontChildAt(x, y, exclude_self ? this : null, get_disabled);
	}
	private boolean hitTestNoVisibleCheckInternal( int x, int y ) throws TJSException {
		// do hit test.
		// this function does not check layer's visiblity
		if( mHitType == htMask ) {
			// use mask
			if( mMainImage != null ) {
				int px = x-mImageLeft, py = y-mImageTop;
				if(px >= 0 && py >= 0 && px < mMainImage.getWidth() && py < mMainImage.getHeight()) {
					int cl = mMainImage.getPoint(px, py);
					if( (cl >>> 24) < mHitThreshold )
						return false;
					else
						return true;
				} else {
					return false;
				}
			} else {
				// layer has no image
				// all pixels are treated as 0 alpha value
				if(x >= 0 && y >= 0 && x < mRect.width() && y < mRect.height() ) {
					if(0 < mHitThreshold) return false;
					else return true;
				}
				return false;
			}
		} else if( mHitType == htProvince ) {
			// use province
			if( mProvinceImage != null ) {
				int px = x-mImageLeft, py = y-mImageTop;
				if(px >= 0 && py >= 0 && px < mProvinceImage.getWidth() && py < mProvinceImage.getHeight()) {
					int cl = mProvinceImage.getPoint(px, py);
					if(cl == 0) return false;
					else return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		}
		return true;
	}
	private static String ON_HIT_TEST = "onHitTest";
	private boolean hitTestNoVisibleCheck( int x, int y ) throws TJSException {
		boolean res = hitTestNoVisibleCheckInternal(x, y);

		if( res ) {
			// call onHitTest to perform additional hittest
			Dispatch2 owner = mOwner.get();
			if( owner != null && !mShutdown ) {
				mOnHitTest_Work = true;

				Variant[] param = new Variant[3];
				param[0] = new Variant(x);
				param[1] = new Variant(y);
				param[2] = new Variant(1);//true
				TVP.EventManager.postEvent( owner, owner, ON_HIT_TEST, 0, EventManager.EPT_IMMEDIATE, param );

				res = mOnHitTest_Work;
			} else {
				return res;
			}
		}
		return res;
	}
	boolean getMostFrontChildAt( int x, int y, Holder<LayerNI> lay, final LayerNI except, boolean get_disabled ) throws TJSException {
		// internal function

		// visible check
		if(!mVisible) return false; // cannot hit invisible layer

		// convert coordinates ( the point is given by parent's coordinates )
		x -= mRect.left;
		y -= mRect.top;

		// rectangle test
		if(x < 0 || y < 0 || x >= mRect.width() || y >= mRect.height())
			return false; // out of the rectangle

		int ox = x, oy = y;

		try { // locked
			mChildren.safeLock();
			final int count = mChildren.getSafeLockedObjectCount();
			for( int i = count-1; i >= 0; i-- ) {
				LayerNI child = mChildren.getSafeLockedObjectAt(i);
				if(child==null) continue;
				boolean b = child.getMostFrontChildAt(x, y, lay, except, get_disabled);
				if(b) return true;
			}
		} finally { // end locked
			mChildren.safeUnlock();
		}

		if(except == this) return false; // exclusion

		if( hitTestNoVisibleCheck(ox, oy) ) {
			if(!get_disabled && (!getNodeEnabled() || isDisabledByMode())) {
				lay.mValue = null;
				return true; // cannot hit disabled or under modal layer
			}
			lay.mValue = this;
			return true;
		}
		return false;
	}

	public void setImagePosition(int left, int top) throws TJSException {
		if(mMainImage==null) Message.throwExceptionMessage(Message.NotDrawableLayerType);
		if( mImageLeft != left || mImageTop != top ) {
			if(left > 0) Message.throwExceptionMessage(Message.InvalidImagePosition);
			if(top > 0) Message.throwExceptionMessage(Message.InvalidImagePosition);
			if( (mMainImage.getWidth()) + left < mRect.width())
				Message.throwExceptionMessage(Message.InvalidImagePosition);
			if( (mMainImage.getHeight()) + top < mRect.height())
				Message.throwExceptionMessage(Message.InvalidImagePosition);
			mImageLeft = left;
			mImageTop = top;
			update();
		}
	}
	public void independMainImage(boolean copy) {
		if(mMainImage!=null) {
			if(copy)
				mMainImage.independ();
			else
				mMainImage.independNoCopy();
		}
	}
	public void independProvinceImage(boolean copy) {
		if(mProvinceImage!=null) {
			if(copy)
				mProvinceImage.independ();
			else
				mProvinceImage.independNoCopy();
		}
	}
	public void setClip(int left, int top, int width, int height) throws TJSException {
		if(mMainImage==null) Message.throwExceptionMessage(Message.NotDrawableLayerType);

		mClipRect.left = left < 0 ? 0 : left;
		mClipRect.top = top < 0 ? 0 : top;
		int right = width + left;
		int bottom = height + top;
		int w = mMainImage.getWidth();
		int h = mMainImage.getHeight();
		mClipRect.right = w < right ? w : right;
		mClipRect.bottom = h < bottom ? h : bottom;
		if(mClipRect.right < mClipRect.left) mClipRect.right = mClipRect.left;
		if(mClipRect.bottom < mClipRect.top) mClipRect.bottom = mClipRect.top;
	}
	public void colorRect(Rect rect, int color, int opa) throws TJSException {
		// color given rectangle with given "color"
		Rect destrect = new Rect();
		if(!Rect.intersectRect( destrect, rect, mClipRect) ) return; // out of the clipping rectangle

		switch(mDrawFace) {
		case DrawFace.dfAlpha: // main and mask
			if(mMainImage==null) Message.throwExceptionMessage(Message.NotDrawableLayerType);
			if(opa > 0) {
				color = toActualColor(color);
				mImageModified = mMainImage.fillColorOnAlpha(destrect, color, opa) || mImageModified;
			} else {
				mImageModified = mMainImage.removeConstOpacity(destrect, -opa) || mImageModified;
			}
			break;

		case DrawFace.dfAddAlpha: // additive alpha; main and mask
			if(mMainImage==null) Message.throwExceptionMessage(Message.NotDrawableLayerType);
			if(opa >= 0) {
				color = toActualColor(color);
				mImageModified = mMainImage.fillColorOnAddAlpha(destrect, color, opa) || mImageModified;
			} else {
				Message.throwExceptionMessage(Message.NegativeOpacityNotSupportedOnThisFace);
			}
			break;

		case DrawFace.dfOpaque: // main only
			if(mMainImage==null) Message.throwExceptionMessage(Message.NotDrawableLayerType);
			color = toActualColor(color);
			mImageModified = mMainImage.fillColor(destrect, color, opa) || mImageModified;
				// note that tTVPBaseBitmap::FillColor always holds destination alpha
			break;

		case DrawFace.dfMask: // mask ( opacity will be ignored )
			if(mMainImage==null) Message.throwExceptionMessage(Message.NotDrawableLayerType);
			mImageModified = mMainImage.fillMask(destrect, ((color&0xff) != 0 || mImageModified) ? 1 : 0);
			break;

		case DrawFace.dfProvince: // province ( opacity will be ignored )
			color = color & 0xff;
			if(color!=0) {
				if(mProvinceImage==null) allocateProvinceImage();
				if(mProvinceImage!=null)
					mImageModified = mProvinceImage.fill(destrect, color&0xff) || mImageModified;
			} else {
				if(mProvinceImage!=null) {
					if( destrect.left == 0 && destrect.top == 0 &&
						destrect.right == mProvinceImage.getWidth() &&
						destrect.bottom == mProvinceImage.getHeight() ) {
						// entire area of the province image will be filled with 0
						deallocateProvinceImage();
						mImageModified = true;
					} else {
						mImageModified = mProvinceImage.fill(destrect, color&0xff) || mImageModified;
					}
				}
			}
			break;
		}

		if( mImageLeft != 0 || mImageTop != 0 ) {
			Rect ur = new Rect(destrect);
			ur.addOffsets(mImageLeft, mImageTop);
			update(ur);
		} else {
			update(destrect);
		}
	}
	private boolean clipDestPointAndSrcRect( Point d, Rect srcrectout, final Rect srcrect ) {
		// clip (dx, dy) <- srcrect	with current clipping rectangle
		srcrectout.set( srcrect );
		int dr = d.x + srcrect.right - srcrect.left;
		int db = d.y + srcrect.bottom - srcrect.top;

		if(d.x < mClipRect.left) {
			srcrectout.left += (mClipRect.left - d.x);
			d.x = mClipRect.left;
		}

		if(dr > mClipRect.right) {
			srcrectout.right -= (dr - mClipRect.right);
		}

		if(srcrectout.right <= srcrectout.left) return false; // out of the clipping rect

		if(d.y < mClipRect.top) {
			srcrectout.top += (mClipRect.top - d.y);
			d.y = mClipRect.top;
		}

		if(db > mClipRect.bottom) {
			srcrectout.bottom -= (db - mClipRect.bottom);
		}

		if(srcrectout.bottom <= srcrectout.top) return false; // out of the clipping rect

		return true;
	}
	/**
	 * rectangle copy of piled layer image
	 *
	 * this can transfer the piled image of the source layer
	 * this ignores Drawface of this, or DrawFace of the source layer.
	 * this is affected by source layer type.
	 * @param dx
	 * @param dy
	 * @param src
	 * @param srcrect
	 * @throws TJSException
	 */
	public void piledCopy(int dx, int dy, LayerNI src, final Rect srcrect) throws TJSException {
		if( mMainImage == null ) Message.throwExceptionMessage(Message.NotDrawableLayerType);
		if( src.mMainImage == null ) Message.throwExceptionMessage(Message.SourceLayerHasNoImage);

		Rect rect = new Rect();
		Point d = new Point(dx, dy);
		if(!clipDestPointAndSrcRect(d, rect, srcrect)) return; // out of the clipping rect
		dx = d.x;
		dy = d.y;

		src.incCacheEnabledCount(); // enable cache
		try {
			BaseBitmap bmp = src.complete(rect);
			mImageModified = mMainImage.copyRect(dx, dy, bmp, rect, BaseBitmap.BB_COPY_MAIN|BaseBitmap.BB_COPY_MASK) || mImageModified;
		} finally {
			// disable cache
			src.decCacheEnabledCount();
		}
		Rect ur = new Rect( rect );
		ur.setOffsets(dx, dy);
		if(mImageLeft != 0 || mImageTop != 0) {
			ur.addOffsets(mImageLeft, mImageTop);
			update(ur);
		} else {
			update(ur);
		}
	}
	/**
	 * copy rectangle
	 *
	 * this method switches automatically backward or forward copy, when
	 * the distination and the source each other are overlapped.
	 * @param dx
	 * @param dy
	 * @param src
	 * @param srcrect
	 * @throws TJSException
	 */
	public void copyRect(int dx, int dy, LayerNI src, final Rect srcrect) throws TJSException {
		Rect rect = new Rect();
		Point d = new Point(dx,dy);
		if(!clipDestPointAndSrcRect(d, rect, srcrect)) return; // out of the clipping rect
		dx = d.x;
		dy = d.y;

		switch(mDrawFace) {
		case DrawFace.dfAlpha:
		case DrawFace.dfAddAlpha:
			if(mMainImage==null) Message.throwExceptionMessage(Message.NotDrawableLayerType);
			if(src.mMainImage==null) Message.throwExceptionMessage(Message.SourceLayerHasNoImage);
			mImageModified = mMainImage.copyRect(dx, dy, src.mMainImage, rect, BaseBitmap.BB_COPY_MAIN|BaseBitmap.BB_COPY_MASK) || mImageModified;
			break;

		case DrawFace.dfOpaque:
			if(mMainImage==null) Message.throwExceptionMessage(Message.NotDrawableLayerType);
			if(src.mMainImage==null) Message.throwExceptionMessage(Message.SourceLayerHasNoImage);
			mImageModified = mMainImage.copyRect(dx, dy, src.mMainImage, rect,
				mHoldAlpha?BaseBitmap.BB_COPY_MAIN:(BaseBitmap.BB_COPY_MAIN|BaseBitmap.BB_COPY_MASK)) || mImageModified;
			break;

		case DrawFace.dfMask:
			if(mMainImage==null) Message.throwExceptionMessage(Message.NotDrawableLayerType);
			if(src.mMainImage==null) Message.throwExceptionMessage(Message.SourceLayerHasNoImage);
			mImageModified = mMainImage.copyRect(dx, dy, src.mMainImage, rect, BaseBitmap.BB_COPY_MASK) || mImageModified;
			break;

		case DrawFace.dfProvince:
			if( src.mProvinceImage == null ) {
				// source province image is null;
				// fill destination with zero
				if(mProvinceImage!=null) mProvinceImage.fill(rect, 0);
				mImageModified = true;
			} else {
				// province image is not created if the image is not needed
				// allocate province image
				if(mProvinceImage==null) allocateProvinceImage();
				// then copy
				mImageModified = mProvinceImage.copyRect(dx, dy, src.mProvinceImage, rect) || mImageModified;
			}
			break;
		}

		Rect ur = new Rect(rect);
		ur.setOffsets(dx, dy);
		if(mImageLeft != 0 || mImageTop != 0) {
			ur.addOffsets(mImageLeft, mImageTop);
			update(ur);
		} else {
			update(ur);
		}
	}
	/**
	 *	obsoleted (use OperateRect)
	 *
	 * pile rectangle ( pixel alpha blend )
	 *
	 * piled destination is determined by Drawface (not LayerType).
	 * dfAlpha: destination alpha is considered
	 * dfOpaque: destination alpha is ignored ( treated as full opaque )
	 * dfMask or dfProvince : causes an error
	 * this method ignores soruce layer's LayerType or DrawFace.
	 * the destination alpha is held on dfAlpha if 'HoldAlpha' is true, otherwide the
	 * alpha information is destroyed.
	 * @param dx
	 * @param dy
	 * @param src
	 * @param srcrect
	 * @param opacity
	 * @throws TJSException
	 */
	public void pileRect(int dx, int dy, LayerNI src, Rect srcrect, int opacity) throws TJSException {
		if( mDrawFace != DrawFace.dfAlpha && mDrawFace != DrawFace.dfOpaque) {
			Message.throwExceptionMessage(Message.NotDrawableFaceType, "pileRect" );
		}

		Rect rect = new Rect();
		Point d = new Point(dx,dy);
		if(!clipDestPointAndSrcRect(d, rect, srcrect)) return; // out of the clipping rect
		dx = d.x;
		dy = d.y;

		switch(mDrawFace) {
		case DrawFace.dfAlpha:
			if(mMainImage == null) Message.throwExceptionMessage(Message.NotDrawableLayerType);
			if(src.mMainImage == null) Message.throwExceptionMessage(Message.SourceLayerHasNoImage);
			mImageModified = mMainImage.blt(dx, dy, src.mMainImage, rect, bmAlphaOnAlpha, opacity, mHoldAlpha) || mImageModified;
			break;

		case DrawFace.dfOpaque:
			if(mMainImage==null) Message.throwExceptionMessage(Message.NotDrawableLayerType);
			if(src.mMainImage==null) Message.throwExceptionMessage(Message.SourceLayerHasNoImage);
			mImageModified = mMainImage.blt(dx, dy, src.mMainImage, rect, bmAlpha, opacity, mHoldAlpha) || mImageModified;
			break;
		}

		Rect ur = new Rect(rect);
		ur.setOffsets(dx, dy);
		if(mImageLeft != 0 || mImageTop != 0) {
			ur.addOffsets(mImageLeft, mImageTop);
			update(ur);
		} else {
			update(ur);
		}
	}
	/**
	 * obsoleted (use OperateRect)
	 *
	 * blend rectangle ( constant alpha blend )
	 *
	 * mostly the same as 'PileRect', but this does treat src as completely
	 * opaque image.
	 * @param dx
	 * @param dy
	 * @param src
	 * @param srcrect
	 * @param opacity
	 * @throws TJSException
	 */
	public void blendRect(int dx, int dy, LayerNI src, final Rect srcrect, int opacity) throws TJSException {
		if( mDrawFace != DrawFace.dfAlpha && mDrawFace != DrawFace.dfOpaque ) {
			Message.throwExceptionMessage(Message.NotDrawableFaceType, "blendRect" );
		}

		Rect rect = new Rect();
		Point d = new Point(dx,dy);
		if(!clipDestPointAndSrcRect(d, rect, srcrect)) return; // out of the clipping rect
		dx = d.x;
		dy = d.y;

		switch(mDrawFace) {
		case DrawFace.dfAlpha:
			if(mMainImage==null) Message.throwExceptionMessage(Message.NotDrawableLayerType);
			if(src.mMainImage==null) Message.throwExceptionMessage(Message.SourceLayerHasNoImage);
			mImageModified = mMainImage.blt(dx, dy, src.mMainImage, rect, bmCopyOnAlpha, opacity, mHoldAlpha) || mImageModified;
			break;

		case DrawFace.dfOpaque:
			if(mMainImage==null) Message.throwExceptionMessage(Message.NotDrawableLayerType);
			if(src.mMainImage==null) Message.throwExceptionMessage(Message.SourceLayerHasNoImage);
			mImageModified = mMainImage.blt(dx, dy, src.mMainImage, rect, bmCopy, opacity, mHoldAlpha) || mImageModified;
			break;
		}

		Rect ur = new Rect(rect);
		ur.setOffsets(dx, dy);
		if(mImageLeft != 0 || mImageTop != 0) {
			ur.addOffsets(mImageLeft, mImageTop);
			update(ur);
		} else {
			update(ur);
		}
	}
	private int getOperationModeFromType() {
		// returns corresponding blend operation mode from layer type
		switch(mDisplayType) {
//		case ltBinder:
		case LayerType.ltOpaque:			return LayerType.omOpaque;
		case LayerType.ltAlpha:				return LayerType.omAlpha;
		case LayerType.ltAdditive:			return LayerType.omAdditive;
		case LayerType.ltSubtractive:		return LayerType.omSubtractive;
		case LayerType.ltMultiplicative:	return LayerType.omMultiplicative;
//		case ltEffect:
//		case ltFilter:
		case LayerType.ltDodge:				return LayerType.omDodge;
		case LayerType.ltDarken:			return LayerType.omDarken;
		case LayerType.ltLighten:			return LayerType.omLighten;
		case LayerType.ltScreen:			return LayerType.omScreen;
		case LayerType.ltAddAlpha:			return LayerType.omAddAlpha;
		case LayerType.ltPsNormal:			return LayerType.omPsNormal;
		case LayerType.ltPsAdditive:		return LayerType.omPsAdditive;
		case LayerType.ltPsSubtractive:		return LayerType.omPsSubtractive;
		case LayerType.ltPsMultiplicative:	return LayerType.omPsMultiplicative;
		case LayerType.ltPsScreen:			return LayerType.omPsScreen;
		case LayerType.ltPsOverlay:			return LayerType.omPsOverlay;
		case LayerType.ltPsHardLight:		return LayerType.omPsHardLight;
		case LayerType.ltPsSoftLight:		return LayerType.omPsSoftLight;
		case LayerType.ltPsColorDodge:		return LayerType.omPsColorDodge;
		case LayerType.ltPsColorDodge5:		return LayerType.omPsColorDodge5;
		case LayerType.ltPsColorBurn:		return LayerType.omPsColorBurn;
		case LayerType.ltPsLighten:			return LayerType.omPsLighten;
		case LayerType.ltPsDarken:			return LayerType.omPsDarken;
		case LayerType.ltPsDifference:		return LayerType.omPsDifference;
		case LayerType.ltPsDifference5:		return LayerType.omPsDifference5;
		case LayerType.ltPsExclusion:		return LayerType.omPsExclusion;
		default:							return LayerType.omOpaque;
		}
	}
	/**
	 * resulting corresponding  tTVPBBBltMethod value of mode and current DrawFace.
	 * returns whether the method is known.
	 * @param result
	 * @param mode
	 * @return
	 */
	private boolean getBltMethodFromOperationModeAndDrawFace( IntWrapper result, int mode) {
		int met = 0;
		boolean met_set = false;
		switch(mode) {
		case LayerType.omPsNormal:			met_set = true; met = bmPsNormal;			break;
		case LayerType.omPsAdditive:		met_set = true; met = bmPsAdditive;			break;
		case LayerType.omPsSubtractive:		met_set = true; met = bmPsSubtractive;		break;
		case LayerType.omPsMultiplicative:	met_set = true; met = bmPsMultiplicative;	break;
		case LayerType.omPsScreen:			met_set = true; met = bmPsScreen;			break;
		case LayerType.omPsOverlay:			met_set = true; met = bmPsOverlay;			break;
		case LayerType.omPsHardLight:		met_set = true; met = bmPsHardLight;		break;
		case LayerType.omPsSoftLight:		met_set = true; met = bmPsSoftLight;		break;
		case LayerType.omPsColorDodge:		met_set = true; met = bmPsColorDodge;		break;
		case LayerType.omPsColorDodge5:		met_set = true; met = bmPsColorDodge5;		break;
		case LayerType.omPsColorBurn:		met_set = true; met = bmPsColorBurn;		break;
		case LayerType.omPsLighten:			met_set = true; met = bmPsLighten;			break;
		case LayerType.omPsDarken:			met_set = true; met = bmPsDarken;			break;
		case LayerType.omPsDifference:   	met_set = true; met = bmPsDifference;		break;
		case LayerType.omPsDifference5:   	met_set = true; met = bmPsDifference5;		break;
		case LayerType.omPsExclusion:		met_set = true; met = bmPsExclusion;		break;
		case LayerType.omAdditive:			met_set = true; met = bmAdd;				break;
		case LayerType.omSubtractive:		met_set = true; met = bmSub;				break;
		case LayerType.omMultiplicative:	met_set = true; met = bmMul;				break;
		case LayerType.omDodge:				met_set = true; met = bmDodge;				break;
		case LayerType.omDarken:			met_set = true; met = bmDarken;				break;
		case LayerType.omLighten:			met_set = true; met = bmLighten;			break;
		case LayerType.omScreen:			met_set = true; met = bmScreen;				break;
		case LayerType.omAlpha:
			if(mDrawFace == DrawFace.dfAlpha)
							{	met_set = true; met = bmAlphaOnAlpha; break;		}
			else if(mDrawFace == DrawFace.dfAddAlpha)
							{	met_set = true; met = bmAlphaOnAddAlpha; break;		}
			else if(mDrawFace == DrawFace.dfOpaque)
							{	met_set = true; met = bmAlpha; break;				}
			break;
		case LayerType.omAddAlpha:
			if(mDrawFace == DrawFace.dfAlpha)
							{	met_set = true; met = bmAddAlphaOnAlpha; break;		}
			else if(mDrawFace == DrawFace.dfAddAlpha)
							{	met_set = true; met = bmAddAlphaOnAddAlpha; break;	}
			else if(mDrawFace == DrawFace.dfOpaque)
							{	met_set = true; met = bmAddAlpha; break;			}
			break;
		case LayerType.omOpaque:
			if(mDrawFace == DrawFace.dfAlpha)
							{	met_set = true; met = bmCopyOnAlpha; break;			}
			else if(mDrawFace == DrawFace.dfAddAlpha)
							{	met_set = true; met = bmCopyOnAddAlpha; break;		}
			else if(mDrawFace == DrawFace.dfOpaque)
							{	met_set = true; met = bmCopy; break;				}
			break;
		}
		result.value = met;
		return met_set;
	}
	/**
	 * operate on rectangle ( add/sub/mul/div and others )
	 * @param dx
	 * @param dy
	 * @param src
	 * @param srcrect
	 * @param mode
	 * @param opacity
	 * @throws TJSException
	 */
	public void operateRect(int dx, int dy, LayerNI src, final Rect srcrect, int mode, int opacity) throws TJSException {
		Rect rect = new Rect();
		Point d = new Point(dx,dy);
		if(!clipDestPointAndSrcRect(d, rect, srcrect)) return; // out of the clipping rect
		dx = d.x;
		dy = d.y;

		// get correct blend mode if the mode is omAuto
		if(mode == LayerType.omAuto) mode = src.getOperationModeFromType();

		// convert tTVPBlendOperationMode to tTVPBBBltMethod
		IntWrapper met = new IntWrapper();
		if(!getBltMethodFromOperationModeAndDrawFace(met, mode)) {
			// unknown blt mode
			Message.throwExceptionMessage(Message.NotDrawableFaceType, "operateRect" );
		}

		if(mMainImage==null) Message.throwExceptionMessage(Message.NotDrawableLayerType);
		if(src.mMainImage==null) Message.throwExceptionMessage(Message.SourceLayerHasNoImage);

		mImageModified = mMainImage.blt(dx, dy, src.mMainImage, rect, met.value, opacity, mHoldAlpha) || mImageModified;

		Rect ur = new Rect(rect);
		ur.setOffsets(dx, dy);
		if(mImageLeft != 0 || mImageTop != 0){
			ur.addOffsets(mImageLeft, mImageTop);
			update(ur);
		} else {
			update(ur);
		}
	}
	/**
	 * stretching copy
	 * @param destrect
	 * @param src
	 * @param srcrect
	 * @param type
	 * @throws TJSException
	 */
	public void stretchCopy( final Rect destrect, LayerNI src, final Rect srcrect, int type) throws TJSException {
		Rect ur = new Rect(destrect);
		if(ur.right < ur.left) {
			int t = ur.right;
			ur.right = ur.left;
			ur.left = t;
		}
		if(ur.bottom < ur.top) {
			int t = ur.bottom;
			ur.bottom = ur.top;
			ur.top = t;
		}
		if(!Rect.intersectRect(ur, ur, mClipRect)) return; // out of the clipping rectangle

		switch(mDrawFace) {
		case DrawFace.dfAlpha:
		case DrawFace.dfAddAlpha:
			if(mMainImage==null) Message.throwExceptionMessage(Message.NotDrawableLayerType);
			if(src.mMainImage==null) Message.throwExceptionMessage(Message.SourceLayerHasNoImage);
			mImageModified = mMainImage.stretchBlt(mClipRect, destrect, src.mMainImage, srcrect, bmCopy, 255, false, type) || mImageModified;
			break;

		case DrawFace.dfOpaque:
			if(mMainImage==null) Message.throwExceptionMessage(Message.NotDrawableLayerType);
			if(src.mMainImage==null) Message.throwExceptionMessage(Message.SourceLayerHasNoImage);
			mImageModified = mMainImage.stretchBlt(mClipRect, destrect, src.mMainImage, srcrect, bmCopy, 255, mHoldAlpha, type) || mImageModified;
			break;

		default:
			Message.throwExceptionMessage(Message.NotDrawableFaceType, "stretchCopy" );
		}

		if( mImageLeft != 0 || mImageTop != 0 ){
			ur.addOffsets(mImageLeft, mImageTop);
			update(ur);
		} else {
			update(ur);
		}
	}
	/**
	 * obsoleted (use OperateStretch)
	 *
	 * stretching pile
	 * @param destrect
	 * @param src
	 * @param srcrect
	 * @param opacity
	 * @param type
	 * @throws TJSException
	 */
	public void stretchPile( final Rect destrect, LayerNI src, final Rect srcrect, int opacity, int type ) throws TJSException {
		if( mDrawFace != DrawFace.dfAlpha && mDrawFace != DrawFace.dfOpaque ) {
			Message.throwExceptionMessage(Message.NotDrawableFaceType, "stretchPile" );
		}

		Rect ur = new Rect(destrect);
		if(ur.right < ur.left) {
			int t = ur.right;
			ur.right = ur.left;
			ur.left = t;
		}
		if(ur.bottom < ur.top) {
			int t = ur.bottom;
			ur.bottom = ur.top;
			ur.top = t;
		}
		if(!Rect.intersectRect(ur, ur, mClipRect)) return; // out of the clipping rectangle

		switch(mDrawFace) {
		case DrawFace.dfAlpha:
			if( mMainImage == null ) Message.throwExceptionMessage(Message.NotDrawableLayerType);
			if( src.mMainImage == null ) Message.throwExceptionMessage(Message.SourceLayerHasNoImage);
			mImageModified = mMainImage.stretchBlt( mClipRect, destrect, src.mMainImage, srcrect, bmAlphaOnAlpha, opacity, mHoldAlpha, type) || mImageModified;
			break;

		case DrawFace.dfOpaque:
			if( mMainImage == null ) Message.throwExceptionMessage(Message.NotDrawableLayerType);
			if( src.mMainImage == null ) Message.throwExceptionMessage(Message.SourceLayerHasNoImage);
			mImageModified = mMainImage.stretchBlt(mClipRect, destrect, src.mMainImage, srcrect, bmAlpha, opacity, mHoldAlpha, type) || mImageModified;
			break;
		}

		if( mImageLeft != 0 || mImageTop != 0 ) {
			ur.addOffsets(mImageLeft, mImageTop);
			update(ur);
		} else {
			update(ur);
		}
	}
	/**
	 * obsoleted (use OperateStretch)
	 *
	 * stretching blend
	 * @param destrect
	 * @param src
	 * @param srcrect
	 * @param opacity
	 * @param type
	 * @throws TJSException
	 */
	public void stretchBlend( final Rect destrect, LayerNI src, final Rect srcrect, int opacity, int type) throws TJSException {
		if( mDrawFace != DrawFace.dfAlpha && mDrawFace != DrawFace.dfOpaque) {
			Message.throwExceptionMessage(Message.NotDrawableFaceType, "stretchBlend" );
		}

		Rect ur = new Rect(destrect);
		if(ur.right < ur.left) {
			int t = ur.right;
			ur.right = ur.left;
			ur.left = t;
		}
		if(ur.bottom < ur.top) {
			int t = ur.bottom;
			ur.bottom = ur.top;
			ur.top = t;
		}
		if(!Rect.intersectRect( ur, ur, mClipRect)) return; // out of the clipping rectangle

		switch(mDrawFace){
		case DrawFace.dfAlpha:
			if(mMainImage==null) Message.throwExceptionMessage(Message.NotDrawableLayerType);
			if(src.mMainImage==null) Message.throwExceptionMessage(Message.SourceLayerHasNoImage);
			mImageModified = mMainImage.stretchBlt(mClipRect, destrect, src.mMainImage, srcrect, bmCopyOnAlpha,
				opacity, mHoldAlpha, type) || mImageModified;
			break;

		case DrawFace.dfOpaque:
			if(mMainImage==null) Message.throwExceptionMessage(Message.NotDrawableLayerType);
			if(src.mMainImage==null) Message.throwExceptionMessage(Message.SourceLayerHasNoImage);
			mImageModified = mMainImage.stretchBlt(mClipRect, destrect, src.mMainImage, srcrect, bmCopy, opacity, mHoldAlpha, type) || mImageModified;
			break;
		}

		if(mImageLeft != 0 || mImageTop != 0) {
			ur.addOffsets(mImageLeft, mImageTop);
			update(ur);
		} else {
			update(ur);
		}
	}
	/**
	 * stretching operation (add/mul/sub etc.)
	 * @param destrect
	 * @param src
	 * @param srcrect
	 * @param mode
	 * @param opacity
	 * @param type
	 * @throws TJSException
	 */
	public void operateStretch(Rect destrect, LayerNI src, Rect srcrect, int mode, int opacity, int type) throws TJSException {
		Rect ur = new Rect(destrect);
		if(ur.right < ur.left) {
			int t = ur.right;
			ur.right = ur.left;
			ur.left = t;
		}
		if(ur.bottom < ur.top) {
			int t = ur.bottom;
			ur.bottom = ur.top;
			ur.top = t;
		}
		if(!Rect.intersectRect(ur, ur, mClipRect)) return; // out of the clipping rectangle

		// get correct blend mode if the mode is omAuto
		if(mode == LayerType.omAuto) mode = src.getOperationModeFromType();

		// convert tTVPBlendOperationMode to tTVPBBBltMethod
		IntWrapper met = new IntWrapper();
		if(!getBltMethodFromOperationModeAndDrawFace(met, mode)) {
			// unknown blt mode
			Message.throwExceptionMessage(Message.NotDrawableFaceType, "operateStretch" );
		}

		if(mMainImage==null) Message.throwExceptionMessage(Message.NotDrawableLayerType);
		if(src.mMainImage==null) Message.throwExceptionMessage(Message.SourceLayerHasNoImage);
		mImageModified = mMainImage.stretchBlt(mClipRect, destrect, src.mMainImage, srcrect, met.value, opacity, mHoldAlpha, type) || mImageModified;

		if( mImageLeft != 0 || mImageTop != 0 ) {
			ur.addOffsets(mImageLeft, mImageTop);
			update(ur);
		} else {
			update(ur);
		}
	}
	/**
	 * affine copy
	 * @param matrix
	 * @param src
	 * @param srcrect
	 * @param type
	 * @param clear
	 * @throws TJSException
	 */
	public void affineCopy(AffineMatrix2D matrix, LayerNI src, Rect srcrect, int type, boolean clear) throws TJSException {
		Rect updaterect = new Rect();
		boolean updated = false;

		switch(mDrawFace) {
		case DrawFace.dfAlpha:
		case DrawFace.dfAddAlpha: {
			if(mMainImage==null) Message.throwExceptionMessage(Message.NotDrawableLayerType);
			if(src.mMainImage==null) Message.throwExceptionMessage(Message.SourceLayerHasNoImage);
			updated = mMainImage.affineBlt(mClipRect, src.mMainImage, srcrect, matrix, bmCopy, 255, updaterect, false, type, clear, mNeutralColor);
			break;
		}

		case DrawFace.dfOpaque: {
			if(mMainImage==null) Message.throwExceptionMessage(Message.NotDrawableLayerType);
			if(src.mMainImage==null) Message.throwExceptionMessage(Message.SourceLayerHasNoImage);
			updated = mMainImage.affineBlt(mClipRect, src.mMainImage, srcrect, matrix, bmCopy, 255, updaterect, mHoldAlpha, type, clear, mNeutralColor);
			break;
		}

		default:
			Message.throwExceptionMessage(Message.NotDrawableFaceType, "affineCopy" );
		}

		mImageModified = updated || mImageModified;

		if( updated ) {
			updaterect.addOffsets(mImageLeft, mImageTop);
			update(updaterect);
		}
	}
	/**
	 * affine copy
	 * @param points
	 * @param src
	 * @param srcrect
	 * @param type
	 * @param clear
	 * @throws TJSException
	 */
	public void affineCopy( final PointD[] points, LayerNI src, final Rect srcrect, int type, boolean clear) throws TJSException {
		Rect updaterect = new Rect();
		boolean updated = false;

		switch(mDrawFace) {
		case DrawFace.dfAlpha:
		case DrawFace.dfAddAlpha: {
			if(mMainImage==null) Message.throwExceptionMessage(Message.NotDrawableLayerType);
			if(src.mMainImage==null) Message.throwExceptionMessage(Message.SourceLayerHasNoImage);
			updated = mMainImage.affineBlt(mClipRect, src.mMainImage, srcrect, points, bmCopy, 255, updaterect, false, type, clear, mNeutralColor);
			break;
		  }

		case DrawFace.dfOpaque: {
			if(mMainImage==null) Message.throwExceptionMessage(Message.NotDrawableLayerType);
			if(src.mMainImage==null) Message.throwExceptionMessage(Message.SourceLayerHasNoImage);
			updated = mMainImage.affineBlt(mClipRect, src.mMainImage, srcrect, points, bmCopy, 255, updaterect, mHoldAlpha, type, clear, mNeutralColor);
			break;
		  }

		default:
			Message.throwExceptionMessage(Message.NotDrawableFaceType, "affineCopy" );
		}

		mImageModified = updated || mImageModified;

		if( updated ) {
			updaterect.addOffsets(mImageLeft, mImageTop);
			update(updaterect);
		}
	}
	/**
	 * obsoleted (use OperateAffine)
	 *
	 * affine pile
	 * @param matrix
	 * @param src
	 * @param srcrect
	 * @param opacity
	 * @param type
	 * @throws TJSException
	 */
	public void affinePile( final AffineMatrix2D matrix, LayerNI src, final Rect srcrect, int opacity, int type ) throws TJSException {
		Rect updaterect = new Rect();
		boolean updated = false;
		if( mDrawFace != DrawFace.dfAlpha && mDrawFace != DrawFace.dfOpaque ) {
			Message.throwExceptionMessage(Message.NotDrawableFaceType, "affinePile" );
		}

		switch( mDrawFace ) {
		case DrawFace.dfAlpha: {
			if(mMainImage==null) Message.throwExceptionMessage(Message.NotDrawableLayerType);
			if(src.mMainImage==null) Message.throwExceptionMessage(Message.SourceLayerHasNoImage);
			updated = mMainImage.affineBlt(mClipRect, src.mMainImage, srcrect, matrix, bmAlphaOnAlpha, opacity, updaterect, mHoldAlpha, type);
			break;
		}

		case DrawFace.dfOpaque: {
			if(mMainImage==null) Message.throwExceptionMessage(Message.NotDrawableLayerType);
			if(src.mMainImage==null) Message.throwExceptionMessage(Message.SourceLayerHasNoImage);
			updated = mMainImage.affineBlt(mClipRect, src.mMainImage, srcrect, matrix, bmAlpha, opacity, updaterect, mHoldAlpha, type);
			break;
		}
		}
		mImageModified = updated || mImageModified;
		if( updated ) {
			updaterect.addOffsets(mImageLeft, mImageTop);
			update(updaterect);
		}
	}
	/**
	 * obsoleted (use OperateAffine)
	 *
	 * affine pile
	 * @param points
	 * @param src
	 * @param srcrect
	 * @param opacity
	 * @param type
	 * @throws TJSException
	 */
	public void affinePile(final PointD[] points, LayerNI src, Rect srcrect, int opacity, int type) throws TJSException {
		Rect updaterect = new Rect();
		boolean updated = false;
		if( mDrawFace != DrawFace.dfAlpha && mDrawFace != DrawFace.dfOpaque ) {
			Message.throwExceptionMessage(Message.NotDrawableFaceType, "affinePile" );
		}

		switch(mDrawFace) {
		case DrawFace.dfAlpha: {
			if(mMainImage==null) Message.throwExceptionMessage(Message.NotDrawableLayerType);
			if(src.mMainImage==null) Message.throwExceptionMessage(Message.SourceLayerHasNoImage);
			updated = mMainImage.affineBlt(mClipRect, src.mMainImage, srcrect, points, bmAlphaOnAlpha, opacity, updaterect, mHoldAlpha, type);
			break;
		  }

		case DrawFace.dfOpaque: {
			if(mMainImage==null) Message.throwExceptionMessage(Message.NotDrawableLayerType);
			if(src.mMainImage==null) Message.throwExceptionMessage(Message.SourceLayerHasNoImage);
			updated = mMainImage.affineBlt(mClipRect, src.mMainImage, srcrect, points, bmAlpha, opacity, updaterect, mHoldAlpha, type);
			break;
		}
		}
		mImageModified = updated || mImageModified;
		if (updated ) {
			updaterect.addOffsets(mImageLeft, mImageTop);
			update(updaterect);
		}
	}
	public void affineBlend(final AffineMatrix2D matrix, LayerNI src, final Rect srcrect, int opacity, int type) throws TJSException {
		// obsoleted (use OperateAffine)
		// affine blend
		Rect updaterect = new Rect();
		boolean updated = false;

		if(mDrawFace != DrawFace.dfAlpha && mDrawFace != DrawFace.dfOpaque) {
			Message.throwExceptionMessage(Message.NotDrawableFaceType, "affineBlend");
		}


		switch(mDrawFace) {
		case DrawFace.dfAlpha: {
			if(mMainImage==null) Message.throwExceptionMessage(Message.NotDrawableLayerType);
			if(src.mMainImage==null) Message.throwExceptionMessage(Message.SourceLayerHasNoImage);
			updated = mMainImage.affineBlt(mClipRect, src.mMainImage, srcrect, matrix, bmCopyOnAlpha, opacity, updaterect, mHoldAlpha, type);
			break;
		}

		case DrawFace.dfOpaque: {
			if(mMainImage==null) Message.throwExceptionMessage(Message.NotDrawableLayerType);
			if(src.mMainImage==null) Message.throwExceptionMessage(Message.SourceLayerHasNoImage);
			updated = mMainImage.affineBlt(mClipRect, src.mMainImage, srcrect, matrix, bmCopy, opacity, updaterect, mHoldAlpha, type);
			break;
		}
		}

		mImageModified = updated || mImageModified;
		if(updated) {
			updaterect.addOffsets(mImageLeft, mImageTop);
			update(updaterect);
		}
	}
	public void affineBlend( final PointD[] points, LayerNI src, final Rect srcrect, int opacity, int type) throws TJSException {
		// obsoleted (use OperateAffine)
		// affine blend
		Rect updaterect = new Rect();
		boolean updated = false;

		if(mDrawFace != DrawFace.dfAlpha && mDrawFace != DrawFace.dfOpaque) {
			Message.throwExceptionMessage(Message.NotDrawableFaceType, "affineBlend");
		}

		switch(mDrawFace) {
		case DrawFace.dfAlpha: {
			if(mMainImage==null) Message.throwExceptionMessage(Message.NotDrawableLayerType);
			if(src.mMainImage==null) Message.throwExceptionMessage(Message.SourceLayerHasNoImage);
			updated = mMainImage.affineBlt(mClipRect, src.mMainImage, srcrect, points, bmCopyOnAlpha, opacity, updaterect, mHoldAlpha, type);
			break;
		}

		case DrawFace.dfOpaque: {
			if(mMainImage==null) Message.throwExceptionMessage(Message.NotDrawableLayerType);
			if(src.mMainImage==null) Message.throwExceptionMessage(Message.SourceLayerHasNoImage);
			updated = mMainImage.affineBlt(mClipRect, src.mMainImage, srcrect, points, bmCopy, opacity, updaterect, mHoldAlpha, type);
			break;
		}
		}

		mImageModified = updated || mImageModified;

		if(updated) {
			updaterect.addOffsets(mImageLeft, mImageTop);
			update(updaterect);
		}
	}
	public void operateAffine( final AffineMatrix2D matrix, LayerNI src, final Rect srcrect, int mode, int opacity, int type) throws TJSException {
		// affine operation
		Rect updaterect = new Rect();
		boolean updated = false;

		// get correct blend mode if the mode is omAuto
		if(mode == LayerType.omAuto) mode = src.getOperationModeFromType();

		// convert tTVPBlendOperationMode to tTVPBBBltMethod
		IntWrapper met = new IntWrapper();
		if(!getBltMethodFromOperationModeAndDrawFace(met, mode)) {
			// unknown blt mode
			Message.throwExceptionMessage(Message.NotDrawableFaceType, "operateAffine");
		}

		if(mMainImage==null) Message.throwExceptionMessage(Message.NotDrawableLayerType);
		if(src.mMainImage==null) Message.throwExceptionMessage(Message.SourceLayerHasNoImage);
		updated = mMainImage.affineBlt(mClipRect, src.mMainImage, srcrect, matrix, met.value, opacity, updaterect, mHoldAlpha, type);

		mImageModified = updated || mImageModified;
		if(updated) {
			updaterect.addOffsets(mImageLeft, mImageTop);
			update(updaterect);
		}
	}
	public void operateAffine( final PointD[] points, LayerNI src, final Rect srcrect, int mode, int opacity, int type) throws TJSException {
		// affine operation
		Rect updaterect = new Rect();
		boolean updated = false;

		// get correct blend mode if the mode is omAuto
		if(mode == LayerType.omAuto) mode = src.getOperationModeFromType();

		// convert tTVPBlendOperationMode to tTVPBBBltMethod
		IntWrapper met = new IntWrapper();
		if(!getBltMethodFromOperationModeAndDrawFace(met, mode)) {
			// unknown blt mode
			Message.throwExceptionMessage(Message.NotDrawableFaceType, "operateAffine");
		}

		if(mMainImage==null) Message.throwExceptionMessage(Message.NotDrawableLayerType);
		if(src.mMainImage==null) Message.throwExceptionMessage(Message.SourceLayerHasNoImage);
		updated = mMainImage.affineBlt(mClipRect, src.mMainImage, srcrect, points, met.value, opacity, updaterect, mHoldAlpha, type);

		mImageModified = updated || mImageModified;
		if(updated) {
			updaterect.addOffsets(mImageLeft, mImageTop);
			update(updaterect);
		}
	}
	public void doBoxBlur(int xblur, int yblur) throws TJSException {
		// blur with box blur method
		if(mMainImage==null) Message.throwExceptionMessage(Message.NotDrawableLayerType);

		boolean updated;

		if(mDrawFace != DrawFace.dfAlpha)
			updated = mMainImage.doBoxBlur(mClipRect, new Rect(-xblur, -yblur, xblur, yblur));
		else
			updated = mMainImage.doBoxBlurForAlpha(mClipRect, new Rect(-xblur, -yblur, xblur, yblur));

		mImageModified = updated || mImageModified;

		if(updated) {
			Rect updaterect = new Rect(mClipRect);
			updaterect.addOffsets(mImageLeft, mImageTop);
			update(updaterect);
		}
	}
	public void adjustGamma(GammaAdjustData data) throws TJSException {
		// this is not affected by mDrawFace
		if(mMainImage==null) Message.throwExceptionMessage(Message.NotDrawableLayerType);

		if(mDrawFace == DrawFace.dfAddAlpha)
			mMainImage.adjustGammaForAdditiveAlpha( mClipRect, data);
		else
			mMainImage.adjustGamma( mClipRect, data);

		mImageModified = true;
		update();
	}
	public void doGrayScale() throws TJSException {
		// this is not affected by mDrawFace
		if(mMainImage==null) Message.throwExceptionMessage(Message.NotDrawableLayerType);

		mMainImage.doGrayScale(mClipRect);

		mImageModified = true;
		update();
	}
	public void flipLR() throws TJSException {
		// this is not affected by mDrawFace
		if(mMainImage==null) Message.throwExceptionMessage(Message.NotDrawableLayerType);

		Rect r = new Rect(0, 0, mMainImage.getWidth(), mMainImage.getHeight());
		mMainImage.flipLR(r);
		if(mProvinceImage!=null) mProvinceImage.flipLR(r);

		mImageModified = true;
		update();
	}
	public void flipUD() throws TJSException {
		// this is not affected by mDrawFace
		if(mMainImage==null) Message.throwExceptionMessage(Message.NotDrawableLayerType);

		Rect r = new Rect(0, 0, mMainImage.getWidth(), mMainImage.getHeight());
		mMainImage.flipUD(r);
		if(mProvinceImage!=null) mProvinceImage.flipUD(r);

		mImageModified = true;
		update();
	}
	public void convertLayerType(int fromtype) throws TJSException {
		// convert layer pixel representation method

		if(mDrawFace == DrawFace.dfAddAlpha && fromtype == DrawFace.dfAlpha) {
			// alpha -> additive alpha
			if(mMainImage!=null) mMainImage.convertAlphaToAddAlpha();
		} else if(mDrawFace == DrawFace.dfAlpha && fromtype == DrawFace.dfAddAlpha) {
			// additive alpha -> alpha
			// this may loose additive stuff
			if(mMainImage!=null) mMainImage.convertAddAlphaToAlpha();
		} else {
			// throw an error
			Message.throwExceptionMessage(Message.CannotConvertLayerTypeUsingGivenDirection);
		}

		mImageModified = true;
		update();
	}
	public void setCursorPos(int x, int y) {
		if(mManager==null) return;

		LayerNI p = this;
		while( p != null ) {
			if(p.mParent==null) break;
			x += p.mRect.left;
			y += p.mRect.top;
			p = p.mParent;
		}
		mManager.setCursorPos(x, y);
	}
	public void releaseCapture() {
		if(mManager!=null) mManager.releaseCapture();
		// this releases mouse capture from all layers, ignoring which layer captures.
	}
	public boolean setFocus(boolean direction) throws TJSException {
		if(mManager!=null) return mManager.setFocusTo(this, direction);
		return false;
	}
	public LayerNI focusPrev() throws TJSException {
		if(mManager!=null) return mManager.focusPrev(); else return null;
	}
	public LayerNI focusNext() throws TJSException {
		if(mManager!=null) return mManager.focusNext(); else return null;
	}
	public void setMode() throws TJSException {
		if(mManager!=null) mManager.setModeTo(this);
	}
	public void removeMode() throws TJSException {
		if(mManager!=null) mManager.removeModeFrom(this);
	}
	public void setAttentionPoint(int l, int t) {
		mAttentionLeft = l;
		mAttentionTop = t;
		if(mManager!=null) mManager.notifyAttentionStateChanged(this);
	}
	private static final String SELFUPDATE = "selfupdate";
	private static final String CALLBACK = "callback";
	public void startTransition(String name, boolean withchildren, LayerNI transsource, VariantClosure options) throws TJSException {
		// start transition

		// is current transition processing?
		if( mInTransition ) {
			Message.throwExceptionMessage(Message.CurrentTransitionMustBeStopping);
		}

		if(transsource !=null&& transsource.mTransSrc == this) {
			Message.throwExceptionMessage(Message.TransitionMutualSource);
		}

		// pointers which must be released at last...
		TransHandlerProvider pro = null;
		SimpleOptionProvider sop = null;
		BaseTransHandler handler = null;

		try {
			// find transition handler
			pro = TVP.findTransHandlerProvider(name);
				// this may raise an exception

			// check selfupdate member of 'options'
			Variant var = new Variant();
			mTransSelfUpdate = false;
			int hr = options.propGet(0, SELFUPDATE, var, null);
			if( hr >= 0 ) {
				if(var.isVoid() != true)
					mTransSelfUpdate = var.asBoolean(); // selfupdate member found
			}

			// check callback member of 'options'
			mTransTickCallback = new VariantClosure(null, null);
			mUseTransTickCallback = false;
			hr = options.propGet(0, CALLBACK, var, null);
			if( hr >= 0 ) {
				// selfupdate member found
				if(var.isVoid() != true) {
					mTransTickCallback = var.asObjectClosure(); // AddRef() is performed here
					mUseTransTickCallback = true;
				}
			}

			// create option provider
			sop = new SimpleOptionProviderObject(options);

			// notify starting of the transition to the provider
			int[] type = { mTransType, mTransUpdateType };
			handler = pro.startTransition(sop, TVP.SimpleImageProvider,
				mDisplayType,
				withchildren ? getWidth()  : mMainImage.getWidth(),
				withchildren ? getHeight() : mMainImage.getHeight(),
				transsource!=null?
					(withchildren?transsource.getWidth() :
						transsource.mMainImage.getWidth())  :0,
				transsource!=null?
					(withchildren?transsource.getHeight() :
						transsource.mMainImage.getHeight()) :0, type );

			mTransType = type[0];
			mTransUpdateType = type[1];

			if( handler == null ) Message.throwExceptionMessage(Message.TransHandlerError, "iTVPTransHandlerProvider::StartTransition failed");

			if(mTransUpdateType != tutDivisibleFade && mTransUpdateType != tutDivisible
				&& mTransUpdateType != tutGiveUpdate)
				Message.throwExceptionMessage(Message.TransHandlerError,
					"Unknown update type");

			if(mTransType != TransHandlerProvider.ttSimple && mTransType != TransHandlerProvider.ttExchange)
				Message.throwExceptionMessage(Message.TransHandlerError,
					"Unknown transition type");

			// check update type
			if(mTransUpdateType == tutGiveUpdate)
				Message.throwExceptionMessage(Message.TransHandlerError,
					"Update type of tutGiveUpdate is not yet supported");
						// sorry for inconvinience
			if(mTransType == TransHandlerProvider.ttExchange && transsource == null)
				Message.throwExceptionMessage(Message.SpecifyTransitionSource);

			// check wether the source and destination both have image
			if(!withchildren) {
				if(mMainImage==null)
					Message.throwExceptionMessage(Message.TransitionSourceAndDestinationMustHaveImage);
				if(transsource !=null && transsource.mMainImage == null)
					Message.throwExceptionMessage(Message.TransitionSourceAndDestinationMustHaveImage);
			}

			// set to cache
			mTransWithChildren = withchildren;
			if(mTransWithChildren) {
				incCacheEnabledCount();
				if(transsource!=null) transsource.incCacheEnabledCount();
			}

			// set to interrupt into updating/completion pipe line
			mTransSrc = transsource;
			if(transsource!=null) transsource.mTransDest = this;

			// set transition handler
			if(mTransUpdateType == tutDivisibleFade || mTransUpdateType == tutDivisible)
				mDivisibleTransHandler = (DivisibleTransHandler) handler;
			if( mTransUpdateType == tutGiveUpdate) mGiveUpdateTransHandler = (GiveUpdateTransHandler) handler;

			// hold destination and source objects
			mTransDestObj = mOwner.get();
			//if(mTransDestObj!=null) mTransDestObj->AddRef();
			if(transsource!=null) mTransSrcObj = transsource.mOwner.get(); else mTransSrcObj = null;
			//if(mTransSrcObj!=null) mTransSrcObj->AddRef();

			// register to idle event handler
			mTransIdleCallback.Owner = this;
			if(!mTransSelfUpdate) TVP.EventManager.addContinuousEventHook(mTransIdleCallback);

			// initial tick count
			// TVPStartTickCount(); 要らない
			if( mUseTransTickCallback ) {
				mTransTick = 0;
				// initially 0
				// dummy calling StartProcess/EndProcess to notify initial tick count;
				// for first call with TransTick = 0, these method should not
				// return any error status here.
				if( mDivisibleTransHandler != null ) {
					mDivisibleTransHandler.startProcess(mTransTick);
					mDivisibleTransHandler.endProcess();
				} else if( mGiveUpdateTransHandler != null ) {
					;// not yet implemented
				}
			} else {
				mTransTick = getTransTick();
			}

			// set flag
			mInTransition = true;
			mTransCompEventPrevented = false;

			// update
			update(true);
		} finally {
			pro = null;
			sop = null;
			//handler = null;
		}
	}
	public void assignImages(LayerNI src) throws TJSException {
		// assign images
		boolean main_changed = true;

		if(src.mMainImage!=null) {
			if(mMainImage!=null)
				main_changed = mMainImage.assign(src.mMainImage);
			else
				mMainImage = new BaseBitmap(src.mMainImage);
			mFontChanged = true; // invalidate font assignment cache
		} else {
			deallocateImage();
		}

		if(src.mProvinceImage!=null) {
			if(mProvinceImage!=null)
				mProvinceImage.assign(src.mProvinceImage);
			else
				mProvinceImage = new BaseBitmap(src.mProvinceImage);
		} else {
			deallocateProvinceImage();
		}

		if(main_changed && mMainImage != null) {
			internalSetImageSize(mMainImage.getWidth(), mMainImage.getHeight());
				// adjust position
		}

		mImageModified = true;

		if(mMainImage!=null) resetClip();  // mClipRect is reset

		if(main_changed) update(false); // update
	}
	private final String getTypeNameString() {
		switch(mType) {
		case LayerType.ltBinder:		return "ltBinder";
		case LayerType.ltOpaque:		return "ltOpaque";
		case LayerType.ltAlpha:			return "ltAlpha";
		case LayerType.ltAdditive:		return "ltAdditive";
		case LayerType.ltSubtractive:	return "ltSubtractive";
		case LayerType.ltMultiplicative:return "ltMultiplicative";
		case LayerType.ltEffect:		return "ltEffect";
		case LayerType.ltFilter:		return "ltFilter";
		case LayerType.ltDodge:			return "ltDodge";
		case LayerType.ltDarken:		return "ltDarken";
		case LayerType.ltLighten:		return "ltLighten";
		case LayerType.ltScreen:		return "ltScreen";
		case LayerType.ltAddAlpha:		return "ltAddAlpha";
		case LayerType.ltPsNormal:		return "PsNormal";
		case LayerType.ltPsAdditive:	return "PsAdditive";
		case LayerType.ltPsSubtractive:	return "PsSubtractive";
		case LayerType.ltPsMultiplicative:return "PsMultiplicative";
		case LayerType.ltPsScreen:		return "PsScreen";
		case LayerType.ltPsOverlay:		return "PsOverlay";
		case LayerType.ltPsHardLight:	return "PsHardLight";
		case LayerType.ltPsSoftLight:	return "PsSoftLight";
		case LayerType.ltPsColorDodge:	return "PsColorDodge";
		case LayerType.ltPsColorDodge5:	return "PsColorDodge5";
		case LayerType.ltPsColorBurn:	return "PsColorBurn";
		case LayerType.ltPsLighten:		return "PsLighten";
		case LayerType.ltPsDarken:		return "PsDarken";
		case LayerType.ltPsDifference:	return "PsDifference";
		case LayerType.ltPsDifference5:	return "PsDifference5";
		case LayerType.ltPsExclusion:	return "PsExclusion";

		default:						return "unknown";
		}
	}
	public void dumpStructure() { dumpStructure(0); }
	public void dumpStructure(int level) {
		char[] indent = new char[level*2];
		StringBuilder builder  = new StringBuilder(256);
		for( int i =0; i<level*2; i++) indent[i] = ' ';

		String name = mName;
		if(name==null||name.length() == 0) name = "<noname>";

		builder.append( indent );
		builder.append( name );
		builder.append( String.format(" (object 0x%x)", mOwner.hashCode()) );
		builder.append( String.format(" (native 0x%x)", this.hashCode()) );
		builder.append( " (" );
		builder.append( mRect.left );
		builder.append( ',' );
		builder.append(mRect.top);
		builder.append(")-(");
		builder.append(mRect.right);
		builder.append(",");
		builder.append(mRect.bottom);
		builder.append(") (");
		builder.append(mRect.width());
		builder.append("x");
		builder.append(mRect.height());
		builder.append(")");
		builder.append(" ");
		builder.append(getVisible()?"visible":"invisible");
		builder.append(" index=" );
		builder.append(getAbsoluteOrderIndex());
		builder.append(mProvinceImage!=null?" p":"");
		builder.append(" ");
		builder.append(getTypeNameString());

		DebugClass.addLog( builder.toString() );

		level++;
		final int count = mChildren.getCount();
		for( int i = 0; i < count; i++ ) {
			LayerNI child = mChildren.get(i);
			if( child != null ) {
				child.dumpStructure(level);
			}
		}
	}
	public void dumpStructureEx() { dumpStructureEx(0); }
	public void dumpStructureEx(int level) {
		char[] indent = new char[level*2];
		StringBuilder builder  = new StringBuilder(256);
		for( int i =0; i<level*2; i++) indent[i] = ' ';

		String filename = mName + ".png";
		String name = mName;
		if(name==null||name.length() == 0) {
			name = "<noname>";
			filename = "noname_"+getAbsoluteOrderIndex()+".png";
		}

		builder.append( indent );
		builder.append( name );
		builder.append( String.format(" (object 0x%x)", mOwner.hashCode()) );
		builder.append( String.format(" (native 0x%x)", this.hashCode()) );
		builder.append( " (" );
		builder.append( mRect.left );
		builder.append( ',' );
		builder.append(mRect.top);
		builder.append(")-(");
		builder.append(mRect.right);
		builder.append(",");
		builder.append(mRect.bottom);
		builder.append(") (");
		builder.append(mRect.width());
		builder.append("x");
		builder.append(mRect.height());
		builder.append(")");
		builder.append(" ");
		builder.append(getVisible()?"visible":"invisible");
		builder.append(" index=" );
		builder.append(getAbsoluteOrderIndex());
		builder.append(mProvinceImage!=null?" p":"");
		builder.append(" ");
		builder.append(getTypeNameString());

		try {
			saveLayerImage(filename,"png");
		} catch (TJSException e) {
			builder.append(" file save error:"+e.getMessage());
		}
		DebugClass.addLog( builder.toString() );

		level++;
		final int count = mChildren.getCount();
		for( int i = 0; i < count; i++ ) {
			LayerNI child = mChildren.get(i);
			if( child != null ) {
				child.dumpStructureEx(level);
			}
		}
	}
	public void setHitTestWork(boolean b) {
		mOnHitTest_Work = b;
	}
	private LayerNI getNeighborAbove(boolean loop) {
		if(mManager==null) return null;

		int index = getOverallOrderIndex();
		ArrayList<LayerNI> allnodes = mManager.getAllNodes();

		if(allnodes.size() == 0) return null; // must be an error !!

		if(index == 0) {
			// first ( primary )
			if(loop)
				return allnodes.get(allnodes.size()-1);
			else
				return null;
		}

		return allnodes.get(index -1);
	}
	public LayerNI getPrevFocusableInternal() {
		// search next focusable layer backward
		LayerNI p = this;
		LayerNI current = this;

		p = p.getNeighborAbove(true);
		if(current == p) return null;
		if(p == null ) return null;
		current = p;
		do {
			if(p.getNodeFocusable() && p.mJoinFocusChain) return p; // next focusable layer found
			p = p.getNeighborAbove(true);
		} while( p != null && p != current );

		return null; // no layer found
	}
	static private final String ON_SEARCH_PREV_FOCUSABLE = "onSearchPrevFocusable";
	public LayerNI getPrevFocusable() {
		// search next focusable layer backward
		mFocusWork = getPrevFocusableInternal();
		Dispatch2 owner = mOwner.get();
		if( owner !=null && !mShutdown && (mFocusWork == null || mFocusWork.mOwner!=null) )
		{
			Variant[] param = new Variant[1];
			if(mFocusWork!=null)
				param[0] = new Variant(mFocusWork.mOwner.get(), mFocusWork.mOwner.get());
			else
				param[0] = new Variant(null,null);
			TVP.EventManager.postEvent( owner, owner, ON_SEARCH_PREV_FOCUSABLE, 0, EventManager.EPT_IMMEDIATE, param);
		}
		return mFocusWork;
	}
	public LayerNI searchFirstFocusable() { return searchFirstFocusable(true); }
	public LayerNI searchFirstFocusable(boolean ignore_chain_focusable) {
		if( ignore_chain_focusable ) {
			if(getNodeFocusable()) return this;
		} else {
			if(getNodeFocusable() && mJoinFocusChain) return this;
		}
		final int count = mChildren.getCount();
		for( int i = 0; i < count; i++ ) {
			LayerNI child = mChildren.get(i);
			if( child != null ) {
				LayerNI lay = child.searchFirstFocusable(ignore_chain_focusable);
				if(lay!=null) return lay;
			}
		}
		return null;
	}
	public void fromPrimaryCoordinates(int[] x, int[] y) {
		LayerNI l = this;
		while( l != null && !l.isPrimary() ) {
			x[0] -= l.mRect.left;
			y[0] -= l.mRect.top;
			l = l.mParent;
		}
	}
	static private final String ON_CLICK = "onClick";
	public void fireClick(int x, int y) {
		Dispatch2 owner = mOwner.get();
		if( owner != null && !mShutdown ) {
			Variant[] param = {new Variant(x),new Variant(y)};
			TVP.EventManager.postEvent(owner, owner, ON_CLICK, 0, EventManager.EPT_IMMEDIATE, param);
		}
	}
	static private final String ON_DOUBLE_CLICK = "onDoubleClick";
	public void fireDoubleClick(int x, int y) {
		Dispatch2 owner = mOwner.get();
		if( owner != null && !mShutdown) {
			Variant[] param = {new Variant(x),new Variant(y)};
			TVP.EventManager.postEvent(owner, owner, ON_DOUBLE_CLICK, 0, EventManager.EPT_IMMEDIATE, param);
		}
	}
	static private final String ON_MOUSE_DOWN = "onMouseDown";
	public void fireMouseDown(int x, int y, int mb, int flags) {
		Dispatch2 owner = mOwner.get();
		if( owner != null && !mShutdown ) {
			Variant[] param = {new Variant(x), new Variant(y), new Variant(mb), new Variant(flags) };
			TVP.EventManager.postEvent( owner, owner, ON_MOUSE_DOWN, 0, EventManager.EPT_IMMEDIATE, param);
		}
	}
	static private final String ON_MOUSE_UP = "onMouseUp";
	public void fireMouseUp(int x, int y, int mb, int flags) {
		Dispatch2 owner = mOwner.get();
		if( owner != null && !mShutdown ) {
			Variant[] param = {new Variant(x), new Variant(y), new Variant(mb), new Variant(flags) };
			TVP.EventManager.postEvent( owner, owner, ON_MOUSE_UP, 0, EventManager.EPT_IMMEDIATE, param);
		}
	}
	static private final String ON_MOUSE_MOVE = "onMouseMove";
	public void fireMouseMove(int x, int y, int flags) {
		Dispatch2 owner = mOwner.get();
		if( owner != null && !mShutdown ) {
			Variant[] param = {new Variant(x), new Variant(y), new Variant(flags) };
			TVP.EventManager.postEvent( owner, owner, ON_MOUSE_MOVE, 0, EventManager.EPT_IMMEDIATE|EventManager.EPT_DISCARDABLE, param);
		}
	}
	static private final String ON_MOUSE_ENTER = "onMouseEnter";
	public void fireMouseEnter() {
		Dispatch2 owner = mOwner.get();
		if( owner != null && !mShutdown ){
			TVP.EventManager.postEvent( owner, owner, ON_MOUSE_ENTER, 0, EventManager.EPT_IMMEDIATE, TJS.NULL_ARG );
		}
	}
	static private final String ON_MOUSE_LEAVE = "onMouseLeave";
	public void fireMouseLeave() {
		Dispatch2 owner = mOwner.get();
		if( owner != null && !mShutdown ) {
			TVP.EventManager.postEvent( owner, owner, ON_MOUSE_LEAVE, 0, EventManager.EPT_IMMEDIATE, TJS.NULL_ARG );
		}
	}
	private int getLayerActiveCursor() {
		// return layer's actual (active) mouse cursor
		int cursor = mCursor;
		LayerNI p = this;
		while( cursor == 0 ) { // while cursor is 0 ( crDefault ) .. look up parent layer
			p = p.mParent;
			if( p == null ) break;
			cursor = p.mCursor;
		}
		return cursor;
	}
	public void setCurrentCursorToWindow() {
		// set current layer cusor to the window
		if( mManager != null ) {
			mManager.setMouseCursor(getLayerActiveCursor());
		}
	}
	public void setCurrentHintToWindow() {
		// set current hint to the window
		if( mManager != null ) {
			LayerNI p = this;
			while( p.mShowParentHint ) {
				if( p.mParent == null ) break;
				p = p.mParent;
			}
			mManager.setHint(p.mHint);
		}
	}
	static private final String ON_KEY_DOWN = "onKeyDown";
	public void fireKeyDown(int key, int shift) {
		Dispatch2 owner = mOwner.get();
		if( owner != null && !mShutdown ) {
			Variant[] param = {new Variant(key), new Variant(shift), new Variant(1/* true */)};
			TVP.EventManager.postEvent( owner, owner, ON_KEY_DOWN, 0, EventManager.EPT_IMMEDIATE, param );
		}
	}
	public void defaultKeyDown(int key, int shift) throws TJSException {
		// default keyboard behavior
		// this method is to be called by default onKeyDown event handler
		if(mManager==null) return;

		boolean no_shift_downed =  (shift & WindowForm.SS_SHIFT) == 0 && (shift & WindowForm.SS_ALT) == 0 && (shift & WindowForm.SS_CTRL) == 0;

		if( (key == VirtualKey.VK_TAB || key == VirtualKey.VK_RIGHT || key == VirtualKey.VK_DOWN) && no_shift_downed) {
			// [TAB] [<RIGHT>] [<DOWN>] : to next focusable
			mManager.focusNext();
		} else if((key == VirtualKey.VK_TAB && (shift & WindowForm.SS_SHIFT)!=0 && (shift & WindowForm.SS_ALT)==0 && (shift & WindowForm.SS_CTRL)==0) || key == VirtualKey.VK_LEFT || key == VirtualKey.VK_UP)
		{
			// [SHIFT]+[TAB] [<LEFT>] [<UP>] : to previous focusable
			mManager.focusPrev();
		} else if((key == VirtualKey.VK_RETURN || key == VirtualKey.VK_ESCAPE) && no_shift_downed) {
			// [ENTER] or [ESC] : pass to parent layer
			if( mParent != null ) {
				if( mParent.getNodeEnabled()) mParent.fireKeyDown(key, shift);
			}
		}
	}
	static private final String ON_KEY_UP = "onKeyUp";
	public void fireKeyUp(int key, int shift) {
		Dispatch2 owner = mOwner.get();
		if( owner != null && !mShutdown ) {
			Variant[] param = { new Variant(key), new Variant(shift), new Variant(1/* true */) };
			TVP.EventManager.postEvent( owner, owner, ON_KEY_UP, 0, EventManager.EPT_IMMEDIATE, param);
		}
	}
	public void defaultKeyUp(int key, int shift) {
		// default keyboard behavior
		if(mManager==null) return;
		boolean no_shift_downed =  (shift & WindowForm.SS_SHIFT)==0 && (shift & WindowForm.SS_ALT)==0 && (shift & WindowForm.SS_CTRL)==0;
		if( (key == VirtualKey.VK_RETURN || key == VirtualKey.VK_ESCAPE) && no_shift_downed ) {
			// [ENTER] or [ESC] : pass to parent layer
			if( mParent != null ) {
				if( mParent.getNodeEnabled() ) mParent.fireKeyUp(key, shift);
			}
		}
	}
	static private final String ON_KEY_PRESS = "onKeyPress";
	public void fireKeyPress(char key) {
		Dispatch2 owner = mOwner.get();
		if( owner != null && !mShutdown ) {
			Variant[] param = {new Variant(String.valueOf(key)), new Variant(1/* true */)};
			TVP.EventManager.postEvent( owner, owner, ON_KEY_PRESS, 0, EventManager.EPT_IMMEDIATE, param);
		}
	}
	public void defaultKeyPress(char key) {
		// default keyboard behavior
		if(mManager==null) return;
		if( key == 13/* enter */ || key == 0x1b/* esc */ ) {
			if( mParent != null ) {
				if( mParent.getNodeEnabled() ) mParent.fireKeyPress(key);
			}
		}
	}
	static private final String ON_MOUSE_WHEEL = "onMouseWheel";
	public void fireMouseWheel(int shift, int delta, int x, int y) {
		Dispatch2 owner = mOwner.get();
		if( owner != null && !mShutdown ) {
			Variant[] val = { new Variant(shift), new Variant(delta), new Variant(x), new Variant(y) };
			TVP.EventManager.postEvent(owner, owner, ON_MOUSE_WHEEL, 0, EventManager.EPT_IMMEDIATE, val);
		}
	}
	public void setFocusWork(LayerNI lay) {
		mFocusWork = lay;
	}
	public void setParent(LayerNI parent) throws TJSException {
		join(parent);
	}
	public Dispatch2 getChildrenArrayObject() throws VariantException, TJSException {
		if(mChildrenArray==null){
			// create an Array object
			Holder<Dispatch2> classobj = new Holder<Dispatch2>(null);
			mChildrenArray = TJS.createArrayObject(classobj);
			try {
				Variant val = new Variant();;
				int er = classobj.mValue.propGet(0, "clear", val, classobj.mValue );
					// retrieve clear method
				if( er < 0 ) Message.throwExceptionMessage(Message.InternalError);
				mArrayClearMethod = val.asObject();
			} catch( TJSException e ) {
				mChildrenArray = null;
				classobj = null;
				throw e;
			}
			classobj = null;
		}

		if(!mChildrenArrayValid) {
			// re-create children list
			mArrayClearMethod.funcCall(0, null, null, TJS.NULL_ARG, mChildrenArray);
				// clear array

			final int total = mChildren.getCount();
			int count = 0;
			for( int i = 0; i < total; i++ ) {
				LayerNI child = mChildren.get(i);
				if( child != null ) {
					Dispatch2 dsp = child.mOwner.get();
					Variant val = new Variant(dsp, dsp);
					mChildrenArray.propSetByNum( Interface.MEMBERENSURE, count, val, mChildrenArray );
					count++;
				}
			}
			mChildrenArrayValid = true;
		}

		return mChildrenArray;
	}

	public boolean getAbsoluteOrderMode()  {
		return mAbsoluteOrderMode;
	}
	boolean getCached() {
		return mCached;
	}
	public WindowNI getWindow() {
		if(mManager==null) return null;
		return mManager.getWindow();
	}
	public void setLeft(int left) throws TJSException {
		if( mRect.left != left) {
			boolean visible = getVisible() || getNodeVisible();
			if( isPrimary() && left != 0)
				Message.throwExceptionMessage(Message.CannotMovePrimary);
			if(visible) parentUpdate();
			int w = mRect.width();
			mRect.left = left;
			mRect.right = w + mRect.left;
			if(mParent!=null) mParent.notifyChildrenVisualStateChanged();
			// OriginalTODO: SetLeft
			if(visible) parentUpdate();
		}
	}
	public void setTop(int top) throws TJSException {
		if(mRect.top != top) {
			boolean visible = getVisible() || getNodeVisible();
			if( isPrimary() && top != 0)
				Message.throwExceptionMessage(Message.CannotMovePrimary);
			if(visible) parentUpdate();
			int h = mRect.height();
			mRect.top = top;
			mRect.bottom = h + mRect.top;
			if(mParent!=null) mParent.notifyChildrenVisualStateChanged();
			// OriginalTODO: SetTop;
			if(visible) parentUpdate();
		}
	}
	public int getImageLeft() throws TJSException {
		if(mMainImage==null) Message.throwExceptionMessage(Message.NotDrawableLayerType);
		return mImageLeft;
	}
	public void setImageLeft(int left) throws TJSException {
		if(mMainImage==null) Message.throwExceptionMessage(Message.NotDrawableLayerType);
		if(mImageLeft != left) {
			if(left > 0) Message.throwExceptionMessage(Message.InvalidImagePosition);
			if( mMainImage.getWidth() + left < mRect.width())
				Message.throwExceptionMessage(Message.InvalidImagePosition);
			mImageLeft = left;
			update();
		}
	}
	public int getImageTop() throws TJSException {
		if(mMainImage==null) Message.throwExceptionMessage(Message.NotDrawableLayerType);
		return mImageTop;
	}
	public void setImageTop(int top) throws TJSException {
		if(mMainImage==null) Message.throwExceptionMessage(Message.NotDrawableLayerType);
		if(mImageTop != top) {
			if(top > 0) Message.throwExceptionMessage(Message.InvalidImagePosition);
			if( mMainImage.getHeight() + top < mRect.height())
				Message.throwExceptionMessage(Message.InvalidImagePosition);
			mImageTop = top;
			update();
		}
	}
	public int getType() {
		return mType;
	}
	/*
	private static int rgba2Color( int r, int g, int b, int a ) {
		return (((a)<<24) +  (((r)<<16) + ((g)<<8) + (b)));
	}
	*/
	public void setType(int type) throws TJSException {
		// set layer type to "type"
		if(mType != type) {
			mType = type;
			switch(mType) {
			case LayerType.ltBinder:
				mNeutralColor = mTransparentColor = TRANS_WHITE;//rgba2Color(255, 255, 255, 0);
				mDisplayType = mType;
				mCanHaveImage = false;
				deallocateImage();
				break;

			case LayerType.ltOpaque: // formerly ltCoverRect
				mNeutralColor = mTransparentColor = TRANS_WHITE;//rgba2Color(255, 255, 255, 0);
				mDisplayType = mType;
				mCanHaveImage = true;
				allocateImage();
				break;

			case LayerType.ltAlpha: // formerly ltTransparent
				mNeutralColor = mTransparentColor = TRANS_WHITE;//rgba2Color(255, 255, 255, 0);
				mDisplayType = mType;
				mCanHaveImage = true;
				allocateImage();
				break;

			case LayerType.ltAdditive:
				mNeutralColor = mTransparentColor = TRANS_BLACK;//rgba2Color(0, 0, 0, 0);
				mDisplayType = mType;
				mCanHaveImage = true;
				allocateImage();
				break;

			case LayerType.ltSubtractive:
				mNeutralColor = mTransparentColor = TRANS_WHITE;//rgba2Color(255, 255, 255, 0);
				mDisplayType = mType;
				mCanHaveImage = true;
				allocateImage();
				break;

			case LayerType.ltMultiplicative:
				mNeutralColor = mTransparentColor = TRANS_WHITE;//rgba2Color(255, 255, 255, 0);
				mDisplayType = mType;
				mCanHaveImage = true;
				allocateImage();
				break;

			case LayerType.ltEffect:
				mNeutralColor = mTransparentColor = TRANS_WHITE;//rgba2Color(255, 255, 255, 0);
				mDisplayType = LayerType.ltBinder;  // OriginalTODO: retrieve actual DrawType
				mCanHaveImage = false;
				deallocateImage();
				break;

			case LayerType.ltFilter:
				mNeutralColor = mTransparentColor = TRANS_WHITE;//rgba2Color(255, 255, 255, 0);
				mDisplayType = LayerType.ltBinder;  // OriginalTODO: retrieve actual mDisplayType
				mCanHaveImage = false;
				deallocateImage();
				break;

			case LayerType.ltDodge:
				mNeutralColor = mTransparentColor = TRANS_BLACK;//rgba2Color(0, 0, 0, 0);
				mDisplayType = mType;
				mCanHaveImage = true;
				allocateImage();
				break;

			case LayerType.ltDarken:
				mNeutralColor = mTransparentColor = TRANS_WHITE;//rgba2Color(255, 255, 255, 0);
				mDisplayType = mType;
				mCanHaveImage = true;
				allocateImage();
				break;

			case LayerType.ltLighten:
				mNeutralColor = mTransparentColor = TRANS_BLACK;//rgba2Color(0, 0, 0, 0);
				mDisplayType = mType;
				mCanHaveImage = true;
				allocateImage();
				break;

			case LayerType.ltScreen:
				mNeutralColor = mTransparentColor = TRANS_BLACK;//rgba2Color(0, 0, 0, 0);
				mDisplayType = mType;
				mCanHaveImage = true;
				allocateImage();
				break;

			case LayerType.ltAddAlpha:
				mNeutralColor = mTransparentColor = TRANS_BLACK;//rgba2Color(0, 0, 0, 0);
				mDisplayType = mType;
				mCanHaveImage = true;
				allocateImage();
				break;

			case LayerType.ltPsNormal:
				mNeutralColor = mTransparentColor = TRANS_BLACK;//rgba2Color(0, 0, 0, 0);
				mDisplayType = mType;
				mCanHaveImage = true;
				allocateImage();
				break;

			case LayerType.ltPsAdditive:
				mNeutralColor = mTransparentColor = TRANS_BLACK;//rgba2Color(0, 0, 0, 0);
				mDisplayType = mType;
				mCanHaveImage = true;
				allocateImage();
				break;

			case LayerType.ltPsSubtractive:
				mNeutralColor = mTransparentColor = TRANS_WHITE;//rgba2Color(255, 255, 255, 0);
				mDisplayType = mType;
				mCanHaveImage = true;
				allocateImage();
				break;

			case LayerType.ltPsMultiplicative:
				mNeutralColor = mTransparentColor = TRANS_WHITE;//rgba2Color(255, 255, 255, 0);
				mDisplayType = mType;
				mCanHaveImage = true;
				allocateImage();
				break;

			case LayerType.ltPsScreen:
				mNeutralColor = mTransparentColor = TRANS_BLACK;//rgba2Color(0, 0, 0, 0);
				mDisplayType = mType;
				mCanHaveImage = true;
				allocateImage();
				break;

			case LayerType.ltPsOverlay:
				mNeutralColor = mTransparentColor = TRANS_GLAY;//rgba2Color(128, 128, 128, 0);
				mDisplayType = mType;
				mCanHaveImage = true;
				allocateImage();
				break;

			case LayerType.ltPsHardLight:
				mNeutralColor = mTransparentColor = TRANS_GLAY;//rgba2Color(128, 128, 128, 0);
				mDisplayType = mType;
				mCanHaveImage = true;
				allocateImage();
				break;

			case LayerType.ltPsSoftLight:
				mNeutralColor = mTransparentColor = TRANS_GLAY;//rgba2Color(128, 128, 128, 0);
				mDisplayType = mType;
				mCanHaveImage = true;
				allocateImage();
				break;

			case LayerType.ltPsColorDodge:
				mNeutralColor = mTransparentColor = TRANS_BLACK;//rgba2Color(0, 0, 0, 0);
				mDisplayType = mType;
				mCanHaveImage = true;
				allocateImage();
				break;

			case LayerType.ltPsColorDodge5:
				mNeutralColor = mTransparentColor = TRANS_BLACK;//rgba2Color(0, 0, 0, 0);
				mDisplayType = mType;
				mCanHaveImage = true;
				allocateImage();
				break;

			case LayerType.ltPsColorBurn:
				mNeutralColor = mTransparentColor = TRANS_WHITE;//rgba2Color(255, 255, 255, 0);
				mDisplayType = mType;
				mCanHaveImage = true;
				allocateImage();
				break;

			case LayerType.ltPsLighten:
				mNeutralColor = mTransparentColor = TRANS_BLACK;//rgba2Color(0, 0, 0, 0);
				mDisplayType = mType;
				mCanHaveImage = true;
				allocateImage();
				break;

			case LayerType.ltPsDarken:
				mNeutralColor = mTransparentColor = TRANS_WHITE;//rgba2Color(255, 255, 255, 0);
				mDisplayType = mType;
				mCanHaveImage = true;
				allocateImage();
				break;

			case LayerType.ltPsDifference:
				mNeutralColor = mTransparentColor = TRANS_BLACK;//rgba2Color(0, 0, 0, 0);
				mDisplayType = mType;
				mCanHaveImage = true;
				allocateImage();
				break;

			case LayerType.ltPsDifference5:
				mNeutralColor = mTransparentColor = TRANS_BLACK;//rgba2Color(0, 0, 0, 0);
				mDisplayType = mType;
				mCanHaveImage = true;
				allocateImage();
				break;

			case LayerType.ltPsExclusion:
				mNeutralColor = mTransparentColor = TRANS_BLACK;//rgba2Color(0, 0, 0, 0);
				mDisplayType = mType;
				mCanHaveImage = true;
				allocateImage();
				break;

			}
			notifyLayerTypeChange();
			setToCreateExposedRegion();
			update();
		}
	}
	private void allocateImage() throws TJSException {
		if(mMainImage==null) {
			mImageLeft = 0;
			mImageTop = 0;
			mMainImage = new BaseBitmap(mRect.width(), mRect.height(), 32);
			mMainImage.fill( new Rect(0, 0, mRect.width(), mRect.height()), mNeutralColor);
			mMainImage.setFont(mFont); // set font
		}

		if(mMainImage!=null) resetClip();  // cliprect is reset

		if(mProvinceImage!=null) 		{
			mProvinceImage.setSizeWithFill(mMainImage.getWidth(), mMainImage.getHeight(), 0);
		}

		mFontChanged = true; // invalidate font assignment cache
		mImageModified = true;
	}
	private void notifyLayerTypeChange() {
		updateDrawFace();
		if(mParent!=null) mParent.notifyLayerTypeChange();
	}
	public int getFace() {
		return mFace;
	}
	public void setFace(int f) {
		mFace = f;
		updateDrawFace();
	}
	public boolean getHoldAlpha() {
		return mHoldAlpha;
	}
	public void setHoldAlpha(boolean b) {
		mHoldAlpha = b;
	}
	public int getClipLeft() {
		return mClipRect.left;
	}
	public void setClipLeft(int left) throws TJSException {
		setClip(left, getClipTop(), getClipWidth(), getClipHeight());
	}
	int getClipHeight() {
		return mClipRect.bottom - mClipRect.top;
	}
	int getClipWidth() {
		return mClipRect.right - mClipRect.left;
	}
	int getClipTop() {
		return mClipRect.top;
	}
	public void setClipTop(int top) throws TJSException {
		setClip(getClipLeft(), top, getClipWidth(), getClipHeight());
	}
	public void setClipWidth(int width) throws TJSException {
		setClip(getClipLeft(), getClipTop(), width, getClipHeight());
	}
	public void setClipHeight(int height) throws TJSException {
		setClip(getClipLeft(), getClipTop(), getClipWidth(), height);
	}
	public boolean getImageModified() {
		return mImageModified;
	}
	public void setImageModified(boolean b) {
		mImageModified = b;
	}
	public int getHitType() {
		return mHitType;
	}
	public void setHitType(int type) {
		mHitType = type;
	}
	public int getHitThreshold() {
		return mHitThreshold;
	}
	public void setHitThreshold(int t) {
		mHitThreshold = t;
	}
	public int getCursor() {
		return mCursor;
	}
	public void setCursorByStorage(String storage) throws TJSException {
		WindowNI win = (WindowNI)getWindow();
		if( win != null ) {
			WindowForm form = win.getForm();
			if( form != null ) {
				mCursor = TVP.MouseCursor.getCursor(form, storage);
			} else {
				Message.throwExceptionMessage(Message.CannotLoadCursor, storage);
			}
		} else {
			Message.throwExceptionMessage(Message.CannotLoadCursor, storage);
		}
		if(mManager!=null) mManager.notifyMouseCursorChange(this, getLayerActiveCursor());
	}
	public void setCursorByNumber(int num) throws TJSException {
		mCursor = num;
		if(mManager!=null) mManager.notifyMouseCursorChange(this, getLayerActiveCursor());
	}
	public int getCursorX() {
		int x;
		if(mManager==null) return 0;
		Point pt = new Point(0,0);
		mManager.getCursorPos( pt );
		x = pt.x;
		LayerNI p = this;
		while(p!=null) {
			if(p.mParent==null) break;
			x -= p.mRect.left;
			p = p.mParent;
		}
		return x;
	}
	public void setCursorX(int x) {
		mCursorX_Work = x; // once store to this variable;
		// cursor moves on call to SetCursorY
	}
	public int getCursorY() {
		int y;
		if(mManager==null) return 0;
		Point pt = new Point(0,0);
		mManager.getCursorPos( pt );
		y = pt.y;
		LayerNI p = this;
		while(p!=null) {
			if(p.mParent==null) break;
			y -= p.mRect.left;
			p = p.mParent;
		}
		return y;
	}
	public void setCursorY(int y) {
		if(mManager==null) return;
		int x = mCursorX_Work;
		LayerNI p = this;
		while( p!=null ) 		{
			if(p.mParent==null) break;
			x += p.mRect.left;
			y += p.mRect.top;
			p = p.mParent;
		}
		mManager.setCursorPos(x, y);
	}
	public String getHint() { return mHint; }
	public void setHint(String hint) throws TJSException {
		mShowParentHint = false;
		mHint = hint;
		if(mManager!=null)
			mManager.notifyHintChange(this, hint);
	}
	public boolean getShowParentHint() {
		return mShowParentHint;
	}
	public void setShowParentHint(boolean b) {
		mShowParentHint = b;
	}
	public boolean getFocusable() {
		return mFocusable;
	}
	public void setFocusable(boolean b) throws TJSException {
		if(mFocusable != b) {
			boolean bstate = getNodeFocusable();
			mFocusable = b;
			boolean astate = getNodeFocusable();
			if(bstate != astate) {
				if(!astate) {
					// remove focus from this layer
					if(mManager!=null) {
						if(mManager.getFocusedLayer() == this)
							mManager.setFocusTo(getNextFocusable(), true); // blur
					}
				}
			}
		}
	}
	public boolean getJoinFocusChain() {
		return mJoinFocusChain;
	}
	public void setJoinFocusChain(boolean b) {
		mJoinFocusChain = b;
	}
	public boolean getFocused() {
		if(mManager==null) return false;
		return mManager.getFocusedLayer() == this;
	}
	public void setEnabled(boolean b) throws TJSException {
		// set enabled
		if(mEnabled != b) {
			if(mManager!=null) mManager.saveEnabledWork();

			try {
				mEnabled = b;
				if(mEnabled) {
					// become enabled
					if(mManager!=null) mManager.checkTreeFocusableState(this);
				} else {
					// become disabled
					if(mManager!=null) mManager.blurTree(this);
				}
			} finally {
				if(mManager!=null) mManager.notifyNodeEnabledState();
			}
		}
	}
	public void setAttentionLeft(int l) {
		mAttentionLeft = l;
		if(mManager!=null) mManager.notifyAttentionStateChanged(this);
	}
	public void setAttentionTop(int t) {
		mAttentionTop = t;
		if(mManager!=null) mManager.notifyAttentionStateChanged(this);
	}
	public void setUseAttention(boolean b) {
		mUseAttention = b ? 1 : 0;
		if(mManager!=null) mManager.notifyAttentionStateChanged(this);
	}
	public void setImeMode(int mode) {
		mImeMode = mode;
		if(mManager!=null) mManager.notifyImeModeChanged(this);
	}
	public boolean getCallOnPaint() {
		return mCallOnPaint;
	}
	public void setCallOnPaint(boolean b) {
		mCallOnPaint = b;
	}
	public String getName() {
		return mName;
	}
	public void setName(String name) {
		mName = name;
	}
	public int getNeutralColor() {
		return mNeutralColor;
	}
	public void setNeutralColor(int color) {
		mNeutralColor = color;
	}
	public boolean getHasImage() {
		return mMainImage != null;
	}
	public void setHasImage(boolean b) throws TJSException {
		if(!mCanHaveImage && b)
			Message.throwExceptionMessage(Message.LayerCannotHaveImage);
		if(b) allocateImage();
		else deallocateImage();
		notifyChildrenVisualStateChanged();
		update();
	}
	public void purgeImage() {
		mMainImage.purgeImage();
	}
	@Override
	public String toString() {
		if( mName != null ) return "Layer - " + mName;
		return "Layer - no name";
	}
}
