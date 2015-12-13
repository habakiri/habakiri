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
import java.util.HashMap;

import jp.kirikiri.tjs2.BinaryStream;
import jp.kirikiri.tjs2.HashCache;
import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tjs2.TJSScriptError;
import jp.kirikiri.tjs2.TJSScriptException;
import jp.kirikiri.tvp2.TVP;
import jp.kirikiri.tvp2.base.BinaryOutputStream;
import jp.kirikiri.tvp2.base.CompactEventCallbackInterface;
import jp.kirikiri.tvp2.base.Storage;
import jp.kirikiri.tvp2.msg.Message;
import jp.kirikiri.tvp2.utils.DebugClass;
import jp.kirikiri.tvp2.visual.PrerenderedFont.CharacterItem;
import jp.kirikiri.tvp2env.Font;
import jp.kirikiri.tvp2env.NativeImageBuffer;

public class BaseBitmap {
	public final static int BB_COPY_MAIN = 1;
	public final static int BB_COPY_MASK = 2;

	public final static int stNearest = 0; // primal method; nearest neighbor method
	public final static int stFastLinear = 1; // fast linear interpolation (does not have so much precision)
	public final static int stLinear = 2;  // (strict) linear interpolation
	public final static int stCubic = 3;    // cubic interpolation
	public final static int stTypeMask = 0xf; // stretch type mask
	public final static int stFlagMask = 0xf0; // flag mask
	public final static int stRefNoClip = 0x10; // referencing source is not limited by the given rectangle
						// (may allow to see the border pixel to interpolate)

	static class PrerenderedFontMap {
		static final int TF_ITALIC		= 0x01;
		static final int TF_BOLD		= 0x02;
		static final int TF_UNDERLINE	= 0x04;
		static final int TF_STRIKEOUT	= 0x08;
		//Font Font;
		int Height; // height of text
		int Flags;
		int Angle; // rotation angle ( in tenths of degrees ) 0 .. 1800 .. 3600
		String Face; // font name
		PrerenderedFont Object;
		public boolean equals( final Object o ) {
			if( o instanceof Font ) {
				Font f = (Font)o;
				if( Face.equals( f.getFaceName() ) &&
					Height == f.getHeight() &&
					Angle == f.getAngle() ) {
					int flag = 0;
					flag |= f.getBold() ? Font.TF_BOLD : 0;
					flag |= f.getItalic() ? Font.TF_ITALIC : 0;
					flag |= f.getStrikeout() ? Font.TF_STRIKEOUT : 0;
					flag |= f.getUnderline() ? Font.TF_UNDERLINE : 0;
					if( flag == Flags ) {
						return true;
					} else {
						return false;
					}
				} else {
					return false;
				}
			} else if( o instanceof PrerenderedFontMap ) {
				PrerenderedFontMap f = (PrerenderedFontMap)o;
				if( Face.equals(f.Face) && Height == f.Height &&
					Flags == f.Flags && Angle == f.Angle ) {
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		}
		public void setFont( Font f ) {
			Face = f.getFaceName();
			Height = f.getHeight();
			Angle = f.getAngle();
			Flags = 0;
			Flags |= f.getBold() ? Font.TF_BOLD : 0;
			Flags |= f.getItalic() ? Font.TF_ITALIC : 0;
			Flags |= f.getStrikeout() ? Font.TF_STRIKEOUT : 0;
			Flags |= f.getUnderline() ? Font.TF_UNDERLINE : 0;
		}
		public void clear() {
			Face = null;
			Height = 0;
			Angle = 0;
			Flags = 0;
			Object = null;
		}
	}
	static public boolean USE_NATIVE_TEXT_DRAW = false;
	private static HashMap<String,PrerenderedFont> PrerenderedFonts;
	private static ArrayList<PrerenderedFontMap> PrerenderedFontMapVector;
	private static int GlobalFontStateMagic;
	private static final int CH_MAX_CACHE_COUNT_LOW = 100;
	private static HashCache<FontAndCharacterData,CharacterData> FontCache;
	static private boolean ClearFontCacheCallbackInit = false;
	public static void initialize() {
		PrerenderedFonts = new HashMap<String,PrerenderedFont>();
		PrerenderedFontMapVector = new ArrayList<PrerenderedFontMap>();
		FontCache = new HashCache<FontAndCharacterData,CharacterData>(CH_MAX_CACHE_COUNT_LOW);
		GlobalFontStateMagic = 0;
		ClearFontCacheCallbackInit = false;
		String type = TVP.Properties.getProperty("text_draw_method", "engine");
		if( "engine".equals(type) ) {
			USE_NATIVE_TEXT_DRAW = false;
		} else {
			USE_NATIVE_TEXT_DRAW = true;
		}
	}
	public static void finalizeApplication() {
		FontCache = null;
		PrerenderedFonts = null;
		PrerenderedFontMapVector = null;
	}
	static void clearFontCache() {
		FontCache.clear();
	}

	private NativeImageBuffer mBitmap;
	private boolean mFontChanged;

	private Font mFont;
	private PrerenderedFont mPrerenderedFont; // プリレンダリング済みフォント

	private int mGlobalFontState;
	// v--- these can be recreated in ApplyFont if FontChanged flag is set
	private int mAscentOfsX;
	private int mAscentOfsY;
	private double mRadianAngle;
	/*
	private int mFontHash;
	*/
	private int mTextWidth;
	private int mTextHeight;
	private String mCachedText;

	public BaseBitmap(int w, int h, int bpp) {
		// TVPFontDCAddRef();

		mFont = new Font( Font.getDefaultFont() );
		// mPrerenderedFont = null;
		mFontChanged = true;
		mGlobalFontState = -1;

		//mTextWidth = mTextHeight = 0;

		mBitmap = new NativeImageBuffer(w,h,bpp);
	}

	public BaseBitmap(BaseBitmap r) {

		mFont = new Font( Font.getDefaultFont() );
		// mPrerenderedFont = null;
		// mLogFont = TVPDefaultLOGFONT;
		mFontChanged = true;
		mGlobalFontState = -1;

		//mTextWidth = mTextHeight = 0;

		mBitmap = r.mBitmap;
		mBitmap.addRef();
	}

	@Override
	protected final void finalize() {
		if( mBitmap != null ) {
			mBitmap.finalizeRelease();
			mBitmap = null;
		}
		try {
			super.finalize();
		} catch (Throwable e) {
		}
	}

	public final int getWidth() { return mBitmap.getWidth(); }
	private final void setWidth( int w ) throws TJSException { setSize( w, getHeight() ); }

	public final int getHeight() { return mBitmap.getHeight(); }
	private final void setHeight( int h ) throws TJSException { setSize( getWidth(), h ); }

	public final void setSize(int w, int h ) throws TJSException { setSize( w, h, true ); }
	public final void setSize(int w, int h, boolean keepimage ) throws TJSException {
		if (mBitmap.getWidth() != w || mBitmap.getHeight() != h ) {
			NativeImageBuffer newbitmap;
			if( keepimage ) {
				/*
				int lh = h < mBitmap.getHeight() ? h : mBitmap.getHeight();
				int lw = w < mBitmap.getWidth() ? w : mBitmap.getWidth();
				newbitmap.copyRect(0, 0, mBitmap, new Rect(0,0,lw,lh) );
				*/
				newbitmap = new NativeImageBuffer( w, h, mBitmap );
			} else {
				newbitmap = new NativeImageBuffer( w, h, mBitmap.getBPP() );
			}
			mBitmap.release();
			mBitmap = null;
			mBitmap = newbitmap;
			mFontChanged = true;
		}
	}
	public final int getByteSize() {
		final int w = mBitmap.getWidth();
		final int h = mBitmap.getHeight();
		final int bpp = mBitmap.getBPP() / 8;
		return w * h * bpp;
	}

	/** bits per pixel を取得する */
	public final int getBPP() { return mBitmap.getBPP(); }
	/** 32bppかどうか確認する */
	public final boolean is32BPP() { return getBPP() == 32; }
	/** 8bppかどうか確認する */
	public final boolean is8BPP() { return getBPP() == 8; }

	/**
	 * 指定 bitmap / font をこのクラスに適用する
	 * @param rhs 適用元画像/フォント保持クラス
	 * @return 適用したかどうか
	 */
	public boolean assign( final BaseBitmap rhs) {
		if( this == rhs || mBitmap == rhs.mBitmap ) return false;

		mBitmap.release();
		mBitmap = null;
		mBitmap = rhs.mBitmap;
		mBitmap.addRef();

		mFont = new Font( rhs.mFont );
		mFontChanged = true;
		return true;
	}
	/**
	 * 画像のみをこのクラスに適用する
	 * @param rhs 適用元画像保持クラス
	 * @return 適用したかどうか
	 */
	public boolean assignBitmap( final BaseBitmap rhs) {
		if( this == rhs || mBitmap == rhs.mBitmap ) return false;

		mBitmap.release();
		mBitmap = null;
		mBitmap = rhs.mBitmap;
		mBitmap.addRef();
		mFontChanged = true;
		return true;
	}

	public boolean setNativeBitmap( final NativeImageBuffer rhs ) {
		if( mBitmap == rhs ) return false;

		mBitmap.release();
		mBitmap = null;
		mBitmap = rhs;
		mFontChanged = true;
		return true;
	}

	/** スキャンラインの取得(現状正しく機能しない) */
	//public void getScanLine( int l, int[] buff) { mBitmap.getScanLine(l,buff); }
	/** スキャンラインの取得(現状正しく機能しない) */
	//public int[] getScanLine( int l ) { return mBitmap.getScanLine(l); }
	/** 書き込み可能スキャンラインの取得(現状正しく機能しない) */
	//public void getScanLineForWrite( int l, int[] buff) { independ(); mBitmap.getScanLine(l, buff); }
	/** ピッチを取得する(現状幅を返すのみ) */
	public int getPitchBytes() { return getWidth(); }

	/** 他と画像共有している時は、分離し独立する。共有元の画像もコピーする */
	public void independ() {
		if( mBitmap.isIndependent() ) return;

		NativeImageBuffer newb = new NativeImageBuffer(mBitmap);
		mBitmap.release();
		mBitmap = null;
		mBitmap = newb;
		mFontChanged = true; // informs internal font information is invalidated
	}

	/** 他と画像共有している時は、分離し独立する。共有元の画像はコピーしない */
	public void independNoCopy() {
		if(mBitmap.isIndependent()) return;
		recreate();
	}

	/** 画像を再生成する */
	private void recreate() {
		recreate( mBitmap.getWidth(), mBitmap.getHeight(), mBitmap.getBPP() );
	}

	/** 画像を再生成する */
	private void recreate(int width, int height, int bpp) {
		mBitmap.release();
		mBitmap = null;
		mBitmap = new NativeImageBuffer(width, height, bpp);
		mFontChanged = true; // informs internal font information is invalidated
	}

	private void applyFont() {
		// apply font
		if( mFontChanged || mGlobalFontState != GlobalFontStateMagic ) {
			independ();
			mFontChanged = false;
			mGlobalFontState = GlobalFontStateMagic;
			mCachedText = null;
			mTextWidth = mTextHeight = 0;

			if( mPrerenderedFont != null ) mPrerenderedFont = null;
			mPrerenderedFont = getPrerenderedMappedFont(mFont);

			//mFont = new Font(mFont);

			float ascent = mFont.getAscent();
			mRadianAngle = mFont.getAngle() * (Math.PI/1800);
			double angle90 = mRadianAngle + Math.PI/2;
			mAscentOfsX = (int) (-Math.cos(angle90) * ascent);
			mAscentOfsY = (int) (Math.sin(angle90) * ascent);
		} else {
			// TVPFontDCApplyFont(this, false);
		}
	}
	/**
	 * 画像を他と共有していないかどうか確認する
	 * @return true : 共有していない, false : 共有している
	 */
	public boolean isIndependent() { return mBitmap.isIndependent(); }

	/** ネイティブ画像クラスを返す */
	public NativeImageBuffer getNativeImageBuffer() { return mBitmap; }
	/** ネイティブ画像クラスを返す */
	public NativeImageBuffer getBitmap() { return mBitmap; }

	/** フォントを指定する */
	public void setFont(Font font) {
		if( mFont != font ) {
			mFont.setFont( font );
		}
		mFontChanged = true;
	}
	/** 設定されているフォントを取得する */
	public Font getFont() { applyFont(); return mFont; }

	/**
	 * フォント選択ダイアログを表示して、フォントを選択する
	 * @param flags
	 * @param caption ダイアログボックスのキャプション ( タイトルバー ) に表示する文字列
	 * @param prompt ダイアログボックス内に表示するメッセージ
	 * @param samplestring ダイアログボックス内の「サンプル」の部分に表示する文字列を指定
	 * @param faceName 初期選択フェイス名
	 * @return ユーザが OK ボタンを選択した場合は true、それ以外の場合は false
	 */
	public boolean selectFont(int flags, String caption, String prompt, String samplestring, String faceName) {
		// フォント選択は未実装 TODO
		applyFont();
		return false;
	}
	/**
	 * フォント一覧を取得する
	 * @param flags フォントの種類制限フラグ
	 * @param list フォント一覧格納先
	 */
	public void getFontList(int flags, ArrayList<String> list) { Font.getFontList( flags, list ); }

	public void loadFont(String storage, String facename) throws TJSException {
		applyFont();
		String name = TVP.StorageMediaManager.normalizeStorageName(storage,null);
		BinaryStream stream = Storage.createStream(name,0);

		independ();
		mFont = new Font(stream,mFont,facename);

		mFontChanged = true;
	}

	public void mapPrerenderedFont(String storage) throws TJSException {
		applyFont();
		// map specified font to specified prerendered font
		String fn = Storage.searchPlacedPath(storage);
		PrerenderedFont font = PrerenderedFonts.get(fn);
		if( font == null ) {
			String name = TVP.StorageMediaManager.normalizeStorageName(storage,null);
			BinaryStream stream = Storage.createStream(name,0);
			font = new PrerenderedFont( stream );
			PrerenderedFonts.put(fn, font);
		}
		final int count = PrerenderedFontMapVector.size();
		int i;
		for( i = 0; i < count; i++ ) {
			PrerenderedFontMap p = PrerenderedFontMapVector.get(i);
			if( p.equals( mFont ) ) {
				p.Object = null;
				p.Object = font;
				break;
			}
		}
		if( i == count ) { // not fount
			PrerenderedFontMap map = new PrerenderedFontMap();
			map.setFont( mFont );
			map.Object = font;
			PrerenderedFontMapVector.add( map );
		}
		GlobalFontStateMagic++;
		clearFontCache();
		mFontChanged = true;
	}

	public void unmapPrerenderedFont() {
		applyFont();
		final int count = PrerenderedFontMapVector.size();
		for( int i = 0; i < count; i++ ) {
			PrerenderedFontMap p = PrerenderedFontMapVector.get(i);
			if( p.equals( mFont ) ) {
				p.Object = null;
				PrerenderedFontMapVector.remove(i);
				GlobalFontStateMagic++;
				clearFontCache();
				break;
			}
		}
		mFontChanged = true;
	}
	public static void unmapPrerenderedAllFont() {
		if( PrerenderedFontMapVector == null ) return;
		final int count = PrerenderedFontMapVector.size();
		for( int i = 0; i < count; i++ ) {
			PrerenderedFontMap p = PrerenderedFontMapVector.get(i);
			p.clear();
		}
		PrerenderedFontMapVector.clear();
		GlobalFontStateMagic++;
	}
	private static PrerenderedFont getPrerenderedMappedFont( final Font font ) {
		final int count = PrerenderedFontMapVector.size();
		for( int i = 0; i < count; i++ ) {
			PrerenderedFontMap p = PrerenderedFontMapVector.get(i);
			if( p.equals(font) ) {
				return p.Object;
			}
		}
		return null;
	}

	/**
	 * 文字列の描画
	 * @param destrect
	 * @param x
	 * @param y
	 * @param text
	 * @param color
	 * @param bltmode
	 * @param opa
	 * @param holdalpha
	 * @param aa
	 * @param shlevel
	 * @param shadowcolor
	 * @param shwidth
	 * @param shofsx
	 * @param shofsy
	 * @param updaterects
	 * @throws TJSException
	 */
	public final void drawText( final Rect destrect, int x, int y, final String text,
			int color, int bltmode, int opa, boolean holdalpha, boolean aa,
			int shlevel, int shadowcolor, int shwidth, int shofsx, int shofsy, ComplexRect updaterects ) throws TJSException {

		final int len = text.length();
		if( len == 0 ) return;
		if( USE_NATIVE_TEXT_DRAW && mPrerenderedFont == null ) {
			independ();
			applyFont();
			mBitmap.drawText( mFont, destrect, x, y, text, color, bltmode, opa, holdalpha, aa, shlevel, shadowcolor, shwidth, shofsx, shofsy, updaterects);
		} else {
			if( len >= 2 ) {
				drawTextMultiple(
						destrect, x, y, text,
						color, bltmode, opa,
						holdalpha, aa, shlevel,
						shadowcolor, shwidth, shofsx, shofsy,
						updaterects);
			} else {
				drawTextSingle(
						destrect, x, y, text,
						color, bltmode, opa,
						holdalpha, aa, shlevel,
						shadowcolor, shwidth, shofsx, shofsy,
						updaterects);
			}
		}
	}
	static class DrawTextData {
		Rect rect;
		//int bmppitch;
		int opa;
		boolean holdalpha;
		int bltmode;
		public DrawTextData() {
			rect = new Rect();
		}
	}
	private DrawTextData mDrawTextData;

	private void drawTextSingle(Rect destrect, int x, int y, String text,
			int color, int bltmode, int opa, boolean holdalpha, boolean aa,
			int shlevel, int shadowcolor, int shwidth, int shofsx, int shofsy,
			ComplexRect updaterects) throws TJSException {

		// text drawing function for single character
		if( !is32BPP() ) Message.throwExceptionMessage(Message.InvalidOperationFor8BPP);

		if( bltmode == LayerNI.bmAlphaOnAlpha ) {
			if(opa < -255) opa = -255;
			if(opa > 255) opa = 255;
		} else {
			if(opa < 0) opa = 0;
			if(opa > 255 ) opa = 255;
		}
		if(opa == 0) return; // nothing to do
		independ();
		applyFont();
		if( mDrawTextData == null ) {
			mDrawTextData = new DrawTextData();
		}
		mDrawTextData.rect.set(destrect);
		mDrawTextData.bltmode = bltmode;
		mDrawTextData.opa = opa;
		mDrawTextData.holdalpha = holdalpha;

		FontAndCharacterData font = new FontAndCharacterData();
		font.Font.setFont(mFont);
		font.Antialiased = aa;
		font.BlurLevel = shlevel;
		font.BlurWidth = shwidth;
		font.Character = text.charAt(0);
		font.Blured = false;
		CharacterData shadow = null;
		CharacterData data = getCharacter(font, mPrerenderedFont, mAscentOfsX, mAscentOfsY );
		if( shlevel != 0 ) {
			if( shlevel == 255 && shwidth == 0 ) {
				shadow = data;
			} else {
				font = new FontAndCharacterData(font);
				font.Blured = true;
				shadow = getCharacter( font, mPrerenderedFont, mAscentOfsX, mAscentOfsY );
			}
		}
		if( data != null ) {
			if( data.mBlackBoxX != 0 && data.mBlackBoxY != 0 ) {
				Rect drect = new Rect();
				Rect shadowdrect = new Rect();
				boolean shadowdrawn = false;
				if( shadow != null ) {
					shadowdrawn = internalDrawText(shadow, x + shofsx, y + shofsy,
						shadowcolor, mDrawTextData, shadowdrect);
				}

				boolean drawn = internalDrawText(data, x, y, color, mDrawTextData, drect);
				if( updaterects != null ) {
					if(!shadowdrawn) {
						if(drawn) updaterects.or(drect);
					} else {
						if(drawn) {
							Rect d = new Rect();
							Rect.unionRect( d, drect, shadowdrect);
							updaterects.or(d);
						} else {
							updaterects.or(shadowdrect);
						}
					}
				}
			}
		}
	}
	static class CharacterDrawData {
		CharacterData mData; // main character data
		CharacterData mShadow; // shadow character data
		int mX, mY;
		Rect mShadowRect;
		boolean mShadowDrawn;

		public CharacterDrawData( CharacterData data, CharacterData shadow, int x, int y ) {
			mData = data;
			mShadow = shadow;
			mX = x;
			mY = y;
			mShadowRect = new Rect();
			mShadowDrawn = false;
		}

		public CharacterDrawData( final CharacterDrawData rhs ) {
			mData = rhs.mData;
			mShadow = rhs.mShadow;
			mX = rhs.mX;
			mY = rhs.mY;
			mShadowRect = new Rect( rhs.mShadowRect );
			mShadowDrawn = rhs.mShadowDrawn;
		}
		public void set( final CharacterDrawData rhs ) {
			mData = rhs.mData;
			mShadow = rhs.mShadow;
			mX = rhs.mX;
			mY = rhs.mY;
			mShadowRect.set( rhs.mShadowRect );
			mShadowDrawn = rhs.mShadowDrawn;
		}
	}
	private void drawTextMultiple(Rect destrect, int x, int y, String text,
			int color, int bltmode, int opa, boolean holdalpha, boolean aa,
			int shlevel, int shadowcolor, int shwidth, int shofsx, int shofsy,
			ComplexRect updaterects) throws TJSException {
		// text drawing function for multiple characters
		if( !is32BPP() ) Message.throwExceptionMessage(Message.InvalidOperationFor8BPP);

		if( bltmode == LayerNI.bmAlphaOnAlpha ) {
			if(opa < -255) opa = -255;
			if(opa > 255) opa = 255;
		} else {
			if(opa < 0) opa = 0;
			if(opa > 255 ) opa = 255;
		}
		if(opa == 0) return; // nothing to do

		independ();
		applyFont();

		if( mDrawTextData == null ) {
			mDrawTextData = new DrawTextData();
		}
		mDrawTextData.rect.set(destrect);
		mDrawTextData.bltmode = bltmode;
		mDrawTextData.opa = opa;
		mDrawTextData.holdalpha = holdalpha;

		FontAndCharacterData base = new FontAndCharacterData();
		base.Font.setFont(mFont);
		base.Antialiased = aa;
		base.BlurLevel = shlevel;
		base.BlurWidth = shwidth;
		base.Blured = false;

		final int count = text.length();
		ArrayList<CharacterDrawData> drawdata = new ArrayList<CharacterDrawData>(count);

		CharacterData data = null;
		CharacterData shadow = null;
		for( int i = 0; i < count; i++ ) {
			char c = text.charAt(i);
			FontAndCharacterData font = new FontAndCharacterData(base);
			font.Character = c;
			data = getCharacter( font, mPrerenderedFont, mAscentOfsX,mAscentOfsY);
			if( data != null ) {
				if( shlevel != 0 ) {
					if( shlevel == 255 && shwidth == 0 ) {
						// normal shadow
						// shadow is the same as main character data
						shadow = data;
					} else {
						// blured shadow
						font = new FontAndCharacterData(font);
						font.Blured = true;
						shadow = getCharacter( font, mPrerenderedFont, mAscentOfsX, mAscentOfsY );
					}
				}

				if( data.mBlackBoxX != 0 && data.mBlackBoxY != 0 ) {
					// append to array
					drawdata.add( new CharacterDrawData(data, shadow, x, y) );
				}

				// step to the next character position
				x += data.mCellIncX;
				if( data.mCellIncY != 0 ) {
					// Windows 9x returns negative CellIncY.
					// so we must verify whether CellIncY is proper.
					if( mFont.getAngle() < 1800 ) {
						if( data.mCellIncY > 0 ) data.mCellIncY = - data.mCellIncY;
					} else {
						if( data.mCellIncY < 0) data.mCellIncY = - data.mCellIncY;
					}
					y += data.mCellIncY;
				}
			}
		}
		final int dcount = drawdata.size();
		// draw shadows first
		if( shlevel != 0 ) {
			for( int  i = 0; i < dcount; i++) {
				CharacterDrawData d = drawdata.get(i);
				shadow = d.mShadow;
				if( shadow != null ) {
					d.mShadowDrawn = internalDrawText( shadow, d.mX + shofsx, d.mY + shofsy, shadowcolor, mDrawTextData, d.mShadowRect);
				}
			}
		}
		Rect drect = new Rect();
		for( int  i = 0; i < dcount; i++) {
			CharacterDrawData d = drawdata.get(i);
			boolean drawn = internalDrawText(d.mData, d.mX, d.mY, color, mDrawTextData, drect );
			if( updaterects != null ) {
				if(!d.mShadowDrawn) {
					if(drawn) updaterects.or(drect);
				} else {
					if(drawn) {
						Rect ur = new Rect();
						Rect.unionRect( ur, drect, d.mShadowRect );
						updaterects.or(ur);
					} else {
						updaterects.or( d.mShadowRect );
					}
				}
			}
		}
	}
	private boolean internalDrawText( CharacterData data, int x, int y, int color, DrawTextData dtdata, Rect drect ) throws TJSException {
		// setup destination and source rectangle
		drect.left = x + data.mOriginX;
		drect.top = y + data.mOriginY;
		drect.right = drect.left + data.mBlackBoxX;
		drect.bottom = drect.top + data.mBlackBoxY;

		Rect srect = new Rect();
		srect.left = srect.top = 0;
		srect.right = data.mBlackBoxX;
		srect.bottom = data.mBlackBoxY;

		// check boundary
		if( drect.left < dtdata.rect.left ) {
			srect.left += (dtdata.rect.left - drect.left);
			drect.left = dtdata.rect.left;
		}

		if( drect.right > dtdata.rect.right ) {
			srect.right -= (drect.right - dtdata.rect.right);
			drect.right = dtdata.rect.right;
		}

		if( srect.left >= srect.right ) return false; // not drawable

		if( drect.top < dtdata.rect.top ) {
			srect.top += (dtdata.rect.top - drect.top);
			drect.top = dtdata.rect.top;
		}

		if(drect.bottom > dtdata.rect.bottom)
		{
			srect.bottom -= (drect.bottom - dtdata.rect.bottom);
			drect.bottom = dtdata.rect.bottom;
		}

		if(srect.top >= srect.bottom) return false; // not drawable

		// blend to the bitmap
		final int pitch = data.mPitch;
		final byte[] bp = data.getData();

		mBitmap.drawFontImage( bp, pitch, srect, drect, color, dtdata.bltmode, dtdata.opa, dtdata.holdalpha );
		return true;
	}
	private CharacterData getCharacter(FontAndCharacterData font, PrerenderedFont pfont, int aofsx, int aofsy ) {
		if( ClearFontCacheCallbackInit == false ) {
			TVP.EventManager.addCompactEventHook( new CompactEventCallbackInterface() {
				@Override
				public void onCompact(int level) {
					if(level >= CompactEventCallbackInterface.COMPACT_LEVEL_MINIMIZE) {
						// clear the font cache on application minimize
						clearFontCache();
					}
				}
			});
			ClearFontCacheCallbackInit = true;
		}

		CharacterData ptr = FontCache.getAndTouch(font);
		if( ptr != null ) {
			// found in the cache
			return ptr;
		}

		// look prerendered font
		CharacterItem pitem = null;
		if( pfont != null )
			pitem = pfont.getCharData(font.Character);
		if( pitem != null ) {
			// prerendered font
			CharacterData data = new CharacterData();
			data.mBlackBoxX = pitem.Width;
			data.mBlackBoxY = pitem.Height;
			data.mCellIncX = pitem.IncX;
			data.mCellIncY = pitem.IncY;
			data.mOriginX = pitem.OriginX + aofsx;
			data.mOriginY = -pitem.OriginY + aofsy;

			data.mAntialiased = font.Antialiased;

			data.mFullColored = false;

			data.mBlured = font.Blured;
			data.mBlurWidth = font.BlurWidth;
			data.mBlurLevel = font.BlurLevel;

			if(data.mBlackBoxX != 0 && data.mBlackBoxY != 0 ) {
				// render
				int newpitch =  (((pitem.Width -1)>>>2)+1)<<2;
				data.mPitch = newpitch;

				data.alloc(newpitch * data.mBlackBoxY);

				pfont.retrieve( pitem, data.getData(), newpitch);

				// apply blur
				if( font.Blured ) data.blur(); // nasty ...

				// add to hash table
				FontCache.put(font, data);
			}

			return data;
		} else {
			// render font
			// アンチエイリアスは組み込みのもののみサポートする
			CharacterData data = NativeImageBuffer.getCharacterData(mFont, font.Character, font.Antialiased);
			data.mOriginX = data.mOriginX + aofsx;
			data.mOriginY = data.mOriginY + aofsy;

			data.mBlured = font.Blured;
			data.mBlurWidth = font.BlurWidth;
			data.mBlurLevel = font.BlurLevel;

			/*
			if(data.mBlackBoxX != 0 && data.mBlackBoxY != 0 ) {
				byte[] old = data.getData();
				int oldPitch = data.mPitch;
				int height = data.mBlackBoxY;

				// render
				int newpitch =  (((data.mBlackBoxX -1)>>>2)+1)<<2;
				data.mPitch = newpitch;

				data.alloc(newpitch * data.mBlackBoxY);
				byte[] dest = data.getData();
				for( int y = 0; y < height; y++ ) {
					for( int x = 0; x < oldPitch; x++ ) {

					}
				}
				//pfont.retrieve( pitem, data.getData(), newpitch);
			}
			*/

			// apply blur
			if( font.Blured ) data.blur(); // nasty ...

			// add to hash table
			FontCache.put(font, data);

			return data;
		}
	}
	/**
	 * テキストのサイズ取得
	 * @param text 取得したい文字列
	 */
	private void getTextSize( String text ) {
		applyFont();
		if( mCachedText == null || mCachedText.equals(text) != true ) {
			mCachedText = text;
			if( mPrerenderedFont == null ) {
				Size size = mFont.getTextSize(text);
				mTextWidth = size.width;
				mTextHeight = size.height;
			} else {
				int width = 0;
				final int len = text.length();
				for( int i = 0; i < len; i++ ) {
					final char c = text.charAt(i);
					CharacterItem item = mPrerenderedFont.getCharData(c);
					if( item != null ) {
						width += item.Inc;
					} else {
						int w = mFont.getTextWidth(String.valueOf(c));
						width += w;
					}
				}
				mTextWidth = width;
				mTextHeight = Math.abs(mFont.getHeight());
			}
		}
	}
	/**
	 * テキストの幅取得
	 * @param text 取得したい文字列
	 * @return 幅
	 */
	public int getTextWidth(String text) {
		getTextSize(text);
		return mTextWidth;
	}
	/**
	 * テキストの高さ取得
	 * @param text 取得したい文字列
	 * @return 高さ
	 */
	public int getTextHeight(String text) {
		getTextSize(text);
		return mTextHeight;
	}

	/**
	 * 文字の横方向への X 座標の移動量
	 * @param text 取得したい文字列
	 * @return
	 */
	public double getEscWidthX(String text) {
		getTextSize(text);
		return Math.cos(mFont.getAngle()) * mTextWidth;
	}

	/**
	 * 文字の横方向への Y 座標の移動量
	 * @param text
	 * @return
	 */
	public double getEscWidthY(String text) {
		getTextSize(text);
		return Math.sin(mFont.getAngle()) * (-mTextWidth);
	}

	/**
	 * 文字の縦方向への X 座標の移動量
	 * @param text
	 * @return
	 */
	public double getEscHeightX(String text) {
		getTextSize(text);
		return Math.sin(mFont.getAngle()) * mTextHeight;
	}

	/**
	 * 文字の縦方向への Y 座標の移動量
	 * @param text
	 * @return
	 */
	public double getEscHeightY(String text) {
		getTextSize(text);
		return Math.cos(mFont.getAngle()) * mTextHeight;
	}

	public void setSizeWithFill(int w, int h, int fillvalue) throws TJSException {
		//DebugClass.addLog("setSizeWithFill");
		// resize, and fill the expanded region with specified value.
		int orgw = getWidth();
		int orgh = getHeight();

		setSize(w, h);

		if( mBitmap.getBPP() == 32 && mBitmap.isImageAllocated() == false ) {
			Rect rect = new Rect(0,0,w,h);
			fill(rect, fillvalue);
			return;
		}
		if(w > orgw && h > orgh) {
			// both width and height were expanded
			Rect rect = new Rect();
			rect.left = orgw;
			rect.top = 0;
			rect.right = w;
			rect.bottom = h;
			fill(rect, fillvalue);

			rect.left = 0;
			rect.top = orgh;
			rect.right = orgw;
			rect.bottom = h;
			fill(rect, fillvalue);
		} else if(w > orgw) {
			// width was expanded
			Rect rect = new Rect();
			rect.left = orgw;
			rect.top = 0;
			rect.right = w;
			rect.bottom = h;
			fill(rect, fillvalue);
		} else if(h > orgh) {
			// height was expanded
			Rect rect = new Rect();
			rect.left = 0;
			rect.top = orgh;
			rect.right = w;
			rect.bottom = h;
			fill(rect, fillvalue);
		}
	}

	// 指定座標の色を返す
	public final int getPoint(int x, int y) throws TJSException {
		// get specified point's color or color index
		if( x < 0 || y < 0 || x >= getWidth() || y >= getHeight() )
			Message.throwExceptionMessage(Message.OutOfRectangle);
		return mBitmap.getPoint( x, y );
	}
	public final void setPoint(int x, int y, int n ) throws TJSException {
		// get specified point's color or color index
		if( x < 0 || y < 0 || x >= getWidth() || y >= getHeight() )
			Message.throwExceptionMessage(Message.OutOfRectangle);
		independ();
		mBitmap.setPoint( x, y, n );
	}
	/**
	 * fill target rectangle represented as "rect", with color ( and opacity )
	 * passed by "value".
	 * value must be : 0xAARRGGBB (for 32bpp) or 0xII ( for 8bpp )
	 * @throws TJSException
	 */
	public boolean fill(Rect rect, int value) throws TJSException {
		int i;
		if(rect.left < 0) rect.left = 0;
		if(rect.top < 0) rect.top = 0;
		if(rect.right > (i=getWidth())) rect.right = i;
		if(rect.bottom > (i=getHeight())) rect.bottom = i;
		if(rect.right - rect.left <= 0 || rect.bottom - rect.top <= 0)
			return false;

		independ();
		mBitmap.fill( rect, value );
		return true;
	}

	public boolean fillColor(Rect rect, int color, int opa ) throws TJSException {
		int i;
		if(rect.left < 0) rect.left = 0;
		if(rect.top < 0) rect.top = 0;
		if(rect.right > (i=getWidth())) rect.right = i;
		if(rect.bottom > (i=getHeight())) rect.bottom = i;
		if(rect.right - rect.left <= 0 || rect.bottom - rect.top <= 0)
			return false;

		if(!is32BPP()) Message.throwExceptionMessage(Message.InvalidOperationFor8BPP);
		if( opa == 0 ) return false;
		if(opa < 0) opa = 0;
		if(opa > 255) opa = 255;

		independ();
		if( opa == 255 ) {
			// complete opaque fill
			mBitmap.fill( rect, color );
		} else {
			// alpha fill
			mBitmap.fillColor( rect, color, opa );
		}
		return true;
	}

	public boolean fillMask(Rect rect, int opa ) throws TJSException {
		int i;
		if(rect.left < 0) rect.left = 0;
		if(rect.top < 0) rect.top = 0;
		if(rect.right > (i=getWidth())) rect.right = i;
		if(rect.bottom > (i=getHeight())) rect.bottom = i;
		if(rect.right - rect.left <= 0 || rect.bottom - rect.top <= 0)
			return false;

		if(!is32BPP()) Message.throwExceptionMessage(Message.InvalidOperationFor8BPP);

		independ();
		mBitmap.fillMask( rect, opa );
		return true;
	}
	/**
	 * copy bitmap rectangle.
	 * BB_COPY_MAIN in "plane" : main image is copied
	 * BB_COPY_MASK in "plane" : mask image is copied
	 * "plane" is ignored if the bitmap is 8bpp
	 * the source rectangle is ( "refrect" ) and the destination upper-left corner
	 * is (x, y).
	 *
	 * @param x
	 * @param y
	 * @param src
	 * @param refrect
	 * @param plane
	 * @return
	 * @throws TJSException
	 */
	public boolean copyRect(int x, int y, BaseBitmap src, Rect refrect ) throws TJSException {
		return copyRect( x, y, src, refrect, BB_COPY_MASK|BB_COPY_MAIN );
	}
	public boolean copyRect(int x, int y, BaseBitmap ref, Rect refrect, int plane ) throws TJSException {
		if(!is32BPP()) plane = (BB_COPY_MASK|BB_COPY_MAIN);
		if(x == 0 && y == 0 && refrect.left == 0 && refrect.top == 0 &&
			refrect.right == ref.getWidth() &&
			refrect.bottom == ref.getHeight() &&
			getWidth() == refrect.right &&
			getHeight() == refrect.bottom &&
			plane == (BB_COPY_MASK|BB_COPY_MAIN) &&
			!is32BPP() == !ref.is32BPP())
		{
			// entire area of both bitmaps
			assignBitmap(ref);
			return true;
		}

		// bound check
		int bmpw, bmph;

		bmpw = ref.getWidth();
		bmph = ref.getHeight();

		if(refrect.left < 0) {
			x -= refrect.left;
			refrect.left = 0;
		}
		if(refrect.right > bmpw)
			refrect.right = bmpw;

		if(refrect.left >= refrect.right) return false;

		if(refrect.top < 0) {
			y -= refrect.top;
			refrect.top = 0;
		}
		if(refrect.bottom > bmph)
			refrect.bottom = bmph;

		if(refrect.top >= refrect.bottom) return false;

		bmpw = getWidth();
		bmph = getHeight();

		Rect rect = new Rect();
		rect.left = x;
		rect.top = y;
		rect.right = rect.left + refrect.width();
		rect.bottom = rect.top + refrect.height();

		if(rect.left < 0) {
			refrect.left += -rect.left;
			rect.left = 0;
		}

		if(rect.right > bmpw) {
			refrect.right -= (rect.right - bmpw);
			rect.right = bmpw;
		}

		if(refrect.left >= refrect.right) return false; // not drawable

		if(rect.top < 0) {
			refrect.top += -rect.top;
			rect.top = 0;
		}

		if(rect.bottom > bmph) {
			refrect.bottom -= (rect.bottom - bmph);
			rect.bottom = bmph;
		}

		if(refrect.top >= refrect.bottom) return false; // not drawable

		independ();
		return mBitmap.copyRect( rect.left, rect.top, ref.getBitmap(), refrect, plane );
	}

	public void blt(int x, int y, BaseBitmap ref, Rect refrect, int method, int opa) throws TJSException {
		blt(x, y, ref, refrect, method, opa, true);
	}
	public boolean blt(int x, int y, BaseBitmap ref, Rect refrect, int method, int opa, boolean hda) throws TJSException {
		//DebugClass.addLog("blt");
		// blt src bitmap with various methods.

		// hda option ( hold destination alpha ) holds distination alpha,
		// but will select more complex function ( and takes more time ) for it ( if
		// can do )

		// this function does not matter whether source and destination bitmap is
		// overlapped.

		if(opa == 255 && method == LayerNI.bmCopy && !hda) {
			independ();
			return copyRect(x, y, ref, refrect);
		}

		if(!is32BPP()) Message.throwExceptionMessage(Message.InvalidOperationFor8BPP);

		if(opa == 0) return false; // opacity==0 has no action

		// bound check
		int bmpw, bmph;

		bmpw = ref.getWidth();
		bmph = ref.getHeight();

		if(refrect.left < 0) {
			x -= refrect.left;
			refrect.left = 0;
		}
		if(refrect.right > bmpw)
			refrect.right = bmpw;

		if(refrect.left >= refrect.right) return false;

		if(refrect.top < 0) {
			y -= refrect.top;
			refrect.top = 0;
		}
		if(refrect.bottom > bmph)
			refrect.bottom = bmph;

		if(refrect.top >= refrect.bottom) return false;

		bmpw = getWidth();
		bmph = getHeight();


		Rect rect = new Rect();
		rect.left = x;
		rect.top = y;
		rect.right = rect.left + refrect.width();
		rect.bottom = rect.top + refrect.height();

		if(rect.left < 0) {
			refrect.left += -rect.left;
			rect.left = 0;
		}

		if(rect.right > bmpw) {
			refrect.right -= (rect.right - bmpw);
			rect.right = bmpw;
		}

		if(refrect.left >= refrect.right) return false; // not drawable

		if(rect.top < 0) {
			refrect.top += -rect.top;
			rect.top = 0;
		}

		if(rect.bottom > bmph) {
			refrect.bottom -= (rect.bottom - bmph);
			rect.bottom = bmph;
		}

		if(refrect.top >= refrect.bottom) return false; // not drawable

		independ();
		return  mBitmap.copyRect( rect, ref.getBitmap(), refrect, method, opa, hda );
	}

	public void saveAsBMP(String name, String type) throws TJSException {
		String nname = TVP.StorageMediaManager.normalizeStorageName(name,null);
		BinaryStream stream = Storage.createStream(nname,BinaryStream.WRITE);
		if( stream != null ) {
			BinaryOutputStream output = new BinaryOutputStream(stream);
			mBitmap.saveAs( output, name, type );
			output = null;
		}
		stream = null;
	}

	/**
	 * set specified point's color (mask is not touched) for 32bpp
	 * @param x X座標位置
	 * @param y Y座標位置
	 * @param color 設定する色
	 * @throws TJSException
	 */
	public void setPointMain(int x, int y, int color) throws TJSException {
		if(!is32BPP()) Message.throwExceptionMessage(Message.InvalidOperationFor8BPP);

		if(x < 0 || y < 0 || x >= getWidth() || y >= getHeight())
			Message.throwExceptionMessage(Message.OutOfRectangle);
		independ();
		mBitmap.setPointMain( x, y, color);
	}

	/**
	 * set specified point's mask (color is not touched) for 32bpp
	 * @param x
	 * @param y
	 * @param mask
	 * @throws TJSException
	 */
	public void setPointMask(int x, int y, int mask) throws TJSException {
		if(!is32BPP()) Message.throwExceptionMessage(Message.InvalidOperationFor8BPP);

		if(x < 0 || y < 0 || x >= getWidth() || y >= getHeight())
			Message.throwExceptionMessage(Message.OutOfRectangle);
		independ();
		mBitmap.setPointMask( x, y, mask );
	}



	public boolean fillColorOnAlpha(Rect rect, int color, int opa) throws TJSException {
		return blendColor(rect, color, opa, false);
	}


	/**
	 * remove constant opacity from bitmap. ( similar to PhotoShop's eraser tool )
	 * level is a strength of removing ( 0 thru 255 )
	 * this cannot work with additive alpha mode.
	 * @param rect
	 * @param level
	 * @return
	 * @throws TJSException
	 */
	public boolean removeConstOpacity(Rect rect, int level) throws TJSException {
		int i;
		if(rect.left < 0) rect.left = 0;
		if(rect.top < 0) rect.top = 0;
		if(rect.right > (i=getWidth())) rect.right = i;
		if(rect.bottom > (i=getHeight())) rect.bottom = i;
		if(rect.right - rect.left <= 0 || rect.bottom - rect.top <= 0)
			return false;

		if(!is32BPP()) Message.throwExceptionMessage(Message.InvalidOperationFor8BPP);

		if(level == 0) return false; // no action
		if(level < 0) level = 0;
		if(level > 255) level = 255;

		independ();
		mBitmap.removeConstOpacity(rect,level);
		return true;
	}

	public boolean fillColorOnAddAlpha(Rect rect, int color, int opa) throws TJSException {
		return blendColor(rect, color, opa, true);
	}

	private boolean blendColor(Rect rect, int color, int opa, boolean additive) throws TJSException {
		int i;
		if(rect.left < 0) rect.left = 0;
		if(rect.top < 0) rect.top = 0;
		if(rect.right > (i=getWidth())) rect.right = i;
		if(rect.bottom > (i=getHeight())) rect.bottom = i;
		if(rect.right - rect.left <= 0 || rect.bottom - rect.top <= 0)
			return false;

		if(!is32BPP()) Message.throwExceptionMessage(Message.InvalidOperationFor8BPP);

		if(opa == 0) return false; // no action
		if(opa < 0) opa = 0;
		if(opa > 255) opa = 255;

		if(opa == 255 && !isIndependent() ) {
			if(rect.left == 0 && rect.top == 0 && rect.right == getWidth() && rect.bottom == getHeight() ) {
				// cover overall
				independNoCopy(); // indepent with no-copy
			}
		}
		independ();
		mBitmap.blendColor( rect, color, opa, additive );

		return true;
	}

	/**
	 * do stretch blt
	 * stFastLinear is enabled only in following condition:
	 * -------OriginalTODO: write corresponding condition--------
	 *
	 * stLinear and stCubic mode are enabled only in following condition:
	 * any magnification, opa:255, method:bmCopy, hda:false
	 * no reverse, destination rectangle is within the image.
	 * @param cliprect
	 * @param destrect
	 * @param ref
	 * @param refrect
	 * @param method
	 * @param opa
	 * @param hda
	 * @param mode
	 * @return
	 * @throws TJSException
	 */
	public boolean stretchBlt(Rect cliprect, Rect destrect, final BaseBitmap ref, Rect refrect, int method, int opa, boolean hda, int mode) throws TJSException {
		// source and destination check
		int dw = destrect.width(), dh = destrect.height();
		int rw = refrect.width(), rh = refrect.height();

		if( dw == 0 || rw == 0 || dh == 0 || rh == 0 ) return false; // nothing to do

		// quick check for simple blt
		if( dw == rw && dh == rh && destrect.includedIn(cliprect) ) {
			// no stretch; do normal blt
			return blt(destrect.left, destrect.top, ref, refrect, method, opa, hda);
		}

		if(!is32BPP()) Message.throwExceptionMessage(Message.InvalidOperationFor8BPP);

		// check for special case noticed above

		//--- extract stretch type
		int type = (mode & stTypeMask);

		//--- pull the dimension
		int w = getWidth();
		int h = getHeight();

		//--- clop clipping rectangle with the image
		Rect cr = new Rect(cliprect);
		if(cr.left < 0) cr.left = 0;
		if(cr.top < 0) cr.top = 0;
		if(cr.right > w) cr.right = w;
		if(cr.bottom > h) cr.bottom = h;

		independ();
		mBitmap.stretchBlt( cr, destrect, ref.getBitmap(), refrect, type, hda, opa, method );
		return false;
	}

	public boolean affineBlt(Rect destrect, BaseBitmap ref,
			Rect refrect, AffineMatrix2D matrix, int method, int opa,
			Rect updaterect, boolean hda, int type, boolean clear,
			int clearcolor) throws TJSException {

		independ();
		return mBitmap.affineBlt( destrect, ref.getBitmap(), refrect, matrix, method, opa, updaterect, hda, type, clear, clearcolor );
	}

	static final private double EPS = 1.0e-12;	// 計算誤差の許容値
	static final private int MAX_COEFF = 3;
	/**
	 * ガウスの消去法で求める
	 * ガウスザイデル法だと求まらないことも多いようなので、消去法を使う
	 * @param coeff
	 * @param tmp
	 * @param val
	 * @return
	 */
	static private boolean gaussianElimination( double[][] coeff, double[] tmp, double[] val ) {
		int i, j, k, p;
		double pmax, s;

		// 前進消去（ピボット選択）
		for( k = 0; k < MAX_COEFF-1; k++ ) {
			p = k;
			pmax = Math.abs( coeff[k][k] );
			for(i = k+1; i < MAX_COEFF; i++){  // ピボット選択
				if( Math.abs( coeff[i][k] ) > pmax){
					p = i;
					pmax = Math.abs( coeff[i][k] );
				}
			}

			// エラー処理：ピボットがあまりに小さい時は失敗
			if( Math.abs( pmax ) < EPS ) {
				return false;
			}
			if(p != k){  // 第k行と第p行の交換
				for(i = k; i < MAX_COEFF; i++){
					// 係数行列
					s = coeff[k][i];
					coeff[k][i] = coeff[p][i];
					coeff[p][i] = s;
				}
				// 既知ベクトル
				s = val[k];
				val[k] = val[p];
				val[p] = s;
			}
			// 前進消去
			for(i = k +1; i < MAX_COEFF; i++){
				tmp[i] = coeff[i][k] / coeff[k][k];
				coeff[i][k] = 0.0;
				// 第k行を-a[i][k]/a[k][k]倍して、第i行に加える
				for(j = k + 1; j < MAX_COEFF; j++){
					coeff[i][j] = coeff[i][j] - coeff[k][j] * tmp[i];
				}
				val[i] = val[i] - val[k] * tmp[i];
			}
		}
		// 後退代入
		for(i = MAX_COEFF - 1; i >= 0; i--){
			for(j = i + 1; j < MAX_COEFF; j++){
				val[i] = val[i] - coeff[i][j] * val[j];
				coeff[i][j] = 0.0;
			}
			val[i] = val[i] / coeff[i][i];
			coeff[i][i] = 1.0;
		}
		return true;
	}
	/**
	 * 座標によるアフェイン変換の時は、連立方程式を解いてマトリックスを求め、それによってアフェイン変換する
	 * @param destrect
	 * @param ref
	 * @param refrect
	 * @param points_in
	 * @param method
	 * @param opa
	 * @param updaterect
	 * @param hda
	 * @param type
	 * @param clear
	 * @param clearcolor
	 * @return
	 * @throws TJSException
	 */
	public boolean affineBlt(Rect destrect, BaseBitmap ref,
			Rect refrect, PointD[] points_in, int method, int opa, Rect updaterect,
			boolean hda, int type, boolean clear, int clearcolor) throws TJSException {

		/*
		 * 連立三元一次方程式
		 * points_in[0].x = (matrix.a * (-0.5) + matrix.c * (-0.5) + matrix.tx)
		 * points_in[1].x = (matrix.a * (rp-0.5) + matrix.c * (-0.5) + matrix.tx)
		 * points_in[2].x = (matrix.a * (-0.5) + matrix.c * (bp-0.5) + matrix.tx)
		*/
		int rp = refrect.width();
		int bp = refrect.height();
		double[][] coeff = new double[MAX_COEFF][];	// 係数
		double[] val = new double[MAX_COEFF];	// 定数項(右辺の解)
		double[] tmp = new double[MAX_COEFF];
		for( int i = 0; i < MAX_COEFF; i++ ) {
			coeff[i] = new double[MAX_COEFF];
			val[i] = points_in[i].x;
			coeff[i][0] = -0.5;
			coeff[i][1] = -0.5;
			coeff[i][2] = 1.0;
		}
		coeff[1][0] = rp-0.5;
		coeff[2][1] = bp-0.5;
		if( gaussianElimination( coeff, tmp, val ) == false ) {
			return false;
		}
		double a = val[0];
		double c = val[1];
		double tx = val[2];
		/*
		 * 連立三元一次方程式
		 * points_in[0].y = (matrix.b * (-0.5) + matrix.d * (-0.5) + matrix.ty)
		 * points_in[1].y = (matrix.b * (rp-0.5) + matrix.d * (-0.5) + matrix.ty)
		 * points_in[2].y = (matrix.b * (-0.5) + matrix.d * (bp-0.5) + matrix.ty)
		*/
		for( int i = 0; i < MAX_COEFF; i++ ) {
			val[i] = points_in[i].y;
			coeff[i][0] = -0.5;
			coeff[i][1] = -0.5;
			coeff[i][2] = 1.0;
		}
		coeff[1][0] = rp-0.5;
		coeff[2][1] = bp-0.5;
		if( gaussianElimination( coeff, tmp, val ) == false ) {
			return false;
		}
		double b = val[0];
		double d = val[1];
		double ty = val[2];
		AffineMatrix2D matrix = new AffineMatrix2D(a,b,c,d,tx,ty);

		independ();
		return mBitmap.affineBlt( destrect, ref.getBitmap(), refrect, matrix, method, opa, updaterect, hda, type, clear, clearcolor );
	}

	public boolean affineBlt(Rect destrect, BaseBitmap ref,
			Rect refrect, AffineMatrix2D matrix, int method,
			int opa, Rect updaterect, boolean hda, int type) throws TJSException {
		return affineBlt( destrect, ref, refrect, matrix, method, opa, updaterect, hda, type, false, 0 );
	}

	public boolean affineBlt(Rect destrect, BaseBitmap ref,
			Rect refrect, PointD[] points_in, int method, int opa,
			Rect updaterect, boolean hda, int type) throws TJSException {

		return affineBlt( destrect, ref, refrect, points_in, method, opa, updaterect, hda, type, false, 0 );
	}

	public boolean doBoxBlur(Rect rect, Rect area) throws TJSException {
		if(!is32BPP()) Message.throwExceptionMessage(Message.InvalidOperationFor8BPP);
		int i;
		if(rect.left < 0) rect.left = 0;
		if(rect.top < 0) rect.top = 0;
		if(rect.right > (i=getWidth())) rect.right = i;
		if(rect.bottom > (i=getHeight())) rect.bottom = i;
		if(rect.right - rect.left <= 0 || rect.bottom - rect.top <= 0)
			return false;

		if(area.right < area.left) {
			int t = area.right;
			area.right = area.left;
			area.left = t;
		}
		if(area.bottom < area.top) {
			int t = area.bottom;
			area.bottom = area.top;
			area.top = t;
		}

		if(area.left == 0 && area.right == 0 &&
			area.top == 0 && area.bottom == 0) return false; // no conversion occurs

		if(area.left > 0 || area.right < 0 || area.top > 0 || area.bottom < 0)
			Message.throwExceptionMessage(Message.BoxBlurAreaMustContainCenterPixel);

		long area_size = (long)(area.right - area.left + 1) * (area.bottom - area.top + 1);
		if( area_size < (1L<<23) ) {
			independ();
			mBitmap.doBoxBlurLoop( rect, area );
		} else {
			Message.throwExceptionMessage(Message.BoxBlurAreaMustBeSmallerThan16Million);
		}
		return true;
	}

	public boolean doBoxBlurForAlpha(Rect rect, Rect area) throws TJSException {
		if(!is32BPP()) Message.throwExceptionMessage(Message.InvalidOperationFor8BPP);
		int i;
		if(rect.left < 0) rect.left = 0;
		if(rect.top < 0) rect.top = 0;
		if(rect.right > (i=getWidth())) rect.right = i;
		if(rect.bottom > (i=getHeight())) rect.bottom = i;
		if(rect.right - rect.left <= 0 || rect.bottom - rect.top <= 0)
			return false;

		if(area.right < area.left) {
			int t = area.right;
			area.right = area.left;
			area.left = t;
		}
		if(area.bottom < area.top) {
			int t = area.bottom;
			area.bottom = area.top;
			area.top = t;
		}

		if(area.left == 0 && area.right == 0 &&
			area.top == 0 && area.bottom == 0) return false; // no conversion occurs

		if(area.left > 0 || area.right < 0 || area.top > 0 || area.bottom < 0)
			Message.throwExceptionMessage(Message.BoxBlurAreaMustContainCenterPixel);

		long area_size = (long)(area.right - area.left + 1) * (area.bottom - area.top + 1);
		if( area_size < (1L<<23) ) {
			independ();
			mBitmap.doBoxBlurLoopAlpha( rect, area );
		} else {
			Message.throwExceptionMessage(Message.BoxBlurAreaMustBeSmallerThan16Million);
		}
		return true;
	}

	public void adjustGamma(Rect rect, GammaAdjustData data) throws TJSException {
		if(!is32BPP()) Message.throwExceptionMessage(Message.InvalidOperationFor8BPP);

		int i;
		if(rect.left < 0) rect.left = 0;
		if(rect.top < 0) rect.top = 0;
		if(rect.right > (i=getWidth())) rect.right = i;
		if(rect.bottom > (i=getHeight())) rect.bottom = i;
		if(rect.right - rect.left <= 0 || rect.bottom - rect.top <= 0)
			return;

		GammaAdjustTempData tmp = new GammaAdjustTempData();
		tmp.Initialize(data);
		independ();
		mBitmap.adjustGamma(rect,tmp);
	}

	public void adjustGammaForAdditiveAlpha(Rect rect, GammaAdjustData data) throws TJSException {
		if(!is32BPP()) Message.throwExceptionMessage(Message.InvalidOperationFor8BPP);

		int i;
		if(rect.left < 0) rect.left = 0;
		if(rect.top < 0) rect.top = 0;
		if(rect.right > (i=getWidth())) rect.right = i;
		if(rect.bottom > (i=getHeight())) rect.bottom = i;
		if(rect.right - rect.left <= 0 || rect.bottom - rect.top <= 0)
			return;

		GammaAdjustTempData tmp = new GammaAdjustTempData();
		tmp.Initialize(data);
		independ();
		mBitmap.adjustGammaForAdditiveAlpha(rect,tmp);
	}

	public void doGrayScale(Rect rect) throws TJSException {
		if(!is32BPP()) Message.throwExceptionMessage(Message.InvalidOperationFor8BPP);

		int i;
		if(rect.left < 0) rect.left = 0;
		if(rect.top < 0) rect.top = 0;
		if(rect.right > (i=getWidth())) rect.right = i;
		if(rect.bottom > (i=getHeight())) rect.bottom = i;
		if(rect.right - rect.left <= 0 || rect.bottom - rect.top <= 0)
			return;

		independ();
		mBitmap.doGrayScale(rect);
	}

	public void flipLR(Rect rect) throws TJSException {
		if(rect.left < 0 || rect.top < 0 || rect.right > getWidth() || rect.bottom > getHeight())
			Message.throwExceptionMessage(Message.SrcRectOutOfBitmap);

		independ();
		mBitmap.flipLR(rect);
	}

	public void flipUD(Rect rect) throws TJSException {
		if(rect.left < 0 || rect.top < 0 || rect.right > getWidth() || rect.bottom > getHeight())
			Message.throwExceptionMessage(Message.SrcRectOutOfBitmap);

		independ();
		mBitmap.flipUD(rect);
	}

	public void convertAddAlphaToAlpha() {
		if( mBitmap.isAlphaPremultiplied() == true ) {
			independ();
			mBitmap.coerceData(false);
		}
	}

	public void convertAlphaToAddAlpha() {
		if( mBitmap.isAlphaPremultiplied() == false ) {
			independ();
			mBitmap.coerceData(true);
		}
	}
	public boolean isAlphaPremultiplied() {
		return mBitmap.isAlphaPremultiplied();
	}
	/**
	 * 最初の列で最も多く使われている色を透過色として扱う
	 * @throws TJSException
	 */
	public void makeAlphaFromAdaptiveColor() throws TJSException {
		if( is32BPP() == false ) return;
		mBitmap.makeAlphaFromAdaptiveColor();
	}
	public void purgeImage() {
		mBitmap.purgeImage();
	}
}
