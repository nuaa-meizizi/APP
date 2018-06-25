package com.scy.health.util;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.scy.health.Interface.XfyunInterface;
import com.scy.health.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class XfyunASR {
    private static final String TAG = "XfyunASR";
    private Context context;
    private RecognizerDialog mDialog;
    XfyunInterface myInterface;
    // 用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<String , String>();
    public XfyunASR(Context context){
        this.context = context;
    }
    public void speekText(String s) {
        if (s.equals("请吩咐")){           //优先播放本地音频
            MediaPlayer mMediaPlayer= MediaPlayer.create(context, R.raw.response);
            mMediaPlayer.start();
        }
        else if (s.equals("好的")){
            MediaPlayer mMediaPlayer= MediaPlayer.create(context, R.raw.yessir);
            mMediaPlayer.start();
        }
        else if (s.equals("我不能理解你的命令")){
            MediaPlayer mMediaPlayer= MediaPlayer.create(context, R.raw.failunderstand);
            mMediaPlayer.start();
        }
        else if (s.equals("警报")){
            MediaPlayer mMediaPlayer= MediaPlayer.create(context, R.raw.alert);
            mMediaPlayer.start();
        }
        else if (s.equals("联系紧急联系人")){
            MediaPlayer mMediaPlayer= MediaPlayer.create(context, R.raw.callphone);
            mMediaPlayer.start();
        }
        else if (s.equals("取消报警")){
            MediaPlayer mMediaPlayer= MediaPlayer.create(context, R.raw.cancel);
            mMediaPlayer.start();
        }
        else{
            SpeechSynthesizer mTts = SpeechSynthesizer.createSynthesizer( context, null);
            mTts.setParameter(SpeechConstant. VOICE_NAME, "xiaoyu" ); // 设置发音人
            mTts.setParameter(SpeechConstant. SPEED, "50" );// 设置语速
            mTts.setParameter(SpeechConstant. VOLUME, "100" );// 设置音量，范围 0~100
            mTts.setParameter(SpeechConstant. ENGINE_TYPE, SpeechConstant. TYPE_CLOUD); //设置云端
            // mTts.setParameter(SpeechConstant. TTS_AUDIO_PATH, "/storage/emulated/0/AudioRecorder/iflytek." );
            //3.开始合成
            mTts.startSpeaking( s, new MySynthesizerListener()) ;
        }
    }

    public void startSpeechDialog(XfyunInterface myInterface) {
        this.myInterface = myInterface;
        //yessir. 创建RecognizerDialog对象
        mDialog = new RecognizerDialog(context, new MyInitListener()) ;
        //2. 设置accent、 language等参数
        mDialog.setParameter(SpeechConstant. LANGUAGE, "zh_cn" );// 设置中文
        mDialog.setParameter(SpeechConstant. ACCENT, "mandarin" );
        // 若要将UI控件用于语义理解，必须添加以下参数设置，设置之后 onResult回调返回将是语义理解
        // 结果
        // mDialog.setParameter("asr_sch", "yessir");
        // mDialog.setParameter("nlp_version", "2.0");
        //3.设置回调接口
        mDialog.setListener(new MyRecognizerDialogListener()) ;
        //4. 显示dialog，接收语音输入
        mDialog.show();
    }

    /**
     * 语音识别
     */
    protected void startSpeech() {
        //yessir. 创建SpeechRecognizer对象，第二个参数： 本地识别时传 InitListener
        SpeechRecognizer mIat = SpeechRecognizer.createRecognizer( context, null); //语音识别器
        //2. 设置听写参数，详见《 MSC Reference Manual》 SpeechConstant类
        mIat.setParameter(SpeechConstant. DOMAIN, "iat" );// 短信和日常用语： iat (默认)
        mIat.setParameter(SpeechConstant. LANGUAGE, "zh_cn" );// 设置中文
        mIat.setParameter(SpeechConstant. ACCENT, "mandarin" );// 设置普通话
        //3. 开始听写
        mIat.startListening( mRecoListener);
    }

    // 听写监听器
    private RecognizerListener mRecoListener = new RecognizerListener() {
        public void onResult(RecognizerResult results, boolean isLast) {
            Log.e (TAG, results.getResultString());
            System.out.println(results.getResultString()) ;
            showTip(results.getResultString()) ;
        }

        // 会话发生错误回调接口
        public void onError(SpeechError error) {
            showTip(error.getPlainDescription(true)) ;
            // 获取错误码描述
            Log. e(TAG, "error.getPlainDescription(true)==" + error.getPlainDescription(true ));
        }

        // 开始录音
        public void onBeginOfSpeech() {
            showTip(" 开始录音 ");
        }

        //volume 音量值0~30， data音频数据
        public void onVolumeChanged(int volume, byte[] data) {
            showTip(" 声音改变了 ");
        }

        // 结束录音
        public void onEndOfSpeech() {
            showTip(" 结束录音 ");
        }

        // 扩展用接口
        public void onEvent(int eventType, int arg1 , int arg2, Bundle obj) {
        }
    };

    private void showTip (String data) {
        Toast.makeText( context, data, Toast.LENGTH_SHORT).show() ;
    }

    class MyRecognizerDialogListener implements RecognizerDialogListener {
        /**
         * @param results
         * @param isLast  是否说完了
         */
        @Override
        public void onResult (RecognizerResult results, boolean isLast){
            String result = results.getResultString(); //为解析的
            String text = JsonParser.parseIatResult(result);//解析过后的
            String sn = null;
            // 读取json结果中的 sn字段
            try {
                JSONObject resultJson = new JSONObject(results.getResultString());
                sn = resultJson.optString("sn");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            mIatResults.put(sn, text);//没有得到一句，添加到

            StringBuffer resultBuffer = new StringBuffer();
            for (String key : mIatResults.keySet()) {
                resultBuffer.append(mIatResults.get(key));
            }
            if (isLast){
                myInterface.GetData(resultBuffer.toString());   //结果回调
            }
        }

        @Override
        public void onError (SpeechError speechError){
            if(speechError.getErrorCode() == 10118)
            {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mDialog.dismiss();
                myInterface.onError("ERRPR");
            }
        }
    }

    class MyInitListener implements InitListener {
        @Override
        public void onInit(int code) {
            if (code != ErrorCode.SUCCESS) {
            }

        }
    }

    class MySynthesizerListener implements SynthesizerListener {

        @Override
        public void onSpeakBegin() {
            Log.d(TAG," 开始播放 ");
        }

        @Override
        public void onSpeakPaused() {
            Log.d(TAG," 暂停播放 ");
        }

        @Override
        public void onSpeakResumed() {
            Log.d(TAG," 继续播放 ");
        }

        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos ,String info) {
            // 合成进度
        }

        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
            // 播放进度
        }

        @Override
        public void onCompleted(SpeechError error) {
            if (error == null) {
                Log.d(TAG,"播放完成");
            } else if (error != null ) {
                System.out.println("!!!!!!");
                Log.d(TAG,error.getPlainDescription( true));
            }
        }

        @Override
        public void onEvent(int eventType, int arg1 , int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话 id，当业务出错时将会话 id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话 id为null
            //if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //     String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //     Log.d(TAG, "session id =" + sid);
            //}
        }
    }
}
