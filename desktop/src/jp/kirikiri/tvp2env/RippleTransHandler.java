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
import java.awt.image.DataBufferInt;

import jp.kirikiri.tjs2.Error;
import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tvp2.utils.DebugClass;
import jp.kirikiri.tvp2.visual.DivisibleData;
import jp.kirikiri.tvp2.visual.DivisibleTransHandler;
import jp.kirikiri.tvp2.visual.ScanLineProvider;
import jp.kirikiri.tvp2.visual.SimpleOptionProvider;

/**
 * '波紋' トランジションハンドラクラスの実装
 *
 * オリジナルとは異なり、キャッシュは1個だけ
 */
public class RippleTransHandler implements DivisibleTransHandler {

	/**
	 * テーブル内で１象限中(90°)の方向をいくつに分割するか
	 * (2 の累乗で 256 まで。大きくするとメモリを食う)
	 */
	private static final int RIPPLE_DIR_PREC = 32;
	/** drift 1 ピクセルをいくつに分割するか */
	private static final int RIPPLE_DRIFT_PREC = 4;

	private long mStartTick; // トランジションを開始した tick count
	private long mTime; // トランジションに要する時間
	private int mLayerType; // レイヤのタイプ
	private int mWidth; // 処理する画像の幅
	private int mHeight; // 処理する画像の高さ
	private long mCurTime; // 現在の tick count
	private int mBlendRatio; // ブレンド比
	private int mPhase; // 位相
	private int mDrift; // 揺れ
	private boolean mFirst; // 一番最初の呼び出しかどうか

	private int mDriftCarePixels; // 周囲の折り返しに注意しなければならないピクセル数

	private int mCenterX; // 中心 X 座標
	private int mCenterY; // 中心 Y 座標
	private int mRippleWidth; // 波紋の幅 (16, 32, 64, 128 のいずれか)
	private float mRoundness; // 波紋の縦/横比
	private float mSpeed; // 波紋の動く角速度
	private int mMaxDrift; // 揺れの最大幅(ピクセル単位) (127まで)

	private short[] mCurDriftMap; // 現在描画中の DirftMap
	private int mDriftMapOffset;
	RippleTable mTable; // 置換マップなどのテーブル

	static class RippleTable {

		private int mWidth; // トランジション画像の幅
		private int mHeight; // トランジション画像の高さ

		private int mCenterX; // 波紋の中心 X 座標
		private int mCenterY; // 波紋の中心 Y 座標

		private int mRippleWidth; // 波紋の幅
		private float mRoundness; // 波紋の縦/横比
		private int mMaxDrift; // 揺れの最大幅

		private int mMapWidth; // 置換マップの幅
		private int mMapHeight; // 置換マップの高さ

		private short[] mDisplaceMap; // [位置]->[方向,距離] 置換マップ
		private short[] mDriftMap; // [揺れの大きさ,方向,距離]->[ずれ] 置換マップ

		public int getWidth() { return mWidth; }
		public int getHeight() { return mHeight; }

		public int getCenterX() { return mCenterX; }
		public int getCenterY() { return mCenterY; }

		public int getRippleWidth() { return mRippleWidth; }
		public float getRoundness() { return mRoundness; }
		public int getMaxDrift() { return mMaxDrift; }

		public int getMapWidth() { return mMapWidth; }
		public int getMapHeight() { return mMapHeight; }

		public RippleTable( int width, int height, int centerx, int centery,
				int ripplewidth, float roundness, int maxdrift) {

			mDisplaceMap = null;
			mDriftMap = null;

			mWidth = width;
			mHeight = height;
			mCenterX = centerx;
			mCenterY = centery;
			mRippleWidth = ripplewidth;
			mRoundness = roundness;
			mMaxDrift = maxdrift;

			makeTable();
		}
		public final short[] getDisplaceMap() { return mDisplaceMap; }
		public final int getDisplaceMapOffset( int x, int y ) {
			return x + y * mMapWidth;
		}
		public final short[] getDriftMap() { return mDriftMap; }
		public final int getDriftMapOffset( int drift, int phase ) {
			return drift * mRippleWidth * (2 * RIPPLE_DIR_PREC) + phase * RIPPLE_DIR_PREC;
		}
		private static final float rippleWaveForm(float rad) {
			// 波を生成する関数
			// 適当に。s は正にしかならないが見た目が良いのでこれでいく
			float s = (float) ((Math.sin(rad) + Math.sin(rad*2-2) * 0.2f) / 1.19f);
			s *= s;
			return s;
		}
		private final void makeTable() {
			int[] rippleform = null;
			int[] cos_table = null;
			int[] sin_table = null;

			try {
				// MapWidth, MapHeight の計算
				// Width, Height を CenterX, CenterY で分割する４つの象限のうち
				// もっとも大きい物のサイズを MapWidth, MapHeight とする
				mMapWidth = mCenterX < (mWidth >>> 1) ? mWidth - mCenterX : mCenterX;
				mMapHeight = mCenterY < (mHeight >>> 1) ? mHeight - mCenterY : mCenterY;

				// DisplaceMap メモリ確保
				mDisplaceMap = new short[mMapWidth * mMapHeight];

				// DisplaceMap 計算
				// 置換マップは１象限についてのみ計算する(他の象限は対称だから)
				int rmp = 0;
				short[] dispmap = mDisplaceMap;
				int ripplemask = mRippleWidth - 1;
				for( int y = 0; y < mMapHeight; y++ ) {
					float yy = ((float)y + 0.5f) * mRoundness;
					float fac = 1.0f / yy;
					for( int x = 0; x < mMapWidth; x++ ) {
						float xx =  (float)x + 0.5f;
						int dir = (int) (Math.atan(xx*fac) * ((1.0/(Math.PI/2.0)) * RIPPLE_DIR_PREC)); // dir = 方向コード
						int dist = (int)Math.sqrt(xx*xx + yy*yy) & ripplemask; // dist = 中心からの距離

						dispmap[rmp] = (short)((dist * RIPPLE_DIR_PREC) + dir);
						rmp++;
					}
				}

				// mDriftMap メモリ確保
				// mDriftMap に使用するメモリ量は
				// mMaxDrift*RIPPLE_DRIFT_PREC * mRippleWidth * 2 * RIPPLE_DIR_PREC * 2byte
				// int [mMaxDrift*RIPPLE_DRIFT_PREC][mRippleWidth*2][RIPPLE_DIR_PREC]
				// *2 が入っているのは 画像演算中に & でマスクをかける必要がないように
				mDriftMap = new short[mMaxDrift * RIPPLE_DRIFT_PREC * mRippleWidth * 2 * RIPPLE_DIR_PREC];

				// 波形の計算
				float rcp_rw = 1.0f / (float)mRippleWidth;
				rippleform = new int[mRippleWidth];
				for( int w = 0; w < mRippleWidth; w++ ) {
					// 適当に波っぽく見える波形(単純なsin波でもよい)
					float rad = (float)(w * rcp_rw * (Math.PI * -2.0f));

					float s = rippleWaveForm(rad);

					if(s < -1.0f) s = -1.0f;
					if(s > 1.0f) s = 1.0f;
					s *= 2048.0f;
					rippleform[w] = (int)(s < 0 ? s - 0.5f : s + 0.5f); // 1.11
				}

				// sin/cos テーブルの生成
				cos_table = new int[RIPPLE_DIR_PREC];
				sin_table = new int[RIPPLE_DIR_PREC];
				for( int w = 0; w < RIPPLE_DIR_PREC; w++ ) {
					float fdir = (float) (Math.PI*0.5f - (((float)w + 0.5f) * ((1.0f / (float)RIPPLE_DIR_PREC) * (Math.PI / 2.0f))));
					float v;
					v = (float) (Math.cos(fdir) * 2048.0f);
					cos_table[w] = (int)(v < 0 ? v - 0.5f : v + 0.5f); // 1.11
					v = (float) (Math.sin(fdir) * 2048.0f);
					sin_table[w] = (int)(v < 0 ? v - 0.5f : v + 0.5f); // 1.11
				}

				// DriftMap 計算
				// float で計算するとエラく遅いので固定小数点で計算する
				int drift, dir;
				int ripplewidth_step = mRippleWidth * RIPPLE_DIR_PREC;
				for(drift = 0; drift < mMaxDrift*RIPPLE_DRIFT_PREC; drift ++) {
					int fdrift = (drift << 10) / RIPPLE_DRIFT_PREC; // 8.10
					int dmp = drift * mRippleWidth * (2 * RIPPLE_DIR_PREC);
					for( int w = 0; w < mRippleWidth; w++ ) {
						int fd = rippleform[w] * fdrift >>> 10; // 8.11
						for( dir = 0; dir < RIPPLE_DIR_PREC; dir++ ) {
							int xd = cos_table[dir] * fd >>> 11; // 8.11
							int yd = sin_table[dir] * fd >>> 11; // 8.11

							short val = (short) ((int)( ((xd >>>11)<< 8) + (yd >>>11) ) & 0xffff);

							mDriftMap[dmp+w * RIPPLE_DIR_PREC +                    dir] =
							mDriftMap[dmp+w * RIPPLE_DIR_PREC + ripplewidth_step + dir] = val;
						}
					}
				}
			} finally {
				rippleform = null;
				sin_table = null;
				cos_table = null;
			}
		}
	}
	// キャッシングは1個だけ
	private static RippleTable mCacheTable;
	private static final RippleTable getRippleTable( int width, int height, int centerx, int centery,
			int ripplewidth, float roundness, int maxdrift ) {
		if( mCacheTable == null ) {
			mCacheTable = new RippleTable( width, height, centerx, centery, ripplewidth, roundness, maxdrift );
			return mCacheTable;
		} else {
			RippleTable table = mCacheTable;
			if(
				table.getWidth() == width &&
				table.getHeight() == height &&
				table.getCenterX() == centerx &&
				table.getCenterY() == centery &&
				table.getRippleWidth() == ripplewidth &&
				table.getRoundness() == roundness &&
				table.getMaxDrift() == maxdrift) {
				return table;
			} else {
				mCacheTable = null;
				mCacheTable = new RippleTable( width, height, centerx, centery, ripplewidth, roundness, maxdrift );
				return mCacheTable;
			}
		}
	}
	public RippleTransHandler(long time, int layertype, int width, int height,
			int centerx, int centery, int ripplewidth, float roundness,
			float speed, int maxdrift) {

		mLayerType = layertype;
		mWidth = width;
		mHeight = height;
		mTime = time;

		mCenterX = centerx;
		mCenterY = centery;

		mRippleWidth = ripplewidth;

		mRoundness = roundness;
		mSpeed = speed;

		mFirst = true;

		mMaxDrift = maxdrift;

		mTable = getRippleTable( mWidth, mHeight, mCenterX, mCenterY, mRippleWidth, mRoundness, mMaxDrift );
	}

	@Override
	public int setOption(SimpleOptionProvider options) {
		return Error.S_OK;
	}

	@Override
	public int startProcess(long tick) {
		if( mFirst ) {
			// 最初の実行
			mFirst = false;
			mStartTick = tick;
		}

		// 画像演算に必要な各パラメータを計算
		mCurTime = (tick - mStartTick);

		// BlendRatio
		mBlendRatio = (int) (mCurTime * 255 / mTime);
		if(mBlendRatio > 255) mBlendRatio = 255;

		// Phase
		// 角速度が Speed (rad/sec) で与えられている
		mPhase = (int)(mSpeed * ((1.0/(Math.PI*2))*(1.0/1000.0)) * mCurTime * mRippleWidth) % mRippleWidth;
		if(mPhase < 0) mPhase = 0;
		mPhase = mRippleWidth - mPhase - 1;

		// Drift
		float s = (float) Math.sin(Math.PI * mCurTime / mTime);
		mDrift = (int)(s * mMaxDrift * RIPPLE_DRIFT_PREC);
		if(mDrift < 0) mDrift = 0;
		if(mDrift >= mMaxDrift * RIPPLE_DRIFT_PREC) mDrift = mMaxDrift * RIPPLE_DRIFT_PREC - 1;

		mDriftCarePixels = (int)(mDrift / RIPPLE_DRIFT_PREC) + 1;
		if((mDriftCarePixels&1) != 0 ) mDriftCarePixels ++; // 一応偶数にアライン

		// CurDriftMap
		mCurDriftMap = mTable.getDriftMap();
		mDriftMapOffset = mTable.getDriftMapOffset(mDrift, mPhase);

		return Error.S_TRUE;
	}

	@Override
	public int endProcess() {
		// トランジションの画面更新一回分が終わるごとに呼ばれる
		if(mBlendRatio == 255) return Error.S_FALSE; // トランジション終了
		return Error.S_TRUE;
	}

	@Override
	public int process(DivisibleData data) {
		BufferedImage dest = (BufferedImage)data.Dest.getScanLineForWrite().getImage();
		BufferedImage src1 = (BufferedImage)data.Src1.getScanLine().getImage();
		BufferedImage src2 = (BufferedImage)data.Src2.getScanLine().getImage();
		//final int destLeft = data.DestLeft;
		//final int src1Left = data.Src1Left;
		//final int src2Left = data.Src2Left;
		//final int height = data.Height;
		final int width = data.Width;
		//final int top = data.Top;
		final int left = data.Left;
		final int right = left + width;
		//final int bottom = top + height;
		final int destTop = data.DestTop;
		//final int src1Top = data.Src1Top;
		//final int src2Top = data.Src2Top;

		final int destpitch = dest.getWidth();
		final int src1pitch = src1.getWidth();
		final int src2pitch = src2.getWidth();

		DataBuffer destBuff = dest.getRaster().getDataBuffer();
		final int destType = destBuff.getDataType();

		DataBuffer src1Buff = src1.getRaster().getDataBuffer();
		final int src1Type = src1Buff.getDataType();
		DataBuffer src2Buff = src2.getRaster().getDataBuffer();
		final int src2Type = src2Buff.getDataType();

		int destxofs = data.DestLeft - data.Left;
		if( destType == DataBuffer.TYPE_INT && src1Type == DataBuffer.TYPE_INT && src2Type == DataBuffer.TYPE_INT ) {
			int[] s1 = ((DataBufferInt)src1Buff).getData();
			int[] s2 = ((DataBufferInt)src2Buff).getData();
			int[] d = ((DataBufferInt)destBuff).getData();
			int doffset = destTop * destpitch;

			// ラインごとに処理
			int h = data.Height;
			int y = data.Top;
			while( h > 0 ) {
				h--;
				int l, r;

				if(y < mDriftCarePixels || y >= mHeight - mDriftCarePixels) {
					// 上下のすみではみ出す可能性があるので
					// 折り返し転送を行う

					// 左端 ～ CenterX
					l = 0;
					r = mCenterX;
					if(l < left) l = left;
					if(r > right) r = right;
					if( l < r ) {
						if( y < mCenterY) {
							rippleTransform_b_a_e(
								mTable.getDisplaceMap(),
								mTable.getDisplaceMapOffset( mCenterX - l - 1, mCenterY - y - 1),
								mCurDriftMap,
								mDriftMapOffset,
								d, doffset + l + destxofs, r - l, src1pitch,
								s1, s2, l, y, mWidth, mHeight, mBlendRatio);
						} else {
							rippleTransform_b_d_e(
								mTable.getDisplaceMap(),
								mTable.getDisplaceMapOffset(mCenterX - l - 1, y - mCenterY),
								mCurDriftMap,
								mDriftMapOffset,
								d, doffset + l + destxofs, r - l, src1pitch,
								s1, s2, l, y, mWidth, mHeight, mBlendRatio);
						}
					}

					// CenterX ～ 右端
					l = mCenterX;
					r = mWidth;
					if(l < left) l = left;
					if(r > right) r = right;
					if( l < r ) {
						if(y < mCenterY) {
							rippleTransform_f_a_e(
								mTable.getDisplaceMap(),
								mTable.getDisplaceMapOffset(l - mCenterX, mCenterY - y - 1),
								mCurDriftMap,
								mDriftMapOffset,
								d, doffset + l + destxofs, r - l, src1pitch,
								s1, s2, l, y, mWidth, mHeight, mBlendRatio);
						} else {
							rippleTransform_f_d_e(
								mTable.getDisplaceMap(),
								mTable.getDisplaceMapOffset(l - mCenterX, y - mCenterY),
								mCurDriftMap,
								mDriftMapOffset,
								d, doffset + l + destxofs, r - l, src1pitch,
								s1, s2, l, y, mWidth, mHeight, mBlendRatio);
						}
					}
				} else {
					// 左端 ～ CenterX
					l = 0;
					r = mCenterX;
					if(l < left) l = left;
					if(r > right) r = right;
					if( l < r ) {
						int ll = 0;
						int rr = mDriftCarePixels;

						if(ll < l) ll = l;
						if(rr > r) rr = r;
						if( ll < rr ) {
							// この ll ～ rr で表される左端は 左端にはみ出す可能性がある
							// ので折り返し転送をさせる
							if(y < mCenterY) {
								rippleTransform_b_a_e(
									mTable.getDisplaceMap(),
									mTable.getDisplaceMapOffset(mCenterX - ll - 1, mCenterY - y - 1),
									mCurDriftMap,
									mDriftMapOffset,
									d, doffset + ll + destxofs, rr - ll, src1pitch,
									s1, s2, ll, y, mWidth, mHeight, mBlendRatio);
							} else {
								rippleTransform_b_d_e(
									mTable.getDisplaceMap(),
									mTable.getDisplaceMapOffset(mCenterX - ll - 1, y - mCenterY),
									mCurDriftMap,
									mDriftMapOffset,
									d, doffset + ll + destxofs, rr - ll, src1pitch,
									s1, s2, ll, y, mWidth, mHeight, mBlendRatio);
							}
						}

						ll = mDriftCarePixels;
						rr = r;
						if(ll < l) ll = l;
						if( ll < rr ) {
							// ここははみ出さない
							if(y < mCenterY) {
								rippleTransform_b(
									mTable.getDisplaceMap(),
									mTable.getDisplaceMapOffset(mCenterX - ll - 1, mCenterY - y - 1),
									mCurDriftMap,
									mDriftMapOffset,
									d, doffset + ll + destxofs,
									rr - ll,
									src1pitch,
									s1, y*src1pitch + ll,
									s2, y*src2pitch + ll,
									mBlendRatio);
							} else {
								rippleTransform_b(
									mTable.getDisplaceMap(),
									mTable.getDisplaceMapOffset(mCenterX - ll - 1, y - mCenterY),
									mCurDriftMap,
									mDriftMapOffset,
									d, doffset + ll + destxofs,
									rr - ll,
									-src1pitch,
									s1, y*src1pitch + ll,
									s2, y*src2pitch + ll,
									mBlendRatio);
							}
						}
					}

					// CenterX ～ 右端
					l = mCenterX;
					r = mWidth;
					if(l < left) l = left;
					if(r > right) r = right;
					if( l < r ) {
						int ll = l, rr = mWidth - mDriftCarePixels;
						if(rr > r) rr = r;
						if( ll < rr ) {
							// ここははみ出さない
							if(y < mCenterY) {
								rippleTransform_f(
									mTable.getDisplaceMap(),
									mTable.getDisplaceMapOffset(ll - mCenterX, mCenterY - y - 1),
									mCurDriftMap,
									mDriftMapOffset,
									d, doffset + ll + destxofs,
									rr - ll,
									src1pitch,
									s1, y*src1pitch + ll,
									s2, y*src2pitch + ll,
									mBlendRatio);
							} else {
								rippleTransform_f(
									mTable.getDisplaceMap(),
									mTable.getDisplaceMapOffset(ll - mCenterX, y - mCenterY),
									mCurDriftMap,
									mDriftMapOffset,
									d, doffset + ll + destxofs,
									rr - ll,
									-src1pitch,
									s1, y*src1pitch + ll,
									s2, y*src2pitch + ll,
									mBlendRatio);
							}
						}

						ll = mWidth - mDriftCarePixels;
						rr = r;
						if(ll < l) ll = l;
						if( ll < rr ) {
							// この ll ～ rr で表される右端は 右端にはみ出す可能性がある
							// ので折り返し転送をさせる
							if(y < mCenterY) {
								rippleTransform_f_a_e(
									mTable.getDisplaceMap(),
									mTable.getDisplaceMapOffset(ll - mCenterX, mCenterY - y - 1),
									mCurDriftMap,
									mDriftMapOffset,
									d, doffset + ll + destxofs, rr - ll, src1pitch,
									s1, s2, ll, y, mWidth, mHeight, mBlendRatio);
							} else {
								rippleTransform_f_d_e(
									mTable.getDisplaceMap(),
									mTable.getDisplaceMapOffset(ll - mCenterX, y - mCenterY),
									mCurDriftMap,
									mDriftMapOffset,
									d, doffset + ll + destxofs, rr - ll, src1pitch,
									s1, s2, ll, y, mWidth, mHeight, mBlendRatio);
							}
						}
					}
				}

				doffset += destpitch;
				y++;
			}
		}
		return Error.S_OK;
	}


	private void rippleTransform_f_a_e(short[] displacemap,
			int displaceMapOffset, short[] driftmap, int driftmapOffset,
			int[] dest, int destOffset, int num, int pitch, int[] src1,
			int[] src2, int srcx, int srcy, int srcwidth, int srcheight,
			int ratio ) {

		for(int i = 0; i < num; i++) {
			int o = displacemap[displaceMapOffset+i];
			int n = driftmap[driftmapOffset+o];
			int x = srcx + i - (int)((n>>>8)&0xff);
			int y = srcy + (int)(n&0xff);
			// TVP_RIPPLE_TURN_BORDER
			if(x<0) x = -x;
			if(y<0) y = -y;
			if(x>=srcwidth) x = srcwidth - 1 - (x - srcwidth);
			if(y>=srcheight) y = srcheight - 1 - (y - srcheight);

			// TVP_RIPPLE_CALC_OFS
			int ofs = x + y*pitch;

			//TVP_RIPPLE_BLEND
			int s1, s2, s1_;
			s1 = src1[ofs];
			s2 = src2[ofs];
			s1_ = s1 & 0xff00ff;
			s1_ = (s1_ + (((s2 & 0xff00ff) - s1_) * ratio >>> 8)) & 0xff00ff;
			s2 &= 0xff00;
			s1 &= 0xff00;
			dest[destOffset+i] = s1_ | ((s1 + ((s2 - s1) * ratio >>> 8)) & 0xff00) | 0xff000000;
		}
	}

	private void rippleTransform_f_d_e(short[] displacemap,
			int displaceMapOffset, short[] driftmap, int driftmapOffset,
			int[] dest, int destOffset, int num, int pitch, int[] src1,
			int[] src2, int srcx, int srcy, int srcwidth, int srcheight,
			int ratio ) {

		for(int i = 0; i < num; i++) {
			int o = displacemap[displaceMapOffset+i];
			int n = driftmap[driftmapOffset+o];
			int x = srcx + i - (int)((n>>>8)&0xff);
			int y = srcy - (int)(n&0xff);
			// TVP_RIPPLE_TURN_BORDER
			if(x<0) x = -x;
			if(y<0) y = -y;
			if(x>=srcwidth) x = srcwidth - 1 - (x - srcwidth);
			if(y>=srcheight) y = srcheight - 1 - (y - srcheight);

			// TVP_RIPPLE_CALC_OFS
			int ofs = x + y*pitch;

			//TVP_RIPPLE_BLEND
			int s1, s2, s1_;
			s1 = src1[ofs];
			s2 = src2[ofs];
			s1_ = s1 & 0xff00ff;
			s1_ = (s1_ + (((s2 & 0xff00ff) - s1_) * ratio >>> 8)) & 0xff00ff;
			s2 &= 0xff00;
			s1 &= 0xff00;
			dest[destOffset+i] = s1_ | ((s1 + ((s2 - s1) * ratio >>> 8)) & 0xff00) | 0xff000000;
		}

	}

	private void rippleTransform_b_a_e(short[] displacemap,
			int displaceMapOffset, short[] driftmap, int driftmapOffset,
			int[] dest, int destOffset, int num, int pitch, int[] src1,
			int[] src2, int srcx, int srcy, int srcwidth, int srcheight,
			int ratio ) {

		for(int i = 0; i < num; i++) {
			int o = displacemap[displaceMapOffset];
			int n = driftmap[driftmapOffset+o];
			displaceMapOffset--;
			int x = srcx + i + (int)((n>>>8)&0xff);
			int y = srcy + (int)(n&0xff);
			// TVP_RIPPLE_TURN_BORDER
			if(x<0) x = -x;
			if(y<0) y = -y;
			if(x>=srcwidth) x = srcwidth - 1 - (x - srcwidth);
			if(y>=srcheight) y = srcheight - 1 - (y - srcheight);

			// TVP_RIPPLE_CALC_OFS
			int ofs = x + y*pitch;

			//TVP_RIPPLE_BLEND
			int s1, s2, s1_;
			s1 = src1[ofs];
			s2 = src2[ofs];
			s1_ = s1 & 0xff00ff;
			s1_ = (s1_ + (((s2 & 0xff00ff) - s1_) * ratio >>> 8)) & 0xff00ff;
			s2 &= 0xff00;
			s1 &= 0xff00;
			dest[destOffset+i] = s1_ | ((s1 + ((s2 - s1) * ratio >>> 8)) & 0xff00) | 0xff000000;
		}
	}

	private void rippleTransform_b_d_e(short[] displacemap,
			int displaceMapOffset, short[] driftmap, int driftmapOffset,
			int[] dest, int destOffset, int num, int pitch, int[] src1, int[] src2, int srcx,
			int srcy, int srcwidth, int srcheight, int ratio ) {

		for(int i = 0; i < num; i++) {
			int o = displacemap[displaceMapOffset];
			int n = driftmap[driftmapOffset+o];
			displaceMapOffset--;
			int x = srcx + i + (int)((n>>>8)&0xff);
			int y = srcy - (int)(n&0xff);
			// TVP_RIPPLE_TURN_BORDER
			if(x<0) x = -x;
			if(y<0) y = -y;
			if(x>=srcwidth) x = srcwidth - 1 - (x - srcwidth);
			if(y>=srcheight) y = srcheight - 1 - (y - srcheight);

			// TVP_RIPPLE_CALC_OFS
			int ofs = x + y*pitch;

			//TVP_RIPPLE_BLEND
			int s1, s2, s1_;
			s1 = src1[ofs];
			s2 = src2[ofs];
			s1_ = s1 & 0xff00ff;
			s1_ = (s1_ + (((s2 & 0xff00ff) - s1_) * ratio >>> 8)) & 0xff00ff;
			s2 &= 0xff00;
			s1 &= 0xff00;
			dest[destOffset+i] = s1_ | ((s1 + ((s2 - s1) * ratio >>> 8)) & 0xff00) | 0xff000000;
		}
	}

	private void rippleTransform_f(short[] displacemap, int displaceMapOffset,
			short[] driftmap, int driftmapOffset, int[] dest, int destOffset, int num,
			int pitch, int[] src1, int src1offset, int[] src2,
			int src2offset, int ratio ) {

		for(int i = 0; i < num; i++) {
			int o = displacemap[displaceMapOffset+i];
			int n = driftmap[driftmapOffset+o];
			int ofs = (i - ((n>>>8)&0xff)) + (n&0xff)*pitch;

			//TVP_RIPPLE_BLEND
			int s1, s2, s1_;
			s1 = src1[src1offset+ofs];
			s2 = src2[src2offset+ofs];
			s1_ = s1 & 0xff00ff;
			s1_ = (s1_ + (((s2 & 0xff00ff) - s1_) * ratio >>> 8)) & 0xff00ff;
			s2 &= 0xff00;
			s1 &= 0xff00;
			dest[destOffset+i] = s1_ | ((s1 + ((s2 - s1) * ratio >>> 8)) & 0xff00) | 0xff000000;
		}
	}

	private void rippleTransform_b(short[] displacemap, int displaceMapOffset,
			short[] driftmap, int driftmapOffset, int[] dest, int destOffset, int num,
			int pitch, int[] src1, int src1offset, int[] src2,
			int src2offset, int ratio ) {

		for(int i = 0; i < num; i++) {
			int o = displacemap[displaceMapOffset];
			int n = driftmap[driftmapOffset+o];
			displaceMapOffset--;
			int ofs = (i + ((n>>>8)&0xff)) + (n&0xff)*pitch;

			//TVP_RIPPLE_BLEND
			int s1, s2, s1_;
			s1 = src1[src1offset+ofs];
			s2 = src2[src2offset+ofs];
			s1_ = s1 & 0xff00ff;
			s1_ = (s1_ + (((s2 & 0xff00ff) - s1_) * ratio >>> 8)) & 0xff00ff;
			s2 &= 0xff00;
			s1 &= 0xff00;
			dest[destOffset+i] = s1_ | ((s1 + ((s2 - s1) * ratio >>> 8)) & 0xff00) | 0xff000000;
		}

	}

	@Override
	public int makeFinalImage(ScanLineProvider dest, ScanLineProvider src1, ScanLineProvider src2) throws TJSException {
		// 常に最終画像は src2
		dest.copyFrom( src2 );
		return Error.S_OK;
	}

}
