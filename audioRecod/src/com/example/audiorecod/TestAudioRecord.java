package com.example.audiorecod;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import android.app.Activity;
import android.graphics.PixelFormat;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

/**
 * class name��TestAudioRecord<BR>
 * class description����AudioRecord������¼��<BR>
 * PS�� <BR>
 * 
 * @version 1.00 2011/09/21
 * @author CODYY)peijiangping
 */
public class TestAudioRecord extends Activity {
	// ��Ƶ��ȡԴ
	private int audioSource = MediaRecorder.AudioSource.MIC;
	// ������Ƶ�����ʣ�44100��Ŀǰ�ı�׼������ĳЩ�豸��Ȼ֧��22050��16000��11025
	private static int sampleRateInHz = 44100;
	// ������Ƶ��¼�Ƶ�����CHANNEL_IN_STEREOΪ˫������CHANNEL_CONFIGURATION_MONOΪ������
	private static int channelConfig = AudioFormat.CHANNEL_IN_STEREO;
	// ��Ƶ���ݸ�ʽ:PCM 16λÿ����������֤�豸֧�֡�PCM 8λÿ����������һ���ܵõ��豸֧�֡�
	private static int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
	// �������ֽڴ�С
	private int bufferSizeInBytes = 0;
	private Button Start;
	private Button Stop;
	private AudioRecord audioRecord;
	private boolean isRecord = false;// ��������¼�Ƶ�״̬
	//AudioName����Ƶ�����ļ�
	private static final String AudioName = "/sdcard/love.raw";
	//NewAudioName�ɲ��ŵ���Ƶ�ļ�
	private static final String NewAudioName = "/sdcard/new.wav";

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFormat(PixelFormat.TRANSLUCENT);// �ý������
		requestWindowFeature(Window.FEATURE_NO_TITLE);// ȥ���������
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		// �������ý����С
		setContentView(R.layout.activity_main);
		init();
	}

	private void init() {
		Start = (Button) this.findViewById(R.id.start_bt);
		Stop = (Button) this.findViewById(R.id.end_bt);
		Start.setOnClickListener(new TestAudioListener());
		Stop.setOnClickListener(new TestAudioListener());
		creatAudioRecord();
	}

	private void creatAudioRecord() {
		// ��û������ֽڴ�С
		bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz,
				channelConfig, audioFormat);
		// ����AudioRecord����
		audioRecord = new AudioRecord(audioSource, sampleRateInHz,
				channelConfig, audioFormat, bufferSizeInBytes);
	}

	class TestAudioListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			if (v == Start) {
				startRecord();
			}
			if (v == Stop) {
				stopRecord();
			}

		}

	}

	private void startRecord() {
		audioRecord.startRecording();
		// ��¼��״̬Ϊtrue
		isRecord = true;
		// ������Ƶ�ļ�д���߳�
		new Thread(new AudioRecordThread()).start();
	}

	private void stopRecord() {
		close();
	}

	private void close() {
		if (audioRecord != null) {
			System.out.println("stopRecord");
			isRecord = false;//ֹͣ�ļ�д��
			audioRecord.stop();
			audioRecord.release();//�ͷ���Դ
			audioRecord = null;
		}
	}

	class AudioRecordThread implements Runnable {
		@Override
		public void run() {
			writeDateTOFile();//���ļ���д��������
			copyWaveFile(AudioName, NewAudioName);//�������ݼ���ͷ�ļ�
		}
	}

	/**
	 * ���ｫ����д���ļ������ǲ����ܲ��ţ���ΪAudioRecord��õ���Ƶ��ԭʼ������Ƶ��
	 * �����Ҫ���žͱ������һЩ��ʽ���߱����ͷ��Ϣ�����������ĺô���������Զ���Ƶ�� �����ݽ��д�����������Ҫ��һ����˵����TOM
	 * è������ͽ�����Ƶ�Ĵ�����Ȼ�����·�װ ����˵�����õ�����Ƶ�Ƚ�������һЩ��Ƶ�Ĵ�����
	 */
	private void writeDateTOFile() {
		// newһ��byte����������һЩ�ֽ����ݣ���СΪ��������С
		byte[] audiodata = new byte[bufferSizeInBytes];
		FileOutputStream fos = null;
		int readsize = 0;
		try {
			File file = new File(AudioName);
			if (file.exists()) {
				file.delete();
			}
			fos = new FileOutputStream(file);// ����һ���ɴ�ȡ�ֽڵ��ļ�
		} catch (Exception e) {
			e.printStackTrace();
		}
		while (isRecord == true) {
			readsize = audioRecord.read(audiodata, 0, bufferSizeInBytes);
			if (AudioRecord.ERROR_INVALID_OPERATION != readsize) {
				try {
					fos.write(audiodata);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		try {
			fos.close();// �ر�д����
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// ����õ��ɲ��ŵ���Ƶ�ļ�
	private void copyWaveFile(String inFilename, String outFilename) {
		FileInputStream in = null;
		FileOutputStream out = null;
		long totalAudioLen = 0;
		long totalDataLen = totalAudioLen + 36;
		long longSampleRate = sampleRateInHz;
		int channels = 2;
		long byteRate = 16 * sampleRateInHz * channels / 8;
		byte[] data = new byte[bufferSizeInBytes];
		try {
			in = new FileInputStream(inFilename);
			out = new FileOutputStream(outFilename);
			totalAudioLen = in.getChannel().size();
			totalDataLen = totalAudioLen + 36;
			WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
					longSampleRate, channels, byteRate);
			while (in.read(data) != -1) {
				out.write(data);
			}
			in.close();
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * �����ṩһ��ͷ��Ϣ��������Щ��Ϣ�Ϳ��Եõ����Բ��ŵ��ļ���
	 * Ϊ��Ϊɶ������44���ֽڣ��������û�����о�������������һ��wav
	 * ��Ƶ���ļ������Է���ǰ���ͷ�ļ�����˵����һ��Ŷ��ÿ�ָ�ʽ���ļ�����
	 * �Լ����е�ͷ�ļ���
	 */
	private void WriteWaveFileHeader(FileOutputStream out, long totalAudioLen,
			long totalDataLen, long longSampleRate, int channels, long byteRate)
			throws IOException {
		byte[] header = new byte[44];
		header[0] = 'R'; // RIFF/WAVE header
		header[1] = 'I';
		header[2] = 'F';
		header[3] = 'F';
		header[4] = (byte) (totalDataLen & 0xff);
		header[5] = (byte) ((totalDataLen >> 8) & 0xff);
		header[6] = (byte) ((totalDataLen >> 16) & 0xff);
		header[7] = (byte) ((totalDataLen >> 24) & 0xff);
		header[8] = 'W';
		header[9] = 'A';
		header[10] = 'V';
		header[11] = 'E';
		header[12] = 'f'; // 'fmt ' chunk
		header[13] = 'm';
		header[14] = 't';
		header[15] = ' ';
		header[16] = 16; // 4 bytes: size of 'fmt ' chunk
		header[17] = 0;
		header[18] = 0;
		header[19] = 0;
		header[20] = 1; // format = 1
		header[21] = 0;
		header[22] = (byte) channels;
		header[23] = 0;
		header[24] = (byte) (longSampleRate & 0xff);
		header[25] = (byte) ((longSampleRate >> 8) & 0xff);
		header[26] = (byte) ((longSampleRate >> 16) & 0xff);
		header[27] = (byte) ((longSampleRate >> 24) & 0xff);
		header[28] = (byte) (byteRate & 0xff);
		header[29] = (byte) ((byteRate >> 8) & 0xff);
		header[30] = (byte) ((byteRate >> 16) & 0xff);
		header[31] = (byte) ((byteRate >> 24) & 0xff);
		header[32] = (byte) (2 * 16 / 8); // block align
		header[33] = 0;
		header[34] = 16; // bits per sample
		header[35] = 0;
		header[36] = 'd';
		header[37] = 'a';
		header[38] = 't';
		header[39] = 'a';
		header[40] = (byte) (totalAudioLen & 0xff);
		header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
		header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
		header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
		out.write(header, 0, 44);
	}

	@Override
	protected void onDestroy() {
		close();
		super.onDestroy();
	}
}