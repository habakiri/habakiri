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
package jp.kirikiri.tvp2.utils;

import java.util.ArrayList;

/**
 * TODO 未実装
 * KAG の文法に従ってトークンを分割する
 *
 */
public class KAGTokenizer {
	// タグ(コマンド), コメント、ラベル、行頭のタブは無視、改行コード
	public enum KAGTokenType {
		TAG,
		LABEL,
		SCRIPT,
		TEXT,
		COMMENT,
		RETURN
	}
	static class Token {
		public KAGTokenType mType;
		public int mLineNo;
		public String mContents;
		public Token( KAGTokenType type, int lineno, String contents ) {
			mType = type;
			mLineNo = lineno;
			mContents = contents;
		}
	}
	static class TagToken extends Token {
		public TagToken( int lineno, String contents) {
			super(KAGTokenType.TAG, lineno, contents);
		}

		public String getTagName() {
			int index = mContents.indexOf(' '); // 区切り文字は\tの場合もあるので、そちらでも調べる必要あり
			return mContents.substring(0,index);
		}
	}
	static class LabelToken extends Token {
		public LabelToken( int lineno, String contents) {
			super(KAGTokenType.LABEL, lineno, contents);
		}
	}
	static class ScriptToken extends Token {
		public ScriptToken( int lineno, String contents) {
			super( KAGTokenType.SCRIPT, lineno, contents);
		}
	}
	static class CommentToken extends Token {
		public CommentToken( int lineno, String contents) {
			super(KAGTokenType.COMMENT, lineno, contents);
		}
	}
	private ArrayList<Token> mTokenList;
	private char[] mText;

	// テキスト終端は例外処理に任せる
	public void tokenize() {
		StringBuilder builder = new StringBuilder(1024);
		int pos = 0;
		int lineno = 0;
		boolean islinehead = true; // 行のはじめかどうか
		KAGTokenType currentToken = null;
		// スクリプト中かどうかの特殊処理は必要
		while( true ) {
			char c = mText[pos];
			if( islinehead ) {
				if( c == ';' ) { // コメント行
					currentToken = KAGTokenType.COMMENT;
					pos++;
					c = mText[pos];
					while( c != '\r' && c != '\n' ) { builder.append(c); }
					mTokenList.add( new CommentToken( lineno, builder.toString() ) );
					builder.delete(0, builder.length());

					// 改行を処理
					mTokenList.add( new Token( KAGTokenType.RETURN, lineno, null ) );
					lineno++;
					islinehead = true;
					char c1 = mText[pos+1];
					if( c == '\r' && c1 == '\n' ) { pos++; }
					continue;
				} else if( c == '*' ) { // ラベル
					currentToken = KAGTokenType.LABEL;
					pos++;
					c = mText[pos];
					while( c != '\r' && c != '\n' ) { builder.append(c); }
					mTokenList.add( new LabelToken( lineno, builder.toString() ) );
					builder.delete(0, builder.length());

					// 改行を処理
					mTokenList.add( new Token( KAGTokenType.RETURN, lineno, null ) );
					lineno++;
					islinehead = true;
					char c1 = mText[pos+1];
					if( c == '\r' && c1 == '\n' ) { pos++; }
					continue;
				} else if( c == '@' ) { // コマンド
					currentToken = KAGTokenType.TAG;
					pos++;
					c = mText[pos];
					while( c != '\r' && c != '\n' ) { builder.append(c); }
					mTokenList.add( new TagToken( lineno, builder.toString() ) );
					builder.delete(0, builder.length());

					// 改行を処理
					mTokenList.add( new Token( KAGTokenType.RETURN, lineno, null ) );
					lineno++;
					islinehead = true;
					char c1 = mText[pos+1];
					if( c == '\r' && c1 == '\n' ) { pos++; }
					continue;
				} else if( c == '\t') { // インデントは無視
					do {
						pos++;
						c = mText[pos];
					} while( c == '\t' );
				}
			}
			if( c == '[' ) {
				pos++;
				c = mText[pos];
				//if( c == '[' ) TODO 以下未実装
			}

			if( c == '\r' || c == '\n' ) {	// 改行
				mTokenList.add( new Token( KAGTokenType.RETURN, lineno, null ) );
				lineno++;
				pos++;
				islinehead = true;
				char c1 = mText[pos];
				if( c == '\r' && c1 == '\n' ) {
					pos++;
				}
			}
		}
	}
}
