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


import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.Paint.FontMetrics;
import android.util.DisplayMetrics;

public class MenuView {
	static class MenuItemView {
		private String mCaption;
		private int mLeft;
		private int mTop;
		private int mRight;
		private int mBottom;
		private boolean mIsPressing;
		private boolean mEnabled;
		private MenuItem mItem;

		public void clear() {
			mCaption = null;
			mLeft = mTop = mRight = mBottom = 0;
			mIsPressing = mEnabled = false;
			mItem = null;
		}
		public void setRect( int l, int t, int r, int b ) {
			mLeft = l;
			mTop = t;
			mRight = r;
			mBottom = b;
		}
		public void set( String caption, boolean enabled, MenuItem item ) {
			mCaption = caption;
			mEnabled = enabled;
			mItem = item;
		}
		public boolean isDrawing() {
			return mCaption != null;
		}
		public void draw( Canvas canvas, Paint paint, int fontheight, int mergine, int fonttop ) {
			if( mCaption == null ) return;

			int left = mLeft;
			int top = mTop;
			int right = mRight;
			int bottom = mBottom;
			int bg, txt;
			if( mEnabled == false ) { // 無効なアイテム
				bg = BG_DISABLE_COLOR;
				txt = TEXT_DISABLE_COLOR;
			} else if( mIsPressing == true ) {
				bg = BG_PRESSED_COLOR;
				txt = TEXT_PRESSED_COLOR;
			} else {
				bg = BG_COLOR;
				txt = TEXT_COLOR;
			}
			paint.setColor( bg );
			paint.setAlpha(MENU_ALPHA);
			canvas.drawRect(left, top, right, bottom, paint);

			paint.setColor( txt );
			paint.setAlpha(MENU_ALPHA);
			int y = (int) (top + ((bottom-top-1)-fontheight)/2 - fonttop );
			canvas.drawText( mCaption, left+fontheight+mergine, y, paint );

			MenuItem item = mItem;
			if( item != null ) {
				int offset = top + (((bottom - top) - fontheight)>>>1);
				if( item.getChecked() ) {
					if( item.getRadioItem() ) { // ラジオアイテム
						drawCircle( canvas, paint, left+mergine, offset, fontheight, txt, bg );
					} else { // チェックボックス
						drawCheck( canvas, paint, left+mergine, offset, fontheight, txt );
					}
				}
				if( item.isEmpty() != true ) {
					int group = right - mergine - (fontheight>>>1);
					canvas.drawText( ">", group, y, paint );
				}
				item = null;
			}
			paint.setColor( LINE_COLOR );
			paint.setAlpha( MENU_ALPHA );
			canvas.drawLine( left, bottom, right, bottom, paint);
			canvas.drawLine( right, top, right, bottom, paint);
		}
		private void drawCircle( Canvas canvas, Paint paint, int x, int y, float size, int txt, int bg ) {
			final float rate = size / 16.0f;
			final float[] dist = {0.0f, 0.6f, 1.0f};
			final int[] colors = {txt, bg, bg };
			final RadialGradient gradient = new RadialGradient( x+8.0f*rate, y+6.0f*rate, 9.0f*rate, colors, dist, Shader.TileMode.CLAMP );
			paint.setAlpha( MENU_ALPHA );
			paint.setShader(gradient);
			canvas.drawCircle( x+8*rate, y+8*rate, 4*rate, paint );
			paint.setShader(null);
		}
		private void drawCheck( Canvas canvas, Paint paint, int x, int y, float size, int txt ) {
			final float rate = size / 16.0f;
			paint.setColor( txt );
			paint.setAlpha( MENU_ALPHA );
			Path path = mPath;
			path.reset();
			path.moveTo( x+ 4*rate, y+ 6*rate );
			path.lineTo( x+ 7*rate, y+12*rate );
			path.lineTo( x+12*rate, y+ 2*rate );
			path.lineTo( x+ 7*rate, y+ 9*rate );
			path.close();
			canvas.drawPath(path, paint);
		}

		public boolean hitTest( int x, int y ) {
			if( mEnabled ) {
				if( x >= mLeft && x < mRight && y >= mTop && y < mBottom ) {
					return true;
				}
			}
			return false;
		}
		public void clearPressed() {
			mIsPressing = false;
		}
		public void setPressed() {
			mIsPressing = true;
		}
		public int handle() {
			MenuItem item = mItem;
			if( item != null ) {
				if( item.isEmpty() == true ) {
					item.actionPerformed();
				} else if( item.countVisibleItem() > 0 ) {
					// 子アイテムがあるので、そちらに移動する
					return 2;
				}
				return 0;
			} else if( mCaption == PREV_PAGE ) {
				return -1;
			} else if( mCaption == NEXT_PAGE ) {
				return 1;
			}
			return 0;
		}
		public MenuItem getMenuItem() { return mItem; }
	}
 	static final int MENU_ITEM_HEIGHT = 50; // 50dip
	static final int MENU_ITEM_MARGINE = 10;
	static final int MENU_ALPHA = 255;
	static final int LINE_COLOR = 0xFF707070;
	static final int TEXT_COLOR = 0xFFFFFFFF;
	static final int TEXT_DISABLE_COLOR = 0xFF707070;
	static final int TEXT_PRESSED_COLOR = 0xFFFFFFFF;
	static final int BG_COLOR = 0xFF000000;
	static final int BG_DISABLE_COLOR = 0xFF000000;
	static final int BG_PRESSED_COLOR = 0xFF707070;
	static String PREV_PAGE;
	static String NEXT_PAGE;
	static DisplayMetrics mMetrics;
	static Paint mPaint;
	static Rect mBounds;
	static Path mPath;
	public static void initialize( Activity ctx ) {
		//Resources res = ctx.getResources();
		/*
		LINE_COLOR = 0x707070; // res.getColor(R.color.menu_line);
		TEXT_COLOR = 0xFFFFFF; // res.getColor(R.color.menu_text_normal);
		BG_COLOR = 0x000000; // res.getColor(R.color.menu_bg_normal);
		TEXT_DISABLE_COLOR = 0x707070; // res.getColor(R.color.menu_text_disable);
		BG_DISABLE_COLOR = 0x000000; // res.getColor(R.color.menu_bg_disable );
		TEXT_PRESSED_COLOR = 0xFFFFFF; // res.getColor(R.color.menu_text_pressed );
		BG_PRESSED_COLOR = 0x707070; // res.getColor(R.color.menu_bg_pressed );
		*/
		PREV_PAGE = SystemMessage.PREV_PAGE;//res.getString(R.string.menu_prev);
		NEXT_PAGE = SystemMessage.NEXT_PAGE;//res.getString(R.string.menu_next);
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
		mPath = new Path();
	}
	private MenuItemView[] mItems;

	public void clear() {
		if( mItems != null ) {
			final int count = mItems.length;
			for( int i = 0; i < count; i++ ) {
				if( mItems[i] != null ) mItems[i].clear();
			}
		}
	}
	private void cleateItems( int count ) {
		if( mItems == null || mItems.length != count ) {
			mItems = new MenuItemView[count];
			for( int i = 0; i < count; i++ ) {
				mItems[i] = new MenuItemView();
			}
		}
	}
	public void layout( MenuItem parent, int width, int height, int page ) {
		clear();

		DisplayMetrics metrics = mMetrics;
		int linepix = (int) (MENU_ITEM_HEIGHT * metrics.scaledDensity);
		int lines = height / linepix;
		int linecount = lines - 1;
		int lineheight = (height - linecount) / lines;

		Paint paint = mPaint;
		paint.setTextSize(lineheight/2);
		FontMetrics fontMetrics = paint.getFontMetrics();
		int fontheight = (int) (fontMetrics.bottom - fontMetrics.top);
		parent.updateTextWidthChildren( paint, mBounds );
		int mergine = (int) (MENU_ITEM_MARGINE * metrics.scaledDensity);
		int maxw = mergine + fontheight + parent.getMaxTextWidthInChildren() + (fontheight>>>1) + mergine;
		int hcount = width / maxw; // 横方向の表示数
		if( hcount < 1 ) hcount = 1;
		cleateItems( lines * hcount );

		int linew = width / hcount;
		int remain = height - (lineheight * lines + linecount); // 誤差
		int top = 0;
		int left = 0;
		int right = linew;
		int h = lineheight;
		int bottom = 0;
		final int textareawidth = linew-fontheight-mergine - (fontheight>>>1) - mergine;
		final int count = parent.getChildrenCount();
		int index = 0;
		int p = 0;
		for( int i = 0; i < count; i++ ) {
			MenuItem m = parent.getChild(i);
			if( m.getVisible() == true && m.isSeparator() == false ) {
				h = lineheight;
				if( remain > 0 ) {
					remain--;
					h++;
				}
				bottom = top + h + 1;
				if( page == p ) {
					if( m.getTextWidth() > textareawidth ) {
						mItems[index].set( m.getShortName( paint, textareawidth, mBounds ), m.getEnabled(), m );
					} else {
						mItems[index].set( m.getCaption(), m.getEnabled(), m );
					}
					mItems[index].setRect(left, top, right, bottom );
					index++;
				}
				top = bottom;
				if( bottom >= height ) {
					if( (right+linew) > width ) { // 現在の画面から溢れる
						// 次のページへ
						if( page == p ) { // このアイテムは、次へとなる
							index--;
							mItems[index].set( NEXT_PAGE, true, null );
							break;
						}
						p++;
						left = 0;
						right = linew;
						top = 0;
						remain = height - (lineheight * lines + linecount);
						h = lineheight;
						if( remain > 0 ) { remain--; h++; }
						bottom = top + h + 1;
						// ここには前へが挿入されるので、1個ずらす
						if( p == page ) { // ここでは前へが入る
							index = 0;
							mItems[index].set( PREV_PAGE, true, null );
							mItems[index].setRect(left, top, right, bottom );
							index++;
						}
						top = bottom;
						h = lineheight;
						if( remain > 0 ) { remain--; h++; }
						bottom = top + h + 1;
					} else { // 下端に到達したら右側へ移動
						left = right;
						right += linew;
						if( (right+linew) > width ) {
							 // 最終行
							right = width;
						}
						top = 0;
						remain = height - (lineheight * lines + linecount);
					}
				}
				if( p > page ) break;
			}
		}
	}
	public void draw( Canvas canvas ) {
		final Paint paint = mPaint;
		final FontMetrics fontMetrics = paint.getFontMetrics();
		final int fontheight = (int) (fontMetrics.bottom - fontMetrics.top);
		final int mergine = (int) (MENU_ITEM_MARGINE * mMetrics.scaledDensity);
		final int count = mItems.length;
		for( int i = 0; i < count; i++ ) {
			mItems[i].draw( canvas, mPaint, fontheight, mergine, (int)fontMetrics.top );
		}
	}
	public int hitTest( int x, int y ) {
		final int count = mItems.length;
		for( int i = 0; i < count; i++ ) {
			boolean hit = mItems[i].hitTest(x, y);
			if( hit ) {
				return i;
			}
		}
		return -1;
	}
	public void setPressed( int index ) {
		final int count = mItems.length;
		for( int i = 0; i < count; i++ ) {
			if( i == index  ) {
				mItems[i].setPressed();
			} else {
				mItems[i].clearPressed();
			}
		}
	}
	public void clearPressed() {
		final int count = mItems.length;
		for( int i = 0; i < count; i++ ) {
			mItems[i].clearPressed();
		}
	}
	/**
	 * index 位置のメニューがクリックされた時の動作
	 * @param index
	 * @return
	 */
	public int handleMenu( int index ) {
		final int count = mItems.length;
		if( index < 0 || index >= count ) return 0; // 何もしない
		return mItems[index].handle();
	}
	public MenuItem getMenuItem( int index ) {
		final int count = mItems.length;
		if( index < 0 || index >= count ) return null; // 何もしない
		return mItems[index].getMenuItem();
	}
}
