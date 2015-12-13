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
 *
 */
package jp.kirikiri.tvp2env;

import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

import jp.kirikiri.tjs2.TJSException;

import com.jcraft.jogg.Packet;
import com.jcraft.jogg.Page;
import com.jcraft.jogg.StreamState;
import com.jcraft.jogg.SyncState;
import com.jcraft.jorbis.Block;
import com.jcraft.jorbis.Comment;
import com.jcraft.jorbis.DspState;
import com.jcraft.jorbis.Info;
import com.jcraft.jorbis.JOrbisException;
import com.jcraft.jorbis.VorbisFile;
import com.jcraft.jorbis.VorbisFile2;

/**
 *
 */
public class VorbisAudioInputStream extends AudioInputStream {

	InputStream mInputStream;
	AudioFormat mOutputFormat;
	AudioFormat mInputFormat;
	VorbisFile2 mVorbisFile;
	long mLength;
	long mMarkPos;
	byte[] mTemp;

	public VorbisAudioInputStream(InputStream stream, AudioFormat format, long length) throws IOException, TJSException {
		super(stream, format, length);
		mInputStream = stream;
		mOutputFormat = format;
		mLength = length;
		mMarkPos = -1;
		mTemp = new byte[2];
		try {
			mVorbisFile = new VorbisFile2(stream,null,0);
		} catch (JOrbisException e) {
			throw new TJSException("Cannot open vorbis file."+e.getLocalizedMessage());
		}
		Info info = mVorbisFile.getInfo(0);
		mInputFormat = new AudioFormat( AudioFormat.Encoding.PCM_SIGNED, info.rate, 16, info.channels, 2*info.channels, info.rate, false );
		// (AudioFormat.Encoding encoding, float sampleRate, int sampleSizeInBits,
		// int channels, int frameSize, float frameRate, boolean bigEndian)
	}
	/**
	 * 読み込みできる最大バイト数を返す
	 */
	public int available() {
		Info info = mVorbisFile.getInfo(0);
		return (int) (mVorbisFile.pcm_total(-1) * info.channels * 2);
	}

	/**
	 * オーディオ入力ストリームを閉る
	 */
	public void close() {
		try {
			mVorbisFile.close();
		} catch (IOException e) {
		}
	}

	/**
	 *  オーディオ入力ストリーム内のサウンドデータのオーディオ形式を取得する
	 */
	public AudioFormat getFormat() {
		return mInputFormat;
	}

	/**
	 * バイト数ではなくサンプルフレーム数で表される、ストリームの長さを取得
	 */
	public long getFrameLength() {
		return mVorbisFile.pcm_total(-1);
	}

	/**
	 * このオーディオ入力ストリームの現在の位置にマークを設定
	 */
	public void mark(int readlimit) {
		mMarkPos = mVorbisFile.pcm_tell();
	}

	/**
	 *  このオーディオ入力ストリームが、mark メソッドと reset メソッドをサポートしているかどうかを判定
	 */
	public boolean markSupported() { return true; }

	/**
	 * オーディオ入力ストリームからデータの次のバイトを読み込みます
	 */
	public int read() {
		int ret = mVorbisFile.read(mTemp, 2 );
		if( ret < 0 ) return -1;
		return ((int)mTemp[0])&0xff;
	}

	/**
	 * オーディオ入力ストリームから数バイトを読み込み、それをバッファー配列 b に格納します
	 */
	public int read(byte[] b) {
		return mVorbisFile.read(b, b.length );
	}

	/**
	 * オーディオストリームから指定されたデータの最大バイト数まで読み込み、読み込んだバイトを指定されたバイト配列に格納します
	 */
	public int read(byte[] b, int off, int len) {
		if( off != 0 ) {
			byte[] tmp = new byte[len];
			int ret = 0;
			ret = mVorbisFile.read( tmp, len );
			if( ret > 0 ) {
				System.arraycopy(tmp, 0, b, off, ret );
			}
			return ret;
		} else {
			return mVorbisFile.read(b, len );
		}
	}

	/**
	 * このオーディオ入力ストリームの位置を、入力ストリームで最後に mark メソッドが呼び出されたときの位置に再設定します
	 */
	public void reset() {
		mVorbisFile.pcm_seek(mMarkPos);
	}

	/**
	 * 指定したバイト数を、このオーディオ入力ストリームからスキップおよび破棄します
	 * @return 実際にスキップされたバイト数
	 */
	public long skip(long n) {
		long pos = mVorbisFile.pcm_tell();
		Info info = mVorbisFile.getInfo(0);
		long sampleskip = n / info.channels * 2;
		pos += sampleskip;
		mVorbisFile.pcm_seek(pos);
		return n;
	}
}
