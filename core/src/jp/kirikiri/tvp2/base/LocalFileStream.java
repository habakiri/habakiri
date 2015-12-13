/**
 ******************************************************************************
 * Copyright (c), Takenori Imoto
 * �� software http://www.kaede-software.com/
 * All rights reserved.
 ******************************************************************************
 * �\�[�X�R�[�h�`�����o�C�i���`�����A�ύX���邩���Ȃ������킸�A�ȉ��̏�����
 * �����ꍇ�Ɍ���A�ĔЕz����юg�p��������܂��B
 *
 * �E�\�[�X�R�[�h���ĔЕz����ꍇ�A��L�̒��쌠�\���A�{�����ꗗ�A����щ��L�Ɛ�
 *   �������܂߂邱�ƁB
 * �E�o�C�i���`���ōĔЕz����ꍇ�A�Еz���ɕt���̃h�L�������g���̎����ɁA��L��
 *   ���쌠�\���A�{�����ꗗ�A����щ��L�Ɛӏ������܂߂邱�ƁB
 * �E���ʂɂ����ʂ̋��Ȃ��ɁA�{�\�t�g�E�F�A����h���������i�̐�`�܂��͔̔�
 *   ���i�ɁA�g�D�̖��O�܂��̓R���g���r���[�^�[�̖��O���g�p���Ă͂Ȃ�Ȃ��B
 *
 * �{�\�t�g�E�F�A�́A���쌠�҂���уR���g���r���[�^�[�ɂ���āu����̂܂܁v��
 * ����Ă���A�����َ����킸�A���ƓI�Ȏg�p�\���A����ѓ���̖ړI�ɑ΂���K
 * �����Ɋւ���Öق̕ۏ؂��܂߁A�܂�����Ɍ��肳��Ȃ��A�����Ȃ�ۏ؂�����܂�
 * ��B���쌠�҂��R���g���r���[�^�[���A���R�̂�������킸�A���Q�����̌�������
 * ����킸�A���ӔC�̍������_��ł��邩���i�ӔC�ł��邩�i�ߎ����̑��́j�s�@
 * �s�ׂł��邩���킸�A���ɂ��̂悤�ȑ��Q����������\����m�炳��Ă����Ƃ�
 * �Ă��A�{�\�t�g�E�F�A�̎g�p�ɂ���Ĕ��������i��֕i�܂��͑�p�T�[�r�X�̒��B�A
 * �g�p�̑r���A�f�[�^�̑r���A���v�̑r���A�Ɩ��̒��f���܂߁A�܂�����Ɍ��肳���
 * ���j���ڑ��Q�A�Ԑڑ��Q�A�����I�ȑ��Q�A���ʑ��Q�A�����I���Q�A�܂��͌��ʑ��Q��
 * ���āA��ؐӔC�𕉂�Ȃ����̂Ƃ��܂��B
 ******************************************************************************
 * �{�\�t�g�E�F�A�́A�g���g��2 ( http://kikyou.info/tvp/ ) �̃\�[�X�R�[�h��Java
 * �ɏ������������̂��ꕔ�g�p���Ă��܂��B
 * �g���g��2 Copyright (C) W.Dee <dee@kikyou.info> and contributors
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
