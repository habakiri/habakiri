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
/**
 * プラットフォーム依存(Android/Java 2D)描画処理を隠蔽する
 */
package jp.kirikiri.tvp2env;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;


import jp.kirikiri.tvp2.visual.LayerNI;
import jp.kirikiri.tvp2.visual.LayerType;
import jp.kirikiri.tvp2.visual.Point;
import jp.kirikiri.tvp2.visual.Rect;

/**
 *
 *
 */
public class DrawTarget {
	private Graphics mGraphcs;
	private WindowForm mWindow;
	private CustomOperationComposite mOpaqueComposite;

	public DrawTarget() {
		mOpaqueComposite = new CustomOperationComposite( LayerNI.bmCopyOnAlpha, 255, false );
	}

	public void setGraphics( Graphics g, WindowForm w ) {
		mGraphcs = g;
		mWindow = w;
	}

	public void drawImage(int x, int y, NativeImageBuffer src, Rect refrect, int type, int opacity) {
		try {
			Graphics2D g = (Graphics2D)mGraphcs;
			if ((g != null) && (src.getImage() != null)) {
				if( type == LayerType.ltOpaque ) {
					if( opacity == 255 ) {
						 // 自前のものを使うのはパフォーマンスが気になるが、すべて不透明として扱う為にやむなし
						g.setComposite( mOpaqueComposite );
						//g.setComposite( AlphaComposite.Src );
					} else {
						//g.setComposite( AlphaComposite.getInstance( AlphaComposite.SRC, ((float)opacity)/255.0f ) );
						g.setComposite( new CustomOperationComposite( LayerNI.bmCopyOnAlpha, opacity, false ) );
					}
				} else {
					if( opacity == 255 ) {
						g.setComposite( AlphaComposite.SrcOver );
					} else {
						g.setComposite( AlphaComposite.getInstance( AlphaComposite.SRC_OVER, ((float)opacity)/255.0f ) );
					}
				}
				g.drawImage( src.getImage(), x, y, x+refrect.width(), y+refrect.height(),
					refrect.left, refrect.top, refrect.right, refrect.bottom, null );
			}
		} catch( NullPointerException ex ){
			// VolatileImageのアクセス中に例外が発生することがあるので、ここでキャッチしておく
		}
	}
	public void show() {
		if( mWindow != null && mWindow.isVisible() ) {
			Toolkit.getDefaultToolkit().sync();
			//mWindow.drawImage();
			mWindow.repaintPanel();
		}
	}
	public void drawLines( Point[] points, int color ) {
		Graphics g = mGraphcs;
		final int count = points.length;
		int[] xPoints = new int[count];
		int[] yPoints = new int[count];
		for( int i = 0; i < count; i++ ) {
			xPoints[i] = points[i].x;
			yPoints[i] = points[i].y;
		}
		g.setColor( new Color(color,false) );
		g.drawPolyline( xPoints, yPoints, count );
	}
}
