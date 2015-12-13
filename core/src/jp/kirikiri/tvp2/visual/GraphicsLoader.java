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
package jp.kirikiri.tvp2.visual;

import java.util.ArrayList;
import java.util.HashMap;

import jp.kirikiri.tjs2.Interface;
import jp.kirikiri.tjs2.BinaryStream;
import jp.kirikiri.tjs2.Dispatch2;
import jp.kirikiri.tjs2.HashTable;
import jp.kirikiri.tjs2.Holder;
import jp.kirikiri.tjs2.TJS;
import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tjs2.TJSScriptError;
import jp.kirikiri.tjs2.TJSScriptException;
import jp.kirikiri.tjs2.Variant;
import jp.kirikiri.tjs2.VariantException;
import jp.kirikiri.tvp2.TVP;
import jp.kirikiri.tvp2.base.CompactEventCallbackInterface;
import jp.kirikiri.tvp2.base.Storage;
import jp.kirikiri.tvp2.msg.Message;
import jp.kirikiri.tvp2.utils.DebugClass;
import jp.kirikiri.tvp2env.NativeImageBuffer;
import jp.kirikiri.tvp2env.NativeImageLoader;
import jp.kirikiri.tvp2env.SystemColor;

public class GraphicsLoader {
	// enum tTVPGraphicLoadMode
	static public final int
		glmNormal = 0, // normal, ie. 32bit ARGB graphic
		glmPalettized = 1, // palettized 8bit mode
		glmGrayscale = 2; // grayscale 8bit mode

	static class GraphicsSearchData {
		public String Name;
		public int KeyIdx; // color key index
		public int Mode; // image mode
		public int DesW; // desired width ( 0 for original size )
		public int DesH; // desired height ( 0 for original size )

		public boolean equals( Object o ) {
			if( o instanceof GraphicsSearchData ) {
				GraphicsSearchData rhs = (GraphicsSearchData)o;
				return Name.equals(rhs.Name) && KeyIdx == rhs.KeyIdx && Mode == rhs.Mode && DesW == rhs.DesW && DesH == rhs.DesH;
			} else {
				return false;
			}
		}
		public int hashCode() {
			int hash = Name.hashCode();
			hash ^= KeyIdx;
			hash ^= Mode;
			hash ^= DesW;
			hash ^= DesH;
			return hash;
		}
	}
	static private HashTable<GraphicsSearchData, GraphicImageData> GraphicCache;
	static private boolean ClearGraphicCacheCallbackInit;
	static public void initialize() {
		GraphicCache = new HashTable<GraphicsSearchData, GraphicImageData>();
		ClearGraphicCacheCallbackInit = false;
	}
	public static void finalizeApplication() {
		GraphicCache = null;
	}

	static class GraphicMetaInfoPair {
		public String Name;
		public String Value;
		public GraphicMetaInfoPair( final String name, final String value ) {
			Name = name;
			Value = value;
		}
	}
	static class GraphicImageData {
		private BaseBitmap mBitmap;
		private int mWidth;
		private int mHeight;
		private int mPixelSize;

		public String mProvinceName;
		public ArrayList<GraphicMetaInfoPair> mMetaInfo;

		private int mSize;

		public GraphicImageData() {
			//mSize = 0;
			//mBitmap = null;
			//mMetaInfo = new ArrayList<GraphicMetaInfoPair>();
		}

		public final void assignBitmap( final BaseBitmap bmp ) {
			if( mBitmap != null ) {
				mBitmap = null;
			}
			mWidth = bmp.getWidth();
			mHeight = bmp.getHeight();
			mPixelSize = bmp.is32BPP()?4:1;
			mSize =  mWidth*mHeight*mPixelSize;

			// simply assin to Bitmap
			mBitmap = new BaseBitmap(bmp);
		}

		public final void assignToBitmap( BaseBitmap bmp ) {
			// simply assign to Bitmap
			if( mBitmap != null ) bmp.assignBitmap(mBitmap);
		}

		public final int getSize() { return mSize; }
	}
	public static void checkGraphicCacheLimit() {
		while( TVP.GraphicCacheTotalBytes > TVP.GraphicCacheLimit ) {
			// 最後に追加されたものを消していく
			GraphicImageData data = GraphicCache.getLastValue();
			if( data != null ) {
				int size = data.getSize();
				TVP.GraphicCacheTotalBytes -= size;
				GraphicCache.chopLast(1);
			} else {
				break;
			}
		}
	}
	public static void clearGraphicCache() {
		GraphicCache.clear();
		TVP.GraphicCacheTotalBytes = 0;
	}
	/**
	 * preload graphic files into the cache.
	 * "limit" is a limit memory for preload, in bytes.
	 * this function gives up when "timeout" (in ms) expired.
	 * currently this function only loads normal graphics.
	 * (univ.trans rule graphics nor province image may not work properly)
	 * @param storages
	 * @param limit
	 * @param timeout
	 * @throws TJSException
	 */
	public static void touchImages(ArrayList<String> storages, int limit, long timeout ) throws TJSException {
		if( TVP.GraphicCacheLimit == 0 ) return;

		int limitbytes;
		if( limit >= 0 ) {
			if( limit > TVP.GraphicCacheLimit || limit == 0 )
				limitbytes = (int) TVP.GraphicCacheLimit;
			else
				limitbytes = limit;
		} else {
			// negative value of limit indicates remaining bytes after loading
			if( (-limit) >= TVP.GraphicCacheLimit) return;
			limitbytes = (int) (TVP.GraphicCacheLimit + limit);
		}
		int count = 0;
		int bytes = 0;
		long starttime = TVP.getTickCount();
		long limittime = starttime + timeout;
		BaseBitmap tmp = new BaseBitmap(32, 32, 32);
		StringBuilder statusstr = new StringBuilder(256);
		statusstr.append("(info) Touching ");
		boolean first = true;
		final int storageCount = storages.size();
		while( count < storageCount ) {
			if( timeout != 0 && TVP.getTickCount() >= limittime ) {
				statusstr.append(" ... aborted [timed out]");
				break;
			}
			if( bytes >= limitbytes ) {
				statusstr.append(" ... aborted [limit bytes exceeded]");
				break;
			}

			try {
				if(!first) statusstr.append(", ");
				first = false;
				statusstr.append( storages.get(count) );

				loadGraphic( tmp, storages.get(count), SystemColor.clNone, 0, 0, glmNormal, null, null ); // load image
				count++;

				// get image size
				bytes += tmp.getByteSize();
			} catch( TJSException e ) {
				statusstr.append("(error!:");
				statusstr.append( e.getMessage() );
				statusstr.append(")");
			} catch( Exception e ) {
				// ignore all errors
			}
		}

		// re-touch graphic cache to ensure that more earlier graphics in storages
		// array can get more priority in cache order.
		count--;
		GraphicsSearchData searchdata = new GraphicsSearchData();
		for( ; count >= 0; count-- ) {
			searchdata.Name = TVP.StorageMediaManager.normalizeStorageName(storages.get(count),null);
			searchdata.KeyIdx = SystemColor.clNone;
			searchdata.Mode = glmNormal;
			searchdata.DesW = 0;
			searchdata.DesH = 0;
			GraphicCache.getAndTouch(searchdata);
		}

		statusstr.append(" (elapsed ");
		statusstr.append( TVP.getTickCount() - starttime );
		statusstr.append("ms)");

		DebugClass.addLog(statusstr.toString());
	}

	/**
	 * @return provincename を返す
	 * @throws TJSException
	 */
	public static void loadGraphic( BaseBitmap dest, final String name, int keyidx, int desw, int desh,
			int mode, String[] provincename, Holder<Dispatch2> metainfo ) throws TJSException {
		// loading with cache management
		String nname = TVP.StorageMediaManager.normalizeStorageName(name,null);

		GraphicsSearchData searchdata = null;
		if( TVP.GraphicCacheEnabled ) {
			// int hash;
			searchdata = new GraphicsSearchData();
			searchdata.Name = nname;
			searchdata.KeyIdx = keyidx;
			searchdata.Mode = mode;
			searchdata.DesW = desw;
			searchdata.DesH = desh;

			GraphicImageData ptr = GraphicCache.getAndTouch(searchdata);
			if( ptr != null ) {
				// found in cache
				ptr.assignToBitmap(dest);
				if( provincename != null && provincename.length > 0 ) provincename[0] = ptr.mProvinceName;

				if( metainfo != null )
					metainfo.mValue = metaInfoPairsToDictionary(ptr.mMetaInfo);
				return;
			}
		}
		// キャッシュには見付からない

		// load into dest
		ArrayList<GraphicMetaInfoPair> mi = new ArrayList<GraphicMetaInfoPair>();
		try {
			internalLoadGraphic( dest, nname, keyidx, desw, desh, mi, mode, provincename );
			if( metainfo != null )
				metainfo.mValue = metaInfoPairsToDictionary(mi);

				if( TVP.GraphicCacheEnabled ) {
					GraphicImageData data = new GraphicImageData();
					data.assignBitmap(dest);
					if( provincename != null && provincename.length > 0 ) {
						data.mProvinceName = provincename[0];
					}
					data.mMetaInfo = mi; // now mi is managed under GraphicImageData
					mi = null;

					// check size limit
					checkGraphicCacheLimit();

					// push into hash table
					int datasize = data.getSize();
					TVP.GraphicCacheTotalBytes += datasize; // Java ではこの値よりも使われているバイト数は多くなるはずだけど、知り得ないのでこれで
					GraphicCache.put( searchdata, data );
				}
		} finally {
			mi = null;
		}
	}
	static private final String GRAPHIC_TYPE[] = {
		".png",
		".jpg",
		".tlg",
		".jpeg",
		".bmp",
	};
	static private boolean internalLoadGraphic( BaseBitmap dest, final String _name,
			int keyidx, int desw, int desh, ArrayList<GraphicMetaInfoPair> MetaInfo,
				int mode, String[] provincename) throws TJSException {

		// graphic compact initialization
		if( !ClearGraphicCacheCallbackInit ) {
			TVP.CompactEvent.addCompactEventHook( new CompactEventCallbackInterface() {
				@Override
				public void onCompact(int level) throws TJSScriptException, TJSScriptError, TJSException {
					if(level >= CompactEventCallbackInterface.COMPACT_LEVEL_MINIMIZE) {
						// clear the font cache on application minimize
						clearGraphicCache();
					}
				}
			});
			ClearGraphicCacheCallbackInit = true;
		}


		StringBuilder builder = new StringBuilder(256);

		String name = _name;
		String ext = Storage.extractStorageExt(name);
		if( ext == null || ext.length() == 0 ) {
			// 拡張子が見付からないので、登録されている拡張子で検索する
			final int count = GRAPHIC_TYPE.length;
			int i;
			for( i = 0; i < count; i++ ) {
				builder.delete(0, builder.length());
				builder.append(name);
				builder.append(GRAPHIC_TYPE[i]);
				String newname = builder.toString();
				if( Storage.isExistentStorage(newname) ) {
					name = newname;
					ext = GRAPHIC_TYPE[i];
					break;
				}
			}
			if( i == count ) {
				Message.throwExceptionMessage(Message.CannotSuggestGraphicExtension, name);
			}
		}

		boolean keyadapt = (keyidx == SystemColor.clAdapt);
		boolean doalphacolormat = SystemColor.is_clAlphaMat(keyidx);
		int alphamatcolor = SystemColor.get_clAlphaMat(keyidx);
		if( SystemColor.is_clPalIdx(keyidx) ) {
			// pass the palette index number to the handler.
			// ( since only Graphic Loading Handler can process the palette information )
			keyidx = SystemColor.get_clPalIdx(keyidx);
		} else {
			keyidx = -1;
		}

		HashMap<String,String> metainfo = new HashMap<String,String>();
		BinaryStream stream = Storage.createStream(name,0);
		NativeImageBuffer img = NativeImageLoader.loadImage( stream, ext, metainfo, keyidx, mode );
		if( img == null ) return false;
		dest.setNativeBitmap( img );
		if( ( desw != 0 && desh != 0 ) && (dest.getWidth() != desw || dest.getHeight() != desh) ) {
			dest.setSize( desw, desh, true );
		}
		if( keyadapt && mode == glmNormal ) {
			// adaptive color key
			dest.makeAlphaFromAdaptiveColor();
		}
		if( mode != glmNormal ) return true;

		int extlen = 0;
		if( ext != null ) extlen = ext.length();
		String baseName;
		if( extlen != 0 ) {
			baseName = _name.substring(0,_name.length()-extlen);
		} else {
			baseName = _name;
		}
		if( provincename != null && provincename.length > 0 ) {
			builder.delete(0, builder.length());
			builder.append(baseName);
			builder.append("_p");
			final int start = builder.length();
			final int count = GRAPHIC_TYPE.length;
			int i;
			for( i = 0; i < count; i++ ) {
				builder.append(GRAPHIC_TYPE[i]);
				String newname = builder.toString();
				if( Storage.isExistentStorage(newname) ) {
					provincename[0] = newname;
					break;
				}
				builder.delete(start, builder.length());
			}
			if( i == count ) {
				provincename[0] = null;
			}
		}

		// mask image handling ( addding _m suffix with the filename )
		builder.delete(0, builder.length());
		builder.append(baseName);
		builder.append("_m");
		final int start = builder.length();
		final int count = GRAPHIC_TYPE.length;
		int i;
		for( i = 0; i < count; i++ ) {
			builder.append(GRAPHIC_TYPE[i]);
			String newname = builder.toString();
			if( Storage.isExistentStorage(newname) ) {
				name = newname;
				ext = GRAPHIC_TYPE[i];
				break;
			}
			builder.delete(start, builder.length());
		}
		if( i != count ) {
			// open the mask file
			stream = Storage.createStream(name,0);
			//img = NativeImageLoader.loadImage( stream, ext, metainfo, GraphicsLoader.glmGrayscale );
			img = NativeImageLoader.loadImage( stream, ext, null, 0, glmGrayscale );
			// 読み込まれた画像にマスク適用する
			Rect refrect = new Rect(0,0,dest.getWidth(),dest.getHeight());
			NativeImageBuffer d = dest.getBitmap();
			d.copyRect(0, 0, img, refrect, BaseBitmap.BB_COPY_MASK );
		}

		// do color matting
		if( doalphacolormat && dest.is32BPP() ) {
			// alpha color mat
			NativeImageBuffer d = dest.getBitmap();
			d.doAlphaColorMat(alphamatcolor);
		}
		return true;
	}

	static private Dispatch2 metaInfoPairsToDictionary( ArrayList<GraphicMetaInfoPair> vec ) throws VariantException, TJSException {
		Dispatch2 dic = TJS.createDictionaryObject();
		if( vec == null ) return dic;
		final int count = vec.size();
		for( int i = 0; i < count; i++ ) {
			GraphicMetaInfoPair g = vec.get(i);
			Variant val = new Variant(g.Value);
			dic.propSet( Interface.MEMBERENSURE, g.Name, val, dic);
		}
		return dic;
	}
}
