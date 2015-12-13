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

import java.lang.ref.WeakReference;
import java.util.ArrayList;


public class DictionaryNI extends NativeInstanceObject {

	private WeakReference<CustomObject> mOwner;

	/*
	static class AssignCallback extends Dispatch {
		public CustomObject mOwner;
		public int funcCall( int flag, final String membername, Variant result, Variant[] param, Dispatch2 objthis) throws VariantException, TJSException {
			// called from iTJSDispatch2::EnumMembers
			// (tTJSDictionaryNI::Assign calls iTJSDispatch2::EnumMembers)
			if( param.length < 3 ) return Error.E_BADPARAMCOUNT;

			// hidden members are not copied
			int flags = param[1].asInteger();
			if( (flags & Interface.HIDDENMEMBER) != 0 ) {
				if( result != null ) result.set(1);
				return Error.S_OK;
			}

			mOwner.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP|flags, param[0].asString(), param[2], mOwner);

			if( result != null ) result.set(1);
			return Error.S_OK;
		}
		// method from iTJSDispatch2, for enumeration callback
	}
	*/
	static class AssignCallback implements EnumMembersCallback {
		private CustomObject mOwner;
		public AssignCallback( CustomObject owner ) {
			mOwner = owner;
		}
		@Override
		public boolean callback(String name, int flags, Variant value) throws VariantException, TJSException {
			// hidden members are not copied
			if( (flags & Interface.HIDDENMEMBER) != 0 ) {
				return true;
			}
			mOwner.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP|flags, name, value, mOwner);
			return true;
		}
	}
	/*
	static class SaveStructCallback extends Dispatch {
		public ArrayList<Dispatch2> mStack;
		public TextWriteStreamInterface mStream;
		public String mIndentStr;
		public boolean mFirst;

		public int funcCall( int flag, final String membername, Variant result, Variant[] param, Dispatch2 objthis ) throws VariantException, TJSException {
			// called indirectly from tTJSDictionaryNI::SaveStructuredData
			if( param.length < 3) return Error.E_BADPARAMCOUNT;

			// hidden members are not processed
			int flags = param[1].asInteger();
			if( (flags & Interface.HIDDENMEMBER) != 0 ) {
				if( result != null ) result.set(1);
				return Error.S_OK;
			}

			if( !mFirst ) mStream.write( ",\n" );
			mFirst = false;

			mStream.write( mIndentStr );
			mStream.write( "\"" );
			mStream.write( LexBase.escapeC(param[0].asString()) );
			mStream.write( "\" => " );

			if( param[2].isObject() ) {
				// object
				VariantClosure clo = param[2].asObjectClosure();
				ArrayNI.saveStructuredDataForObject( clo.selectObject(), mStack, mStream, mIndentStr );
			} else {
				mStream.write( Utils.variantToExpressionString(param[2]) );
			}
			if( result != null) result.set(1);
			return Error.S_OK;
		}
	}
	*/
	static class SaveStructCallback implements EnumMembersCallback {
		private ArrayList<Dispatch2> mStack;
		private TextWriteStreamInterface mStream;
		private String mIndentStr;
		public boolean mCalled;
		public SaveStructCallback( ArrayList<Dispatch2> stack, TextWriteStreamInterface stream, String indent ) {
			mStack = stack;
			mStream = stream;
			mIndentStr = indent;
		}

		@Override
		public boolean callback(String name, int flags, Variant value) throws VariantException, TJSException {
			if( (flags & Interface.HIDDENMEMBER) != 0 ) {
				return true;
			}

			if( mCalled ) mStream.write( ",\n" );
			mCalled = true;

			mStream.write( mIndentStr );
			mStream.write( "\"" );
			mStream.write( LexBase.escapeC( name ) );
			mStream.write( "\" => " );

			if( value.isObject() ) { // object
				VariantClosure clo = value.asObjectClosure();
				ArrayNI.saveStructuredDataForObject( clo.selectObject(), mStack, mStream, mIndentStr );
			} else {
				mStream.write( Utils.variantToExpressionString(value) );
			}
			return true;
		}

	}
	/*
	static class AssignStructCallback extends Dispatch {
		public ArrayList<Dispatch2> mStack;
		public Dispatch2 mDest;
		public int funcCall( int flag, final String membername, Variant result, Variant[] param, Dispatch2 objthis ) throws VariantException, TJSException {
			// called indirectly from tTJSDictionaryNI::AssignStructure or
			// tTJSArrayNI::AssignStructure
			if( param.length < 3) return Error.E_BADPARAMCOUNT;

			// hidden members are not processed
			int flags = param[1].asInteger();
			if( (flags & Interface.HIDDENMEMBER) != 0 ) {
				if( result != null ) result.set(1);
				return Error.S_OK;
			}

			Variant value = param[2];
			if( value.isObject() ){
				// object
				Dispatch2 dsp = value.asObject();
				// determin dsp's object type
				Variant val = new Variant();
				if( dsp != null && dsp.getNativeInstance( DictionaryClass.ClassID ) != null ) {
					//DictionaryNI dicni = (DictionaryNI) holder.mValue;
					// dictionary
					boolean objrec = false;
					final int count = mStack.size();
					for( int i = 0; i < count; i++ ) {
						Dispatch2 v = mStack.get(i);
						if( v == dsp ) {
							// object recursion detected
							objrec = true;
							break;
						}
					}
					if(objrec) {
						val.setObject(null); // becomes null
					} else {
						Dispatch2 newobj = TJS.createDictionaryObject();
						val.setObject(newobj, newobj);
						DictionaryNI newni;
						if( (newni = (DictionaryNI)newobj.getNativeInstance( DictionaryClass.ClassID )) != null ) {
							newni.assignStructure( dsp, mStack );
						}
					}
				} else if( dsp != null && dsp.getNativeInstance( ArrayClass.ClassID ) != null ) {
					//ArrayNI arrayni = (ArrayNI) holder.mValue;
					// array
					boolean objrec = false;
					final int count = mStack.size();
					for( int i = 0; i < count; i++ ) {
						Dispatch2 v = mStack.get(i);
						if( v == dsp ) {
							// object recursion detected
							objrec = true;
							break;
						}
					} if( objrec ) {
						val.setObject( null ); // becomes null
					} else {
						Dispatch2 newobj = TJS.createArrayObject();
						val.setObject(newobj, newobj);
						ArrayNI newni;
						if( (newni = (ArrayNI)newobj.getNativeInstance( ArrayClass.ClassID )) != null ) {
							newni.assignStructure( dsp, mStack );
						}
					}
				} else {
					// other object types
					val = value;
				}
				mDest.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, param[0].asString(), val, mDest );
			} else {
				// other types
				mDest.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, param[0].asString(), value, mDest );
			}

			if( result != null ) result.set(1);
			return Error.S_OK;
		}
	}
	*/
	static class AssignStructCallback implements EnumMembersCallback {
		private ArrayList<Dispatch2> mStack;
		private Dispatch2 mDest;
		public AssignStructCallback( ArrayList<Dispatch2> stack, Dispatch2 dest) {
			mStack = stack;
			mDest = dest;
		}
		@Override
		public boolean callback(String name, int flags, Variant value) throws VariantException, TJSException {
			if( (flags & Interface.HIDDENMEMBER) != 0 ) {
				return true;
			}
			if( value.isObject() ) { // object
				Dispatch2 dsp = value.asObject();
				// determin dsp's object type
				Variant val;
				if( dsp != null ) {
					if( dsp.getNativeInstance( DictionaryClass.ClassID ) != null ) {
						// dictionary
						boolean objrec = false;
						final int count = mStack.size();
						for( int i = 0; i < count; i++ ) {
							Dispatch2 v = mStack.get(i);
							if( v == dsp ) {
								// object recursion detected
								objrec = true;
								break;
							}
						}
						val = new Variant();
						if(objrec) {
							val.setObject(null); // becomes null
						} else {
							Dispatch2 newobj = TJS.createDictionaryObject();
							val.setObject(newobj, newobj);
							DictionaryNI newni;
							if( (newni = (DictionaryNI)newobj.getNativeInstance( DictionaryClass.ClassID )) != null ) {
								newni.assignStructure( dsp, mStack );
							}
						}
					} else if( dsp.getNativeInstance( ArrayClass.ClassID ) != null ) {
						// array
						boolean objrec = false;
						final int count = mStack.size();
						for( int i = 0; i < count; i++ ) {
							Dispatch2 v = mStack.get(i);
							if( v == dsp ) {
								// object recursion detected
								objrec = true;
								break;
							}
						}
						val = new Variant();
						if( objrec ) {
							val.setObject( null ); // becomes null
						} else {
							Dispatch2 newobj = TJS.createArrayObject();
							val.setObject(newobj, newobj);
							ArrayNI newni;
							if( (newni = (ArrayNI)newobj.getNativeInstance( ArrayClass.ClassID )) != null ) {
								newni.assignStructure( dsp, mStack );
							}
						}
					} else {
						val = value;
					}
				} else {
					// other object types
					val = value;
				}
				mDest.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, name, val, mDest );
			} else {
				// other types
				mDest.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, name, value, mDest );
			}
			return true;
		}
	}

	public DictionaryNI() {
		// super(); // スーパークラスでは何もしていない
		//mOwner = null;
	}
	public int construct( Variant[] param, Dispatch2 tjsobj ) {
		// called from TJS constructor
		if( param != null && param.length != 0) return Error.E_BADPARAMCOUNT;
		mOwner = new WeakReference<CustomObject>((CustomObject)(tjsobj));
		return Error.S_OK;
	}
	// Invalidate override
	public void invalidate() throws VariantException, TJSException {
		// put here something on invalidation
		//mOwner = null;
		mOwner.clear();
		super.invalidate();
	}
	public boolean isValid() { return mOwner.get() != null; } // check validation
	public void assign( Dispatch2 dsp ) throws VariantException, TJSException { assign(dsp,true); }
	public void assign( Dispatch2 dsp, boolean clear ) throws VariantException, TJSException {
		// copy members from "dsp" to "Owner"
		// determin dsp's object type
		ArrayNI arrayni = null;
		CustomObject owner = mOwner.get();
		if( dsp != null && (arrayni = (ArrayNI)dsp.getNativeInstance(ArrayClass.ClassID)) != null ){
			// convert from array
			if( clear ) owner.clear();

			final int count = arrayni.mItems.size();
			for( int i = 0; i < count; i++ ) {
				Variant v = arrayni.mItems.get(i);
				String name = v.asString();
				i++;
				if( i >= count ) break;
				Variant v2 = arrayni.mItems.get(i);
				owner.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, name, v2, owner );
			}
		} else {
			// otherwise
			if( clear ) owner.clear();
			/*
			AssignCallback callback = new AssignCallback();
			callback.mOwner = owner;
			dsp.enumMembers(Interface.IGNOREPROP, new VariantClosure(callback, null), dsp);
			*/

			AssignCallback callback = new AssignCallback(owner);
			dsp.enumMembers( Interface.IGNOREPROP, callback, dsp );
		}
	}
	public void clear() {
		CustomObject owner = mOwner.get();
		if( owner != null ) owner.clear();
	}
	public void saveStructuredData( ArrayList<Dispatch2> stack, TextWriteStreamInterface stream, final String indentstr ) throws VariantException, TJSException {
		stream.write( "(const) %[\n" );
		String indentstr2 = indentstr + " ";

		/*
		SaveStructCallback callback = new SaveStructCallback();
		callback.mStack = stack;
		callback.mStream = stream;
		callback.mIndentStr = indentstr2;
		callback.mFirst = true;

		CustomObject owner = mOwner.get();
		owner.enumMembers( Interface.IGNOREPROP, new VariantClosure( callback, null ), owner );
		if( !callback.mFirst ) stream.write( "\n" );
		*/

		SaveStructCallback callback = new SaveStructCallback( stack, stream, indentstr2 );
		CustomObject owner = mOwner.get();
		owner.enumMembers( Interface.IGNOREPROP, callback, owner );
		if( callback.mCalled) stream.write( "\n" );

		stream.write( indentstr );
		stream.write( "]" );
	}

	public void assignStructure(Dispatch2 dsp, ArrayList<Dispatch2> stack) throws VariantException, TJSException {
		// assign structured data from dsp
		//ArrayNI dicni = null;
		if( dsp.getNativeInstance( DictionaryClass.ClassID ) != null ) {
			// copy from dictionary
			stack.add( dsp );
			try {
				CustomObject owner = mOwner.get();
				owner.clear();

				/*
				AssignStructCallback callback = new AssignStructCallback();
				callback.mDest = owner;
				callback.mStack = stack;
				dsp.enumMembers( Interface.IGNOREPROP, new VariantClosure( callback, null ), dsp );
				*/
				AssignStructCallback callback = new AssignStructCallback(stack, owner);
				dsp.enumMembers( Interface.IGNOREPROP, callback, dsp );
			} finally {
				stack.remove(stack.size()-1);
			}
		} else {
			throw new TJSException( Error.SpecifyDicOrArray );
		}
	}
	@Override
	public String toString() {
		CustomObject owner = mOwner.get();
		if( owner != null ) return owner.toString();
		return super.toString();
	}
}
