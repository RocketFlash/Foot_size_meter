//
// Created by Alexey Reznik on 20/02/2017.
//

#include <jni.h>
#include <string>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <android/log.h>

#define TAG "tryfit-lib.cpp"

//Logging utilities
#define LOGD(TAG,...) __android_log_print(ANDROID_LOG_DEBUG  , TAG,__VA_ARGS__)
#define LOGE(TAG,...) __android_log_print(ANDROID_LOG_ERROR  , TAG,__VA_ARGS__)

//Image types
#define TRY_FIT_LIB_TYPE_TOP_PICTURE 0
#define TRY_FIT_LIB_TYPE_LEFT_PICTURE 1
#define TRY_FIT_LIB_TYPE_RIGHT_PICTURE 2

//Return result types
#define TRY_FIT_LIB_RESULT_RESULT_POSITIVE 0
#define TRY_FIT_LIB_RESULT_PAPER_CONTOUR_NOT_FOUND 1
#define TRY_FIT_LIB_RESULT_FOOT_CONTOUR_NOT_FOUND 2

//A4 paper sheet parameters in mm
#define A4_WIDTH 210
#define A4_HEIGHT 290
#define A4_RATIO A4_WIDTH/A4_HEIGHT

//US letter paper sheet parameters in mm
#define LETTER_WIDTH 216
#define LETTER_HEIGHT 280
#define LETTER_RATIO LETTER_WIDTH/LETTER_HEIGHT

//Debug/release version
#define DEBUG 1

using namespace std;
using namespace cv;

double angle(Point pt1, Point pt2, Point pt0);
Size getRectSize(RotatedRect rect);

extern "C"
jobject
Java_com_tryfit_tryfitlib_TryFitLibHelper_processTopPicture(
        JNIEnv *env,
        jobject callingObject,
        jint width,
        jint height,
        jdouble roiSize,
        jobject yuv,
        jintArray bgra) {

    int type = TRY_FIT_LIB_TYPE_TOP_PICTURE;
    int result = TRY_FIT_LIB_RESULT_PAPER_CONTOUR_NOT_FOUND;
    int ballWidthResult = 0;
    int stickLengthResult = 0;

    /*------------------------
    Read original YUV array
    ------------------------*/

    uint8_t *yuvPtr = reinterpret_cast<uint8_t *>(env->GetDirectBufferAddress(yuv));

    if (yuvPtr == nullptr) {
        LOGE(TAG, "yuvPtr NULL pointer ERROR");
        return NULL;
    }
    jint *_bgra = env->GetIntArrayElements(bgra, 0);

    /*------------------------
    Convert YUV image to BGRA
    ------------------------*/

    Mat mYuv(height + height / 2, width, CV_8UC1, yuvPtr);
    Mat mBgra(height, width, CV_8UC4);

    int dstWidth = height;
    int dstHeight = width;
    Mat mResult(dstHeight, dstWidth, CV_8UC4, (unsigned char *) _bgra);
    cvtColor(mYuv, mBgra, CV_YUV420sp2BGR, 4);

    /*------------------------
    Extract Region of Interest with Viewfinder rectangle
    ------------------------*/

    int roiWidth = (int) (width * roiSize);
    int roiHeight = roiWidth * A4_RATIO;

    LOGD(TAG, "mBgra size: %d/%d mRoi size: %d/%d", height, width, roiHeight, roiWidth);

    int x = (width - roiWidth) / 2;
    int y = (height - roiHeight) / 2;
    Rect roiRect = Rect(x, y, roiWidth, roiHeight);
    Mat mRoi = mBgra(roiRect);

    /*------------------------
    Find paper sheet contour
    ------------------------*/

    Mat gray(roiWidth, roiHeight, CV_8UC4);
    medianBlur(mRoi, gray, 9);
    cvtColor(gray, gray, COLOR_RGBA2GRAY);

    double paperContourLength = 0.;
    vector<Point> paperContour;
    vector<Point> paperContourHull;
    RotatedRect paperContourRect;

    int bestThreshold = 0;
    double minCosine = 1.;

    for (int i = 200; i > 100; i = i - 3) {

        Mat binary(roiWidth, roiHeight, CV_8UC4);
        threshold(gray, binary, i, 255, 0);

        vector<vector<Point>> contours;
        vector<Vec4i> hierarchy;
        findContours(binary, contours, hierarchy, CV_RETR_TREE, CV_CHAIN_APPROX_SIMPLE,
                     Point(0, 0));

        LOGD(TAG, "Paper contour. Threshold %d Contours %d", i, contours.size());

        for (int j = 0; j < contours.size(); j++) {

            vector<Point> contour = contours[j];
            double length = arcLength(Mat(contour), true);

            if (length > roiWidth && length < (roiWidth + roiHeight) * 1.95) {

                vector<Point> hull;
                convexHull(Mat(contour), hull, false);
                vector<Point> approxHull;
                approxPolyDP(Mat(hull), approxHull, arcLength(Mat(hull), true) * 0.01, true);

                LOGD(TAG, "approx: %d", approxHull.size());

                if (approxHull.size() == 4) {

                    double maxCosine = 0;
                    for (int k = 2; k < 5; k++) {
                        double cosine = abs(angle(approxHull[k % 4], approxHull[k - 2],
                                                  approxHull[k - 1]));
                        maxCosine = max(maxCosine, cosine);
                    }

                    LOGD(TAG, "maxCosine: %.4f", maxCosine);

                    if (maxCosine != 0 && maxCosine < 0.2 && maxCosine < minCosine) {

                        RotatedRect boundRect = minAreaRect(approxHull);
                        double aspectRatio;

                        if (boundRect.angle < -45.) { aspectRatio = boundRect.size.width /
                                                                    boundRect.size.height;
                        }
                        else { aspectRatio = boundRect.size.height / boundRect.size.width; }

                        double a4Ratio = 210. / 290.;
                        double ratioDiff = abs(1. - a4Ratio / aspectRatio);

                        LOGD(TAG, "ratioDiff: %.2f", ratioDiff);

                        if (ratioDiff < 0.1) {

                            LOGD(TAG, "length %.2f maxCosine %.4f ratioDiff %.2f", length,
                                 maxCosine, ratioDiff);

                            paperContour = contour;
                            paperContourHull = approxHull;
                            paperContourRect = boundRect;
                            paperContourLength = length;
                            minCosine = maxCosine;
                            bestThreshold = i;

                        } //end if (ratioDiff < 0.1)
                    } // end if (maxCosine < 0.2 && maxCosine < minCosine)
                } //end if (approxHull.size() == 4)
            } //end if (length > roiWidth)
        } //end for (int j = 0; j < contours.size(); j++)
    } // end for (int i = 200; i > 100; i = i - 5)


    /*------------------------
    Find foot contour
    ------------------------*/

    if (paperContourLength != 0.) {

        LOGD(TAG, "Paper contour found. length %.2f minCosine %.4f threshold %d",
             paperContourLength, minCosine, bestThreshold);

        double paperWidthPx = 0;
        double paperHeightPx = 0.;
        if (paperContourRect.size.width < paperContourRect.size.height) {

            paperWidthPx = paperContourRect.size.width;
            paperHeightPx = paperContourRect.size.height;
        } else {

            paperWidthPx = paperContourRect.size.height;
            paperHeightPx = paperContourRect.size.width;
        }

        Mat mask = Mat(mRoi.size(), mRoi.type(), Scalar(0, 0, 0, 255));
        fillConvexPoly(mask, paperContourHull, Scalar(255, 255, 255, 255));
        Mat paperMat = Mat(mRoi.size(), mRoi.type(), Scalar(255, 255, 255, 255));
        mRoi.copyTo(paperMat, mask);

        Mat gray(paperMat.size(), paperMat.type());
        medianBlur(paperMat, gray, 9);
        cvtColor(gray, gray, COLOR_RGBA2GRAY);

        vector<vector<Point>> bounds;
        bounds.push_back(paperContourHull);
        drawContours(gray, bounds, 0, Scalar(255, 255, 255, 255), 3);
        bitwise_not(gray, gray);


        Mat binary(paperMat.size(), paperMat.type());
        threshold(gray, binary, 0, 255, THRESH_BINARY | THRESH_OTSU);

        vector<Point> footContour;
        RotatedRect footRect;
        double footContourLength = 0.;

        vector<vector<Point>> contours2;
        vector<Vec4i> hierarchy2;
        findContours(binary, contours2, hierarchy2, CV_RETR_TREE, CV_CHAIN_APPROX_SIMPLE,
                     Point(0, 0));

        LOGD(TAG, "Foot contours: %d", contours2.size());

        for (int i = 0; i < contours2.size(); i++) {

            vector<Point> contour = contours2[i];
            double length = arcLength(Mat(contour), true);
            vector<Point> approx;
            approxPolyDP(contour, approx, length * 0.001, true);

            LOGD(TAG, "approx: %d", approx.size());

            if (approx.size() > 4 && length > paperWidthPx && length > footContourLength) {

                RotatedRect rect = minAreaRect(contour);
                footContour = contour;
                footRect = rect;
                footContourLength = length;
            }
        }

        if (footContourLength != 0.) {

            LOGD(TAG, "Foot contour found. length: %.2f", footContourLength);

            if (DEBUG) {
                vector<vector<Point>> draw;
                draw.push_back(paperContourHull);
                drawContours(mRoi, draw, 0, Scalar(255, 0, 0, 255), 3);
                draw.push_back(footContour);
                drawContours(mRoi, draw, 1, Scalar(0, 0, 255, 255), 3);
            }

            double footTopmostY = mRoi.size().width;
            Point footTopmost;
            for (int i = 0; i < footContour.size(); i++) {

                if (footContour[i].x < footTopmostY) {

                    footTopmostY = footContour[i].x;
                    footTopmost = footContour[i];
                }
            }

            double paperBottommostY = 0.;
            Point paperBottommost;

            for (int i = 0; i < paperContour.size(); i++) {

                if (paperContour[i].x > paperBottommostY) {

                    paperBottommostY = paperContour[i].x;
                    paperBottommost = paperContour[i];
                }
            }

            double yDiff = paperBottommostY - footTopmostY;
            stickLengthResult = (int) (yDiff * 290 / paperHeightPx);

            vector<Point> upperHalfFootContour;
            for (int i = 0; i < footContour.size(); i++) {

                if (footContour[i].x > paperBottommostY - yDiff / 2) {

                    upperHalfFootContour.push_back(footContour[i]);
                }
            }

            RotatedRect upperHalfRect = minAreaRect(upperHalfFootContour);
            double ballWidthPx = 0.;

            if (upperHalfRect.angle < -45.) { ballWidthPx = upperHalfRect.size.width; }
            else { ballWidthPx = upperHalfRect.size.height; }

            ballWidthResult = (int) (ballWidthPx * 210 / paperWidthPx);


            result = TRY_FIT_LIB_RESULT_RESULT_POSITIVE;

        } else {

            result = TRY_FIT_LIB_RESULT_FOOT_CONTOUR_NOT_FOUND;
        }
    }

    /*------------------------
    Transpose and flip resulting image
    ------------------------*/

    transpose(mBgra, mResult);
    flip(mResult, mResult, 1);

    /*------------------------
    Call Java setter methods to return result
    ------------------------*/

    jclass tryFitLibReturnClass = env->FindClass("com/tryfit/tryfitlib/TryFitLibResult");
    jmethodID constructor = env->GetMethodID(tryFitLibReturnClass, "<init>", "(II)V");
    jobject tryFitLibReturnObject = env->NewObject(tryFitLibReturnClass, constructor, type, result);

    if (result == TRY_FIT_LIB_RESULT_RESULT_POSITIVE) {

        jmethodID setter1 = env->GetMethodID(tryFitLibReturnClass, "setBallWidth", "(I)V");
        env->CallVoidMethod(tryFitLibReturnObject, setter1, ballWidthResult);

        jmethodID setter2 = env->GetMethodID(tryFitLibReturnClass, "setStickLength", "(I)V");
        env->CallVoidMethod(tryFitLibReturnObject, setter2, stickLengthResult);
    }

    return tryFitLibReturnObject;
}

double angle(Point pt1, Point pt2, Point pt0) {
    double dx1 = pt1.x - pt0.x;
    double dy1 = pt1.y - pt0.y;
    double dx2 = pt2.x - pt0.x;
    double dy2 = pt2.y - pt0.y;
    return (dx1 * dx2 + dy1 * dy2) /
           sqrt((dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2) + 1e-10);
}

Size getRectSize(RotatedRect rect) {

    if (rect.size.width < rect.size.height) {
        return Size(rect.size.height, rect.size.width);
    } else {
        return Size(rect.size.width, rect.size.height);
    }
}
