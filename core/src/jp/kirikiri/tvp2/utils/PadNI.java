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
package jp.kirikiri.tvp2.utils;

import jp.kirikiri.tjs2.Error;
import jp.kirikiri.tjs2.Dispatch2;
import jp.kirikiri.tjs2.NativeInstanceObject;
import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tjs2.Variant;
import jp.kirikiri.tjs2.VariantException;
import jp.kirikiri.tvp2env.PadForm;
import jp.kirikiri.tvp2env.SystemColor;


public class PadNI extends NativeInstanceObject {
	private PadForm mForm;
	/**
	 * true if this form was created by the userscript,
	 * otherwise (when created by the system as "Script Editor") this will be false
	 */
	private boolean mUserCreationMode;
	//private boolean mMultilineMode;

	public int construct( Variant[] param, Dispatch2 dsp ) throws VariantException, TJSException {
		int hr = super.construct(param, dsp);
		if( hr < 0 ) return hr;

		mForm = new PadForm();
		mForm.setExecButtonEnabled(false);

		return Error.S_OK;
	}
	public void invalidate() throws VariantException, TJSException {
		if(mForm!=null) mForm = null;
		super.invalidate();
	}

	// methods
	public void openFromStorage( final String name) {
	}
	public void saveToStorage( final String name) {
	}

	// properties
	public int getColor() {
		return mForm.getEditColor();
	}
	public void setColor(int color) {
		mForm.setEditColor(color);
	}
	public boolean getVisible() {
		return mForm.getVisible();
	}
	public void setVisible(boolean state) {
		mForm.setVisible(state);
	}
	public String getFileName() {
		return mForm.getFileName();
	}
	public void setFileName( final String name) {
		mForm.setFileName(name);
	}
	public void setText( final String content) {
		mForm.setLines(content);
	}
	public String getText() {
		return mForm.getLines();
	}
	public void setTitle( final String title) {
		mForm.setTitle( title );
	}
	public String getTitle() {
		return mForm.getTitle();
	}

	public int getFontColor() {
		return mForm.getFontColor();
	}
	public void setFontColor(int color) {
		mForm.setFontColor(color);
	}
	public int getFontHeight() {
		return mForm.getFontHeight();
	}	// pixel
	public void setFontHeight(int height) {
		mForm.setFontHeight(height);
	}

	public int getFontSize() {
		return mForm.getFontSize();
	}	// point
	public void setFontSize(int size) {
		mForm.setFontSize(size);
	}

	public boolean containsFontStyle(int style) {
		return mForm.containsFontStyle(style);
	}
	public void addFontStyle(int style) {
		mForm.addFontStyle(style);
	}
	public void removeFontStyle(int style) {
		mForm.removeFontStyle(style);
	}

	public boolean getFontBold() {
		return mForm.getFontBold();
	}
	public void setFontBold( boolean b ) {
		mForm.setFontBold(b);
	}
	public boolean getFontItalic() {
		return mForm.getFontItalic();
	}
	public void setFontItalic( boolean b ) {
		mForm.setFontItalic(b);
	}
	public boolean getFontUnderline() {
		return mForm.getFontUnderline();
	}
	public void setFontUnderline( boolean b ) {
		mForm.setFontUnderline(b);
	}
	public boolean getFontStrikeOut() {
		return mForm.getFontStrikeOut();
	}
	public void setFontStrikeOut( boolean b ) {
		mForm.setFontStrikeOut(b);
	}

	public String getFontName() {
		return mForm.getFontName();
	}
	public void setFontName( final String name ) {
		mForm.setFontName(name);
	}

	public boolean isReadOnly() {
		return mForm.getReadOnly();
	}
	public void setReadOnly(boolean ro) {
		mForm.setReadOnly(ro);
	}

	public boolean getWordWrap() {
		return mForm.getWordWrap();
	}
	public void setWordWrap(boolean ww) {
		mForm.setWordWrap(ww);
	}

	public int getOpacity() {
		return (int) (mForm.getOpacity()*255); // PadForm クラス getOpacity は 0.0 ～ 1.0 を返す(現在1.0固定)
	}
	public void setOpacity(int opa) {
		mForm.setOpacity(opa);
	}

	public boolean getStatusBarVisible() {
		return mForm.getStatusBarVisible();
	}
	public void setStatusBarVisible(boolean vis) {
		mForm.setStatusBarVisible(vis);
	}

	public int getScrollBarsVisible() {
		return mForm.getScrollBarsVisible();
	}
	public void setScrollBarsVisible(int vis) {
		mForm.setScrollBarsVisible(vis);
	}

	public int getBorderStyle() {
		return mForm.getBorderStyle();
	}
	public void setBorderStyle(int style) {
		mForm.setBorderStyle(style);
	}

	public String getStatusText() {
		return mForm.getStatusText();
	}
	public void setStatusText( final String title) {
		mForm.setStatusText(title);
	}

	// form position and size
	public int getFormHeight() {
		return mForm.getFormHeight();
	}
	public int getFormWidth() {
		return mForm.getFormWidth();
	}
	public int getFormTop() {
		return mForm.getFormTop();
	}
	public int getFormLeft() {
		return mForm.getFormLeft();
	}
	public void setFormHeight(int value) {
		mForm.setFormHeight(value);
	}
	public void setFormWidth(int value) {
		mForm.setFormWidth(value);
	}
	public void setFormTop(int value) {
		mForm.setFormTop(value);
	}
	public void setFormLeft(int value) {
		mForm.setFormLeft(value);
	}

	//
	public boolean getUserCreationMode() {
		return mUserCreationMode;
	}
	public void setUserCreationMode(boolean user) {
		mUserCreationMode = user;
		if( user ) {
			mForm.setToolBarVisible(false);
			mForm.setToolBarEnabled(false);
			mForm.setExecuteButtonVisible(false);
			mForm.setExecuteButtonEnabled(false);
			mForm.setStatusBarLeft(0);

			mForm.setEditColor(SystemColor.clWindow);
			mForm.setFontColor(SystemColor.clWindowText);
			mForm.setStatusBarWidth( mForm.getWidth() );
		}
	}

}
