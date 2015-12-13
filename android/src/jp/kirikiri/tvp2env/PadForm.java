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


/**
 * Android ではテキスト入力ダイアログにした方がいいかな
 *
 */
public class PadForm {

	/**
	 *
	 */
	private static final long serialVersionUID = -193457542364045737L;

	private static final int DEFAULT_WIDTH = 600;
	private static final int DEFAULT_HEIGHT = 400;

	private static final int
		ssNone       = 0,
		ssHorizontal = 1,
		ssVertical   = 2,
		ssBoth       = 3;

	private String mSaveFileName;

	public PadForm() {
		super();
	}

	public void setFreeOnTerminate( boolean t ) {
	}
	public void setExecButtonEnabled( boolean t ) {
	}

	public void setLines( String lines ) {
	}
	public String getLines() { return null;
	}

	public void goToLine( int pos /*1 + e.getBlock().srcPosToLine( e.getPosition() ) - e.getBlock().getLineOffset()*/ ) {
	}
	public void setReadOnly( boolean read ) {
	}
	public boolean getReadOnly() { return true;
	}
	public void setStatusText( String mes ) {
	}
	public String getStatusText() {
		return null;
	}

	public void setCaption( String caption ) {
		setTitle( caption );
	}
	public void setVisible( boolean state ) {
	}
	public boolean getVisible() {
		return false;
	}
	public void setTitle( final String title) {
	}
	public String getTitle() {
		return null;
	}

	public int getEditColor() {
		return 0;
	}

	public void setEditColor(int color) {
	}

	public String getFileName() {
		return mSaveFileName;
	}

	public void setFileName(String name) {
		mSaveFileName = name;
	}

	public int getFontColor() {
		return 0;
	}

	public void setFontColor(int color) {
	}

	public int getFontHeight() {
		return 10;
	}

	public void setFontHeight(int height) {
	}

	public int getFontSize() {
		return 10;
	}

	public void setFontSize(int size) {
	}

	public boolean containsFontStyle(int style) {
		return false;
	}

	public void addFontStyle(int style) {
	}

	public void removeFontStyle(int style) {
	}

	public boolean getFontBold() {
		return false;
	}

	public void setFontBold(boolean b) {
	}

	public boolean getFontItalic() {
		return false;
	}

	public void setFontItalic(boolean b) {
	}

	public boolean getFontUnderline() {
		return false;
	}

	public void setFontUnderline(boolean b) {
	}

	public boolean getFontStrikeOut() {
		return false;
	}

	public void setFontStrikeOut(boolean b) {
	}
	public String getFontName() {
		return null;
	}

	public void setFontName(String name) {
	}

	public boolean getWordWrap() {
		return false;
	}

	public void setWordWrap(boolean ww) {
	}

	public int getOpacity() {
		return 255;
	}

	public void setOpacity(int opa) {
		// ignore
	}

	public boolean getStatusBarVisible() {
		return false;
	}

	public void setStatusBarVisible(boolean vis) {
	}

	public int getScrollBarsVisible() {
		return ssNone;
	}

	public void setScrollBarsVisible(int vis) {
	}

	public int getBorderStyle() {
		return WindowForm.bsSingle;
	}

	public void setBorderStyle(int style) {
	}

	public int getFormHeight() {
		return 0;
	}

	public int getFormWidth() {
		return 0;
	}

	public int getFormTop() {
		return 0;
	}

	public int getFormLeft() {
		return 0;
	}

	public void setFormHeight(int value) {
	}

	public void setFormWidth(int value) {
	}

	public void setFormTop(int value) {
	}

	public void setFormLeft(int value) {
	}

	public void setToolBarVisible(boolean b) {
	}

	public void setToolBarEnabled(boolean b) {
	}

	public void setExecuteButtonVisible(boolean b) {
	}

	public void setExecuteButtonEnabled(boolean b) {
	}

	public void setStatusBarLeft(int i) {
	}

	public void setStatusBarWidth(int width) {
	}

	public int getWidth() {
		return 0;
	}

}
