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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.app.ProgressDialog;
import android.os.AsyncTask;

public class MD5CheckTask extends AsyncTask<String, Integer, byte[]> {

	private BaseActivity mActivity = null;
	private ProgressDialog mProgressDialog = null;

	public MD5CheckTask( BaseActivity activity ) {
		mActivity = activity;
	}

	@Override
	protected void onPreExecute() {
		mProgressDialog = new ProgressDialog(mActivity);
		mProgressDialog.setProgressStyle( ProgressDialog.STYLE_SPINNER );
		mProgressDialog.setMessage( "チェック中..." );
		mProgressDialog.setCancelable(false);
		mProgressDialog.show();
	}

	@Override
	protected byte[] doInBackground(String... params) {
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			File target = new File(params[0]);
			InputStream in = new FileInputStream(target); // ストレージから取得するようにする
			byte[] buff = new byte[4096];
			int len = 0;
			while ((len = in.read(buff, 0, buff.length)) >= 0 && isCancelled() == false ) {
				digest.update(buff, 0, len);
			}
			in.close();
			if( isCancelled() ) {
				// キャンセルされた場合、削除してしまう。
				if( target.exists() ) {
					target.delete();
				}
				return null;
			}
			byte[] hash = digest.digest();
			digest.reset();
			return hash;
		} catch (IOException e) {
			return null;
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
	}

	@Override
	protected void onPostExecute(byte[] result) {
		mProgressDialog.dismiss();
		mActivity.onCalcMD5( result );
	}
	@Override
	protected void onCancelled() {
		mProgressDialog.dismiss();
	}

}
