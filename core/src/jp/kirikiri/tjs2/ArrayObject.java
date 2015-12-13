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

class ArrayObject extends CustomObject {

	static private Variant VoidValue;
	static final private int WORK_CHAR_LEN = 256;
	static private char[] WorkChar;
	static private int[] Result;

	public static void initialize() {
		WorkChar = new char[WORK_CHAR_LEN];
		Result = new int[1];
		VoidValue = new Variant();
	}
	public static void finalizeApplication() {
		WorkChar = null;
		Result = null;
		VoidValue = null;
	}
	public ArrayObject() {
		super();
		mCallFinalize = false;
	}
	protected void finalizeObject() throws TJSException {

		ArrayNI ni = (ArrayNI) getNativeInstance(ArrayClass.ClassID);
		if( ni == null )
			throw new TJSException( Error.NativeClassCrash );

		clear(ni);
		super.finalizeObject();
	}
	public void clear(ArrayNI ni) {
		// clear members
		final int count = ni.mItems.size();
		for( int i = 0; i < count; i++) {
			Variant v = ni.mItems.get(i);
			v.clear();
		}
		ni.mItems.clear();
	}
	public void erase(ArrayNI ni, int num) throws TJSException {
		if(num < 0) num += ni.mItems.size();
		if(num < 0) throw new TJSException(Error.RangeError);
		if( num >= ni.mItems.size() ) throw new TJSException(Error.RangeError);

		ni.mItems.remove(num);
	}
	public int remove(ArrayNI ni, Variant ref, boolean removeall) {
		int count = 0;
		IntVector todelete = new IntVector();
		final int arrayCount = ni.mItems.size();
		for( int i = 0; i < arrayCount; i++ ) {
			Variant v = ni.mItems.get(i);
			if(ref.discernCompareInternal(v)) {
				count++;
				todelete.add(i);
				if(!removeall) break;
			}
		}

		// list objects up
		final int delCount = todelete.size();
		for( int i = 0; i < delCount; i++  ) {
			int pos = todelete.get(i);
			Variant v = ni.mItems.get(pos);
			v.clear();
		}
		// remove items found
		for( int i = delCount - 1; i >= 0; i-- ) {
			ni.mItems.remove( todelete.get(i) );
		}
		todelete = null;
		return count;
	}
	public void insert(ArrayNI ni, Variant val, int num) throws TJSException {
		if(num < 0) num += ni.mItems.size();
		if(num < 0) throw new TJSException(Error.RangeError);
		int count = ni.mItems.size();
		if(num > count) throw new TJSException(Error.RangeError);

		ni.mItems.add( num, new Variant(val) );
	}
	public void add(ArrayNI ni, Variant val) {
		ni.mItems.add(new Variant(val));
	}
	public void insert(ArrayNI ni, Variant[] val, int num ) throws TJSException {
		if(num < 0) num += ni.mItems.size();
		if(num < 0) throw new TJSException(Error.RangeError);
		int count = ni.mItems.size();
		if(num > count) throw new TJSException(Error.RangeError);

		int end = val.length;
		ni.mItems.ensureCapacity( count+end );
		for( int i = 0; i < end; i++ ) {
			ni.mItems.add( num+i, new Variant(val[i]) );
		}
	}
	private final boolean isNumber( final String str, int[] result ) {
		if( str == null ) return false;
		final int len = str.length();
		// 1文字目をチェックして、数値以外は早々に除外する
		if( len > 0 ) {
			char ch = str.charAt(0);
			if( (ch >= '0' && ch <= '9') || ch == '-' || ch == '+' || (ch >= 0x09 && ch <= 0x0D) || ch == ' ' ) {
			} else {
				return false;
			}
		} else {
			return false;
		}
		char[] work;
		char ch;
		if( len < WORK_CHAR_LEN ) {
			str.getChars(0, len, WorkChar, 0 );
			work = WorkChar;
		} else {
			work = str.toCharArray();
		}
		int i = 0;
		// skip space
		while( i < len ) {
			ch = work[i];
			if( (ch >= 0x09 && ch <= 0x0D) || ch == ' ' ) {
				i++;
			} else{
				break;
			}
		}
		if( i >= len ) return false;

		boolean sign = false;
		ch = work[i];
		if( ch == '-' ) {
			sign = true;
			i++;
		} else if( ch == '+' ) {
			i++;
		}
		if( i >= len ) return false;

		// skip space
		while( i < len ) {
			ch = work[i];
			if( (ch >= 0x09 && ch <= 0x0D) || ch == ' ' ) {
				i++;
			} else{
				break;
			}
		}
		if( i >= len ) return false;

		int number = 0;
		for( ; i < len; i++ ) {
			ch = work[i];
			if( ch >= '0' && ch <= '9' ) {
				int num = ch - '0';
				number = number * 10 + num;
			} else if( ch == '.' || (ch >= 0x09 && ch <= 0x0D) || ch == ' ' ) {
				break;
			} else {
				return false;
			}
		}
		if( ch == '.' ) {
			for( ; i < len; i++ ) {
				ch = work[i];
				if( (ch >= '0' && ch <= '9') || ch == '.' ) {
				} else {
					break;
				}
			}
		}

		// skip space
		while( i < len ) {
			ch = work[i];
			if( (ch >= 0x09 && ch <= 0x0D) || ch == ' ' ) {
				i++;
			} else{
				break;
			}
		}
		if( i == len ) {
			if( sign ) {
				result[0] = -number;
			} else {
				result[0] = number;
			}
			return true;
		}
		return false;
	}

	// function invocation
	public int funcCall( int flag, final String memberName, Variant result, Variant[] param, Dispatch2 objThis ) throws VariantException, TJSException {
		if( isNumber( memberName, Result ) ) {
			return funcCallByNum(flag, Result[0], result, param, objThis);
		}
		return super.funcCall(flag, memberName, result, param, objThis );
	}

	// function invocation by index number
	public int funcCallByNum( int flag, int num, Variant result, Variant[] param, Dispatch2 objthis ) throws VariantException, TJSException {
		if(!getValidity())
			return Error.E_INVALIDOBJECT;

		ArrayNI ni = (ArrayNI) objthis.getNativeInstance(ArrayClass.ClassID);
		if( ni == null )
			return Error.E_NATIVECLASSCRASH;

		int membercount = ni.mItems.size();
		if(num < 0) num = membercount + num;
		if( (flag & Interface.MEMBERMUSTEXIST) != 0 && (num < 0 || membercount <= num) )
			return Error.E_MEMBERNOTFOUND;
		Variant val = new Variant( (membercount<=num || num < 0) ? VoidValue : ni.mItems.get(num) );
		return defaultFuncCall( flag, val, result, param, objthis );
	}

	// property get
	public int propGet( int flag, final String memberName, Variant result, Dispatch2 objThis ) throws VariantException, TJSException {
		if( isNumber( memberName, Result ) ) {
			return propGetByNum( flag, Result[0], result, objThis );
		}
		return super.propGet(flag, memberName, result, objThis );
	}

	// property get by index number
	public int propGetByNum( int flag, int num, Variant result, Dispatch2 objthis ) throws VariantException, TJSException {
		if(!getValidity())
			return Error.E_INVALIDOBJECT;

		ArrayNI ni = (ArrayNI) objthis.getNativeInstance(ArrayClass.ClassID);
		if( ni == null )
			return Error.E_NATIVECLASSCRASH;

		int membercount = ni.mItems.size();
		if(num < 0) num = membercount + num;
		if( (flag & Interface.MEMBERMUSTEXIST) != 0 && (num < 0 || membercount <= num) )
			return Error.E_MEMBERNOTFOUND;
		Variant val = new Variant( (membercount<=num || num < 0) ? VoidValue : ni.mItems.get(num) );
		return defaultPropGet( flag, val, result, objthis);
	}

	// property set
	public int propSet( int flag, String memberName, final Variant param, Dispatch2 objThis ) throws VariantException, TJSException {
		if( isNumber( memberName, Result ) ) {
			return propSetByNum( flag, Result[0], param, objThis );
		}
		return super.propSet(flag, memberName, param, objThis );
	}

	// property set by index number
	public int propSetByNum( int flag, int num, final Variant param, Dispatch2 objthis ) throws VariantException, TJSException {
		if(!getValidity())
			return Error.E_INVALIDOBJECT;

		ArrayNI ni = (ArrayNI) objthis.getNativeInstance(ArrayClass.ClassID);
		if( ni == null )
			return Error.E_NATIVECLASSCRASH;

		if(num < 0) num += ni.mItems.size();
		if( num >= ni.mItems.size() ) {
			if( (flag & Interface.MEMBERMUSTEXIST) != 0 ) return Error.E_MEMBERNOTFOUND;
			//ni.mItems.resize(num+1);
			for( int i = ni.mItems.size(); i <= num; i++ ) {
				ni.mItems.add( new Variant() );
			}
		}
		if(num < 0) return Error.E_MEMBERNOTFOUND;
		Variant val = ni.mItems.get(num);
		return defaultPropSet(flag, val, param, objthis);
	}

	// enumerate members
	public int enumMembers( int flag, VariantClosure callback, Dispatch2 objThis ) throws VariantException, TJSException {
		return Error.E_NOTIMPL; // currently not implemented
	}

	// delete member
	public int deleteMember( int flag, String memberName, Dispatch2 objThis ) throws VariantException, TJSException {
		if( isNumber( memberName, Result ) ) {
			return deleteMemberByNum( flag, Result[0], objThis );
		}
		return super.deleteMember(flag, memberName, objThis );
	}

	// delete member by index number
	public int deleteMemberByNum( int flag, int num, Dispatch2 objthis ) throws VariantException, TJSException {
		if(!getValidity())
			return Error.E_INVALIDOBJECT;

		ArrayNI ni = (ArrayNI) objthis.getNativeInstance(ArrayClass.ClassID);
		if( ni == null )
			return Error.E_NATIVECLASSCRASH;

		if(num < 0) num += ni.mItems.size();
		if(num < 0 || num>=ni.mItems.size()) return Error.E_MEMBERNOTFOUND;
		ni.mItems.remove( num );
		return Error.S_OK;
	}

	// invalidation
	public int invalidate( int flag, String memberName, Dispatch2 objThis ) throws VariantException, TJSException {
		if( isNumber( memberName, Result ) ) {
			return invalidateByNum( flag, Result[0], objThis );
		}
		return super.invalidate(flag, memberName, objThis );
	}

	// invalidation by index number
	public int invalidateByNum( int flag, int num, Dispatch2 objthis ) throws VariantException, TJSException {
		if(!getValidity())
			return Error.E_INVALIDOBJECT;

		ArrayNI ni = (ArrayNI) objthis.getNativeInstance(ArrayClass.ClassID);
		if( ni == null )
			return Error.E_NATIVECLASSCRASH;

		int membercount = ni.mItems.size();
		if(num < 0) num = membercount + num;
		if( (flag & Interface.MEMBERMUSTEXIST) != 0 && (num < 0 || membercount <= num) )
			return Error.E_MEMBERNOTFOUND;
		Variant val = new Variant( (membercount<=num || num < 0) ? VoidValue : ni.mItems.get(num) );
		return defaultInvalidate(flag, val, objthis);
	}

	// get validation, returns true or false
	public int isValid( int flag, String memberName, Dispatch2 objThis ) throws VariantException, TJSException {
		if( isNumber( memberName, Result ) ) {
			return isValidByNum( flag, Result[0], objThis );
		}
		return super.isValid(flag, memberName, objThis );
	}

	// get validation by index number, returns true or false
	public int isValidByNum( int flag, int num, Dispatch2 objthis ) throws VariantException, TJSException {
		if(!getValidity())
			return Error.E_INVALIDOBJECT;

		ArrayNI ni = (ArrayNI) objthis.getNativeInstance(ArrayClass.ClassID);
		if( ni == null )
			return Error.E_NATIVECLASSCRASH;

		int membercount = ni.mItems.size();
		if(num < 0) num = membercount + num;
		if( (flag & Interface.MEMBERMUSTEXIST) != 0 && (num < 0 || membercount <= num) )
			return Error.E_MEMBERNOTFOUND;
		Variant val = new Variant( (membercount<=num || num < 0) ? VoidValue : ni.mItems.get(num) );
		return defaultIsValid(flag, val, objthis);
	}

	// create new object
	public int createNew( int flag, String memberName, Holder<Dispatch2> result, Variant[] param, Dispatch2 objThis ) throws VariantException, TJSException {
		if( isNumber( memberName, Result ) ) {
			return createNewByNum( flag, Result[0], result, param, objThis );
		}
		return super.createNew(flag, memberName, result, param, objThis );
	}

	// create new object by index number
	public int createNewByNum( int flag, int num, Holder<Dispatch2> result, Variant[] param, Dispatch2 objthis ) throws VariantException, TJSException {
		if(!getValidity())
			return Error.E_INVALIDOBJECT;

		ArrayNI ni = (ArrayNI) objthis.getNativeInstance(ArrayClass.ClassID);
		if( ni == null )
			return Error.E_NATIVECLASSCRASH;

		int membercount = ni.mItems.size();
		if(num < 0) num = membercount + num;
		if( (flag & Interface.MEMBERMUSTEXIST) != 0 && (num < 0 || membercount <= num) )
			return Error.E_MEMBERNOTFOUND;
		Variant val = new Variant( (membercount<=num || num < 0) ? VoidValue : ni.mItems.get(num) );
		return defaultCreateNew(flag, val, result, param, objthis);
	}

	// reserved1 not use

	// class instance matching returns false or true
	public int isInstanceOf( int flag, String memberName, String className, Dispatch2 objThis ) throws VariantException, TJSException {
		if( isNumber( memberName, Result ) ) {
			return isInstanceOfByNum( flag, Result[0], className, objThis );
		}
		return super.isInstanceOf(flag, memberName, className, objThis );
	}

	// class instance matching by index number
	public int isInstanceOfByNum( int flag, int num, String className, Dispatch2 objthis ) throws VariantException, TJSException {
		if(!getValidity())
			return Error.E_INVALIDOBJECT;

		ArrayNI ni = (ArrayNI) objthis.getNativeInstance(ArrayClass.ClassID);
		if( ni == null )
			return Error.E_NATIVECLASSCRASH;

		int membercount = ni.mItems.size();
		if(num < 0) num = membercount + num;
		if( (flag & Interface.MEMBERMUSTEXIST) != 0 && (num < 0 || membercount <= num) )
			return Error.E_MEMBERNOTFOUND;
		Variant val = new Variant( (membercount<=num || num < 0) ? VoidValue : ni.mItems.get(num) );
		return defaultIsInstanceOf(flag, val, className, objthis);
	}

	// operation with member
	public int operation( int flag, String memberName, Variant result, final Variant param, Dispatch2 objThis ) throws VariantException, TJSException {
		if( isNumber( memberName, Result ) ) {
			return operationByNum( flag, Result[0], result, param, objThis );
		}
		return super.operation(flag, memberName, result, param, objThis );
	}

	// operation with member by index number
	public int operationByNum( int flag, int num, Variant result, final Variant param, Dispatch2 objthis ) throws VariantException, TJSException {
		if(!getValidity())
			return Error.E_INVALIDOBJECT;

		ArrayNI ni = (ArrayNI) objthis.getNativeInstance(ArrayClass.ClassID);
		if( ni == null )
			return Error.E_NATIVECLASSCRASH;

		if(num < 0) num += ni.mItems.size();
		if( num >= ni.mItems.size() ) {
			if( (flag & Interface.MEMBERMUSTEXIST) != 0 ) return Error.E_MEMBERNOTFOUND;
			//ni.mItems.resize(num+1);
			for( int i = ni.mItems.size(); i <= num; i++ ) {
				ni.mItems.add( new Variant() );
			}
		}
		if(num < 0) return Error.E_MEMBERNOTFOUND;
		Variant val = ni.mItems.get(num);
		return defaultOperation( flag, val, result, param, objthis );
	}

}

