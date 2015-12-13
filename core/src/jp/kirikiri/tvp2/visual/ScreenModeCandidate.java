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

/**
 * Structure for monitor screen mode candidate
 */
public class ScreenModeCandidate extends ScreenMode {
	public int mZoomNumer; // zoom ratio numer
	public int mZoomDenom; // zoom ratio denom
	public int mRankZoomIn;
	public int mRankBPP;
	public int mRankZoomBeauty;
	public int mRankSize; // candidate preference priority (lower value is higher preference)

	public String dump() {
		StringBuilder builder = new StringBuilder(128);
		builder.append(super.dump());
		builder.append(", ZoomNumer=");
		builder.append(mZoomNumer);
		builder.append(", ZoomDenom=");
		builder.append(mZoomDenom);
		builder.append(", RankZoomIn=");
		builder.append(mRankZoomIn);
		builder.append(", RankBPP=");
		builder.append(mRankBPP);
		builder.append(", RankZoomBeauty=");
		builder.append(mRankZoomBeauty);
		builder.append(", RankSize=");
		builder.append(mRankSize);
		return builder.toString();
	}

	// operator <
	public boolean littlerThan( final ScreenModeCandidate rhs ) {
		if(mRankZoomIn < rhs.mRankZoomIn) return true;
		if(mRankZoomIn > rhs.mRankZoomIn) return false;
		if(mRankBPP < rhs.mRankBPP) return true;
		if(mRankBPP > rhs.mRankBPP) return false;
		if(mRankZoomBeauty < rhs.mRankZoomBeauty) return true;
		if(mRankZoomBeauty > rhs.mRankZoomBeauty) return false;
		if(mRankSize < rhs.mRankSize) return true;
		if(mRankSize > rhs.mRankSize) return false;
		return false;
	}
}
