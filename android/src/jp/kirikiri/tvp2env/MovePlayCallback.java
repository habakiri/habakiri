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


import java.io.IOException;

import jp.kirikiri.tjs2.BinaryStream;
import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tvp2.TVP;

import android.graphics.Rect;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.FrameLayout;

public class MovePlayCallback implements SurfaceHolder.Callback, MediaPlayer.OnErrorListener {
	private static final boolean LOGD = false;

	private FrameLayout mFrameLayout;
	private MediaPlayer mMediaPlayer;
	private BinaryStream mSourceStream;
	private BaseActivity mCompletionListener;
	private int mWidth;
	private int mHeight;
	private int mInnerWidth;
	private int mInnerHeight;
	private Rect mArea;

	/**
	 *
	 * @param src ソースストリーム
	 * @param layout SurfaceView を保有する FrameLayout。これを用いてサイズ調整される。
	 * @param listener 動画再生終了リスナ
	 * @param rect 動画表示サイズ
	 * @param w 画面幅
	 * @param h 画面高さ
	 */
	public MovePlayCallback( BinaryStream src, FrameLayout layout, BaseActivity listener, Rect rect, int w, int h, int iw, int ih ) {
		if( LOGD ) Log.v("Movie","MovePlayCallback");
		mSourceStream = src;
		mFrameLayout = layout;
		mCompletionListener = listener;
		mArea = rect;
		mWidth = w;
		mHeight = h;
		mInnerWidth = iw;
		mInnerHeight = ih;
		adjustImageFrame( mFrameLayout );
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		if( LOGD ) Log.v("Movie","surfaceChanged");
		if( LOGD ) Log.v("wh", "w: " + width + ", h: " + height );
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if( LOGD ) Log.v("Movie","surfaceCreated");
		try {
			//mMediaPlayer = MediaPlayer.create(this, R.raw.test);
			mMediaPlayer = new MediaPlayer();
			mMediaPlayer.setDataSource( mSourceStream.getFileDescriptor(), mSourceStream.getFileOffset(), mSourceStream.getSize() );
			//AssetFileDescriptor fd = getAssets().openFd("test.mp4");
			//mMediaPlayer.setDataSource( fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength() );
			//String path = Environment.getExternalStorageDirectory().getPath() + "/DCIM/100ANDRO/movtest.mp4";
			//String path = Environment.getExternalStorageDirectory().getPath() + "/DCIM/100ANDRO/test.mp4";
			//FileInputStream fis = new FileInputStream(path);
			//mMediaPlayer.setDataSource(fis.getFD());
			mMediaPlayer.setOnCompletionListener( mCompletionListener );
			mMediaPlayer.setOnErrorListener(this);
			mMediaPlayer.setDisplay(holder);
			mMediaPlayer.setLooping(true);
			mMediaPlayer.prepare();
			//adjustImageFrame( mFrameLayout, mMediaPlayer.getVideoWidth(), mMediaPlayer.getVideoHeight());
			//adjustImageFrame( mFrameLayout );
			mCompletionListener.interruptVMThread();
			//mMediaPlayer.start();
		} catch (IllegalArgumentException e) {
			Log.d("MovePlayCallback", "IllegalArgumentException:"+e.getMessage());
		} catch (IllegalStateException e) {
			Log.d("MovePlayCallback", "IllegalStateException:"+e.getMessage());
		} catch (IOException e) {
			Log.d("MovePlayCallback", "IOException:"+e.getMessage());
		} catch (TJSException e) {
			e.printStackTrace();
		}
	}
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		if( LOGD ) Log.v("Movie","surfaceDestroyed");
		if( mMediaPlayer != null ) {
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
		if( mSourceStream != null ) {
			mSourceStream.close();
		}
		mCompletionListener = null;
		mFrameLayout = null;
	}

	/*
	@Override
	public void onCompletion(MediaPlayer mp) {
		if( mp.isLooping() == false ) {
			// 再生終了
		}
	}
	*/

	private void adjustImageFrame( FrameLayout layout ) {
		if( LOGD ) Log.v("Movie","adjustImageFrame");
		int x, y, w, h;
		int ow = mInnerWidth; // WindowForm から InnerWidth 等取得する事
		int oh = mInnerHeight;
		final int dw = mWidth;
		final int dh = mHeight;
		int l = mArea.left;
		int t = mArea.top;
		int r = mArea.right;
		int b = mArea.bottom;
		if( LOGD ) Log.v("Movie","l:"+l+", t:"+t+", r:"+r+", b:"+b);
		if( dw >= ow && dh >= oh ) { // 拡大
			int sw = dh * ow / oh;
			//int sh = dw * oh / ow;
			if( sw > dw ) { // 幅がはみ出す、高さ基準ではなく幅基準で拡大する
				h = (oh * dw) / ow;
				y = (dh - h)/2;
				l = l * dw / ow;
				t = t * dw / ow + y;
				r = r * dw / ow;
				b = b * dw / ow + y;
			} else {
				w = (ow * dh) / oh;
				x = (dw - w)/2;
				l = l * dh / oh + x;
				t = t * dh / oh;
				r = r * dh / oh + x;
				b = b * dh / oh;
			}
		} else { // 縮小
			int rw = (dw<<16) / ow;
			int rh = (dh<<16) / oh;
			if( rw < rh ) {
				h = (oh * dw) / ow;
				y = (dh - h)/2;
				l = l * dw / ow;
				t = t * dw / ow + y;
				r = r * dw / ow;
				b = b * dw / ow + y;
			} else {
				w = (ow * dh) / oh;
				x = (dw - w)/2;
				l = l * dh / oh + x;
				t = t * dh / oh;
				r = r * dh / oh + x;
				b = b * dh / oh;
			}
		}
		if( LOGD ) Log.v( "pad", "left: " + l + ", top: " + t + ", right: " + (dw-r) + ", bottom:" + (dh-b) );
		layout.setPadding( l, t, dw-r, dh-b );
	}
	/*
	public static void adjustImageFrame( FrameLayout layout, int ow, int oh, int dw, int dh, Rect rect ) {
		if( LOGD ) Log.v("Movie","adjustImageFrame");
		int x, y, w, h;
		int l = rect.left;
		int t = rect.top;
		int r = rect.right;
		int b = rect.bottom;
		if( LOGD ) Log.v("Movie","l:"+l+", t:"+t+", r:"+r+", b:"+b);
		if( dw >= ow && dh >= oh ) { // 拡大
			int sw = dh * ow / oh;
			//int sh = dw * oh / ow;
			if( sw > dw ) { // 幅がはみ出す、高さ基準ではなく幅基準で拡大する
				h = (oh * dw) / ow;
				y = (dh - h)/2;
				l = l * dw / ow;
				t = t * dw / ow + y;
				r = r * dw / ow;
				b = b * dw / ow + y;
			} else {
				w = (ow * dh) / oh;
				x = (dw - w)/2;
				l = l * dh / oh + x;
				t = t * dh / oh;
				r = r * dh / oh + x;
				b = b * dh / oh;
			}
		} else { // 縮小
			int rw = (dw<<16) / ow;
			int rh = (dh<<16) / oh;
			if( rw < rh ) {
				h = (oh * dw) / ow;
				y = (dh - h)/2;
				l = l * dw / ow;
				t = t * dw / ow + y;
				r = r * dw / ow;
				b = b * dw / ow + y;
			} else {
				w = (ow * dh) / oh;
				x = (dw - w)/2;
				l = l * dh / oh + x;
				t = t * dh / oh;
				r = r * dh / oh + x;
				b = b * dh / oh;
			}
		}
		if( LOGD ) Log.v( "pad", "left: " + l + ", top: " + t + ", right: " + (dw-r) + ", bottom:" + (dh-b) );
		layout.setPadding( l, t, dw-r, dh-b );
	}
	*/
	/*
	private void adjustImageFrame( FrameLayout layout, int movw, int movh ) {
		final int dw = mWidth;
		final int dh = mHeight;
		if( dw >= movw  && dh >= movh ) {
			float rw = (float)dw / (float)movw ;
			float rh = (float)dh / (float)movh;
			if( rw < rh ) {
				// 幅の方が小さいので幅基準で拡大する
				final int fh = (dw * movh ) / movw;
				final int fw = (fh * movw) / movh;
				final int l = (dw - fw) / 2;
				final int r = dw - fw - l;
				final int t = (dh - fh) / 2;
				final int b = dh - fh - t;
				layout.setPadding( l, t, r, b );
			} else {
				// 高さの方が小さいので高さ基準で拡大する
				final int fw = (dh * movw ) / movh;
				final int fh = (fw * movh) / movw;
				final int l = (dw - fw) / 2;
				final int r = dw - fw - l;
				final int t = (dh - fh) / 2;
				final int b = dh - fh - t;
				layout.setPadding( l, t, r, b );
			}
		} else {
			final int w = movw;
			final int h = movh;

			int desth = (dw * h) / w;	// 幅を合わせる場合
			int destw = (dh * w) / h;	// 高さを合わせる場合
			if( desth <= dh ) {	// 高さの方が収まる
				final int t = (dh - desth) / 2;
				final int b = dh - desth - t;
				layout.setPadding( 0, t, 0, b );
			} else { // if( destw <= dw ) {	// 幅の方が収まる
				final int l = (dw - destw) / 2;
				final int r = dw - destw - l;
				layout.setPadding( l, 0, r, 0 );
			}
		}
	}
	*/
	public void close() {
		if( LOGD ) Log.v("Movie","close");
		if( mMediaPlayer != null ) {
			mMediaPlayer.stop();
		}
		if( mSourceStream != null ) {
			mSourceStream.close();
		}
		mCompletionListener.onCompletion(mMediaPlayer);
		if( mMediaPlayer != null ) {
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
		mCompletionListener = null;
		mFrameLayout = null;
	}

	public void stop() {
		if( LOGD ) Log.v("Movie","stop");
		if( mMediaPlayer != null && mMediaPlayer.isPlaying() ) {
			mMediaPlayer.stop();
		}
	}
	public void play() {
		if( LOGD ) Log.v("Movie","play");
		if( mMediaPlayer != null ) {
			mMediaPlayer.start();
		}
	}
	public void pause() {
		if( LOGD ) Log.v("Movie","pause");
		if( mMediaPlayer != null ) {
			mMediaPlayer.pause();
		}
	}
	public boolean isPlaying() {
		if( LOGD ) Log.v("Movie","isPlaying");
		if( mMediaPlayer != null ) {
			return mMediaPlayer.isPlaying();
		}
		return false;
	}
	public void setLoop( boolean loop ) {
		if( LOGD ) Log.v("Movie","setLoop");
		if( mMediaPlayer != null ) {
			mMediaPlayer.setLooping(loop);
		}
	}
	public boolean isLoop() {
		if( LOGD ) Log.v("Movie","isLoop");
		if( mMediaPlayer != null ) {
			return mMediaPlayer.isLooping();
		}
		return false;
	}
	public int getVideoWidth() {
		if( LOGD ) Log.v("Movie","getVideoWidth");
		if( mMediaPlayer != null ) {
			return mMediaPlayer.getVideoWidth();
		}
		return 0;
	}
	public int getVideoHeight() {
		if( LOGD ) Log.v("Movie","getVideoHeight");
		if( mMediaPlayer != null ) {
			return mMediaPlayer.getVideoHeight();
		}
		return 0;
	}
	// msec
	public int getDuration() {
		if( LOGD ) Log.v("Movie","getDuration");
		if( mMediaPlayer != null ) {
			return mMediaPlayer.getDuration();
		}
		return 0;
	}
	// msec
	public int getCurrentPosition() {
		if( LOGD ) Log.v("Movie","getCurrentPosition");
		if( mMediaPlayer != null ) {
			return mMediaPlayer.getCurrentPosition();
		}
		return 0;
	}
	public void seek( int msec ) {
		if( LOGD ) Log.v("Movie","seek");
		if( mMediaPlayer != null ) {
			mMediaPlayer.seekTo( msec );
		}
	}
	public void setVolume( float left, float right ) {
		if( LOGD ) Log.v("Movie","setVolume");
		if( mMediaPlayer != null ) {
			mMediaPlayer.setVolume( left, right );
		}
	}
	public void setVisible( boolean v ) {
		if( LOGD ) Log.v("Movie","setVisible");
		if( mFrameLayout != null ) {
			if( v ) {
				mFrameLayout.setVisibility( View.VISIBLE );
			} else {
				mFrameLayout.setVisibility( View.INVISIBLE );
			}
		}
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		if( what == MediaPlayer.MEDIA_ERROR_UNKNOWN ) {
			Log.e("MediapLayer", "Unknown Error:"+extra);
		} else if( what == MediaPlayer.MEDIA_ERROR_SERVER_DIED ) {
			Log.e("MediapLayer", "Media Server Died:"+extra);
		} else if( what == MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK ) {
			Log.e("MediapLayer", "Media Not Ready:"+extra);
		} else {
			Log.e("MediapLayer", "Unknown Error Code:"+what+", extra:"+extra);
		}
		if( mp.isPlaying() ) mp.stop();
		mp.release();
		mMediaPlayer = null;
		return true;
	}
}
