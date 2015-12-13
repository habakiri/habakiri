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

import jp.kirikiri.tjs2.TJSException;

public interface TransHandlerProvider {

	/** transition using only one(self) layer ( eg. simple fading ) */
	public static final int ttSimple = 0;
	/** transition using two layer ( eg. cross fading ) */
	public static final int ttExchange = 1;


	/**
	 * used when the transition processing is region-divisible and
	 * the transition updates entire area of the layer.
	 * update area is always given by TransHandler.process caller.
	 * handler must use only given area of the source bitmap on each
	 * callbacking.
	 */
	public static final int tutDivisibleFade = 0;
	/**
	 * same as tutDivisibleFade, except for its usage of source area.
	 * handler can use any area of the source bitmap.
	 * this will somewhat slower than tutDivisibleFade.
	 */
	public static final int tutDivisible = 1;
	/**
	 * used when the transition processing is not region-divisible or
	 * the transition updates only some small regions rather than entire
	 * area.
	 * update area is given by callee of TransHandler.process,
	 * via LayerUpdater interface.
	 */
	public static final int tutGiveUpdate = 2;

	/**
	 * return this transition name
	 * @return
	 */
	public String getName();

	/**
	 * start transition and return a handler.
	 * "handler" is an object of iTVPDivisibleTransHandler when
	 * updatetype is tutDivisibleFade or tutDivisible.
	 * Otherwise is an object of iTVPGiveUpdateTransHandler ( cast to
	 * each class to use it )
	 * layertype is the destination layer type.
	 * @param options : option provider
	 * @param imagepro : image provider
	 * @param layertype : destination layer type
	 * @param src1w : source 1 width
	 * @param src1h : source 1 height
	 * @param src2w : source 2 width
	 * @param src2h : source 2 height
	 * @param type : [0]transition type, [1]update typwe
	 * @return  transition handler
	 * @throws TJSException
	 */
	public BaseTransHandler startTransition( SimpleOptionProvider options, SimpleImageProvider imagepro,
			int layertype, int src1w, int src1h, int src2w, int src2h, int[] type ) throws TJSException;
}
