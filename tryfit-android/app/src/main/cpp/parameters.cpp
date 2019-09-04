//
// Created by Рауф Ягфаров on 06/07/2017.
//

#include "parameters.h"


int paperHeight = 297;
int paperWidth = 210;

int newHeight = 1000;
int numberOfMethods = 3;

// Median blure
int blureLow = 7;
int blureHigh = 7;
int blureStep = 2;

// ApproxPolyDP
int epsLow = 80000;
int epsHigh = 100000;
int epsStep = 10000;

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

// Parameters for contours analysis
double minPerimeterCoef = 0.2;
double maxPerimeterCoef = 0.95;
double maxPossibleCosine = 0.4;
double minCriterion1 = 0.8;
double minCriterion2 = 0.8;
double minCriterion4 = 0.5;
double minCriterion5 = 0.5;
double minCriterion6 = 0.5;
double minCriterion7 = 0.88;


/**
 * Fast paper detector parameters
 */

int FPD_newHeight = 400;

int FPD_numberOfMethods = 2;

// Median blure
int FPD_blureLow = 5;
int FPD_blureHigh = 5;
int FPD_blureStep = 2;

// ApproxPolyDP
int FPD_epsLow = 55000;
int FPD_epsHigh = 55000;
int FPD_epsStep = 5000;

// Canny
int FPD_thresholdLow = 63;
int FPD_thresholdHigh = 63;
int FPD_thresholdStep = 10;
double FPD_thresholdRatio = 4;
int FPD_apertureLow = 3;
int FPD_apertureHigh = 5;

// Simple thresholding
int FPD_thrLow = 100;
int FPD_thrHigh = 250;
int FPD_thrStep = 3;

// Adaptive
int FPD_threshAdaptiveLow = -10;
int FPD_threshAdaptiveHigh = -10;
int FPD_threshAdaptiveStep = 10;
int FPD_blockSizeLow = 9;
int FPD_blockSizeHigh = 9;
int FPD_blockSizeStep = 2;

// Parameters for contours analysis
double FPD_minPerimeterCoef = 0.2;
double FPD_maxPerimeterCoef = 0.95;
double FPD_maxPossibleCosine = 0.4;
double FPD_minCriterion1 = 0.8;
double FPD_minCriterion2 = 0.8;
double FPD_minCriterion4 = 0.5;
double FPD_minCriterion5 = 0.5;
double FPD_minCriterion6 = 0.5;
double FPD_minCriterion7 = 0.88;

String methods[3] = {"Canny", "Adaptive thresholding", "Simple thresholding"};





