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

import jp.kirikiri.tjs2.Dispatch2;
import jp.kirikiri.tjs2.TJSException;

public interface WindowInterface {
	/**
	 *  @throws TJSException
	 * @brief	元画像のサイズが変更された
	 *  @note	描画デバイスが、元画像のサイズが変更されたことを通知するために呼ぶ。
	 * 			ウィンドウは iTVPDrawDevice::GetSrcSize() を呼び出して元画像の
	 * 			サイズを取得した後、ズームなどの計算を行ってから
	 * 			iTVPDrawDevice::SetTargetWindow() を呼び出す。
	 */
	public void notifySrcResize() throws TJSException;

	/**
	 *  @brief		マウスカーソルの形状をデフォルトに戻す
	 *  @note		マウスカーソルの形状をデフォルトの物に戻したい場合に呼ぶ
	 */
	public void setDefaultMouseCursor(); // set window mouse cursor to default

	/**
	 *  @brief		マウスカーソルの形状を設定する
	 *  @param		cursor		マウスカーソル形状番号
	 */
	public void setMouseCursor( int cursor ); // set window mouse cursor

	/**
	 *  @brief		マウスカーソルの位置を取得する
	 *  @param		x			描画矩形内の座標におけるマウスカーソルのx位置
	 *  @param		y			描画矩形内の座標におけるマウスカーソルのy位置
	 */
	public void getCursorPos( Point pt );
		// get mouse cursor position in primary layer's coordinates

	/**
	 *  @brief		マウスカーソルの位置を設定する
	 *  @param		x			描画矩形内の座標におけるマウスカーソルのx位置
	 *  @param		y			描画矩形内の座標におけるマウスカーソルのy位置
	 */
	public void setCursorPos( int x, int y );

	/**
	 *  @brief		ウィンドウのマウスキャプチャを解放する
	 *  @note		ウィンドウのマウスキャプチャを解放すべき場合に呼ぶ。
	 *  @note		このメソッドでは基本的には ::ReleaseCapture() などで
	 * 				マウスのキャプチャを開放すること。
	 */
	public void windowReleaseCapture();

	/**
	 *  @brief		ツールチップヒントを設定する
	 *  @param		text		ヒントテキスト(空文字列の場合はヒントの表示をキャンセルする)
	 */
	public void setHintText( final String text );

	/**
	 *  @brief		注視ポイントの設定
	 *  @param		layer		フォント情報の含まれるレイヤ
	 *  @param		pt			描画矩形内の座標における注視ポイントの位置
	 */
	public void setAttentionPoint( LayerNI layer, Point pt );

	/**
	 *  @brief		注視ポイントの解除
	 */
	public void disableAttentionPoint();

	/**
	 *  @brief		IMEモードの設定
	 *  @param		mode		IMEモード
	 */
	public void setImeMode( int mode );

	/**
	 *  @brief		IMEモードのリセット
	 */
	public void resetImeMode();

	/**
	 *  @brief		iTVPWindow::Update() の呼び出しを要求する
	 *  @note		ウィンドウに対して iTVPWindow::Update() を次の適当なタイミングで
	 * 				呼び出すことを要求する。
	 * 				iTVPWindow::Update() が呼び出されるまでは何回 RequestUpdate() を
	 * 				呼んでも効果は同じである。また、一度 iTVPWindow::Update() が
	 * 				呼び出されると、再び RequestUpdate() を呼ばない限りは
	 * 				iTVPWindow::Update() は呼ばれない。
	 */
	public void requestUpdate();


	/**
	 *  @brief		WindowのiTJSDispatch2インターフェースを取得する
	 */
	public Dispatch2 getWindowDispatch();
}
