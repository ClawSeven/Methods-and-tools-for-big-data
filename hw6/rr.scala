import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.regression.LinearRegressionWithSGD
// import org.apache.spark.mllib.regression.LinearRegressionModel

// Load and parse the data
val data = sc.textFile("pynum.csv").map { line => val parts = line.split(',')
  LabeledPoint(parts(parts.length-1).toDouble, Vectors.dense(parts.slice(0, parts.length).map(_.toDouble)))}.cache()

// // Building the model
// val numIterations = 100
// val stepSize = 0.00000001
// val model = LinearRegressionWithSGD.train(data, numIterations, stepSize)

// // Evaluate model on training examples and compute training error
// val valuesAndPreds = data.map { point => val prediction = model.predict(point.features)
//   (point.label, prediction)}
// val MSE = valuesAndPreds.map{case(v, p) => math.pow((v - p), 2) }.mean()
// println(s"training Mean Squared Error $MSE")

// split train and test
val Array(train,pred) = data.randomSplit(Array(0.8, 0.2))

val numIterations = 5
val stepSize = 0.00000001
val model = LinearRegressionWithSGD.train(train, numIterations, stepSize)

val valuesAndPreds = pred.map{ point => val prediction = model.predict(point.features)
  (point.label, prediction)}
val MSE = valuesAndPreds.map{case(v, p) => math.abs(v - p)}.mean()
println(s"training Mean Squared Error $MSE")

