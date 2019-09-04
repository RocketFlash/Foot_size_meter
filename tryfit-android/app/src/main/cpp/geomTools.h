#include <opencv2/core/types.hpp>

#ifndef TRYFIT_GEOMTOOLS_H
#define TRYFIT_GEOMTOOLS_H

using namespace std;
using namespace cv;

//
// Created by Рауф Ягфаров on 04/07/2017.

bool has_suffix(const string &str, const string &suffix);

bool intersection(Point2d o1, Point2d p1, Point2d o2, Point2d p2, Point2d &r);

double angle(Point pt1, Point pt2, Point pt0);

double distance_to_line(Point begin, Point end, Point x);

double cross_product(Point a, Point b);

double dot_product(Point3d a, Point3d b);

double angle_between_three_points(Point3d A, Point3d B, Point3d C);

vector<double> similarityToA4(vector<Point> points);

vector<Point> findFourVertices(vector<Point> points);

vector<Point> sixPointsToFour(vector<Point> points);

vector<Point> orderAllPoints(vector<Point> points);

vector<Point> rotate_points(double cx, double cy, double angle, vector<Point> points);

vector<Point2f> orderPoints(vector<Point> points);

vector<Point2d>
getParabolaFromThreePoints(double start, double stop, double step, Point2d pt1, Point2d pt2,
                           Point2d pt3);

vector<double> getCurvature(vector<Point> const &vecContourPoints, int step);

void report();

void morphOps(Mat &thresh, int er, int di);

bool compareContourLenghts(vector<Point> contour1, vector<Point> contour2);

void writePointsInCsv(string path, string csvFilename, vector<vector<Point3d>> points);

#endif //TRYFIT_GEOMTOOLS_H
