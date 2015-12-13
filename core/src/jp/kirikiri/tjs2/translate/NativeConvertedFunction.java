package jp.kirikiri.tjs2.translate;

import jp.kirikiri.tjs2.Dispatch2;
import jp.kirikiri.tjs2.Error;
import jp.kirikiri.tjs2.TJS;
import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tjs2.Variant;
import jp.kirikiri.tjs2.VariantException;

public abstract class NativeConvertedFunction extends NativeConvertedClassBase {

	public NativeConvertedFunction(TJS owner) {
		super(owner);
	}

	public int funcCall( int flag, final String membername, Variant result, Variant[] param, Dispatch2 objthis ) throws VariantException, TJSException {
		if( membername != null ) {
			return super.funcCall( flag, membername, result, param, objthis );
		}
		return process( result, param, objthis );
	}

	public int isInstanceOf( int flag, final String membername, final String classname, Dispatch2 objthis ) throws VariantException, TJSException {
		if( membername == null ) {
			if( "Function".equals( classname ) ) return Error.S_TRUE;
		}
		return super.isInstanceOf( flag, membername, classname, objthis );
	}

	// override this instead of FuncCall
	abstract protected int process( Variant result, Variant[] param, Dispatch2 objthis ) throws VariantException, TJSException;
}
