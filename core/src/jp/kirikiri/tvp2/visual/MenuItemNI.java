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
import jp.kirikiri.tjs2.NativeInstance;
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
import jp.kirikiri.tvp2.utils.ObjectList;
import jp.kirikiri.tvp2env.MenuItem;


public class MenuItemNI extends NativeInstanceObject {

	ObjectList<MenuItemNI> mChildren;

	private WindowNI mWindow;

	private WeakReference<Dispatch2> mOwner;
	private VariantClosure mActionOwner; // object to send action

	private MenuItemNI mParent;

	private boolean mChildrenArrayValid;
	private Dispatch2 mChildrenArray;
	private Dispatch2 mArrayClearMethod;

	private ArrayList<Dispatch2> mChildOwnerReference;

	private MenuItem mMenuItem;
	private String mCaption;
	//private String mShortcut;

	public MenuItemNI() {
		/* 初期値なので初期化不要
		mOwner = null;
		mWindow = null;
		mParent = null;
		mChildrenArrayValid = false;
		mChildrenArray = null;
		mArrayClearMethod = null;
		*/

		mChildren = new ObjectList<MenuItemNI>();
		mActionOwner = new VariantClosure(null);
		mChildOwnerReference = new ArrayList<Dispatch2>();
	}
	public final int construct( Variant[] param, Dispatch2 tjs_obj ) throws VariantException, TJSException {
		if(param.length < 1) return Error.E_BADPARAMCOUNT;

		int hr = super.construct(param, tjs_obj);
		if( hr < 0 ) return hr;

		mActionOwner.set( param[0].asObjectClosure() );
		//mActionOwner = param[0].asObjectClosure();
		mOwner = new WeakReference<Dispatch2>(tjs_obj);

		if(param.length >= 2) {
			if(param[1].isObject() ) {
				// is this Window instance ?
				VariantClosure clo = param[1].asObjectClosure();
				if(clo.mObject == null) Message.throwExceptionMessage(Message.SpecifyWindow);

				mWindow = (WindowNI)clo.mObject.getNativeInstance( WindowClass.ClassID );
				if( mWindow == null )
					Message.throwExceptionMessage(Message.SpecifyWindow);
			}
		}
		// create or attach MenuItem object
		if( mWindow!=null ) {
			mMenuItem = mWindow.getRootMenuItem();
		} else {
			mMenuItem = new MenuItem(this);
			//mMenuItem.OnClick = MenuItemClick;
		}

		// fetch initial caption
		if( mWindow==null && param.length >= 2 ) {
			mCaption = param[1].asString();
			mMenuItem.setCaption( mCaption );
		}

		return Error.S_OK;
	}
	public final void invalidate() throws VariantException, TJSException {
		boolean dodeletemenuitem = (mWindow == null);

		TVP.EventManager.cancelSourceEvents(mOwner.get());
		TVP.EventManager.cancelInputEvents(this);

		try { // locked
			mChildren.safeLock();
			int count = mChildren.getSafeLockedObjectCount();
			for( int i = 0; i < count; i++) {
				MenuItemNI item = mChildren.getSafeLockedObjectAt(i);
				if( item == null ) continue;

				Dispatch2 itemowner = item.mOwner.get();
				if( itemowner != null ) {
					itemowner.invalidate( 0, null, itemowner );
					item.mOwner = null;
				}
			}
		} finally {
			mChildren.safeUnlock();
		} // locked

		mWindow = null;
		mParent = null;

		mChildrenArray = null;
		mArrayClearMethod = null;

		mActionOwner.mObjThis = mActionOwner.mObject = null;
		//mActionOwner = null;

		super.invalidate();

		// delete object
		if(dodeletemenuitem) mMenuItem = null;
	}

	public final static MenuItemNI castFromVariant( final Variant from ) throws TJSException {
		if( from.isObject() ) {
			// is this Window instance ?
			VariantClosure clo = from.asObjectClosure();
			if(clo.mObject == null) Message.throwExceptionMessage(Message.SpecifyMenuItem);

			MenuItemNI ni = (MenuItemNI)clo.mObject.getNativeInstance(MenuItemClass.mClassID );
			if( ni == null )
				Message.throwExceptionMessage(Message.SpecifyMenuItem);
			return ni;
		}
		Message.throwExceptionMessage(Message.SpecifyMenuItem);
		return null;
	}

	/**
	 * returns whether events can be delivered
	 * @return
	 */
	private final boolean canDeliverEvents() {
		if(mMenuItem==null) return false;
		boolean enabled = true;
		MenuItem item = mMenuItem;
		while( item != null ) {
			if( !item.getEnabled() ) {
				enabled = false;
				break;
			}
			item = item.getParent();
		}
		return enabled;
	}

	private final void addChild( MenuItemNI item ) {
		if( mChildren.add(item) ) {
			mChildrenArrayValid = false;
			//if(item.mOwner) item.mOwner.addRef();
			Dispatch2 itemowner = item.mOwner.get();
			if( itemowner != null ) mChildOwnerReference.add( itemowner ); // 参照保持
			item.mParent = this;
		}
	}
	private final void removeChild( MenuItemNI item ) {
		if( mChildren.remove(item) ) {
			mChildrenArrayValid = false;
			//if( item.mOwner != null ) item.mOwner.release();
			Dispatch2 itemowner = item.mOwner.get();
			if( itemowner != null ) mChildOwnerReference.remove( itemowner ); // 参照を外す
			item.mParent = null;
		}
	}

	public final void add( MenuItemNI item ) throws TJSException {
		if( mMenuItem != null && item.mMenuItem != null ) {
			mMenuItem.add(item.mMenuItem);
			addChild( item );
		}
	}
	public final void insert( MenuItemNI item, int index ) throws TJSException {
		if( mMenuItem != null && item.mMenuItem != null ) {
			mMenuItem.insert(index, item.mMenuItem);
			addChild(item);
		}

	}
	public final void remove( MenuItemNI item ) throws TJSException {
		if( mMenuItem!=null && item.mMenuItem!=null) {
			int index = mMenuItem.indexOf(item.mMenuItem);
			if(index == -1) Message.throwExceptionMessage(Message.NotChildMenuItem);

			mMenuItem.delete(index);
			removeChild(item);
		}
	}


	public final VariantClosure getActionOwner() { return mActionOwner; }

	public final Dispatch2 getOwner() { return mOwner.get(); }

	public final MenuItemNI getParent() { return mParent; }

	public final MenuItemNI getRootMenuItem() {
		MenuItemNI current = this;
		MenuItemNI parent = current.getParent();
		while( parent != null ) {
			current = parent;
			parent = current.getParent();
		}
		return current;
	}

	public final WindowNI getWindow() { return mWindow; }

	public final Dispatch2 getChildrenArray() throws VariantException, TJSException {
		if( mChildrenArray == null) {
			// create an Array object
			Holder<Dispatch2> classobj = new Holder<Dispatch2>(null);
			mChildrenArray = TJS.createArrayObject(classobj);
			try {
				Variant val = new Variant();
				// retrieve clear method
				int er = classobj.mValue.propGet(0, "clear", val, classobj.mValue );
				if( er < 0 ) Message.throwExceptionMessage( Message.InternalError );
				mArrayClearMethod = val.asObject();
			} catch( TJSException e ) {
				mChildrenArray = null;
				classobj.mValue = null;
				classobj = null;
				throw e;
			}
			classobj.mValue = null;
			classobj = null;
		}

		if(!mChildrenArrayValid) {
			// re-create children list
			mArrayClearMethod.funcCall(0, null, null, null, mChildrenArray );
				// clear array

			try { // locked
				mChildren.safeLock();
				final int count = mChildren.getSafeLockedObjectCount();
				int itemcount = 0;
				for( int i = 0; i < count; i++ ) {
					MenuItemNI item = mChildren.getSafeLockedObjectAt(i);
					if( item == null ) continue;

					Dispatch2 dsp = item.mOwner.get();
					Variant val = new Variant(dsp, dsp);
					mChildrenArray.propSetByNum(Interface.MEMBERENSURE, itemcount, val, mChildrenArray );
					itemcount++;
				}
			} finally {
				mChildren.safeUnlock();
			} // locked

			mChildrenArrayValid = true;
		}

		return mChildrenArray;
	}


	static final String ON_CLICK = "onClick";
	/**
	 * fire onClick event
	 */
	public final void onClick() {
		// fire onClick event
		if(!canDeliverEvents()) return;

		// also check window
		MenuItemNI item = this;
		while( item.mWindow == null ) {
			if( item.mParent == null ) break;
			item = item.mParent;
		}
		if( item.mWindow == null ) return;
		if( !item.mWindow.canDeliverEvents() ) return;

		// fire event
		Dispatch2 owner = mOwner.get();
		TVP.EventManager.postEvent( owner, owner, ON_CLICK , 0, EventManager.EPT_IMMEDIATE, TJS.NULL_ARG );
	}

	public void menuItemClick() {
		// event handler
		// post to the event queue
		TVP.EventManager.postInputEvent(new WindowEvents.OnMenuItemClickInputEvent(this), 0 );
	}

	public void setCaption( final String caption) {
		if(mMenuItem==null) return;
		mCaption = caption;
		//mMenuItem.mAutoHotkeys = maManual;
		mMenuItem.setCaption( caption );
	}
	public String getCaption() {
		if(mMenuItem==null) return null;
		return mCaption;
	}

	public void setChecked(boolean b) {
		if(mMenuItem==null) return;
		mMenuItem.setChecked( b );
	}
	public boolean getChecked() {
		if(mMenuItem==null) return false;
		return mMenuItem.getChecked();
	}

	public void setEnabled(boolean b) {
		if(mMenuItem==null) return;
		mMenuItem.setEnabled( b );
	}
	public boolean getEnabled() {
		if(mMenuItem==null) return false;
		return mMenuItem.getEnabled();
	}

	public void setGroup(int g) {
		if(mMenuItem==null) return;
		mMenuItem.setGroupIndex( g );

	}
	public int getGroup() {
		if(mMenuItem==null) return 0;
		return mMenuItem.getGroupIndex();
	}

	public void setRadio(boolean b) {
		if(mMenuItem==null) return;
		mMenuItem.setRadioItem( b );
	}
	public boolean getRadio() {
		if(mMenuItem==null) return false;
		return mMenuItem.getRadioItem();
	}

	public void setShortcut( final String shortcut) {
		if(mMenuItem==null) return;
		mMenuItem.setShortcut( shortcut );
	}
	public String getShortcut() {
		if(mMenuItem==null) return null;
		return mMenuItem.getShortcut();
	}

	public void setVisible(boolean b) throws TJSException {
		if(mMenuItem==null) return;
		if(mWindow!=null) mWindow.setMenuBarVisible(b);
		else mMenuItem.setVisible( b );
	}
	public boolean getVisible() {
		if(mMenuItem==null) return false;
		if(mWindow!=null) return mWindow.getMenuBarVisible();
		else return mMenuItem.getVisible();
	}

	public int getIndex() {
		if(mMenuItem!=null) return -1;
		return mMenuItem.getMenuIndex();
	}
	public void setIndex( int newIndex) throws TJSException {
		if(mMenuItem!=null) return;
		mMenuItem.setMenuIndex( newIndex );
	}

	public int trackPopup( int flags, int x, int y) {
		if(mMenuItem==null) return 0;

		int result = 0;
		if( getRootMenuItem() != null && getRootMenuItem().getWindow() != null ) {
			result = mMenuItem.popup(getRootMenuItem().getWindow().getForm(), flags, x, y);
		}
		return result;
	}
}
