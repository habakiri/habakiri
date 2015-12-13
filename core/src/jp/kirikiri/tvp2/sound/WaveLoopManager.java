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
package jp.kirikiri.tvp2.sound;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * .sli ファイルを読んで、ラベルを得る事のみに使用している。
 *
 */
public class WaveLoopManager {

	private static final int WL_MAX_FLAGS = 16;
	private static final int WL_MAX_FLAG_VALUE = 9999;

	private int[] mFlags;
	private boolean mFlagsModifiedByLabelExpression; // true if the flags are modified by EvalLabelExpression
	private ArrayList<WaveLoopLink> mLinks;
	private ArrayList<WaveLabel> mLabels;

	private Object DataCS;

	//private int nShortCrossFadeHalfSamples;
		// WL_SMOOTH_TIME_HALF in sample unit

	//private boolean mLooping;

	private long mPosition; // decoding position

	private byte[] mCrossFadeSamples; // sample buffer for crossfading
	/*
	private int mCrossFadeLen;
	private int mCrossFadePosition;
	*/

	private boolean mIsLinksSorted; // false if links are not yet sorted
	private boolean mIsLabelsSorted; // false if labels are not yet sorted

	private boolean mIgnoreLinks; // decode the samples with ignoring links

	public WaveLoopManager() {
		mFlags = new int[WL_MAX_FLAGS];
		mLinks = new ArrayList<WaveLoopLink>();
		mLabels = new ArrayList<WaveLabel>();

		DataCS = new Object();
		/*
		mPosition = 0;
		mIsLinksSorted = false;
		mIsLabelsSorted = false;
		mCrossFadeSamples = null;
		mCrossFadeLen = 0;
		mCrossFadePosition = 0;
		mIgnoreLinks = false;
		mLooping = false;

		clearFlags();
		mFlagsModifiedByLabelExpression = false;
		*/
	}
	public int getFlag( int index ) {
		synchronized (this) {
			return mFlags[index];
		}
	}
	public void copyFlags( int[] dest ) {
		synchronized (this) {
			// copy flags into dest, and clear FlagsModifiedByLabelExpression
			if( dest.length >= mFlags.length ) {
				System.arraycopy(mFlags, 0, dest, 0, mFlags.length);
			}
			mFlagsModifiedByLabelExpression = false;
		}
	}
	public boolean getFlagsModifiedByLabelExpression() {
		synchronized (this) {
			return mFlagsModifiedByLabelExpression;
		}
	}
	public void setFlag( int index, int f ) {
		synchronized (this) {
			if(f < 0) f = 0;
			if(f > WL_MAX_FLAG_VALUE) f = WL_MAX_FLAG_VALUE;
			mFlags[index] = f;
		}
	}
	public void clearFlags() {
		synchronized (this) {
			for( int i = 0; i < WL_MAX_FLAGS; i++)
				mFlags[i] = 0;
		}
	}
	public void clearLinksAndLabels() {
		// clear links and labels
		synchronized (this) {
			mLabels.clear();
			mLinks.clear();
			mIsLinksSorted = false;
			mIsLabelsSorted = false;
		}
	}
	public final ArrayList<WaveLoopLink> getLinks() {
		synchronized (this) {
			return mLinks;
		}
	}
	public final ArrayList<WaveLabel> getLabels() {
		synchronized (this) {
			if(!mIsLabelsSorted) {
				Collections.sort(mLabels, new Comparator<WaveLabel>(){
					@Override
					public int compare(WaveLabel o1, WaveLabel o2) {
						return (int) (o1.Position - o2.Position);
					}
				});
				mIsLabelsSorted = true;
			}
			return mLabels;
		}
	}
	public void setLinks( final ArrayList<WaveLoopLink> links ) {
		synchronized (this) {
			mLinks = links;
			mIsLinksSorted = false;
		}
	}
	public void setLabels( final ArrayList<WaveLabel> labels ) {
		synchronized (this) {
			mLabels = labels;
			mIsLabelsSorted = false;
		}
	}
	public boolean getIgnoreLinks() {
		synchronized (DataCS ) {
			return mIgnoreLinks;
		}
	}
	public void setIgnoreLinks( boolean b ) {
		synchronized (DataCS ) {
			mIgnoreLinks = b;
		}
	}
	public long getPosition() {
		// we cannot assume that the 64bit data access is truely atomic on 32bit machines.
		synchronized (this) {
			return mPosition;
		}
	}
	public void setPosition( long pos ) {
		synchronized (DataCS ) {
			mPosition = pos;
			clearCrossFadeInformation();
			//Decoder->SetPosition(pos);
		}
	}
	/**
	 * 現状対象ラベルを得るのみ
	 */
	public void decode( long position, int samples, ArrayList<WaveLabel> labels ) {
		getLabelAt( position, position+samples, labels );
	}

	private void getLabelAt( long from, long to, ArrayList<WaveLabel> labels ) {
		synchronized (this) {
			if(mLabels.size() == 0) return; // no labels found
			if(!mIsLabelsSorted) {
				Collections.sort(mLabels, new Comparator<WaveLabel>(){
					@Override
					public int compare(WaveLabel o1, WaveLabel o2) {
						return (int) (o1.Position - o2.Position);
					}
				});
				mIsLabelsSorted = true;
			}

			// search nearest label using binary search
			int s = 0, e = mLabels.size();
			while(e - s > 1) {
				int m = (s+e)/2;
				if( mLabels.get(m).Position <= from)
					s = m;
				else
					e = m;
			}

			if(s < mLabels.size()-1 && mLabels.get(s).Position < from) s++;

			if( s >= mLabels.size() || mLabels.get(s).Position < from) {
				// no labels available
				return;
			}

			// rewind while the label position is the same
			long pos = mLabels.get(s).Position;
			while( true ) {
				if(s >= 1 && mLabels.get(s-1).Position == pos)
					s--;
				else
					break;
			}

			// search labels
			for( ; s < mLabels.size(); s++ ) {
				pos = mLabels.get(s).Position;
				if( pos >= from && pos < to)
					labels.add(mLabels.get(s));
				else
					break;
			}
		}
	}


	private void clearCrossFadeInformation() {
		if( mCrossFadeSamples != null ) mCrossFadeSamples = null;
	}
	public boolean readInformation( String p ) {
		synchronized (this) {
			// read information from 'p'
			if( p == null ) return false;

			final int count = p.length();
			mLinks.clear();
			mLabels.clear();
			mIsLinksSorted = false;
			mIsLabelsSorted = false;

			long[] ret = new long[1];
			// check version
			if( p.charAt(0) != '#') {
				// old sli format
				int p_length = p.indexOf("LoopLength=");
				int p_start  = p.indexOf("LoopStart=");
				if( p_length == -1 || p_start == -1 ) return false; // read error
				WaveLoopLink link = new WaveLoopLink();
				link.Smooth = false;
				link.Condition = WaveLoopLink.llcNone;
				link.RefValue = 0;
				link.CondVar = 0;
				long start;
				long length;
				if( getInt64(p, p_length + 11, ret) == false ) return false;
				length = ret[0];
				if( getInt64(p, p_start + 10, ret) == false ) return false;
				start = ret[0];
				link.From = start + length;
				link.To = start;
				mLinks.add(link);
			} else {
				// sli v2.0+
				if( p.startsWith("#2.00") == false )
					return false; // version mismatch

				int i = 0;
				while( true ) {
					if(( i == 0 || p.charAt(i-1) == '\n') && p.charAt(i) == '#') {
						// line starts with '#' is a comment
						// skip the comment
						while( i < count && p.charAt(i) != '\n' ) i++;
						if( i >= count ) break;
						i++;
						continue;
					}

					// skip white space
					while( i < count ) {
						char c = p.charAt(i);
						if( c != ' ' && c != '\f' && c != '\n' && c != '\r' && c != '\t' && c != 0x0B /* \v */) {
							break;
						}
						i++;
					}
					if( i >= count ) break;

					// read id (Link or Label)
					if( (i+5) < count ) {
						String link = p.substring( i, i+4);
						if( "Link".equalsIgnoreCase(link) && Character.isLetter(p.charAt(i+4)) == false ) {
							i += 4;
							// skip white space
							while( i < count ) {
								char c = p.charAt(i);
								if( c != ' ' && c != '\f' && c != '\n' && c != '\r' && c != '\t' && c != 0x0B /* \v */) {
									break;
								}
								i++;
							}
							if( i >= count ) return false;

							WaveLoopLink linkData = new WaveLoopLink();
							i = readLinkInformation(p, i, linkData);
							if( i < 0 ) return false;
							mLinks.add(linkData);
						} else if( (i+6) < count ) {
							String label = p.substring( i, i+5);
							if( "Label".equalsIgnoreCase(label) && Character.isLetter(p.charAt(i+5)) == false ) {
								i += 5;
								// skip white space
								while( i < count ) {
									char c = p.charAt(i);
									if( c != ' ' && c != '\f' && c != '\n' && c != '\r' && c != '\t' && c != 0x0B /* \v */) {
										break;
									}
									i++;
								}
								if( i >= count ) return false;
								WaveLabel labelData = new WaveLabel();
								i = readLabelInformation(p, i, labelData);
								if( i < 0 ) return false;
								mLabels.add(labelData);
							} else {
								return false;
							}
						} else {
							return false;
						}
					} else {
						return false;
					}

					// skip white space
					while( i < count ) {
						char c = p.charAt(i);
						if( c != ' ' && c != '\f' && c != '\n' && c != '\r' && c != '\t' && c != 0x0B /* \v */) {
							break;
						}
						i++;
					}
					if( i >= count ) break;
				}
			}

			return true; // done
		}
	}



	private static int readLabelInformation(String p, int i, WaveLabel label ) {
		// read label information from 'p'.
		// p must point '{' , which indicates start of the block.
		final int count = p.length();
		if( i >= count ) return -1;
		if( p.charAt(i) != '{') return -1;
		i++;
		if( i >= count ) return -1;

		String[] name = new String[1];
		String[] value = new String[1];
		long[] lv = new long[1];
		do {
			// get one token from 'p'
			i = getEntityToken(p, i, name, value);
			if( i < 0 ) return -1;

			String n = name[0];
			if( "Position".equalsIgnoreCase(n) ) {
				if(!getInt64(value[0], 0, lv))
					return -1;
				label.Position = lv[0];
			} else if( "Name".equalsIgnoreCase(n) ) {
				label.Name = value[0];
			} else {
				return -1;
			}

			// skip space
			while( i < count ) {
				char c = p.charAt(i);
				if( c != ' ' && c != '\f' && c != '\n' && c != '\r' && c != '\t' && c != 0x0B /* \v */) {
					break;
				}
				i++;
			}
			if( i >= count ) return -1;

			// check ';'. note that this will also be a null, if no delimiters are used
			if( p.charAt(i) != ';' ) return -1;
			i++;
			if( i >= count ) return -1;

			// skip space
			while( i < count ) {
				char c = p.charAt(i);
				if( c != ' ' && c != '\f' && c != '\n' && c != '\r' && c != '\t' && c != 0x0B /* \v */) {
					break;
				}
				i++;
			}
			if( i >= count ) return -1;

			// check '}'
			if( p.charAt(i) == '}') break;
		} while(true);

		i++;
		return i;
	}



	private static int readLinkInformation(String p, int i, WaveLoopLink link ) {
		// read link information from 'p'.
		// p must point '{' , which indicates start of the block.
		final int count = p.length();
		if( i >= count ) return -1;
		if( p.charAt(i) != '{') return -1;

		i++;
		if(i >= count ) return -1;

		String[] name = new String[1];
		String[] value = new String[1];
		long[] lv = new long[1];
		boolean[] bv = new boolean[1];
		int [] iv = new int [1];
		do {
			// get one token from 'p'
			i = getEntityToken(p, i, name, value);
			if( i < 0 ) return -1;

			String n = name[0];
			if( "From".equalsIgnoreCase(n) ) {
				if(!getInt64( value[0], 0, lv)) {
					return -1;
				}
				link.From = lv[0];
			} else if( "To".equalsIgnoreCase(n) ) {
				if(!getInt64( value[0], 0, lv)) {
					return -1;
				}
				link.To = lv[0];
			} else if( "Smooth".equalsIgnoreCase(n) ) {
				if(!getBool(value[0], 0, bv))
					return -1;
				link.Smooth = bv[0];
			} else if( "Condition".equalsIgnoreCase(n) ) {
				if(!getCondition(value[0], 0, iv))
					return -1;
				link.Condition = iv[0];
			} else if( "RefValue".equalsIgnoreCase(n) ) {
				if(!getInt(value[0], 0, iv))
					return -1;
				link.RefValue = iv[0];
			} else if( "CondVar".equalsIgnoreCase(n) ) {
				if(!getInt(value[0], 0, iv))
					return -1;
				link.CondVar = iv[0];
			} else {
				return -1;
			}

			// skip space
			while( i < count ) {
				char c = p.charAt(i);
				if( c != ' ' && c != '\f' && c != '\n' && c != '\r' && c != '\t' && c != 0x0B /* \v */) {
					break;
				}
				i++;
			}
			if( i >= count ) return -1;

			// check ';'. note that this will also be a null, if no delimiters are used
			if( p.charAt(i) != ';' ) return -1;
			i++;
			if( i >= count ) return -1;

			// skip space
			while( i < count ) {
				char c = p.charAt(i);
				if( c != ' ' && c != '\f' && c != '\n' && c != '\r' && c != '\t' && c != 0x0B /* \v */) {
					break;
				}
				i++;
			}
			if( i >= count ) return -1;

			// check '}'
			if( p.charAt(i) != '}' ) break;
		} while(true);
		i++;
		return i;
	}

	private static int getEntityToken( String p, int i, String[] name, String[] value ) {
		final int count = p.length();

		// get name=value string at 'p'.
		// returns whether the token can be got or not.
		// on success, *id will point start of the name, and value will point
		// start of the value. the buffer given by 'start' will be destroied.

		int namelast;
		int valuelast;
		char delimiter = '\0';

		// skip preceeding white spaces
		while( i < count ) {
			char c = p.charAt(i);
			if( c != ' ' && c != '\t' && c != '\f' && c != '\n' && c != '\r' && c != 0x0B /* \v */) {
				break;
			}
			i++;
		}
		if( i >= count ) return -1;

		// p will now be a name
		int namestart = i;

		// find white space or '='
		while( i < count ) {
			char c = p.charAt(i);
			if( c == ' ' || c == '=' || c == '\t' || c == '\f' || c == '\n' || c == '\r' || c == 0x0B /* \v */ ) {
				break;
			}
			i++;
		}
		if( i >= count ) return -1;
		namelast = i;

		// skip white space
		while( i < count ) {
			char c = p.charAt(i);
			if( c != ' ' && c != '\t' && c != '\f' && c != '\n' && c != '\r' && c != 0x0B /* \v */) {
				break;
			}
			i++;
		}
		if( i >= count ) return -1;

		// is current pointer pointing '='  ?
		if(p.charAt(i) != '=') return -1;

		// step pointer
		i++;
		if( i >= count ) return -1;

		// skip white space
		while( i < count ) {
			char c = p.charAt(i);
			if( c != ' ' && c != '\t' && c != '\f' && c != '\n' && c != '\r' && c != 0x0B /* \v */) {
				break;
			}
			i++;
		}
		if( i >= count ) return -1;

		// find delimiter
		if( p.charAt(i) == '\'') {delimiter = p.charAt(i); i++; }
		if( i >= count ) return -1;

		// now p will be start of value
		int valuestart = i;

		// find delimiter or white space or ';'
		if( delimiter == '\0' ) {
			while( i < count ) {
				char c = p.charAt(i);
				if( c == ' ' || c == ';' || c == '\t' || c == '\f' || c == '\n' || c == '\r' || c == 0x0B /* \v */ ) {
					break;
				}
				i++;
			}
		} else {
			while( i < count ) {
				char c = p.charAt(i);
				if( c == delimiter ) {
					break;
				}
				i++;
			}
		}

		// remember value last point
		valuelast = i;

		// skip last delimiter
		if( i < count && p.charAt(i) == delimiter ) i++;

		// put null terminator
		name[0] = p.substring(namestart,namelast);
		value[0] = p.substring(valuestart,valuelast);

		// finish
		return i;
	}
	private static boolean getBool(String s, int idx, boolean[] ret ) {
		// convert string to boolean
		final int count = s.length();
		if( idx < count ) {
			char c = s.charAt(idx);
			if( c == 'T' || c == 't' ) {
				if( (idx+4) > count ) return false;
				String v = s.substring( idx+1, idx+4 );
				if( "rue".equalsIgnoreCase(v) ) {
					ret[0] = true;
					return true;
				}
			} else if( c == 'F' || c == 'f') {
				if( (idx+5) > count ) return false;
				String v = s.substring( idx+1, idx+5 );
				if( "alse".equalsIgnoreCase(v) ) {
					ret[0] = false;
					return true;
				}
			} else if( c == 'Y' || c == 'y' ) {
				if( (idx+3) > count ) return false;
				String v = s.substring( idx+1, idx+3 );
				if( "es".equalsIgnoreCase(v) ) {
					ret[0] = true;
					return true;
				}
			} else if( c == 'N' || c == 'n' ) {
				if( (idx+2) > count ) return false;
				c = s.charAt(idx+1);
				if( c == 'o' || c == 'O' ) {
					ret[0] = false;
					return true;
				}
			}
		}
		return false;
	}
	private static boolean getCondition( String s, int idx, int[] ret ) {
		final int count = s.length();
		if( (idx+2) > count ) return false;
		String v = s.substring( idx, idx+2 );
		// get condition value
		if("no".equalsIgnoreCase(v)) { ret[0] = WaveLoopLink.llcNone;			return true; }
		if("eq".equalsIgnoreCase(v)) { ret[0] = WaveLoopLink.llcEqual;			return true; }
		if("ne".equalsIgnoreCase(v)) { ret[0] = WaveLoopLink.llcNotEqual;		return true; }
		if("gt".equalsIgnoreCase(v)) { ret[0] = WaveLoopLink.llcGreater;		return true; }
		if("ge".equalsIgnoreCase(v)) { ret[0] = WaveLoopLink.llcGreaterOrEqual;	return true; }
		if("lt".equalsIgnoreCase(v)) { ret[0] = WaveLoopLink.llcLesser;			return true; }
		if("le".equalsIgnoreCase(v)) { ret[0] = WaveLoopLink.llcLesserOrEqual;	return true; }
		return false;
	}
	private static boolean getInt(String s, int idx, int[] ret ) {
		final int count = s.length();
		// convert string to integer
		int r = 0;
		boolean sign = false;
		while( idx < count && s.charAt(idx) <= 0x20) idx++; // skip spaces
		if( idx >= count ) return false;
		if( s.charAt(idx) == '-' ) {
			sign = true;
			idx++;
			while( idx < count && s.charAt(idx) <= 0x20) idx++; // skip spaces
			if( idx >= count ) return false;
		}

		char c = s.charAt(idx);
		while( idx < count && c >= '0' && c <= '9' ) {
			r *= 10;
			r += c - '0';
			idx++;
			if( idx < count ) c = s.charAt(idx);
		}
		if(sign) r = -r;
		ret[0] = r;
		return true;
	}
	private static boolean getInt64(String s, int idx, long[] ret ) {
		final int count = s.length();
		// convert string to integer
		long r = 0;
		boolean sign = false;
		while( idx < count && s.charAt(idx) <= 0x20) idx++; // skip spaces
		if( idx >= count ) return false;
		if( s.charAt(idx) == '-' ) {
			sign = true;
			idx++;
			while( idx < count && s.charAt(idx) <= 0x20) idx++; // skip spaces
			if( idx >= count ) return false;
		}

		char c = s.charAt(idx);
		while( idx < count && c >= '0' && c <= '9' ) {
			r *= 10;
			r += c - '0';
			idx++;
			if( idx < count ) c = s.charAt(idx);
		}
		if(sign) r = -r;
		ret[0] = r;
		return true;
	}
}
