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
package jp.kirikiri.tvp2.base;

import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tjs2.Variant;
import jp.kirikiri.tvp2.visual.WindowNI;
import jp.kirikiri.tvp2.visual.MenuItemNI;

public class WindowEvents {
	public static class OnCloseInputEvent extends BaseInputEvent {
		public OnCloseInputEvent( WindowNI win ) {
			super( win, ++InputEventTagMax );
		}
		public void deliver() {
			((WindowNI)getSource()).onClose();
		}
	}

	public static class OnResizeInputEvent extends BaseInputEvent {
		public OnResizeInputEvent( WindowNI win ) {
			super(win, ++InputEventTagMax);
		}
		public void deliver() {
			((WindowNI)getSource()).onResize();
		}
	}

	public static class OnClickInputEvent extends BaseInputEvent {
		private int X;
		private int Y;
		public OnClickInputEvent(WindowNI win, int x, int y) {
			super(win, ++InputEventTagMax);
			X = x;
			Y = y;
		}
		public void deliver() throws TJSException {
			((WindowNI)getSource()).onClick(X, Y);
		}
	}

	public static class OnDoubleClickInputEvent extends BaseInputEvent {
		private int X;
		private int Y;
		public OnDoubleClickInputEvent(WindowNI win, int x, int y) {
			super(win, ++InputEventTagMax);
			X = x;
			Y = y;
		}
		public void deliver() throws TJSException {
			((WindowNI)getSource()).onDoubleClick(X, Y);
		}
	}

	public static class OnMouseDownInputEvent extends BaseInputEvent {
		private int X;
		private int Y;
		//MouseButton Buttons;
		private int Buttons;
		private int Flags;
		public OnMouseDownInputEvent(WindowNI win, int x, int y, int buttons, int flags ) {
			super(win, ++InputEventTagMax);
			X = x;
			Y = y;
			Buttons = buttons;
			Flags = flags;
		}
		public void deliver() throws TJSException {
			((WindowNI)getSource()).onMouseDown(X, Y, Buttons, Flags);
		}
	}

	public static class OnMouseUpInputEvent extends BaseInputEvent {
		private int X;
		private int Y;
		//MouseButton Buttons;
		private int Buttons;
		private int Flags;
		public OnMouseUpInputEvent(WindowNI win, int x, int y, int buttons, int flags) {
			super(win, ++InputEventTagMax);
			X = x;
			Y = y;
			Buttons = buttons;
			Flags = flags;
		}
		public void deliver() throws TJSException {
			((WindowNI)getSource()).onMouseUp(X, Y, Buttons, Flags);
		}
	}

	public static class OnMouseMoveInputEvent extends BaseInputEvent {
		int X;
		int Y;
		int Flags;
		public OnMouseMoveInputEvent(WindowNI win, int x, int y, int flags) {
			super(win, ++InputEventTagMax);
			X = x;
			Y = y;
			Flags = flags;
		}
		public void deliver() throws TJSException
		{ ((WindowNI)getSource()).onMouseMove(X, Y, Flags); }
	}

	public static class OnReleaseCaptureInputEvent extends BaseInputEvent {
		public OnReleaseCaptureInputEvent(WindowNI win) {
			super(win, ++InputEventTagMax);
		}
		public void deliver()
		{ ((WindowNI)getSource()).onReleaseCapture(); }
	}

	public static class OnMouseOutOfWindowInputEvent extends BaseInputEvent {
		public OnMouseOutOfWindowInputEvent(WindowNI win) {
			super(win, ++InputEventTagMax);
		}
		public void deliver() throws TJSException
		{ ((WindowNI)getSource()).onMouseOutOfWindow(); }
	}

	public static class OnMouseEnterInputEvent extends BaseInputEvent {
		public OnMouseEnterInputEvent(WindowNI win) {
			super(win, ++InputEventTagMax);
		}
		public void deliver()
		{ ((WindowNI)getSource()).onMouseEnter(); }
	}

	public static class OnMouseLeaveInputEvent extends BaseInputEvent {
		public OnMouseLeaveInputEvent(WindowNI win) {
			super(win, ++InputEventTagMax);
		}
		public void deliver()
		{ ((WindowNI)getSource()).onMouseLeave(); }
	}

	public static class OnKeyDownInputEvent extends BaseInputEvent {
		int Key;
		int Shift;
		public OnKeyDownInputEvent(WindowNI win, int key, int shift) {
			super(win, ++InputEventTagMax);
			Key = key;
			Shift = shift;
		}
		public void deliver() throws TJSException
		{ ((WindowNI)getSource()).onKeyDown(Key, Shift); }
	}

	public static class OnKeyUpInputEvent extends BaseInputEvent {
		int Key;
		int Shift;
		public OnKeyUpInputEvent(WindowNI win, int key, int shift) {
			super(win, ++InputEventTagMax);
			Key = key;
			Shift = shift;
		}
		public void deliver()
		{ ((WindowNI)getSource()).onKeyUp(Key, Shift); }
	}

	public static class OnKeyPressInputEvent extends BaseInputEvent {
		char Key;
		public OnKeyPressInputEvent(WindowNI win, char key) {
			super(win, ++InputEventTagMax);
			Key = key;
		}
		public void deliver()
		{ ((WindowNI)getSource()).onKeyPress(Key); }
	}

	public static class OnFileDropInputEvent extends BaseInputEvent {
		Variant Array;
		public OnFileDropInputEvent(WindowNI win, final Variant val) {
			super(win, ++InputEventTagMax);
			Array = new Variant(val);
		}
		public void deliver()
		{ ((WindowNI)getSource()).onFileDrop(Array); }
	}

	public static class OnMouseWheelInputEvent extends BaseInputEvent {
		int Shift;
		int WheelDelta;
		int X;
		int Y;
		public OnMouseWheelInputEvent(WindowNI win, int shift, int wheeldelta, int x, int y) {
			super(win, ++InputEventTagMax);
			Shift = shift;
			WheelDelta = wheeldelta;
			X = x;
			Y = y;
		}
		public void deliver()
		{ ((WindowNI)getSource()).onMouseWheel(Shift, WheelDelta, X, Y); }
	}

	public static class OnPopupHideInputEvent extends BaseInputEvent {
		public OnPopupHideInputEvent(WindowNI win) {
			super(win, ++InputEventTagMax);
		}
		public void deliver()
		{ ((WindowNI)getSource()).onPopupHide(); }
	}

	public static class OnWindowActivateEvent extends BaseInputEvent {
		boolean ActivateOrDeactivate;
		public OnWindowActivateEvent(WindowNI win, boolean activate_or_deactivate) {
			super(win, ++InputEventTagMax);
			ActivateOrDeactivate = activate_or_deactivate;
		}
		public void deliver()
		{ ((WindowNI)getSource()).onActivate(ActivateOrDeactivate); }
	}

	public static class OnOrientationChangedEvent extends BaseInputEvent {
		public OnOrientationChangedEvent(WindowNI win) {
			super(win, ++InputEventTagMax);
		}
		public void deliver()
		{ ((WindowNI)getSource()).onOrientationChanged(); }
	}


	public static class OnMenuItemClickInputEvent extends BaseInputEvent {
		public OnMenuItemClickInputEvent( MenuItemNI menu ) {
			super(menu, ++InputEventTagMax);
		}
		public void deliver() {
			((MenuItemNI)getSource()).onClick();
		}
	}
}
