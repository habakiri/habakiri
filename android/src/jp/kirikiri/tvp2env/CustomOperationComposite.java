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

import java.nio.IntBuffer;

import jp.kirikiri.tvp2.visual.Rect;

import android.graphics.Bitmap;


public class CustomOperationComposite {
	public static byte[] OpacityOnOpacityTable;
	public static byte[] NegativeMulTable;
	public static int[] RecipTable256;
	public static byte[][] PsTableSoftLight;
	public static byte[][] PsTableColorDodge;
	public static byte[][] PsTableColorBurn;

	static public void initialize() {
		OpacityOnOpacityTable = NativeImageBuffer.OpacityOnOpacityTable;
		NegativeMulTable = NativeImageBuffer.NegativeMulTable;
		RecipTable256 = NativeImageBuffer.RecipTable256;
		PsTableSoftLight = null;
		PsTableColorDodge = null;
		PsTableColorBurn = null;
	}

	public static void finalizeApplication() {
		OpacityOnOpacityTable = null;
		NegativeMulTable = null;
		PsTableSoftLight = null;
		PsTableColorDodge = null;
		PsTableColorBurn = null;
	}
	static private void createTables() {
		// create ps tables
		PsTableSoftLight = new byte[256][];
		PsTableColorDodge = new byte[256][];
		PsTableColorBurn = new byte[256][];
		for( int s = 0; s < 256; s++ ) {
			PsTableSoftLight[s] = new byte[256];
			PsTableColorDodge[s] = new byte[256];
			PsTableColorBurn[s] = new byte[256];
			for( int d = 0; d < 256; d++ ) {
				PsTableSoftLight[s][d]  = (s>=128) ?
						(byte)( ((int)(Math.pow(d/255.0, 128.0/s)*255.0))&0xff ) :
						(byte)( ((int)(Math.pow(d/255.0, (1.0-s/255.0)/0.5)*255.0))&0xff );
				PsTableColorDodge[s][d] = ((255-s)<=d) ? (byte)(0xff) : (byte)(((int)((d*255)/(255-s)))&0xff);
				PsTableColorBurn[s][d]  = (s<=(255-d)) ? (byte)(0x00) : (byte)((int)(255-((255-d)*255)/s)&0xff);
			}
		}
	}
	static private final int
		bmCopy = 0,
		bmCopyOnAlpha = 1,
		bmAlpha = 2,
		bmAlphaOnAlpha = 3,
		bmAdd = 4,
		bmSub = 5,
		bmMul = 6,
		bmDodge = 7,
		bmDarken = 8,
		bmLighten = 9,
		bmScreen = 10,
		bmAddAlpha = 11,
		bmAddAlphaOnAddAlpha = 12,
		bmAddAlphaOnAlpha = 13,
		bmAlphaOnAddAlpha = 14,
		bmCopyOnAddAlpha = 15,
		bmPsNormal = 16,
		bmPsAdditive = 17,
		bmPsSubtractive = 18,
		bmPsMultiplicative = 19,
		bmPsScreen = 20,
		bmPsOverlay = 21,
		bmPsHardLight = 22,
		bmPsSoftLight = 23,
		bmPsColorDodge = 24,
		bmPsColorDodge5 = 25,
		bmPsColorBurn = 26,
		bmPsLighten = 27,
		bmPsDarken = 28,
		bmPsDifference = 29,
		bmPsDifference5 = 30,
		bmPsExclusion = 31;

	static public final int
		copyMain = -1,
		copyMask = -2;

	static public void blend( Bitmap dst, Rect drect, Bitmap src, Rect srect, int method, int opa, boolean hda ) {

		final int sw = src.getRowBytes()>>>2;
		final int sh = src.getHeight();
		int length = sw*sh;
		int[] s = new int[length];;
		IntBuffer buff = IntBuffer.wrap(s);
		src.copyPixelsToBuffer(buff);

		final int dw = dst.getRowBytes()>>>2;
		final int dh = dst.getHeight();
		length = dw*dh;
		int[] d = new int [length];
		buff = IntBuffer.wrap(d);
		dst.copyPixelsToBuffer(buff);

		int sx = srect.left;
		int sy = srect.top;
		if( sx < 0 ) sx = -sx;
		if( sy < 0 ) sy = -sy;
		int srcStride = sw;

		int dx = drect.left;
		int dy = drect.top;
		int dstStride = dw;

		int st = sx + sy*srcStride;
		int dt = dx + dy*dstStride;

		switch( method ) {
		case bmCopy:
			if( opa == 255 && hda ) { // CopyColor
				copyColor( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
			} else if( !hda ) { // ConstAlphaBlend
				constAlphaBlend( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
			} else { // ConstAlphaBlend_HDA
				constAlphaBlend_HDA( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
			}
			break;
		case bmCopyOnAlpha:
			if( opa == 255 ) { // CopyOpaqueImage
				copyOpaqueImage( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
			} else { // ConstAlphaBlend_d
				constAlphaBlend_d( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
			}
			break;
		case bmAlpha:
			if( opa == 255 ) {
				if( !hda ) { // AlphaBlend
					alphaBlend( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				} else { // AlphaBlend_HDA
					alphaBlend_HDA( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				}
			} else {
				if( !hda ) { // AlphaBlend_o
					alphaBlend_o( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				} else { // AlphaBlend_HDA_o
					alphaBlend_HDA_o( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				}
			}
			break;
		case bmAlphaOnAlpha:
			if( opa == 255 ) { // AlphaBlend_d
				alphaBlend_d( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
			} else { // AlphaBlend_do
				alphaBlend_do( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
			}
			break;
		case bmAdd:
			if( opa == 255 ) {
				if( !hda ) { // AddBlend
					addBlend( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				} else { // AddBlend_HDA
					addBlend_HDA( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				}
			} else {
				if( !hda ) { // AddBlend_o
					addBlend_o( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				} else { // AddBlend_HDA_o
					addBlend_HDA_o( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				}
			}
			break;
		case bmSub:
			if( opa == 255 ) {
				if( !hda ) { // SubBlend
					subBlend( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				} else { // SubBlend_HDA
					subBlend_HDA( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				}
			} else {
				if( !hda ) { // SubBlend_o
					subBlend_o( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				} else { // SubBlend_HDA_o
					subBlend_HDA_o( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				}
			}
			break;
		case bmMul:
			if( opa == 255 ) {
				if( !hda ) { // MulBlend
					mulBlend( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				} else { // MulBlend_HDA
					mulBlend_HDA( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				}
			} else {
				if( !hda ) { // MulBlend_o
					mulBlend_o( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				} else { // MulBlend_HDA_o
					mulBlend_HDA_o( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				}
			}
			break;
		case bmDodge:
			if( opa == 255 ) {
				if( !hda ) { // ColorDodgeBlend
					colorDodgeBlend( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				} else { // ColorDodgeBlend_HDA
					colorDodgeBlend_HDA( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				}
			} else {
				if( !hda ) { // ColorDodgeBlend_o
					colorDodgeBlend_o( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				} else { // ColorDodgeBlend_HDA_o
					colorDodgeBlend_HDA_o( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				}
			}
			break;
		case bmDarken:
			if( opa == 255 ) {
				if( !hda ) { // DarkenBlend
					darkenBlend( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				} else { // DarkenBlend_HDA
					darkenBlend_HDA( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				}
			} else {
				if( !hda ) { // DarkenBlend_o
					darkenBlend_o( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				} else { // DarkenBlend_HDA_o
					darkenBlend_HDA_o( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				}
			}
			break;
		case bmLighten:
			if( opa == 255 ) {
				if( !hda ) { // LightenBlend
					lightenBlend( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				} else { // LightenBlend_HDA
					lightenBlend_HDA( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				}
			} else {
				if( !hda ) { // LightenBlend_o
					lightenBlend_o( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				} else { // LightenBlend_HDA_o
					lightenBlend_HDA_o( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				}
			}
			break;
		case bmScreen:
			if( opa == 255 ) {
				if( !hda ) { // ScreenBlend
					screenBlend( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				} else { // ScreenBlend_HDA
					screenBlend_HDA( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				}
			} else {
				if( !hda ) { // ScreenBlend_o
					screenBlend_o( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				} else { // ScreenBlend_HDA_o
					screenBlend_HDA_o( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				}
			}
			break;
		case bmAddAlpha:
			if( opa == 255 ) {
				if( !hda ) { // AdditiveAlphaBlend
					additiveAlphaBlend( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				} else { // AdditiveAlphaBlend_HDA
					additiveAlphaBlend_HDA( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				}
			} else {
				if( !hda ) { // AdditiveAlphaBlend_o
					additiveAlphaBlend_o( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				} else { // AdditiveAlphaBlend_HDA_o
					additiveAlphaBlend_HDA_o( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				}
			}
			break;
		case bmAddAlphaOnAddAlpha:
			if( opa == 255 ) { // AdditiveAlphaBlend_a
				additiveAlphaBlend_a( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
			} else { // AdditiveAlphaBlend_ao
				additiveAlphaBlend_ao( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
			}
			break;
		case bmAddAlphaOnAlpha:
			// additive alpha on simple alpha
			// Not yet implemented
			break;
		case bmAlphaOnAddAlpha:
			if( opa == 255 ) { // AlphaBlend_a
				alphaBlend_a( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
			} else { // AlphaBlend_ao
				alphaBlend_ao( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
			}
			break;
		case bmCopyOnAddAlpha:
			if( opa == 255 ) { // CopyOpaqueImage
				copyOpaqueImage( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
			} else { // ConstAlphaBlend_a
				constAlphaBlend_a( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
			}
			break;
		case bmPsNormal:
			if( opa == 255 ) {
				if( !hda ) { // PsAlphaBlend
					psAlphaBlend( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				} else { // PsAlphaBlend_HDA
					psAlphaBlend_HDA( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				}
			} else {
				if( !hda ) { // PsAlphaBlend_o
					psAlphaBlend_o( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				} else { // PsAlphaBlend_HDA_o
					psAlphaBlend_HDA_o( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				}
			}
			break;
		case bmPsAdditive:
			if( opa == 255 ) {
				if( !hda ) { // PsAddBlend
					psAddBlend( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				} else { // PsAddBlend_HDA
					psAddBlend_HDA( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				}
			} else {
				if( !hda ) { // PsAddBlend_o
					psAddBlend_o( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				} else { // PsAddBlend_HDA_o
					psAddBlend_HDA_o( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				}
			}
			break;
		case bmPsSubtractive:
			if( opa == 255 ) {
				if( !hda ) { // PsSubBlend
					psSubBlend( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				} else { // PsSubBlend_HDA
					psSubBlend_HDA( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				}
			} else {
				if( !hda ) { // PsSubBlend_o
					psSubBlend_o( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				} else { // PsSubBlend_HDA_o
					psSubBlend_HDA_o( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				}
			}
			break;
		case bmPsMultiplicative:
			if( opa == 255 ) {
				if( !hda ) { // PsMulBlend
					psMulBlend( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				} else { // PsMulBlend_HDA
					psMulBlend_HDA( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				}
			} else {
				if( !hda ) { // PsMulBlend_o
					psMulBlend_o( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				} else { // PsMulBlend_HDA_o
					psMulBlend_HDA_o( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				}
			}
			break;
		case bmPsScreen:
			if( opa == 255 ) {
				if( !hda ) { // PsScreenBlend
					psScreenBlend( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				} else { // PsScreenBlend_HDA
					psScreenBlend_HDA( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				}
			} else {
				if( !hda ) { // PsScreenBlend_o
					psScreenBlend_o( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				} else { // PsScreenBlend_HDA_o
					psScreenBlend_HDA_o( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				}
			}
			break;
		case bmPsOverlay:
			if( opa == 255 ) {
				if( !hda ) { // PsOverlayBlend
					psOverlayBlend( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				} else { // PsOverlayBlend_HDA
					psOverlayBlend_HDA( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				}
			} else {
				if( !hda ) { // PsOverlayBlend_o
					psOverlayBlend_o( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				} else { // PsOverlayBlend_HDA_o
					psOverlayBlend_HDA_o( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				}
			}
			break;
		case bmPsHardLight:
			if( opa == 255 ) {
				if( !hda ) { // PsHardLightBlend
					psHardLightBlend( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				} else { // PsHardLightBlend_HDA
					psHardLightBlend_HDA( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				}
			} else {
				if( !hda ) { // PsHardLightBlend_o
					psHardLightBlend_o( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				} else { // PsHardLightBlend_HDA_o
					psHardLightBlend_HDA_o( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				}
			}
			break;
		case bmPsSoftLight:
			if( opa == 255 ) {
				if( !hda ) { // PsSoftLightBlend
					psSoftLightBlend( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				} else { // PsSoftLightBlend_HDA
					psSoftLightBlend_HDA( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				}
			} else {
				if( !hda ) { // PsSoftLightBlend_o
					psSoftLightBlend_o( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				} else { // PsSoftLightBlend_HDA_o
					psSoftLightBlend_HDA_o( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				}
			}
			break;
		case bmPsColorDodge:
			if( opa == 255 ) {
				if( !hda ) { // PsColorDodgeBlend
					psColorDodgeBlend( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				} else { // PsColorDodgeBlend_HDA
					psColorDodgeBlend_HDA( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				}
			} else {
				if( !hda ) { // PsColorDodgeBlend_o
					psColorDodgeBlend_o( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				} else { // PsColorDodgeBlend_HDA_o
					psColorDodgeBlend_HDA_o( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				}
			}
			break;
		case bmPsColorDodge5:
			if( opa == 255 ) {
				if( !hda ) { // PsColorDodge5Blend
					psColorDodge5Blend( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				} else { // PsColorDodge5Blend_HDA
					psColorDodge5Blend_HDA( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				}
			} else {
				if( !hda ) { // PsColorDodge5Blend_o
					psColorDodge5Blend_o( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				} else { // PsColorDodge5Blend_HDA_o
					psColorDodge5Blend_HDA_o( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				}
			}
			break;
		case bmPsColorBurn:
			if( opa == 255 ) {
				if( !hda ) { // PsColorBurnBlend
					psColorBurnBlend( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				} else { // PsColorBurnBlend_HDA
					psColorBurnBlend_HDA( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				}
			} else {
				if( !hda ) { // PsColorBurnBlend_o
					psColorBurnBlend_o( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				} else { // PsColorBurnBlend_HDA_o
					psColorBurnBlend_HDA_o( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				}
			}
			break;
		case bmPsLighten:
			if( opa == 255 ) {
				if( !hda ) { // PsLightenBlend
					psLightenBlend( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				} else { // PsLightenBlend_HDA
					psLightenBlend_HDA( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				}
			} else {
				if( !hda ) { // PsLightenBlend_o
					psLightenBlend_o( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				} else { // PsLightenBlend_HDA_o
					psLightenBlend_HDA_o( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				}
			}
			break;
		case bmPsDarken:
			if( opa == 255 ) {
				if( !hda ) { // PsDarkenBlend
					psDarkenBlend( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				} else { // PsDarkenBlend_HDA
					psDarkenBlend_HDA( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				}
			} else {
				if( !hda ) { // PsDarkenBlend_o
					psDarkenBlend_o( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				} else { // PsDarkenBlend_HDA_o
					psDarkenBlend_HDA_o( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				}
			}
			break;
		case bmPsDifference:
			if( opa == 255 ) {
				if( !hda ) { // PsDiffBlend
					psDiffBlend( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				} else { // PsDiffBlend_HDA
					psDiffBlend_HDA( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				}
			} else {
				if( !hda ) { // PsDiffBlend_o
					psDiffBlend_o( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				} else { // PsDiffBlend_HDA_o
					psDiffBlend_HDA_o( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				}
			}
			break;
		case bmPsDifference5:
			if( opa == 255 ) {
				if( !hda ) { // PsDiff5Blend
					psDiff5Blend( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				} else { // PsDiff5Blend_HDA
					psDiff5Blend_HDA( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				}
			} else {
				if( !hda ) { // PsDiff5Blend_o
					psDiff5Blend_o( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				} else { // PsDiff5Blend_HDA_o
					psDiff5Blend_HDA_o( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				}
			}
			break;
		case bmPsExclusion:
			if( opa == 255 ) {
				if( !hda ) { // PsExclusionBlend
					psExclusionBlend( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				} else { // PsExclusionBlend_HDA
					psExclusionBlend_HDA( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				}
			} else {
				if( !hda ) { // PsExclusionBlend_o
					psExclusionBlend_o( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				} else { // PsExclusionBlend_HDA_o
					psExclusionBlend_HDA_o( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
				}
			}
			break;
		case copyMain:
			copyMain( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
			break;
		case copyMask:
			copyMask( s, d, d, srcStride, dstStride, srect.width(), srect.height(), st, dt, opa);
			break;
		default:
			return;
		}
		buff.flip();
		dst.copyPixelsFromBuffer(buff);
	}

	static void copyColor(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				o[dt+x] = (d[dt+x]&0xff000000) | (s[st+x]&0x00ffffff);
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void constAlphaBlend( int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int d1 = d0 & 0xff00ff;
				d1 = ((d1 + (((s0 & 0xff00ff) - d1) * opa >>> 8)) & 0xff00ff);
				d0 &= 0xff00;
				s0 &= 0xff00;
				o[dt+x] = d1 | ((d0 + ((s0 - d0) * opa >>> 8)) & 0xff00) | 0xff000000;
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void constAlphaBlend_HDA(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int d1 = d0 & 0xff00ff;
				d1 = ((d1 + (((s0 & 0xff00ff) - d1) * opa >>> 8)) & 0xff00ff) | (d0 & 0xff000000);
				d0 &= 0xff00;
				s0 &= 0xff00;
				o[dt+x] = d1 | ((d0 + ((s0 - d0) * opa >>> 8)) & 0xff00);
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void copyOpaqueImage(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				o[dt+x] = s[st+x] | 0xff000000;
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void constAlphaBlend_d(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int addr = opa + (d0>>>24);
				int alpha = ((int)OpacityOnOpacityTable[addr])&0xff;
				int d1 = d0 & 0xff00ff;
				d1 = ((d1 + (((s0 & 0xff00ff) - d1) * alpha >>> 8)) & 0xff00ff) | (NegativeMulTable[addr]<<24);
				d0 &= 0xff00;
				s0 &= 0xff00;
				o[dt+x] = d1 | ((d0 + ((s0 - d0) * alpha >>> 8)) & 0xff00);
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void alphaBlend(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int sopa = s0 >>> 24;
				int d1 = d0 & 0xff00ff;
				d1 = (d1 + (((s0 & 0xff00ff) - d1) * sopa >>> 8)) & 0xff00ff;
				d0 &= 0xff00;
				s0 &= 0xff00;
				o[dt+x] = d1 + ((d0 + ((s0 - d0) * sopa >>> 8)) & 0xff00) | 0xff000000;
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void alphaBlend_HDA(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int sopa = s0 >>> 24;
				int d1 = d0 & 0xff00ff;
				d1 = ((d1 + (((s0 & 0xff00ff) - d1) * sopa >>> 8)) & 0xff00ff) + (d0 & 0xff000000); /* hda */
				d0 &= 0xff00;
				s0 &= 0xff00;
				o[dt+x] = d1 + ((d0 + ((s0 - d0) * sopa >>> 8)) & 0xff00);
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void alphaBlend_o(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int sopa = ((s0 >>> 24) * opa) >>> 8;
				int d1 = d0 & 0xff00ff;
				d1 = (d1 + (((s0 & 0xff00ff) - d1) * sopa >>> 8)) & 0xff00ff;
				d0 &= 0xff00;
				s0 &= 0xff00;
				o[dt+x] = d1 + ((d0 + ((s0 - d0) * sopa >>> 8)) & 0xff00) | 0xff000000;
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void alphaBlend_HDA_o(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int sopa = ((s0 >>> 24) * opa) >> 8;
				int d1 = d0 & 0xff00ff;
				d1 = ((d1 + (((s0 & 0xff00ff) - d1) * sopa >>> 8)) & 0xff00ff) + (d0 & 0xff000000);
				d0 &= 0xff00;
				s0 &= 0xff00;
				o[dt+x] = d1 + ((d0 + ((s0 - d0) * sopa >>> 8)) & 0xff00);
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void alphaBlend_d(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int addr = ((s0 >>> 16) & 0xff00) + (d0>>>24);
				int destalpha = NegativeMulTable[addr]<<24;
				int sopa = ((int)OpacityOnOpacityTable[addr])&0xff;
				int d1 = d0 & 0xff00ff;
				d1 = (d1 + (((s0 & 0xff00ff) - d1) * sopa >>> 8)) & 0xff00ff;
				d0 &= 0xff00;
				s0 &= 0xff00;
				o[dt+x] = d1 + ((d0 + ((s0 - d0) * sopa >>> 8)) & 0xff00) + destalpha;
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void alphaBlend_do(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int addr = (( (s0>>>24)*opa) & 0xff00) + (d0>>>24);
				int destalpha = NegativeMulTable[addr]<<24;
				int sopa = ((int)OpacityOnOpacityTable[addr])&0xff;
				int d1 = d0 & 0xff00ff;
				d1 = (d1 + (((s0 & 0xff00ff) - d1) * sopa >>> 8)) & 0xff00ff;
				d0 &= 0xff00;
				s0 &= 0xff00;
				o[dt+x] = d1 + ((d0 + ((s0 - d0) * sopa >>> 8)) & 0xff00) + destalpha;
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void addBlend(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int tmp = (  ( s0 & d0 ) + ( ((s0^d0)>>>1) & 0x7f7f7f7f)  ) & 0x80808080;
				tmp = (tmp<<1) - (tmp>>>7);
				o[dt+x] = (s0 + d0 - tmp) | tmp;
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void addBlend_HDA(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int tmp = ( ( s0 & d0 ) + ( ((s0^d0)>>>1) & 0x7f7f7f7f)  ) & 0x80808080;
				tmp = (tmp<<1) - (tmp>>>7);
				o[dt+x] = (((s0 + d0 - tmp) | tmp) & 0xffffff) | (d0 & 0xff000000);
			}
			st += srcStride;
			dt += dstStride;
		}
	}
	static void addBlend_o( int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa ) {
		int tmp, s0, d0;
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				s0 = s[st+x];
				d0 = d[dt+x];
				s0 = ( ((s0&0x00ff00) * opa >>> 8)&0x00ff00) + (( (s0&0xff00ff) * opa >>> 8)&0xff00ff);
				tmp = ( ( s0 & d0 ) + ( ((s0^d0)>>>1) & 0x7f7f7f7f)  ) & 0x80808080;
				tmp = (tmp<<1) - (tmp>>>7);
				o[dt+x] = (s0 + d0 - tmp) | tmp;
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void addBlend_HDA_o(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				s0 = ( ((s0&0x00ff00)  * opa >>> 8)&0x00ff00) + (( (s0&0xff00ff) * opa >>> 8)&0xff00ff);
				int tmp = ( ( s0 & d0 ) + ( ((s0^d0)>>>1) & 0x7f7f7f7f) ) & 0x80808080;
				tmp = (tmp<<1) - (tmp>>>7);
				o[dt+x] = (((s0 + d0 - tmp) | tmp) & 0xffffff) + (d0 & 0xff000000);
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void subBlend(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int tmp = ( ( s0 & d0 ) + ( ((s0 ^ d0)>>>1) & 0x7f7f7f7f) ) & 0x80808080;
				tmp = (tmp << 1) - (tmp >>> 7);
				o[dt+x] = (s0 + d0 - tmp) & tmp;
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void subBlend_HDA(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				s0 = s0 | 0xff000000;
				int tmp = (  ( s0 & d0 ) + ( ((s0 ^ d0)>>>1) & 0x7f7f7f7f)  ) & 0x80808080;
				tmp = (tmp << 1) - (tmp >>> 7);
				o[dt+x] = (s0 + d0 - tmp) & tmp;
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void subBlend_o(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				s0 = ~s0;
				s0 = ~ (( ((s0&0x00ff00)  * opa >>> 8)&0x00ff00) + (( (s0&0xff00ff) * opa >>> 8)&0xff00ff) );
				int tmp = ( ( s0 & d0 ) + ( ((s0 ^ d0)>>>1) & 0x7f7f7f7f)  ) & 0x80808080;
				tmp = (tmp << 1) - (tmp >>> 7);
				o[dt+x] = (s0 + d0 - tmp) & tmp;
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void subBlend_HDA_o(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				s0 = ~s0;
				s0 = 0xff000000 | ~ (( ((s0&0x00ff00)  * opa >>> 8)&0x00ff00) + (( (s0&0xff00ff) * opa >>> 8)&0xff00ff) );
				int tmp = ( ( s0 & d0 ) + ( ((s0 ^ d0)>>>1) & 0x7f7f7f7f)  ) & 0x80808080;
				tmp = (tmp << 1) - (tmp >>> 7);
				o[dt+x] = (s0 + d0 - tmp) & tmp;
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void mulBlend(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int tmp  = (d0 & 0xff) * (s0 & 0xff) & 0xff00;
				tmp |= ((d0 & 0xff00) >>> 8) * (s0 & 0xff00) & 0xff0000;
				tmp |= ((d0 & 0xff0000) >>> 16) * (s0 & 0xff0000) & 0xff000000;
				tmp >>>= 8;
				o[dt+x] = tmp | 0xff000000;
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void mulBlend_HDA(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int tmp  = (d0 & 0xff) * (s0 & 0xff) & 0xff00;
				tmp |= ((d0 & 0xff00) >>> 8) * (s0 & 0xff00) & 0xff0000;
				tmp |= ((d0 & 0xff0000) >>> 16) * (s0 & 0xff0000) & 0xff000000;
				tmp >>>= 8;
				o[dt+x] = tmp + (d0 & 0xff000000);
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void mulBlend_o(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				s0 = ~s0;
				s0 = ~( ( ((s0&0x00ff00)  * opa >>> 8)&0x00ff00) + (( (s0&0xff00ff) * opa >>> 8)&0xff00ff));
				int tmp  = (d0 & 0xff) * (s0 & 0xff) & 0xff00;
				tmp |= ((d0 & 0xff00) >>> 8) * (s0 & 0xff00) & 0xff0000;
				tmp |= ((d0 & 0xff0000) >>> 16) * (s0 & 0xff0000) & 0xff000000;
				tmp >>>= 8;
				o[dt+x] = tmp | 0xff000000;
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void mulBlend_HDA_o(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				s0 = ~s0;
				s0 = ~( ( ((s0&0x00ff00)  * opa >>> 8)&0x00ff00) + (( (s0&0xff00ff) * opa >>> 8)&0xff00ff));
				int tmp  = (d0 & 0xff) * (s0 & 0xff) & 0xff00;
				tmp |= ((d0 & 0xff00) >>> 8) * (s0 & 0xff00) & 0xff0000;
				tmp |= ((d0 & 0xff0000) >>> 16) * (s0 & 0xff0000) & 0xff000000;
				tmp >>>= 8;
				o[dt+x] = tmp + (d0 & 0xff000000);
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void colorDodgeBlend(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int tmp2 = ~s0;
				int tmp = (d0 & 0xff) * RecipTable256[tmp2 & 0xff] >>> 8;
				int tmp3 = (tmp | (~(tmp - 0x100) >> 31)) & 0xff;
				tmp = ((d0 & 0xff00)>>>8) * RecipTable256[(tmp2 & 0xff00)>>>8];
				tmp3 |= (tmp | (~(tmp - 0x10000) >> 31)) & 0xff00;
				tmp = ((d0 & 0xff0000)>>>16) * RecipTable256[(tmp2 & 0xff0000)>>>16];
				tmp3 |= ((tmp | (~(tmp - 0x10000) >> 31)) & 0xff00 ) << 8;
				o[dt+x] = tmp3 | 0xff000000;
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void colorDodgeBlend_HDA(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int tmp2 = ~s0;
				int tmp = (d0 & 0xff) * RecipTable256[tmp2 & 0xff] >>> 8;
				int tmp3 = (tmp | (~(tmp - 0x100) >> 31)) & 0xff;
				tmp = ((d0 & 0xff00)>>>8) * RecipTable256[(tmp2 & 0xff00)>>>8];
				tmp3 |= (tmp | (~(tmp - 0x10000) >> 31)) & 0xff00;
				tmp = ((d0 & 0xff0000)>>>16) * RecipTable256[(tmp2 & 0xff0000)>>>16];
				tmp3 |= ((tmp | (~(tmp - 0x10000) >> 31)) & 0xff00 ) << 8;
				o[dt+x] = tmp3 + (d0 & 0xff000000);
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void colorDodgeBlend_o(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int tmp2 = ~ (( ((s0&0x00ff00)  * opa >>> 8)&0x00ff00) + (( (s0&0xff00ff) * opa >>> 8)&0xff00ff) );
				int tmp = (d0 & 0xff) * RecipTable256[tmp2 & 0xff] >>> 8;
				int tmp3 = (tmp | (~(tmp - 0x100) >> 31)) & 0xff;
				tmp = ((d0 & 0xff00)>>>8) * RecipTable256[(tmp2 & 0xff00)>>>8];
				tmp3 |= (tmp | (~(tmp - 0x10000) >> 31)) & 0xff00;
				tmp = ((d0 & 0xff0000)>>>16) * RecipTable256[(tmp2 & 0xff0000)>>>16];
				tmp3 |= ((tmp | (~(tmp - 0x10000) >> 31)) & 0xff00 ) << 8;
				o[dt+x] = tmp3 | 0xff000000;
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void colorDodgeBlend_HDA_o(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int tmp2 = ~ (( ((s0&0x00ff00)  * opa >>> 8)&0x00ff00) + (( (s0&0xff00ff) * opa >> 8)&0xff00ff) );
				int tmp = (d0 & 0xff) * RecipTable256[tmp2 & 0xff] >>> 8;
				int tmp3 = (tmp | (~(tmp - 0x100) >> 31)) & 0xff;
				tmp = ((d0 & 0xff00)>>>8) * RecipTable256[(tmp2 & 0xff00)>>>8];
				tmp3 |= (tmp | (~(tmp - 0x10000) >> 31)) & 0xff00;
				tmp = ((d0 & 0xff0000)>>>16) * RecipTable256[(tmp2 & 0xff0000)>>>16];
				tmp3 |= ((tmp | (~(tmp - 0x10000) >> 31)) & 0xff00 ) << 8;
				o[dt+x] = tmp3 + (d0 & 0xff000000);
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void darkenBlend(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int m_src = ~s0;
				int tmp = ((m_src & d0) + (((m_src ^ d0) >>> 1) & 0x7f7f7f7f) ) & 0x80808080;
				tmp = (tmp << 1) - (tmp >>> 7);
				d0 ^= (d0^ s0) & tmp;
				o[dt+x] = d0 | 0xff000000;
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void darkenBlend_HDA(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int m_src = ~s0;
				int tmp = ((m_src & d0) + (((m_src ^ d0) >>> 1) & 0x7f7f7f7f) ) & 0x80808080;
				tmp = (tmp << 1) - (tmp >>> 7);
				o[dt+x] ^= ((d0 ^ s0) & tmp) & 0xffffff;
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void darkenBlend_o(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int m_src = ~s0;
				int tmp = ((m_src & d0) + (((m_src ^ d0) >>> 1) & 0x7f7f7f7f) ) & 0x80808080;
				tmp = (tmp << 1) - (tmp >>> 7);
				tmp = d0 ^ ((d0 ^ s0) & tmp);
				int d1 = d0 & 0xff00ff;
				d1 = (d1 + (((tmp & 0xff00ff) - d1) * opa >>> 8)) & 0xff00ff;
				m_src = d0 & 0xff00;
				tmp &= 0xff00;
				o[dt+x] = d1 + ((m_src + ((tmp - m_src) * opa >>> 8)) & 0xff00) | 0xff000000;
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void darkenBlend_HDA_o(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int m_src = ~s0;
				int tmp = ((m_src & d0) + (((m_src ^ d0) >>> 1) & 0x7f7f7f7f) ) & 0x80808080;
				tmp = (tmp << 1) - (tmp >>> 7);
				tmp = d0 ^ (((d0 ^ s0) & tmp) & 0xffffff);
				int d1 = d0 & 0xff00ff;
				d1 = ((d1 + (((tmp & 0xff00ff) - d1) * opa >>> 8)) & 0xff00ff) + (d0 & 0xff000000); /* hda */
				m_src = d0 & 0xff00;
				tmp &= 0xff00;
				o[dt+x] = d1 + ((m_src + ((tmp - m_src) * opa >>> 8)) & 0xff00);
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void lightenBlend(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int m_dest = ~d0;
				int tmp = ((s0 & m_dest) + (((s0 ^ m_dest) >>> 1) & 0x7f7f7f7f) ) & 0x80808080;
				tmp = (tmp << 1) - (tmp >>> 7);
				d0 ^= (d0 ^ s0) & tmp;
				o[dt+x] = d0 | 0xff000000;
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void lightenBlend_HDA(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int m_dest = ~d0;
				int tmp = ((s0 & m_dest) + (((s0 ^ m_dest) >>> 1) & 0x7f7f7f7f) ) & 0x80808080;
				tmp = (tmp << 1) - (tmp >>> 7);
				o[dt+x] ^= ((d0 ^ s0) & tmp) & 0xffffff;
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void lightenBlend_o(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int m_dest = ~d0;
				int tmp = ((s0 & m_dest) + (((s0 ^ m_dest) >>> 1) & 0x7f7f7f7f) ) & 0x80808080;
				tmp = (tmp << 1) - (tmp >>> 7);
				tmp = d0 ^ ((d0 ^ s0) & tmp);
				int d1 = d0 & 0xff00ff;
				d1 = (d1 + (((tmp & 0xff00ff) - d1) * opa >>> 8)) & 0xff00ff;
				m_dest = d0 & 0xff00;
				tmp &= 0xff00;
				o[dt+x] = d1 + ((m_dest + ((tmp - m_dest) * opa >>> 8)) & 0xff00) | 0xff000000;
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void lightenBlend_HDA_o(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int m_dest = ~d0;
				int tmp = ((s0 & m_dest) + (((s0 ^ m_dest) >>> 1) & 0x7f7f7f7f) ) & 0x80808080;
				tmp = (tmp << 1) - (tmp >>> 7);
				tmp = d0 ^ (((d0 ^ s0) & tmp) & 0xffffff);
				int d1 = d0 & 0xff00ff;
				d1 = ((d1 + (((tmp & 0xff00ff) - d1) * opa >>> 8)) & 0xff00ff) + (d0 & 0xff000000); /* hda */
				m_dest = d0 & 0xff00;
				tmp &= 0xff00;
				o[dt+x] = d1 + ((m_dest + ((tmp - m_dest) * opa >>> 8)) & 0xff00);
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void screenBlend(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				s0 = ~s0;
				d0 = ~d0;
				int tmp  = (d0 & 0xff) * (s0 & 0xff) & 0xff00;
				tmp |= ((d0 & 0xff00) >>> 8) * (s0 & 0xff00) & 0xff0000;
				tmp |= ((d0 & 0xff0000) >>> 16) * (s0 & 0xff0000) & 0xff000000;
				tmp >>>= 8;
				o[dt+x] = ~tmp | 0xff000000;
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void screenBlend_HDA(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				s0 = ~s0;
				d0 = ~d0;
				int tmp  = (d0 & 0xff) * (s0 & 0xff) & 0xff00;
				tmp |= ((d0 & 0xff00) >>> 8) * (s0 & 0xff00) & 0xff0000;
				tmp |= ((d0 & 0xff0000) >>> 16) * (s0 & 0xff0000) & 0xff000000;
				tmp >>>= 8;
				o[dt+x] = ~tmp ^ (d0 & 0xff000000);
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void screenBlend_o(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				d0 = ~d0;
				s0 = ~( ( ((s0&0x00ff00)  * opa >>> 8)&0x00ff00) + (( (s0&0xff00ff) * opa >>> 8)&0xff00ff));
				int tmp  = (d0 & 0xff) * (s0 & 0xff) & 0xff00;
				tmp |= ((d0 & 0xff00) >>> 8) * (s0 & 0xff00) & 0xff0000;
				tmp |= ((d0 & 0xff0000) >>> 16) * (s0 & 0xff0000) & 0xff000000;
				tmp >>>= 8;
				o[dt+x] = ~tmp | 0xff000000;
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void screenBlend_HDA_o(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				d0 = ~d0;
				s0 = ~( ( ((s0&0x00ff00)  * opa >>> 8)&0x00ff00) + (( (s0&0xff00ff) * opa >>> 8)&0xff00ff));
				int tmp  = (d0 & 0xff) * (s0 & 0xff) & 0xff00;
				tmp |= ((d0 & 0xff00) >>> 8) * (s0 & 0xff00) & 0xff0000;
				tmp |= ((d0 & 0xff0000) >>> 16) * (s0 & 0xff0000) & 0xff000000;
				tmp >>>= 8;
				o[dt+x] = ~tmp ^ (d0 & 0xff000000);
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void additiveAlphaBlend(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int sopa = (~s0) >>> 24;
				// TVPSaturatedAdd
				int a = (((d0& 0xff00ff)*sopa >>> 8) & 0xff00ff) + (((d0 & 0xff00)*sopa >>> 8) & 0xff00);
				int b = s0;
				int tmp = (  ( a & b ) + ( ((a ^ b)>>>1) & 0x7f7f7f7f)  ) & 0x80808080;
				tmp = (tmp<<1) - (tmp>>>7);
				o[dt+x] = (a + b - tmp) | tmp;
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void additiveAlphaBlend_HDA(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int sopa = (~s0) >>> 24;
				// TVPSaturatedAdd
				int a = (((d0 & 0xff00ff)*sopa >>> 8) & 0xff00ff) + (((d0 & 0xff00)*sopa >>> 8) & 0xff00);
				int b = s0;
				int tmp = (  ( a & b ) + ( ((a ^ b)>>>1) & 0x7f7f7f7f)  ) & 0x80808080;
				tmp = (tmp<<1) - (tmp>>>7);
				o[dt+x] = (d0 & 0xff000000) + ((a + b - tmp) | tmp) & 0xffffff;
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void additiveAlphaBlend_o(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				s0 = (((s0 & 0xff00ff)*opa >>> 8) & 0xff00ff) + (((s0 >>> 8) & 0xff00ff)*opa & 0xff00ff00);
				int sopa = (~s0) >>> 24;
				// TVPSaturatedAdd
				int a = (((d0& 0xff00ff)*sopa >>> 8) & 0xff00ff) + (((d0 & 0xff00)*sopa >>> 8) & 0xff00);
				int b = s0;
				int tmp = (  ( a & b ) + ( ((a ^ b)>>>1) & 0x7f7f7f7f)  ) & 0x80808080;
				tmp = (tmp<<1) - (tmp>>>7);
				o[dt+x] = (a + b - tmp) | tmp;
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void additiveAlphaBlend_HDA_o(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				s0 = (((s0 & 0xff00ff)*opa >>> 8) & 0xff00ff) + (((s0 >>> 8) & 0xff00ff)*opa & 0xff00ff00);
				int sopa = (~s0) >>> 24;
				// TVPSaturatedAdd
				int a = (((d0 & 0xff00ff)*sopa >>> 8) & 0xff00ff) + (((d0 & 0xff00)*sopa >>> 8) & 0xff00);
				int b = s0;
				int tmp = (  ( a & b ) + ( ((a ^ b)>>>1) & 0x7f7f7f7f)  ) & 0x80808080;
				tmp = (tmp<<1) - (tmp>>>7);
				o[dt+x] = (d0 & 0xff000000) + ((a + b - tmp) | tmp) & 0xffffff;
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void additiveAlphaBlend_a(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				/*
						Di = sat(Si, (1-Sa)*Di)
						Da = Sa + Da - SaDa
				 */
				int dopa = d0 >>> 24;
				int sopa = s0 >>> 24;
				dopa = dopa + sopa - (dopa*sopa >>> 8);
				dopa -= (dopa >>> 8); /* adjust alpha */
				sopa ^= 0xff;
				s0 &= 0xffffff;
				int a = (((d0 & 0xff00ff)*sopa >>> 8) & 0xff00ff) + (((d0 & 0xff00)*sopa >>> 8) & 0xff00);
				int b = s0;
				int tmp = (  ( a & b ) + ( ((a ^ b)>>>1) & 0x7f7f7f7f)  ) & 0x80808080;
				tmp = (tmp<<1) - (tmp>>>7);
				o[dt+x] = (dopa << 24) + ((a + b - tmp) | tmp);
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void additiveAlphaBlend_ao(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				s0 = (((s0 & 0xff00ff)*opa >>> 8) & 0xff00ff) + (((s0 >>> 8) & 0xff00ff)*opa & 0xff00ff00);
				/*
						Di = sat(Si, (1-Sa)*Di)
						Da = Sa + Da - SaDa
				 */
				int dopa = d0 >>> 24;
				int sopa = s0 >>> 24;
				dopa = dopa + sopa - (dopa*sopa >>> 8);
				dopa -= (dopa >>> 8); /* adjust alpha */
				sopa ^= 0xff;
				s0 &= 0xffffff;
				int a = (((d0 & 0xff00ff)*sopa >>> 8) & 0xff00ff) + (((d0 & 0xff00)*sopa >>> 8) & 0xff00);
				int b = s0;
				int tmp = (  ( a & b ) + ( ((a ^ b)>>>1) & 0x7f7f7f7f)  ) & 0x80808080;
				tmp = (tmp<<1) - (tmp>>>7);
				o[dt+x] = (dopa << 24) + ((a + b - tmp) | tmp);
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void alphaBlend_a(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int alpha = s0 >>> 24;
				int color = s0;
				s0 = (((((color & 0x00ff00) * alpha) & 0x00ff0000) + (((color & 0xff00ff) * alpha) & 0xff00ff00) ) >>> 8);
				s0 = s0 + (color & 0xff000000);
				/*
								Di = sat(Si, (1-Sa)*Di)
								Da = Sa + Da - SaDa
				 */
				int dopa = d0 >>> 24;
				int sopa = s0 >>> 24;
				dopa = dopa + sopa - (dopa*sopa >>> 8);
				dopa -= (dopa >>> 8); /* adjust alpha */
				sopa ^= 0xff;
				s0 &= 0xffffff;
				int a = (((d0 & 0xff00ff)*sopa >>> 8) & 0xff00ff) + (((d0 & 0xff00)*sopa >>> 8) & 0xff00);
				int b = s0;
				int tmp = (  ( a & b ) + ( ((a ^ b)>>>1) & 0x7f7f7f7f)  ) & 0x80808080;
				tmp = (tmp<<1) - (tmp>>>7);
				tmp = (a + b - tmp) | tmp;
				o[dt+x] = (dopa << 24) + tmp;
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void alphaBlend_ao(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				s0 = (s0 & 0xffffff) + ((((s0 >>> 24) * opa) >>> 8) << 24);
				int alpha = s0 >>> 24;
				int color = s0;
				s0 = (((((color & 0x00ff00) * alpha) & 0x00ff0000) + (((color & 0xff00ff) * alpha) & 0xff00ff00) ) >>> 8);
				s0 = s0 + (color & 0xff000000);
				/*
						Di = sat(Si, (1-Sa)*Di)
						Da = Sa + Da - SaDa
				 */
				int dopa = d0 >>> 24;
				int sopa = s0 >>> 24;
				dopa = dopa + sopa - (dopa*sopa >>> 8);
				dopa -= (dopa >>> 8); /* adjust alpha */
				sopa ^= 0xff;
				s0 &= 0xffffff;
				int a = (((d0 & 0xff00ff)*sopa >>> 8) & 0xff00ff) + (((d0 & 0xff00)*sopa >>> 8) & 0xff00);
				int b = s0;
				int tmp = (  ( a & b ) + ( ((a ^ b)>>>1) & 0x7f7f7f7f)  ) & 0x80808080;
				tmp = (tmp<<1) - (tmp>>>7);
				tmp = (a + b - tmp) | tmp;
				o[dt+x] = (dopa << 24) + tmp;
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void constAlphaBlend_a(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				s0 = (s0 & 0xffffff) | opa;
				/*
						Di = sat(Si, (1-Sa)*Di)
						Da = Sa + Da - SaDa
				 */
				int dopa = d0 >>> 24;
				int sopa = s0 >>> 24;
				dopa = dopa + sopa - (dopa*sopa >>> 8);
				dopa -= (dopa >>> 8); /* adjust alpha */
				sopa ^= 0xff;
				s0 &= 0xffffff;
				int a = (((d0 & 0xff00ff)*sopa >>> 8) & 0xff00ff) + (((d0 & 0xff00)*sopa >>> 8) & 0xff00);
				int b = s0;
				int tmp = (  ( a & b ) + ( ((a ^ b)>>>1) & 0x7f7f7f7f)  ) & 0x80808080;
				tmp = (tmp<<1) - (tmp>>>7);
				tmp = (a + b - tmp) | tmp;
				o[dt+x] = (dopa << 24) + tmp;
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void psAlphaBlend(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int a = s0>>>24;
				// TVPPsOperationAlphaBlend = TVPPS_ALPHABLEND
				int d1 = d0&0x00ff00ff;
				int d2 = d0&0x0000ff00;
				s0 = ((((((s0&0x00ff00ff)-d1)*a)>>>8)+d1)&0x00ff00ff)|((((((s0&0x0000ff00)-d2)*a)>>>8)+d2)&0x0000ff00);
				o[dt+x] = s0 | 0xff000000;
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void psAlphaBlend_HDA(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		int s0, d0;
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				s0 = s[st+x];
				d0 = d[dt+x];
				int a = s0 >>> 24;
				// TVPPsOperationAlphaBlend = TVPPS_ALPHABLEND
				int d1 = d0&0x00ff00ff;
				int d2 = d0&0x0000ff00;
				s0 = ((((((s0&0x00ff00ff)-d1)*a)>>>8)+d1)&0x00ff00ff)|((((((s0&0x0000ff00)-d2)*a)>>>8)+d2)&0x0000ff00);
				o[dt+x] = s0|(d0&0xff000000);
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void psAlphaBlend_o(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int a = ((s0>>>24)*opa)>>>8;
				// TVPPsOperationAlphaBlend = TVPPS_ALPHABLEND
				int d1 = d0&0x00ff00ff;
				int d2 = d0&0x0000ff00;
				s0 = ((((((s0&0x00ff00ff)-d1)*a)>>>8)+d1)&0x00ff00ff)|((((((s0&0x0000ff00)-d2)*a)>>>8)+d2)&0x0000ff00);
				o[dt+x] = s0 | 0xff000000;
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void psAlphaBlend_HDA_o(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int a = ((s0>>>24)*opa)>>>8;
				// TVPPsOperationAlphaBlend = TVPPS_ALPHABLEND
				int d1 = d0&0x00ff00ff;
				int d2 = d0&0x0000ff00;
				s0 = ((((((s0&0x00ff00ff)-d1)*a)>>>8)+d1)&0x00ff00ff)|((((((s0&0x0000ff00)-d2)*a)>>>8)+d2)&0x0000ff00);
				o[dt+x] = s0|(d0&0xff000000);
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void psAddBlend(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int a = s0>>>24;
				// TVPPsOperationAddBlend
				int n;
				n = (((d0&s0)<<1)+((d0^s0)&0x00fefefe))&0x01010100;
				n = ((n>>>8)+0x007f7f7f)^0x007f7f7f;
				s0 = (d0+s0-n)|n;
				// TVPPsOperationAlphaBlend = TVPPS_ALPHABLEND
				int d1 = d0&0x00ff00ff;
				int d2 = d0&0x0000ff00;
				s0 = ((((((s0&0x00ff00ff)-d1)*a)>>>8)+d1)&0x00ff00ff)|((((((s0&0x0000ff00)-d2)*a)>>>8)+d2)&0x0000ff00);
				o[dt+x] = s0 | 0xff000000;
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void psAddBlend_HDA(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int a = s0 >>> 24;
				// TVPPsOperationAddBlend
				int n;
				n = (((d0&s0)<<1)+((d0^s0)&0x00fefefe))&0x01010100;
				n = ((n>>>8)+0x007f7f7f)^0x007f7f7f;
				s0 = (d0+s0-n)|n;
				// TVPPsOperationAlphaBlend = TVPPS_ALPHABLEND
				int d1 = d0&0x00ff00ff;
				int d2 = d0&0x0000ff00;
				s0 = ((((((s0&0x00ff00ff)-d1)*a)>>>8)+d1)&0x00ff00ff)|((((((s0&0x0000ff00)-d2)*a)>>>8)+d2)&0x0000ff00);
				o[dt+x] = s0|(d0&0xff000000);
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void psAddBlend_o(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int a = ((s0>>>24)*opa)>>>8;
				// TVPPsOperationAddBlend
				int n;
				n = (((d0&s0)<<1)+((d0^s0)&0x00fefefe))&0x01010100;
				n = ((n>>>8)+0x007f7f7f)^0x007f7f7f;
				s0 = (d0+s0-n)|n;
				// TVPPsOperationAlphaBlend = TVPPS_ALPHABLEND
				int d1 = d0&0x00ff00ff;
				int d2 = d0&0x0000ff00;
				s0 = ((((((s0&0x00ff00ff)-d1)*a)>>>8)+d1)&0x00ff00ff)|((((((s0&0x0000ff00)-d2)*a)>>>8)+d2)&0x0000ff00);
				o[dt+x] = s0 | 0xff000000;
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void psAddBlend_HDA_o(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int a = ((s0>>>24)*opa)>>>8;
				// TVPPsOperationAddBlend
				int n;
				n = (((d0&s0)<<1)+((d0^s0)&0x00fefefe))&0x01010100;
				n = ((n>>>8)+0x007f7f7f)^0x007f7f7f;
				s0 = (d0+s0-n)|n;
				// TVPPsOperationAlphaBlend = TVPPS_ALPHABLEND
				int d1 = d0&0x00ff00ff;
				int d2 = d0&0x0000ff00;
				s0 = ((((((s0&0x00ff00ff)-d1)*a)>>>8)+d1)&0x00ff00ff)|((((((s0&0x0000ff00)-d2)*a)>>>8)+d2)&0x0000ff00);
				o[dt+x] = s0|(d0&0xff000000);
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void psSubBlend(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int a = s0>>>24;
				// TVPPsOperationSubBlend
				s0 = ~s0;
				int n = (((~d0&s0)<<1)+((~d0^s0)&0x00fefefe))&0x01010100;
				n = ((n>>>8)+0x007f7f7f)^0x007f7f7f;
				s0 = (d0|n)-(s0|n);
				// TVPPsOperationAlphaBlend = TVPPS_ALPHABLEND
				int d1 = d0&0x00ff00ff;
				int d2 = d0&0x0000ff00;
				s0 = ((((((s0&0x00ff00ff)-d1)*a)>>>8)+d1)&0x00ff00ff)|((((((s0&0x0000ff00)-d2)*a)>>>8)+d2)&0x0000ff00);
				o[dt+x] = s0 | 0xff000000;
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void psSubBlend_HDA(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int a = s0 >>> 24;
				// TVPPsOperationSubBlend
				s0 = ~s0;
				int n = (((~d0&s0)<<1)+((~d0^s0)&0x00fefefe))&0x01010100;
				n = ((n>>>8)+0x007f7f7f)^0x007f7f7f;
				s0 = (d0|n)-(s0|n);
				// TVPPsOperationAlphaBlend = TVPPS_ALPHABLEND
				int d1 = d0&0x00ff00ff;
				int d2 = d0&0x0000ff00;
				s0 = ((((((s0&0x00ff00ff)-d1)*a)>>>8)+d1)&0x00ff00ff)|((((((s0&0x0000ff00)-d2)*a)>>>8)+d2)&0x0000ff00);
				o[dt+x] = s0|(d0&0xff000000);
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void psSubBlend_o(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int a = ((s0>>>24)*opa)>>>8;
				// TVPPsOperationSubBlend
				s0 = ~s0;
				int n = (((~d0&s0)<<1)+((~d0^s0)&0x00fefefe))&0x01010100;
				n = ((n>>>8)+0x007f7f7f)^0x007f7f7f;
				s0 = (d0|n)-(s0|n);
				// TVPPsOperationAlphaBlend = TVPPS_ALPHABLEND
				int d1 = d0&0x00ff00ff;
				int d2 = d0&0x0000ff00;
				s0 = ((((((s0&0x00ff00ff)-d1)*a)>>>8)+d1)&0x00ff00ff)|((((((s0&0x0000ff00)-d2)*a)>>>8)+d2)&0x0000ff00);
				o[dt+x] = s0 | 0xff000000;
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void psSubBlend_HDA_o(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int a = ((s0>>>24)*opa)>>>8;
				// TVPPsOperationSubBlend
				s0 = ~s0;
				int n = (((~d0&s0)<<1)+((~d0^s0)&0x00fefefe))&0x01010100;
				n = ((n>>>8)+0x007f7f7f)^0x007f7f7f;
				s0 = (d0|n)-(s0|n);
				// TVPPsOperationAlphaBlend = TVPPS_ALPHABLEND
				int d1 = d0&0x00ff00ff;
				int d2 = d0&0x0000ff00;
				s0 = ((((((s0&0x00ff00ff)-d1)*a)>>>8)+d1)&0x00ff00ff)|((((((s0&0x0000ff00)-d2)*a)>>>8)+d2)&0x0000ff00);
				o[dt+x] = s0|(d0&0xff000000);
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void psMulBlend(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int a = s0>>>24;
				// TVPPsOperationMulBlend
				s0 = ( ((((d0>>>16)&0xff)*(s0&0x00ff0000))&0xff000000) |
						((((d0>>>8 )&0xff)*(s0&0x0000ff00))&0x00ff0000) |
						((((d0>>>0 )&0xff)*(s0&0x000000ff))           ) ) >>> 8;
				// TVPPsOperationAlphaBlend = TVPPS_ALPHABLEND
				int d1 = d0&0x00ff00ff;
				int d2 = d0&0x0000ff00;
				s0 = ((((((s0&0x00ff00ff)-d1)*a)>>>8)+d1)&0x00ff00ff)|((((((s0&0x0000ff00)-d2)*a)>>>8)+d2)&0x0000ff00);
				o[dt+x] = s0 | 0xff000000;
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void psMulBlend_HDA(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int a = s0 >>> 24;
				// TVPPsOperationMulBlend
				s0 = ( ((((d0>>>16)&0xff)*(s0&0x00ff0000))&0xff000000) |
						((((d0>>>8 )&0xff)*(s0&0x0000ff00))&0x00ff0000) |
						((((d0>>>0 )&0xff)*(s0&0x000000ff))           ) ) >>> 8;
				// TVPPsOperationAlphaBlend = TVPPS_ALPHABLEND
				int d1 = d0&0x00ff00ff;
				int d2 = d0&0x0000ff00;
				s0 = ((((((s0&0x00ff00ff)-d1)*a)>>>8)+d1)&0x00ff00ff)|((((((s0&0x0000ff00)-d2)*a)>>>8)+d2)&0x0000ff00);
				o[dt+x] = s0|(d0&0xff000000);
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void psMulBlend_o(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int a = ((s0>>>24)*opa)>>>8;
				// TVPPsOperationMulBlend
				s0 = ( ((((d0>>>16)&0xff)*(s0&0x00ff0000))&0xff000000) |
						((((d0>>>8 )&0xff)*(s0&0x0000ff00))&0x00ff0000) |
						((((d0>>>0 )&0xff)*(s0&0x000000ff))           ) ) >>> 8;
				// TVPPsOperationAlphaBlend = TVPPS_ALPHABLEND
				int d1 = d0&0x00ff00ff;
				int d2 = d0&0x0000ff00;
				s0 = ((((((s0&0x00ff00ff)-d1)*a)>>>8)+d1)&0x00ff00ff)|((((((s0&0x0000ff00)-d2)*a)>>>8)+d2)&0x0000ff00);
				o[dt+x] = s0 | 0xff000000;
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void psMulBlend_HDA_o(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int a = ((s0>>>24)*opa)>>>8;
				// TVPPsOperationMulBlend
				s0 = ( ((((d0>>>16)&0xff)*(s0&0x00ff0000))&0xff000000) |
						((((d0>>>8 )&0xff)*(s0&0x0000ff00))&0x00ff0000) |
						((((d0>>>0 )&0xff)*(s0&0x000000ff))           ) ) >>> 8;
				// TVPPsOperationAlphaBlend = TVPPS_ALPHABLEND
				int d1 = d0&0x00ff00ff;
				int d2 = d0&0x0000ff00;
				s0 = ((((((s0&0x00ff00ff)-d1)*a)>>>8)+d1)&0x00ff00ff)|((((((s0&0x0000ff00)-d2)*a)>>>8)+d2)&0x0000ff00);
				o[dt+x] = s0|(d0&0xff000000);
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void psScreenBlend(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int a = s0>>>24;
				// TVPPsOperationScreenBlend
				/* c = ((s+d-(s*d)/255)-d)*a + d = (s-(s*d)/255)*a + d */
				int sd1 = ( ((((d0>>>16)&0xff)*(s0&0x00ff0000))&0xff000000) |
						((((d0>>>0 )&0xff)*(s0&0x000000ff))           ) ) >>> 8;
				int sd2 = ( ((((d0>>>8 )&0xff)*(s0&0x0000ff00))&0x00ff0000) ) >>> 8;
				s0 = ((((((s0&0x00ff00ff)-sd1)*a)>>>8)+(d0&0x00ff00ff))&0x00ff00ff) |
					((((((s0&0x0000ff00)-sd2)*a)>>>8)+(d0&0x0000ff00))&0x0000ff00);
				o[dt+x] = s0 | 0xff000000;
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void psScreenBlend_HDA(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int a = s0 >>> 24;
				// TVPPsOperationScreenBlend
				/* c = ((s+d-(s*d)/255)-d)*a + d = (s-(s*d)/255)*a + d */
				int sd1 = ( ((((d0>>>16)&0xff)*(s0&0x00ff0000))&0xff000000) |
						((((d0>>>0 )&0xff)*(s0&0x000000ff))           ) ) >>> 8;
				int sd2 = ( ((((d0>>>8 )&0xff)*(s0&0x0000ff00))&0x00ff0000) ) >>> 8;
				s0 = ((((((s0&0x00ff00ff)-sd1)*a)>>>8)+(d0&0x00ff00ff))&0x00ff00ff) |
					((((((s0&0x0000ff00)-sd2)*a)>>>8)+(d0&0x0000ff00))&0x0000ff00);
				o[dt+x] = s0|(d0&0xff000000);
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void psScreenBlend_o(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int a = ((s0>>>24)*opa)>>>8;
				// TVPPsOperationScreenBlend
				/* c = ((s+d-(s*d)/255)-d)*a + d = (s-(s*d)/255)*a + d */
				int sd1 = ( ((((d0>>>16)&0xff)*(s0&0x00ff0000))&0xff000000) |
						((((d0>>>0 )&0xff)*(s0&0x000000ff))           ) ) >>> 8;
				int sd2 = ( ((((d0>>>8 )&0xff)*(s0&0x0000ff00))&0x00ff0000) ) >>> 8;
				s0 = ((((((s0&0x00ff00ff)-sd1)*a)>>>8)+(d0&0x00ff00ff))&0x00ff00ff) |
					((((((s0&0x0000ff00)-sd2)*a)>>>8)+(d0&0x0000ff00))&0x0000ff00);
				o[dt+x] = s0 | 0xff000000;
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void psScreenBlend_HDA_o(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int a = ((s0>>>24)*opa)>>>8;
				// TVPPsOperationScreenBlend
				/* c = ((s+d-(s*d)/255)-d)*a + d = (s-(s*d)/255)*a + d */
				int sd1 = ( ((((d0>>>16)&0xff)*(s0&0x00ff0000))&0xff000000) |
						((((d0>>>0 )&0xff)*(s0&0x000000ff))           ) ) >>> 8;
				int sd2 = ( ((((d0>>>8 )&0xff)*(s0&0x0000ff00))&0x00ff0000) ) >>> 8;
				s0 = ((((((s0&0x00ff00ff)-sd1)*a)>>>8)+(d0&0x00ff00ff))&0x00ff00ff) |
					((((((s0&0x0000ff00)-sd2)*a)>>>8)+(d0&0x0000ff00))&0x0000ff00);
				o[dt+x] = s0|(d0&0xff000000);
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void psOverlayBlend(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int a = s0>>>24;
				// TVPPsOperationOverlayBlend
				int n = (((d0&0x00808080)>>>7)+0x007f7f7f)^0x007f7f7f;
				int sa1, sa2, d1 = d0&n, s1 = s0&n;
				/* some tricks to avoid overflow (error between /255 and >>8) */
				s0 |= 0x00010101;
				sa1 = ( ((((d0>>>16)&0xff)*(s0&0x00ff0000))&0xff800000) |
						((((d0>>>0 )&0xff)*(s0&0x000000ff))           ) ) >>> 7;
				sa2 = ( ((((d0>>>8 )&0xff)*(s0&0x0000ff00))&0x00ff8000) ) >>> 7;
				s0 = ((sa1&~n)|(sa2&~n));
				s0 |= (((s1&0x00fe00fe)+(d1&0x00ff00ff))<<1)-(n&0x00ff00ff)-(sa1&n);
				s0 |= (((s1&0x0000fe00)+(d1&0x0000ff00))<<1)-(n&0x0000ff00)-(sa2&n);
				// TVPPsOperationAlphaBlend = TVPPS_ALPHABLEND
				d1 = d0&0x00ff00ff;
				int d2 = d0&0x0000ff00;
				s0 = ((((((s0&0x00ff00ff)-d1)*a)>>>8)+d1)&0x00ff00ff)|((((((s0&0x0000ff00)-d2)*a)>>>8)+d2)&0x0000ff00);
				o[dt+x] = s0 | 0xff000000;
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void psOverlayBlend_HDA(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int a = s0 >>> 24;
				// TVPPsOperationOverlayBlend
				int n = (((d0&0x00808080)>>>7)+0x007f7f7f)^0x007f7f7f;
				int sa1, sa2, d1 = d0&n, s1 = s0&n;
				/* some tricks to avoid overflow (error between /255 and >>8) */
				s0 |= 0x00010101;
				sa1 = ( ((((d0>>>16)&0xff)*(s0&0x00ff0000))&0xff800000) |
						((((d0>>>0 )&0xff)*(s0&0x000000ff))           ) ) >>> 7;
				sa2 = ( ((((d0>>>8 )&0xff)*(s0&0x0000ff00))&0x00ff8000) ) >>> 7;
				s0 = ((sa1&~n)|(sa2&~n));
				s0 |= (((s1&0x00fe00fe)+(d1&0x00ff00ff))<<1)-(n&0x00ff00ff)-(sa1&n);
				s0 |= (((s1&0x0000fe00)+(d1&0x0000ff00))<<1)-(n&0x0000ff00)-(sa2&n);
				// TVPPsOperationAlphaBlend = TVPPS_ALPHABLEND
				d1 = d0&0x00ff00ff;
				int d2 = d0&0x0000ff00;
				s0 = ((((((s0&0x00ff00ff)-d1)*a)>>>8)+d1)&0x00ff00ff)|((((((s0&0x0000ff00)-d2)*a)>>>8)+d2)&0x0000ff00);
				o[dt+x] = s0|(d0&0xff000000);
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void psOverlayBlend_o(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int a = ((s0>>>24)*opa)>>>8;
				// TVPPsOperationOverlayBlend
				int n = (((d0&0x00808080)>>>7)+0x007f7f7f)^0x007f7f7f;
				int sa1, sa2, d1 = d0&n, s1 = s0&n;
				/* some tricks to avoid overflow (error between /255 and >>8) */
				s0 |= 0x00010101;
				sa1 = ( ((((d0>>>16)&0xff)*(s0&0x00ff0000))&0xff800000) |
						((((d0>>>0 )&0xff)*(s0&0x000000ff))           ) ) >>> 7;
				sa2 = ( ((((d0>>>8 )&0xff)*(s0&0x0000ff00))&0x00ff8000) ) >>> 7;
				s0 = ((sa1&~n)|(sa2&~n));
				s0 |= (((s1&0x00fe00fe)+(d1&0x00ff00ff))<<1)-(n&0x00ff00ff)-(sa1&n);
				s0 |= (((s1&0x0000fe00)+(d1&0x0000ff00))<<1)-(n&0x0000ff00)-(sa2&n);
				// TVPPsOperationAlphaBlend = TVPPS_ALPHABLEND
				d1 = d0&0x00ff00ff;
				int d2 = d0&0x0000ff00;
				s0 = ((((((s0&0x00ff00ff)-d1)*a)>>>8)+d1)&0x00ff00ff)|((((((s0&0x0000ff00)-d2)*a)>>>8)+d2)&0x0000ff00);
				o[dt+x] = s0 | 0xff000000;
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void psOverlayBlend_HDA_o(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int a = ((s0>>>24)*opa)>>>8;
				// TVPPsOperationOverlayBlend
				int n = (((d0&0x00808080)>>>7)+0x007f7f7f)^0x007f7f7f;
				int sa1, sa2, d1 = d0&n, s1 = s0&n;
				/* some tricks to avoid overflow (error between /255 and >>8) */
				s0 |= 0x00010101;
				sa1 = ( ((((d0>>>16)&0xff)*(s0&0x00ff0000))&0xff800000) |
						((((d0>>>0 )&0xff)*(s0&0x000000ff))           ) ) >>> 7;
				sa2 = ( ((((d0>>>8 )&0xff)*(s0&0x0000ff00))&0x00ff8000) ) >>> 7;
				s0 = ((sa1&~n)|(sa2&~n));
				s0 |= (((s1&0x00fe00fe)+(d1&0x00ff00ff))<<1)-(n&0x00ff00ff)-(sa1&n);
				s0 |= (((s1&0x0000fe00)+(d1&0x0000ff00))<<1)-(n&0x0000ff00)-(sa2&n);
				// TVPPsOperationAlphaBlend = TVPPS_ALPHABLEND
				d1 = d0&0x00ff00ff;
				int d2 = d0&0x0000ff00;
				s0 = ((((((s0&0x00ff00ff)-d1)*a)>>>8)+d1)&0x00ff00ff)|((((((s0&0x0000ff00)-d2)*a)>>>8)+d2)&0x0000ff00);
				o[dt+x] = s0|(d0&0xff000000);
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void psHardLightBlend(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int a = s0>>>24;
				// TVPPsOperationHardLightBlend
				int n = (((s0&0x00808080)>>>7)+0x007f7f7f)^0x007f7f7f;
				int sa1, sa2, d1 = d0&n, s1 = s0&n;
				/* some tricks to avoid overflow (error between /255 and >>8) */
				d0 |= 0x00010101;
				sa1 = ( ((((d0>>>16)&0xff)*(s0&0x00ff0000))&0xff800000) |
						((((d0>>>0 )&0xff)*(s0&0x000000ff))           ) ) >>> 7;
				sa2 = ( ((((d0>>>8 )&0xff)*(s0&0x0000ff00))&0x00ff8000) ) >>> 7;
				s0 = ((sa1&~n)|(sa2&~n));
				s0 |= (((s1&0x00ff00ff)+(d1&0x00fe00fe))<<1)-(n&0x00ff00ff)-(sa1&n);
				s0 |= (((s1&0x0000ff00)+(d1&0x0000fe00))<<1)-(n&0x0000ff00)-(sa2&n);
				// TVPPsOperationAlphaBlend = TVPPS_ALPHABLEND
				d1 = d0&0x00ff00ff;
				int d2 = d0&0x0000ff00;
				s0 = ((((((s0&0x00ff00ff)-d1)*a)>>>8)+d1)&0x00ff00ff)|((((((s0&0x0000ff00)-d2)*a)>>>8)+d2)&0x0000ff00);
				o[dt+x] = s0 | 0xff000000;
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void psHardLightBlend_HDA(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int a = s0 >>> 24;
				// TVPPsOperationHardLightBlend
				int n = (((s0&0x00808080)>>>7)+0x007f7f7f)^0x007f7f7f;
				int sa1, sa2, d1 = d0&n, s1 = s0&n;
				/* some tricks to avoid overflow (error between /255 and >>8) */
				d0 |= 0x00010101;
				sa1 = ( ((((d0>>>16)&0xff)*(s0&0x00ff0000))&0xff800000) |
						((((d0>>>0 )&0xff)*(s0&0x000000ff))           ) ) >>> 7;
				sa2 = ( ((((d0>>>8 )&0xff)*(s0&0x0000ff00))&0x00ff8000) ) >>> 7;
				s0 = ((sa1&~n)|(sa2&~n));
				s0 |= (((s1&0x00ff00ff)+(d1&0x00fe00fe))<<1)-(n&0x00ff00ff)-(sa1&n);
				s0 |= (((s1&0x0000ff00)+(d1&0x0000fe00))<<1)-(n&0x0000ff00)-(sa2&n);
				// TVPPsOperationAlphaBlend = TVPPS_ALPHABLEND
				d1 = d0&0x00ff00ff;
				int d2 = d0&0x0000ff00;
				s0 = ((((((s0&0x00ff00ff)-d1)*a)>>>8)+d1)&0x00ff00ff)|((((((s0&0x0000ff00)-d2)*a)>>>8)+d2)&0x0000ff00);
				o[dt+x] = s0|(d0&0xff000000);
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void psHardLightBlend_o(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int a = ((s0>>>24)*opa)>>>8;
				// TVPPsOperationHardLightBlend
				int n = (((s0&0x00808080)>>>7)+0x007f7f7f)^0x007f7f7f;
				int sa1, sa2, d1 = d0&n, s1 = s0&n;
				/* some tricks to avoid overflow (error between /255 and >>8) */
				d0 |= 0x00010101;
				sa1 = ( ((((d0>>>16)&0xff)*(s0&0x00ff0000))&0xff800000) |
						((((d0>>>0 )&0xff)*(s0&0x000000ff))           ) ) >>> 7;
				sa2 = ( ((((d0>>>8 )&0xff)*(s0&0x0000ff00))&0x00ff8000) ) >>> 7;
				s0 = ((sa1&~n)|(sa2&~n));
				s0 |= (((s1&0x00ff00ff)+(d1&0x00fe00fe))<<1)-(n&0x00ff00ff)-(sa1&n);
				s0 |= (((s1&0x0000ff00)+(d1&0x0000fe00))<<1)-(n&0x0000ff00)-(sa2&n);
				// TVPPsOperationAlphaBlend = TVPPS_ALPHABLEND
				d1 = d0&0x00ff00ff;
				int d2 = d0&0x0000ff00;
				s0 = ((((((s0&0x00ff00ff)-d1)*a)>>>8)+d1)&0x00ff00ff)|((((((s0&0x0000ff00)-d2)*a)>>>8)+d2)&0x0000ff00);
				o[dt+x] = s0 | 0xff000000;
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void psHardLightBlend_HDA_o(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int a = ((s0>>>24)*opa)>>>8;
				// TVPPsOperationHardLightBlend
				int n = (((s0&0x00808080)>>>7)+0x007f7f7f)^0x007f7f7f;
				int sa1, sa2, d1 = d0&n, s1 = s0&n;
				/* some tricks to avoid overflow (error between /255 and >>8) */
				d0 |= 0x00010101;
				sa1 = ( ((((d0>>>16)&0xff)*(s0&0x00ff0000))&0xff800000) |
				((((d0>>>0 )&0xff)*(s0&0x000000ff))           ) ) >>> 7;
				sa2 = ( ((((d0>>>8 )&0xff)*(s0&0x0000ff00))&0x00ff8000) ) >>> 7;
				s0 = ((sa1&~n)|(sa2&~n));
				s0 |= (((s1&0x00ff00ff)+(d1&0x00fe00fe))<<1)-(n&0x00ff00ff)-(sa1&n);
				s0 |= (((s1&0x0000ff00)+(d1&0x0000fe00))<<1)-(n&0x0000ff00)-(sa2&n);
				// TVPPsOperationAlphaBlend = TVPPS_ALPHABLEND
				d1 = d0&0x00ff00ff;
				int d2 = d0&0x0000ff00;
				s0 = ((((((s0&0x00ff00ff)-d1)*a)>>>8)+d1)&0x00ff00ff)|((((((s0&0x0000ff00)-d2)*a)>>>8)+d2)&0x0000ff00);
				o[dt+x] = s0|(d0&0xff000000);
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void psSoftLightBlend(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		if( PsTableSoftLight == null ) createTables();
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int a = s0>>>24;
				// TVPPsOperationSoftLightBlend
				s0 = (PsTableSoftLight[(s0>>>16)&0xff][(d0>>>16)&0xff]<<16) |
				(PsTableSoftLight[(s0>>>8 )&0xff][(d0>>>8 )&0xff]<<8 ) |
				(PsTableSoftLight[(s0>>>0 )&0xff][(d0>>>0 )&0xff]<<0 );
				// TVPPsOperationAlphaBlend = TVPPS_ALPHABLEND
				int d1 = d0&0x00ff00ff;
				int d2 = d0&0x0000ff00;
				s0 = ((((((s0&0x00ff00ff)-d1)*a)>>>8)+d1)&0x00ff00ff)|((((((s0&0x0000ff00)-d2)*a)>>>8)+d2)&0x0000ff00);
				o[dt+x] = s0 | 0xff000000;
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void psSoftLightBlend_HDA(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		if( PsTableSoftLight == null ) createTables();
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int a = s0 >>> 24;
				// TVPPsOperationSoftLightBlend
				s0 = (PsTableSoftLight[(s0>>>16)&0xff][(d0>>>16)&0xff]<<16) |
				(PsTableSoftLight[(s0>>>8 )&0xff][(d0>>>8 )&0xff]<<8 ) |
				(PsTableSoftLight[(s0>>>0 )&0xff][(d0>>>0 )&0xff]<<0 );
				// TVPPsOperationAlphaBlend = TVPPS_ALPHABLEND
				int d1 = d0&0x00ff00ff;
				int d2 = d0&0x0000ff00;
				s0 = ((((((s0&0x00ff00ff)-d1)*a)>>>8)+d1)&0x00ff00ff)|((((((s0&0x0000ff00)-d2)*a)>>>8)+d2)&0x0000ff00);
				o[dt+x] = s0|(d0&0xff000000);
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void psSoftLightBlend_o(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		if( PsTableSoftLight == null ) createTables();
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int a = ((s0>>>24)*opa)>>>8;
				// TVPPsOperationSoftLightBlend
				s0 = (PsTableSoftLight[(s0>>>16)&0xff][(d0>>>16)&0xff]<<16) |
				(PsTableSoftLight[(s0>>>8 )&0xff][(d0>>>8 )&0xff]<<8 ) |
				(PsTableSoftLight[(s0>>>0 )&0xff][(d0>>>0 )&0xff]<<0 );
				// TVPPsOperationAlphaBlend = TVPPS_ALPHABLEND
				int d1 = d0&0x00ff00ff;
				int d2 = d0&0x0000ff00;
				s0 = ((((((s0&0x00ff00ff)-d1)*a)>>>8)+d1)&0x00ff00ff)|((((((s0&0x0000ff00)-d2)*a)>>>8)+d2)&0x0000ff00);
				o[dt+x] = s0 | 0xff000000;
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void psSoftLightBlend_HDA_o(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		if( PsTableSoftLight == null ) createTables();
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int a = ((s0>>>24)*opa)>>>8;
				// TVPPsOperationSoftLightBlend
				s0 = (PsTableSoftLight[(s0>>>16)&0xff][(d0>>>16)&0xff]<<16) |
				(PsTableSoftLight[(s0>>>8 )&0xff][(d0>>>8 )&0xff]<<8 ) |
				(PsTableSoftLight[(s0>>>0 )&0xff][(d0>>>0 )&0xff]<<0 );
				// TVPPsOperationAlphaBlend = TVPPS_ALPHABLEND
				int d1 = d0&0x00ff00ff;
				int d2 = d0&0x0000ff00;
				s0 = ((((((s0&0x00ff00ff)-d1)*a)>>>8)+d1)&0x00ff00ff)|((((((s0&0x0000ff00)-d2)*a)>>>8)+d2)&0x0000ff00);
				o[dt+x] = s0|(d0&0xff000000);
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void psColorDodgeBlend(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		if( PsTableColorDodge == null ) createTables();
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int a = s0>>>24;
				// TVPPsOperationColorDodgeBlend
				s0 =((((int)PsTableColorDodge[(s0>>>16)&0xff][(d0>>>16)&0xff])&0xff)<<16) |
				((((int)PsTableColorDodge[(s0>>>8 )&0xff][(d0>>>8 )&0xff])&0xff)<<8 ) |
				((((int)PsTableColorDodge[(s0>>>0 )&0xff][(d0>>>0 )&0xff])&0xff)<<0 );
				// TVPPsOperationAlphaBlend = TVPPS_ALPHABLEND
				int d1 = d0&0x00ff00ff;
				int d2 = d0&0x0000ff00;
				s0 = ((((((s0&0x00ff00ff)-d1)*a)>>>8)+d1)&0x00ff00ff)|((((((s0&0x0000ff00)-d2)*a)>>>8)+d2)&0x0000ff00);
				o[dt+x] = s0 | 0xff000000;
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void psColorDodgeBlend_HDA(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		if( PsTableColorDodge == null ) createTables();
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int a = s0 >>> 24;
				// TVPPsOperationColorDodgeBlend
				s0 =((((int)PsTableColorDodge[(s0>>>16)&0xff][(d0>>>16)&0xff])&0xff)<<16) |
				((((int)PsTableColorDodge[(s0>>>8 )&0xff][(d0>>>8 )&0xff])&0xff)<<8 ) |
				((((int)PsTableColorDodge[(s0>>>0 )&0xff][(d0>>>0 )&0xff])&0xff)<<0 );
				// TVPPsOperationAlphaBlend = TVPPS_ALPHABLEND
				int d1 = d0&0x00ff00ff;
				int d2 = d0&0x0000ff00;
				s0 = ((((((s0&0x00ff00ff)-d1)*a)>>>8)+d1)&0x00ff00ff)|((((((s0&0x0000ff00)-d2)*a)>>>8)+d2)&0x0000ff00);
				o[dt+x] = s0|(d0&0xff000000);
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void psColorDodgeBlend_o(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		if( PsTableColorDodge == null ) createTables();
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int a = ((s0>>>24)*opa)>>>8;
				// TVPPsOperationColorDodgeBlend
				s0 =((((int)PsTableColorDodge[(s0>>>16)&0xff][(d0>>>16)&0xff])&0xff)<<16) |
				((((int)PsTableColorDodge[(s0>>>8 )&0xff][(d0>>>8 )&0xff])&0xff)<<8 ) |
				((((int)PsTableColorDodge[(s0>>>0 )&0xff][(d0>>>0 )&0xff])&0xff)<<0 );
				// TVPPsOperationAlphaBlend = TVPPS_ALPHABLEND
				int d1 = d0&0x00ff00ff;
				int d2 = d0&0x0000ff00;
				s0 = ((((((s0&0x00ff00ff)-d1)*a)>>>8)+d1)&0x00ff00ff)|((((((s0&0x0000ff00)-d2)*a)>>>8)+d2)&0x0000ff00);
				o[dt+x] = s0 | 0xff000000;
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void psColorDodgeBlend_HDA_o(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		if( PsTableColorDodge == null ) createTables();
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int a = ((s0>>>24)*opa)>>>8;
				// TVPPsOperationColorDodgeBlend
				s0 =((((int)PsTableColorDodge[(s0>>>16)&0xff][(d0>>>16)&0xff])&0xff)<<16) |
				((((int)PsTableColorDodge[(s0>>>8 )&0xff][(d0>>>8 )&0xff])&0xff)<<8 ) |
				((((int)PsTableColorDodge[(s0>>>0 )&0xff][(d0>>>0 )&0xff])&0xff)<<0 );
				// TVPPsOperationAlphaBlend = TVPPS_ALPHABLEND
				int d1 = d0&0x00ff00ff;
				int d2 = d0&0x0000ff00;
				s0 = ((((((s0&0x00ff00ff)-d1)*a)>>>8)+d1)&0x00ff00ff)|((((((s0&0x0000ff00)-d2)*a)>>>8)+d2)&0x0000ff00);
				o[dt+x] = s0|(d0&0xff000000);
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void psColorDodge5Blend(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		if( PsTableColorDodge == null ) createTables();
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int a = s0>>>24;
				// TVPPS_FADESRC
				s0 = ((((s0&0x00ff00ff)*a)>>>8)&0x00ff00ff)|((((s0&0x0000ff00)*a)>>>8)&0x0000ff00);
				// TVPPsOperationColorDodgeBlend
				s0 =((((int)PsTableColorDodge[(s0>>>16)&0xff][(d0>>>16)&0xff])&0xff)<<16) |
				((((int)PsTableColorDodge[(s0>>>8 )&0xff][(d0>>>8 )&0xff])&0xff)<<8 ) |
				((((int)PsTableColorDodge[(s0>>>0 )&0xff][(d0>>>0 )&0xff])&0xff)<<0 );
				o[dt+x] = s0 | 0xff000000;
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void psColorDodge5Blend_HDA(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		if( PsTableColorDodge == null ) createTables();
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int a = s0 >>> 24;
				// TVPPS_FADESRC
				s0 = ((((s0&0x00ff00ff)*a)>>>8)&0x00ff00ff)|((((s0&0x0000ff00)*a)>>>8)&0x0000ff00);
				// TVPPsOperationColorDodgeBlend
				s0 =((((int)PsTableColorDodge[(s0>>>16)&0xff][(d0>>>16)&0xff])&0xff)<<16) |
				((((int)PsTableColorDodge[(s0>>>8 )&0xff][(d0>>>8 )&0xff])&0xff)<<8 ) |
				((((int)PsTableColorDodge[(s0>>>0 )&0xff][(d0>>>0 )&0xff])&0xff)<<0 );
				o[dt+x] = s0|(d0&0xff000000);
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void psColorDodge5Blend_o(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		if( PsTableColorDodge == null ) createTables();
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int a = ((s0>>>24)*opa)>>>8;
				// TVPPS_FADESRC
				s0 = ((((s0&0x00ff00ff)*a)>>>8)&0x00ff00ff)|((((s0&0x0000ff00)*a)>>>8)&0x0000ff00);
				// TVPPsOperationColorDodgeBlend
				s0 =((((int)PsTableColorDodge[(s0>>>16)&0xff][(d0>>>16)&0xff])&0xff)<<16) |
				((((int)PsTableColorDodge[(s0>>>8 )&0xff][(d0>>>8 )&0xff])&0xff)<<8 ) |
				((((int)PsTableColorDodge[(s0>>>0 )&0xff][(d0>>>0 )&0xff])&0xff)<<0 );
				o[dt+x] = s0 | 0xff000000;
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void psColorDodge5Blend_HDA_o(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		if( PsTableColorDodge == null ) createTables();
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int a = ((s0>>>24)*opa)>>>8;
				// TVPPS_FADESRC
				s0 = ((((s0&0x00ff00ff)*a)>>>8)&0x00ff00ff)|((((s0&0x0000ff00)*a)>>>8)&0x0000ff00);
				// TVPPsOperationColorDodgeBlend
				s0 =((((int)PsTableColorDodge[(s0>>>16)&0xff][(d0>>>16)&0xff])&0xff)<<16) |
				((((int)PsTableColorDodge[(s0>>>8 )&0xff][(d0>>>8 )&0xff])&0xff)<<8 ) |
				((((int)PsTableColorDodge[(s0>>>0 )&0xff][(d0>>>0 )&0xff])&0xff)<<0 );
				o[dt+x] = s0|(d0&0xff000000);
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void psColorBurnBlend(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		if( PsTableColorBurn == null ) createTables();
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int a = s0>>>24;
				// TVPPsOperationColorBurnBlend
				s0 = (PsTableColorBurn[(s0>>>16)&0xff][(d0>>>16)&0xff]<<16) |
				(PsTableColorBurn[(s0>>>8 )&0xff][(d0>>>8 )&0xff]<<8 ) |
				(PsTableColorBurn[(s0>>>0 )&0xff][(d0>>>0 )&0xff]<<0 );
				// TVPPsOperationAlphaBlend = TVPPS_ALPHABLEND
				int d1 = d0&0x00ff00ff;
				int d2 = d0&0x0000ff00;
				s0 = ((((((s0&0x00ff00ff)-d1)*a)>>>8)+d1)&0x00ff00ff)|((((((s0&0x0000ff00)-d2)*a)>>>8)+d2)&0x0000ff00);
				o[dt+x] = s0 | 0xff000000;
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void psColorBurnBlend_HDA(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		if( PsTableColorBurn == null ) createTables();
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int a = s0 >>> 24;
				// TVPPsOperationColorBurnBlend
				s0 = (PsTableColorBurn[(s0>>>16)&0xff][(d0>>>16)&0xff]<<16) |
				(PsTableColorBurn[(s0>>>8 )&0xff][(d0>>>8 )&0xff]<<8 ) |
				(PsTableColorBurn[(s0>>>0 )&0xff][(d0>>>0 )&0xff]<<0 );
				// TVPPsOperationAlphaBlend = TVPPS_ALPHABLEND
				int d1 = d0&0x00ff00ff;
				int d2 = d0&0x0000ff00;
				s0 = ((((((s0&0x00ff00ff)-d1)*a)>>>8)+d1)&0x00ff00ff)|((((((s0&0x0000ff00)-d2)*a)>>>8)+d2)&0x0000ff00);
				o[dt+x] = s0|(d0&0xff000000);
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void psColorBurnBlend_o(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		if( PsTableColorBurn == null ) createTables();
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int a = ((s0>>>24)*opa)>>>8;
				// TVPPsOperationColorBurnBlend
				s0 = (PsTableColorBurn[(s0>>>16)&0xff][(d0>>>16)&0xff]<<16) |
				(PsTableColorBurn[(s0>>>8 )&0xff][(d0>>>8 )&0xff]<<8 ) |
				(PsTableColorBurn[(s0>>>0 )&0xff][(d0>>>0 )&0xff]<<0 );
				// TVPPsOperationAlphaBlend = TVPPS_ALPHABLEND
				int d1 = d0&0x00ff00ff;
				int d2 = d0&0x0000ff00;
				s0 = ((((((s0&0x00ff00ff)-d1)*a)>>>8)+d1)&0x00ff00ff)|((((((s0&0x0000ff00)-d2)*a)>>>8)+d2)&0x0000ff00);
				o[dt+x] = s0 | 0xff000000;
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void psColorBurnBlend_HDA_o(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		if( PsTableColorBurn == null ) createTables();
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int a = ((s0>>>24)*opa)>>>8;
				// TVPPsOperationColorBurnBlend
				s0 = (PsTableColorBurn[(s0>>>16)&0xff][(d0>>>16)&0xff]<<16) |
				(PsTableColorBurn[(s0>>>8 )&0xff][(d0>>>8 )&0xff]<<8 ) |
				(PsTableColorBurn[(s0>>>0 )&0xff][(d0>>>0 )&0xff]<<0 );
				// TVPPsOperationAlphaBlend = TVPPS_ALPHABLEND
				int d1 = d0&0x00ff00ff;
				int d2 = d0&0x0000ff00;
				s0 = ((((((s0&0x00ff00ff)-d1)*a)>>>8)+d1)&0x00ff00ff)|((((((s0&0x0000ff00)-d2)*a)>>>8)+d2)&0x0000ff00);
				o[dt+x] = s0|(d0&0xff000000);
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void psLightenBlend(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int a = s0>>>24;
				// TVPPsOperationLightenBlend
				int n = (((~d0&s0)<<1)+((~d0^s0)&0x00fefefe))&0x01010100;
				n = ((n>>>8)+0x007f7f7f)^0x007f7f7f;
				/* n=mask (d<s:0xff, d>=s:0x00) */
				s0 = (s0&n)|(d0&~n);
				// TVPPsOperationAlphaBlend = TVPPS_ALPHABLEND
				int d1 = d0&0x00ff00ff;
				int d2 = d0&0x0000ff00;
				s0 = ((((((s0&0x00ff00ff)-d1)*a)>>>8)+d1)&0x00ff00ff)|((((((s0&0x0000ff00)-d2)*a)>>>8)+d2)&0x0000ff00);
				o[dt+x] = s0 | 0xff000000;
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void psLightenBlend_HDA(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int a = s0 >>> 24;
				// TVPPsOperationLightenBlend
				int n = (((~d0&s0)<<1)+((~d0^s0)&0x00fefefe))&0x01010100;
				n = ((n>>>8)+0x007f7f7f)^0x007f7f7f;
				/* n=mask (d<s:0xff, d>=s:0x00) */
				s0 = (s0&n)|(d0&~n);
				// TVPPsOperationAlphaBlend = TVPPS_ALPHABLEND
				int d1 = d0&0x00ff00ff;
				int d2 = d0&0x0000ff00;
				s0 = ((((((s0&0x00ff00ff)-d1)*a)>>>8)+d1)&0x00ff00ff)|((((((s0&0x0000ff00)-d2)*a)>>>8)+d2)&0x0000ff00);
				o[dt+x] = s0|(d0&0xff000000);
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void psLightenBlend_o(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int a = ((s0>>>24)*opa)>>>8;
				// TVPPsOperationLightenBlend
				int n = (((~d0&s0)<<1)+((~d0^s0)&0x00fefefe))&0x01010100;
				n = ((n>>>8)+0x007f7f7f)^0x007f7f7f;
				/* n=mask (d<s:0xff, d>=s:0x00) */
				s0 = (s0&n)|(d0&~n);
				// TVPPsOperationAlphaBlend = TVPPS_ALPHABLEND
				int d1 = d0&0x00ff00ff;
				int d2 = d0&0x0000ff00;
				s0 = ((((((s0&0x00ff00ff)-d1)*a)>>>8)+d1)&0x00ff00ff)|((((((s0&0x0000ff00)-d2)*a)>>>8)+d2)&0x0000ff00);
				o[dt+x] = s0 | 0xff000000;
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void psLightenBlend_HDA_o(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int a = ((s0>>>24)*opa)>>>8;
				// TVPPsOperationLightenBlend
				int n = (((~d0&s0)<<1)+((~d0^s0)&0x00fefefe))&0x01010100;
				n = ((n>>>8)+0x007f7f7f)^0x007f7f7f;
				/* n=mask (d<s:0xff, d>=s:0x00) */
				s0 = (s0&n)|(d0&~n);
				// TVPPsOperationAlphaBlend = TVPPS_ALPHABLEND
				int d1 = d0&0x00ff00ff;
				int d2 = d0&0x0000ff00;
				s0 = ((((((s0&0x00ff00ff)-d1)*a)>>>8)+d1)&0x00ff00ff)|((((((s0&0x0000ff00)-d2)*a)>>>8)+d2)&0x0000ff00);
				o[dt+x] = s0|(d0&0xff000000);
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void psDarkenBlend(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int a = s0>>>24;
				// TVPPsOperationDarkenBlend
				int n = (((~d0&s0)<<1)+((~d0^s0)&0x00fefefe))&0x01010100;
				n = ((n>>>8)+0x007f7f7f)^0x007f7f7f;
				/* n=mask (d<s:0xff, d>=s:0x00) */
				s0 = (d0&n)|(s0&~n);
				// TVPPsOperationAlphaBlend = TVPPS_ALPHABLEND
				int d1 = d0&0x00ff00ff;
				int d2 = d0&0x0000ff00;
				s0 = ((((((s0&0x00ff00ff)-d1)*a)>>>8)+d1)&0x00ff00ff)|((((((s0&0x0000ff00)-d2)*a)>>>8)+d2)&0x0000ff00);
				o[dt+x] = s0 | 0xff000000;
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void psDarkenBlend_HDA(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int a = s0 >>> 24;
				// TVPPsOperationDarkenBlend
				int n = (((~d0&s0)<<1)+((~d0^s0)&0x00fefefe))&0x01010100;
				n = ((n>>>8)+0x007f7f7f)^0x007f7f7f;
				/* n=mask (d<s:0xff, d>=s:0x00) */
				s0 = (d0&n)|(s0&~n);
				// TVPPsOperationAlphaBlend = TVPPS_ALPHABLEND
				int d1 = d0&0x00ff00ff;
				int d2 = d0&0x0000ff00;
				s0 = ((((((s0&0x00ff00ff)-d1)*a)>>>8)+d1)&0x00ff00ff)|((((((s0&0x0000ff00)-d2)*a)>>>8)+d2)&0x0000ff00);
				o[dt+x] = s0|(d0&0xff000000);
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void psDarkenBlend_o(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int a = ((s0>>>24)*opa)>>>8;
				// TVPPsOperationDarkenBlend
				int n = (((~d0&s0)<<1)+((~d0^s0)&0x00fefefe))&0x01010100;
				n = ((n>>>8)+0x007f7f7f)^0x007f7f7f;
				/* n=mask (d<s:0xff, d>=s:0x00) */
				s0 = (d0&n)|(s0&~n);
				// TVPPsOperationAlphaBlend = TVPPS_ALPHABLEND
				int d1 = d0&0x00ff00ff;
				int d2 = d0&0x0000ff00;
				s0 = ((((((s0&0x00ff00ff)-d1)*a)>>>8)+d1)&0x00ff00ff)|((((((s0&0x0000ff00)-d2)*a)>>>8)+d2)&0x0000ff00);
				o[dt+x] = s0 | 0xff000000;
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void psDarkenBlend_HDA_o(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int a = ((s0>>>24)*opa)>>>8;
				// TVPPsOperationDarkenBlend
				int n = (((~d0&s0)<<1)+((~d0^s0)&0x00fefefe))&0x01010100;
				n = ((n>>>8)+0x007f7f7f)^0x007f7f7f;
				/* n=mask (d<s:0xff, d>=s:0x00) */
				s0 = (d0&n)|(s0&~n);
				// TVPPsOperationAlphaBlend = TVPPS_ALPHABLEND
				int d1 = d0&0x00ff00ff;
				int d2 = d0&0x0000ff00;
				s0 = ((((((s0&0x00ff00ff)-d1)*a)>>>8)+d1)&0x00ff00ff)|((((((s0&0x0000ff00)-d2)*a)>>>8)+d2)&0x0000ff00);
				o[dt+x] = s0|(d0&0xff000000);
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void psDiffBlend(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int a = s0>>>24;
				// TVPPsOperationDiffBlend
				int n = (((~d0&s0)<<1)+((~d0^s0)&0x00fefefe))&0x01010100;
				n = ((n>>>8)+0x007f7f7f)^0x007f7f7f;
				/* n=mask (d<s:0xff, d>=s:0x00) */
				s0 = ((s0&n)-(d0&n))|((d0&~n)-(s0&~n));
				// TVPPsOperationAlphaBlend = TVPPS_ALPHABLEND
				int d1 = d0&0x00ff00ff;
				int d2 = d0&0x0000ff00;
				s0 = ((((((s0&0x00ff00ff)-d1)*a)>>>8)+d1)&0x00ff00ff)|((((((s0&0x0000ff00)-d2)*a)>>>8)+d2)&0x0000ff00);
				o[dt+x] = s0 | 0xff000000;
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void psDiffBlend_HDA(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int a = s0 >>> 24;
				// TVPPsOperationDiffBlend
				int n = (((~d0&s0)<<1)+((~d0^s0)&0x00fefefe))&0x01010100;
				n = ((n>>>8)+0x007f7f7f)^0x007f7f7f;
				/* n=mask (d<s:0xff, d>=s:0x00) */
				s0 = ((s0&n)-(d0&n))|((d0&~n)-(s0&~n));
				// TVPPsOperationAlphaBlend = TVPPS_ALPHABLEND
				int d1 = d0&0x00ff00ff;
				int d2 = d0&0x0000ff00;
				s0 = ((((((s0&0x00ff00ff)-d1)*a)>>>8)+d1)&0x00ff00ff)|((((((s0&0x0000ff00)-d2)*a)>>>8)+d2)&0x0000ff00);
				o[dt+x] = s0|(d0&0xff000000);
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void psDiffBlend_o(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int a = ((s0>>>24)*opa)>>>8;
				// TVPPsOperationDiffBlend
				int n = (((~d0&s0)<<1)+((~d0^s0)&0x00fefefe))&0x01010100;
				n = ((n>>>8)+0x007f7f7f)^0x007f7f7f;
				/* n=mask (d<s:0xff, d>=s:0x00) */
				s0 = ((s0&n)-(d0&n))|((d0&~n)-(s0&~n));
				// TVPPsOperationAlphaBlend = TVPPS_ALPHABLEND
				int d1 = d0&0x00ff00ff;
				int d2 = d0&0x0000ff00;
				s0 = ((((((s0&0x00ff00ff)-d1)*a)>>>8)+d1)&0x00ff00ff)|((((((s0&0x0000ff00)-d2)*a)>>>8)+d2)&0x0000ff00);
				o[dt+x] = s0 | 0xff000000;
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void psDiffBlend_HDA_o(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int a = ((s0>>>24)*opa)>>>8;
				// TVPPsOperationDiffBlend
				int n = (((~d0&s0)<<1)+((~d0^s0)&0x00fefefe))&0x01010100;
				n = ((n>>>8)+0x007f7f7f)^0x007f7f7f;
				/* n=mask (d<s:0xff, d>=s:0x00) */
				s0 = ((s0&n)-(d0&n))|((d0&~n)-(s0&~n));
				// TVPPsOperationAlphaBlend = TVPPS_ALPHABLEND
				int d1 = d0&0x00ff00ff;
				int d2 = d0&0x0000ff00;
				s0 = ((((((s0&0x00ff00ff)-d1)*a)>>>8)+d1)&0x00ff00ff)|((((((s0&0x0000ff00)-d2)*a)>>>8)+d2)&0x0000ff00);
				o[dt+x] = s0|(d0&0xff000000);
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void psDiff5Blend(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int a = s0>>>24;
				// TVPPS_FADESRC
				s0 = ((((s0&0x00ff00ff)*a)>>>8)&0x00ff00ff)|((((s0&0x0000ff00)*a)>>>8)&0x0000ff00);
				// TVPPsOperationDiff5Blend
				int n = (((~d0&s0)<<1)+((~d0^s0)&0x00fefefe))&0x01010100;
				n = ((n>>>8)+0x007f7f7f)^0x007f7f7f;
				/* n=mask (d<s:0xff, d>=s:0x00) */
				s0 = ((s0&n)-(d0&n))|((d0&~n)-(s0&~n));
				o[dt+x] = s0;
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void psDiff5Blend_HDA(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int a = s0 >>> 24;
				// TVPPS_FADESRC
				s0 = ((((s0&0x00ff00ff)*a)>>>8)&0x00ff00ff)|((((s0&0x0000ff00)*a)>>>8)&0x0000ff00);
				// TVPPsOperationDiff5Blend
				int n = (((~d0&s0)<<1)+((~d0^s0)&0x00fefefe))&0x01010100;
				n = ((n>>>8)+0x007f7f7f)^0x007f7f7f;
				/* n=mask (d<s:0xff, d>=s:0x00) */
				s0 = ((s0&n)-(d0&n))|((d0&~n)-(s0&~n));
				o[dt+x] = s0|(d0&0xff000000);
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void psDiff5Blend_o(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int a = ((s0>>>24)*opa)>>>8;
				// TVPPS_FADESRC
				s0 = ((((s0&0x00ff00ff)*a)>>>8)&0x00ff00ff)|((((s0&0x0000ff00)*a)>>>8)&0x0000ff00);
				// TVPPsOperationDiff5Blend
				int n = (((~d0&s0)<<1)+((~d0^s0)&0x00fefefe))&0x01010100;
				n = ((n>>>8)+0x007f7f7f)^0x007f7f7f;
				/* n=mask (d<s:0xff, d>=s:0x00) */
				s0 = ((s0&n)-(d0&n))|((d0&~n)-(s0&~n));
				o[dt+x] = s0;
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void psDiff5Blend_HDA_o(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int a = ((s0>>>24)*opa)>>>8;
				// TVPPS_FADESRC
				s0 = ((((s0&0x00ff00ff)*a)>>>8)&0x00ff00ff)|((((s0&0x0000ff00)*a)>>>8)&0x0000ff00);
				// TVPPsOperationDiff5Blend
				int n = (((~d0&s0)<<1)+((~d0^s0)&0x00fefefe))&0x01010100;
				n = ((n>>>8)+0x007f7f7f)^0x007f7f7f;
				/* n=mask (d<s:0xff, d>=s:0x00) */
				s0 = ((s0&n)-(d0&n))|((d0&~n)-(s0&~n));
				o[dt+x] = s0|(d0&0xff000000);
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void psExclusionBlend(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int a = s0>>>24;
				// TVPPsOperationExclusionBlend
				/* c = ((s+d-(s*d*2)/255)-d)*a + d = (s-(s*d*2)/255)*a + d */
				int sd1 = ( ((((d0>>>16)&0xff)*((s0&0x00ff0000)>>>7))&0x01ff0000) |
						((((d0>>>0 )&0xff)*( s0&0x000000ff    ))>>>7        ) );
				int sd2 = ( ((((d0>>>8 )&0xff)*( s0&0x0000ff00))&0x00ff8000) ) >>> 7;
				s0 = ((((((s0&0x00ff00ff)-sd1)*a)>>>8)+(d0&0x00ff00ff))&0x00ff00ff) |
				((((((s0&0x0000ff00)-sd2)*a)>>>8)+(d0&0x0000ff00))&0x0000ff00);
				o[dt+x] = s0 | 0xff000000;
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void psExclusionBlend_HDA(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int a = s0 >>> 24;
				// TVPPsOperationExclusionBlend
				/* c = ((s+d-(s*d*2)/255)-d)*a + d = (s-(s*d*2)/255)*a + d */
				int sd1 = ( ((((d0>>>16)&0xff)*((s0&0x00ff0000)>>>7))&0x01ff0000) |
						((((d0>>>0 )&0xff)*( s0&0x000000ff    ))>>>7        ) );
				int sd2 = ( ((((d0>>>8 )&0xff)*( s0&0x0000ff00))&0x00ff8000) ) >>> 7;
				s0 = ((((((s0&0x00ff00ff)-sd1)*a)>>>8)+(d0&0x00ff00ff))&0x00ff00ff) |
				((((((s0&0x0000ff00)-sd2)*a)>>>8)+(d0&0x0000ff00))&0x0000ff00);
				o[dt+x] = s0|(d0&0xff000000);
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void psExclusionBlend_o(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int a = ((s0>>>24)*opa)>>>8;
				// TVPPsOperationExclusionBlend
				/* c = ((s+d-(s*d*2)/255)-d)*a + d = (s-(s*d*2)/255)*a + d */
				int sd1 = ( ((((d0>>>16)&0xff)*((s0&0x00ff0000)>>>7))&0x01ff0000) |
						((((d0>>>0 )&0xff)*( s0&0x000000ff    ))>>>7        ) );
				int sd2 = ( ((((d0>>>8 )&0xff)*( s0&0x0000ff00))&0x00ff8000) ) >>> 7;
				s0 = ((((((s0&0x00ff00ff)-sd1)*a)>>>8)+(d0&0x00ff00ff))&0x00ff00ff) |
				((((((s0&0x0000ff00)-sd2)*a)>>>8)+(d0&0x0000ff00))&0x0000ff00);
				o[dt+x] = s0 | 0xff000000;
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void psExclusionBlend_HDA_o(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				int a = ((s0>>>24)*opa)>>>8;
				// TVPPsOperationExclusionBlend
				/* c = ((s+d-(s*d*2)/255)-d)*a + d = (s-(s*d*2)/255)*a + d */
				int sd1 = ( ((((d0>>>16)&0xff)*((s0&0x00ff0000)>>>7))&0x01ff0000) |
						((((d0>>>0 )&0xff)*( s0&0x000000ff    ))>>>7        ) );
				int sd2 = ( ((((d0>>>8 )&0xff)*( s0&0x0000ff00))&0x00ff8000) ) >>> 7;
				s0 = ((((((s0&0x00ff00ff)-sd1)*a)>>>8)+(d0&0x00ff00ff))&0x00ff00ff) |
				((((((s0&0x0000ff00)-sd2)*a)>>>8)+(d0&0x0000ff00))&0x0000ff00);
				o[dt+x] = s0|(d0&0xff000000);
			}
			st += srcStride;
			dt += dstStride;
		}
	}


	static void copyMain(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				o[dt+x] = (s0 & 0x00ffffff) | (d0 & 0xff000000);
			}
			st += srcStride;
			dt += dstStride;
		}
	}

	static void copyMask(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
		for( int y = 0; y < sh; y++ ) {
			for( int x = 0; x < sw; x++ ) {
				int s0 = s[st+x];
				int d0 = d[dt+x];
				o[dt+x] = (s0 & 0xff000000) | (d0 & 0x00ffffff);
			}
			st += srcStride;
			dt += dstStride;
		}
	}
}
