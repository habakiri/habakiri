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

public class LayerType {
	static public final int
		ltBinder = 0,
		ltCoverRect = 1,
		ltOpaque = 1, // the same as ltCoverRect
		ltTransparent = 2, // alpha blend
		ltAlpha = 2, // the same as ltTransparent
		ltAdditive = 3,
		ltSubtractive = 4,
		ltMultiplicative = 5,
		ltEffect = 6,
		ltFilter = 7,
		ltDodge = 8,
		ltDarken = 9,
		ltLighten = 10,
		ltScreen = 11,
		ltAddAlpha = 12, // additive alpha blend
		ltPsNormal = 13,
		ltPsAdditive = 14,
		ltPsSubtractive = 15,
		ltPsMultiplicative = 16,
		ltPsScreen = 17,
		ltPsOverlay = 18,
		ltPsHardLight = 19,
		ltPsSoftLight = 20,
		ltPsColorDodge = 21,
		ltPsColorDodge5 = 22,
		ltPsColorBurn = 23,
		ltPsLighten = 24,
		ltPsDarken = 25,
		ltPsDifference = 26,
		ltPsDifference5 = 27,
		ltPsExclusion = 28;

	static public final int
		omPsNormal = ltPsNormal,
		omPsAdditive = ltPsAdditive,
		omPsSubtractive = ltPsSubtractive,
		omPsMultiplicative = ltPsMultiplicative,
		omPsScreen = ltPsScreen,
		omPsOverlay = ltPsOverlay,
		omPsHardLight = ltPsHardLight,
		omPsSoftLight = ltPsSoftLight,
		omPsColorDodge = ltPsColorDodge,
		omPsColorDodge5 = ltPsColorDodge5,
		omPsColorBurn = ltPsColorBurn,
		omPsLighten = ltPsLighten,
		omPsDarken = ltPsDarken,
		omPsDifference = ltPsDifference,
		omPsDifference5 = ltPsDifference5,
		omPsExclusion = ltPsExclusion,
		omAdditive = ltAdditive,
		omSubtractive = ltSubtractive,
		omMultiplicative = ltMultiplicative,
		omDodge = ltDodge,
		omDarken = ltDarken,
		omLighten = ltLighten,
		omScreen = ltScreen,
		omAlpha = ltTransparent,
		omAddAlpha = ltAddAlpha,
		omOpaque = ltCoverRect,

		omAuto = 128;	// operation mode is guessed from the source layer type

	static public final int
		/** primal method; nearest neighbor method */
		stNearest = 0,
		/** fast linear interpolation (does not have so much precision) */
		stFastLinear = 1,
		/** (strict) linear interpolation */
		stLinear = 2,
		/** cubic interpolation */
		stCubic = 3,

		/** stretch type mask */
		stTypeMask = 0xf,
		/** flag mask */
		stFlagMask = 0xf0,

		/** referencing source is not limited by the given rectangle
		 * (may allow to see the border pixel to interpolate) */
		stRefNoClip = 0x10;

	static final public boolean isTypeUsingAlpha( int type ) {
		return
			type == ltAlpha				||
			type == ltPsNormal			||
			type == ltPsAdditive		||
			type == ltPsSubtractive		||
			type == ltPsMultiplicative	||
			type == ltPsScreen			||
			type == ltPsOverlay			||
			type == ltPsHardLight		||
			type == ltPsSoftLight		||
			type == ltPsColorDodge		||
			type == ltPsColorDodge5		||
			type == ltPsColorBurn		||
			type == ltPsLighten			||
			type == ltPsDarken			||
			type == ltPsDifference		||
			type == ltPsDifference5		||
			type == ltPsExclusion		;
	}

	static final public boolean isTypeUsingAddAlpha( int  type ) {
		return type == ltAddAlpha;
	}

	static final public boolean isTypeUsingAlphaChannel( int type ) {
		return isTypeUsingAddAlpha(type) || isTypeUsingAlpha(type);
	}
}
