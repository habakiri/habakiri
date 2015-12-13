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

import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.AttributedCharacterIterator;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

import jp.kirikiri.tjs2.BinaryStream;
import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tvp2.utils.DebugClass;
import jp.kirikiri.tvp2.visual.PrerenderedFont;
import jp.kirikiri.tvp2.visual.Size;

public class Font {
	/**
	 *
	 */
	private static final long serialVersionUID = 4868231464013156104L;

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
	static private BufferedImage mTarget;

	private java.awt.Font mFont;
	private int mAngle;

	public Font(Map<? extends AttributedCharacterIterator.Attribute,?> attributes) {
		mFont = new java.awt.Font(attributes);
	}
	public Font(String name, int style, int size) {
		mFont = new java.awt.Font(name,style,size);
	}
	public Font( java.awt.Font ref ) {
		mFont = ref;
	}

	public Font(Font org) {
		mFont = new java.awt.Font( org.getFont().getAttributes() );
	}

	public Font(BinaryStream stream,Font ref,String facename) throws TJSException {
		try {
			java.awt.Font font = java.awt.Font.createFont( java.awt.Font.TRUETYPE_FONT, stream.getInputStream() );
			// 読み込んだフォントを登録する
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			ge.registerFont(font);
			java.awt.Font reffont = ref.mFont;
			mFont = font.deriveFont( reffont.getStyle(), reffont.getSize() );
		} catch (FontFormatException e) {
			throw new TJSException("Font file load error.");
		} catch (IOException e) {
			throw new TJSException("Font file load error.");
		}
	}

	/**
	 * デフォルトフォントとして利用しようとするフォント
	 */
	private static final String defaultFontNames[] = {
		"ＭＳ ゴシック",
		"IPAゴシック",
		"VL ゴシック",
		"さざなみゴシック",
		"ヒラギノ角ゴ",
		"ヒラギノ丸ゴ",

		"ＭＳ Ｐゴシック",
		"メイリオ",
		"IPA Pゴシック",
		"VL Pゴシック",

		"ＭＳ 明朝",
		"さざなみ明朝",

		"IPA明朝",
		"IPA P明朝",
	};

	static public void constructDefaultFont() {
		if( DefaultFont != null ) return;

		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		String[] fonts = ge.getAvailableFontFamilyNames(Locale.JAPAN); // 日本語を前提としている
		final int count = fonts.length;
		String fontName = null;
		final int defCount = defaultFontNames.length;
		for( int f = 0; f < defCount; f++ ) {
			for( int i = 0; i < count; i++ ) {
				if( defaultFontNames[f].equals( fonts[i] ) ) {
					fontName = defaultFontNames[f];
					break;
				}
			}
			if( fontName != null ) break;
		}
		if( fontName == null ) { // デフォルトのフォントが見付からない場合
			fontName = java.awt.Font.MONOSPACED; // 論理フォントの固定幅を選ぶ
			java.awt.Font tmp = new java.awt.Font( fontName, java.awt.Font.PLAIN, 12 );
			fontName = tmp.getFontName(Locale.JAPAN);
			if( fontName == null ) {
				fontName = java.awt.Font.MONOSPACED;
			}
		}
		DebugClass.addLog("(info) Default Font Name : " + fontName);
		DefaultFont = new Font( fontName, java.awt.Font.PLAIN, 12 );
	}
	static public Font getDefaultFont() {
		if( DefaultFont == null ) constructDefaultFont();
		return DefaultFont;
	}
    public java.awt.Font getFont() { return mFont; }
    public void setFont( Font font ) {
		mFont = new java.awt.Font( font.getFont().getAttributes() );
		mAngle = font.mAngle;
    }

	public String getFaceName() { return mFont.getFontName(); }
	public void setFaceName( final String name ) {
		@SuppressWarnings("unchecked")
		Map<TextAttribute,Object> attr = (Map<TextAttribute, Object>) mFont.getAttributes();
		java.awt.Font old = mFont;
		attr.put(TextAttribute.FAMILY, name );
		mFont = java.awt.Font.getFont(attr);
		if( mFont == null || mFont.getFontName().equals(name) == false ) {
			// フォントが見付からなかったようなので、前のものから変更しない
			mFont = old;
		}
	}
	public int getHeight() {
		//if( mTarget == null ) mTarget = new BufferedImage( 32, 32, BufferedImage.TYPE_4BYTE_ABGR );
		//Graphics2D g = mTarget.createGraphics();
		//FontMetrics metrics = g.getFontMetrics(mFont);
		//return metrics.getHeight();
		//return metrics.getMaxAscent() + metrics.getMaxDescent();
		return mFont.getSize();
	}
	public void setHeight(int height) {
		@SuppressWarnings("unchecked")
		Map<TextAttribute,Object> attr = (Map<TextAttribute, Object>) mFont.getAttributes();
		/* TODO 以下で計算せずに直接入れてしまっても問題ない？
		int curHeight = getHeight();
		float point = height * 72 / 96;	// 96dpi Windows の場合
		Object val = attr.get(TextAttribute.SIZE);
		if( val instanceof Float ) {
			Float size = (Float)val;
			float s = size.floatValue();
			float dpi = curHeight * 72f / s;
			point = height * 72f / dpi;
		}
		attr.put(TextAttribute.SIZE, Float.valueOf(point) );
		*/
		attr.put(TextAttribute.SIZE, Float.valueOf(height) );
		mFont = java.awt.Font.getFont(attr);
	}
	public int getAngle() {
		/*
		float state = mFont.getItalicAngle();
		if( state == 0.0f ) return 0;
		return (int) (Math.atan(state)*180/Math.PI);
		*/
		return mAngle;
	}
	public void setAngle(int angle) {
		/*
		@SuppressWarnings("unchecked")
		Map<TextAttribute,Object> attr = (Map<TextAttribute, Object>) mFont.getAttributes();
		attr.put(TextAttribute.POSTURE, new Float(Math.tan(Math.PI*angle/180.0)) );
		mFont = java.awt.Font.getFont(attr);
		*/
		mAngle = angle;
	}
	public boolean getBold() {
		return mFont.isBold();
	}
	public void setBold(boolean b) {
		@SuppressWarnings("unchecked")
		Map<TextAttribute,Object> attr = (Map<TextAttribute, Object>) mFont.getAttributes();
		if( b ) {
			attr.put(TextAttribute.WEIGHT , TextAttribute.WEIGHT_BOLD );
		} else {
			attr.put(TextAttribute.WEIGHT , TextAttribute.WEIGHT_REGULAR );
		}
		mFont = java.awt.Font.getFont(attr);
	}
	public boolean getItalic() {
		return mFont.isItalic();
	}
	public void setItalic(boolean b) {
		@SuppressWarnings("unchecked")
		Map<TextAttribute,Object> attr = (Map<TextAttribute, Object>) mFont.getAttributes();
		if( b ) {
			attr.put(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE );
		} else {
			attr.put(TextAttribute.POSTURE, TextAttribute.POSTURE_REGULAR );
		}
		mFont = java.awt.Font.getFont(attr);
	}
	public boolean getStrikeout() {
		Object attr = mFont.getAttributes().get(TextAttribute.STRIKETHROUGH );
		if( attr instanceof Boolean ) {
			Boolean val = (Boolean)attr;
			boolean state = val.booleanValue();
			if( state == TextAttribute.STRIKETHROUGH_ON  )
				return true;
		}
		return false;
	}
	public void setStrikeout(boolean b) {
		@SuppressWarnings("unchecked")
		Map<TextAttribute,Object> attr = (Map<TextAttribute, Object>) mFont.getAttributes();
		if( b ) {
			attr.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON );
		} else {
			attr.remove(TextAttribute.STRIKETHROUGH);
		}
		mFont = java.awt.Font.getFont(attr);
	}
	public boolean getUnderline() {
		Object attr = mFont.getAttributes().get(TextAttribute.INPUT_METHOD_UNDERLINE);
		if( attr instanceof Number ) {
			Number val = (Number)attr;
			int state = val.intValue();
			if( state == TextAttribute.UNDERLINE_ON )
				return true;
		}
		return false;
	}
	public void setUnderline(boolean b) {
		@SuppressWarnings("unchecked")
		Map<TextAttribute,Object> attr = (Map<TextAttribute, Object>) mFont.getAttributes();
		if( b ) {
			attr.put(TextAttribute.INPUT_METHOD_UNDERLINE, TextAttribute.UNDERLINE_ON );
		} else {
			attr.remove(TextAttribute.INPUT_METHOD_UNDERLINE);
		}
		mFont = java.awt.Font.getFont(attr);
	}
	public int getTextWidth(String text) {
		if( mTarget == null ) mTarget = new BufferedImage( 32, 32, BufferedImage.TYPE_4BYTE_ABGR );
		Graphics2D g = mTarget.createGraphics();
		FontRenderContext frc = g.getFontRenderContext();
		Rectangle2D r = mFont.getStringBounds(text, frc);
		g.dispose();
		return (int) r.getWidth();
	}
	public int getTextHeight(String text) {
		if( mTarget == null ) mTarget = new BufferedImage( 32, 32, BufferedImage.TYPE_4BYTE_ABGR );
		Graphics2D g = mTarget.createGraphics();
		FontRenderContext frc = g.getFontRenderContext();
		Rectangle2D r = mFont.getStringBounds(text, frc);
		g.dispose();
		return (int) r.getHeight();
	}
	public Size getTextSize(String text) {
		if( mTarget == null ) mTarget = new BufferedImage( 32, 32, BufferedImage.TYPE_4BYTE_ABGR );
		Graphics2D g = mTarget.createGraphics();
		FontRenderContext frc = g.getFontRenderContext();
		Rectangle2D r = mFont.getStringBounds(text, frc);
		g.dispose();
		return new Size( (int)r.getWidth(), (int)r.getHeight() );
	}
	public float getAscent() {
		if( mTarget == null ) mTarget = new BufferedImage( 32, 32, BufferedImage.TYPE_4BYTE_ABGR );
		Graphics2D g = mTarget.createGraphics();
		FontRenderContext frc = g.getFontRenderContext();
		LineMetrics m = mFont.getLineMetrics("M", frc);
		g.dispose();
		return m.getAscent();
	}
	/**
	 * システムにあるフォントを取得する
	 * @param flags フラグ ( 無視というか、判別手段がないような )
	 * @param list 取得したフォント名のリスト
	 */
	public static void getFontList(int flags, ArrayList<String> list) {
		list.clear();
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		java.awt.Font[] fonts = ge.getAllFonts();
		final int count = fonts.length;
		for( int i = 0; i < count; i++ ) {
			list.add( fonts[i].getFontName() );
		}
	}
}
