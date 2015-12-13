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
package jp.kirikiri.tvp2.base;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import jp.kirikiri.tjs2.BinaryStream;
import jp.kirikiri.tjs2.Error;
import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tvp2.msg.Message;
import jp.kirikiri.tvp2.utils.Random;

public class LocalFileStream extends BinaryStream {

	private RandomAccessFile mRandomAccessFile;
	private FileChannel mFilcChannel;
	private String mFilePath;

	public LocalFileStream( final String origname, final String localname, int flag ) throws TJSException {
		mFilePath = origname;
		try {
			File file = Storage.getCaseInsensitiveFile(localname);
			int access = flag & ACCESS_MASK;
			switch( access ) {
			case READ:
				mRandomAccessFile= new RandomAccessFile(file,"r");
				mFilcChannel = mRandomAccessFile.getChannel();
				break;
			case WRITE: {
				File parent = file.getParentFile();
				if( file.exists() ) {
					if( file.isFile() ) {
						file.delete();
					}
				}
				if( parent.exists() == false ) {
					parent.mkdirs();
				}
				mRandomAccessFile = new RandomAccessFile(file,"rws");
				mFilcChannel = mRandomAccessFile.getChannel();
				break;
			}
			case APPEND: {
				File parent = file.getParentFile();
				if( parent.exists() == false ) {
					parent.mkdirs();
				}
				mRandomAccessFile = new RandomAccessFile(file,"rws");
				mFilcChannel = mRandomAccessFile.getChannel();
				break;
			}
			case UPDATE: {
				if( file.exists() == false ) {
					Message.throwExceptionMessage( Message.CannotOpenStorage, origname );
				}
				mRandomAccessFile = new RandomAccessFile(file,"rws");
				mFilcChannel = mRandomAccessFile.getChannel();
				break;
			}
			}
			if( access == APPEND ) {
				mFilcChannel.position(mFilcChannel.size());
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
		if( mRandomAccessFile != null ) {
			close();
		}
		// push current tick as an environment noise
		// (timing information from file accesses may be good noises)
		//Random.updateEnvironNoiseForTick();
	}
	@Override
	public long seek(long offset, int whence) throws TJSException {
		try {
			if( offset == 0 ) {
				return mFilcChannel.position();
			} else {
				switch(whence){
				case SEEK_SET:
					mFilcChannel.position( offset );
					break;
				case SEEK_CUR:
					mFilcChannel.position( mFilcChannel.position() + offset );
					break;
				case SEEK_END:
					mFilcChannel.position( mFilcChannel.size() + offset );
					break;
				}
				return mFilcChannel.position();
			}
		} catch (IOException e) {
			throw new TJSException( Error.SeekError );
		}
	}

	@Override
	public int read(ByteBuffer buffer) {
		try {
			return mFilcChannel.read(buffer);
		} catch (IOException e) {
			return 0;
		}
	}

	@Override
	public int write(ByteBuffer buffer) {
		try {
			return mFilcChannel.write(buffer);
		} catch (IOException e) {
			return 0;
		}
	}
	@Override
	public long getPosition() {
		try {
			return mFilcChannel.position();
		} catch (IOException e) {
			return 0;
		}
	}

	@Override
	public void setPosition( long pos ) throws TJSException {
		try {
			mFilcChannel.position(pos);
		} catch (IOException e) {
			throw new TJSException( Error.SeekError );
		}
	}

	@Override
	public void close() {
		if( mRandomAccessFile == null ) return;
		try {
			mRandomAccessFile.close();
			mRandomAccessFile = null;
			mFilcChannel = null;
		} catch (IOException e) {
		}
	}

	public void setEndOfStorage() throws TJSException {
		Message.throwExceptionMessage(Message.WriteError);
	}

	public long getSize() {
		try {
			return mFilcChannel.size();
		} catch (IOException e) {
			return 0;
		}
	}

	FileChannel getHandle() { return mFilcChannel; }

	@Override
	public int read(byte[] buffer) {
		try {
			return mRandomAccessFile.read(buffer);
		} catch (IOException e) {
			return 0;
		}
	}
	@Override
	public int read(byte[] b, int off, int len) {
		try {
			return mRandomAccessFile.read(b,off,len);
		} catch (IOException e) {
			return 0;
		}
	}

	@Override
	public int write( final byte[] buffer ) {
		long pos;
		try {
			pos = mRandomAccessFile.getFilePointer();
			mRandomAccessFile.write(buffer);
			return (int) (mRandomAccessFile.getFilePointer() - pos);
		} catch (IOException e) {
			return 0;
		}
	}

	@Override
	public void write(byte[] b, int off, int len) {
		try {
			mRandomAccessFile.write(b,off,len);
		} catch (IOException e) {
		}
	}

	@Override
	public void write(int b) {
		try {
			mRandomAccessFile.writeByte( b );
		} catch (IOException e) {
		}
	}

	@Override
	public InputStream getInputStream() {
		return new BinaryInputStream(this);
	}

	@Override
	public OutputStream getOutputStream() {
		return new BinaryOutputStream(this);
	}

	@Override
	public String getFilePath() {
		return mFilePath;
	}
	public FileDescriptor getFileDescriptor() {
		try {
			return mRandomAccessFile.getFD();
		} catch (IOException e) {
			return null;
		}
	}
}
