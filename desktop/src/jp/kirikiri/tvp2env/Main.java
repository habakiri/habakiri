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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Iterator;
import java.util.Properties;

import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tjs2.VariantException;
import jp.kirikiri.tvp2.TVP;
import jp.kirikiri.tvp2.base.ScriptsClass;
import jp.kirikiri.tvp2.base.SystemInitializer;
import jp.kirikiri.tvp2.base.TVPSystem;
import jp.kirikiri.tvp2.utils.DebugClass;

class Main {
	private static final String PROP_FILE_NAME = "engine.properties";

	public static void main(String[] args) {
		processArg( args );
		loadProperties(PROP_FILE_NAME);

		// dumpProperties();
		try {
			ApplicationSystem app = new ApplicationSystem(); // 自分でTVPに登録する
			TVP.initialize();
			ScriptsClass.initScriptEnging();

			// banner
			DebugClass.addImportantLog( "Program started on " + TVPSystem.getOSName() );
			DebugClass.addImportantLog( "JVM : " + TVPSystem.getJVMName() );
			DebugClass.addImportantLog( "JRE : " + TVPSystem.getJREName() );

			SystemInitializer.systemInitialize();
			ScriptsClass.initializeStartupScript();
			EventHandleThread t = new EventHandleThread();
			t.start();
		} catch (VariantException e) {
			TVP.DebugLog.onError();
			e.printStackTrace();
			ApplicationSystem.messageBox(e.getMessage(),"Error",0);
		} catch (TJSException e) {
			TVP.DebugLog.onError();
			e.printStackTrace();
			ApplicationSystem.messageBox(e.getMessage(),"Error",0);
		} catch( Exception e ) {
			TVP.DebugLog.onError();
			e.printStackTrace();
			ApplicationSystem.messageBox(e.getMessage(),"Error",0);
		}
	}
	private static void processArg( String[] args ) {
		TVP.Properties = new Properties();
		final int count = args.length;
		for( int i = 0; i < count; i++ ) {
			String arg = args[i];
			if( arg.charAt(0) == '-' ) {
				String name = arg.substring(1);
				int index = name.indexOf('=');
				if( index != -1 && name.length() > (index+1) ) {
					String value = name.substring(index+1);
					name = name.substring(0,index);
					TVP.Properties.setProperty(name, value);
				} else {
					TVP.Properties.setProperty(name, "no value" );
				}
			}
		}
	}
	private static void loadProperties( final String filename ) {
		if( TVP.Properties == null ) TVP.Properties = new Properties();
		try {
			@SuppressWarnings("rawtypes")
			Class c = TVP.class;
			InputStream is = c.getResourceAsStream(filename);
			if( is == null ) {
				is = new FileInputStream(filename);
			}
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			TVP.Properties.load(br);
			br.close();
			is.close();
		} catch (IOException e) {
		}
	}
	static void dumpProperties() {
		for( @SuppressWarnings("rawtypes")
		Iterator i = TVP.Properties.keySet().iterator(); i.hasNext();) {
			String key = (String) i.next();
			String val = TVP.Properties.getProperty(key);
			System.out.println(key + "=" + val);
		}
	}
}
