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
