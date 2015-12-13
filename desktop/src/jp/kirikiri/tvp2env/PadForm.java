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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;

import jp.kirikiri.tvp2.visual.LayerNI;

public class PadForm extends JFrame {

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

	private JScrollPane mScrollPane;
	private JTextArea mTextArea;
	private JLabel mStatusBar;
	private String mSaveFileName;

	public PadForm() {
		super();
		getContentPane().setLayout(new BorderLayout());

		mTextArea = new JTextArea();

		mScrollPane = new JScrollPane(mTextArea);
		mScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		mScrollPane.setPreferredSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
		getContentPane().add(mScrollPane, BorderLayout.CENTER);


		mStatusBar = new JLabel();
		getContentPane().add( mStatusBar, BorderLayout.SOUTH );

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
	}

	public void setFreeOnTerminate( boolean t ) {
	}
	public void setExecButtonEnabled( boolean t ) {
	}

	//public void setLines( e.getBlock.getScript() ) {
	public void setLines( String lines ) {
		mTextArea.setText( lines );
		JScrollBar bar = mScrollPane.getVerticalScrollBar();
		if( bar != null ) {
			bar.setMaximum(mTextArea.getLineCount());
		}
	}
	public String getLines() {
		return mTextArea.getText();
	}

	public void goToLine( int pos /*1 + e.getBlock().srcPosToLine( e.getPosition() ) - e.getBlock().getLineOffset()*/ ) {
		mTextArea.setCaretPosition( pos );
		try {
			mTextArea.setSelectionStart( mTextArea.getLineStartOffset(pos) );
			mTextArea.setSelectionEnd( mTextArea.getLineStartOffset(pos+1) );
		} catch (BadLocationException e) {
		}
		Dimension dim = mTextArea.getPreferredScrollableViewportSize();
		int lineheight = (dim.height / mTextArea.getLineCount());
		int y = lineheight * pos;
		mScrollPane.scrollRectToVisible( new Rectangle(0,y,dim.width,lineheight) );
	}
	public void setReadOnly( boolean read ) {
		mTextArea.setEditable( !read );
	}
	public boolean getReadOnly() {
		return !mTextArea.isEditable();
	}
	public void setStatusText( String mes ) {
		mStatusBar.setText( mes );
	}
	public String getStatusText() {
		return mStatusBar.getText();
	}

	public void setCaption( String caption ) {
		setTitle( caption );
	}
	@Override
	public void setVisible( boolean state ) {
		super.setVisible(state);
	}
	public boolean getVisible() {
		return isVisible();
	}
	@Override
	public void setTitle( final String title) {
		super.setTitle( title );
	}
	@Override
	public String getTitle() {
		return super.getTitle();
	}

	public int getEditColor() {
		return mTextArea.getBackground().getRGB();
	}

	public void setEditColor(int color) {
		color = LayerNI.toActualColor(color);
		mTextArea.setBackground(new Color(color,false) );
	}

	public String getFileName() {
		return mSaveFileName;
	}

	public void setFileName(String name) {
		mSaveFileName = name;
	}

	public int getFontColor() {
		return mTextArea.getForeground().getRGB();
	}

	public void setFontColor(int color) {
		color = LayerNI.toActualColor(color);
		mTextArea.setForeground(new Color(color,false));
	}

	public int getFontHeight() {
		Font font = new Font(mTextArea.getFont());
		return font.getHeight();
	}

	public void setFontHeight(int height) {
		Font font = new Font(mTextArea.getFont());
		font.setHeight(height);
		mTextArea.setFont(font.getFont());
	}

	public int getFontSize() {
		return mTextArea.getFont().getSize();
	}

	public void setFontSize(int size) {
		Font font = new Font(mTextArea.getFont());
		font.setHeight(size);
		mTextArea.setFont(font.getFont());
	}

	public boolean containsFontStyle(int style) {
		Font font = new Font(mTextArea.getFont());
		switch(style) {
		case Font.TF_ITALIC:
			return font.getItalic();
		case Font.TF_BOLD:
			return font.getBold();
		case Font.TF_UNDERLINE:
			return font.getUnderline();
		case Font.TF_STRIKEOUT:
			return font.getStrikeout();
		}
		return false;
	}

	public void addFontStyle(int style) {
		Font font = new Font(mTextArea.getFont());
		switch(style) {
		case Font.TF_ITALIC:
			font.setItalic(true);
			mTextArea.setFont(font.getFont());
			break;
		case Font.TF_BOLD:
			font.setBold(true);
			mTextArea.setFont(font.getFont());
			break;
		case Font.TF_UNDERLINE:
			font.setUnderline(true);
			mTextArea.setFont(font.getFont());
			break;
		case Font.TF_STRIKEOUT:
			font.setStrikeout(true);
			mTextArea.setFont(font.getFont());
			break;
		}
	}

	public void removeFontStyle(int style) {
		Font font = new Font(mTextArea.getFont());
		switch(style) {
		case Font.TF_ITALIC:
			font.setItalic(false);
			mTextArea.setFont(font.getFont());
			break;
		case Font.TF_BOLD:
			font.setBold(false);
			mTextArea.setFont(font.getFont());
			break;
		case Font.TF_UNDERLINE:
			font.setUnderline(false);
			mTextArea.setFont(font.getFont());
			break;
		case Font.TF_STRIKEOUT:
			font.setStrikeout(false);
			mTextArea.setFont(font.getFont());
			break;
		}
	}

	public boolean getFontBold() {
		Font font = new Font(mTextArea.getFont());
		return font.getBold();
	}

	public void setFontBold(boolean b) {
		Font font = new Font(mTextArea.getFont());
		font.setBold(true);
		mTextArea.setFont(font.getFont());
	}

	public boolean getFontItalic() {
		Font font = new Font(mTextArea.getFont());
		return font.getItalic();
	}

	public void setFontItalic(boolean b) {
		Font font = new Font(mTextArea.getFont());
		font.setItalic(true);
		mTextArea.setFont(font.getFont());
	}

	public boolean getFontUnderline() {
		Font font = new Font(mTextArea.getFont());
		return font.getUnderline();
	}

	public void setFontUnderline(boolean b) {
		Font font = new Font(mTextArea.getFont());
		font.setUnderline(true);
		mTextArea.setFont(font.getFont());
	}

	public boolean getFontStrikeOut() {
		Font font = new Font(mTextArea.getFont());
		return font.getStrikeout();
	}

	public void setFontStrikeOut(boolean b) {
		Font font = new Font(mTextArea.getFont());
		font.setStrikeout(true);
		mTextArea.setFont(font.getFont());
	}
	public String getFontName() {
		return mTextArea.getFont().getFontName();
	}

	public void setFontName(String name) {
		Font font = new Font(mTextArea.getFont());
		font.setFaceName(name);
		mTextArea.setFont(font.getFont());
	}

	public boolean getWordWrap() {
		return mTextArea.getLineWrap();
	}

	public void setWordWrap(boolean ww) {
		mTextArea.setLineWrap(ww);
		mTextArea.setWrapStyleWord(ww);
	}

	// 1.7 から Window.getOpacity が追加されているので、それに返値の型を合わせる
	public float getOpacity() {
		return 1.0f;
	}

	public void setOpacity(int opa) {
		// ignore
	}

	public boolean getStatusBarVisible() {
		return mStatusBar.isVisible();
	}

	public void setStatusBarVisible(boolean vis) {
		mStatusBar.setVisible(vis);
	}

	public int getScrollBarsVisible() {
		JScrollBar vbar = mScrollPane.getVerticalScrollBar();
		JScrollBar hbar = mScrollPane.getHorizontalScrollBar();

		if( vbar.isVisible() || hbar.isVisible() ) {
			if( vbar.isVisible() && hbar.isVisible() ) {
				return ssBoth;
			} else if( vbar.isVisible() ) {
				return ssVertical;
			} else {
				return ssHorizontal;
			}
		} else {
			return ssNone;
		}
	}

	public void setScrollBarsVisible(int vis) {
		JScrollBar vbar = mScrollPane.getVerticalScrollBar();
		JScrollBar hbar = mScrollPane.getHorizontalScrollBar();
		switch(vis) {
		case ssNone:
			vbar.setVisible(false);
			hbar.setVisible(false);
			break;
		case ssHorizontal:
			vbar.setVisible(false);
			hbar.setVisible(true);
			break;
		case ssVertical:
			vbar.setVisible(true);
			hbar.setVisible(false);
			break;
		case ssBoth:
			vbar.setVisible(true);
			hbar.setVisible(true);
			break;
		}
	}

	public int getBorderStyle() {
		if( isResizable() ) {
			return WindowForm.bsSizeable;
		} else {
			return WindowForm.bsSingle;
		}
	}

	public void setBorderStyle(int style) {
		switch( style ) {
		case WindowForm.bsNone:
			setResizable(false);
			break;
		case WindowForm.bsSingle:
			setResizable(false);
			break;
		case WindowForm.bsSizeable:
			setResizable(true);
			break;
		case WindowForm.bsDialog:
			setResizable(false);
			break;
		case WindowForm.bsToolWindow:
			setResizable(false);
			break;
		case WindowForm.bsSizeToolWin:
			setResizable(true);
			break;
		}
	}

	public int getFormHeight() {
		return getHeight();
	}

	public int getFormWidth() {
		return getWidth();
	}

	public int getFormTop() {
		return getY();
	}

	public int getFormLeft() {
		return getX();
	}

	public void setFormHeight(int value) {
		setSize( getWidth(), value );
	}

	public void setFormWidth(int value) {
		setSize( value, getHeight() );
	}

	public void setFormTop(int value) {
		setLocation( getX(), value );
	}

	public void setFormLeft(int value) {
		setLocation( value, getY() );
	}

	public void setToolBarVisible(boolean b) {
		// TODO 自動生成されたメソッド・スタブ
	}

	public void setToolBarEnabled(boolean b) {
		// TODO 自動生成されたメソッド・スタブ
	}

	public void setExecuteButtonVisible(boolean b) {
		// TODO 自動生成されたメソッド・スタブ
	}

	public void setExecuteButtonEnabled(boolean b) {
		// TODO 自動生成されたメソッド・スタブ
	}

	public void setStatusBarLeft(int i) {
		// TODO 自動生成されたメソッド・スタブ
	}

	public void setStatusBarWidth(int width) {
		// TODO 自動生成されたメソッド・スタブ
	}



}
