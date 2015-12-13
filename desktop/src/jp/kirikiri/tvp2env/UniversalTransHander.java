/**
 ******************************************************************************
 * Copyright (c), Takenori Imoto
 * 楓 software http://www.kaede-software.com/
 * All rights reserved.
 ******************************************************************************
 * ソースコード形式かバイナリ形式か、変更するかしないかを問わず、以下の条件を満
 * たす場合に限り、再頒布および使用が許可されます。
 *
 * ・ソースコードを再頒布する場合、上記の著作権表示、本条件一覧、および下記免責
 *   条項を含めること。
 * ・バイナリ形式で再頒布する場合、頒布物に付属のドキュメント等の資料に、上記の
 *   著作権表示、本条件一覧、および下記免責条項を含めること。
 * ・書面による特別の許可なしに、本ソフトウェアから派生した製品の宣伝または販売
 *   促進に、組織の名前またはコントリビューターの名前を使用してはならない。
 *
 * 本ソフトウェアは、著作権者およびコントリビューターによって「現状のまま」提供
 * されており、明示黙示を問わず、商業的な使用可能性、および特定の目的に対する適
 * 合性に関する暗黙の保証も含め、またそれに限定されない、いかなる保証もありませ
 * ん。著作権者もコントリビューターも、事由のいかんを問わず、損害発生の原因いか
 * んを問わず、かつ責任の根拠が契約であるか厳格責任であるか（過失その他の）不法
 * 行為であるかを問わず、仮にそのような損害が発生する可能性を知らされていたとし
 * ても、本ソフトウェアの使用によって発生した（代替品または代用サービスの調達、
 * 使用の喪失、データの喪失、利益の喪失、業務の中断も含め、またそれに限定されな
 * い）直接損害、間接損害、偶発的な損害、特別損害、懲罰的損害、または結果損害に
 * ついて、一切責任を負わないものとします。
 ******************************************************************************
 * 本ソフトウェアは、吉里吉里2 ( http://kikyou.info/tvp/ ) のソースコードをJava
 * に書き換えたものを一部使用しています。
 * 吉里吉里2 Copyright (C) W.Dee <dee@kikyou.info> and contributors
 ******************************************************************************
 */
package jp.kirikiri.tvp2env;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;

import jp.kirikiri.tvp2.visual.DivisibleData;
import jp.kirikiri.tvp2.visual.LayerType;
import jp.kirikiri.tvp2.visual.ScanLineProvider;
import jp.kirikiri.tvp2.visual.SimpleOptionProvider;

public class UniversalTransHander extends CrossFadeTransHandler {

	private static final int TABLE_SIZE = 256;

	private int mVague;
	private ScanLineProvider mRule;
	private int[] mBlendTable;

	public UniversalTransHander(SimpleOptionProvider options, int layertype, long time, int vague, ScanLineProvider rule) {
		super(options, layertype, time, 255+vague );
		mBlendTable = new int[TABLE_SIZE];
		mVague = vague;
		mRule = rule;
	}

	@Override
	public int startProcess( long tick) {
		int er = super.startProcess(tick);
		if( er < 0 ) return er;
		// start one frame of the transition

		// create blend table
		/*
		if( LayerType.isTypeUsingAlpha(mDestLayerType))
			initUnivTransBlendTable_d(mBlendTable, mPhase, mVague);
		else if(LayerType.isTypeUsingAddAlpha(mDestLayerType))
			initUnivTransBlendTable_a(mBlendTable, mPhase, mVague);
		else
			initUnivTransBlendTable(mBlendTable, mPhase, mVague);
		*/ // 生成はすべて同じ内容だったので統一
		initUnivTransBlendTable(mBlendTable, mPhase, mVague);

		return er;
	}

	@Override
	public void blend( DivisibleData data ) {
		BufferedImage dest = (BufferedImage)data.Dest.getScanLineForWrite().getImage();
		BufferedImage src1 = (BufferedImage)data.Src1.getScanLine().getImage();
		BufferedImage src2 = (BufferedImage)data.Src2.getScanLine().getImage();
		BufferedImage rule = (BufferedImage)mRule.getScanLine().getImage();
		int destLeft = data.DestLeft;
		int src1Left = data.Src1Left;
		int src2Left = data.Src2Left;
		int ruleLeft = data.Left;
		int h = data.Height;
		int w = data.Width;
		int destTop = data.DestTop;
		int src1Top = data.Src1Top;
		int src2Top = data.Src2Top;
		int ruleTop = data.Top;

		int destW = dest.getWidth();
		int src1W = src1.getWidth();
		int src2W = src2.getWidth();
		int ruleW = rule.getWidth();

		DataBuffer destBuff = dest.getRaster().getDataBuffer();
		final int destType = destBuff.getDataType();

		DataBuffer src1Buff = src1.getRaster().getDataBuffer();
		final int src1Type = src1Buff.getDataType();
		DataBuffer src2Buff = src2.getRaster().getDataBuffer();
		final int src2Type = src2Buff.getDataType();

		DataBuffer ruleBuff = rule.getRaster().getDataBuffer();
		final int ruleType = ruleBuff.getDataType();
		final int[] table = mBlendTable;
		if( destType == DataBuffer.TYPE_INT && src1Type == DataBuffer.TYPE_INT &&
				src2Type == DataBuffer.TYPE_INT && ruleType  == DataBuffer.TYPE_BYTE ) {
			int[] s1 = ((DataBufferInt)src1Buff).getData();
			int[] s2 = ((DataBufferInt)src2Buff).getData();
			int[] d = ((DataBufferInt)destBuff).getData();
			byte[] r = ((DataBufferByte)ruleBuff).getData();

			destTop = destTop * destW + destLeft;
			src1Top = src1Top * src1W + src1Left;
			src2Top = src2Top * src2W + src2Left;
			ruleTop = ruleTop * ruleW + ruleLeft;
			if( mVague >= 512 ) {
				if( LayerType.isTypeUsingAlpha(mDestLayerType)) {
					final byte[] opacitytable = CustomOperationComposite.OpacityOnOpacityTable;
					for( int y = 0; y < h; y++ ) {
						for( int x = 0; x < w; x++ ) {
							int s1v = s1[src1Top+x];
							int s2v = s2[src1Top+x];
							int ru = r[ruleTop+x] & 0xff;
							int opa = table[ru];
							int a1 = s1v >>> 24;
							int a2 = s2v >>> 24;
							int addr = (a2*opa & 0xff00) + (a1*(256-opa) >>> 8 );
							int alpha = opacitytable[addr] & 0xff;
							int s1t = s1v & 0xff00ff;
							s1t = ((s1t + (((s2v & 0xff00ff) - s1t) * alpha >>> 8)) & 0xff00ff);
							s1v &= 0xff00;
							s2v &= 0xff00;
							s1t |= (a1 + ((a2 - a1)*opa >>> 8)) << 24;
							d[destTop+x] = s1t | ((s1v + ((s2v - s1v) * alpha >>> 8)) & 0xff00);
						}
						destTop += destW;
						src1Top += src1W;
						src2Top += src2W;
						ruleTop += ruleW;
					}
				} else if(LayerType.isTypeUsingAddAlpha(mDestLayerType)) {
					for( int y = 0; y < h; y++ ) {
						for( int x = 0; x < w; x++ ) {
							int s1v = s1[src1Top+x];
							int s2v = s2[src1Top+x];
							int ru = r[ruleTop+x] & 0xff;
							int opa = table[ru];

							// TVPBlendARGB(b,a,rate) returns a * ratio + b * (1 - ratio)
							int b2 = s1v & 0x00ff00ff;
							int t = (b2 + (((s2v & 0x00ff00ff) - b2) * opa >>> 8)) & 0x00ff00ff;
							b2 = (s1v & 0xff00ff00) >>> 8;
							d[destTop+x] = t + (((b2 + (( ((s2v & 0xff00ff00) >>> 8) - b2) * opa >>> 8)) << 8)& 0xff00ff00);
							// TVPBlendARGB
						}
						destTop += destW;
						src1Top += src1W;
						src2Top += src2W;
						ruleTop += ruleW;
					}
				} else {
					for( int y = 0; y < h; y++ ) {
						for( int x = 0; x < w; x++ ) {
							int s1v = s1[src1Top+x];
							int s2v = s2[src1Top+x];
							int ru = r[ruleTop+x] & 0xff;
							int opa = table[ru];
							int s1t = s1v & 0xff00ff;
							s1t = ((s1t + (((s2v & 0xff00ff) - s1t) * opa >>> 8)) & 0xff00ff);
							s1v &= 0xff00;
							s2v &= 0xff00;
							d[destTop+x] = s1t | ((s1v + ((s2v - s1v) * opa >>> 8)) & 0xff00) | 0xff000000;
						}
						destTop += destW;
						src1Top += src1W;
						src2Top += src2W;
						ruleTop += ruleW;
					}
				}
			} else {
				int src1lv = mPhase;
				int src2lv = mPhase - mVague;
				if( LayerType.isTypeUsingAlpha(mDestLayerType)) {
					final byte[] opacitytable = CustomOperationComposite.OpacityOnOpacityTable;
					final byte[] multable = CustomOperationComposite.NegativeMulTable;
					for( int y = 0; y < h; y++ ) {
						for( int x = 0; x < w; x++ ) {
							int ru = r[ruleTop+x] & 0xff;
							if( ru >= src1lv ) {
								d[destTop+x] = s1[src1Top+x];
							} else if( ru < src2lv ) {
								d[destTop+x] = s2[src1Top+x];
							} else {
								int s1v = s1[src1Top+x];
								int s2v = s2[src1Top+x];
								int opa = table[ru];
								int a1 = s1v >>> 24;
								int a2 = s2v >>> 24;
								int addr = (a2*opa & 0xff00) + (a1*(256-opa) >>> 8 );
								int alpha = opacitytable[addr];
								int s1t = s1v & 0xff00ff;
								s1t = ((s1t + (((s2v & 0xff00ff) - s1t) * alpha >>> 8)) & 0xff00ff) + (multable[addr]<<24);
								s1v &= 0xff00;
								s2v &= 0xff00;
								d[destTop+x] = s1t | ((s1v + ((s2v - s1v) * alpha >>> 8)) & 0xff00);
							}
						}
						destTop += destW;
						src1Top += src1W;
						src2Top += src2W;
						ruleTop += ruleW;
					}
				} else if(LayerType.isTypeUsingAddAlpha(mDestLayerType)) {
					for( int y = 0; y < h; y++ ) {
						for( int x = 0; x < w; x++ ) {
							int ru = r[ruleTop+x] & 0xff;
							if( ru >= src1lv ) {
								d[destTop+x] = s1[src1Top+x];
							} else if( ru < src2lv ) {
								d[destTop+x] = s2[src1Top+x];
							} else {
								int s1v = s1[src1Top+x];
								int s2v = s2[src1Top+x];
								int opa = table[ru];

								// TVPBlendARGB(b,a,rate) returns a * ratio + b * (1 - ratio)
								int b2 = s1v & 0x00ff00ff;
								int t = (b2 + (((s2v & 0x00ff00ff) - b2) * opa >>> 8)) & 0x00ff00ff;
								b2 = (s1v & 0xff00ff00) >>> 8;
								d[destTop+x] = t + (((b2 + (( ((s2v & 0xff00ff00) >>> 8) - b2) * opa >>> 8)) << 8)& 0xff00ff00);
								// TVPBlendARGB
							}
						}
						destTop += destW;
						src1Top += src1W;
						src2Top += src2W;
						ruleTop += ruleW;
					}
				} else {
					for( int y = 0; y < h; y++ ) {
						for( int x = 0; x < w; x++ ) {
							int ru = r[ruleTop+x] & 0xff;
							if( ru >= src1lv ) {
								d[destTop+x] = s1[src1Top+x];
							} else if( ru < src2lv ) {
								d[destTop+x] = s2[src1Top+x];
							} else {
								int s1v = s1[src1Top+x];
								int s2v = s2[src1Top+x];
								int opa = table[ru];
								int s1t = s1v & 0xff00ff;
								s1t = ((s1t + (((s2v & 0xff00ff) - s1t) * opa >>> 8)) & 0xff00ff);
								s1v &= 0xff00;
								s2v &= 0xff00;
								d[destTop+x] = s1t | ((s1v + ((s2v - s1v) * opa >>> 8)) & 0xff00) | 0xff000000;
							}
						}
						destTop += destW;
						src1Top += src1W;
						src2Top += src2W;
						ruleTop += ruleW;
					}
				}
			}
		}
	}


	private void initUnivTransBlendTable(int[] table, int phase, int vague) {
		int phasemax = phase;
		phase -= vague;
		if( vague == 0 ) vague = 1; // 0除算抑止
		for( int i = 0; i < TABLE_SIZE; i++ ) {
			if( i < phase ) table[i] = 255;
			else if( i >= phasemax ) table[i] = 0;
			else {
				int tmp = (255-(( i - phase )*255 / vague));
				if( tmp < 0 ) tmp = 0;
				if( tmp > 255 ) tmp = 255;
				table[i] = tmp;
			}
		}
	}
}
