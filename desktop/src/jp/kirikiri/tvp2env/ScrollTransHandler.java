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

import jp.kirikiri.tvp2.visual.DivisibleData;
import jp.kirikiri.tvp2.visual.Rect;
import jp.kirikiri.tvp2.visual.ScanLineProvider;
import jp.kirikiri.tvp2.visual.SimpleOptionProvider;

public class ScrollTransHandler extends CrossFadeTransHandler {

	public static final int sttLeft = 0, sttTop = 1, sttRight = 2, sttBottom = 3;
	public static final int ststNoStay = 0, ststStayDest = 1, ststStaySrc = 2;

	private int mFrom;
	private int mStay;

	public ScrollTransHandler(SimpleOptionProvider options, int layertype,
			long time, int from, int stay, int maxphase ) {
		super(options, layertype, time, maxphase );
		mFrom = from;
		mStay = stay;
	}

	@Override
	public void blend( DivisibleData data ) {
		final int from = mFrom;
		final int stay = mStay;
		final int phase = mPhase;
		final int imagewidth = data.Src1.getWidth();
		final int imageheight = data.Src1.getHeight();
		int src1left = 0;
		int src1top = 0;
		int src2left = 0;
		int src2top = 0;

		switch( from ) {
		case sttLeft:
			if( stay == ststNoStay ) {
				src1left = phase;
				src2left = phase - imagewidth;
			} else if( stay == ststStayDest ) {
				src2left = phase - imagewidth;
			} else if( stay == ststStaySrc ) {
				src1left = phase;
			}
			break;
		case sttTop:
			if( stay == ststNoStay ) {
				src1top = phase;
				src2top = phase - imageheight;
			} else if( stay == ststStayDest ) {
				src2top = phase - imageheight;
			} else if( stay == ststStaySrc ) {
				src1top = phase;
			}
			break;
		case sttRight:
			if( stay == ststNoStay ) {
				src1left = -phase;
				src2left = imagewidth - phase;
			} else if( stay == ststStayDest ) {
				src2left = imagewidth - phase;
			} else if( stay == ststStaySrc ) {
				src1left = -phase;
			}
			break;
		case sttBottom:
			if( stay == ststNoStay ) {
				src1top = -phase;
				src2top = imageheight - phase;
			} else if( stay == ststStayDest ) {
				src2top = imageheight - phase;
			} else if( stay == ststStaySrc ) {
				src1top = -phase;
			}
			break;
		}

		Rect rdest = new Rect(data.Left, data.Top, data.Width+data.Left, data.Height+data.Top);
		Rect rs1 = new Rect(src1left, src1top, imagewidth + src1left, imageheight + src1top);
		Rect rs2 = new Rect(src2left, src2top, imagewidth + src2left, imageheight + src2top);
		if( stay == ststNoStay ) {
			// both layers have no priority than another.
			// nothing to do.
		} else if( stay == ststStayDest ) {
			// Src2 has priority.
			if( from == sttLeft || from == sttRight ) {
				if(rs2.right >= rs1.left && rs2.right < rs1.right)
					rs1.left = rs2.right;
				if(rs2.left >= rs1.left && rs2.left < rs1.right)
					rs1.right = rs2.left;
			} else {
				if(rs2.bottom >= rs1.top && rs2.bottom < rs1.bottom)
					rs1.top = rs2.bottom;
				if(rs2.top >= rs1.top && rs2.top < rs1.bottom)
					rs1.bottom = rs2.top;
			}
		} else if( stay == ststStaySrc ) {
			// Src1 has priority.
			if( from == sttLeft || from == sttRight ) {
				if(rs1.right >= rs2.left && rs1.right < rs2.right)
					rs2.left = rs1.right;
				if(rs1.left >= rs2.left && rs1.left < rs2.right)
					rs2.right = rs1.left;
			} else {
				if(rs1.bottom >= rs2.top && rs1.bottom < rs2.bottom)
					rs2.top = rs1.bottom;
				if(rs1.top >= rs2.top && rs1.top < rs2.bottom)
					rs2.bottom = rs1.top;
			}
		}

		// copy to destination image
		Rect d = new Rect();
		if( Rect.intersectRect( d, rdest, rs1) ) {
			int dl = d.left - data.Left + data.DestLeft;
			int dt = d.top - data.Top + data.DestTop;
			d.addOffsets( -src1left, -src1top );
			copyRect( data.Dest, dl, dt, data.Src1, d );
		}
		if( Rect.intersectRect( d, rdest, rs2) ) {
			int dl = d.left - data.Left + data.DestLeft;
			int dt = d.top - data.Top + data.DestTop;
			d.addOffsets( -src2left, -src2top );
			copyRect( data.Dest, dl, dt, data.Src2, d );
		}
	}

	private void copyRect( ScanLineProvider destimg, int x, int y, ScanLineProvider srcimg, final Rect srcrect ) {
		// this function does not matter if the src==dest and copying area is overlapped.
		// destimg and srcimg must be 32bpp bitmap.

		NativeImageBuffer dest = destimg.getScanLineForWrite();
		NativeImageBuffer src = srcimg.getScanLine();
		dest.copyRect( x, y, src, srcrect );
	}
}
