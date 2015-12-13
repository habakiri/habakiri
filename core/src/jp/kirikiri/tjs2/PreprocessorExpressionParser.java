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

import java.util.ArrayList;

public class PreprocessorExpressionParser extends LexBase {
	private static final int BUFFER_CAPACITY = 1024;

	private ArrayList<String> mIDs;
//	private int mResult;

	private int mValue;
	private int mUnlex;
	private int mUnValue;
	private boolean mIsUnlex;
	private TJS mTJS;

	public PreprocessorExpressionParser( TJS tjs, final String script ) {
		super( script );
		mIDs = new ArrayList<String>();
		mTJS = tjs;
		//mIsUnlex = false;
	}
	private int getNext() {
		if( mIsUnlex ) {
			mIsUnlex = false;
			mValue = mUnValue;
			return mUnlex;
		}
		mStream.skipSpace();
		int c = mStream.getC();
		switch( c ) {
		case '(':
			return Token.PT_LPARENTHESIS;
		case ')':
			return Token.PT_RPARENTHESIS;
		case ',':
			return Token.PT_COMMA;
		case '=':
			c = mStream.getC();
			if( c == '=' ) {
				return Token.PT_EQUALEQUAL;
			} else {
				mStream.ungetC();
				return Token.PT_EQUAL;
			}
		case '!':
			c = mStream.getC();
			if( c == '=' ) {
				return Token.PT_NOTEQUAL;
			} else {
				mStream.ungetC();
				return Token.PT_EXCLAMATION;
			}
		case '|':
			c = mStream.getC();
			if( c == '|' ) {
				return Token.PT_LOGICALOR;
			} else {
				mStream.ungetC();
				return Token.PT_VERTLINE;
			}
		case '&':
			c = mStream.getC();
			if( c == '&' ) {
				return Token.PT_LOGICALAND;
			} else {
				mStream.ungetC();
				return Token.PT_AMPERSAND;
			}
		case '^':
			return Token.PT_CHEVRON;
		case '+':
			return Token.PT_PLUS;
		case '-':
			return Token.PT_MINUS;
		case '*':
			return Token.PT_ASTERISK;
		case '/':
			return Token.PT_SLASH;
		case '%':
			return Token.PT_PERCENT;
		case '<':
			c = mStream.getC();
			if( c == '=' ) {
				return Token.PT_LTOREQUAL;
			} else {
				mStream.ungetC();
				return Token.PT_LT;
			}
		case '>':
			c = mStream.getC();
			if( c == '=' ) {
				return Token.PT_GTOREQUAL;
			} else {
				mStream.ungetC();
				return Token.PT_GT;
			}
		case '0':
		case '1':
		case '2':
		case '3':
		case '4':
		case '5':
		case '6':
		case '7':
		case '8':
		case '9': { // number
			mStream.ungetC();
			Number num = parseNumber();
			if( num == null ) return Token.PT_ERROR;
			mValue = num.intValue();
			return Token.PT_NUM;
		}
		case -1:
			return 0;
		}

		if( isWAlpha(c) == false && c != '_' ) {
			return Token.PT_ERROR;
		}
		StringBuilder str = new StringBuilder(BUFFER_CAPACITY);
		while( (isWAlpha(c) == true || isWDigit(c) == true || c == '_') && c != -1 ) {
			str.append( (char)c );
			c = mStream.getC();
		}
		mStream.ungetC();
		mIDs.add( str.toString() );
		mValue = mIDs.size() - 1;
		return Token.PT_SYMBOL;
	}
	private String getString( int index ) {
		return mIDs.get( index );
	}
	private void unLex( int lex, int value ) {
		mUnlex = lex;
		mUnValue = value;
		mIsUnlex = true;
	}

	// 単項演算子と括弧
	private int expr1() throws CompileException {
		int let = getNext();
		int result = 0;
		int flag = 1;
		boolean neg = false;
		switch( let ) {
		case Token.PT_EXCLAMATION:	// !
			neg = true;
			let = getNext();
			break;
		case Token.PT_PLUS:		// +
			let = getNext();
			break;
		case Token.PT_MINUS:	// -
			flag = -1;
			let = getNext();
			break;
		}
		if( let == Token.PT_NUM ) {
			result = mValue * flag;
			if( neg ) result = result != 0 ? 0 : 1;
		} else if( let == Token.PT_SYMBOL ) {
			int tmp = mValue;
			let = getNext();
			/*
			if( let == Token.PT_EQUAL ) {
				result = expression() * flag;
				if( neg ) result = result != 0 ? 0 : 1;
				mTJS.setPPValue( getString(tmp), result );
			} else {
			*/
			unLex( let, mValue );
			if( let == Token.PT_EQUAL ) {	// 代入規則
				result = tmp;
			} else {
				result = mTJS.getPPValue( getString(tmp) ) * flag;
				if( neg ) result = result != 0 ? 0 : 1;
			}
		} else if( let == Token.PT_LPARENTHESIS ) { // (？
			result = expression() * flag;
			if( neg ) result = result != 0 ? 0 : 1;
			let = getNext();
			if( let != Token.PT_RPARENTHESIS ) {	// )
				throw new CompileException(Error.NotFoundPreprocessorRPARENTHESISError);
			}
		} else {
			unLex( let, mValue );
		}
		return result;
	}
	// / * %
	private int expr2() throws CompileException {
		int result = expr1();
		int let = getNext();
		int tmp;
		while( let == Token.PT_ASTERISK || let == Token.PT_SLASH || let == Token.PT_PERCENT ) {
			switch( let ) {
			case Token.PT_ASTERISK:	// *
				result *= expr1();
				break;
			case Token.PT_SLASH:	// /
				tmp = expr1();
				if( tmp == 0 ) {
					throw new CompileException(Error.PreprocessorZeroDiv);
				}
				result /= tmp;
				break;
			case Token.PT_PERCENT:	// %
				result /= expr1();
				break;
			}
			let = getNext();
		}
		unLex( let, mValue );
		return result;
	}
	// + -
	private int expr3() throws CompileException {
		int result = expr2();
		int let = getNext();
		while( let == Token.PT_PLUS || let == Token.PT_MINUS ) {
			switch( let ) {
			case Token.PT_PLUS:
				result += expr2();
				break;
			case Token.PT_MINUS:
				result -= expr2();
			}
			let = getNext();
		}
		unLex( let, mValue );
		return result;
	}
	// < > <= >=
	private int expr4() throws CompileException {
		int result = expr3();
		int let = getNext();
		while( let == Token.PT_LT || let == Token.PT_GT || let == Token.PT_LTOREQUAL || let == Token.PT_GTOREQUAL ) {
			int tmp = expr3();
			switch( let ) {
			case Token.PT_LT:	// <
				result = result < tmp ? 1 : 0;
				break;
			case Token.PT_GT:	// >
				result = result > tmp ? 1 : 0;
				break;
			case Token.PT_LTOREQUAL:	// <=
				result = result <= tmp ? 1 : 0;
				break;
			case Token.PT_GTOREQUAL:	// >=
				result = result >= tmp ? 1 : 0;
				break;
			}
			let = getNext();
		}
		unLex( let, mValue );
		return result;
	}
	// != ==
	private int expr5() throws CompileException {
		int result = expr4();
		int let = getNext();
		while( let == Token.PT_NOTEQUAL || let == Token.PT_EQUALEQUAL ) {
			int tmp = expr4();
			switch( let ) {
			case Token.PT_NOTEQUAL:		// !=
				result = result != tmp ? 1 : 0;
				break;
			case Token.PT_EQUALEQUAL:	// ==
				result = result == tmp ? 1 : 0;
				break;
			}
			let = getNext();
		}
		unLex( let, mValue );
		return result;
	}
	// &
	private int expr6() throws CompileException {
		int result = expr5();
		int let = getNext();
		while( let == Token.PT_AMPERSAND ) {
			result = result & expr5();
			let = getNext();
		}
		unLex( let, mValue );
		return result;
	}
	// ^
	private int expr7() throws CompileException {
		int result = expr6();
		int let = getNext();
		while( let == Token.PT_CHEVRON ) {
			result = result ^ expr6();
			let = getNext();
		}
		unLex( let, mValue );
		return result;
	}
	// |
	private int expr8() throws CompileException {
		int result = expr7();
		int let = getNext();
		while( let == Token.PT_VERTLINE ) {
			result = result | expr7();
			let = getNext();
		}
		unLex( let, mValue );
		return result;
	}
	// &&
	private int expr9() throws CompileException {
		int result = expr8();
		int let = getNext();
		while( let == Token.PT_LOGICALAND ) {
			result = (result != 0) && (expr8() != 0) ? 1 : 0;
			let = getNext();
		}
		unLex( let, mValue );
		return result;
	}
	// ||
	private int expr10() throws CompileException {
		int result = expr9();
		int let = getNext();
		while( let == Token.PT_LOGICALOR ) {
			result = (result != 0 ) || (expr9() != 0) ? 1 : 0;
			let = getNext();
		}
		unLex( let, mValue );
		return result;
	}
	// =
	private int expr11() throws CompileException {
		int result = expr10();
		int let = getNext();
		while( let == Token.PT_EQUAL ) {
			int tmp = expr10();
			mTJS.setPPValue( getString(result), tmp );
			result = tmp;
			let = getNext();
		}
		unLex( let, mValue );
		return result;
	}
	// ,
	private int expr12() throws CompileException {
		int result = expr11();
		int let = getNext();
		while( let == Token.PT_COMMA ) {
			result = expr11();
			let = getNext();
		}
		unLex( let, mValue );
		return result;
	}
	private int expression() throws CompileException {
		return expr12();
	}

	public int parse() throws CompileException {
		int result = expression();
		return result;
	}
}

