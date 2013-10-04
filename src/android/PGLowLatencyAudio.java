/*
THIS SOFTWARE IS PROVIDED BY ANDREW TRICE "AS IS" AND ANY EXPRESS OR
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
EVENT SHALL ANDREW TRICE OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package com.phonegap;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;

/**
 * @author triceam
 *
 */
public class PGLowLatencyAudio extends CordovaPlugin {

	public static final String ERROR_NO_AUDIOID="A reference does not exist for the specified audio id.";
	public static final String ERROR_AUDIOID_EXISTS="A reference already exists for the specified audio id.";
	
	public static final String PRELOAD_FX="preloadFX";
	public static final String PRELOAD_AUDIO="preloadAudio";
	public static final String PLAY="play";
	public static final String STOP="stop";
	public static final String LOOP="loop";
	public static final String UNLOAD="unload";
	
	public static final int DEFAULT_POLYPHONY_VOICES = 15;
	

	private static SoundPool soundPool;
	private static HashMap<String, PGLowLatencyAudioAsset> assetMap; 
	private static HashMap<String, Integer> soundMap; 
	private static HashMap<String, ArrayList<Integer>> streamMap; 
	
	/* (non-Javadoc)
	 * @see com.phonegap.api.Plugin#execute(java.lang.String, org.json.JSONArray, java.lang.String)
	 */
	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException 
	{
		initSoundPool();

		
		try 
		{
			String audioID = args.getString(0);
			Log.d(audioID, action);
	
			if ( PRELOAD_FX.equals( action ) ) 
			{
				if ( !soundMap.containsKey(audioID) )
				{
					String assetPath =args.getString(1);
					String fullPath = "www/".concat( assetPath );
					
					AssetManager am =   cordova.getActivity().getResources().getAssets(); 
					AssetFileDescriptor afd = am.openFd(fullPath);
					int assetIntID = soundPool.load(afd, 1);
					soundMap.put( audioID , assetIntID );
					
					callbackContext.success("OK");
					return true;
				}
				else 
				{
					callbackContext.error(ERROR_AUDIOID_EXISTS);
					return false;
				}
			}
			else if ( PRELOAD_AUDIO.equals( action ) ) 
			{
				if ( !assetMap.containsKey(audioID) )
				{
					String assetPath =args.getString(1);
					int voices;
					if ( args.length() < 2 )
					{
						voices = 0;
					}
					else
					{
						voices = args.getInt(2);
					}
					
					String fullPath = "www/".concat( assetPath );
					
					AssetManager am = cordova.getActivity().getResources().getAssets(); 
					AssetFileDescriptor afd = am.openFd(fullPath);
					
					PGLowLatencyAudioAsset asset = new PGLowLatencyAudioAsset( afd, voices );
					assetMap.put( audioID , asset );
					
					callbackContext.success("OK");
					return true;
				}
				else 
				{
					callbackContext.error(ERROR_AUDIOID_EXISTS);
					return false;
				}
			}
			else if ( PLAY.equals( action ) || LOOP.equals( action ) ) 
			{
				if ( assetMap.containsKey(audioID) )
				{
					PGLowLatencyAudioAsset asset = assetMap.get( audioID );
					if ( LOOP.equals( action ) ) 
						asset.loop();
					else
						asset.play();
					
					callbackContext.success("OK");
					return true;
				}
				else if ( soundMap.containsKey(audioID) )
				{
					int loops = 0;
					if ( LOOP.equals( action ) ) {
						loops = -1;
					}
					
					ArrayList<Integer> streams = streamMap.get( audioID );
					if ( streams == null )
						streams = new ArrayList<Integer>();
					
					int assetIntID = soundMap.get( audioID );
					int streamID = soundPool.play( assetIntID, 1, 1, 1, loops, 1);
					streams.add( streamID );
					streamMap.put( audioID , streams );
					
					callbackContext.success("OK");
					return true;
				}
				else 
				{
					callbackContext.error(ERROR_NO_AUDIOID);
					return false;
				}
			}
			else if ( STOP.equals( action ) || UNLOAD.equals( action ) ) 
			{
				if ( assetMap.containsKey(audioID) )
				{
					PGLowLatencyAudioAsset asset = assetMap.get( audioID );
					asset.stop();
					
					callbackContext.success("OK");
					return true;
				}
				else if ( soundMap.containsKey(audioID) )
				{
					ArrayList<Integer> streams = streamMap.get( audioID );
					if ( streams != null )
					{
						for ( int x=0; x< streams.size(); x++)
						soundPool.stop( streams.get(x) );
					}
					streamMap.remove( audioID );
					
					callbackContext.success("OK");
					return true;
				}
				else 
				{
					callbackContext.error(ERROR_NO_AUDIOID);
					return false;
				}
			}
			
			if ( UNLOAD.equals( action ) ) 
			{
				if ( assetMap.containsKey(audioID) )
				{
					PGLowLatencyAudioAsset asset = assetMap.get( audioID );
					asset.unload();
					assetMap.remove( audioID );
					
					callbackContext.success("OK");
					return true;
				}
				else if ( soundMap.containsKey(audioID) ){
					//streams unloaded and stopped above
					int assetIntID = soundMap.get( audioID );
					soundMap.remove( audioID );
					soundPool.unload( assetIntID );
					
					callbackContext.success("OK");
					return true;
				}
				else 
				{
					callbackContext.error(ERROR_NO_AUDIOID);
					return false;
				}
			}
			
		} 
		catch (Exception ex) 
		{
			callbackContext.error(ex.toString());
			return false;
		}
		return true;
	}

	private void initSoundPool() 
	{
		if ( soundPool == null ) 
		{
			soundPool = new SoundPool(DEFAULT_POLYPHONY_VOICES, AudioManager.STREAM_MUSIC, 1);
		}
		
		if ( soundMap == null) 
		{
			soundMap = new HashMap<String, Integer>();
		}
		
		if ( streamMap == null) 
		{
			streamMap = new HashMap<String, ArrayList<Integer>>();
		}
		
		if ( assetMap ==null ) 
		{
			assetMap = new HashMap<String, PGLowLatencyAudioAsset>();
		}
	}
}
