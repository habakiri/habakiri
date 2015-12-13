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
import java.io.InputStream;
import java.util.ArrayList;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.SwingUtilities;

import jp.kirikiri.tjs2.BinaryStream;
import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tvp2.TVP;
import jp.kirikiri.tvp2.base.Storage;
import jp.kirikiri.tvp2.sound.WaveLabel;
import jp.kirikiri.tvp2.sound.WaveLoopManager;
import jp.kirikiri.tvp2.sound.WaveSoundBufferNI;
import jp.kirikiri.tvp2.utils.DebugClass;

public class SoundStream implements Runnable, LineListener {
	private static final int LABEL_CHECK_CYCLE = 16;	// 60msec

	WaveSoundBufferNI mOwner;

	private String mFileName;
	private Thread mThread;
	private SourceDataLine mLine;

	private FloatControl mPanControl;
	private FloatControl mGainControl;
	//private FloatControl mVolumeControl;
	//private FloatControl mSampleRateControl;
	private float mMinPanValue;
	private float mMaxPanValue;
	private float mMinGainValue;
	private float mMaxGainValue;
	//private float mMinVolumeValue;
	//private float mMaxVolumeValue;
	//private float mMinSampleRateValue;
	//private float mMaxSampleRateValue;

	private int mRequenstBufferSize;
	private AudioInputStream mAudioStream;
	private AudioFormat mFormat;
	private InputStream mInputStream;
	private boolean mLoop;
	private long mNumberOfRead;
	long mTotalSamples;
	WaveLoopManager mLoopManager;
	boolean mIsPlaing;
	private boolean mIsPaused;
	private long mStartOffset;
	ArrayList<String> mLavelEvents = new ArrayList<String>();
	//private long mLastRestFramePosition;

	private Runnable mUnloadEvent = new Runnable() {
		@Override
		public void run() {
			if( mOwner != null ) mOwner.setStatus(WaveSoundBufferNI.ssUnload);
		}
	};
	private Runnable mPlayEvent = new Runnable() {
		@Override
		public void run() {
			if( mOwner != null ) mOwner.setStatus(WaveSoundBufferNI.ssPlay);
		}
	};
	private Runnable mStopEvent = new Runnable() {
		@Override
		public void run() {
			if( mOwner != null ) mOwner.setStatus(WaveSoundBufferNI.ssStop);
		}
	};
	private Runnable mLabelEvent = new Runnable() {
		@Override
		public void run() {
			synchronized( mLavelEvents ) {
				final int count = mLavelEvents.size();
				if( count > 0 ) {
					for( int i = 0; i < count; i++ ) {
						final String name = mLavelEvents.get(i);
						if( mOwner != null ) mOwner.invokeLabelEvent( name );
					}
				}
			}
		}
	};
	private void invokeEvent( int state ) {
		switch( state ) {
		case WaveSoundBufferNI.ssPlay:
			SwingUtilities.invokeLater(mPlayEvent);
			break;
		case WaveSoundBufferNI.ssStop:
			SwingUtilities.invokeLater(mStopEvent);
			break;
		case WaveSoundBufferNI.ssUnload:
			SwingUtilities.invokeLater(mUnloadEvent);
			break;
		}
	}
	private void putLabelEvent( String name ) {
		synchronized( mLavelEvents ) {
			mLavelEvents.add(name);
		}
		SwingUtilities.invokeLater(mLabelEvent);
	}

	/**
	 * ラベルイベントを発生させるクラス
	 * 途中再生などは考えていない。
	 */
	class EventThread implements Runnable {
		@Override
		public void run() {
			ArrayList<WaveLabel> labels = mLoopManager.getLabels();
			if( labels != null && labels.size() > 0 ) {
				final int count = labels.size();
				// ラベルが存在する時のみ、処理する。それ以外はすぐにスレッド終了してしまう。
				int nextIndex = 0;
				long nextPosition = labels.get(nextIndex).Position;
				int sampleRate = (int) mFormat.getSampleRate();
				long lastPosition = 0;
				while( mIsPlaing ) {
					long pos = mLine.getLongFramePosition();
					if( mTotalSamples != 0 ) pos %= mTotalSamples;

					if( pos < lastPosition ) {
						// おそらくループで元に戻ったので、残りのイベント全部発生させる
						for( ; nextIndex < count; nextIndex++ ) {
							WaveLabel label = labels.get(nextIndex);
							putLabelEvent( label.Name );
						}
						// 位置を最初に戻す
						nextIndex = 0;
						nextPosition = labels.get(nextIndex).Position;
					}

					if( pos >= nextPosition && nextIndex < count ) {
						// 次のイベント位置を超えているので、イベントを発生させる
						WaveLabel label = labels.get(nextIndex);
						putLabelEvent( label.Name );
						nextIndex++;
						for( ; nextIndex < count; nextIndex++ ) {
							label = labels.get(nextIndex);
							nextPosition = label.Position;
							if( pos >= nextPosition ) {
								putLabelEvent( label.Name );
							} else {
								break;
							}
						}
					}

					// 待ち時間を計算する
					long wait = 0;
					if( pos < nextPosition ) {
						wait = nextPosition - pos;
						wait = (wait * 1000) / sampleRate - 1;
					}
					lastPosition = pos;

					if( wait > 0 ) {
						try {
							Thread.sleep(wait);
						} catch (InterruptedException e) {}
					}
				}
			}
		}
	}
	public SoundStream( WaveSoundBufferNI o ) {
		mOwner = o;
		mLoopManager = new WaveLoopManager();
	}
	@Override
	public void run() {
		if( mAudioStream == null || mLine == null ) return;

		int numBytesRead = 0;
		mLine.start();
		byte[] buffer = new byte[mRequenstBufferSize];
		int frameSize = mFormat.getFrameSize();
		mNumberOfRead = 0;
		try {
			do {
				numBytesRead = mAudioStream.read(buffer, 0, buffer.length);
				if( numBytesRead > 0 ) {
					mNumberOfRead += numBytesRead;
					mLine.write(buffer, 0, numBytesRead);
				} else if( mLoop ) {
					mTotalSamples = mNumberOfRead / frameSize;
					mAudioStream.reset();
					mNumberOfRead = 0;
					// mLastRestFramePosition = mLine.getLongFramePosition();
					numBytesRead = 0;
				}
			} while( mLine.isActive() && numBytesRead != -1 && mIsPaused == false );
			mLine.drain();
			mLine.close();
		} catch (IOException e) {
		}
		//invokeEvent(WaveSoundBufferNI.ssStop);
		mIsPlaing = false;
	}
	/*
	public void openFromFile( String filename ) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
		mFileName = filename;
		mAudioStream = AudioSystem.getAudioInputStream(new File(mFileName) );
		mAudioStream.mark(mAudioStream.available());
		mFormat = mAudioStream.getFormat();
		open( mFormat );
	}
	*/
	public void openFromStream( BinaryStream stream, String filename ) throws IOException, TJSException {
		clear();
		mFileName = filename;
		mInputStream = stream.getInputStream();
		mTotalSamples = 0;
		//mInputStream.mark(mInputStream.available());
		try {
			if( filename.endsWith(".ogg") ) {
				mAudioStream = new VorbisAudioInputStream(mInputStream,new AudioFormat( AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, false ),mInputStream.available());
			} else {
				mAudioStream = AudioSystem.getAudioInputStream(mInputStream);
			}
			String sliname = filename + ".sli";
			if( Storage.isExistentStorage(sliname) ) {
				BinaryStream slistream = Storage.createStream(sliname,BinaryStream.READ);
				long size = slistream.getSize();
				byte[] buf = new byte[(int)size];
				int readSize = slistream.read(buf);
				if( readSize > 0 ) {
					String slistr = new String( buf, 0, readSize, "UTF-8" );
					mLoopManager.readInformation(slistr);
					// test decode, sort
					mLoopManager.getLabels();
				}
				slistream.close();
			}
			mAudioStream.mark(mAudioStream.available());
			mAudioStream.reset();
			mFormat = mAudioStream.getFormat();
			mStartOffset = 0;
			open( mFormat );
		} catch (UnsupportedAudioFileException e) {
			throw new IOException();
		} catch (LineUnavailableException e) {
			throw new IOException();
		}
	}
	private void open( AudioFormat format ) throws LineUnavailableException {
		mRequenstBufferSize = (int) (format.getSampleRate() * format.getChannels() * format.getSampleSizeInBits()/8);
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
		//mLine = (SourceDataLine) AudioSystem.getLine(info);
		mLine = TVP.SoundMixer.getSourceDataLine(info);
		mLine.addLineListener(this);
		mLine.open(format, mRequenstBufferSize);
		mRequenstBufferSize = mLine.getBufferSize();
		try {
			mPanControl = (FloatControl) mLine.getControl(FloatControl.Type.PAN);
			mMinPanValue = mPanControl.getMinimum();
			mMaxPanValue = mPanControl.getMaximum();
		} catch( IllegalArgumentException e ) {
			mPanControl = null; // may be mono
		}
		mGainControl = (FloatControl) mLine.getControl(FloatControl.Type.MASTER_GAIN);
		mMinGainValue = mGainControl.getMinimum();
		mMaxGainValue = mGainControl.getMaximum();

		//mVolumeControl = (FloatControl) mLine.getControl(FloatControl.Type.VOLUME);
		//mSampleRateControl = (FloatControl) mLine.getControl(FloatControl.Type.SAMPLE_RATE);
		//mMinVolumeValue = mVolumeControl.getMinimum();
		//mMaxVolumeValue = mVolumeControl.getMaximum();
		//mMinSampleRateValue = mSampleRateControl.getMinimum();
		//mMaxSampleRateValue = mSampleRateControl.getMaximum();
	}
	public void play() {
		if( mLine != null && mIsPaused == false ) {
			mIsPlaing = true;
			mThread = new Thread(this);
			mThread.setName("Sound Stream : " + mFileName );
			mThread.start();

			ArrayList<WaveLabel> labels = mLoopManager.getLabels();
			if( labels != null && labels.size() > 0 ) {
				Thread ev = new Thread( new EventThread() );
				ev.setName("Sound Event Stream : " + mFileName );
				ev.start();
			}
		}
	}
	public void stop() {
		if( mLine != null ) {
			mLine.stop();
			mIsPlaing = false;
		}
	}

	public void clear() {
		stop();
		mIsPaused = false;
		//mLoop = false;
		mNumberOfRead = 0;
		mTotalSamples = 0;
		//invokeEvent(WaveSoundBufferNI.ssStop);
	}
	/**
	 * パンを設定する
	 * @param pan 有効な値の範囲は -1.0 (左チャネルのみ) ～ 1.0 (右チャネルのみ) です。デフォルトは 0.0 (中央)
	 */
	public void setPanInternal( float pan ) {
		if( mPanControl == null ) return;
		if( pan < mMinPanValue ) pan = mMinPanValue;
		else if( pan > mMaxPanValue ) pan = mMaxPanValue;
		if( mPanControl != null ) mPanControl.setValue( pan );
	}
	public float getPanInternal() {
		if( mPanControl == null ) return 0;
		else return mPanControl.getValue();
	}
	public void setGain( float gain ) {
		if( gain < mMinGainValue ) gain = mMinGainValue;
		else if( gain > mMaxGainValue ) gain = mMaxGainValue;
		if( mGainControl != null ) mGainControl.setValue(gain);
	}
	public float getGain() {
		if( mGainControl == null ) return 0;
		return mGainControl.getValue();
	}
	public void setFade( float from, float to, int msec ) {
		if( mGainControl != null ) mGainControl.shift( from, to, msec );
	}
	/*
	public void setVolumeInternal( float volume ) {
		if( volume < mMinVolumeValue ) volume = mMinVolumeValue;
		else if( volume > mMaxVolumeValue ) volume = mMaxVolumeValue;
		mVolumeControl.setValue(volume);
	}
	public float getVolumeInternal() { return mVolumeControl.getValue(); }
	*/

	@Override
	public void update(LineEvent event) {
		LineEvent.Type type = event.getType();
		if( LineEvent.Type.CLOSE.equals(type) ) {
			invokeEvent(WaveSoundBufferNI.ssUnload);
		} else if( LineEvent.Type.OPEN.equals(type) ) {
		} else if( LineEvent.Type.START.equals(type) ) {
			//invokeEvent(WaveSoundBufferNI.ssPlay);
		} else if( LineEvent.Type.STOP.equals(type) ) {
			invokeEvent(WaveSoundBufferNI.ssStop);
			mIsPlaing = false;
		}
	}

	public void setVolume( int vol ) {
		if( vol < 0 ) vol = 0;
		else if( vol > 100000  ) vol = 100000;

		//vol = vol - 100000;
		//float v = vol / Math.abs(mMinGainValue);
		float v = (float)vol / 100000.0f;
		v = (float)Math.log10(v) * 20.0f;
		setGain( v );
	}
	public int getVolume() {
		float v = getGain();
		v = (float) Math.pow(10.0, v/20.0) * 100000;
		if( v > 100000 ) {
			return 100000;
		} else if( v < 0 ) {
			return 0;
		} else {
			//float vol = v * 100000 / Math.abs(mMinGainValue);
			//vol = 100000 - vol;
			//return (int)vol;
			return (int) v;
		}
	}

	/**
	 * 音量をフェードする
	 * @param to : 目標値
	 * @param time : フェード期間
	 */
	public void fade( int to, int time ) {
		if( mGainControl == null ) return;
		if( to < 0 ) to = 0;
		else if( to > 100000  ) to = 100000;
		to = to - 100000;
		float v = to / Math.abs(mMinGainValue);
		float from = mGainControl.getValue();
		mGainControl.shift( from, v, time );
	}

	// -100000 ～ 0 ～ 100000
	public int getPan() {
		if( mPanControl == null ) return 0;
		float p = mPanControl.getValue();
		if( p < 0.0f ) {
			return (int) (p * 100000 / Math.abs(mMinPanValue));
		} else if( p > 0.0 ) {
			return (int) (p * 100000 / Math.abs(mMaxPanValue));
		} else {
			return 0;
		}
	}

	public void setPan(int v) {
		if( mPanControl == null ) return;
		if( v < 0 ) {
			mPanControl.setValue( v * Math.abs(mMinPanValue) / 100000 );
		} else if( v > 0 ) {
			mPanControl.setValue( v * Math.abs(mMaxPanValue) / 100000 );
		} else {
			mPanControl.setValue( 0.0f );
		}
	}
	public boolean getLooping() {
		return mLoop;
	}
	public void setLooping(boolean b) {
		mLoop = b;
	}
	public boolean getPaused() {
		return mIsPaused;
	}
	public void setPaused(boolean b) {
		mIsPaused = b;
		/*
		if( b ) {
			if( mLine != null && mLine.isActive() ) {
				long pos = mLine.getLongFramePosition();
				if( mTotalSamples != 0 ) pos %= mTotalSamples;
				mStartOffset = pos;
				mIsPlaing = false;
			}
		} else {
			if( mLine != null && mLine.isActive() != true ) {
				mIsPlaing = true;
				try {
					int frameSize = mFormat.getFrameSize();
					mAudioStream.reset();
					mAudioStream.skip(mStartOffset*frameSize);
					mThread = new Thread(this);
					mThread.setName("Sound Stream : " + mFileName );
					Thread ev = new Thread( new EventThread() );
					ev.setName("Sound Event Stream : " + mFileName );
					mThread.start();
					ev.start();
				} catch (IOException e) {
				}
			}
		}
		*/
	}

	public int getPosition() {
		if( mLine == null || mFormat == null ) return 0;
		long sample = mLine.getLongFramePosition();
		if( mTotalSamples != 0 ) sample %= mTotalSamples;
		long time = (long) (sample * 1000 / mFormat.getSampleRate());
		return (int) time;
	}
	public void setPosition(int pos) {
		// TODO 自動生成されたメソッド・スタブ
	}
	public int getSamplePosition() {
		if( mLine == null ) return 0;
		long sample = mLine.getLongFramePosition();
		if( mTotalSamples != 0 ) sample %= mTotalSamples;
		return (int) sample;
	}
	public void setSamplePosition(int sample) {
		if( mLine != null && mLine.isActive() != true ) {
			mStartOffset = sample;
		}
	}

	public int getTotalTime() {
		if( mAudioStream == null || mFormat == null ) return 0;
		long length = mAudioStream.getFrameLength();
		long time = (long) (length * 1000 / mFormat.getSampleRate());
		return (int) time;
	}
	public int getFrequency() {
		if( mFormat == null ) return 0;
		/*
		float rate = mSampleRateControl.getValue();
		//rate = rate * getSampleRate();
		return (int) rate;
		*/
		return (int) mFormat.getSampleRate();
	}
	public void setFrequency(int freq) {
		/*
		float f = freq;
		if( f < mMinSampleRateValue ) f = mMinSampleRateValue;
		else if( f > mMaxSampleRateValue ) f = mMaxSampleRateValue;
		mSampleRateControl.setValue(f);
		*/
	}
	public int getBitsPerSample() {
		if( mFormat == null ) return 0;
		return mFormat.getSampleSizeInBits();
	}
	public int getChannels() {
		if( mFormat == null ) return 0;
		return mFormat.getChannels();
	}
}
