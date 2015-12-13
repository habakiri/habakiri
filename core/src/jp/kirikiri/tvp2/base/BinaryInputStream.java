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

import java.io.IOException;
import java.io.InputStream;

import jp.kirikiri.tjs2.BinaryStream;
import jp.kirikiri.tjs2.TJSException;

public class BinaryInputStream extends InputStream {

	private BinaryStream mStream;
	private long mMark;
	private byte[] mBuffer;

	public BinaryInputStream( BinaryStream stream ) {
		super();
		mStream = stream;
		mBuffer = new byte[1];
	}
	/**
	 *
     この入力ストリームのメソッドの次の呼び出しによって、ブロックせずにこの入力ストリームから読み込むことができる (またはスキップできる) 推定バイト数を返します。
	 */
	@Override
	public int available() {
		try {
			return (int) mStream.getSize();
		} catch (TJSException e) {
			return 0;
		}
	}
	/**
	 *
     この入力ストリームを閉じて、そのストリームに関連するすべてのシステムリソースを解放します。
	 */
	@Override
	public void close() {
		mStream.close();
	}
	/**
	 * この入力ストリームの現在位置にマークを設定します。
	 */
	@Override
	public void mark(int readlimit) {
		try {
			mMark = mStream.getPosition();
		} catch (TJSException e) {
		}
	}
	/**
	 * 入力ストリームが mark と reset メソッドをサポートしているかどうかを判定します。
	 */
	@Override
	public boolean markSupported() { return true; }

	/**
	 *
     入力ストリームからデータの次のバイトを読み込みます。
	 */
	@Override
	public int read() throws IOException {
		int ret;
		try {
			ret = mStream.read(mBuffer);
			if( ret < 0 ) return -1;
			ret = (int)mBuffer[0] & (int)0xff;
			return ret;
		} catch (TJSException e) {
			throw new IOException();
		}
	}
	/**
	 * 入力ストリームから数バイトを読み込み、それをバッファー配列 b に格納します。
	 */
	@Override
	public int read(byte[] b) {
		try {
			return mStream.read(b);
		} catch (TJSException e) {
			return -1;
		}
	}
	/**
	 * 最大 len バイトまでのデータを、入力ストリームからバイト配列に読み込みます。
	 */
	@Override
	public int read(byte[] b, int off, int len) {
		try {
			return mStream.read(b,off,len);
		} catch (TJSException e) {
			return -1;
		}
	}
	/**
	 * このストリームの位置を、入力ストリームで最後に mark メソッドが呼び出されたときのマーク位置に再設定します。
	 */
	@Override
	public void reset() {
		try {
			mStream.setPosition(mMark);
		} catch (TJSException e) {
		}
	}
	/**
	 *  この入力ストリームから n バイト分をスキップおよび破棄します
	 */
	@Override
	public long skip(long n) {
		try {
			long size = mStream.getSize();
			long pos = mStream.getPosition();
			if( (pos+n) > size ) n = size - pos;
			mStream.setPosition(pos+n);
			return n;
		} catch (TJSException e) {
			return 0;
		}

	}
}
