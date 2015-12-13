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
package jp.kirikiri.tjs2;

public class TJSScriptError extends TJSException {

	/**
	 *
	 */
	private static final long serialVersionUID = -1727870605938357683L;

	private static final int  MAX_TRACE_TEXT_LEN = 1500;

	private SourceCodeAccessor mAccessor;
	private int mPosition;
	private String mTrace;


	public SourceCodeAccessor getAccessor() { return mAccessor; }

	public int getPosition() { return mPosition; }

	public int GetSourceLine() {
		return mAccessor.srcPosToLine(mPosition) +1;
	}

	public final String getBlockName() {
		final String name = mAccessor.getName();
		return name != null ? name : "";
	}

	public final String getTrace() { return mTrace; }

	public boolean addTrace( ScriptBlock block, int srcpos ) {
		int len = mTrace.length();
		if( len >= MAX_TRACE_TEXT_LEN) return false;

		if( len != 0 ) mTrace += " <-- ";
		mTrace += block.getLineDescriptionString(srcpos);
		return true;
	}
	public boolean addTrace( final String data ) {
		int len = mTrace.length();
		if(len >= MAX_TRACE_TEXT_LEN) return false;
		if(len != 0) mTrace += " <-- ";
		mTrace += data;
		return true;
	}

	public TJSScriptError( final String Msg, SourceCodeAccessor accessor, int pos ) {
		super(Msg);
		mAccessor = accessor;
		mPosition = pos;
		mTrace = new String();
	}
	public TJSScriptError( final TJSScriptError ref) {
		mAccessor = ref.mAccessor;
		mPosition = ref.mPosition;
		mTrace = ref.mTrace;
	}

	public boolean addTrace(InterCodeObject context, int codepos) {
		int len = mTrace.length();
		if(len >= MAX_TRACE_TEXT_LEN) return false;

		if(len != 0) mTrace += " <-- ";
		mTrace += context.getPositionDescriptionString(codepos);
		return true;
	}
}
