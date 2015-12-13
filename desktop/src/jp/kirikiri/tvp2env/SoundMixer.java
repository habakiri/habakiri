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

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;

public class SoundMixer implements LineListener {
	private Mixer mCurrentMixer;

	private FloatControl mPanControl;
	private FloatControl mGainControl;
	private FloatControl mVolumeControl;
	private float mMinPanValue;
	private float mMaxPanValue;
	private float mMinGainValue;
	private float mMaxGainValue;
	private float mMinVolumeValue;
	private float mMaxVolumeValue;

	public void initialize() {
		// 一般的に利用されるオーディオフォーマットをサポートしているかテスト
		AudioFormat[] requestFormat = new AudioFormat[12];
		requestFormat[0]  = new AudioFormat( AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, false ); // 44.1kHz 16bit stereo
		requestFormat[1]  = new AudioFormat( AudioFormat.Encoding.PCM_SIGNED, 44100,  8, 2, 2, 44100, false ); // 44.1kHz 8bit stereo
		requestFormat[2]  = new AudioFormat( AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 1, 2, 44100, false ); // 44.1kHz 16bit mono
		requestFormat[3]  = new AudioFormat( AudioFormat.Encoding.PCM_SIGNED, 44100,  8, 1, 1, 44100, false ); // 44.1kHz 8bit mono
		requestFormat[4]  = new AudioFormat( AudioFormat.Encoding.PCM_SIGNED, 22050, 16, 2, 4, 22050, false ); // 22.05kHz 16bit stereo
		requestFormat[5]  = new AudioFormat( AudioFormat.Encoding.PCM_SIGNED, 22050,  8, 2, 2, 22050, false ); // 22.05kHz 8bit stereo
		requestFormat[6]  = new AudioFormat( AudioFormat.Encoding.PCM_SIGNED, 22050, 16, 1, 2, 22050, false ); // 22.05kHz 16bit mono
		requestFormat[7]  = new AudioFormat( AudioFormat.Encoding.PCM_SIGNED, 22050,  8, 1, 1, 22050, false ); // 22.05kHz 8bit mono
		requestFormat[8]  = new AudioFormat( AudioFormat.Encoding.PCM_SIGNED, 11025, 16, 2, 4, 11025, false ); // 11.025kHz 16bit stereo
		requestFormat[9]  = new AudioFormat( AudioFormat.Encoding.PCM_SIGNED, 11025,  8, 2, 2, 11025, false ); // 11.025kHz 8bit stereo
		requestFormat[10] = new AudioFormat( AudioFormat.Encoding.PCM_SIGNED, 11025, 16, 1, 2, 11025, false ); // 11.025kHz 16bit mono
		requestFormat[11] = new AudioFormat( AudioFormat.Encoding.PCM_SIGNED, 11025,  8, 1, 1, 11025, false ); // 11.025kHz 8bit mono

		DataLine.Info[] info = new DataLine.Info[12];
		for( int i = 0; i < requestFormat.length; i++ ) {
			info[i] = new DataLine.Info(SourceDataLine.class, requestFormat[i]);
		}

		mCurrentMixer = AudioSystem.getMixer(null);
		if( mCurrentMixer != null) {
			// デフォルトミキサーがサポートしているフォーマットを確認
			Mixer mixer = mCurrentMixer;
			Line.Info[] infos = mixer.getSourceLineInfo(); // ソースラインをサポートしているか確認
			if( infos.length > 0 ) {
				boolean supportAll = true;
				for( int j = 0; j < info.length; j++ ) {
					try {
						if( mixer.isLineSupported(info[j]) == false ) {
							supportAll = false;
							break;
						}
					} catch( IllegalArgumentException e ) {
						supportAll = false;
						break;
					}
				}
				if( supportAll ) { // 要求するフォーマットがサポートされているのでデフォルトミキサーを使う
					return;
				}
			}
		}

		// 要求するフォーマットをサポートしていないので、全てのミキサーの中からサポートしているものを探す
		Mixer.Info[] mixers = AudioSystem.getMixerInfo();
		final int count = mixers.length;
		for( int i = 0; i < count; i++ ) {
			Mixer mixer = AudioSystem.getMixer(mixers[i]);
			Line.Info[] infos = mixer.getSourceLineInfo();
			if( infos.length > 0 ) {
				boolean supportAll = true;
				for( int j = 0; j < info.length; j++ ) {
					try {
						if( mixer.isLineSupported(info[j]) == false ) {
							supportAll = false;
							break;
						}
					} catch( IllegalArgumentException e ) {
						supportAll = false;
						break;
					}
				}
				if( supportAll ) {
					mCurrentMixer = mixer;
					return; // 見付かったので、これを使う
				}
			}
		}
		// 要求するものをサポートするミキサーが見付からないので、デフォルトミキサーにしておく
		mCurrentMixer = AudioSystem.getMixer(null);
		if( mCurrentMixer == null ) return;

		try {
			mCurrentMixer.open();
			mCurrentMixer.addLineListener( this );
			mPanControl = (FloatControl) mCurrentMixer.getControl(FloatControl.Type.PAN);
			mGainControl = (FloatControl) mCurrentMixer.getControl(FloatControl.Type.MASTER_GAIN);
			mVolumeControl = (FloatControl) mCurrentMixer.getControl(FloatControl.Type.VOLUME);
			mMinPanValue = mPanControl.getMinimum();
			mMaxPanValue = mPanControl.getMaximum();
			mMinGainValue = mGainControl.getMinimum();
			mMaxGainValue = mGainControl.getMaximum();
			mMinVolumeValue = mVolumeControl.getMinimum();
			mMaxVolumeValue = mVolumeControl.getMaximum();
		} catch (LineUnavailableException e) {
		}
	}
	public void close() {
		if( mCurrentMixer != null ) {
			mCurrentMixer.close();
		}
	}
	@Override
	public void update(LineEvent event) {
		LineEvent.Type type = event.getType();
		if( LineEvent.Type.CLOSE.equals(type) ) {
		} else if( LineEvent.Type.OPEN.equals(type) ) {
		} else if( LineEvent.Type.START.equals(type) ) {
		} else if( LineEvent.Type.STOP.equals(type) ) {
		}
	}

	public void setPan( float pan ) {
		if( mPanControl == null ) return;
		if( pan < mMinPanValue ) pan = mMinPanValue;
		else if( pan > mMaxPanValue ) pan = mMaxPanValue;
		mPanControl.setValue( pan );
	}
	public float getPan() {
		if( mPanControl != null )
			return mPanControl.getValue();
		else
			return 0;
	}
	public void setGain( float gain ) {
		if( mGainControl == null ) return;
		if( gain < mMinGainValue ) gain = mMinGainValue;
		else if( gain > mMaxGainValue ) gain = mMaxGainValue;
		mGainControl.setValue(gain);
	}
	public float getGain() {
		if( mGainControl != null )
			return mGainControl.getValue();
		else
			return 0;
	}
	public void setVolume( float volume ) {
		if( mVolumeControl == null ) return;
		if( volume < mMinVolumeValue ) volume = mMinVolumeValue;
		else if( volume > mMaxVolumeValue ) volume = mMaxVolumeValue;
		mVolumeControl.setValue(volume);
	}
	public float getVolume() {
		if( mVolumeControl != null )
			return mVolumeControl.getValue();
		else
			return 0;
	}
	public SourceDataLine getSourceDataLine( DataLine.Info info ) throws LineUnavailableException {
		return (SourceDataLine) mCurrentMixer.getLine(info);
	}
	public void setGlobalVolume( int vol ) {
		if( vol < 0 ) vol = 0;
		else if( vol > 100000  ) vol = 100000;

		vol = vol - 100000;
		float v = vol / Math.abs(mMinGainValue);
		setGain( v );
	}
	public int getGlobalVolume() {
		float v = getGain();
		if( v > 0 ) {
			return 100000;
		} else {
			float vol = v * 100000 / Math.abs(mMinGainValue);
			vol = 100000 - vol;
			return (int)vol;
		}
	}

}
