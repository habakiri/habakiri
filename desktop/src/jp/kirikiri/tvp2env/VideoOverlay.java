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

import jp.kirikiri.tjs2.BinaryStream;
import jp.kirikiri.tvp2.visual.Rect;

public class VideoOverlay {

	public void open(String name, BinaryStream stream) {
		// TODO 自動生成されたメソッド・スタブ
	}

	public void setVisible(boolean mVisible) {
		// TODO 自動生成されたメソッド・スタブ
	}

	public void setRect(Rect rect) {
		// TODO 自動生成されたメソッド・スタブ
	}

	public void close() {
		// TODO 自動生成されたメソッド・スタブ
	}

	public void play() {
		// TODO 自動生成されたメソッド・スタブ
	}

	public void stop() {
		// TODO 自動生成されたメソッド・スタブ
	}

	public void pause() {
		// TODO 自動生成されたメソッド・スタブ
	}

	public void rewind() {
		// TODO 自動生成されたメソッド・スタブ
	}

	public void setStopFrame(int f) {
		// TODO 自動生成されたメソッド・スタブ
	}

	public void setDefaultStopFrame() {
		// TODO 自動生成されたメソッド・スタブ
	}

	public int getStopFrame() {
		// TODO 自動生成されたメソッド・スタブ
		return 0;
	}

	public void setPosition(long p) {
		// TODO 自動生成されたメソッド・スタブ
	}

	public long getPosition() {
		// TODO 自動生成されたメソッド・スタブ
		return 0;
	}

	public void setFrame(int f) {
		// TODO 自動生成されたメソッド・スタブ
	}

	public int getFrame() {
		// TODO 自動生成されたメソッド・スタブ
		return 0;
	}

	public double getFPS() {
		// TODO 自動生成されたメソッド・スタブ
		return 0;
	}

	public int getNumberOfFrame() {
		// TODO 自動生成されたメソッド・スタブ
		return 0;
	}

	public long getTotalTime() {
		// TODO 自動生成されたメソッド・スタブ
		return 0;
	}

	public void setLoop(boolean l) {
		// TODO 自動生成されたメソッド・スタブ
	}

	public boolean isOpen() {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

	public double getPlayRate() {
		// TODO 自動生成されたメソッド・スタブ
		return 1.0;
	}

	public void setPlayRate(double r) {
		// TODO 自動生成されたメソッド・スタブ
	}

	public int getAudioBalance() {
		// TODO 自動生成されたメソッド・スタブ
		return 0;
	}

	public void setAudioBalance(int b) {
		// TODO 自動生成されたメソッド・スタブ
	}

	public int getAudioVolume() {
		// TODO 自動生成されたメソッド・スタブ
		return 0;
	}

	public void setAudioVolume(int v) {
		// TODO 自動生成されたメソッド・スタブ
	}

	public int getNumberOfAudioStream() {
		// TODO 自動生成されたメソッド・スタブ
		return 0;
	}

	public void selectAudioStream(int n) {
		// TODO 自動生成されたメソッド・スタブ
	}

	public int getEnableAudioStreamNum() {
		// TODO 自動生成されたメソッド・スタブ
		return 0;
	}

	public void disableAudioStream() {
		// TODO 自動生成されたメソッド・スタブ
	}

	public int getNumberOfVideoStream() {
		// TODO 自動生成されたメソッド・スタブ
		return 0;
	}

	public void selectVideoStream(int n) {
		// TODO 自動生成されたメソッド・スタブ
	}

	public int getEnableVideoStreamNum() {
		// TODO 自動生成されたメソッド・スタブ
		return 0;
	}

	public void setMixingMovieAlpha(double a) {
		// TODO 自動生成されたメソッド・スタブ
	}

	public double getMixingMovieAlpha() {
		// TODO 自動生成されたメソッド・スタブ
		return 0;
	}

	public void setMixingMovieBGColor(int col) {
		// TODO 自動生成されたメソッド・スタブ
	}

	public int getMixingMovieBGColor() {
		// TODO 自動生成されたメソッド・スタブ
		return 0;
	}

	public int getVideoWidth() {
		// TODO 自動生成されたメソッド・スタブ
		return 0;
	}

	public int getVideoHeight() {
		// TODO 自動生成されたメソッド・スタブ
		return 0;
	}

	public void open(String name, BinaryStream stream, int mMode,
			boolean mVisible) {
		// TODO 自動生成されたメソッド・スタブ

	}

	public void setWindow(WindowForm form) {
		// TODO 自動生成されたメソッド・スタブ

	}

	public static void initialize() {
		// TODO 自動生成されたメソッド・スタブ

	}

	public static void finalizeApplication() {
		// TODO 自動生成されたメソッド・スタブ

	}

}
