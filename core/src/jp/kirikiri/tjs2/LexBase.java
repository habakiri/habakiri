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

public class LexBase {
	//private static final String TAG = "TokenStraem";
	//private static final boolean LOGD = false;

	protected StringStream mStream;

	public LexBase( String str ) {
		mStream = new StringStream(str);
	}
	public LexBase() {
		//mStream = null;
	}

	private boolean parseExtractNumber( final int basebits ) {
		boolean point_found = false;
		boolean exp_found = false;
		int offset = mStream.getOffset();
		int c = mStream.next();
		while( c != -1 ) {
			if( c == '.' && point_found == false && exp_found == false ) {
				point_found = true;
				c = mStream.next();
			} else if( (c == 'p' || c == 'P') && exp_found == false ) {
				exp_found = true;
				mStream.skipSpace();
				c = mStream.next();
				if( c == '+' || c == '-' ) {
					mStream.skipSpace();
					c = mStream.next();
				}
			} else if( (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F') ) {
				if( basebits == 3 ) {
					if( c < '0' || c > '7') break;
				} else if( basebits == 1 ) {
					if( c != '0' && c != '1') break;
				}
				c = mStream.next();
			} else {
				break;
			}
		}
		mStream.setOffset(offset);
		return point_found || exp_found;
	}
	static private final int TJS_IEEE_D_SIGNIFICAND_BITS = 52;
	static private final int TJS_IEEE_D_EXP_MIN = -1022;
	static private final int TJS_IEEE_D_EXP_MAX = 1023;
	static private final long TJS_IEEE_D_EXP_BIAS = 1023;
	// base
	// 16進数 : 4
	// 2進数 : 1
	// 8進数 : 3
	private Double parseNonDecimalReal( boolean sign, final int basebits ) {
		long main = 0;
		int exp = 0;
		int numsignif = 0;
		boolean pointpassed = false;

		int c = mStream.getC();
		while( c != -1 ){
			if( c == '.' ) {
				pointpassed = true;
			} else if( c == 'p' || c == 'P' ) {
				mStream.skipSpace();
				c = mStream.next();

				boolean biassign = false;
				if( c == '+' ) {
					biassign = false;
					mStream.skipSpace();
					c = mStream.next();
				}

				if( c == '-' ) {
					biassign = true;
					mStream.skipSpace();
					c = mStream.next();
				}

				int bias = 0;
				while( c >= '0' && c <= '9' ) {
					bias *= 10;
					bias += c - '0';
					c = mStream.next();
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
			c = mStream.next();
		}
		if( c != -1 ) mStream.ungetC();
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
		int c = mStream.next();
		while( c != -1 ) {
			int n = -1;
			if(c >= '0' && c <= '9') n = c - '0';
			else if(c >= 'a' && c <= 'f') n = c - 'a' + 10;
			else if(c >= 'A' && c <= 'F') n = c - 'A' + 10;
			else {
				mStream.ungetC();
				break;
			}
			v <<= 4;
			v += n;
			c = mStream.next();
		}
		if( sign ) {
			return Integer.valueOf((int)-v);
		} else {
			return Integer.valueOf((int)v);
		}
	}
	private Integer parseNonDecimalInteger8( boolean sign ) {
		long v = 0;
		int c = mStream.next();
		while( c != -1 ) {
			int n = -1;
			if(c >= '0' && c <= '7') n = c - '0';
			else {
				mStream.ungetC();
				break;
			}
			v <<= 3;
			v += n;
			c = mStream.next();
		}
		if( sign ) {
			return Integer.valueOf((int)-v);
		} else {
			return Integer.valueOf((int)v);
		}
	}
	private Integer parseNonDecimalInteger2( boolean sign ) {
		long v = 0;
		int c = mStream.next();
		while( c != -1 ) {
			if( c == '1' ) {
				v <<= 1;
				v++;
			} else if( c == '0' ) {
				v <<= 1;
			} else {
				mStream.ungetC();
				break;
			}
			c = mStream.next();
		}
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
	protected boolean isWAlpha( int c ) {
		if( (c & 0xFF00) != 0 ) {
			return true;
		} else if( (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') ) {
			return true;
		}
		return false;
	}
	protected boolean isWDigit( int c ) {
		if( (c&0xFF00) != 0 ) {
			return false;
		} else if( c >= '0' && c <= '9' ) {
			return true;
		}
		return false;
	}

	// @return : Integer or Double or null
	public Number parseNumber() {
		int num = 0;
		boolean sign = false;
		boolean skipNum = false;

		int c = mStream.getC();
		if( c == '+' ) {
			sign = false;
			c = mStream.getC();
		} else if( c == '-' ) {
			sign = true;
			c = mStream.getC();
		}

		if( c == 't' ) {
			if( mStream.equalString("rue") ) {
				return Integer.valueOf(1);
			}
		} else if( c == 'f' ) {
			if( mStream.equalString("alse") ) {
				return Integer.valueOf(0);
			}
		} else if( c == 'N' ) {
			if( mStream.equalString("aN") ) {
				return Double.valueOf(Double.NaN);
			}
		} else if( c == 'I' ) {
			if( mStream.equalString("nfinity") ) {
				if( sign ) {
					return Double.valueOf(Double.NEGATIVE_INFINITY);
				} else {
					return Double.valueOf(Double.POSITIVE_INFINITY);
				}
			}
		}

		//int save = mStream.getOffset();
		// 10進数以外か調べる
		if( c == '0' ) {
			c = mStream.getC();
			if( c == 'x' || c == 'X' ) {
				// hexadecimal
				return parseNonDecimalNumber(sign,4);
			} else if( c == 'b' || c == 'B' ) {
				// binary
				return parseNonDecimalNumber(sign,1);
			} else if( c == '.' ) {
				skipNum = true;
			} else if( c == 'e' || c == 'E' ) {
				skipNum = true;
			} else if( c == 'p' || c == 'P' ) {
				// 2^n exp
				return null;
			} else if( c >= '0' && c <= '7' ) {
				// octal
				mStream.ungetC();
				return parseNonDecimalNumber(sign,3);
			}
		}

		if( skipNum == false ) {
			while( c != -1 ) {
				if( c < '0' || c > '9' ) break;
				num = num * 10 + ( c - '0' );
				c = mStream.getC();
			}
		}
		if( c == '.' || c == 'e' || c == 'E' ) {
			double figure = 1.0;
			int decimal = 0;
			if( c == '.' ) {
				while( c != -1 ) {
					c = mStream.getC();
					if( c < '0' || c > '9' ) break;
					decimal = decimal * 10 + ( c - '0' );
					figure *= 10;
				}
			}
			boolean expSign = false;
			int expValue = 0;
			if( c == 'e' || c == 'E' ) {
				c = mStream.getC();
				if( c == '-' ) {
					expSign = true;
					c = mStream.getC();
				}

				while( c != -1 ) {
					if( c < '0' || c > '9' ) break;
					expValue = expValue * 10 + ( c - '0' );
					c = mStream.getC();
				}
				mStream.ungetC();
			} else {
				mStream.ungetC();
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
			return Double.valueOf( number );
		} else {
			mStream.ungetC();
			if( sign ) num = -num;
			return Integer.valueOf(num);
		}
	}
	public static String escapeC( char c ) {
		StringBuilder ret = new StringBuilder(16);
		switch( c ) {
		case 0x07: ret.append( "\\a" ); break;
		case 0x08: ret.append( "\\b" ); break;
		case 0x0c: ret.append( "\\f" ); break;
		case 0x0a: ret.append( "\\n" ); break;
		case 0x0d: ret.append( "\\r" ); break;
		case 0x09: ret.append( "\\t" ); break;
		case 0x0b: ret.append( "\\v" ); break;
		case '\\': ret.append( "\\\\" ); break;
		case '\'': ret.append( "\\\'" ); break;
		case '\"': ret.append( "\\\"" ); break;
		default:
			if( c < 0x20 ) {
				ret.append("\\x");
				ret.append( Integer.toHexString((int)c) );
			} else {
				ret.append(c);
			}
		}
		return ret.toString();
	}
	public static String escapeC( String str ) {
		final int count = str.length();
		StringBuilder ret = new StringBuilder(count*2);
		for( int i = 0; i < count; i++ ) {
			char c = str.charAt(i);
			switch( c ) {
			case 0x07: ret.append( "\\a" ); break;
			case 0x08: ret.append( "\\b" ); break;
			case 0x0c: ret.append( "\\f" ); break;
			case 0x0a: ret.append( "\\n" ); break;
			case 0x0d: ret.append( "\\r" ); break;
			case 0x09: ret.append( "\\t" ); break;
			case 0x0b: ret.append( "\\v" ); break;
			case '\\': ret.append( "\\\\" ); break;
			case '\'': ret.append( "\\\'" ); break;
			case '\"': ret.append( "\\\"" ); break;
			default:
				if( c < 0x20 ) {
					ret.append("\\x");
					ret.append( Integer.toHexString((int)c) );
				} else {
					ret.append(c);
				}
			}
		}
		return ret.toString();
	}
}

