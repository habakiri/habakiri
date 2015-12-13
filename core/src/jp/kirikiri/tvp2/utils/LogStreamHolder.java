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
package jp.kirikiri.tvp2.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import jp.kirikiri.tvp2.TVP;
import jp.kirikiri.tvp2.base.SystemInitializer;

public class LogStreamHolder {

	PrintWriter mStream;
	boolean mAlive;
	boolean mOpenFailed;

	public LogStreamHolder() {
		//mStream = null;
		mAlive = true;
		//mOpenFailed = false;
	}

	protected void finalize() {
		if(mStream!=null){
			mStream.close();
		}
		mAlive = false;
	}
	 private static boolean checkBeforeWritefile(File file){
		  if( file.exists() ){
			  if( file.isFile() && file.canWrite() ){
				  return true;
			  }
			  return false;
		  }
		  return true;
	}
	private static final String SEPARATOR = "\n\n\n==============================================================================\n==============================================================================\n";
	private void open( boolean append ) {
		if(mOpenFailed) return; // no more try

		try {
			String filename = null;
			if( TVP.DebugLog.enableLogLoacation() == false) {
				mStream = null;
				mOpenFailed = true;
			} else {
				// no log location specified
				filename = TVP.DebugLog.getNativeLogLocation() + "\\krkr.console.log";
				SystemInitializer.ensureDataPathDirectory();

				File file = new File(filename);

				if( checkBeforeWritefile(file) ) {
					boolean appendbom = file.exists() == false;
					mStream = new PrintWriter(new BufferedWriter(new FileWriter(file,append)));
					if( mStream != null && appendbom ) {
						mStream.write(0xff);
						mStream.write(0xfe);
					}
				}
				if(mStream==null) mOpenFailed = true;
			}

			if(mStream!=null) {
				log(SEPARATOR);

				Calendar cal = Calendar.getInstance();
				Date date = cal.getTime();
				DateFormat formatter = DateFormat.getDateInstance();
				String timebuf = formatter.format(date);
				formatter = null;
				date = null;
				cal = null;

				StringBuffer buffer = new StringBuffer(32);
				buffer.append("Logging to ");
				buffer.append( filename );
				buffer.append(" started on ");
				buffer.append( timebuf );
				log( buffer.toString() );
				buffer = null;

			}
		} catch( IOException e ) {
			mOpenFailed = true;
		}
	}

	/**
	 * clear log text
	 */
	public void clear() {
		if( mStream != null ) mStream.close();
		open( false );
	}
	public void log( final String text ) {
		if(mStream==null) open(true);

		if(mStream!=null) {
			mStream.print(text);
			mStream.print('\n');
			mStream.flush();
		}
	}

	/**
	 * reopen log stream
	 */
	public void reopen() {
		if(mStream!=null) mStream.close();
		mStream = null;
		mAlive = false;
		mOpenFailed = false;
	}
}
