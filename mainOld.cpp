#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/imgcodecs.hpp>
#include <iostream>
#include <fstream>
#include <math.h>
#include <opencv/cv.hpp>
#include "geomTools.h"
#include "parameters.h"
#include "guidedfilter.h"

// A4 paper sheet parameters in mm
#define A4_WIDTH 210
#define A4_HEIGHT 297

// Detection status
#define PAPER_DETECTED  0
#define ONE_CORNER_IS_HIDDEN  1
#define SHEETS_LIE_NOT_STRAIGHT 2
#define PAPER_CONTOUR_NOT_DETECTED_CORRECTLY 3
#define UPPER_PAPER_NOT_FOUND 4
#define TOO_FAR_FROM_PAPER  5
#define PAPER_NOT_DETECTED  6


//#define paperWidth 216
//#define paperHeight 280
//#define paperWidth 297
//#define paperHeight 420
#define A4_RATIO paperHeight/paperWidth

using namespace cv;
using namespace std;

string imageName;
Mat bestBinary;

int paperHeight = A4_HEIGHT;
int paperWidth = A4_WIDTH;

foot find_paper(Mat &srcImage);

foot find_foot(Mat &paperImage);

foot fourPointsTransformSix(Mat image, vector<Point> allPoints);

int checkContourAbove(vector<Point> &checkContour, Size imageSize, double &bestRatio, bool &rotated);

Mat equalizeIntensity(const Mat& inputImage);

int main() {

    report();
    namedWindow("Find paper");
    namedWindow("Cropped");


    vector<vector<Point3d>> model3dPoints;

    Mat srcAbove;
    vector<String> results;
    vector<String> pathsToImages;
    string imagesDirectory = "TwoA4Papers/T-shape/OlegAbove/";
    string csvFilename = "OlegAbove";

    glob(projectPath + "dataset/" + imagesDirectory, results);

    ofstream outputFile;

    // create and open the .csv file
    outputFile.open(projectPath + csvFilename + ".csv");
    // write the file headers
    outputFile << "Filename" << "," << "HeightA" << "," << "WidthA" << endl;

    String s;
    for (int j = 0; j < results.size(); j++) {
        if (has_suffix(results[j], ".jpg")) {
            pathsToImages.push_back(results[j]);
        }
    }

    for (int pathNo = 0; pathNo < pathsToImages.size(); pathNo++) {
        s = pathsToImages[pathNo];
        string toPrint = s.substr(s.find_last_of("/") + 1);
        imageName = toPrint.substr(0, toPrint.size() - 4);

        cout << "Image: " << imageName << endl;

        srcAbove = imread(s, IMREAD_COLOR);
        if(srcAbove.cols>srcAbove.rows){
            rotate(srcAbove,srcAbove,ROTATE_90_CLOCKWISE);
        }
        foot aboveFoot = find_paper(srcAbove);
//        imshow("Find paper", srcAbove);
//        waitKey(1);

        if (aboveFoot.heightAndWidthValues[0] > 0) {
            cout << "Height: " << aboveFoot.heightAndWidthValues[0] << endl;
            cout << "Width:  " << aboveFoot.heightAndWidthValues[1] << endl;
        } else {
            cout << "Foot not detected correctly" << endl;
        }
        cout << "----------------------" << endl;

        outputFile << imageName << "," << int(aboveFoot.heightAndWidthValues[0]) << ","
                   << int(aboveFoot.heightAndWidthValues[1]) << endl;
    }

    outputFile.close();
    return 0;
}


/**
 * function, which finds paper sheet on an image using photo from above
 */
foot find_paper(Mat &srcImage) {

//    cv::Mat p = srcImage;
//
//    int r = 22; // try r=2, 4, or 8
//    double eps = 0.2 * 0.2; // try eps=0.1^2, 0.2^2, 0.4^2
//
//    eps *= 255 * 255;   // Because the intensity range of our images is [0, 255]
//
//    srcImage = guidedFilter(srcImage, p, r, eps);

//    colorReduce(srcImage,64);

//    srcImage = equalizeIntensity(srcImage);

    Mat srcMini;
    Size sizeRgbaBefore = srcImage.size();
//    Size sizeRgbaAfter = srcImage.size();
    Size sizeRgbaAfter(int(2000 * (double(sizeRgbaBefore.width) / double(sizeRgbaBefore.height))), 2000);
    resize(srcImage, srcMini, sizeRgbaAfter);

    int maxId = -1;
    double bestRatio = 10000;

    vector<int> statusCodes;
    Mat blurred, rgbaClone, binary, tempBinary;
    Mat gray0;
    vector<vector<Point> > contours;
    vector<Vec4i> hierarchy;
    vector<Point> approxHullBest;
    foot currFoot;
    vector<double> values;
    values.push_back(-1);
    values.push_back(-1);
    currFoot.heightAndWidthValues = values;
    bool found = false;
    bool rotated = false;

    for (int method = 0; method < numberOfMethods; method++) {
        for (int blureKernelSize = blureLow; blureKernelSize <= blureHigh; blureKernelSize += blureStep) {
            rgbaClone = srcMini.clone();
            // blur will enhance edge detection
            medianBlur(rgbaClone, blurred, blureKernelSize);
            gray0 = Mat(blurred.size(), CV_8U);

            // find squares in every color plane of the image
            for (int c = 0; c < 4; c++) {
                if (c == 3) {
                    cvtColor(blurred, gray0, CV_BGR2GRAY);
                } else {
                    int ch[] = {c, 0};
                    mixChannels(&blurred, 1, &gray0, 1, ch, 1);
                }
                for (int trh = thresholdLow; trh <= thresholdHigh; trh += thresholdStep) {
                    bool stop = false;
                    int aperture = apertureLow;
                    int threshAdaptive = threshAdaptiveLow;
                    int blockSize = blockSizeLow;
                    int simpleThreshold = thrLow;
                    while (!stop) {
                        if (method == 0) {
                            Canny(gray0, binary, trh, trh * thresholdRatio, aperture, false);
                            aperture += 2;
                            if (aperture > apertureHigh) stop = true;
                        } else if (method == 1) {
                            adaptiveThreshold(gray0, binary, 255, ADAPTIVE_THRESH_GAUSSIAN_C, THRESH_BINARY,
                                              blockSize, threshAdaptive);
                            threshAdaptive += threshAdaptiveStep;
                            if (threshAdaptive > threshAdaptiveHigh) {
                                blockSize += blockSizeStep;
                                threshAdaptive = threshAdaptiveLow;
                            }
                            if (blockSize > blockSizeHigh) {
                                stop = true;
                            }
                        } else if (method == 2) {
                            threshold(gray0, binary, simpleThreshold, 255, THRESH_BINARY);
                            simpleThreshold += thrStep;
                            if (simpleThreshold > thrHigh) {
                                stop = true;
                            }
                        }

                        // Find contours and store them in a list
                        findContours(binary, contours, hierarchy, CV_RETR_TREE, CV_CHAIN_APPROX_SIMPLE, Point(0, 0));

//                        // sort contours
//                        sort(contours.begin(), contours.end(), compareContourLenghts);
//                        vector<vector<Point>> newContours;
//                        int N = min(numberOfContoursToCheck,int(contours.size()));
//                        for(int cTch = 0; cTch < N;cTch++) {
//                            newContours.push_back(contours[contours.size() - 1 - cTch]);
//                        }

                        // Test contours
                        vector<Point> approx;
                        for (int i = 0; i < contours.size(); i++) {

                            if (fabs(arcLength(Mat(contours[i]), true)) < 2 * (sizeRgbaAfter.width + sizeRgbaAfter.height) * 0.2){
                                continue;
                            }

                            vector<Point> hull;
                            vector<Point> approxHull;

                            convexHull(Mat(contours[i]), hull, true);

                            for (int eps = epsLow; eps <= epsHigh; eps += epsStep) {

                                approxPolyDP(Mat(hull), approxHull, arcLength(Mat(hull), true) * 0.0000001 * eps, true);
                                vector<Point> orderedApproxHull = orderAllPoints(approxHull);
                                int evaluation = checkContourAbove(orderedApproxHull, sizeRgbaAfter, bestRatio, rotated);


                                if(method == 0){
                                    statusCodes.push_back(evaluation);
                                }

                                if (evaluation == PAPER_DETECTED) {
                                    maxId = i;
                                    approxHullBest = orderedApproxHull;
                                    bestBlurredKernelSize = blureKernelSize;
                                    bestBinary = binary;
                                    bestChannel = c;
                                    bestThresh = trh;
                                    bestEps = eps;
                                    bestAperture = aperture - 2;
                                    bestMethod = method;
                                    bestThresholdAdaptive = threshAdaptive - threshAdaptiveStep;
                                    bestBlockSize = blockSize - blockSizeStep;
                                    bestThresholdSimple = simpleThreshold;
                                }
                            }
                        }
                    }
                }
            }
            if (maxId > 0) {
                break;
            }
        }
        // If we have appropriate contour, we bound it with rectangle
        if (maxId >= 0) {

            if (rotated) {
                rotate(srcMini, srcMini, ROTATE_180);
                rotate(bestBinary, bestBinary, ROTATE_180);
                cout << "Rotate!" << endl;
            }

            vector<Point> fpts = sixPointsToFour(approxHullBest);
            vector<Point> paperC;
            paperC.push_back(fpts[0]);
            paperC.push_back(approxHullBest[2]);
            paperC.push_back(approxHullBest[3]);
            paperC.push_back(fpts[1]);

            cout << "Paper was found" << endl;
            cout << "Best blure kernel size: " << bestBlurredKernelSize << endl;
            cout << "Best color channel: " << colors[bestChannel] << endl;

            cout << "Best method : " << methods[bestMethod] << endl;
            if (bestMethod == 0) {
                cout << "Best aperture value: " << bestAperture << endl;
                cout << "Best threshold value: " << bestThresh << endl;
            } else if (bestMethod == 2) {
                cout << "Best block size: " << bestBlockSize << endl;
                cout << "Best threshold value: " << bestThresholdAdaptive << endl;
            } else if (bestMethod == 1) {
                cout << "Best threshold value: " << bestThresholdSimple << endl;
            }
            cout << "Best eps value: " << bestEps << endl;
            currFoot = fourPointsTransformSix(srcMini, approxHullBest);
            currFoot.processingImageSize = sizeRgbaAfter;
            drawContours(srcMini, vector<vector<Point> >(1, approxHullBest), -1, Scalar(255, 0, 0), 2, 8);
            drawContours(srcMini, vector<vector<Point> >(1, paperC), -1, Scalar(255, 255, 0), 2, 8);
            string stringValues = "Height: " + to_string(int(currFoot.heightAndWidthValues[0])) + "  Width: " +
                                  to_string(int(currFoot.heightAndWidthValues[1]));
            putText(srcMini, stringValues, Point(50, 50), FONT_HERSHEY_PLAIN, 4, Scalar(0, 255, 255), 5);
            imwrite(projectPath + "contours/" + imageName + ".jpg", srcMini);
            imwrite(projectPath + "binary/" + imageName + ".jpg", bestBinary);
        } else {
            cout << "Paper not detected by " << methods[method] << endl;
        }
        if (currFoot.heightAndWidthValues[0] > 0) {
            found = true;
            break;
        }
    }
    if(!found){
        int minStatusCode = 100;
        for(int i=0;i<statusCodes.size();i++){
            if(statusCodes[i]<minStatusCode){
                minStatusCode = statusCodes[i];
            }
        }

        string stringValues;

        if(minStatusCode == 1){
            stringValues = "ONE_CORNER_IS_HIDDEN";
        } else if(minStatusCode == 2){
            stringValues = "SHEETS_LIE_NOT_STRAIGHT";
        }else if(minStatusCode == 3){
            stringValues = "PAPER_CONTOUR_NOT_DETECTED_CORRECTLY";
        }else if(minStatusCode == 4){
            stringValues = "UPPER_PAPER_NOT_FOUND";
        }else if(minStatusCode == 5){
            stringValues = "TOO_FAR_FROM_PAPER";
        }else if(minStatusCode == 6){
            stringValues = "PAPER_NOT_DETECTED";
        }

        cout<<"Status code: " << minStatusCode << endl;
        putText(srcMini, stringValues, Point(50, 50), FONT_HERSHEY_PLAIN, 4, Scalar(0, 255, 255), 5);
        imwrite(projectPath + "contours/" + imageName + ".jpg", srcMini);
    }

    srcMini.copyTo(srcImage);
    srcMini.release();

//    srcImage.release();

    return currFoot;
}


/**
 * Function to find foot contour and calculate height and width
 * @param image
 * @return
 */
foot find_foot(Mat &image) {

    foot resultFoot;
    Mat blurred(image);
    medianBlur(image, blurred, 11);
    Mat gray0(blurred.size(), CV_8U);
    cvtColor(blurred, gray0, CV_BGR2GRAY);
    Mat binary;

    Canny(gray0, binary, 0, 255);
    morphOps(binary, 2, 2);

    vector<vector<Point> > contours;
    vector<Vec4i> hierarchy;
    findContours(binary, contours, hierarchy, CV_RETR_TREE, CV_CHAIN_APPROX_SIMPLE, Point(0, 0));

    double maxPerimeter = 0;
    int maxId = 0;

    for (int i = 0; i < contours.size(); i++) {
        if (fabs(arcLength(Mat(contours[i]), true)) > maxPerimeter) {
            maxPerimeter = fabs(arcLength(Mat(contours[i]), true));
            maxId = i;
        }
    }

    cout << "Number of contours: " << contours.size() << endl;
    vector<Point> bestContour = contours[maxId];


    double minYval = 10000;
    double minYXval = 10000;

    for (int k = 0; k < bestContour.size(); k++) {
        if (bestContour[k].y < minYval) {
            minYval = bestContour[k].y;
            minYXval = bestContour[k].x;
        }
    }


    Point2d A = Point2d(image.cols / 2, image.rows);
    Point2d B(minYXval, minYval);

    double lineLength = sqrt((A.x - B.x) * (A.x - B.x) + (A.y - B.y) * (A.y - B.y));
    double alpha = atan2((A.y - B.y), (A.x - B.x));
    Point2d C, D;
    double h = A.y - B.y;


    vector<Point> upperHalfFootContour;
    for (int i = 0; i < bestContour.size(); i++) {

        if (bestContour[i].y < minYval + (h) / 2) {

            upperHalfFootContour.push_back(bestContour[i]);
        }
    }

    Point2d pp1(0, minYval + (h) / 2);
    Point2d pp2(image.cols, minYval + (h) / 2);
    line(image, pp1, pp2, Scalar(0, 255, 255), 5);

    RotatedRect minRect;
    minRect = minAreaRect(Mat(upperHalfFootContour));

    double w = 0.;

    if (minRect.angle < -45.) { w = minRect.size.height; }
    else { w = minRect.size.width; }


    drawContours(image, vector<vector<Point> >(1, contours[maxId]), -1, Scalar(0, 0, 255), 2, 8);


    for (int jm = 0; jm < slices.size(); jm++) {

        double lineLength1 = lineLength * slices[jm];
        Point2d E(B.x + lineLength1 * cos(alpha), B.y + lineLength1 * sin(alpha));
        Point2d v(E.x - A.x, E.y - A.y);
        double mag = sqrt(v.x * v.x + v.y * v.y);
        v.x = v.x / mag;
        v.y = v.y / mag;
        double temp = v.x;
        v.x = -v.y;
        v.y = temp;
        C.x = E.x + v.x * 1000;
        C.y = E.y + v.y * 1000;
        D.x = E.x + v.x * -1000;
        D.y = E.y + v.y * -1000;
        Point2d leftP = D;
        Point2d rightP = C;


        for (int j = 0; j < bestContour.size() - 1; j++) {
            intersection(bestContour[j], bestContour[j + 1], C, E, leftP);
            intersection(bestContour[j], bestContour[j + 1], E, D, rightP);
        }

    }

    vector<double> heightAndWidthValues;
    heightAndWidthValues.push_back(h);
    heightAndWidthValues.push_back(w);
    resultFoot.heightAndWidthValues = heightAndWidthValues;
    resultFoot.footContour = bestContour;
    resultFoot.paperSize = Size(image.cols, image.rows);

    line(image, A, B, Scalar(255, 0, 0), 5);

    return resultFoot;
}


/**
 * Perspective correction of paper sheet
 * @return
 */
foot fourPointsTransformSix(Mat image, vector<Point> allPoints) {
    Mat imageC = image.clone();
    Point2f source_points[4];
    Point2f dest_points[4];
    Mat transformMatrix;

    vector<Point> four_points = sixPointsToFour(allPoints);


    Point P1 = four_points[0];
    Point P2 = four_points[1];
    Point P3 = four_points[2];
    Point P4 = four_points[3];
    Point A = allPoints[0];
    Point B = allPoints[1];
    Point C = allPoints[2];
    Point D = allPoints[3];
    Point E = allPoints[4];
    Point F = allPoints[5];


    source_points[0] = B;
    source_points[1] = C;
    source_points[2] = D;
    source_points[3] = E;

    dest_points[0] = Point(paperHeight - 1, paperWidth);
    dest_points[1] = Point(paperHeight - 1, 0);
    dest_points[2] = Point(0, 0);
    dest_points[3] = Point(0, paperWidth);

    Mat dst, toInsert;
    transformMatrix = getPerspectiveTransform(source_points, dest_points);
    vector<Point2f> toTransform;
    toTransform.push_back(P1);
    toTransform.push_back(C);
    toTransform.push_back(D);
    toTransform.push_back(P2);
    perspectiveTransform(toTransform, toTransform, transformMatrix);
    double distP1C = norm(toTransform[0] - toTransform[1]);
    double distP2D = norm(toTransform[2] - toTransform[3]);
    int height = int(round((distP1C + distP2D) / 2));
    cout << "Paper height " << height << endl;
    warpPerspective(imageC, dst, transformMatrix, Size(paperHeight, height));

//    imshow("Cropped",dst);
//    waitKey(0);

    Rect roi;
    roi.x = 60;
    roi.y = 0;
    roi.width = dst.cols - 120;
    roi.height = dst.rows;

    Mat crop = dst(roi);
    foot currentFoot = find_foot(crop);

    Mat inverseTransformMatrix = getPerspectiveTransform(dest_points, source_points);
    warpPerspective(dst, toInsert, transformMatrix, Size(image.cols, image.rows), CV_WARP_INVERSE_MAP);
    Mat gray, gray_inv;

    cvtColor(toInsert, gray, CV_BGR2GRAY);
    threshold(gray, gray, 0, 255, CV_THRESH_BINARY);
    bitwise_not(gray, gray_inv);
    toInsert.copyTo(image, gray);

    return currentFoot;

}

int checkContourAbove(vector<Point> &checkContour, Size imageSize, double &bestRatio, bool &rotated) {

    int width = imageSize.width;
    int height = imageSize.height;

    double minPerimeter = 2 * (width + height) * 0.2;
    double maxPerimeter = 2 * (width + height) * 0.95;


    if (fabs(arcLength(Mat(checkContour), true)) > minPerimeter &&
        fabs(arcLength(Mat(checkContour), true)) < maxPerimeter) {


        if (checkContour.size() == 6 && isContourConvex(Mat(checkContour))) {

            double maxCosine1 = 0, maxCosine2 = 0;

            vector<Point> pointsToCheck1, pointsToCheck2, pointsToCheck;

            pointsToCheck1.push_back(checkContour[3]);
            pointsToCheck1.push_back(checkContour[2]);
            pointsToCheck1.push_back(checkContour[1]);
            pointsToCheck1.push_back(checkContour[4]);

            pointsToCheck2.push_back(checkContour[4]);
            pointsToCheck2.push_back(checkContour[1]);
            pointsToCheck2.push_back(checkContour[0]);
            pointsToCheck2.push_back(checkContour[5]);


            for (int j = 2; j < 5; j++) {
                double cosine1 = fabs(
                        angle(pointsToCheck1[j % 4], pointsToCheck1[j - 2],
                              pointsToCheck1[j - 1]));
                double cosine2 = fabs(
                        angle(pointsToCheck2[j % 4], pointsToCheck2[j - 2],
                              pointsToCheck2[j - 1]));

                maxCosine1 = max(maxCosine1, cosine1);
                maxCosine2 = max(maxCosine2, cosine2);
            }


            if (maxCosine1 < 0.15 || maxCosine2 < 0.15) {

                vector<double> similarityCriterions1 = similarityToA4(pointsToCheck1);
                vector<double> similarityCriterions2 = similarityToA4(pointsToCheck2);
                double criterion1, criterion2, criterion3;

                if (abs(similarityCriterions1[2] - double(paperHeight) / double(paperWidth)) <
                    abs(similarityCriterions2[2] - double(paperHeight) / double(paperWidth))) {
                    criterion1 = similarityCriterions1[0];
                    criterion2 = similarityCriterions1[1];
                    criterion3 = similarityCriterions1[2];
                    rotated = false;
                    pointsToCheck = pointsToCheck1;
                    cout << "Don't need to rotate!" << endl;
                } else {
                    criterion1 = similarityCriterions2[0];
                    criterion2 = similarityCriterions2[1];
                    criterion3 = similarityCriterions2[2];
                    rotated = true;
                    pointsToCheck = pointsToCheck2;
                    cout << "Need to rotate!" << endl;
                }

                if (criterion1 > 0.80 &&
                    criterion2 > 0.80 &&
                    abs(criterion3 - double(paperHeight) / double(paperWidth)) < bestRatio) {


                    if (rotated) {
                        checkContour = orderAllPoints(
                                rotate_points(double(imageSize.width) / 2, double(imageSize.height) / 2, CV_PI,
                                              checkContour));

                    }
                    vector<Point> fpts = sixPointsToFour(checkContour);
                    vector<Point> paperC;
                    paperC.push_back(fpts[0]);
                    paperC.push_back(checkContour[2]);
                    paperC.push_back(checkContour[3]);
                    paperC.push_back(fpts[1]);
                    double crit1 = min(norm(paperC[0] - paperC[1]), norm(paperC[2] - paperC[3])) /
                                   max(norm(paperC[0] - paperC[1]), norm(paperC[2] - paperC[3]));
                    double crit2 = min(norm(paperC[0] - paperC[3]), norm(paperC[1] - paperC[2])) /
                                   max(norm(paperC[0] - paperC[3]), norm(paperC[1] - paperC[2]));
                    double crit3 = norm(checkContour[1] - checkContour[2])/norm(paperC[0]-paperC[1]);
                    double crit4 = norm(checkContour[3] - checkContour[4])/norm(paperC[2]-paperC[3]);

                    if (crit1 > 0.8 && crit2 > 0.8 && crit3 < 0.5 && crit4 < 0.5) {


                        double angle1 = acos(angle(checkContour[2], checkContour[5], fpts[0])) * 180 / CV_PI;
                        double angle2 = acos(angle(checkContour[3], checkContour[0], fpts[1])) * 180 / CV_PI;
                        if (norm(checkContour[0] - checkContour[5]) / norm(checkContour[2] - checkContour[3]) > 0.5 && min(angle1, angle2) / max(angle1, angle2) > 0.9) {


//                            double angle3 = atan2(checkContour[0].y - checkContour[5].y, checkContour[0].x - checkContour[5].x) * 180 / CV_PI;
//                            double angle4 = atan2(checkContour[2].y - checkContour[3].y, checkContour[2].x - checkContour[3].x) * 180 / CV_PI;
//                            if(min(angle3, angle4) / max(angle3, angle4) > 0.1){
                                bestRatio = abs(criterion3 - double(paperHeight) / double(paperWidth));
                                return PAPER_DETECTED;
//                            }else{
//                                return SHEETS_LIE_NOT_STRAIGHT;
//                            }


                        } else {
                            return ONE_CORNER_IS_HIDDEN;
                        }
                    } else {
                        return PAPER_CONTOUR_NOT_DETECTED_CORRECTLY;
                    }
                }
            }

        }
 else if (checkContour.size() == 7 && isContourConvex(Mat(checkContour))) {

            double maxCosine1 = 0, maxCosine2 = 0;

            vector<Point> pointsToCheck1, pointsToCheck2, pointsToCheck;

            pointsToCheck1.push_back(checkContour[3]);
            pointsToCheck1.push_back(checkContour[2]);
            pointsToCheck1.push_back(checkContour[1]);
            pointsToCheck1.push_back(checkContour[4]);

            pointsToCheck2.push_back(checkContour[5]);
            pointsToCheck2.push_back(checkContour[1]);
            pointsToCheck2.push_back(checkContour[0]);
            pointsToCheck2.push_back(checkContour[6]);


            for (int j = 2; j < 5; j++) {
                double cosine1 = fabs(
                        angle(pointsToCheck1[j % 4], pointsToCheck1[j - 2],
                              pointsToCheck1[j - 1]));
                double cosine2 = fabs(
                        angle(pointsToCheck2[j % 4], pointsToCheck2[j - 2],
                              pointsToCheck2[j - 1]));

                maxCosine1 = max(maxCosine1, cosine1);
                maxCosine2 = max(maxCosine2, cosine2);
            }

            if (maxCosine1 < 0.15 || maxCosine2 < 0.15) {
                vector<double> similarityCriterions1 = similarityToA4(pointsToCheck1);
                vector<double> similarityCriterions2 = similarityToA4(pointsToCheck2);
                double criterion1, criterion2, criterion3;

                if (abs(similarityCriterions1[2] - double(paperHeight) / double(paperWidth)) <
                    abs(similarityCriterions2[2] - double(paperHeight) / double(paperWidth))) {
                    criterion1 = similarityCriterions1[0];
                    criterion2 = similarityCriterions1[1];
                    criterion3 = similarityCriterions1[2];
                    pointsToCheck = pointsToCheck1;
                } else {
                    criterion1 = similarityCriterions2[0];
                    criterion2 = similarityCriterions2[1];
                    criterion3 = similarityCriterions2[2];
                    pointsToCheck = pointsToCheck2;
                }

                if (criterion1 > 0.80 &&
                    criterion2 > 0.80) {

                    return ONE_CORNER_IS_HIDDEN;
                }
            }
        }
        else if (checkContour.size() > 7 && isContourConvex(Mat(checkContour))){
            return PAPER_NOT_DETECTED;
        }
    }else{
        return TOO_FAR_FROM_PAPER;
    }
    return PAPER_NOT_DETECTED;
}


Mat equalizeIntensity(const Mat& inputImage)
{
    if(inputImage.channels() >= 3)
    {
        Mat ycrcb;

        cvtColor(inputImage,ycrcb,CV_BGR2YCrCb);

        vector<Mat> channels;
        split(ycrcb,channels);

        equalizeHist(channels[0], channels[0]);

        Mat result;
        merge(channels,ycrcb);

        cvtColor(ycrcb,result,CV_YCrCb2BGR);

        return result;
    }
    return Mat();
}
