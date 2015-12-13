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
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

public class ArrayClass extends NativeClass {
	static public int ClassID = -1;
	static private final String CLASS_NAME = "Array";

	protected NativeInstance createNativeInstance() {
		return new ArrayNI(); // return a native object instance.
	}
	protected Dispatch2 createBaseTJSObject() {
		return new ArrayObject();
	}
	public static int getArrayElementCount(Dispatch2 dsp ) throws TJSException {
		// returns array element count
		ArrayNI ni = (ArrayNI)dsp.getNativeInstance(ClassID);
		if( ni == null ) throw new TJSException(Error.SpecifyArray);
		return ni.mItems.size();
	}

	public static int copyArrayElementTo( Dispatch2 dsp, Variant[] dest, int dest_offset, int start, int count ) throws TJSException {
		// copy array elements to specified variant array.
		// returns copied element count.
		ArrayNI ni = (ArrayNI)dsp.getNativeInstance(ClassID);
		if( ni == null ) throw new TJSException(Error.SpecifyArray);

		if(count < 0) count = ni.mItems.size();

		if(start >= ni.mItems.size()) return 0;

		int limit = start + count;

		int d = dest_offset;
		for( int i = start; i < limit; i++ ) {
			dest[d] = ni.mItems.get(i);
			d++;
		}

		return limit - start;
	}

	/*
	public static ArrayNI getNativeInstance( Dispatch2 objthis ) {
		Holder<NativeInstance> holder = new Holder<NativeInstance>(null);
		int hr = objthis.nativeInstanceSupport( NIS_GETINSTANCE, mClassID, holder );
		if( hr < 0 ) return null;
		return (ArrayNI) holder.mValue;
	}
	*/

	public ArrayClass() throws VariantException, TJSException {
		super(CLASS_NAME);
		final int NCM_CLASSID = TJS.registerNativeClass(CLASS_NAME);
		setClassID( NCM_CLASSID );
		ClassID = NCM_CLASSID;

		// constructor
		registerNCM( CLASS_NAME, new NativeClassConstructor() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) {
				ArrayNI _this = (ArrayNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				int hr = _this.construct( param, objthis );
				if( hr < 0 ) return hr;
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );


		registerNCM( "load", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				if( objthis == null ) return Error.E_NATIVECLASSCRASH;
				if( TJS.mStorage == null ) return Error.E_NATIVECLASSCRASH;

				ArrayNI ni = (ArrayNI)objthis.getNativeInstance(ClassID);	// インスタンス所得
				if( ni == null ) return Error.E_NATIVECLASSCRASH;

				if( param.length < 1 ) return Error.E_BADPARAMCOUNT;

				String name = param[0].asString();
				String mode = null;
				if( param.length >= 2 && param[1].isVoid() == false ) mode = param[1].asString();

				ni.mItems.clear();
				String content = TJS.mStorage.readText(name, mode);
				final int count = content.length();
				int lines = 0;
				int reamain = 0;
				for( int i = 0; i < count; i++ ) {
					reamain++;
					int ch = content.codePointAt(i);
					if( ch == '\r' || ch == '\n' ) {
						if( (i+1) < count ) {
							if( content.codePointAt(i+1) == '\n' && ch == '\r' ) {
								lines++;
								i++;
							}
						}
						reamain = 0;
					}
				}
				if( reamain > 0 ) {
					lines++;
				}
				ni.mItems.clear();
				ni.mItems.ensureCapacity(lines);

				// split to each line
				lines = 0;

				int start = 0;
				for( int i = 0; i < count; i++ ) {
					int ch = content.codePointAt(i);
					if( ch == '\r' || ch == '\n' ) {
						ni.mItems.add( new Variant(content.substring(start, i)) );
						if( (i+1) < count ) {
							if( content.codePointAt(i+1) == '\n' && ch == '\r' ) {
								i++;
							}
						}
						start = i + 1;
					}
				}
				if( start < count ) {
					ni.mItems.add( new Variant(content.substring(start, count)) );
				}

				if( result != null ) result.set( new Variant(objthis, objthis) );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "save", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				// saves the array into a file.
				// only string and number stuffs are stored.

				ArrayNI ni = (ArrayNI)objthis.getNativeInstance(ClassID);	// インスタンス所得
				if( ni == null ) return Error.E_NATIVECLASSCRASH;

				if( param.length < 1 ) return Error.E_BADPARAMCOUNT;

				String name = param[0].asString();
				String mode = null;
				if( param.length >= 2 && param[1].isVoid() == false ) mode = param[1].asString();
				TextWriteStreamInterface stream = TJS.mStorage.createTextWriteStream(name, mode);
				try {
					Iterator<Variant> i = ni.mItems.iterator();
					while( i.hasNext() ) {
						Variant o = i.next();
						if( o != null && (o.isString() || o.isNumber() ) ) {
							stream.write( o.asString() );
						}
						stream.write( "\n" );
			        }
				} finally {
					stream.destruct();
				}

				if( result != null ) result.set( new Variant(objthis, objthis) );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "saveStruct", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				// saves the array into a file, that can be interpret as an expression.

				ArrayNI ni = (ArrayNI)objthis.getNativeInstance(ClassID);	// インスタンス所得
				if( ni == null ) return Error.E_NATIVECLASSCRASH;

				if( param.length < 1 ) return Error.E_BADPARAMCOUNT;

				String name = param[0].asString();
				String mode = null;
				if( param.length >= 2 && param[1].isVoid() == false ) mode = param[1].asString();
				TextWriteStreamInterface stream = TJS.mStorage.createTextWriteStream(name, mode);
				try {
					ArrayList<Dispatch2> stack = new ArrayList<Dispatch2>();
					stack.add(objthis);
					ni.saveStructuredData(stack, stream, "" );
				} finally {
					stream.destruct();
				}

				if( result != null ) result.set( new Variant(objthis, objthis) );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "split", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				// split string with given delimiters.

				// arguments are : <pattern/delimiter>, <string>, [<reserved>],
				// [<whether ignore empty element>]

				ArrayNI ni = (ArrayNI)objthis.getNativeInstance(ClassID);	// インスタンス取得
				if( ni == null ) return Error.E_NATIVECLASSCRASH;

				if( param.length < 2 ) return Error.E_BADPARAMCOUNT;

				ni.mItems.clear();
				String string = param[1].asString();
				boolean purgeempty = false;
				if( param.length >= 4 && param[3].isVoid() == false ) {
					purgeempty = param[3].asBoolean();
				}
				// Enable REGEX
				if( param[0].isObject() ) {
					RegExpNI re;
					VariantClosure clo = param[0].asObjectClosure();
					if( clo.mObject != null ) {
						if( (re = (RegExpNI)clo.mObject.getNativeInstance(RegExpClass.mClassID)) != null ) {
							// param[0] is regexp
							Holder<Dispatch2> array = new Holder<Dispatch2>(objthis);
							re.split( array, string, purgeempty );
							if( result != null ) result.set( new Variant(objthis, objthis) );
							return Error.S_OK;
						}
					}
				}
				String pattern = param[0].asString();
				final int count = string.length();
				int start = 0;
				for( int i = 0; i < count; i++ ) {
					int ch = string.codePointAt(i);
					if( pattern.indexOf(ch) != -1 ) {
						// delimiter found
						if( purgeempty == false || (purgeempty==true && (i-start) != 0) ) {
							ni.mItems.add(  new Variant(string.substring(start,i)) );
						}
						start = i+1;
					}
				}
				if( purgeempty == false || (purgeempty==true && (count-start) >= 0) ) {
					ni.mItems.add( new Variant(string.substring(start,count)));
				}
				if( result != null ) result.set( new Variant(objthis, objthis) );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "join", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				// join string with given delimiters.
				// arguments are : <delimiter>, [<reserved>],
				// [<whether ignore empty element>]

				ArrayNI ni = (ArrayNI)objthis.getNativeInstance(ClassID);	// インスタンス所得
				if( ni == null ) return Error.E_NATIVECLASSCRASH;

				if( param.length < 1 ) return Error.E_BADPARAMCOUNT;

				String delimiter = param[0].asString();
				boolean purgeempty = false;
				if( param.length >= 3 && param[2].isVoid() != true  ) purgeempty = param[2].asBoolean();

				// join with delimiter
				boolean first = true;
				StringBuilder builer = new StringBuilder(1024);
				final int count = ni.mItems.size();
				for( int i = 0; i < count; i++ ) {
					Variant v = ni.mItems.get(i);
					if( purgeempty && v.isVoid() ) {
					} else {
						if(!first) builer.append(delimiter);
						first = false;
						builer.append(v.asString());
					}
				}
				if( result != null ) result.set( builer.toString() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "sort", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				// sort array items.

				// arguments are : [<sort order/comparison function>], [<whether to do stable sort>]

				// sort order is one of:
				// '+' (default)   :  Normal ascending  (comparison by tTJSVariant::operator < )
				// '-'             :  Normal descending (comparison by tTJSVariant::operator < )
				// '0'             :  Numeric value ascending
				// '9'             :  Numeric value descending
				// 'a'             :  String ascending
				// 'z'             :  String descending

				ArrayNI ni = (ArrayNI)objthis.getNativeInstance(ClassID);	// インスタンス所得
				if( ni == null ) return Error.E_NATIVECLASSCRASH;

				char method = '+';
				//boolean do_stable_sort = false;
				VariantClosure closure = null;

				if( param.length >= 1 && param[0].isVoid() != true ) {
					// check first argument
					if( param[0].isObject() ) {
						// comarison function object
						closure = param[0].asObjectClosure();
						method = 0;
					} else {
						// sort order letter
						String me = param[0].asString();
						method = me.charAt(0);
						switch(method)
						{
						case '+':
						case '-':
						case '0':
						case '9':
						case 'a':
						case 'z':
							break;
						default:
							method = '+';
							break;
						}
					}
				}

				// Collections.sortが常にstable_sortなのでこれは意味がない
				/*
				if( param.length >= 2 && param[1].isVoid() != true ) {
					// whether to do a stable sort
					do_stable_sort = param[1].asBoolean();
				}
				*/

				// sort
				switch(method)
				{
				case '+':
					Collections.sort( ni.mItems, new ArraySortCompare_NormalAscending() );
					break;
				case '-':
					Collections.sort( ni.mItems, new ArraySortCompare_NormalDescending() );
					break;
				case '0':
					Collections.sort( ni.mItems, new ArraySortCompare_NumericAscending() );
					break;
				case '9':
					Collections.sort( ni.mItems, new ArraySortCompare_NumericDescending() );
					break;
				case 'a':
					Collections.sort( ni.mItems, new ArraySortCompare_StringAscending() );
					break;
				case 'z':
					Collections.sort( ni.mItems, new ArraySortCompare_StringDescending() );
					break;
				case 0:
					Collections.sort( ni.mItems, new ArraySortCompare_Functional(closure) );
					break;
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "reverse", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				ArrayNI ni = (ArrayNI)objthis.getNativeInstance(ClassID);	// インスタンス所得
				if( ni == null ) return Error.E_NATIVECLASSCRASH;
				Collections.reverse(ni.mItems);
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "assign", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				ArrayNI ni = (ArrayNI)objthis.getNativeInstance(ClassID);	// インスタンス所得
				if( ni == null ) return Error.E_NATIVECLASSCRASH;
				if( param.length < 1) return Error.E_BADPARAMCOUNT;
				((ArrayObject)objthis).clear(ni);
				VariantClosure clo = param[0].asObjectClosure();
				if( clo.mObjThis != null )
					ni.assign( clo.mObjThis );
				else if( clo.mObject != null )
					ni.assign(clo.mObject);
				else throw new TJSException(Error.NullAccess);

				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "assignStruct", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				ArrayNI ni = (ArrayNI)objthis.getNativeInstance(ClassID);	// インスタンス所得
				if( ni == null ) return Error.E_NATIVECLASSCRASH;
				if( param.length < 1) return Error.E_BADPARAMCOUNT;
				((ArrayObject)objthis).clear(ni);
				ArrayList<Dispatch2> stack = new ArrayList<Dispatch2>();

				VariantClosure clo = param[0].asObjectClosure();
				if( clo.mObjThis != null )
					ni.assignStructure(clo.mObjThis, stack);
				else if( clo.mObject != null )
					ni.assignStructure(clo.mObject, stack);
				else throw new TJSException(Error.NullAccess);

				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "clear", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				ArrayNI ni = (ArrayNI)objthis.getNativeInstance(ClassID);	// インスタンス所得
				((ArrayObject)objthis).clear(ni);
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "erase", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				// remove specified item number from the array
				ArrayNI ni = (ArrayNI)objthis.getNativeInstance(ClassID);	// インスタンス所得
				if( ni == null ) return Error.E_NATIVECLASSCRASH;
				if(param.length < 1) return Error.E_BADPARAMCOUNT;
				int num = param[0].asInteger();
				((ArrayObject)objthis).erase(ni, num);
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "remove", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				// remove specified item from the array wchich appears first or all
				ArrayNI ni = (ArrayNI)objthis.getNativeInstance(ClassID);	// インスタンス所得
				if( ni == null ) return Error.E_NATIVECLASSCRASH;
				if( param.length < 1) return Error.E_BADPARAMCOUNT;

				boolean eraseall;
				if( param.length >= 2)
					eraseall = param[1].asBoolean();
				else
					eraseall = true;

				Variant val = param[0];
				int count = ((ArrayObject)objthis).remove(ni, val, eraseall);
				if( result != null ) result.set( count );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "insert", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				// insert item at specified position
				ArrayNI ni = (ArrayNI)objthis.getNativeInstance(ClassID);	// インスタンス所得
				if( ni == null ) return Error.E_NATIVECLASSCRASH;
				if( param.length < 2) return Error.E_BADPARAMCOUNT;
				int num = param[0].asInteger();
				((ArrayObject)objthis).insert(ni, param[1], num);
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "add", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				// add item at last
				ArrayNI ni = (ArrayNI)objthis.getNativeInstance(ClassID);		// インスタンス所得
				if( ni == null ) return Error.E_NATIVECLASSCRASH;
				if( param.length < 1) return Error.E_BADPARAMCOUNT;
				((ArrayObject)objthis).add( ni, param[0] );
				if( result != null ) result.set( ni.mItems.size() - 1 );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "push", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				// add item(s) at last
				ArrayNI ni = (ArrayNI)objthis.getNativeInstance(ClassID);	// インスタンス所得
				if( ni == null ) return Error.E_NATIVECLASSCRASH;
				((ArrayObject)objthis).insert(ni, param, ni.mItems.size());
				if(result != null) result.set( ni.mItems.size() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "pop", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				// pop item from last
				ArrayNI ni = (ArrayNI)objthis.getNativeInstance(ClassID);	// インスタンス所得
				if( ni == null ) return Error.E_NATIVECLASSCRASH;
				if( ni.mItems.isEmpty() ) {
					if( result != null ) result.clear();
				} else {
					if( result != null ) result.set( ni.mItems.get(ni.mItems.size() - 1) );
					((ArrayObject)objthis).erase(ni, ni.mItems.size() - 1);
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "shift", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				// shift item at head
				ArrayNI ni = (ArrayNI)objthis.getNativeInstance(ClassID);	// インスタンス所得
				if( ni == null ) return Error.E_NATIVECLASSCRASH;
				if( ni.mItems.isEmpty() ) {
					if( result != null ) result.clear();
				} else {
					if( result != null ) result.set( ni.mItems.get(0) );
					((ArrayObject)objthis).erase(ni, 0);
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "unshift", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				// add item(s) at head
				ArrayNI ni = (ArrayNI)objthis.getNativeInstance(ClassID);	// インスタンス所得
				if( ni == null ) return Error.E_NATIVECLASSCRASH;
				((ArrayObject)objthis).insert(ni, param, 0);
				if( result != null ) result.set( ni.mItems.size() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "find", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				// find item in the array,
				// return an index which points the item that appears first.
				ArrayNI ni = (ArrayNI)objthis.getNativeInstance(ClassID);	// インスタンス所得
				if( ni == null ) return Error.E_NATIVECLASSCRASH;
				if( param.length < 1 ) return Error.E_BADPARAMCOUNT;
				if( result != null ) {
					Variant val = param[0];
					int start = 0;
					if( param.length >= 2) start = param[1].asInteger();
					if(start < 0) start += ni.mItems.size();
					if(start < 0) start = 0;
					if(start >= ni.mItems.size()) { result.set( -1 ); return Error.S_OK; }

					final int count = ni.mItems.size();
					int i;
					for( i = start; i < count; i++ ) {
						Variant v = ni.mItems.get(i);
						if( val.discernCompareInternal(v) ) break;
					}
					if(i == count )
						result.set( -1 );
					else
						result.set( i );
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "count", new NativeClassProperty() {
			@Override
			public int get(Variant result, Dispatch2 objthis) {
				ArrayNI ni = (ArrayNI)objthis.getNativeInstance(ClassID);	// インスタンス所得
				if( ni == null ) return Error.E_NATIVECLASSCRASH;
				if( result != null ) result.set( ni.mItems.size() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				ArrayNI ni = (ArrayNI)objthis.getNativeInstance(ClassID);	// インスタンス所得
				if( ni == null ) return Error.E_NATIVECLASSCRASH;
				int resize = param.asInteger();
				int count = ni.mItems.size();
				if( count < resize ) {
					int addcount = resize - count;
					ni.mItems.ensureCapacity(count);
					for( int i = 0; i < addcount; i++ ) {
						ni.mItems.add( new Variant() );
					}
				} else if( count > resize ) {
					for( int i = count-1; i >= resize; i-- ) {
						ni.mItems.remove( i );
					}
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "length", new NativeClassProperty() {
			@Override
			public int get(Variant result, Dispatch2 objthis) {
				ArrayNI ni = (ArrayNI)objthis.getNativeInstance(ClassID);	// インスタンス所得
				if( ni == null ) return Error.E_NATIVECLASSCRASH;
				if( result != null ) result.set( ni.mItems.size() );
				return Error.S_OK;
			}
			@Override
			public int set(Variant param, Dispatch2 objthis) throws VariantException {
				ArrayNI ni = (ArrayNI)objthis.getNativeInstance(ClassID);	// インスタンス所得
				if( ni == null ) return Error.E_NATIVECLASSCRASH;
				int resize = param.asInteger();
				int count = ni.mItems.size();
				if( count < resize ) {
					int addcount = resize - count;
					ni.mItems.ensureCapacity(count);
					for( int i = 0; i < addcount; i++ ) {
						ni.mItems.add( new Variant() );
					}
				} else if( count > resize ) {
					for( int i = count-1; i >= resize; i-- ) {
						ni.mItems.remove( i );
					}
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		//ArrayNI.ClassID_Array = NCM_CLASSID;
	}

	public static class ArraySortCompare_NormalAscending implements Comparator<Variant> {
		@Override
		public int compare(Variant lhs, Variant rhs) {
			try {
				return lhs.greaterThanForSort( rhs );
			} catch (VariantException e ) {
				return 0;
			}
		}
    }
	public static class ArraySortCompare_NormalDescending implements Comparator<Variant> {
		@Override
		public int compare(Variant lhs, Variant rhs) {
			try {
				return lhs.littlerThanForSort( rhs );
			} catch (VariantException e ) {
				return 0;
			}
		}
    }
	public static class ArraySortCompare_NumericAscending implements Comparator<Variant> {
		@Override
		public int compare(Variant lhs, Variant rhs) {
			try {
				if( lhs.isString() && rhs.isString() ) {
					Number l = lhs.asNumber();
					Number r = rhs.asNumber();
					double ret = l.doubleValue() - r.doubleValue();
					if( ret == 0.0 ) return 0;
					else if( ret < 0.0 ) return -1;
					else return 1;
				}
				return lhs.greaterThanForSort( rhs );
			} catch (VariantException e ) {
				return 0;
			}
		}
    }
	public static class ArraySortCompare_NumericDescending implements Comparator<Variant> {
		@Override
		public int compare(Variant lhs, Variant rhs) {
			try {
				if( lhs.isString() && rhs.isString() ) {
					Number l = lhs.asNumber();
					Number r = rhs.asNumber();
					double ret = r.doubleValue() - l.doubleValue();
					if( ret == 0.0 ) return 0;
					else if( ret < 0.0 ) return -1;
					else return 1;
				}
				return lhs.littlerThanForSort( rhs );
			} catch (VariantException e ) {
				return 0;
			}
		}
    }
	public static class ArraySortCompare_StringAscending implements Comparator<Variant> {
		@Override
		public int compare(Variant lhs, Variant rhs) {
			try {
				if( lhs.isString() && rhs.isString() ) {
					String l = lhs.getString();
					String r = rhs.getString();
					return l.compareTo(r);
				} else {
					String l = lhs.asString();
					String r = rhs.asString();
					return l.compareTo(r);
				}
			} catch (VariantException e ) {
				return 0;
			}
		}
    }
	public static class ArraySortCompare_StringDescending implements Comparator<Variant> {
		@Override
		public int compare(Variant lhs, Variant rhs) {
			try {
				if( lhs.isString() && rhs.isString() ) {
					String l = lhs.getString();
					String r = rhs.getString();
					return r.compareTo(l);
				} else {
					String l = lhs.asString();
					String r = rhs.asString();
					return r.compareTo(l);
				}
			} catch (VariantException e ) {
				return 0;
			}
		}
    }
	public static class ArraySortCompare_Functional implements Comparator<Variant> {
		private VariantClosure mClosure;
		public ArraySortCompare_Functional( VariantClosure clo ) {
			mClosure = clo;
		}
		@Override
		public int compare(Variant lhs, Variant rhs) {
			try {
				Variant result = new Variant();
				Variant[] params = new Variant[2];
				params[0] = lhs;
				params[1] = rhs;
				int hr = mClosure.funcCall(0, null, result, params, null );
				if( hr < 0 ) Error.throwFrom_tjs_error(hr,null);
				boolean ret = result.asBoolean();
				return ret ? -1 : 1;
			} catch (VariantException e ) {
				return 0;
			} catch (TJSException e) {
				return 0; // ソートの時例外の扱いが変わる
			}
		}
    }
}
