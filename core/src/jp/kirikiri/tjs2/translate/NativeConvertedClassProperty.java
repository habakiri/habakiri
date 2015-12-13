package jp.kirikiri.tjs2.translate;

import jp.kirikiri.tjs2.Dispatch2;
import jp.kirikiri.tjs2.Error;
import jp.kirikiri.tjs2.TJS;
import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tjs2.Variant;
import jp.kirikiri.tjs2.VariantException;

public abstract class NativeConvertedClassProperty extends NativeConvertedClassBase {

	public NativeConvertedClassProperty(TJS owner) {
		super(owner);
	}

	public int isInstanceOf( int flag, final String membername, final String classname, Dispatch2 objthis ) throws VariantException, TJSException {
		if( membername == null ) {
			if( "Property".equals(classname) ) return Error.S_TRUE;
		}
		return super.isInstanceOf(flag, membername, classname, objthis);
	}
	public int propGet( int flag, final String membername, Variant result, Dispatch2 objthis ) throws VariantException, TJSException {
		if( membername != null ) return super.propGet( flag, membername, result, objthis );
		if( objthis == null ) return Error.E_NATIVECLASSCRASH;
		if( result == null ) return Error.E_FAIL;

		return get( result, objthis );
	}
	public int propSet( int flag, final String membername, final Variant param, Dispatch2 objthis ) throws VariantException, TJSException {
		if( membername != null ) return super.propSet(flag, membername, param, objthis);
		if( objthis == null ) return Error.E_NATIVECLASSCRASH;
		if( param == null ) return Error.E_FAIL;

		return set( param, objthis );
	}

	abstract public int get( Variant result, Dispatch2 objthis ) throws TJSException;
	abstract public int set( Variant param, Dispatch2 objthis ) throws VariantException, TJSException;
}
