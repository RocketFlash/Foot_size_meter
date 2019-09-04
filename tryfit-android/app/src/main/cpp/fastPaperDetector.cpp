
//
// Created by Rauf Yagfarov on 31/08/2017.
//
#include "tryfit_lib.h"

#define TAG "fastPaperDetector.cpp"

//Logging utilities
#define LOGD(TAG, ...) __android_log_print(ANDROID_LOG_DEBUG  , TAG,__VA_ARGS__)
#define LOGE(TAG, ...) __android_log_print(ANDROID_LOG_ERROR  , TAG,__VA_ARGS__)

void processSimplePicture(Mat &inMat);

void find_paper_simple(Mat &srcImage);

int
checkContourAboveSimple(vector<Point> &checkContour, Size imageSize, double &bestRatio);

vector<Point> pointsSimple;
double FPD_xFactor = 0;
double FPD_yFactor = 0;

void processSimplePicture(Mat &inMat) {

    pointsSimple.clear();
    Size sizeRgba = inMat.size();

    int width = sizeRgba.width;
    int height = sizeRgba.height;

    LOGD(TAG, "inMat size: %d/%d ", height, width);

    find_paper_simple(inMat);

}

/**
 * function, which finds paper sheet on an image using photo from above
 */
void find_paper_simple(Mat &srcImage) {

    Mat srcMini;
    Size sizeRgbaBefore = srcImage.size();
    double resolutionCurrent = (double(sizeRgbaBefore.width) / double(sizeRgbaBefore.height));
    Size sizeRgbaAfter(int(FPD_newHeight * resolutionCurrent), FPD_newHeight);
    resize(srcImage, srcMini, sizeRgbaAfter);

    FPD_xFactor = double(sizeRgbaBefore.width) / double(sizeRgbaAfter.width);
    FPD_yFactor = double(sizeRgbaBefore.height) / double(sizeRgbaAfter.height);

    int maxId = -1;
    double bestRatio = 10000;

    vector<int> statusCodes;
    Mat blurred, rgbaClone, binary;
    Mat gray0;
    vector<vector<Point> > contours;
    vector<Vec4i> hierarchy;
    vector<Point> approxHullBest;

    bool found = false;


    for (int method = 0; method < FPD_numberOfMethods; method++) {
        for (int blureKernelSize = FPD_blureLow;
             blureKernelSize <= FPD_blureHigh; blureKernelSize += FPD_blureStep) {
            rgbaClone = srcMini.clone();
            medianBlur(rgbaClone, blurred, blureKernelSize);
            gray0 = Mat(blurred.size(), CV_8U);

            for (int c = 0; c < 4; c++) {
                if (c == 3) {
                    cvtColor(blurred, gray0, CV_BGR2GRAY);
                } else {
                    int ch[] = {c, 0};
                    mixChannels(&blurred, 1, &gray0, 1, ch, 1);
                }
                for (int trh = FPD_thresholdLow;
                     trh <= FPD_thresholdHigh; trh += FPD_thresholdStep) {
                    bool stop = false;
                    int aperture = FPD_apertureLow;
                    int threshAdaptive = FPD_threshAdaptiveLow;
                    int blockSize = FPD_blockSizeLow;
                    int simpleThreshold = FPD_thrLow;
                    while (!stop) {
                        if (method == 0) {
                            Canny(gray0, binary, trh, trh * FPD_thresholdRatio, aperture, false);
                            aperture += 2;
                            if (aperture > FPD_apertureHigh) stop = true;
                        } else if (method == 1) {
                            adaptiveThreshold(gray0, binary, 255, ADAPTIVE_THRESH_GAUSSIAN_C,
                                              THRESH_BINARY,
                                              blockSize, threshAdaptive);
                            threshAdaptive += FPD_threshAdaptiveStep;
                            if (threshAdaptive > FPD_threshAdaptiveHigh) {
                                blockSize += FPD_blockSizeStep;
                                threshAdaptive = FPD_threshAdaptiveLow;
                            }
                            if (blockSize > FPD_blockSizeHigh) {
                                stop = true;
                            }
                        } else if (method == 2) {
                            threshold(gray0, binary, simpleThreshold, 255, THRESH_BINARY);
                            simpleThreshold += FPD_thrStep;
                            if (simpleThreshold > FPD_thrHigh) {
                                stop = true;
                            }
                        }

                        // Find contours and store them in a list
                        findContours(binary, contours, hierarchy, CV_RETR_TREE,
                                     CV_CHAIN_APPROX_SIMPLE, Point(0, 0));

                        // Test contours
                        vector<Point> approx;
                        for (int i = 0; i < contours.size(); i++) {

                            if (fabs(arcLength(Mat(contours[i]), true)) <
                                2 * (sizeRgbaAfter.width + sizeRgbaAfter.height) * 0.2) {
                                continue;
                            }

                            vector<Point> hull;
                            vector<Point> approxHull;

                            convexHull(Mat(contours[i]), hull, true);

                            for (int eps = FPD_epsLow; eps <= FPD_epsHigh; eps += FPD_epsStep) {

                                approxPolyDP(Mat(hull), approxHull,
                                             arcLength(Mat(hull), true) * 0.0000001 * eps, true);
                                vector<Point> orderedApproxHull = orderAllPoints(approxHull);
                                int evaluation = checkContourAboveSimple(orderedApproxHull,
                                                                         sizeRgbaAfter,
                                                                         bestRatio);

                                if (method == 0) {
                                    statusCodes.push_back(evaluation);
                                }

                                if (evaluation == PAPER_DETECTED) {
                                    maxId = i;
                                    approxHullBest = orderedApproxHull;
                                    break;
                                }
                            }
                            if (maxId > 0) {
                                break;
                            }
                        }
                        if (maxId > 0) {
                            break;
                        }
                    }
                    if (maxId > 0) {
                        break;
                    }
                }
                if (maxId > 0) {
                    break;
                }
            }
            if (maxId > 0) {
                break;
            }
        }
        // If we have appropriate contour, we bound it with rectangle
        if (maxId >= 0) {

            vector<Point> fpts = sixPointsToFour(approxHullBest);
            vector<Point> paperC;
            paperC.push_back(fpts[0]);
            paperC.push_back(approxHullBest[2]);
            paperC.push_back(approxHullBest[3]);
            paperC.push_back(fpts[1]);

            pointsSimple = paperC;

            found = true;
            break;
        }
    }
    if (!found) {
        int minStatusCode = 100;
        for (int i = 0; i < statusCodes.size(); i++) {
            if (statusCodes[i] < minStatusCode) {
                minStatusCode = statusCodes[i];
            }
        }

        string stringValues;

        if (minStatusCode == 1) {
            stringValues = "ONE_CORNER_IS_HIDDEN";
        } else if (minStatusCode == 2) {
            stringValues = "SHEETS_LIE_NOT_STRAIGHT";
        } else if (minStatusCode == 3) {
            stringValues = "PAPER_CONTOUR_NOT_DETECTED_CORRECTLY";
        } else if (minStatusCode == 4) {
            stringValues = "UPPER_PAPER_NOT_FOUND";
        } else if (minStatusCode == 5) {
            stringValues = "TOO_FAR_FROM_PAPER";
        } else if (minStatusCode == 6) {
            stringValues = "PAPER_NOT_DETECTED";
        }

        putText(srcMini, stringValues, Point(50, 50), FONT_HERSHEY_PLAIN, 4, Scalar(0, 255, 255),
                5);
    }

    resize(srcMini, srcMini, sizeRgbaBefore);
    srcMini.copyTo(srcImage);
    srcMini.release();
    binary.release();
    rgbaClone.release();
//    srcImage.release();

}

int
checkContourAboveSimple(vector<Point> &checkContour, Size imageSize, double &bestRatio) {

    int width = imageSize.width;
    int height = imageSize.height;

    double minPerimeter = 2 * (width + height) * FPD_minPerimeterCoef;
    double maxPerimeter = 2 * (width + height) * FPD_maxPerimeterCoef;


    if (fabs(arcLength(Mat(checkContour), true)) > minPerimeter &&
        fabs(arcLength(Mat(checkContour), true)) < maxPerimeter) {


        if (checkContour.size() == 6 && isContourConvex(Mat(checkContour))) {
            double cosine, maxCosine = 0;

            vector<Point> pointsToCheck;

            pointsToCheck.push_back(checkContour[3]);
            pointsToCheck.push_back(checkContour[2]);
            pointsToCheck.push_back(checkContour[1]);
            pointsToCheck.push_back(checkContour[4]);


            for (int j = 2; j < 5; j++) {
                cosine = fabs(
                        angle(pointsToCheck[j % 4], pointsToCheck[j - 2],
                              pointsToCheck[j - 1]));

                maxCosine = max(maxCosine, cosine);
            }

            if (maxCosine < FPD_maxPossibleCosine) {
                vector<double> similarityCriterions1 = similarityToA4(pointsToCheck);
                double criterion1, criterion2, criterion3;

                criterion1 = similarityCriterions1[0];
                criterion2 = similarityCriterions1[1];
                criterion3 = similarityCriterions1[2];

                if (criterion1 > FPD_minCriterion1 &&
                    criterion2 > FPD_minCriterion2 &&
                    abs(criterion3 - (double(paperHeight) / double(paperWidth))) < bestRatio) {

                    vector<Point> fpts = sixPointsToFour(checkContour);
                    vector<Point> paperC;
                    paperC.push_back(fpts[0]);
                    paperC.push_back(checkContour[2]);
                    paperC.push_back(checkContour[3]);
                    paperC.push_back(fpts[1]);
                    double criterion4 =
                            min(norm(paperC[0] - paperC[1]), norm(paperC[2] - paperC[3])) /
                            max(norm(paperC[0] - paperC[1]), norm(paperC[2] - paperC[3]));
                    double criterion5 =
                            min(norm(paperC[0] - paperC[3]), norm(paperC[1] - paperC[2])) /
                            max(norm(paperC[0] - paperC[3]), norm(paperC[1] - paperC[2]));

                    if (criterion4 > FPD_minCriterion4 && criterion5 > FPD_minCriterion5) {
                        double angle1 =
                                acos(angle(checkContour[2], checkContour[5], fpts[0])) * 180 /
                                CV_PI;
                        double angle2 =
                                acos(angle(checkContour[3], checkContour[0], fpts[1])) * 180 /
                                CV_PI;
                        double criterion6 = norm(checkContour[0] - checkContour[5]) /
                                            norm(checkContour[2] - checkContour[3]);
                        double criterion7 = min(angle1, angle2) / max(angle1, angle2);

                        if (criterion6 > FPD_minCriterion6 && criterion7 > FPD_minCriterion7) {

                            bestRatio = abs(
                                    criterion3 - (double(paperHeight) / double(paperWidth)));
                            return PAPER_DETECTED;

                        } else {
                            return ONE_CORNER_IS_HIDDEN;
                        }
                    } else {
                        return PAPER_CONTOUR_NOT_DETECTED_CORRECTLY;
                    }
                }
            }
        }
    } else {
        return TOO_FAR_FROM_PAPER;
    }
    return PAPER_NOT_DETECTED;
}


extern "C" {
JNIEXPORT jintArray JNICALL
Java_com_tryfit_camera_PaperDetectionActivity_processSimplePictureAndroid(JNIEnv *env,
                                                                                jclass type,
                                                                                jlong inputMatAddr) {

    Mat &inMat = *(Mat *) inputMatAddr;

    jintArray resultOfProcessing;
    jint size = (jint) (2 * pointsSimple.size());

    resultOfProcessing = (env)->NewIntArray(size);
    if (resultOfProcessing == NULL) {
        return NULL; /* out of memory error thrown */
    }

    if (inMat.size().width > inMat.size().height) {
        //Rotate image 90 degrees clockwise and process
        rotate(inMat, inMat, ROTATE_90_CLOCKWISE);
        processSimplePicture(inMat);
        rotate(inMat, inMat, ROTATE_90_COUNTERCLOCKWISE);
    } else {
        processSimplePicture(inMat);
    }

    // fill a temp structure to use to populate the java int array
    jint fill[256];
    for (int i = 0; i < size; i += 2) {
        fill[i] = (jint) (double(pointsSimple[i / 2].y) * FPD_xFactor);
        fill[i + 1] = (jint) (inMat.rows - double(pointsSimple[i / 2].x) * FPD_yFactor);
    }

    // move from the temp structure to the java structure
    if (size > 0) {
        (env)->SetIntArrayRegion(resultOfProcessing, 0, size, fill);
    }

    return resultOfProcessing;
}

JNIEXPORT jintArray JNICALL
Java_com_tryfitCamera_camera_fragments_CameraFragment_processSimplePictureAndroid(JNIEnv *env,
                                                                                  jclass type,
                                                                                  jlong inputMatAddr) {

    Mat &inMat = *(Mat *) inputMatAddr;

    jintArray resultOfProcessing;
    jint size = (jint) (2 * pointsSimple.size());

    resultOfProcessing = (env)->NewIntArray(size);
    if (resultOfProcessing == NULL) {
        return NULL; /* out of memory error thrown */
    }

    if (inMat.size().width > inMat.size().height) {
        rotate(inMat, inMat, ROTATE_90_CLOCKWISE);
        processSimplePicture(inMat);
//        rotate(inMat, inMat, ROTATE_90_COUNTERCLOCKWISE);
    } else {
        processSimplePicture(inMat);
    }

    // fill a temp structure to use to populate the java int array
    jint fill[256];
    for (int i = 0; i < size; i += 2) {
        fill[i] = (jint) (double(pointsSimple[i / 2].y) * FPD_xFactor);
        fill[i + 1] = (jint) (inMat.rows - double(pointsSimple[i / 2].x) * FPD_yFactor);
    }

    // move from the temp structure to the java structure
    if (size > 0) {
        (env)->SetIntArrayRegion(resultOfProcessing, 0, size, fill);
    }

    return resultOfProcessing;
}

JNIEXPORT jintArray JNICALL
Java_com_tryfit_camera_CameraFragment_processSimplePictureAndroid2(
        JNIEnv *env, jobject thiz,
        jint width, jint height,
        jbyteArray NV21FrameData, jintArray outPixels) {


    jbyte *_yuv = env->GetByteArrayElements(NV21FrameData, 0);
    jint *_bgra = env->GetIntArrayElements(outPixels, 0);
// Prepare a cv::Mat that points to the YUV420sp data.
    Mat myuv(height + height / 2, width, CV_8UC1, (uchar *) _yuv);
// Prepare a cv::Mat that points to the BGRA output data.
    Mat mbgra(height, width, CV_8UC4, (uchar *) _bgra);
// Convert the color format from the camera's
// NV21 "YUV420sp" format to an Android BGRA color image.
    cvtColor(myuv, mbgra, CV_YUV420sp2BGRA);
    Mat inMat = mbgra;

    jintArray resultOfProcessing;
    jint size = (jint) (2 * pointsSimple.size());

    resultOfProcessing = (env)->NewIntArray(size);
    if (resultOfProcessing == NULL) {
        return NULL; /* out of memory error thrown */
    }

    if (inMat.size().width > inMat.size().height) {
//Rotate image 90 degrees clockwise and process
        rotate(inMat, inMat, ROTATE_90_CLOCKWISE);
        processSimplePicture(inMat);
//        rotate(inMat, inMat, ROTATE_90_COUNTERCLOCKWISE);
    } else {
        processSimplePicture(inMat);
    }

// fill a temp structure to use to populate the java int array
    jint fill[256];
    for (int i = 0; i < size; i += 2) {
        fill[i] = (jint) (double(pointsSimple[i / 2].x) * FPD_yFactor);
        fill[i + 1] = (jint) (double(pointsSimple[i / 2].y) * FPD_xFactor);
    }

// move from the temp structure to the java structure
    if (size > 0) {
        (env)->SetIntArrayRegion(resultOfProcessing, 0, size, fill);
    }

    env->ReleaseIntArrayElements(outPixels, _bgra, JNI_ABORT);
    env->ReleaseByteArrayElements(NV21FrameData, _yuv, JNI_ABORT);

    return resultOfProcessing;
}

}