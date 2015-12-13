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

class SymbolData {

	static private final int
		SYMBOL_INIT = 0x2,
		SYMBOL_USING = 0x1;

	public String	mName;
	public int		mHash;
	public int		mSymFlags;
	public int		mFlags;
	public Variant	mValue;
	public SymbolData mNext;

	public void selfClear() {
		mName = null;
		mHash = 0;
		mFlags = 0;
		mValue = new Variant();
		mNext = null;
		mSymFlags = SYMBOL_INIT;
	}
	private void setNameInternal( String name ) throws TJSException {
		if( name == null ) throw new TJSException( Error.IDExpected );
		if( name.length() == 0 ) throw new TJSException( Error.IDExpected );
		if( mName != null && mName.equals(name) ) return;
		//mName = new String( name );
		mName = name;
	}
	public void setName( String name, int hash ) throws TJSException {
		// setNameInternal(name);
		mHash = hash;

		if( mName != null && mName.equals(name) ) return;
		if( name == null ) throw new TJSException( Error.IDExpected );
		if( name.length() == 0 ) throw new TJSException( Error.IDExpected );
		mName = name;
	}
	public final String getName() { return mName; }
	public void postClear() {
		mName = null;
		mValue = null;
		mValue = new Variant();
		mSymFlags &= ~SYMBOL_USING;
	}
	public void destory() {
		mName = null;
		mValue = null;
	}
	public boolean nameMatch( final String name ) {
		if( mName == name ) return true;
		//return mName != null && mName.equals( name );
		return mName.equals( name );
	}
	public void reShare() {
		// search shared string map using mapGlobalStringMap,
		// and ahsre the name string ( if it can )
		if( mName != null ) {
			mName = TJS.mapGlobalStringMap( mName );
		}
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(32);
		if( mName != null ) {
			builder.append(mName);
			builder.append(" : ");
		} else {
			builder.append("no name : ");
		}
		if( mValue != null ) builder.append(mValue.toString());
		else builder.append("empty");
		return builder.toString();
	}
}

