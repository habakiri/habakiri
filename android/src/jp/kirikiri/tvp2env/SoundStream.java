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
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.media.MediaPlayer;
import android.util.Log;

import jp.kirikiri.tjs2.BinaryStream;
import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tvp2.sound.WaveSoundBufferNI;

public class SoundStream implements Runnable {

	private WeakReference<WaveSoundBufferNI> mOwner;

	private String mFileName;
	private Thread mThread;

	private float mMinPanValue;
	private float mMaxPanValue;
	private float mMinGainValue;
	private float mMaxGainValue;
	private int mPan;
	private int mVolume;

	private int mRequenstBufferSize;
	private boolean mLoop;
	private long mNumberOfRead;
	private long mLastRestFramePosition;

	private MediaPlayer mMediaPlayer;
	private static ArrayList<WeakReference<MediaPlayer>> mPlayerList;
	private static ArrayList<WeakReference<MediaPlayer>> mResumeList;
	public static void initialize() {
		mPlayerList = new ArrayList<WeakReference<MediaPlayer>>();
		mResumeList = null;
	}
	public static void finalizeApplication() {
		if( mPlayerList != null ) {
			final int count = mPlayerList.size();
			for( int i = 0; i < count; i++ ) {
				MediaPlayer mp = mPlayerList.get(i).get();
				if( mp != null ) {
					mp.release();
					mp = null;
				}
			}
			mPlayerList.clear();
		}
		mPlayerList = null;
		mResumeList = null;
	}
	public static void stopAllPlayer() {
		if( mPlayerList != null ) {
			final int count = mPlayerList.size();
			for( int i = 0; i < count; i++ ) {
				MediaPlayer mp = mPlayerList.get(i).get();
				if( mp != null ) {
					mp.stop();
				}
			}
		}
	}
	public static void pauseAllPlayer() {
		if( mPlayerList != null ) {
			if( mResumeList == null ) {
				mResumeList = new ArrayList<WeakReference<MediaPlayer>>();
			}
			mResumeList.clear();
			final int count = mPlayerList.size();
			for( int i = 0; i < count; i++ ) {
				MediaPlayer mp = mPlayerList.get(i).get();
				if( mp != null ) {
					if( mp.isPlaying() ) {
						mp.pause();
						mResumeList.add( new WeakReference<MediaPlayer>(mp) );
					}
				}
			}
		}
	}
	public static void resumeAllPlayer() {
		if( mResumeList != null ) {
			final int count = mResumeList.size();
			for( int i = 0; i < count; i++ ) {
				MediaPlayer mp = mResumeList.get(i).get();
				if( mp != null ) {
					mp.start();
				}
			}
			mResumeList.clear();
		}
	}
	private static void erasePlayer( MediaPlayer mp ) {
		if( mp == null ) return;
		if( mPlayerList != null ) {
			final int count = mPlayerList.size();
			for( int i = count-1; i >= 0; i-- ) {
				MediaPlayer cur = mPlayerList.get(i).get();
				if( mp == cur ) {
					mPlayerList.remove(i);
				} else if( cur == null ) {
					mPlayerList.remove(i);
				}
			}
		}
	}

	public SoundStream( WaveSoundBufferNI o ) {
		mOwner = new WeakReference<WaveSoundBufferNI>(o);
	}
	@Override
	public void run() {
	}
	protected void finalize() {
		stop();
		try {
			super.finalize();
		} catch (Throwable e) {
		}
	}

	public void openFromStream( BinaryStream stream, String filename ) throws IOException, TJSException {
		stop();
		mFileName = filename;
		mMediaPlayer = new MediaPlayer();
		mMediaPlayer.setDataSource( stream.getFileDescriptor(), stream.getFileOffset(), stream.getSize() );
		mMediaPlayer.prepare();
		mPlayerList.add( new WeakReference<MediaPlayer>(mMediaPlayer) );
	}
	public void play() {
		if( mMediaPlayer != null && mMediaPlayer.isPlaying() == false ) {
			setVolumeInternal();
			mMediaPlayer.setLooping(mLoop);
			mMediaPlayer.start();
		}
	}

	public void stop() {
		try {
			if( mMediaPlayer != null && mMediaPlayer.isPlaying() ) {
				erasePlayer( mMediaPlayer );
				mMediaPlayer.stop();
				mMediaPlayer.release();
				mMediaPlayer = null;
			}
		} catch( IllegalStateException e ) {
			if( mMediaPlayer != null ) {
				erasePlayer( mMediaPlayer );
				mMediaPlayer.release();
				mMediaPlayer = null;
			}
		}
	}

	private void setVolumeInternal() {
		float v = (float)mVolume / 100000.0f;
		if( mPan != 0 ) {
			float left = 1.0f;
			float right = 1.0f;
			if( mPan > 0 ) {
				left = (float)(100000 - mPan) / 100000.0f;
			} else {
				right = (float)(100000 + mPan) / 100000.0f;
			}
			left *= v;
			right *= v;
			mMediaPlayer.setVolume(left, right);
		} else {
			mMediaPlayer.setVolume(v, v);
		}
	}

	public void setVolume( int vol ) {
		if( vol < 0 ) vol = 0;
		else if( vol > 100000  ) vol = 100000;

		mVolume = vol;
		if( mMediaPlayer != null && mMediaPlayer.isPlaying() ) {
			setVolumeInternal();
		}
	}
	public int getVolume() {
		return mVolume;
	}

	// -100000 ～ 0 ～ 100000
	public int getPan() {
		return mPan;
	}

	public void setPan(int v) {
		mPan = v;
	}
	public boolean getLooping() {
		return mLoop;
	}
	public void setLooping(boolean b) {
		mLoop = b;
	}
	public boolean getPaused() {
		return false;
	}
	public void setPaused(boolean b) {
	}

	/**
	 * ms 単位での位置を取得する
	 * @return 現在位置
	 */
	public int getPosition() {
		if( mMediaPlayer != null && mMediaPlayer.isPlaying() ) {
			return mMediaPlayer.getCurrentPosition();
		}
		return (int)0;
	}
	/**
	 * ms 単位で位置を設定する
	 * @param pos 設定する位置
	 */
	public void setPosition(int pos) {
		if( mMediaPlayer != null ) {
			mMediaPlayer.seekTo(pos);
		}
	}
	/**
	 * サンプル 単位での位置を取得する
	 * @return 現在位置
	 */
 	public int getSamplePosition() {
		return (int)0;
	}

	/**
	 * サンプル 単位で位置を設定する
	 * @param sample 設定する位置
	 */
	public void setSamplePosition(int sample) {
	}

	/**
	 * 総再生時間を ms 単位で取得
	 * @return 総再生時間
	 */
	public int getTotalTime() {
		if( mMediaPlayer != null ) {
			return mMediaPlayer.getDuration();
		}
		return (int)0;
	}
	public int getFrequency() {
		return 0;
	}
	public void setFrequency(int freq) {
	}
	public int getGitsPerSample() {
		return 0;
	}
	public int getChannels() {
		return 2;
	}
	public int getBitsPerSample() {
		return 0;
	}
}
