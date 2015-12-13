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

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import android.content.res.AssetManager;

import jp.kirikiri.tjs2.BinaryStream;
import jp.kirikiri.tjs2.Error;
import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tvp2.msg.Message;
import jp.kirikiri.tvp2.utils.Random;

public class AssetFileStream extends BinaryStream {

	private InputStream mStream;
	private long mCurrentPosition;
	private long mSize;
	private String mFilePath;
	private AssetManager mManager;
	private String mLocalName;

	public AssetFileStream( AssetManager manager, final String origname, final String localname, int flag ) throws TJSException {
		mFilePath = origname;
		mManager = manager;
		mLocalName = localname;
		try {
			int access = flag & ACCESS_MASK;
			switch( access ) {
			case READ:
				mStream = manager.open( localname, AssetManager.ACCESS_RANDOM );
				mSize = mStream.available();
				mStream.mark((int) mSize); // 最初の位置にマーク
				break;
			case WRITE:
			case APPEND:
			case UPDATE:
				Message.throwExceptionMessage( Message.CannotOpenStorage, origname );
				break;
			}
		} catch (FileNotFoundException e) {
			Message.throwExceptionMessage( Message.CannotOpenStorage, origname );
		} catch (IOException e) {
			Message.throwExceptionMessage( Message.CannotOpenStorage, origname );
		}
		// push current tick as an environment noise
		Random.updateEnvironNoiseForTick();
	}
	protected void finalize() {
		if( mStream != null ) {
			close();
		}
		// push current tick as an environment noise
		// (timing information from file accesses may be good noises)
		//Random.updateEnvironNoiseForTick();
	}

	@Override
	public long getSize() { return mSize; }

	@Override
	public long getPosition() { return mCurrentPosition; }

	@Override
	public long seek(long offset, int whence) throws TJSException {
		try {
			if( offset == 0 && whence == SEEK_CUR ) {
				return mCurrentPosition;
			} else {
				switch(whence){
				case SEEK_SET:
					mStream.reset();
					if( offset != 0 ) mCurrentPosition = mStream.skip(offset);
					else mCurrentPosition = 0;
					break;
				case SEEK_CUR:
					mStream.reset();
					mCurrentPosition = mStream.skip( mCurrentPosition + offset );
					break;
				case SEEK_END:
					mStream.reset();
					mCurrentPosition = mStream.skip( mSize + offset );
					break;
				}
				return mCurrentPosition;
			}
		} catch (IOException e) {
			throw new TJSException( Error.SeekError );
		}
	}

	@Override
	public int read(ByteBuffer buffer) {
		try {
			if( buffer.hasArray() ) {
				byte[] buf = buffer.array();
				int ret = mStream.read(buf);
				if( ret > 0 ) {
					mCurrentPosition += ret;
				}
				return ret;
			} else {
				byte[] buf = new byte[buffer.capacity()];
				int ret = mStream.read(buf);
				if( ret > 0 ) {
					mCurrentPosition += ret;
					buffer.put( buf, 0, ret );
					buffer.flip();
				}
				return ret;
			}
		} catch (IOException e) {
			return 0;
		}
	}

	@Override
	public int read(byte[] buffer) {
		try {
			int ret = mStream.read(buffer);
			if( ret > 0 ) {
				mCurrentPosition += ret;
			}
			return ret;
		} catch (IOException e) {
			return 0;
		}
	}

	@Override
	public int read(byte[] b, int off, int len) {
		try {
			int ret = mStream.read(b,off,len);
			if( ret > 0 ) {
				mCurrentPosition += ret;
			}
			return ret;
		} catch (IOException e) {
			return 0;
		}
	}

	// asset に書き込みは出来ない
	@Override
	public int write(ByteBuffer buffer) { return 0; }
	@Override
	public int write(byte[] buffer) { return 0; }
	@Override
	public void write(byte[] b, int off, int len) { }
	@Override
	public void write(int b) { }

	@Override
	public void close() {
		try {
			mStream.close();
		} catch (IOException e) {
		}
		mStream = null;
	}

	@Override
	public InputStream getInputStream() { return mStream; }

	// asset に書き込みは出来ない
	@Override
	public OutputStream getOutputStream() { return null; }
	@Override
	public String getFilePath() {
		return mFilePath;
	}

	public FileDescriptor getFileDescriptor() {
		try {
			return mManager.openFd(mLocalName).getFileDescriptor();
		} catch (IOException e) {
			return null;
		}
	}
	public long getFileOffset() {
		try {
			return mManager.openFd(mLocalName).getStartOffset();
		} catch (IOException e) {
			return 0;
		}
	}
}
