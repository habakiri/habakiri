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
package jp.kirikiri.tvp2env;

import java.io.File;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import jp.kirikiri.tjs2.Interface;
import jp.kirikiri.tjs2.Dispatch2;
import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tjs2.Variant;
import jp.kirikiri.tvp2.TVP;

public class FileSelector {

	public static boolean selectFile( Dispatch2 params ) throws TJSException {
		Variant val = new Variant();

		JFileChooser filechooser = new JFileChooser();

		int hr;
		hr = params.propGet(Interface.MEMBERMUSTEXIST, "filter", val, params );
		if( hr >= 0 ) {
			ArrayList<String> filtterlist = new ArrayList<String>();
			if( val.isObject() != true ) {
				filtterlist.add( val.asString() );
			} else {
				Dispatch2 array = val.asObject();
				int count;
				Variant tmp = new Variant();
				hr = array.propGet( Interface.MEMBERMUSTEXIST, "count", tmp, array );
				if( hr >= 0 )
					count = tmp.asInteger();
				else
					count = 0;

				for( int i = 0; i < count; i++ ) {
					hr = array.propGetByNum( Interface.MEMBERMUSTEXIST, i, tmp, array );
					if( hr >= 0 ) {
						filtterlist.add( tmp.asString() );
					}
				}
			}
			final int count = filtterlist.size();
			StringBuilder builder = new StringBuilder(512); // description
			ArrayList<String> extlist = new ArrayList<String>();
			for( int i = 0; i < count; i++ ) {
				String fileter = filtterlist.get(i);
				String[] desc = fileter.split("|");
				if( desc.length >= 2 ) {
					builder.append(desc[0]);
					String[] ext = desc[1].split(";");
					final int extcount = ext.length;
					for( int j = 0; j < extcount; j++ ) {
						int last = ext[j].lastIndexOf('.');
						if( last != -1 ) {
							String e = ext[j].substring(last);
							if( e.indexOf('*') == -1 ) {
								extlist.add(e);
							}
						}
					}
				}
			}
			if( extlist.size() > 0 ) {
				FileNameExtensionFilter filefilter = new FileNameExtensionFilter(builder.toString(),(String[]) extlist.toArray());
				filechooser.setFileFilter(filefilter);
			}
			builder = null;
			filtterlist = null;
			extlist = null;
		}
		/*
		int filterIndex = 0;
		hr = params.propGet(Interface.MEMBERMUSTEXIST, "filterIndex", val, params );
		if( hr >= 0 ) {
			filterIndex = val.asInteger();
		}
		*/

		String filename;
		hr = params.propGet(Interface.MEMBERMUSTEXIST, "name", val, params );
		if( hr >= 0 ) {
			String lname = val.asString();
			if( lname != null && lname.length() != 0 ) {
				lname = TVP.StorageMediaManager.normalizeStorageName(lname,null);
				filename = TVP.StorageMediaManager.getLocalName(lname);
				filechooser.setSelectedFile( new File(filename) );
			}
		}

		hr = params.propGet(Interface.MEMBERMUSTEXIST, "initialDir", val, params );
		if( hr >= 0 ) {
			String lname = val.asString();
			if( lname != null && lname.length() != 0 ) {
				lname = TVP.StorageMediaManager.normalizeStorageName(lname,null);
				String initDir = TVP.StorageMediaManager.getLocalName(lname);
				filechooser.setCurrentDirectory( new File(initDir) );
			}
		}

		hr = params.propGet(Interface.MEMBERMUSTEXIST, "initialDir", val, params );
		if( hr >= 0 ) {
			String title = val.asString();
			filechooser.setDialogTitle(title);
		}
		boolean issave = false;
		hr = params.propGet(Interface.MEMBERMUSTEXIST, "save", val, params );
		if( hr >= 0 ) {
			issave = val.asBoolean();
		}
		/*
		hr = params.propGet(Interface.MEMBERMUSTEXIST, "defaultExt", val, params );
		if( hr >= 0 ) {
			String ext = val.asString();
		}
		*/


		int selected;
		if( issave ) {
			selected = filechooser.showSaveDialog(null);
		} else {
			selected = filechooser.showOpenDialog(null);
		}
		if( selected == JFileChooser.APPROVE_OPTION ) {
			File file = filechooser.getSelectedFile();

			// file name
			filename = TVP.StorageMediaManager.normalizeStorageName( file.getAbsolutePath(), null );
			val.set(filename);
			params.propSet( Interface.MEMBERENSURE, "name", val, params);
			return true;
		}
		return false;
	}
}
