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
import java.nio.CharBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;


/**
 * TJS2 バイトコードを読み込んで、ScriptBlock を返す
 *
 */
public class ByteCodeLoader {

	private static final boolean LOAD_SRC_POS = false;

	public static final int FILE_TAG_LE = ('T') | ('J'<<8) | ('S'<<16) | ('2'<<24);
	public static final int VER_TAG_LE = ('1') | ('0'<<8) | ('0'<<16) | (0<<24);
	private static final int OBJ_TAG_LE = ('O') | ('B'<<8) | ('J'<<16) | ('S'<<24);
	private static final int DATA_TAG_LE = ('D') | ('A'<<8) | ('T'<<16) | ('A'<<24);

	private static final int TYPE_VOID = 0;
	private static final int TYPE_OBJECT = 1;
	private static final int TYPE_INTER_OBJECT = 2;
	private static final int TYPE_STRING = 3;
	private static final int TYPE_OCTET = 4;
	private static final int TYPE_REAL = 5;
	private static final int TYPE_BYTE = 6;
	private static final int TYPE_SHORT = 7;
	private static final int TYPE_INTEGER = 8;
	private static final int TYPE_LONG = 9;
	private static final int TYPE_INTER_GENERATOR = 10; // temporary
	private static final int TYPE_UNKNOWN = -1;

	static private byte[] mByteArray;
	static private short[] mShortArray;
	static private int[] mIntArray;
	static private long[] mLongArray;
	static private double[] mDoubleArray;
	static private long[] mDoubleTmpArray;
	static private String[] mStringArray;
	static private ByteBuffer[] mByteBufferArray;
	static private short[] mVariantTypeData;

	static private final int MIN_BYTE_COUNT = 64;
	static private final int MIN_SHORT_COUNT = 64;
	static private final int MIN_INT_COUNT = 64;
	static private final int MIN_DOUBLE_COUNT = 8;
	static private final int MIN_LONG_COUNT = 8;
	static private final int MIN_STRING_COUNT = 1024;

	//static private final int MIN_VARIANT_DATA_COUNT = 400*2;

	static private boolean mDeleteBuffer;
	static private byte[] mReadBuffer;
	static private final int MIN_READ_BUFFER_SIZE = 160 * 1024;

	static class ObjectsCache {
		public InterCodeObject[] mObjs;
		public ArrayList<VariantRepalace> mWork;
		public int[] mParent;
		public int[] mPropSetter;
		public int[] mPropGetter;
		public int[] mSuperClassGetter;
		public int[][] mProperties;

		private static final int MIN_COUNT = 500;
		public void create( int count ) {
			if( count < MIN_COUNT )
				count = MIN_COUNT;

			if( mWork == null ) mWork = new ArrayList<VariantRepalace>();
			mWork.clear();

			if( mObjs == null || mObjs.length < count ) {
				mObjs = new InterCodeObject[count];
				mParent = new int[count];
				mPropSetter = new int[count];
				mPropGetter = new int[count];
				mSuperClassGetter = new int[count];
				mProperties = new int[count][];
			}
		}
		public void release() {
			mWork = null;
			mObjs = null;
			mParent = null;
			mPropSetter = null;
			mPropGetter = null;
			mSuperClassGetter = null;
			mProperties = null;
		}
	}
	static private ObjectsCache mObjectsCache;
	static public void initialize() {
		mDeleteBuffer = false;
		mReadBuffer = null;
		mByteArray = null;
		mShortArray = null;
		mIntArray = null;
		mLongArray = null;
		mDoubleArray = null;
		mDoubleTmpArray = null;
		mStringArray = null;
		mByteBufferArray = null;
		mObjectsCache = new ObjectsCache();
		mVariantTypeData = null;
	}
	public static void finalizeApplication() {
		mDeleteBuffer = true;
		mReadBuffer = null;
		mByteArray = null;
		mShortArray = null;
		mIntArray = null;
		mLongArray = null;
		mDoubleArray = null;
		mDoubleTmpArray = null;
		mStringArray = null;
		mByteBufferArray = null;
		mObjectsCache = null;
		mVariantTypeData = null;
	}
	static public void allwaysFreeReadBuffer() {
		mDeleteBuffer = true;
		mReadBuffer = null;
		mByteArray = null;
		mShortArray = null;
		mIntArray = null;
		mLongArray = null;
		mDoubleArray = null;
		mDoubleTmpArray = null;
		mStringArray = null;
		mByteBufferArray = null;
		mObjectsCache.release();
		mVariantTypeData = null;
	}

	public ByteCodeLoader() {
	}

	public ScriptBlock readByteCode( TJS owner, String name, BinaryStream input ) throws TJSException {
		try {
			int size = (int) input.getSize();
			if( mReadBuffer == null || mReadBuffer.length < size) {
				int buflen = size < MIN_READ_BUFFER_SIZE ? MIN_READ_BUFFER_SIZE : size;
				mReadBuffer = new byte[buflen];
			}
			byte[] databuff = mReadBuffer;
			input.read(databuff);
			input.close();
			input  = null;
			// TJS2
			int tag = (databuff[0]&0xff) | (databuff[1]&0xff) << 8 | (databuff[2]&0xff) << 16 | (databuff[3]&0xff) << 24;
			if( tag != FILE_TAG_LE ) return null;
			// 100'\0'
			int ver = (databuff[4]&0xff) | (databuff[5]&0xff) << 8 | (databuff[6]&0xff) << 16 | (databuff[7]&0xff) << 24;
			if( ver != VER_TAG_LE ) return null;

			int filesize = (databuff[8]&0xff) | (databuff[9]&0xff) << 8 | (databuff[10]&0xff) << 16 | (databuff[11]&0xff) << 24;
			if( filesize != size ) return null;

			//// DATA
			tag = (databuff[12]&0xff) | (databuff[13]&0xff) << 8 | (databuff[14]&0xff) << 16 | (databuff[15]&0xff) << 24;
			if( tag != DATA_TAG_LE ) return null;
			size = (databuff[16]&0xff) | (databuff[17]&0xff) << 8 | (databuff[18]&0xff) << 16 | (databuff[19]&0xff) << 24;
			readDataArea( databuff, 20, size );

			int offset = 12 + size; // これがデータエリア後の位置
			// OBJS
			tag = (databuff[offset]&0xff) | (databuff[offset+1]&0xff) << 8 | (databuff[offset+2]&0xff) << 16 | (databuff[offset+3]&0xff) << 24;
			offset+=4;
			if( tag != OBJ_TAG_LE ) return null;
			//int objsize = ibuff.get();
			int objsize = (databuff[offset]&0xff) | (databuff[offset+1]&0xff) << 8 | (databuff[offset+2]&0xff) << 16 | (databuff[offset+3]&0xff) << 24;
			offset+=4;
			ScriptBlock block = new ScriptBlock(owner, name, 0, null, null );
			readObjects( block, databuff, offset, objsize );
			return block;
		} finally {
			if( mDeleteBuffer ) {
				mReadBuffer = null;
				mByteArray = null;
				mShortArray = null;
				mIntArray = null;
				mLongArray = null;
				mDoubleArray = null;
				mDoubleTmpArray = null;
				mStringArray = null;
				mByteBufferArray = null;
				mObjectsCache.release();
				mVariantTypeData = null;
			}
		}
	}

	/**
	 * InterCodeObject へ置換するために一時的に覚えておくクラス
	 */
	static class VariantRepalace {
		public Variant Work;
		public int Index;
		public VariantRepalace( Variant w, int i ) {
			Work = w;
			Index = i;
		}
	}

	private void readObjects( ScriptBlock block, byte[] buff, int offset, int size ) throws TJSException {
		String[] strarray = mStringArray;
		ByteBuffer[] bbarray = mByteBufferArray;
		double[] dblarray = mDoubleArray;
		byte[] barray = mByteArray;
		short[] sarray = mShortArray;
		int[] iarray = mIntArray;
		long[] larray = mLongArray;

		int toplevel = (buff[offset]&0xff) | (buff[offset+1]&0xff) << 8 | (buff[offset+2]&0xff) << 16 | (buff[offset+3]&0xff) << 24;
		offset += 4;
		int objcount = (buff[offset]&0xff) | (buff[offset+1]&0xff) << 8 | (buff[offset+2]&0xff) << 16 | (buff[offset+3]&0xff) << 24;
		offset += 4;

		//Log.v("test","count:"+objcount);
		mObjectsCache.create(objcount);
		InterCodeObject[] objs = mObjectsCache.mObjs;
		ArrayList<VariantRepalace> work = mObjectsCache.mWork;
		int[] parent = mObjectsCache.mParent;
		int[] propSetter = mObjectsCache.mPropSetter;
		int[] propGetter = mObjectsCache.mPropGetter;
		int[] superClassGetter = mObjectsCache.mSuperClassGetter;
		int[][] properties = mObjectsCache.mProperties;
		for( int o = 0; o < objcount; o++ ) {
			int tag = (buff[offset]&0xff) | (buff[offset+1]&0xff) << 8 | (buff[offset+2]&0xff) << 16 | (buff[offset+3]&0xff) << 24;
			offset += 4;
			if( tag != FILE_TAG_LE ) {
				throw new TJSException(Error.ByteCodeBroken);
			}
			//int objsize = (buff[offset]&0xff) | (buff[offset+1]&0xff) << 8 | (buff[offset+2]&0xff) << 16 | (buff[offset+3]&0xff) << 24;
			offset += 4;
			parent[o]  = (buff[offset]&0xff) | (buff[offset+1]&0xff) << 8 | (buff[offset+2]&0xff) << 16 | (buff[offset+3]&0xff) << 24;
			offset += 4;
			int name  = (buff[offset]&0xff) | (buff[offset+1]&0xff) << 8 | (buff[offset+2]&0xff) << 16 | (buff[offset+3]&0xff) << 24;
			offset += 4;
			int contextType = (buff[offset]&0xff) | (buff[offset+1]&0xff) << 8 | (buff[offset+2]&0xff) << 16 | (buff[offset+3]&0xff) << 24;
			offset += 4;
			int maxVariableCount = (buff[offset]&0xff) | (buff[offset+1]&0xff) << 8 | (buff[offset+2]&0xff) << 16 | (buff[offset+3]&0xff) << 24;
			offset += 4;
			int variableReserveCount = (buff[offset]&0xff) | (buff[offset+1]&0xff) << 8 | (buff[offset+2]&0xff) << 16 | (buff[offset+3]&0xff) << 24;
			offset += 4;
			int maxFrameCount = (buff[offset]&0xff) | (buff[offset+1]&0xff) << 8 | (buff[offset+2]&0xff) << 16 | (buff[offset+3]&0xff) << 24;
			offset += 4;
			int funcDeclArgCount = (buff[offset]&0xff) | (buff[offset+1]&0xff) << 8 | (buff[offset+2]&0xff) << 16 | (buff[offset+3]&0xff) << 24;
			offset += 4;
			int funcDeclUnnamedArgArrayBase = (buff[offset]&0xff) | (buff[offset+1]&0xff) << 8 | (buff[offset+2]&0xff) << 16 | (buff[offset+3]&0xff) << 24;
			offset += 4;
			int funcDeclCollapseBase = (buff[offset]&0xff) | (buff[offset+1]&0xff) << 8 | (buff[offset+2]&0xff) << 16 | (buff[offset+3]&0xff) << 24;
			offset += 4;
			propSetter[o] = (buff[offset]&0xff) | (buff[offset+1]&0xff) << 8 | (buff[offset+2]&0xff) << 16 | (buff[offset+3]&0xff) << 24;
			offset += 4;
			propGetter[o] = (buff[offset]&0xff) | (buff[offset+1]&0xff) << 8 | (buff[offset+2]&0xff) << 16 | (buff[offset+3]&0xff) << 24;
			offset += 4;
			superClassGetter[o] = (buff[offset]&0xff) | (buff[offset+1]&0xff) << 8 | (buff[offset+2]&0xff) << 16 | (buff[offset+3]&0xff) << 24;
			offset += 4;

			int count = (buff[offset]&0xff) | (buff[offset+1]&0xff) << 8 | (buff[offset+2]&0xff) << 16 | (buff[offset+3]&0xff) << 24;
			offset += 4;

			LongBuffer srcpos;
			if( LOAD_SRC_POS ) {
				int[] codePos = new int[count];
				int[] srcPos = new int[count];
				for( int i = 0; i < count; i++ ) {
					codePos[i] = (buff[offset]&0xff) | (buff[offset+1]&0xff) << 8 | (buff[offset+2]&0xff) << 16 | (buff[offset+3]&0xff) << 24;
					offset += 4;
				}
				for( int i = 0; i < count; i++ ) {
					srcPos[i] = (buff[offset]&0xff) | (buff[offset+1]&0xff) << 8 | (buff[offset+2]&0xff) << 16 | (buff[offset+3]&0xff) << 24;
					offset += 4;
				}
				// codePos/srcPos は今のところ使ってない、ソート済みなので、longにする必要はないが……
				ByteBuffer code2srcpos = ByteBuffer.allocate(count<<3);
				code2srcpos.order(ByteOrder.LITTLE_ENDIAN);
				srcpos = code2srcpos.asLongBuffer();
				for( int i = 0; i < count; i++ ) {
					srcpos.put( ((long)(codePos[i]) << 32) | (long)(srcPos[i]) );
				}
				srcpos.flip();
			} else {
				offset += count << 3;
				srcpos = null;
			}

			count = (buff[offset]&0xff) | (buff[offset+1]&0xff) << 8 | (buff[offset+2]&0xff) << 16 | (buff[offset+3]&0xff) << 24;
			offset += 4;
			short[] code = new short[count];
			for( int i = 0; i < count; i++ ) {
				code[i] = (short) ((buff[offset]&0xff) | (buff[offset+1]&0xff) << 8);
				offset += 2;
			}
			offset += (count & 1) << 1;

			count = (buff[offset]&0xff) | (buff[offset+1]&0xff) << 8 | (buff[offset+2]&0xff) << 16 | (buff[offset+3]&0xff) << 24;
			offset += 4;
			int vcount = count<<1;
			if( mVariantTypeData == null || mVariantTypeData.length < vcount ) {
				mVariantTypeData = new short[vcount];
			}
			short[] data = mVariantTypeData;
			for( int i = 0; i < vcount; i++ ) {
				data[i] = (short) ((buff[offset]&0xff) | (buff[offset+1]&0xff) << 8);
				offset += 2;
			}

			Variant[] vdata = new Variant[count];
			int datacount = count;
			Variant tmp;
			for( int i = 0; i < datacount; i++ ) {
				int pos = i << 1;
				int type = data[pos];
				int index = data[pos+1];
				switch( type ) {
				case TYPE_VOID:
					vdata[i] = new Variant(); // null
					break;
				case TYPE_OBJECT:
					vdata[i] = new Variant(null,null); // null Array Dictionary はまだサポートしていない TODO
					break;
				case TYPE_INTER_OBJECT:
					tmp = new Variant();
					work.add( new VariantRepalace( tmp, index ) );
					vdata[i] = tmp;
					break;
				case TYPE_INTER_GENERATOR:
					tmp = new Variant();
					work.add( new VariantRepalace( tmp, index ) );
					vdata[i] = tmp;
					break;
				case TYPE_STRING:
					vdata[i] = new Variant( strarray[index] );
					break;
				case TYPE_OCTET:
					vdata[i] = new Variant( bbarray[index] );
					break;
				case TYPE_REAL:
					vdata[i] = new Variant( dblarray[index] );
					break;
				case TYPE_BYTE:
					vdata[i] = new Variant( barray[index] );
					break;
				case TYPE_SHORT:
					vdata[i] = new Variant( sarray[index] );
					break;
				case TYPE_INTEGER:
					vdata[i] = new Variant( iarray[index] );
					break;
				case TYPE_LONG:
					vdata[i] = new Variant( larray[index] );
					break;
				case TYPE_UNKNOWN:
				default:
					vdata[i] = new Variant(); // null;
					break;
				}
			}
			count = (buff[offset]&0xff) | (buff[offset+1]&0xff) << 8 | (buff[offset+2]&0xff) << 16 | (buff[offset+3]&0xff) << 24;
			offset += 4;
			int[] scgetterps = new int[count];
			for( int i = 0; i < count; i++ ) {
				scgetterps[i] = (buff[offset]&0xff) | (buff[offset+1]&0xff) << 8 | (buff[offset+2]&0xff) << 16 | (buff[offset+3]&0xff) << 24;
				offset += 4;
			}
			// properties
			count = (buff[offset]&0xff) | (buff[offset+1]&0xff) << 8 | (buff[offset+2]&0xff) << 16 | (buff[offset+3]&0xff) << 24;
			offset += 4;
			if( count > 0 ) {
				int pcount = count << 1;
				int props[] = new int[pcount];
				for( int i = 0; i < pcount; i++ ) {
					props[i] = (buff[offset]&0xff) | (buff[offset+1]&0xff) << 8 | (buff[offset+2]&0xff) << 16 | (buff[offset+3]&0xff) << 24;
					offset += 4;
				}
				properties[o] = props;
			}

			//IntVector superpointer = IntVector.wrap( scgetterps );
			InterCodeObject obj = new InterCodeObject( block, mStringArray[name], contextType, code, vdata, maxVariableCount, variableReserveCount,
					maxFrameCount, funcDeclArgCount, funcDeclUnnamedArgArrayBase, funcDeclCollapseBase, true, srcpos, scgetterps );
			//objs.add(obj);
			objs[o] = obj;
		}
		Variant val = new Variant();
		for( int o = 0; o < objcount; o++ ) {
			InterCodeObject parentObj = null;
			InterCodeObject propSetterObj = null;
			InterCodeObject propGetterObj = null;
			InterCodeObject superClassGetterObj = null;

			if( parent[o] >= 0 ) {
				parentObj = objs[parent[o]];
			}
			if( propSetter[o] >= 0 ) {
				propSetterObj = objs[propSetter[o]];
			}
			if( propGetter[o] >= 0 ) {
				propGetterObj = objs[propGetter[o]];
			}
			if( superClassGetter[o] >= 0 ) {
				superClassGetterObj = objs[superClassGetter[o]];
			}
			objs[o].setCodeObject(parentObj, propSetterObj, propGetterObj, superClassGetterObj );
			if( properties[o] != null ) {
				InterCodeObject obj = parentObj; // objs.get(o).mParent;
				int[] prop = properties[o];
				int length = prop.length >>> 1;
				for( int i = 0; i < length; i++ ) {
					int pos = i << 1;
					int pname = prop[pos];
					int pobj = prop[pos+1];
					val.set( objs[pobj] );
					obj.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, mStringArray[pname], val, obj );
				}
				properties[o] = null;
			}
		}
		int count = work.size();
		for( int i = 0; i < count; i++ ) {
			VariantRepalace w = work.get(i);
			w.Work.set( objs[w.Index] );
		}
		work.clear();
		InterCodeObject top = null;
		if( toplevel >= 0 ) {
			top = objs[toplevel];
		}
		block.setObjects( top, objs, objcount );
	}

	private void readDataArea( byte[] buff, int offset, int size ) {
		int count = (buff[offset]&0xff) | (buff[offset+1]&0xff) << 8 | (buff[offset+2]&0xff) << 16 | (buff[offset+3]&0xff) << 24;
		offset += 4;
		if( count > 0 ) {
			if( mByteArray == null || mByteArray.length < count ) {
				int c = count < MIN_BYTE_COUNT ? MIN_BYTE_COUNT : count;
				mByteArray = new byte[c];
			}
			System.arraycopy(buff, offset, mByteArray, 0, count );
			int stride = ( count + 3 ) >>> 2;
			offset += stride << 2;
		}
		count = (buff[offset]&0xff) | (buff[offset+1]&0xff) << 8 | (buff[offset+2]&0xff) << 16 | (buff[offset+3]&0xff) << 24;
		offset += 4;
		if( count > 0 ) {	// load short
			if( mShortArray == null || mShortArray.length < count ) {
				int c = count < MIN_SHORT_COUNT ? MIN_SHORT_COUNT : count;
				mShortArray = new short[c];
			}
			short[] tmp = mShortArray;
			for( int i = 0; i < count; i++ ) {
				tmp[i] = (short) ((buff[offset]&0xff) | (buff[offset+1]&0xff) << 8);
				offset += 2;
			}
			offset += (count & 1) << 1;
		}
		count = (buff[offset]&0xff) | (buff[offset+1]&0xff) << 8 | (buff[offset+2]&0xff) << 16 | (buff[offset+3]&0xff) << 24;
		offset += 4;
		if( count > 0 ) {
			if( mIntArray == null || mIntArray.length < count ) {
				int c = count < MIN_INT_COUNT ? MIN_INT_COUNT : count;
				mIntArray = new int[c];
			}
			int[] tmp = mIntArray;
			for( int i = 0; i < count; i++ ) {
				tmp[i] = (buff[offset]&0xff) | (buff[offset+1]&0xff) << 8 | (buff[offset+2]&0xff) << 16 | (buff[offset+3]&0xff) << 24;
				offset += 4;
			}
		}
		count = (buff[offset]&0xff) | (buff[offset+1]&0xff) << 8 | (buff[offset+2]&0xff) << 16 | (buff[offset+3]&0xff) << 24;
		offset += 4;
		if( count > 0 ) {	// load long
			if( mLongArray == null || mLongArray.length < count ) {
				int c = count < MIN_LONG_COUNT ? MIN_LONG_COUNT : count;
				mLongArray = new long[c];
			}
			long[] tmp = mLongArray;
			for( int i = 0; i < count; i++ ) {
				tmp[i] = (long)(buff[offset]&0xff) | (long)(buff[offset+1]&0xff) << 8 | (long)(buff[offset+2]&0xff) << 16 | (long)(buff[offset+3]&0xff) << 24
							| (long)(buff[offset+4]&0xff) << 32 | (long)(buff[offset+5]&0xff) << 40 | (long)(buff[offset+6]&0xff) << 48 | (long)(buff[offset+7]&0xff) << 56;
				offset += 8;
			}
		}
		count = (buff[offset]&0xff) | (buff[offset+1]&0xff) << 8 | (buff[offset+2]&0xff) << 16 | (buff[offset+3]&0xff) << 24;
		offset += 4;
		if( count > 0 ) {	// load double
			if( mDoubleArray == null || mDoubleArray.length < count ) {
				int c = count < MIN_DOUBLE_COUNT ? MIN_DOUBLE_COUNT : count;
				mDoubleArray = new double[c];
			}
			if( mDoubleTmpArray == null || mDoubleTmpArray.length < count ) {
				int c = count < MIN_DOUBLE_COUNT ? MIN_DOUBLE_COUNT : count;
				mDoubleTmpArray = new long[c];
			}
			long[] tmp = mDoubleTmpArray;
			for( int i = 0; i < count; i++ ) {
				tmp[i] = (long)(buff[offset]&0xff) | (long)(buff[offset+1]&0xff) << 8 | (long)(buff[offset+2]&0xff) << 16 | (long)(buff[offset+3]&0xff) << 24
							| (long)(buff[offset+4]&0xff) << 32 | (long)(buff[offset+5]&0xff) << 40 | (long)(buff[offset+6]&0xff) << 48 | (long)(buff[offset+7]&0xff) << 56;
				offset += 8;
			}
			for( int i = 0; i < count; i++ ) {
				mDoubleArray[i] = Double.longBitsToDouble(tmp[i]);
			}
		}
		count = (buff[offset]&0xff) | (buff[offset+1]&0xff) << 8 | (buff[offset+2]&0xff) << 16 | (buff[offset+3]&0xff) << 24;
		offset += 4;
		if( count > 0 ) {
			if( mStringArray == null || mStringArray.length < count ) {
				int c = count < MIN_STRING_COUNT ? MIN_STRING_COUNT : count;
				mStringArray = new String[c];
			}
			for( int i = 0; i < count; i++ ) {
				int len = (buff[offset]&0xff) | (buff[offset+1]&0xff) << 8 | (buff[offset+2]&0xff) << 16 | (buff[offset+3]&0xff) << 24;
				offset += 4;
				char[] ch = new char[len];
				for( int j = 0; j < len; j++ ) {
					ch[j] = (char) ((buff[offset]&0xff) | (buff[offset+1]&0xff) << 8);
					offset += 2;
				}
				mStringArray[i] = TJS.mapGlobalStringMap( new String(ch) );
				offset += (len & 1) << 1;
			}
		}
		count = (buff[offset]&0xff) | (buff[offset+1]&0xff) << 8 | (buff[offset+2]&0xff) << 16 | (buff[offset+3]&0xff) << 24;
		offset += 4;
		if( count > 0 ) {
			if( mByteBufferArray == null || mByteBufferArray.length < count )
				mByteBufferArray = new ByteBuffer[count];
			for( int i = 0; i < count; i++ ) {
				int len = (buff[offset]&0xff) | (buff[offset+1]&0xff) << 8 | (buff[offset+2]&0xff) << 16 | (buff[offset+3]&0xff) << 24;
				offset += 4;
				byte[] tmp = new byte[len];
				System.arraycopy(buff, offset, tmp, 0, len );
				mByteBufferArray[i] = ByteBuffer.wrap(tmp);
				mByteBufferArray[i].position(len);
				offset += (( len + 3 ) >>> 2) << 2;
			}
		}
	}
}
