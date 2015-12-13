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
package jp.kirikiri.tjs2;

import java.io.FileDescriptor;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * バイナリストリーム読み書きクラス
 */
public abstract class BinaryStream {

	/** 読み込みモード */
	public static final int READ = 0;
	/** 書き込みモード */
	public static final int WRITE = 1;
	/** 追記モード */
	public static final int APPEND = 2;
	/** 更新モード */
	public static final int UPDATE = 3;

	/** アクセスモードマスク */
	public static final int ACCESS_MASK = 0x0f;

	/** 先頭からのシーク */
	public static final int SEEK_SET = 0;
	/** 現在位置からのシーク */
	public static final int SEEK_CUR = 1;
	/** 終端位置からのシーク */
	public static final int SEEK_END = 2;

	/**
	 * シークする
	 * エラー時、位置は変更されない
	 * @param offset 基準位置からのオフセット
	 * @param whence 基準位置、SEEK_SET, SEEK_CUR, SEEK_END のいずれかを指定
	 * @return シーク後の現在位置
	 * @throws TJSException
	 */
	public abstract long seek( long offset, int whence ) throws TJSException;

	/**
	 * returns actually read size
	 * @throws TJSException
	 */
	public abstract int read( ByteBuffer buffer ) throws TJSException;

	/**
	 * returns actually read size
	 * @throws TJSException
	 */
	public abstract int read( byte[] buffer ) throws TJSException;

	/**
	 * ストリームからの読み込み
	 * @param b 読み込み先byte配列
	 * @param off 配列オフセット
	 * @param len 読み込みサイズ
	 * @return 実際に読み込んだ長さ。-1 の時ファイル終端
	 * @throws TJSException
	 */
	public abstract int read(byte[] b, int off, int len) throws TJSException;

	/**
	 * returns actually written size
	 */
	public abstract int write( final ByteBuffer buffer );

	/**
	 * returns actually written size
	 */
	public abstract int write( final byte[] buffer );

	public abstract void write(byte[] b, int off, int len);

	/**
	 * 1 バイトが出力ストリームに書き込まれます。
	 * 書き込まれるバイトは、引数 b の下位 8 ビットです。
	 * b の上位 24 ビットは無視されます。
	 * @param b
	 */
	public abstract void write(int b);

	/**
	 * close stream
	 */
	public abstract void close();

	/**
	 * the default behavior is raising a exception
	 * if error, raises exception
	 */
	public void setEndOfStorage() throws TJSException {
		throw new TJSException( Error.WriteError );
	}

	/**
	 * should re-implement for higher performance
	 * @throws TJSException
	 */
	public long getSize() throws TJSException {
		long orgpos = getPosition();
		long size = seek(0, SEEK_END );
		seek( orgpos, SEEK_SET );
		return size;
	}

	public long getPosition() throws TJSException {
		return seek( 0, SEEK_CUR );
	}

	public void setPosition( long pos ) throws TJSException {
		if( pos != seek( pos, SEEK_SET ) ) {
			throw new TJSException( Error.SeekError );
		}
	}

	public void readBuffer( ByteBuffer buffer ) throws TJSException {
		if( read( buffer ) != -1 ) {
			throw new TJSException( Error.ReadError );
		}
		buffer.flip();
	}
	public void writeBuffer( final ByteBuffer buffer ) throws TJSException {
		if( write( buffer ) != -1 ) {
			throw new TJSException( Error.WriteError );
		}
	}

	public abstract InputStream getInputStream();
	public abstract OutputStream getOutputStream();

	/**
	 * アーカイブ内のファイルかどうか判定する
	 * @return
	 */
	public boolean isArchive() {
		return false;
	}

	public abstract String getFilePath();

	public FileDescriptor getFileDescriptor() {
		return null;
	}
	public long getFileOffset() { return 0; }

	/*
	// reads little-endian integers
	public long readI64LE() throws TJSException {
		ByteBuffer buffer = ByteBuffer.allocate(8);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		readBuffer(buffer);
		return buffer.getLong();
	}
	public int readI32LE() throws TJSException {
		ByteBuffer buffer = ByteBuffer.allocate(4);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		readBuffer(buffer);
		return buffer.getInt();
	}
	public short readI16LE() throws TJSException {
		ByteBuffer buffer = ByteBuffer.allocate(4);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		readBuffer(buffer);
		return buffer.getShort();
	}
	*/
}

