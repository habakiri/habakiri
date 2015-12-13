/**
 ******************************************************************************
 * Copyright (c), Takenori Imoto
 * �� software http://www.kaede-software.com/
 * All rights reserved.
 ******************************************************************************
 * �\�[�X�R�[�h�`�����o�C�i���`�����A�ύX���邩���Ȃ������킸�A�ȉ��̏�����
 * �����ꍇ�Ɍ���A�ĔЕz����юg�p��������܂��B
 *
 * �E�\�[�X�R�[�h���ĔЕz����ꍇ�A��L�̒��쌠�\���A�{�����ꗗ�A����щ��L�Ɛ�
 *   �������܂߂邱�ƁB
 * �E�o�C�i���`���ōĔЕz����ꍇ�A�Еz���ɕt���̃h�L�������g���̎����ɁA��L��
 *   ���쌠�\���A�{�����ꗗ�A����щ��L�Ɛӏ������܂߂邱�ƁB
 * �E���ʂɂ����ʂ̋��Ȃ��ɁA�{�\�t�g�E�F�A����h���������i�̐�`�܂��͔̔�
 *   ���i�ɁA�g�D�̖��O�܂��̓R���g���r���[�^�[�̖��O���g�p���Ă͂Ȃ�Ȃ��B
 *
 * �{�\�t�g�E�F�A�́A���쌠�҂���уR���g���r���[�^�[�ɂ���āu����̂܂܁v��
 * ����Ă���A�����َ����킸�A���ƓI�Ȏg�p�\���A����ѓ���̖ړI�ɑ΂���K
 * �����Ɋւ���Öق̕ۏ؂��܂߁A�܂�����Ɍ��肳��Ȃ��A�����Ȃ�ۏ؂�����܂�
 * ��B���쌠�҂��R���g���r���[�^�[���A���R�̂�������킸�A���Q�����̌�������
 * ����킸�A���ӔC�̍������_��ł��邩���i�ӔC�ł��邩�i�ߎ����̑��́j�s�@
 * �s�ׂł��邩���킸�A���ɂ��̂悤�ȑ��Q����������\����m�炳��Ă����Ƃ�
 * �Ă��A�{�\�t�g�E�F�A�̎g�p�ɂ���Ĕ��������i��֕i�܂��͑�p�T�[�r�X�̒��B�A
 * �g�p�̑r���A�f�[�^�̑r���A���v�̑r���A�Ɩ��̒��f���܂߁A�܂�����Ɍ��肳���
 * ���j���ڑ��Q�A�Ԑڑ��Q�A�����I�ȑ��Q�A���ʑ��Q�A�����I���Q�A�܂��͌��ʑ��Q��
 * ���āA��ؐӔC�𕉂�Ȃ����̂Ƃ��܂��B
 ******************************************************************************
 * �{�\�t�g�E�F�A�́A�g���g��2 ( http://kikyou.info/tvp/ ) �̃\�[�X�R�[�h��Java
 * �ɏ������������̂��ꕔ�g�p���Ă��܂��B
 * �g���g��2 Copyright (C) W.Dee <dee@kikyou.info> and contributors
 ******************************************************************************
 */
package jp.kirikiri.tjs2;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class RegExpClass extends NativeClass {

	public static int mClassID = -1;
	public static Variant mLastRegExp;
	private static final int globalsearch = (1<<31);
	private static final int tjsflagsmask = 0xff000000;

	static private final String CLASS_NAME = "RegExp";

	protected NativeInstance createNativeInstance() {
		return new RegExpNI();
	}

	static private void compile( Variant[] param, RegExpNI _this ) throws VariantException, TJSException {
		String expr = param[0].asString();
		int flags;
		if( param.length >= 2 ) {
			String fs = param[1].asString();
			flags = RegExpNI.getRegExpFlagsFromString( fs );
		} else {
			flags = RegExpNI.regExpFlagToValue((char) 0, 0);
		}

		if( expr.length() == 0 ) expr = "(?:)"; // generate empty regular expression

		try {
			int pflag = (flags& ~tjsflagsmask);
			if( pflag != 0 ) {
				_this.RegEx = Pattern.compile( expr, pflag );
			} else {
				_this.RegEx = Pattern.compile( expr );
			}
		} catch( PatternSyntaxException e ) {
			_this.RegEx = null;
			throw new TJSException( e.getMessage() );
		} catch( IllegalArgumentException e ) {
			_this.RegEx = null;
			throw new TJSException( e.getMessage() );
		}
		_this.mFlags = flags;
	}
	static private boolean match( String target, RegExpNI _this) {
		if( _this.RegEx == null ) return false;

		int targlen = target.length();
		if( _this.mStart == targlen ) {
			// Start already reached at end
			return _this.RegEx == null;  // returns true if empty
		} else if(_this.mStart > targlen) {
			// Start exceeds target's length
			return false;
		}
		int searchstart = _this.mStart;
		_this.mMatch = _this.RegEx.matcher( target.substring(searchstart) );
		return _this.mMatch.matches();
	}
	static private boolean exec( String target, RegExpNI _this ) throws VariantException, TJSException {
		boolean matched = match( target, _this );
		Dispatch2 array = getResultArray( matched, _this, _this.mMatch );

		_this.mArray = new Variant(array, array);
		_this.mInput = target;
		if( !matched || _this.RegEx == null ) {
			_this.mIndex = _this.mStart;
			_this.mLastIndex = _this.mStart;
			_this.mLastMatch = new String();
			_this.mLastParen = new String();
			_this.mLeftContext = target.substring(0,_this.mStart);
		} else {
			_this.mIndex = _this.mStart + _this.mMatch.start();
			_this.mLastIndex = _this.mStart + _this.mMatch.end();
			_this.mLastMatch = _this.mMatch.group();
			_this.mLastParen = _this.mMatch.group(_this.mMatch.groupCount()-1);
			_this.mLeftContext = target.substring(_this.mIndex);
			_this.mRightContext = target.substring(_this.mLastIndex);
			if( (_this.mFlags & globalsearch) != 0 ) {
				// global search flag changes the next search starting position.
				int match_end = _this.mLastIndex;
				_this.mStart = match_end;
			}
		}

		return matched;
	}
	static private Dispatch2 getResultArray( boolean matched, RegExpNI _this, Matcher m ) throws VariantException, TJSException{
		Dispatch2 array = TJS.createArrayObject();
		if( matched ) {
			if(_this.RegEx == null  ) {
				Variant val = new Variant( "" );
				array.propSetByNum( Interface.MEMBERENSURE|Interface.IGNOREPROP, 0, val, array);
			} else {
				if( m != null ) {
					boolean isMatch = m.matches();
					Variant val;
					if( isMatch ) {
						val = new Variant( m.group() );
						array.propSetByNum( Interface.MEMBERENSURE|Interface.IGNOREPROP, 0, val, array );
					}
					int size = m.groupCount();
					for( int i = 0; i < size; i++ ) {
						val = new Variant( m.group(i+1) );
						array.propSetByNum( Interface.MEMBERENSURE|Interface.IGNOREPROP, i+1, val, array );
					}
				}
			}
		}
		return array;
	}
	/*
	public static RegExpNI getNativeInstance( Dispatch2 objthis ) {
		Holder<NativeInstance> holder = new Holder<NativeInstance>(null);
		int hr = objthis.nativeInstanceSupport( Interface.NIS_GETINSTANCE, mClassID, holder );
		if( hr < 0 ) return null;
		return (RegExpNI) holder.mValue;
	}
	*/

	public RegExpClass() throws VariantException, TJSException {
		super(CLASS_NAME);
		final int NCM_CLASSID = TJS.registerNativeClass(CLASS_NAME);
		setClassID( NCM_CLASSID );
		mClassID = NCM_CLASSID;


		// constructor
		registerNCM( CLASS_NAME, new NativeClassConstructor() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException, TJSException {
				RegExpNI _this = (RegExpNI)objthis.getNativeInstance( mClassID );
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				int hr = _this.construct( param, objthis );
				if( hr < 0 ) return hr;
				if( param.length >= 1) {
					compile( param, _this );
				}

				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "finalize", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) {
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "compile", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				RegExpNI _this = (RegExpNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				// compiles given regular expression and flags.
				if( param.length < 1) return Error.E_BADPARAMCOUNT;
				compile( param, _this );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "_compile", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				RegExpNI _this = (RegExpNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				/*
					internal function; compiles given constant regular expression.
					input expression is following format:
					//flags/expression
					where flags is flag letters ( [gil] )
					and expression is a Regular Expression
				*/
				if( param.length != 1) return Error.E_BADPARAMCOUNT;
				String expr = param[0].getString();
				if( expr == null || expr.charAt(0) == 0 ) return Error.E_FAIL;
				if( expr.charAt(0) != '/' || expr.charAt(1) != '/' ) return Error.E_FAIL;

				int exprstart = expr.indexOf( '/', 2 );
				if( exprstart < 0 ) return Error.E_FAIL;

				int flags = RegExpNI.getRegExpFlagsFromString( expr.substring(2) );
				int pflag = (flags& ~tjsflagsmask);

				try {
					if( pflag != 0 ) {
						_this.RegEx = Pattern.compile( expr.substring(exprstart+1), pflag );
					} else {
						_this.RegEx = Pattern.compile( expr.substring(exprstart+1) );
					}
					//_this->RegEx.assign(exprstart, (wregex::flag_type));
				} catch( PatternSyntaxException e ) {
					throw new TJSException( e.getMessage() );
				} catch( IllegalArgumentException e ) {
					throw new TJSException( e.getMessage() );
				}
				_this.mFlags = flags;
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "test", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				RegExpNI _this = (RegExpNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				/*
					do the text searching.
					return match found ( true ), or not found ( false ).
					this function *changes* internal status.
				 */
				if( param.length < 1) return Error.E_BADPARAMCOUNT;

				String target = param[0].asString();
				boolean matched = exec( target, _this);

				mLastRegExp = new Variant(objthis, objthis);

				if( result != null ) {
					result.set( matched ? 1 : 0 );
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "match", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				RegExpNI _this = (RegExpNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				/*
					do the text searching.
					this function is the same as test, except for its return value.
					match returns an array that contains each matching part.
					if match failed, returns empty array. eg.
					any internal status will not be changed.
				*/
				if( param.length < 1) return Error.E_BADPARAMCOUNT;

				if( result != null ) {
					String target = param[0].asString();
					boolean matched = match( target, _this);
					Dispatch2 array = getResultArray( matched, _this, _this.mMatch );
					result.set( array, array );
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "exec", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				RegExpNI _this = (RegExpNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				/*
					same as the match except for the internal status' change.
					var ar;
					var pat = /:(\d+):(\d+):/g;
					while((ar = pat.match(target)).count)
					{
						// ...
					}
				*/
				if( param.length < 1) return Error.E_BADPARAMCOUNT;
				String target = param[0].asString();
				exec( target, _this );
				mLastRegExp = new Variant(objthis, objthis);
				if( result != null ) {
					result.set( _this.mArray );
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "replace", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				RegExpNI _this = (RegExpNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				/*
					replaces the string

					newstring = /regexp/.replace(orgstring, newsubstring);
					newsubstring can be:
						1. normal string ( literal or expression that respresents string )
						2. a function
					function is called as in RegExp's context, returns new substring.

					or

					newstring = string.replace(/regexp/, newsubstring);
						( via String.replace method )

					replace method ignores start property, and does not change any
						internal status.
				*/
				if( param.length < 2) return Error.E_BADPARAMCOUNT;
				String target = param[0].asString();
				String to = null;
				boolean func;
				VariantClosure funcval = null;
				if( param[1].isObject() != true ) {
					to = param[1].asString();
					func = false;
				} else {
					funcval = param[1].asObjectClosure();
					if( funcval.mObjThis == null ) {
						funcval.mObjThis = objthis;
					}
					func = true;
				}
				String ret = null;
				Matcher m = _this.RegEx.matcher(target);
				if( func == false ) {
					ret = m.replaceAll(to);
				} else {
					int hr;
					VariantClosure clo = new VariantClosure(null,null);
					Variant funcret = new Variant();
					Variant arrayval = new Variant(clo);
					Variant[] args = new Variant[1];
					args[0] = arrayval;
					int size = target.length();
					ret = "";
					for( int i = 0; i < size; ) {
						if( m.find(i) ) {
							ret += target.substring( i, m.start() );
							Dispatch2 array = getResultArray( true, _this, m );
							clo.set( array, array );
							hr = funcval.funcCall(0, null, funcret, args, null );
							if( hr >= 0 ) {
								ret += funcret.asString();
							}
							i = m.end();
						} else {
							break;
						}
					}

				}
				if( result != null ) result.set( ret );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "split", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				RegExpNI _this = (RegExpNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				/*
					replaces the string

					array = /regexp/.replace(targetstring, <reserved>, purgeempty);

					or

					array = targetstring.split(/regexp/, <reserved>, purgeempty);

					or

					array = [].split(/regexp/, targetstring, <reserved>, purgeempty);

					this method does not update properties
				*/
				if( param.length < 1) return Error.E_BADPARAMCOUNT;

				String target = param[0].asString();
				boolean purgeempty = false;
				if( param.length >= 3 ) purgeempty = param[2].asBoolean();

				Holder<Dispatch2> array = new Holder<Dispatch2>(null);
				_this.split( array, target, purgeempty);

				if( result != null ) result.set( array.mValue, array.mValue );

				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "matches", new NativeClassProperty() {
			@Override
			public int get(Variant result, Dispatch2 objthis) {
				RegExpNI _this = (RegExpNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.mArray );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) {
				return Error.E_ACCESSDENYED;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "start", new NativeClassProperty() {
			@Override
			public int get(Variant result, Dispatch2 objthis) {
				RegExpNI _this = (RegExpNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.mStart );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				RegExpNI _this = (RegExpNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.mStart = param.asInteger();
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "index", new NativeClassProperty() {
			@Override
			public int get(Variant result, Dispatch2 objthis) {
				RegExpNI _this = (RegExpNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.mIndex );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) {
				return Error.E_ACCESSDENYED;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "lastIndex", new NativeClassProperty() {
			@Override
			public int get(Variant result, Dispatch2 objthis) {
				RegExpNI _this = (RegExpNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.mLastIndex);
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) {
				return Error.E_ACCESSDENYED;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "input", new NativeClassProperty() {
			@Override
			public int get(Variant result, Dispatch2 objthis) {
				RegExpNI _this = (RegExpNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.mInput);
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) {
				return Error.E_ACCESSDENYED;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "lastMatch", new NativeClassProperty() {
			@Override
			public int get(Variant result, Dispatch2 objthis) {
				RegExpNI _this = (RegExpNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.mLastMatch);
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "lastParen", new NativeClassProperty() {
			@Override
			public int get(Variant result, Dispatch2 objthis) {
				RegExpNI _this = (RegExpNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.mLastParen);
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "leftContext", new NativeClassProperty() {
			@Override
			public int get(Variant result, Dispatch2 objthis) {
				RegExpNI _this = (RegExpNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.mLeftContext);
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "rightContext", new NativeClassProperty() {
			@Override
			public int get(Variant result, Dispatch2 objthis) {
				RegExpNI _this = (RegExpNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.mRightContext);
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, 0 );


		registerNCM( "last", new NativeClassProperty() {
			@Override
			public int get(Variant result, Dispatch2 objthis) {
				result.set( mLastRegExp );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, Interface.STATICMEMBER );
	}
}
