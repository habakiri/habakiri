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

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class LexicalAnalyzer {
	//private static final String TAG = "Lexer";
	//private static final boolean LOGD = false;

	private boolean mIsFirst;
	private boolean mIsExprMode;
	private boolean mResultNeeded;
	private boolean mRegularExpression;
	private boolean mBareWord;
	private boolean mDicFunc;	// dicfunc quick-hack

	private int mValue;
	private int mPrevToken;
	private int mPrevPos;
	private int mNestLevel;
	private int mIfLevel;

	private static final int BUFFER_CAPACITY = 1024;

	//private static final int CARRIAGE_RETURN = 13;
	//private static final int LINE_FEED = 10;

	private static final int CR = 13;
	private static final int LF = 10;
	private static final int TAB = 0x09;
	private static final int SPACE = 0x20;

	private static final int NOT_COMMENT = 0;
	//private static final int UNCLOSED_COMMENT = -1;
	private static final int CONTINUE = 1;
	private static final int ENDED = 2;

	// String Status
	private static final int NONE = 0;
	private static final int DELIMITER = 1;
	private static final int AMPERSAND = 2;
	private static final int DOLLAR = 3;

	private LongQue mRetValDeque; // 下位がtoken,上位がvalue index
	private ArrayList<EmbeddableExpressionData> mEmbeddableExpressionDataStack;
	private ArrayList<Object> mValues;
	Variant getValue(int idx) { return new Variant(mValues.get(idx)); }
	String getString( int idx ) {
		Object ret = mValues.get(idx);
		if( ret instanceof String ) {
			return (String)ret;
		} else {
			return null;
		}
	}

	private Compiler mBlock;
	private char[] mText;
	private int mCurrent;
	private int mStringStatus;
	private static StringBuilder mWorkBuilder;
	static public void initialize() {
		mWorkBuilder = new StringBuilder(BUFFER_CAPACITY);
	}
	static public void finalizeApplication() {
		mWorkBuilder = null;
	}

	public LexicalAnalyzer( Compiler block, String script, boolean isexpr, boolean resultneeded ) {

		mRetValDeque = new LongQue();
		mEmbeddableExpressionDataStack = new ArrayList<EmbeddableExpressionData>();
		mValues = new ArrayList<Object>();

		mBlock = block;
		mIsExprMode = isexpr;
		mResultNeeded = resultneeded;
		mPrevToken = -1;
		final int scriptLen = script.length();
		if( mIsExprMode ) {
			mText = new char[scriptLen+2];
			script.getChars(0, scriptLen, mText, 0 );
			mText[scriptLen] = ';';
			mText[scriptLen+1] = 0;
			//mStream = new StringStream(script+";");
		} else {
			if( script.startsWith("#!") == true ) {
				// #! を // に置換
				mText = new char[scriptLen+1];
				script.getChars(2, scriptLen, mText, 2 );
				mText[0] = mText[1] = '/';
				mText[scriptLen] = 0;
				//mStream = new StringStream( "//" + script.substring(2));
			} else {
				mText = new char[scriptLen+1];
				script.getChars(0, scriptLen, mText, 0 );
				mText[scriptLen] = 0;
				//mStream = new StringStream(script);
			}
		}
		if( CompileState.mEnableDicFuncQuickHack ) { //----- dicfunc quick-hack
			//mDicFunc = false; // デフォルト値なので入れる必要なし
			//if( mIsExprMode && (script.startsWith("[") == true || script.startsWith("%[") == true) ) {
			char c = script.charAt(0);
			if( mIsExprMode && (c == '[' || (c == '%' && script.charAt(1) == '[')) ) {
				mDicFunc = true;
			}
		}

		//mIfLevel = 0;
		//mPrevPos = 0;
		//mNestLevel = 0;
		mIsFirst = true;
		//mRegularExpression = false;
		//mBareWord = false;
		putValue(null);
	}
	private final int skipComment() throws CompileException {
		final char[] ptr = mText;
		int cur = mCurrent;
		if( ptr[cur] != '/' ) return NOT_COMMENT;
		char c = ptr[cur+1];
		if( c == '/' ) {
			// line comment
			cur += 2;
			c = ptr[cur];
			while( (c != 0) && (c != CR && c != LF ) ) { cur++; c = ptr[cur]; }
			if( c != 0 && c == CR ) {
				cur++; c = ptr[cur];
				if( c == LF ) {
					cur++; c = ptr[cur];
				}
			}
			//if( c == 0 ) return ENDED;
			//skipSpace();
			while( c != 0 && c <= SPACE && (c == CR || c == LF|| c == TAB || c == SPACE) ) { cur++; c = ptr[cur]; }
			mCurrent = cur;
			if( c == 0 ) return ENDED;
			return CONTINUE;
		} else if( c == '*' ) {
			// ブロックコメント
			cur += 2;
			c = ptr[cur];
			if( c == 0 ) {
				mCurrent = cur;
				throw new CompileException( Error.UnclosedComment, mBlock, cur );
			}

			int level = 0;
			while(true) {
				if( c == '/' && ptr[cur+1] == '*' ) {
					// コメントのネスト
					level++;
				} else if( c == '*' && ptr[cur+1] == '/' ) {
					if( level == 0 ) {
						cur += 2;
						c = ptr[cur];
						break;
					}
					level--;
				}
				//if( !next() ) throw new CompileException( Error.UnclosedComment, mBlock, mCurrent );
				cur++; c = ptr[cur];
				if( c == CR && ptr[cur+1] == LF ) { cur++; c = ptr[cur]; }
				if( c == 0 ) {
					mCurrent = cur;
					throw new CompileException( Error.UnclosedComment, mBlock, cur );
				}
			}
			//if( mText[mCurrent] == 0 ) return ENDED;
			//skipSpace();
			while( c != 0 && c <= SPACE && (c == CR || c == LF|| c == TAB || c == SPACE) ) { cur++; c = ptr[cur]; }
			mCurrent = cur;
			if( c == 0 ) return ENDED;
			return CONTINUE;
		}
		return NOT_COMMENT;
	}

	static private final int TJS_IEEE_D_SIGNIFICAND_BITS = 52;
	static private final int TJS_IEEE_D_EXP_MIN = -1022;
	static private final int TJS_IEEE_D_EXP_MAX = 1023;
	static private final long TJS_IEEE_D_EXP_BIAS = 1023;
	private boolean parseExtractNumber( final int basebits ) {
		boolean point_found = false;
		boolean exp_found = false;
		//int offset = mCurrent;
		final char[] ptr = mText;
		int cur = mCurrent;
		char c = ptr[cur];
		while( c != 0 ) {
			if( c == '.' && point_found == false && exp_found == false ) {
				point_found = true;
				//next();
				cur++; c = ptr[cur];
				if( c == CR && ptr[cur+1] == LF ) { cur++; c = ptr[cur]; }
			} else if( (c == 'p' || c == 'P') && exp_found == false ) {
				exp_found = true;
				//next();
				cur++; c = ptr[cur];
				//if( c == CR && ptr[cur+1] == LF ) { cur++; c = ptr[cur]; }
				//skipSpace();
				while( c != 0 && c <= SPACE && (c == CR || c == LF || c == TAB || c == SPACE) ) { cur++; c = ptr[cur]; }
				if( c == '+' || c == '-' ) {
					//next();
					cur++; c = ptr[cur];
					//if( c == CR && ptr[cur+1] == LF ) { cur++; c = ptr[cur]; }
					//skipSpace();
					while( c != 0 && c <= SPACE && (c == CR || c == LF || c == TAB || c == SPACE) ) { cur++; c = ptr[cur]; }
				}
			} else if( (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F') ) {
				if( basebits == 3 ) {
					if( c < '0' || c > '7') break;
				} else if( basebits == 1 ) {
					if( c != '0' && c != '1') break;
				}
				//next();
				cur++; c = ptr[cur];
				if( c == CR && ptr[cur+1] == LF ) { cur++; c = ptr[cur]; }
			} else {
				break;
			}
		}
		//mCurrent = offset;
		return point_found || exp_found;
	}
	// base
	// 16進数 : 4
	// 2進数 : 1
	// 8進数 : 3
	private Double parseNonDecimalReal( boolean sign, final int basebits ) {
		long main = 0;
		int exp = 0;
		int numsignif = 0;
		boolean pointpassed = false;

		final char[] ptr = mText;
		int cur = mCurrent;
		char c = ptr[cur];
		while( c != 0 ){
			if( c == '.' ) {
				pointpassed = true;
			} else if( c == 'p' || c == 'P' ) {
				//next();
				cur++; c = ptr[cur];
				//if( c == CR && ptr[cur+1] == LF ) { cur++; c = ptr[cur]; }
				//skipSpace();
				while( c != 0 && c <= SPACE && (c == CR || c == LF || c == TAB || c == SPACE) ) { cur++; c = ptr[cur]; }

				boolean biassign = false;
				if( c == '+' ) {
					biassign = false;
					//next();
					cur++; c = ptr[cur];
					//if( c == CR && ptr[cur+1] == LF ) { cur++; c = ptr[cur]; }
					//skipSpace();
					while( c != 0 && c <= SPACE && (c == CR || c == LF || c == TAB || c == SPACE) ) { cur++; c = ptr[cur]; }
				}

				if( c == '-' ) {
					biassign = true;
					//next();
					cur++; c = ptr[cur];
					//if( c == CR && ptr[cur+1] == LF ) { cur++; c = ptr[cur]; }
					//skipSpace();
					while( c != 0 && c <= SPACE && (c == CR || c == LF || c == TAB || c == SPACE) ) { cur++; c = ptr[cur]; }
				}

				int bias = 0;
				while( c >= '0' && c <= '9' ) {
					bias *= 10;
					bias += c - '0';
					//next();
					cur++; c = ptr[cur];
					if( c == CR && ptr[cur+1] == LF ) { cur++; c = ptr[cur]; }
				}
				if( biassign ) bias = -bias;
				exp += bias;
				break;
			} else {
				int n = -1;
				if( basebits == 4 ) {
					if(c >= '0' && c <= '9') n = c - '0';
					else if(c >= 'a' && c <= 'f') n = c - 'a' + 10;
					else if(c >= 'A' && c <= 'F') n = c - 'A' + 10;
					else break;
				} else if( basebits == 3 ) {
					if(c >= '0' && c <= '7') n = c - '0';
					else break;
				} else if( basebits == 1 ) {
					if(c == '0' || c == '1') n = c - '0';
					else break;
				}

				if( numsignif == 0 ) {
					int b = basebits - 1;
					while( b >= 0 ) {
						if( ((1<<b) & n) != 0 ) break;
						b--;
					}
					b++;
					if( b != 0 ) {
						// n is not zero
						numsignif = b;
						main |= ((long)n << (64-numsignif));
						if( pointpassed )
							exp -= (basebits - b + 1);
						else
							exp = b - 1;
					} else {
						// n is zero
						if( pointpassed ) exp -= basebits;
					}
				} else {
					// append to main
					if( (numsignif + basebits) < 64 ) {
						numsignif += basebits;
						main |= ((long)n << (64-numsignif));
					}
					if( pointpassed == false ) exp += basebits;
				}
			}
			//next();
			cur++; c = ptr[cur];
			if( c == CR && ptr[cur+1] == LF ) { cur++; c = ptr[cur]; }
		}
		mCurrent = cur;
		main >>>= (64 - 1 - TJS_IEEE_D_SIGNIFICAND_BITS);
		if( main == 0 ) {
			return Double.valueOf(0.0);
		}
		main &= ((1L << TJS_IEEE_D_SIGNIFICAND_BITS) - 1L);
		if( exp < TJS_IEEE_D_EXP_MIN ) {
			return Double.valueOf(0.0);
		}
		if( exp > TJS_IEEE_D_EXP_MAX ) {
			if( sign ) {
				return Double.valueOf( Double.NEGATIVE_INFINITY );
			} else {
				return Double.valueOf( Double.POSITIVE_INFINITY );
			}
		}
		// compose IEEE double
		//double d = Double.longBitsToDouble(0x8000000000000000L | ((exp + TJS_IEEE_D_EXP_BIAS) << 52) | main);
		double d = Double.longBitsToDouble( (((long)exp + TJS_IEEE_D_EXP_BIAS) << 52) | main);
		if( sign ) d = -d;
		return Double.valueOf(d);
	}
	private Integer parseNonDecimalInteger16( boolean sign ) {
		long v = 0;
		final char[] ptr = mText;
		int cur = mCurrent;
		char c = ptr[cur];
		while( c != 0 ) {
			int n = -1;
			if(c >= '0' && c <= '9') n = c - '0';
			else if(c >= 'a' && c <= 'f') n = c - 'a' + 10;
			else if(c >= 'A' && c <= 'F') n = c - 'A' + 10;
			else break;
			v <<= 4;
			v += n;
			//next();
			cur++; c = ptr[cur];
			if( c == CR && ptr[cur+1] == LF ) { cur++; c = ptr[cur]; }
		}
		mCurrent = cur;
		if( sign ) {
			return Integer.valueOf((int)-v);
		} else {
			return Integer.valueOf((int)v);
		}
	}
	private Integer parseNonDecimalInteger8( boolean sign ) {
		long v = 0;
		final char[] ptr = mText;
		int cur = mCurrent;
		char c = ptr[cur];
		while( c != 0 ) {
			int n = -1;
			if(c >= '0' && c <= '7') n = c - '0';
			else break;
			v <<= 3;
			v += n;
			//next();
			cur++; c = ptr[cur];
			if( c == CR && ptr[cur+1] == LF ) { cur++; c = ptr[cur]; }
		}
		mCurrent = cur;
		if( sign ) {
			return Integer.valueOf((int)-v);
		} else {
			return Integer.valueOf((int)v);
		}
	}
	private Integer parseNonDecimalInteger2( boolean sign ) {
		long v = 0;
		final char[] ptr = mText;
		int cur = mCurrent;
		char c = ptr[cur];
		while( c != 0 ) {
			if( c == '1' ) {
				v <<= 1;
				v++;
			} else if( c == '0' ) {
				v <<= 1;
			} else {
				break;
			}
			//next();
			cur++; c = ptr[cur];
			if( c == CR && ptr[cur+1] == LF ) { cur++; c = ptr[cur]; }
		}
		mCurrent = cur;
		if( sign ) {
			return Integer.valueOf((int)-v);
		} else {
			return Integer.valueOf((int)v);
		}
	}
	private Number parseNonDecimalNumber( boolean sign, final int base ) {
		boolean is_real = parseExtractNumber( base );
		if( is_real ) {
			return parseNonDecimalReal( sign, base );
		} else {
			switch(base) {
			case 4: return parseNonDecimalInteger16(sign);
			case 3: return parseNonDecimalInteger8(sign);
			case 1: return parseNonDecimalInteger2(sign);
			}
		}
		return null;
	}
	// @return : Integer or Double or null
	private Number parseNumber() {
		int num = 0;
		boolean sign = false;
		boolean skipNum = false;

		final char[] ptr = mText;
		int cur = mCurrent;
		char c = ptr[cur];
		if( c == '+' ) {
			sign = false;
			//if( !next() ) return null;
			cur++; c = ptr[cur];
			//if( !skipSpace() ) return null;
			//c = mText[mCurrent];
			while( c != 0 && c <= SPACE && (c == CR || c == LF || c == TAB || c == SPACE) ) { cur++; c = ptr[cur]; }
			if( c == 0 ) {
				mCurrent = cur;
				return null;
			}
		} else if( c == '-' ) {
			sign = true;
			//if( !next() ) return null;
			cur++; c = ptr[cur];
			//if( !skipSpace() ) return null;
			//c = mText[cur];
			while( c != 0 && c <= SPACE && (c == CR || c == LF || c == TAB || c == SPACE) ) { cur++; c = ptr[cur]; }
			if( c == 0 ) {
				mCurrent = cur;
				return null;
			}
		}

		if( c > '9' ) { // 't', 'f', 'N', 'I' は '9' より大きい
			if( c == 't' && ptr[cur+1] == 'r' && ptr[cur+2] == 'u' && ptr[cur+3] == 'e' ) {
				cur += 4;
				mCurrent = cur;
				return Integer.valueOf(1);
			} else if( c == 'f' && ptr[cur+1] == 'a' && ptr[cur+2] == 'l' && ptr[cur+3] == 's' && ptr[cur+4] == 'e' ) {
				cur += 5;
				mCurrent = cur;
				return Integer.valueOf(0);
			} else if( c == 'N' && ptr[cur+1] == 'a' && ptr[cur+2] == 'N' ) {
				cur += 3;
				mCurrent = cur;
				return Double.valueOf(Double.NaN);
			} else if( c == 'I' && ptr[cur+1] == 'n' && ptr[cur+2] == 'f' && ptr[cur+3] == 'i' && ptr[cur+4] == 'n' &&
					ptr[cur+5] == 'i' && ptr[cur+6] == 't' && ptr[cur+7] == 'y' ) {
				cur += 8;
				mCurrent = cur;
				if( sign ) {
					return Double.valueOf(Double.NEGATIVE_INFINITY);
				} else {
					return Double.valueOf(Double.POSITIVE_INFINITY);
				}
			}
		}

		// 10進数以外か調べる
		if( c == '0' ) {
			//if( !next() ) {
			cur++; c = ptr[cur];
			if( c == CR && ptr[cur+1] == LF ) { cur++; c = ptr[cur]; }
			if( c == 0 ) {
				mCurrent = cur;
				return Integer.valueOf(0);
			}
			if( c == 'x' || c == 'X' ) {
				// hexadecimal
				//if( !next() ) return null;
				cur++; c = ptr[cur];
				if( c == CR && ptr[cur+1] == LF ) { cur++; c = ptr[cur]; }
				mCurrent = cur;
				if( c == 0 ) return null;
				return parseNonDecimalNumber(sign,4);
			} else if( c == 'b' || c == 'B' ) {
				// binary
				//if( !next() ) return null;
				cur++; c = ptr[cur];
				if( c == CR && ptr[cur+1] == LF ) { cur++; c = ptr[cur]; }
				mCurrent = cur;
				if( c == 0 ) return null;
				return parseNonDecimalNumber(sign,1);
			} else if( c == '.' ) {
				skipNum = true;
			} else if( c == 'e' || c == 'E' ) {
				skipNum = true;
			} else if( c == 'p' || c == 'P' ) {
				// 2^n exp
				mCurrent = cur;
				return null;
			} else if( c >= '0' && c <= '7' ) {
				// octal
				mCurrent = cur;
				return parseNonDecimalNumber(sign,3);
			}
		}

		if( skipNum == false ) {
			while( c != 0 ) {
				if( c < '0' || c > '9' ) break;
				num = num * 10 + ( c - '0' );
				//next();
				cur++; c = ptr[cur];
				if( c == CR && ptr[cur+1] == LF ) { cur++; c = ptr[cur]; }
			}
		}
		if( c == '.' || c == 'e' || c == 'E' ) {
			double figure = 1.0;
			int decimal = 0;
			if( c == '.' ) {
				do {
					//next();
					cur++; c = ptr[cur];
					if( c == CR && ptr[cur+1] == LF ) { cur++; c = ptr[cur]; }
					if( c < '0' || c > '9' ) break;
					decimal = decimal * 10 + ( c - '0' );
					figure *= 10;
				} while( c != 0 );
			}
			boolean expSign = false;
			int expValue = 0;
			if( c == 'e' || c == 'E' ) {
				//next();
				cur++; c = ptr[cur];
				if( c == CR && ptr[cur+1] == LF ) { cur++; c = ptr[cur]; }
				if( c == '-' ) {
					expSign = true;
					//next();
					cur++; c = ptr[cur];
					if( c == CR && ptr[cur+1] == LF ) { cur++; c = ptr[cur]; }
				}

				while( c != 0 ) {
					if( c < '0' || c > '9' ) break;
					expValue = expValue * 10 + ( c - '0' );
					//next();
					cur++; c = ptr[cur];
					if( c == CR && ptr[cur+1] == LF ) { cur++; c = ptr[cur]; }
				}
			}
			double number = (double)num + ( (double)decimal / figure );
			if( expValue != 0 ) {
				if( expSign == false ) {
					number *= Math.pow( 10, expValue );
				} else {
					number /= Math.pow( 10, expValue );
				}
			}
			if( sign ) number = -number;
			mCurrent = cur;
			return Double.valueOf( number );
		} else {
			if( sign ) num = -num;
			mCurrent = cur;
			return Integer.valueOf(num);
		}
	}
	private final String readString( int delimiter, boolean embexpmode ) throws CompileException {
		mStringStatus = NONE;
		int cur = mCurrent;
		char[] ptr = mText;

		StringBuilder str = mWorkBuilder;
		str.delete( 0, str.length() );

		while( ptr[cur] != 0 ) {
			char c = ptr[cur];
			if( c == '\\' ) {
				// escape
				// Next
				cur++; c = ptr[cur];
				if( c == CR && ptr[cur+1] == LF ) { cur++; c = ptr[cur]; }
				if( c == 0 ) break;

				if( c == 'x' || c == 'X' ) {	// hex
					// starts with a "\x", be parsed while characters are
					// recognized as hex-characters, but limited of size of char.
					// on Windows, \xXXXXX will be parsed to UNICODE 16bit characters.

					// Next
					cur++; c = ptr[cur];
					if( c == CR && ptr[cur+1] == LF ) { cur++; c = ptr[cur]; }
					if( c == 0 ) break;

					int code = 0;
					int count = 0;
					while( count < 4 ) {
						int n = -1;
						if(c >= '0' && c <= '9') n = c - '0';
						else if(c >= 'a' && c <= 'f') n = c - 'a' + 10;
						else if(c >= 'A' && c <= 'F') n = c - 'A' + 10;
						else break;

						code <<= 4; // *16
						code += n;
						count++;

						// Next
						cur++; c = ptr[cur];
						if( c == CR && ptr[cur+1] == LF ) { cur++; c = ptr[cur]; }
						if( c == 0 ) break;
					}
					if( c == 0 ) break;
					str.append( (char)code );
				} else if( c == '0' ) {	// octal
					// Next
					cur++; c = ptr[cur];
					if( c == CR && ptr[cur+1] == LF ) { cur++; c = ptr[cur]; }
					if( c == 0 ) break;

					int code = 0;
					while( true ) {
						int n = -1;
						if( c >= '0' && c <= '7' ) n = c - '0';
						else break;
						code <<= 3; // * 8
						code += n;

						// Next
						cur++; c = ptr[cur];
						if( c == CR && ptr[cur+1] == LF ) { cur++; c = ptr[cur]; }
						if( c == 0 ) break;
					}
					str.append( (char)code );
				} else {
					//str.append( (char)unescapeBackSlash(c) );
					switch(c) {
					case 'a': c = 0x07; break;
					case 'b': c = 0x08; break;
					case 'f': c = 0x0c; break;
					case 'n': c = 0x0a; break;
					case 'r': c = 0x0d; break;
					case 't': c = 0x09; break;
					case 'v': c = 0x0b; break;
					}
					str.append( (char)c );

					// Next
					cur++; c = ptr[cur];
					if( c == CR && ptr[cur+1] == LF ) { cur++; c = ptr[cur]; }
				}
			} else if( c == delimiter ) {
				// Next
				cur++; c = ptr[cur];
				if( c == CR && ptr[cur+1] == LF ) { cur++; c = ptr[cur]; }
				if( c == 0 ) {
					mStringStatus = DELIMITER;
					break;
				}

				int offset = cur;
				// skipSpace();
				while( c != 0 && c <= SPACE && (c == CR || c == LF || c == TAB || c == SPACE) ) { cur++; c = ptr[cur]; }
				if( c == delimiter ) {
					// sequence of 'A' 'B' will be combined as 'AB'
					// Next
					cur++; c = ptr[cur];
					if( c == CR && ptr[cur+1] == LF ) { cur++; c = ptr[cur]; }
				} else {
					cur = offset;
					mStringStatus = DELIMITER;
					break;
				}
			} else if( embexpmode == true && c == '&' ) {
				// Next
				cur++; c = ptr[cur];
				if( c == CR && ptr[cur+1] == LF ) { cur++; c = ptr[cur]; }
				if( c == 0 ) break;
				mStringStatus = AMPERSAND;
				break;
			} else if( embexpmode == true && c == '$' ) {
				// '{' must be placed immediately after '$'
				int offset = cur;
				// Next
				cur++; c = ptr[cur];
				if( c == CR && ptr[cur+1] == LF ) { cur++; c = ptr[cur]; }
				if( c == 0 ) break;

				if( c == '{' ) {
					// Next
					cur++; c = ptr[cur];
					if( c == CR && ptr[cur+1] == LF ) { cur++; c = ptr[cur]; }
					if( c == 0 ) break;
					mStringStatus = DOLLAR;
					break;
				} else {
					cur = offset;
					c = ptr[cur];
					str.append( (char)c );
					// Next
					cur++; c = ptr[cur];
					if( c == CR && ptr[cur+1] == LF ) { cur++; c = ptr[cur]; }
				}
			} else {
				str.append( (char)c );
				// Next
				cur++; c = ptr[cur];
				if( c == CR && ptr[cur+1] == LF ) { cur++; c = ptr[cur]; }
			}
		}
		mCurrent = cur;
		if( mStringStatus == NONE ) throw new CompileException(Error.StringParseError, mBlock, mCurrent);
		return str.toString();
	}

	public void setStartOfRegExp() { mRegularExpression = true; }
	public void setNextIsBareWord() { mBareWord = true; }

	private String parseRegExp() throws CompileException {
		boolean ok = false;
		boolean lastbackslash = false;
		StringBuilder str =  mWorkBuilder;
		str.delete( 0, str.length() );
		StringBuilder flag = null;
		final char[] ptr = mText;
		int cur = mCurrent;
		char c = ptr[cur];
		while( c != 0 ) {
			if( c == '\\' ) {
				str.append( (char)c );
				if( lastbackslash == true )
					lastbackslash = false;
				else
					lastbackslash = true;
			} else if( c == '/' && lastbackslash == false ) {
				cur++; c = ptr[cur];
				if( c == CR && ptr[cur+1] == LF ) { cur++; c = ptr[cur]; }
				if( c == 0 ) {
					ok = true;
					break;
				}
				//int flagMask = 0;
				if( flag == null ) {
					flag = new StringBuilder(BUFFER_CAPACITY);
				} else {
					flag.delete( 0, flag.length() );
				}
				while( c >= 'a' && c <= 'z' ) {
					flag.append( (char)c );
					cur++; c = ptr[cur];
					if( c == CR && ptr[cur+1] == LF ) { cur++; c = ptr[cur]; }
					if( c == 0 ) break;
				}
				str.insert( 0, "//" );
				String flgStr = flag.toString();
				str.insert( 2, flgStr );
				str.insert( 2+flgStr.length(), "/" );
				ok = true;
				break;
			} else {
				lastbackslash = false;
				str.append( (char)c );
			}
			cur++; c = ptr[cur];
			if( c == CR && ptr[cur+1] == LF ) { cur++; c = ptr[cur]; }
		}
		mCurrent = cur;
		if( !ok ) throw new CompileException(Error.StringParseError);
		return str.toString();
	}
	// 渡されたByteBufferを切り詰めた、新しいByteBufferを作る
	private ByteBuffer compactByteBuffer( ByteBuffer b ) {
		int count = b.position();
		ByteBuffer ret = ByteBuffer.allocate(count);
		b.position(0);
		for( int i = 0; i < count; i++ ) {
			ret.put( b.get() );
		}
		ret.position(0);
		return ret;
	}
	private ByteBuffer parseOctet() throws CompileException {
		// parse a octet literal;
		// syntax is:
		// <% xx xx xx xx xx xx ... %>
		// where xx is hexadecimal 8bit(octet) binary representation.
		final char[] ptr = mText;
		int cur = mCurrent + 1;
		//mCurrent++;
		char c = ptr[cur]; // skip %
		if( c == CR && ptr[cur+1] == LF ) { cur++; c = ptr[cur]; }

		boolean leading = true;
		byte oct = 0;

		// int count = mStream.countOctetTail() / 2 + 1;
		int count = 0;
		if( c != 0 ) {
			int offset = cur;
			//while( (ptr[offset] == '%' && ptr[offset+1] == '>') == false ) offset++;
			final int len = ptr.length;
			while( (offset+1) < len ) {
				if( ptr[offset] == '%' && ptr[offset+1] == '>' ) break;
				offset++;
			}
			count = offset - cur;
		}
		count = count / 2 + 1;

		ByteBuffer buffer = ByteBuffer.allocate(count);
		while( c != 0 ) {
			if( c == '/' ) {
				if( skipComment() == ENDED ) {
					mCurrent = cur;
					throw new CompileException( Error.StringParseError, mBlock, cur );
				}
			}

			c = ptr[cur];
			int n = cur+1;
			int next = ptr[n];
			if( next == CR && ptr[n+1] == LF ) { n++; c = ptr[n]; }
			if( c == '%' && next == '>' ) {
				cur = n;
				if( ptr[cur] != 0 ) {
					cur++; c = ptr[cur];
					if( c == CR && ptr[cur+1] == LF ) { cur++; c = ptr[cur]; }
				}

				if( !leading ) {
					buffer.put(oct);
				}
				mCurrent = cur;
				return compactByteBuffer(buffer);
			}
			//int num = getHexNum(c);
			int num;
			if( c >= '0' && c <= '9' ) num = c - '0';
			else if( c >= 'a' && c <= 'f' ) num = c - 'a' + 10;
			else if( c >= 'A' && c <= 'F' ) num = c - 'A' + 10;
			else num = -1;
			if( num != -1 ) {
				if( leading ) {
					oct = (byte)num;
					leading = false;
				} else {
					oct <<= 4;
					oct += num;
					buffer.put(oct);
					leading = true;
				}
			}
			if( leading == false && c == ',' ) {
				buffer.put(oct);
				leading = true;
			}
			cur = n;
		}
		mCurrent = cur;
		throw new CompileException( Error.StringParseError, mBlock, cur );
	}
	//private String parseString( int delimiter ) throws CompileException {
	//	return readString(delimiter,false);
	//}
	private int parsePPExpression( String script ) throws CompileException {
		PreprocessorExpressionParser parser = new PreprocessorExpressionParser( mBlock.getTJS(), script );
		return parser.parse();
	}
	private int processPPStatement() throws CompileException {
		// process pre-prosessor statements.
		// int offset = mCurrent;
		// mCurrent++; // skip '@'
		int cur = mCurrent + 1; // skip '@'
		final char[] ptr = mText;
		char c = ptr[cur];
		if( c == 's' && ptr[cur+1] == 'e' && ptr[cur+2] == 't' ) {
			// set statemenet
			mBlock.notifyUsingPreProcessror();
			cur+=3;
			c = ptr[cur];
			while( c != 0 && c <= SPACE && (c == CR || c == LF|| c == TAB || c == SPACE) ) { cur++; c = ptr[cur]; }
			if( c == 0 ) {
				mCurrent = cur;
				throw new CompileException( Error.PPError, mBlock, cur );
			}
			if( c != '(' ) {
				mCurrent = cur;
				throw new CompileException( Error.PPError, mBlock, cur );
			}

			cur++; c = ptr[cur]; // next '('
			if( c == CR && ptr[cur+1] == LF ) { cur++; c = ptr[cur]; }

			StringBuilder script = mWorkBuilder;
			script.delete( 0, script.length() );

			int plevel = 0;
			while( c != 0 && (plevel != 0 || c != ')') ) {
				if( c == '(' ) plevel++;
				else if( c == ')' ) plevel--;
				script.append( (char)c );
				cur++; c = ptr[cur];
				if( c == CR && ptr[cur+1] == LF ) { cur++; c = ptr[cur]; }
			}
			if( c != 0 ) {
				cur++; c = ptr[cur];
				if( c == CR && ptr[cur+1] == LF ) { cur++; c = ptr[cur]; }
			}

			parsePPExpression( script.toString() );
			//skipSpace();
			c = ptr[cur];
			while( c != 0 && c <= SPACE && (c == CR || c == LF|| c == TAB || c == SPACE) ) { cur++; c = ptr[cur]; }
			mCurrent = cur;
			if( c == 0 ) return ENDED;
			return CONTINUE;
		}
		if( c == 'i' && ptr[cur+1] == 'f' ) {
			// if statement
			mBlock.notifyUsingPreProcessror();
			cur+=2;
			//if( !skipSpace() ) throw new CompileException( Error.PPError, mBlock, mCurrent );
			c = ptr[cur];
			while( c != 0 && c <= SPACE && (c == CR || c == LF|| c == TAB || c == SPACE) ) { cur++; c = ptr[cur]; }
			if( c == 0 ) {
				mCurrent = cur;
				throw new CompileException( Error.PPError, mBlock, cur );
			}
			if( c != '(' ) {
				mCurrent = cur;
				throw new CompileException( Error.PPError, mBlock, cur );
			}

			cur++; c = ptr[cur]; // next '('
			if( c == CR && ptr[cur+1] == LF ) { cur++; c = ptr[cur]; }

			StringBuilder script = mWorkBuilder;
			script.delete( 0, script.length() );

			int plevel = 0;
			while( c != 0 && ( plevel != 0 || c != ')' ) ) {
				if( c == '(' ) plevel++;
				else if( c == ')' ) plevel--;
				script.append( (char)c );
				cur++; c = ptr[cur];
				if( c == CR && ptr[cur+1] == LF ) { cur++; c = ptr[cur]; }
			}
			if( c != 0 ) {
				cur++; c = ptr[cur];
				if( c == CR && ptr[cur+1] == LF ) { cur++; c = ptr[cur]; }
			}

			int ret = parsePPExpression( script.toString() );
			if( ret == 0 ) {
				mCurrent = cur;
				return skipUntilEndif();
			}
			mIfLevel++;
			//skipSpace();
			c = ptr[cur];
			while( c != 0 && c <= SPACE && (c == CR || c == LF|| c == TAB || c == SPACE) ) { cur++; c = ptr[cur]; }
			mCurrent = cur;
			if( c == 0 ) return ENDED;
			return CONTINUE;
		}

		if( c == 'e' && ptr[cur+1] == 'n' && ptr[cur+2] == 'd'&& ptr[cur+3] == 'i' && ptr[cur+4] == 'f' ) {
			// endif statement
			cur+=5;
			mIfLevel--;
			if( mIfLevel < 0 ) {
				mCurrent = cur;
				throw new CompileException( Error.PPError, mBlock, cur );
			}
			//skipSpace();
			c = ptr[cur];
			while( c != 0 && c <= SPACE && (c == CR || c == LF|| c == TAB || c == SPACE) ) { cur++; c = ptr[cur]; }
			mCurrent = cur;
			if( c == 0 ) return ENDED;
			return CONTINUE;
		}
		// mCurrent = offset;
		return NOT_COMMENT;
	}
	private int skipUntilEndif() throws CompileException {
		int exl = mIfLevel;
		mIfLevel++;
		final char[] ptr = mText;
		int cur = mCurrent;
		char c = ptr[cur];
		while( true ) {
			if( c == '/' ) {
				// comment
				mCurrent = cur;
				int ret = skipComment();
				cur = mCurrent;
				switch( ret ) {
				case ENDED:
					mCurrent = cur;
					throw new CompileException( Error.PPError, mBlock, cur );
				case CONTINUE:
					c = ptr[cur];
					break;
				case NOT_COMMENT:
					cur++; c = ptr[cur];
					if( c == CR && ptr[cur+1] == LF ) { cur++; c = ptr[cur]; }
					if( c == 0 ) {
						mCurrent = cur;
						throw new CompileException( Error.PPError, mBlock, cur );
					}
					break;
				}
			} else if( c == '@' ) {
				cur++;
				c = ptr[cur];
				boolean skipp = false;
				if( c == 'i' && ptr[cur+1] == 'f' ) {
					mIfLevel++;
					cur += 2;
					c = ptr[cur];
					skipp = true;
				} else if( c == 's' && ptr[cur+1] == 'e' && ptr[cur+2] == 't' ) {
					cur += 3;
					c = ptr[cur];
					skipp = true;
				} else if( c == 'e' && ptr[cur+1] == 'n' && ptr[cur+2] == 'd' && ptr[cur+3] == 'i' && ptr[cur+4] == 'f' ) {
					cur += 5;
					c = ptr[cur];
					mIfLevel--;
					if( mIfLevel == exl ) { // skip ended
						//skipSpace();
						while( c != 0 && c <= SPACE && (c == CR || c == LF|| c == TAB || c == SPACE) ) { cur++; c = ptr[cur]; }
						mCurrent = cur;
						if( c == 0 ) return ENDED;
						return CONTINUE;
					}
				} //else {}

				if( skipp ) {
					while( c != 0 && c <= SPACE && (c == CR || c == LF|| c == TAB || c == SPACE) ) { cur++; c = ptr[cur]; }
					if( c == 0 ) {
						mCurrent = cur;
						throw new CompileException( Error.PPError, mBlock, cur );
					}
					if( c != '(' ) {
						mCurrent = cur;
						throw new CompileException( Error.PPError, mBlock, cur );
					}
					cur++; c = ptr[cur];
					if( c == CR && ptr[cur+1] == LF ) { cur++; c = ptr[cur]; }
					int plevel = 0;
					while( c != 0 && ( plevel > 0 || c != ')' ) ) {
						if( c == '(' ) plevel++;
						else if( c == ')' ) plevel--;
						cur++; c = ptr[cur];
						if( c == CR && ptr[cur+1] == LF ) { cur++; c = ptr[cur]; }
					}
					if( c == 0 ) {
						mCurrent = cur;
						throw new CompileException( Error.PPError, mBlock, cur );
					}
					cur++; c = ptr[cur];
					if( c == CR && ptr[cur+1] == LF ) { cur++; c = ptr[cur]; }
					if( c == 0 ) {
						mCurrent = cur;
						throw new CompileException( Error.PPError, mBlock, cur );
					}
				}
			} else {
				cur++; c = ptr[cur];
				if( c == CR && ptr[cur+1] == LF ) { cur++; c = ptr[cur]; }
				if( c == 0 ) {
					mCurrent = cur;
					throw new CompileException( Error.PPError, mBlock, cur );
				}
			}
		}
	}

	private static final String escapeC( char c ) {
		switch( c ) {
		case 0x07: return( "\\a" );
		case 0x08: return( "\\b" );
		case 0x0c: return( "\\f" );
		case 0x0a: return( "\\n" );
		case 0x0d: return( "\\r" );
		case 0x09: return( "\\t" );
		case 0x0b: return( "\\v" );
		case '\\': return( "\\\\" );
		case '\'': return( "\\\'" );
		case '\"': return( "\\\"" );
		default:
			if( c < 0x20 ) {
				StringBuilder ret = mWorkBuilder;
				ret.delete( 0, ret.length() );
				ret.append("\\x");
				ret.append( Integer.toHexString((int)c) );
				return ret.toString();
			} else {
				return String.valueOf(c);
			}
		}
	}

	private int putValue( Object val ) {
		mValues.add( val );
		return mValues.size() - 1;
	}
	private int getToken() throws CompileException {
		final char[] ptr = mText;
		char c = ptr[mCurrent];
		if( c == 0 ) return 0;
		if( mRegularExpression == true ) {
			mRegularExpression = false;
			mCurrent = mPrevPos;
			//next(); // 最初の'/'を読み飛ばし
			mCurrent++;
			if( mText[mCurrent] == CR && mText[mCurrent+1] == LF ) mCurrent++;
			String pattern = parseRegExp();
			mValue = putValue(pattern);
			return Token.T_REGEXP;
		}

		boolean retry;
		do {
			retry = false;
			mPrevPos = mCurrent;
			c = ptr[mCurrent];
			switch(c) {
			case 0:
				return 0;
			case '>':
				mCurrent++; c = ptr[mCurrent];
				if( c == '>' ) {	// >>
					mCurrent++; c = ptr[mCurrent];
					if( c == '>' ) {	// >>>
						mCurrent++; c = ptr[mCurrent];
						if( c == '=' ) {	// >>>=
							mCurrent++;
							return Token.T_RBITSHIFTEQUAL;
						} else {
							return Token.T_RBITSHIFT;
						}
					} else if( c == '=' ) {	// >>=
						mCurrent++;
						return Token.T_RARITHSHIFTEQUAL;
					} else {	// >>
						return Token.T_RARITHSHIFT;
					}
				} else if( c == '=' ) {	// >=
					mCurrent++;
					return Token.T_GTOREQUAL;
				} else {
					return Token.T_GT;
				}

			case '<':
				mCurrent++; c = ptr[mCurrent];
				if( c == '<' ) {	// <<
					mCurrent++; c = ptr[mCurrent];
					if( c == '=' ) {	// <<=
						mCurrent++;
						return Token.T_LARITHSHIFTEQUAL;
					} else {	// <<
						return Token.T_LARITHSHIFT;
					}
				} else if( c == '-' ) {	// <-
					mCurrent++; c = ptr[mCurrent];
					if( c == '>' ) {	// <->
						mCurrent++;
						return Token.T_SWAP;
					} else {	// <
						mCurrent--;
						return Token.T_LT;
					}
				} else if( c == '=' ) {	// <=
					mCurrent++;
					return Token.T_LTOREQUAL;
				} else if( c == '%' ) {	// '<%'   octet literal
					ByteBuffer buffer = parseOctet();
					mValue = putValue(buffer);
					return Token.T_CONSTVAL;
				} else {	// <
					return Token.T_LT;
				}

			case '=':
				mCurrent++; c = ptr[mCurrent];
				if( c == '=' ) { // ===
					mCurrent++; c = ptr[mCurrent];
					if( c == '=' ) {
						mCurrent++;
						return Token.T_DISCEQUAL;
					} else { // ==
						return Token.T_EQUALEQUAL;
					}
				} else if( c == '>' ) {	// =>
					mCurrent++;
					return Token.T_COMMA;
				} else {	// =
					return Token.T_EQUAL;
				}

			case '!':
				mCurrent++; c = ptr[mCurrent];
				if( c == '=' ) {
					mCurrent++; c = ptr[mCurrent];
					if( c == '=' ) { // !==
						mCurrent++;
						return Token.T_DISCNOTEQUAL;
					} else { // !=
						return Token.T_NOTEQUAL;
					}
				} else {	// !
					return Token.T_EXCRAMATION;
				}

			case '&':
				mCurrent++; c = ptr[mCurrent];
				if( c == '&' ) {
					mCurrent++; c = ptr[mCurrent];
					if( c == '=' ) { // &&=
						mCurrent++;
						return Token.T_LOGICALANDEQUAL;
					} else { // &&
						return Token.T_LOGICALAND;
					}
				} else if( c == '=' ) { // &=
					mCurrent++;
					return Token.T_AMPERSANDEQUAL;
				} else {	// &
					return Token.T_AMPERSAND;
				}

			case '|':
				mCurrent++; c = ptr[mCurrent];
				if( c == '|' ) {
					mCurrent++; c = ptr[mCurrent];
					if( c == '=' ) { // ||=
						mCurrent++;
						return Token.T_LOGICALOREQUAL;
					} else { // ||
						return Token.T_LOGICALOR;
					}
				} else if( c == '=' ) { // |=
					mCurrent++;
					return Token.T_VERTLINEEQUAL;
				} else { // |
					return Token.T_VERTLINE;
				}

			case '.':
				mCurrent++; c = ptr[mCurrent];
				if( c >= '0' && c <= '9' ) { // number
					mCurrent--;
					//mCurrent--;
					Number o = parseNumber();
					if( o != null ) {
						if( o instanceof Integer )
							mValue = putValue((Integer)o);
						else if( o instanceof Double )
							mValue = putValue((Double)o);
						else
							mValue = putValue(null);
					} else {
						mValue = putValue(null);
					}
					return Token.T_CONSTVAL;
				} else if( c == '.' ) {
					mCurrent++; c = ptr[mCurrent];
					if( c == '.' ) { // ...
						mCurrent++;
						return Token.T_OMIT;
					} else { // .
						mCurrent--;
						//mCurrent--;
						return Token.T_DOT;
					}
				} else { // .
					return Token.T_DOT;
				}

			case '+':
				mCurrent++; c = ptr[mCurrent];
				if( c == '+' ) { // ++
					mCurrent++;
					return Token.T_INCREMENT;
				} else if( c == '=' ) { // +=
					mCurrent++;
					return Token.T_PLUSEQUAL;
				} else { // +
					return Token.T_PLUS;
				}

			case '-':
				mCurrent++; c = ptr[mCurrent];
				if( c == '-' ) { // --
					mCurrent++;
					return Token.T_DECREMENT;
				} else if( c == '=' ) {
					mCurrent++;
					return Token.T_MINUSEQUAL; // -=
				} else { // -
					return Token.T_MINUS;
				}

			case '*':
				mCurrent++; c = ptr[mCurrent];
				if( c == '=' ) { // *=
					mCurrent++;
					return Token.T_ASTERISKEQUAL;
				} else { // *
					return Token.T_ASTERISK;
				}

			case '/':
				mCurrent++; c = ptr[mCurrent];
				if( c == '/' || c == '*' ) {
					mCurrent--;
					int comment = skipComment();
					if( comment == CONTINUE ) {
						retry = true;
						break;
					} else if( comment == ENDED ) {
						return 0;
					}
				}
				if( c == '=' ) {	// /=
					mCurrent++;
					return Token.T_SLASHEQUAL;
				} else {	// /
					return Token.T_SLASH;
				}

			case '\\':
				mCurrent++; c = ptr[mCurrent];
				if( c == '=' ) {	// \=
					mCurrent++;
					return Token.T_BACKSLASHEQUAL;
				} else {	// \
					return Token.T_BACKSLASH;
				}

			case '%':
				mCurrent++; c = ptr[mCurrent];
				if( c == '=' ) { // %=
					mCurrent++;
					return Token.T_PERCENTEQUAL;
				} else { // %
					return Token.T_PERCENT;
				}

			case '^':
				mCurrent++; c = ptr[mCurrent];
				if( c == '=' ) { // ^=
					mCurrent++;
					return Token.T_CHEVRONEQUAL;
				} else { // ^
					return Token.T_CHEVRON;
				}

			case '[':
				mNestLevel++;
				mCurrent++;
				return Token.T_LBRACKET;

			case ']':
				mNestLevel--;
				mCurrent++;
				return Token.T_RBRACKET;

			case '(':
				mNestLevel++;
				mCurrent++;
				return Token.T_LPARENTHESIS;

			case ')':
				mNestLevel--;
				mCurrent++;
				return Token.T_RPARENTHESIS;

			case '~':
				mCurrent++;
				return Token.T_TILDE;

			case '?':
				mCurrent++;
				return Token.T_QUESTION;

			case ':':
				mCurrent++;
				return Token.T_COLON;

			case ',':
				mCurrent++;
				return Token.T_COMMA;

			case ';':
				mCurrent++;
				return Token.T_SEMICOLON;

			case '{':
				mNestLevel++;
				mCurrent++;
				return Token.T_LBRACE;

			case '}':
				mNestLevel--;
				mCurrent++;
				return Token.T_RBRACE;

			case '#':
				mCurrent++;
				return Token.T_SHARP;

			case '$':
				mCurrent++;
				return Token.T_DOLLAR;

			case '\'':
			case '\"': {
				// literal string
				//String str = parseString(c);
				//next();
				mCurrent++;
				if( mText[mCurrent] == CR && mText[mCurrent+1] == LF ) mCurrent++;
				String str = readString(c,false);
				mValue = putValue(str);
				return Token.T_CONSTVAL;
			}

			case '@': {
				// embeddable expression in string (such as @"this can be embeddable like &variable;")
				int org = mCurrent;
				//if( !next() ) return 0;
				mCurrent++; c = ptr[mCurrent];
				if( c == CR && ptr[mCurrent+1] == LF ) { mCurrent++; c = ptr[mCurrent]; }
				//if( !skipSpace() ) return 0;
				while( c != 0 && c <= SPACE && (c == CR || c == LF || c == TAB || c == SPACE) ) { mCurrent++; c = ptr[mCurrent]; }
				if( c == 0 ) return 0;
				//c = ptr[mCurrent];
				if( c == '\'' || c == '\"' ) {
					EmbeddableExpressionData data = new EmbeddableExpressionData();
					data.mState = EmbeddableExpressionData.START;
					data.mWaitingNestLevel = mNestLevel;
					data.mDelimiter = c;
					data.mNeedPlus = false;
					//if( !next() ) return 0;
					mCurrent++; c = ptr[mCurrent];
					if( c == CR && ptr[mCurrent+1] == LF ) { mCurrent++; c = ptr[mCurrent]; }
					if( c == 0 ) return 0;
					mEmbeddableExpressionDataStack.add( data );
					return -1;
				} else {
					mCurrent = org;
				}
				// possible pre-procesor statements
				switch( processPPStatement() ) {
				case CONTINUE:
					retry = true;
					break;
				case ENDED:
					return 0;
				case NOT_COMMENT:
					mCurrent = org;
				}
				break;
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
			case '9': {
				Number o = parseNumber();
				if( o != null ) {
					if( o instanceof Integer )
						mValue = putValue((Integer)o);
					else if( o instanceof Double )
						mValue = putValue((Double)o);
					else
						throw new CompileException( Error.NumberError, mBlock, mCurrent );
				} else {
					throw new CompileException( Error.NumberError, mBlock, mCurrent );
				}
				return Token.T_CONSTVAL;
			}
			}	// switch(c)
		} while( retry );

		if( (((c & 0xFF00) != 0 ) || (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')) == false && c != '_' ) {
		//if( isWAlpha(c) == false && c != '_' ) {
			String str = Error.InvalidChar.replace( "%1", escapeC((char)c) );
			throw new CompileException( str, mBlock, mCurrent );
		}
		int oldC = c;
		int offset = mCurrent;
		int nch = 0;
		while( (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')|| c == '_' || ( c >= '0' && c <= '9' ) || ((c & 0xFF00) != 0 ) ) {
		//while( isWDigit(c) || isWAlpha(c) || c == '_' || c > 0x0100 || /*c == CARRIAGE_RETURN || */c == LINE_FEED ) {
			mCurrent++; c = ptr[mCurrent];
			nch++;
		}
		if( nch == 0 ) {
			String str = Error.InvalidChar.replace( "%1", escapeC((char)oldC) );
			throw new CompileException( str, mBlock, mCurrent );
		}

		String str = new String( ptr, offset, nch );
		/*
		int strLen = str.length();
		StringBuilder symStr = mWorkBuilder;
		symStr.delete( 0, symStr.length() );
		for( int i = 0; i < strLen; i++ ) {
			char t = str.charAt(i);
			if( /*t != CARRIAGE_RETURN &&*//* t != LINE_FEED ) symStr.append(t);
		}
		if( str.length() != symStr.length() ) {
			str = symStr.toString();
		}
		*/

		int retnum;
		if( mBareWord ) {
			retnum = -1;
			mBareWord = false;
		} else {
			retnum = ReservedWordToken.getToken( str );
		}

		if( retnum == -1 ) { // not a reserved word
			mValue = putValue(str);
			return Token.T_SYMBOL;
		}
		switch( retnum ) {
		case Token.T_FALSE:
			mValue = putValue( Integer.valueOf(0) );
			return Token.T_CONSTVAL;
		case Token.T_NULL:
			mValue = putValue( new VariantClosure(null) );
			return Token.T_CONSTVAL;
		case Token.T_TRUE:
			mValue = putValue( Integer.valueOf(1) );
			return Token.T_CONSTVAL;
		case Token.T_NAN:
			mValue = putValue( Double.valueOf( Double.NaN ) );
			return Token.T_CONSTVAL;
		case Token.T_INFINITY:
			mValue = putValue( Double.valueOf( Double.POSITIVE_INFINITY ) );
			return Token.T_CONSTVAL;
		}
		return retnum;
	}
	public final int getValue() { return mValue; }

	public final int getNext() throws CompileException {
		if( mIsFirst ) {
			mIsFirst = false;
			if( mIsExprMode && mResultNeeded ) {
				mValue = 0;
				return Token.T_RETURN;
			}
		}
		int n = 0;
		mValue = 0;
		do {
			if( mRetValDeque.isEmpty() != true  ) {
				long pair = mRetValDeque.pop_front();
				mValue = (int) (pair >>> 32);
				mPrevToken = (int) (pair & 0xffffffffL);
				return mPrevToken;
			}
			try {
				if( mEmbeddableExpressionDataStack.size() == 0 ) {
					//skipSpace();
					char c  = mText[mCurrent];
					while( c != 0 && c <= SPACE && (c == CR || c == LF|| c == TAB || c == SPACE) ) { mCurrent++; c = mText[mCurrent]; }
					n = getToken();

					if( CompileState.mEnableDicFuncQuickHack ) { // dicfunc quick-hack
						if( mDicFunc ) {
							if( n == Token.T_PERCENT ) {
								// push "function { return %"
								mRetValDeque.push_back( Token.T_FUNCTION ); // value index は 0 なので無視
								mRetValDeque.push_back( Token.T_LBRACE );
								mRetValDeque.push_back( Token.T_RETURN );
								mRetValDeque.push_back( Token.T_PERCENT );
								n = -1;
							} else if ( n == Token.T_LBRACKET && mPrevToken != Token.T_PERCENT ) {
								// push "function { return ["
								mRetValDeque.push_back( Token.T_FUNCTION ); // value index は 0 なので無視
								mRetValDeque.push_back( Token.T_LBRACE );
								mRetValDeque.push_back( Token.T_RETURN );
								mRetValDeque.push_back( Token.T_LBRACKET );
								n = -1;
							} else if( n == Token.T_RBRACKET ) {
								// push "] ; } ( )"
								mRetValDeque.push_back( Token.T_RBRACKET ); // value index は 0 なので無視
								mRetValDeque.push_back( Token.T_SEMICOLON );
								mRetValDeque.push_back( Token.T_RBRACE );
								mRetValDeque.push_back( Token.T_LPARENTHESIS );
								mRetValDeque.push_back( Token.T_RPARENTHESIS );
								n = -1;
							}
						}
					}
				} else {
					// embeddable expression mode
					EmbeddableExpressionData data = mEmbeddableExpressionDataStack.get( mEmbeddableExpressionDataStack.size() -1 );
					switch( data.mState ) {
					case EmbeddableExpressionData.START:
						mRetValDeque.push_back( Token.T_LPARENTHESIS ); // value index は 0 なので無視
						n = -1;
						data.mState = EmbeddableExpressionData.NEXT_IS_STRING_LITERAL;
						break;

					case EmbeddableExpressionData.NEXT_IS_STRING_LITERAL: {
						String str = readString( data.mDelimiter, true );
						int res = mStringStatus;
						if( mStringStatus == DELIMITER ) {
							// embeddable expression mode ended
							if( str.length() > 0 ) {
								if( data.mNeedPlus ) {
									mRetValDeque.push_back( Token.T_PLUS ); // value index は 0 なので無視
								}
							}
							if( str.length() > 0 || data.mNeedPlus == false ) {
								int v = putValue(str);
								mRetValDeque.push_back( Token.T_CONSTVAL | (v<<32) );
							}
							mRetValDeque.push_back( Token.T_RPARENTHESIS ); // value index は 0 なので無視
							mEmbeddableExpressionDataStack.remove( mEmbeddableExpressionDataStack.size() - 1 );
							n = -1;
							break;
						} else {
							// c is next to ampersand mark or '${'
							if( str.length() > 0 ) {
								if( data.mNeedPlus ) {
									mRetValDeque.push_back( Token.T_PLUS ); // value index は 0 なので無視
								}
								int v = putValue(str);
								mRetValDeque.push_back( Token.T_CONSTVAL | (v<<32) );
								data.mNeedPlus = true;
							}
							if( data.mNeedPlus == true ) {
								mRetValDeque.push_back( Token.T_PLUS ); // value index は 0 なので無視
							}
							mRetValDeque.push_back( Token.T_STRING ); // value index は 0 なので無視
							mRetValDeque.push_back( Token.T_LPARENTHESIS );
							data.mState = EmbeddableExpressionData.NEXT_IS_EXPRESSION;
							if( res == AMPERSAND ) {
								data.mWaitingToken = Token.T_SEMICOLON;
							} else if( res == DOLLAR ) {
								data.mWaitingToken = Token.T_RBRACE;
								mNestLevel++;
							}
							n = -1;
							break;
						}
					}
					case EmbeddableExpressionData.NEXT_IS_EXPRESSION:
						//skipSpace();
						char c  = mText[mCurrent];
						while( c != 0 && c <= SPACE && (c == CR || c == LF|| c == TAB || c == SPACE) ) { mCurrent++; c = mText[mCurrent]; }
						n = getToken();
						if( n == data.mWaitingToken && mNestLevel == data.mWaitingNestLevel ) {
							// end of embeddable expression mode
							mRetValDeque.push_back( Token.T_RPARENTHESIS ); // value index は 0 なので無視
							data.mNeedPlus = true;
							data.mState = EmbeddableExpressionData.NEXT_IS_STRING_LITERAL;
							n = -1;
						}
						break;
					}
				}
				if( n == 0 ) {
					if( mIfLevel != 0 ) {
						throw new CompileException( Error.PPError, mBlock, mCurrent );
					}
				}
			} catch( CompileException e ) {
				e.printStackTrace();
				mBlock.error(e.getMessage());
				return 0;
			}
		} while ( n < 0 );
		mPrevToken = n;
		return n;
	}
	public int getCurrentPosition() { return mCurrent; }
}
