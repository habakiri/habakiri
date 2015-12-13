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

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.CompositeContext;
import java.awt.RenderingHints;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import java.io.FileNotFoundException;
import java.io.PrintStream;

public class CustomOperationComposite implements Composite {

	/* Opacity の影響を受けないものは、事前に生成して置いてそれを返す方がいいな
	 * */
	public static byte[] OpacityOnOpacityTable;
	public static byte[] NegativeMulTable;

	public static byte[] OpacityOnOpacityTable65;
	public static byte[] NegativeMulTable65;
	public static int[] RecipTable256;
	public static short[] RecipTable256_16;
	public static byte[] DivTable;
	public static byte[][] PsTableSoftLight;
	public static byte[][] PsTableColorDodge;
	public static byte[][] PsTableColorBurn;
	public static void initialize() {
		OpacityOnOpacityTable = new byte[256*256];
		NegativeMulTable = new byte[256*256];
		RecipTable256 = new int[256];
		RecipTable256_16 = new short[256];
		DivTable = new byte[256*256];
		OpacityOnOpacityTable65 = new byte[65*256];
		NegativeMulTable65 = new byte[65*256];

		int a,b;
		for( a = 0; a < 256; a++ ) {
			for( b = 0; b < 256; b++ ) {
				float c;
				int ci;
				int addr = b*256+ a;
				if( a != 0 ) {
					float at = a/255.0f, bt = b/255.0f;
					c = bt / at;
					c /= (1.0f - bt + c);
					ci = (int)(c*255);
					if(ci>=256) ci = 255; /* will not overflow... */
				} else {
					ci=255;
				}
				OpacityOnOpacityTable[addr]=(byte)(ci&0xff);
					/* higher byte of the index is source opacity */
					/* lower byte of the index is destination opacity */
				NegativeMulTable[addr] = (byte)(( 255 - (255-a)*(255-b)/ 255 )&0xff);
			}
		}

		for( a = 0; a < 256; a++ ) {
			for( b = 0; b < 65; b++ ) {
				int ci;
				int addr = b*256+ a;
				int bb;
				if( a != 0 ) {
					float at = a / 255.0f;
					float bt = b / 64.0f;
					float c = bt / at;
					c /= (1.0f - bt + c);
					ci = (int)(c*255);
					if(ci>=256) ci = 255; /* will not overflow... */
				} else {
					ci = 255;
				}
				// higher byte of the index is source opacity
				// lower byte of the index is destination opacity
				OpacityOnOpacityTable65[addr]= (byte)(ci & 0xff);

				bb = b * 4;
				if(bb > 255) bb = 255;
				NegativeMulTable65[addr] = (byte)( ( 255 - (255-a)*(255-bb)/ 255 ) & 0xff );
			}
		}

		RecipTable256[0] = 65536;
		RecipTable256_16[0] = 0x7fff;
		for( int i = 1; i < 256; i++ ) {
			int v = 65536/i;
			RecipTable256[i] = v;
			RecipTable256_16[i] = v > 0x7fff ? 0x7fff : (short)v;
		}
		for( b =0; b < 256; b++) {
			DivTable[(0<<8)+b] = 0;
			for( a = 1; a < 256; a++) {
				int tmp = (int)(b*255/a);
				if(tmp > 255) tmp = 255;
				DivTable[(a<<8)+b] = (byte)(tmp&0xff);
			}
		}
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
		/* サイズが大きくなりすぎるので、埋め込む事は出来ない様子
		 * バイナリに書き出して、それを読み込むか？ でも計算する方が早いかも
		try {
			PrintStream output = new PrintStream("OpacityOnOpacityTable.java");
			output.print("package jp.kirikiri.tvp2.env;\n");
			output.print("class OpacityOnOpacityTable{\n");
			output.print("static public final byte[] OpacityOnOpacityTable = {\n");
			for( int i = 0; i < (256*256); i++ ) {
				output.print(String.format("%4d",OpacityOnOpacityTable[i]));
				if( i != (256*256-1) ) output.print(", " );
				if( ((i+1) % 16) == 0 )  output.print("\n" );
			}
			output.print("};\n" );
			output.print("};\n" );

			output = new PrintStream("NegativeMulTable.java");
			output.print("package jp.kirikiri.tvp2.env;\n");
			output.print("class NegativeMulTable{\n");
			output.print("static public final byte[] NegativeMulTable = {\n");
			for( int i = 0; i < (256*256); i++ ) {
				output.print(String.format("%4d",NegativeMulTable[i]));
				if( i != (256*256-1) ) output.print(", " );
				if( ((i+1) % 16) == 0 )  output.print("\n" );
			}
			output.print("};\n" );
			output.print("};\n" );

			output = new PrintStream("OpacityOnOpacityTable65.java");
			output.print("package jp.kirikiri.tvp2.env;\n");
			output.print("class OpacityOnOpacityTable65{\n");
			output.print("static public final byte[] OpacityOnOpacityTable65 = {\n");
			for( int i = 0; i < (65*256); i++ ) {
				output.print(String.format("%4d",OpacityOnOpacityTable65[i]));
				if( i != (65*256-1) ) output.print(", " );
				if( ((i+1) % 16) == 0 )  output.print("\n" );
			}
			output.print("};\n" );
			output.print("};\n" );

			output = new PrintStream("NegativeMulTable65.java");
			output.print("package jp.kirikiri.tvp2.env;\n");
			output.print("class NegativeMulTable65{\n");
			output.print("static public final byte[] NegativeMulTable65 = {\n");
			for( int i = 0; i < (65*256); i++ ) {
				output.print(String.format("%4d",NegativeMulTable65[i]));
				if( i != (65*256-1) ) output.print(", " );
				if( ((i+1) % 16) == 0 )  output.print("\n" );
			}
			output.print("};\n" );
			output.print("};\n" );
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		*/

		/*
		RecipTable256 = new int[256];
		RecipTable256_16 = new short[256];
		DivTable = new byte[256*256];
		PsTableSoftLight = new byte[256][];
		PsTableColorDodge = new byte[256][];
		PsTableColorBurn = new byte[256][];
		*/
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

	private int mMethod;
	private int mOpacity;
	private boolean mHoldAlpha;
	public CustomOperationComposite( int method, int opacity, boolean hda ) {
		mMethod = method;
		mOpacity = opacity;
		mHoldAlpha = hda;
	}
	@Override
	public CompositeContext createContext(ColorModel srcColorModel, ColorModel dstColorModel, RenderingHints hints) {
		int method = mMethod;
		switch( method ) {
		case bmCopy:
			if( mOpacity == 255 && mHoldAlpha ) { // CopyColor
				return new CopyColor();
			} else if( !mHoldAlpha ) { // ConstAlphaBlend
				return new ConstAlphaBlend(mOpacity);
			} else { // ConstAlphaBlend_HDA
				return new ConstAlphaBlend_HDA(mOpacity);
			}
		case bmCopyOnAlpha:
			if( mOpacity == 255 ) { // CopyOpaqueImage
				return new CopyOpaqueImage();
			} else { // ConstAlphaBlend_d
				return new ConstAlphaBlend_d(mOpacity);
			}
		case bmAlpha:
			if( mOpacity == 255 ) {
				if( !mHoldAlpha ) { // AlphaBlend
					return new AlphaBlend();
				} else { // AlphaBlend_HDA
					return new AlphaBlend_HDA();
				}
			} else {
				if( !mHoldAlpha ) { // AlphaBlend_o
					return new AlphaBlend_o(mOpacity);
				} else { // AlphaBlend_HDA_o
					return new AlphaBlend_HDA_o(mOpacity);
				}
			}
		case bmAlphaOnAlpha:
			if( mOpacity == 255 ) { // AlphaBlend_d
				return new AlphaBlend_d();
			} else { // AlphaBlend_do
				return new AlphaBlend_do(mOpacity);
			}
		case bmAdd:
			if( mOpacity == 255 ) {
				if( !mHoldAlpha ) { // AddBlend
					return new AddBlend();
				} else { // AddBlend_HDA
					return new AddBlend_HDA();
				}
			} else {
				if( !mHoldAlpha ) { // AddBlend_o
					return new AddBlend_o(mOpacity);
				} else { // AddBlend_HDA_o
					return new AddBlend_HDA_o(mOpacity);
				}
			}
		case bmSub:
			if( mOpacity == 255 ) {
				if( !mHoldAlpha ) { // SubBlend
					return new SubBlend();
				} else { // SubBlend_HDA
					return new SubBlend_HDA();
				}
			} else {
				if( !mHoldAlpha ) { // SubBlend_o
					return new SubBlend_o(mOpacity);
				} else { // SubBlend_HDA_o
					return new SubBlend_HDA_o(mOpacity);
				}
			}
		case bmMul:
			if( mOpacity == 255 ) {
				if( !mHoldAlpha ) { // MulBlend
					return new MulBlend();
				} else { // MulBlend_HDA
					return new MulBlend_HDA();
				}
			} else {
				if( !mHoldAlpha ) { // MulBlend_o
					return new MulBlend_o(mOpacity);
				} else { // MulBlend_HDA_o
					return new MulBlend_HDA_o(mOpacity);
				}
			}
		case bmDodge:
			if( mOpacity == 255 ) {
				if( !mHoldAlpha ) { // ColorDodgeBlend
					return new ColorDodgeBlend();
				} else { // ColorDodgeBlend_HDA
					return new ColorDodgeBlend_HDA();
				}
			} else {
				if( !mHoldAlpha ) { // ColorDodgeBlend_o
					return new ColorDodgeBlend_o(mOpacity);
				} else { // ColorDodgeBlend_HDA_o
					return new ColorDodgeBlend_HDA_o(mOpacity);
				}
			}
		case bmDarken:
			if( mOpacity == 255 ) {
				if( !mHoldAlpha ) { // DarkenBlend
					return new DarkenBlend();
				} else { // DarkenBlend_HDA
					return new DarkenBlend_HDA();
				}
			} else {
				if( !mHoldAlpha ) { // DarkenBlend_o
					return new DarkenBlend_o(mOpacity);
				} else { // DarkenBlend_HDA_o
					return new DarkenBlend_HDA_o(mOpacity);
				}
			}
		case bmLighten:
			if( mOpacity == 255 ) {
				if( !mHoldAlpha ) { // LightenBlend
					return new LightenBlend();
				} else { // LightenBlend_HDA
					return new LightenBlend_HDA();
				}
			} else {
				if( !mHoldAlpha ) { // LightenBlend_o
					return new LightenBlend_o(mOpacity);
				} else { // LightenBlend_HDA_o
					return new LightenBlend_HDA_o(mOpacity);
				}
			}
		case bmScreen:
			if( mOpacity == 255 ) {
				if( !mHoldAlpha ) { // ScreenBlend
					return new ScreenBlend();
				} else { // ScreenBlend_HDA
					return new ScreenBlend_HDA();
				}
			} else {
				if( !mHoldAlpha ) { // ScreenBlend_o
					return new ScreenBlend_o(mOpacity);
				} else { // ScreenBlend_HDA_o
					return new ScreenBlend_HDA_o(mOpacity);
				}
			}
		case bmAddAlpha:
			if( mOpacity == 255 ) {
				if( !mHoldAlpha ) { // AdditiveAlphaBlend
					return new AdditiveAlphaBlend();
				} else { // AdditiveAlphaBlend_HDA
					return new AdditiveAlphaBlend_HDA();
				}
			} else {
				if( !mHoldAlpha ) { // AdditiveAlphaBlend_o
					return new AdditiveAlphaBlend_o(mOpacity);
				} else { // AdditiveAlphaBlend_HDA_o
					return new AdditiveAlphaBlend_HDA_o(mOpacity);
				}
			}
		case bmAddAlphaOnAddAlpha:
			if( mOpacity == 255 ) { // AdditiveAlphaBlend_a
				return new AdditiveAlphaBlend_a();
			} else { // AdditiveAlphaBlend_ao
				return new AdditiveAlphaBlend_ao(mOpacity);
			}
		case bmAddAlphaOnAlpha:
			// additive alpha on simple alpha
			// Not yet implemented
			break;
		case bmAlphaOnAddAlpha:
			if( mOpacity == 255 ) { // AlphaBlend_a
				return new AlphaBlend_a();
			} else { // AlphaBlend_ao
				return new AlphaBlend_ao(mOpacity);
			}
		case bmCopyOnAddAlpha:
			if( mOpacity == 255 ) { // CopyOpaqueImage
				return new CopyOpaqueImage();
			} else { // ConstAlphaBlend_a
				return new ConstAlphaBlend_a(mOpacity);
			}
		case bmPsNormal:
			if( mOpacity == 255 ) {
				if( !mHoldAlpha ) { // PsAlphaBlend
					return new PsAlphaBlend();
				} else { // PsAlphaBlend_HDA
					return new PsAlphaBlend_HDA();
				}
			} else {
				if( !mHoldAlpha ) { // PsAlphaBlend_o
					return new PsAlphaBlend_o(mOpacity);
				} else { // PsAlphaBlend_HDA_o
					return new PsAlphaBlend_HDA_o(mOpacity);
				}
			}
		case bmPsAdditive:
			if( mOpacity == 255 ) {
				if( !mHoldAlpha ) { // PsAddBlend
					return new PsAddBlend();
				} else { // PsAddBlend_HDA
					return new PsAddBlend_HDA();
				}
			} else {
				if( !mHoldAlpha ) { // PsAddBlend_o
					return new PsAddBlend_o(mOpacity);
				} else { // PsAddBlend_HDA_o
					return new PsAddBlend_HDA_o(mOpacity);
				}
			}
		case bmPsSubtractive:
			if( mOpacity == 255 ) {
				if( !mHoldAlpha ) { // PsSubBlend
					return new PsSubBlend();
				} else { // PsSubBlend_HDA
					return new PsSubBlend_HDA();
				}
			} else {
				if( !mHoldAlpha ) { // PsSubBlend_o
					return new PsSubBlend_o(mOpacity);
				} else { // PsSubBlend_HDA_o
					return new PsSubBlend_HDA_o(mOpacity);
				}
			}
		case bmPsMultiplicative:
			if( mOpacity == 255 ) {
				if( !mHoldAlpha ) { // PsMulBlend
					return new PsMulBlend();
				} else { // PsMulBlend_HDA
					return new PsMulBlend_HDA();
				}
			} else {
				if( !mHoldAlpha ) { // PsMulBlend_o
					return new PsMulBlend_o(mOpacity);
				} else { // PsMulBlend_HDA_o
					return new PsMulBlend_HDA_o(mOpacity);
				}
			}
		case bmPsScreen:
			if( mOpacity == 255 ) {
				if( !mHoldAlpha ) { // PsScreenBlend
					return new PsScreenBlend();
				} else { // PsScreenBlend_HDA
					return new PsScreenBlend_HDA();
				}
			} else {
				if( !mHoldAlpha ) { // PsScreenBlend_o
					return new PsScreenBlend_o(mOpacity);
				} else { // PsScreenBlend_HDA_o
					return new PsScreenBlend_HDA_o(mOpacity);
				}
			}
		case bmPsOverlay:
			if( mOpacity == 255 ) {
				if( !mHoldAlpha ) { // PsOverlayBlend
					return new PsOverlayBlend();
				} else { // PsOverlayBlend_HDA
					return new PsOverlayBlend_HDA();
				}
			} else {
				if( !mHoldAlpha ) { // PsOverlayBlend_o
					return new PsOverlayBlend_o(mOpacity);
				} else { // PsOverlayBlend_HDA_o
					return new PsOverlayBlend_HDA_o(mOpacity);
				}
			}
		case bmPsHardLight:
			if( mOpacity == 255 ) {
				if( !mHoldAlpha ) { // PsHardLightBlend
					return new PsHardLightBlend();
				} else { // PsHardLightBlend_HDA
					return new PsHardLightBlend_HDA();
				}
			} else {
				if( !mHoldAlpha ) { // PsHardLightBlend_o
					return new PsHardLightBlend_o(mOpacity);
				} else { // PsHardLightBlend_HDA_o
					return new PsHardLightBlend_HDA_o(mOpacity);
				}
			}
		case bmPsSoftLight:
			if( mOpacity == 255 ) {
				if( !mHoldAlpha ) { // PsSoftLightBlend
					return new PsSoftLightBlend();
				} else { // PsSoftLightBlend_HDA
					return new PsSoftLightBlend_HDA();
				}
			} else {
				if( !mHoldAlpha ) { // PsSoftLightBlend_o
					return new PsSoftLightBlend_o(mOpacity);
				} else { // PsSoftLightBlend_HDA_o
					return new PsSoftLightBlend_HDA_o(mOpacity);
				}
			}
		case bmPsColorDodge:
			if( mOpacity == 255 ) {
				if( !mHoldAlpha ) { // PsColorDodgeBlend
					return new PsColorDodgeBlend();
				} else { // PsColorDodgeBlend_HDA
					return new PsColorDodgeBlend_HDA();
				}
			} else {
				if( !mHoldAlpha ) { // PsColorDodgeBlend_o
					return new PsColorDodgeBlend_o(mOpacity);
				} else { // PsColorDodgeBlend_HDA_o
					return new PsColorDodgeBlend_HDA_o(mOpacity);
				}
			}
		case bmPsColorDodge5:
			if( mOpacity == 255 ) {
				if( !mHoldAlpha ) { // PsColorDodge5Blend
					return new PsColorDodge5Blend();
				} else { // PsColorDodge5Blend_HDA
					return new PsColorDodge5Blend_HDA();
				}
			} else {
				if( !mHoldAlpha ) { // PsColorDodge5Blend_o
					return new PsColorDodge5Blend_o(mOpacity);
				} else { // PsColorDodge5Blend_HDA_o
					return new PsColorDodge5Blend_HDA_o(mOpacity);
				}
			}
		case bmPsColorBurn:
			if( mOpacity == 255 ) {
				if( !mHoldAlpha ) { // PsColorBurnBlend
					return new PsColorBurnBlend();
				} else { // PsColorBurnBlend_HDA
					return new PsColorBurnBlend_HDA();
				}
			} else {
				if( !mHoldAlpha ) { // PsColorBurnBlend_o
					return new PsColorBurnBlend_o(mOpacity);
				} else { // PsColorBurnBlend_HDA_o
					return new PsColorBurnBlend_HDA_o(mOpacity);
				}
			}
		case bmPsLighten:
			if( mOpacity == 255 ) {
				if( !mHoldAlpha ) { // PsLightenBlend
					return new PsLightenBlend();
				} else { // PsLightenBlend_HDA
					return new PsLightenBlend_HDA();
				}
			} else {
				if( !mHoldAlpha ) { // PsLightenBlend_o
					return new PsLightenBlend_o(mOpacity);
				} else { // PsLightenBlend_HDA_o
					return new PsLightenBlend_HDA_o(mOpacity);
				}
			}
		case bmPsDarken:
			if( mOpacity == 255 ) {
				if( !mHoldAlpha ) { // PsDarkenBlend
					return new PsDarkenBlend();
				} else { // PsDarkenBlend_HDA
					return new PsDarkenBlend_HDA();
				}
			} else {
				if( !mHoldAlpha ) { // PsDarkenBlend_o
					return new PsDarkenBlend_o(mOpacity);
				} else { // PsDarkenBlend_HDA_o
					return new PsDarkenBlend_HDA_o(mOpacity);
				}
			}
		case bmPsDifference:
			if( mOpacity == 255 ) {
				if( !mHoldAlpha ) { // PsDiffBlend
					return new PsDiffBlend();
				} else { // PsDiffBlend_HDA
					return new PsDiffBlend_HDA();
				}
			} else {
				if( !mHoldAlpha ) { // PsDiffBlend_o
					return new PsDiffBlend_o(mOpacity);
				} else { // PsDiffBlend_HDA_o
					return new PsDiffBlend_HDA_o(mOpacity);
				}
			}
		case bmPsDifference5:
			if( mOpacity == 255 ) {
				if( !mHoldAlpha ) { // PsDiff5Blend
					return new PsDiff5Blend();
				} else { // PsDiff5Blend_HDA
					return new PsDiff5Blend_HDA();
				}
			} else {
				if( !mHoldAlpha ) { // PsDiff5Blend_o
					return new PsDiff5Blend_o(mOpacity);
				} else { // PsDiff5Blend_HDA_o
					return new PsDiff5Blend_HDA_o(mOpacity);
				}
			}
		case bmPsExclusion:
			if( mOpacity == 255 ) {
				if( !mHoldAlpha ) { // PsExclusionBlend
					return new PsExclusionBlend();
				} else { // PsExclusionBlend_HDA
					return new PsExclusionBlend_HDA();
				}
			} else {
				if( !mHoldAlpha ) { // PsExclusionBlend_o
					return new PsExclusionBlend_o(mOpacity);
				} else { // PsExclusionBlend_HDA_o
					return new PsExclusionBlend_HDA_o(mOpacity);
				}
			}
		case copyMain:
			return new CopyMain();
		case copyMask:
			return new CopyMask();
		}
		return AlphaComposite.Src.createContext(srcColorModel, dstColorModel, hints);
	}

	static abstract class BlendContext implements CompositeContext {
		private int mOpacity;
		public BlendContext() { mOpacity = 255; }
		public BlendContext( int opa ) { mOpacity = opa; }
		@Override
		public void dispose() {}
		@Override
		public void compose(Raster src, Raster dst, WritableRaster dstOut) {
			if( dst != dstOut ) {
				dstOut.setDataElements(0, 0, dst);
			}

			DataBufferInt srcBuff = (DataBufferInt)src.getDataBuffer();
			int[] s = srcBuff.getData();
			DataBufferInt dstBuff = (DataBufferInt)dst.getDataBuffer();
			int[] d = dstBuff.getData();
			DataBufferInt outBuff = (DataBufferInt)dstOut.getDataBuffer();
			int[] o = outBuff.getData();

			int sW = src.getWidth();
			int sH = src.getHeight();

			SampleModel ssm = src.getSampleModel();
			SampleModel osm = dstOut.getSampleModel();
			int sx = src.getSampleModelTranslateX();
			int sy = src.getSampleModelTranslateY();
			if( sx < 0 ) sx = -sx;
			if( sy < 0 ) sy = -sy;
			int srcStride;
			if( ssm instanceof SinglePixelPackedSampleModel ) {
				srcStride = ((SinglePixelPackedSampleModel)ssm).getScanlineStride();
			} else {
				srcStride = src.getWidth();
			}
			int dx = dstOut.getSampleModelTranslateX();
			int dy = dstOut.getSampleModelTranslateY();
			if( dx < 0 ) dx = -dx;
			if( dy < 0 ) dy = -dy;
			int dstStride;
			if( osm instanceof SinglePixelPackedSampleModel ) {
				dstStride = ((SinglePixelPackedSampleModel)osm).getScanlineStride();
			} else {
				dstStride = dstOut.getWidth();
			}
			int st = sx + sy*srcStride;
			int dt = dx + dy*dstStride;
			//sH -= sy;
			//sW -= sx;
			blt( s, d, o, srcStride, dstStride, sW, sH, st, dt, mOpacity );
		}
		protected abstract void blt( int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa );
	}

	static class CopyColor extends BlendContext {
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
			for( int y = 0; y < sh; y++ ) {
				for( int x = 0; x < sw; x++ ) {
					o[dt+x] = (d[dt+x]&0xff000000) | (s[st+x]&0x00ffffff);
				}
				st += srcStride;
				dt += dstStride;
			}
		}
	}

	static class ConstAlphaBlend extends BlendContext {
		public ConstAlphaBlend( int opa ) { super(opa); }
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class ConstAlphaBlend_HDA extends BlendContext {
		public ConstAlphaBlend_HDA( int opa ) { super(opa); }
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class CopyOpaqueImage extends BlendContext {
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
			for( int y = 0; y < sh; y++ ) {
				for( int x = 0; x < sw; x++ ) {
					o[dt+x] = s[st+x] | 0xff000000;
				}
				st += srcStride;
				dt += dstStride;
			}
		}
	}

	static class ConstAlphaBlend_d extends BlendContext {
		public ConstAlphaBlend_d( int opa ) { super(opa); }
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class AlphaBlend extends BlendContext {
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class AlphaBlend_HDA extends BlendContext {
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class AlphaBlend_o extends BlendContext {
		public AlphaBlend_o( int opa ) { super(opa); }
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class AlphaBlend_HDA_o extends BlendContext {
		public AlphaBlend_HDA_o( int opa ) { super(opa); }
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class AlphaBlend_d extends BlendContext {
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class AlphaBlend_do extends BlendContext {
		public AlphaBlend_do( int opa ) { super(opa); }
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class AddBlend extends BlendContext {
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class AddBlend_HDA extends BlendContext {
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}
	static class AddBlend_o extends BlendContext {
		public AddBlend_o( int opa ) { super(opa); }
		protected void blt( int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa ) {
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
	}

	static class AddBlend_HDA_o extends BlendContext {
		public AddBlend_HDA_o( int opa ) { super(opa); }
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class SubBlend extends BlendContext {
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class SubBlend_HDA extends BlendContext {
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class SubBlend_o extends BlendContext {
		public SubBlend_o( int opa ) { super(opa); }
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class SubBlend_HDA_o extends BlendContext {
		public SubBlend_HDA_o( int opa ) { super(opa); }
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class MulBlend extends BlendContext {
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class MulBlend_HDA extends BlendContext {
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class MulBlend_o extends BlendContext {
		public MulBlend_o( int opa ) { super(opa); }
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class MulBlend_HDA_o extends BlendContext {
		public MulBlend_HDA_o( int opa ) { super(opa); }
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class ColorDodgeBlend extends BlendContext {
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class ColorDodgeBlend_HDA extends BlendContext {
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class ColorDodgeBlend_o extends BlendContext {
		public ColorDodgeBlend_o( int opa ) { super(opa); }
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class ColorDodgeBlend_HDA_o extends BlendContext {
		public ColorDodgeBlend_HDA_o( int opa ) { super(opa); }
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class DarkenBlend extends BlendContext {
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class DarkenBlend_HDA extends BlendContext {
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class DarkenBlend_o extends BlendContext {
		public DarkenBlend_o( int opa ) { super(opa); }
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class DarkenBlend_HDA_o extends BlendContext {
		public DarkenBlend_HDA_o( int opa ) { super(opa); }
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class LightenBlend extends BlendContext {
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class LightenBlend_HDA extends BlendContext {
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class LightenBlend_o extends BlendContext {
		public LightenBlend_o( int opa ) { super(opa); }
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class LightenBlend_HDA_o extends BlendContext {
		public LightenBlend_HDA_o( int opa ) { super(opa); }
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class ScreenBlend extends BlendContext {
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class ScreenBlend_HDA extends BlendContext {
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class ScreenBlend_o extends BlendContext {
		public ScreenBlend_o( int opa ) { super(opa); }
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class ScreenBlend_HDA_o extends BlendContext {
		public ScreenBlend_HDA_o( int opa ) { super(opa); }
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class AdditiveAlphaBlend extends BlendContext {
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class AdditiveAlphaBlend_HDA extends BlendContext {
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class AdditiveAlphaBlend_o extends BlendContext {
		public AdditiveAlphaBlend_o( int opa ) { super(opa); }
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class AdditiveAlphaBlend_HDA_o extends BlendContext {
		public AdditiveAlphaBlend_HDA_o( int opa ) { super(opa); }
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class AdditiveAlphaBlend_a extends BlendContext {
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class AdditiveAlphaBlend_ao extends BlendContext {
		public AdditiveAlphaBlend_ao( int opa ) { super(opa); }
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class AlphaBlend_a extends BlendContext {
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class AlphaBlend_ao extends BlendContext {
		public AlphaBlend_ao( int opa ) { super(opa); }
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class ConstAlphaBlend_a extends BlendContext {
		public ConstAlphaBlend_a( int opa ) { super(opa); }
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class PsAlphaBlend extends BlendContext {
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class PsAlphaBlend_HDA extends BlendContext {
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class PsAlphaBlend_o extends BlendContext {
		public PsAlphaBlend_o( int opa ) { super(opa); }
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class PsAlphaBlend_HDA_o extends BlendContext {
		public PsAlphaBlend_HDA_o( int opa ) { super(opa); }
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class PsAddBlend extends BlendContext {
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class PsAddBlend_HDA extends BlendContext {
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class PsAddBlend_o extends BlendContext {
		public PsAddBlend_o( int opa ) { super(opa); }

		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class PsAddBlend_HDA_o extends BlendContext {
		public PsAddBlend_HDA_o( int opa ) { super(opa); }
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class PsSubBlend extends BlendContext {
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class PsSubBlend_HDA extends BlendContext {
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class PsSubBlend_o extends BlendContext {
		public PsSubBlend_o( int opa ) { super(opa); }
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class PsSubBlend_HDA_o extends BlendContext {
		public PsSubBlend_HDA_o( int opa ) { super(opa); }
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class PsMulBlend extends BlendContext {
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class PsMulBlend_HDA extends BlendContext {
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class PsMulBlend_o extends BlendContext {
		public PsMulBlend_o( int opa ) { super(opa); }
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class PsMulBlend_HDA_o extends BlendContext {
		public PsMulBlend_HDA_o( int opa ) { super(opa); }
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class PsScreenBlend extends BlendContext {
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class PsScreenBlend_HDA extends BlendContext {
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class PsScreenBlend_o extends BlendContext {
		public PsScreenBlend_o( int opa ) { super(opa); }
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class PsScreenBlend_HDA_o extends BlendContext {
		public PsScreenBlend_HDA_o( int opa ) { super(opa); }
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class PsOverlayBlend extends BlendContext {
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class PsOverlayBlend_HDA extends BlendContext {
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class PsOverlayBlend_o extends BlendContext {
		public PsOverlayBlend_o( int opa ) { super(opa); }
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class PsOverlayBlend_HDA_o extends BlendContext {
		public PsOverlayBlend_HDA_o( int opa ) { super(opa); }
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class PsHardLightBlend extends BlendContext {
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class PsHardLightBlend_HDA extends BlendContext {
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class PsHardLightBlend_o extends BlendContext {
		public PsHardLightBlend_o( int opa ) { super(opa); }
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class PsHardLightBlend_HDA_o extends BlendContext {
		public PsHardLightBlend_HDA_o( int opa ) { super(opa); }
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class PsSoftLightBlend extends BlendContext {
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class PsSoftLightBlend_HDA extends BlendContext {
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class PsSoftLightBlend_o extends BlendContext {
		public PsSoftLightBlend_o( int opa ) { super(opa); }
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class PsSoftLightBlend_HDA_o extends BlendContext {
		public PsSoftLightBlend_HDA_o( int opa ) { super(opa); }
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class PsColorDodgeBlend extends BlendContext {
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class PsColorDodgeBlend_HDA extends BlendContext {
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class PsColorDodgeBlend_o extends BlendContext {
		public PsColorDodgeBlend_o( int opa ) { super(opa); }
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class PsColorDodgeBlend_HDA_o extends BlendContext {
		public PsColorDodgeBlend_HDA_o( int opa ) { super(opa); }
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class PsColorDodge5Blend extends BlendContext {
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class PsColorDodge5Blend_HDA extends BlendContext {
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class PsColorDodge5Blend_o extends BlendContext {
		public PsColorDodge5Blend_o( int opa ) { super(opa); }
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class PsColorDodge5Blend_HDA_o extends BlendContext {
		public PsColorDodge5Blend_HDA_o( int opa ) { super(opa); }
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class PsColorBurnBlend extends BlendContext {
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class PsColorBurnBlend_HDA extends BlendContext {
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class PsColorBurnBlend_o extends BlendContext {
		public PsColorBurnBlend_o( int opa ) { super(opa); }
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class PsColorBurnBlend_HDA_o extends BlendContext {
		public PsColorBurnBlend_HDA_o( int opa ) { super(opa); }
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class PsLightenBlend extends BlendContext {
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class PsLightenBlend_HDA extends BlendContext {
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class PsLightenBlend_o extends BlendContext {
		public PsLightenBlend_o( int opa ) { super(opa); }
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class PsLightenBlend_HDA_o extends BlendContext {
		public PsLightenBlend_HDA_o( int opa ) { super(opa); }
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class PsDarkenBlend extends BlendContext {
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class PsDarkenBlend_HDA extends BlendContext {
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class PsDarkenBlend_o extends BlendContext {
		public PsDarkenBlend_o( int opa ) { super(opa); }
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class PsDarkenBlend_HDA_o extends BlendContext {
		public PsDarkenBlend_HDA_o( int opa ) { super(opa); }
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class PsDiffBlend extends BlendContext {
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class PsDiffBlend_HDA extends BlendContext {
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class PsDiffBlend_o extends BlendContext {
		public PsDiffBlend_o( int opa ) { super(opa); }
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class PsDiffBlend_HDA_o extends BlendContext {
		public PsDiffBlend_HDA_o( int opa ) { super(opa); }
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class PsDiff5Blend extends BlendContext {
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class PsDiff5Blend_HDA extends BlendContext {
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class PsDiff5Blend_o extends BlendContext {
		public PsDiff5Blend_o( int opa ) { super(opa); }
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class PsDiff5Blend_HDA_o extends BlendContext {
		public PsDiff5Blend_HDA_o( int opa ) { super(opa); }
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class PsExclusionBlend extends BlendContext {
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class PsExclusionBlend_HDA extends BlendContext {
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class PsExclusionBlend_o extends BlendContext {
		public PsExclusionBlend_o( int opa ) { super(opa); }
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}

	static class PsExclusionBlend_HDA_o extends BlendContext {
		public PsExclusionBlend_HDA_o( int opa ) { super(opa); }
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}


	static class CopyMain extends BlendContext {
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
	}
	static class CopyMask extends BlendContext {
		@Override
		protected void blt(int[] s, int[] d, int[] o, int srcStride, int dstStride, int sw, int sh, int st, int dt, int opa) {
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
}
