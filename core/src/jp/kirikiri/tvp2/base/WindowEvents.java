/**
 ******************************************************************************
 * Copyright (c), Takenori Imoto
 * �� software http://www.kaede-software.com/
 * All rights reserved.
 ******************************************************************************
 * �\�[�X�R�[�h�`�����o�C�i���`�����A�ύX���邩���Ȃ������킸�A�ȉ��̏�����
 * �����ꍇ�Ɍ���A�ĔЕz����юg�p��������܂��B
 *
 * �E�\�[�X�R�[�h���ĔЕz����ꍇ�A��L�̒��쌠�\���A�{�����ꗗ�A����щ��L�Ɛ�
 *   �������܂߂邱�ƁB
 * �E�o�C�i���`���ōĔЕz����ꍇ�A�Еz���ɕt���̃h�L�������g���̎����ɁA��L��
 *   ���쌠�\���A�{�����ꗗ�A����щ��L�Ɛӏ������܂߂邱�ƁB
 * �E���ʂɂ����ʂ̋��Ȃ��ɁA�{�\�t�g�E�F�A����h���������i�̐�`�܂��͔̔�
 *   ���i�ɁA�g�D�̖��O�܂��̓R���g���r���[�^�[�̖��O���g�p���Ă͂Ȃ�Ȃ��B
 *
 * �{�\�t�g�E�F�A�́A���쌠�҂���уR���g���r���[�^�[�ɂ���āu����̂܂܁v��
 * ����Ă���A�����َ����킸�A���ƓI�Ȏg�p�\���A����ѓ���̖ړI�ɑ΂���K
 * �����Ɋւ���Öق̕ۏ؂��܂߁A�܂�����Ɍ��肳��Ȃ��A�����Ȃ�ۏ؂�����܂�
 * ��B���쌠�҂��R���g���r���[�^�[���A���R�̂�������킸�A���Q�����̌�������
 * ����킸�A���ӔC�̍������_��ł��邩���i�ӔC�ł��邩�i�ߎ����̑��́j�s�@
 * �s�ׂł��邩���킸�A���ɂ��̂悤�ȑ��Q����������\����m�炳��Ă����Ƃ�
 * �Ă��A�{�\�t�g�E�F�A�̎g�p�ɂ���Ĕ��������i��֕i�܂��͑�p�T�[�r�X�̒��B�A
 * �g�p�̑r���A�f�[�^�̑r���A���v�̑r���A�Ɩ��̒��f���܂߁A�܂�����Ɍ��肳���
 * ���j���ڑ��Q�A�Ԑڑ��Q�A�����I�ȑ��Q�A���ʑ��Q�A�����I���Q�A�܂��͌��ʑ��Q��
 * ���āA��ؐӔC�𕉂�Ȃ����̂Ƃ��܂��B
 ******************************************************************************
 * �{�\�t�g�E�F�A�́A�g���g��2 ( http://kikyou.info/tvp/ ) �̃\�[�X�R�[�h��Java
 * �ɏ������������̂��ꕔ�g�p���Ă��܂��B
 * �g���g��2 Copyright (C) W.Dee <dee@kikyou.info> and contributors
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
