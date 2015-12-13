/**
 ******************************************************************************
 * Copyright (c), Takenori Imoto
 * •– software http://www.kaede-software.com/
 * All rights reserved.
 ******************************************************************************
 * ƒ\[ƒXƒR[ƒhŒ`®‚©ƒoƒCƒiƒŠŒ`®‚©A•ÏX‚·‚é‚©‚µ‚È‚¢‚©‚ğ–â‚í‚¸AˆÈ‰º‚ÌğŒ‚ğ–
 * ‚½‚·ê‡‚ÉŒÀ‚èAÄ”Ğ•z‚¨‚æ‚Ñg—p‚ª‹–‰Â‚³‚ê‚Ü‚·B
 *
 * Eƒ\[ƒXƒR[ƒh‚ğÄ”Ğ•z‚·‚éê‡Aã‹L‚Ì’˜ìŒ •\¦A–{ğŒˆê——A‚¨‚æ‚Ñ‰º‹L–ÆÓ
 *   ğ€‚ğŠÜ‚ß‚é‚±‚ÆB
 * EƒoƒCƒiƒŠŒ`®‚ÅÄ”Ğ•z‚·‚éê‡A”Ğ•z•¨‚É•t‘®‚ÌƒhƒLƒ…ƒƒ“ƒg“™‚Ì‘—¿‚ÉAã‹L‚Ì
 *   ’˜ìŒ •\¦A–{ğŒˆê——A‚¨‚æ‚Ñ‰º‹L–ÆÓğ€‚ğŠÜ‚ß‚é‚±‚ÆB
 * E‘–Ê‚É‚æ‚é“Á•Ê‚Ì‹–‰Â‚È‚µ‚ÉA–{ƒ\ƒtƒgƒEƒFƒA‚©‚ç”h¶‚µ‚½»•i‚Ìé“`‚Ü‚½‚Í”Ì”„
 *   ‘£i‚ÉA‘gD‚Ì–¼‘O‚Ü‚½‚ÍƒRƒ“ƒgƒŠƒrƒ…[ƒ^[‚Ì–¼‘O‚ğg—p‚µ‚Ä‚Í‚È‚ç‚È‚¢B
 *
 * –{ƒ\ƒtƒgƒEƒFƒA‚ÍA’˜ìŒ Ò‚¨‚æ‚ÑƒRƒ“ƒgƒŠƒrƒ…[ƒ^[‚É‚æ‚Á‚ÄuŒ»ó‚Ì‚Ü‚Üv’ñ‹Ÿ
 * ‚³‚ê‚Ä‚¨‚èA–¾¦–Ù¦‚ğ–â‚í‚¸A¤‹Æ“I‚Èg—p‰Â”\«A‚¨‚æ‚Ñ“Á’è‚Ì–Ú“I‚É‘Î‚·‚é“K
 * ‡«‚ÉŠÖ‚·‚éˆÃ–Ù‚Ì•ÛØ‚àŠÜ‚ßA‚Ü‚½‚»‚ê‚ÉŒÀ’è‚³‚ê‚È‚¢A‚¢‚©‚È‚é•ÛØ‚à‚ ‚è‚Ü‚¹
 * ‚ñB’˜ìŒ Ò‚àƒRƒ“ƒgƒŠƒrƒ…[ƒ^[‚àA–—R‚Ì‚¢‚©‚ñ‚ğ–â‚í‚¸A‘¹ŠQ”­¶‚ÌŒ´ˆö‚¢‚©
 * ‚ñ‚ğ–â‚í‚¸A‚©‚ÂÓ”C‚Ìª‹’‚ªŒ_–ñ‚Å‚ ‚é‚©ŒµŠiÓ”C‚Å‚ ‚é‚©i‰ß¸‚»‚Ì‘¼‚Ìj•s–@
 * sˆ×‚Å‚ ‚é‚©‚ğ–â‚í‚¸A‰¼‚É‚»‚Ì‚æ‚¤‚È‘¹ŠQ‚ª”­¶‚·‚é‰Â”\«‚ğ’m‚ç‚³‚ê‚Ä‚¢‚½‚Æ‚µ
 * ‚Ä‚àA–{ƒ\ƒtƒgƒEƒFƒA‚Ìg—p‚É‚æ‚Á‚Ä”­¶‚µ‚½i‘ã‘Ö•i‚Ü‚½‚Í‘ã—pƒT[ƒrƒX‚Ì’²’BA
 * g—p‚Ì‘r¸Aƒf[ƒ^‚Ì‘r¸A—˜‰v‚Ì‘r¸A‹Æ–±‚Ì’†’f‚àŠÜ‚ßA‚Ü‚½‚»‚ê‚ÉŒÀ’è‚³‚ê‚È
 * ‚¢j’¼Ú‘¹ŠQAŠÔÚ‘¹ŠQA‹ô”­“I‚È‘¹ŠQA“Á•Ê‘¹ŠQA’¦”±“I‘¹ŠQA‚Ü‚½‚ÍŒ‹‰Ê‘¹ŠQ‚É
 * ‚Â‚¢‚ÄAˆêØÓ”C‚ğ•‰‚í‚È‚¢‚à‚Ì‚Æ‚µ‚Ü‚·B
 ******************************************************************************
 * –{ƒ\ƒtƒgƒEƒFƒA‚ÍA‹g—¢‹g—¢2 ( http://kikyou.info/tvp/ ) ‚Ìƒ\[ƒXƒR[ƒh‚ğJava
 * ‚É‘‚«Š·‚¦‚½‚à‚Ì‚ğˆê•”g—p‚µ‚Ä‚¢‚Ü‚·B
 * ‹g—¢‹g—¢2 Copyright (C) W.Dee <dee@kikyou.info> and contributors
 ******************************************************************************
 */
package jp.kirikiri.tvp2.sound;

import jp.kirikiri.tjs2.Error;
import jp.kirikiri.tjs2.Interface;
import jp.kirikiri.tjs2.Dispatch2;
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


public class WaveSoundBufferClass extends NativeClass {

	static int mClassID = -1;
	static private final String CLASS_NAME = "WaveSoundBuffer";

	protected NativeInstance createNativeInstance() {
		return new WaveSoundBufferNI();
	}

	public WaveSoundBufferClass() throws VariantException, TJSException {
		super( CLASS_NAME );
		final int NCM_CLASSID = TJS.registerNativeClass(CLASS_NAME);
		setClassID( NCM_CLASSID );
		mClassID = NCM_CLASSID;

		// constructor
		registerNCM( CLASS_NAME, new NativeClassConstructor() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException, TJSException {
				WaveSoundBufferNI _this = (WaveSoundBufferNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				int hr = _this.construct(param, objthis);
				if( hr < 0 ) return hr;
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );


		registerNCM( "finalize", TVP.ReturnOKMethod, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "open", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				WaveSoundBufferNI _this = (WaveSoundBufferNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if( param.length < 1) return Error.E_BADPARAMCOUNT;
				_this.open( param[0].asString() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "play", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				WaveSoundBufferNI _this = (WaveSoundBufferNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.play();
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "stop", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				WaveSoundBufferNI _this = (WaveSoundBufferNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.stop();
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "fade", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				WaveSoundBufferNI _this = (WaveSoundBufferNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if( param.length < 2) return Error.E_BADPARAMCOUNT;
				int to;
				int time;
				int delay = 0;
				to = param[0].asInteger();
				time = param[1].asInteger();
				if( param.length >= 3 && param[2].isVoid() != true )
					delay = param[2].asInteger();
				_this.fade( to, time, delay );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "stopFade", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				WaveSoundBufferNI _this = (WaveSoundBufferNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.stopFade(false,true);
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "setPos", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws VariantException {
				WaveSoundBufferNI _this = (WaveSoundBufferNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				if( param.length < 3) return Error.E_BADPARAMCOUNT;
				double x, y , z;
				x = param[0].asDouble();
				y = param[1].asDouble();
				z = param[2].asDouble();

				_this.setPos( x, y, z );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );


		// event
		registerNCM( "onStatusChanged", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				WaveSoundBufferNI _this = (WaveSoundBufferNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				VariantClosure obj = _this.getActionOwner();
				if( obj.mObject != null ) {
					if( param.length < 1 ) return Error.E_BADPARAMCOUNT;

					Dispatch2 evobj = EventManager.createEventObject( "onStatusChanged", objthis, objthis );
					Variant evval = new Variant(evobj, evobj);
					// set member
					evobj.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "status", param[0], evobj );

					Variant[] pevval = {evval};
					obj.funcCall( 0, EventManager.ActionName, result, pevval, null );
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "onFadeCompleted", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				WaveSoundBufferNI _this = (WaveSoundBufferNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				VariantClosure obj = _this.getActionOwner();
				if( obj.mObject != null ) {
					Dispatch2 evobj = EventManager.createEventObject( "onFadeCompleted", objthis, objthis );
					Variant evval = new Variant(evobj, evobj);

					Variant[] pevval = {evval};
					obj.funcCall( 0, EventManager.ActionName, result, pevval, null );
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );

		registerNCM( "onLabel", new NativeClassMethod() {
			@Override
			protected int process(Variant result, Variant[] param, Dispatch2 objthis) throws TJSException {
				WaveSoundBufferNI _this = (WaveSoundBufferNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				VariantClosure obj = _this.getActionOwner();
				if( obj.mObject != null ) {
					if( param.length < 1 ) return Error.E_BADPARAMCOUNT;

					Dispatch2 evobj = EventManager.createEventObject( "onLabel", objthis, objthis );
					Variant evval = new Variant(evobj, evobj);
					// set member
					evobj.propSet( Interface.MEMBERENSURE|Interface.IGNOREPROP, "name", param[0], evobj );

					Variant[] pevval = {evval};
					obj.funcCall( 0, EventManager.ActionName, result, pevval, null );
				}
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitMethod, 0 );


		registerNCM( "position", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				WaveSoundBufferNI _this = (WaveSoundBufferNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getPosition() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				WaveSoundBufferNI _this = (WaveSoundBufferNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setPosition( param.asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "samplePosition", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				WaveSoundBufferNI _this = (WaveSoundBufferNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getSamplePosition() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				WaveSoundBufferNI _this = (WaveSoundBufferNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setSamplePosition( param.asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "paused", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				WaveSoundBufferNI _this = (WaveSoundBufferNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getPaused() ? 1 : 0 );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) {
				WaveSoundBufferNI _this = (WaveSoundBufferNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setPaused( param.asBoolean() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "totalTime", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				WaveSoundBufferNI _this = (WaveSoundBufferNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getTotalTime() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) {
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "looping", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				WaveSoundBufferNI _this = (WaveSoundBufferNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getLooping() ? 1 : 0 );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) {
				WaveSoundBufferNI _this = (WaveSoundBufferNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setLooping( param.asBoolean() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "volume", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				WaveSoundBufferNI _this = (WaveSoundBufferNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getVolume() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				WaveSoundBufferNI _this = (WaveSoundBufferNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setVolume( param.asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "volume2", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				WaveSoundBufferNI _this = (WaveSoundBufferNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getVolume2() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				WaveSoundBufferNI _this = (WaveSoundBufferNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setVolume2( param.asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "pan", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				WaveSoundBufferNI _this = (WaveSoundBufferNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getPan() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				WaveSoundBufferNI _this = (WaveSoundBufferNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setPan( param.asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "posX", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				WaveSoundBufferNI _this = (WaveSoundBufferNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getPosX() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				WaveSoundBufferNI _this = (WaveSoundBufferNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setPosX( param.asDouble() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "posY", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				WaveSoundBufferNI _this = (WaveSoundBufferNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getPosY() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				WaveSoundBufferNI _this = (WaveSoundBufferNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setPosY( param.asDouble() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "posZ", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				WaveSoundBufferNI _this = (WaveSoundBufferNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getPosZ() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				WaveSoundBufferNI _this = (WaveSoundBufferNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setPosZ( param.asDouble() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "status", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				WaveSoundBufferNI _this = (WaveSoundBufferNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getStatusString() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "frequency", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				WaveSoundBufferNI _this = (WaveSoundBufferNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getFrequency() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				WaveSoundBufferNI _this = (WaveSoundBufferNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				_this.setFrequency( param.asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "bits", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				WaveSoundBufferNI _this = (WaveSoundBufferNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getBitsPerSample() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "channels", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				WaveSoundBufferNI _this = (WaveSoundBufferNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				result.set( _this.getChannels() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "flags", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				WaveSoundBufferNI _this = (WaveSoundBufferNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				Dispatch2 dsp = _this.getWaveFlagsObject();
				result.set( dsp, dsp );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "labels", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				WaveSoundBufferNI _this = (WaveSoundBufferNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				Dispatch2 dsp = _this.getWaveLabelsObject();
				result.set( dsp, dsp );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "filters", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				WaveSoundBufferNI _this = (WaveSoundBufferNI)objthis.getNativeInstance(mClassID);
				if( _this == null ) return Error.E_NATIVECLASSCRASH;
				Dispatch2 dsp = _this.getWaveFiltersObject();
				result.set( dsp, dsp );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) { return Error.E_ACCESSDENYED; }
		}, CLASS_NAME, Interface.nitProperty, 0 );

		registerNCM( "globalVolume", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				result.set( TVP.SoundMixer.getGlobalVolume() );
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) throws VariantException {
				TVP.SoundMixer.setGlobalVolume( param.asInteger() );
				return Error.S_OK;
			}
		}, CLASS_NAME, Interface.nitProperty, Interface.STATICMEMBER );

		// sgfmNeverMute = 0, sgfmMuteOnMinimize = 1, sgfmMuteOnDeactivate = 2
		registerNCM( "globalFocusMode", new NativeClassProperty() {
			@Override public int get(Variant result, Dispatch2 objthis) {
				result.set( 0 ); // sgfmNeverMute ãƒŸãƒ¥ãƒ¼ãƒˆã—ãªã„
				return Error.S_OK;
			}
			@Override public int set(Variant param, Dispatch2 objthis) {
				return Error.S_OK;	// è¨­å®šä¸å¯
			}
		}, CLASS_NAME, Interface.nitProperty, Interface.STATICMEMBER );
	}

}
