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

public class GammaAdjustTempData {

	public byte[] B;
	public byte[] G;
	public byte[] R;

	public GammaAdjustTempData() {
		R = new byte[256];
		G = new byte[256];
		B = new byte[256];
	}
	public void Initialize( GammaAdjustData data ) {
		// make table
		double ramp = data.RCeil - data.RFloor;
		double gamp = data.GCeil - data.GFloor;
		double bamp = data.BCeil - data.BFloor;

		double rgamma = 1.0/data.RGamma; /* we assume data.?Gamma is a non-zero value here */
		double ggamma = 1.0/data.GGamma;
		double bgamma = 1.0/data.BGamma;

		double rfloor = data.RFloor;
		double gfloor = data.GFloor;
		double bfloor = data.BFloor;
		for( int i = 0;i < 256; i++ ) {
			double rate = (double)i/255.0;
			int n;
			n = (int)(Math.pow(rate, rgamma)*ramp+0.5+rfloor);
			if(n<0) n=0;
			else if(n>255) n=255;
			R[i]= (byte)( n & 0xff);

			n = (int)(Math.pow(rate, ggamma)*gamp+0.5+gfloor);
			if(n<0) n=0;
			else if(n>255) n=255;
			G[i]= (byte)( n & 0xff);

			n = (int)(Math.pow(rate, bgamma)*bamp+0.5+bfloor);
			if(n<0) n=0;
			else if(n>255) n=255;
			B[i]= (byte)( n & 0xff);
		}
	}
}
