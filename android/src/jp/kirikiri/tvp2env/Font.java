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
import java.util.Locale;
import java.util.Map;

import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.Paint.FontMetrics;

import jp.kirikiri.tjs2.BinaryStream;
import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tvp2.utils.DebugClass;
import jp.kirikiri.tvp2.visual.Size;

public class Font {

	public static final int TF_ITALIC    = 0x01;
	public static final int TF_BOLD      = 0x02;
	public static final int TF_UNDERLINE = 0x04;
	public static final int TF_STRIKEOUT = 0x08;

	/** 固定ピッチフォントのみ */
	public final static int fsfFixedPitch = 1;
	/** 同じキャラクタセットのフォントのみ */
	public final static int fsfSameCharSet = 2;
	/** 縦書き用フォントを表示しない */
	public final static int fsfNoVertical = 4;
	/** TrueType フォントのみ */
	public final static int fsfTrueTypeOnly = 8;
	/** 選択リストボックスをそれぞれのフォントで表示 */
	public final static int fsfUseFontFace = 0x100;

	static private Font DefaultFont;

	private Typeface mFont;
	private int mAngle;
	private String mFaceName;
	private int mSize;
	private int mHeight;
	private Paint mPaint;
	private boolean mStrikeout;
	private boolean mUnderline;

	public static void initialize() {
		DefaultFont = null;
	}
	public static void finalizeApplication() {
		DefaultFont = null;
	}
	public Font( String name, int style, int size ) {
		mFont = Typeface.create( name, style );
		mFaceName = name;
		mSize = size;
	}


	public Font( Typeface ref, String name, int size ) {
		mFont = ref;
		mFaceName = name;
		mSize = size;
	}

	public Font(Font org) {
		mFont = org.mFont;
		mFaceName = org.mFaceName;
		mSize = org.mSize;
		mStrikeout = org.mStrikeout;
		mUnderline = org.mUnderline;
	}

	public Font(BinaryStream stream, Font font, String facename) throws TJSException {
		// TODO 自動生成されたコンストラクター・スタブ
		//static Typeface.createFromAsset(AssetManager mgr, String path)
		//static Typeface.createFromFile(String path)
		// いったん書き出すか、AssetManager に入れないと無理かな
		if( stream.isArchive() ) {
			// 書き出さないと無理
		} else {
			String path = stream.getFilePath();
			if( path.startsWith("asset") ) {

			} else {

			}
		}
		throw new TJSException("Not supported yet.");
	}


	static public void constructDefaultFont() {
		if( DefaultFont != null ) return;

		DefaultFont = new Font( Typeface.MONOSPACE, "MONOSPACE", 24 );
		DebugClass.addLog("(info) Default Font Name : MONOSPACE");
	}
	static public Font getDefaultFont() {
		if( DefaultFont == null ) constructDefaultFont();
		return DefaultFont;
	}
    public Typeface getFont() { return mFont; }

	public String getFaceName() { return mFaceName; }
	public void setFaceName( final String name ) {
		if( mFaceName.equals(name) == false ) {
			int style = mFont.getStyle();
			Typeface old = mFont;
			mFont = Typeface.create( name, style );
			if( mFont == null ) {
				mFont = old;
			} else {
				if( mPaint != null ) {
					mPaint.setTypeface(mFont);
					FontMetrics fontMetrics = mPaint.getFontMetrics();
					mHeight = (int) (fontMetrics.bottom - fontMetrics.top);
				}
			}
		}
	}
	public int getHeight() {
		return mSize;
	}
	public void setHeight(int height) {
		if( mSize != height ) {
			mSize = height;
			if( mPaint != null ) {
				mPaint.setTextSize( mSize );
				FontMetrics fontMetrics = mPaint.getFontMetrics();
				mHeight = (int) (fontMetrics.bottom - fontMetrics.top);
			}
		}
	}
	public int getAngle() {
		return mAngle;
	}
	public void setAngle(int angle) {
		mAngle = angle;
	}
	public boolean getBold() {
		return mFont.isBold();
	}
	public void setBold(boolean b) {
		if( mFont.isBold() != b ) {
			int style = mFont.getStyle();
			if( b ) {
				style |= Typeface.BOLD;
			} else {
				style &= ~Typeface.BOLD;
			}
			mFont = Typeface.create( mFont, style );
			if( mPaint != null ) {
				mPaint.setTypeface(mFont);
				FontMetrics fontMetrics = mPaint.getFontMetrics();
				mHeight = (int) (fontMetrics.bottom - fontMetrics.top);
			}
		}
	}
	public boolean getItalic() {
		return mFont.isItalic();
	}
	public void setItalic(boolean b) {
		if( mFont.isItalic() != b ) {
			int style = mFont.getStyle();
			if( b ) {
				style |= Typeface.ITALIC;
			} else {
				style &= ~Typeface.ITALIC;
			}
			mFont = Typeface.create( mFont, style );
			if( mPaint != null ) {
				mPaint.setTypeface(mFont);
				FontMetrics fontMetrics = mPaint.getFontMetrics();
				mHeight = (int) (fontMetrics.bottom - fontMetrics.top);
			}
		}
	}

	public boolean getStrikeout() { return mStrikeout; }
	public void setStrikeout(boolean b) {
		mStrikeout = b;
	}
	public boolean getUnderline() { return mUnderline; }
	public void setUnderline(boolean b) { mUnderline = b; }

	public int getTextWidth(String text) {
		if( mPaint == null ) {
			mPaint = new Paint();
			mPaint.setAntiAlias( true );
			mPaint.setTextAlign( Paint.Align.LEFT );
			mPaint.setStyle( Paint.Style.FILL );
			mPaint.setTextSize( mSize );
			mPaint.setTypeface(mFont);
			FontMetrics fontMetrics = mPaint.getFontMetrics();
			mHeight = (int) (fontMetrics.bottom - fontMetrics.top);
		}
		return (int) mPaint.measureText( text );
	}
	public int getTextHeight(String text) {
		if( mPaint == null ) {
			mPaint = new Paint();
			mPaint.setAntiAlias( true );
			mPaint.setTextAlign( Paint.Align.LEFT );
			mPaint.setStyle( Paint.Style.FILL );
			mPaint.setTextSize( mSize );
			mPaint.setTypeface(mFont);
			FontMetrics fontMetrics = mPaint.getFontMetrics();
			mHeight = (int) (fontMetrics.bottom - fontMetrics.top);
		}
		//Rect bounds = new Rect();
		//mPaint.getTextBounds(text, 0, text.length(), bounds);
		//return bounds.height();
		return mHeight;
	}
	public Size getTextSize(String text) {
		if( mPaint == null ) {
			mPaint = new Paint();
			mPaint.setAntiAlias( true );
			mPaint.setTextAlign( Paint.Align.LEFT );
			mPaint.setStyle( Paint.Style.FILL );
			mPaint.setTextSize( mSize );
			mPaint.setTypeface(mFont);
			FontMetrics fontMetrics = mPaint.getFontMetrics();
			mHeight = (int) (fontMetrics.bottom - fontMetrics.top);
			//DebugClass.addLog("font accent: "+fontMetrics.ascent+", descent: "+fontMetrics.descent+", top: "+fontMetrics.top+", bottom: "+fontMetrics.bottom+", leading: "+fontMetrics.leading );
			//DebugClass.addLog("font: " + mFont + ", size: " + mSize );

		}
		return new Size( (int) mPaint.measureText( text ), mHeight );
	}
	/**
	 * システムにあるフォントを取得する
	 * @param flags フラグ ( 無視というか、判別手段がないような )
	 * @param list 取得したフォント名のリスト
	 */
	public static void getFontList(int flags, ArrayList<String> list) {
		list.clear();
	}


	public float getAscent() {
		if( mPaint == null ) {
			mPaint = new Paint();
			mPaint.setAntiAlias( true );
			mPaint.setTextAlign( Paint.Align.LEFT );
			mPaint.setStyle( Paint.Style.FILL );
			mPaint.setTextSize( mSize );
			mPaint.setTypeface(mFont);
		}
		//FontMetrics fontMetrics = mPaint.getFontMetrics();
		//return fontMetrics.ascent;
		return -mPaint.ascent();
	}


	public void setFont(Font font) {
		mFont = font.mFont;
		mAngle = font.mAngle;
		mFaceName = font.mFaceName;
		mSize = font.mSize;
		mHeight = font.mHeight;
		mStrikeout = font.mStrikeout;
		mUnderline = font.mUnderline;
		if( mPaint == null ) {
			mPaint = new Paint();
		}
		mPaint.setAntiAlias( true );
		mPaint.setTextAlign( Paint.Align.LEFT );
		mPaint.setStyle( Paint.Style.FILL );
		mPaint.setTextSize( mSize );
		mPaint.setTypeface(mFont);
	}
}
