from pyspark import SparkContext as sc
from pyspark.mllib.regression import LabeledPoint, LinearRegressionWithSGD, LinearRegressionModel
from pyspark.mllib.feature import PCA
from random import randrange
from pyspark.mllib.linalg import DenseVector, DenseMatrix
import numpy as np


# Load and parse the data from csv
def parsePoint(line):
    values = [float(x) for x in line.split(',')]
    return LabeledPoint(values[-1], values[0:len(values)-1])


# Ridge regression
def rr_fit(parsed_Data):
    rdd = parsed_Data.randomSplit([0.8, 0.2])
    model = LinearRegressionWithSGD.train(rdd[0], iterations=100,
                                          step=0.00000001, regType="l2")

    # Evaluate the model on training data
    valuesAndPreds = rdd[1].map(lambda p: (p.label, model.predict(p.features)))
    MSE = valuesAndPreds.map(lambda vp: (vp[0] - vp[1])**2)\
              .reduce(lambda x, y: x + y) / valuesAndPreds.count()
    print("Mean Squared Error = " + str(MSE))

# principle component analysis
def pca_fit(parsed_Data):
    x = parsed_Data.map(lambda p: p.features)
    pc = PCA(5).fit(x)
    transformed = pc.transform(x)
    y = parsed_Data.map(lambda p: p.label)
    a = transformed.zip(y)
    paired = a.map(lambda line: LabeledPoint(line[1], line[0]))

    rdd2 = paired.randomSplit([0.8, 0.2])
    model2 = LinearRegressionWithSGD.train(rdd2[0], iterations=100,
                                           step=0.00000001, regType=None)

    # Evaluate the model on training data
    valuesAndPreds = rdd2[1].map(lambda p: (p.label, model2.predict(p.features)))
    MSE = valuesAndPreds.map(lambda vp: (vp[0] - vp[1])**2)\
              .reduce(lambda x, y: x + y) / valuesAndPreds.count()
    print("Mean Squared Error = " + str(MSE))


def main():
    data = sc.textFile("pynum.csv")
    parsedData = data.map(parsePoint)

    print("Ridge regression")
    rr_fit(parsedData)

    print("Principle component analysis")
    pca_fit(parsedData)


if __name__ == '__main__':
    main()