/**
 ******************************************************************************
 * Copyright (c), Takenori Imoto
 * ïñ software http://www.kaede-software.com/
 * All rights reserved.
 ******************************************************************************
 * É\Å[ÉXÉRÅ[Éhå`éÆÇ©ÉoÉCÉiÉäå`éÆÇ©ÅAïœçXÇ∑ÇÈÇ©ÇµÇ»Ç¢Ç©Çñ‚ÇÌÇ∏ÅAà»â∫ÇÃèåèÇñû
 * ÇΩÇ∑èÍçáÇ…å¿ÇËÅAçƒî–ïzÇ®ÇÊÇ—égópÇ™ãñâ¬Ç≥ÇÍÇ‹Ç∑ÅB
 *
 * ÅEÉ\Å[ÉXÉRÅ[ÉhÇçƒî–ïzÇ∑ÇÈèÍçáÅAè„ãLÇÃíòçÏå†ï\é¶ÅAñ{èåèàÍóóÅAÇ®ÇÊÇ—â∫ãLñ∆ê”
 *   èçÄÇä‹ÇﬂÇÈÇ±Ç∆ÅB
 * ÅEÉoÉCÉiÉäå`éÆÇ≈çƒî–ïzÇ∑ÇÈèÍçáÅAî–ïzï®Ç…ïtëÆÇÃÉhÉLÉÖÉÅÉìÉgìôÇÃéëóøÇ…ÅAè„ãLÇÃ
 *   íòçÏå†ï\é¶ÅAñ{èåèàÍóóÅAÇ®ÇÊÇ—â∫ãLñ∆ê”èçÄÇä‹ÇﬂÇÈÇ±Ç∆ÅB
 * ÅEèëñ Ç…ÇÊÇÈì¡ï ÇÃãñâ¬Ç»ÇµÇ…ÅAñ{É\ÉtÉgÉEÉFÉAÇ©ÇÁîhê∂ÇµÇΩêªïiÇÃêÈì`Ç‹ÇΩÇÕîÃîÑ
 *   ë£êiÇ…ÅAëgêDÇÃñºëOÇ‹ÇΩÇÕÉRÉìÉgÉäÉrÉÖÅ[É^Å[ÇÃñºëOÇégópÇµÇƒÇÕÇ»ÇÁÇ»Ç¢ÅB
 *
 * ñ{É\ÉtÉgÉEÉFÉAÇÕÅAíòçÏå†é“Ç®ÇÊÇ—ÉRÉìÉgÉäÉrÉÖÅ[É^Å[Ç…ÇÊÇ¡ÇƒÅuåªèÛÇÃÇ‹Ç‹ÅvíÒãü
 * Ç≥ÇÍÇƒÇ®ÇËÅAñæé¶ñŸé¶Çñ‚ÇÌÇ∏ÅAè§ã∆ìIÇ»égópâ¬î\ê´ÅAÇ®ÇÊÇ—ì¡íËÇÃñ⁄ìIÇ…ëŒÇ∑ÇÈìK
 * çáê´Ç…ä÷Ç∑ÇÈà√ñŸÇÃï€èÿÇ‡ä‹ÇﬂÅAÇ‹ÇΩÇªÇÍÇ…å¿íËÇ≥ÇÍÇ»Ç¢ÅAÇ¢Ç©Ç»ÇÈï€èÿÇ‡Ç†ÇËÇ‹Çπ
 * ÇÒÅBíòçÏå†é“Ç‡ÉRÉìÉgÉäÉrÉÖÅ[É^Å[Ç‡ÅAéñóRÇÃÇ¢Ç©ÇÒÇñ‚ÇÌÇ∏ÅAëπäQî≠ê∂ÇÃå¥àˆÇ¢Ç©
 * ÇÒÇñ‚ÇÌÇ∏ÅAÇ©Ç¬ê”îCÇÃç™ãíÇ™å_ñÒÇ≈Ç†ÇÈÇ©åµäiê”îCÇ≈Ç†ÇÈÇ©Åiâﬂé∏ÇªÇÃëºÇÃÅjïsñ@
 * çsà◊Ç≈Ç†ÇÈÇ©Çñ‚ÇÌÇ∏ÅAâºÇ…ÇªÇÃÇÊÇ§Ç»ëπäQÇ™î≠ê∂Ç∑ÇÈâ¬î\ê´ÇímÇÁÇ≥ÇÍÇƒÇ¢ÇΩÇ∆Çµ
 * ÇƒÇ‡ÅAñ{É\ÉtÉgÉEÉFÉAÇÃégópÇ…ÇÊÇ¡Çƒî≠ê∂ÇµÇΩÅië„ë÷ïiÇ‹ÇΩÇÕë„ópÉTÅ[ÉrÉXÇÃí≤íBÅA
 * égópÇÃëré∏ÅAÉfÅ[É^ÇÃëré∏ÅAóòâvÇÃëré∏ÅAã∆ñ±ÇÃíÜífÇ‡ä‹ÇﬂÅAÇ‹ÇΩÇªÇÍÇ…å¿íËÇ≥ÇÍÇ»
 * Ç¢Åjíºê⁄ëπäQÅAä‘ê⁄ëπäQÅAãÙî≠ìIÇ»ëπäQÅAì¡ï ëπäQÅAí¶î±ìIëπäQÅAÇ‹ÇΩÇÕåãâ ëπäQÇ…
 * Ç¬Ç¢ÇƒÅAàÍêÿê”îCÇïâÇÌÇ»Ç¢Ç‡ÇÃÇ∆ÇµÇ‹Ç∑ÅB
 ******************************************************************************
 * ñ{É\ÉtÉgÉEÉFÉAÇÕÅAãgó¢ãgó¢2 ( http://kikyou.info/tvp/ ) ÇÃÉ\Å[ÉXÉRÅ[ÉhÇJava
 * Ç…èëÇ´ä∑Ç¶ÇΩÇ‡ÇÃÇàÍïîégópÇµÇƒÇ¢Ç‹Ç∑ÅB
 * ãgó¢ãgó¢2 Copyright (C) W.Dee <dee@kikyou.info> and contributors
 ******************************************************************************
 */
package jp.kirikiri.tvp2.visual;

import jp.kirikiri.tjs2.Error;
import jp.kirikiri.tjs2.Interface;
import jp.kirikiri.tjs2.Dispatch2;
import jp.kirikiri.tjs2.Holder;
import jp.kirikiri.tjs2.NativeClass;
import jp.kirikiri.tjs2.NativeClassConstructor;
import jp.kirikiri.tjs2.NativeClassMethod;
import jp.kirikiri.tjs2.NativeClassProperty;
import jp.kirikiri.tjs2.NativeInstance;
import jp.kirikiri.tjs2.TJS;
import jp.kirikiri.tjs2.TJSException;
import jp.kirikiri.tjs2.Variant;
import jp.kirikiri.tjs2.VariantClosure;
import jp.kirikiri.tjs2.VariantException;
import jp.kirikiri.tvp2.TVP;
import jp.kirikiri.tvp2.base.EventManager;
import jp.kirikiri.tvp2.msg.Message;
import jp.kirikiri.tvp2.utils.DebugClass;

public class LayerClass extends NativeClass {

	static public int ClassID = -1;
	static private final String CLASS_NAME = "Layer";

	protected NativeInstance createNativeInstance() throws TJSException {
		return new LayerNI();
	}

	/*
	private static LayerNI getNativeInstance( Dispatch2 objthis ) {
		return (LayerNI)getNativeInstance( ClassID );
	}
	*/

	public LayerClass() throws VariantException, TJSException {
		super(CLASS_NAME);
		final int NCM_CLASSID = TJS.registerNativeClass(CLASS_NAME);
		setClassID( NCM_CLASSID );
		ClassID = NCM_CLASSID;

		// constructor
		registerNCM( CLASS_NAME, new NativeClassConstructor() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				int hr = _this.construct(param, objthis);
				if( hr < 0 ) return hr;
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );


		registerNCM( "finalize", TVP.ReturnOKMethod, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "moveBefore", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if( param.length < 1) return Error.E_BADPARAMCOUNT;

				LayerNI src = null;
				VariantClosure clo = param[0].asObjectClosure();
				if( clo.mObject != null ) {
					src = (LayerNI) clo.mObject.getNativeInstance( LayerClass.ClassID );
				}
				if( src == null ) Message.throwExceptionMessage(Message.SpecifyLayer);

				_this.moveBefore(src);
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "moveBehind", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if( param.length < 1) return Error.E_BADPARAMCOUNT;

				LayerNI src = null;
				VariantClosure clo = param[0].asObjectClosure();
				if( clo.mObject != null ) {
					src = (LayerNI) clo.mObject.getNativeInstance( LayerClass.ClassID );
				}
				if( src == null ) Message.throwExceptionMessage(Message.SpecifyLayer);

				_this.moveBehind(src);
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "bringToBack", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.bringToBack();
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "bringToFront", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.bringToFront();
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "saveLayerImage", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if( param.length < 1) return Error.E_BADPARAMCOUNT;

				String name = param[0].asString();
				String type = "bmp";
				if( param.length >= 2 && param[1].isVoid() != true )
					type = param[1].asString();

				_this.saveLayerImage(name,type);
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "loadImages", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if( param.length < 1) return Error.E_BADPARAMCOUNT;
				String name = param[0].asString();
				int key = 0x1fffffff; // clNone
				if( param.length >=2 && param[1].isVoid() != true  )
					key = param[1].asInteger();
				Dispatch2 metainfo = _this.loadImages(name, key);
				if( result != null ) result.set( metainfo );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "loadProvinceImage", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if( param.length < 1) return Error.E_BADPARAMCOUNT;
				String name = param[0].asString();
				_this.loadProvinceImage(name);
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "getMainPixel", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if( param.length < 2) return Error.E_BADPARAMCOUNT;
				if( result != null ) result.set( _this.getMainPixel(param[0].asInteger(), param[1].asInteger()) );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "setMainPixel", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if( param.length < 3) return Error.E_BADPARAMCOUNT;
				_this.setMainPixel(param[0].asInteger(), param[1].asInteger(), param[2].asInteger());
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "getMaskPixel", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if( param.length < 2) return Error.E_BADPARAMCOUNT;
				if( result != null ) result.set( _this.getMaskPixel(param[0].asInteger(), param[1].asInteger()) );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "setMaskPixel", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if( param.length < 3) return Error.E_BADPARAMCOUNT;
				_this.setMaskPixel(param[0].asInteger(), param[1].asInteger(), param[2].asInteger());
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "getProvincePixel", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if( param.length < 2) return Error.E_BADPARAMCOUNT;
				if( result != null ) result.set( _this.getProvincePixel(param[0].asInteger(), param[1].asInteger()) );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "setProvincePixel", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if( param.length < 3) return Error.E_BADPARAMCOUNT;
				_this.setProvincePixel(param[0].asInteger(), param[1].asInteger(), param[2].asInteger());
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "getLayerAt", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if( param.length < 2) return Error.E_BADPARAMCOUNT;

				boolean exclude_self = false;
				boolean get_disabled = false;
				if( param.length >= 3 && param[2].isVoid() != true )
					exclude_self = param[2].asBoolean();

				if( param.length >= 4 && param[3].isVoid() != true )
					get_disabled = param[3].asBoolean();

				LayerNI lay = _this.getMostFrontChildAt( param[0].asInteger(), param[1].asInteger(), exclude_self, get_disabled);
				if( result != null ) {
					if( lay != null && lay.getOwner() != null )
						result.set( lay.getOwner(), lay.getOwner() );
					else
						result.set( null, null );
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "setPos", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if( param.length < 2) return Error.E_BADPARAMCOUNT;
				if( param.length == 4 && param[2].isVoid() != true && param[3].isVoid() != true ) {
					// set bounds
					Rect r = new Rect();
					r.left = param[0].asInteger();
					r.top = param[1].asInteger();
					r.right = param[2].asInteger() + r.left;
					r.bottom = param[3].asInteger() + r.top;
					_this.setBounds(r);
				} else {
					// set position only
					_this.setPosition( param[0].asInteger(), param[1].asInteger());
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "setSize", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if( param.length < 2) return Error.E_BADPARAMCOUNT;
				_this.setSize( param[0].asInteger(), param[1].asInteger());
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "setSizeToImageSize", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				_this.setSize(_this.getImageWidth(), _this.getImageHeight());
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "setImagePos", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if( param.length < 2) return Error.E_BADPARAMCOUNT;
				_this.setImagePosition( param[0].asInteger(), param[1].asInteger());
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "setImageSize", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if( param.length < 2) return Error.E_BADPARAMCOUNT;
				_this.setImageSize( param[0].asInteger(), param[1].asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "independMainImage", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				boolean copy = true;
				if( param.length >= 1 && param[0].isVoid() != true )
					copy = param[0].asBoolean();
				_this.independMainImage( copy );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "independProvinceImage", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				boolean copy = true;
				if( param.length >= 1 && param[0].isVoid() != true )
					copy = param[0].asBoolean();
				_this.independProvinceImage( copy );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "setClip", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if( param.length == 0 ) {
					// reset clip rectangle
					_this.resetClip();
				} else {
					if( param.length < 4) return Error.E_BADPARAMCOUNT;
					_this.setClip( param[0].asInteger(), param[1].asInteger(), param[2].asInteger(), param[3].asInteger() );
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "fillRect", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if( param.length < 5) return Error.E_BADPARAMCOUNT;
				int x, y;
				x = param[0].asInteger();
				y = param[1].asInteger();
				_this.fillRect( new Rect(x, y, x+param[2].asInteger(), y+param[3].asInteger()), param[4].asInteger());
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "colorRect", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if( param.length < 5) return Error.E_BADPARAMCOUNT;
				int x, y;
				x = param[0].asInteger();
				y = param[1].asInteger();
				int alpha = 255;
				if( param.length >= 6 && param[5].isVoid() != true )
					alpha = param[5].asInteger();
				_this.colorRect( new Rect(x, y, x+param[2].asInteger(), y+param[3].asInteger()), param[4].asInteger(), alpha );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "drawText", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if( param.length < 4) return Error.E_BADPARAMCOUNT;

				_this.drawText(
						param[0].asInteger(),
						param[1].asInteger(),
						param[2].asString(),
						param[3].asInteger(),
						(param.length >= 5 && param[4].isVoid() != true)? param[4].asInteger() : 255,
						(param.length >= 6 && param[5].isVoid() != true)? param[5].asBoolean() : true,
						(param.length >= 7 && param[6].isVoid() != true)? param[6].asInteger() : 0,
						(param.length >= 8 && param[7].isVoid() != true)? param[7].asInteger() : 0,
						(param.length >= 9 && param[8].isVoid() != true)? param[8].asInteger() : 0,
						(param.length >=10 && param[9].isVoid() != true)? param[9].asInteger() : 0,
						(param.length >=11 && param[10].isVoid() != true)? param[10].asInteger() : 0
						);
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "piledCopy", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if( param.length < 7) return Error.E_BADPARAMCOUNT;
				LayerNI src = null;
				VariantClosure clo = param[2].asObjectClosure();
				if( clo.mObject != null ) {
					src = (LayerNI)clo.mObject.getNativeInstance( LayerClass.ClassID );
				}
				if( src == null ) Message.throwExceptionMessage(Message.SpecifyLayer);

				Rect rect = new Rect(param[3].asInteger(), param[4].asInteger(), param[5].asInteger(), param[6].asInteger());
				rect.right += rect.left;
				rect.bottom += rect.top;
				_this.piledCopy( param[0].asInteger(), param[1].asInteger(), src, rect);
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "copyRect", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if( param.length < 7) return Error.E_BADPARAMCOUNT;
				LayerNI src = null;
				VariantClosure clo = param[2].asObjectClosure();
				if( clo.mObject != null ) {
					src = (LayerNI)clo.mObject.getNativeInstance( LayerClass.ClassID );
				}
				if( src == null ) Message.throwExceptionMessage(Message.SpecifyLayer);

				Rect rect = new Rect(param[3].asInteger(), param[4].asInteger(), param[5].asInteger(), param[6].asInteger());
				rect.right += rect.left;
				rect.bottom += rect.top;
				_this.copyRect( param[0].asInteger(), param[1].asInteger(), src, rect);
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "pileRect", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if( param.length < 7) return Error.E_BADPARAMCOUNT;
				LayerNI src = null;
				VariantClosure clo = param[2].asObjectClosure();
				if( clo.mObject != null ) {
					src = (LayerNI)clo.mObject.getNativeInstance( LayerClass.ClassID );
				}
				if( src == null ) Message.throwExceptionMessage(Message.SpecifyLayer);

				Rect rect = new Rect(param[3].asInteger(), param[4].asInteger(), param[5].asInteger(), param[6].asInteger());
				rect.right += rect.left;
				rect.bottom += rect.top;

				if( param.length >= 9 && param[8].isVoid() != true ) {
					DebugClass.addLog( Message.formatMessage( Message.HoldDestinationAlphaParameterIsNowDeprecated,
						"Layer.pileRect", "9") );
				}
				int alpha = 255;
				if( param.length >= 8 && param[7].isVoid() != true )
					alpha = param[7].asInteger();

				_this.pileRect( param[0].asInteger(), param[1].asInteger(), src, rect, alpha);
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "blendRect", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if( param.length < 7) return Error.E_BADPARAMCOUNT;
				LayerNI src = null;
				VariantClosure clo = param[2].asObjectClosure();
				if( clo.mObject != null ) {
					src = (LayerNI)clo.mObject.getNativeInstance( LayerClass.ClassID );
				}
				if( src == null ) Message.throwExceptionMessage(Message.SpecifyLayer);

				Rect rect = new Rect(param[3].asInteger(), param[4].asInteger(), param[5].asInteger(), param[6].asInteger());
				rect.right += rect.left;
				rect.bottom += rect.top;

				if( param.length >= 9 && param[8].isVoid() != true ) {
					DebugClass.addLog( Message.formatMessage( Message.HoldDestinationAlphaParameterIsNowDeprecated,
						"Layer.blendRect", "9") );
				}
				int alpha = 255;
				if( param.length >= 8 && param[7].isVoid() != true )
					alpha = param[7].asInteger();

				_this.blendRect( param[0].asInteger(), param[1].asInteger(), src, rect, alpha);
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "operateRect", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if( param.length < 7) return Error.E_BADPARAMCOUNT;
				LayerNI src = null;
				VariantClosure clo = param[2].asObjectClosure();
				if( clo.mObject != null ) {
					src = (LayerNI)clo.mObject.getNativeInstance( LayerClass.ClassID );
				}
				if( src == null ) Message.throwExceptionMessage(Message.SpecifyLayer);

				Rect rect = new Rect(param[3].asInteger(), param[4].asInteger(), param[5].asInteger(), param[6].asInteger());
				rect.right += rect.left;
				rect.bottom += rect.top;

				int mode;
				if( param.length >= 8 && param[7].isVoid() != true )
					mode = param[7].asInteger();
				else
					mode = LayerType.omAuto;

				if( param.length >= 10 && param[9].isVoid() != true ) {
					DebugClass.addLog( Message.formatMessage( Message.HoldDestinationAlphaParameterIsNowDeprecated,
						"Layer.operateRect", "10") );
				}
				int alpha = 255;
				if( param.length >= 9 && param[8].isVoid() != true )
					alpha = param[8].asInteger();

				_this.operateRect( param[0].asInteger(), param[1].asInteger(), src, rect, mode, alpha);
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "stretchCopy", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				// dx, dy, dw, dh, src, sx, sy, sw, sh, type=0
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if( param.length < 9) return Error.E_BADPARAMCOUNT;
				LayerNI src = null;
				VariantClosure clo = param[4].asObjectClosure();
				if( clo.mObject != null ) {
					src = (LayerNI)clo.mObject.getNativeInstance( LayerClass.ClassID );
				}
				if( src == null ) Message.throwExceptionMessage(Message.SpecifyLayer);

				Rect destrect = new Rect(param[0].asInteger(), param[1].asInteger(), param[2].asInteger(), param[3].asInteger());
				destrect.right += destrect.left;
				destrect.bottom += destrect.top;

				Rect srcrect = new Rect(param[5].asInteger(), param[6].asInteger(), param[7].asInteger(), param[8].asInteger());
				srcrect.right += srcrect.left;
				srcrect.bottom += srcrect.top;

				int type = LayerType.stNearest;
				if( param.length >= 10 && param[9].isVoid() != true )
					type = param[9].asInteger();

				_this.stretchCopy( destrect, src, srcrect, type );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "stretchPile", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				// dx, dy, dw, dh, src, sx, sy, sw, sh, opa=255, type=0
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if( param.length < 9) return Error.E_BADPARAMCOUNT;
				LayerNI src = null;
				VariantClosure clo = param[4].asObjectClosure();
				if( clo.mObject != null ) {
					src = (LayerNI)clo.mObject.getNativeInstance( LayerClass.ClassID );
				}
				if( src == null ) Message.throwExceptionMessage(Message.SpecifyLayer);

				Rect destrect = new Rect(param[0].asInteger(), param[1].asInteger(), param[2].asInteger(), param[3].asInteger());
				destrect.right += destrect.left;
				destrect.bottom += destrect.top;

				Rect srcrect = new Rect(param[5].asInteger(), param[6].asInteger(), param[7].asInteger(), param[8].asInteger());
				srcrect.right += srcrect.left;
				srcrect.bottom += srcrect.top;

				int opa = 255;
				if( param.length >= 10 && param[9].isVoid() != true )
					opa = param[9].asInteger();

				int type = LayerType.stNearest;
				if( param.length >= 11 && param[10].isVoid() != true )
					type = param[10].asInteger();

				if( param.length >= 12 && param[11].isVoid() != true ) {
					DebugClass.addLog( Message.formatMessage( Message.HoldDestinationAlphaParameterIsNowDeprecated,
						"Layer.stretchPile", "12") );
				}

				_this.stretchPile( destrect, src, srcrect, opa, type );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "stretchBlend", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				// dx, dy, dw, dh, src, sx, sy, sw, sh, opa=255, type=0
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if( param.length < 9) return Error.E_BADPARAMCOUNT;
				LayerNI src = null;
				VariantClosure clo = param[4].asObjectClosure();
				if( clo.mObject != null ) {
					src = (LayerNI)clo.mObject.getNativeInstance( LayerClass.ClassID );
				}
				if( src == null ) Message.throwExceptionMessage(Message.SpecifyLayer);

				Rect destrect = new Rect(param[0].asInteger(), param[1].asInteger(), param[2].asInteger(), param[3].asInteger());
				destrect.right += destrect.left;
				destrect.bottom += destrect.top;

				Rect srcrect = new Rect(param[5].asInteger(), param[6].asInteger(), param[7].asInteger(), param[8].asInteger());
				srcrect.right += srcrect.left;
				srcrect.bottom += srcrect.top;

				int opa = 255;
				if( param.length >= 10 && param[9].isVoid() != true )
					opa = param[9].asInteger();

				int type = LayerType.stNearest;
				if( param.length >= 11 && param[10].isVoid() != true )
					type = param[10].asInteger();

				if( param.length >= 12 && param[11].isVoid() != true ) {
					DebugClass.addLog( Message.formatMessage( Message.HoldDestinationAlphaParameterIsNowDeprecated,
						"Layer.stretchBlend", "12") );
				}

				_this.stretchBlend( destrect, src, srcrect, opa, type );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "operateStretch", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				// dx, dy, dw, dh, src, sx, sy, sw, sh, mode=omAuto, opa=255, type=0
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if( param.length < 9) return Error.E_BADPARAMCOUNT;
				LayerNI src = null;
				VariantClosure clo = param[4].asObjectClosure();
				if( clo.mObject != null ) {
					src = (LayerNI)clo.mObject.getNativeInstance( LayerClass.ClassID );
				}
				if( src == null ) Message.throwExceptionMessage(Message.SpecifyLayer);

				Rect destrect = new Rect(param[0].asInteger(), param[1].asInteger(), param[2].asInteger(), param[3].asInteger());
				destrect.right += destrect.left;
				destrect.bottom += destrect.top;

				Rect srcrect = new Rect(param[5].asInteger(), param[6].asInteger(), param[7].asInteger(), param[8].asInteger());
				srcrect.right += srcrect.left;
				srcrect.bottom += srcrect.top;

				int mode = LayerType.omAuto;
				if( param.length >= 10 && param[9].isVoid() != true )
					mode = param[9].asInteger();

				int opa = 255;
				if( param.length >= 11 && param[10].isVoid() != true )
					opa = param[10].asInteger();

				int type = LayerType.stNearest;
				if( param.length >= 12 && param[11].isVoid() != true )
					type = param[11].asInteger();

				_this.operateStretch( destrect, src, srcrect, mode, opa, type );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "affineCopy", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				// src, sx, sy, sw, sh, affine, x0/a, y0/b, x1/c, y1/d, x2/tx, y2/ty, type=0
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if( param.length < 12) return Error.E_BADPARAMCOUNT;
				LayerNI src = null;
				VariantClosure clo = param[0].asObjectClosure();
				if( clo.mObject != null ) {
					src = (LayerNI)clo.mObject.getNativeInstance( LayerClass.ClassID );
				}
				if( src == null ) Message.throwExceptionMessage(Message.SpecifyLayer);

				Rect srcrect = new Rect(param[1].asInteger(), param[2].asInteger(), param[3].asInteger(), param[4].asInteger());
				srcrect.right += srcrect.left;
				srcrect.bottom += srcrect.top;

				int type = LayerType.stNearest;
				if( param.length >= 13 && param[12].isVoid() != true )
					type = param[12].asInteger();

				boolean clear = false;
				if( param.length >= 14 && param[13].isVoid() != true )
					clear = param[13].asBoolean();

				if( param[5].asBoolean() ) {
					// affine matrix mode
					AffineMatrix2D mat = new AffineMatrix2D(
						param[6].asDouble(), param[7].asDouble(), param[8].asDouble(),
						param[9].asDouble(), param[10].asDouble(), param[11].asDouble() );
					_this.affineCopy(mat, src, srcrect, type, clear);
				} else {
					// points mode
					PointD[] points = new PointD[3];
					points[0] = new PointD( param[6].asDouble(), param[7].asDouble() );
					points[1] = new PointD( param[8].asDouble(), param[9].asDouble() );
					points[2] = new PointD( param[10].asDouble(), param[11].asDouble() );
					_this.affineCopy(points, src, srcrect, type, clear);
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "affinePile", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				// src, sx, sy, sw, sh, affine, x0/a, y0/b, x1/c, y1/d, x2/tx, y2/ty, opa=255, type=0
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if( param.length < 12) return Error.E_BADPARAMCOUNT;
				LayerNI src = null;
				VariantClosure clo = param[0].asObjectClosure();
				if( clo.mObject != null ) {
					src = (LayerNI)clo.mObject.getNativeInstance( LayerClass.ClassID );
				}
				if( src == null ) Message.throwExceptionMessage(Message.SpecifyLayer);

				Rect srcrect = new Rect(param[1].asInteger(), param[2].asInteger(), param[3].asInteger(), param[4].asInteger());
				srcrect.right += srcrect.left;
				srcrect.bottom += srcrect.top;

				int opa = 255;
				int type = LayerType.stNearest;
				if( param.length >= 13 && param[12].isVoid() != true )
					opa = param[12].asInteger();
				if( param.length >= 14 && param[13].isVoid() != true )
					type = param[13].asInteger();

				if( param.length >= 15 && param[14].isVoid() != true ) {
					DebugClass.addLog( Message.formatMessage( Message.HoldDestinationAlphaParameterIsNowDeprecated,
						"Layer.affinePile", "15") );
				}

				if( param[5].asBoolean() ) {
					// affine matrix mode
					AffineMatrix2D mat = new AffineMatrix2D(
						param[6].asDouble(), param[7].asDouble(), param[8].asDouble(),
						param[9].asDouble(), param[10].asDouble(), param[11].asDouble() );
					_this.affinePile(mat, src, srcrect, opa, type );
				} else {
					// points mode
					PointD[] points = new PointD[3];
					points[0] = new PointD( param[6].asDouble(), param[7].asDouble() );
					points[1] = new PointD( param[8].asDouble(), param[9].asDouble() );
					points[2] = new PointD( param[10].asDouble(), param[11].asDouble() );
					_this.affinePile(points, src, srcrect, opa, type );
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "affineBlend", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				// src, sx, sy, sw, sh, affine, x0/a, y0/b, x1/c, y1/d, x2/tx, y2/ty, opa=255, type=0
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if( param.length < 12) return Error.E_BADPARAMCOUNT;
				LayerNI src = null;
				VariantClosure clo = param[0].asObjectClosure();
				if( clo.mObject != null ) {
					src = (LayerNI)clo.mObject.getNativeInstance( LayerClass.ClassID );
				}
				if( src == null ) Message.throwExceptionMessage(Message.SpecifyLayer);

				Rect srcrect = new Rect(param[1].asInteger(), param[2].asInteger(), param[3].asInteger(), param[4].asInteger());
				srcrect.right += srcrect.left;
				srcrect.bottom += srcrect.top;

				int opa = 255;
				int type = LayerType.stNearest;
				if( param.length >= 13 && param[12].isVoid() != true )
					opa = param[12].asInteger();
				if( param.length >= 14 && param[13].isVoid() != true )
					type = param[13].asInteger();

				if( param.length >= 15 && param[14].isVoid() != true ) {
					DebugClass.addLog( Message.formatMessage( Message.HoldDestinationAlphaParameterIsNowDeprecated,
						"Layer.affineBlend", "15") );
				}

				if( param[5].asBoolean() ) {
					// affine matrix mode
					AffineMatrix2D mat = new AffineMatrix2D(
						param[6].asDouble(), param[7].asDouble(), param[8].asDouble(),
						param[9].asDouble(), param[10].asDouble(), param[11].asDouble() );
					_this.affineBlend(mat, src, srcrect, opa, type );
				} else {
					// points mode
					PointD[] points = new PointD[3];
					points[0] = new PointD( param[6].asDouble(), param[7].asDouble() );
					points[1] = new PointD( param[8].asDouble(), param[9].asDouble() );
					points[2] = new PointD( param[10].asDouble(), param[11].asDouble() );
					_this.affineBlend(points, src, srcrect, opa, type );
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "operateAffine", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				// src, sx, sy, sw, sh, affine, x0/a, y0/b, x1/c, y1/d, x2/tx, y2/ty,
				// mode=omAuto, opa=255, type=0
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if( param.length < 12) return Error.E_BADPARAMCOUNT;
				LayerNI src = null;
				VariantClosure clo = param[0].asObjectClosure();
				if( clo.mObject != null ) {
					src = (LayerNI)clo.mObject.getNativeInstance( LayerClass.ClassID );
				}
				if( src == null ) Message.throwExceptionMessage(Message.SpecifyLayer);

				Rect srcrect = new Rect(param[1].asInteger(), param[2].asInteger(), param[3].asInteger(), param[4].asInteger());
				srcrect.right += srcrect.left;
				srcrect.bottom += srcrect.top;

				int opa = 255;
				int type = LayerType.stNearest;
				if( param.length >= 14 && param[13].isVoid() != true )
					opa = param[13].asInteger();
				if( param.length >= 15 && param[14].isVoid() != true )
					type = param[14].asInteger();

				if( param.length >= 16 && param[15].isVoid() != true ) {
					DebugClass.addLog( Message.formatMessage( Message.HoldDestinationAlphaParameterIsNowDeprecated,
						"Layer.operateAffine", "16") );
				}
				int mode = LayerType.omAuto;
				if( param.length >= 13 && param[12].isVoid() != true )
					mode = param[12].asInteger();

				if( param[5].asBoolean() ) {
					// affine matrix mode
					AffineMatrix2D mat = new AffineMatrix2D(
						param[6].asDouble(), param[7].asDouble(), param[8].asDouble(),
						param[9].asDouble(), param[10].asDouble(), param[11].asDouble() );
					_this.operateAffine(mat, src, srcrect, mode, opa, type );
				} else {
					// points mode
					PointD[] points = new PointD[3];
					points[0] = new PointD( param[6].asDouble(), param[7].asDouble() );
					points[1] = new PointD( param[8].asDouble(), param[9].asDouble() );
					points[2] = new PointD( param[10].asDouble(), param[11].asDouble() );
					_this.operateAffine(points, src, srcrect, mode, opa, type );
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "doBoxBlur", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				int xblur = 1;
				int yblur = 1;
				if( param.length >= 1 && param[0].isVoid() != true )
					xblur = param[0].asInteger();
				if( param.length >= 2 && param[1].isVoid() != true )
					yblur = param[1].asInteger();

				_this.doBoxBlur( xblur, yblur );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "adjustGamma", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;

				if( param.length == 0) return Error.S_OK;

				GammaAdjustData data = new GammaAdjustData();

				if(param.length >= 1 && param[0].isVoid() != true )
					data.RGamma = (float)param[0].asDouble();
				if(param.length >= 2 && param[1].isVoid() != true )
					data.RFloor = param[1].asInteger();
				if(param.length >= 3 && param[2].isVoid() != true )
					data.RCeil  = param[2].asInteger();
				if(param.length >= 4 && param[3].isVoid() != true )
					data.GGamma = (float)param[3].asDouble();
				if(param.length >= 5 && param[4].isVoid() != true )
					data.GFloor = param[4].asInteger();
				if(param.length >= 6 && param[5].isVoid() != true )
					data.GCeil  = param[5].asInteger();
				if(param.length >= 7 && param[6].isVoid() != true )
					data.BGamma = (float)param[6].asDouble();
				if(param.length >= 8 && param[7].isVoid() != true )
					data.BFloor = param[7].asInteger();
				if(param.length >= 9 && param[8].isVoid() != true )
					data.BCeil  = param[8].asInteger();

				if( data.BCeil == 255 && data.BFloor == 0 && data.BGamma == 1.0f &&
						data.GCeil == 255 && data.GFloor == 0 && data.GGamma == 1.0f &&
						data.RCeil == 255 && data.RFloor == 0 && data.RGamma == 1.0f ) {
					return Error.S_OK;
				}
				_this.adjustGamma(data);
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "doGrayScale", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.doGrayScale();
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "flipLR", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.flipLR(); // LRflip
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "flipUD", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.flipUD(); // UDflip
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "convertType", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if( param.length < 1 ) return Error.E_BADPARAMCOUNT;
				int fromtype = param[0].asInteger();
				_this.convertLayerType(fromtype);
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "update", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				// this event sets callOnPaint flag of LayerNI, to invoke onPaint event.
				if( param.length < 1 ) {
					_this.updateByScript();
					// update entire area of the layer
				} else {
					if(param.length < 4) return Error.E_BADPARAMCOUNT;
					int l, t, w, h;
					l = param[0].asInteger();
					t = param[1].asInteger();
					w = param[2].asInteger();
					h = param[3].asInteger();
					_this.updateByScript( new Rect(l, t, l+w, t+h) );
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "setCursorPos", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if( param.length < 2 ) return Error.E_BADPARAMCOUNT;
				_this.setCursorPos(param[0].asInteger(),param[1].asInteger());
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "releaseCapture", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.releaseCapture();
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "focus", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				boolean direction = true;
				if( param.length >= 1 ) direction = param[0].asBoolean();
				boolean succeeded = _this.setFocus( direction );
				if( result != null ) result.set( succeeded ? 1 : 0 );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "focusPrev", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				LayerNI lay = _this.focusPrev();
				if( result != null ) {
					if( lay != null && lay.getOwner() != null )
						result.set( lay.getOwner(), lay.getOwner() );
					else
						result.set( null, null );
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "focusNext", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				LayerNI lay = _this.focusNext();
				if( result != null ) {
					if( lay != null && lay.getOwner() != null )
						result.set( lay.getOwner(), lay.getOwner() );
					else
						result.set( null, null );
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "setMode", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setMode();
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "removeMode", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.removeMode();
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "setAttentionPos", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if( param.length < 2 ) return Error.E_BADPARAMCOUNT;
				_this.setAttentionPoint(param[0].asInteger(), param[1].asInteger());
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "beginTransition", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if( param.length < 1 ) return Error.E_BADPARAMCOUNT;
				String name = param[0].asString();
				boolean withchildren = true;
				if( param.length >= 2 && param[1].isVoid() != true )
					withchildren = param[1].asBoolean();
				LayerNI transsrc = null;
				if( param.length >= 3 && param[2].isVoid() != true ) {
					VariantClosure clo = param[2].asObjectClosure();
					if( clo.mObject != null ) {
						transsrc = (LayerNI)clo.mObject.getNativeInstance( LayerClass.ClassID );
					}
				}
				if( transsrc == null ) Message.throwExceptionMessage(Message.SpecifyLayer);

				VariantClosure options;
				if( param.length >= 4 && param[3].isVoid() != true )
					options = param[3].asObjectClosure();
				else
					options = new VariantClosure(null, null);

				_this.startTransition(name, withchildren, transsrc, options);

				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "stopTransition", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.stopTransition();
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "assignImages", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if( param.length < 1 ) return Error.E_BADPARAMCOUNT;
				LayerNI src = null;
				VariantClosure clo = param[0].asObjectClosure();
				if( clo.mObject != null ) {
					src = (LayerNI)clo.mObject.getNativeInstance( LayerClass.ClassID );
				}
				if( src == null ) Message.throwExceptionMessage(Message.SpecifyLayer);
				_this.assignImages(src);
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "dump", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.dumpStructure();
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "dumpEx", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.dumpStructureEx();
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "purgeImage", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.purgeImage();
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		// event
		registerNCM( "onHitTest", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				//VariantClosure obj = _this.getActionOwner();
				if( param.length < 3 ) return Error.E_BADPARAMCOUNT;
				boolean b = param[2].asBoolean();
				_this.setHitTestWork(b);
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "onClick", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				VariantClosure obj = _this.getActionOwner();
				if( obj.mObject != null ) {
					if( param.length < 2 ) return Error.E_BADPARAMCOUNT;

					Dispatch2 evobj = EventManager.createEventObject( "onClick", objthis, objthis );
					Variant evval = new Variant(evobj, evobj);
					// set member
					evobj.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "x", param[0], evobj );
					evobj.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "y", param[1], evobj );

					Variant[] pevval = new Variant[1];
					pevval[0] = evval;
					obj.funcCall( 0, EventManager.ActionName, result, pevval, null );
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "onDoubleClick", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				VariantClosure obj = _this.getActionOwner();
				if( obj.mObject != null ) {
					if( param.length < 2 ) return Error.E_BADPARAMCOUNT;

					Dispatch2 evobj = EventManager.createEventObject( "onDoubleClick", objthis, objthis );
					Variant evval = new Variant(evobj, evobj);
					// set member
					evobj.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "x", param[0], evobj );
					evobj.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "y", param[1], evobj );

					Variant[] pevval = new Variant[1];
					pevval[0] = evval;
					obj.funcCall( 0, EventManager.ActionName, result, pevval, null );
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "onMouseDown", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				VariantClosure obj = _this.getActionOwner();
				if( obj.mObject != null ) {
					if( param.length < 4 ) return Error.E_BADPARAMCOUNT;

					Dispatch2 evobj = EventManager.createEventObject( "onMouseDown", objthis, objthis );
					Variant evval = new Variant(evobj, evobj);
					// set member
					evobj.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "x", param[0], evobj );
					evobj.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "y", param[1], evobj );
					evobj.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "button", param[2], evobj );
					evobj.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "shift", param[3], evobj );

					Variant[] pevval = new Variant[1];
					pevval[0] = evval;
					obj.funcCall( 0, EventManager.ActionName, result, pevval, null );
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "onMouseUp", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				VariantClosure obj = _this.getActionOwner();
				if( obj.mObject != null ) {
					if( param.length < 4 ) return Error.E_BADPARAMCOUNT;

					Dispatch2 evobj = EventManager.createEventObject( "onMouseUp", objthis, objthis );
					Variant evval = new Variant(evobj, evobj);
					// set member
					evobj.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "x", param[0], evobj );
					evobj.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "y", param[1], evobj );
					evobj.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "button", param[2], evobj );
					evobj.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "shift", param[3], evobj );

					Variant[] pevval = new Variant[1];
					pevval[0] = evval;
					obj.funcCall( 0, EventManager.ActionName, result, pevval, null );
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "onMouseMove", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				VariantClosure obj = _this.getActionOwner();
				if( obj.mObject != null ) {
					if( param.length < 3 ) return Error.E_BADPARAMCOUNT;

					Dispatch2 evobj = EventManager.createEventObject( "onMouseMove", objthis, objthis );
					Variant evval = new Variant(evobj, evobj);
					// set member
					evobj.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "x", param[0], evobj );
					evobj.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "y", param[1], evobj );
					evobj.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "shift", param[2], evobj );

					Variant[] pevval = new Variant[1];
					pevval[0] = evval;
					obj.funcCall( 0, EventManager.ActionName, result, pevval, null );
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "onMouseEnter", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				VariantClosure obj = _this.getActionOwner();
				if( obj.mObject != null )  callEventNoParam( "onMouseEnter", result, objthis, obj);
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "onMouseLeave", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				VariantClosure obj = _this.getActionOwner();
				if( obj.mObject != null )  callEventNoParam( "onMouseLeave", result, objthis, obj);
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "onBlur", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				VariantClosure obj = _this.getActionOwner();
				if( obj.mObject != null ) {
					if( param.length < 1 ) return Error.E_BADPARAMCOUNT;

					Dispatch2 evobj = EventManager.createEventObject( "onBlur", objthis, objthis );
					Variant evval = new Variant(evobj, evobj);
					// set member
					evobj.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "focused", param[0], evobj );

					Variant[] pevval = new Variant[1];
					pevval[0] = evval;
					obj.funcCall( 0, EventManager.ActionName, result, pevval, null );
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "onFocus", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				VariantClosure obj = _this.getActionOwner();
				if( obj.mObject != null ) {
					if( param.length < 2 ) return Error.E_BADPARAMCOUNT;

					Dispatch2 evobj = EventManager.createEventObject( "onFocus", objthis, objthis );
					Variant evval = new Variant(evobj, evobj);
					// set member
					evobj.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "blurred", param[0], evobj );
					evobj.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "direction", param[1], evobj );

					Variant[] pevval = new Variant[1];
					pevval[0] = evval;
					obj.funcCall( 0, EventManager.ActionName, result, pevval, null );
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "onNodeEnabled", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				VariantClosure obj = _this.getActionOwner();
				if( obj.mObject != null )  callEventNoParam( "onNodeEnabled", result, objthis, obj);
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "onNodeDisabled", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				VariantClosure obj = _this.getActionOwner();
				if( obj.mObject != null )  callEventNoParam( "onNodeDisabled", result, objthis, obj);
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "onKeyDown", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				VariantClosure obj = _this.getActionOwner();
				if( obj.mObject != null ) {
					if( param.length < 3 ) return Error.E_BADPARAMCOUNT;

					Dispatch2 evobj = EventManager.createEventObject( "onKeyDown", objthis, objthis );
					Variant evval = new Variant(evobj, evobj);
					// set member
					evobj.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "key", param[0], evobj );
					evobj.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "shift", param[1], evobj );
					evobj.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "process", param[2], evobj );
					Variant[] pevval = new Variant[1];
					pevval[0] = evval;
					obj.funcCall( 0, EventManager.ActionName, result, pevval, null );
				}
				// call default key down behavior handler
				if( param.length == 2 || (param.length >= 3 && param[2].asBoolean()) )
					_this.defaultKeyDown( param[0].asInteger(), param[1].asInteger() );

				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "onKeyUp", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				VariantClosure obj = _this.getActionOwner();
				if( obj.mObject != null ) {
					if( param.length < 3 ) return Error.E_BADPARAMCOUNT;

					Dispatch2 evobj = EventManager.createEventObject( "onKeyUp", objthis, objthis );
					Variant evval = new Variant(evobj, evobj);
					// set member
					evobj.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "key", param[0], evobj );
					evobj.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "shift", param[1], evobj );
					evobj.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "process", param[2], evobj );
					Variant[] pevval = new Variant[1];
					pevval[0] = evval;
					obj.funcCall( 0, EventManager.ActionName, result, pevval, null );
				}
				// call default key down behavior handler
				if( param.length == 2 || (param.length >= 3 && param[2].asBoolean()) )
					_this.defaultKeyUp( param[0].asInteger(), param[1].asInteger() );

				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "onKeyPress", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				VariantClosure obj = _this.getActionOwner();
				if( obj.mObject != null ) {
					if( param.length < 2 ) return Error.E_BADPARAMCOUNT;

					Dispatch2 evobj = EventManager.createEventObject( "onKeyPress", objthis, objthis );
					Variant evval = new Variant(evobj, evobj);
					// set member
					evobj.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "key", param[0], evobj );
					evobj.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "process", param[1], evobj );
					Variant[] pevval = new Variant[1];
					pevval[0] = evval;
					obj.funcCall( 0, EventManager.ActionName, result, pevval, null );
				}
				// call default key down behavior handler
				if( param.length == 1 || (param.length >= 2 && param[1].asBoolean()) ) {
					String p = param[0].asString();
					char code = 0;
					if( p.length() > 0 )
						code = p.charAt(0);
					_this.defaultKeyPress( code );
				}

				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "onMouseWheel", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				VariantClosure obj = _this.getActionOwner();
				if( obj.mObject != null ) {
					if( param.length < 4 ) return Error.E_BADPARAMCOUNT;

					Dispatch2 evobj = EventManager.createEventObject( "onMouseWheel", objthis, objthis );
					Variant evval = new Variant(evobj, evobj);
					// set member
					evobj.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "shift", param[0], evobj );
					evobj.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "delta", param[1], evobj );
					evobj.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "x", param[2], evobj );
					evobj.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "y", param[3], evobj );

					Variant[] pevval = new Variant[1];
					pevval[0] = evval;
					obj.funcCall( 0, EventManager.ActionName, result, pevval, null );
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "onSearchPrevFocusable", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				VariantClosure obj = _this.getActionOwner();
				if( obj.mObject != null ) {
					if( param.length < 1 ) return Error.E_BADPARAMCOUNT;

					Dispatch2 evobj = EventManager.createEventObject( "onSearchPrevFocusable", objthis, objthis );
					Variant evval = new Variant(evobj, evobj);
					// set member
					evobj.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "layer", param[0], evobj );
					Variant[] pevval = new Variant[1];
					pevval[0] = evval;
					obj.funcCall( 0, EventManager.ActionName, result, pevval, null );
				}
				// call default key down behavior handler
				if( param.length >= 1 && param[0].isVoid() != true ) {
					VariantClosure clo = param[0].asObjectClosure();
					if(clo.mObject != null ) {
						LayerNI src = null;
						if(clo.mObject!=null) {
							src = (LayerNI)clo.mObject.getNativeInstance( LayerClass.ClassID );
						}
						if( src == null ) Message.throwExceptionMessage(Message.SpecifyLayer);
						_this.setFocusWork(src);
					} else {
						_this.setFocusWork(null);
					}
				} else {
					_this.setFocusWork(null);
				}

				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "onSearchNextFocusable", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				VariantClosure obj = _this.getActionOwner();
				if( obj.mObject != null ) {
					if( param.length < 1 ) return Error.E_BADPARAMCOUNT;

					Dispatch2 evobj = EventManager.createEventObject( "onSearchNextFocusable", objthis, objthis );
					Variant evval = new Variant(evobj, evobj);
					// set member
					evobj.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "layer", param[0], evobj );
					Variant[] pevval = new Variant[1];
					pevval[0] = evval;
					obj.funcCall( 0, EventManager.ActionName, result, pevval, null );
				}
				// call default key down behavior handler
				if( param.length >= 1 && param[0].isVoid() != true ) {
					VariantClosure clo = param[0].asObjectClosure();
					if(clo.mObject != null ) {
						LayerNI src = null;
						if(clo.mObject!=null) {
							src = (LayerNI)clo.mObject.getNativeInstance( LayerClass.ClassID );
						}
						if( src == null ) Message.throwExceptionMessage(Message.SpecifyLayer);
						_this.setFocusWork(src);
					} else {
						_this.setFocusWork(null);
					}
				} else {
					_this.setFocusWork(null);
				}

				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "onBeforeFocus", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				VariantClosure obj = _this.getActionOwner();
				if( obj.mObject != null ) {
					if( param.length < 3 ) return Error.E_BADPARAMCOUNT;

					Dispatch2 evobj = EventManager.createEventObject( "onBeforeFocus", objthis, objthis );
					Variant evval = new Variant(evobj, evobj);
					// set member
					evobj.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "layer", param[0], evobj );
					evobj.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "blurred", param[1], evobj );
					evobj.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "direction", param[2], evobj );
					Variant[] pevval = new Variant[1];
					pevval[0] = evval;
					obj.funcCall( 0, EventManager.ActionName, result, pevval, null );
				}
				// call default key down behavior handler
				if( param.length >= 1 && param[0].isVoid() != true ) {
					VariantClosure clo = param[0].asObjectClosure();
					if(clo.mObject != null ) {
						LayerNI src = null;
						if(clo.mObject!=null) {
							src = (LayerNI)clo.mObject.getNativeInstance( LayerClass.ClassID );
						}
						if( src == null ) Message.throwExceptionMessage(Message.SpecifyLayer);
						_this.setFocusWork(src);
					} else {
						_this.setFocusWork(null);
					}
				} else {
					_this.setFocusWork(null);
				}

				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "onPaint", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				VariantClosure obj = _this.getActionOwner();
				if( obj.mObject != null )  callEventNoParam( "onNodeDisabled", result, objthis, obj);
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "onTransitionCompleted", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				VariantClosure obj = _this.getActionOwner();
				if( obj.mObject != null ) {
					if( param.length < 2 ) return Error.E_BADPARAMCOUNT;

					Dispatch2 evobj = EventManager.createEventObject( "onTransitionCompleted", objthis, objthis );
					Variant evval = new Variant(evobj, evobj);
					// set member
					evobj.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "dest", param[0], evobj );
					evobj.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "src", param[1], evobj );

					Variant[] pevval = new Variant[1];
					pevval[0] = evval;
					obj.funcCall( 0, EventManager.ActionName, result, pevval, null );
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );



		// property
		registerNCM( "parent", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				LayerNI parent = _this.getParent();
				if( parent!=null ) {
					Dispatch2 dsp = parent.getOwner();
					result.set( dsp, dsp );
				} else {
					result.set( null, null );
				}
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				LayerNI parent = null;
				VariantClosure clo = param.asObjectClosure();
				if(clo.mObject!=null) {
					parent = (LayerNI)clo.mObject.getNativeInstance( LayerClass.ClassID );
					if( parent == null  )
						Message.throwExceptionMessage(Message.SpecifyLayer);
				}
				_this.setParent( parent );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "children", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) throws VariantException, TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;

				Dispatch2 dsp = _this.getChildrenArrayObject();
				result.set( dsp, dsp );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "order", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getOrderIndex() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException, TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setOrderIndex(param.asInteger());
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "absolute", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getAbsoluteOrderIndex() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException, TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setAbsoluteOrderIndex( param.asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "absoluteOrderMode", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getAbsoluteOrderMode() ? 1 : 0 );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setAbsoluteOrderMode(param.asBoolean());
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "visible", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getVisible() ? 1 : 0 );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException, TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setVisible( param.asBoolean() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "cached", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getCached() ? 1 : 0 );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setCached( param.asBoolean() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "nodeVisible", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getNodeVisible() ? 1 : 0 );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "opacity", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getOpacity() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException, TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setOpacity( param.asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "window", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				WindowNI window = _this.getWindow();
				if( window == null ) {
					result.set( null, null );
				} else {
					Dispatch2 dsp = window.getOwner();
					result.set(dsp, dsp);
				}
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "isPrimary", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.isPrimary() ? 1 : 0 );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "left", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getLeft() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException, TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setLeft( param.asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "top", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getTop() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException, TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setTop( param.asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "width", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getWidth() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException, TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setWidth( param.asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "height", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getHeight() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException, TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setHeight( param.asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "imageLeft", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getImageLeft() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException, TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setImageLeft( param.asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "imageTop", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getImageTop() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException, TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setImageTop( param.asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "imageWidth", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getImageWidth() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException, TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setImageWidth( param.asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "imageHeight", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getImageHeight() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException, TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setImageHeight( param.asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "type", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getType() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException, TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setType( param.asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "face", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getFace() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setFace( param.asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "holdAlpha", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getHoldAlpha() ? 1 : 0 );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setHoldAlpha( param.asBoolean() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "clipLeft", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getClipLeft() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException, TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setClipLeft( param.asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "clipTop", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
			LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
			if( _this == null ) return Error.E_NATIVECLASSCRASH;
			result.set( _this.getClipTop() );
			return Error.S_OK;
		}
		@Override public int set(Variant param, Dispatch2 objthis) throws VariantException, TJSException {
			LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
			if( _this == null ) return Error.E_NATIVECLASSCRASH;
			_this.setClipTop( param.asInteger() );
			return Error.S_OK;
		}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "clipWidth", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getClipWidth() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException, TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setClipWidth( param.asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "clipHeight", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getClipHeight() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException, TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setClipHeight( param.asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "imageModified", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getImageModified() ? 1 : 0 );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setImageModified( param.asBoolean() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "hitType", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getHitType() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setHitType( param.asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "hitThreshold", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getHitThreshold() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setHitThreshold( param.asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "cursor", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getCursor() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if( param.isString() )
					_this.setCursorByStorage(param.asString());
				else
					_this.setCursorByNumber(param.asInteger());
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "cursorX", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getCursorX() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setCursorX( param.asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "cursorY", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getCursorY() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setCursorY( param.asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "hint", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getHint() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setHint( param.asString() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "showParentHint", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getShowParentHint() ? 1 : 0 );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setShowParentHint( param.asBoolean() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "focusable", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getFocusable() ? 1 : 0 );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setFocusable( param.asBoolean() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "prevFocusable", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				LayerNI lay = _this.getPrevFocusable();
				if(lay!=null) {
					Dispatch2 dsp = lay.getOwner();
					result.set(dsp, dsp);
				} else {
					result.set( null, null );
				}
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "nextFocusable", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				LayerNI lay = _this.getNextFocusable();
				if(lay!=null) {
					Dispatch2 dsp = lay.getOwner();
					result.set(dsp, dsp);
				} else {
					result.set( null, null );
				}
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "joinFocusChain", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getJoinFocusChain() ? 1 : 0 );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setJoinFocusChain( param.asBoolean() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "nodeFocusable", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getNodeFocusable() ? 1 : 0 );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "focused", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getFocused() ? 1 : 0 );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "enabled", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getEnabled() ? 1 : 0 );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setEnabled( param.asBoolean() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "nodeEnabled", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getNodeEnabled() ? 1 : 0 );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "attentionLeft", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getAttentionLeft() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setAttentionLeft( param.asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "attentionTop", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getAttentionTop() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setAttentionTop( param.asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "useAttention", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getUseAttention() ? 1 : 0 );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setUseAttention( param.asBoolean() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "imeMode", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getImeMode() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setImeMode( param.asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "callOnPaint", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getCallOnPaint() ? 1 : 0 );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setCallOnPaint( param.asBoolean() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "font", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				Dispatch2 dsp = _this.getFontObject();
				result.set( dsp, dsp );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "name", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getName() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setName( param.asString() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "neutralColor", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getNeutralColor() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setNeutralColor( param.asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "hasImage", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getHasImage() ? 1 : 0 );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws TJSException {
				LayerNI _this = (LayerNI)objthis.getNativeInstance(ClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setHasImage( param.asBoolean() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		/* TODO JAVA Áâà„ÅØ„Ç§„É≥„Éó„É™„É°„É≥„Éà„Åó„Å™„ÅÑ„ÄÇ‰ª£ÊõøÊñπÊ≥ï„Åå„ÅÇ„Çå„Å∞ËÄÉ„Åà„Çã */
		registerNCM( "mainImageBuffer", TVP.NotImplProp, CLASS_NAME, Interface.nitProperty, 0 );

		/* TODO JAVA Áâà„ÅØ„Ç§„É≥„Éó„É™„É°„É≥„Éà„Åó„Å™„ÅÑ„ÄÇ‰ª£ÊõøÊñπÊ≥ï„Åå„ÅÇ„Çå„Å∞ËÄÉ„Åà„Çã */
		registerNCM( "mainImageBufferForWrite", TVP.NotImplProp, CLASS_NAME, Interface.nitProperty, 0 );

		/* TODO JAVA Áâà„ÅØ„Ç§„É≥„Éó„É™„É°„É≥„Éà„Åó„Å™„ÅÑ„ÄÇ‰ª£ÊõøÊñπÊ≥ï„Åå„ÅÇ„Çå„Å∞ËÄÉ„Åà„Çã */
		registerNCM( "mainImageBufferPitch", TVP.NotImplProp, CLASS_NAME, Interface.nitProperty, 0 );

		/* TODO JAVA Áâà„ÅØ„Ç§„É≥„Éó„É™„É°„É≥„Éà„Åó„Å™„ÅÑ„ÄÇ‰ª£ÊõøÊñπÊ≥ï„Åå„ÅÇ„Çå„Å∞ËÄÉ„Åà„Çã */
		registerNCM( "provinceImageBuffer", TVP.NotImplProp, CLASS_NAME, Interface.nitProperty, 0 );

		/* TODO JAVA Áâà„ÅØ„Ç§„É≥„Éó„É™„É°„É≥„Éà„Åó„Å™„ÅÑ„ÄÇ‰ª£ÊõøÊñπÊ≥ï„Åå„ÅÇ„Çå„Å∞ËÄÉ„Åà„Çã */
		registerNCM( "provinceImageBufferForWrite", TVP.NotImplProp, CLASS_NAME, Interface.nitProperty, 0 );

		/* TODO JAVA Áâà„ÅØ„Ç§„É≥„Éó„É™„É°„É≥„Éà„Åó„Å™„ÅÑ„ÄÇ‰ª£ÊõøÊñπÊ≥ï„Åå„ÅÇ„Çå„Å∞ËÄÉ„Åà„Çã */
		registerNCM( "provinceImageBufferPitch", TVP.NotImplProp, CLASS_NAME, Interface.nitProperty, 0 );
	}

	static int callEventNoParam( final String name, Variant result, Dispatch2 objthis, VariantClosure owner ) throws VariantException, TJSException {
		Dispatch2 evobj = EventManager.createEventObject( name, objthis, objthis );
		Variant[] pevval = new Variant[1];
		pevval[0] = new Variant(evobj, evobj);
		owner.funcCall( 0, EventManager.ActionName, result, pevval, null );
		return Error.S_OK;
	}
}
