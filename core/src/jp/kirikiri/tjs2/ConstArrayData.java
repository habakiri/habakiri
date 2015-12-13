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
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * TJS2 バイトコード書き出し、読み込みで Variant 型を分離し、固有型で保持するためのクラス
 * 読み込みはより効率的に処理できるように別クラスにした方がいいか
 * 読み込んだ ByteBuffer を直接処理するような
 *
 */
public class ConstArrayData {
	private ArrayList<Byte> mByte;
	private ArrayList<Short> mShort;
	private ArrayList<Integer> mInteger;
	private ArrayList<Long> mLong;
	private ArrayList<Double> mDouble;
	private ArrayList<String> mString;
	private ArrayList<ByteBuffer> mByteBuffer;

	// 保持したかどうか判定するためのハッシュ
	private HashMap<Byte,Integer> mByteHash;
	private HashMap<Short,Integer> mShortHash;
	private HashMap<Integer,Integer> mIntegerHash;
	private HashMap<Long,Integer> mLongHash;
	private HashMap<Double,Integer> mDoubleHash;
	private HashMap<String,Integer> mStringHash;
	private HashMap<ByteBuffer,Integer> mByteBufferHash;

	private static final byte TYPE_VOID = 0;
	private static final byte TYPE_OBJECT = 1;
	private static final byte TYPE_INTER_OBJECT = 2;
	private static final byte TYPE_STRING = 3;
	private static final byte TYPE_OCTET = 4;
	private static final byte TYPE_REAL = 5;
	private static final byte TYPE_BYTE = 6;
	private static final byte TYPE_SHORT = 7;
	private static final byte TYPE_INTEGER = 8;
	private static final byte TYPE_LONG = 9;
	private static final byte TYPE_INTER_GENERATOR = 10; // temporary
	private static final byte TYPE_UNKNOWN = -1;

	public ConstArrayData() {
		mByte = new ArrayList<Byte>();
		mShort = new ArrayList<Short>();
		mInteger = new ArrayList<Integer>();
		mLong = new ArrayList<Long>();
		mDouble = new ArrayList<Double>();
		mString = new ArrayList<String>();
		mByteBuffer = new ArrayList<ByteBuffer>();

		mByteHash = new HashMap<Byte,Integer>();
		mShortHash = new HashMap<Short,Integer>();
		mIntegerHash = new HashMap<Integer,Integer>();
		mLongHash = new HashMap<Long,Integer>();
		mDoubleHash = new HashMap<Double,Integer>();
		mStringHash = new HashMap<String,Integer>();
		mByteBufferHash = new HashMap<ByteBuffer,Integer>();
	}
	private final int putByteBuffer( ByteBuffer val ) {
		Integer index = mByteBufferHash.get(val);
		if( index == null ) {
			index = Integer.valueOf(mByteBuffer.size());
			mByteBuffer.add(val);
			mByteBufferHash.put(val, index);
			return index.intValue();
		} else {
			return index.intValue();
		}
	}
	public final int putString( String val ) {
		Integer index = mStringHash.get(val);
		if( index == null ) {
			index = Integer.valueOf(mString.size());
			mString.add(val);
			mStringHash.put(val, index);
			return index.intValue();
		} else {
			return index.intValue();
		}
	}
	private final int putByte( byte b ) {
		Byte val = Byte.valueOf(b);
		Integer index = mByteHash.get(val);
		if( index == null ) {
			index = Integer.valueOf(mByte.size());
			mByte.add(val);
			mByteHash.put(val, index);
			return index.intValue();
		} else {
			return index.intValue();
		}
	}
	private final int putShort( short b ) {
		Short val = Short.valueOf(b);
		Integer index = mShortHash.get(val);
		if( index == null ) {
			index = Integer.valueOf(mShort.size());
			mShort.add(val);
			mShortHash.put(val, index);
			return index.intValue();
		} else {
			return index.intValue();
		}
	}
	private final int putInteger( int b ) {
		Integer val = Integer.valueOf(b);
		Integer index = mIntegerHash.get(val);
		if( index == null ) {
			index = Integer.valueOf(mInteger.size());
			mInteger.add(val);
			mIntegerHash.put(val, index);
			return index.intValue();
		} else {
			return index.intValue();
		}
	}
	private final int putLong( long b ) {
		Long val = Long.valueOf(b);
		Integer index = mLongHash.get(val);
		if( index == null ) {
			index = Integer.valueOf(mLong.size());
			mLong.add(val);
			mLongHash.put(val, index);
			return index.intValue();
		} else {
			return index.intValue();
		}
	}
	private final int putDouble( double b ) {
		Double val = Double.valueOf(b);
		Integer index = mDoubleHash.get(val);
		if( index == null ) {
			index = Integer.valueOf(mDouble.size());
			mDouble.add(val);
			mDoubleHash.put(val, index);
			return index.intValue();
		} else {
			return index.intValue();
		}
	}
	public final byte getType( Variant v ) {
		Object o = v.toJavaObject();
		if( o == null ) {
			return TYPE_VOID;
		} else if( o instanceof String ) {
			return TYPE_STRING;
		} else if( o instanceof Integer ) {
			int val = ((Integer)o).intValue();
			if( val >= Byte.MIN_VALUE && val <= Byte.MAX_VALUE ) {
				return TYPE_BYTE;
			} else if( val >= Short.MIN_VALUE && val <= Short.MAX_VALUE ) {
				return TYPE_SHORT;
			} else {
				return TYPE_INTEGER;
			}
		} else if( o instanceof Double ) {
			return TYPE_REAL;
		} else if( o instanceof VariantClosure ) {
			VariantClosure clo = (VariantClosure)o;
			Dispatch2 dsp = clo.mObject;
			if( dsp instanceof InterCodeObject ) {
				return TYPE_INTER_OBJECT;
			} else {
				return TYPE_OBJECT;
			}
		} else if( o instanceof InterCodeGenerator ) {
			return TYPE_INTER_GENERATOR;
		} else if( o instanceof ByteBuffer ) {
			return TYPE_OCTET;
		} else if( o instanceof Long ) {
			long val = ((Long)o).longValue();
			if( val >= Byte.MIN_VALUE && val <= Byte.MAX_VALUE ) {
				return TYPE_BYTE;
			} else if( val >= Short.MIN_VALUE && val <= Short.MAX_VALUE ) {
				return TYPE_SHORT;
			} else if( val >= Integer.MIN_VALUE && val <= Integer.MAX_VALUE ){
				return TYPE_INTEGER;
			} else {
				return TYPE_LONG;
			}
		}
		return TYPE_UNKNOWN;
	}
	public final int putVariant( Variant v, ScriptBlock block ) {
		Object o = v.toJavaObject();
		int type = getType( v );
		switch( type ) {
		case TYPE_VOID:
			return 0; // 常に0
		case TYPE_OBJECT: {
			VariantClosure clo = (VariantClosure)o;
			if( clo.mObject == null && clo.mObjThis == null ) {
				return 0; // null の VariantClosure は受け入れる
			} else {
				return -1; // その他は入れない。Dictionary と Array は保存できるようにした方がいいが……
			}
		}
		case TYPE_INTER_OBJECT: {
			VariantClosure clo = (VariantClosure)o;
			Dispatch2 dsp = clo.mObject;
			return block.getObjectIndex((InterCodeObject)dsp);
		}
		case TYPE_STRING:
			return putString( ((String)o) );
		case TYPE_OCTET:
			return putByteBuffer( (ByteBuffer)o );
		case TYPE_REAL:
			return putDouble( ((Number)o).doubleValue() );
		case TYPE_BYTE:
			return putByte( ((Number)o).byteValue() );
		case TYPE_SHORT:
			return putShort( ((Number)o).shortValue() );
		case TYPE_INTEGER:
			return putInteger( ((Number)o).intValue() );
		case TYPE_LONG:
			return putLong( ((Number)o).longValue() );
		case TYPE_UNKNOWN:
			return -1;
		}
		return -1;
	}
	public final int putVariant( Variant v, Compiler block ) {
		Object o = v.toJavaObject();
		int type = getType( v );
		switch( type ) {
		case TYPE_VOID:
			return 0; // 常に0
		case TYPE_OBJECT: {
			VariantClosure clo = (VariantClosure)o;
			if( clo.mObject == null && clo.mObjThis == null ) {
				return 0; // null の VariantClosure は受け入れる
			} else {
				return -1; // その他は入れない。Dictionary と Array は保存できるようにした方がいいが……
			}
		}
		case TYPE_INTER_OBJECT: {
			VariantClosure clo = (VariantClosure)o;
			Dispatch2 dsp = clo.mObject;
			return block.getObjectIndex((InterCodeObject)dsp);
		}
		case TYPE_STRING:
			return putString( ((String)o) );
		case TYPE_OCTET:
			return putByteBuffer( (ByteBuffer)o );
		case TYPE_REAL:
			return putDouble( ((Number)o).doubleValue() );
		case TYPE_BYTE:
			return putByte( ((Number)o).byteValue() );
		case TYPE_SHORT:
			return putShort( ((Number)o).shortValue() );
		case TYPE_INTEGER:
			return putInteger( ((Number)o).intValue() );
		case TYPE_LONG:
			return putLong( ((Number)o).longValue() );
		case TYPE_INTER_GENERATOR:
			return block.getCodeIndex((InterCodeGenerator)o);
		case TYPE_UNKNOWN:
			return -1;
		}
		return -1;
	}
	public final ByteBuffer exportBuffer() {
		int size = 0;
		// string
		int stralllen = 0;
		int count = mString.size();
		for( int i = 0; i < count; i++ ) {
			int len = mString.get(i).length();
			len = ((len + 1) / 2) * 2;
			stralllen += len * 2;
		}
		stralllen = ((stralllen+3) / 4) * 4; // アライメント
		size += stralllen + count*4 + 4;

		// byte buffer
		int bytealllen = 0;
		count = mByteBuffer.size();
		for( int i = 0; i < count; i++ ) {
			int len = mByteBuffer.get(i).capacity();
			len = ((len+3)/4)*4;
			bytealllen += len;
		}
		bytealllen = ((bytealllen+3) / 4) * 4; // アライメント
		size += bytealllen + count*4 + 4;
		// byte
		count = mByte.size();
		count = ((count+3) / 4) * 4; // アライメント
		size += count + 4;

		// short
		count = mShort.size() * 2;
		count = ((count+3) / 4) * 4; // アライメント
		size += count + 4;

		// int
		size += mInteger.size()*4 + 4;

		// long
		size += mLong.size()*8 + 4;

		// double
		size += mDouble.size()*8 + 4;

		ByteBuffer buf = ByteBuffer.allocate(size);
		buf.order(ByteOrder.LITTLE_ENDIAN);
		buf.clear();

		// byte write
		count = mByte.size();
		buf.putInt(count);
		for( int i = 0; i < count; i++ ) {
			buf.put(mByte.get(i));
		}
		count = (((count+3) / 4) * 4) - count; // アライメント差分
		for( int i = 0; i < count; i++ ) {
			buf.put((byte)0);
		}

		// short write
		count = mShort.size();
		buf.putInt(count);
		for( int i = 0; i < count; i++ ) {
			buf.putShort( mShort.get(i) );
		}
		count *= 2;
		count = (((count+3) / 4) * 4) - count; // アライメント差分
		for( int i = 0; i < count; i++ ) {
			buf.put((byte)0);
		}

		// int write
		count = mInteger.size();
		buf.putInt(count);
		for( int i = 0; i < count; i++ ) {
			buf.putInt( mInteger.get(i) );
		}

		// long write
		count = mLong.size();
		buf.putInt(count);
		for( int i = 0; i < count; i++ ) {
			buf.putLong( mLong.get(i) );
		}

		// double write
		count = mDouble.size();
		buf.putInt(count);
		for( int i = 0; i < count; i++ ) {
			buf.putLong( Double.doubleToRawLongBits(mDouble.get(i)) );
		}

		// string write
		count = mString.size();
		buf.putInt(count);
		for( int i = 0; i < count; i++ ) {
			String str = mString.get(i);
			int len = str.length();
			buf.putInt(len);
			int s = 0;
			for( ; s < len; s++ ) {
				buf.putChar( str.charAt(s) );
			}
			if( (len % 2) == 1 ) { // アライメント差分
				buf.putChar( (char)0 );
			}
		}

		// byte buffer write
		count = mByteBuffer.size();
		buf.putInt(count);
		for( int i = 0; i < count; i++ ) {
			ByteBuffer by = mByteBuffer.get(i);
			int cap = by.capacity();
			buf.putInt(cap);
			for( int b = 0; b < cap; b++ ) {
				buf.put( by.get(b) );
			}
			cap = ((cap+3)/4)*4 - cap; // アライメント差分
			for( int b = 0; b < cap; b++ ) {
				buf.put( (byte)0 );
			}
		}
		buf.flip();
		return buf;
	}
}
