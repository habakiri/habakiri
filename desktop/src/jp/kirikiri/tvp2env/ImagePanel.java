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

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tvp2.visual.BaseBitmap;
import jp.kirikiri.tvp2.visual.GraphicsLoader;


public class ImagePanel extends JPanel {

	/**
	 *
	 */
	private static final long serialVersionUID = 7393364494465761702L;

	private VRAMImageBuffer mImageBuffer;	// VRAMバッファ
	private PanelDrawListener mDraw;

	private NativeImageBuffer mImage;
	private WindowForm mWindow;

	//コンストラクタ
	public ImagePanel() {
		super();
		mImageBuffer = new VRAMImageBuffer(this);
		mImage = new NativeImageBuffer(32,32);
	}
	public void setOwner( WindowForm window ) {
		mWindow = window;
	}
	@Override
	public void setSize(int w, int h ) {
		super.setSize(w,h);
		if( w != 0 && h != 0 ) {
			try {
				mImage.recreate(w, h, true);
				if( mDraw != null ) {
					Image img = mImage.getImage();
					if( img != null ) {
						mDraw.onDraw( img.getGraphics() );
					}
				}
			} catch (TJSException e) {
			}
		}

/*
		mImageBuffer.initializeVolatileImage();
		try {
			do {
				// ダブルバッファ用イメージのチェック
				mImageBuffer.validateVolatileImage();
				if( mDraw != null ) {
					Image img = mImageBuffer.getImage();
					if( img != null ) {
						mDraw.onDraw( img.getGraphics() );
					}
				}
				// バッファ内容が失われたら再描画させる
			} while( mImageBuffer.isContentsLost() );
		} catch( NullPointerException ex ){
			// VolatileImageのアクセス中に例外が発生することがあるので、ここでキャッチしておく
		}
*/
	}
	/*
	public Graphics getTargetGraphics() {
		return mImage.getImage().getGraphics();
	}
	*/
	/*
	private void loadImage() {
		BaseBitmap bmp = new BaseBitmap(32,32,32);
		try {
			GraphicsLoader.loadGraphic( bmp, "kaname_kira.png", 0, 0, 0, GraphicsLoader.glmNormal, null );
		} catch (TJSException e) {
		}
		mTestImage = (BufferedImage) bmp.getBitmap().getImage();
	}
	*/

	// 画面の描画処理
	protected void paintComponent( Graphics graphic ) {
		// ダブルバッファ用イメージの初期化
		mImageBuffer.initializeVolatileImage();
		try {
			do {
				// ダブルバッファ用イメージのチェック
				mImageBuffer.validateVolatileImage();

				// バッファのグラフィックスオブジェクトを取得
				Graphics buffer = mImageBuffer.getGraphics();

				// 親クラスの描画処理を呼びだす
				// super.paintComponent(buffer); 余計な塗りつぶしをなくす

				// 具体的な描画処理を入れる
				buffer.drawImage(mImage.getImage(), 0, 0, null );

				// グラフィックスオブジェクトを破棄する
				buffer.dispose();

				// バッファのイメージを実際に描画する
				if( mWindow != null ) {
					Graphics2D g = (Graphics2D)graphic;
					int ow = mWindow.getInnerWidthSave();
					int oh = mWindow.getInnerHeightSave();
					int dw = mWindow.getInnerWidth();
					int dh = mWindow.getInnerHeight();
					if( ow == dw || oh == dh ) {
						graphic.drawImage(mImageBuffer.getImage(),0,0,this);
					} else {
						int rw = (1000 * dw) / ow;
						int rh = (1000 * dh) / oh;
						int x, y, w, h;
						if( rw < rh ) {
							w = dw;
							h = (oh * dw) / ow;
							x = 0;
							y = (dh - h)/2;
						} else {
							w = (ow * dh) / oh;
							h = dh;
							x = (dw - w)/2;
							y = 0;
						}
						g.setRenderingHint(RenderingHints.KEY_INTERPOLATION , RenderingHints.VALUE_INTERPOLATION_BILINEAR );
						//g.setRenderingHint(RenderingHints.KEY_INTERPOLATION , RenderingHints.VALUE_INTERPOLATION_BICUBIC );
						//g.setRenderingHint(RenderingHints.KEY_INTERPOLATION , RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR );
						g.drawImage(mImageBuffer.getImage(),x,y,x+w,y+h,0,0,ow,oh,this);
					}
				}
				// バッファ内容が失われたら再描画させる
			} while( mImageBuffer.isContentsLost() );
		} catch( NullPointerException ex ){
			// VolatileImageのアクセス中に例外が発生することがあるので、ここでキャッチしておく
		}
	}
	public void drawImage( Graphics graphic, int ox, int oy ) {
		try {
			if( mWindow != null ) {
				Graphics2D g = (Graphics2D)graphic;
				int ow = mWindow.getInnerWidthSave();
				int oh = mWindow.getInnerHeightSave();
				int dw = mWindow.getInnerWidth();
				int dh = mWindow.getInnerHeight();
				if( ow == dw || oh == dh ) {
					graphic.drawImage(mImage.getImage(),ox,oy,this);
				} else {
					int rw = (1000 * dw) / ow;
					int rh = (1000 * dh) / oh;
					int x, y, w, h;
					if( rw < rh ) {
						w = dw;
						h = (oh * dw) / ow;
						x =  ox;
						y = (dh - h)/2 + oy;
					} else {
						w = (ow * dh) / oh;
						h = dh;
						x = (dw - w)/2 + ox;
						y = oy;
					}
					g.setRenderingHint(RenderingHints.KEY_INTERPOLATION , RenderingHints.VALUE_INTERPOLATION_BILINEAR );
					//g.setRenderingHint(RenderingHints.KEY_INTERPOLATION , RenderingHints.VALUE_INTERPOLATION_BICUBIC );
					//g.setRenderingHint(RenderingHints.KEY_INTERPOLATION , RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR );
					g.drawImage(mImage.getImage(),x,y,x+w,y+h,0,0,ow,oh,this);
				}
			}
		} catch( NullPointerException ex ){
		}
	}
	public void setDrawListener( PanelDrawListener draw ) {
		mDraw = draw;
		//if( mDraw != null ) mDraw.onDraw( mImage.getImage().getGraphics() );
	}
}
