//
// Created by Рауф Ягфаров on 30/06/2017.
//

/**
 * function, which finds paper sheet on an image
 */
vector<double> find_paper(Mat srcImage) {

    Mat image = srcImage.clone();

    Size sizeRgbaBefore = image.size();
    Size newSize(sizeRgbaBefore.width/3,sizeRgbaBefore.height/3);
    resize(image,image,newSize);
    Size sizeRgbaAfter = image.size();
    int maxId = -1;
    double bestRatio = 10000;
    double minCos = 1;

    int rows = sizeRgbaAfter.height;
    int cols = sizeRgbaAfter.width;

    int left = cols / 10;
    int top = rows / 10;

    int width = cols * 8 / 10;
    int height = rows * 8 / 10;
    double minPerimeter = 2*(width+height)*0.2;
    double maxPerimeter = 2*(width+height)*0.95;

    // Region of interest
    Point point1(left, top);
    Point point2(left + width, top);
    Point point3(left + width, top + height);
    Point point4(left, top + height);


    // Get subframe from full frame to speed up calculations
    Mat rgba;
    Rect roi(left, top, width, height);
    rgba = image(roi);

    Mat blurred;
    vector<vector<Point> > contours;
    vector<Vec4i> hierarchy;
    vector<Point> approxHullBest;
    vector<double> values;
    values.push_back(-1);
    values.push_back(-1);



    for (int method = 0; method < numberOfMethods; method++) {
        for (int blureKernelSize = blureLow; blureKernelSize <= blureHigh; blureKernelSize += blureStep) {
            Mat rgbaClone = rgba.clone();
            // blur will enhance edge detection
            medianBlur(rgbaClone, blurred, blureKernelSize);
            Mat gray0(blurred.size(), CV_8U);



            // find squares in every color plane of the image
            for (int c = 0; c < 4; c++) {
                if (c == 3) {
                    cvtColor(blurred, gray0, CV_BGR2GRAY);
                } else {
                    int ch[] = {c, 0};
                    mixChannels(&blurred, 1, &gray0, 1, ch, 1);
                }
                for (int trh = thresholdLow; trh <= thresholdHigh; trh += thresholdStep) {
                    Mat binary, tempBinary;
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
                        } else if (method == 2) {
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
                        } else if (method == 1) {
                            threshold(gray0, binary, simpleThreshold, 255, THRESH_BINARY);
                            simpleThreshold += thrStep;
                            if (simpleThreshold > thrHigh) {
                                stop = true;
                            }
                        }

                        // Find contours and store them in a list
                        findContours(binary, contours, hierarchy, CV_RETR_TREE, CV_CHAIN_APPROX_SIMPLE, Point(0, 0));
                        Mat bnry = binary.clone();
//                        resize(bnry, bnry, Size(bnry.cols/6, bnry.rows/6));
//                        imshow("Binary",bnry);
//                        waitKey(1);
                        // Test contours
                        vector<Point> approx;
                        for (int i = 0; i < contours.size(); i++) {

                            vector<Point> hull;
                            vector<Point> approxHull;

                            convexHull(Mat(contours[i]), hull, true);
//                            vector<vector<Point>> pots;
//                            pots.push_back(hull);
//                            if(fabs(contourArea(Mat(contours[i]))>minArea) ){
//                                Mat img = rgba.clone();
//                                drawContours(img, pots, 0, Scalar(255, 0, 255), 8);
//                                resize(img, img, Size(img.cols / 7, img.rows / 7));
//                                imshow("Find paper", img);
//                                waitKey(1000);
//                            }
                            for (int eps = epsLow; eps <= epsHigh; eps *= epsStep) {


                                approxPolyDP(Mat(hull), approxHull, arcLength(Mat(hull), true) * 0.0001 * eps,
                                             true);

                                if (fabs(arcLength(Mat(approxHull), true)) > minPerimeter &&
                                    fabs(arcLength(Mat(approxHull), true)) < maxPerimeter) {
//                                    if (approxHull.size() == 5) {
////                                    vector<vector<Point>> pots;
////                                    pots.push_back(approxHull);
////                                    Mat img = rgba.clone();
////                                    drawContours(img,pots,0,Scalar(255,0,255),8);
//                                        approxHull = findFourVertices(approxHull);
////                                    pots.push_back(approxHull);
////                                    drawContours(img,pots,1,Scalar(255,255,0),8);
////                                    resize(img, img, Size(img.cols/7, img.rows/7));
////                                    imshow("Find paper", img);
////                                    waitKey(0);
//                                    }

                                    if (approxHull.size() == 4 &&
                                        isContourConvex(Mat(approxHull))) {
                                        double maxCosine = 0;


                                        for (int j = 2; j < 5; j++) {
                                            double cosine = fabs(
                                                    angle(approxHull[j % 4], approxHull[j - 2], approxHull[j - 1]));
                                            maxCosine = max(maxCosine, cosine);
                                        }

                                        if (maxCosine < 0.1) {
                                            vector<double> similarityCriterions = similarityToA4(approxHull);
                                            double criterion1 = similarityCriterions[0];
                                            double criterion2 = similarityCriterions[1];
                                            double criterion3 = similarityCriterions[2];

                                            if (criterion1 > 0.85 &&
                                                criterion2 > 0.85 &&
                                                abs(criterion3 - double(A4_HEIGHT) / double(A4_WIDTH)) < 0.2) {

//                                                vector<vector<Point>> pots;
//                                                pots.push_back(approxHull);
//                                                Mat img = rgba.clone();
//                                                drawContours(img, pots, 0, Scalar(255, 0, 255), 8);
//                                                pots.push_back(approxHull);
//                                                drawContours(img, pots, 1, Scalar(255, 255, 0), 8);
//                                                resize(img, img, Size(img.cols / 7, img.rows / 7));
//                                                imshow("Find paper", img);
//                                                waitKey(1);

                                                if (abs(criterion3 - double(A4_HEIGHT) / double(A4_WIDTH)) <
                                                    bestRatio) {
                                                    bestRatio = criterion3;
                                                    maxId = i;
                                                    approxHullBest = approxHull;
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
                        }
                    }
                }
            }
            if(maxId>0){
                break;
            }
        }
        // If we have appropriate contour, we bound it with rectangle
        if (maxId >= 0) {
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
            values = fourPointsTransform(rgba, approxHullBest);
            drawContours(rgba, vector<vector<Point> >(1, approxHullBest), -1, Scalar(255, 0, 0), 2, 8);
            resize(image,image,sizeRgbaBefore);
            imwrite(projectPath + "contours/" + imageName + ".jpg", image);
            imwrite(projectPath + "binary/" + imageName + ".jpg", bestBinary);
        } else {
            cout << "Paper not detected by " << methods[method] << endl;
        }
        if (values[0] > 0) {
            break;
        }
    }
    rgba.release();
    image.release();



    return values;
}




int main() {


    report();
    namedWindow("Binary");
    namedWindow("Find paper");
    namedWindow("Transformed", WINDOW_NORMAL);

    Mat srcLeft, srcAbove, srcRight;
    int i = 1;
    vector<String> results;
    vector<String> pathsToImages;
    string imagesDirectory = "newSideT";
    string csvFilename = "valuesNewSideT";

    glob(projectPath + "dataset/" + imagesDirectory, results);

    ofstream outputFile;

    // create and open the .csv file
    outputFile.open(projectPath + csvFilename + ".csv");
    // write the file headers
    outputFile << "Filename" << "," << "HeightL" << "," << "WidthL" << ","<< "HeightA" << "," << "WidthA" << ","<< "HeightR" << "," << "WidthR" << "," << "Method" << ","
               << "Channel" << ","
               << "Kernel" << "," << "Eps" << endl;

    String s1, s2, s3;
    for (int j = 0; j < results.size(); j++) {
        if (has_suffix(results[j], ".jpg")) {
            pathsToImages.push_back(results[j]);
        }
    }

    for (int j = 0; j < pathsToImages.size(); j+=3) {
        s1 = pathsToImages[j];
        s2 = pathsToImages[j + 1];
        s3 = pathsToImages[j + 2];

        string toPrint = s1.substr(s1.find_last_of("/") + 1);
        imageName = toPrint.substr(0, toPrint.size() - 4);
        cout << "----------------------" << endl;
        cout << imageName << endl;

        srcLeft = imread(s1, IMREAD_COLOR);
        srcAbove = imread(s2, IMREAD_COLOR);
        srcRight = imread(s3, IMREAD_COLOR);

        if (srcLeft.cols > srcLeft.rows) {
            transpose(srcLeft, srcLeft);
            flip(srcLeft, srcLeft, 1);
        }

        if (srcAbove.cols > srcAbove.rows) {
            transpose(srcAbove, srcAbove);
            flip(srcAbove, srcAbove, 1);
        }

        if (srcRight.cols > srcRight.rows) {
            transpose(srcRight, srcRight);
            flip(srcRight, srcRight, 1);
        }
        vector<double> valuesLeftSide = find_paper_from_side(srcLeft);
        vector<double> valuesAbove = find_paper_from_above(srcAbove);
        vector<double> valuesRightSide = find_paper_from_side(srcRight);

        if (valuesLeftSide[0] > 0 && valuesRightSide[0] > 0 && valuesAbove[0] > 0) {
            cout << "Height: Left( " << valuesLeftSide[0] << " ), Above( "<<valuesAbove[0] << "), Right( "<< valuesRightSide[0] <<" )"<< endl;
            cout << "Width: Left( " << valuesLeftSide[1] << " ), Above( "<<valuesAbove[1] << "), Right( "<< valuesRightSide[1] <<" )"<< endl;
        } else {
            cout << "Foot not detected" << endl;
        }
        cout << "----------------------" << endl;
        outputFile << "image" << i++ << "," << int(round(valuesLeftSide[0])) << "," << int(round(valuesLeftSide[1])) << ","
                   << int(round(valuesAbove[0])) << "," << int(round(valuesAbove[1])) << ","
                   << int(round(valuesRightSide[0])) << "," << int(round(valuesRightSide[1])) << ","
                   << methods[bestMethod] << "," << colors[bestChannel] << ","
                   << bestBlurredKernelSize << "," << bestEps
                   << endl;

    }

    outputFile.close();
    return 0;
}





#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/imgcodecs.hpp>
#include <iostream>
#include <fstream>
#include <math.h>
#include <opencv/cv.hpp>


//A4 paper sheet parameters in mm
#define A4_WIDTH 210
#define A4_HEIGHT 297

//#define A4_WIDTH 297
//#define A4_HEIGHT 420
#define A4_RATIO A4_HEIGHT/A4_WIDTH

using namespace cv;
using namespace std;

vector<double> find_paper(Mat image);

vector<double> find_paper_from_side(Mat image);

vector<double> find_paper_from_above(Mat srcImage);

vector<double> find_foot(Mat paperImage);

vector<double> fourPointsTransform(Mat image, vector<Point> points);

vector<double> fourPointsTransformSix(Mat image, vector<Point> allPoints);

vector<double> similarityToA4(vector<Point> points);

vector<Point2f> orderPoints(vector<Point2f> points);

vector<Point> orderSixPoints(vector<Point> points);

void morphOps(Mat &thresh, int er, int di);

void report();

bool has_suffix(const string &str, const string &suffix);

double angle(Point pt1, Point pt2, Point pt0);

vector<Point> findFourVertices(vector<Point> points);

vector<Point> sixPointsToFour(vector<Point> points);

bool intersection(Point2d o1, Point2d p1, Point2d o2, Point2d p2,
                  Point2d &r);

vector<double> getCurvature(vector<Point> const& vecContourPoints, int step);

double cross_product( Point a, Point b );

double distance_to_line( Point begin, Point end, Point x );


string projectPath = "/Users/YagfarovRauf/ClionProjects/TryFit/";
string imageName;

int numberOfMethods = 3;

// Median blure
int blureLow = 7;
int blureHigh = 7;
int blureStep = 4;

// ApproxPolyDP
int epsLow = 30000;
int epsHigh = 30000;
int epsStep = 10;

// Canny
int thresholdLow = 63;
int thresholdHigh = 63;
int thresholdStep = 10;
double thresholdRatio = 4;
int apertureLow = 3;
int apertureHigh = 5;

// Simple thresholding
int thrLow = 100;
int thrHigh = 250;
int thrStep = 3;

// Adaptive
int threshAdaptiveLow = -10;
int threshAdaptiveHigh = -10;
int threshAdaptiveStep = 10;
int blockSizeLow = 17;
int blockSizeHigh = 17;
int blockSizeStep = 2;

int bestMethod = 0;
int bestThresholdAdaptive = 0;
int bestThresholdSimple = 0;
int bestBlockSize = 0;
int bestThresh = 0;
int bestChannel = 0;
int bestBlurredKernelSize = 0;
int bestEps = 0;
int bestAperture = 0;
Mat bestBinary;

vector<Point> leftContour;
vector<Point> rightContour;
vector<Point> aboveContour;

String colors[4] = {"blue", "green", "red", "gray"};
String methods[3] = {"Canny", "Adaptive thresholding", "Simple thresholding"};
//vector<double> slices = {0.1, 0.2, 0.3, 0.4, 0.5, 0.62, 0.68, 0.73, 0.8, 0.9};
vector<double> slices = {0.1, 0.2, 0.3, 0.4, 0.5};


//int main() {
//
//
//    report();
//    namedWindow("Binary");
//    namedWindow("Find paper");
//    namedWindow("Transformed", WINDOW_NORMAL);
//
//    Mat src;
//    int i = 1;
//    vector<String> results;
//    vector<String> pathsToImages;
//    string imagesDirectory = "newSideT";
//    string csvFilename = "valuesSideT";
//
//    glob(projectPath + "dataset/" + imagesDirectory, results);
//
//    ofstream outputFile;
//
//    // create and open the .csv file
//    outputFile.open(projectPath + csvFilename + ".csv");
//    // write the file headers
//    outputFile << "Filename" << "," << "HeightL" << "," << "WidthL" << ","<< "HeightA" << "," << "WidthA" << ","<< "HeightR" << "," << "WidthR" << "," << "Method" << ","
//               << "Channel" << ","
//               << "Kernel" << "," << "Eps" << endl;
//
//    String s;
//    for (int j = 0; j < results.size(); j++) {
//        if (has_suffix(results[j], ".jpg")) {
//            pathsToImages.push_back(results[j]);
//        }
//    }
//
//    for (int j = 0; j < pathsToImages.size(); j++) {
//        s = pathsToImages[j];
//
//        src = imread(s, IMREAD_COLOR);
//
//        if (src.cols > src.rows) {
//            transpose(src, src);
//            flip(src, src, 1);
//        }
//
//        string toPrint = s.substr(s.find_last_of("/") + 1);
//        imageName = toPrint.substr(0, toPrint.size() - 4);
//        cout << "----------------------" << endl;
//        cout <<"Left image: " << imageName << endl;
//        vector<double> valuesSide = find_paper_from_side(src);
//
//
//        if (valuesSide[0] > 0) {
//            cout << "Height: " << valuesSide[0] << endl;
//            cout << "Width: " << valuesSide[1] << endl;
//        } else {
//            cout << "Foot not detected correctly" << endl;
//        }
//
//        cout << "----------------------" << endl;
//        outputFile << "foot " << i++ << "," << int(round(valuesSide[0])) << "," << int(round(valuesSide[1])) << ","
//                   << methods[bestMethod] << "," << colors[bestChannel] << ","
//                   << bestBlurredKernelSize << "," << bestEps
//                   << endl;
//
//    }
//
//    outputFile.close();
//    return 0;
//}


//int main() {
//
//
//    report();
//    namedWindow("Binary");
//    namedWindow("Find paper");
//    namedWindow("Transformed", WINDOW_NORMAL);
//
//    Mat srcLeft, srcAbove, srcRight;
//    int i = 1;
//    vector<String> results;
//    vector<String> pathsToImages;
//    string imagesDirectory = "triptihRauf";
//    string csvFilename = "valuesTriptihRauf";
//
//    glob(projectPath + "dataset/" + imagesDirectory, results);
//
//    ofstream outputFile;
//
//    // create and open the .csv file
//    outputFile.open(projectPath + csvFilename + ".csv");
//    // write the file headers
//    outputFile << "Filename" << "," << "HeightL" << "," << "WidthL" << ","<< "HeightA" << "," << "WidthA" << ","<< "HeightR" << "," << "WidthR" << "," << "Method" << ","
//               << "Channel" << ","
//               << "Kernel" << "," << "Eps" << endl;
//
//    String s1, s2, s3;
//    for (int j = 0; j < results.size(); j++) {
//        if (has_suffix(results[j], ".jpg")) {
//            pathsToImages.push_back(results[j]);
//        }
//    }
//
//    for (int j = 0; j < pathsToImages.size(); j+=3) {
//        s1 = pathsToImages[j];
//        s2 = pathsToImages[j + 1];
//        s3 = pathsToImages[j + 2];
//
//        srcLeft = imread(s1, IMREAD_COLOR);
//        srcAbove = imread(s2, IMREAD_COLOR);
//        srcRight = imread(s3, IMREAD_COLOR);
//
//        if (srcLeft.cols > srcLeft.rows) {
//            transpose(srcLeft, srcLeft);
//            flip(srcLeft, srcLeft, 1);
//        }
//
//        if (srcAbove.cols > srcAbove.rows) {
//            transpose(srcAbove, srcAbove);
//            flip(srcAbove, srcAbove, 1);
//        }
//
//        if (srcRight.cols > srcRight.rows) {
//            transpose(srcRight, srcRight);
//            flip(srcRight, srcRight, 1);
//        }
//
//        string toPrint = s1.substr(s1.find_last_of("/") + 1);
//        imageName = toPrint.substr(0, toPrint.size() - 4);
//        cout << "----------------------" << endl;
//        cout <<"Left image: " << imageName << endl;
//        vector<double> valuesLeftSide = find_paper_from_side(srcLeft);
//
//        toPrint = s2.substr(s2.find_last_of("/") + 1);
//        imageName = toPrint.substr(0, toPrint.size() - 4);
//        cout << "----------------------" << endl;
//        cout << "Above image: " << imageName << endl;
//        vector<double> valuesAbove = find_paper_from_above(srcAbove);
//
//        toPrint = s3.substr(s3.find_last_of("/") + 1);
//        imageName = toPrint.substr(0, toPrint.size() - 4);
//        cout << "----------------------" << endl;
//        cout <<"Right image: " << imageName << endl;
//        vector<double> valuesRightSide = find_paper_from_side(srcRight);
//
//        if (valuesLeftSide[0] > 0 && valuesRightSide[0] > 0 && valuesAbove[0] > 0) {
//            cout << "Height: Left( " << valuesLeftSide[0] << " ), Above( "<<valuesAbove[0] << "), Right( "<< valuesRightSide[0] <<" )"<< endl;
//            cout << "Width: Left( " << valuesLeftSide[1] << " ), Above( "<<valuesAbove[1] << "), Right( "<< valuesRightSide[1] <<" )"<< endl;
//        } else {
//            cout << "Foot not detected correctly" << endl;
//        }
//        cout << "----------------------" << endl;
//        outputFile << "foot " << i++ << "," << int(round(valuesLeftSide[0])) << "," << int(round(valuesLeftSide[1])) << ","
//                   << int(round(valuesAbove[0])) << "," << int(round(valuesAbove[1])) << ","
//                   << int(round(valuesRightSide[0])) << "," << int(round(valuesRightSide[1])) << ","
//                   << methods[bestMethod] << "," << colors[bestChannel] << ","
//                   << bestBlurredKernelSize << "," << bestEps
//                   << endl;
//
//    }
//
//    outputFile.close();
//    return 0;
//}


int main() {


    report();

    Mat srcLeft, srcAbove, srcRight;
    int i = 1;
    vector<String> results;
    vector<String> pathsToImages;
    string imagesDirectory = "triptihRauf";
    string csvFilename = "valuesTriptihRauf";

    glob(projectPath + "dataset/" + imagesDirectory, results);

    ofstream outputFile;

    // create and open the .csv file
    outputFile.open(projectPath + csvFilename + ".csv");
    // write the file headers
    outputFile << "Filename" << "," << "HeightL" << "," << "WidthL" << ","<< "HeightA" << "," << "WidthA" << ","<< "HeightR" << "," << "WidthR" << "," << "Method" << ","
               << "Channel" << ","
               << "Kernel" << "," << "Eps" << endl;

    String s1, s2, s3;
    for (int j = 0; j < results.size(); j++) {
        if (has_suffix(results[j], ".jpg")) {
            pathsToImages.push_back(results[j]);
        }
    }

    for (int j = 0; j < pathsToImages.size(); j+=3) {
        s1 = pathsToImages[j];
        s2 = pathsToImages[j + 1];
        s3 = pathsToImages[j + 2];

        srcLeft = imread(s1, IMREAD_COLOR);
        srcAbove = imread(s2, IMREAD_COLOR);
        srcRight = imread(s3, IMREAD_COLOR);

        if (srcLeft.cols > srcLeft.rows) {
            transpose(srcLeft, srcLeft);
            flip(srcLeft, srcLeft, 1);
        }

        if (srcAbove.cols > srcAbove.rows) {
            transpose(srcAbove, srcAbove);
            flip(srcAbove, srcAbove, 1);
        }

        if (srcRight.cols > srcRight.rows) {
            transpose(srcRight, srcRight);
            flip(srcRight, srcRight, 1);
        }

        string toPrint = s1.substr(s1.find_last_of("/") + 1);
        imageName = toPrint.substr(0, toPrint.size() - 4);
        cout << "----------------------" << endl;
        cout <<"Left image: " << imageName << endl;
        vector<Point> valuesLeftSide = find_paper_from_side(srcLeft);

        toPrint = s2.substr(s2.find_last_of("/") + 1);
        imageName = toPrint.substr(0, toPrint.size() - 4);
        cout << "----------------------" << endl;
        cout << "Above image: " << imageName << endl;
        vector<Point> valuesAbove = find_paper_from_above(srcAbove);

        toPrint = s3.substr(s3.find_last_of("/") + 1);
        imageName = toPrint.substr(0, toPrint.size() - 4);
        cout << "----------------------" << endl;
        cout <<"Right image: " << imageName << endl;
        vector<Point> valuesRightSide = find_paper_from_side(srcRight);

        if (valuesLeftSide[0] > 0 && valuesRightSide[0] > 0 && valuesAbove[0] > 0) {
            cout << "Height: Left( " << valuesLeftSide[0] << " ), Above( "<<valuesAbove[0] << "), Right( "<< valuesRightSide[0] <<" )"<< endl;
            cout << "Width: Left( " << valuesLeftSide[1] << " ), Above( "<<valuesAbove[1] << "), Right( "<< valuesRightSide[1] <<" )"<< endl;
        } else {
            cout << "Foot not detected correctly" << endl;
        }
        cout << "----------------------" << endl;
        outputFile << "foot " << i++ << "," << int(round(valuesLeftSide[0])) << "," << int(round(valuesLeftSide[1])) << ","
                   << int(round(valuesAbove[0])) << "," << int(round(valuesAbove[1])) << ","
                   << int(round(valuesRightSide[0])) << "," << int(round(valuesRightSide[1])) << ","
                   << methods[bestMethod] << "," << colors[bestChannel] << ","
                   << bestBlurredKernelSize << "," << bestEps
                   << endl;

    }

    outputFile.close();
    return 0;
}

/**
 * function, which finds paper sheet on an image using photo from side
 */
vector<double> find_paper_from_side(Mat srcImage) {

    Mat image = srcImage.clone();

    Size sizeRgbaBefore = image.size();
    Size newSize(sizeRgbaBefore.width / 3, sizeRgbaBefore.height / 3);
    resize(image, image, newSize);
    Size sizeRgbaAfter = image.size();
    int maxId = -1;
    double bestRatio = 10000;

    int rows = sizeRgbaAfter.height;
    int cols = sizeRgbaAfter.width;

    int left = cols / 10;
    int top = rows / 10;

    int width = cols * 8 / 10;
    int height = rows * 8 / 10;
    double minPerimeter = 2 * (width + height) * 0.2;
    double maxPerimeter = 2 * (width + height) * 0.95;


    // Get subframe from full frame to speed up calculations
    Mat rgba;
    Rect roi(left, top, width, height);
    rgba = image(roi);

    Mat blurred;
    vector<vector<Point> > contours;
    vector<Vec4i> hierarchy;
    vector<Point> approxHullBest;
    vector<double> values;
    values.push_back(-1);
    values.push_back(-1);


    for (int method = 0; method < numberOfMethods; method++) {
        for (int blureKernelSize = blureLow; blureKernelSize <= blureHigh; blureKernelSize += blureStep) {
            Mat rgbaClone = rgba.clone();
            // blur will enhance edge detection
            medianBlur(rgbaClone, blurred, blureKernelSize);
            Mat gray0(blurred.size(), CV_8U);



            // find squares in every color plane of the image
            for (int c = 0; c < 4; c++) {
                if (c == 3) {
                    cvtColor(blurred, gray0, CV_BGR2GRAY);
                } else {
                    int ch[] = {c, 0};
                    mixChannels(&blurred, 1, &gray0, 1, ch, 1);
                }
                for (int trh = thresholdLow; trh <= thresholdHigh; trh += thresholdStep) {
                    Mat binary, tempBinary;
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
//                        Mat bnry = binary.clone();
//                        resize(bnry, bnry, Size(bnry.cols/3, bnry.rows/3));
//                        imshow("Binary",bnry);
                        imwrite(projectPath + "binary/" + imageName + ".jpg", binary);
//                        waitKey(1);
                        // Test contours
                        vector<Point> approx;
                        for (int i = 0; i < contours.size(); i++) {

                            vector<Point> hull;
                            vector<Point> approxHull;

                            convexHull(Mat(contours[i]), hull, true);
//                            vector<vector<Point>> pots;
//                            pots.push_back(hull);
//                            if(fabs(arcLength(Mat(hull), true)) > minPerimeter){
//                                Mat img = rgba.clone();
//                                drawContours(img, pots, 0, Scalar(255, 0, 255), 8);
//                                resize(img, img, Size(img.cols / 3, img.rows / 3));
//                                imshow("Find paper", img);
//                                waitKey(0);
//                            }

                            for (int eps = epsLow; eps <= epsHigh; eps *= epsStep) {


                                approxPolyDP(Mat(hull), approxHull, arcLength(Mat(hull), true) * 0.0000001 * eps,
                                             true);

                                if (fabs(arcLength(Mat(approxHull), true)) > minPerimeter &&
                                    fabs(arcLength(Mat(approxHull), true)) < maxPerimeter) {


                                    if (approxHull.size() == 6 &&
                                        isContourConvex(Mat(approxHull))) {
                                        double maxCosine = 0;


                                        vector<Point> orderedApproxHull = orderSixPoints(approxHull);


                                        vector<Point> pointsToCheck;
                                        pointsToCheck.push_back(orderedApproxHull[3]);
                                        pointsToCheck.push_back(orderedApproxHull[2]);
                                        pointsToCheck.push_back(orderedApproxHull[1]);
                                        pointsToCheck.push_back(orderedApproxHull[4]);


                                        for (int j = 2; j < 5; j++) {
                                            double cosine = fabs(
                                                    angle(pointsToCheck[j % 4], pointsToCheck[j - 2],
                                                          pointsToCheck[j - 1]));
                                            maxCosine = max(maxCosine, cosine);
                                        }

                                        if (maxCosine < 0.3) {
                                            vector<double> similarityCriterions = similarityToA4(pointsToCheck);
                                            double criterion1 = similarityCriterions[0];
                                            double criterion2 = similarityCriterions[1];
                                            double criterion3 = similarityCriterions[2];

                                            if (criterion1 > 0.60 &&
                                                criterion2 > 0.60 &&
                                                abs(criterion3 - double(A4_HEIGHT) / double(A4_WIDTH)) < bestRatio) {



//                                                if (abs(criterion3 - double(A4_HEIGHT) / double(A4_WIDTH)) <
//                                                    bestRatio) {
                                                bestRatio = abs(criterion3 - double(A4_HEIGHT) / double(A4_WIDTH));
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

                                                vector<vector<Point>> pots;
                                                pots.push_back(approxHull);
                                                Mat img = rgba.clone();
//                                                    drawContours(img, pots, 0, Scalar(255, 255, 0), 4);
                                                for (int hg = 0; hg < orderedApproxHull.size(); hg++) {
                                                    circle(img, orderedApproxHull[hg], 20, Scalar(255, 255, 255),
                                                           CV_FILLED, 8, 0);
                                                    putText(img, to_string(hg), orderedApproxHull[hg],
                                                            FONT_HERSHEY_SIMPLEX, 0.8, Scalar(0, 0, 0), 2, LINE_AA);
                                                }

//                                                    pots.push_back(pointsToCheck);
//                                                    drawContours(img,pots, 1, Scalar(255, 0, 255), 4);
//                                                    vector<Point> fpps = sixPointsToFour(approxHullBest);
//                                                    drawContours(img, vector<vector<Point> >(1, fpps), -1, Scalar(0, 255, 0), 2, 8);
//                                                    resize(img, img, Size(img.cols / 3, img.rows / 3));
//                                                    imshow("Find paper", img);
//                                                    waitKey(1);
//                                                }
                                            }
                                        }
                                    }
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

            values = fourPointsTransformSix(rgba, approxHullBest);

            vector<Point> fpts = sixPointsToFour(approxHullBest);

            drawContours(rgba, vector<vector<Point> >(1, approxHullBest), -1, Scalar(255, 0, 0), 2, 8);
            drawContours(rgba, vector<vector<Point> >(1, fpts), -1, Scalar(255, 255, 0), 2, 8);
            resize(image, image, sizeRgbaBefore);
            imwrite(projectPath + "contours/" + imageName + ".jpg", image);
            imwrite(projectPath + "binary/" + imageName + ".jpg", bestBinary);
        } else {
            cout << "Paper not detected by " << methods[method] << endl;
        }
        if (values[0] > 0) {
            break;
        }
    }
    rgba.release();
    image.release();


    return values;
}

/**
 * function, which finds paper sheet on an image using photo from above
 */
vector<double> find_paper_from_above(Mat srcImage) {

    Mat image = srcImage.clone();

    Size sizeRgbaBefore = image.size();
    Size newSize(sizeRgbaBefore.width / 3, sizeRgbaBefore.height / 3);
    resize(image, image, newSize);
    Size sizeRgbaAfter = image.size();
    int maxId = -1;
    double bestRatio = 10000;

    int rows = sizeRgbaAfter.height;
    int cols = sizeRgbaAfter.width;

    int left = cols / 10;
    int top = rows / 10;

    int width = cols * 8 / 10;
    int height = rows * 8 / 10;
    double minPerimeter = 2 * (width + height) * 0.2;
    double maxPerimeter = 2 * (width + height) * 0.95;


    // Get subframe from full frame to speed up calculations
    Mat rgba;
    Rect roi(left, top, width, height);
    rgba = image(roi);

    Mat blurred;
    vector<vector<Point> > contours;
    vector<Vec4i> hierarchy;
    vector<Point> approxHullBest;
    vector<double> values;
    values.push_back(-1);
    values.push_back(-1);


    for (int method = 0; method < numberOfMethods; method++) {
        for (int blureKernelSize = blureLow; blureKernelSize <= blureHigh; blureKernelSize += blureStep) {
            Mat rgbaClone = rgba.clone();
            // blur will enhance edge detection
            medianBlur(rgbaClone, blurred, blureKernelSize);
            Mat gray0(blurred.size(), CV_8U);



            // find squares in every color plane of the image
            for (int c = 0; c < 4; c++) {
                if (c == 3) {
                    cvtColor(blurred, gray0, CV_BGR2GRAY);
                } else {
                    int ch[] = {c, 0};
                    mixChannels(&blurred, 1, &gray0, 1, ch, 1);
                }
                for (int trh = thresholdLow; trh <= thresholdHigh; trh += thresholdStep) {
                    Mat binary, tempBinary;
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
//                        Mat bnry = binary.clone();
//                        resize(bnry, bnry, Size(bnry.cols/3, bnry.rows/3));
//                        imshow("Binary",bnry);
                        imwrite(projectPath + "binary/" + imageName + ".jpg", binary);
//                        waitKey(1);
                        // Test contours
                        vector<Point> approx;
                        for (int i = 0; i < contours.size(); i++) {

                            vector<Point> hull;
                            vector<Point> approxHull;

                            convexHull(Mat(contours[i]), hull, true);
//                            vector<vector<Point>> pots;
//                            pots.push_back(hull);
//                            if(fabs(arcLength(Mat(hull), true)) > minPerimeter){
//                                Mat img = rgba.clone();
//                                drawContours(img, pots, 0, Scalar(255, 0, 255), 8);
//                                resize(img, img, Size(img.cols / 3, img.rows / 3));
//                                imshow("Find paper", img);
//                                waitKey(0);
//                            }

                            for (int eps = epsLow; eps <= epsHigh; eps *= epsStep) {


                                approxPolyDP(Mat(hull), approxHull, arcLength(Mat(hull), true) * 0.0000001 * eps,
                                             true);

                                if (fabs(arcLength(Mat(approxHull), true)) > minPerimeter &&
                                    fabs(arcLength(Mat(approxHull), true)) < maxPerimeter) {


                                    if (approxHull.size() == 6 &&
                                        isContourConvex(Mat(approxHull))) {
                                        double maxCosine = 0;


                                        vector<Point> orderedApproxHull = orderSixPoints(approxHull);


                                        vector<Point> pointsToCheck;
                                        pointsToCheck.push_back(orderedApproxHull[3]);
                                        pointsToCheck.push_back(orderedApproxHull[2]);
                                        pointsToCheck.push_back(orderedApproxHull[1]);
                                        pointsToCheck.push_back(orderedApproxHull[4]);


                                        for (int j = 2; j < 5; j++) {
                                            double cosine = fabs(
                                                    angle(pointsToCheck[j % 4], pointsToCheck[j - 2],
                                                          pointsToCheck[j - 1]));
                                            maxCosine = max(maxCosine, cosine);
                                        }

                                        if (maxCosine < 0.15) {
                                            vector<double> similarityCriterions = similarityToA4(pointsToCheck);
                                            double criterion1 = similarityCriterions[0];
                                            double criterion2 = similarityCriterions[1];
                                            double criterion3 = similarityCriterions[2];

                                            if (criterion1 > 0.80 &&
                                                criterion2 > 0.80 &&
                                                abs(criterion3 - double(A4_HEIGHT) / double(A4_WIDTH)) < bestRatio) {



//                                                if (abs(criterion3 - double(A4_HEIGHT) / double(A4_WIDTH)) <
//                                                    bestRatio) {
                                                bestRatio = abs(criterion3 - double(A4_HEIGHT) / double(A4_WIDTH));
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

                                                vector<vector<Point>> pots;
                                                pots.push_back(approxHull);
                                                Mat img = rgba.clone();
//                                                    drawContours(img, pots, 0, Scalar(255, 255, 0), 4);
                                                for (int hg = 0; hg < orderedApproxHull.size(); hg++) {
                                                    circle(img, orderedApproxHull[hg], 20, Scalar(255, 255, 255),
                                                           CV_FILLED, 8, 0);
                                                    putText(img, to_string(hg), orderedApproxHull[hg],
                                                            FONT_HERSHEY_SIMPLEX, 0.8, Scalar(0, 0, 0), 2, LINE_AA);
                                                }

//                                                    pots.push_back(pointsToCheck);
//                                                    drawContours(img,pots, 1, Scalar(255, 0, 255), 4);
//                                                    vector<Point> fpps = sixPointsToFour(approxHullBest);
//                                                    drawContours(img, vector<vector<Point> >(1, fpps), -1, Scalar(0, 255, 0), 2, 8);
//                                                    resize(img, img, Size(img.cols / 3, img.rows / 3));
//                                                    imshow("Find paper", img);
//                                                    waitKey(1);
//                                                }
                                            }
                                        }
                                    }
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

            values = fourPointsTransformSix(rgba, approxHullBest);

            vector<Point> fpts = sixPointsToFour(approxHullBest);

            drawContours(rgba, vector<vector<Point> >(1, approxHullBest), -1, Scalar(255, 0, 0), 2, 8);
            drawContours(rgba, vector<vector<Point> >(1, fpts), -1, Scalar(255, 255, 0), 2, 8);
            resize(image, image, sizeRgbaBefore);
            imwrite(projectPath + "contours/" + imageName + ".jpg", image);
            imwrite(projectPath + "binary/" + imageName + ".jpg", bestBinary);
        } else {
            cout << "Paper not detected by " << methods[method] << endl;
        }
        if (values[0] > 0) {
            break;
        }
    }
    rgba.release();
    image.release();


    return values;
}

/**
 * function, which finds paper sheet on an image
 */
vector<double> find_paper(Mat srcImage) {

    Mat image = srcImage.clone();

    Size sizeRgbaBefore = image.size();
    Size newSize(sizeRgbaBefore.width / 3, sizeRgbaBefore.height / 3);
    resize(image, image, newSize);
    Size sizeRgbaAfter = image.size();
    int maxId = -1;
    double bestRatio = 10000;
    double minCos = 1;

    int rows = sizeRgbaAfter.height;
    int cols = sizeRgbaAfter.width;

    int left = cols / 10;
    int top = rows / 10;

    int width = cols * 8 / 10;
    int height = rows * 8 / 10;
    double minPerimeter = 2 * (width + height) * 0.2;
    double maxPerimeter = 2 * (width + height) * 0.95;

    // Region of interest
    Point point1(left, top);
    Point point2(left + width, top);
    Point point3(left + width, top + height);
    Point point4(left, top + height);


    // Get subframe from full frame to speed up calculations
    Mat rgba;
    Rect roi(left, top, width, height);
    rgba = image(roi);

    Mat blurred;
    vector<vector<Point> > contours;
    vector<Vec4i> hierarchy;
    vector<Point> approxHullBest;
    vector<double> values;
    values.push_back(-1);
    values.push_back(-1);


    for (int method = 0; method < numberOfMethods; method++) {
        for (int blureKernelSize = blureLow; blureKernelSize <= blureHigh; blureKernelSize += blureStep) {
            Mat rgbaClone = rgba.clone();
            // blur will enhance edge detection
            medianBlur(rgbaClone, blurred, blureKernelSize);
            Mat gray0(blurred.size(), CV_8U);



            // find squares in every color plane of the image
            for (int c = 0; c < 4; c++) {
                if (c == 3) {
                    cvtColor(blurred, gray0, CV_BGR2GRAY);
                } else {
                    int ch[] = {c, 0};
                    mixChannels(&blurred, 1, &gray0, 1, ch, 1);
                }
                for (int trh = thresholdLow; trh <= thresholdHigh; trh += thresholdStep) {
                    Mat binary, tempBinary;
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
                        } else if (method == 2) {
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
                        } else if (method == 1) {
                            threshold(gray0, binary, simpleThreshold, 255, THRESH_BINARY);
                            simpleThreshold += thrStep;
                            if (simpleThreshold > thrHigh) {
                                stop = true;
                            }
                        }

                        // Find contours and store them in a list
                        findContours(binary, contours, hierarchy, CV_RETR_TREE, CV_CHAIN_APPROX_SIMPLE, Point(0, 0));
                        Mat bnry = binary.clone();
//                        resize(bnry, bnry, Size(bnry.cols/6, bnry.rows/6));
//                        imshow("Binary",bnry);
//                        waitKey(1);
                        // Test contours
                        vector<Point> approx;
                        for (int i = 0; i < contours.size(); i++) {

                            vector<Point> hull;
                            vector<Point> approxHull;

                            convexHull(Mat(contours[i]), hull, true);
//                            vector<vector<Point>> pots;
//                            pots.push_back(hull);
//                            if(fabs(contourArea(Mat(contours[i]))>minArea) ){
//                                Mat img = rgba.clone();
//                                drawContours(img, pots, 0, Scalar(255, 0, 255), 8);
//                                resize(img, img, Size(img.cols / 7, img.rows / 7));
//                                imshow("Find paper", img);
//                                waitKey(1000);
//                            }
                            for (int eps = epsLow; eps <= epsHigh; eps *= epsStep) {


                                approxPolyDP(Mat(hull), approxHull, arcLength(Mat(hull), true) * 0.0000001 * eps,
                                             true);

                                if (fabs(arcLength(Mat(approxHull), true)) > minPerimeter &&
                                    fabs(arcLength(Mat(approxHull), true)) < maxPerimeter) {
//                                    if (approxHull.size() == 5) {
////                                    vector<vector<Point>> pots;
////                                    pots.push_back(approxHull);
////                                    Mat img = rgba.clone();
////                                    drawContours(img,pots,0,Scalar(255,0,255),8);
//                                        approxHull = findFourVertices(approxHull);
////                                    pots.push_back(approxHull);
////                                    drawContours(img,pots,1,Scalar(255,255,0),8);
////                                    resize(img, img, Size(img.cols/7, img.rows/7));
////                                    imshow("Find paper", img);
////                                    waitKey(0);
//                                    }

                                    if (approxHull.size() == 4 &&
                                        isContourConvex(Mat(approxHull))) {
                                        double maxCosine = 0;


                                        for (int j = 2; j < 5; j++) {
                                            double cosine = fabs(
                                                    angle(approxHull[j % 4], approxHull[j - 2], approxHull[j - 1]));
                                            maxCosine = max(maxCosine, cosine);
                                        }

                                        if (maxCosine < 0.1) {
                                            vector<double> similarityCriterions = similarityToA4(approxHull);
                                            double criterion1 = similarityCriterions[0];
                                            double criterion2 = similarityCriterions[1];
                                            double criterion3 = similarityCriterions[2];

                                            if (criterion1 > 0.85 &&
                                                criterion2 > 0.85 &&
                                                abs(criterion3 - double(A4_HEIGHT) / double(A4_WIDTH)) < 0.2) {

//                                                vector<vector<Point>> pots;
//                                                pots.push_back(approxHull);
//                                                Mat img = rgba.clone();
//                                                drawContours(img, pots, 0, Scalar(255, 0, 255), 8);
//                                                pots.push_back(approxHull);
//                                                drawContours(img, pots, 1, Scalar(255, 255, 0), 8);
//                                                resize(img, img, Size(img.cols / 7, img.rows / 7));
//                                                imshow("Find paper", img);
//                                                waitKey(1);

                                                if (abs(criterion3 - double(A4_HEIGHT) / double(A4_WIDTH)) <
                                                    bestRatio) {
                                                    bestRatio = criterion3;
                                                    maxId = i;
                                                    approxHullBest = approxHull;
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
            values = fourPointsTransform(rgba, approxHullBest);
            drawContours(rgba, vector<vector<Point> >(1, approxHullBest), -1, Scalar(255, 0, 0), 2, 8);
            resize(image, image, sizeRgbaBefore);
            imwrite(projectPath + "contours/" + imageName + ".jpg", image);
            imwrite(projectPath + "binary/" + imageName + ".jpg", bestBinary);
        } else {
            cout << "Paper not detected by " << methods[method] << endl;
        }
        if (values[0] > 0) {
            break;
        }
    }
    rgba.release();
    image.release();


    return values;
}

/**
 * Helper function to order points
 *  0-----------1
 *  |           |
 *  |           |
 *  |           |
 *  |           |
 *  3-----------2
 * @param points
 */
vector<Point2f> orderPoints(vector<Point> points) {
    vector<Point2f> rect;
    vector<double> sum;
    vector<double> diff;

    for (int i = 0; i < 4; i++) {
        sum.push_back(points[i].x + points[i].y);
        diff.push_back(points[i].y - points[i].x);
    }

    long argMinSum = distance(sum.begin(), min_element(sum.begin(), sum.end()));
    long argMaxSum = distance(sum.begin(), max_element(sum.begin(), sum.end()));
    long argMinDiff = distance(diff.begin(), min_element(diff.begin(), diff.end()));
    long argMaxDiff = distance(diff.begin(), max_element(diff.begin(), diff.end()));

    rect.push_back(points[argMinSum]);
    rect.push_back(points[argMinDiff]);
    rect.push_back(points[argMaxSum]);
    rect.push_back(points[argMaxDiff]);
    return rect;
}

/**
 * Helper function to order points
 *  0-----------1
 *  |           |
 *  |           |
 *  |           |
 *  |           |
 *  3-----------2
 * @param points
 */
vector<Point> orderSixPoints(vector<Point> points) {
    vector<Point> orderedPoints;
    vector<double> sum;
    vector<double> diff;
    double maxVal = 0;
    int maxId = 0;

    for (int i = 0; i < points.size(); i++) {
        if ((points[i].x + points[i].y) > maxVal) {
            maxVal = points[i].x + points[i].y;
            maxId = i;
        }
    }

    for (int j = 0; j < points.size(); j++) {
        orderedPoints.push_back(points[maxId++]);
        if (maxId > points.size() - 1) {
            maxId = 0;
        }
    }

    return orderedPoints;
}

/**
 * Perspective correction of paper sheet
 * @return
 */
vector<double> fourPointsTransform(Mat image, vector<Point> points) {
    Mat imageC = image.clone();
    vector<Point2f> ordered = orderPoints(points);

    Point2f source_points[4];
    Point2f dest_points[4];
    Mat transformMatrix;

    source_points[0] = ordered[0];
    source_points[1] = ordered[1];
    source_points[2] = ordered[2];
    source_points[3] = ordered[3];

    dest_points[0] = Point2f(0, 0);
    dest_points[1] = Point2f(A4_WIDTH - 1, 0);
    dest_points[2] = Point2f(A4_WIDTH - 1, A4_HEIGHT - 1);
    dest_points[3] = Point2f(0, A4_HEIGHT - 1);

    Mat dst, toInsert;
    transformMatrix = getPerspectiveTransform(source_points, dest_points);
    warpPerspective(imageC, dst, transformMatrix, Size(A4_WIDTH, A4_HEIGHT));

    vector<double> footHW = find_foot(dst);

    Mat inverseTransformMatrix = getPerspectiveTransform(dest_points, source_points);
    warpPerspective(dst, toInsert, transformMatrix, Size(image.cols, image.rows), CV_WARP_INVERSE_MAP);
    Mat gray, gray_inv;

    cvtColor(toInsert, gray, CV_BGR2GRAY);
    threshold(gray, gray, 0, 255, CV_THRESH_BINARY);
    bitwise_not(gray, gray_inv);
    toInsert.copyTo(image, gray);

    return footHW;

}

/**
 * Perspective correction of paper sheet
 * @return
 */
vector<double> fourPointsTransformSix(Mat image, vector<Point> allPoints) {
    Mat imageC = image.clone();
    Point2f source_points[4];
    Point2f dest_points[4];
    Mat transformMatrix;

    vector<Point> four_points = sixPointsToFour(allPoints);
    for (int j = 0; j < four_points.size(); j++) {
        source_points[j] = four_points[j];
    }

    Point P1 = source_points[0];
    Point P2 = source_points[3];
    Point C = allPoints[2];
    Point B = allPoints[1];
    Point D = allPoints[3];
    Point E = allPoints[4];


    double distP1C = norm(P1 - C);
    double distP2D = norm(P2 - D);
    double distDE = norm(D - E);
    double distCB = norm(C - B);

    int height = int(round(A4_WIDTH * ((distP1C + distP2D) / 2) / ((distCB + distDE) / 2)));
    dest_points[0] = Point(A4_WIDTH - 1, height - 1);
    dest_points[1] = Point(A4_WIDTH - 1, 0);
    dest_points[2] = Point(0, 0);
    dest_points[3] = Point(0, height - 1);

    Mat dst, toInsert;
    transformMatrix = getPerspectiveTransform(source_points, dest_points);
    warpPerspective(imageC, dst, transformMatrix, Size(A4_WIDTH, height));

    vector<double> footHW = find_foot(dst);

    Mat inverseTransformMatrix = getPerspectiveTransform(dest_points, source_points);
    warpPerspective(dst, toInsert, transformMatrix, Size(image.cols, image.rows), CV_WARP_INVERSE_MAP);
    Mat gray, gray_inv;

    cvtColor(toInsert, gray, CV_BGR2GRAY);
    threshold(gray, gray, 0, 255, CV_THRESH_BINARY);
    bitwise_not(gray, gray_inv);
    toInsert.copyTo(image, gray);

    return footHW;

}

/**
 * Function to find foot contour and calculate height and width
 * @param image
 * @return
 */
vector<double> find_foot(Mat image) {

    Mat imageC = image.clone();
    Mat blurred(imageC);
    medianBlur(imageC, blurred, 15);
    Mat gray0(blurred.size(), CV_8U);
    cvtColor(blurred, gray0, CV_BGR2GRAY);
    Mat binary;

//    threshold(gray0, binary, 100, 255, THRESH_BINARY);
//    adaptiveThreshold(gray0, binary, 255, ADAPTIVE_THRESH_GAUSSIAN_C, THRESH_BINARY,
//                      23, 0);

    Canny(gray0, binary, 0, 255);
    morphOps(binary, 1, 2);
    vector<vector<Point> > contours;
    vector<Vec4i> hierarchy;
    findContours(binary, contours, hierarchy, CV_RETR_TREE, CV_CHAIN_APPROX_SIMPLE, Point(0, 0));

//    Mat bnry = binary.clone();
//    resize(bnry, bnry, Size(bnry.cols, bnry.rows));
//    imshow("Binary",bnry);
//    waitKey(1);

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
    double maxYval = 0;
    double maxYXval = 0;
    Point maxYPt;
    for (int k = 0; k < bestContour.size(); k++) {
        if (bestContour[k].y < minYval) {
            minYval = bestContour[k].y;
            minYXval = bestContour[k].x;
        }
        if (bestContour[k].y > maxYval && bestContour[k].x > 50 && bestContour[k].x < imageC.cols - 50) {
            maxYval = bestContour[k].y;
            maxYPt = bestContour[k];
        }
    }


    vector<Point> hull;
    vector<int> hullsI; // Indices to contour points
    vector<Vec4i> defects;

    convexHull(bestContour, hull);
    convexHull(bestContour, hullsI);
    if (hullsI.size() > 3) {
        convexityDefects(bestContour, hullsI, defects);
    }





//    for(const Vec4i& v : defects){
//        float depth = v[3] / 256;
//        if (depth > 0) //  filter defects by depth, e.g more than 10
//        {
//
//            int startidx = v[0]; Point ptStart(bestContour[startidx]);
//            int endidx = v[1]; Point ptEnd(bestContour[endidx]);
//            int faridx = v[2]; Point ptFar(bestContour[faridx]);
//
//            if(ptEnd.y>maxYDefect && endidx!=0 && endidx!=bestContour.size()-1&& ptEnd.x > 20 && ptEnd.x<imageC.cols-20){
//                maxYDefect = ptEnd.y;
//                maxYPt = ptEnd;
//            }
//
////            circle(imageC, ptEnd, 4, Scalar(255, 0, 0), 2);
////            circle(imageC, ptStart, 4, Scalar(255, 255, 0), 2);
//            circle(imageC, ptFar, 4, Scalar(255, 0, 255), 2);
//        }
//    }




    Point2d A = maxYPt;
    Point2d B(minYXval, minYval);
    double h = A.y - B.y;

    vector<Point> bottomHalfFootContour;
    for (int i = 0; i < bestContour.size(); i++) {

        if (bestContour[i].y > minYval + (h) / 1.5) {

            bottomHalfFootContour.push_back(bestContour[i]);
        }
    }

    Point midUp(image.cols/2,0),midDown(image.cols/2,image.rows), closest;

    double minDist = 10000;
    double maxValCoordinate = 0;
    double curDist;

    for(int j = 0; j<bottomHalfFootContour.size();j++){
        curDist = abs(distance_to_line(midUp,midDown,bottomHalfFootContour[j]));

        if(maxValCoordinate < bottomHalfFootContour[j].y && curDist<minDist){
            maxValCoordinate = bottomHalfFootContour[j].y;
            minDist = curDist;
            closest = bottomHalfFootContour[j];
        }
    }

    A = Point(closest.x, closest.y);
    double lineLength = sqrt((A.x - B.x) * (A.x - B.x) + (A.y - B.y) * (A.y - B.y));
    double alpha = atan2((A.y - B.y), (A.x - B.x));
    Point2d C, D;
    h = A.y - B.y;


//    vector<double> curvatures = getCurvature(bottomHalfFootContour,1);
//    double maxValCurvature = 0;
//    Point maxValCurvPoint;
//    double closestDelta = 10;
//
//    for(int j = 0; j<bottomHalfFootContour.size();j++){
//        if(closest.x - closestDelta < bottomHalfFootContour[j].x && closest.x + closestDelta > bottomHalfFootContour[j].x && maxValCurvature<curvatures[j]){
//            maxValCurvature = curvatures[j];
//            maxValCurvPoint = bottomHalfFootContour[j];
//        }
//    }
//
//    A = maxValCurvPoint;
//    h = A.y - B.y;

    vector<Point> upperHalfFootContour;
    for (int i = 0; i < bestContour.size(); i++) {

        if (bestContour[i].y < minYval + (h) / 2) {

            upperHalfFootContour.push_back(bestContour[i]);
        }
    }

    RotatedRect minRect;
    minRect = minAreaRect(Mat(upperHalfFootContour));

    double w = 0.;

    if (minRect.angle < -45.) { w = minRect.size.height; }
    else { w = minRect.size.width; }


    drawContours(imageC, vector<vector<Point> >(1, contours[maxId]), -1, Scalar(0, 0, 255), 2, 8);


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

        line(imageC, leftP, rightP, Scalar(0, 255, 255), 2);
        circle(imageC, leftP, 4, Scalar(255, 255, 0), 2);
        circle(imageC, rightP, 4, Scalar(255, 255, 0), 2);
    }

    line(imageC, A, B, Scalar(0, 255, 0), 2);
    circle(imageC, A, 4, Scalar(255, 0, 0), 2);
    circle(imageC, B, 4, Scalar(255, 0, 0), 2);



//
    circle(imageC, A, 6, Scalar(0,255,255), -1);




    imageC.copyTo(image);

//    imshow("Transformed",image);
//    waitKey(0);

    vector<double> result;
    result.push_back(h);
    result.push_back(w);
    return result;
}

/**
 * helper function to check if file has certain suffix (for example: .txt , .jpg, .png)
 */
bool has_suffix(const string &str, const string &suffix) {
    return str.size() >= suffix.size() &&
           str.compare(str.size() - suffix.size(), suffix.size(), suffix) == 0;
}

/**
 * helper function to find cosine between three points
 */
double angle(Point pt1, Point pt2, Point pt0) {
    double dx1 = pt1.x - pt0.x;
    double dy1 = pt1.y - pt0.y;
    double dx2 = pt2.x - pt0.x;
    double dy2 = pt2.y - pt0.y;
    return (dx1 * dx2 + dy1 * dy2) / sqrt((dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2) + 1e-10);
}

/**
 * Helper function to make morphology operations
 */
void morphOps(Mat &thresh, int er, int di) {

    Size erSize = Size(2, 2);
    Size diSize = Size(2, 2);
    //create structuring element that will be used to "dilate" and "erode" image.
    Mat erodeElement = getStructuringElement(MORPH_RECT, erSize);
    //dilate with larger element so make sure object is nicely visible
    Mat dilateElement = getStructuringElement(MORPH_RECT, diSize);

    dilate(thresh, thresh, dilateElement, Point(-1, 1), di);
    erode(thresh, thresh, erodeElement, Point(-1, 1), er);
}

/**
 * Check similarity to A4 paper sheet
 * @param points - points of founded rectangle
 */
vector<double> similarityToA4(vector<Point> points) {
    vector<Point2f> ordered = orderPoints(points);

    double distance1 = norm(ordered[1] - ordered[0]);
    double distance2 = norm(ordered[2] - ordered[3]);
    double distance3 = norm(ordered[3] - ordered[0]);
    double distance4 = norm(ordered[2] - ordered[1]);

    double cr1 = min(distance1, distance2) / max(distance1, distance2);
    double cr2 = min(distance3, distance4) / max(distance3, distance4);
    double length1 = (distance3 + distance4) / 2;
    double length2 = (distance1 + distance2) / 2;
    double cr3 = max(length1, length2) / min(length1, length2);
    vector<double> criterions;
    criterions.push_back(cr1);
    criterions.push_back(cr2);
    criterions.push_back(cr3);
    return criterions;
}

/**
 * Function prints short report about algorithm parameters
 */
void report() {

    cout << "----------------------  Short review  ----------------------" << endl;
    cout << "Number of methods " << numberOfMethods << " :" << endl;
    for (int i = 0; i < numberOfMethods; i++) {
        cout << methods[i] << endl;
    }
    cout << "--------------------  Blure parameters  --------------------" << endl;
    cout << "Median filter with" << endl;
    cout << "Blure kernel size from " << blureLow << " to " << blureHigh << " with step " << blureStep << endl;
    cout << "---------------  Edge detection parameters  ----------------" << endl;
    cout << "Canny edge detector with" << endl;
    cout << "Threshold from " << thresholdLow << " to " << thresholdHigh << " with step " << thresholdStep << endl;
    cout << "Threshold ratio " << thresholdRatio << endl;
    cout << "Simple thresholding with" << endl;
    cout << "Threshold from " << thrLow << " to " << thrHigh << " with step " << thrStep << endl;
    cout << "Adaptive thresholding with" << endl;
    cout << "Threshold from " << threshAdaptiveLow << " to " << threshAdaptiveHigh << " with step "
         << threshAdaptiveStep << endl;
    cout << "Block size between " << blockSizeLow << " and " << blockSizeHigh << " with step " << blockSizeStep << endl;
    cout << "----------------------  Approximation  ---------------------" << endl;
    cout << "Convex hull" << endl;
    cout << "AproxPolyDP parameters:" << endl;
    cout << "Eps value from " << epsLow << " to " << epsHigh << " with step " << epsStep << endl;
    cout << "------------------------------------------------------------" << endl;
}

/**
 * Function to create four vertices of rectangle from five vertices
 * @param points - 5 points of pentagon
 * @return
 */
vector<Point> findFourVertices(vector<Point> points) {

    vector<Point> newContour;
    double maxLength = norm(points[points.size() - 1] - points[0]);
    double currentLength = 0;
    int maxId = 0;
    Point2d A, B, C, D, A1, C1;

    for (int i = 1; i < points.size(); i++) {
        currentLength = norm(points[i] - points[i - 1]);
        if (currentLength > maxLength) {
            maxLength = currentLength;
            maxId = i;
        }
    }

    int toCheck = ((maxId - 1 < 0) ? int(points.size() - 1) : maxId - 1);
    int toCheckDown = ((toCheck - 1 < 0) ? int(points.size() - 1) : toCheck - 1);
    int toCheckUp = ((maxId + 1 > points.size() - 1) ? 0 : maxId + 1);
    int toCheckDownDown = ((toCheckDown - 1 < 0) ? int(points.size() - 1) : toCheckDown - 1);
    int toCheckUpUp = ((toCheckUp + 1 > points.size() - 1) ? 0 : toCheckUp + 1);

    double left = norm(points[maxId] - points[toCheckUp]);
    double right = norm(points[toCheck] - points[toCheckDown]);
    if (left > right) {
        A = points[toCheck];
        B = points[maxId];
        C = points[toCheckUp];
        A1 = points[toCheckDown];
        C1 = points[toCheckUpUp];

    } else {
        A = points[maxId];
        B = points[toCheck];
        C = points[toCheckDown];
        A1 = points[toCheckUp];
        C1 = points[toCheckDownDown];
    }

    double y1 = C1.y;
    double x1 = C1.x;
    double y2 = C.y;
    double x2 = C.x;
    double y3 = A1.y;
    double x3 = A1.x;
    double y4 = A.y;
    double x4 = A.x;
    double X1line = -5000;
    double X2line = 5000;
    double Y1line1 = (y2 - y1) * ((X1line - x1) / (x2 - x1)) + y1;
    double Y2line1 = (y2 - y1) * ((X2line - x1) / (x2 - x1)) + y1;
    double Y1line2 = (y4 - y3) * ((X1line - x3) / (x4 - x3)) + y3;
    double Y2line2 = (y4 - y3) * ((X2line - x3) / (x4 - x3)) + y3;
    Point2d Anew(X1line, Y1line1), A1new(X2line, Y2line1), Cnew(X1line, Y1line2), C1new(X2line, Y2line2);
    intersection(Anew, A1new, Cnew, C1new, D);
    newContour.push_back(A);
    newContour.push_back(B);
    newContour.push_back(C);
    newContour.push_back(D);
    return newContour;
}

/**
 * Function to create four vertices of rectangle from six vertices
 * D-P3---P4-C
 * |         |
 * |         |
 * E         B
 * \         /
 * |\       /|
 * | \     / |
 * P1--F--A--P2
 * @param points - 6 points of hexagon
 * @return
 */
vector<Point> sixPointsToFour(vector<Point> points) {
    Point2d A, B, C, D, E, F, P1, P2, P3, P4;
    A = points[0];
    B = points[1];
    C = points[2];
    D = points[3];
    E = points[4];
    F = points[5];

    double y1 = B.y;
    double x1 = B.x;
    double y2 = C.y;
    double x2 = C.x;
    double y3 = F.y;
    double x3 = F.x;
    double y4 = A.y;
    double x4 = A.x;
    double X1line = -5000;
    double X2line = 5000;
    double Y1line1 = (y2 - y1) * ((X1line - x1) / (x2 - x1 + 1E-10)) + y1;
    double Y2line1 = (y2 - y1) * ((X2line - x1) / (x2 - x1 + 1E-10)) + y1;
    double Y1line2 = (y4 - y3) * ((X1line - x3) / (x4 - x3 + 1E-10)) + y3;
    double Y2line2 = (y4 - y3) * ((X2line - x3) / (x4 - x3 + 1E-10)) + y3;
    Point2d Anew(X1line, Y1line1), A1new(X2line, Y2line1), Cnew(X1line, Y1line2), C1new(X2line, Y2line2);
    intersection(Anew, A1new, Cnew, C1new, P1);


    y1 = D.y;
    x1 = D.x;
    y2 = E.y;
    x2 = E.x;
    y3 = A.y;
    x3 = A.x;
    y4 = F.y;
    x4 = F.x;

    Y1line1 = (y2 - y1) * ((X1line - x1) / (x2 - x1 + 1E-10)) + y1;
    Y2line1 = (y2 - y1) * ((X2line - x1) / (x2 - x1 + 1E-10)) + y1;
    Y1line2 = (y4 - y3) * ((X1line - x3) / (x4 - x3 + 1E-10)) + y3;
    Y2line2 = (y4 - y3) * ((X2line - x3) / (x4 - x3 + 1E-10)) + y3;

    Point2d Dnew(X1line, Y1line1), D1new(X2line, Y2line1), Enew(X1line, Y1line2), E1new(X2line, Y2line2);
    intersection(Dnew, D1new, Enew, E1new, P2);


    y1 = D.y;
    x1 = D.x;
    y2 = C.y;
    x2 = C.x;
    Y1line1 = (y2 - y1) * ((X1line - x1) / (x2 - x1 + 1E-10)) + y1;
    Y2line1 = (y2 - y1) * ((X2line - x1) / (x2 - x1 + 1E-10)) + y1;
    double K = 0;
    K = (D.y - P2.y) / (D.x - P2.x + 1E-10);
    Y1line2 = F.y + K * (X1line - F.x);
    Y2line2 = F.y + K * (X2line - F.x);
    Point2d Z11(X1line, Y1line1), Z12(X2line, Y2line1), Z21(X1line, Y1line2), Z22(X2line, Y2line2);
    intersection(Z11, Z12, Z21, Z22, P4);

    K = (P1.y - C.y) / (P1.x - C.x + 1E-10);
    Y1line2 = A.y + K * (X1line - A.x);
    Y2line2 = A.y + K * (X2line - A.x);


    Point2d R21(X1line, Y1line2), R22(X2line, Y2line2);
    intersection(Z11, Z12, R21, R22, P3);


    vector<Point> newContour;
    newContour.push_back(A);
    newContour.push_back(P3);
    newContour.push_back(P4);
    newContour.push_back(F);


    return newContour;

}

/**
 * Finds the intersection of two lines, or returns false.
 * The lines are defined by (p1, p2) and (o1, o2).
 * @param p1 - first line segment's first edge
 * @param p2 - first line segment's second edge
 * @param o1 - second line segment's first edge
 * @param o2 - second line segment's second edge
 * @param r - point of intersection if it exist
 * @return
 */
bool intersection(Point2d p1, Point2d p2, Point2d o1, Point2d o2,
                  Point2d &r) {

    double x = ((p1.x * p2.y - p2.x * p1.y) * (o1.x - o2.x) - (o1.x * o2.y - o2.x * o1.y) * (p1.x - p2.x)) /
               ((p1.x - p2.x) * (o1.y - o2.y) - (p1.y - p2.y) * (o1.x - o2.x));
    double y = ((p1.x * p2.y - p2.x * p1.y) * (o1.y - o2.y) - (o1.x * o2.y - o2.x * o1.y) * (p1.y - p2.y)) /
               ((p1.x - p2.x) * (o1.y - o2.y) - (p1.y - p2.y) * (o1.x - o2.x));

    if ((((o1.x - p1.x) * (p2.y - p1.y) - (o1.y - p1.y) * (p2.x - p1.x))
         * ((o2.x - p1.x) * (p2.y - p1.y) - (o2.y - p1.y) * (p2.x - p1.x)) < 0)
        &&
        (((p1.x - o1.x) * (o2.y - o1.y) - (p1.y - o1.y) * (o2.x - o1.x))
         * ((p2.x - o1.x) * (o2.y - o1.y) - (p2.y - o1.y) * (o2.x - o1.x)) < 0)) {
        r.x = x;
        r.y = y;
        return true;
    } else {
        return false;
    }

}

vector<double> getCurvature(vector<Point> const& vecContourPoints, int step){
    vector<double> vecCurvature( vecContourPoints.size() );

    if (vecContourPoints.size() < step)
        return vecCurvature;

    auto frontToBack = vecContourPoints.front() - vecContourPoints.back();

    bool isClosed = ((int)max(abs(frontToBack.x), abs(frontToBack.y))) <= 1;

    Point2f pplus, pminus;
    Point2f f1stDerivative, f2ndDerivative;
    for (int i = 0; i < vecContourPoints.size(); i++ ){
        const Point2f& pos = vecContourPoints[i];

        int maxStep = step;
        if (!isClosed){
            maxStep = min(min(step, i), (int)vecContourPoints.size()-1-i);
            if (maxStep == 0)
            {
                vecCurvature[i] = numeric_limits<double>::infinity();
                continue;
            }
        }


        int iminus = i-maxStep;
        int iplus = i+maxStep;
        pminus = vecContourPoints[iminus < 0 ? iminus + vecContourPoints.size() : iminus];
        pplus = vecContourPoints[iplus > vecContourPoints.size() ? iplus - vecContourPoints.size() : iplus];


        f1stDerivative.x =   (pplus.x -        pminus.x) / (iplus-iminus);
        f1stDerivative.y =   (pplus.y -        pminus.y) / (iplus-iminus);
        f2ndDerivative.x = (pplus.x - 2*pos.x + pminus.x) / ((iplus-iminus)/2*(iplus-iminus)/2);
        f2ndDerivative.y = (pplus.y - 2*pos.y + pminus.y) / ((iplus-iminus)/2*(iplus-iminus)/2);

        double curvature2D;
        double divisor = f1stDerivative.x*f1stDerivative.x + f1stDerivative.y*f1stDerivative.y;
        if ( std::abs(divisor) > 10e-8 ){
            curvature2D =  std::abs(f2ndDerivative.y*f1stDerivative.x - f2ndDerivative.x*f1stDerivative.y) /
                           pow(divisor, 3.0/2.0 )  ;
        }else{
            curvature2D = numeric_limits<double>::infinity();
        }

        vecCurvature[i] = curvature2D;


    }
    return vecCurvature;
}

double distance_to_line( Point begin, Point end, Point x ){
    //translate the begin to the origin
    end -= begin;
    x -= begin;
    double area = cross_product(x, end);
    return area / norm(end);
}

double cross_product( Point a, Point b ){
    return a.x*b.y - a.y*b.x;
}