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

public class StringStream {
	//private static final String TAG = "StringStream";
	//private static final boolean LOGD = false;

	private static final int BUFFER_CAPACITY = 1024;

	private final String mString;
	private final char[] mText;
	private int mOffset;
	private boolean mEOF;
	private int mStringStatus;
	private IntVector mLineVector;
	private IntVector mLineLengthVector;
	private int mLineOffset;

	static private final int CARRIAGE_RETURN = 13;
	static private final int LINE_FEED = 10;
	static private final int TAB = 0x09;
	static private final int SPACE = 0x20;

	static public final int NOT_COMMENT = 0;
	static public final int UNCLOSED_COMMENT = -1;
	static public final int CONTINUE = 1;
	static public final int ENDED = 2;

	static public final int NONE = 0;
	static public final int DELIMITER = 1;
	static public final int AMPERSAND = 2;
	static public final int DOLLAR = 3;

	public StringStream( final String str ) {
		mString = str;
		mText = new char[mString.length()];
		str.getChars( 0, mString.length(), mText, 0 );
		//mOffset = 0;
		//mEOF = false;
	}
	public final void ungetC() {
		if( mOffset > 0 ) mOffset--;
	}
	public final int getC() {
		int retval = -1;
		if( mOffset < mText.length ) {
			retval = mText[mOffset];
			mOffset++;
		} else {
			mEOF = true;
		}
		return retval;
	}
	public final void incOffset() {
		if( mOffset < mText.length ) {
			mOffset++;
		} else {
			mEOF = true;
		}
	}
	public final int peekC() {
		int retval = -1;
		if( mOffset < mText.length ) {
			retval = mText[mOffset];
		}
		return retval;
	}
	public final int peekC(int offset) {
		int retval = -1;
		if( (mOffset+offset) < mText.length ) {
			retval = mText[mOffset+offset];
		}
		return retval;
	}
	// 改行コードを無視して取得する
	public final int next() {
		int retval = -1;
		if( mOffset < mText.length ) {
			retval = mText[mOffset];
			mOffset++;
			while( retval == CARRIAGE_RETURN || retval == LINE_FEED ) {
				if( mOffset < mText.length ) {
					retval = mText[mOffset];
					mOffset++;
				} else {
					retval = -1;
					mEOF = true;
					break;
				}
			}
			return retval;
		} else {
			mEOF = true;
		}
		return retval;
	}
	public final boolean isEOF() { return mEOF; }

	public final void skipSpace() {
		if( mOffset < mText.length ) {
			int c = mText[mOffset];
			mOffset++;
			boolean skipToLast = false;
			while( c == CARRIAGE_RETURN || c == LINE_FEED || c == TAB || c == SPACE ) {
				if( mOffset < mText.length ) {
					c = mText[mOffset];
					mOffset++;
				} else {
					skipToLast = true;
					break;
				}
			}
			if( mOffset > 0 && skipToLast == false ) mOffset--;
		}
		if( mOffset >= mText.length ) mEOF = true;
	}
	public final void skipReturn() {
		if( mOffset < mText.length ) {
			int c = mText[mOffset];
			mOffset++;
			boolean skipToLast = false;
			while( c == CARRIAGE_RETURN || c == LINE_FEED ) {
				if( mOffset < mText.length ) {
					c = mText[mOffset];
					mOffset++;
				} else {
					skipToLast = true;
					break;
				}
			}
			if( mOffset > 0 && skipToLast == false ) mOffset--;
		}
		if( mOffset >= mText.length ) mEOF = true;
	}
	public final int skipComment() throws CompileException {
		int offset = mOffset;
		if( (offset+1) < mText.length ) {
			if( mText[offset] != '/' ) return NOT_COMMENT;
			if( mText[offset+1] == '/' ) {
				// ラインコメント
				mOffset += 2;
				int c = mText[mOffset];
				mOffset++;
				while( c != CARRIAGE_RETURN && c != LINE_FEED ) {
					if( mOffset < mText.length ) {
						c = mText[mOffset];
						mOffset++;
					} else {
						break;
					}
				}
				if( mOffset < mText.length ) {
					if( c == CARRIAGE_RETURN ) {
						if( mText[mOffset] == LINE_FEED ) {
							mOffset++;
						}
					}
				} else {
					mEOF = true;
					return ENDED;
				}
				skipSpace();
				if( mOffset >= mText.length ) {
					mEOF = true;
					return ENDED;
				}
				return CONTINUE;
			} else if( mText[offset+1] == '*' ) {
				// ブロックコメント
				mOffset += 2;
				int level = 0;
				while(true) {
					if( (mOffset+1) < mText.length ) {
						if( mText[mOffset] == '/' && mText[mOffset+1] == '*' ) {
							// コメントのネスト
							level++;
						} else if( mText[mOffset] == '*' && mText[mOffset+1] == '/' ) {
							if( level == 0 ) {
								mOffset += 2;
								break;
							}
							level--;
						}
						mOffset++;
					} else {
						throw new CompileException(Error.UnclosedComment);
					}
				}
				if( mOffset >= mText.length ) {
					mEOF = true;
					return ENDED;
				}
				skipSpace();
				if( mOffset >= mText.length ) {
					mEOF = true;
					return ENDED;
				}
				return CONTINUE;
			}
		}
		return NOT_COMMENT;
	}
	public final int getOffset() { return mOffset; }
	public final void setOffset( int offset ) {
		if( offset < mText.length ) {
			mOffset = offset;
		} else {
			mOffset = mText.length;
			mEOF = true;
		}
	}
	public final boolean equalString( final String value ) {
		final int count = value.length();
		if( (mText.length - mOffset) >= count ) {
			final int offset = mOffset;
			for( int i = 0; i < count; i++ ) {
				if( mText[offset+i] != value.charAt(i) ) {
					return false;
				}
			}
			mOffset += count;
			return true;
		} else {
			return false;
		}
	}
	static final public int unescapeBackSlash( int ch ) {
		switch(ch) {
		case 'a': return 0x07;
		case 'b': return 0x08;
		case 'f': return 0x0c;
		case 'n': return 0x0a;
		case 'r': return 0x0d;
		case 't': return 0x09;
		case 'v': return 0x0b;
		default: return ch;
		}
	}
	public final int countOctetTail() {
		if( mOffset < mText.length ) {
			int offset = mOffset;
			while( (offset+1) < mText.length ) {
				if( mText[offset] == '%' && mText[offset+1] == '>' )
					break;
				offset++;
			}
			return offset - mOffset;
		} else {
			return 0;
		}
	}

	public final String readString( int delimiter, boolean embexpmode ) throws CompileException {
		if( mOffset < mText.length ) {
			StringBuilder str = new StringBuilder(BUFFER_CAPACITY);
			mStringStatus = NONE;
			try {
				while( mOffset < mText.length ) {
					int c = mText[mOffset];
					mOffset++;
					while( c == CARRIAGE_RETURN || c == LINE_FEED ) { c = mText[mOffset]; mOffset++; }
					if( c == '\\' ) {
						// escape
						c = mText[mOffset];
						mOffset++;
						while( c == CARRIAGE_RETURN || c == LINE_FEED ) { c = mText[mOffset]; mOffset++; }
						if( c == 'x' || c == 'X' ) {	// hex
//							int num = 0;
							int code = 0;
							int count = 0;
							while( count < 4 ) {
								c = mText[mOffset];
								mOffset++;
								while( c == CARRIAGE_RETURN || c == LINE_FEED ) { c = mText[mOffset]; mOffset++; }

								int n = -1;
								if(c >= '0' && c <= '9') n = c - '0';
								else if(c >= 'a' && c <= 'f') n = c - 'a' + 10;
								else if(c >= 'A' && c <= 'F') n = c - 'A' + 10;
								if( n == -1 ) {
									mOffset--;
									break;
								}
								code <<= 4; // *16
								code += n;
								count++;
							}
							str.append( (char)code );
						} else if( c == '0' ) {	// octal
//							int num;
							int code = 0;
							while( true ) {
								c = mText[mOffset];
								mOffset++;
								while( c == CARRIAGE_RETURN || c == LINE_FEED ) { c = mText[mOffset]; mOffset++; }

								int n = -1;
								if( c >= '0' && c <= '7' ) n = c - '0';
								if( n == -1 ) {
									mOffset--;
									break;
								}
								code <<= 3; // * 8
								code += n;
							}
							str.append( (char)code );
						} else {
							str.append( (char)unescapeBackSlash(c) );
						}
					} else if( c == delimiter ) {
						if( mOffset >= mText.length ) {
							mStringStatus = DELIMITER;
							break;
						}
						int offset = mOffset;
						skipSpace();
						c = mText[mOffset];
						mOffset++;
						while( c == CARRIAGE_RETURN || c == LINE_FEED ) { c = mText[mOffset]; mOffset++; }
						if( c == delimiter ) {
							// sequence of 'A' 'B' will be combined as 'AB'
						} else {
							mStringStatus = DELIMITER;
							mOffset = offset;
							break;
						}
					} else if( embexpmode == true && c == '&' ) {
						if( mOffset >= mText.length ) break;
						mStringStatus = AMPERSAND;
						break;
					} else if( embexpmode == true && c == '$' ) {
						// '{' must be placed immediately after '$'
						int offset = mOffset;
						c = mText[mOffset];
						mOffset++;
						while( c == CARRIAGE_RETURN || c == LINE_FEED ) { c = mText[mOffset]; mOffset++; }
						if( mOffset >= mText.length ) break;
						if( c == '{' ) {
							if( mOffset >= mText.length ) break;
							mStringStatus = DOLLAR;
							break;
						} else {
							mOffset = offset;
							str.append( (char)c );
						}
					} else {
						str.append( (char)c );
					}
				}
			} catch(ArrayIndexOutOfBoundsException e) {
				mEOF = true;
				if( mStringStatus == NONE ) {
					throw new CompileException(Error.StringParseError);
				}
			}
			if( mStringStatus == NONE ) {
				throw new CompileException(Error.StringParseError);
			}
			return str.toString();
		}
		return null;
	}
	public final int getStringStatus() { return mStringStatus; }
	public final String substring( int beginIndex, int endIndex) {
		return mString.substring( beginIndex, endIndex );
	}
	private final void generateLineVector() {
		mLineVector = new IntVector();
		mLineLengthVector = new IntVector();
		int count = mText.length;
		int lastCR = 0;
		int i;
		for( i= 0; i < count; i++ ) {
			int c = mText[i];
			if( c == CARRIAGE_RETURN || c == LINE_FEED ) {
				mLineVector.add( lastCR );
				mLineLengthVector.add( i-lastCR );
				lastCR = i+1;
				if( (i+1) < count ) {
					c = mText[i+1];
					if( c == CARRIAGE_RETURN || c == LINE_FEED ) {
						i++;
						lastCR = i+1;
					}
				}
			}
		}
		if( i != lastCR ) {
			mLineVector.add( lastCR );
			mLineLengthVector.add( i-lastCR );
		}
	}
	public final int getSrcPosToLine( int pos ) {
		if( mLineVector == null ) {
			generateLineVector();
		}
		// 2分法によって位置を求める
		int s = 0;
		int e = mLineVector.size();
		while( true ) {
			if( (e-s) <= 1 ) return s + mLineOffset;
			int m = s + (e-s)/2;
			if( mLineVector.get(m) > pos )
				e = m;
			else
				s = m;
		}
	}
	public final int getLineToSrcPos( int pos ) {
		if( mLineVector == null ) {
			generateLineVector();
		}
		return mLineVector.get(pos);
	}
	public final String getLine( int line ) {
		if( mLineVector == null ) {
			generateLineVector();
		}
		int start = mLineVector.get(line);
		int length = mLineLengthVector.get(line);
		return mString.substring(start, start+length );
	}
	public final int getMaxLine() {
		if( mLineVector == null ) {
			generateLineVector();
		}
		return mLineVector.size();
	}
}
