package jp.kirikiri.tjs2.translate;

import jp.kirikiri.tjs2.Dispatch2;
import jp.kirikiri.tjs2.TJS;
import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tjs2.Variant;
import jp.kirikiri.tjs2.VariantException;

public abstract class NativeConvertedClassConstructor extends NativeConvertedClassMethod {

	public NativeConvertedClassConstructor(TJS owner) {
		super(owner);
	}

	public int funcCall( int flag, final String membername, Variant result, Variant[] param, Dispatch2 objthis ) throws VariantException, TJSException {
		if( membername != null ) return super.funcCall(flag, membername, result, param, objthis );
		if( result != null ) result.clear();

		return process( result, param, objthis );
	}
}
