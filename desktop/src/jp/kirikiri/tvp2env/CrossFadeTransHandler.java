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

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;

import jp.kirikiri.tjs2.Error;
import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tvp2.visual.DivisibleData;
import jp.kirikiri.tvp2.visual.DivisibleTransHandler;
import jp.kirikiri.tvp2.visual.LayerNI;
import jp.kirikiri.tvp2.visual.LayerType;
import jp.kirikiri.tvp2.visual.ScanLineProvider;
import jp.kirikiri.tvp2.visual.SimpleOptionProvider;

public class CrossFadeTransHandler implements DivisibleTransHandler {

	protected SimpleOptionProvider mOptions;
	protected int mDestLayerType;
	protected long mStartTick;
	protected long mTime; // time during transition
	protected boolean mFirst;

	protected int mPhaseMax;
	protected int mPhase; // current phase (0 thru PhaseMax)

	public CrossFadeTransHandler( SimpleOptionProvider options, int layertype, long time ) {
		this( options, layertype, time, 255 );
	}

	public CrossFadeTransHandler(SimpleOptionProvider options, int layertype, long time, int phasemax ) {
		mDestLayerType = layertype;
		mOptions = options;
		mTime = time;
		mPhaseMax = phasemax;
		mFirst = true;
	}

	@Override
	public int setOption(SimpleOptionProvider options) {
		mOptions = null;
		mOptions = options;
		return Error.S_OK;
	}

	@Override
	public int startProcess(long tick) {
		// notifies starting of the update
		if( mFirst ) {
			mFirst = false;
			mStartTick = tick;
		}

		// compute phase ( 0 thru 255 )
		mPhase = (int) ((tick - mStartTick ) * mPhaseMax / mTime);
		if( mPhase >= mPhaseMax ) mPhase = mPhaseMax;
		return Error.S_TRUE;
	}

	@Override
	public int endProcess() {
		if( mPhase >= mPhaseMax ) return Error.S_FALSE;
		return Error.S_TRUE;
	}

	@Override
	public int process( DivisibleData data ) {
		if( mPhase == 0 ) {
			// completely source 1
			data.Dest = data.Src1;
			data.DestLeft = data.Src1Left;
			data.DestTop = data.Src1Top;
		} else if( mPhase == mPhaseMax ) {
			// completety source 2
			data.Dest = data.Src2;
			data.DestLeft = data.Src2Left;
			data.DestTop = data.Src2Top;
		} else {
			try {
				blend(data);
			} catch( Exception e ) {
				e.printStackTrace();
			}
		}
		return Error.S_OK;
	}

	@Override
	public int makeFinalImage( ScanLineProvider dest, ScanLineProvider src1, ScanLineProvider src2 ) throws TJSException {
		// final image is the source2 bitmap
		dest.copyFrom( src2 );
		return Error.S_OK;
	}

	public void blend( DivisibleData data ) {
		BufferedImage dest = (BufferedImage)data.Dest.getScanLineForWrite().getImage();
		BufferedImage src1 = (BufferedImage)data.Src1.getScanLine().getImage();
		BufferedImage src2 = (BufferedImage)data.Src2.getScanLine().getImage();
		int destLeft = data.DestLeft;
		int src1Left = data.Src1Left;
		int src2Left = data.Src2Left;
		int h = data.Height;
		int w = data.Width;
		int destTop = data.DestTop;
		int src1Top = data.Src1Top;
		int src2Top = data.Src2Top;


		if( LayerType.isTypeUsingAlpha(mDestLayerType) ) {
			DataBuffer src1buf = src1.getRaster().getDataBuffer();
			DataBuffer src2buf = src2.getRaster().getDataBuffer();
			DataBuffer destbuf = dest.getRaster().getDataBuffer();
			final int destType = destbuf.getDataType();
			final int src1Type = src1buf.getDataType();
			final int src2Type = src2buf.getDataType();
			if( destType == DataBuffer.TYPE_INT && src1Type == DataBuffer.TYPE_INT && src2Type == DataBuffer.TYPE_INT ) {
				byte[] table = CustomOperationComposite.OpacityOnOpacityTable;
				int opa = mPhase;
				if(opa > 127) opa++; // adjust for error
				int iopa = 256 - opa;
				int[] s1 = ((DataBufferInt)src1buf).getData();
				int[] s2 = ((DataBufferInt)src2buf).getData();
				int[] d = ((DataBufferInt)destbuf).getData();

				final int destW = dest.getWidth();
				final int src1W = src1.getWidth();
				final int src2W = src2.getWidth();
				destTop = destTop * destW + destLeft;
				src1Top = src1Top * src1W + src1Left;
				src2Top = src2Top * src2W + src2Left;
				for( int y = 0; y < h; y++ ) {
					for( int x = 0; x < w; x++ ) {
						int s1v = s1[src1Top+x];
						int s2v = s2[src1Top+x];
						int a1 = s1v >>> 24;
						int a2 = s2v >>> 24;
						int addr = (a2*opa & 0xff00) + (a1*iopa >>> 8);
						int alpha = table[addr] & 0xff;
						int s1t = s1v & 0xff00ff;
						s1t = ((s1t + (((s2v & 0xff00ff) - s1t) * alpha >>> 8)) & 0xff00ff);
						s1v &= 0xff00;
						s2v &= 0xff00;
						s1t |= (a1 + ((a2 - a1)*opa >>> 8)) << 24;
						int color = s1t | ((s1v + ((s2v - s1v) * alpha >>> 8)) & 0xff00);
						d[destTop+x] = color;
					}
					destTop += destW;
					src1Top += src1W;
					src2Top += src2W;
				}
			}
		} else if( LayerType.isTypeUsingAddAlpha(mDestLayerType) ) {
			DataBuffer src1buf = src1.getRaster().getDataBuffer();
			DataBuffer src2buf = src2.getRaster().getDataBuffer();
			DataBuffer destbuf = dest.getRaster().getDataBuffer();
			final int destType = destbuf.getDataType();
			final int src1Type = src1buf.getDataType();
			final int src2Type = src2buf.getDataType();
			if( destType == DataBuffer.TYPE_INT && src1Type == DataBuffer.TYPE_INT && src2Type == DataBuffer.TYPE_INT ) {
				int opa = mPhase;
				int[] s1 = ((DataBufferInt)src1buf).getData();
				int[] s2 = ((DataBufferInt)src2buf).getData();
				int[] d = ((DataBufferInt)destbuf).getData();

				final int destW = dest.getWidth();
				final int src1W = src1.getWidth();
				final int src2W = src2.getWidth();
				destTop = destTop * destW + destLeft;
				src1Top = src1Top * src1W + src1Left;
				src2Top = src2Top * src2W + src2Left;
				for( int y = 0; y < h; y++ ) {
					for( int x = 0; x < w; x++ ) {
						int b = s1[src1Top+x];
						int a = s2[src1Top+x];
						/* dest = a * ratio + b * (1 - ratio) */
						int b2 = b & 0x00ff00ff;
						int t = (b2 + (((a & 0x00ff00ff) - b2) * opa >>> 8)) & 0x00ff00ff;
						b2 = (b & 0xff00ff00) >>> 8;
						d[destTop+x] = t +  (((b2 + (( ((a & 0xff00ff00) >>> 8) - b2) * opa >>> 8)) << 8)& 0xff00ff00);
					}
					destTop += destW;
					src1Top += src1W;
					src2Top += src2W;
				}
			}
		} else {
			float opa = ((float)mPhase)/255.0f;
			if( dest == src1 && destLeft == src1Left && destTop == src1Top) {
				Composite composite = AlphaComposite.getInstance( AlphaComposite.SRC_OVER, opa );
				Graphics2D g = (Graphics2D)dest.getGraphics();
				g.setComposite( composite );
				g.drawImage( src2, destLeft, destTop, destLeft+w, destTop+h, src2Left, src2Top, src2Left+w, src2Top+h, null );
				g.dispose();
			} else {
				Composite composite = AlphaComposite.getInstance( AlphaComposite.SRC_OVER, opa );
				Graphics2D g = (Graphics2D)dest.getGraphics();
				g.setComposite( AlphaComposite.Src );
				g.drawImage( src1, destLeft, destTop, destLeft+w, destTop+h, src1Left, src1Top, src1Left+w, src1Top+h, null );

				g.setComposite( composite );
				g.drawImage( src2, destLeft, destTop, destLeft+w, destTop+h, src2Left, src2Top, src2Left+w, src2Top+h, null );
				g.dispose();
			}
		}

		/*
		if( LayerType.isTypeUsingAlpha(mDestLayerType) ) {
			if(opa > 127) opa++; // adjust for error
			int iopa = 256 - opa;
			if( destType == DataBuffer.TYPE_INT && src1Type == DataBuffer.TYPE_INT && src2Type == DataBuffer.TYPE_INT ) {
				int[] s1 = ((DataBufferInt)src1Buff).getData();
				int[] s2 = ((DataBufferInt)src2Buff).getData();
				int[] d = ((DataBufferInt)destBuff).getData();

				destTop = destTop * destW + destLeft;
				src1Top = src1Top * src1W + src1Left;
				src2Top = src2Top * src2W + src2Left;
				for( int y = 0; y < h; y++ ) {
					for( int x = 0; x < w; x++ ) {
						int s1v = s1[src1Top+x];
						int s2v = s2[src1Top+x];
						int a1 = s1v >>> 24;
						int a2 = s2v >>> 24;
						int s1t = s1v & 0xff00ff;
						s1t = ((s1t + (((s2v & 0xff00ff) - s1t) * alpha >> 8)) & 0xff00ff);
						s1v &= 0xff00;
						s2v &= 0xff00;
						s1t |= (a1 + ((a2 - a1)*opa >> 8)) << 24;
						d[destTop+x] = s1t | ((s1v + ((s2v - s1v) * alpha >> 8)) & 0xff00);
					}
					destTop += destW;
					src1Top += src1W;
					src2Top += src2W;
				}
			}
		} else if( LayerType.isTypeUsingAddAlpha(mDestLayerType) ) {
		} else {
			int destW = dest.getWidth();
			int src1W = src1.getWidth();
			int src2W = src2.getWidth();

			DataBuffer destBuff = dest.getRaster().getDataBuffer();
			final int destType = destBuff.getDataType();

			DataBuffer src1Buff = src1.getRaster().getDataBuffer();
			final int src1Type = src1Buff.getDataType();
			DataBuffer src2Buff = src2.getRaster().getDataBuffer();
			final int src2Type = src2Buff.getDataType();
			int opa = mPhase;
			if( destType == DataBuffer.TYPE_INT && src1Type == DataBuffer.TYPE_INT && src2Type == DataBuffer.TYPE_INT ) {
				int[] s1 = ((DataBufferInt)src1Buff).getData();
				int[] s2 = ((DataBufferInt)src2Buff).getData();
				int[] d = ((DataBufferInt)destBuff).getData();

				destTop = destTop * destW + destLeft;
				src1Top = src1Top * src1W + src1Left;
				src2Top = src2Top * src2W + src2Left;
				for( int y = 0; y < h; y++ ) {
					for( int x = 0; x < w; x++ ) {
						int s1v = s1[src1Top+x];
						int s2v = s2[src1Top+x];
						int s1t = s1v & 0xff00ff;
						s1t = ((s1t + (((s2v & 0xff00ff) - s1t) * opa >> 8)) & 0xff00ff);
						s1v &= 0xff00;
						s2v &= 0xff00;
						d[destTop+x] = s1t | ((s1v + ((s2v - s1v) * opa >> 8)) & 0xff00);
					}
					destTop += destW;
					src1Top += src1W;
					src2Top += src2W;
				}
			}
		}
		*/
	}
}
