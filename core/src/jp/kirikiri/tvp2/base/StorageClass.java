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
package jp.kirikiri.tvp2.base;

import jp.kirikiri.tjs2.NativeClass;
import jp.kirikiri.tjs2.Error;
import jp.kirikiri.tjs2.Interface;
import jp.kirikiri.tjs2.NativeClassMethod;
import jp.kirikiri.tjs2.TJS;
import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tjs2.Variant;
import jp.kirikiri.tjs2.Dispatch2;
import jp.kirikiri.tjs2.VariantException;
import jp.kirikiri.tvp2.TVP;
import jp.kirikiri.tvp2env.FileSelector;

public class StorageClass extends NativeClass {
	static int mClassID = -1;
	static private final String CLASS_NAME = "Storage";

	public StorageClass() throws VariantException, TJSException {
		super(CLASS_NAME);
		final int NCM_CLASSID = TJS.registerNativeClass(CLASS_NAME);
		setClassID( NCM_CLASSID );
		mClassID = NCM_CLASSID;

		registerNCM( "finalize", TVP.ReturnOKMethod, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "addAutoPath", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				if(param.length < 1) return Error.E_BADPARAMCOUNT;
				String path = param[0].asString();
				TVP.AutoPath.addAutoPath(path);
				if( result != null ) result.clear();
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "removeAutoPath", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				if(param.length < 1) return Error.E_BADPARAMCOUNT;
				String path = param[0].asString();
				TVP.AutoPath.removeAutoPath(path);
				if( result != null ) result.clear();
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "getFullPath", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				if(param.length < 1) return Error.E_BADPARAMCOUNT;
				String path = param[0].asString();
				if( result != null ) {
					result.set( TVP.StorageMediaManager.normalizeStorageName(path,null) );
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "getPlacedPath", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				if( param.length < 1 ) return Error.E_BADPARAMCOUNT;
				String path = param[0].asString();
				if( result != null ) {
					result.set( Storage.getPlacedPath(path) );
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "isExistentStorage", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				if( param.length < 1 ) return Error.E_BADPARAMCOUNT;
				String path = param[0].asString();
				if( result != null ) {
					result.set( Storage.isExistentStorage(path) ? 1 : 0 );
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "extractStorageExt", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				if( param.length < 1 ) return Error.E_BADPARAMCOUNT;
				String path = param[0].asString();
				if( result != null ) {
					result.set( Storage.extractStorageExt(path) );
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "extractStorageName", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				if( param.length < 1 ) return Error.E_BADPARAMCOUNT;
				String path = param[0].asString();
				if( result != null ) {
					result.set( Storage.extractStorageName(path) );
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "extractStoragePath", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				if( param.length < 1 ) return Error.E_BADPARAMCOUNT;
				String path = param[0].asString();
				if( result != null ) {
					result.set( Storage.extractStoragePath(path) );
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "chopStorageExt", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				if( param.length < 1 ) return Error.E_BADPARAMCOUNT;
				String path = param[0].asString();
				if( result != null ) {
					result.set( Storage.chopStorageExt(path) );
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "clearArchiveCache", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) {
				TVP.ArchiveCache.clear();
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "searchCD", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				if( param.length < 1 ) return Error.E_BADPARAMCOUNT;
				if( result != null ) {
					result.set( 0 ); // Alway false
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "getLocalName", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				if( param.length < 1 ) return Error.E_BADPARAMCOUNT;
				String path = param[0].asString();
				if( result != null ) {
					String str = TVP.StorageMediaManager.normalizeStorageName(path,null);
					str = TVP.StorageMediaManager.getLocalName(str);
					result.set( str );
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );

		registerNCM( "selectFile", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				if( param.length < 1 ) return Error.E_BADPARAMCOUNT;
				Dispatch2 dsp =  param[0].asObject();
				boolean res = FileSelector.selectFile(dsp);
				if( result != null ) {
					result.set( res ? 1 : 0 );
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, Interface.STATICMEMBER );
	}
}
