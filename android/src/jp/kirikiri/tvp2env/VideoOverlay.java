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

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.media.MediaPlayer;

import jp.kirikiri.tjs2.BinaryStream;
import jp.kirikiri.tvp2.visual.Rect;
import jp.kirikiri.tvp2.visual.VideoOverlayClass;

public class VideoOverlay {
	private WeakReference<BaseActivity> mTarget;
	private android.graphics.Rect mRect;
	private WeakReference<MovePlayCallback> mMoviePlayer;
	private int mAudioBalance;
	private int mAudioVolume;

	private static ArrayList<WeakReference<MovePlayCallback>> mPlayerList;
	public static void initialize() {
		mPlayerList = new ArrayList<WeakReference<MovePlayCallback>>();
	}
	public static void finalizeApplication() {
		mPlayerList = null;
	}
	public static void stopAllPlayer() {
		if( mPlayerList != null ) {
			final int count = mPlayerList.size();
			for( int i = 0; i < count; i++ ) {
				MovePlayCallback mp = mPlayerList.get(i).get();
				if( mp != null ) {
					mp.stop();
				}
			}
		}
	}

	public VideoOverlay() {
		mTarget = new WeakReference<BaseActivity>(null);
		mRect = new android.graphics.Rect();
		mAudioVolume = 100000;
	}

	public void open(String name, BinaryStream stream, int mode, boolean visible  ) {
		mMoviePlayer = null;
		BaseActivity activity = mTarget.get();
		if( activity != null ) {
			boolean ishidelayer = true;
			if( mode == VideoOverlayClass.vomOverlay || mode == VideoOverlayClass.vomMixer ) {
				ishidelayer = true;
			} else if( mode == VideoOverlayClass.vomLayer ) {
				ishidelayer = false;
			}
			Thread.interrupted(); // ここで割り込みをクリアする
			activity.openVideo( this, stream, mRect, ishidelayer, visible );
			// 動画再生準備が終わるまで待つ
			//Log.v("Movie","Check interrupted");
			if( Thread.interrupted() != true ) {
				try {
					//Thread.sleep(5*1000);	// 5秒 経過するか、割り込みによって起こされるまで待つ
					//Log.v("Movie","Sleep");
					Thread.sleep(5000*1000);	// 5秒 経過するか、割り込みによって起こされるまで待つ
				} catch( InterruptedException e ) {
					// 割り込みで、Sleep解除
				}
			}
			setVolumeInternal();
		}
	}
	public void setMoviePlayer(MovePlayCallback player) {
		if( player != null ) {
			mMoviePlayer = new WeakReference<MovePlayCallback>(player);
			mPlayerList.add( new WeakReference<MovePlayCallback>(player) );
		} else {
			mMoviePlayer = null;
		}
		final int count = mPlayerList.size();
		for( int i = count-1; i >= 0; i-- ) {
			MovePlayCallback tmp = mPlayerList.get(i).get();
			if( tmp == null ) {
				mPlayerList.remove(i);
			}
		}
	}

	public void setVisible(boolean b) {
		MovePlayCallback palyer;
		if( mMoviePlayer != null && (palyer = mMoviePlayer.get()) != null ) {
			palyer.setVisible(b);
		}
	}

	public void setRect( Rect rect ) {
		mRect.set( rect.left, rect.top, rect.right, rect.bottom );
	}

	public void close() {
		if( mMoviePlayer != null ) {
			mMoviePlayer = null;
		}
	}

	public void play() {
		MovePlayCallback palyer;
		if( mMoviePlayer != null && (palyer = mMoviePlayer.get()) != null ) {
			palyer.play();
		}
	}

	public void stop() {
		MovePlayCallback palyer;
		if( mMoviePlayer != null && (palyer = mMoviePlayer.get()) != null ) {
			palyer.stop();
		}
	}

	public void pause() {
		MovePlayCallback palyer;
		if( mMoviePlayer != null && (palyer = mMoviePlayer.get()) != null ) {
			palyer.pause();
		}
	}

	public void rewind() {
		MovePlayCallback palyer;
		if( mMoviePlayer != null && (palyer = mMoviePlayer.get()) != null ) {
			palyer.seek(0);
		}
	}

	public void setStopFrame(int f) {
		// TODO 自動生成されたメソッド・スタブ
	}

	public void setDefaultStopFrame() {
		// TODO 自動生成されたメソッド・スタブ
	}

	public int getStopFrame() {
		// TODO 自動生成されたメソッド・スタブ
		return 0;
	}

	public void setPosition(long p) {
		MovePlayCallback palyer;
		if( mMoviePlayer != null && (palyer = mMoviePlayer.get()) != null ) {
			palyer.seek((int) p);
		}
	}

	public long getPosition() {
		MovePlayCallback palyer;
		if( mMoviePlayer != null && (palyer = mMoviePlayer.get()) != null ) {
			return palyer.getCurrentPosition();
		}
		return 0;
	}

	public void setFrame(int f) {
		// TODO 自動生成されたメソッド・スタブ
	}

	public int getFrame() {
		// TODO 自動生成されたメソッド・スタブ
		return 0;
	}

	public double getFPS() {
		// TODO 自動生成されたメソッド・スタブ
		return 0;
	}

	public int getNumberOfFrame() {
		// TODO 自動生成されたメソッド・スタブ
		return 0;
	}

	public long getTotalTime() {
		MovePlayCallback palyer;
		if( mMoviePlayer != null && (palyer = mMoviePlayer.get()) != null ) {
			return palyer.getDuration();
		}
		return 0;
	}

	public void setLoop(boolean l) {
		MovePlayCallback palyer;
		if( mMoviePlayer != null && (palyer = mMoviePlayer.get()) != null ) {
			palyer.setLoop(l);
		}
	}

	public boolean isOpen() {
		return ( mMoviePlayer != null );
	}

	public double getPlayRate() {
		// TODO 自動生成されたメソッド・スタブ
		return 1.0;
	}

	public void setPlayRate(double r) {
		// TODO 自動生成されたメソッド・スタブ
	}

	public int getAudioBalance() {
		return mAudioBalance;
	}

	public void setAudioBalance(int b) {
		mAudioBalance = b;
		setVolumeInternal();
	}

	public int getAudioVolume() {
		return mAudioVolume;
	}

	public void setAudioVolume(int v) {
		mAudioVolume = v;
		setVolumeInternal();
	}
	private void setVolumeInternal() {
		MovePlayCallback palyer;
		if( mMoviePlayer != null && (palyer = mMoviePlayer.get()) != null ) {
			float v = (float)100000 / 100000.0f;
			if( mAudioBalance != 0 ) {
				float left = 1.0f;
				float right = 1.0f;
				if( mAudioBalance > 0 ) {
					left = (float)(100000 - mAudioBalance) / 100000.0f;
				} else {
					right = (float)(100000 + mAudioBalance) / 100000.0f;
				}
				left *= v;
				right *= v;
				palyer.setVolume(left, right);
			} else {
				palyer.setVolume(v, v);
			}
		}
	}

	public int getNumberOfAudioStream() {
		// TODO 自動生成されたメソッド・スタブ
		return 1;
	}

	public void selectAudioStream(int n) {
		// TODO 自動生成されたメソッド・スタブ
	}

	public int getEnableAudioStreamNum() {
		// TODO 自動生成されたメソッド・スタブ
		return 0;
	}

	public void disableAudioStream() {
		// TODO 自動生成されたメソッド・スタブ
	}

	public int getNumberOfVideoStream() {
		// TODO 自動生成されたメソッド・スタブ
		return 1;
	}

	public void selectVideoStream(int n) {
		// TODO 自動生成されたメソッド・スタブ
	}

	public int getEnableVideoStreamNum() {
		// TODO 自動生成されたメソッド・スタブ
		return 0;
	}

	public void setMixingMovieAlpha(double a) {
		// TODO 自動生成されたメソッド・スタブ
	}

	public double getMixingMovieAlpha() {
		// TODO 自動生成されたメソッド・スタブ
		return 0;
	}

	public void setMixingMovieBGColor(int col) {
		// TODO 自動生成されたメソッド・スタブ
	}

	public int getMixingMovieBGColor() {
		// TODO 自動生成されたメソッド・スタブ
		return 0;
	}

	public int getVideoWidth() {
		MovePlayCallback palyer;
		if( mMoviePlayer != null && (palyer = mMoviePlayer.get()) != null ) {
			return palyer.getVideoWidth();
		}
		return 0;
	}

	public int getVideoHeight() {
		MovePlayCallback palyer;
		if( mMoviePlayer != null && (palyer = mMoviePlayer.get()) != null ) {
			return palyer.getVideoHeight();
		}
		return 0;
	}

	public void setWindow(WindowForm form) {
		if( form != null ) {
			mTarget = new WeakReference<BaseActivity>( form.getActivity() );
		}
	}


}
