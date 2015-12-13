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


class ArrayNI extends NativeInstanceObject {

	public ArrayList<Variant> mItems;

	/*
	static class DictionaryEnumCallback extends Dispatch {
		public ArrayList<Variant> mItems;

		public int funcCall( int flag, final String membername, Variant result, Variant[] param, Dispatch2 objthis ) throws VariantException {
			// called from CustomObject::EnumMembers
			if( param.length < 3) return Error.E_BADPARAMCOUNT;

			// hidden members are not processed
			int flags = param[1].asInteger();
			if( (flags & Interface.HIDDENMEMBER) != 0 ) {
				if( result != null ) result.set(1);
				return Error.S_OK;
			}
			// push items
			mItems.add( new Variant(param[0]) );
			mItems.add( new Variant(param[2]) );
			if( result != null ) result.set(1);
			return Error.S_OK;
		}
	};
	*/

	static class DictionaryEnumCallback implements EnumMembersCallback {
		public ArrayList<Variant> mItems;
		public DictionaryEnumCallback( ArrayList<Variant> items ) {
			mItems = items;
		}
		@Override
		public boolean callback( String name, int flags, Variant value ) {
			// hidden members are not processed
			if( (flags & Interface.HIDDENMEMBER) != 0 ) {
				return true;
			}
			// push items
			mItems.add( new Variant(name) );
			mItems.add( new Variant(value) );
			return true;
		}
	};
	public ArrayNI() {
		//super(); // スーパークラスでは何もしていない
		mItems = new ArrayList<Variant>();
	}

	public int construct( Variant[] param, Dispatch2 tjsObj ) {
		// called by TJS constructor
		if( param != null && param.length != 0 ) {
			return Error.E_BADPARAMCOUNT;
		}
		return Error.S_OK;
	}
	public void assign( Dispatch2 dsp ) throws VariantException, TJSException {
		// copy members from "dsp" to "Owner"

		// determin dsp's object type
		//Holder<ArrayNI> arrayni = new Holder<ArrayNI>(null);
		ArrayNI array = (ArrayNI)dsp.getNativeInstance(ArrayClass.ClassID);
		if( array != null  ) {
			// copy from array
			mItems.clear();
			final int count = array.mItems.size();
			for( int i = 0; i < count; i++ ) {
				mItems.add( new Variant(array.mItems.get(i)) );
			}
			//mItems.addAll( array.mItems );
		} else {
			// convert from dictionary or others
			mItems.clear();
			/*
			DictionaryEnumCallback callback = new DictionaryEnumCallback();
			callback.mItems = mItems;
			dsp.enumMembers( Interface.IGNOREPROP, new VariantClosure( callback, null), dsp);
			*/

			DictionaryEnumCallback callback = new DictionaryEnumCallback( mItems );
			dsp.enumMembers( Interface.IGNOREPROP, callback, dsp );
		}
	}
	public void saveStructuredData( ArrayList<Dispatch2> stack, TextWriteStreamInterface stream, final String indentstr ) throws VariantException, TJSException {
		stream.write( "(const) [\n" );
		String indentstr2 = indentstr + " ";

		final int count = mItems.size();
		for( int i = 0; i < count; i++ ) {
			Variant v = mItems.get(i);
			stream.write(indentstr2);
			if( v.isObject() ) {
				// object
				VariantClosure clo = v.asObjectClosure();
				saveStructuredDataForObject( clo.selectObject(),stack, stream, indentstr2 );
			} else {
				stream.write( Utils.variantToExpressionString(v) );
			}
			if( i != mItems.size() -1) // unless last
				stream.write( ",\n" );
			else
				stream.write( "\n" );
		}
		stream.write(indentstr);
		stream.write("]");
	}
	public static void saveStructuredDataForObject( Dispatch2 dsp, ArrayList<Dispatch2> stack, TextWriteStreamInterface stream, final String indentstr ) throws VariantException, TJSException {
		// check object recursion
		final int count = stack.size();
		for( int i = 0; i < count; i++ ) {
			Dispatch2 d = stack.get(i);
			if( d == dsp ) {
				// object recursion detected
				stream.write( "null /* object recursion detected */" );
				return;
			}
		}

		// determin dsp's object type
		DictionaryNI dicni;
		ArrayNI arrayni;
		if( dsp != null ) {
			dicni = (DictionaryNI) dsp.getNativeInstance( DictionaryClass.ClassID );
			if( dicni != null  ) {
				// dictionary
				stack.add(dsp);
				dicni.saveStructuredData( stack, stream, indentstr );
				stack.remove(stack.size()-1);
				return;
			} else {
				arrayni = (ArrayNI) dsp.getNativeInstance( ArrayClass.ClassID );
				if( arrayni != null ) {
					// array
					stack.add(dsp);
					arrayni.saveStructuredData(stack, stream, indentstr);
					stack.remove(stack.size()-1);
					return;
				} else {
					// other objects
					stream.write( "null /* (object) \"" ); // stored as a null
					Variant val = new Variant(dsp,dsp);
					stream.write( LexBase.escapeC(val.asString()) );
					stream.write( "\" */" );
					return;
				}
			}
		}
		stream.write( "null" );
	}
	public void assignStructure( Dispatch2 dsp, ArrayList<Dispatch2> stack ) throws TJSException, VariantException {
		// assign structured data from dsp

		ArrayNI arrayni = (ArrayNI)dsp.getNativeInstance(ArrayClass.ClassID);
		if( arrayni != null ) {
			// copy from array
			stack.add(dsp);
			try {
				mItems.clear();
				final int count = arrayni.mItems.size();
				for( int i = 0; i < count; i++ ) {
					Variant v = arrayni.mItems.get(i);
					if( v.isObject() ) {
						// object
						Dispatch2 dsp1 = v.asObject();
						// determin dsp's object type

						//DictionaryNI dicni = null;
						//ArrayNI arrayni1 = null;

						if( dsp1 != null && dsp1.getNativeInstance( DictionaryClass.ClassID ) != null ) {
							//dicni = (DictionaryNI)ni.mValue;
							// dictionary
							boolean objrec = false;
							final int scount = stack.size();
							for( int j = 0; j < scount; j++ ) {
								Dispatch2 d = stack.get(j);
								if( d == dsp1 ) {
									// object recursion detected
									objrec = true;
									break;
								}
							}
							if( objrec ) {
								mItems.add( new Variant() ); // becomes null
							} else {
								Dispatch2 newobj = TJS.createDictionaryObject();
								mItems.add( new Variant(newobj, newobj) );
								DictionaryNI newni;
								if( (newni = (DictionaryNI)newobj.getNativeInstance( DictionaryClass.ClassID )) != null ) {
									newni.assignStructure(dsp1, stack);
								}
							}
						} else if( dsp1 != null && dsp1.getNativeInstance(ArrayClass.ClassID) != null ) {
							// array
							boolean objrec = false;
							final int scount = stack.size();
							for( int j = 0; j < scount; j++ ) {
								Dispatch2 d = stack.get(j);
								if( d == dsp1 ) {
									// object recursion detected
									objrec = true;
									break;
								}
							}
							if( objrec ) {
								mItems.add( new Variant() ); // becomes null
							} else {
								Dispatch2 newobj = TJS.createArrayObject();
								mItems.add( new Variant(newobj, newobj) );
								ArrayNI newni;
								if( (newni = (ArrayNI)newobj.getNativeInstance( ArrayClass.ClassID )) != null ) {
									newni.assignStructure(dsp1, stack);
								}
							}
						} else {
							// other object types
							mItems.add( v );
						}
					} else {
						// others
						mItems.add( v );
					}
				}
			} finally {
				stack.remove( stack.size()-1 );
			}
		} else {
			throw new TJSException( Error.SpecifyDicOrArray );
		}
	}
}
