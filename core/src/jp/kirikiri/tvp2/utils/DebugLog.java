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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.ListIterator;

import jp.kirikiri.tjs2.ConsoleOutput;
import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tvp2.TVP;
import jp.kirikiri.tvp2.base.TVPSystem;
import jp.kirikiri.tvp2.msg.Message;
import jp.kirikiri.tvp2env.ApplicationSystem;
import jp.kirikiri.tvp2env.DefaultLogger;

public class DebugLog {
	static private boolean QUEUEING_LOG = true;
	static private boolean LOG_TIMESTAMP = true;

	static class LogItem {
		public String Log;
		public String Time;
		public LogItem( final String log, final String time ) {
			Log = log;
			Time = time;
		}
	}
	private LinkedList<LogItem> mLogDeque;
	private int mLogMaxLines;

	private boolean mAutoLogToFileOnError;
	private boolean mAutoClearLogOnError;
	private boolean mLoggingToFile;
	private String mLogLocation;
	private String mNativeLogLocation;
	private String mDumpOutFileName;

	private LogStreamHolder mLogStreamHolder;

	private int mLogToFileRollBack;
	private StringBuilder mImportantLogs;
	private StringBuilder mWorkBuilder;
	private StringBuilder mLastLogWorkBuilder;
	private ConsoleOutput mOnLog;

	private Calendar mCalendar;
	private int mPrevHour;
	private int mPrevMinute;
	private int mPrevSecond;
	private String mTimeBuf;

	public DebugLog() {
		String tmp;
		tmp = TVP.Properties.getProperty("log_queueing", "true");
		if( "true".equalsIgnoreCase(tmp) || "yes".equalsIgnoreCase(tmp) ) {
			QUEUEING_LOG = true;
		} else {
			QUEUEING_LOG = false;
		}

		tmp = TVP.Properties.getProperty("log_timestamp", "true");
		if( "true".equalsIgnoreCase(tmp) || "yes".equalsIgnoreCase(tmp) ) {
			LOG_TIMESTAMP = true;
		} else {
			LOG_TIMESTAMP = false;
		}

		mLogDeque = new LinkedList<LogItem>();
		mAutoLogToFileOnError = true;
		tmp = TVP.Properties.getProperty("log_to_file_roll_back", "100");
		mLogToFileRollBack = Integer.valueOf(tmp);
		tmp = TVP.Properties.getProperty("log_max_lines", "2048");
		mLogMaxLines = Integer.valueOf(tmp);
		mImportantLogs = new StringBuilder(128);
		mWorkBuilder = new StringBuilder(128);
		mLastLogWorkBuilder = new StringBuilder(128);

		mLogStreamHolder = new LogStreamHolder();

		mCalendar = Calendar.getInstance();
		mPrevHour = -1;
		mPrevMinute = -1;
		mPrevSecond = -1;

		mOnLog = new DefaultLogger();
	}
	public boolean enableLogLoacation() {
		return mLogLocation != null && mLogLocation.length() != 0;
	}
	public String getNativeLogLocation() { return mNativeLogLocation; }

	public void addLog( final String line ) {
		addLog( line, false );
	}
	public void addImportantLog( final String line ) {
		addLog( line, true );
	}
	public String getImportantLog() {
		if(mImportantLogs==null) return "";
		return mImportantLogs.toString();
	}
	public void addLog( final String line, boolean appendtoimportant ) {
		// add a line to the log.
		// exceeded lines over TVPLogMaxLines are eliminated.
		// this function is not thread-safe ...

		if(mLogDeque==null) return; // log system is shuttingdown
		if(mImportantLogs==null) return; // log system is shuttingdown

		if( LOG_TIMESTAMP ) {
			mCalendar.setTimeInMillis( System.currentTimeMillis() );

			int h = mCalendar.get( Calendar.HOUR_OF_DAY );
			int m = mCalendar.get( Calendar.MINUTE );
			int s = mCalendar.get( Calendar.SECOND );
			if( mPrevSecond != s || mPrevMinute != m || mPrevHour != h ) {
				mTimeBuf = String.format("%02d:%02d:%02d", h, m, s );
				mPrevHour = h;
				mPrevMinute = m;
				mPrevSecond = s;
			}
		}
		if( QUEUEING_LOG ) mLogDeque.addLast(new LogItem(line, mTimeBuf) );

		if( appendtoimportant ) {
			if( LOG_TIMESTAMP ) mImportantLogs.append(mTimeBuf);
			mImportantLogs.append(" ! ");
			mImportantLogs.append(line);
			mImportantLogs.append("\n");
		}

		if( QUEUEING_LOG )  {
			while( mLogDeque.size() >= mLogMaxLines+100 ) {
				for( int i = 0; i < 100; i++ ) {
					mLogDeque.removeFirst();
				}
			}
		}

		if( LOG_TIMESTAMP ) {
			mWorkBuilder.append(mTimeBuf);
			mWorkBuilder.append(' ');
		}
		mWorkBuilder.append(line);
		String buf = mWorkBuilder.toString();
		mWorkBuilder.delete( 0, mWorkBuilder.length() );

		if( mOnLog != null ) {
			mOnLog.print(buf);
		}

		if(mLoggingToFile) TVP.LogStreamHolder.log(buf);
	}

	public void setLogLocation( final String loc ) throws TJSException {
		mLogLocation = TVP.StorageMediaManager.normalizeStorageName(loc,null);

		String nativepath = TVP.StorageMediaManager.getLocallyAccessibleName(mLogLocation);
		if( nativepath.length() == 0 ) {
			mNativeLogLocation = null;
			mLogLocation = null;
		} else {
			char last = nativepath.charAt(nativepath.length()-1);
			if( last != File.separatorChar ) {
				mNativeLogLocation = nativepath + File.separatorChar;
			} else {
				mNativeLogLocation = nativepath;
			}
		}
		mLogStreamHolder.reopen();

		// check force logging option
		String forcelog = TVP.Properties.getProperty("forcelog", "no" );
		if( "no".equals(forcelog) == false ) {
			if("yes".equals(forcelog)) {
				mLoggingToFile = false;
				startLogToFile(false);
			} else if( "clear".equals(forcelog) ) {
				mLoggingToFile = false;
				startLogToFile(true);
			}
		}
		String logerror = TVP.Properties.getProperty("logerror", "yes" );
		if( "yes".equals(logerror) == false ) {
			if( "no".equals(logerror) ) {
				mAutoClearLogOnError = false;
				mAutoLogToFileOnError = false;
			} else if( "clear".equals(logerror) ) {
				mAutoClearLogOnError = true;
				mAutoLogToFileOnError = true;
			}
		}
	}
	private static final String SEPARATOR = "\n------------------------------------------------------------------------------\n";
	void startLogToFile(boolean clear) {
		if( mImportantLogs == null ) return; // log system is shuttingdown

		if(mLoggingToFile) return; // already logging
		if(clear) mLogStreamHolder.clear();

		// log last lines
		mLogStreamHolder.log( mImportantLogs.toString() );
		mLogStreamHolder.log( SEPARATOR );

		String content = getLastLog(mLogToFileRollBack);
		mLogStreamHolder.log(content);

		mLoggingToFile = true;
	}

	private String getLastLog( int n ) {
		if(mLogDeque==null) return ""; // log system is shuttingdown

		int size = mLogDeque.size();
		if(n > size) n = size;
		if(n==0) return "";

		ListIterator<LogItem> iter = mLogDeque.listIterator(size-n);
		while( iter.hasNext() ) {
			LogItem item = iter.next();
			mLastLogWorkBuilder.append( item.Time );
			mLastLogWorkBuilder.append( ' ' );
			mLastLogWorkBuilder.append( item.Log );
			mLastLogWorkBuilder.append( '\n' );
		}
		String buf = mLastLogWorkBuilder.toString();
		mLastLogWorkBuilder.delete(0, mLastLogWorkBuilder.length() );

		return buf;
	}
	public void startDump() throws TJSException {
		String path = ApplicationSystem.getNativeAppPath();
		char last = path.charAt(path.length()-1);
		if( last != '/' && last != '\\' ) {
			path += File.separatorChar;
		}
		path += "dump.txt";
		mDumpOutFileName = path;
		try {
			TVP.DumpOutputStream = new FileOutputStream( path, true );
			byte[] buf = {(byte)0xff,(byte)0xfe};
			TVP.DumpOutputStream.write(buf);
		} catch( FileNotFoundException e ) {
			Message.throwExceptionMessage( Message.CannotFindStorage, e.getMessage() );
		} catch (IOException e) {
		}
	}
	public void endDump() {
		if( TVP.DumpOutputStream != null ) {
			try {
				TVP.DumpOutputStream.close();
			} catch (IOException e) {
			}
			TVP.DumpOutputStream = null;

			addLog( "Dumped to " + mDumpOutFileName );
		}
	}
	public void onError() {
		if( mAutoLogToFileOnError) startLogToFile(mAutoClearLogOnError);
		// if(TVP.MainForm!=null) TVP.MainForm.notifySystemError();
	}
	public String getLogLocation() {
		return mLogLocation;
	}
	public boolean getAutoLogToFileOnError() {
		return mAutoLogToFileOnError;
	}
	public void setAutoLogToFileOnError(boolean b) {
		mAutoLogToFileOnError = b;
	}
	public boolean getAutoClearLogOnError() {
		return mAutoClearLogOnError;
	}
	public void setAutoClearLogOnError(boolean b) {
		mAutoClearLogOnError = b;
	}
}
