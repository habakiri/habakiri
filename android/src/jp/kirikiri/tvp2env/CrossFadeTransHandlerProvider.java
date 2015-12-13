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
package jp.kirikiri.tvp2env;

import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tvp2.msg.Message;
import jp.kirikiri.tvp2.visual.BaseTransHandler;
import jp.kirikiri.tvp2.visual.SimpleImageProvider;
import jp.kirikiri.tvp2.visual.SimpleOptionProvider;
import jp.kirikiri.tvp2.visual.TransHandlerProvider;

public class CrossFadeTransHandlerProvider implements TransHandlerProvider {

	protected long[] mLogValueRet;
	protected static final String TIME = "time";

	public CrossFadeTransHandlerProvider() {
		mLogValueRet = new long[1];
	}

	@Override
	public String getName() { return "crossfade"; }

	@Override
	public BaseTransHandler startTransition(SimpleOptionProvider options,
			SimpleImageProvider imagepro, int layertype, int src1w, int src1h,
			int src2w, int src2h, int[] type) throws TJSException {

		if( type != null && type.length >= 1 ) type[0] = ttExchange; // transition type : exchange
		if( type != null && type.length >= 2 ) type[1] = tutDivisibleFade;
			// update type : divisible fade
		if( options == null ) return null;

		if(src1w != src2w || src1h != src2h)
			Message.throwExceptionMessage( Message.TransitionLayerSizeMismatch,
				 String.valueOf(src2w) + "x" + src2h,
				 String.valueOf(src1w) + "x" + src1h );

		return getTransitionObject(options, imagepro, layertype, src1w, src1h, src2w, src2h);
	}

	protected BaseTransHandler getTransitionObject( SimpleOptionProvider options,
			SimpleImageProvider imagepro, int layertype, int src1w, int src1h,
			int src2w, int src2h ) throws TJSException {

		int er = options.getAsNumber( TIME, mLogValueRet );
		long time = mLogValueRet[0];
		if( er < 0 ) Message.throwExceptionMessage( Message.SpecifyOption, TIME );
		if(time < 2) time = 2; // too small time may cause problem

		return new CrossFadeTransHandler( options, layertype, time );
	}
}
