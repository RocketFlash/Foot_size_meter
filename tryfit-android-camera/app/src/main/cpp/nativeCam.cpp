//#include <exception>
//#include <jni.h>
//#include <string>
//#include <opencv2/core/core.hpp>
//#include <opencv2/imgproc/imgproc.hpp>
//#include <android/log.h>
//#include <opencv/cv.hpp>
//
//#include <jni.h>
//
//#include <android/bitmap.h>
//
////Logging utilities
//#define LOGD(TAG, ...) __android_log_print(ANDROID_LOG_DEBUG  , TAG,__VA_ARGS__)
//#define LOGE(TAG, ...) __android_log_print(ANDROID_LOG_ERROR  , TAG,__VA_ARGS__)
//
//#define SSTR(x) static_cast< std::ostringstream & >( \
//        ( std::ostringstream() << std::dec << x ) ).str()
//
//using namespace std;
//using namespace cv;
//
//template< class T, class TCpp >
//class JavaArrayAccessor
//{
//public:
//    JavaArrayAccessor(JNIEnv* env, T array) :
//            env(env),
//            array(array),
//            data(reinterpret_cast< TCpp* >(env->GetPrimitiveArrayCritical(array, NULL))) // never returns NULL
//    {}
//
//    ~JavaArrayAccessor()
//    {
//        env->ReleasePrimitiveArrayCritical(array, data, 0);
//    }
//
//    TCpp* getData()
//    {
//        return data;
//    }
//
//private:
//    JNIEnv* env;
//    T array;
//    TCpp* data;
//};
//
//class AndroidBitmapAccessorException : public std::exception
//{
//public:
//    AndroidBitmapAccessorException(int code) :
//            code(code)
//    {}
//
//    virtual ~AndroidBitmapAccessorException()
//    {}
//
//    const int code;
//};
//
//class AndroidBitmapAccessor
//{
//public:
//    AndroidBitmapAccessor(JNIEnv* env, jobject bitmap) throw(AndroidBitmapAccessorException):
//            env(env),
//            bitmap(bitmap),
//            data(NULL)
//    {
//        int rv = AndroidBitmap_lockPixels(env, bitmap, reinterpret_cast< void** >(&data));
//        if(rv != ANDROID_BITMAP_RESULT_SUCCESS)
//        {
//            throw AndroidBitmapAccessorException(rv);
//        }
//    }
//
//    ~AndroidBitmapAccessor()
//    {
//        if(data)
//        {
//            AndroidBitmap_unlockPixels(env, bitmap);
//        }
//    }
//
//    unsigned char* getData()
//    {
//        return data;
//    }
//
//private:
//    JNIEnv* env;
//    jobject bitmap;
//    unsigned char* data;
//};
//
//extern "C" {
//void Java_com_tryfit_rawCamera_CameraView_handleFrame(JNIEnv *env,
//                                                                 jobject thiz,
//                                                                 jint width,
//                                                                 jint height,
//                                                                 jbyteArray nv21Data,
//                                                                 jobject bitmap) {
//    try {
//        // create output rgba-formatted output Mat object using the raw Java data
//        // allocated by the Bitmap object to prevent an extra memcpy. note that
//        // the bitmap must be created in ARGB_8888 format
//        AndroidBitmapAccessor bitmapAccessor(env, bitmap);
//        cv::Mat rgba(height, width, CV_8UC4, bitmapAccessor.getData());
//
//        // create input nv21-formatted input Mat object using the raw Java data to
//        // prevent extraneous allocations. note the use of height*1.5 to account
//        // for the nv21 (YUV420) formatting
//        JavaArrayAccessor<jbyteArray, uchar> nv21Accessor(env, nv21Data);
//        cv::Mat nv21(height * 1.5, width, CV_8UC1, nv21Accessor.getData());
//
//        // initialize the rgba output using the nv21 data
//        cv::cvtColor(nv21, rgba, CV_YUV2RGBA_NV21);
//
//        // convert the nv21 image to grayscale by lopping off the extra 0.5*height bits. note
//        // this this ctor is smart enough to not actually copy the data
//        cv::Mat gray(nv21, cv::Rect(0, 0, width, height));
//
//        // do your processing on the nv21 and/or grayscale image here, making sure to update the
//        // rgba mat with the appropriate output
//    }
//    catch (const AndroidBitmapAccessorException &e) {
//        LOGE("error locking bitmap: %d", e.code);
//    }
//}
//}