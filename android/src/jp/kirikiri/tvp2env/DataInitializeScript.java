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

public class DataInitializeScript {
	static public final int SUCCESS = 0;

	public static class Download {
		public String mUrl;
		public String mPath;
		public String mFileSize;
		public String mMD5;
	}
	public static class Copy {
		public String mSource;
		public String mDest;
		public String mFileSize;
	}

	public static int parse( InputStream stream, ArrayList<Object> dest ) throws IOException {
		byte[] buff = new byte[1024];
		int readsize = 0;
		StringBuilder builder = new StringBuilder(1024);
		while( (readsize = stream.read(buff)) > 0 ) {
			builder.append( new String(buff,0,readsize,"UTF-8") );
		}
		buff = null;
		final int length = builder.length();
		char[] text = new char[length];
		builder.getChars(0, length, text, 0 );
		builder = null;

		int line = 1;
		for( int i = 0; i < length; i++ ) {
			char ch = text[i];
			if( ch == 'd' ) { // download
				if( (i+9) >= length ) return line;
				// download
				if( text[i+1] == 'o' && text[i+2] == 'w' && text[i+3] == 'n' &&
					text[i+4] == 'l' && text[i+5] == 'o' && text[i+6] == 'a' && text[i+7] == 'd' &&
					text[i+8] == ' ' ) {
					i+=9;
				} else {
					return line;
				}
				Download dl = new Download();
				// urlを得る
				int start = i;
				while( i < length && text[i] != ' ' && text[i] != '\t' && text[i] != '\r' && text[i] != '\n' ) i++;
				if( text[i] == '\r' || text[i] == '\n' || i >= length ) return line;
				int end = i;
				dl.mUrl = new String( text, start, end-start );

				while( text[i] == ' ' || text[i] == '\t' ) i++; // skip space

				// pathを得る
				start = i;
				while( i < length && text[i] != ' ' && text[i] != '\t' && text[i] != '\r' && text[i] != '\n' ) i++;
				if( text[i] == '\r' || text[i] == '\n' || i >= length ) return line;
				end = i;
				dl.mPath = new String( text, start, end-start );

				while( text[i] == ' ' || text[i] == '\t' ) i++; // skip space

				// ファイルサイズを得る
				start = i;
				while( i < length && text[i] >= '0' && text[i] <= '9' ) i++;
				if( (text[i] == ' ' || text[i] == '\t') == false || i >= length ) return line;
				end = i;
				dl.mFileSize = new String( text, start, end-start );

				while( text[i] == ' ' || text[i] == '\t' ) i++; // skip space

				// MD5を得る
				start = i;
				while( i < length && text[i] != ' ' && text[i] != '\t' && text[i] != '\r' && text[i] != '\n' ) i++;
				end = i;
				dl.mMD5 = new String( text, start, end-start );

				// 改行まで飛ばす
				while( i < length && text[i] != '\r' && text[i] != '\n' ) i++;

				dest.add(dl);
			} else if( ch == 'c' ) { // copy
				if( (i+5) >= length ) return line;
				// copy
				if( text[i+1] == 'o' && text[i+2] == 'p' && text[i+3] == 'y' && text[i+4] == ' ' ) {
					i+=5;
				} else {
					return line;
				}
				Copy cp = new Copy();
				// urlを得る
				int start = i;
				while( i < length && text[i] != ' ' && text[i] != '\t' && text[i] != '\r' && text[i] != '\n' ) i++;
				if( text[i] == '\r' || text[i] == '\n' || i >= length ) return line;
				int end = i;
				cp.mSource = new String( text, start, end-start );

				while( text[i] == ' ' || text[i] == '\t' ) i++; // skip space

				// pathを得る
				start = i;
				while( i < length && text[i] != ' ' && text[i] != '\t' && text[i] != '\r' && text[i] != '\n' ) i++;
				end = i;
				cp.mDest = new String( text, start, end-start );

				while( text[i] == ' ' || text[i] == '\t' ) i++; // skip space

				// ファイルサイズを得る
				start = i;
				while( i < length && text[i] >= '0' && text[i] <= '9' ) i++;
				end = i;
				cp.mFileSize = new String( text, start, end-start );

				// 改行まで飛ばす
				while( i < length && text[i] != '\r' && text[i] != '\n' ) i++;

				dest.add(cp);
			} else if( ch == '#' ) { // コメント行
				while( i < length && text[i] != '\r' && text[i] != '\n' ) i++;
			} else if( ch == '\r' || ch == '\n' || ch == ' ' || ch == '\t') { // 改行、スペースは無視
			} else {
				// 不明なコマンド
				return line;
			}
		}
		return SUCCESS;
	}
}
