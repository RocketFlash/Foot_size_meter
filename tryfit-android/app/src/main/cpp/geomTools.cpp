//
// Created by Рауф Ягфаров on 04/07/2017.
//

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/imgcodecs.hpp>
#include <iostream>
#include <fstream>
#include <math.h>
#include <opencv/cv.hpp>
#include "geomTools.h"
#include "parameters.h"

//A4 paper sheet parameters in mm
#define A4_WIDTH 210
#define A4_HEIGHT 297

using namespace cv;
using namespace std;


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
    cout << "Blure kernel size from " << blureLow << " to " << blureHigh << " with step "
         << blureStep << endl;
    cout << "---------------  Edge detection parameters  ----------------" << endl;
    cout << "Canny edge detector with" << endl;
    cout << "Threshold from " << thresholdLow << " to " << thresholdHigh << " with step "
         << thresholdStep << endl;
    cout << "Threshold ratio " << thresholdRatio << endl;
    cout << "Simple thresholding with" << endl;
    cout << "Threshold from " << thrLow << " to " << thrHigh << " with step " << thrStep << endl;
    cout << "Adaptive thresholding with" << endl;
    cout << "Threshold from " << threshAdaptiveLow << " to " << threshAdaptiveHigh << " with step "
         << threshAdaptiveStep << endl;
    cout << "Block size between " << blockSizeLow << " and " << blockSizeHigh << " with step "
         << blockSizeStep << endl;
    cout << "----------------------  Approximation  ---------------------" << endl;
    cout << "Convex hull" << endl;
    cout << "AproxPolyDP parameters:" << endl;
    cout << "Eps value from " << epsLow << " to " << epsHigh << " with step " << epsStep << endl;
    cout << "------------------------------------------------------------" << endl;
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
 *           pt1
 *          /
 *         /
 *        /
 *      pt0------pt2
 */
double angle(Point pt1, Point pt2, Point pt0) {
    double dx1 = pt1.x - pt0.x;
    double dy1 = pt1.y - pt0.y;
    double dx2 = pt2.x - pt0.x;
    double dy2 = pt2.y - pt0.y;
    return (dx1 * dx2 + dy1 * dy2) /
           sqrt((dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2) + 1e-10);
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
    Point2d Anew(X1line, Y1line1), A1new(X2line, Y2line1), Cnew(X1line, Y1line2), C1new(X2line,
                                                                                        Y2line2);
    intersection(Anew, A1new, Cnew, C1new, D);
    newContour.push_back(A);
    newContour.push_back(B);
    newContour.push_back(C);
    newContour.push_back(D);
    return newContour;
}

/**
 * Function to create four vertices of rectangle from six vertices
 * D-P4---P3-C
 * |         |
 * |         |
 * E         B
 * \         /
 * |\       /|
 * | \     / |
 * P2--F--A--P1
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
    Point2d Anew(X1line, Y1line1), A1new(X2line, Y2line1), Cnew(X1line, Y1line2), C1new(X2line,
                                                                                        Y2line2);
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

    Point2d Dnew(X1line, Y1line1), D1new(X2line, Y2line1), Enew(X1line, Y1line2), E1new(X2line,
                                                                                        Y2line2);
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
    newContour.push_back(P1);
    newContour.push_back(P2);
    newContour.push_back(P3);
    newContour.push_back(P4);


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

    double x = ((p1.x * p2.y - p2.x * p1.y) * (o1.x - o2.x) -
                (o1.x * o2.y - o2.x * o1.y) * (p1.x - p2.x)) /
               ((p1.x - p2.x) * (o1.y - o2.y) - (p1.y - p2.y) * (o1.x - o2.x));
    double y = ((p1.x * p2.y - p2.x * p1.y) * (o1.y - o2.y) -
                (o1.x * o2.y - o2.x * o1.y) * (p1.y - p2.y)) /
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

vector<double> getCurvature(vector<Point> const &vecContourPoints, int step) {
    vector<double> vecCurvature(vecContourPoints.size());

    if (vecContourPoints.size() < step)
        return vecCurvature;

    auto frontToBack = vecContourPoints.front() - vecContourPoints.back();

    bool isClosed = ((int) max(abs(frontToBack.x), abs(frontToBack.y))) <= 1;

    Point2f pplus, pminus;
    Point2f f1stDerivative, f2ndDerivative;
    for (int i = 0; i < vecContourPoints.size(); i++) {
        const Point2f &pos = vecContourPoints[i];

        int maxStep = step;
        if (!isClosed) {
            maxStep = min(min(step, i), (int) vecContourPoints.size() - 1 - i);
            if (maxStep == 0) {
                vecCurvature[i] = numeric_limits<double>::infinity();
                continue;
            }
        }


        int iminus = i - maxStep;
        int iplus = i + maxStep;
        pminus = vecContourPoints[iminus < 0 ? iminus + vecContourPoints.size() : iminus];
        pplus = vecContourPoints[iplus > vecContourPoints.size() ? iplus - vecContourPoints.size()
                                                                 : iplus];


        f1stDerivative.x = (pplus.x - pminus.x) / (iplus - iminus);
        f1stDerivative.y = (pplus.y - pminus.y) / (iplus - iminus);
        f2ndDerivative.x =
                (pplus.x - 2 * pos.x + pminus.x) / ((iplus - iminus) / 2 * (iplus - iminus) / 2);
        f2ndDerivative.y =
                (pplus.y - 2 * pos.y + pminus.y) / ((iplus - iminus) / 2 * (iplus - iminus) / 2);

        double curvature2D;
        double divisor = f1stDerivative.x * f1stDerivative.x + f1stDerivative.y * f1stDerivative.y;
        if (std::abs(divisor) > 10e-8) {
            curvature2D = std::abs(
                    f2ndDerivative.y * f1stDerivative.x - f2ndDerivative.x * f1stDerivative.y) /
                          pow(divisor, 3.0 / 2.0);
        } else {
            curvature2D = numeric_limits<double>::infinity();
        }

        vecCurvature[i] = curvature2D;


    }
    return vecCurvature;
}

double distance_to_line(Point begin, Point end, Point x) {
    //translate the begin to the origin
    end -= begin;
    x -= begin;
    double area = cross_product(x, end);
    return area / norm(end);
}

double cross_product(Point a, Point b) {
    return a.x * b.y - a.y * b.x;
}

double dot_product(Point3d a, Point3d b) {
    return a.x * b.x + a.y * b.y + a.z * b.z;
}

/**
 * Calculates angle between three points in 3d
 * @param A
 * @param B
 * @param C
 * @return
 */
double angle_between_three_points(Point3d A, Point3d B, Point3d C) {
    Point3d v1 = A - B;
    Point3d v2 = C - B;

    double v1mag = sqrt(v1.x * v1.x + v1.y * v1.y + v1.z * v1.z);
    Point3d v1norm(v1.x / v1mag, v1.y / v1mag, v1.z / v1mag);

    double v2mag = sqrt(v2.x * v2.x + v2.y * v2.y + v2.z * v2.z);
    Point3d v2norm(v2.x / v2mag, v2.y / v2mag, v2.z / v2mag);
    double res = v1norm.x * v2norm.x + v1norm.y * v2norm.y + v1norm.z * v2norm.z;
    return acos(res);
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
vector<Point> orderAllPoints(vector<Point> points) {
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
 * Generates parabola that passes through 3 points
 * @param start - X start coordinate
 * @param stop - X stop coordinate
 * @param step - X step
 * @param pt1 - first point
 * @param pt2 - second point
 * @param pt3 - third point
 * @return
 */
vector<Point2d>
getParabolaFromThreePoints(double start, double stop, double step, Point2d pt1, Point2d pt2,
                           Point2d pt3) {
    double A[9] = {pow(pt1.x, 2), pt1.x, 1, pow(pt2.x, 2), pt2.x, 1, pow(pt3.x, 2), pt3.x, 1};
    double B[3] = {pt1.y, pt2.y, pt3.y};
    Mat AcoeffMatrix = Mat(3, 3, CV_64FC1, &A);
    Mat BcoeffMatrix = Mat(3, 1, CV_64FC1, &B);
    Mat abc;

    // solve the linear system
    solve(AcoeffMatrix, BcoeffMatrix, abc);
    double a = abc.at<double>(0);
    double b = abc.at<double>(1);
    double c = abc.at<double>(2);

    vector<Point2d> parabola;

    for (double u = start; u < stop; u += step) {
        parabola.push_back(Point2d(u, pow(u, 2) * a + u * b + c));
    }
    return parabola;
}

/**
 * Rotate points around some point with (cx,cy) coordinates
 * @param cx
 * @param cy
 * @param angle
 * @param points
 * @return
 */
vector<Point> rotate_points(double cx, double cy, double angle, vector<Point> points) {

    vector<Point> rotatedPoints;
    double s, c, xnew, ynew;
    Point rotatedPoint;

    for (Point p: points) {
        s = sin(angle);
        c = cos(angle);

        // translate point back to origin:
        rotatedPoint.x = int(round(p.x - cx));
        rotatedPoint.y = int(round(p.y - cy));

        // rotate point
        xnew = rotatedPoint.x * c - rotatedPoint.y * s;
        ynew = rotatedPoint.x * s + rotatedPoint.y * c;

        // translate point back:
        rotatedPoint.x = int(round(xnew + cx));
        rotatedPoint.y = int(round(ynew + cy));
        rotatedPoints.push_back(rotatedPoint);
    }
    return rotatedPoints;
}

/**
 * Write point cloud in csv file
 * @param path - path where file must be saved
 * @param csvFilename - filename
 * @param points - point cloud
 */
void writePointsInCsv(string path, string csvFilename, vector<vector<Point3d>> points) {

    ofstream outputFile;
    // create and open the .csv file
    outputFile.open(path + csvFilename + ".csv");

    for (int i = 0; i < points.size(); i++) {
        for (int j = 0; j < points[i].size(); j++) {
            outputFile << points[i][j].x << "," << points[i][j].y << "," << points[i][j].z << ",";
        }
        outputFile << endl;
    }
    outputFile.close();
}

// comparison function object
bool compareContourLenghts(vector<Point> contour1, vector<Point> contour2) {
    double i = fabs(arcLength(Mat(contour1), true));
    double j = fabs(arcLength(Mat(contour2), true));
    return (i < j);
}