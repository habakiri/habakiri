package jp.kirikiri.tjs2.translate;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

import jp.kirikiri.tjs2.CompileException;
import jp.kirikiri.tjs2.CustomObject;
import jp.kirikiri.tjs2.Dispatch;
import jp.kirikiri.tjs2.Dispatch2;
import jp.kirikiri.tjs2.Error;
import jp.kirikiri.tjs2.InterCodeObject;
import jp.kirikiri.tjs2.ScriptBlock;
import jp.kirikiri.tjs2.TJS;
import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tjs2.TJSScriptException;
import jp.kirikiri.tjs2.Utils;
import jp.kirikiri.tjs2.Variant;
import jp.kirikiri.tjs2.VariantClosure;
import jp.kirikiri.tjs2.VariantException;

public class NativeConvertedClassBase extends Dispatch {
	static protected final int
		OP_BAND	= 0x0001,
		OP_BOR	= 0x0002,
		OP_BXOR	= 0x0003,
		OP_SUB	= 0x0004,
		OP_ADD	= 0x0005,
		OP_MOD	= 0x0006,
		OP_DIV	= 0x0007,
		OP_IDIV	= 0x0008,
		OP_MUL	= 0x0009,
		OP_LOR	= 0x000a,
		OP_LAND	= 0x000b,
		OP_SAR	= 0x000c,
		OP_SAL	= 0x000d,
		OP_SR	= 0x000e,
		OP_INC	= 0x000f,
		OP_DEC	= 0x0010;

	WeakReference<TJS> mOwner;
	public TJS getOwner() { return mOwner.get(); }

	public NativeConvertedClassBase( TJS owner ) {
		mOwner = new WeakReference<TJS>(owner);
	}
	protected static final void operateProperty( VariantClosure clo, Variant result, Variant param, Dispatch2 objthis, int ope) throws TJSException, VariantException {
		Dispatch2 objThis = clo.mObjThis != null ? clo.mObjThis : objthis;
		int hr = clo.operation(ope, null, result, param, objThis );
		if( hr < 0 ) throwFrom_tjs_error(hr, null);
	}

	protected static final void operatePropertyIndirect( VariantClosure clo, Variant name, Variant result, Variant param, Dispatch2 objthis, int ope ) throws TJSException, VariantException {
		Dispatch2 objThis = clo.mObjThis != null ? clo.mObjThis : objthis;
		if( name.isInteger() != true ) {
			String str = name.asString();
			int hr = clo.operation( ope, str, result, param, objThis );
			if( hr < 0 ) throwFrom_tjs_error( hr, str );
		} else {
			int num = name.asInteger();
			int hr = clo.operationByNum( ope, num, result, param, objThis );
			if( hr < 0 ) throwFrom_tjs_error_num( hr, num );
		}
	}

	protected static final void operatePropertyDirect( VariantClosure clo, final String name, Variant result, Variant param, Dispatch2 objthis, int ope ) throws TJSException, VariantException {
		Dispatch2 objThis = clo.mObjThis != null ? clo.mObjThis : objthis;
		int hr = clo.operation(ope, name, result, param, objThis );
		if( hr < 0 ) throwFrom_tjs_error( hr, name );
	}

	protected static final void displayExceptionGeneratedCode(int codepos, Variant[] ra, int ra_offset) throws VariantException {
		StringBuilder builder = new StringBuilder(128);
		builder.append("==== An exception occured");
		builder.append(", VM ip = ");
		builder.append(codepos);
		builder.append(" ====");
		TJS.outputToConsole( builder.toString() );
		// ディスアセンブルコードは出力できない
		// レジスタダンプもほとんど意味がないので出力しない
	}

	static protected void throwInvalidVMCode() throws TJSException {
		throw new TJSException(Error.InvalidOpecode);
	}

	protected static void addClassInstanceInfo( Dispatch2 dsp, final String className ) throws VariantException {
		if( dsp != null ) {
			dsp.addClassInstanveInfo( className );
		}
	}
	protected static void throwScriptException( Variant val, ScriptBlock block, int srcpos ) throws TJSException, VariantException {
		String msg = null;
		if( val.isObject() ) {
			VariantClosure clo = val.asObjectClosure();
			if( clo.mObject != null ) {
				Variant v2 = new Variant();
				String message_name = "message";
				int hr = clo.propGet( 0, message_name, v2, null );
				if( hr >= 0 ) {
					msg = "script exception : " + v2.asString();
				}
			}
		}
		if(msg == null || msg.length() == 0 ) {
			msg = "script exception";
		}

		throw new TJSScriptException(msg, block, srcpos, val);
	}

	static public void throwFrom_tjs_error_num( int hr, int num ) throws TJSException {
		Error.throwFrom_tjs_error( hr, String.valueOf(num) );
	}
	static public void throwFrom_tjs_error(int hr, final String name) throws TJSException {
		Error.throwFrom_tjs_error( hr, name );
	}

	protected static void setPropertyIndirect( Variant target, Variant member, Variant param, Dispatch2 objthis, int flags ) throws TJSException, VariantException {
		if( target.isObject() ) {
			VariantClosure clo = target.asObjectClosure();
			if( member.isInteger() != true ) {
				String str = member.asString();
				int hr = clo.propSet( flags, str, param, clo.mObjThis != null ? clo.mObjThis : objthis );
				if( hr < 0 ) throwFrom_tjs_error( hr, str );
			} else {
				int hr = clo.propSetByNum(flags, member.asInteger(), param, clo.mObjThis != null ? clo.mObjThis : objthis );
				if( hr < 0 ) throwFrom_tjs_error_num( hr, member.asInteger() );
			}
		} else if( target.isString() ) {
			setStringProperty( param, target, member );
		} else if( target.isOctet() ) {
			setOctetProperty( param, target, member );
		} else {
			String mes = Error.VariantConvertErrorToObject.replace( "%1", jp.kirikiri.tjs2.Utils.VariantToReadableString(target) );
			throw new VariantException( mes );
		}
	}
	private static void getOctetProperty(Variant result, final Variant octet, final Variant member) throws TJSException, VariantException {
		// processes properties toward octets.
		if( member.isNumber() != true  ) {
			final String name = member.getString();
			if( name == null ) throwFrom_tjs_error( Error.E_MEMBERNOTFOUND, "" );

			if( name.equals("length") ) {
				// get string length
				ByteBuffer o = octet.asOctet();
				if( o != null )
					result.set( o.capacity() );
				else
					result.set( 0 );
				return;
			} else if( name.charAt(0) >= '0' && name.charAt(0) <= '9' ) {
				ByteBuffer o = octet.asOctet();
				int n = Integer.valueOf( name );
				int len = o != null ? o.capacity() : 0;
				if(n<0 || n>=len)
					throw new TJSException(Error.RangeError);
				result.set( o.get(n) );
				return;
			}
			throwFrom_tjs_error( Error.E_MEMBERNOTFOUND, name );
		} else { // member.Type() == tvtInteger || member.Type() == tvtReal
			ByteBuffer o = octet.asOctet();
			int n = member.asInteger();
			int len = o != null ? o.capacity() : 0;
			if(n<0 || n>=len)
				throw new TJSException( Error.RangeError);
			result.set( o.get(n) );
			return;
		}
	}
	private static void getOctetProperty(Variant result, final Variant octet, final String name) throws TJSException, VariantException {
		// processes properties toward octets.
		if( name == null ) throwFrom_tjs_error( Error.E_MEMBERNOTFOUND, "" );
		if( name.equals("length") ) {
			// get string length
			ByteBuffer o = octet.asOctet();
			if( o != null )
				result.set( o.capacity() );
			else
				result.set( 0 );
			return;
		} else if( name.charAt(0) >= '0' && name.charAt(0) <= '9' ) {
			ByteBuffer o = octet.asOctet();
			int n = Integer.valueOf( name );
			int len = o != null ? o.capacity() : 0;
			if(n<0 || n>=len)
				throw new TJSException(Error.RangeError);
			result.set( o.get(n) );
			return;
		}
		throwFrom_tjs_error( Error.E_MEMBERNOTFOUND, name );
	}
	private static void getOctetProperty(Variant result, final Variant octet, int n ) throws TJSException, VariantException {
		// processes properties toward octets.
		ByteBuffer o = octet.asOctet();
		int len = o != null ? o.capacity() : 0;
		if(n<0 || n>=len)
			throw new TJSException( Error.RangeError);
		result.set( o.get(n) );
		return;
	}
	private static void setOctetProperty(Variant param, final Variant octet, final Variant member) throws TJSException, VariantException {
		// processes properties toward octets.
		if( member.isNumber() != true  ) {
			final String name = member.getString();
			if( name == null ) throwFrom_tjs_error( Error.E_MEMBERNOTFOUND, "" );

			if( name.equals("length") ) {
				throwFrom_tjs_error( Error.E_ACCESSDENYED, "" );
			} else if( name.charAt(0) >= '0' && name.charAt(0) <= '9' ) {
				throwFrom_tjs_error( Error.E_ACCESSDENYED, "" );
			}
			throwFrom_tjs_error( Error.E_MEMBERNOTFOUND, name );
		} else { // member.Type() == tvtInteger || member.Type() == tvtReal
			throwFrom_tjs_error( Error.E_ACCESSDENYED, "" );
		}
	}
	private static void setOctetProperty(Variant param, final Variant octet, final String name) throws TJSException, VariantException {
		// processes properties toward octets.
		if( name == null ) throwFrom_tjs_error( Error.E_MEMBERNOTFOUND, "" );

		if( name.equals("length") ) {
			throwFrom_tjs_error( Error.E_ACCESSDENYED, "" );
		} else if( name.charAt(0) >= '0' && name.charAt(0) <= '9' ) {
			throwFrom_tjs_error( Error.E_ACCESSDENYED, "" );
		}
		throwFrom_tjs_error( Error.E_MEMBERNOTFOUND, name );
	}
	private static void setOctetProperty(Variant param, final Variant octet, int member) throws TJSException, VariantException {
		throwFrom_tjs_error( Error.E_ACCESSDENYED, "" );
	}
	private static void getStringProperty(Variant result, final Variant str, final Variant member) throws TJSException, VariantException {
		// processes properties toward strings.
		if( member.isNumber() != true ) {
			final String name = member.getString();
			if( name == null ) throwFrom_tjs_error( Error.E_MEMBERNOTFOUND, "" );

			if( name.equals("length") ) {
				// get string length
				final String s = str.asString();
				if( s == null )
					result.set(0); // tTJSVariantString::GetLength can return zero if 'this' is NULL
				else
					result.set( s.length() );
				return;
			} else if( name.charAt(0) >= '0' && name.charAt(0) <= '9' ) {
				final String s = str.asString();
				int n = Integer.valueOf(name);
				int len = s.length();
				if(n == len) { result.set( new String() ); return; }
				if(n<0 || n>len)
					throw new TJSException(Error.RangeError);

				result.set( s.substring(n,n+1) );
				return;
			}
			throwFrom_tjs_error( Error.E_MEMBERNOTFOUND, name );
		} else { // member.Type() == tvtInteger || member.Type() == tvtReal
			final String s = str.asString();
			int n = member.asInteger();
			int len = s.length();
			if(n == len) { result.set( new String() ); return; }
			if(n<0 || n>len)
				throw new TJSException(Error.RangeError);
			result.set( s.substring(n,n+1) );
			return;
		}
	}
	private static void getStringProperty(Variant result, final Variant str, final String name) throws TJSException, VariantException {
		// processes properties toward strings.
		if( name == null ) throwFrom_tjs_error( Error.E_MEMBERNOTFOUND, "" );

		if( name.equals("length") ) {
			// get string length
			final String s = str.asString();
			if( s == null )
				result.set(0); // tTJSVariantString::GetLength can return zero if 'this' is NULL
			else
				result.set( s.length() );
			return;
		} else if( name.charAt(0) >= '0' && name.charAt(0) <= '9' ) {
			final String s = str.asString();
			int n = Integer.valueOf(name);
			int len = s.length();
			if(n == len) { result.set( new String() ); return; }
			if(n<0 || n>len)
				throw new TJSException(Error.RangeError);

			result.set( s.substring(n,n+1) );
			return;
		}
		throwFrom_tjs_error( Error.E_MEMBERNOTFOUND, name );
	}
	private static void getStringProperty( Variant result, final Variant str, int n ) throws TJSException, VariantException {
		// processes properties toward strings.
		final String s = str.asString();
		int len = s.length();
		if(n == len) { result.set( new String() ); return; }
		if(n<0 || n>len)
			throw new TJSException(Error.RangeError);
		result.set( s.substring(n,n+1) );
		return;
	}
	private static void setStringProperty(Variant param, final Variant str, final Variant member) throws TJSException, VariantException {
		// processes properties toward strings.
		if( member.isNumber() != true  ) {
			final String name = member.getString();
			if( name == null ) throwFrom_tjs_error( Error.E_MEMBERNOTFOUND, "" );

			if( name.equals("length") ) {
				throwFrom_tjs_error( Error.E_ACCESSDENYED, "" );
			} else if( name.charAt(0) >= '0' && name.charAt(0) <= '9' ) {
				throwFrom_tjs_error( Error.E_ACCESSDENYED, "" );
			}
			throwFrom_tjs_error( Error.E_MEMBERNOTFOUND, name );
		} else { // member.Type() == tvtInteger || member.Type() == tvtReal
			throwFrom_tjs_error( Error.E_ACCESSDENYED, "" );
		}
	}
	private static void setStringProperty(Variant param, final Variant str, final String name) throws TJSException, VariantException {
		// processes properties toward strings.
		if( name == null ) throwFrom_tjs_error( Error.E_MEMBERNOTFOUND, "" );

		if( name.equals("length") ) {
			throwFrom_tjs_error( Error.E_ACCESSDENYED, "" );
		} else if( name.charAt(0) >= '0' && name.charAt(0) <= '9' ) {
			throwFrom_tjs_error( Error.E_ACCESSDENYED, "" );
		}
		throwFrom_tjs_error( Error.E_MEMBERNOTFOUND, name );
	}
	private static void setStringProperty(Variant param, final Variant str, int member) throws TJSException, VariantException {
		// processes properties toward strings.
		throwFrom_tjs_error( Error.E_ACCESSDENYED, "" );
	}

	// getPropertyIndirect( ra[ra_offset+ca[code+1]], ra[ra_offset+ca[code+2]], ra[ra_offset+ca[code+3]], objthis, flags );
	protected static void getPropertyIndirect( Variant result, Variant target, Variant member, Dispatch2 objthis, int flags ) throws TJSException, VariantException {
		if( target.isObject() ) {
			VariantClosure clo = target.asObjectClosure();
			if( member.isInteger() != true  ){
				String str = member.asString();
				int hr = clo.propGet(flags, str, result, clo.mObjThis != null ? clo.mObjThis : objthis );
				if( hr < 0 ) throwFrom_tjs_error( hr, str );
			} else {
				int hr = clo.propGetByNum(flags, member.asInteger(), result, clo.mObjThis != null ? clo.mObjThis : objthis );
				if( hr < 0 ) throwFrom_tjs_error_num( hr, member.asInteger() );
			}
		} else if( target.isString() ) {
			getStringProperty( result, target, member );
		} else if( target.isOctet() ) {
			getOctetProperty( result, target, member );
		} else {
			String mes = Error.VariantConvertErrorToObject.replace( "%1", jp.kirikiri.tjs2.Utils.VariantToReadableString(target) );
			throw new VariantException( mes );
		}
	}
	// setPropertyDirect( ra[ra_offset+ca[code+1]], da[ca[code+2]], ra[ra_offset+ca[code+3]], objthis, flags );
	// member は、固定値なので、事前に分岐判定出来るから、展開するようにした方がいいな
	protected static void setPropertyDirect( Variant target, final String member, Variant param, Dispatch2 objthis, int flags ) throws TJSException, VariantException {
		if( target.isObject() ) {
			VariantClosure clo = target.asObjectClosure();
			int hr = clo.propSet(flags, member, param, clo.mObjThis != null ? clo.mObjThis : objthis );
			if( hr < 0 ) throwFrom_tjs_error( hr, member );
		} else if( target.isString() ) {
			setStringProperty( param, target, member );
		} else if( target.isOctet() ) {
			setOctetProperty( param, target, member );
		} else {
			String mes = Error.VariantConvertErrorToObject.replace( "%1", jp.kirikiri.tjs2.Utils.VariantToReadableString(target) );
			throw new VariantException( mes );
		}
	}
	protected static void setPropertyDirect( Variant target, int member, Variant param, Dispatch2 objthis, int flags ) throws TJSException, VariantException {
		if( target.isObject() ) {
			VariantClosure clo = target.asObjectClosure();
			String name = Integer.toString(member);
			int hr = clo.propSet(flags, name, param, clo.mObjThis != null ? clo.mObjThis : objthis );
			if( hr < 0 ) throwFrom_tjs_error( hr, name );
		} else if( target.isString() ) {
			setStringProperty( param, target, member );
		} else if( target.isOctet() ) {
			setOctetProperty( param, target, member );
		} else {
			String mes = Error.VariantConvertErrorToObject.replace( "%1", jp.kirikiri.tjs2.Utils.VariantToReadableString(target) );
			throw new VariantException( mes );
		}
	}
	//getPropertyDirect( ra[ra_offset+ca[code+1]], ra[ra_offset+ca[code+2]], da[ca[code+3]], objthis, flags );
	// member は、固定値なので、事前に条件分岐できる、文字か数値で割り分け
	protected static void getPropertyDirect( Variant result, Variant target, final String member, Dispatch2 objthis, int flags ) throws TJSException, VariantException {
		if( target.isObject() ) {
			VariantClosure clo = target.asObjectClosure();
			int hr = clo.propGet( flags, member, result, clo.mObjThis != null ? clo.mObjThis : objthis );
			if( hr < 0 ) throwFrom_tjs_error(hr, member );
		} else if( target.isString() ) {
			getStringProperty( result, target, member );
		} else if( target.isOctet() ) {
			getOctetProperty( result, target, member );
		} else {
			String mes = Error.VariantConvertErrorToObject.replace( "%1", jp.kirikiri.tjs2.Utils.VariantToReadableString(target) );
			throw new VariantException( mes );
		}
	}
	protected static void getPropertyDirect( Variant result, Variant target, int member, Dispatch2 objthis, int flags ) throws TJSException, VariantException {
		if( target.isObject() ) {
			VariantClosure clo = target.asObjectClosure();
			final String name = Integer.toString(member);
			int hr = clo.propGet( flags, name, result, clo.mObjThis != null ? clo.mObjThis : objthis );
			if( hr < 0 )
				throwFrom_tjs_error(hr, name );
		} else if( target.isString() ) {
			getStringProperty( result, target, member );
		} else if( target.isOctet() ) {
			getOctetProperty( result, target, member );
		} else {
			String mes = Error.VariantConvertErrorToObject.replace( "%1", jp.kirikiri.tjs2.Utils.VariantToReadableString(target) );
			throw new VariantException( mes );
		}
	}

	protected static void processOctetFunction( final String member, final String target, Variant[] args, Variant result) throws TJSException {
		throwFrom_tjs_error( Error.E_MEMBERNOTFOUND, member );
	}
	protected static void processStringFunction( final String member, final String target, Variant[] args, Variant result) throws TJSException, VariantException {
		InterCodeObject.processStringFunction(member, target, args, result);
	}

	protected static void instanceOf( final Variant name, Variant targ) throws VariantException, TJSException {
		// checks instance inheritance.
		String str = name.asString();
		if( str != null ) {
			int hr = CustomObject.defaultIsInstanceOf(0, targ, str, null);
			if( hr < 0 ) throwFrom_tjs_error(hr,null);

			targ.set( (hr == Error.S_TRUE) ? 1 : 0 );
			return;
		}
		targ.set(0);
	}
	protected void eval(Variant val, Dispatch2 objthis, boolean resneed) throws VariantException, TJSException, CompileException {
		Variant res = new Variant();
		String str = val.asString();
		if( str.length() > 0 ) {
			if( resneed )
				getOwner().evalExpression( str, res, objthis, null, 0 );
			else
				getOwner().evalExpression( str, null, objthis, null, 0);
		}
		if(resneed) val.set( res );
	}

	protected static final void typeOfMemberIndirect( Variant result, Variant target, Variant member, Dispatch2 objthis, int flags) throws TJSException, VariantException {
		if( target.isObject() ) {
			VariantClosure clo = target.asObjectClosure();
			if( member.isInteger() != true ) {
				String str = member.asString();
				int hr = clo.propGet( flags, str, result, clo.mObjThis != null ?clo.mObjThis:objthis );
				if( hr == Error.S_OK ) {
					typeOf( result );
				} else if( hr == Error.E_MEMBERNOTFOUND ) {
					result.set("undefined");
				} else if( hr < 0 ) throwFrom_tjs_error(hr, str);
			} else {
				int hr = clo.propGetByNum(flags, member.asInteger(), result, clo.mObjThis != null ?clo.mObjThis:objthis);
				if( hr == Error.S_OK ) {
					typeOf( result );
				} else if( hr == Error.E_MEMBERNOTFOUND ) {
					result.set("undefined");
				} else if( hr < 0 ) throwFrom_tjs_error_num( hr, member.asInteger() );
			}
		} else if( target.isString() ) {
			getStringProperty( result, target, member );
			typeOf( result );
		} else if( target.isOctet()) {
			getOctetProperty( result, target, member );
			typeOf( result );
		} else {
			String mes = Error.VariantConvertErrorToObject.replace( "%1", Utils.VariantToReadableString(target) );
			throw new VariantException( mes );
		}
	}

	protected static void typeOfMemberDirect( Variant result, Variant target, final String member, Dispatch2 objthis, int flags) throws TJSException, VariantException {
		if( target.isObject() ) {
			int hr;
			VariantClosure clo = target.asObjectClosure();
			hr = clo.propGet(flags, member, result, clo.mObjThis != null ?clo.mObjThis:objthis );
			if( hr == Error.S_OK ) {
				typeOf( result );
			} else if( hr == Error.E_MEMBERNOTFOUND ) {
				result.set("undefined");
			} else if( hr < 0 ) throwFrom_tjs_error(hr, member );
		} else if( target.isString() ) {
			getStringProperty( result, target, member );
			typeOf( result );
		} else if( target.isOctet() ){
			getOctetProperty( result, target, member );
			typeOf( result );
		} else {
			String mes = Error.VariantConvertErrorToObject.replace( "%1", Utils.VariantToReadableString(target) );
			throw new VariantException( mes );
		}
	}
	protected static void typeOfMemberDirect( Variant result, Variant target, int member, Dispatch2 objthis, int flags) throws TJSException, VariantException {
		if( target.isObject() ) {
			int hr;
			VariantClosure clo = target.asObjectClosure();
			String name = Integer.toString(member);
			hr = clo.propGet(flags, name, result, clo.mObjThis != null ?clo.mObjThis:objthis );
			if( hr == Error.S_OK ) {
				typeOf( result );
			} else if( hr == Error.E_MEMBERNOTFOUND ) {
				result.set("undefined");
			} else if( hr < 0 ) throwFrom_tjs_error(hr, name );
		} else if( target.isString() ) {
			getStringProperty( result, target, member );
			typeOf( result );
		} else if( target.isOctet() ){
			getOctetProperty( result, target, member );
			typeOf( result );
		} else {
			String mes = Error.VariantConvertErrorToObject.replace( "%1", Utils.VariantToReadableString(target) );
			throw new VariantException( mes );
		}
	}
	protected static void typeOf( Variant val ) {
		// processes TJS2's typeof operator.
		String name = val.getTypeName();
		if( name != null ) {
			val.set( name );
		}
	}

	protected static void operatePropertyIndirect0( VariantClosure clo, Variant name, Variant result, Dispatch2 objthis, int ope ) throws TJSException, VariantException {
		if( name.isInteger() != true ) {
			String str = name.asString();
			int hr = clo.operation(ope, str, result, null, clo.mObjThis != null ?clo.mObjThis:objthis);
			if( hr < 0 ) throwFrom_tjs_error(hr, str);
		} else {
			int hr = clo.operationByNum(ope, name.asInteger(), result, null, clo.mObjThis != null ?clo.mObjThis:objthis);
			if( hr < 0 ) throwFrom_tjs_error_num(hr, name.asInteger());
		}
	}

	public static void characterCodeFrom( Variant val ) throws VariantException {
		char ch[] = new char[1];
		ch[0] = (char) val.asInteger();
		val.set( new String(ch) );
	}
	public static void characterCodeOf( Variant val ) throws VariantException {
		String str = val.asString();
		if( str != null ) {
			int v = str.codePointAt(0);
			val.set( v );
		} else {
			val.set( 0 );
		}
	}
}
