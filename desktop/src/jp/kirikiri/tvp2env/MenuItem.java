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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
//import javax.swing.JPopupMenu;

import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tvp2.msg.Message;
import jp.kirikiri.tvp2.visual.MenuItemNI;

public class MenuItem implements ActionListener {

	JMenuBar	mOwnerBar;
	JMenuItem	mItem;
	JSeparator	mSeparator;

	// JPopupMenu	mPopup;

	private MenuItemNI mActionOwner;
	private MenuItem mParent;
	private ArrayList<MenuItem> mChildren;

	private String mCaption;
	private boolean mChecked;
	private boolean mEnabled;
	private int mGroupIndex;
	private boolean mRadioItem;
	private String mShortcut;
	private boolean mVisible;
	private char mMnemonic;

	private static final long serialVersionUID = 7339008711139249274L;
	/** チェックマークアイコン画像 */
	private static Icon ICON_CHECK;
	/** ○マークアイコン画像 */
	private static Icon ICON_CIRCLE;
	/** 透明アイコン画像 */
	private static Icon ICON_TRANS;


	private static void crateItemImage() {
		if( ICON_TRANS != null ) return;

		// 透明アイコンを作る
		BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_4BYTE_ABGR);
		ICON_TRANS = new ImageIcon(image);

		// ○アイコンを作る
		image = new BufferedImage(16, 16, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g = image.createGraphics();
		float[] dist = {0.0f, 0.6f, 1.0f};
		Color[] colors = {Color.WHITE, Color.BLACK, Color.BLACK };
		//RadialGradientPaint gradient = new RadialGradientPaint( 4.0f, 2.0f, 5.0f, dist, colors, CycleMethod.NO_CYCLE );
		RadialGradientPaint gradient = new RadialGradientPaint( 8.0f, 6.0f, 9.0f, dist, colors, CycleMethod.NO_CYCLE );
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g.setPaint(gradient);
		g.fillOval( 4, 4, 8, 8 );
		g.dispose();
		ICON_CIRCLE = new ImageIcon(image);

		// チェックアイコンを作る
		image = new BufferedImage(16, 16, BufferedImage.TYPE_4BYTE_ABGR);
		g = image.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor( Color.BLACK );
		final int[] xPoints = {4,7,12,7,4};
		final int[] yPoints = {6,12,2,9,6};
		g.fillPolygon( xPoints, yPoints, 5 );
		g.dispose();
		ICON_CHECK = new ImageIcon(image);
	}

	public MenuItem( JMenuBar bar ) {
		this();
		mOwnerBar = bar;
	}
	/*
	public MenuItem( MenuItem parent ) {
		this();
		mParent = parent;
	}
	*/
	public MenuItem( MenuItemNI action ) {
		this();
		mActionOwner = action;
	}
	private MenuItem() {
		mEnabled = true;
		mVisible = true;
		mChildren = new ArrayList<MenuItem>();
		crateItemImage();
	}
	private void createMenuItemForChildren() {
		createMenuItem();
		JMenu menu = null;
		if( mItem != null && mItem instanceof JMenu ) {
			menu = (JMenu)mItem;
		}
		if( menu != null ) {
			final int count = mChildren.size();
			for( int i = 0; i < count; i++ ) {
				MenuItem item = mChildren.get(i);
				item.createMenuItemForChildren();
				if( item.mSeparator != null ) menu.add( item.mSeparator );
				else if( item.mItem != null ) menu.add( item.mItem );
			}
		}
	}

	private void createMenuItem() {
		if( mItem != null || mSeparator != null ) return;

		Icon icon;
		if( mChecked ) {
			if( mRadioItem ) {
				icon = ICON_CIRCLE;
			} else {
				icon = ICON_CHECK;
			}
		} else {
			icon = ICON_TRANS;
		}
		if( mParent != null && mParent.mOwnerBar != null ) {
			icon = null;
		}
		if( mChildren.size() > 0 ) {
			mItem = new JMenu(mCaption);
			if( icon != null ) mItem.setIcon(icon);
		} else {
			if( "-".equals( mCaption ) ) {
				mSeparator = new JSeparator();
				return;
			} else {
				if( icon != null ) mItem = new JMenuItem(mCaption,icon);
				else mItem = new JMenuItem(mCaption);
			}
		}
		mItem.addActionListener(this);
		if( mEnabled == false ) {
			mItem.setEnabled(false);
		}
		if( mVisible == false ) {
			mItem.setVisible(false);
		}
		if( mShortcut != null ) {
			setShortcut( mItem, mShortcut.toLowerCase() );
		}
		if( mMnemonic != 0 ) {
			int vk = VirtualKey.getKeyCode(mMnemonic);
			if( vk >= 0 ) {
				mItem.setMnemonic(vk);
			}
		}
	}
	private void captionToMnemonic() {
		mShortcut = captionToShortcut();
		final int count = mCaption.length();
		int index = mCaption.indexOf("&");
		if( index >= 0 && (index+1) <= count ) {
			char c = mCaption.charAt(index+1); // ニーモニック
			if( c >= 'A' && c <= 'Z' ) {
				c = (char) (c + 'a'-'A');
			}
			int vk = VirtualKey.getKeyCode(c);
			if( vk >= 0 ) {
				mMnemonic = c;

				StringBuilder builder = new StringBuilder(count);
				builder.append( mCaption.substring(0,index) );
				builder.append( mCaption.substring(index+1) );
				mCaption = builder.toString();
			}
		}
	}
	private String captionToShortcut() {
		final int count = mCaption.length();
		int index = mCaption.indexOf('\t');
		if( index >= 0 && (index+1) < count ) {
			String ret = mCaption.substring(index+1);
			mCaption = mCaption.substring(0,index); // \t以降は削る
			return ret;
		} else {
			return null;
		}
	}
	private void setShortcut( JMenuItem item, String shortcut ) {
		if( item == null ) return;
		String[] keys = shortcut.split("\\+");
		final int count = keys.length;
		int mode = 0;
		int keyCode = -1;
		for( int i = 0; i < count; i++ ) {
			int code = VirtualKey.getKeyCode( keys[i] );
			if( code >= 0 ) {
				switch( code ) {
				case VirtualKey.VK_SHIFT:
					mode |= InputEvent.SHIFT_DOWN_MASK;
					break;
				case VirtualKey.VK_CONTROL:
					mode |= InputEvent.CTRL_DOWN_MASK;
					break;
				case VirtualKey.VK_ALT:
					mode |= InputEvent.ALT_DOWN_MASK;
					break;
				case VirtualKey.VK_ALT_GRAPH:
					mode |= InputEvent.ALT_GRAPH_DOWN_MASK ;
					break;
				case VirtualKey.VK_META:
					mode |= InputEvent.META_DOWN_MASK;
					break;
				default:
					//if( ( code >= VirtualKey.VK_0 && code <= VirtualKey.VK_9) ||
					//	( code >= VirtualKey.VK_A && code <= VirtualKey.VK_Z) )
					{
						keyCode = code;
					}
				}
			}
		}
		if( keyCode >= 0 ) {
			item.setAccelerator( KeyStroke.getKeyStroke( keyCode, mode) );
		} else {
			item.setAccelerator( null );
		}
	}
	public MenuItem getParent() { return mParent; }

	private void freeNativeAll() {
		final int count = mChildren.size();
		for( int i = 0; i < count; i++ ) {
			mChildren.get(i).freeNativeAll();
		}
		if( mParent != null && (mParent.mItem != null || mParent.mOwnerBar != null) ) {
			if( mParent.mItem != null && mParent.mItem instanceof JMenu ) {
				JMenu owner = (JMenu)mParent.mItem;
				if( mItem != null ) { owner.remove(mItem); }
				if( mSeparator != null ) { owner.remove(mSeparator); }
			} else if( mParent.mOwnerBar != null ) {
				JMenuBar owner = mParent.mOwnerBar;
				if( mItem != null ) { owner.remove(mItem); }
				if( mSeparator != null ) { owner.remove(mSeparator); }
			}
		}
		mItem = null;
		mSeparator = null;
	}

	private void detach( MenuItem item ) {
		int index = indexOf( item );
		if( index >= 0 ) {
			mChildren.remove(index);
		}
		item.freeNativeAll();
		item.mParent = null;
	}

	private void addMenu( MenuItem item, int index ) throws TJSException {
		if( (mItem != null || mOwnerBar != null) && item.mItem == null ) {
			item.createMenuItemForChildren();
		}
		if( mItem != null && (mItem instanceof JMenu) == false ) {
			// 子を持てないので、作り直す
			if( mParent.mItem != null && mParent.mItem instanceof JMenu ) {
				JMenu owner = (JMenu)mParent.mItem;
				if( mItem != null ) { owner.remove(mItem); }
				if( mSeparator != null ) { owner.remove(mSeparator); }
				mItem = null;
				mSeparator = null;
				createMenuItem();
				if( mItem != null ) owner.add( mItem, mParent.indexOf(this) );
			} else if( mParent.mOwnerBar != null ) {
				JMenuBar owner = mParent.mOwnerBar;
				if( mItem != null ) { owner.remove(mItem); }
				if( mSeparator != null ) { owner.remove(mSeparator); }
				mItem = null;
				mSeparator = null;
				createMenuItem();
				if( mItem != null ) owner.add( mItem, mParent.indexOf(this) );
			} else {
				// 親が子を持ってないケースはあり得ない
				Message.throwExceptionMessage(Message.InternalError);
			}
			if( mItem instanceof JMenu ) {
				JMenu menu = (JMenu)mItem;
				if( item.mItem != null ) {
					if( index >= 0 ) menu.add(item.mItem,index);
					else menu.add(item.mItem);
				} else if( item.mSeparator != null ) {
					if( index >= 0 ) menu.add(item.mSeparator,index);
					else menu.add(item.mSeparator);
				}
			} else {
				Message.throwExceptionMessage(Message.InternalError);
			}
		} else if( mItem != null && mItem instanceof JMenu ) {
			// 子を持てる時はそのまま持つ
			JMenu menu = (JMenu) mItem;
			if( item.mItem != null ) {
				if( index >= 0 ) menu.add(item.mItem,index);
				else menu.add(item.mItem);
			} else if( item.mSeparator != null ) {
				if( index >= 0 ) menu.add(item.mSeparator,index);
				else menu.add(item.mSeparator);
			}
		} else if( mOwnerBar != null ) {
			// バーの時はそこに追加する
			if( item.mItem != null ) {
				if( index >= 0 ) mOwnerBar.add( item.mItem, index );
				else mOwnerBar.add( item.mItem );
			} else if( item.mSeparator != null ) {
				if( index >= 0 ) mOwnerBar.add(item.mSeparator,index);
				else mOwnerBar.add(item.mSeparator);
			}
		}
	}

	public void add(MenuItem item) throws TJSException {
		if( item.mParent != null ) {
			item.mParent.detach(item);
		}
		mChildren.add(item);
		item.mParent = this;
		addMenu( item, -1 );
	}
	public int indexOf(MenuItem item) {
		final int count = mChildren.size();
		for( int i = 0; i < count; i++ ) {
			if( item == mChildren.get(i) )
				return i;
		}
		return -1;
	}
	public void insert(int index, MenuItem item) throws TJSException {
		if( item.mParent != null ) {
			item.mParent.detach(this);
		}
		mChildren.add(index,item);
		item.mParent = this;
		addMenu( item, index );
	}
	public void delete(int index) {
		if( index >= 0  && index < mChildren.size()) {
			MenuItem item = mChildren.get(index);
			mChildren.remove(index);
			item.freeNativeAll();
			item.mParent = null;
		}
	}
	public void setCaption(String caption ) {
		mCaption = caption;
		captionToMnemonic();
		if( mItem != null ) mItem.setText(mCaption);
	}
	private void updateRadioCheck( int group ) {
		if( mParent == null ) return;
		final int count = mParent.mChildren.size();
		for( int i = 0; i < count; i++ ) {
			MenuItem item = mParent.mChildren.get(i);
			if( item.mRadioItem && item.mGroupIndex == group ) {
				if( item.mItem != null ) {
					item.setChecked( false );
				}
			}
		}
	}
	public void setChecked(boolean b) {
		if( mItem != null && mChecked != b ) {
			if( mParent == null || mParent.mOwnerBar == null ) {
				if( b ) {
					if( mRadioItem ) {
						updateRadioCheck( mGroupIndex );
						mItem.setIcon(ICON_CIRCLE);
					} else {
						mItem.setIcon(ICON_CHECK);
					}
				} else {
					mItem.setIcon(ICON_TRANS);
				}
			}
		}
		mChecked = b;
	}
	public boolean getChecked() {
		return mChecked;
	}
	public void setEnabled(boolean b) {
		if( mItem != null ) mItem.setEnabled(b);
		mEnabled = b;
	}
	public boolean getEnabled() {
		return mEnabled;
	}
	public void setGroupIndex(int g) {
		if( mItem != null && mParent != null ) setChecked(false);
		mGroupIndex = g;
	}

	public int getGroupIndex() {
		return mGroupIndex;
	}
	public void setRadioItem(boolean b) {
		mRadioItem = b;
		if( mItem != null && mChecked ) setChecked(mChecked);
	}
	public boolean getRadioItem() {
		return mRadioItem;
	}
	public void setShortcut(String shortcut) {
		if( mItem != null ) setShortcut( mItem, shortcut.toLowerCase() );
		mShortcut = shortcut;
	}
	public String getShortcut() {
		return mShortcut;
	}
	public void setVisible(boolean b) {
		if( mItem != null ) mItem.setVisible(b);
		mVisible = b;
	}
	public boolean getVisible() {
		return mVisible;
	}
	public void setMenuIndex(int newIndex) throws TJSException {
		detach(this);
		insert( newIndex, this );
	}
	public int getMenuIndex() {
		if( mParent != null ) {
			int ret = mParent.indexOf(this);
			if( ret < 0 ) return 0;
		}
		return 0;
	}

	//private boolean mPopupMenuResult;
	public int popup( WindowForm form, int flags, int x, int y ) {
		/* ポップアップするとメニューバーとのリンクが切れてしまうので、ポップアップしないように
		mPopupMenuResult = false;
		if( mItem != null && mItem instanceof JMenu ) {
			JMenu menu = (JMenu)mItem;
			JPopupMenu popupmenu = menu.getPopupMenu();
			popupmenu.addPopupMenuListener( new PopupMenuListener() {
				@Override
				public void popupMenuWillBecomeVisible(PopupMenuEvent e) {}
				@Override
				public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}
				@Override
				public void popupMenuCanceled(PopupMenuEvent e) {
					mPopupMenuResult = false;
				}
			} );
			mPopupMenuResult = true;
			popupmenu.show(form, x, y);
		}
		return mPopupMenuResult ? 1 : 0;
		*/
		return 0;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		/* 自動で切り替えはしない
		if( mRadioItem ) {
			if( mChecked != true ) {
				updateRadioCheck( mGroupIndex );
				setChecked( true );
			}
		}
		*/
		if( mActionOwner != null ) {
			mActionOwner.menuItemClick();
		}
	}
}
