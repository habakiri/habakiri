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

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.Paint.FontMetrics;
import android.util.DisplayMetrics;

import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tvp2.visual.MenuItemNI;

/**
 * 表示前に生成するようにする
 * Android の標準メニューの場合は、2階層までなので、拡張してダイアログでメニュー作った方がいいかも
 */
public class MenuItem {
	/*
	private static int LINE_COLOR;
	private static int TEXT_COLOR;
	private static int TEXT_DISABLE_COLOR;
	private static int TEXT_PRESSED_COLOR;
	private static int BG_COLOR;
	private static int BG_DISABLE_COLOR;
	private static int BG_PRESSED_COLOR;
	private static String PREV_PAGE;
	private static String NEXT_PAGE;
	private static DisplayMetrics mMetrics;
	private static Paint mPaint;
	private static Rect mBounds;
	public static void initialize( Activity ctx ) {
		Resources res = ctx.getResources();
		LINE_COLOR = res.getColor(R.color.menu_line);
		TEXT_COLOR = res.getColor(R.color.menu_text_normal);
		BG_COLOR = res.getColor(R.color.menu_bg_normal);
		TEXT_DISABLE_COLOR = res.getColor(R.color.menu_text_disable);
		BG_DISABLE_COLOR = res.getColor(R.color.menu_bg_disable );
		TEXT_PRESSED_COLOR = res.getColor(R.color.menu_text_pressed );
		BG_PRESSED_COLOR = res.getColor(R.color.menu_bg_pressed );
		PREV_PAGE = res.getString(R.string.menu_prev);
		NEXT_PAGE = res.getString(R.string.menu_next);
		mMetrics = new DisplayMetrics();
		ctx.getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
		mPaint = new Paint();
		Paint paint = mPaint;
		paint.setStrokeWidth(1.0f);
		paint.setAntiAlias( true );
		paint.setTextAlign( Paint.Align.LEFT );
		paint.setStyle( Paint.Style.FILL );
		paint.setTypeface( Typeface.MONOSPACE );
		mBounds = new Rect();
	}
	*/

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
	private boolean mIsSeparator;
	private char mMnemonic;

	// private int mGroupID;
	// private int mItemID;

	private int mTextWidth;
	private String mShortName;

	public MenuItem( MenuItemNI action ) {
		this();
		mActionOwner = action;
	}
	public MenuItem() {
		mEnabled = true;
		mVisible = true;
		mChildren = new ArrayList<MenuItem>();
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
				// 前後の () の表示はカットする形に
				int len = 2;
				if( (index+2) < count && mCaption.charAt(index+2) == ')' ) {
					len++;
				}
				if( index > 0 && mCaption.charAt(index-1) == '(' ) {
					index--;
					len++;
				}

				StringBuilder builder = new StringBuilder(count);
				builder.append( mCaption.substring(0,index) );
				builder.append( mCaption.substring(index+len) );
				mCaption = builder.toString();
			}
		}
		mIsSeparator = mCaption.length() == 1 && mCaption.charAt(0) == '-';
		mTextWidth = 0;
		mShortName = null;
	}
	private String captionToShortcut() {
		final int count = mCaption.length();
		int index = mCaption.indexOf('\t');
		if( index >= 0 && (index+1) < count ) {
			String ret = mCaption.substring(index+1);
			mCaption = mCaption.substring(0,index); // \t以降は削る
			mTextWidth = 0;
			mShortName = null;
			return ret;
		} else {
			return null;
		}
	}
	/*
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
	*/
	public MenuItem getParent() { return mParent; }

	public boolean isEmpty() {
		return mChildren.isEmpty();
	}
	public int getChildrenCount() {
		return mChildren.size();
	}
	public MenuItem getChild( int index ) {
		final int count = mChildren.size();
		if( index < count ) {
			return mChildren.get(index);
		} else {
			return null;
		}
	}

	private void freeNativeAll() {
		final int count = mChildren.size();
		for( int i = 0; i < count; i++ ) {
			mChildren.get(i).freeNativeAll();
		}
	}

	private void detach( MenuItem item ) {
		int index = indexOf( item );
		if( index >= 0 ) {
			mChildren.remove(index);
		}
		item.freeNativeAll();
		item.mParent = null;
	}

	public void add(MenuItem item) throws TJSException {
		if( item.mParent != null ) {
			item.mParent.detach(item);
		}
		mChildren.add(item);
		item.mParent = this;
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
		mTextWidth = 0;
		mShortName = null;
	}
	public String getCaption() {
		return mCaption;
	}
	private void updateRadioCheck( int group ) {
		if( mParent == null ) return;
		final int count = mParent.mChildren.size();
		for( int i = 0; i < count; i++ ) {
			MenuItem item = mParent.mChildren.get(i);
			if( item.mRadioItem && item.mGroupIndex == group ) {
				item.setChecked( false );
			}
		}
	}
	public void setChecked(boolean b) {
		if( mChecked != b && b == true ) {
			if( mRadioItem ) {
				updateRadioCheck( mGroupIndex );
			}
		}
		mChecked = b;
	}
	public boolean getChecked() {
		return mChecked;
	}
	public void setEnabled(boolean b) {
		mEnabled = b;
	}
	public boolean getEnabled() {
		return mEnabled;
	}
	public void setGroupIndex(int g) {
		mGroupIndex = g;
	}

	public int getGroupIndex() {
		return mGroupIndex;
	}
	public void setRadioItem(boolean b) {
		mRadioItem = b;
	}
	public boolean getRadioItem() {
		return mRadioItem;
	}
	public void setShortcut(String shortcut) {
		mShortcut = shortcut;
	}
	public String getShortcut() {
		return mShortcut;
	}
	public void setVisible(boolean b) {
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

	public int popup( WindowForm form, int flags, int x, int y ) {
		return 0;
	}

	public void actionPerformed() {
		if( mActionOwner != null ) {
			mActionOwner.menuItemClick();
		}
	}
	public boolean isSeparator() { return mIsSeparator; }
	/**
	 * 表示状態で、セパレーターではない時
	 * @return
	 */
	public int countVisibleItem() {
		int result = 0;
		final int count = mChildren.size();
		for( int i = 0; i < count; i++ ) {
			MenuItem mi = mChildren.get(i);
			if( mi.mVisible == true && mi.mIsSeparator == false ) {
				result++;
			}
		}
		return result;
	}
	private void updateTextWidth( Paint paint, Rect bounds ) {
		if( mCaption != null ) {
			paint.getTextBounds(mCaption, 0, mCaption.length(), bounds );
			mTextWidth = bounds.width();
		} else {
			mTextWidth = 0;
		}
	}
	public void updateTextWidthChildren( Paint paint, Rect bounds ) {
		final int count = mChildren.size();
		for( int i = 0; i < count; i++ ) {
			mChildren.get(i).updateTextWidth(paint,bounds);
		}
	}
	public int getTextWidth() { return mTextWidth; }

	private void clearSizeInfo() {
		mTextWidth = 0;
		mShortName = null;
	}
	/**
	 * 縦横更新された時などに文字情報を更新する
	 */
	public void clearSizeInfoAll() {
		final int count = mChildren.size();
		for( int i = 0; i < count; i++ ) {
			mChildren.get(i).clearSizeInfoAll();
		}
		clearSizeInfo();
	}
	/**
	 * 子の中で最大文字幅のものを返す
	 * @return 最大文字幅
	 */
	public int getMaxTextWidthInChildren() {
		int maxw = 0;
		final int count = mChildren.size();
		for( int i = 0; i < count; i++ ) {
			final int w = mChildren.get(i).mTextWidth;
			if( w > maxw ) maxw = w;
		}
		return maxw;
	}
	public String getShortName( Paint paint, int maxw, Rect bounds ) {
		if( mShortName != null ) return mShortName;

		StringBuilder builder = new StringBuilder();
		int len = mCaption.length();
		for( int i = len; i > 0; i-- ) {
			builder.delete(0, builder.length() );
			builder.append( mCaption.substring(0,i) );
			builder.append( "…" );
			mShortName = builder.toString();
			paint.getTextBounds(mShortName, 0, mShortName.length(), bounds );
			if( bounds.width() <= maxw ) {
				return mShortName;
			}
		}
		return "";

		/*
		StringBuilder builder = new StringBuilder();
		int len = mCaption.length();
		float[] width = new float[len];
		paint.getTextWidths( mCaption, width );
		float w = 0;
		for( int i = 0; i < len; i++ ) {
			if( (width[i] + w) > maxw ) { // はみ出る
				i -= 2;
				if( i < 0 ) {
					mShortName = "";
					return mShortName;
				}
				builder.append( mCaption.substring(0,i) );
				builder.append( "…" );
				mShortName = builder.toString();
				paint.getTextBounds(mShortName, 0, mShortName.length(), bounds );
				if( bounds.width() <= maxw ) {
					return mShortName;
				}
				i--;
				for( ; i > 0; i-- ) {
					builder.delete(0, builder.length() );
					builder.append( mCaption.substring(0,i) );
					builder.append( "…" );
					mShortName = builder.toString();
					paint.getTextBounds(mShortName, 0, mShortName.length(), bounds );
					if( bounds.width() <= maxw ) {
						return mShortName;
					}
				}
				mShortName = "";
				return mShortName;
			} else {
				w += width[i];
			}
		}
		return mCaption;
		*/
	}

}
