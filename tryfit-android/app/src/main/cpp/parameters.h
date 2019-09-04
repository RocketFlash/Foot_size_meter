//
// Created by Рауф Ягфаров on 06/07/2017.
//

#ifndef TRYFIT_PARAMETERS_H
#define TRYFIT_PARAMETERS_H

#include <opencv2/core/types.hpp>

//Return result types
#define TRY_FIT_LIB_RESULT_RESULT_POSITIVE 0
#define TRY_FIT_LIB_RESULT_PAPER_CONTOUR_NOT_FOUND 1
#define TRY_FIT_LIB_RESULT_FOOT_CONTOUR_NOT_FOUND 2

// Detection status
#define PAPER_DETECTED  0
#define ONE_CORNER_IS_HIDDEN  1
#define SHEETS_LIE_NOT_STRAIGHT 2
#define PAPER_CONTOUR_NOT_DETECTED_CORRECTLY 3
#define UPPER_PAPER_NOT_FOUND 4
#define TOO_FAR_FROM_PAPER  5
#define PAPER_NOT_DETECTED  6

using namespace std;
using namespace cv;

struct foot {
    vector<double> heightAndWidthValues;
    vector<Point> footContour;
    vector<Point> paperContour;
    Size paperSize;
    Size processingImageSize;
    bool rotate;
};

extern int newHeight;
extern int FPD_newHeight;


extern int paperHeight;
extern int paperWidth;

extern string projectPath;
extern int numberOfMethods;

// Median blure
extern int blureLow;
extern int blureHigh;
extern int blureStep;

// ApproxPolyDP
extern int epsLow;
extern int epsHigh;
extern int epsStep;

// Canny
extern int thresholdLow;
extern int thresholdHigh;
extern int thresholdStep;
extern double thresholdRatio;
extern int apertureLow;
extern int apertureHigh;

// Simple thresholding
extern int thrLow;
extern int thrHigh;
extern int thrStep;

// Adaptive
extern int threshAdaptiveLow;
extern int threshAdaptiveHigh;
extern int threshAdaptiveStep;
extern int blockSizeLow;
extern int blockSizeHigh;
extern int blockSizeStep;

// Parameters for contours analysis
extern double minPerimeterCoef;
extern double maxPerimeterCoef;
extern double maxPossibleCosine;
extern double minCriterion1;
extern double minCriterion2;
extern double minCriterion4;
extern double minCriterion5;
extern double minCriterion6;
extern double minCriterion7;

/**
 * Fast paper detector parameters
 */

extern int FPD_numberOfMethods;

// Median blure
extern int FPD_blureLow;
extern int FPD_blureHigh;
extern int FPD_blureStep;

// ApproxPolyDP
extern int FPD_epsLow;
extern int FPD_epsHigh;
extern int FPD_epsStep;

// Canny
extern int FPD_thresholdLow;
extern int FPD_thresholdHigh;
extern int FPD_thresholdStep;
extern double FPD_thresholdRatio;
extern int FPD_apertureLow;
extern int FPD_apertureHigh;

// Simple thresholding
extern int FPD_thrLow;
extern int FPD_thrHigh;
extern int FPD_thrStep;

// Adaptive
extern int FPD_threshAdaptiveLow;
extern int FPD_threshAdaptiveHigh;
extern int FPD_threshAdaptiveStep;
extern int FPD_blockSizeLow;
extern int FPD_blockSizeHigh;
extern int FPD_blockSizeStep;

// Parameters for contours analysis
extern double FPD_minPerimeterCoef;
extern double FPD_maxPerimeterCoef;
extern double FPD_maxPossibleCosine;
extern double FPD_minCriterion1;
extern double FPD_minCriterion2;
extern double FPD_minCriterion4;
extern double FPD_minCriterion5;
extern double FPD_minCriterion6;
extern double FPD_minCriterion7;

extern String methods[];

#endif //TRYFIT_PARAMETERS_H
