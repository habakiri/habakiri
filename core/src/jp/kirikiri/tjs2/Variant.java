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

public class Variant implements Cloneable {
	public static void initialize() {
		NullVariantClosure = new VariantClosure(null,null);
		mBuilder = new StringBuilder(256);
	}
	public static void finalizeApplication() {
		NullVariantClosure = null;
		mBuilder = null;
	}
	private static VariantClosure NullVariantClosure;
	private static StringBuilder mBuilder;

	public static final int
		VOID = 0,  // empty
		OBJECT = 1,
		STRING = 2,
		OCTET = 3,  // octet binary data
		INTEGER = 4,
		REAL = 5;

	public static final String TYPE_VOID = "void";
	public static final String TYPE_INTEGER = "int";
	public static final String TYPE_REAL = "real";
	public static final String TYPE_STRING = "string";
	public static final String TYPE_OCTET = "octet";
	public static final String TYPE_OBJECT = "object";

	private Object	mObject;

	/*
	public Object clone() {
		Variant r;
		try {
			r = (Variant)super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
		r.set( mObject.clone() );
		return r;
	}
	*/

	public Variant() {
		//mObject = null; // 初期値なので不要
	}
	public Variant( int value ) {
		mObject = Integer.valueOf(value);
	}
	public Variant( double value ) {
		mObject = Double.valueOf(value);
	}
	public Variant( ByteBuffer value ) {
		mObject = value;
	}
	public Variant( String value ) {
		mObject = value;
	}
	public Variant( Variant value ) {
		//mObject = value.cloneSeparate();
		if( value.mObject instanceof VariantClosure ) {
			VariantClosure clo = (VariantClosure)value.mObject;
			mObject = new VariantClosure( clo.mObject, clo.mObjThis );
		} else {
			mObject = value.mObject;
		}
	}
	public Variant( Object value ) {
		mObject = value;
	}
	public Variant(Dispatch2 dsp, Dispatch2 dsp2) {
		mObject = new VariantClosure( dsp, dsp2 );
	}
	public Variant(Dispatch2 dsp) {
		mObject = new VariantClosure( dsp, null );
	}/*
	public Variant( VariantClosure value ) {
		mObject = value;
	}*/
	public final void set( int value ) {
		mObject = Integer.valueOf(value);
	}
	public final void set( double value ) {
		mObject = Double.valueOf(value);
	}
	public final void set( ByteBuffer value ) {
		mObject = value;
	}
	public final void set( String value ) {
		mObject = value;
	}
	public final void setJavaObject( Object value ) {
		mObject = value;
	}
	public final void set( Variant value ) {
		// copyRef( value );
		if( value.mObject instanceof VariantClosure ) {
			VariantClosure clo = (VariantClosure)value.mObject;
			mObject = new VariantClosure( clo.mObject, clo.mObjThis );
		} else if( mObject != value.mObject ) {
			mObject = value.mObject;
		}
	}
	public final void set(Dispatch2 dsp, Dispatch2 dsp2) {
		mObject = new VariantClosure( dsp, dsp2 );
	}
	public final void set(Dispatch2 dsp ) {
		mObject = new VariantClosure( dsp );
	}
	public final void copyRef( Variant value ) {
		if( value.mObject instanceof VariantClosure ) {
			VariantClosure clo = (VariantClosure)value.mObject;
			mObject = new VariantClosure( clo.mObject, clo.mObjThis );
		} else if( mObject != value.mObject ) {
			mObject = value.mObject;
		}
	}

	// 一部オブジェクトは参照コピー
	public final Object cloneSeparate() {
		if( mObject instanceof VariantClosure ) {
			VariantClosure clo = (VariantClosure)mObject;
			return new VariantClosure( clo.mObject, clo.mObjThis );
		} else {
			return mObject;
		}
	}
	public final static String OctetToListString( ByteBuffer oct ) {
		final int size = oct.capacity();
		final String hex = new String("0123456789ABCDEF");
		StringBuilder str = mBuilder;
		str.delete(0, str.length() );
		for( int i = 0; i < size; i++ ) {
			byte b = oct.get( i );
			str.append( hex.charAt( (b >> 4)&0x0f ) );
			str.append( hex.charAt( b & 0x0f ) );
			if( i != (size-1) ) {
				str.append( ' ' );
			}
		}
		return str.toString();
	}

	// ~ ビット単位NOT
	public final Variant getBitNotValue() throws VariantException {
		int val = asInteger();
		return new Variant( ~val );
	}
	// ! 論理否定
	public final Variant getNotValue() {
		boolean val = !asBoolean();
		return new Variant( val ? 1 : 0 );
	}
	// ||
	public final Variant logicOr( Variant val ) {
		boolean v = asBoolean() || val.asBoolean();
		return new Variant( v ? 1 : 0 );
	}
	// &&
	public final Variant logicAnd( Variant val ) {
		boolean v = asBoolean() && val.asBoolean();
		return new Variant( v ? 1 : 0 );
	}
	// |
	public final Variant bitOr( Variant val ) throws VariantException {
		int v = asInteger() | val.asInteger();
		return new Variant( v );
	}
	// ^
	public final Variant bitXor( Variant val ) throws VariantException {
		int v = asInteger() ^ val.asInteger();
		return new Variant( v );
	}
	// &
	public final Variant bitAnd( Variant val ) throws VariantException {
		int v = asInteger() & val.asInteger();
		return new Variant( v );
	}
	// !=
	public final Variant notEqual( Variant val ) throws VariantException {
		boolean v = normalCompare( val );
		return new Variant( (!v) ? 1 : 0 );
	}
	// ==
	public final Variant equalEqual( Variant val ) throws VariantException {
		boolean v = normalCompare( val );
		return new Variant( v ? 1 : 0 );
	}
	// !==
	public final Variant discNotEqual( Variant val ) {
		boolean v = discernCompareInternal( val );
		return new Variant( (!v) ? 1 : 0 );
	}
	// ===
	public final Variant discernCompare( Variant val ) {
		boolean v = discernCompareInternal( val );
		return new Variant( v ? 1 : 0 );
	}
	// <
	public final Variant lt( Variant val ) throws VariantException {
		boolean v = greaterThan(val);	// なんか逆転してない？ 元の実装がそうだけど？
		return new Variant( v ? 1 : 0 );
	}
	// >
	public final Variant gt( Variant val ) throws VariantException {
		boolean v = littlerThan(val);	// なんか逆転してない？ 元の実装がそうだけど？
		return new Variant( v ? 1 : 0 );
	}
	// <=
	public final Variant ltOrEqual( Variant val ) throws VariantException {
		boolean v = littlerThan(val);
		return new Variant( (!v) ? 1 : 0 );
	}
	// >=
	public final Variant gtOrEqual( Variant val ) throws VariantException {
		boolean v = greaterThan(val);
		return new Variant( (!v) ? 1 : 0 );
	}
	// >>
	public final Variant rightShift( Variant val ) throws VariantException {
		int v = asInteger() >> val.asInteger();
		return new Variant( v );
	}
	// <<
	public final Variant leftShift( Variant val ) throws VariantException {
		int v = asInteger() << val.asInteger();
		return new Variant( v );
	}
	// >>>
	public final Variant rightBitShift( Variant val ) throws VariantException {
		int v = asInteger();
		v = v >>> val.asInteger();
		return new Variant( (int)v );
	}
	// +
	public final Variant add( Variant val ) throws VariantException {
		if( mObject instanceof String || val.mObject instanceof String ) {
			String s1 = asString();
			String s2 = val.asString();
			if( s1 != null && s2 != null ) {
				StringBuilder builder = mBuilder;
				builder.delete(0, builder.length() );
				builder.append(s1);
				builder.append(s2);
				return new Variant( builder.toString() );
			} else if( s1 != null ) {
				return new Variant( s1 );
			} else {
				return new Variant( s2 );
			}
		}
		if( mObject != null && val.mObject != null ) {
			if( mObject.getClass().isAssignableFrom(val.mObject.getClass() ) ) { // 同じクラス
				if( mObject instanceof ByteBuffer ) {
					ByteBuffer b1 = (ByteBuffer)mObject;
					ByteBuffer b2 = (ByteBuffer)val.mObject;
					ByteBuffer result = ByteBuffer.allocate( b1.capacity() + b2.capacity() );
					b1.position(0);
					b2.position(0);
					result.put( b1 );
					result.put( b2 );
					result.position(0);
					return new Variant( result );
				}
				if( mObject instanceof Integer ) {
					int result = ((Integer)mObject).intValue() + ((Integer)val.mObject).intValue();
					return new Variant(result);
				}
			}
		}
		if( mObject == null ) {
			if( val.mObject != null ) {
				if( val.mObject instanceof Integer ) {
					return new Variant( ((Integer)val.mObject).intValue() );
				} else if( val.mObject instanceof Double ) {
					return new Variant( ((Double)val.mObject).doubleValue() );
				}
			}
		}
		if( val.mObject == null ) {
			if( mObject != null ) {
				if( mObject instanceof Integer ) {
					return new Variant( ((Integer)mObject).intValue() );
				} else if( mObject instanceof Double ) {
					return new Variant( ((Double)mObject).doubleValue() );
				}
			}
		}
		return new Variant( asDouble() + val.asDouble() );
	}
	// -
	public final Variant subtract( Variant val ) throws VariantException {
		if( mObject instanceof Integer && val.mObject instanceof Integer ) {
			int result = ((Integer)mObject).intValue() - ((Integer)val.mObject).intValue();
			return new Variant(result);
		}
		Number n1 = asNumber();
		Number n2 = val.asNumber();
		if( n1 instanceof Integer && n2 instanceof Integer ) {
			int result = n1.intValue() - n2.intValue();
			return new Variant(result);
		} else {
			double result = n1.doubleValue() - n2.doubleValue();
			return new Variant(result);
		}
	}
	// %
	public final Variant residue( Variant val ) throws VariantException {
		int r = val.asInteger();
		if( r == 0 ) throwDividedByZero();
		int l = asInteger();
		return new Variant( l % r );
	}
	// /
	public final Variant divide( Variant val ) throws VariantException {
		double l = asDouble();
		double r = val.asDouble();
		return new Variant(l / r);
	}
	// \
	public final Variant idiv( Variant val ) throws VariantException {
		int r = val.asInteger();
		if( r == 0 ) throwDividedByZero();
		int l = asInteger();
		return new Variant( l / r );
	}
	// *
	public final Variant multiply( Variant val ) throws VariantException {
		if( mObject == null || val.mObject == null ) return new Variant(0);

		if( (mObject instanceof Integer) && (val.mObject instanceof Integer ) ) {
			int result = ((Integer)mObject).intValue() * ((Integer)val.mObject).intValue();
			return new Variant(result);
		}
		Number n1 = asNumber();
		Number n2 = val.asNumber();
		if( n1 instanceof Integer && n2 instanceof Integer ) {
			int result = n1.intValue() * n2.intValue();
			return new Variant(result);
		} else {
			double result = n1.doubleValue() * n2.doubleValue();
			return new Variant(result);
		}
	}
	public final boolean normalCompare( final Variant val2 ) throws VariantException {
		if( mObject != null && val2.mObject != null ) {
			if( mObject.getClass().isAssignableFrom(val2.mObject.getClass() ) ) { // 同じクラス
				if( mObject instanceof Integer ) {
					return ((Integer)mObject).intValue() == ((Integer)val2.mObject).intValue();
				}
				if( mObject instanceof String ) {
					return ((String)mObject).equals( ((String)val2.mObject) );
				}
				if( mObject instanceof ByteBuffer ) {
					//return ((ByteBuffer)mObject).compareTo( ((ByteBuffer)val2.mObject) ) == 0;
					ByteBuffer v1 = (ByteBuffer)mObject;
					ByteBuffer v2 = (ByteBuffer)val2.mObject;
					int c1 = v1.limit();
					int c2 = v2.limit();
					if( c1 == c2 ) {
						for( int i = 0; i < c1; i++ ) {
							byte b1 = v1.get(i);
							byte b2 = v2.get(i);
							if( b1 != b2 ) {
								return false;
							}
						}
					} else {
						return false;
					}
					return true;
				}
				if( mObject instanceof Double ) {
					return ((Double)mObject).doubleValue() == ((Double)val2.mObject).doubleValue();
				}
				return mObject.equals( val2.mObject );
			} else {
				if( mObject instanceof String || val2.mObject instanceof String ) {
					String v1 = asString();
					String v2 = val2.asString();
					return v1.equals( v2 );
				} else if( mObject instanceof Number && val2.mObject instanceof Number ) {
					double r1 = ((Number)mObject).doubleValue();
					double r2 = ((Number)val2.mObject).doubleValue();
					if( Double.isNaN(r1) || Double.isNaN(r2) ) return false;
					if( Double.isInfinite(r1) || Double.isInfinite(r2) ) {
						return Double.compare(r1, r2) == 0;
					}
					return r1 == r2;
				} else {
					return false;
				}
			}
		} else { // 片方はnull
			if( mObject == null && val2.mObject == null ) return true;

			if( mObject == null ) {
				if( val2.mObject instanceof Integer ) {
					return ((Integer)val2.mObject).intValue() == 0;
				}
				if( val2.mObject instanceof Double ) {
					return ((Double)val2.mObject).doubleValue() == 0.0;
				}
				if( val2.mObject instanceof String ) {
					return ((String)val2.mObject).length() == 0;
				}
				return false;
			} else {
				if( mObject instanceof Integer ) {
					return ((Integer)mObject).intValue() == 0;
				}
				if( mObject instanceof Double ) {
					return ((Double)mObject).doubleValue() == 0.0;
				}
				if( mObject instanceof String ) {
					return ((String)mObject).length() == 0;
				}
				return false;
			}
		}
	}
	public final boolean discernCompareInternal( final Variant val ) {
		if( mObject != null && val.mObject != null ) {
			if( (mObject instanceof Integer) && (val.mObject instanceof Integer) ) {
				return ((Integer)mObject).intValue() == ((Integer)val.mObject).intValue();
			} else if( (mObject instanceof String) && (val.mObject instanceof String) ) {
				return ((String)mObject).equals( ((String)val.mObject) );
			} else if( (mObject instanceof Double) && (val.mObject instanceof Double) ) {
				double r1 = ((Number)mObject).doubleValue();
				double r2 = ((Number)val.mObject).doubleValue();
				if( Double.isNaN(r1) || Double.isNaN(r2) ) return false;
				if( Double.isInfinite(r1) || Double.isInfinite(r2) ) {
					return Double.compare(r1, r2) == 0;
				}
				return r1 == r2;
			} else if( (mObject instanceof VariantClosure) && (val.mObject instanceof VariantClosure) ) {
				VariantClosure v1 = (VariantClosure)mObject;
				VariantClosure v2 = (VariantClosure)val.mObject;
				return (v1.mObject == v2.mObject && v1.mObjThis == v2.mObjThis);
			} else if( (mObject instanceof ByteBuffer) && (val.mObject instanceof ByteBuffer) ) {
				//return ((ByteBuffer)mObject).compareTo( ((ByteBuffer)val.mObject) ) == 0;
				ByteBuffer v1 = (ByteBuffer)mObject;
				ByteBuffer v2 = (ByteBuffer)val.mObject;
				int c1 = v1.limit();
				int c2 = v2.limit();
				if( c1 == c2 ) {
					for( int i = 0; i < c1; i++ ) {
						byte b1 = v1.get(i);
						byte b2 = v2.get(i);
						if( b1 != b2 ) {
							return false;
						}
					}
				} else {
					return false;
				}
				return true;
			} else if( mObject.getClass().isAssignableFrom(val.mObject.getClass() ) ) { // 同じクラス
				return mObject.equals( val.mObject );
			} else {
				return false;
			}
			/*
			if( mObject.getClass().isAssignableFrom(val.mObject.getClass() ) ) { // 同じクラス
				if( mObject instanceof Integer ) {
					return ((Integer)mObject).intValue() == ((Integer)val.mObject).intValue();
				}
				if( mObject instanceof String ) {
					return ((String)mObject).equals( ((String)val.mObject) );
				}
				if( mObject instanceof ByteBuffer ) {
					//return ((ByteBuffer)mObject).compareTo( ((ByteBuffer)val.mObject) ) == 0;
					ByteBuffer v1 = (ByteBuffer)mObject;
					ByteBuffer v2 = (ByteBuffer)val.mObject;
					int c1 = v1.limit();
					int c2 = v2.limit();
					if( c1 == c2 ) {
						for( int i = 0; i < c1; i++ ) {
							byte b1 = v1.get(i);
							byte b2 = v2.get(i);
							if( b1 != b2 ) {
								return false;
							}
						}
					} else {
						return false;
					}
					return true;
				}
				if( mObject instanceof Double ) {
					double r1 = ((Number)mObject).doubleValue();
					double r2 = ((Number)val.mObject).doubleValue();
					if( Double.isNaN(r1) || Double.isNaN(r2) ) return false;
					if( Double.isInfinite(r1) || Double.isInfinite(r2) ) {
						return Double.compare(r1, r2) == 0;
					}
					return r1 == r2;
				}
				if( mObject instanceof VariantClosure ) {
					VariantClosure v1 = (VariantClosure)mObject;
					VariantClosure v2 = (VariantClosure)val.mObject;
					return (v1.mObject == v2.mObject && v1.mObjThis == v2.mObjThis);
				}

				return mObject.equals( val.mObject );
			} else {
				return false;
			}
			*/
		} else if( mObject == null && val.mObject == null ) {
			return true;
		} else{
			return false;
		}
	}
	public final boolean discernCompareStrictReal( final Variant val ) throws VariantException {
		if( mObject != null && val.mObject != null ) {
			if( (mObject instanceof Double) && (val.mObject instanceof Double) ) {
				return ((Double)mObject).doubleValue() == ((Double)val.mObject).doubleValue();
			} else if( (mObject instanceof Integer) && (val.mObject instanceof Integer) ) {
				return ((Integer)mObject).intValue() == ((Integer)val.mObject).intValue();
			} else if( (mObject instanceof String) && (val.mObject instanceof String) ) {
				return ((String)mObject).equals( ((String)val.mObject) );
			} else if( (mObject instanceof VariantClosure) && (val.mObject instanceof VariantClosure) ) {
				VariantClosure v1 = (VariantClosure)mObject;
				VariantClosure v2 = (VariantClosure)val.mObject;
				return (v1.mObject == v2.mObject && v1.mObjThis == v2.mObjThis);
			} else if( (mObject instanceof ByteBuffer) && (val.mObject instanceof ByteBuffer) ) {
				ByteBuffer v1 = (ByteBuffer)mObject;
				ByteBuffer v2 = (ByteBuffer)val.mObject;
				int c1 = v1.limit();
				int c2 = v2.limit();
				if( c1 == c2 ) {
					for( int i = 0; i < c1; i++ ) {
						byte b1 = v1.get(i);
						byte b2 = v2.get(i);
						if( b1 != b2 ) {
							return false;
						}
					}
				} else {
					return false;
				}
				return true;
			} else if( mObject.getClass().isAssignableFrom(val.mObject.getClass() ) ) { // 同じクラス
				return mObject.equals( val.mObject );
			} else {
				return false;
			}
			//return discernCompareInternal(val);
		} else if( mObject == null && val.mObject == null ) {
			return true;
		} else{
			return false;
		}
		//return normalCompare(val);
	}
	public final boolean greaterThan( final Variant val ) throws VariantException {
		if( (mObject instanceof String) == false || (val.mObject instanceof String) == false ) {
			if( (mObject instanceof Integer) && (val.mObject instanceof Integer ) ) {
				return ((Integer)mObject).intValue() < ((Integer)val.mObject).intValue();
			}
			return asDouble() < val.asDouble();
		}
		String s1 = asString();
		String s2 = val.asString();
		return s1.compareTo( s2 ) < 0;
	}
	//0：等しい。1：より大きい。-1：より小さい
	public final int greaterThanForSort( final Variant val ) throws VariantException {
		if( (mObject instanceof String) == false || (val.mObject instanceof String) == false ) {
			if( (mObject instanceof Integer) && (val.mObject instanceof Integer ) ) {
				return ((Integer)mObject).intValue() - ((Integer)val.mObject).intValue();
			}
			double ret = (asDouble() - val.asDouble());
			if( ret == 0.0 ) return 0;
			else if( ret < 0.0 ) return -1;
			else return 1;
		}
		String s1 = asString();
		String s2 = val.asString();
		return s1.compareTo( s2 );
	}
	public final boolean littlerThan( final Variant val ) throws VariantException {
		if( (mObject instanceof String) == false || (val.mObject instanceof String) == false ) {
			if( (mObject instanceof Integer) && (val.mObject instanceof Integer ) ) {
				return ((Integer)mObject).intValue() > ((Integer)val.mObject).intValue();
			}
			return asDouble() > val.asDouble();
		}
		String s1 = asString();
		String s2 = val.asString();
		return s1.compareTo( s2 ) > 0;
	}
	public final int littlerThanForSort( final Variant val ) throws VariantException {
		if( (mObject instanceof String) == false || (val.mObject instanceof String) == false ) {
			if( (mObject instanceof Integer) && (val.mObject instanceof Integer ) ) {
				return ((Integer)val.mObject).intValue() - ((Integer)mObject).intValue();
			}
			double ret = val.asDouble() - asDouble();
			if( ret == 0.0 ) return 0;
			else if( ret < 0.0 ) return -1;
			else return 1;
		}
		String s1 = asString();
		String s2 = val.asString();
		return s2.compareTo( s1 );
	}
	public final void asNumber( Variant targ ) throws VariantException {
		if( mObject == null ) {
			targ.set( 0 );
		} else if( mObject instanceof Number ) { // Integer or Double
			if( mObject instanceof Integer ) {
				targ.set( ((Integer)mObject).intValue() );
			} else {
				targ.set( ((Number)mObject).doubleValue() );
			}
		} else if( mObject instanceof String ) {
			LexBase lex = new LexBase( (String)mObject );
			Number num = lex.parseNumber();
			if( num != null ) {
				if( num instanceof Integer ) {
					targ.set( ((Integer)num).intValue() );
				} else {
					targ.set( ((Number)num).doubleValue() );
				}
			} else {
				targ.set( 0 );
			}
		} else { // convert error
			throwVariantConvertError( this, TYPE_INTEGER, TYPE_REAL );
		}
	}
	public final Number asNumber() throws VariantException {
		if( mObject instanceof Integer ) {
			return Integer.valueOf( ((Integer)mObject).intValue() );
		} else if( mObject instanceof Double ) {
			return Double.valueOf( ((Double)mObject).doubleValue() );
		} else if( mObject instanceof String ) {
			LexBase lex = new LexBase( (String)mObject );
			Number num = lex.parseNumber();
			if( num != null ) {
				return num;
			} else {
				return Integer.valueOf( 0 );
			}
		} else if( mObject == null ) {
			return Integer.valueOf( 0 );
		}
		// convert error
		throwVariantConvertError( this, TYPE_INTEGER, TYPE_REAL );
		return null;
	}

	public final void changeSign() throws VariantException {
		if( mObject instanceof Integer ) {
			mObject = Integer.valueOf( - ((Integer)mObject).intValue() );
			return;
		}
		Number val = asNumber();
		if( val instanceof Integer ) {
			mObject = Integer.valueOf( - val.intValue() );
		} else {
			mObject = Double.valueOf( - val.doubleValue() );
		}
	}
	public final void toNumber() throws VariantException {
		if( mObject == null ) {
			mObject = Integer.valueOf(0);
		} else if( mObject instanceof Number ) {
			return;
		} else if( mObject instanceof String ) {
			Number num = stringToNumber( (String)mObject );
			if( num instanceof Integer ) {
				mObject = Integer.valueOf( num.intValue() );
			} else {
				mObject = Double.valueOf( num.doubleValue() );
			}
		} else {
			throwVariantConvertError( this, TYPE_INTEGER, TYPE_REAL );
		}
	}
	public final void toInteger() throws VariantException {
		mObject = Integer.valueOf( asInteger() );
	}
	public final void toReal() throws VariantException {
		mObject = Double.valueOf( asDouble() );
	}
	public final void selfToString() throws VariantException {
		if( mObject == null || mObject instanceof String ) {
			return;
		} else if( mObject instanceof Integer ) {
			mObject = ((Integer)mObject).toString();
		} else if( mObject instanceof Double ) {
			mObject = ((Double)mObject).toString();
		} else if( mObject instanceof ByteBuffer ) {
			throwVariantConvertError( this, TYPE_STRING );
		} else {
			mObject = Utils.VariantToReadableString( this );
		}
	}
	public final void toOctet() throws VariantException {
		if( mObject == null || mObject instanceof ByteBuffer ) return;

		throwVariantConvertError( this, TYPE_OCTET );
	}
	public final boolean asBoolean() {
		if( mObject instanceof Integer ) {
			return ((Integer)mObject).intValue() == 0 ? false: true;
		} else if( mObject instanceof Double ) {
			return ((Double)mObject).doubleValue() == 0.0 ? false : true;
		} else if( mObject instanceof String ) {
			LexBase lex = new LexBase( (String)mObject );
			Number num = lex.parseNumber();
			if( num != null ) {
				return num.intValue() == 0 ? false : true;
			} else {
				return false;
			}
		} else if( mObject instanceof VariantClosure ) {
			VariantClosure v = (VariantClosure)mObject;
			return ( v.mObject != null );
		} else if( mObject instanceof ByteBuffer ) {
			return true;
		}  else if( mObject == null ) {
			return false;
		} else {
			return false;
		}
	}
	public final int asInteger() throws VariantException {
		if( mObject instanceof Integer ) {
			return ((Integer)mObject).intValue();
		} else if( mObject instanceof Double ) {
			return ((Double)mObject).intValue();
		} else if( mObject instanceof String ) {
			LexBase lex = new LexBase( (String)mObject );
			Number num = lex.parseNumber();
			if( num != null ) {
				return num.intValue();
			} else {
				return 0;
			}
		} else  if( mObject == null ) {
			return 0;
		} else { // bytebuffer or object
			throwVariantConvertError( this, TYPE_INTEGER );
		}
		return 0;
	}
	public final double asDouble() throws VariantException {
		if( mObject instanceof Double ) {
			return ((Double)mObject).doubleValue();
		} else if( mObject instanceof Integer ) {
			return ((Integer)mObject).doubleValue();
		} else if( mObject instanceof String ) {
			LexBase lex = new LexBase( (String)mObject );
			Number num = lex.parseNumber();
			if( num != null ) {
				return num.doubleValue();
			} else {
				return 0.0;
			}
		} else  if( mObject == null ) {
			return 0.0;
		} else { // bytebuffer or object
			throwVariantConvertError( this, TYPE_REAL );
		}
		return 0;
	}
	public final String asString() throws VariantException {
		if( mObject instanceof String ) {
			//return new String( (String)mObject );
			return (String)mObject;
		} else if( mObject instanceof Integer ) {
			return ((Integer)mObject).toString();
		} else if( mObject instanceof Double ) {
			return ((Double)mObject).toString();
		} else  if( mObject == null ) {
			return null;
		} else if( mObject instanceof ByteBuffer ) {
			throwVariantConvertError( this, TYPE_STRING );
		} else {
			return Utils.VariantToReadableString( this );
		}
		return null;
	}
	public final VariantClosure asObjectClosure() throws VariantException {
		if( mObject instanceof VariantClosure ) {
			return (VariantClosure)mObject;
		}
		throwVariantConvertError( this, TYPE_OBJECT );
		return NullVariantClosure;
	}
	public final ByteBuffer asOctet() throws VariantException {
		if( mObject == null ) {
			return null;
		} else if( mObject instanceof ByteBuffer ) {
			return (ByteBuffer)mObject;
		} else {
			throwVariantConvertError( this, TYPE_OCTET );
		}
		return null;
	}
	public final Dispatch2 asObject() throws VariantException {
		if( mObject instanceof VariantClosure ) {
			return ((VariantClosure)mObject).mObject;
		}
		throwVariantConvertError( this, TYPE_OBJECT );
		return null;
	}
	public final Dispatch2 asObjectThis() throws VariantException {
		if( mObject instanceof VariantClosure ) {
			return ((VariantClosure)mObject).mObjThis;
		}
		throwVariantConvertError( this, TYPE_OBJECT);
		return null;
	}
	public final void changeClosureObjThis( Dispatch2 objthis ) throws VariantException {
		if( mObject instanceof VariantClosure ) {
			VariantClosure vc = (VariantClosure)mObject;
			if( vc.mObjThis != null ) {
				vc.mObjThis = null;
			}
			vc.mObjThis = objthis;
		} else {
			throwVariantConvertError( this, TYPE_OBJECT );
		}
	}

	public final static void throwVariantConvertError( final Variant from, final String to ) throws VariantException {
		if( to.equals( TYPE_OBJECT ) ) {
			String mes = Error.VariantConvertErrorToObject.replace( "%1", Utils.VariantToReadableString(from) );
			throw new VariantException( mes );
		} else {
			String mes = Error.VariantConvertError.replace( "%1", Utils.VariantToReadableString(from) );
			String mes2 = mes.replace( "%2", to );
			throw new VariantException( mes2 );
		}
	}
	public final static void throwVariantConvertError( final Variant from, final String to1, final String to2 ) throws VariantException {
		String mes = Error.VariantConvertError.replace( "%1", Utils.VariantToReadableString(from) );
		String mes2 = mes.replace( "%2", to1 + "/" + to2 );
		throw new VariantException( mes2 );
	}
	public final static void throwDividedByZero() throws VariantException {
		throw new VariantException( Error.DivideByZero );
	}
	public final static Number stringToNumber( String str ) {
		LexBase lex = new LexBase( str );
		Number num = lex.parseNumber();
		if( num != null ) {
			return num;
		} else {
			return Integer.valueOf( 0 );
		}
	}
	public final boolean isString() { return mObject instanceof String; }
	public final boolean isObject() { return mObject instanceof VariantClosure; }
	public final boolean isInteger() { return mObject instanceof Integer; }
	public final boolean isReal() { return mObject instanceof Double; }
	public final boolean isOctet() { return mObject instanceof ByteBuffer; }
	public final boolean isVoid() { return mObject == null; }
	public final boolean isNumber() { return mObject instanceof Number; }
	public final void clear() {
		mObject = null;
	}
	private final static long IEEE_D_SIGN_MASK = 0x8000000000000000L;
	public final static String specialRealToString( double r ) {
		if( Double.isNaN(r) ) {
			return "NaN";
		}
		if( Double.isInfinite(r) ) {
			if( Double.NEGATIVE_INFINITY == r)
				return "-Infinity";
			else
				return "+Infinity";
		}
		if( r == 0.0 ) {
			long ui64 = Double.doubleToLongBits(r);
			if( (ui64 & IEEE_D_SIGN_MASK) != 0 )
				return "-0.0";
			else
				return "+0.0";
		}
		return null;
	}
	private final static long D_EXP_MASK = 0x7ff0000000000000L;
	private final static int D_SIGNIFICAND_BITS = 52;
	private final static long D_EXP_BIAS = 1023;
	public final static String realToHexString( double r ) {
		String v = specialRealToString(r);
		if( v != null ) return v;

		long ui64 = Double.doubleToLongBits(r);
		StringBuilder builder = mBuilder;
		builder.delete(0, builder.length() );
		if( (ui64 & IEEE_D_SIGN_MASK) != 0 ) {
			builder.append("-0x1.");
		} else {
			builder.append("0x1.");
		}

		final String hexdigits = new String("0123456789ABCDEF");
		int exp = (int)(((ui64&D_EXP_MASK)>>D_SIGNIFICAND_BITS)-D_EXP_BIAS);
		int bits = D_SIGNIFICAND_BITS;
		while( true ) {
			bits -= 4;
			if(bits < 0) break;
			builder.append( hexdigits.charAt( (int)(ui64>>bits) & 0x0f ) );
		}

		builder.append('p');
		builder.append(String.valueOf(exp));
		return builder.toString();
	}
	public final static String octetToListString( final ByteBuffer oct ) {
		if( oct == null ) return null;
		if( oct.capacity() == 0) return null;

		//int stringlen = oct.capacity() * 3 -1;
		StringBuilder str = mBuilder;
		str.delete(0, str.length() );
		final String hex = new String("0123456789ABCDEF");
		final int count = oct.capacity();
		for( int i = 0; i < count; i++ ) {
			byte data = oct.get(i);
			str.append(hex.charAt((data>>4)&0x0f));
			str.append(hex.charAt(data&0x0f));
			if( i != (count-1) ) str.append(' ');
		}
		return str.toString();
	}
	// &=
	public final void andEqual(Variant rhs) throws VariantException {
		int l = asInteger();
		mObject = null;
		mObject = Integer.valueOf( l & rhs.asInteger() );
	}
	// |=
	public final void orEqual(Variant rhs) throws VariantException {
		int l = asInteger();
		mObject = null;
		mObject = Integer.valueOf( l | rhs.asInteger() );
	}
	// ^=
	public final void bitXorEqual(Variant rhs) throws VariantException {
		int l = asInteger();
		mObject = null;
		mObject = Integer.valueOf( l ^ rhs.asInteger() );
	}
	// -=
	public final void subtractEqual(Variant rhs) throws VariantException {
		if( mObject instanceof Integer && rhs.mObject instanceof Integer ) {
			mObject = Integer.valueOf( ((Integer)mObject).intValue() - ((Integer)rhs.mObject).intValue() );
			return;
		}
		Number l = asNumber();
		Number r = rhs.asNumber();
		if( l instanceof Integer && r instanceof Integer ) {
			mObject = Integer.valueOf( ((Integer)l).intValue() - ((Integer)r).intValue() );
		} else {
			mObject = Double.valueOf( l.doubleValue() - r.doubleValue() );
		}
	}
	// +=
	public final void addEqual(Variant rhs) throws VariantException {
		if( mObject instanceof String || rhs.mObject instanceof String ) {
			if( mObject instanceof String && rhs.mObject instanceof String ) {
				// both are string
				mObject = (String)mObject + (String)rhs.mObject;
				return;
			}

			String s1 = asString();
			String s2 = rhs.asString();
			if( s1 != null && s2 != null ) {
				StringBuilder builder = mBuilder;
				builder.delete(0, builder.length() );
				builder.append(s1);
				builder.append(s2);
				mObject = builder.toString();
			} else if( s1 != null ) {
				mObject = s1;
			} else {
				mObject = s2;
			}
			//mObject = s1 + s2;
			return;
		}

		if( mObject != null && rhs.mObject != null ) {
			if( mObject.getClass().isAssignableFrom(rhs.mObject.getClass() ) ) { // 同じクラス
				if( mObject instanceof ByteBuffer ) {
					ByteBuffer b1 = (ByteBuffer)mObject;
					ByteBuffer b2 = (ByteBuffer)rhs.mObject;
					ByteBuffer result = ByteBuffer.allocate( b1.capacity() + b2.capacity() );
					b1.position(0);
					b2.position(0);
					result.put( b1 );
					result.put( b2 );
					result.position(0);
					mObject = result;
					return;
				}
				if( mObject instanceof Integer ) {
					int result = ((Integer)mObject).intValue() + ((Integer)rhs.mObject).intValue();
					mObject = Integer.valueOf(result);
					return;
				}
			}
		}
		if( mObject == null ) {
			if( rhs.mObject != null ) {
				if( rhs.mObject instanceof Integer ) {
					mObject = Integer.valueOf( ((Integer)rhs.mObject).intValue() );
					return;
				} else if( rhs.mObject instanceof Double ) {
					mObject = Double.valueOf( ((Double)rhs.mObject).doubleValue() );
					return;
				}
			}
		}
		if( rhs.mObject == null ) {
			if( mObject != null ) {
				if( mObject instanceof Integer ) return;
				else if( mObject instanceof Double ) return;
			}
		}
		mObject = Double.valueOf( asDouble() + rhs.asDouble() );
	}
	// %=
	public final void residueEqual(Variant rhs) throws VariantException {
		int r = rhs.asInteger();
		if( r == 0 ) throwDividedByZero();
		int l = asInteger();
		mObject = Integer.valueOf( l % r );
	}
	// /=
	public final void divideEqual(Variant rhs) throws VariantException {
		double l = asDouble();
		double r = rhs.asDouble();
		mObject = Double.valueOf(l/r);
	}
	public final void idivequal(Variant rhs) throws VariantException {
		int r = rhs.asInteger();
		if( r == 0 ) throwDividedByZero();
		int l = asInteger();
		mObject = Integer.valueOf( l / r );
	}
	public final void logicalorequal(Variant rhs) {
		boolean l = asBoolean();
		boolean r = rhs.asBoolean();
		mObject = Integer.valueOf( ( l || r ) ? 1 : 0 );
	}
	// *=
	public final void multiplyEqual(Variant rhs) throws VariantException {
		if( mObject instanceof Integer && rhs.mObject instanceof Integer ) {
			mObject = Integer.valueOf( ((Integer)mObject).intValue() * ((Integer)rhs.mObject).intValue() );
			return;
		}
		Number l = asNumber();
		Number r = rhs.asNumber();
		if( l instanceof Integer && r instanceof Integer ) {
			mObject = Integer.valueOf( l.intValue() * r.intValue() );
			return;
		}
		mObject = Double.valueOf( l.doubleValue() * r.doubleValue() );
	}
	public final void logicalandequal(Variant rhs) {
		boolean l = asBoolean();
		boolean r = rhs.asBoolean();
		mObject = Integer.valueOf( ( l && r ) ? 1 : 0 );
	}
	// >>=
	public final void rightShiftEqual(Variant rhs) throws VariantException {
		int l = asInteger();
		int r = rhs.asInteger();
		mObject = Integer.valueOf( l >> r );
	}
	// <<=
	public final void leftShiftEqual(Variant rhs) throws VariantException {
		int l = asInteger();
		int r = rhs.asInteger();
		mObject = Integer.valueOf( l << r );
	}
	public final void rbitshiftequal(Variant rhs) throws VariantException {
		int l = asInteger();
		int r = rhs.asInteger();
		mObject = Integer.valueOf( (int) (l >>> r) );
	}
	public final void increment() throws VariantException {
		if( mObject instanceof String ) toNumber();

		if( mObject instanceof Double ) {
			mObject = Double.valueOf( ((Double)mObject).doubleValue()+1.0 );
		} else if( mObject instanceof Integer ) {
			mObject = Integer.valueOf( ((Integer)mObject).intValue()+1 );
		} else if( mObject == null ) {
			mObject = Integer.valueOf( 1 );
		} else {
			throwVariantConvertError( this, TYPE_INTEGER, TYPE_REAL );
		}
	}
	public final void decrement() throws VariantException {
		if( mObject instanceof String ) toNumber();

		if( mObject instanceof Double ) {
			mObject = Double.valueOf( ((Double)mObject).doubleValue()-1.0 );
		} else if( mObject instanceof Integer ) {
			mObject = Integer.valueOf( ((Integer)mObject).intValue()-1 );
		} else if( mObject == null ) {
			mObject = Integer.valueOf( -1 );
		} else {
			throwVariantConvertError( this, TYPE_INTEGER, TYPE_REAL );
		}
	}
	public final String getString() throws VariantException {
		// returns String
		if( mObject != null && mObject instanceof String ) {
			return (String)mObject;
		} else {
			throwVariantConvertError( this, TYPE_STRING );
			return null;
		}
	}
	public final String getTypeName() {
		if( mObject == null ) return "void";
		else if( mObject instanceof VariantClosure ) return "Object";
		else if( mObject instanceof String ) return "String";
		else if( mObject instanceof Integer ) return "Integer";
		else if( mObject instanceof Double ) return "Real";
		else if( mObject instanceof ByteBuffer ) return "Octet";
		else return null;
	}
	public final void logicalnot() {
		boolean res = !asBoolean();
		mObject = Integer.valueOf( res ? 1 : 0 );
	}
	public final void bitnot() throws VariantException {
		int res = ~asInteger();
		mObject = Integer.valueOf( res );
	}
	public final void tonumber() throws VariantException {
		if( mObject instanceof Number ) return; // nothing to do
		if( mObject instanceof String ) {
			Number num = stringToNumber( (String)mObject );
			if( num instanceof Integer ) {
				mObject = Integer.valueOf( num.intValue() );
			} else {
				mObject = Double.valueOf( num.doubleValue() );
			}
			return;
		}
		if( mObject == null ) { mObject = Integer.valueOf(0); return; }
		throwVariantConvertError( this, TYPE_INTEGER, TYPE_REAL );
	}
	public final void changesign() throws VariantException {
		if( mObject instanceof Integer ) {
			mObject = Integer.valueOf( - ((Integer)mObject).intValue() );
			return;
		}
		Number val = asNumber();
		if( val instanceof Integer ) {
			mObject = Integer.valueOf( - val.intValue() );
		} else {
			mObject = Double.valueOf( - val.doubleValue() );
		}
	}
	public final Object toJavaObject() { return mObject; }
	public final void setObject( Dispatch2 ref ) {
		mObject = new VariantClosure(ref,null);
	}
	public final void setObject( Dispatch2 object, Dispatch2 objthis ) {
		mObject = new VariantClosure(object,objthis);
	}
	@Override
	public final String toString() {
		try {
			if( isVoid() ) {
				return "(void)";
			} else if( isInteger() ) {
				return new String( "(int)" + asString() );
			} else if( isReal() ) {
				return new String( "(real)" + asString() );
			} else if( isString() ) {
				return new String( "(string)\"" + LexBase.escapeC( asString() ) + "\"" );
			} else if( isOctet() ) {
				return new String( "(octet)<% " + Variant.OctetToListString( asOctet() ) + " %>" );
			} else if( isObject() ) {
				VariantClosure c = (VariantClosure)asObjectClosure();
				return c.toString();
			} else { // native object ?
				return new String( "(octet) [" + getClass().getName() + "]" );
			}
		} catch (VariantException e) {
			return "";
		}
	}
	public final String toJavaString() {
		try {
			if( isVoid() ) {
				return null;
			} else if( isInteger() ) {
				return asString();
			} else if( isReal() ) {
				return asString();
			} else if( isString() ) {
				return "\"" + LexBase.escapeC( asString() ) + "\"";
			} else if( isOctet() ) {
				ByteBuffer buf = asOctet();
				if( buf != null && buf.capacity() != 0 ) {
					StringBuilder builder = mBuilder;
					builder.delete(0, builder.length() );
					final int count = buf.capacity();
					builder.append('{');
					for( int i = 0; i < count; i++ ) {
						byte b = buf.get(i);
						builder.append("0x");
						builder.append(Integer.toHexString(b));
						builder.append(", ");
					}
					builder.append('}');
				}
				return new String( "(octet)<% " + Variant.OctetToListString( asOctet() ) + " %>" );
			} else if( isObject() ) {
				return " new " + mObject.getClass().getName();
			} else { // native object ?
				return " new " + mObject.getClass().getName();
			}
		} catch (VariantException e) {
			return " error ";
		}
	}
}
