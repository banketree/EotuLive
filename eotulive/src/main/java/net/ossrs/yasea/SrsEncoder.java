package net.ossrs.yasea;

import android.content.res.Configuration;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Leo Ma on 4/1/2016.
 */
class SrsEncoder {
    private static final String TAG = "SrsEncoder";

    public static final String VCODEC = "video/avc";
    public static final String ACODEC = "audio/mp4a-latm";
    public static String x264Preset = "veryfast";
    public static int vPrevWidth = 1280;
    public static int vPrevHeight = 720;
    public static int vPortraitWidth = 384;
    public static int vPortraitHeight = 640;
    public static int vLandscapeWidth = 640;
    public static int vLandscapeHeight = 384;
    public static int vOutWidth = 640;   // Note: the stride of resolution must be set as 16x for hard encoding with some chip like MTK
    public static int vOutHeight = 384;  // Since Y component is quadruple size as U and V component, the stride must be set as 32x
    public static int vBitrate = 500 * 1000;  // 500kbps
    public static final int VFPS = 12;
    public static final int VGOP = 48;
    public static final int ASAMPLERATE = 44100;
    public static int aChannelConfig = AudioFormat.CHANNEL_IN_STEREO;
    public static final int ABITRATE = 32 * 1000;  // 32kbps


    protected native void setEncoderResolution(int outWidth, int outHeight);

    protected native void setEncoderFps(int fps);

    protected native void setEncoderGop(int gop);

    protected native void setEncoderBitrate(int bitrate);

    protected native void setEncoderPreset(String preset);

    protected native byte[] NV21ToI420(byte[] yuvFrame, int width, int height, boolean flip, int rotate);

    protected native byte[] NV21ToNV12(byte[] yuvFrame, int width, int height, boolean flip, int rotate);

    protected native int NV21SoftEncode(byte[] yuvFrame, int width, int height, boolean flip, int rotate, long pts);

    protected native boolean openSoftEncoder();

    protected native void closeSoftEncoder();

    static {
        System.loadLibrary("encode");
    }
}
