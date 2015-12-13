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
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import jp.kirikiri.tjs2.Error;
import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tvp2.visual.DivisibleData;
import jp.kirikiri.tvp2.visual.DivisibleTransHandler;
import jp.kirikiri.tvp2.visual.ScanLineProvider;
import jp.kirikiri.tvp2.visual.SimpleOptionProvider;

/**
 * 'turn' トランジション
 * 正方形の小さなタイルをひっくり返すようにして切り替わるトランジション
 *
 * オリジナルとは異なり、シャーリング変換とスケーリング変換、移動によるアフェイン変換によって実装。
 * オリジナルも近い処理によってテーブルを生成し、それを元に描画しているようだが、テーブルが巨大なので
 * Java だと「static イニシャライザーのコードが 65535バイトの制限を超えています」と出て動かない。
 * そのためアフェイン変換を用いた実装になっている。
 * 分割描画等があるとたぶん期待通りには動かないと思われる。
 */
public class TurnTransHandler implements DivisibleTransHandler {

	/** 1 を設定すると 2 を設定したときよりも一度に回転するブロックの数が多くなる */
	private static final int TURN_WIDTH_FACTOR = 2;
	private static final int BLOCK_SIZE = 64;
	//private static final int HALF_BLOCK_SIZE = BLOCK_SIZE / 2;

	// テカり
	static private final int[] GLOSS = {
		   0,   0,   0,   0,  16,  48,  80, 128,
		 192, 128,  80,  48,  16,   0,   0,   0,
		   0,   0,   0,   0,   0,   0,   0,   0,
		   0,   0,   0,   0,   0,   0,   0,   0,
		   0,   0,   0,   0,   0,   0,   0,   0,
		   0,   0,   0,   0,   0,   0,   0,   0,
		   0,   0,   0,   0,   0,   0,   0,   0,
		   0,   0,   0,   0,   0,   0,   0,   0,
	};

	private long mStartTick; // トランジションを開始した tick count
	private long mTime; // トランジションに要する時間
	private long mCurTime; // 現在の時間
	private int mWidth; // 処理する画像の幅
	private int mHeight; // 処理する画像の高さ
	private int mBGColor; // 背景色
	private int mPhase; // アニメーションのフェーズ
	private boolean mFirst; // 一番最初の呼び出しかどうか

	private Color mBGColorObj;
	private Color mWhiteColor;
	private AffineTransform[] mTransMatrix;

	public TurnTransHandler(long time, int width, int height, int bgcolor) {

		mWidth = width;
		mHeight = height;
		mTime = time;
		mBGColor = bgcolor | 0xFF000000;
		mBGColorObj = new Color(mBGColor,false);
		mWhiteColor = new Color(0xffffffff,true);

		// 変換用マトリックス準備
		mTransMatrix = new AffineTransform[32];
		mTransMatrix[0] = new AffineTransform(); // identity matrix
		for( int i = 1; i < 32; i++ ) {
			double p = (i*i / 31.0);
			double d = (Math.sin( p * Math.PI / 32 ) * 4);
			double y = p - d;
			double shear = y / 32.0;
			//double shear = i / 31.0;
			double scale = 1.0/(1.0+shear);
			//mTransMatrix[i] = new AffineTransform( scale,shear,shear,scale,0.0,0.0 );

			AffineTransform shearm = AffineTransform.getShearInstance(shear, shear);
			AffineTransform scalem = AffineTransform.getScaleInstance(scale, scale);
			scalem.concatenate(shearm);
			mTransMatrix[i] = scalem;
		}

		mFirst = true;
	}

	@Override
	public int setOption(SimpleOptionProvider options) {
		return Error.S_OK;
	}

	/**
	 * トランジションの画面更新一回ごとに呼ばれる
	 *
	 * トランジションの画面更新一回につき、まず最初に StartProcess が呼ばれる
	 * そのあと Process が複数回呼ばれる ( 領域を分割処理している場合 )
	 * 最後に EndProcess が呼ばれる
	 */
	@Override
	public int startProcess(long tick) {
		if( mFirst ) {
			// 最初の実行
			mFirst = false;
			mStartTick = tick;
		}

		// 画像演算に必要なパラメータを計算
		// 左下から回転し始め、最後に右上が回転を終えるまで処理をする

		mCurTime = (tick - mStartTick);
		if(mCurTime > mTime) mCurTime = mTime;
		int xcount = (mWidth-1) / BLOCK_SIZE + 1;
		int ycount = (mHeight-1) / BLOCK_SIZE + 1;
		mPhase = (int) (mCurTime * (BLOCK_SIZE + (xcount +  ycount) *TURN_WIDTH_FACTOR) / mTime - ycount *TURN_WIDTH_FACTOR);

		return Error.S_TRUE;
	}

	@Override
	public int endProcess() {
		// トランジションの画面更新一回分が終わるごとに呼ばれる
		if(mCurTime == mTime) return Error.S_FALSE; // トランジション終了
		return Error.S_TRUE;
	}

	/**
	 * トランジションの各領域ごとに呼ばれる
	 * 吉里吉里は画面を更新するときにいくつかの領域に分割しながら処理を行うので
	 * このメソッドは通常、画面更新一回につき複数回呼ばれる
	 *
	 * @param data には領域や画像に関する情報が入っている
	 *
	 * data.Left, data.Top, data.Width, data.Height で示される矩形に
	 * のみ転送する必要がある。ここで行う処理は 'モザイク' のトランジション
	 * の処理に似ていて、以下の通り。
	 * 1: その転送矩形に含まれるブロックの範囲を判定する
	 * 2: 画面一番下のブロックはアクセスオーバーランに気をつけて転送する
	 * 3: その範囲の左端と右端のブロックは、上下のクリッピングに加え、
	 * 		左右のクリッピングを行いながら転送する
	 * 4: それ以外のブロックは上下のクリッピングのみを行いながら転送する
	 * ちなみに吉里吉里は通常 8 ラインごとの横に細長い領域を上から順に
	 * 指定してくる。
	 * ブロックサイズは 64x64 固定。
	 */
	@Override
	public int process(DivisibleData data) {
		BufferedImage dest = (BufferedImage)data.Dest.getScanLineForWrite().getImage();
		BufferedImage src1 = (BufferedImage)data.Src1.getScanLine().getImage();
		BufferedImage src2 = (BufferedImage)data.Src2.getScanLine().getImage();
		final int destLeft = data.DestLeft;
		//final int src1Left = data.Src1Left;
		//final int src2Left = data.Src2Left;
		final int h = data.Height;
		final int w = data.Width;
		final int destTop = data.DestTop;
		//final int src1Top = data.Src1Top;
		//final int src2Top = data.Src2Top;
		final int left = data.Left;
		final int top = data.Top;
		final int right = left + w;
		final int bottom = top + h;

		final int destxofs = destLeft - left;
		final int destyofs = destTop - top;

		Graphics2D g = (Graphics2D)dest.getGraphics();
		//g.setRenderingHint(RenderingHints.KEY_INTERPOLATION , RenderingHints.VALUE_INTERPOLATION_BILINEAR ); // バイリニア
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION , RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR ); // ニアレストネイバー
		g.setComposite( AlphaComposite.Src );

		// 1: その転送矩形に含まれるブロックの範囲を判定する
		final int startx = data.Left / BLOCK_SIZE;
		final int starty = data.Top / BLOCK_SIZE;
		final int endx = (data.Left + data.Width - 1) / BLOCK_SIZE;
		final int endy = (data.Top + data.Height - 1) / BLOCK_SIZE;
		for( int y = starty; y <= endy; y++ ) {
			for( int x = startx; x <= endx; x++ ) {
				int phase = mPhase - (x - y) * TURN_WIDTH_FACTOR;
				if(phase < 0) phase = 0;
				if(phase > 63) phase = 63;
				final int gl = GLOSS[phase];
				//if( y * BLOCK_SIZE + BLOCK_SIZE >= mHeight || x == startx || x == endx ) {
				{
					// 下側がアクセスオーバーランの可能性がある
					// あるいは 左端 右端のブロック
					int l = (x) * BLOCK_SIZE;
					int t = (y) * BLOCK_SIZE;
					int r = l + BLOCK_SIZE;
					int b = t + BLOCK_SIZE;
					if(l < left) l = left;
					if(r > right) r = right;
					if(t < top) t = top;
					if(b > bottom) b = bottom;
					if( (l < r) && (t < b) ) {
						// l, t, r, b は既にクリップされた領域を表している
						// phase を決定
						if( phase == 0 ) {
							// 完全に src1
							g.setTransform(mTransMatrix[0]);
							g.drawImage( src1, l+destxofs, t+destyofs, r+destxofs, b+destyofs, l, t, r, b, null);
						} else if(phase == 63) {
							// 完全に src2
							g.setTransform(mTransMatrix[0]);
							g.drawImage( src2, l+destxofs, t+destyofs, r+destxofs, b+destyofs, l, t, r, b, null);
						} else {
							BufferedImage src;
							AffineTransform mat;
							if( phase < 32 ) {
								src = src1;
								mat = mTransMatrix[phase];
							} else {
								src = src2;
								mat = mTransMatrix[63-phase];
							}
							if( x != y ) {
								// 中心からずれている, どの程度ずれているかによって移動量を考える
								double scale = mat.getScaleX();
								double ox = (l - l*scale);
								double oy = (t - t*scale);
								AffineTransform trans = AffineTransform.getTranslateInstance(ox-oy, -ox+oy);
								trans.concatenate(mat);
								mat = trans;
							}
							// まず背景を塗る
							g.setTransform(mTransMatrix[0]);
							g.setColor( mBGColorObj );
							g.fillRect( l+destxofs, t+destyofs, r-l, b-t );
							if( gl != 0 ) {
								g.setTransform(mat);
								g.drawImage( src, l+destxofs, t+destyofs, r+destxofs, b+destyofs, l, t, r, b, null);
								g.setComposite( AlphaComposite.getInstance( AlphaComposite.SRC_OVER, (float)gl/255.0f ) );
								g.setColor( mWhiteColor );
								g.fillRect( l+destxofs, t+destyofs, r-l, b-t );
								g.setComposite( AlphaComposite.Src );
							} else {
								g.setTransform(mat);
								g.drawImage( src, l+destxofs, t+destyofs, r+destxofs, b+destyofs, l, t, r, b, null);
							}
						}
					}
				}
			}
		}
		g.dispose();

		/* テーブルを使う方法では、テーブルのサイズが大きすぎて
		 * static イニシャライザーのコードが 65535バイトの制限を超えています
		 * と出てしまう
		BufferedImage dest = (BufferedImage)data.Dest.getScanLineForWrite().getImage();
		BufferedImage src1 = (BufferedImage)data.Src1.getScanLine().getImage();
		BufferedImage src2 = (BufferedImage)data.Src2.getScanLine().getImage();

		final int destpitch = dest.getWidth();
		final int src1pitch = src1.getWidth();
		final int src2pitch = src2.getWidth();
		final int destLeft = data.DestLeft;
		final int src1Left = data.Src1Left;
		final int src2Left = data.Src2Left;
		final int destTop = data.DestTop;
		final int src1Top = data.Src1Top;
		final int src2Top = data.Src2Top;
		final int height = data.Height;
		final int width = data.Width;
		final int left = data.Left;
		final int top = data.Top;
		final int right = left + width;
		final int bottom = top + height;

		DataBuffer destBuff = dest.getRaster().getDataBuffer();
		final int destType = destBuff.getDataType();

		DataBuffer src1Buff = src1.getRaster().getDataBuffer();
		final int src1Type = src1Buff.getDataType();
		DataBuffer src2Buff = src2.getRaster().getDataBuffer();
		final int src2Type = src2Buff.getDataType();

		if( destType != DataBuffer.TYPE_INT || src1Type != DataBuffer.TYPE_INT || src2Type != DataBuffer.TYPE_INT  ) {
			// 対応出来ない形式
			// Message.throwExceptionMessage( Message.InternalError );
			return Error.S_FALSE;
		}
		int[] s1 = ((DataBufferInt)src1Buff).getData();
		int[] s2 = ((DataBufferInt)src2Buff).getData();
		int[] d = ((DataBufferInt)destBuff).getData();

		int destxofs = data.DestLeft - data.Left;
		int destyofs = data.DestTop - data.Top;

		// 1: その転送矩形に含まれるブロックの範囲を判定する
		int startx = data.Left / BLOCK_SIZE;
		int starty = data.Top / BLOCK_SIZE;
		int endx = (data.Left + data.Width - 1) / BLOCK_SIZE;
		int endy = (data.Top + data.Height - 1) / BLOCK_SIZE;

		// 2: 画面一番下のブロックはアクセスオーバーランに気をつけて転送する
		// 3: その範囲の左端と右端のブロックは、上下のクリッピングに加え、
		//    左右のクリッピングを行いながら転送する
		// 4: それ以外のブロックは上下のクリッピングのみを行いながら転送する
		for( int y = starty; y <= endy; y++ ) {
			// 同じようなのが何回も出てきて汚いけど、勘弁
			for( int x = startx; x <= endx; x++ ) {
				int phase = mPhase - (x - y) * TURN_WIDTH_FACTOR;
				if(phase < 0) phase = 0;
				if(phase > 63) phase = 63;
				int gl = GLOSS[phase];
				if( y * BLOCK_SIZE + BLOCK_SIZE >= mHeight || x == startx || x == endx ) {
					// 下側がアクセスオーバーランの可能性がある
					// あるいは 左端 右端のブロック
					int l = (x) * BLOCK_SIZE;
					int t = (y) * BLOCK_SIZE;
					int r = l + BLOCK_SIZE;
					int b = t + BLOCK_SIZE;
					if(l < left) l = left;
					if(r > right) r = right;
					if(t < top) t = top;
					if(b > bottom) b = bottom;
					if( (l < r) && (t < b) ) {
						// l, t, r, b は既にクリップされた領域を表している

						// phase を決定
						if( phase == 0 ) {
							// 完全に src1
							int dp = (t + destyofs) * destpitch + (l + destxofs);
							int sp = t * src1pitch + l;
							int count = b - t;
							int len = (r - l);
							while( count >= 0 ) {
								count--;
								System.arraycopy( s1, sp, d, dp, len );
								dp += destpitch;
								sp += src1pitch;
							}
						} else if(phase == 63) {
							// 完全に src2
							int dp = (t + destyofs) * destpitch + (l + destxofs);
							int sp = t * src2pitch + l;
							int count = b - t;
							int len = (r - l);
							while( count >= 0 ) {
								count--;
								System.arraycopy( s2, sp, d, dp, len );
								dp += destpitch;
								sp += src2pitch;
							}
						} else {
							// 転送パラメータとソースを決定
							final int[][] params = TurnTransParams[phase];
							int srcp = 0;
							int srcpitch;
							int[] srca;
							if( phase < 32 ) {
								srca = s1;
								srcpitch = src1pitch;
							} else {
								srca = s2;
								srcpitch = src2pitch;
							}

							int line = t - y * BLOCK_SIZE;  // 開始ライン ( 0 .. 63 )
							int start = l - x * BLOCK_SIZE; // 左端の切り取られる部分 ( 0 .. 63 )
							int end = r - x * BLOCK_SIZE; // 右端

							int paramIdx = line;

							srcp += x * 64;

							int count = b - t;
							int dp = (((t + destyofs) * destpitch) + x * 64 + destxofs);
							while( count >= 0 ) {
								count--;
								int fl, fr;

								// 左の背景
								fl = 0;
								fr = params[paramIdx][START_IDX];
								if(fl < start) fl = start;
								if(fr > end) fr = end;
								if(fl < fr) {
									// fl-fr を背景色で塗りつぶす
									Arrays.fill( d, dp + fl, dp + fl + fr - fl, mBGColor );
								}

								// 右の背景
								fl = params[paramIdx][START_IDX] + params[paramIdx][LEN_IDX];
								fr = 64;
								if(fl < start) fl = start;
								if(fr > end) fr = end;
								if(fl < fr) {
									// fl-fr を背景色で塗りつぶす
									Arrays.fill( d, dp + fl, dp + fl + fr - fl, mBGColor );
								}

								// 変形転送
								fl = params[paramIdx][START_IDX];
								fr = params[paramIdx][START_IDX] + params[paramIdx][LEN_IDX];
								if(fl < start) fl = start;
								if(fr > end) fr = end;
								if(fl < fr) {
									int sx = params[paramIdx][SX_IDX];
									int sy = params[paramIdx][SY_IDX];
									sx += params[paramIdx][STEPX_IDX] * (fl - params[paramIdx][START_IDX]);
									sy += params[paramIdx][STEPY_IDX] * (fl - params[paramIdx][START_IDX]);
									if( gl != 0 ) {
										for( ; fl < fr; fl++ ) {
											int yy = y * 64 + (sy >> 16);
											if(yy >= mHeight)
												d[dp+fl] = mBGColor;
											else {
												//d[dp+fl] = blend( srca[(srcp + (sx >> 16) + yy * srcpitch)], 0xffffff, gl);
												int a1 = srca[(srcp + (sx >>> 16) + yy * srcpitch)]; // 第一引数
												int a2 = 0xffffffff; // 第二引数
												int ratio = gl; // 第三引数
												int b2 = a1 & 0x00ff00ff;
												int tmp = (b2 + (((a2 & 0x00ff00ff) - b2) * ratio >>> 8)) & 0x00ff00ff;
												b2 = (a1 & 0xff00ff00) >>> 8;
												d[dp+fl] = tmp + (((b2 + (( ((a2 & 0xff00ff00) >>> 8) - b2) * ratio >>> 8)) << 8) & 0xff00ff00);
											}

											sx += params[paramIdx][STEPX_IDX];
											sy += params[paramIdx][STEPY_IDX];
										}
									} else {
										for(; fl < fr; fl++) {
											int yy = y * 64 + (sy >>> 16);
											if(yy >= mHeight)
												d[dp+fl] = mBGColor;
											else
												d[dp+fl] = srca[(srcp + (sx >>> 16) + yy * srcpitch)];
											sx += params[paramIdx][STEPX_IDX];
											sy += params[paramIdx][STEPY_IDX];
										}
									}
								}
								dp += destpitch;
								paramIdx++;
							}
						}
					}
				} else {
					// 右端、左端、アクセスオーバーランには注意せずに転送
					int l = (x) * BLOCK_SIZE;
					int t = (y) * BLOCK_SIZE;
					int r = l + BLOCK_SIZE;
					int b = t + BLOCK_SIZE;
					if(t < top) t = top;
					if(b > bottom) b = bottom;
					if(t < b) {
						// l, t, r, b は既にクリップされた領域を表している
						// phase を決定
						if( phase == 0 ) {
							// 完全に src1
							int dp = (t + destyofs) * destpitch + (l + destxofs);
							int sp = t * src1pitch + l;
							int count = b - t;
							int len = (r - l);
							while( count >= 0 ) {
								count--;
								System.arraycopy( s1, sp, d, dp, len );
								dp += destpitch;
								sp += src1pitch;
							}
						} else if(phase == 63) {
							// 完全に src2
							int dp = (t + destyofs) * destpitch + (l + destxofs);
							int sp = t * src2pitch + l;
							int count = b - t;
							int len = (r - l);
							while( count >= 0 ) {
								count--;
								System.arraycopy( s2, sp, d, dp, len );
								dp += destpitch;
								sp += src2pitch;
							}
						} else {
							// 転送パラメータとソースを決定
							int[][] params = TurnTransParams[phase];
							int[] srca;
							int srcp = 0;
							int srcpitch;
							if( phase < 32 ) {
								srca = s1;
								srcpitch = src1pitch;
							} else {
								srca = s2;
								srcpitch = src2pitch;
							}
							int line = t - y * BLOCK_SIZE;  // 開始ライン ( 0 .. 63 )
							int paramIdx = line;

							srcp += l + y * BLOCK_SIZE * srcpitch;

							int count = b - t;
							int dp = ((t + destyofs) * destpitch) + l + destxofs;
							while( count >= 0 ) {
								count--;
								int fl, fr;

								// 左の背景
								// 0-params->start を背景色で塗りつぶす
								Arrays.fill( d, dp, dp + params[paramIdx][START_IDX], mBGColor );

								// 右の背景
								fl = params[paramIdx][START_IDX] + params[paramIdx][LEN_IDX];
								// fl-64 を背景色で塗りつぶす
								Arrays.fill( d, dp+fl, dp+fl+BLOCK_SIZE-fl, mBGColor );

								// 変形転送
								fl = params[paramIdx][START_IDX];
								fr = params[paramIdx][START_IDX] + params[paramIdx][LEN_IDX];
								int sx = params[paramIdx][SX_IDX];
								int sy = params[paramIdx][SY_IDX];
								if( gl != 0 ) {
									for(; fl < fr; fl++) {
										//d[dp+fl] = Blend( srca[(srcp + (sx >> 16) + (sy >> 16) * srcpitch)], 0xffffff, gl);
										int a1 = srca[(srcp + (sx >>> 16) + (sy >>> 16) * srcpitch)]; // 第一引数
										int a2 = 0xffffffff; // 第二引数
										int ratio = gl; // 第三引数
										int b2 = a1 & 0x00ff00ff;
										int tmp = (b2 + (((a2 & 0x00ff00ff) - b2) * ratio >>> 8)) & 0x00ff00ff;
										b2 = (a1 & 0xff00ff00) >>> 8;
										d[dp+fl] = tmp + (((b2 + (( ((a2 & 0xff00ff00) >>> 8) - b2) * ratio >>> 8)) << 8) & 0xff00ff00);
										sx += params[paramIdx][SX_IDX];
										sy += params[paramIdx][SY_IDX];
									}
								} else {
									//TVPLinTransCopy( d[dp + fl], fr - fl,
									//	srca[srcp], sx, sy,
									//	params[paramIdx][STEPX_IDX], params[paramIdx][STEPY_IDX], srcpitch);

									int destlen = fr - fl;
									int stepx = params[paramIdx][STEPX_IDX];
									int stepy = params[paramIdx][STEPY_IDX];
									int di = dp + fl;
									while( destlen > 0 ) {
										d[di] = srca[((srcp + (sy>>>16)*srcpitch) + (sx>>16))];
										sx += stepx;
										sy += stepy;
										destlen--;
										di++;
									}
								}
								dp += destpitch;
								paramIdx++;
							}
						}
					}
				}
			}
		}
		*/

		return Error.S_OK;
	}

	/*
	 * Clip( int l, int r, int left, int right)
	if(l < left) l = left;
	if(r > right) r = right;
	if(l < r)

	if(t < top) t = top;
	if(b > bottom) b = bottom;
	if(t < r)
	*/

	@Override
	public int makeFinalImage(ScanLineProvider dest, ScanLineProvider src1, ScanLineProvider src2) throws TJSException {
		// 常に最終画像は src2
		dest.copyFrom( src2 );
		return Error.S_OK;
	}

	/*
	private static final int
		START_IDX = 0,
		LEN_IDX = 1,
		SX_IDX = 2,
		SY_IDX = 3,
		EX_IDX = 4,
		EY_IDX = 5,
		STEPX_IDX = 6,
		STEPY_IDX = 7;
	*/
}
