from pyspark import SparkContext as sc
from pyspark.mllib.regression import LabeledPoint, LinearRegressionWithSGD
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


# matrix D
def mat_D(x):
    d = randrange(-1, 2, 2)
    return d*x

# matrix B serves as filter
def mat_B_filter(x):
    d = randrange(0, 1024)
    return d < 500

# Hadamard transform
def hadamard_fit(data):
    # sample 1024 terms from data
    parsedData = data.map(lambda line: np.array([float(x) for x in line.split(',')]))
    rdd3 = sc.parallelize(parsedData.takeSample(True, 1024),2)

    # create Hadamard matrix
    N = 10
    H = np.zeros([1024, 1024])
    H[0, 0] = 1
    h = 1
    for i in range(N):
        H[0:h, h:2 * h] = H[0:h, 0:h]
        H[h:2 * h, 0:h] = H[0:h, 0:h]
        H[h:2 * h, h:2 * h] = -1 * H[0:h, 0:h]
        h = h * 2

    # multiply with Hadamard matrix
    lens = rdd3.collect()[0].shape[0]
    X_array = np.array(rdd3.collect()).reshape(1024, lens)
    X_hadamard = H.dot(X_array)

    x_rdd = sc.parallelize(X_hadamard)  # each entry is an numpy array
    subset = x_rdd.map(lambda x: LabeledPoint(x[-1], x[0:lens - 1])) \
        .randomSplit([0.8, 0.2])  # split training and testing
    x_rp = subset[0].filter(mat_B_filter)  # mat B actually serve as a filter
    model3 = LinearRegressionWithSGD.train(x_rp, iterations=100,
                                           step=0.00000001, regType=None)
    # Evaluate the model on training data
    valuesAndPreds = subset[1].map(lambda p: (p.label, model3.predict(p.features)))
    MSE = valuesAndPreds \
              .map(lambda vp: (vp[0] - vp[1]) ** 2) \
              .reduce(lambda x, y: x + y) / valuesAndPreds.count()
    print("Mean Squared Error = " + str(MSE))


def main():
    data = sc.textFile("pynum.csv")
    parsedData = data.map(parsePoint)

    print("Ridge regression")
    rr_fit(parsedData)

    print("Principle component analysis")
    pca_fit(parsedData)

    print("Hadamard transform")
    hadamard_fit(data)


if __name__ == '__main__':
    main()