package com.voicematch.bouger.main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

@SuppressLint("HandlerLeak")
public class SpeechRecognition {
	private SpeechRecognizer mRecognizer;							//음성인식 객체
	private Context mContext = null;
	private Handler mMainHandler = null;
	
	private final int READY=0, BEGIN=1, END=2, FINISH=3;			//핸들러 메시지. 음성인식 준비, 끝, 앱 종료
	private Handler mHandler = new Handler(){
		public void handleMessage(Message msg){
			switch (msg.what) {
			case READY:
				break;
			case BEGIN:
				break;
			case END:
				break;
			case FINISH:
				stopRecognition();	// 음성인식을 한번만 수행하는 것이 아니라 계속 수행하기 위해 
				startRecognition();	// 음성인식이 완료되면 인식을 종료한 뒤 새로 실행한다.
				break;
			}
		}
	};
	
    public SpeechRecognition(Context context, Handler handler) {
    	mContext = context;
    	mMainHandler = handler;
    	
    	startRecognition();
    }
    
    public void startRecognition() {
		Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);				//음성인식 intent생성
		i.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, mContext.getPackageName());	//데이터 설정
		i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");							//음성인식 언어 설정
		
		mRecognizer = SpeechRecognizer.createSpeechRecognizer(mContext);				//음성인식 객체
		mRecognizer.setRecognitionListener(listener);									//음성인식 리스너 등록
		mRecognizer.startListening(i);													//음성인식 시작    	
    }
    
    public void stopRecognition() {
    	mRecognizer.stopListening();
    }
    
	//음성인식 리스너
	private RecognitionListener listener = new RecognitionListener() {
		
		//음성 인식 결과 받음
		public void onResults(Bundle results) {
			mHandler.sendEmptyMessage(FINISH);
			mMainHandler.obtainMessage(Main.MESSAGE_SPEECH_REC, results).sendToTarget();			
		}
		
		//음성 인식 준비가 되었으면
		public void onReadyForSpeech(Bundle params) {
			mHandler.sendEmptyMessage(READY);		//핸들러에 메시지 보냄
		}
		
		//입력이 시작되면
		public void onBeginningOfSpeech() {
			mHandler.sendEmptyMessage(BEGIN);
			mMainHandler.obtainMessage(Main.MESSAGE_SPEECH_REC_BEGIN).sendToTarget();
		}

		//음성 입력이 끝났으면
		public void onEndOfSpeech() {
			mHandler.sendEmptyMessage(END);		//핸들러에 메시지 보냄
			mMainHandler.obtainMessage(Main.MESSAGE_SPEECH_REC_END).sendToTarget();
		}
		
		//에러가 발생하면
		public void onError(int error) {
			switch(error) {
			case SpeechRecognizer.ERROR_CLIENT:						// 단말에서 오류가 발생했습니다.
				break;
			case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
			case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
				mMainHandler.obtainMessage(Main.MESSAGE_SPEECH_REC_ERROR, error, 0).sendToTarget();			
				break;
			case SpeechRecognizer.ERROR_AUDIO:
			case SpeechRecognizer.ERROR_NETWORK:
			case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
			case SpeechRecognizer.ERROR_SERVER:						// 서버에서 오류가 발생했습니다.
			case SpeechRecognizer.ERROR_NO_MATCH:					// 일치하는 항목이 없습니다.
			case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:				// 입력이 없습니다.
				mHandler.sendEmptyMessage(FINISH);
				break;
			}
		}

		//입력 소리 변경 시
		public void onRmsChanged(float rmsdB) {}						
		//인식 결과의 일부가 유효할 때
		public void onPartialResults(Bundle partialResults) {
			mHandler.sendEmptyMessage(FINISH);
		}		
		//미래의 이벤트를 추가하기 위해 미리 예약되어진 함수
		public void onEvent(int eventType, Bundle params) {}			
		//더 많은 소리를 받을 때
		public void onBufferReceived(byte[] buffer) {}				
	};

}
