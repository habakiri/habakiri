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
package jp.kirikiri.tvp2.visual;

import jp.kirikiri.tvp2env.Font;

public class FontInfomation {
	private String mFaceName;
	private int mHeight;
	private int mFlags;
	private int mAngle;

	public FontInfomation() {}
	public FontInfomation( final String face, int height, int flags, int angle ) {
		mFaceName = face;
		mHeight = height;
		mFlags = flags;
		mAngle = angle;
	}
	public FontInfomation( Font f ) {
		mFaceName = f.getFaceName();
		mHeight = f.getHeight();
		mAngle = f.getAngle();
		mFlags = 0;
		mFlags |= f.getBold() ? Font.TF_BOLD : 0;
		mFlags |= f.getItalic() ? Font.TF_ITALIC : 0;
		mFlags |= f.getStrikeout() ? Font.TF_STRIKEOUT : 0;
		mFlags |= f.getUnderline() ? Font.TF_UNDERLINE : 0;
	}
	public FontInfomation(FontInfomation font) {
		mFaceName = font.mFaceName;
		mHeight = font.mHeight;
		mFlags = font.mFlags;
		mAngle = font.mAngle;
	}
	public void setFont( Font f ) {
		mFaceName = f.getFaceName();
		mHeight = f.getHeight();
		mAngle = f.getAngle();
		mFlags = 0;
		mFlags |= f.getBold() ? Font.TF_BOLD : 0;
		mFlags |= f.getItalic() ? Font.TF_ITALIC : 0;
		mFlags |= f.getStrikeout() ? Font.TF_STRIKEOUT : 0;
		mFlags |= f.getUnderline() ? Font.TF_UNDERLINE : 0;
	}
	public boolean equals(Object o){
		if( o instanceof FontInfomation ) {
			FontInfomation f = (FontInfomation)o;
			if( mFaceName == null ) {
				if( mFaceName == f.mFaceName && mHeight == f.mHeight && mFlags == f.mFlags && mAngle == f.mAngle ) {
					return true;
				} else {
					return false;
				}
			} else if( mFaceName.equals( f.mFaceName) && mHeight == f.mHeight && mFlags == f.mFlags && mAngle == f.mAngle ) {
				return true;
			} else {
				return false;
			}
		} else {
			return false ;
		}
	}
	public int hashCode() {
		int hash = 0;
		if( mFaceName != null ) {
			hash = mFaceName.hashCode();
		}
		hash ^= mFlags;
		hash ^= mAngle;
		hash ^= mHeight;
		return hash;
	}
}
